package market;

import java.util.Arrays;

public final class Benchmark {
    private static final int[] SIZES = {10, 100, 1_000, 10_000, 50_000};
    private static final int ROUNDS = 7;

    public static void main(String[] args) {
        System.out.println("CPU cores available: " + Runtime.getRuntime().availableProcessors());
        System.out.println();
        System.out.printf("%-10s %-8s %14s %14s %10s   %s%n",
            "traders", "ticks", "sequential", "parallel", "speed-up", "faster");
        System.out.printf("%-10s %-8s %14s %14s %10s%n", "", "", "(ms / tick)", "(ms / tick)", "");

        for (int size : SIZES) {
            int ticks = Math.max(20, 200_000 / size);
            double[] sequential = new double[ROUNDS];
            double[] parallel = new double[ROUNDS];
            for (int round = 0; round < ROUNDS; round++) {
                if (round % 2 == 0) {
                    sequential[round] = msPerTick(size, false, ticks);
                    parallel[round] = msPerTick(size, true, ticks);
                } else {
                    parallel[round] = msPerTick(size, true, ticks);
                    sequential[round] = msPerTick(size, false, ticks);
                }
            }
            double seq = median(sequential);
            double par = median(parallel);
            System.out.printf("%-10d %-8d %14.4f %14.4f %9.2fx   %s%n",
                size, ticks, seq, par, seq / par, par < seq ? "parallel" : "sequential");
        }
        System.out.println();
        System.out.println("speed-up above 1.00x means parallel was faster; below 1.00x means the threading overhead cost more than it saved.");
    }

    private static double msPerTick(int traders, boolean parallel, int ticks) {
        Market market = build(traders, parallel);
        int warmUp = Math.max(30, ticks / 4);
        for (int i = 0; i < warmUp; i++) market.tick();

        long start = System.nanoTime();
        for (int i = 0; i < ticks; i++) market.tick();
        long elapsed = System.nanoTime() - start;
        return elapsed / 1_000_000.0 / ticks;
    }

    private static Market build(int traders, boolean parallel) {
        MarketBuilder builder = new MarketBuilder()
            .impactModel(new LinearImpactModel(0.0001))
            .parallelDecisions(parallel);
        int each = traders / 4;
        for (int i = 0; i < each; i++) {
            builder.addMomentumTrader("Momentum-" + i, 1e12, 5 + i % 6);
            builder.addContrarianTrader("Contrarian-" + i, 1e12, 5 + i % 6);
        }
        builder.addNoiseTraders("Noise-", traders - 2 * each, 1e12, 0.35);
        return builder.build();
    }

    private static double median(double[] values) {
        double[] sorted = values.clone();
        Arrays.sort(sorted);
        return sorted[sorted.length / 2];
    }
}