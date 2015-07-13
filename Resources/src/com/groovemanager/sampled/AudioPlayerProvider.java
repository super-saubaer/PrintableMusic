/*
 * Created on 14.05.2004
 *
 */
package com.groovemanager.sampled;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import com.groovemanager.exception.NotReadyException;

/**
 * An AudioPlayerProvider represents any kind of audio source that can present
 * audio data to an AudioPlayer to be played and/or can process recorded audio
 * data in any way.
 * @author Manu Robledo
 *
 */
public interface AudioPlayerProvider {
	/**
	 * Provide an AudioInputStream for playback
	 * @return The AudioInputStream to be played back
	 * @throws NotReadyException If no audio data can be presented at the
	 * moment
	 */
	public AudioInputStream getAudioInputStream() throws NotReadyException;
	/**
	 * Get the last start position inside the whole audio source. Is needed
	 * for correct displaying of The play position and will usually be called
	 * shortly after <code>getAudioInputStream()</code>
	 * @return The frame position inside the audio source represented by this
	 * provider, at which the provided AudioInputStream starts.
	 */
	public int getLastStart();
	/**
	 * Start recording of audio data. The AudioFormat in which the the data will
	 * be processed from this provider is returned to give the AudioPlayer a
	 * hint. If the AudioPlayer can not find a way to convert the audio data
	 * he gets into this format, recording will not start.
	 * @return The AudioFormat in which this provider processes the recorded
	 * data.
	 * @throws NotReadyException If recording is not possible at the moment
	 */
	public AudioFormat startRec() throws NotReadyException;
	/**
	 * Notification from the AudioPlayer that recording has finished.
	 *
	 */
	public void stopRec();
	/**
	 * Process a recorded buffer of audio data. This method will be called
	 * continuously between <code>startRec()</code> and <code>stopRec()</code>.
	 * The delivered audio data will be in the format returned by
	 * <code>startRec()</code>.
	 * @param b The Array containing the audio data to process
	 * @param offset Offset position inside the Array where the usable audio
	 * data begins
	 * @param length The length of the usable audio data in bytes
	 * @return The number of bytes processed. If this value is less then 
	 * <code>length</code>, the AudioPlayer will re-attempt to write the
	 * remaining data in the next call.
	 */
	public int rec(byte[] b, int offset, int length);
	/**
	 * Ask this provider, if he is ready for providing playback audio data
	 * @return true, if audio data can provided, false otherwise
	 */
	public boolean canProvide();
	/**
	 * Ask this provider, if he can provide audio data in a loop. If so, he
	 * will be asked for a new AudioInputStream after each loop end.
	 * @return true, if this provider can provide its audio data in a loop,
	 * false otherwise
	 */
	public boolean canLoop();
	/**
	 * Ask this provider, if he can process recorded audio data in any way.
	 * @return true, if this provider can currently process recorded audio
	 * data, false otherwise
	 */
	public boolean canRec();
}