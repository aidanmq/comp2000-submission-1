package market;

import java.util.stream.Collector;

public final class PriceSummary {
    private final int count;
    private final double newest;
    private final double oldest;
    private final double min;
    private final double max;
    private final double mean;

    private PriceSummary(int count, double newest, double oldest, double min, double max, double mean) {
        this.count = count;
        this.newest = newest;
        this.oldest = oldest;
        this.min = min;
        this.max = max;
        this.mean = mean;
    }

    public int count() { return count; }
    public double newest() { return newest; }
    public double oldest() { return oldest; }
    public double min() { return min; }
    public double max() { return max; }
    public double mean() { return mean; }

    public double trend() { return newest - oldest; }

    public double range() { return max - min; }

    public static Collector<Double, ?, PriceSummary> collector() {
        return Collector.of(Accumulator::new, Accumulator::accept, Accumulator::combine, Accumulator::finish);
    }

    private static final class Accumulator {
        private int count = 0;
        private double first;
        private double last;
        private double min = Double.POSITIVE_INFINITY;
        private double max = Double.NEGATIVE_INFINITY;
        private double sum = 0;

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

        PriceSummary finish() {
            if (count == 0) {
                return new PriceSummary(0, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
            }
            return new PriceSummary(count, first, last, min, max, sum / count);
        }
    }
}