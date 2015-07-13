/*
 * Created on 05.08.2004
 *
 */
package com.groovemanager.sampled.fx.control;

import javax.sound.sampled.FloatControl;

/**
 * Control for setting the delay time
 * @author Manu Robledo
 *
 */
public class DelayTimeControl extends FloatControl {
	public DelayTimeControl(float minimum, float maximum,
			float precision, float initialValue) {
		super(Type.DELAY_TIME, minimum, maximum, precision, 0, initialValue, "ms");
	}
	public static class Type extends FloatControl.Type{
		public final static Type DELAY_TIME = new Type();
		protected Type(){
			super("Delay time");
		}
	}
}
