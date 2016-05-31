package com.apkscanner.gui.util;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.PatternSyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import com.apkscanner.apkinfo.ApkInfo;
import com.apkscanner.core.AaptWrapper;
import com.apkscanner.util.Log;

public class SearchDlg extends JDialog {
	public String sName;
	private ApkInfo apkinfo;
	private ArrayList<TableData> data = new ArrayList<TableData>();
	final JTextField name;
	AllTableModel allTableModel;
	JTable allTable;
	public SearchDlg() {
		setBounds(100, 100, 500, 500);
		setTitle("Input Dialog");
		setLocationRelativeTo(null);
		getContentPane().setLayout(null);
		
		setLayout(new BorderLayout());
		// Create Input 
		name = new JTextField();
		
		//name.setBounds(57, 36, 175, 20);
		getContentPane().add(name,BorderLayout.NORTH);
		
		getContentPane().add(makeTable(),BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		
		// Button OK
		JButton btnOK = new JButton("OK");
		btnOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sName = name.getText();
				new Thread(new Runnable() {
					public void run()
					{
						searchString();
					}
				}).start();
				//dispose();
			}
		});
		//btnOK.setBounds(70, 93, 78, 23);
		buttonPanel.add(btnOK);
		
		// Button Cancel
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sName = "";
				dispose();
			}
		});
		//btnCancel.setBounds(158, 93, 74, 23);
		buttonPanel.add(btnCancel);
		
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
	}
	
	private JPanel makeTable() {
		allTableModel = new AllTableModel(data);
		JPanel panel = new JPanel(new BorderLayout());
		allTable = new JTable(allTableModel);
		JScrollPane scroll = new JScrollPane(allTable);
		panel.add(scroll);
		return panel;
	}
	
	private void searchString() {
		String findStr =name.getText();
	    	    
	    String[] filelist = apkinfo.images;
	    String temp = new String();
	    
	    data.clear();
	    
	    for(int i=0; i<filelist.length; i++) {
	    	if(filelist[i].endsWith(".png")) continue;
	    	
	    	if(filelist[i].startsWith("res/") || filelist[i].equals("AndroidManifest.xml")) {
				String[] xmlbuffer = AaptWrapper.Dump.getXmltree(apkinfo.filePath, new String[] {filelist[i]});
				StringBuilder sb = new StringBuilder();
				for(String s: xmlbuffer) sb.append(s+"\n");
				temp = sb.toString();
	    	} else if(filelist[i].endsWith(".txt") || filelist[i].endsWith(".mk") 
						|| filelist[i].endsWith(".html") || filelist[i].endsWith(".js") || filelist[i].endsWith(".css") || filelist[i].endsWith(".json")
						|| filelist[i].endsWith(".props") || filelist[i].endsWith(".properties")) {
					ZipFile zipFile = null;
					try {
						zipFile = new ZipFile(apkinfo.filePath);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ZipEntry entry = zipFile.getEntry(filelist[i]);
					byte[] buffer = new byte[(int) entry.getSize()];
					try {
						zipFile.getInputStream(entry).read(buffer);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					temp = new String(buffer);
				} else {
					continue;
				}
	    	
	    	
		    int lineNumber = 1;       // 행 번호
		    try {
		    	
		      ////////////////////////////////////////////////////////////////
		    	Scanner scanner = new Scanner(temp);
		    	//System.out.println(filelist[i]);
		    	
		    	BufferedReader reader = new BufferedReader(new StringReader(temp));
		    	
		    	String line;
		    	try {
					while ((line = reader.readLine()) != null) {
					  //String line = scanner.nextLine();
					  // process the line
					  if (line.indexOf(findStr) !=-1)
					      System.out.format("%3d: %s%n", lineNumber, line);
					  	  data.add(new TableData(data.size(),filelist[i],lineNumber,line));
					  	  allTableModel.fireTableDataChanged();
					  	  
					  	  lineNumber++; // 행 번호 증가
					  }
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	
		      ////////////////////////////////////////////////////////////////
			    } catch (PatternSyntaxException e) { // 정규식에 에러가 있다면
			        System.err.println(e);
			        System.exit(1);
			    }
	    	}
	}
	
	public void setApkInfo(ApkInfo apkinfo) {
		this.apkinfo = apkinfo;
		Log.d(""+this.apkinfo);
	}
	
	
	class TableData {
	    private int Index;
	    private String path;
	    private int line;
	    private String findstring;

	    public TableData(int Index, String path, int line, String findstring) {
	        super();
	        this.Index = Index;
	        this.path = path;
	        this.line = line;
	        this.findstring = findstring;
	    }
	    
	    public int getCount() {
	        return Index;
	    }
	    
	    public String getPath() {
	        return path;
	    }
	    public int getLine() {
	        return line;
	    }	    
	    public String getfindString() {
	        return findstring;
	    }	    
	}
	
	class AllTableModel extends AbstractTableModel {

	    ArrayList<TableData> tableData;

	    Object[] columnNames = {"Index", "path", "line", ""};

	    public AllTableModel(ArrayList<TableData> data) {

	        tableData = data;
	    }

	    public ArrayList<TableData> getTableData() {
	        return tableData;
	    }

	    @Override
	    public String getColumnName(int column) {
	        return columnNames[column].toString();
	    }

	    @Override
	    public int getColumnCount() {
	        return columnNames.length;
	    }

	    @Override
	    public int getRowCount() {
	        return tableData.size();
	    }

	    @Override
	    public Object getValueAt(int rowIndex, int columnIndex) {
	        TableData data = tableData.get(rowIndex);
	        switch (columnIndex) {
	        case 0:
	            return data.getCount();
	        case 1:
	            return data.getPath();
	        case 2:
	            return data.getLine();
	        case 3:
	            return data.getfindString();
	        default:
	            return null;
	        }
	    }

	}
}

