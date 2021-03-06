package lusc.net.github.analysis;

import java.util.*;

import lusc.net.github.ui.DistanceFunctionOptions;

public class DistanceNeighborFunctions {
	
	double[][] data;
	int n;
	int type=1;
	double[][] densities, nNeighbors;
	double[] densityThresholds;
	
	double[] meanDens, meanNN;
	
	int dBins=10;
	int nBins=20;
	
	public DistanceNeighborFunctions(double[][] indata, DistanceFunctionOptions dfo, int type){
		this.data=indata;
		this.type=type;
		dBins=dfo.densityCount;
		nBins=dfo.knn;
		n=data.length;
		calculateDensity();
		calculateNNDist();
	}
	
	public int getType(){
		return type;
	}
	
	public int getN(){
		return n;
	}
	
	public int getNBins(){
		return nBins;
	}
	
	public int getDBins(){
		return dBins;
	}
	
	public double[][] getDensities(){
		return densities;
	}
	
	public double[][] getNNeighbors(){
		return nNeighbors;
	}
	
	public double[] getDensityThresholds(){
		return densityThresholds;
	}
	
	public double[] getMeanDens(){
		return meanDens;
	}
	
	public double[] getMeanNN(){
		return meanNN;
	}
	
	public void calculateDensity(){
		double av=calculateAvDist();
		
		densityThresholds=new double[dBins];
		for (int i=0; i<dBins; i++){
			densityThresholds[i]=av/(i+1.0);
		}
		densities=new double[n][dBins];
		
		for (int i=0; i<n; i++){
			double s=0;
			for (int j=0; j<n; j++){
				if (i!=j){
					if (i>j){s=data[i][j];}
					else{s=data[j][i];}
					for (int k=0; k<dBins; k++){
						if (s<densityThresholds[k]){
							densities[i][k]++;
						}
					}
				}
			}
			for (int j=0; j<dBins; j++){
				densities[i][j]/=n-1;
			}
		}
		meanDens=calculateAverages(densities);
	}
	
	public void calculateNNDist(){
		double av=calculateAvDist();

		nNeighbors=new double[n][nBins];
		
		double[] t=new double[n-1];
		
		for (int i=0; i<n; i++){
			int c=0;
			for (int j=0; j<n; j++){
				if (i!=j){
					if (i>j){t[c]=data[i][j];}
					else{t[c]=data[j][i];}
					c++;
				}
			}
			Arrays.sort(t);
			System.arraycopy(t, 0, nNeighbors[i], 0, nBins);
		}
		meanNN=calculateAverages(nNeighbors);
	}
	
	
	public double calculateAvDist(){
		double av=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				av+=data[i][j];
			}
		}
		av/=n*(n-1)*0.5;
		return av;
	}
	
	public double[] calculateAverages(double[][] dat){
		
		int m=dat[0].length;
		
		double[]out=new double[m];
		for (int i=0; i<n; i++){
			for (int j=0; j<m; j++){
				out[j]+=dat[i][j];
			}
		}
		for (int i=0; i<m; i++){
			out[i]/=n+0.0;
		}
		return out;
		
		
	}
	

}
