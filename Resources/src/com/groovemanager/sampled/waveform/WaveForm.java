/*
 * Created on 11.05.2004
 *
 */
package com.groovemanager.sampled.waveform;

/**
 * A WaveForm is a representation of audio data that can be used for displaying
 * the data on the screen. The problem, is that it would make no sense if an
 * audio source would have to be read completely for each redrawing of the
 * data. To avoid this, minimum and maximum values of parts the audio data
 * is stored in some way.
 * Implementers of this interface represent an audio source and can display its
 * content, but do not have to read through the whole data.
 * To keep the memory costs low, each sample is represented as a byte value.
 * That means, that only 256 different values are possible for the drawing
 * position. For most cases this should be enough though.
 * @author Manu Robledo
 *
 */
public interface WaveForm {
	/**
	 * Get the number of channels in this WaveForm
	 * @return The number of channels (1 or bigger)
	 */
	public int getChannels();
	/**
	 * Set the read position of this WaveForm to the
	 * value corresponding to the given real position
	 * of the represented sample. For WaveForms with
	 * a zoom factor of 1.0 this method should behave
	 * exactly like setPosition(pos)
	 * @param pos The new position in the original sample
	 */
	public void setRealPosition(int pos);
	/**
	 * Get the position inside the sample represented
	 * by this WaveForm corresponding to the actual read
	 * position of this WaveForm. For WaveForms with
	 * a zoom factor of 1.0 this method should behave
	 * exactly like getPosition()
	 * @return The position in the original sample
	 */
	public int getRealPosition();
	/**
	 * Set the read position of this WaveForm
	 * @param pos The new read position
	 */
	public void setPosition(int pos);
	/**
	 * Get the read position of this WaveForm
	 * @return The current read position
	 */
	public int getPosition();
	/**
	 * Get the minimum value at the current read position
	 * in the given channel
	 * @param channel the zero-based index of the channel
	 * @return the minimum value at this position as byte
	 */
	public byte getMin(int channel);
	/**
	 * Get the maximum value at the current read position
	 * in the given channel
	 * @param channel the zero-based index of the channel
	 * @return the maximum value at this position as byte
	 */
	public byte getMax(int channel);
	/**
	 * Sets the current read position to the beginning of
	 * this WaveForm
	 */
	public void rewind();
	/**
	 * Tells the WaveForm to increase its position by one
	 * @return true if the end position was not reached
	 * before this method call so that the position could
	 * be increased, false otherwise. The return value can
	 * be used for iteratin over the whole WaveForm like
	 * this:<br>
	 * <code>
	 * IWaveForm w = ...;<br>
	 * int channel = ...;<br>
	 * w.rewind();<br>
	 * do{<br>
	 * byte min = w.getMin(channel);<br>
	 * byte max = w.getMax(channel);<br>
	 * // ...(do something with those values)<br>
	 * } while(w.next());</code>
	 */
	public boolean next();
	/**
	 * Get a WaveForm Object that represents the part of
	 * this WaveForm specified by <code>begin</code> and
	 * <code>length</code> and displays this part in the
	 * specified <code>width</code>   
	 * @param begin The offset to the beginning of this
	 * WaveForm. May also be negative.
	 * @param length The length in steps of this WaveForm
	 * that should be represented by the returned WaveForm.
	 * @param width The new width in which the part should
	 * be displayed.
	 * @return A WaveForm Object that represents the specified
	 * Part of this WaveForm and displays it in the given
	 * width. Can also be the same Object with adapted
	 * attributes 
	 */
	public WaveForm subWaveForm(int begin, int length, int width);
	/**
	 * Get the Length of the original sample which this
	 * WaveForm represents
	 * @return The length of the original sample in sample
	 * frames.
	 */
	public int getRealLength();
	/**
	 * Get the total displayable length of this WaveForm.
	 * @return The length of this WaveForm
	 */
	public int getDisplayableLength();
	/**
	 * Get the ratio between the length of the represented
	 * Sample and and the displayable length of this WaveForm.
	 * @return <code>getRealLength() / (double)getDisplayableLength()</code>
	 */
	public double getZoomFactor();
	/**
	 * Tells if this WaveForm sees itself capable of providing
	 * a SubWaveForm with the given parameters
	 * @param begin The offset to the beginning of this
	 * WaveForm. May also be negative.
	 * @param length The length in steps of this WaveForm
	 * that should be represented by the returned WaveForm.
	 * @param width The new width in which the part should
	 * be displayed.
	 * @return true if this WaveForm can provide the wanted
	 * subWaveForm, false otherwise
	 * @see com.groovemanager.sampled.waveform.WaveForm#subWaveForm(int, int, int)
	 */
	public boolean canProvide(int begin, int length, int width);
	/**
	 * Get the number of samples of the original Sample
	 * which are represented by one step of this WaveForm.
	 * Especially interesting for PeakWaveForms.
	 * @return The number of samples
	 */
	public int getIntervallSize();
	/**
	 * Get the whole Peak data as array. This method could take very
	 * long for WaveForms which don't store their data in such an
	 * array. Should be very fast for peak Wave Forms.
	 * @return The Peak Data as byte-Array.
	 */
	public byte[] getData();
}