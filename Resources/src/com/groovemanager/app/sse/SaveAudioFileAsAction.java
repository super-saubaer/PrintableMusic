/*
 * Created on 30.05.2004
 *
 */
package com.groovemanager.app.sse;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import com.groovemanager.gui.custom.ChooseFormatDialog;
import com.groovemanager.sampled.AudioManager;
import com.groovemanager.sampled.providers.AudioFileOutputStreamProvider;

/**
 * This class can be used for saving audio files. In addition to what a
 * FileSaveAsAction does, this class will also ensure that a dialog for format
 * selection will be shown to the user.
 * @author Manu Robledo
 *
 */
public class SaveAudioFileAsAction extends Action {
	/**
	 * The AudioManager to be used by this action
	 */
	private AudioManager audioManager = AudioManager.getDefault();
	/**
	 * The parent shell to be used for the dialog
	 */
	private Shell shell;
	/**
	 * The audio format to write
	 */
	private AudioFormat format;
	/**
	 * The dialog to use for selection of the audio format
	 */
	private ChooseFormatDialog formatDialog;
	/**
	 * Allow the user to select already existing files
	 */
	private boolean allowOverwrite;
	/**
	 * The source file that should be saved.
	 */
	private File source;
	/**
	 * List of Listeners registered with this action
	 */
	private ArrayList listeners = new ArrayList();
	/**
	 * Get the currently assigned parent shell
	 * @return The shell currently assigned to this action
	 */
	protected Shell getShell(){
		return shell;
	}
	/**
	 * Set the source file that should be saved. If the argument is
	 * <code>null</code>, the action will be disabled. Otherwise it will be
	 * enabled. 
	 * @param f The file to save or <code>null</code>
	 */
	public void setSourceFile(File f){
		source = f;
		setEnabled(source != null);
	}
	/**
	 * Set the default format to be used for saving
	 * @param format The AudioFormat, the format dialog will show first when
	 * opened
	 */
	public void setFormat(AudioFormat format){
		this.format = format;
	}
	/**
	 * Set the shell to be used as parent shell for the dialogs
	 * @param shell The parent shell for the dialogs
	 */
	public void setShell(Shell shell){
		this.shell = shell;
	}
	/**
	 * Create a new SaveAudioFileAsAction
	 * @param s The name of the action
	 * @param allowOverwrite true, if the user should be able to overwrite
	 * existing files, false otherwise
	 */
	public SaveAudioFileAsAction(String s, boolean allowOverwrite) {
		super(s);
		this.allowOverwrite = allowOverwrite;
	}
	/**
	 * Get the source file currently assigned to this action
	 * @return The source file currently assigned to this action
	 */
	public File getSource(){
		return source;
	}
	/**
	 * Set the ChooseFormatDialog to be shown to the user
	 * @param dialog The dialog that will be shown to the user for format
	 * selection
	 */
	public void setFormatDialog(ChooseFormatDialog dialog){
		formatDialog = dialog;
	}
	/**
	 * Perform this action.
	 * First, the format dialog will be opened and the user will be asked to
	 * select a format. After that a file dialog will be shown for user
	 * selection of the target file. If both dialogs have been accepted, all
	 * listeners will be notified.
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		if(format == null) format = formatDialog.getFormat();
		else formatDialog.setFormat(format);
		if(formatDialog.open() != IDialogConstants.OK_ID) return;
		format = formatDialog.getFormat();
		
		FileDialog d = new FileDialog(shell, SWT.SAVE);
		AudioFileOutputStreamProvider[] providers = audioManager.getRegisteredOutputStreamProviders();
		HashSet types = new HashSet();
		
		for (int i = 0; i < providers.length; i++) {
			AudioFileFormat.Type[] supported = providers[i].getSupportedTypes();
			for (int j = 0; j < supported.length; j++) {
				types.add(supported[j]);
			}
		}
		
		String[] extensions = new String[types.size() + 1];
		String[] names = new String[types.size() + 1];
		
		int i = 0;
		for (Iterator iter = types.iterator(); iter.hasNext(); i++) {
			AudioFileFormat.Type t = (AudioFileFormat.Type) iter.next();
			extensions[i] = "*." + t.getExtension();
			names[i] = t.toString() + " (*." + t.getExtension() + ")";
		}
		
		extensions[extensions.length - 1] = "*.*";
		names[names.length - 1] = "All files (*.*)";
		
		d.setFilterExtensions(extensions);
		d.setFilterNames(names);
		
		File f = getSource();
		if(f != null) d.setFilterPath(f.getParentFile().getAbsolutePath());
		if(d.open() != null){
			f = new File(d.getFilterPath() + File.separator + d.getFileName());
			if(f.exists()){
				if(allowOverwrite){
					MessageDialog dialog = new MessageDialog(shell, "File already exists", null, "The file  " + f + " already exists.\nDo you want to overwrite it?", MessageDialog.QUESTION, new String[]{"OK", "Cancel"}, 0);
					if(dialog.open() != 0) return;
				}
				else{
					MessageDialog dialog = new MessageDialog(shell, "File already exists", null, "The File " + f + " already exists.", MessageDialog.ERROR, new String[]{"OK"}, 0);
					dialog.open();
					return;
				}
			}
			
			for (Iterator iter = listeners.iterator(); iter.hasNext();) {
				AudioFileSaveListener listener = (AudioFileSaveListener) iter.next();
				listener.saveAudioFile(getSource(), f, AudioFileFormat.Type.WAVE, format);
			}
		}
	}
	/**
	 * Add an AudioFileSaveListener to this action
	 * @param listener The AudioFileSaveListener to be added
	 */
	public void addAudioFileSaveListener(AudioFileSaveListener listener){
		listeners.add(listener);
	}
	/**
	 * Remove an AudioFileSaveListener from this action
	 * @param listener The AudioFileSaveListener to be removed
	 */
	public void removeAudioFileSaveListener(AudioFileSaveListener listener){
		listeners.remove(listener);
	}
	/**
	 * Set the AudioManager to be used by this action
	 * @param manager The AudioManager to be used
	 */
	public void setAudioManager(AudioManager manager){
		audioManager = manager;
	}
}
