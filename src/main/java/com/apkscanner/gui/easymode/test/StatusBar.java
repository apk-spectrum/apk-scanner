package com.apkscanner.gui.easymode.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JViewport;

public class StatusBar extends JFrame {
    private static final long serialVersionUID = 1564165742861752330L;

    private final JPanel statusBar;
    private final JPanel leftrightPanel;
    private final JPanel myPane;
    private final JButton rightButton;
    private final JButton leftButton;
    private final JViewport viewport;
    private final JPanel iconsPanel;
    private JButton button;

    public StatusBar() {
        setLayout(new BorderLayout());
        statusBar = new JPanel();
        statusBar.setLayout(new BorderLayout());
        iconsPanel = new JPanel();
        iconsPanel.setLayout(new BoxLayout(iconsPanel, BoxLayout.LINE_AXIS));
        iconsPanel.setBackground(Color.LIGHT_GRAY);
        viewport = new JViewport();
        viewport.setView(iconsPanel);
        leftrightPanel = new JPanel();
        leftrightPanel.setBackground(Color.WHITE);
        rightButton = new JButton("aaa");
        rightButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int iconsPanelStartX = iconsPanel.getX();
                if (iconsPanelStartX < 0) {
                    Point origin = viewport.getViewPosition();
                    if (Math.abs(iconsPanelStartX) < 20) {
                        origin.x -= Math.abs(iconsPanelStartX);
                    } else {
                        origin.x -= 20;
                    }
                    viewport.setViewPosition(origin);
                }
            }
        });
        leftButton = new JButton("aaaaa");
        leftButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Point origin = viewport.getViewPosition();
                origin.x += 20;
                viewport.setViewPosition(origin);
            }
        });
        leftrightPanel.add(rightButton);
        leftrightPanel.add(leftButton);
        statusBar.add(viewport);
        statusBar.add(leftrightPanel, BorderLayout.LINE_END);
        add(statusBar, BorderLayout.SOUTH);
        myPane = new JPanel();
        add(myPane, BorderLayout.CENTER);
        for (int i = 1; i < 20; i++) {
            button = new JButton("Button " + i);
            iconsPanel.add(button);
        }
    }
}
