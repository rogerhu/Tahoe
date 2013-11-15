package com.lake.tahoe.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.lake.tahoe.R;
import com.lake.tahoe.dialogs.BlockerDialog;
import com.lake.tahoe.utils.ActivityUtil;
import com.lake.tahoe.utils.HandlesErrors;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import net.simonvt.messagebar.MessageBar;

public class TahoeActivity extends FragmentActivity implements HandlesErrors {

	private MessageBar messageBar;
	BlockerDialog blockerDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		blockerDialog = new BlockerDialog(this, R.style.DialogBlocker);
	}

	@Override
	protected void onResume() {
		super.onResume();
		toggleBlocker(false);
	}

	@Override
	protected void onPause() {
		super.onPause();
		toggleBlocker(false);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		messageBar = new MessageBar(this);
		super.onPostCreate(savedInstanceState);
	}

	public void showMessage(String message) {
		messageBar.show(message);
	}

	public void toggleBlocker(boolean show) {
		if (show) blockerDialog.show();
		else blockerDialog.hide();
	}

	@Override
	public void onError(Throwable t) {
		String PREFIX = "OddJobError";
		String errorFormat = getString(R.string.error_format);
		Log.e(PREFIX, getLocalClassName(), t);
		showMessage(String.format(errorFormat, t.getLocalizedMessage()));
		if (this instanceof DelegateActivity)
			refreshState();
	}

	public void refreshState() {
		toggleBlocker(true);
		ActivityUtil.startDelegateActivity(this);
		ActivityUtil.transitionFade(this);
	}

	public boolean setUpOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void logout(MenuItem item) {
		ParseFacebookUtils.getSession().closeAndClearTokenInformation();
		ParseUser.logOut();
		ActivityUtil.startLoginActivity(this);
		ActivityUtil.transitionFade(this);
	}
}
