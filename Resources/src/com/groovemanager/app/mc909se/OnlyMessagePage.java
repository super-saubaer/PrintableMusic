/*
 * Created on 20.06.2004
 *
 */
package com.groovemanager.app.mc909se;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * This class represents a wizard page containing a text and optionally an image
 * @author Manu Robledo
 *
 */
public class OnlyMessagePage extends WizardPage {
	/**
	 * The message text
	 */
	private String messageText = "";
	/**
	 * The Label used to display the text
	 */
	private Label messageLabel;
	/**
	 * The image path
	 */
	private String image = null;
	/**
	 * The composite used for displaying the image
	 */
	private Composite imageComp;
	/**
	 * The image
	 */
	private Image img;
	/**
	 * Create a new OnlyMessagePage
	 * @param pageName The page´s name
	 * @param message The message to display
	 */
	public OnlyMessagePage(String pageName, String message) {
		super(pageName);
		messageText = message;
	}
	/**
	 * Create a new OnlyMessagePage
	 * @param pageName The page´s name
	 * @param title The page title
	 * @param titleImage The title image
	 */
	public OnlyMessagePage(String pageName, String title,
			ImageDescriptor titleImage, String message) {
		super(pageName, title, titleImage);
		messageText = message;
	}
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		
		GridLayout gl = new GridLayout(1, true);
		gl.marginHeight = gl.marginWidth = 10;
		gl.horizontalSpacing = gl.verticalSpacing = 5;
		comp.setLayout(gl);
		
		imageComp = new Composite(comp, SWT.NONE);
		imageComp.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if(img != null){
					e.gc.drawImage(img, 0, 0);
				}
				else{
					e.gc.fillRectangle(imageComp.getBounds());
				}
			}
		});
		
		messageLabel = new Label(comp, SWT.WRAP);
		messageLabel.setText(messageText);
		GridData gd = new GridData();
		gd.widthHint = 500;
		messageLabel.setLayoutData(gd);
		
		setControl(comp);
	}
	/**
	 * Set the message text
	 * @param text The message to set
	 */
	public void setMessageText(String text){
		messageText = text;
		if(messageLabel != null && !messageLabel.isDisposed()) messageLabel.setText(text);
	}
	/**
	 * Set the image to be displayed besides the message text
	 * @param path The path of the image
	 */
	public void setImage(String path){
		image = path;
		if(path != null && imageComp != null && !imageComp.isDisposed()){
			img = new Image(imageComp.getDisplay(), path);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.heightHint = img.getBounds().height;
			imageComp.setLayoutData(gd);
			imageComp.getParent().layout();
		}
		else{
			img = null;
			GridData gd = new GridData();
			gd.widthHint = 0;
			gd.heightHint = 0;
			imageComp.setLayoutData(gd);
		}
	}
}
