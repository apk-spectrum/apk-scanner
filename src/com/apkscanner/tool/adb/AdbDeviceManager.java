package com.apkscanner.tool.adb;

import java.util.ArrayList;

import com.apkscanner.util.ConsolCmd;

public class AdbDeviceManager
{
	static private final String adbCmd = AdbWrapper.getAdbCmd();
	
	static public class DeviceInfo
	{
		public final String serialNumber;
		public final String deviceName;
		public final String modelName;
		public final String osVersion;
		public final String buildVersion;
		public final String sdkVersion;
		public final String buildType;
		public final boolean isAbi64;
		public final boolean isRoot;
		
		public DeviceInfo(
				String serialNumber,
				String deviceName,
				String modelName,
				String osVersion,
				String buildVersion,
				String sdkVersion,
				String buildType,
				boolean isAbi64,
				boolean isRoot)
		{
			this.serialNumber = serialNumber;
			this.deviceName = deviceName;
			this.modelName = modelName;
			this.osVersion = osVersion;
			this.buildVersion = buildVersion;
			this.sdkVersion = sdkVersion;
			this.buildType = buildType;
			this.isAbi64 = isAbi64;
			this.isRoot = isRoot;
		}
		
		public String getSummary()
		{
			String s = "Model : " + modelName + " / " + deviceName + "\n";
			s += "Version : " + buildVersion + "(" + buildType + ") / ";
			s += "" + osVersion + "(" + sdkVersion + ")\n";
			return s;
		}
	}

	static public DeviceInfo getDeviceInfo(String name)
	{
		if(adbCmd == null) return null;

		AdbWrapper adbCommander = new AdbWrapper(name, null);
		String serialNumber = name;
		String deviceName = adbCommander.getProp("ro.product.device");
		String modelName = adbCommander.getProp("ro.product.model");
		String osVersion = adbCommander.getProp("ro.build.version.release");
		String buildVersion = adbCommander.getProp("ro.build.version.incremental");
		String sdkVersion = adbCommander.getProp("ro.build.version.sdk");
		String buildType = adbCommander.getProp("ro.build.type");
		boolean isAbi64 = !adbCommander.getProp("ro.product.cpu.abilist64").isEmpty();
		boolean isRoot = adbCommander.root();
		
		return new DeviceInfo(serialNumber, deviceName, modelName, osVersion, buildVersion, sdkVersion, buildType, isAbi64, isRoot);
	}

	static public class DeviceStatus
	{
		public final String name;
		public final String status;
		public final String usb;
		public final String product;
		public final String model;
		public final String device;
		public final String label;

		public DeviceStatus(String name, String status, String usb, String product, String model, String device)
		{
			this.name = name.trim();
			this.status = status.trim();
			this.usb = usb.trim();
			this.product = product.trim();
			this.model = model.trim();
			this.device = device.trim();
			
			String label = this.name;
			if(this.model != null && !this.model.isEmpty()) {
				label += "(" + this.model + ")";
			} else if(this.model != null && !this.model.isEmpty()) {
				label += "(" + this.product + ")";
			} else {
				label += "(Unknown)";
			}
			this.label = label;
		}
		
		public String getSummary()
		{
			String s = "-Device info\n";
			s += "name : " + name + "\n";
			s += "status : " + status + "\n";
			s += "product : " + product + "\n";
			s += "model : " + model + "\n";
			s += "device : " + device + "\n";
			return s;
		}

		@Override
		public String toString() {
		    return this.label;
		}
	}
	
	static public ArrayList<DeviceStatus> scanDevices()
	{
		String[] cmdResult;
		ArrayList<DeviceStatus> deviceList = new ArrayList<DeviceStatus>();

		if(adbCmd == null) return null;

		String[] cmd = {adbCmd, "devices", "-l"};
		cmdResult = ConsolCmd.exc(cmd,true,null);
		
		boolean startList = false;
		for(String output: cmdResult) {
			if(!startList || output.matches("^\\s*$")) {
				if(output.startsWith("List"))
					startList = true;
				continue;
			}
			output = output.replaceAll("^\\s*([^\\s]*)\\s*([^\\s]*)(\\s*(usb:([^\\s]*)))?(\\s*product:([^\\s]*)\\s*model:([^\\s]*)\\s*device:([^\\s]*))?\\s*$", "$1 |$2 |$5 |$7 |$8 |$9 ");
			String[] info = output.split("\\|");
			deviceList.add(new DeviceStatus(info[0], info[1], info[2], info[3], info[4], info[5]));
		}
		return deviceList;
	}
}
