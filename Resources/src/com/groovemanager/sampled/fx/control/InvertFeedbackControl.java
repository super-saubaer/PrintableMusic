/*
 * Created on 05.08.2004
 *
 */
package com.groovemanager.sampled.fx.control;

import javax.sound.sampled.BooleanControl;

/**
 * Control for setting inversion of feedback
 * @author Manu Robledo
 *
 */
public class InvertFeedbackControl extends BooleanControl {
	public InvertFeedbackControl(boolean initialValue) {
		super(Type.INVERT_FEEDBACK, initialValue, "Yes", "No");
	}
	public static class Type extends BooleanControl.Type{
		public final static Type INVERT_FEEDBACK = new Type();
		protected Type(){
			super("Invert feedback");
		}
	}
}
