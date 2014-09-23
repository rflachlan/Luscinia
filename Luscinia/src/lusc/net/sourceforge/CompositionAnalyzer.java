package lusc.net.sourceforge;

import java.util.*;

public class CompositionAnalyzer {
	
	int[] classification;
	float[][] dmat;
	SongGroup sg;
	Random random=new Random(System.currentTimeMillis());
	int k;
	int nboot=10000;
	int[][] look=null;
	int[] popIds=null;
	boolean[] exclude=null;
	int n, ns, npops;
	int[][]q;
	
	public CompositionAnalyzer(int[] classification, SongGroup sg, int k, int type, float[][] dmat){
		this.classification=classification;
		this.sg=sg;
		this.k=k;
		this.dmat=dmat;
		setUp(type);
		variabilityPerSong(type);
		calculateTransitionMat();
	}
	
	public void setUp(int type){
		
		if (type==0){
			look=sg.lookUpEls;
			popIds=sg.getPopulationListArray(0);
		}
		if (type==2){
			look=sg.lookUpSyls;
			popIds=sg.getPopulationListArray(2);
		}
		if (type==3){
			look=sg.lookUpTrans;
			popIds=sg.getPopulationListArray(3);
		}
		
		ns=sg.songs.length;
		n=popIds.length;
		npops=0;
		for (int i=0; i<n; i++){
			if (popIds[i]>npops){npops=popIds[i];}
		}
		npops++;
		int[] count=new int[npops];
		q=new int[npops][];
		for (int i=0; i<n; i++){
			count[popIds[i]]++;
		}
		for (int i=0; i<npops; i++){
			q[i]=new int[count[i]];
		}
		count=new int[npops];
		for (int i=0; i<n; i++){
			q[popIds[i]][count[popIds[i]]]=i;
			count[popIds[i]]++;
		}	
		
		exclude=excludeIds(dmat, 0.00, look);
	}
	
	public void variabilityPerSong(int type){
		
		int[] real=new int[n];
		for (int i=0; i<n; i++){
			real[i]=i;
		}
		
		double realx=calculateComp(real, classification, look, n, ns,k, exclude);
		
		double[] simx=new double[nboot];
		for (int i=0; i<nboot; i++){
			int[] sim=shuffleSongs(q, n);
			simx[i]=calculateComp(sim, classification, look, n, ns,k, exclude);
		}
		int lower=(int)Math.floor(0.025*nboot);
		int med=(int)Math.round(0.5*nboot);
		int upper=(int)Math.ceil(0.975*nboot);
		
		Arrays.sort(simx);
		System.out.println("COMPOSITION ANALYSIS: "+realx+" "+simx[lower]+" "+simx[med]+" "+simx[upper]);
	}
	
	public boolean[] excludeIds(float[][] dmat, double thresh, int[][] ids){
		
		int n=ids.length;
		boolean[] results=new boolean[n];
		for (int i=0; i<n; i++){
			results[i]=false;
		}
		
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				if ((results[j]==false)&&(ids[i][0]==ids[j][0])&&(dmat[i][j]<thresh)){
					results[i]=true;
				}
			}
		}
		
		return results;
	}
	
	public double calculateComp(int[] match, int[] classif, int[][] songs, int n, int ns, int k, boolean[] exclude){
		double score=0;
		int[][] frame=new int[ns][k];
		for (int i=0; i<n; i++){
			int a=match[i];
			if(!exclude[a]){
				int b=classif[a];
				int c=songs[i][0];
				frame[c][b]++;
			}
		}
		for (int i=0; i<ns; i++){
			for (int j=0; j<k; j++){
				if (frame[i][j]>0){score++;}
			}
		}
		score/=ns+0.0;
		return score;
	}
	
	public int[] shuffleSongs(int[][] q, int n){
		int[] results=new int[n];
		for (int i=0; i<q.length; i++){
			int[] x=shuffleArray(q[i]);
			for (int j=0; j<x.length; j++){
				results[q[i][j]]=x[j];
			}
		}

		return results;
	}
	
	public int[] shuffleArray(int[] x){
		int n=x.length;
		int[] out=new int[n];
		System.arraycopy(x, 0, out, 0, n);
		for (int i=0; i<n; i++){
			int p=random.nextInt(n-i);
			p+=i;
			int q=out[i];
			out[i]=out[p];
			out[p]=q;
		}
		return out;
	}
	
	public void calculateTransitionMat(){
		int kk=k+1;
		int[][] obs=new int[kk][kk];
		int[] counts=new int[kk];
		double sum=0;
		for (int i=0; i<n; i++){
			if ((i==0)||(look[i-1][0]!=look[i][0])){
				counts[k]++;
				obs[k][classification[i]]++;
				sum++;
			}
			if ((i==n-1)||(look[i+1][0]!=look[i][0])){
				obs[classification[i]][k]++;
			}
			else{
				obs[classification[i]][classification[i+1]]++;
			}
			counts[classification[i]]++;
			sum++;
		}
		
		double[][] exp=new double[kk][kk];
		
		
		double sum2=(sum-counts[k]);
		sum2=1/sum2;
		int kk1=kk-1;
		double kt[]=new double[kk1];
		double sum3=0;
		
		for (int i=0; i<kk1; i++){		//This correction is necessary because k-k transitions are forbidden
			exp[i][kk1]=counts[i]*counts[kk1]*sum2;
			exp[kk1][i]=exp[i][kk1];
			kt[i]=counts[i]-exp[kk1][i];
			sum3+=kt[i];
		}
		
		
		sum3=1/sum3;
		for (int i=0; i<kk1; i++){
			for (int j=0; j<kk1; j++){
				exp[i][j]=kt[i]*kt[j]*sum3;
			}
		}
		
		
		System.out.println("TRANSITION OUTPUT: k="+k);
		System.out.print("   ");
		for (int i=0; i<kk; i++){
			System.out.print(i+"  ");
		}
		System.out.println();
		for (int i=0; i<kk; i++){
			System.out.print(i+" ");
			for (int j=0; j<kk; j++){
				System.out.print(obs[i][j]+" ");
			}
			System.out.println();
		}
		System.out.println();
		System.out.print("   ");
		for (int i=0; i<kk; i++){
			System.out.print(i+"   ");
		}
		System.out.println();
		for (int i=0; i<kk; i++){
			System.out.print(i+" ");
			for (int j=0; j<kk; j++){
				System.out.print(exp[i][j]+" ");
			}
			System.out.println();
		}
		
	}
	
}
