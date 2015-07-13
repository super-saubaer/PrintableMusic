/*
 * Created on 17.06.2004
 *
 */
package com.groovemanager.sampled.providers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;

/**
 * A simple WavFileReader implementation for support of additional headers
 * @author Manu Robledo
 *
 */
public class WavFileReader extends AudioFileReader {
	/**
	 * supported chunk IDs of additional chunks
	 */
	protected static String[] supportedChunks = new String[]{"slic", "RLND"};
	/**
	 * @see javax.sound.sampled.spi.AudioFileReader#getAudioFileFormat(java.io.InputStream)
	 */
	public AudioFileFormat getAudioFileFormat(InputStream stream)
			throws UnsupportedAudioFileException, IOException {
		byte[] temp = new byte[12];
		ByteBuffer buffer = ByteBuffer.wrap(temp).order(ByteOrder.LITTLE_ENDIAN);
		stream.mark(36);
		stream.read(temp, 0, 12);
		if(!"RIFF".equals(new String(temp, 0, 4))){
			stream.reset();
			throw new UnsupportedAudioFileException("No RIFF header found.");
		}
		if(!"WAVE".equals(new String(temp, 8, 4))){
			stream.reset();
			throw new UnsupportedAudioFileException("No WAVE header found.");
		}
		
		int numBytesRead = 0;
		int datalength = 0;
		Map properties = new HashMap();;
		
		numBytesRead = stream.read(temp, 0, 8);
		if(numBytesRead < 8 || (!new String(temp, 0, 4).equals("fmt ")) || buffer.getInt(4) != 16){
			stream.reset();
			throw new UnsupportedAudioFileException("No correct fmt-header found");
		}
		AudioFormat audioFormat = readFormatHeader(stream);
		
		while(numBytesRead > -1){
			numBytesRead = stream.read(temp, 0, 8);
			if(numBytesRead < 8) numBytesRead = -1;
			else{
				String chunkName = new String(temp, 0, 4);
				int length = buffer.getInt(4);
				if(chunkName.equals("data")){
					datalength += length / audioFormat.getFrameSize();
					if(length % 2 > 0) length++;
					while(length > 0){
						int skipped = (int)stream.skip(length);
						if(skipped == -1) length = 0;
						else length -= skipped;
					}
				}
				else{
					boolean supported = false;
					for (int i = 0; i < supportedChunks.length && !supported; i++) {
						if(chunkName.equals(supportedChunks[i])){
							properties.putAll(processChunk(stream, chunkName, length));
							supported = true;
						}
					}
					if(!supported){
						if(length % 2 > 0) length++;
						stream.skip(length);
					}
				}
			}
		}
		return new WavAudioFileFormat(audioFormat, datalength, properties); 
	}
	/**
	 * Process a chunk and get the properties known out of it, if any
	 * @param stream The InputStream to read from
	 * @param chunkName The chunk ID
	 * @param length The chunk length
	 * @return A Map of properties found in the given chunk 
	 * @throws IOException If an I/O Error occured during reading
	 */
	protected static Map processChunk(InputStream stream, String chunkName, int length) throws IOException{
		Map m = new HashMap();
		if(chunkName.equals("slic")){
			byte[] b = new byte[length];
			if(stream.read(b, 0, length) != length) throw new IOException("Chunk length invalid");
			SliceChunk chunk = new SliceChunk(b);
			m.put("slice_count", new Integer(chunk.getSliceCount()));
			m.put("slices", chunk.getSlices());
		}
		else if(chunkName.equals("RLND")){
			byte[] b = new byte[length];
			if(length != 196 || stream.read(b, 0, length) != length) throw new IOException("Chunk length invalid");
			RLNDChunk chunk = new RLNDChunk(b);
			m.put("bpm", new Float(chunk.getBPM()));
			m.put("RLND", chunk);
		}
		return m;
	}
	/**
	 * Get the AudioFormat out of the given InputStream
	 * @param in The InputStream to read from. The next 16 bytes should be the
	 * data of the fmt-chunk
	 * @return The AudioFormat of the file
	 * @throws IOException If an I/O Error occurs during reading
	 * @throws UnsupportedAudioFileException If the format is invalid
	 */
	protected static AudioFormat readFormatHeader(InputStream in) throws IOException, UnsupportedAudioFileException{
		byte[] header = new byte[16];
		ByteBuffer buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
		if(in.read(header) < 16){
			in.reset();
			throw new UnsupportedAudioFileException("fmt-header corrupted.");
		}
		
		// Encoding
		if(buffer.getShort() != 1){
			in.reset();
			throw new UnsupportedAudioFileException("Only PCM encoding is supported.");
		}
		// Channels
		short channels = buffer.getShort();
		if(channels < 1){
			in.reset();
			throw new UnsupportedAudioFileException("Invalid channels: " + channels);
		}
		// Sampling Freq.
		int sampleFreq = buffer.getInt();
		if(sampleFreq <= 0){
			in.reset();
			throw new UnsupportedAudioFileException("Invalid sampling frequency: " + sampleFreq);
		}
		// Samples/sec.
		buffer.getInt();
		// Bytes/Sample
		short bytesPerSample = buffer.getShort();
		if(bytesPerSample <= 0){
			in.reset();
			throw new UnsupportedAudioFileException("Invalid bytes per sample: " + bytesPerSample);
		}
		// Bits per sample
		short sampleSize = buffer.getShort();
		if(sampleSize < 16){
			in.reset();
			throw new UnsupportedAudioFileException("Only sample sizes of 16 Bit and higher are supported");
		}
		if(sampleSize / 8 * channels > bytesPerSample){
			in.reset();
			throw new UnsupportedAudioFileException("Sample size ("+sampleSize+") exceeds bytes per sample ("+bytesPerSample+")");
		}
		// Ready
		return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleFreq, sampleSize, channels, bytesPerSample, sampleFreq, false);
	}
	public AudioFileFormat getAudioFileFormat(URL url)
			throws UnsupportedAudioFileException, IOException {
		return getAudioFileFormat(new BufferedInputStream(url.openStream()));
	}
	public AudioFileFormat getAudioFileFormat(File file)
			throws UnsupportedAudioFileException, IOException {
		return getAudioFileFormat(new BufferedInputStream(new FileInputStream(file)));
	}
	public AudioInputStream getAudioInputStream(InputStream stream)
			throws UnsupportedAudioFileException, IOException {
		byte[] temp = new byte[12];
		ByteBuffer buffer = ByteBuffer.wrap(temp).order(ByteOrder.LITTLE_ENDIAN);
		stream.mark(36);
		stream.read(temp, 0, 12);
		if(!"RIFF".equals(new String(temp, 0, 4))){
			stream.reset();
			throw new UnsupportedAudioFileException("No RIFF header found.");
		}
		if(!"WAVE".equals(new String(temp, 8, 4))){
			stream.reset();
			throw new UnsupportedAudioFileException("No WAVE header found.");
		}
		
		int numBytesRead = 0;
		Map properties = new HashMap();;
		
		numBytesRead = stream.read(temp, 0, 8);
		if(numBytesRead < 8 || (!new String(temp, 0, 4).equals("fmt ")) || buffer.getInt(4) != 16){
			stream.reset();
			throw new UnsupportedAudioFileException("No correct fmt-header found");
		}
		AudioFormat audioFormat = readFormatHeader(stream);
		
		while(numBytesRead > -1){
			numBytesRead = stream.read(temp, 0, 8);
			if(numBytesRead < 8) numBytesRead = -1;
			else{
				String chunkName = new String(temp, 0, 4);
				int length = buffer.getInt(4);
				if(chunkName.equals("data")){
					return new AudioInputStream(stream, audioFormat, length / audioFormat.getFrameSize());
				}
				else{
					if(length % 2 > 0) length++;
					stream.skip(length);
				}
			}
		}
		throw new UnsupportedAudioFileException("No audio data found");
	}
	/**
	 * @see javax.sound.sampled.spi.AudioFileReader#getAudioInputStream(java.net.URL)
	 */
	public AudioInputStream getAudioInputStream(URL url)
			throws UnsupportedAudioFileException, IOException {
		return getAudioInputStream(new BufferedInputStream(url.openStream()));
	}
	/**
	 * @see javax.sound.sampled.spi.AudioFileReader#getAudioInputStream(java.io.File)
	 */
	public AudioInputStream getAudioInputStream(File file)
			throws UnsupportedAudioFileException, IOException {
		return getAudioInputStream(new BufferedInputStream(new FileInputStream(file)));
	}
}
