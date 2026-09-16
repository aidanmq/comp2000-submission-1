package market;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

public final class MarketPanel extends JPanel {
    private final Market market;
 
    private static final Color GREEN = new Color(60, 200, 100);
    private static final Color RED = new Color(220, 70, 70);
    private static final Color NEUTRAL = new Color(190, 190, 195);
    private static final Color DIM = new Color(140, 140, 150);
 
    public MarketPanel(Market market) {
        this.market = market;
        setPreferredSize(new Dimension(480, 400));
        setBackground(new Color(22, 24, 28));
    }
 
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 
        paintHeader(g2);
        paintPrice(g2);
        paintTransactionLog(g2);
    }
 
    private void paintHeader(Graphics2D g2) {
        g2.setColor(DIM);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g2.drawString(Market.TICKER + " (fake stock)", 24, 34);
    }
 
    private void paintPrice(Graphics2D g2) {
        double price = market.price();
        double prev = market.previousPrice();
 
        Color color;
        if (price > prev) color = GREEN;
        else if (price < prev) color = RED;
        else color = NEUTRAL;
 
        g2.setColor(color);
        g2.setFont(new Font("SansSerif", Font.BOLD, 68));
        g2.drawString(String.format("$%.2f", price), 24, 115);
 
        double change = price - prev;
        double pct = prev == 0 ? 0 : (change / prev) * 100;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g2.drawString(String.format("%+.2f (%+.2f%%) this tick", change, pct), 24, 142);
    }
 
    private void paintTransactionLog(Graphics2D g2) {
        g2.setColor(DIM);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g2.drawString("Recent activity", 24, 190);
 
        g2.setFont(new Font("Monospaced", Font.PLAIN, 16));
        List<Transaction> recent = market.transactionLog().recent();
        int y = 218;
        for (Transaction t : recent) {
            g2.setColor(t.type() == TransactionType.BUY ? GREEN : RED);
            g2.drawString(t.toString(), 24, y);
            y += 24;
        }
    }
}
