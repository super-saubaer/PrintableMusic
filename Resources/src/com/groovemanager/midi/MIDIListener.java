package com.groovemanager.midi;

import javax.sound.midi.MidiMessage;

/**
 * Instances of classes implementing this interface can be added to a
 * MIDIInReceiver in order to be notified of incoming MIDI events
 * @author Manu Robledo
 *
 */
public interface MIDIListener {
	/**
	 * Notification about an incoming MIDI event
	 * @param m The MidiMessage that just came in
	 */
	public void processMessage(MidiMessage m);
}
