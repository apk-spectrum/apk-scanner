package com.apkscanner.core.scanner;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import com.apkscanner.apkinfo.ActionInfo;
import com.apkscanner.apkinfo.ActivityAliasInfo;
import com.apkscanner.apkinfo.ActivityInfo;
import com.apkscanner.apkinfo.ApkInfo;
import com.apkscanner.apkinfo.CategoryInfo;
import com.apkscanner.apkinfo.CompatibleScreensInfo;
import com.apkscanner.apkinfo.DataInfo;
import com.apkscanner.apkinfo.IntentFilterInfo;
import com.apkscanner.apkinfo.ManifestInfo;
import com.apkscanner.apkinfo.MetaDataInfo;
import com.apkscanner.apkinfo.PermissionInfo;
import com.apkscanner.apkinfo.PermissionTreeInfo;
import com.apkscanner.apkinfo.ProviderInfo;
import com.apkscanner.apkinfo.ReceiverInfo;
import com.apkscanner.apkinfo.ResourceInfo;
import com.apkscanner.apkinfo.ServiceInfo;
import com.apkscanner.apkinfo.SupportsGlTextureInfo;
import com.apkscanner.apkinfo.SupportsScreensInfo;
import com.apkscanner.apkinfo.UsesConfigurationInfo;
import com.apkscanner.apkinfo.UsesFeatureInfo;
import com.apkscanner.apkinfo.UsesLibraryInfo;
import com.apkscanner.apkinfo.UsesPermissionInfo;
import com.apkscanner.apkinfo.WidgetInfo;
import com.apkscanner.resource.Resource;
import com.apkscanner.tool.aapt.AaptNativeWrapper;
import com.apkscanner.tool.aapt.AaptXmlTreeNode;
import com.apkscanner.tool.aapt.AaptXmlTreePath;
import com.apkscanner.util.Log;

public class AaptManifestReader
{
	private AaptXmlTreePath manifestPath;
	private String namespace;
	private ManifestInfo manifestInfo;
	private String[] resourcesWithValue;
	
	public AaptManifestReader()
	{
		this(null);
	}
	
	public AaptManifestReader(AaptXmlTreePath manifestPath)
	{
		this(manifestPath, null);
	}
	
	public AaptManifestReader(AaptXmlTreePath manifestPath, String[] resourcesWithValue)
	{
		this(manifestPath, resourcesWithValue, null);
	}
	
	public AaptManifestReader(AaptXmlTreePath manifestPath, String[] resourcesWithValue, ManifestInfo targetManifest)
	{
		setManifestPath(manifestPath);
		setResources(resourcesWithValue);
		if(targetManifest != null)
			manifestInfo = targetManifest;
		else
			manifestInfo = new ManifestInfo();
	}
	
	public ManifestInfo getManifestInfo()
	{
		return manifestInfo;
	}
	
	public void setManifestPath(AaptXmlTreePath manifestPath)
	{
		this.manifestPath = manifestPath;
		if(manifestPath != null) {
			namespace = manifestPath.getNamespace() + ":";
		}
	}
	
	public void setResources(String[] resourcesWithValue)
	{
		this.resourcesWithValue = resourcesWithValue;
	}
	
	public void readBasicInfo()
	{
		if(manifestPath == null) return;
		AaptXmlTreeNode tagNode = manifestPath.getNode("/manifest"); 
		// package
		if(tagNode != null) {
			manifestInfo.packageName = getAttrValue(tagNode , "package");
			String ver = getAttrValue(tagNode, "versionCode");
			if(ver != null) {
				manifestInfo.versionCode = Integer.parseInt(ver);
			}
			manifestInfo.versionName = getAttrValue(tagNode, "versionName");
			manifestInfo.sharedUserId = getAttrValue(tagNode, "sharedUserId");
			manifestInfo.sharedUserLabels = getAttrResourceValues(tagNode, "sharedUserLabels");
			
			if(manifestInfo.versionName != null && manifestInfo.versionName.startsWith("@")) {
				ResourceInfo[] res = getAttrResourceValues(tagNode, "versionName");
				if(res != null && res[0].name != null) {
					manifestInfo.versionName = res[0].name;
				}
			}
			
			String installLocation = getAttrValue(tagNode, "installLocation");
			if(installLocation == null || installLocation.equals("1")) {
				manifestInfo.installLocation = "internalOnly";
			} else if(installLocation.equals("0")) {
				manifestInfo.installLocation = "auto";
			} else if(installLocation.equals("2")) {
				manifestInfo.installLocation = "preferExternal";
			}
		}
		
		tagNode = manifestPath.getNode("/manifest/uses-sdk");
		if(tagNode != null) {
			String ver = getAttrValue(tagNode, "targetSdkVersion");
			if(ver != null) {
				manifestInfo.usesSdk.targetSdkVersion = Integer.parseInt(ver);
			}
			ver = getAttrValue(tagNode, "minSdkVersion");
			if(ver != null) {
				manifestInfo.usesSdk.minSdkVersion = Integer.parseInt(ver);
			}
			ver = getAttrValue(tagNode, "maxSdkVersion");
			if(ver != null) {
				manifestInfo.usesSdk.maxSdkVersion = Integer.parseInt(ver);
			}
		}

		// label & icon
		tagNode = manifestPath.getNode("/manifest/application");
		if(tagNode != null) {
			manifestInfo.application.labels = getAttrResourceValues(tagNode, "label");
			manifestInfo.application.icons = getAttrResourceValues(tagNode, "icon");
			String bool = getAttrValue(tagNode, "debuggable");
			if(bool != null) manifestInfo.application.debuggable = bool.equals("true");
		} else {
			Log.e("error: node not existed : /manifest/application");
		}
		
		manifestInfo.compatibleScreens = getCompatibleScreens(manifestPath.getNodeList("/manifest/compatible-screens"));
		manifestInfo.supportsScreens = getSupportsScreens(manifestPath.getNodeList("/manifest/supports-screens"));
		manifestInfo.supportsGlTexture = getSupportsGlTexture(manifestPath.getNodeList("/manifest/supports-gl-texture"));
		manifestInfo.usesConfiguration = getUsesConfiguration(manifestPath.getNodeList("/manifest/uses-configuration"));
		manifestInfo.usesFeature = getUsesFeature(manifestPath.getNodeList("/manifest/uses-feature"));
		manifestInfo.usesLibrary = getUsesLibrary(manifestPath.getNodeList("/manifest/uses-library"));
		
        // display to launchur
		AaptXmlTreeNode[] launchers = manifestPath.getNodeList("/manifest/application/activity/intent-filter/category[@"+namespace+"name='android.intent.category.LAUNCHER']");
        if(launchers != null && launchers.length > 0) {
        	for(AaptXmlTreeNode node: launchers) {
        		AaptXmlTreeNode[] actionNode = node.getParent().getNodeList("action");
        		if(actionNode != null) {
        			for(AaptXmlTreeNode action: actionNode) {
        				if("android.intent.action.MAIN".equals(getAttrValue(action, "name"))) {
        		        	manifestInfo.featureFlags |= ManifestInfo.MANIFEST_FEATURE_LAUNCHUR;
        		        	break;
        				}
        			}
        		}
        		if((manifestInfo.featureFlags & ManifestInfo.MANIFEST_FEATURE_LAUNCHUR) != 0)
        			break;
        	}
        }

        // startup
        if(manifestPath.getNode("/manifest/uses-permission[@"+namespace+"name='android.permission.RECEIVE_BOOT_COMPLETED']") != null) {
        	manifestInfo.featureFlags |= ManifestInfo.MANIFEST_FEATURE_STARTUP;
        }
        
        if(manifestPath.getNode("/manifest/instrumentation") != null) {
			manifestInfo.featureFlags |= ManifestInfo.MANIFEST_FEATURE_INSTRUMENTATION;
		}
	}
	
	public void readPermissions()
	{
		// permission
        Log.i("read uses-permission");
        ArrayList<UsesPermissionInfo> usesPermissionList = new ArrayList<UsesPermissionInfo>(); 
        AaptXmlTreeNode[] permTag = manifestPath.getNodeList("/manifest/uses-permission");
        if(permTag != null && permTag.length > 0) {
	        for( int idx=0; idx < permTag.length; idx++ ){
	        	String name = getAttrValue(permTag[idx], "name");
	        	String maxSdk = getAttrValue(permTag[idx], "maxSdkVersion");
	        	UsesPermissionInfo info = PermissionManager.getUsesPermissionInfo(name, maxSdk);
	        	usesPermissionList.add(info);
	        }
	        manifestInfo.usesPermission = usesPermissionList.toArray(new UsesPermissionInfo[0]);
	        usesPermissionList.clear();
        }

        Log.i("read uses-permission-sdk23");
        permTag = manifestPath.getNodeList("/manifest/uses-permission-sdk23");
        if(permTag != null && permTag.length > 0) {
	        for( int idx=0; idx < permTag.length; idx++ ){
	        	String name = getAttrValue(permTag[idx], "name");
	        	String maxSdk = getAttrValue(permTag[idx], "maxSdkVersion");

	        	UsesPermissionInfo info = PermissionManager.getUsesPermissionInfo(name, maxSdk);
	        	usesPermissionList.add(info);
	        }
	        manifestInfo.usesPermissionSdk23 = usesPermissionList.toArray(new UsesPermissionInfo[0]);
	        usesPermissionList.clear();
        }
        usesPermissionList = null;
        
        Log.i("read permission");
        permTag = manifestPath.getNodeList("/manifest/permission");
        if(permTag != null && permTag.length > 0) {
        	ArrayList<PermissionInfo> permissionList = new ArrayList<PermissionInfo>();
	        for( int idx=0; idx < permTag.length; idx++ ){
	        	PermissionInfo info = new PermissionInfo();
	        	if(!"android".equals(manifestInfo.packageName)) {
		        	info.descriptions = getAttrResourceValues(permTag[idx], "description");
		        	info.icons = getAttrResourceValues(permTag[idx], "icon");
		        	info.labels = getAttrResourceValues(permTag[idx], "label");
	        	}
	        	info.name = getAttrValue(permTag[idx], "name");
	        	info.permissionGroup = getAttrValue(permTag[idx], "permissionGroup");
	        	String protectionLevel = getAttrValue(permTag[idx], "protectionLevel");
	        	if(protectionLevel != null && protectionLevel.startsWith("0x")) {
	        		int level = Integer.parseInt(protectionLevel.substring(2), 16);
	        		info.protectionLevel = PermissionInfo.protectionToString(level);
	        	}
	        	permissionList.add(info);
	        }
	        manifestInfo.permission = permissionList.toArray(new PermissionInfo[0]);
	        permissionList.clear();
	        permissionList = null;
        }
        
        Log.i("read permission-tree");
        permTag = manifestPath.getNodeList("/manifest/permission-tree");
        if(permTag != null && permTag.length > 0) {
        	ArrayList<PermissionTreeInfo> permissionList = new ArrayList<PermissionTreeInfo>();
	        for( int idx=0; idx < permTag.length; idx++ ){
	        	PermissionTreeInfo info = new PermissionTreeInfo();
	        	//info.icons = getAttrResourceValues(permTag[idx], "icon");
	        	//info.labels = getAttrResourceValues(permTag[idx], "label");
	        	info.name = getAttrValue(permTag[idx], "name");
	        	permissionList.add(info);
	        }
	        manifestInfo.permissionTree = permissionList.toArray(new PermissionTreeInfo[0]);
	        permissionList.clear();
	        permissionList = null;
        }
	}
	
	public WidgetInfo[] getWidgetList(String apkFilePath)
	{
		
        AaptXmlTreeNode[] widgetTag = manifestPath.getNodeList("/manifest/application/receiver/meta-data[@"+namespace+"name='android.appwidget.provider']/..");
        //Log.i("Normal widgetList cnt = " + xmlAndroidManifest.getLength());
        ArrayList<WidgetInfo> widgetList = new ArrayList<WidgetInfo>();
        for( int idx=0; idx < widgetTag.length; idx++ ){
        	WidgetInfo widget = new WidgetInfo();
        	widget.icons = manifestInfo.application.icons;
        	widget.type = "Normal";
        	widget.lables = getAttrResourceValues(widgetTag[idx], "label");
        	widget.name = getAttrValue(widgetTag[idx], "name");
        	if(widget.name != null && widget.name.startsWith("."))
        		widget.name = manifestInfo.packageName + widget.name;
        	Object[] extraInfo = getWidgetInfo(apkFilePath, getResourceValues(widgetTag[idx].getNode("meta-data").getAttribute(namespace + "resource")));
        	if(extraInfo[0] != null) {
        		widget.icons = (ResourceInfo[])extraInfo[0]; 
        	}
        	if(extraInfo[1] != null) {
        		widget.size = (String)extraInfo[1]; 
        	}
        	widgetList.add(widget);
        }
        
        widgetTag = manifestPath.getNodeList("/manifest/application/activity-alias/intent-filter/action[@"+namespace+"name='android.intent.action.CREATE_SHORTCUT']/../..");
        //Log.i("Shortcut widgetList cnt = " + xmlAndroidManifest.getLength());
        for( int idx=0; idx < widgetTag.length; idx++ ){
        	WidgetInfo widget = new WidgetInfo();
        	widget.icons = manifestInfo.application.icons;
        	widget.type = "Shortcut";
        	widget.lables = manifestInfo.application.labels;
        	widget.name = getAttrValue(widgetTag[idx], "name");
        	if(widget.name != null && widget.name.startsWith("."))
        		widget.name = manifestInfo.packageName + widget.name; 
        	widget.size = "1 X 1";
        	widgetList.add(widget);
        }
        
		return widgetList.toArray(new WidgetInfo[0]);
	}
	

	private Object[] getWidgetInfo(String apkFilePath, ResourceInfo[] widgetRes)
	{
		String Size = "";
		ResourceInfo[] iconPaths = null;
		
		if(widgetRes == null || widgetRes.length <= 0
				|| apkFilePath == null || !(new File(apkFilePath)).exists()) {
			return new Object[] { null, null };
		}

		ArrayList<String> xmlPath = new ArrayList<String>();
		for(ResourceInfo r: widgetRes) {
			xmlPath.add(r.name);
		}
		
		String[] wdgXml = AaptNativeWrapper.Dump.getXmltree(apkFilePath, xmlPath.toArray(new String[0]));
		AaptXmlTreePath widgetTree = new AaptXmlTreePath(wdgXml);
		String widgetNamespace = widgetTree.getNamespace() + ":";
		Log.i("widgetNamespace : " + widgetNamespace);
		
		String width = "0";
		String height = "0";

		AaptXmlTreeNode widgetNode = widgetTree.getNode("/appwidget-provider/@"+widgetNamespace+"minWidth");
		if(widgetNode != null) {
			ResourceInfo[] res = getAttrResourceValues(widgetNode, "minWidth", widgetNamespace);
			if(res == null || res.length == 0 || res[0].name.startsWith("0x")) {
				//width = getResourceValues("0x05", width)[0];
				Log.w("Unknown widget width " + width);
				width = "0";
			} else {
				width = res[0].name.replaceAll("^([0-9]*).*", "$1");
			}
		}

		widgetNode = widgetTree.getNode("/appwidget-provider/@"+widgetNamespace+"minHeight");
		if(widgetNode != null) {
			ResourceInfo[] res = getAttrResourceValues(widgetNode, "minHeight", widgetNamespace);
			if(res == null || res.length == 0 || res[0].name.startsWith("0x")) {
				//height = getResourceValues("0x05", height)[0];
				Log.w("Unknown widget height " + height);
				height = "0";
			} else {
				height = res[0].name.replaceAll("^([0-9]*).*", "$1");
			}
		}
		
		if(!"0".equals(width) && !"0".equals(height)) {
			Size = (int)Math.ceil((Float.parseFloat(width) - 40) / 76 + 1) + " X " + (int)Math.ceil((Float.parseFloat(height) - 40) / 76 + 1);
			Size += "\n(" + width + " X " + height + ")";
		} else {
			Size = "Unknown";
		}

		widgetNode = widgetTree.getNode("/appwidget-provider/@"+widgetNamespace+"resizeMode");
		if(widgetNode != null) {
			String ReSizeMode = getAttrValue(widgetNode, "resizeMode", widgetNamespace);
			if("0x0".equals(ReSizeMode)) {
				ReSizeMode = null;
			} else if("0x1".equals(ReSizeMode)) {
				ReSizeMode = "horizontal";
			} else if("0x2".equals(ReSizeMode)) {
				ReSizeMode = "vertical";
			} else if("0x3".equals(ReSizeMode)) {
				ReSizeMode = "horizontal|vertical";
			}
			if(ReSizeMode != null) {
				Size += "\n\n[ReSizeMode]\n" + ReSizeMode.replaceAll("\\|", "\n");
			}
		}

		widgetNode = widgetTree.getNode("/appwidget-provider/@"+widgetNamespace+"previewImage");
		if(widgetNode != null) {
			iconPaths = getAttrResourceValues(widgetNode, "previewImage", widgetNamespace);
			String jarPath = "jar:file:" + apkFilePath.replaceAll("#", "%23") + "!/";
			for(ResourceInfo r: iconPaths) {
				if(r.name == null) {
					r.name = Resource.IMG_DEF_APP_ICON.getPath();
				} else if(r.name.endsWith("qmg")) {
					r.name = Resource.IMG_QMG_IMAGE_ICON.getPath();
				} else {
					r.name = jarPath + r.name;
				}
			}
		}
		//Log.d("widget IconPath " + IconPath);
		//Log.d("widget size " + Size);

		return new Object[] { iconPaths, Size };
	}
	
	private ActionInfo[] getActionInfo(AaptXmlTreeNode[] actionNodeList)
	{
		if(actionNodeList == null || actionNodeList.length == 0) return null;
		
		ArrayList<ActionInfo> list = new ArrayList<ActionInfo>();
		for(AaptXmlTreeNode node: actionNodeList) {
			ActionInfo info = new ActionInfo();
			info.name = getAttrValue(node, "name");
			list.add(info);
		}
		return list.toArray(new ActionInfo[0]);
	}
	
	private CategoryInfo[] getCategoryInfo(AaptXmlTreeNode[] categoryNodeList)
	{
		if(categoryNodeList == null || categoryNodeList.length == 0) return null;

		ArrayList<CategoryInfo> list = new ArrayList<CategoryInfo>();
		for(AaptXmlTreeNode node: categoryNodeList) {
			CategoryInfo info = new CategoryInfo();
			info.name = getAttrValue(node, "name");
			list.add(info);
		}
		return list.toArray(new CategoryInfo[0]);
	}
	
	private MetaDataInfo[] getMetaDataInfo(AaptXmlTreeNode[] metaDataNodeList)
	{
		if(metaDataNodeList == null || metaDataNodeList.length == 0) return null;

		ArrayList<MetaDataInfo> list = new ArrayList<MetaDataInfo>();
		for(AaptXmlTreeNode node: metaDataNodeList) {
			MetaDataInfo info = new MetaDataInfo();
			info.name = getAttrValue(node, "name");
			info.resources = getAttrResourceValues(node, "resource");
			info.value = getAttrValue(node, "value");
			list.add(info);
		}
		return list.toArray(new MetaDataInfo[0]);
	}
	
	private DataInfo[] getDataInfo(AaptXmlTreeNode[] dataNodeList)
	{
		if(dataNodeList == null || dataNodeList.length == 0) return null;

		ArrayList<DataInfo> list = new ArrayList<DataInfo>();
		for(AaptXmlTreeNode node: dataNodeList) {
			DataInfo info = new DataInfo();
			info.scheme = getAttrValue(node, "scheme");
			info.host = getAttrValue(node, "host");
			info.port = getAttrValue(node, "port");
			info.path = getAttrValue(node, "path");
			info.pathPattern = getAttrValue(node, "pathPattern");
			info.pathPrefix = getAttrValue(node, "pathPrefix");
			info.mimeType = getAttrValue(node, "mimeType");
			list.add(info);
		}
		return list.toArray(new DataInfo[0]);
	}
	
	private IntentFilterInfo[] getIntentFilterInfo(AaptXmlTreeNode[] intentFilterNodeList)
	{
    	if(intentFilterNodeList == null || intentFilterNodeList.length == 0) return null;
    		
    	ArrayList<IntentFilterInfo> intentList = new ArrayList<IntentFilterInfo>();
		for(AaptXmlTreeNode intent: intentFilterNodeList) {
			IntentFilterInfo intentFilter = new IntentFilterInfo();
			intentFilter.ation = getActionInfo(intent.getNodeList("action"));
			intentFilter.category = getCategoryInfo(intent.getNodeList("category"));
			intentFilter.data = getDataInfo(intent.getNodeList("data"));
			intentList.add(intentFilter); 
		}
		return intentList.toArray(new IntentFilterInfo[0]);
	}
	
	private Integer checkIntentFlag(IntentFilterInfo[] intentFilterInfo)
	{
		Integer featureFlag = 0;
    	if(intentFilterInfo == null) return featureFlag;

    	for(IntentFilterInfo intent: intentFilterInfo) {
    		if(intent.ation != null 
    				&& (featureFlag & (ApkInfo.APP_FEATURE_MAIN | ApkInfo.APP_FEATURE_STARTUP)) 
    					!= (ApkInfo.APP_FEATURE_MAIN | ApkInfo.APP_FEATURE_STARTUP)) {
        		for(ActionInfo action: intent.ation) {
    				if("android.intent.action.BOOT_COMPLETED".equals(action.name)) {
    					featureFlag |= ApkInfo.APP_FEATURE_STARTUP;
    				} else if("android.intent.action.MAIN".equals(action.name)) {
    					featureFlag |= ApkInfo.APP_FEATURE_MAIN;
    				}
        		}
    		}
    		if(intent.category != null
    				&& (featureFlag & ApkInfo.APP_FEATURE_LAUNCHER) != ApkInfo.APP_FEATURE_LAUNCHER) {
    			for(CategoryInfo category: intent.category) {
    				if("android.intent.category.LAUNCHER".equals(category.name)) {
    					featureFlag |= ApkInfo.APP_FEATURE_LAUNCHER;
    				}
    			}
    		}
    	}
		
		return featureFlag;
	}
	
	public void readActivityInfo()
	{
		AaptXmlTreeNode[] activityTag = manifestPath.getNodeList("/manifest/application/activity");
		if(activityTag == null || activityTag.length == 0) return;

		ArrayList<ActivityInfo> activityList = new ArrayList<ActivityInfo>();
        for(int idx=0; idx < activityTag.length; idx++ ){
        	ActivityInfo info = new ActivityInfo();
        	info.name = getAttrValue(activityTag[idx], "name");
        	if(info.name != null && info.name.startsWith("."))
        		info.name = manifestInfo.packageName + info.name; 
        	info.permission = getAttrValue(activityTag[idx], "permission");
        	String value = getAttrValue(activityTag[idx], "exported");
        	if(value != null) info.exported = "true".equals(value);
        	value = getAttrValue(activityTag[idx], "enabled");
        	if(value != null) info.enabled = "true".equals(value);
        	
        	info.intentFilter = getIntentFilterInfo(activityTag[idx].getNodeList("intent-filter"));
        	info.metaData = getMetaDataInfo(activityTag[idx].getNodeList("meta-data"));
        	
        	info.featureFlag |= checkIntentFlag(info.intentFilter);
        	
        	if((info.featureFlag & ApkInfo.APP_FEATURE_LAUNCHER) != 0) {
        		activityList.add(0, info);
        	} else {
        		activityList.add(info);	
        	}
        }

        manifestInfo.application.activity = activityList.toArray(new ActivityInfo[0]);
	}
	
	public void readActivityAliasInfo()
	{
		AaptXmlTreeNode[] activityTag = manifestPath.getNodeList("/manifest/application/activity-alias");
		if(activityTag == null || activityTag.length == 0) return;

		ArrayList<ActivityAliasInfo> list = new ArrayList<ActivityAliasInfo>();
        for(int idx=0; idx < activityTag.length; idx++ ){
        	ActivityAliasInfo info = new ActivityAliasInfo();
        	info.name = getAttrValue(activityTag[idx], "name");
        	if(info.name != null && info.name.startsWith("."))
        		info.name = manifestInfo.packageName + info.name; 
        	info.permission = getAttrValue(activityTag[idx], "permission");
        	String value = getAttrValue(activityTag[idx], "exported");
        	if(value != null) info.exported = "true".equals(value);
        	value = getAttrValue(activityTag[idx], "enabled");
        	if(value != null) info.enabled = "true".equals(value);
        	info.icons = getAttrResourceValues(activityTag[idx], "icon");
        	info.labels = getAttrResourceValues(activityTag[idx], "label");
        	info.targetActivity = getAttrValue(activityTag[idx], "targetActivity");
        	
        	info.intentFilter = getIntentFilterInfo(activityTag[idx].getNodeList("intent-filter"));
        	info.metaData = getMetaDataInfo(activityTag[idx].getNodeList("meta-data"));
        	
        	info.featureFlag |= checkIntentFlag(info.intentFilter);
        	
        	list.add(info);
        }

        manifestInfo.application.activityAlias = list.toArray(new ActivityAliasInfo[0]);
	}
	
	public void readServiceInfo()
	{
		AaptXmlTreeNode[] activityTag = manifestPath.getNodeList("/manifest/application/service");
		if(activityTag == null || activityTag.length == 0) return;

		ArrayList<ServiceInfo> list = new ArrayList<ServiceInfo>();
        for(int idx=0; idx < activityTag.length; idx++ ){
        	ServiceInfo info = new ServiceInfo();
        	info.name = getAttrValue(activityTag[idx], "name");
        	if(info.name != null && info.name.startsWith("."))
        		info.name = manifestInfo.packageName + info.name; 
        	info.permission = getAttrValue(activityTag[idx], "permission");
        	String value = getAttrValue(activityTag[idx], "exported");
        	if(value != null) info.exported = "true".equals(value);
        	value = getAttrValue(activityTag[idx], "enabled");
        	if(value != null) info.enabled = "true".equals(value);
        	value = getAttrValue(activityTag[idx], "isolatedProcess");
        	if(value != null) info.isolatedProcess = "true".equals(value);
        	info.icons = getAttrResourceValues(activityTag[idx], "icon");
        	info.labels = getAttrResourceValues(activityTag[idx], "label");
        	info.process = getAttrValue(activityTag[idx], "targetActivity");
        	
        	info.intentFilter = getIntentFilterInfo(activityTag[idx].getNodeList("intent-filter"));
        	info.metaData = getMetaDataInfo(activityTag[idx].getNodeList("meta-data"));

        	info.featureFlag |= checkIntentFlag(info.intentFilter);
        	
        	list.add(info);
        }

        manifestInfo.application.service = list.toArray(new ServiceInfo[0]);
	}
	
	public void readReceiverInfo()
	{
		AaptXmlTreeNode[] activityTag = manifestPath.getNodeList("/manifest/application/receiver");
		if(activityTag == null || activityTag.length == 0) return;

		ArrayList<ReceiverInfo> list = new ArrayList<ReceiverInfo>();
        for(int idx=0; idx < activityTag.length; idx++ ){
        	ReceiverInfo info = new ReceiverInfo();
        	info.name = getAttrValue(activityTag[idx], "name");
        	if(info.name != null && info.name.startsWith("."))
        		info.name = manifestInfo.packageName + info.name; 
        	info.permission = getAttrValue(activityTag[idx], "permission");
        	String value = getAttrValue(activityTag[idx], "exported");
        	if(value != null) info.exported = "true".equals(value);
        	value = getAttrValue(activityTag[idx], "enabled");
        	if(value != null) info.enabled = "true".equals(value);
        	info.icons = getAttrResourceValues(activityTag[idx], "icon");
        	info.labels = getAttrResourceValues(activityTag[idx], "label");
        	info.process = getAttrValue(activityTag[idx], "targetActivity");
        	
        	info.intentFilter = getIntentFilterInfo(activityTag[idx].getNodeList("intent-filter"));
        	info.metaData = getMetaDataInfo(activityTag[idx].getNodeList("meta-data"));
        	
        	info.featureFlag |= checkIntentFlag(info.intentFilter);
        	
        	list.add(info);
        }

        manifestInfo.application.receiver = list.toArray(new ReceiverInfo[0]);
	}
	
	public void readProviderInfo()
	{
		AaptXmlTreeNode[] activityTag = manifestPath.getNodeList("/manifest/application/provider");
		if(activityTag == null || activityTag.length == 0) return;

		ArrayList<ProviderInfo> list = new ArrayList<ProviderInfo>();
        for(int idx=0; idx < activityTag.length; idx++ ){
        	ProviderInfo info = new ProviderInfo();
        	info.name = getAttrValue(activityTag[idx], "name");
        	if(info.name != null && info.name.startsWith("."))
        		info.name = manifestInfo.packageName + info.name;
        	info.permission = getAttrValue(activityTag[idx], "permission");
        	String value = getAttrValue(activityTag[idx], "exported");
        	if(value != null) info.exported = "true".equals(value);
        	value = getAttrValue(activityTag[idx], "enabled");
        	if(value != null) info.enabled = "true".equals(value);
        	info.icons = getAttrResourceValues(activityTag[idx], "icon");
        	info.labels = getAttrResourceValues(activityTag[idx], "label");
        	info.process = getAttrValue(activityTag[idx], "targetActivity");
        	
        	info.metaData = getMetaDataInfo(activityTag[idx].getNodeList("meta-data"));
        	
        	list.add(info);
        }

        manifestInfo.application.provider = list.toArray(new ProviderInfo[0]);
	}
	
	private CompatibleScreensInfo[] getCompatibleScreens(AaptXmlTreeNode[] nodeList)
	{
		if(nodeList == null || nodeList.length == 0) return null;

    	ArrayList<CompatibleScreensInfo> intentList = new ArrayList<CompatibleScreensInfo>();
		for(AaptXmlTreeNode node: nodeList) {
			CompatibleScreensInfo info = new CompatibleScreensInfo();
			
			AaptXmlTreeNode[] screenNode = node.getNodeList("screen");
			if(screenNode != null) {
				ArrayList<CompatibleScreensInfo.Screen> screensList = new ArrayList<CompatibleScreensInfo.Screen>();
				for(AaptXmlTreeNode s: screenNode) {
					CompatibleScreensInfo.Screen screen = new CompatibleScreensInfo.Screen();
					screen.screenSize = getAttrValue(s, "screenSize");
					screen.screenDensity = getAttrValue(s, "screenDensity");
					screensList.add(screen);
				}
				info.screen = screensList.toArray(new CompatibleScreensInfo.Screen[0]);
			}
			
			intentList.add(info); 
		}
		return intentList.toArray(new CompatibleScreensInfo[0]);
	}
	
	private SupportsScreensInfo[] getSupportsScreens(AaptXmlTreeNode[] tagNodeList)
	{
		if(tagNodeList == null || tagNodeList.length == 0) return null; 

		ArrayList<SupportsScreensInfo> list = new ArrayList<SupportsScreensInfo>(); 
		for(AaptXmlTreeNode n: tagNodeList) {
			SupportsScreensInfo info = new SupportsScreensInfo();
			String bool = getAttrValue(n, "resizeable");
			if(bool != null) info.resizeable = bool.equals("true");
			bool = getAttrValue(n, "smallScreens");
			if(bool != null) info.smallScreens = bool.equals("true");
			bool = getAttrValue(n, "normalScreens");
			if(bool != null) info.normalScreens = bool.equals("true");
			bool = getAttrValue(n, "largeScreens");
			if(bool != null) info.largeScreens = bool.equals("true");
			bool = getAttrValue(n, "xlargeScreens");
			if(bool != null) info.xlargeScreens = bool.equals("true");
			bool = getAttrValue(n, "anyDensity");
			if(bool != null) info.anyDensity = bool.equals("true");
			String val = getAttrValue(n, "requiresSmallestWidthDp");
			if(val != null) info.requiresSmallestWidthDp = Integer.parseInt(val);
			val = getAttrValue(n, "compatibleWidthLimitDp");
			if(val != null) info.compatibleWidthLimitDp = Integer.parseInt(val);
			val = getAttrValue(n, "largestWidthLimitDp");
			if(val != null) info.largestWidthLimitDp = Integer.parseInt(val);
			list.add(info);
		}
		
		return list.toArray(new SupportsScreensInfo[0]);
	}
	
	private UsesConfigurationInfo[] getUsesConfiguration(AaptXmlTreeNode[] tagNodeList)
	{
		if(tagNodeList == null || tagNodeList.length == 0) return null; 

		ArrayList<UsesConfigurationInfo> list = new ArrayList<UsesConfigurationInfo>(); 
		for(AaptXmlTreeNode n: tagNodeList) {
			UsesConfigurationInfo info = new UsesConfigurationInfo();
			String bool = getAttrValue(n, "reqFiveWayNav");
			if(bool != null) info.reqFiveWayNav = bool.equals("true");
			bool = getAttrValue(n, "reqHardKeyboard");
			if(bool != null) info.reqHardKeyboard = bool.equals("true");
			info.reqKeyboardType = getAttrValue(n, "reqKeyboardType");
			info.reqNavigation = getAttrValue(n, "reqNavigation");
			info.reqTouchScreen = getAttrValue(n, "reqTouchScreen");
			list.add(info);
		}
		
		return list.toArray(new UsesConfigurationInfo[0]);
	}
	
	private UsesFeatureInfo[] getUsesFeature(AaptXmlTreeNode[] tagNodeList)
	{
		if(tagNodeList == null || tagNodeList.length == 0) return null; 

		ArrayList<UsesFeatureInfo> list = new ArrayList<UsesFeatureInfo>(); 
		for(AaptXmlTreeNode n: tagNodeList) {
			UsesFeatureInfo info = new UsesFeatureInfo();
			info.name = getAttrValue(n, "name");
			String bool = getAttrValue(n, "required");
			if(bool != null) info.required = bool.equals("true");
			String val = getAttrValue(n, "glEsVersion");
			if(val != null && val.startsWith("0x")) info.glEsVersion = Integer.parseInt(val.substring(2), 16);
			list.add(info);
		}
		
		return list.toArray(new UsesFeatureInfo[0]);
	}
	
	private UsesLibraryInfo[] getUsesLibrary(AaptXmlTreeNode[] tagNodeList)
	{
		if(tagNodeList == null || tagNodeList.length == 0) return null; 

		ArrayList<UsesLibraryInfo> list = new ArrayList<UsesLibraryInfo>(); 
		for(AaptXmlTreeNode n: tagNodeList) {
			UsesLibraryInfo info = new UsesLibraryInfo();
			info.name = getAttrValue(n, "name");
			String bool = getAttrValue(n, "required");
			if(bool != null) info.required = bool.equals("true");
			list.add(info);
		}
		
		return list.toArray(new UsesLibraryInfo[0]);
	}
	
	private SupportsGlTextureInfo[] getSupportsGlTexture(AaptXmlTreeNode[] tagNodeList)
	{
		if(tagNodeList == null || tagNodeList.length == 0) return null; 

		ArrayList<SupportsGlTextureInfo> list = new ArrayList<SupportsGlTextureInfo>(); 
		for(AaptXmlTreeNode n: tagNodeList) {
			SupportsGlTextureInfo info = new SupportsGlTextureInfo();
			info.name = getAttrValue(n, "name");
			list.add(info);
		}
		return list.toArray(new SupportsGlTextureInfo[0]);
	}
	
	private String getAttrValue(AaptXmlTreeNode node, String attr)
	{
		return getAttrValue(node, attr, namespace);
	}
	
	private String getAttrValue(AaptXmlTreeNode node, String attr, String namespace)
	{
		String value = node.getAttribute(namespace + attr);
		if(value == null) {
			value = node.getAttribute(attr);
		}
		return value;
	}
	
	private ResourceInfo[] getAttrResourceValues(AaptXmlTreeNode node, String attr)
	{
		return getAttrResourceValues(node, attr, namespace);
	}
	
	public ResourceInfo[] getAttrResourceValues(AaptXmlTreeNode node, String attr, String namespace)
	{
		String value = node.getAttribute(namespace + attr);
		ResourceInfo[] resVal = null;
		if(value == null) {
			value = node.getAttribute(attr);
		}
		//Log.d("getAttrValues() " + node + ", namespace : " + namespace + ", attr : " + attr + ", value : " + value);
		
		while(value != null && value.startsWith("@")) {
			if(value.matches("@0x0*\\s*")) {
				resVal = null;
				break;
			}
			resVal = getResourceValues(value);
			if(resVal == null || resVal.length == 0)
				break;
			value = resVal[0].name;
		}
		if(resVal == null || resVal.length == 0) {
			resVal = new ResourceInfo[] { new ResourceInfo(value, null) };
		}
		return resVal;
	}

	private ResourceInfo[] getResourceValues(String id)
	{
		if(id == null || !id.startsWith("@") || resourcesWithValue == null) {
			return new ResourceInfo[] { new ResourceInfo(id, null) };
		}

		ArrayList<ResourceInfo> values = new ArrayList<ResourceInfo>();
		String filter = "^\\s*resource 0x0*" + id.replaceAll("@0x0*(.*)", "$1") + ".*";
		String config = "";

		for(int i = 0; i < resourcesWithValue.length; i++) {
			if(resourcesWithValue[i].indexOf(" config ") >= 0) {
				config = resourcesWithValue[i].replaceAll(".*config \\(?(\\w*)\\)?.*", "$1");
			}
			if(!resourcesWithValue[i].matches(filter)) continue;
			//Log.i(resourcesWithValue[i]);

			if(i+1 < resourcesWithValue.length) {
				String val = resourcesWithValue[i+1].replaceAll("^\\s*\\([^\\(\\)]*\\) (.*)", "$1").replaceAll("^['\"](.*)['\"]\\s*$", "$1");
				String type = resourcesWithValue[i+1].replaceAll("^\\s*\\(([^\\(\\)]*)\\) .*", "$1");
				if("reference".equals(type) && val.startsWith("0x")) {
					Collections.addAll(values, getResourceValues("@"+val));
				} else {
					values.add(new ResourceInfo(val, config));
				}
				//Log.d("getResourceValues() id " + id + ", val " + val);
			}
		}

		return values.toArray(new ResourceInfo[0]);
	}
}
