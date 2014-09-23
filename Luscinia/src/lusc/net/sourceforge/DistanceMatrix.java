package lusc.net.sourceforge;
//
//  DistanceMatrix.java
//  Luscinia
//
//  Created by Robert Lachlan on 4/14/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

public class DistanceMatrix {

	
	//eventually this should replace all the unpleasant references to float[][]s throughout the analysis
	
	float[][] mat;
	
	int length;
	
	boolean isTriangular=true;
	
	public DistanceMatrix(){}
	
	
	public DistanceMatrix(int m, boolean isTriangular){
		
		length=m;
		this.isTriangular=isTriangular;
		
		
		if (isTriangular){
			mat=new float[m][];
			for (int i=0; i<m; i++){
				mat[i]=new float[i+1];
			}
		}
		else{
			mat=new float[m][m];
		}
	}
	
	public void makeTriangularMatrixFromSquareMatrix(float[][] dat){
		
		
		length=dat.length;
		
		mat=new float[length][];
		for (int i=0; i<length; i++){
			mat[i]=new float[i+1];
		}
		
		
		for (int i=0; i<length; i++){
			for (int j=0; j<i; j++){
				mat[i][j]=(float)(0.5*(dat[i][j]+dat[j][i]));
			}
		}
	}
	
	public void correlateDistanceMatrices(DistanceMatrix mat){
		
		
		
	}
		
		
	
	
	
}
