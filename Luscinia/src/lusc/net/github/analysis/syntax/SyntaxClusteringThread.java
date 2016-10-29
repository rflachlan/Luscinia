package lusc.net.github.analysis.syntax;
//
//  SyntaxClusteringThread.java
//  Luscinia
//
//  Created by Robert Lachlan on 10/30/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

import java.util.*;

public class SyntaxClusteringThread extends Thread{

	int numReseeds=50;
	int kv=2;
	int n;
	
	int kv1=0;

	int[] overallPrototypes;
		
	int[][] table1;
	int[] table2;
	
	
	double[] G;
	double logn;
	double invn;
		
	double redundancy=0;
	double overallRedundancy=0;
	
	double[][] matd;
	int[] assignments;
	
	Random random;
	
	SWMLEntropyEstimate swml;
	MarkovChain mkc;
	
	
	int[][]individuals;
	int[][]songs;
	
	double[] mktrajectory, swtrajectory;
	
	int mode=2;
	
	boolean rolling=false;
	boolean byIndividual=true;
	int window=0;
	
	public SyntaxClusteringThread(int kv, double[][] mat, int[][] individuals, int[][] songs, int mode, boolean rolling, int window, boolean byIndividual){
		this.kv=kv;
		this.mode=mode;
		this.individuals=individuals;
		this.songs=songs;
		this.rolling=rolling;
		this.window=window;
		this.byIndividual=byIndividual;
		
		if (!byIndividual){
			individuals=calculateDummyIndividuals(songs.length);
		}
		
		kv1=kv+1;
		
		n=mat.length;
		
		this.matd=new double[n][n];
		for (int i=0; i<n; i++){
			System.arraycopy(mat[i], 0, matd[i], 0, mat[i].length);
		}
		
		table1=new int[kv1][kv1];
		table2=new int[kv1];
		calculateG(n);
		random=new Random(System.currentTimeMillis()+kv*1000);
	}
	

	public synchronized void run(){
		
		assignments=new int[n];
		for (int i=0; i<numReseeds; i++){
			
			int[] tempAssignments=iterateAlgorithmSyntax();
			
			if (redundancy>overallRedundancy){
				overallRedundancy=redundancy;
				System.arraycopy(tempAssignments, 0, assignments, 0, assignments.length);
			}
		}
		
		System.out.println("Finished clustering: "+kv+" "+overallRedundancy);
		
		int[][] assignBySong=new int[songs.length][];
		for (int i=0; i<songs.length; i++){
			assignBySong[i]=new int[songs[i].length];
			for (int j=0; j<assignBySong[i].length; j++){
				assignBySong[i][j]=assignments[songs[i][j]];
				//System.out.print(assignments[songs[i][j]]+" ");
			}
			//System.out.println();
		}
		
		if (mode>0){
			try{
				System.out.println("Starting SWML: "+kv);
				swml=new SWMLEntropyEstimate(individuals, assignBySong, kv, n, true);
				System.out.println("Finished SWML: "+kv);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		if (mode!=1){
			try{
				System.out.println("Starting MKC: "+kv);
				mkc=new MarkovChain(individuals, assignBySong, kv, n, true);
				System.out.println("Finished MKC: "+kv);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		
		
		if (rolling){
			
			mktrajectory=new double[songs.length-window];
			swtrajectory=new double[songs.length-window];
			for (int i=window; i<songs.length; i++){	
				int[][] subset=calculateSubset(assignBySong, i-window, i);
				int n2=calculateN(subset);
				int[][] ind2=calculateDummyIndividuals(subset.length);
				if (mode>0){
					SWMLEntropyEstimate swmlx=new SWMLEntropyEstimate(ind2, subset, kv, n2, false);
					swtrajectory[i-window]=swmlx.rho;
					System.out.println("SWML: "+(i-window)+" "+swtrajectory[i-window]);
				}
				if (mode!=1){
					MarkovChain mkcx=new MarkovChain(ind2, subset, kv, n2, false);
					mktrajectory[i-window]=mkcx.rho;
					System.out.println("MK: "+(i-window)+" "+mktrajectory[i-window]+" "+mkcx.zero+" "+mkcx.ent+" "+kv+" "+n2+" "+subset.length);
				}
				
			}
		}
		
		
	}
	
	public int[][] calculateDummyIndividuals(int n){
		int[][] x=new int[n][1];
		for (int i=0; i<n; i++){
			x[i][0]=i;
		}
		return x;
	}
	
	public int calculateN(int[][] x){
		int n=0;
		for (int i=0; i<x.length; i++){
			n+=x[i].length;
		}
		return n;
	}
	
	public int[][] calculateSubset(int[][] x, int a, int b){
		int n=b-a;
		int[][] out=new int[n][];
		
		for (int i=a; i<b; i++){
			out[i-a]=new int[x[i].length];
			for (int j=0; j<x[i].length; j++){
				out[i-a][j]=x[i][j];
			}
		}
		return out;	
	}
	
	public int[] initiatePrototypes(){
		
		int[] prototypes=new int[kv];
		for (int i=0; i<kv; i++){
			
			prototypes[i]=random.nextInt(n);
			
			boolean found=false;
			for (int j=0; j<i; j++){
				if (prototypes[j]==prototypes[i]){
					found=true;
					j=i;
				}
			}
			while (found){
				prototypes[i]=random.nextInt(n);
				found=false;
				for (int j=0; j<i; j++){
					if (prototypes[j]==prototypes[i]){
						found=true;
						j=i;
					}
				}
			}
		}
		Arrays.sort(prototypes);
		return prototypes;
	}
	
	public boolean getAssignments(int[] prototypes, int[] assignments, int[] countAssignments, double threshold){
		double bestScore=0;
		int a;
		double b;
		for (int i=0; i<kv; i++){
			countAssignments[i]=0;
		}
		
		for (int i=0; i<n; i++){
			bestScore=1000000;
			a=0;
			for (int j=0; j<kv; j++){
				b=matd[prototypes[j]][i];
				if (b<bestScore){
					bestScore=b;
					a=j;
				}
			}
			assignments[i]=a;
			countAssignments[a]++;
		}
		boolean passed=true;
		for (int i=0; i<kv; i++){
			if (countAssignments[i]<threshold){
				passed=false;
				i=kv;
			}
		}
		return passed;
	}
	
	public int[] iterateAlgorithmSyntax(){		
		
		
		int[] tempAssignments=new int[n];
		boolean isMedoid[]=new boolean[n];
		int[] countAssignments=new int[kv];
		
		double fac=3;
		double clusterSizeThreshold=n/(kv*fac);
		
		int[] prototypes=initiatePrototypes();
		boolean pass=getAssignments(prototypes, tempAssignments, countAssignments, clusterSizeThreshold);
		while(!pass){
			prototypes=initiatePrototypes();
			pass=getAssignments(prototypes, tempAssignments, countAssignments, clusterSizeThreshold);
		}
		
		
		for (int i=0; i<n; i++){
			isMedoid[i]=false;
			for (int j=0; j<kv; j++){
				if (prototypes[j]==i){
					isMedoid[i]=true;
				}
			}
		}		
		
		redundancy=calculateFirstOrderEntropy(tempAssignments);
		double oldRedundancy=0;
		
		while (redundancy!=oldRedundancy){
			oldRedundancy=redundancy;
			for (int j=0; j<kv; j++){
				for (int k=0; k<n; k++){
					if (!isMedoid[k]){
						int s=prototypes[j];
						prototypes[j]=k;
						
						pass=getAssignments(prototypes, tempAssignments, countAssignments, clusterSizeThreshold);
						
						if (pass){
							double testRedundancy=0;
							testRedundancy=calculateFirstOrderEntropy(tempAssignments);
							
							if (testRedundancy>redundancy){
								redundancy=testRedundancy;
								isMedoid[s]=false;
								isMedoid[k]=true;
							}
							else{
								prototypes[j]=s;
							}
						}
						else{
							prototypes[j]=s;
						}
					}
				}
			}
		}
		
		int[] bestAssignments=new int[n];
		pass=getAssignments(prototypes, bestAssignments, countAssignments, clusterSizeThreshold);

		return bestAssignments;
	}
	
	public double calculateFirstOrderEntropy(int[] tempLabels){
					
		for (int i=0; i<kv1; i++){
			table2[i]=0;
			for (int j=0; j<kv1; j++){
				table1[i][j]=0;
			}
		}
		
		int p=0;
		int q=0;
		int r=0;
		for (int i=0; i<songs.length; i++){
			if (songs[i].length>0){
				p=tempLabels[songs[i][0]];
				table1[kv][p]++;
				table2[kv]++;
				table2[p]++;
				r=songs[i].length;
				for (int j=1; j<r; j++){
					q=tempLabels[songs[i][j]];
					table1[p][q]++;
					table2[q]++;
					p=q;
				}
				table1[p][kv]++;
			}
		}
	
		double mean=calculateFromTable();	
		
		double zeroOrder=calculateZeroOrderEntropy();
		mean=mean-zeroOrder;
		double rho=(zeroOrder-mean)/zeroOrder;
		//double rho=zeroOrder-mean;
		return rho;
	}
	
	public double calculateFromTable(){
		double score=0;
		for (int i=0; i<kv1; i++){
			for (int j=0; j<kv1; j++){
				if (table1[i][j]>0){
					score+=G[table1[i][j]];
				}
			}
		}
		score=logn-(invn*score);
		return score;
	}
	
	public double calculateZeroOrderEntropy(){
		
		double score=0;
		for (int i=0; i<kv1; i++){
			if (table2[i]>0){
				score+=G[table2[i]];
			}
		}
		score=logn-(invn*score);
		return score;
	}
	
	void calculateG(int m){
		G=new double[m];
		
		G[1]=-0.5772156649-Math.log(2);
		G[2]=2+G[1];
		for (int i=3; i<m; i++){
			if (i % 2 ==0){
				G[i]=G[i-2]+(2/(i-1.0));
			}
			else{
				G[i]=G[i-1];
			}
		}
		
		for (int i=0; i<m; i++){
			G[i]*=i;
		}
		
		
		int ns=songs.length;
		
		int gt=n+ns;
		
		logn=Math.log(gt);
		invn=1/(gt+0.0);
		
	}
}	
