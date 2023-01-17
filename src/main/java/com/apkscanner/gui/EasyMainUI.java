package com.apkscanner.gui;

import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JFrame;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.apkscanner.gui.easymode.EasyGuiMainPanel;
import com.apkscanner.gui.easymode.core.ToolEntryManager;
import com.apkscanner.resource.RImg;
import com.apkscanner.resource.RProp;
import com.apkscanner.resource.RStr;
import com.apkspectrum.core.scanner.ApkScanner;
import com.apkspectrum.logback.Log;
import com.apkspectrum.tool.adb.AdbServerMonitor;

public class EasyMainUI extends JFrame implements IDeviceChangeListener {
    private static final long serialVersionUID = -1104109718930033124L;

    private ApkScanner apkScanner = null;
    private EasyGuiMainPanel mainpanel;
    // private String filepath;

    public static long UIstarttime;
    public static long corestarttime;
    public static long UIInittime;
    public static boolean isdecoframe = false;

    public EasyMainUI(ApkScanner scanner, UiEventHandler eventHandler) {
        apkScanner = scanner;
        ToolEntryManager.initToolEntryManager(eventHandler);
        InitUI(eventHandler);
        setApkScanner(scanner);
    }

    public void setApkScanner(ApkScanner scanner) {
        if (scanner != null) {
            apkScanner = scanner;
            ToolEntryManager.apkScanner = apkScanner;
            mainpanel.setApkScanner(apkScanner);
        }
    }

    public void InitUI(UiEventHandler eventHandler) {
        Log.d("main start");

        UIInittime = UIstarttime = System.currentTimeMillis();

        setTitle(RStr.APP_NAME.get());

        Log.i("new EasyGuiMainPanel");
        mainpanel = new EasyGuiMainPanel(this, eventHandler);
        mainpanel.setApkScanner(apkScanner);

        if (isdecoframe) {
            setdecoframe();
        } else {
            setResizable(true);
        }
        Log.i("setIconImage");
        setIconImage(RImg.APP_ICON.getImage());
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setMinimumSize(new Dimension(300, 200));

        Log.i("add(mainpanel)");
        add(mainpanel); // 100 => 60

        addWindowListener(eventHandler);

        AdbServerMonitor.startServerAndCreateBridgeAsync();
        AndroidDebugBridge.addDeviceChangeListener(this);
        Log.i("pack");
        pack();

        Log.i("setLocationRelativeTo");
        int setposition = RProp.I.EASY_GUI_WINDOW_POSITION_X.get();

        if (setposition == 0) {
            setLocationRelativeTo(null);
            Point position = getLocation();
            RProp.I.EASY_GUI_WINDOW_POSITION_X.set(position.x);
            RProp.I.EASY_GUI_WINDOW_POSITION_Y.set(position.y);
            Log.d(RProp.I.EASY_GUI_WINDOW_POSITION_X.get() + "");
        } else { // setLocationRelativeTo(null); 100 ms
            int x = RProp.I.EASY_GUI_WINDOW_POSITION_X.get();
            int y = RProp.I.EASY_GUI_WINDOW_POSITION_Y.get();

            setLocation(new Point(x, y));
            // RProp.I.EASY_GUI_WINDOW_POSITION_Y.get()));
        }

        if (apkScanner != null
                && (apkScanner.getLastErrorCode() != 0 || apkScanner.getApkInfo() == null)
                && !apkScanner.isCompleted(ApkScanner.STATUS_ALL_COMPLETED)) {
            Log.d("getlatestError is not 0 or args 0");
            mainpanel.showEmptyinfo();
        }

        Log.d("main End");
        Log.d("init UI   : " + (System.currentTimeMillis() - EasyMainUI.UIInittime) / 1000.0);

    }

    private void setdecoframe() {
        setUndecorated(true);
        // com.sun.awt.AWTUtilities.setWindowOpacity(this, 1.0f);
    }

    private void changeDeivce() {
        mainpanel.changeDevice(AndroidDebugBridge.getBridge().getDevices());
    }

    @Override
    public void deviceChanged(IDevice arg0, int arg1) {
        Log.d("deviceChanged");
        changeDeivce();

    }

    @Override
    public void deviceConnected(IDevice arg0) {
        Log.d("deviceConnected");
        changeDeivce();
    }

    @Override
    public void deviceDisconnected(IDevice arg0) {
        Log.d("deviceDisconnected");
        changeDeivce();

    }
}
