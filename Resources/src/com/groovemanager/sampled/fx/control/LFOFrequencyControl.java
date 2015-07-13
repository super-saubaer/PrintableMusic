/*
 * Created on 05.08.2004
 *
 */
package com.groovemanager.sampled.fx.control;

import javax.sound.sampled.FloatControl;

/**
 * Control for setting LFO frequency
 * @author Manu Robledo
 *
 */
public class LFOFrequencyControl extends FloatControl {
	public LFOFrequencyControl(float minimum, float maximum,
			float precision, float initialValue) {
		super(Type.LFO_FREQUENCY, minimum, maximum, precision, 0, initialValue, "Hz");
	}
	public static class Type extends FloatControl.Type{
		public final static Type LFO_FREQUENCY = new Type();
		public Type(){
			super("LFO Frequency");
		}
	}
}
