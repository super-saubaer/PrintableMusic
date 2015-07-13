package com.groovemanager.gui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * An EditorWindow represents an Application Window that can be used as
 * Top level Window (standalone) or inside another application.
 * @author Manu Robledo
 *
 */
public abstract class EditorWindow extends ApplicationWindow{
	/**
	 * Constructs a new EditorWindow which will use the given Shell as
	 * parent Shell
	 * @param shell The Shell to be used as the EditorWindow´s parent Shell
	 */
	public EditorWindow(Shell shell){
		super(shell);
	}
	/**
	 * Constructs a new Top Level EditorWindow
	 *
	 */
	protected EditorWindow(){
		super(null);
	}
	/**
	 * This methos must be implemented to create the EditorWindow´s contents
	 */
	protected abstract Control createContents(Composite parent);
	/**
	 * Show an error message in a new MessageDialog
	 * @param m The error message to display
	 */
	public void errorMessage(String m){
		MessageDialog d = new MessageDialog(getShell(), "Error", null, m, SWT.ICON_ERROR, new String[]{"OK"}, 0);
		d.open();
	}
}
