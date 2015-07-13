/*
 * Created on 09.06.2004
 *
 */
package com.groovemanager.app.sse;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.groovemanager.gui.custom.controls.*;
import com.groovemanager.sampled.fx.Effect;

/**
 * This class represents a dialog that will be shown to the user before
 * applying an effect.
 * @author Manu Robledo
 *
 */
public class EffectDialog extends TitleAreaDialog{
	/**
	 * The effect to apply
	 */
	private Effect effect;
	/**
	 * The WaveDisplay to which the effect shold be applied
	 */
	private WaveTab tab;
	/**
	 * The ControlComposite containing the effect´s controls
	 */
	private ControlComposite comp;
	/**
	 * Create a new EffectDialog
	 * @param effect The effect to apply
	 * @param tab The WaveTab to apply the effect to
	 */
	public EffectDialog(Effect effect, WaveTab tab) {
		super(tab.getShell());
		this.effect = effect;
		this.tab = tab;
	}
	protected Control createDialogArea(Composite parent) {
		comp = new ControlComposite(parent);
		javax.sound.sampled.Control[] controls = effect.getControls();
		for(int i = 0; i < controls.length; i++){
			comp.addControl(controls[i]);
		}
		setTitle("Apply " + effect.getName());
		setMessage("Set the values you want and click OK");
		comp.getComposite().pack();
		return comp.getComposite();
	}
	protected void okPressed() {
		comp.apply();
		super.okPressed();
	}
}