package com.apkspectrum.core.permissionmanager;

import com.apkspectrum.data.apkinfo.ApkInfoHelper;
import com.apkspectrum.data.apkinfo.PermissionInfo;
import com.apkspectrum.data.apkinfo.ResourceInfo;
import com.apkspectrum.resource._RStr;

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
    	return ApkInfoHelper.getResourceValue(getLabels(), _RStr.getLanguage());
    }

	@Override
    public String getDescription() {
    	return ApkInfoHelper.getResourceValue(getDescriptions(), _RStr.getLanguage());
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
	public String getProtectionLevel() {
		return protectionLevel;
	}

	@Override
	public String getPermissionGroup() {
		return permissionGroup;
	}

	@Override
	public String getPermissionFlags() {
		return permissionFlags;
	}

	@Override
	public String getIcon() {
		return icon;
	}

	@Override
	public String getRequest() {
		return null;
	}

	@Override
	public int getPriority() {
		return -1;
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

	public static int parseProtectionFlags(String strLevel) {
		int level = parseProtectionLevel(strLevel);
		int flags = level & PROTECTION_MASK_FLAGS;
		level = 1 << (level & PROTECTION_MASK_BASE); 
		return level | flags;
	}

	public static String protectionFlagsToString(int flags) {
		if(flags == 0) flags = 1;
		StringBuilder sb = new StringBuilder();
        if ((flags & (1 << PROTECTION_NORMAL)) != 0) {
            sb.append("|").append(protectionToString(PROTECTION_NORMAL));
        }
        if ((flags & (1 << PROTECTION_DANGEROUS)) != 0) {
            sb.append("|").append(protectionToString(PROTECTION_DANGEROUS));
        }
        if ((flags & (1 << PROTECTION_SIGNATURE)) != 0) {
            sb.append("|").append(protectionToString(PROTECTION_SIGNATURE));
        }
        if ((flags & (1 << 3)) != 0) { /* PROTECTION_SIGNATURE_OR_SYSTEM */ 
            sb.append("|").append(protectionToString(3));
        }
        sb.append(protectionToString(PROTECTION_MASK_BASE | flags));
		return sb.substring(1);
	}
}
