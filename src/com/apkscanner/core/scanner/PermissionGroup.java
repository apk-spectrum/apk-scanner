package com.apkscanner.core.scanner;

import java.util.ArrayList;

import com.apkscanner.data.apkinfo.PermissionInfo;

public class PermissionGroup
{
	public String name;
	public String label;
	public String desc;
	public String icon;
	public String permSummary;
	public ArrayList<PermissionInfo> permList; 
	public boolean hasDangerous;
}
