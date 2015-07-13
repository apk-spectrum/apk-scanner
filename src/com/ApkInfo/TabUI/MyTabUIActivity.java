package com.ApkInfo.TabUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.ApkInfo.Resource.Resource;

public class MyTabUIActivity extends JPanel {
	private static final long serialVersionUID = 8325900007802212630L;

	private JTextArea textArea;
	private JPanel IntentPanel;
	private JLabel IntentLabel;
	
	private MyTableModel TableModel; 
	private JTable table;
  
	public ArrayList<Object[]> ActivityList = new ArrayList<Object[]>();
  	  
	public MyTabUIActivity() {
		super(new GridLayout(2, 0));

		TableModel = new MyTableModel();
		table = new JTable(TableModel) {
			
			public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
			 Component c = super.prepareRenderer(tcr, row, column);
			 Color temp = null;
			 
			 if(isRowSelected(row)) {
		          //c.setForeground(getSelectionForeground());
		          c.setBackground(Color.GRAY);
		        }else{
					if("activity".equals((String) ActivityList.get(row)[1])) {
						String tempstr = (String)ActivityList.get(row)[0];
						if(tempstr.indexOf("LAUNCHER") != -1) {
							temp = new Color(0x5D9657);
						} else {
							temp = new Color(0xB7F0B1);						
						}
					} else if("service".equals((String) ActivityList.get(row)[1])) {
						temp = new Color(0xB2CCFF);
					} else if("receiver".equals((String) ActivityList.get(row)[1])) {
						temp = new Color(0xCEF279);
					} else {
						temp = new Color(0xFFE08C);
					}
					c.setBackground(temp);
		        }
		        return c;
		      }
		};

		ListSelectionModel cellSelectionModel = table.getSelectionModel();
    
		cellSelectionModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(ActivityList == null) return;
				if(table.getSelectedRow() > -1) {
					textArea.setText((String) ActivityList.get(table.getSelectedRow())[3]);
				}
			}
		});
	
		setJTableColumnsWidth(table, 500, 80,10,10);
		//Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);

		textArea = new JTextArea();
		textArea.setEditable(false);
		JScrollPane scrollPane2 = new JScrollPane(textArea);
		//scrollPane2.setPreferredSize(new Dimension(300, 500));

		IntentPanel = new JPanel();
		IntentLabel = new JLabel(Resource.STR_ACTIVITY_LABEL_INTENT.getString());

		IntentPanel.setLayout(new BorderLayout());

		//IntentLabel.setPreferredSize(new Dimension(300, 100));
		IntentPanel.add(IntentLabel, BorderLayout.NORTH);
		IntentPanel.add(scrollPane2, BorderLayout.CENTER);

		//Add the scroll pane to this panel.
		add(scrollPane);
		add(IntentPanel);
	}
	
	public void setData(ArrayList<Object[]> data)
	{
		ActivityList.clear();
		ActivityList.addAll(data);
		TableModel.fireTableDataChanged();
	}
	
	public void reloadResource()
	{
		TableModel.loadResource();
		TableModel.fireTableStructureChanged();
		setJTableColumnsWidth(table, 500, 80,10,10);
		IntentLabel.setText(Resource.STR_ACTIVITY_LABEL_INTENT.getString());
	}

	public void setJTableColumnsWidth(JTable table, int tablePreferredWidth,
										double... percentages) {
		double total = 0;
		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			total += percentages[i];
		}
	 
		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			TableColumn column = table.getColumnModel().getColumn(i);
			column.setPreferredWidth((int)(tablePreferredWidth * (percentages[i] / total)));
		}
	}

	class MyTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 5291910634830167294L;

		private String[] columnNames = null;

		public MyTableModel()
		{
			loadResource();
		}

		public void loadResource()
		{
			columnNames = new String[] {
				Resource.STR_ACTIVITY_COLUME_CLASS.getString(),
				Resource.STR_ACTIVITY_COLUME_TYPE.getString(),
				Resource.STR_ACTIVITY_COLUME_STARTUP.getString()
			};
		}
		
		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return ActivityList.size();
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return ActivityList.get(row)[col];
		}

		/*
		 * JTable uses this method to determine the default renderer/ editor for
		 * each cell. If we didn't implement this method, then the last column
		 * would contain text ("true"/"false"), rather than a check box.
		 */
		public Class<? extends Object> getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		/*
		 * Don't need to implement this method unless your table's editable.
		 */
		public boolean isCellEditable(int row, int col) {
			//Note that the data/cell address is constant,
			//no matter where the cell appears onscreen.
			return true;
		}
	}
}