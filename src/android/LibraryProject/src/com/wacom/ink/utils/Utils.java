/*
 * Created by Zahari Pastarmadjiev.
 * Copyright (c) 2013 Wacom. All rights reserved.
 */

package com.wacom.ink.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.net.Uri;
import android.os.Debug;
import android.provider.MediaStore;
import android.util.Log;

/**
 * Utility class.
 * 
 */
public class Utils{
	private final static Logger logger = new Logger(Utils.class, true);

	public final static String MIME_PNG 	= "image/png";
	public final static String EXT_PNG 		= "png";
	public final static String MIME_JPEG 	= "image/jpeg";
	public final static String EXT_JPG 		= "jpg";
	
	public final static int COLOR_ARR_ALPHA = 0;
	public final static int COLOR_ARR_RED 	= 1;
	public final static int COLOR_ARR_GREEN = 2;
	public final static int COLOR_ARR_BLUE 	= 3;
	
	public static InputStream openFile(Context context, String filename, String location) throws IOException{
		if (location==null){
			return context.getAssets().open(filename);
		} else {
			return new FileInputStream(new File(location, filename));
		}
	}

	public static float[] colorToArray(int color){
		return colorToArray(color, -1);
	}

	public static float mulIfTrue(float value, float mul, boolean bCondition){
		if (bCondition){
			return value*mul;
		} else {
			return value;
		}
	}

	public static Bitmap cropAndScaleBitmapAtCenterPt(Bitmap bitmap, int width, int height){
		int x, y;

		double sw = bitmap.getWidth();
		double sh = bitmap.getHeight();
		double tw = width;
		double th = height;

		double tr = th/tw;
		double sr = sh/sw;

		double cropW = 0;
		double cropH = 0;

		if (Logger.LOG_ENABLED) logger.i("cropAndScaleBitmapAtCenterPt / src: " + sw + "," + sh);
		if (Logger.LOG_ENABLED) logger.i("cropAndScaleBitmapAtCenterPt / target: " + tw + "," + th);

		if (tr>sr){
			cropW = sh*(1.0/tr);
			cropH = sh;
			if (Logger.LOG_ENABLED) logger.i("cropAndScaleBitmapAtCenterPt / crop width => sr=" + sr + " tr=" + tr);
		} else {
			cropW = sw;
			cropH = sw*tr;
			if (Logger.LOG_ENABLED) logger.i("cropAndScaleBitmapAtCenterPt / crop height => sr=" + sr + " tr=" + tr);
		}

		x = (int) Math.round((sw-cropW)/2);
		y = (int) Math.round((sh-cropH)/2);

		float scaleFactor = (float) (tw/cropW);
		Matrix m = new Matrix();
		m.setScale(scaleFactor, scaleFactor);
		bitmap = Bitmap.createBitmap(bitmap, x, y, (int)Math.round(cropW), (int)Math.round(cropH), m, true);
		if (Logger.LOG_ENABLED) logger.i("cropAndScaleBitmapAtCenterPt / crop | x,y,w,h => " + x + ", " + y + ", " + (int)Math.round(cropW) + ", " + (int)Math.round(cropH));
		if (Logger.LOG_ENABLED) logger.i("cropAndScaleBitmapAtCenterPt / scale | scaleFactor=" + scaleFactor + " => " + (int)Math.round(cropW*scaleFactor) + ", " + (int)Math.round(cropH*scaleFactor));
		
		return bitmap;
	}

	public static float[] colorToArray(int color, float overwriteAlpha){
		float[] array = new float[4];

		array[0] = (overwriteAlpha>=0)?overwriteAlpha:Color.alpha(color)/255.0f;
		array[1] = Color.red(color)/255.0f;
		array[2] = Color.green(color)/255.0f;
		array[3] = Color.blue(color)/255.0f;

		return array;
	}

	public static void alertAndAssert(String assertMessage) {
		if (Logger.LOG_ENABLED) logger.e(assertMessage);
		throw new RuntimeException("ASSERT: " + assertMessage);
	}

	public static Uri insertInMediaStore(ContentResolver contentResolver, File filePath, String imageType){
		Uri uri = null;

		ContentValues values = new ContentValues(2);
		values.put(MediaStore.Images.Media.MIME_TYPE, imageType);
		values.put(MediaStore.Images.Media.DATA, filePath.getAbsolutePath());
		uri = contentResolver.insert(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				values);

		return uri;
	}

	public static void startIntentChooseFile(Activity activity, String mime, int requestCode, String chooserTitle){
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType(mime);
		activity.startActivityForResult(intent, requestCode);
	}

	public static void startIntentSendFile(Context context, String chooserTitle, Uri uri, String mime, String altText){
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_STREAM, uri);
		intent.setType(mime);
		if (altText!=null){
			intent.putExtra(Intent.EXTRA_TEXT, altText);
			intent.putExtra(Intent.EXTRA_SUBJECT, altText);
		}
		context.startActivity(Intent.createChooser(intent, chooserTitle));
	}

	public static void startIntentViewFile(Context context, Uri uri, String mime){
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(uri, mime);
		context.startActivity(intent);
	}

	public static boolean saveJPEG(Bitmap bitmap, String fname){
		return saveImage(bitmap, fname, CompressFormat.JPEG, 100);
	}

	public static boolean saveJPEG(Bitmap bitmap, String fname, int quality){
		return saveImage(bitmap, fname, CompressFormat.JPEG, quality);
	}

	public static boolean savePNG(Bitmap bitmap, String fname){
		return saveImage(bitmap, fname, CompressFormat.PNG, 0);
	}

	public static boolean saveImage(Bitmap bitmap, String fname, Bitmap.CompressFormat fileFormat, int quality){
		FileOutputStream output = null;
		boolean bSuccess = false;
		try {
			output = new FileOutputStream(new File(fname));
			bitmap.compress(fileFormat, quality, output);
			output.close();
			bSuccess = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();	
		} finally {
			if (!bSuccess){
				try{
					output.close();
				} catch (Exception ex){
				}
			}
		}
		return bSuccess;
	}

	public static float transformValue(float value, float minValue, float maxValue, float dstMinValue, float dstMaxValue){
		if (minValue>=maxValue){
			Utils.alertAndAssert("transformValue / minValue exceeds maxValue");
		}
		if (dstMinValue>=dstMaxValue){
			Utils.alertAndAssert("transformValue / dstMinValue exceeds dstMaxValue");
		}
		if (value<minValue || value>maxValue){
			Utils.alertAndAssert("transformValue / value outside required range.");
		}
		float res = dstMinValue + (value-minValue)*(dstMaxValue-dstMinValue)/(maxValue-minValue);
		return res;
	}

	public static class TickTock{
		private long tickTs;
		private long tockTs;
		private float tps;
		private long count;
		private long index;
		private long totalTime;
		private String logString;

		public TickTock(String logString, int count){
			this.count = count;
			this.logString = logString;
		}

		public void tick(){
			tickTs = System.currentTimeMillis();
		}

		public void tock(){
			if (tickTs==0){
				return;
			}
			tockTs = System.currentTimeMillis();
			index++;

			if (tickTs>0){
				totalTime += tockTs-tickTs;
			}

			if (index==count){
				tps = (1000.0f / ((float)totalTime/(float)count));
				if (Logger.LOG_ENABLED) logger.d(logString + " tps: " + tps + " avg: " + ((float)totalTime/count));
				reset();
			}
		}

		public void reset(){
			index = 0;
			tps = 0;
			tockTs = 0;
			tickTs = 0;
			totalTime = 0;
		}
	}

	public static void printMemoryInfo(String tag){
		if (Logger.LOG_ENABLED){
			logger.i(tag + " nativeHeapSize: " + Debug.getNativeHeapSize()/1024.0f + " kb.");
			logger.i(tag + " nativeHeapAllocatedSize: " + Debug.getNativeHeapAllocatedSize()/1024.0f + " kb.");
			logger.i(tag + " nativeHeapFreeSize: " + Debug.getNativeHeapFreeSize()/1024.0f + " kb.");
			logger.i(tag + " freeMemory: " + Runtime.getRuntime().freeMemory()/1024.0f + " kb.");
			logger.i(tag + " totalMemory: " + Runtime.getRuntime().totalMemory()/1024.0f + " kb.");
			logger.i(tag + " maxMemory: " + Runtime.getRuntime().maxMemory()/1024.0f + " kb.");
			logger.i(tag + " availableProcessors: " + Runtime.getRuntime().availableProcessors());
		}
	}

	public static float getNonNan(float v, float alt){
		if (Float.isNaN(v)){
			return alt;
		} else {
			return v;
		}
	}

	public static boolean isPointInsideClosedPath(Path path, float x, float y){
		return isPointInsideClosedPath(path, (int)x, (int)y);
	}
	
	public static boolean isPointInsideClosedPath(Path path, int x, int y){
		RectF rectF = new RectF();
		path.computeBounds(rectF, true);
		Region region = new Region();
		region.setPath(path, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
		//logger.e("isPointInsideClosedPath: " + region.contains(x, y) + "/" + x + "," + y + " / " + rectF.toShortString() + " / " + region.toString());
		return region.contains(x, y);
	}

	public static String[] splitFilenameAndExt(String filename){
		int dotPos = filename.lastIndexOf(".");
		if (dotPos>-1 && dotPos<filename.length()-1){
			String res[] = new String[2];
			res[0] = filename.substring(0, dotPos);
			res[1] = filename.substring(dotPos+1);
			return res;
		} else {
			return null;
		}
	}

	public static Bitmap recycleBitmap(Bitmap bitmap){
		if (bitmap!=null){
			if (!bitmap.isRecycled()){
				bitmap.recycle();
			}
			bitmap = null;
		}
		return null;
	}

	public static String getTrimmedNonNullStr(String string) {
		return string==null?"":string.trim();
	}

	public static File copyFile(Uri uri, String destination){
		return copyFile(uri, destination, null);
	}
	
	public static File copyFile(Uri uri, String destination, String saveAsFilename){
		File destinationFile = new File(destination);
		File sourceFile = new File(uri.getPath());

		if (!sourceFile.isFile() && !sourceFile.exists()){
			return null;
		}

		if (saveAsFilename!=null && destinationFile.isDirectory()){
			destinationFile = new File(destination, saveAsFilename);
		} else if (destinationFile.isDirectory()){
			destinationFile = new File(destination, sourceFile.getName());
		} else if (destinationFile.isFile() && destinationFile.exists()){
			destinationFile.delete();
		} else {
			return null;
		}

		InputStream in = null;
		OutputStream out = null;
		boolean bSuccess = true;
		try {
			in = new FileInputStream(sourceFile);
			out = new FileOutputStream(destinationFile);
			byte[] buf = new byte[128*1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			bSuccess = false;
		} catch (IOException e) {
			e.printStackTrace();
			bSuccess = false;
		} finally {
			try {
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			in = null;
			out = null;
		}
		return bSuccess?destinationFile:null;
	}
	
	public static FloatBuffer createNativeFloatBuffer(FloatBuffer source, int sourcePosition, int size){
		FloatBuffer buffer = createNativeFloatBuffer(size);
		copyFloatBuffer(source, buffer, sourcePosition, 0, size);
		return buffer;
	}
	
	public static FloatBuffer createNativeFloatBuffer(int initialCount){
		return ByteBuffer.allocateDirect(initialCount*Float.SIZE/Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
	}
	
	public static FloatBuffer createNativeFloatBufferBySize(int size){
		return ByteBuffer.allocateDirect(size*Float.SIZE/Byte.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
	}
	
	public static FloatBuffer reallocNativeFloatBuffer(FloatBuffer buffer, int capacity){
		if (buffer==null || capacity>buffer.capacity()){
			buffer = createNativeFloatBuffer(capacity);
		}
		return buffer;
	}
	
	public static void assertBufferCapacity(ByteBuffer buffer, int capacity){
		if (buffer==null || capacity>buffer.capacity()){
			alertAndAssert("Buffer too small.");
		}
	}
	
	public static ByteBuffer createNativeByteBuffer(int initialCount){
		return ByteBuffer.allocateDirect(initialCount).order(ByteOrder.nativeOrder());
	}
	
	public static ByteBuffer createNativeByteBuffer(byte[] bytes){
		ByteBuffer buffer = createNativeByteBuffer(bytes.length);
		buffer.position(0);
		buffer.put(bytes);
		return buffer;
	}
	
	public static ByteBuffer createNativeByteBuffer(byte[] bytes, int offset, int bytesCount){
		ByteBuffer buffer = createNativeByteBuffer(bytesCount);
		buffer.position(0);
		buffer.put(bytes, offset, bytesCount);
		buffer.position(0);
		return buffer;
	}
	
	public static ByteBuffer reallocNativeByteBuffer(ByteBuffer buffer, int capacity){
		if (buffer==null || capacity>buffer.capacity()){
			if (Logger.LOG_ENABLED) logger.i("reallocNativeByteBuffer: " + buffer + 
					" / current capacity=" + (buffer!=null?buffer.capacity():0) + "; requested capacity=" + capacity);
			buffer = createNativeByteBuffer(capacity);
		}
		return buffer;
	}
	
	public static void dumpBuffer(String msg, ByteBuffer buffer, int pos, int size, int stride){
		if (!Logger.LOG_ENABLED || !logger.isEnabled()){
			return;
		}
		int cnt=0;
		String line;
		logger.e("Dumping buffer[I] [" + msg + "]; size=" + size + ", stride=" + stride + ", limit=" + buffer.limit() + " / " + pos + "," + size);
		buffer.position(pos);
		for (int i=0;i<=(size/stride)-1;i++){
			cnt++;
			line = "";
			for (int s=0;s<stride;s++){
				line += (line.length()!=0?", ":" ") + buffer.get();
			}
			logger.e("BUFFER_DUMP[I] [" + msg + "] [" + cnt + "]" + line);
		}
	}
	
	public static void checkChangedBuffer(String msg, ByteBuffer buffer, int pos, int size){
		if (!Logger.LOG_ENABLED || !logger.isEnabled()){
			return;
		}
		int bChangedElements = 0;
		buffer.position(pos);
		for (int i=0;i<=size-1;i++){
			if (buffer.get()!=0){
				bChangedElements++;
			}
		}
		logger.e("checkChangedBuffer: " + bChangedElements + " of " + size);
	}
	
	public static void dumpBuffer(String msg, FloatBuffer buffer, int pos, int size, int stride){
		if (!Logger.LOG_ENABLED || !logger.isEnabled()){
			return;
		}
		int cnt=0;
		String line;
		logger.e("BUFFER_DUMP Dumping buffer[F] [" + msg + "]; size=" + size + ", stride=" + stride + ", limit=" + buffer.limit() + " / " + pos + "," + size);
		buffer.position(pos);
		for (int i=0;i<=(size/stride)-1;i++){
			cnt++;
			line = "";
			for (int s=0;s<stride;s++){
				line += (line.length()!=0?", ":" ") + buffer.get();
			}
			logger.e("BUFFER_DUMP [" + msg + "] [" + cnt + "]" + line);
		}
	}
	
	public static void copyFloatBuffer(FloatBuffer source, FloatBuffer destination, int sourcePosition, int destinationPosition, int size){
		if (sourcePosition>=0){
			source.position(sourcePosition);
		}
		if (destinationPosition>=0){
			destination.position(destinationPosition);
		}
		for (int i=0;i<size;i++){
			destination.put(source.get());
		}
	}
	
	public static float getFloatBufferPointValue(FloatBuffer buffer, int position, int pointIndex, int stride, int channel){
		return buffer.get(position + pointIndex*stride + channel);
	}
	
	public static void fillPath(Path path, FloatBuffer buffer, int size, int stride, int position){
		fillPath(path, buffer, size, stride, position, 0, 1);
	}
	
	public static void fillPath(Path path, FloatBuffer buffer, int size, int stride, int position, int x, int y){
		path.reset();
	    
	    int n = size/stride;
	    
	    for (int i = 1; i<n-2; i++){
	        if (i==1){
	            path.moveTo(getFloatBufferPointValue(buffer, position, i, stride, x), getFloatBufferPointValue(buffer, position, i, stride, y));
	        }
	    
	        if (i<n-3){
	            //CatmullRom to bezier
	            float bufferX[] = {getFloatBufferPointValue(buffer, position, i+0, stride, x), getFloatBufferPointValue(buffer, position, i+1, stride, x), 
	            		getFloatBufferPointValue(buffer, position, i+2, stride, x), getFloatBufferPointValue(buffer, position, i+3, stride, x)};
	            float bufferY[] = {getFloatBufferPointValue(buffer, position, i+0, stride, y), getFloatBufferPointValue(buffer, position, i+1, stride, y), 
	            		getFloatBufferPointValue(buffer, position, i+2, stride, y), getFloatBufferPointValue(buffer, position, i+3, stride, y)};
	            
	            float one6th = 0.166666666666f;
	            float bx1 = -one6th * bufferX[0] + bufferX[1] + one6th * bufferX[2];
	            float bx2 = +one6th * bufferX[1] + bufferX[2] - one6th * bufferX[3];
	            float bx3 = bufferX[2];
	            
	            float by1 = -one6th * bufferY[0] + bufferY[1] + one6th * bufferY[2];
	            float by2 = +one6th * bufferY[1] + bufferY[2] - one6th * bufferY[3];
	            float by3 = bufferY[2];
	            
	            path.cubicTo(bx1, by1, bx2, by2, bx3, by3);
	        }
	        
	        if (i==n-3){
	            path.close();
	        }
	    }
	}
	
	static public <T> void addAllToList(List<T> source, List<T> destination){
		for (T item: source){
			if (!destination.contains(item)){
				destination.add(item);
			}
		}
	}
	
	static public int resGetInt(Context context, int resId){
		return context.getResources().getInteger(resId);
	}
	
	public static double calcDistance(float x1, float y1, float x2, float y2) {
		return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
	}
	
	private static int NEXT_FILENAME_IDX = 0;
	
	public static String generateUniqueString(){
		NEXT_FILENAME_IDX = (NEXT_FILENAME_IDX+1)==100?NEXT_FILENAME_IDX=0:NEXT_FILENAME_IDX+1;
		return System.currentTimeMillis() + "-" + 10000 + (int)(Math.random()*9999) + "-" + 1000 + NEXT_FILENAME_IDX;
	}
	
	public int convertColorComponentToInt(float component){
		return (int)(component * 0xFF);
	}
	
	public int convertARGBFloatComponentsToIntColor(float a, float r, float g, float b){
		return convertARGBIntComponentsToIntColor(convertColorComponentToInt(a), convertColorComponentToInt(r), convertColorComponentToInt(g), convertColorComponentToInt(b));
	}
	
	public int convertARGBIntComponentsToIntColor(int a, int r, int g, int b){
		a = (a << 24) & 0xFF000000;
	    r = (r << 16) & 0x00FF0000;
	    g = (g << 8) & 0x0000FF00;
	    b = b & 0x000000FF;

	    return a | r | g | b;
	}
	
	public static int convertIntARGBColorToIntRGBColor(int intARGBColor){
		return intARGBColor & 0x00FFFFFF;
	}

	public static int convertIntRGBColorToIntARGBColor(int intRGBColor) {
		return intRGBColor | 0xFF000000;
	}
	
    public static int convertIntARGBColorToIntRGBAColor(int intARGBColor){
        return ((intARGBColor&0xff000000)>>>24) | ((intARGBColor&0x00ffffff)<<8);
    }

    public static int convertIntRGBAColorToIntARGBColor(int intRGBAColor) {
        return ((intRGBAColor&0xffffff00)>>>8) | ((intRGBAColor&0xff)<<24);
    }

	public static int premultiplyIntColor(int color) {
		return Color.argb(Color.alpha(color), 
				mul255(Color.red(color), Color.alpha(color)), 
				mul255(Color.green(color), Color.alpha(color)), 
				mul255(Color.blue(color), Color.alpha(color)));
	}
	
	private static int mul255(int c, int a) {
		int prod = c * a + 128;
		return (prod + (prod >> 8)) >> 8;
	}
	
	public static int premultiplyColor(int c) {
		int r = Color.red(c);
		int g = Color.green(c);
		int b = Color.blue(c);
		int a = Color.alpha(c);
		// now apply the alpha to r, g, b
		r = mul255(r, a);
		g = mul255(g, a);
		b = mul255(b, a);
		// now pack it in the correct order
		return pack8888(r, g, b, a);
	}
	
    private static int pack8888(int r, int g, int b, int a) {
        return (r << 0) | ( g << 8) | (b << 16) | (a << 24);
    }
    
    public static void invalidateRectF(RectF rect){
    	rect.left = Float.NaN;
    	rect.right = Float.NaN;
    	rect.top = Float.NaN;
    	rect.bottom = Float.NaN;
    }
    
    public static void uniteWith(RectF rect, RectF uniteWithRect){
    	if (Float.isNaN(rect.left)){
    		rect.left = uniteWithRect.left;
    	} else {
    		rect.left = Math.min(rect.left, uniteWithRect.left);
    	}
    	
    	if (Float.isNaN(rect.right)){
    		rect.right = uniteWithRect.right;
    	} else {
    		rect.right = Math.max(rect.right, uniteWithRect.right);
    	}
    	
    	if (Float.isNaN(rect.top)){
    		rect.top = uniteWithRect.top;
    	} else {
    		rect.top = Math.min(rect.top, uniteWithRect.top);
    	}
    	
    	if (Float.isNaN(rect.bottom)){
    		rect.bottom = uniteWithRect.bottom;
    	} else {
    		rect.bottom = Math.max(rect.bottom, uniteWithRect.bottom);
    	}
    }
    
    public static boolean saveBinaryFile(Uri uri, ByteBuffer buffer, int position, int size){
		File file = new File(uri.getPath());
		if (file.exists()){
			file.delete();
		}
		OutputStream out = null;
		boolean bSuccess = true;
		try {
			out = new FileOutputStream(file);
			byte[] buf = new byte[256*1024];
			buffer.position(0);
			int len;
			while (buffer.position()<buffer.limit()-1){
				len = Math.min(buf.length, buffer.remaining());
				buffer.get(buf, 0, len);
				out.write(buf, 0, len);
			}
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			bSuccess = false;
		} catch (IOException e) {
			e.printStackTrace();
			bSuccess = false;
		} finally {
			try {
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			out = null;
		}
		return bSuccess;
	}
	
	public static ByteBuffer loadBinaryFile(InputStream in){
		ByteBuffer buffer = null;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		boolean bSuccess = true;
		try {
			byte[] buf = new byte[256*1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				os.write(buf, 0, len);
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			bSuccess = false;
		} catch (IOException e) {
			e.printStackTrace();
			bSuccess = false;
		} finally { 
			Log.e("BUF", "BUFFER222: " + os.toByteArray().length);
			buffer = Utils.createNativeByteBuffer(os.toByteArray());
			try {
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				os.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return bSuccess?buffer:null;
	}
	
	public static ByteBuffer loadBinaryFile(Uri uri){
		return loadBinaryFile(uri, null, 0);
	}
	
	public static ByteBuffer loadBinaryFile(Uri uri, ByteBuffer optionalInBuffer, int optionalPosition){
		File file = new File(uri.getPath());

		if (!file.exists()){
			return null;
		}
		
		ByteBuffer buffer = null;
		InputStream in = null;
		boolean bSuccess = true;
		try {
			in = new FileInputStream(file);
			byte[] buf = new byte[256*1024];
			int len;
			buffer = Utils.reallocNativeByteBuffer(optionalInBuffer, optionalPosition + (int)file.length());
			buffer.position(0);
			while ((len = in.read(buf)) > 0) {
				buffer.put(buf, 0, len);
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			bSuccess = false;
		} catch (IOException e) {
			e.printStackTrace();
			bSuccess = false;
		} finally {
			try {
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			in = null;
		}
		return bSuccess?buffer:null;
	} 
}