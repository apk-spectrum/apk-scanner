package com.apkscanner.tool.external;

import java.util.ArrayList;
import java.util.List;

import com.apkscanner.resource.RFile;
import com.apkscanner.util.ConsolCmd;

public class ImgExtractorWrapper
{
	static public String[] extracte(String imgFilePath, String outFolderPath)
	{
		List<String> list = new ArrayList<>();

		String[] result = ConsolCmd.exec(new String[] {RFile.BIN_IMG_EXTRACTOR_WIN.get(), imgFilePath, outFolderPath, "-i"}, true);

		if(result != null) {
			boolean start = false;
			for(String s: result) {
				if(!start) {
					start = s.contains("Extract started");
					continue;
				}
				if(s.startsWith("Extract ")) break;
				list.add(s);
			}
		}
		return list.toArray(new String[list.size()]);
	}

	static public String getSuperblockInfo(String imgFilePath)
	{
		String info = null;

		String[] result = ConsolCmd.exec(new String[] {RFile.BIN_IMG_EXTRACTOR_WIN.get(), imgFilePath, "-s"}, true);

		if(result != null) {
			StringBuilder sb = new StringBuilder();

			boolean start = false;
			for(String s: result) {
				if(!start) {
					start = s.contains("EXT4 superblock info:");
					if(!start) continue;
				}
				if(s.contains("Output information about the")) continue;
				sb.append(s).append("\n");
			}
			info = sb.toString();
		}
		return info;
	}

	static public String getLsInfo(String imgFilePath)
	{
		String info = null;

		String[] result = ConsolCmd.exec(new String[] {RFile.BIN_IMG_EXTRACTOR_WIN.get(), imgFilePath, "-l"}, true);

		if(result != null) {
			StringBuilder sb = new StringBuilder();

			boolean start = false;
			for(String s: result) {
				if(!start) {
					start = s.contains("             ");
					if(!start) continue;
				}
				if(s.contains("Output information about the")) continue;
				sb.append(s).append("\n");
			}
			info = sb.toString();
		}
		return info;
	}
}
