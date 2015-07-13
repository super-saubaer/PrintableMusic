/*
 * Created on 19.06.2004
 *
 */
package com.groovemanager.sampled.providers;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * An AudioFileOutputStreamProvider is something similar to the
 * AudioFileWriter SPI. An AudioFileWriter can provide AudioFileOutputStreams.
 * @author Manu Robledo
 *
 */
public abstract class AudioFileOutputStreamProvider {
	/**
	 * Get the audio file types supported by this provider
	 * @return The audio file types supported by this provider
	 */
	public abstract AudioFileFormat.Type[] getSupportedTypes();
	/**
	 * Get the audio formats supported by this provider
	 * @return The audio formats supported by this provider
	 */
	public abstract AudioFormat[] getSupportedFormats();
	/**
	 * Get the property keys supported by this provider
	 * @return The property keys supported by this provider
	 */
	public abstract String[] getSupportedProperties();
	/**
	 * Ask this provider, if it supports the specified audio file type
	 * @param type The AudioTypeFormat.Type to ask for
	 * @return true, if the given type is supported, false otherwise
	 */
	public boolean isTypeSupported(AudioFileFormat.Type type){
		AudioFileFormat.Type[] types = getSupportedTypes();
		for (int i = 0; i < types.length; i++) {
			if(types[i].equals(type)) return true;
		}
		return false;
	}
	/**
	 * Ask this provider, if the given AudioFormat is supported or not
	 * @param format The AudioFormat to ask for
	 * @return true, if the given format is supported, false otherwise.
	 */
	public boolean isFormatSupported(AudioFormat format){
		AudioFormat[] formats = getSupportedFormats();
		for (int i = 0; i < formats.length; i++) {
			if(formats[i].matches(format)) return true;
		}
		return false;
	}
	/**
	 * Ask this provider, if it supports the property specified by the given
	 * kay.
	 * @param property The property key to ask for
	 * @return true, if this property key is understood, false otherwise.
	 */
	public boolean isPropertySupported(String property){
		String[] properties = getSupportedProperties();
		for (int i = 0; i < properties.length; i++) {
			if(properties[i].equals(property)) return true;
		}
		return false;
	}
	/**
	 * Provide an AudioFileOutputStream
	 * @param f The file to write to
	 * @param type The audio file type to write
	 * @param format The AudioFormat to write
	 * @param properties Map of properties to write into the file, if supported
	 * @return An AudioFileOutputStream matching the given parameters
	 * @throws UnsupportedAudioFileException If the audio file type is not
	 * supported
	 * @throws IOException If an I/O Error occured during creation of the
	 * OutputStream
	 */
	public abstract AudioFileOutputStream getAudioFileOutputStream(File f, AudioFileFormat.Type type, AudioFormat format, Map properties) throws UnsupportedAudioFileException, IOException;
}
