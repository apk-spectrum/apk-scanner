package com.apkscanner.gui.tabpanels;

public class UserNodeData extends DefaultNodeData {
    public final String config;
    protected Object data;

    public UserNodeData(String label, String path, Object data) {
        this(label, path, null, data);
    }

    public UserNodeData(String label, String path, String config, Object data) {
        super(label);
        this.config = config;
        this.data = data;
        setIcon(ResourceTree.getExtensionIcon(".arsc"));
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String getExtension() {
        return ".xml";
    }

    @Override
    public int getDataType() {
        return DATA_TYPE_TEXT;
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        String str = getLabel();
        if (config != null && !config.isEmpty()) {
            str += " (" + config + ")";
        }
        return str;
    }
}
