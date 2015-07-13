package com.groovemanager.gui.custom;

/**
 * This interface represents a single Key on a musical keyboard. It can be
 * selected (pressed) or not.
 * @author Manu Robledo
 *
 */
public interface KeyboardKey {
	/**
	 * Constants for the number of a key inside one octave
	 */
	public final static int
		C = 0,
		CIS = 1,
		D = 2,
		DIS = 3,
		E = 4,
		F = 5,
		FIS = 6,
		G = 7,
		GIS = 8,
		A = 9,
		AIS = 10,
		B = 11;
	/**
	 * Constants for the offsets of each octave
	 */
	public final static int
		OCTAVE_MINUS_1 = 0,
		OCTAVE_0 = 12,
		OCTAVE_1 = 24,
		OCTAVE_2 = 36,
		OCTAVE_3 = 48,
		OCTAVE_4 = 60,
		OCTAVE_5 = 72,
		OCTAVE_6 = 84,
		OCTAVE_7 = 96,
		OCTAVE_8 = 108,
		OCTAVE_9 = 120;
	/**
	 * Get the note value represented by this key.
	 * @return The note value of this key in the range from 0
	 * (<code>OCTAVE_MINUS_1 + C</code>) to 127
	 * (<code>OCTAVE_9 + G</code>.
	 */
	public int getKey();
	/**
	 * Press this key.
	 *
	 */
	public void press();
	/**
	 * Release this key.y
	 *
	 */
	public void release();
	/**
	 * Return whether this key is currently pressed or not
	 * @return true if the key has recently been pressed and since then not
	 * been released, false otherwise
	 */
	public boolean isPressed();
	/**
	 * Add a KeyListener that will be notified, when this key is pressed or
	 * released
	 * @param listener The KeyListener to add
	 */
	public void addKeyListener(KeyboardKeyListener listener);
	/**
	 * Remove a previously registeres KeyListener from this key 
	 * @param listener The KeyListener to remove
	 */
	public void removeKeyListener(KeyboardKeyListener listener);
	public void setHold(boolean hold);
	public boolean getHold();
}
