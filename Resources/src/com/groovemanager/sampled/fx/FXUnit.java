/*
 * Created on 23.06.2004
 *
 */
package com.groovemanager.sampled.fx;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.Control.Type;

import com.groovemanager.spi.floatEncoding.FloatAudioFormat;
import com.groovemanager.tools.*;

/**
 * This implementation of Mixer is a questionable effort to think of an effect
 * unit as a Mixer. An FXUnit can handle the processing of audio data with an
 * Effect instance. A SourceDataLine for analysis and a SourceDataLine for
 * processing as well as a TargetDataLine for the resulting audio data are
 * provided by this Mixer and the effect큦 Controls can also be accessed through
 * the FXUnit.
 * @author Manu Robledo
 *
 */
public class FXUnit implements Mixer {
	/**
	 * The Mixer.Info object for this FXUnit
	 */
	final private Info mixerInfo;
	/**
	 * The SourceDataLine for sending the audio data to be processed
	 */
	final private FXSourceDataLine sourceLine;
	/**
	 * The SourceDataLine for sending the audio data to be analyzed
	 */
	final private FXAnalysisLine analysisLine;
	/**
	 * The TargetDataLine for receiving the processed audio data
	 */
	final private FXTargetDataLine targetLine;
	/**
	 * The Effect represented by this FXUnit
	 */
	final private Effect effect;
	/**
	 * A Float encoded 2 channel AudioFormat with NOT_SPECIFIED sample rate for
	 * use with DataLine.Info.getFormats()
	 */
	public final static AudioFormat FORMAT = new FloatAudioFormat(AudioSystem.NOT_SPECIFIED, false, 2, false),
	/**
	 * A Float encoded 2 channel AudioFormat with 44100Hz sample rate for
	 * use as default AudioFormat
	 */
	DEFAULT_FORMAT = new FloatAudioFormat(44100, false, 2, false);
	/**
	 * List of LineListeners
	 */
	private ArrayList lineListeners = new ArrayList();
	/**
	 * The default buffer size for effect processing
	 */
	private final static int DEFAULT_BUFFER_SIZE = 64 * 1024;
	/**
	 * The RingBuffer into which the FXSourceLine writes its data and from
	 * which the FXTargetLine reads its data
	 */
	private RingBuffer buffer;
	/**
	 * Array of two FloatBuffers used for processing 
	 */
	private FloatBuffer[] inBuffers = new FloatBuffer[2], outBuffers = new FloatBuffer[2];
	/**
	 * Indicates whether the FXSourceLine and/or the FXTargetLine are open or
	 * not
	 */
	private boolean lineOpen = false;
	/**
	 * The current used AudioFormat
	 */
	private AudioFormat currentFormat = DEFAULT_FORMAT;
	/**
	 * Create an AudioFormat object with the given sample rate
	 * @param sampleRate
	 * @return The new AudioFormat object
	 */
	protected AudioFormat createFormat(float sampleRate){
		return new FloatAudioFormat(sampleRate, false, 2, false);
	}
	/**
	 * Create the RingBuffer and the needed temporary Buffers for the
	 * specified buffer size in bytes
	 * @param bufferSize The buffer size in bytes
	 */
	private void createBuffers(int bufferSize){
		ByteBuffer b = ByteBuffer.allocate(bufferSize);
		buffer = new RingBuffer(b, 8);
		buffer.open();
		inBuffers[0] = FloatBuffer.allocate(bufferSize / 8);
		inBuffers[1] = FloatBuffer.allocate(bufferSize / 8);
		outBuffers[0] = FloatBuffer.allocate(bufferSize / 8);
		outBuffers[1] = FloatBuffer.allocate(bufferSize / 8);
	}
	/**
	 * Create a new FXUnit for the given Effect
	 * @param effect The Effect to wrap with an FXUnit
	 */
	public FXUnit(Effect effect){
		mixerInfo = createInfo();
		analysisLine = new FXAnalysisLine();
		sourceLine = new FXSourceDataLine();
		targetLine = new FXTargetDataLine();
		this.effect = effect;
	}
	/**
	 * Create the Mixer.Info object for this Unit
	 * @return The Mixer.Info object to be used for this unit
	 */
	protected Info createInfo(){
		return new Info("FXUnit Mixer", "GrooveManager", "FXUnit Mixer for Digital Signal Processing", "1.0");
	}
	
	/**
	 * @see javax.sound.sampled.Mixer#getMixerInfo()
	 */
	public Mixer.Info getMixerInfo() {
		return mixerInfo;
	}
	/**
	 * @see javax.sound.sampled.Mixer#getSourceLineInfo()
	 */
	public Line.Info[] getSourceLineInfo() {
		return new Line.Info[]{new DataLine.Info(FXSourceDataLine.class, FORMAT)};
	}
	/**
	 * Get the SourceDataLine that is used for analysis input
	 * @return A SourceDataLine into which the audio data for analyzation
	 * should be fed.
	 */
	public SourceDataLine getAnalysisLine(){
		return analysisLine;
	}
	/**
	 * Get the SourceDataLine used for effect input
	 * @return A SourceDataLine into which the audio data for processing
	 * should be fed.
	 */
	public SourceDataLine getEffectSourceLine(){
		return sourceLine;
	}
	/**
	 * Get the TargetDataLine from which the Effect큦 result can be read
	 * @return A TargetDataLine from which the processed audio data can be read
	 */
	public TargetDataLine getEffectTargetLine(){
		return targetLine;
	}
	/**
	 * @see javax.sound.sampled.Mixer#getTargetLineInfo()
	 */
	public Line.Info[] getTargetLineInfo() {
		return new Line.Info[]{new DataLine.Info(TargetDataLine.class, FORMAT)};
	}
	/**
	 * @see javax.sound.sampled.Mixer#getSourceLineInfo(javax.sound.sampled.Line.Info)
	 */
	public Line.Info[] getSourceLineInfo(Line.Info info) {
		if(!(info instanceof DataLine.Info)) return new Line.Info[0];
		DataLine.Info dataInfo = (DataLine.Info)info;
		if(SourceDataLine.class.isAssignableFrom(dataInfo.getLineClass()) && dataInfo.isFormatSupported(FORMAT)){
			return getSourceLineInfo();
		}
		else return new Line.Info[0];
	}
	/**
	 * @see javax.sound.sampled.Mixer#getTargetLineInfo(javax.sound.sampled.Line.Info)
	 */
	public Line.Info[] getTargetLineInfo(Line.Info info) {
		if(!(info instanceof DataLine.Info)) return new Line.Info[0];
		DataLine.Info dataInfo = (DataLine.Info)info;
		if(SourceDataLine.class.isAssignableFrom(dataInfo.getLineClass()) && dataInfo.isFormatSupported(FORMAT)){
			return getSourceLineInfo();
		}
		else return new Line.Info[0];
	}
	/**
	 * @see javax.sound.sampled.Mixer#isLineSupported(javax.sound.sampled.Line.Info)
	 */
	public boolean isLineSupported(Line.Info info) {
		if(!(info instanceof DataLine.Info)) return false;
		DataLine.Info dataInfo = (DataLine.Info)info;
		return dataInfo.isFormatSupported(FORMAT) && 
			(SourceDataLine.class.isAssignableFrom(dataInfo.getLineClass()) ||
			TargetDataLine.class.isAssignableFrom(dataInfo.getLineClass()));
		
	}
	/**
	 * @see javax.sound.sampled.Mixer#getLine(javax.sound.sampled.Line.Info)
	 */
	public Line getLine(Line.Info info)	throws LineUnavailableException {
		if(isLineSupported(info)){
			if(SourceDataLine.class.isAssignableFrom(info.getLineClass())) return sourceLine;
			else return targetLine;
		}
		throw new LineUnavailableException("Line not supported");
	}
	/**
	 * @see javax.sound.sampled.Mixer#getMaxLines(javax.sound.sampled.Line.Info)
	 */
	public int getMaxLines(Line.Info info) {
		if(isLineSupported(info)) return 1;
		else return 0;
	}
	/**
	 * @see javax.sound.sampled.Mixer#getSourceLines()
	 */
	public Line[] getSourceLines() {
		return new Line[]{sourceLine};
	}
	/**
	 * @see javax.sound.sampled.Mixer#getTargetLines()
	 */
	public Line[] getTargetLines() {
		return new Line[]{targetLine};
	}
	/**
	 * @see javax.sound.sampled.Mixer#synchronize(javax.sound.sampled.Line[], boolean)
	 */
	public void synchronize(Line[] lines, boolean maintainSync) {
	}
	/**
	 * @see javax.sound.sampled.Mixer#unsynchronize(javax.sound.sampled.Line[])
	 */
	public void unsynchronize(Line[] lines) {
	}
	/**
	 * @see javax.sound.sampled.Mixer#isSynchronizationSupported(javax.sound.sampled.Line[], boolean)
	 */
	public boolean isSynchronizationSupported(Line[] lines, boolean maintainSync) {
		return false;
	}
	/**
	 * @see javax.sound.sampled.Line#getLineInfo()
	 */
	public javax.sound.sampled.Line.Info getLineInfo() {
		return new Line.Info(Mixer.class);
	}
	/**
	 * @see javax.sound.sampled.Line#open()
	 */
	public void open() throws LineUnavailableException {
		if(isOpen()) return;
		effect.open(currentFormat.getSampleRate());
		notifyListeners(LineEvent.Type.OPEN);
	}
	/**
	 * @see javax.sound.sampled.Line#close()
	 */
	public void close() {
		if(!isOpen()) return;
		sourceLine.close();
		targetLine.close();
		analysisLine.close();
		effect.close();
		notifyListeners(LineEvent.Type.CLOSE);
	}
	/**
	 * @see javax.sound.sampled.Line#isOpen()
	 */
	public boolean isOpen() {
		return effect.isOpen();
	}
	/**
	 * @see javax.sound.sampled.Line#getControls()
	 */
	public Control[] getControls() {
		return effect.getControls();
	}
	/**
	 * @see javax.sound.sampled.Line#isControlSupported(javax.sound.sampled.Control.Type)
	 */
	public boolean isControlSupported(Type type) {
		return effect.isControlSupported(type);
	}
	/**
	 * @see javax.sound.sampled.Line#getControl(javax.sound.sampled.Control.Type)
	 */
	public Control getControl(Type type) {
		return effect.getControl(type);
	}
	/**
	 * Notify all registered LineListeners about a LineEvent
	 * @param type The type of the LineEvent
	 */
	private void notifyListeners(LineEvent.Type type){
		LineEvent event = new LineEvent(this, type, 0);
		for (Iterator iter = lineListeners.iterator(); iter.hasNext();) {
			LineListener listener = (LineListener) iter.next();
			listener.update(event);
		}
	}
	
	/**
	 * @see javax.sound.sampled.Line#addLineListener(javax.sound.sampled.LineListener)
	 */
	public void addLineListener(LineListener listener) {
		lineListeners.add(listener);
	}
	/**
	 * @see javax.sound.sampled.Line#removeLineListener(javax.sound.sampled.LineListener)
	 */
	public void removeLineListener(LineListener listener) {
		lineListeners.remove(listener);
	}
	/**
	 * A FXSourceDataLine is used for streaming audio data into the FXUnit 
	 * @author Manu Robledo
	 *
	 */
	class FXSourceDataLine implements SourceDataLine{
		/**
		 * List of LineListeners registered with this Line
		 */
		private ArrayList lineListeners = new ArrayList();
		/**
		 * Current frame position
		 */
		int framePosition = 0;
		/**
		 * This method will be called when the Effect is opened
		 *
		 */
		private void opened(){
			framePosition = 0;
			notifyListeners(LineEvent.Type.OPEN);
		}
		/**
		 * This method will be called when the Effect is closed
		 *
		 */
		private void closed(){
			framePosition = 0;
			notifyListeners(LineEvent.Type.CLOSE);
		}
		/**
		 * 
		 * @see javax.sound.sampled.SourceDataLine#open(javax.sound.sampled.AudioFormat, int)
		 */
		public void open(AudioFormat format, int bufferSize) throws LineUnavailableException {
			if(isOpen()) return;
			if(!((DataLine.Info)getLineInfo()).isFormatSupported(format)) throw new LineUnavailableException("Format not supported.");
			createBuffers(bufferSize);
			currentFormat = format;
			FXUnit.this.open();
			lineOpen = true;
			targetLine.opened();
			notifyListeners(LineEvent.Type.OPEN);
		}
		/**
		 * Notify all registered LineListeneres about a LineEvent
		 * @param type The type of the LineEvent
		 */
		private void notifyListeners(LineEvent.Type type){
			LineEvent event = new LineEvent(this, type, getFramePosition());
			for (Iterator iter = lineListeners.iterator(); iter.hasNext();) {
				LineListener listener = (LineListener) iter.next();
				listener.update(event);
			}
		}
		/**
		 * 
		 * @see javax.sound.sampled.Line#addLineListener(javax.sound.sampled.LineListener)
		 */
		public void addLineListener(LineListener listener) {
			lineListeners.add(listener);
		}
		/**
		 * 
		 * @see javax.sound.sampled.Line#removeLineListener(javax.sound.sampled.LineListener)
		 */
		public void removeLineListener(LineListener listener) {
			lineListeners.remove(listener);
		}

		/**
		 * @see javax.sound.sampled.SourceDataLine#open(javax.sound.sampled.AudioFormat)
		 */
		public void open(AudioFormat format) throws LineUnavailableException {
			if(isOpen()) return;
			if(!((DataLine.Info)getLineInfo()).isFormatSupported(format)) throw new LineUnavailableException("Format not supported.");
			createBuffers(DEFAULT_BUFFER_SIZE);
			currentFormat = format;
			FXUnit.this.open();
			lineOpen = true;
			targetLine.opened();
			notifyListeners(LineEvent.Type.OPEN);
		}

		/**
		 * @see javax.sound.sampled.SourceDataLine#write(byte[], int, int)
		 */
		public int write(byte[] b, int off, int length) {
			if(!isOpen()) return 0;
			int written = buffer.write(b, off, length);
			framePosition += written / 8;
			return written;
		}

		/**
		 * @see javax.sound.sampled.DataLine#drain()
		 */
		public void drain() {
			while(isOpen() && buffer.readAvailable() > 0)
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
		}

		/**
		 * @see javax.sound.sampled.DataLine#flush()
		 */
		public void flush() {
			if(isOpen()) buffer.flush();
		}

		/**
		 * @see javax.sound.sampled.DataLine#start()
		 */
		public void start() {
			if(!isOpen()) return;
			buffer.open();
		}

		/**
		 * @see javax.sound.sampled.DataLine#stop()
		 */
		public void stop() {
			if(!isActive()) return;
			buffer.close();
		}

		/**
		 * @see javax.sound.sampled.DataLine#isRunning()
		 */
		public boolean isRunning() {
			return isActive();
		}

		/**
		 * @see javax.sound.sampled.DataLine#isActive()
		 */
		public boolean isActive() {
			return isOpen() && buffer.isOpen();
		}

		/**
		 * @see javax.sound.sampled.DataLine#getFormat()
		 */
		public AudioFormat getFormat() {
			return currentFormat;
		}

		/**
		 * @see javax.sound.sampled.DataLine#getBufferSize()
		 */
		public int getBufferSize() {
			if(!isOpen()) return AudioSystem.NOT_SPECIFIED;
			else return buffer.size(); 
		}

		/**
		 * @see javax.sound.sampled.DataLine#available()
		 */
		public int available() {
			if(!isOpen()) return 0;
			else return buffer.writeAvailable(); 
		}

		/**
		 * @see javax.sound.sampled.DataLine#getFramePosition()
		 */
		public int getFramePosition() {
			if(!isOpen()) return 0;
			return framePosition - buffer.readAvailable();
		}

		/**
		 * @see javax.sound.sampled.DataLine#getMicrosecondPosition()
		 */
		public long getMicrosecondPosition() {
			return (long)Math.round(getFramePosition() / (double)currentFormat.getSampleRate() * 1000000.0);
		}

		/**
		 * @see javax.sound.sampled.DataLine#getLevel()
		 */
		public float getLevel() {
			// TODO !!!!!!
			return AudioSystem.NOT_SPECIFIED;
		}

		/**
		 * @see javax.sound.sampled.Line#getLineInfo()
		 */
		public Line.Info getLineInfo() {
			return new DataLine.Info(SourceDataLine.class, FORMAT);
		}

		/**
		 * @see javax.sound.sampled.Line#open()
		 */
		public void open() throws LineUnavailableException {
			if(isOpen()) return;
			createBuffers(DEFAULT_BUFFER_SIZE);
			currentFormat = DEFAULT_FORMAT;
			FXUnit.this.open();
			lineOpen = true;
			targetLine.opened();
			notifyListeners(LineEvent.Type.OPEN);
		}

		/**
		 * @see javax.sound.sampled.Line#close()
		 */
		public void close() {
			if(!isOpen()) return;
			lineOpen = false;
			currentFormat = DEFAULT_FORMAT;
			buffer.close();
			targetLine.closed();
			framePosition = 0;
			notifyListeners(LineEvent.Type.CLOSE);
		}

		/**
		 * @see javax.sound.sampled.Line#isOpen()
		 */
		public boolean isOpen() {
			return lineOpen && buffer != null;
		}

		/**
		 * @see javax.sound.sampled.Line#getControls()
		 */
		public Control[] getControls() {
			return FXUnit.this.getControls();
		}

		/**
		 * @see javax.sound.sampled.Line#isControlSupported(javax.sound.sampled.Control.Type)
		 */
		public boolean isControlSupported(Type type) {
			return FXUnit.this.isControlSupported(type);
		}

		/**
		 * @see javax.sound.sampled.Line#getControl(javax.sound.sampled.Control.Type)
		 */
		public Control getControl(Type type) {
			return FXUnit.this.getControl(type);
		}
	}
	/**
	 * A FXTargetDataLine is used for reading processed audio data out of the
	 * FXUnit 
	 * @author Manu Robledo
	 *
	 */
	class FXTargetDataLine implements TargetDataLine{
		/**
		 * List of LineListeneres registered with this Line
		 */
		private ArrayList lineListeners = new ArrayList();
		/**
		 * Current frame position
		 */
		int framePosition = 0;
		/**
		 * This method will be called when the Effect is opened
		 *
		 */
		private void opened(){
			framePosition = 0;
			notifyListeners(LineEvent.Type.OPEN);
		}
		/**
		 * This method will be called when the Effect is closed
		 *
		 */
		private void closed(){
			framePosition = 0;
			notifyListeners(LineEvent.Type.CLOSE);
		}
		/**
		 * 
		 * @see javax.sound.sampled.TargetDataLine#open(javax.sound.sampled.AudioFormat, int)
		 */
		public void open(AudioFormat format, int bufferSize) throws LineUnavailableException {
			if(isOpen()) return;
			if(!((DataLine.Info)getLineInfo()).isFormatSupported(format)) throw new LineUnavailableException("Format not supported.");
			createBuffers(bufferSize);
			currentFormat = format;
			FXUnit.this.open();
			lineOpen = true;
			sourceLine.opened();
			notifyListeners(LineEvent.Type.OPEN);
		}
		/**
		 * Notify all registered LineListeners about a LineEvent
		 * @param type The type of the LineEvent
		 */
		private void notifyListeners(LineEvent.Type type){
			LineEvent event = new LineEvent(this, type, getFramePosition());
			for (Iterator iter = lineListeners.iterator(); iter.hasNext();) {
				LineListener listener = (LineListener) iter.next();
				listener.update(event);
			}
		}
		/**
		 * 
		 * @see javax.sound.sampled.Line#addLineListener(javax.sound.sampled.LineListener)
		 */
		public void addLineListener(LineListener listener) {
			lineListeners.add(listener);
		}
		/**
		 * 
		 * @see javax.sound.sampled.Line#removeLineListener(javax.sound.sampled.LineListener)
		 */
		public void removeLineListener(LineListener listener) {
			lineListeners.remove(listener);
		}
		/**
		 * @see javax.sound.sampled.TargetDataLine#open(javax.sound.sampled.AudioFormat)
		 */
		public void open(AudioFormat format) throws LineUnavailableException {
			if(isOpen()) return;
			if(!((DataLine.Info)getLineInfo()).isFormatSupported(format)) throw new LineUnavailableException("Format not supported.");
			createBuffers(DEFAULT_BUFFER_SIZE);
			currentFormat = format;
			FXUnit.this.open();
			lineOpen = true;
			sourceLine.opened();
			notifyListeners(LineEvent.Type.OPEN);
		}

		/**
		 * @see javax.sound.sampled.TargetDataLine#read(byte[], int, int)
		 */
		public int read(byte[] b, int off, int len) {
			len = buffer.read(b, off, len);
			if(len == 0) return 0;
			
			FloatBuffer fb = ByteBuffer.wrap(b, off, len).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
			inBuffers[0].rewind();
			inBuffers[0].limit(len / 8);
			inBuffers[1].rewind();
			inBuffers[1].limit(len / 8);
			outBuffers[0].rewind();
			outBuffers[0].limit(len / 8);
			outBuffers[1].rewind();
			outBuffers[1].limit(len / 8);
			for(int i = 0; i < len; i += 8){
				inBuffers[0].put(fb.get());
				inBuffers[1].put(fb.get());
			}
			inBuffers[0].rewind();
			inBuffers[1].rewind();
			effect.process(inBuffers, outBuffers);
			outBuffers[0].rewind();
			outBuffers[1].rewind();
			fb.rewind();
			for(int i = 0; i < len; i += 8){
				fb.put(outBuffers[0].get());
				fb.put(outBuffers[1].get());
			}
			framePosition += len / 8;
			return len;
		}

		/**
		 * @see javax.sound.sampled.DataLine#drain()
		 */
		public void drain() {
		}

		/**
		 * @see javax.sound.sampled.DataLine#flush()
		 */
		public void flush() {
			if(isOpen()) buffer.flush();
		}

		/**
		 * @see javax.sound.sampled.DataLine#start()
		 */
		public void start() {
			if(!isOpen()) return;
			buffer.open();
		}

		/**
		 * @see javax.sound.sampled.DataLine#stop()
		 */
		public void stop() {
			if(!isActive()) return;
			buffer.close();
		}

		/**
		 * @see javax.sound.sampled.DataLine#isRunning()
		 */
		public boolean isRunning() {
			return isActive();
		}

		/**
		 * @see javax.sound.sampled.DataLine#isActive()
		 */
		public boolean isActive() {
			return isOpen() && buffer.isOpen();
		}

		/**
		 * @see javax.sound.sampled.DataLine#getFormat()
		 */
		public AudioFormat getFormat() {
			return currentFormat;
		}

		/**
		 * @see javax.sound.sampled.DataLine#getBufferSize()
		 */
		public int getBufferSize() {
			if(!isOpen()) return AudioSystem.NOT_SPECIFIED;
			else return buffer.size(); 
		}

		/**
		 * @see javax.sound.sampled.DataLine#available()
		 */
		public int available() {
			if(!isOpen()) return 0;
			else return buffer.readAvailable(); 
		}

		/**
		 * @see javax.sound.sampled.DataLine#getFramePosition()
		 */
		public int getFramePosition() {
			if(!isOpen()) return 0;
			return framePosition;
		}

		/**
		 * @see javax.sound.sampled.DataLine#getMicrosecondPosition()
		 */
		public long getMicrosecondPosition() {
			return (long)Math.round(getFramePosition() / (double)currentFormat.getSampleRate() * 1000000.0);
		}

		/**
		 * @see javax.sound.sampled.DataLine#getLevel()
		 */
		public float getLevel() {
			// TODO !!!!!
			return AudioSystem.NOT_SPECIFIED;
		}

		/**
		 * @see javax.sound.sampled.Line#getLineInfo()
		 */
		public Line.Info getLineInfo() {
			return new DataLine.Info(TargetDataLine.class, FORMAT);
		}

		/**
		 * @see javax.sound.sampled.Line#open()
		 */
		public void open() throws LineUnavailableException {
			if(isOpen()) return;
			createBuffers(DEFAULT_BUFFER_SIZE);
			currentFormat = DEFAULT_FORMAT;
			FXUnit.this.open();
			lineOpen = true;
			sourceLine.opened();
			notifyListeners(LineEvent.Type.OPEN);
		}

		/**
		 * @see javax.sound.sampled.Line#close()
		 */
		public void close() {
			if(!isOpen()) return;
			lineOpen = false;
			currentFormat = DEFAULT_FORMAT;
			buffer.close();
			sourceLine.closed();
			framePosition = 0;
			notifyListeners(LineEvent.Type.CLOSE);
		}

		/**
		 * @see javax.sound.sampled.Line#isOpen()
		 */
		public boolean isOpen() {
			return lineOpen && buffer != null;
		}

		/**
		 * @see javax.sound.sampled.Line#getControls()
		 */
		public Control[] getControls() {
			return FXUnit.this.getControls();
		}

		/**
		 * @see javax.sound.sampled.Line#isControlSupported(javax.sound.sampled.Control.Type)
		 */
		public boolean isControlSupported(Type type) {
			return FXUnit.this.isControlSupported(type);
		}

		/**
		 * @see javax.sound.sampled.Line#getControl(javax.sound.sampled.Control.Type)
		 */
		public Control getControl(Type type) {
			return FXUnit.this.getControl(type);
		}
	}
	/**
	 * A FXAnalysisLine is used for streaming audio data for analysis into the
	 * FXUnit 
	 * @author Manu Robledo
	 *
	 */
	class FXAnalysisLine implements SourceDataLine{
		/**
		 * List of LineListeners registered with this Line
		 */
		private ArrayList lineListeners = new ArrayList();
		/**
		 * The current AudioFormat used for analyzing
		 */
		private AudioFormat currentFormat = DEFAULT_FORMAT;
		/**
		 * The current frame position
		 */
		private int framePosition = 0;
		/**
		 * 
		 * @see javax.sound.sampled.SourceDataLine#open(javax.sound.sampled.AudioFormat, int)
		 */
		public void open(AudioFormat format, int bufferSize) throws LineUnavailableException {
			if(isOpen()) return;
			if(!((DataLine.Info)getLineInfo()).isFormatSupported(format)) throw new LineUnavailableException("Format not supported.");
			FXUnit.this.open();
			createBuffers(bufferSize);
			currentFormat = format;
			effect.startAnalysis(currentFormat.getSampleRate());
			notifyListeners(LineEvent.Type.OPEN);
		}
		/**
		 * Notify all registered LineListeners about a LineEvent
		 * @param type The type of the LineEvent
		 */
		private void notifyListeners(LineEvent.Type type){
			LineEvent event = new LineEvent(this, type, getFramePosition());
			for (Iterator iter = lineListeners.iterator(); iter.hasNext();) {
				LineListener listener = (LineListener) iter.next();
				listener.update(event);
			}
		}
		/**
		 * 
		 * @see javax.sound.sampled.Line#addLineListener(javax.sound.sampled.LineListener)
		 */
		public void addLineListener(LineListener listener) {
			lineListeners.add(listener);
		}
		/**
		 * 
		 * @see javax.sound.sampled.Line#removeLineListener(javax.sound.sampled.LineListener)
		 */
		public void removeLineListener(LineListener listener) {
			lineListeners.remove(listener);
		}

		/**
		 * @see javax.sound.sampled.TargetDataLine#open(javax.sound.sampled.AudioFormat)
		 */
		public void open(AudioFormat format) throws LineUnavailableException {
			if(isOpen()) return;
			if(!((DataLine.Info)getLineInfo()).isFormatSupported(format)) throw new LineUnavailableException("Format not supported.");
			FXUnit.this.open();
			createBuffers(DEFAULT_BUFFER_SIZE);
			currentFormat = format;
			effect.startAnalysis(currentFormat.getSampleRate());
			notifyListeners(LineEvent.Type.OPEN);
		}
		/**
		 * @see javax.sound.sampled.SourceDataLine#write(byte[], int, int)
		 */
		public int write(byte[] b, int off, int len) {
			len -= len % 8;
			if(len == 0) return 0;
			if(!isOpen()) return 0;
			FloatBuffer fb = ByteBuffer.wrap(b, off, len).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
			
			inBuffers[0].rewind();
			inBuffers[0].limit(len / 8);
			inBuffers[1].rewind();
			inBuffers[1].limit(len / 8);
			for(int i = 0; i < len; i+= 8){
				inBuffers[0].put(fb.get());
				inBuffers[1].put(fb.get());
			}
			inBuffers[0].rewind();
			inBuffers[1].rewind();
			
			effect.analyze(inBuffers);
			return len;
		}

		/**
		 * @see javax.sound.sampled.DataLine#drain()
		 */
		public void drain() {
		}

		/**
		 * @see javax.sound.sampled.DataLine#flush()
		 */
		public void flush() {
		}

		/**
		 * @see javax.sound.sampled.DataLine#start()
		 */
		public void start() {
		}

		/**
		 * @see javax.sound.sampled.DataLine#stop()
		 */
		public void stop() {
		}

		/**
		 * @see javax.sound.sampled.DataLine#isRunning()
		 */
		public boolean isRunning() {
			return isOpen();
		}

		/**
		 * @see javax.sound.sampled.DataLine#isActive()
		 */
		public boolean isActive() {
			return isRunning();
		}

		/**
		 * @see javax.sound.sampled.DataLine#getFormat()
		 */
		public AudioFormat getFormat() {
			return currentFormat;
		}

		/**
		 * @see javax.sound.sampled.DataLine#getBufferSize()
		 */
		public int getBufferSize() {
			if(!isOpen()) return AudioSystem.NOT_SPECIFIED;
			return buffer.size();
		}

		/**
		 * @see javax.sound.sampled.DataLine#available()
		 */
		public int available() {
			if(!isOpen()) return 0;
			return getBufferSize();
		}

		/**
		 * @see javax.sound.sampled.DataLine#getFramePosition()
		 */
		public int getFramePosition() {
			return framePosition;
		}

		/**
		 * @see javax.sound.sampled.DataLine#getMicrosecondPosition()
		 */
		public long getMicrosecondPosition() {
			return (long)Math.round(getFramePosition() / (double)currentFormat.getSampleRate() * 1000000.0);
		}

		/**
		 * @see javax.sound.sampled.DataLine#getLevel()
		 */
		public float getLevel() {
			// TODO !!!!!!
			return AudioSystem.NOT_SPECIFIED;
		}

		/**
		 * @see javax.sound.sampled.Line#getLineInfo()
		 */
		public Line.Info getLineInfo() {
			return new DataLine.Info(FXAnalysisLine.class, FORMAT);
		}

		/**
		 * @see javax.sound.sampled.Line#open()
		 */
		public void open() throws LineUnavailableException {
			if(isOpen()) return;
			FXUnit.this.open();
			createBuffers(DEFAULT_BUFFER_SIZE);
			currentFormat = DEFAULT_FORMAT;
			effect.startAnalysis(currentFormat.getSampleRate());
			notifyListeners(LineEvent.Type.OPEN);
		}

		/**
		 * @see javax.sound.sampled.Line#close()
		 */
		public void close() {
			if(!isOpen()) return;
			framePosition = 0;
			effect.stopAnalysis();
			currentFormat = DEFAULT_FORMAT;
		}

		/**
		 * @see javax.sound.sampled.Line#isOpen()
		 */
		public boolean isOpen() {
			return effect.isAnalyzing();
		}

		/**
		 * @see javax.sound.sampled.Line#getControls()
		 */
		public Control[] getControls() {
			return new Control[0];
		}

		/**
		 * @see javax.sound.sampled.Line#isControlSupported(javax.sound.sampled.Control.Type)
		 */
		public boolean isControlSupported(Type type) {
			return false;
		}

		/**
		 * @see javax.sound.sampled.Line#getControl(javax.sound.sampled.Control.Type)
		 */
		public Control getControl(Type type) {
			return null;
		}
	}
	/**
	 * Subclass of Mixer.Info to be able to create a new instance 
	 * @author Manu Robledo
	 *
	 */
	class Info extends Mixer.Info{
		/**
		 * Construct a new Info object
		 * @param name The Name of the Mixer
		 * @param vendor The Mixer큦 vendor
		 * @param description Description of the Mixer
		 * @param version The Mixer큦 version
		 */
		protected Info(String name, String vendor, String description, String version) {
			super(name, vendor, description, version);
		}
	}
}
