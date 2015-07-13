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
 * A dummy implementation of a delay Effect. The delay time depends on the
 * buffer size...
 * @author Manu Robledo
 *
 */
public class SimpleEcho implements Effect {
	/**
	 * The Control for controlling the intensity of the echo
	 */
	EchoAmountControl amountControl = new EchoAmountControl();
	/**
	 * Indicates whether this Effect is currently open or not
	 */
	boolean open;
	/**
	 * Temporary variables
	 */
	float[] left, right, temp, left2, right2;
	/**
	 * @see com.groovemanager.sampled.fx.Effect#getControls()
	 */
	public Control[] getControls() {
		return new Control[]{amountControl};
	}
	/**
	 * @see com.groovemanager.sampled.fx.Effect#isControlSupported(javax.sound.sampled.Control.Type)
	 */
	public boolean isControlSupported(Type type) {
		return type.equals(EchoAmountControl.Type.AMOUNT);
	}
	/**
	 * @see com.groovemanager.sampled.fx.Effect#getControl(javax.sound.sampled.Control.Type)
	 */
	public Control getControl(Type type) {
		if(isControlSupported(type)) return amountControl;
		return null;
	}
	/**
	 * 
	 * @see com.groovemanager.sampled.fx.Effect#open(float)
	 */
	public void open(float sampleRate) {
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
	public void process(FloatBuffer[] in, FloatBuffer[] out) {
		if(temp == null || temp.length < in[0].remaining()) temp = new float[in[0].remaining()];
		int length = in[0].remaining();
		in[0].get(temp, 0, length);
		if(left == null || left.length < length) left = new float[length];
		for(int i = 0; i < length; i++){
			out[0].put(temp[i] + amountControl.getValue() / 100.0f * left[i]);
			left[i] = temp[i];
		}
		in[1].get(temp, 0, length);
		if(right == null || right.length < length) right = new float[length];
		for(int i = 0; i < length; i++){
			out[1].put(temp[i] + amountControl.getValue() / 100.0f * right[i]);
			right[i] = temp[i];
		}
	}
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
	public Effect undoEffect() {
		return null;
	}
	/**
	 * @see com.groovemanager.sampled.fx.Effect#isUndoable()
	 */
	public boolean isUndoable() {
		return false;
	}
	/**
	 * @see com.groovemanager.sampled.fx.Effect#getName()
	 */
	public String getName() {
		return "Pseudo Echo";
	}
}
