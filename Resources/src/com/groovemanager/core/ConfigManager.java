package com.groovemanager.core;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.PreferenceStore;

import com.groovemanager.pref.AudioPrefPage;
import com.groovemanager.pref.MIDIPrefPage;

/**
 * This class is used to maintain configuration settings.
 * A default implementation can always be accessed through
 * <code>ConfigManager.getDefault()</code>, but ConfigManager can also
 * be subclassed to adapt its behaviour.
 * @author Manu Robledo
 * 
 */
public class ConfigManager {
	/**
	 * The PreferenceStore used by this ConfigManager
	 */
	protected PreferenceStore prefStore;
	/**
	 * The default filename of the configuration file
	 */
	public final static String DEFAULT_FILENAME = ".main.gmcfg";
	/**
	 * The default implementation of ConfigManager
	 */
	public static final ConfigManager DEFAULT_CONFIG_MANAGER = createDefault();
	
	/**
	 * Constructs a new ConfigManager which stores its settings in
	 * the given file
	 * @param confFile The config File
	 * @throws IOException If an I/O-Error Occurs
	 */
	public ConfigManager(File confFile) throws IOException{
		// Does the file exist?
		if(!confFile.exists()){
			if (Log.active)
				Log.log("Config File " + confFile + " doesn't exist. Trying to create it...", Log.TYPE_DEBUG);
			if(confFile.createNewFile()){
				if (Log.active)
					Log.log("...Success.", Log.TYPE_DEBUG);;
			}
			else{
				if (Log.active)
					Log.log("... no Success, but also no Exception... Hm... Maybe it does already exist.", Log.TYPE_DEBUG);;
			}
		}
		
		// Init the Preference Store
		if(Log.active) Log.log("Loading Config-File: " + confFile + "...", Log.TYPE_DEBUG);
		prefStore = new PreferenceStore(confFile.getAbsolutePath());
		prefStore.setDefault("audio_play_buffer", 16384);
		prefStore.setDefault("audio_player_priority", Thread.NORM_PRIORITY);
		prefStore.load();
		if(Log.active) Log.log("...Success.", Log.TYPE_DEBUG);
	}
	/**
	 * Create the default ConfigManager instance
	 * @return the default ConfigManager instance
	 */
	private static ConfigManager createDefault(){
		try {
			return new ConfigManager(new File(FileManager.getDefault().getConfigPath(DEFAULT_FILENAME)));
		} catch (IOException e) {
			e.printStackTrace();
			if (Log.active)
				Log.log("Could not load default config file, " + e.getMessage(), Log.TYPE_ERROR);
			return null;
		}
	}
	
	/**
	 * Get a preference Manager, which contains the Preference Pages
	 * according to the specified Keys and can be used for PreferenceDialogs
	 * @param pageKeys The keys of the pages which the PreferenceManager
	 * should contain
	 * @return A new PreferenceManager containing the pages specified
	 * by the keys
	 */
	public PreferenceManager getPrefManager(String[] pageKeys){
		return createPrefManager(pageKeys);
	}
	/**
	 * Check, if the given page name is contained inside the given Array
	 * of page names
	 * @param page The page name to be chekced for
	 * @param pages An Array of valid page names
	 * @return true, if the page name was contained inside the Array,
	 * false otherwise
	 */
	protected static boolean checkPage(String page, String[] pages){
		if(pages == null) return true;
		for (int i = 0; i < pages.length; i++) {
			if(page.equals(pages[i])) return true;
		}
		return false;
	}
	/**
	 * Create a preference Manager, which contains the Preference Pages
	 * according to the specified Keys and can be used for PreferenceDialogs
	 * @param pages The keys of the pages which the PreferenceManager
	 * should contain
	 * @return A new PreferenceManager containing the pages specified
	 * by the keys
	 */
	protected PreferenceManager createPrefManager(String[] pages){
		PreferenceManager manager = new PreferenceManager('/');

		if(checkPage("audio", pages)){
			// Audio-Settings Dialog
			PreferencePage audioPage = new AudioPrefPage();
			PreferenceNode audioNode = new PreferenceNode("audio", audioPage);
			manager.addToRoot(audioNode);
		}
		
		if(checkPage("midi", pages)){
			// Midi-Settings Dialog
			PreferencePage midiPage = new MIDIPrefPage();
			PreferenceNode midiNode = new PreferenceNode("midi", midiPage);
			manager.addToRoot(midiNode);
		}
		
		return manager;
	}

	/**
	 * Get the Preference Store connected to this ConfigManager instance
	 * @return The PreferenceStore
	 */
	public PreferenceStore getPrefStore(){
		return prefStore;
	}
	
	/**
	 * Get the default Config Manager.
	 * @return The default Config Manager. In fact it just returns 
	 * DEFAULT_CONFIG_MANAGER
	 */
	public static ConfigManager getDefault(){
		return DEFAULT_CONFIG_MANAGER;
	}
}