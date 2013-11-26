package com.lake.tahoe.navigation;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.lake.tahoe.R;
import com.lake.tahoe.activities.GoogleLocationServiceActivity;

import java.util.ArrayList;

/* See http://www.michenux.net/android-navigation-drawer-748.html */

public abstract class AbstractNavDrawerActivity extends GoogleLocationServiceActivity {

	protected DrawerLayout mDrawerLayout;
	protected ActionBarDrawerToggle mDrawerToggle;

	private ListView mDrawerList;

	protected abstract void onNavItemSelected( int id );

	NavDrawerItem[] navMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.navigation_drawer);

		navMenu = new NavDrawerItem[] {
				NavMenuItem.create(100, getResources().getString(R.string.switch_mode), "ic_action_vendor_mode", false, this),
				NavMenuItem.create(200, getResources().getString(R.string.logout), "ic_launcher", false, this)
		};

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerList.setAdapter(new NavDrawerAdapter(this, R.layout.navdrawer_item, navMenu));

		class DrawerItemClickListener implements ListView.OnItemClickListener {
			@Override
			public void onItemClick(AdapterView parent, View view, int position, long id) {
				selectItem(position);
			}

			private void selectItem(int position) {
				// Highlight the selected item, update the title, and close the drawer

				AbstractNavDrawerActivity.this.onNavItemSelected(navMenu[position].getId());
				mDrawerList.setItemChecked(position, true);
				mDrawerLayout.closeDrawer(mDrawerList);
			}
		}
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

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

		getActionBar().setDisplayShowHomeEnabled(true);
		/*actionBar.setLeftAction(R.drawable.ic_action_vendor_mode, new View.OnClickListener() {
			@Override public void onClick(View v) {
				convertToVendor();
			}
		});*/
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
}
