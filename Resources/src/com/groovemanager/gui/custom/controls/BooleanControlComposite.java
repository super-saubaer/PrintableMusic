/*
 * Created on 09.06.2004
 *
 */
package com.groovemanager.gui.custom.controls;

import javax.sound.sampled.BooleanControl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.groovemanager.gui.custom.CustomComposite;

/**
 * This class is used to make a BooleanControl editable for the user
 * @author Manu Robledo
 *
 */
public class BooleanControlComposite extends CustomComposite implements ControlContainer{
	/**
	 * The BooleanControl represented by this Composite
	 */
	private BooleanControl control;
	/**
	 * Button for true value
	 */
	private Button first,
	/**
	 * Button for false value
	 */
	second;
	/**
	 * Indicates whether auto-apply is set to on or off
	 */
	private boolean auto;
	/**
	 * Construct a new BooleanControlComposite
	 * @param control The BooleanControl to edit
	 * @param parent The parent Composite
	 * @param style Combination of SWT.* style constants
	 */
	public BooleanControlComposite(BooleanControl control, Composite parent, int style) {
		super(parent, style);
		this.control = control;
		Group group = (Group)getComposite();
		group.setText(control.getType().toString());
		first = new Button(group, SWT.RADIO);
		first.setText(control.getStateLabel(true));
		first.setSelection(control.getValue());
		second = new Button(group, SWT.RADIO);
		second.setText(control.getStateLabel(false));
		second.setSelection(!control.getValue());
		SelectionListener listener = new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if(auto) apply();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};
		first.addSelectionListener(listener);
		second.addSelectionListener(listener);

	}
	/**
	 * Construct a new BooleanControlComposite using SWT.NONE style constant
	 * @param control The BooleanControl to edit
	 * @param parent The parent Composite
	 */
	public BooleanControlComposite(BooleanControl control, Composite parent) {
		this(control, parent, SWT.NONE);
	}
	/**
	 * @see com.groovemanager.gui.custom.controls.ControlContainer#apply()
	 */
	public void apply() {
		control.setValue(first.getSelection());
	}
	/**
	 * @see com.groovemanager.gui.custom.controls.ControlContainer#reset()
	 */
	public void reset() {
		first.setSelection(control.getValue());
		second.setSelection(!control.getValue());
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
		Group group = new Group(parent, style);
		group.setLayout(new GridLayout(2, true));
		return group;
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