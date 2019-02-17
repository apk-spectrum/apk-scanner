package com.apkscanner.core.permissionmanager;

import com.apkscanner.data.apkinfo.UsesPermissionInfo;
import com.apkscanner.util.Log;

public class RevokedPermissionInfo {
	enum RevokedReason {
		NO_REVOKED,
		BEFORE_ADDED,
		AFTER_REMOVED,
		OVER_MAX_SDK,
		UNDER_SDK23,
		NO_USES,
		UNKNOWN_SOURCE,
		UNKNOWN_REASON
	}
	public String name;
	public RevokedReason reason;
	public boolean hasRecord;

	public static RevokedPermissionInfo makeRevokedReason(PermissionRecord record, int sdk) {
		if(record == null) return null;
		RevokedPermissionInfo reason = new RevokedPermissionInfo();
		reason.name = record.name;
		reason.reason = RevokedReason.NO_REVOKED;
		reason.hasRecord = true;
		if(record.histories == null || record.histories.length == 0) {
			reason.reason = RevokedReason.UNKNOWN_REASON;
		} else if(sdk < record.addedSdk) {
			Log.v("This SDK(" + sdk + ") version was not have permission. " + reason.name + " added in API level " + record.addedSdk);
			reason.reason = RevokedReason.BEFORE_ADDED;
		} else if(record.removedSdk > -1 && sdk >= record.removedSdk) {
			Log.v("This SDK(" + sdk + ") version was not have permission. " + reason.name + " removed at API level " + record.removedSdk);
			reason.reason = RevokedReason.AFTER_REMOVED;
		} else if(record.sdk23 && sdk < 23) {
			Log.v("This SDK(" + sdk + ") version was not have permission. " + reason.name + " is valid at sdk23 API level or later");
			reason.reason = RevokedReason.UNDER_SDK23;
		} else if(record.maxSdkVersion != null && sdk > record.maxSdkVersion) {
			Log.v("This SDK(" + sdk + ") version was not have permission. " + reason.name + " over maxsdk " + record.maxSdkVersion);
			reason.reason = RevokedReason.OVER_MAX_SDK;
		}
		return reason;
	}

	public static RevokedPermissionInfo makeRevokedReason(DeclaredPermissionInfo declared, int sdk) {
		if(declared == null) return null;
		RevokedPermissionInfo reason = new RevokedPermissionInfo();
		reason.name = declared.name;
		reason.reason = RevokedReason.NO_REVOKED;
		if(declared.sdk23 && sdk < 23) {
			Log.v("This SDK(" + sdk + ") version was not have permission. " + reason.name + " is valid at sdk23 API level or later");
			reason.reason = RevokedReason.UNDER_SDK23;
		} else if(declared.maxSdkVersion != null && sdk > declared.maxSdkVersion) {
			Log.v("This SDK(" + sdk + ") version was not have permission. " + reason.name + " over maxsdk " + declared.maxSdkVersion);
			reason.reason = RevokedReason.OVER_MAX_SDK;
		} else if(!declared.isUsed) {
			Log.v("This SDK(" + sdk + ") version was not have permission. " + reason.name + " is not uses");
			reason.reason = RevokedReason.NO_USES;
		}
		return reason;
	}

	public static RevokedPermissionInfo makeRevokedReason(UsesPermissionInfo unknownSource, int sdk) {
		if(unknownSource == null) return null;
		RevokedPermissionInfo reason = new RevokedPermissionInfo();
		reason.name = unknownSource.name;
		reason.reason = RevokedReason.UNKNOWN_SOURCE;
		return reason;
	}
}
