package lusc.net.github;
//
//  KMeans.java
//  Luscinia
//
//  Created by Robert Lachlan on 12/23/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

public class KMeans {
	
	
	int[] assignments;
	float[][] centroids;
	
	
	
	public KMeans(float[][] vec, int[] initLabels){
		
		int n=0;
		for (int i=0; i<initLabels.length; i++){
			if (initLabels[i]>n){n=initLabels[i];}
		}
		System.out.println(n);
		n++;
		

		
		centroids=recomputeCentroids(vec, initLabels, n);

		
		boolean changed=true;
		
		while(changed){
			
			assignments=assignToCluster(vec, centroids);
			
			float[][] newC=recomputeCentroids(vec, assignments, n);
			
			changed=compareCentroids(centroids, newC);
			centroids=newC;
			
		}
		
		
		
	}
		
		
	public int[] assignToCluster(float[][] vec, float[][] centroids){
		
		
		int n=centroids.length;
		int m=vec.length;
		int z=centroids[0].length;
		int[] results=new int[m];
		
		
		for (int i=0; i<m; i++){
			double bestScore=1000000;
			int loc=-1;
			for (int j=0; j<n; j++){
				
				double score=0;
				
				for (int k=0; k<z; k++){
					score+=(vec[i][k]-centroids[j][k])*(vec[i][k]-centroids[j][k]);
				}
				score=Math.sqrt(score);
				if (score<bestScore){
					bestScore=score;
					loc=j;
				}
			}
			results[i]=loc;
		}
		return results;
	}
	
	public float[][] recomputeCentroids(float[][] vec, int[] assignments, int n){
		int m=vec.length;
		int z=vec[0].length;
		
		float[][] results=new float[n][z];
		float[] count=new float[n];
		
		
		for (int i=0; i<m; i++){
			for (int j=0; j<z; j++){
				results[assignments[i]][j]+=vec[i][j];
			}
			count[assignments[i]]++;
		}
		
		for (int i=0; i<n; i++){
			for (int j=0; j<z; j++){
				results[i][j]/=count[i];
			}
		}
		return results;
	}
	
	public boolean compareCentroids(float[][] oldC, float[][] newC){
		float r=0;
		for (int i=0; i<oldC.length; i++){
			for (int j=0; j<oldC[i].length; j++){
				r+=(oldC[i][j]-newC[i][j])*(oldC[i][j]-newC[i][j]);
			}
		}
		boolean result=true;
		if (r==0f){result=false;}
		
		return result;
	}
	
	public double[] calculatePrototypeDistances(float[][] vec){
		
		int m=vec.length;
		int n=centroids.length;
		int k=vec[0].length;
		
		double[] results=new double[m];
		
		for (int i=0; i<m; i++){
			
			double score=0;
			for (int j=0; j<k; j++){
				score+=(vec[i][j]-centroids[assignments[i]][j])*(vec[i][j]-centroids[assignments[i]][j]);
			}
			results[i]=Math.sqrt(score);
		}
		return results;
	}
	
	

}
