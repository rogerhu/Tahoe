package com.lake.tahoe.activities;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.lake.tahoe.R;
import com.lake.tahoe.models.Request;
import com.lake.tahoe.models.User;
import com.lake.tahoe.navigation.AbstractNavDrawerActivity;
import com.lake.tahoe.navigation.NavDrawerItem;
import com.lake.tahoe.navigation.NavMenuItem;
import com.lake.tahoe.utils.ActivityUtil;
import com.lake.tahoe.utils.Currency;
import com.lake.tahoe.utils.MapUtil;
import com.lake.tahoe.utils.PushUtil;
import com.lake.tahoe.views.CurrencyTextWatcher;
import com.lake.tahoe.views.DynamicActionBar;
import com.lake.tahoe.widgets.SpeechBubble;
import com.lake.tahoe.widgets.SpeechBubbleIconGenerator;
import com.parse.ParsePush;


public class RequestCreateActivity extends AbstractNavDrawerActivity {

	GoogleMap map;
	Marker marker;
	Request request;
	TextView title;
	TextView amt;
	TextView description;

	DynamicActionBar actionBar;

	protected final int SWITCH_MODE = 100;
	protected final int LOGOUT = 200;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		LayoutInflater inflater = getLayoutInflater();
		LinearLayout container = (LinearLayout) findViewById(R.id.content_frame);
		inflater.inflate(R.layout.activity_request_create, container);

		amt = (TextView) findViewById(R.id.rewardText);
		title = (TextView) findViewById(R.id.wantText);
		description = (TextView) findViewById(R.id.anythingElseText);
		amt.addTextChangedListener(new CurrencyTextWatcher());

		actionBar = new DynamicActionBar(RequestCreateActivity.this);
		actionBar.setTitle(getString(R.string.create_a_request));

		actionBar.setAcceptAction(new View.OnClickListener() {
			@Override public void onClick(View v) {
				createRequest();
			}
		});

		getActionBar().setDisplayShowHomeEnabled(true);
	}


	private void convertToVendor() {
		User user = User.getCurrentUser();
		user.setType(User.Type.VENDOR);
		toggleBlocker(true);
		user.saveAndPublish(new PushUtil.HandlesPublish() {
			@Override public void onPublished(ParsePush push) {
				ActivityUtil.startRequestMapActivity(RequestCreateActivity.this);
				ActivityUtil.transitionFade(RequestCreateActivity.this);
			}
			@Override public void onError(Throwable t) {
				RequestCreateActivity.this.onError(t);
			}
		});
	}

	public void createRequest() {

		CharSequence titleText = title.getText();
		CharSequence amtText = amt.getText();
		CharSequence descriptionText = description.getText();

		if (titleText == null || amtText == null ||
				titleText.toString().equals("") || amtText.toString().equals("")) {
			showMessage(getString(R.string.missing_required_fields));
			return;
		}

		toggleBlocker(true);

		request = new Request(Request.State.OPEN);
		request.setTitle(titleText.toString());

		int amount = Currency.getAmountInCents(amtText.toString());
		if (amount > 0) request.setCents(amount);

		if (descriptionText != null)
			request.setDescription(descriptionText.toString());

		request.setClient(User.getCurrentUser());
		request.saveAndPublish(new PushUtil.HandlesPublish() {
			@Override public void onPublished(ParsePush push) {
				ActivityUtil.startRequestOpenActivity(RequestCreateActivity.this);
				ActivityUtil.transitionRight(RequestCreateActivity.this);
			}
			@Override public void onError(Throwable t) {
				RequestCreateActivity.this.onError(t);
			}
		});

	}

	protected void onGooglePlayServicesReady() {
		SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		map = fragment.getMap();
		map.getUiSettings().setZoomControlsEnabled(false);
		map.getUiSettings().setMyLocationButtonEnabled(false);
	}

	@Override
	public void onLocationChanged(Location location) {
		super.onLocationChanged(location);

		SpeechBubbleIconGenerator iconGenerator = new SpeechBubbleIconGenerator(this);

		LatLng position = MapUtil.locationToLatLng(location);
		if (marker == null) {
			marker = map.addMarker(MapUtil.getSpeechBubbleMarkerOptions(
				position,
				getResources().getString(R.string.you),
				iconGenerator, SpeechBubble.ColorType.PURPLE
			));
		} else {
			marker.setPosition(position);
		}
		MapUtil.panAndZoomToLocation(map, location, MapUtil.DEFAULT_ZOOM_LEVEL);
	}

	@Override
	protected void onGooglePlayServicesError(Throwable t) {
		onError(t);
	}

	@Override
	protected void onLocationTrackingFailed(Throwable t) {
		onError(t);

	}

	@Override
	protected void setNavMenuItems() {
		navMenu = new NavDrawerItem[] {
				NavMenuItem.create(SWITCH_MODE, getResources().getString(R.string.switch_to_vendor), "ic_action_vendor_mode", false, this),
				NavMenuItem.create(LOGOUT, getResources().getString(R.string.logout), "ic_logout", false, this)
		};
	}

	@Override
	protected void onNavItemSelected(int id) {
		switch(id) {
			case LOGOUT:
				User.logout();
				ActivityUtil.startLoginActivity(this);
				ActivityUtil.transitionFade(this);
				break;
			case SWITCH_MODE:
				convertToVendor();
				break;
		}
	}
}
