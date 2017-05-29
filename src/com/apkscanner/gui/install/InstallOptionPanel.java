package com.apkscanner.gui.install;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import com.apkscanner.gui.util.AbstractTabRenderer;
import com.apkscanner.gui.util.JXTabbedPane;

import javafx.scene.layout.Border;

public class InstallOptionPanel extends JPanel{
	
	public InstallOptionPanel() {
		this.setLayout(new BorderLayout());	
					
		TitledBorder Installborder = BorderFactory.createTitledBorder("Install");
		TitledBorder Pushborder = BorderFactory.createTitledBorder("Push");
		
		JPanel installPanel = new JPanel(new GridLayout(0,1));
		JPanel pushPanel = new JPanel(new GridLayout(0,1));
		
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        
        tabbedPane.addTab("Install", null, installPanel, "Install");
        tabbedPane.addTab("Push", null, pushPanel, "Push");
        			
		installPanel.setBorder(Installborder);
		pushPanel.setBorder(Pushborder);
		
		
		JRadioButton Radiointernal = new JRadioButton("internal");
		JRadioButton Radioexternal = new JRadioButton("external");
		JCheckBox Checkrunafterinstall = new JCheckBox("run after installed");
		
		 JToggleButton InstalltoggleButton = new JToggleButton("Install");
	     
		
	    installPanel.add(Radiointernal);
		installPanel.add(Radioexternal);
		installPanel.add(Checkrunafterinstall);
			
		
		JRadioButton RadiosystemPush = new JRadioButton("system");
		JRadioButton RadioprivPush = new JRadioButton("priv-app");
		JRadioButton RadiodataPush = new JRadioButton("data");
		JCheckBox CheckOverwrite = new JCheckBox("overwrite lib");
		JCheckBox CheckWithLib = new JCheckBox("with Lib");
		JCheckBox CheckReboot = new JCheckBox("reboot after push");
		
		JToggleButton pushtoggleButton = new JToggleButton("Push");
		
		pushPanel.add(RadiosystemPush);
		pushPanel.add(RadioprivPush);
		pushPanel.add(RadiodataPush);
		pushPanel.add(CheckOverwrite);
		pushPanel.add(CheckWithLib);
		pushPanel.add(CheckReboot);
		
		//optionPanel.add(installPanel);
		//optionPanel.add(pushPanel);
		
		
		InstalltoggleButton.setPreferredSize(new Dimension(100, 25));
		pushtoggleButton.setPreferredSize(new Dimension(100, 25));
		
		//togglePanel.add(InstalltoggleButton);
		//togglePanel.add(pushtoggleButton);
		
		this.add(tabbedPane, BorderLayout.CENTER);		
	}
	
    private GridBagConstraints addGrid(GridBagConstraints gbc, 
            int gridx, int gridy, int gridwidth, int gridheight, int weightx, int weighty) {
      gbc.gridx = gridx;
      gbc.gridy = gridy;
      gbc.gridwidth = gridwidth;
      gbc.gridheight = gridheight;
      gbc.weightx = weightx;
      gbc.weighty = weighty;
      return gbc;
    }
	
}
