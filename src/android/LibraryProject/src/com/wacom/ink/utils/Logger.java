/*
 * Created by Zahari Pastarmadjiev.
 * Copyright (c) 2013 Wacom. All rights reserved.
 */

package com.wacom.ink.utils;

import android.util.Log;

/**
 * Logger class for fast logging and debugging. 
 * Setting LOG_ENABLED to true will enable the debug logging for the java classes.
 * 
 */
public class Logger {
	public static final boolean LOG_ENABLED = false;
	
	private String tag;
	private boolean bEnabled;
	
	public Logger(Class<?> javaClass){
		this(javaClass, true);
	}
	
	public Logger(Class<?> javaClass, boolean bEnabled){
		this(javaClass.getSimpleName(), bEnabled);
	}
	
	public Logger(String tag, boolean bEnabled){
		this.tag = tag;
		this.bEnabled = bEnabled;
	}
	
	public void d(String message){
		if (bEnabled){
			Log.d(tag, message);
		}
	}

	public void i(String message) {
		if (bEnabled){
			Log.i(tag, message);
		}
	}

	public void e(String message) {
		if (bEnabled){
			Log.e(tag, message);
		}
	}
	
	public boolean isEnabled(){
		return bEnabled;
	}
	
	public void setEnabled(boolean bEnabled){
		this.bEnabled = bEnabled;
	}
}