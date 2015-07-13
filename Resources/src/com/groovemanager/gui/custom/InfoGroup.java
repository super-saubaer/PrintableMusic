/*
 * Created on 23.03.2004
 *
 */
package com.groovemanager.gui.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;

import com.groovemanager.core.FileManager;

/**
 * An InfoGroup represents a Composite containing a List of Labels and
 * ProgressBars showing some status information. Multiple InfoGroup instances
 * can be added to a InfoGroupContainer.
 * @author Manu Robledo
 *
 */
public class InfoGroup{
	/**
	 * The Group that contains the status elements
	 */
	private Group group;
	/**
	 * The Composite containing the group and the button for closing this
	 * InfoGroup
	 */
	private Composite comp;
	/**
	 * The Image used for the close button
	 */
	private Image closeImg;
	/**
	 * The Button for closing this InfoGroup
	 */
	private Button close;
	/**
	 * The InfoGroup following this one in an InfoGroupContainer
	 */
	private InfoGroup next = null;
	/**
	 * The InfoGroup before this one in an InfoGroupContainer
	 */
	private InfoGroup last = null;
	/**
	 * Indicates if this InfoGroup is currently opened
	 */
	private boolean open;
	/**
	 * The InfoGroupContainer containing this InfoGroup
	 */
	private InfoGroupContainer container;
	/**
	 * The FileManager used for finding the needed image files
	 */
	private FileManager fileManager;
	/**
	 * This InfoGroup큦 name
	 */
	private String name;
	/**
	 * Get the InfoGroup before this one in the InfoGroupContainer
	 * @return The InfoGroup before this one
	 */
	InfoGroup getLast(){
		return last;
	}
	/**
	 * Get the InfoGroup following this one in the InfoGroupContainer
	 * @return The InfoGroup after this one
	 */
	InfoGroup getNext(){
		return next;
	}
	/**
	 * Constructs a new InfoGroup with the given name that will be displayed
	 * in the given InfoGroupContainer using the default FileManager
	 * @param cont The InfoGroupContainer that should contain this InfoGroup
	 * @param name The name of the InfoGroup
	 */
	public InfoGroup(InfoGroupContainer cont, String name){
		this(cont, name, FileManager.getDefault());
	}
	/**
	 * Constructs a new InfoGroup with the given name that will be displayed
	 * in the given InfoGroupContainer using the given FileManager
	 * @param cont The InfoGroupContainer that should contain this InfoGroup
	 * @param name The name of the InfoGroup
	 * @param fileManager The FileManager to use for locating the needed image
	 * files
	 */
	public InfoGroup(InfoGroupContainer cont, String name, FileManager fileManager){
		this.name = name;
		container = cont;
		this.fileManager = fileManager;
		
		comp = new Composite(cont.getGroupComp(), SWT.NONE);
		FormLayout fl = new FormLayout();
		comp.setLayout(fl);
		FormData fd;
		
		closeImg = new Image(Display.getCurrent(), fileManager.getRootPath("icons/closetrans.gif"));
		closeImg.setBackground(comp.getBackground());
		
		close = new Button(comp, SWT.FLAT);
		close.setImage(closeImg);
		
		fd = new FormData();
		fd.top = new FormAttachment(0,0);
		fd.right = new FormAttachment(100,0);
		fd.width = fd.height = 16;
		
		close.setLayoutData(fd);
		
		group = new Group(comp, SWT.NONE);
		group.setText(name);
		GridLayout gl = new GridLayout(2, false);
		group.setLayout(gl);
		fd = new FormData();
		fd.top = fd.left = new FormAttachment(0,0);
		fd.right = fd.bottom = new FormAttachment(100,0);
		group.setLayoutData(fd);
		
		close.addSelectionListener(new SelectionListener(){

			public void widgetSelected(SelectionEvent e) {
				container.close(InfoGroup.this);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		cont.open(this);
	}
	/**
	 * Get this InfoGroup큦 main Composite
	 * @return This InfoGroup큦 main Composite
	 */
	public Composite getComposite(){
		return comp;
	}
	/**
	 * Add a new row to this InfoGroup with the given description on the left
	 * and a new Label on the right
	 * @param description The text describing the new element
	 * @return The new created Label
	 */
	public Label addLabel(String description){
		new Label(group, SWT.BOLD).setText(description + ":");
		Label l = new Label(group, SWT.RIGHT);
		l.setLayoutData(new GridData(GridData.FILL_BOTH));
		return l;
	}
	/**
	 * Add a new row to this InfoGroup with the given description on the left
	 * and a new read-only Text field on the right
	 * @param description The text describing the new element
	 * @return The new created Text field
	 */
	public Text addText(String description){
		new Label(group, SWT.BOLD).setText(description + ":");
		Text t = new Text(group, SWT.READ_ONLY | SWT.RIGHT);
		t.setLayoutData(new GridData(GridData.FILL_BOTH));
		return t;
	}
	/**
	 * Add a new row to this InfoGroup with the given description on the left
	 * and a new read-only Text field on the right containing the given content
	 * @param description The text describing the new element
	 * @param content The initial content of the Text field
	 * @return The new created Text field
	 */
	public Text addText(String description, String content){
		Text t = addText(description);
		t.setText(content);
		return t;
	}
	/**
	 * Add a new row to this InfoGroup with the given description on the left
	 * and a new Label on the right containing the given content
	 * @param description The text describing the new element
	 * @param content The initial content of the Label
	 * @return The new created Label
	 */
	public Label addLabel(String description, String content){
		Label l = addLabel(description);
		l.setText(content);
		return l;
	}
	/**
	 * Get this InfoGroup큦 name
	 * @param n This InfoGroup큦 name
	 */
	public void setName(String n){
		name = n;
		group.setText(n);
	}
	/**
	 * Add a new row to this InfoGroup with the given description on the left
	 * and a new ProgressBar on the right
	 * @param description The text describing the new element
	 * @return The new created ProgressBar
	 */
	public ProgressBar addProgBar(String description){
		new Label(group, SWT.BOLD).setText(description);
		ProgressBar bar = new ProgressBar(group, SWT.SMOOTH);
		bar.setLayoutData(new GridData(GridData.FILL_BOTH));
		return bar;
	}
	/**
	 * Set the element following this one in the InfoGroupContainer
	 * @param g The Group following this one
	 */
	void setNext(InfoGroup g){
		next = g;
	}
	/**
	 * Set the element before this one in the InfoGroupContainer
	 * @param g The Group before this one
	 */
	void setLast(InfoGroup g){
		last = g;
	}
	/**
	 * Hide this InfoGroup
	 *
	 */
	void close(){
		open = false; 
	}
	/**
	 * Show this InfoGroup
	 *
	 */
	void open(){
		open = true;
	}
	/**
	 * Get this InfoGroup큦 name
	 * @return This InfoGroup큦 name
	 */
	public String getName(){
		return name;
	}
}