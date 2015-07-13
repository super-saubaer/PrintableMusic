/*
 * Created on 25.05.2004
 *
 */
package com.groovemanager.app.sse;

import com.groovemanager.core.FileManager;

/**
 * Starter class for the SampleEditor
 * @author Manu Robledo
 *
 */
public class Starter {
	public static void main(String[] args){
		// Init Classpath and extension dir
		FileManager.getDefault();
		final SimpleSampleEditor s = new SimpleSampleEditor();
		s.setBlockOnOpen(true);
		/*
		new Thread(){
			public void run(){
				while(s.getShell() == null) try{
					Thread.sleep(1000);
				}
				catch(InterruptedException e){}
				s.getShell().getDisplay().syncExec(new Runnable(){
					public void run(){
						//s.fileOpened(new File("c:\\dokumente und einstellungen\\manu\\desktop\\zeit.wav"));
						s.fileOpened(new File("c:\\dokumente und einstellungen\\manu\\desktop\\testsounds\\testsignal.wav"));
						//s.getActiveTab().waveDisplay.addMarker(10000, "test 1");
					}
				});
			}
		}.start();
		*/
		s.open();
	}
}