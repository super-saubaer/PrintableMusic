/*
 * Created on 31.05.2004
 *
 */
package com.groovemanager.sampled.nondestructive;

import java.io.File;

/**
 * A CutListElement that deletes a specified part of the source
 * @author Manu Robledo
 *
 */
public class DeleteElement extends AbstractElement {
	/**
	 * Start position of the deleted part in sample frames
	 */
	private int start,
	/**
	 * Length of the deleted part in sample frames
	 */
	length;
	/**
	 * Create a new DeleteElement deleting the specified part
	 * @param start Start position of the part to delete in sample frames
	 * @param length Length of the part to delete in sample frames
	 */
	public DeleteElement(int start, int length){
		super("Delete");
		this.start = start;
		this.length = length;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListElement#getResult(com.groovemanager.sampled.nondestructive.CutListSource)
	 */
	public CutListSource getResult(CutListSource source) {
		if(length == 0) return source;
		else if(start == 0){
			return new SubSource(source, length, source.getLength() - length);
		}
		else if(start + length >= source.getLength()){
			return new SubSource(source, 0, source.getLength() - length);
		}
		else{
			CutListSource leftSource = new SubSource(source, 0, start);
			CutListSource rightSource = new SubSource(source, start + length, source.getLength() - start - length);
			return new ConcatSource(new CutListSource[]{leftSource, rightSource});
		}
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListElement#duplicate()
	 */
	public CutListElement duplicate() {
		return new DeleteElement(start, length);
	}
	/**
	 * @see com.groovemanager.sampled.nondestructive.CutListElement#usesFile(java.io.File)
	 */
	public boolean usesFile(File f) {
		return false;
	}
	/**
	 * @see com.groovemanager.sampled.nondestructive.CutListElement#replaceFile(java.io.File, java.io.File)
	 */
	public void replaceFile(File from, File to) {
	}
}