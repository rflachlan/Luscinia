package lusc.net.github.analysis;

/**
 * This class carries out the Dynamic Time Warping on input array data.
 * It is thread-enabled for performance
 * @author Rob
 *
 */

public class CompareThread extends Thread{
	int start, stop, maxlength, dims, dimsE, f;
	boolean weightByAmp;
	boolean normaliseWithSDs=true;
	boolean interpolateWarp=true;
	boolean dynamicWarp=true;
	double maximumWarp=0.25;
	int numTempPars=0;
	boolean stitch;
	double[] scores;
	double[] validParameters, sds, seg2, point2, vec1sq;
	double sdsT, validTempPars;
	double[][] p, q, r, s, t, seg1a, seg1b, vec1;
	int[][] proute;
	double[][] dataFocal, dataFocalE, dataComp, dataCompE;
	double[] dataFocalT, dataCompT;
	int[][] elPos;
	int[] pos;
	int[] locsX={-1,-1,0};
	int[] locsY={0,-1,-1};
	
	int l1, l2;
	
	//ComparisonFrame cf;
	double[][][] data, dataE;
	double[][] dataT;
	
	long time1, time2=0;
	
	double sdRatio=0.5;
	double lowerCutOff=0.02;
	int alignPoints=3;
	
	/**
	 * Depreprecated
	 * @param maxlength
	 * @param data
	 * @param dataT
	 * @param dataE
	 * @param elPos
	 * @param sds
	 * @param sdRatio
	 * @param validParameters
	 * @param weightByAmp
	 * @param scores
	 * @param start
	 * @param stop
	 * @param f
	 */
	
	public CompareThread(int maxlength, double[][][]data, double[][][]dataT, double[][][]dataE, int[][]elPos, double[] sds, double sdRatio,  double[] validParameters, boolean weightByAmp, double[] scores, int start, int stop, int f){
		
 		this.data=data;
 		//this.dataT=dataT;
 		this.dataE=dataE;
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
		//dataFocalT=new double [dataT[f].length][];
		dataFocalE=new double [dataE[f].length][];
		
		
		for (int i=0; i<dataFocal.length; i++){
			dataFocal[i]=new double[data[f][i].length];
			System.arraycopy(data[f][i], 0, dataFocal[i], 0, dataFocal[i].length);
			//dataFocalT[i]=new double[dataT[f][i].length];
			//System.arraycopy(dataT[f][i], 0, dataFocalT[i], 0, dataFocalT[i].length);
			dataFocal[i]=new double[dataE[f][i].length];
			System.arraycopy(dataE[f][i], 0, dataFocalE[i], 0, dataFocalE[i].length);
		}
		//System.out.println(dataFocal.length);
		dims=dataFocal.length;
		if (weightByAmp){
			dims--;
		}
	}
	
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
	public CompareThread(int maxlength, PrepareDTW pdtw, boolean stitch, double[] scores, int start, int stop, int f){
		
		numTempPars=pdtw.getNumTempPars();
		this.stitch=stitch;
		if (stitch){
			data=pdtw.getData(2);
			if (numTempPars>0){
				dataT=pdtw.getDataT(1);
				sdsT=pdtw.getSDTemp(1);
			}
			dataE=pdtw.getData(3);
			elPos=pdtw.getElPos();
			sds=pdtw.getSD(1);	
		}
		else{
			data=pdtw.getData(0);
			if (numTempPars>0){
				dataT=pdtw.getDataT(0);
				sdsT=pdtw.getSDTemp(0);
			}
			dataE=pdtw.getData(1);
			sds=pdtw.getSD(0);
				
		}
		
		if (numTempPars>0){
			validTempPars=pdtw.getValidTempPar();
		}
		
		sdRatio=pdtw.getSDRatio();
		validParameters=pdtw.getValidParameters();
		weightByAmp=pdtw.getWeightByAmp();
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
		dataFocalE=new double [dataE[f].length][];
		
		
		for (int i=0; i<dataFocal.length; i++){
			dataFocal[i]=new double[data[f][i].length];
			System.arraycopy(data[f][i], 0, dataFocal[i], 0, dataFocal[i].length);
		}
		for (int i=0; i<dataFocalE.length; i++){
			dataFocalE[i]=new double[dataE[f][i].length];
			System.arraycopy(dataE[f][i], 0, dataFocalE[i], 0, dataFocalE[i].length);
		}
		l1=dataFocal[0].length;
		
		if (numTempPars>0){
			dataFocalT=new double[dataT[f].length];
			System.arraycopy(dataT[f], 0, dataFocalT, 0, dataFocalT.length);
		}
		
		dims=dataFocal.length;
		dimsE=dataFocalE.length;
	}
	
	/**
	 * Run method for thread
	 */
	public synchronized void run(){
		
		p=new double[maxlength][maxlength];
		q=new double[maxlength][maxlength];
		r=new double[maxlength][maxlength];
		s=new double[maxlength][maxlength];
		t=new double[maxlength][maxlength];
		proute=new int[maxlength][maxlength];
		
		for (int i=start; i<stop; i++){
			dataComp=data[i];
			dataCompE=dataE[i];
			//System.out.println("CHECK: "+dataComp[0].length+" "+dataCompE[0].length);
			l2=dataComp[0].length;
			if (numTempPars>0){
				dataCompT=dataT[i];
			}
			if (stitch){
				pos=elPos[i];
			}
			scores[i]=derTimeWarpingAsym();
		}
	}
	
	
	
	/**
	 * Method called from Thread's run method.
	 * This carries out three DTW algorithms, one with the shorter array centre-aligned, one with
	 * it left-aligned, and one with it right-aligned
	 * @return a double dissimarity score.
	 */
			
	public double derTimeWarpingAsym (){
		double scoreb=10000000;
		if ((alignPoints==0)||(alignPoints>2)||(numTempPars==0)){
			double score1=runComp();
			if (score1<scoreb){
				scoreb=score1;
			}
		}
		
		if (numTempPars>0){
			if ((alignPoints==1)||(alignPoints>2)){
				double diff=dataFocalT[dataFocalT.length-1]-dataCompT[dataComp[0].length-1];
				for (int i=0; i<dataFocalT.length; i++){
					dataFocalT[i]-=diff;
				}
				double score3=runComp();
				if (score3<scoreb){scoreb=score3;}
			}
		
			if (alignPoints>1){
				double diff=dataFocalT[dataFocalT.length-1]-dataCompT[dataComp[0].length-1];
				diff=diff*0.5;
				for (int i=0; i<dataFocalT.length; i++){
					dataFocalT[i]+=diff;
				}
				double score5=runComp();
				if (score5<scoreb){scoreb=score5;}
			}
		
			if (alignPoints>3){
				double diff=dataFocalT[dataFocalT.length-1]-dataCompT[dataComp[0].length-1];
				double diff2=diff*0.25;
				for (int i=0; i<dataFocalT.length; i++){
					dataFocalT[i]+=diff2;
				}
				double score5=runComp();
				if (score5<scoreb){scoreb=score5;}
				double diff3=diff*0.75;
				for (int i=0; i<dataFocalT.length; i++){
					dataFocalT[i]+=diff3;
				}
				double score6=runComp();
				if (score6<scoreb){scoreb=score6;}
			}
		}
		

		
		return scoreb;
	}

	
	/**
	 * This method is called by  derTimeWarpingAsym. It calculates the standard deviations
	 * for the various parameters in the matrix. It then runs the actual comparison.
	 * @return a double value for dissimilarity
	 */
	
	public double runComp(){
		
		double[] sd=new double[sds.length];
		
		//double sdT=0.5*(dataFocal[0][dataFocal[0].length-1]+dataComp[0][dataComp[0].length-1]);
		
		
		double totweight=0;	
		for (int i=0; i<dims; i++){
			totweight+=validParameters[i];
		}	
		
		double sdT=0;
		if (numTempPars>0){
			totweight+=validTempPars;
			sdT=Math.max(dataFocalT[l1-1]-dataFocalT[0],dataCompT[l2-1]-dataCompT[0]);
			sdT=sdT*sdRatio+(1-sdRatio)*sdsT;
			sdT=validTempPars/(totweight*sdT);
		}
		
		
		
		for (int i=0; i<dims; i++){
			sd[i]=sds[i];
			if ((Double.isNaN(sd[i]))||(sd[i]==0)){
				sd[i]=sds[i];
				System.out.println("Extreme sd ratio problem!");
			}
			if (normaliseWithSDs){
				sd[i]=validParameters[i]/(totweight*sd[i]);
			}
			else{
				sd[i]=validParameters[i]/(totweight);
			}
		}
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
	public double derTimeWarpingPointInterpol (double sdt, double[] sdf){
		
		
		int length1=l1;
		int length2=l2;
		double sc, d1, a1, b1, c1, xx1, xx2, x1, x2;
		int id, i, j;
		double smallnum=0.000000001;
		x1=0;
		x2=0;
		
		
		j=0;
		for (i=0; i<length2; i++){
			if (dataCompE[0][i]==0){
				j++;
			}
		}
		
		int length3=j;	
				
		int[] w=new int[length3];
		j=0;
		for (i=0; i<length2; i++){
			if (dataCompE[0][i]==0){
				w[j]=i;
				j++;
			}
		}
		
		
		double[][] seg1=new double[length3][dims];
		double[][] seg2=new double[length3][dims];
		double[] seg1T=null, seg2T=null;
		if (numTempPars>0){
			seg1T=new double[length3];
			seg2T=new double[length3];
		}
		double[] d2=new double[length3];
		double[] d3=new double[length3];
		
		//the following section measures the distances between point-point segments in dataComp
		j=0;
		for (i=0; i<length2; i++){
			if (dataCompE[0][i]==0){
				for (id=0; id<dims; id++){
					a1=dataComp[id][w[j]]*sdf[id];
					b1=dataComp[id][w[j]+1]*sdf[id];
					seg1[j][id]=a1;
					seg2[j][id]=b1;
					c1=b1-a1;
					d2[j]+=c1*c1;
				}
				if (numTempPars>0){
					a1=dataCompT[w[j]]*sdt;
					b1=dataCompT[w[j]+1]*sdt;
					seg1T[j]=a1;
					seg2T[j]=b1;
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
				if (numTempPars>0){
					d1=dataFocalT[i]*sdt;
					xx1+=(d1-seg1T[j])*(d1-seg1T[j]);
					xx2+=(d1-seg2T[j])*(d1-seg2T[j]);
				}
				
				//IF syllables are stitched, don't interpolate BETWEEN notes
				
				if((stitch)&&(pos[j]!=pos[j+1])){
					s[i][j]=Math.sqrt(Math.min(xx1, xx2));	
					
				}
				
				//IF params are equal for the two points in dataComp, don't try to interpolate between them
				else if (d2[j]<smallnum){
					s[i][j]=Math.sqrt(Math.min(xx1, xx2));
				}
				else{
					
					//is first angle obtuse? Law of cosines
					if ((xx2-d2[j]-xx1)>0){
						x1=Math.sqrt(xx1);
						s[i][j]=x1;
					}
					//is second angle obtuse?
					else if ((xx1-d2[j]-xx2)>0){
						x2=Math.sqrt(xx2);
						s[i][j]=x2;
					}
					else{
						sc=xx2+d2[j]-xx1;
						sc=xx2-(sc*sc/(4*d2[j]));
						if (sc<smallnum){
							sc=0;
							s[i][j]=0;
						}
						else{
							s[i][j]=Math.sqrt(sc);
							if (Double.isNaN(s[i][j])){
								System.out.println("ISNANX: "+sc+" "+d2[j]+" "+xx1+" "+xx2);
							
							}	
						}
						
					}	
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
	public double derTimeWarpingPoint (double sdt, double[] sdf){
		
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
				if (numTempPars>0){
					d1=dataFocalT[i]*sdt;
					d2=dataCompT[j]*sdt;
					d1=d1-d2;
					x1+=d1*d1;
				}
				s[i][j]=Math.sqrt(x1);
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
		double thresh=length1*maximumWarp;
		double nthresh=-1*thresh;
		double sl=1/Math.sqrt(length2*length2+length1*length1);
		
		
		r[0][0]=s[0][0];
		p[0][0]=1;
		q[0][0]=s[0][0];
		if (weightByAmp){
			q[0][0]=s[0][0]*dataFocalE[1][0];
			p[0][0]=dataFocalE[1][0];
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
						q[i][j]=q[locx][locy]+s2*dataFocalE[1][i];
						p[i][j]=p[locx][locy]+dataFocalE[1][i];
					}
					else{
						q[i][j]=q[locx][locy]+s2;
						p[i][j]=p[locx][locy]+1;
					}
				}
			}
		}
				
				
		//float result=(float)(r[length1-1][length3-1]/Math.max(length1, length3));
		double result=(q[length1-1][length2-1]/p[length1-1][length2-1]);
		//float result=(float)(r[ba][bb]/den);
		//float result=(float)Math.exp(r[ba][bb]/den);
		//float result=(float)Math.sqrt(r[ba][bb]/den);
		return result;	
	}
	
	
	public double linearComp(int length1, int length2){
		double score=0;
		double thresh=length1*maximumWarp;
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
		
		return score;
	}
	
	
	
}
	

