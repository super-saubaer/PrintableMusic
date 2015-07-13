package com.groovemanager.pref;

import java.util.ArrayList;
import java.util.Iterator;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;

/**
 * This class represents a PreferencePage for selection of MIDI settings.
 * These settings can be applied to a MIDIManager instance 
 * @author Manu Robledo
 *
 */
public class MIDIPrefPage extends FieldEditorPreferencePage{
	/**
	 * Construct a new MIDIPrefPage using "MIDI Settings" as title 
	 *
	 */
	public MIDIPrefPage() {
		this("MIDI Settings");
	}
	/**
	 * Construct a new MIDIPrefPage using the given title 
	 * @param title The page title
	 */
	public MIDIPrefPage(String title){
		super(FieldEditorPreferencePage.GRID);
		setTitle(title);
	}
	protected void createFieldEditors() {
		// verfügbare MidiPorts holen
		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
		ArrayList in = new ArrayList(), out = new ArrayList();
		for(int i = 0; i < infos.length; i++){
			if(!infos[i].getName().substring(0, 4).equals("Java")){
				try {
					MidiDevice device = MidiSystem.getMidiDevice(infos[i]);
					if(device.getMaxReceivers() > 0 || device.getMaxReceivers() == -1) out.add(infos[i]);
					if(device.getMaxTransmitters() > 0 || device.getMaxTransmitters() == -1) in.add(infos[i]);
				} catch (MidiUnavailableException e) {
					e.printStackTrace();
				}
			}
		}
		
		// Arrays füllen
		String[][] inPorts = new String[in.size()][2];
		String[][] outPorts = new String[out.size()][2];
		int i = 0;
		for(Iterator iter = in.iterator(); iter.hasNext(); i++){
			inPorts[i][0] = inPorts[i][1] = ((MidiDevice.Info)iter.next()).getName();
		}
		i = 0;
		for(Iterator iter = out.iterator(); iter.hasNext(); i++){
			outPorts[i][0] = outPorts[i][1] = ((MidiDevice.Info)iter.next()).getName();
		}

		
		
		RadioGroupFieldEditor midiIn = new RadioGroupFieldEditor("midi_in", "Midi In Port", 1, inPorts, getFieldEditorParent(), true);
		addField(midiIn);

		RadioGroupFieldEditor midiOut = new RadioGroupFieldEditor("midi_out", "Midi Out Port", 1, outPorts, getFieldEditorParent(), true);
		addField(midiOut);
	}

}
