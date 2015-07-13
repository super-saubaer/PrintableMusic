/*
 * Created on 09.06.2004
 *
 */
package com.groovemanager.gui.custom.controls;

/**
 * Implementers of this interface should be capable of displaying
 * javax.sound.sampled.Control instances for user modification
 * @author Manu Robledo
 *
 */
public interface ControlContainer {
	/**
	 * Appl the changes made by the user to the underlying Control instance
	 *
	 */
	public void apply();
	/**
	 * Reset the GUI elements to the value of the underlying Control instance
	 *
	 */
	public void reset();
	/**
	 * Set this Container into auto-apply mode or out of auto-apply mode. In
	 * auto-apply mode all changes made by the user will be directly made to
	 * the underlying Control instance 
	 * @param auto true if auto-apply should be turned on, false otherwise
	 */
	public void setAutoApply(boolean auto);
}