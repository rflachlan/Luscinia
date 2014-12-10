package lusc.net.github.analysis.syntax;

import java.util.LinkedList;
//
//  EntropyAnalysis.java
//  Luscinia
//
//  Created by Robert Lachlan on 11/6/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//






public class EntropyAnalysis {
	
	int type=0;
	float[][] mat;
	int n=0;
	int mode=2;
	int maxK=0;
	
	SWMLEntropyEstimate[] swml;
	MarkovChain[] mkc;
	int[][] overallAssignments;
	
	public EntropyAnalysis (float[][] dMat, int maxK, int[][] individuals, int[][] lookUps, int type, int mode){
		this.type=type;
		this.maxK=maxK;
		this.mode=mode;
		makeMatrix(dMat);		
		
		n=mat.length;
		
		if (maxK>n/2){
			maxK=n/2;
		}
		
		int[][] songLabels=getSongLabels(lookUps);
		
		
		
		if (mode>0){
			swml=new SWMLEntropyEstimate[maxK-1];
		}
		if (mode!=1){
			mkc=new MarkovChain[maxK-1];
		}
		overallAssignments=new int[maxK-1][];
		
		System.out.println("INDIVIDUALS");
		for (int i=0; i<individuals.length; i++){
			for (int j=0; j<individuals[i].length; j++){
				System.out.print(individuals[i][j]+" ");
			}
			System.out.println();
		}
		
		System.out.println("SONGS "+dMat.length);
		for (int i=0; i<songLabels.length; i++){
			System.out.print((i+1)+" ");
			for (int j=0; j<songLabels[i].length; j++){
				System.out.print(songLabels[i][j]+" ");
			}
			System.out.println();
		}
		
		clusterOnEntropy(individuals, songLabels);
		mat=null;
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
	
	public void clusterOnEntropy(int[][]individuals, int[][]songs){
				
		int ncores=Runtime.getRuntime().availableProcessors()/2;
		swml=new SWMLEntropyEstimate[maxK-1];
		SyntaxClusteringThread[] sct=new SyntaxClusteringThread[maxK-1];
				
		for (int i=2; i<=maxK; i++){
			sct[i-2]=new SyntaxClusteringThread(i, mat, individuals, songs, mode);
			sct[i-2].setPriority(Thread.MIN_PRIORITY);
			sct[i-2].start();
		}
		
		try{
			for (int i=2; i<=maxK; i++){
				sct[i-2].join();

			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
		for (int i=2; i<=maxK; i++){
			overallAssignments[i-2]=sct[i-2].assignments;
			if (mode>0){
				swml[i-2]=sct[i-2].swml;
			}
			if (mode!=1){
				mkc[i-2]=sct[i-2].mkc;
			}
		}
		
		for (int i=2; i<=maxK; i++){
			sct[i-2]=null;
		}	
	}
	
	public void makeMatrix(float[][] dMat){
		int n=dMat.length;
		mat=new float[n][n];
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				float c=dMat[i][j]*dMat[i][j];
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
