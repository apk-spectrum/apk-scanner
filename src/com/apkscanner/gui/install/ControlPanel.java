package com.apkscanner.gui.install;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import com.apkscanner.gui.dialog.ApkInstallWizard;

public class ControlPanel extends JPanel
{
	private static final long serialVersionUID = 5959656550868421305L;
	
	public static final String CTR_ACT_CMD_NEXT = "CTR_ACT_CMD_NEXT";
	public static final String CTR_ACT_CMD_PREVIOUS = "CTR_ACT_CMD_PREVIOUS";
	public static final String CTR_ACT_CMD_OK = "CTR_ACT_CMD_OK";
	public static final String CTR_ACT_CMD_CANCEL = "CTR_ACT_CMD_CANCEL";
	public static final String CTR_ACT_CMD_SHOW_LOG = "CTR_ACT_CMD_SHOW_LOG";
	public static final String CTR_ACT_CMD_RESTART = "CTR_ACT_CMD_RESTART";
	
	private JButton btnNext;
	private JButton btnPre;
	private JButton btnOk;
	private JButton btnCancel;
	private JButton btnShowLog;
	private JButton btnRestart;

	public ControlPanel(ActionListener listener) {
		super(new BorderLayout());
		
		btnNext = getButton("Next", CTR_ACT_CMD_NEXT, listener);
		btnPre = getButton("Previous", CTR_ACT_CMD_PREVIOUS, listener);
		btnOk = getButton("OK", CTR_ACT_CMD_OK, listener);
		btnCancel = getButton("Cancel", CTR_ACT_CMD_CANCEL, listener);
		btnShowLog = getButton("Show Log", CTR_ACT_CMD_SHOW_LOG, listener);
		btnRestart = getButton("Restart", CTR_ACT_CMD_SHOW_LOG, listener);
		
		JPanel stepPanel = new JPanel();
		stepPanel.add(btnCancel);
		stepPanel.add(btnPre);
		stepPanel.add(btnNext);
		stepPanel.add(btnRestart);
		stepPanel.add(btnOk);

		add(btnShowLog, BorderLayout.WEST);
		add(stepPanel, BorderLayout.EAST);
		
		JSeparator separator = new JSeparator();
		separator.setForeground(Color.LIGHT_GRAY);
		add(separator, BorderLayout.NORTH);
		
		// set status
		setStatus(ApkInstallWizard.STATUS_INIT);
	}
	
	private JButton getButton(String text, String actCmd, ActionListener listener) {
		JButton btn = new JButton(text);
		btn.setActionCommand(actCmd);
		btn.addActionListener(listener);
		return btn;
	}
	
	private void setVisibleButtons(boolean next, boolean pre, boolean ok, boolean cancel, boolean showlog, boolean restart) {
		btnNext.setVisible(next);
		btnPre.setVisible(pre);
		btnOk.setVisible(ok);
		btnCancel.setVisible(cancel);
		btnShowLog.setVisible(showlog);
		btnRestart.setVisible(restart);
	}
	
	public void setStatus(int status) {
		switch(status) {
		case ApkInstallWizard.STATUS_INIT:
			setVisibleButtons(true, false, false, false, false, false); break;
		case ApkInstallWizard.STATUS_DEVICE_SCANNING:
		case ApkInstallWizard.STATUS_WAIT_FOR_DEVICE:
			setVisibleButtons(false, false, false, true, false, false); break;
		case ApkInstallWizard.STATUS_DEVICE_REFRESH:
			setVisibleButtons(false, false, false, false, false, false); break;
		case ApkInstallWizard.STATUS_SELECT_DEVICE:
			setVisibleButtons(true, false, false, true, false, false); break;
		case ApkInstallWizard.STATUS_PACKAGE_SCANNING:
			setVisibleButtons(false, false, false, false, false, false); break;
		case ApkInstallWizard.STATUS_CHECK_PACKAGES:
			setVisibleButtons(true, true, false, true, false, false); break;
		case ApkInstallWizard.STATUS_SET_INSTALL_OPTION:
			setVisibleButtons(true, true, false, true, false, false); break;
		case ApkInstallWizard.STATUS_INSTALLING:
			setVisibleButtons(false, false, false, false, true, false); break;
		case ApkInstallWizard.STATUS_COMPLETED:
			setVisibleButtons(false, false, true, false, true, true); break;
		default:
			break;
		}
	}
}