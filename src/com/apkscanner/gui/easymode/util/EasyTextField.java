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

import javax.swing.JLabel;
import javax.swing.JTextField;

import com.apkscanner.gui.easymode.test.FontResizingLabel;
import com.apkscanner.util.Log;

public class EasyTextField extends JTextField {
	public static final int MIN_FONT_SIZE = 3;
	public static final int MAX_FONT_SIZE = 20;
	Graphics g;
	int currFontSize = 0;

	public EasyTextField(String text) {
		super(text);
		currFontSize = this.getFont().getSize();
		init();
	}

	protected void init() {
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				adaptLabelFont(EasyTextField.this);
			}
		});
	}

	protected void adaptLabelFont(EasyTextField easyTextField) {
		if (g == null) {
			g = getGraphics();
			if (g == null)
				return;
			// return;
		}
		currFontSize = this.getFont().getSize();
		Rectangle r = easyTextField.getBounds();
		
		r.x = 0;
		r.y = 0;
		int fontSize = Math.max(MIN_FONT_SIZE, currFontSize);
		Font f = easyTextField.getFont();

		Rectangle r1 = new Rectangle(getTextSize(easyTextField, easyTextField.getFont()));
		while (!r.contains(r1)) {
			fontSize--;
			if (fontSize <= MIN_FONT_SIZE)
				break;
			r1 = new Rectangle(getTextSize(easyTextField, f.deriveFont(f.getStyle(), fontSize)));
		}

		Rectangle r2 = new Rectangle();
		while (fontSize < MAX_FONT_SIZE) {
			r2.setSize(getTextSize(easyTextField, f.deriveFont(f.getStyle(), fontSize + 1)));
			if (!r.contains(r2)) {
				break;
			}
			fontSize++;
		}
		setFont(f.deriveFont(f.getStyle(), fontSize));
		repaint();
	}

	private Dimension getTextSize(JTextField l, Font f) {
		Dimension size = new Dimension();
		// g.setFont(f); // superfluous.
		FontMetrics fm = g.getFontMetrics(f);
		size.width = fm.stringWidth(l.getText());
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
