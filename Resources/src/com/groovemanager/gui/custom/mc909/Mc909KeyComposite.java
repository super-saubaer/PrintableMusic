package com.groovemanager.gui.custom.mc909;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.groovemanager.gui.custom.CustomComposite;
import com.groovemanager.gui.custom.KeyboardKeyListener;
import com.groovemanager.midi.MIDIListener;
import com.groovemanager.midi.MIDIManager;
/**
 * A Mc909KeyComposite contains a number of Mc909Keys aligned as on the MC-909
 * itself. It can send and receive MIDI note on/off events
 * @author Manu Robledo
 *
 */
public class Mc909KeyComposite extends CustomComposite implements MIDIListener, KeyboardKeyListener{
	/**
	 * The MIDIManager used for sending and receiving MIDI events
	 */
	private MIDIManager midiManager;
	/**
	 * The Mc909Keys contained in this Composite
	 */
	protected Mc909Key[] keys;
	/**
	 * Indicates whether this Composite should send MIDI events when the keys
	 * are being pressed or not
	 */
	private boolean sendMIDI;
	/**
	 * Note value of the first key to be displayed in this composite from 0
	 * to 127
	 */
	private final int firstKey;
	/**
	 * The channel to send the MIDI data to
	 */
	private int channel = 0;
	/**
	 * The velocity used for sending MIDI note on events 
	 */
	private int velocity = 100;
	/**
	 * Note value of the last key to be displayed in this composite from 0
	 * to 127
	 */
	private final int lastKey;
	/**
	 * Constant for the width of one key in pixels
	 */
	protected final static int KEY_WIDTH = 60,
	/**
	 * Constant for the height of one key in pixels
	 */
	KEY_HEIGHT = 90;
	/**
	 * List of KeyboardKeyListeners registered with this Composite
	 */
	private ArrayList listeners = new ArrayList();
	/**
	 * Last received ShortMessage
	 */
	private ShortMessage sm;
	/**
	 * Indicates whether this Composite should react to incoming MIDI messages
	 * or not
	 */
	private boolean receiveMIDI;
	/**
	 * The note on ids from the last sent note on events for each key
	 */
	private long[] noteOnIds;
	/**
	 * Array of MIDI message types accepted
	 */
	private final static int[] ACCEPT_TYPES = new int[]{ShortMessage.NOTE_ON, ShortMessage.NOTE_OFF};
	/**
	 * Runnable for thread-safe reaction to incoming MIDI events
	 */
	private Runnable receiver = new Runnable(){
		public void run(){
			if(sm == null) return;
			if(sm.getCommand() == ShortMessage.NOTE_ON){
				int index = sm.getData1() - firstKey;
				if(index >= keys.length || index < 0) return;
				keys[index].getPad().press();
			}
			else if(sm.getCommand() == ShortMessage.NOTE_OFF){
				int index = sm.getData1() - firstKey;
				if(index >= keys.length || index < 0) return;
				keys[index].getPad().release();
			}
		}
	};
	/**
	 * Internal method for creating the Mc909Keys
	 * @param parent The parent Composite
	 * @param firstKey Note value of the lowest key from 0 to 127
	 * @param lastKey Note value of the highest key from 0 to 127
	 * @return The created Mc909Keys
	 */
	protected Mc909Key[] createKeys(Composite parent, int firstKey, int lastKey){
		Mc909Key[] keys = new Mc909Key[lastKey - firstKey + 1];
		RowData rd;
		for(int j = 0; j < keys.length; j++){
			rd = new RowData();
			rd.width = KEY_WIDTH;
			rd.height = KEY_HEIGHT;
			int note = (firstKey + j + 1) % 12;
			int type;
			switch (note) {
				case 1 :
				case 6 :
					type = Mc909Key.WHITE_BEGIN;
					break;
				case 2 :
				case 7 :
					type = Mc909Key.BLACK_LEFT;
					break;
				case 3 :
				case 8 :
				case 10 :
					type = Mc909Key.WHITE_MIDDLE;
					break;
				case 4 :
				case 11 :
					type = Mc909Key.BLACK_RIGHT;
					break;
				case 0 :
				case 5 :
					type = Mc909Key.WHITE_END;
					break;
				default :
					type = Mc909Key.BLACK_MIDDLE;
					break;
			}
			keys[j] = new Mc909Key(parent, type, j + firstKey);
			keys[j].getComposite().setLayoutData(rd);
			keys[j].getPad().addKeyListener(this);
			
			noteOnIds = new long[keys.length];
			Arrays.fill(noteOnIds, -1);
		}
		return keys;
	}
	/**
	 * Internal method for creating the top method which contains the pad
	 * numbers for the keys 59 to 74
	 * @param parent The parent composite
	 * @return The created top composite
	 */
	protected Composite createTopComp(Composite parent){
		Composite comp = new Composite(parent, SWT.NONE);
		
		GridLayout gl = new GridLayout(4, true);
		gl.horizontalSpacing = 4;
		gl.marginHeight = 2;
		gl.marginWidth = 2;
		comp.setLayout(gl);
		
		GridData gd;
		NumCanvas[] nums = new NumCanvas[4];
		for(int i = 0; i < nums.length; i++){
			gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			nums[i] = new NumCanvas(comp, 1 + 4 * (i % 4));
			nums[i].getComposite().setLayoutData(gd);
		}
		
		return comp;
	}
	/**
	 * Construct a new Mc909KeyComposite using the specified MIDIManager
	 * @param parent The parent composite
	 * @param lowKey The note value of the lowest key to display from 0 to 127
	 * @param highKey The note value of the highest key to display from 0 to 127
	 * @param manager The MIDIManager to use for in- and output
	 */
	public Mc909KeyComposite(Composite parent, int lowKey, int highKey, MIDIManager manager) {
		super(parent);
		midiManager = manager;
		
		firstKey = lowKey;
		lastKey = highKey;

		Composite topComp = createTopComp(getComposite());
		FormData fd = new FormData();
		fd.top = new FormAttachment(0, 0);
		fd.height = 20;
		fd.width = 16 * KEY_WIDTH;
		fd.left = new FormAttachment(0, KEY_WIDTH * 59 - KEY_WIDTH * firstKey);
		topComp.setLayoutData(fd);
		
		Composite padComp = createPadComp(getComposite());
		fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.top = new FormAttachment(topComp, 0, SWT.BOTTOM);
		padComp.setLayoutData(fd);
		
		keys = createKeys(padComp, firstKey, lastKey);
		getComposite().pack();
	}
	/**
	 * Construct a new Mc909KeyComposite using the default MIDIManager
	 * @param parent The parent composite
	 * @param lowKey The note value of the lowest key to display from 0 to 127
	 * @param highKey The note value of the highest key to display from 0 to 127
	 */
	public Mc909KeyComposite(Composite parent, int lowKey, int highKey){
		this(parent, lowKey, highKey, MIDIManager.getDefault());
	}
	/**
	 * Set the visibility of the note name on all keys
	 * @param visible true, if the note name and octave should be displayed on
	 * each key, false otherwise
	 */
	public void setNotesVisible(boolean visible){
		for(int i = 0; i < keys.length; i++){
			keys[i].setNoteVisible(visible);
		}
	}
	/**
	 * Set the hold behaviour for all keys
	 * @param hold The new hold behaviour
	 */
	public void setHold(boolean hold){
		for(int i = 0; i < keys.length; i++){
			keys[i].getPad().setHold(hold);
		}
	}
	/**
	 * 
	 * @see com.groovemanager.midi.MIDIListener#processMessage(javax.sound.midi.MidiMessage)
	 */
	public synchronized void processMessage(MidiMessage m) {
		if(!(m instanceof ShortMessage)) return;
		sm = (ShortMessage)m;
		getComposite().getDisplay().syncExec(receiver);
	}
	/**
	 * Set, if this composite should react to incoming MIDI note on/off events
	 * @param receive true, if incoming MIDI events should be handled, false
	 * otherwise
	 */
	public void setReceiveMidi(boolean receive){
		if(receiveMIDI == receive) return;
		if(receive) midiManager.addMIDIListener(this, ACCEPT_TYPES);
		else midiManager.removeMIDIListener(this);
	}
	/**
	 * Add a KeyboardKeyListener that will be notified when one this composite´s
	 * keys is pressed.
	 * @param listener The KeyboardKeyListener to add
	 */
	public void addKeyListener(KeyboardKeyListener listener){
		listeners.add(listener);
	}
	/**
	 * Remove a registered KeyboardKeyListener from this composite
	 * @param listener The KeyboardKeyListener to remove
	 */
	public void removeKeyListener(KeyboardKeyListener listener){
		listeners.remove(listener);
	}
	/**
	 * 
	 * @see com.groovemanager.gui.custom.KeyboardKeyListener#keyPressed(int)
	 */
	public void keyPressed(int key) {
		if(sendMIDI){
			int index = key - firstKey;
			if(index >= 0 && index < noteOnIds.length)
				try {
					noteOnIds[index] = midiManager.sendSingleNoteOn(channel, velocity, key);
				} catch (InvalidMidiDataException e) {
					e.printStackTrace();
				} catch (MidiUnavailableException e) {
					e.printStackTrace();
				}
		}
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			KeyboardKeyListener listener = (KeyboardKeyListener) iter.next();
			listener.keyPressed(key);
		}
	}
	/**
	 * 
	 * @see com.groovemanager.gui.custom.KeyboardKeyListener#keyReleased(int)
	 */
	public void keyReleased(int key) {
		if(sendMIDI){
			int index = key - firstKey;
			if(index >= 0 && index < noteOnIds.length)
				try {
					midiManager.sendSingleNoteOff(noteOnIds[index], channel, key);
				} catch (InvalidMidiDataException e) {
					e.printStackTrace();
				} catch (MidiUnavailableException e) {
					e.printStackTrace();
				}
			noteOnIds[index] = -1;
		}
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			KeyboardKeyListener listener = (KeyboardKeyListener) iter.next();
			listener.keyReleased(key);
		}
	}
	/**
	 * Set, if this compopsite´s keys should send MIDI events when pressed or
	 * released
	 * @param send true, if the keys should send MIDI, false otherwise
	 */
	public void  setSendMIDI(boolean send){
		sendMIDI = send;
	}
	/**
	 * Set the velocity for sending MIDI notes
	 * @param vel The new velocity form 0 to 127
	 */
	public void setVelocity(int vel){
		velocity = Math.max(0, Math.min(127, vel));
	}

	/**
	 * @see com.groovemanager.gui.custom.CustomComposite#createComposite(org.eclipse.swt.widgets.Composite, int)
	 */
	protected Composite createComposite(Composite parent, int style) {
		Composite comp = new Composite(parent, style);
		comp.setLayout(new FormLayout());
		
		comp.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				midiManager.removeMIDIListener(Mc909KeyComposite.this);
			}
		});

		return comp;
	}
	/**
	 * Internal method for creating the composite containing the keys
	 */
	protected Composite createPadComp(Composite parent){
		Composite padComp = new Composite(parent, SWT.NONE);
		
		RowLayout rl = new RowLayout();
		rl.marginBottom = 0;
		rl.marginLeft = 0;
		rl.marginRight = 0;
		rl.marginTop = 0;
		rl.spacing = 0;
		rl.wrap = false;
		padComp.setLayout(rl);
		
		return padComp;
	}

	/**
	 * @see com.groovemanager.gui.custom.CustomComposite#getPossibleStyles()
	 */
	protected int getPossibleStyles() {
		return SWT.NONE;
	}

	/**
	 * @see com.groovemanager.gui.custom.CustomComposite#getListenerTypes()
	 */
	protected int[] getListenerTypes() {
		return new int[0];
	}
	/**
	 * Get the channel used for sending MIDI events
	 * @return The currently used channel between 0 and 15
	 */
	public int getChannel(){
		return channel;
	}
	/**
	 * Set the channel used for sending MIDI events
	 * @param channel The channel to send MIDI messages on between 0 and 15
	 */
	public void setChannel(int channel){
		this.channel = channel;
	}
	/**
	 * Gete the velocity value used for sending MIDI notes
	 * @return The velocity from 0 to 127
	 */
	public int getVelocity(){
		return velocity;
	}
	/**
	 * This class is used drawing the pad numbers above the Mc909Keys 
	 * @author Manu Robledo
	 *
	 */
	private class NumCanvas extends CustomComposite{
		/**
		 * The labels used for displaying the pad numbers
		 */
		private Label[] labels;
		/**
		 * Construct a new NumCanvas
		 * @param parent
		 * @param first
		 */
		NumCanvas(Composite parent, int first){
			super(parent);
			for(int i = 0; i < 4; i++){
				labels[i].setText("" + (first + i));
			}
		}
		/**
		 * @see com.groovemanager.gui.custom.CustomComposite#createComposite(org.eclipse.swt.widgets.Composite, int)
		 */
		protected Composite createComposite(Composite parent, int style) {
			labels = new Label[4];
			Canvas c = new Canvas(parent, style);
			
			GridLayout gl = new GridLayout();
			gl.numColumns = 4;
			gl.marginHeight = 1;
			gl.marginWidth = 6;
			gl.makeColumnsEqualWidth = true;
			c.setLayout(gl);
			c.addPaintListener(new PaintListener(){
				public void paintControl(PaintEvent event) {
					Display display = event.display;
					GC gc = event.gc;
					gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
					Rectangle rect = getComposite().getClientArea();
					gc.fillRoundRectangle(rect.x, rect.y, rect.width, rect.height, 15, 15);
				}
			});
			
			GridData gd;
			for(int i = 0; i < 4; i++){
				gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
				labels[i] = new Label(c, SWT.CENTER);
				labels[i].setLayoutData(gd);
				labels[i].setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
				labels[i].setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
			}
			return c;
		}
		/**
		 * @see com.groovemanager.gui.custom.CustomComposite#getPossibleStyles()
		 */
		protected int getPossibleStyles() {
			return SWT.NONE;
		}
		/**
		 * @see com.groovemanager.gui.custom.CustomComposite#getListenerTypes()
		 */
		protected int[] getListenerTypes() {
			return new int[0];
		}
	}
}
