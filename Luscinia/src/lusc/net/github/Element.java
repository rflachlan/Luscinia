package lusc.net.github;

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

	double timeStep=0;
	double frameLength=0;
	int maxf=0;
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
		timeStep=song.timeStep;
		frameLength=song.frameLength;
		maxf=song.maxf;
		windowMethod=song.windowMethod;
		//dynRange=song.dynEqual;
		//dynComp=song.dynRange;	NOTE CHANGE OF TERMS!!!
		dynEqual=song.dynEqual;
		dynRange=song.dynRange;
		
		echoRange=song.echoRange;
		echoComp=song.echoComp;
		dy=song.dy;
		this.signal=signal;
		this.measurements=measurements;
		this.powerSpectrum=powerSpectrum;
		length=signal.length;
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
	 * @param average if true, various parameters are averaged, if false, they are not
	 * e.g. the length of the new Element
	 */
	
	public Element(Element[] eleArray, boolean average){
		
		double n=eleArray.length;
		double tbn=0;
		double tan=0;
		statistics=new double[19][9];
		
		powerSpectrum=new double[eleArray[0].powerSpectrum.length];
		
		
		
		timeMax=0;
		timeMin=1000000;
		
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
			
			if (eleArray[i].timeMax>timeMax){timeMax=eleArray[i].timeMax;}
			if (eleArray[i].timeMin<timeMin){timeMin=eleArray[i].timeMin;}
			
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
		return timeBefore;
	}
	
	/**
	 * sets the time (in ms) between this element and the previous one. If there were no
	 * previous elements, returns -1000. This isn't a great choice of default parameter...
	 * @param a a float ms value
	 */
	
	public void setTimeBefore(float a){
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
	
	/*
	 //THIS WAS A SLIGHTLY MORE ELEGANT ATTEMPT TO COMPRESS ELEMENT MEASURES WITHIN THE ELEMENT OBJECT.
	  //COULD BE RESURRECTED?
	public void compressElements(int data){
	
		boolean p[]=new boolean[length];
		boolean q[]=new boolean[length];
		int length1=length-5;
		double x, min, score;
		int loc=0;
		double minScore=100000000;
		int scoreLoc=0;
		for (int i=1; i<length1; i++){
			min=10000000;
			loc=0;
			for (int j=1; j<length1; j++){
				if (!p[j]){
					p[j]=true;
					x=calculateFit(p, data);
					if (x<min){
						min=x;
						loc=j;
					}
					p[j]=false;
				}
			}
			p[loc]=true;
			score=Math.log(min+1000)+Math.log(length-i+10);
			if (score<minScore){
				minScore=score;
				scoreLoc=i;
				for (int j=0; j<length; j++){
					q[j]=p[j];
				}
			}
			//System.out.println(i+" "+min+" "+score);
		}
		scoreLoc=length-scoreLoc;
		int dems=measurements[0].length;
		compressedMeasures=new double[scoreLoc][dems];
		compressedPoints=new int[scoreLoc];
		int count=0;
		for (int i=0; i<length; i++){
			if (!q[i]){
				//System.out.println(i+" "+length+" "+scoreLoc+" "+count+" "+measurements.length);
				for (int j=0; j<dems; j++){
					compressedMeasures[count][j]=measurements[i+5][j];
					
				}
				compressedPoints[count]=i;
				count++;
			}
		}		
	}
	*/
	
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
		timeStep=song.timeStep;
		frameLength=song.frameLength;
		maxf=song.maxf;
		windowMethod=song.windowMethod;
		dynEqual=song.dynEqual;
		dynRange=song.dynRange;
		echoRange=song.echoRange;
		echoComp=song.echoComp;
		double archdy=song.dy;
		song.setFFTParameters();
		dy=song.dy;
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
		song.dy=archdy;
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
	
	
}
