package com.groovemanager.gui.custom;

import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;

/**
 * This CustomComposite consists of a number displayed on a button. The value
 * of the number can be changed by selecting and moving with the mouse, by
 * double-clicking and entering it manually or by selecting the Composite and
 * using the Up/Down-Arrows or the PageUp/PageDown-Keys.
 * The idea was to create an alternative to Scale for integer user input
 * @author Manu Robledo
 *
 */
public class ScaleNum extends CustomComposite{
	/**
	 * Text field for user-input of the value; shown when double-clicking
	 */
	private Text userInput;
	/**
	 * The button showing the current value
	 */
	private Button button;
	/**
	 * The layout used to show either the button or the text field
	 */
	private StackLayout layout;
	/**
	 * true if the Text field for user input is currently shown, false
	 * otherwise
	 */
	private boolean editing = false;
	/**
	 * Minimum value
	 */
	private int min = 0,
	/**
	 * Maximum value
	 */
	max = 0,
	/**
	 * Current value
	 */
	selection = 0,
	/**
	 * Default value 
	 */
	def = 0;
	/**
	 * Indicates whether this Composite has been created with READ_ONLY style
	 * or not
	 */
	private boolean readonly = false;
	/**
	 * Indicates, if the mouse button is currently pressed or not
	 */
	private boolean mousedown = false;
	/**
	 * Temporary variables
	 */
	private int lastChange, lastX, lastClick, lastMove;
	/**
	 * Number of pixels the mouse has to be moved up or down to change the value
	 */
	private final static int MOUSE_OFFSET = 2;
	/**
	 * The double click time
	 */
	private final int dcTime = Display.getCurrent().getDoubleClickTime();
	/**
	 * A value for the speed of the mouse movement
	 */
	private double speed = 0;
	/**
	 * Construct a new ScaleNum
	 * @param parent The parent Composite
	 * @param style Combination of SWT.* style constants.
	 * Allowed values are:<br>
	 * <code>SWT.FLAT</code>: For flat appearance<br>
	 * <code>SWT.READ_ONLY</code>: For the user not being able to change the
	 * value<br>
	 * <code>SWT.BORDER</code>: For surrounding this Composite with a border<br>
	 * <code>SWT.LEFT</code>, <code>SWT.CENTER</code> or <code>SWT.RIGHT</code>:
	 * For specification of this Composite´s alignment<br>
	 */
	public ScaleNum(Composite parent, int style){
		super(parent, style);
	}
	/**
	 * Add a SelectionListener that will be notified of value changes
	 * @param listener The SelectionListener to add
	 */
	public void addSelectionListener(SelectionListener listener){
		addListener(listener, SWT.Selection);
	}
	/**
	 * Set the minimum value
	 * @param m The minimum value
	 */
	public void setMinimum(int m){
		min = m;
		checkMinMax();
	}
	/**
	 * Set the maximum value
	 * @param m The maximum value
	 */
	public void setMaximum(int m){
		max = m;
		checkMinMax();
	}
	/**
	 * Set the current value
	 * @param s The current value
	 */
	public void setSelection(int s){
		setSelection(s, true);
	}
	/**
	 * Internal method for value change
	 * @param s The new value
	 * @param updateText true, if the content of the Text field should also be
	 * updated, false otherwise. This parameter is for avoiding endless loops
	 * of change notifications going on between this ScaleNum and its text
	 * field.
	 */
	protected void setSelection(int s, boolean updateText){
		if(max >= min){
			if(s < min) s = min;
			else if(s > max) s = max;
		}
		else{
			if(s > min) s = min;
			else if(s < max) s = max;
		}
		selection = s;
		button.setText("" + s);
		if(updateText) userInput.setText("" + s);
	}
	/**
	 * Set the default value
	 * @param d The default value
	 */
	public void setDefault(int d){
		def = d;
		checkMinMax();
	}
	/**
	 * Internal methodd for keeping the values for minimum, maximum, default
	 * and the current value in a consistent state
	 *
	 */
	private void checkMinMax(){
		if(max >= min){
			if(selection < min) setSelection(min);
			else if(selection > max) setSelection(max);
			if(def < min) def = min;
			else if(def > max) def = max;
		}
		else{
			if(selection > min) setSelection(min);
			else if(selection < max) setSelection(max);
			if(def > min) def = min;
			else if(def < max) def = max;
		}
		String tMin = new String("" + min);
		String tMax = new String("" + max);
		userInput.setTextLimit(Math.max(tMin.length(), tMax.length()));
	}
	/**
	 * Show the text field for user input
	 *
	 */
	private void startEditing(){
		if(readonly) return;
		userInput.setText(button.getText());
		layout.topControl = userInput;
		getComposite().layout();
		userInput.selectAll();
		editing = true;
	}
	/**
	 * Hide the text field
	 *
	 */
	private void finishEditing(){
		try{
			int newSel = Integer.parseInt(userInput.getText());
			setSelection(newSel);
		}
		catch(NumberFormatException ex){
			button.setText("" + selection);
		}
		layout.topControl = button;
		button.setSelection(false);
		getComposite().layout();
		editing = false;
	}
	/**
	 * Indicates that a mouse dragging has been started for changing the value
	 *
	 */
	private void startMouseDrag(){
		mousedown = true;
	}
	/**
	 * Indicates that mouse dragging has been stopped
	 *
	 */
	private void stopMouseDrag(){
		mousedown = false;
		speed = 0;
	}
	/**
	 * Get the current value
	 * @return The current value
	 */
	public int getSelection(){
		return selection;
	}
	/**
	 * Get the minimum value
	 * @return The minimum value
	 */
	public int getMinimum(){
		return min;
	}
	/**
	 * Get the maximum value
	 * @return The maximum value
	 */
	public int getMaximum(){
		return max;
	}
	/**
	 * Notify the registered SelectionListeners of a value change
	 * @param time The time at which this event was generated
	 */
	protected void notifyListeners(int time){
		Event event = new Event();
		event.time = time;
		event.type = SWT.Selection;
		event.widget = getComposite();
		event.data = ScaleNum.this;

		SelectionEvent selEvent = new SelectionEvent(event); 
		List listeners = getListeners(SWT.Selection);
		for(Iterator iter = listeners.iterator(); iter.hasNext(); ){
			((SelectionListener)iter.next()).widgetSelected(selEvent);
		}
	}
	/**
	 * Get the default value
	 * @return The default value
	 */
	public int getDefault(){
		return def;
	}
	/**
	 * Set the minimum, maximum and default value. The vcurrent value will be
	 * set to the default value.
	 * @param defaultSelected The default value
	 * @param minimum The minimum value
	 * @param maximum The maximum value
	 */
	public void setAll(int defaultSelected, int minimum, int maximum){
		setDefault(defaultSelected);
		setMinimum(minimum);
		setMaximum(maximum);
		setSelection(defaultSelected);
	}
	/**
	 * Set the default value and the current value to the specified value
	 * @param i The new value
	 */
	public void setDefaultAndSelection(int i){
		setDefault(i);
		setSelection(i);
	}
	protected Composite createComposite(Composite parent, int style) {
		int textStyle = SWT.SINGLE;
		int labStyle = SWT.NONE;
		
		if ((style & SWT.READ_ONLY) != 0){
			textStyle |= SWT.READ_ONLY;
			readonly = true;
		}
		if ((style & SWT.FLAT) != 0){
			textStyle |= SWT.FLAT;
			labStyle |= SWT.FLAT;
		}
		if ((style & SWT.BORDER) != 0){
			textStyle |= SWT.BORDER;
			labStyle |= SWT.BORDER;
		}
		if ((style & SWT.CENTER) != 0){
			textStyle |= SWT.CENTER;
			labStyle |= SWT.CENTER;
		}
		if ((style & SWT.LEFT) != 0){
			textStyle |= SWT.LEFT;
			labStyle |= SWT.LEFT;
		}
		if ((style & SWT.RIGHT) != 0){
			textStyle |= SWT.RIGHT;
			labStyle |= SWT.RIGHT;
		}
		
		Composite comp = new Composite(parent, SWT.NONE);
		userInput = new Text (comp, textStyle);
		button = new Button (comp, SWT.FLAT | SWT.TOGGLE | SWT.CENTER);
		layout = new StackLayout();
		comp.setLayout(layout);
		layout.topControl = button;
		
		userInput.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e){
				notifyListeners(e.time);
			}
		});
		userInput.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e){
				finishEditing();
			}
			public void widgetSelected(SelectionEvent e) {}
		});
		userInput.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e) {
				if(e.character == SWT.ESC) finishEditing();
			}
			public void keyReleased(KeyEvent e) {}
		});
		userInput.addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent e) {}
			public void focusLost(FocusEvent e) {
				finishEditing();
			}
		});
		
		button.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				button.setSelection(false);
				if(lastClick + dcTime >= e.time){
					startEditing();
				}
				lastClick = e.time;
			}
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		button.addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent e) {}
			public void focusLost(FocusEvent e) {
				button.setSelection(false);
			}
		});
		button.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent event) {
				switch (event.keyCode) {
					case SWT.ARROW_UP :
						setSelection(selection + 1);
						break;
					case SWT.ARROW_DOWN :
						setSelection(selection -1);
						break;
					case SWT.PAGE_UP :
						setSelection(selection + 10);
						break;
					case SWT.PAGE_DOWN :
						setSelection(selection - 10);
						break;
					case SWT.ESC :
						getParent().forceFocus();
						break;
					default :
						break;
				}
			}
			public void keyReleased(KeyEvent e) {}
		});
		button.addMouseListener(new MouseListener(){
			public void mouseDoubleClick(MouseEvent event) {
				if (event.button != 1) return;
				else startEditing();
			}
			public void mouseDown(MouseEvent event) {
				if (event.button == 1){
					lastChange = button.getDisplay().getCursorLocation().y;
					lastX = button.getDisplay().getCursorLocation().x;
					startMouseDrag();
				}
				else if(mousedown){
					setSelection(getDefault());				
				}
			}
			public void mouseUp(MouseEvent event) {
				if (event.button != 1) return;
				stopMouseDrag();
			}
		});
		button.addMouseMoveListener(new MouseMoveListener(){
			public void mouseMove(MouseEvent event) {
				if(mousedown){
					int pos = button.getDisplay().getCursorLocation().y;
					button.getDisplay().setCursorLocation(lastX, lastChange);
					if(pos == lastChange) return;
					else speed = Math.abs(pos - lastChange) / (event.time - lastMove);
					int step = (int)Math.floor(speed * (2 + speed)) + 1;
					if(pos >= lastChange + MOUSE_OFFSET){
						setSelection(selection - step);
					}
					else if(pos <= lastChange - MOUSE_OFFSET){
						setSelection(selection + step);
					}
					lastMove = event.time;
				}
			}
		});
		
		return comp;
	}
	protected int getPossibleStyles() {
		return SWT.FLAT |
			SWT.READ_ONLY | 
			SWT.BORDER | 
			SWT.CENTER |
			SWT.LEFT |
			SWT.RIGHT;
	}
	protected int[] getListenerTypes() {
		return new int[]{SWT.Selection};
	}
}
