package com.apkscanner.gui;

import java.awt.Color;
import java.awt.Component;
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

import com.apkscanner.gui.util.FileDrop;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class DropTargetChooser extends JComponent {
	private static final long serialVersionUID = 4972112842742444927L;

	enum DefaultTargetObject {
		DROPED_TARGET_APK_OPEN,
		DROPED_TARGET_NEW_WIN
	}

	private boolean visible;
	private boolean externalToolVisible;
	private Listener listener;
	private FileDrop fileDropHandler;

	class DropArea extends JComponent implements FileDrop.Listener {
		private static final long serialVersionUID = -5908315150932591478L;

		private Font font;
		private Image image;
		private String text;
		private String title;
		private Color background;
		private Object dropTarget;
		boolean selected;

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
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		addDropTarget(DefaultTargetObject.DROPED_TARGET_APK_OPEN, Resource.STR_BTN_OPEN.getString(), Resource.STR_APP_NAME.getString(),
				Resource.IMG_APP_ICON.getImageIcon(64,64).getImage(), new Color(0.35546875f , 0.60546875f, 0.83203125f, 0.9f));
		addDropTarget(DefaultTargetObject.DROPED_TARGET_NEW_WIN, Resource.STR_MENU_NEW.getString(), Resource.STR_APP_NAME.getString(),
				Resource.IMG_TOOLBAR_MANIFEST.getImageIcon(64,64).getImage(), new Color(0.4375f, 0.67578125f, 0.27734375f, 0.9f));

		fileDropHandler = new FileDrop(this, null);

		this.listener = listener;
	}

	public void addDropTarget(Object dropTarget, String text, String title, Image image, Color background) {
		JComponent component = new DropArea(dropTarget, text, title, image, background);
		add(component);
		if(fileDropHandler != null) fileDropHandler.makeDropTarget(null, component, false);
		if(!(dropTarget instanceof DefaultTargetObject)) component.setVisible(externalToolVisible);
	}

	public void setExternalToolsVisible(boolean visible) {
		externalToolVisible = visible;
		for(Component c: this.getComponents()) {
			if(!(c instanceof DropArea)) continue;
			if(((DropArea)c).dropTarget instanceof DefaultTargetObject) continue;
			c.setVisible(visible);
		}
	}

    public static interface Listener {
        public abstract void filesDropped(Object dropedTarget, java.io.File[] files);
    }
}
