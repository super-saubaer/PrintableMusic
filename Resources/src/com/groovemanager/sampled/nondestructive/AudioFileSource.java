/*
 * Created on 30.05.2004
 *
 */
package com.groovemanager.sampled.nondestructive;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.groovemanager.exception.NotReadyException;
import com.groovemanager.sampled.waveform.AudioFileWaveForm;
import com.groovemanager.sampled.waveform.WaveForm;

/**
 * A CutListSource that gets its audio data from an audio file
 * @author Manu Robledo
 *
 */
public class AudioFileSource extends AbstractSource implements CutListSource {
	/**
	 * The audio file containing the audio data
	 */
	private File f;
	/**
	 * The AudioFileWaveForm corresponding to the audio file
	 */
	private AudioFileWaveForm wf;
	/**
	 * The length of the audio data in the file in sample frames
	 */
	private int length;
	/**
	 * The frame size of the audio file´s format
	 */
	private int frameSize;
	/**
	 * Construct a new AudioFileSource out of the given file and WaveForm
	 * @param source The audio file containing the audio data
	 * @param wf The AudioFileWaveForm corresponding to the file
	 * @throws IllegalArgumentException If the audio file can not be read
	 */
	public AudioFileSource(File source, AudioFileWaveForm wf) {
		f = source;
		this.wf = wf;
		try {
			AudioInputStream in = AudioSystem.getAudioInputStream(f);
			length = (int)in.getFrameLength();
			frameSize = in.getFormat().getFrameSize();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(f+": "+e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(f+": "+e.getMessage());
		}
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#getAudioInputStream(int, int)
	 */
	public AudioInputStream getAudioInputStream(int start, int length)
			throws NotReadyException {
		AudioInputStream in;
		try {
			in = AudioSystem.getAudioInputStream(f);
			int toSkip = (int)Math.min(start, in.getFrameLength()) * frameSize;
			while(toSkip > 0){
				int skipped = (int)in.skip(toSkip);
				if(skipped == -1) throw new NotReadyException("Nothing to play");
				toSkip -= skipped;
			}
		} catch (UnsupportedAudioFileException e) {
			throw new NotReadyException(f + ": Audio file format not supported.");
		} catch (IOException e) {
			throw new NotReadyException("An I/O error occured:" + e.getMessage());
		}
		if(this.length - start == length) return in;
		else return new AudioInputStream(in, in.getFormat(), length);
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#getWaveForm(int, int, int)
	 */
	public WaveForm getWaveForm(int start, int length, int width) {
		return wf.subWaveForm(start, length, width);
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#isReady()
	 */
	public boolean isReady() {
		return f.exists();
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#getLength()
	 */
	public int getLength() {
		return length;
	}
	/**
	 * Append the given number of frames to the audio data´s length. This
	 * method can be used when the source file is still growing. This will be
	 * the case for instance in a recording situation
	 * @param numFrames The number of sample frames to append
	 */
	public void appendFrames(int numFrames){
		length += numFrames;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#duplicate()
	 */
	public CutListSource duplicate() {
		try {
			return new AudioFileSource(f, new AudioFileWaveForm(f, wf.getPeakWaveForm(), wf.getWaveLength(), wf.getMaxWaves()));
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#usesFile(java.io.File)
	 */
	public boolean usesFile(File f) {
		return f.equals(this.f);
	}
	/**
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#replaceFile(java.io.File, java.io.File)
	 */
	public void replaceFile(File from, File to) {
		if(usesFile(from)){
			f = to;
			try {
				wf = new AudioFileWaveForm(f, wf.getPeakWaveForm(), wf.getWaveLength(), wf.getMaxWaves());
				notifyListeners();
			} catch (UnsupportedAudioFileException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}