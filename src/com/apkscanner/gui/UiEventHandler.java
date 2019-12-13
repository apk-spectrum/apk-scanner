package com.apkscanner.gui;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.apkscanner.Launcher;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.gui.PlugInDropTargetChooser.DefaultTargetObject;
import com.apkscanner.gui.action.AbstractApkScannerAction;
import com.apkscanner.gui.action.AbstractUIAction;
import com.apkscanner.gui.action.ActionEventHandler;
import com.apkscanner.gui.action.ChangeUiModeAction;
import com.apkscanner.gui.action.DeviceClearAppDataAction;
import com.apkscanner.gui.action.DeviceLaunchAppAction;
import com.apkscanner.gui.action.DeviceLaunchChooseAction;
import com.apkscanner.gui.action.DeviceLaunchMainAction;
import com.apkscanner.gui.action.DeviceShowInstalledPackageAction;
import com.apkscanner.gui.action.DeviceUninstallAppAction;
import com.apkscanner.gui.action.EditEasyToolbarAction;
import com.apkscanner.gui.action.EscKeyAction;
import com.apkscanner.gui.action.ExitAction;
import com.apkscanner.gui.action.InstallApkAction;
import com.apkscanner.gui.action.LoadResTreeImgFileAction;
import com.apkscanner.gui.action.NewWindowAction;
import com.apkscanner.gui.action.OpenApkAction;
import com.apkscanner.gui.action.OpenApkToNewAction;
import com.apkscanner.gui.action.OpenDecodeByteCodeAction;
import com.apkscanner.gui.action.OpenDecodeJADXGUIAction;
import com.apkscanner.gui.action.OpenDecodeJDGUIAction;
import com.apkscanner.gui.action.OpenDecompilerAction;
import com.apkscanner.gui.action.OpenPackageAction;
import com.apkscanner.gui.action.OpenPackageToNewAction;
import com.apkscanner.gui.action.OpenResTreeFileAction;
import com.apkscanner.gui.action.OpenSearcherAction;
import com.apkscanner.gui.action.OpenSettingsAction;
import com.apkscanner.gui.action.ShowAboutAction;
import com.apkscanner.gui.action.ShowCertDlgAction;
import com.apkscanner.gui.action.ShowExplorerAction;
import com.apkscanner.gui.action.ShowExplorerArchiveAction;
import com.apkscanner.gui.action.ShowExplorerFolderAction;
import com.apkscanner.gui.action.ShowLogsAction;
import com.apkscanner.gui.action.ShowManifestAction;
import com.apkscanner.gui.action.SignApkAction;
import com.apkscanner.gui.component.DropTargetChooser;
import com.apkscanner.gui.component.KeyStrokeAction;
import com.apkscanner.plugin.IExternalTool;
import com.apkscanner.plugin.IPlugIn;
import com.apkscanner.plugin.PlugInManager;
import com.apkscanner.util.ClassFinder;
import com.apkscanner.util.Log;

public class UiEventHandler extends ActionEventHandler implements WindowListener, DropTargetChooser.Listener
{
	public static final String APK_SCANNER_KEY	= AbstractApkScannerAction.APK_SCANNER_KEY;
	public static final String OWNER_WINDOW_KEY	= AbstractApkScannerAction.OWNER_WINDOW_KEY;

	public static final String ACT_CMD_CHANGE_UI_MODE			= ChangeUiModeAction.ACTION_COMMAND;
	public static final String ACT_CMD_CLEAR_APP_DATA			= DeviceClearAppDataAction.ACTION_COMMAND;
	public static final String ACT_CMD_LAUNCH_APP				= DeviceLaunchAppAction.ACTION_COMMAND;
	public static final String ACT_CMD_LAUNCH_CHOOSE_APP		= DeviceLaunchChooseAction.ACTION_COMMAND;
	public static final String ACT_CMD_LAUNCH_MAIN_APP			= DeviceLaunchMainAction.ACTION_COMMAND;
	public static final String ACT_CMD_SHOW_INSTALLED_PACKAGE_INFO = DeviceShowInstalledPackageAction.ACTION_COMMAND;
	public static final String ACT_CMD_UNINSTALL_APP			= DeviceUninstallAppAction.ACTION_COMMAND;
	public static final String ACT_CMD_EDIT_EASY_TOOLBAR		= EditEasyToolbarAction.ACTION_COMMAND;
	public static final String ACT_CMD_ESC_KEY_EVENT			= EscKeyAction.ACTION_COMMAND;
	public static final String ACT_CMD_EXIT						= ExitAction.ACTION_COMMAND;
	public static final String ACT_CMD_INSTALL_APK				= InstallApkAction.ACTION_COMMAND;
	public static final String ACT_CMD_NEW_WINDOW				= NewWindowAction.ACTION_COMMAND;
	public static final String ACT_CMD_OPEN_APK					= OpenApkAction.ACTION_COMMAND;
	public static final String ACT_CMD_OPEN_APK_TO_NEW			= OpenApkToNewAction.ACTION_COMMAND;
	public static final String ACT_CMD_OPEN_DECOMPILER_BYTECODE	= OpenDecodeByteCodeAction.ACTION_COMMAND;
	public static final String ACT_CMD_OPEN_DECOMPILER_JADXGUI	= OpenDecodeJADXGUIAction.ACTION_COMMAND;
	public static final String ACT_CMD_OPEN_DECOMPILER_JDGUI	= OpenDecodeJDGUIAction.ACTION_COMMAND;
	public static final String ACT_CMD_OPEN_DECOMPILER			= OpenDecompilerAction.ACTION_COMMAND;
	public static final String ACT_CMD_OPEN_PACKAGE				= OpenPackageAction.ACTION_COMMAND;
	public static final String ACT_CMD_OPEN_PACKAGE_TO_NEW		= OpenPackageToNewAction.ACTION_COMMAND;
	public static final String ACT_CMD_OPEN_SEARCHER			= OpenSearcherAction.ACTION_COMMAND;
	public static final String ACT_CMD_OPEN_SETTINGS			= OpenSettingsAction.ACTION_COMMAND;
	public static final String ACT_CMD_SHOW_ABOUT				= ShowAboutAction.ACTION_COMMAND;
	public static final String ACT_CMD_SHOW_SIGN_DLG			= ShowCertDlgAction.ACTION_COMMAND;
	public static final String ACT_CMD_SHOW_EXPLORER			= ShowExplorerAction.ACTION_COMMAND;
	public static final String ACT_CMD_SHOW_EXPLORER_ARCHIVE	= ShowExplorerArchiveAction.ACTION_COMMAND;
	public static final String ACT_CMD_SHOW_EXPLORER_FOLDER		= ShowExplorerFolderAction.ACTION_COMMAND;
	public static final String ACT_CMD_SHOW_LOGS				= ShowLogsAction.ACTION_COMMAND;
	public static final String ACT_CMD_SHOW_MANIFEST			= ShowManifestAction.ACTION_COMMAND;
	public static final String ACT_CMD_SIGN_APK					= SignApkAction.ACTION_COMMAND;
	public static final String ACT_CMD_OPEN_RESOURCE_FILE		= OpenResTreeFileAction.ACTION_COMMAND;
	public static final String ACT_CMD_LOAD_FS_IMG_FILE			= LoadResTreeImgFileAction.ACTION_COMMAND;

	public UiEventHandler(ApkScanner apkScanner) {
		setApkScanner(apkScanner);
		loadAllActions();
	}

	private void loadAllActions() {
		try {
			for(Class<?> cls : ClassFinder.getClasses(AbstractUIAction.class.getPackage().getName())) {
				if(cls.isMemberClass() || cls.isInterface()
					|| !AbstractUIAction.class.isAssignableFrom(cls)) continue;
				AbstractUIAction action = null;
				try {
					action = (AbstractUIAction) cls.getDeclaredConstructor(ActionEventHandler.class).newInstance(this);
				} catch (Exception e) { }
				if(action == null) {
					try {
						action = (AbstractUIAction) cls.getDeclaredConstructor().newInstance();
					} catch (Exception e1) { }
				}
				if(action != null) {
					addAction(action);
				}
			}
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	public void registerKeyStrokeAction(JComponent c) {
		// Shortcut key event processing
		KeyStrokeAction.registerKeyStrokeActions(c, JComponent.WHEN_IN_FOCUSED_WINDOW,
			new KeyStroke[] {
				KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0, false),
				KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0, false),
				KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0, false),
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false),
				KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK, false),
				KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK, false),
				KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK, false),
				KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK, false),
				KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK, false),
				KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK, false),
				KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK, false),
				KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK, false),
				KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK, false),
				KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, true),
				KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, true),
				KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, true)
			},
			new String[] {
				ACT_CMD_SHOW_ABOUT,
				ACT_CMD_CHANGE_UI_MODE,
				ACT_CMD_SHOW_LOGS,
				ACT_CMD_ESC_KEY_EVENT,
				ACT_CMD_OPEN_APK,
				ACT_CMD_OPEN_PACKAGE,
				ACT_CMD_NEW_WINDOW,
				ACT_CMD_INSTALL_APK,
				ACT_CMD_SHOW_INSTALLED_PACKAGE_INFO,
				ACT_CMD_SHOW_EXPLORER,
				ACT_CMD_SHOW_MANIFEST,
				ACT_CMD_LAUNCH_APP,
				ACT_CMD_EXIT,
				ACT_CMD_OPEN_APK,
				ACT_CMD_OPEN_PACKAGE,
				ACT_CMD_LAUNCH_CHOOSE_APP
			}, this);
	}

	public void setOwner(Window owner) {
		putData(OWNER_WINDOW_KEY, owner);
	}

	public Window getOwner() {
		return (Window) getData(OWNER_WINDOW_KEY);
	}

	public void setApkScanner(ApkScanner apkScanner) {
		putData(APK_SCANNER_KEY, apkScanner);
	}

	public ApkScanner getApkScanner() {
		return (ApkScanner) getData(APK_SCANNER_KEY);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String actCmd = e.getActionCommand();
		if(actCmd != null) {
			Action act = actionMap.get(actCmd);
			if(act != null) {
				act.actionPerformed(e);
				return;
			}

			IPlugIn plugin = PlugInManager.getPlugInByActionCommand(actCmd);
			if(plugin != null) {
				plugin.launch();
				return;
			}
		}
		Log.e("Unknown action command : " + actCmd);
	}

	// Drag & Drop event processing
	@Override
	public void filesDropped(Object dropedTarget, final File[] files) {
		final String[] filePaths = new String[files.length];
		for(int i = 0; i< files.length; i++) {
			try {
				filePaths[i] = files[i].getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		if(dropedTarget instanceof DefaultTargetObject) {
			switch((DefaultTargetObject)dropedTarget) {
			case DROPED_TARGET_APK_OPEN:
				Log.i("filesDropped()");
				final ApkScanner scanner = getApkScanner();
				if(scanner == null) return;

				Thread thread = new Thread(new Runnable() {
					public void run()
					{
						scanner.clear(false);
						scanner.openApk(filePaths[0]);
					}
				});
				thread.setPriority(Thread.NORM_PRIORITY);
				thread.start();
				break;
			case DROPED_TARGET_NEW_WIN:
				Launcher.run(filePaths[0]);
				break;
			}
		} else if(dropedTarget instanceof IExternalTool) {
			final ApkScanner scanner = getApkScanner();
			if(scanner == null) return;

			String apkPath = scanner.getApkInfo().filePath;
			((IExternalTool) dropedTarget).launch(apkPath, filePaths[0]);
		}
	}

	private void finished(AWTEvent e) {
		Log.v("finished()");

		Object source = e.getSource();
		if(source instanceof Component) {
			Window window = SwingUtilities.getWindowAncestor((Component) source);
			if(window != null) {
				window.setVisible(false);
			}
		}

		ApkScanner scanner = getApkScanner();
		if(scanner != null) {
			scanner.clear(true);
		}

		System.exit(0);
	}

	// Closing event of window be delete tempFile
	@Override public void windowClosing(WindowEvent e) { finished(e); }
	@Override public void windowClosed(WindowEvent e) { finished(e); }
	@Override public void windowOpened(WindowEvent e) { }
	@Override public void windowIconified(WindowEvent e) { }
	@Override public void windowDeiconified(WindowEvent e) { }
	@Override public void windowActivated(WindowEvent e) { }
	@Override public void windowDeactivated(WindowEvent e) { }
}
