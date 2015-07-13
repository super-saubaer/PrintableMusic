package com.groovemanager.sampled.waveform;

/**
 * An updating WaveForm is a WaveForm that ia wrapped around another one and
 * forces to be not re-used by returning false for any call to
 * <code>canProvide()</code>
 * @author Manu Robledo
 *
 */
public class UpdatingWaveForm extends AbstractWaveForm {
	/**
	 * The wrapped WaveForm
	 */
	public WaveForm source;
	/**
	 * Construct a new UpdatingWaveForm
	 * @param source The WaveForm to be wrapped
	 */
	public UpdatingWaveForm(WaveForm source) {
		this.source = source;
	}
	
	public boolean canProvide(int begin, int length, int width) {
		return false;
	}
	
	public byte getMin(int channel) {
		return source.getMin(channel);
	}
	public byte getMax(int channel) {
		return source.getMax(channel);
	}
	protected String getAdditionalToString() {
		return super.getAdditionalToString() + "\tsource: { " + source + "\n\t}\n";
	}
	
	public int getChannels() {
		return source.getChannels();
	}
	
	public int getDisplayableLength() {
		return source.getDisplayableLength();
	}
	
	public int getPosition() {
		return source.getPosition();
	}
	
	public int getRealLength() {
		return source.getRealLength();
	}
	
	public int getRealPosition() {
		return source.getRealPosition();
	}
	
	public double getZoomFactor() {
		return source.getZoomFactor();
	}
	public boolean next() {
		return source.next();
	}
	public void rewind() {
		source.rewind();
	}
	public void setPosition(int pos) {
		source.setPosition(pos);
	}
	public void setRealPosition(int pos) {
		source.setRealPosition(pos);
	}
}