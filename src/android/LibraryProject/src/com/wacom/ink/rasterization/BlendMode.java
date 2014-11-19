/*
 * Created by Zahari Pastarmadjiev.
 * Copyright (c) 2014 Wacom. All rights reserved.
 */

package com.wacom.ink.rasterization;

/**
 * Blend modes enumeration.
 */
public enum BlendMode {
	/**
	 * Normal blending.
	 */
	BLENDMODE_NORMAL((byte)0),
	/**
	 * Reverse blending.
	 */
	BLENDMODE_NORMAL_REVERSE((byte)1),
	/**
	 * "Zero" blending (erase). 
	 */
	BLENDMODE_ERASE((byte)2),

	/**
	 * No blending (overwrite). 
	 */
	BLENDMODE_NONE((byte)3),
	
	/**
	 * Max blending. 
	 */
	BLENDMODE_MAX((byte)4),
	/**
	 * Min blending.
	 */
	BLENDMODE_MIN((byte)5),
	
	/**
	 * Multiply blending. Multiplies alpha and color components.
	 */
	BLENDMODE_MULTIPLY((byte)6),

	BLENDMODE_ADD((byte)7),
	BLENDMODE_SUBSTRACT((byte)8),
	BLENDMODE_SUBSTRACT_REVERSE((byte)9),

	/**
	 * Multiply blending. Multiplies only color components.
	 */
	BLENDMODE_MULTIPLY_NO_ALPHA((byte)10),
	BLENDMODE_MULTIPLY_NO_ALPHA_INVERT((byte)11);

	byte value;
	BlendMode(byte value){
		this.value = value;
	}

	public byte getValue(){
		return value;
	}
	
	@Override
	public String toString() {
		BlendMode blend = this;
		if (blend==BLENDMODE_NORMAL){
			return STR_BLENDMODE_NORMAL;
		} else if (blend==BLENDMODE_NORMAL_REVERSE){
			return STR_BLENDMODE_NORMAL_REVERSE;
		} else if (blend==BLENDMODE_ERASE){
			return STR_BLENDMODE_ERASE;
		} else if (blend==BLENDMODE_MAX){
			return STR_BLENDMODE_MAX;
		} else if (blend==BLENDMODE_MIN){
			return STR_BLENDMODE_MIN;
		} else if (blend==BLENDMODE_MULTIPLY){
			return STR_BLENDMODE_MULTIPLY;
		} else if (blend==BLENDMODE_ADD){
			return STR_BLENDMODE_ADD;
		} else if (blend==BLENDMODE_SUBSTRACT){
			return STR_BLENDMODE_SUBSTRACT;
		} else if (blend==BLENDMODE_SUBSTRACT_REVERSE){
			return STR_BLENDMODE_SUBSTRACT_REVERSE;
		} else if (blend==BLENDMODE_MULTIPLY_NO_ALPHA){
			return STR_BLENDMODE_MULTIPLY_NO_ALPHA;
		} else if (blend==BLENDMODE_MULTIPLY_NO_ALPHA_INVERT){
			return STR_BLENDMODE_MULTIPLY_NO_ALPHA_INVERT;
		}
		return STR_BLENDMODE_NONE;
	}
	
	public static final String STR_BLENDMODE_NORMAL = "NORMAL";
	public static final String STR_BLENDMODE_NORMAL_REVERSE = "NORMAL_REVERSE";
	public static final String STR_BLENDMODE_ERASE = "ERASE";
	public static final String STR_BLENDMODE_NONE = "NONE";
	public static final String STR_BLENDMODE_MAX = "MAX";
	public static final String STR_BLENDMODE_MIN = "MIN";
	public static final String STR_BLENDMODE_MULTIPLY = "MULTIPLY";
	public static final String STR_BLENDMODE_ADD = "ADD";
	public static final String STR_BLENDMODE_SUBSTRACT = "SUBSTRACT";
	public static final String STR_BLENDMODE_SUBSTRACT_REVERSE = "SUBSTRACT_REVERSE";
	public static final String STR_BLENDMODE_MULTIPLY_NO_ALPHA = "MULTIPLY_NO_ALPHA";
	public static final String STR_BLENDMODE_MULTIPLY_NO_ALPHA_INVERT = "MULTIPLY_NO_ALPHA_INVERT";
	
	public static BlendMode getFromString(String blendModeAsString){
	    if (blendModeAsString.equals("NORMAL")) {
	        return BLENDMODE_NORMAL;
	    } else if (blendModeAsString.equals("NORMAL_REVERSE")) {
	    	return BLENDMODE_NORMAL_REVERSE;
	    } else if (blendModeAsString.equals("ERASE")) {
	    	return BLENDMODE_ERASE;
	    } else if (blendModeAsString.equals("MAX")) {
	    	return BLENDMODE_MAX;
	    } else if (blendModeAsString.equals("MIN")) {
	    	return BLENDMODE_MIN;
	    } else if (blendModeAsString.equals("MULTIPLY")) {
	        return BLENDMODE_MULTIPLY;
	    }
	    
	   return BLENDMODE_NONE;
	}
}
