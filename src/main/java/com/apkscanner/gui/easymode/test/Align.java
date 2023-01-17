package com.apkscanner.gui.easymode.test;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Align {
    private static final int PREF_HEIGHT = 100;

    Align() {
        JFrame frame = new JFrame("Align test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel bg = new JPanel();
        ((FlowLayout) bg.getLayout()).setAlignOnBaseline(true);
        frame.add(bg);
        JPanel left = new JPanel();
        left.setBackground(Color.BLUE);
        left.setPreferredSize(new Dimension(100, PREF_HEIGHT));
        bg.add(left);

        JPanel right = new JPanel() {
            private static final long serialVersionUID = 4811265060819408565L;

            @Override
            public int getBaseline(int width, int height) {
                return 100;
            }
        };
        right.setBackground(Color.GREEN);
        right.setPreferredSize(new Dimension(100, 50));
        bg.add(right);

        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Align();
            }
        });
    }
}
