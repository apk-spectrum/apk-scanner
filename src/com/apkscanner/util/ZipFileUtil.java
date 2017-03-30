package com.apkscanner.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.apkscanner.util.FileUtil.FSStyle;

public class ZipFileUtil
{
	public static String[] findFiles(String zipFilePath, String subffix, String filterRegex)
	{
		ArrayList<String> tempList = new ArrayList<String>();
		
		try {
			ZipFile zipFile = new ZipFile(zipFilePath);
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
			zipFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return tempList.toArray(new String[0]);
	}
	
	public static boolean unZip(String zipFilePath, String outPath)
	{
		return false;
	}
	
	public static boolean unZip(String zipFilePath, String srcPath, String outPath)
	{
		try {
			ZipFile zipFile = new ZipFile(zipFilePath);
			ZipEntry entry = zipFile.getEntry(srcPath);
			if(entry != null && !entry.isDirectory() && !srcPath.endsWith("/")) {
				FileOutputStream fos = null;
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
						zipFile.close();
						return false;
					}
				}
				InputStream is = null;
				try {
					fos = new FileOutputStream(outPath);
	 				byte[] buffer = new byte[(int) entry.getSize()];
	 				is = zipFile.getInputStream(entry);
	 				int len = -1;
	 				do {
	 					len = is.read(buffer);
	 					if(len > 0) fos.write(buffer, 0, len);
	 				} while(len > 0);
				} finally {
					if (fos != null) {
						fos.close();
					}
					if(is != null) {
						is.close();
					}
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
					zipFile.close();
					return false;
				}
			}
			zipFile.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static Long getFileSize(String zipFilePath, String srcPath)
	{
		long size = 0;
		try {
			ZipFile zipFile = new ZipFile(zipFilePath);
			ZipEntry entry = zipFile.getEntry(srcPath);
			if(entry != null) {
				size = entry.getSize();
			}
			zipFile.close();
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
		try {
			ZipFile zipFile = new ZipFile(zipFilePath);
			ZipEntry entry = zipFile.getEntry(srcPath);
			if(entry != null && !entry.isDirectory()) {
				ret = true;
			}
			zipFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}
}
