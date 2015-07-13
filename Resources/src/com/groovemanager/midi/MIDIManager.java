package com.groovemanager.midi;

import java.util.HashMap;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import com.groovemanager.core.ConfigManager;

/**
 * A MIDIManager can be used as a central instance for controlling MIDI data 
 * flow. A MIDIManager is connected to a ConfigManager instance from which it
 * retreives the in- and output device to use.
 * @author Manu
 *
 */
public class MIDIManager implements IPropertyChangeListener{
	/**
	 * The default MIDIManager implementaiton
	 */
	public final static MIDIManager DEFAULT_MIDI_MANAGER = new MIDIManager();
	/**
	 * The current output device
	 */
	protected MidiDevice outDevice = null,
	/**
	 * The current input device
	 */
	inDevice = null;
	/**
	 * increasing number used for creating event ids that can be assigned to
	 * note on events so that a note off event can be assigned to a previously
	 * sent note on event
	 */
	protected long eventid = 0;
	/**
	 * set of events that have been created with an event id and have not yet
	 * been finished.
	 */
	private HashMap events = new HashMap();
	/**
	 * The MIDI In Receiver used by this MIDIManager
	 */
	protected final MIDIInReceiver inReceiver;
	/**
	 * The ConfigManager used to retreive selected in- and output devices
	 */
	private final ConfigManager configManager;
	/**
	 * Create and return the MIDIInReceiver. The default implementation creates
	 * a MIDIInReceiver that listens to all types of events. Subclasses may
	 * override this method.
	 * @return The new created MIDIInReceiver
	 */
	protected MIDIInReceiver createInReceiver(){
		return new MIDIInReceiver(this);
	}
	/**
	 * Get the default MIDIManager implemetation
	 * @return The default MIDIManager implementation
	 */
	public static MIDIManager getDefault(){
		return DEFAULT_MIDI_MANAGER;
	}
	/**
	 * Construct a new MIDIManager instance using the specified ConfigManager
	 * @param configManager The ConfigManager to use
	 */
	public MIDIManager(ConfigManager configManager){
		this.configManager = configManager;
		inReceiver = createInReceiver();
		initMidi();
		configManager.getPrefStore().addPropertyChangeListener(this);
	}
	/**
	 * Construct a new MIDIManager using the default ConfigManager 
	 *
	 */
	public MIDIManager(){
		this(ConfigManager.getDefault());
	}
	/**
	 * Get a new unique even id 
	 * @return A new unique event id
	 */
	protected long getNewEventId(){
		if(eventid == -1) eventid ++;
		if(events.get("" + eventid) != null){
			eventid++;
			return getNewEventId();
		}
		return eventid++;
	}
	/**
	 * Internal method for starting an event that expects another event in
	 * the future for completition (like note on and note off). the event id
	 * will be remembered until the expected event is sent. 
	 * @param evid The event id
	 */
	protected synchronized void startEvent(long evid){
		events.put(new Long(evid), new Long(System.currentTimeMillis()));
	}
	/**
	 * Internal method indicating that the corresponding event to the one that
	 * created the given event id has been sent. If after this call no more
	 * events are pending to be completed by another event, the output device
	 * will be closed.
	 * @param evid
	 */
	protected synchronized void stopEvent(long evid){
		events.remove(new Long(evid));
		if(events.isEmpty()){
			outDevice.close();
		}
	}
	/**
	 * Send a MidiMessage to the output device
	 * @param message The MidiMessage to send
	 * @param evid The event id of the message. If this message expects another
	 * message in the future for completing, this event id must be other than
	 * -1. Or if this message is the expected follow-up message to a previous
	 * one it the event id should contain the event id with which the previous
	 * message was sent. Otherwise -1 should be used as event id.
	 * @param start Inidcates whether this event is a start event that expects
	 * a follow-up message (true) or if it is the follow-up message to a
	 * previous message (false). This value is only recognized if eventid != -1.
	 * @throws MidiUnavailableException If the message could not be sent
	 */
	protected synchronized void sendMidiMessage(MidiMessage message, long evid, boolean start) throws MidiUnavailableException{
		MidiDevice dev = null;
		try {
			dev = getOutDevice();
			if(!dev.isOpen()) dev.open();
			dev.getReceiver().send(message, -1);
			if(evid != -1){
				if(start) startEvent(evid);
				else stopEvent(evid);
			}
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
			if(dev != null) dev.close();
			throw e;
		}
	}
	/**
	 * Send a single note on event
	 * @param channel The channel on which the event should be sent (between
	 * 0 and 15)
	 * @param velocity The velocity of the note on event (between 0 and 127)
	 * @param key The note value of the note on event (between 0 and 127)
	 * @return A unique event id that should be used, when a corresponding note
	 * off event is sent
	 * @throws InvalidMidiDataException Should not happen
	 * @throws MidiUnavailableException If the message could not be sent
	 */
	public long sendSingleNoteOn(int channel, int velocity, int key) throws InvalidMidiDataException, MidiUnavailableException{
		velocity = Math.max(Math.min(velocity, 127), 0);
		key = Math.max(Math.min(key, 127), 0);
		channel = Math.max(Math.min(channel, 15), 0);
		
		ShortMessage message = new ShortMessage();
		message.setMessage(ShortMessage.NOTE_ON, channel, key, velocity);
		
		long eventId = getNewEventId();
		sendMidiMessage(message, eventId, true);
		return eventId;
	}
	/**
	 * Send a single note off event
	 * @param eventid The event id of the note on event to which this note event
	 * belongs. If this event is independent of any note on event, -1 should be
	 * used.
	 * @param channel The channel on which the event should be sent (between
	 * 0 and 15)
	 * @param key The note value of the note on event (between 0 and 127)
	 * @throws InvalidMidiDataException Should not happen
	 * @throws MidiUnavailableException If the message could not be sent
	 */
	public void sendSingleNoteOff(long eventid, int channel, int key) throws InvalidMidiDataException, MidiUnavailableException{
		key = Math.max(Math.min(key, 127), 0);
		channel = Math.max(Math.min(channel, 15), 0);
		
		ShortMessage message = new ShortMessage();
		message.setMessage(ShortMessage.NOTE_OFF, channel, key, 0);
		
		sendMidiMessage(message, eventid, false);
	}
	/**
	 * Send a single controller event
	 * @param channel The channel on which the controller should be sent
	 * @param controllerNr The controller number from 0 to 127
	 * @param value The controller value from 0 to 127
	 * @throws InvalidMidiDataException Should not happen
	 * @throws MidiUnavailableException If the message could not be sent
	 */
	public void sendController(int channel, byte controllerNr, byte value) throws InvalidMidiDataException, MidiUnavailableException{
		channel = Math.max(Math.min(channel, 15), 0);
		if(value < 0) value = 0;

		ShortMessage message = new ShortMessage();
		message.setMessage(ShortMessage.CONTROL_CHANGE, channel, controllerNr, value);
		
		sendMidiMessage(message, -1, true);
	}
	/**
	 * Send a single program change event
	 * @param channel The channel on which the event should be sent (from 0
	 * to 15)
	 * @param program The program number to change to (from 0 to 127) 
	 * @throws InvalidMidiDataException Should not happen
	 * @throws MidiUnavailableException If the message could not be sent
	 */
	public void sendProgramChange(int channel, byte program) throws InvalidMidiDataException, MidiUnavailableException{
		channel = Math.max(Math.min(channel, 15), 0);
		if(program < 0) program = 0;

		ShortMessage message = new ShortMessage();
		message.setMessage(ShortMessage.PROGRAM_CHANGE, channel, program, 0);
		
		sendMidiMessage(message, -1, true);
	}
	/**
	 * Send a single SysEx message
	 * @param data The whole message data of the sysex message
	 * @throws InvalidMidiDataException If the provided data is not a valid MIDI
	 * data
	 * @throws MidiUnavailableException If the message could not be sent
	 */
	public void sendSysEx(int[] data) throws InvalidMidiDataException, MidiUnavailableException{
		byte[] b = new byte[data.length];
		for (int i = 0; i < b.length; i++) {
			b[i] = (byte)(data[i] & 0xFF);
		}
		SysexMessage message = new SysexMessage();
		message.setMessage(b, b.length);
		
		sendMidiMessage(message, -1, true);
	}
	/**
	 * Get the current output device
	 * @return The current output device
	 * @throws MidiUnavailableException If the device could not be found or if
	 * no device is selected
	 */
	public MidiDevice getOutDevice() throws MidiUnavailableException{
		if(outDevice != null) return outDevice;
		
		String port = configManager.getPrefStore().getString("midi_out");
		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
		MidiDevice.Info info = null;
		for(int i = 0; i < infos.length; i++){
			if(infos[i].getName().equals(port)) info = infos[i];
		}
		if(info == null) throw new MidiUnavailableException("Midi Device not found. Check settings.");
		outDevice = MidiSystem.getMidiDevice(info);
		return outDevice;
	}
	/**
	 * Get the current input device
	 * @return The current input device
	 * @throws MidiUnavailableException If the device could not be found or if
	 * no device is selected
	 */
	protected MidiDevice getInDevice() throws MidiUnavailableException{
		if(inDevice != null) return inDevice;
		
		String port = configManager.getPrefStore().getString("midi_in");
		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
		MidiDevice.Info info = null;
		for(int i = 0; i < infos.length; i++){
			if(infos[i].getName().equals(port)) info = infos[i];
		}
		if(info == null) throw new MidiUnavailableException("Midi Device not found. Check settings.");
		inDevice = MidiSystem.getMidiDevice(info);
		return inDevice;
	}
	/**
	 * 
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if(event.getProperty().equals("midi_in")) initMidiIn();
		else if(event.getProperty().equals("midi_out")) initMidiOut();
	}
	/**
	 * Initialize the output device 
	 *
	 */
	private void initMidiOut(){
		if(outDevice != null && outDevice.isOpen()) outDevice.close();
		outDevice = null;
		try {
			outDevice = getOutDevice();
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Initialize the input device
	 *
	 */
	private void initMidiIn(){
		boolean connected = false;
		if(inDevice != null && inDevice.isOpen()){
			try {
				if(inDevice.getTransmitter().getReceiver() == inReceiver){
					connected = true;
					inDevice.getTransmitter().setReceiver(null);
				}
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
			}
			inDevice.close();
		}
		inDevice = null;
		try {
			inDevice = getInDevice();
			if(connected){
				inDevice.open();
				inDevice.getTransmitter().setReceiver(inReceiver);
			}
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Mark this MIDIManager as out of use
	 *
	 */
	public void dispose(){
		if(inDevice != null){
			inDevice.close();
			inDevice = null;
		}
		if(outDevice != null){
			outDevice.close();
			outDevice = null;
		}
	}
	/**
	 * Initialize input and output device
	 *
	 */
	public void initMidi(){
		initMidiIn();
		initMidiOut();
	}
	/**
	 * This method is called from a MIDIInReceiver to indicate that he needs
	 * to be connected to the input device
	 *
	 */
	void enableInListener(){
		MidiDevice in = null;
		try {
			in = getInDevice();
			in.open();
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
			return;
		}
		try {
			in.getTransmitter().setReceiver(inReceiver);
		} catch (MidiUnavailableException e1) {
			e1.printStackTrace();
			if(in.isOpen()) in.close();
		}
	}
	/**
	 * This method is called from a MIDIInReceiver to indicate that he can
	 * be disconnected from the input device
	 *
	 */
	void disableInListener(){
		if(inDevice != null){
			try {
				if(inDevice.getTransmitter().getReceiver() == inReceiver) inDevice.getTransmitter().setReceiver(null);
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
			}
			inDevice.close();
		}
	}
	/**
	 * Add a MIDIListener to this MIDIManager. The listener will be notified of
	 * all incoming MIDIEvents on the MIDIManager´s input device that match
	 * the geiven accepted message types
	 * @param listener The MIDIListener to add 
	 * @param acceptedStatus Array of status bytes this listener accepts. Can be
	 * any of <code>MetaMessage.META</code>,
	 * <code>SysexMessage.SYSTEM_EXCLUSIVE</code>,
	 * <code>SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE</code> or
	 * <code>ShortMessage.*</code>.
	 * The array may also be null to indicate that all types of messages are
	 * accepted.
	 */
	public void addMIDIListener(MIDIListener listener, int[] acceptedStatus){
		inReceiver.addMIDIListener(listener, acceptedStatus);
	}
	/**
	 * Remove a MIDIListener from this MIDIManager
	 * @param listener The MIDIListener to remove
	 */
	public void removeMIDIListener(MIDIListener listener){
		inReceiver.removeMIDIListener(listener);
	}
	/**
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		dispose();
		super.finalize();
	}
}