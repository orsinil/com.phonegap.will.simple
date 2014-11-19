/*
 * Created by Zahari Pastarmadjiev.
 * Copyright (c) 2014 Wacom. All rights reserved.
 */

package com.wacom.ink.rasterization;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.opengl.GLES20;

import com.wacom.ink.utils.Logger;
import com.wacom.ink.utils.OpenGLUtils;
import com.wacom.ink.utils.Utils;

/**
 * A StrokeBrush class used to draw strokes with the particle scattering method.
 * 
 */
public class ParticleBrush extends StrokeBrush {
	final static Logger logger = new Logger(ParticleBrush.class);
	
	private boolean bRotateAlongTrajectory;
	private String 	fillTextureFilename;
	private String 	shapeTextureFilename;
	private String	allocatedFillTextureFilename;
	private String	allocatedShapeTextureFilename;
//	private int 	fillTextureId;
//	private int 	shapeTextureId;
	private boolean	bBuildUp;
	private boolean	bRandomizeFill;
	private boolean	bRotateRandom;  
	private float	spacing;
	private float	scattering;
	private Point	fillTextureSize;
	
	private native void nativeSetScatterMethodEnabled(long handle);
	
	/**
	 * Creates and initializes an instance. Strokes with this brush will be drawn using a large number of small textures (called particles), scattered along the stroke's trajectory.
	 * @param bOwnTextures
	 */
	public ParticleBrush(boolean bOwnTextures) {
		super(bOwnTextures);
		nativeSetScatterMethodEnabled(getStrokeBrushHandle());
	}
	
	/**
	 * @return
	 */
	public boolean shouldRotateAlongTrajectory() {
		return bRotateAlongTrajectory;
	}
	
	private native void nativeSetFillTextureSize(long handle, float x, float y);
	public void setFillTextureSize(Point fillTextureSize) {
		if ((fillTextureSize==null && this.fillTextureSize!=null) || (fillTextureSize!=null && !fillTextureSize.equals(this.fillTextureSize))){
			bChanged = true;
		}
		
		this.fillTextureSize = fillTextureSize;
		
		if (fillTextureSize!=null){
			nativeSetFillTextureSize(getStrokeBrushHandle(), fillTextureSize.x, fillTextureSize.y);
		} else {
			nativeSetFillTextureSize(getStrokeBrushHandle(), Float.NaN, Float.NaN);
		}
	}

	/**
	 * @return
	 */
	public Point getFillTextureSize() {
		return fillTextureSize;
	}

	private native void nativeSetScattering(long handle, float scattering);
	public void setScattering(float scattering) {
		if (scattering!=this.scattering){
			bChanged = true;
		}
		this.scattering = scattering;
		nativeSetScattering(getStrokeBrushHandle(), scattering);
	}

	/**
	 * @return
	 */
	public float getScattering(){
		return scattering;
	}

	private native void nativeSetSpacing(long handle, float spacing);
	public void setSpacing(float spacing) {
		if (spacing!=this.spacing){
			bChanged = true;
		}
		this.spacing = spacing;
		nativeSetSpacing(getStrokeBrushHandle(), spacing);
	}

	/**
	 * @return
	 */
	public float getSpacing() {
		return spacing;
	}

	private native void nativeSetRotateAlongTrajectory(long handle, boolean bRotateAlongTrajectory);
	public void setRotateAlongTrajectory(boolean bRotateAlongTrajectory) {
		if (bRotateAlongTrajectory!=this.bRotateAlongTrajectory){
			bChanged = true;
		}
		this.bRotateAlongTrajectory = bRotateAlongTrajectory;
		nativeSetRotateAlongTrajectory(getStrokeBrushHandle(), bRotateAlongTrajectory);
	}

	/**
	 * @return
	 */
	public String getFillTextureFilename() {
		return fillTextureFilename;
	}

	public void setFillTextureFilename(String fillTextureFilename) {
		if ((fillTextureFilename==null && this.fillTextureFilename!=null) || (fillTextureFilename!=null && !fillTextureFilename.equals(this.fillTextureFilename))){
			bChanged = true;
		}
		this.fillTextureFilename = fillTextureFilename;
	}

	/**
	 * @return
	 */
	public String getShapeTextureFilename() {
		return shapeTextureFilename;
	}

	public void setShapeTextureFilename(String shapeTextureFilename) {
		if ((shapeTextureFilename==null && this.shapeTextureFilename!=null) || (shapeTextureFilename!=null && !shapeTextureFilename.equals(this.shapeTextureFilename))){
			bChanged = true;
		}
		this.shapeTextureFilename = shapeTextureFilename;
	}

//	/**
//	 * @return
//	 */
//	public boolean shouldBuildUp() {
//		return bBuildUp;
//	}
//
//	private native void nativeSetBuildUp(long handle, boolean bBuildUp);
//	public void setBuildUp(boolean bBuildUp) {
//		if (bBuildUp!=this.bBuildUp){
//			bChanged = true;
//		}
//		this.bBuildUp = bBuildUp;
//		nativeSetBuildUp(getStrokeBrushHandle(), bBuildUp);
//	}

	/**
	 * @return
	 */
	public boolean shouldRandomizeFill() {
		return bRandomizeFill;
	}

	private native void nativeSetRandomizeFill(long handle, boolean bRandomizeFill);
	public void setRandomizeFill(boolean bRandomizeFill) {
		if (bRandomizeFill!=this.bRandomizeFill){
			bChanged = true;
		}
		this.bRandomizeFill = bRandomizeFill;
		nativeSetRandomizeFill(getStrokeBrushHandle(), bRandomizeFill);
	}

	/**
	 * @return
	 */
	public boolean shouldRotateRandom() {
		return bRotateRandom;
	}

	private native void nativeSetRotateRandom(long handle, boolean bRotateRandom);
	public void setRotateRandom(boolean bRotateRandom) {
		if (bRotateRandom!=this.bRotateRandom){
			bChanged = true;
		}
		this.bRotateRandom = bRotateRandom;
		nativeSetRotateRandom(getStrokeBrushHandle(), bRotateRandom);
	}

	private native int nativeGetFillTextureId(long handle);
	public int getFillTextureId() {
		return nativeGetFillTextureId(getStrokeBrushHandle());
	}

	private native void nativeSetFillTextureId(long handle, int fillTextureId);
	public void setFillTextureId(int fillTextureId) {
		if (fillTextureId!=getFillTextureId()){
			bChanged = true;
		}
		nativeSetFillTextureId(getStrokeBrushHandle(), fillTextureId);
	}

	private native int nativeGetShapeTextureId(long handle);
	public int getShapeTextureId() {
		return nativeGetShapeTextureId(getStrokeBrushHandle());
	}

	private native void nativeSetShapeTextureId(long handle, int shapeTextureId);
	public void setShapeTextureId(int shapeTextureId) {
		if (shapeTextureId!=getShapeTextureId()){
			bChanged = true;
		}
		nativeSetShapeTextureId(getStrokeBrushHandle(), shapeTextureId);
	}
	
	/**
	 * This method loads the shape and fill textures into OpenGL textures. It has to be called from the GL thread (the thread the WILL SDK has been initialized from).
	 * @param context The android context.
	 */
	public void allocateTextures(Context context){
		allocateTextures(context, null);
	}
	
	/**
	 * This method loads the shape and fill textures into OpenGL textures. It has to be called from the GL thread (the thread the WILL SDK has been initialized from).
	 * @param context The android context.
	 * @param location An alternative location to search the textures in.
	 */
	public void allocateTextures(Context context, String location){
		try {
			if (Logger.LOG_ENABLED) logger.i("allocateTextures<start>: " + getFillTextureFilename() + "(" + getFillTextureId() + ") / " + getShapeTextureFilename() + "(" + getShapeTextureId() + ")");
			if (!getFillTextureFilename().equals(allocatedFillTextureFilename) || getFillTextureId()==0){
				InputStream is = Utils.openFile(context, getFillTextureFilename(), location);
				Bitmap bitmap = BitmapFactory.decodeStream(is);
				int fillTexId = OpenGLUtils.bitmapToOpenGL(bitmap, getFillTextureId(), GLES20.GL_LINEAR, GLES20.GL_REPEAT);
				setFillTextureId(fillTexId);
				setFillTextureSize(new Point(bitmap.getWidth(), bitmap.getHeight()));
				is.close();
				allocatedFillTextureFilename = getFillTextureFilename();
			}
			if (!getShapeTextureFilename().equals(allocatedShapeTextureFilename) || getShapeTextureId()==0){
				int shapeTexId = OpenGLUtils.generateTexture(getShapeTextureId(), true);
				OpenGLUtils.generateMipmaps(context, getShapeTextureFilename(), location, shapeTexId, GLES20.GL_LINEAR, GLES20.GL_REPEAT);
				setShapeTextureId(shapeTexId);
				allocatedShapeTextureFilename = getShapeTextureFilename();
			}
			if (Logger.LOG_ENABLED) logger.i("allocateTextures<end>: " + getFillTextureFilename() + "(" + getFillTextureId() + ") / " + getShapeTextureFilename() + "(" + getShapeTextureId() + ")");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
