package market;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public final class MomentumTrader extends Trader {
    private final Random rng = new Random();
    private final int windowSize;

    public MomentumTrader(String name, double startingCash, double startingHoldingsValue, int windowSize) {
        super(name, startingCash, startingHoldingsValue);
        this.windowSize = windowSize;
    }

    @Override
    public Optional<Order> decide(Market market) {
        List<Double> history = market.priceHistory().recent();
        if (history.size() < windowSize) return Optional.empty();

        double newest = history.get(0);
        double oldest = history.get(windowSize - 1);
        double trend = newest - oldest;

        if (Math.abs(trend) < 0.01) return Optional.empty();

        double amount = 20 + rng.nextDouble() * 60;
        if (trend > 0 && cash >= amount) {
            return Optional.of(new Order(TransactionType.BUY, amount));
        } else if (trend < 0 && holdingsValue >= amount) {
            return Optional.of(new Order(TransactionType.SELL, amount));
        }
        return Optional.empty();
    }
}