package com.apkscanner.gui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import com.apkscanner.Launcher;
import com.apkscanner.gui.dialog.PackageTreeDlg;
import com.apkspectrum.core.scanner.ApkScanner;
import com.apkspectrum.logback.Log;
import com.apkspectrum.swing.ApkActionEventHandler;

public class OpenPackageAction extends AbstractApkScannerAction {
    private static final long serialVersionUID = 7804901664854984928L;

    public static final String ACTION_COMMAND = "ACT_CMD_OPEN_PACKAGE";

    public OpenPackageAction(ApkActionEventHandler h) {
        super(h);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean withShift = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
        evtOpenPackage(getWindow(e), withShift);
    }

    protected void evtOpenPackage(Window owner, boolean newWindow) {
        PackageTreeDlg packDlg = new PackageTreeDlg(owner);
        if (packDlg.showTreeDlg() != PackageTreeDlg.APPROVE_OPTION) {
            Log.v("Not choose package");
            return;
        }
        final ApkScanner scanner = getApkScanner();
        if (scanner == null) {
            Log.v("evtOpenApkFile() scanner is null");
            return;
        }

        final String device = packDlg.getSelectedDevice();
        final String apkFilePath = packDlg.getSelectedApkPath();
        final String frameworkRes = packDlg.getSelectedFrameworkRes();

        if (!newWindow) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    scanner.clear(false);
                    scanner.openPackage(device, apkFilePath, frameworkRes);
                }
            });
            thread.setPriority(Thread.NORM_PRIORITY);
            thread.start();
        } else {
            Launcher.run(device, apkFilePath, frameworkRes);
        }
    }
}
