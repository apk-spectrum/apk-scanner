package com.apkscanner.gui.action;

import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import com.android.ddmlib.IDevice;
import com.apkscanner.gui.MessageBoxPool;
import com.apkscanner.gui.easymode.contents.EasyGuiDeviceToolPanel;
import com.apkscanner.resource.RConst;
import com.apkscanner.resource.RProp;
import com.apkscanner.resource.RStr;
import com.apkspectrum.data.apkinfo.ApkInfo;
import com.apkspectrum.data.apkinfo.ApkInfoHelper;
import com.apkspectrum.data.apkinfo.ComponentInfo;
import com.apkspectrum.logback.Log;
import com.apkspectrum.swing.ApkActionEventHandler;
import com.apkspectrum.swing.MessageBoxPane;
import com.apkspectrum.tool.adb.AdbDeviceHelper;
import com.apkspectrum.tool.adb.PackageInfo;

public class DeviceLaunchAppAction extends AbstractDeviceAction {
    private static final long serialVersionUID = -5280023450341526022L;

    public static final String ACTION_COMMAND = "ACT_CMD_LAUNCH_APP";

    public DeviceLaunchAppAction(ApkActionEventHandler h) {
        super(h);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean withShift = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;

        IDevice device = null;
        if (e.getSource() instanceof EasyGuiDeviceToolPanel) {
            device = ((EasyGuiDeviceToolPanel) e.getSource()).getSelecteddevice();
        }

        String data = RProp.S.DEFAULT_LAUNCH_MODE.get();
        int activityOpt = RProp.I.LAUNCH_ACTIVITY_OPTION.get();
        if (withShift || RConst.STR_LAUNCH_SELECT.equals(data)
                || activityOpt == RConst.INT_LAUNCH_ALWAYS_CONFIRM_ACTIVITY) {
            evtLaunchByChooseApp(getWindow(e), device);
        } else {
            evtLaunchApp(getWindow(e), device, activityOpt);
        }
    }

    protected void evtLaunchApp(final Window owner, final IDevice target, final int option) {
        Log.v("activityOpt: " + option);
        final ApkInfo apkInfo = getApkInfo();
        if (apkInfo == null) return;

        Thread thread = new Thread(new Runnable() {
            public void run() {
                String packageName = apkInfo.manifest.packageName;

                IDevice[] devices = null;
                if (target == null) {
                    devices = getInstalledDevice(packageName);
                } else {
                    if (getPackageInfo(target, packageName) != null) {
                        devices = new IDevice[] {target};
                    }
                }

                if (devices == null || devices.length == 0) {
                    Log.i("No such device of a package installed.");
                    MessageBoxPool.show(owner, MessageBoxPool.MSG_NO_SUCH_PACKAGE_DEVICE);
                    return;
                }

                ComponentInfo[] apkActivities =
                        ApkInfoHelper.getLauncherActivityList(apkInfo, true);

                for (IDevice device : devices) {
                    Log.v("launch activity on " + device.getSerialNumber());

                    PackageInfo packageInfo = getPackageInfo(device, packageName);

                    if (!packageInfo.isEnabled()) {
                        MessageBoxPool.show(owner, MessageBoxPool.MSG_DISABLED_PACKAGE,
                                device.getProperty(IDevice.PROP_DEVICE_MODEL));
                        continue;
                    }

                    String selectedActivity = null;
                    ComponentInfo[] activities = packageInfo.getLauncherActivityList(false);
                    if (activities != null && activities.length > 0) {
                        selectedActivity = activities[0].name;
                    } else {
                        activities = packageInfo.getLauncherActivityList(true);
                        if (option == RConst.INT_LAUNCH_LAUNCHER_OR_MAIN_ACTIVITY) {
                            if (activities != null && activities.length > 0) {
                                selectedActivity = activities[0].name;
                            }
                        }
                        if (selectedActivity == null) {
                            selectedActivity = chooseLunchActivity(owner, device, packageName,
                                    activities, apkActivities);
                            if (selectedActivity == null) return;
                        }
                    }

                    lauchActivity(owner, device, packageName, selectedActivity);
                }
            }
        });
        thread.setPriority(Thread.NORM_PRIORITY);
        thread.start();
    }

    protected void evtLaunchByChooseApp(final Window owner, final IDevice target) {
        final ApkInfo apkInfo = getApkInfo();
        if (apkInfo == null) return;

        Thread thread = new Thread(new Runnable() {
            public void run() {
                String packageName = apkInfo.manifest.packageName;

                IDevice[] devices = null;
                if (target == null) {
                    devices = getInstalledDevice(packageName);
                } else {
                    if (getPackageInfo(target, packageName) != null) {
                        devices = new IDevice[] {target};
                    }
                }

                if (devices == null || devices.length == 0) {
                    Log.i("No such device of a package installed.");
                    MessageBoxPool.show(owner, MessageBoxPool.MSG_NO_SUCH_PACKAGE_DEVICE);
                    return;
                }

                ComponentInfo[] apkActivities =
                        ApkInfoHelper.getLauncherActivityList(apkInfo, true);

                for (IDevice device : devices) {
                    Log.v("launch activity on " + device.getSerialNumber());
                    PackageInfo packageInfo = getPackageInfo(device, packageName);
                    if (!packageInfo.isEnabled()) {
                        MessageBoxPool.show(owner, MessageBoxPool.MSG_DISABLED_PACKAGE,
                                device.getProperty(IDevice.PROP_DEVICE_MODEL));
                        continue;
                    }

                    ComponentInfo[] activities = packageInfo.getLauncherActivityList(true);

                    String selectedActivity = chooseLunchActivity(owner, device, packageName,
                            activities, apkActivities);
                    if (selectedActivity == null) return;
                    lauchActivity(owner, device, packageName, selectedActivity);
                }
            }
        });
        thread.setPriority(Thread.NORM_PRIORITY);
        thread.start();
    }

    protected String chooseLunchActivity(final Window owner, IDevice device, String packageName,
            ComponentInfo[] fromDevice, ComponentInfo[] fromApk) {
        int mergeLength = (fromDevice != null ? fromDevice.length : 0)
                + (fromApk != null ? fromApk.length : 0);
        ArrayList<String> mergeList = new ArrayList<String>(mergeLength);

        if (fromDevice != null) {
            for (ComponentInfo comp : fromDevice) {
                boolean isLauncher = ((comp.featureFlag & ApkInfo.APP_FEATURE_LAUNCHER) != 0);
                boolean isMain = ((comp.featureFlag & ApkInfo.APP_FEATURE_MAIN) != 0);
                mergeList.add((isLauncher ? "[LAUNCHER]" : (isMain ? "[MAIN]" : "")) + " "
                        + comp.name.replaceAll("^" + packageName, ""));
            }
        }

        if (fromApk != null) {
            for (ComponentInfo comp : fromApk) {
                boolean isLauncher = ((comp.featureFlag & ApkInfo.APP_FEATURE_LAUNCHER) != 0);
                boolean isMain = ((comp.featureFlag & ApkInfo.APP_FEATURE_MAIN) != 0);
                mergeList.add((isLauncher ? "[APK_LAUNCHER]" : (isMain ? "[APK_MAIN]" : "[APK]"))
                        + " " + comp.name.replaceAll("^" + packageName, ""));
            }
        }

        if (!mergeList.isEmpty()) {
            String selected = (String) MessageBoxPane.showInputDialog(owner,
                    "Select Activity for " + device.getProperty(IDevice.PROP_DEVICE_MODEL),
                    RStr.BTN_LAUNCH.get(), MessageBoxPane.QUESTION_MESSAGE, null,
                    mergeList.toArray(new String[mergeList.size()]), mergeList.get(0));
            if (selected != null) {
                return selected.split(" ")[1];
            } else {
                return null;
            }
        }
        return "";
    }

    protected void lauchActivity(final Window owner, IDevice device, String packageName,
            String activity) {
        if (activity == null || activity.isEmpty()) {
            Log.w("No such activity of launcher or main");
            MessageBoxPool.show(owner, MessageBoxPool.MSG_NO_SUCH_LAUNCHER);
            return;
        }

        final String launcherActivity = packageName + "/" + activity;
        Log.i("launcherActivity : " + launcherActivity);

        String[] cmdResult = AdbDeviceHelper.launchActivity(device, launcherActivity);
        if (cmdResult == null || (cmdResult.length >= 2 && cmdResult[1].startsWith("Error"))
                || (cmdResult.length >= 1 && cmdResult[0].startsWith("error"))) {
            Log.e("activity start faile : " + launcherActivity);

            StringBuilder sb =
                    new StringBuilder("cmd: adb shell start -n " + launcherActivity + "\n\n");
            if (cmdResult != null) {
                for (String s : cmdResult)
                    sb.append(s + "\n");
            }
            final String errMsg = sb.toString();
            Log.e(errMsg);

            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    MessageBoxPool.show(owner, MessageBoxPool.MSG_FAILURE_LAUNCH_APP, errMsg);
                }
            });
        } else if (RProp.B.TRY_UNLOCK_AF_LAUNCH.get()) {
            AdbDeviceHelper.tryDismissKeyguard(device);
        }
    }
}
