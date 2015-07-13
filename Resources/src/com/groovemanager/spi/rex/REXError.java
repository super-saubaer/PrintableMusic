/*
 * Created on 25.04.2004
 *
 */
package com.groovemanager.spi.rex;

/**
 * This Exception class provides a collection of all the REXError error codes
 * that can be returned by calls to the REX API. An instance of this class can
 * be thrown, if the returned error code was not expected.
 * @author Manu Robledo
 *
 */
final class REXError extends Exception{
	/**
	 * The REXError constant that caused this Exception
	 */
	private int code;
	/**
	 * Private constructor. Can only be called from throwREXError()
	 * @param code The REXError constant that occured
	 */
	private REXError(int code){
		super(getMessage(code));
		this.code = code;
	}
	
	/**
	 * Get the REXError constant that caused this Exception
	 * @return The REXError constant that caused this Exception
	 */
	int getCode(){
		return code;
	}
	
	/**
	 * Get an error message out of a REXError constant
	 * @param code The REXError constant
	 * @return An error message
	 */
	static String getMessage(int code){
		if(code == NoError) return "No Error.";
		else if(code == DLLNotFound) return "The REX dll could not be found.";
		else if(code == DLLTooOld) return "The dll found on this system is too old.";
		else if(code == APITooOld) return "The dll found in this system is too new.";
		else if(code == OSVersionNotSupported) return "OS version not supported.";
		else if(code == NotEnoughMemoryForDLL) return "Not enough memory to load the dll.";
		else if(code == UnableToLoadDLL) return "Unable to load dll.";
		else if(code == DLLAlreadyLoaded) return "DLL already loaded.";
		else if(code == OutOfMemory) return "Out of Memory.";
		else if(code == FileCorrupt) return "File corrupt";
		else if(code == REX2FileTooNew) return "REX2 File too new.";
		else if(code == OperationAbortedByUser) return "Operationcancelled by user.";
		else if(code == FileHasZeroLoopLength) return "File has zero loop length.";
		else if(code == DLLNotLoaded) return "DLL not loaded.";
		else if(code == InvalidArgument) return "Invalid argument";
		else if(code == InvalidHandle) return "Invalid Handle";
		else if(code == InvalidSize) return "Invalid size";
		else if(code == NoCreatorInfoAvailable) return "No creator info available";
		else if(code == InvalidSlice) return "Invalid slice";
		else if(code == InvalidSampleRate) return "Invalid sample rate.";
		else if(code == InvalidTempo) return "Invalid tempo.";
		else if(code == IsBeingPreviewed) return "File is currently being previewed.";
		else if(code == NotBeingPreviewed) return "File is not being previewed.";
		else if(code == BufferTooSmall) return "Buffer too small.";
		else return "Unknown Error.";
	}
	
	/**
	 * Verify of the result from a call to an element of the REX API succeded
	 * or returned an error code. If an error is detected, a REXError will be
	 * thrown 
	 * @param code The REXError constant returned by the native function
	 * @throws REXError If the constant is not the same as NoError
	 */
	static void throwREXError(int code) throws REXError{
		if(code != NoError) throw new REXError(code);
	}
	
	/**
	 * REXError constants
	 */
	final static int NoError = NoError(),
		DLLNotFound = DLLNotFound(),
		DLLTooOld = DLLTooOld(),
		APITooOld = APITooOld(),
		OSVersionNotSupported = OSVersionNotSupported(),
		NotEnoughMemoryForDLL = NotEnoughMemoryForDLL(),
		UnableToLoadDLL = UnableToLoadDLL(),
		DLLAlreadyLoaded = DLLAlreadyLoaded(),
		OutOfMemory = OutOfMemory(),
		FileCorrupt = FileCorrupt(),
		REX2FileTooNew = REX2FileTooNew(),
		OperationAbortedByUser = OperationAbortedByUser(),
		FileHasZeroLoopLength = FileHasZeroLoopLength(),
		DLLNotLoaded = DLLNotLoaded(),
		InvalidArgument = InvalidArgument(),
		InvalidHandle = InvalidHandle(),
		InvalidSize = InvalidSize(),
		NoCreatorInfoAvailable = NoCreatorInfoAvailable(),
		InvalidSlice = InvalidSlice(),
		InvalidSampleRate = InvalidSampleRate(),
		InvalidTempo = InvalidTempo(),
		IsBeingPreviewed = IsBeingPreviewed(),
		NotBeingPreviewed = NotBeingPreviewed(),
		BufferTooSmall = BufferTooSmall();
	
	
	private static native int NoError();
	private static native int DLLNotFound();
	private static native int DLLTooOld();
	private static native int APITooOld();
	private static native int OSVersionNotSupported();
	private static native int NotEnoughMemoryForDLL();
	private static native int UnableToLoadDLL();
	private static native int DLLAlreadyLoaded();
	private static native int OutOfMemory();
	private static native int FileCorrupt();
	private static native int REX2FileTooNew();
	private static native int OperationAbortedByUser();
	private static native int FileHasZeroLoopLength();
	private static native int DLLNotLoaded();
	private static native int InvalidArgument();
	private static native int InvalidHandle();
	private static native int InvalidSize();
	private static native int NoCreatorInfoAvailable();
	private static native int InvalidSlice();
	private static native int InvalidSampleRate();
	private static native int InvalidTempo();
	private static native int IsBeingPreviewed();
	private static native int NotBeingPreviewed();
	private static native int BufferTooSmall();
}
