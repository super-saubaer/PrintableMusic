/*
 * Created on 19.05.2004
 *
 */
package com.groovemanager.actions.file;

/**
 * Instances of classes which implement this interface can be registered
 * with a FileNewAction and then be notified when a new file should be created.
 * @author Manu Robledo
 *
 */
public interface FileNewListener {
	/**
	 * Notification that the user requested to create a new file.
	 *
	 */
	public void newFile();
}
