package lusc.net.github;
//
//  NonMetricMultiDimensionalScaling.java
//  Luscinia
//
//  Created by Robert Lachlan on 1/10/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//



public class NonMetricMultiDimensionalScaling {


	double[][] config, diss, bestconfig;
	int[][] ref;
	double[][] gradient, oldGradient;
	double[] diffs, diffs2;
	double stressFactor;
	int n,m,t;
	double oldstresses[]=new double[5];
	int iter=-1;
	double sstar, tstar, ssratio, stratio, magg, maggO;
	double alpha=0.2;
	double snorm=1;
	double bestStress=10000;
	int maxIterations=1000;

		
	public NonMetricMultiDimensionalScaling (double[][] dissimilarities, double[][] initialConfiguration){
		
		System.out.println("Non metric multidimensional scaling initiating");
		
		this.diss=dissimilarities;
		config=initialConfiguration;
		n=dissimilarities.length;
		m=n*(n-1)/2;
		diffs2=new double[m];
		t=initialConfiguration[0].length;
		bestconfig=new double[n][t];
		gradient=new double[n][t];
		oldGradient=new double[n][t];
		diffs=new double[m];
		oldstresses=new double[5];
		sstar=0;
		tstar=0;
		ssratio=0;
		stratio=0;
		magg=0;
		maggO=0;
		iter=-1;
		createReference(dissimilarities);
		
		
		long t1=0;
		long t2=0;
		long t3=0;
		long t4=0;
		long t5=0;
		long t6=0; 
		long t7=0;
		
		
		normalizeConfiguration(t);
		calculateDifference(t);
		calculateStress();
		calculateGradient();
		moveGradients();
		maggO=magg;
		while((magg/maggO>0.01)&&(iter<maxIterations)){
		//while(iter<200){
			long ta=System.currentTimeMillis();
			calculateAlpha();
			t1+=System.currentTimeMillis()-ta;
			//if (Math.random()<0.02){alpha=5;}
			calculateNewConfiguration();
			t2+=System.currentTimeMillis()-ta;
			normalizeConfiguration(t);
			t3+=System.currentTimeMillis()-ta;
			calculateDifference(t);
			t4+=System.currentTimeMillis()-ta;
			calculateStress();
			t5+=System.currentTimeMillis()-ta;
			compareWithBest();
			t6+=System.currentTimeMillis()-ta;
			calculateGradient();
			t7+=System.currentTimeMillis()-ta;
		}
		
		if (magg/maggO>0.01){
			System.out.println("NMDS did NOT converge");
		}
		else{
			System.out.println("NMDS converged");
		}
		System.out.println("NMDS times: alpha - "+t1+" config: "+t2+" norm: "+t3+" diff: "+t4+" stress: "+t5+" comp:+"+t6+" grad: "+t7); 
		System.out.println("NMDS iterations: "+iter);
		
		diss=new double[n][t];
		for (int j=0; j<n; j++){
			for (int k=0; k<t; k++){
				diss[j][k]=bestconfig[j][k];
			}
		}
		normalizeConfiguration(t, diss);
		calculateDifference(t, diss);
		calculateStress();
		stressFactor=snorm;
		
		clearUp();
		System.out.println("Finished NMDS");
	}
	
	public NonMetricMultiDimensionalScaling (double[][] dissimilarities, double[][] initialConfiguration, boolean testOnly){
		this.diss=dissimilarities;
		config=initialConfiguration;
		n=dissimilarities.length;
		m=n*(n-1)/2;
		diffs2=new double[m];
		t=initialConfiguration[0].length;
		bestconfig=new double[n][t];
		gradient=new double[n][t];
		oldGradient=new double[n][t];
		diffs=new double[m];
		createReference(dissimilarities);
		normalizeConfiguration(t);
		calculateDifference(t);
		calculateStress();
		clearUp();
		
	}
	
	void quickSort(int[][] refs){
		shuffle(refs);
		quicksort(refs, 0, refs.length - 1);
    }
	
    public void quicksort(int[][] refs, int left, int right) {
        if (right <= left) return;
        int i = partition(refs, left, right);
        quicksort(refs, left, i-1);
        quicksort(refs, i+1, right);
    }
	
	private int partition(int[][] refs, int left, int right) {
        int i = left - 1;
        int j = right;
        while (true) {
            while (less(refs[++i], refs[right]))      // find item on left to swap
                ;                               // a[right] acts as sentinel
            while (less(refs[right], refs[--j]))      // find item on right to swap
                if (j == left) break;           // don't go out-of-bounds
            if (i >= j) break;                  // check if pointers cross
            exch(refs, i, j);                      // swap two elements into place
        }
        exch(refs, i, right);                      // swap with partition element
        return i;
    }
	
	// is x < y ?
    private boolean less(int[] x, int[] y) {
        return (diss[x[0]][x[1]] < diss[y[0]][y[1]]);
    }
	
	private void exch(int[][] refs, int i, int j){
		int[] t=refs[i];
		refs[i]=refs[j];
		refs[j]=t;
    }
	
	void shuffle(int[][]refs){
		int N = refs.length;
        for (int i = 0; i < N; i++) {
            int r = i + (int) (Math.random() * (N-i));   // between i and N-1
            exch(refs, i, r);
        }
	}
	
	void createReference(double[][] diss){
		ref=new int[m][2];
		int a=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				ref[a][0]=i;
				ref[a][1]=j;
				a++;
			}
		}
		quickSort(ref);
	}
	
	public void calculateStress(){
		if (iter>=0){
			if (iter<5){
				oldstresses[iter]=snorm;
			}
			else{
				for (int i=1; i<5; i++){
					oldstresses[i-1]=oldstresses[i];
				}
				oldstresses[4]=snorm;
			}
		}
		iter++;
		
		for (int i=0; i<m; i++){
			diffs2[i]=diffs[i];
		}
		
		for (int i=0; i<m; i++){
			while ((i<m-1)&&(diffs2[i]==diffs2[i+1])){
				i++;
			}
			boolean doneBoth=false;
			while (!doneBoth){
				doneBoth=true;
				boolean done=false;
				while (!done){				
					done=checkUpActive(i);
					if (!done){doneBoth=false;}
				}
				done=false;
				while (!done) {
					done=checkDownActive(i);
					if (!done){
						doneBoth=false;
					}
				}
			}
		}
				
		sstar=0;
		tstar=0;
		double c,d, q;
		for (int i=0; i<m; i++){
			q=diffs[i];
			d=diffs2[i];
			tstar+=q*q;
			c=q-d;
			sstar+=c*c;
		}
		snorm=Math.sqrt(sstar/tstar);
		ssratio=snorm/sstar;
		stratio=snorm/tstar;
	}
	
	public boolean checkUpActive(int i){
		if (i==m-1){
			return true;
		}
		else{
			boolean satisfied=true;
			while ((i<m-2)&&(diffs2[i]==diffs2[i+1])){
				i++;
			}
			if (diffs2[i]>diffs2[i+1]){
				satisfied=false;
				merge(i+1);
			}
			return satisfied;
		}
	}
	
	public boolean checkDownActive(int i){
		if (i==0){
			return true;
		}
		else{
			boolean satisfied=true;
			while ((i>1)&&(diffs2[i]==diffs2[i-1])){
				i--;
			}
			if (diffs2[i]<diffs2[i-1]){
				satisfied=false;
				merge(i);
			}
			return satisfied;
		}
	}

	
	public void merge(int i){
		
		int d=i-1;
		double p=diffs2[d];
		while((d>=0)&&(p==diffs2[d])){
			d--;
		}
		
		int u=i;
		double q=diffs2[u];
		while((u<m)&&(q==diffs2[u])){
			u++;
		}
		
		double av=((i-1-d)*p+(u-i)*q)/(u-d-1.0);
		
		for (int j=d+1; j<u; j++){
			diffs2[j]=av;
		}
	}
	
	
	public void calculateDifference(int t){
		for (int i=0; i<m; i++){
			int a=ref[i][0];
			int b=ref[i][1];
			double total=0;
			double c;
			for (int j=0; j<t; j++){
				c=(double)(config[a][j]-config[b][j]);
				total+=c*c;
			}
			diffs[i]=(Math.sqrt(total));
		}
	}
	
	public void calculateDifference(int t, double[][] co){
		for (int i=0; i<m; i++){
			int a=ref[i][0];
			int b=ref[i][1];
			double total=0;
			double c;
			for (int j=0; j<t; j++){
				c=(double)(co[a][j]-co[b][j]);
				total+=c*c;
			}
			diffs[i]=(Math.sqrt(total));
		}
	}
	
	public void normalizeConfiguration(int t){
		double[]min=new double[t];
		
		for (int j=0; j<t; j++){
			min[j]=config[0][j];
			for (int i=0; i<n; i++){
				if (config[i][j]<min[j]){
					min[j]=config[i][j];
				}
			}
		}
		
		for (int i=0; i<n; i++){
			for (int j=0; j<t; j++){
				config[i][j]-=min[j];
			}
		}
		
		double[]tot=new double[t];
		
		for (int i=0; i<n; i++){
			for (int j=0; j<t; j++){
				tot[j]+=(double)config[i][j];
			}
		}
		
		for (int i=0; i<t; i++){
			tot[i]=1.0/tot[i];
		}
		
		for (int i=0; i<n; i++){
			for (int j=0; j<t; j++){
				config[i][j]*=tot[j];
			}
		}
	}
	
	public void normalizeConfiguration(int t, double[][] c){
		double[]min=new double[t];
		
		for (int i=0; i<n; i++){
			for (int j=0; j<t; j++){
				if (c[i][j]<min[j]){
					min[j]=config[i][j];
				}
			}
		}
		
		for (int i=0; i<n; i++){
			for (int j=0; j<t; j++){
				c[i][j]-=min[j];
			}
		}
		
		double[]tot=new double[t];
		
		for (int i=0; i<n; i++){
			for (int j=0; j<t; j++){
				tot[j]+=(double)c[i][j];
			}
		}
		
		for (int i=0; i<t; i++){
			tot[i]=1.0/tot[i];
		}
		
		for (int i=0; i<n; i++){
			for (int j=0; j<t; j++){
				c[i][j]*=tot[j];
			}
		}
	}
	
	public void moveGradients(){
	
		for (int i=0; i<n; i++){
			for (int j=0; j<t; j++){
				oldGradient[i][j]=gradient[i][j];
			}
		}
	}
	
	public void calculateGradient(){
		moveGradients();
		gradient=new double[n][t];
		double a, b;
		for (int i=0; i<m; i++){
			a=(ssratio*(diffs[i]-diffs2[i])-stratio*diffs[i])/diffs[i];
			for (int k=0; k<t; k++){
				b=a*(config[ref[i][0]][k]-config[ref[i][1]][k]);
				gradient[ref[i][0]][k]-=b;
				gradient[ref[i][1]][k]+=b;
			}
		}
		
		double sumx=0;
		double sumg=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<t; j++){
				sumx+=config[i][j]*config[i][j];
				sumg+=gradient[i][j]*gradient[i][j];
			}
		}
		magg=Math.sqrt(sumg/sumx);
		
	}
	
	public void calculateNewConfiguration(){
		double ag=alpha/magg;
		for (int i=0; i<n; i++){
			for (int j=0; j<t; j++){
				config[i][j]+=(ag*gradient[i][j]);
			}
		}
	}
	
	public void calculateAlpha(){
		double angleFactor=calculateAngleFactor();
		double goodLuckFactor=calculateGoodLuckFactor();
		double relaxationFactor=calculateRelaxationFactor();
		alpha=alpha*angleFactor*goodLuckFactor*relaxationFactor;
	}
	
	public double calculateAngleFactor(){
		
		double sumog=0;
		double sumg=0;
		double sumgprod=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<t; j++){
				sumog+=oldGradient[i][j]*oldGradient[i][j];
				sumg+=gradient[i][j]*gradient[i][j];
				sumgprod+=gradient[i][j]*oldGradient[i][j];
			}
		}
		if (iter==0){sumgprod=0;}
		double th=sumgprod/(Math.sqrt(sumg)*Math.sqrt(sumog));
		return (Math.pow(4, Math.pow(th, 3)));
	}
	
	public double calculateGoodLuckFactor(){
		double ratio=1;
		if (iter>0){
			ratio=snorm/oldstresses[4];
			if (iter<5){
				ratio=snorm/oldstresses[iter-1];
			}
			if (ratio>1){ratio=1;}
		}
		return(ratio);
	}
	
	public double calculateRelaxationFactor(){
		double ratio=1;
		if (iter>0){
			ratio=snorm/oldstresses[0];
			if (ratio>1){ratio=1;}
		}
		return(1.3/(1+Math.pow(ratio, 5.0)));
	}
	
	public void clearUp(){
		diffs=null;
		ref=null;
		gradient=null;
		oldGradient=null;
	}
	
	public void compareWithBest(){
		if (snorm<bestStress){
			bestStress=snorm;
			for (int i=0; i<n; i++){
				for(int j=0; j<t; j++){
					bestconfig[i][j]=config[i][j];
				}
			}
		}
	}
	
	
			
			
}
