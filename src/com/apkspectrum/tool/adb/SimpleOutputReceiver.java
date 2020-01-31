package com.apkspectrum.tool.adb;

import java.util.ArrayList;
import java.util.Arrays;

import com.android.ddmlib.MultiLineReceiver;

public class SimpleOutputReceiver extends MultiLineReceiver {
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
