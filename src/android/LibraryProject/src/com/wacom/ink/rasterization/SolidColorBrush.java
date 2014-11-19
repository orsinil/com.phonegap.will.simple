/*
 * Created by Zahari Pastarmadjiev.
 * Copyright (c) 2014 Wacom. All rights reserved.
 */

package com.wacom.ink.rasterization;


/**
 * A StrokeBrush class used to draw strokes with a solid color.
 */
public class SolidColorBrush extends StrokeBrush {
	private boolean bGradientAntialiazingEnabled;
	
	private native void nativeSetShapeFillMethodEnabled(long handle);
	
	/**
	 * Creates and returns an instance. Strokes draw with this brush will be filled with a solid color.
	 */
	public SolidColorBrush() {
		super();
		nativeSetShapeFillMethodEnabled(getStrokeBrushHandle());
	}

	/**
	 * @return
	 */
	public boolean isGradientAntialiazingEnabled() {
		return bGradientAntialiazingEnabled;
	}

	private native void nativeSetGradientAntialiazingEnabled(long handle, boolean bGradientAntialiazingEnabled);
	public void setGradientAntialiazingEnabled(boolean bGradientAntialiazingEnabled) {
		if (this.bGradientAntialiazingEnabled!=bGradientAntialiazingEnabled){
			bChanged = true;
		}
		this.bGradientAntialiazingEnabled = bGradientAntialiazingEnabled;
		nativeSetGradientAntialiazingEnabled(getStrokeBrushHandle(), bGradientAntialiazingEnabled);
	}
}
