package market;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public final class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Market market = new Market(100.0);
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