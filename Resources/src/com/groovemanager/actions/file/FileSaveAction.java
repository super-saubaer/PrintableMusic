/*
 * Created on 05.04.2004
 *
 */
package com.groovemanager.actions.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.action.Action;

/**
 * This subclass of Action is used to save a file without user interaction.
 * Before being performed, a File has to be assigned to this Action.
 * When performed, an instance of this class will notify all registeres
 * FileSaveListeners that the file should be saved.
 * @author Manu Robledo
 *
 */
public class FileSaveAction extends Action {
	/**
	 * The file currently assigned to this Action
	 */
	private File file;
	/**
	 * List of FileSaveListeners registeres to this Action
	 */
	protected ArrayList listeners = new ArrayList();
	/**
	 * Constructs a new FIleSaveAction with the given name
	 * @param name This Action´s name
	 */
	public FileSaveAction(String name){
		super(name);
	}
	/**
	 * Register a FileSaveListener with this Action
	 * @param l The FileSaveListener to register
	 */
	public void addFileSaveListener(FileSaveListener l){
		listeners.add(l);
	}
	/**
	 * Remove a registered FileSaveListener to this Action
	 * @param l The FileSaveListener to be removed
	 */
	public void removeFileSaveListener(FileSaveListener l){
		listeners.remove(l);
	}
	/**
	 * Set the file that should be saved. If the argument is <code>null</code>,
	 * the action will be disabled. Otherwise it will be enabled.
	 * @param f The file to be saved or <code>null</code>
	 */
	public void setFile(File f){
		file = f;
		setEnabled(file != null);
	}
	/**
	 * Get the file currently assigned to this Action
	 * @return The file to be saved
	 */
	public File getFile(){
		return file;
	}
	/**
	 * Perform this Action.
	 * If no file has been assigned to this Action, nothing will happen.
	 * Else all registered listeners will be notified that this file should
	 * be saved.
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run(){
		if(file == null) return;
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			FileSaveListener listener = (FileSaveListener) iter.next();
			listener.fileSaved(file, file);
		}
	}
}
