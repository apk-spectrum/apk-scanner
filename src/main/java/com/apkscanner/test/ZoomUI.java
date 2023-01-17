package com.apkscanner.test;

import java.awt.AWTEvent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.plaf.LayerUI;

public class ZoomUI extends LayerUI<JComponent> {
    private static final long serialVersionUID = -9175708480685386829L;

    public double zoom = 1; // Changing this value seems to have no effect

    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.scale(zoom, zoom);
        super.paint(g2, c);
        g2.dispose();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        @SuppressWarnings("rawtypes")
        JLayer jlayer = (JLayer) c;
        jlayer.setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.ACTION_EVENT_MASK
                | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }

    @Override
    public void uninstallUI(JComponent c) {
        @SuppressWarnings("rawtypes")
        JLayer jlayer = (JLayer) c;
        jlayer.setLayerEventMask(0);
        super.uninstallUI(c);
    }
}
