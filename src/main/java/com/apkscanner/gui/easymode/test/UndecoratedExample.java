package com.apkscanner.gui.easymode.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.ImageObserver;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import com.apkscanner.resource.RImg;
import com.apkspectrum.resource._RImg;


public class UndecoratedExample {
    static JFrame frame = new JFrame();

    static class MainPanel extends JPanel {
        private static final long serialVersionUID = -738578708435529100L;

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(510, 250);
        }
    }

    static public class MyButton extends JButton {
        private static final long serialVersionUID = -3110743542595946912L;

        Image image;
        ImageObserver imageObserver;

        public MyButton(ImageIcon icon) {
            super();
            icon = _RImg.PERM_GROUP_PHONE_CALLS.getImageIcon(15, 15);
            image = icon.getImage();
            imageObserver = icon.getImageObserver();
        }

        public void paint(Graphics g) {
            super.paint(g);
            g.drawImage(image, 0, 0, getWidth(), getHeight(), imageObserver);
        }
    }

    static class BorderPanel extends JPanel {
        private static final long serialVersionUID = 9027209933412390193L;

        JButton stackLabel;
        int pX, pY;

        public BorderPanel() {
            setLayout(new FlowLayout(FlowLayout.RIGHT));

            // ((FlowLayout)getLayout()).setVgap(2);
            ImageIcon icon = _RImg.PERM_GROUP_PHONE_CALLS.getImageIcon(15, 15);
            stackLabel = new JButton(icon);
            // stackLabel.setIcon(icon);

            stackLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            // stackLabel.setBorderPainted( false );
            // stackLabel.setContentAreaFilled( false )
            // stackLabel.setFocusPainted(false);
            // stackLabel.addActionListener(this);
            // stackLabel.setSelected(true);
            stackLabel.setContentAreaFilled(false);
            stackLabel.setRolloverIcon(RImg.APK_FILE_ICON.getImageIcon(15, 15));
            setBackground(new Color(230, 230, 230));


            add(stackLabel);

            stackLabel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent me) {
                    // Get x,y and store them
                    pX = me.getX();
                    pY = me.getY();
                }

                public void mouseDragged(MouseEvent me) {

                    frame.setLocation(frame.getLocation().x + me.getX() - pX,
                            frame.getLocation().y + me.getY() - pY);
                }
            });
            addMouseMotionListener(new MouseAdapter() {
                public void mouseDragged(MouseEvent me) {
                    frame.setLocation(frame.getLocation().x + me.getX() - pX,
                            frame.getLocation().y + me.getY() - pY);
                }
            });
        }
    }

    static class OutsidePanel extends JPanel {
        private static final long serialVersionUID = 7536723251557777813L;

        public OutsidePanel() {
            setLayout(new BorderLayout());
            add(new MainPanel(), BorderLayout.CENTER);
            add(new BorderPanel(), BorderLayout.PAGE_START);
            setBorder(new LineBorder(Color.BLACK, 0));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.setUndecorated(true);
                frame.add(new OutsidePanel());
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }
}
