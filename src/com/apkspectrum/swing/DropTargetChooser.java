package com.apkspectrum.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.io.File;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.UIManager;

import com.apkspectrum.util.Log;

public class DropTargetChooser extends JComponent {
	private static final long serialVersionUID = 4972112842742444927L;

	protected boolean visible;
	protected Listener listener;
	protected FileDrop fileDropHandler;

	protected class DropArea extends JComponent implements FileDrop.Listener {
		private static final long serialVersionUID = -5908315150932591478L;

		public final Font font;
		public final Image image;
		public final String text;
		public final String title;
		public final Color background;
		public final Object dropTarget;

		private boolean selected;

		public DropArea(Object dropTarget, String text, String title, Image image, Color background) {
			this.dropTarget = dropTarget;
			this.text = text;
			this.image = image;
			this.title = title;
			this.background = background;
			this.font = UIManager.getFont("Label.font");
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if(!visible) return;
			Map<?, ?> desktopHints = (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
			Graphics2D g2 = (Graphics2D) g;
			if (desktopHints != null) {
			    g2.setRenderingHints(desktopHints);
			} else {
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			}
			g.setColor(selected ? background : background.darker());
	        g.fillRect(0, 0, getWidth(), getHeight());
	        g.setColor(Color.BLACK);
	        if(font != null) g.setFont(font.deriveFont(15f));
    		FontMetrics fm = g.getFontMetrics();
        	int w = getWidth();
        	int h = getHeight();
	        if(image != null) {
	        	int x = (w - image.getWidth(null)) / 2;
	        	int y = (h - image.getHeight(null)) / 2;
	        	g.drawImage(image, x, y, null);
	        	if(text != null) {
	    			x = (w - fm.stringWidth(text)) / 2;
	    			y = y + image.getHeight(null) + fm.getHeight();
	        		g.drawString(text, x, y);
	        	}
	        } else if(text != null) {
    			int x = (w - fm.stringWidth(text)) / 2;
    			int y = (fm.getAscent() + (h - (fm.getAscent() + fm.getDescent())) / 2);
	        	g.drawString(text, x, y);
	        }
	        if(title != null) {
	        	g.drawString(title, 5, fm.getHeight());
	        }
		}

		@Override
		public void dragEnter() {
			visible = true;
			selected = true;
		}

		@Override
		public void dragExit() {
			visible = false;
			selected = false;
		}

		// Drag & Drop event processing
		@Override
		public void filesDropped(final File[] files)
		{
			Log.i("filesDropped() " + text);
			if(listener != null) listener.filesDropped(dropTarget, files);
		}
	}

	public DropTargetChooser(Listener listener) {
		setEnabled(false);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		fileDropHandler = new FileDrop(this, null);

		this.listener = listener;
	}

	public void addDropTarget(Object dropTarget, String text, String title, Image image, Color background) {
		addDropTarget(dropTarget, text, title, image, background, true);
	}

	protected void addDropTarget(Object dropTarget, String text, String title, Image image, Color background, boolean visible) {
		JComponent component = new DropArea(dropTarget, text, title, image, background);
		add(component);
		if(fileDropHandler != null) fileDropHandler.makeDropTarget(null, component, false);
		component.setVisible(visible);
	}

    public static interface Listener {
        public abstract void filesDropped(Object dropedTarget, java.io.File[] files);
    }
}
