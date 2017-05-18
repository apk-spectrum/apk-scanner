package com.apkscanner.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;

import javax.swing.JDialog;

import com.apkscanner.resource.Resource;
import com.apkscanner.tool.adb.PackageInfo;

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

	public void setPackageInfo(PackageInfo info) {		
		infoPanel.setPackageInfo(info);
	}

}
