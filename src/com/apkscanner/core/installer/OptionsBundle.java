package com.apkscanner.core.installer;

import java.util.ArrayList;

import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class OptionsBundle {
	public static final int FLAG_OPT_INSTALL	 	= 0x010000;
	public static final int FLAG_OPT_PUSH			= 0x020000;
	public static final int FLAG_OPT_NO_INSTALL	= 0x030000;

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

	public static final int NO_BLOACKED = 0x0000;
	public static final int BLOACKED_COMMON_CAUSE_UNSIGNED = 0x0001;
	public static final int BLOACKED_COMMON_CAUSE_UNSUPPORTED_SDK_LEVEL = 0x0002;
	public static final int BLOACKED_INSTALL_CAUSE_MISMATCH_SIGNED = 0x0008;
	public static final int BLOACKED_PUSH_CAUSE_NO_ROOT = 0x0004;
	public static final int BLOACKED_PUSH_CAUSE_MISMATCH_SIGNED_NOT_SYSTEM = 0x0010;
	public static final int BLOACKED_LAUNCH_CAUSE_NO_SUCH_ACTIVITY = 0x0020;
	public static final int BLOACKED_CAUSE_UNKNWON = 0x8000;

	public static final int BLOACKED_INSTALL_MASK = BLOACKED_COMMON_CAUSE_UNSIGNED | BLOACKED_COMMON_CAUSE_UNSUPPORTED_SDK_LEVEL | BLOACKED_INSTALL_CAUSE_MISMATCH_SIGNED; 
	public static final int BLOACKED_PUSH_MASK = BLOACKED_COMMON_CAUSE_UNSIGNED | BLOACKED_COMMON_CAUSE_UNSUPPORTED_SDK_LEVEL 
			| BLOACKED_PUSH_CAUSE_NO_ROOT | BLOACKED_PUSH_CAUSE_MISMATCH_SIGNED_NOT_SYSTEM;

	int flag;
	int blockedFlags; 
	int blockedCause;

	String launchActivity;
	String installedPath;
	String systemPath;
	String lib32Arch;
	String lib32ToPath;
	String lib64Arch;
	String lib64ToPath;

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

	public synchronized void setBlockedFlags(int blockedFlags, int blockedCause) {
		this.blockedFlags |= blockedFlags;
		this.blockedCause |= blockedCause;
		flag &= ~blockedFlags;

		if(!isBlockedFlags(FLAG_OPT_INSTALL | FLAG_OPT_PUSH)) {
			if(isBlockedFlags(FLAG_OPT_INSTALL)) {
				flag |= FLAG_OPT_PUSH;
			} else if(isBlockedFlags(FLAG_OPT_PUSH)) {
				flag |= FLAG_OPT_INSTALL;
			}
		}
	}

	public synchronized boolean isBlockedFlags(int flags) {
		return (blockedFlags & flags) == flags;
	}
	
	public synchronized int getBlockedCause(int flag) {
		int cause = NO_BLOACKED;
		switch(flag) {
		case FLAG_OPT_INSTALL:
		case FLAG_OPT_PUSH:
			if(isBlockedFlags(flag)) {
				if((blockedCause & BLOACKED_COMMON_CAUSE_UNSIGNED) == BLOACKED_COMMON_CAUSE_UNSIGNED) {
					cause = BLOACKED_COMMON_CAUSE_UNSIGNED;
				} else if((blockedCause & BLOACKED_COMMON_CAUSE_UNSUPPORTED_SDK_LEVEL) == BLOACKED_COMMON_CAUSE_UNSUPPORTED_SDK_LEVEL) {
					cause = BLOACKED_COMMON_CAUSE_UNSUPPORTED_SDK_LEVEL;
				} else if(flag == FLAG_OPT_INSTALL) {
					if((blockedCause & BLOACKED_INSTALL_CAUSE_MISMATCH_SIGNED) == BLOACKED_INSTALL_CAUSE_MISMATCH_SIGNED) {
						cause = BLOACKED_INSTALL_CAUSE_MISMATCH_SIGNED;
					} else {
						cause = BLOACKED_CAUSE_UNKNWON;
					}
				} else if(flag == FLAG_OPT_PUSH) {
					if((blockedCause & BLOACKED_PUSH_CAUSE_NO_ROOT) == BLOACKED_PUSH_CAUSE_NO_ROOT) {
						cause = BLOACKED_PUSH_CAUSE_NO_ROOT;
					} else if ((blockedCause & BLOACKED_PUSH_CAUSE_MISMATCH_SIGNED_NOT_SYSTEM) == BLOACKED_PUSH_CAUSE_MISMATCH_SIGNED_NOT_SYSTEM) {
						cause = BLOACKED_PUSH_CAUSE_MISMATCH_SIGNED_NOT_SYSTEM;
					} else {
						cause = BLOACKED_CAUSE_UNKNWON;
					}					
				}
			}
			break;
		case FLAG_OPT_INSTALL_LAUNCH:
			if(isBlockedFlags(flag)) {
				cause = BLOACKED_LAUNCH_CAUSE_NO_SUCH_ACTIVITY;
			}
			break;
		}

		return cause;
	}

	public synchronized void set(int flag) {
		set(flag, (String[])null);
	}

	public synchronized void set(int flag, String... extraData) {
		if((flag & blockedFlags) != 0 && flag != FLAG_OPT_NO_INSTALL) {
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
		case FLAG_OPT_NO_INSTALL:
			this.flag &= ~FLAG_OPT_NO_INSTALL;
			break;
		case FLAG_OPT_PUSH_SYSTEM:
		case FLAG_OPT_PUSH_PRIVAPP:
			this.flag &= ~(FLAG_OPT_PUSH_SYSTEM | FLAG_OPT_PUSH_PRIVAPP);
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
		return (flag & FLAG_OPT_NO_INSTALL) == FLAG_OPT_NO_INSTALL;
	}

	public synchronized boolean isDontInstallOptions() {
		return (flag & FLAG_OPT_NO_INSTALL) == 0;
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

	public synchronized boolean isSetPushToSystem() {
		return (flag & FLAG_OPT_PUSH_SYSTEM) == FLAG_OPT_PUSH_SYSTEM;
	}

	public synchronized boolean isSetPushToPriv() {
		return (flag & FLAG_OPT_PUSH_PRIVAPP) == FLAG_OPT_PUSH_PRIVAPP;
	}

	public synchronized boolean isSetReboot() {
		return (flag & FLAG_OPT_PUSH_REBOOT) == FLAG_OPT_PUSH_REBOOT;
	}

	public synchronized boolean isSetWithLib32() {
		return (flag & FLAG_OPT_PUSH_LIB32) == FLAG_OPT_PUSH_LIB32;
	}

	public synchronized boolean isSetWithLib64() {
		return (flag & FLAG_OPT_PUSH_LIB64) == FLAG_OPT_PUSH_LIB64;
	}

	public synchronized String getLaunchActivity() {
		return launchActivity;
	}

	public synchronized String getSystemPath() {
		return systemPath;
	}

	public synchronized String getWithLib32Arch() {
		return lib32Arch;
	}

	public synchronized String getWithLib32ToPath() {
		return lib32ToPath;
	}

	public synchronized String getWithLib64Arch() {
		return lib64Arch;
	}

	public synchronized String getWithLib64ToPath() {
		return lib64ToPath;
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
