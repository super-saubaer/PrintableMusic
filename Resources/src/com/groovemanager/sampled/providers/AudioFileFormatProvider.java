/*
 * Created on 19.06.2004
 *
 */
package com.groovemanager.sampled.providers;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * An AudioFileFormatProvider is something similar to the AudioFileReader SPI,
 * but as the name implies, it can only provide AudioFileFormat objects, not
 * AudioInputStreams. An AudioFileFormatProvider can be propagated to an
 * AudioManager.
 * @author Manu Robledo
 *
 */
public abstract class AudioFileFormatProvider {
	/**
	 * Get the AudioFileFormat.Types supported by this provider
	 * @return The audio file types supported by this provider
	 */
	public abstract AudioFileFormat.Type[] getSupportedTypes();
	/**
	 * Get the AudioFormats supported by this provider
	 * @return The audio formats supported by this provider
	 */
	public abstract AudioFormat[] getSupportedFormats();
	/**
	 * Get the property keys understood by this provider
	 * @return The property keys of the properties this provider can provide
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
	 * Get the AudioFileFormat out of the given file
	 * @param f The file to be read
	 * @return The AudioFileFormat of the specified file.
	 * @throws UnsupportedAudioFileException If the format of the file is not
	 * known or invalid
	 * @throws IOException If an I/O Error occured during reading
	 */
	public abstract AudioFileFormat getAudioFileFormat(File f) throws UnsupportedAudioFileException, IOException;
	/**
	 * Ask this provider, if it is able to get the AudioFileFormat out of this
	 * file 
	 * @param f The file to ask for
	 * @return true, if this file is supported, false otherwise
	 */
	public abstract boolean isFileSupported(File f);
}
