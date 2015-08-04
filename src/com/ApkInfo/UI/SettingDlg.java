package com.ApkInfo.UI;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JCheckBox;

import com.ApkInfo.Resource.Resource;

public class SettingDlg extends JDialog implements ActionListener{
	private static final long serialVersionUID = -854353051241196941L;

	private JTextField textframeworkResPath, textExcutePath;
	
	private String strExcuteEditorPath;
	private String strframeworkResPath;
	
	private String strLanguage;
	private String strSamePackage;
	
	JButton savebutton, exitbutton;
    JButton browser1,browser2;
	
    JComboBox<String> comboBox;
    JCheckBox chckbxNewCheckBox;
    
	SettingDlg() {
		readSettings();
	}
	
	private void readSettings()
	{
		strExcuteEditorPath = (String)Resource.PROP_EDITOR.getData();
		if(strExcuteEditorPath == null) {
			if(System.getProperty("os.name").indexOf("Window") >-1) {				
				strExcuteEditorPath = "notepad";
			} else {  //for linux
				strExcuteEditorPath = "gedit";
			}
			Resource.PROP_EDITOR.setData(strExcuteEditorPath);
		}
		
		strLanguage = (String)Resource.PROP_LANGUAGE.getData();
		if(strLanguage == null) {
			if(System.getProperty("user.language").indexOf("ko") > -1) {
				strLanguage = "ko";
			} else {
				strLanguage = "en";
			}
			Resource.PROP_LANGUAGE.setData(strLanguage);
		}
		
		strSamePackage = (String)Resource.PROP_CHECK_INSTALLED.getData();
		if(strSamePackage == null) {
			strSamePackage = "true";
			Resource.PROP_CHECK_INSTALLED.setData(strSamePackage);
		}
		
		strframeworkResPath = (String)Resource.PROP_FRAMEWORK_RES.getData();
		if(strframeworkResPath == null) {
			strframeworkResPath = "";
			Resource.PROP_FRAMEWORK_RES.setData(strframeworkResPath);
		}
	}
	
	private void saveSettings()
	{
		Resource.PROP_EDITOR.setData(strExcuteEditorPath);
		Resource.PROP_LANGUAGE.setData(strLanguage);
		Resource.PROP_CHECK_INSTALLED.setData(strSamePackage);
		Resource.PROP_FRAMEWORK_RES.setData(strframeworkResPath);
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
		textExcutePath.setText(strExcuteEditorPath);
		
		textExcutePath.setBounds(111, 10, 293, 25);
		panel.add(textExcutePath);		
		
		textframeworkResPath = new JTextField(20);
		if(strframeworkResPath != null && !strframeworkResPath.equals("null"))
			textframeworkResPath.setText(strframeworkResPath);
		else
			textframeworkResPath.setText("");
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
	    
	    comboBox = new JComboBox<String>();
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
				@SuppressWarnings("unused")
				SettingDlg dlg = new SettingDlg();
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	
		if(e.getSource() == savebutton) {
			System.out.println("save");
			
			if((new File(textExcutePath.getText().trim()).exists())) {
				strExcuteEditorPath = textExcutePath.getText().trim();					
			} else {
				textExcutePath.setText(strExcuteEditorPath);
			}
			if((new File(textframeworkResPath.getText().trim()).exists())) {
				strframeworkResPath = textframeworkResPath.getText().trim();					
			} else {
				textframeworkResPath.setText(strframeworkResPath);
			}
			
			strSamePackage = String.valueOf(chckbxNewCheckBox.isSelected());
			strLanguage = (String)comboBox.getSelectedItem();
			
			saveSettings();
			
		} else if(e.getSource() == exitbutton) {
			System.out.println("exit");
			this.dispose();
			
			
		} else if(e.getSource() == browser1) {
			JFileChooser jfc = new JFileChooser();										
			jfc.showOpenDialog(null);
			File dir = jfc.getSelectedFile();
			if(dir!=null) {
				strExcuteEditorPath = dir.getPath();
				textExcutePath.setText(dir.getPath()); 
			}
		} else if(e.getSource() == browser2) {
			JFileChooser jfc = new JFileChooser();			
			jfc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("apk","apk"));							
			jfc.showOpenDialog(null);
			File dir = jfc.getSelectedFile();			
			if(dir!=null) {
				strframeworkResPath = dir.getPath();
				textframeworkResPath.setText(dir.getPath());
			}
		}
	}
}
