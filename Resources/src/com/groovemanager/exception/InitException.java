/*
 * Created on 06.08.2004
 *
 */
package com.groovemanager.exception;

/**
 * This Exception should be thrown, if any kind of initialization fails.
 * @author Manu Robledo
 *
 */
public class InitException extends Exception {
	/**
	 * Create a new InitException
	 */
	public InitException() {
		super();
	}
	/**
	 * Create a new InitException
	 * @param message The message describing the cause
	 */
	public InitException(String message) {
		super(message);
	}
	/**
	 * Create a new InitException
	 * @param message The message describing the cause
	 * @param cause The Throwable that caused this Exception to be thrown
	 */
	public InitException(String message, Throwable cause) {
		super(message, cause);
	}
	/**
	 * Create a new InitException
	 * @param cause The Throwable that caused this Exception to be thrown
	 */
	public InitException(Throwable cause) {
		super(cause);
	}
}
