package com.groovemanager.sampled.waveform;

/**
 * A SelectableListener can be added to a Selectable instance to be notified
 * about changes to its Selection or position. There are two different types
 * of changes: Those that are happening in a situation of continuing changes,
 * so that they are likely to be followed by another change soon and those
 * that are happening at the end of such a situation and are likely to be
 * the last change for a while.<br>
 * A listener should always decide whether it has to be notified about
 * each single change even if it is only a temporary one if it only has to know
 * if a permanent change is made to the Selectable.<br>
 * For Selection changes there are the two methods <code>selectionChanged</code>
 * and <code>selectionPermanentChanged</code>. The first will be called whenever
 * a change to the selection is made and the second one only when this change
 * seems to be permanent for the next while.<br>
 * Because it is likely that a Selectable큦 position may change very often in
 * some situations, another approach is taken for position changes: When the
 * Selectable comes into a situation, where it is likely that the position
 * will change continuously, <code>positionWillChange()</code> is called, so
 * that the listener can decide, whether it wants to start a separate Thread for
 * continuous observation of the Selectable큦 position or not. When this
 * situation is over and no continuous changes are expected any more,
 * <code>positionWontChange()</code> is called. This informs the listener that
 * it can the continuous observation. <code>positionChanged()</code> will only
 * be called when a change seems to be permanent for a while. 
 * @author Manu Robledo
 *
 */
public interface SelectableListener {
	/**
	 * Notification about a change of the Selectable큦 selection that is likely
	 * to be permanent for a while.
	 * @param s The Selectable that sends this notification
	 * @param sel The new Selection
	 */
	public void selectionPermanentChanged(Selectable s, Selection sel);
	/**
	 * Notification about a change of the Selectable큦 selection that is likely
	 * be followed by more changes soon.
	 * @param s The Selectable that sends this notification
	 * @param sel The new Selection
	 */
	public void selectionChanged(Selectable s, Selection sel);
	/**
	 * Notification about a change of the Selectable큦 position that is likely
	 * to be permanent for a while.
	 * @param s The Selectable that sends this notification
	 * @param pos The new position
	 */
	public void positionChanged(Selectable s, int pos);
	/**
	 * Notification that the Selectable is going into a state where continuous
	 * change of its position is expected.
	 * @param s The Selectable that sends this notification
	 */
	public void positionWillChange(Selectable s);
	/**
	 * Notification that the Selectable is leaving a state where continuous
	 * change of its position was expected.
	 * @param s The Selectable that sends this notification
	 */
	public void positionWontChange(Selectable s);
}