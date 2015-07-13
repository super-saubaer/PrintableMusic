/*
 * Created on 12.05.2004
 *
 */
package com.groovemanager.gui.custom;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * This class is a helper class for often moved and size-changed drawed
 * Rectangles to avoid repainting if it is not needed and to keep the state
 * consistent at any time.
 * @author Manu Robledo
 *
 */
public class DrawedRectangle {
	/**
	 * The current coordinates of this Rectangle
	 */
	protected Rectangle rect;
	/**
	 * Temporary object for reuse
	 */
	protected Rectangle temp = new Rectangle(0, 0, 0, 0);
	/**
	 * The Graphics Context used for drawing operations
	 */
	protected GC gc;
	/**
	 * Current status of this Rectangle: visible
	 * (<code>isErased == false</code>) or not visible 
	 * (<code>isErased == true</code>).
	 */
	protected boolean isErased = false;
	/**
	 * Indicates whether this Rectangle should be redrawn after a call to
	 * <code>release()</code>
	 */
	protected boolean redrawAfterRelease = false;
	/**
	 * Constructs a new DrawedRectangle in invisible state using the given
	 * Graphics Context with the coordinates 0,0 and width/height 0
	 * @param gc The Graphics Context to use for drawing operations
	 */
	public DrawedRectangle(GC gc){
		this.gc = gc;
		rect = new Rectangle(0, 0, 0, 0);
		isErased = true;
	}
	/**
	 * Constructs a new DrawedRectangle with the coordinates and width/height
	 * of the given Rectangle using the given Graphics Context for drawing
	 * operations in the state indicated by <code>isErased</code>
	 * @param rect The Rectangle defining coordinates and width/height of this
	 * Rectangle
	 * @param gc The Graphics Context to use for drawing operations
	 * @param isErased The current state of the Rectangle
	 */
	public DrawedRectangle(Rectangle rect, GC gc, boolean isErased) {
		this.rect = rect;
		this.gc = gc;
		this.isErased = isErased;
	}
	/**
	 * Constructs a new DrawedRectangle with the coordinates and width/height
	 * of the given Rectangle using the given Graphics Context for drawing
	 * operations
	 * @param rect The Rectangle defining coordinates and width/height of this
	 * Rectangle
	 * @param gc The Graphics Context to use for drawing operations
	 */
	public DrawedRectangle(Rectangle rect, GC gc) {
		this(rect, gc, true);
	}
	/**
	 * Set the state of this Rectangle
	 * @param erased true, if this Rectangle is erased and therefore NOT
	 * visible on the screen, false otherwise.
	 */
	public void setErased(boolean erased){
		isErased = erased;
	}
	/**
	 * Delete this Rectangle from the screen if it is currently visible
	 *
	 */
	public synchronized void erase(){
		if(isErased) return;
		eraseRectangle(rect);
		isErased = true;
	}
	/**
	 * Make this Rectangle visible on the screen if it is not already
	 *
	 */
	public synchronized void draw(){
		if(isErased) drawRectangle(rect);
		isErased = false;
	}
	/**
	 * Internal method for drawing a Rectangle on the screen
	 * @param r The Rectangel to draw
	 */
	protected void drawRectangle(Rectangle r){
		gc.fillRectangle(r);
	}
	/**
	 * Internal method for erasing a visible Rectangle from the screen
	 * @param r The Rectangle to erase
	 */
	protected void eraseRectangle(Rectangle r){
		boolean oldXOR = gc.getXORMode();
		gc.setXORMode(true);
		gc.fillRectangle(r);
		gc.setXORMode(oldXOR);
	}
	/**
	 * Add the given amount of pixels to this Rectangle큦 width on the left
	 * side
	 * @param pixels The amount of pixels to add
	 */
	public synchronized void addLeft(int pixels){
		if(pixels <= 0) return;
		if(!isErased){
			temp.x = rect.x - pixels;
			temp.y = rect.y;
			temp.width = pixels;
			temp.height = rect.height;
			drawRectangle(temp);
		}
		rect.x -= pixels;
		rect.width += pixels;
	}
	/**
	 * Decrease this Rectangle큦 widthby removing the given amount of pixels
	 * from the left side
	 * @param pixels The amount of pixels to remove
	 */
	public synchronized void removeLeft(int pixels){
		if(pixels <= 0) return;
		if(!isErased){
			temp.x = rect.x;
			temp.y = rect.y;
			temp.width = pixels;
			temp.height = rect.height;
			eraseRectangle(temp);
		}
		rect.x += pixels;
		rect.width -= pixels;
	}
	/**
	 * Increase this Rectangle큦 width by adding the given amount of pixels to
	 * the right side
	 * @param pixels The amount of pixels to add
	 */
	public synchronized void addRight(int pixels){
		if(pixels <= 0) return;
		if(!isErased){
			temp.x = rect.x + rect.width;
			temp.y = rect.y;
			temp.width = pixels;
			temp.height = rect.height;
			drawRectangle(temp);
		}
		rect.width += pixels;
	}
	/**
	 * Decrease this Rectangle큦 width by removing the given amount of pixels
	 * from the right side
	 * @param pixels The amount of pixels to remove.
	 */
	public synchronized void removeRight(int pixels){
		if(pixels <= 0) return;
		if(!isErased){
			temp.x = rect.x + rect.width - pixels;
			temp.y = rect.y;
			temp.width = pixels;
			temp.height = rect.height;
			eraseRectangle(temp);
		}
		rect.width -= pixels;
	}
	/**
	 * Increase this Rectangle큦 height by adding the given amount of pixels
	 * to the top
	 * @param pixels The amount of pixels to add
	 */
	public synchronized void addTop(int pixels){
		if(pixels <= 0) return;
		if(!isErased){
			temp.x = rect.x;
			temp.y = rect.y - pixels;
			temp.width = rect.width;
			temp.height = pixels;
			drawRectangle(temp);
		}
		rect.y -= pixels;
		rect.height += pixels;
	}
	/**
	 * Decrease this Rectangle큦 height by removing the given amount of pixels
	 * from the top
	 * @param pixels The amount of pixels to remove
	 */
	public synchronized void removeTop(int pixels){
		if(pixels <= 0) return;
		if(!isErased){
			temp.x = rect.x;
			temp.y = rect.y;
			temp.width = rect.width;
			temp.height = pixels;
			eraseRectangle(temp);
		}
		rect.y += pixels;
		rect.height -= pixels;
	}
	/**
	 * Increase this Rectangle큦 height by adding the given amount of pixels
	 * to the bottom
	 * @param pixels The amount of pixels to add
	 */
	public synchronized void addBottom(int pixels){
		if(pixels <= 0) return;
		if(!isErased){
			temp.x = rect.x;
			temp.y = rect.y + rect.height;
			temp.width = rect.width;
			temp.height = pixels;
			drawRectangle(temp);
		}
		rect.height += pixels;
	}
	/**
	 * Decrease this Rectangle큦 height by removing the given amount of pixels
	 * from the bottom
	 * @param pixels The amount of pixels to remove
	 */
	public synchronized void removeBottom(int pixels){
		if(pixels <= 0) return;
		if(!isErased){
			temp.x = rect.x;
			temp.y = rect.y + rect.height - pixels;
			temp.width = rect.width;
			temp.height = pixels;
			eraseRectangle(temp);
		}
		rect.height -= pixels;
	}
	/**
	 * Move this Rectangle to the right by the given amount of pixels
	 * @param pixels The amount of pixels to move
	 */
	public synchronized void moveRight(int pixels){
		if(pixels <= 0) return;
		addRight(pixels);
		removeLeft(pixels);
	}
	/**
	 * Move this Rectangle to the left by the given amount of pixels
	 * @param pixels The amount of pixels to move
	 */
	public synchronized void moveLeft(int pixels){
		if(pixels <= 0) return;
		addLeft(pixels);
		removeRight(pixels);
	}
	/**
	 * Move this Rectangle up by the given amount of pixels
	 * @param pixels The amount of pixels to move
	 */
	public synchronized void moveUp(int pixels){
		if(pixels <= 0) return;
		addTop(pixels);
		removeBottom(pixels);
	}
	/**
	 * Move this Rectangle down by the given amount of pixels
	 * @param pixels The amount of pixels to move
	 */
	public synchronized void moveDown(int pixels){
		if(pixels <= 0) return;
		addBottom(pixels);
		removeTop(pixels);
	}
	/**
	 * Set the x-coordinate of this Rectangle큦 left border
	 * @param x The new x-coordinate
	 */
	public synchronized void setLeft(int x){
		if(x < rect.x) addLeft(rect.x - x);
		else removeLeft(x - rect.x);
	}
	/**
	 * Set the y-coordinate of this Rectangle큦 top border
	 * @param y The new y-coordinate
	 */
	public synchronized void setTop(int y){
		if(y < rect.y) addTop(rect.y - y);
		else removeTop(y - rect.y);
	}
	/**
	 * Set the x-coordinate of this Rectangle큦 right border
	 * @param x The new x-coordinate
	 */
	public synchronized void setRight(int x){
		if(x > rect.x + rect.width) addRight(x - rect.x - rect.width);
		else removeRight(rect.x + rect.width - x);
	}
	/**
	 * Set the y-coordinate of this Rectangle큦 bottom border 
	 * @param y The new y-coordinate
	 */
	public synchronized void setBottom(int y){
		if(y < rect.y + rect.height) addBottom(y - rect.y - rect.height);
		else removeBottom(rect.y + rect.height - y);
	}
	/**
	 * Move this Rectangle to the given x-ccordinate
	 * @param x The new x-coordinate
	 */
	public synchronized void setXPosition(int x){
		if(x > rect.x) moveRight(x - rect.x);
		else moveLeft(rect.x - x);
	}
	/**
	 * Move this Rectangle to the given y-ccordinate
	 * @param y The new y-coordinate
	 */
	public synchronized void setYPosition(int y){
		if(y > rect.y) moveDown(y - rect.y);
		else moveUp(rect.y - y);
	}
	/**
	 * Set this Rectangle큦 width
	 * @param width The new width
	 */
	public synchronized void setWidth(int width){
		if(width > rect.width) addRight(width - rect.width);
		else removeRight(rect.width - width);
	}
	/**
	 * Set this Rectangle큦 height
	 * @param height The new height
	 */
	public synchronized void setHeight(int height){
		if(height > rect.height) addBottom(height - rect.height);
		else removeBottom(rect.height - height);
	}
	/**
	 * Block this Rectangle so it is not being redrawn after each change.
	 * Usually a call to this method will be followed by a number of calls
	 * to methods changing this Rectangle큦 position and size. After finishing
	 * these operations, a call to <code>release()</code> will set the Rectangle
	 * back into its usual behaviour.
	 * @see com.groovemanager.gui.custom.DrawedRectangle#release()
	 */
	public void block(){
		redrawAfterRelease = !isErased;
		if(redrawAfterRelease) erase();
	}
	/**
	 * Release this Rectangle after it has been blocked by a call to
	 * <code>block()</code>.
	 * @see com.groovemanager.gui.custom.DrawedRectangle#block()
	 */
	public void release(){
		if(redrawAfterRelease) draw();
	}
	/**
	 * Set the new coordinates, width and height for this Rectangle
	 * @param x The new x-coordinate
	 * @param y The new y-coordinate
	 * @param width The new width
	 * @param height The new height
	 */
	public synchronized void setTo(int x, int y, int width, int height){
		setXPosition(x);
		setYPosition(y);
		setWidth(width);
		setHeight(height);
	}
}