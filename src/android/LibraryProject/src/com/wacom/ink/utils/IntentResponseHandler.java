/*
 * Created by Zahari Pastarmadjiev.
 * Copyright (c) 2014 Wacom. All rights reserved.
 */

package com.wacom.ink.utils;

import android.content.Intent;

/**
 * When using the IntentManager class, this interface should be implemented to handle a Android intent awaiting a response via activity.onActivityResult.
 * 
 */
public abstract class IntentResponseHandler {
	public IntentResponseHandler(){
	}

	public abstract boolean handleResponse(int resultCode, Intent data);
}