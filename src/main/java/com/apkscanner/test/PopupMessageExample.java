package com.apkscanner.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.LinearGradientPaint;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

public class PopupMessageExample {

    public static void main(String[] args) {
        new PopupMessageExample();
    }

    public PopupMessageExample() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }
                new PopupMessageBuilder().withDelay(10000).withMessage("Hello, this is a fading message").show();
            }
        });
    }

    static public class PopupMessageBuilder {

        private int delay;
        private Point location;
        private String message;

        private long startTime;
        //private Timer fadeTimer;

        
        JWindow frame;
        public PopupMessageBuilder at(Point p) {
            this.location = p;
            return this;
        }

        public PopupMessageBuilder withDelay(int delay) {
            this.delay = delay;
            return this;
        }

        public PopupMessageBuilder withMessage(String msg) {
            this.message = msg;
            return this;
        }

        public PopupMessageBuilder show() {

        	frame = new JWindow();
            frame.setOpacity(0f);
            frame.setBackground(new Color(0, 0, 0, 0));
            BackgroundPane pane = new BackgroundPane();
            pane.setMessage(message);
            frame.add(pane);
            frame.pack();
            if (location == null) {
                frame.setLocationRelativeTo(null);
            } else {
                frame.setLocation(location);
            }
            frame.setVisible(true);
            frame.setAlwaysOnTop(true);
            
            new FadeTimer(frame, 1000, 0f, 1f, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Timer timer = new Timer(delay, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            new FadeTimer(frame, 1000, 1f, 0f, new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    frame.dispose();
                                }
                            }).start();
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                }
            }).start();

            return this;
        }

        public void dispose() {
        	frame.dispose();
        }
        
        public class FadeTimer extends Timer implements ActionListener {

            /**
			 * 
			 */
			private static final long serialVersionUID = 4546123210581924146L;
			private final float startAt;
            private final float endAt;
            private final int duration;
            //private long startTimer;

            private ActionListener endListener;

            private Window window;

            public FadeTimer(Window window, int duration, float startAt, float endAt, ActionListener endListener) {
                super(5, null);
                addActionListener(this);
                this.duration = duration;
                this.startAt = startAt;
                this.endAt = endAt;
                this.window = window;

                this.endListener = endListener;
            }

            @Override
            public void start() {
                startTime = System.currentTimeMillis();
                super.start();
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                long now = System.currentTimeMillis();
                long lapsed = now - startTime;
                float opacity = startAt;
                if (lapsed >= duration) {
                    opacity = endAt;
                    ((Timer) e.getSource()).stop();
                    if (endListener != null) {
                        endListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "stopped"));
                    }
                } else {
                    float progress = (float) lapsed / (float) duration;
                    float distance = endAt - startAt;
                    opacity = (float) (distance * progress);
                    opacity += startAt;
                }
                window.setOpacity(opacity);
            }

        }

        public class BackgroundPane extends JPanel {

            /**
			 * 
			 */
			private static final long serialVersionUID = 27094827930732975L;
			private MessagePane messagePane;

            public BackgroundPane() {
                setBorder(new EmptyBorder(10, 10, 10, 10));
                messagePane = new MessagePane();
                setLayout(new BorderLayout());
                add(messagePane);
                setOpaque(false);
            }

            public void setMessage(String msg) {
                messagePane.setMessage(msg);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
                g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                LinearGradientPaint glp = new LinearGradientPaint(
                                new Point(0, 0),
                                new Point(0, getHeight()),
                                new float[]{0f, 1f},
                                new Color[]{Color.GRAY, Color.BLACK});
                RoundRectangle2D frame = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2d.setPaint(glp);
                g2d.fill(frame);
                g2d.setColor(Color.WHITE);
                g2d.draw(frame);
            }

        }

        public class MessagePane extends JPanel {

            /**
			 * 
			 */
			private static final long serialVersionUID = 553830274361457410L;
			private JLabel label;
        	RSyntaxTextArea xmltextArea;
            public MessagePane() {
                setOpaque(false);
                label = new JLabel();
                label.setForeground(Color.WHITE);
                setLayout(new GridBagLayout());
                
        		xmltextArea = new RSyntaxTextArea(20, 60);
        		xmltextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        		xmltextArea.setCodeFoldingEnabled(true);				
        		xmltextArea.setEditable(false);		
        		RTextScrollPane sp = new RTextScrollPane(xmltextArea);
                
        		
                add(sp);
            }

            public void setMessage(String msg) {
                label.setText(msg);
                xmltextArea.setText(msg);
            }

        }

    }

}