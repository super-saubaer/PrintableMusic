/*
 * Created on 12.05.2004
 *
 */
package com.groovemanager.sampled.waveform;

/**
 * Instances of implementing classes can have an unlimited number of Markers
 * added to themselves. The Markers can be moved or removed. Each Marker will
 * be located at a specific position so this Markable instance should be able
 * to deal with something like position.
 * @author Manu Robledo
 *
 */
public interface Markable {
	/**
	 * Add a Marker with the given name to this Markable at the given position
	 * @param position The position where the Marker should be created
	 * @param name The name of the Marker
	 */
	public void addMarker(int position, String name);
	/**
	 * Remove the Marker at the given index
	 * @param index The zero-based index of the Marker to remove
	 */
	public void removeMarkerIndex(int index);
	/**
	 * Remove a marker identified by the given name
	 * @param name The name of the Marker to remove
	 */
	public void removeMarker(String name);
	/**
	 * Remove a marker from the given position
	 * @param position The position from where the Marker should be removed
	 */
	public void removeMarker(int position);
	/**
	 * Remove a Marker identified by the given name and located at the given
	 * position 
	 * @param position The position from where the Marker should be removed
	 * @param name The name of the Marker to remove
	 */
	public void removeMarker(int position, String name);
	/**
	 * Get the current number of Markers contained in this Markable
	 * @return The number of Markers contained in this Markable
	 */
	public int getMarkerCount();
	/**
	 * Get all Markers currently assigned to this Markable
	 * @return An Array of all Markers currently assigned to this Markable
	 */
	public Marker[] getMarkers();
	/**
	 * Move the Marker at the given index to the given new position
	 * @param index The zero-based index of the Marker to move
	 * @param newPos The position to move the Marker to
	 */
	public void moveMarkerIndex(int index, int newPos);
	/**
	 * Move a Marker from the specified position to a new position
	 * @param oldPos The position at which the Marker is currently positioned 
	 * @param newPos The position to which the Marker should be moved
	 */
	public void moveMarker(int oldPos, int newPos);
	/**
	 * Move the Marker identified by the given name to the specified position
	 * @param name The name of the Marker to move
	 * @param newPos The position to which the Marker should be moved
	 */
	public void moveMarker(String name, int newPos);
	/**
	 * Move the Marker identified by the given name and currently located at
	 * the given position to the a new position
	 * @param name The name of the Marker to move
	 * @param oldPos The position at which the Marker is currently located
	 * @param newPos The position to which the Marker should be moved
	 */
	public void moveMarker(String name, int oldPos, int newPos);
	/**
	 * Add a MarkableListener that will be notified of any changes made to
	 * this Markable instance
	 * @param listener The MarkableListener to add
	 */
	public void addMarkableListener(MarkableListener listener);
	/**
	 * Remove a MarkableListener
	 * @param listener The MarkableListener to remove
	 */
	public void removeMarkableListener(MarkableListener listener);
}