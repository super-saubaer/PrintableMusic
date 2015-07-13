/*
 * Created on 30.05.2004
 *
 */
package com.groovemanager.sampled.nondestructive;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Abstract subclass for implementation of CutListElements 
 * @author Manu Robledo
 *
 */
public abstract class AbstractElement implements CutListElement {
	/**
	 * List of ModificationListeners registered with this CutListElement
	 */
	private ArrayList listeners = new ArrayList();
	/**
	 * Name of this CutListElement 
	 */
	private final String name;
	/**
	 * Construct a new CutListElement with the given name
	 * @param name The name of the element
	 */
	public AbstractElement(String name){
		this.name = name;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListElement#addModificationListener(com.groovemanager.sampled.nondestructive.ModificationListener)
	 */
	public void addModificationListener(ModificationListener listener) {
		listeners.add(listener);
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListElement#removeModificationListener(com.groovemanager.sampled.nondestructive.ModificationListener)
	 */
	public void removeModificationListener(ModificationListener listener) {
		listeners.remove(listener);
	}
	/**
	 * Notify all registered ModificationListeners about a change that occured
	 * to this element
	 *
	 */
	protected void notifyListeners(){
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			ModificationListener listener = (ModificationListener) iter.next();
			listener.update(this);
		}
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListElement#getName()
	 */
	public String getName() {
		return name;
	}
}