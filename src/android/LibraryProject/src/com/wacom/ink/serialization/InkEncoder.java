package com.wacom.ink.serialization;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import com.wacom.ink.utils.Logger;
import com.wacom.ink.utils.Utils;

/**
 * Implements incremential encoding approach for creating a binary 
 * representation of multiple strokes. It utilizes the ProtocolBuffer 
 * Serialization library. Please refer to WILL File Format Specification for
 * more details. 
 */
public class InkEncoder {
	private final static Logger logger = new Logger(InkEncoder.class, true);
	
	/**
	 * @hide
	 */
	public long handle;
	private ByteBuffer encodedDataBuffer;
	
	private native long nativeInitialize();
	
	/**
	 * Create new instance.
	 */
	public InkEncoder(){
		handle = nativeInitialize();
	}
	
	/**
	 * Add new path to the encoder.
	 */
	private native void nativeEncodePath(long handle, int precision, FloatBuffer pointsBuffer, int size, int stride, float width, int intColor, float ts, float tf);

	/**
	 * This method encodes a single path, defined by its control points, width, color, ts and tf values.
	 *  
	 * @param precision 
	 * @param pointsBuffer A set of control points.
	 * @param size The size of the path. This is the physical size in floats of the path.
	 * @param stride Defines the offset from one control point to the next.
	 * @param width The width of the path. If the control points include a width property value, this parameter should be NAN.
	 * @param intColor The color in RGBA format.
	 * @param ts The starting value for the Catmull-Rom spline parameter (0 is the default value).
	 * @param tf The ending value for the Catmull-Rom spline parameter (1 is the default value).
	 */
	public void encodePath(int precision, FloatBuffer pointsBuffer, int size, int stride, float width, int intColor, float ts, float tf){
		if (Logger.LOG_ENABLED) logger.i("Encoding / encodePath / precision: " + precision + " size: " + size + " stride: " + stride + " width: " + width + " intColor: " + intColor + " t=" + ts + "," + tf);
		nativeEncodePath(handle, precision, pointsBuffer, size, stride, width, intColor, ts, tf);
	}
	
	
	private native ByteBuffer nativeGetEncodedData(long handle);
	
	/**
	 * Get all encoded paths in a ByteBuffer containing the compressed binary representation of all strokes, 
	 * which have been encoded using the {@link #encodePath(int, FloatBuffer, int, int, float, int, float, float)} method.
	 * 
	 * @return Byte buffer ready to be persisted.
	 */
	public ByteBuffer getEncodedData(){
		encodedDataBuffer = Utils.reallocNativeByteBuffer(encodedDataBuffer, getEncodedDataSizeInBytes());
		encodedDataBuffer.position(0);
		ByteBuffer buf = nativeGetEncodedData(handle);
		buf.position(0);
		encodedDataBuffer.put(buf);
		encodedDataBuffer.position(0);
		
		return encodedDataBuffer;
	}
	
	
	private native int nativeGetEncodedDataSizeInBytes(long handle);
	
	/**
	 * Returns the size of the encoded paths, returned by the {@link #getEncodedData()} method.
	 * 
	 * @return The size in bytes of the buffer.
	 */
	public int getEncodedDataSizeInBytes(){
		return nativeGetEncodedDataSizeInBytes(handle);
	}
	
	
	private native void nativeFinalize(long handle);
	
	@Override
	protected void finalize() throws Throwable {
		nativeFinalize(handle);
		encodedDataBuffer = null;
	}
	
	static { 
		System.loadLibrary("InkingEngine");
	}
}