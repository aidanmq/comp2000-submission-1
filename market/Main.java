package market;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public final class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Market market = new Market(100.0, new LinearImpactModel(0.02));
            market.addTrader(new MomentumTrader("Momentum-A", 5000, 5));
            market.addTrader(new MomentumTrader("Momentum-B", 5000, 8));
            market.addTrader(new ContrarianTrader("Contrarian-A", 5000, 5));
            market.addTrader(new ContrarianTrader("Contrarian-B", 5000, 10));
            for (int i = 1; i <= 6; i++) {
                market.addTrader(new NoiseTrader("Noise-" + i, 1800.0 , 0.35));
            }
 
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
}


