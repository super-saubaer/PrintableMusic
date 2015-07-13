/*
 * Created on 19.05.2004
 *
 */
package com.groovemanager.sampled.waveform;

/**
 * This WaveForm implementation is used to add a given amount of silence to a
 * given WaveForm
 * @author Manu Robledo
 *
 */
public class FillUpWaveForm extends AbstractWaveForm {
	/**
	 * The source WaveForm
	 */
	protected WaveForm source;
	/**
	 * The lenght of the silence to add before the source WaveForm
	 */
	protected int offset;
	/**
	 * Create a new FillUpWaveForm
	 * @param source The WaveForm to surround with silence
	 * @param offset The length of the silence before the source WaveForm
	 * @param width The total length of the resulting WaveForm, that is
	 * <code>offset + source.getDisplayableLength() + </code>[length of silence
	 * after the source WaveForm]
	 */
	public FillUpWaveForm(WaveForm source, int offset, int width){
		this.source = source;
		displayWidth = width;
		this.offset = offset;
	}
	public int getRealLength() {
		return (int)Math.round(source.getRealLength() + (getDisplayableLength() - source.getDisplayableLength()) * source.getZoomFactor());
	}
	public byte getMin(int channel) {
		if(position <= offset || position - offset >= source.getDisplayableLength()) return 0;
		
		source.setPosition(position - offset);
		return source.getMin(channel);
	}
	public byte getMax(int channel) {
		if(position <= offset || position - offset >= source.getDisplayableLength()) return 0;
		
		source.setPosition(position - offset);
		return source.getMax(channel);
	}
	public boolean canProvide(int begin, int length, int width) {
		return source.canProvide(begin, length, width);
	}
	public WaveForm subWaveForm(int begin, int length, int width) {
		return source.subWaveForm(begin, length, width);
	}
	protected String getAdditionalToString() {
		return super.getAdditionalToString() + "\tsource: { " + source +  "\n\t}\n";
	}
}