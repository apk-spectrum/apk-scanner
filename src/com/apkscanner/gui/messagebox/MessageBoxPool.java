package com.apkscanner.gui.messagebox;

import javax.swing.JOptionPane;

import com.apkscanner.resource.Resource;

public class MessageBoxPool {
	public static final int MSG_ERR_NO_SUCH_FILE = 1;

	public static int show(int messageID) {
		switch(messageID) {
		case MSG_ERR_NO_SUCH_FILE:
		    return JOptionPane.showOptionDialog(null,
		    		Resource.STR_MSG_NO_SUCH_APK_FILE.getString(), 
		    		Resource.STR_LABEL_ERROR.getString(),
		    		JOptionPane.ERROR_MESSAGE, JOptionPane.ERROR_MESSAGE, 
		    		Resource.IMG_WARNING.getImageIcon(),
		    		new String[] {Resource.STR_BTN_CLOSE.getString()},
		    		Resource.STR_BTN_CLOSE.getString());
		}
		return -1;
	}
}
