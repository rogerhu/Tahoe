package com.lake.tahoe.navigation;

import android.content.Context;

import com.lake.tahoe.R;

public class LogoutMenuItem extends NavMenuItem {

	public static final int LOGOUT = 200;

	public void LogoutMenuItem() {
	}

	public static NavMenuItem create(Context context) {
		return NavMenuItem.create(LOGOUT, context.getResources().getString(R.string.logout), "ic_logout", false, context);
	}
}
