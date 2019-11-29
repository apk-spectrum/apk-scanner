package com.apkscanner.tool.adb;

import java.io.File;
import java.util.concurrent.TimeUnit;

import com.android.ddmlib.AdbVersion;
import com.apkscanner.resource.RFile;
import com.apkscanner.resource.RProp;
import com.apkscanner.util.ConsolCmd;
import com.apkscanner.util.ConsolCmd.ConsoleOutputObserver;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.google.common.util.concurrent.Uninterruptibles;

public class AdbWrapper
{
	protected static String adbCmd = getAdbCmd();
	private static String version;

	private ConsoleOutputObserver listener;
	private String device;

	public AdbWrapper(String device, ConsoleOutputObserver listener) {
		this.device = device;
		this.listener = listener;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public void setListener(ConsoleOutputObserver listener) {
		this.listener = listener;
	}

	public static String getAdbCmd() {
		String cmd = adbCmd;
		if(cmd == null) {
			String runningAdbPath = null;
			AdbVersion adbVersion = null;
			if(RProp.B.ADB_POLICY_SHARED.get()) {
				String[] runProcess = null;
				int waitCnt = 0;
				do {
					if(runProcess != null) {
						if(waitCnt++ > 5) {
							Log.d("waiting for running adb daemon only one. but any daemon be not exit. " + runProcess.length);
							break;
						};
						Log.d("waiting for running adb daemon only one. runProcess:" + runProcess.length + ", waitCnt:" + waitCnt);
						Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
					}
					runProcess = AdbServerMonitor.getRunningAdbPath();
				} while(runProcess.length > 1);
				if(runProcess != null && runProcess.length > 0) {
					runningAdbPath = runProcess[0];
					adbVersion = AdbVersionManager.getAdbVersion(runningAdbPath);
					if(!AdbVersionManager.checkAdbVersion(adbVersion)) {
						runningAdbPath = null;
					}
				}
			}
			Log.v("runningAdbPath " + runningAdbPath + ", version " + adbVersion);

			if(runningAdbPath == null) {
				String adbPath = (RProp.S.ADB_PATH.get()).trim();
				if(adbPath == null || adbPath.isEmpty()
						|| !AdbVersionManager.checkAdbVersion(adbPath)) {
					adbPath = AdbVersionManager.getAdbLastestVersionFromCache();
					if(adbPath == null){
						AdbVersionManager.loadDefaultAdbs(); // very higher cost
						adbPath = AdbVersionManager.getAdbLastestVersionFromCache();
					}
				}
				runningAdbPath = adbPath;
			}

			if(runningAdbPath == null) {
				runningAdbPath = RFile.BIN_ADB.get();
			}
			cmd = runningAdbPath;

			if(!(new File(cmd)).exists()) {
				Log.e("no such adb tool" + adbCmd);
				cmd = null;
			}
		}
		return cmd;
	}

	public static void setAdbCmd(String adbPath) {
		adbCmd = adbPath;
	}

	public String version() {
		return version(listener);
	}

	static public String version(ConsoleOutputObserver listener) {
		if(version == null) {
			String adb = getAdbCmd();
			if(adb == null) return null;
			String[] result = ConsolCmd.exec(new String[] {adb, "version"}, false, listener);
			version = result[0];
		}
		return version;
	}

	public boolean startServer() {
		return startServer(listener);
	}

	static public boolean startServer(ConsoleOutputObserver listener) {
		if(adbCmd == null) return false;
		String[] result = ConsolCmd.exec(new String[] {adbCmd, "start-server"}, false, listener);
		return result[1].matches(".*daemon started successfully.*");
	}

	public void killServer() {
		killServer(listener);
	}

	static public void killServer(ConsoleOutputObserver listener) {
		if(adbCmd == null) return;
		ConsolCmd.exec(new String[] {adbCmd, "kill-server"}, false, listener);
	}

	public boolean restartServer() {
		return restartServer(listener);
	}

	static public boolean restartServer(ConsoleOutputObserver listener) {
		if(adbCmd == null) return false;
		killServer(listener);
		return startServer(listener);
	}

	static public void waitForDevice() {
		ConsolCmd.exec(new String[] {adbCmd, "wait-for-device"});
	}

	public String[] devices() {
		return devices(listener);
	}

	static public String[] devices(ConsoleOutputObserver listener) {
		if(adbCmd == null) return null;
		return ConsolCmd.exec(new String[] {adbCmd, "devices", "-l"}, false, listener);
	}

	public String getProp(String tag) {
		return getProp(device, tag, listener);
	}

	static public String getProp(String device, String tag, ConsoleOutputObserver listener) {
		if(adbCmd == null) return null;
		String[] param;
		if(device == null || device.isEmpty()) {
			param = new String[] {adbCmd, "shell", "getprop", tag};
		} else {
			param = new String[] {adbCmd, "-s", device, "shell", "getprop", tag};
		}
		String[] result = ConsolCmd.exec(param, false, listener);
		return result[0];
	}

	public boolean root() {
		return root(device, listener);
	}

	static public boolean root(String device, ConsoleOutputObserver listener) {
		if(adbCmd == null) return false;
		String[] param;
		if(device == null || device.isEmpty()) {
			param = new String[] {adbCmd, "root"};
		} else {
			param = new String[] {adbCmd, "-s", device, "root"};
		}
		String[] result = ConsolCmd.exec(param, false, listener);
		if(result == null || result.length == 0 || !result[0].endsWith("running as root")) {
			return false;
		}
		return true;
	}

	public boolean remount() {
		return remount(device, listener);
	}

	static public boolean remount(String device, ConsoleOutputObserver listener) {
		if(adbCmd == null) return false;
		String[] param;
		if(device == null || device.isEmpty()) {
			param = new String[] {adbCmd, "remount"};
		} else {
			param = new String[] {adbCmd, "-s", device, "remount"};
		}
		String[] result = ConsolCmd.exec(param, false, listener);
		if(result == null || result.length == 0 || !result[0].endsWith("remount succeeded")) {
			return false;
		}
		return true;
	}

	public String[] shell(String[] param) {
		return shell(device, param, listener);
	}

	static public String[] shell(String device, String[] param, ConsoleOutputObserver listener) {
		if(adbCmd == null) return null;
		String[] cmd;
		if(device == null || device.isEmpty()) {
			cmd = new String[] {adbCmd, "shell"};
		} else {
			cmd = new String[] {adbCmd, "-s", device, "shell"};
		}
		String[] shellcmd = new String[cmd.length + param.length];
		System.arraycopy(cmd, 0, shellcmd, 0, cmd.length);
		System.arraycopy(param, 0, shellcmd, cmd.length, param.length);
		String[] result = ConsolCmd.exec(shellcmd, false, listener);
		return result;
	}

	public void reboot() {
		reboot(device, listener);
	}

	static public void reboot(String device, ConsoleOutputObserver listener) {
		if(adbCmd == null) return;
		String[] param;
		if(device == null || device.isEmpty()) {
			param = new String[] {adbCmd, "reboot"};
		} else {
			param = new String[] {adbCmd, "-s", device, "reboot"};
		}
		ConsolCmd.exec(param, false, listener);
	}

	static public String getApkPath(String device, String apkPath, boolean force) {
		if(!apkPath.endsWith(".apk")) {
			Log.i("No apk file path : " + apkPath);
			apkPath += "/*.apk";
		} else if(!force) {
			return apkPath;
		}

		String[] result = shell(device, new String[] {"ls", apkPath}, null);
		if(result.length == 0 || !result[0].endsWith(".apk")) {
			Log.e("No such apk file : " + apkPath);
			return null;
		}
		apkPath = result[0];
		Log.i("Cahnge target apk path to " + apkPath);

		return apkPath;
	}

	public boolean pullApk(String srcApkPath, String destApkPath) {
		return pullApk(device, srcApkPath, destApkPath, listener);
	}

	static public boolean pullApk(final String device, final String srcApkPath, final String destApkPath, final ConsoleOutputObserver listener) {
		final String realApkPath = getApkPath(device, srcApkPath, true);
		final String tmpPath = "/sdcard/tmp";

		if(realApkPath == null) {
			Log.e("No such apk file : " + srcApkPath);
			return false;
		}

		FileUtil.makeFolder(new File(destApkPath).getParent());

		boolean ret = pull(device, realApkPath, destApkPath, new ConsoleOutputObserver() {
			@Override
			public boolean ConsolOutput(String output) {
				if(output.trim().endsWith(": Permission denied")){
					Log.w("adb pull permission denied : " + realApkPath);
					String[] mkdir = {"mkdir", "-p", tmpPath + realApkPath.substring(0, realApkPath.lastIndexOf("/"))};
					String[] result = shell(device, mkdir, listener);
					if(result.length == 0) {
						String[] cp = {"cp", realApkPath, tmpPath + realApkPath};
						shell(device, cp, listener);
					}
				}
				if(listener != null) {
					return listener.ConsolOutput(output);
				}
				return true;
			}
		});

		if(!ret) {
			String tmpApkFilePath = getApkPath(device, tmpPath + realApkPath, true);
			if(tmpApkFilePath != null) {
				ret = pull(device, tmpApkFilePath, destApkPath, listener);
			}
		}

		return ret;
	}

	public boolean pull(String srcApkPath, String destApkPath) {
		return pull(device, srcApkPath, destApkPath, listener);
	}

	static public boolean pull(String device, String srcApkPath, String destApkPath, ConsoleOutputObserver listener) {
		if(adbCmd == null) return false;
		String[] param;
		if(device == null || device.isEmpty()) {
			param = new String[] {adbCmd, "pull", srcApkPath, destApkPath};
		} else {
			param = new String[] {adbCmd, "-s", device, "pull", "-p", srcApkPath, destApkPath};
		}
		String[] result = ConsolCmd.exec(param, false, listener);
		if(result == null || result.length == 0
				/*|| (!result[result.length-1].endsWith("s)") && !result[result.length-1].startsWith("[100%]") && !result[0].endsWith("s)"))*/
				|| !(new File(destApkPath).exists())) {
			return false;
		}
		return true;
	}

	public boolean push(String srcApkPath, String destApkPath) {
		return push(device, srcApkPath, destApkPath, listener);
	}

	static public boolean push(String device, String srcApkPath, String destApkPath, ConsoleOutputObserver listener) {
		if(adbCmd == null) return false;
		String[] param;
		if(device == null || device.isEmpty()) {
			param = new String[] {adbCmd, "push", srcApkPath, destApkPath};
		} else {
			param = new String[] {adbCmd, "-s", device, "push", srcApkPath, destApkPath};
		}
		String[] result = ConsolCmd.exec(param, false, listener);
		if(result == null || !result[0].endsWith("s)")) {
			return false;
		}
		return true;
	}

	public String[] install(String apkPath) {
		return install(device, apkPath, listener);
	}

	public String[] install(String apkPath, boolean onSdcard) {
		if(onSdcard) {
			return installOnSdcard(device, apkPath, listener);
		}
		return install(device, apkPath, listener); 
	}

	static public String[] install(String device, String apkPath, ConsoleOutputObserver listener) {
		if(adbCmd == null) return null;
		String[] param;
		if(device == null || device.isEmpty()) {
			param = new String[] {adbCmd, "install", "-r", "-d", apkPath};
		} else {
			param = new String[] {adbCmd, "-s", device, "install", "-r", "-d", apkPath};
		}
		String[] result = ConsolCmd.exec(param, false, listener);
		return result;
	}

	static public String[] installOnSdcard(String device, String apkPath, ConsoleOutputObserver listener) {
		if(adbCmd == null) return null;
		String[] param;
		if(device == null || device.isEmpty()) {
			param = new String[] {adbCmd, "install", "-r", "-d", "-s", apkPath};
		} else {
			param = new String[] {adbCmd, "-s", device, "install", "-r", "-d", "-s", apkPath};
		}
		String[] result = ConsolCmd.exec(param, false, listener);
		return result;
	}

	public String[] uninstall(String packageName) {
		return uninstall(device, packageName, listener);
	}

	static public String[] uninstall(String device, String packageName, ConsoleOutputObserver listener) {
		if(adbCmd == null) return null;
		String[] param;
		if(device == null || device.isEmpty()) {
			param = new String[] {adbCmd, "uninstall", packageName};
		} else {
			param = new String[] {adbCmd, "-s", device, "uninstall", packageName};
		}
		String[] result = ConsolCmd.exec(param, false, listener);
		return result;
	}
}
