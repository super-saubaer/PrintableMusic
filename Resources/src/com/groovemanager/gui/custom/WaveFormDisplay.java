/*
 * Created on 12.05.2004
 *
 */
package com.groovemanager.gui.custom;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;

import com.groovemanager.sampled.waveform.MarkableWaveFormDisplay;
import com.groovemanager.sampled.waveform.Marker;

/**
 * This class is an implementation of the abstract MarkableWaveFormDisplay
 * using SWT
 * @author Manu Robledo
 *
 */
public class WaveFormDisplay extends MarkableWaveFormDisplay {
	/**
	 * The number of sample frames to move a Marker with one arrow press
	 */
	protected int moveStep = 100;
	/**
	 * Color to use for not selected Markers
	 */
	protected Color markerColor,
	/**
	 * Color to use for the selected Marker
	 */
	markerHighlightColor;
	/**
	 * The main Composite
	 */
	protected Canvas comp;
	/**
	 * Different Graphic Context Objects used for different drawing operations
	 */
	protected GC markerGC, selectionGC, positionGC, channelBgGC, channelFgGC, waveGC, backgroundGC;
	/**
	 * Map of all drawed Markers as DrawedRectangle instances
	 */
	protected HashMap drawedMarkers = new HashMap();
	/**
	 * DrawedRectangle for the drawed selection
	 */
	protected DrawedRectangle drawedSelection,
	/**
	 * DrawedRectangle for the Position pointer 
	 */
	drawedPosition;
	/**
	 * If this variable is false, selection events coming from the scrollbar
	 * will be ignored
	 */
	protected boolean reactToScrollBar = true,
	/**
	 * Use a gradient Background for this WaveFormDisplay?
	 */
	gradient;
	/**
	 * The default cursor
	 */
	protected Cursor normalCursor,
	/**
	 * The cursor to use when moving Markers
	 */
	moveMarkerCursor,
	/**
	 * Cursor to be used when creating new Markers or selecting Markers
	 */
	editMarkerCursor,
	/**
	 * Cursor to be used when deleting Markers
	 */
	deleteMarkerCursor;
	/**
	 * Construct a new WaveFormDisplay
	 * @param parent The parent Composite
	 * @param gradientBackground true, if a gradient background should be used,
	 * false otherwise
	 */
	public WaveFormDisplay(Composite parent, boolean gradientBackground){
		this(parent, SWT.H_SCROLL, gradientBackground);
	}
	/**
	 * Construct a new WaveFormDisplay
	 * @param parent The parent Composite
	 * @param style Combination of SWT.* style constants for the main Composite
	 * @param gradientBackground true, if a gradient background should be used,
	 * false otherwise
	 */
	public WaveFormDisplay(Composite parent, int style, boolean gradientBackground){
		gradient = gradientBackground;
		comp = new Canvas(parent, style);
		normalCursor = new Cursor(comp.getDisplay(), SWT.CURSOR_ARROW);
		moveMarkerCursor = new Cursor(comp.getDisplay(), SWT.CURSOR_SIZEWE);
		editMarkerCursor = new Cursor(comp.getDisplay(), SWT.CURSOR_HAND);
		deleteMarkerCursor = new Cursor(comp.getDisplay(), SWT.CURSOR_CROSS);
		final ScrollBar bar = comp.getHorizontalBar();
		if(bar != null) bar.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if(reactToScrollBar){
					int sel = bar.getSelection();
					int max = bar.getMaximum();
					int thumb = bar.getThumb();
					double s = sel / (double)(max - thumb);
					scroll(s);
				}
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		comp.addPaintListener(new PaintListener(){
			public void paintControl(PaintEvent e) {
				redraw();
			}
		});
		markerGC = new GC(comp);
		markerGC.setXORMode(true);
		selectionGC = new GC(comp);
		selectionGC.setXORMode(true);
		drawedSelection = new DrawedRectangle(new Rectangle(leftOffset, topOffset, 0, getUsableDisplayHeight()), selectionGC);
		positionGC = new GC(comp);
		positionGC.setXORMode(true);
		drawedPosition = new DrawedRectangle(new Rectangle(leftOffset, topOffset, 1, getUsableDisplayHeight()), positionGC);
		channelBgGC = new GC(comp);
		channelFgGC = new GC(comp);
		channelFgGC.setXORMode(true);
		waveGC = new GC(comp);
		backgroundGC = new GC(comp);
		initColors();
		
		addMouseListeners(comp);
	}
	/**
	 * Add the needed MouseListeners to the given Composite
	 * @param comp The main Composite
	 */
	protected void addMouseListeners(final Composite comp){
		comp.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
			}
			public void mouseDown(MouseEvent e) {
				WaveFormDisplay.this.mouseDown(e.x, e.y, (e.stateMask & SWT.SHIFT) > 0, (e.stateMask & SWT.CTRL) > 0);
				if(editMarkers && selectedMarker != null && mouseDown) getComposite().setCursor(moveMarkerCursor);
				else getComposite().setCursor(normalCursor);
			}
			public void mouseUp(MouseEvent e) {
				WaveFormDisplay.this.mouseUp(e.x, e.y, (e.stateMask & SWT.SHIFT) > 0, (e.stateMask & SWT.CTRL) > 0);
				if(editMarkers){
					if((e.stateMask & SWT.CTRL) > 0){
						if(getMarkerFromMousePos(e.x) == null) getComposite().setCursor(editMarkerCursor);
						else getComposite().setCursor(deleteMarkerCursor);
					}
					else if((e.stateMask & SWT.SHIFT) > 0){
						Marker nearest = getMarkerFromMousePos(e.x);
						if(nearest == null) getComposite().setCursor(normalCursor);
						else getComposite().setCursor(moveMarkerCursor);
					}
					else getComposite().setCursor(normalCursor);
				}
				else getComposite().setCursor(normalCursor);
			}
		});
		
		comp.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				WaveFormDisplay.this.mouseMove(e.x, e.y, (e.stateMask & SWT.SHIFT) > 0, (e.stateMask & SWT.CTRL) > 0);
				if(!mouseDown){
					if(editMarkers){
						if((e.stateMask & SWT.CTRL) > 0){
							if(getMarkerFromMousePos(e.x) == null) getComposite().setCursor(editMarkerCursor);
							else getComposite().setCursor(deleteMarkerCursor);
						}
						else if((e.stateMask & SWT.SHIFT) > 0){
							Marker nearest = getMarkerFromMousePos(e.x);
							if(nearest == null) getComposite().setCursor(normalCursor);
							else getComposite().setCursor(moveMarkerCursor);
						}
						else getComposite().setCursor(normalCursor);
					}
					else getComposite().setCursor(normalCursor);
				}
			}
		});
		
		getComposite().addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if(selectedMarker == null || !editMarkers) return;
				switch (e.keyCode) {
					case SWT.ARROW_LEFT :
						if((e.stateMask & SWT.CTRL) > 0) moveMarker(selectedMarker, Math.max(0, selectedMarker.getPosition() - 1));
						else moveMarker(selectedMarker, Math.max(0, selectedMarker.getPosition() - moveStep));
						break;
					case SWT.ARROW_RIGHT :
						if((e.stateMask & SWT.CTRL) > 0) moveMarker(selectedMarker, Math.min(selectedMarker.getPosition() + 1, getTotalLength()));
						else moveMarker(selectedMarker, Math.min(selectedMarker.getPosition() + moveStep, getTotalLength()));
						break;
					default :
						break;
				}
			}
			public void keyReleased(KeyEvent e) {
			}
		});
	}
	/**
	 * Get the main Composite of this WaveFormDisplay
	 * @return This WaveFormDisplay´s main Composite
	 */
	public Composite getComposite(){
		return comp;
	}
	/**
	 * Set the colors for the different drawing elements to default colors
	 *
	 */
	protected void initColors(){
		Display d = comp.getDisplay();
		markerColor = d.getSystemColor(SWT.COLOR_GREEN);
		markerHighlightColor = d.getSystemColor(SWT.COLOR_DARK_MAGENTA);
		markerGC.setBackground(markerColor);
		selectionGC.setBackground(d.getSystemColor(SWT.COLOR_BLUE));
		positionGC.setBackground(d.getSystemColor(SWT.COLOR_GRAY));
		channelBgGC.setBackground(d.getSystemColor(SWT.COLOR_DARK_RED));
		channelFgGC.setForeground(d.getSystemColor(SWT.COLOR_RED));
		waveGC.setForeground(d.getSystemColor(SWT.COLOR_YELLOW));
		backgroundGC.setBackground(d.getSystemColor(SWT.COLOR_GRAY));
	}
	/**
	 * Change the Color used for drawing markers
	 * @param c The new Color to be used for drawing markers
	 */
	public void setMarkerColor(Color c){
		markerColor = c;
		redraw();
	}
	/**
	 * Change the Color used for drawing highlighted markers
	 * @param c The new Color to be used for drawing highlighted markers
	 */
	public void setHighlightedMarkerColor(Color c){
		markerHighlightColor = c;
		redraw();
	}
	/**
	 * Set the Color for drawing the selection
	 * @param c The new selection Color
	 */
	public void setSelectionColor(Color c){
		selectionGC.setBackground(c);
		redraw();
	}
	/**
	 * Set the color used for drawing the position pointer
	 * @param c The Color for the position pointer
	 */
	public void setPositionColor(Color c){
		positionGC.setBackground(c);
		redraw();
	}
	/**
	 * Set the background color used for each channel
	 * @param c The new channel background color
	 */
	public void setChannelBackgroundColor(Color c){
		channelBgGC.setBackground(c);
		redraw();
	}
	/**
	 * Set the color of the middle line
	 * @param c The middle line´s color
	 */
	public void setMiddleLineColor(Color c){
		channelFgGC.setBackground(c);
		redraw();
	}
	/**
	 * Set the color used for drawing the WaveForm itself
	 * @param c The waveform´s color
	 */
	public void setWaveColor(Color c){
		waveGC.setForeground(c);
		redraw();
	}
	public void addMarker(Marker m) {
		DrawedRectangle d = new DrawedRectangle(markerGC);
		drawedMarkers.put(m, d);
		super.addMarker(m);
	}
	protected void removeMarker(Marker m) {
		super.removeMarker(m);
		drawedMarkers.remove(m);
	}
	protected void drawHighlightedMarker(Marker m) {
		DrawedRectangle d = (DrawedRectangle)drawedMarkers.get(m);
		d.gc.setBackground(markerHighlightColor);
		d.setTo(leftOffset + dataToPixel(m.getPosition()), topOffset, 1, getUsableDisplayHeight());
		d.draw();
	}
	protected void eraseHighlightedMarker(Marker m) {
		DrawedRectangle d = (DrawedRectangle)drawedMarkers.get(m);
		d.gc.setBackground(markerHighlightColor);
		d.erase();
	}
	protected void drawMarker(Marker m) {
		DrawedRectangle d = (DrawedRectangle)drawedMarkers.get(m);
		d.gc.setBackground(markerColor);
		d.setTo(leftOffset + dataToPixel(m.getPosition()), topOffset, 1, getUsableDisplayHeight());
		d.draw();
	}
	protected void eraseMarker(Marker m) {
		DrawedRectangle d = (DrawedRectangle)drawedMarkers.get(m);
		d.gc.setBackground(markerColor);
		d.erase();
	}
	protected void drawSelection(int x, int y, int width, int height) {
		drawedSelection.setTo(x, y, width, height);
		drawedSelection.draw();
	}
	protected void eraseSelection() {
		drawedSelection.erase();
	}
	protected void drawPosition(int x, int y, int height) {
		drawedPosition.setTo(x - 1, y, 2, height);
		drawedPosition.draw();
	}
	protected void erasePosition() {
		drawedPosition.erase();
	}
	protected void drawGlobalBackground(int x, int y, int width, int height) {
		backgroundGC.fillRectangle(x, y, width, height);
	}
	protected void drawChannelBackground(int x, int y, int width, int height) {
		if(gradient) channelBgGC.fillGradientRectangle(x, y, width, height, true);
		else channelBgGC.fillRectangle(x, y, width, height);
	}
	protected void drawGlobalForeground(int x, int y, int width, int height) {
		drawPosition(x + dataToPixel(pos), y, height);
		int leftPixel = dataToPixel(left);
		drawSelection(x + leftPixel, y, dataToPixel(right) - leftPixel, height);
		for (Iterator iter = markers.iterator(); iter.hasNext();) {
			Marker marker = (Marker) iter.next();
			if(marker == selectedMarker) drawHighlightedMarker(marker);
			else drawMarker(marker);
		}
	}
	protected void drawChannelForeground(int x, int y, int width, int height) {
		channelFgGC.drawLine(x, y + height / 2, x + width, y + height / 2);
	}
	protected void drawWaveLine(int x, int y, int x2, int y2) {
		waveGC.drawLine(x, y, x2, y2);
	}
	protected int getDisplayWidth() {
		return comp.getClientArea().width;
	}
	protected int getDisplayHeight() {
		return comp.getClientArea().height;
	}
	protected void redrawSelection(int x, int y, int width, int height) {
		drawedSelection.setTo(x, y, width, height);
		drawedSelection.draw();
	}
	public synchronized void redraw() {
		drawedPosition.setErased(true);
		drawedSelection.setErased(true);
		for (Iterator iter = drawedMarkers.keySet().iterator(); iter.hasNext();) {
			DrawedRectangle element = (DrawedRectangle) drawedMarkers.get(iter.next());
			element.setErased(true);			
		}
		super.redraw();
		ScrollBar b = comp.getHorizontalBar();
		if(b != null){
			if(zoomFactor <= 1.0){
				b.setEnabled(false);
			}
			else{
				b.setEnabled(true);
				reactToScrollBar = false;
				int min = 0;
				int max = (int)Math.round(zoomFactor * 10);
				int pageInc = 10;
				if(b.getMinimum() != min) b.setMinimum(min);
				if(b.getMaximum() != max) b.setMaximum(max);
				if(b.getPageIncrement() != pageInc) b.setPageIncrement(pageInc);
				if(b.getThumb() != pageInc) b.setThumb(pageInc);
				
				int sel = (int)Math.round(scrollFactor * (b.getMaximum() - b.getThumb()));
				if(b.getSelection() != sel) b.setSelection(sel);
				reactToScrollBar = true;
			}
		}
	}
	/**
	 * Set the number of sample frames a Marker should be moved, when an arrow
	 * key is pressed
	 * @param newStep The number of sample frames to move a Marker with the
	 * arrow keys
	 */
	public void setMoveStep(int newStep){
		moveStep = newStep;
	}
}