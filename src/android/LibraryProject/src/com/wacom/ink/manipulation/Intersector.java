/*
 * Created by Zahari Pastarmadjiev.
 * Copyright (c) 2014 Wacom. All rights reserved.
 */

package com.wacom.ink.manipulation;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Iterator;

import android.graphics.RectF;

import com.wacom.ink.utils.Logger;

/**
 * This class is specialized in calculation of intersections between an {@link com.wacom.ink.manipulation.Intersectable Intersectable} instance and a target. 
 * Therefore stroke models, which should be intersectable, sould implement the Intersectable interface.<br/>
 * A target could be a path, or an area enclosed by a path. 
 * The class will calculate a list of intervals, covering the whole stroke. 
 * They will start from the beginning of the stroke and finish at the end of it. 
 * Every interval will be either entirely inside the target or entirely outside of the stroke. 
 * The class could be also used to for fast checking if the stroke and target are intersecting at all, without calculating the intervals.
 *
 * @param <E> extends {@link com.wacom.ink.manipulation.Intersectable Intersectable}
 */
public class Intersector<E extends Intersectable> {
	private final static Logger logger = new Logger(Intersector.class);
	private final static int TARGET_NONE = 0;
	private final static int TARGET_AS_STROKE = 1;
	private final static int TARGET_AS_CLOSED_PATH = 2;	
	private int currentTarget;
	
	/**
	 * @hide
	 */
	public long handle;
	private IntersectionResult intersection;
	
	/**
	 * Constructs an Intersector instance.
	 */
	public Intersector() {
		handle = nativeInitialize();
		currentTarget = TARGET_NONE;
		intersection = new IntersectionResult();
	}
	
	private void setIntervalsResult(ByteBuffer indices, ByteBuffer values, ByteBuffer inside) {
		if (Logger.LOG_ENABLED) logger.i("called from JNI(1) / setIntervalsResult | " + indices.limit() + "," + indices.limit() + "," + inside.limit());
		intersection.iterator.indices = indices.order(ByteOrder.nativeOrder()).asIntBuffer();
		intersection.iterator.values = values.order(ByteOrder.nativeOrder()).asFloatBuffer();
		intersection.iterator.inside = inside;
		if (Logger.LOG_ENABLED) logger.i("called from JNI(2) / setIntervalsResult | " + intersection.iterator.indices.limit() + "," + intersection.iterator.values.limit() + "," + inside.limit());
	}
	
	private native long nativeInitialize();
	
	private static native void nativeClassInitialize();
	
	private static native void nativeCalculateSegmentBounds(FloatBuffer points, int pointsStride, float width, int index, float scattering, FloatBuffer segmentBoundsBuf);
	
	/**
	 * This method calculates the bounds of a segment of the specified path.
	 * @param points The control points for the path.
	 * @param pointsStride Defines the offset from one control point to the next.
	 * @param width The width of the path. If the control points include a width property value, this parameter should be NAN.
	 * @param index The index of the segment.
	 * @param scattering 
	 * @param segmentBoundsRect An optional RectF instance, where the result will be stored. If this parameter is null, a new RectF instance is created and returned.
	 * @return The bounds of the segment.
	 */
	public static RectF calculateSegmentBounds(FloatBuffer points, int pointsStride, float width, int index, float scattering, RectF segmentBoundsRect){
		if (segmentBoundsRect==null){
			segmentBoundsRect = new RectF();
		}
		FloatBuffer segmentBounds = com.wacom.ink.utils.Utils.createNativeFloatBuffer(4);
		
		nativeCalculateSegmentBounds(points, pointsStride, width, index, scattering, segmentBounds);
		
		segmentBounds.position(0);
		segmentBoundsRect.left = segmentBounds.get();
		segmentBoundsRect.top = segmentBounds.get();
		segmentBoundsRect.right = segmentBoundsRect.left + segmentBounds.get();
		segmentBoundsRect.bottom = segmentBoundsRect.top + segmentBounds.get();
		
		return segmentBoundsRect;
	}

	/**
	 * This method calculates the bounds of a single segment of a path. A segment is the curve between two control points.
	 * @param points The control points for the path.
	 * @param pointsStride Defines the offset from one control point to the next.
	 * @param width The width of the path. If the control points include a width property value, this parameter should be NAN.
	 * @param index The index of the segment.
	 * @param scattering This parameter will increase the width of each point. A value of 1 will double the with. Value of 0 is the default value. Values greater than 0 are used for paths rendered with a particle brushes.
	 * @return The bounds of the segment.
	 */
	public static RectF calculateSegmentBounds(FloatBuffer points, int pointsStride, float width, int index, float scattering){
		return calculateSegmentBounds(points, pointsStride, width, index, scattering, null);
	}
	
	private native void nativeFinalize(long handle);
	@Override
	protected void finalize() throws Throwable {
		try {
			nativeFinalize(handle);
		} finally {
			super.finalize();
		}
	}
	
	static {
		System.loadLibrary("InkingEngine");
		nativeClassInitialize();
	}
	
	/**
	 * The Interval class specifies an interval of a path. Each interval has a fromIndex and an endIndex, defining the 
	 * the indices of the path points, the current interval starts from, and ends from, respectively. Each instance can be either inside or
	 * outside the target of the intersection.
	 */
	public static class Interval {
		/**
		 * The index of the segments's starting point inside the path.
		 */
		public int fromIndex;
		/**
		 * The index of the segments's ending point inside the path.
		 */
		public int toIndex;
		/**
		 * The starting value for the Catmull-Rom spline parameter (1 is the default value).
		 */
		public float fromValue;
		/**
		 * The ending value for the Catmull-Rom spline parameter (1 is the default value).
		 */
		public float toValue;
		
		/**
		 * Each instance can be either inside or outside the target of the intersection. This parameter holds this information as a boolean flag.
		 */
		public boolean inside;
	}
	
	/**
	 * An iterator for intervals.
	 */
	public static class IntervalIterator implements Iterator<Interval> {
		private IntBuffer indices;
		private FloatBuffer values;
		private ByteBuffer inside;
		
		private int size;
		private int stride;
		private Interval interval = new Interval();

		public void reset(int size, int stride) {
			if (indices!=null){
				indices.position(0);
			}
			if (values!=null){
				values.position(0);
			}
			if (inside!=null){
				inside.position(0);
			}
			this.size = size;
			this.stride = stride;
		}

		@Override
		public boolean hasNext() {
			return indices.position() < size-1;
		}

		@Override
		public Interval next() {
			interval.fromIndex = indices.get() * stride;
			interval.fromValue = values.get();
			interval.toIndex = indices.get() * stride;
			interval.toValue = values.get();
			interval.inside = inside.get()!=(byte)0x00;
			return interval;
		}

		@Override
		public void remove() {
			
		}
	}
	
	/**
	 * This class is designed to represent the result of an intersection operation. It holds the number of the intervals produced by the intersection and 
	 * an {@link com.wacom.ink.manipulation.Intersector.IntervalIterator IntervalIterator} allowing the programmer to traverse the computed intervals.
	 */
	public static class IntersectionResult {
		private IntervalIterator iterator;
		private int intervalsSize;
		
		public IntersectionResult(){
			iterator = new IntervalIterator();
		}
		
		public void reset() {
			this.intervalsSize = 0;
			iterator.reset(0, 0);
		}
		
		public void reset(int intervalsSize, int stride) {
			this.intervalsSize = intervalsSize;
			iterator.reset(intervalsSize, stride);
		}
		
		/**
		 * This method returns the count of the intervals contained in the current IntersectionResult instance.
		 * @return count of intervals
		 */
		public int getCount(){
			return intervalsSize/2;
		}
		
		/**
		 * This method returns an {@link com.wacom.ink.manipulation.Intersector.IntervalIterator IntervalIterator} with intervals,
		 * computed by the intersection operation.
		 * @return an iterator with intervals
		 */
		public IntervalIterator getIterator(){
			return iterator;
		}
	}

	/**
	 * This method sets a stroke as target of the intersection. This method assumes that the control points of the path, defining a stroke, include a width property value.
	 * @param points The control points for the path. 
	 * @param position The position in float buffer of the first value of the path.
	 * @param size The size of the path. The size of the path is the number of float values inside the float buffer.
	 * @param stride Defines the offset from one control point to the next.
	 */
	public void setTargetAsStroke(FloatBuffer points, int position, int size, int stride){
		setTargetAsStroke(points, position, size, stride, Float.NaN);
	}

	private native void nativeSetTargetAsStroke(long handle, FloatBuffer points, int position, int size, int pointsStride, float width);
	
	/**
	 * This method sets a stroke as a target of the intersection. 
	 * @param points The control points for the path. 
	 * @param position The position in float buffer of the first value of the path.
	 * @param size The size of the path. The size of the path is the number of float values inside the float buffer.
	 * @param stride Defines the offset from one control point to the next.
	 * @param width The width of the path. If the control points include a width property value, this parameter should be NAN.
	 */
	public void setTargetAsStroke(FloatBuffer points, int position, int size, int stride, float width){
		if (size>=stride*4) {
			currentTarget = TARGET_AS_STROKE;
			nativeSetTargetAsStroke(handle, points, position, size, stride, width);
		} else {
			currentTarget = TARGET_NONE;
		}
	}
	
	private native void nativeSetTargetAsClosedPath(long handle, FloatBuffer points, int position, int size, int stride);
	
	/**
	 * This method sets an area enclosed by a path as a target of the intersection. 
	 * @param points The control points for the path. 
	 * @param position The position in float buffer of the first value of the path.
	 * @param size The size of the path. The size of the path is the number of float values inside the float buffer.
	 * @param stride Defines the offset from one control point to the next.
	 */
	public void setTargetAsClosedPath(FloatBuffer points, int position, int size, int stride){
		if (size>=stride*4) {
			currentTarget = TARGET_AS_CLOSED_PATH;
			nativeSetTargetAsClosedPath(handle, points, position, size, stride);
		} else {
			currentTarget = TARGET_NONE;
		}
	}
	
	private native void nativeIntersectWithTarget(long handle, FloatBuffer points, int size, int pointsStride, float width, float ts, float tf, float strokeBoundsX, float strokeBoundsY, float strokeBoundsW, float strokeBoundsH, FloatBuffer segmentsBounds);

	/**
	 * This method intersects an {@link com.wacom.ink.manipulation.Intersectable Intersectable} instance with the specified target. 
	 * @param intersectableStroke A stroke implementing the Intersectable interface.
	 * @return The result of the intersection. It is important to note, that for performance reasons, each Intersector instance will return the same IntersectionResult each time this method is called.
	 */
	public IntersectionResult intersectWithTarget(E intersectableStroke){
		//Some error handling
		if (currentTarget==TARGET_NONE){
			intersection.reset();
			return intersection;
		}
		nativeIntersectWithTarget(handle, 
				intersectableStroke.getPoints(), intersectableStroke.getSize(), intersectableStroke.getStride(), intersectableStroke.getWidth(), 
				intersectableStroke.getStartValue(), intersectableStroke.getEndValue(), 
				intersectableStroke.getBounds().left, intersectableStroke.getBounds().top, intersectableStroke.getBounds().width(), intersectableStroke.getBounds().height(), 
				intersectableStroke.getSegmentsBounds());
			
		intersection.reset(getIntervalsSize(), intersectableStroke.getStride());
		return intersection;
	}
	
	private native boolean nativeIsIntersectingTarget(long handle, FloatBuffer points, int size, int pointsStride, float width, float ts, float tf, float strokeBoundsX, float strokeBoundsY, float strokeBoundsW, float strokeBoundsH, FloatBuffer segmentsBounds);

	/**
	 * This method checks if a {@link com.wacom.ink.manipulation.Intersectable Intersectable} intersects with the specified target. No intervals calculation is being performed. 
	 * @param intersectableStroke A stroke implementing the Intersectable interface.
	 * @return True if the stroke is inside the target, false otherwise.
	 */
	public boolean isIntersectingTarget(E intersectableStroke){
		return nativeIsIntersectingTarget(handle, 
				intersectableStroke.getPoints(), intersectableStroke.getSize(), intersectableStroke.getStride(), intersectableStroke.getWidth(), 
				intersectableStroke.getStartValue(), intersectableStroke.getEndValue(), 
				intersectableStroke.getBounds().left, intersectableStroke.getBounds().top, intersectableStroke.getBounds().width(), intersectableStroke.getBounds().height(), 
				intersectableStroke.getSegmentsBounds());
	}
	
	private native int nativeGetIntervalsSize(long handle);
	private int getIntervalsSize(){
		return nativeGetIntervalsSize(handle);
	}
}
