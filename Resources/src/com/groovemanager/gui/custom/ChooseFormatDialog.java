/*
 * Created on 01.06.2004
 *
 */
package com.groovemanager.gui.custom;

import javax.sound.sampled.AudioFormat;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * This class represents a dialog in which the user can select some
 * attributes of an AudioFormat
 * @author Manu Robledo
 *
 */
public class ChooseFormatDialog extends TitleAreaDialog {
	/**
	 * Constant for the AudioFormat attributes 
	 */
	public final static int SAMPLING_RATE = 1,
		SAMPLE_SIZE = 2,
		ENCODING = 4,
		CHANNELS = 8,
		ENDIAN = 16;
	/**
	 * The pre-defined or the last selected AudioFormat
	 */
	private AudioFormat format;
	/**
	 * Possible values for Sample size selection
	 */
	private int[] sizes;
	/**
	 * Maximum value that can be selected as channels
	 */
	private int maxChannels;
	/**
	 * Possible values for sampleRate selection
	 */
	private float[] sampleRates;
	/**
	 * Possible values for Encoding selection
	 */
	private AudioFormat.Encoding[] encodings;
	/**
	 * The constants of all attributes that should be selectable by the user
	 * combined with |, for example CHANNELS | SAMPLING_RATE.
	 */
	private int showWhat;
	/**
	 * Text field for user input of the sample rate. Will only be used, if 
	 * <code>showWhat & SAMPLING_RATE > 0</code> and if no possible values
	 * for user selection of the sample rate are given.
	 */
	private Text sampleRateValue;
	/**
	 * Text field for user input of the sample size. Will only be used, if 
	 * <code>showWhat & SAMPLE_SIZE > 0</code> and if no possible values
	 * for user selection of the sample size are given.
	 */
	private Text sampleSizeValue;
	/**
	 * Drop-Down Menu for user selection of the sample rate. Will only be
	 * used, if <code>showWhat & SAMPLING_RATE > 0</code> and if possible
	 * values for user selection of the sample rate are given.
	 */
	private Combo sampleRateSelect;
	/**
	 * Drop-Down Menu for user selection of the sample size. Will only be
	 * used, if <code>showWhat & SAMPLE_SIZE > 0</code> and if possible
	 * values for user selection of the sample size are given.
	 */
	private Combo sampleSizeSelect;
	/**
	 * Drop-Down Menu for user selection of the encoding. Will only be
	 * used, if <code>showWhat & ENCODING > 0</code> and if possible
	 * values for user selection of the encoding are given.
	 */
	private Combo encodingSelect;
	/**
	 * Drop-Down Menu for user selection of the number of channels. Will
	 * only be used, if <code>showWhat & CHANNELS > 0</code>.
	 */
	private Combo channelSelect;
	/**
	 * Drop-Down Menu for user selection of the endianess. Will only be
	 * used, if <code>showWhat & ENDIAN > 0</code>.
	 */
	private Combo endianSelect;
	/**
	 * Current value for the sample rate of the AudioFormat
	 */
	private float sampleRate;
	/**
	 * Current number of channels of the AudioFormat
	 */
	private int channels;
	/**
	 * Current value for the sample size of the AudioFormat
	 */
	private int sampleSize;
	/**
	 * Current value for the encoding of the AudioFormat
	 */
	private AudioFormat.Encoding encoding;
	/**
	 * Current value of the endianess of the AudioFormat
	 */
	private boolean bigEndian;
	/**
	 * The message of this dialog
	 */
	private String msg = "";
	/**
	 * Constructs a new ChooseFormatDialog
	 * @param parentShell The Shell to be used as parent Shell of the dialog
	 * @param format The initial AudioFormat value
	 * @param message The message shown to the user
	 * @param showWhat Mask of attributes that should be selectable for the
	 * user. For example: <code>CHANNELS | SAMPLING_RATE</code>
	 * @param sizes Array of possible values for the sample size. If this
	 * parameter is null, a text input will be shown for the user to input
	 * any sample size
	 * @param sampleRates Array of possible values for the sample rate. If this
	 * parameter is null, a text input will be shown for the user to input
	 * any sample rate
	 * @param encodings Array of possible Encodings foir user selection
	 * @param maxChannels The maximum number of channels
	 */
	public ChooseFormatDialog(Shell parentShell, AudioFormat format, String message, int showWhat, int[] sizes, float[] sampleRates, AudioFormat.Encoding[] encodings, int maxChannels) {
		super(parentShell);
		this.format = format;
		this.sizes = sizes;
		this.encodings = encodings;
		this.maxChannels = maxChannels;
		this.sampleRates = sampleRates;
		this.showWhat = showWhat;
		sampleRate = format.getSampleRate();
		channels = format.getChannels();
		sampleSize = format.getSampleSizeInBits();
		encoding = format.getEncoding();
		bigEndian = format.isBigEndian();
		msg = message;
	}
	public void create() {
		super.create();
		setTitle("Choose Audio format");
	}
	protected Control createDialogArea(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		final GridLayout layout = new GridLayout();
		layout.marginWidth = 15;
		layout.marginHeight = 10;
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		comp.setLayout(layout);
		GridData gd;
		
		// Sampling rate
		if((showWhat & SAMPLING_RATE) > 0){
			new Label(comp, SWT.NONE).setText("Sampling rate (Hz)");
			if(sampleRates == null){
				sampleRateValue = new Text(comp, SWT.SINGLE | SWT.BORDER);
				sampleRateValue.setText("" + format.getSampleRate());
				sampleRateValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				sampleRateValue.addFocusListener(new FocusListener() {
					public void focusGained(FocusEvent e) {
					}
					public void focusLost(FocusEvent e) {
						try{
							sampleRate = Float.parseFloat(sampleRateValue.getText());
						} catch(NumberFormatException ex){
							sampleRateValue.setText(""+sampleRate);
						}
					}
				});
			}
			else{
				sampleRateSelect = new Combo(comp, SWT.READ_ONLY | SWT.DROP_DOWN);
				int sel = 0;
				for (int i = 0; i < sampleRates.length; i++) {
					sampleRateSelect.add(""+sampleRates[i]);
					if(sampleRates[i] == format.getSampleRate()) sel = i;
				}
				sampleRateSelect.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				sampleRateSelect.select(sel);
				sampleRateSelect.addSelectionListener(new SelectionListener() {
					public void widgetSelected(SelectionEvent e) {
						try{
							sampleRate = Float.parseFloat(sampleRateSelect.getText());
						} catch(NumberFormatException ex){
							sampleRateSelect.setText(""+sampleRate);
						}
					}
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});
			}
		}
		
		// Sample size
		if((showWhat & SAMPLE_SIZE) > 0){
			new Label(comp, SWT.NONE).setText("Sample size (Bit)");
			if(sizes == null){
				sampleSizeValue = new Text(comp, SWT.SINGLE | SWT.BORDER);
				sampleSizeValue.setText("" + format.getSampleSizeInBits());
				sampleSizeValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				sampleSizeValue.addFocusListener(new FocusListener() {
					public void focusGained(FocusEvent e) {
					}
					public void focusLost(FocusEvent e) {
						try{
							sampleSize = Integer.parseInt(sampleSizeValue.getText());
						} catch(NumberFormatException ex){
							sampleSizeValue.setText(""+sampleSize);
						}
					}
				});
			}
			else{
				int sel = 0;
				sampleSizeSelect = new Combo(comp, SWT.READ_ONLY | SWT.DROP_DOWN);
				for (int i = 0; i < sizes.length; i++) {
					sampleSizeSelect.add(""+sizes[i]);
					if(sizes[i] == format.getSampleSizeInBits()) sel = i;
				}
				sampleSizeSelect.select(sel);
				sampleSizeSelect.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				sampleSizeSelect.addSelectionListener(new SelectionListener() {
					public void widgetSelected(SelectionEvent e) {
						try{
							sampleSize = Integer.parseInt(sampleSizeSelect.getText());
						} catch(NumberFormatException ex){
							sampleSizeSelect.setText(""+sampleSize);
						}
					}
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});
			}
		}

		// Encoding
		if((showWhat & ENCODING) > 0 && encodings != null){
			new Label(comp, SWT.NONE).setText("Encoding");
			encodingSelect = new Combo(comp, SWT.READ_ONLY | SWT.DROP_DOWN);
			int sel = 0;
			for (int i = 0; i < encodings.length; i++) {
				encodingSelect.add(""+encodings[i]);
				if(encodings[i].equals(format.getEncoding())) sel = i;
			}
			encodingSelect.select(sel);
			encodingSelect.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			encodingSelect.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					// For compatibility to 1.4...
					encoding = new AudioFormat.Encoding(encodingSelect.getText()){};
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}

		// Channels
		if((showWhat & CHANNELS) > 0 && maxChannels > 0){
			new Label(comp, SWT.NONE).setText("Channels");
			channelSelect = new Combo(comp, SWT.READ_ONLY | SWT.DROP_DOWN);
			for (int i = 1; i <= maxChannels; i++) {
				channelSelect.add(""+i);
			}
			channelSelect.select(format.getChannels() - 1);
			channelSelect.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			channelSelect.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					try{
						channels = Integer.parseInt(channelSelect.getText());
					} catch(NumberFormatException ex){
						channelSelect.setText(""+channels);
					}
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}

		// Endianess
		if((showWhat & ENDIAN) > 0){
			new Label(comp, SWT.NONE).setText("Endianess");
			endianSelect = new Combo(comp, SWT.READ_ONLY | SWT.DROP_DOWN);
			endianSelect.add("Little endian");
			endianSelect.add("Big endian");
			if(format.isBigEndian()) endianSelect.select(1);
			else endianSelect.select(0);
			endianSelect.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			endianSelect.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					bigEndian = endianSelect.getSelectionIndex() > 0;
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});

		}
		if(msg != null) setMessage(msg);
		return comp;
	}
	/**
	 * Get the current AudioFormat of this dialog. If the dialog has not yet
	 * been opened, the format given to the Constructor will be returned.
	 * Otherwise the result of the user selection will be returned.
	 * @return The AudioFormat represented by this dialog
	 */
	public AudioFormat getFormat(){
		return new AudioFormat(encoding, sampleRate, sampleSize, channels, sampleSize * channels / 8, sampleRate, bigEndian);
	}
	/**
	 * Set the AudioFormat of this dialog
	 * @param format The current AudioFormat value of this dialog
	 */
	public void setFormat(AudioFormat format){
		encoding = format.getEncoding();
		sampleRate = format.getSampleRate();
		sampleSize = format.getSampleSizeInBits();
		channels = format.getChannels();
		bigEndian = format.isBigEndian();
	}
	/**
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		if((showWhat & ENCODING) > 0) encoding = PseudoAudioFormat.createEncoding(encodingSelect.getText());
		if((showWhat & CHANNELS) > 0) channels = channelSelect.getSelectionIndex() + 1;
		if((showWhat & ENDIAN) > 0) bigEndian = endianSelect.getSelectionIndex() > 0;
		if((showWhat & SAMPLE_SIZE) > 0){
			if(sizes == null) try{
				sampleSize = Integer.parseInt(sampleSizeValue.getText());
			} catch(NumberFormatException ex){
			}
			else try{
				sampleSize = Integer.parseInt(sampleSizeSelect.getText());
			} catch(NumberFormatException ex){
			}
		}
		if((showWhat & SAMPLING_RATE) > 0){
			if(sampleRates == null) try{
				sampleRate = Float.parseFloat(sampleRateValue.getText());
			} catch(NumberFormatException ex){
			}
			else try{
				sampleRate = Float.parseFloat(sampleRateSelect.getText());
			} catch(NumberFormatException ex){
			}
		}
		super.okPressed();
	}
	/**
	 * A Pseudo-class that is needed for accessing the AudioFormat.Encoding
	 * constructor in versions prior to 1.4
	 * @author Manu Robledo
	 *
	 */
	private static class PseudoAudioFormat extends AudioFormat{
		private final static PseudoAudioFormat DUMMY = new PseudoAudioFormat();
		private PseudoAudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean isBigEndian) {
			super(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, isBigEndian);
		}
		private PseudoAudioFormat(){
			super(44100, 16, 2, true, false);
		}
		private Encoding createEncoding0(String name){
			return new Encoding(name);
		}
		private static Encoding createEncoding(String name){
			return DUMMY.createEncoding0(name);
		}
		private class Encoding extends AudioFormat.Encoding{
			protected Encoding(String name) {
				super(name);
			}
		}
	}
}