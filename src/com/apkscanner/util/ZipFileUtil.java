package com.apkscanner.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.apkscanner.util.FileUtil.FSStyle;

public class ZipFileUtil
{
	public static String[] findFiles(String zipFilePath, String subffix, String filterRegex)
	{
		ArrayList<String> tempList = new ArrayList<String>();

		try(ZipFile zipFile = new ZipFile(zipFilePath)) {
			Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
			while(zipEntries.hasMoreElements()) {
				ZipEntry e = zipEntries.nextElement();
				String path = e.getName();

				if(filterRegex != null && !path.matches(filterRegex))
					continue;

				if(subffix == null) {
					tempList.add(path);
				} else {
					for(String s: subffix.split(";")) {
						 if(path.endsWith(s)) {
							 tempList.add(path);
							 break;
						 }
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Collections.sort(tempList, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareToIgnoreCase(o2);
			}
		});

		return tempList.toArray(new String[tempList.size()]);
	}

	public static boolean unZip(String zipFilePath, String srcPath, String outPath)
	{
		try(ZipFile zipFile = new ZipFile(zipFilePath)) {
			ZipEntry entry = zipFile.getEntry(srcPath);
			if(entry != null && !entry.isDirectory() && !srcPath.endsWith("/")) {
				File outFolder = new File(outPath.substring(0, outPath.lastIndexOf(File.separator)));
				if(outFolder == null || !outFolder.exists()) {
					Log.v("make folder : " + outFolder.getAbsolutePath());
					if(FileUtil.makeFolder(outFolder.getAbsolutePath())) {
						Log.d("sucess make folder");
					} else {
						Log.d("failure make folder");
					}
				} else {
					if(!outFolder.isDirectory()) {
						Log.e("unZip failure : " + outFolder.getAbsolutePath() + " is not directory");
						return false;
					}
				}

				try(FileOutputStream fos = new FileOutputStream(outPath);
					InputStream is = zipFile.getInputStream(entry)) {
	 				byte[] buffer = new byte[(int) entry.getSize()];
	 				int len = -1;
	 				do {
	 					len = is.read(buffer);
	 					if(len > 0) fos.write(buffer, 0, len);
	 				} while(len > 0);
				}
			} else {
				String targetFolderPath = srcPath.trim();
				if(targetFolderPath.endsWith("/"))
					targetFolderPath = targetFolderPath.substring(0, targetFolderPath.length()-1);
				String[] subPaths = findFiles(zipFilePath, null, "^"+targetFolderPath+"/.*");
				if(outPath.endsWith(File.separator))
					outPath = outPath.substring(0, outPath.length()-1);
				if(subPaths != null && subPaths.length > 0) {
					for(String p: subPaths) {
						if(p.equals(srcPath)) continue;
						String targetOutPath = outPath + p.substring(targetFolderPath.length()).replace("/", File.separator);
						Log.d("src : " + p + ", targetOutPath : " + targetOutPath);
						unZip(zipFilePath, p, targetOutPath);
					}
				} else {
					Log.e("unZip failure : entry was not existed - " + srcPath);
					return false;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static byte[] readData(String zipFilePath, String srcPath)
	{
		if(zipFilePath == null || srcPath == null) return null;

		byte[] buffer = null;
		try(ZipFile zipFile = new ZipFile(zipFilePath)) {
			ZipEntry entry = zipFile.getEntry(srcPath);
			if(entry != null) {
				try(InputStream is = zipFile.getInputStream(entry);
					ByteArrayOutputStream os = new ByteArrayOutputStream()) {
					int nRead;
					byte[] data = new byte[1024];
					while ((nRead = is.read(data, 0, data.length)) != -1) {
						os.write(data, 0, nRead);
				    }
					os.flush();
					buffer = os.toByteArray();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return buffer;
	}

	public static Long getFileSize(String zipFilePath, String srcPath)
	{
		long size = 0;
		try(ZipFile zipFile = new ZipFile(zipFilePath)) {
			ZipEntry entry = zipFile.getEntry(srcPath);
			if(entry != null) {
				size = entry.getSize();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return size;
	}

	public static Long getCompressedSize(String zipFilePath, String srcPath)
	{
		long size = 0;
		try(ZipFile zipFile = new ZipFile(zipFilePath)) {
			ZipEntry entry = zipFile.getEntry(srcPath);
			if(entry != null) {
				size = entry.getCompressedSize();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return size;
	}

	public static String getFileSize(String zipFilePath, String srcPath, FSStyle style)
	{
		return FileUtil.getFileSize(getFileSize(zipFilePath, srcPath), style);
	}

	public static boolean exists(String zipFilePath, String srcPath)
	{
		boolean ret = false;
		try(ZipFile zipFile = new ZipFile(zipFilePath)) {
			ZipEntry entry = zipFile.getEntry(srcPath);
			if(entry != null && !entry.isDirectory()) {
				ret = true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}
}
