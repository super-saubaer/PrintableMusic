/*
 * Created on 04.08.2004
 *
 */
package com.groovemanager.sampled.fx.control;

import javax.sound.sampled.FloatControl;

/**
 * A Control for selection of feedback level
 * @author Manu Robledo
 *
 */
public class FeedbackControl extends FloatControl {
	public FeedbackControl(float initialValue) {
		super(Type.FEEDBACK, 0, 100, 1, 0, initialValue, "%");
	}
	public static class Type extends FloatControl.Type{
		public final static Type FEEDBACK = new Type();
		protected Type() {
			super("Feedback level");
		}
	}
}
