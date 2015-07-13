/*
 * Created on 05.04.2004
 *
 */
package com.groovemanager.actions.file;

import java.io.File;

/**
 * Instances of classes which implement this interface can be registered
 * with a FileCloseAction and then be notified when a file should be closed.
 * @author Manu Robledo
 *
 */
public interface FileCloseListener {
	/**
	 * Notification that the user requested to close a file
	 * @param f The file to be closed
	 */
	public void fileClosed(File f);
}
