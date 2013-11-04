package com.lake.tahoe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.lake.tahoe.R;
import com.lake.tahoe.bundles.RequestVendorWaitBundle;
import com.lake.tahoe.utils.ErrorUtil;
import com.lake.tahoe.utils.HandlesErrors;
import com.lake.tahoe.views.DynamicActionBar;

public class RequestVendorWaitActivity extends GoogleLocationServiceActivity implements HandlesErrors {

	GoogleMap map;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_request_vendor_wait);

		DynamicActionBar actionBar = new DynamicActionBar(RequestVendorWaitActivity.this, getResources().getColor(R.color.black));

		actionBar.setTitle(getString(R.string.waiting_for_vendors));
		actionBar.setXMarkVisibility(View.VISIBLE, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent();
				RequestVendorWaitBundle result = new RequestVendorWaitBundle(RequestVendorWaitBundle.Result.CANCELLED);
				setResult(RESULT_OK, i);
				i.putExtra("result", result);
				finish();
			}
		});
	}

	protected void onGooglePlayServicesReady() {

		SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		map = fragment.getMap();
	    map.setMyLocationEnabled(true);
		map.getUiSettings().setZoomControlsEnabled(false);
		map.getUiSettings().setMyLocationButtonEnabled(false);
	}

	@Override
	public void onError(Throwable t) {
		ErrorUtil.log(this, t);
	}

	@Override
	protected void onGooglePlayServicesError(Throwable t) {
		onError(t);
	}

	@Override
	protected void onLocationTrackingFailed(Throwable t) {
		onError(t);

	}
}
