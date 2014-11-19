/*
 * Created by Zahari Pastarmadjiev.
 * Copyright (c) 2014 Wacom. All rights reserved.
 */

package com.wacom.ink.utils;

import android.content.Intent;
import android.util.SparseArray;

/**
 * A simple Android Intent manager. This class is designed to work with IntentResponseHandler implementations.
 * 
 */
public class IntentManager{
	private SparseArray<IntentResponseHandler> handlers;
	
	public IntentManager(){
		handlers = new SparseArray<IntentResponseHandler>();
	}
	
	public IntentResponseHandler getIntentResponseHandler(int requestCode){
		return handlers.get(requestCode);
	}
	
	public void addIntentResponseHandler(int requestCode, IntentResponseHandler handler){
		handlers.put(requestCode, handler);
	}
	
	public boolean processIntentResponse(int requestCode, int resultCode, Intent data){
		IntentResponseHandler handler = getIntentResponseHandler(requestCode);
		if (handler!=null){
			return handler.handleResponse(resultCode, data);
		}
		return false;
	}
}