package com.apkscanner.gui.action;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.MessageBoxPool;
import com.apkscanner.gui.tabpanels.TreeNodeData;
import com.apkscanner.resource.RComp;
import com.apkscanner.resource.RConst;
import com.apkscanner.resource.RProp;
import com.apkscanner.tool.BytecodeViewerLauncher;
import com.apkscanner.tool.Dex2JarWrapper;
import com.apkscanner.tool.JADXLauncher;
import com.apkscanner.tool.JDGuiLauncher;
import com.apkspectrum.plugin.IPlugIn;
import com.apkspectrum.plugin.PlugInManager;
import com.apkspectrum.swing.ExtensionButton;
import com.apkspectrum.util.Log;
import com.apkspectrum.util.ZipFileUtil;
import com.apkspectrum.util.ConsolCmd.ConsoleOutputObserver;

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

	protected String getTargetPath(Window owner, Component comp) {
		// from OpenResTreeFileAction
		String target = (String) handler.getData(Integer.toString(comp.hashCode()));

		// from SelectViewPanel
		if(target == null && comp instanceof JComponent) {
			TreeNodeData resObj = (TreeNodeData)((JComponent)comp).getClientProperty(TreeNodeData.class);
			target = uncompressRes(resObj);
		}

		if(target == null) {
			if(!hasCode(owner)) return null;
			ApkInfo apkInfo = getApkInfo();
			if(apkInfo != null) {
				target = apkInfo.filePath;
			}
		}
		return target;
	}

	protected void launchJdGui(final Window owner, final Component comp) {
		String targetPath = getTargetPath(owner, comp);
		if(targetPath == null) return;

		ApkInfo apkInfo = getApkInfo();
		String jarfileName = targetPath;
		if(!jarfileName.startsWith(apkInfo.tempWorkPath)) {
			jarfileName = apkInfo.tempWorkPath + File.separator + (new File(targetPath)).getName();
		}
		jarfileName = jarfileName.replaceAll("\\.(apk|dex)$", ".jar");

		setComponentEnabled(comp, false);
		Dex2JarWrapper.convert(targetPath, jarfileName, comp == null
				? null : new Dex2JarWrapper.DexWrapperListener() {
			private boolean completed;
			@Override
			public void onCompleted() {
				if(completed) return;
				setComponentEnabled(comp, true);
				completed = true;
			}

			@Override
			public void onError(final String message) {
				if(completed) return;
				Log.e("Failure: Fail Dex2Jar : " + message);
				setComponentEnabled(comp, true);
				completed = true;
			}

			@Override
			public void onSuccess(String jarFilePath) {
				JDGuiLauncher.run(jarFilePath);
			}
		});
	}

	protected void launchJadxGui(final Window owner, final Component comp) {
		String targetPath = getTargetPath(owner, comp);
		if(targetPath == null) return;
		setComponentEnabled(comp, false);
		JADXLauncher.run(getTargetPath(owner, comp), comp == null
				? null : new ConsoleOutputObserver() {
			private boolean completed;
			@Override
			public boolean ConsolOutput(String output) {
				if(completed) return true;
				if(output.startsWith("INFO")
						|| output.equals("JADXLauncher Completed")) {
					setComponentEnabled(comp, true);
					completed = true;
				}
				return true;
			}
		});
	}

	protected void launchByteCodeViewer(final Window owner, final Component comp) {
		String targetPath = getTargetPath(owner, comp);
		if(targetPath == null) return;
		setComponentEnabled(comp, false);
		BytecodeViewerLauncher.run(targetPath, comp == null
				? null : new ConsoleOutputObserver() {
			private boolean completed;
			@Override
			public boolean ConsolOutput(String output) {
				if(completed) return true;
				if(output.startsWith("Start up") || output.startsWith("I:")
						|| output.equals("BytecodeViewerLauncher Completed")) {
					setComponentEnabled(comp, true);
					completed = true;
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
				if(comp instanceof ExtensionButton) {
					if(!enabled) {
						RComp.BTN_TOOLBAR_OPEN_CODE_LODING.set(comp);
					}
				}
			}
		});
	}
}