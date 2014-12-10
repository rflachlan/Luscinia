package lusc.net.github.dump;
//
//  CompareThreadOld.java
//  Luscinia
//
//  Created by Robert Lachlan on 11/22/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//


public class CompareThreadOld extends Thread{
	int a, b, c, la, lb;
	double[][]w;
	boolean weightByAmp;
	double[] weightings;
	float[][] scores;
	double[] scoresb;
	double[][] cache;
	double[] data3;
	int[][] eleScheme;
	int dims, length1, length2;
	float score=0;
	double totamp=1;
	double lengthm;
	double variable=1;
	double[][][] data;
	//public CompareThread4(int maxlength, double[][][]data, double[] sds, double sdRatio,  double[] validParameters, boolean weightByAmp, float[] scores, int start, int stop, int f){

	public CompareThreadOld(int a, int b, int c, double[][][]data, boolean weightByAmp, float[][] scores){
		this.a=a;
		this.b=b;
		this.c=c;
		this.data=data;
		this.weightByAmp=weightByAmp;
		this.scores=scores;
	}
	
	public void run(){
		int maxlength=0;
		int d=data.length;
		int eleSize;
		for (lb=0; lb<d; lb++){
			if (data[lb].length>maxlength){maxlength=data[lb].length;}
		}
		data3=new double[maxlength];
		cache=new double[maxlength][maxlength];
		for (la=a; la<b; la++){
			for (lb=0; lb<c; lb++){
				derTimeWarping();
				scores[la][lb]=score;
			}
		}
	}
	
	private void derTimeWarping (){
		
		length1=data[la].length;
		length2=data[lb].length;
		
		
		
		calcWithin();
		lengthm=1/(length1+0.0);
		
		dims=data[la][0].length;
		
		if (weightByAmp){
			dims--;
			totamp=0;
			for (int i=0; i<length1; i++){
				totamp+=data[la][i][dims];		
			}
			if (totamp==0){weightByAmp=false;}
			else{
				lengthm=1;
				totamp=1/totamp;
			}
		}
		
		double bestscore=100000000;
		int loc=0;
		double diff=data[la][1][0]-data[la][0][0];
		int start=length1-length2;
		if (start>0){start=0;}
		int end=length1-length2;
		if (end<0){end=0;}
		double tscore=0;
		int stepSize=(int)Math.ceil(0.1*Math.abs(length1-length2));
		if (stepSize==0){stepSize=1;}
		start-=stepSize;
		end+=stepSize;
		double pd=start*diff;
		for (int i=0; i<length2; i++){
			data3[i]=data[lb][i][0]+pd;
			for (int j=0; j<length1; j++){
				cache[j][i]=0;
			}
		}
		for (int i=start; i<=end; i+=stepSize){
			tscore=getPaths();
			if (tscore<bestscore){
				bestscore=tscore;
				loc=i;
			}
			
			for (int j=0; j<length2; j++){data3[j]+=stepSize*diff;}
		}
		start=loc-stepSize;
		end=loc+stepSize;
		for (int i=0; i<length2; i++){data3[i]=data[lb][i][0]+start*diff;}
		for (int i=start; i<=end; i++){
			tscore=getPaths();
			if (tscore<bestscore){
				bestscore=tscore;
				loc=i;
			}
			for (int j=0; j<length2; j++){data3[j]+=diff;}
		}
		
		score=(float)bestscore;
	}
	
	private void calcWithin(){
		w=null;
		w=new double[length2-1][2];
		for (int i=0; i<length2-1; i++){
			for (int j=0; j<dims; j++){
				w[i][0]+=(data[lb][i][j]-data[lb][i+1][j])*(data[lb][i][j]-data[lb][i+1][j]);
			}
			w[i][1]=1/(2*Math.sqrt(w[i][0]));
		}
	}
	
	private double getPaths(){
		double min=10000000;
		double min2=10000000;
		//double [] holder=new double[length1];
		double cum=0;
		double ho=0;
		int id, ie;
		int j=0;
		int i=0;
		int ic=0;
		int jc=0;
		int g;
		int max=2;
		int l1=length1-1;
		int l2=length2-1;
		double sc=0;
		double sc2=0;
		double sd, se;
		int ps;
		for (int pr=0; pr<length2; pr++){
			sc=(data[la][i][0]-data3[pr])*(data[la][i][0]-data3[pr]);
			if (cache[i][pr]==0){
				for (id=1; id<dims; id++){
					cache[i][pr]+=(data[la][i][id]-data[lb][pr][id])*(data[la][i][id]-data[lb][pr][id]);
				}
			}
			sc+=cache[i][pr];
			
			if (pr>0){
				ps=pr-1;
				sc2=(data[la][i][0]-data3[ps])*(data[la][i][0]-data3[ps]);
				sc2+=cache[0][ps];
				sd=(sc2+w[ps][0]-sc)*w[ps][1];
				if ((sd>0)&&(sd<w[ps][0])){
					se=sd*sd+sc2;
					if (se<sc){sc=se;}
				}
				
			}
			if (sc<min2){
				min2=sc;
				j=pr;
			}
		}
		while ((i!=l1)||(j!=l2)){
			min=10000000;
			for (g=j+1; g<j+max; g++){
				if (g<length2){
					sc=(data[la][i][0]-data3[g])*(data[la][i][0]-data3[g]);
					if (cache[i][g]==0){
						for (id=1; id<dims; id++){
							cache[i][g]+=(data[la][i][id]-data[lb][g][id])*(data[la][i][id]-data[lb][g][id]);
						}
					}
					
					if (g>0){
						ps=g-1;
						sc2=(data[la][i][0]-data3[ps])*(data[la][i][0]-data3[ps]);
						if (cache[i][ps]==0){
							for (id=1; id<dims; id++){
								cache[i][ps]+=(data[la][i][id]-data[lb][ps][id])*(data[la][i][id]-data[lb][ps][id]);
							}
						} 
						sc2+=cache[i][ps];
						sd=(sc2+w[ps][0]-sc)*w[ps][1];
						if ((sd>0)&&(sd<w[ps][0])){
							se=sd*sd+sc2;
							if (se<sc){sc=se;}
						}
						
					}
					
					sc+=cache[i][g];
					if (sc<min){
						min=sc;
						ic=0;
						jc=g-j;
					}
				}
			}
			if (i<l1){
				for (g=j; g<j+max; g++){
					if (g<length2){
						ie=i+1;
						sc=(data[la][ie][0]-data3[g])*(data[la][ie][0]-data3[g]);
						if (cache[ie][g]==0){
							for (id=1; id<dims; id++){
								cache[ie][g]+=(data[la][ie][id]-data[lb][g][id])*(data[la][ie][id]-data[lb][g][id]);
							}
						}
						sc+=cache[ie][g];
						
						if (g>0){
							ps=g-1;
							sc2=(data[la][ie][0]-data3[ps])*(data[la][ie][0]-data3[ps]);
							if (cache[i][ps]==0){
								for (id=1; id<dims; id++){
									cache[i][ps]+=(data[la][i][id]-data[lb][ps][id])*(data[la][i][id]-data[lb][ps][id]);
								}
							}	
							sc2+=cache[i][ps];
							sd=(sc2+w[ps][0]-sc)*w[ps][1];
							if ((sd>0)&&(sd<w[ps][0])){
								se=sd*sd+sc2;
								if (se<sc){sc=se;}
							}
						}
						
						if (sc<min){
							min=sc;
							ic=1;
							jc=g-j;
						}
					}
				}
			}
			j+=jc;
			i+=ic;
			if (ic==0){
				if (min<min2){min2=min;}
			}
			else{
				if (weightByAmp){
					//cum+=elements[la][i-1][dims]*Math.log(variable+min2);
					ho=Math.sqrt(min2);
					//if (ho>cutoff){ho=cutoff;}
					cum+=data[la][i-1][dims]*ho;
					//holder[i-1]=Math.sqrt(min2)*elements[la][i-1][dims];
					//cum+=min2*elements[la][i-1][dims];
				}
				else{
					//cum+=Math.log(variable+min2);
					ho=Math.sqrt(min2);
					//if (ho>cutoff){ho=cutoff;}
					cum+=ho;
					//holder[i-1]=min2;
					//cum+=min2;
				}
				min2=min;
			}
		}
		if (weightByAmp){
			//cum+=elements[la][i-1][dims]*Math.log(variable+min2);
			ho=Math.sqrt(min2);
			//if (ho>cutoff){ho=cutoff;}
			cum+=ho*data[la][i-1][dims];
			cum=cum*totamp;
			//holder[i-1]=Math.sqrt(min2)*elements[la][i-1][dims];
			//cum+=min2*elements[la][i-1][dims];
		}
		else{
			//cum+=Math.log(variable+min2);
			ho=Math.sqrt(min2);
			//if (ho>cutoff){ho=cutoff;}
			cum+=ho;
			cum*=lengthm;
			//holder[i-1]=min2;
			//cum+=min2;
			//Arrays.sort(holder);
			//cum=Math.sqrt(holder[9*length1/10]);
		}
		//cum=Math.pow(Math.E, cum);
		//cum-=variable;
		//cum-=Math.log(variable);
		//return (Math.sqrt(cum)4);
		//
		//
		return (cum);
	}
	
}
