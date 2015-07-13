/*
 * Created on 09.06.2004
 *
 */
package com.groovemanager.gui.custom.controls;

import javax.sound.sampled.FloatControl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;

import com.groovemanager.gui.custom.CustomComposite;

/**
 * This class can be used to make a FloatControl editable for the user
 * @author Manu Robledo
 *
 */
public class FloatControlComposite extends CustomComposite implements ControlContainer{
	/**
	 * The Label containing the Control´s name
	 */
	private Label controlName;
	/**
	 * The Label containing the control´s unit
	 */
	private Label controlUnits;
	/**
	 * The FloatControl to edit
	 */
	private FloatControl control;
	/**
	 * The slider for value changes
	 */
	private Slider slider;
	/**
	 * The precision of the Control
	 */
	private float prec;
	/**
	 * Text field displaying the current value
	 */
	private Text value;
	/**
	 * Indicates whether auto-apply is turned on or off
	 */
	private boolean auto;
	/**
	 * Construct a new FloatControlComposite
	 * @param control The FloatControl to edit
	 * @param parent The parent Composite
	 * @param style Combination of SWT.* style constants
	 */
	public FloatControlComposite(FloatControl control, Composite parent, int style) {
		super(parent, style);
		this.control = control;
		Composite comp = getComposite();
		comp.setLayout(new GridLayout(2, false));
		
		new Label(comp, SWT.NONE).setText(control.getType().toString());
		Composite sliderComp = new Composite(comp, SWT.NONE);
		GridLayout gl = new GridLayout(3, false);
		gl.marginHeight = gl.marginWidth = gl.horizontalSpacing = 0;
		sliderComp.setLayout(gl);
		prec = control.getPrecision();

		slider = new Slider(sliderComp, SWT.HORIZONTAL);
		slider.setMinimum((int)(control.getMinimum() / prec));
		slider.setMaximum((int)(control.getMaximum() / prec));
		slider.setIncrement(1);
		int sel = (int)Math.round(control.getValue() / control.getMaximum() * (slider.getMaximum() - slider.getThumb()));
		if(slider.getSelection() != sel) slider.setSelection(sel);
		value = new Text(sliderComp, SWT.READ_ONLY);
		value.setText("" + control.getValue());
		new Label(sliderComp, SWT.NONE).setText(control.getUnits());
		slider.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if(auto) apply();
				int sel = slider.getSelection();
				int max = slider.getMaximum();
				int thumb = slider.getThumb();
				double s = sel * FloatControlComposite.this.control.getMaximum() / (double)(max - thumb);
				value.setText("" + s);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
	/**
	 * Construct a new FloatControlComposite using SWT.NONE style
	 * @param control The parent Composite
	 * @param parent The parent Composite
	 */
	public FloatControlComposite(FloatControl control, Composite parent){
		this(control, parent, SWT.NONE);
	}
	
	/**
	 * @see com.groovemanager.gui.custom.controls.ControlContainer#apply()
	 */
	public void apply() {
		int sel = slider.getSelection();
		int max = slider.getMaximum();
		int thumb = slider.getThumb();
		double s = sel * control.getMaximum() / (double)(max - thumb);

		control.setValue((float)s);
	}
	/**
	 * @see com.groovemanager.gui.custom.controls.ControlContainer#reset()
	 */
	public void reset() {
		int sel = (int)Math.round(control.getValue() / control.getMaximum() * (slider.getMaximum() - slider.getThumb()));
		if(slider.getSelection() != sel) slider.setSelection(sel);
		value.setText("" + control.getValue());
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