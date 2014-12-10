package lusc.net.github.analysis.syntax;
//
//  SWMLEntropyEstimate.java
//  Luscinia
//
//  Created by Robert Lachlan on 11/6/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

import java.util.*;


public class SWMLEntropyEstimate {
	
	int kv=0;
	int n=0;
	int permutations=100;
	
	double rho=0;
	double[] jackknifeScores;
	double jackknifeSD;
	
	int[] indLabel;
	int[][] dat;
	int numInds=0;
	int numSongs=0;
	
	
	double zero=0;
	double[] G;
	double eval=1/Math.log(2);
	Random random=new Random(System.currentTimeMillis()+kv);
	
	public SWMLEntropyEstimate(int[][] individualLabel, int[][] dat, int kv, int n){
		this.kv=kv;
		this.n=n;
		this.dat=dat;
		numInds=individualLabel.length;
		numSongs=dat.length;
		
		indLabel=new int[numSongs];
		for (int i=0; i<numInds; i++){
			for (int j=0; j<individualLabel[i].length; j++){
				indLabel[individualLabel[i][j]]=i;
			}
		}
		
		calculateG(n*2);	
		zero=calculateZeroOrderEntropy();
		rho=calculateEWMLEntropy();
		System.out.println("SWML "+kv+" "+zero+" "+rho+" "+n);
		jackknife();
		
	}
	
	public int getKv(){
		return kv;
	}
	
	public int getPermutations(){
		return permutations;
	}
	
	public double getRho(){
		return rho;
	}
	
	public double[] getJackknifeScores(){
		return jackknifeScores;
	}
	
	public double getJackknifeSD(){
		return jackknifeSD;
	}
	
	public int[] shuffle (int[] d){
		int n=d.length;
		int[] res=new int[n];
		System.arraycopy(d,	0, res, 0, n);
		
		for (int i=0; i<n; i++){
			int p=i+random.nextInt(n-i);
			int q=res[p];
			res[p]=res[i];
			res[i]=q;
		}
		return(res);
	}
	
	public double calculateZeroOrderEntropy(){
		int[] tots=new int[kv+1];
		double d=1/(n+numSongs+0.0);
		for (int i=0; i<dat.length; i++){
			for (int j=0; j<dat[i].length; j++){
				tots[dat[i][j]]++;
			}
			tots[kv]++;
		}
				
		double score=0;
		for (int i=0; i<tots.length; i++){
			if (tots[i]>0){
				score+=G[tots[i]]*tots[i];
			}
		}
		score=Math.log(n+numSongs)-d*score;
		score*=eval;
		return score;
	}
	
	
	public void jackknife(){
		
		double[] jkests=new double[numInds];
		
		int[] order=new int[numSongs];
		for (int i=0; i<numSongs; i++){
			order[i]=i;
		}
		
		int[] rearrange=new int[n+numSongs];
		
		for (int p=0; p<numInds; p++){
			double score=0;
			for (int q=0; q<permutations; q++){
				Collections.shuffle(Arrays.asList(order));
				int jj=0;
				for (int i=0; i<numSongs; i++){
					int k=order[i];
					if (indLabel[k]!=p){
						for (int j=0; j<dat[k].length; j++){
							rearrange[jj]=dat[k][j];
							jj++;
						}
						rearrange[jj]=-1;
						jj++;
					}
				}
				score+=EWML(rearrange, jj);
			}
			score/=permutations+0.0;
			jkests[p]=(zero-score)/zero;
		}
		
		jackknifeScores=new double[7];
		
		double jl=jkests.length;
		double mean=0;
		for (int i=0; i<jkests.length; i++){
			mean+=jkests[i];
		}
		mean/=jl;
		double ss=0;
		for (int i=0; i<jkests.length; i++){
			ss+=Math.pow(jkests[i]-mean, 2);
		}
		ss*=(jl-1.0)/jl;
		double bse=Math.sqrt(ss);
		jackknifeSD=bse;
		jackknifeScores[3]=mean;
		jackknifeScores[0]=jackknifeScores[3]-3.290527*bse;
		jackknifeScores[1]=jackknifeScores[3]-2.575829*bse;
		jackknifeScores[2]=jackknifeScores[3]-1.959964*bse;
		jackknifeScores[6]=jackknifeScores[3]+3.290527*bse;
		jackknifeScores[5]=jackknifeScores[3]+2.575829*bse;
		jackknifeScores[4]=jackknifeScores[3]+1.959964*bse;
	}
	
	
	public double calculateEWMLEntropy(){
		
		int[] order=new int[numSongs];
		for (int i=0; i<numSongs; i++){
			order[i]=i;
		}
		
		
		double score=0;
		double count=0;
		int[] rearrange=new int[n+numSongs];
		
		for (int p=0; p<permutations; p++){
			Collections.shuffle(Arrays.asList(order));

			int jj=0;
			for (int i=0; i<numSongs; i++){
				int k=order[i];
				for (int j=0; j<dat[k].length; j++){
					rearrange[jj]=dat[k][j];
					jj++;
				}
				rearrange[jj]=-1;
				jj++;
			}
			if (jj!=n+numSongs){System.out.println("OOPS: "+jj+" "+n+" "+numSongs);}
			score+=EWML(rearrange, n+numSongs);
		}
		
		score/=permutations+0.0;		
		score=(zero-score)/zero;
		return score;
	}


	public double EWML(int[] data, int p){
		int w=p-1;
		double score2=0;
		double q=1/(Math.log(w)*eval);
		int ii, jj;
		for (int i=0; i<p; i++){
			double bestm=0;
			for (int j=i-w; j<i; j++){
				if (j!=i){
					ii=i;
					jj=j;
					if(jj<0){jj+=p;}
					int c=0;
					while ((data[ii]==data[jj])&&(c<p)){
						c++;
						ii++;
						if (ii==p){ii=0;}
						jj++;
						if (jj==p){jj=0;}
					}
					if (c>bestm){bestm=c;}
				}
			}
			//System.out.println(data[i]+" "+bestm);
			//score1+=(Math.log(w)*eval)/(bestm+1);
			score2+=(bestm+1)*q;
		}
		//score1/=p-0.0;
		score2/=p-0.0;
		score2=1/score2;
		//score=0.5*(score1+score2);
		return score2;
	}
	
	void calculateG(int m){
		G=new double[m*2];
		
		G[1]=-0.5772156649-Math.log(2);
		G[2]=2+G[1];
		for (int i=3; i<m+1; i++){
			if (i % 2 ==0){
				G[i]=G[i-2]+(2/(i-1.0));
			}
			else{
				G[i]=G[i-1];
			}
		}
	}
	
}
