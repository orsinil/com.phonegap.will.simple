/*
 * Created by Zahari Pastarmadjiev.
 * Copyright (c) 2014 Wacom. All rights reserved.
 */

package com.wacom.ink.geometry;

public class WVec2 {
	public float x;
	public float y;
	public float z;
	
	/**
	 * @hide
	 */
	public long handle;
	
	private native long nativeInitialize();
	private native void nativeFinalize(long handle);
	
	private native float nativeGetX(long handle);
	private native float nativeGetY(long handle);
	
	private native void nativeSetX(long handle, float value);
	private native void nativeSetY(long handle, float value);
	
	public WVec2(){
		this(0, 0);
	}

	public WVec2(float x, float y) {
		handle = nativeInitialize();
		set(x, y);
	}
	
	public float getX(){
		return nativeGetX(handle);
	}
	
	public float getY(){
		return nativeGetY(handle);
	}
	
	public void setX(float value){
		nativeSetX(handle, value);
	}
	
	public void setY(float value){
		nativeSetY(handle, value);
	}
	
	
	public void set(float x, float y){
		setX(x);
		setY(y);
	}
	
	public void setInf(){
		setX(Float.POSITIVE_INFINITY);
		setY(Float.POSITIVE_INFINITY);
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		nativeFinalize(handle);
	}
	
	static { 
		System.loadLibrary("InkingEngine");
	}
}
