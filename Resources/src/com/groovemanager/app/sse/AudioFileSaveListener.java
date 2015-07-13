/*
 * Created on 19.06.2004
 *
 */
package com.groovemanager.app.sse;

import java.io.File;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;

/**
 * An AudioFileSaveListener can be notified when a SaveAudioFileAsAction is
 * being performed.
 * @author Manu Robledo
 *
 */
public interface AudioFileSaveListener {
	/**
	 * Notification that a SaveAudioFileAsAction has been performed, meaning
	 * the given file should be saved as the given audio file type at the given
	 * File location in the given format.
	 * @param source The file that should be saved.
	 * @param f The file to save to.
	 * @param type The audio file type to write.
	 * @param format The audio format to write.
	 */
	public void saveAudioFile(File source, File f, AudioFileFormat.Type type, AudioFormat format);
}
