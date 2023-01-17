package com.apkscanner.test;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.WindowConstants;



public class TestJLayerZoom {

    public static void main(String args[]) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

                JPanel panel = new JPanel();
                final ZoomUI layerUI = new ZoomUI();
                final JLayer<JComponent> jLayer = new JLayer<JComponent>(panel, layerUI);

                JButton zoomIn = new JButton("Zoom In");
                zoomIn.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        layerUI.zoom += 0.1;

                        jLayer.revalidate();
                        jLayer.repaint();
                    }
                });
                panel.add(zoomIn);

                JButton zoomOut = new JButton("Zoom Out");
                zoomOut.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        layerUI.zoom -= 0.1;
                        jLayer.revalidate();
                        jLayer.repaint();
                    }
                });
                panel.add(zoomOut);

                frame.setPreferredSize(new Dimension(400, 200));
                frame.add(jLayer);
                frame.pack();
                frame.setVisible(true);
            }
        });
    }


}
