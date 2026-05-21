package com.trading.simulator.service;

import com.trading.simulator.entity.PriceCandle;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

@Service
public class IndicatorService {

    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);
    private static final BigDecimal TWO = new BigDecimal("2");
    private static final int SCALE = 4;

    // ─── SMA ────────────────────────────────────────────────────────────────────
    // Simple average of last N closes. Result[i] is null until we have enough data.
    public List<BigDecimal> sma(List<BigDecimal> closes, int period) {
        List<BigDecimal> result = new ArrayList<>(Collections.nCopies(closes.size(), null));
        for (int i = period - 1; i < closes.size(); i++) {
            BigDecimal sum = BigDecimal.ZERO;
            for (int j = i - period + 1; j <= i; j++) {
                sum = sum.add(closes.get(j));
            }
            result.set(i, sum.divide(BigDecimal.valueOf(period), SCALE, RoundingMode.HALF_UP));
        }
        return result;
    }

    // ─── EMA ────────────────────────────────────────────────────────────────────
    // Exponential moving average — recent prices get higher weight.
    // multiplier = 2 / (period + 1)
    public List<BigDecimal> ema(List<BigDecimal> closes, int period) {
        List<BigDecimal> result = new ArrayList<>(Collections.nCopies(closes.size(), null));
        if (closes.size() < period) return result;

        BigDecimal multiplier = TWO.divide(BigDecimal.valueOf(period + 1), MC);

        // Seed EMA with first SMA
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < period; i++) sum = sum.add(closes.get(i));
        BigDecimal prevEma = sum.divide(BigDecimal.valueOf(period), SCALE, RoundingMode.HALF_UP);
        result.set(period - 1, prevEma);

        for (int i = period; i < closes.size(); i++) {
            // EMA = (close - prevEMA) * multiplier + prevEMA
            BigDecimal currentEma = closes.get(i)
                    .subtract(prevEma)
                    .multiply(multiplier, MC)
                    .add(prevEma)
                    .setScale(SCALE, RoundingMode.HALF_UP);
            result.set(i, currentEma);
            prevEma = currentEma;
        }
        return result;
    }

    // ─── RSI ────────────────────────────────────────────────────────────────────
    // RSI(14) — measures momentum. Above 70 = overbought, below 30 = oversold.
    // Uses Wilder's smoothing (not simple average).
    public List<BigDecimal> rsi(List<BigDecimal> closes, int period) {
        List<BigDecimal> result = new ArrayList<>(Collections.nCopies(closes.size(), null));
        if (closes.size() < period + 1) return result;

        // First average gain/loss over initial period
        BigDecimal avgGain = BigDecimal.ZERO;
        BigDecimal avgLoss = BigDecimal.ZERO;

        for (int i = 1; i <= period; i++) {
            BigDecimal change = closes.get(i).subtract(closes.get(i - 1));
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                avgGain = avgGain.add(change);
            } else {
                avgLoss = avgLoss.add(change.abs());
            }
        }
        avgGain = avgGain.divide(BigDecimal.valueOf(period), MC);
        avgLoss = avgLoss.divide(BigDecimal.valueOf(period), MC);

        result.set(period, computeRsiValue(avgGain, avgLoss));

        // Wilder's smoothing for subsequent values
        for (int i = period + 1; i < closes.size(); i++) {
            BigDecimal change = closes.get(i).subtract(closes.get(i - 1));
            BigDecimal gain = change.compareTo(BigDecimal.ZERO) > 0 ? change : BigDecimal.ZERO;
            BigDecimal loss = change.compareTo(BigDecimal.ZERO) < 0 ? change.abs() : BigDecimal.ZERO;

            avgGain = avgGain.multiply(BigDecimal.valueOf(period - 1), MC)
                    .add(gain).divide(BigDecimal.valueOf(period), MC);
            avgLoss = avgLoss.multiply(BigDecimal.valueOf(period - 1), MC)
                    .add(loss).divide(BigDecimal.valueOf(period), MC);

            result.set(i, computeRsiValue(avgGain, avgLoss));
        }
        return result;
    }

    private BigDecimal computeRsiValue(BigDecimal avgGain, BigDecimal avgLoss) {
        if (avgLoss.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal("100");
        }
        BigDecimal rs = avgGain.divide(avgLoss, MC);
        // RSI = 100 - (100 / (1 + RS))
        BigDecimal rsi = BigDecimal.valueOf(100)
                .subtract(BigDecimal.valueOf(100)
                        .divide(BigDecimal.ONE.add(rs), MC));
        return rsi.setScale(SCALE, RoundingMode.HALF_UP);
    }

    // ─── MACD ───────────────────────────────────────────────────────────────────
    // MACD Line = EMA(12) - EMA(26)
    // Signal Line = EMA(9) of MACD Line
    // Histogram = MACD Line - Signal Line
    public Map<String, List<BigDecimal>> macd(List<BigDecimal> closes) {
        List<BigDecimal> ema12 = ema(closes, 12);
        List<BigDecimal> ema26 = ema(closes, 26);

        List<BigDecimal> macdLine = new ArrayList<>(Collections.nCopies(closes.size(), null));
        for (int i = 0; i < closes.size(); i++) {
            if (ema12.get(i) != null && ema26.get(i) != null) {
                macdLine.set(i, ema12.get(i).subtract(ema26.get(i)).setScale(SCALE, RoundingMode.HALF_UP));
            }
        }

        // Extract non-null MACD values to compute Signal EMA(9)
        // We need to pass a contiguous list to ema(), so we track offset
        int macdStart = 25; // EMA(26) starts at index 25
        List<BigDecimal> macdValues = macdLine.subList(macdStart, macdLine.size());
        List<BigDecimal> signalValues = ema(macdValues, 9);

        List<BigDecimal> signal = new ArrayList<>(Collections.nCopies(closes.size(), null));
        List<BigDecimal> histogram = new ArrayList<>(Collections.nCopies(closes.size(), null));

        for (int i = 0; i < signalValues.size(); i++) {
            int idx = i + macdStart;
            signal.set(idx, signalValues.get(i));
            if (macdLine.get(idx) != null && signalValues.get(i) != null) {
                histogram.set(idx, macdLine.get(idx).subtract(signalValues.get(i))
                        .setScale(SCALE, RoundingMode.HALF_UP));
            }
        }

        return Map.of("macd", macdLine, "signal", signal, "histogram", histogram);
    }

    // ─── Bollinger Bands ────────────────────────────────────────────────────────
    // Middle = SMA(20)
    // Upper = SMA(20) + 2 * stddev
    // Lower = SMA(20) - 2 * stddev
    public Map<String, List<BigDecimal>> bollingerBands(List<BigDecimal> closes, int period) {
        List<BigDecimal> middle = sma(closes, period);
        List<BigDecimal> upper = new ArrayList<>(Collections.nCopies(closes.size(), null));
        List<BigDecimal> lower = new ArrayList<>(Collections.nCopies(closes.size(), null));

        for (int i = period - 1; i < closes.size(); i++) {
            BigDecimal mean = middle.get(i);
            // Standard deviation of the window
            BigDecimal variance = BigDecimal.ZERO;
            for (int j = i - period + 1; j <= i; j++) {
                BigDecimal diff = closes.get(j).subtract(mean);
                variance = variance.add(diff.multiply(diff, MC));
            }
            variance = variance.divide(BigDecimal.valueOf(period), MC);
            BigDecimal stddev = variance.sqrt(MC).setScale(SCALE, RoundingMode.HALF_UP);

            upper.set(i, mean.add(stddev.multiply(TWO)).setScale(SCALE, RoundingMode.HALF_UP));
            lower.set(i, mean.subtract(stddev.multiply(TWO)).setScale(SCALE, RoundingMode.HALF_UP));
        }

        return Map.of("middle", middle, "upper", upper, "lower", lower);
    }

    // ─── VWAP ───────────────────────────────────────────────────────────────────
    // VWAP = Σ(Typical Price × Volume) / Σ(Volume)
    // Typical Price = (High + Low + Close) / 3
    // Calculated cumulatively from the first candle provided
    public List<BigDecimal> vwap(List<PriceCandle> candles) {
        List<BigDecimal> result = new ArrayList<>();
        BigDecimal cumulativePV = BigDecimal.ZERO;
        BigDecimal cumulativeVol = BigDecimal.ZERO;

        for (PriceCandle c : candles) {
            BigDecimal typicalPrice = c.getHigh().add(c.getLow()).add(c.getClose())
                    .divide(BigDecimal.valueOf(3), MC);
            BigDecimal vol = BigDecimal.valueOf(c.getVolume());
            cumulativePV = cumulativePV.add(typicalPrice.multiply(vol, MC));
            cumulativeVol = cumulativeVol.add(vol);

            if (cumulativeVol.compareTo(BigDecimal.ZERO) == 0) {
                result.add(null);
            } else {
                result.add(cumulativePV.divide(cumulativeVol, SCALE, RoundingMode.HALF_UP));
            }
        }
        return result;
    }
}
