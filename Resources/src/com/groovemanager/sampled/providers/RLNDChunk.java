/*
 * Created on 17.06.2004
 *
 */
package com.groovemanager.sampled.providers;

/**
 * This class provides support for reading/writing Roland MC-909 sample wav data.
 * MC-909 sample wav files have an additional RLND-Chunk that contains some
 * specific information about the sample. This Chunk is represented by this
 * class. 
 * @author Manu Robledo
 *
 */
public class RLNDChunk extends WaveChunk implements SMPLChunk{
	/**
	 * Construct a new empty RLND chunk
	 *
	 */
	public RLNDChunk() {
		super(SMPLChunk.RLND_ID, SMPLChunk.RLND_LENGTH);
		chunkData.rewind();
		//roifxvmc
		chunkData.put(SMPLChunk.RLND_BEGIN.getBytes(), 0, 8);
		// Length: 184
		chunkData.putInt(184);
		// 4
		chunkData.put(12 + 180, (byte)4);
		// 2
		chunkData.put(12 + 181, (byte)2);
	}
	/**
	 * Construct a RLND chunk out of the given chunk data. The chunk data must
	 * not contain the chunk ID ("RLND") or the chunk length
	 * @param data The chunk data
	 */
	public RLNDChunk(byte[] data){
		super(SMPLChunk.RLND_ID, data);
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.providers.SMPLChunk#getBPM100()
	 */
	public short getBPM100() {
		return chunkData.getShort(182 + 12);
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.providers.SMPLChunk#setBPM100(short)
	 */
	public void setBPM100(short bpm100) {
		chunkData.putShort(182 + 12, (short)bpm100);
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.providers.SMPLChunk#getBPM()
	 */
	public float getBPM() {
		return getBPM100() / 100.0f;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.providers.SMPLChunk#setBPM(float)
	 */
	public void setBPM(float bpm) {
		setBPM100((short)(bpm * 100));
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.providers.SMPLChunk#setSampleName(java.lang.String)
	 */
	public void setSampleName(String s) {
		byte[] b;
		if(s.length() == 16) b= s.getBytes();
		else if(s.length() < 16){
			b = new byte[16];
			System.arraycopy(s.getBytes(), 0, b, 0, s.length());
		}
		else{
			b = new byte[16];
			System.arraycopy(s.getBytes(), 0, b, 0, 16);
		}
		chunkData.position(12);
		chunkData.put(b, 0, 16);
	}
	
	public String getSampleName(){
		byte[] b = new byte[16];
		chunkData.position(12);
		chunkData.get(b, 0, 16);
		return new String(b);
	}

	public int getSampleStart() {
		return chunkData.getInt(16 + 12);
	}

	public void setSampleStart(int start) {
		chunkData.putInt(16 + 12, start);
	}

	public int getLoopStart() {
		return chunkData.getInt(20 + 12);
	}

	public void setLoopStart(int start) {
		chunkData.putInt(20 + 12, start);
	}

	public int getSampleEnd() {
		return chunkData.getInt(24 + 12);
	}

	public void setSampleEnd(int end) {
		chunkData.putInt(24 + 12, end);
	}

	public int getSampleLength() {
		return chunkData.getInt(28 + 12);
	}

	public void setSampleLength(int length) {
		chunkData.putInt(28 + 12, length);
	}

	public byte getFineStart() {
		return chunkData.get(172 + 12);
	}

	public void setFineStart(byte fine) {
		chunkData.put(172 + 12, fine);
	}

	public byte getFineLoopStart() {
		return chunkData.get(173 + 12);
	}

	public void setFineLoopStart(byte fine) {
		chunkData.put(173 + 12, fine);
	}

	public byte getFineLoopEnd() {
		return chunkData.get(174 + 12);
	}

	public void setFineLoopEnd(byte fine) {
		chunkData.put(174 + 12, fine);
	}

	public byte getLoopMode() {
		return chunkData.get(175 + 12);
	}

	public void setLoopMode(byte mode) {
		chunkData.put(175 + 12, mode);
	}

	public byte getLoopTune() {
		return chunkData.get(176 + 12);
	}

	public void setLoopTune(byte tune) {
		chunkData.put(176 + 12, tune);
	}

	public byte getRootKey() {
		return chunkData.get(177 + 12);
	}

	public void setRootKey(byte key) {
		chunkData.put(177 + 12, key);
	}

	public short getTimeStretchType() {
		return chunkData.getShort(178 + 12);
	}
	
	public void setTimeStretchType(short type) {
		chunkData.putShort(178 + 12, type);
	}

}