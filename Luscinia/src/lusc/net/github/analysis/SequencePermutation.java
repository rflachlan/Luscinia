package lusc.net.github.analysis;
//
//  SequencePermutation.java
//  Luscinia
//
//  Created by Robert Lachlan on 2/5/07.
//  Copyright 2007 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import java.util.*;

public class SequencePermutation {

	float[][] data, expectedFreqs;
	int[] actualSequence;
	int[][] actualMatrix, contents;
	int n,m, n2;
	float totalFreqs;
	double threshold=0.5f;
	float endMisMatch=1.0f;
	double songCount=0;
	Random random=new Random(System.currentTimeMillis());

	public SequencePermutation(float[][] d, int[][] songStructure){
		n=d.length;
		n2=n+2;
		m=n;
		calculateDistanceMatrix(d);
		calculateActualSequence(songStructure);
		calculateContents();
		int[] s=new int[m];
		for (int i=0; i<m; i++){
			s[i]=i;
		}
		actualMatrix=calculateMatrix(s);
		calculateExpectedDistribution(actualMatrix);
	}
	
	public double calculateActualChiSquare(){
		return calculateChiSquare(actualMatrix);
	}
	
	public double[] carryOutSequencePermutation(int repeats){
		double[] permutationResults=new double[repeats];
		for (int i=0; i<repeats; i++){
			int[] sequence=permuteSequence();
			int[][] matrix=calculateMatrix(sequence);
			permutationResults[i]=calculateChiSquare(matrix);
		}
		Arrays.sort(permutationResults);
		return permutationResults;
	}
	
	public void calculateDistanceMatrix(float[][] d){
		data=new float[n2][n2];
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				data[i][j]=d[i][j];
				data[j][i]=d[i][j];
			}
		}
		for (int i=0; i<n; i++){
			data[n][i]=endMisMatch;
			data[i][n]=endMisMatch;
		}
		for (int i=0; i<n+1; i++){
			data[n+1][i]=endMisMatch;
			data[i][n+1]=endMisMatch;
		}
		/*
		for (int i=0; i<n2; i++){
			for (int j=0; j<n2; j++){
				System.out.print(Math.round(data[i][j])+" ");
			}
			System.out.println();
		}
		*/
	}
	
	public void calculateActualSequence(int[][] songStructure){
		//calculates sequence of TRANSITIONS, referring back to the data matrix
		
		int s=0;
		for (int i=0; i<n; i++){
			if ((i==0)||(songStructure[i][0]!=songStructure[i-1][0])){
				s++;		//calculates number of songs
			}
		}
		m=n+s*2;
		songCount=s;
		actualSequence=new int[m];
		int j=0;
		for (int i=0; i<n; i++){
			if ((i==0)||(songStructure[i-1][0]!=songStructure[i][0])){
				actualSequence[j]=n;
				j++;
				actualSequence[j]=i;
				j++;
			}
			else if ((i==n-1)||(songStructure[i+1][0]!=songStructure[i][0])){
				actualSequence[j]=i;
				j++;
				actualSequence[j]=n+1;
				j++;
			}
			else{
				actualSequence[j]=i;
				j++;
			}
		}
	}
	
	public void calculateExpectedDistribution(int[][] matrix){
	
		expectedFreqs=new float[n2][n2];
		long[] eleFreqs=new long[n2];
		for (int i=0; i<n2; i++){
			for (int j=0; j<n2; j++){
				//System.out.print(matrix[i][j]+" ");
				eleFreqs[i]+=matrix[i][j];
			}
			//System.out.println();
		}
		
		double tot=0;
		for (int i=0; i<n2; i++){
			tot+=eleFreqs[actualSequence[i]];
		}

		for (int i=0; i<n2; i++){
			for (int j=0; j<n2; j++){
				expectedFreqs[i][j]=(float)(eleFreqs[actualSequence[i]]*eleFreqs[actualSequence[j]]/tot);
			}
		}
	}
	
	public void calculateContents(){
	
		//calculates which syllables are within a certain threshold distance of each syllable
	
		contents=new int[n2][];
		for (int i=0; i<n2; i++){
			int count=0;
			for (int j=0; j<m; j++){
				int jj=actualSequence[j];
				if (data[i][jj]<threshold){
					count++;
				}
			}
			contents[i]=new int[count];
			count=0;
			for (int j=0; j<m; j++){
				int jj=actualSequence[j];
				if (data[i][jj]<threshold){
					contents[i][count]=jj;
					count++;
				}
			}
		}
	}
	
	public int[][] calculateMatrix(int[] sequence){
		int[][] matrix=new int[n2][n2];
		int i, j, k, ii, jj;
		int m1=m-1;
		
		for (i=0; i<m1; i++){
			ii=actualSequence[i];
			jj=actualSequence[i+1];
			for (j=0; j<contents[ii].length; j++){
				for (k=0; k<contents[jj].length; k++){
					matrix[contents[ii][j]][contents[jj][k]]++;
				}
			}
		}
		return matrix;
	}
	
	public double calculateChiSquare(int[][] matrix){
		double chisquare=0;
		double t=0;
		double tot=0;
		for (int i=0; i<m; i++){
			if (actualSequence[i]<=n){
				for (int j=0; j<m; j++){
					t=matrix[i][j]-expectedFreqs[i][j];
					chisquare+=t*t/expectedFreqs[i][j];
					tot++;
				}
			}
		}
		return chisquare/tot;
	}
	
	public float[][] calculateChiSquareDistribution(int[][] matrix){
		float[][] chisq=new float[m][m];
		float t=0;
		for (int i=0; i<m; i++){
			for (int j=0; j<m; j++){
				t=matrix[i][j]-expectedFreqs[i][j];
				chisq[i][j]=t*t/expectedFreqs[i][j];
				if (t<0){chisq[i][j]*=-1f;}
			}
		}
		return chisq;
	}
	
	public int[] permuteSequence(){
		int[] s=new int[m];
		for (int i=0; i<m; i++){
			s[i]=i;
		}
		for (int i=0; i<m; i++){
			if ((actualSequence[i]!=n)&&(actualSequence[i]!=n+1)){
				int p=random.nextInt(m-i);
				p+=i;
				while ((actualSequence[p]==n)||(actualSequence[p]==n+1)){
					p=random.nextInt(m-i);
					p+=i;
				}
				int temp=s[p];
				s[p]=s[i];
				s[i]=temp;
			}
		}
		return s;
	}
	
}
