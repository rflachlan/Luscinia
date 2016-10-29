package lusc.net.github.analysis.syntax;

import java.util.LinkedList;

import lusc.net.github.analysis.ComparisonResults;
import lusc.net.github.ui.SyntaxOptions;
//
//  EntropyAnalysis.java
//  Luscinia
//
//  Created by Robert Lachlan on 11/6/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

public class EntropyAnalysis {
	
	int type=0;
	double[][] mat;
	int n=0;
	int mode=2;
	int maxK=0;
	int minK=0;
	
	long[] rollingtimes;
	
	SWMLEntropyEstimate[] swml;
	MarkovChain[] mkc;
	SWMLNoCategories snc;
	
	double[][] swmltrajectory, mkctrajectory;
	
	SyntaxClusteringThread[] sct;
	int[][] overallAssignments;
	
	boolean rolling=false;
	boolean byIndividual=false;
	int window=50;
	
	//public EntropyAnalysis (double[][] dMat, int maxK, int[][] individuals, int[][] lookUps, int type, int mode){

	public EntropyAnalysis (ComparisonResults cr, SyntaxOptions sop, boolean rolling, int window, boolean byIndividual){
		this.type=cr.getType();
		this.maxK=sop.maxSyntaxK;
		this.minK=sop.minSyntaxK;
		this.mode=sop.syntaxMode;
		this.rolling=rolling;
		this.byIndividual=byIndividual;
		this.window=window;
		makeMatrix(cr.getDissT());		
		
		n=mat.length;
		
		if (maxK>n/2){
			maxK=n/2;
		}
		
		int[][] songLabels=getSongLabels(cr.getLookUp());
		int[][] individuals=cr.getIndividualSongs();
		
		
		if (mode==1){
			swml=new SWMLEntropyEstimate[maxK-minK+1];
			swmltrajectory=new double[maxK-minK+1][];
		}
		if (mode==0){
			mkc=new MarkovChain[maxK-minK+1];
			mkctrajectory=new double[maxK-minK+1][];
		}
		if (mode==2){
			snc=new SWMLNoCategories(cr, sop.thresh);
		}
		
		if (mode<2){
		
		overallAssignments=new int[maxK-minK+1][];
		
		if (rolling){
			calculateTimes(cr.getSongDates());
		}
		
		System.out.println("INDIVIDUALS");
		for (int i=0; i<individuals.length; i++){
			for (int j=0; j<individuals[i].length; j++){
				System.out.print(individuals[i][j]+" ");
			}
			System.out.println();
		}
		
		System.out.println("SONGS "+mat.length);
		for (int i=0; i<songLabels.length; i++){
			System.out.print((i+1)+" ");
			for (int j=0; j<songLabels[i].length; j++){
				System.out.print(songLabels[i][j]+" ");
			}
			System.out.println();
		}
		if (mode<2){
			clusterOnEntropy(individuals, songLabels);
		}
		}
		mat=null;
	}
	
	public void calculateTimes(long[] input){
		int n=input.length;
		rollingtimes=new long[n-window];
		
		for (int i=window; i<input.length; i++){
			long t=0;
			int s=0;
			for (int j=i-window; j<i; j++){
				t+=input[j];
				s++;
			}
			rollingtimes[i-window]=t/s;
		}
	}
	
	public long[] getTimes(){
		return rollingtimes;
	}
	
	public int getType(){
		return type;
	}
	
	public int getMode(){
		return mode;
	}
	
	public int getAssignmentLength(){
		return overallAssignments.length;
	}
	
	public int[][] getOverallAssignment(){
		return overallAssignments;
	}
	
	public SWMLEntropyEstimate[] getSWMLEntropyEstimate(){
		return swml;
	}
	
	public MarkovChain[] getMarkovChains(){
		return mkc;
	}
	
	public SWMLNoCategories getSWMLNC(){
		return snc;
	}
	
	public double[][] getSWMLTrajectory(){
		return swmltrajectory;
	}
	
	public double[][] getMKCTrajectory(){
		return mkctrajectory;
	}
	
	public void clusterOnEntropy(int[][]individuals, int[][]songs){
				
		int ncores=Runtime.getRuntime().availableProcessors()/2;
		swml=new SWMLEntropyEstimate[maxK-1];
		sct=new SyntaxClusteringThread[maxK-minK+1];
				
		for (int i=minK; i<=maxK; i++){
			sct[i-minK]=new SyntaxClusteringThread(i, mat, individuals, songs, mode, rolling, window, byIndividual);
			sct[i-minK].setPriority(Thread.MIN_PRIORITY);
			sct[i-minK].start();
		}
		
		try{
			for (int i=minK; i<=maxK; i++){
				sct[i-minK].join();

			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
		for (int i=minK; i<=maxK; i++){
			overallAssignments[i-minK]=sct[i-minK].assignments;
			if (mode>0){
				swml[i-minK]=sct[i-minK].swml;
				if (rolling){
					swmltrajectory[i-minK]=sct[i-minK].swtrajectory;
				}
			}
			if (mode!=1){
				mkc[i-minK]=sct[i-minK].mkc;
				if (rolling){
					mkctrajectory[i-minK]=sct[i-minK].mktrajectory;
				}
			}
		}
		
		for (int i=minK; i<=maxK; i++){
			sct[i-minK]=null;
		}	
	}
	
	public void makeMatrix(double[][] dMat){
		int n=dMat.length;
		mat=new double[n][n];
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				double c=dMat[i][j]*dMat[i][j];
				mat[i][j]=c;
				mat[j][i]=c;
			}
		}
	}
	
	public int[][] getSongLabels(int[][]lookUps){
		int numSongs=0;
		for (int i=0; i<lookUps.length; i++){
			if (lookUps[i][0]>numSongs){numSongs=lookUps[i][0];}
		}
		numSongs++;
		int[][] songLabel=new int[numSongs][];
		int nc=0;
		for (int i=0; i<numSongs; i++){
			int count=0;
			for (int j=0; j<lookUps.length; j++){
				if (lookUps[j][0]==i){count++;}
			}
			if (count==0){nc++;}
			songLabel[i]=new int[count];
			count=0;
			for (int j=0; j<lookUps.length; j++){
				if (lookUps[j][0]==i){
					songLabel[i][count]=j;
					count++;
				}
			}
		}
		int ns=numSongs-nc;
		int[][] songLabel2=new int[ns][];
		int j=0;
		for (int i=0; i<numSongs; i++){
			if (songLabel[i].length>0){
				songLabel2[j]=songLabel[i];
				j++;
			}
		}
		return songLabel2;
	}
	
}
