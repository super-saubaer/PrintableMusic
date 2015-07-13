/*
 * Created on 30.05.2004
 *
 */
package com.groovemanager.sampled.nondestructive;

import java.io.File;

import javax.sound.sampled.AudioInputStream;

import com.groovemanager.exception.NotReadyException;
import com.groovemanager.sampled.waveform.WaveForm;

/**
 * A CutListsource is an audio data source that can be used as input for a
 * CutList. It must be able to provide AudioInputStreams as well as WaveForms
 * for displaying the data.
 * @author Manu Robledo
 *
 */
public interface CutListSource{
	/**
	 * Get an AudioInputStream from this source
	 * @param start The start position of the stream in sample frames
	 * @param length The length of the stream in sample frames
	 * @return An AudioInputStream starting at position <code>start</code>
	 * of this source with the length of <code>length</code> sample frames.
	 * @throws NotReadyException If the audio data can not be provided
	 */
	public AudioInputStream getAudioInputStream(int start, int length) throws NotReadyException;
	/**
	 * Get a WaveForm from this source 
	 * @param start The start position of the WaveForm in sample frames
	 * @param length The length of the audio data represented by the WaveForm
	 * in sample frames
	 * @param width The width in which the WaveForm should be displayed
	 * @return A WaveForm starting at position <code>start</code>
	 * of this source representing <code>length</code> sample frames with the
	 * width of <code>width</code>.
	 */
	public WaveForm getWaveForm(int start, int length, int width);
	/**
	 * Add a ModificationListener to this source that will be notified of any
	 * changes
	 * @param listener The ModificationListener to add
	 */
	public void addModificationListener(ModificationListener listener);
	/**
	 * Remove a ModificationListener from this source
	 * @param listener The ModificationListener to remove
	 */
	public void removeModificationListener(ModificationListener listener);
	/**
	 * Ask this source, if it is ready for providing audio data
	 * @return true, if this source can provide data, false otherwise
	 */
	public boolean isReady();
	/**
	 * Get the total length of this source in sample frames
	 * @return The length of this source in sample frames
	 */
	public int getLength();
	/**
	 * Create a new CutListSource that represents the same data, but is
	 * independent of changes made to this source 
	 * @return An independent copy of this source
	 */
	public CutListSource duplicate();
	/**
	 * Ask this CutListSource whether it relies on the contents of the given
	 * file. This is needed, if a file should be overwritten and it must be
	 * checked that this doesn´t cause any inconsistencies
	 * @param f The file to ask for
	 * @return true, if a change to the given file´s content would also mean
	 * a change to this source, false otherwise
	 */
	public boolean usesFile(File f);
	/**
	 * Replace all references to the given source file inside this
	 * CutListSource with references to the given target file. This may be
	 * because the source file is about to change and to avoid inconsistencies.
	 * If the given source file is not used by this CutListSource, the call
	 * can be ignored.
	 * @param from The source file to replace
	 * @param to The target file to replace with
	 */
	public void replaceFile(File from, File to);
}