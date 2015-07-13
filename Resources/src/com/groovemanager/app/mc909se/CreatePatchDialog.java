/*
 * Created on 21.06.2004
 *
 */
package com.groovemanager.app.mc909se;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Shell;

import com.groovemanager.midi.MIDIManager;

/**
 * This WizardDialog is used when a patch should be created after sample export
 * @author Manu Robledo
 *
 */
public class CreatePatchDialog extends WizardDialog {
	/**
	 * The wizard shown in this dialog
	 */
	CreatePatchWizard wizard;
	/**
	 * Indicates whether the exported sample was stereo or not
	 */
	boolean isStereo;
	/**
	 * Indicates whether the sample has been exported to user area (true) or
	 * card area (false)
	 */
	boolean isUser;
	/**
	 * The sample nr the sample was exported to.
	 */
	int sampleNr;
	/**
	 * Create a new CreatePatchWizard
	 * @param parentShell The parent shell for the dialog
	 * @param patchName The name to be used for the patch
	 * @param isUser true, if the sample was exported to user area, false
	 * otherwise
	 * @param sampleNr The sample nr the sample was exported to.
	 * @param stereo true, if the exported sample is stereo, false otherwise
	 */
	public CreatePatchDialog(Shell parentShell, String patchName, boolean isUser, int sampleNr, boolean stereo) {
		super(parentShell, new CreatePatchWizard(patchName));
		wizard = (CreatePatchWizard)getWizard();
		isStereo = stereo;
		this.isUser = isUser;
		this.sampleNr = sampleNr;
	}
	protected void nextPressed() {
		if(getCurrentPage() == wizard.selectPatchPage){
			try {
				int part = wizard.selectPatchPage.partSelect.getSelectionIndex();
				wizard.writePatchPage.setPart(part);
				MIDIManager.getDefault().sendController(part, (byte)0, (byte)81);
				byte cont;
				if(wizard.selectPatchPage.bankSelect.getSelectionIndex() == 0) cont = 0;
				else cont = 32;
				int prog = wizard.selectPatchPage.patchSelect.getSelectionIndex();
				if(prog > 127){
					cont++;
					prog -= 128;
				}
				
				MIDIManager.getDefault().sendController(part, (byte)32, cont);
				MIDIManager.getDefault().sendProgramChange(part, (byte)prog);
				
				MIDIManager.getDefault().sendSysEx(MC909SysEx.createPatchLevelMessage(part, (byte)127));
				MIDIManager.getDefault().sendSysEx(MC909SysEx.createPatchNameMessage(part, wizard.patchName));
				int right = 0;
				if(isStereo) right = sampleNr + 1; 
				MIDIManager.getDefault().sendSysEx(MC909SysEx.createPatchWaveMessage(part, 0, isUser, sampleNr, right));
				
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
			}
			super.nextPressed();
		}
		else{
			super.nextPressed();
			((WizardPage)getCurrentPage()).setPageComplete(true);
		}
	}
}
