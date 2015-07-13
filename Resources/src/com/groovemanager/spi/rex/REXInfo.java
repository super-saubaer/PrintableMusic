/*
 * Created on 23.04.2004
 *
 */
package com.groovemanager.spi.rex;

/**
 * This class is a wrapper class for the native REXInfo struct
 * @author Manu Robledo
 *
 */
final class REXInfo extends NativeClass {
	final REXHandle handle;
	/**
	 * Create a new REXInfo instance. A new native instance will also
	 * be created.
	 * @param handle The REXHandle to which this info object bleongs.
	 *
	 */
	REXInfo(REXHandle handle) {
		this.handle = handle;
	}
	/**
	 * Create a new REXInfo that is connected to an already existing
	 * native instance which is defined by the given pointer.
	 * @param pointer The address of the native REXInfo instance
	 * @param handle The REXHandle to which this info object bleongs.
	 */
	REXInfo(long pointer, REXHandle handle) {
		super(pointer);
		this.handle = handle;
	}
	protected native long createClass();
	protected native void cleanUp();
	
	/**
	 * The number of channels of this file (1 or 2)
	 * @return The number of channels of this file (1 or 2)
	 */
	native int fChannels();
	/**
	 * The sample rate of this file
	 * @return The sample rate of this file
	 */
	native int fSampleRate();
	/**
	 * The number of slices contained inside this file
	 * @return The number of slices contained inside this file
	 */
	native int fSliceCount();
	/**
	 * The tempo of this file in bpm multiplied by 1000
	 * @return The tempo of this file in bpm multiplied by 1000
	 */
	native int fTempo();
	/**
	 * The length of this file in Pulses Per Quarter (PPQ)
	 * @return The length of this file in Pulses Per Quarter (PPQ)
	 */
	native int fPPQLength();
	/**
	 * The nominator of this file큦 time signature
	 * @return The nominator of this file큦 time signature
	 */
	native int fTimeSigNom();
	/**
	 * The denominator of this file큦 time signature
	 * @return The denominator of this file큦 time signature
	 */
	native int fTimeSigDenom();
	/**
	 * The bit depth in which this file was stored. Has nothing to do with the
	 * constant 32 bit used for output rendering
	 * @return The bit depth in which this file was stored
	 */
	native int fBitDepth();
	
	/**
	 * Get the information of this object packed in an AudioFormat object
	 * @return An AudioFormat object describing this file큦 audio format.
	 */
	REXAudioFormat getAudioFormat(){
		return new REXAudioFormat(fSampleRate(), fChannels(), REXHandle.isBigEndian());
	}
	
	/**
	 * Calculate the frame length of this file out of the given information
	 * @return The length of this file in sample frames
	 */
	int getFrameLength(){
		if(handle != null){
			int length = 0;
			int sliceCount = fSliceCount();
			for(int i = 0; i < sliceCount; i++){
				try {
					length += handle.REXGetSliceInfo(i).sampleLengthTillNext();
				} catch (REXError e) {
					e.printStackTrace();
				}
			}
			return length;
		}
		else{
			double bpm = fTempo() / 1000.0;
			double sampleRate = fSampleRate();
			double ppqResolution = 15360;
			return (int)Math.round(fPPQLength() * sampleRate / ppqResolution / bpm * 60);
		}
	}
}