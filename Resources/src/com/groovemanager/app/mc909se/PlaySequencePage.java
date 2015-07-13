/*
 * Created on 22.06.2004
 *
 */
package com.groovemanager.app.mc909se;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.groovemanager.core.FileManager;

/**
 * This class is used for the wizard page that is displayed at the end of 
 * the ExportRhythmSetWizard
 * @author Manu Robledo
 *
 */
public class PlaySequencePage extends OnlyMessagePage {
	/**
	 * play button
	 */
	Button play;
	/**
	 * The sequence to play
	 */
	Sequence seq;
	/**
	 * The sequencer to be used for playing the sequence
	 */
	Sequencer sequencer;
	/**
	 * The output device to use
	 */
	MidiDevice output;
	/**
	 * The tempo in which to play the sequence
	 */
	float bpm;
	/**
	 * Indicates whether the play button should be visible or not 
	 */
	boolean visible = true;
	/**
	 * Text to display when the play button is visible
	 */
	final static String VISIBLE_TEXT = "You can now transmit the slice sequence to your MC-909 via MIDI. " +
	"To do so, connect the MC-909 to the MIDI Out port specified in the Settings dialog, " +
	"set the Sync Mode to Slave and go into Realtime Rec standby. There, set Rec Beat to " +
	"the Time signature you selected for this loop (usually 4/4) and set Rec Count In as " +
	"well as Quantize Resolution to OFF. When ready, press the play button below.",
	/**
	 * Text to display when the play button is not visible
	 */
	INVISIBLE_TEXT = "";
	/**
	 * Create a new PlaySequencePage
	 * @param pageName The page´s name
	 */
	public PlaySequencePage(String pageName) {
		super(pageName, VISIBLE_TEXT);
	}
	/**
	 * Create a new PlaySequencePage
	 * @param pageName The page´s name
	 * @param title The page title
	 * @param titleImage The title image
	 */
	public PlaySequencePage(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage, VISIBLE_TEXT);
	}
	public void createControl(Composite parent) {
		super.createControl(parent);
		Composite comp = (Composite)getControl();
				
		play = new Button(comp, SWT.PUSH);
		play.setVisible(visible);
		play.setImage(new Image(play.getDisplay(), FileManager.getDefault().getRootPath("icons/trans/play.gif")));
		play.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				play.setEnabled(false);
				play();
				play.setEnabled(true);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
	/**
	 * Play the sequence
	 *
	 */
	void play(){
		if(seq == null || sequencer == null || output == null) return;
		try {
			sequencer.open();
			output.open();
			sequencer.getTransmitter().setReceiver(output.getReceiver());
			sequencer.setSequence(seq);
			sequencer.setTempoInBPM(bpm);
			sequencer.start();
			while(sequencer.isRunning()) try{Thread.sleep(1000);} catch(InterruptedException e){}
			sequencer.close();
			output.close();
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
			sequencer.close();
			output.close();
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
			sequencer.close();
			output.close();
		}
	}
	/**
	 * Set the visibility of the play button
	 * @param visible The visibility of the play button
	 */
	public void setPlayVisible(boolean visible){
		this.visible = visible;
		if(play != null && !play.isDisposed()) play.setVisible(visible);
		if(visible) setMessageText(VISIBLE_TEXT);
		else setMessageText(INVISIBLE_TEXT);
	}
}