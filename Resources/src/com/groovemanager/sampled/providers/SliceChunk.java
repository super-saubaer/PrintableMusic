/*
 * Created on 17.06.2004
 *
 */
package com.groovemanager.sampled.providers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * This class represents a self-defined Chunk for wave files to include slice
 * information.
 * @author Manu Robledo
 *
 */
public class SliceChunk extends WaveChunk {
	/**
	 * Construct a new SliceChunk out of the given slices
	 * @param slices For each slice this Array should contain an int-Array of
	 * size 2 containing the frame position of the slice at index [0] and
	 * the frame length of the slice or -1 at index [1] 
	 */
	public SliceChunk(int[][] slices) {
		super("slic", createData(slices));
	}
	/**
	 * Create the chunk data out of the given slices
	 * @param slices For each slice this Array should contain an int-Array of
	 * size 2 containing the frame position of the slice at index [0] and
	 * the frame length of the slice or -1 at index [1] 
	 * @return The chunk data
	 */
	private static byte[] createData(int[][] slices){
		byte[] b = new byte[slices.length * 8];
		ByteBuffer buffer = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
		for(int i = 0; i < slices.length; i++){
			buffer.putInt(slices[i][0]);
			buffer.putInt(slices[i][1]);
		}
		return b;
	}
	/**
	 * Construct a new SliceChunk out of the given chunk data. The chunk data
	 * must not contain the Chunk id ("slic") or the chunk length
	 * @param data The chunk data
	 */
	public SliceChunk(byte[] data) {
		super("slic", data);
	}
	/**
	 * Get the slices out of this Chunk
	 * @return The slices contained in this Chunk. For each slice this Array
	 * will contain an int-Array of size 2 containing the frame position of the
	 * slice at index [0] and the frame length of the slice or -1 at index [1]
	 */
	public int[][] getSlices(){
		chunkData.rewind();
		int[][] slices = new int[chunkData.capacity() / 8][2];
		for(int i = 0; i < slices.length; i++){
			slices[i][0] = chunkData.getInt();
			slices[i][1] = chunkData.getInt();
		}
		return slices;
	}
	/**
	 * Get the number of slices contained in this Chunk
	 * @return The number of slices contained in this chunk
	 */
	public int getSliceCount(){
		return chunkData.capacity() / 8;
	}
}
