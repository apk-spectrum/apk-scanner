package com.apkscanner.tool.adb;

import java.awt.Dimension;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.DdmPreferences;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.apkscanner.util.Log;

public class AdbDeviceHelper {

	public static class SimpleOutputReceiver extends MultiLineReceiver {
		private ArrayList<String> output = new ArrayList<String>();

		@Override
		public void processNewLines(String[] lines) {
			output.addAll(Arrays.asList(lines));
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		public String[] getOutput() {
			return output.toArray(new String[output.size()]);
		}

		public void clear() {
			output.clear();
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for(String line: output) {
				sb.append(line);
				sb.append("\n");
			}
			return sb.toString();
		}
	}



	public static class CommandRejectedException extends Exception {
		private static final long serialVersionUID = 1L;
		private final boolean mIsDeviceOffline;
		private final boolean mErrorDuringDeviceSelection;
		CommandRejectedException(String message) {
			super(message);
			mIsDeviceOffline = "device offline".equals(message);
			mErrorDuringDeviceSelection = false;
		}
		CommandRejectedException(String message, boolean errorDuringDeviceSelection) {
			super(message);
			mErrorDuringDeviceSelection = errorDuringDeviceSelection;
			mIsDeviceOffline = "device offline".equals(message);
		}
		/**
		 * Returns true if the error is due to the device being offline.
		 */
		public boolean isDeviceOffline() {
			return mIsDeviceOffline;
		}
		/**
		 * Returns whether adb refused to target a given device for the command.
		 * <p/>If false, adb refused the command itself, if true, it refused to target the given
		 * device.
		 */
		public boolean wasErrorDuringDeviceSelection() {
			return mErrorDuringDeviceSelection;
		}
	}

	// public static final long kOkay = 0x59414b4fL;
	// public static final long kFail = 0x4c494146L;
	static final int WAIT_TIME = 5; // spin-wait sleep, in ms
	static final String DEFAULT_ENCODING = "ISO-8859-1"; //$NON-NLS-1$
	/** do not instantiate */
	//private AdbHelper() {
	//}
	/**
	 * Response from ADB.
	 */
	static class AdbResponse {
		public AdbResponse() {
			message = "";
		}
		public boolean okay; // first 4 bytes in response were "OKAY"?
		public String message; // diagnostic string if #okay is false
	}
	/**
	 * Create and connect a new pass-through socket, from the host to a port on
	 * the device.
	 *
	 * @param adbSockAddr
	 * @param device the device to connect to. Can be null in which case the connection will be
	 * to the first available device.
	 * @param devicePort the port we're opening
	 * @throws TimeoutException in case of timeout on the connection.
	 * @throws IOException in case of I/O error on the connection.
	 * @throws AdbCommandRejectedException if adb rejects the command
	 */
	public static SocketChannel open(InetSocketAddress adbSockAddr,
			IDevice device, int devicePort)
					throws IOException, TimeoutException, CommandRejectedException {
		SocketChannel adbChan = SocketChannel.open(adbSockAddr);
		try {
			adbChan.socket().setTcpNoDelay(true);
			adbChan.configureBlocking(false);
			// if the device is not -1, then we first tell adb we're looking to
			// talk to a specific device
			setDevice(adbChan, device);
			byte[] req = createAdbForwardRequest(null, devicePort);
			// Log.hexDump(req);
			write(adbChan, req);
			AdbResponse resp = readAdbResponse(adbChan, false);
			if (!resp.okay) {
				throw new CommandRejectedException(resp.message);
			}
			adbChan.configureBlocking(true);
		} catch (TimeoutException e) {
			adbChan.close();
			throw e;
		} catch (IOException e) {
			adbChan.close();
			throw e;
		} catch (CommandRejectedException e) {
			adbChan.close();
			throw e;
		}
		return adbChan;
	}
	/**
	 * Creates a port forwarding request for adb. This returns an array
	 * containing "####tcp:{port}:{addStr}".
	 * @param addrStr the host. Can be null.
	 * @param port the port on the device. This does not need to be numeric.
	 */
	private static byte[] createAdbForwardRequest(String addrStr, int port) {
		String reqStr;
		if (addrStr == null)
			reqStr = "tcp:" + port;
		else
			reqStr = "tcp:" + port + ":" + addrStr;
		return formAdbRequest(reqStr);
	}
	/**
	 * Create an ASCII string preceded by four hex digits. The opening "####"
	 * is the length of the rest of the string, encoded as ASCII hex (case
	 * doesn't matter).
	 */
	public static byte[] formAdbRequest(String req) {
		String resultStr = String.format("%04X%s", req.length(), req); //$NON-NLS-1$
		byte[] result;
		try {
			result = resultStr.getBytes(DEFAULT_ENCODING);
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace(); // not expected
			return null;
		}
		assert result.length == req.length() + 4;
		return result;
	}
	/**
	 * Reads the response from ADB after a command.
	 * @param chan The socket channel that is connected to adb.
	 * @param readDiagString If true, we're expecting an OKAY response to be
	 * followed by a diagnostic string. Otherwise, we only expect the
	 * diagnostic string to follow a FAIL.
	 * @throws TimeoutException in case of timeout on the connection.
	 * @throws IOException in case of I/O error on the connection.
	 */
	static AdbResponse readAdbResponse(SocketChannel chan, boolean readDiagString)
			throws TimeoutException, IOException {
		AdbResponse resp = new AdbResponse();
		byte[] reply = new byte[4];
		read(chan, reply);
		if (isOkay(reply)) {
			resp.okay = true;
		} else {
			readDiagString = true; // look for a reason after the FAIL
			resp.okay = false;
		}
		// not a loop -- use "while" so we can use "break"
		try {
			while (readDiagString) {
				// length string is in next 4 bytes
				byte[] lenBuf = new byte[4];
				read(chan, lenBuf);
				String lenStr = replyToString(lenBuf);
				int len;
				try {
					len = Integer.parseInt(lenStr, 16);
				} catch (NumberFormatException nfe) {
					Log.w("ddms", "Expected digits, got '" + lenStr + "': "
							+ lenBuf[0] + " " + lenBuf[1] + " " + lenBuf[2] + " "
							+ lenBuf[3]);
					Log.w("ddms", "reply was " + replyToString(reply));
					break;
				}
				byte[] msg = new byte[len];
				read(chan, msg);
				resp.message = replyToString(msg);
				Log.v("ddms", "Got reply '" + replyToString(reply) + "', diag='"
						+ resp.message + "'");
				break;
			}
		} catch (Exception e) {
			// ignore those, since it's just reading the diagnose string, the response will
			// contain okay==false anyway.
		}
		return resp;
	}

	/**
	 * Checks to see if the first four bytes in "reply" are OKAY.
	 */
	static boolean isOkay(byte[] reply) {
		return reply[0] == (byte)'O' && reply[1] == (byte)'K'
				&& reply[2] == (byte)'A' && reply[3] == (byte)'Y';
	}
	/**
	 * Converts an ADB reply to a string.
	 */
	static String replyToString(byte[] reply) {
		String result;
		try {
			result = new String(reply, DEFAULT_ENCODING);
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace(); // not expected
			result = "";
		}
		return result;
	}
	/**
	 * Reads from the socket until the array is filled, or no more data is coming (because
	 * the socket closed or the timeout expired).
	 * <p/>This uses the default time out value.
	 *
	 * @param chan the opened socket to read from. It must be in non-blocking
	 * mode for timeouts to work
	 * @param data the buffer to store the read data into.
	 * @throws TimeoutException in case of timeout on the connection.
	 * @throws IOException in case of I/O error on the connection.
	 */
	static void read(SocketChannel chan, byte[] data) throws TimeoutException, IOException {
		read(chan, data, -1, DdmPreferences.getTimeOut());
	}
	/**
	 * Reads from the socket until the array is filled, the optional length
	 * is reached, or no more data is coming (because the socket closed or the
	 * timeout expired). After "timeout" milliseconds since the
	 * previous successful read, this will return whether or not new data has
	 * been found.
	 *
	 * @param chan the opened socket to read from. It must be in non-blocking
	 * mode for timeouts to work
	 * @param data the buffer to store the read data into.
	 * @param length the length to read or -1 to fill the data buffer completely
	 * @param timeout The timeout value in ms. A timeout of zero means "wait forever".
	 */
	static void read(SocketChannel chan, byte[] data, int length, long timeout)
			throws TimeoutException, IOException {
		ByteBuffer buf = ByteBuffer.wrap(data, 0, length != -1 ? length : data.length);
		int numWaits = 0;
		while (buf.position() != buf.limit()) {
			int count;
			count = chan.read(buf);
			if (count < 0) {
				Log.d("ddms", "read: channel EOF");
				throw new IOException("EOF");
			} else if (count == 0) {
				// TODO: need more accurate timeout?
				if (timeout != 0 && numWaits * WAIT_TIME > timeout) {
					Log.d("ddms", "read: timeout");
					throw new TimeoutException();
				}
				// non-blocking spin
				try {
					Thread.sleep(WAIT_TIME);
				} catch (InterruptedException ie) {
				}
				numWaits++;
			} else {
				numWaits = 0;
			}
		}
	}
	/**
	 * Write until all data in "data" is written or the connection fails or times out.
	 * <p/>This uses the default time out value.
	 * @param chan the opened socket to write to.
	 * @param data the buffer to send.
	 * @throws TimeoutException in case of timeout on the connection.
	 * @throws IOException in case of I/O error on the connection.
	 */
	static void write(SocketChannel chan, byte[] data) throws TimeoutException, IOException {
		write(chan, data, -1, DdmPreferences.getTimeOut());
	}
	/**
	 * Write until all data in "data" is written, the optional length is reached,
	 * the timeout expires, or the connection fails. Returns "true" if all
	 * data was written.
	 * @param chan the opened socket to write to.
	 * @param data the buffer to send.
	 * @param length the length to write or -1 to send the whole buffer.
	 * @param timeout The timeout value. A timeout of zero means "wait forever".
	 * @throws TimeoutException in case of timeout on the connection.
	 * @throws IOException in case of I/O error on the connection.
	 */
	static void write(SocketChannel chan, byte[] data, int length, int timeout)
			throws TimeoutException, IOException {
		ByteBuffer buf = ByteBuffer.wrap(data, 0, length != -1 ? length : data.length);
		int numWaits = 0;
		while (buf.position() != buf.limit()) {
			int count;
			count = chan.write(buf);
			if (count < 0) {
				Log.d("ddms", "write: channel EOF");
				throw new IOException("channel EOF");
			} else if (count == 0) {
				// TODO: need more accurate timeout?
				if (timeout != 0 && numWaits * WAIT_TIME > timeout) {
					Log.d("ddms", "write: timeout");
					throw new TimeoutException();
				}
				// non-blocking spin
				try {
					Thread.sleep(WAIT_TIME);
				} catch (InterruptedException ie) {
				}
				numWaits++;
			} else {
				numWaits = 0;
			}
		}
	}
	/**
	 * tells adb to talk to a specific device
	 *
	 * @param adbChan the socket connection to adb
	 * @param device The device to talk to.
	 * @throws TimeoutException in case of timeout on the connection.
	 * @throws AdbCommandRejectedException if adb rejects the command
	 * @throws IOException in case of I/O error on the connection.
	 */
	static void setDevice(SocketChannel adbChan, IDevice device)
			throws TimeoutException, CommandRejectedException, IOException {
		// if the device is not -1, then we first tell adb we're looking to talk
		// to a specific device
		if (device != null) {
			String msg = "host:transport:" + device.getSerialNumber(); //$NON-NLS-1$
			byte[] device_query = formAdbRequest(msg);
			write(adbChan, device_query);
			AdbResponse resp = readAdbResponse(adbChan, false /* readDiagString */);
			if (!resp.okay) {
				throw new CommandRejectedException(resp.message,
						true/*errorDuringDeviceSelection*/);
			}
		}
	}

	/**
	 * Root the device.
	 *
	 * @param into what to reboot into (recovery, bootloader). Or null to just reboot.
	 * @throws TimeoutException in case of timeout on the connection.
	 * @throws AdbCommandRejectedException if adb rejects the command
	 * @throws IOException in case of I/O error on the connection.
	 */
	public static void root(InetSocketAddress adbSockAddr,
			IDevice device) throws TimeoutException, CommandRejectedException, IOException {
		byte[] request = formAdbRequest("root:"); //$NON-NLS-1$
		SocketChannel adbChan = null;
		try {
			adbChan = SocketChannel.open(adbSockAddr);
			adbChan.configureBlocking(false);
			// if the device is not -1, then we first tell adb we're looking to talk
			// to a specific device
			setDevice(adbChan, device);
			write(adbChan, request);
		} finally {
			if (adbChan != null) {
				adbChan.close();
			}
		}
	}


	/**
	 * Remount the device.
	 *
	 * @throws TimeoutException in case of timeout on the connection.
	 * @throws AdbCommandRejectedException if adb rejects the command
	 * @throws IOException in case of I/O error on the connection.
	 */
	public static void remount(InetSocketAddress adbSockAddr,
			IDevice device) throws TimeoutException, CommandRejectedException, IOException {
		byte[] request = formAdbRequest("remount:"); //$NON-NLS-1$
		SocketChannel adbChan = null;
		try {
			adbChan = SocketChannel.open(adbSockAddr);
			adbChan.configureBlocking(false);
			// if the device is not -1, then we first tell adb we're looking to talk
			// to a specific device
			setDevice(adbChan, device);
			write(adbChan, request);
		} finally {
			if (adbChan != null) {
				adbChan.close();
			}
		}
	}
	public static boolean isRoot(IDevice device) {
		boolean isRoot = false;
		SimpleOutputReceiver outputReceiver = new SimpleOutputReceiver();
		try {
			device.executeShellCommand("id", outputReceiver);
		} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException
				| IOException e) {
			e.printStackTrace();
		}
		String[] result = outputReceiver.getOutput();
		for(String output: result) {
			if(output.indexOf("uid=0") > -1) {
				isRoot = true;
			}
		}
		return isRoot;
	}

	public static boolean hasSu(IDevice device) {
		boolean hasSu = false;
		SimpleOutputReceiver outputReceiver = new SimpleOutputReceiver();
		try {
			device.executeShellCommand("which su", outputReceiver);
		} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException
				| IOException e) {
			e.printStackTrace();
		}
		String[] result = outputReceiver.getOutput();
		for(String output: result) {
			if(output.endsWith("/su")) {
				hasSu = true;
			}
		}
		return hasSu;
	}

	public static boolean isShowingLockscreen(IDevice device)
	{
		final StringBuilder shResultBuilder = new StringBuilder();
		try {
			device.executeShellCommand("dumpsys window policy | grep mShowingLockscreen", new IShellOutputReceiver(){
				@Override
				public void addOutput(byte[] arg0, int arg1, int arg2) {
					shResultBuilder.append(new String(arg0));
				}

				@Override
				public void flush() {
				}

				@Override
				public boolean isCancelled() {
					return false;
				}});
		} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException
				| IOException e) {
			e.printStackTrace();
		}
		String shResult = shResultBuilder.toString().trim();

		boolean islocked = false;
		if(shResult.indexOf("mShowingLockscreen=true") > -1) {
			islocked = true;
		}
		return islocked;
	}

	public static Dimension getPhysicalScreenSize(IDevice device) {
		final StringBuilder shResultBuilder = new StringBuilder();
		try {
			device.executeShellCommand("wm size | grep 'Physical size:'", new IShellOutputReceiver(){
				@Override
				public void addOutput(byte[] arg0, int arg1, int arg2) {
					shResultBuilder.append(new String(arg0));
				}

				@Override
				public void flush() {
				}

				@Override
				public boolean isCancelled() {
					return false;
				}});
		} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException
				| IOException e) {
			e.printStackTrace();
		}
		String shResult = shResultBuilder.toString().trim();

		Dimension size = null;
		if(shResult.indexOf("Physical size:") > -1) {
			String strSize = shResult.replaceAll(".*Physical size:\\s*(\\d+[xX]\\d+).*", "$1");
			if(!strSize.equals(shResult)) {
				String[] temp = strSize.split("[xX]");
				size = new Dimension(Integer.parseInt(temp[0]), Integer.parseInt(temp[1]));
			}
		}
		return size;
	}

	public static boolean isPosibleDismissKeyguard(IDevice device) {
		final StringBuilder shResultBuilder = new StringBuilder();
		try {
			device.executeShellCommand("wm | grep 'wm dismiss-keyguard'", new IShellOutputReceiver(){
				@Override
				public void addOutput(byte[] arg0, int arg1, int arg2) {
					shResultBuilder.append(new String(arg0));
				}

				@Override
				public void flush() {
				}

				@Override
				public boolean isCancelled() {
					return false;
				}});
		} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException
				| IOException e) {
			e.printStackTrace();
		}
		String shResult = shResultBuilder.toString().trim();
		Log.v(shResult);

		return !shResult.isEmpty();
	}

	public static boolean isScreenOn(IDevice device) {
		final StringBuilder shResultBuilder = new StringBuilder();
		try {
			device.executeShellCommand("dumpsys input_method | grep -i 'mActive='", new IShellOutputReceiver(){
				@Override
				public void addOutput(byte[] arg0, int arg1, int arg2) {
					shResultBuilder.append(new String(arg0));
				}

				@Override
				public void flush() {
				}

				@Override
				public boolean isCancelled() {
					return false;
				}});
		} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException
				| IOException e) {
			e.printStackTrace();
		}
		String shResult = shResultBuilder.toString().trim();

		return shResult.toLowerCase().indexOf("mactive=true") > -1;
	}

	public static String[] launchActivity(IDevice device, String activity) {
		final ArrayList<String> output = new ArrayList<String>();
		try {
			device.executeShellCommand("am start -n " + activity, new IShellOutputReceiver(){
				@Override
				public void addOutput(byte[] arg0, int arg1, int arg2) {
					output.add(new String(arg0));
				}

				@Override
				public void flush() {
				}

				@Override
				public boolean isCancelled() {
					return false;
				}});
		} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException
				| IOException e) {
			e.printStackTrace();
		}
		return output.toArray(new String[output.size()]);
	}

	public static void tryDismissKeyguard(IDevice device) {
		String unlockCmd = "wm dismiss-keyguard;";

		if(!AdbDeviceHelper.isPosibleDismissKeyguard(device) 
				&& AdbDeviceHelper.isShowingLockscreen(device)) {
			Dimension screenSize = AdbDeviceHelper.getPhysicalScreenSize(device);
			if(screenSize != null) {
				int y = screenSize.height * 2 / 3;
				unlockCmd = String.format("input touchscreen swipe %d %d %d %d;", 0, y, screenSize.width, y);
			}
		}

		// Screen Turn on
		if(device.getApiLevel() >= 20) {
			// apiLevel="20" platformVersion="Android 4.4W" versionCode="KITKAT_WATCH"
			try {
				device.executeShellCommand("input keyevent KEYCODE_WAKEUP;" + unlockCmd + "input keyevent KEYCODE_MENU;", new IShellOutputReceiver(){
					@Override
					public void addOutput(byte[] arg0, int arg1, int arg2) {
					}

					@Override
					public void flush() {
					}

					@Override
					public boolean isCancelled() {
						return false;
					}});
			} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException
					| IOException e) {
				e.printStackTrace();
			}
		} else {
			Log.v("device api level " + device.getApiLevel());
			String powerKeyCmd = "";
			if(!AdbDeviceHelper.isScreenOn(device)) {
				powerKeyCmd = "input keyevent KEYCODE_POWER;";
			}
			try {
				device.executeShellCommand(powerKeyCmd + unlockCmd + "input keyevent KEYCODE_MENU", new IShellOutputReceiver(){
					@Override
					public void addOutput(byte[] arg0, int arg1, int arg2) {
					}

					@Override
					public void flush() {
					}

					@Override
					public boolean isCancelled() {
						return false;
					}});
			} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException
					| IOException e) {
				e.printStackTrace();
			}
		}
	}
}
