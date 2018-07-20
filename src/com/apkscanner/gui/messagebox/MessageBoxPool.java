package com.apkscanner.gui.messagebox;

import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;

import com.apkscanner.resource.Resource;

public class MessageBoxPool {
	public static final int MSG_NO_SUCH_APK_FILE = 1;
	public static final int MSG_NO_SUCH_CLASSES_DEX = 2;
	public static final int MSG_NO_SUCH_PACKAGE_DEVICE = 3;
	public static final int MSG_DISABLED_PACKAGE = 4;
	public static final int MSG_NO_SUCH_LAUNCHER = 5;
	public static final int MSG_FAILURE_OPEN_APK = 6;
	public static final int MSG_SUCCESS_REMOVED = 7;
	public static final int MSG_FAILURE_DEX2JAR = 8;
	public static final int MSG_FAILURE_UNINSTALLED = 9;
	public static final int MSG_FAILURE_LAUNCH_APP = 10;
	public static final int MSG_FAILURE_PULLED = 11;
	public static final int QUESTION_SUCCESS_PULL_APK = 12;
	public static final int QUESTION_PACK_INFO_REFRESH = 13;
	public static final int QUESTION_PACK_INFO_CLOSE = 14;
	public static final int MSG_CANNOT_WRITE_FILE = 15;
	public static final int QUESTION_SAVE_OVERWRITE = 16;
	public static final int QUESTION_REMOVE_SYSTEM_APK = 17;
	public static final int QUESTION_REBOOT_SYSTEM = 18;

	public static final int MSG_BLOCKED_CAUSE_UNSIGNED = 19;
	public static final int MSG_BLOCKED_UNSUPPORTED_SDK_LEVEL = 20;
	public static final int MSG_BLOCKED_MISMATCH_SIGNED = 21;
	public static final int MSG_BLOCKED_NO_ROOT = 22;
	public static final int MSG_BLOCKED_MISMATCH_SIGNED_NOT_SYSTEM = 23;
	public static final int MSG_BLOCKED_UNKNOWN = 24;

	public static final int MSG_SUCCESS_CLEAR_DATA = 25;
	public static final int MSG_FAILURE_CLEAR_DATA = 26;
	
	public static final int MSG_WARN_UNSUPPORTED_JVM = 27;

	private Component parentComponent;
	private static HashMap<Integer, MessageBoxPool> pool = new HashMap<Integer, MessageBoxPool>(); 

	public MessageBoxPool(Component parentComponent) {
		this.parentComponent = parentComponent;
	}

	public static MessageBoxPool getMessageBoxPool(Component parentComponent) {
		int hashcode = 0;
		if(parentComponent != null) {
			hashcode = parentComponent.hashCode();
		}
		MessageBoxPool mbp = null;
		synchronized(pool) {
			mbp = pool.get(hashcode);
			if(mbp == null) {
				mbp = new MessageBoxPool(parentComponent);
				pool.put(hashcode, mbp);
			}
		}
		return mbp;
	}

	public int show(int messageId, String... extMessage) {
		return show(parentComponent, messageId, extMessage);
	}

	public static int show(Component parentComponent, int messageId, String... extMessage) {
		switch(messageId) {
		case MSG_NO_SUCH_APK_FILE:
			MessageBoxPane.showError(parentComponent, Resource.STR_MSG_NO_SUCH_APK_FILE.getString());
			break;
		case MSG_NO_SUCH_CLASSES_DEX:
			MessageBoxPane.showError(parentComponent, Resource.STR_MSG_NO_SUCH_CLASSES_DEX.getString());
			break;
		case MSG_NO_SUCH_PACKAGE_DEVICE:
			MessageBoxPane.showError(parentComponent, Resource.STR_MSG_NO_SUCH_PACKAGE_DEVICE.getString());
			break;
		case MSG_DISABLED_PACKAGE:
			MessageBoxPane.showError(parentComponent, extMessage[0] + "\n : " + Resource.STR_MSG_DISABLED_PACKAGE.getString());
			break;
		case MSG_NO_SUCH_LAUNCHER:
			MessageBoxPane.showError(parentComponent, Resource.STR_MSG_NO_SUCH_LAUNCHER.getString());
			break;
		case MSG_FAILURE_OPEN_APK:
			MessageBoxPane.showError(parentComponent, Resource.STR_MSG_FAILURE_OPEN_APK.getString());
			break;
		case MSG_SUCCESS_REMOVED:
			MessageBoxPane.showPlain(parentComponent, Resource.STR_MSG_SUCCESS_REMOVED.getString());
			break;
		case MSG_FAILURE_DEX2JAR:
			MessageBoxPane.showTextAreaDialog(parentComponent, Resource.STR_MSG_FAILURE_DEX2JAR.getString() + "\n\nerror message", extMessage[0],
					Resource.STR_LABEL_ERROR.getString(), MessageBoxPane.ERROR_MESSAGE, null, new Dimension(300, 120));
			break;
		case MSG_FAILURE_UNINSTALLED:
			MessageBoxPane.showTextAreaDialog(parentComponent, Resource.STR_MSG_FAILURE_UNINSTALLED.getString() + "\nConsol output:", extMessage[0],
					Resource.STR_LABEL_ERROR.getString(), MessageBoxPane.ERROR_MESSAGE, null, new Dimension(300, 50));
			break;
		case MSG_FAILURE_LAUNCH_APP:
			MessageBoxPane.showTextAreaDialog(parentComponent, Resource.STR_MSG_FAILURE_LAUNCH_APP.getString() + "\n\nConsol output", extMessage[0],
					Resource.STR_LABEL_ERROR.getString(), MessageBoxPane.ERROR_MESSAGE, null, new Dimension(500, 120));
			break;
		case MSG_FAILURE_PULLED:
			MessageBoxPane.showTextAreaDialog(parentComponent, Resource.STR_MSG_FAILURE_PULLED.getString() + "\n\nConsol output", extMessage[0],
					Resource.STR_LABEL_ERROR.getString(), MessageBoxPane.ERROR_MESSAGE, null, new Dimension(400, 100));
			break;
		case MSG_CANNOT_WRITE_FILE:
			MessageBoxPane.showError(parentComponent, Resource.STR_MSG_CANNOT_WRITE_FILE.getString());
			break;
		case QUESTION_SUCCESS_PULL_APK:
			return MessageBoxPane.showOptionDialog(parentComponent, Resource.STR_MSG_SUCCESS_PULL_APK.getString() + "\n" + extMessage[0],
					Resource.STR_LABEL_QUESTION.getString(), MessageBoxPane.DEFAULT_OPTION, MessageBoxPane.INFORMATION_MESSAGE, null,
					new String[] {Resource.STR_BTN_EXPLORER.getString(), Resource.STR_BTN_OPEN.getString(), Resource.STR_BTN_OK.getString()}, Resource.STR_BTN_OK.getString());
		case QUESTION_PACK_INFO_REFRESH:
			return MessageBoxPane.showOptionDialog(parentComponent, Resource.STR_QUESTION_PACK_INFO_REFRESH.getString(),
					Resource.STR_LABEL_QUESTION.getString(), MessageBoxPane.YES_NO_OPTION, MessageBoxPane.QUESTION_MESSAGE, null,
					new String[] {Resource.STR_BTN_CLOSE.getString(), Resource.STR_BTN_NO.getString(), Resource.STR_BTN_YES.getString()}, Resource.STR_BTN_YES.getString());
		case QUESTION_PACK_INFO_CLOSE:
			return MessageBoxPane.showOptionDialog(parentComponent, Resource.STR_QUESTION_PACK_INFO_CLOSE.getString(),
					Resource.STR_LABEL_QUESTION.getString(), MessageBoxPane.YES_NO_OPTION, MessageBoxPane.QUESTION_MESSAGE, null,
					new String[] {Resource.STR_BTN_NO.getString(), Resource.STR_BTN_YES.getString()}, Resource.STR_BTN_YES.getString());
		case QUESTION_SAVE_OVERWRITE:
			return MessageBoxPane.showQuestion(parentComponent, Resource.STR_QUESTION_SAVE_OVERWRITE.getString(), MessageBoxPane.YES_NO_OPTION);
		case QUESTION_REMOVE_SYSTEM_APK:
			return MessageBoxPane.showQuestion(parentComponent, Resource.STR_QUESTION_REMOVE_SYSTEM_APK.getString(), MessageBoxPane.YES_NO_OPTION);
		case QUESTION_REBOOT_SYSTEM:
			return MessageBoxPane.showQuestion(parentComponent, Resource.STR_QUESTION_REMOVED_REBOOT.getString(), MessageBoxPane.YES_NO_OPTION);
		case MSG_BLOCKED_CAUSE_UNSIGNED:
			MessageBoxPane.showError(parentComponent, "Unsigned APK");
			break;
		case MSG_BLOCKED_UNSUPPORTED_SDK_LEVEL:
			MessageBoxPane.showError(parentComponent, "UNSUPPORTED_SDK_LEVEL");
			break;
		case MSG_BLOCKED_MISMATCH_SIGNED:
			MessageBoxPane.showError(parentComponent, "MISMATCH_SIGNED");
			break;
		case MSG_BLOCKED_NO_ROOT:
			MessageBoxPane.showError(parentComponent, "NO_ROOT");
			break;
		case MSG_BLOCKED_MISMATCH_SIGNED_NOT_SYSTEM:
			MessageBoxPane.showError(parentComponent, "MISMATCH_SIGNED_NOT_SYSTEM");
			break;
		case MSG_BLOCKED_UNKNOWN:
			MessageBoxPane.showError(parentComponent, "MISMATCH_SIGNED_NOT_SYSTEM");
			break;
		case MSG_SUCCESS_CLEAR_DATA:
			MessageBoxPane.showInfomation(parentComponent, Resource.STR_MSG_SUCCESS_CLEAR_DATA.getString());
			break;
		case MSG_FAILURE_CLEAR_DATA:
			MessageBoxPane.showError(parentComponent, Resource.STR_MSG_FAILURE_CLEAR_DATA.getString());
			break;
		case MSG_WARN_UNSUPPORTED_JVM:
			MessageBoxPane.showWarring(parentComponent, Resource.STR_MSG_WARN_UNSUPPORTED_JVM.getString());
			break;
		}
		return MessageBoxPane.CLOSED_OPTION;
	}
}
