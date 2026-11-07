package market;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Map;

public final class MarketPanel extends JPanel {
    private final Market market;
    private final TransactionFeed feed;
    private final MarketStats stats;

    private static final Color GREEN = new Color(60, 200, 100);
    private static final Color RED = new Color(220, 70, 70);
    private static final Color NEUTRAL = new Color(190, 190, 195);
    private static final Color DIM = new Color(140, 140, 150);
    private static final Color GRID = new Color(255, 255, 255, 22);
    private static final Color CHART_BG = new Color(28, 30, 35);

    private static final int[] FEED_ALPHAS = {255, 191, 128, 64};

    private static final int MARGIN = 24;
    private static final int CHART_TOP = 170;
    private static final int AXIS_LABEL_WIDTH = 62;

    public MarketPanel(Market market, TransactionFeed feed, MarketStats stats) {
        this.market = market;
        this.feed = feed;
        this.stats = stats;
        setPreferredSize(new Dimension(900, 640));
        setBackground(new Color(22, 24, 28));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int chartX = MARGIN;
        int chartY = CHART_TOP;
        int chartW = getWidth() - MARGIN * 2;
        int chartH = getHeight() - CHART_TOP - MARGIN;

        paintHeader(g2);
        paintPrice(g2);
        paintStats(g2);
        paintChart(g2, chartX, chartY, chartW, chartH);
        paintTransactionFeed(g2, chartX + 14, chartY + 14);
    }

    private void paintHeader(Graphics2D g2) {
        g2.setColor(DIM);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g2.drawString(Market.TICKER + " (fake stock)", MARGIN, 34);
    }

    private void paintPrice(Graphics2D g2) {
        double price = market.price();
        double prev = market.previousPrice();

        Color color = directionColor(prev, price);

        g2.setColor(color);
        g2.setFont(new Font("SansSerif", Font.BOLD, 68));
        g2.drawString(String.format("$%.2f", price), MARGIN, 115);

        double change = price - prev;
        double pct = prev == 0 ? 0 : (change / prev) * 100;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g2.drawString(String.format("%+.2f (%+.2f%%) this tick", change, pct), MARGIN, 142);
    }

    private void paintStats(Graphics2D g2) {
        int width = 280;
        int x = Math.max(getWidth() - MARGIN - width, 360);

        g2.setColor(DIM);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.drawString("Buy vs sell volume (last " + stats.capacity() + " trades)", x, 30);

        int barY = 40;
        int barH = 12;
        Shape previousClip = g2.getClip();
        g2.clip(new RoundRectangle2D.Double(x, barY, width, barH, 6, 6));
        g2.setColor(new Color(255, 255, 255, 28));
        g2.fillRect(x, barY, width, barH);
        if (stats.tradeCount() > 0) {
            int buyWidth = (int) Math.round(stats.buyShare() * width);
            g2.setColor(GREEN);
            g2.fillRect(x, barY, buyWidth, barH);
            g2.setColor(RED);
            g2.fillRect(x + buyWidth, barY, width - buyWidth, barH);
        }
        g2.setClip(previousClip);

        FontMetrics fm = g2.getFontMetrics();
        if (stats.tradeCount() == 0) {
            g2.setColor(DIM);
            g2.drawString("Waiting for trades...", x, barY + barH + 18);
            return;
        }
        int buyPct = (int) Math.round(stats.buyShare() * 100);
        String sellLabel = "Sell " + (100 - buyPct) + "%";
        g2.setColor(GREEN);
        g2.drawString("Buy " + buyPct + "%", x, barY + barH + 18);
        g2.setColor(RED);
        g2.drawString(sellLabel, x + width - fm.stringWidth(sellLabel), barY + barH + 18);

        g2.setColor(DIM);
        g2.drawString("Most active traders", x, 98);
        List<Map.Entry<String, Double>> top = stats.topTradersByVolume(3);
        int rowY = 116;
        for (Map.Entry<String, Double> entry : top) {
            String amount = String.format("$%,.0f", entry.getValue());
            g2.setColor(NEUTRAL);
            g2.drawString(entry.getKey(), x, rowY);
            g2.drawString(amount, x + width - fm.stringWidth(amount), rowY);
            rowY += 17;
        }
    }

    private void paintChart(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(CHART_BG);
        g2.fillRoundRect(x, y, w, h, 12, 12);

        List<Double> history = market.priceHistory().recent();
        if (history.size() < 2) return;

        int plotLeft = x + 10;
        int plotRight = x + w - AXIS_LABEL_WIDTH;
        int plotTop = y + 12;
        int plotBottom = y + h - 12;
        int plotH = plotBottom - plotTop;

        PriceSummary summary = history.stream().collect(PriceSummary.collector());
        double min = summary.min();
        double max = summary.max();
        double range = Math.max(max - min, 1.0);
        double mid = (max + min) / 2.0;
        double lo = mid - range * 0.62;
        double hi = mid + range * 0.95;

        paintGrid(g2, plotLeft, plotRight, plotTop, plotBottom, lo, hi);

        double step = (double) (plotRight - plotLeft) / (Market.PRICE_HISTORY_CAPACITY - 1);

        g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        double lastX = 0;
        double lastY = 0;
        for (int i = 0; i < history.size() - 1; i++) {
            double newer = history.get(i);
            double older = history.get(i + 1);

            double x1 = plotRight - i * step;
            double x2 = plotRight - (i + 1) * step;
            double y1 = plotBottom - (newer - lo) / (hi - lo) * plotH;
            double y2 = plotBottom - (older - lo) / (hi - lo) * plotH;

            g2.setColor(directionColor(older, newer));
            g2.draw(new Line2D.Double(x2, y2, x1, y1));

            if (i == 0) {
                lastX = x1;
                lastY = y1;
            }
        }

        double latest = history.get(0);
        double beforeLatest = history.get(1);
        g2.setColor(directionColor(beforeLatest, latest));
        g2.fill(new Ellipse2D.Double(lastX - 4.5, lastY - 4.5, 9, 9));
    }

    private void paintGrid(Graphics2D g2, int left, int right, int top, int bottom, double lo, double hi) {
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        FontMetrics fm = g2.getFontMetrics();
        int lines = 5;
        for (int i = 0; i < lines; i++) {
            double frac = i / (double) (lines - 1);
            int gy = (int) Math.round(bottom - frac * (bottom - top));
            double value = lo + frac * (hi - lo);

            g2.setColor(GRID);
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(left, gy, right, gy);

            g2.setColor(DIM);
            g2.drawString(String.format("$%.2f", value), right + 8, gy + fm.getAscent() / 2 - 1);
        }
    }

    private void paintTransactionFeed(Graphics2D g2, int x, int y) {
        g2.setColor(DIM);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.drawString("Recent activity", x, y + 8);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 13));
        List<Transaction> recent = feed.recent();
        int rowY = y + 28;
        for (int i = 0; i < FEED_ALPHAS.length && i < recent.size(); i++) {
            Transaction t = recent.get(i);
            Color base = t.type() == TransactionType.BUY ? GREEN : RED;
            g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), FEED_ALPHAS[i]));
            g2.drawString(t.toString(), x, rowY);
            rowY += 18;
        }
    }

    private static Color directionColor(double from, double to) {
        if (to > from) return GREEN;
        if (to < from) return RED;
        return NEUTRAL;
    }
}