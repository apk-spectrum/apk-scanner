package com.apkscanner.util;

import java.io.File;

import com.apkscanner.resource.Resource;

public class SystemUtil
{
	public static final String OS = System.getProperty("os.name").toLowerCase();

	public static boolean isWindows() {
		return OS.indexOf("win") > -1;
	}

	public static boolean isLinux() {
		return OS.indexOf("linux") > -1;
	}

	public static boolean isUnix() {
		return (OS.indexOf("nix") > -1 || OS.indexOf("nux") > -1 || OS.indexOf("aix") >-1 );
	}

	public static boolean isMac() {
		return OS.indexOf("mac") > -1;
	}

	public static boolean isSolaris() {
		return OS.indexOf("sunos") > -1;
	}

	public static String getUserLanguage() {
		return System.getProperty("user.language");
	}

	public static String getTemporaryPath() {
		return System.getProperty("java.io.tmpdir");
	}

	public static String getArchiveExplorer() throws Exception {
		if(isWindows()) {
			return "explorer";
		} else if(isLinux()) {
			return "file-roller";
		}
		throw new Exception("Unknown OS : " + OS);
	}

	public static String getFileExplorer() throws Exception {
		if(isWindows()) {
			return "explorer";
		} else if(isLinux()) {
			return "nautilus";
		}
		throw new Exception("Unknown OS : " + OS);
	}

	public static String getFileOpener() throws Exception {
		if(isWindows()) {
			return "explorer";
		} else if(isLinux()) {
			return "xdg-open";
		}
		throw new Exception("Unknown OS : " + OS);
	}

	public static String getDefaultEditor() throws Exception {
		if(isWindows()) {
			return "notepad";
		} else if(isLinux()) {
			return "gedit";
		}
		throw new Exception("Unknown OS : " + OS);
	}

	public static void openEditor(String path) {
		openEditor(new File(path));
	}

	public static void openEditor(File file) {
		if(file == null || !file.exists()) {
			Log.e("No such file or directory");
			return;
		}

		try {
			String editor = (String)Resource.PROP_EDITOR.getData();
			if(editor == null) {
				editor = SystemUtil.getDefaultEditor();
			}
			new ProcessBuilder(editor, file.getAbsolutePath()).start();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public static void openFileExplorer(String path) {
		openFileExplorer(new File(path));
	}

	public static void openFileExplorer(File file) {
		if(file == null || !file.exists()) {
			Log.e("No such file or directory");
			return;
		}

		String openPath = String.format((isWindows() && file.isFile())? "/select,\"%s\"" : "%s", file.getAbsolutePath());
		try {
			new ProcessBuilder(getFileExplorer(), openPath).start();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public static void openArchiveExplorer(String path) {
		openArchiveExplorer(new File(path));
	}

	public static void openArchiveExplorer(File file) {
		if(file == null || !file.exists()) {
			Log.e("No such file or directory");
			return;
		}

		try {
			new ProcessBuilder(getArchiveExplorer(), file.getAbsolutePath()).start();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public static void openFile(String path) {
		openFile(new File(path));
	}

	public static void openFile(File file) {
		if(file == null || !file.exists()) {
			Log.e("No such file or directory");
			return;
		}

		try {
			new ProcessBuilder(getFileOpener(), file.getAbsolutePath()).start();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}
