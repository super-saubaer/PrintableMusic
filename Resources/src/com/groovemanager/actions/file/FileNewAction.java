/*
 * Created on 19.05.2004
 *
 */
package com.groovemanager.actions.file;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * This subclass of Action is used to create a new file.
 * When performed, an instance of this class will do nothing
 * else than notify all registeres FileNewListeners that a new
 * file should be created. 
 * @author Manu Robledo
 *
 */
public class FileNewAction extends Action {
	/**
	 * List of FileNewListeners registered with this Action
	 */
	private ArrayList listeners = new ArrayList();
	/**
	 * Constructs a new FileNewAction with the given name
	 * @param text This Action큦 name
	 */
	public FileNewAction(String text) {
		super(text);
	}
	/**
	 * Constructs a new FileNewAction with the given name and the
	 * given image.
	 * @param text This Action큦 name
	 * @param image This Action큦 image
	 */
	public FileNewAction(String text, ImageDescriptor image) {
		super(text, image);
	}
	/**
	 * Constructs a new FileNewAction with the given name using the
	 * given style constant
	 * @param text This Action큦 name
	 * @param style The style constant to be used by this Action
	 */
	public FileNewAction(String text, int style) {
		super(text, style);
	}
	/**
	 * Perform this Action. All registered FileNewListeners will be
	 * notified that a new file should be opened.
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run(){
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			FileNewListener listener = (FileNewListener) iter.next();
			listener.newFile();
		}
	}
	/**
	 * Register a FileNewListener with this Action.
	 * @param listener The FileNewListener to register
	 */
	public void addFileNewListener(FileNewListener listener){
		listeners.add(listener);
	}
	/**
	 * Remove a registered FileNewListener from this Action
	 * @param listener The FileNewListener to remove.
	 */
	public void removeFileNewListener(FileNewListener listener){
		listeners.remove(listener);
	}
}