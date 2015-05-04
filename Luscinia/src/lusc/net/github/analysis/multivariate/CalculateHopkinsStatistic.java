package lusc.net.github.analysis.multivariate;
//
//  CalculateHopkinsStatistic.java
//  Luscinia
//
//  Created by Robert Lachlan on 7/27/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//
import java.util.*;

import lusc.net.github.analysis.BasicStatistics;

public class CalculateHopkinsStatistic {

	int type=0;
	
	Random random=new Random(System.currentTimeMillis());
	int resamples=100;
	//String resultString[]=null;
	String resultString=null;
	//double[][] results=null;
	double[] results=null;
	//int[] picks={1, 2, 5, 10, 20, 50, 100, 200, 500, 1000};
	int pick=1;
	double[][] silhouetteMax=null;
	
	int distribution=0;
	
	
	public CalculateHopkinsStatistic(double[][] data, int resamples, int maxPicks, int distribution, int type){
		this.resamples=resamples;
		this.type=type;
		this.pick=maxPicks;
		this.distribution=distribution;
		int dims=data[0].length;
		int n=data.length;
		
		double[][] inputData=new double[dims][n];
		
		BasicStatistics bs=new BasicStatistics();
		
		for (int i=0; i<dims; i++){
			for (int j=0; j<n; j++){
				inputData[i][j]=data[j][i];
			}
			float av=(float)bs.calculateMean(inputData[i]);
			for (int j=0; j<n; j++){
				inputData[i][j]-=av;
			}
		}
		
		
		double[]sds=new double[dims];
		
		
		for (int i=0; i<dims; i++){
			sds[i]=bs.calculateSD(inputData[i], true);
		}
	
		//double realdensity[]=calculateDensity(inputData);
		//int p=(int)Math.round(inputData.length);
		//double threshold=realdensity[p];
		//realdensity=null;
		//double realscore=calculateSumNearestNeighbourGreaterThanX(inputData, threshold);
		//int[] picks={1, 2, 5, 10, 20, 50};
		
		
		if (pick>=n-1){
			pick=n-2;
		}
		
		/*
		for (int i=0; i<picks.length; i++){
			if (picks[i]>=n-1){
				picks[i]=n-2;
			}
		}
		*/
				
		//double realscore[]=calculateSumNNearestNeighbour(inputData, picks);
		double realscore=calculateSumNNearestNeighbour(inputData, pick);
		
		
		//double realscore=calculateSumNNearestNeighbour(inputData, 5);
		//double realdensity[]=calculateDensity(inputData);
		double[][] simData=new double[dims][n];
		//double[][] simresults=new double [picks.length][resamples];
		double[] simresults=new double [resamples];
		//double[][] simresults2=new double [realdensity.length][resamples];
		//double[][]sdsim=new double[dims][resamples];


		//silhouetteMax=new double[n][resamples];

		for (int i=0; i<resamples; i++){
			for (int j=0; j<dims; j++){
				for (int k=0; k<n; k++){
					if (distribution==0){
						simData[j][k]=random.nextGaussian()*sds[j];
					}
					else if (distribution==1){
						simData[j][k]=random.nextDouble()*sds[j];
					}
				}
				//sdsim[j][i]=bs.calculateSD(simData[j], false)-sds[j];				
			}
			//double[] score=calculateSumNNearestNeighbour(simData, inputData, picks);
			//double score=calculateSumNNearestNeighbour(simData, inputData, pick);
			double score=calculateSumNNearestNeighbour(simData, pick);
			//double score=calculateSumNearestNeighbourGreaterThanX(simData, threshold);
			
			simresults[i]=score/(score+realscore);
			
			//for (int j=0; j<picks.length; j++){
				//simresults[j][i]=score[j]/(score[j]+realscore[j]);
			//}
			//double [] score2=calculateDensity(simData);
			//for (int j=0; j<simresults2.length; j++){
			//	simresults2[j][i]=score2[j]/(score2[j]+realdensity[j]);
			//}
			
			//for (int j=0; j<dims; j++){
			//	for (int k=0; k<n; k++){
			//		simData[j][k]=(float)(random.nextDouble()*sds[j]);
			//	}
			//}

			//float[][] matrix=createDistanceMatrix(simData);
			//UPGMA upgma=new UPGMA(matrix);
			//for (int j=0; j<upgma.silhouette.length; j++){
			//	silhouetteMax[j][i]=upgma.silhouette[j];
			//}
			//upgma=null;
			//matrix=null;
			
		}
		
		
		//for (int i=0; i<n; i++){
			//Arrays.sort(silhouetteMax[i]);
		//}
		
		//double[] meanscore=new double[picks.length];
		//double[] sdscore=new double[picks.length];
		//double[] upper2point5=new double[picks.length];
		//double[] lower2point5=new double[picks.length];
		//resultString=new String[picks.length];
		resultString=new String("");
		results=new double[4];
		//results=new double[picks.length][4];
		
		double meanscore=bs.calculateMean(simresults);
		double sdscore=bs.calculateSD(simresults, true);
		double upper2point5=bs.calculatePercentile(simresults, 2.5, true);
		double lower2point5=bs.calculatePercentile(simresults, 2.5, false);
		results[0]=meanscore;
		results[1]=sdscore;
		results[2]=upper2point5;
		results[3]=lower2point5;
		resultString=pick+" MEAN: "+meanscore+ " SD: "+sdscore+" UPPER 2.5: "+upper2point5+" LOWER 2.5: "+lower2point5;
		
		/*
		for (int i=0; i<picks.length; i++){	
			double meanscore=bs.calculateMean(simresults[i]);
			double sdscore=bs.calculateSD(simresults[i], true);
			double upper2point5=bs.calculatePercentile(simresults[i], 2.5, true);
			double lower2point5=bs.calculatePercentile(simresults[i], 2.5, false);
			results[i][0]=meanscore;
			results[i][1]=sdscore;
			results[i][2]=upper2point5;
			results[i][3]=lower2point5;
			resultString[i]=picks[i]+" MEAN: "+meanscore+ " SD: "+sdscore+" UPPER 2.5: "+upper2point5+" LOWER 2.5: "+lower2point5;
		}
		*/
		/*for (int i=0; i<dims; i++){
			double meansd=bs.calculateMean(sdsim[i]);
			double sdsd=bs.calculateSD(sdsim[i], false);
		}
		for (int i=0; i<simresults2.length; i++){
			double meanscore2=bs.calculateMean(simresults2[i]);
			double sdscore2=bs.calculateSD(simresults2[i], true);
			double upper2point52=bs.calculatePercentile(simresults2[i], 2.5, true);
			double lower2point52=bs.calculatePercentile(simresults2[i], 2.5, false);
		}
		*/
	}
	
	public int getType(){
		return type;
	}
	
	public int getResamples(){
		return resamples;
	}
	
	//public String[] getResultString(){
		//return resultString;
	//}
	
	public String getResultString(){
		return resultString;
	}
	
	//public double[][] getResults(){
		//return results;
	//}
	
	public double[] getResults(){
		return results;
	}
	
	//public int[] getPicks(){
		//return picks;
	//}
	
	public int getPicks(){
		return pick;
	}
	
	public double[][] getSilhouetteMax(){
		return silhouetteMax;
	}

	public double calculateSumNearestNeighbour(double[][] data){
	
		double sum=0;
		double suma=0;
		double a;
		int n=data.length; 
		int m=data[0].length;
		
		for (int i=0; i<m; i++){
			double min=100000;
			for (int j=0; j<m; j++){
				if (i!=j){
					suma=0;
					for (int k=0; k<n; k++){
						a=data[k][i]-data[k][j];
						suma+=a*a;
					}
					if (suma<min){
						min=suma;
					}				
				}
			}
			sum+=Math.sqrt(min);
			//sum+=min;
		}
		return sum;	
	}
	
	public double calculateSumNearestNeighbour(double[][] data1, double[][] data2){
	
		double sum=0;
		double suma=0;
		double a;
		int n=data1.length; 
		int m=data1[0].length;
		
		for (int i=0; i<m; i++){
			double min=100000;
			for (int j=0; j<m; j++){
				suma=0;
				for (int k=0; k<n; k++){
					a=data1[k][i]-data2[k][j];
					suma+=a*a;
				}
				if (suma<min){
					min=suma;
				}				
			}
			sum+=Math.sqrt(min);
			//sum+=min;
		}
		return sum;	
	}

	
	public double calculateSumNearestNeighbourGreaterThanX(double[][] data, double X){
	
		double sum=0;
		double suma=0;
		double a;
		int n=data.length; 
		int m=data[0].length;
		
		for (int i=0; i<m; i++){
			double min=100000;
			for (int j=0; j<m; j++){
				if (i!=j){
					suma=0;
					for (int k=0; k<n; k++){
						a=data[k][i]-data[k][j];
						suma+=a*a;
					}
					if ((suma>X)&&(suma<min)){
						min=suma;
					}				
				}
			}
			//sum+=Math.sqrt(min);
			sum+=min;
		}
		return sum;	
	}
	
	public double calculateSumNNearestNeighbour(double[][] data, int N){
	
		double sum=0;
		double suma=0;
		double a;
		int n=data.length; 
		int N2=N-1;
		int m=data[0].length;
		
		for (int i=0; i<m; i++){
			double[] nearest=new double[N];
			for (int j=0; j<N; j++){
				nearest[j]=1000000000;
			}
			for (int j=0; j<m; j++){
				if (i!=j){
					suma=0;
					for (int k=0; k<n; k++){
						a=data[k][i]-data[k][j];
						suma+=a*a;
					}
					if (suma<nearest[N2]){
						nearest[N2]=suma;
						Arrays.sort(nearest);
					}				
				}
			}
			//sum+=Math.sqrt(min);
			sum+=nearest[N2];
		}
		return sum;	
	}
	
	public double[] calculateSumNNearestNeighbour(double[][] data, int[] N){
	
		double suma=0;
		double a;
		int n=data.length; 
		int m=data[0].length;
		
		double[] holder=new double[m];
		double results[]=new double[N.length];
		for (int i=0; i<m; i++){
			for (int j=0; j<m; j++){
				suma=0;
				for (int k=0; k<n; k++){
					a=data[k][i]-data[k][j];
					suma+=a*a;
				}	
				holder[j]=suma;
			}
			Arrays.sort(holder);
			for (int j=0; j<N.length; j++){
				if (N[j]<holder.length){
					results[j]+=Math.sqrt(holder[N[j]]);
				}
			}
		}
		
		for (int j=0; j<N.length; j++){
			results[j]/=m+0.0;
		}

		return results;	
	}
	
	public double[] calculateSumNNearestNeighbour(double[][] data1, double[][] data2, int[] N){
	
		double suma=0;
		double a;
		int n=data1.length; 
		int m=data1[0].length;
		
		double[] holder=new double[m];
		double results[]=new double[N.length];
		for (int i=0; i<m; i++){
			for (int j=0; j<m; j++){
				suma=0;
				for (int k=0; k<n; k++){
					a=data1[k][i]-data2[k][j];
					suma+=a*a;
				}	
				holder[j]=suma;
			}
			Arrays.sort(holder);
			for (int j=0; j<N.length; j++){
				results[j]+=Math.sqrt(holder[N[j]-1]);	// have to take 1 off because first nearest neighbour is at position 0 in the matrix
			}				// this does not apply in the other version of this method above because in that case, position 0 is always taken by comparisons with self (which should be ignored)
		}
		
		for (int j=0; j<N.length; j++){
			results[j]/=m+0.0;
		}

		return results;	
	}
	
	public double calculateSumNNearestNeighbour(double[][] data1, double[][] data2, int N){
		
		double suma=0;
		double a;
		int n=data1.length; 
		int m=data1[0].length;
		
		double[] holder=new double[m];
		double results=0;
		int N2=N-1; // have to take 1 off because first nearest neighbour is at position 0 in the matrix
		for (int i=0; i<m; i++){
			for (int j=0; j<m; j++){
				suma=0;
				for (int k=0; k<n; k++){
					a=data1[k][i]-data2[k][j];
					suma+=a*a;
				}	
				holder[j]=suma;
			}
			Arrays.sort(holder);
			results+=Math.sqrt(holder[N2]);	
		}
		
		results/=m+0.0;


		return results;	
	}


	
	public double[] calculateDensity(double[][] data){
	
	
		int n=data.length; 
		int m=data[0].length;
		
		double[] d=new double[m*(m-1)/2];
		
		int p=0;
		double suma=0;
		double a;
		for (int i=0; i<m; i++){
			for (int j=0; j<i; j++){
				suma=0;
				for (int k=0; k<n; k++){
					a=data[k][i]-data[k][j];
					suma+=a*a;
				}
				//d[p]=Math.sqrt(suma);
				d[p]=suma;
				p++;
			}
		}
		
		Arrays.sort(d);
		return d;
	
	}
	
	public float[][] createDistanceMatrix(double[][] data){
		int n=data.length; 
		int m=data[0].length;
		
		float[][] results=new float[m][];
		for (int i=0; i<m; i++){
			results[i]=new float[i+1];
			
			for (int j=0; j<i; j++){
			
				double score=0;
				for (int k=0; k<n; k++){
					double diff=data[k][i]-data[k][j];
					score+=diff*diff;
				}
				results[i][j]=(float)Math.sqrt(score);
			}
		}
		return results;
	}
	
}
