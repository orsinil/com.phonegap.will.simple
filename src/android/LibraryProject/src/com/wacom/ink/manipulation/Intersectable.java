/*
 * Created by Zahari Pastarmadjiev.
 * Copyright (c) 2014 Wacom. All rights reserved.
 */

package com.wacom.ink.manipulation;

import java.nio.FloatBuffer;

import android.graphics.RectF;

/**
 * This interface is designed to be used by the {@link com.wacom.ink.manipulation.Intersector} class, which is provides an implementation for calculation 
 * of intersections between an instance implementing the Intersectable and a specified target. 
 * Therefore stroke models, which should be intersectable, should implement the Intersectable interface.
 *
 */
public interface Intersectable{
	public FloatBuffer getPoints();
	
	/**
	 * Returns the physical size of the path.
	 * @return The size of the path in floats.
	 */
	public int getSize();
	
	/**
	 * Returns the stride of the path.
	 * @return The offset from one control point to the next.
	 */
	public int getStride();
	
	/**
	 * Returns the width of the path.
	 * @return The width of the path. If the control points include a width property value, this parameter should be NAN.
	 */
	public float getWidth();
	
	/**
	 * Returns the starting value for the Catmull-Rom spline parameter (0 is the default value).
	 * @return The starting value.
	 */
	public float getStartValue();

	/**
	 * Returns the ending value for the Catmull-Rom spline parameter (1 is the default value).
	 * @return The ending value.
	 */
	public float getEndValue();
	
	/**
	 * 
	 * @return
	 */
	public FloatBuffer getSegmentsBounds();
	public RectF getBounds();
}