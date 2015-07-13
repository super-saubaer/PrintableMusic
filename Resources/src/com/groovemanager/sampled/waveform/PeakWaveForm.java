package com.groovemanager.sampled.waveform;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.eclipse.swt.widgets.Shell;

import com.groovemanager.exception.NotFinishedException;
import com.groovemanager.gui.custom.ProgressMonitor;


/**
 * A PeakWaveForm is a WaveForm that gets its data out of a number of peak
 * values that have been created once for a file. Each peak value represents
 * a number of sample frames in the original file and consists of a maximum
 * and a minimum value for each cahnnel. Mostly this peak data is stored in a
 * peak file, that is assigned to an audio file by giving it the name of the
 * original file with an added ".gmpk" extension. The file myfile.wav.gmpk is
 * for example the peak file of the file myfile.wav in the same directory.<br>
 * A PeakWaveForm can also be created out of another WaveForm. In this case,
 * the peak values are created when they are first needed and then stored in
 * memory.
 * @author Manu Robledo
 *
 */
public class PeakWaveForm extends AbstractWaveForm {
	/**
	 * The peak data
	 */
	protected byte[] data;
	/**
	 * PeakWaveForms can also be created on the fly from a source WaveForm.
	 * If this PeakWaveForm has been created in this way, the source WaveForm
	 * is stored in this variable
	 */
	protected WaveForm source = null;
	/**
	 * The default extension for peak files
	 */
	public final static String DEFAULT_PEAKFILE_EXTENSION = "gmpk";
	/**
	 * Create a new PeakWaveForm out of the given peak file
	 * @param peakFile The peak file containing the peak data
	 * @throws IOException If an I/O Error occured during reading
	 */
	public PeakWaveForm(File peakFile) throws IOException{
		readFromFile(peakFile);
	}
	/**
	 * Read the peak data from the given file
	 * @param peakFile The file to read from
	 * @throws IOException If an I/O Error occured during reading
	 */
	protected void readFromFile(File peakFile) throws IOException{
		FileInputStream in = new FileInputStream(peakFile);
		byte[] header = new byte[34];
		in.read(header);
		ByteBuffer b = ByteBuffer.wrap(header);
		b.order(ByteOrder.LITTLE_ENDIAN);
		channels = b.getShort(24);
		displayWidth = (int)b.getLong(26);
		intervallSize = b.getInt(4);
		if(peakFile.length() != 34 + channels * 2 * displayWidth) throw new IllegalArgumentException("Length of Peak File Invalid: " + peakFile.length() + "; expected: " + (34 + channels * 2 * displayWidth));
		realLength = (int)b.getLong(16);
		
		data = new byte[displayWidth * channels * 2];
		in.read(data);
		in.close();
	}
	/**
	 * Construct a new PeakWaveForm out of the given peak data. The first 34
	 * bytes of the data must be a valid peak file header.
	 * @param data The peak data
	 */
	public PeakWaveForm(byte[] data){
		ByteBuffer b = ByteBuffer.wrap(data, 0, 34);
		b.order(ByteOrder.LITTLE_ENDIAN);
		channels = b.getShort(24);
		displayWidth = (int)b.getLong(26);
		realLength = (int)b.getLong(16);
		intervallSize = b.getInt(4);
		
		ByteArrayInputStream in = new ByteArrayInputStream(data, 34, data.length - 34);
		
		this.data = new byte[displayWidth * channels * 2];
		try {
			in.read(this.data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Get a PeakWaveForm for the given WaveForm 
	 * @param source The WaveForm to get a PeakWaveForm for.
	 * @param newWidth The width of the new PeakWaveForm
	 * @param iSize The interval size to use for the new PeakWaveForm
	 * @return A PeakWaveForm with the given interval size and the given width
	 * or the given source WaveForm, if no PeakWaveForm is needed for the
	 * desired width.
	 */
	public static WaveForm getPeak(WaveForm source, int newWidth, int iSize){
		while(source.getDisplayableLength() / iSize > newWidth) source = new PeakWaveForm(source, (int)Math.round(source.getDisplayableLength() / (double)iSize));
		return new ZoomedWaveForm(source, newWidth);
	}
	public WaveForm subWaveForm(int begin, int length, int width) {
		if(source != null && !super.canProvide(begin, length, width)){
			double temp = source.getDisplayableLength() / (double)getDisplayableLength();
			return source.subWaveForm((int)Math.round(begin * temp), (int)Math.round(length * temp), width); 
		}
		else return super.subWaveForm(begin, length, width);
	}
	/**
	 * Private Constructor to be used from <code>getPeak()</code>.
	 * @param source The source WaveForm
	 * @param newWidth The width of the WaveForm to create
	 */
	private PeakWaveForm(WaveForm source, int newWidth){
		this.source = source;
		channels = source.getChannels();
		data = new byte[channels * newWidth * 2];
		displayWidth = newWidth;
		realLength = source.getRealLength();
		ByteBuffer b = ByteBuffer.wrap(data);
		
		source.rewind();
		byte[] temp = new byte[channels * 2];
		for(int i = 0; i < newWidth; i++){
			for(int j = 0; j < channels; j++){
				temp[2 * j] = Byte.MAX_VALUE;
				temp[2 * j + 1] = Byte.MIN_VALUE;
			}
			do{
				for(int j = 0; j < channels; j++){
					temp[2 * j] = (byte)Math.min(temp[2 * j], source.getMin(j));
					temp[2 * j + 1] = (byte)Math.max(temp[2 * j + 1], source.getMax(j));
				}
			}
			while(source.next() && source.getPosition() * displayWidth / (double)source.getDisplayableLength() < i + 1);
			b.put(temp);
		}
	}
	
	public byte getMin(int channel) {
		int pos = 2 * (channels * position + channel);
		if(pos >= data.length){
			return 0;
		}
		return data[pos];
	}

	public byte getMax(int channel) {
		int pos = 2 * (channels * position + channel) + 1;
		if(pos >= data.length){
			return 0;
		}
		return data[pos];
	}
	protected String getAdditionalToString() {
		return "\tsource: " + source + "\n";
	}
	/**
	 * Check, if the given peak file is a valid peak file for the given audio
	 * file.
	 * @param f The audio file
	 * @param p The peak file
	 * @return true, if<br>
	 * - both files exists,<br>
	 * - the peak file´s header data matches the properties of the audio file and<br>
	 * - the peak file´s length matches the peak file´s header data,<br>
	 * false otherwise
	 */
	public static boolean checkPeakFile(File f, File p){
		// Existiert das Peak-File?
		if(! p.exists()) return false;
		// Header überprüfen
		FileInputStream in;
		ByteBuffer b;
		try {
			in = new FileInputStream(p);
			byte[] header = new byte[34];
			in.read(header);
			b = ByteBuffer.wrap(header);
			b.order(ByteOrder.LITTLE_ENDIAN);
			byte[] temp = new byte[4];
			b.get(temp);
			// GMPK
			if(!(new String(temp).equals("GMPK"))) return false;
			// Intervallgröße
			int iSize = b.getInt();
			if(iSize <= 0) return false;
			
			// Original Änderungsdatum
			long mod = b.getLong();
			if(mod != -1 && mod != f.lastModified()) return false;
			
			AudioInputStream ais = AudioSystem.getAudioInputStream(f);
			
			// Anzahl Samples
			long frameLength = b.getLong();
			if(frameLength == -1) frameLength = ais.getFrameLength();
			if(frameLength != ais.getFrameLength()) return false;
			
			// Anzahl Kanäle
			short channels = b.getShort();
			if(channels != ais.getFormat().getChannels()) return false;
			
			// Anzahl Snapshots
			long snapshots = b.getLong();
			long expectedSnapshots = frameLength / iSize;
			if(frameLength % iSize > 0) expectedSnapshots++;
			if(snapshots != expectedSnapshots) return false;
			
			// Erwartete Länge des Peak-Files
			long expectedLength = 34 + 2 * snapshots * channels;
			
			if(p.length() != expectedLength){
				System.out.println(p.length());
				return false;
			}
			
			return true;
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * Get a PeakWaveForm for the given audio file. If a peak file for this file
	 * can be found and if it is a valif peak file, it will be used. Otherwise 
	 * a peak file for the given audio file will be created and a ProgressMonitor
	 * will be shown during the operation.<br>
	 * The peak file for a given audio file will be located in the same directory
	 * as the source file and will have the source file´s file name with an
	 * additional ".gmpk" extension. If no peak file is found and no peak file
	 * can be written to this filename, a temporary file is created in the
	 * temp directory. 
	 * @param source The audio file to create the peak file for
	 * @param shell The parent Shell, under which the ProgressMonitor should
	 * be opened
	 * @return The PeakWaveForm for the given audio file
	 * @throws UnsupportedAudioFileException If the audio file´s format cannot
	 * be read
	 * @throws NotFinishedException If no valid peak file was found and the
	 * operation for creating it didn´t finish correctly.
	 * @throws IOException If an I/O Error occured
	 */
	public static PeakWaveForm createPeakWaveForm(File source, Shell shell) throws UnsupportedAudioFileException, NotFinishedException, IOException{
		//TODO PeakWaveForm.subWaveForm besser machen
		File peak = new File(source.getAbsolutePath() + "." + DEFAULT_PEAKFILE_EXTENSION);
		if(checkPeakFile(source, peak))	return new PeakWaveForm(peak);
		
		if(!peak.canWrite()) peak = File.createTempFile("tmp_", ".gmpk");
		if(!peak.exists()) peak.createNewFile();
		
		CreatePeakFileThread pThread = new CreatePeakFileThread(source, peak);
		ProgressMonitor mon = new ProgressMonitor(shell, pThread, "Creating Peak File...", "...for " + source.getAbsolutePath());
		mon.start();
		
		return new PeakWaveForm(peak);
	}
	/*
	public static void printPeakHeader(File f){
		try {
			System.out.println(f);
			FileInputStream in = new FileInputStream(f);
			byte[] header = new byte[34];
			in.read(header, 0, 34);
			ByteBuffer b = ByteBuffer.wrap(header);
			b.order(ByteOrder.LITTLE_ENDIAN);
			System.out.println("GMPK: " + new String(header, 0, 4));
			b.position(4);
			System.out.println("Intervallgröße: " + b.getInt());
			System.out.println("Änderungsdatum: " + b.getLong());
			System.out.println("Sample Frames: " + b.getLong());
			System.out.println("Kanäle: " + b.getShort());
			System.out.println("Snapshots: " + b.getLong());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	*/
	/**
	 * Create a valid peak file header out of the given data
	 * @param iSize The interval size
	 * @param lastModified The last modification date of the audio file
	 * represented by this peak file or -1, if it is not known
	 * @param realLength The length of the audio data in the audio file in
	 * sample frames
	 * @param channels The number of channels of the audio file
	 * @param snapshots The number of peak values (snapshots) contained in this
	 * peak file 
	 * @return A byte-Array of length 34 containing the header data for the peak
	 * file
	 */
	public static byte[] getHeader(int iSize, long lastModified, long realLength, short channels, int snapshots){
		byte[] header = new byte[34];
		ByteBuffer buffer = ByteBuffer.wrap(header);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put("GMPK".getBytes());
		buffer.putInt(iSize);
		buffer.putLong(lastModified);
		buffer.putLong(realLength);
		buffer.putShort(channels);
		buffer.putLong(snapshots);
		return header;
	}
	/**
	 * Write the given source WaveForm as a peak file into a given output. The
	 * peak header will also be written.
	 * @param source The WaveForm to get the peak data from
	 * @param out The OutputStream to write the data to
	 * @param lastModified The last modification date of the audio file this
	 * peak data belongs to or -1, if it is not known
	 * @param iSize The interval size of the WabeForm
	 * @throws IOException If an I/O Error occurs during writing
	 */
	public static void writeToStream(WaveForm source, OutputStream out, long lastModified, int iSize) throws IOException{
		byte[] header = getHeader(iSize, lastModified, source.getRealLength(), (short)source.getChannels(), source.getDisplayableLength());
		out.write(header, 0, 34);
		
		source.rewind();
		do{
			for(int i = 0; i < source.getChannels(); i++){
				out.write(source.getMin(i));
				out.write(source.getMax(i));
			}
		}
		while(source.next());
	}
	public byte[] getData(){
		return data;
	}
}