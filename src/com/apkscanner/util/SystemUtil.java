package com.apkscanner.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.apkscanner.resource.Resource;

import mslinks.ShellLink;
import mslinks.ShellLinkException;

public class SystemUtil
{
	public static final String OS = System.getProperty("os.name").toLowerCase();

	public static boolean isWindows() {
		return OS.indexOf("win") > -1;
	}

	public static boolean isLinux() {
		return OS.indexOf("nux") > -1;
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
			exec(new String[] { editor, file.getAbsolutePath() });
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
			exec(new String[] {getFileExplorer(), openPath});
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
			exec(new String[] { getArchiveExplorer(), file.getAbsolutePath() });
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
			exec(new String[] { getFileOpener(), file.getAbsolutePath() });
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public static String getRealPath(String path) {
		if(path == null || path.trim().isEmpty()) return null;

		String realPath = null;

		if(path.indexOf(File.separator) > -1) {
			File file = new File(path);
			if(!file.exists()) return null;
			realPath = file.getAbsolutePath();
		} else {
			String cmd = null;
			String regular = null;
			if(isWindows()) {
				cmd = "where";
				regular = "^[A-Z]:\\\\.*";
			} else if(isLinux()) {
				cmd = "which";
				regular = "^/.*";
			}

			String[] result = ConsolCmd.exc(new String[] {cmd, path}, true, null);
			if(result == null || result.length <= 0
					|| !result[0].matches(regular)
					|| !new File(result[0]).exists()){
				Log.e("No such file " + ((result != null && result.length > 0) ? result[0] : "- result null"));
				return null;
			}
			realPath = result[0];
		}

		return realPath;
	}

	public static void createShortCut() {
		if(isWindows()) {
			String filePath = Resource.getUTF8Path() + File.separator + "ApkScanner.exe";
			String lnkPath = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + Resource.STR_APP_NAME.getString() + ".lnk";
			try {
				ShellLink.createLink(filePath, lnkPath);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else if(isLinux()) {

		}
	}

	public static boolean hasShortCut() {
		if(isWindows()) {
			//String filePath = Resource.getUTF8Path() + File.separator + "ApkScanner.exe";
			String lnkPath = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + Resource.STR_APP_NAME.getString() + ".lnk";

			if(!new File(lnkPath).exists()) {
				return false;
			}
			try {
				String pathToExistingFile = new ShellLink(lnkPath).resolveTarget();
				Log.e("pathToExistingFile " + pathToExistingFile);
				if(pathToExistingFile == null || !new File(pathToExistingFile).exists()) {
					return false;
				}
			} catch (IOException | ShellLinkException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	public static boolean exec(ArrayList<String> cmd) {
		return exec(cmd.toArray(new String[0]));
	}

	public static boolean exec(String[] cmd)
	{
		try {
			final Process process = new ProcessBuilder(cmd).redirectErrorStream(true).start();
			new Thread(new Runnable() {
				public void run()
				{
					InputStream inputStream = process.getInputStream();
					InputStreamReader inputStreamReader = new InputStreamReader(inputStream/*, encoding*/);
					BufferedReader stdOut = new BufferedReader(inputStreamReader);
					try {
						while (stdOut.readLine() != null);
						stdOut.close();
						inputStreamReader.close();
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					process.destroy();
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
