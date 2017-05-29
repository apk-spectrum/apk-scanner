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
		this.setLayout(new GridBagLayout());	
		GridBagConstraints gbc = new GridBagConstraints();      
		
		JPanel optionPanel = new JPanel(new GridLayout(1, 2));
		JPanel togglePanel = new JPanel(new GridBagLayout());
					
		TitledBorder Installborder = BorderFactory.createTitledBorder("Install");
		TitledBorder Pushborder = BorderFactory.createTitledBorder("Push");
		
		JPanel installPanel = new JPanel(new GridLayout(0,1));
		JPanel pushPanel = new JPanel(new GridLayout(0,1));
		
		
		JXTabbedPane tabbedPane = new JXTabbedPane(JTabbedPane.LEFT);
        AbstractTabRenderer renderer = (AbstractTabRenderer)tabbedPane.getTabRenderer();
        renderer.setPrototypeText("This text is a prototype");
        renderer.setHorizontalTextAlignment(SwingConstants.LEADING);

        tabbedPane.addTab("Install", null, installPanel, "Install");
        tabbedPane.addTab("Push", null, pushPanel, "Push");
        			
		installPanel.setBorder(Installborder);
		pushPanel.setBorder(Pushborder);
		
		JPanel CertPanel = new JPanel(new BorderLayout());
		
		JLabel textSelectDevice = new JLabel("set install option");
		textSelectDevice.setFont(new Font(textSelectDevice.getFont().getName(), Font.PLAIN, 30));
		
		JLabel textCertInfo = new JLabel("Cert Info");
		
		JTextArea CertInfo = new JTextArea();
		JScrollPane textViewscrollPane = new JScrollPane(CertInfo);
		
		JButton buttonchangeCert = new JButton("change Cert");
		
		
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
		
		optionPanel.add(tabbedPane);
		
		InstalltoggleButton.setPreferredSize(new Dimension(100, 25));
		pushtoggleButton.setPreferredSize(new Dimension(100, 25));
		
        gbc.anchor = GridBagConstraints.EAST;            
        togglePanel.add(InstalltoggleButton,addGrid(gbc, 0, 0, 1, 1, 1, 1));
        gbc.anchor = GridBagConstraints.WEST;
        togglePanel.add(pushtoggleButton,addGrid(gbc, 1, 0, 1, 1, 1, 1));
        
		
		//togglePanel.add(InstalltoggleButton);
		//togglePanel.add(pushtoggleButton);
		
		JPanel certibuttonpanel = new JPanel(new BorderLayout());
		
		certibuttonpanel.add(buttonchangeCert, BorderLayout.EAST);
		
		CertPanel.add(textCertInfo, BorderLayout.NORTH);
		CertPanel.add(textViewscrollPane, BorderLayout.CENTER);
		CertPanel.add(certibuttonpanel, BorderLayout.SOUTH);
		
		      
		
		//panel_set_install_option.add(textSelectDevice, BorderLayout.NORTH);
		//panel_set_install_option.add(optionPanel, BorderLayout.CENTER);
		//panel_set_install_option.add(CertPanel, BorderLayout.SOUTH);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        
		this.add(textSelectDevice,addGrid(gbc, 0, 0, 1, 1, 1, 2));
					
		//panel_set_install_option.add(togglePanel, addGrid(gbc, 0, 1, 1, 1, 1, 1));
		
		gbc.fill = GridBagConstraints.BOTH;
		this.add(optionPanel,addGrid(gbc, 0, 2, 1, 1, 1, 3));
		gbc.fill = GridBagConstraints.BOTH;
		this.add(CertPanel,addGrid(gbc, 0, 3, 1, 1, 1, 5));
		
		this.add(new JPanel(),addGrid(gbc, 0, 4, 1, 1, 1, 3));
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
