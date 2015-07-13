/*
 * Created on 11.05.2004
 *
 */
package com.groovemanager.sampled.waveform;

/**
 * A WaveFormPart stands for a given part of another WaveForm
 * @author Manu Robledo
 *
 */
public class WaveFormPart extends AbstractWaveForm {
	/**
	 * The WaveForm of which this WaveForm represents a part
	 */
	protected WaveForm source;
	/**
	 * Start position inside the source WaveForm
	 */
	protected int offset = 0;
	/**
	 * Construct a new WaveFormPart
	 * @param source The source WaveForm
	 * @param begin Start position inside the source WaveForm
	 * @param length The length of this part
	 */
	public WaveFormPart(WaveForm source, int begin, int length){
		this.source = source;
		displayWidth = length;
		realLength = (int)Math.round(length * source.getZoomFactor());
		channels = source.getChannels();
		offset = begin;
	}
	public byte getMin(int channel) {
		source.setPosition(offset + position);
		return source.getMin(channel);
	}
	public byte getMax(int channel) {
		source.setPosition(offset + position);
		return source.getMax(channel);
	}
	public WaveForm subWaveForm(int begin, int length, int width) {
		if(offset + begin >= 0 && offset + begin + length <= source.getDisplayableLength() && length > 0 && width > 0 && width == length){
			System.out.println(getClass() + " subWaveForm");
			offset += begin;
			displayWidth = width;
			realLength = (int)Math.round(length * source.getZoomFactor());
			return this;
		}
		else return source.subWaveForm(offset + begin, length, width);
	}
	public boolean canProvide(int begin, int length, int width) {
		return source.canProvide(offset + begin, length, width);
	}
	protected String getAdditionalToString() {
		return "\toffset: " + offset + "\n" +
		"\tsource: " + source.toString() + "\n";
	}
}