/*
 * Created on 23.04.2004
 *
 */
package com.groovemanager.spi.floatEncoding;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.spi.FormatConversionProvider;

/**
 * A FloatConversionProvider is an implementation of the
 * FormatConversionProvider SPI. It provides conversion from and to
 * PCM_SIGNED or PCM_FLOAT encoded data, where PCM_FLOAT stands for floating
 * point samples normalized to +/- 1.0 either in 32 or 64 Bit.
 * @author Manu Robledo
 *
 */
public class FloatConversionProvider extends FormatConversionProvider {
	/**
	 * The encodings supported by this provider
	 */
	private final static Encoding[] encodings = new Encoding[]{FloatAudioFormat.Encoding.PCM_FLOAT, Encoding.PCM_SIGNED};
	/**
	 * Constructs a new FloatConversionProvider
	 *
	 */
	public FloatConversionProvider() {
	}
	/**
	 * 
	 * @see javax.sound.sampled.spi.FormatConversionProvider#getSourceEncodings()
	 */
	public Encoding[] getSourceEncodings() {
		return encodings;
	}

	/**
	 * 
	 * @see javax.sound.sampled.spi.FormatConversionProvider#getTargetEncodings()
	 */
	public Encoding[] getTargetEncodings() {
		return encodings;
	}
	/**
	 * Find out, if the given format is a valid PCM encoded format that is being
	 * supported by this provider
	 * @param format The AudioFormat to ask for
	 * @return true, if<br>
	 * - the sample rate is the same as the frame rate,<br>
	 * - the sample size in Bits is dividable by 8,<br>
	 * - the frame size is the sample size multiplied by the number of channels
	 * divided by 8.<br>
	 * false otherwise
	 */
	private boolean isSupportedPCMFormat(AudioFormat format){
		return format.getFrameRate() == format.getSampleRate() && format.getSampleSizeInBits() % 8 == 0 && format.getFrameSize() == format.getChannels() * format.getSampleSizeInBits() / 8;
	}
	/**
	 * Find out, if the given format is supported by this provider
	 * @param sourceFormat The format to check for
	 * @return true, if the given format is supported, false otherwise
	 */
	private boolean isSourceFormatSupported(AudioFormat sourceFormat){
		// Format PCM?
		if(!isSupportedPCMFormat(sourceFormat)) return false;
		
		// Encoding
		Encoding sourceEncoding = sourceFormat.getEncoding();
		if(!isSourceEncodingSupported(sourceEncoding)) return false;
		
		// Sample Size
		int sourceSamplesize = sourceFormat.getSampleSizeInBits();
		if(sourceEncoding.equals(Encoding.PCM_SIGNED) && (sourceSamplesize == 16 || sourceSamplesize == 24 || sourceSamplesize == 32 || sourceSamplesize == 64)) return true;
		else if(sourceEncoding.equals(FloatAudioFormat.Encoding.PCM_FLOAT) && (sourceSamplesize == 32 || sourceSamplesize == 64)) return true;
		else return false;
	}

	/**
	 * 
	 * @see javax.sound.sampled.spi.FormatConversionProvider#getTargetEncodings(javax.sound.sampled.AudioFormat)
	 */
	public Encoding[] getTargetEncodings(AudioFormat sourceFormat) {
		if(isSourceFormatSupported(sourceFormat)) return encodings;
		else return new Encoding[0];
	}
	/**
	 * Get the number of possible formats
	 * @return Always 12 (2 PCM_FLOAT + 4 PCM_SIGNED) * 2 (endianess)
	 */
	private int getFormatLength(){
		// Anzahl an möglichen AudioFormaten unabhängig von Sampling-Rate und channels
		// 2 PCM_FLOAT und 4 PCM_SIGNED, das ganze mal 2 (Endianess)
		return (2 + 4) * 2;
	}
	/**
	 * 
	 * @see javax.sound.sampled.spi.FormatConversionProvider#getTargetFormats(javax.sound.sampled.AudioFormat.Encoding, javax.sound.sampled.AudioFormat)
	 */
	public AudioFormat[] getTargetFormats(Encoding targetEncoding,
			AudioFormat sourceFormat) {
		boolean supported = isSourceEncodingSupported(targetEncoding);
		supported = isSourceFormatSupported(sourceFormat);
		if(isSourceEncodingSupported(targetEncoding) && isSourceFormatSupported(sourceFormat)){
			AudioFormat[] formats = new AudioFormat[getFormatLength()];
			
			formats[0] = new AudioFormat(FloatAudioFormat.Encoding.PCM_FLOAT, sourceFormat.getSampleRate(), 32, sourceFormat.getChannels(), 4 * sourceFormat.getChannels(), sourceFormat.getSampleRate(), true);
			formats[1] = new AudioFormat(FloatAudioFormat.Encoding.PCM_FLOAT, sourceFormat.getSampleRate(), 32, sourceFormat.getChannels(), 4 * sourceFormat.getChannels(), sourceFormat.getSampleRate(), false);

			formats[2] = new AudioFormat(FloatAudioFormat.Encoding.PCM_FLOAT, sourceFormat.getSampleRate(), 64, sourceFormat.getChannels(), 8 * sourceFormat.getChannels(), sourceFormat.getSampleRate(), true);
			formats[3] = new AudioFormat(FloatAudioFormat.Encoding.PCM_FLOAT, sourceFormat.getSampleRate(), 64, sourceFormat.getChannels(), 8 * sourceFormat.getChannels(), sourceFormat.getSampleRate(), false);

			formats[4] = new AudioFormat(Encoding.PCM_SIGNED, sourceFormat.getSampleRate(), 16, sourceFormat.getChannels(), 2 * sourceFormat.getChannels(), sourceFormat.getSampleRate(), true);
			formats[5] = new AudioFormat(Encoding.PCM_SIGNED, sourceFormat.getSampleRate(), 16, sourceFormat.getChannels(), 2 * sourceFormat.getChannels(), sourceFormat.getSampleRate(), false);

			formats[6] = new AudioFormat(Encoding.PCM_SIGNED, sourceFormat.getSampleRate(), 24, sourceFormat.getChannels(), 3 * sourceFormat.getChannels(), sourceFormat.getSampleRate(), true);
			formats[7] = new AudioFormat(Encoding.PCM_SIGNED, sourceFormat.getSampleRate(), 24, sourceFormat.getChannels(), 3 * sourceFormat.getChannels(), sourceFormat.getSampleRate(), false);

			formats[8] = new AudioFormat(Encoding.PCM_SIGNED, sourceFormat.getSampleRate(), 32, sourceFormat.getChannels(), 4 * sourceFormat.getChannels(), sourceFormat.getSampleRate(), true);
			formats[9] = new AudioFormat(Encoding.PCM_SIGNED, sourceFormat.getSampleRate(), 32, sourceFormat.getChannels(), 4 * sourceFormat.getChannels(), sourceFormat.getSampleRate(), false);

			formats[10] = new AudioFormat(Encoding.PCM_SIGNED, sourceFormat.getSampleRate(), 64, sourceFormat.getChannels(), 8 * sourceFormat.getChannels(), sourceFormat.getSampleRate(), true);
			formats[11] = new AudioFormat(Encoding.PCM_SIGNED, sourceFormat.getSampleRate(), 64, sourceFormat.getChannels(), 8 * sourceFormat.getChannels(), sourceFormat.getSampleRate(), false);
			
			return formats;
		}
		else return new AudioFormat[0];
	}
	/**
	 * If only the target encoding is given, but not the target format, get a
	 * default target format out of the source format and the target encoding 
	 * @param sourceFormat The source AudioFormat
	 * @param targetEncoding The target encoding
	 * @return An AudioFormat with the targetEncoding and similar attributes
	 * to the sourceFormat
	 */
	private static AudioFormat getDefaultConversionFormat(AudioFormat sourceFormat, Encoding targetEncoding){
		if(targetEncoding.equals(sourceFormat.getEncoding())) return sourceFormat;
		if(sourceFormat.getSampleSizeInBits() <= 32)
			return new AudioFormat(targetEncoding, sourceFormat.getSampleRate(), 32, sourceFormat.getChannels(), 4 * sourceFormat.getChannels(), sourceFormat.getSampleRate(), sourceFormat.isBigEndian());
		else
			return new AudioFormat(targetEncoding, sourceFormat.getSampleRate(), 64, sourceFormat.getChannels(), 8 * sourceFormat.getChannels(), sourceFormat.getSampleRate(), sourceFormat.isBigEndian());
	}
	/**
	 * 
	 * @see javax.sound.sampled.spi.FormatConversionProvider#getAudioInputStream(javax.sound.sampled.AudioFormat.Encoding, javax.sound.sampled.AudioInputStream)
	 */
	public AudioInputStream getAudioInputStream(Encoding targetEncoding,
			AudioInputStream sourceStream) {
		if(!isSourceFormatSupported(sourceStream.getFormat())) throw new IllegalArgumentException("Conversion not supported");
		if(!isSourceEncodingSupported(targetEncoding)) throw new IllegalArgumentException("Conversion not supported");
		return new AudioInputStream(new FloatConversionInputStream(sourceStream, getDefaultConversionFormat(sourceStream.getFormat(), targetEncoding)), getDefaultConversionFormat(sourceStream.getFormat(), targetEncoding), sourceStream.getFrameLength());
	}
	/**
	 * 
	 * @see javax.sound.sampled.spi.FormatConversionProvider#getAudioInputStream(javax.sound.sampled.AudioFormat, javax.sound.sampled.AudioInputStream)
	 */
	public AudioInputStream getAudioInputStream(AudioFormat targetFormat,
			AudioInputStream sourceStream) {
		
		if(!isSourceFormatSupported(sourceStream.getFormat())) throw new IllegalArgumentException("Conversion not supported");
		if(!isSourceFormatSupported(targetFormat)) throw new IllegalArgumentException("Conversion not supported");
		if(targetFormat.getSampleRate() != sourceStream.getFormat().getSampleRate()) throw new IllegalArgumentException("Conversion not supported");
		if(targetFormat.getChannels() != sourceStream.getFormat().getChannels()) throw new IllegalArgumentException("Conversion not supported");
		return new AudioInputStream(new FloatConversionInputStream(sourceStream, targetFormat), targetFormat, sourceStream.getFrameLength());
	}
}