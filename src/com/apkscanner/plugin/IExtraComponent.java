package com.apkscanner.plugin;

import java.awt.Component;

public interface IExtraComponent extends IPlugIn {
	public static final int STATUS_REQUEST_NONE = 0;
	public static final int STATUS_REQUEST_VISIBLE = 1;
	public static final int STATUS_REQUEST_UNVISIBLE = 2;

	public interface IRequestListener {
		public void onRequestVisible(boolean visible);
	};

	public Component getComponent();
	public Component initailizeComponent();

	public boolean isVisibleRequested();
	public void addStateChangedListener(IRequestListener listener);
	public void removeStateChangedListener(IRequestListener listener);
}
