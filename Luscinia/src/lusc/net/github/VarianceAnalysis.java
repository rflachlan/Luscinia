package lusc.net.github;
//
//  VarianceAnalysis.java
//  Luscinia
//
//  Created by Robert Lachlan on 10/14/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

import java.util.*;

public class VarianceAnalysis {

	
	float[][] data;
	int[] popLabels, indLabels, posLabels;
	int[][] syllID;
	double[][] pairwiseTable, pairwiseTablePos, pairwiseTableAligned;
	double[] groupMeans, groupMeans1, groupMeansPos, groupMeansPosUn, groupMeansAligned;
	
	int permutationRepeats=100000;
	
	int n=0;
	int m=0;
	
	int nPops=0;
	int nPos=0;
	int nOverallInds=0;
	int[] nInds;
	
	double[][][] kpropresults; 
	double[] props={0.05, 0.1, 0.2, 0.3, 0.5};
	double deleteProp=0.5;
	int replicates=1000;
	
	Random random=new Random(System.currentTimeMillis());
	
	public VarianceAnalysis(float[][] data, int[] popLabels, int[] indLabels, int[] posLabels, int[][] syllID){
		
		this.data=data;
		this.indLabels=indLabels;
		this.popLabels=popLabels;
		this.posLabels=posLabels;
		this.syllID=syllID;
		
		n=popLabels.length;
		m=data[0].length;
		//this is to merge pops for analysis of Europe together....
		//this.popLabels=new int[n];
		
		nPops=0;
		nPos=0;
		
		for (int i=0; i<n; i++){
			if (popLabels[i]>nPops){
				nPops=popLabels[i];
			}
			if (indLabels[i]>nOverallInds){
				nOverallInds=indLabels[i];
			}
			if (posLabels[i]>nPos){
				nPos=posLabels[i];
			}
			//System.out.println(popLabels[i]+" "+indLabels[i]+" "+posLabels[i]);
		}
		nPops++;
		nPos++;
		nOverallInds++;
		//System.out.println(nPops+" "+nOverallInds+" "+nPos);
		
		nInds=new int[nPops];
		for (int i=0; i<n; i++){
			if (indLabels[i]>nInds[popLabels[i]]){
				nInds[popLabels[i]]=indLabels[i];
			}
			if (posLabels[i]>nPos){
				nPos=posLabels[i];
			}
		}
		
		double[] distToMedian=calculateDistToMean();
		double[] meanDist=new double[nPops];
		double[] count=new double[nPops];
		for (int i=0; i<n; i++){
			meanDist[popLabels[i]]+=distToMedian[i];
			count[popLabels[i]]++;
		}
		double[] meanDistDummy=new double[nPops];
		for (int i=0; i<nPops; i++){
			meanDist[i]/=count[i];
			meanDistDummy[i]=1;
		}
		
		double[] distToMedianPos=calculateDistToMeanPos(meanDist);
		double[] distToMedianPosUn=calculateDistToMeanPos(meanDistDummy);
		//double[] distToMedianAlign=calculateDistToMedianAligned(meanDist);
		
		double[] distToMedianInd=compressToIndividual(distToMedian);
		double[] distToMedianPosInd=compressToIndividual(distToMedianPos);
		double[] distToMedianPosIndUn=compressToIndividual(distToMedianPosUn);
		//double[] distToMedianAlignedInd=compressToIndividual(distToMedianAlign);
		
		int[] compressedGroupLabels=compressLabelToIndividuals(popLabels);
		
		for (int i=0; i<compressedGroupLabels.length; i++){
			System.out.println(compressedGroupLabels[i]+" "+distToMedianInd[i]);
		}
		
		
		groupMeans1=calculateGroupMeans(distToMedianInd, compressedGroupLabels);
		groupMeansPos=calculateGroupMeans(distToMedianPosInd, compressedGroupLabels);
		groupMeansPosUn=calculateGroupMeans(distToMedianPosIndUn, compressedGroupLabels);
		//groupMeansAligned=calculateGroupMeans(distToMedianAlignedInd, compressedGroupLabels);
		groupMeans=groupMeans1;
		pairwiseTable=runPermutation(distToMedianInd, compressedGroupLabels, groupMeans);
		pairwiseTablePos=runPermutation(distToMedianPosInd, compressedGroupLabels, groupMeansPos);
		//pairwiseTableAligned=runPermutation(distToMedianAlignedInd, compressedGroupLabels, groupMeansAligned);
		reportResults();
		/*
		compressedGroupLabels=compressLabelToIndividuals2(popLabels);

		groupMeans1=calculateGroupMeans(distToMedianInd, compressedGroupLabels);
		groupMeansPos=calculateGroupMeans(distToMedianPosInd, compressedGroupLabels);
		groupMeansPosUn=calculateGroupMeans(distToMedianPosIndUn, compressedGroupLabels);
		
		pairwiseTable=runPermutation(distToMedianInd, compressedGroupLabels, groupMeans);
		pairwiseTablePos=runPermutation(distToMedianPosInd, compressedGroupLabels, groupMeansPos);

		reportResults();
		*/
	}
	
	public void reportResults(){
		
		for (int i=0; i<groupMeans.length; i++){
			System.out.print(groupMeans[i]+" ");
		}
		System.out.println();
		for (int i=0; i<groupMeansPos.length; i++){
			System.out.print(groupMeansPos[i]+" ");
		}
		System.out.println();
		for (int i=0; i<groupMeansPosUn.length; i++){
			System.out.print(groupMeansPosUn[i]+" ");
		}
		System.out.println();
		//for (int i=0; i<groupMeansAligned.length; i++){
		//	System.out.print(groupMeansAligned[i]+" ");
		//}
		//System.out.println();
		for (int i=0; i<pairwiseTable.length; i++){
			for (int j=0; j<pairwiseTable[i].length; j++){
				System.out.print(pairwiseTable[i][j]+" ");
			}
			System.out.println();
		}
		System.out.println();
		for (int i=0; i<pairwiseTablePos.length; i++){
			for (int j=0; j<pairwiseTablePos[i].length; j++){
				System.out.print(pairwiseTablePos[i][j]+" ");
			}
			System.out.println();
		}
		System.out.println();
		//for (int i=0; i<pairwiseTableAligned.length; i++){
		//	for (int j=0; j<pairwiseTableAligned[i].length; j++){
		//		System.out.print(pairwiseTableAligned[i][j]+" ");
		//	}
		//	System.out.println();
		//}
		//System.out.println();
		
	}
	
	
	
	public double[][] runPermutation(double[] distToMedian, int[] compressedGroupLabels, double[] groupMeans){
		
		int[] popSizes=countPopulationSampleSize(compressedGroupLabels);
		double[][] distByPopulation=byPopulation(distToMedian, compressedGroupLabels, popSizes);
		
		double[] pd;
		double[] permutedMeans;
		
		double[][] pairwiseTable=new double[nPops][nPops];

		
		for (int i=0; i<nPops; i++){
			for (int j=0; j<nPops; j++){
				if (i!=j){
					
					int[] pairLabels=new int[popSizes[i]+popSizes[j]];
					for (int k=0; k<popSizes[i]; k++){
						pairLabels[k]=0;
					}
					for (int k=0; k<popSizes[j]; k++){
						pairLabels[k+popSizes[i]]=1;
					}
					
					for (int k=0; k<permutationRepeats; k++){
						pd=shuffleScores(distByPopulation, i, j);
						permutedMeans=calculateGroupMeans(pd, pairLabels);
						if (permutedMeans[0]/permutedMeans[1]>groupMeans[i]/groupMeans[j]){
							pairwiseTable[i][j]++;
						}
					}
				}
			}
		}
		
				
		for (int i=0; i<nPops; i++){
			for (int j=0; j<nPops; j++){
				pairwiseTable[i][j]/=permutationRepeats+0.0;
			}
		}
		return pairwiseTable;
	}
	
	
	public double[] calculateGroupMeans(double[] scores, int[] labels){
		
		int nq=scores.length;
		
		double[] results=new double[nPops*2];
		double[] counts=new double[nPops*2];
		
		
		for (int i=0; i<nq; i++){
			results[labels[i]]+=scores[i];
			counts[labels[i]]++;
		}
		
		for (int i=0; i<nPops; i++){
			results[i]/=counts[i];
		}
		
		for (int i=0; i<nq; i++){
			results[nPops+labels[i]]+=(scores[i]-results[labels[i]])*(scores[i]-results[labels[i]]);
		}
		
		for (int i=0; i<nPops; i++){
			results[i+nPops]/=counts[i]-1.0;
			results[i+nPops]=Math.sqrt(results[i+nPops]);
		}

		return results;
	}
	
	public int[] countPopulationSampleSize(int[] labels){
		int[] results=new int[nPops];
		for (int i=0; i<labels.length; i++){
			results[labels[i]]++;
		}
		return results;
	}
	
	public double[][] byPopulation(double[] scores, int[] labels, int[] popSize){
		double[][] results=new double[nPops][];
		
		for (int i=0; i<nPops; i++){
			results[i]=new double[popSize[i]];
			int c=0;
			for (int j=0; j<labels.length; j++){
				if (labels[j]==i){
					results[i][c]=scores[j];
					c++;
				}
			}
		}
		return results;
	}
	
	public double[] compressToIndividual(double[]r){
		double[] results=new double[nOverallInds];
		
		double[] counts=new double[nOverallInds];
		
		for (int i=0; i<n; i++){
			results[indLabels[i]]+=r[i];
			System.out.println(i+" "+r[i]+" "+indLabels[i]);
			counts[indLabels[i]]++;
		}
		
		for (int i=0; i<nOverallInds; i++){
			results[i]/=counts[i];
			System.out.println(i+" "+results[i]);
		}
		
		return results;
	}
	
	public int[] compressLabelToIndividuals2(int[] labels){
		
		int[] results=new int[nOverallInds];
		nPops-=5;
		for (int i=0; i<n; i++){
			if (labels[i]<3){
				results[indLabels[i]]=0;
			}
			else if(labels[i]<7){
				results[indLabels[i]]=1;
			}
			else{
				results[indLabels[i]]=labels[i]-5;
			}
			//results[indLabels[i]]=labels[i];
			//System.out.println(labels[i]);
		}
		return results;
	}
	
	public int[] compressLabelToIndividuals(int[] labels){
		
		int[] results=new int[nOverallInds];
		for (int i=0; i<n; i++){
			results[indLabels[i]]=labels[i];
		}
		return results;
	}
		
		
	
	public double[] shuffleScores(double[][] scores, int label1, int label2){
		
		int nq=scores[label1].length+scores[label2].length;
		
		double[] results=new double[nq];
		
		System.arraycopy(scores[label1], 0, results, 0, scores[label1].length);
		System.arraycopy(scores[label2], 0, results, scores[label1].length, scores[label2].length);
		
		for (int i=0; i<nq; i++){
			int np=nq-i;
			int p=random.nextInt(np);
			p+=i;
			
			double x=results[i];
			results[i]=results[p];
			results[p]=x;
		}
		return results;
	}
	
	public double[] calculateDistToMean(){
		double[] results=new double[n];
		
		double[][] meanD=new double[nPops][m];
		double[] count=new double[nPops];
		
		for (int i=0; i<n; i++){
			for (int j=0; j<m; j++){
				meanD[popLabels[i]][j]+=data[i][j];
			}
			count[popLabels[i]]++;
		}
		
		for (int i=0; i<nPops; i++){
			for (int j=0; j<m; j++){
				meanD[i][j]/=count[i];
				System.out.println("MEANS: "+meanD[i][j]);
			}
			
		}
		
		
		for (int i=0; i<n; i++){
			
			for (int j=0; j<m; j++){
				double score=meanD[popLabels[i]][j]-data[i][j];
				results[i]+=score*score;
			}
			results[i]=Math.sqrt(results[i]);
			System.out.println(i+" "+popLabels[i]+" "+indLabels[i]+" "+data[i][0]+" "+data[i][1]+" "+data[i][2]+" "+data[i][3]+" "+data[i][4]+" "+data[i][5]+" "+results[i]);
		}
				
		return results;
	}
	
	public double[] calculateDistToMeanPos(double[] sd){
		double[] results=new double[n];
		
		double[][][] meanD=new double[nPops][m][];
		double[][] count=new double[nPops][];
		
		for (int i=0; i<nPops; i++){
			for (int j=0; j<m; j++){
				meanD[i][j]=new double[nPos];
			}
			count[i]=new double[nPos];
		}
		
		
		for (int i=0; i<n; i++){
			for (int j=0; j<m; j++){
				meanD[popLabels[i]][j][posLabels[i]]+=data[i][j]/sd[popLabels[i]];
			}
			count[popLabels[i]][posLabels[i]]++;
		}
		
		for (int i=0; i<nPops; i++){
			for (int j=0; j<m; j++){
				for (int k=0; k<nPos; k++){
					meanD[i][j][k]/=count[i][k];
				}
			}
		}
		
		
		for (int i=0; i<n; i++){
			for (int j=0; j<m; j++){
				double score=meanD[popLabels[i]][j][posLabels[i]]-(data[i][j]/sd[popLabels[i]]);
				results[i]+=score*score;
			}
			results[i]=Math.sqrt(results[i]);			
		}
		
		return results;
	}
	
	public double[] calculateDistToMedianAligned(double[] sd){
		
		
		
		double[] results=new double[n];
		
		int[][] alignments=alignments();
		
		
		for (int i=0; i<n; i++){
			
			
			int loc=-1;
			float best=1000000f;
			
			for (int j=1; j<alignments[i].length; j++){
				if (indLabels[alignments[i][0]]!=indLabels[alignments[i][j]]){
					//System.out.print(alignments[i][j]+" ");
					float tot=0;
					for (int k=1; k<alignments[i].length; k++){
						if (indLabels[alignments[i][0]]!=indLabels[alignments[i][k]]){
							if (alignments[i][j]>alignments[i][k]){
								tot+=data[alignments[i][j]][alignments[i][k]];
							}
							else{
								tot+=data[alignments[i][k]][alignments[i][j]];
							}
						}
					}
					if (tot<best){
						best=tot;
						loc=j;
					}
				}
			}
			//System.out.println();
			loc=alignments[i][loc];
			if (i>loc){
				results[i]=data[i][loc]/sd[popLabels[i]];
			}
			else{
				results[i]=data[loc][i]/sd[popLabels[i]];
			}
			//System.out.println(loc+" "+results[i]);
		}
		return results;
	}

	
	
	public int[][] alignments(){
		
		int songCount=syllID[n-1][0]+1;
		
		int[] songsByPop=new int[nPops];
		
		for (int i=0; i<n; i++){
			if ((i==0)||(syllID[i][0]!=syllID[i-1][0])){
				songsByPop[popLabels[i]]++;
			}
		}
		
		
		int[] syllNum=new int[songCount];
		
		for (int i=0; i<n; i++){
			if (syllID[i][1]>syllNum[syllID[i][0]]){
				syllNum[syllID[i][0]]=syllID[i][1];
			}
		}
		
		for (int i=0; i<songCount; i++){
			syllNum[i]++;
		}
		
		int[][] syllBySong=new int[songCount][];
		for (int i=0; i<songCount; i++){
			syllBySong[i]=new int[syllNum[i]];
		}
		
		for (int i=0; i<n; i++){
			syllBySong[syllID[i][0]][syllID[i][1]]=i;
		}
		
		
		
		int[] counter=new int[n];
		
		int[][] results=new int[n][];
		for (int i=0; i<n; i++){
			results[i]=new int[songsByPop[popLabels[i]]];
			results[i][0]=i;
			counter[i]++;
		}
		
		
		for (int i=0; i<songCount; i++){
			
			
			for (int j=0; j<i; j++){
				if (popLabels[syllBySong[i][0]]==popLabels[syllBySong[j][0]]){
					
					
					
					float[][] songPair=new float[syllNum[i]][syllNum[j]];
					
										
					
					for (int a=0; a<syllNum[i]; a++){
						for (int b=0; b<syllNum[j]; b++){
							//if (syllBySong[i][a]>syllBySong[j][b]){
							songPair[a][b]=data[syllBySong[i][a]][syllBySong[j][b]];
							//}
							//else{
							//	songPair[a][b]=data[syllBySong[j][b]][syllBySong[i][a]];
							//}
							//I DON'T THINK THIS SECOND CASE SHOULD EVER OCCUR...
						}
					}
					
					
					int[][] aligned=alignSongPair(songPair);
					//System.out.print(i+" ");
					for (int a=0; a<syllNum[i]; a++){
						
						int p=syllBySong[i][a];
						int q=syllBySong[j][aligned[0][a]];
						results[p][counter[p]]=q;
						//System.out.print(p+","+q+" ");
						counter[p]++;
					}
					//System.out.println();
					//System.out.print(j+" ");
					for (int a=0; a<syllNum[j]; a++){
						
						int p=syllBySong[j][a];
						int q=syllBySong[i][aligned[1][a]];
						results[p][counter[p]]=q;
						//System.out.print(p+","+q+" ");
						counter[p]++;
					}
					//System.out.println();
				}
			}
		}
	
		
		return results;
		
	}
		
	
	public int[][] alignSongPair(float[][] songPair){
		
		int n=songPair.length;
		int m=songPair[0].length;
		
		float[][] st=new float[n][m];
		st[0][0]=songPair[0][0];
		
		int[] locsX={0, -1, -1};
		int[] locsY={-1, 0, -1};
		int x, y, z, locx, locy, i, j, k;
		float min;
		
		for (i=0; i<n; i++){
			for (j=0; j<m; j++){
				min=1000000f;
				locx=0;
				locy=0;
				z=-1;
				for (k=0; k<3; k++){
					x=i+locsX[k];
					y=j+locsY[k];
					
					if ((x>=0)&&(y>=0)){
						if (st[x][y]<min){
							min=st[x][y];
							locx=x;
							locy=y;
							z=k;
						}
					}
				}
				if (z>=0){
					st[i][j]=min+songPair[i][j];
				}
			}
		}
		
		int[][] trail=new int[n][m];
		
		int g=n-1;
		int h=m-1;
		trail[g][h]=1;
		while ((g>0)||(h>0)){
			min=1000000f;
			locx=0;
			locy=0;
			z=-1;
			for (k=0; k<3; k++){
				x=g+locsX[k];
				y=h+locsY[k];
				
				if ((x>=0)&&(y>=0)){
					if (st[x][y]<min){
						min=st[x][y];
						locx=x;
						locy=y;
						z=k;
					}
				}
			}
			if (z>=0){
				g=locx;
				h=locy;
				trail[g][h]=1;
			}
		}
			
		
		int[][] results=new int[2][];
		results[0]=new int[n];
		results[1]=new int[m];
		
		
		for (i=0; i<n; i++){
			int loc=-1;
			min=1000000f;
			for (j=0; j<m; j++){
				if ((trail[i][j]==1)&&(min>songPair[i][j])){
					min=songPair[i][j];
					loc=j;
				}
			}
			results[0][i]=loc;
		}
		for (i=0; i<m; i++){
			int loc=-1;
			min=1000000f;
			for (j=0; j<n; j++){
				if ((trail[j][i]==1)&&(min>songPair[j][i])){
					min=songPair[j][i];
					loc=j;
				}
			}
			results[1][i]=loc;
		}
		return results;
	}
		
		
		
		
	
}
