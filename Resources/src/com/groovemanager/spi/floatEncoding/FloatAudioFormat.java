/*
 * Created on 08.06.2004
 *
 */
package com.groovemanager.spi.floatEncoding;

import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;

/**
 * This class provides some easier constructors for AdioFormats using the
 * PCM_FLOAT encoding
 * @author Manu Robledo
 *
 */
public class FloatAudioFormat extends AudioFormat {
	/**
	 * The properties Map
	 */
	Map properties = new HashMap();
	/**
	 * Construct a new FloatAudioFormat
	 * @param sampleRate The sample rate of the format in Hz
	 * @param sampleSizeInBits The sample size in Bits. Must be either 32 or
	 * 64
	 * @param channels The number of channels of this format
	 * @param bigEndian true, if the data is aligned in big endian, false
	 * otherwise
	 */
	private FloatAudioFormat(float sampleRate,
			int sampleSizeInBits, int channels, boolean bigEndian) {
		super(Encoding.PCM_FLOAT, sampleRate, sampleSizeInBits, channels, sampleSizeInBits / 8 * channels,
				sampleRate, bigEndian);
	}
	/**
	 * Construct a new FloatAudioFormat
	 * @param sampleRate The sample rate of the format in Hz
	 * @param sampleSizeInBits The sample size in Bits. Must be either 32 or
	 * 64
	 * @param channels The number of channels of this format
	 * @param bigEndian true, if the data is aligned in big endian, false
	 * otherwise
	 * @param properties Map of properties assigned to this AudioFormat
	 */
	private FloatAudioFormat(float sampleRate, int sampleSizeInBits, int channels, boolean bigEndian, Map properties){
		super(Encoding.PCM_FLOAT, sampleRate, sampleSizeInBits, channels, sampleSizeInBits / 8 * channels, sampleRate, bigEndian);
		this.properties = properties;
	}
	/**
	 * Construct a new FloatAudioFormat
	 * @param sampleRate The sample rate of the format in Hz
	 * @param isDouble true, if the floating point samples are stored in double
	 * precidion (64 Bit), false otherwise
	 * @param channels The number of channels of this format
	 * @param bigEndian true, if the data is aligned in big endian, false
	 * otherwise
	 */
	public FloatAudioFormat(float sampleRate, boolean isDouble,
			int channels, boolean bigEndian) {
		this(sampleRate, isDouble ? 64 : 32, channels, bigEndian);
	}
	/**
	 * Construct a new FloatAudioFormat
	 * @param sampleRate The sample rate of the format in Hz
	 * @param isDouble true, if the floating point samples are stored in double
	 * precidion (64 Bit), false otherwise
	 * @param channels The number of channels of this format
	 * @param bigEndian true, if the data is aligned in big endian, false
	 * otherwise
	 * @param properties Map of properties assigned to this AudioFormat
	 */
	public FloatAudioFormat(float sampleRate, boolean isDouble,
			int channels, boolean bigEndian, Map properties) {
		this(sampleRate, isDouble ? 64 : 32, channels, bigEndian, properties);
	}
	/**
	 * 
	 * @see javax.sound.sampled.AudioFormat#properties()
	 */
	public Map properties() {
		return properties;
	}
	/**
	 * @see javax.sound.sampled.AudioFormat#getProperty(java.lang.String)
	 */
	public Object getProperty(String key) {
		return properties.get(key);
	}
	/**
	 * PCM_FLOAT encoding
	 * @author Manu Robledo
	 *
	 */
	public static class Encoding extends AudioFormat.Encoding{
		/**
		 * PCM_FLOAT represents a PCM encoding where all samples are stored as
		 * floating point values normalized to +/- 1.0, either in 32 or 64 bit
		 * sample size
		 */
		public final static Encoding PCM_FLOAT = new Encoding();
		/**
		 * Construct a new PCM_FLOAT encoding
		 *
		 */
		private Encoding(){
			super("PCM_FLOAT");
		}
	}
}