package com.ApkInfo.UIUtil;

/**
 * Copyright (c) 2012, Dhilshuk Reddy All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute SwingJD software is freely
 * granted, provided that this notice is preserved.
*/
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.ButtonModel;
import javax.swing.JButton;


/**
 * 
 * @author Dhilshuk Reddy
 * 
 */
public class StandardButton extends JButton {

	private int buttonTheme = Theme.STANDARD_SILVER_THEME;
	private int selectedButtonTheme = Theme.STANDARD_RED_THEME;
	private int rolloverButtonTheme = Theme.STANDARD_BLACK_THEME;
	private String buttonType = ButtonType.BUTTON_ROUNDED_RECTANGLUR;

	/**
	 * Constructor which takes label of the button as argument.
	 * 
	 * @param text
	 *            label on the button
	 */
	public StandardButton(String text) {
		super();
		setText(text);
		setContentAreaFilled(false);
		setBorderPainted(false);
		setFont(new Font("Thoma", Font.BOLD, 12));
		setForeground(Color.WHITE);
		setFocusable(false);

	}

	/**
	 * Constructor which sets label of the button and the button theme.
	 * 
	 * @param text
	 *            label on the button.
	 * @param buttonTheme
	 *            button theme.
	 */
	public StandardButton(String text, int buttonTheme) {
		super(text);
		this.buttonTheme = buttonTheme;
		init();
	}

	/**
	 * Constructor which sets label of the button,button type and the button
	 * theme.
	 * 
	 * @param text
	 *            label of the button
	 * @param buttonType
	 *            type of the button.
	 * @param buttonTheme
	 */
	public StandardButton(String text, int buttonTheme, String buttonType) {
		super(text);
		this.buttonTheme = buttonTheme;
		this.buttonType = buttonType;
		init();
	}

	/**
	 * Constructor which sets label of the button,button type,the button theme
	 * and the selectedTheme.
	 * 
	 * @param text
	 *            label on the button
	 * @param buttonType
	 *            Shape of the button
	 * @param buttonTheme
	 *            button theme
	 * @param selectedButtonTheme
	 *            selected button theme.
	 */
	public StandardButton(String text, String buttonType, int buttonTheme,
			int selectedButtonTheme) {
		super(text);
		this.buttonType = buttonType;
		this.buttonTheme = buttonTheme;
		init();
	}

	/**
	 * Constructor which sets label of the button,button type,the button theme
	 * and the selected Theme,roll-over theme.
	 * 
	 * @param text
	 *            label on the button
	 * @param buttonType
	 *            Shape of the button
	 * @param buttonTheme
	 *            button theme
	 * @param selectedButtonTheme
	 *            selected button theme
	 * 
	 * @param rolloverButtonTheme
	 *            roll-over button theme
	 */
	public StandardButton(String text, String buttonType, int buttonTheme,
			int selectedButtonTheme, int rolloverButtonTheme) {
		super(text);
		this.buttonType = buttonType;
		this.buttonTheme = buttonTheme;
		this.selectedButtonTheme = selectedButtonTheme;
		this.rolloverButtonTheme = rolloverButtonTheme;
		init();
	}

	/**
	 * Initializes.
	 */
	private void init() {
		setFont(new Font("Thoma", Font.BOLD, 12));
		setFocusable(false);
		setContentAreaFilled(false);
		setBorderPainted(false);
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
				java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		int h = getHeight();
		int w = getWidth();
		Paint color = null;
		ButtonModel model = getModel();
		if (model.isRollover()) {
			color = ColorUtils.getInStance().getStandardColor(
					rolloverButtonTheme, getHeight(), this);
		} else {
			color = ColorUtils.getInStance().getStandardColor(buttonTheme,
					getHeight(), this);

		}
		if (model.isPressed()) {
			color = ColorUtils.getInStance().getStandardColor(
					selectedButtonTheme, getHeight(), this);
		}
		drawShape(g2d, w, h, color);
		super.paintComponent(g);

	}

	/**
	 * Draws the shape.
	 * 
	 * @param g2d
	 *            2d object
	 * @param w
	 *            width of the button
	 * @param h
	 *            height of the button
	 */
	private void drawShape(Graphics2D g2d, int w, int h, Paint color) {
		if (buttonType == ButtonType.BUTTON_ROUNDED_RECTANGLUR) {
			RoundRectangle2D.Float r2d = new RoundRectangle2D.Float(0, 0,
					w - 1, h - 1, 8, 8);
			Shape clip = g2d.getClip();
			g2d.clip(r2d);
			g2d.setPaint(color);
			g2d.fillRoundRect(0, 0, w, h, 8, 8);
			g2d.setClip(clip);
			g2d.setColor(new Color(100, 100, 100));
			g2d.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);
			g2d.setColor(new Color(255, 255, 255, 50));
			g2d.drawRoundRect(1, 1, w - 3, h - 3, 8, 8);
		} else if (buttonType == ButtonType.BUTTON_RECTANGULAR) {
			Rectangle2D.Float r2d = new Rectangle2D.Float(0, 0, w - 1, h - 1);
			Shape clip = g2d.getClip();
			g2d.clip(r2d);
			g2d.setPaint(color);
			g2d.fillRect(0, 0, w, h);
			g2d.setClip(clip);
			g2d.setColor(new Color(100, 100, 100));
			g2d.drawRect(0, 0, w - 1, h - 1);
			g2d.setColor(new Color(255, 255, 255, 50));
			g2d.drawRect(1, 1, w - 3, h - 3);
		} else if (buttonType == ButtonType.BUTTON_ROUNDED) {
			g2d.setPaint(color);
			g2d.fillRoundRect(1, 1, w - 2, h - 2, h - 5, h - 5);
			g2d.setPaint(new Color(100, 100, 100));
			g2d.drawRoundRect(0, 0, w - 1, h - 1, h - 3, h - 3);
			g2d.setColor(new Color(255, 255, 255, 50));
			g2d.drawRoundRect(1, 1, w - 3, h - 3, h - 3, h - 3);
		} else if (buttonType == ButtonType.BUTTON_OVAL) {
			g2d.setPaint(color);
			g2d.fillOval(1, 1, w - 20, h - 2);
			g2d.setPaint(new Color(100, 100, 100));
			g2d.drawOval(0, 0, w - 20, h - 1);
		} else if (buttonType == ButtonType.BUTTON_ELLIPSE) {
			g2d.setPaint(color);
			Shape shape = new Ellipse2D.Double(1, 1, w - 2, h - 2);
			g2d.fill(shape);
			g2d.setPaint(new Color(100, 100, 100));
			shape = new Ellipse2D.Double(0, 0, w - 1, h - 1);
			g2d.draw(shape);
		} else if (buttonType == ButtonType.BUTTON_CIRCULAR) {
			int size = Math.min(getWidth(), getHeight() - 2);
			g2d.setPaint(color);
			g2d.fillOval(2, 2, (size - 2 * 2), (size - 2 * 2));
			g2d.setStroke(new BasicStroke(2));
			g2d.setColor(new Color(100, 100, 100, 100));
			g2d.drawOval(2, 2, (size - 2 * 2), (size - 2 * 2));
		}

	}

	/**
	 * Gets the button theme.
	 * 
	 * @return button theme
	 */
	public int getButtonTheme() {
		return buttonTheme;
	}

	/**
	 * Sets the button theme.
	 * 
	 * @param buttonTheme
	 */
	public void setButtonTheme(int buttonTheme) {
		this.buttonTheme = buttonTheme;
	}

	/**
	 * Gets the selected theme.
	 * 
	 * @return
	 */
	public int getSelectButtonTheme() {
		return selectedButtonTheme;
	}

	/**
	 * Sets the selected theme.
	 * 
	 * @param selectButtonTheme
	 */
	public void setSelectButtonTheme(int selectButtonTheme) {
		this.selectedButtonTheme = selectButtonTheme;
	}

	/**
	 * Returns Roll-Over Theme.
	 * 
	 * @return roll-over theme
	 */
	public int getRolloverButtonTheme() {
		return rolloverButtonTheme;
	}

	/**
	 * Sets Roll-Over Theme
	 * 
	 * @param rolloverButtonTheme
	 */
	public void setRolloverButtonTheme(int rolloverButtonTheme) {
		this.rolloverButtonTheme = rolloverButtonTheme;
	}

	/**
	 * Returns button type.
	 * 
	 * @returns buttonType
	 */
	public String getButtonType() {
		return buttonType;
	}

	/**
	 * Sets Button Type.
	 * 
	 * @param buttonType
	 */
	public void setButtonType(String buttonType) {
		this.buttonType = buttonType;
	}

}