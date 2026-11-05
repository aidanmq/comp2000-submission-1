package market;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public final class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Market market = buildMarket();
            TransactionFeed feed = new TransactionFeed(10);
            market.addListener(feed);
 
            MarketPanel panel = new MarketPanel(market, feed);
 
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
        return new MarketBuilder()
            .startingPrice(100.0)
            .impactModel(new LinearImpactModel(0.02))
            .addMomentumTrader("Momentum-A", 5000, 5)
            .addMomentumTrader("Momentum-B", 5000, 8)
            .addContrarianTrader("Contrarian-A", 5000, 5)
            .addContrarianTrader("Contrarian-B", 5000, 10)
            .addNoiseTraders("Noise-", 6, 1800.0, 0.35)
            .build();
    }
}