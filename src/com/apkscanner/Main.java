package com.apkscanner;

import java.awt.EventQueue;
import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.apkscanner.core.AaptWrapper;
import com.apkscanner.core.AdbWrapper;
import com.apkscanner.core.ApktoolManager;
import com.apkscanner.gui.ApkInstaller;
import com.apkscanner.gui.MainUI;
import com.apkscanner.gui.ApkInstaller.InstallButtonStatusListener;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;

public class Main
{
	static private Options allOptions = new Options();
	static private Options normalOptions = new Options();
	static private Options targetApkOptions = new Options();
	static private Options targetPackageOptions = new Options();
	
	static public void main(final String[] args)
	{
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		String cmdType = null;
		
		Resource.setLanguage((String)Resource.PROP_LANGUAGE.getData(System.getProperty("user.language")));
		if(Resource.STR_APP_BUILD_MODE.getString().equals("user")) {
			Log.enableConsoleLog(false);
		}
		
		Log.i(Resource.STR_APP_NAME.getString() + " " + Resource.STR_APP_VERSION.getString() + " " + Resource.STR_APP_BUILD_MODE.getString());
		Log.i("OS : " + System.getProperty("os.name"));
		
		createOpstions();

		try {
			if(args.length > 0) {
				if(!"p".equals(args[0]) && !"package".equals(args[0])
						&& "i".equals(args[0]) && "install".equals(args[0])
						&& "d".equals(args[0]) && "delete-temp-path".equals(args[0]) 
						&& !args[0].startsWith("-") && !args[0].endsWith(".apk") && !args[0].endsWith(".ppk")) {
					throw new ParseException("Missing argument for option: " + args[0]);
				}
				if("p".equals(args[0]) || "package".equals(args[0])) {
					cmdType = "package";
				} else if("i".equals(args[0]) || "install".equals(args[0])) {
					cmdType = "install";
				} else if("d".equals(args[0]) || "delete-temp-path".equals(args[0])) {
					cmdType = "delete-temp-path";
				}
			}

			cmd = parser.parse(allOptions, args);
			if(cmdType == null && !cmd.hasOption("v") && !cmd.hasOption("version")
					&& !cmd.hasOption("h") && !cmd.hasOption("help")) {
				cmdType = "file";
			}
			
			if("package".equals(cmdType)) {
				if(cmd.getArgs().length > 2 || cmd.getArgs().length == 0)
					throw new ParseException("Must be just one that the package");
			} else if("file".equals(cmdType)) {
				if(cmd.getArgs().length > 1 /* || cmd.getArgs().length == 0 */)
					throw new ParseException("Must be just one that the Apk file path");
				
				if(cmd.getArgs().length == 0)
					cmdType = null;

				//if(!cmd.getArgs()[0].endsWith(".apk"))
					//throw new ParseException("Unknown type : " + cmd.getArgs()[0]);
			}
		} catch (ParseException e) {
			String argsList = "";
			for(String s: args) {
				argsList += " " + s;
			}
			//Log.enableConsoleLog(false);
			Log.e(e.toString());
			Log.e("args[" + args.length + "] :" + argsList);
			usage();
			new MainUI();
			System.exit(1);
		}
		
		if("file".equals(cmdType)) {
			solveApkFile(cmd);
		} else if("package".equals(cmdType)) {
			solvePackage(cmd);
		} else if("install".equals(cmdType)) {
			install(cmd);
		} else if("delete-temp-path".equals(cmdType)) {
			deleteTempPath(cmd);
		}else {
			emptyCmd(cmd);			
		}
	}

	static private void emptyCmd(CommandLine cmd)
	{
		Log.v("emptyCmd() ");
		
		 if(!cmd.hasOption("c") && !cmd.hasOption("cui")) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					new MainUI();
				}
			});
		} else {
			usage();
		}
	}
	
	static private void solveApkFile(CommandLine cmd)
	{
		final String apkFilePath = cmd.getArgs()[0];

		Log.v("solveApkFile() " + apkFilePath);
		
		if(!cmd.hasOption("c") && !cmd.hasOption("cui")) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					new MainUI(apkFilePath);
				}
			});
		} else {
			
		}
	}
	
	static private void solvePackage(CommandLine cmd)
	{
		final String apkPathInDevice = cmd.getArgs()[1];
		final String frameworkResPath = cmd.getOptionValue("famework", null);
		final String deviceSerialNum = cmd.getOptionValue("device", null);

		Log.v("solvePackage() " + apkPathInDevice + ", " + frameworkResPath + ", " + deviceSerialNum);
		
		if(!cmd.hasOption("c") && !cmd.hasOption("cui")) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					new MainUI(deviceSerialNum, apkPathInDevice, frameworkResPath);
				}
			});
		} else {
			
		}
	}
	
	static private void install(CommandLine cmd)
	{
		String apkFilePath = cmd.getArgs()[1];
		
		if(apkFilePath == null || apkFilePath.isEmpty() || !new File(apkFilePath).exists()) {
			Log.e("apk is null");
			return;
		}
		apkFilePath = new File(apkFilePath).getAbsolutePath();
		Log.v("install() " + apkFilePath);
		
		if(!cmd.hasOption("c") && !cmd.hasOption("cui")) {
			String tempPath = FileUtil.makeTempPath(apkFilePath.substring(apkFilePath.lastIndexOf(File.separator)));
			String libPath = tempPath + File.separator + "lib" + File.separator;
			String packageName = AaptWrapper.Dump.getBadging(apkFilePath, false)[0].replaceAll(".* name='([^']*)'.*", "$1");
			Log.i("package : " + packageName);
			new ApkInstaller(packageName, apkFilePath, libPath,
					(boolean)Resource.PROP_CHECK_INSTALLED.getData(false), false, new InstallButtonStatusListener() {
				@Override
				public void SetInstallButtonStatus(Boolean Flag) { }

				@Override
				public void OnOpenApk(String path) {
					if((new File(path)).exists())
						Launcher.run(path);
				}
			});
		} else {
			
		}
	}
	
	static private void deleteTempPath(CommandLine cmd)
	{
		String path = cmd.getArgs()[1];
		
		while(path != null && !path.isEmpty() && path.startsWith(FileUtil.getTempPath()) && new File(path).exists()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) { }

			File parent = new File(path).getParentFile();
			Log.i("delete temp APK folder : "  + parent.getPath());
			while(parent != null && parent.exists() && parent.getParentFile() != null 
					&& parent.getParentFile().listFiles().length == 1 
					&& parent.getParentFile().getAbsolutePath().length() > FileUtil.getTempPath().length()) {
				parent = parent.getParentFile();
			}
			FileUtil.deleteDirectory(parent);
		}
	}
	
	static private void createOpstions()
	{
		Option opt = new Option("v", "version", false, "Prints the version then exits");
		allOptions.addOption(opt);
		normalOptions.addOption(opt);

		opt = new Option("h", "help", false, "Prints this help");
		allOptions.addOption(opt);
		normalOptions.addOption(opt);
		
		/*
		opt = new Option( "c", "cui", false, "Prints the result to the command line");
		allOptions.addOption(opt);
		targetApkOptions.addOption(opt);
		targetPackageOptions.addOption(opt);
		
		opt = new Option( "g", "gui", false, "Show result by GUI [default]");
		allOptions.addOption(opt);
		targetApkOptions.addOption(opt);
		targetPackageOptions.addOption(opt);
		*/
		opt = new Option( "i", "install", true, "install APK");
		allOptions.addOption(opt);
		
		opt = new Option( "d", "device", true, "The serial number of device");
		allOptions.addOption(opt);
		targetPackageOptions.addOption(opt);

		opt = new Option( "f", "framework", true, "Uses framework files located in <arg>");
		allOptions.addOption(opt);
		targetApkOptions.addOption(opt);
		targetPackageOptions.addOption(opt);
	}
	
	static private void usage()
	{
		System.out.println(Resource.STR_APP_NAME.getString() + " " + Resource.STR_APP_VERSION.getString());
		System.out.println("with apktool " + ApktoolManager.getApkToolVersion() + " (http://ibotpeaches.github.io/Apktool/)");
		System.out.println(" - Apache License 2.0 (http://www.apache.org/licenses/LICENSE-2.0)");
		System.out.println("with " + AdbWrapper.getVersion() + " (http://developer.android.com/tools/help/adb.html)");
		System.out.println("Programmed by " + Resource.STR_APP_MAKER.getString() + " <" + Resource.STR_APP_MAKER_EMAIL.getString() + ">" + ", 2015");
		System.out.println("Apache License 2.0 (http://www.apache.org/licenses/LICENSE-2.0)");
		System.out.println();
		
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("apkscanner ", normalOptions);
		formatter.printHelp("apkscanner [options] <app_path>", targetApkOptions);
		formatter.printHelp("apkscanner p[ackage] [options] [-d[evice] <serial_number>] [-f[ramework] <framework.apk>] <package>", targetPackageOptions);
	}
}
