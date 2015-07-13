/*
 * Created on 13.06.2004
 *
 */
package com.groovemanager.spi.rex;

import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;

/**
 * This class specifies the AudioFileFormat of a REX file.
 * The following properties are accesible via the getProperty()-method
 * if they could be found in the file:<br>
 * - duration: The duration of this file in microseconds (Long)<br>
 * - author (optional): name, email and URL of the author (String)<br>
 * - copyright (optional): a string containing the copyright information (String)<br>
 * - comment (optional): a free text (String)<br>
 * - slice_count: The number of slices found in this file (Integer)<br>
 * - bpm: The Tempo of the file (Float)<br>
 * - time_signature: An int-Array containing the nominator of the time signature
 * at position [0] and the denominator at position [1] (int[])<br>
 * - slices: A two-dimensional int-Array which contains information about the
 * slices in this file. Sor each slice there is one entry in the first dimension
 * two entries in the second dimension describing the position and the length
 * of the slice in sample frames. For example the position of the third slice
 * can be found at slices[2][0] and the duration of the first slice at slices[0][1].
 * @author Manu Robledo
 *
 */
public class REXAudioFileFormat extends AudioFileFormat {
	// For 1.4 compatibility...
	/**
	 * Map of properties assigned to this AudioFileFormat
	 */
	protected Map properties = new HashMap();
	/**
	 * Constructs a new REXAudioFileFormat
	 * @param format The Format of the audio data
	 * @param length The length in sample frames of the audio data 
	 * @param properties The Map of properties
	 */
	public REXAudioFileFormat(REXAudioFormat format, int length, Map properties) {
		super(Type.REX, format, length);
		this.properties = properties;
	}
	/**
	 * Constructs a new REXAudioFileFormat
	 * @param info The RexInfo obtained from REXGetInfo() 
	 * @param cInfo The REXCreatorInfo obtained from REXGetCreatorInfo(), may
	 * be null if no creator info is available
	 * @param sliceInfos Array of REXSliceInfo-objects obtained from
	 * REXGetSliceInfo()
	 */
	public REXAudioFileFormat(REXInfo info, REXCreatorInfo cInfo, REXSliceInfo[] sliceInfos){
		this(info.getAudioFormat(), info.getFrameLength(), createProperties(info, cInfo, sliceInfos));
	}
	/**
	 * Create and return the properties Map out of the given data
	 * @param info The RexInfo obtained from REXGetInfo() 
	 * @param cInfo The REXCreatorInfo obtained from REXGetCreatorInfo(), may
	 * be null if no creator info is available
	 * @param sInfos Array of REXSliceInfo-objects obtained from
	 * @return The Map with the properties of this REX file
	 */
	private static Map createProperties(REXInfo info, REXCreatorInfo cInfo, REXSliceInfo[] sInfos){
		Map map = new HashMap();
		// duration
		long duration = (long)Math.round(info.getFrameLength() / (double)info.fSampleRate() * 1000000.0);
		map.put("duration", new Long(duration));
		if(cInfo != null){
			// author
			String author = cInfo.fName();
			if(!cInfo.fEmail().equals("")) author += " <" + cInfo.fEmail() + ">";
			if(!cInfo.fURL().equals("")) author += " [" + cInfo.fURL() + "]";
			map.put("author", author);
			// copyright
			map.put("copyright", cInfo.fCopyright());
			// comment
			map.put("comment", cInfo.fFreeText());
		}
		// slice count
		int sliceCount = sInfos.length;
		map.put("slice_count", new Integer(sliceCount));
		// tempo
		map.put("bpm", new Float(info.fTempo() / 1000.0f));
		// time signature
		map.put("time_signature", new int[]{info.fTimeSigNom(), info.fTimeSigDenom()});
		
		// slices
		int[][] slices = new int[sliceCount][2];
		for(int i = 0; i < sliceCount; i++){
			slices[i][0] = sInfos[i].samplePos();
			slices[i][1] = sInfos[i].fSamplelength();
		}
		map.put("slices", slices);
		
		return map;
	}
	/**
	 * @see javax.sound.sampled.AudioFileFormat#getProperty(java.lang.String)
	 */
	// For 1.4 compatibility...
	public Object getProperty(String key) {
		return properties.get(key);
	}
	/**
	 * @see javax.sound.sampled.AudioFileFormat#properties()
	 */
	// For 1.4 compatibility...
	public Map properties() {
		return properties;
	}
	// For 1.4 compatibility...
	public static class Type extends AudioFileFormat.Type{
		final static public Type REX = new Type("REX", "RX2");
		protected Type(String name, String extension) {
			super(name, extension);
		}
		
	}
}
