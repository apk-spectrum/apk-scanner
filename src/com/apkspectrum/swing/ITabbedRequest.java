package com.apkspectrum.swing;

public interface ITabbedRequest {
	public static final int REQUEST_NONE = 0;
	public static final int REQUEST_VISIBLE = 1;
	public static final int REQUEST_INVISIBLE = 2;
	public static final int REQUEST_ENABLED = 3;
	public static final int REQUEST_DISABLED = 4;
	public static final int REQUEST_SELECTED = 5;
	public static final int REQUEST_CHANGE_TITLE = 6;

	public boolean onRequestVisible(boolean visible);
	public boolean onRequestEnabled(boolean enable);
	public boolean onRequestSelected();
	public boolean onRequestChangeTitle();
	public boolean onRequest(int request);
}