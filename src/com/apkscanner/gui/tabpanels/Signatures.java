package com.apkscanner.gui.tabpanels;

import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.gui.TabbedPanel.TabDataObject;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class Signatures extends JPanel implements ComponentListener, TabDataObject
{
	private static final long serialVersionUID = 4333997417315260023L;

	JList<String> jlist;
	JTextArea textArea;

	String mCertSummary;
	String[] mCertList;
	String[] mCertFiles;

	public Signatures() {
		//initialize();
	}

	@Override
	public void initialize()
	{
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

		add(splitPane);        

		ListSelectionListener listSelectionListener = new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent listSelectionEvent) {
				if(mCertList == null) return;
				if(jlist.getSelectedIndex() > -1) {
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
						File selFile = null;
						for(String path: mCertFiles) {
							if(path.endsWith(File.separator + fileName)) {
								Log.i("Select cert file : " + path);
								selFile = new File(path);
								break;
							}
						}
						if(selFile != null) {
							FileReader fr = null;
							BufferedReader inFile = null;
							String line;
							StringBuilder sb = new StringBuilder();
							try {
								fr = new FileReader(selFile);
								inFile = new BufferedReader(fr);
								while( (line = inFile.readLine()) != null ) {
									sb.append(line + "\n");
								}
								inFile.close();
								fr.close();
								textArea.setText(sb.toString());
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						} else {
							textArea.setText("fail read file : " + fileName);
						}
					}
					textArea.setCaretPosition(0);
				}
				//textArea.requestFocus();
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
						//Object o = theList.getModel().getElementAt(index);
						//Log.i("Double-clicked on: " + o.toString());
					}
				}
			}
		};
		jlist.addMouseListener(mouseListener);
	}

	@Override
	public void setData(ApkInfo apkInfo)
	{
		if(jlist == null)
			initialize();

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

		reloadResource();
		jlist.setSelectedIndex(0);
	}

	@Override
	public void setExtraData(ApkInfo apkInfo) { }

	@Override
	public void reloadResource()
	{
		if(jlist == null) return;

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
			labels[0] = Resource.STR_CERT_SUMMURY.getString();
			for(; i <= mCertList.length; i++) {
				labels[i] = Resource.STR_CERT_CERTIFICATE.getString() + "[" + i + "]";
			}
		} else if (mCertList.length == 1) {
			labels = new String[listSize];
			labels[0] = Resource.STR_CERT_CERTIFICATE.getString() + "[1]";
		} else {
			labels = new String[listSize];
		}

		if(mCertFiles != null) {
			for(String path: mCertFiles){
				labels[i++] = path.substring(path.lastIndexOf(File.separator)+1);
			}
		}

		jlist.setListData(labels);
	}

	@Override
	public void componentResized(ComponentEvent e) {

	}

	@Override
	public void componentMoved(ComponentEvent e) {

	}

	@Override
	public void componentShown(ComponentEvent e) {

	}

	@Override
	public void componentHidden(ComponentEvent e) {

	}
}

