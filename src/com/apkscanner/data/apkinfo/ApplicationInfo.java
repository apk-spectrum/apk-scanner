package com.apkscanner.data.apkinfo;

public class ApplicationInfo
{
	public Boolean allowTaskReparenting = null;
    public Boolean allowBackup = null;
    public String backupAgent = null; //"string"
    public ResourceInfo[] banners = null; //"drawable resource"
    public Boolean debuggable = null;
    public ResourceInfo[] descriptions = null; //"string resource"
    public Boolean enabled = null;
    public Boolean hasCode = null;
    public Boolean hardwareAccelerated = null;
    public ResourceInfo[] icons = null; //"drawable resource"
    public Boolean isGame = null;
    public Boolean killAfterRestore = null;
    public Boolean largeHeap = null;
    public ResourceInfo[] labels = null; //"string resource"
    public ResourceInfo[] logos = null; //"drawable resource"
    public String manageSpaceActivity = null; //"string"
    public String name = null; //"string"
    public String permission = null; //"string"
    public Boolean persistent = null;
    public String process = null; //"string"
    public Boolean restoreAnyVersion = null;
    public String requiredAccountType = null; //"string"
    public String restrictedAccountType = null; //"string"
    public Boolean supportsRtl = null;
    public String taskAffinity = null; //"string"
    public Boolean testOnly = null;
    public ResourceInfo[] themes = null; //"resource or theme"
    public String uiOptions = null; //["none" | "splitActionBarWhenNarrow"]
    public Boolean usesCleartextTraffic = null;
    public Boolean vmSafeMode = null;
    
    public ActivityInfo[] activity = null;
    public ActivityAliasInfo[] activityAlias = null;
    public MetaDataInfo[] metaData = null;
    public ServiceInfo[] service = null;
    public ReceiverInfo[] receiver = null;
    public ProviderInfo[] provider = null;
    public UsesLibraryInfo[] usesLibrary = null;
}
