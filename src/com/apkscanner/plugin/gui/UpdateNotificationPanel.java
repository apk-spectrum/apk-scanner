package com.apkscanner.plugin.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import com.apkscanner.plugin.IUpdateChecker;
import com.apkscanner.plugin.NetworkException;
import com.apkscanner.plugin.PlugInManager;
import com.apkscanner.plugin.PlugInPackage;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class UpdateNotificationPanel extends JPanel implements ListSelectionListener, ActionListener
{
	private static final long serialVersionUID = -1990492224827651449L;

	private static final String ACT_CMD_LAUNCH = "ACT_CMD_LAUNCH";
	private static final String ACT_CMD_CHECK_UPDATE = "ACT_CMD_CHECK_UPDATE";

	private JTable updateList;
	private DefaultTableModel updateListModel;
	private JTextArea updateDescription;
	private JButton btnLaunch;
	private JButton btnCheckUpdate;

	public UpdateNotificationPanel() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new EmptyBorder(5,5,5,5));

		add(new JLabel("Update List"));

		updateListModel = new DefaultTableModel(new String[] { "Name", "Package", "Cur Ver.", "New Ver.", "Last update check" }, 0) {
			private static final long serialVersionUID = 3925202106037646345L;
			private ArrayList<Object[]> dataList = new ArrayList<>();

			@Override
			public void addRow(Object[] data) {
				super.addRow(data);
				dataList.add(data);
			}

			@Override
			public void insertRow(int index, Object[] data) {
				super.insertRow(index, data);
				dataList.add(index, data);
			}

			@Override
			public void removeRow(int index) {
				super.removeRow(index);
				dataList.remove(index);
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
		
		btnCheckUpdate = new JButton("Check update");
		btnCheckUpdate.setActionCommand(ACT_CMD_CHECK_UPDATE);
		btnCheckUpdate.addActionListener(this);
		btnCheckUpdate.setEnabled(false);
		
		JPanel btnPanel = new JPanel() {
			private static final long serialVersionUID = -7930379247697419237L;
			@Override
            public Dimension getMaximumSize() {
                Dimension max = super.getMaximumSize();
                max.height = getPreferredSize().height;
                return max;
            }
        };
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.X_AXIS));
		btnPanel.setAlignmentX(0.0f);

		btnPanel.add(btnCheckUpdate);
		btnPanel.add(Box.createHorizontalGlue());
		btnPanel.add(btnLaunch);

		add(btnPanel);
	}
	
	private Object[] makeRowObject(IUpdateChecker plugin) {
		Map<?,?> version = plugin.getLatestVersionInfo();
		String target = plugin.getTargetPackageName();
		if(target == null || target.isEmpty()) return null;

		String label = target;
		String curVer = "";

		if("com.apkscanner".equals(target)) {
			label = Resource.STR_APP_NAME.getString();
			curVer = Resource.STR_APP_VERSION.getString();
		} else {
			PlugInPackage pack = PlugInManager.getPlugInPackage(target);
			label = pack.getLabel();
			curVer = pack.getVersionName();
		}

		String newVer = (version != null) ? (String)version.get("version") : curVer;
		Object rawData = (version != null) ? version.get("description") : null;
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
			Log.v(rawData.toString());
		}
		if(desc == null) {
			desc = "No information";
		}

		String checkDate = "";
		if(plugin.getLastUpdateDate() > 0) {
			checkDate = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(new Date(plugin.getLastUpdateDate()));
		}

		return new Object[] { label, target, curVer, newVer, checkDate, desc, plugin };
	}

	public void addUpdateList(IUpdateChecker[] list) {
		if(list == null || list.length == 0) return;
		for(IUpdateChecker plugin: list) {
			Object[] data = makeRowObject(plugin);
			if(data == null) continue;
			updateListModel.addRow(data);
		}
	}

	public void updatePluginState(final IUpdateChecker plugin) {
		if(plugin == null) return;
		int count = updateListModel.getRowCount();
		for(int i=0; i<count; i++) {
			if(plugin.equals(updateListModel.getValueAt(i, 6))) {
				Object[] data = makeRowObject(plugin);
				if(data == null) continue;
				updateListModel.removeRow(i);
				updateListModel.insertRow(i, makeRowObject(plugin));
				updateListModel.fireTableDataChanged();
				break;
			}
		}
	}

	public void checkUpdate(final IUpdateChecker plugin) {
        new SwingWorker<Boolean, Void>() {
			@Override
			protected Boolean doInBackground() throws Exception {
				try {
					return plugin.checkNewVersion();
				} catch (NetworkException e) {
					publish();
					if(e.isNetworkNotFoundException()) {
						Log.d("isNetworkNotFoundException");
					}
				}
				return false;
			}

			@Override
			protected void process(List<Void> arg0) {
				int ret = NetworkErrorDialog.show(UpdateNotificationPanel.this, plugin, true);
				switch(ret) {
				case NetworkErrorDialog.RESULT_RETRY:
					checkUpdate(plugin);
				default:
					updatePluginState(plugin);
					break;
				}
			}

			@Override
			protected void done() {
				Boolean result = false;
				try {
					result = get();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}

				updatePluginState(plugin);
				if(result) {
					PlugInManager.saveProperty();
				}
				
			}
		}.execute();
	}

	@Override
	public void valueChanged(ListSelectionEvent evt) {
		if(evt.getValueIsAdjusting()) return;
		int row = updateList.getSelectedRow();
		if(row > -1) {
			updateDescription.setText(""+updateListModel.getValueAt(row, 5));
			IUpdateChecker plugin = (IUpdateChecker)updateListModel.getValueAt(row, 6);
			if(plugin.hasNewVersion()) {
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
				btnLaunch.setText("No updated");
				btnLaunch.setEnabled(false);
			}
			btnCheckUpdate.setEnabled(true);
		} else {
			updateDescription.setText("");
			btnLaunch.setText("Choose update");
			btnLaunch.setEnabled(false);
			btnCheckUpdate.setEnabled(false);
		}
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		String actCmd = evt.getActionCommand();
		if(actCmd == null) return;

		int row = -1;
		switch(actCmd) {
		case ACT_CMD_LAUNCH:
			row = updateList.getSelectedRow();
			if(row > -1) {
				IUpdateChecker plugin = (IUpdateChecker)updateListModel.getValueAt(row, 6);
				plugin.launch();
			}
			updateList.clearSelection();
			break;
		case ACT_CMD_CHECK_UPDATE:
			row = updateList.getSelectedRow();
			if(row > -1) {
				IUpdateChecker plugin = (IUpdateChecker)updateListModel.getValueAt(row, 6);
				checkUpdate(plugin);
			}
			break;
		}
	}
}
