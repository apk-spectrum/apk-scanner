package com.apkscanner.gui.easymode.test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;

public class HardwareAcceleration extends JFrame {
    private static final long serialVersionUID = 9101279457165111191L;

	public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch(Exception err) {
                    err.printStackTrace();
                }
 
                System.setProperty("sun.java2d.trace", "count");
                new HardwareAcceleration().setVisible(true);
            }
        });
    }
 
    boolean back = false;
    Rectangle rect = new Rectangle(0, 0, 200, 400);
 
    BufferStrategy bufferStrategy;
    GraphicsDevice gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
 
    public HardwareAcceleration() {
 
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setIgnoreRepaint(true);
 
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                System.exit(0);
            }
        });
 
        JOptionPane.showMessageDialog(null, "Click anywhere to exit full screen.");
 
        gc.setFullScreenWindow(this);
        createBufferStrategy(2);
        bufferStrategy = getBufferStrategy();
 
        Timer logicTimer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logic();
            }
        });
        logicTimer.start();
 
        Timer repaintTimer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                draw();
            }
        });
        repaintTimer.start();
    }
 
    public void logic() {
        if(!back) {
 
            if((rect.x + rect.width) + 2 < getWidth()) {
                rect.x += 2;
            } else {
                rect.x = getWidth() - rect.width;
                back = !back;
            }
        } else {
            if(rect.x - 2 > 0) {
                rect.x -= 2;
            } else {
                rect.x = 0;
                back = !back;
            }
        }
    }
 
    public void draw() {
 
        Graphics2D g2 = null;
 
        try {
            g2 = (Graphics2D)bufferStrategy.getDrawGraphics();
 
            g2.setColor(Color.black);
            g2.fillRect(0, 0, getWidth(), getHeight());
 
            g2.setColor(Color.blue);
            g2.fill(rect);
        } finally {
            if(g2 != null) g2.dispose();
        }
 
        bufferStrategy.show();
        Toolkit.getDefaultToolkit().sync();
    }
}