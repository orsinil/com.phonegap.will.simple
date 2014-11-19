package com.wacom.ink.smooth;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import com.wacom.ink.utils.Logger;
import com.wacom.ink.utils.Utils;

/**
 *  This class is used the smooth out the noise in a data sequences stored in one or more channels. 
 *  The implementation is based on the double exponential smoothing technique. 
 *  The result of the smooth operation will depend only on the last several values of the sequence. 
 *  The default smoothener configuration works best for touch input with rate of 60 events per second.
 */
public class MultiChannelSmoothener {
	private final static Logger logger = new Logger(MultiChannelSmoothener.class, true);
	private long handle;
	private int channelCount;
	
	private FloatBuffer smoothBuffer;
	private int smoothBufferSize;
	
	private FloatBuffer smoothFinishBuffer;
	private int smoothFinishBufferSize;
	
	private FloatBuffer emptyBuffer;
	
	private native long nativeInitialize();
	private native void nativeConfigure(long handle, int channelCount);
	
	private SmoothingResult smoothResult;
	private SmoothingResult smoothFinishResult;
	
	/**
	 * Create a new instance with one or more channels.
	 * 
	 * @param channelCount Indicates how many independent data sequences we will smoothed.
	 */
	public MultiChannelSmoothener(int channelCount){
		handle = nativeInitialize();
		nativeConfigure(handle, channelCount);
		
		this.channelCount = channelCount;
		
		smoothFinishResult = new SmoothingResult();
		smoothResult = new SmoothingResult();
		
		for (int index=0;index<channelCount;index++){
			setDefaultProperties(index);
		}
		
		emptyBuffer = Utils.createNativeFloatBuffer(0);
		
	}
	
	private native void nativeReset(long handle);
	
	/**
	 * This method resets the instance. After calling this method, the new data sequence can be smoothed.
	 */
	public void reset(){
		nativeReset(handle);
	}

	private native void nativeSetDefaultProperties(long handle, int channelIndex);
	
	
	/**
	 * Configures the channel with the specified index with the default smoothing configuration.
	 */
	public void setDefaultProperties(int channelIndex){
		nativeSetDefaultProperties(handle, channelIndex);
	}
	
	private native void nativeSetChannelProperties(long handle, int channelIndex, float alpha, float beta, float finalBeta);
	
	public void setChannelProperties(int channelIndex, float alpha, float beta, float finalBeta){
		nativeSetChannelProperties(handle, channelIndex, alpha, beta, finalBeta);
	}
	
	private native void nativeSetChannelPropertiesOpt(long handle, int channelIndex, float alpha, float beta, float finalBeta, int windowSize, int iterations);
	
	public void setChannelProperties(int channelIndex, float alpha, float beta, float finalBeta, int windowSize, int iterations){
		nativeSetChannelPropertiesOpt(handle, channelIndex, alpha, beta, finalBeta, windowSize, iterations);
	}
	
	private native void nativeSetEnableChannel(long handle, int channelIndex, boolean bEnabled);
	
	/**
	 * Enables or disables the smoothing for the channel with the specified index.
	 * @param channelIndex The channel index.
	 * @param bEnabled If true the data in this channel will be smoothed, otherwise no smoothing will be performed.
	 */
	public void setEnableChannel(int channelIndex, boolean bEnabled){
		nativeSetEnableChannel(handle, channelIndex, bEnabled);
	}
	
	/**
	 * Enables the smoothing for the channel with the specified index.
	 * @param channelIndex The channel index.
	 */
	public void enableChannel(int channelIndex){
		setEnableChannel(channelIndex, true);
	}
	
	/**
	 * Disables the smoothing for the channel with the specified index.
	 * @param channelIndex The channel index.
	 */
	public void disableChannel(int channelIndex){
		setEnableChannel(channelIndex, false);
	}
	
	/**
	 * Returns the count of the independent data sequences to be smoothed.
	 * @return The channels count.
	 */
	public int getChannelCount(){
		return channelCount;
	}
	
	private native void nativeSmooth(long handle, FloatBuffer buffer, int size, boolean bFinish);
	
	/**
	 * This method smoothes the next values in the data sequences. The size of the buffer parameter must be a multiple of the channels count. 
	 * If the bFinish parameter is set to true, the method will return sequences that will smoothly reach the last set of the values passed - the last set of values will be equal to the last set of the values passed. 
	 * It is important to note that in this case the internal state of the instance will not change.
	 * @param buffer A buffer of float values to be smoothed.
	 * @param size The physical size in floats of the data to be smoothed.
	 * @param bFinish If set to false the size of the output will be equal to the size of the input. If set to true the size of the output could be greater than the size of the input. 
	 * @return The smoothed values. It is important to note, that for performance reasons, each time this method is called, the same SmoothingResult instance will be returned.
	 */
	public SmoothingResult smooth(FloatBuffer buffer, int size, boolean bFinish){
		nativeSmooth(handle, buffer, size, bFinish);
		return bFinish?smoothFinishResult:smoothResult;
	}

	private void setSmoothResult(ByteBuffer buffer, int size){
		smoothBufferSize = size;
		if (buffer==null){
			smoothBuffer = emptyBuffer;
		} else {
			smoothBuffer = buffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
		}
		smoothResult.set(smoothBuffer, smoothBufferSize);
	}
	
	private void setSmoothFinishResult(ByteBuffer buffer, int size){
		smoothFinishBufferSize = size;
		if (buffer==null){
			smoothFinishBuffer = emptyBuffer;
		} else {
			smoothFinishBuffer = buffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
		}
		smoothFinishResult.set(smoothFinishBuffer, smoothFinishBufferSize);
	}
	
	/**
	 * This class holding smoothed data.
	 */
	public static class SmoothingResult{
		private FloatBuffer buffer;
		private int size;
		
		/**
		 * Returns a set of smoothed values.
		 * @return a float buffer
		 */
		public FloatBuffer getSmoothedPoints(){
			return buffer;
		}
		
		/**
		 * Returns the size of the buffer, holding the smoothed values.
		 * @return The physical size in floats of the buffer.
		 */
		public int getSize(){
			return size;
		}
		
		void set(FloatBuffer buffer, int size){
			this.buffer = buffer;
			this.size = size;
		}
	}
	
	private native void nativeFinalize(long handle);
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		nativeFinalize(handle);
	}
	
	static { 
		System.loadLibrary("InkingEngine");
	}
}