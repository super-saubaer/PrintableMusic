/*
 * Created on 11.05.2004
 *
 */
package com.groovemanager.sampled.waveform;

/**
 * This WaveForm implementation can be used to concatenate a given number of
 * WaveForms into a new one. The WaveForms must all have the same number of
 * channels. 
 * @author Manu Robledo
 *
 */
public class ConcatWaveForm extends AbstractWaveForm {
	/**
	 * The source WaveForms to concatenate
	 */
	protected WaveForm[] sources;
	/**
	 * Construct a new ConcatWaveForm out of the given WaveForms
	 * @param sources The WaveForms to concatenate. Must all have the same
	 * number of channels
	 * @throws IllegalArgumentException If no WaveForms are supplied or if not
	 * all of the given WaveForms have the same number of channels
	 */
	public ConcatWaveForm(WaveForm[] sources){
		this.sources = sources;
		displayWidth = 0;
		if(sources != null && sources.length > 0){
			channels = sources[0].getChannels();
		}
		else throw new IllegalArgumentException("No WaveForms to Concatenate.");
		for (int i = 0; i < sources.length; i++) {
			if(sources[i].getChannels() != channels) throw new IllegalArgumentException("Different Channel count.");
			displayWidth += sources[i].getDisplayableLength();
			realLength += sources[i].getRealLength();
		}
	}
	public byte getMin(int channel) {
		int pos = position;
		for (int i = 0; i < sources.length; i++) {
			if(pos < sources[i].getDisplayableLength()){
				sources[i].setPosition(pos);
				return sources[i].getMin(channel);
			}
			pos -= sources[i].getDisplayableLength();
		}
		// That shouldn't happen
		return 0;
	}
	public byte getMax(int channel) {
		int pos = position;
		for (int i = 0; i < sources.length; i++) {
			if(pos < sources[i].getDisplayableLength()){
				sources[i].setPosition(pos);
				return sources[i].getMax(channel);
			}
			pos -= sources[i].getDisplayableLength();
		}
		// That shouldn't happen
		return 0;
	}
}