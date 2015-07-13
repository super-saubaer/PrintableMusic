package lmu_PrintableMusic;


import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.imageio.ImageIO;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


import lmu_PrintableMusic_object.ModelCube;
import lmu_PrintableMusic_object.ModelDisc;
import lmu_PrintableMusic_object.ModelPrism;

public class MidiInputReceiver implements Receiver, ActionListener 
{
	
	static String mode = "";
	static boolean record = false;
	static boolean linux = true;
	
	JButton button_safe = new JButton("<html>SAFE<br />SCULPTURE</html>");
	JButton button_cube = new JButton("CUBES");
	JButton button_prism = new JButton("PRISM");
	JButton button_start = new JButton("<html>START<br />RECORDING</html>");
	
	
	Font customFont = null;
	
	//hallo
	//Safetypush2+3+4
	
	
	//Synthie
	MidiSynthesizer synth = new MidiSynthesizer();
    public String name;
    
    
   

    //ModelHandler
    ModelPrism prism = new ModelPrism("Piano");
    ModelCube cube = new ModelCube("Piano");
    
    
    public MidiInputReceiver(String name) 
    {
        this.name = name;
    }
    public void send(MidiMessage msg, long timeStamp) 
    {
    	
    	ShortMessage answer = new ShortMessage();
    	answer = (ShortMessage) msg;
        //System.out.println("time: " + timeStamp);
        
        //Note note = new Note();
        //note.setPitch(answer.getData1());
        //note.setDuration(10.00);
        
        
        if(msg.getStatus() == 144)
		{
        	//synth.startMidi(msg, timeStamp);
        	synth.playSynth(answer, timeStamp);
        	
        	Double strength = Double.valueOf(answer.getData2());
        	Integer tone = answer.getData1();
        	
        	if(mode.equals("prism"))
        	{
        		strength = Double.valueOf(answer.getData2()) / 100;
        		if(strength < 0.5)
        		{
        			strength = strength + 0.75;
        		}
        		
        		
        		tone = answer.getData1() % 7;
        		if(tone < 4)
        		{
        			tone = tone + 7;
        		}
        	}
        	
        	//System.out.println("strength: " + strength*3.5);
        	//System.out.println("tone: " + tone);
        	
        	if(mode.equals("prism") && record)
        	{
        		prism.addPrism(strength*strength*3.5, tone);
            	prism.safeModel();
        		
        	}
        	else if(mode.equals("cube") && record)
        	{
        		cube.addCube(Double.valueOf(tone), Double.valueOf(strength), Double.valueOf(tone));
            	cube.safeModel();
        	}
        	
        	
		
		}
		else
		{
			//synth.endMidi(msg, timeStamp);
			//System.out.println("end: " + timeStamp);
			synth.stopSynth(answer, timeStamp);
		}
        
        
       
    }
    public void close() {}
    
    public void initSynth() throws MidiUnavailableException
    {
    	synth.initSynth();
    	
    }
    public void initFrontend()
    {
    	
    	//Font
    	 //create the font
        try 
        {
            //create the font to use. Specify the size!
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File(File.separator + "media" + File.separator + "sebastian" + File.separator + "Stuff" + File.separator + "IRT" + File.separator + "06_Eclipse" + File.separator + "lmu_PrintableMusic" + File.separator + "Resources" + File.separator + "PRINTABLE_MUSIC" + File.separator + "HFJ_Gotham_1" + File.separator + "HFJ_Gotham_1" + File.separator + "Gotham-Book.otf")).deriveFont(24f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            //register the font
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File(File.separator + "media" + File.separator + "sebastian" + File.separator + "Stuff" + File.separator + "IRT" + File.separator + "06_Eclipse" + File.separator + "lmu_PrintableMusic" + File.separator + "Resources" + File.separator + "PRINTABLE_MUSIC" + File.separator + "HFJ_Gotham_1" + File.separator + "HFJ_Gotham_1" + File.separator + "Gotham-Book.otf")));
        } catch (IOException e) {e.printStackTrace();}
        catch(FontFormatException e)
        {
            e.printStackTrace();
        }

    	
    	JPanel panel = new JPanel();
    	JPanel button_panel = new JPanel();
    	JPanel first_panel = new JPanel();
    	JPanel second_panel = new JPanel();
    	JPanel third_panel = new JPanel();
    	
    	
		
		button_safe.addActionListener(this);
		button_safe.setFont(customFont);
		button_safe.setEnabled(false);
		button_cube.addActionListener(this);
		button_prism.addActionListener(this);
		button_start.addActionListener(this);
		button_safe.setFont(customFont);
		button_cube.setFont(customFont);
		button_prism.setFont(customFont);
		button_start.setFont(customFont);
		button_start.setEnabled(false);
		
		//hallo
		
		//Images
		BufferedImage pic_welcome = null;
		BufferedImage pic_1 = null;
		BufferedImage pic_2 = null;
		BufferedImage pic_3 = null;
		try 
		{
			if(linux)
			{
				pic_welcome = ImageIO.read(new File(File.separator + "media" + File.separator + "sebastian" + File.separator + "Stuff" + File.separator + "IRT" + File.separator + "06_Eclipse" + File.separator + "lmu_PrintableMusic" + File.separator + "Resources" + File.separator + "PRINTABLE_MUSIC" + File.separator + "welcome_neu2.jpg"));
				pic_1 = ImageIO.read(new File(File.separator + "media" + File.separator + "sebastian" + File.separator + "Stuff" + File.separator + "IRT" + File.separator + "06_Eclipse" + File.separator + "lmu_PrintableMusic" + File.separator + "Resources" + File.separator + "PRINTABLE_MUSIC" + File.separator + "erstens.jpg"));
				pic_2 = ImageIO.read(new File(File.separator + "media" + File.separator + "sebastian" + File.separator + "Stuff" + File.separator + "IRT" + File.separator + "06_Eclipse" + File.separator + "lmu_PrintableMusic" + File.separator + "Resources" + File.separator + "PRINTABLE_MUSIC" + File.separator + "zweitens.jpg"));
				pic_3 = ImageIO.read(new File(File.separator + "media" + File.separator + "sebastian" + File.separator + "Stuff" + File.separator + "IRT" + File.separator + "06_Eclipse" + File.separator + "lmu_PrintableMusic" + File.separator + "Resources" + File.separator + "PRINTABLE_MUSIC" + File.separator + "drittens.jpg"));

			}
			else
			{
				pic_welcome = ImageIO.read(new File("D:" + File.separator + "IRT" + File.separator + "06_Eclipse" + File.separator + "lmu_PrintableMusic" + File.separator + "Resources" + File.separator + "PRINTABLE_MUSIC" + File.separator + "welcome_neu2.jpg"));
				pic_1 = ImageIO.read(new File("D:" + File.separator + "IRT" + File.separator + "06_Eclipse" + File.separator + "lmu_PrintableMusic" + File.separator + "Resources" + File.separator + "PRINTABLE_MUSIC" + File.separator + "erstens.jpg"));
				pic_2 = ImageIO.read(new File("D:" + File.separator + "IRT" + File.separator + "06_Eclipse" + File.separator + "lmu_PrintableMusic" + File.separator + "Resources" + File.separator + "PRINTABLE_MUSIC" + File.separator + "zweitens.jpg"));
				pic_3 = ImageIO.read(new File("D:" + File.separator + "IRT" + File.separator + "06_Eclipse" + File.separator + "lmu_PrintableMusic" + File.separator + "Resources" + File.separator + "PRINTABLE_MUSIC" + File.separator + "drittens.jpg"));
				
			}
			
		} catch (IOException e) {e.printStackTrace();}
		JLabel picLabHeader = new JLabel(new ImageIcon(resize(pic_welcome, 800, 400)));
		JLabel picLab1 = new JLabel(new ImageIcon(resize(pic_1, 200, 200)));
		JLabel picLab2 = new JLabel(new ImageIcon(resize(pic_2, 200, 200)));
		JLabel picLab3 = new JLabel(new ImageIcon(resize(pic_3, 200, 200)));
		
		
		
		button_panel.setLayout(new GridLayout(2, 1));
		button_panel.add(button_cube);
		button_panel.add(button_prism);
		
		first_panel.setLayout(new GridLayout(1, 2));
		first_panel.add(picLab1);
		first_panel.add(button_panel);
		
		second_panel.setLayout(new GridLayout(1, 2));
		second_panel.add(picLab2);
		second_panel.add(button_start);
		
		third_panel.setLayout(new GridLayout(1, 2));
		third_panel.add(picLab3);
		third_panel.add(button_safe);
		
		
		
		
		panel.setLayout(new GridLayout(4,1));
		
		panel.add(picLabHeader);
		panel.add(first_panel);
		panel.add(second_panel);
		panel.add(third_panel);
		/*
		 * 
		panel.add(button_start);
		panel.add(picLab3);
	    panel.add(button_safe);
		 */
		
		
		
		JFrame myWindow = new JFrame("Printable Music");
		myWindow.setSize(520,1020);
		myWindow.add(panel);
		myWindow.setVisible(true);

    	
    	
    }
	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() == button_safe)
		{
            record = false;
            if(mode.equals("prism"))
        	{
            	prism.safeForGood();
            	
        		
        	}
        	else if(mode.equals("cube"))
        	{
            	cube.safeForGood();
        	}
            
            button_start.setEnabled(false);
        	button_safe.setEnabled(false);
        	
        	button_cube.setEnabled(true);
        	button_prism.setEnabled(true);
        }
		else if(e.getSource() == button_cube)
		{
			mode = "cube";
			button_cube.setEnabled(false);
			button_prism.setEnabled(true);
			button_start.setEnabled(true);
		}
		else if(e.getSource() == button_prism)
		{
			mode = "prism";
			button_prism.setEnabled(false);
			button_cube.setEnabled(true);
			button_start.setEnabled(true);
		}
		else if(e.getSource() == button_start)
		{
			record = true;
			
			
			//refresh modells:
			prism = new ModelPrism("Piano");
			cube = new ModelCube("Piano");
			
			
			
			
			button_safe.setEnabled(true);
			button_start.setEnabled(false);
			button_cube.setEnabled(false);
			button_prism.setEnabled(false);
			
		}
		
		
		
		
	}
	
	
	public static BufferedImage resize(BufferedImage img, int newW, int newH) { 
	    Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
	    BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

	    Graphics2D g2d = dimg.createGraphics();
	    g2d.drawImage(tmp, 0, 0, null);
	    g2d.dispose();

	    return dimg;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
