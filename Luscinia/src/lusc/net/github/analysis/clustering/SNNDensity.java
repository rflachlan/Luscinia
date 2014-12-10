package lusc.net.github.analysis.clustering;
//
//  SNNDensity.java
//  Luscinia
//
//  Created by Robert Lachlan on 8/3/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

import java.util.*;

import lusc.net.github.analysis.multivariate.MRPP;

public class SNNDensity {

	int[][] SNNSimilarity;
	int[] SNNDensityLabels;
	int[] SNNDensity;
	boolean[] corePoints;
	int[] DBSCANclusters;
	double[][][] clusters;
	
	int numClusts=0;
	int type=0;
	double MRPPresults[];

	public SNNDensity(float[][] data, int k, int Eps, int MinPts, int type){
		this.type=type;
				
		int[][] knearest=getKNearest(data, k);
		SNNSimilarity=getSNN(knearest, k);
		
		SNNDensity=getSNNDensity(SNNSimilarity, Eps);
		//corePoints=getCorePoints(SNNDensity, MinPts);
		//int[] cats=clusterCorePoints(corePoints, SNNSimilarity, Eps);
		//DBSCANclusters=assignBorderPoints(cats, SNNSimilarity, corePoints, Eps);
		
		SNNDensityLabels=getDensityLabels(SNNSimilarity, Eps, MinPts);
		boolean[][] edges=linkCorePoints(SNNDensityLabels, SNNSimilarity, Eps);
		DBSCANclusters=calculateClusters(edges, SNNDensityLabels, SNNSimilarity);
		numClusts=getNumberOfClusters();
		//fudgeClusters(numClusts);
	}
	
	public int getType(){
		return type;
	}
	
	public int[] getDBSCANClusters(){
		return DBSCANclusters;
	}
	
	public int getNumClusts(){
		return numClusts;
	}
	
	public int getNumberOfClusters(){
		int maxN=0;
		for (int i=0; i<DBSCANclusters.length; i++){
			if (DBSCANclusters[i]>maxN){
				maxN=DBSCANclusters[i];
			}
		}
		return maxN;
		
	}
	
	public void fudgeClusters(int maxN){	
		
		clusters=new double[maxN+1][DBSCANclusters.length][maxN];
		
		for (int i=0; i<DBSCANclusters.length; i++){
			if (DBSCANclusters[i]>0){
				clusters[maxN][i][DBSCANclusters[i]-1]=1;
			}
		}
	}
		
		
		
		
	
	public void recastSimilaritiesAsDistances(float[][] originalData){
		int n=SNNSimilarity.length;
		
		float[][] results=new float[n][];
		for (int i=0; i<n; i++){
			results[i]=new float[i+1];
		}
	
	
		int max=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<=i; j++){
				if (SNNSimilarity[i][j]>max){
					max=SNNSimilarity[i][j];
				}
			}
		}
		float maxf=max;
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				results[i][j]=(float)((max-SNNSimilarity[i][j])/maxf);
				originalData[i][j]+=50*results[i][j];
			}
		}
		//return results;
	}
	
	public int[] calculateClusters(boolean[][] edges, int[] labels, int[][] dists){
	
		int n=edges.length;
		
		int[] results=new int[n];
		boolean[] clustered=new boolean[n];
		for (int i=0; i<n; i++){
			clustered[i]=false;
			results[i]=-1;			//this is the cluster label for noise points
		}
		
		int labelID=0;
		for (int i=0; i<n; i++){
			if ((labels[i]==2)&&(!clustered[i])){
				results[i]=labelID;
				clustered[i]=true;
				
				for (int j=0; j<n; j++){
					if (labels[j]==2){
						if (edges[i][j]){
							results[j]=labelID;
							clustered[j]=true;
						}
					}
				}
				labelID++;
			}
		}
		
		
		for (int i=0; i<n; i++){
			if (labels[i]==1){
				int maxSim=0;
				int loc=0;
				for (int j=0; j<n; j++){
					if (labels[j]==2){
						if (dists[i][j]>maxSim){
							maxSim=dists[i][j];
							loc=j;
						}
					}
				}
				results[i]=results[loc];
			}
			//System.out.println(i+" "+results[i]);
		}
		
		for (int i=0; i<n; i++){
			if (results[i]==-1){
				results[i]=labelID;
			}
		}
		
		return results;
	}
	
	public boolean[][] linkCorePoints(int[] labels, int[][]similarity, int Eps){
		
		int n=labels.length;
		boolean[][] results=new boolean [n][n];
		for (int i=0; i<n; i++){
			for (int j=0; j<n; j++){
				results[i][j]=false;
			}
		}
		
		
		for (int i=0; i<n; i++){
			if (labels[i]==2){
				for (int j=0; j<n; j++){
					if (labels[i]==2){
						
						if (similarity[i][j]>=Eps){
							results[i][j]=true;
							results[j][i]=true;
							
							for (int a=0; a<n; a++){
								if (results[i][a]){
									results[a][j]=true;
									results[j][a]=true;
								}
								if (results[j][a]){
									results[a][i]=true;
									results[i][a]=true;
								}
							}
						}
					}
				}
			}
		}
		return results;
	
	}
	
	public int[] getDensityLabels(int[][] data, int eps, int minpts){
	
		int n=data.length;
		int[] results=new int[n];
		
		for (int i=0; i<n; i++){
			int count=0;
			for (int j=0; j<n; j++){
				
				if (data[i][j]>=eps){
					count++;
				}
			}
			//System.out.print(count+" ");
			if (count>minpts){
				results[i]=2;
				for (int j=0; j<n; j++){
					if (results[j]!=2){
						
						if (data[i][j]>=eps){
							results[j]=1;
						}
					}
				}
			}
			//System.out.println(results[i]);
		}
		return results;		
	}
	
	public int[] getSNNDensity(int[][] data, int eps){
		
		int n=data.length;
		int[] results=new int[n];
		
		for (int i=0; i<n; i++){
			int count=0;
			for (int j=0; j<n; j++){
				if (data[i][j]>=eps){
					count++;
				}
			}
			results[i]=count;
			System.out.println("density: "+results[i]);
		}
		return results;		
	}
	
	public boolean[] getCorePoints(int[] density, int MinPts){
		
		int n=density.length;
		boolean[] results=new boolean[n];
		for (int i=0; i<n; i++){
			if (density[i]>MinPts){
				results[i]=true;
			}
			else{
				results[i]=false;
			}
			System.out.println("is core: "+results[i]);
		}
		return results;
	}
	
	public int[] clusterCorePoints(boolean[] corePts, int[][] snn, int eps){
		
		int n=corePts.length;
		
		int m=1;
		
		int[] categories=new int[n];
		int[] cat=new int[n+1];
		
		for (int i=0; i<n; i++){
			if (corePts[i]){
				boolean foundCat=false;
				for (int j=0; j<i; j++){
					if (corePts[j]){
						if (snn[i][j]>=eps){
							foundCat=true;
							if (categories[i]==0){
								if (categories[j]==0){
									categories[i]=m;
									categories[j]=m;
									cat[m]=1;
									m++;
								}
								else{
									categories[i]=categories[j];
								}
							}
							else if (categories[j]==0){
								categories[j]=categories[i];
							}
							else{
								int t=categories[i];
								int u=categories[j];
								for (int k=0; k<n; k++){	
									if (categories[k]==t){
										categories[k]=u;
									}
								}
								cat[t]=0;
							}
						}
					}
				}
				if (!foundCat){
					categories[i]=m;
					cat[m]=1;
					m++;
				}
			}
		}
		
		int[] categories2=new int[n];
		
		int a=1;
		for (int i=0; i<n+1; i++){
			if (cat[i]==1){
				for (int j=0; j<n; j++){
					if (categories[j]==i){
						categories2[j]=a;
					}
					//System.out.println("Category: "+a+" member: "+j);
				}
				a++;
			}
		}
		
		return (categories2);
	}
	
	public int[] assignBorderPoints(int[] categories, int[][] snn, boolean[] corePts, int eps){
		
		int n=categories.length;
		int[] cats=new int[n];
		System.arraycopy(categories, 0, cats, 0, n);
		for (int i=0; i<n; i++){
			if (!corePts[i]){
				int max=-1;
				int loc=-1;
				for (int j=0; j<n; j++){
					if ((corePts[j])&&(snn[i][j]>max)){
						max=snn[i][j];
						loc=j;
					}
				}
				if (max>=eps){
					cats[i]=cats[loc];
				}
				else{cats[i]=0;}
			}
		}
		return cats;
	}
	
	public int[][] getSNN(int[][] knearest, int k){
		
		int n=knearest.length;
		int[][] results=new int[n][n];
		
		for (int i=0; i<n; i++){
			for (int j=0; j<n; j++){
				boolean foundI=false;
				boolean foundJ=false;
				for (int a=0; a<k; a++){
					if (knearest[i][a]==j){
						foundJ=true;
					}
					if (knearest[j][a]==i){
						foundI=true;
					}
				}
				
				if ((foundI)&&(foundJ)){
					int count=0;
					for (int a=0; a<k; a++){
						int p=knearest[i][a];
						for (int b=0; b<k; b++){
							if (knearest[j][b]==p){
								count++;
								b=k;
							}
						}
					}
					results[i][j]=count;
				}
				else{
					results[i][j]=0;				
				}
				//System.out.print(results[i][j]+" ");
			}
			//System.out.println();
		}
		return results;
	}
	
	
	public int[][] getKNearest(float[][] data, int k){
		int n=data.length;
		int[][] knearest=new int[n][k];
		
		float[] holder1=new float[n];
		float[] holder2=new float[n];
		for (int i=0; i<n; i++){
			for (int j=0; j<n; j++){
				int ii=i;
				int jj=j;
				if (ii<jj){
					jj=i;
					ii=j;
				}
				holder1[j]=data[ii][jj];
				if (ii==jj){holder1[j]=100000;}
				holder2[j]=holder1[j];
			}
			Arrays.sort(holder1);
			for (int j=0; j<k; j++){	
				for (int a=0; a<n; a++){
					if (holder1[j]==holder2[a]){
						knearest[i][j]=a;
						holder2[a]=-1;
					}
				}
				//System.out.print(knearest[i][j]+" ");
			}
			//System.out.println();
		}
		holder1=null;
		holder2=null;
		return knearest;
	
	}
	
	public void runMRPP(int[] ids, float[][] tmat){
		MRPPresults=new double[numClusts];
		for (int j=0; j<numClusts; j++){
			int[] t=new int[ids.length];
			System.arraycopy(ids, 0, t, 0, ids.length);
			for (int k=0; k<ids.length; k++){
				if (DBSCANclusters[k]!=j){
					t[k]=-1;
				}
			}
			MRPP mrp1=new MRPP(tmat, t);
				
			MRPPresults[j]=mrp1.getPValue();
		}
			
		int[] t=new int[ids.length];
		System.arraycopy(ids, 0, t, 0, ids.length);
		System.out.println("Overall MRPP, k="+numClusts);
		MRPP mrp2=new MRPP(tmat, t,DBSCANclusters);
	}

}
