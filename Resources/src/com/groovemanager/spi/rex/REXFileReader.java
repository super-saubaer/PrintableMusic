/*
 * Created on 23.04.2004
 *
 */
package com.groovemanager.spi.rex;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;

/**
 * This implementation of the AudioFileReader SPI provides access to 
 * Recycle files such as REX/REX2/RX2/RCY. The files have to be provided as
 * files because the REX API Doesn´t allow reading audio data from streams or
 * URLs. Also - because the files always have to be loaded completely into
 * memory, there is a maximum file size limit used by this provider. It is
 * assumed that a file bigger than 4 MB won´t be a REX file. 
 * @author Manu Robledo
 *
 */
public class REXFileReader extends AudioFileReader {
	/**
	 * The version of the jsrex library version
	 */
	final static String LIB_VERSION = "1.0";
	/**
	 * The name of the jsrex library
	 */
	final static String LIB_NAME = "jsrex";
	/**
	 * Indicates whether the REX dll has already been loaded by this VM
	 */
	static boolean dllLoaded = false;
	static{
		System.loadLibrary(getLibName());
		try {
			REXLoadDll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the full library name to be loaded
	 * @return The library name consisting of LIB_NAME + "_" + LIB_VERSION
	 */
	static String getLibName(){
		return LIB_NAME + "_" + LIB_VERSION;
	}

	/**
	 * The REX API requires a full file to be loaded into memory for being able
	 * to get the AudioFileFormat out of it so a call to this method will
	 * always result in an UnsupportedAudioFileException being thrown
	 * @see javax.sound.sampled.spi.AudioFileReader#getAudioFileFormat(java.io.InputStream)
	 */
	public AudioFileFormat getAudioFileFormat(InputStream stream)
			throws UnsupportedAudioFileException, IOException {
		throw new UnsupportedAudioFileException("REX only allows use of Files not streams.");
	}

	/**
	 * The REX API requires a full file to be loaded into memory for being able
	 * to get the AudioFileFormat out of it so a call to this method will
	 * always result in an UnsupportedAudioFileException being thrown
	 * @see javax.sound.sampled.spi.AudioFileReader#getAudioFileFormat(java.net.URL)
	 */
	public AudioFileFormat getAudioFileFormat(URL url)
			throws UnsupportedAudioFileException, IOException {
		throw new UnsupportedAudioFileException("REX only allows use of Files not URLs.");
	}

	/**
	 * The AufdioFileFormat returned by this method will contain some additional
	 * information as properties. See REXAudioFileFormat for more details.
	 * Note that REX files bigger than 4 MB are not supported they will always
	 * have to be loaded into memory completely and REX files are usually much
	 * smaller than 1 MB.
	 * @see javax.sound.sampled.spi.AudioFileReader#getAudioFileFormat(java.io.File)
	 * @see com.groovemanager.spi.rex.REXAudioFileFormat
	 */
	public AudioFileFormat getAudioFileFormat(File file)
			throws UnsupportedAudioFileException, IOException {
		// Wir gehen mal davon aus, dass kein REX-File größer als 4 MB ist.
		if(file.length() > 4 * 1024 * 1024) throw new UnsupportedAudioFileException("File too big for REX files.");
		try {
			REXHandle h = REXHandle.REXCreate(file);
			REXInfo info = h.REXGetInfo();
			REXCreatorInfo cInfo = null;
			try{
				cInfo = h.REXGetCreatorInfo();
			}
			catch(REXError e){
				if(e.getCode() != REXError.NoCreatorInfoAvailable) throw e;
			}
			REXSliceInfo[] sliceInfos = new REXSliceInfo[info.fSliceCount()];
			for (int i = 0; i < sliceInfos.length; i++) {
				sliceInfos[i] = h.REXGetSliceInfo(i);
			}
			REXAudioFileFormat f = new REXAudioFileFormat(info, cInfo, sliceInfos);
			return f;
		} catch (REXError e) {
			throw new UnsupportedAudioFileException(e.getMessage());
		}
	}

	/**
	 * The REX API requires a full file to be loaded into memory for being able
	 * to get an AudioInputStream out of it so a call to this method will
	 * always result in an UnsupportedAudioFileException being thrown
	 * @see javax.sound.sampled.spi.AudioFileReader#getAudioInputStream(java.io.InputStream)
	 */
	public AudioInputStream getAudioInputStream(InputStream stream)
			throws UnsupportedAudioFileException, IOException {
		throw new IllegalArgumentException("REX only allows use of Files not streams.");
	}

	/**
	 * The REX API requires a full file to be loaded into memory for being able
	 * to get an AudioInputStream out of it so a call to this method will
	 * always result in an UnsupportedAudioFileException being thrown
	 * @see javax.sound.sampled.spi.AudioFileReader#getAudioInputStream(java.net.URL)
	 */
	public AudioInputStream getAudioInputStream(URL url)
			throws UnsupportedAudioFileException, IOException {
		throw new IllegalArgumentException("REX only allows use of Files not URLs.");
	}


	/**
	 * Note that REX files bigger than 4 MB are not supported they will always
	 * have to be loaded into memory completely and REX files are usually much
	 * smaller than 1 MB.
	 * @see javax.sound.sampled.spi.AudioFileReader#getAudioInputStream(java.io.File)
	 */
	public AudioInputStream getAudioInputStream(File file)
			throws UnsupportedAudioFileException, IOException {
		if(file.length() > 4 * 1024 * 1024) throw new UnsupportedAudioFileException("File too big for REX files.");
		REXHandle h;
		try {
			h = REXHandle.REXCreate(file);
			return h.getAudioInputStream();
		} catch (REXError e) {
			throw new UnsupportedAudioFileException(e.getMessage());
		}
	}

	/**
	 * Just a delegator to REXError.throwREXError()
	 * @param code The REXError constant
	 * @throws REXError If the given constant is not the same as REXError.NoError
	 */
	static void throwREXError(int code) throws REXError{
		REXError.throwREXError(code);
	}

	/**
	 * Load the REX dll only if it has not already been loaded by this VM
	 * instance.
	 * @throws REXError If a REXError occured
	 */
	static void REXLoadDll() throws REXError{
		if(dllLoaded) return;
		throwREXError(jREXLoadDLL());
		dllLoaded = true;
	}
	
	/**
	 * Unload the REX dll
	 *
	 */
	static void REXUnloadDLL(){
		if(!dllLoaded) return;
		jREXUnloadDLL();
		dllLoaded = false;
	}
	
	//Basic
	private static native int jREXLoadDLL();
	private static native void jREXUnloadDLL();
}