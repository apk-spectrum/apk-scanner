package com.apkscanner.gui.tabpanels;

import java.net.URI;

import javax.swing.Icon;

public interface TreeNodeData {
    public static final int DATA_TYPE_UNKNOWN = 0;
    public static final int DATA_TYPE_TEXT = 1;
    public static final int DATA_TYPE_IMAGE = 2;
    public static final int DATA_TYPE_CERTIFICATION = 3;

    public String getLabel();

    public URI getURI();

    public String getPath();

    public String getExtension();

    public Icon getIcon();

    public int getDataType();

    public Object getData();

    public boolean isFolder();
}
