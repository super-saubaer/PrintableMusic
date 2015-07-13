/*
 * Created on 30.05.2004
 *
 */
package com.groovemanager.sampled.nondestructive;

import java.io.File;

/**
 * A CutListElement is used to add a modification to a CutList. The main
 * task of a CutListElement is to create a modified CutListSource out of a
 * given CutListSource.
 * @author Manu Robledo
 *
 */
public interface CutListElement {
	/**
	 * Create a new CutListSource out of the given source including this
	 * CutListElement´s modification.
	 * @param source The CutListSource to modify
	 * @return A modified CutListSource
	 */
	public CutListSource getResult(CutListSource source);
	/**
	 * Add a ModificationListener to this CutListElement that will be notified
	 * of any changes made to this CutListElement
	 * @param listener The ModificationListener to add
	 */
	public void addModificationListener(ModificationListener listener);
	/**
	 * Remove a ModificationListener from this CutListElement
	 * @param listener The ModificationListener to remove
	 */
	public void removeModificationListener(ModificationListener listener);
	/**
	 * Create a new CutListElement out of this one that is independent of any
	 * changes made to this one.
	 * @return An independent copy of this CutListElement
	 */
	public CutListElement duplicate();
	/**
	 * Get this CutListElement´s name
	 * @return A short String describing, what this CutListElement does (e.g.
	 * "Delete") 
	 */
	public String getName();
	/**
	 * Ask this CutListElement whether it relies on the contents of the given
	 * file. This is needed, if a file should be overwritten and it must be
	 * checked that this doesn´t cause any inconsistencies
	 * @param f The file to ask for
	 * @return true, if a change to the given file´s content would also mean
	 * a change to this element, false otherwise
	 */
	public boolean usesFile(File f);
	/**
	 * Replace all references to the given source file inside this
	 * CutListElement with references to the given target file. This may be
	 * because the source file is about to change and to avoid inconsistencies.
	 * If the given source file is not used by this CutListElement, the call
	 * can be ignored.
	 * @param from The source file to replace
	 * @param to The target file to replace with
	 */
	public void replaceFile(File from, File to);
}
