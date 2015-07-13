/*
 * Created on 19.06.2004
 *
 */
package com.groovemanager.sampled.providers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioFileFormat.Type;

/**
 * This implementation of AudioFileFormatProvider reads the AudioFileFormat
 * out of a wave file. It supports "RLND" and "slices" properties
 * @author Manu Robledo
 *
 */
public class WavFormatProvider extends AudioFileFormatProvider {
	/**
	 * Supported types
	 */
	private final static Type[] TYPES = new Type[]{Type.WAVE};
	/**
	 * Supported formats
	 */
	private final static AudioFormat[] FORMATS = createFormats();
	/**
	 * Supported properties
	 */
	private final static String[] PROPERTIES = new String[]{"RLND", "slices"};
	/**
	 * WavFileReader used to delegate the calls
	 */
	private final static WavFileReader reader = new WavFileReader();
	/**
	 * Create the supported AudioFormats
	 * @return Array of supported AudioFormats
	 */
	protected static AudioFormat[] createFormats(){
		/*
		 * Sample rate: NOT_SPECIFIED
		 * Channels: NOT_SPECIFIED
		 * Sample size: 16, 24, 32, 64
		 * Endianess: Little
		 * Encoding: PCM_SIGNED
		 */
		int[] sampleSizes = new int[]{16, 24, 32, 64};
		AudioFormat[] formats = new AudioFormat[sampleSizes.length];
		for (int i = 0; i < formats.length; i++) {
			formats[i] = new AudioFormat(AudioSystem.NOT_SPECIFIED, sampleSizes[i], AudioSystem.NOT_SPECIFIED, true, false);
		}
		return formats;
	}
	/**
	 * @see com.groovemanager.sampled.providers.AudioFileFormatProvider#getSupportedTypes()
	 */
	public Type[] getSupportedTypes() {
		return TYPES;
	}
	/**
	 * @see com.groovemanager.sampled.providers.AudioFileFormatProvider#isFormatSupported(javax.sound.sampled.AudioFormat)
	 */
	public boolean isFormatSupported(AudioFormat format) {
		return (!format.isBigEndian()) && format.getChannels() > 0 && format.getSampleRate() > 0 && format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED) && format.getSampleSizeInBits() >= 16;
	}
	/**
	 * @see com.groovemanager.sampled.providers.AudioFileFormatProvider#getSupportedFormats()
	 */
	public AudioFormat[] getSupportedFormats() {
		return FORMATS;
	}
	/**
	 * @see com.groovemanager.sampled.providers.AudioFileFormatProvider#getSupportedProperties()
	 */
	public String[] getSupportedProperties() {
		return PROPERTIES;
	}
	/**
	 * @see com.groovemanager.sampled.providers.AudioFileFormatProvider#getAudioFileFormat(java.io.File)
	 */
	public AudioFileFormat getAudioFileFormat(File f)
			throws UnsupportedAudioFileException, IOException {
		return reader.getAudioFileFormat(f);
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.providers.AudioFileFormatProvider#isFileSupported(java.io.File)
	 */
	public boolean isFileSupported(File f){
		FileInputStream in = null;
		try {
			in = new FileInputStream(f);
			byte[] b = new byte[44];
			if(in.read(b, 0, 44) < 44){
				in.close();
				return false;
			}
			if(!new String(b, 0, 4).equals("RIFF")){
				in.close();
				return false;
			}
			if(!new String(b, 8, 8).equals("WAVEfmt ")){
				in.close();
				return false;
			}
			ByteBuffer buffer = ByteBuffer.wrap(b, 16, 20).order(ByteOrder.LITTLE_ENDIAN);
			if(buffer.getInt() != 16){
				in.close();
				return false;
			}
			// Encoding
			if(buffer.getShort() != 1){
				in.close();
				return false;
			}
			// Channels
			short channels = buffer.getShort();
			if(channels < 1){
				in.close();
				return false;
			}
			// Sampling Freq.
			int sampleFreq = buffer.getInt();
			if(sampleFreq <= 0){
				in.close();
				return false;
			}
			// Samples/sec.
			buffer.getInt();
			// Bytes/Sample
			short bytesPerSample = buffer.getShort();
			if(bytesPerSample <= 0){
				in.close();
				return false;
			}
			// Bits per sample
			short sampleSize = buffer.getShort();
			if(sampleSize < 16){
				in.close();
				return false;
			}
			if(sampleSize / 8 * channels > bytesPerSample){
				in.close();
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			if(in != null)
				try {
					in.close();
				} catch (IOException e1) {
				}
			return false;
		}
		return true;
	}
}