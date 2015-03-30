package lusc.net.github.analysis.clustering;
//
//  KMedoids.java
//  Luscinia
//
//  Created by Robert Lachlan on 9/4/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//

import java.util.*;

import lusc.net.github.analysis.multivariate.MRPP;

public class KMedoids {
	
	int type=0;
	int nsims=10;
	int maxK, minK;
	int n;
	int numReseeds=20;
	
	int[][] overallAssignments;
	//int[] overallPrototypes;
	double[][] prototypeDistances, silhouettes, MRPPresults;
	
	double[] globalSilhouette, simulatedSilhouette;
	
	double[] mat2;
	int[] rowheads;
	int bestK=0;
	int assignmentLength;
	
	Random random=new Random(System.currentTimeMillis());
	
	
	
	public KMedoids (double[][] dMat, int minK, int maxK, int type, double[] sds, int nsims){
		this.type=0;
		makeMatrix2(dMat, 1);
		this.maxK=maxK;
		this.minK=minK;
		this.nsims=nsims;
		if (maxK*2>=dMat.length){
			maxK=dMat.length/2;
		}
		
		n=dMat.length;
		if (maxK>n/2){
			maxK=n/2;
		}
		
		assignmentLength=maxK-minK+1;
		
		overallAssignments=new int[assignmentLength][n];
		prototypeDistances=new double[maxK+minK+1][n];
		silhouettes=new double[assignmentLength][n];
		globalSilhouette=new double[assignmentLength];
		simulatedSilhouette=new double[assignmentLength];
		int ncores=Runtime.getRuntime().availableProcessors()/2;
		int nr=(int)Math.ceil(numReseeds/ncores);
		KMedoidsThread[] kmt=new KMedoidsThread[numReseeds];
		
		
		
		
		for (int i=minK; i<=maxK; i++){
			System.out.println("k: "+i);
			int a=0;
			
			KMedoidsThread kmb=new KMedoidsThread(mat2, rowheads, i, nr, true);
			kmb.setPriority(Thread.MIN_PRIORITY);
			kmb.start();
			try{
				kmb.join();	
			}
			catch (Exception e){
				e.printStackTrace();
			}
			
			int[] assignments=assignToClusters(kmb.results, i);
			double bestScore=kmb.bestscore;
			
			
			for (int j=0; j<nr; j++){
				int b=a+ncores;
				if(b>numReseeds){b=numReseeds;}
				
				for (int k=a; k<b; k++){
					kmt[k]=new KMedoidsThread(mat2, rowheads, i, nr, false);
					kmt[k].setPriority(Thread.MIN_PRIORITY);
					kmt[k].start();

				}
				try{
					for (int k=a; k<b; k++){
						kmt[k].join();
					}	
				}
				catch (Exception e){
					e.printStackTrace();
				}
				a=b;
			}
			
			for (int k=0; k<numReseeds; k++){
				if (kmt[k].bestscore<bestScore){
					int[] overallPrototypes=kmt[k].results;
					assignments=assignToClusters(overallPrototypes, i);
					bestScore=kmt[k].bestscore;
				}
			}
			System.arraycopy(assignments, 0, overallAssignments[i-minK], 0, n);
			silhouettes[i-minK]=calculateSilhouetteWidth(assignments, dMat, i);
			for (int j=0; j<silhouettes[i-minK].length; j++){
				globalSilhouette[i-minK]+=silhouettes[i-minK][j];
			}
			globalSilhouette[i-minK]/=n+0.0;
		}
		if (nsims>0){
			for (int i=minK; i<=maxK; i++){
				for (int ii=0; ii<nsims; ii++){
					double[][] simmat=simulateMatrix(sds);
					makeMatrix2(simmat, 1);
					int a=0;
					
					KMedoidsThread kmb=new KMedoidsThread(mat2, rowheads, i, nr, true);
					kmb.setPriority(Thread.MIN_PRIORITY);
					kmb.start();
					try{
						kmb.join();	
					}
					catch (Exception e){
						e.printStackTrace();
					}
					
					int[] assignments=assignToClusters(kmb.results, i);
					double bestScore=kmb.bestscore;
					
					
					for (int j=0; j<nr; j++){
						int b=a+ncores;
						if(b>numReseeds){b=numReseeds;}
					
			
						System.out.println("k: "+i);
			
				
						for (int k=a; k<b; k++){
							kmt[k]=new KMedoidsThread(mat2, rowheads, i, nr, false);
							kmt[k].setPriority(Thread.MIN_PRIORITY);
							kmt[k].start();

						}
						try{
							for (int k=a; k<b; k++){
								kmt[k].join();
							}
						}
						catch (Exception e){
							e.printStackTrace();
						}
						a=b;
					}
			
					for (int k=0; k<numReseeds; k++){
						if (kmt[k].bestscore<bestScore){
							int[] overallPrototypes=kmt[k].results;
							assignments=assignToClusters(overallPrototypes, i);
							bestScore=kmt[k].bestscore;
						}
					}

			
			
					double[] siltemp=calculateSilhouetteWidth(assignments, simmat, i);
					for (int j=0; j<siltemp.length; j++){
						simulatedSilhouette[i-minK]+=siltemp[j];
					}
				}
				simulatedSilhouette[i-minK]/=n*nsims*1.0;
			}
		}
		mat2=null;
	}
	
	
	public void makeMatrix2(double[][] dMat, int pow){
		int n=dMat.length;
		mat2=new double[n*n];
		rowheads=new int[n];
		int k=0;
		double c=0;
		for (int i=0; i<n; i++){
			rowheads[i]=k;
			for (int j=0; j<n; j++){
				
				if (i==j){
					c=0f;
				}
				else if(i>j){
					c=Math.pow(dMat[i][j], pow);
				}
				else{
					c=Math.pow(dMat[j][i], pow);
				}
				mat2[k]=c;
				k++;
			}
		}
	}
	
	public int getType(){
		return type;
	}
	
	public int getMinK(){
		return minK;
	}
	
	public double[][] getPrototypeDistances(){
		return prototypeDistances;
	}
	
	public double[][] getSilhouettes(){
		return silhouettes;
	}
	
	public int[][] getOverallAssignments(){
		return overallAssignments;
	}
	
	public double[] getGlobalSilhouette(){
		return globalSilhouette;
	}
	
	public double[] getSimulatedSilhouette(){
		return simulatedSilhouette;
	}
	
	public int getAssignmentLength(){
		return assignmentLength;
	}
	
	
	public double[][] simulateMatrix(double[] sds){
		
		double[][] dMatS=new double[n][];
		for (int i=0; i<n; i++){
			dMatS[i]=new double[i+1];
		}
		int m=sds.length;
		double[][] locs=new double[n][m];
		for (int i=0; i<n; i++){
			for (int j=0; j<m; j++){
				locs[i][j]=random.nextGaussian()*sds[j];
			}
		}
		
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				double score=0;
				for (int k=0; k<m; k++){
					score+=(locs[i][k]-locs[j][k])*(locs[i][k]-locs[j][k]);
				}
				dMatS[i][j]=Math.sqrt(score);
			}
		}
		return dMatS;
	}
	
	class KMedoidsThread extends Thread{
	
		int kv;
		int reps;
		int p=0;
		double oscore;
		double bestscore;
		int[] results;
		double[] dat;
		int[] rowhead;
		boolean useBuild=false;
		
		
		public KMedoidsThread(double[] da, int[] rowh, int kv, int reps, boolean useBuild){
			this.reps=reps;
			this.kv=kv;
			this.useBuild=useBuild;
			int m=da.length;
			dat=new double[m];
			System.arraycopy(da,0, dat, 0, m);
			p=rowh.length;
			rowhead=new int[p];
			System.arraycopy(rowh,0, rowhead, 0, p);
		}
		
		public int[] build(int kv){
			int[] prots=new int[kv];
			for (int i=0; i<kv; i++){
				int ii=i+1;
				int[] tp=new int[ii];
				for (int j=0; j<i; j++){
					tp[j]=prots[j];
				}
				double bestscore=1000000;
				int loc=-1;
				for (int j=0; j<n; j++){
					boolean found=false;
					for (int k=0; k<i; k++){
						if (tp[k]==j){
							found=true;
							k=i;
						}
					}
					if (!found){
						tp[i]=j;
						double d=0;
						double z=0;
						double bs=0;
						for (int r=0; r<n; r++){
							bs=1000000;
							for (int s=0; s<ii; s++){
								z=dat[rowhead[tp[s]]+r];
								if (z<bs){
									bs=z;
								}
							}
							d+=bs;
						}
						
						if (d<bestscore){
							bestscore=d;
							loc=j;
						}	
					}
					
					
				}
				prots[i]=loc;
			}
			return prots;
		}
		
		
		public void run(){
			
			bestscore=10000000f;
			for (int i=0; i<reps; i++){
				int[] r=iterateAlgorithm();
				if (oscore<bestscore){
					bestscore=oscore;
					results=r;
				}
			}	
		}
	
		public int[] iterateAlgorithm(){
		
			int[] prototypes=new int[kv];
			int[] protoheads=new int[kv];
			int[] assignments=new int[p];
			double[] scores=new double[p];
			boolean[] isPrototype=new boolean[p];
			int y=0;
			double totalScore=0;
		
			int a;
			double bestScore,z;
		
			for (int i=0; i<p; i++){
				isPrototype[i]=false;
			}
		
			
			if (useBuild){
				prototypes=build(kv);
				for (int i=0; i<kv; i++){
					isPrototype[prototypes[i]]=true;
					protoheads[i]=rowhead[prototypes[i]];
				}
			}
			else{
				for (int i=0; i<kv; i++){
					int j=random.nextInt(p);
					while(isPrototype[j]){
						j=random.nextInt(p);
					}
					prototypes[i]=j;
					protoheads[i]=rowhead[j];
					isPrototype[j]=true;
				}
			}
			
			for (int i=0; i<p; i++){
				bestScore=1000000;
				a=0;
				for (int j=0; j<kv; j++){
					z=dat[protoheads[j]+i];
					if (z<bestScore){
						bestScore=z;
						a=j;
					}
				}
				assignments[i]=a;
				scores[i]=bestScore;
				totalScore+=bestScore;
			}
			int id1=0;
			boolean cont=true;
		
			while(cont){
			//System.out.println(id1+" "+totalScore);
			
				cont=false;
				for (int i=0; i<kv; i++){
					double topScore=totalScore;
					id1=-1;
					for (int j=0; j<p; j++){
						if (!isPrototype[j]){
							double tempScore=totalScore;
							protoheads[i]=rowhead[j];
							for (int k=0; k<p; k++){
								if (assignments[k]==i){
									bestScore=1000000;
									for (int l=0; l<kv; l++){
										z=dat[protoheads[l]+k];
										if (z<bestScore){
											bestScore=z;
										}
									}		
									tempScore+=bestScore-scores[k];
								}
								else{
									z=dat[protoheads[i]+k];
									if (z<scores[k]){
										tempScore+=z-scores[k];
									}
								}
							}
							if (tempScore<topScore){
								topScore=tempScore;
								id1=j;
							}
						}
					}
					protoheads[i]=rowhead[prototypes[i]];
			
			//System.out.println(id1+" "+totalScore+" "+topScore);
					if (id1>=0){
						cont=true;
						isPrototype[prototypes[i]]=false;
						prototypes[i]=id1;
						isPrototype[id1]=true;
						protoheads[i]=rowhead[id1];
				
						totalScore=0f;
						for (int k=0; k<p; k++){
							bestScore=1000000;
							a=0;
							for (int j=0; j<kv; j++){
								z=dat[protoheads[j]+k];
								if (z<bestScore){
									bestScore=z;
									a=j;
								}
							}
							assignments[k]=a;
							scores[k]=bestScore;
							totalScore+=bestScore;
						}
					}
				}	
			//System.out.println(id1+" "+totalScore+" "+topScore);
			}
			oscore=totalScore;
			return prototypes;
		}

	
	public int[] iterateAlgorithm2(int kv){
		
		int[] prototypes=new int[kv];
		int[] protoheads=new int[kv];
		int[] assignments=new int[p];
		double[] scores=new double[p];
		boolean[] isPrototype=new boolean[p];
		int y=0;
		double totalScore=0;
		
		int a;
		double bestScore,z;
		
		for (int i=0; i<p; i++){
			isPrototype[i]=false;
		}
		
		for (int i=0; i<kv; i++){
			int j=random.nextInt(p);
			while(isPrototype[j]){
				j=random.nextInt(p);
			}
			prototypes[i]=j;
			protoheads[i]=rowhead[j];
			isPrototype[j]=true;
		}
			
		for (int i=0; i<p; i++){
			bestScore=1000000;
			a=0;
			for (int j=0; j<kv; j++){
				z=dat[protoheads[j]+i];
				if (z<bestScore){
					bestScore=z;
					a=j;
				}
			}
			assignments[i]=a;
			scores[i]=bestScore;
			totalScore+=bestScore;
		}
		
		int id1=0;
		int id2=0;
		while(id1>=0){
			//System.out.println(id1+" "+totalScore);
			double topScore=totalScore;
			id1=-1;
			id2=-1;
			for (int i=0; i<kv; i++){
				for (int j=0; j<p; j++){
					if (!isPrototype[j]){
						double tempScore=totalScore;
						protoheads[i]=rowhead[j];
						for (int k=0; k<p; k++){
							if (assignments[k]==i){
								bestScore=1000000;
								for (int l=0; l<kv; l++){
									z=dat[protoheads[l]+k];
									if (z<bestScore){
										bestScore=z;
									}
								}	
								tempScore+=bestScore-scores[k];
							}
							else{
								z=dat[protoheads[i]+k];
								if (z<scores[k]){
									tempScore+=z-scores[k];
								}
							}
						}
						if (tempScore<topScore){
							topScore=tempScore;
							id1=i;
							id2=j;
						}
					}
				}
				protoheads[i]=rowhead[prototypes[i]];
			}
			//System.out.println(id1+" "+totalScore+" "+topScore);
			if (id1>=0){
				isPrototype[prototypes[id1]]=false;
				prototypes[id1]=id2;
				isPrototype[id2]=true;
				protoheads[id1]=rowhead[id2];
				
				totalScore=0f;
				for (int i=0; i<p; i++){
					bestScore=1000000;
					a=0;
					for (int j=0; j<kv; j++){
						z=dat[protoheads[j]+i];
						if (z<bestScore){
							bestScore=z;
							a=j;
						}
					}
					assignments[i]=a;
					scores[i]=bestScore;
					totalScore+=bestScore;
				}
			}	
			//System.out.println(id1+" "+totalScore+" "+topScore);
		}
		return prototypes;
	}
	
	}
	
	public double calculateCost2(int[] prototypes){
		int m=prototypes.length;
		double totalCost=0;
		double bestScore=1000000;
		double q=0;
		for (int i=0; i<n; i++){
			bestScore=1000000;
			for (int j=0; j<m; j++){
				q=mat2[rowheads[prototypes[j]]+i];
				
				if(q<bestScore){
					bestScore=q;
				}
			}
			totalCost+=bestScore;
		}
		return (totalCost);
	}
	
	
	public int[] assignToClusters(int[] prototypes, int q){
		
		int m=prototypes.length;
		int[] assignments=new int[n];
		for (int i=0; i<n; i++){
			double bestScore=1000000;
			int loc=-1;
			for (int j=0; j<m; j++){
				double s=mat2[rowheads[prototypes[j]]+i];
				if(s<bestScore){
					bestScore=s;
					loc=j;
				}
			}
			prototypeDistances[q-minK][i]=bestScore;
			assignments[i]=loc;
		}
		return assignments;
	}
	
	public double calculateAverageSilhouetteScore(int[] assignments, int kv, double[][] dMat){
		double overallAvSil=0;
		for (int i=0; i<kv; i++){
			double avSil=0;
			double counter=0;
			for (int j=0; j<n; j++){
				if (assignments[j]==i){
					avSil+=calculateSilhouetteWidth(j, assignments, dMat, kv);
					counter++;
				}
			}
			overallAvSil+=avSil/counter;
		}
		return (overallAvSil/(kv+0.0));
	}
	
	public double calculateSilhouetteWidth(int f, int[] assignments, double[][] dMat, int m){
		
		double[] avbetweenscore=new double[m];
		double[] counter=new double[m];
		for (int i=0; i<n; i++){
			
			if (i<f){
				avbetweenscore[assignments[i]]+=dMat[f][i];
			}
			else{
				avbetweenscore[assignments[i]]+=dMat[i][f];
			}
			counter[assignments[i]]++;
			
		}
		double avwithinscore=avbetweenscore[assignments[f]]/(counter[assignments[f]]-1.0);
		double minbetweenscore=1000000;
		for (int i=0; i<m; i++){
			if (i!=assignments[f]){
				avbetweenscore[i]/=counter[i];
				if (avbetweenscore[i]<minbetweenscore){
					minbetweenscore=avbetweenscore[i];
				}
			}
		}
		
		double score=(minbetweenscore-avwithinscore)/Math.max(minbetweenscore, avwithinscore);
		if (counter[assignments[f]]==1){
			score=0;
		}
		return score;		
	}	
	
	public double[] calculateSilhouetteWidth(int[] assignments, double[][] dMat, int m){
		
		double[] results=new double[n];
		for (int i=0; i<n; i++){
			
			double[] score=new double[m];
			double[] counter=new double[m];
			for (int j=0; j<n; j++){
				if (i!=j){
					if (i>j){
						score[assignments[j]]+=dMat[i][j];
					}
					else{
						score[assignments[j]]+=dMat[j][i];
					}
					counter[assignments[j]]++;
				}
			}
			
			double bestBetween=1000000;
			for (int j=0; j<m; j++){
				if (counter[j]>0){
					score[j]/=counter[j];
					if ((j!=assignments[i])&&(score[j]<bestBetween)){
						bestBetween=score[j];
					}
				}
			}
			
			results[i]=(bestBetween-score[assignments[i]])/Math.max(bestBetween, score[assignments[i]]);
			//System.out.println("sils: "+i+" "+bestBetween+" "+score[assignments[i]]+" "+results[i]);
		}
		
		return results;		
	}	
	
	public void runMRPP(int[] ids, int[] spids, double[][] tmat){
		MRPPresults=new double[overallAssignments.length][];
		
		System.out.println("Overall, Non-clustered MRPP - Populations");
		MRPP mrp3=new MRPP(tmat, ids);
		System.out.println("Overall, Non-clustered MRPP - Species");
		MRPP mrp4=new MRPP(tmat, spids);
		
		
		for (int i=0; i<overallAssignments.length; i++){
			MRPPresults[i]=new double[i+minK];
			
			for (int j=0; j<i+minK; j++){
				int[] t=new int[ids.length];
				System.arraycopy(ids, 0, t, 0, ids.length);
				int[] u=new int[spids.length];
				System.arraycopy(spids, 0, u, 0, spids.length);
				for (int k=0; k<ids.length; k++){
					if (overallAssignments[i][k]!=j){
						t[k]=-1;
						u[k]=-1;
					}
				}
				System.out.println("POPULATION COMP: "+(i+minK)+" "+(j+1));
				MRPP mrp1=new MRPP(tmat, t);
				System.out.println("SPECIES COMP: "+(i+minK)+" "+(j+1));
				MRPP mrp1a=new MRPP(tmat, u);
				
				MRPPresults[i][j]=mrp1.getPValue();
			}
			
			int[] t=new int[ids.length];
			System.arraycopy(ids, 0, t, 0, ids.length);
			System.out.println("Overall MRPP, k="+(i+minK));
			MRPP mrp2=new MRPP(tmat, t,overallAssignments[i]);
			
		}
	}
		
	
	
}


