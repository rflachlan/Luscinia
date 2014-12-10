package lusc.net.github.analysis.syntax;
//
//  MarkovChain.java
//  Luscinia
//
//  Created by Robert Lachlan on 4/17/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

import java.util.*;

public class MarkovChain {
	
	double[] redundancy, entropy, zeroOrder;
	double[] resultArrayP;
	
	int kv=0;
	int n=0;
	int nPos=6;
	
	int numInd=0;
	int numSong=0;
	
	boolean trueRedundancy=false;
			
	
	int[][] individualLabel, dat, posDat;
	double[] G;
	double eval=1/Math.log(2);
	
	Random random=new Random(System.currentTimeMillis());
	
	public MarkovChain(){}
	
	public MarkovChain(int[][] individualLabel, int[][] dat, int kv, int n){
		this.kv=kv;
		this.n=n;
		numInd=individualLabel.length;
		numSong=dat.length;
		this.individualLabel=individualLabel;
		this.dat=dat;
		
		posDat=new int[numSong][];

		for (int i=0; i<numSong; i++){
			posDat[i]=new int[dat[i].length];
			for (int j=0; j<dat[i].length; j++){
				posDat[i][j]=(int)Math.round((nPos-1)*j/(dat[i].length-1.0));
			}
		}
		calculateG(2*n);
		System.out.println("Starting mk bootstrap: "+kv);
		double[][] results=bootstrapEntropyN();
		redundancy=results[0];
		entropy=results[1];
		zeroOrder=results[2];
		
		System.out.println("Starting pos bootstrap: "+kv);
		resultArrayP=bootstrapEntropyA();
	}

	public int getKv(){
		return kv;
	}
	
	public int getNPos(){
		return nPos;
	}
	
	public double[] getRedundancy(){
		return redundancy;
	}
	
	public double[] getEntropy(){
		return entropy;
	}
	
	public double[] getZeroOrder(){
		return zeroOrder;
	}
	
	public double[] getResultArrayP(){
		return resultArrayP;
	}
	
	public double calculateFirstOrderEntropy(int[] labels, int[] elementLabels, int n){
		
		int totalElementCount=labels.length;
		
		int[][] table=new int[n+1][n+1];
		int[] tot=new int[n+1];
		for (int j=0; j<totalElementCount; j++){
			if ((j-1>=0)&&(labels[j-1]==labels[j])){
				table[elementLabels[j-1]][elementLabels[j]]++;
				tot[elementLabels[j-1]]++;
				
			}
			else {
				table[n][elementLabels[j]]++;
				tot[n]++;
			}
			if ((j+1==totalElementCount)||(labels[j+1]!=labels[j])){
				table[elementLabels[j]][n]++;
				tot[elementLabels[j]]++;
			}
		}
		
		double mean=calculateFromTable(table);	
		
		double zeroOrder=calculateZeroOrderEntropy2(tot);
		
		//System.out.println(mean+" "+zeroOrder);
		
		double entropy=mean-zeroOrder; 
		double maxH=zeroOrder;
		double rho=(maxH-entropy)/maxH;
		//System.out.println("REF"+mean+" "+zeroOrder+" "+entropy+" "+rho);
		return rho;
	}
		

	public double[][] bootstrapEntropyN(){
		
		int resamples=100001;
		int cutOffs[]={50, 500, 2500, 50000, 97500, 99500, 99950}; 

		double[] results=new double[resamples];
		double[] entropies=new double[resamples];
		double[] zeroes=new double[resamples];
		
		int kv1=kv+1;
		double MaxH=Math.log(kv1);
		for (int i=0; i<resamples; i++){

			int[][] table1=new int[kv1][kv1];
			int[] table2=new int[kv1];
			
			int p,q,r;

			for (int j=0; j<numInd; j++){
				int ind=random.nextInt(numInd);
				
				for (int k=0; k<individualLabel[ind].length; k++){
				//int k=random.nextInt(individualLabel[ind].length);	
				int kk=individualLabel[ind][k];
					
					p=dat[kk][0];
					table1[kv][p]++;
					table2[kv]++;
					table2[p]++;
					r=dat[kk].length;
					for (int a=1; a<r; a++){
						q=dat[kk][a];
						table1[p][q]++;
						table2[q]++;
						p=q;
					}
					table1[p][kv]++;
				}
			}
				
			double mean=calculateFromTable(table1);	
			double zeroOrder=calculateZeroOrderEntropy2(table2);
			double entropy=mean-zeroOrder;
			double rho=(zeroOrder-entropy)/zeroOrder;
			if (trueRedundancy){
				rho=(MaxH-entropy)/MaxH;
			}
			results[i]=rho;
			entropies[i]=entropy;
			zeroes[i]=zeroOrder;
		}
		Arrays.sort(results);
				
		double[][] output=new double[3][cutOffs.length];
		for (int i=0; i<cutOffs.length; i++){
			output[0][i]=results[cutOffs[i]];
			output[1][i]=entropies[cutOffs[i]];
			output[2][i]=zeroes[cutOffs[i]];
		}
		return(output);
	}
	
	public double[] bootstrapEntropyNoOrder(){
		
		int resamples=100001;

		double[] results=new double[resamples];
		int kv1=kv+1;
		double MaxH=Math.log(kv1);
		for (int i=0; i<resamples; i++){

			int[][] table1=new int[kv1][kv1];
			int[] table2=new int[kv1];
			
			int p,q,r;

			for (int j=0; j<numInd; j++){
				int ind=random.nextInt(numInd);
				
				for (int k=0; k<individualLabel[ind].length; k++){
				//int k=random.nextInt(individualLabel[ind].length);	
				int kk=individualLabel[ind][k];
					
					p=dat[kk][0];
					table1[kv][p]++;
					table2[kv]++;
					table2[p]++;
					r=dat[kk].length;
					for (int a=1; a<r; a++){
						q=dat[kk][a];
						table1[p][q]++;
						table2[q]++;
						p=q;
					}
					table1[p][kv]++;
				}
			}
				
			double mean=calculateFromTable(table1);	
			double zeroOrder=calculateZeroOrderEntropy2(table2);
			double entropy=mean-zeroOrder;
			double rho=(zeroOrder-entropy)/zeroOrder;
			if (trueRedundancy){
				rho=(MaxH-entropy)/MaxH;
			}
			results[i]=rho;
		}
		
		return(results);
	}
	
	public double[] bootstrapEntropyA(){
		
		int resamples=100000;
		int cutOffs[]={24, 249, 2499, 50000, 97500, 99750, 99975}; 
		
		double[] results=new double[resamples];
		
		for (int i=0; i<resamples; i++){
			
			int[][] table1=new int[kv][nPos];
			int[][] table2=new int[kv][nPos];
			int[] table3=new int[kv];
			int[] table4=new int[nPos];
			
			int p,q,r;
			double tot=0;
			for (int j=0; j<numInd; j++){
				int ind=random.nextInt(numInd);
				
				for (int k=0; k<individualLabel[ind].length; k++){
					int kk=individualLabel[ind][k];
					r=dat[kk].length;
					for (int a=0; a<r; a++){
						p=dat[kk][a];
						q=posDat[kk][a];
						table1[p][q]++;
						table3[p]++;
						table4[q]++;
						tot++;
					}
				}
			}
			double totadj=1/tot;
			for (int j=0; j<kv; j++){
				for (int k=0; k<nPos; k++){
					table2[j][k]=(int)Math.round(table3[j]*table4[k]*totadj);
				}
			}
			
			double mean=calculateFromTable(table1);	
			double zeroOrder=calculateZeroOrderEntropy2(table4);
			double entropy=calculateFromTable(table2);
			double rho=(entropy-mean)/(entropy-zeroOrder);
			
			results[i]=rho;
			
		}
		Arrays.sort(results);
		
		double[] output=new double[cutOffs.length];
		for (int i=0; i<cutOffs.length; i++){
			output[i]=results[cutOffs[i]];
		}
		return(output);
	}
	
	public double calculateFromTable(int[][] table){
		
		double gt=0;
		int n=table.length;
		int m=table[0].length;
		
		for (int j=0; j<n; j++){
			for (int k=0; k<m; k++){
				if (table[j][k]>0){
					gt+=table[j][k];
				}
			}
		}
		
		double score=0;
		for (int j=0; j<n; j++){
			for (int k=0; k<m; k++){
				if (table[j][k]>0){
					score+=G[table[j][k]]*table[j][k];
					//score+=Math.log(table[j][k]/gt)*table[j][k]/gt;
				}
			}
		}
		score=Math.log(gt)-(score/gt);
		//score*=-1;
		return score;
	}

	
	
	public double calculateZeroOrderEntropy(int[] elementLabels){
		
		double score=0;
		int n=elementLabels.length;
		double m=0;
		for (int i=0; i<n; i++){
			m+=elementLabels[i];
		}
						
		for (int i=0; i<n; i++){
			if (elementLabels[i]>0){
				score+=G[elementLabels[i]]*elementLabels[i];
				//score+=Math.log(elementLabels[i]/m)*elementLabels[i]/m;
			}
		}
		score=Math.log(m)-(score/m);
		//score*=-1;
		return score;
	}
	
	public double calculateZeroOrderEntropy2(int[] elementLabels){
		
		double score=0;
		int n=elementLabels.length;
		int n1=n-1;
		double m=0;
		for (int i=0; i<n; i++){
			m+=elementLabels[i];
		}

		double labn1=elementLabels[n1];
		
		double denom2=m-labn1;
		denom2=labn1/denom2;
		
		double[] adj=new double[n1];
		
		for (int i=0; i<n1; i++){
			double k=elementLabels[i]*denom2;
			adj[i]=elementLabels[i]-k;
			
			if (k>0){
				int k2=(int)Math.floor(k);
				int k3=k2+1;
				score+=2*(k3-k)*G[k2]*k2;
				score+=2*(k-k2)*G[k3]*k3;
			}
		}	

		double denom=1/(m-2.0*labn1);
		for (int i=0; i<n1; i++){
			for (int j=0; j<n1; j++){
				double k=adj[i]*adj[j]*denom;
				if (k>0){
					int k2=(int)Math.floor(k);
					int k3=k2+1;
					score+=(k3-k)*G[k2]*k2;
					score+=(k-k2)*G[k3]*k3;
				}
			}
		}	
		
		score=0.5*(Math.log(m)-(score/m));
		//score*=-1;
		return score;
	}
	
	public double calculatePlaceHolderEntropy(int[] elementLabels, int[] locationLabels){
		
		int n=elementLabels.length;
		double nd=1/(n+0.0);
		int m=0;
		for (int i=0; i<n; i++){
			if (elementLabels[i]>m){
				m=elementLabels[i];
			}
		}
		m++;
		
		int p=0;
		for (int i=0; i<n; i++){
			if (locationLabels[i]>p){
				p=locationLabels[i];
			}
		}
		double overallScore=0;
		int overallCount=0;
		for (int i=0; i<=p; i++){
			int[] counts=new int[m];
			double q=0;
			for (int j=0; j<n; j++){
				if (locationLabels[j]==i){
					counts[elementLabels[j]]++;
					q++;
				}
			}
			
			for (int j=0; j<m; j++){
				if (counts[j]>0){
					overallCount++;
					double d=counts[j]*nd;
					overallScore+=d*Math.log(counts[j]/q)*eval;
				}
			}
		}
		
		double correctionFactor=0.72*(overallCount-p)*nd;
		overallScore*=-1;
		overallScore+=correctionFactor;
		
		
		
		return overallScore;
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

