package com.apkscanner.gui.tabpanels;

import javax.swing.Icon;

import com.apkspectrum.util.SystemUtil;

public class UserNodeData extends DefaultNodeData implements Cloneable
{
	public final String config;
	protected Object data;

	public UserNodeData(String label, String path, Object data) {
		this(label, path, null, data);
	}

	public UserNodeData(String label, String path, String config, Object data) {
		super(label, null);
		this.path = path;
		this.config = config;
		this.data = data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	@Override
	public String getExtension() {
		return ".xml";
	}

	@Override
	public Icon getIcon() {
		return icon != null ? icon : (icon = SystemUtil.getExtensionIcon(".arsc"));
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
