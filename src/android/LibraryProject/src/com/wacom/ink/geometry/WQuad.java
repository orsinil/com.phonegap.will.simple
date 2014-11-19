/*
 * Created by Zahari Pastarmadjiev.
 * Copyright (c) 2014 Wacom. All rights reserved.
 */

package com.wacom.ink.geometry;

public class WQuad{
	private WVec2[] quad;
	
	/**
	 * @hide
	 */
	public long handle;
	
	private native long nativeInitialize();
	private native void nativeFinalize(long handle);
	private native void nativeSet(long handle, long bottomLeftHandle, long bottomRightHandle, long topLeftHandle, long topRightHandle);
	
	public WQuad(WVec2 leftBottom, WVec2 rightBottom, WVec2 leftTop, WVec2 rightTop){
		handle = nativeInitialize();
		set(leftBottom, rightBottom, leftTop, rightTop);
	}
	
	public WVec2[] get(){
		return quad;
	}
	
	public void set(WVec2 leftBottom, WVec2 rightBottom, WVec2 leftTop, WVec2 rightTop) {
		quad = new WVec2[]{leftBottom, rightBottom, leftTop, rightTop};
		nativeSet(handle, leftBottom.handle, rightBottom.handle, leftTop.handle, rightTop.handle);
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
