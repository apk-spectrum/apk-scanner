package com.apkscanner.gui.action;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

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
import com.apkscanner.util.ConsolCmd.ConsoleOutputObserver;
import com.apkscanner.util.Log;
import com.apkscanner.util.ZipFileUtil;

@SuppressWarnings("serial")
public class OpenDecompilerAction extends AbstractApkScannerAction
{
	public static final String ACTION_COMMAND = "ACT_CMD_OPEN_DECOMPILER";

	public OpenDecompilerAction(ActionEventHandler h) { super(h); }

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source instanceof JMenuItem) {
			source = ((JMenuItem)source).getParent();
			if(source instanceof JPopupMenu) {
				source = ((JPopupMenu) source).getClientProperty(RConst.MENU_OWNER_KEY);
			}
		}
		evtOpenDecompiler(getWindow(e),
				source instanceof Component ? (Component)source : null);
	}

	protected void evtOpenDecompiler(final Window owner, final Component comp) {
		if(!hasCode(owner)) return;

		String data = RProp.S.DEFAULT_DECORDER.get();
		Log.v("PROP_DEFAULT_DECORDER : " + data);

		if(data.matches(".*!.*#.*@.*")) {
			if(evtPluginLaunch(data)) return;
			data = (String) RProp.DEFAULT_DECORDER.getDefaultValue();
		}

		switch(data) {
		case RConst.STR_DECORDER_JD_GUI:
			launchJdGui(owner, comp);
			break;
		case RConst.STR_DECORDER_JADX_GUI:
			launchJadxGui(owner, comp);
			break;
		case RConst.STR_DECORDER_BYTECOD:
			launchByteCodeViewer(owner, comp);
			break;
		default:
		}
	}

	protected boolean hasCode(final Window owner) {
		ApkInfo apkInfo = getApkInfo();
		if(apkInfo == null || apkInfo.filePath == null
				|| !new File(apkInfo.filePath).exists()) {
			Log.e("evtOpenDecompiler() apkInfo is null");
			MessageBoxPool.show(owner, MessageBoxPool.MSG_NO_SUCH_APK_FILE);
			return false;
		}

		if(!ZipFileUtil.exists(apkInfo.filePath, "classes.dex")) {
			Log.e("No such file : classes.dex");
			MessageBoxPool.show(owner, MessageBoxPool.MSG_NO_SUCH_CLASSES_DEX);
			return false;
		}
		return true;
	}

	protected void launchJdGui(final Window owner, final Component comp) {
		ApkInfo apkInfo = getApkInfo();
		setComponentEnabled(comp, false);
		String jarfileName = apkInfo.tempWorkPath + File.separator + (new File(apkInfo.filePath)).getName().replaceAll("\\.apk$", ".jar");
		Dex2JarWrapper.convert(apkInfo.filePath, jarfileName, comp == null
				? null : new Dex2JarWrapper.DexWrapperListener() {
			@Override
			public void onCompleted() {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						if(comp != null) comp.setEnabled(true);
					}
				});
			}

			@Override
			public void onError(final String message) {
				Log.e("Failure: Fail Dex2Jar : " + message);
				setComponentEnabled(comp, true);
			}

			@Override
			public void onSuccess(String jarFilePath) {
				JDGuiLauncher.run(jarFilePath);
			}
		});
	}

	protected void launchJadxGui(final Window owner, final Component comp) {
		ApkInfo apkInfo = getApkInfo();
		setComponentEnabled(comp, false);
		JADXLauncher.run(apkInfo.filePath, comp == null
				? null : new ConsoleOutputObserver() {
			@Override
			public boolean ConsolOutput(String output) {
				if(output.startsWith("INFO")) {
					setComponentEnabled(comp, true);
				}
				return true;
			}
		});
	}

	protected void launchByteCodeViewer(final Window owner, final Component comp) {
		ApkInfo apkInfo = getApkInfo();
		setComponentEnabled(comp, false);
		BytecodeViewerLauncher.run(apkInfo.filePath, comp == null
				? null : new ConsoleOutputObserver() {
			@Override
			public boolean ConsolOutput(String output) {
				if(output.startsWith("Start up") || output.startsWith("I:")) {
					setComponentEnabled(comp, true);
				}
				return true;
			}
		});
	}

	protected boolean evtPluginLaunch(String actionCommand) {
		IPlugIn plugin = PlugInManager.getPlugInByActionCommand(actionCommand);
		if(plugin == null) return false;
		plugin.launch();
		return true;
	}

	protected void setComponentEnabled(final Component comp, final boolean enabled) {
		if(comp == null) return;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				comp.setEnabled(enabled);
			}
		});
	}
}