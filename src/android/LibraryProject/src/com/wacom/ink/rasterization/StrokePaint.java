/*
 * Created by Zahari Pastarmadjiev.
 * Copyright (c) 2014 Wacom. All rights reserved.
 */

package com.wacom.ink.rasterization;

import android.graphics.Color;

import com.wacom.ink.utils.Utils;

/**
 * The StrokePaint class holds the {@link StrokeBrush}, color, width and cap beginning/ending information about how to draw strokes.
 * Stroke paint instances are being passed to the {@link InkCanvas#drawStroke(StrokePaint, StrokeJoin, java.nio.FloatBuffer, int, int, int, float, float)} method.
 */
public class StrokePaint {
	/**
	 * @hide
	 */
	public long				handle;
	private StrokeBrush		strokeBrush;
	private float 			a,r,g,b;
	private boolean 		bRoundCapBeginning;
	private boolean 		bRoundCapEnding;
	private float			width;
	private int				color;
	
	private native long nativeInitialize();
	private native void nativeFinalize(long handle);
	
	public StrokePaint(){
		this(null);
	}
	
	/**
	 * Creates and initializes an instance with the specified brush.
	 * 
	 * @param strokeBrush The brush can be solid color or particle brush.
	 */
	public StrokePaint(StrokeBrush strokeBrush){
		a = 0.0f;
		r = 0.0f;
		g = 0.0f;
		b = 0.0f;
		width = Float.NaN;
		handle = nativeInitialize();
		
		setStrokeBrush(strokeBrush);
		setColor(r, g, b, a);
		setWidth(width);
		setRoundCapBeginning(true);
		setRoundCapEnding(true);
	}

	private native void nativeSetStrokeBrush(long handle, long strokeBrushHandle);
	
	/**
	 * Sets the brush this paint instance should draw with.
	 * @param strokeBrush The brush can be solid color or particle brush.
	 */
	public void setStrokeBrush(StrokeBrush strokeBrush){
		this.strokeBrush = strokeBrush;
		if (strokeBrush!=null){
			nativeSetStrokeBrush(handle, strokeBrush.getStrokeBrushHandle());
		}
	}
	
	public StrokeBrush getStrokeBrush(){
		return strokeBrush;
	}
	
	private native void nativeSetColor(long handle, float r, float g, float b, float a);
	
	/**
	 * Sets the color to draw with.
	 * @param r The red component as float from 0 to 1.
	 * @param g The green component as float from 0 to 1.
	 * @param b The blue component as float from 0 to 1.
	 * @param a The alpha component as float from 0 to 1.
	 */
	public void setColor(float r, float g, float b, float a){
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		nativeSetColor(handle, r, g, b, a);
		updateColor();
	}
	
	public float getRed(){
		return r;
	}
	
	public float getGreen(){
		return g;
	}
	
	public float getBlue(){
		return b;
	}
	
	public float getAlpha(){
		return a;
	}
	
	public boolean isRoundCapBeginning() {
		return bRoundCapBeginning;
	}
	
	private native void nativeSetRoundCapBeginning(long handle, boolean bRoundCapBeginning);
	public void setRoundCapBeginning(boolean bRoundCapBeginning) {
		this.bRoundCapBeginning = bRoundCapBeginning;
		nativeSetRoundCapBeginning(handle, bRoundCapBeginning);
	}
	
	public boolean isRoundCapEnding() {
		return bRoundCapEnding;
	}
	
	private native void nativeSetRoundCapEnding(long handle, boolean bRoundCapEnding);
	public void setRoundCapEnding(boolean bRoundCapEnding) {
		this.bRoundCapEnding = bRoundCapEnding;
		nativeSetRoundCapEnding(handle, bRoundCapEnding);
	}
	
	/**
	 * Gets the width of the stroke. 
	 * @return The width.
	 */
	public float getWidth() {
		return width;
	}

	public void setColor(int color){
		setColor(color, 1.0f);
	}
	
	/**
	 * Gets the current color.
	 * @return The color.
	 */
	public int getColor(){
		return color;
	}
	
	public void setColorRGB(int color){
		float colorArray[] = Utils.colorToArray(color);
		setColorRGB(colorArray[1], colorArray[2], colorArray[3]);
	}
	
	public void setAlpha(int color){
//		float colorArr[] = Utils.colorToArray(color);
//		setAlpha(colorArr[4]);
		setAlpha(Color.alpha(color)/255.0f);
		updateColor();
	}
	
	public void setColor(int color, float overwriteAlpha){
		float colorArr[] = Utils.colorToArray(color);
		setColor(colorArr[1], colorArr[2], colorArr[3], Float.isNaN(overwriteAlpha)?colorArr[0]:overwriteAlpha);
	}
	
	private native void nativeSetWidth(long handle, float width);
	
	/**
	 * Set the width of the stroke. 
	 * @param width If this parameter is NAN, a variable width path is expected.
	 */
	public void setWidth(float width) {
		nativeSetWidth(handle, width);
		this.width = width;
	}
	
	private native void nativeSetAlpha(long handle, float a);
	public void setAlpha(float alpha){
		this.a = alpha;
		nativeSetAlpha(handle, a);
	}
	
	private native void nativeSetColorRGB(long handle, float r, float g, float b);
	public void setColorRGB(float r, float g, float b){
		this.r = r;
		this.g = g;
		this.b = b;
		
		updateColor();
		nativeSetColorRGB(handle, r, g, b);
	}
	
	public void copy(StrokePaint paint){
		setColor(paint.r, paint.g, paint.b, paint.a);
		setWidth(paint.width);
		setRoundCapBeginning(paint.bRoundCapBeginning);
		setRoundCapEnding(paint.bRoundCapEnding);
		setStrokeBrush(paint.strokeBrush);
	}
	
	public void updateColor(){
		if (Float.isNaN(a)){
			this.color = Color.rgb((int)(r*255.0f), (int)(g*255.0f), (int)(b*255.0f));
		} else {
			this.color = Color.argb((int)(a*255.0f), (int)(r*255.0f), (int)(g*255.0f), (int)(b*255.0f));
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		nativeFinalize(handle);
	}

	static { 
		System.loadLibrary("InkingEngine");
	}

	public void setRoundCaps(boolean bRoundCapBeginning, boolean bRoundCapEnding) {
		setRoundCapBeginning(bRoundCapBeginning);
		setRoundCapEnding(bRoundCapEnding);
	}
	
	@Override
	public String toString() {
		return "strokeBrush: " + strokeBrush + "; a: " + a + ",r: " + r + ",g: " + g + ",b: " + b + "; caps:" + bRoundCapBeginning + "," + bRoundCapEnding + "; width: " + width;
	}
}
