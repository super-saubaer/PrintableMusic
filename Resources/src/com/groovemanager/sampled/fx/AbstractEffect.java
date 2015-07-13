/*
 * Created on 25.06.2004
 *
 */
package com.groovemanager.sampled.fx;

import java.nio.FloatBuffer;

import javax.sound.sampled.Control;
import javax.sound.sampled.Control.Type;

/**
 * Abstract superclass for implementation of effects that don´t need
 * analyzation of the audio data before they can process it
 * @author Manu Robledo
 *
 */
public abstract class AbstractEffect implements Effect {
	/**
	 * The effect name
	 */
	private final String name;
	/**
	 * Indicates whether this effect is currently open or closed
	 */
	private boolean open;
	/**
	 * The current sampleRate provided by the last call to <code>open()</code>
	 * or <code>startAnalysis()</code>
	 */
	protected float sampleRate;
	/**
	 * Construct a new effect
	 * @param name The effect name
	 */
	public AbstractEffect(String name) {
		this.name = name;
	}
	/**
	 * @see com.groovemanager.sampled.fx.Effect#getControls()
	 */
	public abstract Control[] getControls();
	/**
	 * @see com.groovemanager.sampled.fx.Effect#isControlSupported(javax.sound.sampled.Control.Type)
	 */
	public abstract boolean isControlSupported(Type type);
	/**
	 * @see com.groovemanager.sampled.fx.Effect#getControl(javax.sound.sampled.Control.Type)
	 */
	public abstract Control getControl(Type type);
	/**
	 * 
	 * @see com.groovemanager.sampled.fx.Effect#open(float)
	 */
	public void open(float sampleRate) {
		this.sampleRate = sampleRate;
		open = true;
	}
	/**
	 * @see com.groovemanager.sampled.fx.Effect#isOpen()
	 */
	public boolean isOpen() {
		return open;
	}
	/**
	 * @see com.groovemanager.sampled.fx.Effect#close()
	 */
	public void close() {
		open = false;
	}
	/**
	 * @see com.groovemanager.sampled.fx.Effect#process(java.nio.FloatBuffer[], java.nio.FloatBuffer[])
	 */
	public abstract void process(FloatBuffer[] in, FloatBuffer[] out);
	/**
	 * 
	 * @see com.groovemanager.sampled.fx.Effect#startAnalysis(float)
	 */
	public void startAnalysis(float sampleRate) {
	}
	/**
	 * @see com.groovemanager.sampled.fx.Effect#analyze(java.nio.FloatBuffer[])
	 */
	public void analyze(FloatBuffer[] in) {
	}
	/**
	 * @see com.groovemanager.sampled.fx.Effect#stopAnalysis()
	 */
	public void stopAnalysis() {
	}
	/**
	 * @see com.groovemanager.sampled.fx.Effect#isAnalyzing()
	 */
	public boolean isAnalyzing() {
		return false;
	}
	/**
	 * @see com.groovemanager.sampled.fx.Effect#needsAnalysis()
	 */
	public boolean needsAnalysis() {
		return false;
	}
	/**
	 * @see com.groovemanager.sampled.fx.Effect#undoEffect()
	 */
	public abstract Effect undoEffect();
	/**
	 * @see com.groovemanager.sampled.fx.Effect#isUndoable()
	 */
	public abstract boolean isUndoable();
	/**
	 * @see com.groovemanager.sampled.fx.Effect#getName()
	 */
	public String getName() {
		return name;
	}
}
