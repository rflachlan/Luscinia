package lusc.net.github.analysis.syntax;
//
//  SyntaxAnalysis.java
//  Luscinia
//
//  Created by Robert Lachlan on 9/15/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

import java.util.*;

import lusc.net.github.analysis.BasicStatistics;
import lusc.net.github.analysis.AnalysisGroup;

public class SyntaxAnalysis {

	float increment=0.05f;
	int nrepeats=1000;
	int[] locs={1, 2, 5, 10, 20};
	boolean[] hasPrevious, hasNext;
	int[] songLengths;
	float[][] data;
	double[] transLabels=null;
	String[] resultString=null;
	double minimumSDdev=0;
	int[][] exceedMax;
	
	
	
	int n, n2, m, nSongs;
	BasicStatistics bs=new BasicStatistics();
	Random random=new Random(System.currentTimeMillis());

	public SyntaxAnalysis(float[][] input, int[][] lookUps, boolean waste){
	
		this.nSongs=nSongs;
		n=input.length;
		setUpNext(lookUps);
		m=0;
		for (int i=0; i<n; i++){
			if (!hasPrevious[i]){m++;}
		}
		n2=n-m;
		
		data=new float[n][n];
		
		float[][] reald2=new float[n][n];
		
		float[][] simd2=new float[n][n];
		float[][] maxd2=new float[n][n];
		float[][] maxd=new float[n][n];
		float[][] mind2=new float[n][n];
		
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				data[i][j]=input[i][j];
				data[j][i]=input[i][j];
				mind2[i][j]=1000000f;
			}
		}
		
		
		float[][] si1=new float[n2][];
		for (int i=0; i<n2; i++){
			si1[i]=new float[i+1];
		}
		int[] order=new int[n];
		getStraightOrder(order);
		syntaxCompression2(si1, order);
		double[] realResult=calculateMeanKNearestNeighbour(si1, locs);
		
		//syntaxCompareAll(order, reald2);
		
		double[] perpointreal=syntaxComparison2(order, 5);
		
		double[] perpointsim;
		
		int p=locs.length;
		
		double[][] simResults=new double[p][nrepeats];
		int[] perpointcount=new int[n2];
		
		
		
		for (int i=0; i<nrepeats; i++){
			reorder(order);
			syntaxCompression2(si1, order);
			double[] sim=calculateMeanKNearestNeighbour(si1, locs);
			for (int j=0; j<p; j++){
				simResults[j][i]=sim[j]/(sim[j]+realResult[j]);
			}
			perpointsim=syntaxComparison2(order, 5);
			for (int j=0; j<n2; j++){
				if (perpointsim[j]>perpointreal[j]){perpointcount[j]++;}
			}
			
			//syntaxCompareAll(order, simd2);
			
			//for (int j=0; j<n; j++){
			//	for (int k=0; k<n; k++){
			//		maxd2[j][k]+=simd2[j][k]*simd2[j][k];
			//		maxd[j][k]+=simd2[j][k];
				
					//if (simd2[j][k]>=maxd2[j][k]){maxd2[j][k]=simd2[j][k];}
					//if (simd2[j][k]<=mind2[j][k]){mind2[j][k]=simd2[j][k];}
			//	}
			//}		
		}
		
		double mincount=10000;
		double maxcount=0;
		
		for (int i=0; i<n2; i++){
			if (perpointcount[i]>maxcount){maxcount=perpointcount[i];}
			if (perpointcount[i]<mincount){mincount=perpointcount[i];}
		}
		transLabels=new double[n];
		for (int i=0; i<n2; i++){
			transLabels[i]=perpointcount[i]/(nrepeats+0.0);
		}
		
		
		
		BasicStatistics bs=new BasicStatistics();
		
		
		
		double[] mean=new double[p];
		double[] sd=new double[p];
		double[] upper=new double[p];
		double[] lower=new double[p];
		resultString=new String[p];
		for (int i=0; i<p; i++){
			mean[i]=bs.calculateMean(simResults[i]);
			sd[i]=bs.calculateSD(simResults[i], true);
			upper[i]=bs.calculatePercentile(simResults[i], 2.5, true);
			lower[i]=bs.calculatePercentile(simResults[i], 2.5, false);
			resultString[i]="SYNTAX ANAL: "+mean[i]+" "+sd[i]+" "+upper[i]+" "+lower[i];
		}	
		
		//getStraightOrder(order);
		//	syntaxCompression2(si1, order);
		//	data=si1;
		
		si1=null;
		/*
		double nr=nrepeats+0.0;
		
		int q=50;
		if (q>n/4){q=n/4;}
		
		
		exceedMax=new int[q][2];
		double[] best=new double[q];
		double worstdev=0;
		int worstloc=0;
		
		int[] checker=new int [n];
		for (int i=0; i<n; i++){
			checker[i]=-1;
		}
		
		for (int i=0; i<n; i++){
		
			double bestdev=0;
			int bestloc1=0;
			int bestloc2=0;
			for (int j=i+1; j<n; j++){
			
				double mean1=maxd[i][j]/nr;
				
				double sd1=Math.sqrt((maxd2[i][j]/nr)-(mean1*mean1));
				
				double meandev=reald2[i][j]-mean1;
				
				meandev=(float)(meandev/sd1);
				
				if (meandev>bestdev){
					if ((checker[j]==-1)||(best[checker[j]]<meandev)){
						bestdev=meandev;
						bestloc1=i;
						bestloc2=j;
					}
				}
			}
			
			if (bestdev>0){
				if (checker[bestloc2]>-1){
					int w=checker[bestloc2];
					best[w]=bestdev;
					exceedMax[w][0]=bestloc1;
					worstdev=1000000;
					for (int k=0; k<q; k++){
						if (best[k]<worstdev){
							worstdev=best[k];
							worstloc=k;
						}
					}
				}
				else if (bestdev>worstdev){
					best[worstloc]=bestdev;
					exceedMax[worstloc][0]=bestloc1;
					exceedMax[worstloc][1]=bestloc2;
					worstdev=1000000;
					for (int k=0; k<n; k++){
						if (checker[k]==worstloc){
							checker[k]=-1;
						}
					}
					checker[bestloc2]=worstloc;
					for (int k=0; k<q; k++){
						if (best[k]<worstdev){
							worstdev=best[k];
							worstloc=k;
						}
					}
				}
			}
		}
		minimumSDdev=worstdev;		
		resultString[4]="MinSD: "+worstdev;
		
		*/
	}
	
	public SyntaxAnalysis(float[][] input, int[][] lookUps){
				
		int resamples=1000;
		n=lookUps.length;
		int p=locs.length;
	
		boolean[] terminal=new boolean[n];
		int nTransitions=0;
		nSongs=1;
		for (int i=0; i<n-1; i++){
			if (lookUps[i][0]!=lookUps[i+1][0]){
				terminal[i]=true;
				nSongs++;
			}
			else{
				terminal[i]=false;
				nTransitions++;
			}
		}
		n2=n-nSongs;
		terminal[n-1]=true;
		
		double score1, score2;
		
		int[][] realDat=new int[nTransitions][2];
		int j=0;
		for (int i=0; i<n-1; i++){
			if (!terminal[i]){
				realDat[j][0]=i;
				realDat[j][1]=i+1;
				j++;
			}
		}
		
		double refScores[][]=new double[p][nTransitions];
		double refScores2[][]=new double[p][nTransitions];
		for (int i=0; i<100; i++){
			calculateKNNDistance(realDat, realDat, input, refScores2, locs, true);
			for (j=0; j<p; j++){
				for (int k=0; k<nTransitions; k++){
					refScores[j][k]+=refScores2[j][k];
				}
			}
		}
		for (int i=0; i<p; i++){
			for (j=0; j<nTransitions; j++){
				refScores[i][j]/=100.0;
			}
		}
		/*
		for (int i=0; i<p; i++){
			calculateKNNDistance(realDat, realDat, input, refScores[i], locs[i]);
			//calculateDensityDistance(ref, realDat, input, refScores, 2);
		}
		*/
		
		
		double[][] simScores=new double[p][nTransitions];
		double[][] labels=new double[p][resamples];
		double[] perPoint=new double[nTransitions];
		int[][] simDat=new int[nTransitions][2];
		int[] order=new int[n];
		
		transLabels=new double[nTransitions];
		
		setUpNext(lookUps);
		
		
		for (int i=0; i<resamples; i++){
			
			reorderWithinSong(order);
			//for (int ii=0; ii<order.length; ii++){System.out.println(ii+" "+n+" "+order[ii]);}
			/*
			for (j=0; j<n; j++){
				order[j]=j;
			}
			for (j=0; j<n; j++){
				int r=random.nextInt(n-j)+j;
				int q=order[r];
				order[r]=order[j];
				order[j]=q;
			}
			*/
			 
			int s=0;
			for (j=0; j<n-1; j++){
				if (!terminal[j]){
					simDat[s][0]=order[j];
					simDat[s][1]=order[j+1];
					s++;
				}
			}
			calculateKNNDistance(realDat, simDat, input, simScores, locs, false);
			for (j=0; j<p; j++){
				for (int k=0; k<nTransitions; k++){
					//double u=simScores[j][k]/(simScores[j][k]+refScores[j][k]);
					//labels[j][i]+=u;
					//if (j==1){
					//	perPoint[k]+=u;
					//}
					
					if (simScores[j][k]>refScores[j][k]){
						labels[j][i]++;
						if (j==1){
							perPoint[k]++;
						}
					}
				}
			}
		}
				
		for (int i=0; i<nTransitions; i++){
			transLabels[i]=perPoint[i]/(resamples+0.0);
		}
		for (int i=0; i<resamples; i++){
			for (j=0; j<p; j++){
				labels[j][i]/=nTransitions+0.0;
			}
		}
		BasicStatistics bs=new BasicStatistics();
		
		double[] mean=new double[p];
		double[] sd=new double[p];
		double[] upper=new double[p];
		double[] lower=new double[p];
		resultString=new String[p];
		for (int i=0; i<p; i++){
			mean[i]=bs.calculateMean(labels[i]);
			sd[i]=bs.calculateSD(labels[i], true);
			upper[i]=bs.calculatePercentile(labels[i], 2.5, true);
			lower[i]=bs.calculatePercentile(labels[i], 2.5, false);
			resultString[i]="SYNTAX ANAL: "+mean[i]+" "+sd[i]+" "+upper[i]+" "+lower[i];
		}	
	}
	
	public SyntaxAnalysis(int[] sample, float[][] input, int[][] lookUps){
		
		int resamples=500;
		
		int m=lookUps.length;
		int n=sample.length;
		int n2=n*n;
		int[][] ref=new int[n2][2];
		int k=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<n; j++){
				ref[k][0]=i;
				ref[k][1]=j;
				k++;
			}
		}
		
		boolean[] terminal=new boolean[m];
		int nTransitions=0;
		int nSongs=1;
		for (int i=0; i<m-1; i++){
			if (lookUps[i][0]!=lookUps[i+1][0]){
				terminal[i]=true;
				nSongs++;
			}
			else{
				terminal[i]=false;
				nTransitions++;
			}
		}
		terminal[m-1]=true;
		
		double score1, score2;
		
		int[][] realDat=new int[nTransitions][2];
		int j=0;
		for (int i=0; i<m-1; i++){
			if (!terminal[i]){
				realDat[j][0]=i;
				realDat[j][1]=i+1;
				j++;
			}
		}
		
		double refScores[]=new double[n2];
		
		calculateKNNDistance(ref, realDat, input, refScores, 5);
		//calculateDensityDistance(ref, realDat, input, refScores, 2);
		
		int[] locs=new int[m];
		
		double[] simScores=new double[n2];
		int[][] simDat=new int[nTransitions][2];
		
		transLabels=new double[n2];
		
		for (int i=0; i<resamples; i++){
			
			for (j=0; j<m; j++){
				locs[j]=j;
			}
			for (j=0; j<m; j++){
				int r=random.nextInt(m-j)+j;
				int p=locs[r];
				locs[r]=locs[j];
				locs[j]=p;
			}
			int s=0;
			for (j=0; j<m-1; j++){
				if (!terminal[j]){
					simDat[s][0]=locs[j];
					simDat[s][1]=locs[j+1];
					s++;
				}
			}
			
			calculateKNNDistance(ref, simDat, input, simScores, 5);
			//calculateDensityDistance(ref, simDat, input, simScores, 2);
			for (j=0; j<n2; j++){
				if (simScores[j]>refScores[j]){
					transLabels[j]++;
				}
				//transLabels[j]+=simScores[j];
			}
		}
		for (int i=0; i<n2; i++){
			transLabels[i]/=resamples+0.0;
			// transLabels[i]=Math.log((1+refScores[i])/(1+transLabels[i]));
			//transLabels[i]=Math.log(transLabels[i]/refScores[i]);
		}
	}
	
	public void calculateKNNDistance(int[][] ref1, int[][] ref2, float[][] scores, double[] results, int k){
		int n=ref1.length;
		int m=ref2.length;
		double[] q=new double[k];
		double score;
		for (int i=0; i<n; i++){
			for (int j=0; j<k; j++){
				q[j]=1000000;
			}
			for (int j=0; j<m; j++){
				if (ref1[i][0]>ref2[j][0]){
					score=scores[ref1[i][0]][ref2[j][0]];
				}
				else{
					score=scores[ref2[j][0]][ref1[i][0]];
				}
				if (ref1[i][1]>ref2[j][1]){
					score+=scores[ref1[i][1]][ref2[j][1]];
				}
				else{
					score+=scores[ref2[j][1]][ref1[i][1]];
				}
				if (score<q[0]){		//if trans distance is < smallest diff in buffer, put it in buffer
					q[0]=score;
					Arrays.sort(q);
				}
			}
			results[i]=q[0];
		}
	}
	
	public void calculateKNNDistance(int[][] ref1, int[][] ref2, float[][] scores, double[][] results, int[] locs, boolean randomDelete){
		int n=ref1.length;
		int m=ref2.length;
		int p=locs[locs.length-1];
		int r=p-1;
		double[] q=new double[p];
		
		double score, score2;
		for (int i=0; i<n; i++){
			for (int j=0; j<p; j++){
				q[j]=1000000;
			}
			int rd=0;
			if (randomDelete){
				rd=random.nextInt(m);
			}
			
			double min=1000000;
			double max=0;
			
			for (int j=0; j<m; j++){
				if ((!randomDelete)||(j!=rd)){
					if (ref1[i][0]>ref2[j][0]){
						score=scores[ref1[i][0]][ref2[j][0]];
					}
					else{
						score=scores[ref2[j][0]][ref1[i][0]];
					}
					if (ref1[i][1]>ref2[j][1]){
						score2=scores[ref1[i][1]][ref2[j][1]];
					}
					else{
						score2=scores[ref2[j][1]][ref1[i][1]];
					}
					//score=Math.sqrt(score*score+score2*score2);
					score=Math.sqrt(score*score2);
					if ((ref1[i][0]!=ref2[j][0])&&(ref1[i][1]!=ref2[j][1])){
						if (score<q[r]){		//if trans distance is < biggest diff in buffer, put it in buffer
							int subloc=0;
							for (int k=r-1; k>=0; k--){
								if (score<q[k]){
									q[k+1]=q[k];
								}
								else{
									subloc=k+1;
									k=-1;
								}
							}
							q[subloc]=score;
						}
						if (score<min){
							min=score;
						}
						if (score>max){
							max=score;
						}
					}
				}
			}
			for (int j=0; j<locs.length; j++){
				results[j][i]=q[locs[j]-1];
			}
		}
	}

	public void calculateDensityDistance(int[][] ref1, int[][] ref2, float[][] scores, double[] results, double p){
		int n=ref1.length;
		int m=ref2.length;
		double score;
		for (int i=0; i<n; i++){
			int counter=0;
			for (int j=0; j<m; j++){
				if (ref1[i][0]>ref2[j][0]){
					score=scores[ref1[i][0]][ref2[j][0]];
				}
				else{
					score=scores[ref2[j][0]][ref1[i][0]];
				}
				if (ref1[i][1]>ref2[j][1]){
					score+=scores[ref1[i][1]][ref2[j][1]];
				}
				else{
					score+=scores[ref2[j][1]][ref1[i][1]];
				}
				if (score<p){		
					counter++;
				}
			}
			results[i]=counter;
		}
	}
	
	public float[][] calculateSampleDistanceMatrix(int[] sample, float[][] input){
		
		int n=sample.length;
		int n2=n*n;
		int[][] ref=new int[n2][2];
		int k=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<n; j++){
				ref[k][0]=sample[i];
				ref[k][1]=sample[j];
				k++;
			}
		}
		
		float[][] out=new float[n2][];
		for (int i=0; i<n2; i++){
			out[i]=new float[i+1];
			
			for (int j=0; j<i; j++){
				if (ref[i][0]>ref[j][0]){
					out[i][j]=input[ref[i][0]][ref[j][0]];
				}
				else{
					out[i][j]=input[ref[j][0]][ref[i][0]];
				}
				if (ref[i][1]>ref[j][1]){
					out[i][j]+=input[ref[i][1]][ref[j][1]];
				}
				else{
					out[i][j]+=input[ref[j][1]][ref[i][1]];
				}
			}
		}
		ref=null;
		return out;
	}
	
	
	/*
	
	public SyntaxAnalysis(float[][] input, int[][] lookUps, int nrepeats){
		n=input.length;
		setUpNext(lookUps);
		m=0;
		
		double dthresh=0.5;
		
		for (int i=0; i<n; i++){
			if (!hasPrevious[i]){m++;}
		}
		n2=n-m;
	
		float [][] d2=new float[n][];
		data=new float[n][];
		float [][] dref=new float[n][];
		for (int i=0; i<n; i++){
			d2[i]=new float[i+1];
			data[i]=new float[i+1];
			dref[i]=new float[i+1];
			for (int j=0; j<i; j++){
				d2[i][j]=input[i][j];
			}
		}
		normalizeMatrix(d2);
		
		syntaxCompression3(d2, dref);
	
		double beststandard=-1000000;
		int bestloc=0;
	
		for (int i=0; i<nrepeats; i++){
			squareMatrix(d2);
			syntaxCompression3(d2, data);
			squareRootMatrix(data);
			copyMatrix(data, d2);
			normalizeMatrix(d2);
			double avdist=calculateAverageDensity(d2, dthresh);
			double compression=compareMatrices(dref, d2);
			double sumstandard=Math.log(avdist)-Math.log(compression);
			if ((i>10)&&(sumstandard>beststandard)){
				beststandard=sumstandard;
				bestloc=i;
			}
		}	
		/*
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				d2[i][j]=input[i][j];
			}
		}
		normalizeMatrix(d2);
		
		syntaxCompression3(d2, dref);
		bestloc++;
	
		for (int i=0; i<bestloc; i++){
			syntaxCompression3(d2, data);
			copyMatrix(data, d2);
			normalizeMatrix(d2);
		}	
			
	}

	*/
	
	
	
	public void setUpNext(int[][] lookUp){
	
		hasNext=new boolean[n];
		hasPrevious=new boolean[n];
		songLengths=new int[nSongs];
		int a=0;
		int b=0;
		for (int i=0; i<n; i++){
			if ((i<n-1)&&(lookUp[i][0]==lookUp[i+1][0])){
				hasNext[i]=true;
				a++;
			}
			else{
				hasNext[i]=false;
				songLengths[b]=a+1;
				a=0;
				b++;
			}
			if ((i>0)&&(lookUp[i][0]==lookUp[i-1][0])){
				hasPrevious[i]=true;
			}
			else{
				hasPrevious[i]=false;
			}
		}	
	}
	
	public void reorder(int[] order){
		int m=n;
		int c, b;
		for (int i=0; i<n; i++){
			order[i]=i;
		}
		
		for (int i=0; i<n; i++){
			c=random.nextInt(m);
			c+=i;
			b=order[i];
			order[i]=order[c];
			order[c]=b;
			m--;
		}
	}
	
	public void getStraightOrder(int[] order){
		for (int i=0; i<n; i++){
			order[i]=i;
		}
	}
	
	public void reorderWithinSong(int[] order){
		//System.out.println("here"+n);
		for (int i=0; i<n; i++){
			order[i]=i;
			//System.out.println("a"+i+" "+n+" "+order[i]);
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
	
	public void syntaxCompression(float[][] output, int[] order){
		
		for (int i=0; i<n; i++){
			int ii=order[i];
			for (int j=0; j<i; j++){
				int jj=order[j];
				output[i][j]=data[ii][jj];
				if ((hasPrevious[i])&&(hasPrevious[j])){
					output[i][j]+=increment*data[order[i-1]][order[j-1]];
				}
				else if ((hasNext[i])&&(hasNext[j])){
					output[i][j]+=increment*data[order[i+1]][order[j+1]];
				}
				else{
					output[i][j]+=increment*data[ii][jj];
				}
				/*
				if ((hasNext[i])&&(hasNext[j])){
					output[i][j]+=increment*data[order[i+1]][order[j+1]];
				}
				else if ((hasPrevious[i])&&(hasPrevious[j])){
					output[i][j]+=increment*data[order[i-1]][order[j-1]];
				}
				else{
					output[i][j]+=increment*data[ii][jj];
				}
				*/
			}
		}
	}
	
	public void syntaxCompression2(float[][] output, int[] order){
		
		System.out.println("Comp: "+n);
		
		int ic=0;
		for (int i=0; i<n; i++){
			int jc=0;
			System.out.println(ic+" "+output[ic].length);
			if (hasPrevious[i]){
				int i1=order[i];
				int i2=order[i-1];
				for (int j=0; j<i; j++){
					if (hasPrevious[j]){
						//output[ic][jc]=data[i1][order[j]]+data[i2][order[j-1]];
						//output[ic][jc]=(float)Math.sqrt(data[i1][order[j]]*data[i1][order[j]]+data[i2][order[j-1]]*data[i2][order[j-1]]);
						//output[ic][jc]=(float)Math.sqrt(data[i1][order[j]]*data[i2][order[j-1]]);
						System.out.println(i+" "+j);
						System.out.println(data[i][j]);
						System.out.println(data[i-1][j-1]);
						System.out.println(ic+" "+jc);
						output[ic][jc]=(float)Math.sqrt(data[i][j]*data[i-1][j-1]);
						
						//output[ic][jc]=(float)Math.max(data[i1][order[j]],data[i2][order[j-1]]);
						jc++;
					}
				}
				ic++;
			}	
		}
	}
	
	public void syntaxCompression3(float[][] input, float[][] output){
		float c;
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				output[i][j]=input[i][j];
				
				c=0;
				
				if ((hasPrevious[i])&&(hasPrevious[j])){
					c+=input[i-1][j-1];
				}
				else if ((hasNext[i])&&(hasNext[j])){
					c+=input[i+1][j+1];
				}
				else{
					c+=input[i][j];
				}
				if ((hasNext[i])&&(hasNext[j])){
					c+=input[i+1][j+1];
				}
				else if ((hasPrevious[i])&&(hasPrevious[j])){
					c+=input[i-1][j-1];
				}
				else{
					c+=input[i][j];
				}
				
				//if (c<2*input[i][j]){
					output[i][j]+=increment*c;
				//}
				//else{
				//	output[i][j]+=2*increment*input[i][j];
				//}
				
			}
		}
	
	}
	
	public double[] syntaxComparison(int[] order, int k){
		
		double[] results=new double[n];
		double[] holder=new double[n];
		for (int i=0; i<n; i++){
			for (int j=0; j<n; j++){
				int a=order[j];
				if (a!=i){
					double h=data[i][a];					
					if ((hasPrevious[i])&&(hasPrevious[j])){
						h+=increment*data[i-1][order[j-1]];
					}
					else if ((hasNext[i])&&(hasNext[j])){
						h+=increment*data[i+1][order[j+1]];
					}
					else{
						h+=increment*data[i][a];
					}
					/*
					if ((hasNext[i])&&(hasNext[j])){
						h+=increment*data[i+1][order[j+1]];
					}
					else if ((hasPrevious[i])&&(hasPrevious[j])){
						h+=increment*data[i-1][order[j-1]];
					}
					else{
						h+=increment*data[i][a];
					}
					*/
					holder[j]=h;
				}
			}
			Arrays.sort(holder);
			results[i]=holder[k];
		}
		return results;				
	}
	
	public double[] syntaxComparison2(int[] order, int k){
		
		double[] results=new double[n2];
		double[] holder=new double[n2];
		int ii=0;
		for (int i=0; i<n; i++){
			if (hasPrevious[i]){
				int jj=0;
				for (int j=0; j<n; j++){
					if (hasPrevious[j]){
						holder[jj]=data[i][order[j]]+data[i-1][order[j-1]];	
						jj++;				
					}
				}
				Arrays.sort(holder);
				results[ii]=holder[k];
				ii++;
			}
		}
		return results;				
	}	
	
	public int[] syntaxComparisonDensity(int[] order, float threshold){
		
		int[] results=new int[n2];
		int ii=0;
		int count, i, j;
		for (i=0; i<n; i++){
			if (hasPrevious[i]){
				count=0;
				for (j=0; j<n; j++){
					if (hasPrevious[j]){
						if(data[i][order[j]]+data[i-1][order[j-1]]<threshold){
							count++;
						}
					}
				}
				results[ii]=count;
				ii++;
			}
		}
		return results;				
	}	
	
	public void syntaxCompareAll(int[] order, float[][] sum){

		int i, j, k;
		float c, h;
		
		int[] order1=new int[n2];
		int[] order2=new int[n2];
		
		j=0;
		for (i=0; i<n; i++){
			if (hasPrevious[i]){
				order1[j]=order[i];
				order2[j]=order[i-1];
				j++;
			}
		}

		for (i=0; i<n; i++){
			for (j=0; j<n; j++){
				
				c=1000000;
				for (k=0; k<n2; k++){
					h=data[i][order1[k]]+data[j][order2[k]];
					if (h<c){
						c=h;
					}
				}
				sum[i][j]=c;
			}
		}
	}
	
	public void syntaxCompareAllDensity(int[] order, int[][] sum, float threshold){

		int i, j, k, c;
				
		int[] order1=new int[n2];
		int[] order2=new int[n2];
		
		j=0;
		for (i=0; i<n; i++){
			if (hasPrevious[i]){
				order1[j]=order[i];
				order2[j]=order[i-1];
				j++;
			}
		}

		for (i=0; i<n; i++){
			for (j=0; j<n; j++){
				c=0;
				for (k=0; k<n2; k++){
					if (data[i][order1[k]]+data[j][order2[k]]<threshold){
						c++;
					}
				}
				sum[i][j]=c;
			}
		}
	}


	public double[] calculateMeanKNearestNeighbour(float[][] data, int[] locs ){
	
		
		double sum=0;
		int m=locs.length;
		int n=data.length;
		
		double[] holder=new double[n];
		double[] averages=new double[m];
		
		for (int i=0; i<n; i++){
			for (int j=0; j<n; j++){
				if (i>j){
					holder[j]=data[i][j];
				}
				else {
					holder[j]=data[j][i];
				}
			}
			Arrays.sort(holder);
			for (int j=0; j<m; j++){
				averages[j]+=holder[locs[j]];
			}
			
		}
		for (int i=0; i<m; i++){
			averages[i]/=n+0.0;
		}
		return averages;
	}
	
	public double[] calculateMeanKNearestNeighbour(float[][] data, int k){
	
		
		double sum=0;
		int n=data.length;
		
		double[] holder=new double[n];
		double[] knearest=new double[n];
		double maxknearest=0;		
		for (int i=0; i<n; i++){
			for (int j=0; j<n; j++){
				if (i>j){
					holder[j]=data[i][j];
				}
				else {
					holder[j]=data[j][i];
				}
			}
			Arrays.sort(holder);
			knearest[i]=holder[k];
			if (knearest[i]>maxknearest){maxknearest=knearest[i];}
			
		}
		for (int i=0; i<n; i++){
			knearest[i]/=maxknearest;
		}
		return knearest;
	}
	
	public int[] reorderMatrix(float[][] inmatrix, float[][] outmatrix){
		int n=inmatrix.length;
		
		int[] placeholder=new int [n];
		for (int i=0; i<n; i++){
			placeholder[i]=i;
		}
		
		for (int i=0; i<n; i++){
			int p=random.nextInt(n-i);
			p+=i;
			int q=placeholder[i];
			placeholder[i]=placeholder[p];
			placeholder[p]=q;
		}
		
		for (int i=0; i<n; i++){
			int a=placeholder[i];
			for (int j=0; j<i; j++){
				int b=placeholder[j];
				if (a>b){
					outmatrix[i][j]=inmatrix[a][b];
				}
				else{
					outmatrix[i][j]=inmatrix[b][a];
				}
			}
		}
		return placeholder;
	}
	
	public void fixUpTrans(AnalysisGroup sg){
		int n=sg.getLookUp(3).length;		
		int n2=n+exceedMax.length;
		int[][] lookUp2=new int[n2][4];
		double[] label2=new double[n2];
		String[] names2=new String[n2];
		int[][] lu=sg.getLookUp(3);
		String[] tn=sg.getNames(3);
		double[] tl=sg.getLabels(3);
		
		for (int i=0; i<n2; i++){
		
			int ii=i-n;
			if (i<n){
				for (int j=0; j<4; j++){
					lookUp2[i][j]=lu[i][j];
				}
				label2[i]=tl[i];
				names2[i]=tn[i];
			}
			else{
				
				lookUp2[i][2]=exceedMax[ii][0];
				lookUp2[i][3]=exceedMax[ii][1];
				label2[i]=-1;
				names2[i]=" ";
			}
		}
		float[][] matrix=new float[n2][];
		for (int i=0; i<n2; i++){
			int i1=lookUp2[i][2];
			int i2=lookUp2[i][3];
			matrix[i]=new float[i+1];
			for (int j=0; j<i; j++){
				int j1=lookUp2[j][2];
				int j2=lookUp2[j][3];
			
				matrix[i][j]=data[i1][j1]+data[i2][j2];
			}
		}
		
		//sg.setLookUp(3, lookUp2);
		//sg.setLabels(3,  label2);
		//sg.setNames(3, names2);
		//sg.setScoresTrans(matrix);
	}
	
	public void calcTransMatrix(AnalysisGroup sg){
	
		float[][] si1=new float[n2][];
		for (int i=0; i<n2; i++){
			si1[i]=new float[i+1];
		}
		int[] order=new int[n];
		
		System.out.println("n2: "+n2+" nSongs: "+nSongs);
		
		getStraightOrder(order);
		syntaxCompression2(si1, order);	
		
		/*
		for (int i=0; i<n2; i++){
			for (int j=0; j<i; j++){
				float p=(float)Math.abs(transLabels[i]-transLabels[j]);
				si1[i][j]+=p;
			}
		}
		*/
		
		//sg.setScoresTrans(si1);
		//sg.setLabels(3, transLabels);	
	}
	
	public double compareMatrices(float[][] mat1, float[][] mat2){
		int n=mat1.length;
		
		double sum=0;
		
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				sum+=Math.abs(mat1[i][j]-mat2[i][j]);
			}
		}
		
		return (sum/(n*(n-1)*0.5));
	}
	
	public double calculateAverageDensity(float[][] mat, double threshold){
		int n=mat.length;
		double sum=0;
		for (int i=0; i<n; i++){
			
			int t=0;
		
			for (int j=0; j<i; j++){
				if (mat[i][j]<threshold){t++;}
			}
			for (int j=i+1; j<n; j++){
				if (mat[j][i]<threshold){t++;}
			}
			sum+=t;
		}
		return(sum/(n+0.0));
	}
	
	public void normalizeMatrix(float[][] mat){
		int n=mat.length;
		float sd=(float)bs.calculateSD(mat, false);
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				mat[i][j]/=sd;
			}
		}
	}
	
	public void copyMatrix(float[][] input, float[][] output){
		int n=input.length;
		
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				output[i][j]=input[i][j];
			}
		}
	}
	
	public void squareMatrix(float[][] mat){
		int n=mat.length;
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				mat[i][j]=mat[i][j]*mat[i][j];
			}
		}
	}
	
	public void squareRootMatrix(float[][] mat){
		int n=mat.length;
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				mat[i][j]=(float)Math.sqrt(mat[i][j]);
			}
		}
	}


}
