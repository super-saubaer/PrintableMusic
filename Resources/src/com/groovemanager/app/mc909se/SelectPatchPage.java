/*
 * Created on 15.06.2004
 *
 */
package com.groovemanager.app.mc909se;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * This class is used in the CreatePatchWizard as first page 
 * @author Manu Robledo
 *
 */
public class SelectPatchPage extends WizardPage {
	/**
	 * Popup for selection of the part
	 */
	Combo partSelect,
	/**
	 * Popup for selection of the bank
	 */
	bankSelect,
	/**
	 * Popup for selection of the patch number
	 */
	patchSelect;
	/**
	 * The patch큦 name
	 */
	String patchName;
	/**
	 * Create a new SelectPatchPage
	 * @param pageName The page큦 name
	 * @param patchName The patch큦 name
	 */
	public SelectPatchPage(String pageName, String patchName) {
		super(pageName);
		this.patchName = patchName;
	}
	/**
	 * Create a new SelectPatchPage
	 * @param pageName The page큦 name
	 * @param title The page title
	 * @param titleImage The title image
	 * @param patchName The patch큦 name
	 */
	public SelectPatchPage(String pageName, String title,
			ImageDescriptor titleImage, String patchName) {
		super(pageName, title, titleImage);
		this.patchName = patchName;
	}
	public void createControl(Composite parent) {
		setMessage("Select the part and the patch location of the patch to be created for Sample " + patchName);
		setTitle("Create patch for " + patchName);
		
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		
		new Label(comp, SWT.NONE).setText("Part");
		
		partSelect = new Combo(comp, SWT.READ_ONLY | SWT.DROP_DOWN);
		for(int i = 0; i < 16; i++){
			partSelect.add("" + (i + 1));
		}
		partSelect.select(0);
		
		new Label(comp, SWT.NONE).setText("Patch");
		
		Composite patchComp = new Composite(comp, SWT.NONE);
		GridLayout gl = new GridLayout(2, false);
		gl.horizontalSpacing = 0;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		patchComp.setLayout(gl);
		patchComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		bankSelect = new Combo(patchComp, SWT.READ_ONLY | SWT.DROP_DOWN);
		bankSelect.add("User");
		bankSelect.add("Card");
		bankSelect.select(0);
		
		patchSelect = new Combo(patchComp, SWT.READ_ONLY | SWT.DROP_DOWN);
		for(int i = 0; i < 256; i++){
			patchSelect.add("" + (i + 1));
		}
		setControl(comp);
		patchSelect.select(0);
	}
}
