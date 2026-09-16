package market;

import java.util.Optional;
import java.util.Random;

public final class NoiseTrader extends Trader {
    private final Random rng = new Random();
    private final double activityChance;

    public NoiseTrader(String name, double startingCash, double activityChance) {
        super(name, startingCash);
        this.activityChance = activityChance;
    }

    @Override
    public Optional<Order> decide(Market market) {
        if (rng.nextDouble() > activityChance) return Optional.empty();

        double amount = 10 + rng.nextDouble() * 40;
        boolean buy = rng.nextBoolean();
        if (buy && cash >= amount) {
            return Optional.of(new Order(TransactionType.BUY, amount));
        } else if (!buy && holdingsValue >= amount) {
            return Optional.of(new Order(TransactionType.SELL, amount));
        }
        return Optional.empty();
    }
}
