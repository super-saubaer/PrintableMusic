/*
 * Created on 12.05.2004
 *
 */
package com.groovemanager.sampled.waveform;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class extends SelectableWaveFormDisplay to implement the Markable
 * interface. The Markers are made visible and editable if wanted.
 * @author Manu Robledo
 *
 */
public abstract class MarkableWaveFormDisplay extends SelectableWaveFormDisplay implements Markable{
	protected final static int MOUSE_TOLERANCE = 3;
	/**
	 * The currently selected Marker if any
	 */
	protected Marker selectedMarker;
	/**
	 * Indicates whether Markers can be added, moved or removed on this
	 * WaveFormDisplay
	 */
	protected boolean editMarkers;
	/**
	 * Set of all markers
	 */
	protected SortedSet markers = new TreeSet(new Comparator(){
		public int compare(Object o1, Object o2) {
			Marker m1 = (Marker)o1;
			Marker m2 = (Marker)o2;
			if(m1.getPosition() > m2.getPosition()) return 1;
			else if(m1.getPosition() < m2.getPosition()) return -1;
			else return 0;
		}
	});
	/**
	 * List of MarkableListeners
	 */
	protected ArrayList markableListeners = new ArrayList();
	/**
	 * Get the Marker at the given index
	 * @param index The zero-based index of the marker in question
	 * @return The Marker at the given index or <code>null</code>, if no Marker
	 * exists at the given index.
	 */
	public Marker getMarker(int index){
		return identifyMarkerIndex(index);
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.Markable#addMarker(int, java.lang.String)
	 */
	public void addMarker(int position, String name) {
		addMarker(new Marker(position, name));
	}
	/**
	 * Internal method for adding a Marker
	 * @param m The Marker to add
	 */
	protected void addMarker(Marker m) {
		markers.add(m);
		for (Iterator iter = markableListeners.iterator(); iter.hasNext();) {
			MarkableListener listener = (MarkableListener) iter.next();
			listener.markerAdded(this, m);
		}
		drawMarker(m);
	}
	/**
	 * Internal Method for removing a marker
	 * @param m The Marker to remove
	 */
	protected void removeMarker(Marker m){
		if(selectedMarker == m) deselectMarker();
		for (Iterator iter = markableListeners.iterator(); iter.hasNext();) {
			MarkableListener listener = (MarkableListener) iter.next();
			listener.markerRemoved(this, m);
		}
		eraseMarker(m);
		markers.remove(m);
	}
	/**
	 * @see com.groovemanager.sampled.waveform.Markable#removeMarkerIndex(int)
	 */
	public void removeMarkerIndex(int index) {
		Marker toRemove = identifyMarkerIndex(index);
		if(toRemove != null) removeMarker(toRemove);
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.Markable#removeMarker(java.lang.String)
	 */
	public void removeMarker(String name) {
		Marker toRemove = identifyMarker(name);
		if(toRemove != null) removeMarker(toRemove);
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.Markable#removeMarker(int)
	 */
	public void removeMarker(int position) {
		Marker toRemove = identifyMarker(position);
		if(toRemove != null) removeMarker(toRemove);
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.Markable#removeMarker(int, java.lang.String)
	 */
	public void removeMarker(int position, String name) {
		Marker toRemove = identifyMarker(name, position);
		if(toRemove != null) removeMarker(toRemove);
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.Markable#getMarkerCount()
	 */
	public int getMarkerCount() {
		return markers.size();
	}
	/**
	 * Internal method for moving a amrker
	 * @param m The Marker to move
	 * @param newPos The position to move the Marker to
	 */
	protected void moveMarker(Marker m, int newPos){
		int oldPos = m.getPosition();
		m.setPosition(newPos);
		for (Iterator iter = markableListeners.iterator(); iter.hasNext();) {
			MarkableListener listener = (MarkableListener) iter.next();
			listener.markerMoved(this, m, oldPos);
		}
		redrawMarker(m);
	}
	/**
	 * @see com.groovemanager.sampled.waveform.Markable#moveMarkerIndex(int, int)
	 */
	public void moveMarkerIndex(int index, int newPos) {
		Marker toMove = identifyMarkerIndex(index);
		if(toMove != null) moveMarker(toMove, newPos);
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.Markable#moveMarker(int, int)
	 */
	public void moveMarker(int oldPos, int newPos){
		Marker toMove = identifyMarker(oldPos);
		if(toMove != null) moveMarker(toMove, newPos);
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.Markable#moveMarker(java.lang.String, int)
	 */
	public void moveMarker(String name, int newPos){
		Marker toMove = identifyMarker(name);
		if(toMove != null) moveMarker(toMove, newPos);
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.Markable#moveMarker(java.lang.String, int, int)
	 */
	public void moveMarker(String name, int oldPos, int newPos){
		Marker toMove = identifyMarker(name, oldPos);
		if(toMove != null) moveMarker(toMove, newPos);
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.Markable#getMarkers()
	 */
	public Marker[] getMarkers(){
		Marker[] ms = new Marker[markers.size()];
		int i = 0;
		for (Iterator iter = markers.iterator(); iter.hasNext(); i++) {
			ms[i] = (Marker) iter.next();
		}
		return ms;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.Markable#addMarkableListener(com.groovemanager.sampled.waveform.MarkableListener)
	 */
	public void addMarkableListener(MarkableListener listener){
		markableListeners.add(listener);
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.waveform.Markable#removeMarkableListener(com.groovemanager.sampled.waveform.MarkableListener)
	 */
	public void removeMarkableListener(MarkableListener listener){
		markableListeners.remove(listener);
	}
	/**
	 * Draw the given Marker on the WaveFormDisplay
	 * @param m The Marker to draw
	 */
	protected abstract void drawMarker(Marker m);
	/**
	 * Erase the given marker from the WaveformDisplay
	 * @param m The Marker to erase
	 */
	protected abstract void eraseMarker(Marker m);
	/**
	 * Redraw the given marker
	 * @param m The Marker to redraw
	 */
	protected void redrawMarker(Marker m){
		if(m == selectedMarker){
			eraseHighlightedMarker(m);
			drawHighlightedMarker(m);
		}
		else{
			eraseMarker(m);
			drawMarker(m);
		}
	}
	/**
	 * Specify whether the user should be able to edit (create, move or remove)
	 * Markers on this WaveformDisplay or not
	 * @param edit true, if editing Markers should be possible, false otherwise
	 */
	public void setEditMarkers(boolean edit){
		editMarkers = edit;
	}
	/**
	 * Select the Marker at the given index 
	 * @param index The zero-based index of the Marker to select
	 */
	public void selectMarkerIndex(int index){
		Marker toSelect = identifyMarkerIndex(index);
		if(toSelect != null) selectMarker(toSelect);
	}
	/**
	 * Select the Marker at the given position
	 * @param position The position of the Marker to select
	 */
	public void selectMarker(int position){
		Marker toSelect = identifyMarker(position);
		if(toSelect != null) selectMarker(toSelect);
	}
	/**
	 * Select the Marker with the given name
	 * @param name The name of the Marker to select
	 */
	public void selectMarker(String name){
		Marker toSelect = identifyMarker(name);
		if(toSelect != null) selectMarker(toSelect);
	}
	/**
	 * Select the Marker with the given name and positon
	 * @param name The name of the Marker to select
	 * @param position The position of the marker to select
	 */
	public void selectMarker(String name, int position){
		Marker toSelect = identifyMarker(name, position);
		if(toSelect != null) selectMarker(toSelect);
	}
	/**
	 * Internal method for selecting a marker
	 * @param m The Marker to select
	 */
	protected void selectMarker(Marker m){
		if(m == selectedMarker) return;
		deselectMarker();
		selectedMarker = m;
		highlightMarker(m);
	}
	/**
	 * Internal method for identifying a Marker by its position
	 * @param position The position of the Marker to identify
	 * @return The Marker at the given position or <code>null</code>, if no
	 * Marker could be found at the given position
	 */
	protected Marker identifyMarker(int position){
		for (Iterator iter = markers.iterator(); iter.hasNext();) {
			Marker marker = (Marker) iter.next();
			if(marker.getPosition() == position) return marker;
		}
		return null;
	}
	/**
	 * Internal method for identifying a Marker by its name
	 * @param name The name of the Marker to identify
	 * @return The Marker with the given name or <code>null</code>, if no Marker
	 * could be found with the given name
	 */
	protected Marker identifyMarker(String name){
		for (Iterator iter = markers.iterator(); iter.hasNext();) {
			Marker marker = (Marker) iter.next();
			if(marker.getName().equals(name)) return marker;
		}
		return null;
	}
	/**
	 * Internal method for identifying a Marker by its name and position
	 * @param name The name of the Marker to identify
	 * @param position The position of the Marker to identify
	 * @return The Marker with the given name at the given position or
	 * <code>null</code>, if no Marker with the given name and position could be
	 * found
	 */
	protected Marker identifyMarker(String name, int position){
		for (Iterator iter = markers.iterator(); iter.hasNext();) {
			Marker marker = (Marker) iter.next();
			if(marker.getName().equals(name) && marker.getPosition() == position) return marker;
		}
		return null;
	}
	/**
	 * Internal method for identifying a Marker by its index
	 * @param index The zero-based index of the Marker to be identified
	 * @return The Marker at the given index or <code>null</code>, if no Marker
	 * exists at the given index
	 */
	protected Marker identifyMarkerIndex(int index){
		int i = 0;
		for (Iterator iter = markers.iterator(); iter.hasNext();) {
			Marker marker = (Marker) iter.next();
			if(i == index) return marker;
			i++;
		}
		return null;
	}
	/**
	 * Deselect the currently selected Marker, if any
	 *
	 */
	public void deselectMarker(){
		if(selectedMarker != null) deHighlightMarker(selectedMarker);
		selectedMarker = null;
	}
	/**
	 * Highlight the given Marker to indicate that this one is the currently
	 * selected Marker
	 * @param m The Marker to highlight
	 */
	private void highlightMarker(Marker m){
		eraseMarker(m);
		drawHighlightedMarker(m);
	}
	/**
	 * Dehighlight the given marker to indicate this one is no more the
	 * currently selected Marker 
	 * @param m The Marker to dehighlight
	 */
	private void deHighlightMarker(Marker m){
		eraseHighlightedMarker(m);
		drawMarker(m);
	}
	/**
	 * Draw the given Marker in selected state
	 * @param m The Marker to draw
	 */
	protected abstract void drawHighlightedMarker(Marker m);
	/**
	 * Erase the given Marker that has been drawn in selected state before
	 * @param m The Marker to erase
	 */
	protected abstract void eraseHighlightedMarker(Marker m);
	
	protected synchronized void mouseDown(int realX, int realY,
			boolean shiftPressed, boolean ctrlPressed) {
		if(!editMarkers) super.mouseDown(realX, realY, shiftPressed, ctrlPressed);
		else if(ctrlPressed){
			Marker nearest = getMarkerFromMousePos(realX);
			if(nearest == null){
				int pos = mouseToData(realX);
				addMarker(pos, "");
				selectMarker(pos);
				mouseDown = true;
			}
			else{
				removeMarker(nearest);
			}
		}
		else if(shiftPressed){
			Marker nearest = getMarkerFromMousePos(realX);
			if(nearest == null) deselectMarker();
			else{
				mouseDown = true;
				selectMarker(nearest);
			}
		}
		else if(selectedMarker != null) deselectMarker();
		else super.mouseDown(realX, realY, shiftPressed, ctrlPressed);
	}
	protected synchronized void mouseMove(int realX, int realY,
			boolean shiftPressed, boolean ctrlPressed) {
		if(!editMarkers) super.mouseMove(realX, realY, shiftPressed, ctrlPressed);
		else if(selectedMarker != null){
			if(mouseDown) moveMarker(selectedMarker, mouseToData(realX));
		}
		else super.mouseMove(realX, realY, shiftPressed, ctrlPressed);
	}
	protected synchronized void mouseUp(int realX, int realY,
			boolean shiftPressed, boolean ctrlPressed) {
		mouseMove(realX, realY, shiftPressed, ctrlPressed);
		if(mouseDown){
			if(editMarkers && selectedMarker != null) mouseDown = false;
			else super.mouseUp(realX, realY, shiftPressed, ctrlPressed);
		}
	}
	/**
	 * Get the currently selected Marker
	 * @return The currently selected Marker or <code>null</code>, if no Marker
	 * is currently selected
	 */
	public Marker getSelectedMarker(){
		if(editMarkers) return selectedMarker;
		else return null;
	}
	/**
	 * Get the Marker that is nearest to the given mouse position, but only if
	 * it is not more than <code>MOUSE_TOLERANCE</code> pixels away.
	 * @param mousePos The mouse x position
	 * @return The nearest Marker to the given mouse position or
	 * <code>null</code>, if the nearest Marker is too far away
	 */
	protected Marker getMarkerFromMousePos(int mousePos){
		int minDiff = Integer.MAX_VALUE;
		Marker nearest = null;
		int min = mouseToData(Math.max(0, mousePos - MOUSE_TOLERANCE));
		int max = Math.min(mouseToData(mousePos + MOUSE_TOLERANCE), getTotalLength());
		int dataX = mouseToData(mousePos);
		for (Iterator iter = markers.iterator(); iter.hasNext();) {
			Marker marker = (Marker) iter.next();
			int pos = marker.getPosition();
			if(pos >= min && pos <= max){
				int diff = Math.abs(pos - dataX);
				if(diff < minDiff){
					minDiff = diff;
					nearest = marker;
				}
			}
		}
		return nearest;
	}
}