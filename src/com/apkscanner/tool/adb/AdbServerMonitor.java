package com.apkscanner.tool.adb;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.android.ddmlib.AdbVersion;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.DdmPreferences;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;
import com.google.common.base.Joiner;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Uninterruptibles;

public final class AdbServerMonitor {

	/*
	 * Minimum and maximum version of adb supported. This correspond to
	 * ADB_SERVER_VERSION found in //device/tools/adb/adb.h
	 * MIN_ADB_VERSION found in //tools/base/ddmlib/src/main/java/com/android/ddmlib/AndroidDebugBridge.java
	 */
	private static final AdbVersion MIN_ADB_VERSION = AdbVersion.parseFrom("1.0.20");

	private static final String ADB = "adb"; //$NON-NLS-1$
	private static final String DDMS = "ddms"; //$NON-NLS-1$

	private static final ArrayList<IAdbDemonChangeListener> sServerListeners =
			new ArrayList<IAdbDemonChangeListener>();

	private static AdbServerMonitor sThis;
	private static final Object sLock = sServerListeners;

	private String mAdbPath;
	private AdbVersion mAdbVersion;

	private boolean mAllowRestart;
	private boolean mForceNewDemon;

	private AdbServerMonitorTask mAdbDemonMonitorTask;


	public interface IAdbDemonChangeListener {
		void adbDemonConnected(String adbPath, AdbVersion version);
		void adbDemonDisconnected();
	}

	private AdbServerMonitor(String adbPath, boolean forceNewDemon, boolean allowRestart) {
		mAdbPath = adbPath;
		mForceNewDemon = forceNewDemon;
		mAllowRestart = allowRestart;
	}

	public static void addAdbDemonChangeListener(IAdbDemonChangeListener listener) {
		synchronized (sLock) {
			if (!sServerListeners.contains(listener)) {
				sServerListeners.add(listener);
			}
		}
	}

	public static void removeAdbDemonChangeListener(IAdbDemonChangeListener listener) {
		synchronized (sLock) {
			sServerListeners.remove(listener);
		}
	}

	private void adbDemonConnected(String adbPath, AdbVersion version) {
		mAdbPath = adbPath;
		mAdbVersion = version;

		IAdbDemonChangeListener[] listenersCopy = null;
		synchronized (sLock) {
			listenersCopy = sServerListeners.toArray(
					new IAdbDemonChangeListener[sServerListeners.size()]);
		}

		for (IAdbDemonChangeListener listener : listenersCopy) {
			try {
				listener.adbDemonConnected(mAdbPath, mAdbVersion);
			} catch (Exception e) {
				Log.e(DDMS, e.toString());
			}
		}
	}

	private void adbDemonDisconnected() {
		IAdbDemonChangeListener[] listenersCopy = null;
		synchronized (sLock) {
			listenersCopy = sServerListeners.toArray(
					new IAdbDemonChangeListener[sServerListeners.size()]);
		}

		for (IAdbDemonChangeListener listener : listenersCopy) {
			try {
				listener.adbDemonDisconnected();
			} catch (Exception e) {
				Log.e(DDMS, e.toString());
			}
		}
	}

	public static AdbServerMonitor startServerAndCreateBridge(String adbPath, boolean forceNewDemon, boolean allowRestart) {
		synchronized (sLock) {
			if (sThis != null) {
				if(sThis.mAdbPath != null && sThis.mAdbPath.equals(adbPath) && !forceNewDemon) {
					return sThis;
				} else {
					sThis.stop();
				}
			}

			sThis = new AdbServerMonitor(adbPath, forceNewDemon, allowRestart);
			sThis.start(forceNewDemon);

			Log.v("DeviceMonitor end");

			return sThis;
		}
	}

	private void start(boolean forceNewDemon) {
		mAdbDemonMonitorTask = new AdbServerMonitorTask(this);
		new Thread(mAdbDemonMonitorTask, "Adb Demon Monitor").start();
	}

	private void stop() {
		mAdbDemonMonitorTask.stop();
	}

	public static AdbVersion checkAdbVersion(String adbPath) {
		// default is bad check
		if(adbPath == null) return null;

		File adb = new File(adbPath);
		if(!adb.exists())
			return null;

		ListenableFuture<AdbVersion> future = AndroidDebugBridge.getAdbVersion(adb);
		AdbVersion version;
		try {
			version = future.get(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		} catch (java.util.concurrent.TimeoutException e) {
			String msg = "Unable to obtain result of 'adb version'";
			//Log.logAndDisplay(LogLevel.ERROR, ADB, msg);
			Log.e(msg);
			return null;
		} catch (ExecutionException e) {
			//Log.logAndDisplay(LogLevel.ERROR, ADB, e.getCause().getMessage());
			Log.e(e.getCause().getMessage());
			//Throwables.propagateIfInstanceOf(e.getCause(), IOException.class);
			return null;
		}
		if (version.compareTo(MIN_ADB_VERSION) > 0) {
			//mVersionCheck = true;
		} else {
			String message = String.format(
					"Required minimum version of adb: %1$s."
							+ "Current version is %2$s : %3$s", MIN_ADB_VERSION, version, adbPath);
			//Log.logAndDisplay(LogLevel.ERROR, ADB, message);
			Log.e(message);
			version = null;
		}
		return version;
	}

	private String[] getAdbLaunchCommand(String option) {
		List<String> command = new ArrayList<String>(4);
		command.add(mAdbPath);
		//if (sAdbServerPort != DEFAULT_ADB_PORT) {
		//	command.add("-P"); //$NON-NLS-1$
		//	command.add(Integer.toString(sAdbServerPort));
		//}
		command.add(option);
		return command.toArray(new String[command.size()]);
	}

	/**
	 * Starts the adb host side server.
	 * @return true if success
	 */
	synchronized boolean startAdb() {
		if (mAdbPath == null) {
			Log.e(ADB,
					"Cannot start adb when AndroidDebugBridge is created without the location of adb."); //$NON-NLS-1$
			return false;
		}
		Process proc;
		int status = -1;
		String[] command = getAdbLaunchCommand("start-server");
		String commandString = Joiner.on(',').join(command);
		try {
			Log.d(DDMS, String.format("Launching '%1$s' to ensure ADB is running.", commandString));
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			if (DdmPreferences.getUseAdbHost()) {
				String adbHostValue = DdmPreferences.getAdbHostValue();
				if (adbHostValue != null && !adbHostValue.isEmpty()) {
					//TODO : check that the String is a valid IP address
					Map<String, String> env = processBuilder.environment();
					env.put("ADBHOST", adbHostValue);
				}
			}
			proc = processBuilder.start();
			ArrayList<String> errorOutput = new ArrayList<String>();
			ArrayList<String> stdOutput = new ArrayList<String>();
			status = grabProcessOutput(proc, errorOutput, stdOutput, false /* waitForReaders */);
		} catch (IOException ioe) {
			Log.e(DDMS, "Unable to run 'adb': " + ioe.getMessage()); //$NON-NLS-1$
			// we'll return false;
		} catch (InterruptedException ie) {
			Log.e(DDMS, "Unable to run 'adb': " + ie.getMessage()); //$NON-NLS-1$
			// we'll return false;
		}
		if (status != 0) {
			Log.e(DDMS,
					String.format("'%1$s' failed -- run manually if necessary", commandString)); //$NON-NLS-1$
			return false;
		} else {
			Log.d(DDMS, String.format("'%1$s' succeeded", commandString)); //$NON-NLS-1$
			return true;
		}
	}
	/**
	 * Get the stderr/stdout outputs of a process and return when the process is done.
	 * Both <b>must</b> be read or the process will block on windows.
	 * @param process The process to get the output from
	 * @param errorOutput The array to store the stderr output. cannot be null.
	 * @param stdOutput The array to store the stdout output. cannot be null.
	 * @param waitForReaders if true, this will wait for the reader threads.
	 * @return the process return code.
	 * @throws InterruptedException
	 */
	private int grabProcessOutput(final Process process, final ArrayList<String> errorOutput,
			final ArrayList<String> stdOutput, boolean waitForReaders)
					throws InterruptedException {
		assert errorOutput != null;
		assert stdOutput != null;
		// read the lines as they come. if null is returned, it's
		// because the process finished
		Thread t1 = new Thread("") { //$NON-NLS-1$
			@Override
			public void run() {
				// create a buffer to read the stderr output
				InputStreamReader is = new InputStreamReader(process.getErrorStream());
				BufferedReader errReader = new BufferedReader(is);
				try {
					while (true) {
						String line = errReader.readLine();
						if (line != null) {
							Log.e(ADB, line);
							errorOutput.add(line);
						} else {
							break;
						}
					}
				} catch (IOException e) {
					// do nothing.
				}
			}
		};
		Thread t2 = new Thread("") { //$NON-NLS-1$
			@Override
			public void run() {
				InputStreamReader is = new InputStreamReader(process.getInputStream());
				BufferedReader outReader = new BufferedReader(is);
				try {
					while (true) {
						String line = outReader.readLine();
						if (line != null) {
							Log.d(ADB, line);
							stdOutput.add(line);
						} else {
							break;
						}
					}
				} catch (IOException e) {
					// do nothing.
				}
			}
		};
		t1.start();
		t2.start();
		// it looks like on windows process#waitFor() can return
		// before the thread have filled the arrays, so we wait for both threads and the
		// process itself.
		if (waitForReaders) {
			try {
				t1.join();
			} catch (InterruptedException e) {
			}
			try {
				t2.join();
			} catch (InterruptedException e) {
			}
		}
		// get the return code from the process
		return process.waitFor();
	}

	public static String getRunningAdbPath() {
		String processName = null;
		if(SystemUtil.isWindows()) {
			processName = "adb.exe";
		} else if(SystemUtil.isLinux()) {
			processName = "adb"; 
		} else {
			Log.e("Unknown OS " + SystemUtil.OS);
			return null;
		}

		String[] list = SystemUtil.getRunningProcessFullPath(processName);
		if(list.length > 1) {
			Log.v("adb process list size : " + list.length);
			String ret = null;
			for(String s: list) {
				Log.v("adb process : " + s);
				if(s != null && !s.isEmpty()) {
					ret = s;
				}
			}
			return ret;
		}
		return (list != null && list.length > 0) ? list[0] : null;
	}

	static class AdbServerMonitorTask  implements Runnable {
		private volatile boolean mQuit;
		private AdbServerMonitor mAdbServerMonitor;
		private boolean isConnected;

		public AdbServerMonitorTask (AdbServerMonitor adbServerMonitor) {
			mAdbServerMonitor = adbServerMonitor;
		}

		@Override
		public void run() {
			isConnected = false;

			Log.v("startServerAndCreateBridge");
			String runningAdbPath = null;
			AdbVersion adbVersion = null;
			if(!mAdbServerMonitor.mForceNewDemon) {
				runningAdbPath = getRunningAdbPath();
				adbVersion = checkAdbVersion(runningAdbPath);
				if(adbVersion != null) {
					isConnected = true;
				} else {
					runningAdbPath = null;
				}
			} 
			Log.v("runningAdbPath " + runningAdbPath + ", version " + adbVersion);

			if(runningAdbPath == null){
				isConnected = mAdbServerMonitor.startAdb();
				runningAdbPath = getRunningAdbPath();
				adbVersion = checkAdbVersion(runningAdbPath);
			}

			AndroidDebugBridge.init(false);
			AndroidDebugBridge.createBridge();
			
			if(isConnected) {
				mAdbServerMonitor.adbDemonConnected(runningAdbPath, adbVersion);
			}
			
			Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);

			do {
				AndroidDebugBridge adb = AndroidDebugBridge.getBridge();
				if(isConnected != isConnected(adb)) {
					isConnected = !isConnected;
					if(isConnected) {
						runningAdbPath = getRunningAdbPath();
						adbVersion = checkAdbVersion(runningAdbPath);
						mAdbServerMonitor.adbDemonConnected(runningAdbPath, adbVersion);
					} else {
						mAdbServerMonitor.adbDemonDisconnected();
					}
				} else if(!isConnected && adb != null && adb.getConnectionAttemptCount() > 5) {
					if(mAdbServerMonitor.mAllowRestart) {
						mAdbServerMonitor.startAdb();
					} else {
						AndroidDebugBridge.disconnectBridge();
					}
				} else if(!isConnected && adb == null) {
					String adbPath = getRunningAdbPath();
					if(adbPath != null) {
						AndroidDebugBridge.createBridge();
					}
					
				}
				Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
			} while (!mQuit);
		}

		private boolean isConnected(AndroidDebugBridge adb) {
			return (adb != null) && adb.isConnected() 
					&& (adb.getConnectionAttemptCount() == 0) /*&& (adb.getRestartAttemptCount() == 0)*/;
		}

		public void stop() {
			mQuit = true;
		}
	}
}
