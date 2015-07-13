/*
 * Created on 04.08.2004
 *
 */
package com.groovemanager.sampled.fx;

import java.nio.FloatBuffer;

import javax.sound.sampled.Control;
import javax.sound.sampled.Control.Type;

import com.groovemanager.sampled.fx.control.BypassControl;
import com.groovemanager.sampled.fx.control.DelayTimeControl;
import com.groovemanager.sampled.fx.control.DryWetControl;
import com.groovemanager.sampled.fx.control.FeedbackControl;
import com.groovemanager.sampled.fx.control.InvertFeedbackControl;
import com.groovemanager.sampled.fx.control.LFODepthControl;
import com.groovemanager.sampled.fx.control.LFOFrequencyControl;
import com.groovemanager.sampled.fx.control.LFOWaveFormControl;

/**
 * A Chorus/Flanger Effect. The processing code is derived from the one given
 * in the book "Digital Audio with Java" by Craig A. Lindley.
 * @author Manu Robledo
 *
 */
public class Flanger extends AbstractEffect {
	private BypassControl bypassControl = new BypassControl(false);
	private DryWetControl dryWetControl = new DryWetControl(70);
	private LFOFrequencyControl lfoFreqControl = new LFOFrequencyControl(0, 5, 0.01f, 0.5f);
	private LFODepthControl lfoDepthControl = new LFODepthControl(0, 10, 0.01f, 2);
	private DelayTimeControl delayControl = new DelayTimeControl(0, 15, 0.01f, 0.5f);
	private FeedbackControl feedbackControl = new FeedbackControl(60);
	private InvertFeedbackControl invertControl = new InvertFeedbackControl(true);
	private LFOWaveFormControl waveFormControl = new LFOWaveFormControl(new String[]{"Sine", "Triangle"}, "Sine");
	
	private double sweepValue, step = -1;
	private int readIndex = -1, writeIndex = -1;
	private int sampleNumber, delayBufferSize = -1;
	
	private float[][] delayBuffer = new float[2][];
	
	public Flanger() {
		super("Chorus/Flanger");
	}
	public void open(float sampleRate) {
		super.open(sampleRate);
		readIndex = writeIndex = -1;
		sampleNumber = 0;
		sweepValue = 0;
		delayBufferSize = -1;
		step = -1;
	}
	public Control[] getControls() {
		return new Control[]{
			bypassControl,
			dryWetControl,
			waveFormControl,
			lfoFreqControl,
			lfoDepthControl,
			delayControl,
			feedbackControl,
			invertControl
		};
	}
	public boolean isControlSupported(Type type) {
		return type.equals(BypassControl.Type.BYPASS) ||
			type.equals(DryWetControl.Type.DRY_WET);
	}
	public Control getControl(Type type) {
		if(type.equals(BypassControl.Type.BYPASS)) return bypassControl;
		if(type.equals(DryWetControl.Type.DRY_WET)) return dryWetControl;
		else return null;
	}
	public void process(FloatBuffer[] in, FloatBuffer[] out) {
		if(bypassControl.getValue()){
			out[0].put(in[0]);
			out[1].put(in[1]);
		}
		else{
			if(writeIndex == -1) writeIndex = 0;
			if(readIndex == -1) readIndex = in[0].remaining();
			double radiansPerSample = lfoFreqControl.getValue() / sampleRate * 2 * Math.PI;
			double halfDepthInSamples = sampleRate * lfoDepthControl.getValue() / 1000.0 / 2;
			int delayOffset = (int)(delayControl.getValue() * sampleRate) / 1000;
			if (delayBufferSize == -1) delayBufferSize = in[0].remaining() + delayOffset;
			if(delayBuffer[0] == null || delayBuffer[0].length < delayBufferSize) delayBuffer[0] = new float[delayBufferSize];
			if(delayBuffer[1] == null || delayBuffer[1].length < delayBufferSize) delayBuffer[1] = new float[delayBufferSize];
			
			if(step == -1){
				double periodInSamples = (1.0 / lfoFreqControl.getValue()) * sampleRate;
				double quarterPeriod = periodInSamples / 4.0;

				step = halfDepthInSamples / quarterPeriod;
			}
			while(in[0].hasRemaining()){
				double sampleOffset1 = sweepValue - halfDepthInSamples;
				double sampleOffset2 = sampleOffset1 - 1;
				
				double delta = Math.abs((int)sampleOffset1 - sampleOffset1);
				int actualIndex1 = readIndex + (int)sampleOffset1;
				int actualIndex2 = readIndex + (int)sampleOffset2;

				boolean underflow1 = actualIndex1 < 0;
				boolean underflow2 = actualIndex2 < 0;
				
				if(underflow1) actualIndex1 += delayBufferSize;
				else actualIndex1 %= delayBufferSize;
				if(underflow2) actualIndex2 += delayBufferSize;
				else actualIndex2 %= delayBufferSize;
				
				// Left channel
				float delaySample1 = delayBuffer[0][actualIndex1];
				float delaySample2 = delayBuffer[0][actualIndex2];
				float delaySample = (float)(delaySample2 * delta + delaySample1 * (1.0 - delta));
				
				float wet = dryWetControl.getValue();
				float dry = 100.0f - wet;
				float input = in[0].get();
				float output = input * dry / 100f + delaySample * wet / 100f;
				out[0].put(output);
				
				input += (delaySample * feedbackControl.getValue() * (invertControl.getValue() ? -1:1)) / 100f;
				delayBuffer[0][writeIndex] = input;
				
				// Right channel
				delaySample1 = delayBuffer[1][actualIndex1];
				delaySample2 = delayBuffer[1][actualIndex2];
				delaySample = (float)(delaySample2 * delta + delaySample1 * (1.0 - delta));
				
				input = in[1].get();
				output = input * dry / 100f + delaySample * wet / 100f;
				out[1].put(output);
				
				input += (delaySample * feedbackControl.getValue() * (invertControl.getValue() ? -1:1)) / 100f;
				delayBuffer[1][writeIndex] = input;
				
				if(waveFormControl.getValue().equals("Sine")){
					sampleNumber %= sampleRate;
					sweepValue = halfDepthInSamples * Math.sin(radiansPerSample * sampleNumber);
				}
				else{
					sweepValue += step;
					if(sweepValue >= halfDepthInSamples || sweepValue <= -halfDepthInSamples) step *= -1;
				}

				readIndex++;
				readIndex %= delayBufferSize;
				writeIndex++;
				writeIndex %= delayBufferSize;
				sampleNumber++;
			}
		}
	}
	public Effect undoEffect() {
		return null;
	}
	public boolean isUndoable() {
		return false;
	}
}
