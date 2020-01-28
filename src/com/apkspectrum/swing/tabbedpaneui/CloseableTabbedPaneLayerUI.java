package com.apkspectrum.swing.tabbedpaneui;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.LayerUI;

/* from  w  ww.jav a  2  s  .  c o  m
 * http://www.java2s.com/Tutorials/Java/Swing_How_to/JTabbedPane/Create_Closeable_JTabbedPane_alignment_of_the_close_button.htm
 */
public class CloseableTabbedPaneLayerUI extends LayerUI<JTabbedPane> {
	private static final long serialVersionUID = -637673246032684669L;

	JPanel p = new JPanel();
	Point pt = new Point(-100, -100);
	JButton button = new JButton("x") {
		private static final long serialVersionUID = -3906672586591216612L;
		@Override
		public Dimension getPreferredSize() {
			return new Dimension(16, 16);
		}
	};

	public CloseableTabbedPaneLayerUI() {
		super();

		button.setBorder(BorderFactory.createEmptyBorder());
		button.setFocusPainted(false);
		button.setBorderPainted(false);
		button.setContentAreaFilled(false);
		button.setRolloverEnabled(false);
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		super.paint(g, c);
		if (c instanceof JLayer == false) {
			return;
		}
		JLayer<?> jlayer = (JLayer<?>) c;
		JTabbedPane tabPane = (JTabbedPane) jlayer.getView();
		for (int i = 0; i < tabPane.getTabCount(); i++) {
			if(!tabPane.getTitleAt(i).endsWith("  ")) continue;
			Rectangle r = getButtonArea(tabPane, i);
			button.setForeground(r.contains(pt) ? Color.RED : Color.BLACK);
			SwingUtilities.paintComponent(g, button, p, r);
		}
	}

	@Override
	public void installUI(JComponent c) {
		super.installUI(c);
		((JLayer<?>) c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK
				| AWTEvent.MOUSE_MOTION_EVENT_MASK);
	}

	@Override
	public void uninstallUI(JComponent c) {
		((JLayer<?>) c).setLayerEventMask(0);
		super.uninstallUI(c);
	}

	@Override
	protected void processMouseEvent(MouseEvent e, JLayer<? extends JTabbedPane> l) {
		if (e.getID() != MouseEvent.MOUSE_CLICKED) {
			return;
		}
		pt.setLocation(e.getPoint());
		JTabbedPane tabbedPane = (JTabbedPane) l.getView();
		int index = tabbedPane.indexAtLocation(pt.x, pt.y);
		if (index >= 0 && tabbedPane.getTitleAt(index).endsWith("  ")) {
			Rectangle r = getButtonArea(tabbedPane, index);
			if (r.contains(pt)) {
				tabbedPane.removeTabAt(index);
			}
		}
		l.getView().repaint();
	}

	@Override
	protected void processMouseMotionEvent(MouseEvent e,
			JLayer<? extends JTabbedPane> l) {
		pt.setLocation(e.getPoint());
		JTabbedPane tabbedPane = (JTabbedPane) l.getView();
		int index = tabbedPane.indexAtLocation(pt.x, pt.y);
		if (index >= 0 && tabbedPane.getTitleAt(index).endsWith("  ")) {
			tabbedPane.repaint(tabbedPane.getBoundsAt(index));
		} else {
			tabbedPane.repaint();
		}
	}

	protected Rectangle getButtonArea(JTabbedPane tabbedPane, int index) {
		Rectangle rect = tabbedPane.getBoundsAt(index);
		Dimension d = button.getPreferredSize();
		int x = rect.x + rect.width - d.width;
		int y = rect.y;
		switch(TabbedPaneUIManager.getUIInfo(tabbedPane).getName()) {
		case "Aqua Bar":
			x += -2;
			y += (rect.height - d.height) / 2;
			break;
		case "Code Warrior":
			x += -5;
			if(tabbedPane.getSelectedIndex() == index) {
				y += (rect.height - d.height) / 2;
			} else {
				y += -2;
			}
			break;
		case "Plastic":
			x += -10;
			y += (rect.height - d.height) / 2;
			break;
		case "MS PowerPoint":
			x += -5;
			y += (rect.height - d.height) / 2;
			break;
		case "Photoshop Palette Windows":
			x += -3;
			y += (rect.height - d.height) / 2;
			break;
		default :
			x += -2;
			y += (rect.height - d.height) / 2;
			break;
		}
		return new Rectangle(x, y, d.width, d.height);
	}
}