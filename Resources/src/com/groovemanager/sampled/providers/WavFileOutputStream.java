package com.groovemanager.sampled.providers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;

/**
 * This class is an AudioFileOutputStream implementation for writing Wav files.
 * @author Manu Robledo
 *
 */
public class WavFileOutputStream extends AudioFileOutputStream {
	/**
	 * Indicates whether this stream has been closed or not
	 */
	private boolean closed = false;
	/**
	 * List of additional WaveChunks to write when closing
	 */
	private ArrayList chunks = new ArrayList();
	/**
	 * Number of bytes written so far
	 */
	private int written = 0;
	/**
	 * A byteBuffer wrapped around the Wav header
	 */
	private ByteBuffer header;
	/**
	 * The header data containing the RIFF identifier, the WAVE identifier,
	 * the fmt chunk, the data chunk ID and the data chunk length
	 */
	private static byte[] headerData = new byte[44];
	/**
	 * Bytelength of all chunks
	 */
	private int chunklength;
	/**
	 * FileOutputStream for writing
	 */
	private FileOutputStream out;
	/**
	 * Construct a new WavFileOutputStream 
	 * @param f The file to write to
	 * @param format The format in which to write audio data
	 * @param properties Map of properties to write to the file, if supported
	 * @throws IOException If an I/O Error occurs during OutputStream creation
	 */
	public WavFileOutputStream(File f, AudioFormat format, Map properties) throws IOException {
		super(f, format, AudioFileFormat.Type.WAVE, properties);
		
		out = new FileOutputStream(f);
		
		// Leave some space for the header...
		header = ByteBuffer.wrap(headerData);
		header.order(ByteOrder.LITTLE_ENDIAN);
		initHeader();
		out.write(headerData);
	}
	/**
	 * Add a WaveChunk to be written to the file
	 * @param c The WaveChunk to add
	 */
	public void addChunk(WaveChunk c){
		chunks.add(c);
	}
	public void write(int b) throws IOException {
		out.write(b);
		written++;
	}
	public void write(byte[] b) throws IOException {
		out.write(b);
		written += b.length;
	}
	public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
		written += len;
	}
	public void flush() throws IOException {
	}
	public void close() throws IOException {
		if(closed) return;
		writeChunks();
		writeHeader();
		closed = true;
	}
	/**
	 * Initialize the header with the data known so far 
	 *
	 */
	private void initHeader(){
		header.rewind();
		//**
		// RIFF-Header
		//**
		// "RIFF"
		header.put("RIFF".getBytes());
		// Länge Der Datei - 8
		header.putInt(36);
		// WAVE
		header.put("WAVE".getBytes());

		//**
		// fmt-Chunk
		//**
		// fmt
		header.put("fmt ".getBytes());
		// Länge 16
		header.putInt(16);
		// Formattyp (1: PCM)
		header.putShort((short)1);
		// Kanalzahl
		header.putShort((short)format.getChannels());
		// Sampling Rate (in Hz)
		header.putInt((int)format.getSampleRate());
		// Bytes pro Sekunde
		header.putInt((int)(format.getSampleRate() * format.getSampleSizeInBits() / 8));
		// Bytes pro Sample
		header.putShort((short)(format.getSampleSizeInBits() * format.getChannels() / 8));
		// Bits pro Sample
		header.putShort((short)format.getSampleSizeInBits());
		
		//**
		// data-Chunk
		//**
		header.put("data".getBytes());
		header.putInt(0);
	}
	/**
	 * Write the header data after all other data has been written
	 * @throws IOException If an I/O Error occurs during the write operation
	 */
	private void writeHeader() throws IOException{
		initHeader();
		header.putInt(4, written + chunklength + 80);
		header.putInt(40, written);
		out.close();
		RandomAccessFile rafile = new RandomAccessFile(outputFile, "rw");
		rafile.seek(0);
		rafile.write(headerData, 0, 44);
		rafile.close();
	}
	/**
	 * Write the chunks to the file 
	 * @throws IOException If an I/O Error occurs during the write operation
	 */
	private void writeChunks()throws IOException{
		chunklength = 0;
		for(Iterator iter = chunks.iterator(); iter.hasNext();){
			chunklength += ((WaveChunk)iter.next()).writeToOut(out);
		}
	}
}