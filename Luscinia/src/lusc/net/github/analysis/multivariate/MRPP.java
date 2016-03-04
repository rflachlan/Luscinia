package lusc.net.github.analysis.multivariate;
//
//  MRPP.java
//  Luscinia
//
//  Created by Robert Lachlan on 2/2/07.
//  Copyright 2007 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.


import java.util.*;

import lusc.net.github.analysis.ComparisonResults;
import lusc.net.github.ui.MRPPOptions;


public class MRPP {
	int n;
	Random random=new Random(System.currentTimeMillis());
	
	double[][] dat;
	int[] marker;
	
	int weightingMethod=0;
	int nresamp=10000;
	
	boolean pairwise=false;
	
	double expectedDelta, empiricalDelta, pvalue, avalue;
	double[][] pairwisePValue, pairwiseAValue, pairwiseExpectedDelta, pairwiseEmpiricalDelta;
	int dataType=0;
	
	String[] levelNames;
	
	public MRPP(ComparisonResults cr, MRPPOptions mo, int type){
		this.dataType=type;
		
		double[][] d=cr.getDiss();
		
		int[] partition=null;
		int s=mo.levelSel;
		if (s==0){
			partition=cr.getSpeciesListArray();
			levelNames=cr.getSpeciesNames();
		}
		else if (s==1){
			partition=cr.getPopulationListArray();
			levelNames=cr.getPopulationNames();
		}
		else if (s==2){
			partition=cr.getLookUpIndividuals();
			levelNames=cr.getIndividualNames();
		}
		weightingMethod=mo.weightingSel;
		nresamp=mo.numRepeats;
		pairwise=mo.pairwise;
		double[] results=calculateMRPP(d, partition);
		pvalue=results[0];
		avalue=results[1];
		empiricalDelta=results[2];
		expectedDelta=results[3];
		if (pairwise){
			calculateMRPPPairwise(d, partition);
		}
	}
	
	public MRPP(double[][] d, int[] partition){
		calculateMRPP(d, partition);
	}
	
	public int getType(){
		return dataType;
	}
	
	public double getEmpiricalDelta(){
		return empiricalDelta;
	}
	
	public double getExpectedDelta(){
		return expectedDelta;
	}
	
	public double getPValue(){
		return pvalue;
	}
	
	public double getAValue(){
		return avalue;
	}
	
	public boolean getPairwise(){
		return pairwise;
	}
	
	public double[][] getPairwisePValue(){
		return pairwisePValue;
	}
	
	public double[][] getPairwiseAValue(){
		return pairwiseAValue;
	}
	
	public double[][] getPairwiseExpectedDelta(){
		return pairwiseExpectedDelta;
	}
	
	public double[][] getPairwiseEmpiricalDelta(){
		return pairwiseEmpiricalDelta;
	}
	
	public String[] getLevelNames(){
		return levelNames;
	}
	
	public void calculateMRPPPairwise(double[][]d, int[] partition){
		int[] levels=calculateLevels(partition);
		int n=levels.length;
		pairwisePValue=new double[n][n];
		pairwiseAValue=new double[n][n];
		pairwiseEmpiricalDelta=new double[n][n];
		pairwiseExpectedDelta=new double[n][n];
		for (int i=0; i<n; i++){
			for (int j=0; j<n; j++){	
				int[] subpart=getSubsetPartition(partition, levels[i], levels[j]);
				System.out.println("LEVELS: "+levels[i]+" "+levels[j]);
				double[][] subscore=getSubsetScores(partition, d, levels[i], levels[j]);
				double[] results=calculateMRPP(subscore, subpart);
				pairwisePValue[i][j]=results[0];
				pairwiseAValue[i][j]=results[1];
				pairwiseEmpiricalDelta[i][j]=results[2];
				pairwiseExpectedDelta[i][j]=results[3];
			}
		}	
	}
	
	public int[] getSubsetPartition(int[] levels, int x, int y){
		int n=levels.length;
		int m=0;
		for (int i=0; i<n; i++){
			if (levels[i]==x){
				m++;
			}
			else if (levels[i]==y){
				m++;
			}
		}
		
		int[] out=new int[m];
		m=0;
		for (int i=0; i<n; i++){
			if (levels[i]==x){
				out[m]=x;
				m++;
			}
			else if (levels[i]==y){
				out[m]=y;
				m++;
			}
		}
		
		//for (int i=0; i<out.length; i++){
			//System.out.print(out[i]+" ");
		//}
		//System.out.println();
		for (int i=0; i<out.length; i++){
			if (out[i]==x){
				out[i]=0;
			}
			else{
				out[i]=1;
			}
		}
		return out;
	}
	
	public double[][] getSubsetScores(int[] levels, double[][] scores, int x, int y){
		int n=levels.length;
		int m=0;
		for (int i=0; i<n; i++){
			if (levels[i]==x){
				m++;
			}
			else if (levels[i]==y){
				m++;
			}
		}
		
		double[][] out=new double[m][];		
		m=0;
		for (int i=0; i<n; i++){
			if ((levels[i]==x)||(levels[i]==y)){
				out[m]=new double[i+1];
				int m2=0;
				for (int j=0; j<i; j++){
					if ((levels[j]==x)||(levels[j]==y)){
						out[m][m2]=scores[i][j];
						m2++;
					}
				}
				m++;
			}
		}
		return out;	
	}
		
	
	public int[] calculateLevels(int[] input){
		int x=0;
		int n=input.length;
		int[] y=new int[n];
		
		for (int i=0; i<n; i++){
			boolean found=false;
			for (int j=0; j<x; j++){
				if (input[i]==y[j]){
					found=true;
					j=x;
				}
			}
			if (!found){
				y[x]=input[i];
				x++;
			}
		}
		
		int[] z=new int[x];
		System.arraycopy(y, 0, z, 0, x);
		
		return z;
	}
	
	
	public double[] calculateMRPP(double[][] d, int[] partition){
		n=d.length;
		double numPart=0;
		int maxPart=0;
		double[] results=new double[4];
		for (int i=0; i<n; i++){
			//System.out.println(partition[i]);
			if (partition[i]>maxPart){maxPart=partition[i];}
		}
		if (maxPart>0){
			maxPart++;
			int[] countPart=new int[maxPart];
			int t=0;
			for (int i=0; i<n; i++){
				if (partition[i]>=0){
					countPart[partition[i]]++;
					t++;
				}
			}
		
			int[] partb=new int[t];
			int[] look=new int[t];
			t=0;
			for (int i=0; i<n; i++){
				if (partition[i]>=0){
					partb[t]=partition[i];
					look[t]=i;
					t++;
					numPart++;
				}
			}
			double t2=t;
			
			if (weightingMethod==1){
				t2-=numPart;
			}
			if (weightingMethod==2){
				t2=0;
				for (int i=0; i<maxPart; i++){
					if (countPart[i]>0){
						t2+=countPart[i]*(countPart[i]-1);
					}
				}	
			}
			
		
			dat=new double[t][];
			for (int i=0; i<t; i++){
				dat[i]=new double[i+1];
				for (int j=0; j<i; j++){
					dat[i][j]=d[look[i]][look[j]];
				}
			}
			n=dat.length;
		
			double ed=calculateDelta(partb, countPart, t2, maxPart);
		
			double[] resamples=new double[nresamp];
		
			int count=0;
			double av=0;
			for (int i=0; i<nresamp; i++){
				int[] markr=shufflePartition(partb);
				resamples[i]=calculateDelta(markr, countPart, t2, maxPart);
				if (resamples[i]<=ed){count++;}
				av+=resamples[i];
			}
			//expectedDelta=av/nresamp+0.0;
			//pvalue=(1.0+count)/(nresamp+0.0);
			//avalue=1-(empiricalDelta/expectedDelta);
			results[0]=(1.0+count)/(nresamp+0.0);
			results[2]=ed;
			results[3]=av/nresamp+0.0;
			results[1]=1-(ed/results[3]);
			//System.out.println("Empirical Delta: "+empiricalDelta+" Expected Delta: "+expectedDelta+" p: "+pvalue+" A: "+avalue);
		}
		return results;
	}
	
	
	public MRPP(double[][] d, int[] partition, int[] group){
		n=d.length;
		this.dat=d;
		int maxPart=0;
		int maxGroup=0;
		for (int i=0; i<n; i++){
			//System.out.println(partition[i]);
			if (partition[i]>maxPart){maxPart=partition[i];}
			if (group[i]>maxGroup){maxGroup=group[i];}
		}
		if (maxPart>0){
			maxPart++;
			maxGroup++;
		
			int[][] countPart=new int[maxPart][maxGroup];
			int[] countGroup=new int[maxGroup];
			double t=0;
			for (int i=0; i<n; i++){
				countPart[partition[i]][group[i]]++;
				countGroup[group[i]]++;
				t++;
			}
		
			int[][] groupID=new int[maxGroup][];
			for (int i=0; i<maxGroup; i++){
				groupID[i]=new int[countGroup[i]];
				int a=0;
				for (int j=0; j<n; j++){
					if (group[j]==i){
						groupID[i][a]=j;
						a++;
					}
				}
			}
		
			empiricalDelta=calculateDelta(partition, group, countPart, t, maxPart, maxGroup);
		
			double[] resamples=new double[nresamp];
		
			int count=0;
			double av=0;
			for (int i=0; i<nresamp; i++){
				int[] markr=shufflePartition(partition, groupID);
				resamples[i]=calculateDelta(markr, group, countPart, t, maxPart, maxGroup);
				if (resamples[i]<=empiricalDelta){count++;}
				av+=resamples[i];
			}
			expectedDelta=av/nresamp+0.0;
			pvalue=(1.0+count)/(nresamp+0.0);
			avalue=1-(empiricalDelta/expectedDelta);
		
			System.out.println("Empirical Delta: "+empiricalDelta+" Expected Delta: "+expectedDelta+" p: "+pvalue+" A: "+avalue);
		}
	}
	
	
	public double calculateDelta(int[] partition, int[] countPart, double t, int maxPart){
		
		double sc[]=new double[maxPart];
		double c[]=new double[maxPart];
		
		for (int i=0; i<n; i++){
			int a=partition[i];
			for (int j=0; j<i; j++){
				if (a==partition[j]){
					sc[a]+=dat[i][j];
					c[a]++;
				}
			}
		}
		
		double r=0;
		for (int i=0; i<maxPart; i++){
			if (c[i]>0){
				double s=sc[i]/c[i];
				if(weightingMethod==0){
					r+=s*(countPart[i]/t);
				}
				if (weightingMethod==1){
					r+=s*(countPart[i]-1)/t;
				}
				if (weightingMethod==2){
					r+=s*(countPart[i])*(countPart[i]-1)/t;
				}
			}
		}
		return r;
	}
	
	public double calculateDelta(int[] partition, int[] group, int[][] count, double t, int maxPart, int maxGroup){
		
		double sc[][]=new double[maxPart][maxGroup];
		double c[][]=new double[maxPart][maxGroup];
		
		for (int i=0; i<n; i++){
			int a=partition[i];
			int b=group[i];
			for (int j=0; j<i; j++){
				if ((a==partition[j])&&(b==group[j])){
					sc[a][b]+=dat[i][j];
					c[a][b]++;
				}
			}
		}
		
		double r=0;
		for (int i=0; i<maxPart; i++){
			for (int j=0; j<maxGroup; j++){
				if (c[i][j]>0){
					double s=sc[i][j]/c[i][j];
					if(weightingMethod==0){
						r+=s*(count[i][j]/t);
					}
				}
			}
		}
		return r;
	}
	
	public int[] shufflePartition(int[] partition){
		int p=partition.length;
		int[] res=new int[p];
		System.arraycopy(partition, 0, res, 0, p);
		for (int i=0; i<p; i++){
			int n1=i+random.nextInt(p-i);
			int n2=res[i];
			res[i]=res[n1];
			res[n1]=n2;
			
		}
		return(res);
	}
	
	public int[] shufflePartition(int[] partition, int[][] groupIds){
		
		int p=partition.length;
		int[] res=new int[p];
		
		for (int i=0; i<groupIds.length; i++){
			int[] pb=shufflePartition(groupIds[i]);
			for (int j=0; j<pb.length; j++){
				res[groupIds[i][j]]=partition[pb[j]];
			}
			
			
		}
		return(res);
	}

}
