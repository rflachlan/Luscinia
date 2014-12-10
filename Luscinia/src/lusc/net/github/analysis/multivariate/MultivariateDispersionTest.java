package lusc.net.github.analysis.multivariate;

import java.util.*;

import lusc.net.github.analysis.BasicStatistics;
import lusc.net.github.analysis.dendrograms.UPGMA;

public class MultivariateDispersionTest {
	
	double testFScore=0;
	double pValue=0;
	double[][] meanScores, spatialMedianComp;
	String[] popNames;
	
	double[][][] popScore, indScore2;
	double[][] indScore, clusterDev;
	int type=0;
	float[][] data;
	int n;
	int permutations=10000;
	Random random=new Random(System.currentTimeMillis());
	
	public MultivariateDispersionTest(float[][] data, int[] pop, int type, String[] popNames, int[][] individuals){
		this.data=data;
		this.type=type;
		this.popNames=popNames;
		n=data.length;
		int n1=n;
		
		//pop=new int[pop.length];
		//popNames=new String[1];
		//popNames[0]="temp";
		
		MultiDimensionalScaling mds=new MultiDimensionalScaling();
		boolean completed=false;
		while (completed==false){
			completed=true;
			try{
				mds.RunMetricAnalysis(data, n1, false, false);
			}
			catch(Exception e){
				completed=false;
				n1--;
			}
		}
		
		System.out.println("Finished MDS");
		double[][] config=mds.configuration;
		double[] eig=mds.eigenValues;
		
		int[] indLabels=new int[n];
		for (int i=0; i<individuals.length; i++){
			for (int j=0; j<individuals[i].length; j++){
				indLabels[individuals[i][j]]=i;
				//System.out.println(i+" "+individuals[i][j]);
			}
		}	
		
		int npop=0;
		for (int i=0; i<pop.length; i++){
			if (pop[i]>npop){npop=pop[i];}
		}
		npop++;
		
		System.out.println("Number of Populations: "+npop+" "+popNames.length);

		
		int[] indRef=new int[pop.length];
		for (int i=0; i<individuals.length; i++){
			indRef[pop[individuals[i][0]]]++;
		}
		
		for (int i=0; i<popNames.length; i++){
			System.out.println("Population: "+popNames[i]+" Number of Individuals: "+indRef[i]);
		}
		
		
		int indLabels2[]=new int[n];
		
		for (int i=0; i<npop; i++){
			int u=0;
			for (int j=0; j<individuals.length; j++){
				if (pop[individuals[j][0]]==i){
					for (int k=0; k<n; k++){
						if (indLabels[k]==j){
							indLabels2[k]=u;
						}
					}
					u++;
				}
			}
		}
		
		
		
		int[] popMembers=new int[npop];
		for (int i=0; i<pop.length; i++){
			popMembers[pop[i]]++;
		}
		
		double[][][] byPop=new double[npop][][];
		int[][] indLabel=new int[npop][];
		
		for (int i=0; i<npop; i++){
			byPop[i]=new double[popMembers[i]][n1];
			indLabel[i]=new int[popMembers[i]];
		}
		
		int[] popCount=new int[npop];
		
		for (int i=0; i<n; i++){
			indLabel[pop[i]][popCount[pop[i]]]=i;
			for (int j=0; j<n1; j++){
				byPop[pop[i]][popCount[pop[i]]][j]=config[i][j];
			}
			//System.out.println(pop[i]+" "+popCount[pop[i]]+" "+byPop[pop[i]].length);
			popCount[pop[i]]++;
			
		}

		
		BasicStatistics bs=new BasicStatistics();
		
		
		double[][] spatmed=new double[npop][];
		for (int i=0; i<npop; i++){
			spatmed[i]=calculateSpatialMedian(byPop[i], eig);
		}
		
		spatialMedianComp=compareSpatialMedians(spatmed, eig);
		
		popScore=new double[npop][npop][];
		meanScores=new double[npop][npop];
		for (int i=0; i<npop; i++){
			for (int j=0; j<npop; j++){
				popScore[i][j]=calculateDivergence(spatmed[i], byPop[j], eig);
				meanScores[i][j]=bs.calculateMean(popScore[i][j]);
				
			}
		}
		
		
		
		indScore=new double[npop][];
		double[][] counter=new double[npop][];
		for (int i=0; i<npop; i++){
			indScore[i]=new double[indRef[i]];
			counter[i]=new double[indRef[i]];
		}
				
		for (int i=0; i<npop; i++){
			for (int j=0; j<popScore[i][i].length; j++){
				int p=indLabels2[indLabel[i][j]];
				indScore[i][p]+=popScore[i][i][j];
				counter[i][p]++;
			}
			
			for (int j=0; j<indScore[i].length; j++){
				indScore[i][j]/=counter[i][j];
			}
		}
		if (npop>1){
			calculateFPermutationTest(indScore);
		}
		
		indScore2=new double[npop][npop][];
		double[][][] counter2=new double[npop][npop][];
		for (int i=0; i<npop; i++){
			for (int j=0; j<npop; j++){
				indScore2[i][j]=new double[indRef[j]];
				counter2[i][j]=new double[indRef[j]];
			}
		}
		
		
		for (int i=0; i<npop; i++){
			for (int j=0; j<npop; j++){
				for (int k=0; k<popScore[i][j].length; k++){
					int p=indLabels2[indLabel[j][k]];
					indScore2[i][j][p]+=popScore[i][j][k];
					counter2[i][j][p]++;
				}
			
				for (int k=0; k<indScore2[i][j].length; k++){
					indScore2[i][j][k]/=counter2[i][j][k];
				}
			}
		}
		
		
	}
	
	public MultivariateDispersionTest(float[][] data, UPGMA tree){
		this.data=data;
		n=data.length;
		int n1=n;
		
		MultiDimensionalScaling mds=new MultiDimensionalScaling();
		boolean completed=false;
		while (completed==false){
			completed=true;
			try{
				mds.RunMetricAnalysis(data, n1, false, false);
			}
			catch(Exception e){
				completed=false;
				n1--;
			}
		}
		
		System.out.println("Finished MDS");
		double[][] config=mds.configuration;
		double[] eig=mds.eigenValues;

		int[][] cats=tree.calculateClassificationMembers(100);
		int n=cats[0].length;
		
		clusterDev=new double[n][];
		for (int i=1; i<n; i++){
			int[] t=new int[cats.length];
			for (int j=0; j<cats.length; j++){
				t[j]=cats[j][i];
			}
			clusterDev[i]=calculateSpatialMean(config, eig, t, i);
			
		}

	}
	
	public int getType(){
		return type;
	}
	
	public double getTestFScore(){
		return testFScore;
	}
	
	public double getPValue(){
		return pValue;
	}
	
	public double[][] getMeanScores(){
		return meanScores;
	}
	
	public double[][] getSpatialMedianComp(){
		return spatialMedianComp;
	}
	
	
	public String[] getPopNames(){
		return popNames;
	}
	
	public double[][][] getIndScore2(){
		return indScore2;
	}
	
	public double[][][] getPopScore(){
		return popScore;
	}
	
	public double[][] getClusterDev(){
		return clusterDev;
	}
	
	
	public void calculateFPermutationTest(double[][] data){
		int nmax=0;
		for (int i=0; i<data.length; i++){
			nmax+=data[i].length;
		}
		
		testFScore=calculateLevenesF(data, nmax);
		double count=0;
		
		
		for (int i=0; i<permutations; i++){
			double[][] t=permuteResults(data, nmax);
			double permuteScore=calculateLevenesF(t, nmax);	
			if (permuteScore>testFScore){count++;}
		}
		pValue=count/(permutations+0.0);
		System.out.println("ANDERSON TEST: "+testFScore+" "+pValue);	
	}
	
	public double[][] permuteResults(double[][] input, int nmax){
		int[] locs=new int[nmax];
		for (int i=0; i<nmax; i++){
			locs[i]=i;
		}
		for (int i=0; i<nmax; i++){
			int j=random.nextInt(nmax-i);
			j+=i;
			int k=locs[j];
			locs[j]=locs[i];
			locs[i]=k;
		}
		
		double[] lined=new double[nmax];
		int k=0;
		for (int i=0; i<input.length; i++){
			for (int j=0; j<input[i].length; j++){
				lined[k]=input[i][j];
				k++;
			}
		}
		
		k=0;
		double[][] output=new double[input.length][];
		for (int i=0; i<input.length; i++){
			output[i]=new double[input[i].length];
			for (int j=0; j<input[i].length; j++){
				output[i][j]=lined[locs[k]];
				k++;
			}
		}
		return output;
	}
	
	
	public double calculateLevenesF(double[][] dist, int N){
		int k=dist.length;
		
		//calculate Z.. and Z.
		double ztot=0;
		double[] zmeans=new double[k];
		double[] nByPop=new double[k];
		for (int i=0; i<k; i++){
			nByPop[i]=dist[i].length;
			for (int j=0; j<dist[i].length; j++){
				ztot+=dist[i][j];
				zmeans[i]+=dist[i][j];
			}
			if (nByPop[i]==0){System.out.println("ALERT! "+i);}
			else{
				zmeans[i]/=nByPop[i];
			}
		}
		ztot/=N+0.0;
		
		double numerator=0;
		for (int i=0; i<k; i++){
			double s=zmeans[i]-ztot;
			numerator+=nByPop[i]*s*s;
		}
		
		double denominator=0;
		for (int i=0; i<k; i++){
			for (int j=0; j<dist[i].length; j++){
				double s=dist[i][j]-zmeans[i];
				denominator+=s*s;
			}
		}
		
		double result=((N-k)/(k-1.0))*numerator/denominator;
		
		Double d1=new Double(numerator);
		Double d2=new Double(denominator);
		
		
		if (d1.isNaN()){
			System.out.println("Numerator is a Nan");
			System.out.println(N+" "+k);
		}
		if (d2.isNaN()||(denominator==0)){
			System.out.println("Denominator is a Nan");
			for (int i=0; i<k; i++){
				System.out.println("ZMEANS: "+zmeans[i]);
			}
			System.out.println(N+" "+k);
		}
		return result;
	}
	
	
	public double[] calculateMeanDivergence(double[][] data, double[] eigen){
		int n=data.length;
		int m=data[0].length;
		
		System.out.println("Calculating divergence: "+n+" "+m);
		
		//int n1=(int)Math.round(n*0.25);
		//int n2=(int)Math.round(n*0.75);
		int n1=0;
		int n2=n-1;
		
		double[] means=new double[m];
		double[] holder=new double[n];
		
		for (int i=0; i<m; i++){
			for (int j=0; j<n; j++){
				holder[j]=data[j][i];
			}
			Arrays.sort(holder);
			for (int j=n1; j<=n2; j++){
				means[i]+=holder[j];
			}
			means[i]/=(n2-n1+1.0);
			//means[i]=holder[n2/2];
			//System.out.println("MEANS: "+means[i]);
		}
		double[] dists=new double[n];
		
		for (int i=0; i<n; i++){
			for (int j=0; j<m; j++){
				double q=data[i][j]-means[j];
				q*=q;
				if (eigen[j]<0){q*=-1;}
				dists[i]+=q;
			}
			dists[i]=Math.sqrt(dists[i]);
		}
		return dists;
	}
	
	public double[] calculateSpatialMedianDivergence(double[][] data, double[] eigen){
		int n=data.length;
		int m=data[0].length;
		
		System.out.println("Calculating divergence: "+n+" "+m);
		
		double[] means=new double[m];
		double[] holder=new double[n];
		
		for (int i=0; i<m; i++){
			for (int j=0; j<n; j++){
				holder[j]=data[j][i];
			}
			for (int j=0; j<n; j++){
				means[i]+=holder[j];
			}
			means[i]/=n+0.0;
		}
		double[] dists=new double[n];
		
		int converge=10000;
		
		for (int i=0; i<converge; i++){
			double[] s1=new double[m];
			double s2=0;
			for (int j=0; j<n; j++){
				double d=0;
				for (int k=0; k<m; k++){
					double q=data[j][k]-means[k];
					q*=q;
					if (eigen[k]<0){q*=-1;}
					d+=q;
				}
				if(d>0){
					d=1/Math.sqrt(d);
					s2+=d;
					for (int k=0; k<m; k++){
						s1[k]+=data[j][k]*d;
					}
				}
			}
			
			for (int j=0; j<m; j++){
				means[j]=s1[j]/s2;
			}
		}

		for (int i=0; i<n; i++){
			for (int j=0; j<m; j++){
				double q=data[i][j]-means[j];
				q*=q;
				if (eigen[j]<0){q*=-1;}
				dists[i]+=q;
			}
			
			if (dists[i]<0){
				System.out.println("ERROR!: "+i+" "+dists[i]);
				dists[i]=0;
			}
			else{
				dists[i]=Math.sqrt(dists[i]);
			}
		}
		return dists;
	}
	
	public double[] calculateSpatialMedian(double[][] data, double[] eigen){
		int n=data.length;
		int m=data[0].length;
		
		System.out.println("Calculating divergence: "+n+" "+m);
		
		double[] means=new double[m];
		double[] holder=new double[n];
		
		for (int i=0; i<m; i++){
			for (int j=0; j<n; j++){
				holder[j]=data[j][i];
			}
			for (int j=0; j<n; j++){
				means[i]+=holder[j];
			}
			means[i]/=n+0.0;
		}
		
		int converge=10000;
		
		for (int i=0; i<converge; i++){
			double[] s1=new double[m];
			double s2=0;
			for (int j=0; j<n; j++){
				double d=0;
				for (int k=0; k<m; k++){
					double q=data[j][k]-means[k];
					q*=q;
					if (eigen[k]<0){q*=-1;}
					d+=q;
				}
				if(d>0){
					d=1/Math.sqrt(d);
					s2+=d;
					for (int k=0; k<m; k++){
						s1[k]+=data[j][k]*d;
					}
				}
			}
			
			for (int j=0; j<m; j++){
				means[j]=s1[j]/s2;
			}
		}

		
		return means;
	}
	
	public double[] calculateSpatialMedian(double[][] data, double[] eigen, int[] cluster, int max){
		int n=data.length;
		int m=data[0].length;
		
		System.out.println("Calculating divergence: "+n+" "+m);
		
		double[] means=new double[m];
		double[] holder=new double[n];
		double[] dists=new double[n];
		for (int x=0; x<=max; x++){
			double count=0;
			for (int i=0; i<m; i++){
				means[i]=0;
				count=0;
				for (int j=0; j<n; j++){
					if (cluster[j]==x){
						means[i]+=data[j][i];
						count++;
					}
				}
				means[i]/=count;
			}
		
			System.out.println(max+" "+x+" "+count);
			
			int converge=10000;
			if (count<5){
				converge=100;
			}
			if (count>3){
			for (int i=0; i<converge; i++){
				double[] s1=new double[m];
				double s2=0;
				double s3=0;
				for (int j=0; j<n; j++){
					if(cluster[j]==x){
						double d1=0;
						double d2=0;
						for (int k=0; k<m; k++){
							double q=data[j][k]-means[k];
							q*=q;
							if (eigen[k]>0){d1+=q;}
							else{d2+=q;}
						}
					
						if(d1>0){
							d1=1/Math.sqrt(d1);
							s2+=d1;
							for (int k=0; k<m; k++){
								if (eigen[k]>0){
									s1[k]+=data[j][k]*d1;
								}
							}
						}
						if(d2>0){
							d2=1/Math.sqrt(d2);
							s3+=d2;
							for (int k=0; k<m; k++){
								if (eigen[k]<0){
									s1[k]+=data[j][k]*d2;
								}
							}
						}
					}
				}
				for (int j=0; j<m; j++){
					if (eigen[j]>0){
						means[j]=s1[j]/s2;
					}
					else{
						means[j]=s1[j]/s3;
					}
				}
			}
			for (int i=0; i<m; i++){System.out.print(means[i]+" ");}System.out.println();
			for (int i=0; i<n; i++){
				if (cluster[i]==x){
					for (int j=0; j<m; j++){
						double q=data[i][j]-means[j];
						q*=q;
						if (eigen[j]<0){q*=-1;}
						dists[i]+=q;
					}
			
					if (dists[i]<0){
						System.out.println("ERROR!: "+i+" "+dists[i]);
						dists[i]=0;
					}
					else{
						dists[i]=Math.sqrt(dists[i]);
					}
				}
			}
			}
		}
		return dists;
	}
	
	public double[] calculateSpatialMean(double[][] data, double[] eigen, int[] cluster, int max){
		int n=data.length;
		int m=data[0].length;
				
		double[] means=new double[m];
		double[] dists=new double[n];
		
		for (int x=0; x<=max; x++){
			double count=0;
			for (int i=0; i<m; i++){
				means[i]=0;
				count=0;
				for (int j=0; j<n; j++){
					if (cluster[j]==x){
						means[i]+=data[j][i];
						count++;
					}
				}
				means[i]/=count;
			}
			
			for (int i=0; i<n; i++){
				if (cluster[i]==x){
					for (int j=0; j<m; j++){
						double q=data[i][j]-means[j];
						q*=q;
						if (eigen[j]<0){q*=-1;}
						dists[i]+=q;
					}
			
					if (dists[i]<0){
						System.out.println("ERROR!: "+i+" "+dists[i]);
						dists[i]=0;
					}
					else{
						dists[i]=Math.sqrt(dists[i]);
					}
				}
			}
		}
		return dists;
	}
	
	public double[] calculateDivergence(double[] means, double[][] data, double[] eigen){
		int n=data.length;
		int m=data[0].length;
		
		
		double[] dists=new double[n];


		for (int i=0; i<n; i++){
			for (int j=0; j<m; j++){
				double q=data[i][j]-means[j];
				q*=q;
				if (eigen[j]<0){q*=-1;}
				dists[i]+=q;
			}
			
			if (dists[i]<0){
				System.out.println("ERROR!: "+i+" "+dists[i]);
				dists[i]=0;
			}
			else{
				dists[i]=Math.sqrt(dists[i]);
			}
		}
		return dists;
	}
	
	public double calculateNewF(float[][] dist, int[] pop, int npop){
		int n=dist.length;
		
		double[] dk=new double[npop];
		double[] dc=new double[npop];
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				if (pop[i]==pop[j]){
					dk[pop[i]]+=dist[i][j];
					dc[pop[i]]++;
				}
			}
		}
		for (int i=0; i<npop; i++){
			dk[i]/=dc[i];
			System.out.println("POPULATION MEANS: "+dk[i]);
		}
		
		double[]c=new double[npop];
		for (int i=0; i<n; i++){
			c[pop[i]]++;
		}
		
		double[] sk=new double[npop];
		for (int i=0; i<n; i++){
			double d=0;
			for (int j=0; j<n; j++){
				if ((i!=j)&&(pop[i]==pop[j])){
					if(i>j){
						d+=dist[i][j];
					}
					else{
						d+=dist[j][i];
					}
				}
			}
			d/=c[pop[i]]-1.0;
			sk[pop[i]]+=(d-dk[pop[i]])*(d-dk[pop[i]]);
		}
		
		for (int i=0; i<npop; i++){
			double n1=4*(c[i]-1.0);
			double n2=(c[i]-2.0)*(c[i]-2.0);
			sk[i]*=n1/n2;
		}
		
		double dov=0;
		for (int i=0; i<npop; i++){
			dov+=c[i]*dk[i];
		}
		dov*=1/(npop+0.0);
		
		double skov=0;
		for (int i=0; i<npop; i++){
			skov+=(c[i]-1)*sk[i];
		}
		skov*=1/(n-npop+0.0);
		
		double f=0;
		for (int i=0; i<npop; i++){
			f+=c[i]*(dk[i]-dov)*(dk[i]-dov);
		}
		f*=1/(skov*(npop-1));
		System.out.println("F RESULTS: "+f+" "+dov+" "+skov);
		
		return f;
		
		
	
	}
		
	
	public double[][] compareSpatialMedians(double[][] sm, double[] eig){
		
		int n=sm.length;
		int m=sm[0].length;
		
		double[][] result=new double[n][n];
		
		for (int i=0; i<n; i++){
			for (int j=0; j<n; j++){
				double p=0;
				for (int k=0; k<m; k++){
					double q=sm[i][k]-sm[j][k];
					double s=1;
					if (eig[k]<0){s=-1;}
					p+=q*q*s;
				}
				result[i][j]=Math.sqrt(p);
			}
		}
		return result;
		
	}

	

}
