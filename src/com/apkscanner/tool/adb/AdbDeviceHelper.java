package com.apkscanner.tool.adb;

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.apkscanner.util.Log;

public class AdbDeviceHelper {

	public static boolean isShowingLockscreen(IDevice device)
	{
		final StringBuilder shResultBuilder = new StringBuilder();
		try {
			device.executeShellCommand("dumpsys window policy | grep mShowingLockscreen", new IShellOutputReceiver(){
				@Override
				public void addOutput(byte[] arg0, int arg1, int arg2) {
					shResultBuilder.append(new String(arg0));
				}

				@Override
				public void flush() {
				}

				@Override
				public boolean isCancelled() {
					return false;
				}});
		} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException
				| IOException e) {
			e.printStackTrace();
		}
		String shResult = shResultBuilder.toString().trim();

		boolean islocked = false;
		if(shResult.indexOf("mShowingLockscreen=true") > -1) {
			islocked = true;
		}
		return islocked;
	}
	
	public static Dimension getPhysicalScreenSize(IDevice device) {
		final StringBuilder shResultBuilder = new StringBuilder();
		try {
			device.executeShellCommand("wm size | grep 'Physical size:'", new IShellOutputReceiver(){
				@Override
				public void addOutput(byte[] arg0, int arg1, int arg2) {
					shResultBuilder.append(new String(arg0));
				}

				@Override
				public void flush() {
				}

				@Override
				public boolean isCancelled() {
					return false;
				}});
		} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException
				| IOException e) {
			e.printStackTrace();
		}
		String shResult = shResultBuilder.toString().trim();
		
		Dimension size = null;
		if(shResult.indexOf("Physical size:") > -1) {
			String strSize = shResult.replaceAll(".*Physical size:\\s*(\\d+[xX]\\d+).*", "$1");
			if(!strSize.equals(shResult)) {
				String[] temp = strSize.split("[xX]");
				size = new Dimension(Integer.valueOf(temp[0]), Integer.valueOf(temp[1]));
			}
		}
		return size;
	}
	
	public static boolean isPosibleDismissKeyguard(IDevice device) {
		final StringBuilder shResultBuilder = new StringBuilder();
		try {
			device.executeShellCommand("wm | grep 'wm dismiss-keyguard'", new IShellOutputReceiver(){
				@Override
				public void addOutput(byte[] arg0, int arg1, int arg2) {
					shResultBuilder.append(new String(arg0));
				}

				@Override
				public void flush() {
				}

				@Override
				public boolean isCancelled() {
					return false;
				}});
		} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException
				| IOException e) {
			e.printStackTrace();
		}
		String shResult = shResultBuilder.toString().trim();
		Log.v(shResult);

		return !shResult.isEmpty();
	}
	
	public static boolean isScreenOn(IDevice device) {
		final StringBuilder shResultBuilder = new StringBuilder();
		try {
			device.executeShellCommand("dumpsys input_method | grep -i 'mActive='", new IShellOutputReceiver(){
				@Override
				public void addOutput(byte[] arg0, int arg1, int arg2) {
					shResultBuilder.append(new String(arg0));
				}

				@Override
				public void flush() {
				}

				@Override
				public boolean isCancelled() {
					return false;
				}});
		} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException
				| IOException e) {
			e.printStackTrace();
		}
		String shResult = shResultBuilder.toString().trim();

		return shResult.toLowerCase().indexOf("mactive=true") > -1;
	}

	public static String[] launchActivity(IDevice device, String activity) {
		final ArrayList<String> output = new ArrayList<String>();
		try {
			device.executeShellCommand("am start -n " + activity, new IShellOutputReceiver(){
				@Override
				public void addOutput(byte[] arg0, int arg1, int arg2) {
					output.add(new String(arg0));
				}

				@Override
				public void flush() {
				}

				@Override
				public boolean isCancelled() {
					return false;
				}});
		} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException
				| IOException e) {
			e.printStackTrace();
		}
		return output.toArray(new String[output.size()]);
	}
	
	public static void tryDismissKeyguard(IDevice device) {
		String unlockCmd = "wm dismiss-keyguard;";

		if(!AdbDeviceHelper.isPosibleDismissKeyguard(device) 
				&& AdbDeviceHelper.isShowingLockscreen(device)) {
			Dimension screenSize = AdbDeviceHelper.getPhysicalScreenSize(device);
			if(screenSize != null) {
				int y = screenSize.height * 2 / 3;
				unlockCmd = String.format("input touchscreen swipe %d %d %d %d;", 0, y, screenSize.width, y);
			}
		}

		// Screen Turn on
		if(device.getApiLevel() >= 20) {
			// apiLevel="20" platformVersion="Android 4.4W" versionCode="KITKAT_WATCH"
			try {
				device.executeShellCommand("input keyevent KEYCODE_WAKEUP;" + unlockCmd + "input keyevent KEYCODE_MENU;", new IShellOutputReceiver(){
					@Override
					public void addOutput(byte[] arg0, int arg1, int arg2) {
					}

					@Override
					public void flush() {
					}

					@Override
					public boolean isCancelled() {
						return false;
					}});
			} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException
					| IOException e) {
				e.printStackTrace();
			}
		} else {
			Log.v("device api level " + device.getApiLevel());
			String powerKeyCmd = "";
			if(!AdbDeviceHelper.isScreenOn(device)) {
				powerKeyCmd = "input keyevent KEYCODE_POWER;";
			}
			try {
				device.executeShellCommand(powerKeyCmd + unlockCmd + "input keyevent KEYCODE_MENU", new IShellOutputReceiver(){
					@Override
					public void addOutput(byte[] arg0, int arg1, int arg2) {
					}

					@Override
					public void flush() {
					}

					@Override
					public boolean isCancelled() {
						return false;
					}});
			} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException
					| IOException e) {
				e.printStackTrace();
			}
		}
	}
}
