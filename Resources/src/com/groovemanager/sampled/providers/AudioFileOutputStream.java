package com.groovemanager.sampled.providers;

import java.io.File;
import java.io.OutputStream;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;

/**
 * An AudioFileOutputStream is an OutputStream for writing a soecific type of
 * audio file.
 * @author Manu Robledo
 *
 */
public abstract class AudioFileOutputStream extends OutputStream {
	/**
	 * The file to write to
	 */
	protected final File outputFile;
	/**
	 * The audio file type to write
	 */
	protected final AudioFileFormat.Type type;
	/**
	 * The AudioFormat in which to write the file
	 */
	protected final AudioFormat format;
	/**
	 * Map of additional properties to write into the file, if supported
	 */
	protected final Map properties;
	/**
	 * Construct a new AudioFileOutputStream
	 * @param f The file to write to
	 * @param format The format in which the audio data should be written into
	 * the file
	 * @param t The audio file type to write
	 * @param properties Map of additional properties to write into the file,
	 * if supported
	 */
	public AudioFileOutputStream(File f, AudioFormat format, AudioFileFormat.Type t, Map properties) {
		this.format = format;
		outputFile = f;
		this.properties = properties;
		type = t;
	}
	/**
	 * Get the AudioFormat of this AudioFileOutputStream
	 * @return This stream´s AudioFormat
	 */
	public AudioFormat getFormat(){
		return format;
	}
	/**
	 * Get the audio file type of this stream
	 * @return This stream´s AudioFileFormat.Type
	 */
	public AudioFileFormat.Type getType(){
		return type;
	}
	/**
	 * Get the file into which this stream writes
	 * @return The file being written by this stream
	 */
	public File getOutputFile(){
		return outputFile;
	}
}