package com.apkscanner.gui.tabpanels;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.print.attribute.UnmodifiableSetException;
import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

import com.apkscanner.resource.RImg;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResourceTreeIcons {
    public static final String FOLDER_ICON = "FOLDER";
    public static final String LOADING_ICON = "LOADING";
    public static final String PROCESSING_ICON = "PROCESSING";

    private static ResourceTreeIcons instance;

    private final HashMap<String, Icon> cacheIcon = new HashMap<String, Icon>();

    public static ResourceTreeIcons getDefaultSet() {
        if (instance == null) instance = new ResourceTreeIcons();
        return instance;
    }

    public void setIcon(String suffix, Icon icon) {
        if (this == instance) {
            throw new UnmodifiableSetException("The default icon set cannot be changed.");
        }
        cacheIcon.put(suffix, icon);
    }

    public Icon getIcon(@NonNull String suffix) {
        Icon icon = cacheIcon.get(suffix);
        if (icon != null) return icon;
        boolean useCached = true;

        switch (suffix) {
            case FOLDER_ICON:
                icon = RImg.TREE_FOLDER.getImageIcon();
                /*
                 * UIDefaults defaults = UIManager.getDefaults();
                 * Icon computerIcon = defaults.getIcon("FileView.computerIcon");
                 * Icon floppyIcon = defaults.getIcon("FileView.floppyDriveIcon");
                 * Icon diskIcon = defaults.getIcon("FileView.hardDriveIcon");
                 * Icon fileIcon = defaults.getIcon("FileView.fileIcon");
                 * Icon folderIcon = defaults.getIcon("FileView.directoryIcon");
                 *
                 * icon = folderIcon;
                 */
                break;
            case LOADING_ICON:
                icon = RImg.TREE_LOADING.getImageIcon();
                useCached = false;
                break;
            case PROCESSING_ICON:
                icon = RImg.RESOURCE_TREE_OPEN_JD.getImageIcon();
                useCached = false;
                break;
            case ".xml":
                icon = RImg.RESOURCE_TREE_XML.getImageIcon();
                break;
            case ".dex":
                icon = RImg.RESOURCE_TREE_CODE.getImageIcon();
                break;
            case ".arsc":
                icon = RImg.RESOURCE_TREE_ARSC.getImageIcon();
                break;
            default:
                try {
                    File file = File.createTempFile("icon", suffix);
                    FileSystemView view = FileSystemView.getFileSystemView();
                    icon = view.getSystemIcon(file);
                    file.delete();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
                break;
        }
        if (useCached && icon != null) {
            cacheIcon.put(suffix, icon);
        }
        return icon;
    }
}
