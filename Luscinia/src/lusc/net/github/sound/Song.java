package lusc.net.github.sound;

//
//  Song.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/10/2005.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.


import java.util.LinkedList;
import java.util.Arrays;
import java.util.Calendar;
import java.awt.Component;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.JOptionPane;
import org.apache.commons.math3.*;

import lusc.net.github.Individual;
import lusc.net.github.db.DbConnection;



/**
 * Song is the object that recording units are held in. It contains both acoustic recording data
 * and measured sub-units like elements and syllables. It contains methods to make spectrograms.
 * It is one of the most central classes in Luscinia, and one of the largest. 
 * @author Rob
 *
 */
public class Song {
	
	public Spectrogram spectrogram=null;
	public SpectrogramOperations spop=null;
	public Sound sound=null;
	public SpectrogramMeasurement sm;
	public SongUnits su;
	public Individual ind;
	
	
	//public boolean useFileNameForDateTime=true;
	public boolean loaded=false;	
	
	int startTime=0; 
	int endTime=0; 
	
	public int individualID=0; 
	int individualDayID=0;
	int songID=0; 
	public long tDate=0;
	
	//parameters for SpectrogramMeasurements - DO I NEED THEM HERE?
	double fundAdjust=1; 
	double fundJumpSuppression=100; 
	double minGap=0;
	double minLength=5;
	double maxTrillWavelength=20; 
	double upperLoop=15; 
	double lowerLoop=0;
	
	
	//parameters for spectrpane to use
	int brushSize=5;
	int brushType=1; 
	boolean clickDrag=false;
	int maxBrush=10;
	int minBrush=0; 

	
	public String notes=" "; 
	public String quality=" ";
	public String type=" ";
	public String[] custom=new String[2];
	public String location=" "; 
	public String recordEquipment=" ";
	public String recordist=" "; 
	int archived=1;
	
	
	public String name=" ";

	
	
	String sx, sy;
	
	
	
	public Song(){
		
	}
	
	public Song (DbConnection db, int id, int loadData) {
		
		songID=id;
		sound=new Sound(this);
		this.spectrogram=sound.spectrogram;
		spop=new SpectrogramOperations(this);
		
		
		db.loadSoundFromDatabase(sound, this, songID, loadData);
		sound.setUp();
		db.loadSongDataFromDatabase(this, songID);
		
		
		su=new SongUnits(this);
		//System.out.println("I: "+song.individualID);
		su.setEleList(db.loadElementsFromDatabase2(songID));
		su.setUpElements();		
		su.loadSyllables(db.loadSyllablesFromDatabase(songID));	
		
		ind=new Individual(db, individualID);
			//THIS ISN'T GREAT - WOULD BE BETTER IF PASSED THE CORRECT INDIVIDUAL

	}
	
	
	public Song (File f, Individual ind){
		try {
			
			tDate=f.lastModified();
			individualID=ind.getID();
			name=f.getName();
			
			
		} 
		catch (Exception e){
			e.printStackTrace();
		}
		
		sound=new Sound(f, this);
		this.spectrogram=sound.spectrogram;
		spop=new SpectrogramOperations(this);
		su=new SongUnits(this);
		this.ind=ind;
	}
	
	
	/**
	 * Sets the song's time based on file name format from song recorder
	 * Expects format, e.g.: R21_2022_04_25_07_00_27.wav
	 */
	public void parseFileNameForTimeDate() {
		String[] sp=name.split("_");
		String[]sq=sp[6].split("[.]");
		Calendar cal=Calendar.getInstance();		
		
		cal.set(Calendar.YEAR, Integer.parseInt(sp[1]));
		cal.set(Calendar.MONTH, Integer.parseInt(sp[2])-1);
		cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(sp[3]));
		cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(sp[4]));
		cal.set(Calendar.MINUTE, Integer.parseInt(sp[5]));
		cal.set(Calendar.SECOND, Integer.parseInt(sq[0]));
		cal.set(Calendar.MILLISECOND, 0);
		tDate=cal.getTimeInMillis();
		
		
	}

	
	/**
	 * gets the Day-of-month from the tDate parameter as a long starting at the begining of the month
	 */
	public long getDay() {
		Calendar cal=Calendar.getInstance();
		cal.setTimeInMillis(tDate);
		Calendar cal2=Calendar.getInstance();
		cal2.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),0,0,0);
		cal2.set(Calendar.MILLISECOND, 0);
		long x=cal2.getTimeInMillis();
		//System.out.println(name+" "+x);
		return(x);	
	}
	
	/**
	 * gets the week-of-year from the tDate parameter as a long starting at the begining of the month
	 */
	public long getWeek() {
		Calendar cal=Calendar.getInstance();
		cal.setTimeInMillis(tDate);
		Calendar cal2=Calendar.getInstance();
		cal2.set(Calendar.YEAR, cal.get(Calendar.YEAR));
		cal2.set(Calendar.WEEK_OF_YEAR, cal.get(Calendar.WEEK_OF_YEAR));
		//cal2.set(Calendar.DAY_OF_WEEK_IN_MONTH, 0);
		cal2.set(Calendar.HOUR, 0);
		cal2.set(Calendar.MINUTE, 0);
		cal2.set(Calendar.SECOND, 0);
		cal2.set(Calendar.MILLISECOND, 0);
		
		long x=cal2.getTimeInMillis();
		return(x);	
	}
	
	

	
	/**
	 * Convenience get Method to build a complex SQL line to write data into database...
	 * @param b separator in SQL syntax
	 * @return a String in part-formed SQL with a complex list of parameters
	 */
	public String getDetails(String b){
		int sa=(int)sound.sampleRate;
		String details="echocomp="+spop.echoComp+b+"echorange="+spop.echoRange+b+"noise1="+spop.noiseRemoval+b+"noise2="+
				spop.noiseLength1+b+"noise3="+spop.noiseLength2+b+"dyncomp="+spop.dynRange+
				b+"dynrange="+spop.dynEqual+b+"maxfreq="+spectrogram.maxf+b+"framelength="+spectrogram.frameLength+b+"timestep="
				+spectrogram.timeStep+b+"filtercutoff="+sound.frequencyCutOff+b+"windowmethod="+spectrogram.windowMethod+b+"dx="+
				spectrogram.dx+b+"dy="+spectrogram.dy+b+"samplerate="+sa;
		return details;
	}
	

	/**
	 * Helps clear up a Song object when closed. Not sure whether this helps garbage collection 
	 * or not...
	 */
	public void clearUp(){
		notes=null;
		location=null;
		recordEquipment=null;
		recordist=null;
		sound.shutDownPlayback();
		spectrogram.clearUp();
		su.clearUp();
		spop.clearUp();
	}
	
	public void makeSpectrogram(int startTime, int endTime, boolean updateMeasurements) {
		makeSpectrogram(startTime, endTime, spectrogram);
		sm.updateSpectrogram(spectrogram);		
	}
	
	
	public void makeSpectrogram(int startTime, int endTime, Spectrogram spect) {
		this.startTime=startTime;
		this.endTime=endTime;
		
		sound.makeSoundArray(startTime, endTime, false);
			
		spect.setFFTParameters(true, sound.sampleRate);
		
		spect.calculateSampleFFT();
		spect.calculateFFT(sound.data, false);
				
		spop.copySpectrogram(spect);
		spop.dynamicEqualizer(spect);
		spop.dynamicRange(spect);
		
		spop.noiseRemoval(spect);
		spop.echoRemoval(spect);
		
		spop.spectrogramEnhancement();
		spop.equalize(spect);
		
		float[][] t=spop.spect;
		spectrogram.spect=new float[t.length][t[0].length];	
		for (int i=0; i<spectrogram.spect.length; i++) {
			for (int j=0; j<spectrogram.spect[i].length; j++) {
				spectrogram.spect[i][j]=t[i][j];
			}
		}
	}
	
	public Spectrogram makeSpectrogramWholeSignal() {
		int startTime=0;
		int endTime=sound.overallLength;
		
		Spectrogram spect=new Spectrogram(this.sound, this.spectrogram);
		spect.timeStep=2;
		
		makeSpectrogram(startTime, endTime, spect);
		
		float[][] out=spop.spect;
		
		return spect;
	}
	
	
	public void automaticMeasurement() {
		
		Spectrogram spect=makeSpectrogramWholeSignal();
		int[] pointList1=new int[spect.nx];
		int[] pointList2=new int[spect.nx];
		for (int i=1; i<spect.nx-1; i++){
			pointList1[i]=1;
			pointList2[i]=spect.ny-2;
		}
		
		
		//NEED TO SEND sm THE SPECTROGRAM!!!
		LinkedList<int[][]> signals=sm.getSignal(pointList1, pointList2, spect.nx, false);
		sm.checkMinimumLengths(signals);
		int n=sm.measureAndAddElements(signals, null, 0);
		su.makeEveryElementASyllable();
		
		/*
		System.out.println("MAKING SPECTROGRAM: "+name+" "+nx);
		setDynEqual(0);
		setDynRange(30);
		minGap=5000000;
		timeStep=2;
		setFFTParameters();
		setFFTParameters2(nx);
		
		
		makeMyFFT(0, nx);
		makePhase();
		int[] pointList1=new int[nx];
		int[] pointList2=new int[nx];
		for (int i=1; i<nx-1; i++){
			pointList1[i]=1;
			pointList2[i]=ny-2;
		}
		SpectrogramMeasurement sm=new SpectrogramMeasurement(this);
		LinkedList<int[][]> signals=sm.getSignal(pointList1, pointList2, nx, false);
		sm.checkMinimumLengths(signals);
		//sm.segment(signals, false);
		int n=sm.measureAndAddElements(signals, null, 0);
		
		makeEveryElementASyllable();
		for (Element ele: eleList) {
			if (ele.syl==null) {
				System.out.println("ALERT!!!");
			}
		}
		
		*/
	}
	
	
	
}
