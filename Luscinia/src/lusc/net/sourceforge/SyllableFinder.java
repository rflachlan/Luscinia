package lusc.net.sourceforge;
//
//  SyllableFinder.java
//  Luscinia
//
//  Created by Robert Lachlan on 08/11/2005.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import java.util.*;

public class SyllableFinder {

	float[][] scores;
	int[] data;
	int range=10;
	double threshold=0.05;
	double gapThreshold=0.2;
	LinkedList results=new LinkedList();
	LinkedList gapList=new LinkedList();
	//float[][] gramscores;
	double t1, t2, t3;

	public SyllableFinder(float[][]scores, LinkedList gapList, int [] data, double t1, double t2){
		this.scores=scores;
		this.data=data;
		this.gapList=gapList;
		this.t1=t1;			//t1, t2 are the cut-off ranges of element distances
		this.t2=t2;
		t3=t2-t1;			
		int tot=0;
		for (int i=0; i<data.length; i++){
			if (i==0){
				compressSong(0, data[0]);
				tot=data[0];
			}
			else{
				compressSong(tot, tot+data[i]);
				tot+=data[i];
			}
		}
		
		double p=results.size();
		//gramscores=new float[(int)p][4];
		//for (int i=0; i<p; i++){
		//	for (int j=0; j<4; j++){
		//		gramscores[i][j]=(float)(0.5*(2-(i/p)));
		//	}
		//}
	}
	
	public void compressSong(int start, int end){
		double t;
		int size=end-start;
		double[][] res1=new double[size][range];	//these two tables give "likelihoods" of syllable positions
		double[][] res2=new double[size][range];	// based on inter-element gaps (res1) and element distances (res2)
		for (int i=0; i<size; i++){					//size is the length of the song
			for (int j=0; j<range; j++){			//range is the maximum number of elements per syllable
				if (i-2*j-1>=0){					//we count back from the focal element towards the beginning of the song
					double withinScore=0;			//withinScore is the average gap within the syllable, betweenScore the average gap between syllables
					double betweenScore=0;
					for (int k=0; k<=j; k++){
						float[] gap=(float[])gapList.get(i+start-k);
						withinScore+=gap[0];
						if (k==0){betweenScore=gap[0];}
						t=scores[i+start-k][i+start-j-k-1];		//here we add up all the element distances
						t-=t1;				
						if (t<0){t=0;}
						t/=t3;									// which are transformed here by the "max" and "min" distances - these help focus the algorithm on the most relevant range of variation in element distances
						if (t>1){t=1;}
						res2[i][j]+=t;
					}
					withinScore/=j+1.0;
					res1[i][j]=withinScore/betweenScore;
					res2[i][j]/=j+1.0;
				}
				else{
					res2[i][j]=100000;						//a very high number - means very unlikely
				}
				//System.out.println(i+" "+j+" "+res1[i][j]+" "+res2[i][j]);
			}
		}
	
		
		//System.out.println(size+" "+start+" "+end);
		double correct=0.5;
		int members[][]=new int[size][size];
		int numMembers[]=new int[size];
		
		for (int i=0; i<size; i++){
			members[i][0]=i+start;
			numMembers[i]=1;
		}
		int amt=0;
		int rpt=1;
		double bestS=0;
		for (int i=0; i<size; i++){
			bestS=1000000;
			rpt=0;
			if (numMembers[i]>0){		//this is basically saying: if no other phrase has already taken this syllable...
				for (int j=0; j<range; j++){	
					int maxk=0;
					double score=0;
					double av=0;
					double count=0;
					for (int kk=2; kk<20; kk++){		//here we're searching for repeats of syllables - up to 20
						int k=(j+1)*kk;
						int k3=i+k-1;
						boolean complete=true;
						if (k3<size){					//if we still fit within the song...
							if (res2[k3][j]>score){score=res2[k3][j];}		//we look for largest dissimilarity within potential phrase
							av+=res2[k3][j];			//and we also build up the average distance (but we don't use it!!!)
							count++;
						}
						else{
							complete=false;
						}
					
						if (complete){		//if the potential phrase fitted within the song...
							double x=score;
							if (x<threshold){
								if(k>rpt){	//the clever thing here is to look for most number of repeats that fit - more simple syllables than few complex ones
									rpt=k;		//this is all done in a rather arcane way based on an older, more flexible method, I'm afraid... basically assigns rpt etc the standards of the new best fitting syllable
									amt=j;
									bestS=x;
								}
								else if ((k==rpt)&&(x<bestS*correct)){
									rpt=k;
									amt=j;
									bestS=x;
								}
							}
						}
					}
				}
			}
			if (bestS<threshold){		//if the best-fitting phrase model is within the threshold, then we make it a real phrase...
				int k=0;
				for (int j=amt+1; j<rpt; j++){	//amt ends up being the number of repeats?
					int g=k+i;
					int h=j+i;
					int[]p={(start+g),(start+h)};
					results.add(p);
					int w=numMembers[g];
					for (int v=0; v<numMembers[h]; v++){
						members[g][w]=members[j][v];
						w++;
					}
					numMembers[g]+=numMembers[h];
					numMembers[h]=0;		//this is where we flag that these elements have already been included in a syllable
					k++;
					if (k==amt+1){k=0;}
				}
				i+=rpt-1;
			}
		}
	}

	

			



}
