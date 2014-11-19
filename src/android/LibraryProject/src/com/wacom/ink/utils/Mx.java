/*
 * Created by Zahari Pastarmadjiev.
 * Copyright (c) 2014 Wacom. All rights reserved.
 */

package com.wacom.ink.utils;

/**
 * Class containing utility functions for matrix operations.
 * 
 */
import android.graphics.Matrix;

public class Mx{
	private static Logger logger = new Logger(Mx.class);
	public static float transformPtX(Matrix mx, float x){
		float[] pts = new float[]{x, 0};
		mx.mapPoints(pts);
		return pts[0];
	}
	
	public static float transformPtY(Matrix mx, float y){
		float[] pts = new float[]{0, y};
		mx.mapPoints(pts);
		return pts[1];
	}
	
	public static float[] convertToRowMajor4x4Matrix(float[] mx4x4, float[] mx3x3){
		mx4x4[0]  = mx3x3[0]; mx4x4[1]  = mx3x3[1]; mx4x4[2]  = mx3x3[2]; mx4x4[3]  = 0;
		mx4x4[4]  = mx3x3[3]; mx4x4[5]  = mx3x3[4]; mx4x4[6]  = mx3x3[5]; mx4x4[7]  = 0;
		mx4x4[8]  = mx3x3[6]; mx4x4[9]  = mx3x3[7]; mx4x4[10] = mx3x3[8]; mx4x4[11] = 0;
		mx4x4[12] = 0; 	 	  mx4x4[13] = 0;	    mx4x4[14] = 0; 	      mx4x4[15] = 1;
		
		return mx4x4;
	}
	
	public static void limitMatrix(Matrix mx, float minS, float maxS, float width, float height){
		float[] v = new float[9];
		mx.getValues(v);
		boolean bFixed = false;
		
		if (Logger.LOG_ENABLED) logger.i("LIMIT_MATRIX before fix: " + mx.toShortString());
		
		if (v[Matrix.MSCALE_X]>maxS){
			mx.postScale(maxS/v[Matrix.MSCALE_X], maxS/v[Matrix.MSCALE_X], width/2, height/2);
			if (Logger.LOG_ENABLED) logger.i("LIMIT_MATRIX limit scale: " + v[Matrix.MSCALE_X] + ">" + maxS);
			bFixed = true;
		}
		
		if (v[Matrix.MSCALE_X]<minS){
			mx.postScale(minS/v[Matrix.MSCALE_X], minS/v[Matrix.MSCALE_X], width/2, height/2);
			if (Logger.LOG_ENABLED) logger.i("LIMIT_MATRIX limit scale: " + v[Matrix.MSCALE_X] + "<" + minS);
			bFixed = true;
		}

		if (Mx.transformPtX(mx, 0)>0){
			mx.postTranslate(-Mx.transformPtX(mx, 0), 0);
			if (Logger.LOG_ENABLED) logger.i("LIMIT_MATRIX fix translation (1)");
			bFixed = true;
		}
		if (Mx.transformPtY(mx, 0)>0){
			mx.postTranslate(0, -Mx.transformPtY(mx, 0));
			if (Logger.LOG_ENABLED) logger.i("LIMIT_MATRIX fix translation (2)");
			bFixed = true;
		}
		if (Mx.transformPtX(mx, width)<width){
			mx.postTranslate(width - Mx.transformPtX(mx, width), 0);
			if (Logger.LOG_ENABLED) logger.i("LIMIT_MATRIX fix translation (3)");
			bFixed = true;
		}
		if (Mx.transformPtY(mx, height)<height){
			mx.postTranslate(0, height - Mx.transformPtY(mx, height));
			if (Logger.LOG_ENABLED) logger.i("LIMIT_MATRIX fix translation (4)");
			bFixed = true;
		}
		if (bFixed){
			if (Logger.LOG_ENABLED) logger.i("LIMIT_MATRIX after fix: " + mx.toShortString());
		}
	}
	
	public static float[] convertToColumnMajor4x4Matrix(float[] mx4x4, float[] mx3x3){
		mx4x4[0] = mx3x3[0]; mx4x4[4] = mx3x3[1]; mx4x4[8]  = mx3x3[2]; mx4x4[12] = 0;
		mx4x4[1] = mx3x3[3]; mx4x4[5] = mx3x3[4]; mx4x4[9]  = mx3x3[5]; mx4x4[13] = 0;
		mx4x4[2] = mx3x3[6]; mx4x4[6] = mx3x3[7]; mx4x4[10] = mx3x3[8]; mx4x4[14] = 0;
		mx4x4[3] = 0; 	 	 mx4x4[7] = 0;	      mx4x4[11] = 0; 	    mx4x4[15] = 1;
		
		return mx4x4;
	}
	
	public static float[] convertTo4x4Matrix(float[] mx4x4, float[] mx3x3){
		mx4x4[0]  = mx3x3[0]; mx4x4[1]  = mx3x3[1];	mx4x4[2]  = 0; 	mx4x4[3]  = mx3x3[2];
		mx4x4[4]  = mx3x3[3]; mx4x4[5]  = mx3x3[4]; mx4x4[6]  = 0; 	mx4x4[7]  = mx3x3[5];
		mx4x4[8]  = 0; 		  mx4x4[9]  = 0;        mx4x4[10] = 1;  mx4x4[11] = 0;
		mx4x4[12] = mx3x3[6]; mx4x4[13] = mx3x3[7];	mx4x4[14] = 0; 	mx4x4[15] = mx3x3[8]; 
		
		mx4x4[0]  = mx3x3[Matrix.MSCALE_X]; mx4x4[1]  = mx3x3[Matrix.MSKEW_X]; 		mx4x4[2]  = 0; 	mx4x4[3]  = mx3x3[Matrix.MTRANS_X];
		mx4x4[4]  = mx3x3[Matrix.MSKEW_Y]; 	mx4x4[5]  = mx3x3[Matrix.MSCALE_Y];  	mx4x4[6]  = 0; 	mx4x4[7]  = mx3x3[Matrix.MTRANS_Y];
		mx4x4[8]  = 0; 		   				mx4x4[9]  = 0;         					mx4x4[10] = 1;  mx4x4[11] = 0;
		mx4x4[12] = 0; 		   				mx4x4[13] = 0;							mx4x4[14] = 0; 	mx4x4[15] = 1; 
		
//		for (int r=0;r<3;r++){
//			Log.e("convertTo4x4Matrix", "3x3 | mx[" + r+ "] " + mx3x3[r*3] + " " + mx3x3[r*3+1] + " " + mx3x3[r*3+2]);
//		}
//		
//		for (int r=0;r<4;r++){
//			Log.e("convertTo4x4Matrix", "4x4 | mx[" + r+ "] " + mx4x4[r*4] + " " + mx4x4[r*4+1] + " " + mx4x4[r*4+2] + " " + mx4x4[r*4+3]);
//		}
		return mx4x4;
	}
	
	public static float getScaleFactorX(Matrix mx){
		float[] v = new float[9];
		mx.getValues(v);
		return v[Matrix.MSCALE_X];
	}
	
}