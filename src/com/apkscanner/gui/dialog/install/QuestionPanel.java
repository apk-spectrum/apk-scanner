package com.apkscanner.gui.dialog.install;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;


//ArrowTraversalPane.showOptionDialog(null, Resource.STR_MSG_ALREADY_INSTALLED.getString() + "\n"  +  strLine + pkgInfo + strLine + Resource.STR_QUESTION_OPEN_OR_INSTALL.getString(),
		//Resource.STR_LABEL_WARNING.getString(), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, Appicon, checkPackDelOptions, checkPackDelOptions[3]);

public class QuestionPanel extends JPanel{

	public QuestionPanel(String message, int optionType, int messageType, Icon icon, Object[] options, Object initialValue) {
		// TODO Auto-generated constructor stub
		
		JLabel iconLabel = new JLabel();
		iconLabel.setIcon(icon);
	
		this.add(iconLabel);
		
	}
	
	public QuestionPanel(String message,Icon icon) {
		// TODO Auto-generated constructor stub
		
		JLabel iconLabel = new JLabel();
		iconLabel.setIcon(icon);
	
		JLabel MessageLabel = new JLabel("message");
		
		this.add(iconLabel);
		this.add(MessageLabel);
		
		
	}
}
