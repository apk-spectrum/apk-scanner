package com.apkscanner.gui.tabpanels;

public enum ResourceType
{
	ANIMATOR(0),
	ANIM(1),
	COLOR(2),
	DRAWABLE(3),
	MIPMAP(4),
	LAYOUT(5),
	MENU(6),
	RAW(7),
	VALUES(8),
	XML(9),
	ASSET(10),
	METAINF(11),
	ROOTRES(12),
	ETC(13),
	LOCAL(14),
	DATA(15),
	COUNT(16);

	private int type;

	ResourceType(int type) {
		this.type = type;
	}

	public int getInt() {
		return type;
	}

	public static ResourceType getType(String path) {
		if (path.startsWith("res/animator")) {
			return ANIMATOR;
		} else if (path.startsWith("res/anim")) {
			return ANIM;
		} else if (path.startsWith("res/color")) {
			return COLOR;
		} else if (path.startsWith("res/drawable")) {
			return DRAWABLE;
		} else if (path.startsWith("res/mipmap")) {
			return MIPMAP;
		} else if (path.startsWith("res/layout")) {
			return LAYOUT;
		} else if (path.startsWith("res/menu")) {
			return MENU;
		} else if (path.startsWith("res/raw")) {
			return RAW;
		} else if (path.startsWith("res/values")) {
			return VALUES;
		} else if (path.startsWith("res/xml")) {
			return XML;
		} else if (path.startsWith("assets")) {
			return ASSET;
		} else if(path.startsWith("META-INF")) {
			return METAINF;
		} else if(!path.contains("/")) {
			return ROOTRES;
		} else {
			return ETC;
		}
	}

	public boolean isMultiConfigType() {
		switch(this) {
		case ASSET: case METAINF: case ROOTRES: case ETC:
			return false;
		default:
			return true;
		}
	}

	public String toString() {
		switch(this) {
		case METAINF:
			return "META-INF";
		case ROOTRES:
			return "";
		default:
			return super.toString().toLowerCase();
		}
	}
}