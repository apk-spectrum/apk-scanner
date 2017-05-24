package com.apkscanner.gui.messagebox;

import javax.swing.JOptionPane;

import com.apkscanner.resource.Resource;

public class MessageBoxPool {
	public static final int MSG_NO_SUCH_APK_FILE = 1;
	public static final int MSG_NO_SUCH_CLASSES_DEX = 2;
	public static final int MSG_NO_SUCH_PACKAGE_DEVICE = 3;
	public static final int MSG_DISABLED_PACKAGE = 4;
	public static final int MSG_NO_SUCH_LAUNCHER = 5;

	public static int show(int messageId, String... extMessage) {
		switch(messageId) {
		case MSG_NO_SUCH_APK_FILE:
		    return JOptionPane.showOptionDialog(null,
		    		Resource.STR_MSG_NO_SUCH_APK_FILE.getString(), 
		    		Resource.STR_LABEL_ERROR.getString(),
		    		JOptionPane.ERROR_MESSAGE, JOptionPane.ERROR_MESSAGE, 
		    		Resource.IMG_WARNING.getImageIcon(),
		    		new String[] {Resource.STR_BTN_CLOSE.getString()},
		    		Resource.STR_BTN_CLOSE.getString());
		case MSG_NO_SUCH_CLASSES_DEX:
			return MessageBoxPane.showOptionDialog(null,
					Resource.STR_MSG_NO_SUCH_CLASSES_DEX.getString(),
					Resource.STR_LABEL_WARNING.getString(),
					JOptionPane.OK_OPTION, 
					JOptionPane.WARNING_MESSAGE,
					null,
					new String[] {Resource.STR_BTN_OK.getString()},
					Resource.STR_BTN_OK.getString());
		case MSG_NO_SUCH_PACKAGE_DEVICE:
			return MessageBoxPane.showOptionDialog(null,
					Resource.STR_MSG_NO_SUCH_PACKAGE_DEVICE.getString(),
					Resource.STR_LABEL_INFO.getString(),
					JOptionPane.OK_OPTION, 
					JOptionPane.INFORMATION_MESSAGE,
					null,
					new String[] {Resource.STR_BTN_OK.getString()},
					Resource.STR_BTN_OK.getString());
		case MSG_DISABLED_PACKAGE:
			return MessageBoxPane.showOptionDialog(null,
					extMessage[0] + "\n : " + Resource.STR_MSG_DISABLED_PACKAGE.getString(),
					Resource.STR_LABEL_WARNING.getString(),
					JOptionPane.OK_OPTION, 
					JOptionPane.INFORMATION_MESSAGE,
					null,
					new String[] {Resource.STR_BTN_OK.getString()},
					Resource.STR_BTN_OK.getString());
		case MSG_NO_SUCH_LAUNCHER:
			return MessageBoxPane.showOptionDialog(null,
					Resource.STR_MSG_NO_SUCH_LAUNCHER.getString(),
					Resource.STR_LABEL_WARNING.getString(),
					JOptionPane.OK_OPTION, 
					JOptionPane.INFORMATION_MESSAGE,
					null,
					new String[] {Resource.STR_BTN_OK.getString()},
					Resource.STR_BTN_OK.getString());
		}
		return -1;
	}
	
	public static int showInfomation(String message) {
		return -1;
	}
	
	public static int showWarring(String message) {
		return -1;
	}
	
	public static int showQuestion(String message) {
		
		return -1;
	}

	public static int showPlain(String message) {
		return MessageBoxPane.showOptionDialog(null,
				message,
				Resource.STR_LABEL_WARNING.getString(),
				JOptionPane.OK_OPTION, 
				JOptionPane.INFORMATION_MESSAGE,
				null,
				new String[] {Resource.STR_BTN_OK.getString()},
				Resource.STR_BTN_OK.getString());
	}
	
}
