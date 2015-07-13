/**
 * @author Manu
 * 
 */
package com.groovemanager.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public abstract class FileManager {
	/**
	 * The Root-Path of the Application큦 Ressources
	 */
	public final String ROOT_PATH = tellRootPath();
	/**
	 * The Path, where Configuration Files Should be stored
	 */
	public final String CONFIG_PATH = tellConfigPath();
	/**
	 * The Path, where Temporary Files Should be stored
	 */
	public final String TEMP_PATH = tellTempPath();
	/**
	 * The Path, where Extensions for this Application should be stored
	 */
	public final String EXT_PATH = tellExtPath();
	/**
	 * The default FileManager instance
	 */
	public final static FileManager DEFAULT_FILE_MANAGER = createDefault();
	
	/**
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "FileManager{\n" +
		"\tRoot: " + ROOT_PATH + "\n" +
		"\tConfig: " + CONFIG_PATH + "\n" +
		"\tTemp: " + TEMP_PATH + "\n" +
		"\tExt: " + EXT_PATH + "\n" +
		"}";
	}
	
	/**
	 * Constructs a new FileManager instance, tries to create the
	 * different paths if they don't exist and adds the EXT_PATH
	 * to the java.ext.dirs and java.library.path system properties.
	 *
	 */
	protected FileManager(){
		setErrorHandler();
		System.setProperty("java.library.path", System.getProperty("java.library.path") + File.pathSeparator + new File(".").getAbsolutePath());
		if(EXT_PATH != null){
			System.setProperty("java.ext.dirs", System.getProperty("java.ext.dirs") + File.pathSeparator + EXT_PATH);
			System.setProperty("java.library.path", System.getProperty("java.library.path") + File.pathSeparator + EXT_PATH);
		}
	}
	
	/**
	 * Create the default FileManager instance
	 * @return The default FileManager instance
	 */
	private static FileManager createDefault(){
		return new FileManager(){
			protected String tellRootPath(){
				File f = new File("root");
				return f.getAbsolutePath();
			}
			protected String tellExtPath(){
				File f = new File("ext");
				return f.getAbsolutePath();
			}
			protected String tellTempPath(){
				return System.getProperty("java.io.tmpdir");
			}
			protected String tellConfigPath(){
				return System.getProperty("user.home");
			}
			protected void setErrorHandler() {
				File f = new File(getRootPath("error.log"));
				if(!f.exists())
					try {
						f.createNewFile();
						System.setErr(new PrintStream(new FileOutputStream(f)));
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		};
	}
	
	/**
	 * Tell the Root path of an application under which the
	 * ressource files lie. In the default FileManager this
	 * is the directory named "root" unter the current 
	 * working directory
	 * @return The Root directory큦 absolute path
	 */
	protected abstract String tellRootPath();
	/**
	 * Tell the Extension path of an application under which
	 * the extension files lie. This path will be added to the
	 * java.ext.dirs System property so that Java extensions
	 * can be placed there too. In the default FileManager this
	 * is the directory named "ext" unter the current 
	 * working directory
	 * @return The extension directory큦 absolute path. May
	 * also be <code>null</code> to indicate that this
	 * FileManager doesn't support an extension dir. 
	 */
	protected abstract String tellExtPath();
	/**
	 * Tell the Temporary path under which temp files should be
	 * stored. In the default FileManager this is the directory
	 * given in the java.io.tmpdir System property.
	 * @return The temp directory큦 absolute path
	 */
	protected abstract String tellTempPath();
	/**
	 * Tell the Config path of an application under which the
	 * configuration files should be stored. In the default
	 * FileManager this is the directory given in the
	 * user.home System property.
	 * @return The config directory큦 absolute path
	 */
	protected abstract String tellConfigPath();
	
	
	/**
	 * Get an absolute path from a given String containing the
	 * relative path to the ROOT directory.
	 * @param path The relative path to the ROOT directory.
	 * 	The Unix Slash "/" should be used as path separator
	 * 	(will be replaced with the system dependend separator.
	 * @return The absolute path of the requested File
	 */
	public String getRootPath(String path){
		return convertPath(ROOT_PATH + "/" + path);
	}
	
	/**
	 * Get an absolute path from a given String containing the
	 * relative path to the CONFIG directory.
	 * @param path The relative path to the CONFIG directory.
	 * 	The Unix Slash "/" should be used as path separator
	 * 	(will be replaced with the system dependend separator.
	 * @return A <code>File</code>-Instance for the wanted file.
	 */
	public String getConfigPath(String path){
		return convertPath(CONFIG_PATH + "/" + path);
	}
	
	/**
	 * Converts a given path with Unix slashes "/" to the system
	 * dependend path 
	 * @param path The path using Unix slashes "/"
	 * @return The path using the system dependend file separator
	 */
	public static String convertPath(String path){
		return path.replace('/', File.separatorChar);
	}
	
	/**
	 * List all directories in a directory relative to the ROOT directory
	 * @param directory relative path to the directory
	 * 	The Unix Slash "/" should be used as path separator
	 * 	(will be replaced with the system dependend separator.
	 * @return An array with the names of the found directories
	 */
	public String[] listDirsRoot(String directory){
		File f = new File(getRootPath(directory));
		if(!f.isDirectory()) throw new IllegalArgumentException(f.getAbsolutePath() + " is not a directory.");
		return f.list();
	}
	
	/**
	 * Extracts the extension (e.g. "txt") from a file
	 * @param f The file
	 * @return The extension without the "."
	 */
	public static String getExtension(File f){
		String[] temp = f.getName().split("\\.");
		return temp[temp.length - 1];
	}
	
	/**
	 * Returns a File object with the name of the source file
	 * with an added additional extension
	 * @param source The source File
	 * @param extension Extension without "." that should be
	 * added to the source file큦 name 
	 * @return The File object with the new name.
	 */
	public static File getParallelFile(File source, String extension){
		return new File(source +  "." + extension);
	}
	
	/**
	 * Maps a whole file into memory in Read-Only mode
	 * @param f The file to be mapped
	 * @return The MappedByteBuffer
	 * @throws IOException If an I/O-Error occurs
	 */
	public static MappedByteBuffer mapByteContent(File f) throws IOException{
		return mapByteContent(f, 0, f.length());
	}
	
	/**
	 * Maps part of a file into memory in Read-Only mode
	 * @param f The file to be mapped
	 * @param position Offset in bytes from the beginning of the file
	 * @param length Length in bytes of the region to be mapped
	 * @return The MappedByteBuffer
	 * @throws IOException If an I/O-Error occurs
	 */
	public static MappedByteBuffer mapByteContent(File f, long position, long length) throws IOException{
		return mapByteContent(f, position, length, FileChannel.MapMode.READ_ONLY);
	}
	
	/**
	 * Maps a whole file into memory in the given MapMode
	 * @param f The file to be mapped
	 * @param mapMode The MapMode in which the file should be mapped
	 * @return The MappedByteBuffer
	 * @throws IOException If an I/O-Error occurs
	 */
	public static MappedByteBuffer mapByteContent(File f, FileChannel.MapMode mapMode) throws IOException{
		return mapByteContent(f, 0, f.length(), mapMode);
	}
	
	/**
	 * Maps part of a file into memory in the given MapMode
	 * @param f The file to be mapped
	 * @param position Offset in bytes from the beginning of the file
	 * @param length Length in bytes of the region to be mapped
	 * @param mapMode The MapMode in which the file should be mapped
	 * @return The MappedByteBuffer
	 * @throws IOException If an I/O-Error occurs
	 */
	public static MappedByteBuffer mapByteContent(File f, long position, long length, FileChannel.MapMode mapMode) throws IOException{
		FileInputStream in = new FileInputStream(f);
		return in.getChannel().map(mapMode, position, length);
	}
	
	/**
	 * Get the default File Manager.
	 * @return The default File Manager. In fact it just returns 
	 * DEFAULT_FILE_MANAGER
	 */
	public static FileManager getDefault(){
		return DEFAULT_FILE_MANAGER;
	}
	
	/**
	 * Get the name of a file without its extension. For the file
	 * 'Test.txt' this method will return 'Test'.
	 * @param f The file
	 * @return The name of the file without any extension
	 */
	public static String getNameWithoutExtension(File f){
		return f.getName().split("\\.")[0];
	}
	/**
	 * Set the default error handler
	 * This method will be called from within the Constructor
	 * and can be overridden by subclasses to set another error  
	 *
	 */
	protected abstract void setErrorHandler();
}