package lmu_PrintableMusic_object;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import eu.printingin3d.javascad.context.IScadGenerationContext;
import eu.printingin3d.javascad.coords.Boundaries3d;
import eu.printingin3d.javascad.coords.Coords3d;
import eu.printingin3d.javascad.coords.Dims3d;
import eu.printingin3d.javascad.enums.Side;
import eu.printingin3d.javascad.exceptions.IllegalValueException;
import eu.printingin3d.javascad.models.Abstract3dModel;
import eu.printingin3d.javascad.models.Cube;
import eu.printingin3d.javascad.models.IModel;
import eu.printingin3d.javascad.models.Prism;
import eu.printingin3d.javascad.models.SCAD;
import eu.printingin3d.javascad.utils.SaveScadFiles;
import eu.printingin3d.javascad.vrl.CSG;
import eu.printingin3d.javascad.vrl.FacetGenerationContext;

public class ModelCube 
{
	private static List<Abstract3dModel> myModelList = new ArrayList<>();
	static Coords3d[] maxima = new Coords3d[4]; 
	
	
	private static String name = ""; 
	private static Coords3d currPos = new Coords3d(0.0, 0.5, 0);
	public static Integer count = 0;
	
	
	
	public ModelCube(String name)
	{
		this.setName(name);
		myModelList = new ArrayList<>();
		
		currPos = new Coords3d(0.0, 0.0, 0);
		
		//add Base
		myModelList.add(new Cube(new Dims3d(15.0, 15.0, 5.0)).move(new Coords3d(0, 0, 0)));
		currPos = new Coords3d(currPos.getX(), currPos.getY(), currPos.getZ());
		maxima[0] = new Coords3d(4.0, 0, 2.0); //x
		maxima[1] = new Coords3d(0, 4.0, 2.0); //y
		maxima[2] = new Coords3d(-4.0, 0, 2.0); //-x		
		maxima[3] = new Coords3d(0, -4.0, 2.0); //-x	
		
	}
	
	
	
	
	public void addCube(Double x, Double y, Double z)
	{
		Integer factor = 1;
		if((count % 2) == 0)
		{
			factor = 1;
		}
		else
		{
			factor = -1;
		}
		
		
		//System.out.println("Zuwachs: " + x/150);
		//System.out.println("Schtrengsch: " + y);
		
		if(x < 42)
		{
			currPos = new Coords3d(maxima[0].getX()+(x/100)*factor, maxima[0].getY()+(x/100)*factor, maxima[0].getZ()+(x/300));
			maxima[0] = currPos;
			
		}
		else if(x >= 42 && x < 64)
		{
			currPos = new Coords3d(maxima[1].getX()-(x/100)*factor, maxima[1].getY()+(x/100)*factor, maxima[1].getZ()+(x/300));
			maxima[1] = currPos;
		}
		else if(x >= 64 && x < 86)
		{
			currPos = new Coords3d(maxima[2].getX()+(x/100)*factor, maxima[2].getY()-(x/100)*factor, maxima[2].getZ()+(x/300));
			maxima[2] = currPos;
		}
		else if(x >= 84 && x <= 108)
		{
			currPos = new Coords3d(maxima[3].getX()-(x/100)*factor, maxima[3].getY()-(x/100)*factor, maxima[3].getZ()+(x/300));
			maxima[3] = currPos;
		}
		
		Double size = y % 4;
		if(size < 1.0)
		{
			size = 1.0;
		}
		
		myModelList.add(new Cube(new Dims3d(size, size, size)).move(new Coords3d(currPos.getX(), currPos.getY(), currPos.getZ())));
		
		
		
		/*
		 * 
		if((count % 3) == 0)
		{
			currPos = new Coords3d(-x, y, z);
		}
		else if((count % 3) == 1)
		{
			currPos = new Coords3d(x, -y, z);
		}
		else if((count % 3) == 2)
		{
			currPos = new Coords3d(x, y, -z);
		}
		 */
		count++;
		
		//System.out.println("X: " + currPos.getX());
		//System.out.println("Y: " + currPos.getY());
		//System.out.println("Z: " + currPos.getZ());
	}
	
	public void safeModel()
	{
		ModelCreate update = new ModelCreate(myModelList);
		
		try 
		{
			//System.out.println(File.separator + "media" + File.separator + "sebastian" + File.separator + "Stuff" + File.separator + "LMU/15_SS/Kunst/reources/current");
			new SaveScadFiles(new File(File.separator + "media" + File.separator + "sebastian" + File.separator + "Stuff" + File.separator + "LMU/15_SS/Kunst/reources/current")).
			addModel("current_model.scad", update).
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
			addModel(dateFormat.format(cal.getTime()) + "_cube_model.scad", update).
			saveScadFiles();
		} 
		catch (IllegalValueException e) {e.printStackTrace();} 
		catch (IOException e) {e.printStackTrace();}
	}




	public static String getName() {
		return name;
	}




	public void setName(String name) {
		ModelCube.name = name;
	}

}
