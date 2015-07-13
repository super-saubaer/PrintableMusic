package com.groovemanager.sampled.waveform;

/**
 * A selection is an immutable class, that represents a selection consisting
 * of a left and a right border
 * @author Manu Robledo
 *
 */
public class Selection{
	/**
	 * The left border
	 */
	protected final int left,
	/**
	 * The right border
	 */
	right;
	/**
	 * Create a new Selection which has the same left and right border
	 * @param left The value for the left and the right border
	 */
	public Selection(int left){
		this.left = right = left;
	}
	/**
	 * Get the value of the left border
	 * @return The left border
	 */
	public int getLeft(){
		return left;
	}
	/**
	 * Get the value for the right border
	 * @return The right border
	 */
	public int getRight(){
		return right;
	}
	/**
	 * Create a new selection
	 * @param left The value for the left border
	 * @param right The value for the right border
	 */
	public Selection(int left, int right){
		if(left < right){
			this.left = left;
			this.right = right;
		}
		else{
			this.left = right;
			this.right = left;
		}
	}
	/**
	 * Ask this Selection, if its left border is the same than its right border
	 * @return true, if left and right border are the same, false otherwise
	 */
	public boolean isEmpty(){
		return left == right;
	}
	/**
	 * Get the length of this selection
	 * @return The length of this selection, that is the space between left and
	 * right border 
	 */
	public int getLength(){
		return right - left;
	}
}
