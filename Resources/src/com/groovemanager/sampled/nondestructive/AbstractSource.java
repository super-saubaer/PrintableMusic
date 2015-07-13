/*
 * Created on 30.05.2004
 *
 */
package com.groovemanager.sampled.nondestructive;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Abstract superclass for the implementation of a CutListSource
 * @author Manu Robledo
 *
 */
public abstract class AbstractSource implements CutListSource {
	/**
	 * List of ModificationListeners registered with this CutListSource
	 */
	private ArrayList listeners = new ArrayList();
	/**
	 * Notify all registered ModificationListeners about a change made to this
	 * CutListSource
	 *
	 */
	protected void notifyListeners(){
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			ModificationListener element = (ModificationListener) iter.next();
			element.update(this);
		}
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#addModificationListener(com.groovemanager.sampled.nondestructive.ModificationListener)
	 */
	public void addModificationListener(ModificationListener listener) {
		listeners.add(listener);
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#removeModificationListener(com.groovemanager.sampled.nondestructive.ModificationListener)
	 */
	public void removeModificationListener(ModificationListener listener) {
		listeners.remove(listener);
	}
}
