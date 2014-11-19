/*
 * Created by Zahari Pastarmadjiev.
 * Copyright (c) 2013 Wacom. All rights reserved.
 */

package com.wacom.ink.rendering;

import java.lang.ref.WeakReference;

import android.graphics.SurfaceTexture;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

import com.wacom.ink.utils.Logger;

/**
 * The RenderingSurfaceHandler is responsible for maintaining the rendering surface. 
 * This abstract class accepts a {@link SurfaceView} or a {@link TextureView}, responds to its lifecycle and provides abstract methods, clients should implement, to receive information about surface changes.
 * 
 * <p/>The {@link RenderingSurfaceHandler} holds a {@link RenderingContext} instance and is responsible for observing the surface lifecycle and maintaining the provided rendering context.
 * <p/>The following abstract methods should be implemented:
 * <br/> {@link RenderingSurfaceHandler#onRenderingSurfaceCreate(Object, int, int) onRenderingSurfaceCreate(Object, int, int)}
 * <br/> {@link RenderingSurfaceHandler#onRenderingSurfaceInitialize(int, int) onRenderingSurfaceInitialize(int, int)}
 * <br/> {@link RenderingSurfaceHandler#onRenderingSurfaceUpdate(int, int) onRenderingSurfaceUpdate(int, int)}
 * <br/> {@link RenderingSurfaceHandler#onRenderingSurfaceDestroy() onRenderingSurfaceDestroy()}
 */
public abstract class RenderingSurfaceHandler{
	private final static Logger logger = new Logger(RenderingSurfaceHandler.class);

	private boolean bSurfaceInitialized;

	private int width;
	private int height;

	private WeakReference<View> surfaceViewRef;
	private RenderingContext renderingContext;
	
	private SurfaceHolder.Callback surfaceHolderCallback;
	private TextureView.SurfaceTextureListener surfaceTextureListener;
	
	private Object currentNativeWindow;
	
	/**
	 * Constructs a surface handler for the given view and rendering context.
	 * @param view A view providing the rendering surface.
	 * @param renderingContext A rendering context holding the EGL/OpenGl state.
	 */
	public RenderingSurfaceHandler(View view, RenderingContext renderingContext){
		surfaceViewRef = new WeakReference<View>(view);
		this.renderingContext = renderingContext;
		if (view instanceof SurfaceView){
			if (Logger.LOG_ENABLED) logger.i("RenderingSurfaceCallback: using SurfaceView");
			initSurfaceHolderCallback();
		} else if (view instanceof TextureView){
			if (Logger.LOG_ENABLED) logger.i("RenderingSurfaceCallback: using TextureView");
			initSurfaceTextureListener();
		} else {
			if (Logger.LOG_ENABLED) logger.i("RenderingSurfaceCallback: no surface, given " + view.getClass().getSimpleName());
		}
	}

	public void destroyEGLSurface(){
		renderingContext.unbindEGLContext();
		renderingContext.destroyEGLSurface();
	}
	
	public void initializeEGLSurface(Object nativeWindow, int width, int height){
		renderingContext.initContext();
		boolean bRebindContext = currentNativeWindow!=nativeWindow && currentNativeWindow!=null;
		if (bRebindContext){
			if (Logger.LOG_ENABLED) logger.i("initializeSurface / rebind context");
			renderingContext.unbindEGLContext();
			renderingContext.destroyEGLSurface();
		} else {
			if (Logger.LOG_ENABLED) logger.i("initializeSurface");			
		}

		renderingContext.createEGLSurface(nativeWindow);
		renderingContext.bindEGLContext();

		RenderingSurfaceHandler.this.width = width;
		RenderingSurfaceHandler.this.height = height;
		
		currentNativeWindow = nativeWindow;
	}
	
	public void initializeSurface(Object nativeWindow, int width, int height){
		boolean bSurfaceAccepted = onRenderingSurfaceCreate(nativeWindow, width, height);
		if (bSurfaceAccepted){
			if (!bSurfaceInitialized) {
				if (Logger.LOG_ENABLED) logger.i("initialize<start>" + " TID:" + Thread.currentThread().getId());
				bSurfaceInitialized = true;	
				onRenderingSurfaceInitialize(width, height);
				if (Logger.LOG_ENABLED) logger.i("initialize<end>" + " TID:" + Thread.currentThread().getId());
			} else {
				if (Logger.LOG_ENABLED) logger.i("initialize -> already initialized" + " TID:" + Thread.currentThread().getId());
				onRenderingSurfaceUpdate(width, height);
			}
		}
	}
	
	private void initSurfaceTextureListener() {
		surfaceTextureListener = new TextureView.SurfaceTextureListener(){
			
			@Override
			public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
				if (Logger.LOG_ENABLED) logger.i("onSurfaceTextureAvailable <start>: " + surface + " " + width + "," + height + " TID:" + Thread.currentThread().getId());
				initializeSurface(surface, width, height);
				renderingContext.printInfo();
				if (Logger.LOG_ENABLED) logger.i("onSurfaceTextureAvailable <end>: " + surface + " " + width + "," + height + " TID:" + Thread.currentThread().getId());
			}

			@Override
			public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
				if (Logger.LOG_ENABLED) logger.i("onSurfaceTextureDestroyed: " + surface + " TID:" + Thread.currentThread().getId());
				onRenderingSurfaceDestroy();
				return true;
			}

			@Override
			public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
				if (Logger.LOG_ENABLED) logger.i("onSurfaceTextureSizeChanged: " + surface + " " + width + "," + height + " TID:" + Thread.currentThread().getId());
			}

			@Override
			public void onSurfaceTextureUpdated(SurfaceTexture surface) {
				if (Logger.LOG_ENABLED) logger.i("onSurfaceTextureUpdated: " + surface + " TID:" + Thread.currentThread().getId());
			}
		};
	}

	private void initSurfaceHolderCallback(){
		surfaceHolderCallback = new SurfaceHolder.Callback(){
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
				if (Logger.LOG_ENABLED) logger.i("surfaceChanged TID:" + Thread.currentThread().getId());
				initializeSurface(holder, width, height);
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				if (Logger.LOG_ENABLED) logger.i("surfaceCreated TID:" + Thread.currentThread().getId());
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				if (Logger.LOG_ENABLED) logger.i("surfaceDestroyed TID:" + Thread.currentThread().getId());
				onRenderingSurfaceDestroy();
			}
		};
	}

	public boolean isSurfaceInitialized(){
		return bSurfaceInitialized;
	}

	public int getWidth(){
		return width;
	}

	public int getHeight(){
		return height;
	}

	/**
	 * This is called immediately after an android surface is available. You should at this point create a rendering surface.
	 * @param width of the surface
	 * @param height of the surface
	 */
	protected abstract boolean onRenderingSurfaceCreate(Object nativeWindow, int width, int height);
	
	/**
	 * This is called immediately after successful rendering surface creation. You should at this point do some initialization work.
	 * @param width of the surface
	 * @param height of the surface
	 */
	protected abstract void onRenderingSurfaceInitialize(int width, int height);
	
	/**
	 * This is called immediately after the android surface has been changed. Depending on the implementation, if no reinitialization work is required, normally the canvas should only be updated.
	 * @param width of the surface
	 * @param height of the surface
	 */
	protected abstract void onRenderingSurfaceUpdate(int width, int height);
	
	/**
	 * This is called immediately after destruction of the android surface. You should at this point deinitialize and release resources.
	 * @param width of the surface
	 * @param height of the surface
	 */
	protected abstract void onRenderingSurfaceDestroy();

	public void setup() {
		View view = surfaceViewRef.get();
		if (view instanceof SurfaceView){ 
			if (Logger.LOG_ENABLED) logger.i("RenderingSurfaceCallback: setup surfaceHolderCallback");
			((SurfaceView)view).getHolder().addCallback(surfaceHolderCallback);
		} else if (view instanceof TextureView){
			if (Logger.LOG_ENABLED) logger.i("RenderingSurfaceCallback: setup surfaceTextureListener");
			((TextureView)view).setSurfaceTextureListener(surfaceTextureListener);
		}
	}				
}