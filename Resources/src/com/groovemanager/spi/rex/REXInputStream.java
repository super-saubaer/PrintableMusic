/*
 * Created on 13.06.2004
 *
 */
package com.groovemanager.spi.rex;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * This class is used when constructing an AudioInputStream for REX files.
 * It makes use of the REX API´s preview functionality. 
 * @author Manu Robledo
 *
 */
class REXInputStream extends InputStream {
	/**
	 * The REX handle representing the file to be streamed
	 */
	private REXHandle handle;
	/**
	 * The length of the file in sample frames
	 */
	private int length;
	/**
	 * The number of channels in the file
	 */
	private int channels;
	/**
	 * The buffers (one for each channel) into which the audio data will be
	 * rendered
	 */
	private ByteBuffer[] buffers = new ByteBuffer[2];
	/**
	 * The number of frames rendered so far.
	 */
	private int currentSlice = 0;
	/**
	 * The number of slices
	 */
	private int sliceCount = 0;
	/**
	 * The REXSliceInfo objects for all slices
	 */
	private REXSliceInfo[] sliceInfos;
	/**
	 * Constrcuts a new REXInputStream
	 * @param handle A REXHandle object referring to the audio data that should
	 * be streamed 
	 * @throws REXError
	 */
	REXInputStream(REXHandle handle) throws REXError {
		this.handle = handle;
		
		REXInfo i = handle.REXGetInfo();
		length = i.getFrameLength();
		channels = i.fChannels();
		
		sliceCount = i.fSliceCount();
		sliceInfos = new REXSliceInfo[sliceCount];
		for (int j = 0; j < sliceInfos.length; j++) {
			sliceInfos[j] = handle.REXGetSliceInfo(j);
		}
		
		buffers[0] = ByteBuffer.allocateDirect(length * 4);
		buffers[0].position(buffers[0].capacity());
		buffers[1] = ByteBuffer.allocateDirect(length * 4);
		buffers[1].position(buffers[1].capacity());
	}
	/**
	 * because of the frame size granularity of 4 or 8 bytes, this method should
	 * never be called. Will always return 0.
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		return 0;
	}
	/**
	 * 
	 * @see java.io.InputStream#read(byte[])
	 */
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}
	/**
	 * 
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	public int read(byte[] b, int off, int len) throws IOException {
		if(!(buffers[0].hasRemaining()) && !renderNext()) return -1;
		
		len = Math.min(len, channels * buffers[0].remaining());
		len -= len % (channels * 4);
		if(channels == 1){
			buffers[0].get(b, off, len);
		}
		else{
			for(int i = 0; i < len / 2 / 4; i++){
				buffers[0].get(b, off + i * 8, 4);
				buffers[1].get(b, off + i * 8 + 4, 4);
			}
		}
		return len;
	}
	/**
	 * Render the next part of audio data
	 * @return true if there was still data left to render, false otherwise
	 */
	private boolean renderNext(){
		if(currentSlice == sliceCount) return false;
		else{
			buffers[0].rewind();
			buffers[1].rewind();
			int limit = sliceInfos[currentSlice].sampleLengthTillNext();
			//int limit = sliceInfos[currentSlice].fSamplelength();
			buffers[0].limit(buffers[0].capacity());
			buffers[1].limit(buffers[1].capacity());
			try {
				handle.REXRenderSlice(currentSlice, sliceInfos[currentSlice].fSamplelength(), buffers);
			} catch (REXError e) {
				e.printStackTrace();
				return false;
			}
			buffers[0].limit(limit * 4);
			buffers[1].limit(limit * 4);
			currentSlice++;
			return true;
		}
	}
	/**
	 * 
	 * @see java.io.InputStream#available()
	 */
	public int available() throws IOException {
		return (buffers[0].remaining() * channels);
	}
	/**
	 * @see java.io.InputStream#markSupported()
	 */
	public boolean markSupported() {
		return false;
	}
	/**
	 * @see java.io.InputStream#skip(long)
	 */
	public long skip(long s) throws IOException {
		if(!buffers[0].hasRemaining()) {
			if(!renderNext()) return 0;
		}
		s = Math.min(buffers[0].remaining() * channels, s);
		int newpos = buffers[0].position() + (int)s / channels;
		buffers[0].position(newpos);
		buffers[1].position(newpos);
		return s;
	}
}
