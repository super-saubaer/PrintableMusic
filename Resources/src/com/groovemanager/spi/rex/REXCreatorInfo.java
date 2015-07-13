/*
 * Created on 23.04.2004
 *
 */
package com.groovemanager.spi.rex;

/**
 * This class is a wrapper class for the native REXCreatorInfo struct
 * @author Manu Robledo
 *
 */
final class REXCreatorInfo extends NativeClass {
	/**
	 * Create a new REXCreatorInfo instance. A new native instance will also
	 * be created.
	 *
	 */
	REXCreatorInfo() {
	}
	/**
	 * Create a new REXCreatorInfo that is connected to an already existing
	 * native instance which is defined by the given pointer.
	 * @param pointer The address of the native REXCreatorInfo instance
	 */
	REXCreatorInfo(long pointer) {
		super(pointer);
	}
	
	protected native long createClass();
	protected native void cleanUp();
	
	/**
	 * The name of the creator of this file
	 * @return The name of the creator of this file
	 */
	native String fName();
	/**
	 * A text containing the copyright information of ths file
	 * @return A text containing the copyright information of ths file
	 */
	native String fCopyright();
	/**
	 * An URL where the creator of this file can be found on the net
	 * @return An URL where the creator of this file can be found on the net
	 */
	native String fURL();
	/**
	 * The email address of the author
	 * @return The email address of the author
	 */
	native String fEmail();
	/**
	 * A free text containig comments, etc.
	 * @return A free text containig comments, etc.
	 */
	native String fFreeText();
}
