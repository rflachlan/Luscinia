package lusc.net.sourceforge;
//
//  Element.java
//  Luscinia
//
//  Created by Robert Lachlan on 31/03/2006.
//  Copyright 2006 Robert Lachlan. All rights reserved.
//

public class Element {

	double timeStep=0;
	double frameLength=0;
	int maxf=0;
	int windowMethod=0;
	double dynEqual=0;
	double dynRange=0;
	int echoRange=0;
	int id;
	double echoComp=0;
	double dy=0;
	double[][] measurements;
	double[][] measurements2;
	double[][] statistics;
	double[][] compressedMeasures;
	int[] compressedPoints;
	double[] powerSpectrum;
	double[] sd;
	int[][] signal;
	int[][] signal2;
	int length=0;
	float overallPeak1, overallPeak2, timelength, timeBefore, timeAfter, ta, tb, timeMin, timeMax, withinSyllableGap, betweenSyllableGap;
	int begintime;
	String name;
	
	double lowerQuartile=0;
	double upperQuartile=0;
	double upper95tile=0;
	double lower95tile=0;
	double maxFreq=0;
	double minFreq=0;
	double energyVariance=0;
	
	
	public Element(){}
	
	public Element(Song song, int[][]signal, double[][] measurements, double[] powerSpectrum){
		timeStep=song.timeStep;
		frameLength=song.frameLength;
		maxf=song.maxf;
		windowMethod=song.windowMethod;
		//dynRange=song.dynEqual;
		//dynComp=song.dynRange;	NOTE CHANGE OF TERMS!!!
		dynEqual=song.dynEqual;
		dynRange=song.dynRange;
		
		echoRange=song.echoRange;
		echoComp=song.echoComp;
		dy=song.dy;
		this.signal=signal;
		this.measurements=measurements;
		this.powerSpectrum=powerSpectrum;
		length=signal.length;
	}
	
	public Element(Element oldElement){
	
		timeStep=oldElement.timeStep;
		frameLength=oldElement.frameLength;
		maxf=oldElement.maxf;
		windowMethod=oldElement.windowMethod;
		dynEqual=oldElement.dynEqual;
		dynRange=oldElement.dynRange;
		
		echoRange=oldElement.echoRange;
		echoComp=oldElement.echoComp;
		dy=oldElement.dy;
		length=oldElement.signal.length;
		overallPeak1=oldElement.overallPeak1;
		overallPeak2=oldElement.overallPeak2;
		timelength=oldElement.timelength;
		timeBefore=oldElement.timeBefore;
		timeAfter=oldElement.timeAfter;
		begintime=oldElement.begintime;
		
		withinSyllableGap=0;
		betweenSyllableGap=0;
		
		measurements=new double[length+5][];
		signal=new int[length][];
		for (int i=0; i<length+5; i++){
			measurements[i]=new double[oldElement.measurements[i].length];
			System.arraycopy(oldElement.measurements[i], 0, measurements[i], 0, oldElement.measurements[i].length);
		}
		for (int i=0; i<length; i++){
			signal[i]=new int[oldElement.signal[i].length];
			System.arraycopy(oldElement.signal[i], 0, signal[i], 0, oldElement.signal[i].length);
		}
		powerSpectrum=new double[oldElement.powerSpectrum.length];
		System.arraycopy(oldElement.powerSpectrum, 0, powerSpectrum, 0, powerSpectrum.length);
		
		int tmax=-100;
		int tmin=1000000;
		
		for (int j=0; j<length; j++){
			if (signal[j][0]>tmax){tmax=signal[j][0];}
			if (signal[j][0]<tmin){tmin=signal[j][0];}
		}
		
		timelength=(float)((tmax-tmin)*timeStep);
		timeMax=(float)(tmax*timeStep);
		timeMin=(float)(tmin*timeStep);
	}
	
	public Element(Element ele1, Element ele2){
		
		timeStep=ele1.timeStep;
		frameLength=ele1.frameLength;
		maxf=ele1.maxf;
		windowMethod=ele1.windowMethod;
		dynEqual=ele1.dynEqual;
		dynRange=ele1.dynRange;
		
		echoRange=ele1.echoRange;
		echoComp=ele1.echoComp;
		dy=ele1.dy;
		length=ele1.signal.length+ele2.signal.length;
		overallPeak1=ele1.overallPeak1;
		overallPeak2=ele1.overallPeak2;
		timelength=ele1.timelength;
		timeBefore=ele1.timeBefore;
		timeAfter=ele1.timeAfter;
		begintime=ele1.begintime;
		
		measurements=new double[length][];
		signal=new int[length][];
		for (int i=0; i<ele1.length; i++){
			measurements[i]=new double[ele1.measurements[i+5].length];
			System.arraycopy(ele1.measurements[i+5], 0, measurements[i], 0, ele1.measurements[i+5].length);
		}
		for (int i=0; i<ele2.length; i++){
			measurements[i+ele1.length]=new double[ele2.measurements[i+5].length];
			System.arraycopy(ele2.measurements[i+5], 0, measurements[i+ele1.length], 0, ele2.measurements[i+5].length);
		}
		
		
		for (int i=0; i<ele1.length; i++){
			signal[i]=new int[ele1.signal[i].length];
			System.arraycopy(ele1.signal[i], 0, signal[i], 0, ele1.signal[i].length);
		}
		for (int i=0; i<ele2.length; i++){
			signal[i+ele1.length]=new int[ele2.signal[i].length];
			System.arraycopy(ele2.signal[i], 0, signal[i+ele1.length], 0, ele2.signal[i].length);
		}
		
		powerSpectrum=new double[ele1.powerSpectrum.length];
		
		for (int i=0; i<powerSpectrum.length; i++){
			powerSpectrum[i]=ele1.powerSpectrum[i]+ele2.powerSpectrum[i];
		}
		
		calculateStatistics();
	}
	
	
	public Element(Element[] eleArray, boolean average){
		
		double n=eleArray.length;
		double tbn=0;
		double tan=0;
		statistics=new double[19][9];
		
		powerSpectrum=new double[eleArray[0].powerSpectrum.length];
		
		
		
		timeMax=0;
		timeMin=1000000;
		
		if (!average){
			timeBefore=eleArray[0].timeBefore;
			timeAfter=eleArray[eleArray.length-1].timeAfter;
			begintime=eleArray[0].begintime;
			
			
			for (int i=0; i<19; i++){
				statistics[i][3]=-1000000000;
				statistics[i][4]=1000000000;
			}
			
			withinSyllableGap=0;
			for (int i=0; i<n-1; i++){
				withinSyllableGap+=eleArray[i].timeAfter;
			}
			
			betweenSyllableGap=0;
			double a=0;
			if (timeBefore>-10000){
				betweenSyllableGap+=timeBefore;
				a++;
			}
			if (timeAfter>-10000){
				betweenSyllableGap+=timeAfter;
				a++;
			}
			if (a==0){betweenSyllableGap=-10000;}
			else{
				betweenSyllableGap/=a;
			}
		}
		
		
		for (int i=0; i<n; i++){
	
			timeStep+=eleArray[i].timeStep;
			frameLength+=eleArray[i].frameLength;
			maxf+=eleArray[i].maxf;
			windowMethod+=eleArray[i].windowMethod;
			dynEqual+=eleArray[i].dynEqual;
			dynRange+=eleArray[i].dynRange;
		
			echoRange+=eleArray[i].echoRange;
			echoComp+=eleArray[i].echoComp;
			dy+=eleArray[i].dy;
			length+=eleArray[i].length;
			overallPeak1+=eleArray[i].overallPeak1;
			overallPeak2+=eleArray[i].overallPeak2;
			timelength+=eleArray[i].timelength;
			
			if (average){
				withinSyllableGap+=eleArray[i].withinSyllableGap;
				betweenSyllableGap+=eleArray[i].betweenSyllableGap;
				if (eleArray[i].timeBefore>-10000){
					timeBefore+=eleArray[i].timeBefore;
					tbn++;
				}
				if (eleArray[i].timeAfter>-10000){
					timeAfter+=eleArray[i].timeAfter;
					tan++;
				}
			}
			
			if (eleArray[i].timeMax>timeMax){timeMax=eleArray[i].timeMax;}
			if (eleArray[i].timeMin<timeMin){timeMin=eleArray[i].timeMin;}
			
			begintime+=eleArray[i].begintime;
			for (int j=0; j<19; j++){
				if (!average){
				
					for (int k=0; k<3; k++){
						statistics[j][k]+=eleArray[i].statistics[j][k]*eleArray[i].length;
					}
				
					for (int k=7; k<9; k++){
						statistics[j][k]+=eleArray[i].statistics[j][k]*eleArray[i].length;
					}
				
					if (eleArray[i].statistics[j][3]>statistics[j][3]){
						statistics[j][3]=eleArray[i].statistics[j][3];
						statistics[j][5]=eleArray[i].statistics[j][5];
					}
					if (eleArray[i].statistics[j][4]<statistics[j][4]){
						statistics[j][4]=eleArray[i].statistics[j][4];
						statistics[j][6]=eleArray[i].statistics[j][6];
					}
				}
			
				else{
					for (int k=0; k<9; k++){
						statistics[j][k]+=eleArray[i].statistics[j][k];
					}
				}
			}
			
			for (int j=0; j<powerSpectrum.length; j++){
				powerSpectrum[j]+=eleArray[i].powerSpectrum[j];
			}
			
		}
		timeStep/=n;
		frameLength/=n;
		maxf/=n;
		windowMethod/=n;
		dynEqual/=n;
		dynRange/=n;
		echoRange/=n;
		echoComp/=n;
		dy/=n;
		
		overallPeak1/=n;
		overallPeak2/=n;
		if (!average){
			timelength=timeMax-timeMin;
		}
		if (average){
			if (tbn>0){
				timeBefore/=tbn;
			}
			else{
				timeBefore=-10000;
			}
			if (tan>0){
				timeAfter/=tan;
			}
			else{
				timeAfter=-10000;
			}
			
			timelength/=n;
			begintime/=n;
			
			withinSyllableGap/=n;
			betweenSyllableGap/=n;
			length/=n;
		}
		
		if (average){
			for (int j=0; j<19; j++){
				for (int k=0; k<9; k++){
					statistics[j][k]/=n;
				}
			}
		}
		else{
			for (int j=0; j<19; j++){
				for (int k=0; k<3; k++){
					statistics[j][k]/=length;
				}
				for (int k=7; k<9; k++){
					statistics[j][k]/=length;
				}
				//statistics[j][6]=eleArray[0].statistics[j][6];
				//statistics[j][7]=eleArray[eleArray.length-1].statistics[j][6];
			}
		}
			
		
			
		calculatePowerSpectrumStats();
	}
	
	void calculateStatistics(){
		double results[][]=new double[length+5][15];
		double maxvals, minvals, timemax, timemin, avvals;
		overallPeak1=0;
		for (int j=0; j<15; j++){
			maxvals=-100000000;
			minvals=100000000;
			avvals=0;
			timemax=0;
			timemin=0;
			for (int k=0; k<length; k++){
				if (measurements[k][j]>maxvals){
					maxvals=measurements[k][j];
					timemax=k;
				}
				if (measurements[k][j]<minvals){
					minvals=measurements[k][j];
					timemin=k;
				}
				avvals+=measurements[k][j];
				results[k+5][j]=measurements[k][j];
			}
			if (j==11){
				overallPeak1=(float)measurements[(int)timemin][0];
			}
			avvals/=length+0.0;
			timemin*=timeStep;
			timemax*=timeStep;
			results[0][j]=maxvals;
			results[1][j]=minvals;
			results[2][j]=timemax;
			results[3][j]=timemin;
			results[4][j]=avvals;
		}
		measurements=null;
		measurements=results;
		
		int peakloc=0;
		double peaksize=-100000;
		for (int j=0; j<powerSpectrum.length; j++){
			if ((powerSpectrum[j]!=0)&&(powerSpectrum[j]>peaksize)){
				peaksize=powerSpectrum[j];
				peakloc=j;
			}
		}
		
		overallPeak2=(float)(peakloc*dy);
		
		
		int tmax=-100;
		int tmin=1000000;
		
		for (int j=0; j<length; j++){
			if (signal[j][0]>tmax){tmax=signal[j][0];}
			if (signal[j][0]<tmin){tmin=signal[j][0];}
		}
		
		timelength=(float)((tmax-tmin)*timeStep);
		timeMax=(float)(tmax*timeStep);
		timeMin=(float)(tmin*timeStep);
		begintime=signal[0][0];
		//System.out.println(overallPeak1+" "+overallPeak2);
	}
	
	void calculateStatisticsS(){
		
		calculateStatisticsAbsolute();
		
		statistics=new double[19][9];
		int a=measurements.length-1;
		double variance, b;
		double n=a-5;
		BasicStatistics bs=new BasicStatistics();
		
		for (int i=0; i<19; i++){
		
			statistics[i][0]=measurements[4][i];   //mean
			statistics[i][3]=measurements[0][i];	//max
			statistics[i][4]=measurements[1][i];	//min
			statistics[i][5]=measurements[2][i];	//timemax
			statistics[i][6]=measurements[3][i];	//timemin
			statistics[i][7]=measurements[5][i];	//start
			statistics[i][8]=measurements[a][i];	//end
	
			variance=0;
			double[] t=new double[measurements.length-5];
			for (int j=5; j<measurements.length; j++){
				b=measurements[4][i]-measurements[j][i];
				variance+=b*b;
				t[j-5]=measurements[j][i];
			}
			statistics[i][2]=variance/n;			//variance
			statistics[i][1]=bs.calculateMedian(t);	//median
	
		}	
	}
	
	
	void calculateStatisticsAbsolute(){
		double results[][]=new double[length+5][19];
		double maxvals, minvals, timemax, timemin, avvals;
		for (int j=0; j<15; j++){
			for (int k=0; k<length+5; k++){
				results[k][j]=measurements[k][j];
			}
		}
		measurements=null;
		measurements=results;
		for (int j=0; j<4; j++){
			maxvals=-100000000;
			minvals=100000000;
			avvals=0;
			timemax=0;
			timemin=0;
			double v;
			int w=j+4;
			int x=j+15;
			for (int k=5; k<length+5; k++){
				v=Math.abs(measurements[k][w]-0.5);
			
				if (v>maxvals){
					maxvals=v;
					timemax=k;
				}
				if (v<minvals){
					minvals=v;
					timemin=k;
				}
				avvals+=v;
				measurements[k][x]=v;
			}
			avvals/=length+0.0;
			timemin*=timeStep;
			timemax*=timeStep;
			measurements[0][x]=maxvals;
			measurements[1][x]=minvals;
			measurements[2][x]=timemax;
			measurements[3][x]=timemin;
			measurements[4][x]=avvals;
		}
	}
	
	public void calculatePowerSpectrumStats(){
		
		double tot=0;
		double average=0;
		for (int i=0; i<powerSpectrum.length; i++){
			tot+=powerSpectrum[i];
			average+=powerSpectrum[i]*i*dy;
		}
		average/=tot;
		
		double cumulative=0;
		boolean foundMin=false;
		energyVariance=0;
		for (int i=0; i<powerSpectrum.length; i++){

			if ((cumulative<tot*0.25)&&(cumulative+powerSpectrum[i]>=tot*0.25)){
				lowerQuartile=i+((tot*0.25-cumulative)/(powerSpectrum[i]));
			}
			if ((cumulative<tot*0.75)&&(cumulative+powerSpectrum[i]>=tot*0.75)){
				upperQuartile=i+((tot*0.75-cumulative)/(powerSpectrum[i]));
			}
			if ((cumulative<tot*0.05)&&(cumulative+powerSpectrum[i]>=tot*0.05)){
				lower95tile=i+((tot*0.05-cumulative)/(powerSpectrum[i]));
			}
			if ((cumulative<tot*0.95)&&(cumulative+powerSpectrum[i]>=tot*0.95)){
				upper95tile=i+((tot*0.95-cumulative)/(powerSpectrum[i]));
			}
			if ((!foundMin)&&(powerSpectrum[i]>0)){
				minFreq=i*dy;
				foundMin=true;
			}
			if (powerSpectrum[i]>0){
				maxFreq=i*dy;
			}
			energyVariance+=(average-i*dy)*(average-i*dy)*powerSpectrum[i];

			cumulative+=powerSpectrum[i];
		}
		energyVariance/=tot;
		lowerQuartile*=dy;
		upperQuartile*=dy;
		lower95tile*=dy;
		upper95tile*=dy;
	}
	
	public void compressElements(int data){
	
		boolean p[]=new boolean[length];
		boolean q[]=new boolean[length];
		int length1=length-5;
		double x, min, score;
		int loc=0;
		double minScore=100000000;
		int scoreLoc=0;
		for (int i=1; i<length1; i++){
			min=10000000;
			loc=0;
			for (int j=1; j<length1; j++){
				if (!p[j]){
					p[j]=true;
					x=calculateFit(p, data);
					if (x<min){
						min=x;
						loc=j;
					}
					p[j]=false;
				}
			}
			p[loc]=true;
			score=Math.log(min+1000)+Math.log(length-i+10);
			if (score<minScore){
				minScore=score;
				scoreLoc=i;
				for (int j=0; j<length; j++){
					q[j]=p[j];
				}
			}
			//System.out.println(i+" "+min+" "+score);
		}
		scoreLoc=length-scoreLoc;
		int dems=measurements[0].length;
		compressedMeasures=new double[scoreLoc][dems];
		compressedPoints=new int[scoreLoc];
		int count=0;
		for (int i=0; i<length; i++){
			if (!q[i]){
				//System.out.println(i+" "+length+" "+scoreLoc+" "+count+" "+measurements.length);
				for (int j=0; j<dems; j++){
					compressedMeasures[count][j]=measurements[i+5][j];
					
				}
				compressedPoints[count]=i;
				count++;
			}
		}		
	}
	
	public double calculateFit(boolean [] p, int d){
		double sum=0;
		int a, b,c;
		double x;
		for (int i=0; i<length; i++){
			if (p[i]){
				a=i;
				while (p[a]){a--;}
				b=i;
				while (p[b]){b++;}
				a+=5;
				b+=5;
				c=i+5;
				x=((i-a)/(b-a+0.0))*(measurements[b][d]-measurements[a][d]);
				x+=measurements[a][d];
				sum+=(x-measurements[c][d])*(x-measurements[c][d]);
			}
		}
		return Math.sqrt(sum);
	}


	
	public void update(Song song){
		timeStep=song.timeStep;
		frameLength=song.frameLength;
		maxf=song.maxf;
		windowMethod=song.windowMethod;
		dynEqual=song.dynEqual;
		dynRange=song.dynRange;
		echoRange=song.echoRange;
		echoComp=song.echoComp;
		double archdy=song.dy;
		song.setFFTParameters();
		dy=song.dy;
		boolean adjdy=false;
		if (dy!=archdy){adjdy=true;}
		double a=archdy/dy;
		length=signal.length;
		double measure2[][]=new double [length][15];
		double ny=maxf/dy;
		powerSpectrum=new double[(int)Math.round(ny)];
		for (int i=0; i<length; i++){
			measure2[i][0]=(ny-signal[i][1])*dy;
			measure2[i][3]=(ny-signal[i][2])*dy;
			if (adjdy){
				measure2[i][0]=(ny-(a*signal[i][1]))*dy;
				measure2[i][3]=(ny-(a*signal[i][2]))*dy;
			}
			
			if (measurements.length==measure2.length){
			
				measure2[i][8]=measurements[i][0];
				measure2[i][11]=measurements[i][1];
				measure2[i][12]=measurements[i][2];
			}
		}
		measurements=measure2;
		int sig2[][]=new int [length][];
		for (int i=0; i<length; i++){
			sig2[i]=new int[signal[i].length-2];
			sig2[i][0]=signal[i][0];
			for (int j=3; j<signal[i].length; j++){
				sig2[i][j-2]=signal[i][j];
				if (adjdy){
					sig2[i][j-2]=(int)Math.round(a*signal[i][j]);
				}
			}
		}
		signal=sig2;
		calculateStatistics();
		song.dy=archdy;
	}
	
	public double[] getMeasurements(int id, int npoints){
		double[] results=new double[npoints];
		
		int n=measurements.length-5;
		int n1=n-1;
		
		int m=npoints;
		int m1=m-1;
		
		double increment=n1/(m1+0.0);
		
		for (int i=0; i<m; i++){
			
			double p=increment*i;
			p+=5;
			
			int x1=(int)Math.floor(p);
			int x2=(int)Math.ceil(p);
			
			if (x1<5){x1=5;}
			if (x2>=measurements.length){x2=measurements.length-1;}
			
			double y1=measurements[x1][id];
			double y2=measurements[x2][id];
			
			double q1=p-x1;
			double q2=x2-p;
			if(x1!=x2){
				results[i]=y1*q2+y2*q1;
			}
			else{
				results[i]=y1;
			}
			
		}
		return results;
		
	}
	
	
}
