package com.groovemanager.sampled.waveform;

/**
 * A Marker is an element that can be added to a Markable instance. It has a
 * position and a name.
 * @author Manu Robledo
 *
 */
public class Marker{
	/**
	 * The Marker큦 position
	 */
	private int position;
	/**
	 * The Marker큦 name
	 */
	private String name;
	/**
	 * Construct a new Marker
	 * @param pos The position of the Marker
	 * @param s The name of the Marker
	 */
	public Marker(int pos, String s){
		position = pos;
		name = s;
	}
	/**
	 * Set the Marker큦 name
	 * @param s The new name
	 */
	public void setName(String s){
		name = s;
	}
	/**
	 * Set the marker큦 position
	 * @param pos The new position
	 */
	protected void setPosition(int pos){
		position = pos;
	}
	/**
	 * Get the Marker큦 name
	 * @return The name of the Marker
	 */
	public String getName(){
		return name;
	}
	/**
	 * Get the marker큦 position
	 * @return The Marker큦 position
	 */
	public int getPosition(){
		return position;
	}
}
