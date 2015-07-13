package lmu_PrintableMusic;



import java.util.List;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Patch;
import javax.sound.midi.Receiver;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;
import javax.sound.midi.VoiceStatus;

import lmu_PrintableMusic_object.ModelCube;
import lmu_PrintableMusic_object.ModelPrism;

public class MidiHandler implements Synthesizer
{
	
	public MidiHandler()
    {
        MidiDevice device;
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (int i = 0; i < infos.length; i++) {
            try {
            device = MidiSystem.getMidiDevice(infos[i]);
            //does the device have any transmitters?
            //if it does, add it to the device list
            //System.out.println(infos[i]);

            //get all transmitters
            List<Transmitter> transmitters = device.getTransmitters();
            //and for each transmitter
            //System.out.println("for transmitters");

            /*
             * 
            for(int j = 0; j<transmitters.size();j++) 
            {
            	System.out.println("Here: " + device.getDeviceInfo().toString());
                //create a new receiver
            	MidiInputReceiver receiver = new MidiInputReceiver(device.getDeviceInfo().toString());
            	//System.out.println("init Synth");
            	receiver.initSynth();
            	receiver.initFrontend();
            	
            	transmitters.get(j).setReceiver(receiver);
            }
             */
            
            if(!device.getDeviceInfo().toString().contains("Real Time Sequencer"))
            {

                Transmitter trans = device.getTransmitter();
                
                MidiInputReceiver receiver = new MidiInputReceiver(device.getDeviceInfo().toString());
            	//System.out.println("init Synth");
            	receiver.initSynth();
            	receiver.initFrontend();
                trans.setReceiver(receiver);
               
                
                

                //open each device
                device.open();
                //if code gets this far without throwing an exception
                //print a success message
               
            	
            }


        } catch (MidiUnavailableException e) {}
    }

    }

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Info getDeviceInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMaxReceivers() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaxTransmitters() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getMicrosecondPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Receiver getReceiver() throws MidiUnavailableException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Receiver> getReceivers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Transmitter getTransmitter() throws MidiUnavailableException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Transmitter> getTransmitters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isOpen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void open() throws MidiUnavailableException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Instrument[] getAvailableInstruments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MidiChannel[] getChannels() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Soundbank getDefaultSoundbank() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getLatency() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Instrument[] getLoadedInstruments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMaxPolyphony() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public VoiceStatus[] getVoiceStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSoundbankSupported(Soundbank soundbank) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean loadAllInstruments(Soundbank soundbank) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean loadInstrument(Instrument instrument) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean loadInstruments(Soundbank soundbank, Patch[] patchList) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean remapInstrument(Instrument from, Instrument to) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void unloadAllInstruments(Soundbank soundbank) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unloadInstrument(Instrument instrument) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unloadInstruments(Soundbank soundbank, Patch[] patchList) {
		// TODO Auto-generated method stub
		
	}
}
