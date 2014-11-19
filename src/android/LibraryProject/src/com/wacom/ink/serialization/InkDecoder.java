package com.wacom.ink.serialization;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.wacom.ink.utils.Logger;
import com.wacom.ink.utils.Utils;

/**
 * Implements iterator-based interface for decoding compressed ink data.
 *
 */
public class InkDecoder {
	private final static Logger logger = new Logger(InkDecoder.class, true);
	
	/**
	 * @hide
	 */
	public long handle;
	
	private FloatBuffer decodedPathBuffer;
	
	private native long nativeInitialize(ByteBuffer dataBuffer, int dataBufferSize);
	
	/**
	 * Creates new instance.
	 * 
	 * @param dataBuffer A binary representation of a set of strokes to be decoded.
	 */
	public InkDecoder(ByteBuffer dataBuffer){
		handle = nativeInitialize(dataBuffer, dataBuffer.limit());
	}
	
	/**
	 * Creates new instance.
	 * 
	 * @param dataBuffer A binary representation of a set of strokes to be decoded.
	 * @param dataBufferSizeInBytes Number of bytes to read from the dataBuffer, starting at position 0.
	 */
	public InkDecoder(ByteBuffer dataBuffer, int dataBufferSizeInBytes){
		handle = nativeInitialize(dataBuffer, dataBufferSizeInBytes);
	}
	
	
	private native boolean nativeDecodePath(long handle);
	
	/**
	 * This method provides an iteration based approach to decode a set of encoded strokes. 
	 * It should be iteratively called until it returns false in order to decode the complete set of strokes. 
	 * @return True if a stroke has been decoded or false otherwise.  
	 */
	public boolean decodeNextPath(){
		return nativeDecodePath(handle);
	}
	
	private native ByteBuffer nativeGetDecodedPathData(long handle);
	
	/**
	 * This method returns the currently decoded path as a set of control points.
	 * @return A float buffer with the control points of the path.
	 */
	public FloatBuffer getDecodedPathData(){
		decodedPathBuffer = Utils.reallocNativeFloatBuffer(decodedPathBuffer, getDecodedPathSize());
		FloatBuffer buf = nativeGetDecodedPathData(handle).order(ByteOrder.nativeOrder()).asFloatBuffer();
		Utils.copyFloatBuffer(buf, decodedPathBuffer, 0, 0, getDecodedPathSize());
		return decodedPathBuffer;
	}
	
	
	private native int nativeGetDecodedPathSize(long handle);
	
	/**
	 * This method returns the size of the currently decoded path.
	 * 
	 * @return The physical size in floats of the path.
	 */
	public int getDecodedPathSize(){
		return nativeGetDecodedPathSize(handle);
	}
	
	private native int nativeGetDecodedPathStride(long handle);
	
	/**
	 * This method returns the stride of the currently decoded path.
	 * @return The stide of the path, which is the offset from one control point to the next.
	 */
	public int getDecodedPathStride(){
		return nativeGetDecodedPathStride(handle);
	}

	
	private native int nativeGetDecodedPathIntColor(long handle);
	
	/**
	 * This method returns the color of the path.
	 * 
	 * @return The int color in RGBA format.
	 */
	public int getDecodedPathIntColor(){
		return nativeGetDecodedPathIntColor(handle);
	}

	private native float nativeGetDecodedPathWidth(long handle);

	/**
	 * This method returns the width of the path.
	 * 
	 * @return The width of the path. If this parameter is NAN, the control points include a width property value.
	 */
	public float getDecodedPathWidth(){
		return nativeGetDecodedPathWidth(handle);
	}
	
	private native float nativeGetDecodedPathTs(long handle);
	
	/**
	 * This method returns the starting value for the Catmull-Rom spline of the stroke.
	 * 
	 * @return The starting value for the Catmull-Rom spline parameter (0 is the default value)..
	 */
	public float getDecodedPathTs(){
		return nativeGetDecodedPathTs(handle);
	}

	private native float nativeGetDecodedPathTf(long handle);
	
	/**
	 * This method returns the ending value for the Catmull-Rom spline of the stroke.
	 * 
	 * @return The ending value for the Catmull-Rom spline parameter (1 is the default value)..
	 */
	public float getDecodedPathTf(){
		return nativeGetDecodedPathTf(handle);
	}
	
	private native void nativeFinalize(long handle);
	
	@Override
	protected void finalize() throws Throwable {
		nativeFinalize(handle);
	}
	
	static { 
		System.loadLibrary("InkingEngine");
	}
}