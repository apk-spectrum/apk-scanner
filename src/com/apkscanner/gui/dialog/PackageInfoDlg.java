package com.apkscanner.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.apkscanner.Launcher;
import com.apkscanner.core.installer.ApkInstaller;
import com.apkscanner.core.installer.ApkInstaller.ApkInstallerListener;
import com.apkscanner.core.signer.Signature;
import com.apkscanner.core.signer.SignatureReport;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.ComponentInfo;
import com.apkscanner.gui.messagebox.ArrowTraversalPane;
import com.apkscanner.gui.messagebox.ComboMessageBox;
import com.apkscanner.gui.messagebox.JTextOptionPane;
import com.apkscanner.gui.theme.TabbedPaneUIManager;
import com.apkscanner.gui.util.ApkFileChooser;
import com.apkscanner.gui.util.JHtmlEditorPane;
import com.apkscanner.gui.util.JHtmlEditorPane.HyperlinkClickListener;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.adb.AdbDeviceHelper;
import com.apkscanner.tool.adb.SimpleOutputReceiver;
import com.apkscanner.tool.adb.PackageInfo;
import com.apkscanner.tool.adb.PackageManager;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.FileUtil.FSStyle;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;

public class PackageInfoDlg extends JDialog {
	private static final long serialVersionUID = 7654892270063010429L;

	private PackageInfoPanel infoPanel;
	
	public PackageInfoDlg(Window owner) {
		super(owner);
		initialize(owner);
	}

	private void initialize(Window window)
	{
		setTitle("Package Info");
		setIconImage(Resource.IMG_APP_ICON.getImageIcon().getImage());
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(true);
		setModal(false);
		setLayout(new BorderLayout());
		setSize(new Dimension(500, 400));
		setLocationRelativeTo(window);
		
		infoPanel = new PackageInfoPanel();
		
		add(infoPanel, BorderLayout.CENTER);
	}

	private JButton getToolbarButton(String text, Icon icon, String tooltip, String actCommand) {
		JButton button = new JButton(icon);
		button.setToolTipText(tooltip);
		button.setBorderPainted(false);
		button.setOpaque(false);
		button.setFocusable(false);
		button.setActionCommand(actCommand);
		button.setVerticalTextPosition(JLabel.BOTTOM);
		button.setHorizontalTextPosition(JLabel.CENTER);
		//button.setPreferredSize(new Dimension(43,45));
		return button;
	}

	public void setPackageInfo(PackageInfo info) {		
		infoPanel.setPackageInfo(info);
	}

	public void showFeatureInfo(String id) {
		infoPanel.showFeatureInfo(id);
	}

}
