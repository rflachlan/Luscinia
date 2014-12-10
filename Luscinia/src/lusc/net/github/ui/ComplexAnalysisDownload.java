package lusc.net.github.ui;


//
//  ComparisonFrame.java
//  Luscinia
//
//  Created by Robert Lachlan on 7/2/07.
//  Copyright 2007 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import javax.swing.*;

import java.awt.event.*;
import java.awt.image.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.*;
import java.text.*;
import java.io.*;
import java.util.*;

//import lusc.net.github.analysis.SongGroup;
import lusc.net.github.analysis.AnalysisGroup;
import lusc.net.github.Defaults;
import lusc.net.github.Song;

public class ComplexAnalysisDownload extends JPanel implements PropertyChangeListener {

	
	JRadioButton element=new JRadioButton("By element: ", true);
	JRadioButton syllable=new JRadioButton("By syllable: ", true);
	JRadioButton syllableTransition=new JRadioButton("By syllable transition: ", true);
	JRadioButton song=new JRadioButton("By song: ", true);
	JRadioButton elementCompression=new JRadioButton("Compress element distance: ", true);
	JRadioButton useTransForSong=new JRadioButton("Use syllable transitions for song distance: ", true);
	JFormattedTextField thresholdFactor;
	double threshold;
	AnalysisGroup ag;
	//SongGroup sg;
	Defaults defaults;
	
	double songUpperLimit=0.5;
	double songLowerLimit=20;
		

	//public ComplexAnalysisDownload (SongGroup sg, Defaults defaults){
	public ComplexAnalysisDownload (AnalysisGroup ag, Defaults defaults){
		//this.sg=sg;
		this.ag=ag;
		this.defaults=defaults;
		
		this.setLayout(new GridLayout(0,1));
		
		this.add(element);
		this.add(syllable);
		this.add(syllableTransition);
		this.add(song);
		this.add(elementCompression);
		this.add(useTransForSong);
		
		JLabel thresholdLabel=new JLabel("Threshold: ");
		JPanel thresholdPanel=new JPanel(new BorderLayout());
		thresholdPanel.add(thresholdLabel, BorderLayout.WEST);
		thresholdFactor=new JFormattedTextField();
		thresholdFactor.setValue(new Double(threshold));
		thresholdFactor.setColumns(10);
		thresholdFactor.addPropertyChangeListener("value", this);
		thresholdPanel.add(thresholdFactor, BorderLayout.CENTER);
		
		this.add(thresholdPanel);
		
		JLabel instruction=new JLabel("Click 'next' to save");
		this.add(instruction);
	}
	
	public void compressResults(){
			
		boolean anacomp=elementCompression.isSelected();
		boolean anael=element.isSelected();
		boolean anasy=syllable.isSelected();
		boolean anast=syllableTransition.isSelected();
		boolean anaso=song.isSelected();
	
	
		if (anacomp){
			//sg.compressElements();
			ag.compressElements();
		}
		
		if ((anasy)||(anast)||(anaso)){
			//sg.compressSyllables2();
			//sg.compressSyllables3();
			//sg.compressSyllableTransitions();
			ag.compressSyllableTransitions();
		}
		
		if ((anast)||(anaso)){
			//sg.compressSyllableTransitions();
		}
		
		if (anaso){
			//sg.compressSongs(useTransForSong, songUpperLimit, songLowerLimit);
			ag.compressSongs(useTransForSong.isSelected(), songUpperLimit, songLowerLimit);
			//sg.compressSongsAsymm();
		}
	}


	public void export(){
		SaveDocument sd=new SaveDocument(this, defaults);
		boolean readyToWrite=sd.makeFile();
		//sg.normalizeScores(sg.scoresSyll);
		if (readyToWrite){
			if (song.isSelected()){
				
				sd.writeString("BirdName1");
				sd.writeString("BirdName2");
				sd.writeString("Song1");
				sd.writeString("Song2");
				sd.writeString("Distance score");
				sd.writeLine();
				//int ns=sg.getNSongs();
				int ns=ag.getLengths(4);
				//Song[] songs=sg.getSongs();
				Song[] songs=ag.getSongs();
				//boolean[][] compScheme=sg.getCompScheme();
				boolean[][] compScheme=ag.getCompScheme();
				//float[][] scoresSong=sg.getScoresSong();
				float[][] scoresSong=ag.getScoresSong();
				
				for (int i=0; i<ns; i++){
					for (int j=0; j<ns; j++){
					
						String s1=songs[i].getName();
						if (s1.endsWith(".wav")){
							int len=s1.length();
							s1=s1.substring(0,len-4);
						}
						String s2=songs[j].getName();
						if (s2.endsWith(".wav")){
							int len=s2.length();
							s2=s2.substring(0,len-4);
						}
					
						if (compScheme[i][j]){
							sd.writeString(songs[i].getIndividualName());
							sd.writeString(songs[j].getIndividualName());
							sd.writeString(s1);
							sd.writeString(s2);
							if (i>j){
								sd.writeDouble(scoresSong[i][j]);
							}
							else{
								sd.writeDouble(scoresSong[j][i]);
							}
							sd.writeInt(songs[i].getNumPhrases());
							sd.writeInt(songs[j].getNumPhrases());
							
							int sharedSylls=ag.getSharedSyllCount(i, j, threshold);
							sd.writeInt(sharedSylls);
							
							int sharedTrans=ag.getSharedTransCount(i, j, threshold);
							sd.writeInt(sharedTrans);
							
							double weightedTrans=ag.getWeightedSharedTransCount(i, j, threshold);
							sd.writeDouble(weightedTrans);
							
							sd.writeLine();
						}
					}
				}
				
				sd.writeString("BirdName");
				sd.writeString("Song");
				sd.writeString("Mean distance");
				sd.writeString("Median distance");
				sd.writeString("1% percentile");
				sd.writeString("5% percentile");
				sd.writeString("10% percentile");
				sd.writeString("25% percentile");
				sd.writeLine();

				
				for (int i=0; i<ns; i++){
					
					int count=0;
					for (int j=0; j<ns; j++){
						if (compScheme[i][j]||compScheme[j][i]){
							count++;
						}
					}
					float[] rVec=new float[count];
					count=0;
					float sum=0;
					for (int j=0; j<scoresSong.length; j++){
						if (compScheme[i][j]||compScheme[j][i]){
							if (i>j){
								rVec[count]=scoresSong[i][j];
							}
							else{
								rVec[count]=scoresSong[j][i];
							}
							sum+=rVec[count];
							count++;
							
						}
					}
					
					Arrays.sort(rVec);
					
					sd.writeString(songs[i].getIndividualName());
					sd.writeString(songs[i].getName());
					sd.writeDouble(sum/(count+0.0));
					
					double percentiles[]={0.5, 0.01, 0.05, 0.1, 0.25};
					
					for (int w=0; w<percentiles.length; w++){
						double x=count*percentiles[w];
						double y=Math.floor(x);
						if (y==x){
							sd.writeFloat(rVec[(int)x]);
						}
						else{
							int place=(int)y;
							x-=y;
							double sides=rVec[place]*(1-x)+rVec[place+1]*x;
							sd.writeDouble(sides);
						}
					}
					sd.writeLine();
				}				
				
			}
			if (syllableTransition.isSelected()){
				
				sd.writeString("BirdName1");
				sd.writeString("BirdName2");
				sd.writeString("Song1");
				sd.writeString("Song2");
				sd.writeString("Syl. transition 1");
				sd.writeString("Syl. transition 2");
				sd.writeString("Distance score");
				sd.writeLine();
				
				int nt=ag.getLengths(3);
				Song[] songs=ag.getSongs();
				boolean[][] compScheme=ag.getCompScheme();
				float[][] scoreTrans=ag.getScoresTrans();
				int[][] lookUpTrans=ag.getLookUp(3);
				
				for (int i=0; i<nt; i++){
					int a=lookUpTrans[i][0];
					for (int j=0; j<i; j++){
						int b=lookUpTrans[j][0];
						if (compScheme[a][b]||compScheme[b][a]){
							sd.writeString(songs[a].getIndividualName());
							sd.writeString(songs[b].getIndividualName());
							sd.writeString(songs[a].getName());
							sd.writeString(songs[b].getName());
							sd.writeInt(lookUpTrans[i][1]+1);
							sd.writeInt(lookUpTrans[j][1]+1);
							sd.writeDouble(scoreTrans[i][j]);
							sd.writeLine();
						}
					}
				}
			}	
				
			if (syllable.isSelected()){
				
				sd.writeString("BirdName1");
				sd.writeString("BirdName2");
				sd.writeString("Song1");
				sd.writeString("Song2");
				sd.writeString("Syllable1");
				sd.writeString("Syllable2");
				sd.writeString("Distance score");
				sd.writeLine();
				
				int ns=ag.getLengths(2);
				Song[] songs=ag.getSongs();
				boolean[][] compScheme=ag.getCompScheme();
				float[][] scoresSyll=ag.getScoresSyll();
				int[][] lookUpSyls=ag.getLookUp(2);
				
				for (int i=0; i<ns; i++){
					int a=lookUpSyls[i][0];
					for (int j=0; j<i; j++){
						int b=lookUpSyls[j][0];
						if (compScheme[a][b]||compScheme[b][a]){
							sd.writeString(songs[a].getIndividualName());
							sd.writeString(songs[b].getIndividualName());
							sd.writeString(songs[a].getName());
							sd.writeString(songs[b].getName());
							sd.writeInt(lookUpSyls[i][1]+1);
							sd.writeInt(lookUpSyls[j][1]+1);
							sd.writeDouble(scoresSyll[i][j]);
							sd.writeLine();
						}
					}
				}
				
				
				sd.writeString("BirdName1");
				sd.writeString("Song");
				sd.writeString("Syllable");
				sd.writeString("Mean distance");
				sd.writeString("Median distance");
				sd.writeString("1% percentile");
				sd.writeString("5% percentile");
				sd.writeString("10% percentile");
				sd.writeString("25% percentile");
				sd.writeLine();

				
				for (int i=0; i<scoresSyll.length; i++){
					
					int a=lookUpSyls[i][0];
					int count=0;
					for (int j=0; j<scoresSyll.length; j++){
						int b=lookUpSyls[j][0];
						if (compScheme[a][b]||compScheme[b][a]){
							count++;
						}
					}
					float[] rVec=new float[count];
					count=0;
					float sum=0;
					for (int j=0; j<scoresSyll.length; j++){
						int b=lookUpSyls[j][0];
						if (compScheme[a][b]||compScheme[b][a]){
							if (i>j){
								rVec[count]=scoresSyll[i][j];
							}
							else{
								rVec[count]=scoresSyll[j][i];
							}
							sum+=rVec[count];
							count++;
							
						}
					}
					
					Arrays.sort(rVec);
					
					sd.writeString(songs[a].getIndividualName());
					sd.writeString(songs[a].getName());
					sd.writeInt(lookUpSyls[i][1]+1);
					sd.writeDouble(sum/(count+0.0));
					
					double percentiles[]={0.5, 0.01, 0.05, 0.1, 0.25};
					
					for (int w=0; w<percentiles.length; w++){
						double x=count*percentiles[w];
						double y=Math.floor(x);
						if (y==x){
							sd.writeFloat(rVec[(int)x]);
						}
						else{
							int place=(int)y;
							x-=y;
							double sides=rVec[place]*(1-x)+rVec[place+1]*x;
							sd.writeDouble(sides);
						}
					}
					sd.writeLine();
				}
			}
			if (element.isSelected()){
				sd.writeString("BirdName1");
				sd.writeString("BirdName2");
				sd.writeString("Song1");
				sd.writeString("Song2");
				sd.writeString("Element1");
				sd.writeString("Element2");
				sd.writeString("Distance score");
				sd.writeLine();
				
				int ns=ag.getLengths(0);
				Song[] songs=ag.getSongs();
				boolean[][] compScheme=ag.getCompScheme();
				float[][] scoresEl=ag.getScoresEleC();
				int[][] lookUpEls=ag.getLookUp(1);
				
				if (!ag.getCompressElements()){
					scoresEl=ag.getScoresEle();
					lookUpEls=ag.getLookUp(0);
				}
				
				for (int i=0; i<scoresEl.length; i++){
					int a=lookUpEls[i][0];
					for (int j=0; j<i; j++){
						int b=lookUpEls[j][0];
						if (compScheme[a][b]||compScheme[b][a]){
							sd.writeString(songs[a].getIndividualName());
							sd.writeString(songs[b].getIndividualName());
							sd.writeString(songs[a].getName());
							sd.writeString(songs[b].getName());
							sd.writeInt(lookUpEls[i][2]+1);
							sd.writeInt(lookUpEls[j][2]+1);
							sd.writeDouble(scoresEl[i][j]);
							sd.writeLine();
						}
					}
				}
			}
		}
		sd.finishWriting();	
	}
	
	public void propertyChange(PropertyChangeEvent e) {
        Object source = e.getSource();
        if (source==thresholdFactor){
			double s=((Number)thresholdFactor.getValue()).doubleValue();
			if (s<01){s=0;}
			threshold=s;
			thresholdFactor.setValue(new Double(s));
		}
	}
}