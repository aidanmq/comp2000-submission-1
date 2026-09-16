package market;

public abstract class TradingException extends Exception {
    protected TradingException(String message) {
        super(message);
    }
}