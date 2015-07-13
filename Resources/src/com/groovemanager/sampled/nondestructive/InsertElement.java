/*
 * Created on 31.05.2004
 *
 */
package com.groovemanager.sampled.nondestructive;

import java.io.File;

/**
 * A CutListElement that inserts another CutListSource at a specified position.
 * Can be used for paste operations
 * @author Manu Robledo
 *
 */
public class InsertElement extends AbstractElement {
	/**
	 * The source to insert
	 */
	private CutListSource source;
	/**
	 * The position in sample frames where to insert the source
	 */
	private int position;
	/**
	 * Create a new InsertElement
	 * @param source The source to insert
	 * @param position The position where to insert the source
	 */
	public InsertElement(CutListSource source, int position){
		super("Paste");
		this.source = source;
		this.position = position;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListElement#getResult(com.groovemanager.sampled.nondestructive.CutListSource)
	 */
	public CutListSource getResult(CutListSource source) {
		if(position == 0){
			return new ConcatSource(new CutListSource[]{this.source, source});
		}
		else if(position == source.getLength()){
			return new ConcatSource(new CutListSource[]{source, this.source});
		}
		else{
			CutListSource[] sources = new CutListSource[3];
			sources[0] = new SubSource(source, 0, position);
			sources[1] = this.source;
			sources[2] = new SubSource(source, position, source.getLength() - position);
			return new ConcatSource(sources);
		}
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListElement#duplicate()
	 */
	public CutListElement duplicate() {
		return new InsertElement(source.duplicate(), position); 
	}
	/**
	 * @see com.groovemanager.sampled.nondestructive.CutListElement#usesFile(java.io.File)
	 */
	public boolean usesFile(File f) {
		return source.usesFile(f);
	}
	/**
	 * @see com.groovemanager.sampled.nondestructive.CutListElement#replaceFile(java.io.File, java.io.File)
	 */
	public void replaceFile(File from, File to) {
		if(source.usesFile(from)){
			source.replaceFile(from, to);
			notifyListeners();
		}
	}
}