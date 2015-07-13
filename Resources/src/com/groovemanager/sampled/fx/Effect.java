/*
 * Created on 23.06.2004
 *
 */
package com.groovemanager.sampled.fx;

import java.nio.FloatBuffer;

import javax.sound.sampled.Control;

/**
 * An Effect is an audio processing unit that may or may not need to analyze
 * the audio data before it can process it. All processing will be done with
 * 32 Bit float encoded samples.
 * @author Manu Robledo
 *
 */
public interface Effect {
	/**
	 * Get the set of Controls for manipulation of this effect´s parameters
	 * @return An Array of Control instances for the manipualtion of this
	 * effect´s attributes.
	 */
	public Control[] getControls();
	/**
	 * Ask this effect, if it supports a Control of the given type
	 * @param type The type of Control to ask for
	 * @return true, if this type of control is supported by this effect,
	 * false otherwise
	 */
	public boolean isControlSupported(Control.Type type);
	/**
	 * Get a Control of the desired type from this effect
	 * @param type The type of the Control
	 * @return The Control of the specified type, if the effect supports such
	 * a Control. Otherwise <code>null</code>.
	 */
	public Control getControl(Control.Type type);
	/**
	 * Start the effect processing with the given sample rate. A call to this
	 * method will usually be followed by a number of calls to
	 * <code>process()</code> and not necessarily by a final call to
	 * <code>close()</code>. An Effect instance can be opened, closed and
	 * re-opened unlimited times.
	 * @param sampleRate The samplerate of the audio data to process
	 */
	public void open(float sampleRate);
	/**
	 * Indicate, whether this effect is open and therefore ready for processing
	 * or not.
	 * @return true, if the Effect is open, false otherwise.
	 */
	public boolean isOpen();
	/**
	 * Close this Effect. A call to this method is optional and will only tell
	 * the Effect, that no further calls to <code>process()</code> are to be
	 * expected before the next call to <code>open()</code>
	 *
	 */
	public void close();
	/**
	 * Process a buffer of audio data. This method is the heart of the Effect.
	 * In it the processing will be done. The given buffers will all be
	 * rewinded and have the same number of floats remaining, so that any one
	 * of the buffers can be used for iteration. The Arrays are always of size
	 * 2 with the left channel´s data at index [0] and the right channel at
	 * index [1]. The in-buffers contain the audio data before processing and
	 * the out-buffers should contain the processed data after the return of
	 * this method. 
	 * All audio data is normalized to floats with a maximum of +/- 1.0.
	 * Anyway it may happen, that some values are greater than 1.0 or less than
	 * -1.0 because a previous Effect in the chain has exceeded the maximum
	 * amplitude. This is no problem. No clipping will occur until the end of
	 * the processing chain has been reached. This should then be done by the
	 * application so that each Effect doesn´t have to worry about exceeding
	 * the maximum amplitude.   
	 * @param in Array of two FloatBuffers containing the audio data to be
	 * processed for the left (index [0]) and the right (index [1]) channel.
	 * @param out Array of two FloatBuffers into which the processed audio
	 * data should be written during this method.
	 */
	public void process(FloatBuffer[] in, FloatBuffer[] out);
	/**
	 * If this Effects needs to analyze the whole audio data before processing,
	 * an analyzation will be done before the processing. This method will be
	 * called to indicate that analyzation starts and will usually be followed
	 * by a number of calls to <code>analyze()</code> and an optional final
	 * call to <code>stopAnalysis()</code>. After finishing analyzation
	 * <code>open()</code> to start processing. so an analyzing Effect is not
	 * necessarily open.
	 * @param sampleRate The sample rate of the audio data to be analyzed 
	 */
	public void startAnalysis(float sampleRate);
	/**
	 * Analyze a buffer of audio data
	 * @param in Array of two FloatBuffers containing the audio data to be
	 * analyzed for the left (index [0]) and the right (index [1]) channel.
	 */
	public void analyze(FloatBuffer[] in);
	/**
	 * Indicate that the analyzation process has been finished and no more
	 * calls to <code>analyze()</code> are to be expected before the next call
	 * to <code>startAnalysis()</code>. This method is optional.
	 *
	 */
	public void stopAnalysis();
	/**
	 * Indicates whether this Effect is currently expecting calls to
	 * <code>analyze()</code> or not.
	 * @return true, if this Effect is currently in analyzing state, false
	 * otherwise
	 */
	public boolean isAnalyzing();
	/**
	 * Ask this Effect, if it needs to analyze the whole audio data before
	 * being able to process it
	 * @return true, if anayzation is needed by this Effect, false otherwise
	 */
	public boolean needsAnalysis();
	/**
	 * If undo is supported, this method should return an Effect that can
	 * bring the processed audio data into the state it had before processing
	 * by applying it to the processed audio data.
	 * @return An Effect that is the exact undo-Effect of this Effect.
	 */
	public Effect undoEffect();
	/**
	 * Ask this Effect, if it can provide an Undo-Effect
	 * @return true, if undo is supported and a call to
	 * <code>undoEffect()</code> will return a valid Undo-Effect, false
	 * otherwise 
	 */
	public boolean isUndoable();
	/**
	 * Get the name of this Effect
	 * @return This effect´s name
	 */
	public String getName();
}
