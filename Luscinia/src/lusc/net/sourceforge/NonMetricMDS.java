package lusc.net.sourceforge;

import mdsj.*;

public class NonMetricMDS {
	
	int n, ndi;
	int maxIter=10000;
	int maxTime=1000000;
	int threshold=4;
	double stressFactor=0;
	double[] eigenValues;
	double[] percentExplained;
	double[] sds;
	double[][] pc;
	
	
	public NonMetricMDS(float[][] input, int ndi){
		
		n=input.length;
		this.ndi=ndi;
		
		double [][]d=new double[n][n];		//turn the triangular input matrix into a square symmetric distance matrix
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				d[i][j]=input[i][j];
				d[j][i]=input[i][j];
			}
		}
		
		double[][] dt=getQuasimetricMatrix(d, n);
		double[][] pc1=calculateClassicMDS(dt, ndi, n);
		StressMinimization sm=calculateNMDS(d, pc1, ndi);
		stressFactor=sm.getNormalizedStress();
		double[][] nmpc=calculateNMPCs(sm);
		dt=getDissimilarityMatrix(nmpc, n, ndi);
		pc=calculateClassicMDS(dt, ndi, n);
		percentExplained=calculateCorrelation(pc, d);
		sds=calculateSDs(pc, n, ndi);
	}
	
	
	StressMinimization calculateNMDS(double[][] d, double[][] init, int ndi){
		
		StressMinimization sm=new StressMinimization(d, init);
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
	
	double[][] calculateClassicMDS(double[][] dt, int n, int ndi){
		
		//double[][] results=MDSJ.classicalScaling(dt, ndi);
		ClassicalScaling cs=new ClassicalScaling();
		double[][] results=new double[n][ndi];
		double q[]=cs.fullmds(dt, results);
		eigenValues=q;
		for (int i=0; i<q.length; i++){System.out.println(i+"::"+q[i]);}
		
		return results;
		
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
		for (int i=0; i<input.length; i++){
			for (int j=0; j<input.length; j++){
				double q=0;
				for (int k=0; k<ndi; k++){
					q+=(input[i][k]-input[j][k])*(input[i][k]-input[j][k]);
				}
				r[i][j]=Math.sqrt(q);
			}
		}
		return r;
	}
	
	public double[] calculateCorrelation(double[][] d, double[][] input){
		
		int n=d.length;
		int m=d[0].length;
		
		double[] results=new double[m];
		
		
		double[][] temp=getDissimilarityMatrix(d, n, m);
		
		float meanin=0;
		float count=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				meanin+=input[i][j];
				count++;
			}
		}
		meanin/=count;
		float sstot=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				sstot+=(input[i][j]-meanin)*(input[i][j]-meanin);
			}
		}
		
		for (int i=0; i<m; i++){
			float meantemp=0;
			for (int j=0; j<n; j++){
				for (int k=0; k<j; k++){
					meantemp+=temp[j][k];
				}
			}
			meantemp/=count;
			float sserr=0;
			float numer=0f;
			float ssreg=0;
			for (int j=0; j<n; j++){
				for (int k=0; k<j; k++){
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
	
	public double[] calculateSDs(double[][] out, int n, int npcs){
		BasicStatistics bs=new BasicStatistics();
		double[] sds=new double[npcs];
		double[] temp=new double [out.length];
		for (int i=0; i<npcs; i++){
			for (int j=0; j<temp.length; j++){
				temp[j]=out[j][i];
			}
			sds[i]=bs.calculateSD(temp, true);
		}
		return sds;
	}
	
}
