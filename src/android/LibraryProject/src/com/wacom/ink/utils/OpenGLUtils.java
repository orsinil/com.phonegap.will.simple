/*
 * Created by Zahari Pastarmadjiev.
 * Copyright (c) 2013 Wacom. All rights reserved.
 */

package com.wacom.ink.utils;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

/**
 * OpenGl utility class.
 * 
 */
public class OpenGLUtils{
	public static Logger logger = new Logger(OpenGLUtils.class);

	/**
	 * Constant: no OpenGl texture
	 */
	public static int NO_TEXTURE_ID = 0;

	/**
	 * Creates an OpenGl texture, filling it with the given bitmap's pixels.
	 * @param bitmap a bitmap object
	 * @param sampleMode a value to be used as {@link GLES20#GL_TEXTURE_MIN_FILTER} and {@link GLES20#GL_TEXTURE_MAG_FILTER} texture parameter
	 * @param wrapMode a value to be used as {@link GLES20#GL_TEXTURE_WRAP_S} and {@link GLES20#GL_TEXTURE_WRAP_T} texture parameter
	 * @return id of the allocated OpenGl texture
	 */
	public static int bitmapToOpenGL(Bitmap bitmap, int sampleMode, int wrapMode){
		return bitmapToOpenGL(bitmap, NO_TEXTURE_ID, sampleMode, wrapMode);
	}

	public static int generateTexture(boolean bBindTexture){
		return generateTexture(NO_TEXTURE_ID, bBindTexture);
	}

	public static int generateTexture(int textureId, boolean bBindTexture){
		int textures[] = new int[1];
		if (textureId==NO_TEXTURE_ID){
			GLES20.glGenTextures(1, textures, 0);
			textureId = textures[0];
		} 
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
		return textureId;
	}

	/**
	 * Creates or overwrites an OpenGl texture, filling it with the given bitmap's pixels.
	 * @param bitmap a bitmap object
	 * @param textureId OpenGl texture id. If this value equals {@link #NO_TEXTURE_ID}, a new OpenGl texture will be created.
	 * @param sampleMode a value to be used as {@link GLES20#GL_TEXTURE_MIN_FILTER} and {@link GLES20#GL_TEXTURE_MAG_FILTER} texture parameter
	 * @param wrapMode a value to be used as {@link GLES20#GL_TEXTURE_WRAP_S} and {@link GLES20#GL_TEXTURE_WRAP_T} texture parameter
	 * @return OpengGl texture id
	 */
	public static int bitmapToOpenGL(Bitmap bitmap, int textureId, int sampleMode, int wrapMode){
		int textures[] = new int[1];

		if (textureId==NO_TEXTURE_ID){
			GLES20.glGenTextures(1, textures, 0);
			textureId = textures[0];
		} 
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, wrapMode);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, wrapMode);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, sampleMode);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, sampleMode);

		if (Logger.LOG_ENABLED) logger.i("bitmapToOpenGL: " + bitmap.getWidth() + "," + bitmap.getHeight() + " | " + wrapMode + "," + sampleMode);

		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

		if (GLES20.glGetError()>0){
			Utils.alertAndAssert("bitmapToOpenGL failed!");
		}

		return textureId;
	}

	/**
	 * Loads a bitmap from the given filename and location into the provided OpenGl texture and generates mipmaps for it.
	 * This method searches for additional image files in the provided location folder for the different mipmap levels.
	 * For example: if the filename is image.png, the method will look for a image_i.png file for the i-th mipmap level.
	 * @param context An Android context.
	 * @param filename The image filename.
	 * @param location The folder, where the image is located. If location is null, the method expects the file to be located in the assets folder of the application.
	 * @param textureId A value to be used as {@link GLES20#GL_TEXTURE_MIN_FILTER} and {@link GLES20#GL_TEXTURE_MAG_FILTER} texture parameter.
	 * @param sampleMode A value to be used as {@link GLES20#GL_TEXTURE_MIN_FILTER} and {@link GLES20#GL_TEXTURE_MAG_FILTER} texture parameter.
	 * @param wrapMode A value to be used as {@link GLES20#GL_TEXTURE_WRAP_S} and {@link GLES20#GL_TEXTURE_WRAP_T} texture parameter.
	 * @return OpengGl texture id.
	 */
	public static boolean generateMipmaps(Context context, String filename, String location, int textureId, int sampleMode, int wrapMode) {
		InputStream is = null;
		String res[] = Utils.splitFilenameAndExt(filename);
		int errCode;
		  
		if (res==null){
			//can't split filename, that's odd
			return false;
		}
		
		String fn = res[0];
		String ext = res[1];
		
		if (Logger.LOG_ENABLED) logger.i("generateMipmaps / bind texture " + textureId);
		
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, wrapMode);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, wrapMode);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, sampleMode);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
		
		Bitmap bitmap = null;
		Bitmap baseBitmap = null;
		boolean bMipmapping = true;
		int level = 0;
		int size = 0;
		while (bMipmapping) {
			String f = null;
			try {
				if (level==0){
					f = filename;
				} else {
					f = fn + "_" + level + "." + ext;
				}
//				is = context.getAssets().open(f);
				is = Utils.openFile(context, f, location);
				bitmap = BitmapFactory.decodeStream(is);
				if (bitmap != null) {
					baseBitmap = bitmap;
					size = baseBitmap.getWidth();
				}
			} catch (IOException e) {
				if (Logger.LOG_ENABLED) logger.i("generateMipmaps /  error opening asset " + f);
				bitmap = null;
			} finally { 
				try { is.close(); }  
				catch (Exception e) { }
			}
			if (bitmap==null){
				size = size / 2;
				if (Logger.LOG_ENABLED) logger.i("generateMipmaps / no bitmap allocated for " + filename + ", level=" + level + "; downscaling initial bitmap to " + size + "x" + size);
				bitmap = Bitmap.createScaledBitmap(baseBitmap, size, size, true);
				if (bitmap==baseBitmap){
					//if this ever happens, it seems like android sdk bug (1); maybe we should throw an exception here
					if (Logger.LOG_ENABLED) logger.i("generateMipmaps / if this ever happens, it seems like android sdk bug (1); maybe we should throw an exception here");
					return false;
				}
				if (bitmap==null){
					//if this ever happens, it seems like android sdk bug (2); maybe we should throw an exception here
					if (Logger.LOG_ENABLED) logger.i("generateMipmaps / if this ever happens, it seems like android sdk bug (2); maybe we should throw an exception here");
					return false;
				}
			}
			if (Logger.LOG_ENABLED) logger.i("generateMipmaps / texImage2D => fn=" + filename + " level=" + level + " / size=" + bitmap.getWidth() + "x" + bitmap.getHeight());


			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, level, bitmap, 0);
			errCode = GLES20.glGetError();
			if (errCode!=GLES20.GL_NO_ERROR){
				if (Logger.LOG_ENABLED) logger.e("generateMipmaps / ERROR " + errCode+ " with texImage2D => fn=" + filename + " level=" + level);
			}
			if (level>0){
				bitmap = Utils.recycleBitmap(bitmap);
			}
			level++;
			bMipmapping = size>1;
		}
		
		Utils.recycleBitmap(baseBitmap);
		
		return true;
	}
	
	public static void checkError(String errorMessageOpt){
		EGL10 egl = (EGL10)EGLContext.getEGL();
		int e = GLES20.glGetError();
		
		if (e!=GLES20.GL_NO_ERROR){
			if (Logger.LOG_ENABLED) logger.e("OpenGL error" + (errorMessageOpt!=null?(" ("+errorMessageOpt+")"):"") + ": " + e);
		}
		
		e = egl.eglGetError();
		if (e!=EGL10.EGL_SUCCESS){
			if (Logger.LOG_ENABLED) logger.e("EGL error" + (errorMessageOpt!=null?(" ("+errorMessageOpt+")"):"") + ": " + e);
		}
	}
	
	public static String getGPUInfo(){
		String info = "";
		
		info += "GPUINFO <start>"; 
		info += "GPUINFO | vendor: " + GLES20.glGetString(GLES20.GL_VENDOR) + "\n";
		info += "GPUINFO | renderer: " + GLES20.glGetString(GLES20.GL_RENDERER) + "\n";
		info += "GPUINFO | verison: " + GLES20.glGetString(GLES20.GL_VERSION) + "\n";
		info += "GPUINFO | extensions: " + GLES20.glGetString(GLES20.GL_EXTENSIONS) + "\n";
		info += "GPUINFO <end>"; 
		
		return info;
	}
	
	public static int getFramebufferBinding(){
//	glGetIntegerv(GL_FRAMEBUFFER_BINDING, &defaultFBO);
		int params[] = new int[1];
		GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, params, 0);
		//return params[0];
		return 0;
	}
}