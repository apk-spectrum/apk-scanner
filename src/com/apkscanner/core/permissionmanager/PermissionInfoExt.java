package com.apkscanner.core.permissionmanager;

import com.apkscanner.data.apkinfo.PermissionInfo;
import com.apkscanner.data.apkinfo.ResourceInfo;

public class PermissionInfoExt extends PermissionInfo {
	public int sdk;
	public String comment;
	public String label;
	public String icon;
	public String description;

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
				&& stringEquals(label, other.label)
				&& stringEquals(icon, other.icon)
				&& stringEquals(description, other.description)
				&& stringEquals(comment, other.comment);
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

	public ResourceInfo[] getLabels() {
		if(labels != null) return labels;
		labels = PermissionManager.getResource(label, sdk);
		return labels;
	}

	public ResourceInfo[] getDescriptions() {
		if(descriptions != null) return descriptions;
		descriptions = PermissionManager.getResource(description, sdk);
		return descriptions;
	}

	public ResourceInfo[] getIcons() {
		if(icons != null) return icons;
		icons = PermissionManager.getResource(icon, sdk);
		return icons;
	}
}
