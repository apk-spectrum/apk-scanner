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

import javafx.scene.control.Button;

public class EasyToolbarCertDlg extends JDialog implements ActionListener {

	JButton btnCert;

	JList<String> jlist;
	JTextArea textArea;

	private String mCertSummary;
	private String[] mCertList;
	private String[] mCertFiles;
	private String apkFilePath;

	public EasyToolbarCertDlg(Frame frame, boolean modal, ApkInfo apkInfo) {
		super(frame, Resource.STR_TAB_SIGNATURES.getString(), modal);
		this.setSize(500, 500);
		// this.setPreferredSize(new Dimension(500, 500));
		this.setLocationRelativeTo(frame);
		this.setMinimumSize(new Dimension(500, 500));
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		// this.setResizable(false);

		setLayout(new BorderLayout());

		jlist = new JList<String>();
		JScrollPane scrollPane1 = new JScrollPane(jlist);

		textArea = new JTextArea();
		textArea.setEditable(false);
		final JScrollPane scrollPane2 = new JScrollPane(textArea);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		splitPane.setLeftComponent(scrollPane1);
		splitPane.setRightComponent(scrollPane2);
		splitPane.setDividerLocation(100);

		
		btnCert = new JButton(Resource.STR_TITLE_APK_SIGNER.getString());
		btnCert.addActionListener(this);
		JPanel temppanel = new JPanel();
		temppanel.add(btnCert);
		add(temppanel, BorderLayout.SOUTH);
		add(splitPane, BorderLayout.CENTER);

		ListSelectionListener listSelectionListener = new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent listSelectionEvent) {
				if (mCertList == null)
					return;
				if (jlist.getSelectedIndex() > -1) {
					if (jlist.getSelectedIndex() == 0) {
						if (mCertList.length > 1) {
							textArea.setText(mCertSummary);
						} else {
							textArea.setText(mCertList[0]);
						}
					} else if (mCertList.length > 1 && jlist.getSelectedIndex() <= mCertList.length) {
						textArea.setText(mCertList[jlist.getSelectedIndex() - 1]);
					} else {
						String fileName = jlist.getSelectedValue();
						String entryPath = null;

						for (String path : mCertFiles) {
							if (path.endsWith("/" + fileName)) {
								Log.i("Select cert file : " + path);
								entryPath = path;
								break;
							}
						}
						if (apkFilePath != null && entryPath != null) {
							ZipFile zipFile = null;
							InputStream is = null;
							try {
								zipFile = new ZipFile(apkFilePath);
								ZipEntry entry = zipFile.getEntry(entryPath);
								byte[] buffer = new byte[(int) entry.getSize()];
								is = zipFile.getInputStream(entry);
								is.read(buffer);
								textArea.setText(new String(buffer));
							} catch (IOException e) {
								e.printStackTrace();
							} finally {
								if (is != null) {
									try {
										is.close();
									} catch (IOException e) {
									}
								}
								if (zipFile != null) {
									try {
										zipFile.close();
									} catch (IOException e) {
									}
								}
							}
						} else {
							textArea.setText("fail read file : " + fileName);
						}
					}
					textArea.setCaretPosition(0);
				}
				// textArea.requestFocus();
			}
		};
		jlist.addListSelectionListener(listSelectionListener);

		MouseListener mouseListener = new MouseAdapter() {
			@SuppressWarnings("unchecked")
			public void mouseClicked(MouseEvent mouseEvent) {
				JList<String> theList = (JList<String>) mouseEvent.getSource();
				if (mouseEvent.getClickCount() == 2) {
					int index = theList.locationToIndex(mouseEvent.getPoint());
					if (index >= 0) {
						// Object o = theList.getModel().getElementAt(index);
						// Log.i("Double-clicked on: " + o.toString());
					}
				}
			}
		};

		jlist.addMouseListener(mouseListener);

		apkFilePath = apkInfo.filePath;
		mCertList = apkInfo.certificates;
		mCertFiles = apkInfo.certFiles;
		mCertSummary = "";

		if (mCertList != null) {
			for (String sign : mCertList) {
				String[] line = sign.split("\n");
				if (line.length >= 3) {
					mCertSummary += line[0] + "\n" + line[1] + "\n" + line[2] + "\n\n";
				} else {
					mCertSummary += "error\n";
				}
			}
		}

		reloadResource();
		jlist.setSelectedIndex(0);

		this.setVisible(true);
	}

	private void reloadResource() {
		
		if (jlist == null)
			return;

		jlist.removeAll();
		if (mCertList == null)
			return;

		int listSize = mCertList.length;
		if (mCertFiles != null) {
			listSize += mCertFiles.length;
		}

		int i = 1;
		String[] labels;
		if (mCertList.length > 1) {
			listSize++;
			labels = new String[listSize];
			labels[0] = Resource.STR_CERT_SUMMURY.getString();
			for (; i <= mCertList.length; i++) {
				labels[i] = Resource.STR_CERT_CERTIFICATE.getString() + "[" + i + "]";
			}
		} else if (mCertList.length == 1) {
			labels = new String[listSize];
			labels[0] = Resource.STR_CERT_CERTIFICATE.getString() + "[1]";
		} else {
			labels = new String[listSize];
		}

		if (mCertFiles != null) {
			for (String path : mCertFiles) {
				labels[i++] = path.substring(path.lastIndexOf("/") + 1);
			}
		}

		jlist.setListData(labels);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource().equals(btnCert)) {
			ToolEntryManager.excuteSinerDlg(this);
		}
	}
}
