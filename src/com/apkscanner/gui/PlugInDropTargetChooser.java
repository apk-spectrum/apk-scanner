package com.apkscanner.gui;

import java.awt.Color;
import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;

import com.apkscanner.gui.component.DropTargetChooser;
import com.apkscanner.gui.component.ImageScaler;
import com.apkscanner.plugin.IExternalTool;
import com.apkscanner.plugin.IPlugInEventListener;
import com.apkscanner.plugin.PlugInManager;

public class PlugInDropTargetChooser extends DropTargetChooser implements IPlugInEventListener {
	private static final long serialVersionUID = 333800480269221248L;

	public PlugInDropTargetChooser(Listener listener) {
		super(listener);
		PlugInManager.addPlugInEventListener(this);
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
