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
import lusc.net.github.analysis.ComparisonResults;

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
	
	BasicStatistics bs=new BasicStatistics();
	
	LinkedList<double[]> timeResults=new LinkedList<double[]>();
	
	int maxPicks;
	
	public CalculateHopkinsStatistic(ComparisonResults cr, int resamples, int maxPicks, int distribution, int type){
		this.resamples=resamples;
		this.type=type;
		this.maxPicks=maxPicks;
		this.distribution=distribution;
		
		
		double[][] data=cr.getMDS().getConfiguration();
		int dims=data[0].length;
		int n=data.length;
		
		double[][] inputData=new double[dims][n];

		for (int i=0; i<dims; i++){
			for (int j=0; j<n; j++){
				inputData[i][j]=data[j][i];
			}
			float av=(float)bs.calculateMean(inputData[i]);
			for (int j=0; j<n; j++){
				inputData[i][j]-=av;
			}
		}
		
		
		//if (pick>=n-1){
			//pick=n-2;
		//}
		
		
		double[] simresults=calculateHopkins(inputData);
		
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
		System.out.println(resultString);

		LinkedList<int[]> breaks=cr.getSplits(20, 36);
		
		for (int[] x : breaks){
			System.out.println(x[0]+" "+x[1]+" "+x[2]);
			double[][] idata=new double[dims][x[2]];
			int j=0;
			for (int i=x[0]; i<x[1]; i++){
				for (int k=0; k<dims; k++){
					idata[k][j]=inputData[k][i];
				}
				j++;
			}
			
			double[] sresults=calculateHopkins(idata);
			double[] r=new double[5];
			System.out.println(sresults.length);
			r[0]=x[3]/24.0;
			r[1]=bs.calculateMean(sresults);
			r[2]=bs.calculateSD(sresults, true);
			r[3]=bs.calculatePercentile(sresults, 2.5, true);
			r[4]=bs.calculatePercentile(sresults, 2.5, false);
			System.out.println(r[0]+" "+r[1]+" "+r[2]+" "+r[3]+" "+r[4]);
			timeResults.add(r);
		}
		
		
		
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
	
	public LinkedList<double[]> getTimeResults(){
		return timeResults;
	}
	
	public double[] calculateHopkins(double[][] data){
		int n=data[0].length;
		pick=(int)Math.round(n*maxPicks/100);
		int dims=data.length;
		
		double[]sds=new double[dims];
		for (int i=0; i<dims; i++){
			sds[i]=bs.calculateSD(data[i], true);
		}
		
		double[] realscore=calculateNearestNeighbour(data, data, true);
		
		
		//double realscore=calculateSumNNearestNeighbour(data, pick);
		double[][] simData=new double[dims][pick];
		double[] simresults=new double [resamples];

		for (int i=0; i<resamples; i++){
			boolean[] chosen=new boolean[n];
			double escore=0;
			for (int j=0; j<pick; j++){
				int p=random.nextInt(n);
				while (chosen[p]){p=random.nextInt(n);}
				chosen[p]=true;
				escore+=realscore[p];
			}
			
			
			for (int j=0; j<dims; j++){
				for (int k=0; k<pick; k++){
					if (distribution==0){
						simData[j][k]=random.nextGaussian()*sds[j];
					}
					else if (distribution==1){
						simData[j][k]=random.nextDouble()*2*sds[j];
					}
				}
			}
			
			double[] ssc=calculateNearestNeighbour(simData, data, false);
			double sscore=0;
			for (int j=0; j<pick; j++){
				sscore+=ssc[j];
			}			
			simresults[i]=sscore/(sscore+escore);
		}
		return simresults;
	}
	
	public double[] calculateNearestNeighbour(double[][] data, double[][] data2, boolean avoidIdentical){
		int n=data[0].length;
		int m=data2[0].length;
		int p=data.length;
		double[] out=new double[n];
		
		for (int i=0; i<n; i++){
			double x=Double.MAX_VALUE;
			//int loc=-1;
			for (int j=0; j<m; j++){
				if ((!avoidIdentical)||(i!=j)){
					double y=0;
					for (int k=0; k<p; k++){
						y+=(data[k][i]-data2[k][j])*(data[k][i]-data2[k][j]);
					}
					if (y<x){
						x=y;
						//loc=j;
					}
					
				}
			}
			out[i]=Math.sqrt(x);
		}
		
		return out;
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
