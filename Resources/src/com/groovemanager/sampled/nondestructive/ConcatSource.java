/*
 * Created on 30.05.2004
 *
 */
package com.groovemanager.sampled.nondestructive;

import java.io.File;

import javax.sound.sampled.AudioInputStream;

import com.groovemanager.exception.NotReadyException;
import com.groovemanager.sampled.waveform.ConcatWaveForm;
import com.groovemanager.sampled.waveform.WaveForm;

/**
 * This class represents a CutListSource that is created out of some given
 * CutListSources by concatenating them
 * @author Manu Robledo
 *
 */
public class ConcatSource extends AbstractSource {
	/**
	 * The sources to concatenate
	 */
	private CutListSource[] sources;
	/**
	 * Create a new ConcatSource out of the given sources
	 * @param sources The CutListSources to concatenate
	 */
	public ConcatSource(CutListSource[] sources){
		this.sources = sources;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#getAudioInputStream(int, int)
	 */
	public AudioInputStream getAudioInputStream(int start, int length)
			throws NotReadyException {
		int first = getIndex(start);
		int last = getIndex(start + length - 1);
		AudioInputStream[] streams = new AudioInputStream[last - first + 1];
		int s = start;
		int l = length;
		for(int i = 0; i < sources.length; i++){
			int temps, templ;
			if(i < first) s -= sources[i].getLength();
			else if(i <= last){
				if(i == first) temps = s;
				else temps = 0;
				if(i == last) templ = l;
				else templ = sources[i].getLength() - temps;
				streams[i - first] = sources[i].getAudioInputStream(temps, templ);
				l -= templ;
			}
		}
		return new AudioInputStream(new ConcatInputStream(streams), streams[0].getFormat(), length);
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#getWaveForm(int, int, int)
	 */
	public WaveForm getWaveForm(int start, int length, int width) {
		int first = getIndex(start);
		int last = getIndex(start + length - 1);
		double factor = width / (double)length;
		WaveForm[] wfs = new WaveForm[last - first + 1];
		int s = start;
		int l = length;
		for (int i = 0; i < sources.length; i++) {
			int temps, templ;
			if(i < first) s -= sources[i].getLength();
			else if(i <= last){
				if(i == first) temps = s;
				else temps = 0;
				templ = Math.min(l, sources[i].getLength() - temps);
				wfs[i - first] = sources[i].getWaveForm(temps, templ, (int)Math.round(templ * factor));
				l -= templ;
			}
		}
		return new ConcatWaveForm(wfs);
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#isReady()
	 */
	public boolean isReady() {
		for (int i = 0; i < sources.length; i++) {
			if(!sources[i].isReady()) return false;
		}
		return true;
	}
	/**
	 * Get the index in the source Array for the CutListSource at the given
	 * frame position
	 * @param pos The frame position in question
	 * @return The index inside the source Array of the CutListSource to which
	 * the given frame position belongs
	 */
	protected int getIndex(int pos){
		for (int i = 0; i < sources.length; i++) {
			if(pos < sources[i].getLength()) return i;
			pos -= sources[i].getLength();
		}
		return sources.length - 1;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#getLength()
	 */
	public int getLength() {
		int l = 0;
		for (int i = 0; i < sources.length; i++) {
			l += sources[i].getLength();
		}
		return l;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#duplicate()
	 */
	public CutListSource duplicate() {
		CutListSource[] newSources = new CutListSource[sources.length];
		for (int i = 0; i < sources.length; i++) {
			newSources[i] = sources[i].duplicate();
		}
		return new ConcatSource(newSources);
	}
	/**
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#usesFile(java.io.File)
	 */
	public boolean usesFile(File f) {
		for (int i = 0; i < sources.length; i++) {
			if(sources[i].usesFile(f)) return true;
		}
		return false;
	}
	/**
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#replaceFile(java.io.File, java.io.File)
	 */
	public void replaceFile(File from, File to) {
		for (int i = 0; i < sources.length; i++) {
			if(sources[i].usesFile(from)){
				sources[i].replaceFile(from, to);
				notifyListeners();
			}
		}
	}
}