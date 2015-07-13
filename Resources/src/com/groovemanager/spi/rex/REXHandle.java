/*
 * Created on 23.04.2004
 *
 */
package com.groovemanager.spi.rex;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.sound.sampled.AudioInputStream;

/**
 * This class is kind of a wrapper class vor the native REXHandle. All functions
 * of the REX API that require a reference to a REXHandle instance are found
 * in this wrapper class as member methods.
 * @author Manu Robledo
 *
 */
final class REXHandle extends NativeClass {
	/**
	 * An REXInfo object is stored with each REXHAndle so that only one call
	 * to the native function REXGetInfo() is needed per handle.
	 */
	private REXInfo info;
	
	/**
	 * Private constructor will only be called from REXCreate()
	 *
	 */
	private REXHandle() {
	}
	/**
	 * Create a new REXHandle that should be connected to an existing native
	 * instance specified by the given pointer.
	 * @param pointer
	 */
	REXHandle(long pointer) {
		super(pointer);
	}
	native protected long createClass();
	protected void cleanUp() {
		jREXDelete();
	}
	
	/**
	 * Creates an AudioInputStream from this REXHandle
	 * @return A new AudioInputStream for the file represented by this handle
	 * @throws REXError
	 */
	AudioInputStream getAudioInputStream() throws REXError{
		REXInfo info = REXGetInfo();
		return new AudioInputStream(new REXInputStream(this), info.getAudioFormat(), info.getFrameLength());
	}
	
	/**
	 * Create a new REXHandle out of a REX file
	 * @param file The REX file to create the handle for
	 * @return A new REXHandle related to the given file
	 * @throws REXError
	 * @throws IOException
	 */
	static REXHandle REXCreate(File file) throws REXError, IOException{
		if(file.length() > 4 * 1024 * 1024) throw new IOException("File too big");
		FileInputStream stream = new FileInputStream(file);
		ByteBuffer buffer = ByteBuffer.allocateDirect((int)file.length());
		byte[] temp = new byte[8192];
		int numBytesRead = 0;
		while(numBytesRead > -1){
			numBytesRead = stream.read(temp, 0, temp.length);
			if(numBytesRead > 0) buffer.put(temp, 0, numBytesRead);
		}
		stream.close();
		REXHandle handle = new REXHandle();
		REXError.throwREXError(handle.jREXCreate(buffer));
		
		handle.info = new REXInfo(handle);
		REXError.throwREXError(handle.jREXGetInfo(handle.info.getPointer()));
		
		return handle;
	}
	
	/**
	 * Get general information about the REX file represented by this handle
	 * @return A REXinfo instance containing general information about the file
	 * represented by this handle
	 */
	REXInfo REXGetInfo(){
		return info;
	}
	
	/**
	 * The REX API provides this function for getting information about a REX
	 * file without creating a REXHandle from it
	 * @param b A direct ByteBuffer containing the whole REX file data 
	 * @return A REXinfo instance containing general information about the file
	 * @throws REXError
	 */
	static REXInfo REXGetInfoFromBuffer(ByteBuffer b) throws REXError{
		REXInfo info = new REXInfo(null);
		REXError.throwREXError(jREXGetInfoFromBuffer(b, info.getPointer()));
		return info;
	}
	
	/**
	 * Get info about the author and copyright of the REX file represented by
	 * this handle
	 * @return A REXCreatorInfo instance containing information about author
	 * and copyright
	 * @throws REXError
	 */
	REXCreatorInfo REXGetCreatorInfo() throws REXError{
		REXCreatorInfo info = new REXCreatorInfo();
		REXError.throwREXError(jREXGetCreatorInfo(info.getPointer()));
		return info;
	}
	
	/**
	 * Get information about a specific slice inside this file
	 * @param index The zero-based index of the slice in question
	 * @return A REXSliceInfo instance containing information about the desired
	 * slice
	 * @throws REXError
	 */
	REXSliceInfo REXGetSliceInfo(int index) throws REXError{
		REXSliceInfo info = new REXSliceInfo(this, index);
		REXError.throwREXError(jREXGetSliceInfo(index, info.getPointer()));
		return info;
	}
	
	/**
	 * Set the output sample rate at which the audio data should be rendered
	 * @param rate The new output sample rate
	 * @throws REXError
	 */
	void REXSetOutputSampleRate(int rate) throws REXError{
		REXError.throwREXError(jREXSetOutputSampleRate(rate));
	}
	
	/**
	 * Render the audio data of one slice to the specified buffer
	 * @param index The zero-based index of the slice which should be rendered
	 * @param bufferFrameLength The length of the buffer in sample frames
	 * @param buffers An Array of two direct ByteBuffers into which the audio
	 * data should be rendered 
	 * @throws REXError
	 */
	void REXRenderSlice(int index, int bufferFrameLength, ByteBuffer[] buffers) throws REXError{
		boolean stereo = info.fChannels() != 1;
		REXError.throwREXError(jREXRenderSlice(index, bufferFrameLength, buffers[0], buffers[1], stereo));
	}
	
	/**
	 * The REX API provides a preview mechanism for previewing REX files
	 * without rendering the different slices. This method starts a preview.
	 * @throws REXError
	 */
	void REXStartPreview() throws REXError{
		REXError.throwREXError(jREXStartPreview());
	}

	/**
	 * Stop a previously started preview.
	 * 
	 * @throws REXError
	 */
	void REXStopPreview() throws REXError{
		REXError.throwREXError(jREXStopPreview());
	}
	
	/**
	 * Set the tempo in which the audio data should be rendered
	 * @param tempo The bpm multiplied with 1000. 123.456 is given as 123456
	 * @throws REXError
	 */
	void REXSetPreviewTempo(int tempo) throws REXError{
		REXError.throwREXError(jREXSetPreviewTempo(tempo));
	}
	
	/**
	 * Render one buffer of preview audio data. startPreview() must have been
	 * called before.
	 * @param frames The number of sample frames to render
	 * @param buffers An Array of two direct ByteBuffers into which the data
	 * should be rendered
	 * @throws REXError
	 */
	void REXRenderPreviewBatch(int frames, ByteBuffer[] buffers) throws REXError{
		REXError.throwREXError(jREXRenderPreviewBatch(frames, buffers[0], buffers[1]));
	}

	// Slice Rendering
	//private native int jREXCreate(byte[] buffer);
	private native int jREXCreate(ByteBuffer buffer);
	private native void jREXDelete();
	private native int jREXGetInfo(long infoPointer);
	private static native int jREXGetInfoFromBuffer(ByteBuffer buffer, long infoPointer);
	private native int jREXGetCreatorInfo(long infoPointer);
	private native int jREXGetSliceInfo(int sliceIndex, long infoPointer);
	private native int jREXSetOutputSampleRate(int rate);
	private native int jREXRenderSlice(int sliceIndex, int bufferFrameLength, ByteBuffer left, ByteBuffer right, boolean stereo);
	// Für Callback
	static native int REXPercentFinished();
	// Preview Playing
	private native int jREXStartPreview();
	private native int jREXStopPreview();
	private native int jREXSetPreviewTempo(int tempo);
	private native int jREXRenderPreviewBatch(int framesToRender, ByteBuffer left, ByteBuffer right);
	// Plattformspezifisch
	native static boolean isBigEndian();
}