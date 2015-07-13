/*
 * Created on 24.06.2004
 *
 */
package com.groovemanager.sampled.fx;

import java.nio.FloatBuffer;

import javax.sound.sampled.Control;
import javax.sound.sampled.Control.Type;

import com.groovemanager.sampled.fx.control.*;

/**
 * A Normalizer Effect analyzes the whole audio data for it´s maximum
 * amplitude and then processes the data so that the resulting data has a
 * maximum amplitude of the alue specified in the NormalizeToControl (in %)
 * @author Manu Robledo
 *
 */
public class Normalizer extends AbstractAnalyzeEffect {
	/**
	 * The maximum amplitude gathered from analysis
	 */
	float maxAmp = 0;
	/**
	 * FloatControl for selecting the max amplitude for normalization
	 */
	NormalizeToControl normControl = new NormalizeToControl();
	/**
	 * Construct a new Normalizer Effect
	 *
	 */
	public Normalizer(){
		super("Normalize");
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.fx.Effect#getControls()
	 */
	public Control[] getControls() {
		return new Control[]{normControl};
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.fx.Effect#isControlSupported(javax.sound.sampled.Control.Type)
	 */
	public boolean isControlSupported(Type type) {
		return type.equals(NormalizeToControl.Type.NORMALIZE_TO);
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.fx.Effect#getControl(javax.sound.sampled.Control.Type)
	 */
	public Control getControl(Type type) {
		if(isControlSupported(type)) return normControl;
		else return null;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.fx.Effect#process(java.nio.FloatBuffer[], java.nio.FloatBuffer[])
	 */
	public void process(FloatBuffer[] in, FloatBuffer[] out) {
		while(in[0].hasRemaining()){
			out[0].put(in[0].get() / maxAmp * normControl.getValue() / 100f);
			out[1].put(in[1].get() / maxAmp * normControl.getValue() / 100f);
		}
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.fx.Effect#startAnalysis(float)
	 */
	public void startAnalysis(float sampleRate) {
		super.startAnalysis(sampleRate);
		if(isAnalyzing()) return;
		maxAmp = 0;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.fx.Effect#analyze(java.nio.FloatBuffer[])
	 */
	public void analyze(FloatBuffer[] in) {
		while(in[0].hasRemaining()){
			maxAmp = Math.max(maxAmp, Math.abs(in[0].get()));
			maxAmp = Math.max(maxAmp, Math.abs(in[1].get()));
		}
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.fx.Effect#isUndoable()
	 */
	public boolean isUndoable(){
		return true;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.fx.Effect#undoEffect()
	 */
	public Effect undoEffect() {
		final Normalizer undo = this;
		return new Effect() {
			private boolean open;
			public Control[] getControls() {
				return new Control[0];
			}
			public boolean isControlSupported(Type type) {
				return false;
			}
			public Control getControl(Type type) {
				return null;
			}
			public void open(float sampleRate) {
				open = true;
			}
			public boolean isOpen() {
				return open;
			}
			public void close() {
				open = false;
			}
			public void process(FloatBuffer[] in, FloatBuffer[] out) {
				while(in[0].hasRemaining()){
					out[0].put(in[0].get() * maxAmp / normControl.getValue() * 100f);
					out[1].put(in[1].get() * maxAmp / normControl.getValue() * 100f);
				}
			}
			public void startAnalysis(float sampleRate) {
			}
			public void analyze(FloatBuffer[] in) {
			}
			public void stopAnalysis() {
			}
			public boolean isAnalyzing() {
				return false;
			}
			public boolean needsAnalysis() {
				return false;
			}
			public Effect undoEffect() {
				return undo;
			}
			public boolean isUndoable() {
				return true;
			}
			public String getName(){
				return "Undo Normalize";
			}
		};
	}
}
