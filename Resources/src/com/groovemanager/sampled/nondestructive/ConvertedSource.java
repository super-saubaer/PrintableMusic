/*
 * Created on 25.06.2004
 *
 */
package com.groovemanager.sampled.nondestructive;

import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import com.groovemanager.exception.NotReadyException;
import com.groovemanager.sampled.waveform.WaveForm;

/**
 * This CutListSource is being constructed out of a given CutListSource and a
 * target AudioFormat. When queried for an AudioInputStream, this source will
 * try to provide a stream in this format out of the source´s stream by
 * converting the stream if needed
 * @author Manu Robledo
 *
 */
public class ConvertedSource extends AbstractSource {
	/**
	 * The desired target AudioFormat
	 */
	final AudioFormat targetFormat;
	/**
	 * The source to get the unconverted stream from
	 */
	final CutListSource source;
	/**
	 * Construct a new ConvertedSource out of the given CutListSource and 
	 * AudioFormat
	 * @param source The CutListSource to get the unconverted stream from
	 * @param targetFormat The AudioFormat into which the stream will be
	 * converted 
	 */
	public ConvertedSource(CutListSource source, AudioFormat targetFormat){
		this.targetFormat = targetFormat;
		this.source = source;
	}
	/**
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#getAudioInputStream(int, int)
	 */
	public AudioInputStream getAudioInputStream(int start, int length)
			throws NotReadyException {
		AudioInputStream stream = source.getAudioInputStream(start, length);
		if(stream.getFormat().equals(targetFormat)) return stream;
		else if(!AudioSystem.isConversionSupported(targetFormat, stream.getFormat())) throw new NotReadyException("Format conversion from " + stream.getFormat() + " to " + targetFormat + " not supported.");
		else return AudioSystem.getAudioInputStream(targetFormat, stream);
	}
	/**
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#getWaveForm(int, int, int)
	 */
	public WaveForm getWaveForm(int start, int length, int width) {
		return source.getWaveForm(start, length, width);
	}
	/**
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#isReady()
	 */
	public boolean isReady() {
		return source.isReady();
	}
	/**
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#getLength()
	 */
	public int getLength() {
		return source.getLength();
	}
	/**
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#duplicate()
	 */
	public CutListSource duplicate() {
		return new ConvertedSource(source.duplicate(), targetFormat);
	}
	/**
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#usesFile(java.io.File)
	 */
	public boolean usesFile(File f) {
		return source.usesFile(f);
	}
	/**
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#replaceFile(java.io.File, java.io.File)
	 */
	public void replaceFile(File from, File to) {
		if(source.usesFile(from)){
			source.replaceFile(from, to);
			notifyListeners();
		}
	}
}
