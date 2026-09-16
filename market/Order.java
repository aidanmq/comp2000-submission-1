package market;

public final class Order {
    private final TransactionType type;
    private final double amount;

    public Order(TransactionType type, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Order amount must be positive, was " + amount);
        }
        this.type = type;
        this.amount = amount;
    }

    public TransactionType type() { return type; }
    public double amount() { return amount; }
}
