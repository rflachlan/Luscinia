package lusc.net.github.analysis.syntax;


import java.util.*;

import lusc.net.github.analysis.ComparisonResults;


public class SWMLNoCategories {
	
	int n=0;
	int permutations=100;
	
	double rho=0;
	double entropy=0;
	double zeroentropy=0;
	
	int numSongs=0;
	
	double zero=0;
	
	double[][] diss;
	double thresh;
	int[][] lookup;
	
	int[] marker, mark, labs, labs2;
	
	long[] times;
	
	int numsongs=0;
	
	double eval=1/Math.log(2);
	Random random=new Random(System.currentTimeMillis());
	boolean jk=true;
	
	LinkedList<double[]> results=new LinkedList<double[]>();
	
	public SWMLNoCategories(ComparisonResults cr, double thresh){
		
		diss=cr.getDiss();
		this.thresh=thresh;
		lookup=cr.getLookUp();
		n=diss.length;
		for (int i=0; i<lookup.length; i++){
			if (lookup[i][0]>numsongs){numsongs=lookup[i][0];}
		}
		numsongs++;
		
		marker=new int[lookup.length];
		mark=new int[lookup.length];
		
		labs=new int[numsongs];
		int a=1;
		labs2=new int[numsongs];
		int b=0;
		labs2[numsongs-1]=lookup.length-1;
		
		times=cr.getSongDates();
		
		for (int i=0; i<lookup.length; i++){
			marker[i]=lookup[i][0];
			if ((i>0)&&(lookup[i][0]!=lookup[i-1][0])){
				mark[i]=1;
				labs[a]=i;
				a++;
				
			}
			if ((i<lookup.length-1)&&(lookup[i][0]!=lookup[i+1][0])){
				mark[i]=2;
				labs2[b]=i+1;
				b++;
			}			
			
		}
		
		System.out.println(marker.length+" "+numsongs+" "+diss.length);
		
		
		//EWML(thresh, 0, n, 0, numsongs);
		//EWMLrolling(thresh, 50);
		//EWMLperiodic(thresh, 36);
		//LinkedList<int[]> breaks=cr.getSplits(20, 36);
		LinkedList<int[]> breaks=cr.getSplits();
		EWMLperiodic(thresh, breaks);
	}
	
	public double getRho(){
		return rho;
	}
	
	public double dissimilarity(int a, int b){
		if(a<0){
			if (b<0){
				return 0;
			}
			else{
				return thresh;
			}
		}
		else if (b<0){
			return thresh;
		}
		else if (a>b){
			return diss[a][b];
		}
		else{
			return diss[b][a];
		}
	}
	
	public double randdissimilarity(int start, int n){
		
		int a=random.nextInt(n)+start;
		int b=random.nextInt(n)+start;
		if (a>b){
			return diss[a][b];
		}
		else{
			return diss[b][a];
		}
	}
	
	public int checkMarker(int a, int b){
		if (a==n){a=0;}
		if (b==n){b=0;}
		if (marker[a]==marker[b]){
			return 0;
		}
		else if((mark[a]==1)&&(mark[b]==1)){
			return 1;
		}
		else if((mark[a]==2)&&(mark[b]==2)){
			return 2;
		}
		else{
			return 3;
		}	
	}
	
	public int[] resamplePopulation(int start, int end, boolean replace){
		int[] out=new int[n];
		
		if (!replace){
			int p=end-start;
			for (int i=start; i<end; i++){
				out[i]=random.nextInt(p)+start;
			}
		}
		else{
			for (int i=start; i<end; i++){
				out[i]=i;
			}
			int p=end-start-1;
			int e=end-1;
			for (int i=start; i<e; i++){
				int x=random.nextInt(p)+i+1;
				int y=out[x];
				out[x]=out[i];
				out[i]=y;
				p--;
			}
			
			
		}
		return out;
	}
	
	
	public double[] EWML(double thresh, int start, int end, int startsong, int endsong){
		
		int n=end-start;
		int ns=endsong-startsong;
		
		double t=0;
		double u=0;
		double v=0;
		double[] max2=new double[numsongs];
		for (int i=start; i<end; i++){
			max2[marker[i]]++;	
		}
		for (int i=startsong; i<endsong; i++){
			max2[i]=Math.log(max2[i]+2)*eval;
		}
		boolean rolling=false;
		for (int i=startsong; i<endsong; i++){	
			for (int j=labs[i]; j<labs2[i]; j++){	
				for (int k=startsong; k<endsong; k++){
					if (k!=i){
						int max=0;
						for (int h=labs[k]; h<labs2[k]; h++){
							int a=j;
							int b=h;
							int c=0;
							int l=labs2[i]-labs[i]+1;
							double p=dissimilarity(j, h);
							if (rolling) {
								while ((p<thresh)&&(c<l)){
									a++;
									b++;
									c++;
									if (a==labs2[i]){
										a=-1;
									}
									if (b==labs2[k]){
										b=-1;
									}
									if (a==0){
										a=labs[i];
									}
									if (b==0){
										b=labs[k];
									}
									p=dissimilarity(a,b);
								}
							}
							else {
								boolean br=false;
								while ((p<thresh)&&(br==false)){
									a++;
									b++;
									c++;
									if (a==labs2[i]){
										a=-1;
										br=true;
									}
									if (b==labs2[k]){
										b=-1;
										br=true;
									}
									if (a==0){
										a=labs[i];
									}
									if (b==0){
										b=labs[k];
									}
									p=dissimilarity(a,b);
								}
							}
							if (c>max){max=c;}
						}
						if (max2[k]>0){
							t+=max/max2[k];
							v++;
						}
					}
				}
			}
		}
		
		double best=Double.MAX_VALUE;
		
		for (int w=0; w<permutations; w++){
			double u2=0;
			int[] resample=resamplePopulation(start, end, true);
			for (int i=startsong; i<endsong; i++){	
				for (int j=labs[i]; j<labs2[i]; j++){	
					for (int k=startsong; k<endsong; k++){
						if (k!=i){
							int max=0;
							for (int h=labs[k]; h<labs2[k]; h++){
								int a=j;
								int b=h;
								int c=0;
								int l=labs2[i]-labs[i];
								double p=dissimilarity(resample[j], resample[h]);
								//double p=randdissimilarity(start, n);
								while ((p<thresh)&&(c<l)){
									a++;
									b++;
									c++;
									if (a==labs2[i]){
										a=-1;
									}
									if (b==labs2[k]){
										b=-1;
									}
									if (a==0){
										a=labs[i];
									}
									if (b==0){
										b=labs[k];
									}
									int aa=a;
									if (aa>=0){aa=resample[a];}
									int bb=b;
									if (bb>=0){bb=resample[b];}
									
									p=dissimilarity(aa,bb);
								}
								if (c>max){max=c;}
							}
							if (max2[k]>0){
								u+=max/max2[k];	
								u2+=max/max2[k];
							}
						}
					}
				}

			}
			if (u2<best){best=u2;}
		}
		
		
		entropy=n*(endsong-startsong)/(t);
		
		zeroentropy=n*permutations*(endsong-startsong)/(u);
		
		//double zeroentropy2=n*(endsong-startsong)/(best);
		
		rho=(zeroentropy-entropy)/zeroentropy;
		
		//double rho2=(zeroentropy2-entropy)/zeroentropy;
		double[] x={rho, entropy, zeroentropy};
		//System.out.println(rho+" "+entropy+" "+zeroentropy+" "+rho2+" "+zeroentropy2);
		return x;
	}
	
	public void EWMLrolling(double thresh, int window){
		
		for (int i=window; i<numsongs; i++){
			int start=labs[i-window];
			int end=labs2[i];
			
			double p=getAverageTime(i-window, i);
			
			System.out.print(i+" "+p+" "+start+" "+end+" ");
			EWML(thresh, start, end, i-window, i+1);
		}
	}
	
	public void EWMLperiodic(double thres, LinkedList<int[]> pos){
		
		
		for (int i=0; i<pos.size(); i++){
			int[] x=pos.get(i);
			int start=labs[x[0]];
			int end=labs2[x[1]-1];
			double t=x[3]/24.0;
			System.out.print(i+" "+t+" "+start+" "+end+" "+x[0]+" "+x[1]+ " ");
			double[] r=EWML(thresh, start, end, x[0], x[1]);
			
			double[] y={i, t, x[1]-x[0], r[0], r[1], r[2]};
			results.add(y);
		}
	}
	
	
	public double getAverageTime(int a, int b){
		double p=0;
		for (int i=a; i<b; i++){
			p+=times[i]-times[0];
		}
		p/=(b-a-0.0);
		p/=1000.0*3600*24;
		return p;
	}
	
	public LinkedList<double[]> getResults(){
		return results;
	}
	
	
}