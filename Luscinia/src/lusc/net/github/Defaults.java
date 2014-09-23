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

public class Defaults {
	NumberFormat num;
	
	Properties props=new Properties();
	
	static LookAndFeel lnf;

	public Defaults(){
		num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(10);
		readProperties();
	}
	
	void readProperties(){
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
	
	void writeProperties(){
		try{
			FileOutputStream out = new FileOutputStream("lusciniaproperties");
			props.store(out, "---No Comment---");
			out.close();
		}
		catch(Exception e){
		
		}
	}
	
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
	
	public double getDoubleProperty(String key, double multiplier){
		int x=getIntProperty(key);
		return x/multiplier;
	}
	
	public double getDoubleProperty(String key, double multiplier, double defval){
		
		int defdoub=(int)Math.round(defval*multiplier);
		
		int x=getIntProperty(key, defdoub);
		return x/multiplier;
	}
	
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
	
	public void setIntProperty(String key, int x){
		Integer y=new Integer(x);
		String s=y.toString();
		props.setProperty(key, s);
	}
	
	public void setDoubleProperty(String key, double x, int multiplier){
		int p=(int)Math.round(x*multiplier);
		setIntProperty(key, p);
	}
	
	public void setBooleanArray(String key, boolean[]data){
		StringBuffer sb=new StringBuffer();
		for (int i=0; i<data.length; i++){
			if (data[i]){sb.append("1");}
			else{sb.append("0");}
		}
		String s=sb.toString();
		props.setProperty(key, s);	
	}
	
	public boolean[] getBooleanArray(String key, int length){
		String s=props.getProperty(key);
		boolean[] results=null;
		if (s!=null){
			int n=s.length();
			results=new boolean[length];
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
	
	void setIndividualDetails(Individual ind){
		props.setProperty("spec", ind.species);
		props.setProperty("popu", ind.population);
	}
	
	void getIndividualDetails(Individual ind){
		ind.species=props.getProperty("spec", " ");
		ind.population=props.getProperty("popu", " ");
	}
	
	
	
	void setSongDetails(Song song){
		props.setProperty("reco", song.recordist);
		props.setProperty("rece", song.recordEquipment);
		props.setProperty("loca", song.location);
	}
	
	void getSongDetails(Song song){
		song.recordist=props.getProperty("reco");
		song.recordEquipment=props.getProperty("rece");
		song.location=props.getProperty("loca");
	}
	
	
	void setSongParameters(Song song){
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
	
	void getSongParameters(Song song){
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
	
	void getMiscellaneousSongParameters(Song song){
		System.out.println("HERE GETTING RANDOM VALUES");
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
	
	void setDTWParameters(SongGroup sg){
		
		for (int i=0; i<sg.parameterValues.length; i++){
			
			if (i<10){
				setDoubleProperty("dtwpa0"+i, sg.parameterValues[i], 1000);
			}
			else{
				setDoubleProperty("dtwpa"+i, sg.parameterValues[i], 1000);
			}
		}
		
		if (sg.weightByAmp){
			setIntProperty("dtwwba", 1);
		}
		else{
			setIntProperty("dtwwba", 0);
		}
		setIntProperty("dtwsti", sg.stitchSyllables);
		/*
		if (sg.stitchSyllables){
			setIntProperty("dtwsti", 1);
		}
		else{
			setIntProperty("dtwsti", 0);
		}
		*/
		if (sg.logFrequencies){
			setIntProperty("dtwlf", 1);
		}
		else{
			setIntProperty("dtwlf", 0);
		}
		setDoubleProperty("dtwmrf", sg.mainReductionFactor, 1000);
		setDoubleProperty("dtwsdr", sg.sdRatio, 1000);
		setDoubleProperty("dtwstp", sg.stitchPunishment, 1000);
		setDoubleProperty("dtwalc", sg.alignmentCost, 1000);
		setIntProperty("dtwmpo", sg.minPoints);
		setDoubleProperty("dtwsrw", sg.syllableRepetitionWeighting, 1000);
	}
	
	void getDTWParameters(SongGroup sg){
		
		for (int i=0; i<sg.parameterValues.length; i++){
			
			if (i<10){
				sg.parameterValues[i]=getDoubleProperty("dtwpa0"+i, 1000, 0);
			}
			else{
				sg.parameterValues[i]=getDoubleProperty("dtwpa"+i, 1000, 0);
			}
		}
		
		int a=getIntProperty("dtwwba", 0);
		if (a==0){
			sg.weightByAmp=false;
		}
		else{
			sg.weightByAmp=true;
		}
		
		int b=getIntProperty("dtwsti", 0);
		sg.stitchSyllables=b;

		
		int c=getIntProperty("dtwlf", 1);
		if (c==0){
			sg.logFrequencies=false;
		}
		else{
			sg.logFrequencies=true;
		}
		
		sg.mainReductionFactor=getDoubleProperty("dtwmrf", 1000, 1);
		sg.minPoints=getIntProperty("dtwmpo", 10);
		sg.sdRatio=getDoubleProperty("dtwsdr", 1000, 0.5);
		sg.stitchPunishment=getDoubleProperty("dtwstp", 1000, 150);
		sg.alignmentCost=getDoubleProperty("dtwalc", 1000, 150);
		sg.syllableRepetitionWeighting=getDoubleProperty("dtwsrw", 1000, 0);
	}
	
	void setAnalysisOptions(StatOptionPanel sop){
		setBooleanArray("anatyp", sop.analysisTypes);
		setBooleanArray("analev", sop.analysisLevels);
		setBooleanArray("anamis", sop.miscOptions);
		
		setIntProperty("anandi", sop.ndi);
		setIntProperty("anadmo", sop.dendrogramMode);
		setIntProperty("anasmo", sop.syntaxMode);
		setIntProperty("anamxk", sop.maxClusterK);
		setIntProperty("anamnk", sop.minClusterK);
		setIntProperty("anasnnk", sop.snnK);
		setIntProperty("anaSMPS", sop.snnMinPts);
		setIntProperty("anaSEPS", sop.snnEps);
		setIntProperty("anamsk", sop.maxSyntaxK);	
		
		setDoubleProperty("anasoup", sop.songUpperLimit, 100);
		setDoubleProperty("anasolo", sop.songLowerLimit, 100);
		setDoubleProperty("anageog", sop.geogPropLimit, 100);
	}
	
	void getAnalysisOptions(StatOptionPanel sop){
		boolean[] results1=getBooleanArray("anatyp", 11);
		if (results1!=null){
			sop.analysisTypes=results1;
		}
		boolean[] results2=getBooleanArray("analev", 4);
		if (results2!=null){
			sop.analysisLevels=results2;
		}
		boolean[] results3=getBooleanArray("anamis", 3);
		if (results3!=null){
			sop.miscOptions=results3;
		}
		
		sop.ndi=getIntProperty("anandi", 5);
		sop.dendrogramMode=getIntProperty("anadmo", 1);
		sop.syntaxMode=getIntProperty("anasmo", 2);
		sop.maxClusterK=getIntProperty("anamxk", 10);
		sop.minClusterK=getIntProperty("anamnk", 2);
		sop.snnK=getIntProperty("anasnnk", 20);
		sop.snnMinPts=getIntProperty("anaSMPS", 10);
		sop.snnEps=getIntProperty("anaSEPS", 10);
		sop.maxSyntaxK=getIntProperty("anamsk", 10);
		
		sop.songUpperLimit=getDoubleProperty("anasoup", 100, 1000);
		sop.songLowerLimit=getDoubleProperty("anasolo", 100, 0);
		sop.geogPropLimit=getDoubleProperty("anageog", 100, 5);
	}
						
	
		
	void setDefaultSoundFormat(int a){
		setIntProperty("sofo", a);
	}
	
	int getDefaultSoundFormat(){
		int p=getIntProperty("sofo", 0);
		return p;					 
	}
	
	void setDefaultImageFormat(int a){
		setIntProperty("imfo", a);
	}
	
	int getDefaultImageFormat(){
		int p=getIntProperty("imfo", 0);
		return p;					 
	}
	
	void setDefaultDocFormat(int a){
		setIntProperty("dofo", a);
	}
	
	int getDefaultDocFormat(){
		int p=getIntProperty("dofo", 0);
		return p;					 
	}
	
	
	void setParameterViews(SpectrPane sp){
		setBooleanArray("viewp", sp.viewParameters);
	}
	
	void getParameterViews(SpectrPane sp){
		boolean[] results=getBooleanArray("viewp", 19);
		if (results!=null){
			sp.viewParameters=results;
		}
	}
	
	void setStatisticsOutput(boolean[] chooserV, boolean[] chooserP, boolean[] chooserM, boolean[] chooserS, boolean[] chooserSy){
		
		setBooleanArray("vecOut", chooserV);
		setBooleanArray("parOut", chooserP);
		setBooleanArray("measOut", chooserM);
		setBooleanArray("scalOut", chooserS);
		setBooleanArray("sylOut", chooserSy);
	}
}
