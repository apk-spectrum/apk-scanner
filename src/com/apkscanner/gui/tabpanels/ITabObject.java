package com.apkscanner.gui.tabpanels;

import java.awt.Component;

import javax.swing.Icon;

import com.apkscanner.core.scanner.ApkScanner.Status;
import com.apkscanner.data.apkinfo.ApkInfo;

public abstract interface ITabObject
{
	public static final int REQUEST_NONE = 0;
	public static final int REQUEST_VISIBLE = 1;
	public static final int REQUEST_INVISIBLE = 2;
	public static final int REQUEST_ENABLED = 3;
	public static final int REQUEST_DISABLED = 4;
	public static final int REQUEST_SELECTED = 5;

	public abstract class ITabbedRequest {
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
	};

	public Component getComponent();

	public void initialize();
	public void reloadResource();

	public String getTitle();
	public String getToolTip();
	public Icon getIcon();

	public void setData(ApkInfo apkInfo, Status status, ITabbedRequest request);
	public void clearData();
}