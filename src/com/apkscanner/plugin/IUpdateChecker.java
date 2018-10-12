package com.apkscanner.plugin;

import java.util.Map;

public interface IUpdateChecker extends IPlugIn {
	public static final int TYPE_LAUNCH_OPEN_LINK = 0;
	public static final int TYPE_LAUNCH_DIRECT_UPDATE = 1;
	public static final int TYPE_LAUNCH_DOWNLOAD = 2;

	public boolean checkNewVersion() throws NetworkException;
	public boolean hasNewVersion();
	public NetworkException getLastNetworkException();
	public long getPeriod();
	public void setPeriod(long period);
	public long getLastUpdateDate();
	public void setLastUpdateDate(long lastUpdateDate);
	public Map<?,?> getLatestVersionInfo();
	public void setLatestVersionInfo(Map<?, ?> latestVersionInfo);
	public boolean wasPeriodPassed();
	public String getTargetPackageName();
	public int getLaunchType();
}
