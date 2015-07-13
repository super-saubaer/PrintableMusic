/*
 * Created on 19.06.2004
 *
 */
package com.groovemanager.spi.rex;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioFileFormat.Type;

import com.groovemanager.sampled.providers.AudioFileFormatProvider;

/**
 * A REXFormatProvider can read AudioFileFormat out of REX files
 * @author Manu Robledo
 *
 */
public class REXFormatProvider extends AudioFileFormatProvider {
	/**
	 * The types supported by tis provider
	 */
	private final static Type[] TYPES = new Type[]{REXAudioFileFormat.Type.REX};
	/**
	 * The formats supported by this provider
	 */
	final static AudioFormat[] FORMATS = new AudioFormat[]{
		new REXAudioFormat(AudioSystem.NOT_SPECIFIED, 1, true),
		new REXAudioFormat(AudioSystem.NOT_SPECIFIED, 2, true),
		new REXAudioFormat(AudioSystem.NOT_SPECIFIED, 1, false),
		new REXAudioFormat(AudioSystem.NOT_SPECIFIED, 2, false)
	}; 
	/**
	 * The property keys supported by this provider
	 */
	final static String[] PROPERTIES = new String[]{
		"duration",
		"author",
		"copyright",
		"comment",
		"slice_count",
		"slices",
		"bpm",
		"time_signature"
	};
	/**
	 * The REXFileReader to be used for delegating calls
	 */
	final static REXFileReader reader = new REXFileReader();
	/**
	 * @see com.groovemanager.sampled.providers.AudioFileFormatProvider#getSupportedTypes()
	 */
	public Type[] getSupportedTypes() {
		return TYPES;
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
	 * @see com.groovemanager.sampled.providers.AudioFileFormatProvider#isFileSupported(java.io.File)
	 */
	public boolean isFileSupported(File f) {
		try {
			reader.getAudioFileFormat(f);
			return true;
		} catch (UnsupportedAudioFileException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}
}
