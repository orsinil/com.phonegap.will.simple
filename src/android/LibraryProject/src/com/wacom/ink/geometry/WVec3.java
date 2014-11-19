/*
 * Created by Zahari Pastarmadjiev.
 * Copyright (c) 2014 Wacom. All rights reserved.
 */

package com.wacom.ink.geometry;

public class WVec3 {
	/**
	 * @hide
	 */
	public long handle;
	
	private native long nativeInitialize();
	private native void nativeFinalize(long handle);
	
	private native float nativeGetX(long handle);
	private native float nativeGetY(long handle);
	private native float nativeGetZ(long handle);
	
	private native void nativeSetX(long handle, float value);
	private native void nativeSetY(long handle, float value);
	private native void nativeSetZ(long handle, float value);
	
	public WVec3(){
		this(0, 0, 0);
	}

	public WVec3(float x, float y, float z) {
		handle = nativeInitialize();
		set(x, y, z);
	}
	
	public float getX(){
		return nativeGetX(handle);
	}
	
	public float getY(){
		return nativeGetY(handle);
	}
	
	public float getZ(){
		return nativeGetZ(handle);
	}
	
	public void setX(float value){
		nativeSetX(handle, value);
	}
	
	public void setY(float value){
		nativeSetY(handle, value);
	}
	
	public void setZ(float value){
		nativeSetZ(handle, value);
	}
	
	public void copy(WVec3 vec3){
		set(vec3.getX(), vec3.getY(), vec3.getZ());
	}
	
	public void set(float x, float y, float z){
		setX(x);
		setY(y);
		setZ(z);
	}
	
	public void setInf(){
		setX(Float.POSITIVE_INFINITY);
		setY(Float.POSITIVE_INFINITY);
		setZ(Float.POSITIVE_INFINITY);
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
