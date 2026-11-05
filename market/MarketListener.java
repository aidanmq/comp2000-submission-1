package market;

public interface MarketListener {
    default void onTrade(Trader trader, Order order, double priceAfter) {}

    default void onTick(double price) {}
}