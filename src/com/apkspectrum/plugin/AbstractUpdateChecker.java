package com.apkspectrum.plugin;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import com.apkscanner.resource.RStr;
import com.apkspectrum.plugin.manifest.Component;
import com.apkspectrum.util.GeneralVersionChecker;

public abstract class AbstractUpdateChecker extends AbstractPlugIn implements IUpdateChecker
{
	private static final long PERIOD_ONE_DAY_MS = 3600 * 1000 * 24;

	protected NetworkException lastNetworkException;
	protected long period;
	protected long lastUpdateDate;
	protected Map<?, ?> latestVersionInfo;

	private int state = STATUS_NO_UPDATED;
	private ArrayList<StateChangeListener> listeners = new ArrayList<>();

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
		setState(STATUS_ERROR_OCCURED);
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
		if(this.period == period) return;
		long oldValue = this.period;
		this.period = period;

		firePropertyChange(PERIOD_PROPERTY, oldValue, period);
	}

	@Override
	public long getLastUpdateDate() {
		return lastUpdateDate;
	}

	@Override
	public void setLastUpdateDate(long lastUpdateDate) {
		long newValue = lastUpdateDate > 0 ? lastUpdateDate : 0;
		if(this.lastUpdateDate == newValue) return;
		long oldValue = this.lastUpdateDate; 
		this.lastUpdateDate = newValue;

		firePropertyChange(LAST_UPDATE_CHECKED_PROPERTY, oldValue, newValue);
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
	public boolean hasNewVersion() {
		if(latestVersionInfo == null) return false;

		String version = (String)latestVersionInfo.get("version");
		String targetPackageName = getTargetPackageName();
		if("com.apkscanner".equals(targetPackageName)) {
			GeneralVersionChecker newVer = GeneralVersionChecker.parseFrom(version);
			GeneralVersionChecker oldVer = GeneralVersionChecker.parseFrom(RStr.APP_VERSION.get());
			return newVer.compareTo(oldVer) > 0;
		} else if ("com.android.sdk".equals(targetPackageName)) {
			return false;
		} else {
			PlugInPackage targetPackage = PlugInManager.getPlugInPackage(targetPackageName);
			if(targetPackage == null) return false;
			int curVer = targetPackage.getVersionCode();
			int newVer = Integer.parseInt(version);
			return newVer > curVer;
		}
	}

	@Override
	public int getLaunchType() {
		return this instanceof UpdateCheckerLinker ? TYPE_LAUNCH_OPEN_LINK : TYPE_LAUNCH_DIRECT_UPDATE;
	}

	@Override
	public int getState() {
		return state;
	}

	protected void setState(int state) {
		synchronized(listeners) {
			if(this.state == state) return;
			this.state = state;
			for(StateChangeListener listener: listeners) {
				listener.stateChanged(this, state);
			}
		}
	}

	@Override
	public void addStateChangeListener(StateChangeListener listener) {
		synchronized(listeners) {
			if(!listeners.contains(listener)) {
				listeners.add(listener);
			}
		}
	}

	@Override
	public void removeStateChangeListener(StateChangeListener listener) {
		synchronized(listeners) {
			if(listeners.contains(listener)) {
				listeners.remove(listener);
			}
		}
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
