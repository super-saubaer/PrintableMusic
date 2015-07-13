/*
 * Created on 30.05.2004
 *
 */
package com.groovemanager.sampled.nondestructive;

/**
 * A ModificationListener instance can be notified of a change made to a
 * CutListSource or a CutListElement
 * @author Manu Robledo
 *
 */
public interface ModificationListener {
	/**
	 * Notification about the change of a CutListSource 
	 * @param source The modified CutListSource
	 */
	public void update(CutListSource source);
	/**
	 * Notification about the change of a CutListElement 
	 * @param element The modified CutListElement
	 */
	public void update(CutListElement element);
}
