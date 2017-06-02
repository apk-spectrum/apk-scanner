package com.apkscanner.core.installer;

import java.util.ArrayList;

import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class OptionsBundle {
	public static final int FLAG_OPT_INSTALL	 	= 0x010000;
	public static final int FLAG_OPT_PUSH			= 0x020000;
	public static final int FLAG_OPT_NOT_INSTALL	= 0x030000;

	public static final int FLAG_OPT_INSTALL_FORWARD_LOCK	= 0x0001;
	public static final int FLAG_OPT_INSTALL_REPLACE		= 0x0002;
	public static final int FLAG_OPT_INSTALL_TEST_PACKAGE	= 0x0004;
	public static final int FLAG_OPT_INSTALL_ON_SDCARD		= 0x0008;
	public static final int FLAG_OPT_INSTALL_DOWNGRADE		= 0x0010;
	public static final int FLAG_OPT_INSTALL_GRANT_PERM		= 0x0020;
	public static final int FLAG_OPT_INSTALL_LAUNCH			= 0x0040;

	public static final int FLAG_OPT_PUSH_SYSTEM	= 0x0100;
	public static final int FLAG_OPT_PUSH_PRIVAPP	= 0x0200;
	public static final int FLAG_OPT_PUSH_LIB32		= 0x0400;
	public static final int FLAG_OPT_PUSH_LIB64		= 0x0800;
	public static final int FLAG_OPT_PUSH_LIB_BOTH	= 0x1000;
	public static final int FLAG_OPT_PUSH_REBOOT	= 0x4000;

	public static final int FLAG_OPT_DISSEMINATE 	= 0x400000;
	public static final int FLAG_OPT_CLEAR_OPTIONS	= 0x800000;

	public static final int FLAG_OPT_HAS_EXTRADATA_MASK = FLAG_OPT_INSTALL_LAUNCH | FLAG_OPT_PUSH_LIB32 | FLAG_OPT_PUSH_LIB64;

	private int flag;
	private int blockedFlags; 

	public String launchActivity;
	public String systemPath;
	public String lib32Arch;
	public String lib32ToPath;
	public String lib64Arch;
	public String lib64ToPath;

	public interface IOptionsChangedListener {
		void changeOptions(int changedFlag, String... extraData);
	}

	private final ArrayList<IOptionsChangedListener> sChangedListeners =
			new ArrayList<IOptionsChangedListener>();
	private final Object sLock = sChangedListeners;

	public OptionsBundle() {
		this(null);
	}

	public OptionsBundle(IOptionsChangedListener listener) {
		clear();
		addOptionsChangedListener(listener);
	}

	public synchronized boolean copyFrom(OptionsBundle bundle) {
		if((blockedFlags & bundle.flag) != 0) {
			return false; 
		}

		flag = bundle.flag;
		launchActivity = bundle.launchActivity;
		systemPath = bundle.systemPath;
		lib32Arch = bundle.lib32Arch;
		lib32ToPath = bundle.lib32ToPath;
		lib64Arch = bundle.lib64Arch;

		/*
		launchActivity = bundle.launchActivity != null ? new String(bundle.launchActivity) : null;
		path = bundle.path != null ? new String(bundle.path) : null;
		lib32Arch = bundle.lib32Arch != null ? new String(bundle.lib32Arch) : null;
		lib32ToPath = bundle.lib32ToPath != null ? new String(bundle.lib32ToPath) : null;
		lib64Arch = bundle.lib64Arch != null ? new String(bundle.lib64Arch) : null;
		 */
		return true;
	}

	public synchronized boolean copyTo(OptionsBundle bundle) {
		return bundle.copyFrom(this);
	}

	public synchronized void setBlockedFlags(int blockedFlags) {
		this.blockedFlags = blockedFlags;
		flag &= ~blockedFlags;
	}

	public synchronized boolean isBlockedFlags(int flags) {
		return (blockedFlags & flags) == flags;
	}

	public synchronized void set(int flag) {
		set(flag, (String[])null);
	}

	public synchronized void set(int flag, String... extraData) {
		if((flag & blockedFlags) != 0) {
			Log.w(String.format("flag(0x%x) is blocked(0x%x)", flag, blockedFlags));
			flag &= ~blockedFlags;
		}
		if(flag == 0 || Integer.bitCount(flag) > 1) {
			//Log.v("Can be set flag to only one bit at once " + Integer.bitCount(flag) + ", " + Integer.toHexString(flag));
			//return;
		}

		if((flag & FLAG_OPT_HAS_EXTRADATA_MASK) != 0 && extraData == null) {
			Log.w("This flag is need extra Data, but extraData is null : 0x"  + Integer.toHexString(flag));
		}

		boolean isChangeExtraData = false;
		if(extraData != null && extraData.length >= 1) {
			switch(flag) {
			case FLAG_OPT_INSTALL_LAUNCH:
				if(launchActivity != extraData[0]) {
					isChangeExtraData = true;
					launchActivity = extraData[0];
				}
				break;
				/*
			case FLAG_OPT_PUSH:
				if(systemPath != extraData[0]) {
					isChangeExtraData = true;
					systemPath = extraData[0];
				}
				break;
				 */
			case FLAG_OPT_PUSH_LIB32:
				if(extraData.length >= 2
				&& (lib32Arch != extraData[0] || lib32ToPath != extraData[1])) {
					isChangeExtraData = true;
					lib32Arch = extraData[0];
					lib32ToPath = extraData[1];
				}
				break;
			case FLAG_OPT_PUSH_LIB64:
				if(extraData.length >= 2
				&& (lib64Arch != extraData[0] || lib64ToPath != extraData[1])) {
					isChangeExtraData = true;
					lib64Arch = extraData[0];
					lib64ToPath = extraData[1];
				}
				break;
			}
		}

		switch(flag) {
		case FLAG_OPT_INSTALL:
		case FLAG_OPT_PUSH:
		case FLAG_OPT_NOT_INSTALL:
			this.flag &= ~FLAG_OPT_NOT_INSTALL;
			break;
		}

		int pre = this.flag;
		this.flag |= flag;

		if(pre != this.flag || isChangeExtraData) {
			int changeFlag = pre ^ this.flag;
			optionsChanged(changeFlag, extraData);
		}
	}

	public synchronized void unset(int flag) {
		if(flag == 0 || Integer.bitCount(flag) > 1) {
			Log.e("Can be unset flag to only one bit at once");
			return;
		}
		int pre = this.flag;
		this.flag &= ~flag;
		if(pre != this.flag) {
			optionsChanged(pre ^ this.flag, (String[])null);
		}
	}

	public synchronized int get() {
		return flag;
	}

	public synchronized void clear() {
		flag = FLAG_OPT_INSTALL | FLAG_OPT_INSTALL_REPLACE | FLAG_OPT_INSTALL_DOWNGRADE;
		if((boolean)Resource.PROP_LAUNCH_AF_INSTALLED.getData()) {
			flag |= FLAG_OPT_INSTALL_LAUNCH; 
		}

		flag |= FLAG_OPT_PUSH_SYSTEM | FLAG_OPT_PUSH_REBOOT | FLAG_OPT_PUSH_LIB_BOTH;
		optionsChanged(FLAG_OPT_CLEAR_OPTIONS, (String[])null);
	}

	public synchronized boolean isSet(int flag) {
		return (this.flag & flag) == flag;
	}

	public synchronized boolean isInstallOptions() {
		return (flag & (FLAG_OPT_INSTALL | FLAG_OPT_PUSH)) == FLAG_OPT_INSTALL;
	}

	public synchronized boolean isPushOptions() {
		return (flag & (FLAG_OPT_INSTALL | FLAG_OPT_PUSH)) == FLAG_OPT_PUSH;
	}

	public synchronized boolean isNoInstallOptions() {
		return (flag & FLAG_OPT_NOT_INSTALL) == FLAG_OPT_NOT_INSTALL;
	}

	public synchronized boolean isDontInstallOptions() {
		return (flag & FLAG_OPT_NOT_INSTALL) == 0;
	}

	public synchronized boolean isSetForwardLock() {
		return (flag & FLAG_OPT_INSTALL_FORWARD_LOCK) == FLAG_OPT_INSTALL_FORWARD_LOCK;
	}

	public synchronized boolean isSetReplace() {
		return (flag & FLAG_OPT_INSTALL_REPLACE) == FLAG_OPT_INSTALL_REPLACE;
	}

	public synchronized boolean isSetAllowTestPackage() {
		return (flag & FLAG_OPT_INSTALL_TEST_PACKAGE) == FLAG_OPT_INSTALL_TEST_PACKAGE;
	}

	public synchronized boolean isSetOnSdcard() {
		return (flag & FLAG_OPT_INSTALL_ON_SDCARD) == FLAG_OPT_INSTALL_ON_SDCARD;
	}

	public synchronized boolean isSetDowngrade() {
		return (flag & FLAG_OPT_INSTALL_DOWNGRADE) == FLAG_OPT_INSTALL_DOWNGRADE;
	}

	public synchronized boolean isSetGrantPermissions() {
		return (flag & FLAG_OPT_INSTALL_GRANT_PERM) == FLAG_OPT_INSTALL_GRANT_PERM;
	}

	public synchronized boolean isSetLaunch() {
		return (flag & FLAG_OPT_INSTALL_LAUNCH) == FLAG_OPT_INSTALL_LAUNCH;
	}

	public synchronized boolean isSetReboot() {
		return (flag & FLAG_OPT_PUSH_REBOOT) == FLAG_OPT_PUSH_REBOOT;
	}

	public synchronized String getLaunchActivity() {
		return isInstallOptions() ? launchActivity : null;
	}

	public synchronized String getSystemPath() {
		return isPushOptions() ? systemPath : null;
	}

	public void addOptionsChangedListener(IOptionsChangedListener listener) {
		synchronized (sLock) {
			if(listener == null) return;
			if (!sChangedListeners.contains(listener)) {
				sChangedListeners.add(listener);
			}
		}
	}

	public void removeOptionsChangedListener(IOptionsChangedListener listener) {
		synchronized (sLock) {
			sChangedListeners.remove(listener);
		}
	}

	private void optionsChanged(int changedFlag, String... extraData) {
		IOptionsChangedListener[] listenersCopy = null;
		synchronized (sLock) {
			listenersCopy = sChangedListeners.toArray(
					new IOptionsChangedListener[sChangedListeners.size()]);
		}

		for (IOptionsChangedListener listener : listenersCopy) {
			listener.changeOptions(changedFlag, extraData);
		}
	}

	public void disseminate() {
		IOptionsChangedListener[] listenersCopy = null;
		synchronized (sLock) {
			listenersCopy = sChangedListeners.toArray(
					new IOptionsChangedListener[sChangedListeners.size()]);
		}

		for (IOptionsChangedListener listener : listenersCopy) {
			listener.changeOptions(FLAG_OPT_DISSEMINATE, (String[])null);
		}
	}
}
