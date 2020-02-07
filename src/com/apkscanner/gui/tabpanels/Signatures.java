package com.apkscanner.gui.tabpanels;

import java.awt.BorderLayout;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.apkscanner.resource.RComp;
import com.apkscanner.resource.RStr;
import com.apkspectrum.core.scanner.ApkScanner;
import com.apkspectrum.data.apkinfo.ApkInfo;
import com.apkspectrum.data.apkinfo.ApkInfoHelper;
import com.apkspectrum.util.ZipFileUtil;

public class Signatures extends AbstractTabbedPanel implements ListSelectionListener
{
	private static final long serialVersionUID = 4333997417315260023L;

	JList<String> jlist;
	JTextArea textArea;

	private String mCertSummary;
	private String[] mCertList;
	private String[] mCertFiles;
	private String apkFilePath;

	public Signatures() {
		setTitle(RComp.TABBED_SIGNATURES);
		setTabbedEnabled(false);
	}

	@Override
	public void initialize()
	{
		setLayout(new BorderLayout());

		jlist = new JList<String>();
		jlist.addListSelectionListener(this);

		textArea = new JTextArea();
		textArea.setEditable(false);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		splitPane.setLeftComponent(new JScrollPane(jlist));
		splitPane.setRightComponent(new JScrollPane(textArea));
		splitPane.setDividerLocation(100);

		add(splitPane);
	}

	@Override
	public void setData(ApkInfo apkInfo, int status)
	{
		if(ApkScanner.STATUS_CERT_COMPLETED != status) {
			return;
		}

		if(jlist == null)
			initialize();

		apkFilePath = apkInfo.filePath;
		mCertList = apkInfo.certificates;
		mCertFiles = apkInfo.certFiles;
		mCertSummary = "";

		if(mCertList != null) {
			for(String sign: mCertList) {
				String[] line = sign.split("\n");
				if(line.length >= 3) {
					mCertSummary += line[0] + "\n" + line[1] + "\n" + line[2] + "\n\n";
				} else {
					mCertSummary += "error\n";
				}
			}
		}

		jlist.removeAll();
		if(mCertList == null) return;

		int listSize = mCertList.length;
		if(mCertFiles != null) {
			listSize += mCertFiles.length;
		}

		int i = 1;
		String[] labels;
		if(mCertList.length > 1) {
			listSize++;
			labels = new String[listSize];
			labels[0] = RStr.CERT_SUMMURY.get();
			for(; i <= mCertList.length; i++) {
				labels[i] = RStr.CERT_CERTIFICATE.get() + "[" + i + "]";
			}
		} else if (mCertList.length == 1) {
			labels = new String[listSize];
			labels[0] = RStr.CERT_CERTIFICATE.get() + "[1]";
		} else {
			labels = new String[listSize];
		}

		if(mCertFiles != null) {
			for(String path: mCertFiles){
				labels[i++] = path.substring(path.lastIndexOf("/")+1);
			}
		}

		jlist.setListData(labels);
		jlist.setSelectedIndex(0);

		setDataSize(ApkInfoHelper.isSigned(apkInfo) ? apkInfo.certificates.length : 0, true, false);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(mCertList == null || e.getValueIsAdjusting()
				|| jlist.getSelectedIndex() < 0)
			return;

		if(jlist.getSelectedIndex() == 0) {
			if(mCertList.length > 1) {
				textArea.setText(mCertSummary);
			} else {
				textArea.setText(mCertList[0]);
			}
		} else if(mCertList.length > 1 && jlist.getSelectedIndex() <= mCertList.length) {
			textArea.setText(mCertList[jlist.getSelectedIndex()-1]);
		} else {
			String fileName = jlist.getSelectedValue();
			String entryPath = null;

			for(String path: mCertFiles) {
				if(path.endsWith("/" + fileName)) {
					//Log.v("Select cert file : " + path);
					entryPath = path;
					break;
				}
			}
			byte[] buffer = ZipFileUtil.readData(apkFilePath, entryPath);
			if(buffer != null) {
				textArea.setText(new String(buffer));
			} else {
				textArea.setText("fail read file : " + fileName);
			}
		}
		textArea.setCaretPosition(0);
	}
}

