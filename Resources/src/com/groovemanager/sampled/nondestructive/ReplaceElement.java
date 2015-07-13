/*
 * Created on 24.06.2004
 *
 */
package com.groovemanager.sampled.nondestructive;

import java.io.File;

import javax.sound.sampled.AudioFormat;

/**
 * A ReplaceElement replaces a specified part of the CutListSource with another
 * CutListSource. It also takes care of format conversion so that the inserted
 * element has the same AudioFormat as the replaced element
 * @author Manu Robledo
 *
 */
public class ReplaceElement extends AbstractElement {
	/**
	 * Start position of the part to replace in sample frames
	 */
	private int start,
	/**
	 * Length of the part to replace in sample frames
	 */
	length;
	/**
	 * The source to replace the part with
	 */
	private CutListSource newSource;
	/**
	 * The format into which the resulting AudioInputStreams should all be
	 * converted 
	 */
	private AudioFormat targetFormat;
	/**
	 * Construct a new ReplaceElement
	 * @param name The ReplaceElement´s name. May be the name of an applied
	 * Effect for example
	 * @param newSource The new CutListSource to replace the specified part
	 * with
	 * @param start Start position of the part to replace in sample frames
	 * @param length Length of the part to replace in sample frames
	 * @param targetFormat The AudioFormat into which all resulting
	 * AudioInputStreams should be converted
	 */
	public ReplaceElement(String name, CutListSource newSource, int start, int length, AudioFormat targetFormat){
		super(name);
		this.start = start;
		this.length = length;
		this.newSource = newSource;
		this.targetFormat = targetFormat;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListElement#getResult(com.groovemanager.sampled.nondestructive.CutListSource)
	 */
	public CutListSource getResult(CutListSource source) {
		if(start >= source.getLength()){
			return new ConvertedSource(source, targetFormat);
		}
		else if(start == 0 && length >= source.getLength()){
			return new ConvertedSource(newSource, targetFormat);
		}
		else if(start == 0){
			CutListSource[] sources = new CutListSource[2];
			sources[0] = new ConvertedSource(newSource, targetFormat);
			sources[1] = new ConvertedSource(new SubSource(source, length, source.getLength() - length), targetFormat);
			return new ConcatSource(sources);
		}
		else if(start + length < source.getLength()){
			CutListSource[] sources = new CutListSource[3];
			sources[0] = new ConvertedSource(new SubSource(source, 0, start), targetFormat);
			sources[1] = new ConvertedSource(newSource, targetFormat);
			sources[2] = new ConvertedSource(new SubSource(source, start + length, source.getLength() - start - length), targetFormat);
			return new ConcatSource(sources);
		}
		else{
			CutListSource[] sources = new CutListSource[2];
			sources[0] = new ConvertedSource(new SubSource(source, 0, start), targetFormat);
			sources[1] = new ConvertedSource(newSource, targetFormat);
			return new ConcatSource(sources);
		}
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListElement#duplicate()
	 */
	public CutListElement duplicate() {
		return new ReplaceElement(getName(), newSource.duplicate(), start, length, targetFormat);
	}
	/**
	 * @see com.groovemanager.sampled.nondestructive.CutListElement#usesFile(java.io.File)
	 */
	public boolean usesFile(File f) {
		return newSource.usesFile(f);
	}
	/**
	 * @see com.groovemanager.sampled.nondestructive.CutListElement#replaceFile(java.io.File, java.io.File)
	 */
	public void replaceFile(File from, File to) {
		if(newSource.usesFile(from)){
			newSource.replaceFile(from, to);
			notifyListeners();
		}
	}
}