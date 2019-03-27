package com.apkscanner.gui.easymode.util;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.apkscanner.gui.easymode.EasyGuiMain;
import com.apkscanner.gui.easymode.test.FontResizingLabel;
import com.apkscanner.util.Log;

public class EasyTextField extends JTextField {
	public static final int MIN_FONT_SIZE = 3;
	public static final int MAX_FONT_SIZE = 20;
	Graphics g;
	int currFontSize = 10;	
	
	public EasyTextField(String text) {
//		super(text); // 20ms slow
		currFontSize = this.getFont().getSize();		
		init();
		setEasyTextField(this);
	}
	
	private void setEasyTextField(JTextField textfield) {
		textfield.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
		textfield.setEditable(false);
		textfield.setOpaque(false);
		textfield.setFont(new Font(getFont().getName(), Font.PLAIN, 15));
	}
	
	protected void init() {
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				adaptLabelFont();
			}
		});
	}

	public void setText(final String str) {
		super.setText(str);
		adaptLabelFont();
	}
	
	protected void adaptLabelFont() {
		if (g == null) {
			g = getGraphics();
			if (g == null)
				return;
			// return;
		}
		currFontSize = this.getFont().getSize();
		Rectangle r = getBounds();
		
		//Log.d(getText() + getBorder().getBorderInsets(this).left);
		
		r.x = 0;
		r.y = 0;
		
		//for padding
		r.width = r.width - getBorder().getBorderInsets(this).left -getBorder().getBorderInsets(this).right;
		r.height = r.height; 
		
		int fontSize = Math.max(MIN_FONT_SIZE, currFontSize);
		Font f = getFont();

		Rectangle r1 = new Rectangle(getTextSize(getFont()));
		while (!r.contains(r1)) {
			fontSize--;
			if (fontSize <= MIN_FONT_SIZE)
				break;
			r1 = new Rectangle(getTextSize(f.deriveFont(f.getStyle(), fontSize)));
		}

		Rectangle r2 = new Rectangle();
		while (fontSize < MAX_FONT_SIZE) {
			r2.setSize(getTextSize(f.deriveFont(f.getStyle(), fontSize + 1)));
			if (!r.contains(r2)) {
				break;
			}
			fontSize++;
		}
		setFont(f.deriveFont(f.getStyle(), fontSize));
		repaint();
	}

	private Dimension getTextSize(Font f) {
		Dimension size = new Dimension();
		// g.setFont(f); // superfluous.
		FontMetrics fm = g.getFontMetrics(f);
		size.width = fm.stringWidth(this.getText());
		size.height = fm.getHeight();
		return size;
	}

	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		super.paintComponent(g);
		// adaptLabelFont(EasyTextField.this);
	}
}
