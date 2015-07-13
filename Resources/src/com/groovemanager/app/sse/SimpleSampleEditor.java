/*
 * Created on 12.05.2004
 *
 */
package com.groovemanager.app.sse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioFileFormat.Type;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.groovemanager.actions.ConfigAction;
import com.groovemanager.actions.file.FileCloseAction;
import com.groovemanager.actions.file.FileCloseListener;
import com.groovemanager.actions.file.FileNewAction;
import com.groovemanager.actions.file.FileNewListener;
import com.groovemanager.actions.file.FileOpenAction;
import com.groovemanager.actions.file.FileOpenListener;
import com.groovemanager.actions.file.FileSaveAction;
import com.groovemanager.actions.file.FileSaveListener;
import com.groovemanager.core.ConfigManager;
import com.groovemanager.core.FileManager;
import com.groovemanager.exception.NotFinishedException;
import com.groovemanager.exception.NotReadyException;
import com.groovemanager.gui.EditorWindow;
import com.groovemanager.gui.custom.ChooseFormatDialog;
import com.groovemanager.gui.custom.InfoGroup;
import com.groovemanager.gui.custom.InfoGroupContainer;
import com.groovemanager.gui.custom.ProgressMonitor;
import com.groovemanager.gui.custom.WaveFormDisplay;
import com.groovemanager.sampled.AudioManager;
import com.groovemanager.sampled.AudioPlayer;
import com.groovemanager.sampled.AudioPlayerListener;
import com.groovemanager.sampled.fx.Effect;
import com.groovemanager.sampled.providers.AudioFileOutputStream;
import com.groovemanager.sampled.waveform.AbstractWaveFormDisplay;
import com.groovemanager.sampled.waveform.AudioFileWaveForm;
import com.groovemanager.sampled.waveform.DynamicAudioFileWaveForm;
import com.groovemanager.sampled.waveform.WaveForm;
import com.groovemanager.sampled.waveform.PeakWaveForm;
import com.groovemanager.sampled.waveform.Selectable;
import com.groovemanager.sampled.waveform.SelectableListener;
import com.groovemanager.sampled.waveform.SelectableWaveFormDisplay;
import com.groovemanager.sampled.waveform.WaveDisplayListener;
import com.groovemanager.sampled.waveform.Selection;
import com.groovemanager.thread.SaveFileThread;

/**
 * This class represents the application window of the sample editor application
 * @author Manu Robledo
 *
 */
public class SimpleSampleEditor extends EditorWindow implements FileNewListener, FileOpenListener, FileCloseListener, FileSaveListener, WaveDisplayListener, AudioFileSaveListener{
	/**
	 * ConfigAction used for Settings
	 */
	protected ConfigAction configAction;
	/**
	 * All effect actions
	 */
	protected ArrayList effectActions = new ArrayList();
	/**
	 * Action used for saving files
	 */
	protected FileSaveAction fileSaveAction;
	/**
	 * Action used for saving files as...
	 */
	protected SaveAudioFileAsAction fileSaveAsAction;
	/**
	 * Action used for closing files
	 */
	protected FileCloseAction fileCloseAction;
	/**
	 * The action used for opening files
	 */
	protected FileOpenAction fileOpenAction;
	/**
	 * The control bar composite
	 */
	protected Composite controlBar,
	/**
	 * The left bar composite
	 */
	leftBar, rightBar,
	/**
	 * The wave tab composite
	 */
	waveContent,
	/**
	 * The main composite
	 */
	mainContent;
	/**
	 * The SashForm containing the left bar and the wave form composite
	 */
	protected SashForm sashForm;
	/**
	 * This window´s style
	 */
	protected int style = SWT.NONE;
	/**
	 * The TabFolder containing the WaveTabs
	 */
	protected TabFolder tabFolder;
	/**
	 * The transport bar on the right
	 */
	protected TransportBar transportBar;
	/**
	 * The AudioPlayer to be used for playback and recording
	 */
	protected AudioPlayer player = new AudioPlayer();
	/**
	 * A seperate Thread for updating the current position of the AudioPlayer
	 */
	protected AudioPlayerPositionReader positionReader;
	/**
	 * the FileManager to be used
	 */
	protected FileManager fileManager;
	/**
	 * The left bar
	 */
	protected LeftBar left;
	/**
	 * Zoom buttons
	 */
	protected Button zoomIn, zoomOut, zoomSel, zoomAll;
	/**
	 * The current source for paste operations
	 */
	protected WaveTab copySource;
	/**
	 * break condition for the memory reader thread
	 */
	protected boolean readMemory;
	/**
	 * The WaveFormDisplay displayed at the bottom for direct zooming
	 */
	protected WaveFormDisplay zoomWaveDisplay;
	/**
	 * Indicates whether selection changes on the zoom waveform should be
	 * handled with a zoom and scroll change or not
	 */
	protected boolean listenToZoomWaveForm = true;
	/**
	 * Indicates whether a zoom or scroll change in the main WaveFormDisplay of
	 * the currently active WaveTab should be handled with a selection change
	 * of the ZoomWaveDisplay or not 
	 */
	protected boolean listenToWaveDisplay = true;
	/**
	 * Seperate thread for showing the current memory usage
	 */
	protected Thread memoryReader;
	/**
	 * KeyListener for global shortcuts
	 */
	protected KeyListener keyListener = new KeyListener() {
		public void keyPressed(KeyEvent e) {
			switch (e.character) {
				case '+' :
					zoomIn();
					break;
				case '-' :
					zoomOut();
					break;
				case ' ' :
					if(player.getStatus() == AudioPlayer.PAUSE_PLAY ||
						player.getStatus() == AudioPlayer.PAUSE_REC)
						transportPause();
					else if(player.getStatus() == AudioPlayer.PLAYING ||
							player.getStatus() == AudioPlayer.RECORDING) transportStop();
					else if(player.getStatus() == AudioPlayer.READY_FOR_PLAY) transportPlay();
					else if(player.getStatus() == AudioPlayer.READY_FOR_REC) transportRecord();
						break;
				default :
					break;
			}
		}
		public void keyReleased(KeyEvent e) {
		}
	};

	/**
	 * Create a new SimpleSampleEditor
	 * @param shell The parent shell. May be <code>null</code> to create a
	 * top level window.
	 * @param style The window´s style
	 * @param fileManager The FileManager to use
	 */
	public SimpleSampleEditor(Shell shell, int style, FileManager fileManager) {
		super(shell);
		this.fileManager = fileManager;
		this.style = style;
		addMenuBar();
		addStatusLine();
		addToolBar(SWT.NONE);
	}
	/**
	 * Create a new SimpleSampleEditor using the default FileManager
	 * @param shell The parent shell. May be <code>null</code> to create a
	 * top level window.
	 * @param style The window´s style
	 */
	public SimpleSampleEditor(Shell shell, int style){
		this(shell, style, FileManager.getDefault());
	}
	/**
	 * Create a new SimpleSampleEditor
	 * @param shell The parent shell. May be <code>null</code> to create a
	 * top level window.
	 * @param fileManager The FileManager to use
	 */
	public SimpleSampleEditor(Shell shell, FileManager fileManager) {
		super(shell);
		this.fileManager = fileManager;
		addMenuBar();
		addStatusLine();
		addToolBar(SWT.NONE);
	}
	/**
	 * Create a new SimpleSampleEditor using the default FileManager
	 * @param shell The parent shell. May be <code>null</code> to create a
	 * top level window.
	 */
	public SimpleSampleEditor(Shell shell) {
		this(shell, FileManager.getDefault());
	}
	/**
	 * Create a new SimpleSampleEditor in a top level window
	 * @param fileManager The FileManager to use
	 */
	public SimpleSampleEditor(FileManager fileManager) {
		super();
		this.fileManager = fileManager;
		addMenuBar();
		addStatusLine();
		addToolBar(SWT.NONE);
	}
	/**
	 * Create a new SimpleSampleEditor in a top level window using the default
	 * FileManager
	 *
	 */
	public SimpleSampleEditor(){
		this(FileManager.getDefault());
	}
	/**
	 * Get the style to be used for the SashForm
	 * @return The style to be used for the SashForm
	 */
	protected int getSashFormStyle(){
		return SWT.NONE;
	}
	/**
	 * Get the weights to be used for the SashForm
	 * @return The weights to be used for the SashForm
	 */
	protected int[] getSashFormWeights(){
		// TODO Abhängig von der tatsächlichen Breite machen
		return new int[]{220, 550};
	}
	/**
	 * Get the style to be used for the control bar (bottom bar)
	 * @return The style to be used for the control bar (bottom bar)
	 */
	protected int getControlBarStyle(){
		return SWT.NONE;
	}
	/**
	 * Create the control bar elements (bottom bar)
	 * @param comp The parent composite
	 */
	protected void createControlBarElements(Composite comp){
		comp.setLayout(new FillLayout());
		zoomWaveDisplay = new WaveFormDisplay(comp, SWT.BORDER, false);

		zoomWaveDisplay.setPositionColor(zoomWaveDisplay.getComposite().getDisplay().getSystemColor(SWT.COLOR_BLACK));
		zoomWaveDisplay.setChannelBackgroundColor(zoomWaveDisplay.getComposite().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		zoomWaveDisplay.setWaveColor(zoomWaveDisplay.getComposite().getDisplay().getSystemColor(SWT.COLOR_BLACK));
		zoomWaveDisplay.setSelectionColor(zoomWaveDisplay.getComposite().getDisplay().getSystemColor(SWT.COLOR_YELLOW));
		zoomWaveDisplay.addSelectableListener(new SelectableListener() {
			public void selectionChanged(Selectable s, Selection sel) {
			}
			public void positionChanged(Selectable s, int pos) {
			}
			public void positionWillChange(Selectable s) {
			}
			public void positionWontChange(Selectable s) {
			}
			public void selectionPermanentChanged(Selectable s, Selection sel) {
				WaveTab wt = getActiveTab();
				if(listenToZoomWaveForm && wt != null){
					listenToWaveDisplay = false;
					if(sel.getLeft() == sel.getRight()) wt.waveDisplay.showAll();
					else wt.waveDisplay.showData(sel.getLeft(), sel.getRight());
					listenToWaveDisplay = true;
				}
			}
		});
	}
	/**
	 * React to the play button being pressed
	 *
	 */
	protected void transportPlay(){
		try {
			player.play();
		} catch (NotReadyException e) {
			e.printStackTrace();
			errorMessage(e.getMessage());
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			errorMessage(e.getMessage());
		}
	}
	/**
	 * React to the stop button being pressed
	 *
	 */
	protected void transportStop(){
		player.stop();
	}
	/**
	 * React to the pause button being pressed
	 *
	 */
	protected void transportPause(){
		if(player.getStatus() == AudioPlayer.PAUSE_REC || player.getStatus() == AudioPlayer.PAUSE_PLAY)
			try {
				player.cont();
			} catch (NotReadyException e) {
				e.printStackTrace();
				errorMessage(e.getMessage());
			}
		else player.pause();
	}
	/**
	 * React to the loop button being pressed
	 *
	 */
	protected void transportLoop(){
		player.switchLoop();
	}
	/**
	 * React to the record button being pressed
	 *
	 */
	protected void transportRecord(){
		switch(player.getStatus()){
			case AudioPlayer.READY_FOR_REC:
				try {
					player.rec();
				} catch (NotReadyException e) {
					e.printStackTrace();
					errorMessage(e.getMessage());
				} catch (LineUnavailableException e) {
					e.printStackTrace();
					errorMessage(e.getMessage());
				}
				break;
			default:
				errorMessage("Not ready for recording: " + player.getStatus());
				
		}
		
	}
	/**
	 * React to the back button being pressed
	 *
	 */
	protected void transportBack(){
		errorMessage("Still not implemented :(");
	}
	/**
	 * React to the search back button being pressed
	 *
	 */
	protected void transportBackSearch(){
		errorMessage("Still not implemented :(");
	}
	/**
	 * React to the forward button being pressed
	 *
	 */
	protected void transportFwd(){
		errorMessage("Still not implemented :(");
	}
	/**
	 * React to the search forward button being pressed
	 *
	 */
	protected void transportFwdSearch(){
		errorMessage("Still not implemented :(");
	}
	/**
	 * Get the style to be used for the left bar
	 * @return the style to be used for the left bar
	 */
	protected int getLeftBarStyle() {
		return SWT.NONE;
	}
	/**
	 * Create the elements for the left bar
	 * @param comp The parent Composite
	 */
	protected void createLeftBarElements(Composite comp){
		left = new LeftBar(comp);
	}
	/**
	 * Get the style to be used for the WaveForm content
	 * @return The style to be used for the WaveForm content
	 */
	protected int getWaveContentStyle(){
		return SWT.NONE;
	}
	/**
	 * Get the style to be used for the TabFolder
	 * @return The style to be used for the TabFolder
	 */
	protected int getTabFolderStyle(){
		return SWT.NONE;
	}
	/**
	 * Create the elements for the WaveForm content
	 * @param comp The parent composite
	 */
	protected void createWaveContentElements(Composite comp){
		comp.setLayout(new FillLayout());
		tabFolder = new TabFolder(comp, getTabFolderStyle());
		tabFolder.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if(player.getStatus() == AudioPlayer.PLAYING || player.getStatus() == AudioPlayer.PAUSE_PLAY || player.getStatus() == AudioPlayer.PAUSE_REC){
					player.stop();
				}
				TabItem[] items = tabFolder.getSelection();
				if(items.length == 0) tabActivated(null);
				else tabActivated((WaveTab)items[0].getData());
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
	/**
	 * React to a WaveTab being selected to the Foreground
	 * @param wt The WaveTab that has been selected or <code>null</code>, if all
	 * WaveTabs have been closed
	 */
	protected void tabActivated(WaveTab wt){
		if(wt == null){
			player.removeProvider();
			zoomWaveDisplay.removeSource();
			zoomIn.setEnabled(false);
			zoomOut.setEnabled(false);
			zoomSel.setEnabled(false);
			zoomAll.setEnabled(false);
			sourceFileChanged(null);
		}
		else{
			player.setProvider(wt);
			zoomWaveDisplay.setSource(wt);
			fileSaveAsAction.setFormat(wt.getFormat());
			zoomIn.setEnabled(true);
			zoomOut.setEnabled(wt.waveDisplay.getZoom() >= 1);
			zoomSel.setEnabled(!wt.waveDisplay.getSelection().isEmpty());
			zoomAll.setEnabled(true);
			sourceFileChanged(wt.source);
		}
		left.setSource(wt);
		for (Iterator iter = effectActions.iterator(); iter.hasNext();) {
			EffectAction action = (EffectAction) iter.next();
			action.setWaveTab(wt);
		}
	}
	/**
	 * Notification that the currently active source file (represented by a
	 * WaveTab instance) has changed.
	 * @param newSource The new active source file
	 */
	protected void sourceFileChanged(File newSource){
		fileCloseAction.setFile(newSource);
		if(newSource != null) fileOpenAction.setFilterPath(newSource.getParent());
		fileSaveAction.setFile(newSource);
		fileSaveAsAction.setSourceFile(newSource);
		
		WaveTab wt = getActiveTab();
		fileSaveAction.setEnabled(wt != null && wt.canSave());
	}
	/**
	 * Get the style to be used for the right bar
	 * @return The style to be used for the right bar
	 */
	protected int getRightBarStyle(){
		return SWT.BORDER;
	}
	/**
	 * Zoom one level in
	 */
	protected void zoomIn(){
		AbstractWaveFormDisplay display = getWaveDisplay();
		if(display == null) return;
		
		display.zoom(display.getZoom() * 2);
	}
	/**
	 * Zoom one level out
	 *
	 */
	protected void zoomOut(){
		AbstractWaveFormDisplay display = getWaveDisplay();
		if(display == null) return;
		
		display.zoom(display.getZoom() / 2);
	}
	/**
	 * Zoom and scroll to the current selection
	 *
	 */
	protected void zoomSelection(){
		SelectableWaveFormDisplay display = (SelectableWaveFormDisplay)getWaveDisplay();
		if(display == null) return;
		
		display.showSelection();
	}
	/**
	 * Zoom and scroll in the way that the whole WaveForm is visible and nothing
	 * more
	 *
	 */
	protected void zoomAll(){
		SelectableWaveFormDisplay display = (SelectableWaveFormDisplay)getWaveDisplay();
		if(display == null) return;
		
		display.showAll();
	}
	/**
	 * Get the current WaveFormDisplay
	 * @return The WaveFormDisplay of the currently active WaveTab
	 */
	protected AbstractWaveFormDisplay getWaveDisplay(){
		WaveTab wt = getActiveTab();
		if(wt == null) return null;
		else return wt.waveDisplay;
	}
	/**
	 * Create the elementes for the right bar
	 * @param parent The parent composite
	 */
	protected void createRightBarElements(Composite parent){
		parent.setLayout(new FormLayout());
		FormData fd;
		
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, true));
		fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.bottom = fd.right = new FormAttachment(100, 0);
		fd.height = -1;
		comp.setLayoutData(fd);
		
		zoomIn = new Button(comp, SWT.PUSH);
		zoomIn.setText("Zoom +");
		zoomIn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		zoomIn.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				zoomIn();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		zoomIn.setEnabled(false);

		zoomOut = new Button(comp, SWT.PUSH);
		zoomOut.setText("Zoom -");
		zoomOut.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		zoomOut.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				zoomOut();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		zoomOut.setEnabled(false);
		
		zoomSel = new Button(comp, SWT.PUSH);
		zoomSel.setText("Zoom Sel");
		zoomSel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		zoomSel.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				zoomSelection();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		zoomSel.setEnabled(false);

		zoomAll = new Button(comp, SWT.PUSH);
		zoomAll.setText("Zoom All");
		zoomAll.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		zoomAll.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				zoomAll();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		zoomAll.setEnabled(false);
		
		transportBar = new TransportBar(parent);
		transportBar.disableAll();
		player.addAudioPlayerListener(transportBar);
		fd = new FormData();
		fd.left = fd.top = new FormAttachment(0, 0);
		fd.right = new FormAttachment(100, 0);
		fd.height = -1;
		transportBar.getComposite().setLayoutData(fd);
	}
	/**
	 * 
	 * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		FormData fd;
		FormAttachment faBegin = new FormAttachment(0, 0);
		FormAttachment faEnd = new FormAttachment(100, 0);
		
		// The all-containing Composite
		mainContent = new Composite(parent, style);
		mainContent.setLayout(new FormLayout());
		
		// The Control Bar
		controlBar = new Composite(mainContent, getControlBarStyle());
		fd = new FormData();
		fd.left = faBegin;
		fd.right = fd.bottom = faEnd;
		fd.height = -1;
		controlBar.setLayoutData(fd);
		createControlBarElements(controlBar);
		
		// The right Bar
		rightBar = new Composite(mainContent, getRightBarStyle());
		createRightBarElements(rightBar);
		fd = new FormData();
		fd.width = -1;
		fd.right = faEnd;
		fd.bottom = new FormAttachment(controlBar, 0, SWT.TOP);
		fd.top = faBegin;
		rightBar.setLayoutData(fd);

		// The SashForm
		sashForm = new SashForm(mainContent, getSashFormStyle());
		fd = new FormData();
		fd.left = fd.top = faBegin;
		fd.right = new FormAttachment(rightBar, 0, SWT.LEFT);
		fd.bottom = new FormAttachment(controlBar, 0, SWT.TOP);
		sashForm.setLayoutData(fd);
		
		// The left Bar
		leftBar = new Composite(sashForm, getLeftBarStyle());
		createLeftBarElements(leftBar);
		
		// The Wave Content
		waveContent = new Composite(sashForm, getWaveContentStyle());
		createWaveContentElements(waveContent);
		
		sashForm.setWeights(getSashFormWeights());
		
		getShell().setText(getTitle());
		getShell().setImage(getImage());
		
		getShell().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				transportStop();
				readMemory = false;
				memoryReader.stop();
			}
		});
		
		/*
		TabItem[] items = tabFolder.getItems();
		for (int i = 0; i < items.length; i++) {
			WaveTab wt = (WaveTab)items[i].getData();
			if(wt != null){
				if(wt.hasBeenModified() || wt.isNew()){
					MessageDialog dialog = new MessageDialog(getShell(), "File has not been saved.", null, "The file " + wt.source + " has not been saved. Do you want to save it before closing?", MessageDialog.QUESTION, new String[]{"Yes", "No", "Cancel"}, 0);
					switch (dialog.open()) {
						case 0 :
							if(wt.canSave()) fileSaved(wt.source, wt.source);					
							else{
								fileSaveAsAction.setSourceFile(wt.source);
								fileSaveAsAction.run();
							}
							break;
						case 1 :
							break;
						default :
							e.doit = false;
							return;
					}
				}

			}
		}
		*/
		/*
		getShell().addListener(SWT.Close, new Listener() {
			public void handleEvent(Event e){
				e.doit = false;
				System.out.println(e.widget);
			}
		});
		*/
		// Global Key Listener
		getShell().addKeyListener(keyListener);
		waveContent.addKeyListener(keyListener);
		parent.addKeyListener(keyListener);
		mainContent.addKeyListener(keyListener);
		leftBar.addKeyListener(keyListener);
		rightBar.addKeyListener(keyListener);
		sashForm.addKeyListener(keyListener);
		
		DropTarget target = new DropTarget(getShell(), DND.DROP_LINK | DND.DROP_DEFAULT);
		target.setTransfer(new Transfer[]{FileTransfer.getInstance()});
		target.addDropListener(new DropTargetListener() {
			public void dragEnter(DropTargetEvent event) {
			}
			public void dragLeave(DropTargetEvent event) {
			}
			public void dragOperationChanged(DropTargetEvent event) {
			}
			public void dragOver(DropTargetEvent event) {
			}
			public void drop(DropTargetEvent event) {
				if(FileTransfer.getInstance().isSupportedType(event.currentDataType)){
					String[] files = (String[])event.data;
					for (int i = 0; i < files.length; i++) {
						File f = new File(files[i]);
						fileOpened(f);
					}
				}
			}
			public void dropAccept(DropTargetEvent event) {
			}
		});
		
		setShells(getShell());
		
		return mainContent;
	}
	/**
	 * This method is called after the Shell has been created so that the Shell
	 * can be set for any action or other object that needs it.
	 * @param s This Window´s Shell
	 */
	protected void setShells(Shell s){
		fileSaveAction.setEnabled(false);
		fileSaveAsAction.setEnabled(false);
		fileCloseAction.setEnabled(false);
		
		for (Iterator iter = effectActions.iterator(); iter.hasNext();) {
			EffectAction action = (EffectAction) iter.next();
			action.setEnabled(false);
		}
		
		configAction.setShell(s);
		fileOpenAction.setShell(s);
		fileSaveAsAction.setShell(s);
		//TODO besser lösen
		fileSaveAsAction.setFormatDialog(new ChooseFormatDialog(s, new AudioFormat(44100, 16, 2, true, false), "Choose format for saving", ChooseFormatDialog.SAMPLE_SIZE, new int[]{16, 24, 32}, null, new AudioFormat.Encoding[]{AudioFormat.Encoding.PCM_SIGNED}, 8));
	}
	/**
	 * Get the icon to be used for this editor
	 * @return The icon to be used for this editor
	 */
	protected Image getImage(){
		Image img = new Image(getShell().getDisplay(), fileManager.getRootPath("icons/wave.gif"));
		img.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND));
		return img;
	}
	/**
	 * Get the title to be used for the window
	 * @return The window title to be used
	 */
	protected String getTitle(){
		return "Simple Sample Editor";
	}
	/**
	 * Add the file menu elements to the given menu manager
	 * @param fileMenu The file Menu´s MenuManager
	 */
	protected void createFileMenu(MenuManager fileMenu){
		final FileNewAction fileNewAction = new FileNewAction("&New\tCtrl+N");
		fileNewAction.setId("New");
		fileNewAction.setAccelerator(SWT.CTRL | 'n');
		fileMenu.add(fileNewAction);
		fileNewAction.addFileNewListener(this);
		
		fileMenu.add(new Separator());
		
		fileOpenAction = new FileOpenAction("&Open\tCtrl+O", true);
		fileOpenAction.setId("Open");
		fileOpenAction.setAccelerator(SWT.CTRL | 'o');
		fileMenu.add(fileOpenAction);
		fileOpenAction.addFileOpenListener(this);
		
		fileCloseAction = new FileCloseAction("&Close\tAlt+X");
		fileCloseAction.setId("Close");
		fileCloseAction.setAccelerator(SWT.ALT | 'x');
		fileMenu.add(fileCloseAction);
		fileCloseAction.addFileCloseListener(this);

		fileMenu.add(new Separator());
		
		fileSaveAction = new FileSaveAction("Save\tCtrl+S");
		fileSaveAction.setId("Save");
		fileSaveAction.setAccelerator(SWT.CTRL | 's');
		fileMenu.add(fileSaveAction);
		fileSaveAction.addFileSaveListener(this);

		fileSaveAsAction = new SaveAudioFileAsAction("Save &As...\tCtrl+Shift+S", true);
		fileSaveAsAction.setId("SaveAs");
		fileSaveAsAction.setAccelerator(SWT.CTRL | SWT.SHIFT | 's');
		fileMenu.add(fileSaveAsAction);
		fileSaveAsAction.addAudioFileSaveListener(this);
	}
	/**
	 * Get the currently active WaveTab
	 * @return The currently active WaveTab or <code>null</code>, if no WaveTab
	 * is actually present
	 */
	protected WaveTab getActiveTab(){
		int index = tabFolder.getSelectionIndex();
		if(index == -1) return null;
		
		return (WaveTab)tabFolder.getItem(index).getData();
	}
	/**
	 * Get the source file of the currently active WaveTab
	 * @return The source file of the currently active WaveTab or 
	 * <code>null</code>, if no WaveTab is actually present 
	 */
	protected File getActiveSource(){
		WaveTab wt = getActiveTab();
		if(wt != null) return wt.source;
		else return null;
	}
	/**
	 * Add the options menu elements to the given menu manager
	 * @param optionsMenu The options Menu´s MenuManager
	 */
	protected void createOptionsMenu(MenuManager optionsMenu){
		configAction = createConfigAction();
		configAction.setId("Settings");
		configAction.setAccelerator(SWT.ALT | 's');
		optionsMenu.add(configAction);
		optionsMenu.addMenuListener(new IMenuListener(){
			public void menuAboutToShow(IMenuManager manager) {
				configAction.setShell(getShell());
			}
		});
		
	}
	/**
	 * Create the action that will be used for "Settings"
	 * @return The action that will be used for "Settings"
	 */
	protected ConfigAction createConfigAction(){
		return new ConfigAction("&Settings\tAlt+S", new String[]{"audio"});
	}
	/**
	 * Add the edit menu elements to the given menu manager
	 * @param editMenu The edit Menu´s MenuManager
	 */
	protected void createEditMenu(MenuManager editMenu){
		// Copy
		final EditAction copy = new EditAction(this, "&Copy\tCtrl+C", EditAction.COPY);
		copy.setId("Copy");
		editMenu.add(copy);

		// Cut
		final EditAction cut = new EditAction(this, "C&ut\tCtrl+X", EditAction.CUT);
		cut.setId("Cut");
		editMenu.add(cut);

		// Paste
		final EditAction paste = new EditAction(this, "&Paste\tCtrl+V", EditAction.PASTE);
		paste.setId("Paste");
		editMenu.add(paste);

		editMenu.add(new Separator());

		// Delete
		final EditAction delete = new EditAction(this, "&Delete\tDel", EditAction.DELETE);
		delete.setId("Delete");
		editMenu.add(delete);
		
		// Trim
		final EditAction trim = new EditAction(this, "&Trim\tCtrl+T", EditAction.TRIM);
		trim.setId("Trim");
		editMenu.add(trim);
		
		editMenu.add(new Separator());
		
		// Undo
		final EditAction undo = new EditAction(this, "&Undo\tCtrl+Z", EditAction.UNDO);
		undo.setId("Undo");
		editMenu.add(undo);

		// Redo
		final EditAction redo = new EditAction(this, "&Redo\tCtrl+Y", EditAction.REDO);
		redo.setId("Redo");
		editMenu.add(redo);
		
		editMenu.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				WaveTab wt = getActiveTab();
				if(wt == null){
					undo.setEnabled(false);
					undo.setText("&Undo\tCtrl+Z");
					redo.setEnabled(false);
					redo.setText("&Redo\tCtrl+Y");
					delete.setEnabled(false);
					copy.setEnabled(false);
					cut.setEnabled(false);
					paste.setEnabled(false);
					trim.setEnabled(false);
				}
				else{
					undo.setEnabled(wt.canUndo());
					undo.setText("Undo " + wt.getUndoName() + "\tCtrl+Z");
					redo.setEnabled(wt.canRedo());
					redo.setText("Redo " + wt.getRedoName() + "\tCtrl+Y");
					Selection sel = wt.waveDisplay.getSelection();
					if(sel.getLeft() >= sel.getRight()){
						copy.setEnabled(false);
						cut.setEnabled(false);
						delete.setEnabled(false);
						trim.setEnabled(false);
					}
					else{
						copy.setEnabled(true);
						cut.setEnabled(true);
						delete.setEnabled(true);
						trim.setEnabled(true);
					}
					if(getCopySource() != null && getCopySource().getChannels() == wt.getChannels()) paste.setEnabled(true);
					else paste.setEnabled(false);
				}
			}
		});
	}
	/**
	 * Add the effect menu elements to the given menu manager
	 * @param parent The effect Menu´s MenuManager
	 */
	protected void createEffectMenu(MenuManager parent){
		List effects = AudioManager.getDefault().getProviders(Effect.class);
		for (Iterator iter = effects.iterator(); iter.hasNext();) {
			Effect effect = (Effect)iter.next();
			EffectAction action = new EffectAction(effect.getName(), effect.getClass());
			action.setId(effect.getName());
			effectActions.add(action);
			parent.add(action);
		}
	}
	
	protected MenuManager createMenuManager(){
		MenuManager menuManager = new MenuManager();
		
		// File
		MenuManager fileMenu = new MenuManager("&File");
		createFileMenu(fileMenu);
		menuManager.add(fileMenu);
		
		// Edit
		MenuManager editMenu = new MenuManager("&Edit");
		createEditMenu(editMenu);
		menuManager.add(editMenu);
		
		// Effect
		MenuManager effectMenu = new MenuManager("&Effect");
		createEffectMenu(effectMenu);
		menuManager.add(effectMenu);
		
		// Options
		MenuManager optionsMenu = new MenuManager("&Options");
		createOptionsMenu(optionsMenu);
		menuManager.add(optionsMenu);

		return menuManager;
	}
	/**
	 * Get the style to be used for the WaveForm composite
	 * @return The style to be used for the WaveForm composite
	 */
	protected int getWaveStyle(){
		return SWT.H_SCROLL;
	}
	/**
	 * Get the style to be used for each Tabitem
	 * @return The style to be used for each Tabitem
	 */
	protected int getTabItemStyle(){
		return SWT.NONE;
	}
	/**
	 * Check, if the given file is opened somewhere inside this editor
	 * @param f The file to check for
	 * @return true, if the file is open in this editor, false otherwise
	 */
	protected boolean isOpen(File f){
		TabItem[] items = tabFolder.getItems();
		for (int i = 0; i < items.length; i++) {
			WaveTab w = (WaveTab)items[i].getData();
			if(f.equals(w.source)) return true;
		}
		return false;
	}
	/**
	 * Bring the WaveTab to the foreground to ehich the given file belongs
	 * @param f The source file of the WaveTab to bring to the foreground
	 * @return true, if such a WaveTab was found, false otherwise
	 */
	protected boolean selectFileTab(File f){
		if(f == null) return false;
		TabItem[] items = tabFolder.getItems();
		for (int i = 0; i < items.length; i++) {
			WaveTab w = (WaveTab)items[i].getData();
			if(f.equals(w.source)){
				tabFolder.setSelection(new TabItem[]{items[i]});
				return true;
			}
		}
		return false;
	}
	/**
	 * 
	 * @see com.groovemanager.actions.file.FileOpenListener#fileOpened(java.io.File)
	 */
	public void fileOpened(File f) {
		if(f == null || selectFileTab(f)) return;
		try {
			WaveForm peak = PeakWaveForm.createPeakWaveForm(f, getShell());
			AudioFileWaveForm wf = new AudioFileWaveForm(f, peak, 32 * 1024, 50);
			WaveTab wt = new WaveTab(this, tabFolder, getTabItemStyle(), getWaveStyle(), f, wf, null, false);
			tabFolder.setSelection(new TabItem[]{wt.tabItem});
			tabActivated(wt);
		} catch (UnsupportedAudioFileException e) {
			errorMessage("Audio File not supported.\n" + e.getMessage());
		} catch (NotFinishedException e) {
			errorMessage(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			errorMessage("An I/O Error occured:\n" + e.getMessage());
		}
	}
	/**
	 * Get the WaveTab to which the given file is the source
	 * @param f The file to get the Wavetab for
	 * @return The WaveTab to which the given file belongs or <code>null</code>,
	 * if no WaveTab was found for this file
	 */
	protected WaveTab getTabForFile(File f){
		if(f != null){
			TabItem[] items = tabFolder.getItems();
			for (int i = 0; i < items.length; i++) {
				WaveTab w = (WaveTab)items[i].getData();
				if(w.source.equals(f)) return w;
			}
		}
		return null;
	}
	/**
	 * 
	 * @see com.groovemanager.actions.file.FileCloseListener#fileClosed(java.io.File)
	 */
	public void fileClosed(File f) {
		WaveTab w = getTabForFile(f);
		if(w == null) return;
		
		if(w.hasBeenModified() || w.isNew()){
			MessageDialog dialog = new MessageDialog(getShell(), "File has not been saved.", null, "The file " + f + " has not been saved. Do you want to save it before closing?", MessageDialog.QUESTION, new String[]{"Yes", "No", "Cancel"}, 0);
			switch (dialog.open()) {
				case 0 :
					if(w.canSave()) fileSaved(w.source, w.source);					
					else{
						fileSaveAsAction.setSourceFile(w.source);
						fileSaveAsAction.run();
					}
					break;
				case 1 :
					break;
				default :
					return;
			}
		}
		
		if(getCopySource() == w) removeCopySource();
		w.tabItem.dispose();
		
		tabActivated(getActiveTab());
	}
	/**
	 * Get the style to be used for the transport bar
	 * @return The style to be used for the transport bar
	 */
	protected int getTransportBarStyle(){
		return SWT.NONE;
	}
	/**
	 * This class is used for the transport bar of a Sample editor
	 * @author Manu Robledo
	 *
	 */
	private class TransportBar implements AudioPlayerListener{
		/**
		 * The main composite
		 */
		protected Composite comp;
		/**
		 * Transport buttons
		 */
		protected Button playButton, recButton, backButton, fwdButton, backSearchButton, fwdSearchButton, pauseButton, stopButton, loopButton;
		/**
		 * Transport button images
		 */
		protected Image playEnabled, playDisabled, recEnabled, recDisabled, pauseEnabled, pauseDisabled,
			fwdEnabled, fwdDisabled, fwdSearchEnabled, fwdSearchDisabled, backEnabled, backDisabled,
			backSearchEnabled, backSearchDisabled, stopEnabled, stopDisabled, loopEnabled, loopDisabled,
			playActiveEnabled, playActiveDisabled, recActiveEnabled, recActiveDisabled, pauseActiveEnabled,
			pauseActiveDisabled, loopActiveEnabled, loopActiveDisabled;
		/**
		 * Values indicating enabled status for the transport buttons
		 */
		protected boolean canPlay, canPause, canStop, canRec, canFwd, canBack, canSearchBack, canSearchFwd, canLoop;
		/**
		 * Values indicating active status for the transport buttons
		 */
		protected boolean activePlay, activePause, activeLoop, activeRec;
		/**
		 * Create a new TransportBar
		 * @param parent The parent composite
		 */
		TransportBar(Composite parent){
			comp = new Composite(parent, getTransportBarStyle());
			comp.setLayout(new GridLayout(1, true));
			String path = fileManager.getRootPath("icons/trans/");
			String dispath = fileManager.getRootPath("icons/trans/dis/");
			String actpath = fileManager.getRootPath("icons/trans/act/");
			String actdispath = fileManager.getRootPath("icons/trans/act/dis/");
			Button b;
			Image img;
			
				
			// >
			b = new Button(comp, SWT.PUSH);
			b.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			img = new Image(comp.getDisplay(), actpath + "play.gif");
			img.setBackground(b.getBackground());
			playActiveEnabled = img;
			img = new Image(comp.getDisplay(), actdispath + "play.gif");
			img.setBackground(b.getBackground());
			playActiveDisabled = img;
			img = new Image(comp.getDisplay(), dispath + "play.gif");
			img.setBackground(b.getBackground());
			playDisabled = img;
			img = new Image(comp.getDisplay(), path + "play.gif");
			img.setBackground(b.getBackground());
			playEnabled = img;
			b.setImage(img);
			b.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					if(canPlay) transportPlay();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			playButton = b;
	
			// ||
			b = new Button(comp, SWT.PUSH);
			b.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			img = new Image(comp.getDisplay(), actpath + "pause.gif");
			img.setBackground(b.getBackground());
			pauseActiveEnabled = img;
			img = new Image(comp.getDisplay(), actdispath + "pause.gif");
			img.setBackground(b.getBackground());
			pauseActiveDisabled = img;
			img = new Image(comp.getDisplay(), dispath + "pause.gif");
			img.setBackground(b.getBackground());
			pauseDisabled = img;
			img = new Image(comp.getDisplay(), path + "pause.gif");
			img.setBackground(b.getBackground());
			pauseEnabled = img;
			b.setImage(img);
			b.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					if(canPause) transportPause();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			pauseButton = b;
	
			// []
			b = new Button(comp, SWT.PUSH);
			b.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			img = new Image(comp.getDisplay(), dispath + "stop.gif");
			img.setBackground(b.getBackground());
			stopDisabled = img;
			img = new Image(comp.getDisplay(), path + "stop.gif");
			img.setBackground(b.getBackground());
			stopEnabled = img;
			b.setImage(img);
			b.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					if(canStop) transportStop();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			stopButton = b;
			
			// O
			b = new Button(comp, SWT.PUSH);
			b.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			img = new Image(comp.getDisplay(), actpath + "rec.gif");
			img.setBackground(b.getBackground());
			recActiveEnabled = img;
			img = new Image(comp.getDisplay(), actdispath + "rec.gif");
			img.setBackground(b.getBackground());
			recActiveDisabled = img;
			img = new Image(comp.getDisplay(), dispath + "rec.gif");
			img.setBackground(b.getBackground());
			recDisabled = img;
			img = new Image(comp.getDisplay(), path + "rec.gif");
			img.setBackground(b.getBackground());
			recEnabled = img;
			b.setImage(img);
			b.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					if(canRec) transportRecord();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			recButton = b;
	
			/*
			// |<<
			b = new Button(comp, SWT.PUSH);
			b.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			img = new Image(comp.getDisplay(), dispath + "back.gif");
			img.setBackground(b.getBackground());
			backDisabled = img;
			img = new Image(comp.getDisplay(), path + "back.gif");
			img.setBackground(b.getBackground());
			backEnabled = img;
			b.setImage(img);
			b.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					if(canBack) transportBack();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			backButton = b;
			
			// <<
			b = new Button(comp, SWT.PUSH);
			b.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			img = new Image(comp.getDisplay(), dispath + "back_search.gif");
			img.setBackground(b.getBackground());
			backSearchDisabled = img;
			img = new Image(comp.getDisplay(), path + "back_search.gif");
			img.setBackground(b.getBackground());
			backSearchEnabled = img;
			b.setImage(img);
			b.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					if(canSearchBack) transportBackSearch();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			backSearchButton = b;
	
			// >>
			b = new Button(comp, SWT.PUSH);
			b.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			img = new Image(comp.getDisplay(), dispath + "fwd_search.gif");
			img.setBackground(b.getBackground());
			fwdSearchDisabled = img;
			img = new Image(comp.getDisplay(), path + "fwd_search.gif");
			img.setBackground(b.getBackground());
			fwdSearchEnabled = img;
			b.setImage(img);
			b.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					if(canSearchFwd) transportFwdSearch();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			fwdSearchButton = b;
			
			// >>|
			b = new Button(comp, SWT.PUSH);
			b.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			img = new Image(comp.getDisplay(), dispath + "fwd.gif");
			img.setBackground(b.getBackground());
			fwdDisabled = img;
			img = new Image(comp.getDisplay(), path + "fwd.gif");
			img.setBackground(b.getBackground());
			fwdEnabled = img;
			b.setImage(img);
			b.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					if(canFwd) transportFwd();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			fwdButton = b;
			*/

			// Loop
			b = new Button(comp, SWT.PUSH);
			b.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			img = new Image(comp.getDisplay(), actpath + "loop.gif");
			img.setBackground(b.getBackground());
			loopActiveEnabled = img;
			img = new Image(comp.getDisplay(), actdispath + "loop.gif");
			img.setBackground(b.getBackground());
			loopActiveDisabled = img;
			img = new Image(comp.getDisplay(), dispath + "loop.gif");
			img.setBackground(b.getBackground());
			loopDisabled = img;
			img = new Image(comp.getDisplay(), path + "loop.gif");
			img.setBackground(b.getBackground());
			loopEnabled = img;
			b.setImage(img);
			b.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					if(canLoop) transportLoop();
					else loopButton.setSelection(false);
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			loopButton = b;
	
		}
		/**
		 * Get the main composite
		 * @return The main composite
		 */
		public Composite getComposite(){
			return comp;
		}
		/**
		 * Enable the play button
		 *
		 */
		protected void enablePlay(){
			canPlay = true;
			if(activePlay) playButton.setImage(playActiveEnabled);
			else playButton.setImage(playEnabled);
		}
		/**
		 * Disable the play button
		 *
		 */
		protected void disablePlay(){
			canPlay = false;
			if(activePlay) playButton.setImage(playActiveDisabled);
			else playButton.setImage(playDisabled);
		}
		/**
		 * Enable the loop button
		 *
		 */
		protected void enableLoop(){
			canLoop = true;
			if(activeLoop) loopButton.setImage(loopActiveEnabled);
			else loopButton.setImage(loopEnabled);
		}
		/**
		 * Disable the loop button
		 *
		 */
		protected void disableLoop(){
			canLoop = false;
			if(activeLoop) loopButton.setImage(loopActiveDisabled);
			else loopButton.setImage(loopDisabled);
		}
		/**
		 * Enable the pause button
		 *
		 */
		protected void enablePause(){
			canPause = true;
			if(activePause) pauseButton.setImage(pauseActiveEnabled);
			else pauseButton.setImage(pauseEnabled);
		}
		/**
		 * Disable the pause button
		 *
		 */
		protected void disablePause(){
			canPause = false;
			if(activePause) pauseButton.setImage(pauseActiveDisabled);
			else pauseButton.setImage(pauseDisabled);
		}
		/**
		 * Enable the stop button
		 *
		 */
		protected void enableStop(){
			canStop = true;
			stopButton.setImage(stopEnabled);
		}
		/**
		 * Disable the stop button
		 *
		 */
		protected void disableStop(){
			canStop = false;
			stopButton.setImage(stopDisabled);
		}
		/**
		 * Enable the rec button
		 *
		 */
		protected void enableRec(){
			canRec = true;
			if(activeRec) recButton.setImage(recActiveEnabled);
			else recButton.setImage(recEnabled);
		}
		/**
		 * Disable the rec button
		 *
		 */
		protected void disableRec(){
			canRec = false;
			if(activeRec) recButton.setImage(recActiveDisabled);
			else recButton.setImage(recDisabled);
		}
		/**
		 * Enable the forward button
		 *
		 */
		protected void enableFwd(){
			canFwd = true;
			fwdButton.setImage(fwdEnabled);
		}
		/**
		 * Disable the forward button
		 *
		 */
		protected void disableFwd(){
			canFwd = false;
			fwdButton.setImage(fwdDisabled);
		}
		/**
		 * Enable the forward search button
		 *
		 */
		protected void enableSearchFwd(){
			canSearchFwd = true;
			fwdSearchButton.setImage(fwdSearchEnabled);
		}
		/**
		 * Disable the forward search button
		 *
		 */
		protected void disableSearchFwd(){
			canSearchFwd = false;
			fwdSearchButton.setImage(fwdSearchDisabled);
		}
		/**
		 * Enable the back button
		 *
		 */
		protected void enableBack(){
			canBack = true;
			backButton.setImage(backEnabled);
		}
		/**
		 * Disable the back button
		 *
		 */
		protected void disableBack(){
			canBack = false;
			backButton.setImage(backDisabled);
		}
		/**
		 * Enable the back search button
		 *
		 */
		protected void enableSearchBack(){
			canSearchBack = true;
			backSearchButton.setImage(backSearchEnabled);
		}
		/**
		 * Disable the back search button
		 *
		 */
		protected void disableSearchBack(){
			canSearchBack = false;
			backSearchButton.setImage(backSearchDisabled);
		}
		/**
		 * Activate the play button
		 *
		 */
		protected void activatePlay(){
			if(canPlay) playButton.setImage(playActiveEnabled);
			else playButton.setImage(playActiveDisabled);
			activePlay = true;
		}
		/**
		 * Deactivate the play button
		 *
		 */
		protected void deActivatePlay(){
			if(canPlay) playButton.setImage(playEnabled);
			else playButton.setImage(playDisabled);
			activePlay = false;
		}
		/**
		 * Activate the pause button
		 *
		 */
		protected void activatePause(){
			if(canPause) pauseButton.setImage(pauseActiveEnabled);
			else pauseButton.setImage(pauseActiveDisabled);
			activePause = true;
		}
		/**
		 * Deactivate the pause button
		 *
		 */
		protected void deActivatePause(){
			if(canPause) pauseButton.setImage(pauseEnabled);
			else pauseButton.setImage(pauseDisabled);
			activePause = false;
		}
		/**
		 * Activate the rec button
		 *
		 */
		protected void activateRec(){
			if(canRec) recButton.setImage(recActiveEnabled);
			else recButton.setImage(recActiveDisabled);
			activeRec = true;
		}
		/**
		 * Deactivate the rec button
		 *
		 */
		protected void deActivateRec(){
			if(canRec) recButton.setImage(recEnabled);
			else recButton.setImage(recDisabled);
			activeRec = false;
		}
		/**
		 * Activate the loop button
		 *
		 */
		protected void activateLoop(){
			if(canLoop) loopButton.setImage(loopActiveEnabled);
			else loopButton.setImage(loopActiveDisabled);
			activeLoop = true;
		}
		/**
		 * Deactivate the loop button
		 *
		 */
		protected void deActivateLoop(){
			if(canLoop) loopButton.setImage(loopEnabled);
			else loopButton.setImage(loopDisabled);
			activeLoop = false;
		}
		/**
		 * Disable all buttons
		 *
		 */
		protected void disableAll(){
			//disableBack();
			//disableFwd();
			disablePlay();
			disableRec();
			//disableSearchBack();
			//disableSearchFwd();
			disableStop();
			disablePause();
			disableLoop();
		}
		/**
		 * Enable all buttons
		 *
		 */
		protected void enableAll(){
			//enableBack();
			//enableFwd();
			enablePlay();
			enableRec();
			//enableSearchBack();
			//enableSearchFwd();
			enableStop();
			enablePause();
			enableLoop();
		}
		/**
		 * Deactivate all buttons
		 *
		 */
		public void deActivateAll(){
			deActivatePlay();
			deActivateLoop();
			deActivatePause();
			deActivateRec();
		}
		/**
		 * 
		 * @see com.groovemanager.sampled.AudioPlayerListener#statusChanged(int)
		 */
		public void statusChanged(int type) {
			if(type == AudioPlayer.STARTED){
				positionReader = new AudioPlayerPositionReader();
				positionReader.start();
			}
			else if(type == AudioPlayer.STOPPED){
				if(positionReader != null) positionReader.cont = false;
				positionReader = null;
			}
			getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					disableAll();
					deActivateAll();
					
					switch (player.getStatus()) {
						case AudioPlayer.READY_FOR_PLAY:
							enablePlay();
							if(player.getProvider().canLoop()) enableLoop();
							break;
						case AudioPlayer.READY_FOR_REC:
							enableRec();
							break;
						case AudioPlayer.PAUSE_PLAY:
							activatePause();
							activatePlay();
							enableStop();
							enablePause();
							break;
						case AudioPlayer.PAUSE_REC:
							activatePause();
							activateRec();
							enableStop();
							enablePause();
							break;
						case AudioPlayer.PLAYING:
							activatePlay();
							enableStop();
							enablePause();
							break;
						case AudioPlayer.RECORDING:
							activateRec();
							enableStop();
							enablePause();
							break;
						default :
							break;
					}
					if(player.getLoop()) activateLoop();
					if(player.getProvider() != null && player.getProvider().canLoop()) enableLoop();
					else disableLoop();
				}
			});
		}
		/**
		 * 
		 * @see com.groovemanager.sampled.AudioPlayerListener#loopChanged(boolean)
		 */
		public void loopChanged(final boolean loop) {
			getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					if(loop) activateLoop();
					else deActivateLoop();
				}
			});
		}
	}
	/**
	 * Open this editor
	 * @see org.eclipse.jface.window.Window#open()
	 */
	public int open() {
		readMemory = true;
		memoryReader = new Thread(){
			public void run(){
				while(readMemory){
					if(left != null) left.updateMemory(Runtime.getRuntime().totalMemory(), Runtime.getRuntime().freeMemory());
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
					}
				}
			}
		};
		memoryReader.start();
		return super.open();
	}
	/**
	 * This subclass of Thread is used to monitor the AudioPlayer status and
	 * adapt the SampleEditor´s gui elements
	 * @author Manu Robledo
	 *
	 */
	protected class AudioPlayerPositionReader extends Thread {
		/**
		 * Current position
		 */
		int pos;
		/**
		 * The Display to be used for asynchronous thread execution
		 */
		Display d;
		/**
		 * The current WaveTab
		 */
		WaveTab w;
		/**
		 * Runnable for asynchronous thread execution
		 */
		Runnable r = new Runnable() {
			public void run() {
				if(w != null) w.waveDisplay.setPosition(pos);
			}
		};
		/**
		 * Break condition
		 */
		boolean cont = true;
		
		public void run(){
			AudioFormat playerFormat = player.getRealFormat();
			left.setLineFormat(playerFormat);
			w = (WaveTab)player.getProvider();
			d = getShell().getDisplay();
			//while(player.getStatus() == AudioPlayer.PLAYING || player.getStatus() == AudioPlayer.RECORDING || player.getStatus() == AudioPlayer.PAUSE_PLAY || player.getStatus() == AudioPlayer.PAUSE_REC){
			while(cont){
				while(player.getStatus() == AudioPlayer.PAUSE_PLAY || player.getStatus() == AudioPlayer.PAUSE_REC) try{
					sleep(10);
				}
				catch(InterruptedException e){}
				pos = player.getFramePosition();
				AudioFormat format = player.getRealFormat();
				if(format != null) left.setBuffer(player.getRealBufferSize() / format.getFrameSize(), player.getBufferFillLevel() / format.getFrameSize());
				else left.setBuffer(0, 0);
				if(player.getStatus() == AudioPlayer.PLAYING || player.getStatus() == AudioPlayer.PAUSE_PLAY || player.getStatus() == AudioPlayer.READY_FOR_PLAY) d.syncExec(r);
				left.setLinePos(pos);
				try {
					sleep(10);
				} catch (InterruptedException e) {}
			}
			left.setLineFormat(null);
			left.setBuffer(0, 0);
		}
	}
	/**
	 * Create a temporary file for saving temporary audio data
	 * @param format The audio format of the file
	 * @return The temp file
	 * @throws IOException If no temp file could be created
	 */
	protected File createTempFile(AudioFormat format) throws IOException{
		File f = File.createTempFile("gmtmp_", ".wav");
		f.deleteOnExit();
		try {
			AudioManager.getDefault().getAudioFileOutputStream(f, format, AudioFileFormat.Type.WAVE, null, null, null).close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
		return f;
	}
	/**
	 * 
	 * @see com.groovemanager.actions.file.FileNewListener#newFile()
	 */
	public void newFile() {
		ChooseFormatDialog dialog = new ChooseFormatDialog(getShell(), new AudioFormat(44100, 16, 2, true, false), "Please select the Audio format for recording.", ChooseFormatDialog.CHANNELS | ChooseFormatDialog.SAMPLE_SIZE | ChooseFormatDialog.SAMPLING_RATE, new int[]{16, 24, 32}, null, null, 8);
		int result = dialog.open();
		
		AudioFormat format;
		if(result == ChooseFormatDialog.CANCEL) return;
		else format = dialog.getFormat();
		try {
			File temp = createTempFile(format);
			DynamicAudioFileWaveForm wf = new DynamicAudioFileWaveForm(temp, 64 * 1024, 25, 100);
			WaveTab wt = new WaveTab(this, tabFolder, getTabItemStyle(), getWaveStyle(), temp, wf, format, true);
			tabFolder.setSelection(new TabItem[]{wt.tabItem});
			tabActivated(wt);
			wt.tabItem.setText("* Untitled");
			zoomWaveDisplay.setSource(null);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
			errorMessage("Audio File not supported.\n" + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			errorMessage("An I/O Error occured:\n" + e.getMessage());
		}
	}
	/**
	 * Create a 1:1 copy of the given file in a temporary file
	 * @param source The file to copy
	 * @return The copied file
	 */
	protected File copyFile(File source) throws IOException, NotFinishedException{
		File tempFile = File.createTempFile("gmtmp_", ".tmp");
		tempFile.deleteOnExit();
		FileInputStream in = new FileInputStream(source);
		FileOutputStream out = new FileOutputStream(tempFile);
		SaveFileThread copier = new SaveFileThread(in, out, (int)source.length(), 1, true, 8192);
		ProgressMonitor mon = new ProgressMonitor(getShell(), copier, "Copying to temporary file", "Saving " + source + " to temporary location...");
		if(mon.start() != null) return tempFile;
		else return null;
	}
	/**
	 * 
	 * @see com.groovemanager.actions.file.FileSaveListener#fileSaved(java.io.File, java.io.File)
	 */
	public void fileSaved(File source, File f) {
		WaveTab wt = getTabForFile(source);
		
		File replaceFile = null;
		TabItem[] items = tabFolder.getItems();
		for (int i = 0; i < items.length; i++) {
			WaveTab tab = (WaveTab)items[i].getData();
			if(tab.cutList.usesFile(f)){
				if(replaceFile == null)
					try {
						replaceFile = copyFile(f);
					} catch (IOException e1) {
						e1.printStackTrace();
						errorMessage(e1.getMessage());
						return;
					} catch (NotFinishedException e1) {
						e1.printStackTrace();
						errorMessage(e1.getMessage());
						return;
					}
				tab.cutList.replaceFile(f, replaceFile);
			}
		}
		
		if(wt == null) return;
		if(source.equals(f) && ((!wt.hasBeenModified()) || wt.isNew())) return;
		AudioFileOutputStream out = null;
		try {
			Map properties = new HashMap();
			Map existingProperties = AudioManager.getProperties(wt.fileFormat);
			if(existingProperties != null) properties.putAll(existingProperties);
			int[][] slices = wt.getSlices();
			properties.put("slices", slices);
			properties.put("slice_count", new Integer(slices.length));
			
			out = AudioManager.getDefault().getAudioFileOutputStream(f, wt.fileFormat.getFormat(), wt.fileFormat.getType(), properties, null, new String[]{"slices", "RLND"});
			wt.saveFile(wt.getWholeAudioInputStream(), f, out);
			out.close();
			left.setSource(wt);
			sourceFileChanged(wt.source);
		} catch (NotReadyException e3) {
			errorMessage("Nothing to save.\n" + e3.getMessage());
			return;
		} catch (IOException e3) {
			errorMessage("An I/O Error occured.\n" + e3.getMessage());
			if(out != null)  try {
				out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return;
		} catch (NotFinishedException e) {
			e.printStackTrace();
			errorMessage("Saving aborted.\n" + e.getMessage());
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
			errorMessage("Could not save file.\n" + e.getMessage());
		}
	}
	/**
	 * 
	 * @see com.groovemanager.app.sse.AudioFileSaveListener#saveAudioFile(java.io.File, java.io.File, javax.sound.sampled.AudioFileFormat.Type, javax.sound.sampled.AudioFormat)
	 */
	public void saveAudioFile(File source, File f, Type type, AudioFormat format) {
		WaveTab wt = getTabForFile(source);
		if(wt == null) return;
		AudioFileOutputStream out = null;
		
		File replaceFile = null;
		TabItem[] items = tabFolder.getItems();
		for (int i = 0; i < items.length; i++) {
			WaveTab tab = (WaveTab)items[i].getData();
			if(tab.cutList.usesFile(f)){
				if(replaceFile == null)
					try {
						replaceFile = copyFile(f);
					} catch (IOException e1) {
						e1.printStackTrace();
						errorMessage(e1.getMessage());
						return;
					} catch (NotFinishedException e1) {
						e1.printStackTrace();
						errorMessage(e1.getMessage());
						return;
					}
				tab.cutList.replaceFile(f, replaceFile);
			}
		}
		
		Map properties = new HashMap();
		Map existingProperties = AudioManager.getProperties(wt.fileFormat);
		if(existingProperties != null) properties.putAll(existingProperties);
		int[][] slices = wt.getSlices();
		properties.put("slices", slices);
		properties.put("slice_count", new Integer(slices.length));

		try{
			out = AudioManager.getDefault().getAudioFileOutputStream(f, format, type, properties, null, new String[]{"slices", "RLND"});
			wt.saveFile(wt.getWholeAudioInputStream(), f, out);
			out.close();
			left.setSource(wt);
			sourceFileChanged(wt.source);
		} catch (NotReadyException e3) {
			errorMessage("Nothing to save.\n" + e3.getMessage());
			return;
		} catch (IOException e3) {
			e3.printStackTrace();
			errorMessage("An I/O Error occured.\n" + e3.getMessage());
			if(out != null)  try {
				out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return;
		} catch (NotFinishedException e) {
			e.printStackTrace();
			errorMessage("Saving aborted.\n" + e.getMessage());
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
			errorMessage("Could not save file.\n" + e.getMessage());
		}
	}
	/**
	 * This class represents the left bar of a SampleEditor
	 * @author Manu Robledo
	 *
	 */
	protected class LeftBar implements IPropertyChangeListener{
		/**
		 * Different InfoGroups
		 */
		protected InfoGroup groupSource, groupMixer, groupMemory;
		/**
		 * Text fields
		 */
		protected Text fileName, inputDevice, outputDevice;
		/**
		 * Labels
		 */
		protected Label fileSize, frameLength, encoding, channels,
			samplingRate, sampleSize, frameSize, frameRate, bigEndian, 
			bufferSize, linePosition, lineEncoding, lineChannels,
			lineSamplingRate, lineSampleSize, lineFrameSize, lineFrameRate,
			lineBigEndian, totalMemory;
		/**
		 * ProgressBars
		 */
		protected ProgressBar bufferFill, memoryUsage;
		/**
		 * The InfoGroupContainer containing the InfoGroups
		 */
		protected InfoGroupContainer container;
		/**
		 * Create a new LeftBar
		 * @param comp The parent composite
		 */
		protected LeftBar(Composite comp){
			createContents(comp);
		}
		/**
		 * Create the contents of the left bar
		 * @param comp The parent composite 
		 */
		protected void createContents(Composite comp){
			comp.setLayout(new FillLayout());
			container = new InfoGroupContainer(comp, "Info");
			
			// Source Info
			groupSource = new InfoGroup(container, "File Info");
			fileName = groupSource.addText("File name");
			fileSize = groupSource.addLabel("File size");
			frameLength = groupSource.addLabel("Frame length");
			encoding = groupSource.addLabel("Encoding");
			channels = groupSource.addLabel("Channels");
			samplingRate = groupSource.addLabel("Sampling rate");
			sampleSize = groupSource.addLabel("Sample size");
			frameSize = groupSource.addLabel("Frame size");
			frameRate = groupSource.addLabel("Frame rate");
			bigEndian = groupSource.addLabel("Big endian");
			
			// Mixer Info
			groupMixer = new InfoGroup(container, "Device Info");
			inputDevice = groupMixer.addText("Input device");
			outputDevice = groupMixer.addText("Output device");
			bufferSize = groupMixer.addLabel("Buffer size (samples)");
			bufferFill = groupMixer.addProgBar("Buffer fill level");
			linePosition = groupMixer.addLabel("Frame Position");
			lineEncoding = groupMixer.addLabel("Encoding");
			lineChannels = groupMixer.addLabel("Channels");
			lineSamplingRate = groupMixer.addLabel("Sampling rate");
			lineSampleSize = groupMixer.addLabel("Sample size");
			lineFrameSize = groupMixer.addLabel("Frame size");
			lineFrameRate = groupMixer.addLabel("Frame rate");
			lineBigEndian = groupMixer.addLabel("Big endian");
			
			// Memory Info
			groupMemory = new InfoGroup(container, "Memory Info");
			totalMemory = groupMemory.addLabel("Total memory");
			memoryUsage = groupMemory.addProgBar("Used memory");
			
			ConfigManager.getDefault().getPrefStore().addPropertyChangeListener(this);
			if(AudioManager.getDefault().getInMixer() != null)
				container.update(inputDevice, AudioManager.getDefault().getInMixer().getMixerInfo().getName());
			else container.update(inputDevice, "none");
			if(AudioManager.getDefault().getOutMixer() != null)
				container.update(outputDevice, AudioManager.getDefault().getOutMixer().getMixerInfo().getName());
			else container.update(outputDevice, "none");
		}
		/**
		 * Set the WaveTab that is used as source for this LeftBar
		 * @param wt The WaveTab to set
		 */
		protected void setSource(WaveTab wt){
			if(wt == null || wt.source == null){
				frameLength.setText("");
				encoding.setText("");
				channels.setText("");
				samplingRate.setText("");
				sampleSize.setText("");
				frameSize.setText("");
				frameRate.setText("");
				bigEndian.setText("");
				setFile(null);
			}
			else{
				setFile(wt.source);
				try {
					AudioInputStream in = wt.getWholeAudioInputStream();
					AudioFormat format = in.getFormat();
					long length = in.getFrameLength();
					if(length != AudioSystem.NOT_SPECIFIED) frameLength.setText("" + length);
					else frameLength.setText("Unknown");
					encoding.setText(format.getEncoding().toString());
					channels.setText("" + format.getChannels());
					samplingRate.setText("" + format.getSampleRate());
					sampleSize.setText("" + format.getSampleSizeInBits());
					frameSize.setText("" + format.getFrameSize());
					frameRate.setText("" + format.getFrameRate());
					bigEndian.setText("" + format.isBigEndian());
				} catch (NotReadyException e) {
					frameLength.setText("Unknown");
					encoding.setText("Unknown");
					channels.setText("Unknown");
					samplingRate.setText("Unknown");
					sampleSize.setText("Unknown");
					frameSize.setText("Unknown");
					frameRate.setText("Unknown");
					bigEndian.setText("Unknown");
				}
			}
		}
		/**
		 * React to changes of the output or input Mixer
		 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event) {
			if(AudioManager.getDefault().getInMixer() != null)
				container.update(inputDevice, AudioManager.getDefault().getInMixer().getMixerInfo().getName());
			else 
				container.update(inputDevice, "none");
			if(AudioManager.getDefault().getOutMixer() != null)
				container.update(outputDevice, AudioManager.getDefault().getOutMixer().getMixerInfo().getName());
			else 
				container.update(outputDevice, "none");
		}
		/**
		 * Update the memory usage to be displayed
		 * @param total Amount of total memory allocated to this VM (from
		 * <code>System.totalMemory()</code>)
		 * @param free  Amount of free memory (from
		 * <code>System.freeMemory()</code>)
		 */
		public void updateMemory(long total, long free){
			container.update(totalMemory, "" + (total / (1024 * 1024)) + "." + ((total % (1024 * 1024)) / 1024) + " MB");
			container.update(memoryUsage, 0, (int)(total / (1024 * 1024)), (int)((total - free) / (1024 * 1024)));
		}
		/**
		 * Update the buffer size and fill level to be displayed
		 * @param size The new buffer size in sample frames
		 * @param fill The new buffer fill level im sample frames
		 */
		public void setBuffer(int size, int fill){
			if(size == AudioSystem.NOT_SPECIFIED) size = 0;
			container.update(bufferSize, "" + size);
			container.update(bufferFill, 0, size, fill);
		}
		/**
		 * Update the current play position
		 * @param pos The current play position
		 */
		public void setLinePos(int pos){
			container.update(linePosition, "" + pos);
		}
		/**
		 * Update the current AudioFormat of the in- or output line
		 * @param format The current AudioFormat
		 */
		public void setLineFormat(AudioFormat format){
			if(format == null){
				container.update(lineBigEndian, "Unknown");
				container.update(lineChannels, "Unknown");
				container.update(lineEncoding, "Unknown");
				container.update(lineFrameRate, "Unknown");
				container.update(lineFrameSize, "Unknown");
				container.update(lineSampleSize, "Unknown");
				container.update(lineSamplingRate, "Unknown");
			}
			else{
				container.update(lineBigEndian, ""+format.isBigEndian());
				container.update(lineChannels, ""+format.getChannels());
				container.update(lineEncoding, format.getEncoding().toString());
				container.update(lineFrameRate, ""+format.getFrameRate());
				container.update(lineFrameSize, ""+format.getFrameSize());
				container.update(lineSampleSize, ""+format.getSampleSizeInBits());
				container.update(lineSamplingRate, ""+format.getSampleRate());
			}
		}
		/**
		 * Set the current source file
		 * @param f The file to set
		 */
		public void setFile(File f){
			if(f == null){
				fileName.setText("");
				fileSize.setText("");
			}
			else{
				fileName.setText(f.getName());
				fileSize.setText("" + f.length());
			}
		}
	}
	/**
	 * Perform an edit action of the given type
	 * @param type The type of edit action to perform
	 */
	public void editAction(int type){
		WaveTab wt = getActiveTab();
		if(wt == null) return;
		wt.editAction(type);
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.WaveDisplayListener#viewChanged(com.groovemanager.sampled.waveform.AbstractWaveFormDisplay, double, double)
	 */
	public void viewChanged(AbstractWaveFormDisplay display, double zoom, double scroll) {
		if(zoom <= 1) zoomOut.setEnabled(false);
		else zoomOut.setEnabled(true);
		
		if(listenToWaveDisplay){
			listenToZoomWaveForm = false;
			zoomWaveDisplay.setSelection(new Selection(display.getFirstData(), display.getLastData()));
			listenToZoomWaveForm = true;
		}
	}
	/**
	 * Get the WaveTab that is currently assigned as copy source, if any.
	 * @return The WaveTab, on which the last copy operation was performed or
	 * <code>null</code>
	 */
	public WaveTab getCopySource(){
		return copySource;
	}
	/**
	 * Set the WaveTab that should act as copy source, meaning that a this
	 * WaveTab will be used for a paste operation
	 * @param wt The WaveTab to set as copy source
	 */
	public void setCopySource(WaveTab wt){
		copySource = wt;
	}
	/**
	 * Remove the current copy source
	 *
	 */
	public void removeCopySource(){
		copySource = null;
	}
}