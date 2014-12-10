package lusc.net.github;
//
//  Song.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/10/2005.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.


//import java.awt.image.BufferedImage;
import java.sql.Blob;
import java.util.LinkedList;
import java.util.Arrays;

import javax.sound.sampled.*;


public class Song {
	
	boolean loaded=false;
	boolean running=false;
	boolean setRangeToMax=true;
	boolean relativeAmp=true;
	float data[];
	float maxAmp, maxAmp2;
	
	double [] phase;
	int minFreq=5;
	double octstep=10;
	
	private static final int EXTERNAL_BUFFER_SIZE = 128000;
	
	float [] trig1, trig2;
	int [] places1,  places2,  places3,  places4;
	int bitTab[][];
	float window[];
	double[] filtl, filtu;
	int counter=0;
	int counter2=0;
	int rangef=0;
	int overallSize=0;
	int overallLength=0;
	byte []rawData, rawData2, stereoRawData;
	float [][] out, out1, envelope;
	int startTime=0;
	int endTime=0;
	
	int dynEqual=0;
	double dynRange=40;
	float dynMax=10;
	int echoRange=50;
	//int distinctivenessRange=100;
	double echoComp=0.4f;
	
	int noiseLength1=200;
	int noiseLength2=15;
	float noiseRemoval=0f;
	
	
	float maxDB=0;
	float maxPossAmp=0;
	
	int maxf=0;
	double frameLength=1;
	double overlap=0;
	double timeStep=0.5;
	double frequencyCutOff=150;
	int windowMethod=1;
	double dx, dy, step;
	int nx, ny, anx, frame, framePad, place;
	int individualID=0;
	int songID=0;
	long tDate=0;
	String notes=" ";
	String location=" ";
	String recordEquipment=" ";
	String recordist=" ";
	
	double fundAdjust=1;
	double fundJumpSuppression=100;
	double minGap=0;
	double minLength=5;
	double maxTrillWavelength=20;
	
	int brushSize=5;
	int brushType=1;
	int maxBrush=10;
	int minBrush=0;
	
	int playbackDivider=1;
	
	double upperLoop=15;
	double lowerLoop=0;
	
	//Sound File parameters
	boolean bigEnd=true;
	boolean signed=true;
	int stereo=1;
	int ssizeInBits=0;
	double sampleRate, sampRate;
	int frameSize=0;
	
	String name=" ";
	String individualName=" ";
	String population=" ";
	String species=" ";
	String locationX=" ";
	String locationY=" ";
	String sx, sy;
	//Analysis stored data
	LinkedList<Element> eleList;
	LinkedList<int[]> syllList;
	LinkedList<int[][]> phrases;
	boolean[] phraseId;
	SourceDataLine  line = null;
	AudioFormat af;
	SpectrogramMeasurement sm;
	
	public SpectrogramMeasurement getMeasurer(){
		sm=new SpectrogramMeasurement(this);
		
		return sm;
	}
	
	public double getTimeStep(){
		return timeStep;
	}
	
	public double getFrameLength(){
		return frameLength;
	}
	
	public double getEchoComp(){
		return echoComp;
	}
	
	public double getSampRate(){
		return sampRate;
	}
	
	public double getDynRange(){
		return dynRange;
	}
	
	public double getDx(){
		return dx;
	}
	
	public double getDy(){
		return dy;
	}
	
	public double getFundAdjust(){
		return fundAdjust;
	}
	
	public double getMinLength(){
		return minLength;
	}
	
	public double getMinGap(){
		return minGap;
	}
	
	public int getFrameSize(){
		return frameSize;
	}
	
	public float getDynMax(){
		return dynMax;
	}
	
	public int getMaxF(){
		return maxf;
	}
	
	public int getDynEqual(){
		return dynEqual;
	}
	
	public int getEchoRange(){
		return echoRange;
	}
	
	public int getOverallLength(){
		return overallLength;
	}
	
	public int getNx(){
		return nx;
	}
	
	public int getNy(){
		return ny;
	}
	
	public int getBrushType(){
		return brushType;
	}
	
	public int getBrushSize(){
		return brushSize;
	}
	
	public int getMaxBrush(){
		return maxBrush;
	}
	
	public int getMinBrush(){
		return minBrush;
	}
	
	public float getNoiseRemoval(){
		return noiseRemoval;
	}
	
	public float[][] getOut(){
		return out;
	}
	
	public float[][] getEnvelope(){
		return envelope;
	}
	
	public int getNumPhrases(){
		if (phrases==null){
			interpretSyllables();
			if (phrases==null){
				return 0;
			}
		}
		return phrases.size();
	}
	
	public int[][] getPhrase(int a){
		//System.out.println("getting phrase "+a+" out of "+phrases.size());
		if (phrases==null){
			System.out.println("WARNING NULL PHRASES");
			interpretSyllables();
		}
		return phrases.get(a);
	}
	
	public void setPhrases(LinkedList<int[][]> a){
		phrases=a;
	}
	
	public void setSyllList(LinkedList<int[]> a){
		syllList=a;
	}
	
	//This is a legacy function to update syllable measurements for old versions of the db.
	public void updateSyllableList(){
		System.out.println("UPDATING!!!");
		for(int i=0; i<syllList.size(); i++){
			int[] s=syllList.get(i);
			int[]t=new int[2];
			t[0]=(int)Math.round(s[0]*dx);
			t[1]=(int)Math.round(s[1]*dx);
			syllList.remove(i);
			syllList.add(t);
		}
	}
	
	public int[] getSyllable(int a){
		if (a>=syllList.size()){
			return null;
		}
		return syllList.get(a);
	}
	
	public int getNumSyllables(){
		if (syllList==null){
			return 0;
		}
		return syllList.size();
	}
	
	public void removeSyllable(int a){
		if (a<syllList.size()){
			syllList.remove(a);
		}
	}
	
	public void addSyllable(int a, int[] syl){
		syllList.add(a, syl);
	}
	
	public void addSyllable(int[] syl){
		syllList.add(syl);
	}
	
	public void clearSyllables(){
		syllList.clear();
	}
	
	public void setEleList(LinkedList<Element> a){
		eleList=a;
	}
	
	public Element getElement(int a){
		if (a>=eleList.size()){
			return null;
		}
		return eleList.get(a);
	}
	
	public int getNumElements(){
		if (eleList==null){
			return 0;
		}
		return eleList.size();
	}
	
	public void removeElement(int a){
		if (a<eleList.size()){
			eleList.remove(a);
		}
	}
	
	public void addElement(int a, Element ele){
		eleList.add(a, ele);
	}
	
	public void addElement(Element ele){
		eleList.add(ele);
	}
	
	public void clearElements(){
		eleList.clear();
	}
	
	public void setTimeStep(double a){
		timeStep=a;
	}
	
	public void setFrameLength(double a){
		frameLength=a;
	}
	
	public void setEchoComp(double a){
		echoComp=a;
	}
	
	public void setSampRate(double a){
		sampRate=a;
	}
	
	public void setDynRange(double a){
		dynRange=a;
	}
	
	public void setDx(double a){
		dx=a;
	}
	
	public void setDy(double a){
		dy=a;
	}
	
	public void setFundAdjust(double a){
		fundAdjust=a;
	}
	
	public void setMinLength(double a){
		minLength=a;
	}
	
	public void setMinGap(double a){
		minGap=a;
	}
	
	public void setFrameSize(int a){
		frameSize=a;
	}
	
	public void setDynMax(float a){
		dynMax=a;
	}
	
	public void setMaxF(int a){
		maxf=a;
	}
	
	public void setDynEqual(int a){
		dynEqual=a;
	}
	
	public void setEchoRange(int a){
		echoRange=a;
	}
	
	public void setOverallLength(int a){
		overallLength=a;
	}
	
	public void setNx(int a){
		nx=a;
	}
	
	public void setNy(int a){
		ny=a;
	}
	
	public void setBrushType(int a){
		brushType=a;
	}
	
	public void setBrushSize(int a){
		brushSize=a;
	}
	
	public void setMaxBrush(int a){
		maxBrush=a;
	}
	
	public void setMinBrush(int a){
		minBrush=a;
	}
	
	public void setNoiseRemoval(float a){
		noiseRemoval=a;
	}
	
	public void setOut(float[][] a){
		out=a;
	}
	
	public void setEnvelope(float[][]a){
		envelope=a;
	}
	
	public int getSongID(){
		return songID;
	}
	
	public int getIndividualID(){
		return individualID;
	}
	
	public String getIndividualName(){
		return individualName;
	}
	
	public String getLocationX(){
		return locationX;
	}
	
	public String getLocationY(){
		return locationY;
	}
	
	public String getSpecies(){
		return species;
	}
	
	public String getName(){
		return name;
	}
	
	public String getPopulation(){
		return population;
	}
	
	public void setSongID(int a){
		songID=a;
	}
	
	public void setIndividualID(int a){
		individualID=a;
	}
	
	public void setIndividualName(String a){
		individualName=a;
	}
	
	public void setLocationX(String a){
		locationX=a;
	}
	
	public void setLocationY(String a){
		locationY=a;
	}
	
	public void setSpecies(String a){
		species=a;
		if (species==null){species=" ";}
	}
	
	public void setPopulation(String a){
		population=a;
		if (population==null){population=" ";}
	}
	
	public void setName(String a){
		name=a;
		if (name==null){name=" ";}
	}
	
	public AudioFormat getAf(){
		return af;
	}
	
	public int getRDLength(){
		return rawData.length;
	}
	
	public boolean getLoaded(){
		return loaded;
	}
	
	public void setLoaded(boolean a){
		loaded=a;
	}
	
	public int getStereo(){
		return stereo;
	}
	
	public boolean getBigEnd(){
		return bigEnd;
	}

	public double getSampleRate(){
		return sampleRate;
	}
	
	public byte[] getRawData(){
		return rawData;
	}
	
	public int getSizeInBits(){
		return ssizeInBits;
	}
	
	public long getTDate(){
		return tDate;
	}
	
	public boolean getSigned(){
		return signed;
	}
	
	public void setStereo(int a){
		stereo=a;
	}
	
	public void setBigEnd(boolean a){
		bigEnd=a;
	}
	
	public void setSampleRate(double a){
		sampleRate=a;
	}
	
	public void setRawData(byte[] a){
		rawData=a;
	}
	
	public void setSizeInBits(int a){
		ssizeInBits=a;
	}
	
	public void setTDate(long a){
		tDate=a;
	}
	
	public void setSigned(boolean a){
		signed=a;
	}
	
	public double getFrequencyCutOff(){
		return frequencyCutOff;
	}
	
	public int getWindowMethod(){
		return windowMethod;
	}
	
	public int getNoiseLength1(){
		return noiseLength1;
	}
	
	public int getNoiseLength2(){
		return noiseLength2;
	}
	
	public void setFrequencyCutOff(double a){
		frequencyCutOff=a;
	}
	
	public void setWindowMethod(int a){
		windowMethod=a;
	}
	
	public void setNoiseLength1(int a){
		noiseLength1=a;
	}
	
	public void setNoiseLength2(int a){
		noiseLength2=a;
	}
	
	public String getLocation(){
		return location;
	}
	
	public void setLocation(String a){
		location=a;
	}
	
	public String getNotes(){
		return notes;
	}
	
	public void setNotes(String a){
		notes=a;
	}
	
	public String getRecordEquipment(){
		return recordEquipment;
	}
	
	public void setRecordEquipment(String a){
		recordEquipment=a;
	}
	
	public String getRecordist(){
		return recordist;
	}
	
	public void setRecordist(String a){
		recordist=a;
	}
	
	public int getStartTime(){
		return startTime;
	}
	
	public int getEndTime(){
		return endTime;
	}
	
	public String getSx(){
		return sx;
	}
	
	public void setSx(String a){
		sx=a;
	}
	
	public String getSy(){
		return sy;
	}
	
	public void setSy(String a){
		sy=a;
	}
	
	public boolean getSetRangeToMax(){
		return setRangeToMax;
	}
	
	public void setSetRangeToMax(boolean a){
		setRangeToMax=a;
	}
	
	public double getOverlap(){
		return overlap;
	}
	
	public void setOverlap(double a){
		overlap=a;
	}
	
	public int getFrame(){
		return frame;
	}
	
	public void setFrame(int a){
		frame=a;
	}
	
	public double getFundJumpSuppression(){
		return fundJumpSuppression;
	}
	
	public void setFundJumpSuppression(double a){
		fundJumpSuppression=a;
	}
	
	public double getUpperLoop(){
		return upperLoop;
	}
	
	public void setUpperLoop(double a){
		upperLoop=a;
	}
	
	public double getLowerLoop(){
		return lowerLoop;
	}
	
	public void setLowerLoop(double a){
		lowerLoop=a;
	}
	
	public int getPlaybackDivider(){
		return playbackDivider;
	}
	
	public void setPlaybackDivider(int a){
		playbackDivider=a;
	}
	
	public int getRawDataLength(){
		return rawData.length;
	}
	
	public float getMaxDB(){
		return maxDB;
	}
	
	
	//Convenience get Method to build a complex SQL line to write data into database...
	public String getDetails(String b){
		int sa=(int)sampleRate;
		String details="echocomp="+echoComp+b+"echorange="+echoRange+b+"noise1="+noiseRemoval+b+"noise2="+
				noiseLength1+b+"noise3="+noiseLength2+b+"dyncomp="+dynRange+
				b+"dynrange="+dynEqual+b+"maxfreq="+maxf+b+"framelength="+frameLength+b+"timestep="
				+timeStep+b+"filtercutoff="+frequencyCutOff+b+"windowmethod="+windowMethod+b+"dx="+
				dx+b+"dy="+dy+b+"samplerate="+sa;
		return details;
	}
	
	
	
	public void setOverallSize(){
		if (frameSize==0){frameSize++;}
		overallSize=rawData.length/frameSize;
	}
	
	public int getOverallSize(){
		return overallSize;
	}
	
	public boolean[] getPhraseID(){
		return phraseId;
	}
	
	public void clearUp(){
		rawData=null;
		rawData2=null;
		bitTab=null;
		filtl=null;
		filtu=null;
		data=null;
		window=null;
		out=null;
		out1=null;
		eleList=null;
		syllList=null;
		notes=null;
		location=null;
		recordEquipment=null;
		recordist=null;
		trig1=null;
		trig2=null;
		places1=null;
		places2=null;
		places3=null;
		places4=null;
		
		shutDownPlayback();
		
	}
	
	public void tidyUp(){
		out=null;
		out1=null;
		data=null;
		eleList=null;
	}
	
	
	void parseSound2(){
		float[][]look=new float[256][frameSize];
		float divider=(float)(0.5*Math.pow(256, frameSize));
		data=new float[rawData.length/frameSize];
		float[]mult=new float[frameSize];
		for (int i=0; i<frameSize; i++){mult[i]=(float)Math.pow(256, i);}
		int k=0;
		for (int i=0; i<frameSize; i++){
			for (int j=0; j<256; j++){
				k=j;
				if ((i==frameSize-1)&&(j>127)){k-=256;}
				look[j][i]=k*mult[i];
			}
		}
		int numb=frameSize-1;
		int i=0;
		int n=0;
		int p;
		//float max=0;
		//float min=0;
		while (i<rawData.length){
			for (int j=0; j<numb; j++){
				p=rawData[i];
				if (p<0){p+=256;}
				data[n]+=look[p][j];
				i++;
			}
			
			p=rawData[i];
			if (p<0){p+=256;}
			data[n]+=look[p][numb];
			//data[n]=(float)Math.sin(n*0.5);
			data[n]/=divider;
			//if (data[n]>max){max=data[n];}
			//if (data[n]<min){min=data[n];}
			i++;
			n++;
		}
		look=null;
		if (stereo!=1){
			float[]out2=new float[data.length/stereo];
			for (i=0; i<out2.length; i++){
				for (int j=0; j<stereo; j++){
					out2[i]+=data[i*stereo+j];
				}
				out2[i]/=stereo;
			}
			//sampleRate/=stereo;
			data=new float[out2.length];
			System.arraycopy(out2, 0, data, 0, out2.length);
			out2=null;
		}
	}
	
	private void parseSound(int start, int end){
		int frameSizeC=frameSize/stereo;
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
		data=null;
		data=new float[size];
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
			i++;
			n++;
		}
		look=null;
		if (stereo!=1){
			float[]out2=new float[data.length/stereo];
			for (i=0; i<out2.length; i++){
				for (int j=0; j<stereo; j++){
					out2[i]+=data[i*stereo+j];
				}
				out2[i]/=stereo;
			}
			data=new float[out2.length];
			System.arraycopy(out2, 0, data, 0, out2.length);
			out2=null;
		}
	}
	
	void chunkyFFT(double chunks){
		place=0;
		int eTime, sTime;
		int overSize=overallSize;
		for (int i=0; i<=chunks; i++){
			sTime=startTime+i*20000;
			eTime=sTime+20000;
			if (eTime>endTime){eTime=endTime;}
			
			//System.out.println("BOUNDS 1: "+i+" "+sTime+" "+eTime);
			
			sTime=(int)Math.round(sTime*step);
			eTime=(int)Math.round(eTime*step+frame);
			
			int L=eTime-frame-sTime;
			
			//System.out.println("BOUNDS 2: "+i+" "+sTime+" "+eTime+" "+L);
			
			//while (eTime>overSize){eTime-=(int)Math.round(step);}
			if (eTime>overSize){eTime=overSize;}
			
			//System.out.println("BOUNDS 3: "+i+" "+sTime+" "+eTime);
			
			if (sTime<eTime){
				int adStartTime=sTime-rangef;
				if (adStartTime<0){adStartTime=0;}
				int adEndTime=eTime+rangef;
				if (adEndTime>overSize){adEndTime=overSize;}
				//int adStartTime=sTime;
				//int adEndTime=eTime;
				parseSound(adStartTime*stereo, adEndTime*stereo);
				//System.out.println("a: "+data.length+" "+adEndTime+" "+adStartTime+" "+rawData.length);
				filter(sTime-adStartTime, eTime-adStartTime);
				//System.out.println("b: "+data.length);
				calculateFFT(L);
			}
		}
	}

	
	public void makeMyFFT(int startTime, int endTime){
		this.startTime=startTime;
		this.endTime=endTime;
		
		double chunks=(endTime-startTime)/20000.0;
		
		calculateSampleFFT();

		
		chunkyFFT(chunks);
		
		/*
		int pframe=frame;
		float[][]o1=new float[ny][anx];
		for (int i=0; i<ny; i++){
			for (int j=0; j<anx; j++){
				o1[i][j]=out[i][j];
			}
		}
		frame=framePad/4;
		makeWindow(frame);
		chunkyFFT(chunks);
		float[][]o2=new float[ny][anx];
		for (int i=0; i<ny; i++){
			for (int j=0; j<anx; j++){
				o2[i][j]=out[i][j];
			}
		}
		frame=1+(framePad/8);
		makeWindow(frame);
		chunkyFFT(chunks);
		float[][]o3=new float[ny][anx];
		for (int i=0; i<ny; i++){
			for (int j=0; j<anx; j++){
				o3[i][j]=out[i][j];
			}
		}
		frame=pframe;
		makeWindow(frame);
		for (int i=0; i<ny; i++){
			for (int j=0; j<anx; j++){
				out[i][j]=(float)Math.pow(o1[i][j]*o2[i][j]*o3[i][j], 1/3.0);
				//if (o1[i][j]<o2[i][j]){out[i][j]=o1[i][j];}
				//if (o3[i][j]<out[i][j]){out[i][j]=o3[i][j];}
			}
		}
		*/
		
		if (dynEqual>0){
			dynamicEqualizer();
		}
		dynamicRange();
		//denoiser();
		//float maxCh=10f;
		//out=medianFilterXD(10, 0.45f);
		Matrix2DOperations m2o=new Matrix2DOperations();
		
		if (noiseRemoval>0){
			out=m2o.medianFilterNR(noiseLength1, 0.25f, noiseRemoval, out, 0);
			out=m2o.medianFilterNR1(noiseLength2, 0.75f, 0, out, 0);
		}
		
		
		if (echoComp>0){
			
			
			
			//out=m2o.accentuateLines(out, 5, 10);
			
			//float[][] out1=m2o.medianFilterNR2(20, 0.25f, 1.5f, out, 0);
			//out1=m2o.matrixMax(out1,  0);
			//out=m2o.matrixAdd(out1, out);
			
			//float[][] out3=m2o.medianFilterNR(100, 0.25f, 10, out, 0);
			//out=m2o.matrixMax(out1, out2);
			//out=m2o.matrixMax(out2, out3);
			//out=out2;
			int noisePasses=1;
			
			//float[][] ed=m2o.edgeDetection(1, false, out);
			//out=m2o.fillDetects(out, ed, 15, 0, 1);
			
			
			for (int i=0; i<noisePasses; i++){
				float[][] o=echoAverager2();
				out=m2o.matrixSubtract(out, o);
				//out=m2o.matrixSubSameMax(out, o);
				//echoAverager();
			}
			
			
			
			//out=m2o.medianFilterNR(20, 0.25f, 10, out, 0);
		}
		equalize();	
	}
	
	public void makeAmplitude(int startTime, int endTime, int steps){
		int s=(int)Math.round(startTime*step);
		int e=(int)Math.round(endTime*step);
		int overSize=overallSize;
		
		int adStartTime=s-rangef;
		if (adStartTime<0){adStartTime=0;}
		int adEndTime=e+rangef;
		if (adEndTime>overSize){adEndTime=overSize;}
		
		parseSound(adStartTime*stereo, adEndTime*stereo);
		filter(s-adStartTime, e-s);
		
		if (steps<data.length){
			
			double r=(data.length-0.0)/(steps-0.0);
		
			envelope=new float[steps][2];
			for (int i=0; i<steps-1; i++){
				
				int a=(int)Math.round(r*i);
				int b=(int)Math.round(r*(i+1));
				float maxAmp=-1000;
				float minAmp=1000;
				//System.out.println(data.length+" "+a+" "+b+" "+r+" "+i+" "+steps);
				for (int j=a; j<=b; j++){
					if (data[j]>maxAmp){
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
		
		else{
			envelope=new float[data.length][1];
			for (int i=0; i<data.length; i++){
				envelope[i][0]=data[0];
			}
		}	
	}
	
	void reconstitute(){
		float[]row=new float[anx];
		float freq=0;
		float p2=(float)(Math.PI*2/sampleRate);
		int size=(int)(anx*dx*0.001*sampleRate);
		float score=0;
		float place=0;
		int pl1;		
		
		for (int i=0; i<ny; i++){
			freq=(float)((i+0.5)*dy);
			for (int j=0; j<size; j++){
				place=(float)(j*1000*sampleRate/dx);
				pl1=(int)Math.floor(place);
				place-=pl1;
				score=out[i][pl1]*(1-place)+out[i][pl1+1]*place;
				row[j]+=(float)(score*Math.sin(j*freq*p2));
			}
		}
	}
	
	public void setFFTParameters(){
		//if (frameSize==1){frameSize=2;}
		sampRate=sampleRate;
		//step=(int)Math.round(timeStep*sampRate*0.001);	//step is the time-step of the spectrogram expressed in samples
		step=timeStep*sampRate*0.001;
		System.out.println(timeStep+" "+step);
		
		frame=(int)Math.round(frameLength*sampRate*0.001);	//frame is the frame-size of the spectrogram expressed in samples
		
		overlap=1-(timeStep/frameLength);
		
		framePad=2;
		int p2=1;
		while (framePad<frame){p2++; framePad=(int)Math.pow(2, p2);}
		framePad=(int)Math.pow(2, p2+2);								//framePad is the frame-size of the spectrogram, padded out to the next integer power of 2
		makeWindow(frame);
		makeTrigTables(framePad);
		makeBitShiftTables(framePad);
		filtl=makeFilterBank(frequencyCutOff, 1);
		filtu=makeFilterBank(0.9*(sampRate/2.0), 0);
		dy=sampRate/(framePad+0.0);
		dx=timeStep;
		
		overallSize=(rawData.length/(frameSize));
		overallLength=(int)Math.round(overallSize/(sampRate*0.001));
		
		double start=0;
		ny=(int)Math.floor(maxf/dy);
		//Adjust maxf if it lies outside possible values for the sound file.
		if (ny>framePad){
			ny=framePad;
			maxf=(int)Math.floor(ny*dy);
		}
		nx=0;
		while (start+frame<overallSize){start+=step; nx++;}
		System.out.println("Spect params: "+nx+" "+overallSize+" "+overallLength+" "+stereo+" "+frameSize+" "+sampRate+" "+step+" "+frame+" "+dy+" "+ny+" "+framePad);
	}
	
	public void  setFFTParameters2(int dist){
		double start=0;
		anx=0;
		while (start+frame<dist*step+frame){start+=step; anx++;}
		out=null;
		out1=null;
		out=new float[ny][anx];
		out1=new float[ny][anx];
		System.out.println("out dims: "+ny+" "+anx);
	}
	
	public int setFFTParameters3(int newny){
		sampRate=sampleRate;
		dy=maxf/(newny+0.0);
		framePad=2;
		int p2=1;
		while (sampRate/(framePad+0.0)>dy){p2++; framePad=(int)Math.pow(2, p2);}
		framePad=(int)Math.pow(2, p2-1);
		dy=sampRate/(framePad+0.0);
		ny=(int)Math.floor(maxf/dy);
		if (frame>framePad){
			frame=framePad;
			frameLength=(int)Math.round(frame/(sampRate*0.001));
		}
		makeWindow(frame);
		makeTrigTables(framePad);
		makeBitShiftTables(framePad);
		return ny;
	}
	
	public void makeAudioFormat(){
		af=new AudioFormat((float)sampleRate, ssizeInBits, stereo, signed, bigEnd);
	}	
	
	void makeStereoCopy(){
		
		stereoRawData=new byte[rawData.length*2];
		for (int i=0; i<rawData.length; i++){
			stereoRawData[i*2]=rawData[i];
			stereoRawData[i*2+1]=rawData[i];
		}
	}
	
	void makeStereoCopy(int ssizeInBits, int channel, boolean signed, boolean resample){
		
		int q=ssizeInBits/8;
		System.out.println("Bytes: "+q);
		int p=rawData.length*2;
		if (resample){p/=2;}
		stereoRawData=new byte[p];
		System.out.println("Size: "+rawData.length*2);
		byte silenceChar=-128;
		
		if (signed){
			silenceChar=0;
		}
		
		
		
		for (int i=0; i<p/(2*q); i++){
			int ii=i;
			if (resample){ii*=2;}
			for (int j=0; j<q; j++){
				
				if ((channel==0)||(channel==1)){				
					stereoRawData[i*q*2+j]=rawData[ii*q+j];
				}
				else{
					stereoRawData[i*q*2+j]=silenceChar;
				}
				if ((channel==0)||(channel==2)){
					stereoRawData[i*q*2+q+j]=rawData[ii*q+j];
				}
				else{
					stereoRawData[i*q*2+q+j]=silenceChar;
				}
			}
		}
	}
		
		
	
	public void setUpPlayback(){
		try{
			//af=new AudioFormat((float)sampleRate, ssizeInBits, stereo, signed, bigEnd);
			float p=(float)(sampleRate/playbackDivider);
			boolean resample=false;
			if (p>48000){
				resample=true;
				p/=2;
			}
			
			
			af=new AudioFormat((float)p, ssizeInBits, 2, signed, bigEnd);
			
			System.out.println("Stereo: "+stereo);
							   
			
			if (stereo==1){
				makeStereoCopy(ssizeInBits, 0, signed, resample);
			}
			else{
				stereoRawData=rawData;
			}
			
			
			
			DataLine.Info   info = new DataLine.Info(SourceDataLine.class, af);
			
			//MixerChooser mc=new MixerChooser();
			//mc.getMixerInfo(info);
			
			if (line!=null){line.close();}
			line = null;
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(af);
			FloatControl fc=(FloatControl)line.getControl(FloatControl.Type.PAN);
			fc.setValue(1f);
			
			
			
			
		
			
		} 
		catch (Exception e) {
			System.out.println("Here's the problem!");
			System.out.println(e);
			//System.exit(0);
		}//
	}
	
	public void setUpPlayback(Mixer mixer){
		try{
			af=new AudioFormat((float)sampleRate, ssizeInBits, stereo, signed, bigEnd);
			
			
			Mixer.Info minfo=mixer.getMixerInfo();
			
			line=AudioSystem.getSourceDataLine(af, minfo);
			
			
			
			line.open(af);
			//line.start();
			
			
		} 
		catch (Exception e) {
			System.out.println("Here's the problem!");
			System.out.println(e);
			//System.exit(0);
		}//
	}
	
	public void shutDownPlayback(){
		try{
			System.out.println("here1");
			if (line.isOpen()){
				line.flush();
				System.out.println("here2");
				//line.stop();
				line.close();
				System.out.println("here3");
			}
		}
		catch(Exception e){}
	}
	
	public double[] prepPlayback(int[] syll){
		
		int a=0;
		int b=0;
		
		if (ssizeInBits<=16){
			int p1=syll[0]-7;
			if (p1<0){p1=0;}
			int p2=syll[1]+7;
			if (p2>=overallLength){
				p2=overallLength-1;
			}
			a=(int)(p1*Math.round(sampleRate*stereo*0.002));
			b=(int)(p2*Math.round(sampleRate*stereo*0.002));
		}
		if(rawData.length<=b){
			b=a;
		}	
		
		playSound(a, b);
		
		double[] c={a/rawData.length, b/rawData.length};
		return c;
	}
		
	
		
	public void playSound (int a, int b){
		try{
			//af=new AudioFormat((float)sampleRate, ssizeInBits, stereo, signed, bigEnd);
			//DataLine.Info   info = new DataLine.Info(SourceDataLine.class, af);
			//line = null;
			//line = (SourceDataLine) AudioSystem.getLine(info);
			//line.open(af);
			//line.start();
			
			if ((line==null)||(!line.isOpen())){setUpPlayback();}
			
			line.start();
			
			Thread play=new Thread(new PlaySound(a, b));
			play.setPriority(Thread.MIN_PRIORITY);
			play.start();
		} 
		catch (Exception e) {
			System.out.println(e);
			System.exit(0);
		}//
	}
	
	class PlaySound extends Thread{
		
		int start, end;
		//byte data[] = new byte[10000];
		
		public PlaySound(int start, int end){
			this.start=start;
			this.end=end;
		}

		public void run(){
			//System.out.println(stereo+" "+bigEnd+" "+signed+" "+sampleRate);
			
			
			//int start2=start/(stereo*2);
			//int end2=end/(stereo*2);
			//double size=rawData.length;
			//double size2=rawData.length/(stereo*2.0);
			//byte[]  abData = new byte[EXTERNAL_BUFFER_SIZE];
			System.out.println(EXTERNAL_BUFFER_SIZE+" "+start+" "+end);
			try{
				
				
				
				line.write(stereoRawData, start*2, 2*(end-start));
				//int a=line.write(stereoRawData, start, (end-start));
			
				//while ((line.isOpen())&&(start+EXTERNAL_BUFFER_SIZE<end)){
				//	System.arraycopy(rawData, start, abData, 0, EXTERNAL_BUFFER_SIZE);
				//	int a=line.write(abData, 0, EXTERNAL_BUFFER_SIZE);
				//	start+=EXTERNAL_BUFFER_SIZE;
				//}
							}
			catch (Exception e) {
				e.printStackTrace();
				System.out.println("OWWW");
			}
		}
	}
	
	public void stopSound(){
		try{
			if (line.isOpen()){
				line.stop();
				line.flush();
				//line.close();
			}
		}
		catch (Exception e) {
			System.out.println("OWWW too");
		}
	}
	
	public void sortSyllsEles(){
		int num=syllList.size();
		int [] dat;
		while (num>0){
			int min=10000000;
			int loc=0;
			for (int i=0; i<num; i++){
				dat=syllList.get(i);
				if (dat[0]<min){
					loc=i;
					min=dat[0];
				}
			}
			
			dat=syllList.get(loc);
			syllList.remove(loc);
			syllList.addLast(dat);
			num--;
		}
		num=eleList.size();
		while (num>0){
			double min=10000000;
			int loc=0;
			for (int i=0; i<num; i++){
				Element ele=eleList.get(i);
				double j=ele.signal[0][0]+0.5*(ele.signal[ele.signal.length-1][0]-ele.signal[0][0]);
				if (j<min){
					loc=i;
					min=j;
				}
			}
		
			Element ele=eleList.get(loc);
			eleList.remove(loc);
			eleList.addLast(ele);
			num--;
		}
		dat=null;
	}
	
	private void filter(int startd, int endd){
		float[] data1=fir(startd, endd, filtu, data);
		if (frequencyCutOff>0){
			float[] data2=fir(startd, endd, filtl, data1);
			for (int i=0; i<data.length; i++){
				data1[i]=data2[i];
			}
		}

		int size=endd-startd;
		data=new float[size];
		for (int i=0; i<size; i++){
			data[i]=data1[i+startd];
		}		
	}

	private float[] fir(int startd, int endd, double[] filt, float[] d){
		///System.out.println(startd+" "+endd+" "+frequencyCutOff+" "+data.length);
		int i, j, k;
		int size=d.length;
		float[] data2=new float[size];
		for (i=0; i<size; i++){
			k=i-(rangef/2);
			for (j=0; j<rangef; j++){
				if ((k>=0)&&(k<size)){data2[i]+=(float)d[k]*filt[j];}
				k++;
			}
		}
		return data2;
	}
	
	double[] makeFilterBank(double fco, int type){
		int m=300;
		double M=m;
		rangef=m+1;
		double M2=m/2;
		int m2=(int)M2;
		double K=0;
		double fc=fco/sampRate;
		System.out.println(fco+" "+fc+" "+sampRate);
		double one, two;
		double[] filt=new double[rangef];
		int i;
		for (i=0; i<rangef; i++){
			double p=Math.PI*(i-M2);
			one=Math.sin(2*p*fc)/p;
			if (type==1){
				one*=-1;
			}
			two=0.42-0.5*Math.cos((2*Math.PI*i)/M)+0.08*Math.cos((4*Math.PI*i)/M);
			filt[i]=one*two;
			//System.out.println(i+" "+filt[i]);
		}
		filt[(int)M2]=2*fc;
		if (type==1){
			filt[(int)M2]=1-2*fc;
		}
		//for (i=0; i<rangef; i++){K+=filt[i];}
		//for (i=0; i<rangef; i++){
		//	filt[i]/=K;
		//}
		//filt[m2]++;
		return filt;
	}
	
	private void calculateFFT(int L){
		int L2=data.length;
		float[] dat=new float[2*framePad];
		maxAmp=0;
		int istart=0;
		double start=0;
		int i=0;
		int py=ny;
		if (py>framePad){py=framePad;}				//py is calculated in case the user picks a max freq that is above the resolving power of the other spectrogram
		float tempr, tempi;							//settings. If that happens, the spectrogram only plots up to the max. resolving power.
		int a,b,c,d;
		
		while(istart<L){
			//System.out.println(L+" "+start+" "+frame+" "+place);
			for (i=0; i<frame; i++){
				d=i+istart;
				if (d<L2){
					dat[2*i]=window[i]*data[d];				//This is where the window is applied
				}
				else{
					dat[2*i]=0;
				}
				dat[2*i+1]=0;
			}
			for(i=0; i<counter2; i++) {
				tempr = dat[bitTab[i][0]];
				dat[bitTab[i][0]] = dat[bitTab[i][1]];				//This is the bit-shifting
				dat[bitTab[i][1]] = tempr;
			}
			for (i=0; i<counter; i++){
				a=places1[i];
				b=places2[i];
				c=places3[i];
				d=places4[i];
				tempr = trig1[i]*dat[c] - trig2[i]*dat[d];			//This is the rest of the FFT
				tempi = trig1[i]*dat[d] + trig2[i]*dat[c];				
				dat[c] = dat[a] - tempr;
				dat[d] = dat[b] - tempi;
				dat[a] += tempr;
				dat[b] += tempi;
			}
			for(i=0; i<framePad; ++i) {  
				dat[2*i] /= framePad;
				dat[2*i + 1] /= framePad;
			}
			for (i=0; i<py; i++){
				a=2*i;
				b=2*i+1;
				if (place<out1[i].length){
					out1[i][place]=dat[a]*dat[a]+dat[b]*dat[b];						//This is the production of the power spectrum
					if (out1[i][place]>maxAmp){maxAmp=out1[i][place];}
					out[i][place]=out1[i][place];
				}
				
			}
			start+=step;
			istart=(int)Math.round(start);
			place++;
		}	
		dat=null;
	}

	private void calculateSampleFFT(){		//Makes a tonal signal of maximum amplitude to set a reference amplitude value
		float[] dat=new float[2*framePad];
		
		int i=0;
		float tempr, tempi;					
		float calc;
		int a,b,c,d;
		
		int N=framePad;
		double adj=sampRate*2/(maxf);
		for (i=0; i<frame; i++){
			dat[2*i]=(float)(window[i]*Math.sin(i/adj));				//This is where the fake signal is made
			dat[2*i+1]=0;
		}
		for(i=0; i<counter2; i++) {
			tempr = dat[bitTab[i][0]];
			dat[bitTab[i][0]] = dat[bitTab[i][1]];				//This is the bit-shifting
			dat[bitTab[i][1]] = tempr;
		}
		for (i=0; i<counter; i++){
			a=places1[i];
			b=places2[i];
			c=places3[i];
			d=places4[i];
			tempr = trig1[i]*dat[c] - trig2[i]*dat[d];			//This is the rest of the FFT
			tempi = trig1[i]*dat[d] + trig2[i]*dat[c];				
			dat[c] = dat[a] - tempr;
			dat[d] = dat[b] - tempi;
			dat[a] += tempr;
			dat[b] += tempi;
		}


		for(i=0; i<N; ++i) {  
			dat[2*i] /= N;
			dat[2*i + 1] /= N;
		}
		maxPossAmp=0;
		for (i=0; i<ny; i++){
			//calc=(float)(Math.sqrt(dat[2*i]*dat[2*i]+dat[2*i+1]*dat[2*i+1]));			
			calc=(float)(dat[2*i]*dat[2*i]+dat[2*i+1]*dat[2*i+1]);						//This is the production of the power spectrum
			if (calc>maxPossAmp){maxPossAmp=calc;}
		}	
		dat=null;
	}

	private void makeTrigTables(int val){				//This method produces look-up tables for the production of the spectrograms
		val=val*2;
		double om2=2.0*Math.PI;
		int amt=2;
		counter=1;
		while (amt<val){amt*=2; counter++;}
		counter--;
		float[][] xrTab=new float[counter][];
		float[][] xiTab=new float[counter][];

		double sin, cos, xtemp, xr, xi;
		int M=2;
		for (int i=0; i<counter; i++){
		
			xrTab[i]=new float[M/2];
			xiTab[i]=new float[M/2];
			xr=1.0;
			xi=0.0;
			sin=Math.sin(om2/M);
			cos=Math.cos(om2/M)-1.0;
			for (int j=0; j<M/2; j++){
				xrTab[i][j]=(float)xr;
				xiTab[i][j]=(float)xi;
			
				xtemp = xr;
				xr = xr + xr*cos - xi*sin;
				xi = xi + xtemp*sin + xi*cos;
			}
			M*=2;
		}		
		
		
		int count=0;
		int a=1;
		int b=2;
		int c;
		int d=framePad*2;
		for (int i=0; i<counter; i++){
			c=b*2;
			for (int j=0; j<a; j++){
				for (int k=j*2; k<d; k+=c){
					count++;
				}
			}
		}
		places1=new int[count];
		places2=new int[count];
		places3=new int[count];
		places4=new int[count];
		trig1=new float[count];
		trig2=new float[count];
		a=1;
		b=2;
		float x,y;
		count=0;
		for (int i=0; i<counter; i++){
			c=b*2;
			for (int j=0; j<a; j++){
				x=xrTab[i][j];
				y=xiTab[i][j];
				for (int k=j*2; k<d; k+=c){
					places1[count]=k;
					places2[count]=k+1;
					places3[count]=k+b;
					places4[count]=k+b+1;
					trig1[count]=x;
					trig2[count]=y;
					count++;
				}
			}
			a=b;
			b=c;
		}
		counter=count;
	}
	
	private void makeBitShiftTables(int N){			//This method produces look-up tables for figuring out how to do the bit-shifting
		int i,j,k,tot;
		counter2=0;
		j=0;
		for(i=0; i<N-1; i++) {
			if (i<j){counter2++;}
			k = N/2;
			while (k <= j)  {
				j -= k;
				k >>= 1;
			}
			j += k;
		}
		
		bitTab=new int[counter2][2];
		tot=0;
		j=0;
		for(i=0; i<N-1; i++) {
			if (i<j){
				bitTab[tot][0]=2*i;
				bitTab[tot][1]=2*j;
				tot++;
			}
			k = N/2;
			while (k <= j)  {
				j -= k;
				k >>= 1;
			}
			j += k;
		}
		
	}
	

	private void makeWindow(int N){			//produces various windowing functions
		window=new float[N];
		if (windowMethod==3){
			for (int i=0; i<N; i++){window[i]=(float)(1-(0.5+0.5*Math.cos((2*Math.PI*i)/(N-1.0))));}
		}
		if (windowMethod==2){
			for (int i=0; i<N; i++){window[i]=(float)(1-(0.54+0.46*Math.cos((2*Math.PI*(i+1))/(N+1.0))));}
		}
		if (windowMethod==1){
			int mid=frame/2;
			for (int i=0; i<N; i++){window[i]=(float)(Math.exp((-1*(Math.PI*(i-mid))*(Math.PI*(i-mid)))/(N*N+0.0)));}
		}
		//for (int i=0; i<N; i++){
		//	System.out.println(i+" "+window[i]);
		//}
	}
	
	private void dynamicEqualizer(){			//a dynamic equalizer; key parameter (dynEqual) varies time-range over which spectrogram is normalized
		int dynEqual2=(int)(dynEqual/dx);
		if (dynEqual2>anx){dynEqual2=anx;}
		int range2=-1*dynEqual2+1;
		float max=0;
		int i,j, k;
		float[] tops=new float[anx];
		for (i=0; i<anx; i++){
			for (j=0; j<ny; j++){
				if (out[j][i]>tops[i]){tops[i]=out[j][i];}
			}
			//System.out.println(tops[i]);
		}
		for (i=0; i<anx; i++){
			max=0;
			for (j=range2; j<dynEqual2; j++){
				k=j+i;
				if ((k>=0)&&(k<anx)&&(tops[k]>max)){max=tops[k];}
			}
			if (max>0){
				max=maxAmp/max;				//I'm still not sure maxAmp is appropriate here!
				for (j=0; j<ny; j++){out[j][i]*=max;}
			}
		}
		tops=null;
	}
	
	private void dynamicRange(){		//converts power spectrum into logarithmic, decibel scale (relative to maxPossAmp).
		//System.out.print("a");
		double logd=10/(Math.log(10));
		double maxC=dynRange-Math.log(maxPossAmp)*logd;
		
		if (relativeAmp){
			maxC=dynRange-Math.log(maxAmp)*logd;
		}
		int i,j;
		maxAmp2=0;
		for (i=0; i<ny; i++){
			for (j=0; j<anx; j++){
				if (out[i][j]>0){
					out[i][j]=(float)(Math.log(out[i][j])*logd+maxC);
				}
				//if (out[i][j]<0){out[i][j]=0;}
				if (out[i][j]>maxAmp2){maxAmp2=out[i][j];}
			}
		}
		//System.out.println("b");
	}	
	
	

	
	
	
	
	void findBlobs(float[][] edges){

		int x=out.length; 
		int y=out[0].length;
		float[][] out3=new float[x][y];
		int[] xd={-1, -1, -1, 0, 0, 1, 1, 1};
		int[] yd={-1, 0, 1, -1, 1, -1, 0, 1};
		
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				float r=-1000f;
				float p=edges[i][j];
				float p1=out[i][j];
				for (int k=0; k<8; k++){
					
					for (int g=1; g<10; g++){
						int xq=i+g*xd[k];
						int yq=j+g*yd[k];
							
						if ((xq>=0)&&(yq>=0)&&(xq<x)&&(yq<y)){
							float q=edges[xq][yq];
							float q1=out[xq][yq];
							if (q1<0){q1=0;}
							xq=i-g*xd[k];
							yq=j-g*yd[k];
							if ((xq>=0)&&(yq>=0)&&(xq<x)&&(yq<y)){
								float s=edges[xq][yq];
								float s1=out[xq][yq];
								if (s1<0){s1=0;}
								q=Math.min(s,q)-p;
								
								q+=p1-Math.min(s1,q1);
								if(q>r){
									r=q;
								}
							}
						}
					}	
				}	
				out3[i][j]=0.5f*(r+out[i][j]);

			}
		}
		out=out3;	
	}
	
	

	void medianFilterNoiseRemoval(){
		
		int kr=10;
		
		int x=out.length;
		int y=out[0].length;
		
		float[][] filtered=new float[x][y];
		
		float[] buffer=new float[(2*kr+1)*(2*kr+1)];
		
		int a1, a2, b1, b2, w;
		double summer=0;	
		for (int a=0; a<x; a++){
			a1=a-kr;
			if (a1<0){a1=0;}
			a2=a+kr;
			if (a2>=x){a2=x-1;}
			
			for (int b=0; b<y; b++){
				
				b1=b-kr;
				if (b1<0){b1=0;}
				b2=b+kr;
				if (b2>=y){b2=y-1;}
				
				w=0;
				for (int c=a1; c<=a2; c++){
					for (int d=b1; d<=b2; d++){
						buffer[w]=out[c][d];
						w++;
					}
				}
				if (w<=buffer.length){
					for (int c=w-1; c<buffer.length; c++){
						buffer[c]=0;
					}
				}
				Arrays.sort(buffer);
				filtered[a][b]=buffer[buffer.length/2];
				summer+=filtered[a][b];
			}
		}
		summer/=x*y*1.0;
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				out[i][j]-=(float)(0.5*(filtered[i][j]-summer));
			}
		}
		//out=filtered;
	}	
	
	
	private float[][] blobAccentuator2(){
		
		//out=gaussianBlur(2);
		int x=out.length;
		int y=out[0].length;
		float[][] out2=new float[x][y];
		
		int[] xd={-1, -1, -1, 0, 0, 1, 1, 1};
		int[] yd={-1, 0, 1, -1, 1, -1, 0, 1};
		
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				
				int xp=i;
				int yp=j;
				float p=out[i][j];
				float q=0;
				boolean cont=true;
				while (cont){
					cont=false;
					int kc=-1;
					for (int k=0; k<8; k++){
						int xq=xp+xd[k];
						int yq=yp+yd[k];
						if ((xq>=0)&&(yq>=0)&&(xq<x)&&(yq<y)&&(out[xq][yq]>p)){
							p=out[xq][yq];
							cont=true;
							kc=k;
						}
					}
					if (cont){
						float r=out[xp][yp]-p;
						if (r>q){q=r;}
						xp+=xd[kc];
						yp+=yd[kc];
					}
				}
				p-=q;
				if (p>10){p=10;}
				out2[i][j]=out[i][j]+p;	
			}
		}
		
		return out2;
	}
	
	/*
	
	void blobAccentuator(){
		int repeats=3;
		int rad1=2;
		int kw=2*rad1+1;
		float[][] out2=gaussianBlur(rad1);
		float[][] kernel=makeGaussianKernel(rad1);
		
		int x=out2.length;
		int y=out2[0].length;
		
		int[][] top=new int[x][y];
		int[][] sec=new int[x][y];
		float[][] out3=new float[x][y];
		
		int[] xs={-kw, -kw, -kw, 0, kw, kw, kw, 0};
		int[] ys={-kw, 0, kw, kw, kw, 0, -kw, -kw};
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				//if(out[i][j]>0){
					float first=-10000;
					float second=-10000;
					int locf=-1;
					int locs=-1;
					for (int k=0; k<8; k++){
						int ii=i+xs[k];
						if ((ii>=0)&&(ii<x)){
							int jj=j+ys[k];
							if ((jj>=0)&&(jj<y)){
								float p=out2[ii][jj];
								if (p>first){
									second=first;
									locs=locf;
									first=p;
									locf=k;
								}
								else if (p>second){
									second=p;
									locs=k;
								}
							}
						}
					}
					top[i][j]=locf;
					sec[i][j]=locs;
				//}
			}
		}
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				out2[i][j]=out[i][j];
				out3[i][j]=out[i][j];
			}
		}
		for (int r=0; r<repeats; r++){
			float[][] counter=new float[x][y];
			for (int i=0; i<x; i++){
				for (int j=0; j<y; j++){
					if(out[i][j]>0){
						int e=top[i][j];
						/*
						for (int f=xs[e]-rad1; f<=xs[e]+rad1; f++){
							int ff=f+i;
							if((ff>=0)&&(ff<x)){
								for (int g=ys[e]-rad1; g<=ys[e]+rad1; g++){
									int gg=g+j;
									if((gg>=0)&&(gg<y)&&(out3[ff][gg]<out[i][j])){
										out3[ff][gg]+=out[i][j]*kernel[f-xs[e]+rad1][g-ys[e]+rad1];
										if (out3[ff][gg]>out[i][j]){out3[ff][gg]=out[i][j];}
										counter[ff][gg]+=kernel[f-xs[e]+rad1][g-ys[e]+rad1];
									}
								}
							}
						}
						
						e=sec[i][j];
						for (int f=xs[e]-rad1; f<=xs[e]+rad1; f++){
							int ff=f+i;
							if((ff>=0)&&(ff<x)){
								for (int g=ys[e]-rad1; g<=ys[e]+rad1; g++){
									int gg=g+j;
									if((gg>=0)&&(gg<y)&&(out3[ff][gg]<out[i][j])){
										out3[ff][gg]+=out[i][j]*kernel[f-xs[e]+rad1][g-ys[e]+rad1];
										//if (out3[ff][gg]>out[i][j]){out3[ff][gg]=out[i][j];}
										counter[ff][gg]+=kernel[f-xs[e]+rad1][g-ys[e]+rad1];
									}
								}
							}
						}
					}
					
				}
			}
			for (int i=0; i<x; i++){
				for (int j=0; j<y; j++){
					//out[i][j]=out3[i][j]/(1+counter[i][j]);
					out[i][j]=out3[i][j];
					out3[i][j]=out[i][j];
				}
			}
		}	
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				//out[i][j]=out[i][j]+out2[i][j];
			}
		}
	}
	
	void blobAccentuator3(float[][] out2){
		//float[][] out2=medianFilterXY(10, 0.45f);
		//float[][] out2=out;
		int x=out.length; 
		int y=out[0].length;
		float[][] out3=new float[x][y];
		int[] xd={-1, -1, -1, 0, 0, 1, 1, 1};
		int[] yd={-1, 0, 1, -1, 1, -1, 0, 1};
		
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				float r=-1000f;
				float p=out2[i][j];
				for (int k=0; k<8; k++){
					
					for (int g=1; g<10; g++){
						int xq=i+g*xd[k];
						int yq=j+g*yd[k];
							
						if ((xq>=0)&&(yq>=0)&&(xq<x)&&(yq<y)){
							float q=out2[xq][yq];
							//if (q<0){q=0;}
							xq=i-g*xd[k];
							yq=j-g*yd[k];
							if ((xq>=0)&&(yq>=0)&&(xq<x)&&(yq<y)){
								float s=out2[xq][yq];
								//if(s<0){s=0;}
								q=p-(0.5f)*(s+q);
								if(q>r){
									r=q;
								}
							}
						}
					}	
				}	
				out3[i][j]=r;
				if (out[i][j]<out3[i][j]){out3[i][j]=out[i][j];}
				out3[i][j]+=out[i][j];
			}
		}
		out=out2;	
	}
	
void blobAccentuator4(){
		
		int x=out.length; 
		int y=out[0].length;
		float[][] out2=new float[x][y];
		int[] xd={-1, -1, -1, 0, 0, 1, 1, 1};
		int[] yd={-1, 0, 1, -1, 1, -1, 0, 1};
		
		for (int i=0; i<x; i++){
			for (int j=0; j<y; j++){
				
				if (out[i][j]>0){
					
					float p=out[i][j];
					
					int xp=i;
					int yp=j;
					int kk=0;
					while (kk>=0){
						if (out[i][j]>out2[xp][yp]){
							out2[xp][yp]=out[i][j];
						}
						float r=0;
						kk=-1;
						for (int k=0; k<8; k++){
							int xq=xp+xd[k];
							int yq=yp+yd[k];
							
							if ((xq>=0)&&(yq>=0)&&(xq<x)&&(yq<y)){
								float q=out[xq][yq];
								if((q>r)&&(q<p)){
									r=q;
									kk=k;
								}
							}
						}
						if (kk>=0){
							xp=xp+xd[kk];
							yp=yp+yd[kk];
							p=r;
						}
					}	
				}	
			}
		}
		out=out2;	
	}
	*/
	/*
	void lineAccentuator(){
		
		//this method carries out a simple line detection convolution
		
		int rad1=5;
		int diam=2*rad1+1;
		float inv=(float)(1/(diam+0.0));
		
		int[][] k=makeKernels(rad1);		
		
		int x=out.length;
		int y=out[0].length;
		
		int[][] filtered=new int[x][y];
		float[] medf=new float[diam];
		
		for (int a=0; a<x; a++){			
			for (int b=0; b<y; b++){
				double bestsc=-10000;
				double worstsc=10000;
				int loc=0;
				for (int c=0; c<k.length; c++){
					double sc=0;
					for (int d=0; d<diam; d++){
						int aa=a+k[c][d];
						if ((aa>=0)&&(aa<x)){
							int bb=b+k[c][d+diam];
							if ((bb>=0)&&(bb<y)){
								if(out[aa][bb]<out[a][b]){sc+=(out[aa][bb]-out[a][b])*(out[aa][bb]-out[a][b]);}
								//sc+=out[aa][bb];
							}
						}
					}
					
					if (sc>bestsc){bestsc=sc;}
					if (sc<worstsc){worstsc=sc;  filtered[a][b]=c;}
				}
			}
		}
		double sum=0;
		for (int a=0; a<x; a++){
			for (int b=0; b<y; b++){
				//out[a][b]=filtered[a][b];
				
				sum+=out[a][b];
				//out[a][b]+=100;
			}
		}
		
		sum/=x*y*1.0;
		//float out2[][]=new float[x][y];
		for (int repeat=0; repeat<1; repeat++){
			for (int a=0; a<x; a++){
				for (int b=0; b<y; b++){
					int loc=filtered[a][b];
					double sc=0;
					double co=0;
					for (int d=a-1; d<=a+1; d++){
						if ((d>=0)&&(d<x)){
							for (int e=b-1; e<b+1; e++){
								if ((e>=0)&&(e<y)){
								//if (out[a][b]>out[aa][bb]){
									int di=Math.abs(loc-filtered[d][e]);
									if (di>rad1){di=diam-di;}
									sc+=di;
									co++;
									//out2[aa][bb]=(0.25f*out[a][b]+out[aa][bb]);
								}
							}
						}
					}
					sc/=(co*rad1);
					out[a][b]+=(float)(10*(1-sc));
				}
			}
			//for (int a=0; a<x; a++){
			//	for (int b=0; b<y; b++){
			//		out[a][b]=out2[a][b];
			//	}
			//}
		}
		double sum2=0;
		for (int a=0; a<x; a++){
			for (int b=0; b<y; b++){
				sum2+=out[a][b];
			}
		}
		sum2/=x*y*1.0;
		System.out.println(sum+" "+sum2);
		float adj=(float)(sum-sum2);
		for (int a=0; a<x; a++){
			for (int b=0; b<y; b++){
				//out[a][b]+=adj;
			}
		}
		
	}
	
	
	
	*/
	

	
	
	
	private void echoAverager(){		//Echo removal algorithm. Algorithm looks for maximum in previous echorange time-steps. 
		float[][] out2=out;
		//float[][] out2=gaussianBlur(20);
		
		int echoRange2=(int)(echoRange/dx);
		int i,j,b,c;
		int aanx=anx-1;
		double score=0;
		float ec=(float)(echoComp);
				
		for (i=0; i<ny; i++){
		
			
			for (j=aanx; j>=0; j--){
				if ((j==aanx)||(out2[i][j+1]==score)){
					score=0;
					c=j-echoRange2;
					if (c<0){c=0;}
					for (b=c; b<=j; b++){
						if (out2[i][b]>score){score=out2[i][b];}
					}
				}
				else{							//this else bit is my clever attempt to speed things up: you track back progressively over a whole row; you only really search for the largest value
					b=j-echoRange2;				//over all of the previous echorange samples IF the maximum drops off the end.
					if ((b>=0)&&(out2[i][b]>score)){score=out2[i][b];}
				}
				out[i][j]-=(float)(ec*(score-out2[i][j]));
				if (j==0){
					out[i][j]=0;
				}
			}
		}
	}
	
	private float[][] echoAverager2(){		//Echo removal algorithm. Algorithm looks for maximum in previous echorange time-steps. 
		float[][] out2=out;

		int echoRange2=(int)(echoRange/dx);
		if (echoRange2>anx){echoRange2=anx;}
		
		//System.out.println("ECHO: "+echoRange+" "+echoRange2+" "+anx);
		
		double minWeight=10;
		double decayFactor=(minWeight)/(echoRange2+0.0);
		//System.out.println(decayFactor);
		int i,j,k;
		double[] buffer=new double[echoRange2];
		int position=0;
		double bestBuf;
		
		float[][]t=new float[ny][anx];
		for (i=0; i<ny; i++){
			//buffer=new double[echoRange2];
			for (k=0; k<echoRange2; k++){
				buffer[k]=-1000000000;
			}
			position=0;
			
			for (j=0; j<anx; j++){
				buffer[position]=out2[i][j];
				
				bestBuf=0;

				for (k=0; k<echoRange2; k++){	
					if (buffer[k]>bestBuf){
						bestBuf=buffer[k];
					}

					buffer[k]-=decayFactor;
				}
				bestBuf=echoComp*(bestBuf-out2[i][j]);
				
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
	
	public double[] sampleEchoDecays(){
		//this method takes the median regression through each point, along the x-axis of the spectrogram
		
		int windowSize=(int)(0.5*echoRange/dx);
		//int windowSize=50;
		if (windowSize>anx){windowSize=anx;}
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
		
		int xwidth=anx-windowSize;
		double[] buffer=new double[xwidth];
		for (int i=0; i<ny; i++){
			for (int j=0; j<xwidth; j++){
				double ysum=0;
				double xysum=0;
				for (int k=0; k<windowSize; k++){
					float p=out[i][j+k];
					ysum+=p;
					xysum+=k*p;
				}
				buffer[j]=(xysum+(xsum*ysum))*den;	
			}
			Arrays.sort(buffer);
			results[i]=buffer[medLoc];
			System.out.println(results[i]);
		}
		return results;
		
		
		
		
	}
		
		
	public float[][] enhanceDistinctiveness(double wei, int p1, int p2, float maxCh){
		
		int rad1=p1;
		int rad2=p2;
		
		int er1=2*rad1+1;
		int er2=2*rad2+1;
				
		if (er1>anx){er1=anx;}
		if (er2>anx){er2=anx;}
		
		float ec=(float)(wei);
		
		float[][] t=new float[ny][anx];
		float[] buffer1=new float[er1];
		float[] buffer2=new float[er2];

		float[] maxout=new float[ny];
		float[] maxt=new float[ny];
		for (int i=0; i<ny; i++){
			maxout[i]=Float.NEGATIVE_INFINITY;
			maxt[i]=Float.NEGATIVE_INFINITY;
		}
		
		for (int i=0; i<ny; i++){
			
			
			for (int j=0; j<er1; j++){
				buffer1[j]=out[i][j];
			}
			Arrays.sort(buffer1);
			
			for (int j=0; j<er2; j++){
				buffer2[j]=out[i][j];
			}
			Arrays.sort(buffer2);
			
			int place1=0-rad1;
			int place2=0-rad2;
			
			for (int j=0; j<anx; j++){
				if(out[i][j]>maxout[i]){maxout[i]=out[i][j];}
			}
			for (int j=0; j<anx; j++){
				//The following code continually adjusts the the frame, keeping its ascending order by substituting out one and adding one.
				if ((place1>0)&&(place1<anx-er1)){
					int kk=0;
					float p=out[i][place1-1];
					boolean found=false;
					for (int k=0; k<er1-1; k++){
						if ((!found)&&(buffer1[k]==p)){
							kk++;
							found=true;
						}
						buffer1[k]=buffer1[kk];
						kk++;
					}
					p=out[i][place1+er1];
					for (int k=er1-2; k>=0; k--){
						if (buffer1[k]<=p){
							buffer1[k+1]=p;
							k=-1;
						}
						else{
							buffer1[k+1]=buffer1[k];
						}
					}
					if (p<buffer1[0]){buffer1[0]=p;}
				}
				
				if ((place2>0)&&(place2<anx-er2)){
					int kk=0;
					float p=out[i][place2-1];
					boolean found=false;
					for (int k=0; k<er2-1; k++){
						if ((!found)&&(buffer2[k]==p)){
							kk++;
							found=true;
						}
						buffer2[k]=buffer2[kk];
						kk++;
					}
					p=out[i][place2+er2];
					for (int k=er2-2; k>=0; k--){
						if (buffer2[k]<=p){
							buffer2[k+1]=p;
							k=-1;
						}
						else{
							buffer2[k+1]=buffer2[k];
						}
					}
					if (p<buffer2[0]){buffer2[0]=p;}
				}
				
				t[i][j]=out[i][j];
				
				float res1=out[i][j]-buffer1[rad1];
				float res2=out[i][j]-buffer2[rad2];
				
				
				//if(res1>0){
				//	res1=(res1/40f)*(maxout[i]-out[i][j]);
				//}
				//if(res2>0){
				//	res2=(res2/40f)*(maxout[i]-out[i][j]);
				//}
				
				
				if ((res1)>(res2)){
					if(res1>maxCh){res1=maxCh;}
					t[i][j]+=ec*res1;
				}
				else{
					if (res2>maxCh){res2=maxCh;}
					t[i][j]+=ec*res2;
				}
				if(t[i][j]>maxout[i]+20){t[i][j]=maxout[i]+20;}
				
				
				//t[i][j]+=ec*(out[i][j]-buffer1[medLoc1]);

				
				if(t[i][j]>maxt[i]){maxt[i]=t[i][j];}
				
				place1++;
				place2++;
				
			}
		}	
		
		for (int i=0; i<ny; i++){
			float adj=(maxout[i]-maxt[i]);
			for (int j=0; j<anx; j++){
				//t[i][j]+=adj;
			}
		}
		return t;
	}
	
	public void enhanceDistinctivenessFreq(double wei){
		
		int er=ny;
		float ec=(float)(wei);
		
		float[][] t=new float[ny][anx];
		float[] buffer=new float[er];
		int medLoc=er/2;
		float avout=0;
		float avt=0;
		for (int i=0; i<anx; i++){
			
			
			for (int j=0; j<er; j++){
				buffer[j]=out[j][i];
			}
			Arrays.sort(buffer);

			for (int j=0; j<ny; j++){
				t[j][i]=out[j][i]-ec*buffer[medLoc];
				avout+=out[j][i];
				avt+=t[j][i];				
			}
		}		
		float adj=(avout-avt)/(ny*anx*1f);
		for (int i=0; i<ny; i++){
			for (int j=0; j<anx; j++){
				out[i][j]=t[i][j];
			}
		}
	}
	
	
	
	
	
	void denoiser(){
		int sampleLength=100;
		double[] means=new double[ny];
		for (int i=0; i<ny; i++){
			for (int j=0; j<sampleLength; j++){
				means[i]+=out[i][j];
			}
		}
		for (int i=0; i<ny; i++){
			means[i]/=sampleLength+0.0;
		}
		double[] sds=new double[ny];
		for (int i=0; i<ny; i++){
			for (int j=0; j<sampleLength; j++){
				double p=out[i][j]-means[i];
				sds[i]+=p*p;
			}
		}
		for (int i=0; i<ny; i++){
			sds[i]/=sampleLength+0.0;
			sds[i]=Math.sqrt(i);
		}
		float cutOffs[]=new float[ny];
		for (int i=0; i<ny; i++){
			double p=means[i];
			if (p>0){
				cutOffs[i]=(float)p;
			}
			
		}
		for (int i=0; i<ny; i++){
			for (int j=0; j<anx; j++){
				out[i][j]-=cutOffs[i];
			}
		}

	}
	
	
	private void equalize(){
		maxDB=-10000;
		int i,j;
		int startx=0;
		if (echoComp>0){startx=(int)(echoRange/dx);}
		for (i=0; i<ny; i++){
			for (j=startx; j<anx; j++){
				if (out[i][j]>maxDB){maxDB=out[i][j];}
			}
		}
		if (setRangeToMax){
			dynMax=(float)(maxDB/dynRange);
		}
		//System.out.println("Done");
	}
	
	public void calculateGaps(){
		float gapbefore=0f;
		float gapafter=0f;
		for (int i=0; i<eleList.size(); i++){
			Element ele=eleList.get(i);
			if (i==0){gapbefore=-10000f;}
			gapafter=-10000f;
			if (i<eleList.size()-1){
				Element ele2=eleList.get(i+1);
				gapafter=(float)(ele2.signal[0][0]*ele2.timeStep-ele.signal[ele.length-1][0]*ele.timeStep);
			}
			ele.timeBefore=gapbefore;
			ele.timeAfter=gapafter;
		}
	}
	
	
	public void interpretSyllables(){
		phrases=new LinkedList<int[][]>();
		
		//syllList=new LinkedList();
		System.out.println(name);
		if (syllList.size()==0){
			//System.out.println("HERE WE ARE!");
			for (int i=0; i<eleList.size(); i++){
				Element ele=eleList.get(i);
				int[] sy={(int)((ele.begintime-1)*ele.timeStep), (int)((ele.begintime+ele.signal.length+1)*ele.timeStep)};
				//System.out.println(sy[0]+" "+sy[1]);
				syllList.add(sy);
			}
		}
		phraseId=new boolean[syllList.size()];
		for (int i=0; i<syllList.size(); i++){
			
			int[] syll1=syllList.get(i);
			
			boolean isPhrase=true;
			for (int j=0; j<syllList.size(); j++){
				int[] syll2=syllList.get(j);
				if ((j!=i)&&(syll1[0]>=syll2[0])&&(syll1[1]<=syll2[1])){
					isPhrase=false;
					j=syllList.size();
				}
			}
			//System.out.println(name+" "+i+" "+isPhrase);
			if (isPhrase){
				phraseId[i]=true;
				LinkedList<int[]> p=new LinkedList<int[]>();
				for (int j=0; j<syllList.size(); j++){
					int[] syll2=syllList.get(j);
					if ((j!=i)&&(syll2[0]>=syll1[0])&&(syll2[1]<=syll1[1])){
						p.add(syll2);
					}
				}
				if (p.size()==0){p.add(syll1);}
				int maxlength=0;
				for (int j=0; j<p.size(); j++){
					int[] s=p.get(j);
					//System.out.println(s[0]+" "+s[1]);
					int count=0;
					for (int k=0; k<eleList.size(); k++){
						Element ele=eleList.get(k);
						//System.out.println(ele.begintime+" "+ele.timeStep+" "+ele.signal.length);
						//System.out.println(i+" "+j+" "+k+" "+name+" "+individualName+" "+ele.begintime+" "+s[0]+" "+s[1]);
						double ml=(ele.begintime+(ele.signal.length/2))*ele.timeStep;
						
						
						if ((ml>s[0])&&(ml<=s[1])){
							count++;
						}
					}
					if (count>maxlength){maxlength=count;}
					int[] t=new int[count];
					count=0;
					for (int k=0; k<eleList.size(); k++){
						Element ele=eleList.get(k);
						double ml=(ele.begintime+(ele.signal.length/2))*ele.timeStep;
						if ((ml>s[0])&&(ml<=s[1])){
							t[count]=k;
							count++;
						}
					}
					p.remove(j);
					p.add(j, t);
				}
				
				int[][] eletable=new int[p.size()][maxlength];		//IMPORTANT! first element of phrase is number of repeats; second element is syllable length in elements
				for (int j=0; j<p.size(); j++){
					int[] s=p.get(j);
					
					if (s.length<maxlength){
						if (j==0){
							int b=0;
							for (int k=0; k<maxlength-s.length; k++){
								eletable[j][k]=-1;
								b++;
							}
							for (int k=0; k<s.length; k++){
								eletable[j][b]=s[k];
								b++;
							}
						}
						else{
							for (int k=0; k<s.length; k++){
								eletable[j][k]=s[k];
							}
							for (int k=s.length; k<maxlength; k++){
								eletable[j][k]=-1;
							}
						}
					}
					else{
						for (int k=0; k<s.length; k++){
							eletable[j][k]=s[k];
						}
					}
					
										
					
				}
				/*
				for (int j=0; j<eletable.length; j++){
					for (int k=0; k<eletable[j].length; k++){
						System.out.print(eletable[j][k]+" ");
					}
					System.out.println();
				}
				*/
				phrases.add(eletable);
				//System.out.println(phrases.size()+" "+eletable.length+" "+maxlength);
			}
		}
	}
	
	public double[] getPhase(){
		return phase;
	}
	
	public int getMinFreq(){
		return minFreq;
	}
	
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
	
	public float[][] runPitchCalculator(int unx){
		int ncores=Runtime.getRuntime().availableProcessors();
		
		float[][] pout=new float[unx][ny];
		
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
						pout[i][j]=ct[cores].pout[ii][j];
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
		return pout;
	}
	
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
			int oct=(int)Math.round(octstep);
			double[] spectrum=new double[ny];
			double[] hscor=new double[ny];
			double maxHscor;
			double subSuppression=fundAdjust;
			for (int i=start; i<end; i++){
				double peakAmp=-1000;
				for (int j=0; j<ny; j++){
					spectrum[j]=out[j][i];
					if (spectrum[j]<0){
						spectrum[j]=0;
					}
				
					hscor[j]=0;
					if (spectrum[j]>peakAmp){
						peakAmp=spectrum[j];
					}
					spectrum[j]=Math.pow(spectrum[j], subSuppression);
				}

				int count=0;
				double sumxy=0;
				for (int j=0; j<ny; j++){
					sumxy=0;
					for (int k=0; k<ny; k++){
						sumxy+=phase[count]*spectrum[k];
						count++;
					}
					hscor[j]=sumxy;
				}
				maxHscor=-1000000;

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
				/*
				for (int j=minFreq; j<ny; j++){
					double p=1-hscor[j];
					int q=(int)(p*255);
					if (q>255){q=255;}
					if (q<0){q=0;}
					out[ii][j]=q;
				}
				*/
			}
		}
	}

}
