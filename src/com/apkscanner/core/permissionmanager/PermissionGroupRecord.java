package com.apkscanner.core.permissionmanager;

import java.util.Arrays;

import com.apkscanner.data.apkinfo.PermissionInfo;
import com.apkscanner.util.XmlPath;

public class PermissionGroupRecord extends UnitRecord<PermissionGroupInfoExt> {
	private PermissionManager manager;

	public PermissionGroupRecord(PermissionManager manager, XmlPath node) {
		super(PermissionGroupInfoExt.class, node);
		this.manager = manager;
	}

	@Override
	public PermissionGroupInfoExt getInfomation(int sdk) {
		PermissionGroupInfoExt info = super.getInfomation(sdk);
		if(info != null && info.permissions == null) {
			info.permissions = Arrays.asList((PermissionInfo[]) manager.getGroupPermissions(name, sdk));
			for(PermissionInfo perm: info.permissions) {
				if(perm instanceof PermissionInfoExt)
					info.protectionFlags |= ((PermissionInfoExt)perm).protectionFlags;
			}
		}
		return info;
	}

	public String getPresentIcon() {
		if(histories == null) return null;
		String icon = null;
		for(int i=0; i<histories.length; i++) {
			icon = ((PermissionGroupInfoExt) histories[i]).icon;
			if(icon != null) break;
		}
		if(icon == null) {
			switch(name) {
			case "android.permission-group.DEVELOPMENT_TOOLS":
				icon = "@drawable/perm_group_development_tools";
				break;
			case "android.permission-group.HARDWARE_CONTROLS":
				icon = "@drawable/perm_group_hardware_controls";
				break;
			case "android.permission-group.COST_MONEY":
				icon = "@drawable/perm_group_cost_money";
				break;
			default:
				icon = "@drawable/perm_group_unknown";
				break;
			}
		}
		return icon;
	}
}
