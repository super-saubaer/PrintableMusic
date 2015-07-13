/*
 * Created on 03.04.2004
 *
 */
package com.groovemanager.exception;

/**
 * This Exception indicates that the user has cancelled an operation.
 * This will mostly happen when pressing cancel.
 * @author Manu Robledo
 *
 */
public class CancelledByUserException extends NotFinishedException {
	/**
	 * Constructs a new CancelledByUserException
	 *
	 */
	public CancelledByUserException(){
	}
	/**
	 * Constructs a new CancelledByUserException with the specified
	 * message.
	 * @param message The message
	 */
	public CancelledByUserException(String message){
		super(message);
	}
}
