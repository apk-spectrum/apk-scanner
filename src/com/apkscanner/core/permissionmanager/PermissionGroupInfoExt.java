package com.apkscanner.core.permissionmanager;

import java.util.List;

import com.apkscanner.data.apkinfo.PermissionGroupInfo;
import com.apkscanner.data.apkinfo.ResourceInfo;
import com.apkscanner.util.Log;

public class PermissionGroupInfoExt extends PermissionGroupInfo {
	public int sdk;
	public String comment;
	public String label;
	public String icon;
	public String description;
	public List<PermissionInfoExt> permissions;

	public PermissionGroupInfoExt() { }

	public PermissionGroupInfoExt(PermissionGroupInfo info) {
		super(info);
		if(info instanceof PermissionGroupInfoExt) {
			PermissionGroupInfoExt infoExt = (PermissionGroupInfoExt) info;
			comment = infoExt.comment;
			sdk = infoExt.sdk;
			label = infoExt.label;
			icon = infoExt.icon;
			description = infoExt.description;
		}
	}

	public PermissionGroupInfoExt(PermissionGroupInfoExt info) {
		this((PermissionGroupInfo)info);
	}

	@Override
	public boolean equals(Object target) {
		if(!(target instanceof PermissionGroupInfoExt)) return false;
		PermissionGroupInfoExt other = (PermissionGroupInfoExt) target;
		return super.equals(target)
				&& stringEquals(label, other.label)
				&& stringEquals(icon, other.icon)
				&& stringEquals(description, other.description)
				&& stringEquals(comment, other.comment);
	}

	public boolean isDeprecated() {
		return comment != null && comment.contains("@deprecated");
	}

	public boolean isRemoved() {
		return comment != null && comment.contains("@Removed");
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
		Log.e(description);
		descriptions = PermissionManager.getResource(description, sdk);
		Log.e(descriptions + "");
		return descriptions;
	}

	public ResourceInfo[] getIcons() {
		if(icons != null) return icons;
		icons = PermissionManager.getResource(icon, sdk);
		return icons;
	}
}
