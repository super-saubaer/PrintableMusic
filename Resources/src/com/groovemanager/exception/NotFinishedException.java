/*
 * Created on 27.05.2004
 *
 */
package com.groovemanager.exception;

/**
 * This Exception Type will be thrown when an operation could not be
 * finished.
 * @author Manu Robledo
 *
 */
public class NotFinishedException extends Exception{
	/**
	 * Constrcuts a new NotFinishedException
	 *
	 */
	public NotFinishedException() {
	}
	/**
	 * Constructs a new NotFinishedException with the given message
	 * @param message The message about hte cause of the Exception
	 */
	public NotFinishedException(String message) {
		super(message);
	}
	/**
	 * Constructs a new NotFinishedException with the given message
	 * and a Throwable which caused the operation to break.
	 * @param message The message about the cause of the Exception
	 * @param cause The Exception which caused the operation to break
	 */
	public NotFinishedException(String message, Throwable cause){
		super(cause);
	}
	/**
	 * Constructs a new NotFinishedException with the given Throwable
	 * that caused the operation to break.
	 * @param cause The Exception which caused the operation to break
	 */
	public NotFinishedException(Throwable cause){
		super(cause);
	}
}
