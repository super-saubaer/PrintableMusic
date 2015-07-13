package com.groovemanager.sampled.waveform;

/**
 * A Selectable instance always has a Selection that can be changed and queried.
 * A Selection has a left and a right border. A Selection can also be of zero
 * length when left and right border are the same. A Selectable has also a
 * position that can be set and queried. The position is a single value that is
 * independent from the Selection.
 * @author Manu Robledo
 *
 */
public interface Selectable {
	/**
	 * Get the current Selection
	 * @return The current Selection
	 */
	public Selection getSelection();
	/**
	 * Set the Selection
	 * @param sel The new Selection
	 */
	public void setSelection(Selection sel);
	/**
	 * Get the current position
	 * @return The current position
	 */
	public int getPosition();
	/**
	 * Set the position
	 * @param pos The new position
	 */
	public void setPosition(int pos);
	/**
	 * Add a SelectableListener that will be notified of changes to this
	 * Selectable´s Selection or position
	 * @param listener The SelectableListener to add
	 */
	public void addSelectableListener(SelectableListener listener);
	/**
	 * Remove a SelectableListener from this Selectable
	 * @param listener The SelectableListener to remove
	 */
	public void removeSelectableListener(SelectableListener listener);
}