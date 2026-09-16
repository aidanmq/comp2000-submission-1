package market;

public class InsufficientHoldingsException extends TradingException {
    public InsufficientHoldingsException(String traderName, double requested, double available) {
        super(traderName + " tried to sell $" + String.format("%.2f", requested)
            + " worth of stock but only holds $" + String.format("%.2f", available));
    }
}