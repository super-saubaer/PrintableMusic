/*
 * Created on 03.06.2004
 *
 */
package com.groovemanager.app.mc909se;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * This class is used to read the sample info contained in a MC909 .wav or
 * .aif sample.
 * @author Manu Robledo
 *
 */
public class Mc909SampleInfo {
	/**
	 * Number of channels
	 */
	public final short channels;
	/**
	 * Sample name
	 */
	public final String name;
	/**
	 * The file from which the data was read
	 */
	public final File file;
	/**
	 * Loop mode
	 */
	public final byte loopMode;
	/**
	 * Timestretch type
	 */
	public final short tsType;
	/**
	 * Tempo in beats per minute multiplied with 100
	 */
	public final short bpm100;
	/**
	 * Indicates whether the sample file is a wav file (true) or an aif file 
	 * (false)
	 */
	private boolean wave;
	/**
	 * Total length of the sample in sample frames
	 */
	private int totlength;
	/**
	 * Create a new Mc909SampleInfo by reading from the given file
	 * @param f The sample file to read from
	 * @throws UnsupportedAudioFileException If the given file doesn´t contain
	 * valid MC909 sample data
	 * @throws IOException If an I/O error occured during reading
	 */
	public Mc909SampleInfo(File f) throws UnsupportedAudioFileException, IOException{
		file = f;
		FileInputStream in = new FileInputStream(f);
		byte[] b = new byte[4];
		ByteBuffer buffer = ByteBuffer.wrap(b);
		in.read(b);
		if(new String(b).equals("RIFF")){
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			in.read(b);
			totlength = buffer.getInt();
			in.read(b);
			if(new String(b).equals("WAVE")) wave = true;
			else throw new UnsupportedAudioFileException();
		}
		else if(new String(b).equals("FORM")){
			buffer.order(ByteOrder.BIG_ENDIAN);
			in.read(b);
			totlength = buffer.getInt();
			in.read(b);
			if(new String(b).equals("AIFF")) wave = false;
			else throw new UnsupportedAudioFileException();
		}
		boolean foundChannel = false, foundName = false;
		String n = null;
		short c = 0, bpm = 13800, ts = 0;
		byte lm = 0;
		while(!(foundChannel && foundName)){
			if(in.read(b, 0, 4) != 4) throw new UnsupportedAudioFileException("No valid Roland file.");
			String header = new String(b);
			if(in.read(b, 0, 4) != 4) throw new UnsupportedAudioFileException("No valid Roland file.");
			int length = buffer.getInt(0);
			if(header.equals("RLND")){
				if(wave) in.skip(12);
				else in.skip(8);
				byte[] nameBytes = new byte[16];
				if(in.read(nameBytes, 0, 16) != 16) throw new UnsupportedAudioFileException("No valid Roland file.");
				n = new String(nameBytes);
				foundName = true;
				in.skip(159);
				in.read(b, 0, 1);
				lm = b[0];
				in.read(b);
				ts = buffer.getShort(2);
				in.read(b);
				bpm = buffer.getShort(2);
			}
			else if(wave && header.equals("fmt ")){
				in.skip(2);
				if(in.read(b, 0, 2) != 2) throw new UnsupportedAudioFileException("No valid Roland file.");
				c = buffer.getShort(0);
				in.skip(length - 4);
				foundChannel = true;
			}
			else if((!wave) && header.equals("COMM")){
				if(in.read(b, 0, 2) != 2) throw new UnsupportedAudioFileException("No valid Roland file.");
				c = buffer.getShort(0);
				in.skip(length - 2);
				foundChannel = true;
			}
			else if((!wave) && header.equals("APPL")){
			}
			else in.skip(buffer.getInt(0));
		}
		in.close();
		while(!(foundChannel && foundName)) throw new UnsupportedAudioFileException("No valid Roland file.");
		name = n;
		channels = c;
		loopMode = lm;
		bpm100 = bpm;
		tsType = ts;
	}
}