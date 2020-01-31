package com.apkspectrum.plugin.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

import com.apkspectrum.plugin.IUpdateChecker;
import com.apkspectrum.plugin.NetworkException;
import com.apkspectrum.plugin.PlugInManager;
import com.apkspectrum.plugin.PlugInPackage;
import com.apkspectrum.resource._RStr;
import com.apkspectrum.util.Log;

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

		add(new JLabel(_RStr.LABEL_UPDATE_LIST.get()));

		updateListModel = new DefaultTableModel(new String[] {
				_RStr.COLUMN_NAME.get(),
				_RStr.COLUMN_PACKAGE.get(),
				_RStr.COLUMN_THIS_VERSION.get(),
				_RStr.COLUMN_NEW_VERSION.get(),
				_RStr.COLUMN_LAST_CHECKED_DATE.get() }, 0) {
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

		add(new JLabel(_RStr.LABEL_DESCRIPTION.get()));

		updateDescription = new JTextArea();
		updateDescription.setEditable(false);
		JScrollPane updateDescPanel = new JScrollPane(updateDescription);
		updateDescPanel.setPreferredSize(new Dimension(150,100));
		updateDescPanel.setAlignmentX(0.0f);

		add(updateDescPanel);

		btnLaunch = new JButton(_RStr.BTN_CHOOSE_UPDATE.get());
		btnLaunch.setActionCommand(ACT_CMD_LAUNCH);
		btnLaunch.addActionListener(this);
		btnLaunch.setEnabled(false);

		btnCheckUpdate = new JButton(_RStr.BTN_CHECK_UPDATE.get());
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

		if(PlugInManager.getAppPackage().equals(target)) {
			label = PlugInManager.getAppTitle();
			curVer = PlugInManager.getAppVersion();
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
			desc = _RStr.MSG_NO_UPDATE_INFO.get();
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
		int row = updateList.getSelectedRow();
		if(row < 0) {
			updateList.addRowSelectionInterval(0, 0);
		}
	}

	public void updatePluginState(final IUpdateChecker plugin) {
		if(plugin == null) return;
		int count = updateListModel.getRowCount();
		for(int i=0; i<count; i++) {
			if(plugin.equals(updateListModel.getValueAt(i, 6))) {
				Object[] data = makeRowObject(plugin);
				if(data == null) continue;
				boolean selected = updateList.isRowSelected(i);
				updateListModel.removeRow(i);
				updateListModel.insertRow(i, makeRowObject(plugin));
				updateListModel.fireTableDataChanged();
				if(selected) {
					updateList.addRowSelectionInterval(i, i);
				}
				break;
			}
		}
	}

	public void checkUpdate(final IUpdateChecker plugin) {
        new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				try {
					plugin.checkNewVersion();
				} catch (NetworkException e) {
					publish();
					if(e.isNetworkNotFoundException()) {
						Log.d("isNetworkNotFoundException");
					}
				}
				return null;
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
				updatePluginState(plugin);
				PlugInManager.saveProperty();
			}
		}.execute();
	}

	@Override
	public void valueChanged(ListSelectionEvent evt) {
		if(evt.getValueIsAdjusting()) return;
		int row = updateList.getSelectedRow();
		if(row > -1) {
			updateDescription.setText((String) updateListModel.getValueAt(row, 5));
			updateDescription.setCaretPosition(0);
			IUpdateChecker plugin = (IUpdateChecker)updateListModel.getValueAt(row, 6);
			if(plugin.hasNewVersion()) {
				switch(plugin.getLaunchType()) {
				case IUpdateChecker.TYPE_LAUNCH_OPEN_LINK:
					btnLaunch.setText(_RStr.BTN_GO_TO_WEBSITE.get());
					break;
				case IUpdateChecker.TYPE_LAUNCH_DIRECT_UPDATE:
					btnLaunch.setText(_RStr.BTN_UPDATE.get());
					break;
				case IUpdateChecker.TYPE_LAUNCH_DOWNLOAD:
					btnLaunch.setText(_RStr.BTN_DOWNLOAD.get());
					break;
				}
				btnLaunch.setEnabled(true);
			} else {
				switch(plugin.getLaunchType()) {
				case IUpdateChecker.TYPE_LAUNCH_OPEN_LINK:
					btnLaunch.setText(_RStr.BTN_GO_TO_WEBSITE.get());
					btnLaunch.setEnabled(true);
					break;
				case IUpdateChecker.TYPE_LAUNCH_DIRECT_UPDATE:
				case IUpdateChecker.TYPE_LAUNCH_DOWNLOAD:
					btnLaunch.setText(_RStr.BTN_NO_UPDATED.get());
					btnLaunch.setEnabled(false);
					break;
				}
			}
			btnCheckUpdate.setEnabled(true);
		} else {
			updateDescription.setText("");
			btnLaunch.setText(_RStr.BTN_CHOOSE_UPDATE.get());
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
			//updateList.clearSelection();
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
