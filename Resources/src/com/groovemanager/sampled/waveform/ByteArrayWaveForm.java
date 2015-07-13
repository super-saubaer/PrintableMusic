/*
 * Created on 11.05.2004
 *
 */
package com.groovemanager.sampled.waveform;

import javax.sound.sampled.AudioFormat;


/**
 * A ByteArrayWaveForm reads its displayable data directly from a given array
 * of audio data. This can be very memory consuming so that ByteArrayWaveForms
 * should only be used for small amounts of audio data.
 * This implementation will only read the most signifigant byte of each sample.
 * For AudioFormats where this byte doesn´t represent more or less the real
 * amplitude, this implementation should not be used. 
 * @author Manu Robledo
 *
 */
public class ByteArrayWaveForm extends AbstractWaveForm{
	/**
	 * The audio data
	 */
	protected byte[] data;
	/**
	 * The format of the audio data
	 */
	protected AudioFormat format;
	/**
	 * The frame size of the audio format
	 */
	protected int fSize,
	/**
	 * The sample size of the audio format
	 */
	sSize;
	/**
	 * Construct a new ByteArrayWaveForm
	 * @param data The audio data to display
	 * @param format The format of the given audio data
	 */
	public ByteArrayWaveForm(byte[] data, AudioFormat format){
		if(data == null || format == null) throw new IllegalArgumentException("Argument is null.");
		this.data = data;
		this.format = format;
		fSize = format.getFrameSize();
		sSize = fSize / format.getChannels();
		displayWidth = realLength = data.length / fSize;
		channels = format.getChannels();
	}
	public byte getMin(int channel) {
		if(format.isBigEndian()) return data[fSize * position + channel * sSize];
		else return data[fSize * position + channel * sSize + sSize - 1];
	}
	public byte getMax(int channel) {
		return getMin(channel);
	}
}
