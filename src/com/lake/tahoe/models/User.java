package com.lake.tahoe.models;

import android.view.MenuItem;

import com.google.android.gms.maps.model.LatLng;
import com.lake.tahoe.callbacks.ModelCallback;
import com.lake.tahoe.callbacks.ModelFindCallback;
import com.lake.tahoe.callbacks.ModelGetCallback;
import com.lake.tahoe.utils.ActivityUtil;
import com.lake.tahoe.utils.PushUtil;
import com.parse.ParseClassName;
import com.parse.ParseFacebookUtils;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;


/**
 * Created on 10/21/13.
 */

@ParseClassName("_User")
public class User extends ParseUser {

	public static enum Type {

		/**
		 * Buys services or things from Vendors
		 */
		CLIENT,

		/**
		 * Sells services or things to Clients
		 */
		VENDOR

	}

	public User() {
		super();
	}

	public Type getType() {
		String type = getString("type");
		return type != null ? Type.valueOf(type) : Type.CLIENT;
	}

	public void setType(Type type) {
		put("type", type.toString());
	}

	public ParseGeoPoint getLocation() {
		return getParseGeoPoint("location");
	}

	public LatLng getGoogleMapsLocation() {
		ParseGeoPoint pGP = getLocation();
		LatLng latlng = null;
		if (pGP != null) {
			latlng = new LatLng(pGP.getLatitude(), pGP.getLongitude());
		}
		return latlng;
	}

	public void setLocation(Double latitude, Double longitude) {
		put("location", new ParseGeoPoint(latitude, longitude));
	}

	public String getFacebookId() {
		return getString("facebookId");
	}

	public void setFacebookId(String facebookId) {
		put("facebookId", facebookId);
	}

	public String getEmail() {
		return getString("email");
	}

	public void setEmail(String email) {
		put("email", email);
	}

	public String getName() {
		return getString("name");
	}

	public void setName(String name) {
		put("name", name);
	}

	public static User getCurrentUser() {
		return (User) ParseUser.getCurrentUser();
	}

	public static ParseQuery<User> getUserQuery() {
		return ParseQuery.getQuery(User.class);
	}

	public void findOthersByType(User.Type userType, ModelCallback<User> callback) {
		ParseQuery<User> query = getUserQuery();
		query.whereContains("type", userType.toString());
		query.whereExists("name");  // filter out blank names
		query.whereNotEqualTo("objectId", this.getObjectId());  // exclude self
		query.findInBackground(new ModelFindCallback<User>(callback));
	}

	public void getUnfinishedRequest(ModelCallback<Request> callback) {
		String type = getType().toString().toLowerCase();
		ParseQuery<Request> query = Request.getRequestQuery();
		ArrayList<String> states = new ArrayList<String>();
		states.add(Request.State.FULFILLED.toString());
		states.add(Request.State.CANCELLED.toString());
		query.whereNotContainedIn("state", states);
		query.whereEqualTo(type, this);
		query.include("vendor");
		query.include("client");
		query.getFirstInBackground(new ModelGetCallback<Request>(callback));
	}

	public void saveAndPublish(PushUtil.HandlesPublish handler) {
		PushUtil.saveAndPublish(this, handler);
	}

	public void saveAndPublish() {
		PushUtil.saveAndPublish(this);
	}

	public double calculateDistance(User user) {
		return getLocation().distanceInMilesTo(user.getLocation());
	}

	public static void logout() {
		ParseFacebookUtils.getSession().closeAndClearTokenInformation();
		ParseUser.logOut();
	}
}
