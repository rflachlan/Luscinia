package lusc.net.github.analysis;

/**
 * This class carries out the Dynamic Time Warping on input array data.
 * It is thread-enabled for performance
 * @author Rob
 *
 */

public class CompareThread extends Thread{
	int start, stop, maxlength, dims, dimsT, f;
	boolean weightByAmp;
	boolean normaliseWithSDs=true;
	boolean interpolateWarp=true;
	boolean dynamicWarp=true;
	boolean squared=true;
	double maximumWarp=0.25;
	int numTempPars=0;
	double[] scores, d2;
	double[] validParameters, validTempPars, sds, sdsT;
	double[][] p, q, r, s, seg1, seg1t;
	double[][] dataFocal, dataComp,dataFocalT, dataCompT;
	double[] dataFocalA, dataCompA;
	int[] dataFocalL, dataCompL;

	int[] locsX={-1,-1,0};
	int[] locsY={0,-1,-1};
	double timediff=0;
	
	int l1, l2;
	
	//ComparisonFrame cf;
	double[][][] data, dataT;
	double[][] dataA;
	int[][] dataL;
	
	
	long time1, time2=0;
	
	double sdRatio=0.5;
	double sdRatioNT=0.5;
	double lowerCutOff=0.02;
	int alignPoints=3;
	
	boolean saveMatrix=false;
	
	double[][] tempMatrix, bestMatrix;
	int[][] trajectory, bestTrajectory;
	int al1, al2;
	
	double[] meanErrors, meanErrorsT, sdErrors, sdErrorsT;
	
	boolean[][]compmat=null;
	
	boolean checkcompmat=false;
	
	/**
	 * Constructor for CompareThread
	 * @param maxlength the maximum length of any array (time series)
	 * @param pdtw a PrepareDTW object containing data for the analysis 
	 * @param stitch if true, the data is stitched, and the algorithm should check for joins
	 * @param scores an array to put the results
	 * @param start position to begin comparing
	 * @param stop	 position to stop comparing
	 * @param f
	 */
	public CompareThread(int maxlength, PrepareDTW pdtw, double[] scores, int start, int stop, int f, boolean saveMatrix){
		//System.out.println("0");
		this.saveMatrix=saveMatrix;
		numTempPars=pdtw.getNumTempPars();
		//System.out.println("1");
		
		data=pdtw.getData();
		if (numTempPars>0){
			dataT=pdtw.getDataT();
			sdsT=pdtw.getSDTemp();
		}
		dataA=pdtw.getDataAmp();
		dataL=pdtw.getDataLoc();
			
		sds=pdtw.getSD();	
		
		
		if (numTempPars>0){
			validTempPars=pdtw.getValidTempPar();
		}
		//System.out.println("2");
		sdRatio=pdtw.getSDRatio();
		sdRatioNT=pdtw.getSDRatioNT();
		validParameters=pdtw.getValidParameters();
		weightByAmp=pdtw.getWeightByAmp();
		squared=pdtw.getSquared();
		//System.out.println("3");
		normaliseWithSDs=pdtw.getNormaliseWithSds();
		alignPoints=pdtw.getAlignmentPoints();
		interpolateWarp=pdtw.getInterpolateWarp();
		dynamicWarp=pdtw.getDynamicWarp();
		maximumWarp=pdtw.getMaximumWarp()*0.01;
		
		this.maxlength=maxlength;
		this.scores=scores;		
		this.start=start;
		this.stop=stop;
		this.f=f;

		dataFocal=new double [data[f].length][];
		dataFocalT=new double[dataT[f].length][];
		dataFocalA=new double[dataA[f].length];
		dataFocalL=new int[dataL[f].length];
		
		for (int i=0; i<dataFocal.length; i++){
			dataFocal[i]=new double[data[f][i].length];
			System.arraycopy(data[f][i], 0, dataFocal[i], 0, dataFocal[i].length);
		}
		for (int i=0; i<dataFocalT.length; i++){
			dataFocalT[i]=new double[dataT[f][i].length];
			System.arraycopy(dataT[f][i], 0, dataFocalT[i], 0, dataFocalT[i].length);
		}
		
		System.arraycopy(dataA[f], 0, dataFocalA, 0, dataFocalA.length);
		System.arraycopy(dataL[f], 0, dataFocalL, 0, dataFocalL.length);
		
		l1=dataFocal[0].length;
		
		dims=dataFocal.length;
		dimsT=dataFocalT.length;
		if (saveMatrix){System.out.println("PREPARED");}
	}
	
	public CompareThread(int maxlength, PrepareDTW pdtw, double[] scores, int start, int stop, int f, boolean saveMatrix, boolean[][] compmat){
		//System.out.println("0");
		this.compmat=compmat;
		checkcompmat=true;
		this.saveMatrix=saveMatrix;
		numTempPars=pdtw.getNumTempPars();
		//System.out.println("1");
		
		data=pdtw.getData();
		if (numTempPars>0){
			dataT=pdtw.getDataT();
			sdsT=pdtw.getSDTemp();
		}
		dataA=pdtw.getDataAmp();
		dataL=pdtw.getDataLoc();
			
		sds=pdtw.getSD();	
		
		
		if (numTempPars>0){
			validTempPars=pdtw.getValidTempPar();
		}
		//System.out.println("2");
		sdRatio=pdtw.getSDRatio();
		sdRatioNT=pdtw.getSDRatioNT();
		validParameters=pdtw.getValidParameters();
		weightByAmp=pdtw.getWeightByAmp();
		squared=pdtw.getSquared();
		//System.out.println("3");
		normaliseWithSDs=pdtw.getNormaliseWithSds();
		alignPoints=pdtw.getAlignmentPoints();
		interpolateWarp=pdtw.getInterpolateWarp();
		dynamicWarp=pdtw.getDynamicWarp();
		maximumWarp=pdtw.getMaximumWarp()*0.01;
		
		this.maxlength=maxlength;
		this.scores=scores;		
		this.start=start;
		this.stop=stop;
		this.f=f;

		dataFocal=new double [data[f].length][];
		dataFocalT=new double[dataT[f].length][];
		dataFocalA=new double[dataA[f].length];
		dataFocalL=new int[dataL[f].length];
		
		for (int i=0; i<dataFocal.length; i++){
			dataFocal[i]=new double[data[f][i].length];
			System.arraycopy(data[f][i], 0, dataFocal[i], 0, dataFocal[i].length);
		}
		for (int i=0; i<dataFocalT.length; i++){
			dataFocalT[i]=new double[dataT[f][i].length];
			System.arraycopy(dataT[f][i], 0, dataFocalT[i], 0, dataFocalT[i].length);
		}
		
		System.arraycopy(dataA[f], 0, dataFocalA, 0, dataFocalA.length);
		System.arraycopy(dataL[f], 0, dataFocalL, 0, dataFocalL.length);
		
		l1=dataFocal[0].length;
		
		dims=dataFocal.length;
		dimsT=dataFocalT.length;
		if (saveMatrix){System.out.println("PREPARED");}
	}
	
	
	
	
	
	/**
	 * Run method for thread
	 */
	public synchronized void run(){
		
		p=new double[maxlength][maxlength];
		q=new double[maxlength][maxlength];
		r=new double[maxlength][maxlength];
		s=new double[maxlength][maxlength];
		d2=new double[maxlength];
		seg1=new double[dims][maxlength];
		seg1t=new double[dimsT][maxlength];
		bestMatrix=new double[maxlength][maxlength];
		trajectory=new int[2*maxlength][2];
		bestTrajectory=new int[2*maxlength][2];
		if (saveMatrix){
			System.out.println(f+" "+start+" "+stop);
		}
		
		double[] scoresl=new double[scores.length];
		
		for (int i=start; i<stop; i++){
			
			boolean cont=true;
			if (checkcompmat){
				
				cont=compmat[f][start];
				
			}
			
			if (cont){
			
				
				
				
				dataComp=data[i];
				dataCompA=dataA[i];
				dataCompL=dataL[i];
				
				
				
			
				l2=dataComp[0].length;
				if ((l1==0)||(l2==0)){
					System.out.println("Lengths: "+l1+" "+l2);
				}
				if (numTempPars>0){
					dataCompT=dataT[i];
				}
				
				
			
				scoresl[i]=derTimeWarpingAsym();
			}
			System.out.println("COMP: "+f+" "+i+" "+scoresl[i]);
		}
	}
	
	
	
	/**
	 * Method called from Thread's run method.
	 * This carries out three DTW algorithms, one with the shorter array centre-aligned, one with
	 * it left-aligned, and one with it right-aligned
	 * @return a double dissimilarity score.
	 */
			
	public double derTimeWarpingAsym (){
		double scoreb=10000000;
		if ((alignPoints==0)||(alignPoints>2)||(numTempPars==0)){
			timediff=0;
			double score1=runComp();
			
			if (score1<scoreb){
				scoreb=score1;
				if (saveMatrix){
					copyS();
				}
			}
		}
		
		if (numTempPars>0){
			
			double diff=dataFocalT[0][l1-1]-dataCompT[0][l2-1];
			
			if ((alignPoints==1)||(alignPoints>2)){
				//for (int i=0; i<dataFocalT.length; i++){
					//dataFocalT[i]=dataFocalTX[i]+diff;
				//}
				timediff=diff;
				double score3=runComp();
				if (score3<scoreb){
					scoreb=score3;
					if (saveMatrix){
						copyS();
					}
				}
			}
		
			if (alignPoints>1){
				//double diff2=diff*0.5;
				//for (int i=0; i<dataFocalT.length; i++){
					//dataFocalT[i]=dataFocalTX[i]+diff2;
				//}
				timediff=diff*0.5;
				double score5=runComp();
				if (score5<scoreb){
					scoreb=score5;
					if (saveMatrix){
						copyS();
					}
				}
			}
		
			if (alignPoints>3){
				
				//double diff2=diff*0.25;
				//for (int i=0; i<dataFocalT.length; i++){
					//dataFocalT[i]=dataFocalTX[i]+diff2;
				//}
				timediff=diff*0.25;
				double score5=runComp();
				if (score5<scoreb){
					if (saveMatrix){
						copyS();
					}
					scoreb=score5;
				}
				//double diff3=diff*0.75;
				//for (int i=0; i<dataFocalT.length; i++){
					//dataFocalT[i]=dataFocalTX[i]+diff3;
				//}
				timediff=diff*0.75;
				double score6=runComp();
				if (score6<scoreb){
					scoreb=score6;
					if (saveMatrix){
						copyS();
					}
				}
			}
		}
		
		/*
		if (saveMatrix){
			for (int id=0; id<dims; id++){	
				System.out.println(id+" "+meanErrors[id]);
				
			}
			for (int id=0; id<dimsT; id++){		
				System.out.println(id+" T "+meanErrorsT[id]);
			}
		}
		*/
		return scoreb;
	}
	
	public void copyS(){
		
		for (int i=0; i<s.length; i++){
			for (int j=0; j<s[i].length; j++){
				bestMatrix[i][j]=s[i][j];
			}
		}
		
		for (int i=0; i<trajectory.length; i++){
			for (int j=0; j<2; j++){
				bestTrajectory[i][j]=trajectory[i][j];
			}
		}
		
		calculateErrors();
		
	}
	
	public double getJointSD(double[] data1, double[] data2, double[] weights1, double[] weights2, boolean weight){
		
		double a;
		
		double avs1=0;
		double sum1=0;
		double sd1=0;
		
		double avs2=0;
		double sum2=0;
		double sd2=0;

		for (int j=0; j<data1.length; j++){
			if (weight){
				avs1+=data1[j]*weights1[j];
				sum1+=data1[j];
			}
			else{
				avs1+=data1[j];
				sum1++;
			}
		}
		for (int j=0; j<data2.length; j++){
			if (weight){
				avs2+=data2[j]*weights2[j];
				sum2+=weights2[j];
			}
			else{
				avs2+=data2[j];
				sum2++;
			}
		}
		if (sum1<=0){sum1=1;}
		if (sum2<=0){sum2=1;}
		
		avs1/=sum1;
		avs2/=sum2;
		
		for (int j=0; j<data1.length; j++){
				//if (i==1){System.out.println(data1[i][j]);}
			a=data1[j]-avs1;
			if (weight){
				sd1+=a*a*weights1[j];
			}
			else{
				sd1+=a*a;
			}
		}
		for (int j=0; j<data2.length; j++){
			a=data2[j]-avs2;
			if (weight){
				sd2+=a*a*weights2[j];
			}
			else{
				sd2+=a*a;
				
			}
		}	

		
		//sd1=0.5*(Math.sqrt(sd1/(sum1))+Math.sqrt(sd2/(sum2)));
		sd1=0.5*(Math.sqrt((sd1+sd2)/(sum1+sum2)));
			//sd1[i]=Math.max(Math.sqrt(sd1[i]/(sum1[i])),Math.sqrt(sd2[i]/(sum2[i])));
			//sd1[i]=Math.sqrt((sd1[i]+sd2[i])/(sum1[i]+sum2[i]-1.0));
			//System.out.println((i+1)+" "+sd1[i]+" "+avs[i]);
		//sd1[0]=0.5*(data1[0][data1[0].length-1]+data2[0][data2[0].length-1]);
		//if (sd1+sd2<=0){System.out.println(sd1+" "+sd2);}
		return (sd1);
	}

	
	/**
	 * This method is called by  derTimeWarpingAsym. It calculates the standard deviations
	 * for the various parameters in the matrix. It then runs the actual comparison.
	 * @return a double value for dissimilarity
	 */
	
	public double runComp(){
		
		double[] sd=new double[sds.length];
		
		//double sdT=0.5*(dataFocal[0][dataFocal[0].length-1]+dataComp[0][dataComp[0].length-1]);
		//double[] sdtest={0.3003265323335812, 0.14223617119007287, 0.8977044023342039, 0.7196754540708244};
		//System.out.println(sdRatio+" "+sdRatioNT);
		double totweight=0;	
		for (int i=0; i<dims; i++){
			totweight+=validParameters[i];
		}	
		/*
		StringBuffer sb=new StringBuffer();
		for (int i=0; i<dims; i++){
			sb.append(validParameters[i]+" ");
		}	
		sb.append(validTempPars);
		System.out.println(sb.toString());
		*/
		double[] sdT=new double[dimsT];
		for (int i=0; i<dimsT; i++){
			totweight+=validTempPars[i];
		}
		
		for (int i=0; i<dimsT; i++){
			sdT[i]=Math.max(dataFocalT[i][l1-1]-dataFocalT[i][0],dataCompT[i][l2-1]-dataCompT[i][0]);
			//System.out.println(sdT[i]+" "+dataFocalT[i][l1-1]+" "+dataFocalT[i][0]+" "+dataCompT[i][l2-1]+" "+dataCompT[i][0]);
			sdT[i]=sdT[i]*sdRatio+(1-sdRatio)*sdsT[i];
			sdT[i]=validTempPars[i]/(totweight*sdT[i]);
			//System.out.println("p: "+validTempPars[i]+" "+totweight+" "+i+" "+sdT[i]);
		}	
		
		for (int i=0; i<dims; i++){
			sd[i]=sds[i];
			if (sdRatioNT>0){
				double sdp=getJointSD(dataFocal[i], dataComp[i], dataFocalA, dataCompA, weightByAmp);
				
				sd[i]=(1-sdRatioNT)*(sds[i])+sdRatioNT*sdp;
				//System.out.println(sds[i]+" "+sdp+" "+sd[i]);
			}
			//sd[i]=1/sdtest[i];
			//if ((Double.isNaN(sd[i]))||(sd[i]==0)){
				//sd[i]=sds[i];
				//sd[i]=sdtest[i];
				//System.out.println("Extreme sd ratio problem! "+" "+sd[i]+" "+sds[i]);
			//}
			if (normaliseWithSDs){
				sd[i]=validParameters[i]/(totweight*sd[i]);
			}
			else{
				sd[i]=validParameters[i]/(totweight);
			}
		}
		//for (int i=0; i<dims; i++){
			//System.out.print(sd[i]+ " ");
		//}
		//System.out.println("x "+sdT[0]+" "+totweight);
		double score1=0;
		if (interpolateWarp){
			score1=derTimeWarpingPointInterpol(sdT, sd);
		}
		else{
			score1=derTimeWarpingPoint(sdT, sd);
		}
		//float score1=derTimeWarpingPointFast(dataFocal, dataComp, sd);
		return score1;
	}
	
	/**
	 * This method carries out Luscinia's implementation of dynamic time-warping.
	 * It treats time differently from other parameters
	 * It interpolates between points in an asymmetric manner. This helps when points vary rapidly
	 * over time
	 * It can detect break-points in the time series (i.e. different elements) and doesn't interpolate over break-points
	 * @param sdt standard deviation for time parameter
	 * @param sdf standard deviation for other parameters
	 * @return double value of dissimilarity between two time series.
	 */
	public double derTimeWarpingPointInterpol (double[] sdt, double[] sdf){
		
		
		int length1=l1;
		int length2=l2;
		double sc, d1, a1, b1, c1, xx1, xx2, x1, x2, st;
		int id, i, j, k;
		double smallnum=0.000000001;
		x1=0;
		x2=0;
		
		int length3=length2-1;
		
		for (i=0; i<length2; i++){
			for (id=0; id<dims; id++){
				seg1[id][i]=dataComp[id][i]*sdf[id];
			}
			for (id=0; id<dimsT; id++){
				seg1t[id][i]=dataCompT[id][i]*sdt[id];
			}
		}
		for (i=0; i<length3; i++){
			j=i+dataCompL[i];
			d2[i]=0;
			for (id=0; id<dims; id++){
				a1=seg1[id][i];
				b1=seg1[id][j];
				c1=b1-a1;
				d2[i]+=c1*c1;
			}
			for (id=0; id<dimsT; id++){
				a1=seg1t[id][i];
				b1=seg1t[id][j];
				c1=b1-a1;
				d2[i]+=c1*c1;
			}
		}

		
		//the following section finds the distances between points in dataFocal to point-point segments in dataComp using trig.
		//it also generates the length1 x length3 distance matrix between dataFocal and the dataComp segments, s.
		
		for (i=0; i<length1; i++){
			for (j=0; j<length3; j++){
				k=j+dataCompL[j];
				s[i][j]=0;
				xx1=0;
				xx2=0;
				for (id=0; id<dims; id++){
					d1=dataFocal[id][i]*sdf[id];
					xx1+=(d1-seg1[id][j])*(d1-seg1[id][j]);
					xx2+=(d1-seg1[id][k])*(d1-seg1[id][k]);
				}
				for (id=0; id<dimsT; id++){
					d1=(dataFocalT[id][i]+timediff)*sdt[id];
					xx1+=(d1-seg1t[id][j])*(d1-seg1t[id][j]);
					xx2+=(d1-seg1t[id][k])*(d1-seg1t[id][k]);
				}
				
				//IF syllables are stitched, don't interpolate BETWEEN notes
				st=0;
				if(k==j){
					//s[i][j]=Math.sqrt(Math.min(xx1, xx2));	
					st=Math.min(xx1, xx2);
				}
				
				//IF params are equal for the two points in dataComp, don't try to interpolate between them
				else if (d2[j]<smallnum){
					st=Math.min(xx1, xx2);
					//s[i][j]=Math.sqrt(Math.min(xx1, xx2));
				}
				else{
					
					//is first angle obtuse? Law of cosines
					if ((xx2-d2[j]-xx1)>0){
						//x1=Math.sqrt(xx1);
						//s[i][j]=x1;
						st=xx1;
					}
					//is second angle obtuse?
					else if ((xx1-d2[j]-xx2)>0){
						//x2=Math.sqrt(xx2);
						//s[i][j]=x2;
						st=xx2;
					}
					else{
						sc=xx2+d2[j]-xx1;
						sc=xx2-(sc*sc/(4*d2[j]));
						if (sc<smallnum){
							sc=0;
							st=0;
							//s[i][j]=0;
						}
						else{
							st=sc;
							//s[i][j]=Math.sqrt(sc);
							//if (Double.isNaN(s[i][j])){
								//System.out.println("ISNANX: "+sc+" "+d2[j]+" "+xx1+" "+xx2);
						//	}	
						}	
					}	
				}
				if (squared){
					s[i][j]=st;
				}
				else{
					s[i][j]=Math.sqrt(st);
				}
			}
		}
				
		double score=0;
		if (dynamicWarp){
			score=timeWarping(length1, length3);
		}
		else{
			score=linearComp(length1, length3);
		}
		return score;		
	}
	
	/**
	 * This method carries out Luscinia's implementation of dynamic time-warping.
	 * It treats time differently from other parameters
	 * This version does NOT interpolate between points.
	 * It can detect break-points in the time series (i.e. different elements) and doesn't interpolate over break-points
	 * @param sdt standard deviation for time parameter
	 * @param sdf standard deviation for other parameters
	 * @return double value of dissimilarity between two time series.
	 */
	public double derTimeWarpingPoint (double[] sdt, double[] sdf){
		
		int length1=l1;
		int length2=l2;
		double d1, d2, x1;
		int id, i, j;
		
		//the following section finds the distances between points in dataFocal to point-point segments in dataComp using trig.
		//it also generates the length1 x length3 distance matrix between dataFocal and the dataComp segments, s.
		
		for (i=0; i<length1; i++){
			for (j=0; j<length2; j++){
				x1=0;
				for (id=0; id<dims; id++){
					d1=dataFocal[id][i]*sdf[id];
					d2=dataComp[id][j]*sdf[id];
					d1=d1-d2;
					x1+=d1*d1;
				}
				for (id=0; id<dimsT; id++){
					d1=(dataFocalT[id][i]+timediff)*sdt[id];
					d2=dataCompT[id][j]*sdt[id];
					d1=d1-d2;
					x1+=d1*d1;
				}
				if (squared){
					s[i][j]=x1;
				}
				else{
					s[i][j]=Math.sqrt(x1);
				}
			}
		}
		
		double score=0;
		if (dynamicWarp){
			score=timeWarping(length1, length2);
		}
		else{
			score=linearComp(length1, length2);
		}
		return score;
		
	}
	
	
	/**
	 * This method actually carries out the time warping algorithm, based on a previously calculated
	 * dissimilarity matrix (stored in s).
	 * @param length1 length of first input time series
	 * @param length2 length of second input time series
	 * @return double score of dissimilarity.
	 */
	
	public double timeWarping(int length1, int length2){
		
		int x, y, i, j, k, locx, locy;
		double min, sc, s2, f;
		double thresh=Math.max(length1, length2)*maximumWarp;
		double nthresh=-1*thresh;
		double sl=1/Math.sqrt(length2*length2+length1*length1);
		
		
		r[0][0]=s[0][0];
		p[0][0]=1;
		q[0][0]=s[0][0];
		if (weightByAmp){
			q[0][0]=s[0][0]*dataFocalA[0];
			p[0][0]=dataFocalA[0];
		}
		for (i=0; i<length1; i++){
			for (j=0; j<length2; j++){
				s2=s[i][j];
				f=((i+1)*length2-(j+1)*length1)*sl;
				if ((f>thresh)||(f<nthresh)){
					s2=100000000;
				}
				min=1000000000;
				locx=0;
				locy=0;
				for (k=0; k<3; k++){
					x=i+locsX[k];
					y=j+locsY[k];
							
					if ((x>=0)&&(y>=0)){
						sc=r[x][y];
						if (sc<min){
							min=sc;
							locx=x;
							locy=y;
						}
					}
				}
				if (min<1000000000){
					r[i][j]=min+s2;
					if (weightByAmp){
						q[i][j]=q[locx][locy]+s2*dataFocalA[i];
						p[i][j]=p[locx][locy]+dataFocalA[i];
						
						//System.out.println(i+" "+j+" "+q[i][j]+" "+p[i][j]);
						
					}
					else{
						q[i][j]=q[locx][locy]+s2;
						p[i][j]=p[locx][locy]+1;
					}
				}
			}
		}
		
		if (saveMatrix){
			
			al1=length1;
			al2=length2;
			
			int a1=length1-1;
			int a2=length2-1;
	
			trajectory[0][0]=a1;
			trajectory[0][1]=a2;
			int co=1;
			while ((a1>0)||(a2>0)){
				min=1000000000;
				locx=0;
				locy=0;
				for (k=0; k<3; k++){
					x=a1+locsX[k];
					y=a2+locsY[k];
							
					if ((x>=0)&&(y>=0)){
						sc=q[x][y];
						if (sc<min){
							min=sc;
							locx=x;
							locy=y;
						}
					}
				}
				a1=locx;
				a2=locy;
				trajectory[co][0]=a1;
				trajectory[co][1]=a2;
				co++;
			}
			
			System.out.println("SCORES: "+q[length1-1][length2-1]+" "+p[length1-1][length2-1]);
			System.out.println(Math.sqrt(q[length1-1][length2-1]/p[length1-1][length2-1]));
		}	
				
		//float result=(float)(r[length1-1][length3-1]/Math.max(length1, length3));
		double result=(q[length1-1][length2-1]/p[length1-1][length2-1]);
		//float result=(float)(r[ba][bb]/den);
		//float result=(float)Math.exp(r[ba][bb]/den);
		//float result=(float)Math.sqrt(r[ba][bb]/den);
		
		if (squared){result=Math.sqrt(result);}
		return result;	
	}
	
	
	public double linearComp(int length1, int length2){
		double score=0;
		double thresh=Math.max(length1, length2)*maximumWarp;
		double nthresh=-1*thresh;
		double sl=1/Math.sqrt(length2*length2+length1*length1);
		//double th2=Math.abs(thresh*length1*Math.sqrt(length2*length2+length1*length1)/(length2*length2-length1*length1));
		double th2=thresh*Math.sqrt(length2*length2+length1*length1)/(length1+0.0);
		//System.out.println(length1+" "+length2+" "+sl+" "+th2);
		double rowsum=0;
		double colsum=0;
		double total=0;
		for (int i=0; i<length1; i++){
			for (int j=0; j<length2; j++){
				double p=((i+1)*length2-(j+1)*length1)*sl;
				if ((p<thresh)&&(p>nthresh)){
					double q=1/(s[i][j]+0.01);
					//q=1/(p+1.0);
					rowsum+=q*i;
					colsum+=q*j;
					total+=q;
				}
			}
		}
		rowsum/=total;
		colsum/=total;
		double xd, yd;
		double num=0;
		double dem=0;
		for (int i=0; i<length1; i++){
			for (int j=0; j<length2; j++){
				double p=((i+1)*length2-(j+1)*length1)*sl;
				if ((p<thresh)&&(p>nthresh)){
					xd=i-rowsum;
					yd=j-colsum;
					double q=1/(s[i][j]+0.01);
					//q=1/(p+1.0);
					num+=xd*yd*q;
					dem+=xd*xd*q;
				}
			}
		}
		double b=num/dem;
		double a=colsum-b*rowsum;
		double tot=0;
		for (int i=0; i<length1; i++){
			int y=(int)Math.round(a+b*i);
			if (y<0){y=0;}
			double ye=i*(length2/(length1+0.0));
			int p=(int)Math.round(ye-th2);
			if (y<p){y=p;}
			if (y>=length2){y=length2;}
			p=(int)Math.round(ye+th2);
			if (y>p){y=p;}
			tot+=s[i][y];
		}
		score=(tot/(length1+0.0));
		
		
		if (saveMatrix){
			al1=length1;
			al2=length2;
			for (int i=0; i<length1; i++){
				int y=(int)Math.round(a+b*i);
				if (y<0){y=0;}
				double ye=i*(length2/(length1+0.0));
				int p=(int)Math.round(ye-th2);
				if (y<p){y=p;}
				if (y>=length2){y=length2;}
				p=(int)Math.round(ye+th2);
				if (y>p){y=p;}
				
				trajectory[i][0]=i;
				trajectory[i][1]=y;
			}
		}
		
		
		
		if (squared){score=Math.sqrt(score);}
		return score;
	}
	
	public double[][] getCompMatrix(){
		return bestMatrix;
	}
	
	public int[] getLengths(){
		return new int[]{al1, al2};
	}
	
	public int[][] getTrajectory(){
		return bestTrajectory;
	}
	
	public void calculateErrors(){
		int n=bestTrajectory.length;
		double[][] errorParam=new double[dims][n];
		double[][] errorParamT=new double[dimsT][n];
		
		for (int i=0; i<n; i++){
			
			int x=bestTrajectory[i][0];
			int y=bestTrajectory[i][1];
			
			for (int id=0; id<dims; id++){	
				double f1=Math.abs(dataFocal[id][x]-dataComp[id][y]);
				double f2=Math.abs(dataFocal[id][x]-dataComp[id][y+1]);
				if (f2<f1){f1=f2;}
				errorParam[id][i]=f1;
			}
			for (int id=0; id<dimsT; id++){	
				double f1=Math.abs(dataFocalT[id][x]+timediff-dataCompT[id][y]);
				double f2=Math.abs(dataFocalT[id][x]+timediff-dataCompT[id][y+1]);
				//System.out.println(i+" "+f1+" "+x+" "+y+" "+dataFocalT[id][x]+" "+dataCompT[id][y]+" "+timediff);
				if (f2<f1){f1=f2;}
				errorParamT[id][i]=f1;
			}
			
			if ((x==0)&&(y==0)){
				n=i+1;
				i=n;
			}
		}
		
		meanErrors=new double[dims];
		meanErrorsT=new double[dimsT];
		
		for (int i=0; i<n; i++){
			for (int id=0; id<dims; id++){	
				meanErrors[id]+=errorParam[id][i];
			}
			for (int id=0; id<dimsT; id++){	
				meanErrorsT[id]+=errorParamT[id][i];
			}
		}
		for (int id=0; id<dims; id++){	
			meanErrors[id]/=n+0.0;		
		}
		for (int id=0; id<dimsT; id++){	
			meanErrorsT[id]/=n+0.0;
		}
		
		sdErrors=new double[dims];
		sdErrorsT=new double[dimsT];
		
		for (int i=0; i<n; i++){
			for (int id=0; id<dims; id++){	
				sdErrors[id]+=(errorParam[id][i]-meanErrors[id])*(errorParam[id][i]-meanErrors[id]);
			}
			for (int id=0; id<dimsT; id++){	
				sdErrorsT[id]+=(errorParamT[id][i]-meanErrorsT[id])*(errorParamT[id][i]-meanErrorsT[id]);
			}
		}
		for (int id=0; id<dims; id++){	
			sdErrors[id]=Math.sqrt(sdErrors[id])/(n-1.0);
		}
		for (int id=0; id<dimsT; id++){	
			sdErrorsT[id]=Math.sqrt(sdErrorsT[id])/(n-1.0);
		}
		
		
	}
	
}
	

