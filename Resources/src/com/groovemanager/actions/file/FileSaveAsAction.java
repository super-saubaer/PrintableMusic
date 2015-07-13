/*
 * Created on 05.04.2004
 *
 */
package com.groovemanager.actions.file;

import java.io.File;
import java.util.Iterator;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * This subclass of Action is used to save a file with user interaction.
 * Before being performed, a Shell and a source File have to be assigned
 * to this Action. When performed, an instance of this class will open a
 * FileDialog where the user can select the filename under which this file
 * should be saved. When the file already exists, either a message will be
 * shown that the file cannot be saved under this name or a Dialog will
 * open up asking the user whether he wants to overwrite this file or not.
 * This behaviour depends on the setting of <code>allowOverride</code>.
 * When a valid filename has been selected, it will notify all registered
 * FileSaveListeners that the file should be saved.
 * @author Manu Robledo
 *
 */
public class FileSaveAsAction extends FileSaveAction {
	/**
	 * The source file which should be saved
	 */
	private File source;
	/**
	 * The Shell which will be used as parent Shell for the FileDialog
	 */
	private Shell shell;
	/**
	 * Decides whether the user will be allowed to overwrite existing files
	 */
	private boolean allowOverwrite = true;
	/**
	 * Constructs a new FileSaveAsAction with the given name
	 * @param name This Action´s name
	 * @param allowOverwrite true if the user should be allowed to overwrite
	 * existing files, false otherwise
	 */
	public FileSaveAsAction(String name, boolean allowOverwrite) {
		super(name);
		this.allowOverwrite = allowOverwrite;
	}
	/**
	 * Constructs a new FileSaveAsAction with the given name which allows the
	 * user to ovewrite existing files
	 * @param name This Action´s name
	 */
	public FileSaveAsAction(String name) {
		super(name);
	}
	/**
	 * Set the parent Shell of the FileDialog
	 * @param s The Shell that will be used as parent Shell of the FileDialog
	 */
	public void setShell(Shell s){
		shell = s;
	}
	/**
	 * Set the source file that should be saved. If the argument is
	 * <code>null</code>, the action will be disabled. Otherwise it will be
	 * enabled.
	 * @param file The source file that should be saved or <code>null</code>
	 */
	public void setSourceFile(File file){
		source = file;
		setEnabled(source != null);
	}
	/**
	 * Set if the user is allowed to overwrite existing files or not
	 * @param allow true if the user should be allowed to overwrite existing
	 * files, false otherwise
	 */
	public void setAllowOverwrite(boolean allow){
		allowOverwrite = allow;
	}
	/**
	 * Perform this action.
	 * If no source file or no shell has been assigned to this Action,
	 * nothing will happen.
	 * The FileDialog will be opened. When cancelling this dialog, nothing
	 * will happen. Otherwise ther will be a check, if the file exists. If it
	 * exists, depending on the setting of <code>allowOverwrite</code> the
	 * user will be asked if he wants overwrite it or he will be notified
	 * that he mustn´t overwrite it. After selecting a valid filename and
	 * confirming, all registered listeners will be notified that the
	 * source file should be saved under the selected name.
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run(){
		if(source == null || shell == null) return;
		
		FileDialog d = new FileDialog(shell, SWT.SAVE);
		if(d.open() != null){
			File f = new File(d.getFilterPath() + File.separator + d.getFileName());
			if(f.exists()){
				if(allowOverwrite){
					MessageDialog dialog = new MessageDialog(shell, "File already exists", null, "The file  " + f + " already exists.\nDo you want to overwrite it?", MessageDialog.QUESTION, new String[]{"OK", "Cancel"}, 0);
					if(dialog.open() != MessageDialog.OK) return;
				}
				else{
					MessageDialog dialog = new MessageDialog(shell, "File already exists", null, "The File " + f + " already exists.", MessageDialog.ERROR, new String[]{"OK"}, 0);
					dialog.open();
					return;
				}
			}
			
			for (Iterator iter = listeners.iterator(); iter.hasNext();) {
				FileSaveListener listener = (FileSaveListener) iter.next();
				listener.fileSaved(source, f);
			}
		}
	}
}
