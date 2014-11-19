/*
 * Created by Zahari Pastarmadjiev.
 * Copyright (c) 2014 Wacom. All rights reserved.
 */

package com.wacom.ink.rasterization;

import com.wacom.ink.utils.Logger;


/**
 * This class represents a storage for graphics(pixels) that can be updated, drawn to other layers or presented on the screen. 
 * It could represent the screen, a texture or an offscreen render buffer.
 * The layer is associated with an OpenGL frame buffer, texture or a render buffer. 
 * The layer has a scaleFactor that defines the ratio between the abstract layer dimensions and the actual size in pixels. 
 * Layers are being drawn to an {@link InkCanvas} instances.
 */
public class Layer {
	private final static Logger logger = new Logger(Layer.class, true);
	/**
	 * @hide
	 */
	public long handle;
	
	private native long nativeInitialize();
	private native void nativeFinalize(long handle);

	private native void nativeInit(long rasterizerHandle, long handle, int width, int height, float scaleFactor, boolean bUseTextureStorage);
	private native void nativeInitWithFramebuffer(long rasterizerHandle, long handle, int width, int height, float scaleFactor, int framebufferId);
	
	private native boolean nativeIsUsingTextureStorage(long handle);
	private native boolean nativeHasOwnGLResources(long handle);
	
	private native int nativeGetColorTexture(long handle);
	private native int nativeGetFrameBuffer(long handle);
	private native int nativeGetRenderBuffer(long handle);
	
	public Layer(String name){
		this();
	}
	
	/**
	 * Constructs an empty layer.
	 */
	public Layer(){
		handle = nativeInitialize();
	}

	private native int nativeGetWidth(long handle);
	/**
	 * Returns the width of the layer.
	 * @return
	 */
	public int getWidth(){
		return nativeGetWidth(handle);
	}

	private native int nativeGetHeight(long handle);
	/**
	 * Returns the height of the layer.
	 * @return
	 */
	public int getHeight(){
		return nativeGetHeight(handle);
	}

	
	private native int nativeGetStorageWidth(long handle);
	/**
	 * Returns the width of the layer.
	 * @return
	 */
	public int getStorageWidth(){
		return nativeGetStorageWidth(handle);
	}

	private native int nativeGetStorageHeight(long handle);
	/**
	 * Returns the height of the layer.
	 * @return
	 */
	public int getStorageHeight(){
		return nativeGetStorageHeight(handle);
	}
	
	private native float nativeGetScaleFactor(long handle);
	/**
	 * Returns the scale factor of the layer.
	 * @return
	 */
	public float getScaleFactor(){
		return nativeGetScaleFactor(handle);
	}

	private native boolean nativeGetFlipY(long handle);
	/**
	 * Gets the flipping among y-axis.
	 * @return
	 */
	public boolean getFlipY(){
		return nativeGetFlipY(handle);
	}
	
	private native void nativeSetFlipY(long handle, boolean bFlipY);
	/**
	 * Sets the flipping among y-axis.
	 * @return
	 */
	public void setFlipY(boolean bFlipY){
		nativeSetFlipY(handle, bFlipY);
	}
	
	/**
	 * Indicates if the layer should allocate and maintain texture storage.
	 * @return
	 */
	public boolean isUsingTextureStorage(){
		return nativeIsUsingTextureStorage(handle);
	}

	/**
	 * Indicates if the layer owns the OpenGL resources it uses.
	 * @return
	 */
	public boolean hasOwnGLResources(){
		return nativeHasOwnGLResources(handle);
	}

	/**
	 * Returns the id of the OpenGL texture bound to this layer.
	 * @return
	 */
	public int getColorTexture(){
		return nativeGetColorTexture(handle);
	}

	/**
	 * Returns the id of the OpenGL framebuffer bound to this layer.
	 * @return
	 */
	public int getFrameBuffer(){
		return nativeGetFrameBuffer(handle);
	}

	/**
	 * Returns the id of the OpenGl renderbuffer bound to this layer.
	 * @return
	 */
	public int getRenderBuffer(){
		return nativeGetRenderBuffer(handle);
	}

	/**
	 * Initializes a layer with width, height, scale factor.
	 * @param width The layer's width.
	 * @param height The layer's height.
	 * @param scaleFactor The scale factor of the layer. It defines the ratio between the abstract layer dimensions and the actual underlying pixels.
	 * @param bUseTextureStorage If true, when the layer is deallocated, the OpenGL resources (framebufferId, textureId, renderbufferId) will be deleted. If NO, they will be not.
	 */
	public void init(InkCanvas canvas, int width, int height, float scaleFactor, boolean bUseTextureStorage){
		nativeInit(canvas.handle, handle, width, height, scaleFactor, bUseTextureStorage);
	}
	
	/**
	 * Initializes a layer with width, height, scale factor and a framebuffer identifier. 
	 * @param width The layer's width.
	 * @param height The layer's height.
	 * @param scaleFactor The scale factor of the layer. It defines the ratio between the abstract layer dimensions and the actual underlying pixels.
	 * @param framebufferId A frameBuffer identifier. If frameBuffer is set to 0, the layer could NOT be a target to a draw operation.
	 */
	public void initWithFramebuffer(InkCanvas canvas, int width, int height, float scaleFactor, int framebufferId){
		if (Logger.LOG_ENABLED) logger.i("initWithFramebuffer: " + width + "," + height + "," + scaleFactor + "," + framebufferId);
		nativeInitWithFramebuffer(canvas.handle, handle, width, height, scaleFactor, framebufferId);
	}
	
	/**
	 * Initializes the layer with width, height, scale factor and framebuffer id 0. 
	 * @param width The layer's width.
	 * @param height The layer's height.
	 * @param scaleFactor The scale factor of the layer. It defines the ratio between the abstract layer dimensions and the actual underlying pixels.
	 */
	public void initWithFramebuffer(InkCanvas canvas, int width, int height, float scaleFactor){
		nativeInitWithFramebuffer(canvas.handle, handle, width, height, scaleFactor, 0);
	}
	
	@Override
	protected void finalize() throws Throwable {
		nativeFinalize(handle);
	}
	
	static { 
		System.loadLibrary("InkingEngine");
	}
}
