package com.lake.tahoe.navigation;

import android.app.ActionBar;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lake.tahoe.R;
import com.lake.tahoe.activities.GoogleLocationServiceActivity;

/* See http://www.michenux.net/android-navigation-drawer-748.html */

public abstract class AbstractNavDrawerActivity extends GoogleLocationServiceActivity implements ListView.OnItemClickListener {

	protected DrawerLayout mDrawerLayout;
	protected ActionBarDrawerToggle mDrawerToggle;

	private ListView mDrawerList;
	protected NavDrawerItem[] navMenu;

	protected abstract void setNavMenuItems();
	protected abstract void onNavItemSelected( int id );

	protected void hideDrawer() {
		ActionBar navBar = getActionBar();
		if (navBar != null) {
			navBar.setDisplayHomeAsUpEnabled(false);
			navBar.setHomeButtonEnabled(false);
			navBar.setDisplayShowHomeEnabled(false);
		}
	}

	protected void showDrawer() {
		ActionBar navBar = getActionBar();
		if (navBar != null) {
			navBar.setDisplayHomeAsUpEnabled(true);
			navBar.setHomeButtonEnabled(true);
			navBar.setDisplayShowHomeEnabled(true);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.navigation_drawer);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		this.setNavMenuItems();
		mDrawerList.setAdapter(new NavDrawerAdapter(this, R.layout.navdrawer_item, navMenu));

		mDrawerList.setOnItemClickListener(this);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onItemClick(AdapterView parent, View view, int position, long id) {
		// Highlight the selected item, update the title, and close the drawer

		AbstractNavDrawerActivity.this.onNavItemSelected(navMenu[position].getId());
		mDrawerList.setItemChecked(position, true);
		mDrawerLayout.closeDrawer(mDrawerList);
	}
}
