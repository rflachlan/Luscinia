package lusc.net.github.analysis;
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
	
	double[] labels;
	String[] names;
	int[][] lookUp;
	int[][] individual;
	
	int length;
	int type;
	
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
	
	public void DistanceMatrix(float[][] mat){
		this.mat=mat;
		length=mat.length;
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
	
	public float[][] getMat(){
		return mat;
	}
	
	public void setMat(float[][] a){
		mat=a;
	}
	
	public double[] getLabels(){
		return labels;
	}
	
	public void setLabels(double[] a){
		labels=a;
	}
	
	public String[] getNames(){
		return names;
	}
	
	public void setNames(String[] a){
		names=a;
	}
	
	public int[][] getLookUp(){
		return lookUp;
	}
	
	public void setLookUp(int[][] a){
		lookUp=a;
	}
	
	
	
}
