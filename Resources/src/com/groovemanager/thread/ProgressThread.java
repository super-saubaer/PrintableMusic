package com.groovemanager.thread;

import com.groovemanager.exception.InitException;

/**
 * A ProgressThread is a thread that can be used to perform a possibly long
 * running operation. The progress of the operation can be onitored using
 * a ProgressListener.
 * The total length fo the operation and the currently processed part of this
 * length can be measured in any unit. It is not specified.
 * @author Manu Robledo
 *
 */
public abstract class ProgressThread extends Thread{
	/**
	 * Length already processed
	 */
	private int elapsed,
	/**
	 * Totoal length
	 */
	total;
	/**
	 * ProgressListener for monitoring the progress
	 */
	private ProgressListener monitor = null;
	/**
	 * Inidicates whether this thread could complete its operation or not
	 */
	private boolean finished = false;
	/**
	 * Break condition for the run()-Method
	 */
	private boolean stopped = false;
	/**
	 * Optionally: An error message describing the cause, why an operation was
	 * cancelled.
	 */
	private String errorMessage = "";
	/**
	 * Construct a new ProgressThread
	 *
	 */
	public ProgressThread() {
	}
	/**
	 * Construct a new ProgressThread monitored by the given ProgressListener
	 * @param mon The ProgressListener
	 */	
	public ProgressThread(ProgressListener mon){
		setMonitor(mon);
	}
	/**
	 * Ask this thread, if it could complete its operation
	 * @return true, if this thread has completed its operation succesfully
	 * without being cancelled, false otherwise
	 */
	public boolean hasFinished(){
		return finished;
	}
	/**
	 * Set the ProgressListener to monitor this Thread
	 * @param mon The ProgressListener
	 */
	public void setMonitor(ProgressListener mon){
		monitor = mon;
	}
	/**
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run(){
		try {
			init();
		} catch (InitException e1) {
			e1.printStackTrace();
			cancelOperation(e1.getMessage());
			cleanUp();
			monitor.progressCancelled();
			return;
		}
		stopped = false;
		total = tellTotal();
		if(monitor != null){
			monitor.progressTotal(total);
			monitor.progressElapsed(0);
			monitor.progressStarted();
		}
		while((!stopped) && (elapsed < total || total == -1) && !breakCondition()){
			try {
				processNext();
				elapsed = tellElapsed();
				if(monitor != null){
					monitor.progressElapsed(elapsed);
				}
			} catch (Exception e) {
				e.printStackTrace();
				cancelOperation(e.getMessage());
			}
		}
		if(!stopped) finished = true;
		cleanUp();
		if(monitor != null){
			if(finished) monitor.progressFinished();
			else monitor.progressCancelled();
		}
	}
	/**
	 * Get the total length of the operation
	 * @return The total length of the operation. The unit to use for this
	 * value is free defineable
	 */
	public int getTotal(){
		return total;
	}
	/**
	 * Get the length of the operation elapsed so for
	 * @return The elapsed length of the operation. The unit to use for this
	 * value is free defineable
	 */
	public int getElapsed(){
		return elapsed;
	}
	/**
	 * Any kind of initialization in the moment right before starting the
	 * operation should be performed here
	 * @throws InitException If initialization fails
	 *
	 */
	protected abstract void init() throws InitException;
	/**
	 * Tell the total length of the operation
	 * @return The total length of the operation. The unit to use for this
	 * value is free defineable
	 */
	protected abstract int tellTotal();
	/**
	 * Perform the next step. This method will be called repeatedly as long
	 * as<br>
	 * - <code>tellElpased() < tellTotal()</code> and<br>
	 * - <code>breakCondition() != true</code> and<br>
	 * - The thread has not been cancelled
	 * @throws Exception This method may throw an Exception which will then
	 * lead to a cancellation of this operation
	 */
	protected abstract void processNext() throws Exception;
	/**
	 * Tell the length of the operation elapsed so for
	 * @return The elapsed length of the operation. The unit to use for this
	 * value is free defineable
	 */
	protected abstract int tellElapsed();
	/**
	 * The break condition to indicate that the operation has finished
	 * @return true, if the operation has finished and the thread should
	 * therefore come to its end, false otherwise
	 */
	protected abstract boolean breakCondition(); 
	/**
	 * Free any ressources and perform clean up operations. This methos will be
	 * called, if the operation has been cancelled, if an Exception is thrown
	 * by processNext(), or if the operation has been finished as expected.
	 *
	 */
	protected abstract void cleanUp();
	/**
	 * Specify the result of this operation, if any.
	 * @return Any kind of object representing the result of the performed
	 * operation. <code>null</code> should onlx be returned, if the operation
	 * didn´t finish correctly. If it did finish correctly, but it has no
	 * result, a dummy object should be returned.
	 */
	protected abstract Object result();
	/**
	 * Get the result of this operation, if any
	 * @return The resulting object of this operation or <code>null</code>,
	 * if the operation didn´t finish correctly
	 */
	public Object getResult(){
		if(hasFinished()) return result();
		else return null;
	}
	/**
	 * Cancel this operation
	 *
	 */
	public void cancelOperation(){
		stopped = true;
	}
	/**
	 * Cancel this operation because of the given reason
	 * @param string A text describing the reason for the cancellation
	 */
	private void cancelOperation(String string) {
		errorMessage = string;
		cancelOperation();
	}
	/**
	 * If this thread has been cancelled, get the reason for the cancellation.
	 * @return A text descibing the reason, why this thread has been cancelled
	 * or an empty String, if the reason is not nearer known.
	 */
	public String getErrorMessage(){
		return errorMessage;
	}
}