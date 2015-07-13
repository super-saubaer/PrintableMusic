package com.groovemanager.sampled;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import com.groovemanager.core.ConfigManager;
import com.groovemanager.core.Log;
import com.groovemanager.exception.NotReadyException;

/**
 * This class can be used for playing or recording Audio data in a new Thread.
 * @author Manu Robledo
 *
 */
public class AudioPlayer implements Runnable, IPropertyChangeListener {
	/**
	 * The List of registered AudioPlayerListeners
	 */
	protected ArrayList listeners = new ArrayList();
	/**
	 * Break condition for the play/record Thread
	 */
	protected boolean cont;
	/**
	 * Tells if the stop method has been invoked
	 */
	protected boolean stopped;
	/**
	 * The current player status
	 */
	protected int status = NOT_READY;
	/**
	 * Thread for playback and recording
	 */
	private Thread playAndRecordThread;
	/**
	 * Possible values for status changes
	 */
	public static final int STOPPED = 0, STARTED = 1, PAUSED = 2,
			CONTINUED = 3, PROVIDER_SET = 4, PROVIDER_REMOVED = 5;
	/**
	 * Possible values for player status
	 */
	public final static int PLAYING = 100, PAUSE_PLAY = 101, READY_FOR_PLAY = 102, READY_FOR_REC = 103, NOT_READY = 105, RECORDING = 106, PAUSE_REC = 107;
	/**
	 * The byte[] buffer used for reading and writing audio data
	 */
	protected byte[] readBuffer;
	/**
	 * The AudioPlayerProvider which provides thsi player with audio data
	 * and receives recorded audio data if capable for recording
	 */
	protected AudioPlayerProvider provider;
	/**
	 * The stream from which data is read
	 */
	protected AudioInputStream in;
	/**
	 * Flag indicating if the Thread has already been requested to stop.
	 * Is needed to avoid endless loops in the stop() method. 
	 */
	protected boolean endReached = false;
	/**
	 * Flag indicating if the playback should be looped or not
	 */
	protected boolean loop;
	/**
	 * Audio output line
	 */
	protected SourceDataLine sourceLine;
	/**
	 * Audio input line
	 */
	protected TargetDataLine targetLine;
	/**
	 * The number of frames currently read from the last provided
	 * AudioInputStream. Needed for getFramePosition()
	 */
	protected int actualLength;
	/**
	 * The start position of the last provided AudioInputStream.
	 * Needed for getFramePosition()
	 */
	protected int lastStart = 0;
	/**
	 * List containing an OneLoop instances for each complete read
	 * AudioInputStream during this playback phase. Needed in loop
	 * situations for getFramePosition()
	 */
	protected ArrayList aisLengths = new ArrayList();
	/**
	 * The buffer size to be used when opening lines.
	 * Is retreived from the ConfigManager´s "audio_play_buffer"
	 * property.
	 */
	protected int bufferSize;
	/**
	 * The AudioFormat in which the Audio data must be written
	 * to the line (in play situations) or to the provider (in
	 * rec situations)
	 */
	protected AudioFormat targetFormat;
	/**
	 * The thread priority to be assigned to the play/rec threads
	 */
	protected int priority = Thread.NORM_PRIORITY + 2;
	/**
	 * The ConfigManager. Currently only used for the bufferSize
	 */
	protected ConfigManager configManager;
	/**
	 * The AudioManager. used for getting the selected in and out
	 * Mixers
	 */
	protected AudioManager audioManager;
	
	/**
	 * Constructs a new AudioPlayer using the given ConfigManager
	 * and AudioManager
	 * @param configManager The ConfigManager to be used
	 * @param audioManager The AudioManager to be used
	 */
	public AudioPlayer(ConfigManager configManager, AudioManager audioManager) {
		this.configManager = configManager;
		this.audioManager = audioManager;
		configManager.getPrefStore().addPropertyChangeListener(this);
		bufferSize = configManager.getPrefStore().getInt("audio_play_buffer");
		priority = configManager.getPrefStore().getInt("audio_player_priority");
	}
	/**
	 * Constructs a new AudioPlayer using the given ConfigManager
	 * and a new AudioManager using this ConfigManager too
	 * @param configManager The ConfigManager to be used
	 */
	public AudioPlayer(ConfigManager configManager){
		this(configManager, new AudioManager(configManager));
	}
	/**
	 * Constructs a new AudioPlayer using the default ConfigManager
	 * and the default Audiomanager
	 */
	public AudioPlayer(){
		this(ConfigManager.getDefault(), AudioManager.getDefault());
	}

	/**
	 * Register an AudioPlayerListener
	 * @param listener The AudioPlayerListener
	 */
	public void addAudioPlayerListener(AudioPlayerListener listener) {
		listeners.add(listener);
	}
	/**
	 * Remove a registered AudioPlayerListener
	 * @param listener The AudioPlayerListener
	 */
	public void removeAudioPlayerListener(AudioPlayerListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Ask for the real buffer size used by the line
	 * @return The buffersize in bytes
	 */
	public int getRealBufferSize(){
		int size = 0;
		switch (status) {
			case PLAYING :
			case PAUSE_PLAY :
				size = sourceLine.getBufferSize();
				break;
			case RECORDING :
			case PAUSE_REC :
				size = targetLine.getBufferSize();
				break;
			default :
				return 0;
		}
		if(size == AudioSystem.NOT_SPECIFIED) return 0;
		else return size;
	}
	/**
	 * Notify all registered listeners about a status change
	 * @param type The type of status change
	 */
	private void notifyListeners(int type) {
		// Inform listeners about a status change
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			((AudioPlayerListener) iter.next()).statusChanged(type);
		}
	}
	
	/**
	 * Enable or disable looping
	 * @param loop true if looping should be turned on, false otherwise
	 */
	public void setLoop(boolean loop){
		if(loop == this.loop || status == RECORDING || status == PAUSE_REC) return; 
		this.loop = loop;
		notifyListenersLoop(loop);
	}
	/**
	 * Switch the current loop state  
	 */
	public void switchLoop() {
		setLoop(!loop);
	}
	/**
	 * Notify all registered listeners about a loop change
	 * @param loop The new loop state
	 */
	private void notifyListenersLoop(boolean loop) {
		// Inform listeners about a change of the loop setting
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			((AudioPlayerListener) iter.next()).loopChanged(loop);
		}
	}
	
	/**
	 * Get a String description of the given player status
	 * @param status The player status
	 * @return The textual description of the given status
	 */
	public static String getStatusName(int status){
		switch (status) {
			case NOT_READY :
				return "Not ready (No input is set).";
			case READY_FOR_PLAY :
				return "Ready for playback.";
			case READY_FOR_REC :
				return "Ready for recording.";
			case PLAYING :
				return "Playing back.";
			case PAUSE_PLAY :
				return "Playback paused";
			case RECORDING :
				return "Recording.";
			case PAUSE_REC :
				return "Recording paused.";
			default :
				return "Unknown status.";
		}
	}
	
	/**
	 * Start playback
	 * @throws NotReadyException if the Player or the provider are not
	 * ready for playback or if playback is already active
	 * @throws LineUnavailableException if the needed ressources are not
	 * available
	 */
	public synchronized void play() throws NotReadyException, LineUnavailableException{
		if(status != READY_FOR_PLAY && status != READY_FOR_REC) throw new NotReadyException("The current player status doesn't allow playback. Current status: " + getStatusName(status));
		
		cont = true;
		stopped = false;
		endReached = false;
		
		if(provider == null){
			// This shouldn't happen...
			status = NOT_READY;
			notifyListeners(PROVIDER_REMOVED);
			throw new NotReadyException("Input not set.");
		}
		
		// Get the first AudioInputStream and prepare for correct
		// working of getFramePosition()
		synchronized(provider){
			in = provider.getAudioInputStream();
			lastStart = provider.getLastStart();
			aisLengths.clear();
			actualLength = 0;
		}
		
		// Get the sourceLine and open it
		try {
			AudioFormat af = in.getFormat();
			sourceLine = (SourceDataLine)createLine(af, false);
			targetFormat = getUsableFormat(sourceLine, af, false);
			if(targetFormat == null){
				throw new NotReadyException("Can't get an output line for the format: " + af);
			}
			sourceLine.open(targetFormat, bufferSize * targetFormat.getFrameSize());
			// Integrate conversion
			if(!targetFormat.matches(af)){
				if(!AudioSystem.isConversionSupported(targetFormat, af)){
					throw new LineUnavailableException("Format Conversion not supported from " + af + " to " + targetFormat + ".");
				}
				in = AudioSystem.getAudioInputStream(targetFormat, in);
			}
		} catch (LineUnavailableException e2) {
			cont = false;
			stopped = true;
			endReached = true;
			freeRessources();
			throw e2;
		}
		
		// Adapt the buffer size to the one used by the line
		if(readBuffer == null || readBuffer.length != sourceLine.getBufferSize()) readBuffer = new byte[sourceLine.getBufferSize()];
		
		// change the status
		status = PLAYING;
		notifyListeners(STARTED);
		
		// Create and start the thread
		playAndRecordThread = new Thread(this);
		playAndRecordThread.setName("AudioPlayer");
		playAndRecordThread.setPriority(priority);
		playAndRecordThread.start();
	}
	
	/**
	 * Start recording
	 * @throws NotReadyException if the recorder or the provider is not ready
	 * @throws LineUnavailableException if the needed ressources are not
	 * available
	 */
	public synchronized void rec() throws NotReadyException, LineUnavailableException{
		if(status != READY_FOR_PLAY && status != READY_FOR_REC) throw new NotReadyException("The current player status doesn't allow recording. Current status: " + getStatusName(status));

		// Looping must be turned off for recording
		setLoop(false);
		
		cont = true;
		stopped = false;
		endReached = false;
		
		if(provider == null){
			// This shouldn't happen...
			status = NOT_READY;
			notifyListeners(PROVIDER_REMOVED);
			throw new NotReadyException("Input not set.");
		}
		
		// Get the target Line and open it
		try {
			targetFormat = provider.startRec();
			targetLine = (TargetDataLine)createLine(targetFormat, true);
			AudioFormat realFormat = getUsableFormat(targetLine, targetFormat, true);
			if(realFormat == null){
				throw new NotReadyException("Can't get an input line for the format: " + targetFormat);
			}
			targetLine.open(realFormat, bufferSize * realFormat.getFrameSize());
			in = new AudioInputStream(targetLine);
			// Integrate conversion
			if(!targetFormat.matches(realFormat)){
				if(!AudioSystem.isConversionSupported(targetFormat, realFormat)){
					throw new LineUnavailableException("Format Conversion not supported from " + realFormat + " to " + targetFormat + ".");
				}
				in = AudioSystem.getAudioInputStream(targetFormat, in);
			}
		} catch (LineUnavailableException e2) {
			cont = false;
			stopped = true;
			endReached = true;
			freeRessources();
			throw e2;
		}
		
		// Adapt the buffer size to the one used by the line
		if(readBuffer == null || readBuffer.length != targetLine.getBufferSize()) readBuffer = new byte[targetLine.getBufferSize()];
		
		// change the status
		status = RECORDING;
		notifyListeners(STARTED);
		
		// Create and start the thread
		playAndRecordThread = new Thread(this);
		playAndRecordThread.setName("AudioPlayer");
		playAndRecordThread.setPriority(priority);
		playAndRecordThread.start();
	}
	
	/**
	 * Stop recording or playback  
	 */
	public void stop() {
		if (status != PLAYING && status != PAUSE_PLAY && status != RECORDING && status != PAUSE_REC) return;

		// Should the provider be notified of the end of recording?
		boolean stopRec = false;
		if((status == PAUSE_REC || status == RECORDING) && provider != null) stopRec = true;
		
		// Change the status and set the break conditions
		if(provider == null) status = NOT_READY;
		else if(provider.canRec()) status = READY_FOR_REC;
		else if(provider.canProvide()) status = READY_FOR_PLAY;
		else status = NOT_READY;
		
		stopped = true;
		
		end();
		
		// Wait for the thread to come to its end.
		while(playAndRecordThread != null && playAndRecordThread.isAlive() && !endReached){
			try {
				Thread.sleep(0, 1);
			} catch (InterruptedException e) {
			}
		}
			
		if(sourceLine != null) sourceLine.stop();
		if(targetLine != null) targetLine.stop();
		
		// If needed: Tell the provider that recording has stopped
		if(stopRec) provider.stopRec();
		
		// Always free all ressources after stop
		freeRessources();

		// Notify the listeners of the stop.
		notifyListeners(STOPPED);
	}
	
	/**
	 * Tell the Thread that it should come to its end.
	 */
	protected void end() {
		cont = false;
	}

	/**
	 * Get the current status
	 * @return The current player status
	 */
	public int getStatus() {
		return status;
	}
	
	/**
	 * Pause playback or recording, but stay prepared for immediate
	 * continuing 
	 */
	public void pause() {
		if(status == PLAYING){
			status = PAUSE_PLAY;
			sourceLine.stop();
		}
		else if(status == RECORDING){
			status = PAUSE_REC;
			targetLine.stop();
		}
		else return;

		notifyListeners(PAUSED);
	}
	/**
	 * Continue playback or recording after pause  
	 */
	public void cont() throws NotReadyException{
		if(status == PAUSE_REC){
			status = RECORDING;
			playAndRecordThread.interrupt();
		}
		else if(status == PAUSE_PLAY){
			status = PLAYING;
			playAndRecordThread.interrupt();
		}
		else throw new NotReadyException();

		notifyListeners(CONTINUED);
	}
	
	/**
	 * Get the current position inside audio source considering
	 * passed loops and the startPosition provided by the
	 * AudioPlayerProvider
	 * @return the current position
	 */
	public int getFramePosition() {
		if(status == NOT_READY) return 0;
		
		// Get the position for playback
		if(status == PLAYING || status == PAUSE_PLAY || status == READY_FOR_PLAY){
			if(sourceLine == null) return lastStart;
			int linePos = sourceLine.getFramePosition();
			int i = 0;
			OneLoop ol = null;
			synchronized(aisLengths){
				for (Iterator iter = aisLengths.iterator(); iter.hasNext();) {
					ol = (OneLoop) iter.next();
					if(i + ol.length >= linePos) return ol.start + linePos - i;
					i += ol.length;
				}
			}
			if(ol == null) return lastStart + linePos - i;
			else return ol.start + linePos - i;
		}
		
		// Get the position for recording
		else if(status == RECORDING || status == PAUSE_REC || status == READY_FOR_REC){
			if(targetLine == null) return lastStart;
			return targetLine.getFramePosition();
		}
		
		// This shouldn't happen
		return 0;
	}
	
	/**
	 * Get the line from the currently selected Mixer that best
	 * supports the specified AudioFormat
	 * @param format The wished AudioFormat. Will also return a
	 * Line if the Format doesn't match.
	 * @param isTarget true if an Input Line (TargetdataLine) is
	 * wanted, false for an output Line (SourceDataLine)
	 * @return The best matching DataLine instance
	 * @throws LineUnavailableException if the requestes ressources
	 * are not available
	 */
	protected DataLine createLine(AudioFormat format, boolean isTarget) throws LineUnavailableException{
		int maxMatch = -1;
		Mixer m;
		Line.Info[] lineInfos;
		Class c;
		Line.Info lineInfo = null;
		if(isTarget){
			m = audioManager.getInMixer();
			lineInfos = m.getTargetLineInfo();
			c = TargetDataLine.class;
		}
		else{
			m = audioManager.getOutMixer();
			lineInfos = m.getSourceLineInfo();
			c = SourceDataLine.class;
		}
		
		Line.Info def = null;
		int rel;
		for (int i = 0; i < lineInfos.length; i++) {
			if(c.isAssignableFrom(lineInfos[i].getLineClass())){
				if(def == null) def = lineInfos[i];
				AudioFormat[] supported = ((DataLine.Info)lineInfos[i]).getFormats();
				for (int j = 0; j < supported.length; j++) {
					if(isTarget) rel = AudioManager.getRelation(supported[j], format);
					else rel = AudioManager.getRelation(format, supported[j]);
					if(rel > maxMatch){
						maxMatch = rel;
						lineInfo = lineInfos[i];
					}
				}
			}
		}
		if(lineInfo != null) return (DataLine)m.getLine(lineInfo); 
		else if(def != null) return (DataLine)m.getLine(def);
		else throw new LineUnavailableException("Could get no DataLine from the selected Mixer.");
	}
	/**
	 * Tries to get an AudioFormat which is supported by the line and is
	 * as near as possible to the specified sourceFormat
	 * @param line The Line which has to support the Format
	 * @param sourceFormat A hint how the resulting Format should
	 * be like if possible
	 * @param isTarget Specifies if we are talking about a Target
	 * line or not. This is important to find the best matching format.
	 * @return The best matching AudioFormat supported by the Line
	 * or null if the Line doesn't support any AudioFormat (which is
	 * unlikely to happen, but you never know...)
	 */
	protected static AudioFormat getUsableFormat(DataLine line, AudioFormat sourceFormat, boolean isTarget){
		DataLine.Info info = (DataLine.Info)line.getLineInfo();
		if(info.isFormatSupported(sourceFormat)) return sourceFormat;

		int maxMatch = -1;
		AudioFormat bestFormat = null;
		AudioFormat[] formats = info.getFormats();
		int rel;
		for (int i = 0; i < formats.length; i++) {
			if(isTarget){
				rel = AudioManager.getRelation(formats[i], sourceFormat);
			}
			else rel = AudioManager.getRelation(sourceFormat, formats[i]);
			if(rel > maxMatch){
				maxMatch = rel;
				bestFormat = formats[i];
			}
		}
		
		if(maxMatch == -1) return null;
		else if(maxMatch == 0){
			AudioFormat bigFormat = null, littleFormat = null;
			for (int i = 0; i < formats.length; i++) {
				if(formats[i].getSampleSizeInBits() >= 16 && formats[i].getChannels() == sourceFormat.getChannels()){
					if(formats[i].isBigEndian()) bigFormat = new AudioFormat(sourceFormat.getSampleRate(), formats[i].getSampleSizeInBits(), sourceFormat.getChannels(), true, formats[i].isBigEndian());
					else littleFormat = new AudioFormat(sourceFormat.getSampleRate(), formats[i].getSampleSizeInBits(), sourceFormat.getChannels(), true, formats[i].isBigEndian());
				}
			}
			if((sourceFormat.isBigEndian() || littleFormat == null) && bigFormat != null) bestFormat = bigFormat;
			else bestFormat = littleFormat;
			return bestFormat;
		}
		if(bestFormat != null && !isFormatComplete(bestFormat)){
			//TODO besser lösen
			bestFormat = new AudioFormat(bestFormat.getEncoding(), sourceFormat.getSampleRate(), bestFormat.getSampleSizeInBits(), sourceFormat.getChannels(), bestFormat.getFrameSize(), sourceFormat.getFrameRate(), bestFormat.isBigEndian());
		}
		if(!isFormatComplete(bestFormat)){
			if(bestFormat.getSampleRate() == AudioSystem.NOT_SPECIFIED){
				if(sourceFormat.getSampleRate() != AudioSystem.NOT_SPECIFIED) bestFormat = new AudioFormat(bestFormat.getEncoding(), sourceFormat.getSampleRate(), bestFormat.getSampleSizeInBits(), bestFormat.getChannels(), bestFormat.getFrameSize(), sourceFormat.getSampleRate(), bestFormat.isBigEndian());
				else if(sourceFormat.getFrameRate() != AudioSystem.NOT_SPECIFIED) bestFormat = new AudioFormat(bestFormat.getEncoding(), sourceFormat.getFrameRate(), bestFormat.getSampleSizeInBits(), bestFormat.getChannels(), bestFormat.getFrameSize(), sourceFormat.getFrameRate(), bestFormat.isBigEndian());
				else if(bestFormat.getFrameRate() != AudioSystem.NOT_SPECIFIED) bestFormat = new AudioFormat(bestFormat.getEncoding(), bestFormat.getFrameRate(), bestFormat.getSampleSizeInBits(), bestFormat.getChannels(), bestFormat.getFrameSize(), bestFormat.getFrameRate(), bestFormat.isBigEndian());
				else bestFormat = new AudioFormat(bestFormat.getEncoding(), 44100, bestFormat.getSampleSizeInBits(), bestFormat.getChannels(), bestFormat.getFrameSize(), 44100, bestFormat.isBigEndian());
			}
			if(bestFormat.getSampleSizeInBits() == AudioSystem.NOT_SPECIFIED){
				if(sourceFormat.getSampleSizeInBits() != AudioSystem.NOT_SPECIFIED) bestFormat = new AudioFormat(bestFormat.getEncoding(), bestFormat.getSampleRate(), sourceFormat.getSampleSizeInBits(), bestFormat.getChannels(), sourceFormat.getSampleSizeInBits() / 8 * bestFormat.getChannels(), bestFormat.getFrameRate(), bestFormat.isBigEndian());
				else bestFormat = new AudioFormat(bestFormat.getEncoding(), bestFormat.getSampleRate(), 16, bestFormat.getChannels(), 2 * bestFormat.getChannels(), bestFormat.getFrameRate(), bestFormat.isBigEndian());
			}
			if(bestFormat.getChannels() == AudioSystem.NOT_SPECIFIED){
				if(sourceFormat.getChannels() != AudioSystem.NOT_SPECIFIED) bestFormat = new AudioFormat(bestFormat.getEncoding(), bestFormat.getSampleRate(), bestFormat.getSampleSizeInBits(), sourceFormat.getChannels(), sourceFormat.getChannels() * bestFormat.getSampleSizeInBits() / 8, bestFormat.getFrameRate(), bestFormat.isBigEndian());
				else bestFormat = new AudioFormat(bestFormat.getEncoding(), bestFormat.getSampleRate(), bestFormat.getSampleSizeInBits(), 2, 2 * bestFormat.getSampleSizeInBits() / 8, bestFormat.getFrameRate(), bestFormat.isBigEndian());
			}
			if(bestFormat.getFrameSize() == AudioSystem.NOT_SPECIFIED){
				bestFormat = new AudioFormat(bestFormat.getEncoding(), bestFormat.getSampleRate(), bestFormat.getSampleSizeInBits(), bestFormat.getChannels(), bestFormat.getChannels() * bestFormat.getSampleSizeInBits() / 8, bestFormat.getFrameRate(), bestFormat.isBigEndian());
			}
			if(bestFormat.getFrameRate() == AudioSystem.NOT_SPECIFIED){
				bestFormat = new AudioFormat(bestFormat.getEncoding(), bestFormat.getSampleRate(), bestFormat.getSampleSizeInBits(), bestFormat.getChannels(), bestFormat.getFrameSize(), bestFormat.getSampleRate(), bestFormat.isBigEndian());
			}
		}
		return bestFormat;
	}
	/**
	 * Checks if the given AudioFormat is fully specified and doesn't
	 * contain any AudioSystem.NOT_SPECIFIED values
	 * @param format The Format to be checked
	 * @return true if the Format is fully specified, false otherwise
	 */
	protected static boolean isFormatComplete(AudioFormat format){
		if(format.getChannels() == AudioSystem.NOT_SPECIFIED) return false;
		if(format.getFrameRate() == AudioSystem.NOT_SPECIFIED) return false;
		if(format.getFrameSize() == AudioSystem.NOT_SPECIFIED) return false;
		if(format.getSampleRate() == AudioSystem.NOT_SPECIFIED) return false;
		if(format.getSampleSizeInBits() == AudioSystem.NOT_SPECIFIED) return false;
		return true;
	}
	/**
	 * Gets the fill level of the line´s buffer
	 * @return the number of unprocessed bytes contained
	 * in the line´s buffer
	 */
	public int getBufferFillLevel() {
		int level = 0;
		switch (status) {
			case PLAYING :
			case PAUSE_PLAY :
				level = sourceLine.getBufferSize() - sourceLine.available();
				break;
			case RECORDING :
			case PAUSE_REC :
				level = targetLine.getBufferSize() - targetLine.available();
				break;
			default :
				return 0;
		}
		if(level == AudioSystem.NOT_SPECIFIED) return 0;
		else return level;
	}

	/**
	 * Free all ressources
	 */
	protected synchronized void freeRessources() {
		if (sourceLine != null){
			sourceLine.close();
			sourceLine = null;
		}
		if (targetLine != null){
			targetLine.close();
			targetLine = null;
		}
		in = null;
		playAndRecordThread = null;
	}
	/**
	 * Set the provider, which provides the data for playing and -
	 * if supported - processes recorded data
	 * @param p The AudioPlayerProvider
	 */
	public void setProvider(AudioPlayerProvider p){
		if(p == null) return;
		if(provider != null) removeProvider();
		provider = p;

		if(provider.canRec()) status = READY_FOR_REC;
		else status = READY_FOR_PLAY;

		if(!provider.canLoop()) loop = false;
		notifyListeners(PROVIDER_SET);
	}
	/**
	 * Remove the AudioPlayerProvider
	 */
	public void removeProvider(){
		stop();
		provider = null;
		status = NOT_READY;
		notifyListeners(PROVIDER_REMOVED);
	}
	/**
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		// Go for it!
		int numBytesRead = 0, numBytesWritten = 0;
		int length;
		boolean rec = status == RECORDING || status == PAUSE_REC;
		DataLine line;
		if(rec) line = targetLine;
		else line = sourceLine;
		int frameSize = in.getFormat().getFrameSize();
		
		while (cont) {
			try {
				while(status == PAUSE_REC || status == PAUSE_PLAY){
					try {
						Thread.sleep(0, 1);
					} catch (InterruptedException e1) {
					}
				}
				length = readBuffer.length;
				
				// If the line is stopped, don't write more data
				// into the line than the buffer could handle to
				// avoid blocking
				if(!line.isActive()) length = Math.min(line.available(), length);
				
				// Read and write
				if(status == RECORDING) targetLine.start();
				if(numBytesWritten == numBytesRead){
					numBytesRead = in.read(readBuffer, 0, length);
					numBytesWritten = 0;
					synchronized(aisLengths){
						actualLength += numBytesRead / frameSize;
					}
				}
				if(numBytesRead > -1){
					if(rec) numBytesWritten += provider.rec(readBuffer, numBytesWritten, numBytesRead - numBytesWritten);
					else numBytesWritten += sourceLine.write(readBuffer, numBytesWritten, numBytesRead - numBytesWritten);
					if(status == PLAYING){
						sourceLine.start();
					}
				}
				else{
					if (Log.active)
						Log.log("Suche nach einem neuen AudioInputStream...", Log.TYPE_DEBUG);
					synchronized(aisLengths){
						aisLengths.add(new OneLoop(lastStart, actualLength));
						actualLength = 0;
					}
					if(loop && !rec){
						try{
							if(provider != null) synchronized(provider){
								in = provider.getAudioInputStream();
								lastStart = provider.getLastStart();
								if(!AudioSystem.isConversionSupported(targetFormat, in.getFormat())) cont = false;
								else in = AudioSystem.getAudioInputStream(targetFormat, in);
								numBytesRead = numBytesWritten = 0;
							}
							else cont = false;
						}
						catch(NotReadyException e){
							cont = false;
							e.printStackTrace();
						}
						if (Log.active)
							Log.log("...gefunden.", Log.TYPE_DEBUG);
					}
					else{
						cont = false;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				cont = false;
			}
		}
		
		// If the end was reached without invocation of the stop()
		// method, we want the line to keep playing until its
		// buffer is emptied
		if(status == PLAYING && !stopped) sourceLine.drain();
		endReached = true;
		stop();
	}
	/**
	 * React to changes of the buffer size 
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals("audio_play_buffer"))
			bufferSize = ((Integer) event.getNewValue()).intValue();
		else if (event.getProperty().equals("audio_player_priority"))
			setPriority(((Integer)event.getNewValue()).intValue());
	}
	
	/**
	 * Tell if the player is currently in loop mode or not
	 * @return true if it is in loop mode, false otherwise
	 */
	public boolean getLoop(){
		return loop;
	}
	/**
	 * Get the currently assigned AudioPlayerProvider
	 * @return The AudioPlayerProvider
	 */
	public AudioPlayerProvider getProvider(){
		return provider;
	}
	/**
	 * Set the priority of the playback/record Thread.
	 * @param p The new priority. Values between
	 * Thread.MIN_PRIORITY and Thread.MAX_PRIORITY are
	 * accepted
	 */
	public void setPriority(int p){
		p = Math.min(Math.max(Thread.MIN_PRIORITY, p), Thread.MAX_PRIORITY);
		if(priority == p) return;
		priority = p;
		if(playAndRecordThread != null) playAndRecordThread.setPriority(p);
	}
	/**
	 * Get the AudioFormat which the currently active Line is
	 * using.
	 * @return The AudioFormat of the currently active Line or
	 * null if no Line is active.
	 */
	public AudioFormat getRealFormat(){
		if(status == PLAYING | status == PAUSE_PLAY) return sourceLine.getFormat();
		else if(status == RECORDING | status == PAUSE_REC) return targetLine.getFormat();
		else return null;
	}
	/**
	 * A helper class for calculation if the current play position 
	 * @author Manu Robledo
	 *
	 */
	private class OneLoop{
		/**
		 * Start position of this loop in sample frames
		 */
		private int start,
		/**
		 * Length of this loop in sample frames
		 */
		length;
		/**
		 * Construct a new instance of this class
		 * @param start The start point of this loop in sample frames
		 * @param length The length of this loop in sample frames
		 */
		OneLoop(int start, int length){
			this.start = start;
			this.length = length;
		}
		/**
		 * 
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "OneLoop: Start "+start+", length "+length;
		}
	}
}