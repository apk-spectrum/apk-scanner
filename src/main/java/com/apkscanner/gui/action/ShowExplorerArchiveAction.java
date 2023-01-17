package com.apkscanner.gui.action;

import java.awt.event.ActionEvent;

import com.apkspectrum.core.scanner.ApkScanner;
import com.apkspectrum.data.apkinfo.ApkInfo;
import com.apkspectrum.logback.Log;
import com.apkspectrum.swing.ApkActionEventHandler;
import com.apkspectrum.util.SystemUtil;

public class ShowExplorerArchiveAction extends AbstractApkScannerAction {
    private static final long serialVersionUID = 8568658309453447667L;

    public static final String ACTION_COMMAND = "ACT_CMD_SHOW_EXPLORER_ARCHIVE";

    public ShowExplorerArchiveAction(ApkActionEventHandler h) {
        super(h);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        evtShowExplorerArchive();
    }

    private void evtShowExplorerArchive() {
        ApkScanner scanner = getApkScanner();
        if (scanner == null) return;

        ApkInfo apkInfo = scanner.getApkInfo();
        if (apkInfo == null) {
            Log.e("evtShowExplorer() apkInfo is null");
            return;
        }

        SystemUtil.openArchiveExplorer(apkInfo.filePath);
    }
}
