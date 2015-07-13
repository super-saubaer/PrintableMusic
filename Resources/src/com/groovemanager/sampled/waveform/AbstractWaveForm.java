/*
 * Created on 11.05.2004
 *
 */
package com.groovemanager.sampled.waveform;

/**
 * An abstract superclass for implementation of IWaveForm
 * @author Manu Robledo
 *
 */
public abstract class AbstractWaveForm implements WaveForm {
	/**
	 * Total displayable width of this WaveForm 
	 */
	protected int displayWidth,
	/**
	 * Current read position inside this WaveForm
	 */
	position,
	/**
	 * Length of the represented audio data
	 */
	realLength,
	/**
	 * Number of channels of the represented WaveForm
	 */
	channels;
	/**
	 * An empty WaveForm to be reused when needed
	 */
	protected static WaveForm nullWave = new EmptyWaveForm();
	/**
	 * The intervall size. May be -1 to indicate that the intervall size should
	 * be calculated out of <code>getRealLength()</code> and
	 * <code>getDisplayableLength()</code>. 
	 */
	protected int intervallSize = -1;
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.WaveForm#setRealPosition(int)
	 */
	public void setRealPosition(int pos){
		position = (int)Math.round(pos / getZoomFactor());
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.WaveForm#getRealPosition()
	 */
	public int getRealPosition(){
		return (int)Math.round(position * getZoomFactor());
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.WaveForm#setPosition(int)
	 */
	public void setPosition(int pos){
		position = pos;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.WaveForm#getPosition()
	 */
	public int getPosition(){
		return position;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.WaveForm#rewind()
	 */
	public void rewind() {
		position = 0;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.WaveForm#next()
	 */
	public boolean next(){
		if(position + 1 >= displayWidth) return false;
		
		position ++;
		return true;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.WaveForm#subWaveForm(int, int, int)
	 */
	public WaveForm subWaveForm(int begin, int length, int width) {
		if(width < 0){
			System.out.println();
		}
		if(width >= 0 && length >= 0){
			double temp = length / (double)width;
			WaveForm w = this;
			if(begin != 0 || length != getDisplayableLength()) w = new WaveFormPart(w, begin, length);
			if(length != width) w = PeakWaveForm.getPeak(w, width, 1000);
			if(w.getDisplayableLength() < width) w = new FillUpWaveForm(w, 0, width);
			return w;
		}
		return nullWave;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.WaveForm#getRealLength()
	 */
	public int getRealLength(){
		return realLength;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.WaveForm#getDisplayableLength()
	 */
	public int getDisplayableLength(){
		return displayWidth;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.WaveForm#canProvide(int, int, int)
	 */
	public boolean canProvide(int begin, int length, int width){
		return begin >= 0 && begin + length <= getDisplayableLength() && width <= length;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.WaveForm#getZoomFactor()
	 */
	public double getZoomFactor(){
		return getRealLength() / (double)getDisplayableLength();
	}
	/**
	 * Get an additional String describing this WaveForm. Will be used in
	 * <code>toString()</code>
	 * @return An optional String describing special characteristics of this
	 * WaveForm
	 */
	protected String getAdditionalToString(){
		return "";
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.WaveForm#getChannels()
	 */
	public int getChannels(){
		return channels;
	}
	/**
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return super.toString() + "{\n" +
				"\tend: " + getDisplayableLength() + "\n" +
				"\tzoomFactor: " + getZoomFactor() + "\n" + 
				"\treal Length: " + getRealLength() + "\n" +
				getAdditionalToString() +
				"}";
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.WaveForm#getIntervallSize()
	 */
	public int getIntervallSize(){
		if(intervallSize == -1){
			int i = getRealLength() / getDisplayableLength();
			if(getRealLength() % getDisplayableLength() > 0) i++;
			return i;
		}
		else return intervallSize;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.WaveForm#getData()
	 */
	public byte[] getData(){
		int length = getDisplayableLength();
		int channels = getChannels();
		byte[] data = new byte[length * 2 * channels];
		rewind();
		for(int i = 0; i < length; i++){
			setPosition(i);
			for(int j = 0; j < channels; j++){
				data[2 * (i * channels + j)] = getMin(j);
				data[2 * (i * channels + j) + 1] = getMax(j);
			}
		}
		return data;
	}
}