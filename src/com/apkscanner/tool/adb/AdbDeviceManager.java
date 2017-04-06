package com.apkscanner.tool.adb;

import java.util.ArrayList;

public class AdbDeviceManager
{
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

	static public DeviceStatus[] scanDevices()
	{
		String[] cmdResult;
		ArrayList<DeviceStatus> deviceList = new ArrayList<DeviceStatus>();

		cmdResult = AdbWrapper.devices(null);

		boolean startList = false;
		for(String output: cmdResult) {
			if(!startList || output.matches("^\\s*$")) {
				if(output.startsWith("List"))
					startList = true;
				continue;
			}
			output = output.replaceAll("^\\s*([^\\s]*)\\s*([^\\s]*)(\\s*(usb:([^\\s]*)))?(\\s*product:([^\\s]*)\\s*model:([^\\s]*)\\s*device:([^\\s]*))?\\s*.*$", "$1 |$2 |$5 |$7 |$8 |$9 ");
			String[] info = output.split("\\|");
			deviceList.add(new DeviceStatus(info[0], info[1], info[2], info[3], info[4], info[5]));
		}
		return deviceList.toArray(new DeviceStatus[0]);
	}

	static public String[] getDeviceList() {
		DeviceStatus[] devices = scanDevices();
		String[] list = new String[devices.length];

		int i = 0;
		for(DeviceStatus dev: devices) {
			if(dev.status.equals("device")) {
				list[i++] = dev.name + "(" + dev.device + ")";
			} else {
				list[i++] = dev.name + "(Unknown) - " + dev.status; 
			}
		}

		return list;
	}
}
