package lusc.net.github.sound;

import java.util.Arrays;

public class SpectrogramOperations {
	
	Song song;
	public int dynEqual=0;
	public double dynRange=40; 
	public float dynMax=10; 
	public int echoRange=50;
	public double echoComp=0.4f; 
	public int noiseLength1=200; 
	public int noiseLength2=15; 
	public boolean spectEnhance=false;
	boolean setRangeToMax=true; 
	boolean relativeAmp=true; 
	public float noiseRemoval=0f; 
	float maxDB=0;
	
	float[][] spect;
	int ny=0;
	int nx=0;
	
	double octstep;
	double[] phase;
	double fundAdjust;
	
	int minFreq=100;
	
	
	public SpectrogramOperations(Song song) {
		this.song=song;
	}
	
	public void clearUp() {
		spect=null;
	}

	
	public void copySpectrogram(Spectrogram sp) {
		ny=sp.ny;
		nx=sp.nx;
		spect=new float[ny][nx];
		for (int i=0; i<ny; i++) {
			for (int j=0; j<nx; j++) {
				spect[i][j]=sp.spectrogram[i][j];
			}
		}
	}
	

	/**
	 * This method carries spect a process of dynamic equalization, making each window of
	 * a certain length have at least one black point
	 */
	void dynamicEqualizer(Spectrogram sp){			//a dynamic equalizer; key parameter (dynEqual) varies time-range over which spectrogram is normalized
		if (dynEqual>0) {
			int dynEqual2=(int)(dynEqual/sp.dx);
			if (dynEqual2>nx){dynEqual2=nx;}
			int range2=-1*dynEqual2+1;
			float max=0;
			int i,j, k;
			float[] tops=new float[nx];
			for (i=0; i<nx; i++){
				for (j=0; j<ny; j++){
					if (spect[j][i]>tops[i]){tops[i]=spect[j][i];}
				}
			}
			for (i=0; i<nx; i++){
				max=0;
				for (j=range2; j<dynEqual2; j++){
					k=j+i;
					if ((k>=0)&&(k<nx)&&(tops[k]>max)){max=tops[k];}
				}
				if (max>0){
					max=sp.maxAmp/max;				//I'm still not sure maxAmp is appropriate here!
					for (j=0; j<ny; j++){spect[j][i]*=max;}
				}
			}
			tops=null;
		}
	}
	
	/**
	 * This function adjusts spectrogram for dynamic range, leaving values that are to be
	 * coloured white in the spectrogram <0
	 */
	void dynamicRange(Spectrogram sp){		//converts power spectrum into logarithmic, decibel scale (relative to maxPossAmp).
		float mpa=sp.maxPossAmp;
		if (relativeAmp) {
			mpa=sp.maxAmp;
		}
		double logd=10/(Math.log(10));
		double maxC=dynRange-Math.log(mpa)*logd;

		int i,j;
		float maxAmp2=0;
		for (i=0; i<ny; i++){
			for (j=0; j<nx; j++){
				if (spect[i][j]>0){
					spect[i][j]=(float)(Math.log(spect[i][j])*logd+maxC);
				}
				if (spect[i][j]>maxAmp2){maxAmp2=spect[i][j];}
			}
		}
	}	
	
	
	/**
	 * This algorithm does simple noise removal using a median filter 
	 */
	void noiseRemoval(Spectrogram sp) {
		if (noiseRemoval>0){
			Matrix2DOperations m2o=new Matrix2DOperations();
			spect=m2o.medianFilterNR(noiseLength1, 0.25f, noiseRemoval, spect, 0);
			spect=m2o.medianFilterNR1(noiseLength2, 0.75f, 0, spect, 0);
		}
	}
	
	
	
	/**
	 * This organises the process of dereverberation
	 */
	
	void echoRemoval(Spectrogram sp) {
		if ((echoComp>0)&&(echoRange>0)){
			Matrix2DOperations m2o=new Matrix2DOperations();
			int noisePasses=1;
			for (int i=0; i<noisePasses; i++){
				float[][] o=echoAverager(sp);
				spect=m2o.matrixSubtract(spect, o);
			}
		}
		
	}
	

	/**
	 * This is an algorithm for dereverberation in a spectrogram.
	 * Algorithm looks for maximum in previous echorange time-steps. 
	 * @return a float[][] of same size as spect that has reduced reverberation.
	 */
	private float[][] echoAverager(Spectrogram sp){		//Echo removal algorithm. 

		int echoRange2=(int)(echoRange/sp.dx);
		if (echoRange2>nx){echoRange2=nx;}
		
		
		double minWeight=5;
		double decayFactor=(minWeight)/(echoRange2+0.0);
		
		int i,j,k;
		
		double[] buffer=new double[echoRange2];
		int position=0;
		double bestBuf;
		
		float[][]t=new float[ny][nx];
		for (i=0; i<ny; i++){
			for (k=0; k<echoRange2; k++){
				buffer[k]=-1000000000;
			}
			position=0;
			
			for (j=0; j<nx; j++){
				buffer[position]=spect[i][j];
				
				bestBuf=0;

				for (k=0; k<echoRange2; k++){	
					if (buffer[k]>bestBuf){
						bestBuf=buffer[k];
					}

					buffer[k]-=decayFactor;
				}
				bestBuf=echoComp*(bestBuf-spect[i][j]);
				
				if (bestBuf>0){
					t[i][j]=(float)bestBuf;
				}
				else{
					t[i][j]=0f;
				}
				position++;
				if (position==echoRange2){position=0;}
				
			}
		}
		return t;
	}
	
	
	void spectrogramEnhancement(){
		if (spectEnhance) {
		int wi=5;
		int nreps=10;
		double enhp=0.1;
		
		float[][]t=new float[ny][nx];
		
		float[] maxes=new float[nx];
		for (int i=0; i<nx; i++){
			float max=0;
			for (int j=0; j<ny; j++) {
				if (spect[j][i]>max) {max=spect[j][i];}
			}
			maxes[i]=max;
		}
		
		int q=0;
		
		double[] di=new double[(wi*2+1)*(wi*2+1)];
		
		for (int i= -wi; i<wi+1; i++) {
			for (int j= -wi; j<wi+1; j++) {
				di[q]=1/(1+Math.sqrt(i*i+j*j));
				q++;
			}
		}
		
		
		for (int reps=0; reps<nreps; reps++) {
			for (int i=0; i<nx; i++){				
				
				for (int j=0; j<ny; j++) {
					if (spect[j][i]>0) {
						double max=-100000;
						for (int jj=j-wi; jj<j+wi+1; jj++) {
							for (int kk=i-wi; kk<i+wi+1; kk++) {
								if ((jj!=j)||(kk!=i)) {
									if ((jj>=0)&&(jj<ny)&&(kk>=0)&&(kk<nx)){
										if (spect[jj][kk]>max) {
											max=spect[jj][kk];
										}
									}
								}
							}
						}
						
						
						
						
						
						
						float x=(float)((max-spect[j][i])*enhp);
						t[j][i]=spect[j][i]-x;
					}
					
				}
				
			}
			for (int i=0; i<nx; i++){
				float max2=0;
			
				for (int j=0; j<ny; j++) {
					if (t[j][i]>max2) {max2=t[j][i];}
				}
				float max=maxes[i]-max2;
				for (int j=0; j<ny; j++) {
					spect[j][i]=t[j][i];
				}
			}
			
		}
		
		
		spectrogramEnhancement2();
		}
	}
	
	private void spectrogramEnhancement2(){
		int wi=5;
		int nreps=5;
		double enhp=0.25;
		
		float[][]t=new float[ny][nx];
		
		float[] maxes=new float[nx];
		for (int i=0; i<nx; i++){
			float max=0;
			for (int j=0; j<ny; j++) {
				if (spect[j][i]>max) {max=spect[j][i];}
			}
			maxes[i]=max;
		}
		
		int q=0;
		
		double[] di=new double[(wi*2+1)*(wi*2+1)];
		
		for (int i= -wi; i<wi+1; i++) {
			for (int j= -wi; j<wi+1; j++) {
				di[q]=1/(1+Math.sqrt(i*i+j*j));
				q++;
			}
		}
		
		
		for (int reps=0; reps<nreps; reps++) {
			for (int i=0; i<nx; i++){				
				
				for (int j=0; j<ny; j++) {
					if (spect[j][i]>0) {
						double sx=0;
						double sxx=0;
						double sxy=0;
						double sy=0;
						double c=0;
						double sz=0;
						double szz=0;
						double szy=0;
						double sxz=0;
						double p=0;
						q=0;
						for (int jj=j-wi; jj<j+wi+1; jj++) {
							for (int kk=i-wi; kk<i+wi+1; kk++) {
								if ((jj>=0)&&(jj<ny)&&(kk>=0)&&(kk<nx)){
									p=di[q];
									//p=1/(1+Math.sqrt((kk-i)*(kk-i)+(jj-j)*(jj-j)));
									sx+=jj*p;
									sxx+=jj*jj*p;
									sxy+=jj*spect[jj][kk]*p;
									sy+=spect[jj][kk]*p;
									sz+=kk*p;
									szz+=kk*kk*p;
									szy+=kk*spect[jj][kk]*p;
										
									sxz+=kk*jj*p;
									c+=p;
									
								}
								q++;
							}
						}
						
						c=1/c;
						
						sxx=sxx-(sx*sx*c);
						szz=szz-(sz*sz*c);
						sxy=sxy-(sx*sy*c);
						szy=szy-(sz*sy*c);
						sxz=sxz-(sx*sz*c);
						
						
						double b1=(szz*sxy-sxz*szy)/(sxx*szz-sxz*sxz);
						double b2=(sxx*szy-sxz*sxy)/(sxx*szz-sxz*sxz);
						
						double b=Math.sqrt(b1*b1+b2*b2);

						
						
						float x=(float)(spect[j][i]*b*enhp);
						
						float mean=(float)(sy*c*0.25);
						
						if (spect[j][i]<0) {x*=-1f;}
						t[j][i]=spect[j][i]-x+mean;
					}
					
				}
				
			}
			for (int i=0; i<nx; i++){
				float max2=0;
			
				for (int j=0; j<ny; j++) {
					if (t[j][i]>max2) {max2=t[j][i];}
				}
				float max=maxes[i]-max2;
				for (int j=0; j<ny; j++) {
					spect[j][i]=t[j][i];
				}
			}
			
		}		
	}
	
	/**
	 * This method carries out a process of dynamic equalization. 
	 */
	void equalize(Spectrogram sp){
		maxDB=-10000;
		int i,j;
		int startx=0;
		if (echoComp>0){startx=(int)(echoRange/sp.dx);}
		for (i=0; i<spect.length; i++){
			for (j=startx; j<spect[i].length; j++){
				if (spect[i][j]>maxDB){maxDB=spect[i][j];}
			}
		}
		if (setRangeToMax){
			dynMax=(float)(maxDB/dynRange);
		}
	}
	
	private double[] signalToNoise(int min, int max) {
		
		double[] snr=new double[nx];
		int sigrange=3;
		
		for (int i=0; i<nx; i++) {
			
			double maxp=-100;
			int maxloc=-1;
			for (int j=min; j<max; j++) {
				if (spect[j][i]>maxp){
					maxloc=i;
					maxp=spect[j][i];
				}
			}
			
			double sig=0;
			double noi=0;
			
			for (int j=min; j<max; j++) {
				if (Math.abs(j-maxloc)<sigrange) {
					sig+=spect[j][i];
					
				}
				else {
					noi+=spect[j][i];
				}
			}
			
			snr[i]=sig-noi;	
		}
		
		return snr;
		
	}
	
	
	/**
	 * Method to measure Wiener Entropy
	 * @param tList input signal location
	 * @param results spectput WE trajectories
	 */
	private double[] measureWienerEntropy(){
	
		//Calculates the Wiener Entropy (ratio of Geometric:Arithmetic means of the power spectrum).
		//Uses the fact that spectrogram is already in decibel format to speed-up calculation of the geometric mean.

		
		double[] wienerEntropy=new double[nx];
		double logd=10/(Math.log(10));
		//double maxC=dynRange-Math.log(maxPossAmp)*logd;

		//double minPower=Math.pow(10, -1);
		
		
		for (int i=0; i<nx; i++){
			double scoreArith=0;
			double scoreGeom=0;
			for (int j=0; j<ny; j++){
				double amp=spect[j][i];
				scoreArith+=Math.pow(10, (amp-1));
				scoreGeom+=amp;
					
			}
			scoreArith/=ny+0.0;
			scoreGeom=Math.pow(10, ((scoreGeom/(ny+0.0))-1));
			wienerEntropy[i]=-1*Math.log(scoreGeom/scoreArith);
		}
		return wienerEntropy;
	}
	
	
	
	
	
	/**
	 * this method takes the median regression through each point, along the x-axis of the spectrogram
	 * It could be used to assist with dereverberation, but it isn't at present
	 * @return a double[] indicating the degree of attentuation at each point after a signal
	 */
	public double[] sampleEchoDecays(Spectrogram sp){
		//
		
		int windowSize=(int)(0.5*echoRange/sp.dx);
		//int windowSize=50;
		if (windowSize>nx){windowSize=nx;}
		int medLoc=windowSize/2;
		double xsum=0;
		double x2sum=0;
		for (int i=0; i<windowSize; i++){
			xsum+=i;
			x2sum+=i*i;
		}
		
		double xsum2=(xsum*xsum)/(windowSize+0.0);
		xsum/=windowSize*(-1.0);
		double den=1/(x2sum-xsum2);
		double[] results=new double[ny];
		
		int xwidth=nx-windowSize;
		double[] buffer=new double[xwidth];
		for (int i=0; i<ny; i++){
			for (int j=0; j<xwidth; j++){
				double ysum=0;
				double xysum=0;
				for (int k=0; k<windowSize; k++){
					float p=spect[i][j+k];
					ysum+=p;
					xysum+=k*p;
				}
				buffer[j]=(xysum+(xsum*ysum))*den;	
			}
			Arrays.sort(buffer);
			results[i]=buffer[medLoc];
			//System.spect.println(results[i]);
		}
		return results;
	}	
	
	
	/**
	 * This method calculates the double[] phase object which is used in pitch calculation - both
	 * for pitch representation and for fundamental frequency estimation.
	 * This should only be called once upon initiation of a new spectrogram.
	 */
	public void makePhase(){
		
		double log2Adj=1/Math.log(2);
		double logMax=Math.log(ny-1)*log2Adj;
		double logMin=Math.log(minFreq)*log2Adj;
		double step=(logMax-logMin)/(ny+0.0);
		octstep=1/step;
		double[] logTransform=new double[ny];
		for (int i=0; i<ny; i++){
			logTransform[i]=Math.pow(2, logMin+(i*step));
		}

		phase=new double[ny*ny];

		int count=0;
		for (int j=0; j<ny; j++){			
						
			for (int i=0; i<ny; i++){
				
				double q=Math.cos(Math.PI*((i)/logTransform[j]));
				q=Math.pow(q, 2);
				if (i<logTransform[j]*0.5){q=0;}
				phase[count]=q;
	
				count++;
			}
		}
	}

	
	/**
	 * This method calculates the double[] phase object which is used in pitch calculation - both
	 * for pitch representation and for fundamental frequency estimation.
	 * This should only be called once upon initiation of a new spectrogram.
	 */
	public void makePhase2(){
		
		double log2Adj=1/Math.log(2);
		double logMax=Math.log(ny-1)*log2Adj;
		double logMin=Math.log(minFreq)*log2Adj;
		double step=(logMax-logMin)/(ny+0.0);
		octstep=1/step;
		double[] logTransform=new double[ny];
		for (int i=0; i<ny; i++){
			logTransform[i]=Math.pow(2, logMin+(i*step));
		}

		phase=new double[ny*ny];

		int count=0;
		for (int j=0; j<ny; j++){			
			double tot=0;
			for (int i=0; i<ny; i++){
				double g=i-logTransform[j]*Math.round(i/logTransform[j]);
				
				double h=Math.cos(Math.PI*g/logTransform[ny/2]);
				
				if (Math.abs(g)>logTransform[0]){h=0;}
				
				double q=Math.cos(Math.PI*((i)/logTransform[j]));
				q=Math.pow(h, 2);
				if (i<logTransform[j]*0.5){q=0;}
				phase[count]=q;
				tot+=q;
				//if (j==ny/2){
					//System.out.println(j+" "+i+" "+phase[count]);
				//}
				count++;
			}
			count-=ny;
			for (int i=0; i<ny; i++){
				phase[count]/=tot;
				count++;
			}
		}
	}
	
	
	
	
	
	
	/**
	 * This method calculates the pitch representation. Specifically, it calculates the
	 * number of available cores and creates instances of PitchCalculator to do the
	 * calculation
	 * @param unx the number of columns in the spectrogram. Not obvious why this needs to 
	 * be sent to the method.
	 * @return a float[][] pitch representation of the sound.
	 */
	public float[][] runPitchCalculator(int unx){
		int ncores=Runtime.getRuntime().availableProcessors();
		
		float[][] pout=new float[ny][unx];
		
		PitchCalculator ct[]=new PitchCalculator[ncores];
		
		
		int[] starts=new int[ncores];
		int[] stops=new int[ncores];
		for (int i=0; i<ncores; i++){
			starts[i]=i*(unx/ncores);
			stops[i]=(i+1)*(unx/ncores);
		}
		stops[ncores-1]=unx;
		//System.out.println("CORES USED: "+ncores);
		
		for (int i=0; i<ncores; i++){
			ct[i]=new PitchCalculator(starts[i], stops[i]);
			ct[i].setPriority(Thread.MIN_PRIORITY);
			ct[i].start();
		}
		
		try{
			for (int cores=0; cores<ncores; cores++){
				ct[cores].join();
			}
			for (int cores=0; cores<ncores; cores++){
				//System.out.println(cores);
				for (int i=ct[cores].start; i<ct[cores].end; i++){
					int ii=i-ct[cores].start;
					for (int j=0; j<ny; j++){
						pout[j][i]=ct[cores].pout[ii][j];
						/*
						int h=ny-j-1;
						int sh=ct[cores].out[ii][j];
	
						img.setRGB(i, h, colpal[sh]);
						*/
					}
				}
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
		/*
		if (dynEqual>0){
			dynamicEqualizer(pout);
		}
		System.out.println("Dyn Range");
		dynamicRange(pout, maxAmp);

	
		System.out.println("Equalize");
		equalize(pout);	
		System.out.println("FInished");
		*/
		
		float max=0;
		for (int i=0; i<ny; i++){
			for (int j=0; j<unx; j++){
				
				//System.out.println(i+" "+j+" "+pout[i][j]);
				
				if (pout[i][j]>max){
					max=pout[i][j];
				}
			}
		}
		for (int i=0; i<ny; i++){
			for (int j=0; j<unx; j++){
				//if (pout[i][j]>max){
					pout[i][j]/=max;
				//}
			}
		}
		
		return pout;
	}
	
	

	/**
	 * This internal class calculates the 'pitch' representation of sounds. By making
	 * it an internal class, it makes it possible to use multiple threads/cores
	 * @author Rob
	 *
	 */
	public class PitchCalculator extends Thread{
		
		int start, end;
		float[][] pout;
		double subsup=0.25;
		
		public PitchCalculator(int start, int end){
			this.start=start;
			this.end=end;	
			pout=new float[end-start][ny];
		}

		public void run(){
			double adjust=0;
			int oct=(int)Math.round(octstep);
			double[] spectrum=new double[ny];
			double[] hscor=new double[ny];
			double[] iscor=new double[ny];
			double maxHscor;
			double subSuppression=fundAdjust;
			for (int i=start; i<end; i++){
				double peakAmp=0;
				double sumAmp=0;
				for (int j=0; j<ny; j++){
					spectrum[j]=spect[j][i]+adjust;
					if (spectrum[j]<0){
						spectrum[j]=0;
					}
				
					hscor[j]=0;
					if (spectrum[j]>peakAmp){
						peakAmp=spectrum[j];
					}
					sumAmp+=spectrum[j];
					//spectrum[j]=Math.pow(spectrum[j], subSuppression);
				}

				int count=0;
				double sumxy=0;
				double sumx=0;
				double p;
				for (int j=0; j<ny; j++){
					sumxy=0;
					sumx=0;
					for (int k=0; k<ny; k++){
						p=phase[count];
						sumxy+=p*p*spectrum[k];
						sumx+=p*p;
						count++;
					}
					hscor[j]=sumxy/sumx;
					
					//System.out.println(j+" "+spectrum[j]+" "+hscor[j]);
					
				}
				maxHscor=-1000000;
				/*
				for (int j=0; j<ny-oct; j++){
					hscor[j]-=subsup*hscor[j+oct];
					if (hscor[j]<0.0001){hscor[j]=0.0001;}
				}
				for (int j=ny-oct; j<ny; j++){
					hscor[j]-=subsup*hscor[ny-1];
				}
				
				
				for (int j=0; j<ny; j++){
					hscor[j]=Math.pow(hscor[j], 2);
					if (hscor[j]>maxHscor){
						maxHscor=hscor[j];
					}
				}
				for (int j=0; j<ny; j++){
					hscor[j]=(hscor[j]/maxHscor);
				}
			
				if (maxHscor>0){
					for (int j=minFreq; j<ny; j++){
						
						hscor[j]*=peakAmp/(dynRange);					
					}
				}
				else{
					for (int j=0; j<ny; j++){
						hscor[j]=0;
					}
				}
				int ii=i-start;
				for (int j=minFreq; j<ny; j++){
					pout[ii][j]=(float)hscor[j];
				}
				*/
				/*
				for (int j=minFreq; j<ny; j++){
					double p=1-hscor[j];
					int q=(int)(p*255);
					if (q>255){q=255;}
					if (q<0){q=0;}
					out[ii][j]=q;
				}
				*/
				
				for (int j=0; j<ny; j++){
					
					double p1=hscor[j];
					double p2=p1;
					if (j>=oct){p2=hscor[j-oct];}
					double p3=p1;
					if (j<ny-oct){p3=hscor[j+oct];}
					
					iscor[j]=p1-p2;
					
					if (iscor[j]<0.0001){iscor[j]=0.0001;}
				}
				
				for (int j=0; j<ny; j++){
					
					double p1=iscor[j];
					double p2=p1;
					if (j>=oct){p2=iscor[j-oct];}
					double p3=p1;
					if (j<ny-oct){p3=iscor[j+oct];}
					
					hscor[j]=p1-p3;
					
					if (hscor[j]<0.0001){hscor[j]=0.0001;}
				}
				
				for (int j=0; j<ny; j++){
					//hscor[j]=Math.pow(hscor[j], 2);
					//hscor[j]=iscor[j];
					if (hscor[j]>maxHscor){
						maxHscor=hscor[j];
					}
				}
				double atot=0;
				for (int j=0; j<ny; j++){
					//hscor[j]=(hscor[j]/maxHscor);
					if (hscor[j]>0){
						atot+=hscor[j];
					}
					
				}
				//System.out.println(sumAmp+" "+atot+" "+peakAmp+" "+atot);
				if (maxHscor>0){
					for (int j=minFreq; j<ny; j++){
						
						hscor[j]*=(peakAmp-adjust)/maxHscor;	
						//hscor[j]*=sumAmp/atot;
					}
				}
				else{
					for (int j=0; j<ny; j++){
						hscor[j]=0;
					}
				}
				
				int ii=i-start;
				for (int j=minFreq; j<ny; j++){
					//if (iscor[j]>0){
					//pout[ii][j]=(float)Math.log(iscor[j]);
					
					//if (pout[ii][j]>maxAmp){
						//maxAmp=pout[ii][j];
					//}
					//}
					pout[ii][j]=(float)hscor[j];
				}
				
				
				
			}
		}
	}
	
}
