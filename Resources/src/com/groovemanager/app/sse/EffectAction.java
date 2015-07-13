/*
 * Created on 24.06.2004
 *
 */
package com.groovemanager.app.sse;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;

import com.groovemanager.sampled.fx.Effect;

/**
 * This subclass of Action is used to apply effects to the audio data of a
 * WaveTab.
 * @author Manu Robledo
 * 
 */
public class EffectAction extends Action {
	/**
	 * The effect큦 class
	 */
	protected final Class effectClass;
	/**
	 * The WaveTab to apply the effect to
	 */
	protected WaveTab wt;
	/**
	 * Create a new EffectAction for the given effect type with the given name
	 * @param text The action큦 name
	 * @param effectClass The class type of the effect
	 */
	public EffectAction(String text, Class effectClass) {
		super(text);
		this.effectClass = effectClass;
	}
	/**
	 * Create a new EffectAction for the given effect type with the given name
	 * and image.
	 * @param text The action큦 name
	 * @param image The action큦 image
	 * @param effectClass The class type of the effect
	 */
	public EffectAction(String text, ImageDescriptor image, Class effectClass) {
		super(text, image);
		this.effectClass = effectClass;
	}
	/**
	 * Create a new EffectAction for the given effect type with the given name
	 * and style.
	 * @param text The action큦 name
	 * @param style The action큦 style
	 * @param effectClass The class type of the effect
	 */
	public EffectAction(String text, int style, Class effectClass) {
		super(text, style);
		this.effectClass = effectClass;
	}
	/**
	 * Perform this action. When performed, a dialog is opened containing the
	 * effect큦 controls for manipulation. When clicked OK, the effect will
	 * be applyed to the WaveTab큦 CutList.
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		if(wt == null) return;
		try {
			Effect effect = (Effect)effectClass.newInstance();
			EffectDialog dialog = new EffectDialog(effect, wt);
			if(dialog.open() == IDialogConstants.OK_ID) wt.applyEffect(effect);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Set the WaveTab to which the effect should be applied. If the argument
	 * is <code>null</code>, the action will be disabled. Otherwise it will be
	 * enabled.
	 * @param wt The WaveTab to which the effect should be applied or
	 * <code>null</code>.
	 */
	public void setWaveTab(WaveTab wt){
		this.wt = wt;
		setEnabled(wt != null);
	}
}