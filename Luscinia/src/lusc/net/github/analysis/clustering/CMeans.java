package lusc.net.github.analysis.clustering;
//
//  CMeans.java
//  Luscinia
//
//  Created by Robert Lachlan on 11/16/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

import java.util.*;

public class CMeans {
	
	
	float[][] data, centroidsArray;
	
	double[][] membershipArray;
	
	
	float exponent1, exponent2;
	
	int n,m, kg;
	
	float[] max, min, mean;
	
	double errorThreshold=0.000001;
	int numReseeds=100;
	
	double[][][] fuzzyAssignments;
	
	
	Random random=new Random(System.currentTimeMillis());
	
	
	public CMeans(float[][] data, int maxK){ 
		this.data=data;
		n=data.length;
		m=data[0].length;
		
		exponent2=1.5f;
		exponent1=2/(exponent2-1f);
		
		summarizeData();
		
		fuzzyAssignments=new double[maxK+1][][];
		
		
		for (int i=2; i<=maxK; i++){
			
			double bestFit=1000000000;
			for (int j=0; j<numReseeds; j++){
				iterateAlgorithm(i);
				double x=calculateFit();
				if (x<bestFit){
					fuzzyAssignments[i]=membershipArray;
					bestFit=x;
				}
			}
			//printAssignments();
		}
	}
	
	
	public void printAssignments(){
		
		
		for (int i=0; i<membershipArray.length; i++){
			for (int j=0; j<membershipArray[i].length; j++){
				System.out.print(membershipArray[i][j]+" ");
			}
			System.out.println();
		}
		System.out.println();
	}
			
	
	public void iterateAlgorithm(int k){
		
		kg=k;
		centroidsArray=inititialize(k);
		
		double er=1;
		
		while (er>errorThreshold){
			
			calculateMembership();
			er=updateCentroids();
			
			//System.out.println(k+" "+er);

		}
	}
	
	
	
	
	
	public void summarizeData(){
	
		max=new float[m];
		min=new float[m];
		mean=new float[m];
		
		for (int i=0; i<m; i++){
			max[i]=-10000f;
			min[i]=10000f;
		}
		
		for (int i=0; i<n; i++){
			for (int j=0; j<m; j++){
				if (data[i][j]>max[j]){
					max[j]=data[i][j];
				}
				if (data[i][j]<min[j]){
					min[j]=data[i][j];
				}
				mean[j]+=data[i][j];
			}
		}
		
		
		for (int i=0; i<m; i++){
			mean[i]/=m+0f;
		}
	}
	
	
	public float[][] inititialize(int k){
	
		float[][] results=new float[k][m];
		
		for (int i=0; i<k; i++){
			for (int j=0; j<m; j++){
				results[i][j]=min[j]+(random.nextFloat()*(max[j]-min[j]));
			}
		}
		
		membershipArray=new double[n][k];
		
		return results;
	}
			
			
		
		
	public void calculateMembership(){
		
		double s,t,u;
		
		double[] a=new double[kg];
		
		
		for (int i=0; i<n; i++){
			for (int k=0; k<kg; k++){
				t=0;
				for (int j=0; j<m; j++){
					s=centroidsArray[k][j]-data[i][j];
					t+=s*s;
				}
				a[k]=Math.sqrt(t);
			}
			
			
			for (int j=0; j<kg; j++){
				u=0;
				for (int k=0; k<kg; k++){
					u+=Math.pow(a[j]/a[k], exponent1);
				}
				membershipArray[i][j]=1/u;
			}
		}
	}
	
	
	public double updateCentroids(){
		
		double s, t, u, v;
		double r=0;
		
		double[] temp=new double[n];
		
		for (int i=0; i<kg; i++){
			s=0;
			for (int j=0; j<n; j++){
				temp[j]=Math.pow(membershipArray[j][i], exponent2);
				s+=temp[j];
			}
			v=0;
			for (int j=0; j<m; j++){
				
				u=centroidsArray[i][j];
				
				t=0;
				for (int k=0; k<n; k++){
					t+=temp[k]*data[k][j];
				}
				centroidsArray[i][j]=(float)(t/s);
				
				u-=centroidsArray[i][j];
				v+=u*u;
			}
			r+=Math.sqrt(v);
		}
		return (r/(n+0.0));
	}
	
	public double calculateFit(){
		
		double score=0;
		double t,s;
		
		for (int i=0; i<n; i++){

			
			for (int j=0; j<kg; j++){
				
				t=0;
				for (int k=0; k<m; k++){
					s=centroidsArray[j][k]-data[i][k];
					t+=s*s;
				}
			
				score+=membershipArray[i][j]*Math.sqrt(t);
			}
		}
		
		return score;
	}
				
				
			
			
			
			
			
	
	
	
	
	
	
	
	
	

}
