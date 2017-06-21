package com.apkscanner.core.scanner;

import java.io.File;

import org.json.simple.JSONArray;

import com.apkscanner.resource.Resource;
import com.apkscanner.util.ZipFileUtil;

public class EstimatedTimeEnRoute
{
	static private final int MEMORY_CNT = 5;

	static public long calc(String apkFilePath)
	{
		long time = -1;
		if(apkFilePath == null || !new File(apkFilePath).exists())
			return -1;
		
		long rscSize = ZipFileUtil.getFileSize(apkFilePath, "resources.arsc");
		time = getAvrgTime(rscSize);

		return time;
	}
	
	static private long getAvrgTime(long rscSize)
	{
		long sizeMb = rscSize / 1024 / 1024;
		String key = Resource.PROP_SOVE_LEAD_TIME.getValue() + "_" + sizeMb;
		JSONArray preTimes = (JSONArray)Resource.getPropData(key);
		if(preTimes == null)
			return (sizeMb * 1000);
		long avrg = 0;
		for(int i = 0; i < preTimes.size(); i++) {
			avrg += (Long)preTimes.get(i);
		}
		avrg /= preTimes.size();
		return avrg;
	}
	
	@SuppressWarnings("unchecked")
	static public void setRealLeadTime(String apkFilePath, long estimatedTime)
	{
		if(apkFilePath == null || !new File(apkFilePath).exists())
			return;
		long rscSize = ZipFileUtil.getFileSize(apkFilePath, "resources.arsc");
		long sizeMb = rscSize / 1024 / 1024;
		String key = Resource.PROP_SOVE_LEAD_TIME.getValue() + "_" + sizeMb;

		JSONArray preTimes = null;
		preTimes = (JSONArray)Resource.getPropData(key);
		if(preTimes == null) preTimes = new JSONArray();

		if(preTimes.size() >= MEMORY_CNT) {
			preTimes.remove(0);
		}
		preTimes.add(estimatedTime);
		Resource.setPropData(key, preTimes);
	}
}
