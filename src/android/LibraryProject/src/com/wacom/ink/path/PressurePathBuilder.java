/*
 * Created by Zahari Pastarmadjiev.
 * Copyright (c) 2014 Wacom. All rights reserved.
 */

package com.wacom.ink.path;

import java.nio.FloatBuffer;

/**
 * The PressurePathBuilder class is a specialized PathBuilder designed for building of paths from pressure based user input.
 * 
 */
public class PressurePathBuilder extends PathBuilder{
	
	/**
	 * Constructs a new instance.
	 * @param density Specify density value used in normalization config.
	 */
	public PressurePathBuilder(float density) {
		super(density);
	}
	
	private native long nativeInitialize(float density);
	private native void nativeBeginPath(long handle, float x, float y, float pressure);
	private native void nativeAddPoint(long handle, float x, float y, float pressure);
	private native void nativeEndPath(long handle, float x, float y, float pressure);
	
	/**
	 * Starts building a path from pressure based input. Normally this method is called on down event.
	 * The method calculates a path part (a set of path control points) from the provided input.
	 * The returned path part can be modified by clients before adding it to the currently built path with the {@link #addPathPart(FloatBuffer, int)} method (for example the part could be first smoothened).
	 * @param x The x coordinate of the user's input in the desired path's coordinate system.
	 * @param y The y coordinate of the user's input in the desired path's coordinate system.
	 * @param pressure The pressure value.
	 */
	public FloatBuffer beginPath(float x, float y, float pressure) {
		beginPath();
		nativeBeginPath(handle, x, y, pressure);
		return getPathPartBuffer();
	}

	/**
	 * Continues the path generation from pressure based input. Normally this method is called on move event.
	 * The method calculates a path part (a set of path control points) from the provided input.
	 * The returned path part can be modified by clients before adding it to the currently built path with the {@link #addPathPart(FloatBuffer, int)} method (for example the part could be first smoothened).
	 * @param x The x coordinate of the user's input in the desired path's coordinate system.
	 * @param y The y coordinate of the user's input in the desired path's coordinate system.
	 * @param pressure The pressure value.
	 */
	public FloatBuffer addPoint(float x, float y, float pressure) {
		nativeAddPoint(handle, x, y, pressure);
		return getPathPartBuffer();
	}

	/**
	 * Ends the path generation from pressure based input. Normally this method is called on up event.
	 * The method calculates a path part (a set of path control points) from the provided input.
	 * The returned path part can be modified by clients before adding it to the currently built path with the {@link #addPathPart(FloatBuffer, int)} method (for example the part could be first smoothened).
	 * @param x The x coordinate of the user's input in the desired path's coordinate system.
	 * @param y The y coordinate of the user's input in the desired path's coordinate system.
	 * @param pressure The pressure value.
	 */
	public FloatBuffer endPath(float x, float y, float pressure) {
		nativeEndPath(handle, x, y, pressure);
		return getPathPartBuffer();
	}
	
	@Override
	protected long initialize(float density) {
		return nativeInitialize(density);
	}

	@Override
	/**
	 * Sets the min and max pressure values that will be used for clamping the input values.
	 * @param minValue The minimum pressure.
	 * @param maxValue The maximum pressure.
	 */
	public void setNormalizationConfig(float minValue, float maxValue) {
		nativeSetNormalizationConfig(handle, minValue, maxValue);
	}

}
