package com.groovemanager.sampled.waveform;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.groovemanager.thread.ProgressListener;
import com.groovemanager.thread.ProgressThread;

/**
 * This ProgressThread implementation can be used to save the data from a
 * WaveForm into a peak file
 * @author Manu Robledo
 *
 */
public class SavePeakWaveFormThread extends ProgressThread {
	/**
	 * The WaveForm to get the peak data from
	 */
	protected WaveForm source;
	/**
	 * The interval size
	 */
	protected int iSize;
	/**
	 * The last modification date of the audio file or -1, if it is not known
	 */
	protected long lastModified;
	/**
	 * The OutputStream to write to
	 */
	protected OutputStream out;
	/**
	 * The number of snapshots written so far
	 */
	protected int written = 0;
	/**
	 * Create a new SavePeakWaveFormThread using the interval size of the given
	 * WaveForm.
	 * @param source The WaveForm to get the peak data from
	 * @param out The OutputStream to write to
	 * @param lastModified last modification date of the audio file or -1, if
	 * it is not known
	 */
	public SavePeakWaveFormThread(WaveForm source, OutputStream out, long lastModified) {
		this(source, source.getIntervallSize(), out, lastModified);
	}
	/**
	 * Create a new SavePeakWaveFormThread using the given interval size
	 * @param source The WaveForm to get the peak data from
	 * @param iSize The interval size to use
	 * @param out The OutputStream to write to
	 * @param lastModified last modification date of the audio file or -1, if
	 * it is not known
	 */
	public SavePeakWaveFormThread(WaveForm source, int iSize, OutputStream out, long lastModified) {
		this.source = source;
		this.out = out;
		this.iSize = iSize;
		this.lastModified = lastModified;
	}
	/**
	 * Create a new SavePeakWaveFormThread using the given interval size that
	 * will be monitored by the given ProgressListener
	 * @param mon The ProgressListener to monitor the progress
	 * @param source The WaveForm to get the peak data from
	 * @param iSize The interval size to use
	 * @param out The OutputStream to write to
	 * @param lastModified last modification date of the audio file or -1, if
	 * it is not known
	 */
	public SavePeakWaveFormThread(ProgressListener mon, WaveForm source, int iSize, OutputStream out, long lastModified) {
		super(mon);
		this.source = source;
		this.out = out;
		this.iSize = iSize;
		this.lastModified = lastModified;
	}
	protected void init() {
		out = new BufferedOutputStream(out);

		byte[] header = new byte[34];
		ByteBuffer buffer = ByteBuffer.wrap(header);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put("GMPK".getBytes());
		buffer.putInt(iSize);
		buffer.putLong(lastModified);
		buffer.putLong(source.getRealLength());
		buffer.putShort((short)source.getChannels());
		int snapshots = source.getDisplayableLength();
		buffer.putLong(snapshots);
		
		try {
			out.write(header, 0, 34);
		} catch (IOException e) {
			e.printStackTrace();
			cancelOperation();
		}
	}
	protected int tellTotal() {
		return source.getDisplayableLength();
	}
	protected void processNext() throws IOException {
		// TODO... is eigentlich kein progressthread mehr...
		byte[] b = source.getData();
		out.write(b);
		written = b.length / 2 / source.getChannels();
		
	}
	protected int tellElapsed() {
		return written;
	}
	protected boolean breakCondition() {
		return false;
	}
	protected void cleanUp() {
		if(out != null)
			try {
				out.close();
			} catch (IOException e) {
			}
	}
	protected Object result() {
		return out;
	}
}
