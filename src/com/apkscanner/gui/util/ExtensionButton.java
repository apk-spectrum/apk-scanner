package com.apkscanner.gui.util;

// http://www.java2s.com/Tutorials/Java/Swing_How_to/JButton/Extend_JButton_to_create_arrow_button.htm

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

public class ExtensionButton extends JButton {

	private static final long serialVersionUID = -5922951340712796431L;

	/** The cardinal direction of the arrow(s). */
	private int direction=0;

	/** The number of arrows. */
	private int arrowCount=0;

	/** The arrow size. */
	private int arrowSize=0;

	private boolean arrowVisible = false;

	private int badgeCount=0;

	/*
  	public ExtensionButton(int direction, int arrowCount, int arrowSize) {
		setMargin(new Insets(0, 2, 0, 2));
		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		this.direction = direction;
		this.arrowCount = arrowCount;
		this.arrowSize = arrowSize;
  	}
	*/

	public ExtensionButton() {
		super();
	}

	public ExtensionButton(String text, ImageIcon icon) {
		super(text, icon);
	}

	public void setArrowVisible(boolean visible) {
		arrowVisible = visible;
		setMargin(new Insets(0, 1, 0, visible && isEnabled() ? arrowSize*arrowCount + 3 : 0));
	}

	public boolean getArrowVisible() {
		return arrowVisible;
	}

	public void setArrowStyle(int direction, int arrowCount, int arrowSize) {
		this.direction = direction;
		this.arrowCount = arrowCount;
		this.arrowSize = arrowSize;
		if(arrowVisible && isEnabled()) {
			Insets l = new Insets(0, 0, 0, arrowSize*arrowCount + 3);
			setMargin(l);
		}
	}

	/**
	 * Returns the cardinal direction of the arrow(s).
	 *
	 * @see #setDirection(int)
	 */
	public int getDirection() {
		return direction;
	}

	/**
	 * Sets the cardinal direction of the arrow(s).
	 *
	 * @param direction
	 *		  the direction of the arrow(s), can be SwingConstants.NORTH,
	 *		  SwingConstants.SOUTH, SwingConstants.WEST or SwingConstants.EAST
	 * @see #getDirection()
	 */
	public void setDirection(int direction) {
		this.direction = direction;
	}

	/** Returns the number of arrows. */
	public int getArrowCount() {
		return arrowCount;
	}

	/** Sets the number of arrows. */
	public void setArrowCount(int arrowCount) {
		this.arrowCount = arrowCount;
	}

	/** Returns the arrow size. */
	public int getArrowSize() {
		return arrowSize;
	}

	/** Sets the arrow size. */
	public void setArrowSize(int arrowSize) {
		this.arrowSize = arrowSize;
	}

	public void setBadge(int count) {
		this.badgeCount = count;
	}

	public int getBadge() {
		return this.badgeCount;
	}

	public void incrementBadge(int increment) {
		this.badgeCount += increment;
	}

	public void clearBadge() {
		this.badgeCount = 0;
	}

	/*
  	public Dimension getPreferredSize() {
		return getMinimumSize();
  	}

  	public Dimension getMinimumSize() {
		return new Dimension(
			arrowSize
			* (direction == SwingConstants.EAST
				|| direction == SwingConstants.WEST ? arrowCount : 3)
			+ getBorder().getBorderInsets(this).left
			+ getBorder().getBorderInsets(this).right, arrowSize
			* (direction == SwingConstants.NORTH
				|| direction == SwingConstants.SOUTH ? arrowCount : 3)
			+ getBorder().getBorderInsets(this).top
			+ getBorder().getBorderInsets(this).bottom);
  	}

  	public Dimension getMaximumSize() {
		return getMinimumSize();
  	}
	*/

	protected void paintComponent(Graphics g) {
		// this will paint the background
		super.paintComponent(g);

		if(!isEnabled()) return;

		Color oldColor = g.getColor();
		//g.setColor(isEnabled() ? getForeground() : getForeground().brighter());
		//if(!isEnabled()) g.setColor(Color.GRAY);

		// paint the arrows
		int w = getSize().width;
		int h = getSize().height;

		if(arrowVisible) {
			for (int i = 0; i < arrowCount; i++) {
				paintArrow(g,
					(w - 5 - arrowSize * (direction == SwingConstants.EAST || direction == SwingConstants.WEST ? arrowCount : 1)) // / 2
						+ arrowSize * (direction == SwingConstants.EAST || direction == SwingConstants.WEST ? i : 0),
					(h - arrowSize * (direction == SwingConstants.EAST || direction == SwingConstants.WEST ? 1 : arrowCount)) / 2
						+ arrowSize * (direction == SwingConstants.EAST || direction == SwingConstants.WEST ? 0 : i) + 1,
					g.getColor()
				);
			}
		}
		if(badgeCount > 0) {
			int size = ((w < h ? w : h) / 4);
			if(size > 15) size = 15;
			else if(size < 8) size = 8;
			if(size > w || size > h) size = (w < h ? w : h);
			g.setColor(new Color(237, 125, 49));
			g.fillOval(w-size-5, 5, size, size);
			if(size > 12) {
				Map<?, ?> desktopHints = (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
				Graphics2D g2 = (Graphics2D) g;
				if (desktopHints != null) {
				    g2.setRenderingHints(desktopHints);
				} else {
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				}
				String badge = badgeCount < 10 ? Integer.toString(badgeCount) : "!";
				Font font = UIManager.getFont("Label.font");
				g.setFont(font.deriveFont((float)size-3));
				FontMetrics fm = g.getFontMetrics();
				int bw = fm.stringWidth(badge);
				int bh = fm.getAscent();
				g.setColor(Color.WHITE);
				g.drawString(badge, w-(size+bw)/2-5, (size+bh)/2 + 4);
			}
		}

		g.setColor(oldColor);
	}

	private void paintArrow(Graphics g, int x, int y, Color highlight) {
		int mid, i, j;

		Color oldColor = g.getColor();
		boolean isEnabled = true;//isEnabled();

		j = 0;
		arrowSize = Math.max(arrowSize, 2);
		mid = (arrowSize / 2) - 1;

		g.translate(x, y);

		switch (direction) {
		case NORTH:
			for (i = 0; i < arrowSize; i++) {
				g.drawLine(mid - i, i, mid + i, i);
			}
			if (!isEnabled) {
				g.setColor(highlight);
				g.drawLine(mid - i + 2, i, mid + i, i);
			}
			break;
		case SOUTH:
			if (!isEnabled) {
				g.translate(1, 1);
				g.setColor(highlight);
				for (i = arrowSize - 1; i >= 0; i--) {
					g.drawLine(mid - i, j, mid + i, j);
					j++;
				}
				g.translate(-1, -1);
				g.setColor(oldColor);
			}
			j = 0;
			for (i = arrowSize - 1; i >= 0; i--) {
				g.drawLine(mid - i, j, mid + i, j);
				j++;
			}
			break;
		case WEST:
			for (i = 0; i < arrowSize; i++) {
				g.drawLine(i, mid - i, i, mid + i);
			}
			if (!isEnabled) {
				g.setColor(highlight);
				g.drawLine(i, mid - i + 2, i, mid + i);
			}
			break;
		case EAST:
			if (!isEnabled) {
				g.translate(1, 1);
				g.setColor(highlight);
				for (i = arrowSize - 1; i >= 0; i--) {
					g.drawLine(j, mid - i, j, mid + i);
					j++;
				}
				g.translate(-1, -1);
				g.setColor(oldColor);
			}
			j = 0;
			for (i = arrowSize - 1; i >= 0; i--) {
				g.drawLine(j, mid - i, j, mid + i);
				j++;
			}
			break;
		}

		g.translate(-x, -y);
		g.setColor(oldColor);
	}
}
