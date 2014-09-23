
package lusc.net.sourceforge;
//
//  SWMLEntropyEstimate.java
//  Luscinia
//
//  Created by Robert Lachlan on 11/6/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

import java.util.*;


public class ContinuousSWMLEstimator {
	
	Random random=new Random(System.currentTimeMillis()+45678);
	
	
	double eval=1/Math.log(2);
	double bestRho=0;
	
	double[] bootstrapScores;
	double bootstrapMedian;
	double bootstrapSD;
	double[] G;
	
	int[][] dat;
	int[][] indlocs;
	int[] datIndLabel;
	int tec=0;
		
	public ContinuousSWMLEstimator(int[] locations, float[][] dist, int[][] individuals, int[][] lookUpSyls){
		
		/*
		int totalElementCount=0;
		for (int i=0; i<locations.length; i++){
			totalElementCount+=locations[i];
		}
		
		int[] labels=new int[totalElementCount];
		int a=0;
		for (int i=0; i<locations.length; i++){
			for (int j=0; j<locations[i]; j++){
				labels[a]=i;
				a++;
			}
		}
		
		int[] individualLabel=new int[totalElementCount];
		for (int i=0; i<individuals.length; i++){
			for (int j=0; j<individuals[i].length; j++){
				for (int k=0; k<lookUpSyls.length; k++){
					if (individuals[i][j]==lookUpSyls[k][0]){
						individualLabel[k]=i;
					}
				}
			}
		}

		LinkedList<int[]> pos=new LinkedList<int[]>();
		int x=0;
		int y=0;
		for (int i=1; i<labels.length; i++){
			if (labels[i]!=labels[i-1]){
				y=i;
				int[] aa={x,y};
				pos.add(aa);
				x=i;
			}
		}
		int[] d={x,labels.length};
		pos.add(d);
		
		dat=new int[pos.size()][];
		datIndLabel=new int[dat.length];
		int[] lengths=new int[dat.length];
		int[] inds=new int[pos.size()];
		for(int i=0; i<dat.length; i++){
			int[] aa=pos.get(i);
			
			int w=aa[1]-aa[0];
			lengths[i]=w+1;
			dat[i]=new int[w+1];
			for (int j=aa[0]; j<aa[1]; j++){
				dat[i][j-aa[0]+1]=j;
			}
			dat[i][0]=-1;
			tec+=w+1;
			datIndLabel[i]=individualLabel[aa[0]];
		}
		
		indlocs=new int[individuals.length][];
		
		int[] indcounts=new int[individuals.length];
		
		for (int i=0; i<dat.length; i++){
			int[] aa=pos.get(i);
			indcounts[individualLabel[aa[0]]]++;
		}
		for (int i=0; i<individuals.length; i++){
			indlocs[i]=new int[indcounts[i]];
		}
		indcounts=new int[individuals.length];
		for (int i=0; i<dat.length; i++){
			int[] aa=pos.get(i);
			int b=individualLabel[aa[0]];
			indlocs[b][indcounts[b]]=i;
			indcounts[b]++;
		}
			
		calculateG(totalElementCount*2);		
		*/
		
		int n=dist.length;
		float[][] dat2=new float[n][n];
		float[] datList=new float[n*(n-1)/2];
		int jj=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				dat2[i][j]=dist[i][j];
				dat2[j][i]=dist[i][j];
				datList[jj]=dist[i][j];
				jj++;
			}
		}
		Arrays.sort(datList);
		
		float[][] dat3=new float[n][n];
		int[] order=new int[n];
		for (int i=0; i<n; i++){
			order[i]=i;
		}
		
		double[] prop={0.01, 0.02, 0.03, 0.05, 0.1, 0.2, 0.3, 0.5}; 
		
		for (int i=0; i<8; i++){
			
			
			int q=(int)Math.round(prop[i]*n*(n-1)/2);
			double s=datList[q];
			System.out.println("CSWML"+i+" "+prop[i]+" "+q+" "+datList[0]+" "+datList[(n*(n-1)/2)-1]);
			double score=CEWML(dat2, n, s);
			double randomscore=0;
			for (int j=0; j<10; j++){
				int[] ord2=shuffle(order);
				
				for (int a=0; a<n; a++){
					for (int b=0; b<n; b++){
						dat3[order[a]][order[b]]=dat2[i][j];
					}
				}
				randomscore+=CEWML(dat3, n, s);
			}
			randomscore*=0.1;
			score=(randomscore-score)/randomscore;
			
			
			System.out.println(prop[i]+" "+s+" "+score);
		}

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
			//System.out.println(i+" "+d[i]+" "+res[i]);
		}
		return(res);
	}
	
	public double calculateZeroOrderEntropy(int[] labels, int k, int sn){
		
		int[] tots=new int[k+1];
		double d=1/(labels.length+sn+0.0);
		for (int i=0; i<labels.length; i++){
			tots[labels[i]]++;
		}
		tots[k]=sn;
				
		double adj=-1/Math.log(2);
		double score=0;
		for (int i=0; i<tots.length; i++){
			if (tots[i]>0){
				double p=tots[i]*d;
				//score+=p*Math.log(p)*adj;
				score+=G[tots[i]]*tots[i];
			}
		}
		//score+=0.72*k*d;
		score=Math.log(labels.length+sn)-d*score;
		score*=eval;
		return score;
	}
	
	public void bootstrap(){

		int bootstrapreps=10000;
		int[] props={2, 25, 250, 5000, 9750, 9975, 9998};
		
		double[] bootstrapests=new double[bootstrapreps];
		
		int[] order=new int[dat.length];
		for (int i=0; i<order.length; i++){
			order[i]=i;
		}
		
		int[] rearrange=new int[2*tec];
		
		int ind=indlocs.length;
		
		int[][] datr=new int[dat.length][];
		
		
		for (int p=0; p<bootstrapreps; p++){
			int tot=0;
			for (int i=0; i<dat.length; i++){
				int q=random.nextInt(ind);
				datr[i]=dat[q];   //this is very wrong. But the bootstrap won't work anyway
				tot+=dat[q].length;
			}
			
			Collections.shuffle(Arrays.asList(order));
			
			
			int jj=0;
			for (int i=0; i<datr.length; i++){
				for (int j=0; j<datr[order[i]].length; j++){
					rearrange[jj]=datr[order[i]][j];
					jj++;
				}
			}
			double score=EWML(rearrange, tot);
			bootstrapests[p]=score;
		}
		
		
		Arrays.sort(bootstrapests);
		
		bootstrapScores=new double[props.length];
		for (int i=0; i<props.length; i++){
			bootstrapScores[i]=bootstrapests[props[i]];
		}
		
		BasicStatistics bs=new BasicStatistics();
		
		bootstrapSD=bs.calculateSD(bootstrapests, true);
		
	}
	
	public void jackknife(){
		
		int[] props={2, 25, 250, 5000, 9750, 9975, 9998};
		int ind=indlocs.length;
		double[] jkests=new double[ind];
		
		int[] order=new int[dat.length];
		for (int i=0; i<order.length; i++){
			order[i]=i;
		}
		
		int[] rearrange=new int[tec];
		int permutations=10;
		
		for (int p=0; p<indlocs.length; p++){
			
			for (int q=0; q<permutations; q++){
				Collections.shuffle(Arrays.asList(order));

				int jj=0;
				for (int i=0; i<dat.length; i++){
					if (datIndLabel[i]!=p){
						for (int j=0; j<dat[order[i]].length; j++){
							rearrange[jj]=dat[order[i]][j];
							jj++;
						}
					}
				}
				double score=EWML(rearrange, jj);
				double zero=1;
				jkests[p]+=(zero-score)/zero;
			}
			jkests[p]/=permutations+0.0;
		}
		

		
		BasicStatistics bs=new BasicStatistics();
		
		bootstrapSD=bs.calculateSD(jkests, true);
		double bse=bootstrapSD;
		bootstrapScores=new double[7];
		bootstrapScores[3]=bs.calculateMean(jkests);
		bootstrapScores[0]=bootstrapScores[3]-3.290527*bse;
		bootstrapScores[1]=bootstrapScores[3]-2.575829*bse;
		bootstrapScores[2]=bootstrapScores[3]-1.959964*bse;
		bootstrapScores[6]=bootstrapScores[3]+3.290527*bse;
		bootstrapScores[5]=bootstrapScores[3]+2.575829*bse;
		bootstrapScores[4]=bootstrapScores[3]+1.959964*bse;
	}
	
	
	public double calculateEWMLEntropy(){
		
		int[] order=new int[dat.length];
		for (int i=0; i<order.length; i++){
			order[i]=i;
		}
		
		
		double score=0;
		double count=0;
		int[] rearrange=new int[tec];
		
		int permutations=1000;
		for (int p=0; p<permutations; p++){
			Collections.shuffle(Arrays.asList(order));
			
			
			int jj=0;
			for (int i=0; i<dat.length; i++){
				for (int j=0; j<dat[order[i]].length; j++){
					rearrange[jj]=dat[order[i]][j];
					jj++;
				}
			}
			score+=EWML(rearrange, rearrange.length);
		}
		
		score/=permutations+0.0;
		double zero=1;
		
		System.out.println(score+" "+zero);
		score=(zero-score)/zero;
		return score;
	}


	public double EWML(int[] data, int p){
		int w=p-1;
		double score=0;
		double score1=0;
		double score2=0;
		double q=1/(Math.log(w)*eval);
		for (int i=0; i<p; i++){
			double bestm=0;
			for (int j=i-w; j<i; j++){
				if (j!=i){
					int ii=i;
					int jj=j;
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
	
	public double CEWML(float[][] data, int p, double threshold){
		int w=p-1;
		double score=0;
		double q=1/(Math.log(w)*eval);
		for (int i=0; i<p; i++){
			double bestm=0;
			for (int j=i-w; j<i; j++){
				if (j!=i){
					int ii=i;
					int jj=j;
					if(jj<0){jj+=p;}
					int c=0;
					while ((data[ii][jj]<threshold)&&(c<p)){
						c++;
						ii++;
						if (ii==p){ii=0;}
						jj++;
						if (jj==p){jj=0;}
					}
					if (c>bestm){bestm=c;}
				}
			}
			score+=(bestm+1)*q;
		}
		score/=p-0.0;
		score=1/score;
		return score;
	}
	
	void calculateG(int m){
		G=new double[m*2];
		
		G[1]=-0.5772156649-Math.log(2);
		G[2]=2+G[1];
		//System.out.println("1 "+G[1]);
		//System.out.println("2 "+G[2]);
		for (int i=3; i<m+1; i++){
			if (i % 2 ==0){
				G[i]=G[i-2]+(2/(i-1.0));
			}
			else{
				G[i]=G[i-1];
			}
			//System.out.println(i+" "+G[i]);
		}
		
	}
	
}
