/*
 * Created on 23.05.2004
 *
 */
package com.groovemanager.sampled.waveform;

/**
 * A WaveDisplayListener can be added to WaveFormDisplay to be notified when
 * the view changes, this is when the zoom or scroll factor is changed
 * @author Manu Robledo
 *
 */
public interface WaveDisplayListener {
	/**
	 * Notification about a zoom or scroll change
	 * @param display The WaveFormDisplay that was changed
	 * @param zoom The new zoom factor
	 * @param scroll The new scroll factor
	 */
	public void viewChanged(AbstractWaveFormDisplay display, double zoom, double scroll);
}
