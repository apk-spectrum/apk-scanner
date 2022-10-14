package com.apkscanner.gui.easymode.test;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class TransparentBorder {

    public TransparentBorder() {
        // Create and set up the window.
        final JFrame frame = new JFrame("Transparent Border");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 100);

        // set window size
        frame.setUndecorated(false);

        // P1
        final JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLUE);

        final JButton btn = new JButton("Search");
        btn.setOpaque(false);
        panel.add(btn, BorderLayout.EAST);
        JPanel wrappingPanel = new JPanel(new BorderLayout());
        wrappingPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        wrappingPanel.setOpaque(false);
        final JTextField field = new JTextField();
        field.setBorder(null);
        wrappingPanel.add(field);
        panel.add(wrappingPanel);

        frame.add(panel);
        frame.revalidate();
        // Display the window.
        // frame.pack();
        frame.setVisible(true);

    }

    public static void main(final String[] args) {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TransparentBorder();
            }
        });
    }
}