/*
 * Created on 17.05.2004
 *
 */
package com.groovemanager.sampled.waveform;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * This class extends the AudioFileWaveForm class so that it can be used for
 * a dynamically growing audio file, especially in recording situations.
 * @author Manu Robledo
 *
 */
public class DynamicAudioFileWaveForm extends AudioFileWaveForm {
	/**
	 * Construct a new DynamicAudioFileWaveForm
	 * @param source The source audio file
	 * @param wavelength The number of WaveForms kept in the pool
	 * @param maxwaves The length of each WaveForm in the pool
	 * @param iSize The interval size to be used for the DynmaicPeakWaveForm
	 * which is used as this WaveForm´s peak WaveForm
	 * @throws UnsupportedAudioFileException If the given audio file can not be
	 * read 
	 * @throws IOException If an I/O Error occured
	 */
	public DynamicAudioFileWaveForm(File source, int wavelength, int maxwaves, int iSize) throws UnsupportedAudioFileException, IOException {
		super(source, createPeak(source, iSize), wavelength, maxwaves);
	}
	/**
	 * Create a DynamicPeakFile for the given source file
	 * @param source The source file
	 * @param iSize The intervall size
	 * @return The created DynamicPeakFile
	 * @throws UnsupportedAudioFileException If the AudioFileFormat cannot be
	 * read out of the given file
	 * @throws IOException If an I/O error occurs
	 */
	private static DynamicPeakWaveForm createPeak(File source, int iSize) throws UnsupportedAudioFileException, IOException{
		AudioFileFormat fFormat = AudioSystem.getAudioFileFormat(source);
		return new DynamicPeakWaveForm(fFormat.getFormat(), iSize);
	}
	/**
	 * Dynamically append data to this WaveForm. Note that the data will not be
	 * written to the underlying audio file.
	 * @param data The audio data to append
	 * @param offset Start position inside the data Array
	 * @param length The length of data to be appended in bytes
	 */
	public void append(byte[] data, int offset, int length){
		int diff = length / format.getFrameSize();
		((DynamicPeakWaveForm)peakWave).append(data, offset, length);
		
		realLength += diff;
		displayWidth += diff;
	}
	
	public WaveForm subWaveForm(int begin, int length, int width) {
		
		WaveForm w = super.subWaveForm(begin, length, width);
		return new UpdatingWaveForm(w);
	}
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
		byte[] b = new byte[WAVE_LENGTH * format.getFrameSize()];
		w = new ByteArrayWaveForm(b, format);

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
}