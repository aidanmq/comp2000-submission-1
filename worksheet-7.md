# COMP2000 Worksheet 1 — Mid-Semester Submission

**Student name: Aidan Winter**

**Student ID: 47596686**

**GitHub repo URL:** (your own fork of your team's repository, not your team's URL)

---

## 1. Version Control

**1.1.** Paste the first 10 lines of the output of `git log --graph --oneline --all` from your repository:

```
* 611614b (HEAD -> main, origin/main, origin/HEAD) Exceptions, Bug Fixes, and Realism
* b589c4b More Classes and Visual Changes
* f1e8393 Created Initial Pages
* 58bf5fa Initial commit

```

**1.2.** Describe your workflow. Did you use branches? Pull requests?

As I am working alone, there was no need for the use of branches (and therefore pull requests) during development. I began by researching how stock markets work (largely inspired by day trading, not that I've done it before), as they seemed like a volatile environment that could be simulated.
After that, I begun by creating basic constructors, and slowly expanded the simulations functions after confirming the functionality all worked.

**1.3.** Estimate the percentage of commits you contributed relative to the total in your repository.
100%

---

## 2. Program Design

Don't forget to submit a pdf file of your program design along with this file.

**2.1.** List every class in your project and write 1–2 sentences describing its responsibility.
- Main: Builds, populates, and loops the market.
- Market: Holds price state, history, trader list, and the price impact model. The price impact model runs each tick, collects trader decisions, and executes valid order.
- MarketPanel: JPanel (Swing Package) that renders stocks price, price changes, and recent transactions for the user to watch.

- Trader (Abstract): Base class for all traders, so they all have the same shared state, and defines logic for traders.
- NoiseTrader: Trades randomly with some probability, independent of price trend.
- MomentumTrader: Buys when price trend is rising and sells when falling.
- ContrarianTrader: Buys when price trend is falling and sells when rising.

- Order: Represents a proposed trade (buy/sell + amount), and validates if the trade is positive.
- Transaction: Record of validated orders.
- TransactionType: Enum of BUY/SELL.

- PriceImpactModel: Defines how validated orders affects stock price.
- LinearImpactModel: Implements PriceImpactModel to be a simple linear supply/demand price impact.
- EventLog<T>: Fixed Capacity log used for price and transaction history.

- TradingException (Abstract): Base checked-exception for trading failures.
- InsufficientFundsException: Throw when traders buy more than they have.
- InsufficientHoldingsException: Throw when trader sells more than their holdings.
- MarketIntegrityException: Throw when price model produces invalid price.

**2.2.** Identify any inheritance relationships. For each parent–child pair, list what the child inherits and what it overrides.
Trader (MomentumTrader, ContrarianTrader, NoiseTrader):
    - Inherits: name, cash, and holdingsValue fields, as well as package-private bookkeeping methods.
    - Overrides: decide(Market market) - each subclass has its own decision making logic.

TradingException (InsufficientFundsException, InsufficientHoldingsException)
    - Inherits: checked-exception structure and constructor-based message handling.
    - Override: Each subclass has its own message format in its constructor.

RuntimeException (MarketIntegrityException)
    - Inherits: unchecked exception behavior

**2.3.** Pick the class that you think has the best design. Explain why.
EventLog<T> is used identically for both price history and transaction history without any duplicate code. Its fixed size ensures
old entries silently fall off, and the API (add, recent, size) returns newest-first copies so the internal-state can't be messed with.
It is also a genuine abstraction as market reuses it for two different jobs.


**2.4.** Paste one code snippet that demonstrates your use of polymorphism or encapsulation.  Include an explanation of _how_ this demonstrates polymorphim or encapsulation.  Give a reference to a provided reading that talks about this type of polymorphism or encapsulation.

``` java
private boolean runTraderDecisions() {
    boolean anyExecuted = false;
    for (Trader trader : traders) {
        Optional<Order> orderOpt = trader.decide(this);
        if (orderOpt.isEmpty()) continue;

        try {
            execute(trader, orderOpt.get());
            anyExecuted = true;
        } catch (InsufficientFundsException | InsufficientHoldingsException e) {
            continue;
        }
    }
    return anyExecuted;
}
```
Polymorphism: traders is declared as List<Trader>, but holds a mix of Momentum, Contrarian, and Noise Trader at runtime.
The decide function is a single call, but the actual method thats called depends on the runtime type of each object. This dynamic polymorphism removes the need for if/else chains that check the type.

Encapsulation: Market never touches a traders cash or holdings directly, as they are protected fields. Instead, the public accessor methods and execute() validate the funds/holdings before mutating states and throwing exceptions for invalid operations.

Also, I didnt do any of the readings.
---

## 3. Generics and Exceptions

**3.1.** List every place your code uses generics (e.g. `ArrayList<Actor>`, `Optional<Cell>`, `HashMap<String, Team>`). If you deliberately used none, explain why.
- EventLog<T>: defined as a generic class, its instantiated as either EventLog<double> or EventLong<Transaction> in market.
- Deque<T>/ArrayDeque<>: internal backing structure inside of EventLog.
- List<T>/ArrayList<>: used as a return type in EventLog, and throughout Market, MomentumTrader, ContrarianTrader, and MarketPanel.
- Optional<Order>: return type of .decide(), used by every trader subclass and in Market.

**3.2.** List every place your code handles exceptions (try/catch, throws, custom exception classes). What error is each protecting against?

- Order constructor — throws unchecked IllegalArgumentException if amount <= 0, guarding against invalid orders.
- Market.execute() — declares throws InsufficientFundsException, InsufficientHoldingsException. Throws when either a traders cash or holdings cant cover the requested order amount, protecting against overdrawing/overselling.
- Market.runTraderDecisions() — catches InsufficientFundsException | InsufficientHoldingsException around execute(), so one trader's invalid order doesn't crash the tick loop; it just skips that trader.
- Market.forceFallbackPurchase() — same catch pattern, used when forcing a fallback trade to guarantee market activity.
- Market.tick() — throws unchecked MarketIntegrityException if price drops to $0 or below, ensuring price stays positive.
- InsufficientFundsException/InsufficientHoldingsException — custom checked exceptions that extend TradingException, each formatting their own error message.

**3.3.** Paste a code snippet showing either a generic class/method or a try/catch block.

``` java
public final class EventLog<T> {
    private final int capacity;
    private final Deque<T> items = new ArrayDeque<>();

    public EventLog(int capacity) {
        this.capacity = capacity;
    }

    public void add(T item) {
        items.addLast(item);
        while (items.size() > capacity) {
            items.removeFirst();
        }
    }

    public List<T> recent() {
        List<T> list = new ArrayList<>(items);
        Collections.reverse(list);
        return list;
    }

    public int size() { return items.size(); }
}
```
This is a generic class that lets market reuse the same bounded history logic for two unrelated typess, with safety at compile-time.

---

## 4. Log Book

Don't forget to submit an electronic version of your logbook.

**4.1.** Which week's activity taught you the most? What did you learn?
Week 4 Inheritance and Overloading taught how to extend and implement, functionality that is used throughout this project.
---

## 5. Uniqueness and Creativity

**5.1.** List everything you added to the project that was not part of the in-class activities.
- Day Trading
- Swing JPanel (despite recommendations against it)
- Double-ended Queue (ArrayDeque)

**5.2.** Which feature required the most independent research or problem-solving? What did you learn from it?
I've never worked with double-ended queues before, and while it could've been done without it, in pursuit of cleanliness and efficiency, learning how to use it provided a cleaner solution.

**5.3.** Paste one code snippet that you are especially proud of. Explain why it goes beyond what was done in class.
``` java
public final class EventLog<T> {
    private final int capacity;
    private final Deque<T> items = new ArrayDeque<>();

    public EventLog(int capacity) {
        this.capacity = capacity;
    }

    public void add(T item) {
        items.addLast(item);
        while (items.size() > capacity) {
            items.removeFirst();
        }
    }

    public List<T> recent() {
        List<T> list = new ArrayList<>(items);
        Collections.reverse(list);
        return list;
    }

    public int size() { return items.size(); }
}
```
Despite being the code snippet from above, I am proud of this due to the efficiency of the code, as it provides solutions for two unrelated types.