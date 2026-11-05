package market;

import java.util.List;

public final class TransactionFeed implements MarketListener {
    private final EventLog<Transaction> log;

    public TransactionFeed(int capacity) {
        this.log = new EventLog<>(capacity);
    }

    @Override
    public void onTrade(Trader trader, Order order, double priceAfter) {
        log.add(new Transaction(order.type(), order.amount()));
    }

    public List<Transaction> recent() {
        return log.recent();
    }
}