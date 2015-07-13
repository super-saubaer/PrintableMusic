package com.groovemanager.sampled.waveform;

/**
 * A ZoomedWaveForm is a WaeForm that displays the data of another WaveForm in
 * a smaller width.
 * @author Manu Robledo
 *
 */
public class ZoomedWaveForm extends AbstractWaveForm {
	/**
	 * The source WaveForm
	 */
	protected WaveForm source;
	/**
	 * The zoom factor. Reoresents the relation between the source´s width and
	 * this WaveForm´s width. A factor of 2.0 for example means that the source
	 * WaveForm´s width is two times the width of this WaveForm.
	 */
	protected double factor;
	/**
	 * Create a new ZoomedWaveForm in the given width
	 * @param source The source WaveForm
	 * @param newWidth The width of the new WaveForm
	 */
	public ZoomedWaveForm(WaveForm source, int newWidth){
		this.source = source;
		channels = source.getChannels();
		displayWidth = newWidth;
		factor = source.getDisplayableLength() / (double)displayWidth;
	}
	public int getRealLength() {
		return source.getRealLength();
	}
	public byte getMin(int channel) {
		source.setPosition((int)Math.round(position * factor));
		byte min = source.getMin(channel);
		while(source.next() && source.getPosition() / factor < position + 1){
			min = (byte)Math.min(min, source.getMin(channel));
		}
		return min;
	}
	public byte getMax(int channel) {
		source.setPosition((int)Math.round(position * factor));
		byte max = source.getMax(channel);
		while(source.next() && source.getPosition() / factor < position + 1){
			max = (byte)Math.max(max, source.getMax(channel));
		}
		return max;
	}
	public boolean canProvide(int begin, int length, int width) {
		return source.canProvide((int)Math.round(factor * begin), (int)Math.round(factor * length), width);
	}
	public WaveForm subWaveForm(int begin, int length, int width) {
		if(begin == 0 && length > 0 && length <= source.getDisplayableLength() / factor && width <= source.getDisplayableLength() && width > 0){
			displayWidth = width;
			factor *= length / (double)displayWidth;
			return this;
		}
		else return source.subWaveForm((int)Math.round(factor * begin), (int)Math.round(factor * length), width);
	}
	protected String getAdditionalToString() {
		return super.getAdditionalToString() + "\tsource: { " + source + "\n\t}\n";
	}
}