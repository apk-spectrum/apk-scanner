package com.apkscanner.plugin;

import java.util.Map;

public interface IUpdateChecker extends IPlugIn {
	public boolean checkNewVersion() throws NetworkException;
	public NetworkException getLastNetworkException();
	public long getPeriod();
	public void setPeriod(long period);
	public long getLastUpdateDate();
	public void setLastUpdateDate(long lastUpdateDate);
	public Map<?,?> getLatestVersionInfo();
	public void setLatestVersionInfo(Map<?, ?> latestVersionInfo);
	public boolean wasPeriodPassed();
}
