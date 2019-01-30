package com.apkscanner.data.apkinfo;

import java.util.Arrays;

public class PermissionInfo
{
    public static final int PROTECTION_NORMAL = 0;
    public static final int PROTECTION_DANGEROUS = 1;
    public static final int PROTECTION_SIGNATURE = 2;
    @Deprecated
    public static final int PROTECTION_SIGNATURE_OR_SYSTEM = 3;
    public static final int PROTECTION_FLAG_PRIVILEGED = 0x10;
    @Deprecated
    public static final int PROTECTION_FLAG_SYSTEM = 0x10;
    public static final int PROTECTION_FLAG_DEVELOPMENT = 0x20;
    public static final int PROTECTION_FLAG_APPOP = 0x40;
    public static final int PROTECTION_FLAG_PRE23 = 0x80;
    public static final int PROTECTION_FLAG_INSTALLER = 0x100;
    public static final int PROTECTION_FLAG_VERIFIER = 0x200;
    public static final int PROTECTION_FLAG_PREINSTALLED = 0x400;

    public static final int PROTECTION_MASK_BASE = 0xf;
    public static final int PROTECTION_MASK_FLAGS = 0xff0;

    public static final String[] DeprecatedPermissionList = new String[] {
    	"android.permission.BIND_CARRIER_MESSAGING_SERVICE:API level 23.\n - Use BIND_CARRIER_SERVICES instead",
    	"android.permission.GET_TASKS:API level 21.\n - No longer enforced.",
    	"android.permission.PERSISTENT_ACTIVITY:API level 9.\n - This functionality will be removed in the future; please do not use. Allow an application to make its activities persistent.",
    	"android.permission.READ_INPUT_STATE:API level 16.\n - The API that used this permission has been removed.",
    	"android.permission.RESTART_PACKAGES:API level 8.\n - The restartPackage(String) API is no longer supported.",
    	"android.permission.SET_PREFERRED_APPLICATIONS:API level 7.\n - No longer useful, see addPackageToPreferred(String) for details."
    };

	public ResourceInfo[] descriptions = null; // "string resource"
	public ResourceInfo[] icons = null; // "drawable resource"
	public ResourceInfo[] labels = null; // "string resource"
	public String name = null; // "string"
	public String permissionGroup = null; // "string"
	public String protectionLevel = null; /* ["normal" | "dangerous" |
                             "signature" | "signatureOrSystem"] */
	public String permissionFlags = null;

	public PermissionInfo() { }

	public PermissionInfo(PermissionInfo info) {
		descriptions = info.descriptions;
		icons = info.icons;
		labels = info.labels;
		name = info.name;
		permissionGroup = info.permissionGroup;
		protectionLevel = info.protectionLevel;
		permissionFlags = info.permissionFlags;
	}

	@Override
	public boolean equals(Object target) {
		if(!(target instanceof PermissionInfo)) return false;
		PermissionInfo other = (PermissionInfo) target;
		return stringEquals(name, other.name)
				&& stringEquals(permissionGroup, other.permissionGroup)
				&& stringEquals(protectionLevel, other.protectionLevel)
				&& stringEquals(permissionFlags, other.permissionFlags)
				&& Arrays.deepEquals(labels, other.labels)
				&& Arrays.deepEquals(descriptions, other.descriptions)
				&& Arrays.deepEquals(icons, other.icons);
	}

	protected boolean stringEquals(String a, String b) {
		return ((a == null && b == null) || (a != null && a.equals(b)));
	}

	public boolean isDangerousLevel()
	{
		return (protectionLevel != null && protectionLevel.contains("dangerous"));
	}

	public boolean isSignatureLevel()
	{
		return (protectionLevel != null && protectionLevel.contains("signature")
				&& !protectionLevel.contains("ystem") && !protectionLevel.contains("privileged"));
	}

	public boolean isSignatureOrSystemLevel()
	{
		return (protectionLevel != null && protectionLevel.contains("signature")
				&& (protectionLevel.contains("ystem") || protectionLevel.contains("privileged")));
	}

	public boolean isSystemLevel()
	{
		return (protectionLevel != null && !protectionLevel.contains("signature")
				&& (protectionLevel.contains("ystem") || protectionLevel.contains("privileged")));
	}

	public boolean isDeprecated()
	{
		for(String s: DeprecatedPermissionList) {
			if(s.startsWith(name+":")) return true;
		}
		return false;
	}

	public boolean isRemoved()
	{
		return permissionFlags != null && permissionFlags.contains("removed");
	}

	public boolean isCostsMoney()
	{
		return permissionFlags != null && permissionFlags.contains("costsMoney");
	}

	public String getDeprecatedMessage()
	{
		for(String s: DeprecatedPermissionList) {
			if(s.startsWith(name+":")) return "[" + s.replaceAll(":", "]\n - This constant was deprecated in ");
		}
		return null;
	}

    public static int fixProtectionLevel(int level)
    {
        if (level == PROTECTION_SIGNATURE_OR_SYSTEM) {
            level = PROTECTION_SIGNATURE | PROTECTION_FLAG_PRIVILEGED;
        }
        return level;
    }

    public static String protectionToString(int level)
    {
        String protLevel = "????";
        switch (level&PROTECTION_MASK_BASE) {
            case PermissionInfo.PROTECTION_DANGEROUS:
                protLevel = "dangerous";
                break;
            case PermissionInfo.PROTECTION_NORMAL:
                protLevel = "normal";
                break;
            case PermissionInfo.PROTECTION_SIGNATURE:
                protLevel = "signature";
                break;
            case PermissionInfo.PROTECTION_SIGNATURE_OR_SYSTEM:
                protLevel = "signatureOrSystem";
                break;
        }
        if ((level&PermissionInfo.PROTECTION_FLAG_PRIVILEGED) != 0) {
            protLevel += "|privileged";
        }
        if ((level&PermissionInfo.PROTECTION_FLAG_DEVELOPMENT) != 0) {
            protLevel += "|development";
        }
        if ((level&PermissionInfo.PROTECTION_FLAG_APPOP) != 0) {
            protLevel += "|appop";
        }
        if ((level&PermissionInfo.PROTECTION_FLAG_PRE23) != 0) {
            protLevel += "|pre23";
        }
        if ((level&PermissionInfo.PROTECTION_FLAG_INSTALLER) != 0) {
            protLevel += "|installer";
        }
        if ((level&PermissionInfo.PROTECTION_FLAG_VERIFIER) != 0) {
            protLevel += "|verifier";
        }
        if ((level&PermissionInfo.PROTECTION_FLAG_PREINSTALLED) != 0) {
            protLevel += "|preinstalled";
        }
        return protLevel;
    }
}
