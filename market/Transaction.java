package market;

public final class Transaction {
    private final TransactionType type;
    private final double amount; 

    public Transaction(TransactionType type, double amount) {
        this.type = type;
        this.amount = amount;
    }

    public TransactionType type() { return type; }
    public double amount() { return amount; }

    @Override
    public String toString() {
        return String.format("%s - $%.2f", type, amount);
    }
}
