/*
 * Created on 11.05.2004
 *
 */
package com.groovemanager.sampled.waveform;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A WaveFormDisplay can be used to display WaveForm objects provided by a
 * WaveFormProvider. This abstract class is intended to be independent of any
 * graphics framework so that implementations are possible in Swing, SWT or
 * whatever.
 * @author Manu Robledo
 *
 */
public abstract class AbstractWaveFormDisplay {
	/**
	 * The scroll factor: 0 means scroll to the left, 0.5 means scroll to
	 * center and 1 means scroll to the right. Any inbetween value is possible.
	 */
	protected double scrollFactor = 0.5;
	/**
	 * Factor describing the current zoom state of this WaveFormDisplay. A
	 * zoom factor of 1.0 means, that the whole WaveForm is visible, a greater
	 * value means zooming in and a smaller value zooming out.
	 */
	protected double zoomFactor = 1.0;
	/**
	 * Left and right offset in pixels
	 */
	protected int leftOffset = 2,
	/**
	 * Top and bottom offset in pixels
	 */
	topOffset = 2,
	/**
	 * Spacing betweeen two channels in pixels
	 */
	channelSpacing = 2;
	/**
	 * The source provider from which the WaveForm data is being provided
	 */
	protected WaveFormProvider source;
	/**
	 * List of WaveDisplayListeners registered with this WaveFormDisplay 
	 */
	protected ArrayList listeners = new ArrayList();

	
	//--------------------------------------------------------------------------
	//
	// Data Source
	//
	//--------------------------------------------------------------------------
	/**
	 * Set the WaveFormProvider from which to get the WaveForm data
	 * @param source The new provider
	 */
	public  void setSource(WaveFormProvider source){
		this.source = source;
		zoomFactor = 1;
		scrollFactor = 0.5;
		redraw();
	}
	/**
	 * Remove the current WaveFormProvider if any
	 *
	 */
	public void removeSource(){
		source = null;
		redraw();
	}
	/**
	 * Get the current WaveFormProvider of this WaveFormDisplay
	 * @return The current WaveFormProvider or <code>null</code>, if no provider
	 * is set
	 */
	public WaveFormProvider getSource(){
		return source;
	}
	/**
	 * Ask this WaveFormDisplay, if it has data to display
	 * @return true, if there is data available for displaying, false otherwise
	 */
	public boolean hasData(){
		return getTotalLength() > 0;
	}
	/**
	 * Get the visible WaveForm depending on zommFactor, scrollFactor and source
	 * @return The WaveForm representing the currently visible audio data or
	 * <code>null</code>, if no data is available for displaying
	 */
	protected WaveForm getVisibleWaveForm(){
		if(!hasData()) return null;
		
		int firstData = getFirstData();
		int lastData = getLastData();
		int width = getUsableDisplayWidth();
		
		return source.getWaveForm(this, firstData, lastData - firstData, width);
	}
	/**
	 * Add a WaveDisplayListener to this display that will be notified when
	 * the visible area of this display changes, e.g. when scrolling or zooming
	 * @param listener The WaveDisplayListener to add
	 */
	public void addWaveDisplayListener(WaveDisplayListener listener){
		listeners.add(listener);
	}
	/**
	 * Remove a WaveDisplayListener from this display
	 * @param listener The WaveDisplayListener to remove
	 */
	public void removeWaveDisplayListener(WaveDisplayListener listener){
		listeners.remove(listener);
	}
	//--------------------------------------------------------------------------
	//
	// Display parameters
	//
	//--------------------------------------------------------------------------
	//--------------------------------------------------------------------------
	// Offsets
	//--------------------------------------------------------------------------
	/**
	 * Set the space between the left border and the beginning of the WaveForm
	 * as well as the space between the end of the WaveForm and the right
	 * border
	 * @param offset The new space in pixels
	 */
	public void setLeftOffset(int offset){
		leftOffset = offset;
	}
	/**
	 * Get the space between the left border and the beginning of the WaveForm
	 * as well as the space between the end of the WaveForm and the right
	 * border
	 * @return The space in pixels
	 */
	public int getLeftOffset(){
		return leftOffset;
	}
	/**
	 * Set the space between the top border and the beginning of the WaveForm
	 * as well as the space between the end of the WaveForm and the bottom
	 * border
	 * @param offset The new space in pixels
	 */
	public void setTopOffset(int offset){
		topOffset = offset;
	}
	/**
	 * Get the space between the top border and the beginning of the WaveForm
	 * as well as the space between the end of the WaveForm and the bottom
	 * border
	 * @return The space in pixels
	 */
	public int getTopOffset(){
		return topOffset;
	}
	/**
	 * Set the space between two channels
	 * @param spacing The new space in pixels
	 */
	public void setChannelSpacing(int spacing){
		channelSpacing = spacing;
	}
	/**
	 * Get the space between two channels
	 * @return The space in pixels
	 */
	public int getChannelSpacing(){
		return channelSpacing;
	}
	//--------------------------------------------------------------------------
	// Number of Channels
	//--------------------------------------------------------------------------
	/**
	 * Get the number of channels
	 * @return The number of channels currently displayed
	 */
	public int getChannels(){
		if(!hasData()) return 0;
		return source.getChannels();
	}
	
	
	//--------------------------------------------------------------------------
	//
	// Drawing
	//
	//--------------------------------------------------------------------------
	/**
	 * Redraw the current display
	 */
	public synchronized void redraw(){
		if(getUsableDisplayHeight() <= 0 || getUsableDisplayWidth() <= 0) return;
		drawBackground();
		if(hasData()){
			drawWave();
			drawForeground();
		}
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			WaveDisplayListener listener = (WaveDisplayListener) iter.next();
			listener.viewChanged(this, zoomFactor, scrollFactor);
		}
	}
	/**
	 * Draw the Background
	 *
	 */
	protected void drawBackground(){
		drawGlobalBackground(leftOffset, topOffset, getUsableDisplayWidth(), getUsableDisplayHeight());
		for(int channel = 0; channel < getChannels(); channel++){
			drawChannelBackground(leftOffset, getChannelTop(channel), getUsableDisplayWidth(), getChannelHeight());
		}
	}
	/**
	 * Draw the Foreground elements
	 *
	 */
	protected void drawForeground(){
		drawGlobalForeground(leftOffset, topOffset, getUsableDisplayWidth(), getUsableDisplayHeight());
		for(int channel = 0; channel < getChannels(); channel++){
			int x = leftOffset;
			int y = getChannelTop(channel);
			int w = getUsableDisplayWidth();
			int h = getChannelHeight();
			drawChannelForeground(leftOffset, getChannelTop(channel), getUsableDisplayWidth(), getChannelHeight());
		}
	}
	/**
	 * Draw the WaveForm
	 *
	 */
	protected void drawWave(){
		WaveForm w = getVisibleWaveForm();
		int height = getChannelHeight();
		for(int channel = 0; channel < getChannels(); channel++){
			int top = getChannelTop(channel);
			w.rewind();
			byte lastMin = w.getMin(channel);
			byte lastMax = w.getMax(channel);
			byte min, max;
			drawWaveLine(leftOffset, calcY(top, height, lastMin), leftOffset, calcY(top, height, lastMax));
			for(int x = 0; w.next(); x++){
				min = w.getMin(channel);
				max = w.getMax(channel);
				drawWaveLine (leftOffset + x, calcY(top, height, (lastMin + lastMax) / 2), leftOffset + x + 1, calcY(top, height, (min + max) / 2));
				drawWaveLine (leftOffset + x + 1, calcY(top, height, min), leftOffset + x + 1, calcY(top, height, max));
				lastMin = min;
				lastMax = max;
			}
		}
	}
	/**
	 * Calculate the y coordinate for a given WaveForm sample value 
	 * @param top The top offset
	 * @param maxheight The total height of the area into which the WaveForm
	 * should be drawn
	 * @param value The sample value gotten from the WaveForm
	 * @return An y-coordinate that corresponds to the given WaveForm sample
	 */
	protected static int calcY(int top, int maxheight, int value){
		return (int)(top + maxheight / 2 - value / 128.0 * maxheight / 2); 
	}
	/**
	 * Get the usable height for displaying a single channel
	 * @return The height in pixels available for displaying one channel큦 data
	 */
	protected int getChannelHeight(){
		if(!hasData()) return 0;
		return (getUsableDisplayHeight() + channelSpacing) / getChannels() - channelSpacing;
	}
	/**
	 * Get the y-coordinate of the top border of the specified channel
	 * @param channel The zero-based channel
	 * @return The y-coordinate of the channel큦 top in pixels
	 */
	protected int getChannelTop(int channel){
		return topOffset + channel * (getChannelHeight() + channelSpacing);
	}
	/**
	 * Draw the global background in the given area. Most implementations will
	 * just fill this background
	 * @param x The x-coordinate of the area
	 * @param y The y-coordinate of the area
	 * @param width The width of the area
	 * @param height The height of the area
	 */
	protected abstract void drawGlobalBackground(int x, int y, int width, int height);
	/**
	 * Draw one channel큦 background into the given area
	 * @param x The x-coordinate of the area
	 * @param y The y-coordinate of the area
	 * @param width The width of the area
	 * @param height The height of the area
	 */
	protected abstract void drawChannelBackground(int x, int y, int width, int height);
	/**
	 * Draw the global foreground elements into the given area
	 * @param x The x-coordinate of the area
	 * @param y The y-coordinate of the area
	 * @param width The width of the area
	 * @param height The height of the area
	 */
	protected abstract void drawGlobalForeground(int x, int y, int width, int height);
	/**
	 * Draw the foreground elements of one channel into the given area
	 * @param x The x-coordinate of the area
	 * @param y The y-coordinate of the area
	 * @param width The width of the area
	 * @param height The height of the area
	 */
	protected abstract void drawChannelForeground(int x, int y, int width, int height);
	/**
	 * Draw one line of the WaveForm, this is either a vertical connection from
	 * a maximum to a minmum or a connection from the average value of pixel x
	 * to the average value of pixel x + 1
	 * @param x The x-coordinate of the first point
	 * @param y The y-coordinate of the first point
	 * @param x2 The x-coordinate of the second point
	 * @param y2 The y-coordinate of the second point
	 */
	protected abstract void drawWaveLine(int x, int y, int x2, int y2);

	//--------------------------------------------------------------------------
	//
	// View Range
	//
	//--------------------------------------------------------------------------
	//--------------------------------------------------------------------------
	// Own size
	//--------------------------------------------------------------------------
	/**
	 * Get the total width available to this WaveDisplay
	 * @return This WaveDisplay큦 total width
	 */
	protected abstract int getDisplayWidth();
	/**
	 * Get the total height available to this WaveDisplay
	 * @return This WaveDisplay큦 total height
	 */
	protected abstract int getDisplayHeight();
	
	//--------------------------------------------------------------------------
	// Zooming
	//--------------------------------------------------------------------------
	/**
	 * Zoom to the specified zoom factor.
	 * @param d The new zoom factor. A zoom factor of 1.0 means, that the whole
	 * WaveForm is visible, a greater value means zooming in and a smaller
	 * value zooming out.
	 */
	public void zoom(double d){
		if(zoomFactor == d) return;
		if(d <= 1) d = 1;
		zoomFactor = d;
		redraw();
	}
	/**
	 * Get the current zoom factor
	 * @return The current zoom factor. A zoom factor of 1.0 means, that the whole
	 * WaveForm is visible, a greater value means zooming in and a smaller
	 * value zooming out. 
	 */
	public double getZoom(){
		return zoomFactor;
	}

	//--------------------------------------------------------------------------
	// Scrolling
	//--------------------------------------------------------------------------
	/**
	 * Scroll to the given scroll factor.
	 * @param d The new scroll factor. A scroll factor of 0 means scroll to the
	 * left, 0.5 means scroll to center and 1 means scroll to the right. Any
	 * inbetween value is possible.
	 */
	public void scroll(double d){
		scrollFactor = Math.max(Math.min(d, 1), 0);
		redraw();
	}
	/**
	 * Get the current scroll factor
	 * @return The current scroll factor. A scroll factor of 0 means scroll to
	 * the left, 0.5 means scroll to center and 1 means scroll to the right. Any
	 * inbetween value is possible.
	 */
	public double getScroll(){
		return scrollFactor;
	}
	/**
	 * Scroll to the most left position
	 *
	 */
	public void scrollLeft(){
		scroll(0);
	}
	/**
	 * Scroll to the most right position
	 *
	 */
	public void scrollRight(){
		scroll(1);
	}
	/**
	 * Scroll to center position
	 *
	 */
	public void scrollCenter(){
		scroll(0.5);
	}
	
	
	//--------------------------------------------------------------------------
	//
	// Data position and pixels
	//
	//--------------------------------------------------------------------------
	/**
	 * Get the total length of the audio data represented by this
	 * WaveFormDisplay
	 * @return The total length in sample frames
	 */
	public int getTotalLength(){
		if(source == null) return 0;
		return source.getTotalLength();
	}
	/**
	 * Get the frame position of the first visible data
	 * @return The frame position of the first visible sample frame
	 */
	public int getFirstData(){
		int total = getTotalLength();
		double length = total / zoomFactor;
		double max = total - length;
		
		return Math.max(0, (int)Math.round(scrollFactor * max));
	}
	/**
	 * Get the frame position of the last visible data
	 * @return The frame position of the last visible sample frame
	 */
	public int getLastData(){
		int total = getTotalLength();
		double length = total / zoomFactor;

		return (int)Math.round(getFirstData() + length);
	}
	/**
	 * Get the width in pixels that can be used for drawing the WaveForm
	 * @return The width in pixels available for drawing the Waveform, this is
	 * the total width minus 2 times the leftOffset
	 */
	protected int getUsableDisplayWidth(){
		return getDisplayWidth() - 2 * leftOffset;
	}
	/**
	 * Get the height in pixels that can be used for drawing the WaveForm
	 * @return The height in pixels available for drawing the Waveform, this is
	 * the total height minus 2 times the topOffset
	 */
	protected int getUsableDisplayHeight(){
		return getDisplayHeight() - 2 * topOffset;
	}
	/**
	 * Get the quotient between one pixel and the real number of sample frames
	 * represented by this pixel
	 * @return 1 / [number of sample frames per pixel]
	 */
	protected double getPixelFactor(){
		if(!hasData()) return 1;
		
		double factor = getUsableDisplayWidth() / (double)getTotalLength() * zoomFactor;
		if(factor <= 0) return 1;
		
		return factor;
	}
	/**
	 * Convert a data position into a pixel position
	 * @param dataPos The position in the audio data in sample frames
	 * @return The x-coordinate of the corresponding pixel, not including
	 * the left offset
	 */
	protected int dataToPixel(int dataPos){
		if(!hasData()) return 0;
		
		dataPos -= getFirstData();
		return (int)Math.round(getPixelFactor() * dataPos);
	}
	/**
	 * Convert a pixel position to a data position
	 * @param pixelPos The x-coordinate of a pixel, not including the left
	 * offset
	 * @return The position in the audio data corresponding to this pixel in
	 * sample frames
	 */
	protected int pixelToData(int pixelPos){
		if(!hasData()) return 0;
		
		return (int)Math.round(pixelPos / getPixelFactor() + getFirstData());
	}
	/**
	 * Convert a mouse position to a data position
	 * @param mousePos The x-coordinate of a mouse position including the left
	 * offset
	 * @return The position in the audio data corresponding to this mouse
	 * position in sample frames
	 */
	public int mouseToData(int mousePos){
		if(!hasData()) return 0;
		
		mousePos -= leftOffset;
		if(mousePos < 0) mousePos = 0;
		if(mousePos > getUsableDisplayWidth()) mousePos = getUsableDisplayWidth();
		
		return pixelToData(mousePos);
	}
	/**
	 * Adapt the zoom and scroll factor so that the given part of the source
	 * will be visible
	 * @param first The position of the first sample frame to be made visible 
	 * @param last The position of the last sample frame to be made visible
	 */
	public void showData(int first, int last){
		if(first == last){
			zoom(1);
			scroll(0.5);
		}
		int total = getTotalLength(); 
		zoomFactor = total / (double)(last - first);
		scrollFactor = first / (double)(total - total / zoomFactor);
		redraw();
	}
	/**
	 * Show the whole WaveForm
	 *
	 */
	public void showAll(){
		scrollFactor = 0.5;
		zoomFactor = 1;
		redraw();
	}

}