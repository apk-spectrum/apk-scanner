package com.apkscanner.gui.dialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import com.apkscanner.resource.Resource;

public class PermissionHistoryPanel extends JPanel implements ItemListener {
	private static final long serialVersionUID = -3567803690045423840L;

	private JTable permTable;
	private PermissionTableModel permModel;
	private ArrayList<Object[]> permList = new ArrayList<Object[]>();
	
	public PermissionHistoryPanel() {
		setLayout(new GridBagLayout());

		//GridBagConstraints(int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty, int anchor, int fill, Insets insets, int ipadx, int ipady) 
		GridBagConstraints gridConst = new GridBagConstraints(0,0,1,1,1.0f,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(5,5,5,5),0,0);

		JPanel sdkSelectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		sdkSelectPanel.add(new JLabel("SDK Ver. "));

		JComboBox<Integer> sdkVersions = new JComboBox<Integer>(new Integer[] {3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28});
		sdkVersions.setRenderer(new ListCellRenderer<Integer>() {
			JLabel label;
			@Override
			public Component getListCellRendererComponent(JList<? extends Integer> list, Integer value, int index,
					boolean isSelected, boolean cellHasFocus) {
				if(label == null) label = new JLabel();
				label.setText(value > 0 ? "API Level " + value : "API Levels");
				return label;
			}
		});
		sdkVersions.addItemListener(this);
		sdkSelectPanel.add(sdkVersions);
		add(sdkSelectPanel, gridConst);

		JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		filterPanel.add(new JLabel("▶ Filter : All permission of used in Package"));

		gridConst.gridy++;
		add(filterPanel, gridConst);
		
		gridConst.gridy++;
		gridConst.weighty = 1.0f;

		permTable = new JTable(permModel = new PermissionTableModel());
		add(new JScrollPane(permTable), gridConst);

		
		setPreferredSize(new Dimension(500,600));

	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		//int value = (int)arg0.getItem();
		//setSdkVersion(value);
	}

	class PermissionTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 9177174072480438788L;

		private String[] columnNames = null;

		PermissionTableModel() {
			loadResource();
		}

		public void loadResource() {
			columnNames = new String[] {
				"",
				"icon",
				"name",
				"label",
				"Protection Level"
			};
		}

		@Override
		public int getRowCount() {
			return permList.size();
		}

		@Override
		public String getColumnName(int col) {
			return columnNames[col];
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public Object getValueAt(int row, int column) {
			return permList.get(row)[column];
		}
	}

	public static void main(String args[]) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel((String)Resource.PROP_CURRENT_THEME.getData());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException e1) {
					e1.printStackTrace();
				}

				PermissionHistoryPanel history = new PermissionHistoryPanel();
				JFrame frame = new JFrame();
				frame.add(history);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.pack();
				frame.setVisible(true);
			}
		});
	}

}
