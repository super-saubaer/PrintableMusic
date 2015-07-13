/*
 * Created on 09.06.2004
 *
 */
package com.groovemanager.gui.custom.controls;

import java.util.ArrayList;
import java.util.Iterator;

import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.CompoundControl;
import javax.sound.sampled.Control;
import javax.sound.sampled.EnumControl;
import javax.sound.sampled.FloatControl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.groovemanager.gui.custom.CustomComposite;

/**
 * This class can be used for user control of CompoundControl instances as
 * well as any other set of Control instances
 * @author Manu Robledo
 *
 */
public class ControlComposite extends CustomComposite implements ControlContainer{
	/**
	 * List of ControlContainer instances assigned to this Composite
	 */
	private ArrayList controls = new ArrayList();
	/**
	 * Construct a new ControlComposite
	 * @param parent The parent Composite
	 * @param style Combination of SWT.* style constants
	 */
	public ControlComposite(Composite parent, int style) {
		super(parent, style);
	}
	/**
	 * Construct a new ControlComposite with SWT.NONE style
	 * @param parent The parent Composite
	 */
	public ControlComposite(Composite parent){
		this(parent, SWT.NONE);
	}
	/**
	 * Add a Control of type CompoundControl to this Composite
	 * @param c The CompoundControl to add
	 */
	public void addCompoundControl(CompoundControl c){
		Group g = new Group(getComposite(), SWT.NONE);
		g.setText(c.getType().toString());
		g.setLayout(new FillLayout());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		g.setLayoutData(gd);
		ControlComposite ccomp = new ControlComposite(g, SWT.NONE);
		controls.add(ccomp);
		Control[] cs = c.getMemberControls();
		for (int i = 0; i < cs.length; i++) {
			ccomp.addControl(cs[i]);
		}
	}
	/**
	 * Add a Control of type BooleanControl to this Composite
	 * @param c The BooleanControl to add
	 */
	public void addBooleanControl(BooleanControl c){
		BooleanControlComposite ccomp = new BooleanControlComposite(c, getComposite());
		controls.add(ccomp);
	}
	/**
	 * Add a Control of type EnumControl to this Composite
	 * @param c The EnumControl to add
	 */
	public void addEnumControl(EnumControl c){
		EnumControlComposite ccomp = new EnumControlComposite(c, getComposite());
		controls.add(ccomp);
	}
	/**
	 * Add a Control of type FloatControl to this Composite
	 * @param c The FloatControl to add
	 */
	public void addFloatControl(FloatControl c){
		FloatControlComposite ccomp = new FloatControlComposite(c, getComposite());
		controls.add(ccomp);
	}
	/**
	 * Add any type of Control to this Composite
	 * @param c The Control to add. Must be an instance of either
	 * CompoundControl, BooleanControl, EnumControl or FloatControl.
	 * @throws IllegalArgumentException If the given Control isn´t an instance
	 * of the above mentioned Controls.
	 */
	public void addControl(Control c){
		if(c instanceof CompoundControl) addCompoundControl((CompoundControl)c);
		else if(c instanceof BooleanControl) addBooleanControl((BooleanControl)c);
		else if(c instanceof EnumControl) addEnumControl((EnumControl)c);
		else if(c instanceof FloatControl) addFloatControl((FloatControl)c);
		else throw new IllegalArgumentException("Not a supported type of Control");
	}
	/**
	 * apply() will be called for all contained ControlContainers
	 * @see com.groovemanager.gui.custom.controls.ControlContainer#apply()
	 */
	public void apply() {
		for (Iterator iter = controls.iterator(); iter.hasNext();) {
			ControlContainer element = (ControlContainer) iter.next();
			element.apply();
		}
	}
	/**
	 * reset() will be called for all contained ControlContainers
	 * @see com.groovemanager.gui.custom.controls.ControlContainer#reset()
	 */
	public void reset() {
		for (Iterator iter = controls.iterator(); iter.hasNext();) {
			ControlContainer element = (ControlContainer) iter.next();
			element.reset();
		}
	}
	/**
	 * setAutoApply() will be called for all contained ControlContainers
	 * @see com.groovemanager.gui.custom.controls.ControlContainer#setAutoApply(boolean)
	 */
	public void setAutoApply(boolean auto) {
		for (Iterator iter = controls.iterator(); iter.hasNext();) {
			ControlContainer element = (ControlContainer) iter.next();
			element.setAutoApply(auto);
		}
	}
	/**
	 * @see com.groovemanager.gui.custom.CustomComposite#createComposite(org.eclipse.swt.widgets.Composite, int)
	 */
	protected Composite createComposite(Composite parent, int style) {
		Composite comp = new Composite(parent, style);
		comp.setLayout(new GridLayout(1, true));
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