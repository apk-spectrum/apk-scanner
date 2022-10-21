package com.apkscanner.gui.easymode.test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

// Improved version of http://java-sl.com/tip_adapt_label_font_size.html
public class FontResizingLabel extends JLabel {
    private static final long serialVersionUID = -7659503471621773815L;

	public static final int MIN_FONT_SIZE=3;
    public static final int MAX_FONT_SIZE=240;
    Graphics g;
    int currFontSize = 0;

    public FontResizingLabel(String text) {
        super(text);
        currFontSize = this.getFont().getSize();
        init();
    }

    protected void init() {
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                adaptLabelFont(FontResizingLabel.this);
            }
        });
    }

   protected void adaptLabelFont(JLabel l) {
        if (g==null) {
            return;
        }
        currFontSize = this.getFont().getSize();

        Rectangle r  = l.getBounds();
        r.x          = 0;    
        r.y          = 0;    
        int fontSize = Math.max(MIN_FONT_SIZE, currFontSize);
        Font f       = l.getFont();

        Rectangle r1 = new Rectangle(getTextSize(l, l.getFont()));
        while (!r.contains(r1)) {
               fontSize --;
            if (fontSize <= MIN_FONT_SIZE) 
                break;
            r1 = new Rectangle(getTextSize(l, f.deriveFont(f.getStyle(), fontSize)));
        }    

        Rectangle r2 = new Rectangle();
        while (fontSize < MAX_FONT_SIZE) {
            r2.setSize(getTextSize(l, f.deriveFont(f.getStyle(),fontSize+1)));
            if (!r.contains(r2)) {
                break;
            }
            fontSize++;
        }

        setFont(f.deriveFont(f.getStyle(),fontSize));
        repaint();
    }

    private Dimension getTextSize(JLabel l, Font f) {
        Dimension size  = new Dimension();
        //g.setFont(f);   // superfluous.
        FontMetrics fm  = g.getFontMetrics(f);
        size.width      = fm.stringWidth(l.getText());
        size.height     = fm.getHeight();
        return size;
    }
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.g=g;
    }

    public static void main(String[] args) throws Exception {
        FontResizingLabel label=new FontResizingLabel("Some text");
        JFrame frame=new JFrame("Resize label font");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.getContentPane().add(label);

        frame.setSize(300,300);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}