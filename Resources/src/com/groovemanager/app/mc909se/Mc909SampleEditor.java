/*
 * Created on 02.06.2004
 *
 */
package com.groovemanager.app.mc909se;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.groovemanager.actions.ConfigAction;
import com.groovemanager.app.sse.SimpleSampleEditor;
import com.groovemanager.app.sse.WaveTab;
import com.groovemanager.core.FileManager;
import com.groovemanager.exception.NotFinishedException;
import com.groovemanager.gui.custom.KeyboardKeyListener;
import com.groovemanager.gui.custom.mc909.Mc909KeyComposite;
import com.groovemanager.midi.MIDIManager;
import com.groovemanager.sampled.AudioPlayer;
import com.groovemanager.sampled.waveform.Markable;
import com.groovemanager.sampled.waveform.MarkableListener;
import com.groovemanager.sampled.waveform.Marker;
import com.groovemanager.sampled.waveform.Selection;

/**
 * This class represents the Mc909 sample editor application
 * @author Manu Robledo
 *
 */
public class Mc909SampleEditor extends SimpleSampleEditor implements KeyboardKeyListener{
	/**
	 * Create a new Mc909SampleEditor 
	 * @param shell The parent shell. May be <code>null</code> to create a
	 * top level window.
	 * @param style The window큦 style
	 * @param fileManager The FileManager to use
	 */
	public Mc909SampleEditor(Shell shell, int style, FileManager fileManager) {
		super(shell, style, fileManager);
	}
	/**
	 * Create a new Mc909SampleEditor using the default FileManager 
	 * @param shell The parent shell. May be <code>null</code> to create a
	 * top level window.
	 * @param style The window큦 style
	 */
	public Mc909SampleEditor(Shell shell, int style) {
		super(shell, style);
	}
	/**
	 * Create a new Mc909SampleEditor 
	 * @param shell The parent shell. May be <code>null</code> to create a
	 * top level window.
	 * @param fileManager The FileManager to use
	 */
	public Mc909SampleEditor(Shell shell, FileManager fileManager) {
		super(shell, fileManager);
	}
	/**
	 * Create a new Mc909SampleEditor using the default FileManager 
	 * @param shell The parent shell. May be <code>null</code> to create a
	 * top level window.
	 */
	public Mc909SampleEditor(Shell shell) {
		super(shell);
	}
	/**
	 * Create a new Mc909SampleEditor in a top level window
	 * @param fileManager The FileManager to use
	 */
	public Mc909SampleEditor(FileManager fileManager) {
		super(fileManager);
	}
	/**
	 * Create a new Mc909SampleEditor in a top level window using the default
	 * Filemanager
	 *
	 */
	public Mc909SampleEditor() {
		super();
	}
	/**
	 * 
	 * @see com.groovemanager.app.sse.SimpleSampleEditor#createLeftBarElements(org.eclipse.swt.widgets.Composite)
	 */
	protected void createLeftBarElements(Composite comp) {
		left = new Left909(comp);
	}
	/**
	 * 
	 * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Control c = super.createContents(parent);
		tabFolder.addSelectionListener(((Left909)left).sListener);
		((Left909)left).fileList.addSelectionListener(((Left909)left).sListener);
		c.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				MIDIManager.getDefault().dispose();
			}
		});
		return c;
	}
	/**
	 * @see com.groovemanager.app.sse.SimpleSampleEditor#getTitle()
	 */
	protected String getTitle() {
		return "MC-909 Sample Editor";
	}
	/**
	 * @see com.groovemanager.app.sse.SimpleSampleEditor#getImage()
	 */
	protected Image getImage() {
		Image img = new Image(getShell().getDisplay(), fileManager.getRootPath("icons/909.gif"));
		img.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND));
		return img;
	}
	/**
	 * 
	 * @see com.groovemanager.actions.file.FileOpenListener#fileOpened(java.io.File)
	 */
	public void fileOpened(File f) {
		super.fileOpened(f);
		final WaveTab wt = getTabForFile(f);
		
		if(wt == null) return;
		
		wt.getWaveDisplay().addMarkableListener(new MarkableListener() {
			public void markerAdded(Markable markable, Marker marker) {
				((Left909)left).exportRS.setEnabled(true);
			}
			public void markerRemoved(Markable markable, Marker marker) {
				((Left909)left).exportRS.setEnabled(wt.getWaveDisplay().getMarkerCount() > 0);
			}
			public void markerMoved(Markable markable, Marker marker,
					int positionBefore) {
			}
		});
		
		Properties p = ((Left909)left).add(wt);
		try {
			Mc909SampleInfo info = new Mc909SampleInfo(f);
			List fileList = ((Left909)left).fileList;
			String[] items = fileList.getItems();
			for (int i = 0; i < items.length; i++) {
				if(items[i].equals(p.nameText.getText())) fileList.setItem(i, info.name);
				fileList.select(i);
			}
			p.nameText.setText(info.name);
			p.loopMode.select(info.loopMode);
			p.timeStretchType.select(info.tsType);
			p.setBPM(info.bpm100 / (float)100);
		} catch (UnsupportedAudioFileException e1) {
		} catch (IOException e1) {
		}
		
		Event ev = new Event();
		ev.widget = tabFolder;
		SelectionEvent e = new SelectionEvent(ev);
		((Left909)left).sListener.widgetSelected(e);
	}
	/**
	 * 
	 * @see com.groovemanager.actions.file.FileCloseListener#fileClosed(java.io.File)
	 */
	public void fileClosed(File f) {
		((Left909)left).remove(getTabForFile(f));
		super.fileClosed(f);
		Event ev = new Event();
		ev.widget = tabFolder;
		SelectionEvent e = new SelectionEvent(ev);
		((Left909)left).sListener.widgetSelected(e);
	}
	/**
	 * This class overrides the SampleEditor큦 Leftbar with a new one adapted
	 * to the needs of the Mc909SampleEditor
	 * @author Manu Robledo
	 *
	 */
	class Left909 extends LeftBar{
		/**
		 * Import/Export samples button
		 */
		Button imExport,
		/**
		 * Export Rhythm set button
		 */
		exportRS;
		/**
		 * StackLayout for the Properties
		 */
		StackLayout stack;
		/**
		 * SelectionListener to be used for the file list and the TabFolder
		 */
		SelectionListener sListener;
		/**
		 * List of currently opened files in this editor
		 */
		List fileList;
		/**
		 * List of open files
		 */
		ArrayList files = new ArrayList(),
		/**
		 * List of Properties objects, each one corresponding to an open file
		 */
		properties = new ArrayList();
		/**
		 * The composite used for the Properties
		 */
		Composite propertiesComp;
		/**
		 * The Composite used for the file list
		 */
		Composite fileListComp;
		/**
		 * Contruct a new Left909
		 * @param comp The parent composite
		 */
		Left909(Composite comp) {
			super(comp);
		}
		/**
		 * 
		 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event) {
		}
		/**
		 * 
		 * @see com.groovemanager.app.sse.SimpleSampleEditor.LeftBar#setBuffer(int, int)
		 */
		public void setBuffer(int size, int fill) {
		}
		/**
		 * 
		 * @see com.groovemanager.app.sse.SimpleSampleEditor.LeftBar#setLineFormat(javax.sound.sampled.AudioFormat)
		 */
		public void setLineFormat(AudioFormat format) {
		}
		/**
		 * 
		 * @see com.groovemanager.app.sse.SimpleSampleEditor.LeftBar#setLinePos(int)
		 */
		public void setLinePos(int pos) {
		}
		/**
		 * 
		 * @see com.groovemanager.app.sse.SimpleSampleEditor.LeftBar#updateMemory(long, long)
		 */
		public void updateMemory(long total, long free) {
		}
		/**
		 * 
		 * @see com.groovemanager.app.sse.SimpleSampleEditor.LeftBar#createContents(org.eclipse.swt.widgets.Composite)
		 */
		protected void createContents(Composite comp) {
			stack = new StackLayout();
			comp.setLayout(new FormLayout());
			FormData fd;
			
			// Export Rhythm set button
			exportRS = new Button(comp, SWT.PUSH);
			exportRS.setText("Export Rhythm set");
			fd = new FormData();
			fd.left = new FormAttachment(0, 0);
			fd.height = -1;
			fd.bottom = fd.right = new FormAttachment(100, 0);
			exportRS.setLayoutData(fd);
			exportRS.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					exportRS();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			exportRS.setEnabled(false);
			
			// Import / Export button
			imExport = new Button(comp, SWT.PUSH);
			imExport.setText("Import / Export");
			fd = new FormData();
			fd.left = new FormAttachment(0, 0);
			fd.height = -1;
			fd.right = new FormAttachment(100, 0);
			fd.bottom = new FormAttachment(exportRS, 0, SWT.TOP);
			imExport.setLayoutData(fd);
			imExport.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					export();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			
			// Properties
			propertiesComp = new Composite(comp, SWT.BORDER);
			fd = new FormData();
			fd.left = new FormAttachment(0, 0);
			fd.height = -1;
			fd.right = new FormAttachment(100, 0);
			fd.bottom = new FormAttachment(imExport, 0, SWT.TOP);
			propertiesComp.setLayoutData(fd);
			propertiesComp.setLayout(stack);
			
			// File list
			fileListComp = new Composite(comp, SWT.BORDER | SWT.V_SCROLL);
			fd = new FormData();
			fd.top = fd.left = new FormAttachment(0, 0);
			fd.right = new FormAttachment(100, 0);
			fd.bottom = new FormAttachment(propertiesComp, 0);
			fileListComp.setLayoutData(fd);
			fileListComp.setLayout(new FillLayout());
			fileList = new List(fileListComp, SWT.SINGLE);
			sListener = new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					int sel;
					if(e.widget == tabFolder) sel = tabFolder.getSelectionIndex();
					else if(e.widget == fileList) sel = fileList.getSelectionIndex();
					else return;
					if(tabFolder.getSelectionIndex() != sel && tabFolder.getItemCount() > sel)
						tabFolder.setSelection(sel);
					if(fileList.getSelectionIndex() != sel && fileList.getItemCount() > sel)
						fileList.setSelection(sel);
					
					WaveTab wt = getActiveTab();
					tabActivated(wt);
					for (Iterator iter = properties.iterator(); iter.hasNext();) {
						Properties element = (Properties) iter.next();
						if(element.wt == wt){
							stack.topControl = element.comp;
							propertiesComp.layout();
						}
					}
					exportRS.setEnabled(wt != null && wt.getWaveDisplay().getMarkerCount() > 0);
					leftBar.layout();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			};
		}
		/**
		 * Create a new Properties object for a new added WaveTab
		 * @param wt The added WaveTab
		 * @return A Properties object corresponding to the WaveTab큦 source
		 * file
		 */
		Properties add(WaveTab wt){
			if(wt == null) return null;
			files.add(wt);
			final Properties p = new Properties(propertiesComp, wt);
			p.nameText.addModifyListener(new ModifyListener(){
				public void modifyText(ModifyEvent e) {
					String[] sel = fileList.getSelection();
					if(sel.length == 0) return;
					if(!sel[0].equals(p.nameText.getText())){
						int index = fileList.getSelectionIndex();
						fileList.remove(index);
						fileList.add(p.nameText.getText(), index);
						fileList.select(index);
					}
				}
			});
			fileList.add(p.nameText.getText());
			properties.add(p);
			return p;
		}
		void remove(WaveTab wt){
			if(wt == null) return;
			Properties p = (Properties)properties.get(files.indexOf(wt));
			fileList.remove(p.nameText.getText());
			p.comp.dispose();
			properties.remove(p);
			files.remove(wt);
		}
		public void setFile(File f) {
		}
		protected void setSource(WaveTab wt) {
		}
	}
	/**
	 * This class represents the special properties of MC909 sample files.
	 * Each instance of this class is connected to a sample file.
	 * @author Manu Robledo
	 *
	 */
	class Properties{
		/**
		 * Popup for user selection of the loop mode
		 */
		Combo loopMode,
		/**
		 * Popup for user selection of the timestretch type
		 */
		timeStretchType;
		/**
		 * Text fields for user input
		 */
		Text nameText, bpmText, beatsText, denomText, barsText;
		/**
		 * Main composite
		 */
		Composite comp;
		/**
		 * The WaveTab to which this instance belongs
		 */
		WaveTab wt;
		/**
		 * Current bpm value
		 */
		float bpm;
		/**
		 * Current beats value
		 */
		int beats = 4,
		/**
		 * Current denominator value
		 */
		denom = 4,
		/**
		 * Current number of bars
		 */
		bars = 1;
		/**
		 * NumberFormat to be used for formatting the bpm value
		 */
		NumberFormat nf;
		/**
		 * Possible loop mode values
		 */
		final static int FWD = 0, ONE_SHOT = 1, REV = 2, REV_ONE = 3;
		/**
		 * Create a new Properties instance
		 * @param parent The parent composite
		 * @param wt The WaveTab to which this instance should belong
		 */
		Properties(Composite parent, WaveTab wt){
			this.wt = wt;
			comp = new Composite(parent, SWT.NONE);
			GridLayout gl = new GridLayout();
			gl.numColumns = 2;
			//gl.makeColumnsEqualWidth = true;
			comp.setLayout(gl);
			GridData gd;
			
			// Name
			new Label(comp, SWT.NONE).setText("Name:");
			nameText = new Text(comp, SWT.BORDER);
			nameText.setTextLimit(16);
			nameText.setText(FileManager.getNameWithoutExtension(wt.getSource()));
			gd = new GridData(GridData.FILL_HORIZONTAL);
			nameText.setLayoutData(gd);
			
			// LoopMode
			new Label(comp, SWT.NONE).setText("Loop Mode:");
			loopMode = new Combo(comp, SWT.READ_ONLY | SWT.DROP_DOWN);
			loopMode.add("FWD");
			loopMode.add("ONE-SHOT");
			loopMode.add("REV");
			loopMode.add("REV-ONE");
			loopMode.select(1);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			loopMode.setLayoutData(gd);
			
			// TimeStretchType
			new Label(comp, SWT.NONE).setText("TS Type:");
			timeStretchType = new Combo(comp, SWT.READ_ONLY | SWT.DROP_DOWN);
			for(int i = 1; i < 10; i++){
				timeStretchType.add("TYPE0" + i);
			}
			timeStretchType.add("TYPE10");
			timeStretchType.select(9);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			timeStretchType.setLayoutData(gd);
			
			// Bars
			new Label(comp, SWT.NONE).setText("Bars:");
			barsText = new Text(comp, SWT.BORDER | SWT.RIGHT);
			barsText.setText("1");
			barsText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			barsText.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent e) {
				}
				public void focusLost(FocusEvent e) {
					try{
						bars = Integer.parseInt(barsText.getText());
					}
					catch(NumberFormatException ex){
					}
					barsText.setText("" + bars);
				}
			});
			
			// Beats
			new Label(comp, SWT.NONE).setText("Time signature:");
			Composite sign = new Composite(comp, SWT.NONE);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			sign.setLayoutData(gd);
			sign.setLayout(new FillLayout());
						
			beatsText = new Text(sign, SWT.BORDER | SWT.RIGHT);
			beatsText.setText("4");
			beatsText.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent e) {
				}
				public void focusLost(FocusEvent e) {
					try{
						beats = Integer.parseInt(beatsText.getText());
					}
					catch(NumberFormatException ex){
					}
					beatsText.setText("" + beats);
				}
			});
			new Label(sign, SWT.CENTER).setText("/");
			denomText = new Text(sign, SWT.BORDER | SWT.RIGHT);
			denomText.setText("4");
			denomText.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent e) {
				}
				public void focusLost(FocusEvent e) {
					try{
						denom = Integer.parseInt(denomText.getText());
					}
					catch(NumberFormatException ex){
					}
					denomText.setText("" + denom);
				}
			});
			
			// BPM
			AudioFormat format = wt.getFormat();
			if(format == null) bpm = 138;
			else bpm = Math.max(Math.min(format.getSampleRate() * 60 / (float)wt.getTotalLength() * beats, 300), 5);
			new Label(comp, SWT.NONE).setText("BPM:");
			bpmText = new Text(comp, SWT.BORDER | SWT.RIGHT);
			nf = NumberFormat.getInstance();
			nf.setMaximumIntegerDigits(3);
			nf.setMinimumFractionDigits(2);
			nf.setMaximumFractionDigits(2);
			bpmText.setText(nf.format(bpm));
			bpmText.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent e) {
				}
				public void focusLost(FocusEvent e) {
					try{
						bpm = Float.parseFloat(bpmText.getText());
					}
					catch(NumberFormatException ex){
					}
					bpm = Math.max(Math.min(bpm, 300), 5);
					bpmText.setText(nf.format(bpm));
				}
			});
			gd = new GridData(GridData.FILL_HORIZONTAL);
			bpmText.setLayoutData(gd);
			
			// Calc
			Button calc = new Button(comp, SWT.PUSH);
			calc.setText("Calc BPM from selection");
			calc.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					AudioFormat format = Properties.this.wt.getFormat();
					if(format == null) bpm = 138;
					Selection sel = Properties.this.wt.getWaveDisplay().getSelection();
					int l;
					if(sel.getLeft() == sel.getRight()) l = Properties.this.wt.getTotalLength();
					else l = sel.getRight() - sel.getLeft();
					bpm = Math.max(Math.min(format.getSampleRate() * 60 / (float)l * beats * bars, 300), 5);
					bpmText.setText(nf.format(bpm));
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			calc.setLayoutData(gd);
		}
		/**
		 * Set the current bpm value
		 * @param bpm The bpm value to set
		 */
		void setBPM(float bpm){
			this.bpm = bpm;
			bpmText.setText(nf.format(bpm));
		}
	}
	/**
	 * 
	 * @see com.groovemanager.actions.file.FileNewListener#newFile()
	 */
	public void newFile() {
		super.newFile();
		WaveTab wt = getActiveTab();
		if(wt == null) return;
		
		((Left909)left).add(wt);
		Event ev = new Event();
		ev.widget = tabFolder;
		SelectionEvent e = new SelectionEvent(ev);
		((Left909)left).sListener.widgetSelected(e);
	}
	/**
	 * Get the Properties instance corresponding to the active WaveTab or
	 * <code>null</code>, if no WaveTab is currently active
	 * @return The Properties instance for the currently active WaveTab
	 */
	Properties getActiveProperties(){
		WaveTab wt = getActiveTab();
		for (Iterator iter = ((Left909)left).properties.iterator(); iter.hasNext();) {
			Properties p = (Properties) iter.next();
			if(p.wt == wt) return p;
		}
		return null;
	}
	/**
	 * Open the dialog for importing or exporting samples from/to the Mc909
	 *
	 */
	void export(){
		ArrayList l = ((Left909)left).properties;
		Properties[] p = new Properties[l.size()];
		int i = 0;
		for (Iterator iter = l.iterator(); iter.hasNext(); i++) {
			Properties element = (Properties) iter.next();
			p[i] = element;
		}
		Export909SamplesDialog dialog = new Export909SamplesDialog(getShell(), p, this);
		dialog.open();
	}
	/**
	 * Open the wizard for exporting a Rhythm set to the Mc909
	 *
	 */
	void exportRS(){
		WaveTab wt = getActiveTab();
		if(wt == null) return;
		Properties p = getActiveProperties();
		if(p == null) return;
		WizardDialog dialog = new ExportRhythmSetDialog(getShell(), p, wt);
		dialog.open();
	}
	/**
	 * 
	 * @see com.groovemanager.app.sse.SimpleSampleEditor#createControlBarElements(org.eclipse.swt.widgets.Composite)
	 */
	protected void createControlBarElements(Composite comp) {
		comp.setLayout(new FormLayout());
		comp.setBackground(comp.getDisplay().getSystemColor(SWT.COLOR_BLACK));

		Composite top = new Composite(comp, SWT.NONE);
		super.createControlBarElements(top);
		FormData fd;
		fd = new FormData();
		fd.left = fd.top = new FormAttachment(0, 0);
		fd.right = new FormAttachment(100, 0);
		top.setLayoutData(fd);
		
		Composite bottom = new Composite(comp, SWT.NONE);
		Mc909KeyComposite w = new Mc909KeyComposite(bottom, 59, 74);
		w.setNotesVisible(false);
		w.addKeyListener(this);
		w.setReceiveMidi(true);
		
		fd = new FormData();
		int wi = bottom.computeSize(SWT.DEFAULT, SWT.DEFAULT).x / 2;
		fd.left = new FormAttachment(50, -wi);
		fd.top = new FormAttachment(top, 0, SWT.BOTTOM);
		bottom.setLayoutData(fd);
	}
	/**
	 * @see com.groovemanager.gui.custom.KeyboardKeyListener#keyPressed(int)
	 */
	public void keyPressed(int key) {
		WaveTab wt = getActiveTab();
		if(wt == null) return;
		Marker marker = wt.getWaveDisplay().getMarker(key - 59);
		if(marker == null) return;
		int startpos = marker.getPosition();
		Marker nextMarker = wt.getWaveDisplay().getMarker(key - 59 + 1);
		int endpos;
		if(nextMarker == null) endpos = wt.getTotalLength();
		else endpos = nextMarker.getPosition();
		wt.getWaveDisplay().setSelection(new Selection(startpos, endpos));
		if(player.getStatus() == AudioPlayer.PLAYING) transportStop();
		if(player.getStatus() == AudioPlayer.READY_FOR_PLAY){
			transportPlay();
		}
	}
	/**
	 * @see com.groovemanager.gui.custom.KeyboardKeyListener#keyReleased(int)
	 */
	public void keyReleased(int key) {
	/*
		if(player.getStatus() == AudioPlayer.PLAYING && currentKey == key){
			transportStop();
			currentKey = -1;
		}
	*/
	}
	/**
	 * @see com.groovemanager.app.sse.SimpleSampleEditor#createConfigAction()
	 */
	protected ConfigAction createConfigAction() {
		return new ConfigAction("&Settings\tAlt+S", new String[]{"audio", "midi"});
	}
	/**
	 * @see com.groovemanager.app.sse.SimpleSampleEditor#getSashFormWeights()
	 */
	protected int[] getSashFormWeights() {
		return new int[]{180, 620};
	}
	/**
	 * Import a Sample from the MC909. The sample will be copied to a temporary
	 * file and then opened.
	 * @param sourceSample The sample file to import
	 * @throws IOException If an I/O error occurs
	 * @throws NotFinishedException If the Copy process did not finish
	 */
	void importFile(File sourceSample) throws IOException, NotFinishedException{
		File temp = copyFile(sourceSample);
		fileOpened(temp);
	}
}