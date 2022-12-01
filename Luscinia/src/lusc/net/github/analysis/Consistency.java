package lusc.net.github.analysis;

import java.util.Arrays;
import java.util.LinkedList;

public class Consistency {

	ComparisonResults cr;
	
	double[] means, medians;
	
	String[][] names;
	int[] phraseids;
	long[] times;
	
	double meanscore=0;
	double mc=0;
	double zeroscore=0;
	
	public Consistency(ComparisonResults cr){
		this.cr=cr;
		
		int[][] lookUp=cr.getLookUp();
		double[][] diss=cr.getDiss();
		
		int n=lookUp.length;
		int count=1;
		for (int i=1; i<n; i++){
			if ((lookUp[i-1][0]!=lookUp[i][0])||(lookUp[i-1][2]!=lookUp[i][2])){
				count++;
			}
		}
		
		int[] index=new int[n];
		count=0;
		index[0]=0;
		for (int i=1; i<n; i++){
			if ((lookUp[i-1][0]!=lookUp[i][0])||(lookUp[i-1][2]!=lookUp[i][2])){
				count++;
			}
			index[i]=count;
		}
		count++;
		
		means=new double[count];
		meanscore=0;
		medians=new double[count];
		mc=0;
		zeroscore=0;
		
		double[] cache=new double[1000];
		
		
		for (int i=0; i<count; i++){
			double sc=0;
			int co=0;
			for (int j=0; j<n; j++){
				if (index[j]==i){
					for (int k=0; k<j; k++){
						if (index[k]==i){
							sc+=diss[j][k];
							cache[co]=diss[j][k];
							co++;
						}
					}
				}
			}
			if (co>0){
				means[i]=sc/(co+0.0);
				meanscore+=means[i];
				medians[i]=calculateMedian(cache, co);
				mc++;
			}
			else{
				zeroscore++;
			}
		}
		
		meanscore/=mc;
		
		names=new String[count][2];
		phraseids=new int[count];
		times=new long[count];
		
		String[][] cnames=cr.getNamesArray(false, false, true, true, false);
		long[] stimes=cr.getTimes();
		
		for (int i=0; i<count; i++){
			for (int j=0; j<n; j++){
				if (index[j]==i){
					names[i][0]=cnames[j][0];
					names[i][1]=cnames[j][1];
					phraseids[i]=lookUp[j][2];
					times[i]=stimes[j];
				}
			}
		}
		
		
		System.out.println("Consistency: "+meanscore);	
	}
	
	public double calculateMedian(double[] cache, int co){
		
		double[] t=new double[co];
		System.arraycopy(cache, 0, t, 0, co);
		Arrays.sort(t);
		double x=0;
		if ((co%2)==0){
			int p=co/2;
			x=0.5*(t[p]+t[p-1]);
		}
		else{
			int p=Math.floorDiv(co, 2);
			x=t[p];
		}
		return x;
	}
	
	public double[] getMeanScores(){
		return means;
	}
	
	public double[] getMedianScores(){
		return medians;
	}
	
	public double getMeanScore(){
		return meanscore;
	}
	
	public String[][] getNames(){
		return names;
	}
	
	public int[] getPhraseIds(){
		return phraseids;
	}
	
	public long[] getTimes(){
		return times;
	}

}
