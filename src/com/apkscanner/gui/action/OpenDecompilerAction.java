package com.apkscanner.gui.action;

import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;

import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.messagebox.MessageBoxPool;
import com.apkscanner.plugin.IPlugIn;
import com.apkscanner.plugin.PlugInManager;
import com.apkscanner.resource.RConst;
import com.apkscanner.resource.RProp;
import com.apkscanner.tool.external.BytecodeViewerLauncher;
import com.apkscanner.tool.external.Dex2JarWrapper;
import com.apkscanner.tool.external.JADXLauncher;
import com.apkscanner.tool.external.JDGuiLauncher;
import com.apkscanner.util.Log;
import com.apkscanner.util.ZipFileUtil;

@SuppressWarnings("serial")
public class OpenDecompilerAction extends AbstractApkScannerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_OPEN_DECOMPILER";

	public OpenDecompilerAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		evtOpenDecompiler(getWindow(e));
	}

	private void evtOpenDecompiler(final Window owner) {
		ApkInfo apkInfo = getApkInfo();
		if(apkInfo == null || apkInfo.filePath == null
				|| !new File(apkInfo.filePath).exists()) {
			Log.e("evtOpenJDGUI() apkInfo is null");
			MessageBoxPool.show(owner, MessageBoxPool.MSG_NO_SUCH_APK_FILE);
			return;
		}

		if(!ZipFileUtil.exists(apkInfo.filePath, "classes.dex")) {
			Log.e("No such file : classes.dex");
			MessageBoxPool.show(owner, MessageBoxPool.MSG_NO_SUCH_CLASSES_DEX);
			return;
		}

		String data = RProp.S.DEFAULT_DECORDER.get();
		Log.v("PROP_DEFAULT_DECORDER : " + data);

		if(data.matches(".*!.*#.*@.*")) {
			if(evtPluginLaunch(data)) return;
			data = (String) RProp.DEFAULT_DECORDER.getDefaultValue();
		}

		switch(data) {
		case RConst.STR_DECORDER_JD_GUI:
			//toolBar.setEnabledAt(ButtonSet.OPEN_CODE, false);
			String jarfileName = apkInfo.tempWorkPath + File.separator + (new File(apkInfo.filePath)).getName().replaceAll("\\.apk$", ".jar");
			Dex2JarWrapper.convert(apkInfo.filePath, jarfileName, new Dex2JarWrapper.DexWrapperListener() {
				@Override
				public void onCompleted() {
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							//toolBar.setEnabledAt(ButtonSet.OPEN_CODE, true);
						}
					});
				}

				@Override
				public void onError(final String message) {
					Log.e("Failure: Fail Dex2Jar : " + message);
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							MessageBoxPool.show(owner, MessageBoxPool.MSG_FAILURE_DEX2JAR, message);
						}
					});
				}

				@Override
				public void onSuccess(String jarFilePath) {
					JDGuiLauncher.run(jarFilePath);
				}
			});
			break;
		case RConst.STR_DECORDER_JADX_GUI:
			JADXLauncher.run(apkInfo.filePath);
			break;
		case RConst.STR_DECORDER_BYTECOD:
			BytecodeViewerLauncher.run(apkInfo.filePath);
			break;
		default:
		}
	}

	private boolean evtPluginLaunch(String actionCommand) {
		IPlugIn plugin = PlugInManager.getPlugInByActionCommand(actionCommand);
		if(plugin == null) return false;
		plugin.launch();
		return true;
	}

}