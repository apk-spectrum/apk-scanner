package com.apkscanner.gui.easymode.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

public class EasyRoundButton extends JButton {
    private static final long serialVersionUID = -6927025737749969747L;
    private boolean entered = false;
    private boolean isfold = true;
    private Color originalcolor;
    private int len = 0;
    private String strfold;
    private String strspread = null;
    private static int TYPE_NOMAL = 0;
    private static int TYPE_POSSIBLE_SPREAD = 1;
    private static int TYPE_POSSIBLE_ACTION = 2;

    // private static Color spreadColor = new Color(0x368AFF);
    private static Color spreadColor = Color.lightGray;

    private int type;

    public EasyRoundButton(ImageIcon icon) {
        super(icon);
        // setlistener();
        type = TYPE_NOMAL;
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setContentAreaFilled(false);
        setFocusable(false);
        setlistener();
    }

    public EasyRoundButton(String str) {
        super(str);
        strfold = str;
        type = TYPE_NOMAL;
        // setlistener();
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setContentAreaFilled(false);
        setFocusable(false);
        setlistener();
    }

    public EasyRoundButton(String str, String spread) {
        super(str);
        strfold = str;
        strspread = spread;

        if (str.equals(spread)) {
            type = TYPE_NOMAL;
        } else if (spread.equals("")) {
            type = TYPE_POSSIBLE_ACTION;
        } else {
            type = TYPE_POSSIBLE_SPREAD;
        }
        // setlistener();
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setContentAreaFilled(false);
        setFocusable(false);
        setlistener();
    }


    void setlistener() {
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // this.setBackground(new Color(255,255,255));
                // setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
                entered = true;
                repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                // setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                entered = false;
                repaint();
            }

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // entered = false;
                if (SwingUtilities.isRightMouseButton(evt)) {
                    setclipboard();
                } else {
                    if (type == TYPE_POSSIBLE_SPREAD && strspread != null) {
                        isfold = !isfold;
                        if (!isfold)
                            setText(strspread);
                        else {
                            setText(strfold);
                        }
                    }
                }
                updateUI();
            }

            public void mouseReleased(MouseEvent e) {

            }
        });
    }

    private void setclipboard() {
        StringSelection stringSelection = new StringSelection(getText());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        AndroidLikeToast.ShowToast("Copying to the clipboard!", this);
    }


    public void setBackground(Color color) {
        super.setBackground(color);
        originalcolor = color;
    }

    public void setshadowlen(int setlen) {
        this.len = setlen;
        // setBorder(BorderFactory.createEmptyBorder(1, len/2, len, len/2));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics gr) {
        Graphics2D g = (Graphics2D) gr;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (isfold && type != TYPE_NOMAL) {
            g.setColor(spreadColor);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 13, 13);
            if (entered) {
                g.setColor(originalcolor.darker().darker());
            } else {
                g.setColor(originalcolor);
            }

            if (type == TYPE_POSSIBLE_SPREAD)
                g.fillRoundRect(0, 0, getWidth() - 2, getHeight() - len, 13, 13);
            if (type == TYPE_POSSIBLE_ACTION)
                g.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 13, 13);
        } else {
            if (type == TYPE_POSSIBLE_SPREAD) {
                g.setColor(spreadColor);
                g.fillRoundRect(0, 0, getWidth() - 2, getHeight() - len, 13, 13);
            }
            if (entered) {
                g.setColor(originalcolor.darker().darker());
            } else {
                g.setColor(originalcolor);
            }
            if (type == TYPE_POSSIBLE_SPREAD) {
                g.fillRoundRect(2, 0, getWidth() - 2, getHeight() - len, 13, 13);
            } else {
                g.fillRoundRect(0, 0, getWidth() - len, getHeight() - len, 13, 13);
            }
        }

        super.paintComponent(gr);
    }

}
