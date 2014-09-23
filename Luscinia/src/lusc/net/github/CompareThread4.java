package lusc.net.github;



public class CompareThread4 extends Thread{
	int start, stop, maxlength, dims, f;
	boolean weightByAmp;
	boolean stitch;
	float[] scores;
	double[] validParameters, sds, seg2, point2, vec1sq;
	double[][] p, q, r, s, t, seg1a, seg1b, vec1;
	int[][] proute;
	double[][] dataFocal;
	int[][] elPos;
	int[] locsX={-1,-1,0};
	int[] locsY={0,-1,-1};
	
	//ComparisonFrame cf;
	double[][][] data;
	
	long time1, time2=0;
	
	double sdRatio=0.5;
	double lowerCutOff=0.02;
	
	//OLD:
	/*
	double[][] w;
	double[] weightings;
	double[] scoresb;
	double[][] cache;
	double[] data3;
	int length1, length2;
	float score=0;
	double totamp=1;
	double lengthm;
	double variable=1;
	int mode=0;
	 */
	
	public CompareThread4(int maxlength, double[][][]data, int[][]elPos, double[] sds, double sdRatio,  double[] validParameters, boolean weightByAmp, float[] scores, int start, int stop, int f){
		
 		this.data=data;
 		this.elPos=elPos;
 		if (elPos!=null){stitch=true;}
 		else{stitch=false;}
		this.sds=sds;
		this.sdRatio=sdRatio;
		this.validParameters=validParameters;
		this.maxlength=maxlength;
		this.weightByAmp=weightByAmp;
		this.scores=scores;
		
		this.start=start;
		this.stop=stop;
		this.f=f;

		dataFocal=new double [data[f].length][];
		
		
		
		for (int i=0; i<dataFocal.length; i++){
			dataFocal[i]=new double[data[f][i].length];
			System.arraycopy(data[f][i], 0, dataFocal[i], 0, dataFocal[i].length);			
		}
		//System.out.println(dataFocal.length);
		dims=dataFocal.length-1;
		if (weightByAmp){
			dims--;
		}
	}
	
	
	public synchronized void run(){
		
		p=new double[maxlength][maxlength];
		q=new double[maxlength][maxlength];
		r=new double[maxlength][maxlength];
		s=new double[maxlength][maxlength];
		t=new double[maxlength][maxlength];
		proute=new int[maxlength][maxlength];
		
		//OLD!:
		//data3=new double[maxlength];
		//cache=new double[maxlength][maxlength];
		
		for (int i=start; i<stop; i++){
			if (stitch){
				scores[i]=derTimeWarpingAsym(dataFocal, data[i], elPos[i]);
			}
			else{
				scores[i]=derTimeWarpingAsym(dataFocal, data[i], null);
			}
			//scores[i]=derTimeWarpingStretch(dataFocal, data[i]);
			//scores[i]=derTimeWarping(f, i);
			
		}
	}
	
	
	
	
			
	public double[] getJointAverages(double[][] data1, double[][] data2, boolean weight){
		
		double avs1[]=new double[dims];
		double avs2[]=new double[dims];
		double sum1[]=new double[dims];
		double sum2[]=new double[dims];
		for (int i=0; i<dims; i++){
			for (int j=0; j<data1[0].length; j++){
				if (weight){
					avs1[i]+=data1[i][j]*data1[dims][j];
					sum1[i]+=data1[dims][j];
				}
				else{
					avs1[i]+=data1[i][j];
					sum1[i]++;
				}
			}
			for (int j=0; j<data2[0].length; j++){
				if (weight){
					avs2[i]+=data2[i][j]*data2[dims][j];
					sum2[i]+=data2[dims][j];
				}
				else{
					avs2[i]+=data2[i][j];
					sum2[i]++;
				}
			}
		}
		
		for (int i=0; i<dims; i++){
			//avs1[i]=0.5*((avs1[i]/sum1[i])+(avs2[i]/sum2[i]));
			avs1[i]=((avs1[i]+avs2[i])/(sum1[i]+sum2[i]));
		}
		return (avs1);
	}
	
	public double[] getJointSD(double[][] data1, double[][] data2, double[] avs, boolean weight){
		
		double sd1[]=new double[dims];
		double sd2[]=new double[dims];
		double sum1[]=new double[dims];
		double sum2[]=new double[dims];
		double a;
		
		double avs1[]=new double[dims];
		double avs2[]=new double[dims];

		for (int i=0; i<dims; i++){
			for (int j=0; j<data1[0].length; j++){
				if (weight){
					avs1[i]+=data1[i][j]*data1[dims][j];
					sum1[i]+=data1[dims][j];
				}
				else{
					avs1[i]+=data1[i][j];
					sum1[i]++;
				}
			}
			for (int j=0; j<data2[0].length; j++){
				if (weight){
					avs2[i]+=data2[i][j]*data2[dims][j];
					sum2[i]+=data2[dims][j];
				}
				else{
					avs2[i]+=data2[i][j];
					sum2[i]++;
				}
			}
		}
		
		for (int i=0; i<dims; i++){
			avs1[i]/=sum1[i];
			avs2[i]/=sum2[i];
		}
		
		for (int i=0; i<dims; i++){
			for (int j=0; j<data1[0].length; j++){
				//if (i==1){System.out.println(data1[i][j]);}
				if (weight){
					a=(data1[i][j]-avs1[i]);
					sd1[i]+=a*a*data1[dims][j];
				}
				else{
					a=data1[i][j]-avs1[i];
					sd1[i]+=a*a;
				}
			}
			for (int j=0; j<data2[0].length; j++){
				if (weight){
					a=(data2[i][j]-avs2[i]);
					sd2[i]+=a*a*data2[dims][j];
				}
				else{
					a=data2[i][j]-avs2[i];
					sd2[i]+=a*a;
				}
			}
		}	
		
		
		for (int i=0; i<dims; i++){
			sd1[i]=0.5*(Math.sqrt(sd1[i]/(sum1[i]))+Math.sqrt(sd2[i]/(sum2[i])));
			//sd1[i]=Math.max(Math.sqrt(sd1[i]/(sum1[i])),Math.sqrt(sd2[i]/(sum2[i])));
			//sd1[i]=Math.sqrt((sd1[i]+sd2[i])/(sum1[i]+sum2[i]-1.0));
			//System.out.println((i+1)+" "+sd1[i]+" "+avs[i]);
		}
		sd1[0]=0.5*(data1[0][data1[0].length-1]+data2[0][data2[0].length-1]);
		return (sd1);
	}
	
	public float runComp(double[][] d1, double[][] d2, int[]pos){
		//double[] avs=getJointAverages(dataFocal, dataComp, weightByAmp);
		//double[] sd=getJointSD(dataFocal, dataComp, avs, weightByAmp);
		double[] sd=new double[sds.length];
		
		//double sdT=0.5*(dataFocal[0][dataFocal[0].length-1]+dataComp[0][dataComp[0].length-1]);
		double sdT=Math.max(d1[0][d1[0].length-1]-d1[0][0],d2[0][d2[0].length-1]-d2[0][0]);
		
		double totweight=0;
		
		for (int i=0; i<dims; i++){
			totweight+=validParameters[i];
		}	
		
		for (int i=0; i<dims; i++){
			//System.out.println(sd[i]+" "+sds[i]);
			if(i==0){
				//System.out.println(sdT+" "+sds[i]);
				sd[i]=sdRatio*sdT+(1-sdRatio)*sds[i];	
			}
			else{
				sd[i]=sds[i];
			}
			if ((Double.isNaN(sd[i]))||(sd[i]==0)){
				sd[i]=sds[i];
				System.out.println("Extreme sd ratio problem!");
			}
			sd[i]=validParameters[i]/(totweight*sd[i]);
			//System.out.println(sds[i]+" ");
		}
		//System.out.println();
		float score1=derTimeWarpingPointInterpol(d1, d2, pos, sd);
		//float score1=derTimeWarpingPointFast(dataFocal, dataComp, sd);
		return score1;
	}
	
	
	public float derTimeWarpingStretch(double[][] dataFocal, double[][] dataComp){
		double[] avs=getJointAverages(dataFocal, dataComp, weightByAmp);
		double[] sd=getJointSD(dataFocal, dataComp, avs, weightByAmp);
		
		double totweight=0;
		
		for (int i=0; i<dims; i++){
			totweight+=validParameters[i];
		}	
		
		for (int i=0; i<dims; i++){
			//System.out.println(sd[i]+" "+sds[i]);
			sd[i]=sdRatio*sd[i]+(1-sdRatio)*sds[i];			
			if ((Double.isNaN(sd[i]))||(sd[i]==0)){
				sd[i]=sds[i];
				System.out.println("Extreme sd ratio problem!");
			}
			sd[i]=validParameters[i]/(totweight*sd[i]);
			
		}
		float score1=derTimeWarpingPointTimeStretcher(dataFocal, dataComp, sd);
		return score1;
	}
	
	public float derTimeWarpingAsym (double[][] dataFocal, double[][] dataComp, int[]pos){
		
		
		float score1=runComp(dataFocal, dataComp, pos);
		
		float scoreb=score1;
		
		
		double[][] x1=new double[dataFocal.length][dataFocal[0].length];
		
		for (int i=1; i<dataFocal.length; i++){
			System.arraycopy(dataFocal[i],0,x1[i],0, dataFocal[0].length);
		}
		
		double diff=dataFocal[0][dataFocal[0].length-1]-dataComp[0][dataComp[0].length-1];
		
		for (int i=0; i<dataFocal[0].length; i++){
			x1[0][i]=dataFocal[0][i]-diff;
		}
		
		float score3=runComp(x1, dataComp, pos);
		
		if (score3<scoreb){scoreb=score3;}
		
		diff=diff*0.5;
		for (int i=0; i<dataFocal[0].length; i++){
			x1[0][i]=dataFocal[0][i]-diff;
		}
		
		float score5=runComp(x1, dataComp, pos);
		
		if (score5<scoreb){scoreb=score5;}
		
		
		if (Float.isNaN(score1)){
			System.out.println("DTW Made a NaN");
			System.out.println(score1+" "+score3+" "+score5);
			System.out.println();
		}
		
		//System.out.println(score1+" "+score3+" "+score5);
		return scoreb;
		//return 0.5f*(score1+score2);
		//return ((float)Math.min(Math.max(score1, score2), Math.max(score3, score4), Math.max(score5, score6)));
	}
	
	
	public float derTimeWarpingPointInterpolOld (double[][] dataFocal, double[][] dataComp, double[] sdf){
		
		
		int length1=dataFocal[0].length;
		int length2=dataComp[0].length;
		int length21=dataComp.length-1;
		
		double min, sc, sc2, d1, a1, b1, c1, xx1, xx2, xx3, x1, x2, x3;;
		int x,y,z, id, i, j,k, locx, locy;
				
		x1=0;
		x2=0;
		x3=0;
		sc2=0;
		
		
		j=0;
		for (i=0; i<length2; i++){
			if (dataComp[length21][i]==0){
				j++;
			}
		}
		
		int length3=j;	
				
		int[] w=new int[length3];
		j=0;
		for (i=0; i<length2; i++){
			if (dataComp[length21][i]==0){
				w[j]=i;
				j++;
			}
		}
		
		
		double[][] seg1=new double[length3][dims];
		double[][] seg2=new double[length3][dims];
		double[] d2=new double[length3];
		double[] d3=new double[length3];
		//the following section measures the distances between point-point segments in dataComp
		j=0;
		for (i=0; i<length2; i++){
			if (dataComp[length21][i]==0){
				for (id=0; id<dims; id++){
					a1=dataComp[id][w[j]]*sdf[id];
					b1=dataComp[id][w[j]+1]*sdf[id];
					seg1[j][id]=a1;
					seg2[j][id]=b1;
					c1=b1-a1;
					d2[j]+=c1*c1;
				}
				d3[j]=Math.sqrt(d2[j]);
				j++;
			}
		}
		
		//the following section finds the distances between points in dataFocal to point-point segments in dataComp using trig.
		//it also generates the length1 x length3 distance matrix between dataFocal and the dataComp segments, s.
		
		for (i=0; i<length1; i++){
			for (j=0; j<length3; j++){
				s[i][j]=0;
				xx1=0;
				xx2=0;
				for (id=0; id<dims; id++){
					d1=dataFocal[id][i]*sdf[id];
					xx1+=(d1-seg1[j][id])*(d1-seg1[j][id]);
					xx2+=(d1-seg2[j][id])*(d1-seg2[j][id]);
				}

				
				//is first angle obtuse? Law of cosines
				if ((xx2-d2[j]-xx1)>0){
					x1=Math.sqrt(xx1);
					s[i][j]=x1;
					x=1;
				}
				//is second angle obtuse?
				else if ((xx1-d2[j]-xx2)>0){
					x2=Math.sqrt(xx2);
					s[i][j]=x2;
					x=2;
				}
				else{
					x1=Math.sqrt(xx1);
					x2=Math.sqrt(xx2);
					x3=d3[j];
										
					sc=0.5*(x3+x1+x2);
					xx1=sc-x1;
					xx2=sc-x2;
					xx3=sc-x3;
					if (xx3>=0){
						sc2=Math.sqrt(sc*xx1*xx2*xx3);			//Heron's method for finding the area of a triangle
						s[i][j]=2*sc2/d3[j];
					}
					else{
						s[i][j]=0;
					}
					
																	//Height of a triangle
					x=3;
				}
				if (Double.isNaN(s[i][j])){
					System.out.println("COMPISNAN "+x+" "+x1+" "+x2+" "+x3+" "+sc2);
				}
				//if (s[i][j]<0.01){s[i][j]=0.01;}
				//s[i][j]=Math.log(s[i][j]*100);
				//s[i][j]=Math.sqrt(s[i][j]);
				//System.out.println(xx1+" "+xx2+" "+d2[j]+" "+s[i][j]+" "+x);
				//s[i][j]*=s[i][j];
				//if (s[i][j]<lowerCutOff){s[i][j]=lowerCutOff;}
				
			}
		}
				

		//Finally here is the actual DTW algorithm, searching for the best route through x.
		//the search is weighted by the path length (p), but user variable weightByAmp can mean that the end score is weighted by amplitude in dataFocal instead.
		
		r[0][0]=s[0][0];
		p[0][0]=1;
		if (weightByAmp){
			q[0][0]=dataFocal[dims][0];
		}
		for (i=0; i<length1; i++){
			for (j=0; j<length3; j++){
				min=1000000000;
				locx=0;
				locy=0;
				z=-1;
				for (k=0; k<3; k++){
					x=i+locsX[k];
					y=j+locsY[k];
					
					if ((x>=0)&&(y>=0)){
						sc2=r[x][y]/p[x][y];
						if (sc2<min){
							min=sc2;
							locx=x;
							locy=y;
							z=k;
							proute[i][j]=k;
						}
					}
				}
				if (z>=0){
					r[i][j]=r[locx][locy]+s[i][j];
					p[i][j]=p[locx][locy]+1;
					if (weightByAmp){
						q[i][j]=q[locx][locy]+dataFocal[dims][i];
					}
				}
			}
		}
		
		int ba=length1-1;
		int bb=length3-1;
		double den=p[ba][bb];
		if (weightByAmp){
			den=q[ba][bb];
		}
		j=bb;
		double finalScore=0;
		den=0;
		for (i=ba; i>=0; i--){
			sc2=s[i][j];
			sc=1;
			while (proute[i][j]==2){
				j--;
				sc2+=s[i][j];
				sc++;
			}
			sc2/=sc;
			if (weightByAmp){
				finalScore+=sc2*dataFocal[dims][i];
				den+=dataFocal[dims][i];
			}
			else{
				finalScore+=sc2;
			}
			if (proute[i][j]==1){
				j--;
			}
		}
		if (weightByAmp){
			finalScore/=den;
		}
		else{
			finalScore/=length1+0.0;
		}
		float result=(float)finalScore;
		//float result=(float)(r[ba][bb]/den);
		//float result=(float)Math.exp(r[ba][bb]/den);
		//float result=(float)Math.sqrt(r[ba][bb]/den);
		return result;
	}
	
	public float derTimeWarpingPointInterpol (double[][] dm1, double[][] dm2, int[] pos, double[] sdf){
		
		
		int length1=dm1[0].length;
		int length2=dm2[0].length;
		int length21=dm2.length-1;
		
		double min, sc, sc2, d1, a1, b1, c1, xx1, xx2, xx3, x1, x2, x3;;
		int x,y,z, id, i, j,k, locx, locy;
		double smallnum=0.000000001;
		x1=0;
		x2=0;
		x3=0;
		sc2=0;
		
		
		j=0;
		for (i=0; i<length2; i++){
			if (dm2[length21][i]==0){
				j++;
			}
		}
		
		int length3=j;	
				
		int[] w=new int[length3];
		j=0;
		for (i=0; i<length2; i++){
			if (dm2[length21][i]==0){
				w[j]=i;
				j++;
			}
		}
		
		
		double[][] seg1=new double[length3][dims];
		double[][] seg2=new double[length3][dims];
		double[] d2=new double[length3];
		double[] d3=new double[length3];
		//the following section measures the distances between point-point segments in dataComp
		j=0;
		for (i=0; i<length2; i++){
			if (dm2[length21][i]==0){
				for (id=0; id<dims; id++){
					a1=dm2[id][w[j]]*sdf[id];
					b1=dm2[id][w[j]+1]*sdf[id];
					seg1[j][id]=a1;
					seg2[j][id]=b1;
					c1=b1-a1;
					d2[j]+=c1*c1;
				}
				d3[j]=Math.sqrt(d2[j]);
				j++;
			}
		}
		
		//the following section finds the distances between points in dataFocal to point-point segments in dataComp using trig.
		//it also generates the length1 x length3 distance matrix between dataFocal and the dataComp segments, s.
		
		for (i=0; i<length1; i++){
			for (j=0; j<length3; j++){
				s[i][j]=0;
				xx1=0;
				xx2=0;
				for (id=0; id<dims; id++){
					d1=dm1[id][i]*sdf[id];
					xx1+=(d1-seg1[j][id])*(d1-seg1[j][id]);
					xx2+=(d1-seg2[j][id])*(d1-seg2[j][id]);
				}
				
				
				//IF syllables are stitched, don't interpolate BETWEEN notes
				
				if((stitch)&&(pos[j]!=pos[j+1])){
					s[i][j]=Math.sqrt(Math.min(xx1, xx2));	
					
				}
				else{
					//is first angle obtuse? Law of cosines
					if ((xx2-d2[j]-xx1)>0){
						x1=Math.sqrt(xx1);
						s[i][j]=x1;
						x=1;
					}
					//is second angle obtuse?
					else if ((xx1-d2[j]-xx2)>0){
						x2=Math.sqrt(xx2);
						s[i][j]=x2;
						x=2;
					}
					else{
						/*
						x1=Math.sqrt(xx1);
						x2=Math.sqrt(xx2);
						x3=d3[j];
										
						sc=0.5*(x3+x1+x2);
						xx1=sc-x1;
						xx2=sc-x2;
						xx3=sc-x3;
						if (xx3>=0){
							sc2=Math.sqrt(sc*xx1*xx2*xx3);			//Heron's method for finding the area of a triangle
							s[i][j]=2*sc2/d3[j];					//Height of a triangle
						}
						else{
							s[i][j]=0;
						}
						*/
						sc=xx2+d2[j]-xx1;
						sc=xx2-(sc*sc/(4*d2[j]));
						if (sc<smallnum){sc=0;}
						s[i][j]=Math.sqrt(sc);
						x=3;
						if (Double.isNaN(s[i][j])){
							System.out.println("ISNAN: "+x+" "+sc+" "+d2[j]+" "+xx1+" "+xx2);
							
						}											
						
					}
				
					
				}
				//if (s[i][j]<0.01){s[i][j]=0.01;}
				//s[i][j]=Math.log(s[i][j]*100);
				//s[i][j]=Math.sqrt(s[i][j]);
				//System.out.println(xx1+" "+xx2+" "+d2[j]+" "+s[i][j]+" "+x);
				//s[i][j]*=s[i][j];
				//if (s[i][j]<lowerCutOff){s[i][j]=lowerCutOff;}
				
			}
		}
				

		//Finally here is the actual DTW algorithm, searching for the best route through x.
		//the search is weighted by the path length (p), but user variable weightByAmp can mean that the end score is weighted by amplitude in dataFocal instead.
		
		r[0][0]=s[0][0];
		p[0][0]=1;
		q[0][0]=s[0][0];
		if (weightByAmp){
			q[0][0]=s[0][0]*dataFocal[dims][0];
			p[0][0]=dataFocal[dims][0];
		}
		for (i=0; i<length1; i++){
			for (j=0; j<length3; j++){
				min=1000000000;
				locx=0;
				locy=0;
				for (k=0; k<3; k++){
					x=i+locsX[k];
					y=j+locsY[k];
					
					if ((x>=0)&&(y>=0)){
						sc2=r[x][y];
						if (sc2<min){
							min=sc2;
							locx=x;
							locy=y;
						}
					}
				}
				if (min<1000000000){
					r[i][j]=min+s[i][j];
					if (weightByAmp){
						q[i][j]=q[locx][locy]+s[i][j]*dataFocal[dims][i];
						p[i][j]=p[locx][locy]+dataFocal[dims][i];
					}
					else{
						q[i][j]=q[locx][locy]+s[i][j];
						p[i][j]=p[locx][locy]+1;
					}
				}
			}
		}
		
		
		//float result=(float)(r[length1-1][length3-1]/Math.max(length1, length3));
		float result=(float)(q[length1-1][length3-1]/p[length1-1][length3-1]);
		//float result=(float)(r[ba][bb]/den);
		//float result=(float)Math.exp(r[ba][bb]/den);
		//float result=(float)Math.sqrt(r[ba][bb]/den);
		return result;
	}
	
	
	
	public float derTimeWarpingPointFastOld (double[][] dataFocal, double[][] dataComp, double[] sdf){
		
		
		int length1=dataFocal[0].length;
		int length2=dataComp[0].length;
		
		double min, sc, sc2;
		int x,y,z, i, j,k, locx, locy;
		
		//make a matrix of point vs point differences in the two signals
		for (i=0; i<length1; i++){
			for (j=0; j<length2; j++){
				sc=0;
				for (k=0; k<dims; k++){
					sc2=(dataComp[k][j]-dataFocal[k][i])*sdf[k];
					sc+=sc2*sc2;
				}
				s[i][j]=Math.sqrt(sc);
			}
		}
		
		//Finally here is the actual DTW algorithm, searching for the best route through x.
		//the search is weighted by the path length (p), but user variable weightByAmp can mean that the end score is weighted by amplitude in dataFocal instead.
		
		r[0][0]=s[0][0];
		p[0][0]=1;
		if (weightByAmp){
			q[0][0]=dataFocal[dims][0];
		}
		for (i=0; i<length1; i++){
			for (j=0; j<length2; j++){
				min=1000000000;
				locx=0;
				locy=0;
				z=-1;
				for (k=0; k<3; k++){
					x=i+locsX[k];
					y=j+locsY[k];
					
					if ((x>=0)&&(y>=0)){
						sc2=r[x][y]/p[x][y];
						if (sc2<min){
							min=sc2;
							locx=x;
							locy=y;
							z=k;
						}
					}
				}
				if (z>=0){
					r[i][j]=r[locx][locy]+s[i][j];
					p[i][j]=p[locx][locy]+1;
					if (weightByAmp){
						q[i][j]=q[locx][locy]+dataFocal[dims][i];
					}
				}
			}
		}
		
		int ba=length1-1;
		int bb=length2-1;
		double den=p[ba][bb];
		if (weightByAmp){
			den=q[ba][bb];
		}
		float result=(float)(r[ba][bb]/den);
		
		return result;
	}
	
	public float derTimeWarpingPointFast (double[][] dataFocal, double[][] dataComp, double[] sdf){
		
		
		int length1=dataFocal[0].length;
		int length2=dataComp[0].length;
		
		double min, sc, sc2;
		int x,y,z, i, j,k, locx, locy;
		
		//make a matrix of point vs point differences in the two signals
		for (i=0; i<length1; i++){
			for (j=0; j<length2; j++){
				sc=0;
				for (k=0; k<dims; k++){
					sc2=(dataComp[k][j]-dataFocal[k][i])*sdf[k];
					sc+=sc2*sc2;
				}
				s[i][j]=Math.sqrt(sc);
			}
		}
		
		//Finally here is the actual DTW algorithm, searching for the best route through x.
		//the search is weighted by the path length (p), but user variable weightByAmp can mean that the end score is weighted by amplitude in dataFocal instead.
		
		r[0][0]=s[0][0];
		q[0][0]=1;
		if (weightByAmp){
			q[0][0]=dataFocal[dims][0];
		}
		for (i=0; i<length1; i++){
			for (j=0; j<length2; j++){
				min=1000000000;
				locx=0;
				locy=0;
				z=-1;
				for (k=0; k<3; k++){
					x=i+locsX[k];
					y=j+locsY[k];
					
					if ((x>=0)&&(y>=0)){
						sc2=r[x][y];
						if (sc2<min){
							min=sc2;
							locx=x;
							locy=y;
							z=k;
						}
					}
				}
				if (z>=0){
					r[i][j]=r[locx][locy]+s[i][j];
				}
			}
		}
		
		int ba=length1-1;
		int bb=length2-1;
		double den=Math.max(length1, length2);
		float result=(float)(r[ba][bb]/den);
		
		return result;
	}
	
	public float derTimeWarpingPointTimeStretcher (double[][] dataFocal, double[][] dataComp, double[] sdf){
			
			
			int length1=dataFocal[0].length;
			int length2=dataComp[0].length;
			int length21=dataComp.length-1;
			
			double min, sc, sc2, d1, a1, b1, c1, xx1, xx2, xx3, x1, x2, x3;;
			int x,y,z, id, i, j,k, locx, locy;
					
			x1=0;
			x2=0;
			x3=0;
			sc2=0;
			
			
			j=0;
			for (i=0; i<length2; i++){
				if (dataComp[length21][i]==0){
					j++;
				}
			}
			
			int length3=j;	
					
			int[] w=new int[length3];
			j=0;
			for (i=0; i<length2; i++){
				if (dataComp[length21][i]==0){
					w[j]=i;
					j++;
				}
			}
			
			
			double[][] seg1=new double[length3][dims];
			double[][] seg2=new double[length3][dims];
			double[] d2=new double[length3];
			double[] d3=new double[length3];
			//the following section measures the distances between point-point segments in dataComp
			j=0;
			for (i=0; i<length2; i++){
				if (dataComp[length21][i]==0){
					for (id=0; id<dims; id++){
						a1=dataComp[id][w[j]]*sdf[id];
						b1=dataComp[id][w[j]+1]*sdf[id];
						seg1[j][id]=a1;
						seg2[j][id]=b1;
						c1=b1-a1;
						d2[j]+=c1*c1;
					}
					d3[j]=Math.sqrt(d2[j]);
					j++;
				}
			}
			
			//the following section finds the distances between points in dataFocal to point-point segments in dataComp using trig.
			//it also generates the length1 x length3 distance matrix between dataFocal and the dataComp segments, s.
			
			for (i=0; i<length1; i++){
				for (j=0; j<length3; j++){
					s[i][j]=0;
					xx1=0;
					xx2=0;
					for (id=1; id<dims; id++){
						d1=dataFocal[id][i]*sdf[id];
						xx1+=(d1-seg1[j][id])*(d1-seg1[j][id]);
						xx2+=(d1-seg2[j][id])*(d1-seg2[j][id]);
					}

					//is first angle obtuse? Law of cosines
					if ((xx2-d2[j]-xx1)>0){
						//x1=Math.sqrt(xx1);
						s[i][j]=xx1;
						d1=dataFocal[0][i]*sdf[0];
						t[i][j]=d1-seg1[j][0];
						x=1;
					}
					//is second angle obtuse?
					else if ((xx1-d2[j]-xx2)>0){
						//x2=Math.sqrt(xx2);
						s[i][j]=xx2;
						d1=dataFocal[0][i]*sdf[0];
						t[i][j]=d1-seg2[j][0];
						x=2;
					}
					else{
						x1=Math.sqrt(xx1);
						x2=Math.sqrt(xx2);
						x3=d3[j];
											
						sc=0.5*(x3+x1+x2);
						xx1=sc-x1;
						xx2=sc-x2;
						xx3=sc-x3;
						d1=dataFocal[0][i]*sdf[0];
						if (xx3>=0){
							sc2=Math.sqrt(sc*xx1*xx2*xx3);			//Heron's method for finding the area of a triangle
							s[i][j]=2*sc2/d3[j];
							s[i][j]*=s[i][j];
							t[i][j]=d1-(Math.sqrt(x1*x1-s[i][j]*s[i][j])/x3)*(seg2[j][0]-seg1[j][0]);
						}
						else{
							s[i][j]=0;
							t[i][j]=0;
						}
						
																		//Height of a triangle
						x=3;
					}
					if (Double.isNaN(s[i][j])){
						System.out.println(x+" "+x1+" "+x2+" "+x3+" "+sc2);
					}
					//if (s[i][j]<0.01){s[i][j]=0.01;}
					//s[i][j]=Math.log(s[i][j]*100);
					//s[i][j]=Math.sqrt(s[i][j]);
					//System.out.println(xx1+" "+xx2+" "+d2[j]+" "+s[i][j]+" "+x);
					//s[i][j]*=s[i][j];
					//if (s[i][j]<lowerCutOff){s[i][j]=lowerCutOff;}
					
				}
			}
					

			//Finally here is the actual DTW algorithm, searching for the best route through x.
			//the search is weighted by the path length (p), but user variable weightByAmp can mean that the end score is weighted by amplitude in dataFocal instead.
			
			r[0][0]=s[0][0];
			p[0][0]=1;
			if (weightByAmp){
				q[0][0]=dataFocal[dims][0];
			}
			for (i=0; i<length1; i++){
				for (j=0; j<length3; j++){
					min=1000000000;
					locx=0;
					locy=0;
					z=-1;
					for (k=0; k<3; k++){
						x=i+locsX[k];
						y=j+locsY[k];
						
						if ((x>=0)&&(y>=0)){
							sc2=r[x][y]/p[x][y];
							if (sc2<min){
								min=sc2;
								locx=x;
								locy=y;
								z=k;
								proute[i][j]=k;
							}
						}
					}
					if (z>=0){
						r[i][j]=r[locx][locy]+s[i][j]+(t[i][j]-t[locx][locy])*(t[i][j]-t[locx][locy]);
						p[i][j]=p[locx][locy]+1;
						if (weightByAmp){
							q[i][j]=q[locx][locy]+dataFocal[dims][i];
						}
					}
				}
			}
			
			int ba=length1-1;
			int bb=length3-1;
			double den=p[ba][bb];
			if (weightByAmp){
				den=q[ba][bb];
			}
			j=bb;
			double finalScore=0;
			den=0;
			for (i=ba; i>=0; i--){
				sc2=s[i][j];
				sc=1;
				while (proute[i][j]==2){
					j--;
					sc2+=s[i][j];
					sc++;
				}
				sc2/=sc;
				if (weightByAmp){
					finalScore+=sc2*dataFocal[dims][i];
					den+=dataFocal[dims][i];
				}
				else{
					finalScore+=sc2;
				}
				if (proute[i][j]==1){
					j--;
				}
			}
			if (weightByAmp){
				finalScore/=den;
			}
			else{
				finalScore/=length1+0.0;
			}
			float result=(float)Math.sqrt(finalScore);

			return result;
		}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public float derTimeWarpingPointOld (double[][] dataFocal, double[][] dataComp, double[] sdf){
		
		
		//long t1=System.currentTimeMillis();
		
		
		int length1=dataFocal[0].length-1;
		int length2=dataComp[0].length;
		
		double min, sc2, b, d1, d, a1, b1, c1;
		int x,y,z, id, i, j,k, locx, locy;
		
		double[][][] seg1=new double[length1][dims][2];
		double[][] seg2=new double[dims][2];
		
		//double [] vec1, vec2 vec3;
		double[] vec2=new double[dims];
		double[] d2=new double[length1];
		double[] point2=new double[dims];
		double[][] vec1=new double[length1][dims];
		
		for (i=0; i<length1; i++){
			for (id=0; id<dims; id++){
				a1=dataFocal[id][i]*sdf[id];
				b1=dataFocal[id][i+1]*sdf[id];
				seg1[i][id][0]=a1;
				seg1[i][id][1]=b1;
				c1=b1-a1;
				d2[i]+=c1*c1;
				vec1[i][id]=c1;
			}
			//vec1[i]=vector(seg1[i]);
		}
		
		for (i=0; i<length1; i++){
			for (j=0; j<length2; j++){
				s[i][j]=0;
				d1=0;
				for (id=0; id<dims; id++){
					
					seg2[id][1]=seg1[i][id][0];
					seg2[id][0]=dataComp[id][j]*sdf[id];
					vec2[id]=seg2[id][1]-seg2[id][0];
					d1+=vec1[i][id]*vec2[id];
				}
				
				//vec2=vector(seg2);
				
				//d1=dotProduct(vec1[i], vec2);
				
				if (d1<=0){
					d=0;
					for (id=0; id<dims; id++){
						d+=(seg1[i][id][0]-seg2[id][0])*(seg1[i][id][0]-seg2[id][0]);
					}
					//s[i][j]=Math.sqrt(d);
					s[i][j]=d;
				}
				else{
					//d2=dotProduct(vec1[i], vec1[i]);
					if (d2[i]<=d1){
						d=0;
						for (id=0; id<dims; id++){
							d+=(seg1[i][id][1]-seg2[id][0])*(seg1[i][id][1]-seg2[id][0]);
						}
						//s[i][j]=Math.sqrt(d);
						s[i][j]=d;
					}
					else{
						b = d1 / d2[i];
						
						for (id=0; id<dims; id++){
							point2[id]=seg1[i][id][0]+b*vec1[i][id];
						}
						
						d=0;
						for (id=0; id<dims; id++){
							d+=(point2[id]-seg2[id][0])*(point2[id]-seg2[id][0]);
						}
						//s[i][j]=Math.sqrt(d);
						s[i][j]=d;					
					}
				}
				//s[i][j]=Math.sqrt(s[i][j]);
				
				if (weightByAmp){
					q[i][j]=0.5*(dataFocal[dims][i]+dataFocal[dims][i+1]);
				}
				else{
					q[i][j]=1;
				}
			}
		}
		
		//long t3=System.currentTimeMillis();
		
		r[0][0]=s[0][0];
		p[0][0]=q[0][0];
		for (i=0; i<length1; i++){
			for (j=0; j<length2; j++){
				min=1000000000;
				locx=0;
				locy=0;
				z=-1;
				for (k=0; k<3; k++){
					x=i+locsX[k];
					y=j+locsY[k];
					
					if ((x>=0)&&(y>=0)){
						sc2=r[x][y]/p[x][y];
						if (sc2<min){
							min=sc2;
							locx=x;
							locy=y;
							z=k;
						}
					}
				}
				if (z>=0){
					if (weightByAmp){
						r[i][j]=r[locx][locy]+s[i][j]*q[i][j];
						p[i][j]=q[i][j]+p[locx][locy];
					}
					else{
						r[i][j]=r[locx][locy]+s[i][j];
						p[i][j]=p[locx][locy]+1;
					}
					
				}
			}
		}
		
		
		
		//time1+=t2-t1;
		//time2+=t3-t2;
		
		
		return (float)Math.sqrt(r[length1-1][length2-1]/p[length1-1][length2-1]);
	}
	
	/*
	private float derTimeWarping (int la, int lb){
		
		double[] avs=getJointAverages(data[la], data[lb], weightByAmp);
		
		double[] sd=getJointSD(data[la], data[lb], avs, weightByAmp);
		
		double totweight=0;
		
		for (int i=0; i<dims; i++){
			totweight+=validParameters[i];
		}	
		
		for (int i=0; i<dims; i++){
			sd[i]=sdRatio*sd[i]+(1-sdRatio)*sds[i];			
			if (sd[i]==0){sd[i]=1;}
			sd[i]=validParameters[i]/(totweight*sd[i]);
			
		}
		
		length1=data[la][0].length;
		length2=data[lb][0].length;
		
		calcWithin(la, lb, sd);
		lengthm=1/(length1+0.0);
		
		
		if (weightByAmp){
			totamp=0;
			for (int i=0; i<length1; i++){
				totamp+=data[la][dims][i];		
			}
			if (totamp==0){weightByAmp=false;}
			else{
				lengthm=1;
				totamp=1/totamp;
			}
		}
		
		double bestscore=100000000;
		int loc=0;
		double diff=data[la][0][1]-data[la][0][0];
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
			data3[i]=data[lb][0][i]+pd;
			for (int j=0; j<length1; j++){
				cache[j][i]=0;
			}
		}
		for (int i=start; i<=end; i+=stepSize){
			tscore=getPaths(la, lb, sd);
			if (tscore<bestscore){
				bestscore=tscore;
				loc=i;
			}
			
			for (int j=0; j<length2; j++){data3[j]+=stepSize*diff;}
		}
		start=loc-stepSize;
		end=loc+stepSize;
		for (int i=0; i<length2; i++){data3[i]=data[lb][0][i]+start*diff;}
		for (int i=start; i<=end; i++){
			tscore=getPaths(la, lb, sd);
			if (tscore<bestscore){
				bestscore=tscore;
				loc=i;
			}
			for (int j=0; j<length2; j++){data3[j]+=diff;}
		}
		
		score=(float)bestscore;
		return score;
	}
	
	private void calcWithin(int la, int lb, double[]sd){
		w=null;
		w=new double[length2-1][2];
		for (int i=0; i<length2-1; i++){
			for (int j=0; j<dims; j++){
				w[i][0]+=(data[lb][j][i]-data[lb][j][i+1])*(data[lb][j][i]-data[lb][j][i+1])*(sd[j]*sd[j]);
			}
			//System.out.println(la+" "+lb+" "+length2+" "+w[i][0]);
			if (w[i][0]>1000){
				for (int j=0; j<dims; j++){
					System.out.println(data[lb][j][i]+" "+data[lb][j][i+1]+" "+sd[j]+" "+(data[lb][j][i]-data[lb][j][i+1])*(data[lb][j][i]-data[lb][j][i+1])*(sd[j]*sd[j]));
				}
			}
			w[i][1]=1/(2*Math.sqrt(w[i][0]));
		}
	}
	
	private double getPaths(int la, int lb, double[] sd){
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
		double sd2, se;
		int ps;
		for (int pr=0; pr<length2; pr++){
			sc=(data[la][0][i]-data3[pr])*(data[la][0][i]-data3[pr])*(sd[0]*sd[0]);
			if (cache[i][pr]==0){
				for (id=1; id<dims; id++){
					cache[i][pr]+=(data[la][id][i]-data[lb][id][pr])*(data[la][id][i]-data[lb][id][pr])*(sd[id]*sd[id]);
				}
			}
			sc+=cache[i][pr];
			
			if (pr>0){
				ps=pr-1;
				sc2=(data[la][0][i]-data3[ps])*(data[la][0][i]-data3[ps])*(sd[0]*sd[0]);
				sc2+=cache[0][ps];
				sd2=(sc2+w[ps][0]-sc)*w[ps][1];
				if ((sd2>0)&&(sd2<w[ps][0])){
					se=sd2*sd2+sc2;
					if (se<sc){sc=se;}
				}
				
			}
			if (sc<min2){
				min2=sc;
				j=pr;
			}
		}
		while ((i!=l1)||(j!=l2)){
			min=10000000000.0;
			for (g=j+1; g<j+max; g++){
				if (g<length2){
					sc=(data[la][0][i]-data3[g])*(data[la][0][i]-data3[g])*(sd[0]*sd[0]);
					if (cache[i][g]==0){
						for (id=1; id<dims; id++){
							cache[i][g]+=(data[la][id][i]-data[lb][id][g])*(data[la][id][i]-data[lb][id][g])*(sd[id]*sd[id]);
						}
					}
					
					if (g>0){
						ps=g-1;
						sc2=(data[la][0][i]-data3[ps])*(data[la][0][i]-data3[ps])*(sd[0]*sd[0]);
						if (cache[i][ps]==0){
							for (id=1; id<dims; id++){
								cache[i][ps]+=(data[la][id][i]-data[lb][id][ps])*(data[la][id][i]-data[lb][id][ps])*(sd[id]*sd[id]);
							}
						} 
						sc2+=cache[i][ps];
						sd2=(sc2+w[ps][0]-sc)*w[ps][1];
						if ((sd2>0)&&(sd2<w[ps][0])){
							se=sd2*sd2+sc2;
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
						if (g>=data3.length){System.out.println(la+" "+data.length+" "+data[0].length+" "+ie+" "+g+" "+data3.length);}
						if (ie>=data[la][0].length){System.out.println("B:" +la+" "+data.length+" "+data[0].length+" "+ie+" "+g+" "+data3.length);}
						sc=(data[la][0][ie]-data3[g])*(data[la][0][ie]-data3[g])*(sd[0]*sd[0]);
						if (cache[ie][g]==0){
							for (id=1; id<dims; id++){
								cache[ie][g]+=(data[la][id][ie]-data[lb][id][g])*(data[la][id][ie]-data[lb][id][g])*(sd[id]*sd[id]);
							}
						}
						sc+=cache[ie][g];
						
						if (g>0){
							ps=g-1;
							sc2=(data[la][0][ie]-data3[ps])*(data[la][0][ie]-data3[ps])*(sd[0]*sd[0]);
							if (cache[i][ps]==0){
								for (id=1; id<dims; id++){
									cache[i][ps]+=(data[la][id][i]-data[lb][id][ps])*(data[la][id][i]-data[lb][id][ps])*(sd[id]*sd[id]);
								}
							}	
							sc2+=cache[i][ps];
							sd2=(sc2+w[ps][0]-sc)*w[ps][1];
							if ((sd2>0)&&(sd2<w[ps][0])){
								se=sd2*sd2+sc2;
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
					cum+=data[la][dims][i-1]*ho;
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
			cum+=ho*data[la][dims][i-1];
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
	
	 */

	
	
}
