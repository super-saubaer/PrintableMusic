/*
 * Created on 09.06.2004
 *
 */
package com.groovemanager.gui.custom.controls;

import javax.sound.sampled.EnumControl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.groovemanager.gui.custom.CustomComposite;

/**
 * @author Manu
 *
 */
public class EnumControlComposite extends CustomComposite implements ControlContainer{
	/**
	 * The EnumControl to edit
	 */
	private EnumControl control;
	/**
	 * The drop-down list for user-selection of the value
	 */
	private Combo combo;
	/**
	 * Indicates whether auto-apply is set to on or off
	 */
	private boolean auto;
	/**
	 * Construct a new EnumControlComposite
	 * @param control The EnumControl to edit
	 * @param parent The parent Composite
	 * @param style Combination of SWT.* style constants
	 */
	public EnumControlComposite(EnumControl control, Composite parent, int style){
		super(parent, style);
		this.control = control;
		Composite comp = getComposite();
		comp.setLayout(new GridLayout(2, false));
		new Label(comp, SWT.NONE).setText(control.getType().toString());
		combo = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
		Object[] values = control.getValues();
		for (int i = 0; i < values.length; i++) {
			combo.add(values[i].toString());
		}
		combo.setText(control.getValue().toString());
		combo.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				if(auto) apply();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
	/**
	 * Construct a new EnumControlComposite using SWT.NONE style 
	 * @param control The EnumControl to edit
	 * @param parent The parent Composite
	 */
	public EnumControlComposite(EnumControl control, Composite parent) {
		this(control, parent, SWT.NONE);
	}
	/**
	 * @see com.groovemanager.gui.custom.controls.ControlContainer#apply()
	 */
	public void apply() {
		Object[] values = control.getValues();
		String s = combo.getText();
		for (int i = 0; i < values.length; i++) {
			if(s.equals(values[i].toString())) control.setValue(values[i]);
		}
	}
	/**
	 * @see com.groovemanager.gui.custom.controls.ControlContainer#reset()
	 */
	public void reset() {
		combo.setText(control.getValue().toString());
	}
	/**
	 * @see com.groovemanager.gui.custom.controls.ControlContainer#setAutoApply(boolean)
	 */
	public void setAutoApply(boolean auto) {
		this.auto = auto;
	}
	/**
	 * @see com.groovemanager.gui.custom.CustomComposite#createComposite(org.eclipse.swt.widgets.Composite, int)
	 */
	protected Composite createComposite(Composite parent, int style) {
		Composite comp = new Composite(parent, style);
		return comp;
	}
	/**
	 * @see com.groovemanager.gui.custom.CustomComposite#getPossibleStyles()
	 */
	protected int getPossibleStyles() {
		return SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL;
	}
	/**
	 * @see com.groovemanager.gui.custom.CustomComposite#getListenerTypes()
	 */
	protected int[] getListenerTypes() {
		return new int[0];
	}
}