package market;
 
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public final class Market {
    public static final String TICKER = "FAKE";
 
    private static final double MIN_PRICE = 0.50;
    private static final double DRIFT_NOISE = 0.05;

    public static final int PRICE_HISTORY_CAPACITY = 120;

    private double price;
    private double previousPrice;
    private final PriceImpactModel impactModel;
    private final EventLog<Double> priceHistory = new EventLog<>(PRICE_HISTORY_CAPACITY);
    private final List<Trader> traders = new ArrayList<>();
    private final List<MarketListener> listeners = new ArrayList<>();
    private final Random rng = new Random();
 
    public Market(double startingPrice, PriceImpactModel impactModel) {
        this.price = startingPrice;
        this.previousPrice = startingPrice;
        this.impactModel = impactModel;
        priceHistory.add(startingPrice);
    }
 
    public void addTrader(Trader trader) { traders.add(trader); }
    public void addListener(MarketListener listener) { listeners.add(listener); }
 
    public double price() { return price; }
    public double previousPrice() { return previousPrice; }
    public EventLog<Double> priceHistory() { return priceHistory; }
 
    public void tick() {
        previousPrice = price;
 
        for (Trader trader : traders) {
            Optional<Order> orderOpt = trader.decide(this);
            if (orderOpt.isEmpty()) continue;
            execute(trader, orderOpt.get());
        }
 
        price += (rng.nextDouble() - 0.5) * DRIFT_NOISE;
        price = Math.max(MIN_PRICE, price);
        priceHistory.add(price);
        listeners.forEach(l -> l.onTick(price));
    }
 
    private void execute(Trader trader, Order order) {
        if (order.type() == TransactionType.BUY) {
            if (trader.cash() < order.amount()) return;
            trader.applyBuy(order.amount());
        } else {
            if (trader.holdingsValue() < order.amount()) return;
            trader.applySell(order.amount());
        }
 
        price += impactModel.priceDelta(order, price);
        price = Math.max(MIN_PRICE, price);
        listeners.forEach(l -> l.onTrade(trader, order, price));
    }
}