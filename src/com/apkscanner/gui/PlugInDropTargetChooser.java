package com.apkscanner.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;

import com.apkscanner.gui.component.DropTargetChooser;
import com.apkscanner.gui.component.ImageScaler;
import com.apkscanner.plugin.IExternalTool;
import com.apkscanner.plugin.IPlugInEventListener;
import com.apkscanner.plugin.PlugInManager;
import com.apkscanner.resource.Resource;

public class PlugInDropTargetChooser extends DropTargetChooser implements IPlugInEventListener {
	private static final long serialVersionUID = 333800480269221248L;

	public enum DefaultTargetObject {
		DROPED_TARGET_APK_OPEN,
		DROPED_TARGET_NEW_WIN
	}

	private boolean externalToolVisible;

	public PlugInDropTargetChooser(Listener listener) {
		super(listener);

		super.addDropTarget(DefaultTargetObject.DROPED_TARGET_APK_OPEN, Resource.STR_BTN_OPEN.getString(), Resource.STR_APP_NAME.getString(),
				Resource.IMG_APP_ICON.getImageIcon(64,64).getImage(), new Color(0.35546875f , 0.60546875f, 0.83203125f, 0.9f));
		super.addDropTarget(DefaultTargetObject.DROPED_TARGET_NEW_WIN, Resource.STR_MENU_NEW.getString(), Resource.STR_APP_NAME.getString(),
				Resource.IMG_TOOLBAR_MANIFEST.getImageIcon(64,64).getImage(), new Color(0.4375f, 0.67578125f, 0.27734375f, 0.9f));

		PlugInManager.addPlugInEventListener(this);
	}

	@Override
	public void addDropTarget(Object dropTarget, String text, String title, Image image, Color background) {
		addDropTarget(dropTarget, text, title, image, background, externalToolVisible);
	}

	public void setExternalToolsVisible(boolean visible) {
		externalToolVisible = visible;
		for(Component c: this.getComponents()) {
			if(!(c instanceof DropArea)
				|| ((DropArea)c).dropTarget instanceof DefaultTargetObject) continue;
			c.setVisible(visible);
		}
	}

	@Override
	public void onPluginLoaded() {
		for(IExternalTool plugin: PlugInManager.getExternalTool()) {
			if(!plugin.isDiffTool()) continue;
			Image icon = null;
			URL iconUrl = plugin.getIconURL();
			if(iconUrl != null) {
				ImageIcon imageIcon = new ImageIcon(iconUrl);
				if(imageIcon != null) {
					icon = ImageScaler.getScaledImage(imageIcon, 64, 64);
				}
			}
			addDropTarget(plugin, plugin.getLabel(), "plugin", icon, new Color(0.9f,0.7f,0.3f,0.9f));
		}
	}
}
