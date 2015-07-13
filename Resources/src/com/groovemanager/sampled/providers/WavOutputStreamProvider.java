/*
 * Created on 19.06.2004
 *
 */
package com.groovemanager.sampled.providers;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioFileFormat.Type;

/**
 * This AudioFileOutputStreamProvider implementation provides wave file
 * writing services.
 * @author Manu Robledo
 *
 */
public class WavOutputStreamProvider extends AudioFileOutputStreamProvider {
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
	 * Create the supported AudioFormats
	 * @return The supported AudioFormats
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
	 * 
	 * @see com.groovemanager.sampled.providers.AudioFileOutputStreamProvider#getSupportedTypes()
	 */
	public Type[] getSupportedTypes() {
		return TYPES;
	}
	/**
	 * @see com.groovemanager.sampled.providers.AudioFileOutputStreamProvider#getSupportedFormats()
	 */
	public AudioFormat[] getSupportedFormats() {
		return FORMATS;
	}
	/**
	 * @see com.groovemanager.sampled.providers.AudioFileOutputStreamProvider#getSupportedProperties()
	 */
	public String[] getSupportedProperties() {
		return PROPERTIES;
	}
	/**
	 * @see com.groovemanager.sampled.providers.AudioFileOutputStreamProvider#getAudioFileOutputStream(java.io.File, javax.sound.sampled.AudioFileFormat.Type, javax.sound.sampled.AudioFormat, java.util.Map)
	 */
	public AudioFileOutputStream getAudioFileOutputStream(File f, Type type,
			AudioFormat format, Map properties)
			throws UnsupportedAudioFileException, IOException {
		return new WAV909OutputStream(f, format, properties);
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.providers.AudioFileOutputStreamProvider#isFormatSupported(javax.sound.sampled.AudioFormat)
	 */
	public boolean isFormatSupported(AudioFormat format) {
		return (!format.isBigEndian()) && format.getChannels() > 0 && format.getSampleRate() > 0 && format.getEncoding().toString().equals(AudioFormat.Encoding.PCM_SIGNED.toString()) && format.getSampleSizeInBits() >= 16;
	}
}
