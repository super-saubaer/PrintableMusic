/*
 * Created on 04.08.2004
 *
 */
package com.groovemanager.sampled.fx.control;

import javax.sound.sampled.BooleanControl;

/**
 * Control class for bypass switch
 * @author Manu Robledo
 *
 */
public class BypassControl extends BooleanControl {
	public BypassControl(boolean initialValue) {
		super(Type.BYPASS, initialValue, "Bypass", "Off");
	}
	public static class Type extends BooleanControl.Type{
		public final static Type BYPASS = new Type();
		protected Type(){
			super("Bypass");
		}
	}
}
