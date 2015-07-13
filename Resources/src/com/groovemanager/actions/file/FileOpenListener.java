/*
 * Created on 22.03.2004
 *
 */
package com.groovemanager.actions.file;

import java.io.File;

/**
 * Instances of classes which implement this interface can be registered
 * with a FileOpenAction and then be notified when a file should be opened.
 * @author Manu Robledo
 *
 */
public interface FileOpenListener {
	/**
	 * Notification that the user desired to open a file. When multiple files
	 * should be opened, this method will be called once for each file.
	 * @param f The file to be opened
	 */
	public void fileOpened(File f);
}
