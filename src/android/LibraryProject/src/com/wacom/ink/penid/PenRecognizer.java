package com.wacom.ink.penid;

import android.view.InputDevice;
import android.view.MotionEvent;

public class PenRecognizer {
	private static final long GD_MASK = 1l << 52;
	private static final String WACOM_PEN_INPUT_DEVICE_PREFIX = "Wacom";

	public static final long NO_ID = 0l;

	/**
	 * Utility method that checks if the motion event was triggered with a Wacom Pen and returns it's Pen Id.
	 * @param event {@link MotionEvent}
	 * @return The PenId of the stylus that triggered the event or {@link #NO_ID} if the pen has no identifier or 
	 * the motion event was not triggered with a stylus. 
	 */
	public static long getPenId(MotionEvent event) {
		int pointerIndex = event.getActionIndex();
		if (event.getToolType(pointerIndex) != MotionEvent.TOOL_TYPE_STYLUS && event.getToolType(pointerIndex) != MotionEvent.TOOL_TYPE_ERASER) {
			return NO_ID;
		}
		InputDevice inputDevice = event.getDevice();
		if (inputDevice == null || inputDevice.getName() == null || !inputDevice.getName().startsWith(WACOM_PEN_INPUT_DEVICE_PREFIX)) {
			return NO_ID;
		}
		long type = Float.floatToRawIntBits(event.getAxisValue(MotionEvent.AXIS_GENERIC_2, pointerIndex));
		long serialNumber = Float.floatToRawIntBits(event.getAxisValue(MotionEvent.AXIS_GENERIC_1, pointerIndex));
		long id = GD_MASK | (((type & 0xFF6) << 32) | serialNumber);
		
		return id;
	}

}
