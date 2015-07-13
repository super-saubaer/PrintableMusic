package com.groovemanager.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Shell;

import com.groovemanager.core.ConfigManager;
/**
 * This subclass of Action is used for showing a PreferenceDialog with the
 * selected predefined PreferencePages. The key names given for the
 * PreferencePages to show must be known by the used ConfigManager.
 * @author Manu Robledo
 *
 */
public class ConfigAction extends Action{
	/**
	 * The parent Shell
	 */
	protected Shell parent;
	/**
	 * The key names of the selected pages that should be displayed
	 */
	protected String[] pages = new String[]{};
	/**
	 * The ConfigManager to be used
	 */
	protected ConfigManager confManager;
	
	/**
	 * Constructs a new ConfigAction with the given name using the
	 * default ConfigManager and without any PreferencePages. Pages
	 * can be added later with addPages(String[])
	 * @param name The Action큦 name
	 */
	public ConfigAction(String name){
		super(name);
		confManager = ConfigManager.DEFAULT_CONFIG_MANAGER;
	}
	
	/**
	 * Constructs a new ConfigAction with the given name using the
	 * default ConfigManager with the given PreferencePages
	 * @param name The Action큦 name
	 * @param pages The key names of the pages to be shown in the
	 * PreferenceDialog
	 */
	public ConfigAction(String name, String[] pages){
		this(name);
		this.pages = pages;
	}
	
	/**
	 * Constructs a new ConfigAction with the given name using the
	 * given ConfigManager with the given PreferencePages
	 * @param name The Action큦 name
	 * @param pages The key names of the pages to be shown in the
	 * PreferenceDialog
	 * @param manager The ConfigManager to be used
	 */
	public ConfigAction(String name, String[] pages, ConfigManager manager){
		this(name, pages);
		confManager = manager;
	}
	
	/**
	 * Constructs a new ConfigAction with the given name using the
	 * given ConfigManager and without any PreferencePages. Pages
	 * can be added later with addPages(String[])
	 * @param name The Action큦 name
	 * @param manager The ConfigManager to be used
	 */
	public ConfigAction(String name, ConfigManager manager){
		this(name);
		confManager = manager;
	}

	/**
	 * Set the ConfigManager to be used by the dialog
	 * @param manager The new ConfigManager to be used
	 */
	public void setConfigManager(ConfigManager manager){
		confManager = manager;
	}
	
	/**
	 * Set the PreferencePages to display
	 * @param pages The key names of the predefined PreferencePages
	 */
	public void setPages(String[] pages){
		this.pages = pages;
	}
	
	/**
	 * Set the parent Shell under which the dialog should be opened
	 * @param shell The parent Shell
	 */
	public void setShell(Shell shell){
		parent = shell;
	}
	
	/**
	 * Open the dialog
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run(){
		PreferenceDialog dialog = new PreferenceDialog(parent, confManager.getPrefManager(pages));
		dialog.setPreferenceStore(confManager.getPrefStore());
		dialog.open();
	}
}