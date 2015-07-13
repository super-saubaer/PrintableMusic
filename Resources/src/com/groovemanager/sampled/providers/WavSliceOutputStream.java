/*
 * Created on 17.06.2004
 *
 */
package com.groovemanager.sampled.providers;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.sound.sampled.AudioFormat;

/**
 * This AudioFileOutputStream implementation supports writing of additional
 * slice informations into a wave file.
 * @author Manu Robledo
 *
 */
public class WavSliceOutputStream extends WavFileOutputStream {
	/**
	 * The slices to write if any
	 */
	protected int[][] slices;
	/**
	 * Construct a new WavSliceOutputStream
	 * @param f The file to write to
	 * @param format The AudioFormat in which to write the audio data
	 * @param properties Map of properties to write to the file, if supported
	 * @throws IOException If an I/O Error occurs during OutputStream creation
	 */
	public WavSliceOutputStream(File f, AudioFormat format, Map properties) throws IOException {
		super(f, format, properties);
		if(properties != null){
			Object o = properties.get("slices");
			if(o != null && o instanceof int[][]) slices = (int[][])o;
		}
	}
	/**
	 * 
	 * @see java.io.Closeable#close()
	 */
	public void close() throws IOException {
		if(slices != null && slices.length > 0) addChunk(new SliceChunk(slices));
		super.close();
	}
}
