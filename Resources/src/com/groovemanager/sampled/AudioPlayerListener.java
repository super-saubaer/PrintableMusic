package com.groovemanager.sampled;

/**
 * Instances of classes implementing this interfaces can be notified of changes
 * to an AudioPlayer´s status or to its loop state
 * @author Manu Robledo
 *
 */
public interface AudioPlayerListener{
	/**
	 * Notification about a status change
	 * @param type The type of change
	 */
	public void statusChanged(int type);
	/**
	 * Notification about a change of the loop state
	 * @param loop The new loop state
	 */
	public void loopChanged(boolean loop);
}
