package com.trading.simulator.service;

import com.trading.simulator.entity.User;
import com.trading.simulator.entity.Wallet;
import com.trading.simulator.repository.UserRepository;
import com.trading.simulator.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BotAccountService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> provisionBots(String botType, int count) {
        List<User> bots = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            String email = "bot_" + botType + "_" + i + "@sim.internal";

            Optional<User> existing = userRepository.findByEmail(email);

            User bot;
            if (existing.isPresent()) {
                // Bot already provisioned in a previous run — reuse it
                bot = existing.get();
            } else {
                // Create new bot user account
                User newBot = new User();
                newBot.setEmail(email);
                newBot.setName("Bot " + botType + " #" + i);
                newBot.setPassword(passwordEncoder.encode("bot-internal-password"));
                newBot.setBot(true);
                bot = userRepository.save(newBot);

                // Provision wallet with generous capital
                Wallet wallet = new Wallet();
                wallet.setUser(bot);
                wallet.setBalance(new BigDecimal("5000000"));
                walletRepository.save(wallet);
            }

            bots.add(bot);
        }

        return bots;
    }
}