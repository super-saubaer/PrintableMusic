/*
 * Created on 20.06.2004
 *
 */
package com.groovemanager.gui.custom;

/**
 * Instances of implementing classes can be notified when a KeyboardKey is
 * pressed or released. 
 * @author Manu Robledo
 *
 */
public interface KeyboardKeyListener {
	/**
	 * Notification that a KeyboardKey has been pressed
	 * @param key The note value of the key that has been pressed in the range
	 * from 0 to 127
	 */
	public void keyPressed(int key);
	/**
	 * Notification that a KeyboardKey has been released
	 * @param key The note value of the key that has been released in the range
	 * from 0 to 127
	 */
	public void keyReleased(int key);
}
