package com.apkscanner.plugin.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.apkscanner.gui.util.WindowSizeMemorizer;
import com.apkscanner.plugin.IUpdateChecker;
import com.apkscanner.plugin.PlugInConfig;
import com.apkscanner.plugin.PlugInManager;
import com.apkscanner.resource.Resource;

public class UpdateNotificationWindow extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 8847497098166369293L;

	private static final String ACT_CMD_CLOSE = "ACT_CMD_CLOSE";
	
	private static UpdateNotificationWindow frame;
	private static UpdateNotificationPanel mainPanel;

	private JCheckBox ckbNaver;

	private UpdateNotificationWindow(Component parent) {
		setTitle("Update List");
		setIconImage(Resource.IMG_APP_ICON.getImageIcon().getImage());
		setResizable(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		add(mainPanel = new UpdateNotificationPanel());
		
		JPanel ctrPanel = new JPanel();
		BoxLayout ctrLayout = new BoxLayout(ctrPanel, BoxLayout.Y_AXIS);
		ctrPanel.setLayout(ctrLayout);
		ctrPanel.setBorder(new EmptyBorder(5,5,5,5));
		
		ckbNaver = new JCheckBox("다시 보지 않기");
		ctrPanel.add(ckbNaver);

		JButton btnClose = new JButton("Close");
		btnClose.setActionCommand(ACT_CMD_CLOSE);
		btnClose.addActionListener(this);

		JPanel btnCtrPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)) {
			private static final long serialVersionUID = -7930379247697419237L;
			@Override
            public Dimension getMaximumSize() {
                Dimension max = super.getMaximumSize();
                max.height = getPreferredSize().height;
                return max;
            }
        };
        btnCtrPanel.setAlignmentX(0.0f);
        btnCtrPanel.add(btnClose);

		ctrPanel.add(btnCtrPanel);
		
		add(ctrPanel, BorderLayout.SOUTH);

		pack();
		if((boolean)Resource.PROP_SAVE_WINDOW_SIZE.getData()) {
			WindowSizeMemorizer.resizeCompoent(this);
		}
		WindowSizeMemorizer.registeComponent(this);
		setLocationRelativeTo(parent);
	}

	public static void show(Component parent, IUpdateChecker[] list) {
		if(frame == null) frame = new UpdateNotificationWindow(parent);
		mainPanel.addUpdateList(list);
		if(!frame.isShowing()) {
			frame.setVisible(true);
		}
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		String actCmd = evt.getActionCommand();
		if(actCmd == null) return;

		switch(actCmd) {
		case ACT_CMD_CLOSE:
			if(ckbNaver.isSelected()) {
				PlugInConfig.setGlobalConfiguration(PlugInConfig.CONFIG_NO_LOOK_UPDATE_POPUP, "true");
				PlugInManager.saveProperty();
			}
			frame.dispose();
			break;
		}
	}
}
