package lusc.net.github;

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


public class ComplexAnalysisDownload extends JPanel {

	
	JRadioButton element=new JRadioButton("By element: ", true);
	JRadioButton syllable=new JRadioButton("By syllable: ", true);
	JRadioButton syllableTransition=new JRadioButton("By syllable transition: ", true);
	JRadioButton song=new JRadioButton("By song: ", true);
	JRadioButton elementCompression=new JRadioButton("Compress element distance: ", true);
	JRadioButton useTransForSong=new JRadioButton("Use syllable transitions for song distance: ", true);
		
	SongGroup sg;
		

	public ComplexAnalysisDownload (SongGroup sg){
		this.sg=sg;
		
		this.setLayout(new GridLayout(0,1));
		
		this.add(element);
		this.add(syllable);
		this.add(syllableTransition);
		this.add(song);
		this.add(elementCompression);
		this.add(useTransForSong);
		
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
			sg.compressElements();
		}
		
		if ((anasy)||(anast)||(anaso)){
			sg.compressSyllables2();
			if (sg.scoresSyll2!=null){
				sg.compressSyllables3();
			}
		}
		
		if ((anast)||(anaso)){
			sg.compressSyllableTransitions();
		}
		
		if (anaso){
			sg.compressSongsAsymm();
		}
	}


	public void export(){
		SaveDocument sd=new SaveDocument(this, sg.defaults);
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
				for (int i=0; i<sg.scoresSong.length; i++){
					for (int j=0; j<sg.scoresSong.length; j++){
					
						String s1=sg.songs[i].name;
						if (s1.endsWith(".wav")){
							int len=s1.length();
							s1=s1.substring(0,len-4);
						}
						String s2=sg.songs[j].name;
						if (s2.endsWith(".wav")){
							int len=s2.length();
							s2=s2.substring(0,len-4);
						}
					
						if (sg.compScheme[i][j]){
							sd.writeString(sg.songs[i].individualName);
							sd.writeString(sg.songs[j].individualName);
							sd.writeString(s1);
							sd.writeString(s2);
							if (i>j){
								sd.writeDouble(sg.scoresSong[i][j]);
							}
							else{
								sd.writeDouble(sg.scoresSong[j][i]);
							}
							sd.writeInt(sg.songs[i].phrases.size());
							sd.writeInt(sg.songs[j].phrases.size());
							
							int sharedSylls=sg.getSharedSyllCount(i, j);
							sd.writeInt(sharedSylls);
							
							int sharedTrans=sg.getSharedTransCount(i, j);
							sd.writeInt(sharedTrans);
							
							double weightedTrans=sg.getWeightedSharedTransCount(i, j);
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

				
				for (int i=0; i<sg.scoresSong.length; i++){
					
					int count=0;
					for (int j=0; j<sg.scoresSong.length; j++){
						if (sg.compScheme[i][j]||sg.compScheme[j][i]){
							count++;
						}
					}
					float[] rVec=new float[count];
					count=0;
					float sum=0;
					for (int j=0; j<sg.scoresSong.length; j++){
						if (sg.compScheme[i][j]||sg.compScheme[j][i]){
							if (i>j){
								rVec[count]=sg.scoresSong[i][j];
							}
							else{
								rVec[count]=sg.scoresSong[j][i];
							}
							sum+=rVec[count];
							count++;
							
						}
					}
					
					Arrays.sort(rVec);
					
					sd.writeString(sg.songs[i].individualName);
					sd.writeString(sg.songs[i].name);
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
				for (int i=0; i<sg.scoreTrans.length; i++){
					int a=sg.lookUpTrans[i][0];
					for (int j=0; j<i; j++){
						int b=sg.lookUpTrans[j][0];
						if (sg.compScheme[a][b]||sg.compScheme[b][a]){
							sd.writeString(sg.songs[a].individualName);
							sd.writeString(sg.songs[b].individualName);
							sd.writeString(sg.songs[a].name);
							sd.writeString(sg.songs[b].name);
							sd.writeInt(sg.lookUpTrans[i][1]+1);
							sd.writeInt(sg.lookUpTrans[j][1]+1);
							sd.writeDouble(sg.scoreTrans[i][j]);
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
				for (int i=0; i<sg.scoresSyll.length; i++){
					int a=sg.lookUpSyls[i][0];
					for (int j=0; j<i; j++){
						int b=sg.lookUpSyls[j][0];
						if (sg.compScheme[a][b]||sg.compScheme[b][a]){
							sd.writeString(sg.songs[a].individualName);
							sd.writeString(sg.songs[b].individualName);
							sd.writeString(sg.songs[a].name);
							sd.writeString(sg.songs[b].name);
							sd.writeInt(sg.lookUpSyls[i][1]+1);
							sd.writeInt(sg.lookUpSyls[j][1]+1);
							sd.writeDouble(sg.scoresSyll[i][j]);
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

				
				for (int i=0; i<sg.scoresSyll.length; i++){
					
					int a=sg.lookUpSyls[i][0];
					int count=0;
					for (int j=0; j<sg.scoresSyll.length; j++){
						int b=sg.lookUpSyls[j][0];
						if (sg.compScheme[a][b]||sg.compScheme[b][a]){
							count++;
						}
					}
					float[] rVec=new float[count];
					count=0;
					float sum=0;
					for (int j=0; j<sg.scoresSyll.length; j++){
						int b=sg.lookUpSyls[j][0];
						if (sg.compScheme[a][b]||sg.compScheme[b][a]){
							if (i>j){
								rVec[count]=sg.scoresSyll[i][j];
							}
							else{
								rVec[count]=sg.scoresSyll[j][i];
							}
							sum+=rVec[count];
							count++;
							
						}
					}
					
					Arrays.sort(rVec);
					
					sd.writeString(sg.songs[a].individualName);
					sd.writeString(sg.songs[a].name);
					sd.writeInt(sg.lookUpSyls[i][1]+1);
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
				if (sg.compressElements){
					for (int i=0; i<sg.scoresEleC.length; i++){
						int a=sg.lookUpElsC[i][0];
						for (int j=0; j<i; j++){
							int b=sg.lookUpElsC[j][0];
							if (sg.compScheme[a][b]||sg.compScheme[b][a]){
								sd.writeString(sg.songs[a].individualName);
								sd.writeString(sg.songs[b].individualName);
								sd.writeString(sg.songs[a].name);
								sd.writeString(sg.songs[b].name);
								sd.writeInt(sg.lookUpElsC[i][2]+1);
								sd.writeInt(sg.lookUpElsC[j][2]+1);
								sd.writeDouble(sg.scoresEleC[i][j]);
								sd.writeLine();
							}
						}
					}
				}
				else{
					for (int i=0; i<sg.scoresEle.length; i++){
						int a=sg.lookUpEls[i][0];
						for (int j=0; j<i; j++){
							int b=sg.lookUpEls[j][0];
							if (sg.compScheme[a][b]||sg.compScheme[b][a]){
								sd.writeString(sg.songs[a].individualName);
								sd.writeString(sg.songs[b].individualName);
								sd.writeString(sg.songs[a].name);
								sd.writeString(sg.songs[b].name);
								sd.writeInt(sg.lookUpEls[i][1]+1);
								sd.writeInt(sg.lookUpEls[j][1]+1);
								sd.writeDouble(sg.scoresEle[i][j]);
								sd.writeLine();
							}
						}
					}
				}
			}
		}
		sd.finishWriting();	
	}
}