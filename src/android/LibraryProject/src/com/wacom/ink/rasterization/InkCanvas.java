/*
 * Created by Zahari Pastarmadjiev.
 * Copyright (c) 2014 Wacom. All rights reserved.
 */

package com.wacom.ink.rasterization;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.wacom.ink.geometry.WQuad;
import com.wacom.ink.geometry.WRect;
import com.wacom.ink.utils.Mx;
import com.wacom.ink.utils.Utils;

/**
 * The {@link InkCanvas} is a graphics canvas, that is used to render visual objects like stokes and layers, to fill paths and others.
 * The destination of draw operations is always a instance of the {@link Layer} class.
 * All draw operations are performed on layers, while only one layer can be set as target layer.
 * <br/>The method that is used to render strokes is the {@link InkCanvas#drawStroke(StrokePaint, StrokeJoin, FloatBuffer, int, int, int, float, float)} 
 * method. It uses {@StrokePaint} instance, holding information about how to render the stroke, and a  {@link StrokeJoin} instance "joining" two successive rendered strokes parts.
 * <br/>To draw a layer the {@link InkCanvas#drawLayer(Layer, BlendMode)} method should be called.
 */
public class InkCanvas {
	/**
	 * @hide
	 */
	public long handle;
	private boolean bInitialized;
	
	private native long nativeInitialize();
	private native void nativeFinalize(long handle);

	private native void nativeGlInit(long handle);
	private native void nativeSetTarget(long handle, long layerHandle);
	private native void nativeClearColor(long handle, float red, float green, float blue, float alpha);
	private native void nativeSetTargetClipRect(long handle, long rectHandle);
	private native void nativeDisableTargetClipRect(long handle);
	
	//controlPointsBeginning sequence: x,y,w,a
	private native void nativeDrawStroke(long inkCanvasHandle, long strokePaintHandle, long strokeJoinHandle,
			FloatBuffer controlPointsBuffer, int bufferPosition, int bufferSize, int stride, float ts, float tf);
	
	private native void nativeDrawLayer(long handle, long layerHandle, float[] rowMajor4x4Matrix, byte blendMode);
	private native void nativeDrawLayer(long handle, long layerHandle, long sourceQuadHandle, long destinationQuadHandle, byte blendMode);
	
	protected int width, height;
//	private float identityMatrix4x4[];
	
	/**
	 * Constructs an InkCanvas instance.
	 */
	public InkCanvas(){
		handle = nativeInitialize();
	}
	
	/**
	 * Returns the width of the canvas. The width and the height of the canvas should have been already set using {@link InkCanvas#setDimensions(int, int)}.
	 * @return width of the canvas
	 */
	public int getWidth(){
		return width;
	}
	
	/**
	 * Returns the height of the canvas. The width and the height of the canvas should have been already set using {@link InkCanvas#setDimensions(int, int)}.
	 * @return height of the canvas
	 */
	public int getHeight(){
		return height;
	}
	
	/**
	 * Sets the dimensions of the canvas.
	 * @param width
	 * @param height
	 */
	public void setDimensions(int width, int height){
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Initializes the canvas. This method should be called on the GL thread.
	 */
	public void glInit(){
		nativeGlInit(handle);
	}

	/**
	 * Sets the layer that is going to be the storage of the draw operations.
	 * @param layer The layer to be set as active.
	 */
	public void setTarget(Layer layer){
		nativeSetTarget(handle, layer.handle);
	}
	
	/**
	 * Clears the currently set clipping rect of the target layer with 0x00000000 color. <br/>This method should be called on the GL thread.
	 */
	public void clearColor(){
		nativeClearColor(handle, 0.0f, 0.0f, 0.0f, 0.0f);
	}

	/**
	 * Clears the currently set clipping rect of the target layer with the provided ARGB color. <br/>This method should be called on the GL thread.
	 */
	public void clearColor(int color){
		nativeClearColor(handle, Color.red(color)/255.0f, Color.green(color)/255.0f, Color.blue(color)/255.0f, Color.alpha(color)/255.0f);
	}
	
	/**
	 * Clears the currently set clipping rect of the target layer with the provided color. <br/>This method should be called on the GL thread.
	 * @param alpha The alpha component of the color.
	 * @param red The red component of the color.
	 * @param green The green component of the color.
	 * @param blue The blue component of the color.
	 */
	public void clearColor(int alpha, int red, int green, int blue){
		nativeClearColor(handle, red/255.0f, green/255.0f, blue/255.0f, alpha/255.0f);
	}
	
	/**
	 * Clears the currently set clipping rect of the target layer with the provided color. <br/>This method should be called on the GL thread.
	 * @param alpha The alpha component of the color as float from 0 to 1.
	 * @param red The red component of the color as float from 0 to 1.
	 * @param green The green component of the color as float from 0 to 1.
	 * @param blue The blue component of the color as float from 0 to 1.
	 */
	public void clearColor(float alpha, float red, float green, float blue){
		nativeClearColor(handle, red, green, blue, alpha);
	}

	/**
	 * This method set a clipping rect of the current target. 
	 * This call will lead to a glScissor call. 
	 * Note that this will be set only if the target is not changed.
	 * Every change of the target will disable the clipping rect.
	 * <br/>This method should be called on the GL thread.
	 * @param rect The clipping rect to be set.
	 */
	public void setClipRect(WRect rect){
		nativeSetTargetClipRect(handle, rect.handle);
	}

	/**
	 * This method disables the currently set clipping rect.
	 */
	public void disableClipRect(){
		nativeDisableTargetClipRect(handle);
	}

	/**
	 * Draws a stroke into the currently set target layer. 
	 * <br/>This method should be called on the GL thread.
	 * @param strokePaint A StrokePaint containing information how to draw the stroke.
	 * @param strokeJoin A StrokeJoin storing relevant stroke composition information between successive drawStroke calls
	 * @param controlPointsBuffer A float buffer containing the control points
	 * @param bufferPosition The position in controlPointsBuffer to read the control points from
	 * @param bufferSize The size of the points to read in floats.
	 * @param bRoundCapBeggining Cap the stroke with a circle at the start.
	 * @param bRoundCapEnding Cap the stroke with a circle at the end.
	 * @param ts The starting value for the Catmull-Rom spline parameter (0 is the default value).
	 * @param tf The ending value for the Catmull-Rom spline parameter (1 is the default value).
	 */
	public void drawStroke(StrokePaint strokePaint, StrokeJoin strokeJoin, FloatBuffer controlPointsBuffer, int bufferPosition, int bufferSize, int stride, float ts, float tf){
		nativeDrawStroke(handle, strokePaint.handle, strokeJoin.handle, controlPointsBuffer, bufferPosition, bufferSize, stride, ts, tf);		
	}

	private native void nativeFillPath(long handle, FloatBuffer points, int size, int stride, float r, float g, float b, float a, boolean bAntialiazing);
	
	/**
	 * Fills the interior of a path with a solid color. The path is defined by a list of Catmull-Rom spline control points. The path will be close automatically. <br/>This method should be called on the GL thread.
	 * @param buffer The path to be used for the fill operation.
	 * @param size The size of the path in floats.
	 * @param stride The stride of the path.
	 * @param color The fill color.
	 * @param bAntiAliasing Whether or not to anti-alias the edges of the path. Default value is false. If you set it to true, you have to have to keep in mind the fact, that due to implementention reasons, it will work correctly only when the target layer is cleared with black color.
	 */
	public void fillPath(FloatBuffer buffer, int size, int stride, int color, boolean bAntiAliasing){
		float ca[] = Utils.colorToArray(color);
		nativeFillPath(handle, buffer, size, stride, ca[Utils.COLOR_ARR_RED], ca[Utils.COLOR_ARR_GREEN], ca[Utils.COLOR_ARR_BLUE], ca[Utils.COLOR_ARR_ALPHA], bAntiAliasing);
	}

	/**
	 * Fills the interior of a path with a solid color. The path is defined by a list of Catmull-Rom spline control points. The path will be close automatically. <br/>This method should be called on the GL thread.
	 * @param buffer The path to be used for the fill operation.
	 * @param size The size of the path in floats.
	 * @param stride The stride of the path.
	 * @param red The red color's component
	 * @param green The green color's component
	 * @param blue The blue color's component
	 * @param alpha The alpha color's component
	 * @param bAntiAliasing Whether or not to anti-alias the edges of the path. Default value is false. If you set it to true, you have to have to keep in mind the fact, that due to implementention reasons, it will work correctly only when the target layer is cleared with black color.
	 */
	public void fillPath(FloatBuffer buffer, int size, int stride, float r, float g, float b, float a, boolean bAntiAliasing){
		nativeFillPath(handle, buffer, size, stride, r, g, b, a, bAntiAliasing);
	}

	/**
	 * Draws the content of the layer into the current target layer. <br/>This method should be called on the GL thread.
	 * @param layer The layer to be drawn.
	 * @param blendMode The blend mode to be used.
	 */
	public void drawLayer(Layer layer, BlendMode blendMode){
		drawLayer(layer, null, blendMode);
	}
	
	/**
	 * Draws the given layer into the currently set target layer using the specified {@link BlendMode}. <br/>This method should be called on the GL thread.
	 * @param layer the source layer
	 * @param matrix this transformation matrix will be applied on the source layer
	 * @param blendMode The blend mode to be used.
	 */
	public void drawLayer(Layer layer, Matrix matrix, BlendMode blendMode){
		float mxValues4x4[] = null;
		if (matrix!=null){
			mxValues4x4 = new float[16];
			float mxValues3x3[] = new float[9];
			matrix.getValues(mxValues3x3);
			Mx.convertTo4x4Matrix(mxValues4x4, mxValues3x3);
		}
		nativeDrawLayer(handle, layer.handle, mxValues4x4, blendMode.getValue());
	}

	private void drawLayer(Layer layer, WQuad sourceQuad, WQuad destinationQuad, BlendMode blendMode){
		long srcHandle = sourceQuad != null ? sourceQuad.handle : 0;
		long dstHandle = destinationQuad != null ? destinationQuad.handle : 0;
		nativeDrawLayer(handle, layer.handle, srcHandle, dstHandle, blendMode.getValue());
	}

	private native void nativeReadPixels(long handle, float x, float y, float w, float h, ByteBuffer buffer, int position);
	/**
	 * Reads a rect of pixels from the source layer into the provided buffer. <br/>This method should be called on the GL thread.
	 * @param layer the layer to read the pixels from
	 * @param x the x coordinate in the source layer
	 * @param y the y coordinate in the source layer
	 * @param w the width of the rect to read
	 * @param h the height of the rect to read
	 * @param buffer the destination buffer to write the pixels into
	 * @param position the start position in buffer
	 */
	public void readPixels(Layer layer, float x, float y, float w, float h, ByteBuffer buffer, int position){
		setTarget(layer);
		nativeReadPixels(handle, x, y, w, h, buffer, position);
	}

	private native void nativeWritePixels(long handle, float x, float y, float w, float h, ByteBuffer buffer, int position);
	/**
	 * Writes a rect of pixels from the source buffer into the provided layer. <br/>This method should be called on the GL thread.
	 * @param layer the layer to write the pixels into
	 * @param x the x coordinate in the layer
	 * @param y the y coordinate in the layer
	 * @param w the width of the rect to write
	 * @param h the height of the rect to write
	 * @param buffer the source buffer to read the pixels from
	 * @param position the start position in buffer
	 */
	public void writePixels(Layer layer, float x, float y, float w, float h, ByteBuffer buffer, int position){
		setTarget(layer);
		nativeWritePixels(handle, x, y, w, h, buffer, position);
	}
	
	/**
	 * Load an android bitmap object as an OpenGL texture and binds it to the layer. <br/>This method should be called on the GL thread.
	 * 
	 * @param layer The layer to load the bitmap into.
	 * @param bitmap An android bitmap instance.
	 * @param sampleMode A value to be used as GLES20#GL_TEXTURE_MIN_FILTER and GLES20#GL_TEXTURE_MAG_FILTER texture parameter.
	 * @param wrapMode A value to be used as GLES20#GL_TEXTURE_WRAP_S and @link GLES20#GL_TEXTURE_WRAP_T texture parameter.
	 * @param wrapMode 
	 */
	public void loadBitmap(Layer layer, Bitmap bitmap, int sampleMode, int wrapMode){
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, layer.getColorTexture());

		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, wrapMode);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, wrapMode);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, sampleMode);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, sampleMode);

		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

		if (GLES20.glGetError()>0){
			Utils.alertAndAssert("loadBitmap failed!");
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
}
