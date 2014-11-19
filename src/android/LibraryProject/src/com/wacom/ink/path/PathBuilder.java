/*
 * Created by Zahari Pastarmadjiev.
 * Copyright (c) 2014 Wacom. All rights reserved.
 */

package com.wacom.ink.path;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * The PathBuilder class and its two concrete subclasses {@link com.wacom.ink.path.PressurePathBuilder PressurePathBuilder} and {@link com.wacom.ink.path.SpeedPathBuilder SpeedPathBuilder}
 * are used to translate user input into geometry representation of a stroke.
 * Roughly said it converts series of (x,y,timestamp) or (x,y,pressure) data into series of (x, y, width, alpha) control points of a spline. 
 * These control points could be rendered as stroke, or used in other ways.
 * <br/>
 * The PathBuilder class is responsible for building a path from user input,
 * input normalisation and path generation based on a preset property configuration.
 * In a few words, the PathBuilder class first normalizes the input and then
 * generates the path properties based on that input. The clients can 
 * configure a both normalisation step and path property generation step.
 * <br/>
 * The PathBuilder class also uses a two-step approach, to allow transformations of the 
 * intermediate path part before committing it to the actual path. One example of such kind of processing is data smoothing.
 * <br/>
 * The result of the generation process is a list of control points with 
 * width and opacity inform of Catmull-Rom splines.
 * <br/>
 * The PathBuilder class also provides a mechanism for obtaining a pre-liminary path,
 * which can be used to reduce the perceived latency introduced by the interpolation method
 * or any sort of processing applied by the client.
 * <br/>
 * WILL Core provides two different implementations of PathBuilder class suitable for pressure-based and 
 * simple touch-based input, which doesn't provide pressure information:<br/>
 * {@link com.wacom.ink.path.PressurePathBuilder PressurePathBuilder}<br/>
 * {@link com.wacom.ink.path.SpeedPathBuilder SpeedPathBuilder}
 */
public abstract class PathBuilder{
	protected float density;

	/**
	 * Enumeration of functions used for generating path property (width or opacity) from the normilized input.
	 */
	public static enum PropertyFunction {
		/**
		 * Based on power function: x^n
		 */
		Power((byte)1),
		/**
		 * Based on cosine function
		 */
		Periodic((byte)2),
		/**
		 * Similar to logistic function.
		 */
		Sigmoid((byte)3); 
		
		byte value;
		PropertyFunction(byte value){
	    	this.value = value;
		}
		
		/**
		 * @hide
		 */
		public byte getValue(){
			return value;
		}
	}
	
	/**
	 * Enumeration of path properties, which
	 * should be included in the generated path.
	 */
	public static enum PropertyName {
		/**
		 * States that the generated path should have variable width.
		 */
		Width((byte)1),

		/**
		 * States that the generated path should have variable opacity.
		 */
		Alpha((byte)2);
		
		byte value;
		
		PropertyName(byte value){
	    	this.value = value;
		}

		/**
		 * @hide
		 */
		public byte getValue(){
			return value;
		}
	}
	
	
	/**
	 * @hide
	 */
	public long handle;
	private int controlPointsCount;
	
	private FloatBuffer pathBuffer;
	private FloatBuffer pathPartBuffer;
	
	private FloatBuffer preliminaryPathPartBuffer;
	private FloatBuffer preliminaryPathBuffer;
	
	protected boolean bFinished = false;
	
	protected abstract long initialize(float density);
	private native void nativeFinalize(long handle);
	
	private native void nativeAddPathPart(long handle, FloatBuffer pathPartBuffer, int count);
	private native void nativeSetMovementThreshold(long handle, float minMovement);
	protected native void nativeSetNormalizationConfig(long handle, float minValue, float maxValue);

	private native void nativeSetPropertyConfig(long handle, byte name, float minValue, float maxValue, float initialValue, float finalValue, byte function, float functionParameter, boolean bShouldFlip);
	private native int nativeCalculateStride(long handle);

	/**
	 * Constructs a new instance.
	 * @param density Specify density value used in normalization config.
	 */
	public PathBuilder(float density) {
		this.density = density;
		handle = initialize(density);
	}
	
	public void beginPath(){
		bFinished = false;
	}
	
	/**
	 * Returns the number of sequential floats representing a single control point.
	 * For example:<br/>
	 * if a single control point has variable width, then the stride is be 3 (x, y, width);<br/>
	 * if a single control point has both variable width and variable alpha, then the stride is be 4 (x, y, width, alpha);<br/>
	 * @return the stride of the current path
	 */
	public int getStride() {
		return nativeCalculateStride(handle);
	}

	/**
	 * Returns a buffer containing the currently built path.
	 * @return a float buffer
	 */
	public FloatBuffer getPathBuffer(){
		return pathBuffer;
	}

	/**
	 * Returns a buffer containing the currently generated part of a path.<br/>
	 * This part can be modified by clients before committing it to the path with the {@link #addPathPart(FloatBuffer, int)} method. For example it can be smoothened.
	 * @return a float buffer
	 */
	protected FloatBuffer getPathPartBuffer(){
		return pathPartBuffer;
	}

//	/**
//	 * Checks if the PathBuilder has generated a preliminary path for the current state of the building process.<br/>
//	 * There are cases when there is no need of a preliminary path, like after adding the last point of the path.
//	 * @return true if there is a preliminary path
//	 */
//	public boolean hasPreliminaryPath(){
//		return preliminaryPathBuffer!=null && preliminaryPathBuffer.limit()>0;
//	}
	
	/**
	 * Returns a buffer containing the currently generated preliminary path.
	 * @return a float buffer
	 */
	public FloatBuffer getPreliminaryPathBuffer(){
		return preliminaryPathBuffer;
	}

	/**
	 * Returns a buffer containing the currently generated part of the preliminary path. 
	 * This part can be modified by clients before committing it to the preliminary path with the {@link #finishPreliminaryPath(FloatBuffer, int)} method. For example it can be smoothened.
	 * @return a float buffer
	 */
	public FloatBuffer getPreliminaryPathPartBuffer(){
		return preliminaryPathPartBuffer;
	}
	
	protected void setPathBuffer(ByteBuffer buffer){
		pathBuffer = buffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
	}

	protected void setPreliminaryPathPartBuffer(ByteBuffer buffer){
		preliminaryPathPartBuffer = buffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
	}
	
	protected void setPreliminaryPathBuffer(ByteBuffer buffer){
		preliminaryPathBuffer = buffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
	}
	
	protected void setPathPartBuffer(ByteBuffer buffer){
		pathPartBuffer = buffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
	}

	
	private native void nativeGetPreliminaryPathPart(long handle);
	public FloatBuffer createPreliminaryPath() {
		nativeGetPreliminaryPathPart(handle);
		return getPreliminaryPathPartBuffer();
	}
	
	private native void nativeGetPreliminaryPath(long handle, FloatBuffer preliminaryPathPartBuffer, int size);
	public FloatBuffer finishPreliminaryPath(FloatBuffer preliminaryPathPartBuffer, int size) {
		nativeGetPreliminaryPath(handle, preliminaryPathPartBuffer, size);
		return getPreliminaryPathBuffer();
	}
	
	/**
	 * This method adds a path part (set of control points) to the currently built path.
	 * @param points The control points to be added to currently built path.
	 * @param size The size of the path part to be added to the path. This is the physical size in floats of the control points.
	 */
	public void addPathPart(FloatBuffer points, int size) {
		nativeAddPathPart(handle, points, size);
	}
	
	private native int nativeGetNewPointsSize(long handle);
	
	/**
	 * This method returns the size of the control points added to the path with the last call of the {@link com.wacom.ink.path.PathBuilder#addPathPart addPathPart} method.
	 * @return The physical size in floats of the added control points.
	 */
	public int getAddedPointsSize(){
		return nativeGetNewPointsSize(handle);
	}
	
	
	private native int nativeGetPathPartSize(long handle);
	
	/**
	 * This method returns the size of the control points generated by the PathBuilder from the user input.
	 * @return The physical size in floats of the control points.
	 */
	public int getPathPartSize(){
		return nativeGetPathPartSize(handle);
	}
	
	private native int nativeGetPathSize(long handle);
	
	/**
	 * This method returns the size of the whole path.
	 * @return The physical size in floats of the path.
	 */
	public int getPathSize(){
		return nativeGetPathSize(handle);
	}

	/**
	 * Returns the number of control points contained in the path.
	 * @return The number of control points.
	 */
	public int getPointsCount(){
		return getPathSize()/getStride();
	}
	
	private native int nativeGetPreliminaryPathPartSize(long handle);
	
	/**
	 * This method returns the size of the control points of the preliminary path.
	 * @return The physical size in floats of the preliminary path.
	 */
	public int getPreliminaryPathSize(){
		return nativeGetPreliminaryPathPartSize(handle);
	}
	
	private native int nativeGetPreliminaryPathSize(long handle);
	
	/**
	 * This method returns the size of the finished preliminary path.
	 * @return The physical size in floats of the finished preliminary path.
	 */
	public int getFinishedPreliminaryPathSize(){
		return nativeGetPreliminaryPathSize(handle);
	}
	
	/**
	 * This method returns the position in the underlying path buffer, where the lastly added path part has been applied to.
	 * @return The position in the underlying path float buffer.
	 */
	public int getPathLastUpdatePosition(){
		int position = getPathSize() - getAddedPointsSize();
		return position;
	}
	
	private void delete() {
		nativeFinalize(handle);
	}
	
	private native boolean nativeHasFinished(long handle);
	
	/**
	 * This method checks if the PathBuilder has finished building the current path. The path will be finished when the endPath method is being called.
	 * @return True if the path is complete, false otherwise. 
	 */
	public boolean hasFinished(){
		return nativeHasFinished(handle);
	}
	
	protected void finalize() throws Throwable {
		delete(); 
		super.finalize();
	}

	/**
	 * Sets the movement threshold - the minimal distance between two input points required by the path builder needed to produce new path part. 
	 * The default value is 0.
	 *
	 * @param minMovement Threshold in pixels
	 */
	public void setMovementThreshold(float minMovement) {
		nativeSetMovementThreshold(handle, Float.isNaN(minMovement)?Float.NaN:minMovement);
	}
	
	/**
	 * Configures the PathBuilder to generate a specific property (width or opacity) based on the user input.</br>
	 * A property configuration guides the values that will be produced. A property could be the width or the alpha of the path for each control point.
	 * 
	 * @param name The name of the property that will be configured.
	 * @param minValue The minimum value of the property.
	 * @param maxValue The maximum value of the property.
	 * @param initialValue The value of the property in the beginning of the path. Could be set to NAN. In this case the value will be same as the next point.
	 * @param finalValue The value of the property in the ending of the path. Could be set to NAN. In this case the value will be same as the previous point.
	 * @param function The function that will convert the input into a property value. See {@link PropertyFunction}.
	 * @param functionParameter The parameter of the property function.
	 * @param bShouldFlip If set to true, the property will increase as the input increase. Otherwise the property will decrease as the input decreases.
	 */
	public void setPropertyConfig(PropertyName name, float minValue, float maxValue, float initialValue, float finalValue, PropertyFunction function, float functionParameter, boolean bShouldFlip) {
		nativeSetPropertyConfig(handle, name.getValue(), minValue, maxValue, initialValue, finalValue, function.getValue(), functionParameter, bShouldFlip);
	}
	
	private native void nativeDisablePropertyConfig(long handle, byte name);

	/**
	 * Instructs the PathBuilder to disregard the given property.
	 *
	 * @param name The name of the property to be disabled.
	 */
	public void disablePropertyConfig(PropertyName name) {
		nativeDisablePropertyConfig(handle, name.getValue());
	}
	
	/**
	 * Sets the minimum and maximum values that will be used for clamping the input values. Input values could be pressure or velocity, depending of the concrete class used.
	 * @param minValue The minimum value.
	 * @param maxValue The maximum value.
	 */
	public abstract void setNormalizationConfig(float minValue, float maxValue);
	
	public static int calculateSegmentsCount(int size, int stride){
		return calculatePointsCount(size, stride)-3;
	}
	
	/**
	 * Calculates the points from size and stride.
	 * @param size The size of the path in floats.
	 * @param stride Defines the offset from one control point to the next.
	 * @return
	 */
	public static int calculatePointsCount(int size, int stride){
		return size/stride;
	}
	
	static { 
		System.loadLibrary("InkingEngine");
	}
}
