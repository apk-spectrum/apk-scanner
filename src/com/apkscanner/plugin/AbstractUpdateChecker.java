package com.apkscanner.plugin;

import java.util.Date;
import java.util.Map;

import com.apkscanner.plugin.manifest.Component;

public abstract class AbstractUpdateChecker extends AbstractPlugIn implements IUpdateChecker
{
	private static final long PERIOD_ONE_DAY_MS = 3600 * 1000 * 24;

	protected NetworkException lastNetworkException;
	protected long period;
	protected long lastUpdateDate;
	protected Map<?, ?> latestVersionInfo;

	public AbstractUpdateChecker(PlugInPackage pluginPackage, Component component) {
		super(pluginPackage, component);
		if(component.periodDay != null) {
			int day = Integer.parseInt(component.periodDay);
			period = day * PERIOD_ONE_DAY_MS;
		} else {
			period = PERIOD_ONE_DAY_MS;
		}
		lastUpdateDate = 0;
	}

	NetworkException makeNetworkException(Exception e) {
		return lastNetworkException = new NetworkException(e);
	}

	@Override
	public NetworkException getLastNetworkException() {
		return lastNetworkException;
	}

	@Override
	public String getTargetPackageName() {
		return component.targetPackageName;
	}

	@Override
	public long getPeriod() {
		return period;
	}

	@Override
	public void setPeriod(long period) {
		this.period = period;
	}

	@Override
	public long getLastUpdateDate() {
		return lastUpdateDate;
	}

	@Override
	public void setLastUpdateDate(long lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate > 0 ? lastUpdateDate : 0;
	}

	@Override
	public boolean wasPeriodPassed() {
		long curTime = new Date().getTime();
		long elapsedTime = curTime - lastUpdateDate;
		return period <= elapsedTime;
	}

	@Override
	public void setLatestVersionInfo(Map<?, ?> latestVersionInfo) {
		this.latestVersionInfo = latestVersionInfo;
	}

	@Override
	public Map<?, ?> getLatestVersionInfo() {
		return latestVersionInfo;
	}

	@Override
	public Map<String, Object> getChangedProperties() {
		Map<String, Object> data = super.getChangedProperties();
		long orgPeriod = 0;
		if(component.periodDay != null) {
			int day = Integer.parseInt(component.periodDay);
			orgPeriod = day * PERIOD_ONE_DAY_MS;
		} else {
			orgPeriod = PERIOD_ONE_DAY_MS;
		}
		if(orgPeriod != getPeriod()) {
			data.put("period", getPeriod());
		}
		if(getLastUpdateDate() != 0) {
			data.put("lastUpdateDate", getLastUpdateDate());
		}
		if(latestVersionInfo != null && !latestVersionInfo.isEmpty()) {
			data.put("latestVersionInfo", getLatestVersionInfo());
		}
		return data;
	}

	@Override
	public void restoreProperties(Map<?, ?> data) {
		super.restoreProperties(data);
		if(data == null) return;
		if(data.containsKey("period")) {
			setPeriod((long)data.get("period"));
		}
		if(data.containsKey("lastUpdateDate")) {
			setLastUpdateDate((long)data.get("lastUpdateDate"));
		}
		if(data.containsKey("latestVersionInfo")) {
			Object value = data.get("latestVersionInfo");
			if(value instanceof Map<?, ?>) {
				@SuppressWarnings("unchecked")
				Map<String, String> versionInfo = (Map<String, String>) value;
				setLatestVersionInfo(versionInfo);
			}
		}
	}
}
