/*
 * Created on 17.05.2004
 *
 */
package com.groovemanager.sampled.waveform;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import javax.sound.sampled.AudioFormat;

/**
 * This class extends PeakWaveForm for being able to handle dynamically growing
 * audio files especially for recording situations. This class is used by
 * DynamicAudioFileWaveForm.
 * @author Manu Robledo
 *
 */
public class DynamicPeakWaveForm extends PeakWaveForm {
	/**
	 * peak data pool
	 */
	private byte[][] datas = new byte[10][];
	/**
	 * peak data pool positions
	 */
	private int[] positions = new int[10];
	/**
	 * Current index inside the pool
	 */
	private int index = 0;
	/**
	 * Temporary counter
	 */
	private int tempCount;
	/**
	 * Temporary buffer
	 */
	private byte[] dataBuffer;
	/**
	 * Current write position for peak data to append  
	 */
	private int dataPos;
	/**
	 * size of one pool element
	 */
	private final static int ARRAY_SIZE = 32 * 1024;
	/**
	 * Frame size of the audio format in bytes
	 */
	private int frameSize,
	/**
	 * Sample size of the audio format
	 */
	sampleSize;
	/**
	 * endianess of the audio format
	 */
	private boolean bigEndian;
	/**
	 * The file into which the peak data should be written
	 */
	private File f;
	/**
	 * RandomAccessFile wor reading and writing the peak data
	 */
	private RandomAccessFile raFile;
	/**
	 * Construct a new DynamicPeakWaveForm
	 * @param format The format of the source audio format
	 * @param iSize The interval size to be used
	 */
	public DynamicPeakWaveForm(AudioFormat format, int iSize) {
		super(getHeader(iSize, -1, 0, (short)format.getChannels(), 0));
		try{
			f = File.createTempFile("gmtmp_", ".gmpk");
			raFile = new RandomAccessFile(f, "rw");
			raFile.write(getHeader(iSize, -1, 0, (short)format.getChannels(), 0));
		}
		catch(IOException e){
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		channels = format.getChannels();
		intervallSize = iSize;
		frameSize = format.getFrameSize();
		sampleSize = frameSize / channels;
		bigEndian = format.isBigEndian();
		dataBuffer = new byte[ARRAY_SIZE * 2 * channels];
	}
	/**
	 * Get the index of the peak data at the current position 
	 * @return The index of the peak data for the current position
	 */
	private int getCurrentIndex(){
		return position / ARRAY_SIZE;
	}
	/**
	 * Get the peak data for the current position. This data will either be
	 * read from the peak file or from the pool of peak data
	 * @return The peak data that contains the peak values for the current
	 * position
	 */
	private byte[] getCurrentData(){
		int current = getCurrentIndex();
		if(current == index) return dataBuffer;
		int nullindex = -1;
		int maxdiff = 0;
		int maxindex = 0;
		for (int i = 0; i < positions.length; i++) {
			if(positions[i] == current && datas[i] != null) return datas[i];
			else if(datas[i] == null) nullindex = i;
			else{
				int diff = Math.abs(positions[i] - current);
				if(diff > maxdiff){
					maxindex = i;
					maxdiff = diff;
				}
			}
		}
		
		if(nullindex > -1){
			positions[nullindex] = current;
			datas[nullindex] = new byte[ARRAY_SIZE * channels * 2];
			readDataAt(current, datas[nullindex]);
			return datas[nullindex];
		}
		else{
			positions[maxindex] = current;
			readDataAt(current, datas[maxindex]);
			return datas[maxindex];
		}
	}
	/**
	 * Read peak data from the peak file at the given index 
	 * @param index The index to read
	 * @param b The byte Array to read the data into
	 */
	private void readDataAt(int index, byte[] b){
		try {
			raFile.seek(34 + index * 2 * channels * ARRAY_SIZE);
			int length = b.length;
			int read = 0;
			int tempread = 0;
			while(length > read && tempread != -1){
				tempread = raFile.read(b, read, length - read);
				if(tempread != -1) read += tempread;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public byte getMin(int channel) {
		if(raFile == null) return super.getMin(channel);
		byte[] posData = getCurrentData();
		int p = position % ARRAY_SIZE;
		if(posData == null) return 0;
		else return posData[2 * (p * channels + channel)];
	}
	public byte getMax(int channel) {
		if(raFile == null) return super.getMax(channel);
		byte[] posData = getCurrentData();
		int p = position % ARRAY_SIZE;
		if(posData == null) return 0;
		else return posData[2 * (p * channels + channel) + 1];
	}
	/**
	 * Dynamically append audio data to this WaveForm
	 * @param data The audio data to append
	 * @param offset Start position inside the data Array
	 * @param length The length of data to append
	 */
	public void append(byte[] data, int offset, int length){
		byte temp;
		for(int i = 0; i < length; i+= frameSize){
			if(tempCount == 0) displayWidth++;
			for(int j = 0; j < channels; j++){
				if(bigEndian) temp = data[offset + i + j * sampleSize];
				else temp = data[offset + i + j * sampleSize + sampleSize - 1];
				if(tempCount == 0){
					dataBuffer[dataPos * 2 * channels + 2 * j] = dataBuffer[dataPos * 2 * channels + 2 * j + 1] = temp;
				}
				else{
					dataBuffer[dataPos * 2 * channels + 2 * j] = (byte)Math.min(temp, dataBuffer[dataPos * 2 * channels + 2 * j]);
					dataBuffer[dataPos * 2 * channels + 2 * j + 1] = (byte)Math.max(temp, dataBuffer[dataPos * 2 * channels + 2 * j + 1]);
				}
			}
			tempCount++;
			realLength++;
			// Reached end of one intervall
			if(tempCount == intervallSize){
				tempCount = 0;
				dataPos++;
				// Reached end of one array
				if(dataPos == ARRAY_SIZE){
					try {
						raFile.seek(34 + index * ARRAY_SIZE * 2 * channels);
						raFile.write(dataBuffer);
					} catch (IOException e) {
						e.printStackTrace();
					}
					dataPos = 0;
					index++;
				}
			}
		}
	}
	/**
	 * Close this WaveForm indicating that no more data will be appended. The
	 * header will be written into the file with the given audio file
	 * modification date
	 * @param lastModified The modification date of the audio file this WaveForm
	 * belongs to id it is known, otherwise -1
	 */
	public void close(long lastModified){
		try {
			int snapshots = index * ARRAY_SIZE;
			if(tempCount > 0) dataPos++;
			if(dataPos > 0){
				snapshots += dataPos;
				// write last snapshot
				raFile.seek(34 + index * ARRAY_SIZE * 2 * channels);
				raFile.write(dataBuffer, 0, 2 * channels * dataPos);
			}
			MappedByteBuffer b = raFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, 34);
			b.order(ByteOrder.LITTLE_ENDIAN);
			b.putLong(8, lastModified);
			b.putLong(16, realLength);
			b.putLong(26, snapshots);
			b.force();
			raFile.close();
			raFile = null;
			
			readFromFile(f);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}