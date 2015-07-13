/*
 * Created on 27.05.2004
 *
 */
package com.groovemanager.app.sse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.groovemanager.core.FileManager;
import com.groovemanager.exception.InitException;
import com.groovemanager.exception.NotFinishedException;
import com.groovemanager.exception.NotReadyException;
import com.groovemanager.gui.custom.ProgressMonitor;
import com.groovemanager.gui.custom.WaveFormDisplay;
import com.groovemanager.sampled.AudioManager;
import com.groovemanager.sampled.AudioPlayer;
import com.groovemanager.sampled.AudioPlayerProvider;
import com.groovemanager.sampled.fx.Effect;
import com.groovemanager.sampled.fx.FXUnit;
import com.groovemanager.sampled.nondestructive.AudioFileSource;
import com.groovemanager.sampled.nondestructive.CutList;
import com.groovemanager.sampled.nondestructive.CutListElement;
import com.groovemanager.sampled.nondestructive.CutListSource;
import com.groovemanager.sampled.nondestructive.DeleteElement;
import com.groovemanager.sampled.nondestructive.InsertElement;
import com.groovemanager.sampled.nondestructive.ModificationListener;
import com.groovemanager.sampled.nondestructive.ReplaceElement;
import com.groovemanager.sampled.nondestructive.SubSource;
import com.groovemanager.sampled.providers.AudioFileOutputStream;
import com.groovemanager.sampled.waveform.AbstractWaveFormDisplay;
import com.groovemanager.sampled.waveform.AudioFileWaveForm;
import com.groovemanager.sampled.waveform.CreatePeakFileThread;
import com.groovemanager.sampled.waveform.DynamicAudioFileWaveForm;
import com.groovemanager.sampled.waveform.DynamicPeakWaveForm;
import com.groovemanager.sampled.waveform.Marker;
import com.groovemanager.sampled.waveform.Selectable;
import com.groovemanager.sampled.waveform.SelectableListener;
import com.groovemanager.sampled.waveform.Selection;
import com.groovemanager.sampled.waveform.WaveForm;
import com.groovemanager.sampled.waveform.PeakWaveForm;
import com.groovemanager.sampled.waveform.SavePeakWaveFormThread;
import com.groovemanager.sampled.waveform.WaveFormProvider;
import com.groovemanager.thread.ProgressThread;
import com.groovemanager.thread.SaveFileThread;

/**
 * A WaveTab represents one open file inside a SampleEditor. It has a TabItem
 * and a source file assigned to it. The WaveTab will handle all things like
 * providing audio data, handling applied effects or edit actions by managing
 * a CutList.
 * @author Manu Robledo
 *
 */
public class WaveTab implements AudioPlayerProvider, ModificationListener, WaveFormProvider{
	/**
	 * The SampleEditor to which this WaveTab belongs
	 */
	private final SimpleSampleEditor editor;
	/**
	 * The AudioFileFormat of the current source file
	 */
	protected AudioFileFormat fileFormat;
	/**
	 * The current AudioFormat of the audio data
	 */
	protected AudioFormat format;
	/**
	 * The CutList containing all modifications made to this WaveTab
	 */
	protected CutList cutList;
	/**
	 * The TabItem assigned to this Wavetab
	 */
	protected TabItem tabItem;
	/**
	 * The WaveFormDisplay for displaying the WaveForm data
	 */
	protected WaveFormDisplay waveDisplay;
	/**
	 * The source file
	 */
	protected File source;
	/**
	 * Last start position used when providing audio data
	 */
	protected int lastStart;
	/**
	 * Indicates whether this Wavetab is currently involved in a recording
	 * operation
	 */
	protected boolean recording = false;
	/**
	 * Number of channels of this WaveTab
	 */
	private int channels;
	/**
	 * OutputStream used for recording
	 */
	protected OutputStream out;
	/**
	 * Counter for redrawing the WaveFormDisplay in constant intervals during
	 * recording
	 */
	protected int recCount;
	/**
	 * Indicates whether this WaveTab has been modified since the last new, open
	 * or save operation
	 */
	protected boolean modified;
	/**
	 * Indicates whether this WaveTab contains a new file that has still not
	 * been saved
	 */
	protected boolean isNew;
	/**
	 * The last WaveForm used for providing WaveForm data to the main
	 * WaveFormDisplay 
	 */
	protected WaveForm lastWaveForm = null,
	/**
	 * The last WaveForm used for providing WaveForm data to the ZoomWaveForm
	 */
	lastZoomWaveForm = null;
	/**
	 * Position in sample frames of the lastWaveForm큦 position 
	 */
	protected int lastWaveFormPos = 0,
	/**
	 * Position in sample frames of the lastZoomWaveForm큦 position 
	 */
	lastZoomWaveFormPos = 0;
	/**
	 * The source of this WaveTab
	 */
	private AudioFileSource afSource;
	/**
	 * Indicates whether this WaveTab is able to record or not 
	 */
	private boolean canRec;
	/**
	 * The AudioFileWaveForm corresponding to the source file 
	 */
	private AudioFileWaveForm afWF;
	/**
	 * The CutListSource that was the actual one, when the last copy operation
	 * was performed 
	 */
	private CutListSource copySource;
	/**
	 * List of undo operations that can be performed
	 */
	protected ArrayList undoOperations = new ArrayList();
	/**
	 * List of redo operations that can be performed
	 */
	protected ArrayList redoOperations = new ArrayList();
	/**
	 * CutListElements corresponding to the redoOperations
	 */
	protected ArrayList redoElements = new ArrayList();
	/**
	 * Runnable for thread-safe redrawing of the WaveForm
	 */
	protected Runnable redrawWave = new Runnable() {
		public void run() {
			int w = waveDisplay.getComposite().getClientArea().width;
			int l = waveDisplay.getTotalLength();
			double z = l / (double)w / 500;
			if(waveDisplay.getZoom() == z) waveDisplay.redraw();
			else waveDisplay.zoom(z);
		}
	};
	/**
	 * Get the Shell of the editor this WaveTab is assigned to
	 * @return The Shell of the editor this WaveTab is assigned to
	 */
	public Shell getShell(){
		return editor.getShell();
	}
	/**
	 * Try to save the peak file
	 * @return true, if the operation succeeded and the file was saved, false
	 * otherwise
	 */
	protected boolean savePeak(){
		// Save peak File
		File p = FileManager.getParallelFile(source, "gmpk");
		OutputStream out;
		try {
			out = new FileOutputStream(p);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return false;
		}
		WaveForm peak = afWF.getPeakWaveForm();
		ProgressThread saver = new SavePeakWaveFormThread(peak, peak.getIntervallSize(), out, source.lastModified());
		ProgressMonitor progress = new ProgressMonitor(getShell(), saver, "Saving Peak File...", "Saving Peak File " + p.getAbsolutePath());
		try{
			progress.start();
		}
		catch(NotFinishedException ex){
			try{
				out.close();
			}
			catch(IOException e){
				e.printStackTrace();
			}
			return false;
		}
		try {
			out.close();
		} catch (IOException e4) {
			e4.printStackTrace();
			return false;
		}
		return true;
	}
	/**
	 * Save this WaveTab큦 audio data to the given file using the given
	 * AudioFileOutputStream
	 * @param f The file to save to (will be set as new source file)
	 * @param out The AudioFileOutputStream to use for writing
	 * @throws NotFinishedException If the save operation could not be completed
	 * @throws NotReadyException If the audio data could not be provided
	 */
	protected void saveFile(AudioInputStream in, File f, AudioFileOutputStream out) throws NotFinishedException, NotReadyException{
		if(!in.getFormat().equals(out.getFormat())) in = AudioSystem.getAudioInputStream(out.getFormat(), in);
		ProgressThread saver = new SaveFileThread(in, out, (int)in.getFrameLength(), in.getFormat().getFrameSize());
		ProgressMonitor progress = new ProgressMonitor(editor.getShell(), saver, "Saving Audio File...", "Saving " + f.getAbsolutePath());
		
		try{
			// Save Audio File
			progress.start();			
		}
		catch(NotFinishedException e){
			try {
				out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			throw e;
		}
		
		try {
			out.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new NotFinishedException(e1.getMessage());
		}
		
		source = f;
		savePeak();
		tabItem.setText(source.getName());
		modified = false;
	}
	/**
	 * Create a new WaveTab
	 * @param editor The SampleEditor to which the WaveTab belongs
	 * @param parent The TabFolder to be used as parent for the TabItem that
	 * will be created
	 * @param style The style to be used for the TabItem
	 * @param waveStyle The style to be used for the WaveFormDisplay
	 * @param f The source file
	 * @param wf The WaveForm corresponding to the file
	 * @param format The AudioFormat to be used for recording or
	 * <code>null</code>, if not recording
	 * @param rec true, if recording should be possible for this WaveTab, false
	 * otherwise
	 * @throws UnsupportedAudioFileException If the AudioFileFormat of the given
	 * file can not be detected (only in case of not recording)
	 * @throws IOException If an I/O error occured
	 */
	WaveTab(SimpleSampleEditor editor, TabFolder parent, int style, int waveStyle, File f, AudioFileWaveForm wf, AudioFormat format, boolean rec) throws UnsupportedAudioFileException, IOException{
		this.editor = editor;
		afWF = wf;
		channels = wf.getChannels();
		afSource = new AudioFileSource(f, afWF);
		cutList = new CutList(afSource);
		cutList.addModificationListener(this);
		waveDisplay = new WaveFormDisplay(parent, waveStyle, false);
		waveDisplay.setChannelBackgroundColor(parent.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		waveDisplay.addWaveDisplayListener(editor);
		waveDisplay.setSource(this);
		waveDisplay.setEditMarkers(true);
		waveDisplay.getComposite().addKeyListener(createKeyListener());
		waveDisplay.getComposite().addKeyListener(editor.keyListener);
		waveDisplay.addSelectableListener(new SelectableListener() {
			public void selectionPermanentChanged(Selectable s, Selection sel) {
			}
			public void selectionChanged(Selectable s, Selection sel) {
				WaveTab.this.editor.zoomSel.setEnabled(!sel.isEmpty());
			}
			public void positionChanged(Selectable s, int pos) {
			}
			public void positionWillChange(Selectable s) {
			}
			public void positionWontChange(Selectable s) {
			}
		});
		source = f;
		tabItem = new TabItem(parent, style);
		tabItem.setText(f.getName());
		tabItem.setControl(waveDisplay.getComposite());
		tabItem.setData(WaveTab.this);
		tabItem.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if(WaveTab.this.editor.player.getProvider() == WaveTab.this) WaveTab.this.editor.player.removeProvider();
			}
		});
		recording = rec;
		if(rec){
			this.format = format;
			isNew = true;
			canRec = true;
		}
		else{
			fileFormat = AudioManager.getDefault().getAudioFileFormat(f, null, new String[]{"slices", "RLND"});
			format = fileFormat.getFormat();
			isNew = false;
			canRec = false;
			
			// For compatibility to 1.4...
			Map properties = AudioManager.getProperties(fileFormat);
			if(properties != null){
				Object sl = properties.get("slices");
				if(sl != null && sl instanceof int[][]){
					int[][] slices = (int[][])sl;
					for (int i = 0; i < slices.length; i++) {
						waveDisplay.addMarker(slices[i][0], "");
					}
				}
			}
		}
	}
	/**
	 * Create the KeyListener to be added to the WaveFormDisplay큦 composite
	 * @return The KeyListener to be used
	 */
	protected KeyListener createKeyListener(){
		// KeyListener for shortcuts related to waveform
		return new KeyListener() {
			public void keyPressed(KeyEvent e) {
				switch (e.keyCode) {
					case SWT.CTRL | 'c' :
						editAction(EditAction.COPY);
						break;
					case SWT.CTRL | 'z' :
						editAction(EditAction.UNDO);
						break;
					case SWT.CTRL | 'y' :
						editAction(EditAction.REDO);
						break;
					case SWT.CTRL | 'x' :
						editAction(EditAction.CUT);
						break;
					case SWT.CTRL | 'v' :
						editAction(EditAction.PASTE);
						break;
					case SWT.CTRL | 't' :
						editAction(EditAction.TRIM);
						break;
					case SWT.DEL :
						editAction(EditAction.DELETE);
						break;
					default :
						break;
				}
			}
			public void keyReleased(KeyEvent e) {
			}
		};
	}
	/**
	 * Set the source file for this WaveTab
	 * @param f The source file to set
	 * @param waveForm The WaveForm corresponding to the file
	 */
	protected void setInput(File f, AudioFileWaveForm waveForm){
		tabItem.setText(f.getName());
		source = f;
		cutList.removeModificationListener(this);
		cutList = new CutList(new AudioFileSource(f, waveForm));
		cutList.addModificationListener(this);
		waveDisplay.redraw();
	}
	/**
	 * Get the whole AudioInputStream from this WaveTab큦 audio data independent
	 * of its selection
	 * @return The AudioInputStream for all of this WaveTab큦 audio data
	 * @throws NotReadyException If no audio data can be provided at the
	 * moment
	 */
	public AudioInputStream getWholeAudioInputStream() throws NotReadyException{
		return cutList.getAudioInputStream(0, cutList.getLength());
	}
	/**
	 * Get the AudioInputStream for the current selection
	 * @see com.groovemanager.sampled.AudioPlayerProvider#getAudioInputStream()
	 */
	public AudioInputStream getAudioInputStream() throws NotReadyException {
		Selection sel = waveDisplay.getSelection();
		lastStart = sel.getLeft();
		if(lastStart >= cutList.getLength()) lastStart = 0;
		int right = sel.getRight();
		if(right > cutList.getLength()) right = cutList.getLength();
		if(lastStart == right) return cutList.getAudioInputStream(lastStart, cutList.getLength() - right);
		else return cutList.getAudioInputStream(lastStart, right - lastStart);
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.AudioPlayerProvider#canProvide()
	 */
	public boolean canProvide() {
		return cutList.isReady();
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.AudioPlayerProvider#canLoop()
	 */
	public boolean canLoop(){
		return canProvide() && waveDisplay.hasData();
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.AudioPlayerProvider#canRec()
	 */
	public boolean canRec(){
		return canRec;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.AudioPlayerProvider#getLastStart()
	 */
	public int getLastStart() {
		return lastStart;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.AudioPlayerProvider#startRec()
	 */
	public AudioFormat startRec() throws NotReadyException{
		tabItem.getDisplay().asyncExec(new Runnable() {
			public void run() {
				waveDisplay.scroll(1);
			}
		});
		try {
			out = AudioManager.getDefault().getAudioFileOutputStream(source, format, AudioFileFormat.Type.WAVE, null, null, null);
		} catch (IOException e) {
			e.printStackTrace();
			throw new NotReadyException(e.getMessage());
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
			throw new NotReadyException(e.getMessage());
		}
		recording = true;
		return format;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.AudioPlayerProvider#stopRec()
	 */
	public void stopRec() {
		modified = true;
		canRec = false;
		recording = false;
		try {
			out.close();
			File tempPeak = File.createTempFile("gmtmp_", ".gmpk");
			((DynamicPeakWaveForm)afWF.getPeakWaveForm()).close(source.lastModified());
			try {
				AudioFileWaveForm aw = new AudioFileWaveForm(source, afWF.getPeakWaveForm(), 32 * 1024, 25);
				fileFormat = AudioSystem.getAudioFileFormat(source);
				cutList.setSource(new AudioFileSource(source, aw));
				waveDisplay.showAll();
			} catch (UnsupportedAudioFileException e1) {
				e1.printStackTrace();
				editor.errorMessage(e1.getMessage());
			}
			this.editor.player.setProvider(this);
		} catch (IOException e) {
			e.printStackTrace();
			editor.errorMessage(e.getMessage());
		}
		editor.zoomWaveDisplay.setSource(this);
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.AudioPlayerProvider#rec(byte[], int, int)
	 */
	public int rec(byte[] b, int offset, int length) {
		if(this.editor.player.getStatus() != AudioPlayer.RECORDING && this.editor.player.getStatus() != AudioPlayer.PAUSE_REC) return 0;
		int written = 0;
		try {
			out.write(b, offset, length);
			written = length;
			((DynamicAudioFileWaveForm)afWF).append(b, offset, length);
			afSource.appendFrames(length / format.getFrameSize());
		} catch (IOException e) {
			e.printStackTrace();
		}
		recCount += written / format.getFrameSize();
		if(recCount >= format.getSampleRate()){
			recCount -= format.getSampleRate();
			waveDisplay.getComposite().getDisplay().asyncExec(new Runnable() {
				public void run() {
					waveDisplay.zoom(getTotalLength() / 30.0 / format.getSampleRate());
					cutList.update();
				}
			});
		}
		return written;
	}
	/**
	 * Ask this WaveTab, if it has been modified since the last open, new or
	 * save operation
	 * @return true, if this WaveTab has been modified without saving the
	 * modification, false otherwise
	 */
	public boolean hasBeenModified(){
		return modified;
	}
	/**
	 * Ask this WaveTab, if it contains a new file that has not been saved yet.
	 * @return true, if this WaveTab큦 source is a new file that has not been
	 * saved yet, false otherwise.
	 */
	public boolean isNew(){
		return isNew;
	}
	/**
	 * Delete the current selection
	 *
	 */
	public void performDelete(){
		Selection sel = waveDisplay.getSelection();
		int pos = sel.getLeft();
		cutList.addElement(new DeleteElement(sel.getLeft(), sel.getRight() - sel.getLeft()));
		waveDisplay.setSelection(new Selection(0));
		waveDisplay.setPosition(pos);
	}
	/**
	 * Trim this WaveTab큦 data to the current selection
	 *
	 */
	public void performTrim(){
		Selection sel = waveDisplay.getSelection();
		int pos = sel.getLeft();
		cutList.addElement(new DeleteElement(0, sel.getLeft()));
		cutList.addElement(new DeleteElement(sel.getRight() - sel.getLeft(), cutList.getLength() - sel.getRight() + sel.getLeft()));
		waveDisplay.setSelection(new Selection(0, cutList.getLength()));
		waveDisplay.setPosition(0);
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.ModificationListener#update(com.groovemanager.sampled.nondestructive.CutListSource)
	 */
	public void update(CutListSource source) {
		modified = true;
		lastWaveForm = null;
		waveDisplay.setSelection(new Selection(waveDisplay.getPosition()));
		if(!isNew) tabItem.setText("* " + this.source.getName());
		if(!recording){
			lastWaveForm = null;
			lastWaveFormPos = 0;
			lastZoomWaveForm = null;
			lastZoomWaveFormPos = 0;
			waveDisplay.redraw();
			editor.zoomWaveDisplay.redraw();
			editor.tabActivated(this);
		}
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.ModificationListener#update(com.groovemanager.sampled.nondestructive.CutListElement)
	 */
	public void update(CutListElement element) {
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.WaveFormProvider#getWaveForm(com.groovemanager.sampled.waveform.AbstractWaveFormDisplay, int, int, int)
	 */
	public WaveForm getWaveForm(AbstractWaveFormDisplay display, int start, int length, int width) {
		if(display == waveDisplay){
			// Can we use the last WaveForm?
			if(lastWaveForm != null && 
					lastWaveForm.canProvide(
							(int)Math.round((start - lastWaveFormPos) / lastWaveForm.getZoomFactor()), 
							(int)Math.round(length / lastWaveForm.getZoomFactor()), 
							width))	lastWaveForm = 
								lastWaveForm.subWaveForm(
										(int)Math.round((start - lastWaveFormPos) / lastWaveForm.getZoomFactor()), 
										(int)Math.round(length / lastWaveForm.getZoomFactor()), 
										width);
			else lastWaveForm = cutList.getWaveForm(start, length, width);
			
			lastWaveFormPos = start;
			
			return lastWaveForm;
		}
		else{
			// Can we use the last WaveForm?
			if(lastZoomWaveForm != null && 
					lastZoomWaveForm.canProvide(
							(int)Math.round((start - lastZoomWaveFormPos) / lastZoomWaveForm.getZoomFactor()), 
							(int)Math.round(length / lastZoomWaveForm.getZoomFactor()), 
							width))	lastZoomWaveForm = 
								lastZoomWaveForm.subWaveForm(
										(int)Math.round((start - lastZoomWaveFormPos) / lastZoomWaveForm.getZoomFactor()), 
										(int)Math.round(length / lastZoomWaveForm.getZoomFactor()), 
										width);
			else lastZoomWaveForm = cutList.getWaveForm(start, length, width);
			
			lastZoomWaveFormPos = start;
			
			return lastZoomWaveForm;
		}
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.WaveFormProvider#getChannels()
	 */
	public int getChannels() {
		return channels;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.WaveFormProvider#getTotalLength()
	 */
	public int getTotalLength() {
		return cutList.getLength();
	}
	/**
	 * Redo the last undone operation
	 *
	 */
	public void performRedo(){
		if(!canRedo()) return;
		Object lastOperation = redoOperations.get(redoOperations.size() - 1);
		if(lastOperation instanceof Integer){
			Integer last = (Integer)redoOperations.get(redoOperations.size() - 1); 
			int action = last.intValue();
			CutListElement el;
			switch (action) {
				case EditAction.PASTE :
				case EditAction.TRIM :
					el = (CutListElement)redoElements.get(redoElements.size() - 1);
					redoElements.remove(el);
					cutList.addElement(el);
				case EditAction.CUT :
				case EditAction.DELETE :
					el = (CutListElement)redoElements.get(redoElements.size() - 1);
					redoElements.remove(el);
					cutList.addElement(el);
					break;
				default :
					break;
			}
		}
		else if(lastOperation instanceof CutListElement){
			cutList.addElement((CutListElement)lastOperation);
			redoElements.remove(lastOperation);
		}
		undoOperations.add(lastOperation);
		redoOperations.remove(lastOperation);
	}
	/**
	 * Undo the last operation
	 *
	 */
	public void performUndo(){
		if(!canUndo()) return;
		Object lastOperation = undoOperations.get(undoOperations.size() - 1);
		if(lastOperation instanceof Integer){
			Integer last = (Integer)lastOperation; 
			int action = last.intValue();
			switch (action) {
				case EditAction.PASTE :
				case EditAction.TRIM :
					redoElements.add(cutList.getLastElement());
					cutList.removeLastElement();
				case EditAction.CUT :
				case EditAction.DELETE :
					redoElements.add(cutList.getLastElement());
					cutList.removeLastElement();
					break;
				default :
					break;
			}
		}
		else if(lastOperation instanceof CutListElement){
			cutList.removeLastElement();
			redoElements.add(lastOperation);
		}
		redoOperations.add(lastOperation);
		undoOperations.remove(lastOperation);
	}
	/**
	 * Copy the current selection to the audio editor큦 cipboard
	 *
	 */
	public void performCopy(){
		prepareClipboard();
		editor.setCopySource(this);
	}
	/**
	 * Copy the current selection to the audio editor큦 cipboard
	 * and delete it from this WaveTab큦 audio data
	 *
	 */
	public void performCut(){
		prepareClipboard();
		performDelete();
		editor.setCopySource(this);
	}
	/**
	 * Insert the clipboard content of the editor at the current position
	 *
	 */
	public void performPaste() throws DifferentChannelException{
		WaveTab sourceTab = editor.getCopySource();
		if(sourceTab == null) return;
		
		if(sourceTab.getChannels() != getChannels()){
			throw new DifferentChannelException();
		}
		
		CutListSource source = sourceTab.getCopySource();
		
		int pos = waveDisplay.getSelection().getLeft();
		performDelete();
		cutList.addElement(new InsertElement(source, pos));
	}
	/**
	 * Create a clone of the current CutList to be used as clipboard source
	 *
	 */
	private void prepareClipboard(){
		Selection sel = waveDisplay.getSelection();
		int left = sel.getLeft();
		int right = sel.getRight();
		CutListSource cl = cutList.duplicate();
		if(left == 0 && right == cl.getLength()) copySource = cl;
		else copySource = new SubSource(cl, left, right - left);
	}
	/**
	 * Get the clipbaord source
	 * @return The clipboard source that was last prepared for this WaveTab,
	 * if any
	 */
	public CutListSource getCopySource(){
		return copySource;
	}
	/**
	 * Get the name of the next possible operation to undo
	 * @return The name of the next possible operation to undo
	 */
	public String getUndoName(){
		if(!canUndo()) return "";
		else {
			Object lastOperation = undoOperations.get(undoOperations.size() - 1);
			if(lastOperation instanceof Integer) return EditAction.getName(((Integer)lastOperation).intValue());
			else if(lastOperation instanceof CutListElement) return ((CutListElement)lastOperation).getName();
			else return "";
		}
	}
	/**
	 * Get the name of the next possible operation to redo
	 * @return The name of the next possible operation to redo
	 */
	public String getRedoName(){
		if(!canRedo()) return "";
		else {
			Object lastOperation = redoOperations.get(redoOperations.size() - 1);
			if(lastOperation instanceof Integer) return EditAction.getName(((Integer)lastOperation).intValue());
			else if(lastOperation instanceof CutListElement) return ((CutListElement)lastOperation).getName();
			else return "";
		}
	}
	/**
	 * Indicates whether this WaveTab can undo the last operation
	 * @return true, if undo can be applied, false otherwise
	 */
	public boolean canUndo(){
		return undoOperations.size() > 0;
	}
	/**
	 * Indicates whether this WaveTab can redo the last undone operation
	 * @return true, if redo can be applied, false otherwise
	 */
	public boolean canRedo(){
		return redoOperations.size() > 0;
	}
	/**
	 * Perform an EditAction of the given type
	 * @param type The type of the EditAction to apply
	 */
	public void editAction(int type){
		Selection sel = waveDisplay.getSelection(); 
		switch (type) {
			case EditAction.DELETE :
				if(sel.getRight() <= sel.getLeft()) return;
				performDelete();
				undoOperations.add(new Integer(type));
				redoOperations.clear();
				redoElements.clear();
				break;
			case EditAction.UNDO :
				if(!canUndo()) return;
				performUndo();
				break;
			
			case EditAction.REDO :
				if(!canRedo()) return;
				performRedo();
				break;
			
			case EditAction.COPY :
				if(sel.getRight() <= sel.getLeft()) return;
				performCopy();
				break;
				
			case EditAction.CUT :
				if(sel.getRight() <= sel.getLeft()) return;
				performCut();
				undoOperations.add(new Integer(type));
				redoOperations.clear();
				redoElements.clear();
				break;
				
			case EditAction.PASTE :
				try {
					performPaste();
					undoOperations.add(new Integer(type));
					redoOperations.clear();
					redoElements.clear();
				} catch (DifferentChannelException e) {
					e.printStackTrace();
					editor.errorMessage("Could not perform paste operation because the data in the clipboard has different number of channels.");
				}
				break;
				
			case EditAction.TRIM :
				if(sel.getRight() <= sel.getLeft()) return;
				performTrim();
				undoOperations.add(new Integer(type));
				redoOperations.clear();
				redoElements.clear();
				break;
			default :
				break;
		}
	}
	/**
	 * Get the TabItem associated with this WaveTab
	 * @return The TabItem associated with this WaveTab
	 */
	public TabItem getItem(){
		return tabItem;
	}
	/**
	 * Get this WaveTab큦 source file
	 * @return This WaveTab큦 source file
	 */
	public File getSource(){
		return source;
	}
	/**
	 * Get the WaveFormDisplay used for displaying WaveForm data
	 * @return The WaveFormDisplay of this WaveTab
	 */
	public WaveFormDisplay getWaveDisplay(){
		return waveDisplay;
	}
	/**
	 * Get the format of the audio data contained in this WaveTab
	 * @return The format of the audio data contained in this WaveTab
	 */
	public AudioFormat getFormat(){
		if(recording) return format;
		else return getFileFormat().getFormat();
	}
	/**
	 * Get the audio file format of this WaveTab큦 source
	 * @return The audio file format of this WaveTab큦 source
	 */
	public AudioFileFormat getFileFormat(){
		return fileFormat;
	}
	/**
	 * Get the slices contained in this WabeTab
	 * @return Array of slices contained in this WaveTab. Each slice is
	 * represented by an int-Array of size 2 with the sample position of the
	 * slice in sample frames at index [0] and the length of the slice or -1,
	 * if the length is not specified at index [1]
	 */
	public int[][] getSlices(){
		Marker[] markers = waveDisplay.getMarkers();
		int[][] slices = new int[markers.length][2];
		for (int i = 0; i < markers.length; i++) {
			slices[i][0] = markers[i].getPosition();
			slices[i][1] = -1;
		}
		return slices;
	}
	/**
	 * Apply the given effect to this WaveTab큦 audio data
	 * @param effect The effect to apply
	 */
	public void applyEffect(Effect effect){
		Selection sel = waveDisplay.getSelection();
		if(sel.getLeft() == sel.getRight()) waveDisplay.setSelection(new Selection(0, getTotalLength()));
		Thread thread = null;
		try {
			AudioInputStream stream = getAudioInputStream();
			int sourceChannels = stream.getFormat().getChannels();
			stream = AudioManager.getStereoInputStream(stream);
			final FXUnit unit = new FXUnit(effect);		
			if(effect.needsAnalysis()){
				Analyzer a = new Analyzer(unit, stream);
				ProgressMonitor monitor = new ProgressMonitor(getShell(), a, "Analyzing...", "Analyzing audio data");
				monitor.start();
				stream = AudioManager.getStereoInputStream(getAudioInputStream());
			}
			
			final SourceDataLine sourceLine = unit.getEffectSourceLine();
			sourceLine.open();
			sourceLine.start();
			final TargetDataLine targetLine = unit.getEffectTargetLine();
			targetLine.open();
			targetLine.start();
			if(!stream.getFormat().equals(sourceLine.getFormat())){
				if(AudioSystem.isConversionSupported(sourceLine.getFormat(), stream.getFormat()))
					stream = AudioSystem.getAudioInputStream(sourceLine.getFormat(), stream);
				else{
					editor.errorMessage("Unable to apply effect:\nFormat conversion from " + stream.getFormat() + " to " + sourceLine.getFormat() + " not supported.");
					return;
				}
			}
			
			final AudioInputStream inStream = stream;
			thread = new Thread(){
				public void run(){
					int numBytesRead = 0;
					byte[] buffer = new byte[sourceLine.getBufferSize()];
					while(numBytesRead != -1 && !getItem().isDisposed()){
						try {
							numBytesRead = inStream.read(buffer, 0, buffer.length);
						} catch (IOException e1) {
							e1.printStackTrace();
							numBytesRead = -1;
						}
						if(numBytesRead > 0){
							sourceLine.write(buffer, 0, numBytesRead);
						}
						try{
							Thread.sleep(0,1);
						} catch(InterruptedException e){}
					}
				}
			};
			thread.start();
			
			AudioInputStream in = new AudioInputStream(targetLine);
			if(sourceChannels == 1) in = AudioManager.getMonoInputStream(in);
			File tempFile = File.createTempFile("gmtmp_", ".wav");
			AudioFormat tempFormat = new AudioFormat(fileFormat.getFormat().getSampleRate(), 16, fileFormat.getFormat().getChannels(), true, false);
			AudioFileOutputStream out = AudioManager.getDefault().getAudioFileOutputStream(tempFile, tempFormat, AudioFileFormat.Type.WAVE, null,null, null);
			if(!in.getFormat().equals(out.getFormat())) in = AudioSystem.getAudioInputStream(out.getFormat(), in);
			SaveFileThread saver = new SaveFileThread(in, out, (int)inStream.getFrameLength(), in.getFormat().getFrameSize(), true);
			ProgressMonitor monitor = new ProgressMonitor(getShell(), saver, "Apply Effect", "Applying " + effect.getName() + " to Selection");
			monitor.start();
			
			File tempPeak = File.createTempFile("gmtmp_", ".gmpk");
			CreatePeakFileThread peak = new CreatePeakFileThread(AudioSystem.getAudioInputStream(tempFile), tempPeak);
			monitor = new ProgressMonitor(getShell(), peak, "Creating peak file", "Creating peak file for applied effect.");
			monitor.start();
			
			PeakWaveForm pwf = new PeakWaveForm(tempPeak);
			AudioFileWaveForm awf = new AudioFileWaveForm(tempFile, pwf, 32 * 1024, 25);
			CutListSource newSource = new AudioFileSource(tempFile, awf);
			
			sel = waveDisplay.getSelection();
			int left = sel.getLeft();
			int right = sel.getRight();
			
			ReplaceElement el = new ReplaceElement(effect.getName(), newSource, left, right - left, fileFormat.getFormat());
			cutList.addElement(el);
			undoOperations.add(el);
			redoOperations.clear();
			thread.stop();
		} catch (NotReadyException e) {
			e.printStackTrace();
			editor.errorMessage(e.getMessage());
			if(thread != null) thread.stop();
		} catch (NotFinishedException e) {
			e.printStackTrace();
			editor.errorMessage(e.getMessage());
			if(thread != null) thread.stop();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			editor.errorMessage(e.getMessage());
			if(thread != null) thread.stop();
		} catch (IOException e) {
			e.printStackTrace();
			editor.errorMessage(e.getMessage());
			if(thread != null) thread.stop();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
			editor.errorMessage(e.getMessage());
			if(thread != null) thread.stop();
		}
	}
	/**
	 * This class is used for analyzing the WaveTab큦 audio data if needed
	 * before applying an effect
	 * @author Manu Robledo
	 *
	 */
	class Analyzer extends ProgressThread{
		/**
		 * The FXUnit to use
		 */
		private final FXUnit unit;
		/**
		 * The stream providing the audio data
		 */
		private AudioInputStream in;
		/**
		 * The internal buffer
		 */
		private byte[] buffer;
		/**
		 * Number of bytes read during the last read operation or -1, if the end
		 * of the stream was reached
		 */
		private int numBytesRead = 0;
		/**
		 * The analysisLine of the FXUnit
		 */
		private SourceDataLine analysisLine;
		/**
		 * Number of bytes written so far to the analysisLine
		 */
		private int written = 0;
		/**
		 * Create a new Analyzer
		 * @param unit The FXUnit wrapped around the effect that needs analysis
		 * @param in The AudioInputStream providing the audio data
		 */
		Analyzer(FXUnit unit, AudioInputStream in){
			this.unit = unit;
			this.in = in;
		}
		/**
		 * 
		 * @see com.groovemanager.thread.ProgressThread#init()
		 */
		protected void init() throws InitException{
			analysisLine = unit.getAnalysisLine();
			try {
				analysisLine.open();
			} catch (LineUnavailableException e) {
				e.printStackTrace();
				cancelOperation();
			}
			if(!in.getFormat().equals(analysisLine.getFormat())){
				if(AudioSystem.isConversionSupported(analysisLine.getFormat(), in.getFormat()))
					in = AudioSystem.getAudioInputStream(analysisLine.getFormat(), in);
				else throw new InitException("Unable to process audio data:\nConversion from " + in.getFormat() + " to " + analysisLine.getFormat() + " not supported.");
			}
			buffer = new byte[analysisLine.getBufferSize()];
		}

		/**
		 * @see com.groovemanager.thread.ProgressThread#tellTotal()
		 */
		protected int tellTotal() {
			if(in.getFrameLength() == AudioSystem.NOT_SPECIFIED) return Integer.MAX_VALUE;
			else return (int)in.getFrameLength();
		}

		/**
		 * @see com.groovemanager.thread.ProgressThread#processNext()
		 */
		protected void processNext() throws Exception {
			numBytesRead = in.read(buffer, 0, buffer.length);
			if(numBytesRead != -1){
				analysisLine.write(buffer, 0, numBytesRead);
				written += numBytesRead / in.getFormat().getFrameSize();
			}
		}

		/**
		 * @see com.groovemanager.thread.ProgressThread#tellElapsed()
		 */
		protected int tellElapsed() {
			return written;
		}

		/**
		 * @see com.groovemanager.thread.ProgressThread#breakCondition()
		 */
		protected boolean breakCondition() {
			return numBytesRead == -1;
		}

		/**
		 * @see com.groovemanager.thread.ProgressThread#cleanUp()
		 */
		protected void cleanUp() {
			analysisLine.close();
		}

		/**
		 * @see com.groovemanager.thread.ProgressThread#result()
		 */
		protected Object result() {
			return unit;
		}
	}
	/**
	 * This Exception is to be used when sources with different channel count
	 * should be merged.
	 * @author Manu Robledo
	 *
	 */
	public class DifferentChannelException extends Exception{
		/**
		 * Create a new DifferentChannelException
		 *
		 */
		public DifferentChannelException(){
			super("Source and target have different number of channels.");
		}
	}
	/**
	 * Indicates whether this WaveTab can allows performing a Save-operation
	 * at the moment (not Save as...) or not.
	 * @return true, if<br>
	 * - the audio data has been modified and<br>
	 * - the source is not a new created file and<br>
	 * - the source file can be written,<br>
	 * false otherwise
	 */
	public boolean canSave() {
		return hasBeenModified() && !isNew() && source.canWrite();
	}
}