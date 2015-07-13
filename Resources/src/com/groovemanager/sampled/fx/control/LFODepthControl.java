/*
 * Created on 05.08.2004
 *
 */
package com.groovemanager.sampled.fx.control;

import javax.sound.sampled.FloatControl;

/**
 * Control for setting the LFO depth
 * @author Manu Robledo
 *
 */
public class LFODepthControl extends FloatControl {
	public LFODepthControl(float minimum, float maximum,
			float precision, float initialValue) {
		super(Type.LFO_DEPTH, minimum, maximum, precision, 0, initialValue, "ms");
	}
	public static class Type extends FloatControl.Type{
		public final static Type LFO_DEPTH = new Type();
		protected Type(){
			super("LFO Depth");
		}
	}
}
