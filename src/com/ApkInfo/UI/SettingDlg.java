package com.ApkInfo.UI;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.JCheckBox;

public class SettingDlg extends JDialog implements ActionListener{
	private JTextField textframeworkResPath, textExcutePath;
	
	static private String strExcuteEditorPath;
	static private String strframeworkResPath;
	
	static private String strLanguage;
	static private String strSamePackage;
	
	JButton savebutton, exitbutton;
    JButton browser1,browser2;
	
    JComboBox comboBox;
    JCheckBox chckbxNewCheckBox;
    
	SettingDlg() {
		readSettingInfoFromFile();
		
	}
	
	public String getExcuteEditorPath() {
		return strExcuteEditorPath;
	}
	public String getframeworkResPath() {
		return strframeworkResPath;
	}
	public String getLanguage() {
		return strLanguage;
	}
	public String strSamePackage() {
		return strSamePackage;
	}
	
	public void readSettingInfoFromFile() {
		// TODO Auto-generated method stub
		File f=new File("setting.txt");
		if(f.exists()) {
			System.out.println("found setting.txt");			
			FileReader fileReader;
			try {
				fileReader = new FileReader(f);
				BufferedReader reader = new BufferedReader(fileReader);				
				String line = null;
				int index=0;
				try {
					while ((line = reader.readLine()) != null) {						
						switch(index) {
						case 0:
							strExcuteEditorPath = line;						
							break;
						case 1:
							strLanguage = line;
							break;
						case 2:
							strSamePackage = line;
							break;
						case 3:
							strframeworkResPath = line;
							break;						
						}
						index ++;
					}
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		} else {
			System.out.println("not found setting.txt... make file...");
			if(System.getProperty("os.name").indexOf("Window") >-1) {				
				strExcuteEditorPath = "notepad";
			} else {  //for linux
				strExcuteEditorPath = "gedit";
			}
			if(System.getProperty("user.language").indexOf("ko") > -1) {
				strLanguage = "ko";
			} else {
				strLanguage = "en";
			}
			strSamePackage = "true";
			strframeworkResPath = "null";			

			WriteSettingInfoToFile();
		}		
	}
	public void WriteSettingInfoToFile() {
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter("setting.txt"));
			writer.write(strExcuteEditorPath + "\n");
			writer.write(strLanguage + "\n");
			writer.write(strSamePackage + "\n");
			writer.write(strframeworkResPath + "\n");
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void makeDialog() {
		this.setTitle("Setting");
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setSize(new Dimension(480,210));
		this.setResizable( false );
		this.setLocationRelativeTo(null);
		this.setModal(true);
		getContentPane().add(makeLayoutPanel());
		//this.pack();
		//dlgDialog.setLocationRelativeTo(null);
		this.setVisible(true);
		//readSettingInfoFromFile();
		//readSettingInfoFromFile();
	}
	
	JPanel makeLayoutPanel() {
		JPanel panel = new JPanel();		

		panel.setLayout(null);

		JLabel userLabel = new JLabel("change Editor : ");
		userLabel.setBounds(10, 10, 116, 25);
		panel.add(userLabel);

		textExcutePath = new JTextField(20);
		textExcutePath.setText(this.strExcuteEditorPath);
		
		textExcutePath.setBounds(111, 10, 293, 25);
		panel.add(textExcutePath);		
		
		textframeworkResPath = new JTextField(20);
		textframeworkResPath.setText(this.strframeworkResPath);
		textframeworkResPath.setBounds(111, 40, 293, 25);
	    panel.add(textframeworkResPath);

		JLabel frameworkLabel = new JLabel("framework res  : ");
		frameworkLabel.setBounds(10, 40, 114, 25);
		panel.add(frameworkLabel);
		
		savebutton = new JButton("저장");
		savebutton.setBounds(288, 145, 80, 25);
		savebutton.addActionListener(this);
		savebutton.setFocusable(false);
		panel.add(savebutton);
		
		
		exitbutton = new JButton("종료");
		exitbutton.setBounds(380, 145, 80, 25);
		exitbutton.addActionListener(this);
		exitbutton.setFocusable(false);
		panel.add(exitbutton);

	    
	    browser1 = new JButton("...");
	    browser1.setBounds(405, 10, 44, 25);
	    browser1.addActionListener(this);
	    browser1.setFocusable(false);
	    panel.add(browser1);
	    
	    browser2 = new JButton("...");
	    browser2.setBounds(405, 40, 44, 25);
	    browser2.addActionListener(this);
	    browser2.setFocusable(false);
	    panel.add(browser2);
	    
	    chckbxNewCheckBox = new JCheckBox("설치시 동일 패키지 팝업 유무");
	    
	    if(strSamePackage.indexOf("true") >-1) {
	    	chckbxNewCheckBox.setSelected(true);
	    } else {
	    	chckbxNewCheckBox.setSelected(false);
	    }
	    
	    chckbxNewCheckBox.addActionListener(this);
	    chckbxNewCheckBox.setBounds(10, 73, 236, 25);
	    panel.add(chckbxNewCheckBox);
	    
	    JLabel label = new JLabel("언어");
	    label.setBounds(52, 106, 32, 25);
	    panel.add(label);
	    
	    comboBox = new JComboBox();
	    comboBox.setBounds(87, 109, 94, 24);
	    
	    comboBox.addItem("ko");
	    comboBox.addItem("en");
	    
	    comboBox.setSelectedItem(strLanguage);
	    
	    panel.add(comboBox);	
	    
		return panel;
	}
	
public static void main(final String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				SettingDlg dlg = new SettingDlg();
			}
		});
	}

@Override
public void actionPerformed(ActionEvent e) {
	// TODO Auto-generated method stub
	
		if(e.getSource() == savebutton) {
			System.out.println("save");
			
			this.strExcuteEditorPath = textExcutePath.getText();
			this.strframeworkResPath = textframeworkResPath.getText();
			
			this.strSamePackage = String.valueOf(chckbxNewCheckBox.isSelected());
			this.strLanguage = (String)comboBox.getSelectedItem();
			
			
			WriteSettingInfoToFile();
		} else if(e.getSource() == exitbutton) {
			System.out.println("exit");
			this.dispose();
			
			
		} else if(e.getSource() == browser1) {
			JFileChooser jfc = new JFileChooser();										
			jfc.showOpenDialog(null);
			File dir = jfc.getSelectedFile();
			if(dir!=null) {
				this.strExcuteEditorPath = dir.getPath();
				textExcutePath.setText(dir.getPath()); 
			}
		} else if(e.getSource() == browser2) {
			JFileChooser jfc = new JFileChooser();			
			jfc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("apk","apk"));							
			jfc.showOpenDialog(null);
			File dir = jfc.getSelectedFile();			
			if(dir!=null) {
				this.strframeworkResPath = dir.getPath();
				textframeworkResPath.setText(dir.getPath());
			}
		}
		
	}
}
