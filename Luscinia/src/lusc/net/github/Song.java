package lusc.net.github;
//
//  Song.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/10/2005.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.


//import java.awt.image.BufferedImage;
//import java.sql.Blob;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.attribute.BasicFileAttributes;
//import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Arrays;
import java.awt.Component;
import java.io.*;

import javax.sound.sampled.*;
//import javax.sound.sampled.Mixer.Info;
import javax.swing.JOptionPane;


/**
 * Song is the object that recording units are held in. It contains both acoustic recording data
 * and measured sub-units like elements and syllables. It contains methods to make spectrograms.
 * It is one of the most central classes in Luscinia, and one of the largest. 
 * @author Rob
 *
 */
public class Song {

	boolean updateFFT=true;
	boolean loaded=false;
	boolean running=false;
	boolean setRangeToMax=true; 
	boolean relativeAmp=true; 
	float data[]; 
	float maxAmp, maxAmp2; 
	double [] phase; 
	int minFreq=5; 
	double octstep=10; 
	private static final int EXTERNAL_BUFFER_SIZE = 128000; 
	float [] trig1, trig2; 
	int [] places1,  places2,  places3,  places4; 
	int bitTab[][]; 
	float window[]; 
	double[] filtl, filtu; 
	int counter=0; 
	int counter2=0; 
	int rangef=0; 
	int overallSize=0; 
	int overallLength=0; 
	byte []rawData, rawData2, stereoRawData; 
	float [][] out, out1, envelope;
	int startTime=0; 
	int endTime=0; 
	int dynEqual=0;
	double dynRange=40; 
	float dynMax=10; 
	int echoRange=50;
	//int distinctivenessRange=100; 
	double echoComp=0.4f; 
	int noiseLength1=200; 
	int noiseLength2=15; 
	float noiseRemoval=0f; 
	float maxDB=0;
	float maxPossAmp=0; 
	int maxf=0; 
	double frameLength=1;
	double overlap=0;
	double timeStep=0.5;
	double frequencyCutOff=150;
	int windowMethod=1; 
	double dx, dy, step; 
	int nx, ny, anx, frame, framePad, place; 
	int individualID=0; 
	int songID=0; 
	long tDate=0;
	int numSylls=0;
	String notes=" "; 
	String location=" "; 
	String recordEquipment=" ";
	String recordist=" "; 
	int archived=1;
	double fundAdjust=1; 
	double fundJumpSuppression=100; 
	double minGap=0;
	double minLength=5;
	double maxTrillWavelength=20; 
	int brushSize=5;
	int brushType=1; 
	boolean clickDrag=false;
	int maxBrush=10;
	int minBrush=0; 
	int playbackDivider=1; 
	double upperLoop=15; 
	double lowerLoop=0;
	//Sound File parameters 
	boolean bigEnd=true; 
	boolean signed=true;
	int stereo=1; 
	int ssizeInBits=0; 
	double sampleRate, sampRate; 
	int frameSize=0; 
	String name=" ";
	String individualName=" ";
	String population=" "; 
	String species=" ";
	String gridType=" ";
	String locationX=" "; 
	String locationY=" ";
	String sx, sy;
	//Analysis stored data
	LinkedList<Element> eleList;
	LinkedList<int[]> syllList;
	LinkedList<int[][]> phrases; 
	boolean[] phraseId;
	SourceDataLine  line = null;
	AudioFormat af;
	
	  
	 
	SpectrogramMeasurement sm;
	
	public Song(){
		
	}
	
	public Song (File f, int indid){
		try {
			//Path pa=Paths.get(f.getPath());
			//BasicFileAttributes attr = Files.readAttributes(pa, BasicFileAttributes.class);
			//tDate=attr.creationTime().toMillis();
			//System.out.println("creationTime: " + attr.creationTime());
			//System.out.println("lastAccessTime: " + attr.lastAccessTime());
			//System.out.println("lastModifiedTime: " + attr.lastModifiedTime());
			
			tDate=f.lastModified();
			
			
			AudioInputStream AFStreamA = AudioSystem.getAudioInputStream(f);
			AudioFormat afFormat = AFStreamA.getFormat();
			if(afFormat.isBigEndian()){
				AudioFormat targetFormat = new AudioFormat(afFormat.getEncoding(), afFormat.getSampleRate(), afFormat.getSampleSizeInBits(), afFormat.getChannels(),
						afFormat.getFrameSize(), afFormat.getFrameRate(), false);
				afFormat=targetFormat;
			}
			if (afFormat.getEncoding().toString().startsWith("MPEG")){
				AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, afFormat.getSampleRate(), 16, afFormat.getChannels(),
						afFormat.getChannels()*2, afFormat.getSampleRate(), false);
					afFormat=targetFormat;
			}
			
			System.out.println("ORIGINAL: "+AFStreamA.getFrameLength()+" "+afFormat.getEncoding()+" "+afFormat.getFrameRate());
			
			
			AudioInputStream AFStream=AudioSystem.getAudioInputStream(afFormat, AFStreamA);
			
			sampleRate=AFStream.getFormat().getSampleRate();
			stereo=AFStream.getFormat().getChannels();
			
			int process=0;
			if (stereo>1){
				Object[] possibleValues = { "Use left channel", "Use right channel", "Merge channels" };
				Object selectedValue = JOptionPane.showInputDialog(null, "Choose one", "This is a stereo file. How would you like Luscinia to process it?", JOptionPane.INFORMATION_MESSAGE, null, possibleValues, possibleValues[0]);
				if (selectedValue.equals(possibleValues[0])){
					process=1;
				}
				else if (selectedValue.equals(possibleValues[1])){
					process=2;
				}
				else if (selectedValue.equals(possibleValues[2])){
					process=3;
				}
			}
			
			frameSize=AFStream.getFormat().getFrameSize();
			
			
			
			
			long length=(long)(AFStream.getFrameLength()*frameSize);
			System.out.println(length+" "+Integer.MAX_VALUE+" "+frameSize+" "+AFStream.getFrameLength());
			bigEnd=AFStream.getFormat().isBigEndian();
			AudioFormat.Encoding afe=AFStream.getFormat().getEncoding();
			signed=false;
			if (afe.toString().startsWith("PCM_SIGNED")){signed=true;}
			ssizeInBits=AFStream.getFormat().getSampleSizeInBits();
			
			System.out.println("READ SONG: "+ sampleRate+" "+stereo+" "+frameSize+" "+length+" "+afe.toString()+" "+ssizeInBits);
			
			int xl=(int)Math.round(1000*AFStream.getFrameLength()/(sampleRate));
			
			//This is a hack that uses the last modified time and subtracts the length of the file to get the 
			//creation time - when the file was begun to have been recorded. This works quite well for Macs
			//where there is a bug with the nio creation time attribute. I should revisit this when I move
			//to Java 8...
			
			
			tDate-=xl;
			
			if (length>0){
				rawData=new byte[(int)length];
				AFStream.read(rawData);
			}
			else{
				LinkedList<byte[]> bl=new LinkedList<byte[]>();
				byte[] temp=new byte[frameSize];
				while (AFStream.read(temp)>0){
					System.out.println(bl.size());
					bl.add(temp);
				}
				length=bl.size()*frameSize;
				rawData=new byte[(int)length];
				int x=0;
				for (byte[] t : bl){
					System.arraycopy(t, 0, rawData, x, frameSize);
					x+=frameSize;
				}
			}
			syllList=new LinkedList<int[]>();
			eleList=new LinkedList<Element>();
			individualID=indid;
			name=f.getName();
			
			if (process>0){
				if (process<=2){
					parseSingle(process);
					stereo=1;
					frameSize=frameSize/2;
				}
				//else{
					//parseMerge();
				//}
				//stereo=1;
				//frameSize=frameSize/2;
			}
			
		} 
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Gets the Song's {@link SpectrogramMeasurement} object
	 * @return a {@link SpectrogramMeasurement} object
	 */
	public SpectrogramMeasurement getMeasurer(){
		sm=new SpectrogramMeasurement(this);
		
		return sm;
	}
	
	/**
	 * Gets the time step for the spectrogram
	 * @return a double value (ms) of time step
	 */
	public double getTimeStep(){
		return timeStep;
	}
	
	/**
	 * Sets the time step for the spectrogram
	 * @param a double value (ms) of time step
	 */
	public void setTimeStep(double a){
		timeStep=a;
	}

	/**
	 * gets the frame length for the spectrogram
	 * @return a double value (ms) of frame length
	 */
	public double getFrameLength(){
		return frameLength;
	}
	
	/**
	 * Sets the frame length for the spectrogram
	 * @param a double value (ms) of frame length
	 */
	public void setFrameLength(double a){
		frameLength=a;
	}

	/**
	 * gets the echoComp parameter - the amount of dereverberation.
	 * @return a double value of echoComp
	 */
	public double getEchoComp(){
		return echoComp;
	}
	
	/**
	 * Sets the echoComp parameter - the amount of dereverberation.
	 * @param a double value of echoComp
	 */
	public void setEchoComp(double a){
		echoComp=a;
	}

	/**
	 * gets the sample rate for the Song
	 * @return a double value (Hz)
	 */
	public double getSampRate(){
		return sampRate;
	}
	
	/**
	 * Sets the sample rate for the Song
	 * @param a double value (Hz)
	 */
	public void setSampRate(double a){
		sampRate=a;
	}

	/**
	 * gets the dynamic range for the spectrogram
	 * @return a double value for dynamic range (dB)
	 */
	public double getDynRange(){
		return dynRange;
	}
	
	/**
	 * Sets the dynamic range for the spectrogram
	 * @param a double value for dynamic range (dB)
	 */
	public void setDynRange(double a){
		dynRange=a;
	}

	/**
	 * gets the dx parameter for the spectrogram - the number of ms between neighbouring
	 * columns of the spectrogram
	 * @return a double (ms) value for dx
	 */
	public double getDx(){
		return dx;
	}
	
	/**
	 * Sets the dx parameter for the spectrogram - the number of ms between neighbouring
	 * columns of the spectrogram
	 * @param a double (ms) value for dx
	 */
	public void setDx(double a){
		dx=a;
	}

	/**
	 * gets the dy parameter for the spectrogram - the frequency differences between
	 * neighbouring rows for the spectrogram
	 * @return a double (Hz) value for dy
	 */
	public double getDy(){
		return dy;
	}
	
	/**
	 * Sets the dy parameter for the spectrogram - the frequency differences between
	 * neighbouring rows for the spectrogram
	 * @param a double (Hz) value for dy
	 */
	public void setDy(double a){
		dy=a;
	}

	/**
	 * gets the fundAdjust parameter - a user-set value that adjusts the model of fundamental
	 * frequency estimation
	 * @return a double value for fundAdjust
	 */
	public double getFundAdjust(){
		return fundAdjust;
	}
	
	/**
	 * Sets the fundAdjust parameter - a user-set value that adjusts the model of fundamental
	 * frequency estimation
	 * @param a double value for fundAdjust
	 */
	public void setFundAdjust(double a){
		fundAdjust=a;
	}

	/**
	 * gets the minLength parameter - a user-set value that sets the minimum length of an 
	 * element. If a smaller element is detected, it is not added to the Element list.
	 * @return a double (ms) for minLength
	 */
	public double getMinLength(){
		return minLength;
	}
	
	/**
	 * Sets the minLength parameter - a user-set value that sets the minimum length of an 
	 * element. If a smaller element is detected, it is not added to the Element list.
	 * @param a double (ms) for minLength
	 */
	public void setMinLength(double a){
		minLength=a;
	}

	/**
	 * gets the minGap parameter - a user-set value that sets the minimum gap allowed between 
	 * neighbouring elements (detected from the same user drawn blob). If two detected elements
	 * are closer together than this, Song will attempt to join them together.
	 * @return a double (ms) value for minGap
	 */
	public double getMinGap(){
		return minGap;
	}
	
	/**
	 * Sets the minGap parameter - a user-set value that sets the minimum gap allowed between 
	 * neighbouring elements (detected from the same user drawn blob). If two detected elements
	 * are closer together than this, Song will attempt to join them together.
	 * @param a double (ms) value for minGap
	 */
	public void setMinGap(double a){
		minGap=a;
	}

	/**
	 * gets the Frame size of the spectrogram - an int value of the length of the FFT buffer
	 * @return an int value (number of samples) for frame size
	 */
	public int getFrameSize(){
		return frameSize;
	}
	
	/**
	 * Sets the Frame size of the spectrogram - an int value of the length of the FFT buffer
	 * @param a int value (number of samples) for frame size
	 */
	public void setFrameSize(int a){
		frameSize=a;
	}

	/**
	 * gets the dynMax parameter. This sets the black-point of the spectrogram - intensity values 
	 * above this are set to black 
	 * @return a float value for dynMax
	 */
	public float getDynMax(){
		return dynMax;
	}
	
	/**
	 * Sets the dynMax parameter. This sets the black-point of the spectrogram - intensity values 
	 * above this are set to black 
	 * @param a float value for dynMax
	 */
	public void setDynMax(float a){
		dynMax=a;
	}

	/**
	 * gets the maxf parameter. This sets the maximum frequency in the plotted spectrogram, and
	 * is scaled as the number of rows in the spectrogram
	 * @return an int value for the number of rows in the spectrogram
	 */
	public int getMaxF(){
		return maxf;
	}
	
	/**
	 * Sets the maxf parameter. This sets the maximum frequency in the plotted spectrogram, and
	 * is scaled as the number of rows in the spectrogram
	 * @param a int value for the number of rows in the spectrogram
	 */
	public void setMaxF(int a){
		maxf=a;
	}

	/**
	 * gets the dynEqual dynamic equalization parameter. This parameter specifies a window
	 * within which the highest intensity point is redrawn as black. If it is set to 0,
	 * this equalization is not used.
	 * @return an int value (ms) for the dynEqual parameter.
	 */
	public int getDynEqual(){
		return dynEqual;
	}
	
	/**
	 * Sets the dynEqual dynamic equalization parameter. This parameter specifies a window
	 * within which the highest intensity point is redrawn as black. If it is set to 0,
	 * this equalization is not used.
	 * @param a int value (ms) for the dynEqual parameter.
	 */
	public void setDynEqual(int a){
		dynEqual=a;
	}

	/**
	 * gets the echoRange parameter - the window length for the dereverberation algorithm
	 * @return an int value (ms) for the echoRange parameter.
	 */
	public int getEchoRange(){
		return echoRange;
	}
	
	/**
	 * Sets the echoRange parameter - the window length for the dereverberation algorithm
	 * @param a int value (ms) for the echoRange parameter.
	 */
	public void setEchoRange(int a){
		echoRange=a;
	}

	/**
	 * gets the overall length of the plotted spectrogram
	 * @return an int value (ms?) for the overallLength parameter
	 */
	public int getOverallLength(){
		return overallLength;
	}
	
	/**
	 * gets the overall length of the plotted spectrogram
	 * @param a int value (ms?) for the overallLength parameter
	 */
	public void setOverallLength(int a){
		overallLength=a;
	}

	/**
	 * gets the nx parameter - the number of columns in the plotted spectrogram
	 * @return an int value for nx
	 */
	public int getNx(){
		return nx;
	}
	
	/**
	 * gets the nx parameter - the number of columns in the plotted spectrogram
	 * @param a int value for nx
	 */
	public void setNx(int a){
		nx=a;
	}

	/**
	 * gets the ny parameter - the number of rows in the plotted spectrogram. 
	 * IS THIS EVER DIFFERENT FROM maxf???
	 * @return an int value for ny
	 */
	public int getNy(){
		return ny;
	}
	
	/**
	 * gets the ny parameter - the number of rows in the plotted spectrogram. 
	 * IS THIS EVER DIFFERENT FROM maxf???
	 * @param a int value for ny
	 */
	public void setNy(int a){
		ny=a;
	}
	
	/**
	 * gets the brushType parameter - an int index for different types of brush for
	 * measuring elements
	 * @return an int for the brushType parameter
	 */
	public int getBrushType(){
		return brushType;
	}
	
	/**
	 * gets the brushType parameter - an int index for different types of brush for
	 * measuring elements
	 * @param a int for the brushType parameter
	 */
	public void setBrushType(int a){
		brushType=a;
	}

	/**
	 * gets the brushSize parameter - the number of spectrogram rows that the measurement
	 * brush is wide
	 * @return an int (spectrogram rows) for the brush size parameter.
	 */
	public int getBrushSize(){
		return brushSize;
	}
	
	/**
	 * gets the brushSize parameter - the number of spectrogram rows that the measurement
	 * brush is wide
	 * @param a int (spectrogram rows) for the brush size parameter.
	 */
	public void setBrushSize(int a){
		brushSize=a;
	}

	/**
	 * gets the maxBrush parameter - when a frequency range is set for spectrogram measurement,
	 * this is the maximum frequency.
	 * @return an int value for maxBrush
	 */
	public int getMaxBrush(){
		return maxBrush;
	}
	
	/**
	 * gets the maxBrush parameter - when a frequency range is set for spectrogram measurement,
	 * this is the maximum frequency.
	 * @param a int value for maxBrush
	 */
	public void setMaxBrush(int a){
		maxBrush=a;
	}

	/**
	 * gets the minBrush parameter - when a frequency range is set for spectrogram measurement,
	 * this is the minimum frequency.
	 * @return an int value for minBrush
	 */
	public int getMinBrush(){
		return minBrush;
	}
	
	/**
	 * gets the minBrush parameter - when a frequency range is set for spectrogram measurement,
	 * this is the minimum frequency.
	 * @param a int value for minBrush
	 */
	public void setMinBrush(int a){
		minBrush=a;
	}

	/**
	 * gets the noiseRemoval parameter - a value for the intensity of noise removal
	 * @return a float value for noiseRemoval
	 */
	public float getNoiseRemoval(){
		return noiseRemoval;
	}
	
	/**
	 * gets the noiseRemoval parameter - a value for the intensity of noise removal
	 * @param a float value for noiseRemoval
	 */
	public void setNoiseRemoval(float a){
		noiseRemoval=a;
	}

	/**
	 * gets the frequency cut off of the high-pass filter
	 * @return value of frequencyCutOff
	 */
	public double getFrequencyCutOff(){
		return frequencyCutOff;
	}

	/**
	 * sets the frequency cut off of the high-pass filter
	 * @param a value for frequencyCutOff
	 */
	public void setFrequencyCutOff(double a){
		frequencyCutOff=a;
	}

	/**
	 * gets the FFT window method index
	 * @return value of windowMethod
	 */
	public int getWindowMethod(){
		return windowMethod;
	}

	/**
	 * sets the FFT window method index
	 * @param a value for windowMethod
	 */
	public void setWindowMethod(int a){
		windowMethod=a;
	}

	/**
	 * Gets the first noise removal length parameter
	 * @return value of noiseLength1
	 */
	public int getNoiseLength1(){
		return noiseLength1;
	}

	/**
	 * Sets the first noise removal length parameter
	 * @param a value for noiseLength1
	 */
	public void setNoiseLength1(int a){
		noiseLength1=a;
	}

	/**
	 * Gets the second noise removal length parameter
	 * @return value of noiseLength2
	 */
	public int getNoiseLength2(){
		return noiseLength2;
	}

	/**
	 * Sets the second noise removal length parameter
	 * @param a value for noiseLength2
	 */
	public void setNoiseLength2(int a){
		noiseLength2=a;
	}

	/**
	 * sets the fundamental frequency jump suppression parameter (stops algorithm from 
	 * jumping between octaves, mostly)
	 * @return the fundJumpSuppression parameter
	 */
	public double getFundJumpSuppression(){
		return fundJumpSuppression;
	}

	/**
	 * gets the fundamental frequency jump suppression parameter (stops algorithm from 
	 * jumping between octaves, mostly)
	 * @param a the jump suppression parameter
	 */
	public void setFundJumpSuppression(double a){
		fundJumpSuppression=a;
	}

	/**
	 * gets the upper hysteresis loop for element measurement
	 * @return a double value for the upper loop value
	 */
	public double getUpperLoop(){
		return upperLoop;
	}

	/**
	 * sets the upper hysteresis loop for element measurement
	 * @param a double value for the upper loop value
	 */
	public void setUpperLoop(double a){
		upperLoop=a;
	}

	/**
	 * gets the lower hysteresis loop for element measurement
	 * @return a double value for the lower loop value
	 */
	public double getLowerLoop(){
		return lowerLoop;
	}

	/**
	 * sets the lower hysteresis loop for element measurement
	 * @param a double value for the lower loop value
	 */
	public void setLowerLoop(double a){
		lowerLoop=a;
	}

	/**
	 * gets the setRangeToMax parameter. If true (default), the black-point in the
	 * spectrogram is set to the most intense point. If not, it can be set lower.
	 * @return setRangeToMax
	 */
	public boolean getSetRangeToMax(){
		return setRangeToMax;
	}

	/**
	 * sets the setRangeToMax parameter. If true (default), the black-point in the
	 * spectrogram is set to the most intense point. If not, it can be set lower.
	 * @param a value for setRangeToMax
	 */
	public void setSetRangeToMax(boolean a){
		setRangeToMax=a;
	}

	/**
	 * gets the overlap parameter for spectrogram calculation.
	 * @return the overlap parameter
	 */
	public double getOverlap(){
		return overlap;
	}

	/**
	 * sets the overlap parameter for spectrogram calculation. Don't change this without changing
	 * timeStep
	 * @param a overlap
	 */
	public void setOverlap(double a){
		overlap=a;
	}

	/**
	 * gets the spectrogram
	 * @return a float[][] array, frequency x time, with intensity indicated in the cells.
	 */
	public float[][] getOut(){
		return out;
	}
	
	/**
	 * sets the spectrogram
	 * @param a float[][] array, frequency x time, with intensity indicated in the cells.
	 */
	public void setOut(float[][] a){
		out=a;
	}

	/**
	 * gets the envelope float[][] an array with max and min ampitude values for each spectrogram bin
	 * @return a float[][] for the envelope parameter
	 */
	public float[][] getEnvelope(){
		return envelope;
	}
	
	/**
	 * Sets the envelope float[][] an array with max and min ampitude values for each spectrogram bin
	 * @param a float[][] for the envelope parameter
	 */
	public void setEnvelope(float[][]a){
		envelope=a;
	}

	/**
	 * Gets the ID for this song
	 * @return value for songID
	 */
	public int getSongID(){
		return songID;
	}
	
	/**
	 * Sets the ID for this song
	 * @param a value for songID
	 */
	public void setSongID(int a){
		songID=a;
	}

	/**
	 * Gets the name of this song
	 * @return String value of the name parameter
	 */
	public String getName(){
		return name;
	}

	/**
	 * Sets the name of this song
	 * @param a String value of the name parameter
	 */
	public void setName(String a){
		name=a;
		if (name==null){name=" ";}
	}

	/**
	 * Gets the individual ID for this song
	 * @return value for individualID
	 * @see Individual
	 */
	public int getIndividualID(){
		return individualID;
	}
	
	/**
	 * Sets the individual ID for this song
	 * @param a value for individualID
	 * @see Individual
	 */
	public void setIndividualID(int a){
		individualID=a;
	}
	
	/**
	 * Gets the grid type for this song (lat/long, etc)
	 * @return String value for grid type
	 * @see Individual
	 */
	public String getGridType(){
		return gridType;
	}

	/**
	 * Sets the grid type for this song (lat/long, etc)
	 * @param a String value for grid type
	 * @see Individual
	 */
	public void setGridType(String a){
		gridType=a;
	}
	
	/**
	 * Gets the location for this song in the longitude dimension
	 * @return String value for locationX
	 * @see Individual
	 */
	public String getLocationX(){
		return locationX;
	}
	
	/**
	 * Sets the location for this song in the longitude dimension
	 * @param a String value for locationX
	 * @see Individual
	 */
	public void setLocationX(String a){
		locationX=a;
	}

	/**
	 * Gets the location for this song in the latitude dimension
	 * @return String value for locationY
	 * @see Individual
	 */
	public String getLocationY(){
		return locationY;
	}
	
	/**
	 * Sets the location for this song in the latitude dimension
	 * @param a String value for locationY
	 * @see Individual
	 */
	public void setLocationY(String a){
		locationY=a;
	}

	/**
	 * get a location parameter. Check whether this is really needed.
	 * @return a String location parameter
	 */
	public String getSx(){
		return sx;
	}

	/**
	 * set a location parameter. Check whether this is really needed.
	 * @param a a String location parameter
	 */
	public void setSx(String a){
		sx=a;
	}

	/**
	 * get a location parameter. Check whether this is really needed.
	 * @return a String location parameter
	 */
	public String getSy(){
		return sy;
	}

	/**
	 * set a location parameter. Check whether this is really needed.
	 * @param a a String location parameter
	 */
	public void setSy(String a){
		sy=a;
	}

	/**
	 * Gets the individual name for this Song
	 * @return String value for individualName
	 * @see Individual
	 */
	public String getIndividualName(){
		return individualName;
	}

	/**
	 * Sets the individual name for this Song
	 * @param a String value for individualName
	 * @see Individual
	 */
	public void setIndividualName(String a){
		individualName=a;
	}

	/**
	 * Gets the species name for this song
	 * @return String value of the species parameter
	 * @see Individual
	 */
	public String getSpecies(){
		return species;
	}
	
	/**
	 * Sets the species name for this song
	 * @param a String value of the species parameter
	 * @see Individual
	 */
	public void setSpecies(String a){
		species=a;
		if (species==null){species=" ";}
	}

	/**
	 * Gets the population name for this song
	 * @return a String value of the population parameter
	 * @see Individual
	 */
	public String getPopulation(){
		return population;
	}
	
	/**
	 * Sets the population name for this song
	 * @param a String value for the population parameter
	 * @see Individual
	 */
	public void setPopulation(String a){
		population=a;
		if (population==null){population=" ";}
	}

	/**
	 * gets the time and date of the recording
	 * @return value of tDate
	 */
	public long getTDate(){
		return tDate;
	}

	/**
	 * sets the time and date of the recording
	 * @param a value for tDate
	 */
	public void setTDate(long a){
		tDate=a;
	}

	/**
	 * sets the metadata for verbal description of recording location
	 * @return String value for location
	 */
	public String getLocation(){
		return location;
	}

	/**
	 * gets the metadata for verbal description of recording location
	 * @param a String value for location
	 */
	public void setLocation(String a){
		location=a;
	}

	/**
	 * gets the metadata for notes about the song
	 * @return String value for notes
	 */
	public String getNotes(){
		return notes;
	}

	/**
	 * sets the metadata for notes about the song
	 * @param a String value for notes
	 */
	public void setNotes(String a){
		notes=a;
	}

	/**
	 * gets the metadata for recording equipment
	 * @return String value for recordEquipment
	 */
	public String getRecordEquipment(){
		return recordEquipment;
	}

	/**
	 * sets the metadata for recording equipment
	 * @param a String value for recordEquipment
	 */
	public void setRecordEquipment(String a){
		recordEquipment=a;
	}

	/**
	 * gets the metadata for the recordist
	 * @return String value for recordist
	 */
	public String getRecordist(){
		return recordist;
	}
	
	/**
	 * gets the archive status of the song
	 * @return int value for archived
	 */
	public int getArchived(){
		return archived;
	}

	/**
	 * sets the metadata for the recordist
	 * @param a String value for recordist
	 */
	public void setRecordist(String a){
		recordist=a;
	}
	
	/**
	 * sets whether or not the song is an archived file (not to be measured)
	 * @param an int value for archive status (0=archived, 1=not an archive)
	 */
	public void setArchived(int a){
		archived=a;
	}

	/**
	 * gets the constructed AudioFormat object for Song
	 * @return af, an AudioFormat object
	 */
	public AudioFormat getAf(){
		return af;
	}
	
	/**
	 * gets whether or not the audio data has been loaded from the database
	 * (Sometimes it is not to save time/memory during analysis)
	 * @return value of the loaded parameter
	 */
	public boolean getLoaded(){
		return loaded;
	}
	
	/**
	 * sets whether or not the audio data has been loaded from the database
	 * (Sometimes it is not to save time/memory during analysis)
	 * @param a value for the loaded parameter
	 */
	public void setLoaded(boolean a){
		loaded=a;
	}
	
	/**
	 * Gets the number of channels
	 * @return value of stereo
	 */
	public int getStereo(){
		return stereo;
	}
	
	/**
	 * Sets the number of channels (max 2)
	 * @param a int value for stereo
	 */
	public void setStereo(int a){
		stereo=a;
	}

	/**
	 * gets whether or not the audio format is big-ended
	 * @return value of bigEnd
	 */
	public boolean getBigEnd(){
		return bigEnd;
	}

	/**
	 * sets whether or not the audio format is big-ended
	 * @param a value for bigEnd
	 */
	public void setBigEnd(boolean a){
		bigEnd=a;
	}

	/**
	 * gets the boolean value for whether the audio format is signed
	 * @return value of signed
	 */
	public boolean getSigned(){
		return signed;
	}

	/**
	 * sets the boolean value for whether the audio format is signed
	 * @param a value for signed
	 */
	public void setSigned(boolean a){
		signed=a;
	}

	/**
	 * gets the sample rate in Hz
	 * @return value of sampleRate
	 */
	public double getSampleRate(){
		return sampleRate;
	}
	
	/**
	 * sets the sample rate in Hz
	 * @param a value for sampleRate
	 */
	public void setSampleRate(double a){
		sampleRate=a;
	}

	/**
	 * gets the sample size in bits of the sound format
	 * @return value of ssizeInBits
	 */
	public int getSizeInBits(){
		return ssizeInBits;
	}

	/**
	 * sets the sample size in bits of the sound format
	 * @param a value for ssizeInBits
	 */
	public void setSizeInBits(int a){
		ssizeInBits=a;
	}

	/**
	 * gets the length of the raw byte array data
	 * @return length of the rawData array
	 */
	public int getRDLength(){
		return rawData.length;
	}

	/**
	 * gets the raw data - byte array of audio data
	 * @return byte array of audio data
	 */
	public byte[] getRawData(){
		return rawData;
	}
	
	/**
	 * sets the raw data - byte array of audio data
	 * @param a byte array of audio data
	 */
	public void setRawData(byte[] a){
		rawData=a;
	}

	/**
	 * gets the maximum amplitude in the spectrogram
	 * @return a float value of the maximum amplitude
	 */
	public float getMaxDB(){
		return maxDB;
	}

	/**
	 * get the start point for the spectrogram. Used in the guidePanel
	 * @return an int value for startTime
	 */
	public int getStartTime(){
		return startTime;
	}

	/**
	 * get the end point for the spectrogram. Used in the guidePanel.
	 * @return an int value for endTime
	 */
	public int getEndTime(){
		return endTime;
	}

	/**
	 * gets the frame parameter - the frame size if the spectrogram in sample units
	 * @return frame size
	 */
	public int getFrame(){
		return frame;
	}

	/**
	 * sets the frame parameter - the frame size if the spectrogram in sample units
	 * @param a frame size
	 */
	public void setFrame(int a){
		frame=a;
	}

	/**
	 * sets the overall size of the sound, in terms of the number of samples
	 */
	public void setOverallSize(){
		if (frameSize==0){frameSize++;}
		overallSize=rawData.length/frameSize;
	}

	/**
	 * gets the overall size of the sound - the number of samples
	 * @return an int value of the number of samples in the sound
	 */
	public int getOverallSize(){
		return overallSize;
	}

	/**
	 * This gets the playbackDivider parameter, which determines the speed of playback. Should
	 * be 1,2,4 or 8
	 * @return an int showing how much to slow down playback
	 */
	public int getPlaybackDivider(){
		return playbackDivider;
	}
	
	/**
	 * 
	 * @param a
	 */
	public void setPlaybackDivider(int a){
		playbackDivider=a;
	}
	
	/**
	 * gets the length of the raw (byte) audio data
	 * @return an int with the length of the byte array
	 */
	public int getRawDataLength(){
		return rawData.length;
	}
	
	/**
	 * Convenience get Method to build a complex SQL line to write data into database...
	 * @param b separator in SQL syntax
	 * @return a String in part-formed SQL with a complex list of parameters
	 */
	public String getDetails(String b){
		int sa=(int)sampleRate;
		String details="echocomp="+echoComp+b+"echorange="+echoRange+b+"noise1="+noiseRemoval+b+"noise2="+
				noiseLength1+b+"noise3="+noiseLength2+b+"dyncomp="+dynRange+
				b+"dynrange="+dynEqual+b+"maxfreq="+maxf+b+"framelength="+frameLength+b+"timestep="
				+timeStep+b+"filtercutoff="+frequencyCutOff+b+"windowmethod="+windowMethod+b+"dx="+
				dx+b+"dy="+dy+b+"samplerate="+sa;
		return details;
	}
	
	
	
	/**
	 * Gets a boolean array indicating which syllables are actually phrases. interpretSyllables
	 * needs to be run first!
	 * @return a boolean array, 'true' values represent syllables that are phrase markers
	 */
	public boolean[] getPhraseID(){
		return phraseId;
	}
	
	/**
	 * gets the number of phrases in the song
	 * @return number of phrases
	 */
	public int getNumPhrases(){
		if (phrases==null){
			interpretSyllables();
			if (phrases==null){
				return 0;
			}
		}
		return phrases.size();
	}

	/**
	 * gets a specified phrase
	 * @param a index of the phrase to get from the phrase list
	 * @return an int[][] specifying the elements in a particular phrase
	 */
	public int[][] getPhrase(int a){
		//System.out.println("getting phrase "+a+" out of "+phrases.size());
		if (phrases==null){
			System.out.println("WARNING NULL PHRASES");
			interpretSyllables();
		}
		return phrases.get(a);
	}

	/**
	 * sets the whole set of phrases for this Song
	 * @param a an int[][] object specifiying phrase structure
	 */
	public void setPhrases(LinkedList<int[][]> a){
		phrases=a;
	}

	/**
	 * Sets the whole syllList to an external list of syllables
	 * @param a a LinkedList of int[]s specifying syllables
	 */
	public void setSyllList(LinkedList<int[]> a){
		syllList=a;
	}
	
	/**
	 * This method overrides making the FFT. It may be useful in some cases to save redoing
	 * identical FFTs.
	 * @param x sets updateFFT flag
	 */
	
	public void setUpdateFFT(boolean x){
		updateFFT=x;
	}

	/**
	 * This is a legacy function to update syllable measurements for old versions of the db.
	 */
	public void updateSyllableList(){
		System.out.println("UPDATING!!!");
		for(int i=0; i<syllList.size(); i++){
			int[] s=syllList.get(i);
			int[]t=new int[2];
			t[0]=(int)Math.round(s[0]*dx);
			t[1]=(int)Math.round(s[1]*dx);
			syllList.remove(i);
			syllList.add(t);
		}
	}

	/**
	 * get a specified syllable
	 * @param a index of the syllable to get
	 * @return an int[] specifying a syllable
	 */
	public int[] getSyllable(int a){
		if (a>=syllList.size()){
			return null;
		}
		return syllList.get(a);
	}

	/**
	 * get the number of syllables in the syllList
	 * @return the size of syllList
	 */
	public int getNumSyllables(){
		if (syllList==null){
			return 0;
		}
		return syllList.size();
	}

	/**
	 * removes a specified syllable from the syllList
	 * @param a location of the syllable to be removed from the syllList
	 */
	public void removeSyllable(int a){
		if (a<syllList.size()){
			syllList.remove(a);
		}
	}
	
	public void setNumSylls(int a){
		numSylls=a;
	}
	
	public int getNumSylls(){
		return numSylls;
	}

	/**
	 * adds a new syllable at a specific location in the syllList
	 * @param a location to add the new syllable
	 * @param syl an int[] specifying a syllable
	 */
	public void addSyllable(int a, int[] syl){
		syllList.add(a, syl);
	}

	/**
	 * adds a new syllable to the end of the syllList
	 * @param syl an int[] specifying a syllable
	 */
	public void addSyllable(int[] syl){
		syllList.add(syl);
	}

	/**
	 * Empties the whole list of syllables
	 */
	public void clearSyllables(){
		syllList.clear();
	}

	/**
	 * Sets the whole eleList to an external LinkedList
	 * @param a a LinkedList of Elements
	 * @see Element
	 */
	public void setEleList(LinkedList<Element> a){
		eleList=a;
	}

	/**
	 * Gets a specified Element from the eleList
	 * @param a index of Element to get
	 * @return an {@link Element}
	 */
	public Element getElement(int a){
		if (a>=eleList.size()){
			return null;
		}
		return eleList.get(a);
	}

	/**
	 * Gets the number of elements in this Song object
	 * @return the number of Elements
	 * @see Element
	 */
	public int getNumElements(){
		if (eleList==null){
			return 0;
		}
		return eleList.size();
	}

	/**
	 * Removes a specified element from eleList
	 * @param a index of Element to remove
	 * @see Element
	 */
	public void removeElement(int a){
		if (a<eleList.size()){
			eleList.remove(a);
		}
	}

	/**
	 * Adds an element to the eleList, at a specific location
	 * @param a location to add a new Element
	 * @param ele an Element to add
	 * @see Element
	 */
	public void addElement(int a, Element ele){
		eleList.add(a, ele);
	}

	/**
	 * Adds an element to the eleList, at the end
	 * @param ele an Element to add
	 * @see Element
	 */
	public void addElement(Element ele){
		eleList.add(ele);
	}

	/**
	 * Empties the linked list of elements
	 * @see Element
	 */
	public void clearElements(){
		eleList.clear();
	}

	/**
	 * Helps clear up a Song object when closed. Not sure whether this helps garbage collection 
	 * or not...
	 */
	public void clearUp(){
		rawData=null;
		rawData2=null;
		bitTab=null;
		filtl=null;
		filtu=null;
		data=null;
		window=null;
		out=null;
		out1=null;
		eleList=null;
		syllList=null;
		notes=null;
		location=null;
		recordEquipment=null;
		recordist=null;
		trig1=null;
		trig2=null;
		places1=null;
		places2=null;
		places3=null;
		places4=null;
		
		shutDownPlayback();
		
	}
	
	/*
	public void tidyUpX(){
		out=null;
		out1=null;
		data=null;
		eleList=null;
	}
	*/
	
	/**
	 * This method splits a song object into multiple song objects by its syllables
	 * making a set of new 'songs' that are returned in an array.
	 */
	public Song[] splitSong(){
		Song[] songs=new Song[syllList.size()];
		for (int i=0; i<syllList.size(); i++){
			int[] syll=(int[])syllList.get(i);
			int a=0;
			int b=0;
			
			if (ssizeInBits<=16){
				int p1=(int)Math.round(syll[0]-20);
				if (p1<0){p1=0;}
				int p2=(int)Math.round(syll[1]+20);
				if (p2>=overallLength){
					p2=overallLength-1;
				}
				double q=sampleRate*0.001;
				System.out.println(q+" "+frameSize);
				a=(int)Math.round(p1*q)*frameSize;
				b=(int)Math.round(p2*q)*frameSize;
				System.out.println("NEWSONGBOUNDS: "+a+" "+b+" "+sampleRate+" "+stereo+" "+p1+" "+p2+" "+syll[0]+" "+syll[1]);
			}
			
			byte[] sub=new byte[b-a];
			System.arraycopy(rawData, a, sub, 0, b-a);
			Song s=new Song();
			s.rawData=sub;
			s.ssizeInBits=this.ssizeInBits;
			s.signed=this.signed;
			s.bigEnd=this.bigEnd;
			s.stereo=this.stereo;
			s.sampleRate=this.sampleRate;
			s.frameSize=this.frameSize;
			String sn=this.name;
			if (name.endsWith(".wav")){
				sn=name.substring(0, name.length()-4);
			}
			if (name.endsWith(".mp3")){
				sn=name.substring(0, name.length()-4);
			}
			if (name.endsWith(".aif")){
				sn=name.substring(0, name.length()-4);
			}
			if (name.endsWith(".aiff")){
				sn=name.substring(0, name.length()-5);
			}
			s.name=sn+"_"+(i+1);
			s.individualID=this.individualID;
			s.individualName=this.individualName;
			s.tDate=this.tDate+syll[0];
			s.recordEquipment=this.recordEquipment;
			s.recordist=this.recordist;
			s.location=this.location;
			s.notes=this.notes;
			songs[i]=s;
		}
		return songs;
	}
	
	
	
	/**
	 * This is another effort to turn audio data into a float[] array. This is deprecated
	 */
	void parseSound2(){
		float[][]look=new float[256][frameSize];
		float divider=(float)(0.5*Math.pow(256, frameSize));
		data=new float[rawData.length/frameSize];
		float[]mult=new float[frameSize];
		for (int i=0; i<frameSize; i++){mult[i]=(float)Math.pow(256, i);}
		int k=0;
		for (int i=0; i<frameSize; i++){
			for (int j=0; j<256; j++){
				k=j;
				if ((i==frameSize-1)&&(j>127)){k-=256;}
				look[j][i]=k*mult[i];
			}
		}
		int numb=frameSize-1;
		int i=0;
		int n=0;
		int p;
		//float max=0;
		//float min=0;
		while (i<rawData.length){
			for (int j=0; j<numb; j++){
				p=rawData[i];
				if (p<0){p+=256;}
				data[n]+=look[p][j];
				i++;
			}
			
			p=rawData[i];
			if (p<0){p+=256;}
			data[n]+=look[p][numb];
			//data[n]=(float)Math.sin(n*0.5);
			data[n]/=divider;
			//if (data[n]>max){max=data[n];}
			//if (data[n]<min){min=data[n];}
			i++;
			n++;
		}
		look=null;
		if (stereo!=1){
			float[]out2=new float[data.length/stereo];
			for (i=0; i<out2.length; i++){
				for (int j=0; j<stereo; j++){
					out2[i]+=data[i*stereo+j];
				}
				out2[i]/=stereo;
			}
			//sampleRate/=stereo;
			data=new float[out2.length];
			System.arraycopy(out2, 0, data, 0, out2.length);
			out2=null;
		}
	}
	
	/**
	 * This is an effort to take binary data and transform it into a float[] representation of the sound
	 * @param start beginning point in the sound
	 * @param end end point in the sound.
	 */
	private void parseSound(int start, int end){
		int frameSizeC=frameSize/stereo;
		float[][]look=new float[256][frameSizeC];
		float divider=(float)(0.5*Math.pow(256, frameSizeC));
		
		float[]mult=new float[frameSizeC];
		for (int i=0; i<frameSizeC; i++){mult[i]=(float)Math.pow(256, i);}
		int k=0;
		for (int i=0; i<frameSizeC; i++){
			for (int j=0; j<256; j++){
				k=j;
				if ((frameSizeC>1)&&(i==frameSizeC-1)&&(j>127)){k-=256;}
				look[j][i]=k*mult[i];
			}
		}
		int numb=frameSizeC-1;
		int size=end-start;
		data=null;
		data=new float[size];
		end*=frameSizeC;
		start*=frameSizeC;
		int i=start;
		int n=0;
		int p;
		while (i<end){
			for (int j=0; j<numb; j++){
				p=rawData[i];
				if (p<0){p+=256;}
				data[n]+=look[p][j];
				i++;
			}
			
			p=rawData[i];
			if (p<0){p+=256;}
			data[n]+=look[p][numb];
			data[n]/=divider;
			i++;
			n++;
		}
		look=null;
		if (stereo!=1){
			float[]out2=new float[data.length/stereo];
			for (i=0; i<out2.length; i++){
				for (int j=0; j<stereo; j++){
					out2[i]+=data[i*stereo+j];
				}
				out2[i]/=stereo;
				/*
				out2[i]=(data[i*2])/(data[i*2]+data[i*2+1]);
				if (out2[i]<0){out2[i]=0;}
				if (out2[i]>1){out2[i]=0;}
				if (data[i*2]+data[i*2+1]<=0){out2[i]=0;}
				*/
				//out2[i]=(data[i*2])/(Math.abs(data[i*2])+Math.abs(data[i*2+1]));
			}
			data=new float[out2.length];
			System.arraycopy(out2, 0, data, 0, out2.length);
			out2=null;
		}
	}
	
	/**
	 * This organizes calculation of the spectrogram, breaking the sound into chunks
	 * that will not overwhelm the memory limits of java
	 * @param chunks number of chunks
	 */
	void chunkyFFT(double chunks){
		place=0;
		int eTime, sTime;
		int overSize=overallSize;
		for (int i=0; i<=chunks; i++){
			sTime=startTime+i*20000;
			eTime=sTime+20000;
			if (eTime>endTime){eTime=endTime;}
			
			//System.out.println("BOUNDS 1: "+i+" "+sTime+" "+eTime);
			
			sTime=(int)Math.round(sTime*step);
			eTime=(int)Math.round(eTime*step+frame);
			
			int L=eTime-frame-sTime;
			
			//System.out.println("BOUNDS 2: "+i+" "+sTime+" "+eTime+" "+L);
			
			//while (eTime>overSize){eTime-=(int)Math.round(step);}
			if (eTime>overSize){eTime=overSize;}
			
			//System.out.println("BOUNDS 3: "+i+" "+sTime+" "+eTime);
			
			if (sTime<eTime){
				int adStartTime=sTime-rangef;
				if (adStartTime<0){adStartTime=0;}
				int adEndTime=eTime+rangef;
				if (adEndTime>overSize){adEndTime=overSize;}
				//int adStartTime=sTime;
				//int adEndTime=eTime;
				//System.out.println("PARSESOUND "+adStartTime+" "+adEndTime);
				parseSound(adStartTime*stereo, adEndTime*stereo);
				//System.out.println("a: "+data.length+" "+adEndTime+" "+adStartTime+" "+rawData.length);
				//System.out.println("FILTERSOUND "+(sTime-adStartTime)+" "+(eTime-adStartTime));
				filter(sTime-adStartTime, eTime-adStartTime);
				//System.out.println("b: "+data.length);
				//System.out.println("FFTSOUND");
				calculateFFT(L);
				//System.out.println("DONECHUNK");
			}
		}
	}

	
	/**
	 * This organizes the calculation of a spectrographic representation of the sound
	 * @param startTime time to start calculating
	 * @param endTime time to finish
	 */
	public void makeMyFFT(int startTime, int endTime){
		/*
		DataLine.Info dataLineInfo =
			    new DataLine.Info(
					      TargetDataLine.class,
					      af);
		Line.Info lines[] = AudioSystem.getTargetLineInfo(dataLineInfo);
		for (int n = 0; n < lines.length; n++) {
		    System.out.println("Target " + lines[n].toString() + " " + lines[n].getLineClass());
		}
		
		for (Mixer.Info mi : AudioSystem.getMixerInfo()) {
			Mixer m = AudioSystem.getMixer(mi);
			System.out.println(mi.getDescription());
			//Line.Info[] infos2 = AudioSystem.getTargetLineInfo(mi);
			//for (int i = 0; i < infos2.length; i++)
		//	{
			//	System.out.println(infos[i].toString());
			//}
		}
			*/
		
		if (updateFFT){
			this.startTime=startTime;
			this.endTime=endTime;
		
			double chunks=(endTime-startTime)/20000.0;
			System.out.println("Sampling FFT");
			calculateSampleFFT();

			System.out.println("Chunky FFT");
			chunkyFFT(chunks);
			System.out.println("Done FFT");
		/*
		int pframe=frame;
		float[][]o1=new float[ny][anx];
		for (int i=0; i<ny; i++){
			for (int j=0; j<anx; j++){
				o1[i][j]=out[i][j];
			}
		}
		frame=framePad/4;
		makeWindow(frame);
		chunkyFFT(chunks);
		float[][]o2=new float[ny][anx];
		for (int i=0; i<ny; i++){
			for (int j=0; j<anx; j++){
				o2[i][j]=out[i][j];
			}
		}
		frame=1+(framePad/8);
		makeWindow(frame);
		chunkyFFT(chunks);
		float[][]o3=new float[ny][anx];
		for (int i=0; i<ny; i++){
			for (int j=0; j<anx; j++){
				o3[i][j]=out[i][j];
			}
		}
		frame=pframe;
		makeWindow(frame);
		for (int i=0; i<ny; i++){
			for (int j=0; j<anx; j++){
				out[i][j]=(float)Math.pow(o1[i][j]*o2[i][j]*o3[i][j], 1/3.0);
				//if (o1[i][j]<o2[i][j]){out[i][j]=o1[i][j];}
				//if (o3[i][j]<out[i][j]){out[i][j]=o3[i][j];}
			}
		}
		*/
			System.out.println("Dyn Equal");
			if (dynEqual>0){
				dynamicEqualizer();
			}
			System.out.println("Dyn Range");
			dynamicRange();
		//denoiser();
		//float maxCh=10f;
		//out=medianFilterXD(10, 0.45f);
			Matrix2DOperations m2o=new Matrix2DOperations();
		
			if (noiseRemoval>0){
				out=m2o.medianFilterNR(noiseLength1, 0.25f, noiseRemoval, out, 0);
				out=m2o.medianFilterNR1(noiseLength2, 0.75f, 0, out, 0);
			}
		//out=m2o.accentuateLines(out, 2, 1);
			System.out.println("Dereverb");
			if (echoComp>0){
			
			
			
			//out=m2o.accentuateLines(out, 5, 10);
			
			//float[][] out1=m2o.medianFilterNR2(20, 0.25f, 1.5f, out, 0);
			//out1=m2o.matrixMax(out1,  0);
			//out=m2o.matrixAdd(out1, out);
			
			//float[][] out3=m2o.medianFilterNR(100, 0.25f, 10, out, 0);
			//out=m2o.matrixMax(out1, out2);
			//out=m2o.matrixMax(out2, out3);
			//out=out2;
				int noisePasses=1;
			
			//float[][] ed=m2o.edgeDetection(1, false, out);
			//out=m2o.fillDetects(out, ed, 15, 0, 1);
			
			
				for (int i=0; i<noisePasses; i++){
					float[][] o=echoAverager2();
					out=m2o.matrixSubtract(out, o);
				//out=m2o.matrixSubSameMax(out, o);
				//echoAverager();
				}
			
			
			
			//out=m2o.medianFilterNR(20, 0.25f, 10, out, 0);
			}
			System.out.println("Equalize");
			equalize();	
			System.out.println("FInished");
		}
	}
	
	/**
	 * This organizes the calculation of a wave form representation of the sound
	 * @param startTime
	 * @param endTime
	 * @param steps
	 */
	public void makeAmplitude(int startTime, int endTime, int steps){
		int s=(int)Math.round(startTime*step);
		int e=(int)Math.round(endTime*step);
		int overSize=overallSize;
		
		int adStartTime=s-rangef;
		if (adStartTime<0){adStartTime=0;}
		int adEndTime=e+rangef;
		if (adEndTime>overSize){adEndTime=overSize;}
		
		parseSound(adStartTime*stereo, adEndTime*stereo);
		filter(s-adStartTime, e-s);
		
		if (steps<data.length){
			
			double r=(data.length-0.0)/(steps-0.0);
		
			envelope=new float[steps][2];
			for (int i=0; i<steps-1; i++){
				
				int a=(int)Math.round(r*i);
				int b=(int)Math.round(r*(i+1));
				float maxAmp=-1000;
				float minAmp=1000;
				//System.out.println(data.length+" "+a+" "+b+" "+r+" "+i+" "+steps);
				for (int j=a; j<=b; j++){
					if (data[j]>maxAmp){
						maxAmp=data[j];
					}
					if (data[j]<minAmp){
						minAmp=data[j];
					}
				}
				envelope[i][0]=maxAmp;
				envelope[i][1]=minAmp;
			}
		}
		
		else{
			envelope=new float[data.length][1];
			for (int i=0; i<data.length; i++){
				envelope[i][0]=data[0];
			}
		}	
	}
	
	/**
	 * This was the beginning of an attempt to reconstitute sounds from a spectrogram. It didn't
	 * work!
	 */
	void reconstitute(){
		float[]row=new float[anx];
		float freq=0;
		float p2=(float)(Math.PI*2/sampleRate);
		int size=(int)(anx*dx*0.001*sampleRate);
		float score=0;
		float place=0;
		int pl1;		
		
		for (int i=0; i<ny; i++){
			freq=(float)((i+0.5)*dy);
			for (int j=0; j<size; j++){
				place=(float)(j*1000*sampleRate/dx);
				pl1=(int)Math.floor(place);
				place-=pl1;
				score=out[i][pl1]*(1-place)+out[i][pl1+1]*place;
				row[j]+=(float)(score*Math.sin(j*freq*p2));
			}
		}
	}
	
	/**
	 * First of several preparatory steps for making an FFT. This calculates basic parameters
	 * of the spectrogram and calculates various look up tables etc.
	 */
	public void setFFTParameters(){
		
		//DataLine.Info dataLineInfo = (DataLine.Info) lineInfo;
		
		//if (frameSize==1){frameSize=2;}
		sampRate=sampleRate;
		//step=(int)Math.round(timeStep*sampRate*0.001);	//step is the time-step of the spectrogram expressed in samples
		step=timeStep*sampRate*0.001;
		System.out.println(timeStep+" "+step);
		
		frame=(int)Math.round(frameLength*sampRate*0.001);	//frame is the frame-size of the spectrogram expressed in samples
		
		overlap=1-(timeStep/frameLength);
		
		framePad=2;
		int p2=1;
		while (framePad<frame){p2++; framePad=(int)Math.pow(2, p2);}
		framePad=(int)Math.pow(2, p2+2);								//framePad is the frame-size of the spectrogram, padded out to the next integer power of 2
		makeWindow(frame);
		makeTrigTables(framePad);
		makeBitShiftTables(framePad);
		filtl=makeFilterBank(frequencyCutOff, 1);
		filtu=makeFilterBank(0.9*(sampRate/2.0), 0);
		dy=sampRate/(framePad+0.0);
		dx=timeStep;
		System.out.println(songID+" "+name);
		overallSize=(rawData.length/(frameSize));
		overallLength=(int)Math.round(overallSize/(sampRate*0.001));
		
		double start=0;
		ny=(int)Math.floor(maxf/dy);
		//Adjust maxf if it lies outside possible values for the sound file.
		if (ny>framePad){
			ny=framePad;
			maxf=(int)Math.floor(ny*dy);
		}
		nx=0;
		while (start+frame<overallSize){start+=step; nx++;}
		System.out.println("Spect params: "+nx+" "+overallSize+" "+overallLength+" "+stereo+" "+frameSize+" "+sampRate+" "+step+" "+frame+" "+dy+" "+ny+" "+framePad);
	}
	
	/**
	 * Second preparatory step for the FFT. This creates the float[][] that the spectrogram
	 * will be stored in
	 * @param dist a measure of length
	 */
	public void  setFFTParameters2(int dist){
		double start=0;
		anx=0;
		while (start+frame<dist*step+frame){start+=step; anx++;}
		if (updateFFT){
			out=null;
			out1=null;
			out=new float[ny][anx];
			out1=new float[ny][anx];
		}
		System.out.println("out dims: "+ny+" "+anx);
	}
	
	/**
	 * A third preparatory step for the FFT. This is no longer used- - deprecated
	 * @param newny
	 * @return an int containing ny
	 */
	public int setFFTParameters3(int newny){
		sampRate=sampleRate;
		dy=maxf/(newny+0.0);
		framePad=2;
		int p2=1;
		while (sampRate/(framePad+0.0)>dy){p2++; framePad=(int)Math.pow(2, p2);}
		framePad=(int)Math.pow(2, p2-1);
		dy=sampRate/(framePad+0.0);
		ny=(int)Math.floor(maxf/dy);
		if (frame>framePad){
			frame=framePad;
			frameLength=(int)Math.round(frame/(sampRate*0.001));
		}
		makeWindow(frame);
		makeTrigTables(framePad);
		makeBitShiftTables(framePad);
		return ny;
	}
	
	/**
	 * This creates a new AudioFormat object based on details of the format of the stored
	 * sound
	 */
	public void makeAudioFormat(){
		af=new AudioFormat((float)sampleRate, ssizeInBits, stereo, signed, bigEnd);
	}	
	
	/**
	 * This method splits a stereo track into two, and keeps one track
	 * @param side which track to keep (1=left, 2=right)
	 */
	public void parseSingle(int side){
		int n=rawData.length/2;
		byte[] rd=new byte[n];
		
		int q=ssizeInBits/8;
		System.out.println("Bytes: "+q);
		//int p=rawData.length;

		
		int ii=0;
		int k=0;
		if (side==2){k=q;}
		for (int i=0; i<n/q; i++){
			
			for (int j=0; j<q; j++){
				
				rd[ii]=rawData[k];
				//if (i<1000){
					//System.out.println("PARSING: "+i+" "+j+" "+ii+" "+k+" "+rawData[k]);
				//}
				ii++;
				k++;
			}
			k+=q;
		}
		rawData=rd;
	}
	
	/**
	 * This method splits a stereo track into two, and keeps one track
	 * @param side which track to keep (1=left, 2=right)
	 */
	public void parseMerge(){
		int n=rawData.length/2;
		byte[] rd=new byte[n];
		
		int q=ssizeInBits/8;
		System.out.println("Bytes: "+q);
		//int p=rawData.length;

		
		int ii=0;
		int k=0;
		for (int i=0; i<n/q; i++){
			
			for (int j=0; j<q; j++){
				rd[ii]=(byte)((rawData[k]+rawData[k+q])/2);
				ii++;
				k++;
			}
			k+=q;
		}
		rawData=rd;
	}
	
	
	/**
	 * This is a crude method to try to make a stereo copy of a sound. Is not used.
	 */
	void makeStereoCopy(){
		
		stereoRawData=new byte[rawData.length*2];
		for (int i=0; i<rawData.length; i++){
			stereoRawData[i*2]=rawData[i];
			stereoRawData[i*2+1]=rawData[i];
		}
	}
	
	/**
	 * This method makes a stereo copy of a mono sound file. It is used on playback of a mono file
	 * @param ssizeInBits sample size in bits
	 * @param channel number of channels
	 * @param signed is the sound format signed?
	 * @param resample should it resample the sound?
	 */
	void makeStereoCopy(int ssizeInBits, int channel, boolean signed, boolean resample){
		
		int q=ssizeInBits/8;
		System.out.println("Bytes: "+q);
		int p=rawData.length*2;
		if (resample){p/=2;}
		stereoRawData=new byte[p];
		System.out.println("Size: "+rawData.length*2);
		byte silenceChar=-128;
		
		if (signed){
			silenceChar=0;
		}
		
		
		
		for (int i=0; i<p/(2*q); i++){
			int ii=i;
			if (resample){ii*=2;}
			for (int j=0; j<q; j++){
				
				if ((channel==0)||(channel==1)){				
					stereoRawData[i*q*2+j]=rawData[ii*q+j];
				}
				else{
					stereoRawData[i*q*2+j]=silenceChar;
				}
				if ((channel==0)||(channel==2)){
					stereoRawData[i*q*2+q+j]=rawData[ii*q+j];
				}
				else{
					stereoRawData[i*q*2+q+j]=silenceChar;
				}
			}
		}
	}
		
		
	
	/**
	 * This sets up the sound system for playback
	 */
	public void setUpPlayback(){
		try{
			//af=new AudioFormat((float)sampleRate, ssizeInBits, stereo, signed, bigEnd);
			float p=(float)(sampleRate/playbackDivider);
			boolean resample=false;
			if (p>48000){
				resample=true;
				p/=2;
			}
			
			
			af=new AudioFormat((float)p, ssizeInBits, 2, signed, bigEnd);
			
			System.out.println("Stereo: "+stereo);
							   
			
			if (stereo==1){
				makeStereoCopy(ssizeInBits, 0, signed, resample);
			}
			else{
				stereoRawData=rawData;
			}
			
			
			
			DataLine.Info   info = new DataLine.Info(SourceDataLine.class, af);
			
			//MixerChooser mc=new MixerChooser();
			//mc.getMixerInfo(info);
			
			if (line!=null){line.close();}
			line = null;
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(af);
			FloatControl fc=(FloatControl)line.getControl(FloatControl.Type.PAN);
			fc.setValue(1f);
			
			
			
			
		
			
		} 
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Here's the problem!");
			System.out.println(e);
			//System.exit(0);
		}//
	}
	
	/**
	 * This prepares the java sound system for playback
	 * @param mixer a Mixer object
	 */
	public void setUpPlayback(Mixer mixer){
		try{
			af=new AudioFormat((float)sampleRate, ssizeInBits, stereo, signed, bigEnd);
			
			
			Mixer.Info minfo=mixer.getMixerInfo();
			
			line=AudioSystem.getSourceDataLine(af, minfo);
			line.open(af);
			//line.start();
			
			
		} 
		catch (Exception e) {
			System.out.println(e);
			//System.exit(0);
		}//
	}
	
	/**
	 * This shuts down playback on quitting.
	 */
	public void shutDownPlayback(){
		try{
			if (line.isOpen()){
				line.flush();
				//line.stop();
				line.close();
			}
		}
		catch(Exception e){}
	}
	
	/**
	 * This method is called to arrange playback of a region of a song (normally the screen view)
	 * @param x start point for playback
	 * @param y end point for playback
	 */
	
	public void prepPlaybackScreen(int x, int y){
		int a=(int)(2*x*Math.round(dx*sampleRate*stereo*0.001));
		int b=(int)(2*y*Math.round(dx*sampleRate*stereo*0.001));
		System.out.println(a+" "+b+" "+sampleRate+" "+ssizeInBits+" "+stereo);
		playSound(a, b);
	}
	
	/**
	 * This method is called to arrange which parts of the song need to be played back
	 * @param syll
	 * @return a double[] with the transformed start and stopped locations.
	 */
	public double[] prepPlayback(int[] syll){
		
		int a=0;
		int b=0;
		
		if (ssizeInBits<=16){
			int p1=syll[0]-7;
			if (p1<0){p1=0;}
			int p2=syll[1]+7;
			if (p2>=overallLength){
				p2=overallLength-1;
			}
			a=(int)(p1*Math.round(sampleRate*stereo*0.002));
			b=(int)(p2*Math.round(sampleRate*stereo*0.002));
			
		}
		
		if(rawData.length<=b){
			b=a;
		}	
		
		playSound(a, b);
		
		double[] c={a/rawData.length, b/rawData.length};
		return c;
	}
	
	/**
	 * This method organizes playback of the song. It creates a PlaySound instance to actually
	 * carry out the playback.
	 * @param a
	 * @param b
	 */
	public void playSound (int a, int b, double pan){
		try{
			//af=new AudioFormat((float)sampleRate, ssizeInBits, stereo, signed, bigEnd);
			//DataLine.Info   info = new DataLine.Info(SourceDataLine.class, af);
			//line = null;
			//line = (SourceDataLine) AudioSystem.getLine(info);
			//line.open(af);
			//line.start();
			
			if ((line==null)||(!line.isOpen())){setUpPlayback();}
			
			line.start();
			
			Thread play=new Thread(new PlaySound(a, b));
			play.setPriority(Thread.MIN_PRIORITY);
			play.start();
		} 
		catch (Exception e) {
			System.out.println(e);
			System.exit(0);
		}//
	}
		
	
		
	/**
	 * This method organizes playback of the song. It creates a PlaySound instance to actually
	 * carry out the playbacl.
	 * @param a
	 * @param b
	 */
	public void playSound (int a, int b){
		try{
			//af=new AudioFormat((float)sampleRate, ssizeInBits, stereo, signed, bigEnd);
			//DataLine.Info   info = new DataLine.Info(SourceDataLine.class, af);
			//line = null;
			//line = (SourceDataLine) AudioSystem.getLine(info);
			//line.open(af);
			//line.start();
			
			if ((line==null)||(!line.isOpen())){setUpPlayback();}
			
			line.start();
			
			Thread play=new Thread(new PlaySound(a, b));
			play.setPriority(Thread.MIN_PRIORITY);
			play.start();
		} 
		catch (Exception e) {
			System.out.println(e);
			System.exit(0);
		}//
	}
	
	/**
	 * This internal class plays the sound file encoded by the Song.
	 * @author Rob
	 *
	 */
	class PlaySound extends Thread{
		
		int start, end;
		//byte data[] = new byte[10000];
		
		public PlaySound(int start, int end){
			this.start=start;
			this.end=end;
		}

		public void run(){
			//System.out.println(stereo+" "+bigEnd+" "+signed+" "+sampleRate);
			
			
			//int start2=start/(stereo*2);
			//int end2=end/(stereo*2);
			//double size=rawData.length;
			//double size2=rawData.length/(stereo*2.0);
			//byte[]  abData = new byte[EXTERNAL_BUFFER_SIZE];
			System.out.println(EXTERNAL_BUFFER_SIZE+" "+start+" "+end);
			try{
				
				
				
				line.write(stereoRawData, start*2, 2*(end-start));
				//int a=line.write(stereoRawData, start, (end-start));
			
				//while ((line.isOpen())&&(start+EXTERNAL_BUFFER_SIZE<end)){
				//	System.arraycopy(rawData, start, abData, 0, EXTERNAL_BUFFER_SIZE);
				//	int a=line.write(abData, 0, EXTERNAL_BUFFER_SIZE);
				//	start+=EXTERNAL_BUFFER_SIZE;
				//}
							}
			catch (Exception e) {
				e.printStackTrace();
				System.out.println("OWWW");
			}
		}
	}
	
	/**
	 * This is a method to stop playback of the sound 
	 */
	public void stopSound(){
		try{
			if (line.isOpen()){
				line.stop();
				line.flush();
				//line.close();
			}
		}
		catch (Exception e) {
			System.out.println("Stop playback error");
		}
	}
	
	/**
	 * Another part of the filter function. This calls fir up to twice: once
	 * a low-pass for aliases, then optionally a high-pass for frequency cut-off
	 * @param startd start point of signal
	 * @param endd end point of signal
	 */
	private void filter(int startd, int endd){
		int size=endd-startd;
		//System.out.println("SIZE: "+size+" "+endd+" "+startd);
		if (frequencyCutOff>0){
			float[] data1=fir(filtu, data);
			float[] data2=fir(filtl, data1);
			//for (int i=0; i<data.length; i++){
				//data1[i]=data2[i];
			//}

			data=new float[size];
			for (int i=0; i<size; i++){
				data[i]=data2[i+startd];
			}
		}
		else{
			float data1[]=new float[size];
			for (int i=0; i<size; i++){
				data1[i]=data[i+startd];
			}
			data=data1;
		}
		//else{
			//data=new float(size);
			//System.arr
		//}
	}

	/**
	 * Carries out simple convolution-based filtering
	 * @param startd start point in signal
	 * @param endd end point in signal
	 * @param filt filter bank (?terminology)
	 * @param d signal to be filtered
	 * @return a float[] of filtered signal
	 */
	private float[] fir(double[] filt, float[] d){
		///System.out.println(startd+" "+endd+" "+frequencyCutOff+" "+data.length);
		int i, j, k;
		int size=d.length;
		float[] data2=new float[size];
		for (i=0; i<size; i++){
			k=i-(rangef/2);
			for (j=0; j<rangef; j++){
				if ((k>=0)&&(k<size)){data2[i]+=(float)d[k]*filt[j];}
				k++;
			}
		}
		return data2;
	}
	
	/**
	 * This method makes filter banks for high/low-pass filters
	 * @param fco frequency cut-off
	 * @param type 1=high-pass, 0=low-pass
	 * @return a double[] containing the calculated filter bank
	 */
	double[] makeFilterBank(double fco, int type){
		int m=300;
		double M=m;
		rangef=m+1;
		double M2=m/2;
		//int m2=(int)M2;
		//double K=0;
		double fc=fco/sampRate;
		//System.out.println(fco+" "+fc+" "+sampRate);
		double one, two;
		double[] filt=new double[rangef];
		int i;
		for (i=0; i<rangef; i++){
			double p=Math.PI*(i-M2);
			one=Math.sin(2*p*fc)/p;
			if (type==1){
				one*=-1;
			}
			two=0.42-0.5*Math.cos((2*Math.PI*i)/M)+0.08*Math.cos((4*Math.PI*i)/M);
			filt[i]=one*two;
			//System.out.println(i+" "+filt[i]);
		}
		filt[(int)M2]=2*fc;
		if (type==1){
			filt[(int)M2]=1-2*fc;
		}
		//for (i=0; i<rangef; i++){K+=filt[i];}
		//for (i=0; i<rangef; i++){
		//	filt[i]/=K;
		//}
		//filt[m2]++;
		return filt;
	}
	
	
	/**
	 * This is the FFT method
	 * @param L indicates where in the signal to start from
	 */
	private void calculateFFT(int L){
		int L2=data.length;
		double[] datr=new double[framePad];
		double[] dati=new double[framePad];
		maxAmp=0;
		int istart=0;
		double start=0;
		int i=0;
		int py=ny;
		if (py>framePad){py=framePad;}				//py is calculated in case the user picks a max freq that is above the resolving power of the other spectrogram
		float tempr, tempi;							//settings. If that happens, the spectrogram only plots up to the max. resolving power.
		int a,b,c,d;
		
		FFTbase fft=new FFTbase();
		
		while(istart<L){
			//System.out.println(L+" "+start+" "+frame+" "+place);
			for (i=0; i<frame; i++){
				d=i+istart;
				if (d<L2){
					datr[i]=window[i]*data[d];				//This is where the window is applied
				}
				else{
					datr[i]=0;
				}
				dati[i]=0;
			}
			
			//System.out.println(datr.length+" "+dati.length+" "+frame+" "+framePad);
			
			
			double[] dat=fft.fft(datr, dati, true);
			
			
			for (i=0; i<py; i++){
				a=2*i;
				b=2*i+1;
				if (place<out1[i].length){
					out1[i][place]=(float)(dat[a]*dat[a]+dat[b]*dat[b]);						//This is the production of the power spectrum
					if (out1[i][place]>maxAmp){maxAmp=out1[i][place];}
					out[i][place]=out1[i][place];
				}
				
			}
			start+=step;
			istart=(int)Math.round(start);
			place++;
		}	
		datr=null;
	}
	
	
	/**
	 * This is the FFT method
	 * @param L indicates where in the signal to start from
	 */
	private void calculateFFTb(int L){
		int L2=data.length;
		float[] dat=new float[2*framePad];
		maxAmp=0;
		int istart=0;
		double start=0;
		int i=0;
		int py=ny;
		if (py>framePad){py=framePad;}				//py is calculated in case the user picks a max freq that is above the resolving power of the other spectrogram
		float tempr, tempi;							//settings. If that happens, the spectrogram only plots up to the max. resolving power.
		int a,b,c,d;
		
		while(istart<L){
			//System.out.println(L+" "+start+" "+frame+" "+place);
			for (i=0; i<frame; i++){
				d=i+istart;
				if (d<L2){
					dat[2*i]=window[i]*data[d];				//This is where the window is applied
				}
				else{
					dat[2*i]=0;
				}
				dat[2*i+1]=0;
			}
			for(i=0; i<counter2; i++) {
				tempr = dat[bitTab[i][0]];
				dat[bitTab[i][0]] = dat[bitTab[i][1]];				//This is the bit-shifting
				dat[bitTab[i][1]] = tempr;
			}
			for (i=0; i<counter; i++){
				a=places1[i];
				b=places2[i];
				c=places3[i];
				d=places4[i];
				tempr = trig1[i]*dat[c] - trig2[i]*dat[d];			//This is the rest of the FFT
				tempi = trig1[i]*dat[d] + trig2[i]*dat[c];				
				dat[c] = dat[a] - tempr;
				dat[d] = dat[b] - tempi;
				dat[a] += tempr;
				dat[b] += tempi;
			}
			for(i=0; i<framePad; ++i) {  
				dat[2*i] /= framePad;
				dat[2*i + 1] /= framePad;
			}
			for (i=0; i<py; i++){
				a=2*i;
				b=2*i+1;
				if (place<out1[i].length){
					out1[i][place]=dat[a]*dat[a]+dat[b]*dat[b];						//This is the production of the power spectrum
					if (out1[i][place]>maxAmp){maxAmp=out1[i][place];}
					out[i][place]=out1[i][place];
				}
				
			}
			start+=step;
			istart=(int)Math.round(start);
			place++;
		}	
		dat=null;
	}

	/**
	 * Makes a tonal signal of maximum amplitude to set a reference amplitude value in
	 * the spectrogram
	 */
	private void calculateSampleFFT(){		
		float[] dat=new float[2*framePad];
		
		int i=0;
		float tempr, tempi;					
		float calc;
		int a,b,c,d;
		
		int N=framePad;
		double adj=sampRate*2/(maxf);
		for (i=0; i<frame; i++){
			dat[2*i]=(float)(window[i]*Math.sin(i/adj));				//This is where the fake signal is made
			dat[2*i+1]=0;
		}
		for(i=0; i<counter2; i++) {
			tempr = dat[bitTab[i][0]];
			dat[bitTab[i][0]] = dat[bitTab[i][1]];				//This is the bit-shifting
			dat[bitTab[i][1]] = tempr;
		}
		for (i=0; i<counter; i++){
			a=places1[i];
			b=places2[i];
			c=places3[i];
			d=places4[i];
			tempr = trig1[i]*dat[c] - trig2[i]*dat[d];			//This is the rest of the FFT
			tempi = trig1[i]*dat[d] + trig2[i]*dat[c];				
			dat[c] = dat[a] - tempr;
			dat[d] = dat[b] - tempi;
			dat[a] += tempr;
			dat[b] += tempi;
		}


		for(i=0; i<N; ++i) {  
			dat[2*i] /= N;
			dat[2*i + 1] /= N;
		}
		maxPossAmp=0;
		for (i=0; i<ny; i++){
			//calc=(float)(Math.sqrt(dat[2*i]*dat[2*i]+dat[2*i+1]*dat[2*i+1]));			
			calc=(float)(dat[2*i]*dat[2*i]+dat[2*i+1]*dat[2*i+1]);						//This is the production of the power spectrum
			if (calc>maxPossAmp){maxPossAmp=calc;}
		}	
		dat=null;
	}

	/**
	 * This method produces look-up tables for the production of the spectrograms,
	 * optimizing slightly
	 * @param val
	 */
	private void makeTrigTables(int val){			
		val=val*2;
		double om2=2.0*Math.PI;
		int amt=2;
		counter=1;
		while (amt<val){amt*=2; counter++;}
		counter--;
		float[][] xrTab=new float[counter][];
		float[][] xiTab=new float[counter][];

		double sin, cos, xtemp, xr, xi;
		int M=2;
		for (int i=0; i<counter; i++){
		
			xrTab[i]=new float[M/2];
			xiTab[i]=new float[M/2];
			xr=1.0;
			xi=0.0;
			sin=Math.sin(om2/M);
			cos=Math.cos(om2/M)-1.0;
			for (int j=0; j<M/2; j++){
				xrTab[i][j]=(float)xr;
				xiTab[i][j]=(float)xi;
			
				xtemp = xr;
				xr = xr + xr*cos - xi*sin;
				xi = xi + xtemp*sin + xi*cos;
			}
			M*=2;
		}		
		
		
		int count=0;
		int a=1;
		int b=2;
		int c;
		int d=framePad*2;
		for (int i=0; i<counter; i++){
			c=b*2;
			for (int j=0; j<a; j++){
				for (int k=j*2; k<d; k+=c){
					count++;
				}
			}
		}
		places1=new int[count];
		places2=new int[count];
		places3=new int[count];
		places4=new int[count];
		trig1=new float[count];
		trig2=new float[count];
		a=1;
		b=2;
		float x,y;
		count=0;
		for (int i=0; i<counter; i++){
			c=b*2;
			for (int j=0; j<a; j++){
				x=xrTab[i][j];
				y=xiTab[i][j];
				for (int k=j*2; k<d; k+=c){
					places1[count]=k;
					places2[count]=k+1;
					places3[count]=k+b;
					places4[count]=k+b+1;
					trig1[count]=x;
					trig2[count]=y;
					count++;
				}
			}
			a=b;
			b=c;
		}
		counter=count;
	}
	
	/**
	 * This method produces look-up tables for figuring out how to do the bit-shifting in the
	 * FFT - optimizes FFT slightly
	 * @param N
	 */
	private void makeBitShiftTables(int N){			
		int i,j,k,tot;
		counter2=0;
		j=0;
		for(i=0; i<N-1; i++) {
			if (i<j){counter2++;}
			k = N/2;
			while (k <= j)  {
				j -= k;
				k >>= 1;
			}
			j += k;
		}
		
		bitTab=new int[counter2][2];
		tot=0;
		j=0;
		for(i=0; i<N-1; i++) {
			if (i<j){
				bitTab[tot][0]=2*i;
				bitTab[tot][1]=2*j;
				tot++;
			}
			k = N/2;
			while (k <= j)  {
				j -= k;
				k >>= 1;
			}
			j += k;
		}
		
	}
	
	public double calcG(double sigma, double x, double N){
		double N2=0.5*(N-1);
		double p=(x-N2)/(2*sigma);
		double G=Math.exp(-1*p*p);
		
		return G;
	}
	

	/**
	 * This method creates windowFunctions for the FFT
	 * @param N the index of the window
	 */
	private void makeWindow(int N){			//produces various windowing functions
		window=new float[N];
		if (windowMethod==2){
			System.out.println("Hamming window "+N);
			for (int i=0; i<N; i++){
				window[i]=(float)(1-(0.5+0.5*Math.cos((2*Math.PI*i)/(N-1.0))));
				//System.out.println(window[i]);
			}
		}
		if (windowMethod==3){
			System.out.println("Hanning window "+N);
			for (int i=0; i<N; i++){
				window[i]=(float)(1-(0.54+0.46*Math.cos((2*Math.PI*(i+1))/(N+1.0))));
				//System.out.println(window[i]);
			}
		}
		if (windowMethod==1){
			//int mid=frame/2;
			double sigma=0.4;
			double N12=0.5*(N-1);
			
			System.out.println("Gaussian window "+N);
			float max=0;
			float min=1;
			for (int i=0; i<N; i++){
				double mq=(i-N12)/(sigma*N12);
				window[i]=(float)(Math.exp(-0.5*mq*mq));
				if (window[i]>max){max=window[i];}
				if (window[i]<min){min=window[i];}
			}
			for (int i=0; i<N; i++){
				window[i]=(window[i]-min)/(max-min);
				//System.out.println(window[i]);
			}
				//window[i]=(float)(Math.exp((-1*(Math.PI*(i-mid))*(Math.PI*(i-mid)))/(N*N+0.0)));}
		}
		if (windowMethod==4){
			
			double sigma=0.14*N;
			System.out.println("confined Gaussian window");
			for (int i=0; i<N; i++){
				
				double Gn=calcG(sigma, i, N);
				double Gh=calcG(sigma, -0.5, N);
				double GnN=calcG(sigma, i+N, N);
				double GnmN=calcG(sigma, i-N, N);
				double Gnmh=calcG(sigma, N-0.5, N);
				double Gnmh2=calcG(sigma, -0.5-N, N);
				
				window[i]=(float)(Gn-((Gh*(GnN+GnmN))/(Gnmh+Gnmh2)));
				//System.out.println(window[i]);
			}
			
			
			
		}
		//for (int i=0; i<N; i++){
		//	System.out.println(i+" "+window[i]);
		//}
	}
	
	/**
	 * This method carries out a process of dynamic equalization, making each window of
	 * a certain length have at least one black point
	 */
	private void dynamicEqualizer(){			//a dynamic equalizer; key parameter (dynEqual) varies time-range over which spectrogram is normalized
		int dynEqual2=(int)(dynEqual/dx);
		if (dynEqual2>anx){dynEqual2=anx;}
		int range2=-1*dynEqual2+1;
		float max=0;
		int i,j, k;
		float[] tops=new float[anx];
		for (i=0; i<anx; i++){
			for (j=0; j<ny; j++){
				if (out[j][i]>tops[i]){tops[i]=out[j][i];}
			}
			//System.out.println(tops[i]);
		}
		for (i=0; i<anx; i++){
			max=0;
			for (j=range2; j<dynEqual2; j++){
				k=j+i;
				if ((k>=0)&&(k<anx)&&(tops[k]>max)){max=tops[k];}
			}
			if (max>0){
				max=maxAmp/max;				//I'm still not sure maxAmp is appropriate here!
				for (j=0; j<ny; j++){out[j][i]*=max;}
			}
		}
		tops=null;
	}
	
	/**
	 * This function adjusts spectrogram for dynamic range, leaving values that are to be
	 * coloured white in the spectrogram <0
	 */
	private void dynamicRange(){		//converts power spectrum into logarithmic, decibel scale (relative to maxPossAmp).
		//System.out.print("a");
		double logd=10/(Math.log(10));
		double maxC=dynRange-Math.log(maxPossAmp)*logd;
		
		if (relativeAmp){
			maxC=dynRange-Math.log(maxAmp)*logd;
		}
		int i,j;
		maxAmp2=0;
		for (i=0; i<ny; i++){
			for (j=0; j<anx; j++){
				if (out[i][j]>0){
					out[i][j]=(float)(Math.log(out[i][j])*logd+maxC);
				}
				//if (out[i][j]<0){out[i][j]=0;}
				if (out[i][j]>maxAmp2){maxAmp2=out[i][j];}
			}
		}
		//System.out.println("b");
	}	
	
	

	
	
	
	
	
	
	

	
	
	/**
	 * This is a dereverberation algorithm and represents my first version at such an algorith.
	 * Algorithm looks for maximum in previous echorange time-steps. 
	 */
	void echoAverager(){		//Echo removal algorithm. 
		float[][] out2=out;
		//float[][] out2=gaussianBlur(20);
		
		int echoRange2=(int)(echoRange/dx);
		int i,j,b,c;
		int aanx=anx-1;
		double score=0;
		float ec=(float)(echoComp);
				
		for (i=0; i<ny; i++){
		
			
			for (j=aanx; j>=0; j--){
				if ((j==aanx)||(out2[i][j+1]==score)){
					score=0;
					c=j-echoRange2;
					if (c<0){c=0;}
					for (b=c; b<=j; b++){
						if (out2[i][b]>score){score=out2[i][b];}
					}
				}
				else{							//this else bit is my clever attempt to speed things up: you track back progressively over a whole row; you only really search for the largest value
					b=j-echoRange2;				//over all of the previous echorange samples IF the maximum drops off the end.
					if ((b>=0)&&(out2[i][b]>score)){score=out2[i][b];}
				}
				out[i][j]-=(float)(ec*(score-out2[i][j]));
				if (j==0){
					out[i][j]=0;
				}
			}
		}
	}
	
	/**
	 * This is an algorithm for dereverberation in a spectrogram.
	 * Algorithm looks for maximum in previous echorange time-steps. 
	 * @return a float[][] of same size as out that has reduced reverberation.
	 */
	private float[][] echoAverager2(){		//Echo removal algorithm. 
		float[][] out2=out;

		int echoRange2=(int)(echoRange/dx);
		if (echoRange2>anx){echoRange2=anx;}
		
		//System.out.println("ECHO: "+echoRange+" "+echoRange2+" "+anx);
		
		double minWeight=5;
		double decayFactor=(minWeight)/(echoRange2+0.0);
		//System.out.println(decayFactor);
		int i,j,k;
		double[] buffer=new double[echoRange2];
		int position=0;
		double bestBuf;
		
		float[][]t=new float[ny][anx];
		for (i=0; i<ny; i++){
			//buffer=new double[echoRange2];
			for (k=0; k<echoRange2; k++){
				buffer[k]=-1000000000;
			}
			position=0;
			
			for (j=0; j<anx; j++){
				buffer[position]=out2[i][j];
				
				bestBuf=0;

				for (k=0; k<echoRange2; k++){	
					if (buffer[k]>bestBuf){
						bestBuf=buffer[k];
					}

					buffer[k]-=decayFactor;
				}
				bestBuf=echoComp*(bestBuf-out2[i][j]);
				
				if (bestBuf>0){
					t[i][j]=(float)bestBuf;
				}
				else{
					t[i][j]=0f;
				}
				position++;
				if (position==echoRange2){position=0;}
				
			}
		}
		return t;
	}
	
	/**
	 * this method takes the median regression through each point, along the x-axis of the spectrogram
	 * It could be used to assist with dereverberation, but it isn't at present
	 * @return a double[] indicating the degree of attentuation at each point after a signal
	 */
	public double[] sampleEchoDecays(){
		//
		
		int windowSize=(int)(0.5*echoRange/dx);
		//int windowSize=50;
		if (windowSize>anx){windowSize=anx;}
		int medLoc=windowSize/2;
		double xsum=0;
		double x2sum=0;
		for (int i=0; i<windowSize; i++){
			xsum+=i;
			x2sum+=i*i;
		}
		
		double xsum2=(xsum*xsum)/(windowSize+0.0);
		xsum/=windowSize*(-1.0);
		double den=1/(x2sum-xsum2);
		double[] results=new double[ny];
		
		int xwidth=anx-windowSize;
		double[] buffer=new double[xwidth];
		for (int i=0; i<ny; i++){
			for (int j=0; j<xwidth; j++){
				double ysum=0;
				double xysum=0;
				for (int k=0; k<windowSize; k++){
					float p=out[i][j+k];
					ysum+=p;
					xysum+=k*p;
				}
				buffer[j]=(xysum+(xsum*ysum))*den;	
			}
			Arrays.sort(buffer);
			results[i]=buffer[medLoc];
			System.out.println(results[i]);
		}
		return results;
		
		
		
		
	}
		
		
	
	
	
	/**
	 * This method carries out a process of dynamic equalization. 
	 */
	private void equalize(){
		maxDB=-10000;
		int i,j;
		int startx=0;
		if (echoComp>0){startx=(int)(echoRange/dx);}
		for (i=0; i<ny; i++){
			for (j=startx; j<anx; j++){
				if (out[i][j]>maxDB){maxDB=out[i][j];}
			}
		}
		if (setRangeToMax){
			dynMax=(float)(maxDB/dynRange);
		}
		//System.out.println("Done");
	}
	
	/**
	 * This method calculates gaps before and after elements in eleList
	 */
	public void calculateGaps(){
		float gapbefore=0f;
		float gapafter=0f;
		for (int i=0; i<eleList.size(); i++){
			Element ele=eleList.get(i);
			if (i==0){gapbefore=-10000f;}
			gapafter=-10000f;
			if (i<eleList.size()-1){
				Element ele2=eleList.get(i+1);
				gapafter=(float)(ele2.signal[0][0]*ele2.timeStep-ele.signal[ele.length-1][0]*ele.timeStep);
			}
			ele.timeBefore=gapbefore;
			ele.timeAfter=gapafter;
		}
	}
	
	
	/**
	 * This method sorts syllables and elements into chronological order
	 */
	public void sortSyllsEles(){
		int num=syllList.size();
		int [] dat;
		while (num>0){
			int min=10000000;
			int loc=0;
			for (int i=0; i<num; i++){
				dat=syllList.get(i);
				if (dat[0]<min){
					loc=i;
					min=dat[0];
				}
			}
			
			dat=syllList.get(loc);
			syllList.remove(loc);
			syllList.addLast(dat);
			num--;
		}
		num=eleList.size();
		while (num>0){
			double min=10000000;
			int loc=0;
			for (int i=0; i<num; i++){
				Element ele=eleList.get(i);
				double j=ele.signal[0][0]+0.5*(ele.signal[ele.signal.length-1][0]-ele.signal[0][0]);
				if (j<min){
					loc=i;
					min=j;
				}
			}
		
			Element ele=eleList.get(loc);
			eleList.remove(loc);
			eleList.addLast(ele);
			num--;
		}
		dat=null;
	}

	/**
	 * This method takes a set of syllables, and parses them into phrases.
	 * It searches for syllables that encompass other syllables, and makes them markers
	 * of phrases. It produces a linked list of phrases. Each item if this is an int[][].
	 * The first index represents the number of repeats of a syllable within a phrase, and the second indicates 
	 * which elements from eleList belong to that syllable.
	 * 
	 * This method imposes quite strict limits on patterns of hierarchical organization in song. It is
	 * used extensively by analysis methods, but it would be better to revise it to be more flexible.
	 */
	public void interpretSyllables(){
		phrases=new LinkedList<int[][]>();
		
		//syllList=new LinkedList();
		System.out.println(name);
		if (syllList.size()==0){
			//System.out.println("HERE WE ARE!");
			for (int i=0; i<eleList.size(); i++){
				Element ele=eleList.get(i);
				int[] sy={(int)((ele.begintime-1)*ele.timeStep), (int)((ele.begintime+ele.signal.length+1)*ele.timeStep)};
				//System.out.println(sy[0]+" "+sy[1]);
				syllList.add(sy);
			}
		}
		phraseId=new boolean[syllList.size()];
		for (int i=0; i<syllList.size(); i++){
			
			int[] syll1=syllList.get(i);
			
			boolean isPhrase=true;
			for (int j=0; j<syllList.size(); j++){
				int[] syll2=syllList.get(j);
				if ((j!=i)&&(syll1[0]>=syll2[0])&&(syll1[1]<=syll2[1])){
					isPhrase=false;
					j=syllList.size();
				}
			}
			//System.out.println(name+" "+i+" "+isPhrase);
			if (isPhrase){
				phraseId[i]=true;
				LinkedList<int[]> p=new LinkedList<int[]>();
				for (int j=0; j<syllList.size(); j++){
					int[] syll2=syllList.get(j);
					if ((j!=i)&&(syll2[0]>=syll1[0])&&(syll2[1]<=syll1[1])){
						p.add(syll2);
					}
				}
				if (p.size()==0){p.add(syll1);}
				int maxlength=0;
				for (int j=0; j<p.size(); j++){
					int[] s=p.get(j);
					//System.out.println(s[0]+" "+s[1]);
					int count=0;
					for (int k=0; k<eleList.size(); k++){
						Element ele=eleList.get(k);
						//System.out.println(ele.begintime+" "+ele.timeStep+" "+ele.signal.length);
						//System.out.println(i+" "+j+" "+k+" "+name+" "+individualName+" "+ele.begintime+" "+s[0]+" "+s[1]);
						double ml=(ele.begintime+(ele.signal.length/2))*ele.timeStep;
						
						
						if ((ml>s[0])&&(ml<=s[1])){
							count++;
						}
					}
					if (count>maxlength){maxlength=count;}
					int[] t=new int[count];
					count=0;
					for (int k=0; k<eleList.size(); k++){
						Element ele=eleList.get(k);
						double ml=(ele.begintime+(ele.signal.length/2))*ele.timeStep;
						if ((ml>s[0])&&(ml<=s[1])){
							t[count]=k;
							count++;
						}
					}
					p.remove(j);
					p.add(j, t);
				}
				
				int[][] eletable=new int[p.size()][maxlength];		//IMPORTANT! first element of phrase is number of repeats; second element is syllable length in elements
				for (int j=0; j<p.size(); j++){
					int[] s=p.get(j);
					
					if (s.length<maxlength){
						if (j==0){
							int b=0;
							for (int k=0; k<maxlength-s.length; k++){
								eletable[j][k]=-1;
								b++;
							}
							for (int k=0; k<s.length; k++){
								eletable[j][b]=s[k];
								b++;
							}
						}
						else{
							for (int k=0; k<s.length; k++){
								eletable[j][k]=s[k];
							}
							for (int k=s.length; k<maxlength; k++){
								eletable[j][k]=-1;
							}
						}
					}
					else{
						for (int k=0; k<s.length; k++){
							eletable[j][k]=s[k];
						}
					}
					
										
					
				}
				/*
				for (int j=0; j<eletable.length; j++){
					for (int k=0; k<eletable[j].length; k++){
						System.out.print(eletable[j][k]+" ");
					}
					System.out.println();
				}
				*/
				phrases.add(eletable);
				//System.out.println(phrases.size()+" "+eletable.length+" "+maxlength);
			}
		}
	}
	
	/**
	 * gets a phase matrix, indicating the support of a given input frequency for a 
	 * hypothesized pitch. i.e. integer multiples of fundamental frequencies are given 
	 * a large weight.
	 * @return a double[] representing phase
	 */
	public double[] getPhase(){
		return phase;
	}
	
	/**
	 * gets the minFreq parameter. This limits the minimum detectable frequency for pitch
	 * calculations
	 * @return an int (number of spectrogram rows) representation of minFreq
	 */
	public int getMinFreq(){
		return minFreq;
	}
	
	/**
	 * This method calculates the double[] phase object which is used in pitch calculation - both
	 * for pitch representation and for fundamental frequency estimation.
	 * This should only be called once upon initiation of a new spectrogram.
	 */
	public void makePhase(){
		
		double log2Adj=1/Math.log(2);
		double logMax=Math.log(ny-1)*log2Adj;
		double logMin=Math.log(minFreq)*log2Adj;
		double step=(logMax-logMin)/(ny+0.0);
		octstep=1/step;
		double[] logTransform=new double[ny];
		for (int i=0; i<ny; i++){
			logTransform[i]=Math.pow(2, logMin+(i*step));
		}

		phase=new double[ny*ny];

		int count=0;
		for (int j=0; j<ny; j++){			
						
			for (int i=0; i<ny; i++){
				
				double q=Math.cos(Math.PI*((i)/logTransform[j]));
				q=Math.pow(q, 2);
				if (i<logTransform[j]*0.5){q=0;}
				phase[count]=q;
	
				count++;
			}
		}
	}
	
	public void updateElements(){
		for (int i=0; i<eleList.size(); i++){
			Element ele=(Element)eleList.get(i);
			ele.begintime-=14;
			for (int j=0; j<ele.signal.length; j++){
				ele.signal[j][0]-=14;
			}
		}	
	}
	
	
	public void setEleGaps(){
		
		for (int i=0; i<eleList.size()-1; i++){
			Element ele1=eleList.get(i);
			Element ele2=eleList.get(i+1);
			ele2.setTimeBefore(ele1.getTimeAfter());	
		}	
	}
	
	/**
	 * This method calculates the pitch representation. Specifically, it calculates the
	 * number of available cores and creates instances of PitchCalculator to do the
	 * calculation
	 * @param unx the number of columns in the spectrogram. Not obvious why this needs to 
	 * be sent to the method.
	 * @return a float[][] pitch representation of the sound.
	 */
	public float[][] runPitchCalculator(int unx){
		int ncores=Runtime.getRuntime().availableProcessors();
		
		float[][] pout=new float[unx][ny];
		
		PitchCalculator ct[]=new PitchCalculator[ncores];
		
		
		int[] starts=new int[ncores];
		int[] stops=new int[ncores];
		for (int i=0; i<ncores; i++){
			starts[i]=i*(unx/ncores);
			stops[i]=(i+1)*(unx/ncores);
		}
		stops[ncores-1]=unx;
		//System.out.println("CORES USED: "+ncores);
		
		for (int i=0; i<ncores; i++){
			ct[i]=new PitchCalculator(starts[i], stops[i]);
			ct[i].setPriority(Thread.MIN_PRIORITY);
			ct[i].start();
		}
		
		try{
			for (int cores=0; cores<ncores; cores++){
				ct[cores].join();
			}
			for (int cores=0; cores<ncores; cores++){
				//System.out.println(cores);
				for (int i=ct[cores].start; i<ct[cores].end; i++){
					int ii=i-ct[cores].start;
					for (int j=0; j<ny; j++){
						pout[i][j]=ct[cores].pout[ii][j];
						/*
						int h=ny-j-1;
						int sh=ct[cores].out[ii][j];

						img.setRGB(i, h, colpal[sh]);
						*/
					}
				}
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return pout;
	}
	
	public boolean checkSong(Component parentComponent){
		
		LinkedList<Syllable> syls=new LinkedList<Syllable>();
		
		for (int i=0; i< syllList.size(); i++){
			int [] syl=syllList.get(i);
			Syllable sy=new Syllable(syl, i);
			sy.addFamily(syls, eleList);
			sy.checkMaxLevel();
			syls.add(sy);
		}		
		
		for (Syllable sy : syls){
			System.out.println("SYLLABLE: "+sy.id+" "+sy.maxLevel+" "+sy.eles.size()+" "+sy.parents.size()+" "+sy.children.size());
			
			
		}
		
		LinkedList<Integer> oe=checkOrphanElements();
		
		if (oe.size()>0){
			StringBuffer sb=new StringBuffer();
		
			for (Integer a: oe){
				sb.append(a+", ");
			}	
			
			String s="The following elements are orphans (belong to no syllable): " + sb.toString() +" do you want to add syllables for them?";
			
			int c=JOptionPane.showConfirmDialog(parentComponent, s, "Warning", JOptionPane.YES_NO_CANCEL_OPTION);
			
			if (c==JOptionPane.YES_OPTION){
				adoptOrphanedElements(oe, syls);
			}
			else if (c==JOptionPane.CANCEL_OPTION){
				return false;	
			}	
		}
		
		LinkedList<Syllable> os=checkChildlessSyllables(syls);
		
		if (os.size()>0){
			StringBuffer sb=new StringBuffer();
		
			for (Syllable a: os){
				sb.append((a.id+1)+", ");
			}	
			
			String s="The following syllables are childless (have no elements): " + sb.toString() +" do you want to delete them?";
			
			int c=JOptionPane.showConfirmDialog(parentComponent, s, "Warning", JOptionPane.YES_NO_CANCEL_OPTION);
			
			if (c==JOptionPane.YES_OPTION){
				deleteSyllables(os, syls);
			}
			else if (c==JOptionPane.CANCEL_OPTION){
				return false;	
			}	
		}
		
		LinkedList<Syllable> ls=checkSyllableLevels(syls);
		
		if (ls.size()>0){
			StringBuffer sb=new StringBuffer();
		
			for (Syllable a: ls){
				sb.append((a.id+1)+", ");
			}	
			
			String s="The following syllables have a hierarcical level > 2: " + sb.toString() +" do you want to delete them?";
			
			int c=JOptionPane.showConfirmDialog(parentComponent, s, "Warning", JOptionPane.YES_NO_CANCEL_OPTION);
			
			if (c==JOptionPane.YES_OPTION){
				deleteSyllables(ls, syls);
			}
			else if (c==JOptionPane.CANCEL_OPTION){
				return false;	
			}	
		}
		
		
		return true;
	}
	
	public LinkedList<Syllable> checkSyllableLevels(LinkedList<Syllable> syls){
		LinkedList<Syllable> levels=new LinkedList<Syllable>();
		
		for (Syllable sy: syls){
			if (sy.maxLevel>2){
				levels.add(sy);				
			}	
		}
		
		return levels;
	}
	
	public LinkedList<Syllable> checkChildlessSyllables(LinkedList<Syllable> syls){
		
		LinkedList<Syllable> childless=new LinkedList<Syllable>();
		
		for (Syllable sy: syls){
			if (sy.eles.size()==0){
				childless.add(sy);				
			}	
		}

		return childless;
	}
	
	public void deleteSyllables(LinkedList<Syllable> oe, LinkedList<Syllable> syls){
		
		LinkedList<int[]> rlist=new LinkedList<int[]>();
		for (Syllable sy : oe){
			rlist.add(syllList.get(sy.id));
			sy.remove();
		}
		
		syls.removeAll(oe);
		syllList.removeAll(rlist);

	}
	
	public LinkedList<Integer> checkOrphanElements(){
		
		LinkedList<Integer> orphans=new LinkedList<Integer>();
		
		for (int i=0; i<eleList.size(); i++){
			Element ele=eleList.get(i);
			
			if (ele.syls.size()==0){
				orphans.add(new Integer(i+1));
			}	
		}
		return orphans;
	}
	
	public void adoptOrphanedElements(LinkedList<Integer> oe, LinkedList<Syllable> syls){
		
		for (Integer a: oe){
			
			int b=a.intValue()-1;
			
			Element ele=eleList.get(b);
			int start=ele.getBeginTime();
			int end=ele.getLength()+start;
			int[] syl={start-1, end+1};
			Syllable sy=new Syllable(syl, syllList.size());
			syllList.add(syl);
			sy.addFamily(syls, eleList);
			sy.checkMaxLevel();
			syls.add(sy);	
		}
	
	}
	
	
	/**
	 * This internal class calculates the 'pitch' representation of sounds. By making
	 * it an internal class, it makes it possible to use multiple threads/cores
	 * @author Rob
	 *
	 */
	public class PitchCalculator extends Thread{
		
		int start, end;
		float[][] pout;
		double subsup=0.25;
		
		public PitchCalculator(int start, int end){
			this.start=start;
			this.end=end;	
			pout=new float[end-start][ny];
		}

		public void run(){
			int oct=(int)Math.round(octstep);
			double[] spectrum=new double[ny];
			double[] hscor=new double[ny];
			double maxHscor;
			double subSuppression=fundAdjust;
			for (int i=start; i<end; i++){
				double peakAmp=-1000;
				for (int j=0; j<ny; j++){
					spectrum[j]=out[j][i];
					if (spectrum[j]<0){
						spectrum[j]=0;
					}
				
					hscor[j]=0;
					if (spectrum[j]>peakAmp){
						peakAmp=spectrum[j];
					}
					spectrum[j]=Math.pow(spectrum[j], subSuppression);
				}

				int count=0;
				double sumxy=0;
				for (int j=0; j<ny; j++){
					sumxy=0;
					for (int k=0; k<ny; k++){
						sumxy+=phase[count]*spectrum[k];
						count++;
					}
					hscor[j]=sumxy;
				}
				maxHscor=-1000000;

				for (int j=0; j<ny-oct; j++){
					hscor[j]-=subsup*hscor[j+oct];
					if (hscor[j]<0.0001){hscor[j]=0.0001;}
				}
				for (int j=ny-oct; j<ny; j++){
					hscor[j]-=subsup*hscor[ny-1];
				}
				
				
				for (int j=0; j<ny; j++){
					hscor[j]=Math.pow(hscor[j], 2);
					if (hscor[j]>maxHscor){
						maxHscor=hscor[j];
					}
				}
				for (int j=0; j<ny; j++){
					hscor[j]=(hscor[j]/maxHscor);
				}
			
				if (maxHscor>0){
					for (int j=minFreq; j<ny; j++){
						
						hscor[j]*=peakAmp/(dynRange);					
					}
				}
				else{
					for (int j=0; j<ny; j++){
						hscor[j]=0;
					}
				}
				int ii=i-start;
				for (int j=minFreq; j<ny; j++){
					pout[ii][j]=(float)hscor[j];
				}
				/*
				for (int j=minFreq; j<ny; j++){
					double p=1-hscor[j];
					int q=(int)(p*255);
					if (q>255){q=255;}
					if (q<0){q=0;}
					out[ii][j]=q;
				}
				*/
			}
		}
	}

}
