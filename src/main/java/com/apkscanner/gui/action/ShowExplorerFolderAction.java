package com.apkscanner.gui.action;

import java.awt.event.ActionEvent;

import com.apkspectrum.core.scanner.ApkScanner;
import com.apkspectrum.data.apkinfo.ApkInfo;
import com.apkspectrum.swing.ApkActionEventHandler;
import com.apkspectrum.util.Log;
import com.apkspectrum.util.SystemUtil;

public class ShowExplorerFolderAction extends AbstractApkScannerAction {
    private static final long serialVersionUID = 7631745631480656014L;

    public static final String ACTION_COMMAND = "ACT_CMD_SHOW_EXPLORER_FOLDER";

    public ShowExplorerFolderAction(ApkActionEventHandler h) {
        super(h);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        evtShowExplorerFolder();
    }

    private void evtShowExplorerFolder() {
        ApkScanner scanner = getApkScanner();
        if (scanner == null) return;

        ApkInfo apkInfo = scanner.getApkInfo();
        if (apkInfo == null) {
            Log.e("evtShowExplorer() apkInfo is null");
            return;
        }

        SystemUtil.openFileExplorer(apkInfo.filePath);
    }
}
