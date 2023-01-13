package com.apkscanner.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;

import com.apkscanner.resource.RImg;
import com.apkscanner.resource.RStr;
import com.apkspectrum.plugin.ExternalTool;
import com.apkspectrum.plugin.PlugInEventAdapter;
import com.apkspectrum.plugin.PlugInManager;
import com.apkspectrum.swing.DropTargetChooser;
import com.apkspectrum.swing.ImageScaler;

public class PlugInDropTargetChooser extends DropTargetChooser {
    private static final long serialVersionUID = 333800480269221248L;

    public enum DefaultTargetObject {
        DROPED_TARGET_APK_OPEN, DROPED_TARGET_NEW_WIN
    }

    private boolean externalToolVisible;

    public PlugInDropTargetChooser(Listener listener) {
        super(listener);

        super.addDropTarget(DefaultTargetObject.DROPED_TARGET_APK_OPEN, RStr.BTN_OPEN.get(),
                RStr.APP_NAME.get(), RImg.APP_ICON.getImage(64, 64),
                new Color(0.35546875f, 0.60546875f, 0.83203125f, 0.9f));
        super.addDropTarget(DefaultTargetObject.DROPED_TARGET_NEW_WIN, RStr.MENU_NEW.get(),
                RStr.APP_NAME.get(), RImg.TOOLBAR_MANIFEST.getImage(64, 64),
                new Color(0.4375f, 0.67578125f, 0.27734375f, 0.9f));

        PlugInManager.addPlugInEventListener(new PlugInEventAdapter() {
            @Override
            public void onPluginLoaded() {
                for (ExternalTool plugin : PlugInManager.getExternalTool()) {
                    if (!plugin.isDiffTool()) continue;
                    Image icon = null;
                    URL iconUrl = plugin.getIconURL();
                    if (iconUrl != null) {
                        ImageIcon imageIcon = new ImageIcon(iconUrl);
                        if (imageIcon != null) {
                            icon = ImageScaler.getScaledImage(imageIcon, 64, 64);
                        }
                    }
                    addDropTarget(plugin, plugin.getLabel(), "plugin", icon,
                            new Color(0.9f, 0.7f, 0.3f, 0.9f));
                }
            }
        });
    }

    @Override
    public void addDropTarget(Object dropTarget, String text, String title, Image image,
            Color background) {
        addDropTarget(dropTarget, text, title, image, background, externalToolVisible);
    }

    public void setExternalToolsVisible(boolean visible) {
        externalToolVisible = visible;
        for (Component c : this.getComponents()) {
            if (!(c instanceof DropArea)
                    || ((DropArea) c).dropTarget instanceof DefaultTargetObject) {
                continue;
            }
            c.setVisible(visible);
        }
    }
}
