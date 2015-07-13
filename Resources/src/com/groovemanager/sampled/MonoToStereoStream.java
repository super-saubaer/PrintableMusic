/*
 * Created on 24.06.2004
 *
 */
package com.groovemanager.sampled;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;

/**
 * This class is used for simple conversion of a mono AudioInputStream into
 * stereo just by duplicating the mono channel. It is used for instance in
 * <code>AudioManager.getStereoInputStream()</code>
 * @author Manu Robledo
 *
 */
public class MonoToStereoStream extends InputStream {
	/**
	 * The mono source stream to be converted
	 */
	final private AudioInputStream source;
	/**
	 * The sample size of the source stream´s format
	 */
	final private int sampleSize;
	/**
	 * Construct a new MonoToStereoInputStream
	 * @param source The source stream in mono to be converted
	 * @throws IllegalArgumentException If the source stream is not mono
	 */
	public MonoToStereoStream(AudioInputStream source) {
		if(source.getFormat().getChannels() != 1) throw new IllegalArgumentException("Source stream is not mono.");
		this.source = source;
		sampleSize = source.getFormat().getFrameSize();
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
		return 2 * source.available();
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
		int read = source.read(b, off, len / 2);
		
		for(int i = read - sampleSize; i >= 0; i -= sampleSize){
			for(int j = 0; j < sampleSize; j++){
				b[off + 2 * i + j] = b[off + 2 * i + sampleSize + j] = b[off + i + j];
			}
		}
		return 2 * read;
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
		return 2 * source.skip(s / 2);
	}
}
