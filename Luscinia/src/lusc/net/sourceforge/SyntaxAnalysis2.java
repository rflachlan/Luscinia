package lusc.net.sourceforge;
//
//  SyntaxAnalysis2.java
//  Luscinia
//
//  Created by Robert Lachlan on 8/19/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

import java.util.*;

public class SyntaxAnalysis2 {

	float[][] distanceMatrix, transitionMatrix;
	int n, nSongs, nTransitions;
	int[] songLengths;
	int resamples=1000;
	int[][] songPosition;
	double[] position;
	
	double[] perSyllableResults;
	boolean[] isFinal;
	
	double[] thresholds={0.01, 0.02, 0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.5};
		
	BasicStatistics bs=new BasicStatistics();
	Random random=new Random(System.currentTimeMillis());
	
	double[] resultArray;
	
	String[] resultString;
	
	public SyntaxAnalysis2(float[][] distanceMatrix, int[][] lookUps){
		
		this.distanceMatrix=distanceMatrix;
		
		setUp(lookUps);
		Analysis3();
	}
	
	public SyntaxAnalysis2(int[][] assignments, int[][] lookUps){
		setUp(lookUps);
		resultArray=new double[assignments.length];
		for (int i=0; i<assignments.length; i++){
			resultArray[i]=AbsoluteSyntaxTest4(assignments[i]);
		}
	}
	
	public void Analysis1(){
	
		int[] order=new int[n];
		getStraightOrder(order);
		
		float[][] realResults=perSongComparison(order);
		
		//float[] results=perSongComparisonAllPermutations();
		
		
		float[] beatsReal=new float[n];
		
		for (int i=0; i<resamples; i++){
			reorderWithinSong(order);
			float[][] simResults=perSongComparison(order);
			
			for (int j=0; j<n; j++){
				if (!isFinal[j]){
					float c=0;
					for (int k=0; k<nSongs; k++){
						if ((simResults[j][k]>0)&&(simResults[j][k]<realResults[j][k])){
							c++;
						}
					}
					beatsReal[j]+=c/(nSongs-1.0f);
				}
			}
		}
		
		perSyllableResults=new double[beatsReal.length];
		double[] results=new double[nTransitions];
		int nonzero=0;
		for (int i=0; i<n; i++){
			if (!isFinal[i]){
				perSyllableResults[i]=beatsReal[i]/(resamples+0.0);
				results[nonzero]=perSyllableResults[i];
				nonzero++;
			}
		}
		 
		
				
		double meanScore=bs.calculateMean(results);
		double sdScore=bs.calculateSD(results, true);
		double upper5pc=bs.calculatePercentile(results, 5, true);
		double lower5pc=bs.calculatePercentile(results, 5, false);
		
		resultString=new String[1];
		resultString[0]="SYNTAX ANAL: "+meanScore+" "+sdScore+" "+upper5pc+" "+lower5pc;
		
	}
	
	public void Analysis2(){
		int[] order=new int[n];
		getStraightOrder(order);
		setUpTransitionMatrix();
		calculateTransitionMatrix(order);
		double sd=bs.calculateSD(transitionMatrix, true);
		double mean=bs.calculateMean(transitionMatrix);
		int[] realResults=calculateTransitionProportions(sd, mean);
		
		
		
		
		double[] aggregateSimResults=new double[100];
		
		double[] countDiffs=new double[100];
		
		for (int i=0; i<resamples; i++){
			reorderWithinSong(order);
			calculateTransitionMatrix(order);
			int[] simResults=calculateTransitionProportions(sd, mean);
			for (int j=0; j<100; j++){
				aggregateSimResults[j]+=simResults[j];
				if (simResults[j]>=realResults[j]){
					countDiffs[j]++;
				}
			}
		}
		
		for (int i=0; i<100; i++){
			System.out.println(realResults[i]+" "+aggregateSimResults[i]+" "+countDiffs[i]);
			aggregateSimResults[i]/=resamples+0.0;
			countDiffs[i]/=resamples+0.0;
		}
		
		double p=1/(nTransitions*(nTransitions-1)*0.5);
		
		resultString=new String[3];
		resultString[0]="Real Results: "+realResults[9]*p+" "+realResults[19]*p+" "+realResults[24]*p+" "+realResults[29]*p+" "+realResults[34]*p+realResults[39]*p+" "+realResults[44]*p+" "+realResults[49]*p+" "+realResults[54]*p+" "+realResults[59]*p;
		resultString[1]="Agg Results: "+aggregateSimResults[9]*p+" "+aggregateSimResults[19]*p+" "+aggregateSimResults[24]*p+" "+aggregateSimResults[29]*p+" "+aggregateSimResults[34]*p+aggregateSimResults[39]*p+" "+aggregateSimResults[44]*p+" "+aggregateSimResults[49]*p+" "+aggregateSimResults[54]*p+" "+aggregateSimResults[59]*p;
		resultString[2]="Count Results: "+countDiffs[9]+" "+countDiffs[19]+" "+countDiffs[24]+" "+countDiffs[29]+" "+countDiffs[34]+countDiffs[39]+" "+countDiffs[44]+" "+countDiffs[49]+" "+countDiffs[54]+" "+countDiffs[59];
	}
	
	public void Analysis3(){int[] order=new int[n];
		getStraightOrder(order);
		setUpTransitionMatrix();
		calculateTransitionMatrix(order);
		
		
		double[] absyn=AbsoluteSyntaxTest3();
		
		float[] p=getProportionsLessThan(thresholds, transitionMatrix);
		
		int[][] aggregateResults=new int[thresholds.length][resamples];
		
		
		for (int i=0; i<resamples; i++){
			reorderOverWholeSet(order);
			calculateTransitionMatrix(order);
			
			int[] simResults=countLessThan(p);
			
			for (int j=0; j<thresholds.length; j++){
				aggregateResults[j][i]=simResults[j];
			}
		}
		
		double[] meanR=new double[thresholds.length];
		int[] ciRU=new int[thresholds.length];
		int[] ciRL=new int[thresholds.length];
		
		resultString=new String[thresholds.length+1];
		double q=1/(nTransitions*(nTransitions-1)*0.5);
		for (int i=0; i<thresholds.length; i++){
			
			meanR[i]=bs.calculateMean(aggregateResults[i]);
			ciRU[i]=bs.calculatePercentile(aggregateResults[i], 0.05, true);
			ciRL[i]=bs.calculatePercentile(aggregateResults[i], 0.05, false);
			
			resultString[i]="Threshold: "+thresholds[i]+" "+(meanR[i]*q)+" "+(ciRU[i]*q)+" "+(ciRL[i]*q);
		}
		resultString[resultString.length-1]="Absolute syntax: "+absyn[0]+" "+absyn[1]+" "+absyn[2]+" "+absyn[3]+" "+absyn[4]+" "+absyn[5]+" "+absyn[6]+" "+absyn[7]+" "+absyn[8]+" "+absyn[9];
			
	}
	
	public double[] AbsoluteSyntaxTest(){
		
		
		double th=0.1;
		double mean=bs.calculateMean(distanceMatrix);
		double sd=bs.calculateSD(distanceMatrix, true);
		
		double[] bound={0.01, 0.02, 0.03, 0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.5};
		float[] thresholds=getProportionsLessThan(bound, distanceMatrix);
		
		double a=0;
		double b=0;
		
		int[] countProp=new int[bound.length];
		
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				
				if(Math.abs(position[i]-position[j])<=th){
					a++;
					if (distanceMatrix[i][j]<thresholds[0]){
						b+=thresholds[0];
					}
					else if (distanceMatrix[i][j]>thresholds[1]){
						b+=thresholds[1];
					}
					else{
						b+=distanceMatrix[i][j];
					}
					
					for (int k=0; k<bound.length; k++){
						if (distanceMatrix[i][j]<thresholds[k]){
							countProp[k]++;
						}
					}
				}
			}
		}
		
		b=((b/a)-mean)/sd;
		double[] results=new double[bound.length];
		for (int i=0; i<bound.length; i++){
			results[i]=countProp[i]/(a+0.0);
		}
		return (results);
	}
	
	public double[] AbsoluteSyntaxTest2(){
		
		double th=0.1;
		
		int[][] ranks= calculateRankMatrix(distanceMatrix);
		
		double[] bound={0.01, 0.02, 0.03, 0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.5};

		int[] boundInt=new int[bound.length];
		
		for (int i=0; i<bound.length; i++){
			boundInt[i]=(int)Math.round(bound[i]*(n-1));
		}
		
		double[][] r=new double[bound.length][n];
		
		for (int i=0; i<n; i++){
			double a=0;
			for (int j=0; j<n; j++){
				if (i!=j){
					int ii=i;
					int jj=j;
					if (j>i){
						ii=j;
						jj=i;
					}
					if(Math.abs(position[i]-position[j])<=th){
						a++;
						for (int k=0; k<bound.length; k++){
							if (ranks[i][j]<boundInt[k]){
								r[k][i]++;
							}
						}
					}
				}
			}
			for (int j=0; j<bound.length; j++){
				r[j][i]/=a;
			}
		}
		
		double[] results=new double[bound.length];
		
		for (int i=0; i<bound.length; i++){
			results[i]=bs.calculateMedian(r[i]);
		}
		
		/*
		for (int i=0; i<n; i++){
			for (int j=0; j<bound.length; j++){
				results[j]+=r[j][i];
			}
		}
		for (int i=0; i<bound.length; i++){
			results[i]/=n+0.0;
		}
		*/
		
		return (results);
	}
	
	public double[] AbsoluteSyntaxTest3(){
				
		int[][] ranks= calculateRankMatrix(distanceMatrix);
		
		double[] bound={0.01, 0.02, 0.03, 0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.5};
		
		int[] boundInt=new int[bound.length];
		
		for (int i=0; i<bound.length; i++){
			boundInt[i]=(int)Math.round(bound[i]*(n-1));
		}
		
		double[] r=new double[bound.length];
		double[] s=new double[n];
		double[][]c=new double[bound.length][n];
		
		
		for (int i=0; i<n; i++){
			for (int j=0; j<n; j++){
				if (i!=j){
					double q=Math.abs(position[i]-position[j]);
					
					for (int k=0; k<bound.length; k++){
						if (ranks[i][j]<=boundInt[k]){
							r[k]+=q;
							c[k][i]+=q;
						}
					}
					s[i]+=q;
				}
			}
			s[i]/=n-1.0;
			for (int j=0; j<bound.length; j++){
				c[j][i]/=s[i]*(boundInt[j]+1.0);
			}
		}

		
		for (int j=0; j<bound.length; j++){
			//r[j]/=(boundInt[j]+1.0)*n;
			r[j]=bs.calculateMedian(c[j]);
		}
		
		return (r);
	}
	
	public double AbsoluteSyntaxTest4(int[] categories){
		
		

		double[] r=new double[n];
		
		for (int i=0; i<n; i++){
			double s=0;
			double t=0;
			for (int j=0; j<n; j++){
				if (i!=j){
					double q=Math.abs(position[i]-position[j]);
					if(categories[i]==categories[j]){
						r[i]+=q;
						s++;
					}
					t+=q;
				}
			}
			t/=n-1.0;
			r[i]/=s*t;
		}
		
		double q=bs.calculateMedian(r);
			
		return (q);
	}
	
	
	
	public int[][] calculateRankMatrix(float[][] d){
		
		int n=d.length;
		
		int[][] results=new int[n][n];		
		float[] holder=new float[n-1];
		
		for (int i=0; i<n; i++){
			System.out.println();
			int a=0;
			for (int j=0; j<i; j++){
				holder[a]=d[i][j];
				a++;
			}
			for (int j=i+1; j<n; j++){
				holder[a]=d[j][i];
				a++;
			}
			Arrays.sort(holder);
			a=0;
			for (int j=0; j<i; j++){
				
				for (int k=0; k<n-1; k++){
					if (holder[k]==d[i][j]){
						results[i][a]=k;
						System.out.print(k+" ");
						k=n-1;
					}
				}
				a++;
			}
			results[i][a]=-1;
			System.out.print("-1 ");
			a++;
			for (int j=i+1; j<n; j++){
				for (int k=0; k<n-1; k++){
					if (holder[k]==d[j][i]){
						results[i][a]=k;
						System.out.print(k+" ");
						k=n-1;
					}
				}
				a++;
			}

		}
		return results;
	}
		
		
			
		
		
		
	
	public void setUp(int[][] lookUps){
		n=lookUps.length;
		nTransitions=0;
		nSongs=1;
		isFinal=new boolean[n];
		isFinal[n-1]=true;
		
		for (int i=0; i<n-1; i++){
			if (lookUps[i][0]!=lookUps[i+1][0]){
				nSongs++;
				isFinal[i]=true;
			}
			else{
				nTransitions++;
				isFinal[i]=false;
			}
		}
		
		position=new double[n];
		for (int i=0; i<n; i++){
			if (isFinal[i]){
				position[i]=1;
			}
			else{
				int j=i+1;
				while(!isFinal[j]){j++;}
				position[i]=lookUps[i][1]/(lookUps[j][1]+0.0);
			}
			
			System.out.println(lookUps[i][0]+" "+lookUps[i][1]+" "+isFinal[i]+" "+position[i]);
			
		}
		
		songLengths=new int[nSongs];
		int a=0;
		int b=0;
		for (int i=0; i<n; i++){
			if ((i==n-1)||(lookUps[i][0]!=lookUps[i+1][0])){
				songLengths[a]=b+1;
				a++;
				b=0;
			}
			else{
				b++;
			}
		}
		
		songPosition=new int[nSongs][];
		for (int i=0; i<nSongs; i++){
			songPosition[i]=new int[songLengths[i]];
		}
		
		a=0;
		b=0;
		for (int i=0; i<n; i++){
			songPosition[a][b]=i;
			if ((i==n-1)||(lookUps[i][0]!=lookUps[i+1][0])){
				a++;
				b=0;
			}
			else{
				b++;
			}
		}
		
		
	}
	
	public float lookUpScore(int a, int b){
		float r=0;
		if (a<b){
			r=distanceMatrix[b][a];
		}
		else{
			r=distanceMatrix[a][b];
		}
		return r;
	}
	
	public float lookUpScore(int a, int b, float[][] data){
		float r=0;
		if (a<b){
			r=data[b][a];
		}
		else{
			r=data[a][b];
		}
		return r;
	}
	
	public void getStraightOrder(int[] order){
		
		for (int i=0; i<order.length; i++){
			order[i]=i;
		}
	}
	
	public void reorderWithinSong(int[] order){
		for (int i=0; i<n; i++){
			order[i]=i;
		}
		
		int a=0;
		for (int i=0; i<nSongs; i++){
			int m=songLengths[i];
			for (int j=0; j<m; j++){
				int c=random.nextInt(m);
				int d=a+j;
				c+=d;
				int b=order[d];
				order[d]=order[c];
				order[c]=b;
				m--;
			}
			a+=songLengths[i];
		}
	}
	
	public void reorderOverWholeSet(int[] order){
		for (int i=0; i<n; i++){
			order[i]=i;
		}
		
		int a=n;
		for (int i=0; i<n; i++){
			int c=random.nextInt(a);
			c+=i;
			int b=order[c];
			order[c]=order[i];
			order[i]=b;
			a--;
		}
	}
	
	public float[][] calculateLocationDistanceMatrix(){
		
		float[][] output=new float[n][];
		for (int i=0; i<n; i++){
			output[i]=new float[i+1];
		}
		
		for (int i=0; i<n; i++){
						
			for (int j=0; j<i; j++){
				double posscore=Math.abs(position[i]-position[j]);
				if (posscore<0.1){
					output[i][j]=1;
				}
				else{
					output[i][j]=0;
				}
			}
		}
		return output;
	}
		
	
	
	public float[][] perSongComparison(int[] order){
		
		float[][] results=new float[n][nSongs];
		
		for (int i=0; i<nSongs; i++){
			
			for (int a=0; a<songPosition[i].length-1; a++){
				
				int t=songPosition[i][a];
				int u=songPosition[i][a+1];
				
				
				for (int j=0; j<nSongs; j++){
					if (j!=i){
						
						float bestScore=1000000f;
						for (int b=0; b<songPosition[j].length-1; b++){
							
							float q=lookUpScore(t,order[songPosition[j][b]]);
							float r=lookUpScore(u,order[songPosition[j][b+1]]);
							q=q*r;
							if (q<bestScore){bestScore=q;}
						}
						
						results[t][j]=bestScore;
					}
				}
			}
		}		
		return results;
	}
	
	public float[] perSongComparisonAllPermutations(){
		float[] results=new float[nTransitions];
		
		
		float q, r;
		
		int y=0;
		
		for (int i=0; i<nSongs; i++){
			
			for (int a=0; a<songPosition[i].length-1; a++){
				
				int t=songPosition[i][a];
				int u=songPosition[i][a+1];
				
				for (int j=0; j<nSongs; j++){
					if (j!=i){
						
						float bestScore=1000000f;
						for (int b=0; b<songPosition[j].length; b++){
							q=lookUpScore(t, songPosition[j][b]);
							for (int c=0; c<songPosition[j].length; c++){
								if (b!=c){
									r=lookUpScore(u, songPosition[j][c]);
									r=r*q;
									if (r<bestScore){
										bestScore=r;
									}
								}
							}
						}
						
						float actualScore=100000f;
						for (int b=0; b<songPosition[j].length-1; b++){
							q=lookUpScore(t, songPosition[j][b]);
							r=lookUpScore(u, songPosition[j][b+1]);
							r=r*q;
							if (r<actualScore){
								actualScore=r;
							}
						}
						
						if (actualScore==bestScore){
							results[y]++;
						}	
					}
				}
				results[y]/=nSongs-1.0f;
				y++;
			}
		}		
		return results;
	}
	
	public void setUpTransitionMatrix(){
		
		transitionMatrix=new float[nTransitions][];
		for (int i=0; i<nTransitions; i++){
			transitionMatrix[i]=new float[i+1];
		}
	}
	
	
	public void calculateTransitionMatrix(int[] order){
		
		
		int a=0;
		
		for (int i=0; i<n; i++){
			if (!isFinal[i]){
				int c1=order[i];
				int c2=order[i+1];
				int b=0;
				for (int j=0; j<i; j++){
					if (!isFinal[j]){
						int d1=order[j];
						int d2=order[j+1];
						float p1=lookUpScore(c1, d1);
						float p2=lookUpScore(c2, d2);
						//transitionMatrix[a][b]=(float)Math.sqrt(p1*p2);
						transitionMatrix[a][b]=p1+p2;
						
						b++;
					}
				}
				a++;
			}
		}

	}
	
	public float[] getProportionsLessThan(double[] thresholds, float[][] data){
		int n=data.length;
		int m=n*(n-1)/2;
		float[] inline=new float[m];
		
		int a=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				inline[a]=data[i][j];
				a++;
			}
		}
		
		Arrays.sort(inline);
		
		float[] results=new float[thresholds.length];
		
		for (int i=0; i<thresholds.length; i++){
			int p=(int)Math.round(thresholds[i]*m);
			results[i]=inline[p];
		}
		
		return results;
		
	}
	
	public int[] countLessThan(float[] thresholds){
		
		int[] results=new int[thresholds.length];
		
		for (int i=0; i<thresholds.length; i++){
			for (int j=0; j<nTransitions; j++){
				for (int k=0; k<j; k++){
					if (transitionMatrix[j][k]<thresholds[i]){
						results[i]++;
					}
				}
			}
		}
		
		return results;
	}
		
	
	
	public int[] calculateTransitionProportions(double sd, double mean){
		
		int[] results=new int[100];
		
		for (int i=0; i<100; i++){
			
			double comp=sd*(i+1)/50.0;
			comp=mean-comp;
			
			for (int j=1; j<nTransitions; j++){
				for (int k=0; k<j; k++){
					if (transitionMatrix[j][k]<=comp){results[i]++;}
				}
			}
		}
		return results;
	}
		
		
		
	
	
}
