package market;

import java.util.Optional;

public abstract class Trader {
    private final String name;
    protected double cash;
    protected double holdingsValue;
 
    protected Trader(String name, double startingCash) {
        this.name = name;
        this.cash = startingCash;
        this.holdingsValue = 0;
    }
 
    public String name() { return name; }
    public double cash() { return cash; }
    public double holdingsValue() { return holdingsValue; }
 
    public abstract Optional<Order> decide(Market market);
 
    void applyBuy(double amount) {
        cash -= amount;
        holdingsValue += amount;
    }
 
    void applySell(double amount) {
        holdingsValue -= amount;
        cash += amount;
    }
}
