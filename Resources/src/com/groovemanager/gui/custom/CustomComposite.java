/*
 * Created on 28.06.2004
 *
 */
package com.groovemanager.gui.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * This class can be subclassed for implementation of own custom widgets.
 * 
 * @author Manu Robledo
 *
 */
public abstract class CustomComposite {
	/**
	 * Array of SWT.* constants for the types of listeners supported by this
	 * Composite
	 */
	private final int[] listenerTypes;
	/**
	 * 
	 */
	private HashMap listeners = new HashMap();
	/**
	 * User-defineable data assigned to this Composite
	 */
	private Object data;
	/**
	 * The main Composite containing all elements.
	 */
	private final Composite mainComp;
	/**
	 * Create a new CustomComposite with the given parent and the given style
	 * @param parent The parent Composite under which this Composite should
	 * be created
	 * @param style Combination of valid SWT.* style constants
	 */
	public CustomComposite(Composite parent, int style) {
		style = checkStyle(style);
		mainComp = createComposite(parent, style);
		listenerTypes = getListenerTypes();
		for (int i = 0; i < listenerTypes.length; i++) {
			listeners.put(new Integer(listenerTypes[i]), new ArrayList());
		}
	}
	/**
	 * Create a new CustomComposite with the given parent and the style
	 * constant <code>SWT.NONE</code>
	 * @param parent The parent Composite under which this Composite should
	 * be created
	 */
	public CustomComposite(Composite parent){
		this(parent, SWT.NONE);
	}
	/**
	 * This method is to be overwritten by concrete subclasses to create the
	 * real contents of this CustomComposite 
	 * @param parent The parent Composite under which this Composite should
	 * be created
	 * @param style Combination of valid SWT.* style constants
	 * @return The new created main composite
	 */
	protected abstract Composite createComposite(Composite parent, int style);
	/**
	 * Get a combination of all allowed SWT.* style constants
	 * @return A combination of all allowed style constants, for example
	 * <code>SWT.BORDER | SWT.LEFT | SWT.BORDER</code>
	 */
	protected abstract int getPossibleStyles();
	/**
	 * Check, if the given combination of style constants is a valid one and
	 * change it to a valid one if not. The default implementation only ensures
	 * that only <code>SWT.NONE</code> or the values obtained from
	 * <code>getPossibleStyles()</code> pass through this method. Subclasses
	 * should implement a chekc for valid combinations also if needed.
	 * @param style The combination of SWT.* style constants to check
	 * @return The value of <code>style</code>, if it is a valid one. Otherwise
	 * the best matching allowed combination
	 */
	protected int checkStyle(int style){
		return SWT.NONE & style & getPossibleStyles();
	}
	/**
	 * Get the main Composite of this CustomComposite. It can be used for
	 * layouting, etc.
	 * @return This CustomComposite큦 main Composite
	 */
	public Composite getComposite(){
		return mainComp;
	}
	/**
	 * Get the parent Composite of this CustomComposite큦 main Composite
	 * @return This CustomComposite큦 parent Composite
	 */
	public Composite getParent(){
		return mainComp.getParent();
	}
	/**
	 * Set the layout data of this CustomComposite큦 main Composite
	 * @param data The LayoutData to set
	 */
	public void setLayoutData(Object data){
		mainComp.setLayoutData(data);
	}
	/**
	 * Assign a user-defineable data to this Composite 
	 * @param data The user-defined data to set
	 */
	public void setData(Object data){
		this.data = data;
	}
	/**
	 * Get the user-defined data assigned to this Composite
	 * @return The data assigned to this Composite or null, if no data has
	 * been assigned
	 */
	public Object getData(){
		return data;
	}
	/**
	 * For all types of listeners that should be possible to add to this
	 * Composite directly, the corresponding SWT.* constant should be
	 * contained in the returned Array.
	 * @return An Array of SWT.* constants defining the set of listeners
	 * types applicable to this Composite
	 */
	protected abstract int[] getListenerTypes();
	/**
	 * Add a listener of the specified type to this Composite
	 * @param listener The listener to add
	 * @param type The SWT.* constant defining the type of the listener
	 */
	protected void addListener(Object listener, int type){
		Integer i = new Integer(type);
		if(listeners.containsKey(i)) ((ArrayList)listeners.get(i)).add(listener);
	}
	/**
	 * Remove a listener of the specified type from this Composite
	 * @param listener The listener to remove
	 * @param type The SWT.* constant defining the type of the listener
	 */
	protected void removeListener(Object listener, int type){
		Integer i = new Integer(type);
		if(listeners.containsKey(i)) ((ArrayList)listeners.get(i)).remove(listener);
	}
	/**
	 * Get a list of all listeners of a specific type
	 * @param type The SWT.* constant defining the type of the listener
	 * @return The list of listeners of the specified type or null if no
	 * this type is not supported by this Composite
	 */
	protected List getListeners(int type){
		Integer i = new Integer(type);
		if(listeners.containsKey(i)) return (ArrayList)listeners.get(i);
		else return null;
	}
}