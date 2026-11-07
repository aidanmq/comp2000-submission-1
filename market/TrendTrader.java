package market;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public final class TrendTrader extends Trader {
    private final Random rng = new Random();
    private final int windowSize;
    private final TrendReaction reaction;

    public TrendTrader(String name, double startingCash, int windowSize, TrendReaction reaction) {
        super(name, startingCash);
        this.windowSize = windowSize;
        this.reaction = reaction;
    }

    public static TrendTrader momentum(String name, double startingCash, int windowSize) {
        return new TrendTrader(name, startingCash, windowSize,
            trend -> trend > 0 ? TransactionType.BUY : TransactionType.SELL);
    }

    public static TrendTrader contrarian(String name, double startingCash, int windowSize) {
        return new TrendTrader(name, startingCash, windowSize,
            trend -> trend > 0 ? TransactionType.SELL : TransactionType.BUY);
    }

    @Override
    public Optional<Order> decide(Market market) {
        List<Double> history = market.priceHistory().recent();
        if (history.size() < windowSize) return Optional.empty();

        double trend = history.stream().limit(windowSize).collect(PriceSummary.collector()).trend();

        if (Math.abs(trend) < 0.01) return Optional.empty(); // flat - sit out

        double amount = 20 + rng.nextDouble() * 60;
        TransactionType side = reaction.sideFor(trend);
        if (side == TransactionType.BUY && cash >= amount) {
            return Optional.of(new Order(TransactionType.BUY, amount));
        } else if (side == TransactionType.SELL && holdingsValue >= amount) {
            return Optional.of(new Order(TransactionType.SELL, amount));
        }
        return Optional.empty();
    }
}