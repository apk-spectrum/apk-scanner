package com.apkscanner;

import java.awt.EventQueue;
import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.apkscanner.core.scanner.AaptScanner;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.gui.MainUI;
import com.apkscanner.gui.dialog.ApkInstallWizard;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;

public class Main implements Runnable
{
	private static final ApkScanner apkScanner = new AaptScanner(null);
	private static final Options allOptions = new Options();
	private static final Options normalOptions = new Options();
	private static final Options targetApkOptions = new Options();
	private static final Options targetPackageOptions = new Options();

	static public void main(final String[] args)
	{
		Resource.setLanguage((String)Resource.PROP_LANGUAGE.getData(SystemUtil.getUserLanguage()));
		if("user".equalsIgnoreCase(Resource.STR_APP_BUILD_MODE.getString())) {
			Log.enableConsoleLog(false);
		}

		Log.i(Resource.STR_APP_NAME.getString() + " " + Resource.STR_APP_VERSION.getString() + " " + Resource.STR_APP_BUILD_MODE.getString());
		Log.i("OS : " + SystemUtil.OS);
		Log.i("file.encoding : " + System.getProperty("file.encoding"));
		Log.i("java.library.path : " + System.getProperty("java.library.path"));
		Log.i("Default Charset : " + Charset.defaultCharset());

		createOpstions();

		CommandLine cmd = null;
		String cmdType = null;
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

			CommandLineParser parser = new DefaultParser();
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

			if("file".equals(cmdType)) {
				solveApkFile(cmd);
			} else if("package".equals(cmdType)) {
				solvePackage(cmd);
			} else if("install".equals(cmdType)) {
				install(cmd);
			} else if("delete-temp-path".equals(cmdType)) {
				deleteTempPath(cmd);
			} else {
				emptyCmd(cmd);
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
			emptyCmd(cmd);
			System.exit(1);
		}
	}

	private static void createAndShowGUI() {
		MainUI mainFrame = new MainUI(apkScanner);
		mainFrame.setVisible(true);
	}

	static private void emptyCmd(CommandLine cmd)
	{
		Log.v("emptyCmd() ");

		if(!cmd.hasOption("c") && !cmd.hasOption("cli")) {
			EventQueue.invokeLater(new Main());
		} else {
			usage();
		}
	}

	static private void solveApkFile(CommandLine cmd)
	{
		final String apkFilePath = cmd.getArgs()[0];

		Log.v("solveApkFile() " + apkFilePath);

		if(!cmd.hasOption("c") && !cmd.hasOption("cli")) {
			EventQueue.invokeLater(new Main());
			apkScanner.openApk(apkFilePath);
		} else {

		}
	}

	static private void solvePackage(CommandLine cmd)
	{
		final String apkPathInDevice = cmd.getArgs()[1];
		final String frameworkResPath = cmd.getOptionValue("famework", null);
		final String deviceSerialNum = cmd.getOptionValue("device", null);

		Log.v("solvePackage() " + apkPathInDevice + ", " + frameworkResPath + ", " + deviceSerialNum);

		if(!cmd.hasOption("c") && !cmd.hasOption("cli")) {
			EventQueue.invokeLater(new Main());
			apkScanner.openPackage(deviceSerialNum, apkPathInDevice, frameworkResPath);
		} else {

		}
	}

	static private void install(CommandLine cmd)
	{
		final String apkFilePath = cmd.getArgs()[1];

		if(apkFilePath == null || apkFilePath.isEmpty() || !new File(apkFilePath).exists()) {
			Log.e("apk is null");
			return;
		}
		Log.v("install() " + apkFilePath);

		if(!cmd.hasOption("c") && !cmd.hasOption("cli")) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					String path = new File(apkFilePath).getAbsolutePath();
					ApkInstallWizard wizard = new ApkInstallWizard(path);
					wizard.start();
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
		opt = new Option( "c", "cli", false, "Prints the result to the command line");
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
		//System.out.println("with apktool " + ApktoolWrapper.getApkToolVersion() + " (http://ibotpeaches.github.io/Apktool/)");
		//System.out.println(" - Apache License 2.0 (http://www.apache.org/licenses/LICENSE-2.0)");
		System.out.println("with Android debug bridge (http://developer.android.com/tools/help/adb.html)");
		System.out.println("Programmed by " + Resource.STR_APP_MAKER.getString() + " <" + Resource.STR_APP_MAKER_EMAIL.getString() + ">" + ", 2015");
		System.out.println("Apache License 2.0 (http://www.apache.org/licenses/LICENSE-2.0)");
		System.out.println();

		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("apkscanner ", normalOptions);
		formatter.printHelp("apkscanner [options] <app_path>", targetApkOptions);
		formatter.printHelp("apkscanner p[ackage] [options] [-d[evice] <serial_number>] [-f[ramework] <framework.apk>] <package>", targetPackageOptions);
	}

	@Override
	public void run() {
		createAndShowGUI();
	}
}
