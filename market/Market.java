package market;
 
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Collections;

public final class Market {
    public static final String TICKER = "FAKE";
 
    private static final double MIN_PRICE = 0.50;
    private static final double DRIFT_NOISE = 0.05;
    private static final double PURCHASE_CHANCE_PER_TICK = 0.3;
    private static final int MAX_TICKS_WITHOUT_PURCHASE = 10;
    private static final double FALLBACK_ORDER_AMOUNT = 15.0;
 
    private double price;
    private double previousPrice;
    private final PriceImpactModel impactModel;
    private final EventLog<Double> priceHistory = new EventLog<>(30);
    private final EventLog<Transaction> transactionLog = new EventLog<>(10);
    private final List<Trader> traders = new ArrayList<>();
    private final Random rng = new Random();
    private int ticksSinceLastPurchase = 0;
 
    public Market(double startingPrice, PriceImpactModel impactModel) {
        this.price = startingPrice;
        this.previousPrice = startingPrice;
        this.impactModel = impactModel;
        priceHistory.add(startingPrice);
    }
 
    public void addTrader(Trader trader) { traders.add(trader); }
 
    public double price() { return price; }
    public double previousPrice() { return previousPrice; }
    public EventLog<Double> priceHistory() { return priceHistory; }
    public EventLog<Transaction> transactionLog() { return transactionLog; }
    public int ticksSinceLastPurchase() { return ticksSinceLastPurchase; }
 
    public void tick() {
        previousPrice = price;
 
        boolean forced = ticksSinceLastPurchase >= MAX_TICKS_WITHOUT_PURCHASE;
        boolean tradingTick = forced || rng.nextDouble() < PURCHASE_CHANCE_PER_TICK;
 
        if (tradingTick) {
            boolean purchased = runTraderDecisions();
            if (!purchased) {
                purchased = forceFallbackPurchase();
            }
            ticksSinceLastPurchase = purchased ? 0 : ticksSinceLastPurchase + 1;
        } else {
            ticksSinceLastPurchase++;
        }

        price += (rng.nextDouble() - 0.5) * DRIFT_NOISE;
        price = Math.max(MIN_PRICE, price);
        priceHistory.add(price);
 
        if (price <= 0) {
            throw new MarketIntegrityException("Price reached $" + price + " - pricing model invariant violated");
        }
    }

     private boolean runTraderDecisions() {
        boolean anyExecuted = false;
        for (Trader trader : traders) {
            Optional<Order> orderOpt = trader.decide(this);
            if (orderOpt.isEmpty()) continue;
 
            try {
                execute(trader, orderOpt.get());
                anyExecuted = true;
            } catch (InsufficientFundsException | InsufficientHoldingsException e) {
                continue;
            }
        }
        return anyExecuted;
    }

    private boolean forceFallbackPurchase() {
        List<Trader> shuffled = new ArrayList<>(traders);
        Collections.shuffle(shuffled, rng);
        for (Trader trader : shuffled) {
            try {
                if (trader.cash() >= FALLBACK_ORDER_AMOUNT) {
                    execute(trader, new Order(TransactionType.BUY, FALLBACK_ORDER_AMOUNT));
                    return true;
                } else if (trader.holdingsValue() >= FALLBACK_ORDER_AMOUNT) {
                    execute(trader, new Order(TransactionType.SELL, FALLBACK_ORDER_AMOUNT));
                    return true;
                }
            } catch (InsufficientFundsException | InsufficientHoldingsException e) {
                continue;
            }
        }
        return false;
    }
 
    private void execute(Trader trader, Order order)
            throws InsufficientFundsException, InsufficientHoldingsException {
        if (order.type() == TransactionType.BUY) {
            if (trader.cash() < order.amount()) {
                throw new InsufficientFundsException(trader.name(), order.amount(), trader.cash());
            }
            trader.applyBuy(order.amount());
        } else {
            if (trader.holdingsValue() < order.amount()) {
                throw new InsufficientHoldingsException(trader.name(), order.amount(), trader.holdingsValue());
            }
            trader.applySell(order.amount());
        }
 
        price += impactModel.priceDelta(order, price);
        price = Math.max(MIN_PRICE, price);
        transactionLog.add(new Transaction(order.type(), order.amount()));
    }
}



