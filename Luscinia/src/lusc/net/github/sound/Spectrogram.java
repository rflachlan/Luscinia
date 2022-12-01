package lusc.net.github.sound;


//
//  Spectrogram.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/10/2005.
//  Copyright 2005-2022 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.


/**
 * Spectrogram is the object that calculates spectrograms and filters sounds
 * @author Rob
 *
 */
public class Spectrogram {
	boolean DIRECTION=true;
	double[] cosines, sines;
	int[] bitreverse;
	int nu;
	double radice;
	double[] xReal, xImag;
	
	double[] window=null;
	
	public float[][] spectrogram, phase, spect;
	
	public double dy, dx, timeStep, frameLength, overlap;	//dx is redundant! Same as timeStep!
	double sampleRate;
	//why is frameLength here?
	
	double step;
	public int frame, framePad, windowMethod, ny, nx, maxf;
	Sound sound;
	
	public float maxAmp, maxPossAmp;
	
	public Spectrogram() {}
	
	public Spectrogram(Sound sound){
		this.sound=sound;	
	}
	
	public Spectrogram(Sound sound, Spectrogram spect) {
		this.sound=sound;
		timeStep=spect.timeStep;
		frameLength=spect.frameLength;
		frame=spect.frame;
		windowMethod=spect.windowMethod;
		maxf=spect.maxf;
		
	}
	
	public void clearUp() {
		spectrogram=null;
		phase=null;
		spect=null;
		window=null;
		xReal=null;
		xImag=null;
		cosines=null;
		sines=null;
	}

	
	/**
	 * This method creates windowFunctions for the FFT
	 * @param N the index of the window
	 */
	private double[] makeWindow(int N, int windowMethod){			//produces various windowing functions
		double[] window=new double[N];
		if (windowMethod==2){
			//System.out.println("Hamming window "+N);
			for (int i=0; i<N; i++){
				window[i]=(float)(1-(0.5+0.5*Math.cos((2*Math.PI*i)/(N-1.0))));
				//System.out.println(window[i]);
			}
		}
		if (windowMethod==3){
			//System.out.println("Hanning window "+N);
			for (int i=0; i<N; i++){
				window[i]=(float)(1-(0.54+0.46*Math.cos((2*Math.PI*(i+1))/(N+1.0))));
				//System.out.println(window[i]);
			}
		}
		if (windowMethod==1){
			//int mid=frame/2;
			double sigma=0.4;
			double N12=0.5*(N-1);
			
			//System.out.println("Gaussian window "+N);
			double max=0;
			double min=1;
			for (int i=0; i<N; i++){
				double mq=(i-N12)/(sigma*N12);
				window[i]=(float)(Math.exp(-0.5*mq*mq));
				if (window[i]>max){max=window[i];}
				if (window[i]<min){min=window[i];}
			}
			for (int i=0; i<N; i++){
				window[i]=(window[i]-min)/(max-min);
				//System.out.println(window[i]);
			}
				//window[i]=(float)(Math.exp((-1*(Math.PI*(i-mid))*(Math.PI*(i-mid)))/(N*N+0.0)));}
		}
		if (windowMethod==4){
			
			double sigma=0.14*N;
			//System.out.println("confined Gaussian window");
			for (int i=0; i<N; i++){
				
				double Gn=calcG(sigma, i, N);
				double Gh=calcG(sigma, -0.5, N);
				double GnN=calcG(sigma, i+N, N);
				double GnmN=calcG(sigma, i-N, N);
				double Gnmh=calcG(sigma, N-0.5, N);
				double Gnmh2=calcG(sigma, -0.5-N, N);
				
				window[i]=(Gn-((Gh*(GnN+GnmN))/(Gnmh+Gnmh2)));
			}	
		}
		
		return window;
	}
	
	public double calcG(double sigma, double x, double N){
		double N2=0.5*(N-1);
		double p=(x-N2)/(2*sigma);
		double G=Math.exp(-1*p*p);
		
		return G;
	}

	
	/**
	 * This is the FFT method
	 * @param L indicates where in the signal to start from
	 */
	
	
	//CHANGES: I've removed maxAmp calculation
	//I've removed copying from out1 to out.
	
	
	public void calculateFFT(float[] data, boolean recordPhase){
		
		
		int nx=(int)Math.ceil((data.length/step)-1);
		
		
		spectrogram=new float[ny][nx];
		if (recordPhase) {phase=new float[ny][nx];}
		
		int place=0;
		
		if (window==null) {
			window=makeWindow(framePad, windowMethod);
		}
		
		int L2=data.length;
		xReal=new double[framePad];
		xImag=new double[framePad];

		int istart=0;
		double start=0;
		int i=0;
		int py=ny;
		if (py>framePad){py=framePad;}				//py is calculated in case the user picks a max freq that is above the resolving power of the other spectrogram
		float tempr, tempi;							//settings. If that happens, the spectrogram only plots up to the max. resolving power.
		int a,b,c,d;
		
		boolean reachedEnd=false;
		
		//System.out.println(framePad+" "+data.length+" "+window.length);
		
		while(!reachedEnd){

			for (i=0; i<frame; i++){
				d=i+istart;
				if (d<L2){
					xReal[i]=window[i]*data[d];				//This is where the window is applied
				}
				else{
					xReal[i]=0;
					reachedEnd=true;
				}
				xImag[i]=0;
			}
			for (i=frame; i<framePad; i++) {
				xReal[i]=0;
				xImag[i]=0;
			}
						
			fft();
			maxAmp=0f;
			for (i=0; i<py; i++){
				if (place<spectrogram[i].length){
					spectrogram[i][place]=(float)(radice*(xReal[i]*xReal[i]+xImag[i]*xImag[i]));						//This is the production of the power spectrum
					if (spectrogram[i][place]>maxAmp) {maxAmp=spectrogram[i][place];}
					if (recordPhase) {phase[i][place]=(float)(Math.atan2(xReal[i], xImag[i]));}
				}
			}
			start+=step;
			istart=(int)Math.round(start);
			place++;
		}	
		//System.out.println("SPECTROGRAM MADE: "+place+" "+py+" "+nx+" "+ny+" "+framePad+" "+frame);
	}
	
	
	/**
	 * The Fast Fourier Transform (generic version, with some optimizations).
	 *
	 * @param inputReal
	 *            an array of length n, the real part
	 * @param inputImag
	 *            an array of length n, the imaginary part
	 * @param DIRECT
	 *            TRUE = direct transform, FALSE = inverse transform
	 * @return a new array of length 2n
	 */
	private void fft() {
		// - n is the dimension of the problem
		// - nu is its logarithm in base e
		//int framePad = inputReal.length;
		
		int n2 = framePad / 2;
		int nu1 = nu - 1;
		double tReal, tImag, c, s;
		// First phase - calculation
		int k = 0;
		int kk=0;
		for (int l = 1; l <= nu; l++) {
		    while (k < framePad) {
			for (int i = 1; i <= n2; i++) {

			    kk=k>>nu1;
			    c=cosines[kk];
			    s=sines[kk];
			    
			    tReal = xReal[k + n2] * c + xImag[k + n2] * s;
			    tImag = xImag[k + n2] * c - xReal[k + n2] * s;
			    xReal[k + n2] = xReal[k] - tReal;
			    xImag[k + n2] = xImag[k] - tImag;
			    xReal[k] += tReal;
			    xImag[k] += tImag;
			    k++;
			}
			k += n2;
		    }
		    k = 0;
		    nu1--;
		    n2 /= 2;
		}
		// Second phase - recombination
		k = 0;
		int r;
		while (k < framePad) {
		    //r = bitreverseReference(k, nu);
		    r=bitreverse[k];
		    if (r > k) {
			tReal = xReal[k];
			tImag = xImag[k];
			xReal[k] = xReal[r];
			xImag[k] = xImag[r];
			xReal[r] = tReal;
			xImag[r] = tImag;
		    }
		    k++;
		}

	}


	private void makeBitReverseRef(int n, int nu, double constant){
		cosines=new double[n];
		sines=new double[n];
		bitreverse=new int[n];
	
		for (int a=0; a<n; a++){
			int j2;
			int j1 = a;
			int k = 0;
			for (int i = 1; i <= nu; i++) {
				j2 = j1 / 2;
				k = 2 * k + j1 - 2 * j2;
				j1 = j2;
			}
		
			bitreverse[a]=k;
		
			double arg = constant * k / n;
			cosines[a] = Math.cos(arg);
			sines[a] = Math.sin(arg);
		
		}
	}
	
	
	/**
	 * First of several preparatory steps for making an FFT. This calculates basic parameters
	 * of the spectrogram and calculates various look up tables etc.
	 */
	public void setFFTParameters(boolean DIRECTION, double sampleRate){
		
		this.DIRECTION=DIRECTION;
		this.sampleRate=sampleRate;
		step=timeStep*sampleRate*0.001;		
		frame=(int)Math.round(frameLength*sampleRate*0.001);	//frame is the frame-size of the spectrogram expressed in samples
		
		overlap=1-(timeStep/frameLength);
		
		double ld = Math.ceil(Math.log(frame) / Math.log(2.0));
		framePad=(int)Math.pow(2, ld+2);
		while (framePad<frame) {
			ld++;
			framePad=(int)Math.pow(2, ld+2);
		}
		//radice = 1 / Math.sqrt(n);		//n is framePad!!!
		radice = 1 / (framePad+0.0);
		
		
		//System.out.println(frame+" "+ld+" "+frameLength+" "+framePad);
		
		nu = (int) ld+2;	
		
		makeWindow(frame, windowMethod);
		
		dy=sampleRate/(framePad+0.0);
		dx=timeStep;		//THIS IS REDUNDANT!
		
		ny=(int)Math.floor(maxf/dy);
		
		//Adjust maxf if it lies outside possible values for the sound file.
		if (ny>framePad){
			ny=framePad;
			maxf=(int)Math.floor(ny*dy);
		}
		
		//double start=0;
		//nx=0;
		//while (start+frame<sound.overallSize){start+=step; nx++;}
		
		double dconstant;
		if (DIRECTION)
		    dconstant = -2 * Math.PI;
		else
		    dconstant = 2 * Math.PI;
		makeBitReverseRef(framePad, nu, dconstant);	
	}
	
	/**
	 * Makes a tonal signal of maximum amplitude to set a reference amplitude value in
	 * the spectrogram
	 */
	void calculateSampleFFT(){		
		float[] dat=new float[2*framePad];
		
		int i=0;

		
		int N=framePad;
		double adj=sampleRate*2/(maxf);
		
		for (i=0; i<dat.length; i++){
			dat[i]=(float)(window[i]*Math.sin(i/adj));				//This is where the fake signal is made
		}

		calculateFFT(dat, false);
		
		maxPossAmp=0;
		for (i=0; i<spectrogram.length; i++){
			for (int j=0; j<spectrogram[0].length; j++) {
				if (spectrogram[i][j]>maxPossAmp) {maxPossAmp=spectrogram[i][j];}
			}
		}	
		dat=null;

	}
	
}
