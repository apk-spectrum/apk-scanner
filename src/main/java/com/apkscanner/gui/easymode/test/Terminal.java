package com.apkscanner.gui.easymode.test;

import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsDevice.WindowTranslucency;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.apkscanner.resource.RImg;

public class Terminal extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel jContentPane = null;
    private JLabel jLabel = null;

    public Terminal() {
        super();
        setBackground(new Color(0, 0, 0, 0));
        initialize();
    }

    private void initialize() {
        this.setSize(434, 695);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(getJContentPane());
        this.setTitle("JFrame");
        this.setUndecorated(true);
        this.getLayeredPane().remove(getLayeredPane().getComponent(1));// removes titlebar
        this.getRootPane().setBorder(null);// removes border
    }

    private JPanel getJContentPane() {

        if (jContentPane == null) {
            jLabel = new JLabel();
            jLabel.setBounds(new Rectangle(0, -10, 434, 674));
            jLabel.setIcon(RImg.ADD_TO_DESKTOP.getImageIcon());
            jLabel.setText("JLabel");
            jContentPane = new JPanel();
            jContentPane.setLayout(null);
            jContentPane.add(jLabel, null);
            jContentPane.setOpaque(false);
        }
        return jContentPane;
    }


    public static void main(String args[]) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        boolean isPerPixelTranslucencySupported =
                gd.isWindowTranslucencySupported(WindowTranslucency.TRANSLUCENT);

        if (!isPerPixelTranslucencySupported) {
            System.out.println("Per-pixel translucency is not supported");
            System.exit(0);
        }

        JFrame.setDefaultLookAndFeelDecorated(true);
        Terminal terminal = new Terminal();
        terminal.setVisible(true);
    }

}
