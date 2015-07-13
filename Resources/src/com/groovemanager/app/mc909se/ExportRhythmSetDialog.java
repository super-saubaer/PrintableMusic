/*
 * Created on 20.06.2004
 *
 */
package com.groovemanager.app.mc909se;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;

import com.groovemanager.app.sse.WaveTab;
import com.groovemanager.core.FileManager;
import com.groovemanager.exception.NotFinishedException;
import com.groovemanager.exception.NotReadyException;
import com.groovemanager.gui.custom.ProgressMonitor;
import com.groovemanager.midi.MIDIManager;
import com.groovemanager.sampled.AudioManager;
import com.groovemanager.sampled.providers.AudioFileOutputStream;
import com.groovemanager.sampled.providers.RLNDChunk;
import com.groovemanager.sampled.waveform.Marker;
import com.groovemanager.sampled.waveform.Selection;
import com.groovemanager.thread.SaveFileThread;

/**
 * This dialog is used to display the ExportRhythmSetWizard. It performs the
 * needed operations (mainly sending MIDI messages) when needed.
 * @author Manu Robledo
 *
 */
public class ExportRhythmSetDialog extends WizardDialog {
	/**
	 * The ExportRhythmSetWizard to be displayed in this dialog
	 */
	private ExportRhythmSetWizard wizard;
	/**
	 * The Properties object representing the source file
	 */
	private Mc909SampleEditor.Properties properties;
	/**
	 * The WaveTab containing the source file
	 */
	private WaveTab wt;
	/**
	 * The next number to be used for sample export
	 */
	private int maxNr = 0;
	/**
	 * The number of slice samples exported
	 */
	private int samples = 0;
	/**
	 * Indicates whether the samples have been exported to user area (true) or
	 * card area (false)
	 */
	private boolean isUser;
	/**
	 * Create a new ExportRhythmSetDialog
	 * @param parentShell The dialog´s parent shell
	 * @param properties The Properties object representing the sample to be
	 * exported
	 * @param wt The WaveTab containing ths sample
	 */
	public ExportRhythmSetDialog(Shell parentShell, Mc909SampleEditor.Properties properties, WaveTab wt) {
		super(parentShell, new ExportRhythmSetWizard());
		wizard = (ExportRhythmSetWizard)getWizard();
		this.properties = properties;
		this.wt = wt;
	}
	protected void nextPressed() {
		if(getCurrentPage() == wizard.exportSamplesPage){
			wizard.exportSamplesPage.pathChecker.stop();
			
			wizard.playSequencePage.bpm = properties.bpm;
			if(wizard.exportSamplesPage.playSequence.getSelection()){
				try {
					wizard.playSequencePage.output = MIDIManager.getDefault().getOutDevice();
					Method[] methods = MidiSystem.class.getMethods();
					Method m = null;
					for (int i = 0; i < methods.length; i++) {
						if(methods[i].getName().equals("getSequencer") && methods[i].getParameterTypes().length == 1 && methods[i].getParameterTypes()[0].equals(Boolean.TYPE)) m = methods[i];
					}
					if(m != null) wizard.playSequencePage.sequencer = (Sequencer) m.invoke(null, new Object[]{new Boolean(false)});
					else wizard.playSequencePage.sequencer = MidiSystem.getSequencer();
					wizard.playSequencePage.seq = createSequence();
					wizard.playSequencePage.setPlayVisible(true);
				} catch (MidiUnavailableException e1) {
					e1.printStackTrace();
					wizard.playSequencePage.setPlayVisible(false);
					wizard.playSequencePage.setMessage("The sequence can not be transmitted via MIDI because of the following reason:\n" + e1.getMessage(), WizardPage.ERROR);
				} catch (InvalidMidiDataException e) {
					e.printStackTrace();
					wizard.playSequencePage.setPlayVisible(false);
					wizard.playSequencePage.setMessage("The sequence can not be transmitted via MIDI because of the following reason:\n" + e.getMessage(), WizardPage.ERROR);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					wizard.playSequencePage.setPlayVisible(false);
					wizard.playSequencePage.setMessage("The sequence can not be transmitted via MIDI because of the following reason:\n" + e.getMessage(), WizardPage.ERROR);
				} catch (InvocationTargetException e) {
					e.printStackTrace();
					wizard.playSequencePage.setPlayVisible(false);
					wizard.playSequencePage.setMessage("The sequence can not be transmitted via MIDI because of the following reason:\n" + e.getMessage(), WizardPage.ERROR);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					wizard.playSequencePage.setPlayVisible(false);
					wizard.playSequencePage.setMessage("The sequence can not be transmitted via MIDI because of the following reason:\n" + e.getMessage(), WizardPage.ERROR);
				}
			}
			else wizard.playSequencePage.setPlayVisible(false);
			
			final File f = new File(wizard.exportSamplesPage.exportPath + ExportSamplesPage.SMPL_FOLDER);
			String destName;
			if(wizard.exportSamplesPage.internal.getSelection()){
				isUser = true;
				destName = "User";
			}
			else{
				isUser = false;
				destName = "Card";
			}
			BusyIndicator.showWhile(getShell().getDisplay(), new Runnable(){
				public void run(){
					File[] files = f.listFiles(new FilenameFilter() {
						public boolean accept(File f, String name) {
							if(name.length() != 12) return false;
							else if(!name.startsWith("smpl")) return false;
							else if(!(name.endsWith(".wav") || name.endsWith(".aif"))) return false;
							else try{
								Integer.parseInt(name.substring(4, 8));
							}catch(NumberFormatException e){ return false; }
							return true;
						}
					});
					for (int i = 0; i < files.length; i++) {
						int nr = Integer.parseInt(files[i].getName().substring(4, 8));
						if(nr > maxNr) maxNr = nr;
					}
				}
			});
			Marker[] markers = wt.getWaveDisplay().getMarkers();
			int samplePos = maxNr + 2;
			for (int i = 0; i < markers.length && i < 16; i++) {
				int startPos = markers[i].getPosition();
				int endPos;
				if(i + 1 == markers.length || wizard.exportSamplesPage.slicesTillEnd.getSelection()) endPos = wt.getTotalLength(); 
				else endPos = markers[i + 1].getPosition();
				wt.getWaveDisplay().setSelection(new Selection(startPos, endPos));
				wt.getWaveDisplay().setPosition(startPos);
				try {
					AudioInputStream in = wt.getAudioInputStream();
					int channels = in.getFormat().getChannels();
					String s = f.getAbsolutePath() + File.separator + "smpl";
					String nr = "" + samplePos;
					while(nr.length() < 4) nr = "0" + nr;
					s += nr + ".wav";
					File target = new File(s);
					RLNDChunk chunk = new RLNDChunk();
					chunk.setBPM(properties.bpm);
					s = properties.nameText.getText();
					while(s.length() < 14) s += " ";
					if(i < 9) nr = "0" + (i + 1);
					else nr = "" + (i + 1);
					s = s.substring(0, 14) + nr;
					chunk.setSampleName(s);
					chunk.setLoopMode((byte)properties.loopMode.getSelectionIndex());
					chunk.setRootKey((byte)60);
					chunk.setSampleEnd(endPos - startPos);
					chunk.setSampleStart(0);
					chunk.setSampleLength(endPos - startPos);
					chunk.setTimeStretchType((short)properties.timeStretchType.getSelectionIndex());
					HashMap props = new HashMap();
					props.put("RLND", chunk);
					AudioFileOutputStream out = AudioManager.getDefault().getAudioFileOutputStream(target, new AudioFormat(44100, 16, channels, true, false), AudioFileFormat.Type.WAVE, props, new String[]{"RLND"}, null);
					
					if(!in.getFormat().equals(out.getFormat())) in = AudioSystem.getAudioInputStream(out.getFormat(), in);
					
					SaveFileThread saver = new SaveFileThread(in, out, (int)in.getFrameLength(), in.getFormat().getFrameSize(), true);
					ProgressMonitor monitor = new ProgressMonitor(getShell(), saver, "Exporting Sample " + (i + 1), "Exporting " + s + " to " + destName + " " + samplePos);
					monitor.start();
					samplePos += channels;
					samples++;
				} catch (NotReadyException e) {
					e.printStackTrace();
					close();
				} catch (UnsupportedAudioFileException e) {
					e.printStackTrace();
					close();
				} catch (IOException e) {
					e.printStackTrace();
					close();
				} catch (NotFinishedException e) {
					e.printStackTrace();
				}
				wizard.createRhythmSetPage.setMessage("The samples have been exported.", WizardPage.INFORMATION);
			}
			if(wizard.exportSamplesPage.createSequence.getSelection()) try {
				String file = createMidiFile();
				wizard.createRhythmSetPage.setMessage("The samples have been exported and the SMF file with the sequence has been generated as " + file + ".\n", WizardPage.INFORMATION);
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
				wizard.createRhythmSetPage.setMessage("The samples have been exported. The SMF file with the sequence could not be created because of the following reason:\n" + e.getMessage(), WizardPage.ERROR);
			} catch (IOException e) {
				e.printStackTrace();
				wizard.createRhythmSetPage.setMessage("The samples have been exported. The SMF file with the sequence could not be created because of the following reason:\n" + e.getMessage(), WizardPage.ERROR);
			}
			wizard.createRhythmSetPage.setImage(FileManager.getDefault().getRootPath("909screens/exit.gif"));
			super.nextPressed();
		}
		else if(getCurrentPage() == wizard.createRhythmSetPage){
			byte cont;
			if(wizard.exportSamplesPage.bank.getSelectionIndex() == 0) cont = 0;
			else cont = 32;
			try {
				int channel = wizard.exportSamplesPage.part.getSelectionIndex();
				MIDIManager.getDefault().sendController(channel, (byte)0, (byte)82);
				MIDIManager.getDefault().sendController(channel, (byte)32, cont);
				MIDIManager.getDefault().sendProgramChange(channel, (byte)wizard.exportSamplesPage.rhythm.getSelectionIndex());
				
				MIDIManager.getDefault().sendSysEx(MC909SysEx.createRhythmLevelMessage(channel, (byte)127));
				MIDIManager.getDefault().sendSysEx(MC909SysEx.createRhythmNameMessage(channel, properties.nameText.getText()));
				
				int channels = wt.getFormat().getChannels();
				
				for(int i = 0; i < samples; i++){
					String s = properties.nameText.getText();
					while(s.length() < 14) s += " ";
					String nr;
					if(i < 9) nr = "0" + (i + 1);
					else nr = "" + (i + 1);
					s = s.substring(0, 14) + nr;
					MIDIManager.getDefault().sendSysEx(MC909SysEx.createRhythmToneNameMessage(channel, s, i));
					int right;
					if(channels == 1) right = 0;
					else right = maxNr + 2 + i * 2 + 1;
					MIDIManager.getDefault().sendSysEx(MC909SysEx.createRhythmWaveMessage(channel, i, 0, isUser, maxNr + 2 + i * channels, right));
				}
				String destination;
				if(wizard.exportSamplesPage.bank.getSelectionIndex() == 0) destination = "User";
				else destination = "Card";
				wizard.playSequencePage.setMessage("The Rhythm set has been created on part " + (wizard.exportSamplesPage.part.getSelectionIndex() + 1) + " as " + destination + " " + (wizard.exportSamplesPage.rhythm.getSelectionIndex() + 1) + ". To keep it you have to save it manually on your MC-909.", WizardPage.INFORMATION);
				wizard.playSequencePage.setImage(FileManager.getDefault().getRootPath("909screens/write.jpg"));
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
				wizard.playSequencePage.setMessage("The Rhythm set could not be created because of the following reason:\n" + e.getMessage(), WizardPage.ERROR);
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
				wizard.playSequencePage.setMessage("The Rhythm set could not be created because of the following reason:\n" + e.getMessage(), WizardPage.ERROR);
			}
			super.nextPressed();
			wizard.canFinish = true;
			((WizardPage)getCurrentPage()).setPageComplete(true);
			getButton(IDialogConstants.FINISH_ID).setEnabled(true);
		}
		getButton(IDialogConstants.BACK_ID).setEnabled(false);
	}
	/**
	 * Back is not possible
	 */
	protected void backPressed() {
	}
	/**
	 * Create the MIDI file out of the slice sequence
	 * @return The path of the created file
	 * @throws InvalidMidiDataException Should not happen
	 * @throws IOException If the write process failed
	 */
	private String createMidiFile() throws InvalidMidiDataException, IOException{
		Marker[] markers = wt.getWaveDisplay().getMarkers();
		Sequence seq = new Sequence(Sequence.PPQ, 480, 1);
		Track track = seq.getTracks()[0];
		int totalLength = wt.getTotalLength();
		float bpm = properties.bpm;
		int nominator = properties.beats;
		int denominator = properties.denom;
		int bars = properties.bars;
		int totalTicks = bars * nominator * 480;
		MetaMessage meta;
		byte[] metadata;
		ShortMessage sm;
		
		// Program & Bank change
		int channel = wizard.exportSamplesPage.part.getSelectionIndex(); 
		sm = new ShortMessage();
		sm.setMessage(ShortMessage.CONTROL_CHANGE, channel, 0, 82);
		track.add(new MidiEvent(sm, 0));
		
		sm = new ShortMessage();
		int cont;
		if(wizard.exportSamplesPage.bank.getSelectionIndex() == 0) cont = 0;
		else cont = 32;
		sm.setMessage(ShortMessage.CONTROL_CHANGE, channel, 32, cont);
		track.add(new MidiEvent(sm, 0));
		
		sm = new ShortMessage();
		sm.setMessage(ShortMessage.PROGRAM_CHANGE, channel, wizard.exportSamplesPage.rhythm.getSelectionIndex(), 0);
		track.add(new MidiEvent(sm, 0));
		
		// Name
		meta = new MetaMessage();
		metadata = properties.nameText.getText().getBytes();
		meta.setMessage(0x03, metadata, metadata.length);
		track.add(new MidiEvent(meta, 0));
		
		// Tempo
		meta = new MetaMessage();
		float tempo = 60000000 / bpm;
		metadata = new byte[3];
		metadata[0] = (byte)(tempo / (256 * 256));
		tempo -= metadata[0] * 256 * 256;
		metadata[1] = (byte)(tempo / 256);
		tempo -= metadata[1] * 256;
		metadata[2] = (byte)tempo;
		meta.setMessage(0x51, metadata, metadata.length);
		track.add(new MidiEvent(meta, 0));
		
		// Time signature
		meta = new MetaMessage();
		metadata = new byte[4];
		metadata[0] = (byte)properties.beats;
		metadata[1] = (byte)properties.denom;
		metadata[2] = 0x18;
		metadata[3] = 0x08;
		meta.setMessage(0x59, metadata, metadata.length);
		track.add(new MidiEvent(meta, 0));
		
		for (int i = 0; i < markers.length && i < 16; i++) {
			int pos = markers[i].getPosition();
			int end;
			if(i + 1 == markers.length) end = totalLength;
			else end = markers[i + 1].getPosition();
			ShortMessage noteOn = new ShortMessage();
			ShortMessage noteOff = new ShortMessage();
			noteOn.setMessage(ShortMessage.NOTE_ON, 0, 59 + i, 127);
			noteOff.setMessage(ShortMessage.NOTE_OFF, 0, 59 + i, 0);
			long ticksOn = (long)Math.round(totalTicks / (double)totalLength * pos);
			long ticksOff = (long)Math.round(totalTicks / (double)totalLength * end) - 1;
			track.add(new MidiEvent(noteOn, ticksOn));
			track.add(new MidiEvent(noteOff, ticksOff));
		}
		String path = wizard.exportSamplesPage.exportPath;
		if(!path.endsWith(File.separator)) path += File.separator;
		path += "TMP" + File.separator + "SMF" + File.separator + "export_" + properties.nameText.getText() + ".mid";
		File midiFile = new File(path); 
		MidiSystem.write(seq, 0, midiFile);
		return path;
	}
	/**
	 * Create the MIDI sequence out of the slice sequence
	 * @return The created MIDI sequence
	 * @throws InvalidMidiDataException Should not happen
	 */
	private Sequence createSequence() throws InvalidMidiDataException{
		Marker[] markers = wt.getWaveDisplay().getMarkers();
		Sequence seq = new Sequence(Sequence.PPQ, 480, 1);
		Track track = seq.getTracks()[0];
		int totalLength = wt.getTotalLength();
		int nominator = properties.beats;
		int bars = properties.bars;
		int totalTicks = bars * nominator * 480;
		int channel = wizard.exportSamplesPage.part.getSelectionIndex();
		
		for (int i = 0; i < markers.length && i < 16; i++) {
			int pos = markers[i].getPosition();
			int end;
			if(i + 1 == markers.length) end = totalLength;
			else end = markers[i + 1].getPosition();
			ShortMessage noteOn = new ShortMessage();
			ShortMessage noteOff = new ShortMessage();
			noteOn.setMessage(ShortMessage.NOTE_ON, channel, 59 + i, 127);
			noteOff.setMessage(ShortMessage.NOTE_OFF, channel, 59 + i, 0);
			long ticksOn = (long)Math.round(totalTicks / (double)totalLength * (double)pos);
			long ticksOff = (long)Math.round(totalTicks / (double)totalLength * (double)end) - 1;
			track.add(new MidiEvent(noteOn, ticksOn));
			track.add(new MidiEvent(noteOff, ticksOff));
		}
		
		// Manual syncing
		ShortMessage m = new ShortMessage();
		m.setMessage(ShortMessage.START);
		track.add(new MidiEvent(m, 0));
		
		m = new ShortMessage();
		m.setMessage(ShortMessage.STOP);
		track.add(new MidiEvent(m, totalTicks));
		
		m = new ShortMessage();
		m.setMessage(ShortMessage.TIMING_CLOCK);
		long clockIntervall = totalTicks / bars / nominator / 24;
		for(int i = 0; i < bars * nominator; i++){
			for(int j = 0; j < 24; j++){
				track.add(new MidiEvent(m, (i * 24 + j) * clockIntervall));
			}
		}
		
		return seq;
	}
}
