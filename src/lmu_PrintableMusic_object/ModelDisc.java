package lmu_PrintableMusic_object;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eu.printingin3d.javascad.coords.Coords3d;
import eu.printingin3d.javascad.coords.Dims3d;
import eu.printingin3d.javascad.exceptions.IllegalValueException;
import eu.printingin3d.javascad.models.Abstract3dModel;
import eu.printingin3d.javascad.models.Cube;
import eu.printingin3d.javascad.models.Cylinder;
import eu.printingin3d.javascad.models.Sphere;
import eu.printingin3d.javascad.utils.SaveScadFiles;

public class ModelDisc 
{
	private static List<Abstract3dModel> myModelList = new ArrayList<>();
	private static String name = ""; 
	private static Coords3d currPos = new Coords3d(0.0, 0.5, 0);
	
	
	
	public ModelDisc(String name)
	{
		this.setName(name);
		//movesList.add(new Coords3d(2.5, 2.5, 2.5));
		//myModelList.add(new Cube(new Dims3d(1, 1, 1)).move(new Coords3d(2.5, 2.5, 2.5)));
		
	}
	
	
	
	
	public void addDisc(Double x, Double y, Double z)
	{
		
		
		myModelList.add(new Cylinder(1, 10));
		
		
		currPos = new Coords3d(currPos.getX(), (currPos.getY() + y/2), 0);
		System.out.println(currPos.getY());
	}
	
	public void safeModel()
	{
		ModelCreate update = new ModelCreate(myModelList);
		
		try 
		{
			new SaveScadFiles(new File("D:/LMU/15_SS/Kunst/Modelle")).
			addModel("second_try.scad", update).
			saveScadFiles();
		} 
		catch (IllegalValueException e) {e.printStackTrace();} 
		catch (IOException e) {e.printStackTrace();}
	}




	public static String getName() {
		return name;
	}




	public void setName(String name) {
		ModelDisc.name = name;
	}

}
