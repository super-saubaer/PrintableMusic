/*
 * Created on 23.04.2004
 *
 */
package com.groovemanager.spi.floatEncoding;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

/**
 * This class is used for converion of PCM_SIGNED encoded audio data to
 * PCM_FLOAT encoding and vice versa. As an additional bonus it can provide
 * PCM_SIGNED to PCM_SIGNED and PCM_FLOAT to PCM_FLOAT conversion.
 * @author Manu Robledo
 *
 */
class FloatConversionInputStream extends InputStream {
	/**
	 * AudioFormat of the source data
	 */
	private AudioFormat sourceFormat,
	/**
	 * AudioFormat to convert to 
	 */
	targetFormat;
	/**
	 * Temporary buffer for reading from the source stream
	 */
	private byte[] readTemp = new byte[8 * 1024];
	/**
	 * Constants used for conversion
	 */
	private final static float MAXINT = -(float)Integer.MIN_VALUE, MAXINTBACK = (float)Integer.MAX_VALUE;
	/**
	 * Constants used for conversion
	 */
	private final static double MAXLONG = -(double)Long.MIN_VALUE, MAXLONGBACK = (double)Long.MAX_VALUE;
	/**
	 * The source stream to be converted
	 */
	private final AudioInputStream source;
	/**
	 * Constructs a new ConversionInputStream from the given AudioInputStream
	 * being converted to the given Format
	 * @param stream The Source AudioInputStream to be converted
	 * @param targetFormat The Format into which the Stream should be converted
	 */
	public FloatConversionInputStream(AudioInputStream stream, AudioFormat targetFormat) {
		this.source = stream;
		this.targetFormat = targetFormat;
		sourceFormat = stream.getFormat();
	}
	/**
	 * Frame size is at least 2 bytes so this method mustn't be called
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		throw new IOException("Can't read single bytes from this Input Stream.");
	}
	/**
	 * 
	 * @see java.io.InputStream#read(byte[])
	 */
	public int read(byte[] b) throws IOException{
		// Delegate read(byte[], int, int)
		return read(b, 0, b.length);
	}
	/**
	 * 
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	public int read(byte[] b, int off, int len) throws IOException{
		// Check arguments
		if(b.length < off + len) throw new IllegalArgumentException("Buffer size and length/offset are incompatible.");

		// Get the length of unconverted data to be read
		int originallength = (int)getUnconvertedLength(len);
		// Adapt the size of readTemp if needed
		if(readTemp.length < originallength) readTemp = new byte[originallength];
		// Read unconverted data
		originallength = source.read(readTemp, 0, originallength);
		if(originallength == -1) return -1;
		
		// Convert the data
		convert(readTemp, b, off, originallength);
		return (int)getConvertedLength(originallength);
	}
	/**
	 * Get the length in bytes of unconverted data which leads
	 * to converted data of the given length
	 * @param len The length in bytes of the converted data.
	 * @return The length in bytes of the unconverted data.
	 */
	private long getUnconvertedLength(long len){
		int s = sourceFormat.getFrameSize();
		int t = targetFormat.getFrameSize();

		// Cut off any division rest (usually there shouldn't be any)
		len -= len % t;
		
		return len / t * s;
	}
	/**
	 * Get the length in bytes that will be needed for converting
	 * the given length of unconverted data.
	 * @param len The length in bytes of the unconverted data.
	 * @return The length in bytes of the converted data.
	 */
	private long getConvertedLength(long len){
		int s = sourceFormat.getFrameSize();
		int t = targetFormat.getFrameSize();

		// Cut off any division rest (usually there shouldn't be any)
		len -= len % s;
		
		return len / s * t;
	}
	/**
	 * Swap the the samples inside the given array from little endian
	 * to big endian or vice versa.
	 * @param from The array of audio data
	 * @param off The start address inside the array
	 * @param length The length in bytes of the data to be processed
	 * @param sampleSize The size of one sample in bytes
	 */
	private static void swap(byte[] from, int off, int length, int sampleSize){
		byte temp;
		for(int i = 0; i < length; i += sampleSize){
			// Walk through the first half of the sample and exchange
			// the content with the content from the end of the sample
			for(int j = 0; j < sampleSize / 2; j++){
				// Temporary store the left value
				temp = from[off + i + j];
				// overwrite the left with the right value
				from[off + i + j] = from[off + i + sampleSize - j - 1];
				// overwrite the right with the stored left value
				from[off + i + sampleSize - j - 1] = temp;
			}
		}
	}
	/**
	 * Increase the sample size of the given audio data by copying
	 * it into another larger array and filling the samples up with
	 * zeros in the least significant bytes
	 * @param from The source array
	 * @param to The target array
	 * @param off offset inside the target array
	 * @param length The length in bytes of the data to be processed
	 * @param sampleSize The new sampleSize
	 * @param zeroCount The number of bytes to fill up per sample
	 * @param isBigEndian Indicates whether the zeros should be filled
	 * from the left or from the right side
	 */
	private static void insertZeros(byte[] from, byte[] to, int off, int length, int sampleSize, int zeroCount, boolean isBigEndian){
		// calculate the old sampleSize
		int oldSize = sampleSize - zeroCount;
		// empty the target Array
		Arrays.fill(to, off, length * sampleSize / oldSize, (byte)0);
		// Walk through the samples from the right
		for(int i = length / oldSize - 1; i >= 0; i--){
			if(isBigEndian){
				// Copy the old sample
				for(int j = 0; j < oldSize; j++){
					to[off + sampleSize * i + j] = from[oldSize * i + j];
				}
			}
			else{
				// Copy the old sample
				for(int j = 0; j < oldSize; j++){
					to[off + sampleSize * i + zeroCount + j] = from[oldSize * i + j];
				}
			}
		}
	}
	
	/**
	 * Decrease the sample size of the given audio data by copying
	 * it into another smaller array and cutting out the least
	 * significant bytes
	 * @param from The source array
	 * @param to The target array
	 * @param off offset inside the target array
	 * @param length The length in bytes of the data to be processed
	 * @param sampleSize The new sampleSize
	 * @param zeroCount The number of bytes to cut from each sample
	 * @param isBigEndian Indicates whether the bytes should be cut
	 * from the left or from the right side
	 */
	private static void deleteBytes(byte[] from, byte[] to, int off, int length, int sampleSize, int zeroCount, boolean isBigEndian){
		// calculate the old sampleSize
		int oldSize = sampleSize + zeroCount;
		// Walk through the samples from the left
		for(int i = 0; i < length / oldSize; i++){
			if(isBigEndian){
				for(int j = 0; j < oldSize; j++){
					// Copy the most significant bytes
					to[off + sampleSize * i + j] = from[oldSize * i + j];
				}
			}
			else{
				for(int j = 0; j < sampleSize; j++){
					// Copy the most significant bytes
					to[off + sampleSize * i + j] = from[oldSize * i + j + zeroCount];
				}
			}
		}
	}
	
	/**
	 * Convert the data from 32 Bit integer to 32 Bit float 
	 * @param from The data to be converted
	 * @param off Offset inside the data array
	 * @param length The number of bytes to be processed
	 * @param isBigEndian Indicates whether the samples are stored in
	 * big endian or little endian
	 */
	private static void intToFloat(byte[] from, int off, int length, boolean isBigEndian){
		// Temporary variables
		int itemp;
		float ftemp;
		if(isBigEndian){
			for(int i = 0; i < length; i += 4){
				// Get Int Value
				itemp = (from[off + i + 0] << 24) |
						((from[off + i + 1] &0xFF) << 16) |
						((from[off + i + 2] &0xFF) << 8) |
						(from[off + i + 3] &0xFF);
				// Normalize to +/- 1.0
				ftemp = itemp / MAXINT;
				// Construct an int from The Float Bits
				itemp = Float.floatToIntBits(ftemp);
				
				// Write back to Byte Array
				from[off + i + 0] = (byte) ((itemp >> 24) & 0xFF); 
				from[off + i + 1] = (byte) ((itemp >> 16) & 0xFF); 
				from[off + i + 2] = (byte) ((itemp >> 8) & 0xFF); 
				from[off + i + 3] = (byte) (itemp & 0xFF); 
			}
		}
		else{
			for(int i = 0; i < length; i += 4){
				// Get Int Value
				itemp = (from[off + i + 3] << 24) |
						((from[off + i + 2] &0xFF) << 16) |
						((from[off + i + 1] &0xFF) << 8) |
						(from[off + i + 0] &0xFF);
				// Normalize to +/- 1.0
				ftemp = itemp / MAXINT;
				// Construct an int from The Float Bits
				itemp = Float.floatToIntBits(ftemp);
				
				// Write back to Byte Array
				from[off + i + 3] = (byte) ((itemp >> 24) & 0xFF); 
				from[off + i + 2] = (byte) ((itemp >> 16) & 0xFF); 
				from[off + i + 1] = (byte) ((itemp >> 8) & 0xFF); 
				from[off + i + 0] = (byte) (itemp & 0xFF); 
			}
		}
	}

	/**
	 * Convert the data from 32 Bit float to 32 Bit integer 
	 * @param from The data to be converted
	 * @param off Offset inside the data array
	 * @param length The number of bytes to be processed
	 * @param isBigEndian Indicates whether the samples are stored in
	 * big endian or little endian
	 */
	private static void floatToInt(byte[] from, int off, int length, boolean isBigEndian){
		// Temporary variables
		int itemp;
		float ftemp;
		if(isBigEndian){
			for(int i = 0; i < length; i += 4){
				// Get Int Bits
				itemp = (from[off + i + 0] << 24) |
						((from[off + i + 1] &0xFF) << 16) |
						((from[off + i + 2] &0xFF) << 8) |
						(from[off + i + 3] &0xFF);
				// Get the Float Value
				ftemp = Float.intBitsToFloat(itemp);
				// Normalize to +/- Integer.MAX_VALUE
				ftemp *= MAXINTBACK;
				itemp = (int)ftemp;
				
				// Write back to Byte Array
				from[off + i + 0] = (byte) ((itemp >> 24) & 0xFF); 
				from[off + i + 1] = (byte) ((itemp >> 16) & 0xFF); 
				from[off + i + 2] = (byte) ((itemp >> 8) & 0xFF); 
				from[off + i + 3] = (byte) (itemp & 0xFF); 
			}
		}
		else{
			for(int i = 0; i < length; i += 4){
				// Get Int Bits
				itemp = (from[off + i + 3] << 24) |
						((from[off + i + 2] &0xFF) << 16) |
						((from[off + i + 1] &0xFF) << 8) |
						(from[off + i + 0] &0xFF);
				// Get the Float Value
				ftemp = Float.intBitsToFloat(itemp);
				
				// Normalize to +/- Integer.MAX_VALUE
				ftemp *= MAXINTBACK;
				itemp = (int)ftemp;
				
				// Write back to Byte Array
				from[off + i + 3] = (byte) ((itemp >> 24) & 0xFF); 
				from[off + i + 2] = (byte) ((itemp >> 16) & 0xFF); 
				from[off + i + 1] = (byte) ((itemp >> 8) & 0xFF); 
				from[off + i + 0] = (byte) (itemp & 0xFF); 
			}
		}
	}

	/**
	 * Convert the data from 64 Bit long to 64 Bit double 
	 * @param from The data to be converted
	 * @param off Offset inside the data array
	 * @param length The number of bytes to be processed
	 * @param isBigEndian Indicates whether the samples are stored in
	 * big endian or little endian
	 */
	private static void longToDouble(byte[] from, int off, int length, boolean isBigEndian){
		// Temporary variables
		long ltemp;
		double dtemp;
		if(isBigEndian){
			for(int i = 0; i < length; i += 8){
				// Get Long Value
				ltemp = (from[off + i + 0] << 56) |
						((from[off + i + 1] &0xFF) << 48) |
						((from[off + i + 2] &0xFF) << 40) |
						((from[off + i + 3] &0xFF) << 32) |
						((from[off + i + 4] &0xFF) << 24) |
						((from[off + i + 5] &0xFF) << 16) |
						((from[off + i + 6] &0xFF) << 8) |
						(from[off + i + 7] &0xFF);
				// Normalize to +/- 1.0
				dtemp = ltemp / MAXLONG;
				// Construct an long from The Double Bits
				ltemp = Double.doubleToLongBits(dtemp);
				
				// Write back to Byte Array
				from[off + i + 0] = (byte) ((ltemp >> 56) & 0xFF); 
				from[off + i + 1] = (byte) ((ltemp >> 48) & 0xFF); 
				from[off + i + 2] = (byte) ((ltemp >> 40) & 0xFF); 
				from[off + i + 3] = (byte) ((ltemp >> 32) & 0xFF); 
				from[off + i + 4] = (byte) ((ltemp >> 24) & 0xFF); 
				from[off + i + 5] = (byte) ((ltemp >> 16) & 0xFF); 
				from[off + i + 6] = (byte) ((ltemp >> 8) & 0xFF); 
				from[off + i + 7] = (byte) (ltemp & 0xFF); 
			}
		}
		else{
			for(int i = 0; i < length; i += 8){
				// Get Long Value
				ltemp = (from[off + i + 7] << 56) |
						((from[off + i + 6] &0xFF) << 48) |
						((from[off + i + 5] &0xFF) << 40) |
						((from[off + i + 4] &0xFF) << 32) |
						((from[off + i + 3] &0xFF) << 24) |
						((from[off + i + 2] &0xFF) << 16) |
						((from[off + i + 1] &0xFF) << 8) |
						(from[off + i + 0] &0xFF);
				// Normalize to +/- 1.0
				dtemp = ltemp / MAXLONG;
				// Construct a long from The Souble Bits
				ltemp = Double.doubleToLongBits(dtemp);
				
				// Write back to Byte Array
				from[off + i + 7] = (byte) ((ltemp >> 56) & 0xFF); 
				from[off + i + 6] = (byte) ((ltemp >> 48) & 0xFF); 
				from[off + i + 5] = (byte) ((ltemp >> 40) & 0xFF); 
				from[off + i + 4] = (byte) ((ltemp >> 32) & 0xFF); 
				from[off + i + 3] = (byte) ((ltemp >> 24) & 0xFF); 
				from[off + i + 2] = (byte) ((ltemp >> 16) & 0xFF); 
				from[off + i + 1] = (byte) ((ltemp >> 8) & 0xFF); 
				from[off + i + 0] = (byte) (ltemp & 0xFF); 
			}
		}
	}
	
	/**
	 * Convert the data from 64 Bit double to 64 Bit long 
	 * @param from The data to be converted
	 * @param off Offset inside the data array
	 * @param length The number of bytes to be processed
	 * @param isBigEndian Indicates whether the samples are stored in
	 * big endian or little endian
	 */
	private static void doubleToLong(byte[] from, int off, int length, boolean isBigEndian){
		// Temporary variables
		long ltemp;
		double dtemp;
		if(isBigEndian){
			for(int i = 0; i < length; i += 8){
				// Get Long Bits
				ltemp = (from[off + i + 0] << 56) |
						((from[off + i + 1] &0xFF) << 48) |
						((from[off + i + 2] &0xFF) << 40) |
						((from[off + i + 3] &0xFF) << 32) |
						((from[off + i + 4] &0xFF) << 24) |
						((from[off + i + 5] &0xFF) << 16) |
						((from[off + i + 6] &0xFF) << 8) |
						(from[off + i + 7] &0xFF);
				// Get the Double Value
				dtemp = Double.longBitsToDouble(ltemp);
				
				// Normalize to +/- Integer.MAX_VALUE
				dtemp *= MAXLONGBACK;
				ltemp = (long)dtemp;
				
				// Write back to Byte Array
				from[off + i + 0] = (byte) ((ltemp >> 56) & 0xFF); 
				from[off + i + 1] = (byte) ((ltemp >> 48) & 0xFF); 
				from[off + i + 2] = (byte) ((ltemp >> 40) & 0xFF); 
				from[off + i + 3] = (byte) ((ltemp >> 32) & 0xFF); 
				from[off + i + 4] = (byte) ((ltemp >> 24) & 0xFF); 
				from[off + i + 5] = (byte) ((ltemp >> 16) & 0xFF); 
				from[off + i + 6] = (byte) ((ltemp >> 8) & 0xFF); 
				from[off + i + 7] = (byte) (ltemp & 0xFF); 
			}
		}
		else{
			for(int i = 0; i < length; i += 8){
				// Get Long Bits
				ltemp = (from[off + i + 7] << 56) |
						((from[off + i + 6] &0xFF) << 48) |
						((from[off + i + 5] &0xFF) << 40) |
						((from[off + i + 4] &0xFF) << 32) |
						((from[off + i + 3] &0xFF) << 24) |
						((from[off + i + 2] &0xFF) << 16) |
						((from[off + i + 1] &0xFF) << 8) |
						(from[off + i + 0] &0xFF);
				// Get the Double Value
				dtemp = Double.longBitsToDouble(ltemp);
				
				// Normalize to +/- Integer.MAX_VALUE
				dtemp *= MAXLONGBACK;
				ltemp = (long)dtemp;
				
				// Write back to Byte Array
				from[off + i + 7] = (byte) ((ltemp >> 56) & 0xFF); 
				from[off + i + 6] = (byte) ((ltemp >> 48) & 0xFF); 
				from[off + i + 5] = (byte) ((ltemp >> 40) & 0xFF); 
				from[off + i + 4] = (byte) ((ltemp >> 32) & 0xFF); 
				from[off + i + 3] = (byte) ((ltemp >> 24) & 0xFF); 
				from[off + i + 2] = (byte) ((ltemp >> 16) & 0xFF); 
				from[off + i + 1] = (byte) ((ltemp >> 8) & 0xFF); 
				from[off + i + 0] = (byte) (ltemp & 0xFF); 
			}
		}
	}

	/**
	 * Convert the data depending on the source and target format
	 * while copying it from the source array to the target array
	 * @param from The array of audio data that should be converted
	 * @param to The array to copy the converted data to
	 * @param off offset inside the target array
	 * @param length The number of bytes to be processed
	 */
	private void convert(byte[] from, byte[] to, int off, int length){
		// Swap if needed
		if(sourceFormat.isBigEndian() != targetFormat.isBigEndian()) swap(from, off, length, sourceFormat.getSampleSizeInBits() / 8);
		
		// From PCM_FLOAT to PCM_SIGNED
		if(sourceFormat.getEncoding().equals(FloatAudioFormat.Encoding.PCM_FLOAT) && targetFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)){
			if(sourceFormat.getSampleSizeInBits() == 32){
				floatToInt(from, 0, length, targetFormat.isBigEndian());
			}
			else if(sourceFormat.getSampleSizeInBits() == 64){
				doubleToLong(from, 0, length, targetFormat.isBigEndian());
			}
		}
		
		// From 16 Bit...
		if(sourceFormat.getSampleSizeInBits() == 16){
			// ...to 24 Bit
			if(targetFormat.getSampleSizeInBits() == 24){
				insertZeros(from, to, off, length, 3, 1, targetFormat.isBigEndian());
			}
			// ...to 32 Bit
			else if(targetFormat.getSampleSizeInBits() == 32){
				insertZeros(from, to, off, length, 4, 2, targetFormat.isBigEndian());
			}
			// ...to 64 Bit
			else if(targetFormat.getSampleSizeInBits() == 64){
				insertZeros(from, to, off, length, 8, 6, targetFormat.isBigEndian());
			}
		}
		// From 24 Bit...
		else if(sourceFormat.getSampleSizeInBits() == 24){
			// ...to 16 Bit
			if(targetFormat.getSampleSizeInBits() == 16){
				deleteBytes(from, to, off, length, 2, 1, targetFormat.isBigEndian());
			}
			// ...to 32 Bit
			else if(targetFormat.getSampleSizeInBits() == 32){
				insertZeros(from, to, off, length, 4, 1, targetFormat.isBigEndian());
			}
			// ...to 64 Bit
			else if(targetFormat.getSampleSizeInBits() == 64){
				insertZeros(from, to, off, length, 8, 5, targetFormat.isBigEndian());
			}
		}
		// From 32 Bit...
		else if(sourceFormat.getSampleSizeInBits() == 32){
			// ...to 16 Bit
			if(targetFormat.getSampleSizeInBits() == 16){
				deleteBytes(from, to, off, length, 2, 2, targetFormat.isBigEndian());
			}
			// ...to 24 Bit
			else if(targetFormat.getSampleSizeInBits() == 24){
				deleteBytes(from, to, off, length, 3, 1, targetFormat.isBigEndian());
			}
			// ...to 64 Bit
			else if(targetFormat.getSampleSizeInBits() == 64){
				insertZeros(from, to, off, length, 8, 4, targetFormat.isBigEndian());
			}
		}
		// From 64 Bit...
		else if(sourceFormat.getSampleSizeInBits() == 64){
			// ...to 16 Bit
			if(targetFormat.getSampleSizeInBits() == 16){
				deleteBytes(from, to, off, length, 2, 6, targetFormat.isBigEndian());
			}
			// ...to 24 Bit
			else if(targetFormat.getSampleSizeInBits() == 24){
				deleteBytes(from, to, off, length, 3, 5, targetFormat.isBigEndian());
			}
			// ...to 32 Bit
			else if(targetFormat.getSampleSizeInBits() == 32){
				deleteBytes(from, to, off, length, 4, 4, targetFormat.isBigEndian());
			}
		}
		
		// From PCM_SIGNED to PCM_FLOAT
		if(sourceFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED) && targetFormat.getEncoding().equals(FloatAudioFormat.Encoding.PCM_FLOAT)){
			if(targetFormat.getSampleSizeInBits() == 32){
				intToFloat(to, off, (int)getConvertedLength(length), targetFormat.isBigEndian());
			}
			else if(targetFormat.getSampleSizeInBits() == 64){
				longToDouble(to, off, (int)getConvertedLength(length), targetFormat.isBigEndian());
			}
		}
	}
	/**
	 * 
	 * @see java.io.InputStream#skip(long)
	 */
	public long skip(long n) throws IOException {
		return getConvertedLength(source.skip(getUnconvertedLength(n)));
	}
	/**
	 * @see java.io.InputStream#available()
	 */
	public int available() throws IOException {
		return (int)getConvertedLength(source.available());
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
	public synchronized void mark(int readlimit) {
		source.mark((int)getUnconvertedLength(readlimit));
	}
	/**
	 * @see java.io.InputStream#markSupported()
	 */
	public boolean markSupported() {
		return source.markSupported();
	}
	/**
	 * @see java.io.InputStream#reset()
	 */
	public synchronized void reset() throws IOException {
		source.reset();
	}
}