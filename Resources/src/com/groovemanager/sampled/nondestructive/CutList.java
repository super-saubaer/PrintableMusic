/*
 * Created on 30.05.2004
 *
 */
package com.groovemanager.sampled.nondestructive;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.sound.sampled.AudioInputStream;

import com.groovemanager.exception.NotReadyException;
import com.groovemanager.sampled.waveform.WaveForm;

/**
 * A CutList consists of a CutListSource and a stack of CutListElements that
 * acts as a CutListSource itself. When queried for an AudioInputStream or a
 * WaveForm, the CutList will handle over the request to the top CutListElement,
 * which handles it over to the next one and so on. Each CutListElement adds
 * its own modification to the CutList. The result will then be an
 * AudioinputStream or a WaveForm containing all modifications of the CutList.
 * @author Manu Robledo
 *
 */
public class CutList extends AbstractSource implements CutListSource, ModificationListener{
	/**
	 * List of CutListElements contained in this CutList
	 */
	private ArrayList elements = new ArrayList();
	/**
	 * The CutListSource of this CutList
	 */
	private CutListSource source;
	/**
	 * The last result of the query through all CutListEleemnts. As long as no
	 * modification happens to any this CutList큦 elements or to its source,
	 * this result can be used again and again to avoid having to calculate it
	 * for each request.
	 */
	private CutListSource lastResult;
	/**
	 * Create a new CutList using the given source
	 * @param source The CutListSource to get the audio data from
	 */
	public CutList(CutListSource source){
		lastResult = this.source = source;
		source.addModificationListener(this);
	}
	/**
	 * Add a CutListElement to the top of this CutList
	 * @param el The CutListEleemnt to add
	 */
	public void addElement(CutListElement el){
		elements.add(el);
		el.addModificationListener(this);
		lastResult = null;
		notifyListeners();
	}
	/**
	 * Remove a CutListElement from this CutList
	 * @param el The CutListElement to remove
	 */
	public void removeElement(CutListElement el){
		elements.remove(el);
		el.removeModificationListener(this);
		lastResult = null;
		notifyListeners();
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#getAudioInputStream(int, int)
	 */
	public AudioInputStream getAudioInputStream(int start, int length) throws NotReadyException{
		if(lastResult == null) calcLastResult();
		return lastResult.getAudioInputStream(start, length);
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#getWaveForm(int, int, int)
	 */
	public WaveForm getWaveForm(int start, int length, int width) {
		if(lastResult == null) calcLastResult();
		return lastResult.getWaveForm(start, length, width);
	}
	/**
	 * Notification that a change has been made to this CutList
	 *
	 */
	public void update() {
		lastResult = null;
		notifyListeners();
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.ModificationListener#update(com.groovemanager.sampled.nondestructive.CutListSource)
	 */
	public void update(CutListSource source) {
		if(this.source == source) update();
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.ModificationListener#update(com.groovemanager.sampled.nondestructive.CutListElement)
	 */
	public void update(CutListElement element) {
		if(elements.contains(element)) update();
	}
	/**
	 * Calc the resulting CutListSource element by quering through all
	 * CutListElements
	 *
	 */
	protected void calcLastResult(){
		lastResult = source;
		for (Iterator iter = elements.iterator(); iter.hasNext();) {
			CutListElement element = (CutListElement) iter.next();
			lastResult = element.getResult(lastResult);
		}
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#isReady()
	 */
	public boolean isReady(){
		if(lastResult == null) calcLastResult();
		return lastResult.isReady();
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#getLength()
	 */
	public int getLength(){
		if(lastResult == null) calcLastResult();
		return lastResult.getLength();
	}
	/**
	 * Set this CutList큦 source
	 * @param source The new CutListSource
	 */
	public void setSource(CutListSource source){
		lastResult = null;
		this.source.removeModificationListener(this);
		this.source = source;
		source.addModificationListener(this);
		notifyListeners();
	}
	/**
	 * Get the top element of this CutList
	 * @return This CutList큦 top element or <code>null</code>, if it has no
	 * elements
	 */
	public CutListElement getLastElement(){
		if(elements.size() > 0) return (CutListElement)elements.get(elements.size() - 1);
		else return null;
	}
	/**
	 * Remove this CutList큦 top element
	 *
	 */
	public void removeLastElement(){
		CutListElement el = getLastElement();
		if(el != null) remove(el);
	}
	/**
	 * Remove a CutListElement from this CutList
	 * @param el The CutListElement to remove
	 */
	public void remove(CutListElement el){
		elements.remove(el);
		update();
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#duplicate()
	 */
	public CutListSource duplicate() {
		CutList newCutList = new CutList(source.duplicate());
		for (Iterator iter = elements.iterator(); iter.hasNext();) {
			CutListElement element = (CutListElement) iter.next();
			newCutList.addElement(element.duplicate());
		}
		return newCutList;
	}
	/**
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#usesFile(java.io.File)
	 */
	public boolean usesFile(File f) {
		if(source.usesFile(f)) return true;
		for (Iterator iter = elements.iterator(); iter.hasNext();) {
			CutListElement element = (CutListElement) iter.next();
			if(element.usesFile(f)) return true;
		}
		return false;
	}
	/**
	 * @see com.groovemanager.sampled.nondestructive.CutListSource#replaceFile(java.io.File, java.io.File)
	 */
	public void replaceFile(File from, File to) {
		if(source.usesFile(from)) source.replaceFile(from, to);
		for (Iterator iter = elements.iterator(); iter.hasNext();) {
			CutListElement element = (CutListElement) iter.next();
			if(element.usesFile(from)) element.replaceFile(from, to);
		}
	}
}