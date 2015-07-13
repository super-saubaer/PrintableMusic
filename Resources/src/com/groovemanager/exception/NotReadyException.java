/*
 * Created on 14.05.2004
 *
 */
package com.groovemanager.exception;

/**
 * This Exception will be thrown if any kind of a provider is asked to
 * provide data and he is because of some reason not able to.
 * @author Manu Robledo
 *
 */
public class NotReadyException extends Exception{
	/**
	 * Constructs a new NotReadyException.
	 *
	 */
	public NotReadyException(){
	}
	/**
	 * Constructs a new NotReadyException with the given message.
	 * @param message The message telling, why the provider is not ready
	 */
	public NotReadyException(String message){
		super(message);
	}
	/**
	 * Constructs a new NotReadyException with the given message and the
	 * given Throwable that prevents the provider from being ready.
	 * @param message The message telling, why the provider is not ready
	 * @param cause The Throwable preventing the provider from being ready
	 */
	public NotReadyException(String message, Throwable cause){
		super(message, cause);
	}
	/**
	 * Constructs a new NotReadyException with the given Throwable that
	 * prevents the provider from being ready.
	 * @param cause The Throwable preventing the provider from being ready
	 */
	public NotReadyException(Throwable cause){
		super(cause);
	}
}