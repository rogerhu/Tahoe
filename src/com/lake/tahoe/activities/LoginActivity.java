package com.lake.tahoe.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import com.facebook.FacebookException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.lake.tahoe.R;
import com.lake.tahoe.models.User;
import com.lake.tahoe.utils.ErrorUtil;
import com.lake.tahoe.utils.HandlesErrors;
import com.lake.tahoe.utils.ManifestReader;
import com.parse.*;

import java.util.Arrays;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

public class LoginActivity extends Activity implements HandlesErrors, View.OnClickListener {

	View btnLogin, pbLoading;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		btnLogin = findViewById(R.id.btnLogin);
		pbLoading = findViewById(R.id.pbLoading);
		btnLogin.setOnClickListener(this);
		animate(btnLogin).alpha(1).setStartDelay(1500).setDuration(1000).setInterpolator(new AccelerateInterpolator()).start();
		animate(findViewById(R.id.tvOdd)).translationYBy(160).setDuration(1300).setInterpolator(new OvershootInterpolator()).start();
		animate(findViewById(R.id.tvJob)).translationYBy(160).setStartDelay(400).setDuration(1300).setInterpolator(new OvershootInterpolator()).start();
		animate(findViewById(R.id.ivBg)).translationXBy(-150).setDuration(5000).setInterpolator(new DecelerateInterpolator()).start();
	}

	@Override
	public void onClick(View v) {
		// TODO: Show a spinner, or at least disable the button. Right now it hangs out for a bit when the user returns.
		String fbPermissions = (String) ManifestReader.getPackageMetaData(getApplicationContext(), "com.facebook.sdk.PERMISSIONS");
		ParseFacebookUtils.logIn(Arrays.asList(fbPermissions.split(",")), this, new OnLogIn());
		pbLoading.setVisibility(View.VISIBLE);
		btnLogin.setVisibility(View.INVISIBLE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}

	private class OnLogIn extends LogInCallback {
		@Override
		public void done(ParseUser parseUser, ParseException e) {
			if (e == null) getGraphData();
			else onError(e);
		}
	}

	private void getGraphData() {
		Request.newMeRequest(ParseFacebookUtils.getSession(), new OnGraphData()).executeAsync();
	}

	private class OnGraphData implements Request.GraphUserCallback {
		@Override public void onCompleted(GraphUser graphUser, Response response) {
			if (response.getError() != null)
				onError(new FacebookException(response.getError().toString()));
			if (graphUser != null) {
				User currentUser = User.getCurrentUser();
				currentUser.setFacebookId(graphUser.getId());
				currentUser.setName(graphUser.getFirstName());
				currentUser.setEmail((String) graphUser.getProperty("email"));
				currentUser.setType(User.Type.CLIENT);
				currentUser.saveEventually(new OnUserSaved());
			}
		}
	}

	class OnUserSaved extends SaveCallback {
		@Override public void done(ParseException e) {
			if (e == null)  finish();
			else onError(e);
		}
	}

	/**
	 * TODO: Make this better. Show something on the login page
	 */
	@Override
	public void onError(Throwable t) {
		pbLoading.setVisibility(View.INVISIBLE);
		btnLogin.setVisibility(View.VISIBLE);
		ErrorUtil.log(this, t);
	}

}
