/*
 * Created on 25.06.2004
 *
 */
package com.groovemanager.sampled.fx;

import java.nio.FloatBuffer;

/**
 * Abstract superclass to be used for implementations of Effects that need an
 * analyzation of the audio data before they can process it.
 * @author Manu Robledo
 *
 */
public abstract class AbstractAnalyzeEffect extends AbstractEffect {
	/**
	 * Indicates whether this Effect is currently analyzing or not
	 */
	private boolean analyzing;
	/**
	 * Construct a new Effect with the given name
	 * @param name The effect name
	 */
	public AbstractAnalyzeEffect(String name) {
		super(name);
	}
	/**
	 * @see com.groovemanager.sampled.fx.AbstractEffect#needsAnalysis()
	 */
	public boolean needsAnalysis() {
		return true;
	}
	/**
	 * @see com.groovemanager.sampled.fx.AbstractEffect#isAnalyzing()
	 */
	public boolean isAnalyzing() {
		return analyzing;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.fx.Effect#startAnalysis(float)
	 */
	public void startAnalysis(float sampleRate) {
		this.sampleRate = sampleRate;
		analyzing = true;
	}
	/**
	 * @see com.groovemanager.sampled.fx.AbstractEffect#stopAnalysis()
	 */
	public void stopAnalysis() {
		analyzing = false;
	}
	/**
	 * @see com.groovemanager.sampled.fx.AbstractEffect#analyze(java.nio.FloatBuffer[])
	 */
	public abstract void analyze(FloatBuffer[] in);
}
