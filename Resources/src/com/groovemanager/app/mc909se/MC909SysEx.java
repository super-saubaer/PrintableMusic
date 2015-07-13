/*
 * Created on 21.06.2004
 *
 */
package com.groovemanager.app.mc909se;

/**
 * This helper class provides some static methods for creating Mc909 compatible
 * sysex messages.
 * @author Manu Robledo
 *
 */
public class MC909SysEx {
	/**
	 * Private constructor: This class doesn´t need to be instanciated
	 *
	 */
	private MC909SysEx() {
	}
	/**
	 * Create a sysex message for altering the rhythm level of the given part
	 * @param part The part to alter (between 0 and 15)
	 * @param level The new level to set, must be non-negative
	 * @return The sysex message as int-Array
	 */
	static int[] createRhythmLevelMessage(int part, byte level){
		if(part < 0) part = 0;
		if(part > 15) part = 15;
		
		int[] data = new int[5];
		data[0] = 0x11 + part / 4;
		data[1] = 0x10 + (part % 4) * 0x20;
		data[2] = 0x00;
		data[3] = 0x0C;
		data[4] = level;
		return createMessage(data);
	}
	/**
	 * Create a sysex message for altering the patch level of the given part
	 * @param part The part to alter (between 0 and 15)
	 * @param level The new level to set, must be non-negative
	 * @return The sysex message as int-Array
	 */
	static int[] createPatchLevelMessage(int part, byte level){
		if(part < 0) part = 0;
		if(part > 15) part = 15;
		
		int[] data = new int[5];
		data[0] = 0x11 + part / 4;
		data[1] = 0x00 + (part % 4) * 0x20;
		data[2] = 0x00;
		data[3] = 0x0E;
		data[4] = level;
		return createMessage(data);
	}
	/**
	 * Create a sysex message for setting the patch tone wave
	 * @param part The part to set the wave for (between 0 and 15)
	 * @param tone The tone to set the wave for (between 0 and 3)
	 * @param isUser true, if the wave is located in user area, false if it is
	 * located on the card
	 * @param leftNr The sample nr for the left channel
	 * @param rightNr The sample nr for the right channel
	 * @return The sysex message as int-Array
	 */
	static int[] createPatchWaveMessage(int part, int tone, boolean isUser, int leftNr, int rightNr){
		if(part < 0) part = 0;
		if(part > 15) part = 15;
		
		if(tone < 0) tone = 0;
		if(tone > 3) tone = 3;
		
		int[] data = new int[18];
		data[0] = 0x11 + part / 4;
		data[1] = 0x00 + (part % 4) * 0x20;
		data[2] = 0x20 + tone * 0x02;
		data[3] = 0x27;
		
		data[4] = 3;
		if(isUser) data[8] = 2;
		else data[8] = 3;

		data[9] = leftNr / (16 * 16 * 16);
		leftNr -= 16*16*16*data[9];
		data[10] = leftNr / (16 * 16);
		leftNr -= 16*16*data[10];
		data[11] = leftNr / 16;
		leftNr -= 16*data[11];
		data[12] = leftNr;
		
		data[13] = rightNr / (16 * 16 * 16);
		rightNr -= 16*16*16*data[13];
		data[14] = rightNr / (16 * 16);
		rightNr -= 16*16*data[14];
		data[15] = rightNr / 16;
		rightNr -= 16*data[15];
		data[16] = rightNr;
		
		data[17] = 3;
		
		return createMessage(data);
	}
	/**
	 * Create a sysex message for setting the rhythm name of the given part
	 * @param part The part to set the rhythm name for (between 0 and 15)
	 * @param name The new name to set
	 * @return The sysex message as int-Array
	 */
	static int[] createRhythmNameMessage(int part, String name){
		if(part < 0) part = 0;
		if(part > 15) part = 15;
		
		if(name.length() > 12) name = name.substring(0, 12);
		else while(name.length() < 12) name += " ";
		byte[] nameBytes = name.getBytes();
		
		int[] data = new int[16];
		data[0] = 0x11 + part / 4;
		data[1] = 0x10 + (part % 4) * 0x20;
		data[2] = 0x00;
		data[3] = 0x00;
		for(int i = 0; i < 12; i++){
			data[4 + i] = nameBytes[i];
		}
		return createMessage(data);
	}
	/**
	 * Create a sysex message for setting a rhythm tone´s wave
	 * @param part The part to set the wave for (between 0 and 15)
	 * @param key The key of the rhythm set to set the wave for (between 0 and
	 * 15)
	 * @param wmt Wave mix table value (between 0 and 3)
	 * @param isUser true, if the wave is located in user area, false if it is
	 * located on the card.
	 * @param leftNr The sample nr for the left channel
	 * @param rightNr The sample nr for the right channel
	 * @return The sysex message as int-Array
	 */
	static int[] createRhythmWaveMessage(int part, int key, int wmt, boolean isUser, int leftNr, int rightNr){
		if(part < 0) part = 0;
		if(part > 15) part = 15;
		
		if(key < 0) key = 0;
		if(key > 15) key = 15;
		
		if(wmt < 0) wmt = 0;
		if(wmt > 3) wmt = 3;
		
		int[] data = new int[18];
		data[0] = 0x11 + part / 4;
		data[1] = 0x10 + (part % 4) * 0x20;
		data[2] = 0x5C + 0x02 * key;
		data[3] = 0x22 + wmt * 0x1D;
		data[4] = 3;
		if(isUser) data[8] = 2;
		else data[8] = 3;

		data[9] = leftNr / (16 * 16 * 16);
		leftNr -= 16*16*16*data[9];
		data[10] = leftNr / (16 * 16);
		leftNr -= 16*16*data[10];
		data[11] = leftNr / 16;
		leftNr -= 16*data[11];
		data[12] = leftNr;
		
		data[13] = rightNr / (16 * 16 * 16);
		rightNr -= 16*16*16*data[13];
		data[14] = rightNr / (16 * 16);
		rightNr -= 16*16*data[14];
		data[15] = rightNr / 16;
		rightNr -= 16*data[15];
		data[16] = rightNr;
		
		data[17] = 3;
		
		return createMessage(data);
	}
	/**
	 * Create a sysex message for setting a rhythm tone´s name
	 * @param part The part to set the rhythm tone for (between 0 and 15)
	 * @param name The name to set
	 * @param key The rhythm set key to set the name for (between 0 and 15)
	 * @return The sysex message as int-Array
	 */
	static int[] createRhythmToneNameMessage(int part, String name, int key){
		if(part < 0) part = 0;
		if(part > 15) part = 15;
		
		if(name.length() > 12) name = name.substring(0, 12);
		else while(name.length() < 12) name += " ";
		byte[] nameBytes = name.getBytes();
		
		if(key < 0) key = 0;
		if(key > 15) key = 15;
		
		int[] data = new int[16];
		data[0] = 0x11 + part / 4;
		data[1] = 0x10 + (part % 4) * 0x20;
		data[2] = 0x5C + 0x02 * key;
		data[3] = 0x00;
		for(int i = 0; i < 12; i++){
			data[4 + i] = nameBytes[i];
		}
		return createMessage(data);
	}
	/**
	 * Create a sysex message for setting a patch´s name
	 * @param part The part to set the patch name for (between 0 and 15)
	 * @param name The name to set
	 * @return The sysex message as int-Array
	 */
	static int[] createPatchNameMessage(int part, String name){
		if(part < 0) part = 0;
		if(part > 15) part = 15;
		
		if(name.length() > 12) name = name.substring(0, 12);
		else while(name.length() < 12) name += " ";
		byte[] nameBytes = name.getBytes();
		
		int[] data = new int[16];
		data[0] = 0x11 + part / 4;
		data[1] = 0x00 + (part % 4) * 0x20;
		data[2] = 0x00;
		data[3] = 0x00;
		for(int i = 0; i < 12; i++){
			data[4 + i] = nameBytes[i];
		}
		return createMessage(data);
	}
	/**
	 * Create a sysex message out of the given data
	 * @param data The sysex data
	 * @return The sysex message as int-Array
	 */
	static int[] createMessage(int[] data){
		int[] message = new int[6 + data.length + 2];
		message[0] = 0xF0;
		message[1] = 0x41;
		message[2] = 0x7F;
		message[3] = 0x00;
		message[4] = 0x59;
		message[5] = 0x12;
		int total = 0;
		for(int i = 0; i < data.length; i++){
			total += data[i];
			message[6 + i] = data[i];
		}
		message[message.length - 2] = 128 - (total % 128);
		message[message.length - 1] = 0xF7;
		return message;
	}
}
