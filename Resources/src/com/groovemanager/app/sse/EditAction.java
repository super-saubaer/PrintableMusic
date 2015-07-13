/*
 * Created on 31.05.2004
 *
 */
package com.groovemanager.app.sse;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * An EditAction stands for a type of action that can be applied to a part of
 * an audio source without need of interaction with the user. An EditAction
 * can be of any of the pre-defined constants.
 * @author Manu Robledo
 *
 */
public class EditAction extends Action {
	/**
	 * The SimpleSampleEditor to which this EditAction belongs
	 */
	SimpleSampleEditor editor;
	/**
	 * The type of this EditAction
	 */
	int type;
	/**
	 * Possible values for the edit type
	 */
	public final static int UNDO = 1, DELETE = 2, COPY = 3, CUT = 4, PASTE = 5, REDO = 6, TRIM = 7, NORMALIZE = 8;
	/**
	 * Create a new EditAction of the given type with the given name
	 * @param editor The SampleEditor to which this action belongs
	 * @param text The action큦 name
	 * @param editType The edit type of the action
	 */
	public EditAction(SimpleSampleEditor editor, String text, int editType) {
		super(text);
		this.editor = editor;
		type = editType;
	}
	/**
	 * Create a new EditAction of the given type with the given name and image
	 * @param editor The SampleEditor to which this action belongs
	 * @param text The action큦 name
	 * @param image This action큦 image
	 * @param editType The edit type of the action
	 */
	public EditAction(SimpleSampleEditor editor, String text, ImageDescriptor image, int editType) {
		super(text, image);
		this.editor = editor;
		type = editType;
	}
	/**
	 * Create a new EditAction of the given type with the given name and style
	 * @param editor The SampleEditor to which this action belongs
	 * @param text The action큦 name
	 * @param style This action큦 style
	 * @param editType The edit type of the action
	 */
	public EditAction(SimpleSampleEditor editor, String text, int style, int editType) {
		super(text, style);
		this.editor = editor;
		type = editType;
	}
	/**
	 * Tell the Sample Editor to perform this action
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run(){
		editor.editAction(type);
	}
	/**
	 * Get a textual description of an edit type
	 * @param i The edit type to get the name for
	 * @return A short name describing the edit type shortly
	 */
	public static String getName(int i){
		switch (i) {
			case UNDO :
				return "Undo";
			case DELETE :
				return "Delete";
			case COPY :
				return "Copy";
			case CUT :
				return "Cut";
			case PASTE :
				return "Paste";
			case REDO :
				return "Redo";
			case TRIM :
				return "Trim";
			case NORMALIZE :
				return "Normalize";
			default :
				return "";
		}
	}
}