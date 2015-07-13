/*
 * Created on 13.05.2004
 *
 */
package com.groovemanager.thread;

/**
 * A ProgressListener can be added to a ProgressThread to be notified about
 * the progress status of the operation
 * @author Manu Robledo
 *
 */
public interface ProgressListener {
	/**
	 * The total length of the operation
	 * @param i The length, the operation will take. The unit to use for this
	 * value is free defineable. If the total length of the operation can not
	 * be specified, -1 will be returned to indicate "unknown".
	 */
	public void progressTotal(int i);
	/**
	 * The part of te operation elapsed so far
	 * @param i The length of the part of the operation elapsed so far. The
	 * unit to use for this value is free defineable
	 */
	public void progressElapsed(int i);
	/**
	 * Notification about the start of the operation
	 *
	 */
	public void progressStarted();
	/**
	 * Notification that the operation has been cancelled before finishing
	 *
	 */
	public void progressCancelled();
	/**
	 * Notification htat the operation has been finished
	 *
	 */
	public void progressFinished();
}