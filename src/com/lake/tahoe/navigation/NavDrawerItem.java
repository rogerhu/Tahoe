package com.lake.tahoe.navigation;

/* Inspired by http://www.michenux.net/android-navigation-drawer-748.html */
public interface NavDrawerItem {
	public int getId();
	public String getLabel();
	public int getType();
	public boolean isEnabled();
	public boolean updateActionBarTitle();
}