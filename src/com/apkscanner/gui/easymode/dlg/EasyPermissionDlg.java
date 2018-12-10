package com.apkscanner.gui.easymode.dlg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import com.apkscanner.core.scanner.PermissionGroupManager;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.PermissionGroup;
import com.apkscanner.data.apkinfo.PermissionInfo;
import com.apkscanner.data.apkinfo.UsesPermissionInfo;
import com.apkscanner.gui.easymode.core.ToolEntry;
import com.apkscanner.gui.easymode.core.ToolEntryManager;
import com.apkscanner.gui.easymode.test.ColumnGroup;
import com.apkscanner.gui.easymode.test.DragAndDropTest;
import com.apkscanner.gui.easymode.test.ReportingListTransferHandler;
import com.apkscanner.gui.easymode.test.headtable.GroupableTableHeader;
import com.apkscanner.gui.easymode.util.ImageUtils;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;
import com.sun.xml.internal.ws.util.StringUtils;

public class EasyPermissionDlg extends JDialog implements ActionListener {

	JButton btngetDeviceGranted;
	JLabel sdkversion;
	
	JTabbedPane tabbedpane;
	private JTable usestable;
	private JTable declaredtable;
	
	ArrayList<String> protectionLevelCalumn = new ArrayList<String>();
	
	private String apkFilePath;
	private TableRowSorter<PermissionUsesTableModel> sorter;
	
	
	
	ArrayList<Object[]> usesTableData = new ArrayList<Object[]>();
//			Arrays.asList(data));
	    
	PermissionUsesTableModel model;
	

	public EasyPermissionDlg(Frame frame, boolean modal, ApkInfo apkInfo) {
		super(frame, Resource.STR_BASIC_PERMISSIONS.getString(), modal);
		this.setSize(700, 700);
		// this.setPreferredSize(new Dimension(500, 500));
		this.setLocationRelativeTo(frame);
		this.setMinimumSize(new Dimension(500, 500));
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setLayout(new BorderLayout());
		
		// this.setResizable(false);
//	    protectionLevelCalumn.add("priv");
//	    protectionLevelCalumn.add("sig");
//	    protectionLevelCalumn.add("nomal");
//	    protectionLevelCalumn.add("xxxxx");
		
		makeusesPermissionRow(apkInfo);
        sorter = new TableRowSorter<PermissionUsesTableModel>(model);
        usestable = new JTable(model) {
	      protected JTableHeader createDefaultTableHeader() {
	          return new GroupableTableHeader(columnModel);
	      }
	    };
	    makeProtectionColumn();
		
        usestable.setRowSorter(sorter);
        usestable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        usestable.setRowHeight(36);
        setColumnWidths(usestable, 36,350);
        
        JScrollPane scrollPane = new JScrollPane(usestable);
		
		
		btngetDeviceGranted = new JButton("permission granted on device");
		sdkversion =  new JLabel("@SDK 27");
		tabbedpane = new JTabbedPane();
		
		tabbedpane.addTab("Uses", null, scrollPane,
		                  "Uses");
		JPanel panel2 = new JPanel();
		tabbedpane.addTab("Declared", null, panel2,
		                  "Declared");
		
		add(tabbedpane, BorderLayout.CENTER);
		
		JPanel temppanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		temppanel.add(btngetDeviceGranted);
		temppanel.add(sdkversion);
		
		add(temppanel, BorderLayout.NORTH);
		
		this.setVisible(true);
	}
	
	private void makeusesPermissionRow(ApkInfo apkInfo) {
		// TODO Auto-generated method stub
		PermissionGroupManager manager = new PermissionGroupManager(apkInfo.manifest.usesPermission);
		
		if(apkInfo.manifest.usesPermission != null) {
			
			protectionLevelCalumn.add("Base");
			protectionLevelCalumn.add("Flag");
			
			Object[] obj = new Object[protectionLevelCalumn.size()+3];
			
			int j= 0;
			obj[j++] = "icon";
			obj[j++] = "name";
			
			for(String str : protectionLevelCalumn) {				
				obj[j++] = str;
			}
			obj[j] = "granted";
			model = new PermissionUsesTableModel(obj);

			for(UsesPermissionInfo info: apkInfo.manifest.usesPermission) {
				PermissionGroup group = manager.getPermissionGroup(info.permissionGroup);				
				try {
//					Log.d(protectionLevelCalumn.size() + "");
					obj = new Object[protectionLevelCalumn.size()+3];
					
					String[] permissions = info.protectionLevel.split("\\|");
					
					j= 0;
					obj[j++] = new ImageIcon(new URL(group.icon)) {
						public String toString() {
							return "";
						}
					};
					obj[j++] = info.name;
					int i = 0;
						
					obj[j+i++] = permissions[0];							
					String Flagstr = "";
					for(int k = 1; k< permissions.length; k++) {
						Flagstr += permissions[k] + ((k==permissions.length-1)?"":" | ");
					}
					
					obj[j+i++] = Flagstr;
					
					obj[j+i] = new Boolean(true);
					
					model.add(obj);
					
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void setColumnWidths(JTable table, int... widths) {
		TableColumnModel columnModel = table.getColumnModel();
		for (int i = 0; i < widths.length; i++) {
			if (i < columnModel.getColumnCount()) {
				switch(i) {
				case 0:
					columnModel.getColumn(i).setMaxWidth(widths[i]);
					break;
				default:
						
				}
				columnModel.getColumn(i).setMinWidth(widths[i]);
				columnModel.getColumn(i).setWidth(widths[i]);
			} else
				break;
		}
	}
	
	private void makeProtectionColumn() {
	    final int START_PROTECTION_INDEX = 2;
		TableColumnModel cm = usestable.getColumnModel();
	    ColumnGroup g_protection = new ColumnGroup("Protection Level");
	    	    
	    for(int i= 0; i<protectionLevelCalumn.size(); i++) {
	    	Log.d((START_PROTECTION_INDEX + i ) + "");
	    	
	    	g_protection.add(cm.getColumn(START_PROTECTION_INDEX + i));
	    }
	    
	    GroupableTableHeader header = (GroupableTableHeader)usestable.getTableHeader();
	    header.addColumnGroup(g_protection);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource().equals(btngetDeviceGranted)) {

		}
	}
	
    class PermissionUsesTableModel extends AbstractTableModel {
        private Object[] columnNames;

        PermissionUsesTableModel(Object[] column) {
        	columnNames = column;
        }
        
        public int getColumnCount() {
            return columnNames.length;
        }
        public void add(Object[] obj) {
        	usesTableData.add(obj);
        }
        
        public int getRowCount() {
            return usesTableData.size();
        }

		public String getColumnName(int col) {
			return columnNames[col].toString();			
        }
 
        public Object getValueAt(int row, int col) {
        	
            return usesTableData.get(row)[col];
        }
 
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }
 
        public boolean isCellEditable(int row, int col) {
                return false;
        }
 
        public void setValueAt(Object value, int row, int col) {
        	usesTableData.get(row)[col] = value;
            fireTableCellUpdated(row, col);
        }
    }        
}
