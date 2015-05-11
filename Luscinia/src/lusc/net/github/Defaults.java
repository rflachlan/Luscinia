package lusc.net.github;
//
//  Defaults.java
//  Luscinia
//
//  Created by Robert Lachlan on 8/17/06.
//  Copyright 2006 __MyCompanyName__. All rights reserved.
//

import java.io.*;
import java.text.*;
import java.util.*;

//import java.util.Properties.*;
import javax.swing.*;

import lusc.net.github.ui.compmethods.DTWPanel;
import lusc.net.github.ui.StatOptionPanel;
import lusc.net.github.ui.spectrogram.SpectrPane;

/**
 * Defaults coordinates - across the whole of Luscinia - the writing and reading of 
 * default parameters for the UI to use. In general the rule here is that Defaults should
 * only interact with UI classes, and in the future it may be moved to the ui package.
 * @author Rob
 *
 */

public class Defaults {
	NumberFormat num;
	
	public Properties props=new Properties();
	
	static LookAndFeel lnf;
	int scaleFactor=1;
	JMenuBar menuBar;
	
	/**
	 * constructor
	 */

	public Defaults(){
		num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(10);
		readProperties();
	}
	
	/**
	 * Reads all default properties from file into a new Properties object.
	 * Should be called once near the initiation of the program.
	 */
	
	public void readProperties(){
		try{
			//InputStream in = Luscinia.class.getClassLoader().getResourceAsStream(sConfigFile);
			FileInputStream in = new FileInputStream("lusciniaproperties");

			props = new Properties();
			props.load(in);
			in.close();
		}
		catch (Exception e){
		
		}
	}
	
	/**
	 * Writes all the default values to file (lusciniaproperties).
	 * Should be called whenever a more permanent updating of default values
	 * is called for (especially on program shutdown, but also more frequently).
	 */
	
	public void writeProperties(){
		try{
			FileOutputStream out = new FileOutputStream("lusciniaproperties");
			props.store(out, "---No Comment---");
			out.close();
		}
		catch(Exception e){
		
		}
	}
	
	/**
	 * Returns an integer value from the Properties store.
	 * @param key the key value (String)
	 * @return an integer default value.
	 */
	
	public int getIntProperty(String key){
		String s=props.getProperty(key, "0");
		int x=0;
		try{
			Number number=num.parse(s);
			x=number.intValue();
		}
		catch(ParseException e){}
		return x;
	}
	
	/**
	 * Returns an integer value from the Properties store. If one is not found, returns
	 * a supplied default value instead
	 * @param key the key value (String)
	 * @param defval the default value to return if nothing is found in the store
	 * @return an integer default value.
	 */
	
	public int getIntProperty(String key, int defval){
		Integer a=new Integer(defval);
		String s=props.getProperty(key, a.toString());
		int x=0;
		try{
			Number number=num.parse(s);
			x=number.intValue();
		}
		catch(ParseException e){}
		return x;
	}
	
	/**
	 * Returns a double value from the Properties store.
	 * @param key the key value (String)
	 * @param multiplier (actually a divider). This allows double values to be stored as integers
	 * and must be specified. 
	 * @return a double parameter, calculated as the value obtained from the store divided by multiplier
	 */
	
	public double getDoubleProperty(String key, double multiplier){
		int x=getIntProperty(key);
		return x/multiplier;
	}
	
	/**
	 * Returns a double value from the Properties store. If one is not found, returns
	 * a supplied default value instead
	 * @param key the key value (String)
	 * @param multiplier (actually a divider). This allows double values to be stored as integers
	 * and must be specified. 
	 * @param defval the default value to return if nothing is found in the store
	 * @return a double parameter, calculated as the value obtained from the store divided by multiplier
	 */
	
	public double getDoubleProperty(String key, double multiplier, double defval){
		
		int defdoub=(int)Math.round(defval*multiplier);
		
		int x=getIntProperty(key, defdoub);
		return x/multiplier;
	}
	
	/**
	 * Returns a LinkedList of Strings from the properties store. Internally, '|~|' is 
	 * used as a rather idiosyncratic separator between values.
	 * @param key the key value (String)
	 * @return a list of strings.
	 */
	
	public LinkedList<String> getStringList(String key){
		String s=props.getProperty(key);
		LinkedList<String> LList=new LinkedList<String>();
		if (s!=null){
			int ind=0;
			int ind2=0;
			ind2=s.indexOf("'|~|'", ind);
			ind=-4;
			while(ind2!=-1){
				String t=s.substring(ind+4, ind2);
				LList.add(t);
				ind=ind2+1;
				ind2=s.indexOf("'|~|'", ind);
			}
		}
		return LList;
	}
	
	/**
	 * Adds/replaces a list of Strings to the store
	 * @param key the key value (String)
	 * @param ll a LinkedList of Strings.
	 */
	
	public void setStringList(String key, LinkedList<String> ll){
	
		StringBuffer sb=new StringBuffer();
		for(int i=0; i<ll.size(); i++){
			String s=(String)ll.get(i);
			sb.append(s);
			sb.append("'|~|'");
		}
		String s=sb.toString();
		props.setProperty(key, s);
	}
	
	/**
	 * Adds/replaces an integer to the store
	 * @param key the key value (String)
	 * @param x an integer value to store
	 */
	
	public void setIntProperty(String key, int x){
		Integer y=new Integer(x);
		String s=y.toString();
		props.setProperty(key, s);
	}
	
	/**
	 * Adds/replaces a double to the store
	 * @param key the key value (String)
	 * @param x a double value to store
	 * @param multiplier a value to multiply x by, after which it is rounded to an integer before storage.
	 */
	
	public void setDoubleProperty(String key, double x, int multiplier){
		int p=(int)Math.round(x*multiplier);
		setIntProperty(key, p);
	}
	
	/**
	 * Adds/replaces an array of booleans to the store. This is used for sets of radiobuttons,
	 * for example
	 * @param key the key value (String)
	 * @param data an array of booleans to store.
	 */
	
	public void setBooleanArray(String key, boolean[]data){
		StringBuffer sb=new StringBuffer();
		for (int i=0; i<data.length; i++){
			if (data[i]){sb.append("1");}
			else{sb.append("0");}
		}
		String s=sb.toString();
		props.setProperty(key, s);	
	}
	
	/**
	 * gets an array of booleans from the store.
	 * @param key the key value (String)
	 * @param length the length of the array. I'm pretty sure this is superfluous!
	 * @return an array of booleans
	 */
	
	public boolean[] getBooleanArray(String key, int length){
		String s=props.getProperty(key);
		boolean[] results=new boolean[length];
		for (int i=0; i<results.length; i++){
			results[i]=false;
		}
		if (s!=null){
			int n=s.length();
			//results=new boolean[length];
			for (int i=0; i<n; i++){
				char p=s.charAt(i);
				if (p=='0'){
					results[i]=false;
				}
				else{
					results[i]=true;
				}
			}
		}
		return results;
	}
	
	/**
	 * Gets a boolean parameter from the store
	 * @param key the key value (String)
	 * @param defval the default value for the store (if it's not already stored)
	 * @return a boolean parameter value.
	 */
	
	public boolean getBooleanProperty(String key, boolean defval){
		String s=props.getProperty(key);
		boolean results=defval;
		if (s!=null){
			if (s.equals("0")){
				results=false;
			}
			else{
				results=true;
			}
		}
		return results;
	}
	
	/**
	 * Sets a boolean parameter in the store
	 * @param key the key value (String)
	 * @param val the boolean parameter to be stored
	 */
	
	public void setBooleanProperty(String key, boolean val){
		String s="0";
		if (val){
			s="1";
		}
		props.setProperty(key, s);
	}
	
	
	/**
	 * gets an array of booleans from the store.
	 * @param key the key value (String)
	 * @return an array of booleans.
	 */
	
	public boolean[] getBooleanArray(String key){
		String s=props.getProperty(key);
		boolean[] results=null;
		if (s!=null){
			int n=s.length();
			results=new boolean[n];
			for (int i=0; i<n; i++){
				char p=s.charAt(i);
				if (p=='0'){
					results[i]=false;
				}
				else{
					results[i]=true;
				}
			}
		}
		return results;
	}
	
	/**
	 * Convenience method that sets details of Individual objects to the defaults. This
	 * is used (with its get counterpart) for quickly filling in species, population details, for example.
	 * @param ind An Individual object
	 */
	
	public void setIndividualDetails(Individual ind){
		props.setProperty("spec", ind.species);
		props.setProperty("popu", ind.population);
	}
	
	/**
	 * Convenience method that gets details of Individual objects to the defaults. This
	 * is used for quickly filling in species, population details, for example.
	 * Ideally, this should probably call set methods in Individual.
	 * @param ind An Individual object
	 */
	
	public void getIndividualDetails(Individual ind){
		ind.species=props.getProperty("spec", " ");
		ind.population=props.getProperty("popu", " ");
	}
	
	/**
	 * Convenience method that sets details about Song objects. This is used with its
	 * get counterpart for quickly filling in recording details, for example.
	 * @param song A Song object
	 */
	
	public void setSongDetails(Song song){
		props.setProperty("reco", song.recordist);
		props.setProperty("rece", song.recordEquipment);
		props.setProperty("loca", song.location);
	}
	
	/**
	 * Convenience method that gets details about Song objects. This is used for 
	 * quickly filling in recording details, for example.
	 * This should probably use the set functions in Song.
	 * @param song A Song object
	 */
	
	public void getSongDetails(Song song){
		song.recordist=props.getProperty("reco");
		song.recordEquipment=props.getProperty("rece");
		song.location=props.getProperty("loca");
	}
	
	/**
	 * Convenience method that gets spectrogram details about Song objects. This is used for 
	 * defaul spectrogram parameters
	 * @param song A Song object.
	 */
	
	
	public void setSongParameters(Song song){
		setIntProperty("maxf", song.maxf);
		setDoubleProperty("frl", song.frameLength, 1000);
		setDoubleProperty("tst", song.timeStep, 1000);
		setDoubleProperty("fco", song.frequencyCutOff, 1);
		setIntProperty("wim", song.windowMethod);
		setDoubleProperty("echamt", song.echoComp, 1000);
		setIntProperty("echlen", song.echoRange);
		setDoubleProperty("noiseamt", song.noiseRemoval, 1000);
		setIntProperty("noiselen1", song.noiseLength1);
		setIntProperty("noiselen2", song.noiseLength2);
		setDoubleProperty("dynr", song.dynRange, 1000);
		setIntProperty("dyne", song.dynEqual);
		setDoubleProperty("fund", song.fundAdjust, 1000);
		setDoubleProperty("gap", song.minGap, 1000);
		setIntProperty("brush", song.brushSize);
		setDoubleProperty("fund2", song.fundJumpSuppression, 1000);
		setDoubleProperty("minlength", song.minLength, 1000);
		setDoubleProperty("upperloop", song.upperLoop, 1000);
		setDoubleProperty("lowerloop", song.lowerLoop, 1000);
		setIntProperty("maxbrush", song.maxBrush);
		setIntProperty("minbrush", song.minBrush);
		setIntProperty("brushtype", song.brushType);
	}
	
	/**
	 * A convenience method that gets spectrogram parameters for a Song object.
	 * This should probably use the set functions in Song.
	 * @param song a Song object.
	 */
	
	public void getSongParameters(Song song){
		song.maxf=getIntProperty("maxf", 8000);
		song.frameLength=getDoubleProperty("frl", 1000, 5);
		song.timeStep=getDoubleProperty("tst", 1000, 1);
		song.frequencyCutOff=getDoubleProperty("fco", 1, 0);
		song.windowMethod=getIntProperty("wim", 1);
		song.echoComp=getDoubleProperty("echamt", 1000, 0);
		song.echoRange=getIntProperty("echlen", 50);
		song.noiseRemoval=(float)getDoubleProperty("noiseamt", 1000, 0);
		song.noiseLength1=getIntProperty("noiselen1", 50);
		song.noiseLength2=getIntProperty("noiselen2", 50);
		song.dynEqual=getIntProperty("dyne", 0);
		song.dynRange=getDoubleProperty("dynr", 1000, 50);
		song.fundAdjust=getDoubleProperty("fund", 1000, 1);
		song.minGap=getDoubleProperty("gap", 1000, 0);
		song.brushSize=getIntProperty("brush", 1);
	}
	
	/**
	 * A convenience function that gets a range of measurement parameters for a Song
	 * object. Is this necessary (or used)? Should it be rolled into the previous function?
	 * @param song A Song object
	 */
	
	public void getMiscellaneousSongParameters(Song song){
		song.fundAdjust=getDoubleProperty("fund", 1000, 1);
		song.minGap=getDoubleProperty("gap", 1000, 0);
		song.brushSize=getIntProperty("brush", 1);
		song.fundJumpSuppression=getDoubleProperty("fund2", 1000, 100);
		song.minLength=getDoubleProperty("minLength", 1000, 5);
		song.upperLoop=getDoubleProperty("upperloop", 1000, 15);
		song.lowerLoop=getDoubleProperty("lowerloop", 1000, 0);
		song.maxBrush=getIntProperty("maxbrush", 10000);
		song.minBrush=getIntProperty("minbrush", 0);
		song.brushType=getIntProperty("brushtype", 1);
	}
	
	/**
	 * A convenience function that sets parameters for a DTW comparison.
	 * @param sg a DTWPanel object.
	 */
	
	public void setDTWParameters(DTWPanel sg){
		
		double[] pv=sg.getParameterValues();
		
		for (int i=0; i<pv.length; i++){
			
			if (i<10){
				setDoubleProperty("dtwpa0"+i, pv[i], 1000);
			}
			else{
				setDoubleProperty("dtwpa"+i, pv[i], 1000);
			}
		}
		/*
		boolean w=sg.getWeightByAmp();
		if (w){
			setIntProperty("dtwwba", 1);
		}
		else{
			setIntProperty("dtwwba", 0);
		}
		int st=sg.getStitchSyllables();
		setIntProperty("dtwsti", st);
		*/
		/*
		if (sg.stitchSyllables){
			setIntProperty("dtwsti", 1);
		}
		else{
			setIntProperty("dtwsti", 0);
		}
		*/
		/*
		boolean lf=sg.getLogFrequencies();
		if (lf){
			setIntProperty("dtwlf", 1);
		}
		else{
			setIntProperty("dtwlf", 0);
		}
		*/
		
		setBooleanProperty("dtwWBAX", sg.getWeightByAmp());
		setBooleanProperty("dtwLOGF", sg.getLogFrequencies());
		setBooleanProperty("dtwnsd", sg.getWeightBySD());
		setBooleanProperty("dtwintp", sg.getInterpolate());
		setBooleanProperty("dtwdyn", sg.getDynamicWarping());
		setIntProperty("dtwSTISYL", sg.getStitchSyllables());
		setIntProperty("dtwALIGN", sg.getAlignmentPoints());
		
		setDoubleProperty("dtwmrf", sg.getMainReductionFactor(), 1000);
		setDoubleProperty("dtwsdr", sg.getSDRatio(), 1000);
		setDoubleProperty("dtwstp", sg.getStitchPunishment(), 1000);
		setDoubleProperty("dtwalc", sg.getAlignmentCost(), 1000);
		setIntProperty("dtwmpo", sg.getMinPoints());
		setDoubleProperty("dtwsrw", sg.getSyllableRepetitionWeighting(), 1000);
		setDoubleProperty("dtwmaxw", sg.getMaximumWarp(), 1000);
	}
	
	/**
	 * A convenience function that gets parameters for a DTW comparisons
	 * @param sg a DTWPanel object.
	 */
	
	public void getDTWParameters(DTWPanel sg){
		
		for (int i=0; i<sg.parameterValues.length; i++){
			
			if (i<10){
				sg.parameterValues[i]=getDoubleProperty("dtwpa0"+i, 1000, 0);
			}
			else{
				sg.parameterValues[i]=getDoubleProperty("dtwpa"+i, 1000, 0);
			}
		}
		
		/*
		int a=getIntProperty("dtwwba", 0);
		if (a==0){
			sg.setWeightByAmp(false);
		}
		else{
			sg.setWeightByAmp(true);
		}
		
		int b=getIntProperty("dtwsti", 0);
		sg.setStitchSyllables(b);

		
		int c=getIntProperty("dtwlf", 1);
		if (c==0){
			sg.setLogFrequencies(false);
		}
		else{
			sg.setLogFrequencies(true);
		}
		*/
		
		sg.setWeightByAmp(getBooleanProperty("dtwWBAX", true)); 
		sg.setLogFrequencies(getBooleanProperty("dtwLOGF", true));
		sg.setWeightBySD(getBooleanProperty("dtwnsd", true));
		sg.setInterpolate(getBooleanProperty("dtwintp", true));
		sg.setDynamicWarping(getBooleanProperty("dtwdyn", true));
		sg.setStitchSyllables(getIntProperty("dtwSTISYL", 1));
		sg.setAlignmentPoints(getIntProperty("dtwALIGN", 1));
				
		sg.setMainReductionFactor(getDoubleProperty("dtwmrf", 1000, 1));
		sg.setMinPoints(getIntProperty("dtwmpo", 10));
		sg.setSDRatio(getDoubleProperty("dtwsdr", 1000, 0.5));
		sg.setStitchPunishment(getDoubleProperty("dtwstp", 1000, 150));
		sg.setAlignmentCost(getDoubleProperty("dtwalc", 1000, 150));
		sg.setSyllableRepetitionWeighting(getDoubleProperty("dtwsrw", 1000, 0));
		sg.setMaximumWarp(getDoubleProperty("dtwmaxw", 1000, 0));
	}
	
	/**
	 * A convenience function that sets options for various statistical analyses
	 * @param sop a StatOptionPanel object.
	 */
	
	public void setAnalysisOptions(StatOptionPanel sop){
		setBooleanArray("anatyp", sop.getAnalysisTypes());
		setBooleanArray("analev", sop.getAnalysisLevels());
		setBooleanArray("anamis", sop.getMiscOptions());	
		
		setDoubleProperty("anasoup", sop.getSongUpperLimit(), 100);
		setDoubleProperty("anasolo", sop.getSongLowerLimit(), 100);
		//setDoubleProperty("anageog", sop.getGeogPropLimit(), 100);
	}
	
	/**
	 * A convenience method that gets various parameters for statistical analyses.
	 * @param sop a StatOptionPanel object.
	 */
	
	public void getAnalysisOptions(StatOptionPanel sop){
		boolean[] results1=getBooleanArray("anatyp", 12);
		if (results1!=null){
			sop.setAnalysisTypes(results1);
		}
		boolean[] results2=getBooleanArray("analev", 5);
		if (results2!=null){
			sop.setAnalysisLevels(results2);
		}
		boolean[] results3=getBooleanArray("anamis", 5);
		if (results3!=null){
			sop.setMiscOptions(results3);
		}
		
		sop.setSongUpperLimit(getDoubleProperty("anasoup", 100, 1000));
		sop.setSongLowerLimit(getDoubleProperty("anasolo", 100, 0));
		//sop.setGeogPropLimit(getDoubleProperty("anageog", 100, 5));
	}
						
	/**
	 * a function to specifically set a sound format parameter. Why not just use setInt?
	 * @param a
	 */
		
	public void setDefaultSoundFormat(int a){
		setIntProperty("sofo", a);
	}
	
	/**
	 * a function to specifically get a sound format parameter. Why not just use getInt?
	 * @return p a sound format integer
	 */
	
	public int getDefaultSoundFormat(){
		int p=getIntProperty("sofo", 0);
		return p;					 
	}
	
	/**
	 * a function to specifically set an image format parameter. Why not just use setInt?
	 * @param a
	 */
	
	public void setDefaultImageFormat(int a){
		setIntProperty("imfo", a);
	}
	
	/**
	 * a function to specifically get an image format parameter. Why not just use getInt?
	 * @return p an image format integer
	 */
	
	public int getDefaultImageFormat(){
		int p=getIntProperty("imfo", 0);
		return p;					 
	}
	
	/**
	 * a function to specifically set a spreadsheet parameter. Why not just use setInt?
	 * @param a
	 */
	
	public void setDefaultDocFormat(int a){
		setIntProperty("dofo", a);
	}
	
	/**
	 * a function to specifically get a spreadsheet format parameter. Why not just use getInt?
	 * @return p a spreadsheet parameter
	 */
	
	public int getDefaultDocFormat(){
		int p=getIntProperty("dofo", 0);
		return p;					 
	}
	
	/**
	 * A convenience function to set the defaults for which parameters to view 
	 * on a spectrogram
	 * @param sp a SpectrPane object
	 */
	
	public void setParameterViews(SpectrPane sp){
		setBooleanArray("viewp", sp.getViewParameters());
	}
	
	/**
	 * A convenience function to get the defaults for which parameters to view
	 * on a spectrogram
	 * @param sp a SpectrPane object
	 */
	public void getParameterViews(SpectrPane sp){
		boolean[] results=getBooleanArray("viewp", 19);
		if (results!=null){
			sp.setViewParameters(results);
		}
	}
	
	/**A convenience function to get the array of parameters chosen for Parametric comparison
	 * 
	 */
	
	public boolean[][] getParameterPanelArray(){
		
		boolean[] temp=getBooleanArray("paramarr", 8*17);
		
		boolean[][] out=new boolean[8][17];
		
		int k=0;
		for (int i=0; i<8; i++){
			for (int j=0; j<17; j++){
				out[i][j]=temp[k];
				k++;
			}
		}
		return (out);
		
	}
	
	/**A convenience function to set the array of parameters chosen for Parametric comparison
	 * 
	 */
	
	public void setParameterPanelArray(boolean[][] d){
		
		boolean[] temp=new boolean[8*17];
				
		int k=0;
		for (int i=0; i<8; i++){
			for (int j=0; j<17; j++){
				temp[k]=d[i][j];
				k++;
			}
		}
		setBooleanArray("paramarr", temp);
		
	}
	
	
	/**
	 * A convenience function to set the defaults for the statistics output. This method works
	 * in conjunction with StatisticsOutput.
	 * @param chooserV a boolean array for which element measures to set.
	 * @param chooserP a boolean array for a range of metadata parameters.
	 * @param chooserM a boolean array for which types of measures (mean, min, max etx) to set.
	 * @param chooserS a boolean array for a range of scalar, typically temporal parameters.
	 * @param chooserSy a boolean array for syllable parameters
	 */
	
	public void setStatisticsOutput(boolean[] chooserV, boolean[] chooserP, boolean[] chooserM, boolean[] chooserS, boolean[] chooserSy){
		
		setBooleanArray("vecOut", chooserV);
		setBooleanArray("parOut", chooserP);
		setBooleanArray("measOut", chooserM);
		setBooleanArray("scalOut", chooserS);
		setBooleanArray("sylOut", chooserSy);
	}
	
	/**
	 * This method sets the graphics scaling factor (e.g. for retina screens)
	 * @param a scaling factor
	 */
	public void setScaleFactor(int a){
		scaleFactor=a;
		if (scaleFactor<1){
			System.out.println("ERROR WITH SCALE FACTOR DEFAULTING TO 1");
			scaleFactor=1;
		}
	}
	
	/**
	 * This method gets the graphics scaling factor (e.g. for retina screens)
	 * @return an int value scaling factor
	 */
	public int getScaleFactor(){
		return scaleFactor;
	}
	
	public void setMenuBar(JMenuBar menuBar){
		this.menuBar=menuBar;
	}
	
	public JMenuBar getMenuBar(){
		return menuBar;
	}
	
}
