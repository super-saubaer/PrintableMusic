package com.groovemanager.gui.custom.mc909;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.groovemanager.gui.custom.CustomComposite;


/**
 * An Mc909Key represents a Composite surrounding a Mc909Pad. A number of
 * Mc909Keys can be packed into a Mc909KeyComposite.
 * @author Manu Robledo
 *
 */
public class Mc909Key extends CustomComposite{
	/**
	 * The Mc909Pad contained in this Key
	 */
	private Mc909Pad pad;
	/**
	 * Constants for different types of keys
	 */
	public final static int WHITE_BEGIN = 1, WHITE_MIDDLE = 2, WHITE_END = 3, BLACK_LEFT= 4, BLACK_MIDDLE = 5, BLACK_RIGHT = 6;
	/**
	 * The constant for the line width
	 */
	private final static int LINE_WIDTH = 3;
	/**
	 * Note names
	 */
	private final static String[] notes = new String[]{"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
	/**
	 * The Label containing the note name and octave
	 */
	private Label note;
	/**
	 * Construct a new Mc909Key
	 * @param parent The parent Composite
	 * @param type The type of the key (one of the type constants, e.g.
	 * <code>WHITE_BEGIN</code>
	 * @param key The key number in the range from 0 to 127
	 */
	public Mc909Key(Composite parent, int type, int key){
		super(parent);
		drawKey(type);
		
		if(type == WHITE_BEGIN || type == WHITE_MIDDLE || type == WHITE_END) pad = new Mc909Pad(getComposite(), Mc909Pad.WHITE, key);
		else  pad = new Mc909Pad(getComposite(), Mc909Pad.BLACK, key);

		FormData fd = new FormData();
		fd.right = new FormAttachment(90, 0);
		fd.left = new FormAttachment(10, 0);
		fd.top = new FormAttachment(8, 0);
		fd.bottom = new FormAttachment(75, 0);
		
		pad.getComposite().setLayoutData(fd);
		
		note.setText(notes[key % 12] + " " + (key / 12 - 1));
	}
	/**
	 * Internal method for adding the PaintListener to the main composite
	 * @param type The type of this Key
	 */
	private void drawKey(final int type){
		getComposite().addPaintListener(new PaintListener(){

			public void paintControl(PaintEvent event) {
				Display display = event.display;
				GC gc = event.gc;
				gc.setLineWidth(LINE_WIDTH);
				Rectangle rect = getComposite().getClientArea();
				// obere Linie
				gc.drawLine(rect.x, rect.y + 1, rect.x + rect.width, rect.y + 1);
				// untere Linie
				gc.drawLine(rect.x, rect.y + rect.height - 1, rect.x + rect.width, rect.y + rect.height - 1);
				
				switch(type){
					case WHITE_BEGIN:
						// Linie links
						gc.drawLine(rect.x, rect.y, rect.x, rect.y + rect.height);
						break;
						
					case WHITE_END:
					// Linie rechts
					gc.drawLine(rect.x + rect.width, rect.y, rect.x + rect.width, rect.y + rect.height);
					break;
			
					case BLACK_LEFT:
					// Schwarze Taste
					gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
					gc.fillRoundRectangle(rect.x, rect.y - 5, rect.x + rect.width, rect.y + 5 + (int)Math.round(rect.height * 0.8), 8, 8);
					gc.drawLine(rect.x + (int)Math.round(rect.width * 0.66), rect.y - 1 + (int)Math.round(rect.height * 0.8), rect.x + (int)Math.round(rect.width * 0.66), rect.y + rect.height);
					break;

					case BLACK_RIGHT:
					// Schwarze Taste
					gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
					gc.fillRoundRectangle(rect.x, rect.y - 5, rect.x + rect.width, rect.y + 5 + (int)Math.round(rect.height * 0.8), 8, 8);
					gc.drawLine(rect.x + (int)Math.round(rect.width * 0.33), rect.y - 1 + (int)Math.round(rect.height * 0.8), rect.x + (int)Math.round(rect.width * 0.33), rect.y + rect.height);
					break;

					case BLACK_MIDDLE:
					// Schwarze Taste
					gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
					gc.fillRoundRectangle(rect.x, rect.y - 5, rect.x + rect.width, rect.y + 5 + (int)Math.round(rect.height * 0.8), 8, 8);
					gc.drawLine(rect.x + (int)Math.round(rect.width * 0.5), rect.y - 1 + (int)Math.round(rect.height * 0.8), rect.x + (int)Math.round(rect.width * 0.5), rect.y + rect.height);
					break;
				}
			}
		});
		
	}
	/**
	 * Set the visible status of the note name
	 * @param visible true, if the note name and octave should be visible,
	 * false otherwise
	 */
	public void setNoteVisible(boolean visible){
		note.setVisible(visible);
	}
	/**
	 * Get the Mc909Pad contained in this Key
	 * @return The Mc909Pad contained in this Key
	 */
	public Mc909Pad getPad(){
		return pad;
	}
	/**
	 * @see com.groovemanager.gui.custom.CustomComposite#createComposite(org.eclipse.swt.widgets.Composite, int)
	 */
	protected Composite createComposite(Composite parent, int style) {
		Composite comp = new Canvas(parent, SWT.NONE);
		
		comp.setLayout(new FormLayout());
		
		note = new Label(comp, SWT.CENTER);
		FormData lfd = new FormData();
		lfd.right = new FormAttachment(90, 0);
		lfd.left = new FormAttachment(10, 0);
		lfd.top = new FormAttachment(83, 0);
		note.setLayoutData(lfd);

		comp.setBackground(comp.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		return comp;
	}
	/**
	 * @see com.groovemanager.gui.custom.CustomComposite#getPossibleStyles()
	 */
	protected int getPossibleStyles() {
		return SWT.NONE;
	}
	/**
	 * @see com.groovemanager.gui.custom.CustomComposite#getListenerTypes()
	 */
	protected int[] getListenerTypes() {
		return new int[0];
	}
}
