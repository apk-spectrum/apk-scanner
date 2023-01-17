package com.apkscanner.gui.easymode.util;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;


public class AndroidLikeToast extends JDialog {
    private static final long serialVersionUID = 1584433476131530937L;
    String msg;
    JFrame frame;
    // you can set the time for how long the dialog will be displayed
    public static final int LENGTH_LONG = 5000;
    public static final int LENGTH_SHORT = 1000;

    public AndroidLikeToast(JFrame frame, boolean modal, String msg) {
        super(frame, modal);
        this.msg = msg;
        this.frame = frame;
        initComponents();
    }

    private void initComponents() {
        setLayout(new FlowLayout(FlowLayout.CENTER));
        addComponentListener(new ComponentAdapter() {
            // Give the window an rounded rect shape.

            @Override
            public void componentResized(ComponentEvent e) {
                setShape(new RoundRectangle2D.Double(0, (getHeight() - 20) / 2, getWidth(), 20, 20,
                        20));
            }
        });
        setUndecorated(true);
        setSize(200, 30);
        setLocationRelativeTo(frame);// adding toast to frame or use null
        getContentPane().setBackground(Color.BLACK);

        // Determine what the GraphicsDevice can support.
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        final boolean isTranslucencySupported =
                gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT);

        // If shaped windows aren't supported, exit.
        if (!gd.isWindowTranslucencySupported(
                GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT)) {
            System.err.println("Shaped windows are not supported");
        }

        // If translucent windows are supported,
        // create an opaque window.
        // Set the window to 50% translucency, if supported.
        if (isTranslucencySupported) {
            setOpacity(0.7f);
        } else {
            System.out.println("Translucency is not supported.");
        }

        JLabel label = new JLabel();
        label.setForeground(Color.WHITE);
        label.setText(msg);
        label.setFont(new Font(label.getFont().getName(), Font.PLAIN, 15));
        add(label);
    }

    static public void ShowToast(String str, JComponent component) {
        final JDialog dialog = new AndroidLikeToast(null, false, str);

        if (component != null) dialog.setLocationRelativeTo(component);

        Timer timer = new Timer(AndroidLikeToast.LENGTH_SHORT, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        });
        timer.setRepeats(false);
        timer.start();

        dialog.setVisible(true);
    }

}
