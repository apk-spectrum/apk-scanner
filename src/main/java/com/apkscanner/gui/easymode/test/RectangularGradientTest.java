package com.apkscanner.gui.easymode.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


public class RectangularGradientTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private static void createAndShowGUI() {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(new RectangularGradientTestPanel());
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}


class RectangularGradientTestPanel extends JPanel {
    private static final long serialVersionUID = 2726591667713468669L;

    @Override
    protected void paintComponent(Graphics gr) {
        super.paintComponent(gr);
        Graphics2D g = (Graphics2D) gr;
        Rectangle2D r = new Rectangle2D.Double(100, 110, 200, 100);
        draw(g, r, 10);

        Shape rr = new Rectangle2D.Double(95, 95, 210, 115);
        g.setColor(Color.BLACK);
        g.fill(rr);
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        return new Dimension(400, 300);
    }


    private static void draw(Graphics2D g, Rectangle2D r, double s) {
        Color c0 = new Color(100, 100, 100);
        Color c1 = new Color(100, 100, 100, 0);

        double x0 = r.getMinX();
        double y0 = r.getMinY();
        double x1 = r.getMaxX();
        double y1 = r.getMaxY();
        double w = r.getWidth();
        double h = r.getHeight();

        // Left
        g.setPaint(new GradientPaint(new Point2D.Double(x0, y0), c0, new Point2D.Double(x0 - s, y0),
                c1));
        g.fill(new Rectangle2D.Double(x0 - s, y0, s, h));

        // Right
        g.setPaint(new GradientPaint(new Point2D.Double(x1, y0), c0, new Point2D.Double(x1 + s, y0),
                c1));
        g.fill(new Rectangle2D.Double(x1, y0, s, h));

        // Top
        g.setPaint(new GradientPaint(new Point2D.Double(x0, y0), c0, new Point2D.Double(x0, y0 - s),
                c1));
        g.fill(new Rectangle2D.Double(x0, y0 - s, w, s));

        // Bottom
        g.setPaint(new GradientPaint(new Point2D.Double(x0, y1), c0, new Point2D.Double(x0, y1 + s),
                c1));
        g.fill(new Rectangle2D.Double(x0, y1, w, s));

        float fractions[] = new float[] {0.0f, 1.0f};
        Color colors[] = new Color[] {c0, c1};

        // Top Left
        g.setPaint(new RadialGradientPaint(new Rectangle2D.Double(x0 - s, y0 - s, s + s, s + s),
                fractions, colors, CycleMethod.NO_CYCLE));
        g.fill(new Rectangle2D.Double(x0 - s, y0 - s, s, s));

        // Top Right
        g.setPaint(new RadialGradientPaint(new Rectangle2D.Double(x1 - s, y0 - s, s + s, s + s),
                fractions, colors, CycleMethod.NO_CYCLE));
        g.fill(new Rectangle2D.Double(x1, y0 - s, s, s));

        // Bottom Left
        g.setPaint(new RadialGradientPaint(new Rectangle2D.Double(x0 - s, y1 - s, s + s, s + s),
                fractions, colors, CycleMethod.NO_CYCLE));
        g.fill(new Rectangle2D.Double(x0 - s, y1, s, s));

        // Bottom Right
        g.setPaint(new RadialGradientPaint(new Rectangle2D.Double(x1 - s, y1 - s, s + s, s + s),
                fractions, colors, CycleMethod.NO_CYCLE));
        g.fill(new Rectangle2D.Double(x1, y1, s, s));
    }


}
