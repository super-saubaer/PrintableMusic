/*
 * Created on 22.03.2004
 *
 */
package com.groovemanager.actions.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * This subclass of Action is used to open a file.
 * Before being performed, a Shell has to be assigned to this Action.
 * When performed, an instance of this class will open a FileDialog where
 * the user can select the file(s) to be opened. After closing the dialog,
 * it will notify all registeres FileOpenListeners that the file(s)
 * should be opened.
 * @author Manu Robledo
 *
 */
public class FileOpenAction extends Action {
	/**
	 * The path into which the FileDialog should point when opened
	 */
	private String path;
	/**
	 * The Shell that will be used as parent Shell of the FileDialog
	 */
	private Shell shell;
	/**
	 * List of FileOpenListeners registered to this Action
	 */
	private ArrayList listeners = new ArrayList();
	/**
	 * Should the user be able to open multiple files at once?
	 */
	private boolean multiple = false;
	/**
	 * Constructs a new FileOpenAction with the given name
	 * @param name This Action´s name
	 * @param multi true if the user should be able to select multiple
	 * files at once in the FileDialog, false otherwise
	 */
	public FileOpenAction(String name, boolean multi){
		super(name);
		multiple = multi;
	}
	/**
	 * Constructs a new FileOpenAction with the given name which lets the
	 * user select only one file at a time.
	 * @param name This Action´s name
	 */
	public FileOpenAction(String name){
		super(name);
	}
	/**
	 * Set the parent Shell of the FileDialog
	 * @param s The Shell which will be used as parent Shell for the FileDialog
	 */
	public void setShell(Shell s){
		shell = s;
	}
	/**
	 * Register a FileOpenListener with this Action
	 * @param l The FileOpenListener to register
	 */
	public void addFileOpenListener(FileOpenListener l){
		listeners.add(l);
	}
	/**
	 * Remove a registered FileOpenListener from this Action
	 * @param l The FileOpenListener to remove
	 */
	public void removeFileOpenListener(FileOpenListener l){
		listeners.remove(l);
	}
	/**
	 * Set the filter path of the FileDialog
	 * @param path The path to which the FileDialog should point when opened.
	 * May be null. In this case, a default path will be used.
	 */
	public void setFilterPath(String path){
		this.path = path;
	}
	/**
	 * Perform this Action.
	 * The FileDialog will be opened. If the user closes it without selecting
	 * a file (e.g. Cancel) nothing will happen. Else all registered
	 * FileOpenListeners will be notified about all selected files.
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run(){
		if(shell == null) return;
		int style = SWT.OPEN;
		if(multiple) style |= SWT.MULTI;
		FileDialog d = new FileDialog(shell, style);
		if(path != null) d.setFilterPath(path);
		if(d.open() != null){
			String[] files = d.getFileNames();
			path = d.getFilterPath();
			for (int i = 0; i < files.length; i++) {
				String filename = path + File.separator + files[i];
				File f = new File(filename);
				for (Iterator iter = listeners.iterator(); iter.hasNext();) {
					FileOpenListener listener = (FileOpenListener) iter.next();
					listener.fileOpened(f);
				}
			}
		}
	}
}