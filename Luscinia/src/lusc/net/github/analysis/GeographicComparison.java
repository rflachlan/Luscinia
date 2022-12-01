package lusc.net.github.analysis;
//
//  GeographicComparison.java
//  Luscinia
//
//  Created by Robert Lachlan on 2/13/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//

import java.util.*;

import lusc.net.github.analysis.dendrograms.UPGMA;
import lusc.net.github.ui.GeographicAnalysisPreferences;
import lusc.net.github.ui.statistics.DisplayUPGMA;

public class GeographicComparison {
	
	
	double[] meanScore, withinCatMeanScore;
	double[][] meanScoreByGroup;
	double[][] confidenceIntervals;
	
	double[][] geographicalDistances;
	double[][] distanceCategories;
	double[][] repertoireComparison;
	double[][] coordinates;
	
	double[][][] withinTypeDists;
	
	
	int[][] repertoires, repertoires2;
	int dataType;
	int numCategories=0;
	int numTypes=0;
	int numComps=0;
	int numInds=0;
	
	int defaultCategoryNumber=10;
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
	//SongGroup sg;
	AnalysisGroup sg;
	ComparisonResults cr;
	
	int[] groupids;
	String[] levelNames, levelNames2;
	String[] populationNames;
	
	double thresholdDissimilarity=0.1;
	
	
	GeographicAnalysisPreferences gap;
	
	
	//public GeographicComparison(SongGroup sg, int dataType, doublse thresholdProp){
	public GeographicComparison(AnalysisGroup sg, int dataType, GeographicAnalysisPreferences gap){
		this.sg=sg;
		this.dataType=dataType;
		this.gap=gap;
		
		
		
		cr=sg.getScores(dataType);
		if ((cr.individuals==null)||(cr.individualNumber==0)){
			cr.calculateIndividuals();
		}
		
		int[] lookUps=cr.lookUpIndividual;
		
		int[] partition=null;
		int s=gap.levelSel;
		levelNames=null;
		if (s==0){
			partition=cr.getLookUpTypes();
			levelNames=cr.getTypeNames();
		}
		if (s==1){
			partition=cr.getSpeciesListArray();
			levelNames=cr.getSpeciesNames();
		}
		else if (s==2){
			partition=cr.getPopulationListArray();
			levelNames=cr.getPopulationNames();
		}
		else if (s==3){
			partition=cr.getLookUpIndividuals();
			levelNames=cr.getIndividualNames();
		}		
		
		if (levelNames.length>1){
			
			int[] ind=cr.getLookUpIndividuals();
			
			int ni=cr.getIndividualNames().length;
			
			groupids=new int[ni];
			
			for (int i=0; i<ind.length; i++){
				groupids[ind[i]]=partition[i];
			}
			
		}
		
		for (int i=0; i<partition.length; i++) {
			System.out.println(levelNames[partition[i]]);
		}
		
		
		numCategories=defaultCategoryNumber;
		geographicalDistances=calculateGeographicalDistances();
		
		doRepComparison(numCategories, thresholdDissimilarity);
		
		doWithinCatRepComparison(numCategories, thresholdDissimilarity);
		
		//doWithinTypeComparison(geographicalDistances, numTypes);
		
		

		//doComparison(numCategories);
		
		
		
		
	}
	
	//public GeographicComparison(SongGroup sg, int dataType, DisplayUPGMA dup){
	public GeographicComparison(AnalysisGroup sg, int dataType, DisplayUPGMA dup, GeographicAnalysisPreferences gap){
		
		this.sg=sg;
		this.dataType=dataType;
		this.upgma=dup.getUPGMA();
		this.gap=gap;
		cr=sg.getScores(dataType);
		
		if ((cr.individuals==null)||(cr.individualNumber==0)){
			cr.calculateIndividuals();
		}
		numInds=cr.individualNumber;		
		numCategories=defaultCategoryNumber;
		numComps=(cr.individualNumber*(cr.individualNumber-1))/2;
		if (numCategories>numComps){numCategories=numComps;}
		

		double bestSil=-1;
		int silind=-1;
		int ds2=dup.getSize1()/2;
		double[][] avsils=dup.getAvSils();
		for (int i=0; i<ds2; i++){
			int ii=avsils[0].length-i-1;
			if (avsils[0][ii]>bestSil){
				bestSil=avsils[0][ii];
				silind=ii;
			}
		}
		silind=dup.getSize1()-silind;
		numTypes=silind+1;
		
		
		
		int[] partition=null;
		int s=gap.levelSel;
		levelNames=null;
		if (s==0){
			partition=cr.getLookUpTypes();
			levelNames=cr.getTypeNames();
		}
		if (s==1){
			partition=cr.getSpeciesListArray();
			levelNames=cr.getSpeciesNames();
		}
		else if (s==2){
			partition=cr.getPopulationListArray();
			levelNames=cr.getPopulationNames();
		}
		else if (s==3){
			partition=cr.getLookUpIndividuals();
			levelNames=cr.getIndividualNames();
		}		
		
		if (levelNames.length>1){
			
			int[] ind=cr.getLookUpIndividuals();
			
			int ni=cr.getIndividualNames().length;
			
			groupids=new int[ni];
			
			for (int i=0; i<ind.length; i++){
				groupids[ind[i]]=partition[i];
			}
			
		}
		
		//System.out.println(levelNames.length);
		geographicalDistances=calculateGeographicalDistances();
		doComparison(numCategories, numTypes);
		
		doWithinCatComparison(numCategories, numTypes);
		
		doWithinTypeComparison(geographicalDistances, numTypes);
		
	}
	
	public int getDataType(){
		return dataType;
	}
	
	public int getNumInds(){
		return numInds;
	}
	
	public int getNumCategories(){
		return numCategories;
	}
	
	public int getNumTypes(){
		return numTypes;
	}
	
	public int getNumComps(){
		return numComps;
	}
	
	public double[][] getConfidenceIntervals(){
		return confidenceIntervals;
	}
	
	public double[][] getDistanceCategories(){
		return distanceCategories;
	}
	
	public double getThresholdDissimilarity() {
		return thresholdDissimilarity;
	}
	
	public double[] getMeanScore(){
		return meanScore;
	}
	
	public double[] getWithinCatMeanScore(){
		return withinCatMeanScore;
	}
	
	public double[][][] getWithinTypeMatrix(){
		return withinTypeDists;
	}
	
	public double[][] getMeanScoreByGroup(){
		return meanScoreByGroup;
	}
	
	public double[][] getCoordinates(){
		return coordinates;
	}
	
	public double[][] getGeographicalDistances(){
		return geographicalDistances;
	}
	
	public int[][] getRepertoires(){
		return repertoires;
	}
	
	public int[][] getRepertoires2(){
		return repertoires2;
	}
	
	public String[] getLevelNames() {
		return levelNames;
	}
	
	public String[] getLevelNames2() {
		return levelNames2;
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
		maxDist=distanceCategories[1][distanceCategories.length-1];
		
		
		meanScore=calculateMeanScore(repertoireComparison, false);
		for (int i=0; i<meanScore.length; i++){System.out.println(meanScore[i]);}
		confidenceIntervals=deleteNJackknife(prop, resamples);
	}
	
	public void doComparison(int distcats, int songcats){
		
		
		
		numCategories=distcats;
		int[] categories=calculateCategories(songcats);
		
		
		
		int[] lookUps=cr.lookUpIndividual;
		
		
		
		calculateRepertoires(categories, lookUps, cr.individualNumber);
		
		if (dataType<5){
			int[][] lookUps2=cr.getLookUp();
			calculateRepertoires2(categories, lookUps, lookUps2, cr.individualNumber);
		}
		
		repertoireComparison=calculateJaccardIndex(categories, lookUps, cr.individualNumber);
		
		
		distanceCategories=calculateDistanceCategories(distcats, geographicalDistances);
		maxDist=distanceCategories[distanceCategories.length-1][1];

		meanScore=calculateMeanScore(repertoireComparison, false);
		//for (int i=0; i<meanScore.length; i++){System.out.println(meanScore[i]);}
		confidenceIntervals=deleteNJackknife(prop, resamples);
		
		if (groupids!=null){
			meanScoreByGroup=calculateMeanScoreByGroup(groupids, levelNames.length);
		}
		
	}
	
	public void doRepComparison(int distcats, double thresh) {
		
		System.out.println(distcats+" "+thresh+" "+cr.individualNumber);
		
		numCategories=distcats;
		int[] lookUps=cr.lookUpIndividual;
		repertoireComparison=calculateJaccardIndex(thresh, lookUps, cr.individualNumber);
		
		distanceCategories=calculateDistanceCategories(distcats, geographicalDistances);
		maxDist=distanceCategories[distanceCategories.length-1][1];

		meanScore=calculateMeanScore(repertoireComparison, true);
		
		
		
		
		
		confidenceIntervals=deleteNJackknife(prop, resamples);
		
		delete1Jackknife();
		
		if (groupids!=null){
			meanScoreByGroup=calculateMeanScoreByGroup(groupids, levelNames.length);
		}
		
		
		
	}
	
public void doWithinCatRepComparison(int distcats, double threshold){
		
		numCategories=distcats;
		
		int[] lookUps=cr.lookUpIndividual;
		
		double[][] rc=calculateWithinCatDissimilarities(lookUps, cr.individualNumber, cr.getDiss(), threshold);
		
		
		withinCatMeanScore=calculateMeanScore(rc, true);
		
	}
	
	
	public void doWithinCatComparison(int distcats, int songcats){
		
		
		
		numCategories=distcats;
		int[] categories=calculateCategories(songcats);
		
		
		
		int[] lookUps=cr.lookUpIndividual;
		
		
		
		calculateRepertoires(categories, lookUps, cr.individualNumber);
		
		if (dataType<5){
			int[][] lookUps2=cr.getLookUp();
			calculateRepertoires2(categories, lookUps, lookUps2, cr.individualNumber);
		}
		
		double[][] rc=calculateWithinCatIndex(categories, lookUps, cr.individualNumber, cr.getDiss());
		
		
		withinCatMeanScore=calculateMeanScore(rc, false);
		
	}
	
	public void doWithinTypeComparison(double[][] geog, int songcats){
		

		
		int[] categories=calculateCategories(songcats);

		int[] lookUps=cr.lookUpIndividual;
			
		calculateRepertoires(categories, lookUps, cr.individualNumber);
		
		if (dataType<5){
			int[][] lookUps2=cr.getLookUp();
			calculateRepertoires2(categories, lookUps, lookUps2, cr.individualNumber);
		}
		
		double[][][]rc=new double[songcats+1][][];
		
		for (int i=0; i<=songcats; i++){
			rc[i]=calculateTypeMatrix(i, categories, lookUps,  cr.individualNumber, cr.getDiss(), geog);
		}
		
		withinTypeDists=rc;
		
		
	}
	
	
	
	public double[] calculateMeanScore(double[][] d, boolean square){
		
		int nc=distanceCategories[0].length;
		
		double[] results=new double[nc];
		double[] counts=new double[nc];
		
		
		
		
		for (int i=0; i<d.length; i++){
			int maxj=i;
			if (square) {maxj=d.length;}
			for (int j=0; j<maxj; j++){
				if (i!=j) {
					double p=0;
					for (int k=0; k<nc; k++){
						if ((geographicalDistances[i][j]>p)&&(geographicalDistances[i][j]<=distanceCategories[0][k])&&(d[i][j]>=0)){
							results[k]+=d[i][j];
							counts[k]++;
						}
						p=distanceCategories[0][k];
					}
				}
			}
		}
		
		for (int i=0; i<nc; i++){
			if (counts[i]>0){
				results[i]/=counts[i];
			}
			System.out.println(counts[i]+" "+results[i]);
		}
		
		return results;
	}
	
	public double[][] calculateMeanScoreByGroup(int[] groups, int ngroups){
		
		//System.out.println("calculating mean score by group "+ngroups);
		
		
		
		int ncomps=ngroups*(ngroups+1)/2;
		int lookups[][]=new int[ngroups][ngroups];
		int a=0;
		for (int i=0; i<ngroups; i++) {
			for (int j=0; j<=i; j++) {
				lookups[i][j]=a;
				lookups[j][i]=a;
				a++;
			}
		}
		
		levelNames2=new String[ncomps];
		int kk=0;
		for (int i=0; i<levelNames.length; i++) {
			for (int j=0; j<=i; j++) {
				levelNames2[kk]=levelNames[i]+" , "+levelNames[j];	
				kk++;
			}
		}
		
		
		
		double[][] results=new double[ncomps][numCategories];
		double[][] counts=new double[ncomps][numCategories];
		
		for (int i=0; i<repertoireComparison.length; i++){
			//System.out.println(groups[i]);
			for (int j=0; j<i; j++){
				//if (groups[i]==groups[j]){
					double p=0;
					for (int k=0; k<numCategories; k++){
						if ((geographicalDistances[i][j]>p)&&(geographicalDistances[i][j]<=distanceCategories[0][k])){
							int r=lookups[groups[i]][groups[j]];
							//results[groups[i]][k]+=repertoireComparison[i][j];
							//counts[groups[i]][k]++;
							results[r][k]+=repertoireComparison[i][j];
							counts[r][k]++;
						}
						p=distanceCategories[0][k];
					}
				//}
			}
		}
		
		for (int i=0; i<ncomps; i++){
			for (int j=0; j<numCategories; j++){
				if (counts[i][j]>0){
					results[i][j]/=counts[i][j];		
				}
				//System.out.println(i+" "+j+" "+results[i][j]);
			}
		}
		
		return results;
	}
	
	public void delete1Jackknife() {
		int n=repertoireComparison.length;
		boolean[] delete=new boolean[n];
		double[] results=new double[numCategories];
		double[] counts=new double[numCategories];
		
		double[][] resampleResults=new double[numCategories][resamples];
		boolean[][] fs=new boolean[numCategories][resamples];
		
		for (int r=0; r<n; r++) {
			for (int i=0; i<n; i++){
				delete[i]=false;
			}
			delete[r]=true;
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
			System.out.print(r+" ");
			for (int i=0; i<numCategories; i++){
				if (counts[i]==0){
					counts[i]=1;
					fs[i][r]=false;
				}
				else{
					fs[i][r]=true;
				}
				resampleResults[i][r]=results[i]/counts[i];
				System.out.print(resampleResults[i][r]+" ");
			}
			System.out.println();
			
		}
		
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
		
		//System.out.println(n+" "+deleteNumber+" "+resamples);
		for (int i=0; i<numCategories; i++){
			double p=sumsquares[i]*(n-deleteNumber)/(deleteNumber*rc[i]+0.0);
			p=Math.sqrt(p);
			overallResults[0][i]=means[i]-p;
			overallResults[1][i]=means[i]+p;
		}
		
		return overallResults;
	}
	
	
	public double[][] calculateJaccardIndex(double[][] songData, int[] lookUps, double threshold){
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
		double[] counts=new double[cr.individualNumber];
		for (int i=0; i<n; i++){
			if(!leaveOut[i]){
				counts[lookUps[i]]++;
			}
		}
		double[][] results=new double[cr.individualNumber][];
		for (int i=0; i<cr.individualNumber; i++){
			results[i]=new double[i+1];
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
		for (int i=0; i<cr.individualNumber; i++){
			for (int j=0; j<i; j++){
				//System.out.println(i+" "+j+" "+results[i][j]+" "+counts[i]+" "+counts[j]);
				results[i][j]/=counts[i]+counts[j]-results[i][j];
				//System.out.println(results[i][j]);
			}
		}
		return results;
	}
	
	public double[][] calculateJaccardIndex(double thresh, int[] lookUps, int ind){
		int n=lookUps.length;
		
		
		int[][] lu=new int[ind][];
		for (int i=0; i<ind; i++) {
			int c=0;
			for (int j=0; j<lookUps.length; j++) {
				if (lookUps[j]==i) {
					c++;
				}
			}
			lu[i]=new int[c];
			c=0;
			for (int j=0; j<lookUps.length; j++) {
				if (lookUps[j]==i) {
					lu[i][c]=j;
					c++;
				}
			}
			
		}
		
		double[][] out=new double[ind][ind];
		
		double[][]x=cr.getDiss();
		
		for (int i=0; i<ind; i++) {
			for (int j=0; j<ind; j++) {
				double count=0;
				for (int a=0; a<lu[i].length; a++) {
					for (int b=0; b<lu[j].length; b++) {
						int c=lu[i][a];
						int d=lu[j][b];
						
						if (c<d) {
							c=lu[j][b];
							d=lu[i][a];
						}
						
						if (x[c][d]<thresh) {
							count++;
							b=lu[j].length;	
						}
					}
				}
				out[i][j]=count/(lu[i].length+lu[j].length-count);
				
				//System.out.println(i+" "+j+" "+count+" "+lu[i].length+" "+lu[j].length+" "+out[i][j]);
				
				
			}
		}
		return out;
	}
	
	public double[][] calculateJaccardIndex(int[] songIndex, int[] lookUps, int ind){
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
				//System.out.print(reps[i][j]+" ");
				if (reps[i][j]==-1){System.out.println("ALERT");}
			}
			//System.out.println(reps[i].length);
		}
		
		double[][] results=new double[ind][];
		for (int i=0; i<ind; i++){
			results[i]=new double[i+1];
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
	
	public double[][] calculateWithinCatDissimilarities(int[] lookUps, int ind, double[][] d, double thresh){
		int n=lookUps.length;
		
		double[][] sim=new double[ind][ind];
		double[][] counts=new double[ind][ind];
		
		for (int i=0; i<n; i++){
			for (int j=0; j<n; j++){
				if (i>=j) {
					if (d[i][j]<thresh) {
						sim[lookUps[i]][lookUps[j]]+=d[i][j];
						counts[lookUps[i]][lookUps[j]]++;
					}	
				}
			}
		}
	
		double[][] results=new double[ind][];
		for (int i=0; i<ind; i++){
			results[i]=new double[i+1];
			for (int j=0; j<i; j++){
				if (counts[i][j]>0){
					results[i][j]=sim[i][j]/counts[i][j];
				}
				else{
					results[i][j]=-1;
				}
			}
		}	
	
		return results;
	}
	
	
	
	public double[][] calculateWithinCatIndex(int[] songIndex, int[] lookUps, int ind, double[][] d){
		int n=lookUps.length;
		
		double[][] sim=new double[ind][ind];
		double[][] counts=new double[ind][ind];
		
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				if (songIndex[i]==songIndex[j]){
					sim[lookUps[i]][lookUps[j]]+=d[i][j];
					counts[lookUps[i]][lookUps[j]]++;
				}
			}
		}
	
		double[][] results=new double[ind][];
		for (int i=0; i<ind; i++){
			results[i]=new double[i+1];
			for (int j=0; j<i; j++){
				if (counts[i][j]>0){
					results[i][j]=sim[i][j]/counts[i][j];
				}
				else{
					results[i][j]=-1;
				}
			}
		}	
	
		return results;
	}
	
	public double[][] calculateTypeMatrix(int a, int[] songIndex, int[] lookUps, int numInds, double[][] diss, double[][] geog){
		int n=lookUps.length;
		int[] indl=new int[numInds];
		
		double[][] t=new double[numInds][numInds];
		double[][] c=new double[numInds][numInds];
		
		for (int i=0; i<n; i++){
			if (songIndex[i]==a){
				int g=lookUps[i];
				indl[g]=1;
				for (int j=0; j<i; j++){
					if (songIndex[i]==songIndex[j]){
						int h=lookUps[j];
						t[g][h]+=diss[i][j];
						c[g][h]++;
						t[h][g]+=diss[i][j];
						c[h][g]++;
					}
				}
			}
		}
		
		for (int i=0; i<numInds; i++){
			for (int j=0; j<numInds; j++){
				if (c[i][j]>0){t[i][j]/=c[i][j];}
			}
		}	
		
		int countInds=0;
		for (int i=0; i<numInds; i++){
			countInds+=indl[i];
		}
		
		int[] trans=new int[countInds];
		countInds=0;
		for (int i=0; i<numInds; i++){
			if (indl[i]==1){
				trans[countInds]=i;
				countInds++;
			}
		}
		
		double[][] out=new double[countInds*(countInds-1)/2][4];
		
		int x=0;
		for (int i=0; i<countInds; i++){
			for (int j=0; j<i; j++){
				out[x][0]=trans[i];
				out[x][1]=trans[j];
				out[x][2]=t[trans[i]][trans[j]];
				out[x][3]=geog[trans[i]][trans[j]];
				x++;
			}
		}	
		
		return out;
	}
	
	public double[][] calculateJaccardIndex2(int[] songIndex, int[] lookUps, int ind){
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
		double[] counts=new double[ind];
		for (int i=0; i<n; i++){
			if(!leaveOut[i]){
				counts[lookUps[i]]++;
			}
		}
		double[][] results=new double[ind][];
		for (int i=0; i<ind; i++){
			results[i]=new double[i+1];
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
		
	
	public double[][] calculateRepertoireSimilarity(double[][] songData, int[] lookUps){
		
		int n=lookUps.length;
		
		//System.out.println("THRESHOLD: "+uberthresh);
		
		boolean[] leaveOut=new boolean[n];

		double[] counts=new double[cr.individualNumber];
		
		
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
		
		double[][] pt=new double[n][cr.individualNumber];
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
				
		double[][] results=new double[cr.individualNumber][];
		for (int i=0; i<cr.individualNumber; i++){
			results[i]=new double[i+1];
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
		
		for (int i=0; i<cr.individualNumber; i++){
			for (int j=0; j<i; j++){
				if (results[i][j]>Math.min(counts[i],  counts[j])){results[i][j]=Math.min(counts[i],  counts[j]);}
				results[i][j]/=(counts[i]+counts[j]-results[i][j]);
			}			
		}
		
		return results;
	}
	
	public double[][] calculateRepertoireSimilarity2(double[][] songData, int[] lookUps){
		
		int n=lookUps.length;
		
				
		//boolean[] leaveOut=new boolean[n];
		
		
		//double threshold1=calculateThreshold(songData, thresholdProportion1);
		//double threshold2=calculateThreshold(songData, thresholdProportion2);
		//System.out.println("THRESHOLD: "+uberthresh);
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
		double[] counts=new double[cr.individualNumber];
		for (int i=0; i<n; i++){
			//if(!leaveOut[i]){
				counts[lookUps[i]]++;
			//}
		}
		
		//double[][] ps=new double[n][cr.individualNumber];
		double[][] pt=new double[cr.individualNumber][cr.individualNumber];
		for (int i=0; i<n; i++){
			//if (!leaveOut[i]){
				//for (int j=0; j<cr.individualNumber; j++){
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
		
		double[][] results=new double[cr.individualNumber][];
		for (int i=0; i<cr.individualNumber; i++){
			results[i]=new double[i+1];
		}
		for (int i=0; i<cr.individualNumber; i++){
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
	
	public double calculateThreshold(double[][] data, double prop){
		int n=data.length;
		int m=n*(n-1)/2;
		double[] d=new double[m];
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
	
	public void gateDistances(double[][] data, double t1, double t2){
		
		for (int i=0; i<data.length; i++){
			for (int j=0; j<data[i].length; j++){
				data[i][j]=(double)((data[i][j]-t1)/(t2-t1));
				if (data[i][j]<0){data[i][j]=0;}
				if (data[i][j]>1){data[i][j]=1;}
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
				//System.out.print(reps[i][j]+" ");
				if (reps[i][j]==-1){System.out.println("ALERT");}
			}
			//System.out.println(reps[i].length);
		}
		repertoires=reps;
	}
	
	public void calculateRepertoires2(int[] songIndex, int[] lookUps, int[][] lookUps2, int ind){
		int n=lookUps.length;
		int[][] reps=new int[ind][];
		int[] counts=new int[ind];
		for (int i=0; i<n; i++){
			counts[lookUps[i]]++;		
			boolean matched=false;
			for (int j=0; j<i; j++){
				if (lookUps2[i][0]==lookUps2[j][0]){
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
				if(reps[l][j]==-1){
					reps[l][j]=songIndex[i];
					
					boolean matched=false;
					for (int k=i+1; k<n; k++){
						if (lookUps2[i][0]==lookUps2[k][0]){
							matched=true;
							k=n;
						}
					}
					if (!matched){reps[l][j+1]=-100;}
					
					j=reps[l].length;
				}
			}
		}
		
		repertoires2=reps;
	}
	
	public double haversinAlgorithm(double lat1, double lon1, double lat2, double lon2){
		double R = 6371000; // metres
		double ph1 = Math.toRadians(lat1);
		double ph2 = Math.toRadians(lat2);
		double deltph = Math.toRadians(lat2-lat1);
		double deltlam = Math.toRadians(lon2-lon1);

		//double a = Math.sin(deltph/2) * Math.sin(deltph/2) +
		//        Math.cos(ph1) * Math.cos(ph2) *
		 //       Math.sin(deltlam/2) * Math.sin(deltlam/2);
		//double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

		//double d = R * c;
		//return d;
		
		double a = Math.pow(Math.sin(deltph / 2), 2)
                + Math.cos(ph1) * Math.cos(ph2)
                * Math.pow(Math.sin(deltlam / 2),2);
            
       double c = 2 * Math.asin(Math.sqrt(a));
       
       return (c*R);
				
		
	}
	
	public double[][] calculateGeographicalDistances(){
		int i,j, k;
		coordinates=new double [cr.individualNumber][2];
		String[] indname=cr.getIndividualNames();
		for (i=0; i<cr.individuals.length; i++){
			j=cr.lookUps[cr.individuals[i][0]][0];
			String sx1=cr.songs[j].getLocationX();
			String sy1=cr.songs[j].getLocationY();
			//System.out.println(i+" "+sx1+" "+sy1);
			if (sx1!=null){
				try{
					//Integer xx1=Integer.parseInt(sx1.trim());
					Double xx1=Double.parseDouble(sx1.trim());
					//coordinates[i][0]=xx1.intValue()*0.001;
					coordinates[i][0]=xx1.doubleValue();
				}
				catch(Exception e){}
			}
			if (sy1!=null){
				try{
					//Integer yy1=Integer.parseInt(sy1.trim());
					Double yy1=Double.parseDouble(sy1.trim());
					//coordinates[i][1]=yy1.intValue()*0.001;
					coordinates[i][1]=yy1.doubleValue();
				}
				catch(Exception e){}
			}
			//System.out.println(i+" "+groupids[i]+" "+indname[i]+" "+coordinates[i][0]+" "+coordinates[i][1]);
		}
		double[][] geographicalDistances=new double[cr.individualNumber][cr.individualNumber];
		for (i=0; i<cr.individualNumber; i++){
			for (j=0; j<i; j++){
				//for (k=0; k<2; k++){
					//geographicalDistances[i][j]+=(coordinates[i][k]-coordinates[j][k])*(coordinates[i][k]-coordinates[j][k]);
				//}
				//geographicalDistances[i][j]=1000*Math.sqrt(geographicalDistances[i][j]);
				geographicalDistances[i][j]=haversinAlgorithm(coordinates[i][0], coordinates[i][1], coordinates[j][0], coordinates[j][1]);
			}
		}
		return geographicalDistances;
	}
	
	public double[][] calculateDistanceCategories(int numCategories, double[][] distances){
		
		int[] dists= {100,200,400,800,1600,3200,6400,12800,25600,51200,100000, 200000,400000,800000,1600000,3200000,6400000, 10000000,20000000,40000000,80000000};
		if (numCategories>=dists.length) {numCategories=dists.length-1;}
		
		double[][] distanceCategories=new double[3][numCategories];
		for (int i=0; i<numCategories; i++) {
			distanceCategories[0][i]=dists[i];
			distanceCategories[1][i]=dists[i];
			distanceCategories[2][i]=dists[i];	
		}
		return distanceCategories;
		
	}

	
	public double[][] calculateDistanceCategoriesS(int numCategories, double[][] distances){
		
		double[][] distanceCategories=new double[3][numCategories];
		//double[] distanceAverages=new double[numCategories];
		double[] distanceSort=new double[cr.individualNumber*(cr.individualNumber-1)/2];
		int count=0;
		for (int i=0; i<cr.individualNumber; i++){
			for (int j=0; j<i; j++){
				distanceSort[count]=distances[i][j];
				count++;
			}
		}
		Arrays.sort(distanceSort);
		
		double cut=distanceSort.length/(numCategories+0.0);
		
		System.out.println("Distance categories: "+numCategories+" "+distanceSort.length+" "+cut);
		
		int[] xc=new int[numCategories];
		for (int i=0; i<numCategories; i++){
			int j=(int)Math.round((i+1)*cut-1);
			if (j>=distanceSort.length){j=distanceSort.length-1;}
			distanceCategories[0][i]=distanceSort[j];
			int xc1=(int)Math.round(i*cut-1);
			xc[i]=j-xc1;
			
			System.out.println(xc[i]);
			
		}
		
		double[] counter=new double[numCategories];
		
		for (int i=0; i<numCategories; i++){
			if (xc.length>0) {
				double[] p=new double[xc[i]];
				int pi=0;
			
				double t2=distanceCategories[0][i];
				double t1=0;
				if (i>0){t1=distanceCategories[0][i-1];}
			
				for (int j=0; j<distanceSort.length; j++){
					if ((distanceSort[j]<=t2)&&(distanceSort[j]>t1)){
						p[pi]=distanceSort[j];
						pi++;
						if (pi>=p.length){pi--;}
					}
				}
				Arrays.sort(p);
				int q=(int)Math.ceil(xc[i]*0.5);
				if (q>=p.length) {
					q=p.length-1;
				}
				distanceCategories[1][i]=p[q];
			}
		}
		
		for (int i=0; i<distanceSort.length; i++){
			for (int j=0; j<numCategories; j++){
				if (distanceSort[i]<=distanceCategories[0][j]){
					distanceCategories[2][j]+=distanceSort[i];
					counter[j]++;
					j=numCategories;
				}
			}
		}
		for (int i=0; i<numCategories; i++){
			distanceCategories[2][i]/=counter[i];
			//System.out.println("DCATS: "+i+" "+distanceCategories[1][i]+" "+distanceCategories[0][i]);
		}
				
		return distanceCategories;
	}
			
}
