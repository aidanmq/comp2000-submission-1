package market;

@FunctionalInterface
public interface TrendReaction {
    TransactionType sideFor(double trend);
}