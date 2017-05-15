package com.apkscanner.gui.install;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.apkscanner.gui.dialog.ApkInstallWizard.UIEventHandler;
import com.apkscanner.tool.adb.AdbDeviceManager.DeviceStatus;
import com.apkscanner.util.Log;

public class FindPackagePanel extends JPanel{
	private JTextArea pack_textPakcInfo;
	private JComboBox<String> pack_comboStartActivity;
	private JList<String> pack_deviceList;
	private JButton pack_btnSave;
	private JButton pack_btnOpen;
	private JButton pack_btnLaunch;
	private JButton pack_btnRemove;
	private ActionListener mainlistener;
	
	public static final String CTR_ACT_CMD_FINDPACKAGE_SAVE = "CTR_ACT_CMD_FINDPACKAGE_SAVE";
	public static final String CTR_ACT_CMD_FINDPACKAGE_OPEN = "CTR_ACT_CMD_FINDPACKAGE_OPEN";
	public static final String CTR_ACT_CMD_FINDPACKAGE_LAUNCH= "CTR_ACT_CMD_FINDPACKAGE_LAUNCH";
	public static final String CTR_ACT_CMD_FINDPACKAGE_REMOVE = "CTR_ACT_CMD_FINDPACKAGE_REMOVE";
	public static final String CTR_ACT_CMD_FINDPACKAGE_PACKAGEINFO = "CTR_ACT_CMD_FINDPACKAGE_PACKAGEINFO";
	
	
    public FindPackagePanel(ActionListener listener) {
		this.setLayout(new GridBagLayout());
		mainlistener = listener;
		JPanel mainpanel = new JPanel(new BorderLayout());
		JPanel Listpanel = new JPanel(new BorderLayout());
		JPanel packagepanel = new JPanel(new BorderLayout());
		JPanel appstartpanel = new JPanel(new BorderLayout());
		JPanel buttonpanel = new JPanel();
		
		JLabel textSelectDevice = new JLabel("installed same package!");
		textSelectDevice.setFont(new Font(textSelectDevice.getFont().getName(), Font.PLAIN, 30));
		
		pack_deviceList = new JList<String>(new DefaultListModel<String>());
		pack_deviceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		pack_deviceList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// TODO Auto-generated method stub				
				if(pack_deviceList.getSelectedIndex() <= -1) return;
				String selDev = pack_deviceList.getSelectedValue();
				Object[] obj = new Object[2];
				obj[0] = selDev;
				obj[1] = pack_deviceList;
				ActionEvent action = new ActionEvent(obj, 0, CTR_ACT_CMD_FINDPACKAGE_PACKAGEINFO);
				mainlistener.actionPerformed(action);
			}
		});
		JScrollPane listscrollPane = new JScrollPane(pack_deviceList);
		
		pack_textPakcInfo = new JTextArea();
		pack_textPakcInfo.setEditable(false);
		JScrollPane textViewscrollPane = new JScrollPane(pack_textPakcInfo);
		
		pack_comboStartActivity = new JComboBox<String>(new DefaultComboBoxModel<String>());
	    
	    pack_btnLaunch = getButton("Launch", CTR_ACT_CMD_FINDPACKAGE_LAUNCH, listener);
	    pack_btnLaunch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String selDev = pack_deviceList.getSelectedValue();
				arg0.setSource((String)(selDev));				
				mainlistener.actionPerformed(arg0);
			}
	    });
	    pack_btnOpen = getButton("Open", CTR_ACT_CMD_FINDPACKAGE_OPEN, listener);
	    pack_btnOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String selDev = pack_deviceList.getSelectedValue();
				arg0.setSource((String)(selDev));				
				mainlistener.actionPerformed(arg0);
			}
	    });

	    pack_btnSave = getButton("Save", CTR_ACT_CMD_FINDPACKAGE_SAVE, listener);
	    pack_btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(pack_deviceList.getSelectedIndex() <= -1) return;
				String selDev = pack_deviceList.getSelectedValue();				
				arg0.setSource((String)(selDev));				
				mainlistener.actionPerformed(arg0);
			}
	    });
	    pack_btnRemove = getButton("Remove", CTR_ACT_CMD_FINDPACKAGE_REMOVE, listener);
	    pack_btnRemove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(pack_deviceList.getSelectedIndex() <= -1) return;
				String selDev = pack_deviceList.getSelectedValue();
				arg0.setSource((String)(selDev));				
				mainlistener.actionPerformed(arg0);
			}
	    });
	    
	    setmargin(mainpanel,5);
	    setmargin(packagepanel,5);
	    setmargin(Listpanel,5);
	    
	    
	    buttonpanel.add(pack_btnOpen, BorderLayout.WEST);
	    buttonpanel.add(pack_btnSave, BorderLayout.CENTER);
	    buttonpanel.add(pack_btnRemove, BorderLayout.EAST);
	    
	    appstartpanel.add(pack_comboStartActivity, BorderLayout.CENTER);
	    appstartpanel.add(pack_btnLaunch, BorderLayout.EAST);
	    
	    packagepanel.add(textViewscrollPane, BorderLayout.CENTER);
	    packagepanel.add(appstartpanel, BorderLayout.SOUTH);		    
	    
	    Listpanel.add(packagepanel, BorderLayout.CENTER);
	    Listpanel.add(buttonpanel, BorderLayout.SOUTH);
	    
	    //mainpanel.add(textSelectDevice,BorderLayout.NORTH);
	    mainpanel.add(Listpanel,BorderLayout.CENTER);
	    mainpanel.add(listscrollPane, BorderLayout.WEST);
	    
	    GridBagConstraints gbc = new GridBagConstraints();            
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    gbc.anchor = GridBagConstraints.NORTH;
	    this.add(textSelectDevice,addGrid(gbc, 0, 0, 1, 1, 1, 1));
	    gbc.fill = GridBagConstraints.BOTH;
	    this.add(mainpanel,addGrid(gbc, 0, 1, 1, 1, 1, 7));
	    this.add(new JPanel(),addGrid(gbc, 0, 2, 1, 1, 1, 3));
	}

	private void setmargin(JPanel c, int size) {
    	c.setBorder(BorderFactory.createEmptyBorder(size, size, size, size));
    }
    
	private JButton getButton(String text, String actCmd, ActionListener listener) {
		JButton btn = new JButton(text);
		btn.setActionCommand(actCmd);		
		btn.addActionListener(listener);
		return btn;
	}
	
	public JList getDeivceListCombo() {
		return pack_deviceList;
	}
	
	public JComboBox getActivityCombo() {
		return pack_comboStartActivity;
	}
	public JTextArea getappInfotextarea() {
		return pack_textPakcInfo;
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
