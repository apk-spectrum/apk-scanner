package com.apkscanner.plugin;

public abstract class ITabbedRequest {
	public static final int REQUEST_NONE = 0;
	public static final int REQUEST_VISIBLE = 1;
	public static final int REQUEST_INVISIBLE = 2;
	public static final int REQUEST_ENABLED = 3;
	public static final int REQUEST_DISABLED = 4;
	public static final int REQUEST_SELECTED = 5;

	public boolean onRequestVisible(boolean visible) { return false; };
	public boolean onRequestEnabled(boolean enable) { return false; };
	public boolean onRequestSelected() { return false; };
	public boolean onRequest(int request) {
		switch(request) {
		case REQUEST_VISIBLE:
			return onRequestVisible(true);
		case REQUEST_INVISIBLE:
			return onRequestVisible(false);
		case REQUEST_ENABLED:
			return onRequestEnabled(true);
		case REQUEST_DISABLED:
			return onRequestEnabled(false);
		case REQUEST_SELECTED:
			return onRequestSelected();
		}
		return false;
	}
}