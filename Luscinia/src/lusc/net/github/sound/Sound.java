//
//  Sound.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/10/2005.
//  Copyright 2022 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.


package lusc.net.github.sound;

import javax.sound.sampled.*;

import uk.me.berndporr.iirj.Butterworth;
import uk.me.berndporr.iirj.ChebyshevII;

import java.io.*;
import java.util.*;

/**
 * Sound is the class for sound objects. It contains both acoustic recording data
 *
 * It is one of the most central classes in Luscinia, and one of the largest. 
 * @author Rob
 *
 */

public class Sound {
	
	Song song;
	Spectrogram spectrogram;
	
	public float[][] envelope;
	
	public int [][]micOrderOptions= {{0,1,2,3},{0,1,3,2},{1,0,2,3},{1,0,3,2}};
	public int micOrderChoice=0;
	public double beamFormingDelay=0;
	public double[] beamFormingDelays=new double[3];
	public int bfmaxfreq=6000;
	public int bfminfreq=2000;
	
	public int stereomode=6;
	public int stereomodeGP=0;
	
	//if stereo=2, stereomode=0 means l; 1 means right; 2 means combine
	//if stereo=4, 0 means t1, 1 means t2, 2 means t3, 3 means t4, 4 means t1 & t2, 5 means t3&t4, 6 means all four
	
	int maxDelay=30;
	int expansionFactor=10;
	
	double sampleRate=0;
	int stereo=2;
	int frameSize=2;
	boolean bigEnd=false;
	boolean signed=false;
	int ssizeInBits=0;
	int overallSize=0;
	int overallLength=0;

	byte[] rawData, stereoRawData;
	float[] data;
	
	SourceDataLine  line = null;
	AudioFormat af;

	
	int playbackDivider=1;
	
	public double frequencyCutOff=1000;
	
	public Sound(Song song){
		this.song=song;
		spectrogram=new Spectrogram(this);
	}
	
	public Sound (File f, Song song){
		this.song=song;
		spectrogram=new Spectrogram(this);
		
		try {
			AudioInputStream AFStreamA = AudioSystem.getAudioInputStream(f);
			AudioFormat afFormat = AFStreamA.getFormat();
			if(afFormat.isBigEndian()){
				AudioFormat targetFormat = new AudioFormat(afFormat.getEncoding(), afFormat.getSampleRate(), afFormat.getSampleSizeInBits(), afFormat.getChannels(),
						afFormat.getFrameSize(), afFormat.getFrameRate(), false);
				afFormat=targetFormat;
			}
			if (afFormat.getEncoding().toString().startsWith("MPEG")){
				AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, afFormat.getSampleRate(), 16, afFormat.getChannels(),
						afFormat.getChannels()*2, afFormat.getSampleRate(), false);
					afFormat=targetFormat;
			}
	
			AudioInputStream AFStream=AudioSystem.getAudioInputStream(afFormat, AFStreamA);
			
			sampleRate=AFStream.getFormat().getSampleRate();
			stereo=AFStream.getFormat().getChannels();
						
			frameSize=AFStream.getFormat().getFrameSize();

			long length=(long)(AFStream.getFrameLength()*frameSize);
			bigEnd=AFStream.getFormat().isBigEndian();
			AudioFormat.Encoding afe=AFStream.getFormat().getEncoding();
			signed=false;
			if (afe.toString().startsWith("PCM_SIGNED")){signed=true;}
			ssizeInBits=AFStream.getFormat().getSampleSizeInBits();
			
			
			int xl=(int)Math.round(1000*AFStream.getFrameLength()/(sampleRate));
						
			if (length>0){
				rawData=new byte[(int)length];
				AFStream.read(rawData);
			}
			else{
				LinkedList<byte[]> bl=new LinkedList<byte[]>();
				byte[] temp=new byte[frameSize];
				while (AFStream.read(temp)>0){
					bl.add(temp);
				}
				length=bl.size()*frameSize;
				rawData=new byte[(int)length];
				int x=0;
				for (byte[] t : bl){
					System.arraycopy(t, 0, rawData, x, frameSize);
					x+=frameSize;
				}
			}
			
		} 
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	
	public void setUp() {
		overallSize=(rawData.length/(frameSize));
		overallLength=(int)Math.round(overallSize/(sampleRate*0.001));
	}

	/**
	 * gets the Frame size of the spectrogram - an int value of the length of the FFT buffer
	 * @return an int value (number of samples) for frame size
	 */
	public int getFrameSize(){
		return frameSize;
	}
	
	/**
	 * Sets the Frame size of the spectrogram - an int value of the length of the FFT buffer
	 * @param a int value (number of samples) for frame size
	 */
	public void setFrameSize(int a){
		frameSize=a;
	}

	/**
	 * Gets the number of channels
	 * @return value of stereo
	 */
	public int getStereo(){
		return stereo;
	}
	
	/**
	 * Sets the number of channels (max 2)
	 * @param a int value for stereo
	 */
	public void setStereo(int a){
		stereo=a;
	}

	/**
	 * gets whether or not the audio format is big-ended
	 * @return value of bigEnd
	 */
	public boolean getBigEnd(){
		return bigEnd;
	}

	/**
	 * sets whether or not the audio format is big-ended
	 * @param a value for bigEnd
	 */
	public void setBigEnd(boolean a){
		bigEnd=a;
	}

	/**
	 * gets the boolean value for whether the audio format is signed
	 * @return value of signed
	 */
	public boolean getSigned(){
		return signed;
	}

	/**
	 * sets the boolean value for whether the audio format is signed
	 * @param a value for signed
	 */
	public void setSigned(boolean a){
		signed=a;
	}

	/**
	 * gets the sample rate in Hz
	 * @return value of sampleRate
	 */
	public double getSampleRate(){
		return sampleRate;
	}
	
	/**
	 * sets the sample rate in Hz
	 * @param a value for sampleRate
	 */
	public void setSampleRate(double a){
		sampleRate=a;
	}

	/**
	 * gets the sample size in bits of the sound format
	 * @return value of ssizeInBits
	 */
	public int getSizeInBits(){
		return ssizeInBits;
	}

	/**
	 * sets the sample size in bits of the sound format
	 * @param a value for ssizeInBits
	 */
	public void setSizeInBits(int a){
		ssizeInBits=a;
	}

	/**
	 * gets the length of the raw byte array data
	 * @return length of the rawData array
	 */
	public int getRDLength(){
		return rawData.length;
	}

	
	/**
	 * gets the raw data - byte array of audio data
	 * @return byte array of audio data
	 */
	public byte[] getRawData(){
		return rawData;
	}
	
	/**
	 * sets the raw data - byte array of audio data
	 * @param a byte array of audio data
	 */
	public void setRawData(byte[] a){
		rawData=a;
	}

	public int getStereoMode() {
		return stereomode;
	}
	
	public void setStereoMode(int a) {
		stereomode=a;
	}
	
	public void setStereoModeGP(int a) {
		stereomodeGP=a;
	}
	
	public void setMicArrayMode(int a) {
		micOrderChoice=a;
	}
	
	
	
	/**
	 * sets the overall size of the sound, in terms of the number of samples
	 */
	public void setOverallSize(){
		if (frameSize==0){frameSize++;}
		overallSize=rawData.length/frameSize;
	}

	/**
	 * gets the overall size of the sound - the number of samples
	 * @return an int value of the number of samples in the sound
	 */
	public int getOverallSize(){
		return overallSize;
	}

	/**
	 * This gets the playbackDivider parameter, which determines the speed of playback. Should
	 * be 1,2,4 or 8
	 * @return an int showing how much to slow down playback
	 */
	public int getPlaybackDivider(){
		return playbackDivider;
	}
	
	/**
	 * 
	 * @param a
	 */
	public void setPlaybackDivider(int a){
		playbackDivider=a;
	}
	
	
	/**
	 * gets the length of the raw (byte) audio data
	 * @return an int with the length of the byte array
	 */
	public int getRawDataLength(){
		return rawData.length;
	}
	

	/**
	 * Helps clear up a Song object when closed. Not sure whether this helps garbage collection 
	 * or not...
	 */
	public void clearUp(){
		rawData=null;
		data=null;
		
		shutDownPlayback();
		
	}
	
	
	public Sound getSubSound(Song song, int x, int y) {
		int a=0;
		int b=0;
	
		if (ssizeInBits<=24){
			int p1=x;
			if (p1<0){p1=0;}
			int p2=y;
			//if (p2>=overallLength){
			//	p2=overallLength-1;
			//}
			double q=sampleRate*0.001;
			//System.out.println(q+" "+frameSize);
			a=(int)Math.round(p1*q)*frameSize;
			b=(int)Math.round(p2*q)*frameSize;
			//System.out.println("NEWSONGBOUNDS: "+a+" "+b+" "+sampleRate+" "+stereo+" "+p1+" "+p2+" "+syll[0]+" "+syll[1]);
		}
	
		byte[] sub=new byte[b-a];
		System.arraycopy(rawData, a, sub, 0, b-a);
		Sound s=new Sound(song);
		s.ssizeInBits=this.ssizeInBits;
		s.signed=this.signed;
		s.bigEnd=this.bigEnd;
		s.stereo=this.stereo;
		s.sampleRate=this.sampleRate;
		s.frameSize=this.frameSize;
		
		return s;
		
	}
	
	
	
	
	private byte[] makeByteArray(float[] x, int start, int end) {
		
		int n1=end-start;
		int n2=n1*2; //In this case frameSize must be 2 for 16bit and mono
		
		byte[] audio=new byte[n2];
		
		for (int i=0; i<n1; i++) {
			short p=(short)(x[i+start]*Short.MAX_VALUE);
			audio[i*2] = (byte) p;
			audio[i*2+1] = (byte)(p >> 8 & 0xFF);
		}
		return audio;
	}
	
	private byte[] makeStereoByteArray(float[] x, int start, int end) {
		
		int n1=end-start;
		int n2=n1*4;		//In this case frameSize must be 2 for 16bit and stereo
		
		byte[] audio=new byte[n2];
		
		for (int i=0; i<n1; i++) {
			short p=(short)(x[i+start]*Short.MAX_VALUE);
			
			audio[i*4] = (byte) p;
			audio[i*4+1] = (byte)(p >> 8 & 0xFF);
			audio[i*4+2] = audio[i*4];
			audio[i*4+3] = audio[i*4+1];
			
		}
		
		return audio;
	}
	
	/**
	 * This is an effort to take binary data and transform it into a float[] representation of the sound
	 * @param start beginning point in the sound
	 * @param end end point in the sound.
	 */
	
	private float[] parseSound(byte[] rawData, int start, int end){
		System.out.println("Parse Sound started");
		
		int frameSizeC=frameSize/stereo;
		
		System.out.println(frameSizeC+" "+frameSize+" "+stereo);
		
		float[][]look=new float[256][frameSizeC];
		float divider=(float)(0.5*Math.pow(256, frameSizeC));
		
		float[]mult=new float[frameSizeC];
		for (int i=0; i<frameSizeC; i++){mult[i]=(float)Math.pow(256, i);}
		int k=0;
		for (int i=0; i<frameSizeC; i++){
			for (int j=0; j<256; j++){
				k=j;
				if ((frameSizeC>1)&&(i==frameSizeC-1)&&(j>127)){k-=256;}
				look[j][i]=k*mult[i];
			}
		}
		int numb=frameSizeC-1;
		int size=end-start;
		//data=null;
		float[] data=new float[size];
		end*=frameSizeC;
		start*=frameSizeC;
		int i=start;
		int n=0;
		int p;
		while (i<end){
			for (int j=0; j<numb; j++){
				p=rawData[i];
				if (p<0){p+=256;}
				data[n]+=look[p][j];
				i++;
			}
			
			p=rawData[i];
			if (p<0){p+=256;}
			data[n]+=look[p][numb];
			data[n]/=divider;
			//System.out.println(data[n]);
			i++;
			n++;
			
			
		}
		look=null;
		return data;
	}
	
	public float[] filter(float[]x, double sr, double fco, boolean highPass) {
		Butterworth bwfilter=new Butterworth();
		if (highPass) {
			bwfilter.highPass(6, sr, fco);
		}
		else {
			bwfilter.lowPass(6, sr, fco);
		}
		//bwfilter.highPass(6, sampleRate, frequencyCutOff);
		
		int size=x.length;
		float[] data2=new float[size];
		if (fco>0){
			for (int i=0; i<size; i++) {
				data2[i]=(float)bwfilter.filter(x[i]);
			}
			//data=data2;
		}
		else {
			for (int i=0; i<size; i++){
				data2[i]=x[i];
			}
			//data=data1;
		}
		return data2;
	}
	
	private float[] filterCh(float[] x, double sr, double fco, boolean highPass) {
		ChebyshevII chfilter=new ChebyshevII();
		if (highPass) {
			chfilter.highPass(2, sr, fco, 1);
		}
		else {
			chfilter.lowPass(2, sr, fco, 1);
		}
		
		
		int size=x.length;
		float[] data2=new float[size];
		if (fco>0){
			for (int i=0; i<size; i++) {
				data2[i]=(float)chfilter.filter(x[i]);
			}
		}
		else {
			for (int i=0; i<size; i++){
				data2[i]=x[i];
			}

		}	
		return data2;
	}
	
	
	float[] averageScore(float[] x, float[] y) {
		int n=x.length;
		float[] out=new float[n];
		for (int i=0; i<n; i++) {
			out[i]=(x[i]+y[1])*0.5f;
		}
		
		return out;
	}
	

	
	float[] normalise(float[] d, float targetMax) {
		int n=d.length;
		double max=0;
		for (int i=0; i<n; i++) {
			double x=Math.abs(d[i]);
			if (x>max) {max=x;}
		}
		
		float mult=(float)(targetMax/max);
		float[] out=new float[n];
		for (int i=0; i<n; i++) {
			out[i]=d[i]*mult;
		}
		
		return out;
	}
	
	
	float[] expand(float[] d, int st, int f) {
		
		int n=d.length;
		int m=n*f;
		float[] out=new float[m];
		
		
		double[][]fb=new double[f][];
		for (int i=0; i<f; i++) {
			fb[i]=fractional(i/(f+0.0));
		}
		int q=n/st;
		
		for (int i=0; i<q; i++) {
			for (int b=0; b<st; b++) {
				for (int j=0; j<f; j++) {
					if (j==0) {
						out[i*st*f+b]=d[i*st+b];
					}
					else {
						double x=0;
						for (int k=0; k<11; k++) {
							int p=i*st+b+(k-5)*st;
							if ((p>=0)&&(p<n)) {
								x+=fb[j][k]*d[p];
							}
						}
						out[i*st*f+b+j*st]=(float)x;
					}
				}
			}
		}
		
		return out;
	}
	
	float[] contract(float[] d, int st, int f) {
		int n=d.length;
		int m=n/f;
		int p=m/st;
		
		float[] out=new float[m];
		int a=0;
		for (int i=0; i<p; i++) {
			int b=i*f*st;
			for (int j=0; j<st; j++) {
				out[a]=d[b];
				a++;
				b++;
			}
			
		}
		
		return out;
		
	}
	
	
	
	double[] fractional(double delay) {
		//double delay = 0.25;               // Fractional delay amount
		int filterLength = 11;             // Number of FIR filter taps (should be odd)
		int centreTap = filterLength / 2;  // Position of centre FIR tap
		double[] tapWeight=new double[filterLength];
		for (int t=0 ; t<filterLength ; t++){
			// Calculated shifted x position
			double x = t - delay;

			// Calculate sinc function value
			double sinc = Math.sin(Math.PI * (x-centreTap)) / (Math.PI * (x-centreTap));

			// Calculate (Hamming) windowing function value
			double window = 0.54 - 0.46 * Math.cos(2.0 * Math.PI * (x+0.5) / filterLength);

			// Calculate tap weight
			tapWeight[t] = window * sinc;
		}
		return tapWeight;  
	}
	
	float[] extractOffset(float[] d, int ch, int st) {
		float[] out=extractChannel(d,ch,st);
		return removeOffset(out);
	}
	
	float[] extractChannel(float[] d, int ch, int st) {
		int n=d.length/st;
		
		float[] out=new float[n];
		
		for (int i=0; i<n; i++) {
			out[i]=d[i*st+ch];
		}
		
		return out;
		
	}
	
	float[] removeOffset(float[] d) {
		int n=d.length;
		double tot=0;
		for (int i=0; i<n; i++) {
			tot+=d[i];
		}
		float x=(float)(tot/(n+0.0));
		//System.out.println("OFFSET: "+x);
		float[] out=new float[n];
		for (int i=0; i<n; i++) {
			out[i]=d[i]-x;
			
		}
		
		return out;
	}
	
	
	//Sound is written into Class array data...
	
	void makeSoundArray(int a, int b, boolean guideSpect) {
		
		float[] dd=parseSound(rawData, a*stereo, b*stereo);
		
		if (stereo==1) {
			data=filter(dd, sampleRate, frequencyCutOff, true);
		}
		if (guideSpect==true) {
			
			data=extractOffset(dd, stereomodeGP, stereo);
			data=filter(data, sampleRate, frequencyCutOff, true);
		}
		
		else {
			
			if (stereo==2) {
				if (stereomode==0){
					data=extractOffset(dd, 0, stereo);
					data=filter(data, sampleRate, frequencyCutOff, true);
				}
				
				else if (stereomode==1) {
					data=extractOffset(dd, 1, stereo);
					data=filter(data, sampleRate, frequencyCutOff, true);
				}
				
				else {
					MakePhaseSpect[]mps=new MakePhaseSpect[4];
					for (int k=0; k<2; k++) {
						mps[k]=new MakePhaseSpect(dd, k);
						mps[k].start();
					}
						
					try{
						for (int k=0; k<4; k++) {
							mps[k].join();
								
						}
					}
					catch (Exception g){
						g.printStackTrace();
					}
						
					float[][] phx=comparePhase(mps[0].spect.phase, mps[1].spect.phase);	
					double delay=estimateDelay(phx, mps[0].spect);
					
					beamFormingDelay=delay;
						
					int[] ch= {0,1};
					data=delayAndSum(dd, ch, stereo, delay);
					data=filter(data, sampleRate, frequencyCutOff, true);								
				}
				
				
				
				
			}
			else if (stereo==4) {
				
				int[] order=micOrderOptions[micOrderChoice];
				
				
				if (stereomode==0){
					data=extractOffset(dd, order[0], stereo);
					data=filter(data, sampleRate, frequencyCutOff, true);
				}
				
				else if (stereomode==1) {
					data=extractOffset(dd, order[1], stereo);
					data=filter(data, sampleRate, frequencyCutOff, true);
				}
				else if (stereomode==2){
					data=extractOffset(dd, order[2], stereo);
					data=filter(data, sampleRate, frequencyCutOff, true);
				}
				
				else if (stereomode==3) {
					data=extractOffset(dd, order[3], stereo);
					data=filter(data, sampleRate, frequencyCutOff, true);
				}
				else if (stereomode==4) {
						
					MakePhaseSpect[]mps=new MakePhaseSpect[4];
					for (int k=0; k<2; k++) {
						mps[k]=new MakePhaseSpect(dd, order[k]);
						mps[k].start();
					}
						
					try{
						for (int k=0; k<4; k++) {
							mps[k].join();
								
						}
					}
					catch (Exception g){
						g.printStackTrace();
					}
						
					float[][] phx=comparePhase(mps[0].spect.phase, mps[1].spect.phase);	
					double delay=estimateDelay(phx, mps[0].spect);
					
					beamFormingDelay=delay;
						
					int[] ch= {order[0],order[1]};
					data=delayAndSum(dd, ch, stereo, delay);
					data=filter(data, sampleRate, frequencyCutOff, true);
						
						
				}
				else if (stereomode==5) {
					MakePhaseSpect[]mps=new MakePhaseSpect[4];
					for (int k=0; k<2; k++) {
						mps[k]=new MakePhaseSpect(dd, order[k+2]);
						mps[k].start();
					}
						
					try{
						for (int k=0; k<4; k++) {
							mps[k].join();
								
						}
					}
					catch (Exception g){
						g.printStackTrace();
					}
						
					float[][] phx=comparePhase(mps[0].spect.phase, mps[1].spect.phase);	
					double delay=estimateDelay(phx, mps[0].spect);
					
					beamFormingDelay=delay;
						
					int[] ch= {order[2],order[3]};
					data=delayAndSum(dd, ch, stereo, delay);
					data=filter(data, sampleRate, frequencyCutOff, true);
				}
					
				else {
												
					MakePhaseSpect[]mps=new MakePhaseSpect[4];
						
					for (int k=0; k<4; k++) {
						mps[k]=new MakePhaseSpect(dd, order[k]);
						mps[k].start();
					}
						
					try{
						for (int k=0; k<4; k++) {
							mps[k].join();
								
						}
					}
					catch (Exception g){
						g.printStackTrace();
					}
						
					float[][] phx=comparePhase(mps[0].spect.phase, mps[1].spect.phase);
						
					beamFormingDelays[0]=estimateDelay(phx, mps[0].spect);					
						
					float[][] phy=comparePhase(mps[2].spect.phase, mps[3].spect.phase);
						
					beamFormingDelays[1]=estimateDelay(phy, mps[2].spect);
						
					phx=averagePhase(mps[0].spect.phase, mps[1].spect.phase);
					phy=averagePhase(mps[2].spect.phase, mps[3].spect.phase);
						
					float[][] phz=comparePhase(phx, phy);
						
					beamFormingDelays[2]=estimateDelay(phz, mps[0].spect);
					beamFormingDelays[2]=beamFormingDelays[0]+beamFormingDelays[1];
					mps=null;
						
					data=delayAndSumAll(dd, stereo, beamFormingDelays, order);
					data=filter(data, sampleRate, frequencyCutOff, true);
						
				}
			}
		}
	}
	
	public double estimateDelay(float[][]x, Spectrogram a) {
		float[][] y=a.spectrogram;
		int n=x.length;
		int m=x[0].length;
		double[] sc=new double[1000];
		int minFreq=(int)Math.round(bfminfreq/a.dy);
		int maxFreq=(int)Math.round(bfmaxfreq/a.dy);
		//System.out.println(minFreq+" "+maxFreq+" "+n+" "+dy);
		
		for (int i=0; i<m; i++) {
			
			int loc=-1;
			double max=-100000000;
			for (int j=minFreq; j<maxFreq; j++) {
				if (y[j][i]>max) {
					max=y[j][i];
					loc=j;
				}	
			}
			int xx=(int)Math.round((x[loc][i]*loc)/(Math.PI));	
			sc[xx+500]+=max;	
		}
		
		
		int loc=-1;
		double max=-100000000;
		
		for (int i=0; i<sc.length; i++) {
			if (sc[i]>max) {
				max=sc[i];
				loc=i;
			}
				
		}
		
		loc-=500;
		
		return loc/a.dy;
	}
	
	public class MakePhaseSpect extends Thread{
		
		public Spectrogram spect;
		float[] input=null;
		int order=0;
		
		public MakePhaseSpect(float[]input, int order) {
			this.input=input;
			this.order=order;
		}
		
		public synchronized void run(){
			
			float[] data=extractOffset(input, order, stereo);
			spect=new Spectrogram(Sound.this, song.spectrogram);
			data=filter(data, sampleRate, frequencyCutOff, true);
			spect.calculateFFT(data, true);			
		}
		
	}

	
	public float[] delayAndSumAll(float[] x, int stereo, double[] delays, int[] order) {
		
		float[]y=expand(x, stereo, expansionFactor);
		
		float[] a1=extractOffset(y, order[0], stereo);
		float[] b1=extractOffset(y, order[1], stereo);
		float[] a2=extractOffset(y, order[2], stereo);
		float[] b2=extractOffset(y, order[3], stereo);
		
		int n=a1.length;
		
		int d1=(int)Math.round(delays[0]*expansionFactor);
		int d2=(int)Math.round(delays[1]*expansionFactor);
		int d3=(int)Math.round(delays[2]*expansionFactor);
		
		float[]c1=new float[n];
		float[]c2=new float[n];
		float[] c=new float[n];
		for (int i=0; i<n; i++) {
			int f=i+d1;
			if (f<0) {f=0;}
			if (f>=n) {f=n-1;}
			
			int g=i+d2;
			if (g<0) {g=0;}
			if (g>=n) {g=n-1;}
			
			c1[i]=a1[i]+b1[f];
			c2[i]=a2[i]+b2[g];
			
			
		}
		
		
		
		for (int i=0; i<n; i++) {
			int f=i+d3;
			if (f<0) {f=0;}
			if (f>=n) {f=n-1;}
			
			c[i]=c2[i]+c1[f];
		}
		

		float[] out=contract(c, 1, expansionFactor);
		
		//System.out.println(out.length+" "+x.length+" "+a.length+" "+expansionFactor+" "+d);
		return out;
	}
	
	
	public float[] delayAndSum(float[] x, int[]ch, int stereo, double delay) {
		
		float[]y=expand(x, stereo, expansionFactor);
		
		float[] a=extractOffset(y, ch[0], stereo);
		float[] b=extractOffset(y, ch[1], stereo);
		int n=a.length;
		int d=(int)Math.round(delay*expansionFactor);
		
		float[]c=new float[n];
		
		for (int i=0; i<n; i++) {
			int f=i+d;
			if (f<0) {f=0;}
			if (f>=n) {f=n-1;}
			
			c[i]=a[i]+b[f];
		}

		float[] out=contract(c, 1, expansionFactor);
		
		//System.out.println(out.length+" "+x.length+" "+a.length+" "+expansionFactor+" "+d);
		return out;
	}
	
	
	
	public float[][] comparePhase(float[][] x, float[][]y){
		int n=x.length;
		int m=x[0].length;
		
		float[][] out=new float[n][m];
		for (int i=0; i<n; i++) {
			for (int j=0; j<m; j++) {
				double p=x[i][j]-y[i][j];
				
				if (p< -Math.PI) {
					p+= 2*Math.PI;
				}
				if (p> Math.PI) {
					p-= 2*Math.PI;
				}
				out[i][j]=(float)p;
			}
		}
		return out;
	}
	
	
	
	public float[][] averagePhase(float[][] x, float[][]y){
		int n=x.length;
		int m=x[0].length;
		
		float[][] out=new float[n][m];
		for (int i=0; i<n; i++) {
			for (int j=0; j<m; j++) {
				double p=(x[i][j]+y[i][j])*0.5;
				out[i][j]=(float)p;
			}
		}
		return out;
	}
	
	public float[][] recordPhase(float[][]x){
		int n=x.length;
		int m=x[0].length;
		
		float[][] out=new float[n][m];
		for (int i=0; i<n; i++) {
			for (int j=0; j<m; j++) {
				out[i][j]=x[i][j];
			}
		}
		return out;
	}
	

	
	
	
	
	/**
	 * This creates a new AudioFormat object based on details of the format of the stored
	 * sound
	 */
	public void makeAudioFormat(){
		af=new AudioFormat((float)sampleRate, ssizeInBits, stereo, signed, bigEnd);
	}	
	
	
	public void makeStereoDataFromRawData(int start, int end) {
		float[]x=parseSound(rawData, start, end);
		float[] y=null;
		if (stereo>1) {
			y=extractOffset(x, 0, stereo);
		}
		else {
			y=x;
		}
		y=normalise(y, 0.5f);
		y=expand(y,1, playbackDivider);
		stereoRawData=makeStereoByteArray(y, 0, y.length);
	}
	
	public void makeStereoDataFromProcessedData(int start, int end) {
		float[] y=new float[end-start];
		System.arraycopy(data, start, y, 0, y.length);
		y=normalise(y, 0.5f);
		y=expand(y,1, playbackDivider);
		stereoRawData=makeStereoByteArray(y, 0, y.length);
	}
	
		
		
	
	/**
	 * This sets up the sound system for playback
	 */
	public void setUpPlayback(){
		try{

			af=new AudioFormat((float)sampleRate, 16, 2, signed, bigEnd);
			
			DataLine.Info   info = new DataLine.Info(SourceDataLine.class, af);
			
			if (line!=null){line.close();}
			line = null;
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(af);
			
			System.out.println("READY TO PLAYBACK: "+line.isOpen());
			
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * This shuts down playback on quitting.
	 */
	public void shutDownPlayback(){
		try{
			if (line.isOpen()){
				line.flush();
				//line.stop();
				line.close();
			}
		}
		catch(Exception e){}
	}
	
	
	
	/**
	 * This method is called to arrange playback from a point in the song until the end
	 * @param x start point for playback
	 */
	
	public void prepPlaybackFrom(int x){
		int a=(int)(x*sampleRate*0.001*stereo);
		//int a=(int)(x*step*stereo);
		int b=stereo*rawData.length/frameSize;
		makeStereoDataFromRawData(a,b);
		
		System.out.println("PREPARTING TO PLAYBACK FROM: "+x+" "+a+" "+rawData.length);
		
		playSound();
	}
	
	
	public void prepPlaybackAll(){
		int a=0;
		int b=stereo*rawData.length/frameSize;
		makeStereoDataFromRawData(a,b);
		
		playSound();
		
	}
	
	/**
	 * This method is called to arrange which parts of the song need to be played back
	 * @param syll
	 * @return a double[] with the transformed start and stopped locations.
	 */
	public double[] prepPlayback(int[] syll){
		
		int p1=syll[0]-7-song.startTime;
		if (p1<0) {p1=0;}
		int p2=syll[1]+7;
		
		if (p2>=song.endTime) {
			p2=song.endTime-1;
		}
		p2-=song.startTime;
		
		int a=(int)(p1*0.001*sampleRate);
		int b=(int)(p2*0.001*sampleRate);
		
		//out.println("Prepared to play back syllable: "+syll[0]+" "+syll[1]+" "+a+" "+b+" "+data.length+" "+anx);
		
		makeStereoDataFromProcessedData(a,b);
		playSound();
		
		double d=1/(overallLength+0.0);
		p1+=song.startTime;
		p2+=song.startTime;
		
		double[] c={p1*d, p2*d};
		return c;
	}
	
	
	/**
	 * This method is called to arrange playback of a region of a song (normally the screen view)
	 * @param x start point for playback
	 * @param y end point for playback
	 */
	
	public void prepPlaybackScreen(int x, int y){

		makeStereoDataFromProcessedData(0,data.length);
		
		playSound();
	}
	
	
		
	
		
	/**
	 * This method organizes playback of the song. It creates a PlaySound instance to actually
	 * carry out the playbacl.
	 * @param a
	 * @param b
	 */
	public void playSound (){
		try{
			
			
			System.out.println("Playing sound");
			setUpPlayback();
			//if ((line==null)||(!line.isOpen())){}
			
			line.start();
			
			Thread play=new Thread(new PlaySound());
			play.setPriority(Thread.MIN_PRIORITY);
			play.start();
		} 
		catch (Exception e) {
			System.out.println(e);
			System.exit(0);
		}//
	}
	
	/**
	 * This internal class plays the sound file encoded by the Song.
	 * @author Rob
	 *
	 */
	class PlaySound extends Thread{
		
		
		public PlaySound(){

		}

		public void run(){
			try{
				line.write(stereoRawData, 0, stereoRawData.length);
			}
			catch (Exception e) {
				e.printStackTrace();
				System.out.println("OWWW: "+stereoRawData.length);
			}
		}
	}
	
	/**
	 * This is a method to stop playback of the sound 
	 */
	public void stopSound(){
		try{
			if (line.isOpen()){
				line.stop();
				line.flush();
				//line.close();
			}
		}
		catch (Exception e) {
			System.out.println("Stop playback error");
		}
	}
	
	

	
	/**
	 * This organizes the calculation of a wave form representation of the sound
	 * @param startTime
	 * @param endTime
	 * @param steps
	 */
	public void makeAmplitude(){
		int steps=spectrogram.nx;
			
		double r=(data.length-0.0)/(steps-0.0);
		
		envelope=new float[steps][2];
		for (int i=0; i<steps-1; i++){
				
			int a=(int)Math.round(r*i);
			int b=(int)Math.round(r*(i+1));
			if (b>=data.length){b=data.length-1;}
			float maxAmp=data[a];
			float minAmp=data[a];
				//System.out.println(data.length+" "+a+" "+b+" "+r+" "+i+" "+steps);
			for (int j=a; j<=b; j++){
				if(data[j]>maxAmp){
					maxAmp=data[j];
				}
				if (data[j]<minAmp){
					minAmp=data[j];
				}
			}
			envelope[i][0]=maxAmp;
			envelope[i][1]=minAmp;
		}
	}

	
}
