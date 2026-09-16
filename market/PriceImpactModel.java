package market;
 
public interface PriceImpactModel {
    double priceDelta(Order order, double currentPrice);
}
