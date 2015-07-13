/*
 * Created on 31.05.2004
 *
 */
package com.groovemanager.sampled.nondestructive;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class represents an InputStreams that reads its data from different
 * streams in a row, but acts like if it was one stream.
 * @author Manu Robledo
 *
 */
public class ConcatInputStream extends InputStream {
	/**
	 * The source streams
	 */
	private InputStream[] sources;
	/**
	 * Index of the currently used stream
	 */
	private int index = 0;
	/**
	 * Create a new ConcatInputStream out of the given Streams
	 * @param sources The source streams to concatenate
	 */
	public ConcatInputStream(InputStream[] sources) {
		this.sources = sources;
	}
	/**
	 * 
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		if(index >= sources.length) return -1;
		int i = sources[index].read();
		if(i == -1) index++;
		return i;
	}
	/**
	 * 
	 * @see java.io.InputStream#read(byte[])
	 */
	public int read(byte[] arg0) throws IOException {
		return read(arg0, 0, arg0.length);
	}
	/**
	 * 
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	public int read(byte[] arg0, int arg1, int arg2) throws IOException {
		if(index >= sources.length) return -1;
		int i = sources[index].read(arg0, arg1, arg2);
		if(i == -1){
			index++;
			return read(arg0, arg1, arg2);
		}
		else return i;
	}
	/**
	 * @see java.io.InputStream#available()
	 */
	public int available() throws IOException {
		if(index >= sources.length) return 0;
		else return sources[index].available(); 
	}
	/**
	 * @see java.io.InputStream#close()
	 */
	public void close() throws IOException {
		for (int i = 0; i < sources.length; i++) {
			sources[i].close();
		}
	}
	/**
	 * @see java.io.InputStream#mark(int)
	 */
	public synchronized void mark(int readlimit) {
	}
	/**
	 * @see java.io.InputStream#markSupported()
	 */
	public boolean markSupported() {
		return false;
	}
	/**
	 * @see java.io.InputStream#reset()
	 */
	public synchronized void reset() throws IOException {
	}
	/**
	 * @see java.io.InputStream#skip(long)
	 */
	public long skip(long n) throws IOException {
		if(n < 0) return 0;
		if(index >= sources.length) return 0;
		return sources[index].skip(n);
	}
}