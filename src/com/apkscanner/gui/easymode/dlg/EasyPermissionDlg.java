package com.apkscanner.gui.easymode.dlg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.easymode.core.ToolEntry;
import com.apkscanner.gui.easymode.core.ToolEntryManager;
import com.apkscanner.gui.easymode.test.DragAndDropTest;
import com.apkscanner.gui.easymode.test.ReportingListTransferHandler;
import com.apkscanner.gui.easymode.util.ImageUtils;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class EasyPermissionDlg extends JDialog implements ActionListener {

	JButton btnCert;

	JList<String> jlist;
	JTextArea textArea;

	private String mCertSummary;
	private String[] mCertList;
	private String[] mCertFiles;
	private String apkFilePath;

	public EasyPermissionDlg(Frame frame, boolean modal, ApkInfo apkInfo) {
		super(frame, Resource.STR_TAB_SIGNATURES.getString(), modal);
		this.setSize(500, 500);
		// this.setPreferredSize(new Dimension(500, 500));
		this.setLocationRelativeTo(frame);
		this.setMinimumSize(new Dimension(500, 500));
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		// this.setResizable(false);

		setLayout(new BorderLayout());

		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource().equals(btnCert)) {

		}
	}
}
