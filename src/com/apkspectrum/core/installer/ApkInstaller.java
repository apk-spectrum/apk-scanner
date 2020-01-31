package com.apkspectrum.core.installer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.NullOutputReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import com.apkspectrum.data.apkinfo.CompactApkInfo;
import com.apkspectrum.resource._RProp;
import com.apkspectrum.tool.adb.AdbDeviceHelper;
import com.apkspectrum.tool.adb.PackageManager;
import com.apkspectrum.tool.adb.SimpleOutputReceiver;
import com.apkspectrum.tool.adb.AdbDeviceHelper.CommandRejectedException;
import com.apkspectrum.util.FileUtil;
import com.apkspectrum.util.Log;
import com.apkspectrum.util.ZipFileUtil;

public class ApkInstaller
{
	private IDevice device;

	public ApkInstaller() {
		this(null);	
	}

	public ApkInstaller(IDevice device) {
		setDevice(device);	
	}

	public void setDevice(IDevice device) {
		this.device = device;
	}

	public void install(final CompactApkInfo apkInfo, final OptionsBundle options) {
		install(device, apkInfo, options);
	}

	public static String install(final IDevice device, final CompactApkInfo apkInfo, final OptionsBundle options)
	{
		String errMessage = null;
		Log.i("InstallApk() device : " + device.getSerialNumber() + ", apkPath: " + apkInfo.filePath);
		if(apkInfo == null || apkInfo.filePath == null || apkInfo.filePath.isEmpty()) {
			errMessage = "No such file: " + apkInfo.filePath;
		} else if(options == null) {
			errMessage = "Options is null";
		} else if(options.isImpossibleInstallOptions()) {
			errMessage = "Can not install";
		} else if(options.isNotInstallOptions()) {
			errMessage = "No install";
		} else if(device == null  || !device.isOnline()) {
			errMessage = "Device is not online";
		}

		if(errMessage == null) {
			if(options.isInstallOptions()) {
				errMessage = installApk(device, apkInfo, options);
			} else if(options.isPushOptions()) {
				errMessage = pushApk(device, apkInfo, options);
			}
		}

		return errMessage;
	}

	private static String installApk(final IDevice device, final CompactApkInfo apkInfo, final OptionsBundle options) {
		String errMessage = null;

		boolean reinstall = options.isSetReplace();

		ArrayList<String> extraArgs = new ArrayList<String> ();
		if(options.isSetForwardLock()) {
			extraArgs.add("-l");
		}
		if(options.isSetAllowTestPackage()) {
			extraArgs.add("-t");
		}
		if(options.isSetOnSdcard()) {
			extraArgs.add("-s");
		}
		if(options.isSetDowngrade()) {
			extraArgs.add("-d");
		}
		if(options.isSetGrantPermissions()) {
			extraArgs.add("-g");
		}

		errMessage = PackageManager.installPackage(device, apkInfo.filePath, reinstall, extraArgs.isEmpty() ? null : extraArgs.toArray(new String[extraArgs.size()]));
		if(errMessage == null || errMessage.isEmpty()) {
			if(options.isSetLaunch()) {
				String activity = options.getLaunchActivity();
				if(activity != null) {
					String pacakgeName = apkInfo.packageName;
					if(pacakgeName != null) {
						String[] cmdResult = AdbDeviceHelper.launchActivity(device, pacakgeName + "/" + activity);
						if(cmdResult == null || (cmdResult.length >= 2 && cmdResult[1].startsWith("Error")) ||
								(cmdResult.length >= 1 && cmdResult[0].startsWith("error"))) {
							Log.e("activity start faile : " + pacakgeName + "/" + activity);

							if(cmdResult != null) {
								StringBuilder sb = new StringBuilder("cmd: adb shell start -n " + pacakgeName + "/" + activity + "\n\n");
								for(String s : cmdResult) sb.append(s+"\n");
								errMessage = sb.toString();
								Log.e(errMessage);
							}
						} else if(_RProp.B.TRY_UNLOCK_AF_LAUNCH.get()) {
							AdbDeviceHelper.tryDismissKeyguard(device);
						}
					}
				}
			}
		}
		return errMessage;
	}

	private static String pushApk(final IDevice device, final CompactApkInfo apkInfo, final OptionsBundle options) {
		String errMessage = null;

		if(!AdbDeviceHelper.hasSu(device)) {
			errMessage = "Permission denied: Can not push to a system!";
		} else if(!AdbDeviceHelper.isRoot(device)) {
			errMessage = "Permission denied: System is not root, try again after change root mode by 'adb root' command";
		} else {
			try {
				AdbDeviceHelper.remount(AndroidDebugBridge.getSocketAddress(), device);
				device.executeShellCommand("su root setenforce 0", new NullOutputReceiver());
			} catch (TimeoutException | CommandRejectedException | IOException e) {
				errMessage = "Failure: Can not remount";
				e.printStackTrace();
			} catch (ShellCommandUnresponsiveException e) {
				Log.w("Warning: fail: su root setenforce 0");
				e.printStackTrace();
			} catch (AdbCommandRejectedException e1) {
				Log.w(e1.getMessage());
			}
		}

		if(errMessage == null) {
			String installedPath = options.getInstalledSystemPath();
			if(installedPath != null && installedPath.startsWith("/system/")) {
				String removePath = installedPath.replaceAll("^(/system/(priv-)?app/[^/]*/)[^/]*\\.apk", "$1");
				Log.v("removePath: " + removePath + ", installedPath: " +installedPath);
				if(!removePath.matches("^/system/(priv-)?app/$")) {
					try {
						SimpleOutputReceiver outputReceiver = new SimpleOutputReceiver();
						device.executeShellCommand("rm -r " + removePath, outputReceiver);
						for(String line: outputReceiver.getOutput()) {
							if(!line.isEmpty()) {
								errMessage = line;
								break;
							}
						}

						if(errMessage == null || errMessage.isEmpty()) {

						}
					} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
						errMessage = e.getMessage();
						e.printStackTrace();
					}
				}
			} else if(installedPath != null) {
				Log.w("Unknown system path : " + installedPath);
			}
		}

		if(errMessage == null) {
			Log.v("push " + apkInfo.filePath + " to " + options.getTargetSystemPath());
			try {
				device.pushFile(apkInfo.filePath, options.getTargetSystemPath());
			} catch (SyncException | IOException | AdbCommandRejectedException | TimeoutException e) {
				errMessage = e.getMessage();
				e.printStackTrace();
			}
		}

		if(errMessage == null) {
			String tempPath = FileUtil.getTempPath() + File.separator + device.getSerialNumber() + "_push" + File.separator;
			if(options.isSetWithLib32()) {
				String selArch = options.getWithLib32Arch();
				String selDest = options.getWithLib32ToPath();
				String filter = "lib/" + selArch + "/";
				if(!ZipFileUtil.unZip(apkInfo.filePath, filter, tempPath + filter)) {
					errMessage = "Fail to unzip libraries : " + filter;
				} else {
					try {
						for(File lib: new File(tempPath + filter).listFiles()) {
							Log.v(lib.getAbsolutePath() + " to " + selDest + lib.getName());
							device.pushFile(lib.getAbsolutePath(), selDest + lib.getName());
							lib.deleteOnExit();
						}
					} catch (SyncException | IOException | AdbCommandRejectedException | TimeoutException e) {
						errMessage = e.getMessage();
						e.printStackTrace();
					}
				}
			}

			if(options.isSetWithLib64()) {
				String selArch = options.getWithLib64Arch();
				String selDest = options.getWithLib64ToPath();
				String filter = "lib/" + selArch + "/";
				if(!ZipFileUtil.unZip(apkInfo.filePath, filter, tempPath + filter)) {
					if(errMessage == null) {
						errMessage = "Fail to unzip libraries : " + filter;
					} else{ 
						errMessage += "; Fail to unzip libraries : " + filter;
					}
				} else {
					try {
						for(File lib: new File(tempPath + filter).listFiles()) {
							Log.v(lib.getAbsolutePath() + " to " + selDest + lib.getName());
							device.pushFile(lib.getAbsolutePath(), selDest + lib.getName());
							lib.deleteOnExit();
						}
					} catch (SyncException | IOException | AdbCommandRejectedException | TimeoutException e) {
						if(errMessage == null) {
							errMessage = e.getMessage();
						} else{ 
							errMessage += "; " + e.getMessage();
						}
						e.printStackTrace();
					}
				}
			}
			FileUtil.deleteDirectory(new File(tempPath));
		}

		if(errMessage == null) {
			if(options.isSetReboot()) {
				Log.v("reboot " + device.getSerialNumber());
				try {
					device.reboot(null);
				} catch (TimeoutException | AdbCommandRejectedException | IOException e) {
					e.printStackTrace();
				}
			}
		}

		return errMessage;
	}
}
