package com.apkscanner.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import com.apkscanner.resource.RStr;


public class FileUtil
{
	public static byte[] readData(String filePath)
	{
		if(filePath == null) return null;
		File f = new File(filePath);
		byte[] buffer = new byte[(int) f.length()];
		try(InputStream is = new FileInputStream(filePath);
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
		return buffer;
	}

	public static void copy(String src, String dest) {
	    try (
		  InputStream in = new BufferedInputStream(new FileInputStream(src));
		  OutputStream out = new BufferedOutputStream(new FileOutputStream(dest))) {

			byte[] buffer = new byte[1024];
			int lengthRead;
			while ((lengthRead = in.read(buffer)) > 0) {
				out.write(buffer, 0, lengthRead);
				out.flush();
			}
	    } catch (IOException e) {
	    	e.printStackTrace();
		}
	}

	public static ArrayList<String> findFiles(File f, String subffix, String filterRegex)
	{
		ArrayList<String> tempList = new ArrayList<String>();
		File[] list = f.listFiles();
		if(list == null) {
			Log.i("findFiles() list is null : " + f.toString());
			return tempList;
		}
		for (int i=0; i<list.length; i++) {
			if(filterRegex != null && !list[i].getAbsolutePath().matches(filterRegex)) {
				continue;
			}
			if (list[i].isDirectory()) {
				tempList.addAll(findFiles(list[i], subffix, filterRegex));
			} else {
				if(list[i].getName().endsWith(subffix)) {
					tempList.add(list[i].getAbsolutePath());
				}
			}
		}
		return tempList;
	}

	public static String getTempPath()
	{
		String tempPath = SystemUtil.getTemporaryPath();
		if(!tempPath.endsWith(File.separator)) tempPath += File.separator;
		tempPath += "ApkScanner";

		return tempPath;
	}

	public static String makeTempPath(String apkFilePath)
	{
		String tempPath = getTempPath();

		tempPath += apkFilePath.substring(apkFilePath.indexOf(File.separator),apkFilePath.lastIndexOf(".")).replaceAll("#", "");

		if((new File(tempPath)).exists()) {
			int n;
			for(n=1; (new File(tempPath+"_"+n)).exists(); n++) ;
			tempPath += "_" + n;
		}

		return tempPath;
	}

	public static Boolean makeFolder(String dirPath)
	{
		if(dirPath.endsWith(File.separator)) {
			dirPath = dirPath.substring(0, dirPath.length()-1);
		}
		File newDir = new File(dirPath);
		File parentDir = new File(dirPath.substring(0, dirPath.lastIndexOf(File.separator)));
		if(!parentDir.exists()) {
			if(!makeFolder(parentDir.getAbsolutePath()))
				return false;
		}
		if(!newDir.exists()) {
			return newDir.mkdir();
		}
		return false;
	}

    public static boolean deleteDirectory(File path)
    {
		if(!path.exists()) {
			return false;
		}
		if(path.isDirectory()) {
			File[] files = path.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					deleteDirectory(file);
				} else {
					file.delete();
				}
			}
		}
		return path.delete();
    }

	public enum FSStyle
	{
		NONE(-1),
		BYTES(0),
		KB(1),
		MB(2),
		GB(3),
		TB(4),
		SHORT(5),
		FULL(6);

		private final int value;
	    private FSStyle(int value)
	    {
	        this.value = value;
	    }

	    static FSStyle fromValue(int value)
	    {
	        for (FSStyle my: FSStyle.values()) {
	            if (my.value == value) {
	                return my;
	            }
	        }
	        return null;
	    }

	    public int getValue()
	    {
	        return value;
	    }

		public String toString()
		{
			String unit = null;
			switch(this) {
			case BYTES: unit = RStr.FILE_SIZE_BYTES.get(); break;
			case KB: unit = RStr.FILE_SIZE_KB.get(); break;
			case MB: unit = RStr.FILE_SIZE_MB.get(); break;
			case GB: unit = RStr.FILE_SIZE_GB.get(); break;
			case TB: unit = RStr.FILE_SIZE_TB.get(); break;
			default: break;
			}
			return unit;
		}
	}

	public static String getFileSize(File file, FSStyle style) {
		if (!file.exists() || !file.isFile()) {
			return "-1";
		}
		return getFileSize(file.length(), style);
	}

	public static String getFileSize(long length, FSStyle style) {
		double LengthbyUnit = (double) length;
		int Unit = 0;
		String strUnit = null;

		switch(style) {
		case BYTES: case KB: case MB: case GB: case TB:
			Unit = style.getValue();
			while (Unit-- > 0) {
				LengthbyUnit = LengthbyUnit / 1024;
			}
			strUnit = " " + style;
			break;
		case SHORT: case FULL:
			Unit = 0;
			while (LengthbyUnit > 1024 && Unit < 5) {
				LengthbyUnit = LengthbyUnit / 1024;
				Unit++;
			}
			strUnit = " " + FSStyle.fromValue(Unit);
			break;
		case NONE: default:
			strUnit = "";
			break;
		}

		DecimalFormat df;
		if(FSStyle.fromValue(Unit) == FSStyle.BYTES) {
			df = new DecimalFormat("#,##0");
		} else {
			df = new DecimalFormat("#,##0.00");
		}

		StringBuilder result = new StringBuilder(df.format(LengthbyUnit).length());
		result.append(df.format(LengthbyUnit) + strUnit);

		if(style.equals(FSStyle.FULL)) {
			df = new DecimalFormat("#,##0");
			result.append(" (" + df.format(length) +" "+ FSStyle.BYTES +")");
		}

		return result.toString();
	}


    /**
     * Converts a byte to hex digit and writes to the supplied buffer
     */
    private static void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                            '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }

    /**
     * Converts a byte array to hex string
     */
    private static String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();
        int len = block.length;
        for (int i = 0; i < len; i++) {
             byte2hex(block[i], buf);
             if (i < len-1) {
                 buf.append(":");
             }
        }
        return buf.toString();
    }

	public static String getMessageDigest(File file, String algorithm) {
		if(file.canRead()) {
			try {
				MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
				messageDigest.update(Files.readAllBytes(file.toPath()));
				byte[] hash = messageDigest.digest();
				return toHexString(hash);
			} catch (IOException | NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
