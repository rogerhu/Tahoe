package com.lake.tahoe.activities;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import com.lake.tahoe.R;
import com.lake.tahoe.callbacks.ModelCallback;
import com.lake.tahoe.channels.RequestUpdateChannel;
import com.lake.tahoe.models.Request;
import com.lake.tahoe.models.User;
import com.lake.tahoe.utils.ErrorUtil;
import com.lake.tahoe.utils.PushUtil;

public class RequestActiveClientActivity extends RequestActiveActivity implements ModelCallback<Request>, RequestUpdateChannel.HandlesRequestUpdates {

	BroadcastReceiver subscription;
	Request currentRequest;

	@Override
	public void onRequestUpdated(Request request) {

		if (request == null)
			return;

		if (!request.getObjectId().equals(currentRequest.getObjectId()))
			return;

		if (request.getState().equals(Request.State.ACTIVE))
			return;

		if (request.getState().equals(Request.State.PENDING))
			startPendingActivity();

	}

	protected void startPendingActivity() {
		Intent i = new Intent(RequestActiveClientActivity.this, RequestPendingClientActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
	}

	@Override
	public void onRequestUpdateError(Throwable t) {
		onError(t);
	}

	@Override
	protected void onStart() {
		super.onStart();
		subscription = RequestUpdateChannel.subscribe(this, this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (subscription != null) {
			PushUtil.unsubscribe(this, subscription);
			subscription = null;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		User.getCurrentUser().getUnfinishedRequest(this);
	}

	@Override
	public void onModelFound(final Request request) {

		if (request == null || !request.getState().equals(Request.State.ACTIVE)) {
			finish();
			return;
		}

		currentRequest = request;
		User vendor = request.getVendor();
		createViews(vendor);
		createMapViews(vendor);

		if (request.getVendor() == null) {
			onError(new IllegalStateException("Bad request with no vendor"));
		} else {
			String title = String.format(getString(R.string.to_the_rescue, request.getVendor().getName()));
			getDynamicActionBar().setTitle(title);
		}

	}

	@Override
	public void onModelError(Throwable t) {
		onError(t);
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
	public void onError(Throwable t) {
		ErrorUtil.log(this, t);
	}
}
