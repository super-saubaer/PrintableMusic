/*
 * Created on 10.05.2004
 *
 */
package com.groovemanager.sampled.providers;

/**
 * This interface provides support for reading/writing Roland MC-909 sample
 * data. MC-909 sample files (wav or aiff) have an additional RLND-Chunk that
 * contains specific information about the sample. This Chunk is represented by
 * this interface. 
 * @author Manu Robledo
 *
 */
public interface SMPLChunk {
	/**
	 * Constant file modification date used for MC-909 sample files
	 */
	final static long MODIFIED909 = 1031555340000l;
	/**
	 * Possible loop mode values
	 */
	final static byte LOOP_FWD = 0,
		LOOP_ONE_SHOT = 1,
		LOOP_REV = 2,
		LOOP_REV_ONE = 3;
	/**
	 * Chunk Id of the RLND-chunk
	 */
	final static String RLND_ID = "RLND";
	/**
	 * Constant String used at the beginning of the chunk
	 */
	final static String RLND_BEGIN = "roifxvmc";
	/**
	 * Length of the RLND chunk without its header
	 */
	final static int RLND_LENGTH = 196;
	/**
	 * Get the bpm multiplied by 100
	 * @return The bpm multiplied by 100 in the range from 500 to 30000
	 */
	short getBPM100();
	/**
	 * Set the bpm multiplied by 100
	 * @param bpm100 The bpm multiplied by 100 in the range from 500 to 30000
	 */
	void setBPM100(short bpm100);
	/**
	 * Get the bpm
	 * @return The bpm in the range from 5.0 to 300.0
	 */
	float getBPM();
	/**
	 * Set the bpm
	 * @param bpm The bpm in the range from 5.0 to 300.0
	 */
	void setBPM(float bpm);
	/**
	 * Get the name of the sample
	 * @return A String of length 16 containing the sample name
	 */
	String getSampleName();
	/**
	 * Set the name of the sample
	 * @param s A String containing the name. The name will be filled up or
	 * shortened to length 16 if needed
	 */
	void setSampleName(String s);
	/**
	 * Get sample start position
	 * @return The sample start position
	 */
	int getSampleStart();
	/**
	 * Set the sample start position
	 * @param start The sample start position
	 */
	void setSampleStart(int start);
	/**
	 * Get the loop start position
	 * @return The loop start position
	 */
	int getLoopStart();
	/**
	 * Set the llop start position
	 * @param start The loop start position
	 */
	void setLoopStart(int start);
	/**
	 * Get the sample end position
	 * @return The sample end position
	 */
	int getSampleEnd();
	/**
	 * Set the sample end position
	 * @param end The sample end position
	 */
	void setSampleEnd(int end);
	/**
	 * Get the sample length
	 * @return The sample length in sample frames
	 */
	int getSampleLength();
	/**
	 * Set the sample length
	 * @param length The sample length in sample frames
	 */
	void setSampleLength(int length);
	/**
	 * Get the sample fine start
	 * @return The sample fine start
	 */
	byte getFineStart();
	/**
	 * Set the sample fine start
	 * @param fine The sample fine start
	 */
	void setFineStart(byte fine);
	/**
	 * Get the loop fine start
	 * @return The loop fine start
	 */
	byte getFineLoopStart();
	/**
	 * Set the loop fine start
	 * @param fine The loop fine start
	 */
	void setFineLoopStart(byte fine);
	/**
	 * Get the loop fine end
	 * @return The loop fine end
	 */
	byte getFineLoopEnd();
	/**
	 * Set the loop fine end
	 * @param fine The loop fine end
	 */
	void setFineLoopEnd(byte fine);
	/**
	 * Get the loop mode
	 * @return The loop mode
	 */
	byte getLoopMode();
	/**
	 * Set the loop mode
	 * @param mode The loop mode
	 */
	void setLoopMode(byte mode);
	/**
	 * Get the loop tune
	 * @return The loop tune
	 */
	byte getLoopTune();
	/**
	 * Set the loop tune
	 * @param tune The loop tune
	 */
	void setLoopTune(byte tune);	
	/**
	 * Get the root key
	 * @return The root key
	 */
	byte getRootKey();
	/**
	 * Set the root key
	 * @param key The root key
	 */
	void setRootKey(byte key);
	/**
	 * Get the timestretch type
	 * @return The timestretch type
	 */
	short getTimeStretchType();
	/**
	 * Set the timestretch type
	 * @param type The timestretch type
	 */
	void setTimeStretchType(short type);
}