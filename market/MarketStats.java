package market;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class MarketStats implements MarketListener {

    private static final class Trade {
        final String traderName;
        final TransactionType type;
        final double amount;

        Trade(String traderName, TransactionType type, double amount) {
            this.traderName = traderName;
            this.type = type;
            this.amount = amount;
        }
    }

    private final int capacity;
    private final EventLog<Trade> trades;

    public MarketStats(int capacity) {
        this.capacity = capacity;
        this.trades = new EventLog<>(capacity);
    }

    @Override
    public void onTrade(Trader trader, Order order, double priceAfter) {
        trades.add(new Trade(trader.name(), order.type(), order.amount()));
    }

    public int capacity() { return capacity; }

    public int tradeCount() { return trades.size(); }

    public Map<TransactionType, Double> volumeByType() {
        Map<TransactionType, Double> volume = trades.recent().stream().collect(Collectors.groupingBy(
            t -> t.type,
            () -> new EnumMap<>(TransactionType.class),
            Collectors.summingDouble(t -> t.amount)));
        for (TransactionType type : TransactionType.values()) {
            volume.putIfAbsent(type, 0.0);
        }
        return volume;
    }

    public double buyShare() {
        Map<TransactionType, Double> volume = volumeByType();
        double buys = volume.get(TransactionType.BUY);
        double total = buys + volume.get(TransactionType.SELL);
        return total == 0 ? 0.5 : buys / total;
    }

    public List<Map.Entry<String, Double>> topTradersByVolume(int n) {
        return trades.recent().stream()
            .collect(Collectors.groupingBy(t -> t.traderName, Collectors.summingDouble(t -> t.amount)))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(n)
            .collect(Collectors.toList());
    }
}