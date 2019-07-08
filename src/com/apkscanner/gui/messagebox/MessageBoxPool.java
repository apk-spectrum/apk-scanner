package com.apkscanner.gui.messagebox;

import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;

import com.apkscanner.resource.RStr;

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

	public static final int MSG_WARN_ACCESS_TRUSTSTORE = 28;
	public static final int MSG_CERTIFICATE_NO_SELECTED = 29;
	public static final int CONFIRM_IMPORT_CERTIFICATE = 30;
	
	public static final int MSG_WARN_NEED_CERT_ALISAS = 31;
	public static final int MSG_WARN_CERT_ALISAS_EXISTED = 32;

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

	public int show(int messageId, Object... extMessage) {
		return show(parentComponent, messageId, extMessage);
	}

	public static int show(Component parentComponent, int messageId, Object... extData) {
		switch(messageId) {
		case MSG_NO_SUCH_APK_FILE:
			MessageBoxPane.showError(parentComponent, RStr.MSG_NO_SUCH_APK_FILE.get());
			break;
		case MSG_NO_SUCH_CLASSES_DEX:
			MessageBoxPane.showError(parentComponent, RStr.MSG_NO_SUCH_CLASSES_DEX.get());
			break;
		case MSG_NO_SUCH_PACKAGE_DEVICE:
			MessageBoxPane.showError(parentComponent, RStr.MSG_NO_SUCH_PACKAGE_DEVICE.get());
			break;
		case MSG_DISABLED_PACKAGE:
			MessageBoxPane.showError(parentComponent, (String)extData[0] + "\n : " + RStr.MSG_DISABLED_PACKAGE.get());
			break;
		case MSG_NO_SUCH_LAUNCHER:
			MessageBoxPane.showError(parentComponent, RStr.MSG_NO_SUCH_LAUNCHER.get());
			break;
		case MSG_FAILURE_OPEN_APK:
			MessageBoxPane.showError(parentComponent, RStr.MSG_FAILURE_OPEN_APK.get());
			break;
		case MSG_SUCCESS_REMOVED:
			MessageBoxPane.showPlain(parentComponent, RStr.MSG_SUCCESS_REMOVED.get());
			break;
		case MSG_FAILURE_DEX2JAR:
			MessageBoxPane.showTextAreaDialog(parentComponent, RStr.MSG_FAILURE_DEX2JAR.get() + "\n\nerror message", (String)extData[0],
					RStr.LABEL_ERROR.get(), MessageBoxPane.ERROR_MESSAGE, null, new Dimension(300, 120));
			break;
		case MSG_FAILURE_UNINSTALLED:
			MessageBoxPane.showTextAreaDialog(parentComponent, RStr.MSG_FAILURE_UNINSTALLED.get() + "\nConsol output:", (String)extData[0],
					RStr.LABEL_ERROR.get(), MessageBoxPane.ERROR_MESSAGE, null, new Dimension(300, 50));
			break;
		case MSG_FAILURE_LAUNCH_APP:
			MessageBoxPane.showTextAreaDialog(parentComponent, RStr.MSG_FAILURE_LAUNCH_APP.get() + "\n\nConsol output", (String)extData[0],
					RStr.LABEL_ERROR.get(), MessageBoxPane.ERROR_MESSAGE, null, new Dimension(500, 120));
			break;
		case MSG_FAILURE_PULLED:
			MessageBoxPane.showTextAreaDialog(parentComponent, RStr.MSG_FAILURE_PULLED.get() + "\n\nConsol output", (String)extData[0],
					RStr.LABEL_ERROR.get(), MessageBoxPane.ERROR_MESSAGE, null, new Dimension(400, 100));
			break;
		case MSG_CANNOT_WRITE_FILE:
			MessageBoxPane.showError(parentComponent, RStr.MSG_CANNOT_WRITE_FILE.get());
			break;
		case QUESTION_SUCCESS_PULL_APK:
			return MessageBoxPane.showOptionDialog(parentComponent, RStr.MSG_SUCCESS_PULL_APK.get() + "\n" + (String)extData[0],
					RStr.LABEL_QUESTION.get(), MessageBoxPane.DEFAULT_OPTION, MessageBoxPane.INFORMATION_MESSAGE, null,
					new String[] {RStr.BTN_EXPLORER.get(), RStr.BTN_OPEN.get(), RStr.BTN_OK.get()}, RStr.BTN_OK.get());
		case QUESTION_PACK_INFO_REFRESH:
			return MessageBoxPane.showOptionDialog(parentComponent, RStr.QUESTION_PACK_INFO_REFRESH.get(),
					RStr.LABEL_QUESTION.get(), MessageBoxPane.YES_NO_OPTION, MessageBoxPane.QUESTION_MESSAGE, null,
					new String[] {RStr.BTN_CLOSE.get(), RStr.BTN_NO.get(), RStr.BTN_YES.get()}, RStr.BTN_YES.get());
		case QUESTION_PACK_INFO_CLOSE:
			return MessageBoxPane.showOptionDialog(parentComponent, RStr.QUESTION_PACK_INFO_CLOSE.get(),
					RStr.LABEL_QUESTION.get(), MessageBoxPane.YES_NO_OPTION, MessageBoxPane.QUESTION_MESSAGE, null,
					new String[] {RStr.BTN_NO.get(), RStr.BTN_YES.get()}, RStr.BTN_YES.get());
		case QUESTION_SAVE_OVERWRITE:
			return MessageBoxPane.showQuestion(parentComponent, RStr.QUESTION_SAVE_OVERWRITE.get(), MessageBoxPane.YES_NO_OPTION);
		case QUESTION_REMOVE_SYSTEM_APK:
			return MessageBoxPane.showQuestion(parentComponent, RStr.QUESTION_REMOVE_SYSTEM_APK.get(), MessageBoxPane.YES_NO_OPTION);
		case QUESTION_REBOOT_SYSTEM:
			return MessageBoxPane.showQuestion(parentComponent, RStr.QUESTION_REMOVED_REBOOT.get(), MessageBoxPane.YES_NO_OPTION);
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
			MessageBoxPane.showInfomation(parentComponent, RStr.MSG_SUCCESS_CLEAR_DATA.get());
			break;
		case MSG_FAILURE_CLEAR_DATA:
			MessageBoxPane.showError(parentComponent, RStr.MSG_FAILURE_CLEAR_DATA.get());
			break;
		case MSG_WARN_UNSUPPORTED_JVM:
			MessageBoxPane.showWarring(parentComponent, RStr.MSG_WARN_UNSUPPORTED_JVM.get());
			break;
		case MSG_WARN_ACCESS_TRUSTSTORE:
			return MessageBoxPane.showConfirmDialog(parentComponent, "Warning. Other applications and systems may be affected. continue deleting? ", RStr.LABEL_QUESTION.get(), MessageBoxPane.YES_NO_OPTION, MessageBoxPane.WARNING_MESSAGE, null);
		case MSG_CERTIFICATE_NO_SELECTED:
			MessageBoxPane.showWarring(parentComponent, "No selected");
			break;
		case CONFIRM_IMPORT_CERTIFICATE:
			return MessageBoxPane.showConfirmDialog(parentComponent, extData[0], RStr.LABEL_QUESTION.get(), MessageBoxPane.OK_CANCEL_OPTION, MessageBoxPane.PLAIN_MESSAGE, null);
		case MSG_WARN_NEED_CERT_ALISAS:
			MessageBoxPane.showWarring(parentComponent, "Empty alilas name.");
			break;
		case MSG_WARN_CERT_ALISAS_EXISTED:
			MessageBoxPane.showWarring(parentComponent, "Already existed alias name");
			break;
		}
		return MessageBoxPane.CLOSED_OPTION;
	}
}
