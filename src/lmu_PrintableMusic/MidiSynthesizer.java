package lmu_PrintableMusic;


import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;


public class MidiSynthesizer
{
	Synthesizer synth = null;
	MidiChannel[] mc = null;
	Instrument[] instr = null;
	

	
	public void initSynth() throws MidiUnavailableException
	{
		
		synth = MidiSystem.getSynthesizer();
		synth.open();
		
		
		mc = synth.getChannels();
		//System.out.println(mc);
		
		
		instr = synth.getDefaultSoundbank().getInstruments();
		synth.loadInstrument(instr[90]);
		
	}
	
	public void playSynth(ShortMessage answer, long timeStamp)
	{
		//System.out.println(mc);
		mc[5].noteOn(answer.getData1(),answer.getData2());
	}
	
	public void stopSynth(ShortMessage answer, long timeStamp)
	{
		//System.out.println(mc);
		mc[5].noteOff(answer.getData1(),answer.getData2());
	}
	

	
	
	
	
	
		
	

	

}
