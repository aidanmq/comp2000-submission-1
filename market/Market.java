package market;

import java.util.Random;

public final class Market {
    public static final String TICKER = "FAKE";

    private static final double MIN_PRICE = 0.50;
    private static final double DRIFT_NOISE = 0.05; 

    private double price;
    private double previousPrice;
    private final Random rng = new Random();

    public Market(double startingPrice) {
        this.price = startingPrice;
        this.previousPrice = startingPrice;
    }

    public double price() { return price; }
    public double previousPrice() { return previousPrice; }

    public void tick() {
        previousPrice = price;

        price += (rng.nextDouble() - 0.5) * DRIFT_NOISE;
        price = Math.max(MIN_PRICE, price);
    }
}