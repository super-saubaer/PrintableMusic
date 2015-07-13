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
 * A SubSource represents a part of another source
 * @author Manu Robledo
 *
 */
public class SubSource extends AbstractSource {
	/**
	 * The original source
	 */
	private CutListSource source;
	/**
	 * Start position in sample frames of the resulting source
	 */
	private int start,
	/**
	 * Length in sample frames of the resulting source 
	 */
	length;
	/**
	 * Construct a new SubSource out of the given source
	 * @param source The original source
	 * @param start Start position of the resulting source in sample frames
	 * @param length Length of the resulting source in sample frames
	 */
	public SubSource(CutListSource source, int start, int length){
		this.source = source;
		this.start = start;
		this.length = length;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#getAudioInputStream(int, int)
	 */
	public AudioInputStream getAudioInputStream(int start, int length)
			throws NotReadyException {
		return source.getAudioInputStream(this.start + start, Math.min(length, this.length - start));
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#getWaveForm(int, int, int)
	 */
	public WaveForm getWaveForm(int start, int length, int width) {
		return source.getWaveForm(start + this.start, Math.min(length, this.length - start), width);
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#isReady()
	 */
	public boolean isReady() {
		return source.isReady();
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#getLength()
	 */
	public int getLength() {
		return length;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#duplicate()
	 */
	public CutListSource duplicate() {
		return new SubSource(source.duplicate(), start, length);
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