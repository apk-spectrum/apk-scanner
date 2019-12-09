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
	ETC(12),
	LOCAL(13),
	COUNT(14);

	private int type;

	ResourceType(int type) {
		this.type = type;
	}

	public int getInt() {
		return type;
	}

	public String toString() {
		if(this.equals(METAINF))
			return "META-INF";
		else
			return super.toString().toLowerCase();
	}
}