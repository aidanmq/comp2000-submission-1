package market;

public final class LinearImpactModel implements PriceImpactModel {
    private final double sensitivity;
 
    public LinearImpactModel(double sensitivity) {
        this.sensitivity = sensitivity;
    }
 
    @Override
    public double priceDelta(Order order, double currentPrice) {
        double signedAmount = order.type() == TransactionType.BUY ? order.amount() : -order.amount();
        return signedAmount * sensitivity;
    }
}
