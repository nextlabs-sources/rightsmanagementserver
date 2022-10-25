package com.nextlabs.rms.eval;

import com.nextlabs.rms.auth.RMSUserPrincipal;

public class RMSPermissionMgr {

	public static final int ACTION_SAVE_SETTINGS = 1001;
	public static final int ACTION_REMOVE_SPAPP_SETTINGS = 1002;

	public static boolean isActionAllowed(int action, RMSUserPrincipal user) {

		switch (action) {

		case RMSPermissionMgr.ACTION_SAVE_SETTINGS:
			if (user.getRole().equalsIgnoreCase("ADMIN_USER")) {
				return true;
			}

		case RMSPermissionMgr.ACTION_REMOVE_SPAPP_SETTINGS:
			if (user.getRole().equalsIgnoreCase("ADMIN_USER")) {
				return true;
			}

		}
		return false;
	}

}
