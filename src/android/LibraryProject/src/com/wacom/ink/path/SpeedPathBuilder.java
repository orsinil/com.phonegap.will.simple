/*
 * Created by Zahari Pastarmadjiev.
 * Copyright (c) 2014 Wacom. All rights reserved.
 */

package com.wacom.ink.path;

import java.nio.FloatBuffer;

/**
 * The SpeedPathBuilder class is a specialized PathBuilder designed for building of paths from velocity based user input.
 * 
 */
public class SpeedPathBuilder extends PathBuilder{
	
	/**
	 * Constructs a new instance.
	 * @param density Specify density value used in normalization config.
	 */
	public SpeedPathBuilder(float density) {
		super(density);
	}
	
	private native long nativeInitialize(float density);
	private native void nativeBeginPath(long handle, float x, float y, double timestamp);
	private native void nativeAddPoint(long handle, float x, float y, double timestamp);
	private native void nativeEndPath(long handle, float x, float y, double timestamp);
	
	/**
	 * Starts building a path from velocity based input. Normally this method is called on a touch down event.
	 * The method calculates a path part (a set of path control points) from the provided input.
	 * The returned path part can be modified by clients before adding it to the currently built path with the {@link #addPathPart(FloatBuffer, int)} method (for example the part could be first smoothened).
	 * @param x The x coordinate of the user's input in the desired path's coordinate system.
	 * @param y The y coordinate of the user's input in the desired path's coordinate system.
	 * @param timestamp The timestamp of the user's input in seconds with double precision. It is used for the velocity calculation.
	 * @return A part of a path (a set of control points) as a float buffer.
	 */
	public FloatBuffer beginPath(float x, float y, double timestamp) {
		beginPath();
		nativeBeginPath(handle, x, y, timestamp);
		return getPathPartBuffer();
	}

	/**
	 * Continues the path generation from velocity based input. Normally this method is called on a touch move event.
	 * The method calculates a path part (a set of path control points) from the provided input.
	 * The returned path part can be modified by clients before adding it to the currently built path with the {@link #addPathPart(FloatBuffer, int)} method (for example the part could be first smoothened).
	 * @param x The x coordinate of the user's input in the desired path's coordinate system.
	 * @param y The y coordinate of the user's input in the desired path's coordinate system.
	 * @param timestamp The timestamp of the user's input in seconds with double precision. It is used for the velocity calculation.
	 * @return A part of a path (a set of control points) as a float buffer.
	 */
	public FloatBuffer addPoint(float x, float y, double timestamp) {
		nativeAddPoint(handle, x, y, timestamp);
		return getPathPartBuffer();
	}

	/**
	 * Ends the path generation from velocity based input. Normally this method is called on a touch up event.
	 * The method calculates a path part (a set of path control points) from the provided input.
	 * The returned path part can be modified by clients before adding it to the currently built path with the {@link #addPathPart(FloatBuffer, int)} method (for example the part could be first smoothened).
	 * @param x The x coordinate of the user's input in the desired path's coordinate system.
	 * @param y The y coordinate of the user's input in the desired path's coordinate system.
	 * @param timestamp The timestamp of the user's input in seconds with double precision. It is used for the velocity calculation.
	 * @return A part of a path (a set of control points) as a float buffer.
	 */
	public FloatBuffer endPath(float x, float y, double timestamp) {
		nativeEndPath(handle, x, y, timestamp);
		return getPathPartBuffer();
	}
	
	@Override
	protected long initialize(float density) {
		return nativeInitialize(density);
	}

	@Override
	/**
	 * Sets the min and max velocities that will be used for clamping the input values.
	 * @param minValue The minimum velocity.
	 * @param maxValue The maximum velocity.
	 */
	public void setNormalizationConfig(float minValue, float maxValue) {
		nativeSetNormalizationConfig(handle, density*minValue, density*maxValue);
	}

}
