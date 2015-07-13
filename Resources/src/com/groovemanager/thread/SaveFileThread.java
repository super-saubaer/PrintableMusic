package com.groovemanager.thread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * This ProgressThread implementation can be used for writing data from an
 * InputStream to an OutputStream
 * @author Manu Robledo
 *
 */
public class SaveFileThread extends ProgressThread {
	/**
	 * The OutputStream to write to
	 */
	protected OutputStream out;
	/**
	 * The InputStream to read from
	 */
	protected InputStream in;
	/**
	 * The length in frames to be written
	 */
	protected int frameLength;
	/**
	 * Temporary buffer
	 */
	protected byte[] buffer;
	/**
	 * The number of bytes written so far
	 */
	protected int written = 0;
	/**
	 * The size of one frame
	 */
	protected int frameSize;
	/**
	 * The number of bytes read in the last read operation. If this value is -1,
	 * it indicates that the end of the InputStream has been reached
	 */
	protected int numBytesRead = 0;
	/**
	 * Indicates, whether the streams should be closed when finishing the
	 * read/write operation or not
	 */
	protected boolean closeAtEnd;
	/**
	 * The default buffer size to use
	 */
	public final static int DEFAULT_BUFFER_SIZE = 32 * 1024;
	/**
	 * Create a new SaveFileThread using the default buffer size. This
	 * instance will not close the given streams when ready
	 * @param in The InputStream to read from
	 * @param out The OutputStream to write to
	 * @param frameLength The number of frames to write
	 * @param frameSize The size of one frame in bytes
	 */
	public SaveFileThread(InputStream in, OutputStream out, int frameLength, int frameSize) {
		this(in, out, frameLength, frameSize, false, DEFAULT_BUFFER_SIZE);
	}
	/**
	 * Create a new SaveFileThread using the default buffer size.
	 * @param in The InputStream to read from
	 * @param out The OutputStream to write to
	 * @param frameLength The number of frames to write
	 * @param frameSize The size of one frame in bytes
	 * @param closeAtEnd true, if the given streams should be closed when ready,
	 * false otherwise
	 */
	public SaveFileThread(InputStream in, OutputStream out, int frameLength, int frameSize, boolean closeAtEnd) {
		this(in, out, frameLength, frameSize, closeAtEnd, DEFAULT_BUFFER_SIZE);
	}
	/**
	 * Create a new SaveFileThread. This instance will not close the given
	 * streams when ready
	 * @param in The InputStream to read from
	 * @param out The OutputStream to write to
	 * @param frameLength The number of frames to write
	 * @param frameSize The size of one frame in bytes
	 * @param bufferSize The buffer size in bytes to be used
	 */
	public SaveFileThread(InputStream in, OutputStream out, int frameLength, int frameSize, int bufferSize) {
		this(in, out, frameLength, frameSize, false, bufferSize);
	}
	/**
	 * Create a new SaveFileThread
	 * @param in The InputStream to read from
	 * @param out The OutputStream to write to
	 * @param frameLength The number of frames to write
	 * @param frameSize The size of one frame in bytes
	 * @param closeAtEnd true, if the given streams should be closed when ready,
	 * false otherwise
	 * @param bufferSize The buffer size in bytes to be used
	 */
	public SaveFileThread(InputStream in, OutputStream out, int frameLength, int frameSize, boolean closeAtEnd, int bufferSize) {
		this.out = out;
		this.in = in;
		this.frameLength = frameLength;
		this.frameSize = frameSize;
		this.closeAtEnd = closeAtEnd;
		buffer = new byte[bufferSize];
	}
/*	
	public SaveFileThread(ProgressListener mon, InputStream in, OutputStream out, int frameLength, int frameSize) {
		this(mon, in, out, frameLength, frameSize, false, DEFAULT_BUFFER_SIZE);
	}
	public SaveFileThread(ProgressListener mon, InputStream in, OutputStream out, int frameLength, int frameSize, boolean closeAtEnd) {
		this(mon, in, out, frameLength, frameSize, closeAtEnd, DEFAULT_BUFFER_SIZE);
	}
	public SaveFileThread(ProgressListener mon, InputStream in, OutputStream out, int frameLength, int frameSize, int bufferSize) {
		this(mon, in, out, frameLength, frameSize, false, bufferSize);
	}
	public SaveFileThread(ProgressListener mon, InputStream in, OutputStream out, int frameLength, int frameSize, boolean closeAtEnd, int bufferSize) {
		super(mon);
		this.out = out;
		this.in = in;
		this.frameLength = frameLength;
		this.frameSize = frameSize;
		this.closeAtEnd = closeAtEnd;
		buffer = new byte[bufferSize];
	}
*/
	
	protected void init() {
	}
	protected int tellTotal() {
		return frameLength;
	}
	protected void processNext() {
		try {
			out.write(buffer, 0, numBytesRead);
			written += numBytesRead;
			numBytesRead = in.read(buffer);
		} catch (IOException e) {
			e.printStackTrace();
			cancelOperation();
		}
	}
	protected int tellElapsed() {
		return written / frameSize; 
	}
	protected boolean breakCondition() {
		return numBytesRead == -1;
	}
	protected void cleanUp() {
		if(closeAtEnd){
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	protected Object result() {
		return out;
	}
}
