package lusc.net.sourceforge;
//
//  GeographicComparison.java
//  Luscinia
//
//  Created by Robert Lachlan on 2/13/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//

import java.util.*;

public class GeographicComparison {
	
	
	double[] meanScore;
	double[][] confidenceIntervals;
	
	double[][] geographicalDistances;
	double[][] distanceCategories;
	float[][] repertoireComparison;
	double[][] coordinates;
	int[][] repertoires;
	int dataType;
	int numCategories=0;
	int numTypes=0;
	int numComps=0;
	int numInds=0;
	SongGroup sg;
	int defaultCategoryNumber=20;
	double thresholdProportion1=0.0;
	double thresholdProportion2=1.0;
	double maxDist=0;
	double similarityThreshold=0.05;
	double prop=0.5;
	int resamples=10000;
	double uberthresh=0.2;
	double threshold=0.08;
	
	Random random=new Random(System.currentTimeMillis());	
	UPGMA upgma;
	
	public GeographicComparison(SongGroup sg, int dataType, double thresholdProp){
	
		this.sg=sg;
		this.dataType=dataType;
		
		if ((sg.individualSongs==null)||(sg.individualNumber==0)){
			sg.calculateIndividuals();
		}
				
		numCategories=defaultCategoryNumber;
		numComps=(sg.individualNumber*(sg.individualNumber-1))/2;
		if (numCategories>numComps){numCategories=numComps;}
		
		float[][] songData;

		if (dataType==0){
			songData=sg.scoresEle;
		}
		else if (dataType==1){
			songData=sg.scoresEleC;
		}
		else if (dataType==2){
			songData=sg.scoresSyll;
		}
		else if (dataType==3){
			songData=sg.scoreTrans;
		}		
		else {
			songData=sg.scoresSong;
		}
		
		int[] lookUps=sg.calculateSongAssignments(dataType);
		threshold=calculateThreshold(songData, thresholdProp);
		//repertoireComparison=calculateRepertoireSimilarity(songData, lookUps);
		repertoireComparison=calculateJaccardIndex(songData, lookUps, threshold);
		geographicalDistances=calculateGeographicalDistances();
		
		
		doComparison(numCategories);
	}
	
	public GeographicComparison(SongGroup sg, int dataType, DisplayUPGMA dup){
		
		this.sg=sg;
		this.dataType=dataType;
		this.upgma=dup.upgma;
		
		if ((sg.individualSongs==null)||(sg.individualNumber==0)){
			sg.calculateIndividuals();
		}
		numInds=sg.individualNumber;		
		numCategories=defaultCategoryNumber;
		numComps=(sg.individualNumber*(sg.individualNumber-1))/2;
		if (numCategories>numComps){numCategories=numComps;}
		

		double bestSil=-1;
		int silind=-1;
		for (int i=0; i<dup.size1/2; i++){
			int ii=dup.avsils[0].length-i-1;
			if (dup.avsils[0][ii]>bestSil){
				bestSil=dup.avsils[0][ii];
				silind=ii;
			}
		}
		silind=dup.size1-silind;
		numTypes=silind+1;
		geographicalDistances=calculateGeographicalDistances();
		doComparison(numCategories, numTypes);
	}
	
	public int[] calculateCategories(int n){
		
		//int[][] cats=upgma.calculateClassificationMembers(n+1);
		int[][] cats=upgma.calculateClassificationMembers(n+1);
		
		int[] categories=new int[cats.length];
		//System.out.println(numCategories);
		for (int i=0; i<cats.length; i++){
			categories[i]=cats[i][n];
			//System.out.println(i+" "+categories[i]);
		}
		return(categories);
	}
	
	public void doComparison(int nc){
		numCategories=nc;
		distanceCategories=calculateDistanceCategories(numCategories, geographicalDistances);
		System.out.println("DISTANCE CATEGORIES MADE");
		maxDist=distanceCategories[distanceCategories.length-1][1];
		
		
		meanScore=calculateMeanScore();
		for (int i=0; i<meanScore.length; i++){System.out.println(meanScore[i]);}
		confidenceIntervals=deleteNJackknife(prop, resamples);
	}
	
	public void doComparison(int distcats, int songcats){
		
		
		
		numCategories=distcats;
		int[] categories=calculateCategories(songcats);
		int[] lookUps=sg.calculateSongAssignments(dataType);
		calculateRepertoires(categories, lookUps, sg.individualNumber);
		repertoireComparison=calculateJaccardIndex(categories, lookUps, sg.individualNumber);
		
		/*
		float[][] songData;

		if (dataType==0){
			songData=sg.scoresEle;
		}
		else if (dataType==1){
			songData=sg.scoresEleC;
		}
		else if (dataType==2){
			songData=sg.scoresSyll;
		}
		else if (dataType==3){
			songData=sg.scoreTrans;
		}		
		else {
			songData=sg.scoresSong;
		}
		uberthresh=songcats/(200.0);
		repertoireComparison=calculateRepertoireSimilarity(songData, lookUps);
		*/
		
		
		distanceCategories=calculateDistanceCategories(distcats, geographicalDistances);
		maxDist=distanceCategories[distanceCategories.length-1][1];

		meanScore=calculateMeanScore();
		for (int i=0; i<meanScore.length; i++){System.out.println(meanScore[i]);}
		confidenceIntervals=deleteNJackknife(prop, resamples);
	}
	
	public double[] calculateMeanScore(){
		
		
		double[] results=new double[numCategories];
		double[] counts=new double[numCategories];
		
		for (int i=0; i<repertoireComparison.length; i++){
			for (int j=0; j<i; j++){
				double p=0;
				for (int k=0; k<numCategories; k++){
					if ((geographicalDistances[i][j]>p)&&(geographicalDistances[i][j]<=distanceCategories[0][k])){
						results[k]+=repertoireComparison[i][j];
						counts[k]++;
					}
					p=distanceCategories[0][k];
				}
			}
		}
		
		for (int i=0; i<numCategories; i++){
			if (counts[i]>0){
				results[i]/=counts[i];
			}
		}
		
		return results;
	}
	
	public double[][] deleteNJackknife(double prop, int resamples){
		
		int n=repertoireComparison.length;
		boolean[] delete=new boolean[n];
		
		int deleteNumber=(int)Math.round(n*prop);
		
		double[] results=new double[numCategories];
		double[] counts=new double[numCategories];
		
		double[][] resampleResults=new double[numCategories][resamples];
		boolean[][] fs=new boolean[numCategories][resamples];
		for (int r=0; r<resamples; r++){
			
			for (int i=0; i<n; i++){
				delete[i]=false;
			}
			int a=0;
			while (a<deleteNumber){
				int p=random.nextInt(n);
				if (!delete[p]){
					delete[p]=true;
					a++;
				}
			}
			
			for (int i=0; i<numCategories; i++){
				results[i]=0;
				counts[i]=0;
			}
				
			for (int i=0; i<repertoireComparison.length; i++){
				if (!delete[i]){
					for (int j=0; j<i; j++){
						if (!delete[j]){
							double p=0;
							for (int k=0; k<numCategories; k++){
								if ((geographicalDistances[i][j]>p)&&(geographicalDistances[i][j]<=distanceCategories[0][k])){
								//if (geographicalDistances[i][j]<=distanceCategories[0][k]){
									results[k]+=repertoireComparison[i][j];
									counts[k]++;
								}
								p=distanceCategories[0][k];
							}
						}
					}
				}
			}
			
			for (int i=0; i<numCategories; i++){
				if (counts[i]==0){
					counts[i]=1;
					fs[i][r]=false;
				}
				else{
					fs[i][r]=true;
				}
				resampleResults[i][r]=results[i]/counts[i];
			}
		}
		
		double[][] overallResults=new double[2][numCategories];
		
		double[] means=new double[numCategories];
		double[] rc=new double[numCategories];
		for (int i=0; i<resamples; i++){
			for (int j=0; j<numCategories; j++){
				means[j]+=resampleResults[j][i];
				if (fs[j][i]){rc[j]++;}
			}
		}
		
		for (int i=0; i<numCategories; i++){
			means[i]/=rc[i];
		}
		
		double[] sumsquares=new double[numCategories];
		for (int i=0; i<resamples; i++){
			for (int j=0; j<numCategories; j++){
				if (fs[j][i]){
					double p=resampleResults[j][i]-means[j];
					sumsquares[j]+=p*p;
				}
			}
		}
		
		System.out.println(n+" "+deleteNumber+" "+resamples);
		for (int i=0; i<numCategories; i++){
			double p=sumsquares[i]*(n-deleteNumber)/(deleteNumber*rc[i]+0.0);
			p=Math.sqrt(p);
			overallResults[0][i]=means[i]-p;
			overallResults[1][i]=means[i]+p;
		}
		
		return overallResults;
	}
	
	
	public float[][] calculateJaccardIndex(float[][] songData, int[] lookUps, double threshold){
		int n=lookUps.length;
		boolean[] leaveOut=new boolean[n];
		for (int i=0; i<n; i++){
			leaveOut[i]=false;
			for (int j=0; j<i; j++){
				if ((lookUps[i]==lookUps[j])&&(songData[i][j]<threshold)){
					leaveOut[j]=true;
				}
			}
		}
		float[] counts=new float[sg.individualNumber];
		for (int i=0; i<n; i++){
			if(!leaveOut[i]){
				counts[lookUps[i]]++;
			}
		}
		float[][] results=new float[sg.individualNumber][];
		for (int i=0; i<sg.individualNumber; i++){
			results[i]=new float[i+1];
		}
		
		for (int i=0; i<n; i++){
			if (!leaveOut[i]){
				int i2=lookUps[i];
				for (int j=0; j<i; j++){
					int j2=lookUps[j];
					if (songData[i][j]<threshold){
						if (i2>j2){
							results[i2][j2]++;
						}
						else{
							results[j2][i2]++;
						}
						j=i;
					}
				}
			}
		}
		for (int i=0; i<sg.individualNumber; i++){
			for (int j=0; j<i; j++){
				//System.out.println(i+" "+j+" "+results[i][j]+" "+counts[i]+" "+counts[j]);
				results[i][j]/=counts[i]+counts[j]-results[i][j];
				//System.out.println(results[i][j]);
			}
		}
		return results;
	}
	
	public float[][] calculateJaccardIndex(int[] songIndex, int[] lookUps, int ind){
		int n=lookUps.length;
		int[][] reps=new int[ind][];
		int[] counts=new int[ind];
		for (int i=0; i<n; i++){
			boolean matched=false;
			for (int j=0; j<i; j++){
				if ((lookUps[i]==lookUps[j])&&(songIndex[i]==songIndex[j])){
					matched=true;
					j=i;
				}
			}
			if (!matched){counts[lookUps[i]]++;}
		}
		for (int i=0; i<ind; i++){
			reps[i]=new int[counts[i]];
			for (int j=0; j<counts[i]; j++){
				reps[i][j]=-1;
			}
		}
		for (int i=0; i<n; i++){
			int l=lookUps[i];
			for (int j=0; j<reps[l].length; j++){
				if (songIndex[i]==reps[l][j]){
					j=reps[l].length;
				}
				else if(reps[l][j]==-1){
					reps[l][j]=songIndex[i];
					j=reps[l].length;
				}
			}
		}
		
		for (int i=0; i<ind; i++){
			Arrays.sort(reps[i]);
			for (int j=0; j<reps[i].length; j++){
				System.out.print(reps[i][j]+" ");
				if (reps[i][j]==-1){System.out.println("ALERT");}
			}
			System.out.println(reps[i].length);
		}
		
		float[][] results=new float[ind][];
		for (int i=0; i<ind; i++){
			results[i]=new float[i+1];
		}
		
		for (int i=0; i<ind; i++){
			for (int j=0; j<i; j++){
				int c=0;
				int l1=reps[i].length;
				int l2=reps[j].length;
				for (int a=0; a<l1; a++){
					for (int b=0; b<l2; b++){
						if (reps[i][a]==reps[j][b]){
							c++;
						}
					}
				}
				results[i][j]=c/(l1+l2-c+0.0f);
				
			}
		}
		return results;
	}
	
	public float[][] calculateJaccardIndex2(int[] songIndex, int[] lookUps, int ind){
		int n=lookUps.length;
		boolean[] leaveOut=new boolean[n];
		for (int i=0; i<n; i++){
			leaveOut[i]=false;
			for (int j=0; j<i; j++){
				if ((lookUps[i]==lookUps[j])&&(songIndex[i]==songIndex[j])){
					leaveOut[i]=true;
				}
			}
		}
		float[] counts=new float[ind];
		for (int i=0; i<n; i++){
			if(!leaveOut[i]){
				counts[lookUps[i]]++;
			}
		}
		float[][] results=new float[ind][];
		for (int i=0; i<ind; i++){
			results[i]=new float[i+1];
		}
		
		for (int i=0; i<n; i++){
			if (!leaveOut[i]){
				int i2=lookUps[i];
				for (int j=0; j<i; j++){
					if (!leaveOut[j]){
						int j2=lookUps[j];
						if (songIndex[i]==songIndex[j]){
							if (i2>j2){
								results[i2][j2]++;
							}
							else{
								results[j2][i2]++;
							}
						}
					}
				}
			}
		}
		for (int i=0; i<ind; i++){
			for (int j=0; j<i; j++){
				//System.out.println(i+" "+j+" "+results[i][j]+" "+counts[i]+" "+counts[j]);
				results[i][j]/=counts[i]+counts[j]-results[i][j];
				//System.out.println(results[i][j]);
			}
		}
		return results;
	}
		
	
	public float[][] calculateRepertoireSimilarity(float[][] songData, int[] lookUps){
		
		int n=lookUps.length;
		
		System.out.println("THRESHOLD: "+uberthresh);
		
		boolean[] leaveOut=new boolean[n];

		float[] counts=new float[sg.individualNumber];
		
		
		for (int i=0; i<n; i++){
			leaveOut[i]=false;
			for (int j=0; j<i; j++){
				if ((lookUps[i]==lookUps[j])&&(songData[i][j]<uberthresh)){
					leaveOut[j]=true;
				}
			}
		}
		
		for (int i=0; i<n; i++){
			if (!leaveOut[i]){
				counts[lookUps[i]]++;
			}
		}
		
		float[][] pt=new float[n][sg.individualNumber];
		for (int i=0; i<n; i++){
			int i2=lookUps[i];
			for (int j=0; j<n; j++){
				int j2=lookUps[j];
				if (i>j){
					if (songData[i][j]<uberthresh){
						pt[i][j2]++;
					}
				}
				else{
					if (songData[j][i]<uberthresh){
						pt[i][j2]++;
					}	
				}
			}
		}
				
		float[][] results=new float[sg.individualNumber][];
		for (int i=0; i<sg.individualNumber; i++){
			results[i]=new float[i+1];
		}
		
		for (int i=0; i<n; i++){
			int i2=lookUps[i];
			if(!leaveOut[i]){
				for (int j=0; j<pt[i].length; j++){
					if ((i2>j)&&(pt[i][j]>0)){
						results[i2][j]++;
					}
				}
			}
		}
		
		for (int i=0; i<sg.individualNumber; i++){
			for (int j=0; j<i; j++){
				if (results[i][j]>Math.min(counts[i],  counts[j])){results[i][j]=Math.min(counts[i],  counts[j]);}
				results[i][j]/=(counts[i]+counts[j]-results[i][j]);
			}			
		}
		
		return results;
	}
	
	public float[][] calculateRepertoireSimilarity2(float[][] songData, int[] lookUps){
		
		int n=lookUps.length;
		
				
		//boolean[] leaveOut=new boolean[n];
		
		
		//double threshold1=calculateThreshold(songData, thresholdProportion1);
		//double threshold2=calculateThreshold(songData, thresholdProportion2);
		System.out.println("THRESHOLD: "+uberthresh);
		//double threshold1=uberthresh;
		//double threshold2=uberthresh+0.0001;
		/*
		for (int i=0; i<n; i++){
			leaveOut[i]=false;
			for (int j=0; j<i; j++){
				if ((lookUps[i]==lookUps[j])&&(songData[i][j]<threshold1)){
					leaveOut[j]=true;
				}
			}
		}
		*/
		float[] counts=new float[sg.individualNumber];
		for (int i=0; i<n; i++){
			//if(!leaveOut[i]){
				counts[lookUps[i]]++;
			//}
		}
		
		//float[][] ps=new float[n][sg.individualNumber];
		float[][] pt=new float[sg.individualNumber][sg.individualNumber];
		for (int i=0; i<n; i++){
			//if (!leaveOut[i]){
				//for (int j=0; j<sg.individualNumber; j++){
					//ps[i][j]=1000000f;
				//}
				int i2=lookUps[i];
				for (int j=0; j<n; j++){
					if (i!=j){
					//if ((i!=j)&&(!leaveOut[j])){
						int j2=lookUps[j];
						if (i>j){
							//if (songData[i][j]<ps[i][j2]){
								//ps[i][j2]=songData[i][j];
							//}
							if (songData[i][j]<uberthresh){
								pt[i2][j2]++;
							}
						}
						else{
							//if (songData[j][i]<ps[i][j2]){
								//ps[i][j2]=songData[j][i];
							//}
							if (songData[j][i]<uberthresh){
								pt[j2][i2]++;
							}
						}
					}
				}
			//}
		}
		
		//gateDistances(ps, threshold1, threshold2);
		
		float[][] results=new float[sg.individualNumber][];
		for (int i=0; i<sg.individualNumber; i++){
			results[i]=new float[i+1];
		}
		for (int i=0; i<sg.individualNumber; i++){
		//for (int i=0; i<n; i++){
			//if (!leaveOut[i]){
				//int i2=lookUps[i];
				for (int j=0; j<i; j++){
					//if (i2>j){
						//results[i2][j]+=ps[i][j]/counts[i2];
					//}
					//else{
						//results[j][i2]+=ps[i][j]/counts[i2];
					//}
					results[i][j]=pt[i][j]/(counts[i]+counts[j]-pt[i][j]);
				}
			//}
			
		}
		
		return results;
	}
	
	public double calculateThreshold(float[][] data, double prop){
		int n=data.length;
		int m=n*(n-1)/2;
		float[] d=new float[m];
		int a=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				d[a]=data[i][j];
				a++;
			}
		}
		Arrays.sort(d);
		
		int c=(int)Math.round(prop*m);
		if(c>=d.length){c=d.length-1;}
		double r=d[c];
		
		return r;
	}
	
	public void gateDistances(float[][] data, double t1, double t2){
		
		for (int i=0; i<data.length; i++){
			for (int j=0; j<data[i].length; j++){
				data[i][j]=(float)((data[i][j]-t1)/(t2-t1));
				if (data[i][j]<0){data[i][j]=0f;}
				if (data[i][j]>1){data[i][j]=1f;}
			}
		}
	}
		
	public void calculateRepertoires(int[] songIndex, int[] lookUps, int ind){
		int n=lookUps.length;
		int[][] reps=new int[ind][];
		int[] counts=new int[ind];
		for (int i=0; i<n; i++){
			boolean matched=false;
			for (int j=0; j<i; j++){
				if ((lookUps[i]==lookUps[j])&&(songIndex[i]==songIndex[j])){
					matched=true;
					j=i;
				}
			}
			if (!matched){counts[lookUps[i]]++;}
		}
		for (int i=0; i<ind; i++){
			reps[i]=new int[counts[i]];
			for (int j=0; j<counts[i]; j++){
				reps[i][j]=-1;
			}
		}
		for (int i=0; i<n; i++){
			int l=lookUps[i];
			for (int j=0; j<reps[l].length; j++){
				if (songIndex[i]==reps[l][j]){
					j=reps[l].length;
				}
				else if(reps[l][j]==-1){
					reps[l][j]=songIndex[i];
					j=reps[l].length;
				}
			}
		}
		
		for (int i=0; i<ind; i++){
			Arrays.sort(reps[i]);
			for (int j=0; j<reps[i].length; j++){
				System.out.print(reps[i][j]+" ");
				if (reps[i][j]==-1){System.out.println("ALERT");}
			}
			System.out.println(reps[i].length);
		}
		repertoires=reps;
	}
	
	public double[][] calculateGeographicalDistances(){
		int i,j, k;
		coordinates=new double [sg.individualNumber][2];
		
		for (i=0; i<sg.individualSongs.length; i++){
			j=sg.individualSongs[i][0];
			String sx1=sg.songs[j].locationX;
			String sy1=sg.songs[j].locationY;
			System.out.println(i+" "+sx1+" "+sy1);
			if (sx1!=null){
				try{
					Integer xx1=Integer.parseInt(sx1.trim());
					coordinates[i][0]=xx1.intValue()*0.001;
				}
				catch(Exception e){}
			}
			if (sy1!=null){
				try{
					Integer yy1=Integer.parseInt(sy1.trim());
					coordinates[i][1]=yy1.intValue()*0.001;
				}
				catch(Exception e){}
			}
			//System.out.println(i+" "+coordinates[i][0]+" "+coordinates[i][1]);
		}
		double[][] geographicalDistances=new double[sg.individualNumber][sg.individualNumber];
		for (i=0; i<sg.individualNumber; i++){
			for (j=0; j<i; j++){
				for (k=0; k<2; k++){
					geographicalDistances[i][j]+=(coordinates[i][k]-coordinates[j][k])*(coordinates[i][k]-coordinates[j][k]);
				}
				geographicalDistances[i][j]=1000*Math.sqrt(geographicalDistances[i][j]);
			}
		}
		return geographicalDistances;
	}

	
	public double[][] calculateDistanceCategories(int numCategories, double[][] distances){
		
		double[][] distanceCategories=new double[2][numCategories];
		//double[] distanceAverages=new double[numCategories];
		double[] distanceSort=new double[sg.individualNumber*(sg.individualNumber-1)/2];
		int count=0;
		for (int i=0; i<sg.individualNumber; i++){
			for (int j=0; j<i; j++){
				distanceSort[count]=distances[i][j];
				count++;
			}
		}
		Arrays.sort(distanceSort);
		
		double cut=distanceSort.length/(numCategories+0.0);
		
		for (int i=0; i<numCategories; i++){
			int j=(int)Math.round((i+1)*cut-1);
			if (j>=distanceSort.length){j=distanceSort.length-1;}
			distanceCategories[0][i]=distanceSort[j];
			
		}
		
		double[] counter=new double[numCategories];
		for (int i=0; i<distanceSort.length; i++){
			for (int j=0; j<numCategories; j++){
				if (distanceSort[i]<=distanceCategories[0][j]){
					distanceCategories[1][j]+=distanceSort[i];
					counter[j]++;
					j=numCategories;
				}
			}
		}
		for (int i=0; i<numCategories; i++){
			distanceCategories[1][i]/=counter[i];
			//System.out.println("DCATS: "+i+" "+distanceCategories[1][i]+" "+distanceCategories[0][i]);
		}
				
		return distanceCategories;
	}
			
}
