package com.groovemanager.midi;

import java.util.HashMap;
import java.util.Iterator;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;

/**
 * This class is a Receiver implementation for being connected to the MIDI
 * Input of a MIDIManager. When added MIDIListeners to an instance of this
 * class, it will ask the MIDIManager to open the input device. When all
 * listeners have been removed, it will ask the MIDIManager to clode the
 * input device. For an instance of this class the status bytes of the types
 * of MIDI messages to listen to can be set. Also for any MIDIListener added
 * to this instance, the accepted message types can be set. But a MIDIListener
 * will not be notified of messages the MIDIInReceiver doesn´t acccept, even if
 * he supports them. 
 * @author Manu Robledo
 *
 */
class MIDIInReceiver implements Receiver {
	/**
	 * The status bytes of the messages to listen to
	 */
	private int[] acceptStatus;
	/**
	 * The MIDIListeners registered with this Receiver
	 */
	private HashMap listeners = new HashMap();
	/**
	 * The MIDIManager to which this Receiver is connected
	 */
	private final MIDIManager midiManager;
	/**
	 * Construct a new MIDIReciever that listens to the specified types of
	 * MIDI messages on the specified MIDIManager
	 * @param manager The MIDIManager to use
	 * @param acceptStatus Array of status bytes to accept. Can be any of
	 * <code>MetaMessage.META</code>,
	 * <code>SysexMessage.SYSTEM_EXCLUSIVE</code>,
	 * <code>SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE</code> or
	 * <code>ShortMessage.*</code>.
	 * The array may also be null to indicate that all types of messages are
	 * accepted.
	 */
	MIDIInReceiver (MIDIManager manager, int[] acceptStatus){
		setAcceptStatus(acceptStatus);
		midiManager = manager;
	}
	/**
	 * Construct a new MIDIReciever that listens to all types of
	 * MIDI messages on the specified MIDIManager
	 * @param manager The MIDIManager to use
	 */
	MIDIInReceiver (MIDIManager manager){
		setAcceptStatus(null);
		midiManager = manager;
	}
	/**
	 * Check, if given status accepted
	 * @param accepted Array of accepted status bytes or <code>null</code> to
	 * indicate that all types of messages are accepted
	 * @param status The status byte to check for
	 * @return true, if the given Array is <code>null</code> or if the given
	 * status byte is contained inside the Array, false otherwise
	 */
	protected static boolean checkStatus(int[] accepted, int status){
		if(accepted == null) return true;
		for (int i = 0; i < accepted.length; i++) {
			if(status == accepted[i]) return true;
		}
		return false;
	}
	/**
	 * 
	 * @see javax.sound.midi.Receiver#send(javax.sound.midi.MidiMessage, long)
	 */
	public void send(MidiMessage message, long timestamp) {
		if(!checkStatus(acceptStatus, message.getStatus())) return;
		Iterator iter = listeners.keySet().iterator();
		while(iter.hasNext()){
			MIDIListener l = (MIDIListener)iter.next();
			int[] accept = (int[])listeners.get(l);
			if(checkStatus(accept, message.getStatus())) l.processMessage(message);
		}
	}
	/**
	 * Add a MIDIListener that listens to the specified types of messages
	 * @param l The MIDIListener to add
	 * @param acceptTypes Array of status bytes this listener accepts. Can be
	 * any of <code>MetaMessage.META</code>,
	 * <code>SysexMessage.SYSTEM_EXCLUSIVE</code>,
	 * <code>SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE</code> or
	 * <code>ShortMessage.*</code>.
	 * The array may also be null to indicate that all types of messages are
	 * accepted.
	 */
	public void addMIDIListener(MIDIListener l, int[] acceptTypes){
		midiManager.enableInListener();
		listeners.put(l, acceptTypes);
	}
	/**
	 * Remove the specified MIDIListener from the list of listeners. If
	 * no listeners are left after this call, the MIDIManager will be asked
	 * to close the device if it is not needed elsewhere.
	 * @param l The MIDIListener to remove
	 */
	public void removeMIDIListener(MIDIListener l){
		listeners.remove(l);
		if(listeners.size() == 0) midiManager.disableInListener();
	}
	/**
	 * Set the MIDI message types accepted by this Receveiver
	 * @param status Array of status bytes to accept or <code>null</code> to
	 * indicate that all types are accepted
	 */
	public void setAcceptStatus(int[] status){
		acceptStatus = status;
	}
	/**
	 * @see javax.sound.midi.Receiver#close()
	 */
	public void close() {
		listeners.clear();
		midiManager.disableInListener();
	}
	/**
	 * Get the MIDI message types accepted by this Receveiver
	 * @return status Array of status bytes to accept or <code>null</code> to
	 * indicate that all types are accepted
	 */
	public int[] getAcceptedStatus(){
		return acceptStatus;
	}
	/**
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
}
