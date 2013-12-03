package com.lake.tahoe.activities;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.lake.tahoe.R;
import com.lake.tahoe.callbacks.ModelCallback;
import com.lake.tahoe.handlers.RequestUpdateChannel;
import com.lake.tahoe.handlers.UserUpdateChannel;
import com.lake.tahoe.models.Request;
import com.lake.tahoe.models.User;
import com.lake.tahoe.navigation.AbstractNavDrawerActivity;
import com.lake.tahoe.navigation.NavDrawerItem;
import com.lake.tahoe.navigation.NavMenuItem;
import com.lake.tahoe.utils.ActivityUtil;
import com.lake.tahoe.utils.MapUtil;
import com.lake.tahoe.utils.PushUtil;
import com.lake.tahoe.views.DynamicActionBar;
import com.lake.tahoe.widgets.SpeechBubble;
import com.lake.tahoe.widgets.SpeechBubbleIconGenerator;
import com.parse.ParsePush;
import com.parse.ParseUser;

import java.util.Hashtable;

public class RequestMapActivity extends AbstractNavDrawerActivity implements
		RequestUpdateChannel.HandlesRequestUpdates,
		UserUpdateChannel.HandlesUserUpdates, PushUtil.HandlesPublish, ModelCallback<Request> {

	GoogleMap map;
	Marker marker;
	DynamicActionBar actionBar;
	Hashtable<Marker, Request> markerRequestMap = new Hashtable<Marker, Request>();
	Hashtable<String, Marker> userMarkerMap = new Hashtable<String, Marker>();

	SpeechBubbleIconGenerator iconGenerator = new SpeechBubbleIconGenerator(this);
	boolean mapReadyToPan = false;

	protected final int SWITCH_MODE = 100;
	protected final int LOGOUT = 200;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutInflater inflater = getLayoutInflater();
		LinearLayout container = (LinearLayout) findViewById(R.id.content_frame);
		inflater.inflate(R.layout.activity_request_map, container);

		actionBar = new DynamicActionBar(this);

		setUpActionBar();
	}

	public void setUpActionBar() {
		actionBar.setTitle(getResources().getString(R.string.select_client));
		actionBar.setBackgroundColor(getResources().getColor(R.color.black));
		actionBar.toggleRightAction(View.INVISIBLE);
		this.showDrawer();
	}

	private void convertToClient() {
		User user = User.getCurrentUser();
		user.setType(User.Type.CLIENT);
		toggleBlocker(true);
		user.saveAndPublish(this);
	}

	@Override
	public void onPublished(ParsePush push) {
		ActivityUtil.startRequestCreateActivity(this);
		ActivityUtil.transitionFade(this);
	}

	@Override
	public void onRequestUpdated(Request request) {
		if (request == null)
			return;
		if (!request.getState().equals(Request.State.OPEN))
			return;

		generateMarkerForRequest(request);
	}

	@Override
	public void onRequestUpdateError(Throwable t) {
		onError(t);
	}

	@Override
	public void onModelFound(Request request) {
		generateMarkerForRequest(request);
	}

	void generateMarkerForRequest(Request request) {

		User client = request.getClient();
		if (client == null) {
			this.onError(new IllegalStateException("Client was null"));
			return;
		}

		String clientId = client.getObjectId();

		Marker marker = userMarkerMap.get(clientId);

		if (marker != null) {
			marker.setPosition(request.getGoogleMapsLocation());
		} else {
			marker = map.addMarker(MapUtil.getSpeechBubbleMarkerOptions(
					request,
					iconGenerator,
					SpeechBubble.ColorType.BLACK));
		}
		userMarkerMap.put(clientId, marker);
		markerRequestMap.put(marker, request);
	}

	@Override
	public void onModelError(Throwable e) {
		// ignore requests if we can't load the details
	}

	@Override
	protected void onStart() {
		super.onStart();
		RequestUpdateChannel.subscribe(this);
		UserUpdateChannel.subscribe(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		RequestUpdateChannel.unsubscribe(this);
		UserUpdateChannel.unsubscribe(this);
	}

	@Override
	public void onUserUpdated(User user) {
		if (user == null)
			return;
		if (user.getObjectId().equals(User.getCurrentUser().getObjectId()))
			return;
		if (!user.getType().equals(User.Type.CLIENT))
			return;
		user.getUnfinishedRequest(this);
	}

	@Override
	public void onUserUpdateError(Throwable t) {
		onError(t);
	}

	@Override
	protected void onGooglePlayServicesReady() {
		SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		map = fragment.getMap();
		map.setOnMarkerClickListener(new OnMarkerClick());
		map.setOnMapClickListener(new OnMapClick());

		User user = (User) ParseUser.getCurrentUser();
		Request.findNearbyRequests(Request.State.OPEN, user, this);

		mapReadyToPan = true;

	}

	/**
	 * TODO We should probably encapsulate this behavior in other classes that use a map.
	 * See the comment on OnMarkerClick (below) about GoogleMapActivity
	 */
	@Override
	public void onLocationChanged(Location location) {
		super.onLocationChanged(location);
		LatLng position = MapUtil.locationToLatLng(location);
		if (mapReadyToPan) {
			mapReadyToPan = false;
			MapUtil.panAndZoomToLocation(map, location, MapUtil.DEFAULT_ZOOM_LEVEL);
			marker = map.addMarker(MapUtil.getSpeechBubbleMarkerOptions(position,
					getResources().getString(R.string.you), iconGenerator, SpeechBubble.ColorType.PURPLE));
		} else if (marker != null && !marker.getPosition().equals(position)) {
			marker.setPosition(position);
		}
	}

	private class OnMarkerClick implements GoogleMap.OnMarkerClickListener {
		@Override
		public boolean onMarkerClick(Marker marker) {

			final Request request = markerRequestMap.get(marker);
			if (request == null)
				return true;

			BitmapDescriptor bitmapDescriptor = SpeechBubble.generateMarkerBitmap(
					iconGenerator,
					request.getDisplayDollars(),
					SpeechBubble.ColorType.BLUE
			);
			marker.setIcon(bitmapDescriptor);

			actionBar.setTitle(request.getDisplayDollars() + " | " + request.getTitle());
			actionBar.setBackgroundColor(getResources().getColor(R.color.dark_blue));
			actionBar.setRightArrowAction(new View.OnClickListener() {
				@Override public void onClick(View v) {
					ActivityUtil.startRequestDetailActivity(
							RequestMapActivity.this,
							request
					);
				}
			});
			actionBar.toggleRightAction(View.VISIBLE);
			RequestMapActivity.this.hideDrawer();
			return true;
		}
	}

	private class OnMapClick implements GoogleMap.OnMapClickListener {
		@Override
		public void onMapClick(LatLng point) {
			setUpActionBar();
		}
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
				NavMenuItem.create(SWITCH_MODE, getResources().getString(R.string.switch_to_client), "ic_action_client_mode", false, this),
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
				convertToClient();
				break;
		}
	}

}
