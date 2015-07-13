/*
 * Created on 10.05.2004
 *
 */
package com.groovemanager.sampled.providers;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.sound.sampled.AudioFormat;

/**
 * This AudioFileOutputStream implementation supports writing of Roland
 * MC-909 RLND chunks in addition to the chunks supported by its superclass 
 * @author Manu Robledo
 *
 */
public class WAV909OutputStream extends WavSliceOutputStream implements SMPLChunk{
	/**
	 * The RLNDChunk to add, if any
	 */
	private RLNDChunk chunk;
	/**
	 * Construct a new WAV909OutputStream
	 * @param f The file to write to
	 * @param format The AufioFormat to write
	 * @param properties Map of properties to write, if supported
	 * @throws IOException If an I/O Exception occurs during creation of the
	 * OutputStream
	 */
	public WAV909OutputStream(File f, AudioFormat format, Map properties) throws IOException{
		super(f, format, properties);
		if(properties != null){
			Object o = properties.get("RLND");
			if(o != null && o instanceof RLNDChunk) chunk = (RLNDChunk)o;
			if(chunk != null) addChunk(chunk);
		}
	}
	public short getBPM100() {
		if(chunk == null) chunk = new RLNDChunk();
		return chunk.getBPM100();
	}

	public void setBPM100(short bpm100) {
		if(chunk == null) chunk = new RLNDChunk();
		chunk.setBPM100(bpm100);
	}

	public float getBPM() {
		if(chunk == null) chunk = new RLNDChunk();
		return chunk.getBPM();
	}

	public void setBPM(float bpm) {
		if(chunk == null) chunk = new RLNDChunk();
		chunk.setBPM(bpm);
	}

	public String getSampleName() {
		if(chunk == null) chunk = new RLNDChunk();
		return chunk.getSampleName();
	}

	public void setSampleName(String s) {
		if(chunk == null) chunk = new RLNDChunk();
		chunk.setSampleName(s);
	}

	public int getSampleStart() {
		if(chunk == null) chunk = new RLNDChunk();
		return chunk.getSampleStart();
	}

	public void setSampleStart(int start) {
		if(chunk == null) chunk = new RLNDChunk();
		chunk.setSampleStart(start);
	}

	public int getLoopStart() {
		if(chunk == null) chunk = new RLNDChunk();
		return chunk.getLoopStart();
	}

	public void setLoopStart(int start) {
		if(chunk == null) chunk = new RLNDChunk();
		chunk.setLoopStart(start);
	}

	public int getSampleEnd() {
		if(chunk == null) chunk = new RLNDChunk();
		return chunk.getSampleEnd();
	}

	public void setSampleEnd(int end) {
		if(chunk == null) chunk = new RLNDChunk();
		chunk.setSampleEnd(end);
	}

	public int getSampleLength() {
		if(chunk == null) chunk = new RLNDChunk();
		return chunk.getSampleLength();
	}

	public void setSampleLength(int length) {
		if(chunk == null) chunk = new RLNDChunk();
		chunk.setSampleLength(length);
	}

	public byte getFineStart() {
		if(chunk == null) chunk = new RLNDChunk();
		return chunk.getFineStart();
	}

	public void setFineStart(byte fine) {
		if(chunk == null) chunk = new RLNDChunk();
		chunk.setFineStart(fine);
	}

	public byte getFineLoopStart() {
		if(chunk == null) chunk = new RLNDChunk();
		return chunk.getFineLoopStart();
	}

	public void setFineLoopStart(byte fine) {
		if(chunk == null) chunk = new RLNDChunk();
		chunk.setFineLoopStart(fine);
	}

	public byte getFineLoopEnd() {
		if(chunk == null) chunk = new RLNDChunk();
		return chunk.getFineLoopEnd();
	}

	public void setFineLoopEnd(byte fine) {
		if(chunk == null) chunk = new RLNDChunk();
		chunk.setFineLoopEnd(fine);
	}

	public byte getLoopMode() {
		if(chunk == null) chunk = new RLNDChunk();
		return chunk.getLoopMode();
	}

	public void setLoopMode(byte mode) {
		if(chunk == null) chunk = new RLNDChunk();
		chunk.setLoopMode(mode);
	}

	public byte getLoopTune() {
		if(chunk == null) chunk = new RLNDChunk();
		return chunk.getLoopTune();
	}

	public void setLoopTune(byte tune) {
		if(chunk == null) chunk = new RLNDChunk();
		chunk.setLoopTune(tune);
	}

	public byte getRootKey() {
		if(chunk == null) chunk = new RLNDChunk();
		return chunk.getRootKey();
	}

	public void setRootKey(byte key) {
		if(chunk == null) chunk = new RLNDChunk();
		chunk.setRootKey(key);
	}

	public short getTimeStretchType() {
		if(chunk == null) chunk = new RLNDChunk();
		return chunk.getTimeStretchType();
	}

	public void setTimeStretchType(short type) {
		if(chunk == null) chunk = new RLNDChunk();
		chunk.setTimeStretchType(type);
	}
}