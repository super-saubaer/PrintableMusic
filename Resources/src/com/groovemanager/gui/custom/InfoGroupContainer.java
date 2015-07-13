/*
 * Created on 23.03.2004
 *
 */
package com.groovemanager.gui.custom;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;

/**
 * An InfoGropContainer is needed for showing InfoGorups. When InfoGroups are
 * closed, they are accessible for re-opening in the bottom of the container
 * as a Button
 * @author Manu Robledo
 *
 */
public class InfoGroupContainer {
	/**
	 * The Composite containing the InfoGroups
	 */
	private Composite groupComp;
	/**
	 * The Composite containing the re-open Buttons
	 */
	private Composite buttonComp;
	/**
	 * The last opened InfoGroup
	 */
	private InfoGroup lastgroup;
	/**
	 * The map containing all re-open Buttons
	 */
	private HashMap buttons = new HashMap();
	/**
	 * The main group surrounding all
	 */
	private Group group;
	/**
	 * Runnable for Thread-indipendent update operations
	 */
	private final static LabelUpdater labelUpdater = new LabelUpdater();
	/**
	 * Runnable for Thread-indipendent update operations
	 */
	private final static TextUpdater textUpdater = new TextUpdater();
	/**
	 * Runnable for Thread-indipendent update operations
	 */
	private final static ProgBarUpdater progBarUpdater = new ProgBarUpdater();
	/**
	 * The Display to use for thread-safe updating
	 */
	private final Display display;
	/**
	 * Update a Label큦 content. This method is thread-safe. So it can be
	 * called from any Thread, not only the one that created the Label.
	 * @param l The Label to update.
	 * @param text The new content of the Label
	 */
	public void update(final Label l, final String text){
		labelUpdater.setLabel(l);
		labelUpdater.setText(text);
		display.syncExec(labelUpdater);
	}
	/**
	 * Update a Text field큦 content. This method is thread-safe. So it can be
	 * called from any Thread, not only the one that created the Text field.
	 * @param t The Text field to update
	 * @param text The new content of the Text field
	 */
	public void update(final Text t, final String text){
		textUpdater.setText(text);
		textUpdater.setTextField(t);
		display.syncExec(textUpdater);
	}
	/**
	 * Update a ProgressBar. This method is thread-safe. So it can be
	 * called from any Thread, not only the one that created the Text field.
	 * @param b The ProgressBar to update
	 * @param selection The new value for the ProgressBar큦 selection
	 */
	public void update(final ProgressBar b, final int selection){
		progBarUpdater.setProgBar(b);
		progBarUpdater.setSelection(selection);
		display.asyncExec(progBarUpdater);
	}
	/**
	 * Update a ProgressBar. This method is thread-safe. So it can be
	 * called from any Thread, not only the one that created the Text field.
	 * @param b The ProgressBar to update
	 * @param min The new value for the ProgressBar큦 minimum
	 * @param max The new value for the ProgressBar큦 maximum
	 * @param selection The new value for the ProgressBar큦 selection
	 */
	public void update(final ProgressBar b, final int min, final int max, final int selection){
		progBarUpdater.setProgBar(b);
		progBarUpdater.setAll(min, max, selection);
		display.syncExec(progBarUpdater);
	}
	/**
	 * Construct a new InfoGroupContainer without headline
	 * @param parent The parent Composite
	 */
	public InfoGroupContainer(Composite parent){
		display = parent.getDisplay();
		
		group = new Group(parent, SWT.NONE);
		group.setLayout(new FormLayout());
		
		groupComp = new Composite(group, SWT.NONE);
		buttonComp = new Composite(group, SWT.NONE);
		FormData fd = new FormData();
		fd.bottom = new FormAttachment(buttonComp, 0, SWT.TOP);
		fd.right = new FormAttachment(100, 0);
		fd.top = fd.left = new FormAttachment(0, 0);
		groupComp.setLayoutData(fd);
		groupComp.setLayout(new FormLayout());
		
		fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.height = 0;
		fd.right = fd.bottom = new FormAttachment(100, 0);
		buttonComp.setLayoutData(fd);
		buttonComp.setLayout(new FillLayout(SWT.VERTICAL));
	}
	/**
	 * Construct a new InfoGroupContainer without headline
	 * @param parent The parent Composite
	 * @param headline The headline of this InfoGroupContainer
	 */
	public InfoGroupContainer(Composite parent, String headline){
		this(parent);
		group.setText(headline);
	}
	/**
	 * Get the main Composite of this InfoGroupContainer
	 * @return This InfoGroupContainer큦 main Composite
	 */
	public Composite getComposite(){
		return group;
	}
	/**
	 * Get the Composite which contains the InfoGroup instances
	 * @return The Composite for creating new InfoGroup instances
	 */
	Composite getGroupComp(){
		return groupComp;
	}
	/**
	 * Create and return a new InfoGroup which is added to this container
	 * @param name The name of the group
	 * @return The new created InfoGroup
	 */
	public InfoGroup newGroup(String name){
		InfoGroup g = new InfoGroup(this, name);
		g.setLast(lastgroup);
		FormData fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(100, 0);
		fd.height = -1;
		if(lastgroup == null){
			fd.top = new FormAttachment(0, 0);
		}
		else{
			lastgroup.setNext(g);
			fd.top = new FormAttachment(lastgroup.getComposite(), 1, SWT.BOTTOM);
		}
		g.getComposite().setLayoutData(fd);
		lastgroup = g;
		return g;
	}
	/**
	 * Close the specified InfoGroup. This method is called from the InfoGroup
	 * instance when it is closed
	 * @param g The InfoGroup being closed
	 */
	void close(final InfoGroup g){
		g.getComposite().setVisible(false);
		g.close();
		if(g.getNext() != null){
			g.getNext().getComposite().setLayoutData(g.getComposite().getLayoutData());
			g.getNext().setLast(g.getLast());
		}
		if(g.getLast() != null){
			g.getLast().setNext(g.getNext());
		}
		if(lastgroup == g) lastgroup = g.getLast();

		g.setLast(null);
		g.setNext(null);
		
		Button b = new Button(buttonComp, SWT.FLAT | SWT.PUSH);
		b.setText("Show " + g.getName());
		b.addSelectionListener(new SelectionListener(){

			public void widgetSelected(SelectionEvent e) {
				open(g);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
			
		});
		buttons.put(g, b);

		FormData fd = (FormData)buttonComp.getLayoutData();
		fd.height = -1;
		
		getComposite().layout();
	}
	/**
	 * Open the specified InfoGroup. This method is called from the InfoGroup
	 * instance when it is opened
	 * @param g The InfoGroup being opened
	 */
	void open(InfoGroup g){
		g.setLast(lastgroup);
		FormData fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(100, 0);
		fd.height = -1;
		if(lastgroup == null){
			fd.top = new FormAttachment(0, 0);
		}
		else{
			lastgroup.setNext(g);
			fd.top = new FormAttachment(lastgroup.getComposite(), 1, SWT.BOTTOM);
		}
		g.getComposite().setLayoutData(fd);
		lastgroup = g;
		
		Button b = (Button)buttons.get(g);
		if(b != null && !b.isDisposed()){
			b.dispose();
			buttons.remove(g);
		}
		
		if(buttons.size() == 0){
			fd = (FormData)buttonComp.getLayoutData();
			fd.height = 0;
		}
		
		g.getComposite().setVisible(true);
		
		getComposite().layout();
	}
	/**
	 * A Runnable implementation used for Thread-safe updating of Labels
	 * @author Manu Robledo
	 *
	 */
	private static class LabelUpdater implements Runnable{
		/**
		 * The Label to update
		 */
		private Label l;
		/**
		 * The new text
		 */
		private String text;
		/**
		 * Set the Label to update
		 * @param l The Label to update
		 */
		void setLabel(Label l){
			this.l = l;
		}
		/**
		 * Set the new text
		 * @param text The new text
		 */
		void setText(String text){
			this.text = text;
		}
		/**
		 * Update the Label큦 content
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			if(text != null && !l.getText().equals(text)) l.setText(text);
		}
	}
	/**
	 * A Runnable implementation used for Thread-safe updating of Text fields
	 * @author Manu Robledo
	 *
	 */
	private static class TextUpdater implements Runnable{
		/**
		 * The Text field to update
		 */
		private Text t;
		/**
		 * The new text
		 */
		private String text;
		/**
		 * Set the Text field to update
		 * @param t The Text field to update
		 */
		void setTextField(Text t){
			this.t = t;
		}
		/**
		 * Set the new text
		 * @param text The new text
		 */
		void setText(String text){
			this.text = text;
		}
		/**
		 * Update the Text field큦 content.
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			if(text != null && !t.getText().equals(text)) t.setText(text);
		}
	}
	/**
	 * A Runnable implementation used for Thread-safe updating of ProgressBars
	 * @author Manu Robledo
	 *
	 */
	private static class ProgBarUpdater implements Runnable{
		/**
		 * The ProgressBar to update
		 */
		private ProgressBar p;
		/**
		 * The new selection or -1 if the selection should not be updated
		 */
		private int selection;
		/**
		 * The new minimum value or -1 if the minimum value should not be
		 * updated
		 */
		private int min;
		/**
		 * The new maximum value or -1 if the maximum value should not be
		 * updated
		 */
		private int max;
		/**
		 * Set the ProgressBar to update
		 * @param p The ProgressBar to update
		 */
		void setProgBar(ProgressBar p){
			this.p = p;
			selection = -1;
			min = -1;
			max = -1;
		}
		/**
		 * Set the new selection
		 * @param sel The new selection value 
		 */
		void setSelection(int sel){
			selection = sel;
		}
		/**
		 * Set new values for minimum, maximum and the new selection
		 * @param min The new minimum value
		 * @param max The new maximum value
		 * @param selection The new selection
		 */
		void setAll(int min, int max, int selection){
			this.min = min;
			this.max = max;
			this.selection = selection;
		}
		/**
		 * Update the ProgressBar
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			if(selection != -1 && p.getSelection() != selection) p.setSelection(selection);
			if(min != -1 && p.getMinimum() != min) p.setMinimum(min);
			if(max != -1 && p.getMaximum() != max) p.setMaximum(max);
		}
	}
}