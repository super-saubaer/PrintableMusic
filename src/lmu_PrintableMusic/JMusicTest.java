package lmu_PrintableMusic;

import jm.JMC;
import jm.midi.MidiCommunication;
import jm.music.data.Note;
import jm.util.Play;

public class JMusicTest extends MidiCommunication implements JMC
{
	public static void main(String[] args) {
		JMusicTest mt = new JMusicTest();
    }	

    public void handleMidiInput(int status, int channel, int data1, int data2) 
    {
        System.out.println(status + " " + channel + " " + data1 + " " + data2);
      
    }

}
