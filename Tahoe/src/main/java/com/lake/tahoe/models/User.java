package com.lake.tahoe.models;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

/**
 * Created on 10/21/13.
 */

@ParseClassName("User")
public class User extends ParseUser {

	public static enum Type {
		CLIENT, // buys things
		VENDOR  // sells things
	}

	public User() {
		super();
	}

	public Type getType() {
		return Type.valueOf(getString("type"));
	}

	public void setType(Type type) {
		put("type", type.toString());
	}

	public ParseGeoPoint getLocation() {
		return (ParseGeoPoint) get("location");
	}

	public void setLocation(Double latitude, Double longitude) {
		put("location", new ParseGeoPoint(latitude, longitude));
	}

}
