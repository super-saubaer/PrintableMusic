/*
 * Created on 20.06.2004
 *
 */
package com.groovemanager.app.mc909se;

import org.eclipse.jface.wizard.Wizard;

/**
 * This Wizard is used for exporting a Rhythm set to the Mc909.
 * @author Manu Robledo
 *
 */
public class ExportRhythmSetWizard extends Wizard{
	/**
	 * The first page of this wizard
	 */
	ExportSamplesPage exportSamplesPage = new ExportSamplesPage("Export samples");
	/**
	 * The second page of this wizard
	 */
	OnlyMessagePage createRhythmSetPage = new OnlyMessagePage("Exit USB Mode", "Now exit USB mode on the MC-909. In order to use the exported samples, you will have to load the samples into RAM. This can be done either by rebooting the MC-909 or by selecting the samples in the Sample List and pressing Load (Shift + F3). For Rhythm Set creation now make sure you have established a MIDI Out connection to the MC-909 with the MIDI out port selected in the settings dialog. When finished, press \"Next\".");
	/**
	 * The third page of this wizard
	 */
	PlaySequencePage playSequencePage = new PlaySequencePage("play");
	/**
	 * Indicates whether this wizard can be finished
	 */
	boolean canFinish = false;

	public boolean performFinish() {
		return true;
	}
	
	public void addPages() {
		addPage(exportSamplesPage);
		addPage(createRhythmSetPage);
		addPage(playSequencePage);
	}
	
	public boolean canFinish() {
		return canFinish;
	}
}
