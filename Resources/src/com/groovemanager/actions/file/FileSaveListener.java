/*
 * Created on 05.04.2004
 *
 */
package com.groovemanager.actions.file;

import java.io.File;

/**
 * @author Manu
 *
 */
public interface FileSaveListener {
	public void fileSaved(File source, File as);
}
