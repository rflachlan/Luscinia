package lusc.net.github.analysis.multivariate;
//
//  MultiDimensionalScaling.java
//  Luscinia
//
//  Created by Robert Lachlan on 10/24/06.
//  Copyright 2006 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import org.ejml.*;
import org.ejml.data.*;
import org.ejml.factory.*;
import org.ejml.ops.*;

import lusc.net.github.analysis.BasicStatistics;
import lusc.net.github.ui.AnalysisSwingWorker;
import mdsj.*;

import java.util.*;

public class MultiDimensionalScaling {
	
		
	int n;
	int npcs;
		
	double[][] configuration;
	
	double[] percentExplained;
	double[] eigenValues;
	double[] sds;
	
	double stressFactor=0;
	
	int maxIter=10000;
	int maxTime=1000000;
	int threshold=4;
	Random random=new Random(System.currentTimeMillis());
	
	public MultiDimensionalScaling(){
		
	}
	
	public double[] getSDS(){
		return sds;
	}
	
	public double[][] getConfiguration(){
		return configuration;
	}
	
	public int getN(){
		return n;
	}
	
	public int getnpcs(){
		return npcs;
	}
	
	public double getStressFactor(){
		return stressFactor;
	}
	
	public double[] getPercentExplained(){
		return percentExplained;
	}
	
	public double[] getEigenValues(){
		return eigenValues;
	}
	
	public void RunNonMetricAnalysis(double[][] data, int anpcs, AnalysisSwingWorker asw){
		n=data.length;
		npcs=anpcs;
		if (npcs>n){npcs=n;}
		
		double[][] d=getSquareDistanceMatrix(data);
		double[][] d2=getQuasimetricMatrix(d, n);
		double[][] init=mds(d2, npcs);
		asw.progress();
		double[][] nmdsConfig=runNMDS(d, init, npcs);
		asw.progress();
		double[][] dt=getDissimilarityMatrix(nmdsConfig, n, npcs);
		configuration=PCA(nmdsConfig);
		asw.progress();
		d=getSquareDistanceMatrix(data);
		percentExplained=calculateCorrelation(configuration, d);
		calculateSDs(configuration, n, npcs);
	}
	
	public void RunMetricAnalysis(double[][] data, int anpcs, boolean fixTriangleInequality, boolean calculateSummaries){
		n=data.length;
		npcs=anpcs;
		if (npcs>n){npcs=n;}
		
		double[][] d=getSquareDistanceMatrix(data);
		double[][] d2=d;
		if (fixTriangleInequality){
			d2=getQuasimetricMatrix(d, n);
		}
		configuration=mds(d2, npcs);
		if (calculateSummaries){
			percentExplained=calculateCorrelation(configuration, d);
			calculateSDs(configuration, n, npcs);
		}
	}
	
	
	public double[][] runNMDS(double[][] d, double[][] pc1, int ndi){
		//NonMetricMultiDimensionalScaling nmds=new NonMetricMultiDimensionalScaling(d, pc1);
		//stressFactor=nmds.stressFactor;
		//double[][] nmpc=nmds.bestconfig;
		StressMinimization sm=calculateNMDS(d, pc1, ndi);
		stressFactor=sm.getNormalizedStress();
		double[][] nmpc=calculateNMPCs(sm);
		
		return nmpc;
	}
	
	
	StressMinimization calculateNMDS(double[][] d, double[][] init, int ndi){
		
		double[][] swapM=new double[init[0].length][init.length];
		for (int i=0; i<init[0].length; i++){
			for (int j=0; j<init.length; j++){
				swapM[i][j]=init[j][i];
			}
		}
		
		StressMinimization sm=new StressMinimization(d, swapM);
		sm.iterate(maxIter, maxTime, threshold);
		return sm;
	}
	
	double[][] calculateNMPCs(StressMinimization sm){
		double[][] t=sm.getPositions();
		int nt=t[0].length;
		int di=t.length;
		double[][] results=new double[nt][di];
		for (int i=0; i<nt; i++){
			for (int j=0; j<di; j++){
				results[i][j]=t[j][i];
			}
		}
		return results;
	}
	
	public double[][] mds(double[][] data, int ndim){
		int n=data.length;
		double[][] d=new double[n][n];
		double[][] config=new double[n][ndim];		//out is the result of the scaling
		
		double d_col[]=new double[n];			//next calculate column and row totals.
		double d_row[]=new double[n];
		double d_tot=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<n; j++){
				if (i!=j){
					d[i][j]=-0.5*(data[i][j]*data[i][j]);		//this line transforms the matrix 
					d_col[i]+=d[i][j];
					d_row[j]+=d[i][j];
					d_tot+=d[i][j];
				}
			}
		}
			 
		for (int i=0; i<n; i++){			//now turn row column totals into means
			d_col[i]/=n+0.0;
			d_row[i]/=n+0.0;
			d_col[i]*=d_col[i];
			d_row[i]*=d_row[i];
		}
		d_tot/=n*n+0.0;
		d_tot*=d_tot;
		for (int i=0; i<n; i++){
			for (int j=0; j<n; j++){
				d[i][j]=d[i][j]-d_col[i]-d_row[j]+d_tot;
			}
		}
		long st=System.currentTimeMillis();
		
		DenseMatrix64F D = new DenseMatrix64F(d);
		EigenDecomposition<DenseMatrix64F> ed=DecompositionFactory.eig(n, true, true);

		if( !ed.decompose(D)){
			   throw new RuntimeException("Cholesky failed!");}
		
		//System.out.println("Eigenvalues");
		
		double[] eig=new double[n];
		for (int i=0; i<n; i++){
			eig[i]=ed.getEigenvalue(i).real;
			//System.out.println(eig[i]);
		}
		
		double[] e2=eig.clone();
		Arrays.sort(e2);
		eigenValues=new double[ndim];
		for (int i=0; i<ndim; i++){
			eigenValues[i]=e2[n-i-1];
			//System.out.println(eigenValues[i]+" "+eig[i]);
		}
		
		double[][] d2=new double[ndim][];
		
		for (int i=0; i<ndim; i++){
			for (int j=0; j<n; j++){
				if (eigenValues[i]==eig[j]){
					d2[i]=ed.getEigenVector(j).getData();
					j=n;
				}
			}
		}

		System.out.println(d2.length+" "+d2[0].length);
		double[][] d3=scaleEigenvectors(d2, eigenValues);
		
		for (int i=0; i<n; i++){
			for (int j=0; j<ndim; j++){
				config[i][j]=d3[j][i];
			}
		}

		System.out.println("Configuration sent");
		return config;
	}
	
	public double[][] PCA(double[][] input) {
		
		int n=input.length;
		int m=input[0].length;
		
		
		DenseMatrix64F A = new DenseMatrix64F(input);
		double[] mean=new double[m];
		for( int i = 0; i < A.getNumRows(); i++ ) {
            for( int j = 0; j < mean.length; j++ ) {
                mean[j] += A.get(i,j);
            }
        }
        for( int j = 0; j < mean.length; j++ ) {
            mean[j] /= A.getNumRows();
        }
        for( int i = 0; i < A.getNumRows(); i++ ) {
            for( int j = 0; j < mean.length; j++ ) {
                A.set(i,j,A.get(i,j)-mean[j]);
            }
        }
        SingularValueDecomposition<DenseMatrix64F> svd =
                DecompositionFactory.svd(A.numRows, A.numCols, false, true, false);
        if( !svd.decompose(A) )
            throw new RuntimeException("SVD failed");
		
        DenseMatrix64F V_t = svd.getV(null,true);
        DenseMatrix64F W = svd.getW(null);

        // Singular values are in an arbitrary order initially
        SingularOps.descendingOrder(null,false,W,V_t,true);
        
        eigenValues=svd.getSingularValues();
        
        for (int i=0; i<eigenValues.length; i++){
        	eigenValues[i]*=eigenValues[i];
        	//System.out.println(eigenValues[i]);
        }
        
        double[][] d2=new double[n][m];
        DenseMatrix64F mean2 = DenseMatrix64F.wrap(m,1,mean);
        for (int i=0; i<n; i++){
        	DenseMatrix64F s = new DenseMatrix64F(m,1,true,input[i]);
        	DenseMatrix64F r = new DenseMatrix64F(m,1);
        	CommonOps.sub(s,mean2,s);
        	CommonOps.mult(V_t,s,r);
        	d2[i]=r.data;
        }
        
        return d2;
        
        
        // strip off unneeded components and find the basis
        //V_t.reshape(numComponents,mean.length,true);
		
		
		/*
		System.out.println(input.length+" "+input[0].length);
	    double[] means = new double[input[0].length];
	    double[][] cov = getCovariance(input, means);
	    
	    
	    DenseMatrix64F D = new DenseMatrix64F(cov);
		EigenDecomposition<DenseMatrix64F> ed=DecompositionFactory.eig(n, true, false);

		if( !ed.decompose(D)){
			   throw new RuntimeException("Cholesky failed!");}

		System.out.println("Eigenvalues");
		
		double[] eig=new double[input[0].length];
		for (int i=0; i<eig.length; i++){
			eig[i]=ed.getEigenvalue(i).real;
			System.out.println(eig[i]);
		}
		
		double[] e2=eig.clone();
		Arrays.sort(e2);
		eigenValues=new double[eig.length];
		for (int i=0; i<eig.length; i++){
			eigenValues[i]=e2[eig.length-i-1];
		}
		
		double[][] d2=new double[ed.getNumberOfEigenvalues()][];
		
		for (int i=0; i<eig.length; i++){
			for (int j=0; j<eig.length; j++){
				if (eigenValues[i]==eig[j]){
					d2[i]=ed.getEigenVector(j).getData();
					j=eig.length;
				}
			}
		}
		
		return d2;
		*/
	  }
	
	
	
	
	
	
	public double[][] scaleEigenvectors(double[][]d, double[] eig){
		
		int n=d[0].length; 
		int m=eig.length;
		System.out.println(n+" "+m);
		double[][] f=new double[m][n];
		
		for (int i=0; i<m; i++){
			
			//double q=0;
			//for (int j=0; j<n; j++){
			//	q+=d[i][j]*d[i][j];
			//}
			
			//double r=Math.sqrt(q/Math.abs(eig[i]));
			
			double r=Math.sqrt(Math.abs(eig[i]));
			
			for (int j=0; j<n; j++){
				f[i][j]=d[i][j]*r;
			}
		}
		return f;
	}
		
	public double[] calculateCorrelation(double[][] d, double[][] input){
		
		int n=d.length;
		int m=d[0].length;
		
		
		double[] results=new double[m];
		
		double[][] temp=new double[n][];
		for (int i=0; i<n; i++){
			temp[i]=new double[i+1];
		}
		
		double meanin=0;
		double count=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				meanin+=input[i][j];
				count++;
			}
		}
		meanin/=count;
		double sstot=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				sstot+=(input[i][j]-meanin)*(input[i][j]-meanin);
			}
		}
		
		for (int i=0; i<m; i++){
			double meantemp=0;
			for (int j=0; j<n; j++){
				for (int k=0; k<j; k++){
					double p=0;
					for (int a=0; a<=i; a++){
						double q=1;
						if (eigenValues[a]<0){q=-1;}
						p+=q*(d[j][a]-d[k][a])*(d[j][a]-d[k][a]);
					}
					
					
					temp[j][k]=Math.sqrt(p);
					meantemp+=temp[j][k];
				}
			}
			meantemp/=count;
			double sserr=0;
			double numer=0;
			double ssreg=0;
			for (int j=0; j<n; j++){
				for (int k=0; k<j; k++){
					//if (i==m-1){System.out.println(j+" "+k+" "+temp[j][k]+" "+input[j][k]);}
					sserr+=(temp[j][k]-input[j][k])*(temp[j][k]-input[j][k]);
					numer+=(temp[j][k]-meantemp)*(input[j][k]-meanin);
					ssreg+=(temp[j][k]-meantemp)*(temp[j][k]-meantemp);
				}
			}
			
			results[i]=numer*numer/(ssreg*sstot);
			//System.out.println("Explained variance: "+(i+1)+" "+results[i]);
		}
		
		return results;
	
	}
	
	public void calculateSDs(double[][] data, int n, int npcs){
		BasicStatistics bs=new BasicStatistics();
		sds=new double[npcs];
		double[] temp=new double [data.length];
		for (int i=0; i<npcs; i++){
			for (int j=0; j<temp.length; j++){
				temp[j]=data[j][i];
			}
			sds[i]=bs.calculateSD(temp, true);
		}
	}
	
	public double[][] getSquareDistanceMatrix(double[][] data){
		double[][] dist=new double[n][n];		//turn the triangular input matrix into a square symmetric distance matrix
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				dist[i][j]=data[i][j];
				dist[j][i]=data[i][j];
			}
		}
		return dist;
	}
	
	double[][] getQuasimetricMatrix(double[][] d, int n){
		
		double[][] d2=new double[n][n];
		for (int i=0; i<n; i++){
			for (int j=0; j<n; j++){
				d2[i][j]=d[i][j];
			}
		}
		boolean correcting=true;
		while (correcting){						//...this uses a quasimetric approach to correct for violations of the triangle inequality (see Dzhafarov 2010).
			correcting=false;
			for (int i=0; i<n; i++){
				for (int j=0; j<n; j++){
					double m=d2[i][j]+1;
					for (int k=0; k<n; k++){
						m=Math.min(m, d2[i][k]+d2[k][j]);
					}
					if (m<d2[i][j]){
						correcting=true;
						d2[i][j]=m;
					}
				}
			}
		}
		return d2;
	}
	
	double[][] getDissimilarityMatrix(double[][] input, int n, int ndi){
		double[][] r=new double[n][n];
		for (int i=0; i<n; i++){
			for (int j=0; j<n; j++){
				double q=0;
				for (int k=0; k<ndi; k++){
					q+=(input[i][k]-input[j][k])*(input[i][k]-input[j][k]);
				}
				r[i][j]=Math.sqrt(q);
			}
		}
		return r;
	}
	
	public double[][] getDistanceMatrix(){
		double[][] r=new double[n][];
		for (int i=0; i<n; i++){
			r[i]=new double[i+1];
		}
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				double q=0;
				for (int k=0; k<npcs; k++){
					double s=1;
					if (eigenValues[k]<0){s=-1;}
					q+=s*(configuration[i][k]-configuration[j][k])*(configuration[i][k]-configuration[j][k]);
				}
				r[i][j]=Math.sqrt(q);
			}
		}
		return r;
	}
	
	public static double[][] getCovariance(double[][] input, double[] meanValues) {
	    int numDataVectors = input.length;
	    int n = input[0].length;

	    double[] sum = new double[n];
	    double[] mean = new double[n];
	    for (int i = 0; i < numDataVectors; i++) {
	      double[] vec = input[i];
	      for (int j = 0; j < n; j++) {
	        sum[j] = sum[j] + vec[j];
	      }
	    }
	    for (int i = 0; i < sum.length; i++) {
	      mean[i] = sum[i] / numDataVectors;
	    }

	    double[][] ret = new double[n][n];
	    for (int i = 0; i < n; i++) {
	      for (int j = i; j < n; j++) {
	        double v = getCovariance(input, i, j, mean);
	        ret[i][j] = v;
	        ret[j][i] = v;
	      }
	    }
	    if (meanValues != null) {
	      System.arraycopy(mean, 0, meanValues, 0, mean.length);
	    }
	    return ret;
	}
	
	private static double getCovariance(double[][] matrix, int colA, int colB, double[] mean) {
	    double sum = 0;
	    for (int i = 0; i < matrix.length; i++) {
	      double v1 = matrix[i][colA] - mean[colA];
	      double v2 = matrix[i][colB] - mean[colB];
	      sum = sum + (v1 * v2);
	    }
	    int n = matrix.length;
	    double ret = (sum / (n - 1));
	    return ret;
	  }

	
}
