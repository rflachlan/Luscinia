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
	int numTempPars=0;
	boolean stitch;
	float[] scores;
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
	
	public CompareThread(int maxlength, double[][][]data, double[][][]dataT, double[][][]dataE, int[][]elPos, double[] sds, double sdRatio,  double[] validParameters, boolean weightByAmp, float[] scores, int start, int stop, int f){
		
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
	public CompareThread(int maxlength, PrepareDTW pdtw, boolean stitch, float[] scores, int start, int stop, int f){
		
		numTempPars=pdtw.getNumTempPars();
		
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
			sds=pdtw.getSD(1);
				
		}
		
		if (numTempPars>0){
			validTempPars=pdtw.getValidTempPar();
		}
		
		sdRatio=pdtw.getSDRatio();
		validParameters=pdtw.getValidParameters();
		weightByAmp=pdtw.getWeightByAmp();
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
	 * @return a float dissimarity score.
	 */
			
	public float derTimeWarpingAsym (){
		
		float score1=runComp();
		
		float scoreb=score1;
		
		if (numTempPars>0){
			double diff=dataFocalT[dataFocalT.length-1]-dataCompT[dataComp[0].length-1];
			for (int i=0; i<dataFocalT.length; i++){
				dataFocalT[i]-=diff;
			}
			float score3=runComp();
			if (score3<scoreb){scoreb=score3;}
			diff=diff*0.5;
			for (int i=0; i<dataFocalT.length; i++){
				dataFocalT[i]+=diff;
			}
			float score5=runComp();
			if (score5<scoreb){scoreb=score5;}
		}
		
		if (Float.isNaN(score1)){
			System.out.println("DTW Made a NaN");
			System.out.println();
		}
		
		return scoreb;
	}

	
	/**
	 * This method is called by  derTimeWarpingAsym. It calculates the standard deviations
	 * for the various parameters in the matrix. It then runs the actual comparison.
	 * @return a float value for dissimilarity
	 */
	
	public float runComp(){
		
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
			sd[i]=validParameters[i]/(totweight*sd[i]);
		}
		
		float score1=derTimeWarpingPointInterpol(sdT, sd);
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
	 * @return float value of dissimilarity between two time series.
	 */
	public float derTimeWarpingPointInterpol (double sdt, double[] sdf){
		
		
		int length1=l1;
		int length2=l2;
		int length21=dataCompE.length-1;
		double min, sc, sc2, d1, a1, b1, c1, xx1, xx2, xx3, x1, x2, x3;;
		int x,y,z, id, i, j,k, locx, locy;
		double smallnum=0.000000001;
		x1=0;
		x2=0;
		x3=0;
		sc2=0;
		
		
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
		double[] seg1T=new double[length3];
		double[] seg2T=new double[length3];
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
				a1=dataCompT[w[j]]*sdt;
				b1=dataCompT[w[j]+1]*sdt;
				seg1T[j]=a1;
				seg2T[j]=b1;
				c1=b1-a1;
				d2[j]+=c1*c1;
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
				d1=dataFocalT[i]*sdt;
				xx1+=(d1-seg1T[j])*(d1-seg1T[j]);
				xx2+=(d1-seg2T[j])*(d1-seg2T[j]);
				
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
			}
		}
				

		//Finally here is the actual DTW algorithm, searching for the best route through x.
		//the search is weighted by the path length (p), but user variable weightByAmp can mean that the end score is weighted by amplitude in dataFocal instead.
		
		r[0][0]=s[0][0];
		p[0][0]=1;
		q[0][0]=s[0][0];
		if (weightByAmp){
			q[0][0]=s[0][0]*dataFocalE[1][0];
			p[0][0]=dataFocalE[1][0];
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
						q[i][j]=q[locx][locy]+s[i][j]*dataFocalE[1][i];
						p[i][j]=p[locx][locy]+dataFocalE[1][i];
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
	
	
	/**
	 * This method carries out dynamic time warping without interpolation. Currently unused,
	 * but I'd like to add it back in as an option in the future. 
	 * @param dataFocal Focal time series
	 * @param dataComp Comparator time series
	 * @param sdf set of standard deviations for normalisation
	 * @return float value of dissimilarity.
	 */
	
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
}
	

