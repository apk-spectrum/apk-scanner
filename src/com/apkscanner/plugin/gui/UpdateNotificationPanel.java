package com.apkscanner.plugin.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import com.apkscanner.plugin.IUpdateChecker;
import com.apkscanner.plugin.PlugInManager;
import com.apkscanner.plugin.PlugInPackage;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class UpdateNotificationPanel extends JPanel implements ListSelectionListener, ActionListener
{
	private static final long serialVersionUID = -1990492224827651449L;

	private static final String ACT_CMD_LAUNCH = "ACT_CMD_LAUNCH";

	private JTable updateList;
	private DefaultTableModel updateListModel;
	private JTextArea updateDescription;
	private JButton btnLaunch;

	public UpdateNotificationPanel() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new EmptyBorder(5,5,5,5));

		add(new JLabel("Update List"));

		updateListModel = new DefaultTableModel(new String[] { "Name", "Package", "Cur Ver.", "New Ver." }, 0) {
			private static final long serialVersionUID = 3925202106037646345L;
			private ArrayList<Object[]> dataList = new ArrayList<>();

			@Override
			public void addRow(Object[] data) {
				super.addRow(data);
				dataList.add(data);
			}

			@Override
			public Object getValueAt(int row, int col) {
				return dataList.get(row)[col];
			}
		};
		updateList = new JTable(updateListModel);
		updateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		updateList.getSelectionModel().addListSelectionListener(this);

		JScrollPane certListPanel = new JScrollPane(updateList);
		certListPanel.setPreferredSize(new Dimension(400,100));
		certListPanel.setAlignmentX(0.0f);
		add(certListPanel);

		add(new JLabel("Update description"));

		updateDescription = new JTextArea();
		updateDescription.setEditable(false);
		JScrollPane updateDescPanel = new JScrollPane(updateDescription);
		updateDescPanel.setPreferredSize(new Dimension(150,100));
		updateDescPanel.setAlignmentX(0.0f);

		add(updateDescPanel);

		btnLaunch = new JButton("Choose update");
		btnLaunch.setActionCommand(ACT_CMD_LAUNCH);
		btnLaunch.addActionListener(this);
		btnLaunch.setEnabled(false);
		
		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)) {
			private static final long serialVersionUID = -7930379247697419237L;
			@Override
            public Dimension getMaximumSize() {
                Dimension max = super.getMaximumSize();
                max.height = getPreferredSize().height;
                return max;
            }
        };
		btnPanel.setAlignmentX(0.0f);
		btnPanel.add(btnLaunch);

		add(btnPanel);
	}

	public void addUpdateList(IUpdateChecker[] list) {
		if(list == null || list.length == 0) return;
		for(IUpdateChecker plugin: list) {
			Map<?,?> version = plugin.getLatestVersionInfo();
			String target = plugin.getTargetPackageName();
			if(target == null || target.isEmpty()) continue;
			String label = target;
			String curVer = "";
			String newVer = (String)version.get("version");
			Object rawData = version.get("description");

			String desc = null;
			if(rawData instanceof String) {
				desc = (String) rawData;
			} else if(rawData instanceof Map<?,?>) {
				Map<?,?> descMap = (Map<?,?>) rawData;
				if(descMap.containsKey(PlugInManager.getLang())) {
					desc = (String) descMap.get(PlugInManager.getLang());
				} else if(descMap.containsKey("en")) {
					desc = (String) descMap.get("en");
				} else if(!descMap.isEmpty()) {
					desc = (String) descMap.values().toArray()[0];
				}
				Log.e(rawData.toString());
			}
			if(desc == null) {
				desc = "No information";
			}
			if("com.apkscanner".equals(target)) {
				label = Resource.STR_APP_NAME.getString();
				curVer = Resource.STR_APP_VERSION.getString();
			} else {
				PlugInPackage pack = PlugInManager.getPlugInPackage(target);
				label = pack.getLabel();
				curVer = pack.getVersionName();
			}
			Object[] data = new Object[] { label, target, curVer, newVer, desc, plugin };
			updateListModel.addRow(data);
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent evt) {
		if(evt.getValueIsAdjusting()) return;
		int row = updateList.getSelectedRow();
		if(row > -1) {
			updateDescription.setText(""+updateListModel.getValueAt(row, 4));
			IUpdateChecker plugin = (IUpdateChecker)updateListModel.getValueAt(row, 5);
			switch(plugin.getLaunchType()) {
			case IUpdateChecker.TYPE_LAUNCH_OPEN_LINK:
				btnLaunch.setText("Go to WebSite");
				break;
			case IUpdateChecker.TYPE_LAUNCH_DIRECT_UPDATE:
				btnLaunch.setText("Update");
				break;
			case IUpdateChecker.TYPE_LAUNCH_DOWNLOAD:
				btnLaunch.setText("Download");
				break;
			}
			btnLaunch.setEnabled(true);
		} else {
			updateDescription.setText("");
			btnLaunch.setText("Choose update");
			btnLaunch.setEnabled(false);
		}
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		String actCmd = evt.getActionCommand();
		if(actCmd == null) return;

		switch(actCmd) {
		case ACT_CMD_LAUNCH:
			int row = updateList.getSelectedRow();
			if(row > -1) {
				IUpdateChecker plugin = (IUpdateChecker)updateListModel.getValueAt(row, 5);
				plugin.launch();
			} else {
				updateDescription.setText("");
			}
			updateList.clearSelection();
			break;
		}
	}
}
