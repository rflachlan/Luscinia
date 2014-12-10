package lusc.net.github.analysis;
//
//  VarianceClusterAnalysis.java
//  Luscinia
//
//  Created by Robert Lachlan on 10/20/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

import java.util.*;

import lusc.net.github.analysis.clustering.KMeans;
import lusc.net.github.analysis.dendrograms.TreeDat;
import lusc.net.github.analysis.dendrograms.UPGMA;


public class VarianceClusterAnalysis {
	
	int numK=20;
	int permutationRepeats=100000;
	int bootstrapRepeats=100000;
	Random random=new Random(System.currentTimeMillis());
	
	
	public VarianceClusterAnalysis (float[][] data, float[][] vec, SongGroup sg, int unitType){
		
		int n=data.length;
		int[] syllPopLabels=sg.getPopulationListArray(unitType);
		int[] individualLabels=sg.getIndividualListArray(unitType);		
				
		int numPops=sg.populations.length;
		
		double[][] distToCentroid=new double[n][numK+1];
	
		float[][][][] centroids=new float[numPops][numK+1][][];
		
		for (int i=0; i<numPops; i++){
			
			float[][] subdata=createSubMatrix(data, i, syllPopLabels);
			
			float[][] subdata2=createSubMatrix2(vec, i, syllPopLabels);
			
			UPGMA upgma=new UPGMA(subdata, 1);
			
			int[][] partitions=upgma.getPartitionMembers();
			
			double[][] prototypes=new double[numK+1][];			
			int[][] catLabels=new int[numK+1][];
			
			
			
			prototypes[0]=calculateDistanceToMean(subdata2);
			
			
			centroids[i][0]=new float[1][1];
			
			
			
			int[] elementLabels=new int[subdata2.length];
			for (int a=1; a<numK+1; a++){
				
				int ii=partitions.length-a-1;
				
				if (partitions[ii]!=null){
					
					for (int j=0; j<partitions[ii].length; j++){
						TreeDat[] dat=upgma.getDat();
						for (int k=0; k<dat[partitions[ii][j]].child.length; k++){
							elementLabels[dat[partitions[ii][j]].child[k]]=j;
						}
					}
					
					KMeans km=new KMeans(subdata2, elementLabels);
					
					catLabels[a]=km.getAssignments();
					
					prototypes[a]=km.calculatePrototypeDistances(subdata2);
					
					centroids[i][a]=km.getCentroids();
					
				}
			}
			
			
			calculateDistanceToCentroids(prototypes, i, syllPopLabels, distToCentroid);			
			
		}
		
		double[][] compressedCentroidDist=new double[numK+1][];
		double[] sc=new double[n];
		for (int i=0; i<numK+1; i++){
			for (int j=0; j<n; j++){
				sc[j]=distToCentroid[j][i];
			}
			compressedCentroidDist[i]=compressDistanceScoresWithinIndividuals(sc, individualLabels);
		}
		int[] compressedPopLabels=compressPopulationLabelsWithinIndividuals(syllPopLabels, individualLabels);
		
		for (int i=0; i<numK+1; i++){
			double[] avsc=new double[numPops];
			double[] cosc=new double[numPops];
			
			for (int j=0; j<compressedPopLabels.length; j++){
				avsc[compressedPopLabels[j]]+=compressedCentroidDist[i][j];
				cosc[compressedPopLabels[j]]++;
			}
			System.out.print(i+" ");
			for (int j=0; j<numPops; j++){
				avsc[j]/=cosc[j];
				System.out.print(avsc[j]+" ");
			}
			
			double[] sdsc=new double[numPops];
			for (int j=0; j<compressedPopLabels.length; j++){
				sdsc[compressedPopLabels[j]]+=(compressedCentroidDist[i][j]-avsc[compressedPopLabels[j]])*(compressedCentroidDist[i][j]-avsc[compressedPopLabels[j]]);
			}
			for (int j=0; j<numPops; j++){
				sdsc[j]/=cosc[j]-1;
				System.out.print(Math.sqrt(sdsc[j])+" ");
			}
			System.out.println();
		}
		
		double[][][] populationComparisons=new double[numPops][][];
		for (int i=0; i<numPops; i++){
			populationComparisons[i]=new double[i+1][numK+1];
			
			for (int j=0; j<i; j++){
				for (int k=1; k<numK+1; k++){
					populationComparisons[i][j][k]=permutationTest(compressedCentroidDist[k], compressedPopLabels, i, j);
					System.out.println(i+" "+j+" "+k+" "+populationComparisons[i][j][k]);
				}
			}
		}
		
		
		
		for (int i=1; i<numK+1; i++){
			
			double[][] silscore=new double[numPops][numPops];
			double[][] count=new double[numPops][numPops];
			for (int j=0; j<n; j++){

				
				for (int a=0; a<numPops; a++){
					double bestPop=1000000000;
					for (int b=0; b<centroids[a][i].length; b++){
						double q=calculateDistance(vec[j], centroids[a][i][b]);
						if (q<bestPop){
							bestPop=q;
						}
					}
					
					
					silscore[syllPopLabels[j]][a]=bestPop;
					count[syllPopLabels[j]][a]++;
				}
			}
			
			for (int j=0; j<numPops; j++){
				for (int k=0; k<numPops; k++){
					silscore[j][k]/=count[j][k];
					System.out.println(i+" "+j+" "+k+" "+silscore[j][k]);
				}
			}
		}
		
		
		/*
		 
		 
		 for (int i=0; i<compressedPopLabels.length; i++){
		 if (compressedPopLabels[i]<3){compressedPopLabels[i]=0;}
		 else if(compressedPopLabels[i]<7){compressedPopLabels[i]=1;}
		 else{compressedPopLabels[i]-=5;}
		 }
		 
		 numPops-=5;
		 
		 for (int i=2; i<numK+1; i++){
		 double[] avsc=new double[numPops];
		 double[] cosc=new double[numPops];
		 
		 for (int j=0; j<compressedPopLabels.length; j++){
		 avsc[compressedPopLabels[j]]+=compressedCentroidDist[i][j];
		 cosc[compressedPopLabels[j]]++;
		 }
		 System.out.print(i+" ");
		 for (int j=0; j<numPops; j++){
		 avsc[j]/=cosc[j];
		 System.out.print(avsc[j]+" ");
		 }
		 
		 double[] sdsc=new double[numPops];
		 for (int j=0; j<compressedPopLabels.length; j++){
		 sdsc[compressedPopLabels[j]]+=(compressedCentroidDist[i][j]-avsc[compressedPopLabels[j]])*(compressedCentroidDist[i][j]-avsc[compressedPopLabels[j]]);
		 }
		 System.out.println();
		 for (int j=0; j<numPops; j++){
		 sdsc[j]/=cosc[j]-1;
		 System.out.print(Math.sqrt(sdsc[j])+" ");
		 }
		 System.out.println();
		 }
		 
		 double[][][] populationComparisons2=new double[numPops][][];
		 for (int i=0; i<numPops; i++){
		 populationComparisons2[i]=new double[i+1][numK+1];
		 
		 for (int j=0; j<i; j++){
		 for (int k=2; k<numK+1; k++){
		 populationComparisons2[i][j][k]=permutationTest(compressedCentroidDist[k], compressedPopLabels, i, j);
		 System.out.println(i+" "+j+" "+k+" "+populationComparisons2[i][j][k]);
		 }
		 }
		 }
		 */
		
		
		UPGMA upgma=new UPGMA(data, 1);
		int[][] partitions=upgma.getPartitionMembers();
		int[] elementLabels=new int[vec.length];
		
		for (int a=1; a<numK+1; a++){
			int ii=partitions.length-a-1;
			if (partitions[ii]!=null){
				for (int j=0; j<partitions[ii].length; j++){
					TreeDat[] dat=upgma.getDat();
					for (int k=0; k<dat[partitions[ii][j]].child.length; k++){
						elementLabels[dat[partitions[ii][j]].child[k]]=j;
					}
				}
				
				KMeans km=new KMeans(vec, elementLabels);
				
				double[][] results=analyzeClusterComposition(km.getAssignments(), syllPopLabels, a+1, numPops);
				
				System.out.println("Composition results, k="+(a+1));
				
				for (int t=0; t<results.length; t++){
					for (int u=0; u<results[t].length; u++){
						System.out.print(results[t][u]+" ");
					}
					System.out.println();
				}
			}
		}
		
		
		
		
	}
	
	
	
	public double[][] analyzeClusterComposition(int[] clusterLabels, int[] popLabels, int k, int p){
	
		
		int n=clusterLabels.length;
		double [][] actualProportions=new double[k][3*p];
		
		double [] populationProportions=new double[p];
		int[] clusterCounts=new int[k];
		
		
		for (int i=0; i<n; i++){
			actualProportions[clusterLabels[i]][popLabels[i]]++;
			populationProportions[popLabels[i]]++;
			clusterCounts[clusterLabels[i]]++;
		}		
		
		for (int i=0; i<k; i++){
			for (int j=0; j<bootstrapRepeats; j++){
				int [] simCount=new int[p];
				for (int a=0; a<clusterCounts[i]; a++){
					int c=random.nextInt(n);
					int d=0;
					for (int b=0; b<p; b++){
						d+=populationProportions[b];
						if (d>c){
							simCount[b]++;
							b=p;
						}
					}
				}
				
				for (int a=0; a<p; a++){
					if (simCount[a]<actualProportions[i][a]){
						actualProportions[i][p+a]++;
					}
					if (simCount[a]==0){
						actualProportions[i][2*p+a]++;
					}
				}
				
			}
			
			for (int j=0; j<p; j++){
				actualProportions[i][p+j]/=bootstrapRepeats+0.0;
			}
			
		}
		
		return actualProportions;
		
		
	}
	
	
	
	
	public double calculateDistance(float[] d1, float[] d2){
		
		int n=d1.length;
		
		double d=0;
		for (int i=0; i<n; i++){
			d+=(d1[i]-d2[i])*(d1[i]-d2[i]);
		}
		
		d=Math.sqrt(d);
		return d;
	}
			
			
		
	
	
	public double[] calculatePositionLabels(int[][] tlabs, int[][] slabs){
		
		int m=tlabs.length;
		
		double[] r=new double[m];
		
		for (int i=0; i<m; i++){
			
			if (tlabs[i][0]==-1){
				r[i]=0;
			}
			else if (tlabs[i][1]==-1){
				r[i]=1;
			}
			else{
				int p=tlabs[i][0];
				int q=slabs[p][1];
				int k=p;
				while ((k<slabs.length)&&(slabs[k][0]==slabs[p][0])){
					k++;
				}
				k--;
				r[i]=(q+1.0)/(slabs[k][1]+1.0);
			}
			//System.out.println(r[i]);
		}
		return r;
	}

	public float[][] createSubMatrix2(float[][] mainMatrix, int population, int[] labels){
		
		int n=labels.length;
		System.out.println(n+" "+mainMatrix.length);
		int c=0;
		for (int i=0; i<n; i++){
			if (labels[i]==population){
				c++;
			}
		}
		
		int d=mainMatrix[0].length;
		
		float[][] subMatrix=new float[c][d];
		
		int a=0;
		for (int i=0; i<n; i++){
			if (labels[i]==population){
				for (int j=0; j<d; j++){
					subMatrix[a][j]=mainMatrix[i][j];
				}
				a++;
			}
		}
		return subMatrix;
	}
	
	
	public float[][] createSubMatrix(float[][] mainMatrix, int population, int[] labels){
		
		int n=labels.length;
		System.out.println(n+" "+mainMatrix.length);
		int c=0;
		for (int i=0; i<n; i++){
			if (labels[i]==population){
				c++;
			}
		}
		
		float[][] subMatrix=new float[c][];
		for (int i=0; i<c; i++){
			subMatrix[i]=new float[i+1];
		}
		
		int a=0;
		int b=0;
		for (int i=0; i<n; i++){
			if (labels[i]==population){
				b=0;
				for (int j=0; j<i; j++){
					if (labels[j]==population){
						subMatrix[a][b]=mainMatrix[i][j];
						b++;
					}
				}
				a++;
			}
		}
		return subMatrix;
	}
	
	public double[] calculateDistanceToMean(float[][] vec){
		
		int n=vec.length;
		int m=vec[0].length;
		
		double[] means=new double[m];
		
		for (int i=0; i<n; i++){
			for (int j=0; j<m; j++){
				means[j]+=vec[i][j];
			}
		}
		for (int i=0; i<m; i++){
			means[i]/=n+0.0;
			//System.out.println("MEANS: "+means[i]);

		}
		
		double[] results=new double[n];
		
		for (int i=0; i<n; i++){
			for (int j=0; j<m; j++){
				results[i]+=(vec[i][j]-means[j])*(vec[i][j]-means[j]);
			}
			results[i]=Math.sqrt(results[i]);
			//System.out.println(i+" "+results[i]);
		}	
		return results;
	}
	
	
	public void calculateDistanceToCentroids(double[][] prototypeDistance, int population, int[] popLabels, double[][] mainArray){
		
		int n=popLabels.length;
		int c=0;
		int m=prototypeDistance.length;
		for (int i=0; i<n; i++){
			if (popLabels[i]==population){
				for (int j=0; j<m; j++){
					mainArray[i][j]=prototypeDistance[j][c];
				}
				c++;
			}
		}
	}
	
	public int[] compressPopulationLabelsWithinIndividuals(int[] popLabels, int[] individualLabels){
		int n=popLabels.length;
		
		int numInds=0;
		for (int i=0; i<n; i++){
			if (individualLabels[i]>numInds){
				numInds=individualLabels[i];
			}
		}
		numInds++;
		System.out.println("Number of individuls: "+numInds);
		int[] results=new int[numInds];
		
		for (int i=0; i<n; i++){
			results[individualLabels[i]]=popLabels[i];
		}
		
		return results;
	}
	
	public double[] compressDistanceScoresWithinIndividuals(double[] oscores, int[] individualLabels){
		
		int n=oscores.length;
		
		int numInds=0;
		for (int i=0; i<n; i++){
			if (individualLabels[i]>numInds){
				numInds=individualLabels[i];
			}
		}
		numInds++;
		
		double[] results=new double[numInds];
		double[] indCount=new double[numInds];
		
		for (int i=0; i<n; i++){
			results[individualLabels[i]]+=oscores[i];
			indCount[individualLabels[i]]++;
		}
		
		for (int i=0; i<numInds; i++){
			results[i]/=indCount[i];
		}
		
		return results;
	}
	
	public double permutationTest(double[] scores, int[] indScores, int label1, int label2){
		
		int n=indScores.length;
		int t=0;
		for (int i=0; i<n; i++){
			if ((indScores[i]==label1)||(indScores[i]==label2)){
				t++;
			}
		}
		
		int[] labels=new int[t];
		t=0;
		for (int i=0; i<n; i++){
			if (indScores[i]==label1){
				labels[t]=i;
				//System.out.println(labels[t]+" 1 "+scores[i]);
				t++;
			}
		}
		int l1=t;
		for (int i=0; i<n; i++){
			if (indScores[i]==label2){
				labels[t]=i;
				//System.out.println(labels[t]+" 2 "+scores[i]);
				t++;
			}
		}
		//System.out.println(l1);
		
		
		double score=calculatePermutationScore(scores, labels, l1);
		System.out.println("Main score: "+score+" "+scores.length+" "+labels.length+" "+l1);
		double c=0;
		for (int i=0; i<permutationRepeats; i++){
			int[] plabels=permuteLabels(labels);
			double pscore=calculatePermutationScore(scores, plabels, l1);
			//System.out.println(score+" "+pscore);
			if (pscore>score){
				c++;
			}
		}
		
		c/=permutationRepeats+0.0;
		
		return c;
	}
	
	public double calculatePermutationScore(double[] scores, int[] labels, int s){
		int n=labels.length;
		double tot1=0;
		double tot2=0;
		//System.out.println(scores.length+" "+labels.length+" "+s);
		for (int i=0; i<s; i++){
			tot1+=scores[labels[i]];
		}
		for (int i=s; i<n; i++){
			tot2+=scores[labels[i]];
		}
		tot1/=s+0.0;
		tot2/=n-s+0.0;
		
		double r=tot1-tot2;
		return r;
	}
	
	public int[] permuteLabels(int[] originalLabels){
		int n=originalLabels.length;
		int[] permutedLabels=new int [n];
		System.arraycopy(originalLabels,0, permutedLabels, 0, n);
		for (int i=0; i<n; i++){
			int p=random.nextInt(n-i);
			p+=i;
			int q=permutedLabels[i];
			permutedLabels[i]=permutedLabels[p];
			permutedLabels[p]=q;
		}
		return permutedLabels;
	}
	
}
