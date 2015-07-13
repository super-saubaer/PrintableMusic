/*
 * Created on 24.06.2004
 *
 */
package com.groovemanager.sampled;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;

/**
 * This class is used for simple conversion of a stereo AudioInputStream into
 * mono just by reading from it´s left channel. It is used for instance in
 * <code>AudioManager.getMonoInputStream()</code>
 * @author Manu Robledo
 *
 */
public class StereoToMonoStream extends InputStream {
	/**
	 * The stereo source stream to be converted
	 */
	final private AudioInputStream source;
	/**
	 * The sample size of the source stream´s format
	 */
	final int sampleSize;
	/**
	 * Temporary Array
	 */
	private byte[] temp;
	/**
	 * Construct a new StereoToMonoInputStream
	 * @param source The source stream in stereo to be converted
	 * @throws IllegalArgumentException If the source stream is not stereo
	 */
	public StereoToMonoStream(AudioInputStream source){
		if(source.getFormat().getChannels() != 2) throw new IllegalArgumentException("Source stream is not stereo.");
		this.source = source;
		sampleSize = source.getFormat().getFrameSize() / 2;
	}
	/**
	 * Don´t use this method!
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		return 0;
	}
	/**
	 * @see java.io.InputStream#available()
	 */
	public int available() throws IOException {
		return source.available() / 2;
	}
	/**
	 * @see java.io.InputStream#close()
	 */
	public void close() throws IOException {
		source.close();
	}
	/**
	 * @see java.io.InputStream#mark(int)
	 */
	public synchronized void mark(int arg0) {
		source.mark(arg0);
	}
	/**
	 * @see java.io.InputStream#markSupported()
	 */
	public boolean markSupported() {
		return source.markSupported();
	}
	/**
	 * @see java.io.InputStream#read(byte[])
	 */
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}
	/**
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	public int read(byte[] b, int off, int len) throws IOException {
		if(temp == null || temp.length < 2 * len) temp = new byte[2 * len];
		int read = source.read(temp, 0, 2 * len);
		for(int i = 0; i < read / 2; i += sampleSize){
			for(int j = 0; j < sampleSize; j++){
				b[off + i + j] = temp[2 * i + j];
			}
		}
		return read / 2;
	}
	/**
	 * @see java.io.InputStream#reset()
	 */
	public synchronized void reset() throws IOException {
			source.reset();
	}
	/**
	 * @see java.io.InputStream#skip(long)
	 */
	public long skip(long s) throws IOException {
		return source.skip(2 * s);
	}
}
