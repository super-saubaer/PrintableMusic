/*
 * Created on 27.05.2004
 *
 */
package com.groovemanager.sampled.providers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Wave files can contain additional information besides the audio data
 * organized in chunks. This class provides support for such a chunk.
 * A chunk is organized in the following way:<br>
 * - The first 4 bytes contain an ASCII-String identifying the chunk type,
 * called chunk ID.<br>
 * - The next 4 bytes contain an Integer describing the length of the following
 * data in bytes.<br>
 * - The format of the following data is specific to each chunk type
 * @author Manu Robledo
 *
 */
public class WaveChunk{
	/**
	 * The Chunk ID (length 4)
	 */
	protected String chunkname;
	/**
	 * The chunk data without the chunk ID and the length
	 */
	protected ByteBuffer chunkData;
	/**
	 * Construct a new WaveChunk
	 * @param name The chunk ID (will be filled up or shortened to length 4)
	 * @param data The chunk data without the chunk ID and the length
	 */
	public WaveChunk(String name, byte[] data){
		chunkData = ByteBuffer.wrap(data);
		chunkname = name;
		chunkData.order(ByteOrder.LITTLE_ENDIAN);
	}
	/**
	 * Construct a new WaveChunk
	 * @param wholeChunk The chunk data including the chunk ID (first 4 bytes)
	 * and the chunk length (next 4 bytes)
	 */
	public WaveChunk(byte[] wholeChunk){
		chunkname = new String(wholeChunk, 0, 4);
		chunkData = ByteBuffer.wrap(wholeChunk, 8, wholeChunk.length - 8);
		chunkData.order(ByteOrder.LITTLE_ENDIAN);
	}
	/**
	 * Construct a new empty WaveChunk
	 * @param name The chunk ID (will be filled up or shortened to length 4)
	 * @param length The length of the chunk data
	 */
	public WaveChunk(String name, int length){
		this(name, new byte[length]);
	}
	/**
	 * Get the Chunk ID
	 * @return the Chunk ID
	 */
	public String getName(){
		return chunkname;
	}
	/**
	 * Get the chunk length
	 * @return The chunk length in bytes, not including the chunk ID and the
	 * length itself (each 4 bytes)
	 */
	public int getLength(){
		return chunkData.capacity();
	}
	/**
	 * Get the chunk data not including the chunk ID and the chunk length
	 * @return The chunk data
	 */
	public byte[] getData(){
		byte[] b = new byte[chunkData.capacity()];
		chunkData.rewind();
		chunkData.get(b);
		return b;
	}
	/**
	 * Write this chunk to an OutputStream
	 * @param out The OutputStream to write to
	 * @return The number of bytes actually written (8 + chunkdata length)
	 * @throws IOException IF an I/O Error occured during the write operation
	 */
	public int writeToOut(OutputStream out) throws IOException{
		int length = chunkData.capacity();
		byte[] b = new byte[4];
		ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).putInt(length);
		out.write(chunkname.getBytes(), 0, 4);
		out.write(b);
		out.write(getData());
		return 8 + getLength();
	}
	/**
	 * Write this chunk to a RandomAccessFile
	 * @param out The OutputStream to write to
	 * @return The number of bytes actually written (8 + chunkdata length)
	 * @throws IOException IF an I/O Error occured during the write operation
	 */
	public int writeToOut(RandomAccessFile out) throws IOException{
		int length = chunkData.capacity();
		byte[] b = new byte[4];
		ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).putInt(length);
		out.write(chunkname.getBytes(), 0, 4);
		out.write(b);
		out.write(getData());
		return 8 + getLength();
	}
}