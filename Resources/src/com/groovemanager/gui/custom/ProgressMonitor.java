package com.groovemanager.gui.custom;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import com.groovemanager.exception.CancelledByUserException;
import com.groovemanager.exception.NotFinishedException;
import com.groovemanager.thread.ProgressListener;
import com.groovemanager.thread.ProgressThread;

/**
 * This class can be used for showing a status dialog for monitoring a
 * longer running operation (ProgressThread)
 * @author Manu Robledo
 * @see com.groovemanager.thread.ProgressThread
 */
public class ProgressMonitor extends Dialog implements ProgressListener{
	/**
	 * Minimum value for the ProgressBar
	 */
	private int min = 0,
	/**
	 * Maximum value for the ProgressBar 
	 */
	max = 0,
	/**
	 * Selection of the ProgressBar 
	 */
	pos = 0;
	/**
	 * The ProgressBar for displaying the progress
	 */
	private ProgressBar progBar;
	/**
	 * The ProgressBar for displaying the progress
	 */
	private ProgressBar indetProgBar;
	/**
	 * The message displayed inside the dialog
	 */
	private String message = "",
	/**
	 * The dialog title
	 */
	title = "";
	/**
	 * The Label containing the message
	 */
	private Label messageLabel;
	/**
	 * The Composite containing the two different ProgressBars
	 */
	private Composite pbComp;
	/**
	 * The ProgressThread to monitor
	 */
	final private ProgressThread pThread;
	/**
	 * Runnable for thread-safe updating of the ProgressBar
	 */
	private ProgBarChanger pbChanger = new ProgBarChanger();
	/**
	 * Indicates whether the user has pressed Cancel or not 
	 */
	private boolean cancelled = false;
	/**
	 * The current Display
	 */
	private Display d;
	/**
	 * Runnable for thread-safe closing of the dialog
	 */
	private Runnable closer = new Runnable(){
		public void run(){
			close();
		}
	};
	/**
	 * The Layout to use for the two different progress bars
	 */
	private StackLayout stack = new StackLayout();

	/**
	 * Set the dialog´s title
	 * @param t The new title
	 */
	public void setTitle(String t){
		title = t;
		if(getShell() != null) getShell().setText(t);
	}
	/**
	 * Set the dialog´s message
	 * @param m The new message
	 */
	public void setMessage(String m){
		message = m;
		if(messageLabel != null) messageLabel.setText(m);
	}
	/**
	 * Set the ProgressBar´s maximum value
	 * @param i The maximum value
	 */
	public void setMax(final int i){
		max = i;
		pbChanger.go(i, true);
	}
	/**
	 * Set the ProgressBar´s selection value
	 * @param i The new selection value
	 */
	public void setPosition(final int i){
		pos = i;
		pbChanger.go(i);
	}
	/**
	 * Construct a new ProgressMonitor without title or message
	 * @param parent The parent Shell for the dialog
	 * @param t The ProgressThread to monitor
	 */
	public ProgressMonitor(Shell parent, ProgressThread t) {
		super(parent);
		d = parent.getDisplay();
		pThread = t;
		pThread.setMonitor(this);
	}
	/**
	 * Construct a new ProgressMonitor with the given title and message
	 * @param parent The parent Shell for the dialog
	 * @param t The ProgressThread to monitor
	 * @param title The dialog title
	 * @param message The dialog message
	 */
	public ProgressMonitor(Shell parent, ProgressThread t, String title, String message){
		this(parent, t);
		setTitle(title);
		setMessage(message);
	}
	protected void createButtonsForButtonBar(Composite parent){
		createButton(parent, IDialogConstants.CANCEL_ID, "Abort", true);
	}
	protected void cancelPressed(){
		super.cancelPressed();
		pThread.cancelOperation();
		cancelled = true;
	}
	public Control createDialogArea(Composite parent){
		getShell().setText(title);
		
		Composite area = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout();
		GridData gd;
		
		gl.numColumns = 1;
		area.setLayout(gl);
		
		messageLabel = new Label(area, SWT.NONE);
		messageLabel.setText(message);
		
		pbComp = new Composite(area, SWT.NONE);
		pbComp.setLayout(stack);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		pbComp.setLayoutData(gd);
		
		progBar = new ProgressBar(pbComp, SWT.SMOOTH);
		progBar.setMinimum(min);
		progBar.setMaximum(max);
		progBar.setSelection(pos);
		
		indetProgBar = new ProgressBar(pbComp, SWT.SMOOTH | SWT.INDETERMINATE);
		indetProgBar.setSelection(pos);

		return area;
	}
	/**
	 * Start the ProgressThread and open this dialog
	 * @return The ProgressThread´s result or null, if it didn´t finish
	 * @throws NotFinishedException If the ProgressThread didn´t finish
	 */
	public Object start() throws NotFinishedException{
		pThread.start();
		open();
		if(getReturnCode() == CANCEL) throw new CancelledByUserException("Operation cancelled by User.");
		if(!pThread.hasFinished()) throw new NotFinishedException(pThread.getErrorMessage());
		
		return pThread.getResult();
	}
	/**
	 * 
	 * @see com.groovemanager.thread.ProgressListener#progressTotal(int)
	 */
	public void progressTotal(int i) {
		setMax(i);
	}
	/**
	 * 
	 * @see com.groovemanager.thread.ProgressListener#progressElapsed(int)
	 */
	public void progressElapsed(int i) {
		setPosition(i);
	}
	/**
	 * 
	 * @see com.groovemanager.thread.ProgressListener#progressStarted()
	 */
	public void progressStarted() {
	}
	/**
	 * 
	 * @see com.groovemanager.thread.ProgressListener#progressCancelled()
	 */
	public void progressCancelled() {
		final String s = pThread.getErrorMessage();
		if(!s.equals("")){
			d.syncExec(new Runnable(){
				public void run(){
					MessageDialog dialog = new MessageDialog(getShell(), "Error", null, s, SWT.ICON_ERROR, new String[]{"OK"}, 0);
					dialog.open();
				}
			});
		}
		d.syncExec(closer);
	}
	/**
	 * 
	 * @see com.groovemanager.thread.ProgressListener#progressFinished()
	 */
	public void progressFinished() {
		d.syncExec(closer);
	}
	/**
	 * A Runnable implementation for thread-safe updating of the ProgressBar
	 * @author Manu Robledo
	 *
	 */
	private class ProgBarChanger implements Runnable{
		/**
		 * Current selection value
		 */
		private int value;
		/**
		 * Indicates whether the maximum value (true) or the selection (false)
		 * should be changed when calling the run()-Method
		 */
		private boolean setMax;
		/**
		 * Start asynchronous execution of this Runnable
		 * @param value The new value
		 * @param what true, if the given value is the new maximum value, false
		 * if it is the new selection value
		 */
		private void go(int value, boolean what){
			this.value = value;
			setMax = what;
			if(!d.isDisposed()) d.syncExec(this);
		}
		/**
		 * Start asynchronous execution of this Runnable (only selection change)
		 * @param value The new selection value
		 */
		private void go(int value){
			go(value, false);
		}
		/**
		 * Update the ProgressBar
		 * @see java.lang.Runnable#run()
		 */
		public void run(){
			if(progBar != null && !progBar.isDisposed()){
				if(setMax){
					if(value == -1) stack.topControl = indetProgBar;
					else{
						stack.topControl = progBar;
						progBar.setMaximum(value);
					}
					pbComp.layout();
				}
				else{
					progBar.setSelection(value);
					indetProgBar.setSelection(value);
				}
			}
		}
		
	}
}
