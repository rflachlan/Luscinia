package lusc.net.github.analysis.clustering;

import java.util.Arrays;

public class AffinityPropagation {

	double[][] s, r, a;
	double[] sarr;
	double skk;
	double lambda=0.5;
	
	int nc=0;
	int ce[]=null;
	int countMatch=0;
	int countTarget=10;
	
	int[] assignments;
	
	int type=0;
	int n=0;
	double MRPPresults[];
	int nreps=100000;
	int maxCount=10000;

	public AffinityPropagation(double[][] data, int type, double skk, double lambda, int target){
		this.type=type;
		this.skk=skk;
		this.lambda=lambda;
		this.countTarget=target;
		n=data.length;
		calculateSimilarity(data, skk);
		r=new double[n][n];
		a=new double[n][n];
		
		calculateResponsibility(true);
		calculateAvailability(true);
		calculateAssignments();
		
		int countTotal=0;
		
		//for (int i=0; i<nreps; i++){
		while ((countMatch<countTarget)&&(countTotal<maxCount)){
			calculateResponsibility(false);
			calculateAvailability(false);
			calculateAssignments();
			countTotal++;
			//System.out.println("AP Count: "+countMatch+" "+lambda+" "+skk);
		}
				
	}
	
	public void calculateSimilarity(double[][] d, double skk){
	
		s=new double[n][n];
		sarr=new double[(n*n-1)/2];
		int k=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				s[i][j]=-1*d[i][j]*d[i][j];
				s[j][i]=s[i][j];
				sarr[k]=s[i][j];
				k++;
			}
			//s[i][i]=skk;
		}
		Arrays.sort(sarr);
		int p=(int)Math.round(skk*sarr.length);
		double sk=sarr[p];
		//System.out.println("SKK: "+sk+" "+sarr[0]+" "+sarr[k-1]+" "+skk+" "+p);
		for (int i=0; i<n; i++){
			s[i][i]=sk;
		}
	}
	
	public void calculateResponsibility(boolean init){
		for (int i=0; i<n; i++){
			double max=-1000000000;
			double maxsub=-1000000000;
			int loc=0;
			for (int k=0; k<n; k++){
				double d=s[i][k]+a[i][k];
				if (d>max){
					maxsub=max;
					max=d;
					loc=k;
				}
				else if (d>maxsub){
					maxsub=d;
				}
			}
			
			
			for (int k=0; k<n; k++){
				double rx=0;
				if (k!=loc){
					rx=s[i][k]-max;
				}
				else{
					rx=s[i][k]-maxsub;
				}
				if (init){
					r[i][k]=rx;
				}
				else{
					r[i][k]=(1-lambda)*rx+lambda*r[i][k];
				}
			}
		}
	}
	
	public void calculateAvailabilityO(boolean init){
		
		for (int k=0; k<n; k++){
			
			double d=0;
			for (int i=0; i<n; i++){
				if (i!=k){
					d+=Math.max(0, r[i][k]);
				}
			}
			
			for (int i=0; i<n; i++){
				double ax=0;
				if (i!=k){
					double d2=Math.max(0,  r[i][k]);
					a[i][k]=Math.min(0, r[k][k]+d-d2);
				}
				else{
					a[i][k]=d;
				}
				if (init){
					a[i][k]=ax;
				}
				else{
					a[i][k]=(1-lambda)*ax+lambda*a[i][k];
				}
			}
		}
	}
	
	public void calculateAvailability(boolean init){
		double[] rpn=new double[n];
		for (int k=0; k<n; k++){
			
			double d=0;
			for (int i=0; i<n; i++){
				if ((r[i][k]<0)&&(i!=k)){
					rpn[i]=0;
				}
				else{
					rpn[i]=r[i][k];
				}
				d+=rpn[i];
			}
			
			for (int i=0; i<n; i++){
				
				double d2=d-rpn[i];
				if ((d2>0)&&(i!=k)){
					d2=0;
				}
				double ax=d2;
				
				if (init){
					a[i][k]=ax;
				}
				else{
					a[i][k]=(1-lambda)*ax+lambda*a[i][k];
				}
			}
		}
	}
	
	public void calculateAssignments(){
		int[] assign=new int[n];
		
		int ncent=0;
		int[] cents=new int[n];
		
		for (int i=0; i<n; i++){
			double max=-1000000000;
			int loc=-1;
			for (int j=0; j<n; j++){
				double x=a[i][j]+r[i][j];
				//if (i==0){System.out.print(x+" ");}
				if (max<x){
					max=x;
					loc=j;
				}
			}
			//if (i==0){
				//System.out.println();
				//System.out.println(i+" "+max+" "+loc);
				//System.out.println();
			//}
			assign[i]=loc;
			if (loc==i){
				cents[ncent]=loc;
				ncent++;
			}
		}
		
		boolean match=false;
		if (ncent==nc){
			match=true;
			for (int i=0; i<nc; i++){
				if (ce[i]!=cents[i]){
					match=false;
					i=nc;
				}
			}
		}
		if (match){
			countMatch++;
		}
		else{
			countMatch=0;
			nc=ncent;
			ce=new int[ncent];
			for (int i=0; i<ncent; i++){
				ce[i]=cents[i];
			}
		}
		
		assignments=assign;
		
	}
	
	public int[] getAssignments(){
		
		int[] output=new int[n];
		
		for (int i=0; i<ce.length; i++){
			for (int j=0; j<n; j++){
				if (assignments[j]==ce[i]){
					output[j]=i;
				}
			}
		}

		return output;
	}
	
	public int getNC(){
		return nc;
	}
	
}
