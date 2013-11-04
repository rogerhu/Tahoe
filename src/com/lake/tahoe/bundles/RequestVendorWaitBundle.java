package com.lake.tahoe.bundles;

import java.io.Serializable;

/**
 * Created by rhu on 11/3/13.
 */

// FIXME -- switch to Parcelable
public class RequestVendorWaitBundle implements Serializable {
	public static enum Result {
		CANCELLED
	}

	Result result;

	public RequestVendorWaitBundle(Result result) {
		this.result = result;
	}

}
