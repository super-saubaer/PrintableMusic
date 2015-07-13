/*
 * Created on 11.05.2004
 *
 */
package com.groovemanager.sampled.waveform;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.groovemanager.sampled.AudioManager;

/**
 * An AudioFileWaveForm is a WaveForm that abstracts the audio data contained
 * in an AudioFile. For creating an AudioFileWaveForm, a corresponding
 * PeakWaveForm is needed, which has to be created before. The
 * AudioFileWaveForm then decides on the fly depending on the zoom factor if
 * it can provide the data to be made visible out of the PeakWaveForm or if
 * it has to read a part of the audio file. This implementation also holds a
 * pool of WaveForms that were read from the audio file so that the number of
 * needed file I/O operations is minimized.
 * @author Manu Robledo
 *
 */
public class AudioFileWaveForm extends AbstractWaveForm {
	/**
	 * The source audio file
	 */
	protected File source;
	/**
	 * The source file´s audio format
	 */
	protected AudioFormat format;
	/**
	 * The peak WaveForm
	 */
	protected WaveForm peakWave;
	/**
	 * Pool of lately used WaveForms read from the file
	 */
	protected WaveForm[] realWaveForms;
	/**
	 * Positions of the WaveForms contained in the pool
	 */
	protected int[] waveFormPositions;
	/**
	 * Maximum number of WaveForms to store in the pool 
	 */
	final protected int MAX_WAVES,
	/**
	 * Length of each WaveForm stored in the pool
	 */
	WAVE_LENGTH;
	/**
	 * Get the maximum number of WaveForms stored in the pool
	 * @return The maximum number of WaveForms stored in the pool
	 */
	public int getMaxWaves(){
		return MAX_WAVES;
	}
	/**
	 * Get the length of each WaveForm stored in the pool.
	 * @return The length of each WaveForm stored in the pool
	 */
	public int getWaveLength(){
		return WAVE_LENGTH;
	}
	/**
	 * Construct a new AudioFileWaveForm
	 * @param source The audio file this WaveForm should represent
	 * @param peak The peak WaveForm containing average values of the source
	 * file´s data in determined intervals
	 * @param wavelength The length of the WaveForms stored in the pool
	 * @param maxwaves Maximum number of WaveForms to be stored in the pool
	 * @throws UnsupportedAudioFileException If the audio format of the source
	 * file cannot be read.
	 * @throws IOException If an I/O Error occured
	 */
	public AudioFileWaveForm(File source, WaveForm peak, int wavelength, int maxwaves) throws UnsupportedAudioFileException, IOException {
		WAVE_LENGTH = wavelength;
		MAX_WAVES = maxwaves;
		this.source = source;
		AudioInputStream in = AudioSystem.getAudioInputStream(source);
		format = in.getFormat();
		if(in.getFrameLength() == AudioSystem.NOT_SPECIFIED){
			in = new AudioInputStream(in, in.getFormat(), AudioManager.getDefault().getFrameLength(source));
		}
		if(in.getFrameLength() > Integer.MAX_VALUE) throw new UnsupportedAudioFileException("File too big.");
		realLength = displayWidth = (int)in.getFrameLength();
		in.close();
		peakWave = peak;
		channels = format.getChannels();
		realWaveForms = new WaveForm[MAX_WAVES];
		waveFormPositions = new int[MAX_WAVES];
	}
	/**
	 * Get the WaveForm that corresponds to the given sample position. If this
	 * WaveForm is already in the pool, it will be returned from there,
	 * otherwise it will be loaded into the pool
	 * @param pos The position of the audio data in question in sample frames
	 * @return The WaveForm holding the audio data at the given position
	 */
	protected WaveForm loadRealWaveForm(int pos){
		int index = pos / WAVE_LENGTH;
		WaveForm w;
		
		// If Waveform is already loaded: fine.
		for (int i = 0; i < waveFormPositions.length; i++) {
			if(waveFormPositions[i] == index){
				w = realWaveForms[i];
				if(w != null) return realWaveForms[i];
			}
		}
		
		// Create new WaveForm
		AudioInputStream in = null;
		byte[] b = null;
		try {
			in = AudioSystem.getAudioInputStream(source);
			in = AudioSystem.getAudioInputStream(new AudioFormat(format.getSampleRate(), 16, format.getChannels(), true, format.isBigEndian()), in);
			b = new byte[WAVE_LENGTH * 2 * in.getFormat().getChannels()];
			
			int toSkip = index * WAVE_LENGTH * in.getFormat().getFrameSize();
			while(toSkip > 0){
				int skipped = (int)in.skip(toSkip);
				if(skipped == -1) break;
				else toSkip -= skipped;
			}
			
			int toRead = b.length;
			while(toRead > 0){
				int read = in.read(b, b.length - toRead, toRead);
				if(read == -1) break;
				toRead -= read;
			}
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		w = new ByteArrayWaveForm(b, in.getFormat());

		// Search for the entry which is most far away from this one
		int maxdiff = 0;
		int maxindex = 0;
		for (int i = 0; i < waveFormPositions.length; i++) {
			if(realWaveForms[i] == null){
				realWaveForms[i] = w;
				waveFormPositions[i] = index;
				return w;
			}
			if(Math.abs(waveFormPositions[i] - index) > maxdiff){
				maxdiff = Math.abs(waveFormPositions[i] - index);
				maxindex = i;
			}
		}
		waveFormPositions[maxindex] = index;
		realWaveForms[maxindex] = w;
		return w;
	}
	public byte getMin(int channel) {
		WaveForm w = loadRealWaveForm(position);
		if(w == null) return 0;
		w.setRealPosition(position % WAVE_LENGTH);
		return w.getMin(channel);
	}
	public byte getMax(int channel) {
		WaveForm w = loadRealWaveForm(position);
		if(w == null) return 0;
		w.setRealPosition(position % WAVE_LENGTH);
		return w.getMax(channel);
	}
	public WaveForm subWaveForm(int begin, int length, int width) {
		WaveForm w = null;
		// Always display from the beginning
		if(begin < 0) begin = 0;
		
		// If more data should be displayed than this WaveForm can provide:
		// ownWidth and ownLength should contain the width and the length
		// this WaveForm can provide. The rest will be filled up with an
		// instance of FillUpWaveForm
		int ownWidth = width;
		int ownLength = length;
		
		// First check the width...
		if(ownWidth > getDisplayableLength()){
			ownWidth = getDisplayableLength();
			ownLength = (int)Math.round(length * ownWidth / (double)width);
		}
		// ... then the length
		if(begin + ownLength > getDisplayableLength()){
			ownLength = getDisplayableLength() - begin;
			ownWidth = (int)Math.round(width * (double)ownLength / (double)length); 
		}
		if(begin == 0 && ownLength == ownWidth && ownWidth == getDisplayableLength()) w = this;
		else{
			double f = peakWave.getZoomFactor();
			if(peakWave.canProvide((int)Math.round(begin / f), (int)Math.round(ownLength / f), ownWidth)){
				double temp = getDisplayableLength() / (double)peakWave.getDisplayableLength();
				w = peakWave.subWaveForm((int)Math.round(begin / temp), (int)Math.round(ownLength / temp), ownWidth);
			}
			else if(ownLength >= 0 && ownWidth >= 0){
				int firstIndex = begin / WAVE_LENGTH;
				int lastIndex = (begin + ownLength) / WAVE_LENGTH;
				if(lastIndex > firstIndex){
					WaveForm[] waveForms = new WaveForm[lastIndex - firstIndex + 1];
					for(int i = 0; i < waveForms.length; i++){
						waveForms[i] = loadRealWaveForm(WAVE_LENGTH * (firstIndex + i));
					}
					w = new ConcatWaveForm(waveForms);
					w = w.subWaveForm(begin - firstIndex * WAVE_LENGTH, ownLength, ownWidth);
				}
				else{
					w = loadRealWaveForm(WAVE_LENGTH * firstIndex);
					w = w.subWaveForm(begin - firstIndex * WAVE_LENGTH, ownLength, ownWidth);
				}
			}
		}
		
		if(w != null){
			if(width == ownWidth) return w;
			else return new FillUpWaveForm(w, 0, width);
		}
		else return super.subWaveForm(begin, length, width);
	}
	/**
	 * Get the peak WaveForm assigned to this AudioFileWaveForm
	 * @return The peak WaveForm assigned to this AudioFileWaveForm
	 */
	public WaveForm getPeakWaveForm(){
		return peakWave;
	}
}