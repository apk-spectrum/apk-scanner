package com.apkscanner.tool.adb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.android.ddmlib.AdbVersion;
import com.android.ddmlib.AndroidDebugBridge;
import com.apkspectrum.resource.RFile;
import com.apkspectrum.util.FileUtil;
import com.apkspectrum.util.Log;
import com.apkspectrum.util.SystemUtil;
import com.google.common.util.concurrent.ListenableFuture;

public class AdbVersionManager implements Comparator<String> {

	/*
	 * Minimum and maximum version of adb supported. This correspond to
	 * ADB_SERVER_VERSION found in //device/tools/adb/adb.h
	 * MIN_ADB_VERSION found in //tools/base/ddmlib/src/main/java/com/android/ddmlib/AndroidDebugBridge.java
	 */
	public static final AdbVersion MIN_ADB_VERSION = AdbVersion.parseFrom("1.0.20");

	private static JSONObject jsonCache = null;

	private static final HashMap<String, AdbVersion> cacheMap = new HashMap<String, AdbVersion>();
	private static final ArrayList<String> sortedList = new ArrayList<String>();

	public static final String CACHE_FILE_PATH = FileUtil.getTempPath() + File.separator + "adb_list_cache.txt";

	public static AdbVersion getAdbVersion(String adbPath) {
		return getAdbVersion(adbPath, true);	
	}

	public static AdbVersion getAdbVersion(String adbPath, boolean cached) {
		if(adbPath == null) return null;
		return getAdbVersion(new File(adbPath), cached);
	}

	public static AdbVersion getAdbVersion(File adbfile) {
		return getAdbVersion(adbfile, true);
	}

	public static AdbVersion getAdbVersion(File adbfile, boolean cached) {
		if(adbfile == null || !adbfile.exists()) {
			return null;
		}

		AdbVersion version = cached ? cacheMap.get(adbfile.getAbsolutePath()) : null;

		if(version == null) {
			ListenableFuture<AdbVersion> future = AndroidDebugBridge.getAdbVersion(adbfile);

			try {
				version = future.get(5, TimeUnit.SECONDS);
				if(cached && version != null) {
					addCacheMap(adbfile.getAbsolutePath(), version);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (java.util.concurrent.TimeoutException e) {
				String msg = "Unable to obtain result of 'adb version'";
				Log.e(msg);
			} catch (ExecutionException e) {
				Log.e(e.getCause().getMessage());
			}
		}

		return version;
	}

	public static boolean checkAdbVersion(String adbPath) {
		if(adbPath == null) return false;
		return checkAdbVersion(new File(adbPath));
	}

	public static boolean checkAdbVersion(File adbfile) {
		if(adbfile == null || !adbfile.exists()) {
			return false;
		}

		AdbVersion version = getAdbVersion(adbfile);
		if (version.compareTo(MIN_ADB_VERSION) > 0) {
			//mVersionCheck = true;
		} else {
			String message = String.format(
					"Required minimum version of adb: %1$s."
							+ "Current version is %2$s : %3$s", MIN_ADB_VERSION, version, adbfile.getAbsolutePath());
			//Log.logAndDisplay(LogLevel.ERROR, ADB, message);
			Log.e(message);
			version = null;
			return false;
		}

		return true;
	}

	public static boolean checkAdbVersion(AdbVersion version) {
		return (version != null && version.compareTo(MIN_ADB_VERSION) > 0);
	}

	public static void loadDefaultAdbs() {
		for(String runPath: AdbServerMonitor.getRunningAdbPath()) {
			getAdbVersion(runPath, true);
		}
		getAdbVersion(SystemUtil.getRealPath(SystemUtil.isWindows() ? "adb.exe" : "adb"), true);
		getAdbVersion(RFile.BIN_ADB.get(), true);
	}

	public static String[] getAdbListFromCache() {
		return sortedList.toArray(new String[sortedList.size()]);
	}

	public static HashMap<String, AdbVersion> getAdbListFromCacheMap() {
		return cacheMap;
	}

	public static String getAdbLastestVersionFromCache() {
		return sortedList.size() > 0 ? sortedList.get(0) : null;
	}

	public static void addCacheMap(String path, AdbVersion ver) {
		cacheMap.put(path, ver);
		setCache(path, ver);

		int i = 0;
		for(; i < sortedList.size(); i++) {
			if(ver.compareTo(cacheMap.get(sortedList.get(i))) >= 0) {
				break;
			}
		}
		sortedList.add(i, path);
	}

	@Override
	public int compare(String o1, String o2) {
		AdbVersion o1v = getAdbVersion(o1);
		AdbVersion o2v = getAdbVersion(o2);
		if(o1v != null && o2v != null) {
			return o2v.compareTo(o1v);
		} else if(o1v != null) {
			return 1;
		} else if(o2v != null) {
			return -1;
		}
		return 0;
	}

	@SuppressWarnings("unchecked")
	private static void setCache(String path, AdbVersion ver) {
		if(jsonCache == null) {
			jsonCache = new JSONObject();
		}
		String md5AndVer = FileUtil.getMessageDigest(new File(path), "MD5") + "@" + ver;

		if(!md5AndVer.equals(jsonCache.get(path))) {
			jsonCache.put(path, md5AndVer);
			saveCache();
		}
	}

	public static void loadCache()
	{
		synchronized(cacheMap) {
			if(jsonCache == null) {
				File file = new File(CACHE_FILE_PATH);
				if(!file.exists() || file.length() == 0) {
					return;
				}
				try {
					FileReader fileReader = new FileReader(file);
					JSONParser parser = new JSONParser();
					jsonCache = (JSONObject)parser.parse(fileReader);

					fileReader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

				@SuppressWarnings("unchecked")
				Iterator<String> iterator = jsonCache.keySet().iterator();
				ArrayList<String> removeCasheList = new ArrayList<String>();
				while(iterator.hasNext()) {
					String path = iterator.next();
					if(path == null || path.isEmpty()) {
						removeCasheList.add(path);
						continue;
					}
					String md5AndVer = (String)jsonCache.get(path);
					if(md5AndVer == null || md5AndVer.isEmpty() || !md5AndVer.contains("@")) {
						Log.w("Bad format: md5AndVer = '" + md5AndVer + "'");
						removeCasheList.add(path);
						continue;
					}
					String[] tmp = md5AndVer.split("@");
					String md5 = tmp[0];
					if(!md5.isEmpty() && md5.equals(FileUtil.getMessageDigest(new File(path), "MD5"))) {
						addCacheMap(path, AdbVersion.parseFrom(tmp[1]));
					} else {
						Log.w("Unmatchd MD5 " + path);
						removeCasheList.add(path);
					}
					Log.v("load: " + path + ", md5AndVer: " + md5AndVer);
				}

				if(!removeCasheList.isEmpty()) {
					for(String cache: removeCasheList) {
						jsonCache.remove(cache);
					}
					saveCache();
				}
				
			}
		}
	}

	public static void saveCache()
	{
		synchronized(cacheMap) {
			if(jsonCache == null)
				return;

			String transMultiLine = jsonCache.toJSONString()
					.replaceAll("^\\{(.*)\\}$", "{\n$1\n}")
					.replaceAll("(\"[^\"]*\":(\"[^\"]*\")?([^\",]*)?,)", "$1\n");
			//.replaceAll("(\"[^\"]*\":(\"[^\"]*\")?([^\",\\[]*(\\[[^\\]]\\])?)?,)", "$1\n");

			File file = new File(CACHE_FILE_PATH);
			if(!file.exists() || file.length() == 0) {
				FileUtil.makeFolder(file.getParent());
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
			BufferedWriter writer;
			try {
				writer = new BufferedWriter(new FileWriter(CACHE_FILE_PATH));
				writer.write(transMultiLine);
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
