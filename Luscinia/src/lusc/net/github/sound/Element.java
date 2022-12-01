package lusc.net.github.sound;

import java.util.LinkedList;

import lusc.net.github.analysis.BasicStatistics;
//
//  Element.java
//  Luscinia
//
//  Created by Robert Lachlan on 31/03/2006.
//  Copyright 2006 Robert Lachlan. All rights reserved.
//

/**
 * This is the object for elements, the smallest unit of sound measurement in Luscinia. Defined by
 * a region on a spectrogram, elements have a range of different parameters, a definition of their range in the spectrogram, and
 * a list of acoustic measurements. 
 * @author Rob
 *
 */

public class Element {

	String[] measurementLevels={"Peak frequency", "Mean frequency", "Median frequency", "Fundamental frequency", "Peak frequency change", "Mean frequency change",
			"Median frequency change", "Fundamental frequency change", "Harmonicity", "Wiener entropy", "Frequency bandwidth", "Amplitude", "Vibrato rate", "Vibrato amplitude"};

	
	String[] paramNames= {"Peak frequency", "Mean frequency", "Median frequency", "Fundamental frequency",
			"Peak frequency change", "Mean frequency change", "Median frequency change", "Fundamental frequency change",
			"Harmonicity", "Wiener entropy", "Bandwidth", "Amplitude", "Vibrato rate", "Vibrato amplitude", "Vibrato shape"
	};
	
	
	double timeStep=0;
	double frameLength=0;
	public int maxf=0;
	int windowMethod=0;
	double dynEqual=0;
	double dynRange=0;
	int echoRange=0;
	int id;
	double echoComp=0;
	double dy=0;
	double[][] measurements;
	double[][] measurements2;
	double[][] statistics;
	double[][] compressedMeasures;
	int[] compressedPoints;
	double[] powerSpectrum;
	double[] sd;
	int[][] signal;
	int[][] signal2;
	int length=0;
	float overallPeak1, overallPeak2, timelength, timeBefore, timeAfter, ta, tb, timeMin, timeMax, withinSyllableGap, betweenSyllableGap;
	int begintime;
	String name;
	
	double lowerQuartile=0;
	double upperQuartile=0;
	double upper95tile=0;
	double lower95tile=0;
	double maxFreq=0;
	double minFreq=0;
	double energyVariance=0;
	
	int songid=-1;
	
	LinkedList<Syllable> syls=new LinkedList<Syllable>();
	Syllable syl;
	
	double [][] cdata,cdataTemp;
	double[] cdataAmp;
	int[] cdataLoc;
	
	/**
	 * An empty constructor
	 */
	
	public Element(){}
	
	/**
	 * A complex constructor that takes measurements and details already made.
	 * @param song a Song object that supplies spectrographic parameters
	 * @param signal an int[][] that defines the location of the element in a spectrogram
	 * @param measurements a double[][] that defines arrays of a range of acoustic parameters
	 * @param powerSpectrum	a double[] that defines the energy summed through the element.
	 */
	
	public Element(Song song, int[][]signal, double[][] measurements, double[] powerSpectrum){
		timeStep=song.spectrogram.timeStep;
		frameLength=song.spectrogram.frameLength;
		maxf=song.spectrogram.maxf;
		windowMethod=song.spectrogram.windowMethod;

		dynEqual=song.spop.dynEqual;
		dynRange=song.spop.dynRange;
		
		echoRange=song.spop.echoRange;
		echoComp=song.spop.echoComp;
		dy=song.spectrogram.dy;
		this.signal=signal;
		this.measurements=measurements;
		this.powerSpectrum=powerSpectrum;
		this.songid=song.songID;
		length=signal.length;
		begintime=signal[0][0];
		
		
		
	}
	
	/**
	 * A constructor that creates a new Element by duplicating an old one.
	 * @param oldElement an Element object to duplicate
	 */
	
	public Element(Element oldElement){
	
		timeStep=oldElement.timeStep;
		frameLength=oldElement.frameLength;
		maxf=oldElement.maxf;
		windowMethod=oldElement.windowMethod;
		dynEqual=oldElement.dynEqual;
		dynRange=oldElement.dynRange;
		
		echoRange=oldElement.echoRange;
		echoComp=oldElement.echoComp;
		dy=oldElement.dy;
		length=oldElement.signal.length;
		overallPeak1=oldElement.overallPeak1;
		overallPeak2=oldElement.overallPeak2;
		timelength=oldElement.timelength;
		timeBefore=oldElement.timeBefore;
		timeAfter=oldElement.timeAfter;
		begintime=oldElement.begintime;
		
		withinSyllableGap=0;
		betweenSyllableGap=0;
		
		measurements=new double[length+5][];
		signal=new int[length][];
		for (int i=0; i<length+5; i++){
			measurements[i]=new double[oldElement.measurements[i].length];
			System.arraycopy(oldElement.measurements[i], 0, measurements[i], 0, oldElement.measurements[i].length);
		}
		for (int i=0; i<length; i++){
			signal[i]=new int[oldElement.signal[i].length];
			System.arraycopy(oldElement.signal[i], 0, signal[i], 0, oldElement.signal[i].length);
		}
		powerSpectrum=new double[oldElement.powerSpectrum.length];
		System.arraycopy(oldElement.powerSpectrum, 0, powerSpectrum, 0, powerSpectrum.length);
		
		int tmax=-100;
		int tmin=1000000;
		
		for (int j=0; j<length; j++){
			if (signal[j][0]>tmax){tmax=signal[j][0];}
			if (signal[j][0]<tmin){tmin=signal[j][0];}
		}
		
		timelength=(float)((tmax-tmin)*timeStep);
		timeMax=(float)(tmax*timeStep);
		timeMin=(float)(tmin*timeStep);
	}
	
	/**
	 * A constructor that attempts to create a new Element by merging two old ones.
	 * @param ele1 First Element object
	 * @param ele2 Second Element object.
	 */
	
	
	public Element(Element ele1, Element ele2){
		
		timeStep=ele1.timeStep;
		frameLength=ele1.frameLength;
		maxf=ele1.maxf;
		windowMethod=ele1.windowMethod;
		dynEqual=ele1.dynEqual;
		dynRange=ele1.dynRange;
		
		echoRange=ele1.echoRange;
		echoComp=ele1.echoComp;
		dy=ele1.dy;
		length=ele1.signal.length+ele2.signal.length;
		overallPeak1=ele1.overallPeak1;
		overallPeak2=ele1.overallPeak2;
		timelength=ele1.timelength;
		timeBefore=ele1.timeBefore;
		timeAfter=ele1.timeAfter;
		begintime=ele1.begintime;
		
		measurements=new double[length][];
		signal=new int[length][];
		for (int i=0; i<ele1.length; i++){
			measurements[i]=new double[ele1.measurements[i+5].length];
			System.arraycopy(ele1.measurements[i+5], 0, measurements[i], 0, ele1.measurements[i+5].length);
		}
		for (int i=0; i<ele2.length; i++){
			measurements[i+ele1.length]=new double[ele2.measurements[i+5].length];
			System.arraycopy(ele2.measurements[i+5], 0, measurements[i+ele1.length], 0, ele2.measurements[i+5].length);
		}
		
		
		for (int i=0; i<ele1.length; i++){
			signal[i]=new int[ele1.signal[i].length];
			System.arraycopy(ele1.signal[i], 0, signal[i], 0, ele1.signal[i].length);
		}
		for (int i=0; i<ele2.length; i++){
			signal[i+ele1.length]=new int[ele2.signal[i].length];
			System.arraycopy(ele2.signal[i], 0, signal[i+ele1.length], 0, ele2.signal[i].length);
		}
		
		powerSpectrum=new double[ele1.powerSpectrum.length];
		
		for (int i=0; i<powerSpectrum.length; i++){
			powerSpectrum[i]=ele1.powerSpectrum[i]+ele2.powerSpectrum[i];
		}
		
		calculateStatistics();
	}
	
	/**
	 * A constructor that creates a new Element by merging an array of Elements.
	 * @param eleArray an array of elements
	 * e.g. the length of the new Element
	 */
	
	public Element(Element[] eleArray){
		mergeMeasurements(eleArray);
		
		timeBefore=eleArray[0].getTimeBefore();
		timeAfter=eleArray[eleArray.length-1].getTimeAfter();
		begintime=eleArray[0].begintime;
		Element re=eleArray[0];
		timeStep=re.timeStep;
		frameLength=re.frameLength;
		maxf=re.maxf;
		windowMethod=re.windowMethod;
		
		dynEqual=re.dynEqual;
		dynRange=re.dynRange;
		
		echoRange=re.echoRange;
		echoComp=re.echoComp;
		dy=re.dy;
		
		this.songid=re.songid;
	}
	
	/**
	 * A constructor that creates a new Element by merging an array of Elements.
	 * @param eleArray an array of elements
	 * @param average if true, various parameters are averaged, if false, they are not
	 * e.g. the length of the new Element
	 */
	
	public Element(Element[] eleArray, boolean average){
		
		double n=eleArray.length;
		double tbn=0;
		double tan=0;
		statistics=new double[19][9];
		
		powerSpectrum=new double[eleArray[0].powerSpectrum.length];
		
		
		
		timeMax=-100000000;
		timeMin=100000000;
		
		if (!average){
			
			timeBefore=eleArray[0].timeBefore;
			timeAfter=eleArray[eleArray.length-1].timeAfter;
			begintime=eleArray[0].begintime;
			
			
			for (int i=0; i<19; i++){
				statistics[i][3]=-1000000000;
				statistics[i][4]=1000000000;
			}
			
			withinSyllableGap=0;
			for (int i=0; i<n-1; i++){
				withinSyllableGap+=eleArray[i].timeAfter;
			}
			
			betweenSyllableGap=0;
			double a=0;
			if (timeBefore>-10000){
				betweenSyllableGap+=timeBefore;
				a++;
			}
			if (timeAfter>-10000){
				betweenSyllableGap+=timeAfter;
				a++;
			}
			if (a==0){betweenSyllableGap=-10000;}
			else{
				betweenSyllableGap/=a;
			}
		}
		
		
		for (int i=0; i<n; i++){
			
			timeStep+=eleArray[i].timeStep;
			frameLength+=eleArray[i].frameLength;
			maxf+=eleArray[i].maxf;
			windowMethod+=eleArray[i].windowMethod;
			dynEqual+=eleArray[i].dynEqual;
			dynRange+=eleArray[i].dynRange;
		
			echoRange+=eleArray[i].echoRange;
			echoComp+=eleArray[i].echoComp;
			dy+=eleArray[i].dy;
			length+=eleArray[i].length;
			overallPeak1+=eleArray[i].overallPeak1;
			overallPeak2+=eleArray[i].overallPeak2;
			timelength+=eleArray[i].timelength;
			
			if (average){
				withinSyllableGap+=eleArray[i].withinSyllableGap;
				betweenSyllableGap+=eleArray[i].betweenSyllableGap;
				if (eleArray[i].timeBefore>-10000){
					timeBefore+=eleArray[i].timeBefore;
					tbn++;
				}
				if (eleArray[i].timeAfter>-10000){
					timeAfter+=eleArray[i].timeAfter;
					tan++;
				}
			}
			System.out.println(i+" "+eleArray[i].timeMax+" "+eleArray[i].timeMin);
			if (eleArray[i].timeMax>timeMax){
				timeMax=eleArray[i].timeMax;
				
			}
			if (eleArray[i].timeMin<timeMin){
				timeMin=eleArray[i].timeMin;
			}
			
			begintime+=eleArray[i].begintime;
			for (int j=0; j<19; j++){
				if (!average){
				
					for (int k=0; k<3; k++){
						statistics[j][k]+=eleArray[i].statistics[j][k]*eleArray[i].length;
					}
				
					for (int k=7; k<9; k++){
						statistics[j][k]+=eleArray[i].statistics[j][k]*eleArray[i].length;
					}
				
					if (eleArray[i].statistics[j][3]>statistics[j][3]){
						statistics[j][3]=eleArray[i].statistics[j][3];
						statistics[j][5]=eleArray[i].statistics[j][5];
					}
					if (eleArray[i].statistics[j][4]<statistics[j][4]){
						statistics[j][4]=eleArray[i].statistics[j][4];
						statistics[j][6]=eleArray[i].statistics[j][6];
					}
				}
			
				else{
					for (int k=0; k<9; k++){
						statistics[j][k]+=eleArray[i].statistics[j][k];
					}
				}
			}
			
			for (int j=0; j<powerSpectrum.length; j++){
				powerSpectrum[j]+=eleArray[i].powerSpectrum[j];
			}
			
		}
		timeStep/=n;
		frameLength/=n;
		maxf/=n;
		windowMethod/=n;
		dynEqual/=n;
		dynRange/=n;
		echoRange/=n;
		echoComp/=n;
		dy/=n;
		
		overallPeak1/=n;
		overallPeak2/=n;
		if (!average){
			timelength=timeMax-timeMin;
			System.out.println("TIME LENGTH IS: "+timelength+" "+timeMax+" "+timeMin);
		}
		if (average){
			if (tbn>0){
				timeBefore/=tbn;
			}
			else{
				timeBefore=-10000;
			}
			if (tan>0){
				timeAfter/=tan;
			}
			else{
				timeAfter=-10000;
			}
			
			timelength/=n;
			begintime/=n;
			
			withinSyllableGap/=n;
			betweenSyllableGap/=n;
			length/=n;
		}
		
		if (average){
			for (int j=0; j<19; j++){
				for (int k=0; k<9; k++){
					statistics[j][k]/=n;
				}
			}
		}
		else{
			for (int j=0; j<19; j++){
				for (int k=0; k<3; k++){
					statistics[j][k]/=length;
				}
				for (int k=7; k<9; k++){
					statistics[j][k]/=length;
				}
				//statistics[j][6]=eleArray[0].statistics[j][6];
				//statistics[j][7]=eleArray[eleArray.length-1].statistics[j][6];
			}
		}
			
		
			
		calculatePowerSpectrumStats();
	}
	
	/**
	 * This method returns a String of Element details that can be written to the database
	 * @param b a separator String supplied by the DBC.
	 * @return a long String of semi-composed sql
	 */
	
	public String getDBStamp(String b){
		String v=overallPeak1+b+overallPeak2+b+begintime+b+timelength+b+timeBefore+b+timeAfter+b+timeStep+b+frameLength+b+dy+b+maxf+b+echoComp+b+echoRange+b+dynRange+b+dynEqual+")";
		return v;
	}
	
	/**
	 * This method returns the location of the element in a spectrogram as an int[][].
	 * The first index should be even, and details start and stop locations of the 
	 * element in the frequency domain (min freq, max freq pairs). The second index is the length of the element 
	 * in the time domain. This allows fairly complex element forms to be stored. 
	 * @return an int[][] detailing the location of the element.
	 */
	
	public int[][] getSignal(){
		return signal;
	}
	
	/**
	 * This method sets the location of the element in a spectrogram. Not sure this is/should be
	 * used.
	 * @param a an int[][]
	 */
	
	public void setSignal(int[][] a){
		signal=a;
	}
	
	/**
	 * This method returns a 'power spectrum' of the element, estimated from the power of the region
	 * of the spectrogram indicated by the element.
	 * @return a double[] where each value represents power at a different frequency bin in the spectrum.
	 */
	
	public double[] getPowerSpectrum(){
		return powerSpectrum;
	}
	
	/**
	 * This method sets the power spectrum of the element in a spectrogram. Not sure this is/should be
	 * used.
	 * @param a a double[]
	 */
	
	public void setPowerSpectrum(double[] a){
		powerSpectrum=a;
		
		
		
		
		
	}
	
	
	public double checkDiff(double mf) {
		double mindiff=0;
		//System.out.println(measurements.length+" "+length+" "+measurements[0].length+" "+signal[0][0]+" "+signal[0][1]+" "+signal[0][2]);
		for (int i=0; i<length; i++) {
			double f1=mf-(dy*signal[i][1]);
			f1=f1-measurements[i+5][0];
			if (f1>mindiff) {mindiff=f1;}
			//System.out.println(i+" "+length+" "+maxf+" "+f1+" "+measurements[i][0]+" "+signal[i][0]+" "+signal[i][1]+" "+signal[i][2]);
		}
		return mindiff;
	}
	
	/** 
	 * This method gets the Elements measurements - a double[][] in which the first index refers 
	 * to different frequency, amplitude etc parameters, and the second refers to the length
	 * of the element in the time domain. The array thus traces out trajectories of the various
	 * parameters
	 * @return a double[][] of acoustic trajectories
	 */
	
	public double[][] getMeasurements(){
		return measurements;
	}
	
	/**
	 * This method sets the measurements of the element in a spectrogram. Not sure this is/should be
	 * used.
	 * @param a a double[][]
	 */
	
	public void setMeasurements(double[][] a){
		measurements=a;
	}
	
	/**
	 * This method gets the spectrogram frequency increment (in Hz) - the difference in 
	 * frequency between two rows of the spectrogram
	 * @see Song
	 * @return a double frequency measure.
	 */
	
	public double getDy(){
		return dy;
	}
	
	/**
	 * This method sets the spectrogram frequency increment (in Hz) - the difference in 
	 * frequency between two rows of the spectrogram
	 * @see Song
	 * @param a a double frequency measure.
	 */
	
	public void setDy(double a){
		dy=a;
	}

	/**
	 * This method gets the maximum frequency of the spectrogram, as an integer (number of
	 * spectrogram rows)
	 * @return an int measure of number of spectrogram rows.
	 */
	
	public int getMaxF(){
		return maxf;
	}
	
	/** 
	 * This method gets the difference between two neighbouring columns of the spectrogram
	 * in ms. 
	 * @return a double time value.
	 */
	
	public double getTimeStep(){
		return timeStep;
	}
	
	/**
	 * This method sets the spectrogram time step
	 * @param a a double value for the time step
	 */
	
	public void setTimeStep(double a){
		timeStep=a;
	}
	
	/**
	 * This method gets the frame length of the spectrogram.
	 * @return a double value of the framelength (in ms)
	 */
	
	public double getFrameLength(){
		return frameLength;
	}
	
	/**
	 * This method sets the frame length of the spectrogram.
	 * @param a a double value for the framelength (in ms)
	 */
	
	public void setFrameLength(double a){
		frameLength=a;
	}
	
	/**
	 * This method sets the maximum frequency - the number of spectrogram rows
	 * @see Song
	 * @param a an integer value of the number of spectrogram rows.
	 */
	
	public void setMaxf(int a){
		maxf=a;
	}
	
	/**
	 * Gets the type of window method used, using a standard list of window method types for
	 * the spectrogram. @see Song
	 * @return an integer value for the type of window method
	 */
	
	public int getWindowMethod(){
		return windowMethod;
	}
	
	/**
	 * Sets the type of window method used, using a standard list of window method types for
	 * the spectrogram. @see Song
	 * @param a
	 */
	
	public void setWindowMethod(int a){
		windowMethod=a;
	}
	
	/**
	 * gets the dynamic equalization parameter setting used when the element was measured.
	 * (See @Song for more details).
	 * @return a double value for dynamic equalization.
	 */
	
	public double getDynEqual(){
		return dynEqual;
	}
	
	/**
	 * sets the dynamic equalization parameter used when the element was measured. @see Song for more details).
	 * @param a
	 */
	
	public void setDynEqual(double a){
		dynEqual=a;
	}
	
	/**
	 * gets the dynamic range parameter of the spectrogram when the element was measured
	 * @see Song
	 * @return a double value for the dynamic range
	 */
	
	public double getDynRange(){
		return dynRange;
	}
	
	/**
	 * sets the dynamic range parameter of the spectrogram when the element was measured
	 * @see Song
	 * @param a
	 */
	
	public void setDynRange(double a){
		dynRange=a;
	}
	
	/**
	 * gets the echo range parameter (dereverberation window) of the spectrogram when the
	 * element was measured
	 * @see Song
	 * @return an int value
	 */
	
	public int getEchoRange(){
		return echoRange;
	}
	
	/**
	 * sets the echo range parameter (dereverberation window) of the spectrogram when the
	 * element was measured
	 * @see Song
	 * @param a an int value
	 */
	
	public void setEchoRange(int a){
		echoRange=a;
	}
	
	/**
	 * gets the echo comp parameter (dereverberation amount) of the spectrogram when the
	 * element was measured
	 * @see Song
	 * @return a double value
	 */
	
	public double getEchoComp(){
		return echoComp;
	}
	
	/**
	 * sets the echo range parameter (dereverberation window) of the spectrogram when the
	 * element was measured
	 * @see Song
	 * @param a an int value
	 */
	
	public void setEchoComp(double a){
		echoComp=a;
	}
	
	/**
	 * gets the time (in ms) between this element and the previous one. If there were no
	 * previous elements, returns -1000. This isn't a great choice of default parameter...
	 * @return a float ms value
	 */
	
	public float getTimeBefore(){
		//System.out.println("Time before: "+timeBefore);
		return timeBefore;
	}
	
	/**
	 * sets the time (in ms) between this element and the previous one. If there were no
	 * previous elements, returns -1000. This isn't a great choice of default parameter...
	 * @param a a float ms value
	 */
	
	public void setTimeBefore(float a){
		//System.out.println("Time before: "+a);
		timeBefore=a;
	}
	
	/**
	 * gets the time (in ms) between this element and the subsequent one. If there were no
	 * subsequent elements, returns -1000. This isn't a great choice of default parameter...
	 * @return a float ms value
	 */
	
	public float getTimeAfter(){
		return timeAfter;
	}
	
	/**
	 * gets the time (in ms) between this element and the subsequent one. If there were no
	 * subsequent elements, returns -1000. This isn't a great choice of default parameter...
	 * @param a a float ms value
	 */
	
	public void setTimeAfter(float a){
		timeAfter=a;
	}
	
	/**
	 * gets a time parameter related to drawing element sketches for analysis displays.
	 * @see lusc.net.github.ui.statistics.DisplaySketches
	 * @return a time parameter float
	 */
	
	public float getTb(){
		return tb;
	}
	
	/**
	 * sets a time parameter related to drawing element sketches for analysis displays.
	 * @see lusc.net.github.ui.statistics.DisplaySketches
	 * @param a 
	 */
	
	public void setTb(float a){
		tb=a;
	}
	
	/**
	 * gets a time parameter related to drawing element sketches for analysis displays.
	 * @see lusc.net.github.ui.statistics.DisplaySketches
	 * @return a time parameter float
	 */
	
	public float getTa(){
		return ta;
	}
	
	/**
	 * sets a time parameter related to drawing element sketches for analysis displays.
	 * @see lusc.net.github.ui.statistics.DisplaySketches
	 * @param a 
	 */
	
	public void setTa(float a){
		ta=a;
	}
	
	/**
	 * gets the time within the spectrogram (in rows) at which the element began
	 * @return an int value
	 */
	
	public int getBeginTime(){
		return begintime;
	}
	
	/**
	 * sets the time within the spectrogram (in rows) at which the element began
	 * @param a
	 */
	
	public void setBeginTime(int a){
		begintime=a;
	}
	
	/** 
	 * This method returns the length of the element (in terms of measurement points)
	 * @return an int of the length
	 */
	
	public int getLength(){
		return length;
	}
	
	public int getEndTime(){
		return begintime+length;
	}

	/** 
	 * This method sets the length of the element (in terms of measurement points)
	 * @param a an int of the length
	 */
	
	public void setLength(int a){
		length=a;
	}
	
	/**
	 * This method gets the length of the element (in ms)
	 * @return a float value of element length of the element length
	 */

	public float getTimelength(){
		return timelength;
	}
	
	/**
	 * This method sets the length of the element (in ms)
	 * @param a a float value of element length of the element length
	 */
	
	public void setTimelength(float a){
		timelength=a;
	}
	
	/**
	 * This method gets the first measure of the overall peak frequency of the spectrogram
	 * in Hz
	 * @return a float value of frequency in Hz 
	 */
	
	public float getOverallPeak1(){
		return overallPeak1;
	}
	
	/**
	 * This method sets the first measure of the overall peak frequency of the spectrogram
	 * in Hz
	 * @param a a float value of frequency in Hz
	 */
	
	public void setOverallPeak1(float a){
		overallPeak1=a;
	}
	
	/**
	 * This method gets the second measure of the overall peak frequency of the spectrogram
	 * in Hz
	 * @return a float value of frequency in Hz 
	 */
	
	public float getOverallPeak2(){
		return overallPeak2;
	}
	
	/**
	 * This method sets the second measure of the overall peak frequency of the spectrogram
	 * in Hz
	 * @param a a float value of frequency in Hz
	 */
	
	public void setOverallPeak2(float a){
		overallPeak2=a;
	}
	
	/**
	 * This method gets the minimum frequency of the spectrogram in Hz
	 * @return minimum frequency in Hz
	 */
	
	public double getMinFreq(){
		return minFreq;
	}
	
	/**
	 * This method gets the maximum frequency of the spectrogram in Hz
	 * @return minimum frequency in Hz
	 */
	
	public double getMaxFreq(){
		return maxFreq;
	}
	
	/**
	 * This method gets the lower-quartile frequency of the spectrogram in Hz
	 * @return lower quartile frequency in Hz
	 */
	
	public double getLowerQuartile(){
		return lowerQuartile;
	}
	
	/**
	 * This method gets the upper-quartile frequency of the spectrogram in Hz
	 * @return upper quartile frequency in Hz
	 */
	
	public double getUpperQuartile(){
		return upperQuartile;
	}
	
	/**
	 * This method gets the lower-95%-ile frequency of the spectrogram in Hz
	 * @return lower-95%-ile frequency in Hz
	 */
	
	public double getLower95tile(){
		return lower95tile;
	}
	
	/**
	 * This method gets the upper-95%-ile frequency of the spectrogram in Hz
	 * @return upper-95%-ile frequency in Hz
	 */
	
	public double getUpper95tile(){
		return upper95tile;
	}
	
	/**
	 * This method gets the variance in the power spectrum
	 * @return ps variance
	 */
	
	public double getEnergyVariance(){
		return energyVariance;
	}
	
	/**
	 * This method gets a range of summary statistics for each acoustic measurement of
	 * the element. This is a [][] double with length 9 in the first index, corresponding to 
	 * the mean, median, variance, max, min, time of max, time of min, start and end values of each parameter. 
	 * @return a double[][] of statistics.
	 */
	
	public double[][] getStatistics(){
		return statistics;
	}
	
	/**
	 * returns the within syllable gap parameter - something that only applies if this element
	 * has been formed by joining together several elements. This is counter-intuitive.
	 * @return a double time value
	 */
	
	public double getWithinSyllableGap(){
		return withinSyllableGap;
	}
	
	/**
	 * returns the between syllable gap parameter - something that only applies if this element
	 * has been formed by joining together several elements. This is counter-intuitive
	 * @return a double time value
	 */
	
	public double getBetweenSyllableGap(){
		return betweenSyllableGap;
	}
	
	//public int[] getCompressedPoints(){
		//return compressedPoints;
	//}
	
	/**
	 * this method sets the id for the element from the database. Useful for re-writing the element
	 * (double check this)
	 * @param a
	 */
	
	public void setId(int a){
		id=a;
	}
	
	
	/**
	 * This method calculates a set of summary statistics for the measurements provided to
	 * the class. This is not a very elegant way to do things at present.
	 */
	
	public void calculateStatistics(){
		double results[][]=new double[length+5][15];
		double maxvals, minvals, timemax, timemin, avvals;
		overallPeak1=0;
		for (int j=0; j<15; j++){
			maxvals=-100000000;
			minvals=100000000;
			avvals=0;
			timemax=0;
			timemin=0;
			for (int k=0; k<length; k++){
				if (measurements[k][j]>maxvals){
					maxvals=measurements[k][j];
					timemax=k;
				}
				if (measurements[k][j]<minvals){
					minvals=measurements[k][j];
					timemin=k;
				}
				avvals+=measurements[k][j];
				results[k+5][j]=measurements[k][j];
			}
			if (j==11){
				overallPeak1=(float)measurements[(int)timemin][0];
			}
			avvals/=length+0.0;
			timemin*=timeStep;
			timemax*=timeStep;
			results[0][j]=maxvals;
			results[1][j]=minvals;
			results[2][j]=timemax;
			results[3][j]=timemin;
			results[4][j]=avvals;
		}
		measurements=null;
		measurements=results;
		
		int peakloc=0;
		double peaksize=-100000;
		for (int j=0; j<powerSpectrum.length; j++){
			if ((powerSpectrum[j]!=0)&&(powerSpectrum[j]>peaksize)){
				peaksize=powerSpectrum[j];
				peakloc=j;
			}
		}
		
		overallPeak2=(float)(peakloc*dy);
		
		
		int tmax=-100;
		int tmin=1000000;
		
		for (int j=0; j<length; j++){
			if (signal[j][0]>tmax){tmax=signal[j][0];}
			if (signal[j][0]<tmin){tmin=signal[j][0];}
		}
		
		timelength=(float)((tmax-tmin)*timeStep);
		timeMax=(float)(tmax*timeStep);
		timeMin=(float)(tmin*timeStep);
		begintime=signal[0][0];
		//System.out.println(overallPeak1+" "+overallPeak2);
	}
	
	/**
	 * This method extracts the summary statistics from the measurements array.
	 * It is a bit of an improvement over the above, but the whole is still a mess.
	 * It also calculates median and variance statistics.
	 */
	
	public void calculateStatisticsS(){
		
		calculateStatisticsAbsolute();
		
		statistics=new double[19][9];
		int a=measurements.length-1;
		double variance, b;
		double n=a-5;
		BasicStatistics bs=new BasicStatistics();
		
		for (int i=0; i<19; i++){
		
			statistics[i][0]=measurements[4][i];   //mean
			statistics[i][3]=measurements[0][i];	//max
			statistics[i][4]=measurements[1][i];	//min
			statistics[i][5]=measurements[2][i];	//timemax
			statistics[i][6]=measurements[3][i];	//timemin
			statistics[i][7]=measurements[5][i];	//start
			statistics[i][8]=measurements[a][i];	//end
	
			variance=0;
			double[] t=new double[measurements.length-5];
			for (int j=5; j<measurements.length; j++){
				b=measurements[4][i]-measurements[j][i];
				variance+=b*b;
				t[j-5]=measurements[j][i];
			}
			statistics[i][2]=variance/n;			//variance
			statistics[i][1]=bs.calculateMedian(t);	//median
	
		}
		
		int tmax=-100;
		int tmin=1000000;
		
		for (int j=0; j<length; j++){
			if (signal[j][0]>tmax){tmax=signal[j][0];}
			if (signal[j][0]<tmin){tmin=signal[j][0];}
		}
		
		timelength=(float)((tmax-tmin)*timeStep);
		timeMax=(float)(tmax*timeStep);
		timeMin=(float)(tmin*timeStep);
		begintime=signal[0][0];
	}
	
	/**
	 * This measure calculates statistics for measurements in their original units.
	 * Again, these three methods are a bit of a mess.
	 */
	
	public void calculateStatisticsAbsolute(){
		double results[][]=new double[length+5][19];
		double maxvals, minvals, timemax, timemin, avvals;
		for (int j=0; j<15; j++){
			for (int k=0; k<length+5; k++){
				results[k][j]=measurements[k][j];
			}
		}
		measurements=null;
		measurements=results;
		for (int j=0; j<4; j++){
			maxvals=-100000000;
			minvals=100000000;
			avvals=0;
			timemax=0;
			timemin=0;
			double v;
			int w=j+4;
			int x=j+15;
			for (int k=5; k<length+5; k++){
				v=Math.abs(measurements[k][w]-0.5);
			
				if (v>maxvals){
					maxvals=v;
					timemax=k;
				}
				if (v<minvals){
					minvals=v;
					timemin=k;
				}
				avvals+=v;
				measurements[k][x]=v;
			}
			avvals/=length+0.0;
			timemin*=timeStep;
			timemax*=timeStep;
			measurements[0][x]=maxvals;
			measurements[1][x]=minvals;
			measurements[2][x]=timemax;
			measurements[3][x]=timemin;
			measurements[4][x]=avvals;
		}
	}
	
	/**
	 * This method calculates various statistics to do with the distribution 
	 * of the power spectrum.
	 */
	
	public void calculatePowerSpectrumStats(){
		
		double tot=0;
		double average=0;
		for (int i=0; i<powerSpectrum.length; i++){
			tot+=powerSpectrum[i];
			average+=powerSpectrum[i]*i*dy;
		}
		average/=tot;
		
		double cumulative=0;
		boolean foundMin=false;
		energyVariance=0;
		for (int i=0; i<powerSpectrum.length; i++){

			if ((cumulative<tot*0.25)&&(cumulative+powerSpectrum[i]>=tot*0.25)){
				lowerQuartile=i+((tot*0.25-cumulative)/(powerSpectrum[i]));
			}
			if ((cumulative<tot*0.75)&&(cumulative+powerSpectrum[i]>=tot*0.75)){
				upperQuartile=i+((tot*0.75-cumulative)/(powerSpectrum[i]));
			}
			if ((cumulative<tot*0.05)&&(cumulative+powerSpectrum[i]>=tot*0.05)){
				lower95tile=i+((tot*0.05-cumulative)/(powerSpectrum[i]));
			}
			if ((cumulative<tot*0.95)&&(cumulative+powerSpectrum[i]>=tot*0.95)){
				upper95tile=i+((tot*0.95-cumulative)/(powerSpectrum[i]));
			}
			if ((!foundMin)&&(powerSpectrum[i]>0)){
				minFreq=i*dy;
				foundMin=true;
			}
			if (powerSpectrum[i]>0){
				maxFreq=i*dy;
			}
			energyVariance+=(average-i*dy)*(average-i*dy)*powerSpectrum[i];

			cumulative+=powerSpectrum[i];
		}
		energyVariance/=tot;
		lowerQuartile*=dy;
		upperQuartile*=dy;
		lower95tile*=dy;
		upper95tile*=dy;
	}
	
	public double[] getAverages(double[][] m, boolean logFrequencies){
		
		int n1=m.length;
		
		double[] out=new double[4];
		
		int a=11;
		
		for (int i=0; i<4; i++){
			double t=0;
			double u=0;
			for (int j=5; j<n1; j++){
				if (logFrequencies){
					//t+=Math.log(m[j][i])*m[j][a];
					t+=Math.log(m[j][i]);
				}
				else{
					//t+=m[j][i]*m[j][a];
					t+=m[j][i];
				}
				//u+=m[j][a];
				u++;
			}
			t/=u;
			out[i]=t;
			
			//System.out.println(i+" "+out[i]+" "+Math.log(m[4][i]));
			
		}
		return out;
	}
	
	public void compressElements(double reductionFactor, int minPoints, boolean logFrequencies, double slopeATanTransform){
		System.out.println("Started compression...");		
		
		
		
		//System.out.println("CAN I GET SYLS?"+syl.getID()+" "+syl.getNumSyllables());
		
		
		double[] averages=syl.calculateElementMeasurementAverages(logFrequencies);
		System.out.println(songid+" "+averages[3]);
		
		
		
		
		double[][] measu=getMeasurements();
		//double[] averages=getAverages(measu, logFrequencies);
				
		int s=getLength();
				
		int t=(int)Math.round(reductionFactor*s);
								
		if (t<minPoints){
			t=minPoints;
			reductionFactor=t/(s+0.0);
			if (s<minPoints){
				t=s;
				reductionFactor=1;
			}
		}
		double s2=s-1;
		
		cdata=new double[19][t];
		cdataTemp=new double[2][t];
		cdataLoc=new int[t];
		cdataAmp=new double[t];
		

		double[] count=new double[t];
		double diff=measu[0][11]-measu[1][11];
		double diff2=measu[0][11];
				
				
		for (int a=0; a<s; a++){
			int c=(int)Math.floor(a*reductionFactor+0.0000000001);
			if (c==t){c--;}		
			count[c]++;
			int ab=a+5;
					
			cdataTemp[0][c]+=getTimeStep()*a;
			cdataTemp[1][c]+=a/s2;
					
			for (int q=0; q<4; q++){
				if (logFrequencies){
					cdata[q][c]+=Math.log(measu[ab][q]);
				}
				else{
					cdata[q][c]+=measu[ab][q];
				}
			}
			for (int q=4; q<8; q++){
				double tt=Math.atan2(measu[ab][q], slopeATanTransform);
				cdata[q][c]+=tt;
			}
			for (int q=8; q<13; q++){
				cdata[q][c]+=measu[ab][q];
			}
			
			double ta=measu[ab][13];
			//ta=Math.log(Math.max(ta,1));
			ta=Math.sqrt(ta);
			cdata[13][c]+=ta;
						 
			if (getTimeAfter()!=-10000){
				cdata[14][c]+=getTimeAfter();
			}
			else{
				cdata[14][c]+=50;
			}
			for (int q=15; q<19; q++){
				if (logFrequencies){
					cdata[q][c]+=Math.log(measu[ab][q-15])-averages[q-15];
				}
				else{
					cdata[q][c]+=measu[ab][q-15]-averages[q-15];
				}
			}
			cdataAmp[c]+=(diff2-measu[ab][11])/diff;
			cdataAmp[c]+=0.01;
		}
				
		for (int a=0; a<t; a++){
			for (int b=0; b<2; b++){
				cdataTemp[b][a]/=count[a];
			}
			for (int b=0; b<19; b++){
				cdata[b][a]/=count[a];
			}
			cdataAmp[a]/=count[a];
			cdataLoc[a]=1;
		}
		//cdataLoc[t-1]=0;
		//System.out.println("Finished compression");
	}
	
	public void mergeMeasurements(Element[] ea){
		int n=ea.length;
		
		int a=0;
		
		for (int i=0; i<n; i++){
			if (ea[i]==null){System.out.println("ALERT2 "+i);}
			if (ea[i].cdata==null){System.out.println("ALTERT3 "+i);}
			a+=ea[i].cdata[0].length;
		}
		
		cdata=new double[19][a];
		cdataTemp=new double[2][a];
		cdataAmp=new double[a];
		cdataLoc=new int[a];
		
		double[][] xd=new double[19][a];
		double[][] xdt=new double[2][a];
		double[] xde=new double[a];
		int[] xdl=new int[a];
		
		//put values together in one index...
		a=0;
		
		double minstart=Double.MAX_VALUE;
		
		
		for (int i=0; i<n; i++){
			double startPos=ea[i].getBeginTime()*ea[i].getTimeStep();
			if (startPos<minstart){minstart=startPos;}
			
			for (int j=0; j<ea[i].cdata[0].length; j++){
				for (int k=0; k<19; k++){
					xd[k][a]=ea[i].cdata[k][j];
				}
				xdt[0][a]=ea[i].cdataTemp[0][j]+startPos;
				
				xdl[a]=i;
				//if (j==ea[i].cdata[0].length-1) {xdl[a]=-1;}
				xde[a]=ea[i].cdataAmp[j];
				a++;
			}
		}	
		for (int i=0; i<a; i++){
			xdt[0][i]-=minstart;
		}
		
		
		//sort values by time...
		
		for (int i=0; i<a; i++){
			//System.out.print(i+" ");
			double minxdt=Double.MAX_VALUE;
			int minloc=-1;
			for (int j=0; j<a; j++){
				if (xdt[0][j]<minxdt){
					minxdt=xdt[0][j];
					minloc=j;
				}
			}
			for (int k=0; k<19; k++){
				cdata[k][i]=xd[k][minloc];
				
				//System.out.print(cdata[k][i]+" ");
			}
			cdataTemp[0][i]=xdt[0][minloc];
			
			
			
			//System.out.println(cdataTemp[0][i]);
			cdataLoc[i]=xdl[minloc];
			cdataAmp[i]=xde[minloc];
			xdt[0][minloc]=Double.MAX_VALUE;
		}
		
		//sets first index of cdataLoc to 'skip ahead' number...
		/*
		for (int i=0; i<a; i++){
			double p=cdataLoc[i];
			cdataLoc[i]=0;
			for (int j=i+1; j<n; j++){
				if (p==cdataLoc[j]){
					cdataLoc[i]=j;
					j=n;
				}
			}
		}
		*/
		
		//make relative time index...
		double maxt=cdataTemp[0][a-1];
		for (int i=0; i<a; i++){
			cdataTemp[1][i]=cdataTemp[0][i]/maxt;
			
			//System.out.println(cdataTemp[0][i]+" ");
			
		}
	
	}
	
	
	
	public double[][] extractTempParams(int[] v){
		double[][] out=new double[v.length][];
		
		for (int i=0; i<v.length; i++){
			out[i]=cdataTemp[v[i]];
		}
		return out;
	}
	
	public double[][] extractParams(int[] v){
		double[][] out=new double[v.length][];
		//System.out.println(v.length);
		for (int i=0; i<v.length; i++){
			//System.out.println(cdata.length+" "+paramNames[v[i]]);
			out[i]=cdata[v[i]];
		}
		return out;
	}
	
	public double[] extractAmpParams(){
		return cdataAmp;
	}
	
	public int[] extractLocParams(){
		return cdataLoc;
	}
	

	public double calculateFit(boolean [] p, int d){
		double sum=0;
		int a, b,c;
		double x;
		for (int i=0; i<length; i++){
			if (p[i]){
				a=i;
				while (p[a]){a--;}
				b=i;
				while (p[b]){b++;}
				a+=5;
				b+=5;
				c=i+5;
				x=((i-a)/(b-a+0.0))*(measurements[b][d]-measurements[a][d]);
				x+=measurements[a][d];
				sum+=(x-measurements[c][d])*(x-measurements[c][d]);
			}
		}
		return Math.sqrt(sum);
	}

	/**
	 * This method updates element measures based on a Song object. The idea here is to adapt
	 * the measurements to new spectrogram settings. This is not yet functioning properly.
	 * @param song a Song object.
	 */
	
	public void update(Song song){
		timeStep=song.spectrogram.timeStep;
		frameLength=song.spectrogram.frameLength;
		maxf=song.spectrogram.maxf;
		windowMethod=song.spectrogram.windowMethod;

		dynEqual=song.spop.dynEqual;
		dynRange=song.spop.dynRange;
		
		echoRange=song.spop.echoRange;
		echoComp=song.spop.echoComp;
		
		double archdy=song.spectrogram.dy;
		
		
		
		//THIS IS BROKEN!!!
		//song.spectrogram.setFFTParameters(true);
		dy=song.spectrogram.dy;
		boolean adjdy=false;
		if (dy!=archdy){adjdy=true;}
		double a=archdy/dy;
		length=signal.length;
		double measure2[][]=new double [length][15];
		double ny=maxf/dy;
		powerSpectrum=new double[(int)Math.round(ny)];
		for (int i=0; i<length; i++){
			measure2[i][0]=(ny-signal[i][1])*dy;
			measure2[i][3]=(ny-signal[i][2])*dy;
			if (adjdy){
				measure2[i][0]=(ny-(a*signal[i][1]))*dy;
				measure2[i][3]=(ny-(a*signal[i][2]))*dy;
			}
			
			if (measurements.length==measure2.length){
			
				measure2[i][8]=measurements[i][0];
				measure2[i][11]=measurements[i][1];
				measure2[i][12]=measurements[i][2];
			}
		}
		measurements=measure2;
		int sig2[][]=new int [length][];
		for (int i=0; i<length; i++){
			sig2[i]=new int[signal[i].length-2];
			sig2[i][0]=signal[i][0];
			for (int j=3; j<signal[i].length; j++){
				sig2[i][j-2]=signal[i][j];
				if (adjdy){
					sig2[i][j-2]=(int)Math.round(a*signal[i][j]);
				}
			}
		}
		signal=sig2;
		calculateStatistics();
		song.spectrogram.dy=archdy;
	}
	
	
	
	
	public double[] getTimes(int npoints){
		double[] results=new double[npoints];
		
		double m=npoints;
		
		double increment=length/(m-1.0);
		
		for (int i=0; i<npoints; i++){
			
			results[i]=(begintime+i*increment)*timeStep;
			
		}
			
		return results;
	}
	
	public double[] getTimes() {
		double[] results=new double[length];
		for (int i=0; i<length; i++){
			results[i]=(begintime+i)*timeStep;	
		}
			
		return results;
		
	}
	
	
	
	public double[] getMeasurements(int id, int npoints){
		double[] results=new double[npoints];
		
		int n=measurements.length-5;
		int n1=n-1;
		
		int m=npoints;
		int m1=m-1;
		
		double increment=n1/(m1+0.0);
		
		for (int i=0; i<m; i++){
			
			double p=increment*i;
			p+=5;
			
			int x1=(int)Math.floor(p);
			int x2=(int)Math.ceil(p);
			
			if (x1<5){x1=5;}
			if (x2>=measurements.length){x2=measurements.length-1;}
			
			double y1=measurements[x1][id];
			double y2=measurements[x2][id];
			
			double q1=p-x1;
			double q2=x2-p;
			if(x1!=x2){
				results[i]=y1*q2+y2*q1;
			}
			else{
				results[i]=y1;
			}
			
		}
		return results;
		
	}
	
	
	public void updateFreqChangeMeasures(){
		
		for (int i=4; i<8; i++){
			
			
			double max=-100000000;
			double min=100000000;
			//double timemax=0;
			//double timemin=0;
			double av=0;
			
			for (int j=5; j<measurements.length; j++){				
				double x=Math.PI*(measurements[j][i]-0.5);
				double y=Math.tan(x);
				measurements[j][i]=y/30.0;
				
				av+=y;
				if (y>max){
					max=y;				
				}
				if (y<min){
					min=y;
				}		
			}
			
			av/=(measurements.length-5.0);
			
			measurements[0][i]=max;
			measurements[1][i]=min;
			measurements[4][i]=av;			
		}	
	}
	
	/*
	public int checkValues(Song song){
		int p=0;
		for (int i=0; i<measurements[0].length; i++){
			
			for (int j=0; j<measurements.length; j++){
				if (Double.isNaN(measurements[j][i])){
					System.out.println(song.getName()+" "+i+" NaN");
				}
				if (Double.isInfinite(measurements[j][i])){
					System.out.println(song.getName()+" "+i+" INF");
				}
			}
			
		}
		return p;
	}
	*/
	
	
	public String[] checkValues(SongUnits song){
		String s[]=null;
		for (int i=0; i<measurements[0].length; i++){
			
			for (int j=0; j<measurements.length; j++){
				if (Double.isNaN(measurements[j][i])){
					
					s=new String[2];
					s[0]="NaN";
					s[1]=measurementLevels[i];
					//System.out.println(song.getName()+" "+i+" NaN");
				}
				if (Double.isInfinite(measurements[j][i])){
					
					s=new String[2];
					s[0]="Infinite value";
					s[1]=measurementLevels[i];
				}
			}
			
		}
		
		return s;
		
	}
	
	public int getMinLevel(){
		int p=3;
		if (syls!=null){
			for (Syllable sy : syls){
				if (sy.maxLevel<p){
					p=sy.maxLevel;
					//System.out.println(p+" "+syls.size());
				}
			
			}
		}
		return p;
	}
	
	public void setSyllable(Syllable syl){
		this.syl=syl;
	}
	
	public Syllable getSyllable() {
		for (Syllable syl: syls) {
			if (syl.getNumChildren()==0) {
				return syl;
			}
		}
		return null;
	}
	
	public Syllable getPhrase() {
		for (Syllable syl: syls) {
			if (syl.getNumParents()==0) {
				return syl;
			}
		}
		return null;
	}
	
	public boolean checkElement(int maxf2) {
		boolean ok=true;
		for (int i=0; i<signal.length; i++) {
			double p=maxf2-signal[i][1]*dy;
			if (p-500>measurements[i+5][0]) {
				ok=false;
			}	
			//System.out.println(i+" "+p+" "+maxf+" "+measurements[i+5][0]+" "+signal.length+" "+measurements.length);
		}
		return ok;
	}
	
	public void resetSignal(int oldmaxf) {
		int d=maxf-oldmaxf;
		
		int steps=(int)Math.ceil(d/dy);
	
		for (int i=0; i<signal.length; i++) {
			for (int j=1; j<signal[i].length; j++) {
				signal[i][j]+=steps;
			}
		}
		
		maxf=oldmaxf;
		
		
		
	}
	
	
}


