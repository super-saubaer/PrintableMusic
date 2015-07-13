/*
 * Created on 03.06.2004
 *
 */
package com.groovemanager.app.mc909se;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.groovemanager.app.mc909se.Mc909SampleEditor.Properties;
import com.groovemanager.core.ConfigManager;
import com.groovemanager.exception.NotFinishedException;
import com.groovemanager.exception.NotReadyException;
import com.groovemanager.gui.custom.ProgressMonitor;
import com.groovemanager.gui.custom.ScaleNum;
import com.groovemanager.sampled.providers.RLNDChunk;
import com.groovemanager.sampled.providers.WAV909OutputStream;
import com.groovemanager.thread.ProgressThread;
import com.groovemanager.thread.SaveFileThread;

/**
 * This dialog is used for exporting samples from the Mc909 sample editor to
 * the MC909 or for importing them from the 909 to the editor.
 * @author Manu Robledo
 *
 */
public class Export909SamplesDialog extends TitleAreaDialog {
	/**
	 * Button for performing export
	 */
	Button createPatches;
	/**
	 * List of Mc909SampleEditor.Properties representing the currently opened
	 * samples
	 */
	ArrayList properties = new ArrayList();
	/**
	 * Text field for user input of the target path
	 */
	Text directory;
	/**
	 * List displaying the currently opened samples to be selected for export.
	 */
	List exportSamples;
	/**
	 * Table of existing samples on the Mc909. Samples marked for export will
	 * also be displayed here.
	 */
	Table existingSamples;
	/**
	 * ScaleNum for user selection of the next sample number used for export
	 */
	ScaleNum nextNum;
	/**
	 * The Mc909SampleEditor from whicht this dialog was opened
	 */
	final Mc909SampleEditor editor;
	/**
	 * The path for sample export (Path to the Mc909´s root folder)
	 */
	String exportPath = "";
	/**
	 * Radio button for user selection of "USB-Select"
	 */
	Button internal, card;
	/**
	 * Relative path from the Mc909´s root path to the sample directory
	 */
	final static String SMPL_FOLDER = "ROLAND" + File.separator + "SMPL";
	/**
	 * FileNameFilter to be used for filtering out potential Mc909 samples
	 */
	FilenameFilter fileNameFilter = new FilenameFilter() {
		public boolean accept(File f, String s) {
			try{
				Integer.parseInt(s.substring(4, 8));
			}
			catch(Exception ex){
				return false;
			}
			return s.length() == 12 && s.startsWith("smpl") &&
				(s.endsWith(".wav") || s.endsWith(".aif"));
		}
	};
	/**
	 * Create a new ExportSamplesDialog
	 * @param parentShell The shell to be used as parent shell for this dialog
	 * @param properties Array of all Mc909SampleEditor.Properties representing
	 * the currently opened files
	 * @param editor The Mc909SampleEditor from which this dialog was opened
	 */
	public Export909SamplesDialog(Shell parentShell, Mc909SampleEditor.Properties[] properties, Mc909SampleEditor editor) {
		super(parentShell);
		for (int i = 0; i < properties.length; i++) {
			this.properties.add(properties[i]);
		}
		this.editor = editor;
	}
	protected Control createDialogArea(Composite parent) {
		exportPath = ConfigManager.getDefault().getPrefStore().getString("mc909_sample_export");
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new FormLayout());
		FormData fd;
		
		// Destination
		Label l = new Label(comp, SWT.NONE);
		l.setText("Destination:");
		fd = new FormData();
		fd.left = fd.top = new FormAttachment(0, 10);
		fd.width = fd.height = -1;
		l.setLayoutData(fd);
		
		// Directory
		directory = new Text(comp, SWT.BORDER);
		fd = new FormData();
		fd.left = new FormAttachment(l, 0, SWT.LEFT);
		fd.top = new FormAttachment(l, 0, SWT.BOTTOM);
		fd.width = 400;
		fd.height = -1;
		directory.setLayoutData(fd);
		if(!exportPath.endsWith(File.separator)) exportPath += File.separator;
		directory.setText(exportPath);
		directory.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}
			public void focusLost(FocusEvent e) {
				String s = directory.getText();
				if(!s.endsWith(File.separator)) s+= File.separator;
				fillTable(s);
			}
		});
		
		// Choose
		Button choose = new Button(comp, SWT.PUSH);
		choose.setText("Choose...");
		fd = new FormData();
		fd.right = new FormAttachment(100, -10);
		fd.top = new FormAttachment(directory, 0, SWT.TOP);
		fd.bottom = new FormAttachment(directory, 0, SWT.BOTTOM);
		fd.left = new FormAttachment(directory, 0, SWT.RIGHT);
		choose.setLayoutData(fd);
		choose.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dirDialog = new DirectoryDialog(getShell(), SWT.SAVE | SWT.SINGLE);
				String path = dirDialog.open();
				if(path != null){
					if(!path.endsWith(File.separator)) path += File.separator;
					directory.setText(path);
					fillTable(path);
				}
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		Group usbSelect = new Group(comp, SWT.NONE);
		usbSelect.setText("USB Select");
		fd = new FormData();
		fd.left = new FormAttachment(directory, 0, SWT.LEFT);
		fd.top = new FormAttachment(directory, 10, SWT.BOTTOM);
		fd.right = new FormAttachment(choose, 0, SWT.RIGHT);
		usbSelect.setLayoutData(fd);
		usbSelect.setLayout(new GridLayout(2, true));
		
		internal = new Button(usbSelect, SWT.RADIO);
		internal.setText("Internal");
		
		card = new Button(usbSelect, SWT.RADIO);
		card.setText("Memory Card");
		card.setSelection(true);
		
		// Export samples
		exportSamples = new List(comp, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		for (Iterator iter = properties.iterator(); iter.hasNext();) {
			Properties p = (Properties) iter.next();
			exportSamples.add(p.nameText.getText());
		}
		fd = new FormData();
		fd.left = new FormAttachment(usbSelect, 0, SWT.LEFT);
		fd.top = new FormAttachment(usbSelect, 10, SWT.BOTTOM);
		fd.width = 200;
		fd.height = 150;
		exportSamples.setLayoutData(fd);
		
		// Existing samples
		existingSamples = new Table(comp, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		TableColumn col = new TableColumn(existingSamples, SWT.LEFT);
		col.setText("Nr");
		col.setWidth(30);
		col = new TableColumn(existingSamples, SWT.LEFT);
		col.setText("Name");
		col.setWidth(170);
		existingSamples.setHeaderVisible(true);
		fd = new FormData();
		fd.right = new FormAttachment(choose, 0, SWT.RIGHT);
		fd.top = new FormAttachment(exportSamples, 0, SWT.TOP);
		fd.bottom = new FormAttachment(exportSamples, 0, SWT.BOTTOM);
		fd.width = 200;
		existingSamples.setLayoutData(fd);
		existingSamples.setData("");
		
		// Transfer button
		Button transfer = new Button(comp, SWT.NONE);
		transfer.setText(">>");
		fd = new FormData();
		fd.left = new FormAttachment(exportSamples, 5, SWT.RIGHT);
		fd.right = new FormAttachment(existingSamples, -5, SWT.LEFT);
		fd.top = new FormAttachment(exportSamples, 0, SWT.TOP);
		transfer.setLayoutData(fd);
		transfer.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				transfer();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		// Transfer button
		Button importer = new Button(comp, SWT.NONE);
		importer.setText("<<");
		fd = new FormData();
		fd.left = new FormAttachment(transfer, 0, SWT.LEFT);
		fd.right = new FormAttachment(transfer, 0, SWT.RIGHT);
		fd.top = new FormAttachment(transfer, 10, SWT.BOTTOM);
		importer.setLayoutData(fd);
		importer.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				importSamples(existingSamples.getSelection());
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		// Next
		Label next = new Label(comp, SWT.NONE);
		next.setText("Next:");
		fd = new FormData();
		fd.left = new FormAttachment(importer, 0, SWT.LEFT);
		fd.right = new FormAttachment(importer, 0, SWT.RIGHT);
		fd.top = new FormAttachment(importer, 10, SWT.BOTTOM);
		next.setLayoutData(fd);
		
		// Next number
		nextNum = new ScaleNum(comp, SWT.NONE);
		nextNum.setAll(0, 0, 5000);
		fd = new FormData();
		fd.left = new FormAttachment(next, 0, SWT.LEFT);
		fd.right = new FormAttachment(next, 0, SWT.RIGHT);
		fd.top = new FormAttachment(next, 0, SWT.BOTTOM);
		nextNum.getComposite().setLayoutData(fd);
		
		// Update button
		Button update = new Button(comp, SWT.PUSH);
		update.setText("Update");
		fd = new FormData();
		fd.right = new FormAttachment(transfer, 0, SWT.RIGHT);
		fd.left = new FormAttachment(transfer, 0, SWT.LEFT);
		fd.bottom = new FormAttachment(existingSamples, 0, SWT.BOTTOM);
		fd.height = -1;
		update.setLayoutData(fd);
		update.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				fillTable(exportPath, true);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		setTitle("Export samples to MC909");
		setMessage("Please specify a destination folder.\nThen select the samples you want to export and add them at the position you want.");

		fillTable(exportPath, true);
		
		return comp;
	}
	/**
	 * Create a new Table Item and insert it into the existing samples table at
	 * the given index
	 * @param pos The index where to insert the Item
	 * @return The new created and inserted TableItem
	 */
	private TableItem insertItemAt(int pos){
		if(pos > existingSamples.getItemCount()) pos = existingSamples.getItemCount();
		int items = existingSamples.getItemCount() - pos;
		Color[] colors = new Color[items];
		Object[] datas = new Object[items];
		String[] names = new String[items];
		String[] nums = new String[items];
		for(int j = items - 1; j >= 0; j--){
			// Store existing
			TableItem ti = existingSamples.getItem(pos + j);
			colors[j] = ti.getForeground();
			datas[j] = ti.getData();
			names[j] = ti.getText(1); 
			nums[j] = ti.getText(0); 
			existingSamples.remove(pos + j);
		}
		TableItem item = new TableItem(existingSamples, SWT.NONE);
		for(int j = items - 1; j >= 0; j--){
			TableItem ti = new TableItem(existingSamples, SWT.NONE);
			ti.setForeground(colors[j]);
			ti.setData(datas[j]);
			ti.setText(0, nums[j]);
			ti.setText(1, names[j]);
		}
		return item;
	}
	/**
	 * Get the TableItem containing the sample with the given sample nr.
	 * @param nr The sample nr in question
	 * @return The TableItem containing the sample with the given sample nr.
	 * If no sample with the given number is found, <code>null</code> will be
	 * returned.
	 */
	private TableItem getItemForNumber(int nr){
		TableItem[] items = existingSamples.getItems();
		for (int i = 0; i < items.length; i++) {
			if(Integer.parseInt(items[i].getText(0)) == nr) return items[i];
		}
		return null;
	}
	/**
	 * Move the selected samples from the open file list to the existing samples
	 * table to indicate that they will be exported when completing this dialog.
	 *
	 */
	private void transfer(){
		int next = nextNum.getSelection();
		int[] indices = exportSamples.getSelectionIndices();
		for (int i = 0; i < indices.length; i++) {
			Properties p = (Properties)properties.get(indices[i]); 
			transferOne(p, next);
			next++;
			if(p.wt.getChannels() > 1) next++;
		}
		nextNum.setSelection(next);
	}
	/**
	 * If a collossion between existing samples and samples to be exported is
	 * detected, this method can be used to ask, if the existing sample should
	 * be overwritten or not
	 * @param num The sample number where the sample should be exported to
	 * @param samplename The name of the sample to be exported
	 * @param firstCollission The number and name of the first (and maybe only)
	 * sample the exported one collides with
	 * @param secondCollission In some cases a stereo file might collide with
	 * two samples at a time. In these cases, this parameter should contain the
	 * second colliding existing sample. Otherwise it may be <code>null</code>. 
	 * @param stereo true, if the exported sample is stereo, false otherwise.
	 * @return true, if the user accepted overwriting, false otherwise.
	 */
	private boolean askForOverwrite(int num, String samplename, String firstCollission, String secondCollission, boolean stereo){
		if(firstCollission == null && secondCollission == null) return true;
		if(firstCollission.equals(secondCollission)) secondCollission = null;
		String n = "" + num;
		if(stereo) n += "/" + (num + 1);
		String collission, pn;
		if(firstCollission == null || secondCollission == null){
			collission = "the sample " + firstCollission;
			pn = "it";
		}
		else{
			collission = "the samples \"" + firstCollission + "\"\nand " + secondCollission;
			pn = "them";
		}
		String message = "The new sample \"" + samplename + " [" + n + "]\" collides with " + collission + ".\nDo you want to overwrite " + pn + "?";
		MessageBox overwrite = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		overwrite.setText("Confirm overwrite");
		overwrite.setMessage(message);
		return overwrite.open() == SWT.YES;

	}
	/**
	 * Remove the given TableItem from the existing samples table
	 * @param item The TableItem to remove
	 */
	private void deleteItem(TableItem item){
		if(item == null) return;
		if(item.getData() instanceof TableItem) deleteItem((TableItem)item.getData());
		
		TableItem[] items = existingSamples.getItems();
		for (int i = 0; i < items.length; i++) {
			if(item == items[i]){
				existingSamples.remove(i);
				return;
			}
		}
	}
	/**
	 * Find out, with which sample(s) the given sample number collides and try
	 * ask the user, if they should be overwritten.
	 * @param num The sample number in question
	 * @param stereo true, if the sample to export is stereo, false otherwise
	 * @param sampleName The name of the sample to export
	 * @return true, if the user accepted to overwrite, false otherwise
	 */
	private boolean tryToOverwrite(int num, boolean stereo, String sampleName){
		TableItem item = getItemForNumber(num);
		TableItem item2 = null;
		if(stereo) item2 = getItemForNumber(num + 1);
		String name = null, name2 = null;
		if(item != null){
			// If this is the right channel of a stereo sample,
			// continue with the left data
			if(item.getData() instanceof TableItem) item = (TableItem)item.getData();
			name = getItemName(item);
		}
		if(item2 != null){
			// If this is the right channel of a stereo sample,
			// continue with the left data
			if(item2.getData() instanceof TableItem) item2 = (TableItem)item2.getData();
			name2 = getItemName(item2);
		}
		if(askForOverwrite(num, sampleName, name, name2, stereo)){
			if(item != null) deleteItem(item);
			if(item2 != null) deleteItem(item2);
			return true;
		}
		else return false;
	}
	/**
	 * Get the index of the TableItem containing the given sample number
	 * @param num The sample number to get the index for.
	 * @return The index of the TableItem containing the sample with the given
	 * number or - if no TableItem was found - the index the next created
	 * TableItem will have.
	 */
	private int getIndexForNum(int num){
		TableItem[] items = existingSamples.getItems();
		for (int i = 0; i < items.length; i++) {
			if(Integer.parseInt(items[i].getText(0)) >= num) return i;
		}
		return items.length;
	}
	/**
	 * Get a String describing the sample represented by the given TableItem
	 * @param item The TableItem containing the sample
	 * @return A text containing the sample´s name and the sample number(s).
	 */
	private String getItemName(TableItem item){
		if(item == null) return null;
		Object o = item.getData();
		String num, s;
		int channels;
		// If this is a right channel object, switch to the left one.
		if(o instanceof TableItem){
			item = (TableItem)o; 
			o = item.getData();
		}
		if(o instanceof Properties){
			Properties p = (Properties)o; 
			s = p.nameText.getText();
			channels = p.wt.getChannels();
		}
		else if(o instanceof Mc909SampleInfo){
			Mc909SampleInfo info = (Mc909SampleInfo)o;
			s = info.name;
			channels = info.channels;
		}
		else return null;
		
		num = item.getText(0);
		s += "[" + num;
		if(channels > 1) s += "/" + (Integer.parseInt(num) + 1);
		s += "]";
		return s;
	}
	/**
	 * Move a single sample from the opened files list to the table of samples
	 * to be exported
	 * @param p The Properties object representing the opened sample
	 * @param desiredNum The number, where to export the sample to
	 */
	private void transferOne(Properties p, int desiredNum){
		// Do we need one or two items in the table?
		boolean stereo = p.wt.getChannels() > 1;
		
		// Are the items we need already there?
		// If they exist, we have to ask for overwriting
		if(tryToOverwrite(desiredNum, stereo, p.nameText.getText())){
			// Create the item(s) if needed
			TableItem item = insertItemAt(getIndexForNum(desiredNum));
			item.setData(p);
			item.setForeground(item.getDisplay().getSystemColor(SWT.COLOR_RED));
			item.setText(0, "" + desiredNum);
			item.setText(1, p.nameText.getText());
			existingSamples.showItem(item);
			if(stereo){
				item.setText(1, fillName(p.nameText.getText()) + " L");
				TableItem item2 = insertItemAt(getIndexForNum(desiredNum + 1));
				item2.setData(item);
				item2.setForeground(item.getDisplay().getSystemColor(SWT.COLOR_GRAY));
				item2.setText(0, "" + (desiredNum + 1));
				item2.setText(1, fillName(p.nameText.getText()) + " R");
				existingSamples.showItem(item2);
			}
		}
	}
	/**
	 * Fill the existing samples table if and only if the given path is other
	 * than the one used the last time for filling the table.
	 * @param path The Mc909´s root path
	 */
	private void fillTable(String path){
		fillTable(path, false);		
	}
	/**
	 * Fill the existing samples table. The <code>force</code> parameter
	 * specifies, whether the table should be updated even if the path didn´t
	 * change.
	 * @param path The Mc909´s root path
	 * @param force true, if the table´s content should be updated even if the
	 * path didn´t change, false otherwise
	 */
	private void fillTable(String path, boolean force){
		if(path == null || existingSamples == null) return;
		// If the path didn't change, return.
		if((!force) && exportPath.equals(path)) return;
		
		// First empty the table
		existingSamples.removeAll();
		
		if(!path.endsWith(File.separator)) path += File.separator;
		final String p = path;
		// Search the directoy for Mc909 samples
		final File f = new File(path + SMPL_FOLDER);
		if(f.isDirectory()){
			Runnable r = new Runnable(){
				public void run(){
					exportPath = p;
					File[] files = f.listFiles(fileNameFilter);
					int smplnr = 0;
					int maxnr = 0;
					for (int i = 0; i < files.length; i++) {
						try {
							Mc909SampleInfo info = new Mc909SampleInfo(files[i]);
							smplnr = Integer.parseInt(files[i].getName().substring(4, 8));
							TableItem ti = insertItemAt(getIndexForNum(smplnr));
							ti.setForeground(ti.getDisplay().getSystemColor(SWT.COLOR_BLACK));
							ti.setData(info);
							ti.setText(0, ""+smplnr);
							ti.setText(1, fillName(info.name));
							if(info.channels > 1){
								ti.setText(1, fillName(info.name) + " L");
								TableItem ti2 = insertItemAt(getIndexForNum(++smplnr));
								ti2.setForeground(ti.getDisplay().getSystemColor(SWT.COLOR_GRAY));
								ti2.setData(ti);
								ti2.setText(0, ""+smplnr);
								ti2.setText(1, fillName(info.name) + " R");
							}
							maxnr = Math.max(maxnr, smplnr);
						} catch (UnsupportedAudioFileException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
					nextNum.setSelection(maxnr + 1);
				}
			};
			BusyIndicator.showWhile(getShell().getDisplay(), r);
		}
		directory.setText(exportPath);
		ConfigManager.getDefault().getPrefStore().setValue("mc909_sample_export", exportPath);
	}
	/**
	 * Try to export all samples that were marked for export.
	 * @return The number of samples succesfully exported.
	 */
	private int export() {
		TableItem[] items = existingSamples.getItems();
		int exported = 0;
		ArrayList toExport = new ArrayList();
		
		for (int i = 0; i < items.length; i++) {
			if((!items[i].isDisposed()) && items[i].getData() instanceof Properties)
				try {
					String path = exportPath;
					if(!path.endsWith(File.separator)) path += File.separator;
					path += SMPL_FOLDER;
					File f = export((Properties)items[i].getData(), Integer.parseInt(items[i].getText(0)), path);
					items[i].setData(new Mc909SampleInfo(f));
					exported ++;
					toExport.add(items[i]);
					items[i].setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
				} catch (NumberFormatException e) {
					e.printStackTrace();
					errorMessage(e.getMessage());
				} catch (IOException e) {
					e.printStackTrace();
					errorMessage(e.getMessage());
				} catch (NotReadyException e) {
					e.printStackTrace();
					errorMessage(e.getMessage());
				} catch (NotFinishedException e) {
					e.printStackTrace();
					errorMessage(e.getMessage());
				} catch (UnsupportedAudioFileException e) {
					e.printStackTrace();
					errorMessage(e.getMessage());
				}
		}
		for (Iterator iter = toExport.iterator(); iter.hasNext();) {
			TableItem item = (TableItem) iter.next();
			Mc909SampleInfo info = (Mc909SampleInfo)item.getData();
			WizardDialog dialog = new CreatePatchDialog(getShell(), item.getText(1), internal.getSelection(), Integer.parseInt(item.getText(0)), info.channels > 1);
			dialog.open();
		}
		return exported;
	}
	/**
	 * Export one sample
	 * @param prop The Properties object representing the file to export
	 * @param sampleNum The sample number to export to
	 * @param path The Mc909´s root path to use for export
	 * @return The file to ehich the sample was exported.
	 * @throws IOException If an I/O error occurs
	 * @throws NotReadyException If the sample could not be read
	 * @throws NotFinishedException If the sample export operation could not be
	 * finished
	 */
	private File export(Properties prop, int sampleNum, String path) throws IOException, NotReadyException, NotFinishedException{
		// Filename
		String s = "" + sampleNum;
		while(s.length() < 4) s = "0" + s;
		if(!path.endsWith(File.separator)) path += File.separator;
		s = path + "smpl" + s + ".wav";
		File f = new File(s);
		
		AudioInputStream in = prop.wt.getWholeAudioInputStream();
		AudioFormat format;
		int channels = 1;
		if(prop.wt.getChannels() > 1) channels++;
		format = new AudioFormat(44100, 16, channels, true, false);
		HashMap props = new HashMap();
		RLNDChunk chunk = new RLNDChunk();
		props.put("RLND", chunk);
		
		chunk.setSampleName(prop.nameText.getText());
		chunk.setBPM(prop.bpm);
		chunk.setTimeStretchType((short)prop.timeStretchType.getSelectionIndex());
		chunk.setLoopMode((byte)prop.loopMode.getSelectionIndex());
		chunk.setRootKey((byte)60);
		chunk.setSampleEnd(prop.wt.getTotalLength());
		chunk.setSampleLength(prop.wt.getTotalLength());
		
		WAV909OutputStream out = new WAV909OutputStream(f, format, props);
		if(!in.getFormat().equals(out.getFormat())){
			in = AudioSystem.getAudioInputStream(out.getFormat(), in);
		}
		
		ProgressThread saver = new SaveFileThread(in, out, (int) in.getFrameLength(), in.getFormat().getFrameSize(), true);
		ProgressMonitor monitor = new ProgressMonitor(getShell(), saver, "Writing " + f.getName(), "Exporting Sample " + prop.nameText.getText() + " to " + sampleNum + "...");
		
		monitor.start();
		
		return f;
	}
	/**
	 * Display an error message to the user
	 * @param m The message to display
	 */
	public void errorMessage(String m){
		MessageDialog d = new MessageDialog(getShell(), "Error", null, m, SWT.ICON_ERROR, new String[]{"OK"}, 0);
		d.open();
	}
	protected void okPressed() {
		int exported = export();
		System.out.println(exported);
		MessageDialog d = new MessageDialog(getShell(), "Export finished", null, "Exported " + exported + " Samples.", SWT.ICON_INFORMATION, new String[]{"OK"}, 0);
		d.open();
	}
	protected void createButtonsForButtonBar(Composite parent) {
		((GridLayout) parent.getLayout()).numColumns++;
		createPatches = new Button(parent, SWT.CHECK);
		createPatches.setText("Create Patches");
		createPatches.setSelection(true);
		createButton(parent, IDialogConstants.OK_ID, "Export", false);
		createButton(parent, IDialogConstants.CANCEL_ID, "Close", false);
	}
	public boolean close() {
		try {
			ConfigManager.getDefault().getPrefStore().save();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return super.close();
	}
	/**
	 * Get a String of length 16 out of the given String.
	 * @param name The original String
	 * @return A String of length 16 that gets its content from the given
	 * String
	 */
	private static String fillName(String name){
		if(name.length() == 16) return name;
		if(name.length() > 16) return name.substring(0, 16);
		
		while(name.length() < 16){
			name += " ";
		}
		
		return name;
	}
	/**
	 * Import the samples represented by the given TableItems from the Mc909 to
	 * the editor
	 * @param items The TableItems selected for import
	 */
	private void importSamples(TableItem[] items){
		for (int i = 0; i < items.length; i++) {
			TableItem item = items[i];
			if(!item.isDisposed()){
				if(item.getData() instanceof TableItem) item = (TableItem)item.getData();
				if(item.getData() instanceof Mc909SampleInfo){
					try {
						editor.importFile(((Mc909SampleInfo)item.getData()).file);
						Properties p = editor.getActiveProperties();
						if(!properties.contains(p)){
							properties.add(p);
							exportSamples.add(p.nameText.getText());
						}
					} catch (IOException e) {
						e.printStackTrace();
						errorMessage("Unable to import sample " + ((Mc909SampleInfo)item.getData()).name + "\n" + e.getMessage());
					} catch (NotFinishedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}