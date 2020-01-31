package com.apkspectrum.core.permissionmanager;

import java.util.List;

import com.apkspectrum.data.apkinfo.ApkInfoHelper;
import com.apkspectrum.data.apkinfo.PermissionGroupInfo;
import com.apkspectrum.data.apkinfo.PermissionInfo;
import com.apkspectrum.data.apkinfo.ResourceInfo;
import com.apkspectrum.resource._RProp;

public class PermissionGroupInfoExt extends PermissionGroupInfo implements UnitInformation {
	public int sdk;
	public String comment;
	public String label;
	public String icon;
	public String description;
    public String request;
    public Integer priority;
	public List<PermissionInfo> permissions;
	public int protectionFlags;

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
			request = infoExt.request;
			priority = infoExt.priority;
			permissions = infoExt.permissions;
			protectionFlags = infoExt.protectionFlags;
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
				&& objEquals(label, other.label)
				&& objEquals(icon, other.icon)
				&& objEquals(description, other.description)
				&& objEquals(request, other.request)
				&& objEquals(priority, other.priority)
				&& objEquals(comment, other.comment);
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

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getLabel() {
    	return ApkInfoHelper.getResourceValue(getLabels(), _RProp.S.PREFERRED_LANGUAGE.get());
    }

	@Override
    public String getDescription() {
    	return ApkInfoHelper.getResourceValue(getDescriptions(), _RProp.S.PREFERRED_LANGUAGE.get());
    }

	@Override
    public String getRequest() {
    	return ApkInfoHelper.getResourceValue(getRequests(), _RProp.S.PREFERRED_LANGUAGE.get());
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

	@Override
	public int getPriority() {
		return priority != null ? priority : -1;
	}

	@Override
	public String getPermissionGroup() {
		return name;
	}

	@Override
	public String getProtectionLevel() {
		return null;
	}

	@Override
	public String getPermissionFlags() {
		return null;
	}

	public ResourceInfo[] getLabels() {
		if(labels != null) return labels;
		return labels = PermissionManager.getResource(label, sdk);
	}

	public ResourceInfo[] getDescriptions() {
		if(descriptions != null) return descriptions;
		return descriptions = PermissionManager.getResource(description, sdk);
	}

	public ResourceInfo[] getRequests() {
		if(requests != null) return requests;
		return requests = PermissionManager.getResource(request, sdk);
	}

	public String getIconPath() {
		return getIcons()[0].name;
	}

	public ResourceInfo[] getIcons() {
		if(icons != null) return icons;
		return icons = PermissionManager.getResource(icon, sdk);
	}

	public String getSummary() {
		StringBuilder summary = new StringBuilder();
		summary.append("[");
		summary.append(label != null ? getLabel() : name);
		summary.append("]");
		if(description != null) {
			summary.append(" : ");
			summary.append(getDescription());
		}
		if(permissions != null) {
			for(PermissionInfo info : permissions) {
				String permLabel = info.getLabel();
				if(permLabel == null) permLabel = info.name;
				summary.append("\n - ");
				summary.append(permLabel);
			}
		}
		return summary.toString();
	}

	public boolean hasDangerous() {
		return (protectionFlags & (1 << PermissionInfo.PROTECTION_DANGEROUS)) != 0;
	}
}
