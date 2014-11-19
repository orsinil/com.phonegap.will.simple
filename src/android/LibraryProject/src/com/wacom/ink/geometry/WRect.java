/*
 * Created by Zahari Pastarmadjiev.
 * Copyright (c) 2014 Wacom. All rights reserved.
 */

package com.wacom.ink.geometry;

public class WRect {
	/**
	 * @hide
	 */
	public long handle;

	private native long nativeInitialize();
	private native void nativeFinalize(long handle);

	private native float nativeGetX(long handle);
	private native float nativeGetY(long handle);
	private native float nativeGetW(long handle);
	private native float nativeGetH(long handle);	

	private native void nativeSetX(long handle, float value);
	private native void nativeSetY(long handle, float value);
	private native void nativeSetW(long handle, float value);
	private native void nativeSetH(long handle, float value);	

	public WRect() {
		this(Float.NaN, Float.NaN, Float.NaN, Float.NaN);
	}
	
	public WRect(float w, float h){
		this(0, 0, w, h);
	}

	public WRect(WRect rect) {
		handle = nativeInitialize();
		set(rect);
	}
	
	public WRect(float x, float y, float w, float h) {
		handle = nativeInitialize();
		set(x, y, w, h);
	}

	public float getX(){
		return nativeGetX(handle);
	}

	public float getY(){
		return nativeGetY(handle);
	}

	public float getWidth(){
		return nativeGetW(handle);
	}

	public float getHeight(){
		return nativeGetH(handle);
	}
	
	public void setX(float value){
		nativeSetX(handle, value);
	}

	public void setY(float value){
		nativeSetY(handle, value);
	}

	public void setWidth(float value){
		nativeSetW(handle, value);
	}

	public void setHeight(float value){
		nativeSetH(handle, value);
	}

	public boolean isNaN(){
		return Float.isNaN(getX()) || Float.isNaN(getY()) || Float.isNaN(getWidth()) || Float.isNaN(getHeight());
	}

	public void setNaN(){
		set(Float.NaN, Float.NaN, Float.NaN, Float.NaN);
	}

	public void copy(WRect rect){
		set(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	}
	
	public void union(WRect rect){
		if (isNaN()){
			if (!rect.isNaN()){
				set(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
			}
			return;
		}
		if (rect.isNaN()){
			return;
		}
		float nx1 = Math.min(getX(), rect.getX());
		float ny1 = Math.min(getY(), rect.getY());
		float nx2 = Math.max(getX()+getWidth(), rect.getX()+rect.getWidth());
		float ny2 = Math.max(getY()+getHeight(), rect.getY()+rect.getHeight());
		
		set(nx1, ny1, nx2-nx1, ny2-ny1);
	}
	
	public boolean intersect(WRect rect) {
		if (rect.isNaN()) {
			setNaN();
			return true;
		}
		return intersect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	}
		
	public boolean intersect(float x, float y, float width, float height) {
		if (!isNaN()){
			float[] curr = new float[]{getX(), getY(), getWidth(), getHeight()};
	        if (curr[0] < x + width && x < curr[0] + curr[2] && curr[1] < y + height && y < curr[1] + curr[3]) {
	            float nx1 = Math.max(curr[0], x);
	            float ny1 = Math.max(curr[1], y);
	            float nx2 = Math.min(curr[0] + curr[2], x + width);
	            float ny2 = Math.min(curr[1] + curr[3], y + height);
	            
	            set(nx1, ny1, nx2-nx1, ny2-ny1);
	            return true;
	        }
		}
        return false;
    }
	
	public void roundOut() {
		float nx = (float) Math.floor(getX());
		float ny = (float) Math.floor(getY());
		float nw = (float) Math.ceil(getWidth());
		float nh = (float) Math.ceil(getHeight());
		
		set(nx, ny, nw, nh);
	}

	public void set(WRect rect) {
		set(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	}

	public void set(float x, float y, float w, float h){
		setX(x);
		setY(y);
		setWidth(w);
		setHeight(h);
	}

	@Override
	public String toString() {
		return "[" + getX() + "," + getY() + "; " + getWidth() + "," + getHeight() + "]";
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
