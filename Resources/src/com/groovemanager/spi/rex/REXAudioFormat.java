/*
 * Created on 13.06.2004
 *
 */
package com.groovemanager.spi.rex;

import javax.sound.sampled.AudioFormat;

/**
 * This class represents the format in which the audio data is provided in a
 * REX file. REX files always use PCM_FLOAT encoding (32 Bit).
 * @author Manu Robledo
 */
public class REXAudioFormat extends AudioFormat {
	/**
	 * Constructs a new REXAudioFormat
	 * @param sampleRate The Sample rate of the audio data
	 * @param channels The number of channels
	 * @param bigEndian true if the data is stored in big endian format,
	 * false otherwise
	 */
	public REXAudioFormat(float sampleRate, int channels, boolean bigEndian) {
		super(Encoding.PCM_FLOAT, sampleRate, 32, channels, channels * 4, sampleRate, bigEndian);
	}
	//For 1.4 compatibility...
	/**
	 * Encoding class for PCM_FLOAT
	 * @author Manu Robledo
	 *
	 */
	static class Encoding extends AudioFormat.Encoding{
		/**
		 * PCM_FLOAT is an encoding where all samples are stored as floating
		 * point values normalized to +/- 1.0, either in 32 or 64 bit sample
		 * size
		 */
		public final static Encoding PCM_FLOAT = new Encoding();
		/**
		 * Construct a new PCM_FLOAT encoding
		 *
		 */
		protected Encoding(){
			super("PCM_FLOAT");
		}
	}
}
