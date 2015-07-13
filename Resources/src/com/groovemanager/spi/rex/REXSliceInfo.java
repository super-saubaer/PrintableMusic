/*
 * Created on 23.04.2004
 *
 */
package com.groovemanager.spi.rex;

/**
 * This class is a wrapper class for the native REXSliceInfo struct. A
 * REXSliceInfo instance contains information about one specific slice of
 * a REX file
 * @author Manu Robledo
 *
 */
final class REXSliceInfo extends NativeClass {
	/**
	 * The REXInfo object from the handle
	 */
	final REXInfo info;
	/**
	 * The zero-based index of this slice
	 */
	final int index;
	/**
	 * The REX handle to which this slice info belongs
	 */
	final REXHandle handle;
	/**
	 * Create a new REXSliceInfo instance. A new native instance will also
	 * be created.
	 * @param handle The REXHandle to which this slice info belongs.
	 * @param sliceIndex The zero-based index of the slice this info object
	 * belongs to.
	 * @throws REXError If handle.REXGetInfo() throws one.
	 */
	REXSliceInfo(REXHandle handle, int sliceIndex) throws REXError{
		info = handle.REXGetInfo();
		this.handle = handle;
		index = sliceIndex;
	}
	/**
	 * Create a new REXSliceInfo that is connected to an already existing
	 * native instance which is defined by the given pointer.
	 * @param pointer The address of the native REXSliceInfo instance
	 * @param handle The REXHandle to which this slice info belongs.
	 * @param sliceIndex The zero-based index of the slice this info object
	 * belongs to.
	 * @throws REXError If handle.REXGetInfo() throws one.
	 */
	REXSliceInfo(long pointer, REXHandle handle, int sliceIndex) throws REXError{
		super(pointer);
		info = handle.REXGetInfo();
		this.handle = handle;
		index = sliceIndex;
	}

	protected native long createClass();
	protected native void cleanUp();
	
	/**
	 * The position of this slice inside the file in PPQ (Parts Per Quarter
	 * Note)
	 * @return The position of this slice inside the file
	 */
	native int fPPQPos();
	/**
	 * The length of this slice in sample frames
	 * @return The length of this slice in sample frames
	 */
	native int fSamplelength();
	/**
	 * The position of this slice in sample frames
	 * @return The position of this slice in sample frames
	 */
	int samplePos(){
		double bpm = info.fTempo() / 1000.0;
		double ppqResolution = 15360;
		double sampleRate = info.fSampleRate();
		double pos = fPPQPos();
		return (int)Math.round(pos * sampleRate / ppqResolution / bpm * 60);
	}
	/**
	 * The length in sample frames between the beginning of this slice and
	 * either the beginning of the next slice or - if this is the last slice -
	 * the end of the file 
	 * @return The total length of this slice in sample frames including
	 * optional silence at the end
	 */
	int sampleLengthTillNext(){
		double ppqLength;
		if(info.fSliceCount() == index + 1) ppqLength = info.fPPQLength() - fPPQPos();
		else
			try {
				ppqLength = handle.REXGetSliceInfo(index + 1).fPPQPos() - fPPQPos();
			} catch (REXError e) {
				e.printStackTrace();
				return fSamplelength();
			}
		double bpm = info.fTempo() / 1000.0;
		double ppqResolution = 15360;
		double sampleRate = info.fSampleRate();
		return (int)Math.round(ppqLength * sampleRate / ppqResolution / bpm * 60);
	}
}
