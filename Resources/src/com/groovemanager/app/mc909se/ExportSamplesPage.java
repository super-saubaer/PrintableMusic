/*
 * Created on 20.06.2004
 *
 */
package com.groovemanager.app.mc909se;

import java.io.File;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.groovemanager.core.ConfigManager;
import com.groovemanager.core.FileManager;

/**
 * This page is part of the ExportRhythmSetWizard.
 * @author Manu Robledo
 *
 */
public class ExportSamplesPage extends WizardPage {
	/**
	 * Relative path to the Mc909큦 sample folder from its root path
	 */
	final static String SMPL_FOLDER = "ROLAND" + File.separator + "SMPL";
	/**
	 * The Mc909큦 root path
	 */
	String exportPath = "";
	/**
	 * Radio buttons for USB-select
	 */
	Button internal, card,
	/**
	 * Checkbox to select, if slices should be exported until the end
	 */
	slicesTillEnd,
	/**
	 * Checkbox to select, if a MIDI file for the slice sequence should be
	 * generated
	 */
	createSequence,
	/**
	 * Checkbox to select, if the slice sequence should be played via MIDI
	 */
	playSequence;
	/**
	 * Text field for input of the Mc909큦 root path
	 */
	Text directory;
	/**
	 * Thread that continuously checks, if the selected path is a valid one
	 */
	Thread pathChecker;
	/**
	 * Popup for part selection
	 */
	Combo part,
	/**
	 * Popup for bank selection
	 */
	bank,
	/**
	 * Popup for rhythm number selection
	 */
	rhythm;
	/**
	 * Create a new ExportSamplesPage with the given name
	 * @param pageName The page큦 name
	 */
	public ExportSamplesPage(String pageName) {
		super(pageName);
	}
	/**
	 * Create a new ExportSamplesPage with the given name, title and title image
	 * @param pageName The page큦 name
	 * @param title The page큦 title
	 * @param titleImage The page큦 title image
	 */
	public ExportSamplesPage(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}
	public void createControl(Composite parent) {
		setMessage("Establish an USB connection between your computer and your MC-909.\nMake sure, you make the same settings in the USB select dialog of your MC-909 and in this dialog.");
		
		Composite comp = new Composite(parent, SWT.NONE);
		exportPath = ConfigManager.getDefault().getPrefStore().getString("mc909_sample_export");
		comp.setLayout(new FormLayout());
		FormData fd;
		
		// Destination
		Label l = new Label(comp, SWT.NONE);
		l.setText("MC-909 Location:");
		fd = new FormData();
		fd.left = fd.top = new FormAttachment(0, 10);
		fd.width = fd.height = -1;
		l.setLayoutData(fd);
		
		// Directory
		directory = new Text(comp, SWT.BORDER);
		fd = new FormData();
		fd.left = new FormAttachment(l, 0, SWT.LEFT);
		fd.top = new FormAttachment(l, 0, SWT.BOTTOM);
		fd.width = 400;
		fd.height = -1;
		directory.setLayoutData(fd);
		directory.setText(exportPath);
		directory.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}
			public void focusLost(FocusEvent e) {
				String s = directory.getText(); 
				if(!s.endsWith(File.separator)) s+= File.separator;
				File f = new File(s + SMPL_FOLDER);
				if(f.exists() && f.isDirectory()){
					exportPath = s;
					ConfigManager.getDefault().getPrefStore().setValue("mc909_sample_export", exportPath);
					setPageComplete(true);
				}
				directory.setText(exportPath);
			}
		});
		
		// Choose
		Button choose = new Button(comp, SWT.PUSH);
		choose.setText("Choose...");
		fd = new FormData();
		fd.right = new FormAttachment(100, -10);
		fd.top = new FormAttachment(directory, 0, SWT.TOP);
		fd.bottom = new FormAttachment(directory, 0, SWT.BOTTOM);
		fd.left = new FormAttachment(directory, 0, SWT.RIGHT);
		choose.setLayoutData(fd);
		choose.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dirDialog = new DirectoryDialog(getShell(), SWT.SAVE | SWT.SINGLE);
				String path = dirDialog.open();
				if(path != null){
					if(!path.endsWith(File.separator)) path += File.separator;
					File f = new File(path + SMPL_FOLDER);
					if(f.exists() && f.isDirectory()){
						exportPath = path;
						ConfigManager.getDefault().getPrefStore().setValue("mc909_sample_export", exportPath);
						directory.setText(path);
						setPageComplete(true);
					}
				}
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		final Image img = new Image(comp.getDisplay(), FileManager.getDefault().getRootPath("909screens/usbselect.gif"));
		Composite imageComp = new Composite(comp, SWT.NONE);
		imageComp.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				gc.drawImage(img, 0, 0);
			}
		});
		fd = new FormData();
		fd.width = img.getBounds().width;
		fd.height = img.getBounds().height;
		fd.top = new FormAttachment(choose, 10, SWT.BOTTOM);
		fd.right = new FormAttachment(choose, 0, SWT.RIGHT);
		imageComp.setLayoutData(fd);
		
		Group usbSelect = new Group(comp, SWT.NONE);
		usbSelect.setText("USB Select");
		usbSelect.setLayout(new GridLayout(2, true));
		
		internal = new Button(usbSelect, SWT.RADIO);
		internal.setText("Internal");
		
		card = new Button(usbSelect, SWT.RADIO);
		card.setText("Memory Card");
		card.setSelection(true);
		
		fd = new FormData();
		fd.left = new FormAttachment(directory, 0, SWT.LEFT);
		fd.top = new FormAttachment(directory, 10, SWT.BOTTOM);
		fd.right = new FormAttachment(imageComp, -10, SWT.LEFT);
		usbSelect.setLayoutData(fd);

		l = new Label(comp, SWT.NONE);
		l.setText("Create on part");
		fd = new FormData();
		fd.left = new FormAttachment(usbSelect, 0, SWT.LEFT);
		fd.top = new FormAttachment(usbSelect, 15, SWT.BOTTOM);
		l.setLayoutData(fd);
		
		part = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
		for(int i = 1; i <= 16; i++){
			part.add("" + i);
		}
		part.select(0);
		fd = new FormData();
		fd.left = new FormAttachment(l, 5, SWT.RIGHT);
		fd.bottom = new FormAttachment(l, 0, SWT.BOTTOM);
		part.setLayoutData(fd);
		
		l = new Label(comp, SWT.NONE);
		l.setText("Create as Rhythm set");
		fd = new FormData();
		fd.left = new FormAttachment(usbSelect, 0, SWT.LEFT);
		fd.top = new FormAttachment(part, 15, SWT.BOTTOM);
		l.setLayoutData(fd);
		bank = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
		bank.add("User");
		bank.add("Card");
		bank.select(0);
		fd = new FormData();
		fd.left = new FormAttachment(l, 5, SWT.RIGHT);
		fd.bottom = new FormAttachment(l, 0, SWT.BOTTOM);
		bank.setLayoutData(fd);
		
		rhythm = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
		for(int i = 1; i <= 128; i++){
			rhythm.add("" + i);
		}
		rhythm.select(0);
		fd = new FormData();
		fd.left = new FormAttachment(bank, 0, SWT.RIGHT);
		fd.top = new FormAttachment(bank, 0, SWT.TOP);
		rhythm.setLayoutData(fd);
		
		slicesTillEnd = new Button(comp, SWT.CHECK);
		slicesTillEnd.setText("Export single slices until end of file");
		
		fd = new FormData();
		fd.left = new FormAttachment(l, 0, SWT.LEFT);
		fd.top = new FormAttachment(l, 10, SWT.BOTTOM);
		slicesTillEnd.setLayoutData(fd);
		
		createSequence = new Button(comp, SWT.CHECK);
		createSequence.setText("Create sequence as Standard MIDI File");
		
		fd = new FormData();
		fd.left = new FormAttachment(slicesTillEnd, 0, SWT.LEFT);
		fd.top = new FormAttachment(slicesTillEnd, 5, SWT.BOTTOM);
		createSequence.setLayoutData(fd);
		
		playSequence = new Button(comp, SWT.CHECK);
		playSequence.setText("Play sequence for Realtime Rec");
		
		fd = new FormData();
		fd.left = new FormAttachment(createSequence, 0, SWT.LEFT);
		fd.top = new FormAttachment(createSequence, 5, SWT.BOTTOM);
		playSequence.setLayoutData(fd);
		
		setControl(comp);
		File f = new File(exportPath + SMPL_FOLDER);
		setPageComplete(f.exists() && f.isDirectory());
		
		final Runnable r = new Runnable(){
			public void run(){
				String path = directory.getText();
				if(!path.endsWith(File.separator)) path += File.separator;
				File f = new File(path + SMPL_FOLDER);
				if(f.exists() && f.isDirectory()){
					exportPath = path;
					setPageComplete(true);
				}
				else{
					setPageComplete(false);
				}
			}
		};
		final Display d = getShell().getDisplay();
		
		pathChecker = new Thread(){
			public void run() {
				while(true){
					d.asyncExec(r);
					try{
						sleep(1000);
					}
					catch(InterruptedException e){}
				}
			}
		};
		
		directory.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				pathChecker.stop();
			}
		});
		
		pathChecker.start();
	}
}
