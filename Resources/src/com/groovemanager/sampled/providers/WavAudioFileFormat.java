/*
 * Created on 17.06.2004
 *
 */
package com.groovemanager.sampled.providers;

import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;

/**
 * This class represents an AudioFileFormat for wav e files
 * @author Manu Robledo
 *
 */
public class WavAudioFileFormat extends AudioFileFormat {
	/**
	 * Map of properties assigned to this format. (For compatibility to 1.4)
	 */
	protected Map properties = new HashMap();
	/**
	 * Construct a new WavAudioFileFormat
	 * @param byteLength The bytelength of the audio file
	 * @param format The AudioFormat of the data
	 * @param frameLength The length of the data in sample frames
	 */
	public WavAudioFileFormat(int byteLength, AudioFormat format, int frameLength) {
		super(Type.WAVE, byteLength, format, frameLength);
	}
	/**
	 * Construct a new WavAudioFileFormat
	 * @param format The AudioFormat of the data
	 * @param frameLength The length of the data in sample frames
	 */
	public WavAudioFileFormat(AudioFormat format, int frameLength) {
		super(Type.WAVE, format, frameLength);
	}
	/**
	 * Construct a new WavAudioFileFormat
	 * @param format The AudioFormat of the data
	 * @param frameLength The length of the data in sample frames
	 * @param properties Map of additional properties of this format
	 */
	public WavAudioFileFormat(AudioFormat format, int frameLength, Map properties) {
		super(Type.WAVE, format, frameLength);
		this.properties = properties;
	}
	/**
	 * @see javax.sound.sampled.AudioFileFormat#properties()
	 */
	public Map properties() {
		return properties;
	}
	/**
	 * @see javax.sound.sampled.AudioFileFormat#getProperty(java.lang.String)
	 */
	public Object getProperty(String s) {
		return properties.get(s);
	}
}
