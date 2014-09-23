package lusc.net.sourceforge;
//
//  TreeDat.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/10/2005.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

public class TreeDat {

	int[]child=new int[1];
	int children=0;
	double dist=0;
	double dist2=0;
	double dist_arch;
	int medoid=0;
	int[] daughters={-1, -1};
	double xplace=0;
	double xrange=1;
	double xstart=0;
	double xrt;
	double xloc=0;
	double yloc=0;
	int parent=0;
	double colval=-1;
	
	public TreeDat(double dist, int[]child){
		this.dist=dist;
		this.child=child;
		children=1;
	}
	
	public TreeDat(double dist, int[]child1, int[] child2, int daughter1, int daughter2){
		this.dist=dist;
		dist_arch=dist;
		child=new int[child1.length+child2.length];
		int j=0;
		for (int i=0; i<child1.length; i++){child[j]=child1[i]; j++;}
		for (int i=0; i<child2.length; i++){child[j]=child2[i]; j++;}
		daughters[0]=daughter1;
		daughters[1]=daughter2;
		children=child.length;
	}
	
	public void identifyMedoid(float[][] dist){
		float bestscore=1000000f;
		int id=0;
		if (children>1){
			for (int i=0; i<children; i++){
				float score=0;
				for (int j=0; j<children; j++){
					if (i!=j){
						if (child[i]<child[j]){
							score+=dist[child[j]][child[i]];
						}
						else{
							score+=dist[child[i]][child[j]];
						}
					}
				}
				score/=children-1.0f;
				if (score<bestscore){
					bestscore=score;
					id=i;
				}
			}
		}
		System.out.println("MEDOID: "+child[id]);
		medoid=child[id];
	}
	
	public double calculateClusterAverage(double[] data){
		double score=0;
		for (int i=0; i<children; i++){
			score+=data[child[i]];
		}
		score/=children+0.0;
		return score;
	}


}
