package com.apkspectrum.data.apkinfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class WidgetInfo
{
	public String name;
	public ResourceInfo[] labels;
	public ResourceInfo[] icons;
	public String width;
	public String height;
	public String size;
	public String tartget;
	public String type;
	public Boolean enabled;
	public String xmlString;
	public String xmlMetaData;
	public String shortcutId;
	public String mapId;

	public Map<String, Entry<String, ResourceInfo[]>> resourceMap;

	public WidgetInfo() {
		this.resourceMap = new HashMap<>();
	}

	public WidgetInfo(Map<String, Entry<String, ResourceInfo[]>> resourceMap) {
		this.resourceMap = resourceMap;
	}
}
