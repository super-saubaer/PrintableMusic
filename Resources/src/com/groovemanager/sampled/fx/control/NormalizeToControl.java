/*
 * Created on 24.06.2004
 *
 */
package com.groovemanager.sampled.fx.control;

import javax.sound.sampled.FloatControl;

/**
 * Float Control subclass used for the Normalizer Effect to determine to
 * which maximum amplitude the audio data should be processed
 * @author Manu Robledo
 *
 */
public class NormalizeToControl extends FloatControl{
	/**
	 * Create a new NormalizeControl
	 *
	 */
	public NormalizeToControl(){
		super(Type.NORMALIZE_TO, 0f, 100f, 0.1f, 1, 100f, "%");
	}
	/**
	 * The NormalizeControl type
	 * @author Manu Robledo
	 *
	 */
	public static class Type extends FloatControl.Type{
		/**
		 * The NormalizeToControl type
		 */
		public static Type NORMALIZE_TO = new Type();
		/**
		 * Construct a new NormalizeToControl.Type
		 *
		 */
		protected Type() {
			super("Normalize to");
		}
	}
}
