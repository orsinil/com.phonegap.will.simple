/*
 * Created by Zahari Pastarmadjiev.
 * Copyright (c) 2013 Wacom. All rights reserved.
 */

package com.wacom.ink.rendering;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import android.opengl.GLUtils;
import android.util.SparseArray;

import com.wacom.ink.utils.Logger;
import com.wacom.ink.utils.Utils;

/**
 * The RenderingContext class wraps all necessary EGL primitives into one rendering context. 
 * 
 */
public class RenderingContext{
	private final static Logger logger = new Logger(RenderingContext.class, true);

	private static final int EGL_OPENGL_ES2_BIT = 4;
	private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
	private static final int EGL_CONTEXT_CLIENT_VERSION_20 = 0x02;

	private static final int SURFACE_STATE_UPDATED = 0;
	private static final int SURFACE_STATE_SUCCESS = 1;
	
	private static final int NO_THREAD_ID = -1;
	private static SparseArray<String> EGL_ATTRIBS = new SparseArray<String>();

	private EGL10 egl;
	private EGLDisplay display;
	private EGLConfigSpecification eglConfigSpec;
	private EGLConfig config;	
	private EGLSurface surface;
	private EGLContext context;
	private long threadId;

	/**
	 * Constructs a rendering context, which will query a suitable EGL config on initialization based on the provided {@link EGLConfigSpecification}.
	 * @param eglConfigSpec
	 */
	public RenderingContext(EGLConfigSpecification eglConfigSpec){
		this.eglConfigSpec = eglConfigSpec;
		threadId = NO_THREAD_ID;
	}

	public EGLConfigSpecification getEglConfigSpec(){
		return eglConfigSpec;
	}

	/**
	 * Initializes EGL, gets the EGL display, chooses an EGL config and creates both the EGL surface and EGL context.
	 * @param width of the surface
	 * @param height of the surface
	 */
	public void initContext(){
		if (Logger.LOG_ENABLED) logger.i("initContext TID:" + Thread.currentThread().getId());

		if (!hasEGLDisplay()){
			createEGLDisplay();
		}

		if (!hasEGLConfig()){
			chooseEGLConfig(eglConfigSpec);
		}

		if (!hasEGLContext()){
			createEGLContext();
		}

		if (Logger.LOG_ENABLED) logger.i("initEGL <after> / initEGL.egl=" + egl);
		if (Logger.LOG_ENABLED) logger.i("initEGL <after> / initEGL.display=" + display);
		if (Logger.LOG_ENABLED) logger.i("initEGL <after> / initEGL.config=" + config);
		if (Logger.LOG_ENABLED) logger.i("initEGL <after> / initEGL.context=" + context);
	}

	/**
	 * Destroys the context. Destroys the EGL context, EGL surface and terminates the EGL. After this call, the application can be safely terminated.
	 */
	public void destroyContext(){
		if (Logger.LOG_ENABLED) logger.i("destroyContext TID:" + Thread.currentThread().getId());
		unbindEGLContext();
		destroyEGLSurface();
		destroyEGLContext();
		destroyEGL();
		display = null;
		surface = null;
		context = null;
	}

	public boolean createEGLDisplay(){
		if (Logger.LOG_ENABLED) logger.i("createEGLDisplay TID:" + Thread.currentThread().getId());
		EGL10 egl = getEGL();
		if (!hasEGLDisplay()){
			display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
			if (display == EGL10.EGL_NO_DISPLAY) {
				throw new RuntimeException("eglGetDisplay failed");
			}
			int[] version = new int[2];
			if (!egl.eglInitialize(display, version)) {
				throw new RuntimeException("eglInitialize failed");
			}
			if (Logger.LOG_ENABLED) logger.i("initializeEGL = " + display);
			int error = egl.eglGetError();
			if (error!=EGL10.EGL_SUCCESS){ 
				if (Logger.LOG_ENABLED) logger.i("createEGLDisplay failed; egl error=" + error);
				return false;
			} else {
				if (Logger.LOG_ENABLED) logger.i("createEGLDisplay success; display=" + display);
				return true;
			}
		} else {
			if (Logger.LOG_ENABLED) logger.i("createEGLDisplay = already created");
			return false;
		}
	}

	public boolean createEGLContext() {
		if (Logger.LOG_ENABLED) logger.i("createEGLContext TID:" + Thread.currentThread().getId());
		EGL10 egl = getEGL();
		if (hasEGLContext()){
			if (Logger.LOG_ENABLED) logger.i("createEGLContext = already created");
			return false;
		}
		int[] attribList = {EGL_CONTEXT_CLIENT_VERSION, EGL_CONTEXT_CLIENT_VERSION_20, EGL10.EGL_NONE };
		context = egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, attribList);
		if (context==null || context==EGL10.EGL_NO_CONTEXT) {
			int error = egl.eglGetError();
			if (error!=EGL10.EGL_SUCCESS){ 
				if (Logger.LOG_ENABLED) logger.i("createEGLContext failed; egl error=" + error);
			} else {
				if (Logger.LOG_ENABLED) logger.i("createEGLContext failed");
			}
			return false;
		} else {
			if (Logger.LOG_ENABLED) logger.i("createEGLContext = " + context);
			return true;
		}
	}

	public boolean createEGLSurface(Object nativeWindow){
		if (Logger.LOG_ENABLED) logger.i("createEGLSurface TID:" + Thread.currentThread().getId());
		EGL10 egl = getEGL();
		if (hasEGLSurface()){
			if (Logger.LOG_ENABLED) logger.e("createEGLSurface = already created");
			return false;
		}
		surface = egl.eglCreateWindowSurface(display, config, nativeWindow, null);
		if (surface==null || surface==EGL10.EGL_NO_SURFACE) {
			int error = egl.eglGetError();
			if (error!=EGL10.EGL_SUCCESS){ 
				if (Logger.LOG_ENABLED) logger.i("createEGLSurface failed; egl error=" + error);
			} else {
				if (Logger.LOG_ENABLED) logger.i("createEGLSurface failed");
			}
			return false;
		} else {
			if (Logger.LOG_ENABLED) logger.i("createEGLSurface = " + surface);
			return true;
		}
	}

	public boolean createEGLPbufferSurface(){
		if (Logger.LOG_ENABLED) logger.i("createEGLPbufferSurface TID:" + Thread.currentThread().getId());
		EGL10 egl = getEGL();
		if (hasEGLSurface()){
			if (Logger.LOG_ENABLED) logger.e("createEGLSurface = already created");
			return false;
		}
		int pbufferAttribs[] =  { 
				EGL10.EGL_WIDTH, 1,
				EGL10.EGL_HEIGHT, 1,
				EGL10.EGL_NONE
		};
		surface = egl.eglCreatePbufferSurface(display, config, pbufferAttribs);
		if (Logger.LOG_ENABLED) logger.i("createEGLSurface = " + surface);

		return true;
	}

	public boolean destroyEGLSurface(){
		if (Logger.LOG_ENABLED) logger.i("destroyEGLSurface TID:" + Thread.currentThread().getId());
		EGL10 egl = getEGL();
		if (hasEGLSurface() && hasEGLDisplay()){
			if (Logger.LOG_ENABLED) logger.i("destroyEGLSurface = " + surface);
			boolean bResult = egl.eglDestroySurface(display, surface);
			surface = null;
			return bResult;
		} else {
			if (Logger.LOG_ENABLED) logger.i("destroyEGLSurface <skip> / egl=" + egl + " | display=" + display + " | surface=" + surface);
			return false;
		}
	}

	public boolean destroyEGLContext(){
		if (Logger.LOG_ENABLED) logger.i("destroyEGLContext TID:" + Thread.currentThread().getId());
		EGL10 egl = getEGL();
		if (hasEGLContext() && hasEGLDisplay()){
			if (Logger.LOG_ENABLED) logger.i("destroyEGLContext = " + context);
			boolean bResult = egl.eglDestroyContext(display, context);
			context = null;
			return bResult;
		} else {
			if (Logger.LOG_ENABLED) logger.i("destroyEGLContext <skip> / egl=" + egl + " | display=" + display + " | context=" + context);
			return false;
		}
	}

	public boolean destroyEGL() {
		if (Logger.LOG_ENABLED) logger.i("destroyEGL TID:" + Thread.currentThread().getId());
		EGL10 egl = getEGL();
		if (hasEGLDisplay()){
			if (Logger.LOG_ENABLED) logger.i("terminateEGL = " + display);
			return egl.eglTerminate(display);
		} else {
			if (Logger.LOG_ENABLED) logger.i("terminateEGL <skip> / egl=" + egl + " | display=" + display);
			return false;
		}
	}

	/**
	 * Search for a suitable EGL config based on the {@link #getEglConfigSpec()}
	 */
	public void chooseEGLConfig(EGLConfigSpecification eglConfigSpec) {
		if (Logger.LOG_ENABLED) logger.i("chooseEGLConfig TID:" + Thread.currentThread().getId());
		EGL10 egl = getEGL();
		int[] num_config = new int[1];

		if (!egl.eglChooseConfig(display, eglConfigSpec.getAttribList(), null, 0, num_config)) {
			throw new RuntimeException("eglChooseConfig failed");
		}

		int numConfigs = num_config[0];

		if (numConfigs <= 0) {
			throw new RuntimeException( "No configs match configSpec");
		}

		EGLConfig[] configs = new EGLConfig[numConfigs];

		if (!egl.eglChooseConfig(display, eglConfigSpec.getAttribList(), configs, numConfigs, num_config)) {
			throw new RuntimeException("eglChooseConfig#2 failed");
		}

		EGLConfig config = chooseConfig(egl, display, configs, eglConfigSpec);

		if (config == null) {
			throw new RuntimeException("No config chosen");
		}

		this.config = config;
	}

	private static EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs, EGLConfigSpecification eglConfigSpec) {
		for (EGLConfig config : configs) {
			int d = findConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0);
			int s = findConfigAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0);
			int r = findConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0);
			int g = findConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0);
			int b = findConfigAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0);
			int a = findConfigAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0);

			if (d>0 && d<eglConfigSpec.eglDepthSize){
				continue;
			}
			if (s>0 && s<eglConfigSpec.eglStencilSize){
				continue;
			}
			if (a>0 && a<eglConfigSpec.eglAlphaSize){
				continue;
			}
			if (r>0 && r<eglConfigSpec.eglRedSize){
				continue;
			}
			if (g>0 && g<eglConfigSpec.eglGreenSize){
				continue;
			}
			if (b>0 && b<eglConfigSpec.eglBlueSize){
				continue;
			}
			if (Logger.LOG_ENABLED) logger.i("chooseConfig = " + config);
			dumpConfig(egl, display, config);
			return config;
		}
		if (Logger.LOG_ENABLED) logger.i("chooseConfig = null");
		return null;
	}

	private static void dumpConfig(EGL10 egl, EGLDisplay display, EGLConfig config){
		if (Logger.LOG_ENABLED) logger.d("EGL_DUMP: Dumping eglConfig <start>");
		for (int i=0;i<EGL_ATTRIBS.size();i++){
			int attr = EGL_ATTRIBS.keyAt(i);
			String name = EGL_ATTRIBS.get(attr);
			int value = findConfigAttrib(egl, display, config, attr, 0);
			if (Logger.LOG_ENABLED) logger.d("EGL_DUMP: attr " + name + "[" + attr + "]: " + value);
		}
		if (Logger.LOG_ENABLED) logger.d("EGL_DUMP: Dumping eglConfig <end>");
	}

	private static int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue){
		int mValue[] = new int[1];

		if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
			return mValue[0];
		}
		return defaultValue;
	}

	private static Utils.TickTock swapTickTock = new Utils.TickTock("ticktock swap", 100);

	public void swap(){
		swap(true);
	}

	/**
	 * Calls {@link EGL10#eglSwapBuffers(EGLDisplay, EGLSurface)} for the current rendering context. Invoke this method from the thread owning the context!
	 */
	public void swap(boolean bCheckContext){
		if (Logger.LOG_ENABLED) logger.i("swap TID:" + Thread.currentThread().getId());
		EGL10 egl = getEGL();
		if (hasEGLDisplay() && hasEGLSurface()){
			if (bCheckContext && !isContextBoundToCurrentThread()){
				if (Logger.LOG_ENABLED) logger.e("swap / check context: context not bound or bound to another thread");
				if (Logger.LOG_ENABLED) logger.e("swap / check context: bound thread=" + threadId + " / running thread=" + Thread.currentThread().getId());
				return;
			}
//			int state = checkCurrent();
//			if (Logger.LOG_ENABLED) logger.d("surface state: " + state);
			swapTickTock.tick();
			if (!egl.eglSwapBuffers(display, surface)) {
				int error = egl.eglGetError();
				if (Logger.LOG_ENABLED) logger.e("swap error: " + error);
				printInfo();
				throw new RuntimeException("swap error: " + error);
			}
			swapTickTock.tock();
		} else {
			if (Logger.LOG_ENABLED) logger.e("swap / no display or surface");
		}
	}

	/**
	 * Binds the EGL context to the current thread and to the EGL surface. Invoke this method from the thread you want to use for rendering!
	 */
	public boolean bindEGLContext(){
		if (Logger.LOG_ENABLED) logger.i("bindEGLContext TID:" + Thread.currentThread().getId());
		EGL10 egl = getEGL();
		if (!isContextInUse()){
			threadId = Thread.currentThread().getId();
			if (!egl.eglMakeCurrent(display, surface, surface, context)){
				throw new RuntimeException("bindEGLContext failed");
			} else {
				if (Logger.LOG_ENABLED) logger.i("bindEGLContext success");
			}
			return true;
		} else {
			if (Logger.LOG_ENABLED) logger.e("can't bind context for thread [" + Thread.currentThread().getId() + "], it's already in use by thread [" + threadId + "]");
			return false;
		}
	}

	public boolean isContextInUse(){
		return threadId!=NO_THREAD_ID;
	}

	public boolean isContextBoundToCurrentThread(){
		return threadId==Thread.currentThread().getId();
	}

	/**
	 * Releases the current context. Invoke this method from the thread owning the context!
	 */
	public boolean unbindEGLContext(){
		if (Logger.LOG_ENABLED) logger.i("unbindEGLContext TID:" + Thread.currentThread().getId());
		EGL10 egl = getEGL();
		if (isContextBoundToCurrentThread()){
			if (!egl.eglMakeCurrent(display, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT)){
				throw new RuntimeException("unbindEGLContext failed");
			} else {
				threadId = NO_THREAD_ID;
				if (Logger.LOG_ENABLED) logger.i("unbindEGLContext");
				return true;
			}
		} else {
			return false;
		}
	}

	//	public static int EGL_MIN_SWAP_INTERVAL = 0x303B;
	//	public static int EGL_MAX_SWAP_INTERVAL = 0x303C;
	//	public static int EGL_BIND_TO_TEXTURE_RGB = 0x3039;
	//	public static int EGL_BIND_TO_TEXTURE_RGBA = 0x303A;
	//	public static int EGL_NATIVE_VISUAL_TYPE = 0x302F;

	static {
		EGL_ATTRIBS.clear();
		EGL_ATTRIBS.put(EGL10.EGL_BUFFER_SIZE, "EGL_BUFFER_SIZE");
		EGL_ATTRIBS.put(EGL10.EGL_RED_SIZE, "EGL_RED_SIZE");
		EGL_ATTRIBS.put(EGL10.EGL_GREEN_SIZE, "EGL_GREEN_SIZE");
		EGL_ATTRIBS.put(EGL10.EGL_BLUE_SIZE, "EGL_BLUE_SIZE");
		EGL_ATTRIBS.put(EGL10.EGL_ALPHA_SIZE, "EGL_ALPHA_SIZE");
		EGL_ATTRIBS.put(EGL10.EGL_DEPTH_SIZE, "EGL_DEPTH_SIZE");
		EGL_ATTRIBS.put(EGL10.EGL_STENCIL_SIZE, "EGL_STENCIL_SIZE");
		EGL_ATTRIBS.put(EGL10.EGL_CONFIG_ID, "EGL_CONFIG_ID");
		EGL_ATTRIBS.put(EGL10.EGL_LEVEL, "EGL_LEVEL");
		EGL_ATTRIBS.put(EGL10.EGL_MAX_PBUFFER_WIDTH, "EGL_MAX_PBUFFER_WIDTH");
		EGL_ATTRIBS.put(EGL10.EGL_MAX_PBUFFER_HEIGHT, "EGL_MAX_PBUFFER_HEIGHT");
		EGL_ATTRIBS.put(EGL10.EGL_MAX_PBUFFER_PIXELS, "EGL_MAX_PBUFFER_PIXELS");
		EGL_ATTRIBS.put(EGL10.EGL_NATIVE_RENDERABLE, "EGL_NATIVE_RENDERABLE");
		EGL_ATTRIBS.put(EGL10.EGL_NATIVE_VISUAL_ID, "EGL_NATIVE_VISUAL_ID");
		EGL_ATTRIBS.put(EGL10.EGL_NATIVE_VISUAL_TYPE, "EGL_NATIVE_VISUAL_TYPE");
		EGL_ATTRIBS.put(EGL10.EGL_SAMPLE_BUFFERS, "EGL_SAMPLE_BUFFERS");
		EGL_ATTRIBS.put(EGL10.EGL_SAMPLES, "EGL_SAMPLES");
		EGL_ATTRIBS.put(EGL10.EGL_SURFACE_TYPE, "EGL_SURFACE_TYPE");
		EGL_ATTRIBS.put(EGL10.EGL_CONFIG_CAVEAT, "EGL_CONFIG_CAVEAT");
		EGL_ATTRIBS.put(EGL10.EGL_TRANSPARENT_TYPE, "EGL_TRANSPARENT_TYPE");
		EGL_ATTRIBS.put(EGL10.EGL_TRANSPARENT_RED_VALUE, "EGL_TRANSPARENT_RED_VALUE");
		EGL_ATTRIBS.put(EGL10.EGL_TRANSPARENT_GREEN_VALUE, "EGL_TRANSPARENT_GREEN_VALUE");
		EGL_ATTRIBS.put(EGL10.EGL_TRANSPARENT_BLUE_VALUE, "EGL_TRANSPARENT_BLUE_VALUE");
		//		EGL_ATTRIBS.put(EGL14.EGL_SWAP_BEHAVIOR_PRESERVED_BIT, "EGL_SWAP_BEHAVIOR_PRESERVED_BIT");
		//		EGL_ATTRIBS.put(EGL14.EGL_MATCH_NATIVE_PIXMAP, "EGL_MATCH_NATIVE_PIXMAP");
		//		EGL_ATTRIBS.put(EGL14.EGL_CONFORMANT, "EGL_CONFORMANT");
		//		EGL_ATTRIBS.put(EGL14.EGL_MIN_SWAP_INTERVAL, "EGL_MIN_SWAP_INTERVAL");
		//		EGL_ATTRIBS.put(EGL14.EGL_MAX_SWAP_INTERVAL, "EGL_MAX_SWAP_INTERVAL");
		//		EGL_ATTRIBS.put(EGL14.EGL_BIND_TO_TEXTURE_RGB, "EGL_BIND_TO_TEXTURE_RGB");
		//		EGL_ATTRIBS.put(EGL14.EGL_BIND_TO_TEXTURE_RGBA, "EGL_BIND_TO_TEXTURE_RGBA");
		EGL_ATTRIBS.put(EGL10.EGL_LUMINANCE_SIZE, "EGL_LUMINANCE_SIZE");
		//EGL_ATTRIBS.put(0x00003093, "EGL_SWAP_BEHAVIOR");
	}


	/**
	 * Specifies a list of properties, the EGL config should be checked against.
	 * 
	 * Copyright (c) 2013 Wacom. All rights reserved.
	 */
	public static class EGLConfigSpecification{
		public int eglRedSize;
		public int eglGreenSize;
		public int eglBlueSize;
		public int eglAlphaSize;
		public int eglDepthSize;
		public int eglStencilSize;

		public EGLConfigSpecification(){
			this(0, 0, 0, 0, 0, 0);
		}

		public EGLConfigSpecification(int eglRedSize, int eglGreenSize, int eglBlueSize, int eglAlphaSize, int eglDepthSize, int eglStencilSize){
			this.eglDepthSize = eglDepthSize;
			this.eglStencilSize = eglStencilSize;
			this.eglRedSize = eglRedSize;
			this.eglBlueSize = eglBlueSize;
			this.eglGreenSize = eglGreenSize;
			this.eglAlphaSize = eglAlphaSize;
		}

		public int[] getAttribList(){
			return new int[] {
					EGL10.EGL_SURFACE_TYPE, EGL10.EGL_WINDOW_BIT,
					EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
					EGL10.EGL_RED_SIZE, eglRedSize,
					EGL10.EGL_GREEN_SIZE, eglGreenSize,
					EGL10.EGL_BLUE_SIZE, eglBlueSize,
					EGL10.EGL_ALPHA_SIZE, eglAlphaSize,
					EGL10.EGL_DEPTH_SIZE, eglDepthSize,
					EGL10.EGL_STENCIL_SIZE, eglStencilSize,
					EGL10.EGL_NONE};
		}
	}

	public EGLSurface getEGLSurface(){
		return surface;
	}

	public EGL10 getEGL(){
		if (egl==null){
			egl = (EGL10)EGLContext.getEGL();
		}
		return egl;
	}

	public EGLDisplay getEGLDisplay(){
		return display;
	}

	public EGLContext getEGLContext(){
		return context;
	}

	public EGLConfig getEGLConfig(){
		return config;
	}

	public boolean hasEGLSurface(){
		return surface!=null && surface!=EGL10.EGL_NO_SURFACE;
	}

	public boolean hasEGLDisplay(){
		return display!=null && display!=EGL10.EGL_NO_DISPLAY;
	}

	public boolean hasEGLContext(){
		return context!=null && context!=EGL10.EGL_NO_CONTEXT;
	}

	public boolean hasEGLConfig(){
		return config!=null;
	}

	public void printInfo(){
		EGL10 egl = getEGL();
		if (Logger.LOG_ENABLED) logger.i("eglctx=" + egl.eglGetCurrentContext() + " / working=" + context);
		if (Logger.LOG_ENABLED) logger.i("eglsurf=" + egl.eglGetCurrentSurface(EGL10.EGL_DRAW) + " / working=" + surface);
	}

	public int checkCurrent() {
		if (!isContextBoundToCurrentThread()) {
			throw new RuntimeException("checkCurrent: invalid thread");
		}
		if (!context.equals(egl.eglGetCurrentContext()) || !surface.equals(egl.eglGetCurrentSurface(EGL10.EGL_DRAW))) {
			if (!egl.eglMakeCurrent(display, surface, surface, context)) {
				if (Logger.LOG_ENABLED) logger.e("eglMakeCurrent failed " + GLUtils.getEGLErrorString(egl.eglGetError()));
				throw new RuntimeException("checkCurrent: can't perform eglMakeCurrent");
			} else {
				return SURFACE_STATE_UPDATED;
			}
		}
		return SURFACE_STATE_SUCCESS;
	}
}