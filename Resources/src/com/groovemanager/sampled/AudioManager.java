package com.groovemanager.sampled;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import sun.misc.Service;

import com.groovemanager.core.ConfigManager;
import com.groovemanager.sampled.providers.AudioFileFormatProvider;
import com.groovemanager.sampled.providers.AudioFileOutputStream;
import com.groovemanager.sampled.providers.AudioFileOutputStreamProvider;

/**
 * This class can act as a central unit for using AudioRessources of different
 * kinds. It provides some static methods as well as a default implementation,
 * but it can also be subclassed. An AudioManager gets its settings for in
 * and out device (Mixer) as well as the buffer size to use from a
 * ConfigManager to which it is connected
 * @author Manu Robledo
 *
 */
public class AudioManager implements IPropertyChangeListener {
	/**
	 * The input Mixer selected by the user or by the application
	 */
	protected Mixer inMixer,
	/**
	 * The output Mixer selected by the user or by the application
	 */
	outMixer;
	/**
	 * The ConfigManager used by this instance.
	 */
	protected ConfigManager configManager;
	/**
	 * The default AudioManager instance.
	 */
	public final static AudioManager DEFAULT_AUDIO_MANAGER = createDefault();
	/**
	 * List of known AudioFileOutputStreamProviders that will be queried for
	 * an AudioFileOutputStream when requested
	 */
	ArrayList outputStreamProviders = new ArrayList();
	/**
	 * List of known AudioFileFormatProviders that will be queried for an 
	 * AudioFileFormat when requested
	 */
	ArrayList fileFormatProviders = new ArrayList();
	
	/**
	 * Constructs a new Audiomanager using the given ConfigManager
	 * @param configManager The ConfigManager to be used.
	 */
	public AudioManager(ConfigManager configManager){
		this.configManager = configManager;
		configManager.getPrefStore().addPropertyChangeListener(this);
	}
	/**
	 * Constructs a new Audiomanager using the default ConfigManager
	 */
	protected AudioManager(){
		this(ConfigManager.getDefault());
	}
	
	/**
	 * Get infos about all installed Mixers that are capable of
	 * recording Audio data via a TargetDataLine
	 * @return An array of Mixer.Info Objects describing the found Mixers.
	 */
	public static Mixer.Info[] getInputMixers(){
		ArrayList list = new ArrayList();
		Mixer.Info[] infos = AudioSystem.getMixerInfo();
		for (int i = 0; i < infos.length; i++) {
			Line.Info[] targetInfos = AudioSystem.getMixer(infos[i]).getTargetLineInfo();
			for (int j = 0; j < targetInfos.length; j++) {
				if(TargetDataLine.class.isAssignableFrom(targetInfos[j].getLineClass()) && !list.contains(infos[i])) list.add(infos[i]);
			}
		}
		
		infos = new Mixer.Info[list.size()];
		int i = 0;
		for (Iterator iter = list.iterator(); iter.hasNext(); i++) {
			infos[i] = (Mixer.Info) iter.next();
		}
		
		return infos;
	}
	/**
	 * Get infos about all installed Mixers that are capable of
	 * playing back Audio data via a SourceDataLine
	 * @return An array of Mixer.Info Objects describing the found Mixers.
	 */
	public static Mixer.Info[] getOutputMixers(){
		ArrayList list = new ArrayList();
		Mixer.Info[] infos = AudioSystem.getMixerInfo();
		for (int i = 0; i < infos.length; i++) {
			Line.Info[] sourceInfos = AudioSystem.getMixer(infos[i]).getSourceLineInfo();
			for (int j = 0; j < sourceInfos.length; j++) {
				if(SourceDataLine.class.isAssignableFrom(sourceInfos[j].getLineClass()) && !list.contains(infos[i])) list.add(infos[i]);
			}
		}
		
		infos = new Mixer.Info[list.size()];
		int i = 0;
		for (Iterator iter = list.iterator(); iter.hasNext(); i++) {
			infos[i] = (Mixer.Info) iter.next();
		}
		
		return infos;
	}
	
	/**
	 * Get the Input Mixer which is selected in the "audio_in" property
	 * of the given ConfigManager
	 * @return The selected input Mixer
	 */
	public Mixer getInMixer(){
		if(inMixer != null) return inMixer;
		
		String mixername = configManager.getPrefStore().getString("audio_in");
		
		Mixer.Info[] infos = getInputMixers();
		Mixer.Info info = null;
		
		// This is a little bit complicated because we have to distinguish
		// different Mixers even if they have the same name.
		for(int i = 0; i < infos.length; i++){
			String tempname = infos[i].getVendor() + infos[i].getName() + infos[i].getVersion();
			if(mixername.startsWith(tempname) && info == null) info = infos[i];
			else if(mixername.equals(tempname + i)) info = infos[i];
		}
		
		// This really shouldn't happen... only if no Mixers are intalled on
		// the system at all
		if(infos.length == 0) return null;
		else if(info == null) return AudioSystem.getMixer(infos[0]);
		else{
			inMixer = AudioSystem.getMixer(info);
			return inMixer;
		}
	}
	
	/**
	 * Get the Output Mixer which is selected in the "audio_out" property
	 * of the given ConfigManager
	 * @return The selected output Mixer
	 */
	public Mixer getOutMixer(){
		if(outMixer != null) return outMixer;
		
		String mixername = configManager.getPrefStore().getString("audio_out");
		
		Mixer.Info[] infos = getOutputMixers();
		Mixer.Info info = null;
		
		// This is a little bit complicated because we have to distinguish
		// different Mixers even if they have the same name.
		for(int i = 0; i < infos.length; i++){
			String tempname = infos[i].getVendor() + infos[i].getName() + infos[i].getVersion();
			if(mixername.startsWith(tempname) && info == null) info = infos[i];
			else if(mixername.equals(tempname + i)) info = infos[i];
		}
		
		// This really shouldn't happen... only if no Mixers are intalled on
		// the system at all
		if(infos.length == 0) return null;
		else if(info == null) return AudioSystem.getMixer(infos[0]);
		else{
			outMixer = AudioSystem.getMixer(info);
			return outMixer;
		}
	}
	
	/**
	 * 
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if(event.getProperty().equals("audio_in")) inMixer = null;
		else if(event.getProperty().equals("audio_out")) outMixer = null;
	}
	/**
	 * Get the default Audio Manager.
	 * @return The default Audio Manager. In fact it just returns 
	 * <code>DEFAULT_AUDIO_MANAGER</code>
	 */
	public static AudioManager getDefault(){
		return DEFAULT_AUDIO_MANAGER;
	}
	/**
	 * Get a numeric value for the equalness of two AudioFormat objects.
	 * The number of channels must be the same and conversion from source
	 * to target must be supported, otherwise 0 will be returned.
	 * @param source The source Format
	 * @param target The target Format
	 * @return A number between 0 (no match) and 100 (total match)
	 */
	public static int getRelation(AudioFormat source, AudioFormat target){
		int rel = 0;
		
		// Equalness is the best match
		if(source.equals(target)) return 100;
		
		// Channels must match!!
		if(source.getChannels() != target.getChannels() && source.getChannels() != AudioSystem.NOT_SPECIFIED && target.getChannels() != AudioSystem.NOT_SPECIFIED) return 0;
		
		// Encoding conversion must be supported
		if(!AudioSystem.isConversionSupported(target.getEncoding(), source)) return 0;
		
		// What says matches()? (40) points
		if(target.matches(source)) rel += 40;
		// Or at least conversion supported? (30 points)
		else if(AudioSystem.isConversionSupported(target, source)) rel += 30;
		
		// Samplig Rate: 20 points
		if(source.getSampleRate() == target.getSampleRate() || source.getSampleRate() == AudioSystem.NOT_SPECIFIED || target.getSampleRate() == AudioSystem.NOT_SPECIFIED) rel += 20;

		// Encoding: 12 points
		if(source.getEncoding().equals(target.getEncoding())) rel += 12;
		
		// Sample size: 6 points
		if(source.getSampleSizeInBits() == target.getSampleSizeInBits()) rel += 6;
		else rel -= Math.abs(target.getSampleSizeInBits() - source.getSampleSizeInBits()) / 8;
		
		// Frame rate: 3 points
		if(source.getFrameRate() == target.getFrameRate()) rel += 3;

		// Endianess: 2 points
		if(source.isBigEndian() == target.isBigEndian()) rel += 2;

		// Frame size: 1 point
		if(source.getFrameSize() == target.getFrameSize()) rel += 1;
		
		return rel;
	}
	/**
	 * Add an AudioFileOutputStreamProvider to the list of known
	 * AudioOutputStreamProviders
	 * @param provider The AudioFileOutputStreamProvider to add
	 */
	public void registerAudioFileOutputStreamProvider(AudioFileOutputStreamProvider provider){
		outputStreamProviders.add(provider);
	}
	/**
	 * Remove an AudioFileOutputStreamProvider from the list of known
	 * AudioOutputStreamProviders
	 * @param provider The AudioFileOutputStreamProvider to remove
	 */
	public void unregisterAudioFileOutputStreamProvider(AudioFileOutputStreamProvider provider){
		outputStreamProviders.remove(provider);
	}
	/**
	 * Add an AudioFileFormatProvider to the list of known
	 * AudioFileFormatProviders
	 * @param provider The AudioFileFormatProvider to add
	 */
	public void registerAudioFileFormatProvider(AudioFileFormatProvider provider){
		fileFormatProviders.add(provider);
	}
	/**
	 * Remove an AudioFileFormatProvider from the list of known
	 * AudioFileFormatProviders
	 * @param provider The AudioFileFormatProvider to remove
	 */
	public void unregisterAudioFileFormatProvider(AudioFileFormatProvider provider){
		fileFormatProviders.remove(provider);
	}
	/**
	 * Get the AudioFileFormat out of a file. The default implementation will
	 * first look, if a known provider can provide the required properties from
	 * the specified file. Then if more than one provider is left, the remaining
	 * providers are asked about the desired properties. The one that can
	 * provide the most of them will be asked to provide the AudiofFileFormat.
	 * If no properties are required and no provider can be found,
	 * <code>AudioSystem.getAudioFileFormat()</code> will be used.
	 * Subclasses may provide a different behaviour for getting the
	 * AudioFileFormat, but it should be documented.
	 * @param f The Audio file to get the format from
	 * @param requiredProperties Array of property keys that the provider must
	 * understand. May also be <code>null</code> to indicate that no properties
	 * are required.
	 * @param desiredProperties Array of property keys that would be nice to
	 * have, but are not absolutely necessary. May also be <code>null</code> to
	 * indicate that no optional properties are desired.
	 * @throws IOException If an I/O Error occurs during file reading
	 * @throws UnsupportedAudioFileException If either the file format of the
	 * file can not be determined or no provider knowing all required properties
	 * could be found.
	 */
	public AudioFileFormat getAudioFileFormat(File f, String[] requiredProperties, String[] desiredProperties) throws UnsupportedAudioFileException, IOException{
		ArrayList providers = new ArrayList();
		providers.addAll(fileFormatProviders);
		ArrayList temp = new ArrayList(providers.size());
		
		// Check for file support
		for (Iterator iter = providers.iterator(); iter.hasNext();) {
			AudioFileFormatProvider provider = (AudioFileFormatProvider) iter.next();
			if(provider.isFileSupported(f)) temp.add(provider);
		}
		
		providers.clear();
		providers.addAll(temp);
		temp.clear();
		
		// Check for required properties
		if(requiredProperties != null && requiredProperties.length > 0){
			for (Iterator iter = providers.iterator(); iter.hasNext();) {
				AudioFileFormatProvider provider = (AudioFileFormatProvider) iter.next();
				boolean supported = true;
				for (int i = 0; supported && i < requiredProperties.length; i++) {
					if(!provider.isPropertySupported(requiredProperties[i])){
						supported = false;
					}
				}
				if(supported) temp.add(provider);
			}
			if(temp.size() == 0) throw new UnsupportedAudioFileException("Not all required properties are supported.");
			providers.clear();
			providers.addAll(temp);
			temp.clear();
		}
		
		// Check for desired properties
		AudioFileFormatProvider bestProvider = null;
		int maxSupported = -1;
		for (Iterator iter = providers.iterator(); iter.hasNext();) {
			AudioFileFormatProvider provider = (AudioFileFormatProvider) iter.next();
			int supported = 0;
			if(desiredProperties != null) for (int i = 0; i < desiredProperties.length; i++) {
				if(provider.isPropertySupported(desiredProperties[i])) supported++;
			}
			if(supported > maxSupported){
				maxSupported = supported;
				bestProvider = provider;
			}
		}
		
		if(bestProvider != null) return bestProvider.getAudioFileFormat(f);
		else return AudioSystem.getAudioFileFormat(f);
	}
	/**
	 * Get an AudioFileOutputStream for writing the desired Audio data to the
	 * given file. The default implementation will first search for a provider
	 * being able to write a file of the specified type in the specified format
	 * with the required properties. Then if more than one provider is left,
	 * the remaining providers are asked about the desired properties. The one
	 * that can provide the most of them will be asked to provide the
	 * AudioFileOutputStream.
	 * Subclasses may provide a different behaviour for getting the
	 * AudioFileFormat, but it should be documented.
	 * @throws IOException If an I/O Error occurs during file reading
	 * @throws UnsupportedAudioFileException If either the file format of the
	 * file can not be determined or no provider knowing all required properties
	 * could be found.
	 * 
	 * @param f The file to write the audio data to
	 * @param format The format in which the audio data should be written
	 * @param type The type of the audio file
	 * @param properties Map of properties of the source data
	 * @param requiredProperties Array of property keys that must be supported
	 * for writing
	 * @param desiredProperties Array of property keys that are nice to have but
	 * don´t need to be supported.
	 * @return An AudioFileOutputStream to which the data can be written. When
	 * this stream is being closed, the specified properties will have been
	 * written to the file too.
	 * @throws UnsupportedAudioFileException If no provider can be found
	 * supporting the file type, the format and the required properties 
	 * @throws IOException If an I/O Error occurs during OutputStream creation
	 */
	public AudioFileOutputStream getAudioFileOutputStream(File f, AudioFormat format, AudioFileFormat.Type type, Map properties, String[] requiredProperties, String[] desiredProperties) throws UnsupportedAudioFileException, IOException{
		ArrayList providers = new ArrayList();
		ArrayList temp = new ArrayList();
		providers.addAll(outputStreamProviders);
		
		// Check for type
		for (Iterator iter = providers.iterator(); iter.hasNext();) {
			AudioFileOutputStreamProvider provider = (AudioFileOutputStreamProvider) iter.next();
			if(provider.isTypeSupported(type)) temp.add(provider);
		}
		if(temp.size() == 0) throw new UnsupportedAudioFileException("Type " + type + " not supported.");
		
		providers.clear();
		providers.addAll(temp);
		temp.clear();
		
		// Check for format
		for (Iterator iter = providers.iterator(); iter.hasNext();) {
			AudioFileOutputStreamProvider provider = (AudioFileOutputStreamProvider) iter.next();
			if(provider.isFormatSupported(format)) temp.add(provider);
		}
		if(temp.size() == 0) throw new UnsupportedAudioFileException("Format " + format + " not supported.");
		
		providers.clear();
		providers.addAll(temp);
		temp.clear();

		// Check for required properties
		if(requiredProperties != null){
			for (Iterator iter = providers.iterator(); iter.hasNext();) {
				AudioFileOutputStreamProvider provider = (AudioFileOutputStreamProvider) iter.next();
				boolean supported = true;
				for (int i = 0; supported && i < requiredProperties.length; i++) {
					if(!provider.isPropertySupported(requiredProperties[i])){
						supported = false;
					}
				}
				if(supported) temp.add(provider);
			}
			if(temp.size() == 0) throw new UnsupportedAudioFileException("Not all required properties are supported.");
			providers.clear();
			providers.addAll(temp);
			temp.clear();
		}

		// Check for desired properties
		AudioFileOutputStreamProvider bestProvider = null;
		int maxSupported = -1;
		for (Iterator iter = providers.iterator(); iter.hasNext();) {
			AudioFileOutputStreamProvider provider = (AudioFileOutputStreamProvider) iter.next();
			int supported = 0;
			if(desiredProperties != null) for (int i = 0; i < desiredProperties.length; i++) {
				if(provider.isPropertySupported(desiredProperties[i])) supported++;
			}
			if(supported > maxSupported){
				maxSupported = supported;
				bestProvider = provider;
			}
		}
		if(bestProvider == null) throw new UnsupportedAudioFileException("Could not write file");
		return bestProvider.getAudioFileOutputStream(f, type, format, properties);
	}
	/**
	 * Helper method for getting the properties out of an AudioFormat or
	 * AudioFileFormat object, if we cannot access it directly because of
	 * compatibility to JDK 1.4
	 * @param o The AudioFormat or AudioFileFormat object to query for its
	 * properties.
	 * @return The map of properties returned by the object´s
	 * <code>properties()</code> method or <code>null</code>
	 */
	public static Map getProperties(Object o){
		Method m = null;
		Method[] methods = o.getClass().getMethods();
		for (int i = 0; i < methods.length; i++) {
			if(methods[i].getName().equals("properties") && methods[i].getParameterTypes().length == 0) m = methods[i];
		}
		if(m == null) return null;
		Object map;
		try {
			map = m.invoke(o, new Object[]{});
		} catch (IllegalArgumentException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		} catch (InvocationTargetException e) {
			return null;
		}
		if(map instanceof Map) return (Map)map;
		else return null;
	}
	/**
	 * Find out if a provider exists that can write files in the given 
	 * AudioFileFormat
	 * @param format The deired AudioFileFormat in which the provider should be
	 * able to write files
	 * @return true, if such aprovider could be found, false otherwise
	 */
	public boolean canWrite(AudioFileFormat format){
		for (Iterator iter = outputStreamProviders.iterator(); iter.hasNext();) {
			AudioFileOutputStreamProvider provider = (AudioFileOutputStreamProvider) iter.next();
			if(provider.isFormatSupported(format.getFormat()) && provider.isTypeSupported(format.getType())) return true;
		}
		return false;
	}
	/**
	 * Get a mono AudioInputStream out of the specified one´s left channel.
	 * If the given AudioInputStream is already mono, it will be returned
	 * itself. Otherwise an AudioInputStream reading from its left channel
	 * will be returned.
	 * @param in The AudioInputStream to convert to mono
	 * @return A mono AudioInputStream
	 * @throws IllegalArgumentException If the given AudioInputStream has more
	 * than 2 channels 
	 */
	public static AudioInputStream getMonoInputStream(AudioInputStream in){
		switch (in.getFormat().getChannels()) {
			case 1 :
				return in;
			case 2 :
				AudioFormat f = in.getFormat();
				return new AudioInputStream(new StereoToMonoStream(in), new AudioFormat(f.getEncoding(), f.getSampleRate(), f.getSampleSizeInBits(), 1, f.getFrameSize() / 2, f.getFrameRate(), f.isBigEndian()), in.getFrameLength());
			default :
				throw new IllegalArgumentException("Conversion from " + in.getFormat().getChannels() + " channels to Mono not supported.");
		}
	}
	/**
	 * Get a stereo AudioInputStream out of the specified one.
	 * If the given AudioInputStream is already stereo, it will be returned
	 * itself. Otherwise an AudioInputStream reading from using its mono channel
	 * for its left as well as for its right channel will be returned.
	 * @param in The AudioInputStream to convert to stereo
	 * @return A stereo AudioInputStream
	 * @throws IllegalArgumentException If the given AudioInputStream has more
	 * than 2 channels 
	 */
	public static AudioInputStream getStereoInputStream(AudioInputStream in){
		switch (in.getFormat().getChannels()) {
			case 2 :
				return in;
			case 1 :
				AudioFormat f = in.getFormat();
				return new AudioInputStream(new MonoToStereoStream(in), new AudioFormat(f.getEncoding(), f.getSampleRate(), f.getSampleSizeInBits(), 2, f.getFrameSize() * 2, f.getFrameRate(), f.isBigEndian()), in.getFrameLength());
			default :
				throw new IllegalArgumentException("Conversion from " + in.getFormat().getChannels() + " channels to Stereo not supported.");
		}
	}
	/**
	 * Get all AudioFileOutputStreamProviders currently registered to this
	 * AudioManager
	 * @return An array of all AudioFileOutputStreamProviders currently
	 * registered to this AudioManager
	 */
	public AudioFileOutputStreamProvider[] getRegisteredOutputStreamProviders(){
		AudioFileOutputStreamProvider[] providers = new AudioFileOutputStreamProvider[outputStreamProviders.size()];
		
		int i = 0;
		for (Iterator iter = outputStreamProviders.iterator(); iter.hasNext(); i++) {
			AudioFileOutputStreamProvider provider = (AudioFileOutputStreamProvider) iter.next();
			providers[i] = provider;
		}
		
		return providers;
	}
	/**
	 * Get all AudioFileFormatProviders currently registered to this
	 * AudioManager
	 * @return An array of all AudioFileFormatProviders currently
	 * registered to this AudioManager
	 */
	public AudioFileFormatProvider[] getRegisteredFileFormatProviders(){
		AudioFileFormatProvider[] providers = new AudioFileFormatProvider[outputStreamProviders.size()];
		
		int i = 0;
		for (Iterator iter = fileFormatProviders.iterator(); iter.hasNext(); i++) {
			AudioFileFormatProvider provider = (AudioFileFormatProvider) iter.next();
			providers[i] = provider;
		}
		
		return providers;
	}
	/**
	 * Create the default AudioManager instance
	 * @return The default AudioManager instance
	 */
	private static AudioManager createDefault(){
		AudioManager def = new AudioManager();
		
		List formatProviders = def.getProviders(AudioFileFormatProvider.class);
		for (Iterator iter = formatProviders.iterator(); iter.hasNext();) {
			AudioFileFormatProvider provider = (AudioFileFormatProvider) iter.next();
			def.registerAudioFileFormatProvider(provider);
		}

		List outputProviders = def.getProviders(AudioFileOutputStreamProvider.class);
		for (Iterator iter = outputProviders.iterator(); iter.hasNext();) {
			AudioFileOutputStreamProvider provider = (AudioFileOutputStreamProvider) iter.next();
			def.registerAudioFileOutputStreamProvider(provider);
		}
		
		return def;
	}
	/**
	 * Get the frame length in sample frames out of an audio file. this method
	 * is intended as a help, if the AudioInputStream provided for this file
	 * has AudioSystem.NOT_SPECIFIED length. What it does is searching for
	 * the "duration" property in AudioFileFormat of this file and calculating
	 * the sample frame length out of it. 
	 * @param source The audio file in question
	 * @return The number of sample frames of the audio data contained in the
	 * given file or AudioSystem.NOT_SPECIFIED, if the length cannot be
	 * determined. 
	 */
	public long getFrameLength(File source){
		AudioInputStream in;
		try {
			in = AudioSystem.getAudioInputStream(source);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
			return AudioSystem.NOT_SPECIFIED;
		} catch (IOException e) {
			e.printStackTrace();
			return AudioSystem.NOT_SPECIFIED;
		}
		long l = in.getFrameLength();
		AudioFormat f = in.getFormat();
		try {
			in.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if(l != AudioSystem.NOT_SPECIFIED) return l;
		else{
			AudioFileFormat format;
			try {
				format = getAudioFileFormat(source, null, new String[]{"duration"});
			} catch (UnsupportedAudioFileException e2) {
				e2.printStackTrace();
				return AudioSystem.NOT_SPECIFIED;
			} catch (IOException e2) {
				e2.printStackTrace();
				return AudioSystem.NOT_SPECIFIED;
			}
			Map properties = getProperties(format);
			if(properties != null){
				Object o = properties.get("duration");
				if(!(o instanceof Long)) return AudioSystem.NOT_SPECIFIED;
				long duration = ((Long)o).longValue();
				return (long)(duration / 1000000.0 * f.getSampleRate()); 
			}
			else return AudioSystem.NOT_SPECIFIED;
		}
	}
	/**
	 * Get the installed providers for the given type of service
	 * @param c The class representing the service
	 * @return A List of all found providers
	 */
	public List getProviders(Class c){
		Vector p = new Vector();
		Iterator ps = Service.providers(c);
		while (ps.hasNext()) {
			p.addElement(ps.next());
		}
		return p;
	}
}