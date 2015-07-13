package com.groovemanager.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class is used for logging. It allows time measurement and different
 * types of logging destinations
 * @author Manu Robledo
 *
 */
public class Log {
	/**
	 * Zero-length Class-Array to use for Reflection calls
	 */
	private final static Class[] NO_PARAMETERS = new Class[0];
	/**
	 * The method to invoke when using <code>PROFILE_MODE_NANO</code>. For
	 * compatibility to 1.4 this has to be done via Reflection
	 */
	private static Method nanoMethod;
	/**
	 * Possible Mode values for logging output
	 */
	public static final int MODE_OFF = 1000, MODE_SYSOUT = 1001;
	/**
	 * Possible Mode values for the profile time value
	 */
	public static final int PROFILE_MODE_OFF = 2000, PROFILE_MODE_NANO = 2001, PROFILE_MODE_MILLI = 2002;
	/**
	 * Mode for logging output
	 */
	public static int outputMode = MODE_SYSOUT;
	/**
	 * Mode for profilying
	 */
	public static int profileMode = PROFILE_MODE_MILLI;
	/**
	 * Possible message types
	 */
	public static final int
		TYPE_MESSAGE = 1,
		TYPE_DEBUG = 2,
		TYPE_WARNING = 4,
		TYPE_ERROR = 8,
		TYPE_PROFILE = 16,
		TYPE_ALL = TYPE_MESSAGE | TYPE_DEBUG | TYPE_WARNING | TYPE_ERROR | TYPE_PROFILE;
	/**
	 * Message types to which should be reacted 
	 */
	public static int types = TYPE_ALL;
	/**
	 * Is logging active?
	 */
	public static boolean active = false;
	/**
	 * Has the check for the nanoMethod already been done?
	 */
	private static boolean nanoChecked = false;
	/**
	 * Search for the method to invoke when using PROFILE_MODE_NANO
	 */
	private static void nanoCheck(){
		Class c = System.class;
		Method[] methods = c.getMethods();
		for (int i = 0; i < methods.length; i++) {
			if(methods[i].getName().equals("nanoTime") && methods[i].getParameterTypes().length == 0) nanoMethod = methods[i];
		}
		if(nanoMethod == null)
			try {
				nanoMethod = Log.class.getMethod("pseudoNano", NO_PARAMETERS);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		nanoChecked = true;
	}
	
	/**
	 * Log a message of a specific type
	 * @param message The message
	 * @param type Type of the message
	 */
	public static void log(String message, int type){
		if((type & types) > 0) log(message);
	}
	
	/**
	 * Log a message of unspecified type (will always be
	 * logged if logging is active). Specifying a type is
	 * always better.
	 * @param s The message
	 */
	public static void log(String s){
		switch (outputMode) {
			case MODE_SYSOUT :
				System.out.println(getProfile() + s);
				
				break;
				
			default : return;
		}
	}
	
	/**
	 * Get a profile String which tells the current time depending on the
	 * selected profile mode. The returned String should be formatted in a
	 * way that the user can clearly see the seconds
	 * @return A fromatted String representing the time at which this method
	 * was invoked
	 */
	protected static String getProfile(){
		long l = 0;
		switch (profileMode) {
			case PROFILE_MODE_NANO	:
				if(!nanoChecked) nanoCheck();
				if(nanoMethod != null) try {
					l = ((Long)nanoMethod.invoke(null, NO_PARAMETERS)).longValue();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				return l / 1000000000 + "." + l % 1000000000 + ": ";
			case PROFILE_MODE_MILLI :
				l = System.currentTimeMillis();
				return l / 1000 + "." + l % 1000 + ": ";
			default:
				return "";
		}
	}
	/**
	 * Pseudo method as replacement for <code>System.nanoTime()</code> for
	 * compatibility to 1.4
	 * @return The current nanoTime derived from
	 * <code>System.currentTimeMillis()</code>
	 */
	private static long pseudoNano(){
		long l = System.currentTimeMillis();
		l %= 1000000;
		l *= 1000000;
		return l;
	}
}