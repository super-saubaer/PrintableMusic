/*
 * Created on 04.08.2004
 *
 */
package com.groovemanager.sampled.fx.control;

import javax.sound.sampled.FloatControl;

/**
 * A Control for selection of Dry/Wet balance
 * @author Manu Robledo
 *
 */
public class DryWetControl extends FloatControl {
	public DryWetControl(float initialValue) {
		super(Type.DRY_WET, 0, 100, 1, 0, initialValue, "%", "Dry", "", "Wet");
	}
	public static class Type extends FloatControl.Type{
		public final static Type DRY_WET = new Type();
		protected Type() {
			super("Dry/Wet Balance");
		}
	}
}
