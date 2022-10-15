package com.apkscanner.gui.easymode.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JSlider;
import javax.swing.Painter;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


public class CustomSlider extends JSlider {
	private static final long serialVersionUID = 4913084976607687539L;
	UIDefaults d;

	public CustomSlider() {
		
		String systemlook = UIManager.getSystemLookAndFeelClassName();
		
		try {
			for (UIManager.LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(laf.getName())) {
					UIManager.setLookAndFeel(laf.getClassName());					
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		d = new UIDefaults();
		d.put("Slider:SliderTrack[Enabled].backgroundPainter", new Painter<JSlider>() {
			@Override
			public void paint(Graphics2D g, JSlider c, int w, int h) {
				int arc = 10;
				int trackHeight = 8;
				int trackWidth = w - 2;
				int fillTop = 4;
				int fillLeft = 1;

				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setStroke(new BasicStroke(1.5f));
				g.setColor(Color.GRAY);
				g.fillRoundRect(fillLeft, fillTop, trackWidth, trackHeight, arc, arc);

				int fillBottom = fillTop + trackHeight;
				int fillRight = xPositionForValue(c.getValue(), c,
						new Rectangle(fillLeft, fillTop, trackWidth, fillBottom - fillTop));

				g.setColor(Color.ORANGE);
				g.fillRect(fillLeft + 1, fillTop + 1, fillRight - fillLeft, fillBottom - fillTop);

				g.setColor(Color.WHITE);
				g.drawRoundRect(fillLeft, fillTop, trackWidth, trackHeight, arc, arc);
			}

			// @see javax/swing/plaf/basic/BasicSliderUI#xPositionForValue(int
			// value)
			protected int xPositionForValue(int value, JSlider slider, Rectangle trackRect) {
				int min = slider.getMinimum();
				int max = slider.getMaximum();
				int trackLength = trackRect.width;
				double valueRange = (double) max - (double) min;
				double pixelsPerValue = (double) trackLength / valueRange;
				int trackLeft = trackRect.x;
				int trackRight = trackRect.x + (trackRect.width - 1);
				int xPosition;

				xPosition = trackLeft;
				xPosition += Math.round(pixelsPerValue * ((double) value - min));

				xPosition = Math.max(trackLeft, xPosition);
				xPosition = Math.min(trackRight, xPosition);

				return xPosition;
			}
		});
		//this.putClientProperty("Nimbus.Overrides", d);
		
		//UIDefaults defaults = UIManager.getLookAndFeelDefaults();
		
//		Log.d("" + defaults);
//		this.putClientProperty("Nimbus.Overrides", defaults);
//		this.putClientProperty("Nimbus.Overrides.InheritDefaults", false);
		setPreferredSize(new Dimension(50, 15));
		
		this.updateUI();
		try {
			UIManager.setLookAndFeel(systemlook);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}

}
