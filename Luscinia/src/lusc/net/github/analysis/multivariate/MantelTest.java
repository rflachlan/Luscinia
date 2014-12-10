package lusc.net.github.analysis.multivariate;
//
//  MantelTest.java
//  Luscinia
//
//  Created by Robert Lachlan on 1/25/07.
//  Copyright 2007 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import cern.colt.*;
import cern.colt.matrix.*;
import cern.colt.matrix.linalg.*;
import java.util.*;

public class MantelTest {

	DoubleMatrix2D data1, data2;
	int n;
	Random random=new Random(System.currentTimeMillis());
	Algebra alg=new Algebra();

	public MantelTest(double[][] d1, double[][]d2){
	
		DoubleFactory2D df=DoubleFactory2D.dense;
		data1=df.make(d1);
		data2=df.make(d2);
		n=d1.length;
		
	}
	
	public MantelTest(float[][] d1, float[][]d2){
		n=d1.length;
	
		DoubleFactory2D df=DoubleFactory2D.dense;
		data1=df.make(n, n);
		data2=df.make(n, n);
		for (int a=0; a<n; a++){
			for (int b=0; b<n; b++){
				data1.setQuick(a, b, (double)d1[a][b]);
				data2.setQuick(a, b, (double)d2[a][b]);
			}
		}
	}
	
	public MantelTest(float[][] d1, double[]d2){
		n=d1.length;
	
		DoubleFactory2D df=DoubleFactory2D.dense;
		data1=df.make(n, n);
		data2=df.make(n, n);
		for (int a=0; a<n; a++){
			for (int b=0; b<n; b++){
				double nd=d2[a]-d2[b];
				nd=Math.sqrt(nd*nd);
				//nd=Math.abs(nd);
				//nd=-1;
				//if (d2[a]==d2[b]){
				//	nd=1;
				//}
				
				if (nd>0.1){nd=1;}
				else{nd=0;}

				
				
				
				double p=0;
				if ((a>b)&&(d1[a][b]>0)){
					//p=Math.sqrt(d1[a][b]);
					p=d1[a][b];
				}
				else if ((a<b)&&(d1[b][a]>0)){
					//p=Math.sqrt(d1[b][a]);
					p=d1[b][a];
				}
				//System.out.println(p);
				//if (p>3){p=3;}
				data1.setQuick(a, b, p);
				data2.setQuick(a, b, nd);
			}
		}
	}
	
	public DoubleMatrix2D createCMatrix(double b){
		DoubleFactory2D df=DoubleFactory2D.dense;
		DoubleMatrix2D dataC=df.make(n, n);
		for (int i=0; i<n; i++){
			for (int j=0; j<n; j++){
				double x=data1.getQuick(i, j);
				double y=data2.getQuick(i, j);
				double z=x-b*y;
				dataC.setQuick(i, j, z);
			}
		}
		return dataC;
	}
	
	public DoubleMatrix2D randomPermute(DoubleMatrix2D d){
		int[] code=new int[n];
		for (int i=0; i<n; i++){
			code[i]=i;
		}
		for (int i=0; i<n; i++){
			int j=random.nextInt(n-i);
			j+=i;
			int k=code[i];
			code[i]=code[j];
			code[j]=k;
		}
		
		DoubleMatrix2D dataP=alg.permute(d, code, code);
		return dataP;
	}
	
	public double averageMatrix(DoubleMatrix2D d){
	
		double sum=0;
		double count=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				sum+=d.getQuick(i, j);
				count++;
			}
		}
		return (sum/count);
	}
	
	public double sdMatrix(DoubleMatrix2D d, double average){
	
		double sum=0;
		double count=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				double p=d.getQuick(i, j)-average;
				sum+=p*p;
				count++;
			}
		}
		count--;
		return Math.sqrt(sum/count);
	}
	
	public void capAverage(){
		double av1=averageMatrix(data1);
		for (int i=0; i<n; i++){
			for (int j=0; j<=i; j++){
				double s=data1.getQuick(i, j);
				if (s>av1){s=av1;}
				data1.setQuick(i, j, s);
			}
		}
	}

	public DoubleMatrix2D standardizeMatrix(DoubleMatrix2D d, double av, double sd){
		double s=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<=i; j++){
				s=d.getQuick(i, j);
				s=(s-av)/sd;
				d.setQuick(i, j, s);
				d.setQuick(j, i, s);
			}
		}
		return d;
	}
	
	public DoubleMatrix2D thresholdMatrix(DoubleMatrix2D d, double threshold){
		double s=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<=i; j++){
				s=d.getQuick(i, j);
				if (s>threshold){s=1;}
				else{s=-1;}
				d.setQuick(i, j, s);
				d.setQuick(j, i, s);
			}
		}
		return d;
	}
	
	public double pearsonPMC(DoubleMatrix2D d1, DoubleMatrix2D d2){
		double n2=n*(n-1)*0.5;
		n2--;
		double s=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				s+=d1.getQuick(i, j)*d2.getQuick(i, j);
			}
		}
		//System.out.println(s+" "+n2+" "+n);
		return (s/n2);	
	}
		
	public void standardizeData(){
		double av1=averageMatrix(data1);
		double sd1=sdMatrix(data1, av1);
		data1=standardizeMatrix(data1, av1, sd1);
		System.out.println("norms: "+ av1+" "+sd1);
		//data1=thresholdMatrix(data1, -1.5);
		double av2=averageMatrix(data2);
		double sd2=sdMatrix(data2, av2);
		data2=standardizeMatrix(data2, av2, sd2);
	}
	
	public double getMainCorrelation(){
		return pearsonPMC(data1, data2);
	}
	
	public double[] runRandomPermutationAnalysis(int c){
		double[] results=new double[c];
		for (int i=0; i<c; i++){
			DoubleMatrix2D p=randomPermute(data2);
			results[i]=pearsonPMC(p, data2);
		}
		java.util.Arrays.sort(results);
		return results;
	}
	
	public double getLowerPValue(double score, double[] rankedPermutations){
		int sampleSize=rankedPermutations.length;
		double position=0;
		for (int i=0; i<sampleSize; i++){
			position=i;
			if (score<=rankedPermutations[i]){
				break;
			}
		}
		return position;
	}
	
	public double getUpperPValue(double score, double[] rankedPermutations){
		int sampleSize=rankedPermutations.length;
		double position=0;
		for (int i=sampleSize-1; i>=0; i--){
			position=i;
			if (score>=rankedPermutations[i]){
				break;
			}
		}
		return position;
	}

}
