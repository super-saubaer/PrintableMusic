package lmu_PrintableMusic_object;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import eu.printingin3d.javascad.coords.Coords3d;
import eu.printingin3d.javascad.coords.Dims3d;
import eu.printingin3d.javascad.exceptions.IllegalValueException;
import eu.printingin3d.javascad.models.Abstract3dModel;
import eu.printingin3d.javascad.models.Cube;
import eu.printingin3d.javascad.models.Prism;
import eu.printingin3d.javascad.utils.SaveScadFiles;

public class ModelPrism 
{
	private static List<Abstract3dModel> myModelList = new ArrayList<>();
	private static String name = ""; 
	private static String timeStamp = ""; 
	private static String current = "current";
	private static Coords3d currPos = new Coords3d(0.0, 0.0, 0);
	
	
	
	public ModelPrism(String name)
	{
		this.setName(name);
		myModelList = new ArrayList<>();
		currPos = new Coords3d(0.0, 0.0, 0);
	    
	    
		
		//add Base
		addPrism(10.0, 100);
		
	}
	
	
	
	
	public void addPrism(Double radius, Integer sides)
	{
		
		
		myModelList.add(new Prism(0.2, radius, sides).move(new Coords3d(currPos.getX(), currPos.getY(), currPos.getZ())));
		
		
		currPos = new Coords3d(currPos.getX(), currPos.getY(), currPos.getZ() + 0.2);
		//System.out.println(currPos.getZ());
	}
	
	public void safeModel()
	{
		ModelCreate update = new ModelCreate(myModelList);
		
		try 
		{
			new SaveScadFiles(new File("/media/sebastian/Stuff/LMU/15_SS/Kunst/reources/current")).
			addModel(current + "_model.scad", update).
			saveScadFiles();
		} 
		catch (IllegalValueException e) {e.printStackTrace();} 
		catch (IOException e) {e.printStackTrace();}
	}
	
	public void safeForGood()
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Calendar cal = Calendar.getInstance();
		
		ModelCreate update = new ModelCreate(myModelList);
		
		try 
		{
			new SaveScadFiles(new File("/media/sebastian/Stuff/LMU/15_SS/Kunst/Modelle")).
			addModel(dateFormat.format(cal.getTime()) + "_prism_model.scad", update).
			saveScadFiles();
		} 
		catch (IllegalValueException e) {e.printStackTrace();} 
		catch (IOException e) {e.printStackTrace();}
	}




	public static String getName() {
		return name;
	}




	public void setName(String name) {
		ModelPrism.name = name;
	}


}
