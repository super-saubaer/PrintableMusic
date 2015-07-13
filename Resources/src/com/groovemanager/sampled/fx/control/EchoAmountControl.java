
package com.groovemanager.sampled.fx.control;

import javax.sound.sampled.FloatControl;

/**
 * Float Control subclass used for the SimpleEcho Effect to determine how
 * loud the echo should be.
 * @author Manu Robledo
 *
 */
public class EchoAmountControl extends FloatControl{
	/**
	 * Create a new EchoAmountControl
	 *
	 */
	public EchoAmountControl(){
		super(Type.AMOUNT, 0f, 100f, 0.1f, 1, 100f, "%");
	}
	/**
	 * The EchoAmount type
	 * @author Manu Robledo
	 *
	 */
	public static class Type extends FloatControl.Type{
		/**
		 * The AmountControl type
		 */
		public static Type AMOUNT = new Type();
		/**
		 * Construct a new AmountControl.Type
		 *
		 */
		protected Type() {
			super("Intensity");
		}
	}
}
