package com.apkscanner.gui.easymode.test;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import javax.swing.*;
import javax.swing.event.*;

public class Panel2Test {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                ShadeOptionsPanel shadeOptions = new ShadeOptionsPanel();
                ShadowSelector shadowSelector = new ShadowSelector(shadeOptions);
                //ComponentSource componentSource = new ComponentSource(shadeOptions);
                JFrame f = new JFrame("Rounded Concept Demo with Shadows");
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.add(shadowSelector, "North");
                f.add(shadeOptions);
                //f.add(componentSource, "South");
                f.setSize(300, 200);
                f.setLocation(150, 150);
                f.setVisible(true);
            }
        });
    }

    private Panel2Test() {
    }
}

class ShadeOptionsPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private final int PAD, DIA, BORDER;
    private Color colorIn, colorOut;
    private int xc, yc;
    private Ellipse2D eIn, eOut;
    private GradientPaint gradient;
    private CustomPaint customPaint;
    private Area arcBorder;
    private int width, height;
    private Point2D neOrigin, nwOrigin, swOrigin, seOrigin, neDiag, nwDiag, swDiag, seDiag;
    private final static int NORTHEAST = 0, NORTHWEST = 1, SOUTHWEST = 2, SOUTHEAST = 3;
    public int shadowVertex = 3;

    public ShadeOptionsPanel() {
        PAD = 25;
        DIA = 75;
        BORDER = 10;
        colorIn = Color.black;
        colorOut = getBackground();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        width = getWidth();
        height = getHeight();
        g2.drawRoundRect(PAD, PAD, width - 2 * PAD, height - 2 * PAD, DIA, DIA);
        calculateArcOrigins();
        calculateCardinalDiagonals();
        drawVertexArc(g2, shadowVertex);
        switch (shadowVertex) {
            case NORTHEAST:
                drawNorthSide(g2);
                drawEastSide(g2);
                xc = PAD + DIA / 2; // draw northwest arc
                yc = PAD + DIA / 2;
                customPaint = new CustomPaint(xc, yc, new Point2D.Double(0, DIA / 2),
                        DIA / 2, BORDER, colorIn, colorOut);
                g2.setPaint(customPaint);
                eIn = getInnerEllipse(nwOrigin, nwDiag);
                eOut = getOuterEllipse(nwOrigin, nwDiag);
                arcBorder = getArcArea(eIn, eOut, 90.0);
                g2.fill(arcBorder);
                xc = width - PAD - DIA / 2; // draw southeast arc
                yc = height - PAD - DIA / 2;
                customPaint = new CustomPaint(xc, yc, new Point2D.Double(0, DIA / 2),
                        DIA / 2, BORDER, colorIn, colorOut);
                g2.setPaint(customPaint);
                eIn = getInnerEllipse(seOrigin, seDiag);
                eOut = getOuterEllipse(seOrigin, seDiag);
                arcBorder = getArcArea(eIn, eOut, 270.0);
                g2.fill(arcBorder);
                break;
            case NORTHWEST:
                drawNorthSide(g2);
                drawWestSide(g2);
                xc = width - PAD - DIA / 2;// draw northeast arc
                yc = PAD + DIA / 2;
                customPaint = new CustomPaint(xc, yc, new Point2D.Double(0, DIA / 2),
                        DIA / 2, BORDER, colorIn, colorOut);
                g2.setPaint(customPaint);
                eIn = getInnerEllipse(neOrigin, neDiag);
                eOut = getOuterEllipse(neOrigin, neDiag);
                arcBorder = getArcArea(eIn, eOut, 0.0);
                g2.fill(arcBorder);
                xc = PAD + DIA / 2;// draw southwest arc
                yc = height - PAD - DIA / 2;
                customPaint = new CustomPaint(xc, yc, new Point2D.Double(0, DIA / 2),
                        DIA / 2, BORDER, colorIn, colorOut);
                g2.setPaint(customPaint);
                eIn = getInnerEllipse(swOrigin, swDiag);
                eOut = getOuterEllipse(swOrigin, swDiag);
                arcBorder = getArcArea(eIn, eOut, 180.0);
                g2.fill(arcBorder);
                break;
            case SOUTHWEST:
                drawWestSide(g2);
                drawSouthSide(g2);
                xc = PAD + DIA / 2; // draw northwest arc
                yc = PAD + DIA / 2;
                customPaint = new CustomPaint(xc, yc, new Point2D.Double(0, DIA / 2),
                        DIA / 2, BORDER, colorIn, colorOut);
                g2.setPaint(customPaint);
                eIn = getInnerEllipse(nwOrigin, nwDiag);
                eOut = getOuterEllipse(nwOrigin, nwDiag);
                arcBorder = getArcArea(eIn, eOut, 90.0);
                g2.fill(arcBorder);
                xc = width - PAD - DIA / 2; // draw the southeast arc
                yc = height - PAD - DIA / 2;
                customPaint = new CustomPaint(xc, yc, new Point2D.Double(0, DIA / 2),
                        DIA / 2, BORDER, colorIn, colorOut);
                g2.setPaint(customPaint);
                eIn = getInnerEllipse(seOrigin, seDiag);
                eOut = getOuterEllipse(seOrigin, seDiag);
                arcBorder = getArcArea(eIn, eOut, 270.0);
                g2.fill(arcBorder);
                break;
            case SOUTHEAST:
                drawEastSide(g2);
                drawSouthSide(g2);
                xc = width - PAD - DIA / 2; // draw northeast arc
                yc = PAD + DIA / 2;
                customPaint = new CustomPaint(xc, yc, new Point2D.Double(0, DIA / 2),
                        DIA / 2, BORDER, colorIn, colorOut);
                g2.setPaint(customPaint);
                eIn = getInnerEllipse(neOrigin, neDiag);
                eOut = getOuterEllipse(neOrigin, neDiag);
                arcBorder = getArcArea(eIn, eOut, 0.0);
                g2.fill(arcBorder);
                xc = PAD + DIA / 2;  // draw southwest arc
                yc = height - PAD - DIA / 2;
                customPaint = new CustomPaint(xc, yc, new Point2D.Double(0, DIA / 2),
                        DIA / 2, BORDER, colorIn, colorOut);
                g2.setPaint(customPaint);
                eIn = getInnerEllipse(swOrigin, swDiag);
                eOut = getOuterEllipse(swOrigin, swDiag);
                arcBorder = getArcArea(eIn, eOut, 180.0);
                g2.fill(arcBorder);
        }
    }

    private Ellipse2D getInnerEllipse(Point2D center, Point2D corner) {
        return new Ellipse2D.Double(center.getX() - DIA / 2,
                center.getY() - DIA / 2, DIA, DIA);
    }

    private Ellipse2D getOuterEllipse(Point2D center, Point2D corner) {
        int w = DIA, h = DIA;
        if (shadowVertex < 2) {
            if (center.getY() > corner.getY()) {
                h += 2 * BORDER;
            } else {
                w += 2 * BORDER;
            }
        } else if (center.getY() > corner.getY()) {
            w += 2 * BORDER;
        } else {
            h += 2 * BORDER;
        }
        return new Ellipse2D.Double(center.getX() - w / 2, center.getY() - h / 2, w, h);
    }

    private Area getArcArea(Ellipse2D e1, Ellipse2D e2, double start) {
        Arc2D arc1 = new Arc2D.Double(e1.getBounds2D(), start, 90.0, Arc2D.PIE);
        Arc2D arc2 = new Arc2D.Double(e2.getBounds2D(), start, 90.0, Arc2D.PIE);
        Area arc = new Area(arc2);
        arc.subtract(new Area(arc1));
        return arc;
    }

    private void drawNorthSide(Graphics2D g2) {
        gradient = new GradientPaint(width / 2, PAD - BORDER, colorOut,
                width / 2, PAD, colorIn);
        g2.setPaint(gradient);
        g2.fill(new Rectangle2D.Double(PAD + DIA / 2, PAD - BORDER,
                width - 2 * (PAD + DIA / 2) + 1, BORDER));
    }

    private void drawWestSide(Graphics2D g2) {
        gradient = new GradientPaint(PAD - BORDER, height / 2, colorOut,
                PAD, height / 2, colorIn);
        g2.setPaint(gradient);
        g2.fill(new Rectangle2D.Double(PAD - BORDER, PAD + DIA / 2,
                BORDER, height - 2 * (PAD + DIA / 2) + 1));
    }

    private void drawSouthSide(Graphics2D g2) {
        gradient = new GradientPaint(width / 2, height - PAD, colorIn,
                width / 2, height - PAD + BORDER, colorOut);
        g2.setPaint(gradient);
        g2.fill(new Rectangle2D.Double(PAD + DIA / 2, height - PAD,
                width - 2 * (PAD + DIA / 2) + 1, BORDER));
    }

    private void drawEastSide(Graphics2D g2) {
        gradient = new GradientPaint(width - PAD, height / 2, colorIn,
                width - PAD + BORDER, height / 2, colorOut);
        g2.setPaint(gradient);
        g2.fill(new Rectangle2D.Double(width - PAD, PAD + DIA / 2,
                BORDER, height - 2 * (PAD + DIA / 2) + 1));
    }

    /**
     * Draws the central, full-shaded arc (opposite of the unshaded arc).
     */
    private void drawVertexArc(Graphics2D g2, int index) {
        switch (index) {
            case NORTHEAST:
                xc = width - PAD - DIA / 2;
                yc = PAD + DIA / 2;
                customPaint = new CustomPaint(xc, yc, new Point2D.Double(0, DIA / 2),
                        DIA / 2, BORDER, colorIn, colorOut);
                g2.setPaint(customPaint);
                eIn = new Ellipse2D.Double(width - PAD - DIA, PAD, DIA, DIA);
                eOut = new Ellipse2D.Double(width - PAD - DIA - BORDER, PAD - BORDER,
                        DIA + 2 * BORDER, DIA + 2 * BORDER);
                arcBorder = getArcArea(eIn, eOut, 0.0);
                g2.fill(arcBorder);
                break;
            case NORTHWEST:
                xc = PAD + DIA / 2;
                yc = PAD + DIA / 2;
                customPaint = new CustomPaint(xc, yc, new Point2D.Double(0, DIA / 2),
                        DIA / 2, BORDER, colorIn, colorOut);
                g2.setPaint(customPaint);
                eIn = new Ellipse2D.Double(PAD, PAD, DIA, DIA);
                eOut = new Ellipse2D.Double(PAD - BORDER, PAD - BORDER,
                        DIA + 2 * BORDER, DIA + 2 * BORDER);
                arcBorder = getArcArea(eIn, eOut, 90.0);
                g2.fill(arcBorder);
                break;
            case SOUTHWEST:
                xc = PAD + DIA / 2;
                yc = height - PAD - DIA / 2;
                customPaint = new CustomPaint(xc, yc, new Point2D.Double(0, DIA / 2),
                        DIA / 2, BORDER, colorIn, colorOut);
                g2.setPaint(customPaint);
                eIn = new Ellipse2D.Double(PAD, height - PAD - DIA, DIA, DIA);
                eOut = new Ellipse2D.Double(PAD - BORDER, height - PAD - DIA - BORDER,
                        DIA + 2 * BORDER, DIA + 2 * BORDER);
                arcBorder = getArcArea(eIn, eOut, 180.0);
                g2.fill(arcBorder);
                break;
            case SOUTHEAST:
                xc = width - PAD - DIA / 2;
                yc = height - PAD - DIA / 2;
                customPaint = new CustomPaint(xc, yc, new Point2D.Double(0, DIA / 2),
                        DIA / 2, BORDER, colorIn, colorOut);
                g2.setPaint(customPaint);
                eIn = new Ellipse2D.Double(width - PAD - DIA, height - PAD - DIA, DIA, DIA);
                eOut = new Ellipse2D.Double(width - PAD - DIA - BORDER,
                        height - PAD - DIA - BORDER, DIA + 2 * BORDER, DIA + 2 * BORDER);
                arcBorder = getArcArea(eIn, eOut, 270.0);
                g2.fill(arcBorder);
        }
    }

    private void calculateArcOrigins() {
        neOrigin = new Point2D.Double(width - PAD - DIA / 2, PAD + DIA / 2);
        nwOrigin = new Point2D.Double(PAD + DIA / 2, PAD + DIA / 2);
        swOrigin = new Point2D.Double(PAD + DIA / 2, height - PAD - DIA / 2);
        seOrigin = new Point2D.Double(width - PAD - DIA / 2, height - PAD - DIA / 2);
    }

    private void calculateCardinalDiagonals() {
        neDiag = new Point2D.Double(neOrigin.getX()
                + DIA * Math.cos(Math.toRadians(45)) / 2,
                neOrigin.getY() - DIA * Math.sin(Math.toRadians(45)) / 2);
        nwDiag = new Point2D.Double(nwOrigin.getX()
                + DIA * Math.cos(Math.toRadians(135)) / 2,
                nwOrigin.getY() - DIA * Math.sin(Math.toRadians(135)) / 2);
        swDiag = new Point2D.Double(swOrigin.getX()
                + DIA * Math.cos(Math.toRadians(225)) / 2,
                swOrigin.getY() - DIA * Math.sin(Math.toRadians(225)) / 2);
        seDiag = new Point2D.Double(seOrigin.getX()
                + DIA * Math.cos(Math.toRadians(315)) / 2,
                seOrigin.getY() - DIA * Math.sin(Math.toRadians(315)) / 2);
    }

    public Dimension getInnerSize() {
        return new Dimension((int) nwOrigin.distance(neOrigin),
                (int) nwOrigin.distance(swOrigin));
    }
}

class ShadowSelector extends JPanel {

    private static final long serialVersionUID = 1L;
    private ShadeOptionsPanel soPanel;
    private String[] directions = {"northeast", "northwest", "southwest", "southeast"};

    public ShadowSelector(ShadeOptionsPanel sop) {
        soPanel = sop;

        final SpinnerListModel model = new SpinnerListModel(directions);
        model.setValue(directions[3]);
        JSpinner spinner = new JSpinner(model);
        spinner.setPreferredSize(new Dimension(90, spinner.getPreferredSize().height));
        spinner.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                String value = (String) model.getValue();
                soPanel.shadowVertex = model.getList().indexOf(value);
                soPanel.repaint();
            }
        });
        add(new JLabel("shadow vertex", JLabel.RIGHT));
        add(spinner);
    }
}

class CustomPaint implements Paint {

Point2D originP, radiusP;
int radius, border;
Color colorIn, colorOut;

public CustomPaint(int x, int y, Point2D radiusP,
        int radius, int border,
        Color colorIn, Color colorOut) {
    originP = new Point2D.Double(x, y);
    this.radiusP = radiusP;
    this.radius = radius;
    this.border = border;
    this.colorIn = colorIn;
    this.colorOut = colorOut;
}

@Override
public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform, RenderingHints hints) {
    Point2D xformOrigin = xform.transform(originP, null), xformRadius = xform.deltaTransform(radiusP, null);
    return new CustomPaintContext(xformOrigin, xformRadius, radius, border, colorIn, colorOut);
}

@Override
public int getTransparency() {
    int alphaIn = colorIn.getAlpha();
    int alphaOut = colorOut.getAlpha();
    return (((alphaIn & alphaOut) == 0xff) ? OPAQUE : TRANSLUCENT);
}
}

class CustomPaintContext implements PaintContext {

Point2D originP, radiusP;
Color colorIn, colorOut;
int radius, border;

public CustomPaintContext(Point2D originP, Point2D radiusP, int radius, int border, Color colorIn, Color colorOut) {
    this.originP = originP;
    this.radiusP = radiusP;
    this.radius = radius;
    this.border = border;
    this.colorIn = colorIn;
    this.colorOut = colorOut;
}

@Override
public void dispose() {
}

@Override
public ColorModel getColorModel() {
    return ColorModel.getRGBdefault();
}

@Override
public Raster getRaster(int x, int y, int w, int h) {
    WritableRaster raster = getColorModel().createCompatibleWritableRaster(w, h);
    int[] data = new int[w * h * 4];
    for (int j = 0; j < h; j++) {
        for (int i = 0; i < w; i++) {
            double distance = originP.distance(x + i, y + j);
            double r = radiusP.distance(radius, radius);
            double ratio = distance - r < 0 ? 0.0 : (distance - r) / border;
            if (ratio > 1.0) {
                ratio = 1.0;
            }
            int base = (j * w + i) * 4;
            data[base + 0] = (int) (colorIn.getRed() + ratio * (colorOut.getRed() - colorIn.getRed()));
            data[base + 1] = (int) (colorIn.getGreen() + ratio * (colorOut.getGreen() - colorIn.getGreen()));
            data[base + 2] = (int) (colorIn.getBlue() + ratio * (colorOut.getBlue() - colorIn.getBlue()));
            data[base + 3] = (int) (colorIn.getAlpha() + ratio * (colorOut.getAlpha() - colorIn.getAlpha()));
        }
    }
    raster.setPixels(0, 0, w, h, data);
    return raster;
}
}