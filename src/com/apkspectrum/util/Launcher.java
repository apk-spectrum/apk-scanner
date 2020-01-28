package com.apkspectrum.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public final class Launcher
{
	private List<String> defaultCmd;
	private String[] jvmOpts;

	public Launcher() {
		this((Class<?>) null, null);
	}

	public Launcher(String className)
			throws ClassNotFoundException {
		this(className, null);
	}

	public Launcher(Class<?> clazz) {
		this(clazz, null);
	}

	public Launcher(String className, String classPaths)
			throws ClassNotFoundException {
		this(Class.forName(className), classPaths);
	}

	public Launcher(Class<?> clazz, String classPaths) {
		initLauncher(clazz, classPaths);
	}

	private void initLauncher(Class<?> clazz, String classPaths) {
		String className = null;
		if(clazz == null) {
			Attributes attrs = getMainManifestAttributes();
			//URL url = (URL) attrs.get("URL");
			className = attrs.getValue("Main-Class");
			try {
				clazz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				Log.e(e.getMessage());
				return;
			}
			if(classPaths == null) {
				classPaths = attrs.getValue("Class-Path");
			}
		}
		defaultCmd = getDefaultCmd(clazz, classPaths);
	}

	public void setJvmOptions(String... jvmOpts) {
		this.jvmOpts = jvmOpts;
	}

	public void run(String... params) {
		run(params, jvmOpts);
	}

	public void run(String[] params, String[] jvmOpts) {
		SystemUtil.exec(getCommand(params, jvmOpts));
	}

	public Attributes getMainManifestAttributes() {
	    Enumeration<URL> resEnum;
	    try {
	    	resEnum = Launcher.class.getClassLoader().getResources(JarFile.MANIFEST_NAME);
	        while (resEnum.hasMoreElements()) {
                URL url = (URL) resEnum.nextElement();
	            try (InputStream is = url.openStream()){
	                if (is == null) continue;
                    Manifest manifest = new Manifest(is);
                    Attributes mainAttribs = manifest.getMainAttributes();
                    if(mainAttribs.getValue("Main-Class") != null) {
                    	mainAttribs.putValue("URL", url.toExternalForm());
                        return mainAttribs;
	                }
	            } catch (IOException e) { }
	        }
	    } catch (IOException e) { }
	    return null;
	}

	public List<String> getCommand(String[] param) {
		return getCommand(param, jvmOpts);
	}

	public List<String> getCommand(String[] param, String[] jvmOpts) {
		List<String> command = null;
		if(defaultCmd != null) {
			command = new ArrayList<String>(defaultCmd);
			if(jvmOpts != null) {
				command.addAll(1, Arrays.asList(jvmOpts));
			}
			if(param != null) {
				command.addAll(Arrays.asList(param));
			}
		}
		Log.v("command " + command.toString());
		return command;
	}

	public static List<String> getDefaultCmd(Class<?> launchClass, String classPaths) {
		List<String> defaultCmd = new ArrayList<>();

		String appPath = null;
		try {
			appPath = new File(launchClass.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
		} catch (URISyntaxException e) {
			Log.w(e.getMessage());
			appPath = launchClass.getProtectionDomain().getCodeSource().getLocation().getPath();
			if(SystemUtil.isWindows() && appPath.startsWith("/")) {
				appPath = appPath.substring(1);
			}
		}

		StringBuilder classPathBuilder = new StringBuilder(appPath);
		if(classPaths != null && !classPaths.isEmpty()) {
			classPathBuilder.append(File.pathSeparator);
			classPathBuilder.append(classPaths.replaceAll(" ", File.pathSeparator));
		}

		try {
			classPaths = URLDecoder.decode(classPathBuilder.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e1) { }

		defaultCmd.add("java");
		defaultCmd.add("-Dfile.encoding=utf-8");
		defaultCmd.add("-cp");
		defaultCmd.add(classPaths);
		defaultCmd.add(launchClass.getName());

		return defaultCmd;
	}
}