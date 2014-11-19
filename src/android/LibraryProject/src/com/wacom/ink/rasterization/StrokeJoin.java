/*
 * Created by Zahari Pastarmadjiev.
 * Copyright (c) 2014 Wacom. All rights reserved.
 */

package com.wacom.ink.rasterization;

import com.wacom.ink.geometry.WRect;
import com.wacom.ink.geometry.WVec3;

/**
 * The StrokeJoin class holds information about the last drawn particle, the affected dirty area during the last draw call and the seed for the random number generator used for the stroke rendering.  
 * This class is used to relay relevant stroke information between successive {@link InkCanvas#drawStroke(StrokePaint, StrokeJoin, java.nio.FloatBuffer, int, int, int, float, float)} calls. 
 * In fact it can be treated as a stroke rendering state between successive draw calls.
 * The last drawn particle is relevant only for strokes drawn with particle brush.
 * Using StrokeJoin instances a single stroke can be drawn with several successive draw calls. 
 * Each draw call will continue from state (the stroke join) at which the previous draw call finished.
 */
public class StrokeJoin {
	/**
	 * @hide
	 */
	public long handle;
	private native long nativeInitialize(long lastPointHandle, long dirtyAreaHandle, boolean bUseSeed, int seed);
	private native void nativeFinalize(long handle);
	private native void nativeUpdateHandles(long handle, long lastPointHandle, long dirtyAreaHandle);
	
	private WVec3	lastPoint;
	private WRect	dirtyArea;
	private boolean bUseSeed;
	
	/**
	 * Create new instance.
	 */
	public StrokeJoin(){
		lastPoint = new WVec3();
		dirtyArea = new WRect();
		this.bUseSeed = false;
		handle = nativeInitialize(lastPoint.handle, dirtyArea.handle, bUseSeed, 0);
	}
	/**
	 * Create new instance with a seed for the random number generator used for the stroke rendering. 
	 * This parameter is relevant only for strokes drawn with a particle brush. 
	 * The stroke will look exactly the same every time you draw it with the same seed.
	 * @param seed The seed for the random number generator used for the stroke the stroke rendering.
	 */
	public StrokeJoin(int seed){
		lastPoint = new WVec3();
		dirtyArea = new WRect();
		bUseSeed = true;
		handle = nativeInitialize(lastPoint.handle, dirtyArea.handle, bUseSeed, seed);
	}
	
	/**
	 * Resets the instance, setting the last particle's coordinates to Inf and the affected dirty area to NaN.
	 */
	public void reset(){
		lastPoint.setInf();
		dirtyArea.setNaN();
		setUseSeed(false);
	}
	
	/**
	 * Resets the instance, setting the last particle's coordinates to Inf and the affected dirty area to NaN.
	 * @param seed The seed for the random number generator used for the stroke the stroke rendering.
	 */
	public void reset(int seed){
		lastPoint.setInf();
		dirtyArea.setNaN();
		setUseSeed(false);
		setSeed(seed);
	}
	
	/**
	 * This method returns the last drawn particle. It is relevant only for strokes drawn with a particle brush.
	 * @return lastPoint The last particle.
	 */
	public WVec3 getLastPoint(){
		return lastPoint;
	}
	
	public void setLastPoint(WVec3 lastPoint) {
		this.lastPoint = lastPoint;
		nativeUpdateHandles(handle, lastPoint.handle, dirtyArea.handle);
	}
	
	/**
	 * This method returns area of the stroke, which was affected during the last draw call.
	 * @return The affected area.
	 */
	public WRect getDirtyArea() {
		return dirtyArea;
	}

	public void setDirtyArea(WRect dirtyArea) {
		this.dirtyArea = dirtyArea;
		nativeUpdateHandles(handle, lastPoint.handle, dirtyArea.handle);
	}
	
	private native int nativeGetSeed(long handle);
	public int getSeed(){
		return nativeGetSeed(handle);
	}
	
	private native void nativeSetSeed(long handle, int seed);
	public void setSeed(int seed){
		nativeSetSeed(handle, seed);
	}
	
	private native void nativeSetUseSeed(long handle, boolean bUseSeed);
	public void setUseSeed(boolean bUseSeed){
		this.bUseSeed = bUseSeed;
		nativeSetUseSeed(handle, bUseSeed);
	}
	
	/**
	 * This method copies the contents of the provided StrokeJoin instance to the current instance.
	 * @param strokeJoin The instance to copy the contents from - the last drawn particle, the affected dirty area during the last draw call and the seed for the random number generator used for the stroke rendering.
	 */
	public void copy(StrokeJoin strokeJoin){
		dirtyArea.copy(strokeJoin.dirtyArea);
		lastPoint.copy(strokeJoin.lastPoint);
//		nativeUpdateHandles(handle, lastPoint.handle, dirtyArea.handle);
		nativeSetSeed(handle, strokeJoin.getSeed());
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		nativeFinalize(handle);
	}
}
