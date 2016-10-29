package lusc.net.github.ui.compmethods;
//
//  StatisticsOutput.java
//  Luscinia
//
//  Created by Robert Lachlan on 3/1/07.
//  Copyright 2007 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.
//

import java.awt.*;

import javax.swing.*;

import java.awt.event.*;
import java.io.*;
import java.util.*;

import lusc.net.github.Defaults;
import lusc.net.github.Element;
import lusc.net.github.Song;
import lusc.net.github.Syllable;
//import lusc.net.github.analysis.SongGroup;
import lusc.net.github.analysis.AnalysisGroup;
import lusc.net.github.ui.SaveDocument;

public class StatisticsOutput  extends JPanel implements ActionListener{

	//public static String BUTTON_PRESS="Button pressed";
	public static String RADIO_PRESS="Radio pressed"; 

	String [] vectorStatistics={"Peak frequency", "Mean frequency", "Median frequency", "Fundamental frequency", "Peak frequency change", "Mean frequency change", "Median frequency change", 
	"Fundamental frequency change", "Harmonicity", "Wiener entropy", "Frequency bandwidth", "Amplitude", "Vibrato rate", "Vibrato amplitude",  "Vibrato asymmetry", "Abs. Peak Frequency Change", 
	"Abs. Mean Frequency Change", "Abs. Median Frequency Change", "Abs. Fundamental Frequency Change"};	

	String [] parameters={"Parameters", "Record time", "Record date", "Recordist", "Record location", "Record equipment", "Notes"};
	
	String [] scalarStatistics={"Start time", "Length", "Gap before", "Gap after", "Overall instantaneous peak freq.", "Overall peak frequency", "Power Spectrum statistics"};
	
	String [] syllableStatistics={"Within-syllable gap", "Between-syllable gap", "No. elements per syllable", "Syllable repetitions per phrase", "Is a phrase"};

	String [] measures={"Mean", "Median", "Variance", "Maximum", "Minimum", "Time of maximum", "Time of minimum", "Start", "End"};

	String [] details={"Individual name", "Song name", "Syllable Number", "Element Number", "Element repeats", "Syllable repeats"};
	
	String [] details2={"Time Step", "Frame Length", "Maximum frequency", "Windowing Method", "Dynamic Range", "Dynamic Equalization", "Echo Tail", "Echo Reduction", "dy"};
	
	int numVectorStatistics=vectorStatistics.length;
	int numParameters=parameters.length;
	int numScalarStatistics=scalarStatistics.length;
	int numSyllableStatistics=syllableStatistics.length-1;
	int numMeasures=measures.length;
	int numDetails=details.length;
	int numDetails2=details2.length;

	JRadioButton[] vecStatSet=new JRadioButton[numVectorStatistics];
	JRadioButton[] scalStatSet=new JRadioButton[numScalarStatistics];
	JRadioButton[] sylStatSet=new JRadioButton[numSyllableStatistics];
	JRadioButton[] paramSet=new JRadioButton[numParameters];
	JRadioButton[] meSet=new JRadioButton[numMeasures];
	
	JRadioButton rawOutput=new JRadioButton("Uncompressed element measures");
	JRadioButton compressPhrase=new JRadioButton("Compress element measures within phrase");
	JRadioButton compressSyll=new JRadioButton("Compress syllable measures");
	JRadioButton syllableRepertoireSize=new JRadioButton("Syllable repertoire size");
	
	JLabel description=new JLabel("Use this tab to export acoustic statistics for each element");
		
	LinkedList data;
	boolean[] chooserV=new boolean[numVectorStatistics];
	boolean[] chooserS=new boolean[numScalarStatistics];
	boolean[] chooserSy=new boolean[numSyllableStatistics];
	boolean[] chooserM=new boolean[numMeasures];
	boolean[] chooserP=new boolean[numParameters];
	
	Song[] songs;
	Defaults defaults;
	SaveDocument sd;
	
	
	//public StatisticsOutput(SongGroup sg, Defaults defaults){
	public StatisticsOutput(AnalysisGroup sg, Defaults defaults){

		this.songs=sg.getSongs();
		
		this.defaults=defaults;
		
		Font font=new Font("Sans-Serif", Font.PLAIN, 10);
		chooserV=defaults.getBooleanArray("vecOut");
		//chooserS=defaults.getBooleanArray("scalOut");
		chooserSy=defaults.getBooleanArray("sylOut");
		boolean[] defmeas=defaults.getBooleanArray("measOut");
		if (defmeas!=null){
			for (int i=0; i<defmeas.length; i++){
				chooserM[i]=defmeas[i];
			}
		}
		chooserP=defaults.getBooleanArray("parOut");
		if (chooserV==null) {chooserV=new boolean[numVectorStatistics];}
		if (chooserS==null) {chooserS=new boolean[numScalarStatistics];}
		if (chooserSy==null) {chooserSy=new boolean[numSyllableStatistics];}
		if (chooserM==null) {chooserM=new boolean[numMeasures];}
		if (chooserP==null) {chooserP=new boolean[numParameters];}
		
		
		JPanel mainpanel=new JPanel(new BorderLayout());
		
		JPanel northPanel=new JPanel(new GridLayout(0,1));
		
		compressSyll.addActionListener(this);
		compressSyll.setActionCommand(RADIO_PRESS);
		
		northPanel.add(description);
		northPanel.add(rawOutput);
		northPanel.add(compressPhrase);
		northPanel.add(compressSyll);
		
		mainpanel.add(northPanel, BorderLayout.NORTH);
		
		JPanel vecStatPanel=new JPanel(new BorderLayout());
		vecStatPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		
		JPanel measurePanel=new JPanel(new GridLayout(0,1));
		measurePanel.setBorder(BorderFactory.createLoweredBevelBorder());
		for (int i=0; i<numMeasures; i++){
			meSet[i]=new JRadioButton(measures[i]);
			meSet[i].setFont(font);
			meSet[i].setSelected(chooserM[i]);
			measurePanel.add(meSet[i]);
		}
		vecStatPanel.add(measurePanel, BorderLayout.WEST);
		
		JPanel vecPanel=new JPanel(new GridLayout(0,2));
		for (int i=0; i<numVectorStatistics; i++){
			vecStatSet[i]=new JRadioButton(vectorStatistics[i]);
			vecStatSet[i].setFont(font);
			vecStatSet[i].setSelected(chooserV[i]);
			vecPanel.add(vecStatSet[i]);
		}
		vecStatPanel.add(vecPanel, BorderLayout.EAST);
		
		mainpanel.add(vecStatPanel, BorderLayout.CENTER);
		
		JPanel westPanel=new JPanel(new BorderLayout());
		westPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		JPanel scalStatPanel=new JPanel(new GridLayout(0,1));
		for (int i=0; i<numScalarStatistics; i++){
			scalStatSet[i]=new JRadioButton(scalarStatistics[i]);
			scalStatSet[i].setFont(font);
			scalStatSet[i].setSelected(chooserS[i]);
			scalStatPanel.add(scalStatSet[i]);
		}
		westPanel.add(scalStatPanel, BorderLayout.EAST);
		
		JPanel paramPanel=new JPanel(new GridLayout(0,1));
		for (int i=0; i<numParameters; i++){
			paramSet[i]=new JRadioButton(parameters[i]);
			paramSet[i].setFont(font);
			paramSet[i].setSelected(chooserP[i]);
			paramPanel.add(paramSet[i]);
		}
		westPanel.add(paramPanel, BorderLayout.WEST);
		
		mainpanel.add(westPanel, BorderLayout.WEST);
		
		JPanel syllStatPanel=new JPanel(new GridLayout(0,1));
		syllStatPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		for (int i=0; i<numSyllableStatistics; i++){
			sylStatSet[i]=new JRadioButton(syllableStatistics[i]);
			sylStatSet[i].setFont(font);
			sylStatSet[i].setSelected(chooserSy[i]);
			sylStatSet[i].setEnabled(compressSyll.isEnabled());
			syllStatPanel.add(sylStatSet[i]);
		}
		syllableRepertoireSize.setFont(font);
		syllStatPanel.add(syllableRepertoireSize);
		
		mainpanel.add(syllStatPanel, BorderLayout.EAST);
		
		//JPanel startpanel=new JPanel();
		//JButton save=new JButton("save");
		//save.addActionListener(this);
		//save.setActionCommand(BUTTON_PRESS);
		//startpanel.add(save);
		
		this.setLayout(new BorderLayout());
		this.add(mainpanel, BorderLayout.CENTER);
		//this.add(startpanel, BorderLayout.SOUTH);
	}
	
	public void calculateStatistics(){
		for (int i=0; i<numVectorStatistics; i++){
			if (vecStatSet[i].isSelected()){
				chooserV[i]=true;
			}
			else{
				chooserV[i]=false;
			}
		}
		for (int i=0; i<numScalarStatistics; i++){
			if (scalStatSet[i].isSelected()){
				chooserS[i]=true;
			}
			else{
				chooserS[i]=false;
			}
		}
		for (int i=0; i<numSyllableStatistics; i++){
			if (sylStatSet[i].isSelected()){
				chooserSy[i]=true;
			}
			else{
				chooserSy[i]=false;
			}
		}
		for (int i=0; i<numParameters; i++){
			if (paramSet[i].isSelected()){
				chooserP[i]=true;
			}
			else{
				chooserP[i]=false;
			}
		}
		for (int i=0; i<numMeasures; i++){
			if (meSet[i].isSelected()){
				chooserM[i]=true;
			}
			else{
				chooserM[i]=false;
			}
		}
		calculateParameters();
	}
	

	public void actionPerformed(ActionEvent evt) {
		String command = evt.getActionCommand();
		//if (BUTTON_PRESS.equals(command)) {
			
		//}
		if (RADIO_PRESS.equals(command)){
			for (int i=0; i<numSyllableStatistics; i++){
				if (compressSyll.isSelected()){
					sylStatSet[i].setEnabled(true);
				}
				else{
					sylStatSet[i].setEnabled(false);
				}
			}
		
		}	
	}
	
	public void calculateParameters(){
		sd=new SaveDocument(this, defaults);
		calculate();
	}
	
	public void calculateParameters(String path, String name){
		sd=new SaveDocument(path, name);
		calculate();
	}
	
		
	public void calculate(){
		boolean readyToWrite=sd.makeFile();
		boolean pageWritten=false;
		if (readyToWrite){
			if(rawOutput.isSelected()){
				System.out.println("raw output");
				calculateRawOutput();
				pageWritten=true;
			}
			
			if (compressPhrase.isSelected()){
				if (pageWritten){sd.writeSheet("Phrase");}
				calculateCompressedPhrase();
				pageWritten=true;
			}
			
			if (compressSyll.isSelected()){
				if (pageWritten){sd.writeSheet("Syllables");}
				calculateCompressedSyll();
			}
			sd.finishWriting();
			defaults.setStatisticsOutput(chooserV, chooserP, chooserM, chooserS, chooserSy);
			defaults.writeProperties();
		}
	}
	
	public void writeHeader(int type){
		
		sd.writeString(details[0]);
		sd.writeString(details[1]);
		sd.writeString(details[2]);
		if (type<2){
			sd.writeString(details[3]);
		}
		if (type==1){
			sd.writeString(details[4]);
		}
			
		if (chooserP[0]){
			for (int i=0; i<details2.length; i++){
				sd.writeString(details2[i]);
			}
		}
		for (int i=1; i<numParameters; i++){
			if (chooserP[i]){
				sd.writeString(parameters[i]);
			}
		}
		for (int i=0; i<numScalarStatistics-1; i++){
			if (chooserS[i]){
				sd.writeString(scalarStatistics[i]);
			}
		}
		if (chooserS[numScalarStatistics-1]){
			sd.writeString("Overall Min Freq");
			sd.writeString("Overall Max Freq");
			sd.writeString("PS lower quartile");
			sd.writeString("PS upper quartile");
			sd.writeString("PS lower 95%-ile");
			sd.writeString("PS upper 95%-ile");
			sd.writeString("PS variance");
		}
		for (int i=0; i<numVectorStatistics; i++){
			if (chooserV[i]){
				for (int j=0; j<numMeasures; j++){
					if (chooserM[j]){
						sd.writeString(vectorStatistics[i]+" "+measures[j]);
					}
				}
			}
		}
		if (type==2){
			for (int i=0; i<numSyllableStatistics; i++){
				if (chooserSy[i]){
					sd.writeString(syllableStatistics[i]);
				}
			}
			sd.writeString(syllableStatistics[numSyllableStatistics-1]);
		}
		sd.writeLine();
	}
	
	public void writeDetails(Song song, Element ele, String songDate, String songTime){
		if (chooserP[0]){
			sd.writeDouble(ele.getTimeStep());
			sd.writeDouble(ele.getFrameLength());
			sd.writeInt(ele.getMaxF());
			sd.writeInt(ele.getWindowMethod());
			sd.writeDouble(ele.getDynRange());
			sd.writeDouble(ele.getDynEqual());
			sd.writeInt(ele.getEchoRange());
			sd.writeDouble(ele.getEchoComp());
			sd.writeDouble(ele.getDy());
		}
		if (chooserP[1]){
			sd.writeString(songTime);
		}
		if (chooserP[2]){
			sd.writeString(songDate);
		}
		if (chooserP[3]){
			sd.writeString(" "+song.getRecordist());
		}
		if (chooserP[4]){
			sd.writeString(" "+song.getLocation());
		}
		if (chooserP[5]){
			sd.writeString(" "+song.getRecordEquipment());
		}
		if (chooserP[6]){
			sd.writeString(" "+song.getNotes());
		}
	}
	
	public void writeElementMeasures(Element ele){
		if (chooserS[0]){
			sd.writeDouble(ele.getBeginTime()*ele.getTimeStep());
		}
		if (chooserS[1]){
			sd.writeFloat(ele.getTimelength());
		}
		if (chooserS[2]){
			sd.writeFloat(ele.getTimeBefore());
		}
		if (chooserS[3]){
			sd.writeFloat(ele.getTimeAfter());
		}
		if (chooserS[4]){
			sd.writeDouble(ele.getOverallPeak1());
		}
		if (chooserS[5]){
			sd.writeDouble(ele.getOverallPeak2());
		}	
		if (chooserS[6]){
			sd.writeDouble(ele.getMinFreq());
			sd.writeDouble(ele.getMaxFreq());
			sd.writeDouble(ele.getLowerQuartile());
			sd.writeDouble(ele.getUpperQuartile());
			sd.writeDouble(ele.getLower95tile());
			sd.writeDouble(ele.getUpper95tile());
			sd.writeDouble(ele.getEnergyVariance());
		}
		
		double[][] statistics=ele.getStatistics();
		for (int k=0; k<numVectorStatistics; k++){
			if (chooserV[k]){
				for (int a=0; a<numMeasures; a++){
					if(chooserM[a]){
						sd.writeDouble(statistics[k][a]);
					}
				}
			}
		}
	}
	
	public void writeSyllableMeasures(Element ele, int phraseRep, int numEls, boolean isPhrase){
		String [] syllableStatistics={"Within-syllable gap", "Between-syllable gap", "No. elements per syllable", "Syllable repetitions per phrase"};

		if (chooserSy[0]){sd.writeDouble(ele.getWithinSyllableGap());}

		if (chooserSy[1]){sd.writeDouble(ele.getBetweenSyllableGap());}
		
		if (chooserSy[2]){
			sd.writeInt(numEls);
		}
		if (chooserSy[3]){
			sd.writeInt(phraseRep);
		}
		sd.writeBoolean(isPhrase);
	}
	
	public String getTime(Song song){
		Calendar cal=Calendar.getInstance();
		cal.setTimeInMillis(song.getTDate());
		int hour=cal.get(Calendar.HOUR_OF_DAY);
		int minute=cal.get(Calendar.MINUTE);
		double second=cal.get(Calendar.SECOND)+(cal.get(Calendar.MILLISECOND)*0.001);
		String s=""+hour+":"+minute+":"+second;
		
		return s;
	}
	
	public String getDate(Song song){
		Calendar cal=Calendar.getInstance();
		cal.setTimeInMillis(song.getTDate());
		int day=cal.get(Calendar.DAY_OF_MONTH);
		int monthid=cal.get(Calendar.MONTH);
		int year=cal.get(Calendar.YEAR);
		String s=""+day+":"+monthid+":"+year;
		
		return s;
	}
	
	

	
	public void calculateRawOutput(){
		
		writeHeader(0);
		
		int amt=songs.length;		
		System.out.println("songs: "+amt);
		for (int i=0; i<amt; i++){
			String songDate=getDate(songs[i]);
			//System.out.println("elements: "+songs[i].eleList.size());
			String songTime=getTime(songs[i]);
			try{
				for (int j=0; j<songs[i].getNumElements(); j++){
					System.out.println(j);
					Element ele=(Element)songs[i].getElement(j);
					ele.calculateStatisticsS();
					ele.calculatePowerSpectrumStats();
					int[][] signal=ele.getSignal();
					/*
					int syll=-1;
					int aa=ele.getLength()-1;
					sd.writeString(songs[i].getIndividualName());
					int ns=songs[i].getNumSyllables();
					if (ns>0){
						for (int b=0; b<ns; b++){
							int[] dat=(int[])songs[i].getSyllable(b);
							if ((signal[0][0]*ele.getTimeStep()<dat[1])&&(signal[aa][0]*ele.getTimeStep()>dat[0])){
								syll=b+1;
							}
						}
					}
		
					else{
						syll=0;
					}
					*/
					
					LinkedList<Syllable> phrases=songs[i].getPhrases();
					int syll=0;
					for (int b=0; b<phrases.size(); b++){
						Syllable p=phrases.get(b);
						if (p.getElements().contains(ele)){
							syll=b;
						}
					}
					
					sd.writeString(songs[i].getName());
					sd.writeInt(syll);
					sd.writeInt(j+1);
				
					writeDetails(songs[i], ele, songDate, songTime);
					writeElementMeasures(ele);
					sd.writeLine();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void calculateCompressedPhrase(){
		writeHeader(1);
		
		int amt=songs.length;		
		for (int i=0; i<amt; i++){
			//System.out.println(i+" "+songs[i].name);
			try{
				//if (songs[i].getNumPhrases()==0){
					//songs[i].interpretSyllables();
				//}
				String songDate=getDate(songs[i]);
				String songTime=getTime(songs[i]);
				LinkedList<Syllable>phr=songs[i].getPhrases();
				int np=phr.size();
				//int np=songs[i].getNumPhrases();
				for (int j=0; j<np; j++){
					//int[][] phrase=(int[][])songs[i].getPhrase(j);
					Syllable ph=phr.get(j);
					LinkedList<Syllable>ch=ph.getSyllables();
					for (int k=0; k<ph.getMaxSyllLength(); k++){
						LinkedList<Element> eles=new LinkedList<Element>();
						
						for (Syllable s: ch){
							int q=s.getOffset()+k;
							if ((q>=0)&&(q<s.getNumEles())){
								eles.add(s.getElement(q));
							}
	
						}
						
						Element[] ele=eles.toArray(new Element[eles.size()]);
						
						int countEles=eles.size();
						for (int a=0; a<countEles; a++){
							ele[a].calculateStatisticsS();
							ele[a].calculatePowerSpectrumStats();
						}
						
					//for (int k=0; k<phrase[0].length; k++){
						//int countEles=0;
						//for (int a=0; a<phrase.length; a++){
							//if (phrase[a][k]!=-1){countEles++;}
						//}
						//Element [] ele=new Element [countEles];
						//countEles=0;
						//for (int a=0; a<phrase.length; a++){
							//if (phrase[a][k]!=-1){
								
								//Element elc=new Element((Element)songs[i].getElement(phrase[a][k]));
								
								//ele[countEles]=elc;
								//ele[countEles].calculateStatisticsS();
								//ele[countEles].calculatePowerSpectrumStats();
								//countEles++;
							//}
						//}
						Element eleM=new Element(ele, true);
					
						sd.writeString(songs[i].getIndividualName());
						sd.writeString(songs[i].getName());
						sd.writeInt(j+1);
						sd.writeInt(k+1);
						sd.writeInt(countEles);
					
						writeDetails(songs[i], eleM, songDate, songTime);
						writeElementMeasures(eleM);
						sd.writeLine();
					}
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public void calculateCompressedSyll(){
		writeHeader(2);
		int amt=songs.length;		
		for (int i=0; i<amt; i++){
			try{
				//if (songs[i].getNumPhrases()==0){
					//songs[i].interpretSyllables();
				//}
				
				checkTimeBefore(songs[i]);
				
				
				String songDate=getDate(songs[i]);
				String songTime=getTime(songs[i]);
				
				
				LinkedList<Syllable>phr=songs[i].getPhrases();
				int np=phr.size();
				//int np=songs[i].getNumPhrases();
				for (int j=0; j<np; j++){
					//int[][] phrase=(int[][])songs[i].getPhrase(j);
					Syllable ph=phr.get(j);
					LinkedList<Syllable>ch=ph.getSyllables();
					int[] counter=new int[ch.size()];
					Element[] eleSyl=new Element[ch.size()];
					for (int k=0; k<ch.size(); k++){
						Syllable s=ch.get(k);
					
						LinkedList<Element>eles=s.getElements();
						
						counter[k]=eles.size();
						Element[] ele=eles.toArray(new Element[eles.size()]);
						
						int countEles=eles.size();
						for (int a=0; a<countEles; a++){
							
							ele[a].calculateStatisticsS();
							ele[a].calculatePowerSpectrumStats();
						}
						eleSyl[k]=new Element(ele, false);
					}

					Element eleM=new Element(eleSyl, true);
						
					sd.writeString(songs[i].getIndividualName());
					sd.writeString(songs[i].getName());
					sd.writeInt(j+1);
					
					writeDetails(songs[i], eleM, songDate, songTime);
					writeElementMeasures(eleM);
					//writeSyllableMeasures(eleM, phrase[0].length, phrase.length, true);
					writeSyllableMeasures(eleM, ph.getMaxSyllLength(), ph.getNumSyllables(), true);
					
					sd.writeLine();
					//for (int k=0; k<phrase.length; k++){
					for (int k=0; k<ph.getMaxSyllLength(); k++){
						sd.writeString(songs[i].getIndividualName());
						sd.writeString(songs[i].getName());
						sd.writeInt(k+1);
						
						writeDetails(songs[i], eleSyl[k], songDate, songTime);
						writeElementMeasures(eleSyl[k]);
						
						writeSyllableMeasures(eleSyl[k], counter[k], 1, false);
						sd.writeLine();
					}	
				}
			}
			catch(Exception e){
				e.printStackTrace();
			
			}
		}
	}
	
	public void checkTimeBefore(Song song){
		
		int n=song.getNumElements();
		boolean checked=false;
		for (int i=0; i<n; i++){
			Element ele=(Element)song.getElement(i);
			if (ele.getTimeBefore()>-10000){
				checked=true;
			}
		}
		
		if (!checked){
			
			for (int i=1; i<n; i++){
				Element ele=(Element)song.getElement(i);
				Element ele2=(Element)song.getElement(i-1);
				ele.setTimeBefore(ele2.getTimeAfter());
			}
		}
	}
			
		
		
	
				
				
}
