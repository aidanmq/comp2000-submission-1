package market;

public class InsufficientFundsException extends TradingException {
    public InsufficientFundsException(String traderName, double requested, double available) {
        super(traderName + " tried to spend $" + String.format("%.2f", requested)
            + " but only has $" + String.format("%.2f", available));
    }
}