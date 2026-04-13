package com.trading.simulator.service;

import com.trading.simulator.dto.*;
import com.trading.simulator.entity.User;
import com.trading.simulator.entity.Wallet;
import com.trading.simulator.repository.UserRepository;
import com.trading.simulator.repository.WalletRepository;
import com.trading.simulator.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authManager;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        // Create and save user
        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        userRepository.save(user);

        // Auto-create wallet with ₹1,00,000
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        walletRepository.save(wallet);

        String token = jwtUtil.generateToken(user.getEmail() , user.getId());
        return new AuthResponse(token, user.getName(), user.getEmail(),
                wallet.getBalance().doubleValue());
    }

    public AuthResponse login(LoginRequest req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        User user = userRepository.findByEmail(req.getEmail()).orElseThrow();
        Wallet wallet = walletRepository.findByUserId(user.getId()).orElseThrow();
        String token = jwtUtil.generateToken(user.getEmail() , user.getId());

        return new AuthResponse(token, user.getName(), user.getEmail(),
                wallet.getBalance().doubleValue());
    }
}
