package com.apkscanner.gui.tabpanels;

public class WidgetResData extends ResourceObject {

    public WidgetResData(String uriPath) {
        super(uriPath);
    }

    public WidgetResData(String label, String config, String uriPath) {
        super(uriPath);
        this.label = label;
        this.config = config;
    }

    @Override
    public String toString() {
        String str = getLabel();
        if (config != null && !config.isEmpty()) {
            str += " (" + config + ")";
        }
        // if(node != null) {
        // int childCount = node.getChildCount();
        // if (childCount > 0) {
        // str += " (" + childCount + ")";
        // }
        // }
        return str;
    }

}
