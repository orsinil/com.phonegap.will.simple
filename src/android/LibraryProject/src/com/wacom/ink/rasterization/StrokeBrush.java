/*
 * Created by Zahari Pastarmadjiev.
 * Copyright (c) 2014 Wacom. All rights reserved.
 */

package com.wacom.ink.rasterization;

/**
 * This base abstract class defines a stroke brush, required by a {@link StrokePaint} instance.  
 * The stroke brush contains rendering information about how the stroke should be drawn.
 * <br/>Currently available implementations:
 * <br/>{@link SolidColorBrush}
 * <br/>{@link ParticleBrush}
 */
public abstract class StrokeBrush{
	private long 		handle;
	private boolean 	bOwnTextures;
	private int 		identifier;

	private BlendMode 	blendMode;
	protected boolean	bChanged;
	
	private native long nativeInitialize(boolean bOwnTextures);
	private native void nativeFinalize(long handle);

	public StrokeBrush(boolean bOwnTextures){
		this.bOwnTextures = bOwnTextures;
		handle = nativeInitialize(bOwnTextures); 
	}

	public StrokeBrush(){
		this(false);
	}
	
	long getStrokeBrushHandle(){
		return handle;
	}

	public boolean hasOwnTextures(){
		return bOwnTextures;
	}

	
	/**
	 * Returns the blend mode of the stroke brush. This blend mode is being used when drawing a part of a stroke.
	 * @return the blendmode
	 */
	public BlendMode getBlendMode() {
		return blendMode;
	}

	private native void nativeSetBlendMode(long handle, byte blendMode);

	/**
	 * Sets the blend mode, which should be used when drawing a part of a stroke.
	 * @param blendMode
	 */
	public void setBlendMode(BlendMode blendMode) {
		if (blendMode!=this.blendMode){
			bChanged = true;
		}
		this.blendMode = blendMode;
		nativeSetBlendMode(handle, blendMode.getValue());
	}
	
	public void setBlendModeFromString(String blendModeString) {
		setBlendMode(BlendMode.getFromString(blendModeString));
	}
	
	public boolean isChanged(){
		return bChanged;
	}

	public void setReady() {
		bChanged = false;
	}

	
	@Override
	protected void finalize() throws Throwable {
		nativeFinalize(handle);
	}

	static { 
		System.loadLibrary("InkingEngine");
	}
}
