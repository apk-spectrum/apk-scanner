package com.ApkInfo.UI;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import com.ApkInfo.Core.MyDeviceInfo;
import com.ApkInfo.UIUtil.ButtonType;
import com.ApkInfo.UIUtil.StandardButton;
import com.ApkInfo.UIUtil.Theme;


class ADBDialog extends Dialog implements ActionListener 
{
	
	StandardButton btnshowDeiveInfo;
	StandardButton btnrefresh;
	Frame mainui;
	
	Label AppInfo;
	Label DeviceInfo;	
	Panel textPanel;
	
	MyDeviceInfo mMyDeviceInfo;
	ArrayList<MyDeviceInfo.Device> DeviceList;
	
	JTable table;
	
	public ADBDialog(Frame f1) {
		super(f1, "ADB Install", true);
		mainui = f1;
		setLayout(new BorderLayout());
		
		Panel ButtonPanel = new Panel(new GridLayout(1,2));
		
		table = new JTable(new MyTableModel());
		
	    ListSelectionModel cellSelectionModel = table.getSelectionModel();
	    
	    cellSelectionModel.addListSelectionListener(new ListSelectionListener() {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			// TODO Auto-generated method stub
			
		}
	  });
		
	    JScrollPane scrollPane = new JScrollPane(table);
	    
		ButtonPanel.add(btnshowDeiveInfo = new StandardButton("Refresh Device List",Theme.GRADIENT_LIGHTBLUE_THEME,ButtonType.BUTTON_ROUNDED));
		ButtonPanel.add(btnrefresh = new StandardButton("설치 및 Device 정보",Theme.GRADIENT_LIGHTBLUE_THEME,ButtonType.BUTTON_ROUNDED));
		
        WindowAdapter wa = new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                System.exit(0);
            }
        };
        this.addWindowListener(wa);
		
        //textPanel = new Panel(new GridLayout(1,2));
        
		setResizable( false );
		btnshowDeiveInfo.addActionListener(this);
		btnrefresh.addActionListener(this);
		
		textPanel.add(AppInfo = new Label("App Infomation"));
		textPanel.add(DeviceInfo = new Label("Device Information"));
		//add(textPanel, BorderLayout.NORTH);
		//add(ButtonPanel,BorderLayout.SOUTH);
		
		add(scrollPane,BorderLayout.NORTH);
		
		setSize(400,300);
		this.setLocation(MainUI.nPositionX+100, MainUI.nPositionY+100);	
		
		mMyDeviceInfo = new MyDeviceInfo();
		
		DeviceList = mMyDeviceInfo.DeviceList;
	}
	public void actionPerformed(ActionEvent e)
	{		
		
		String str=e.getActionCommand();
		
		if(str == "Refresh Device List") {
			System.out.println("click  :" + str);
			setVisible(false);
		} else if(str == "설치 및 Device 정보") {
			System.out.println("click  :" + str);	
		}		
	}
	public void showPlease() {
		setVisible(true);
		
	}
  class MyTableModel extends AbstractTableModel {
	    private String[] columnNames = { "Class", "Type",
        "Startup" };


    public int getColumnCount() {
      return columnNames.length;
    }

    public int getRowCount() {
      return DeviceList.size();
    }

    public String getColumnName(int col) {
      return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
      return table.getValueAt(row, col);
    }

    /*
     * JTable uses this method to determine the default renderer/ editor for
     * each cell. If we didn't implement this method, then the last column
     * would contain text ("true"/"false"), rather than a check box.
     */
    public Class getColumnClass(int c) {
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
		  
public static void main(final String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {	
				//args = file path					
				Frame window = null;
				ADBDialog dlg = new ADBDialog(window);
				
				dlg.showPlease();
				
			}
		});
	}
}

