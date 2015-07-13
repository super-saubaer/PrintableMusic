/*
 * Created on 12.05.2004
 *
 */
package com.groovemanager.sampled.waveform;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class extends AbstractWaveFormDisplay to implement the Selectable
 * interface. The position and selection of the Display are made visible.
 * @author Manu Robledo
 *
 */
public abstract class SelectableWaveFormDisplay extends AbstractWaveFormDisplay implements Selectable{
	/**
	 * Current selection큦 left border 
	 */
	protected int left,
	/**
	 * Current selection큦 right border
	 */
	right,
	/**
	 * Current position
	 */
	pos,
	/**
	 * For situations of continuous selection change: The point at which the
	 * mouse button was pressed 
	 */
	selStartedAt;
	/**
	 * List of SelectableListeners registered with this Display
	 */
	protected ArrayList selectableListeners = new ArrayList();
	/**
	 * Indicates whether the mouse button is currently pressed or not
	 */
	protected boolean mouseDown = false;
	
	public Selection getSelection() {
		return new Selection(left, right);
	}
	public void setSelection(Selection sel) {
		left = sel.left;
		right = sel.right;
		int leftPixel = leftOffset + dataToPixel(left);
		int rightPixel = leftOffset + dataToPixel(right);
		redrawSelection(leftPixel, topOffset, rightPixel - leftPixel, getUsableDisplayHeight());
		for (Iterator iter = selectableListeners.iterator(); iter.hasNext();) {
			SelectableListener listener = (SelectableListener) iter.next();
			listener.selectionChanged(this, sel);
		}
	}
	public int getPosition() {
		return pos;
	}
	/**
	 * Zoom and scroll this display to the current selection
	 *
	 */
	public void showSelection(){
		Selection sel = getSelection();
		if(sel.left == sel.right) return;
		int length = sel.right - sel.left;
		int total = getTotalLength();
		zoomFactor = total / (double)length;
		double max = total - length;
		scrollFactor = sel.left / max;
		redraw();
	}
	public void setPosition(int pos) {
		this.pos = pos;
		redrawPosition(leftOffset + dataToPixel(pos), topOffset, getUsableDisplayHeight());
		for (Iterator iter = selectableListeners.iterator(); iter.hasNext();) {
			SelectableListener listener = (SelectableListener) iter.next();
			listener.positionChanged(this, pos);
		}
	}
	public void addSelectableListener(SelectableListener listener) {
		selectableListeners.add(listener);
	}
	public void removeSelectableListener(SelectableListener listener) {
		selectableListeners.remove(listener);
	}
	/**
	 * Draw the selection in the given area
	 * @param x The x-coordinate of the area
	 * @param y The y-coordinate of the area
	 * @param width The width of the area
	 * @param height The height of the area
	 */
	protected abstract void drawSelection(int x, int y, int width, int height);
	/**
	 * Hide the current selection
	 *
	 */
	protected abstract void eraseSelection();
	/**
	 * Draw the position pointer at the given position
	 * @param x The x-coordinate of the position pointer
	 * @param y The y-coordinate of the position pointer큦 top
	 * @param height The height of the position pointer
	 */
	protected abstract void drawPosition(int x, int y, int height);
	/**
	 * Hide the position pointer
	 *
	 */
	protected abstract void erasePosition();
	/**
	 * Adapt the visible selection to the given area or - if it was not visible
	 * before - draw it to the given area.
	 * @param x The x-coordinate of the area
	 * @param y The y-coordinate of the area
	 * @param width The width of the area
	 * @param height The height of the area
	 */
	protected abstract void redrawSelection(int x, int y, int width, int height);
	/**
	 * Move the visible position pointer to the specified position or - if it
	 * was not visible before - draw it to the given position 
	 * @param x The x-coordinate of the position pointer
	 * @param y The y-coordinate of the position pointer큦 top
	 * @param height The height of the position pointer
	 */
	protected void redrawPosition(int x, int y, int height){
		erasePosition();
		drawPosition(x, y, height);
	}
	/**
	 * Notification about a mouse button being pressed. Subclasses must ensure
	 * with MouseListeners, that this method is called, when a mouse button is
	 * pressed.
	 * @param realX The x-coordinate of where the mouse button was pressed
	 * @param realY The y-coordinate of where the mouse button was pressed
	 * @param shiftPressed true, if the Shift-Button is currently pressed,
	 * false otherwise
	 * @param ctrlPressed true, if the CTRL-Button is currently pressed, false
	 * otherwise
	 */
	protected synchronized void mouseDown(int realX, int realY, boolean shiftPressed, boolean ctrlPressed){
		int dataPos = mouseToData(realX); 
		setPosition(dataPos);
		setSelection(new Selection(dataPos));
		selStartedAt = dataPos;
		mouseDown = true;
	}
	/**
	 * Notification about the mouse being moved. Subclasses must ensure
	 * with MouseListeners, that this method is called, when the mouse is moved.
	 * @param realX The x-coordinate of where the mouse was moved to
	 * @param realY The y-coordinate of where the mouse was moved to
	 * @param shiftPressed true, if the Shift-Button is currently pressed,
	 * false otherwise
	 * @param ctrlPressed true, if the CTRL-Button is currently pressed, false
	 * otherwise
	 */
	protected synchronized void mouseMove(int realX, int realY, boolean shiftPressed, boolean ctrlPressed){
		if(mouseDown){
			int dataPos = mouseToData(realX);
			setSelection(new Selection(dataPos, selStartedAt));
		}
	}
	/**
	 * Notification about a mouse button being released. Subclasses must ensure
	 * with MouseListeners, that this method is called, when a mouse button is
	 * released.
	 * @param realX The x-coordinate of where the mouse button was released
	 * @param realY The y-coordinate of where the mouse button was released
	 * @param shiftPressed true, if the Shift-Button is currently pressed,
	 * false otherwise
	 * @param ctrlPressed true, if the CTRL-Button is currently pressed, false
	 * otherwise
	 */
	protected synchronized void mouseUp(int realX, int realY, boolean shiftPressed, boolean ctrlPressed){
		mouseDown = false;
		for (Iterator iter = selectableListeners.iterator(); iter.hasNext();) {
			SelectableListener listener = (SelectableListener) iter.next();
			listener.selectionPermanentChanged(this, new Selection(left, right));
		}
	}
}