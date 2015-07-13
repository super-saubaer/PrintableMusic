/*
 * Created on 19.05.2004
 *
 */
package com.groovemanager.sampled.waveform;

/**
 * This WaveForm implementaion represents an empty WaveForm that has no data
 * to display
 * @author Manu Robledo
 *
 */
public class EmptyWaveForm implements WaveForm {
	public int getChannels() {
		return 0;
	}
	public void setChannel(int channel) {
	}
	public void setRealPosition(int pos) {
	}
	public int getRealPosition() {
		return 0;
	}
	public void setPosition(int pos) {
	}
	public int getPosition() {
		return 0;
	}
	public byte getMin(int channel) {
		return 0;
	}
	public byte getMax(int channel) {
		return 0;
	}
	public void rewind() {
	}
	public boolean next() {
		return false;
	}
	public WaveForm subWaveForm(int begin, int length, int width) {
		return this;
	}
	public int getRealLength() {
		return 0;
	}
	public int getDisplayableLength() {
		return 0;
	}
	public double getZoomFactor() {
		return 1;
	}
	public boolean canProvide(int begin, int length, int width) {
		return false;
	}
	public int getIntervallSize(){
		return 1;
	}
	public byte[] getData() {
		return new byte[0];
	}
}