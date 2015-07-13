/*
 * Created on 31.05.2004
 *
 */
package com.groovemanager.sampled.waveform;

/**
 * A WaveFormProvider acts as a source for a WaveFormDisplay. Each time the
 * WaveFormDisplay needs to be updated, it asks, its provider for the WaveForm.
 * @author Manu Robledo
 *
 */
public interface WaveFormProvider {
	/**
	 * Get a WaveForm for the specified display
	 * @param display The WaveFormDisplay that asks for the WaveForm
	 * @param start The start position of the WaveForm inside the source data
	 * in sample frames
	 * @param length The length of the audio data that should be represented by
	 * the WaveForm
	 * @param width The width of the resulting WaveForm.
	 * @return A WaveForm representing <code>length</code> sample frames of the
	 * provider큦 audio data beginning at position <code>start</code> to be
	 * displayed in the width of <code>width</code>. 
	 */
	public WaveForm getWaveForm(AbstractWaveFormDisplay display, int start, int length, int width);
	/**
	 * Get the channels of the audio data represented by the provider
	 * @return The provider큦 audio data큦 channels
	 */
	public int getChannels();
	/**
	 * Get the total length of the audio data represented by the provider in
	 * sample frames
	 * @return The provider큦 audio data큦 length in sample frames
	 */
	public int getTotalLength();
}
