package com.apkscanner.gui.dialog;

import java.awt.Dimension;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;

public class LogDlg
{
	public LogDlg()
	{

	}
	
	static public int showLogDialog()
	{
		JTextArea taskOutput = new JTextArea();
		taskOutput.setText(Log.getLog());
		taskOutput.setEditable(false);
		taskOutput.setCaretPosition(0);
		
		JScrollPane scrollPane = new JScrollPane(taskOutput);
		scrollPane.setPreferredSize(new Dimension(600, 400));

		return JOptionPane.showOptionDialog(null, scrollPane, Resource.STR_LABEL_LOG.getString(), JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE, null,
	    		new String[] {Resource.STR_BTN_OK.getString()}, Resource.STR_BTN_OK.getString());
	}
}
