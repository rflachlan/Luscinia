package lusc.net.github.analysis.multivariate;
//
//  MRPP.java
//  Luscinia
//
//  Created by Robert Lachlan on 2/2/07.
//  Copyright 2007 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.


import java.util.*;


public class MRPP {
	int n;
	Random random=new Random(System.currentTimeMillis());
	
	
	double[][] dat;
	int[] marker;
	
	int weightingMethod=0;
	int nresamp=10000;
	
	double expectedDelta, empiricalDelta, pvalue, avalue;
	
	public MRPP(double[][] d, int[] partition){
		n=d.length;

		int maxPart=0;
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
				}
			}
		double t2=t;
		
			dat=new double[t][];
			for (int i=0; i<t; i++){
				dat[i]=new double[i+1];
				for (int j=0; j<i; j++){
					dat[i][j]=d[look[i]][look[j]];
				}
			}
			n=dat.length;
		
			empiricalDelta=calculateDelta(partb, countPart, t2, maxPart);
		
			double[] resamples=new double[nresamp];
		
			int count=0;
			double av=0;
			for (int i=0; i<nresamp; i++){
				int[] markr=shufflePartition(partb);
				resamples[i]=calculateDelta(markr, countPart, t2, maxPart);
				if (resamples[i]<=empiricalDelta){count++;}
				av+=resamples[i];
			}
			expectedDelta=av/nresamp+0.0;
			pvalue=(1.0+count)/(nresamp+0.0);
			avalue=1-(empiricalDelta/expectedDelta);
		
			System.out.println("Empirical Delta: "+empiricalDelta+" Expected Delta: "+expectedDelta+" p: "+pvalue+" A: "+avalue);
		}
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
	
	public double getPValue(){
		return pvalue;
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
