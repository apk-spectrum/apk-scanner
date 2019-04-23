package com.apkscanner.core.permissionmanager;

import com.apkscanner.data.apkinfo.ApkInfoHelper;
import com.apkscanner.data.apkinfo.PermissionInfo;
import com.apkscanner.data.apkinfo.ResourceInfo;
import com.apkscanner.resource.Resource;

public class PermissionInfoExt extends PermissionInfo implements UnitInformation {
	public int sdk;
	public String comment;
	public String label;
	public String icon;
	public String description;
	public int protectionFlags;

	public PermissionInfoExt() { }

	public PermissionInfoExt(PermissionInfo info) {
		super(info);
		if(info instanceof PermissionInfoExt) {
			PermissionInfoExt infoExt = (PermissionInfoExt) info;
			comment = infoExt.comment;
			sdk = infoExt.sdk;
			label = infoExt.label;
			icon = infoExt.icon;
			description = infoExt.description;
			protectionLevel = infoExt.protectionLevel;
			protectionFlags = infoExt.protectionFlags;
		}
	}

	public PermissionInfoExt(PermissionInfoExt info) {
		this((PermissionInfo)info);
	}

	@Override
	public boolean equals(Object target) {
		if(!(target instanceof PermissionInfoExt)) return false;
		PermissionInfoExt other = (PermissionInfoExt) target;
		return super.equals(target)
				&& objEquals(label, other.label)
				&& objEquals(icon, other.icon)
				&& objEquals(description, other.description)
				&& objEquals(comment, other.comment);
	}

	@Override
	public boolean isDeprecated() {
		return comment != null && comment.contains("@deprecated");
	}

	@Override
	public boolean isRemoved() {
		return super.isRemoved() || (comment != null && comment.contains("@Removed"));
	}

	public boolean isSystemApi() {
		return comment != null && comment.contains("@SystemApi");
	}

	public boolean isHide() {
		return comment != null && comment.contains("@hide");
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getLabel() {
    	return ApkInfoHelper.getResourceValue(getLabels(), Resource.getLanguage());
    }

	@Override
    public String getDescription() {
    	return ApkInfoHelper.getResourceValue(getDescriptions(), Resource.getLanguage());
    }

	@Override
	public String getNonLocalizedDescription() {
		return comment;
	}

	@Override
	public int getApiLevel() {
		return sdk;
	}

	@Override
	public int getProtectionFlags() {
		return protectionFlags;
	}

	@Override
	public String getIcon() {
		return icon;
	}

	public ResourceInfo[] getLabels() {
		if(labels != null) return labels;
		return labels = PermissionManager.getResource(label, sdk);
	}

	public ResourceInfo[] getDescriptions() {
		if(descriptions != null) return descriptions;
		return descriptions = PermissionManager.getResource(description, sdk);
	}

	public ResourceInfo[] getIcons() {
		if(icons != null) return icons;
		return icons = PermissionManager.getResource(icon, sdk);
	}

	public String getSummary() {
		StringBuilder summary = new StringBuilder();
		String label = getLabel();
		if(label != null) {
			summary.append("[" + label + "] : ");
			summary.append(getDescriptions());
		} else {
			summary.append("[" + name + "] : ");
		}
		return summary.toString();
	}
}
