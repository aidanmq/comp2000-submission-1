package market;
 
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public final class Main {
    public static void main(String[] args) {
        if (args.length >= 1 && args[0].equals("--headless")) {
            int ticks = args.length >= 2 ? Integer.parseInt(args[1]) : 2000;
            Market market = buildMarket();
            for (int i = 0; i < ticks; i++) {
                market.tick();
            }
            System.out.println("Headless run OK: " + ticks + " ticks completed, final price $"
                + String.format("%.2f", market.price()));
            return;
        }
 
        SwingUtilities.invokeLater(() -> {
            Market market = buildMarket();
            MarketPanel panel = new MarketPanel(market);
 
            JFrame frame = new JFrame("Fake Stock Market Simulation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
 
            Timer timer = new Timer(300, e -> {
                market.tick();
                panel.repaint();
            });
            timer.start();
        });
    }
 
    private static Market buildMarket() {
        Market market = new Market(100.0, new LinearImpactModel(0.02));
        market.addTrader(new MomentumTrader("Momentum-A", 3000, 2000, 5));
        market.addTrader(new MomentumTrader("Momentum-B", 3000, 2000, 8));
        market.addTrader(new ContrarianTrader("Contrarian-A", 3000, 2000, 5));
        market.addTrader(new ContrarianTrader("Contrarian-B", 3000, 2000, 10));
        for (int i = 1; i <= 6; i++) {
            market.addTrader(new NoiseTrader("Noise-" + i, 1800, 1200, 0.35));
        }
        return market;
    }
}
 
