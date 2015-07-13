/*
 * Created on 14.05.2004
 *
 */
package com.groovemanager.sampled.waveform;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.groovemanager.sampled.AudioManager;
import com.groovemanager.thread.ProgressThread;

/**
 * This ProgressThread implementationcan be used to create a peak file out of
 * a given AudioInputStream. This peak file can then be used for creating a
 * PeakWaveForm.
 * @author Manu Robledo
 *
 */
public class CreatePeakFileThread extends ProgressThread{
	/**
	 * The source audio file if known
	 */
	protected File sourceFile;
	/**
	 * The OutputStream to write the peak file to
	 */
	protected OutputStream out;
	/**
	 * The AudioInputStream to read the peak data from
	 */
	protected AudioInputStream in;
	/**
	 * Temporary buffer
	 */
	protected byte[] p, buffer;
	/**
	 * The AudioFormat of the source data
	 */
	protected AudioFormat format;
	/**
	 * The AudioFormat´s frame size
	 */
	protected int bytesPerFrame,
	/**
	 * The sample size in bytes of the AudioFormat
	 */
	bytesPerSample;
	/**
	 * Number of bytes read in the last read operation. This value will be -1
	 * when the end of the input stream has been reached
	 */
	protected int numBytesRead = 0;
	/**
	 * Number of snapshot values written so far
	 */
	protected int valuesWritten = 0;
	/**
	 * temporary variable
	 */
	protected byte value = 0;
	/**
	 * buffer size
	 */
	protected int buffer_size = 8 * 1024;
	/**
	 * Number of snapshots to write 
	 */
	protected long snapshots;
	/**
	 * Default number of sample frames represented by one snapshot 
	 */
	public final static int DEFAULT_INTERVALL_SIZE = 100;
	/**
	 * Number of sample frames represented by one snapshot
	 */
	protected int iSize = DEFAULT_INTERVALL_SIZE;
	/**
	 * Current position inside one interval
	 */
	protected int intervallPosition = 0;
	protected Object result(){
		return out;
	}
	/**
	 * Construct a new CreatePeakFileThread that reads audio data from the
	 * given AudioInputStream and writes the peak data to the given OutputStream
	 * @param in The AudioInputStream to read from
	 * @param out The OutputStream to write to
	 */
	public CreatePeakFileThread(AudioInputStream in, OutputStream out){
		// evtl. Conversion
		format = new AudioFormat(in.getFormat().getSampleRate(), 16, in.getFormat().getChannels(), true, in.getFormat().isBigEndian());
		this.in = AudioSystem.getAudioInputStream(format, in);
		this.out = out;
	}
	/**
	 * Construct a new CreatePeakFileThread that reads audio data from the
	 * given AudioInputStream and writes the peak data into the given file
	 * @param in The AudioInputStream to read from
	 * @param peak The file to write the data to
	 * @throws FileNotFoundException If the file could not be opened for writing
	 */
	public CreatePeakFileThread(AudioInputStream in, File peak) throws FileNotFoundException{
		this(in, new FileOutputStream(peak));
	}
	/**
	 * Construct a new CreatePeakFileThread that reads audio data from the
	 * given audio file and writes the peak data to the given OutputStream
	 * @param source The audio file to read from
	 * @param out The OutputStream to write to
	 * @throws UnsupportedAudioFileException If the given audio file´s format
	 * can not be read
	 * @throws IOException If an I/O Error occured during file reading
	 */
	public CreatePeakFileThread(File source, OutputStream out) throws UnsupportedAudioFileException, IOException{
		this(AudioSystem.getAudioInputStream(source), out);
		sourceFile = source;
	}
	/**
	 * Construct a new CreatePeakFileThread that reads audio data from the
	 * given audio file and writes the peak data to the given OutputStream
	 * @param source The audio file to read from
	 * @param peak The file to write to
	 * @throws UnsupportedAudioFileException If the given audio file´s format
	 * can not be read
	 * @throws IOException If an I/O Error occured during file reading or
	 * OutputStream creation
	 */
	public CreatePeakFileThread(File source, File peak) throws UnsupportedAudioFileException, IOException{
		this(AudioSystem.getAudioInputStream(source), new FileOutputStream(peak));
		sourceFile = source;
	}
	/**
	 * Construct a new CreatePeakFileThread that reads audio data from the
	 * given AudioInputStream and writes the peak data to the given OutputStream
	 * @param in The AudioInputStream to read from
	 * @param out The OutputStream to write to
	 * @param intervallSize The interval size to use
	 */
	public CreatePeakFileThread(AudioInputStream in, OutputStream out, int intervallSize){
		this(in, out);
		iSize = intervallSize;
	}
	/**
	 * Construct a new CreatePeakFileThread that reads audio data from the
	 * given AudioInputStream and writes the peak data into the given file
	 * @param in The AudioInputStream to read from
	 * @param peak The file to write the data to
	 * @param intervallSize The interval size to use
	 * @throws FileNotFoundException If the file could not be opened for writing
	 */
	public CreatePeakFileThread(AudioInputStream in, File peak, int intervallSize) throws FileNotFoundException{
		this(in, peak);
		iSize = intervallSize;
	}
	/**
	 * Construct a new CreatePeakFileThread that reads audio data from the
	 * given audio file and writes the peak data to the given OutputStream
	 * @param source The audio file to read from
	 * @param out The OutputStream to write to
	 * @param intervallSize The interval size to use
	 * @throws UnsupportedAudioFileException If the given audio file´s format
	 * can not be read
	 * @throws IOException If an I/O Error occured during file reading
	 */
	public CreatePeakFileThread(File source, OutputStream out, int intervallSize) throws UnsupportedAudioFileException, IOException{
		this(source, out);
		iSize = intervallSize;
	}
	/**
	 * Construct a new CreatePeakFileThread that reads audio data from the
	 * given audio file and writes the peak data to the given OutputStream
	 * @param source The audio file to read from
	 * @param peak The file to write to
	 * @param intervallSize The interval size to use
	 * @throws UnsupportedAudioFileException If the given audio file´s format
	 * can not be read
	 * @throws IOException If an I/O Error occured during file reading or
	 * OutputStream creation
	 */
	public CreatePeakFileThread(File source, File peak, int intervallSize) throws UnsupportedAudioFileException, IOException{
		this(source, peak);
		iSize = intervallSize;
	}
	/**
	 * Generate the peak file´s header data. The peak file header is defined as
	 * follows:<br>
	 * - First 4 bytes: "GMPK" as ASCII-String<br>
	 * - Next 4 bytes: Integer containing used intervall size (= number of
	 * sample frames represented by one snapshot)<br>
	 * - Next 8 bytes: Long containing the modification date of the audio
	 * file this peak file belongs to or -1 if it is not known.<br>
	 * - Next 8 bytes: Number of sample frames contained in the original
	 * audio file<br>
	 * - Next 2 bytes: Short containing the number of channels of the source 
	 * audio file<br>
	 * - Next 8 bytes: Number of snapshots contained in this file.<br>A snapshot
	 * always consists of two bytes for each channel: A minimum value (first
	 * byte) and a maximum value (second byte)
	 * @return The header data as byte Array
	 */
	protected byte[] writeHeader(){
		// Header schreiben
		byte[] header = new byte[34];
		ByteBuffer b = ByteBuffer.wrap(header);
		b.order(ByteOrder.LITTLE_ENDIAN);
		// GMPK
		b.put("GMPK".getBytes());
		// Intervallgröße
		b.putInt(iSize);
		// Änderungsdatum Audiofile
		if(sourceFile == null) b.putLong(-1);
		else b.putLong(sourceFile.lastModified());
		// frameLength
		b.putLong(in.getFrameLength());
		// Kanäle
		b.putShort((short)in.getFormat().getChannels());
		// Snapshots
		snapshots = in.getFrameLength() / iSize;
		if(in.getFrameLength() % iSize > 0) snapshots++;
		b.putLong(snapshots);
		return header;
	}
	
	protected void init() {
		try{
			if(in.getFrameLength() == AudioSystem.NOT_SPECIFIED && sourceFile != null){
				in = new AudioInputStream(in, in.getFormat(), AudioManager.getDefault().getFrameLength(sourceFile));
			}
			out = new BufferedOutputStream(out);
			format = in.getFormat();
			bytesPerFrame = format.getFrameSize();
			bytesPerSample = bytesPerFrame / format.getChannels();
			p = new byte[2 * format.getChannels()];
			buffer = new byte[buffer_size * iSize * bytesPerFrame];
			out.write(writeHeader());
		}
		catch(Exception ex){
			ex.printStackTrace();
			cancelOperation();
		}
	}

	protected int tellTotal() {
		return (int)snapshots;
	}

	protected void processNext() {
		try {
			numBytesRead = in.read(buffer, 0, buffer.length);
			if(numBytesRead > 0){
				for(int k = 0; k < numBytesRead; k += iSize * bytesPerFrame){
					for(int i = 0; i < p.length / 2; i++){
						p[2 * i] = Byte.MAX_VALUE;
						p[2 * i + 1] = Byte.MIN_VALUE;
					}
					for(int i = 0; i < iSize * bytesPerFrame && i + k < numBytesRead; i += bytesPerFrame){
						// Für jeden Kanal
						for(int j = 0; j < format.getChannels(); j++){
							// Wert für nächstes Sample
							// jeweils das MSB
							if(format.isBigEndian()) value = buffer[k + i + j * bytesPerSample];
							else value = buffer[k + i + j * bytesPerSample + bytesPerSample - 1];
							int pos = j * 2;
							p[pos] = (byte)Math.min(p[pos], value);
							p[pos + 1] = (byte)Math.max(p[pos + 1], value);
						}
						intervallPosition++;
						if(intervallPosition == iSize){
							out.write(p);
							valuesWritten++;
							intervallPosition = 0;
						}
					}
				}
			}
			else if(intervallPosition > 0){
				out.write(p);
				valuesWritten++;
				intervallPosition = 0;
			}
		} catch (IOException e) {
			e.printStackTrace();
			cancelOperation();
		}
	}

	protected int tellElapsed() {
		return valuesWritten;
	}

	protected boolean breakCondition() {
		return numBytesRead == -1;
	}

	protected void cleanUp() {
		try{
			if(in != null) in.close();
			if(out != null) out.close();
		}
		catch(Exception ex){
		}
	}
}