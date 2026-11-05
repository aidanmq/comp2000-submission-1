package market;

import java.util.ArrayList;
import java.util.List;

public final class MarketBuilder {
    private double startingPrice = 100.0;
    private PriceImpactModel impactModel;
    private final List<Trader> traders = new ArrayList<>();

    public MarketBuilder startingPrice(double price) {
        if (price <= 0) throw new IllegalArgumentException("Starting price must be positive, was " + price);
        this.startingPrice = price;
        return this;
    }

    public MarketBuilder impactModel(PriceImpactModel model) {
        this.impactModel = model;
        return this;
    }

    public MarketBuilder addMomentumTrader(String name, double startingCash, int windowSize) {
        traders.add(TrendTrader.momentum(name, startingCash, windowSize));
        return this;
    }

    public MarketBuilder addContrarianTrader(String name, double startingCash, int windowSize) {
        traders.add(TrendTrader.contrarian(name, startingCash, windowSize));
        return this;
    }

    public MarketBuilder addNoiseTraders(String namePrefix, int count, double startingCash, double activityChance) {
        for (int i = 1; i <= count; i++) {
            traders.add(new NoiseTrader(namePrefix + i, startingCash, activityChance));
        }
        return this;
    }

    public Market build() {
        if (impactModel == null) {
            throw new IllegalStateException("A market needs a price impact model - call impactModel(...) first");
        }
        if (traders.isEmpty()) {
            throw new IllegalStateException("A market needs at least one trader");
        }
        Market market = new Market(startingPrice, impactModel);
        traders.forEach(market::addTrader);
        return market;
    }
}