package com.apkspectrum.core.permissionmanager;

import com.apkspectrum.data.apkinfo.PermissionInfo;
import com.apkspectrum.data.apkinfo.UsesPermissionInfo;
import com.apkspectrum.util.Log;

public class RevokedPermissionInfo extends PermissionInfo {
	public enum RevokedReason {
		NO_REVOKED,
		BEFORE_ADDED,
		AFTER_REMOVED,
		OVER_MAX_SDK,
		UNDER_SDK23,
		NO_USES,
		NEED_PLATFORM_SIGNATURE,
		UNKNOWN_SOURCE,
		UNKNOWN_REASON
	}
	public enum RevokedSource {
		RECORD, DECLARED, UNKNOWN
	}
	public RevokedSource source;
	public RevokedReason reason;
	public boolean hasRecord;
	public int sdk;

	public RevokedPermissionInfo() { }

	public RevokedPermissionInfo(PermissionInfo info) {
		super(info);
	}

	public String getReasonText() {
		switch(reason) {
		case AFTER_REMOVED:
			return reason + ", removed in API level " + sdk;
		case BEFORE_ADDED:
			return reason + ", added in API level " + sdk;
		case NO_USES:
			return reason + ", no usesed";
		case OVER_MAX_SDK:
			return reason + ", over max sdk " + sdk;
		case UNDER_SDK23:
			return reason + ", under sdk 23";
		case NEED_PLATFORM_SIGNATURE:
			return reason + ", need platform signature";
		case UNKNOWN_SOURCE:
			return reason + ", unknown source";
		case UNKNOWN_REASON:
			return reason + ", unknown";
		case NO_REVOKED:
		default:
	
		}
		return "";
	}

	public static RevokedPermissionInfo makeRevokedReason(PermissionRecord record, int sdk, int targetSdk, boolean treatSignAsRevoked) {
		if(record == null) return null;
		RevokedPermissionInfo reason = new RevokedPermissionInfo();
		reason.name = record.name;
		reason.source = RevokedSource.RECORD;
		reason.reason = RevokedReason.NO_REVOKED;
		reason.hasRecord = true;
		reason.sdk = -1;
		if(record.histories == null || record.histories.length == 0) {
			reason.reason = RevokedReason.UNKNOWN_REASON;
		} else if(sdk < record.addedSdk) {
			Log.v("This SDK(" + sdk + ") version was not have permission. " + reason.name + " added in API level " + record.addedSdk);
			reason.sdk = record.addedSdk;
			reason.reason = RevokedReason.BEFORE_ADDED;
		} else if(record.removedSdk > -1 && sdk >= record.removedSdk) {
			Log.v("This SDK(" + sdk + ") version was not have permission. " + reason.name + " removed at API level " + record.removedSdk);
			reason.sdk = record.removedSdk;
			reason.reason = RevokedReason.AFTER_REMOVED;
		} else if(record.sdk23 != null && record.sdk23 && sdk < 23) {
			Log.v("This SDK(" + sdk + ") version was not have permission. " + reason.name + " is valid at sdk23 API level or later");
			reason.reason = RevokedReason.UNDER_SDK23;
		} else if(record.maxSdkVersion != null && sdk > record.maxSdkVersion) {
			Log.v("This SDK(" + sdk + ") version was not have permission. " + reason.name + " over maxsdk " + record.maxSdkVersion);
			reason.sdk = record.maxSdkVersion;
			reason.reason = RevokedReason.OVER_MAX_SDK;
		} else {
			PermissionInfoExt info = record.getInfomation(sdk);
			if(treatSignAsRevoked && info.isSignatureLevel()
					&& (!info.protectionLevel.contains("pre23") || targetSdk >= 23)) {
				reason = new RevokedPermissionInfo(info);
				reason.hasRecord = true;
				reason.source = RevokedSource.RECORD;
				reason.reason = RevokedReason.NEED_PLATFORM_SIGNATURE;
			}
		}
		return reason;
	}

	public static RevokedPermissionInfo makeRevokedReason(DeclaredPermissionInfo declared, int sdk) {
		if(declared == null) return null;
		RevokedPermissionInfo reason = new RevokedPermissionInfo(declared);
		reason.name = declared.name;
		reason.source = RevokedSource.DECLARED;
		reason.reason = RevokedReason.NO_REVOKED;
		reason.sdk = -1;
		if(!declared.isUsed) {
			Log.v("This SDK(" + sdk + ") version was not have permission. " + reason.name + " is not uses");
			reason.reason = RevokedReason.NO_USES;
		} else if(declared.sdk23 && sdk < 23) {
			Log.v("This SDK(" + sdk + ") version was not have permission. " + reason.name + " is valid at sdk23 API level or later");
			reason.reason = RevokedReason.UNDER_SDK23;
		} else if(declared.maxSdkVersion != null && sdk > declared.maxSdkVersion) {
			Log.v("This SDK(" + sdk + ") version was not have permission. " + reason.name + " over maxsdk " + declared.maxSdkVersion);
			reason.sdk = declared.maxSdkVersion;
			reason.reason = RevokedReason.OVER_MAX_SDK;
		}
		return reason;
	}

	public static RevokedPermissionInfo makeRevokedReason(UsesPermissionInfo unknownSource) {
		if(unknownSource == null) return null;
		RevokedPermissionInfo reason = new RevokedPermissionInfo();
		reason.name = unknownSource.name;
		reason.source = RevokedSource.UNKNOWN;
		reason.reason = RevokedReason.UNKNOWN_SOURCE;
		return reason;
	}
}
