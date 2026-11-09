# COMP2000 Worksheet 2 — Final Submission

**Student name:** Aidan Winter

**GitHub repo URL:** (your own fork of your team's repository, not your team's URL)

---

## 1. Design Patterns

Don't forget to submit a pdf file which shows a diagram of your pattern implemenation to supplement this text description.

**1.1.** Which design pattern(s) did you implement? Name them and state where each appears in your codebase.

- Observer: MarketListener is the observer interface, and Market is the subject (it holds a list of listeners and notifies them in tick() and execute()). TransactionFeed and MarketStats are the concrete observers, and they get registered in Main.
- Strategy: Used in two places. PriceImpactModel/LinearImpactModel decides how much an order moves the price, and it's passed into Market through the builder. TrendReaction is a functional interface inside TrendTrader, where a lambda decides if the trader is momentum or contrarian.
- Builder: MarketBuilder, used in Main.buildMarket() and Benchmark.build().
- Static Factory Methods (smaller): TrendTrader.momentum() and TrendTrader.contrarian().

**1.2.** For each pattern, paste a code snippet showing the key interaction between participants.

Observer:
``` java
// Market.java
private void execute(Trader trader, Order order) {
    ...
    price += impactModel.priceDelta(order, price);
    price = Math.max(MIN_PRICE, price);
    listeners.forEach(l -> l.onTrade(trader, order, price));
}

// MarketListener.java
public interface MarketListener {
    default void onTrade(Trader trader, Order order, double priceAfter) {}
    default void onTick(double price) {}
}

// MarketStats.java
@Override
public void onTrade(Trader trader, Order order, double priceAfter) {
    trades.add(new Trade(trader.name(), order.type(), order.amount()));
}
```
Market only knows about the MarketListener interface, so it just loops through its listeners and calls onTrade(). The default methods mean each listener only has to implement the event it actually cares about.

Strategy (PriceImpactModel):
``` java
public interface PriceImpactModel {
    double priceDelta(Order order, double currentPrice);
}

public final class LinearImpactModel implements PriceImpactModel {
    @Override
    public double priceDelta(Order order, double currentPrice) {
        double signedAmount = order.type() == TransactionType.BUY ? order.amount() : -order.amount();
        return signedAmount * sensitivity;
    }
}
```

Strategy (TrendReaction):
``` java
public static TrendTrader momentum(String name, double startingCash, int windowSize) {
    return new TrendTrader(name, startingCash, windowSize,
        trend -> trend > 0 ? TransactionType.BUY : TransactionType.SELL);
}

public static TrendTrader contrarian(String name, double startingCash, int windowSize) {
    return new TrendTrader(name, startingCash, windowSize,
        trend -> trend > 0 ? TransactionType.SELL : TransactionType.BUY);
}

// inside decide():
TransactionType side = reaction.sideFor(trend);
```

Builder:
``` java
return new MarketBuilder()
    .startingPrice(100.0)
    .impactModel(new LinearImpactModel(0.02))
    .addMomentumTrader("Momentum-A", 5000, 5)
    .addContrarianTrader("Contrarian-A", 5000, 5)
    .addNoiseTraders("Noise-", 6, 1800.0, 0.35)
    .build();

public Market build() {
    if (impactModel == null) {
        throw new IllegalStateException("A market needs a price impact model - call impactModel(...) first");
    }
    if (traders.isEmpty()) {
        throw new IllegalStateException("A market needs at least one trader");
    }
    ...
}
```

**1.3.** For each pattern, explain why you chose it over an alternative. What trade-off did you make?

Observer: The alternative was having Market hold direct references to the feed and stats, or having MarketPanel poll the market and recalculate everything itself. Using Observer means Market has no idea the GUI exists, so I could add MarketStats without touching Market, and Benchmark can run a market with no listeners at all. The trade-off is that it's harder to follow what happens after a trade, since the code that runs isn't visible from Market. The callbacks are also synchronous, so a slow listener slows down the whole tick. There's also no removeListener().

Strategy: In the earlier version I had separate MomentumTrader and ContrarianTrader subclasses, but they were basically the same class with one comparison flipped. Swapping to a TrendReaction lambda removed that duplicate decide() code, and the impact model can be swapped in one line in the builder. The trade-off is that lambdas don't have a name or a toString(), so when debugging you can't easily tell if a TrendTrader is momentum or contrarian. A strategy that needed its own state would also have to become a full class anyway.

Builder: Without it, Market would need a constructor with 5+ parameters, or a half-built object filled in with setters. The builder gives sensible defaults (starting price 100, parallel off) and validates everything in one place before the market is made. The trade-off is that it's an extra class, and it isn't actually enforced, since Market.addTrader() and setParallelDecisions() are still public.

---

## 2. Streams / Lambdas / Parallelism

**2.1.** List every file in your project that uses lambda expressions or the Streams API.

- Main: lambdas for SwingUtilities.invokeLater() and the Swing Timer action.
- Market: listeners.forEach() lambdas, and traders.stream()/traders.parallelStream() with map and collect.
- MarketBuilder: method reference in traders.forEach(market::addTrader).
- MarketStats: groupingBy into an EnumMap, summingDouble, sorted, and limit.
- MarketPanel: history.stream().collect(PriceSummary.collector()).
- PriceSummary: custom Collector.of() built from method references.
- TrendTrader: the strategy lambdas, and history.stream().limit(windowSize).collect(PriceSummary.collector()).
- TrendReaction: doesn't use a lambda itself, but it's the @FunctionalInterface the lambdas target.

**2.2.** For the most sophisticated use, paste the code and explain: what does it do, and why was a stream/lambda better than a loop?

``` java
public static Collector<Double, ?, PriceSummary> collector() {
    return Collector.of(Accumulator::new, Accumulator::accept, Accumulator::combine, Accumulator::finish);
}

private static final class Accumulator {
    void accept(double price) {
        if (count == 0) first = price;
        last = price;
        min = Math.min(min, price);
        max = Math.max(max, price);
        sum += price;
        count++;
    }

    Accumulator combine(Accumulator other) {
        if (other.count == 0) return this;
        if (count == 0) return other;
        last = other.last;
        min = Math.min(min, other.min);
        max = Math.max(max, other.max);
        sum += other.sum;
        count += other.count;
        return this;
    }
}

// TrendTrader
double trend = history.stream().limit(windowSize).collect(PriceSummary.collector()).trend();

// MarketPanel
PriceSummary summary = history.stream().collect(PriceSummary.collector());
```
This is a custom collector that goes through a list of prices once and works out the count, first, last, min, max and mean. Since recent() returns the newest price first, trend() is just newest minus oldest. TrendTrader uses it with limit(windowSize) to check the trend over its window, and MarketPanel uses it on the whole history to scale the chart.

A stream was better than a loop because the same collector gets reused in two places, and each caller just adds its own steps (like limit()) in front of it. With a loop I would have had to write a slightly different version each time. It's also safe to run in parallel, because each thread gets its own Accumulator and combine() keeps the order (it keeps this.first and takes other.last). For a list of 120 prices it isn't really faster than a loop, the benefit is reuse and less room for mistakes.

**2.3.** Did you use parallel streams, custom collectors, or the Fork/Join framework? If yes, how did you ensure thread safety? If no, where could parallelism be introduced?

Yes, both. Market.collectDecisions() uses parallelStream() when it's turned on with MarketBuilder.parallelDecisions(true), and PriceSummary is a custom collector.

``` java
private List<Optional<Order>> collectDecisions() {
    Stream<Trader> stream = parallelDecisions ? traders.parallelStream() : traders.stream();
    return stream.map(trader -> trader.decide(this)).collect(Collectors.toList());
}
```

How I made it thread safe:
- The tick is split into two phases. In the parallel phase, traders only read shared state (the price history and their own cash). All the writing (applyBuy/applySell, changing the price, notifying listeners) happens afterwards in a normal sequential loop.
- Every trader has its own Random, so threads aren't fighting over one shared generator.
- priceHistory().recent() hands each trader its own copy of the list.
- Collectors.toList() keeps the original order even in parallel, so decisions.get(i) still matches traders.get(i).
- Since every trader decides on the same snapshot, the parallel version behaves exactly the same as the sequential one, just faster.

I also wrote Benchmark to check if it was worth it. On 2 cores, parallel was slower with 10 traders (0.22x) and 100 traders (0.56x), and about 1.4x faster from 1,000 traders up. That's why Main (10 traders) leaves it off.

One thing to note is that "decide() doesn't change anything shared" is just a rule I followed, nothing actually enforces it. If a future Trader subclass wrote to shared state, it would cause a data race.

---

## 3. Uniqueness and Creativity

**3.1.** What did you add to your simulation that went beyond the in-class activities? Describe the feature and what inspired it.

- Three types of trader (momentum, contrarian and noise), with each trend trader looking back over its own window of 5, 8 or 10 ticks. This was inspired by day trading, where some people follow the trend, some bet against it, and some just trade randomly.
- A swappable price impact model, so the price is driven by orders and not just a random walk.
- A custom stream collector (PriceSummary) shared by the traders and the chart.
- Optional parallel trader decisions, with a Benchmark class that measures when parallelism actually pays off.
- A live Swing dashboard: a red/green price chart, a fading feed of recent trades, a buy vs sell volume bar, and a most active traders leaderboard. It's all fed through Observer, from the generic EventLog<T>.

**3.2.** Paste a code snippet or describe a design decision that demonstrates emergent behaviour, non-trivial complexity, or a genuinely surprising outcome.

``` java
// Trader.java
void applyBuy(double amount)  { cash -= amount; holdingsValue += amount; }
void applySell(double amount) { holdingsValue -= amount; cash += amount; }

// Market.java
if (trader.holdingsValue() < order.amount()) return;
price += impactModel.priceDelta(order, price);
```
Nothing in the code says the price should only go up, but it can never really drop below where it started. I ran 5 simulations of 20,000 ticks each and the lowest price in any of them was $100.18 (starting at $100). Every run also jumped to about $300 in the first 100 ticks.

This comes from a few simple rules working together. Traders start with no holdings, you can only sell what you hold, holdings are tracked in dollars, and a dollar bought moves the price up by the same amount a dollar sold moves it down. Because of this the price basically works out to:

price ≈ 100 + 0.02 × (total dollars held by all traders) + small random drift

I checked this, at tick 500 the price was $343.54 and the formula gave $343.47. Holdings can't go below zero, so the price can't go below about $100, and since there's only $30,800 of cash in total, it can't go above about $716.

The opening rally comes from the same rules. At the start nobody owns anything, so in a downtrend momentum traders want to sell but can't, while contrarians buy. In an uptrend momentum traders buy, and contrarians want to sell but can't. Either way only buys get through. Later on the holdings swap between groups, and by tick 5,000 the momentum traders had sold back to about $4,500 cash each, while the contrarians were nearly all in.

A side effect is that holdings never get revalued at the current price, so no trader's total wealth ever changes. Nobody can actually make or lose money.

**3.3.** If you had another 6 weeks, what would you add or change?

- Track shares instead of dollars and value them at the current price. That would give real profit and loss, a leaderboard by profit, and break the "price = total holdings" lock from 3.2.
- Allow short selling, or give traders some starting shares, to get rid of the upward bias.
- Actually use the exception classes again. In the earlier version execute() threw InsufficientFundsException/InsufficientHoldingsException and the tick loop caught them, but after refactoring, execute() just returns silently, so TradingException and its subclasses (and MarketIntegrityException) are now defined but never thrown. I'd either bring that back and log rejected orders, or delete them.
- Add a non-linear impact model (percentage based or square root). LinearImpactModel currently ignores currentPrice, and because of the Strategy pattern this would only be one new class.
- Smaller things: work out each window summary once per tick instead of once per trader, seed the Randoms so runs are reproducible, put the trader's name in Transaction so the feed shows who traded, and make Market.addTrader() package-private so the builder can't be skipped.
- A bigger change would be replacing the price impact model with a real order book that matches buy and sell orders.

---

## 4. Log Book and Final Reflection

**4.1.** Attach or link your log book entries for Weeks 7–13.

1/11: Begun reviewing course materials
3/11: Researched day-trading softwares to see missing content
4/11: Added better visualisation of stock timeline
5/11: Implemented better patterns & lambda, following criteria
6/11: Observer & build pattern added
7/11: Changed manual indexing to streams throughout software
8/11: Cleaned workspace & made traders run in parallel
9/11: Completed worksheet

**4.2.** Reflect on the semester:

- What was the most important concept you learned?

Programming to an interface instead of a concrete class. The market in submission 1 did most work itself. Now it only depends on MarketListener and PriceImpactModel, so it doesn’t know if it’s talking to the GUI feed, the stats panel, or nothing at all (like in Benchmark). Once I understood this, adding new features got a lot easier. MarketStats was a new class that implemented MarketListener, and I didn’t have to change Market at all. 

- What design trade-off did you face, and how did you resolve it?

Whether to make trader decisions run in parallel. Parallel streams could speed up big markets, but they add complexity and risk data races, since every trader reads the same market. I solved the safety problem by splitting tick() into two phases. Traders only read during the parallel decision phase, and all the writing happens after in a normal sequential loop. For the speed side, I wrote Benchmark to actually measure it instead of guessing. Parallel was slower under about 1,000 traders and ~1.5x faster above that. So I made it an option in MarketBuilder (parallelDecisions(true)) that’s off by default, since Main only runs 10 traders.

- If you started over, what would you do differently?

- Track holdings as shares instead of dollars from the start. Because holdings never get revalued, nobody can actually make or lose money, and the price would get locked (it would never go below $100). I only noticed this after the rest of the code was built around it.
- Decide how to handle invalid orders early and stick with it. I went from throwing exceptions to silently returning, which left four exception classes that are never used.
- Write unit tests as I go. There are no tests in the project, so I checked things by running the GUI and watching. Tests for things like PriceSummary and execute() would have caught problems sooner.
- Seed the Random objects so I could replay the same run when something looked wrong.

- What feedback from the Week 7 submission did you act on?

- Put more effort into the construction of the diagram, trying to remove any inconsistencies. Also, tried to do more commits, as both areas seemed to be silly things I lost marks on.