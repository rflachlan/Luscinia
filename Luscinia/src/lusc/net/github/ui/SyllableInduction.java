package lusc.net.github.ui;

//
//  Syllable Induction.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/10/2006.
//  Copyright 2005 Robert Lachlan. All rights reserved.
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

import lusc.net.github.Defaults;
import lusc.net.github.Element;
import lusc.net.github.Song;
import lusc.net.github.analysis.AnalysisGroup;
import lusc.net.github.analysis.dendrograms.UPGMA;
import lusc.net.github.db.DataBaseController;
import lusc.net.github.ui.compmethods.DTWPanel;
import lusc.net.github.ui.compmethods.DTWSwingWorker;
import lusc.net.github.ui.spectrogram.MainPanel;


public class SyllableInduction extends JFrame implements ActionListener{
	
	float[][]scores=new float[1][1];
	float[][]scores2=new float[1][1];
	int[][]compressedStructure;
	double [] sd, average;
	UPGMA syllUpgma;

	int progress=0;
	int nextProgressTarget=10;
	
	int numElements=0;
	
	int individualNumber=0;
	
	int[][] individualTable;
	
	
	String[] names, names2, songNames;
	int[] songStructure;
	int songNumber=0;
	int[][] eleScheme;
	int[] order=new int[1];
	
	
	double simThresh=1.6;
	
	
	JButton start=new JButton("Start");
	
	JLabel thresholdLabel=new JLabel("Threshold for element distance");
	JFormattedTextField thresholdTB;
	double distanceThreshold=2;
				
	LinkedList[] elements;
	LinkedList songList=new LinkedList();
	LinkedList populationID=new LinkedList();
	LinkedList gapList=new LinkedList();
	LinkedList compID;
	
	int[] songRepSize, syllableRepSize, syllableTransitions, elementRepSize, elementsPerSong, syllablesPerSong;
	int[][] repsPerSyllable, elementsPerSyllable;
	
	boolean started=false;
	
	JLabel progressLabel=new JLabel("Waiting to start");
	
	DataBaseController dbc;
	NumberFormat num;
	Defaults defaults;
	MainPanel mp;
	AnalysisGroup sg;
	DTWPanel dtwPanel;
	Song song;
	
	
	Random r=new Random(System.currentTimeMillis());

	public SyllableInduction (DataBaseController dbc, Song song, Defaults defaults, MainPanel mp){
		
		this.dbc=dbc;
		this.defaults=defaults;
		this.mp=mp;
		this.song=song;
		
		song.sortSyllsEles();
		song.calculateGaps();
		num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(10);	
		songNumber=songList.size();
		Song[] songs=new Song[1];
		songs[0]=song;
		sg=new AnalysisGroup(songs, defaults);
		
		dtwPanel=new DTWPanel(dbc, sg, false, defaults);
		//dtwPanel.stitch.setSelectedIndex(0);
		start.addActionListener(this);
		
		makeFrame();
	}
	
	public void compress(){
		
		//dtwPanel.simpleAnalysis();
		DTWSwingWorker dtwsw=new DTWSwingWorker(null, dtwPanel, sg, defaults);
		//dtwsw.addPropertyChangeListener(this);
		//stopButton.setEnabled(true);
		dtwsw.execute();
		
		double results1[][]=calculateSimilarities(sg.getScoresEle(), 5);
		double results2[][]=calculateRhythm(song, 5, 20);
		results2=sparsifyResults(results2);
		results2=combineResults(results2, results1);
		double[][] resultsI=getReverseMat(results2);
		int n=results1.length;
		
		int[] best=new int[n];
		double bestScore=0;
		
		for (int i=0; i<10000; i++){
			int[] arr=getPartition(results2, resultsI);
			double sc=getPartitionScore(results2, arr);
			if (sc>bestScore){
				System.arraycopy(arr, 0, best, 0, n);
				bestScore=sc;
			}
		}
		
		LinkedList phrase_choice=new LinkedList();
		
		for (int i=0; i<n; i++){
			System.out.print(best[i]+" ");
			if (best[i]>0){
				int[] x={i, i+best[i]-1};
				phrase_choice.add(x);
			}
		}
		System.out.println();
		
		
		for (int i=1; i<n; i++){
			if (best[i]>0){
				System.out.println(results1[i-1][best[i]-1]);
				if (results1[i-1][best[i]-1]<simThresh){
					int j=i-1+best[i];
					while ((j<n)&&(results1[j][best[i]-1]<simThresh)){
						j+=best[i];
					}

					int[] x={i-best[i], j};
					System.out.println(i+" "+j);
					i+=j;
					phrase_choice.add(x);
				}
			}
		}
						
		
		
		
		
		
		song.setSyllList(new LinkedList());
		
		for (int i=0; i<phrase_choice.size(); i++){
			int[] x=(int[])phrase_choice.get(i);
			if ((x[0]<n)&&(x[1]<n)){
				Element ele1=(Element)song.getElement(x[0]);
				Element ele2=(Element)song.getElement(x[1]);
			
				int[] p={(int)((ele1.getBeginTime()-1)*ele1.getTimeStep()), (int)((ele2.getBeginTime()+ele2.getLength()+1)*ele2.getTimeStep())};
				song.addSyllable(p);
			}
		}
		 
		 
		
	}
	

	public void compress2(){	
		DTWSwingWorker dtwsw=new DTWSwingWorker(null, dtwPanel, sg, defaults);
		//dtwsw.addPropertyChangeListener(this);
		//stopButton.setEnabled(true);
		dtwsw.execute();
		//dtwPanel.startAnalysis();
	
		scores=sg.getScoresEle();
	
		double GAP_THRESHOLD=1.0;
		
		int size=scores.length;
		int range=20;

		double[][][] res=new double[size][range][];	
		
		//System.out.println("HERE WE ARE!");
				
		for (int i=0; i<size; i++){
			for (int j=0; j<range; j++){			//range is the length of the syllable we will consider
				int jj=j+i+1;						// jj is the start of the next repetition of the syllable after i
				int kk=i;
				int kj=1;
				double reps=0;
				int h=0;
				if (jj<size){res[i][j]=new double[size-jj];}
				double sum_distance=0;
				double max=0;
				for (int k=jj; k<size; k++){		//here we search ahead up to the end of the song.
					reps++;
					
					kk=k-j-1;
					
					sum_distance+=scores[k][kk];
					if (reps>j+1){
						kj=kk-j-1;
						sum_distance-=scores[kk][kj];
						reps--;
						if (sum_distance/reps>max){max=sum_distance/reps;}
						res[i][j][h]=max;
					}
					else if (reps==j+1){
						if (sum_distance/reps>max){max=sum_distance/reps;}
						res[i][j][h]=max;
					}
					else{
						res[i][j][h]=100;
					}
					//System.out.println(i+" "+j+" "+h+" "+res[i][j][h]);
					h++;
				}
			}
		}
		
		int[][]phrase_choice=new int[size][2];
		
		for (int i=0; i<size; i++){
			int longest_phrase=0;
			int rep_rate=-1;
			double score=0;
			for (int j=0; j<range; j++){
				if (res[i][j]!=null){
					for (int k=longest_phrase; k<res[i][j].length; k++){
						if (res[i][j][k]<distanceThreshold){
							longest_phrase=k;
							rep_rate=j;
							score=res[i][j][k];
						}
					}
				}
			}
			phrase_choice[i][0]=longest_phrase+rep_rate+1;
			phrase_choice[i][1]=rep_rate;
			//System.out.println((i+1)+" "+phrase_choice[i][0]+" "+phrase_choice[i][1]+" "+score+" "+longest_phrase+" "+rep_rate);
		}
		
		
		double relative_gaps[]=new double[size];
		double av_gap=0;
		for (int i=0; i<size; i++){
			Element ele=(Element)song.getElement(i);
			relative_gaps[i]=ele.getTimeAfter();
			if (i<size-1){av_gap+=ele.getTimeAfter();}
		}
		av_gap/=size-1.0;
		for (int i=0; i<size; i++){
			Element ele=(Element)song.getElement(i);
			relative_gaps[i]/=av_gap;
			if (i==size-1){relative_gaps[i]=1;}
		}
		
		for (int i=0; i<size; i++){
			if (phrase_choice[i][0]>0){
				int a=i+phrase_choice[i][0];
				for (int j=i; j<=a; j++){
					if (phrase_choice[j][0]+j>a){
						//System.out.println(relative_gaps[j-1]+" "+relative_gaps[j]);
						if (relative_gaps[j-1]>relative_gaps[j]){
							phrase_choice[i][0]--;
							j=a+1;
						}
					}
				}
			}
			else{
				for (int j=i; j<size; j++){
					if ((j==size-1)||(phrase_choice[j+1][0]>0)){
						phrase_choice[i][0]=j-i;
						phrase_choice[i][1]=j-i;
						int startplace=i;
						for (int k=i; k<j; k++){
							if (relative_gaps[k]>GAP_THRESHOLD){
								phrase_choice[startplace][0]=k-startplace;
								phrase_choice[startplace][1]=k-startplace;
								startplace=k;
							}
						}
						j=size;
					}
				}
			}
			i+=phrase_choice[i][0];
		}
		
		song.setSyllList(new LinkedList());
		
		for (int i=0; i<size; i++){
			int a=i+phrase_choice[i][0];
			Element ele1=(Element)song.getElement(i);
			Element ele2=(Element)song.getElement(a);
			System.out.println("Phrase: "+i+" "+a);
			int[] p={(int)((ele1.getBeginTime()-1)*ele1.getTimeStep()), (int)((ele2.getBeginTime()+ele2.getLength()+1)*ele2.getTimeStep())};

			song.addSyllable(p);
			
			
			if (phrase_choice[i][0]>phrase_choice[i][1]){
				double [] offset_scores=new double[phrase_choice[i][1]+1];
				double[] count=new double[phrase_choice[i][1]+1];
				int k=0;
				for (int j=i; j<=a; j++){
					offset_scores[k]+=relative_gaps[j];
					count[k]++;
					k++;
					if (k>phrase_choice[i][1]){k=0;}
				}
				double max=0;
				int loc=0;
				for (int j=0; j<count.length; j++){
					offset_scores[j]/=count[j];
					if (offset_scores[j]>max){
						max=offset_scores[j];
						loc=j+1;
					}
				}
				
				int b=i+loc-phrase_choice[i][1]-2;
				int c=i;
				boolean done=false;
				while (!done){
					c=b+1;
					if (c<i){c=i;}
					b+=phrase_choice[i][1]+1;
					//System.out.print(b+" "+a+" ");
					if (b>=a){
						b=a;
						done=true;
					}
					
					System.out.println(c+" "+b+" "+phrase_choice[i][1]+" "+a);
					
					ele1=(Element)song.getElement(c);
					ele2=(Element)song.getElement(b);
					//int q[]={ele1.begintime-1, (int)Math.round(ele2.begintime+ele2.length*ele1.timeStep+1)};
					int[] sy={(int)((ele1.getBeginTime()-1)*ele1.getTimeStep()), (int)((ele2.getBeginTime()+ele2.getLength()+1)*ele2.getTimeStep())};
					System.out.println("Syllable: "+c+" "+b);
					song.addSyllable(sy);
				}
			}
			i+=phrase_choice[i][0];
			
		}
		//System.out.println("New sylls: "+song.syllList.size());
		//if (output){dbc.writeSongMeasurements(song);}		
	}
	
	public void makeFrame(){
		JPanel contentpane=new JPanel();
		contentpane.setLayout(new BorderLayout());
		contentpane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		contentpane.add(dtwPanel, BorderLayout.CENTER);
		
		JPanel bottomPane=new JPanel(new BorderLayout());
		bottomPane.add(start, BorderLayout.WEST);
		bottomPane.add(progressLabel, BorderLayout.CENTER);
		contentpane.add(bottomPane, BorderLayout.SOUTH);
		
		
		this.setTitle("Comparison Parameters");
		this.getContentPane().add(contentpane);
		this.pack();
		this.setVisible(true);
	}


	public void clearUpFrame(){
		System.out.println("Comparison Window's closing!");
		System.out.println("Heap size is " + Runtime.getRuntime().totalMemory());
		System.out.println("Available memory: " + Runtime.getRuntime().freeMemory());
		System.gc();
		System.out.println("Heap size is " + Runtime.getRuntime().totalMemory());
		System.out.println("Available memory: " + Runtime.getRuntime().freeMemory());
	}
	
	public void closeUp(){
		System.out.println("Setting Window's closing!");
		System.out.println("Heap size is " + Runtime.getRuntime().totalMemory());
		System.out.println("Available memory: " + Runtime.getRuntime().freeMemory());
		scores=null;
		elements=null;
		songList=null;
		populationID=null;
		this.dispose();
		System.gc();
		System.out.println("Heap size is " + Runtime.getRuntime().totalMemory());
		System.out.println("Available memory: " + Runtime.getRuntime().freeMemory());
	}
	
	public void windowClosing(WindowEvent e){
		JFrame fr=(JFrame)e.getWindow();
		if (fr==this){
			closeUp();
		}
	}

	public void windowDeactivated(WindowEvent e){}
	public void windowActivated(WindowEvent e){}
	public void windowClosed(WindowEvent e){}
	public void windowDeiconified(WindowEvent e){}
	public void windowIconified(WindowEvent e){}
	public void windowOpened(WindowEvent e){}
	
	
	public void actionPerformed(ActionEvent e) {
			
		progressLabel.setText("Preparing elements...");
		Rectangle rect=progressLabel.getBounds();
		rect.x=0;
		rect.y=0;
		progressLabel.paintImmediately(rect);
		progress=0;
		nextProgressTarget=0;
			
		compress();
		mp.updateSyllables();
		closeUp();
		
		started=true;
	}
	
	public void updateProgressLabel(){
		progress++;
		if (progress>=nextProgressTarget){
			nextProgressTarget+=10;
			progressLabel.setText("Comparing elements: "+progress+" of "+scores.length);
			Rectangle rect=progressLabel.getBounds();
			rect.x=0;
			rect.y=0;
			progressLabel.paintImmediately(rect);
		}
	}
	
	public void remeasure(){
		clearUpFrame();
	}
	
	
	
	
	public double[][] calculateSimilarities(float[][] mat, int range){
		
		int n=mat.length;
		double[][] results=new double[n][range];
		
		double multiplier=1.1;
		
		for (int i=0; i<n; i++){
			System.out.print((i+1)+" ");
			for (int j=0; j<range; j++){
				results[i][j]=0;
				double count=0;
				for (int k=0; k<j+1; k++){
					
					int a=i-k;
					int b=i+j-k+1;
					
					if ((a>=0)&&(b<n)){
						results[i][j]+=mat[b][a]*mat[b][a];
						count++;
					}
					else{
						results[i][j]=1000000;
						count=1;
					}
				}
				results[i][j]/=count;
				results[i][j]*=Math.pow(multiplier, j);
				System.out.print(results[i][j]+" ");
			}
			System.out.println();
		}
		return results;
	}
	
	
	public double[][] calculateRhythm(Song song, int range, double threshold){
		
		int n=song.getNumElements();
		int n1=n+1;
		
		double[][] results=new double[n1][range];
			
		double[] t=new double[n1];
		for (int i=0; i<n; i++){
			Element ele=(Element)song.getElement(i);
			t[i+1]=ele.getTimeAfter()+5;
		}
		t[0]=50;
		t[n]=50;
		
		
		for (int i=0; i<n1; i++){
			System.out.print((i+1)+" ");
			for (int j=0; j<range; j++){
				//results[i][j]=t[i];
				double count=0;
				double sc=0;
				double maxsc=1;
				for (int k=1; k<j+1; k++){
					
					int a=i+k;
					
					if (a<n1){
						if (t[a]>0){
							sc+=t[a];
							if (t[a]>maxsc){maxsc=t[a];}
						}
						count++;
					}
					else{
						//results[i][j]=1000000;
						count=0;
					}
				}
				if (count>0){
					
					//double x=sc/count;
					double x=maxsc;
					if(x<1){x=1;}
					
					x=t[i]/x;
					if (x>3){
						results[i][j]=3;
					}
					else if (x>0.75){
						results[i][j]=x;
					}
					
				}
				else if(j==0){
					results[i][j]=1.2;
				}
				System.out.print(results[i][j]+" ");
			}
			System.out.println();
		}

		return results;
	}
	
	public double[][] sparsifyResults(double[][] input){
		
		int n=input.length;
		int m=input[0].length;
		
		double[][] results=new double[n][m];
		
		for (int i=0; i<n; i++){
			for (int j=0; j<m; j++){
				if (input[i][j]>0){
					int k=i+j+1;
					boolean found=false;
					if (k==n){found=true;}
					else if(k<n){
						for (int a=0; a<m; a++){
							if (input[k][a]>0){
								found=true;
								a=m;
							}
						}
					}
					if (found){
						results[i][j]=input[i][j];
					}
				}
			}
		}
		
		return results;
	}
		
	public double[][] combineResults(double[][] input1, double[][] input2){
		
		int n=input1.length;
		int m=input1[0].length;
		
		int n2=input2.length;
		
		
		for (int i=0; i<n; i++){
			for (int j=0; j<m; j++){
				if (input1[i][j]>0){
					
					double x=1000000;
					
					int ii=i-1;
					
					if (ii>0){
						x=input2[ii][j];
					}
					
					int k=ii+j+1;
					if ((k<n2)&&(input2[k][j]<x)){
						x=input2[k][j];
					}
					k=ii-j-1;
					//if ((k>=0)&&(input2[k][j]<x)){
					//	x=input2[k][j];
					//}
					if (x>4){x=4;}
					input1[i][j]/=x;
				}
				System.out.print(input1[i][j]+" ");
			}
			System.out.println();
		}
		
		return input1;
	}
	
	public double[][] getReverseMat(double[][] input){
		int n=input.length;
		int m=input[0].length;
		
		double[][] results=new double[n][m];
		
		for (int i=0; i<n; i++){
			for (int j=0; j<m; j++){
				if ((input[i][j]>0)&&(i+j+1<n)){
					results[i+j+1][j]=input[i][j];
				}
			}
		}
		return results;
	}
	
	public int[] getPartition(double[][] input1, double[][] input2){
		int n=input1.length;
		int m=input1[0].length;
		
		
		int[] results=new int[n];
		
		double[] tots1=new double[n];
		double[] tots2=new double[n];
		
		for (int i=0; i<n; i++){
			for (int j=0; j<m; j++){
				tots1[i]+=input1[i][j];
				tots2[i]+=input2[i][j];
			}
		}
		
		int a=r.nextInt(n);
		while (tots1[a]==0){
			a=r.nextInt(n);
		}
		
		//forward
		int b=a;
		while (a<n){
			//System.out.print(a+" ");
			double x=r.nextDouble()*tots1[a];
			double y=0;
			for (int i=0; i<m; i++){
				y+=input1[a][i];
				if (y>x){
					results[a]=i+1;
					a+=i+1;
					i=m;
				}
			}
		}
		//System.out.println();
		
		//backward
		while (b>0){
			double x=r.nextDouble()*tots2[b];
			double y=0;
			for (int i=0; i<m; i++){
				y+=input2[b][i];
				if (y>x){
					b-=i+1;
					results[b]=i+1;
					i=m;
				}
			}
		}
		
		return results;
	}
	
	public double getPartitionScore(double[][]input, int[] partition){
	
		double c=0;
		double d=0;
		int n=partition.length;
		for (int i=0; i<n; i++){
			
			if (partition[i]>0){
				c++;
				d+=input[i][partition[i]-1]*partition[i];
			}
		}
		
		return (d/(n+0.0));

	}
}
