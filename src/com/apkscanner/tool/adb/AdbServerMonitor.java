package com.apkscanner.tool.adb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.android.ddmlib.AdbVersion;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.DdmPreferences;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.Log;
import com.apkscanner.util.SystemUtil;
import com.google.common.base.Joiner;
import com.google.common.util.concurrent.Uninterruptibles;

public final class AdbServerMonitor {
	private static final String ADB = "adb"; //$NON-NLS-1$
	private static final String DDMS = "ddms"; //$NON-NLS-1$
	private static final String SERVER_PORT_ENV_VAR = "ANDROID_ADB_SERVER_PORT"; //$NON-NLS-1$

	// Where to find the ADB bridge.
	static final String DEFAULT_ADB_HOST = "127.0.0.1"; //$NON-NLS-1$
	static final int DEFAULT_ADB_PORT = 5037;

	/** Port where adb server will be started **/
	private static int sAdbServerPort = 0;

	//private static InetAddress sHostAddr;
	//private static InetSocketAddress sSocketAddr;


	private static final ArrayList<IAdbDemonChangeListener> sServerListeners =
			new ArrayList<IAdbDemonChangeListener>();

	private static AdbServerMonitor sThis;
	private static final Object sLock = sServerListeners;

	private String mAdbPath;

	private boolean mAllowRestart;
	private boolean mDemonShared;

	private AdbServerMonitorTask mAdbDemonMonitorTask;


	public interface IAdbDemonChangeListener {
		void adbDemonConnected(String adbPath, AdbVersion version);
		void adbDemonDisconnected();
	}

	private AdbServerMonitor(String adbPath, boolean demonShared, boolean allowRestart) {
		mAdbPath = adbPath;
		mDemonShared = demonShared;
		mAllowRestart = allowRestart;

		initAdbSocketAddr();
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
		IAdbDemonChangeListener[] listenersCopy = null;
		synchronized (sLock) {
			listenersCopy = sServerListeners.toArray(
					new IAdbDemonChangeListener[sServerListeners.size()]);
		}

		AdbWrapper.setAdbCmd(adbPath);
		for (IAdbDemonChangeListener listener : listenersCopy) {
			try {
				listener.adbDemonConnected(adbPath, version);
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

	public static void startServerAndCreateBridgeAsync() {
		Thread thread = new Thread(new Runnable() {
			public void run() {
				startServerAndCreateBridge();
			}
		});
		thread.setPriority(Thread.NORM_PRIORITY);
		thread.start();
	}

	public static AdbServerMonitor startServerAndCreateBridge() {
		AdbVersionManager.loadCache();

		String adbPath = ((String)Resource.PROP_ADB_PATH.getData()).trim();
		if(adbPath == null || adbPath.isEmpty()
				|| !AdbVersionManager.checkAdbVersion(adbPath)) {
			adbPath = AdbVersionManager.getAdbLastestVersionFromCache();
			if(adbPath == null){
				AdbVersionManager.loadDefaultAdbs(); // very higher cost
				adbPath = AdbVersionManager.getAdbLastestVersionFromCache();
			}
		}

		return AdbServerMonitor.startServerAndCreateBridge(
				adbPath,
				(boolean)Resource.PROP_ADB_POLICY_SHARED.getData(),
				(boolean)Resource.PROP_ADB_DEVICE_MONITORING.getData());
	}

	public static AdbServerMonitor startServerAndCreateBridge(String adbPath, boolean demonShared, boolean allowRestart) {
		synchronized (sLock) {
			if (sThis != null) {
				if(demonShared || (sThis.mAdbPath != null && sThis.mAdbPath.equals(adbPath))) {
					return sThis;
				} else {
					sThis.stop();
					sThis = null;
				}
			}

			sThis = new AdbServerMonitor(adbPath, demonShared, allowRestart);
			sThis.start();

			Log.v("DeviceMonitor end");

			return sThis;
		}
	}

	public void start() {
		mAdbDemonMonitorTask = new AdbServerMonitorTask(this);
		new Thread(mAdbDemonMonitorTask, "Adb Demon Monitor").start();
	}

	public void stop() {
		mAdbDemonMonitorTask.stop();
		mAdbDemonMonitorTask = null;
	}

	public static AndroidDebugBridge getAndroidDebugBridge() {
		return getAndroidDebugBridge(5000);
	}

	public static AndroidDebugBridge getAndroidDebugBridge(int waitMs) {
		synchronized (sLock) {
			if(sThis == null) {
				startServerAndCreateBridge();
			}
			if(sThis.mAdbDemonMonitorTask.state == AdbServerMonitorTask.STATUS_NEW
					|| sThis.mAdbDemonMonitorTask.state == AdbServerMonitorTask.STATUS_STARTED) {
				if(null == AndroidDebugBridge.getBridge()) {
					try {
						Log.i("Wait to created AndroidDebugBridge");
						sLock.wait(waitMs);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return AndroidDebugBridge.getBridge();
	}

	private String[] getAdbLaunchCommand(String option) {
		List<String> command = new ArrayList<String>(4);
		command.add(mAdbPath);
		if (sAdbServerPort != DEFAULT_ADB_PORT) {
			command.add("-P"); //$NON-NLS-1$
			command.add(Integer.toString(sAdbServerPort));
		}
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

	public static String[] getRunningAdbPath() {
		String processName = null;
		if(SystemUtil.isWindows()) {
			processName = "adb.exe";
		} else if(SystemUtil.isLinux()) {
			processName = "adb"; 
		} else {
			Log.e("Unknown OS " + SystemUtil.OS);
		}

		ArrayList<String> adbList = new ArrayList<String>();
		if(processName != null) {
			String[] list = SystemUtil.getRunningProcessFullPath(processName);
			if(list != null) {
				Log.v("adb process list size : " + list.length);
				for(String s: list) {
					Log.v("adb process : " + s);
					if(s != null && !s.isEmpty() && !adbList.contains(s)) {
						adbList.add(s);
					}
				}
			}
		}

		return adbList.toArray(new String[adbList.size()]);
	}

	static class AdbServerMonitorTask  implements Runnable {
		private static final int STATUS_NEW = 0;
		private static final int STATUS_STARTED = 1;
		private static final int STATUS_CREATE_BRIDGE = 2;
		private static final int STATUS_RUNNABLE = 3;

		private volatile boolean mQuit;
		private volatile int state = STATUS_NEW;
		private AdbServerMonitor mAdbServerMonitor;
		private boolean isConnected;

		public AdbServerMonitorTask (AdbServerMonitor adbServerMonitor) {
			mAdbServerMonitor = adbServerMonitor;
		}

		@Override
		public void run() {
			state = STATUS_STARTED;
			isConnected = false;

			Log.v("startServerAndCreateBridge");
			String runningAdbPath = null;
			AdbVersion adbVersion = null;
			if(mAdbServerMonitor.mDemonShared) {
				String[] runProcess = null;
				int waitCnt = 0;
				do {
					if(runProcess != null) {
						if(waitCnt++ > 5) {
							Log.d("waiting for running adb daemon only one. but any daemon be not exit. " + runProcess.length);
							break;
						};
						Log.d("waiting for running adb daemon only one. runProcess:" + runProcess.length + ", waitCnt:" + waitCnt);
						Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
					}
					runProcess = getRunningAdbPath();
				} while(runProcess.length > 1);
				if(runProcess != null && runProcess.length > 0) {
					runningAdbPath = runProcess[0];
					adbVersion = AdbVersionManager.getAdbVersion(runningAdbPath);
					if(AdbVersionManager.checkAdbVersion(adbVersion)) {
						isConnected = true;
					} else {
						runningAdbPath = null;
					}
				}
			}
			Log.v("runningAdbPath " + runningAdbPath + ", version " + adbVersion);

			if(runningAdbPath == null){
				isConnected = mAdbServerMonitor.startAdb();
				runningAdbPath = mAdbServerMonitor.mAdbPath;
				adbVersion = AdbVersionManager.getAdbVersion(runningAdbPath);
			}

			synchronized(sLock) {
				try{
					AndroidDebugBridge.init(false);
				} catch (Exception e) {}
				AndroidDebugBridge.createBridge();
				Log.i("AndroidDebugBridge createBridge() notifyAll");
				state = STATUS_CREATE_BRIDGE;
				sLock.notifyAll();
			}

			if(isConnected) {
				mAdbServerMonitor.adbDemonConnected(runningAdbPath, adbVersion);
			}

			Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);

			state = STATUS_RUNNABLE;
			do {
				AndroidDebugBridge adb = AndroidDebugBridge.getBridge();
				if(isConnected != isConnected(adb)) {
					isConnected = !isConnected;
					if(isConnected) {
						String[] adbPath = getRunningAdbPath();
						if(adbPath != null && adbPath.length == 1) {
							runningAdbPath = adbPath[0];
							adbVersion = AdbVersionManager.getAdbVersion(runningAdbPath);
							mAdbServerMonitor.adbDemonConnected(runningAdbPath, adbVersion);
						} else {
							if(adbPath.length > 1) {
								Log.d("current running adb is multiple.");
								for(String s: adbPath) {
									Log.d("adb:" + s + ", " + AdbVersionManager.getAdbVersion(s));
								}
							} else {
								Log.d("no such running adb...");
							}
							isConnected = false;
						}
					} else {
						mAdbServerMonitor.adbDemonDisconnected();
					}
				} else if(!isConnected && adb != null && adb.getConnectionAttemptCount() > 5) {
					if(mAdbServerMonitor.mAllowRestart) {
						runningAdbPath = mAdbServerMonitor.mAdbPath;
						adbVersion = AdbVersionManager.getAdbVersion(runningAdbPath);
						mAdbServerMonitor.adbDemonConnected(runningAdbPath, adbVersion);

						isConnected = mAdbServerMonitor.startAdb();

						if(!isConnected){
							Log.e("Failure: startAdb");
							mAdbServerMonitor.adbDemonDisconnected();	
						}
					} else {
						AndroidDebugBridge.disconnectBridge();
					}
				} else if(!isConnected && adb == null) {
					String[] adbPath = getRunningAdbPath();
					if(adbPath != null) {
						if(adbPath.length == 1) {
							runningAdbPath = adbPath[0];
							AndroidDebugBridge.createBridge();
						} else if(adbPath.length > 1) {
							Log.d("current running adb is multiple.");
							for(String s: adbPath) {
								Log.d("adb:" + s);
							}
						}
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

	/**
	 * Instantiates sSocketAddr with the address of the host's adb process.
	 */
	private static void initAdbSocketAddr() {
		sAdbServerPort = getAdbServerPort();
		/*
		try {
			//sAdbServerPort = getAdbServerPort();
			//sHostAddr = InetAddress.getByName(DEFAULT_ADB_HOST);
			//sSocketAddr = new InetSocketAddress(sHostAddr, sAdbServerPort);
		} catch (UnknownHostException e) {
			// localhost should always be known.
		}
		 */
	}
	/**
	 * Returns the port where adb server should be launched. This looks at:
	 * <ol>
	 * <li>The system property ANDROID_ADB_SERVER_PORT</li>
	 * <li>The environment variable ANDROID_ADB_SERVER_PORT</li>
	 * <li>Defaults to {@link #DEFAULT_ADB_PORT} if neither the system property nor the env var
	 * are set.</li>
	 * </ol>
	 *
	 * @return The port number where the host's adb should be expected or started.
	 */
	private static int getAdbServerPort() {
		// check system property
		Integer prop = Integer.getInteger(SERVER_PORT_ENV_VAR);
		if (prop != null) {
			try {
				return validateAdbServerPort(prop.toString());
			} catch (IllegalArgumentException e) {
				String msg = String.format(
						"Invalid value (%1$s) for ANDROID_ADB_SERVER_PORT system property.",
						prop);
				Log.w(DDMS, msg);
			}
		}
		// when system property is not set or is invalid, parse environment property
		try {
			String env = System.getenv(SERVER_PORT_ENV_VAR);
			if (env != null) {
				return validateAdbServerPort(env);
			}
		} catch (SecurityException ex) {
			// A security manager has been installed that doesn't allow access to env vars.
			// So an environment variable might have been set, but we can't tell.
			// Let's log a warning and continue with ADB's default port.
			// The issue is that adb would be started (by the forked process having access
			// to the env vars) on the desired port, but within this process, we can't figure out
			// what that port is. However, a security manager not granting access to env vars
			// but allowing to fork is a rare and interesting configuration, so the right
			// thing seems to be to continue using the default port, as forking is likely to
			// fail later on in the scenario of the security manager.
			Log.w(DDMS,
					"No access to env variables allowed by current security manager. "
							+ "If you've set ANDROID_ADB_SERVER_PORT: it's being ignored.");
		} catch (IllegalArgumentException e) {
			String msg = String.format(
					"Invalid value (%1$s) for ANDROID_ADB_SERVER_PORT environment variable (%2$s).",
					prop, e.getMessage());
			Log.w(DDMS, msg);
		}
		// use default port if neither are set
		return DEFAULT_ADB_PORT;
	}
	/**
	 * Returns the integer port value if it is a valid value for adb server port
	 * @param adbServerPort adb server port to validate
	 * @return {@code adbServerPort} as a parsed integer
	 * @throws IllegalArgumentException when {@code adbServerPort} is not bigger than 0 or it is
	 * not a number at all
	 */
	private static int validateAdbServerPort(String adbServerPort)
			throws IllegalArgumentException {
		try {
			// C tools (adb, emulator) accept hex and octal port numbers, so need to accept them too
			int port = Integer.decode(adbServerPort);
			if (port <= 0 || port >= 65535) {
				throw new IllegalArgumentException("Should be > 0 and < 65535");
			}
			return port;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Not a valid port number");
		}
	}
}
