/*
 * Created on 05.04.2004
 *
 */
package com.groovemanager.actions.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * This subclass of Action is used to close an open file.
 * A File needs to be assigned to an instance of this class before it can
 * be performed. When performed, an instance of this class will do nothing
 * else than notify all registeres FileCLoseListeners that the file
 * should be closed. 
 * @author Manu Robledo
 *
 */
public class FileCloseAction extends Action {
	/**
	 * The file currently associated to this Action
	 */
	private File file;
	/**
	 * List of FileCloseListeners registered with this Action
	 */
	private ArrayList listeners = new ArrayList();
	
	/**
	 * Constructs a new FileCloseAction
	 * @param text The name of the Action
	 */
	public FileCloseAction(String text) {
		super(text);
	}
	/**
	 * Constructs a new FileCloseAction
	 * @param text The name of the Action
	 * @param image The image assigned to this Action
	 */
	public FileCloseAction(String text, ImageDescriptor image) {
		super(text, image);
	}
	/**
	 * Constructs a new FileCloseAction
	 * @param text The name of the Action
	 * @param style The style constant used by this Action
	 */
	public FileCloseAction(String text, int style) {
		super(text, style);
	}
	
	/**
	 * Get the file currently assigned to this Action, that is the
	 * file that should be closed
	 * @return The file to be closed
	 */
	public File getFile(){
		return file;
	}
	
	/**
	 * Assign a file to this Action. If the argument is <code>null</code>, the
	 * action will be disabled, otherwise it will be enabled
	 * @param f The file to be closed or <code>null</code>
	 */
	public void setFile(File f){
		file = f;
		setEnabled(file != null);
	}
	
	/**
	 * Register a listener with this Action that will be notified when the
	 * Action is performed.
	 * @param listener The FileCloseListener to add
	 */
	public void addFileCloseListener(FileCloseListener listener){
		listeners.add(listener);
	}
	
	/**
	 * Remove a registered listener from this Action
	 * @param listener The FileCloseListener to remove
	 */
	public void removeFileCloseListener(FileCloseListener listener){
		listeners.remove(listener);
	}
	
	/**
	 * Perform this Action. When no file is assigned to this Action,
	 * nothing will happen. Else all registeres listeners will be
	 * notified that this file should be closed.
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run(){
		if(file == null) return;
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			FileCloseListener element = (FileCloseListener) iter.next();
			element.fileClosed(file);
		}
	}
}