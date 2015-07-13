/*
 * Created on 12.05.2004
 *
 */
package com.groovemanager.sampled.waveform;

/**
 * A MarkableListener can be added to a Markable instance to be notified when
 * Markers are being moved, added to or removed from it.
 * @author Manu Robledo
 *
 */
public interface MarkableListener {
	/**
	 * Notification that a Marker has been added to the observed Markable 
	 * @param markable The Markable to which the Marker was added
	 * @param marker The Marker that was added
	 */
	public void markerAdded(Markable markable, Marker marker);
	/**
	 * Notification that a Marker has been removed from the observed Markable
	 * @param markable The Markable from which the Marker has been removed
	 * @param marker The Marker that has been removed
	 */
	public void markerRemoved(Markable markable, Marker marker);
	/**
	 * Notification that a Marker has been moved
	 * @param markable The Markable to which the Marker belongs
	 * @param marker The Marker that has been moved
	 * @param positionBefore The position the Marker had before moving it
	 */
	public void markerMoved(Markable markable, Marker marker, int positionBefore);
}