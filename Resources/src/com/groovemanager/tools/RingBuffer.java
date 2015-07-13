package com.groovemanager.tools;

import java.nio.ByteBuffer;

/**
 * A RingBuffer represents a buffer that has a read and a write position which
 * are set back to 0 when the end is reached. Buffer underruns or overflows are
 * avoided by only allowing writing or reading aslong as there are enough bytes
 * available for the deired operation. As one can expect that reader and writer
 * will be different threads, this implementation is synchronized where needed.
 * @author Manu Robledo 
 *
 */
public class RingBuffer {
	/**
	 * When read and write position are the same, this value indicates, whether
	 * the buffer is filled completely (false) or emptied completely (true)
	 */
	private boolean writeAhead = false;
	/**
	 * Indicates whether this buffer is in open state or not
	 */
	private boolean open;
	/**
	 * The granularity in which data must be read or written. Data will only be
	 * available in blocks of this value.
	 */
	final private int granularity;
	/**
	 * A view to the source buffer used for reading
	 */
	final private ByteBuffer readBuffer,
	/**
	 * A view to the source buffer used for writing
	 */
	writeBuffer;
	/**
	 * Construct a new RingBuffer out of the given buffer
	 * @param buffer The buffer to be used as source for this ring buffer
	 * @param granularity The buffer granularity in bytes
	 */
	public RingBuffer(ByteBuffer buffer, int granularity) {
		this.granularity = granularity;
		if(buffer.remaining() % granularity > 0) throw new IllegalArgumentException("Granularity doens´t match buffer capacity.");
		readBuffer = buffer.slice();
		writeBuffer = buffer.slice();
	}
	/**
	 * Construct a new RingBuffer out of the specified part of the given byte
	 * array
	 * @param buffer The byte array that should be used as source of this buffer
	 * @param offset Start position inside the given array
	 * @param length The length of the buffer to create
	 * @param granularity The buffer granularity in bytes
	 */
	public RingBuffer(byte[] buffer, int offset, int length, int granularity){
		this(ByteBuffer.wrap(buffer, offset, length), granularity);
	}
	/**
	 * Get the number of bytes currently available for reading
	 * @return The number of bytes avilable for reading
	 */
	public synchronized int readAvailable(){
		if(!isOpen()) return 0;
		int readPos = readBuffer.position();
		int writePos = writeBuffer.position();
		if(readPos < writePos) return writePos - readPos;
		else if(readPos > writePos || writeAhead) return readBuffer.remaining() + writePos;
		else return 0;
	}
	/**
	 * Get the number of bytes currently available for writing
	 * @return The number of bytes available for writing
	 */
	public synchronized int writeAvailable(){
		int readPos = readBuffer.position();
		int writePos = writeBuffer.position();
		if(writePos < readPos) return readPos - writePos;
		else if(writePos > readPos || !writeAhead) return writeBuffer.remaining() + readPos;
		else return 0;
	}
	/**
	 * Try to read <code>len</code> bytes from this buffer starting at the
	 * current position into <code>b</code> starting at position
	 * <code>off</code>. The currently avilable bytes will be read the number
	 * of bytes read will be returned.
	 * @param b The byte array to transfer the data to
	 * @param off The offset inside the target array
	 * @param len The number of bytes to read
	 * @return The number of bytes read
	 */
	public int read(byte[] b, int off, int len){
		if(!isOpen()) return 0;
		len -= len % granularity;
		if(off + len > b.length) throw new IllegalArgumentException("Can´t read " + len + " bytes beginning at offset " + off + " into Array of length " + b.length);
		synchronized(this){
			len = Math.min(len, readAvailable());
			if(len == 0) return 0;
			
			int available = readBuffer.remaining();
			if(available <= len){
				readBuffer.get(b, off, available);
				readBuffer.rewind();
				readBuffer.get(b, off + available, len - available);
			}
			else{
				readBuffer.get(b, off, len);
			}
			if(readBuffer.position() == writeBuffer.position()) writeAhead = false;
			notify();
		}
		return len;
	}
	/**
	 * Try to write <code>len</code> bytes from <code>b</code> starting at
	 * index <code>off</code> into this buffer starting at the current position.
	 * This method will not return before the given number of bytes has been
	 * written or the buffer has been closed. This means that if the given
	 * number of bytes can not be written at once, this methods waits until
	 * another thread empties the needed part of this buffer by reading from it.
	 * @param b The array containing the data to write
	 * @param off Start position inside the given array
	 * @param len The number of bytes to write
	 * @return The number of bytes written
	 */
	public int write(byte[] b, int off, int len){
		len -= len % granularity;
		if(off + len > b.length) throw new IllegalArgumentException("Can´t write " + len + " bytes beginning at offset " + off + " from Array of length " + b.length);
		int written = 0;
		synchronized(this){
			while(len > written && isOpen()){
				while(writeAvailable() == 0) try{ wait(1000); } catch(InterruptedException e){}
				int toWrite = Math.min(len - written, writeAvailable());
				int available = writeBuffer.remaining();
				if(available <= toWrite){
					writeBuffer.put(b, off + written, available);
					written += available;
					writeBuffer.rewind();
					writeBuffer.put(b, off + written, toWrite - available);
					written += toWrite - available;
				}
				else{
					writeBuffer.put(b, off + written, toWrite);
					written += toWrite;
				}
				writeAhead = true;
			}
			if(len > 0 && !isOpen()){
				len = Math.min(len, writeAvailable());
				int available = writeBuffer.remaining();
				if(available <= len){
					writeBuffer.put(b, off + written, available);
					written += available;
					writeBuffer.rewind();
					writeBuffer.put(b, off + written, len - available);
					written += len - available;
				}
				else{
					writeBuffer.put(b, off + written, len);
					written += len;
				}
			}
		}
		return written;
	}
	/**
	 * Open this buffer
	 *
	 */
	public void open(){
		open = true;
	}
	/**
	 * Tells whether this buffer is currently open or not
	 * @return true, if this buffer is open, false otherwise
	 */
	public boolean isOpen(){
		return open;
	}
	/**
	 * Close this buffer
	 *
	 */
	public void close(){
		open = false;
	}
	/**
	 * Get this buffer´s total size
	 * @return This buffer´s total size in bytes 
	 */
	public int size(){
		return readBuffer.capacity();
	}
	/**
	 * Empty this buffer
	 *
	 */
	public synchronized void flush(){
		readBuffer.rewind();
		writeBuffer.rewind();
		writeAhead = false;
	}
}