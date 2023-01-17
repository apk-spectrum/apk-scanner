package com.apkscanner.gui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import com.apkscanner.Launcher;
import com.apkspectrum.core.scanner.ApkScanner;
import com.apkspectrum.logback.Log;
import com.apkspectrum.swing.ApkActionEventHandler;
import com.apkspectrum.swing.ApkFileChooser;

public class OpenApkAction extends AbstractApkScannerAction {
    private static final long serialVersionUID = 2760350430817651512L;

    public static final String ACTION_COMMAND = "ACT_CMD_OPEN_APK";

    public OpenApkAction(ApkActionEventHandler h) {
        super(h);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean withShift = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
        evtOpenApkFile(getWindow(e), withShift);
    }

    protected void evtOpenApkFile(Window owner, boolean newWindow) {
        final String apkFilePath = ApkFileChooser.openApkFilePath(owner);
        if (apkFilePath == null) {
            Log.v("Not choose apk file");
            return;
        }
        final ApkScanner scanner = getApkScanner();
        if (scanner == null) {
            Log.v("evtOpenApkFile() scanner is null");
            return;
        }
        if (!newWindow) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    scanner.clear(false);
                    scanner.openApk(apkFilePath);
                }
            });
            thread.setPriority(Thread.NORM_PRIORITY);
            thread.start();
        } else {
            Launcher.run(apkFilePath);
        }
    }
}
