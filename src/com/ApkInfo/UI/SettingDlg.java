package com.ApkInfo.UI;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.KeyStroke;

import com.ApkInfo.Resource.Resource;

public class SettingDlg extends JDialog implements ActionListener
{
	private static final long serialVersionUID = -854353051241196941L;

	private JTextField textExcutePath;
	
	private String strExcuteEditorPath;
	private String strframeworkResPath;
	
	private String strLanguage;
	private boolean isSamePackage;
	
	JButton savebutton, exitbutton;
    JButton browser1,browser2,browser3;
	
    JComboBox<String> comboBox;
    JCheckBox chckbxNewCheckBox;
    
    JList<String> jlist;
    ArrayList<String> resList = new ArrayList<String>();
    
	SettingDlg() {
		KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
		getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
			private static final long serialVersionUID = -8988954049940512230L;
			public void actionPerformed(ActionEvent e) {
		        dispose();
		    }
		});
		
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
		
		isSamePackage = (boolean)Resource.PROP_CHECK_INSTALLED.getData(false);
		
		strframeworkResPath = (String)Resource.PROP_FRAMEWORK_RES.getData();
		if(strframeworkResPath == null) {
			strframeworkResPath = "";
			Resource.PROP_FRAMEWORK_RES.setData(strframeworkResPath);
		}
		
		for(String s: strframeworkResPath.split(";")) {
			resList.add(s);
		}
	}
	
	private void saveSettings()
	{
		Resource.PROP_EDITOR.setData(strExcuteEditorPath);
		Resource.PROP_LANGUAGE.setData(strLanguage);
		Resource.PROP_CHECK_INSTALLED.setData(isSamePackage);
		Resource.PROP_FRAMEWORK_RES.setData(strframeworkResPath);
	}

	void makeDialog() {
		this.setTitle(Resource.STR_SETTINGS_TITLE.getString());
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setSize(new Dimension(480,215));
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

		JLabel userLabel = new JLabel(Resource.STR_SETTINGS_EDITOR.getString());
		userLabel.setBounds(10, 10, 116, 25);
		panel.add(userLabel);

		textExcutePath = new JTextField(20);
		textExcutePath.setText(strExcuteEditorPath);
		
		textExcutePath.setBounds(121, 10, 283, 25);
		panel.add(textExcutePath);		
		
		JLabel frameworkLabel = new JLabel(Resource.STR_SETTINGS_RES.getString());
		frameworkLabel.setBounds(10, 40, 114, 25);
		panel.add(frameworkLabel);
	    
	    jlist = new JList<String>();
        JScrollPane scrollPane1 = new JScrollPane(jlist);
        scrollPane1.setPreferredSize(new Dimension(50, 400));
        scrollPane1.setBounds(121, 40, 283, 50);
        panel.add(scrollPane1);
		jlist.setListData(resList.toArray(new String[0]));
		
		savebutton = new JButton(Resource.STR_BTN_SAVE.getString());
		savebutton.setBounds(288, 150, 80, 25);
		savebutton.addActionListener(this);
		savebutton.setFocusable(false);
		panel.add(savebutton);
		
		
		exitbutton = new JButton(Resource.STR_BTN_CLOSE.getString());
		exitbutton.setBounds(380, 150, 80, 25);
		exitbutton.addActionListener(this);
		exitbutton.setFocusable(false);
		panel.add(exitbutton);

	    
	    browser1 = new JButton("...");
	    browser1.setBounds(405, 10, 64, 25);
	    browser1.addActionListener(this);
	    browser1.setFocusable(false);
	    panel.add(browser1);

	    browser2 = new JButton(Resource.STR_BTN_ADD.getString());
	    browser2.setBounds(405, 40, 64, 24);
	    browser2.addActionListener(this);
	    browser2.setFocusable(false);
	    panel.add(browser2);
	    
	    browser3 = new JButton(Resource.STR_BTN_DEL.getString());
	    browser3.setBounds(405, 65, 64, 24);
	    browser3.addActionListener(this);
	    browser3.setFocusable(false);
	    panel.add(browser3);
	    
	    chckbxNewCheckBox = new JCheckBox(Resource.STR_SETTINGS_CHECK_INSTALLED.getString());
	    
    	chckbxNewCheckBox.setSelected(isSamePackage);
	    
	    chckbxNewCheckBox.addActionListener(this);
	    chckbxNewCheckBox.setBounds(10, 93, 236, 25);
	    panel.add(chckbxNewCheckBox);
	    
	    JLabel label = new JLabel(Resource.STR_SETTINGS_LANGUAGE.getString());
	    label.setBounds(15, 120, 60, 25);
	    panel.add(label);
	    
	    comboBox = new JComboBox<String>();
	    comboBox.setBounds(87, 120, 94, 24);
	    
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
				dlg.makeDialog();
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	
		if(e.getSource() == savebutton) {
			//System.out.println("save");
			
			if((new File(textExcutePath.getText().trim()).exists())) {
				strExcuteEditorPath = textExcutePath.getText().trim();					
			} else {
				textExcutePath.setText(strExcuteEditorPath);
			}

			strframeworkResPath = "";
			for(String f: resList) {
				strframeworkResPath += f + ";";
			}
			
			isSamePackage = chckbxNewCheckBox.isSelected();
			strLanguage = (String)comboBox.getSelectedItem();
			
			saveSettings();
		} else if(e.getSource() == exitbutton) {
			//System.out.println("exit");
			this.dispose();
		} else if(e.getSource() == browser1) {
			JFileChooser jfc = new JFileChooser();										
			if(jfc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
				return;
			
			File dir = jfc.getSelectedFile();
			if(dir!=null) {
				strExcuteEditorPath = dir.getPath();
				textExcutePath.setText(dir.getPath()); 
			}
		} else if(e.getSource() == browser2) {
			JFileChooser jfc = new JFileChooser();			
			jfc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("apk","apk"));							
			
			if(jfc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
				return;
			
			File dir = jfc.getSelectedFile();
			String file = null;
			if(dir!=null) {
				file = dir.getPath();
			}
			
			if(file == null || file.isEmpty()) return;
			for(String f: resList) {
				if(file.equals(f)) return;
			}
			resList.add(file);
			jlist.setListData(resList.toArray(new String[0]));
		} else if(e.getSource() == browser3) {
			if(jlist.getSelectedIndex() < 0) return;
			resList.remove(jlist.getSelectedIndex());
			jlist.setListData(resList.toArray(new String[0]));
		}
	}
}
