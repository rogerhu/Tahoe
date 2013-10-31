package com.lake.tahoe.activities;

import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.lake.tahoe.R;
import com.lake.tahoe.utils.ErrorUtil;
import com.lake.tahoe.utils.HandlesErrors;

public class RequestMapActivity extends GooglePlayServicesActivity implements HandlesErrors {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_request_map);
		SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		GoogleMap map = fragment.getMap();
		map.setMyLocationEnabled(true);

	}

	@Override
	protected void onGooglePlayServicesReady() {
	}

	@Override
	protected void onGooglePlayServicesError(Throwable t) {
		onError(t);
	}

	@Override
	public void onError(Throwable t) {
		ErrorUtil.log(this, t);
	}

}
