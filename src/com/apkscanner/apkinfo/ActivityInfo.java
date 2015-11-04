package com.apkscanner.apkinfo;

public class ActivityInfo
{
	public static final int ACTIVITY_FEATURE_MAIN = 0x1;
	public static final int ACTIVITY_FEATURE_LAUNCHUR = 0x2;
	public static final int ACTIVITY_FEATURE_STARTUP = 0x4;
	
	public Boolean allowEmbedded = null;
	public Boolean allowTaskReparenting = null;
	public Boolean alwaysRetainTaskState = null;
	public Boolean autoRemoveFromRecents = null;
	public ResourceInfo[] banners = null; // "drawable resource"
	public Boolean clearTaskOnLaunch = null;
	public String configChanges = null; /*["mcc", "mnc", "locale",
	                                 "touchscreen", "keyboard", "keyboardHidden",
	                                 "navigation", "screenLayout", "fontScale",
	                                 "uiMode", "orientation", "screenSize",
	                                 "smallestScreenSize"] */
	public String documentLaunchMode = null; /*["intoExisting" | "always" |
	                                  "none" | "never"] */
	public Boolean enabled = null;
	public Boolean excludeFromRecents = null;
	public Boolean exported = null;
	public Boolean finishOnTaskLaunch = null;
	public Boolean hardwareAccelerated = null;
	public ResourceInfo[] icons = null; // "drawable resource"
	public ResourceInfo[] labels = null; // "string resource"
	public String launchMode = null; /* ["multiple" | "singleTop" |
	                              "singleTask" | "singleInstance"]*/
	public Integer maxRecents = null;
	public Boolean multiprocess = null;
	public String name = null; //"string"
	public Boolean noHistory = null;  
	public String parentActivityName = null; //"string" 
	public String permission = null; //"string"
	public String process = null; //"string"
	public Boolean relinquishTaskIdentity = null;
	public String screenOrientation = null; /*["unspecified" | "behind" |
	                                     "landscape" | "portrait" |
	                                     "reverseLandscape" | "reversePortrait" |
	                                     "sensorLandscape" | "sensorPortrait" |
	                                     "userLandscape" | "userPortrait" |
	                                     "sensor" | "fullSensor" | "nosensor" |
	                                     "user" | "fullUser" | "locked"] */
	public Boolean stateNotNeeded = null;
	public String taskAffinity = null; //"string"
	public ResourceInfo[] themes = null; // "resource or theme"
	public String uiOptions = null; // ["none" | "splitActionBarWhenNarrow"]
	public String windowSoftInputMode = null; /*["stateUnspecified",
	                                       "stateUnchanged", "stateHidden",
	                                       "stateAlwaysHidden", "stateVisible",
	                                       "stateAlwaysVisible", "adjustUnspecified",
	                                       "adjustResize", "adjustPan"] */
	
	public Integer featureFlag = 0;

	public IntentFilterInfo[] intentFilter = null;
	public MetaDataInfo[] metaData = null;
}
