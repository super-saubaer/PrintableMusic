/*
 * Created on 05.08.2004
 *
 */
package com.groovemanager.sampled.fx.control;

import javax.sound.sampled.EnumControl;

/**
 * Control for selection of LFO waveform
 * @author Manu Robledo
 *
 */
public class LFOWaveFormControl extends EnumControl {
	public LFOWaveFormControl(String[] values, String value) {
		super(Type.LFO_WAVEFORM, values, value);
	}
	public static class Type extends EnumControl.Type{
		public final static Type LFO_WAVEFORM = new Type();
		protected Type(){
			super("LFO Waveform");
		}
	}
}
