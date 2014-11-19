/*
 * Created by Zahari Pastarmadjiev.
 * Copyright (c) 2013 Wacom. All rights reserved.
 */
package com.wacom.ink.utils;

import android.view.MotionEvent;

/**
 * OpenGl utility class.
 * 
 */
public class TouchUtils {
	private final static Logger logger = new Logger(TouchUtils.class, true);
	
	public final static float NO_PRESSURE = -1.0f;
	public final static double NO_TIMESTAMP = -1.0d;
	
	private final static float MINIMUM_POINT_DISTANCE = 2f;
	
	/**
	 * Constant: Minimum reported pressure by the Android Framework.
	 * <br/>According to Android Docs: "<i>For a touch screen or touch pad, reports the approximate pressure applied to the surface by a finger or other tool.
	 * The value is normalized to a range from 0 (no pressure at all) to 1 (normal pressure), although values higher than 1 may be generated depending on the calibration of the input device.</i>"
	 * <br/>That's why we take 0 for minimum and 1 maximum value.
	 */
	public final static float TOUCHINPUT_MIN_PRESSURE = 0.0f;

	/**
	 * Constant: Maximum reported pressure by the Android Framework.
	 * <br/>According to Android Docs: "<i>For a touch screen or touch pad, reports the approximate pressure applied to the surface by a finger or other tool.
	 * The value is normalized to a range from 0 (no pressure at all) to 1 (normal pressure), although values higher than 1 may be generated depending on the calibration of the input device.</i>"
	 * <br/>That's why we take 0 for minimum and 1 maximum value.
	 */
	public final static float TOUCHINPUT_MAX_PRESSURE = 1.0f;

	/**
	 * Constant:  for {@link #filterMotionEventForInking(MotionEvent, TouchPointID)}: The current motion event couldn't be interpreted as valid stroke event by the Wacom Ink.
	 */
	public final static int STROKE_EVENT_FAIL = -1;

	/**
	 * Constant for {@link #filterMotionEventForInking(MotionEvent, TouchPointID)}: The current motion event should be interpreted as stroke begin event by the Wacom Ink.
	 */
	public final static int STROKE_EVENT_BEGIN = 0;

	/**
	 * Constant for {@link #filterMotionEventForInking(MotionEvent, TouchPointID)}: The current motion event should be interpreted as stroke move event by the Wacom Ink.
	 */
	public final static int STROKE_EVENT_MOVE = 1;

	/**
	 * Constant for {@link #filterMotionEventForInking(MotionEvent, TouchPointID)}: The current motion event should be interpreted as stroke end event by the Wacom Ink.
	 */
	public final static int STROKE_EVENT_END = 2;

	/**
	 * Constant for {@link #filterMotionEventForInking(MotionEvent, TouchPointID)}: The current motion event should be interpreted as stroke end event by the Wacom Ink. 
	 * This constant is used when the Android Framework reports a MotionEvent.ACTION_CANCEL event, which should be treated as stroke end event. 
	 * Because the current MotionEvent is outside the screen, in this particular scenario the previous touch event's data should be used for the stroke end event ({@link TouchPointID#getOldX()}, {@link TouchPointID#getOldY()}).
	 * 
	 */
	public final static int STROKE_EVENT_FORCEEND = 3;
	
	public static float normalizePressure(float pressure, float srcMinPressure, float srcMaxPressure, float dstMinPressure, float dstMaxPressure, boolean bIsStylusEvent){
		if (!bIsStylusEvent){
			return NO_PRESSURE;
		}
		return Utils.transformValue(Math.min(srcMaxPressure, pressure), srcMinPressure, srcMaxPressure, dstMinPressure, dstMaxPressure);
	}

	public static boolean isStylusEvent(MotionEvent event) {
		return event.getToolType(event.getActionIndex())==MotionEvent.TOOL_TYPE_STYLUS;
	}

	/**
	 * Return an Wacom Ink based timestamp for the given MotionEvent.
	 * @param event a MotionEvent object
	 * @return timestamp in seconds
	 */
	public static double getTimestamp(MotionEvent event){
		double ts = event.getEventTime()/1000.0;
		return ts;
	}

	/**
	 * Returns an Wacom Ink based timestamp from the given timestamp.
	 * @param timestampInMillis timestamp in milliseconds
	 * 
	 * @return timestamp in seconds
	 */
	public static double getTimestamp(long timestampInMillis){
		return timestampInMillis/1000.0;
	}

	/**
	 * Object used to keep track of the coordinates, timestamp and the pointerId of an Android MotionEvent object.
	 * <p/>
	 * TouchPointID objects are used to store Android MotionEvent's data, relevant for the Wacom Ink stroke's lifecycle.
	 */
	public static class TouchPointID {
		private float x;
		private float y;
		private long timestamp;
		private float pressure;
		private int pointerId;
		
		private float oldX;
		private float oldY;
		private float oldPressure;
		private long oldTimestamp;
		
		public TouchPointID(float x, float y, int pointerId){
			oldX = -1;
			oldY = -1;
			this.x = x;
			this.y = y;
			this.pointerId = pointerId;
			timestamp = -1;
		}

		public TouchPointID() {
			this(-1, -1, -1);
		}

		public float getX() {
			return x;
		}

		public void setX(float x) {
			oldX = this.x;
			this.x = x;
		}

		public float getY() {
			return y;
		}

		public void setY(float y) {
			oldY = this.y;
			this.y = y;
		}

		public float getPressure() {
			return pressure;
		}
		
		public void setPressure(float pressure) {
			oldPressure = this.pressure;
			this.pressure = pressure;
		}
		
		public long getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(long timestamp) {
			oldTimestamp = this.timestamp;
			this.timestamp = timestamp;
		}

		public int getPointerId() {
			return pointerId;
		}

		public void setPointerId(int pointerId) {
			this.pointerId = pointerId;
		}

		public void invalidate(){
			pointerId=-1;
			setX(-1);
			setY(-1);
			setTimestamp(-1);
			setPressure(-1);
		}

		public boolean isValidXY(){
			return x>=0 && y>=0;
		}

		public boolean isValidOldXY(){
			return oldX>=0 && oldY>=0;
		}

		public float getOldX(){
			return oldX;
		}

		public float getOldY(){
			return oldY;
		}

		public long getOldTimestamp(){
			return oldTimestamp;
		}

		public float getOldPressure(){
			return oldPressure;
		}
		
		public boolean isValid(){
			return pointerId>=0;
		}

		public boolean isInvalid(){
			return !isValid();
		}

		public void setData(MotionEvent motionEvent, int pointerIndex){
			this.setX(motionEvent.getX(pointerIndex));
			this.setY(motionEvent.getY(pointerIndex)); 
			this.setTimestamp(motionEvent.getEventTime());
			this.setPressure(motionEvent.getPressure(pointerIndex));
			this.setPointerId(motionEvent.getPointerId(pointerIndex));
		} 

		public void setData(MotionEvent motionEvent) {
			setData(motionEvent, motionEvent.getActionIndex());
		}
	}

    /**
     * Translates Android Framework MotionEvents into Wacom Ink stroke events, performing basic analysis based on the current MotionEvent and the previously received touch input.
     * <p/> According to the InkingEngine Core's documentation, a stroke's life cycle starts with a stroke begin event, followed by at least one stroke move event and finishes with a stroke end event.
     * <p/>In order to ensure the correct Wacom Ink stroke's life cycle, the Android Framework's touch input is being filtered for every dispatched MotionEvent, 
     * the expected stroke's state is being returned and the current touch event's relevant data (x,y coordinates, pressure, action type, etc.) are being stored in a TouchPointID object in order 
     * to be used again as reference during the next call of this method.
     * @param motionEvent an MotionEvent reported by the Android Framework.
     * @param touchPointID a TouchPointID object containing relevant data of the previous MotionEvent.
     * 
     * @return The expected stroke's event.
     * <p/>Possible values:
     * <br/>{@link #STROKE_EVENT_BEGIN}, 
     * <br/>{@link #STROKE_EVENT_MOVE}, 
     * <br/>{@link #STROKE_EVENT_END}, 
     * <br/>{@link #STROKE_EVENT_FAIL}, 
     * <br/>{@link #STROKE_EVENT_FORCEEND}
     */
	public static int filterMotionEventForInking(MotionEvent motionEvent, TouchPointID touchPointID){
		int activePointerIndex = motionEvent.getActionIndex();
		int currentPointerId = motionEvent.getPointerId(activePointerIndex);

		int failCode = STROKE_EVENT_FAIL;

		switch (motionEvent.getActionMasked()){
		case MotionEvent.ACTION_DOWN:
			if (touchPointID.isValid()){
				throw new RuntimeException("ACTION_DOWN / already inking?");
			} else {
				touchPointID.setData(motionEvent);
			}
			if (logger.isEnabled()){
				if (Logger.LOG_ENABLED) logger.d("ACTION_DOWN / OK, down");
			}
			return STROKE_EVENT_BEGIN;

		case MotionEvent.ACTION_POINTER_DOWN:
			if (touchPointID.isInvalid()){
				//no prev point so we can begin, it's ok
				touchPointID.setData(motionEvent, activePointerIndex);
				if (Logger.LOG_ENABLED) logger.d("ACTION_POINTER_DOWN / OK, ptr_down: no prev point so we can begin");
				return STROKE_EVENT_BEGIN;
			} else {
				//prev point available, it can't be, fail!
				if (Logger.LOG_ENABLED) logger.d("ACTION_POINTER_DOWN / FAIL, ptr_down: prev point available, it can't be");
				return failCode;
			}  

		case MotionEvent.ACTION_MOVE:
			if (touchPointID.isInvalid()){
				if (Logger.LOG_ENABLED) logger.d("ACTION_MOVE / FAIL, move: invalid last point");
				return failCode;
			}
			int prevPointerIndex = motionEvent.findPointerIndex(touchPointID.getPointerId());
			if (prevPointerIndex==-1){
				//glitch: prev pointer id disappeared?! it's impossible! fail!
				throw new RuntimeException("ACTION_MOVE / prev pointer id disappeared?! it's impossible! fail!");
			} else if (!hasReallyMoved(motionEvent.getX(prevPointerIndex), motionEvent.getY(prevPointerIndex), touchPointID.getX(), touchPointID.getY())){
				//glitch: new move event, but the x,y coordinates are the same as the prev ones, fail!
				if (Logger.LOG_ENABLED) logger.d("ACTION_MOVE / FAIL, move: new move event, but the x,y coordinates are the same as the prev ones");
				return failCode;
			} else {
				//use prev. pointer id to take the data, it's ok
				if (Logger.LOG_ENABLED) logger.d("ACTION_MOVE / OK, move: use prev. pointer id to take the data");
				touchPointID.setData(motionEvent, prevPointerIndex);
				return STROKE_EVENT_MOVE;
			}

		case MotionEvent.ACTION_UP:
			if (touchPointID.isInvalid()){
				//glitch: prev point it missing?! it's impossible! fail!
				if (Logger.LOG_ENABLED) logger.d("ACTION_UP / FAIL, up: prev point it missing?! it's impossible!");
				return failCode;
			} else {
				//prev point it available, it's ok
				if (Logger.LOG_ENABLED) logger.d("ACTION_UP / OK, up: prev point is available");
				touchPointID.invalidate();
				return STROKE_EVENT_END;
			}   

		case MotionEvent.ACTION_POINTER_UP:
			if (touchPointID.isInvalid()){
				//no prev point, discard point, fail!
				if (Logger.LOG_ENABLED) logger.d("ACTION_POINTER_UP / FAIL, ptr_up: no prev point, discard point");
				return failCode;
			}
			if (currentPointerId == touchPointID.getPointerId()){
				//prev pointer id is the same, it's ok
				touchPointID.invalidate();
				if (Logger.LOG_ENABLED) logger.d("ACTION_POINTER_UP / OK, ptr_up: prev pointer id is the same");
				return STROKE_EVENT_END;
			} else {
				//prev pointer id is different, fail!
				return failCode;
			}

		case MotionEvent.ACTION_CANCEL:
			if (touchPointID.isValid()){
				touchPointID.invalidate();
				if (Logger.LOG_ENABLED) logger.d("OK, cancel: treat point as up, use prev. x,y");
				return STROKE_EVENT_FORCEEND;
				//Debug: debugHistory.add("13. ACTION_CANCEL lastWritePoint!=null; copy the last point and treat the point as PEN_UP; " + motionEvent);
			} else {
				if (Logger.LOG_ENABLED) logger.d("FAIL, cancel: cancel");
				return failCode;
			}
		}
		if (Logger.LOG_ENABLED) logger.d("FAIL, unknown: ?");
		return failCode;
	}

	private static boolean hasReallyMoved(float x, float y, float oldX, float oldY) {
		double dist = Math.sqrt((oldX-x)*(oldX-x)+(oldY-y)*(oldY-y));
		if (dist<MINIMUM_POINT_DISTANCE){
			if (Logger.LOG_ENABLED) logger.d("not 'moved': " + dist);
			return false;
		} else {
			return true;
		}
	}
}
