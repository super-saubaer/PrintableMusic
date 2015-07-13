package com.groovemanager.pref;

import java.util.ArrayList;
import java.util.Iterator;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;

/**
 * This class represents a preference page to be used for audio settings that
 * can be applied to a AudioManager instance
 * @author Manu Robledo
 *
 */
public class AudioPrefPage extends FieldEditorPreferencePage {
	/**
	 * Create a new AudioPrefPage with the given title
	 * @param title The title of the page
	 */
	public AudioPrefPage(String title) {
		super(FieldEditorPreferencePage.GRID);
		setTitle(title);
	}
	/**
	 * Create a new AudioPrefPage with "Audio Settings" as title
	 *
	 */
	public AudioPrefPage(){
		this("Audio Settings");
	}
	protected void createFieldEditors() {
		// verfügbare Audio-Ports holen
		ArrayList ins = new ArrayList(), outs = new ArrayList();
		Mixer m;
		Line l;
		Line.Info[] lineinfos;
		Mixer.Info[] infos = AudioSystem.getMixerInfo();
		Line.Info input = new Line.Info(TargetDataLine.class);
		Line.Info output = new Line.Info(SourceDataLine.class);
		for(int i = 0; i < infos.length; i++){
			//if(!infos[i].getName().substring(0, 4).equals("Java")){
				m = AudioSystem.getMixer(infos[i]);
				if(m != null){
					// Inputs
					if(m.getTargetLineInfo(input).length > 0){
						ins.add(infos[i]);
					}
					// Outputs
					if(m.getSourceLineInfo(output).length > 0){
						outs.add(infos[i]);
					}
				}
			//}
		}
		// Inputs
		String[][] inLines = new String[ins.size()][2];
		int i = 0;
		for(Iterator iter = ins.iterator(); iter.hasNext(); i++){
			Mixer.Info info = (Mixer.Info)iter.next();
			inLines[i][0] = info.getName();
			inLines[i][1] = info.getVendor() + info.getName() + info.getVersion() + i;
		}
		
		// Outputs
		String[][] outLines = new String[outs.size()][2];
		i = 0;
		for(Iterator iter = outs.iterator(); iter.hasNext(); i++){
			Mixer.Info info = (Mixer.Info)iter.next();
			outLines[i][0] = info.getName(); 
			outLines[i][1] = info.getVendor() + info.getName() + info.getVersion() + i;
		}
		
		RadioGroupFieldEditor audioOut = new RadioGroupFieldEditor("audio_out", "Audio Output Port", 1, outLines, getFieldEditorParent(), true);
		addField(audioOut);
		RadioGroupFieldEditor audioIn = new RadioGroupFieldEditor("audio_in", "Audio Input Port", 1, inLines, getFieldEditorParent(), true);
		addField(audioIn);
		IntegerFieldEditor playBuffer = new IntegerFieldEditor("audio_play_buffer", "Buffer Size in Sample Frames", getFieldEditorParent());
		addField(playBuffer);
		IntegerFieldEditor playerPriority = new IntegerFieldEditor("audio_player_priority", "Priority of the player thread from " + Thread.MIN_PRIORITY + " to " + Thread.MAX_PRIORITY, getFieldEditorParent());
		playerPriority.setValidRange(Thread.MIN_PRIORITY, Thread.MAX_PRIORITY);
		addField(playerPriority);
	}
}
