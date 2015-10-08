package com.apkscanner.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFileUtil
{
	public static ArrayList<String> findFiles(String zipFilePath, String subffix, String filterRegex)
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

				if(path.endsWith(subffix))
					tempList.add(path);
			}
			zipFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return tempList;
	}
}
