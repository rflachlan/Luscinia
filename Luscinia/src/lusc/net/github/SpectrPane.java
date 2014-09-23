package lusc.net.github;
//
//  SpectrPane.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/10/2005.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import javax.swing.event.*;
import java.awt.geom.AffineTransform;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.*;

public class SpectrPane extends DisplayPane implements MouseInputListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -87325727540762828L;
	BufferedImage imf, im;
	boolean started=false;
	boolean displayEdge=false;
	boolean editable=true;
	boolean syllable=false;
	boolean readytopaint=true;
	boolean customCursor=false;
	boolean automaticMerge=false;
	boolean paintOver=true;
	
	boolean[]viewParameters=new boolean[19];
	
	boolean readyToUpdateGP=false;
		
	Color syllableColor=new Color(255, 0, 0);
	Color elementColor=new Color(0, 166, 81);
	Color peakFreqColor=new Color(0,0,255);
	Color fundFreqColor=new Color(96, 57, 19);
	Color meanFreqColor=new Color(0,0,255);
	Color medianFreqColor=new Color(96, 57, 19);
	Color harmonicityColor=new Color(244, 154, 193);
	Color wienerColor=new Color(158, 0, 93);
	Color amplitudeColor=new Color(0, 88, 38);
	Color bandwidthColor=new Color(0, 88, 38);
	Color peakChangeColor=new Color(0, 174, 239);
	Color fundChangeColor=new Color(198, 156, 109);
	Color meanChangeColor=new Color(0, 174, 239);
	Color medianChangeColor=new Color(198, 156, 109);
	Color trillAmpColor=new Color(158, 158, 0);
	Color trillRateColor=new Color(158, 0, 158);
	Color reverbColor=new Color(0, 158, 158);
	
	int minorFreqTick=1000;
	int majorFreqTick=5000;
	int minorTimeTick=500;
	int majorTimeTick=1000;
	
	int unitStyle=0;
	
	int tickLabelFontSize=12;
	int axisLabelFontSize=14;
	
	String fontFace="Arial";
	
	int tickLabelFontStyle=Font.PLAIN;
	int axisLabelFontStyle=Font.PLAIN;
	
	Font timeAxisFont=new Font(fontFace, tickLabelFontStyle, tickLabelFontSize);
	Font freqAxisFont=new Font(fontFace, tickLabelFontStyle, tickLabelFontSize);	
	Font timeLabelFont=new Font(fontFace, axisLabelFontStyle, axisLabelFontSize);
	Font freqLabelFont=new Font(fontFace, axisLabelFontStyle, axisLabelFontSize);
	
	
	int majorTickMarkLength=5;
	int minorTickMarkLength=3;
	boolean interiorTickMarks=false;
	boolean showMajorTimeTickMarks=true;
	boolean showMinorTimeTickMarks=true;
	boolean showMajorFreqTickMarks=true;
	boolean showMinorFreqTickMarks=true;
	boolean showTimeTickMarkLabels=true;
	boolean showFreqTickMarkLabels=true;
	boolean showFrame=true;
	boolean frequencyUnit=true;
	boolean timeUnit=true;
	
	
	int timeLabel=500;
	int timeTickMark=1000;
	int freqLabel=5000;
	int freqTickMark=1000;
	float lineWeight=1;
	
	BasicStroke fs=new BasicStroke(lineWeight, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
	
	double stretchX=1;
	double stretchY=1;
	int compressYToFit=0;
	int compressXToFit=0;
	
	int displayMode=0;			//displayMode =0 means spectrogram; =1 means amp envelope; 2=pitch
	
	int location=-1;
	int bounds=0;
	int paintUnits=100;
	int soundposition=0;
	double ecorrect=Math.log(10);
	double stopRatio=0.5;
	Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

	//spectrogram variables:
	int nnx=(int)d.getWidth()-50;
	int nny=0;
	int tnx=0;
	int unx=0;
	int oldx=0;
	int oldy=0;
	int newx=0;
	int newy=0;
	int minFreq=5;
	int currentMinX=0;
	int currentMaxX=0;
	int archiveMinX=0;
	int archiveMaxX=0;
	int xspace=75;
	int xspace2=10;
	int yspace=50;
	int maxsl=0;
	double dx,dy, tdx, tdy;
	int nx, ny, tny;
	int minPressedX=0;
	int maxPressedX=0;
	int colpal[];
	double harm[][];
	int[][] pointList=null;
	int[][] archivePointList=null;
	int[] archiveLastElementsAdded=null;
	int[][] lastPointList=null;
	int[][] pList=null;
	int[][] oldPList=null;
	float [][] nout, envelope;
	double [] phase;
	GuidePanel gp;
	Song song;
	
	double upper=35;
	double lower=5;
		
	Point point=null;
	Cursor cur;
	MainPanel mp;
	double octstep=10;
	float [] trig1, trig2;
	//int [] places1,  places2,  places3,  places4;
	int bitTab[][];
	float window[];
	int counter=0;
	int counter2=0;
	
	AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f);
	AlphaComposite ac2 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
	
	
	//JComboBox deleteList=null;
	
	public SpectrPane(Song song, boolean editable, boolean start, MainPanel mp){
		this.mp=mp;
		relaunch(song, editable, start);
	}
	
	public SpectrPane(){
	
	}
	
	public void relaunch(Song song, boolean editable, boolean start){
		
		System.out.println("PROGRESS: Producing a spectrogram");
		
		this.song=song;
		this.editable=editable;
		this.setLayout(new BorderLayout());
		addMouseListener(this);
		addMouseMotionListener(this);
		createColourPalette();
		createCursor();
		if (!viewParameters[3]){
			for (int i=0; i<viewParameters.length; i++){
				viewParameters[i]=false;
			}
		}

		if (start){restart();}
	}
	
	void setGP(GuidePanel gp){
		this.gp=gp;
	}
	
	void makeGuidePanel(double minX, int gpmaxf){
		
		System.out.println("PROGRESS: Making a guide panel");
		
		//if (this.gp==null){this.gp=gp;}
		
		int dispMode=displayMode;
		displayMode=0;
		
		readyToUpdateGP=false;
		double timeStepArch=song.timeStep;
		double frameLengthArch=song.frameLength;
		int maxfArch=song.maxf;
		int dynEqualArch=song.dynEqual;
		double dynRangeArch=song.dynRange;
		int unxArch=unx;
		int tnxArch=tnx;
		int nyArch=ny;
		int currentMinXArch=currentMinX;
		int currentMaxXArch=currentMaxX;
		double stretchYArch=stretchY;
		double stretchXArch=stretchX;
		int locationArch=location;
		double echoComp=song.echoComp;
		float noiseRemArch=song.noiseRemoval;
		//song.frameLength=5;
		song.frameLength=256*1000/song.sampRate;
		song.maxf=gpmaxf;
		song.timeStep=song.overallLength/(d.width-60.0);
		//if (song.timeStep>song.frameLength){song.timeStep=song.frameLength;}
		//song.timeStep=10;
		song.dynEqual=500;
		song.dynRange=40;
		song.echoComp=0;
		song.noiseRemoval=0;
		song.setFFTParameters();		

		if (song.nx<minX){
			song.timeStep*=song.nx/minX;
			song.setFFTParameters();
		}
		
		BufferedImage imf=new BufferedImage(song.nx, song.ny, BufferedImage.TYPE_INT_ARGB);
		
		ny=song.ny;
		unx=song.nx;
		
		currentMinX=0;
		currentMaxX=song.nx;
		song.setFFTParameters2(song.nx);
		
		System.out.println("GUIDE PANEL: "+currentMinX+" "+currentMaxX);
		
		song.makeMyFFT(currentMinX, currentMaxX);
		System.out.println("MADE GUIDE PANEL");
		nout=song.out;
		updatePixelVals(imf);
		
		gp.makePanel(imf, song, this);

		song.frameLength=frameLengthArch;
		song.maxf=maxfArch;
		song.timeStep=timeStepArch;
		song.dynEqual=dynEqualArch;
		song.dynRange=dynRangeArch;
		song.echoComp=echoComp;
		song.noiseRemoval=noiseRemArch;
		song.setFFTParameters();
		stretchY=stretchYArch;
		stretchX=stretchXArch;
		location=locationArch;
		unx=unxArch;
		tnx=tnxArch;
		ny=nyArch;
		currentMaxX=currentMaxXArch;
		currentMinX=currentMinXArch;
		readyToUpdateGP=true;
		//gp.draw();	
		displayMode=dispMode;
	}
	
	
	public BufferedImage resizeImage(double ratio){
		
		paintOver=false;
		//System.out.println("II"+ratio);
		//d.width=((int)Math.round(d.getWidth()*ratio));
		//d.height=((int)Math.round());
		d.setSize((d.getWidth()-100)*ratio+100, d.getHeight()*ratio);
		
		double archiveStretchX=stretchX;
		double archiveStretchY=stretchY;
		int archivetickLabelFontSize=tickLabelFontSize;
		int archiveaxisLabelFontSize=axisLabelFontSize;
		int archivemajorTickMarkLength=majorTickMarkLength;
		int archiveminorTickMarkLength=minorTickMarkLength;
		int archiveYSpace=yspace;
		int archiveXSpace=xspace;
		int archiveXSpace2=xspace2;
		
		float archivelineWeight=lineWeight;
		
		stretchX=stretchX*ratio;
		stretchY=stretchY*ratio;
		
		tickLabelFontSize=(int)Math.round(tickLabelFontSize*ratio);
		axisLabelFontSize=(int)Math.round(axisLabelFontSize*ratio);

		majorTickMarkLength=(int)Math.round(majorTickMarkLength*ratio);
		minorTickMarkLength=(int)Math.round(minorTickMarkLength*ratio);
		
		yspace=(int)Math.round(yspace*ratio);
		xspace=(int)Math.round(xspace*ratio);
		xspace2=(int)Math.round(xspace2*ratio);
		
		lineWeight=(float)(lineWeight*ratio);
		
		resetFonts();
		restart();
		paintOver=true;
		
		d = Toolkit.getDefaultToolkit().getScreenSize();
		
		stretchX=archiveStretchX;
		stretchY=archiveStretchY;
		tickLabelFontSize=archivetickLabelFontSize;
		axisLabelFontSize=archiveaxisLabelFontSize;
		majorTickMarkLength=archivemajorTickMarkLength;
		minorTickMarkLength=archiveminorTickMarkLength;
		lineWeight=archivelineWeight;
		yspace=archiveYSpace;
		xspace=archiveXSpace;
		xspace2=archiveXSpace2;
		
		BufferedImage im2=imf.getSubimage(0, 0, imf.getWidth(), imf.getHeight());
		
		resetFonts();
		restart();
		return im2;
	}
	
	
	void restart(){
		
		System.out.println("PROGRESS: Setting the bounds");
		
		nnx=(int)d.getWidth()-100;
		dx=song.dx;
		tdx=dx/stretchX;
		nx=song.nx;
		tnx=(int)Math.round(stretchX*nx);
		ny=song.ny;
		tny=(int)Math.round(ny*stretchY);
		nny=tny+yspace*3;
		if (compressYToFit>0){
			tny=ny;
			nny=tny+yspace*3;
			if (nny>compressYToFit){
				nny=compressYToFit;
				tny=nny-yspace*3;
				stretchY=tny/(ny+0.0);
			}
		}
		dy=song.dy;
		tdy=dy/stretchY;
		if (compressXToFit>0){
			nnx=compressXToFit;
			if (nx>nnx-xspace-xspace2){
				tnx=nnx-xspace-xspace2;
				stretchX=tnx/(nx+0.0);
			}
		}
		if (tnx>nnx-xspace-xspace2){tnx=nnx-xspace-xspace2;}
			
		unx=(int)Math.round(tnx/stretchX);
		this.setPreferredSize(new Dimension(nnx, nny));
		this.revalidate();
		imf=new BufferedImage(nnx, nny, BufferedImage.TYPE_INT_ARGB);
		im=new BufferedImage(unx, song.ny, BufferedImage.TYPE_INT_ARGB);
		//paintFrame();
		pList=new int[tnx][2];
		oldPList=new int[tnx][2];
		//if (editable){fixElements();}
		if (location==-1){
			location=0;
		}
		
		relocate(location);
		customCursor=System.getProperty("os.name").endsWith("Mac OS X");
	}

	void relocate(int start){
		System.out.println("PROGRESS: Setting the position");
		location=start;
		currentMinX=(int)Math.round(start-(0.5*unx));
		if (currentMinX<0){currentMinX=0;}
		currentMaxX=currentMinX+unx;
		System.out.println("LOCS: "+currentMinX+" "+currentMaxX+" "+unx+" "+start);
		if (currentMaxX>nx){
			currentMaxX=nx;
			currentMinX=currentMaxX-unx;
			if (currentMinX<0){currentMinX=0;}
		}
		System.out.println("LOCS: "+currentMinX+" "+currentMaxX+" "+unx+" "+start+" "+nx);
		nout=null;
		System.out.println("PROGRESS: Running the FFT");
		song.setFFTParameters2(unx);
		song.makeMyFFT(currentMinX, currentMaxX);
		song.makeAmplitude(currentMinX, currentMaxX, tnx);
		nout=song.out;
		makePhase();
		envelope=song.envelope;
		if (displayMode!=2){
			updatePixelVals(im);
		}
		else {
			updatePixelValsPitchThreaded(im);
		}
		paintFound();
		if(readyToUpdateGP){gp.updateBoundaries();}
	}
	
	void relocate(int start, int end){
		System.out.println("PROGRESS: Setting the position with stretch "+tnx);
		location=(end+start)/2;
		double stretch=100*(end-start)/(tnx+0.0);
		if (mp!=null){
			mp.updateable=false;
			mp.started=false;
			mp.timeZoom.setValue(new Double(stretch));
			int fpt=(int)Math.round((Math.sqrt(stretch*0.01)-0.5)*100);
			if (fpt<0){fpt=0;}
			if (fpt>100){fpt=100;}
			mp.timeZoomSL.setValue(fpt);
			//relocate(location);
			mp.started=true;
			mp.updateable=true;
		}
		stretchX=tnx/(end-start-0.0);
		System.out.println(stretchX+" "+location+" "+tnx+" "+start+" "+end);
		restart();
	}
	
	void fixElements(){
		for (int s=0; s<song.eleList.size(); s++){
			Element ele=(Element)song.eleList.get(s);
			ele.signal2=new int[ele.length][];
			int a=ele.length;
			if ((ele.dy!=tdy)||(ele.maxf!=song.maxf)){
				System.out.println("FIXING!! "+" "+ele.dy+" "+tdy+" "+ele.maxf+" "+song.maxf);
				double fr=0;
				for (int i=0; i<a; i++){
					ele.signal2[i]=new int[ele.signal[i].length];
					for (int j=1; j<ele.signal[i].length; j++){
						fr=ele.maxf-ele.dy*(ele.signal[i][j]-1);
						ele.signal2[i][j]=(int)Math.round(ny-(fr/dy)-1);
					}
					ele.signal2[i][0]=ele.signal[i][0];
				}
			}
			else{
				for (int i=0; i<a; i++){
					ele.signal2[i]=new int[ele.signal[i].length];
					for (int j=0; j<ele.signal[i].length; j++){
						ele.signal2[i][j]=ele.signal[i][j];
					}
				}
			}
			if (ele.timeStep!=dx){
				double changer=ele.timeStep/dx;
				int b=(int)Math.round(ele.signal2.length*changer);
				int[][]ele3=new int[b][];
				double[][]mea2=new double[b+5][];
				
				int startx=(int)Math.round(ele.signal2[0][0]*changer);
				for (int i=0; i<5; i++){
					mea2[i]=new double[ele.measurements[i].length];
					for (int j=0; j<mea2[i].length; j++){
						mea2[i][j]=ele.measurements[i][j];
					}
				}
				for (int i=0; i<b; i++){
					int c=(int)Math.round(i/changer);
					
					if (c>=ele.length){
						c=ele.length-1;
					}
					ele3[i]=new int[ele.signal2[c].length];
					ele3[i][0]=startx+i;
					for (int j=1; j<ele.signal2[c].length; j++){
						ele3[i][j]=ele.signal2[c][j];
					}
					int ii=i+5;
					int cc=c+5;
					mea2[ii]=new double[ele.measurements[cc].length];
					for (int j=0; j<mea2[ii].length; j++){
						mea2[ii][j]=ele.measurements[cc][j];
					}
				}
				ele.signal2=new int[b][];
				ele.measurements2=new double[b][];
				for (int i=0; i<b; i++){
					ele.signal2[i]=new int[ele3[i].length];
					for (int j=0; j<ele.signal2[i].length; j++){
						ele.signal2[i][j]=ele3[i][j];
					}
					ele.measurements2[i]=new double[mea2[i].length];
					for (int j=0; j<ele.measurements2[i].length; j++){
						ele.measurements2[i][j]=mea2[i][j];
					}
				}
			}
			else{
				int b=ele.signal.length;
				ele.signal2=new int[b][];
				ele.measurements2=new double[b][];
				for (int i=0; i<b; i++){
					ele.signal2[i]=new int[ele.signal[i].length];
					for (int j=0; j<ele.signal2[i].length; j++){
						ele.signal2[i][j]=ele.signal[i][j];
					}
					ele.measurements2[i]=new double[ele.measurements[i].length];
					for (int j=0; j<ele.measurements2[i].length; j++){
						ele.measurements2[i][j]=ele.measurements[i][j];
					}
				}
			}
		}
	}
	
	void updatePixelVals(BufferedImage img){
		int i,j,h;
		float sh;
		float c=(float)(1/(song.dynMax*song.dynRange));
		//System.out.println("C: "+c+" "+song.dynMax+" "+song.dynRange);
		for (j=0; j<ny; j++){
			h=ny-j-1;
			for (i=0; i<unx; i++){
				sh=1-(nout[j][i]*c);
				if (sh<0){sh=0;}
				if (sh>1){sh=1;}
				img.setRGB(i, h, colpal[(int)(sh*255)]);
			}
		}
	}
	
	
class PitchCalculator extends Thread{
		
		int start, end;
		int[][] out;
		double subsup=0.25;
		BufferedImage img;
		
		public PitchCalculator(int start, int end, BufferedImage img){
			this.start=start;
			this.end=end;	
			this.img=img;
			out=new int[end-start][ny];
		}

		public void run(){
			int oct=(int)Math.round(octstep);
			double[] spectrum=new double[ny];
			double[] hscor=new double[ny];
			double maxHscor;
			double subSuppression=song.fundAdjust;
			for (int i=start; i<end; i++){
				double peakAmp=-1000;
				for (int j=0; j<ny; j++){
					spectrum[j]=nout[j][i];
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
						
						hscor[j]*=peakAmp/(song.dynRange);					
					}
				}
				else{
					for (int j=0; j<ny; j++){
						hscor[j]=0;
					}
				}
				int ii=i-start;
				for (int j=minFreq; j<ny; j++){
					double p=1-hscor[j];
					int q=(int)(p*255);
					if (q>255){q=255;}
					if (q<0){q=0;}
					out[ii][j]=q;
				}
			}
		}
	}

	void updatePixelValsPitchThreaded(BufferedImage img){
		int ncores=Runtime.getRuntime().availableProcessors();
		
		PitchCalculator ct[]=new PitchCalculator[ncores];
		
		int[] starts=new int[ncores];
		int[] stops=new int[ncores];
		for (int i=0; i<ncores; i++){
			starts[i]=i*(unx/ncores);
			stops[i]=(i+1)*(unx/ncores);
		}
		stops[ncores-1]=unx;
		System.out.println("CORES USED: "+ncores);
		
		for (int i=0; i<ncores; i++){
			ct[i]=new PitchCalculator(starts[i], stops[i], img);
			ct[i].setPriority(Thread.MIN_PRIORITY);
			ct[i].start();
		}
		
		try{
			for (int cores=0; cores<ncores; cores++){
				ct[cores].join();
			}
			for (int cores=0; cores<ncores; cores++){
				System.out.println(cores);
				for (int i=ct[cores].start; i<ct[cores].end; i++){
					int ii=i-ct[cores].start;
					for (int j=0; j<ny; j++){
						int h=ny-j-1;
						int sh=ct[cores].out[ii][j];

						img.setRGB(i, h, colpal[sh]);
					}
				}
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
	}
	
	void updatePixelValsPitch(BufferedImage img){
		System.out.println("HERE");
		double[] spectrum=new double[ny];
		
		double[]hscor=new double[ny];

		
		double subSuppression=song.fundAdjust;
		double maxHscor=-1000;
		
		for (int i=0; i<unx; i++){
			double peakAmp=-1000;
			for (int j=0; j<ny; j++){
				
				spectrum[j]=nout[j][i];
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
			for (int j=0; j<ny; j++){
				double sumxy=0;
				double sumxx=0;
				double sumyy=0;
				for (int k=0; k<ny; k++){
					double q=phase[count];
					sumxy+=q*spectrum[k];
					//sumxx+=q*q;
					//sumyy+=spectrum[k]*spectrum[k];
					count++;
				}
				//hscor[j]=sumxy/Math.sqrt(sumxx*sumyy);
				hscor[j]=sumxy;
				//hscor[j]/=logTransform[j];
			}
			maxHscor=-1000000;

			for (int j=0; j<ny; j++){
				//hscor[j]*=1+(j-(ny/2))*subSuppression;
				if (hscor[j]>maxHscor){
					maxHscor=hscor[j];
				}
			}
			for (int j=0; j<ny; j++){
				hscor[j]=(hscor[j]/maxHscor);
				//if (hscor[j]<0.999){hscor[j]=0;}
			}
			
			if (maxHscor>0){
				for (int j=minFreq; j<ny; j++){
					//hscor[j]=1-(hscor[j]/maxHscor);
					//hscor[j]*=hscor[j];
					hscor[j]=Math.pow(hscor[j], 2);
					hscor[j]*=peakAmp/(song.dynRange);
					//hscor[j]=1-hscor[j];
					
				}
			}
			else{
				for (int j=0; j<ny; j++){
					hscor[j]=0;
				}
			}
			for (int j=0; j<minFreq; j++){
				hscor[j]=0;
			}
			
			for (int j=0; j<ny; j++){
				int h=ny-j-1;
				float sh=(float)(1-hscor[j]);
				if (sh<0){sh=0;}
				if (sh>1){sh=1;}
				img.setRGB(i, h, colpal[(int)(sh*255)]);
			}	
		}
	}
	
	void gaussianBlur(double[][] data){
		double vari=2;
		float[][] kernel=new float[5][5];
		for (int i=0; i<=4; i++){
			int ii=i-2;
			for (int j=0; j<=4; j++){
				int jj=j-2;
				kernel[i][j]=(float)Math.exp(-1*ii*ii/vari);
				kernel[i][j]*=(float)Math.exp(-1*jj*jj/vari);
			}
		}
		
		int dim1=data.length; 
		int dim2=data[0].length;
	
		double[][]out=new double[dim1][dim2];
		double tot=0;
		int g,h,i,j,gg,hh;
		double[] av=new double[dim1];
		for (i=0; i<dim1; i++){
			for (j=0; j<dim2; j++){
				tot=0;
				for (g=0; g<=4; g++){
					gg=i+g-2;
					if ((gg>=0)&&(gg<dim1)){
						for (h=0; h<=4; h++){
							hh=j+h-2;
							if((hh>=0)&&(hh<dim2)){
								out[i][j]+=data[gg][hh]*kernel[g][h];
								tot+=kernel[g][h];
							}
						}
					}
				}
				out[i][j]/=tot;
				av[i]+=out[i][j];
			}
			
		}
		for (i=0; i<dim1; i++){
			av[i]/=dim2+0.0;
		}
		for (i=0; i<dim1; i++){
			for (j=0; j<dim2; j++){
				//data[i][j]-=out[i][j]-av[i];
				
				data[i][j]=out[i][j];
				
			}
		}
		out=null;
		kernel=null;
	}
	
	void makePhase(){
		
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
	
	
	void updatePixelValsPitchCepstrum(){
		
		double[]hscor=new double[ny];
		
		int subband=-1;
		int superband=1;
		
		double[][] matrix=new double[unx][ny];
		
		int ny2=(int)Math.pow(2, Math.ceil(Math.log(ny)/Math.log(2)));
		double[] spectrum=new double[ny2];
		
		for (int i=0; i<unx; i++){
			double peakAmp=-1000;
			for (int j=0; j<ny; j++){
				double countband=0;
				spectrum[j]=0;
				for (int k=subband; k<=superband; k++){
					int kk=k+i;
					if ((kk>=0)&&(kk<unx)){
						spectrum[j]+=nout[j][kk];
						countband++;
					}
				}
				spectrum[j]/=countband;
				hscor[j]=0;
				if (spectrum[j]>peakAmp){
					peakAmp=spectrum[j];
				}
			}
						
			double minAmp=peakAmp-song.dynRange;

			for (int j=0; j<ny; j++){
				spectrum[j]-=minAmp;
			}
			
			double max=-10000;
			double min=100000;
			for (int j=0; j<ny; j++){
				if (hscor[j]>max){
					max=hscor[j];
				}
				if (hscor[j]<min){
					min=hscor[j];
				}
			}

						
			for (int j=minFreq; j<ny; j++){
				hscor[j]=(hscor[j]-min)/(max-min);
				//hscor[j]*=peakAmp/(song.dynRange);
			}
			System.arraycopy(hscor, 0, matrix[i], 0, ny);
		}
		
		for (int i=0; i<unx; i++){
			for (int j=0; j<ny; j++){
				int h=ny-j-1;
				float sh=(float)(1-matrix[i][j]);
				if (sh<0){sh=0;}
				if (sh>1){sh=1;}
				im.setRGB(i, h, colpal[(int)(sh*255)]);
			}
		}
	}
	
	
	private void createColourPalette(){
		colpal=new int[256];
		for (int i=0; i<256; i++){
			Color c=new Color(i, i, i);
			colpal[i]=c.getRGB();
		}
	}

	
	private int[] createModel(){
		int time_samples=(int)Math.round(750/song.dx);
		int[] model=new int[time_samples];
		
		double freqch=9300/(time_samples+0.0);
		double rows_per_ms=freqch/song.dy;
		for (int i=0; i<time_samples; i++){
			model[i]=(int)Math.round(rows_per_ms*i);
		}
		return model;
	}
	
		
	private void createCursor(){
		BufferedImage cu=new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB);
		Graphics h=cu.getGraphics();
		h.setColor(Color.WHITE);
		h.fillRect(0,0,3,3);
		h.setColor(Color.BLACK);
		h.drawLine(0,1,3,1);
		h.drawLine(1,0,1,3);
		h.dispose();
		Point ho=new Point(1,1);
		Toolkit tk=getToolkit();
		
		cur=tk.createCustomCursor(cu, ho, "cure");
	}
	
	void merge(int p){
		Element ele1=(Element)song.eleList.get(p);
		Element ele2=(Element)song.eleList.get(p+1);
		
		
		int diff=ele2.signal[0][0]-ele1.signal[ele1.signal.length-1][0];
				
		if (diff>0){
			song.eleList.remove(p);
			song.eleList.remove(p);
			Element ele=new Element(ele1, ele2);
			double q=ele.begintime+0.5*(ele.length);
			int count=0;
			for (int j=0; j<song.eleList.size(); j++){
				Element ele3=(Element)song.eleList.get(j);
				double p2=ele3.begintime+0.5*(ele3.length);
				if (p2<q){count++;}
				else{j=song.eleList.size();}
			}
			song.eleList.add(count, ele);
		}
		
		
		else{
			for (int i=0; i<ele1.signal.length; i++){
				for (int j=1; j<ele1.signal[i].length; j++){
					ele1.signal[i][j]=ny-1-ele1.signal[i][j];
				}
				ele1.signal[i][0]-=currentMinX;
			}
			for (int i=0; i<ele2.signal.length; i++){
				for (int j=1; j<ele2.signal[i].length; j++){
					ele2.signal[i][j]=ny-1-ele2.signal[i][j];
				}
				ele2.signal[i][0]-=currentMinX;
			}

			if ((ele1.signal[0][0]>0)&&(ele2.signal[0][0]>0)&&(ele1.signal[ele1.signal.length-1][0]<nout[0].length)&&(ele2.signal[ele2.signal.length-1][0]<nout[0].length)){
				song.eleList.remove(p);
				song.eleList.remove(p);
			
					
				int tot=ele1.length+ele2.length;
				
				for (int i=0; i<ele1.signal.length; i++){
					for (int j=0; j<ele2.signal.length; j++){
						if (ele1.signal[i][0]==ele2.signal[j][0]){
							tot--;
						}
					}
				}
				
				
				int[][] signal=new int[tot][];
				for (int i=0; i<ele1.signal.length; i++){
					signal[i]=new int[ele1.signal[i].length];
					for (int j=0; j<ele1.signal[i].length; j++){
						signal[i][j]=ele1.signal[i][j];
					}
				}
				
				int ii=ele1.signal.length;
				for (int i=0; i<ele2.signal.length; i++){
					int loc=-1;
					for (int j=0; j<ele1.signal.length; j++){
						if (ele1.signal[j][0]==ele2.signal[i][0]){
							loc=j;
							j=ele1.signal.length;
						}
					}
					
					if (loc==-1){
						signal[ii]=new int[ele2.signal[i].length];
						for (int j=0; j<ele2.signal[i].length; j++){
							signal[ii][j]=ele2.signal[i][j];
						}
						ii++;
					}

			
					else{
						boolean[] board=new boolean[ny];
						for (int j=1; j<ele1.signal[loc].length; j+=2){
							for (int k=ele1.signal[loc][j]; k<ele1.signal[loc][j+1]; k++){
								board[k]=true;
							}
						}
						for (int j=1; j<ele2.signal[i].length; j+=2){
							for (int k=ele2.signal[i][j]; k<ele2.signal[i][j+1]; k++){
								board[k]=true;
							}
						}
						boolean c=false;
						int count=0;
						for (int j=0; j<ny; j++){
							if (board[j]!=c){
								count++;
								c=!c;
							}
						}
						signal[loc]=new int[count+1];
						signal[loc][0]=ele1.signal[loc][0];
						c=false;
						count=1;
						for (int j=0; j<ny; j++){
							if (board[j]!=c){
								signal[loc][count]=j;
								count++;
								c=!c;
							}
						}
					}
				}
				LinkedList<int[][]> holder=new LinkedList<int[][]>();
				holder.add(signal);
				measureAndAddElements(holder);
			}
		}
		paintFound();
		gp.draw();
	}
	
	void selectAll(){
		pointList=new int[unx][2];
		for (int i=1; i<unx-1; i++){
			pointList[i][0]=1;
			pointList[i][1]=ny-2;
		}
		updateElement();
	}
	
	void reDoLastSelection(){
		if (lastPointList!=null){
			pointList=new int[lastPointList.length][];
			for (int i=0; i<lastPointList.length; i++){
				pointList[i]=new int[lastPointList[i].length];
				System.arraycopy(lastPointList[i], 0, pointList[i], 0, pointList[i].length);
			}
			LinkedList<int[][]> signals=getSignal();
			segment(signals, false);
			measureAndAddElements(signals);
			paintFound();
			gp.draw();
		}
	}

	
	
	private void updateSyllable(){
		if (pointList!=null){
			int minx=0;
			while (pointList[minx][0]==0){minx++;}
			int maxx=unx-2;								//this bit identifies the start and end of the element
			while (pointList[maxx][0]==0){maxx--;}
			maxx++;
			int[] s={(int)Math.round(dx*(minx+currentMinX)), (int)Math.round(dx*(maxx+currentMinX))};
			//System.out.println(s[0]+" "+s[1]+" "+minx+" "+maxx);
			int p=s[0]+s[1];
			int count=0;
			for (int j=0; j<song.syllList.size(); j++){
				int[] s2=(int[])song.syllList.get(j);
				int p2=s2[0]+s2[1];
				if (p2<p){count++;}
				else{j=song.syllList.size();}
			}
			song.syllList.add(count, s);
			
			//song.syllList.add(s);
			mp.updateSyllableLists();
			mp.cloneLists();
			pointList=null;
			pList=new int[tnx][2];
			oldPList=new int[tnx][2];
			paintFound();
			gp.draw();
		}
	}
	
	void modelSearch(){
		int[] model=createModel();
		int p=model.length;
		float max=0f;
		int loc=0;
		float tot=0f;
		int xl=nout[0].length;
		for (int i=0; i<xl-p; i++){
			tot=0f;
			for (int j=0; j<p; j++){
				int k=model[j];
				if (k<0){k=0;}
				if (k<nout.length){
					if (nout[k][j+1]>0){
						tot+=nout[k][j+i];
					}
				}
			}
			if (tot>max){
				max=tot;
				loc=i;
			}
		}
		int[][] data=new int[p][3];
		int[][] data2=new int[p][2];
		for (int i=0; i<p; i++){
			int loc2=loc+i;
			data[i][0]=loc2;
			data2[i][0]=loc2;
			data2[i][1]=model[i];
			if (i>9){
				data[i][1]=model[i-10];
			}
			else{
				data[i][1]=model[0];
			}
			if (data[i][1]<=0){data[i][1]=1;}
			if (data[i][1]>=ny-1){data[i][1]=ny-2;}
			if (i<p-10){
				data[i][2]=model[i+10];
			}
			else{
				data[i][2]=model[p-1];
			}
			if (data[i][2]<=1){data[i][2]=2;}
			if (data[i][2]>=ny){data[i][2]=ny-1;}
			//System.out.println(data[i][0]+" "+data[i][1]+" "+data[i][2]+" "+model[i]);
		}
		LinkedList<int[][]> signals=new LinkedList<int[][]>();
		signals.add(data);
		LinkedList<int[][]> signals2=new LinkedList<int[][]>();
		signals2.add(data2);
		measureAndAddElements(signals);
		
		Element ele=(Element)song.eleList.get(song.eleList.size()-1);
		LinkedList<double[]> reverbList=new LinkedList<double[]>();
		measureReverberationRegression(signals2, reverbList);
		double[] reverb=reverbList.get(0);
		
		for (int i=0; i<ele.signal.length; i++){
			ele.measurements[i+5][0]=model[i]*song.dy;
			ele.measurements[i+5][12]=reverb[i];
		}
				
		mp.updateElementLists();
		paintFound();
		gp.draw();
	}
		
		
	public int reEstimateElements(){
	
	
		for (int i=archiveLastElementsAdded.length-1; i>=0; i--){
			song.eleList.remove(archiveLastElementsAdded[i]);
		}
	
		pointList=archivePointList;
		
		if ((currentMinX!=archiveMinX)||(currentMaxX!=archiveMaxX)){
			relocate(archiveMinX, archiveMaxX);	
		}
		updateElement();
		
		return 1;
	}
		
	private void updateElement(){
		if (pointList!=null){
			LinkedList<int[][]> signals=getSignal();
			if (signals.size()>0){
				archiveMinX=currentMinX;
				archiveMaxX=currentMaxX;
				archivePointList=new int[lastPointList.length][];
				for (int i=0; i<lastPointList.length; i++){
					archivePointList[i]=new int[lastPointList[i].length];
					System.arraycopy(lastPointList[i], 0, archivePointList[i], 0, lastPointList[i].length);
				}
			}
			//segmentMDL(signals);
			if (automaticMerge){segment(signals, false);}
			checkMinimumLengths(signals);
			measureAndAddElements(signals);
			
			mp.updateElementLists();
			mp.cloneLists();
			//mp.redo.setEnabled(true);
			paintFound();
			gp.draw();
		}
	}
	
	public void remeasureAll(){
		
		int a=currentMinX;
		int b=currentMaxX;
		float c=(float)(1/song.dynMax);
		relocate(0, nx);
		LinkedList<int[][]> tList=new LinkedList<int[][]>();
		
		for (int i=0; i<song.eleList.size(); i++){
			Element ele=(Element)song.eleList.get(i);
			int[][] signal=ele.signal;
			int eleLength=signal.length;
			for (int j=0; j<eleLength; j++){
				for (int k=1; k<signal[j].length; k++){
					signal[j][k]=ny-signal[j][k]-1;
				}
			}
			int[]temp=new int [ny];
			
			int[][] signal2=new int[eleLength][];
			
			for (int j=0; j<eleLength; j++){
				temp=new int [ny];
				int chunkNum=signal[j].length;
				int time=signal[j][0];
				for (int k=1; k<chunkNum; k+=2){
					for (int aa=signal[j][k]; aa<signal[j][k+1]; aa++){						
						if (nout[aa][time]*c>song.lowerLoop){
							temp[aa]=1;
						}
					}
				}
				int count=0;
				for (int k=0; k<ny-1; k++){
					if ((temp[k]==0)&&(temp[k+1]==1)){count++;}
				}
				signal2[j]=new int[count*2+1];
				//System.out.println(time+" "+signal2[j].length+" "+" "+signal[j].length);
				signal2[j][0]=time;
				int c2=1;
				for (int k=0; k<ny-1; k++){
					if ((temp[k]==0)&&(temp[k+1]==1)){
						signal2[j][c2]=k;
						c2++;
					}
					if ((temp[k]==1)&&(temp[k+1]==0)){
						signal2[j][c2]=k;
						c2++;
					}
				}
				if (c2<signal2[j].length){signal2[j][c2]=ny-1;}	
			}
			
			for (int j=0; j<eleLength; j++){
				if (signal2[j].length>1){
					int jj=j;
					while ((jj<signal2.length)&&(signal2[jj].length>1)){
						jj++;
					}
					int[][] signal3=new int[jj-j][];
					for (int k=j; k<jj; k++){
						int kk=k-j;
						signal3[kk]=new int[signal2[k].length];
						signal3[kk][0]=signal2[k][0];
						for (int l=1; l<signal2[k].length; l++){
							signal3[kk][l]=signal2[k][l];
						}
					}
					tList.add(signal3);
					j=jj;
					
				}
			}
			
			/*
			int eleLength=signal.length;
			for (int j=0; j<eleLength; j++){
				for (int k=1; k<signal[j].length; k++){
					signal[j][k]=ny-signal[j][k]-1;
				}
			}
			tList.add(signal);
			*/
		}	
		song.eleList.clear();
		measureAndAddElements(tList);
		
		relocate(a,b);
	
	}

	public void remeasureAll(int temp){
		
		int a=currentMinX;
		int b=currentMaxX;
		
		relocate(0, nx);
		LinkedList<int[][]> tList=new LinkedList<int[][]>();
		
		for (int i=0; i<song.eleList.size(); i++){
			Element ele=(Element)song.eleList.get(i);
			int[][] signal=ele.signal;
			
			for (int j=0; j<signal.length; j++){
				
				
				
			}
			
			
			int eleLength=signal.length;
			for (int j=0; j<eleLength; j++){
				for (int k=1; k<signal[j].length; k++){
					signal[j][k]=ny-signal[j][k]-1;
				}
			}
			tList.add(signal);
		}	
		song.eleList.clear();
		measureAndAddElements(tList);
		
		relocate(a,b);

	}
	
	private void segment(LinkedList<int[][]> signals, boolean force){
		
		//This method checks through the discovered elements, and joins together those elements that are separated by less than minGap
	
		int i, j, k, jj, loc;
		for (i=0; i<signals.size()-1; i++){
			int[][]t1=signals.get(i);			//t1 is the first signal space
			int[][]t2=signals.get(i+1);		//t2 is the second signal space
			
			if ((t2[0][0]-t1[t1.length-1][0]<song.minGap/song.timeStep)||(force)){			//if the two signals are closer than minGap to each other... then we have to join them up!
				
				
				int newLength=t2[t2.length-1][0]-t1[0][0]+1;
				
				int [][]u=new int [newLength][];	//u is the new signal space for our new joined up element
				
				for (j=0; j<t1.length; j++){		//this is the easy bit: copy the first signal into the beginning of the new vectors (u, u2)
					u[j]=new int[t1[j].length];
					for (k=0; k<u[j].length; k++){u[j][k]=t1[j][k];}
				}
				
				
				jj=t2[0][0]-t1[0][0];
				for (j=0; j<t2.length; j++){		//this is also easy: copy the second signal into the end of the new vectors
					u[jj]=new int[t2[j].length];
					for (k=0; k<u[jj].length; k++){u[jj][k]=t2[j][k];}
					jj++;
				}
				
				jj=t2[0][0]-t1[0][0];
				int bottom1=t1[t1.length-1][1];
				int bottom2=t2[0][1];
				int top1=t1[t1.length-1][t1[t1.length-1].length-1];
				int top2=t2[0][t2[0].length-1];
				int st, sb;
				double place;
				double diff=jj-t1.length;
				float max2;
				
				for (j=t1.length; j<jj; j++){			//now comes the difficult bit: filling in the gap between the two signals (if there is one)
					u[j]=new int[3];			//make a new vector: we assume there's only one band of signal present!
					u[j][0]=t1[0][0]+j;					//fill in the time value (easy!)
					
					
					place=(j-t1.length)/diff;
					st=(int)Math.round(place*top1+(1-place)*top2);
					sb=(int)Math.round(place*bottom1+(1-place)*bottom2);
					
					max2=-1000000;
					loc=0;
					for (k=sb; k<=st; k++){
						if (nout[k][u[j][0]]>max2){
							max2=nout[k][u[j][0]];
							loc=k;
						}
					}
					
					u[j][1]=sb;
					for (k=loc; k>=sb; k--){
						if (nout[k][u[j][0]]<max2*0.25){
							u[j][1]=k;
							k=sb-1;
						}
					}
					
					u[j][2]=st;
					for (k=loc; k<=st; k++){
						if (nout[k][u[j][0]]<max2*0.25){
							u[j][2]=k;
							k=st+1;
						}
					}
					if (u[j][2]==u[j][1]){
						u[j][2]++;
						if (u[j][2]>=nout.length){
							u[j][2]=nout.length-1;
						}
						u[j][1]--;
						if (u[j][1]<0){
							u[j][1]=0;
						}
					}					
				}
				signals.remove(i);
				signals.remove(i);
				signals.add(i, u);
				i--;
			}
		}
	}
	
	
	public void checkMinimumLengths(LinkedList<int[][]> signals){
		
		for (int i=0; i<signals.size(); i++){
			int[][] s=signals.get(i);
			if (s.length<song.minLength/song.timeStep){
				signals.remove(i);
				i--;
			}
		}
		
	}
	
	public void updateEleStartTimes(){
		
		for (int i=0; i<song.eleList.size(); i++){
			Element ele=(Element)song.eleList.get(i);
			ele.begintime=(int)Math.round(ele.begintime*0.998);
			
			for (int j=0; j<ele.signal.length; j++){
				ele.signal[j][0]=(int)Math.round(ele.signal[j][0]*0.998);
			}
			
		}
	}
	
	public void updateHarmonicity(){
		LinkedList<Element> eleList=new LinkedList<Element>();
		for (int i=0; i<song.eleList.size(); i++){
			//System.out.println("Q "+i);
			Element ele=(Element)song.eleList.get(i);
			
			int[][] signal=ele.signal;
			int eleLength=signal.length;
			for (int j=0; j<eleLength; j++){
				for (int k=1; k<signal[j].length; k++){
					signal[j][k]=ny-signal[j][k]-1;
				}
			}
			LinkedList<int[][]> slist=new LinkedList<int[][]>();
			slist.add(signal);
			double[][] freq=new double[ele.measurements.length-5][4];
			for (int j=0; j<freq.length; j++){
				for (int k=0; k<4; k++){
					freq[j][k]=ele.measurements[j+5][k]/ele.dy;
				}
			}
			LinkedList<double[][]> freqList=new LinkedList<double[][]>();
			freqList.add(freq);
			
			LinkedList<double[]> harmList=new LinkedList<double[]>();
			try{
				measureHarmonicity(slist, freqList, harmList);
			}
			catch(Error e){System.out.println(e);}
			
			double[] harm=harmList.get(0);
			
			double[][] newm=new double[freq.length][15];
			
			for (int j=0; j<freq.length; j++){
				for (int k=0; k<15; k++){
					newm[j][k]=ele.measurements[j+5][k];
				}
				
				newm[j][8]=harm[j];
			}
			
			for (int j=0; j<eleLength; j++){
				for (int k=1; k<signal[j].length; k++){
					signal[j][k]=ny-signal[j][k]-1;
				}
			}
			
			ele.measurements=newm;
			ele.calculateStatistics();
			eleList.add(ele);
		}
		System.out.println("done");
		song.eleList=eleList;

	}
		
	
	public void updateTrillMeasures(){
		//System.out.print(song.eleList.size()+" P ");
		LinkedList<Element> eleList=new LinkedList<Element>();
		for (int i=0; i<song.eleList.size(); i++){
			//System.out.println("Q "+i);
			Element ele=(Element)song.eleList.get(i);
			double[][] freq=new double[ele.measurements.length-5][4];
			for (int j=0; j<freq.length; j++){
				for (int k=0; k<4; k++){
					freq[j][k]=ele.measurements[j+5][k]/ele.dy;
				}
			}
			LinkedList<double[][]> freqList=new LinkedList<double[][]>();
			freqList.add(freq);
			
			LinkedList<double[][]> trillList=new LinkedList<double[][]>();
			try{
				measureTrills(freqList, trillList);
			}
			catch(Error e){System.out.println(e);}
			
			double[][] trill=trillList.get(0);
			
			double[][] newm=new double[freq.length][15];
			
			for (int j=0; j<freq.length; j++){
				for (int k=0; k<13; k++){
					newm[j][k]=ele.measurements[j+5][k];
				}
				for (int k=0; k<2; k++){
					newm[j][13+k]=trill[j][k];
				}
			}
			ele.measurements=newm;
			ele.calculateStatistics();
			eleList.add(ele);
		}
		System.out.println("done");
		song.eleList=eleList;
	}
	
	public void updateChangeMeasures(){
		//System.out.print(song.eleList.size()+" P ");
		LinkedList<Element> eleList=new LinkedList<Element>();
		for (int i=0; i<song.eleList.size(); i++){
			//System.out.println("Q "+i);
			Element ele=(Element)song.eleList.get(i);
			int[][] signal=ele.signal;
			int eleLength=signal.length;
			for (int j=0; j<eleLength; j++){
				for (int k=1; k<signal[j].length; k++){
					signal[j][k]=ny-signal[j][k]-1;
				}
			}
			
			LinkedList<int[][]> tList=new LinkedList<int[][]>();
			tList.add(signal);
			//System.out.println("STARTS: "+signal[0][0]+" "+nout.length+" "+nout[0].length);
			
			double[][] freq=new double[ele.measurements.length-5][4];
			for (int j=0; j<freq.length; j++){
				for (int k=0; k<4; k++){
					freq[j][k]=ele.measurements[j+5][k]/ele.dy;
				}
			}
			LinkedList<double[][]> freqList=new LinkedList<double[][]>();
			freqList.add(freq);
			
			LinkedList<double[][]> freqChangeList=new LinkedList<double[][]>();
			try{
				//measureFrequencyChange2(freqList, freqChangeList);
				measureFrequencyChange4(tList, freqList, freqChangeList);
			}
			catch(Error e){System.out.println(e);}
			
			double[][] freqChange=freqChangeList.get(0);
			
			double[][] newm=new double[freq.length][15];
			
			for (int j=0; j<freq.length; j++){
				for (int k=0; k<15; k++){
					newm[j][k]=ele.measurements[j+5][k];
				}
				for (int k=0; k<4; k++){
					//System.out.print(freqChange[j][k]+" ");
					newm[j][4+k]=freqChange[j][k];
				}
				//System.out.println();
			}
			
			for (int j=0; j<eleLength; j++){
				for (int k=1; k<signal[j].length; k++){
					signal[j][k]=ny-signal[j][k]-1;
				}
			}
			
			ele.measurements=newm;
			ele.calculateStatistics();
			eleList.add(ele);
		}
		System.out.println("done");
		song.eleList=eleList;
	}
	
	public void updateWienerEntropy(){
		//System.out.print(song.eleList.size()+" P ");
		LinkedList<Element> eleList=new LinkedList<Element>();
		for (int i=0; i<song.eleList.size(); i++){
			//System.out.println("Q "+i);
			Element ele=(Element)song.eleList.get(i);
			int[][] signal=ele.signal;
			int eleLength=signal.length;
			for (int j=0; j<eleLength; j++){
				for (int k=1; k<signal[j].length; k++){
					signal[j][k]=ny-signal[j][k]-1;
				}
			}
			
			LinkedList<int[][]> tList=new LinkedList<int[][]>();
			tList.add(signal);
			//System.out.println("STARTS: "+signal[0][0]+" "+nout.length+" "+nout[0].length);
			
			
			
			LinkedList<double[]> entList=new LinkedList<double[]>();
			try{
				measureWienerEntropy(tList, entList);
			}
			catch(Error e){e.printStackTrace();}
			
			double[] ent=entList.get(0);
			
			double[][] newm=new double[ent.length][15];
			
			for (int j=0; j<ent.length; j++){
				for (int k=0; k<15; k++){
					newm[j][k]=ele.measurements[j+5][k];
				}
				newm[j][9]=ent[j];
			}
			
			for (int j=0; j<eleLength; j++){
				for (int k=1; k<signal[j].length; k++){
					signal[j][k]=ny-signal[j][k]-1;
				}
			}
			
			ele.measurements=newm;
			ele.calculateStatistics();
			eleList.add(ele);
		}
		System.out.println("done");
		song.eleList=eleList;
	}
	
	private int measureAndAddElements(LinkedList<int[][]> signals){
		LinkedList<double[][]> freqList=new LinkedList<double[][]>();
		LinkedList<double[][]> freqChangeList=new LinkedList<double[][]>();
		//LinkedList<double[][]> freqChangeList2=new LinkedList<double[][]>();
		LinkedList<double[]> harmList=new LinkedList<double[]>();
		LinkedList<double[]> wienerList=new LinkedList<double[]>();
		LinkedList<int[]> bandwidthList=new LinkedList<int[]>();
		LinkedList<double[]> ampList=new LinkedList<double[]>();
		LinkedList<double[]> powerList=new LinkedList<double[]>();
		LinkedList<double[][]> trillList=new LinkedList<double[][]>();
		//LinkedList<boolean[][]> sigList=new LinkedList<boolean[][]>();
		//getSignalLoc(signals, sigList);
		measureFrequencies(signals, freqList);
		//measureFrequencyChange(signals, freqList, freqChangeList);
		//
		measureFrequencyChange4(signals, freqList, freqChangeList);
		//freqChangeList=new LinkedList<double[][]>();
		//measureFrequencyChange2(freqList, freqChangeList);
		//measureFrequencyChange3(signals, sigList, freqChangeList2);
		measureTrills(freqList, trillList);
		measureHarmonicity(signals, freqList, harmList);
		measureWienerEntropy(signals, wienerList);
		measureBandwidth(signals, bandwidthList);
		measureAmplitude(freqList, signals, ampList);
		calculatePowerSpectrum(signals, powerList);
		LinkedList<double[][]> measures=new LinkedList<double[][]>();
		for (int i=0; i<freqList.size(); i++){
			double[] harmonicity=harmList.get(i);
			double[] wiener=wienerList.get(i);
			int[] bandwidth=bandwidthList.get(i);
			double[] amplitude=ampList.get(i);
			//double[] reverberation=(double[])reverbList.get(i);
			double[][] freqChange=freqChangeList.get(i);
			//double[][] freqChange2=freqChangeList2.get(i);
			double[][] freq=freqList.get(i);
			double[][] trill=trillList.get(i);
			double[][] measureP=new double[harmonicity.length][15];
			for (int j=0; j<harmonicity.length; j++){
				measureP[j][0]=freq[j][0]*dy;
				measureP[j][1]=freq[j][1]*dy;
				measureP[j][2]=freq[j][2]*dy;
				measureP[j][3]=freq[j][3]*dy;
				measureP[j][4]=freqChange[j][0];
				measureP[j][5]=freqChange[j][1];
				measureP[j][6]=freqChange[j][2];
				measureP[j][7]=freqChange[j][3];
				//measureP[j][7]=freqChange2[j][0];
				measureP[j][8]=harmonicity[j];
				measureP[j][9]=wiener[j];
				measureP[j][10]=bandwidth[j]*dy;
				measureP[j][11]=amplitude[j];
				measureP[j][12]=trill[j][2];
				measureP[j][13]=trill[j][0];
				measureP[j][14]=trill[j][1];
			}
			measures.add(measureP);
		}		
		freqList=null;
		freqChangeList=null;
		harmList=null;
		wienerList=null;
		ampList=null;
		bandwidthList=null;
		fixTimes(signals);
		int numberElementsAddedLastTime=signals.size();
		
		archiveLastElementsAdded=new int[numberElementsAddedLastTime];
		
		for (int i=0; i<numberElementsAddedLastTime; i++){
			int[][]t=signals.get(i);
			double[][]t2=measures.get(i);
			double[]t3=powerList.get(i);
			Element ele=new Element(song, t, t2, t3);
			ele.calculateStatistics();
			double p=ele.begintime+0.5*(ele.length);
			int count=0;
			for (int j=0; j<song.eleList.size(); j++){
				Element ele3=(Element)song.eleList.get(j);
				double p2=ele3.begintime+0.5*(ele3.length);
				if (p2<p){count++;}
				else{j=song.eleList.size();}
			}
			song.eleList.add(count, ele);
			archiveLastElementsAdded[i]=count;
		}
		signals=null;
		measures=null;
		pointList=null;
		pList=new int[tnx][2];
		oldPList=new int[tnx][2];
		return numberElementsAddedLastTime;
	}

	

	private LinkedList<int[][]> getSignal(){						//This method identifies which parts of the highlighted sound are loud enough to be "signal"
														//and joins them together on the basis of temporal contiguity into elements (further lumping/splitting
														//of elements can happen in later methods)
		LinkedList<int[][]> tList=new LinkedList<int[][]>();
		lastPointList=new int[pointList.length][];
		for (int i=0; i<pointList.length; i++){
			lastPointList[i]=new int[pointList[i].length];
			System.arraycopy(pointList[i], 0, lastPointList[i], 0, lastPointList[i].length);
		}
		
		int minx=0;
		while (pointList[minx][0]==0){minx++;}
		int maxx=unx-2;								//this bit identifies the start and end of the element
		while (pointList[maxx][0]==0){maxx--;}
		maxx++;
		
		int p1;
		for (int i=minx; i<maxx; i++){
			p1=ny-pointList[i][0]-1;
			pointList[i][0]=ny-pointList[i][1]-1;
			pointList[i][1]=p1;
			if (pointList[i][1]==ny-1){pointList[i][1]--;}
			if (pointList[i][0]==0){pointList[i][0]++;}
		}
		
		int ou[][]=new int[ny][unx];
		//int the2list[][]=new int [ny*(maxx-minx)][2];
		LinkedList<int[]> the2list=new LinkedList<int[]>();
		
		double focal;
		float c=(float)(1/song.dynMax);
		double u1=song.upperLoop;
		double l1=song.lowerLoop;
		
		//System.out.println("loop "+u1+" "+l1);
				
		for (int j=minx; j<maxx; j++){
			for (int i=pointList[j][0]; i<pointList[j][1]; i++){
				focal=nout[i][j]*c;
				if (focal>u1){
					int[] new2={i, j};
					the2list.add(new2);
					ou[i][j]=2;
				}
				else if (focal>l1){ou[i][j]=1;}	
				else{ou[i][j]=0;}
			}
		}

		int i,j,k, a,b, ia, jb, ii, jj;
		
		
		int counter=3;
		
		for (j=minx; j<maxx; j++){
			for (i=pointList[j][0]; i<pointList[j][1]; i++){
				if (ou[i][j]==2){
					int[]n3={i, j};
					ou[i][j]=counter;
					the2list=new LinkedList<int[]>();
					the2list.add(n3);
					while(the2list.size()>0){
						int[] n2=the2list.get(0);
						the2list.remove(0);
						ii=n2[0];
						jj=n2[1];
						for (a=-1; a<=1; a++){
							ia=ii+a;
							for (b=-1; b<=1; b++){
								jb=jj+b;
								if ((ia>=0)&&(ia<ny)&&(jb>=0)&&(jb<unx)){
									if ((ou[ia][jb]==1)||(ou[ia][jb]==2)){
										ou[ia][jb]=counter;
										int[]n4={ia, jb};
										the2list.add(n4);
									}
								}
							}
						}
					}
					counter++;
				}
			}
		}
		
		int numEls=counter-3;
		
		int[]starts=new int[numEls];
		int[]ends=new int[numEls];
		boolean[] exists=new boolean[numEls];
		for (k=0; k<numEls; k++){
			int mx1=-1;
			int mx2=0;
			for (j=minx; j<maxx; j++){
				for (ii=pointList[j][0]; ii<pointList[j][1]; ii++){
					if (ou[ii][j]==k+3){
						if (mx1==-1){
							mx1=j;
						}
						mx2=j;
					}
				}
			}
			mx2++;
			starts[k]=mx1;
			ends[k]=mx2;
			exists[k]=true;
		}
		
		double gapThresh=song.minGap/song.timeStep;
		
		for (k=0; k<numEls; k++){
			//System.out.println(numEls);
			if (exists[k]){
				for (j=0; j<k; j++){
					if (exists[j]){
						boolean merge=false;
						
						if ((starts[j]>starts[k]-gapThresh)&&(starts[j]<=ends[k]+gapThresh)){
							merge=true;
						}
						if ((starts[k]>starts[j]-gapThresh)&&(starts[k]<=ends[j]+gapThresh)){
							merge=true;
						}
						
						if (merge){
							exists[j]=false;
							for (jj=minx; jj<maxx; jj++){
								for (ii=pointList[jj][0]; ii<pointList[jj][1]; ii++){
									if (ou[ii][jj]==j+3){
										ou[ii][jj]=k+3;
									}
								}
							}
						}
					}
				}
			}
		}
		
		
		for (k=0; k<numEls; k++){
			int mx1=-1;
			int mx2=0;
			for (j=minx; j<maxx; j++){
				for (ii=pointList[j][0]; ii<pointList[j][1]; ii++){
					if (ou[ii][j]==k+3){
						if (mx1==-1){
							mx1=j;
						}
						mx2=j;
					}
				}
			}
			if (mx1>-1){
				mx2++;
				int[][] data=new int[mx2-mx1][];
				
				int nullCount=0;
				
				for (j=mx1; j<mx2; j++){
					int cc=0;
					for (ii=pointList[j][0]; ii<pointList[j][1]; ii++){
						//System.out.print(ou[ii][j]+" ");
						if ((ou[ii][j]==k+3)&&(ou[ii-1][j]!=k+3)){
							cc++;
						}
						if ((ou[ii-1][j]==k+3)&&(ou[ii][j]!=k+3)){
							cc++;
						}
					}
					if (ou[pointList[j][1]-1][j]==k+3){
						cc++;
					}
							
					jj=j-mx1;
					data[jj]=new int[cc+1];
					data[jj][0]=j;
					
					if (cc==0){nullCount++;}
					
					//System.out.println(cc);
					
					cc=1;
					
					for (ii=pointList[j][0]; ii<pointList[j][1]; ii++){
						if ((ou[ii][j]==k+3)&&(ou[ii-1][j]!=k+3)){
							data[jj][cc]=ii;
							cc++;
						}
						if ((ou[ii-1][j]==k+3)&&(ou[ii][j]!=k+3)){
							data[jj][cc]=ii;
							cc++;
						}
					}
					if (ou[pointList[j][1]-1][j]==k+3){
						data[jj][cc]=pointList[j][1];
					}
				}
				
				int[][] data2=new int[data.length-nullCount][];
				ii=0;
				for (j=0; j<data.length; j++){
					if (data[j].length>1){
						data2[ii]=new int[data[j].length];
						System.arraycopy(data[j],0, data2[ii],0,data[j].length);
						ii++;
					}
				}
				tList.add(data2);
			}
		}
				
	
			
			//Format of tList at this point: LinkedList containing int[][]'s. First dimension of int[][] is time. Second dimension frequencies.
			//First slot filled with time. Next two frequency slots are left blank for now (will be filled with: peak freq, fund freq below). Then, there are a variable
			//number of pairs: min, max; min, max etc. that represent minima and maxima for "chunks" of signal. e.g a tonal signal will have just one
			//min and max pair, while a harmonic signal will have multiple pairs one for each harmonic.
		 
		return tList;	
	}
	
	private void getSignalLoc(LinkedList<int[][]> tList, LinkedList<boolean[][]> sigList){
		int elNum=tList.size();
		for (int i=0; i<elNum; i++){
			int[][]data=tList.get(i);
			int eleLength=data.length;
			boolean[][] sig=new boolean[eleLength][ny];
			for (int j=0; j<eleLength; j++){
				for (int k=0; k<ny; k++){sig[j][k]=false;}
				int chunkNum=data[j].length;
				int time=data[j][0];
				float peakF=-100000;
				float sumF=0;
				int peakFLoc=0;
				for (int k=1; k<chunkNum; k+=2){
					for (int a=data[j][k]; a<data[j][k+1]; a++){
						if (nout[a][time]>0){
							sig[j][a]=true;
						}
					}
				}
			}
			sigList.add(sig);
		}
	}
	
	
	private void measureFrequencies(LinkedList<int[][]> tList, LinkedList<double[][]> freqList){
				
		int elNum=tList.size();
		for (int i=0; i<elNum; i++){
			int[][]data=tList.get(i);
			int eleLength=data.length;
			double [][] freqMeasures=new double[eleLength][4];
			//Following section measures the peak frequency within the signal at each time point within the signal, puts it in data[x][1]
			
			double [] amp=new double[eleLength];
			double []peakamp=new double[eleLength];
			for (int j=0; j<eleLength; j++){
				int chunkNum=data[j].length;
				int time=data[j][0];
				float peakF=-100000;
				float sumF=0;
				int peakFLoc=0;
				for (int k=1; k<chunkNum; k+=2){
					for (int a=data[j][k]; a<data[j][k+1]; a++){
						if (nout[a][time]>0){
							amp[j]+=nout[a][time];
							sumF+=nout[a][time]*a;
							if (nout[a][time]>peakF){
								peakF=nout[a][time];
								peakFLoc=a;
							}
						}
					}
				}
				freqMeasures[j][0]=peakFLoc;
				peakamp[j]=peakF;
				freqMeasures[j][1]=sumF/amp[j];
				
				double medianThreshold=amp[j]*0.5;
				if (amp[j]<0){
					medianThreshold=amp[j]*2;
				}
				double medianCount=0;
				boolean finished=false;
				for (int k=1; k<chunkNum; k+=2){
					for (int a=data[j][k]; a<data[j][k+1]; a++){
						medianCount+=Math.max(0, nout[a][time]);
						if ((nout[a][time]>0)&&(medianCount>medianThreshold)){
							freqMeasures[j][2]=a+((medianCount-medianThreshold)/nout[a][time]);
							finished=true;
						}
						if (finished){a=data[j][k+1];}
					}
					if (finished){k=chunkNum+1;}
				}
			}
			
			
			//Following section estimates the fundamental frequency at each time point, puts it in data[x][2]
			
			int x=0;
			//double ratioAdjust=song.fundAdjust;
			double subSuppression=song.fundAdjust;
			int harmlimit=(int)Math.round(0.5*song.fundJumpSuppression);
			
			double[][]hscor=new double[eleLength][ny];
			double[] logTransform=new double[ny];
			double log2Adj=1/Math.log(2);
			double logMax=Math.log(ny-1)*log2Adj;
			double logMin=Math.log(minFreq)*log2Adj;
			double step=(logMax-logMin)/(ny+0.0);

			for (int j=0; j<ny; j++){
				logTransform[j]=Math.pow(2, logMin+(j*step));
			}
			
			double[] spectrum=new double[ny];
			int rpitch[]=new int[eleLength];
			double scmax=0;
			//int loc=0;
			for (int j=0; j<eleLength; j++){
				x=data[j][0];
				scmax=-100000;
				//int maxP=data[j][data[j].length-1];
				int chunkNum=data[j].length;
				spectrum=new double[ny];
				/*
				for (int a=0; a<ny; a++){
					spectrum[a]=nout[a][x];
					if (spectrum[a]<0){
						spectrum[a]=0;
					}
					spectrum[a]=Math.pow(spectrum[a], subSuppression);
				}
				*/
				for (int a=1; a<chunkNum; a+=2){
					for (int b=data[j][a]; b<data[j][a+1]; b++){
						spectrum[b]=Math.max(0, nout[b][x]);
						spectrum[b]=Math.pow(spectrum[b], subSuppression);
					}
				}
				
				int count=0;
				for (int a=0; a<ny; a++){
					double sumxy=0;
					for (int b=0; b<ny; b++){
						double q=phase[count];
						sumxy+=q*spectrum[b];
						count++;
					}
					//hscor[j][a]=(sumxy/sumxx[a]);
					hscor[j][a]=(sumxy);
					if (hscor[j][a]>=scmax){
						scmax=hscor[j][a];
						rpitch[j]=a;
					}
				}
			}

			//rpitch=smoothPitch(hscor, rpitch, amp);
			rpitch=smoothPitch(hscor, amp, harmlimit);
			for (int j=0; j<eleLength; j++){
				freqMeasures[j][3]=logTransform[rpitch[j]];
				//System.out.println(logTransform[rpitch[j]]+" "+logTransform[rpitch[j]+1]);
			}
	
			freqList.add(freqMeasures);
			
			
		}
	}
	
	void measureFrequencyChange(LinkedList<int[][]> tList, LinkedList<double[][]> freqList, LinkedList<double[][]> freqChangeList){
		
		int elNum=tList.size();
		for (int i=0; i<elNum; i++){
			int[][]data=tList.get(i);
			int eleLength=data.length;
			
			double[][] hscor=new double[eleLength][ny];
			
			double[][] freqMeasures=freqList.get(i);
			
			int boxSize=10;
			int x, xp, minxf, maxxf;
			double sxy, sx2, tot, ylog, yref;
			
			double dat[][]=new double[eleLength][4];
		
			boolean[][] signal=new boolean[eleLength][ny];
			
			for (int k=0; k<eleLength; k++){
				//System.out.println(data[k].length+" "+eleLength+" "+data[k][1]+" "+data[k][2]+" "+ny);
				xp=data[k][0];
				for (int j=0; j<ny; j++){
					signal[k][j]=false;
				}
				for (int j=1; j<data[k].length; j+=2){
					for (int m=data[k][j]; m<data[k][j+1]; m++){
						signal[k][m]=true;
					}
				}
				//for (int j=0; j<ny; j++){
					//int pp=0;
					//if (signal[k][j]){pp=1;}
					//System.out.print(pp);
				//}
				//System.out.println();
				
				for (int j=1; j<ny; j++){
					int p=j;
					double pc=0;
					hscor[k][j]=0;
					while (p<ny){
						if (signal[k][p]){hscor[k][j]+=nout[p][xp];}
						pc++;
						p+=j;
					}
					hscor[k][j]/=pc;
					
					//System.out.println(k+" "+j+" "+xp+" "+pc+" "+hscor[k][j]+" "+nout[j][xp]+" "+freqMeasures[k][3]);
				}
				//System.out.println();
					
				
			}
			for (int j=0; j<4; j++){
				for (int k=0; k<eleLength; k++){
					sxy=0;
					sx2=0;
					yref=Math.log(freqMeasures[k][j]*dy);
					
					
					minxf=100000;
					maxxf=0;
					for (int a=0-boxSize; a<=boxSize; a++){
						x=a+k;
						if ((x>=0)&&(x<eleLength)){
							if (freqMeasures[x][j]<minxf){minxf=(int)Math.round(freqMeasures[x][j]);}
							if (freqMeasures[x][j]>maxxf){maxxf=(int)Math.round(freqMeasures[x][j]);}
						}
					}
					minxf-=boxSize;
					maxxf+=boxSize;
					for (int a=0-boxSize; a<=boxSize; a++){
						xp=data[k][0]+a;
						x=a+k;
						if ((x>=0)&&(x<eleLength)){
							for (int b=minxf; b<=maxxf; b++){
								if ((b>0)&&(b<ny)){
									ylog=Math.log(b*dy)-yref;
									if (j==3){
										sxy+=a*dx*ylog*hscor[x][b];
										sx2+=a*a*dx*dx*hscor[x][b];
									}
									else if (signal[x][b]){
										sxy+=a*dx*ylog*nout[b][xp];
										sx2+=a*a*dx*dx*nout[b][xp];
									}
								}
							}
						
						}
					}
					tot=30*sxy/sx2;
					//System.out.println(sxy+" "+sx2+" "+minxf+" "+maxxf);
					dat[k][j]=(float)(0.5+(Math.atan2(tot, 1)/Math.PI));
				}
			}
			freqChangeList.add(dat);
		}
	}
	
private void measureFrequencyChange2a(LinkedList<double[][]> freqList, LinkedList<double[][]> freqChangeList){
		
		int elNum=freqList.size();
		for (int i=0; i<elNum; i++){
			
						
			double[][] freqMeasures=freqList.get(i);
			int eleLength=freqMeasures.length;
			double dxx=1;
			if (dx<1){dxx=dx;}
			int boxSize=(int)Math.round(5/dxx);
			
			if (boxSize>=eleLength){boxSize=eleLength-1;}
			int x, y;
			double sxy, sx2, tot, ylog;
			
			double dat[][]=new double[eleLength][4];
		
			for (int j=0; j<4; j++){
				double[] f=new double[eleLength];
				for (int k=0; k<eleLength; k++){
					double c=0;
					for (int a=0; a<=boxSize; a++){
						x=a+k;
						y=k-a;
						if ((y>=0)&&(x<eleLength)){
							f[k]+=Math.log(freqMeasures[x][j]*dy);
							f[k]+=Math.log(freqMeasures[y][j]*dy);
							c+=2;
						}
					}
					f[k]/=c;
					//f[k]=Math.log(freqMeasures[k][j]*dy);
				}
				
				
				
				for (int k=0; k<eleLength; k++){
					sxy=0;
					sx2=0;
					

					for (int a=0-boxSize; a<=boxSize; a++){
						x=a+k;
						
						if ((x>=0)&&(x<eleLength)){
							ylog=f[x]-f[k];
							sxy+=a*ylog;
							sx2+=a*a*dx*dx;
						}
					}
					tot=20*sxy/(sx2);
					dat[k][j]=(float)(0.5+(Math.atan2(tot, 1)/Math.PI));
				}
				for (int k=0; k<boxSize; k++){
					dat[k][j]=dat[boxSize][j];
					dat[eleLength-1-k][j]=dat[eleLength-boxSize-1][j];
				}
			}
			freqChangeList.add(dat);
		}
	}

	private void measureFrequencyChange4(LinkedList<int[][]> tList, LinkedList<double[][]> freqList, LinkedList<double[][]> freqChangeList){
	
		double[] logs=new double[ny];
		for (int j=0; j<ny; j++){
			logs[j]=Math.log(j+1.5);
		}
		
		
		int elNum=freqList.size();
		for (int i=0; i<elNum; i++){
					
			double[][] freqMeasures=freqList.get(i);
			int[][] data=tList.get(i);
			int eleLength=freqMeasures.length;
			double dxx=1;
			if (dx<1){dxx=dx;}
			int boxSize=(int)Math.round(8/dxx);
		
			if (boxSize>=eleLength/2){boxSize=(eleLength/2)-1;}
			int x, y;
			double sxy, sx2, tot, ylog, sx, sy, cx;
			
			double[][] fa=new double[eleLength][ny];
			for (int j=0; j<eleLength; j++){
				int chunkNum=data[j].length;
				int time=data[j][0];
				for (int k=1; k<chunkNum; k+=2){
					for (int j2=data[j][k]; j2<data[j][k+1]; j2++){
						double n=nout[j2][time];
						if (n>0){
							fa[j][j2]=n*n;
						}
					}
				}
			}
			
			
				
			double dat[][]=new double[eleLength][4];
			for (int j=0; j<4; j++){
				for (int k=boxSize; k<eleLength-boxSize; k++){
					
					
					sy=0;
					sx=0;
					cx=0;
					sxy=0;
					sx2=0;
					for (int a=0-boxSize; a<=boxSize; a++){
						x=a+k;
						double x2=x*dx;
						if ((x>=0)&&(x<eleLength)){
							double f=Math.log(freqMeasures[x][j]);
							double ft=Math.exp(f+logs[0]);
							int harm=0;
							
							for (int b=0; b<ny; b++){
								double n=fa[x][b];
								if (n>0){
									double m=0;
									if (j==3){
										while (b>ft){
											harm++;
											ft=Math.exp(f+logs[harm]);
										}
										m=harm;
									}
									double m2=Math.log(b-freqMeasures[x][j]*m);
									sy+=m2*n;
									sx+=x2*n;
									sx2+=x2*x2*n;
									sxy+=m2*x2*n;
									cx+=n;
								}
							}
							
							
							/*
							int chunkNum=data[x].length;
							int time=data[x][0];
						
							for (int i2=1; i2<chunkNum; i2+=2){
								for (int j2=data[x][i2]; j2<data[x][i2+1]; j2++){
									double n=nout[j2][time];
									if (n>0){
										double m=0;
										if (j==3){
											m=Math.round(j2/freqMeasures[k][j]);
											if (m==0){m=1;}
										}
										
										double m2=Math.log(dy*j2/m);
										sy+=m2*n;
										sx+=x*dx*n;
										sx2+=x*x*dx*dx*n;
										sxy+=m2*x*dx*n;
										cx+=n;
									}
								}
							}
							*/
						}
					}
					
					tot=30*(sxy*cx-(sx*sy))/(cx*sx2-sx*sx);
					//tot=30*sxy/(sx2);
					dat[k][j]=(float)(0.5+(Math.atan2(tot, 1)/Math.PI));
					//if (j==3){System.out.println(k+" "+sy+" "+sx+" "+sxy+" "+sx2+" "+tot+" "+dat[k][j]);}
				}
				
				for (int k=0; k<boxSize; k++){
					dat[k][j]=dat[boxSize][j];
				}
				for (int k=eleLength-boxSize; k<eleLength; k++){
					dat[k][j]=dat[eleLength-boxSize-1][j];
				}
			}
			freqChangeList.add(dat);
		}
	}

	private void measureFrequencyChange2(LinkedList<double[][]> freqList, LinkedList<double[][]> freqChangeList){
		
		int elNum=freqList.size();
		for (int i=0; i<elNum; i++){
						
			double[][] freqMeasures=freqList.get(i);
			int eleLength=freqMeasures.length;
			double dxx=1;
			if (dx<1){dxx=dx;}
			int boxSize=(int)Math.round(4/dxx);
			
			//if (boxSize>=eleLength/2){boxSize=eleLength/2;}
			int x, y;
			double sxy, sx2, tot, ylog, sx, sy, cx;
			
			double dat[][]=new double[eleLength][4];
			double[] f=new double[eleLength];
			for (int j=0; j<4; j++){
				for (int k=0; k<eleLength; k++){
					
					f[k]=Math.log(freqMeasures[k][j]*dy);
				}
				
				
				for (int k=0; k<eleLength; k++){
					sy=0;
					sx=0;
					cx=0;
					for (int a=0-boxSize; a<=boxSize; a++){
						x=a+k;
						if ((x>=0)&&(x<eleLength)){
							sy+=f[x];
							sx+=x*dx;
							cx++;
						}
					}
					sy/=cx;
					sx/=cx;
					sxy=0;
					sx2=0;
					for (int a=0-boxSize; a<=boxSize; a++){
						x=a+k;
						if ((x>=0)&&(x<eleLength)){
							ylog=f[x];
							sxy+=(ylog-sy)*(x*dx-sx);
							sx2+=(x*dx-sx)*(x*dx-sx);
						}
					}
					//System.out.println(sxy+ " "+sx2);
					//if (j==3){System.out.println(k+" "+sy+" "+sx+" "+sxy+" "+sx2);}
					tot=30*sxy/(sx2);
					dat[k][j]=(float)(0.5+(Math.atan2(tot, 1)/Math.PI));
					if (j==3){System.out.println(k+" "+sy+" "+sx+" "+sxy+" "+sx2+" "+tot+" "+dat[k][j]);}
				}
				//for (int k=0; k<boxSize; k++){
					//dat[k][j]=dat[boxSize][j];
					//dat[eleLength-1-k][j]=dat[eleLength-boxSize-1][j];
				//}
			}
			freqChangeList.add(dat);
		}
	}

	private void measureFrequencyChange3(LinkedList<int[][]> tList, LinkedList<boolean[][]> sigList, LinkedList<double[][]> freqChangeList2){
		
		int elNum=sigList.size();
		
		double[] f=new double[ny];
		for (int i=1; i<ny; i++){f[i]=Math.log(i);}
		
		for (int i=0; i<elNum; i++){
			
			int[][]data=tList.get(i);
			
			boolean[][] sig=sigList.get(i);
			int eleLength=sig.length;
			int boxSize=(int)Math.round(5/dx);
			
			if (boxSize>=eleLength){boxSize=eleLength-1;}
			int x,x2, y;
			double sx, sy, cx, cy, sxy, sxx, tot, c, ns;
			
			double dat[][]=new double[eleLength][1];
			
			for (int j=0; j<eleLength; j++){
					tot=0;
					c=0;
					int time=data[j][0];
					for (int k=2; k<ny-1; k++){
						if ((sig[j][k])&&(nout[k-1][time]<nout[k][time])&&(nout[k+1][time]<nout[k][time])){
							sy=0;
							cy=0;
							sx=0;
							cx=0;
							for (int a=0-boxSize; a<=boxSize; a++){
								for (int b=0-boxSize; b<=boxSize; b++){
									x=a+j;
									y=b+k;
									x2=a+time;
									//System.out.println(x+" "+y+" "+a+" "+b+" "+x2+" "+eleLength+" "+ny);
									if ((x>=0)&&(x<eleLength)&&(y>0)&&(y<ny)&&(sig[x][y])&&(nout[y][x2]>0)){
										//System.out.println("h");
										sy+=nout[y][x2]*f[y];
										cy+=nout[y][x2];
										sx+=nout[y][x2]*x*dx;
										cx+=nout[y][x2];
									}
								}
							}
							sy/=cy;
							sx/=cx;
							sxy=0;
							sxx=0;
							ns=0;
							for (int a=0-boxSize; a<=boxSize; a++){
								for (int b=0-boxSize; b<=boxSize; b++){
									x=a+j;
									y=b+k;
									x2=a+time;
									if ((x>=0)&&(x<eleLength)&&(y>0)&&(y<ny)&&(sig[x][y])&&(nout[y][x2]>0)){
										sxy+=nout[y][x2]*nout[y][x2]*(f[y]-sy)*(x*dx-sx);
										sxx+=nout[y][x2]*nout[y][x2]*(x*dx-sx)*(x*dx-sx);
										ns+=nout[y][x2];
									}
								}
							}
							System.out.println(j+" "+k+" "+sxx+" "+sxy);
							tot+=ns*30*sxy/sxx;
							c+=ns;
						}
					}
					tot/=c;
					dat[j][0]=(float)(0.5+(Math.atan2(tot, 1)/Math.PI));
					System.out.println(tot+" "+c);
			}
			for (int k=0; k<boxSize; k++){
				dat[k][0]=dat[boxSize][0];
				dat[eleLength-1-k][0]=dat[eleLength-boxSize-1][0];
			}
			freqChangeList2.add(dat);
		}
	}
	
	public void redodiffs(Song song){
	
		
		for (int i=0; i<song.eleList.size(); i++){
		
			Element ele=(Element)song.eleList.get(i);
	
			for (int j=6; j<10; j++){				
				double max=0;
				double min=1000000;
			
				for (int k=0; k<ele.measurements.length; k++){
			
					double p=Math.tan((ele.measurements[k][j]-0.5)*Math.PI);
					p=p*0.3;
					p=0.5+(Math.atan2(p, 1)/Math.PI);
					ele.measurements[k][j]=p;
			
					if (p>max){
						max=p;
					}
					if (p<min){
						min=p;
					}
				}
				
				
			
			}
		}
	}
	

	
	
	

	
	private void measureHarmonicity(LinkedList<int[][]> tList, LinkedList<double[][]> freqList, LinkedList<double[]> harmList){ 
		int elNum=tList.size();
		int x;
		double c, tot;
		boolean[] signal=new boolean[ny];
		for (int i=0; i<elNum; i++){
			int[][]data=tList.get(i);
			double[][]freqMeasures=freqList.get(i);
			int eleLength=data.length;
			//Following section measures harmonicity
			double[] dat2=new double[eleLength];
			for (int j=0; j<eleLength; j++){
				x=data[j][0];
				tot=0;
				int chunkNum=data[j].length;
				double maxNout=0;
				
				for (int k=0; k<ny; k++){
					signal[k]=false;
				}
				for (int a=1; a<chunkNum; a+=2){
					for (int b=data[j][a]; b<data[j][a+1]; b++){
						if (nout[b][x]>maxNout){maxNout=nout[b][x];}
						signal[b]=true;
					}
				}
				for (int a=0; a<ny; a++){

					c=a/freqMeasures[j][3];
					c=Math.abs(c-Math.round(c));
					double z=0;
					if (signal[a]){
						z=nout[a][x];
						if (z<0){z=0;}
					}
					double y=Math.pow(10, (z/maxNout))-1;
					if (c<0.25){dat2[j]+=y;}
					tot+=y;
				}
				if ((tot<=0)||(maxNout<=0)){
					dat2[j]=0;
				}
				else{
					dat2[j]/=tot;
				}
			}
			
			double[]median=new double[data.length];
			double holder[]=new double[11];
			int a, count;
			for (int j=0; j<eleLength; j++){
				count=0;
				for (int k=-5; k<=5; k++){
					a=j+k;
					if ((a>=0)&&(a<eleLength)){
						holder[count]=dat2[a];
						count++;
					}
				}
				Arrays.sort(holder, 0, count);
				median[j]=holder[count/2];
			}
			 
			harmList.add(median);
		}
	}

	
	private void measureWienerEntropy(LinkedList<int[][]> tList, LinkedList<double[]> results){
	
		//Calculates the Wiener Entropy (ratio of Geometric:Arithmetic means of the power spectrum).
		//Uses the fact that spectrogram is already in decibel format to speed-up calculation of the geometric mean.
	
		int elNum=tList.size();
		boolean signal[]=new boolean[ny];
		
		//double logd=10/(Math.log(10));
		//double maxC=song.dynRange-Math.log(song.maxPossAmp)*logd;
		//double minPower=Math.pow(10, (0-maxC)*0.1);
		double minPower=Math.pow(10, -1);
		int x=0;
		for (int i=0; i<elNum; i++){
			int[][]data=tList.get(i);
			int eleLength=data.length;
			double [] wienerEntropies=new double[eleLength];
			for (int j=0; j<eleLength; j++){
				x=data[j][0];
				int chunkNum=data[j].length;
				double scoreArith=0;
				double scoreGeom=0;
				for (int k=0; k<ny; k++){
					signal[k]=false;
				}
				double maxNout=0;
				for (int k=1; k<chunkNum; k+=2){
					for (int a=data[j][k]; a<data[j][k+1]; a++){
						signal[a]=true;
						if (nout[a][x]>maxNout){maxNout=nout[a][x];}
					}
				}
				minPower=Math.pow(10, maxNout-100);
				for (int k=0; k<ny; k++){
					if (signal[k]){
						//double amp=nout[k][x]/maxNout;
						double amp=nout[k][x];
						//scoreArith+=Math.pow(10, (amp-maxC)*0.1);
						scoreArith+=Math.pow(10, (amp-1));
						scoreGeom+=amp;
					}
					else{
						scoreArith+=minPower;
						scoreGeom+=maxNout-100;
					}
				}
				//System.out.println(scoreGeom+" "+scoreArith);
				scoreArith/=ny+0.0;
				//scoreGeom=Math.pow(10, 0.1*((scoreGeom/(ny+0.0))-maxC));
				scoreGeom=Math.pow(10, ((scoreGeom/(ny+0.0))-1));
				wienerEntropies[j]=-1*Math.log(scoreGeom/scoreArith);
				//System.out.println("W "+wienerEntropies[j]+" "+scoreGeom+" "+scoreArith);
			}
			results.add(wienerEntropies);
		}
	}
	
	private void measureBandwidth(LinkedList<int[][]> tList, LinkedList<int[]> results){
		int elNum=tList.size();

		for (int i=0; i<elNum; i++){
			int[][]data=tList.get(i);
			int eleLength=data.length;
			int[] dat=new int[eleLength];
			int a, b, c, x;
			float maxs;
			for (int j=0; j<eleLength; j++){
				x=data[j][0];
				maxs=-100000;
				a=data[j][1];
				while ((a<=data[j][2])&&(nout[a][x]>maxs)){
					maxs=nout[a][x];
					a++;
				}
				
				maxs=-1000000;
				b=data[j][data[j].length-1];
				c=data[j][data[j].length-2];
				while ((b>=c)&&(nout[b][x]>maxs)){
					maxs=nout[b][x];
					b--;
				}
				dat[j]=b-a+2;
			}
			results.add(dat);
		}
	}
	
	private void measureAmplitude(LinkedList<double[][]> tList, LinkedList<int[][]> uList, LinkedList<double[]> results){
		int elNum=tList.size();
		int v;		
		for (int i=0; i<elNum; i++){
			double[][]data=tList.get(i);
			int[][] data2=uList.get(i);
			int eleLength=data.length;
			double[] dat=new double[eleLength];
			for (int j=0; j<eleLength; j++){
				v=(int)Math.round(data[j][0]);
				//System.out.println(j+" "+eleLength+" "+v+" "+data2[j][0]+" "+nout.length+" "+nout[0].length);
				dat[j]=song.dynRange-nout[v][data2[j][0]];
			}
			results.add(dat);
		}
	}
	
	void measureReverberation(LinkedList<int[][]> tList, LinkedList<double[]> results){
		
		int elNum=tList.size();
		
		double dbDecayThreshold=20;
		
		int window=(int)Math.round(50/song.timeStep);
		if (window<1){window=1;}
		
		for (int i=0; i<elNum; i++){
			int[][]data=(int[][])tList.get(i);
			int eleLength=data.length;
			double[] dat=new double[eleLength];
			for (int j=0; j<eleLength; j++){
				double dc=0;
				double co=0;
				int loc3=200;
				int loc2=data[j][1];
				int ij=data[j][0];
				int ii=ij;
				double db=-1000000;
				for (int ak=-10; ak<10; ak++){
					int aj=ak+ij;
					if ((aj>0)&&(aj<nout[loc2].length)&&(nout[loc2][aj]>db)){
						db=nout[loc2][aj];
						ii=aj;
					}
				}
				
				double dbStop=db-dbDecayThreshold;
				for (int ak=1; ak<200; ak++){
					dc=0;
					co=0;
					if (ak+window<nout[0].length){
						for (int aj=0; aj<window; aj++){
							if (ii+ak+aj<nout[loc2].length){
								dc+=nout[loc2][ii+ak+aj];
								co++;
							}
						}
						dc/=co;
						//if (dc<stopRatio*db){
						if (dc<dbStop){
							loc3=ak;
							ak=201;
						}

					}		
				}
				dat[j]=loc3*song.timeStep;
			}
			results.add(dat);
		}
	}

	private void measureReverberationRegression(LinkedList<int[][]> tList, LinkedList<double[]> results){
		
		int elNum=tList.size();
		double timeAmpEquivalence=0.01*song.timeStep;
		int window=(int)Math.round(100/song.timeStep);
		if (window<1){window=1;}
		
		for (int i=0; i<elNum; i++){
			int[][]data=tList.get(i);
			int eleLength=data.length;
			double[] dat=new double[eleLength];
			for (int j=0; j<eleLength; j++){
				int a=data[j][1];
				int c=data[j][0];
				int b=c;
				double db=-1000000;
				for (int ak=-10; ak<10; ak++){
					int aj=ak+c;
					if ((aj>0)&&(aj<nout[a].length)&&(nout[a][aj]>db)){
						db=nout[a][aj];
						b=aj;
					}
				}
				
				double sumx=0;
				double sumy=0;
				double sumxy=0;
				double sumxx=0;
				double count=0;
				double t,f;
				
				for (int k=0; k<window; k++){
					int d=b+k;
					if (d<nout[a].length){
						t=k*timeAmpEquivalence;
						f=nout[a][d];
						sumx+=t;
						sumx+=t*t;
						sumy+=f;
						sumxy+=t*f;
						count++;
					}		
				}
				count=1/count;
				dat[j]=(sumxy-(sumx*sumy*count))/(sumxx-(sumx*sumx*count));
			}
			results.add(dat);
		}
	}
	
	//public void measureTrills(LinkedList tList, LinkedList freqs, LinkedList trills){
	public void measureTrills(LinkedList<double[][]> freqs, LinkedList<double[][]> trills){
		for (int i=0; i<freqs.size(); i++){
			double[][] freqMeasures=freqs.get(i);
				
			double [] peakFreqs=new double[freqMeasures.length];
			for (int j=0; j<freqMeasures.length; j++){
				//peakFreqs[j]=freqMeasures[j][3];
				
				//System.out.println(freqMeasures[j][0]+" "+freqMeasures[j][3]);
				//if (Math.abs(freqMeasures[j][0]-freqMeasures[j][3])<freqMeasures[j][3]*0.25){
				//	peakFreqs[j]=freqMeasures[j][0];
				//}
				//else{
					//System.out.println("sw");
					peakFreqs[j]=freqMeasures[j][3];
				//}
				
				//System.out.println(freqMeasures[j][0]+" "+freqMeasures[j][3]+" "+peakFreqs[j]);
			}
			
			//for (int j=0; j<freqMeasures.length-1; j++){
			//	peakFreqs[j]=peakFreqs[j]-peakFreqs[j+1];
				//if (peakFreqs[j]>0){peakFreqs[j]=Math.sqrt(peakFreqs[j]);}
				//else{
				//	peakFreqs[j]=-1*Math.sqrt(-1*peakFreqs[j]);
				//}
				
			//}
			//peakFreqs[freqMeasures.length-1]=peakFreqs[freqMeasures.length-2];
			//double[][] r=calculateFFT(peakFreqs);
			//trillList.add(r);
			
			//int[][] els=(int[][])tList.get(i);
			
			//double[][]r=calculateTrill2(peakFreqs, els);
			
			if (peakFreqs.length>1){
			
				double[][]r=calculateTrill5(peakFreqs, song.maxTrillWavelength);
			//System.out.println("eg1: "+r[0][0]);
			//double[][] r=calculateTrill(peakFreqs);
			//System.out.println("eg2: "+r[0][0]);

				trills.add(r);
			}
			else{
				double[][] r={{0,0,0}};
				trills.add(r);
			}
			/*
			
			if (freqMeasures.length>32){
				double[][] r=calculateFFT(peakFreqs, 32);
				trills.add(r);
			}
			else if (freqMeasures.length>16){
				double[][] r=calculateFFT(peakFreqs, 16);
				trills.add(r);
			}
			else{
				double[][] r=new double[freqMeasures.length][2];
				trills.add(r);
			}*/
		}
	}
	
	
	public double[][] calculateSinWaves(double[] periods){
		int n=periods.length;
		
		double[][] sines=new double[n][];
		
		
		for (int i=0; i<n; i++){
			int p=(int)Math.ceil(periods[i]);
			double q=2*Math.PI/periods[i];
			p*=2;
			sines[i]=new double[p];
			for (int j=0; j<p; j++){
				sines[i][j]=Math.cos(j*q)-Math.cos((j+1)*q);
				//sines[i][j]=Math.cos(j*q);
				//System.out.print(sines[i][j]+" ");
			}
			//System.out.println();
		}
		return sines;
	}
	
	
	public double[] regress(double[] da, int startpoint, int endpoint){
		
		double sumxy=0;
		double sumx=0;
		double sumy=0;
		double sumxx=0;
		double sumyy=0;
		double den=endpoint-startpoint+1.0;
		for (int k=startpoint; k<=endpoint; k++){
			sumxy+=k*da[k];
			sumx+=k;
			sumy+=da[k];
			sumxx+=k*k;
			sumyy+=da[k]*da[k];
		}
		
		double[] results=new double[3];
		
		double sumx2=sumx*sumx;
		double sumy2=sumy*sumy;
		double sumxy2=sumx*sumy;
		
		double p=(den*sumxx)-sumx2;
		double q=(den*sumyy)-sumy2;
		double r=(den*sumxy)-sumxy2;
		
		results[1]=r/p;

		results[2]=r/(Math.sqrt(p)*Math.sqrt(q));
		results[2]=results[2]*results[2];
		if ((p<=0)||(q<=0)){
			results[2]=0;
			results[1]=0;
		}
		results[0]=(sumy/den)-results[1]*(sumx/den);
		return results;
	}
	
	public double pointToLine(double a, double b, double x, double y){
		
		
		double xl=(x+a*(y-b))/(1+a*a);
		double yl=a*xl+b;
		
		double dx=xl-x;
		double dy=yl-y;
		
		double r=Math.sqrt(dx*dx+dy*dy);
		
		//double[] results={xl, yl, r};
		
		return r;
	}
	
	public double verticalPointToLine(double a, double b, double x, double y){
		
		double expectedY=a*x+b;
		
		double r=Math.abs(expectedY-y);
		
		return r;
	}
		
	
	
	
	public double[][] calculateTrill3(double[] contours, int[][] data, int windowLength){
		
		if (contours.length<windowLength*2){
			windowLength=contours.length/2;
		}
		
		//double coeff=0.75;
		double coeff2=20;
		//int w2=windowLength/2;
		int n=data.length;
		
		
		
		int[]max=new int[n];
		int[]min=new int[n];
		
		for (int i=0; i<n; i++){
			
			int c=data[i][0];
			int a=(int)Math.round(contours[i]);
			int b=data[i][1];
			double q=nout[a][c]-coeff2;
			if (q<0){q=0;}
			while((a>=b)&&(nout[a][c]>q)){
				a--;
			}
			min[i]=a;
			
			a=(int)Math.round(contours[i]);
			b=data[i][2];
			q=nout[a][c]-coeff2;
			while((a<=b)&&(a<nout.length)&&(nout[a][c]>q)){
				a++;
			}
			max[i]=a;
			
			
			
			
			/*
			int a=data[i][1];
			int b=data[i][2];
			
			
			//System.out.println(c+" "+a+" "+b);
			double tot=0;
			double m=0;
			int ml=0;
			double mina=1000000;
			for (int j=a; j<=b; j++){
				if (nout[j][c]>=0){
					tot+=nout[j][c];
					if (nout[j][c]>m){
						m=nout[j][c];
						ml=j;
					}
					if (nout[j][c]<mina){
						mina=nout[j][c];
					}
				}
			}
			
			tot-=(b-a+1)*mina;
			
			double cum=m-mina;
			int loc1=ml;
			int loc2=ml;
			for (int j=ml; j>0; j--){
				loc1++;
				loc2--;
				if (loc1>b){
					loc1--;
				}
				else{
					if (nout[loc1][c]>0){
						cum+=nout[loc1][c]-mina;
					}
				}
				if (loc2<a){
					loc2++;
				}
				else{
					if (nout[loc2][c]>0){
						cum+=nout[loc2][c]-mina;
					}
				}
				//cum+=nout[loc1][c]+nout[loc2][c];
			}
			max[i]=loc1;
			min[i]=loc2;
			*/
			//System.out.println(i+" "+max[i]+" "+min[i]);
		}
		
		
		double[][] results=new double[n][2];
		System.out.println("made it");
		
		for (int i=0; i<n; i++){
			
			
			int p=windowLength/2;
			int loca=i-p;
			if (loca<0){
				loca=0;
			}
			int locb=loca+windowLength;
			if (locb>=n){
				locb=n-1;
				loca=locb-windowLength;
			}
			
			double regb1=0;
			double rega1=0;
			double regb2=0;
			double rega2=0;
			double bestcorr=-2;
			int locreg=0;
			
			for (int k=2; k<windowLength-2; k++){
				double[] r1=regress(contours, loca, loca+k);
				double[] r2=regress(contours, loca+k, locb);
				double score=r1[2]*k+r2[2]*(windowLength-k);
				if (score>bestcorr){
					bestcorr=score;
					regb1=r1[1];
					rega1=r1[0];
					regb2=r2[1];
					rega2=r2[0];
					locreg=k+loca;
				}
			}
			
			
			//System.out.println(contours[i]+" "+locreg+" "+rega1+" "+regb1+" "+rega2+" "+regb2+" "+bestcorr+" "+loca+" "+locb);
			
			
			/*
			double sumxy=0;
			double sumx=0;
			double sumy=0;
			double sumxx=0;
			double den=windowLength+0.0;
			for (int k=0; k<windowLength; k++){
				int kk=k+loca;
				sumxy+=k*contours[kk];
				sumx+=k;
				sumy+=contours[kk];
				sumxx+=k*k;
			}
			double regb=(sumxy-(sumx*sumy/den))/(sumxx-(sumx*sumx/den));
			double rega=(sumy/den)-regb*(sumx/den);
			//System.out.println(regb+" "+rega);
			 */
			double bestMax=0;
			double bestMin=1000000;

			double sumLarge=0;
			double maxx=0;
			double minn=0;
			double regb=0;
			for (int k=0; k<windowLength; k++){
				int kk=loca+k;
				if (kk<=locreg){
					maxx=max[kk]-rega1-regb1*kk;
					minn=min[kk]-rega1-regb1*kk;
					regb=regb1;
				}
				else{
					maxx=max[kk]-rega2-regb2*kk;
					minn=min[kk]-rega2-regb2*kk;
					regb=regb2;
				}
				//double maxx=max[loca+k]-rega-regb*k;
				//double minn=min[loca+k]-rega-regb*k;
				if (maxx>bestMax){
					bestMax=maxx;
				}
				if (minn<bestMin){
					bestMin=minn;
				}

				double minna=Math.abs(minn);
				sumLarge+=Math.max(minna, maxx)-Math.abs(0.5*regb*song.frameLength/song.timeStep);
			}

			results[i][0]=sumLarge*song.dy/(windowLength+0.0);
		}
		return results;
	}
	
	public double[][] calculateTrill3a(double[] contours, int[][] data, int windowLength){
		
		if (contours.length<windowLength*2){
			windowLength=contours.length/2;
		}
		double coeff=0.75;
		int n=data.length;

		int[]max=new int[n];
		int[]min=new int[n];
		
		for (int i=0; i<n; i++){
			
			min[i]=data[i][1];
			max[i]=data[i][2];
			//System.out.println(max[i]+" "+min[i]+" "+contours[i]);
			if (contours[i]<min[i]){
				min[i]=(int)Math.floor(contours[i]);
			}
			if (contours[i]>max[i]){
				max[i]=(int)Math.ceil(contours[i]);
			}
			
		}
		
		
		double[][] results=new double[n][2];
		
		for (int i=0; i<n; i++){
			
			
			int p=windowLength/2;
			int loca=i-p;
			if (loca<0){
				loca=0;
			}
			int locb=i+p;
			if (locb>=n){
				locb=n-1;
			}
			
			int wl2=locb-loca+1;
			
			
			
			double regb1=0;
			double rega1=0;
			double regb2=0;
			double rega2=0;
			double bestcorr=-2;
			int locreg=0;
			
			for (int k=2; k<wl2-2; k++){
				double[] r1=regress(contours, loca, loca+k);
				double[] r2=regress(contours, loca+k, locb);
				double score=r1[2]*k+r2[2]*(wl2-k);
				if (score>bestcorr){
					bestcorr=score;
					regb1=r1[1];
					rega1=r1[0];
					regb2=r2[1];
					rega2=r2[0];
					locreg=k+loca;
				}
			}
			
			
			//System.out.println(contours[i]+" "+locreg+" "+rega1+" "+regb1+" "+rega2+" "+regb2+" "+bestcorr+" "+loca+" "+locb);

			//System.out.println(regb+" "+rega);
			double bestMax=0;
			double bestMin=1000000;

			double sumExp=0;
			double maxx=0;
			double minn=0;
			double exp=0;
			
			double slopeAdj=Math.max(Math.abs(regb1), Math.abs(regb2))*coeff*wl2;		//the steeper the slope, the larger we expect the bandwidth to be due to spectrogram limitations. This factor is a crude way of adjusting for this.
			
			
			for (int k=loca; k<=locb; k++){
				int kk=k-loca;
				//double maxx=max[k]-rega-regb*kk;
				//double minn=min[k]-rega-regb*kk;
				
				if (k<=locreg){
					maxx=max[k]-rega1-regb1*kk;
					minn=min[k]-rega1-regb1*kk;
					exp=contours[k]-rega1-regb1*kk;
				}
				else{
					maxx=max[k]-rega2-regb2*kk;
					minn=min[k]-rega2-regb2*kk;
					exp=contours[k]-rega2-regb2*kk;
				}
				
				
				
				//System.out.println(i+" "+k+" "+exp+" "+contours[k]+" "+slopeAdj+" "+rega1+" "+rega2+" "+regb1+" "+regb2);
				
				
				if (maxx>bestMax){
					bestMax=maxx;
				}
				if (minn<bestMin){
					bestMin=minn;
				}

				sumExp+=Math.abs(exp);
			}
			
			//System.out.println("R: "+i+" "+sumExp+" "+slopeAdj);
			slopeAdj=0;
			//System.out.println(bestMax+" "+bestMin);
			//results[i][0]=(bestMax-bestMin-slopeAdj)*song.dy;
			//results[i][0]=(sumMax-sumMin)*song.dy/(windowLength+0.0);
			//results[i][0]=(sumLarge*song.dy/(locb-loca+1.0))-slopeAdj;
			results[i][0]=song.dy*(sumExp-slopeAdj);
			
		}
		return results;
	}
	
	
	public double[][] calculateTrill5(double[] contours, double max){
		
		
		double thresh=0;
		double threshwav=max/song.timeStep;
		double threshasym=0.5;
		
		double threshold2=0.25;
		
		double threshold=contours.length*0.2;
		
		double minJump=2;
		
		LinkedList<int[]> peaks=new LinkedList<int[]>();
		
		int n=contours.length;
		
		boolean up=true;
		if (contours[1]>contours[0]){
			up=true;
		}
		else if (contours[1]<contours[0]){
			up=false;
		}
		else{
			for (int i=0; i<n; i++){
				if (contours[i]!=contours[0]){
					if (contours[i]>contours[0]){
						up=true;
					}
					else{
						up=false;
					}
					i=n;
				}
			}
		}
		//System.out.println(contours[0]+" "+contours[1]+" "+up);
		int loccont=0;
		
		for (int i=1; i<n; i++){
			//System.out.println(contours[i]);
			if (up){
				if(contours[i]<contours[loccont]-minJump){
					int[] q={i-1, 0};
					peaks.add(q);
					up=false;
					loccont=i;
					//System.out.println(q[0]+" "+q[1]+" "+contours[i-1]);
				}
				else if (contours[i]>contours[loccont]){
					loccont=i;
				}
			}
			if (!up){
				if(contours[i]>contours[loccont]+minJump){
					int[] q={i-1, 1};
					peaks.add(q);
					up=true;
					//System.out.println(q[0]+" "+q[1]+" "+contours[i-1]);
					loccont=i;
				}
				else if (contours[i]<contours[loccont]){
					loccont=i;
				}
			}
		}
		
		double[][] results=new double[n][3];
		int m=peaks.size();
		//System.out.println("peaks detected: "+m);
		
		if (m>5){
			double[]wavelengths=new double[m-2];
			double[]amps=new double[m-2];
			double[]asym=new double[m-2];
			int[] locs=new int[m-2];
		
			for (int i=1; i<m-1; i++){
				int[] pm1=peaks.get(i-1);
				int[] p=peaks.get(i);
				int[] pp1=peaks.get(i+1);
				wavelengths[i-1]=pp1[0]-pm1[0];
				//wavelengths[i-1]=Math.min(pp1[0]-p[0], p[0]-pm1[0]);
				//System.out.println(wavelengths[i-1]);
				double s=Math.abs(contours[p[0]]-contours[pm1[0]]);
				double t=Math.abs(contours[p[0]]-contours[pp1[0]]);
			
				//amps[i-1]=100*Math.abs(0.5*(s+t))/contours[p[0]];
				amps[i-1]=100*Math.min(s,t)/contours[p[0]];
				if (p[1]==0){
					asym[i-1]=(p[0]-pm1[0])/wavelengths[i-1];
				}
				else{
					asym[i-1]=(pp1[0]-p[0])/wavelengths[i-1];
				}
				locs[i-1]=p[0];
				//System.out.println(locs[i-1]+" "+amps[i-1]+" "+wavelengths[i-1]+" "+asym[i-1]);
			}
			
			double[] sumsq=new double[m-2];
			
			for (int i=0; i<m-2; i++){
				double ss1=0;
				for (int j=1; j<4; j++){
					double jj=locs[i]+j*wavelengths[i];
					double mindi=1000000;
					for (int k=i+j; k<locs.length; k++){
						if (Math.abs(wavelengths[i]-wavelengths[k])/Math.min(wavelengths[i], wavelengths[k])<1.5){
							double di=(locs[k]-jj)*(locs[k]-jj);
							if (di<mindi){mindi=di;}
						}
					}
					ss1+=mindi;
				}
				
				double ss2=0;
				for (int j=1; j<4; j++){
					double jj=locs[i]-j*wavelengths[i];
					double mindi=1000000;
					for (int k=0; k<i; k++){
						double di=(locs[k]-jj)*(locs[k]-jj);
						if (di<mindi){mindi=di;}
					}
					ss2+=mindi;
				}
				sumsq[i]=Math.sqrt(Math.min(ss1, ss2));
				if (wavelengths[i]>8){
					sumsq[i]/=wavelengths[i];
				}
				else{
					sumsq[i]/=8;
				}
				//System.out.println(wavelengths[i]+" "+sumsq[i]);
			}
		
		
			for (int i=0; i<n; i++){
				int loc=-1;
				double score=1000000;
				double bestscore=1000000;
				for (int j=0; j<m-2; j++){
					score=Math.abs(locs[j]-i);
					//if ((score<bestscore)&&(score<wavelengths[j])&&(sumsq[j]<threshold)){							//only consider local peaks that are within 1 wavelength of focal point
					if ((score<bestscore)&&(score<wavelengths[j])&&(wavelengths[j]<threshold)&&(sumsq[j]<threshold2)){	
						bestscore=score;
						loc=j;
					}
				}
				if(loc>=0){
					results[i][0]=amps[loc]*song.dy;
					//results[i][0]=amps[loc];
				}
				if (results[i][0]>thresh){
					results[i][1]=wavelengths[loc]*song.timeStep;
					results[i][2]=asym[loc];
				}
				else{
					results[i][1]=threshwav;
					results[i][2]=threshasym;
				}
				//System.out.println(song.dy+" "+i+" "+results[i][0]+" "+results[i][1]+" "+results[i][2]);
			}
		}
		else{
			for (int i=0; i<n; i++){
				results[i][1]=threshwav;
				results[i][2]=threshasym;
			}
		}
			
		return results;
	}

	
	public double[][] calculateTrill4(double[] contours, int windowLength){
		
		
		windowLength=contours.length/3;
		if (windowLength>40){windowLength=40;}
		if (windowLength<10){
			windowLength=10;
			if (windowLength>=contours.length){windowLength=contours.length-1;}
		}

		int n=contours.length;
		
		double[][] results=new double[n][2];
		
		for (int i=0; i<n; i++){
			
			int p=windowLength/2;
			int loca=i-p;
			if (loca<0){
				loca=0;
			}
			int locb=loca+windowLength;
			if (locb>=n){
				locb=n-1;
				loca=locb-windowLength;
				if (loca<0){loca=0;}
			}
			
			double wl2=locb-loca+1.0;
			
			double bestDist=1000000;
			
			for (int k=loca+2; k<locb-2; k++){
				double[] r1=regress(contours, loca, k);
				double[] r2=regress(contours, k, locb);
				double score=0;
				for (int j=loca; j<=locb; j++){
					if (j<=k){
						score+=pointToLine(r1[1], r1[0], j, contours[j]);
					}
					else{
						score+=pointToLine(r2[1], r2[0], j, contours[j]);
					}
				}
				if (score<bestDist){
					bestDist=score;
				}
			}
			if (bestDist==1000000){bestDist=1;}
			//System.out.println(i+" "+contours[i]+" "+bestDist+" "+locreg+" "+regb1+" "+regb2+" "+rega1+" "+rega2+" "+cc1+" "+cc2);
			
			results[i][0]=song.dy*10*(bestDist/wl2);
			
		}
		return results;
	}
	
	
	public double[][] calculateTrill2(double[] data, int[][] data2){
		
		
		double dampenFactor=0.1;
		int minPeriod=2;
		int maxPeriod=20;
		int rep=2;
		int et=data.length;
		int fc=nout.length;
		
		float[][] tempel=new float[et][fc];
		double[][]results=new double[et][2];
		double[] bestScores=new double[et];
		
		float c=(float)(1/(song.dynMax*song.dynRange));
		
		for (int i=0; i<et; i++){
			
			float tot=0;
			int d=data2[i][0];
			for (int j=1; j<data2[i].length; j+=2){
				for (int k=data2[i][j]; k<data2[i][j+1]; k++){
					tempel[i][k]=nout[k][d]*c;
					if (tempel[i][k]<0){tempel[i][k]=0;}
					tot=tot+tempel[i][k];
				}
			}
			for (int j=0; j<tempel[i].length; j++){
				tempel[i][j]/=tot;
				//System.out.print(tempel[i][j]+" ");
			}
			//System.out.println();
		}
		
		int[] mean=new int[et];
		double[] diffs=new double[et];
		for (int i=minPeriod; i<maxPeriod; i++){
			for (int j=0; j<et; j++){
			
				int a=j-i;
				if (a<0){a=0;}
				int b=j+j-a;
				//int b=j+i;
				if (b>=et){
					b=et-1;
					a=j+j-b;
				}
				
				double sum=0;
				double tot=0;
				for (int k=a; k<=b; k++){
					sum+=data[k];
					tot++;
				}
				sum/=tot;
				mean[j]=(int)Math.round(sum);
				diffs[j]=Math.abs(sum-data[j]);
				//System.out.println(i+" "+j+" "+mean[j]+" "+data[j]+" "+sum+" "+diffs[j]);
			}
			double[]s=new double[et];
					
			for (int j=0; j<et; j++){
				int k=j+rep*i;
				if (k<et){
					int diff=mean[k]-mean[j];
					for (int a=0; a<fc; a++){
						int b=a+diff;
						if ((b>=0)&&(b<fc)){
							s[j]+=tempel[j][b]*tempel[k][a];
						}
					}
					double self=0;
					for (int a=0; a<fc; a++){
						self+=tempel[j][a]*tempel[j][a];
					}	
					s[j]/=self;
				}
				else{
					s[j]=-1;
				}

			}
			for (int j=0; j<et; j++){
				int k=j-rep*i;
				if (k<0){k=0;}
				double score=0;
				double tot=0;
				for (int a=k; a<=j; a++){
					if (s[a]>=0){
						score+=s[a];
						tot++;
					}
				}
				if (tot>0){
					score/=tot;
				}
				//System.out.println(score+" "+tot);
				//score/=Math.log(i);
				
				score/=1+dampenFactor*i;
				
				if (score>bestScores[j]){
					bestScores[j]=score;
					results[j][1]=i/dx;
					
					double b=0;
					double t=0;
					
					int aa=j-rep*i;
					if (aa<0){aa=0;}
					int ab=j+rep*i;
					if (ab>=et){ab=et-1;}
					
					double[] temp=new double[ab-aa+1];
					for (int a=aa; a<=ab; a++){
						b+=diffs[a];
						temp[a-aa]=diffs[a];
						//System.out.print(diffs[a]+" ");
						t++;
					}
					//System.out.println();
					if (t>0){
						results[j][0]=b/t;
						Arrays.sort(temp);
						results[j][0]=temp[temp.length/2];
					}				
				
				}				
				
			}
		}
		for (int j=0; j<et; j++){
			if (bestScores[j]<0.2){
				results[j][1]=0;
				results[j][0]=0;
			}
			//System.out.println(j+" "+bestScores[j]+" "+results[j][1]+" "+results[j][0]);
			
		}
		return results;
	}
	
	
	public double[][] calculateTrill(double[] data){
		int n=data.length;
		BasicStatistics bs=new BasicStatistics();
		double mean=bs.calculateMean(data);
		
		for (int i=0; i<n; i++){
			data[i]-=mean;
		}
		
		int nfreqs=50;
		int minPeriod=2;
		int maxPeriod=64;
		
		double[][] compTable=new double[n][nfreqs];
		double[][] corrTable=new double[n][nfreqs];
	
		double[] frame=new double[nfreqs];
		int [] frame2=new int[nfreqs];
		double adj2=1/Math.log(2);
		
		double minLog=Math.log(minPeriod)*adj2;
		double maxLog=Math.log(maxPeriod)*adj2;
		double gapP=(maxLog-minLog)/(nfreqs-1.0);
		
		for (int i=0; i<nfreqs; i++){
			double p=(i*gapP)+minLog;
			frame2[i]=(int)Math.round(Math.pow(2, p));
			frame[i]=Math.pow(2, p);
			//System.out.println("FRAME: "+frame[i]);
		}
		
		
		
		double[][] sines=calculateSinWaves(frame);
		int loca, locb, kk;
		double sumxy, sumx, sumy, sumxx, sumyy, regb, rega, adjy, den;
		for (int i=0; i<n; i++){
			for (int j=2; j<nfreqs; j++){
				int m=sines[j].length;
				if (n<=m*2){
					corrTable[i][j]=-1;
				}
				else{
				
					int p=m/2;
					loca=i-p;
					if (loca<0){
						loca=0;
					}
					locb=loca+m;
					if (locb>=n){
						locb=n-1;
						loca=locb-m;
					}
					
					sumxy=0;
					sumx=0;
					sumy=0;
					sumxx=0;
					den=m+0.0;
					for (int k=0; k<m; k++){
						kk=k+loca;
						sumxy+=k*data[kk];
						sumx+=k;
						sumy+=data[kk];
						sumxx+=k*k;
					}
					regb=(sumxy-(sumx*sumy/den))/(sumxx-(sumx*sumx/den));
					rega=(sumy/den)-regb*(sumx/den);
										
					sumxy=0;
					sumx=0;
					sumy=0;
					sumxx=0;
					sumyy=0;
					for (int k=0; k<m; k++){
						adjy=data[loca+k]-rega-regb*k;
						//adjy=data[loca+k];
						sumxy+=adjy*sines[j][k];
						sumx+=sines[j][k];
						sumy+=adjy;
						sumxx+=sines[j][k]*sines[j][k];
						sumyy+=adjy*adjy;
						//compTable[i][j]+=adjy*sines[j][k];
					}
					//compTable[i][j]/=den;
					
					//corrTable[i][j]=sumxy;
					sumxy=sumxy-((sumx*sumy)/den);
					sumxx=sumxx-(sumx*sumx/den);
					sumyy=sumyy-(sumy*sumy/den);
					if (sumyy==0){sumyy=1;}
					compTable[i][j]=(sumxy/sumxx);
					corrTable[i][j]=(sumxy/Math.sqrt(sumxx*sumyy));
					
				}
			}
		}
		
		double[][] results=new double[n][2];
		
		double score;
		int offset=-1;
		int freq=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<nfreqs; j++){
				//System.out.println(n+" "+j+" "+sines[j].length);
				if (n>=sines[j].length*4){
					int aa=i-frame2[j];
					if (aa<0){aa=0;}
					int bb=i+frame2[j];
					if (bb>=n){bb=n-1;}
					//System.out.println(j+" "+sines[j].length+" "+aa+" "+bb);
					for (int k=aa; k<=bb; k++){
					
				//loca=i-frame2[j];
				//if (loca<0){loca=0;}
				//locb=i+frame2[j];
				//if (locb>=n){locb=n-1;}
				//score=0;
				//for (int k=loca; k<=locb; k++){
				//	score+=corrTable[k][j];
				//}
						//score=corrTable[i][j]*Math.sqrt(compTable[i][j]);
						score=corrTable[k][j];
				//score/=(locb-loca+1.0);
						if (score>results[i][0]){
					
							results[i][0]=score;
							results[i][1]=j;
							offset=k;
							freq=j;
						}
					}
				}
			}
			
			if (offset>=0){
				//System.out.println(offset+" "+freq+" "+results[i][0]);
			
				int m=sines[freq].length;
				int p=m/2;
				loca=offset-p;
				if (loca<0){
					loca=0;
				}
				locb=loca+m;
				if (locb>=n){
					locb=n-1;
					loca=locb-m;
				}
					
				sumxy=0;
				sumx=0;
				sumy=0;
				sumxx=0;
				den=m+0.0;
				for (int k=0; k<m; k++){
					kk=k+loca;
					sumxy+=k*data[kk];
					sumx+=k;
					sumy+=data[kk];
					sumxx+=k*k;
				}
				regb=(sumxy-(sumx*sumy/den))/(sumxx-(sumx*sumx/den));
				rega=(sumy/den)-regb*(sumx/den);
				double bestA=100000;
				int ampVal=0;
				int maxAmp=50;
				for (int w=0; w<maxAmp; w++){
					sumx=0;
					for (int k=0; k<m; k++){
						adjy=data[loca+k]-rega-regb*k;
						sumx+=(adjy-(sines[freq][k]*(frame[w]-minPeriod)))*(adjy-(sines[freq][k]*(frame[w]-minPeriod)));
					}
					sumx=Math.sqrt(sumx/den);
					if (sumx<bestA){
						bestA=sumx;
						ampVal=w;
					}
				}
				results[i][0]=frame[ampVal]-minPeriod;
			//System.out.println(n+" "+sines[5].length);
				//System.out.println(results[i][0]+" "+results[i][1]);
			
			}
		}
		return results;
	}
		
	private void fixTimes(LinkedList<int[][]> signals){
	
		int elNum=signals.size();

		for (int i=0; i<elNum; i++){
			int[][]data=signals.get(i);
			int eleLength=data.length;
			for (int j=0; j<eleLength; j++){
				data[j][0]+=currentMinX;
				for (int k=1; k<data[j].length; k++){
					data[j][k]=ny-data[j][k]-1;
				}
			}
			
			signals.remove(i);
			signals.add(i, data);
		}
	}
	
	
	
	public void calculatePowerSpectrum(LinkedList<int[][]> signal, LinkedList<double[]> results){
	
		//this method measures the power spectrum of a discovered element.
		for (int i=0; i<signal.size(); i++){
			int[][] ele=signal.get(i);
			double[] powerSpectrum=new double[song.ny];
			int j=0;
			int a=ele.length;
			//double logd=Math.log(10)*0.1;
			//double maxC=song.dynRange-Math.log(song.maxPossAmp)*logd;
			
			for (j=0; j<ele.length; j++){
				int b=ele[j][0];
				for (int k=1; k<ele[j].length; k+=2){
					//System.out.println(b+" "+ele[j][k]+" "+ele[j][k+1]);
					for (int l=ele[j][k]; l<ele[j][k+1]; l++){
						
						powerSpectrum[l]+=song.out1[l][b];
					}
				}
			}
			
			//logd=10/(Math.log(10));

			for (int k=0; k<powerSpectrum.length; k++){
				powerSpectrum[k]/=a+0.0;
				//if (powerSpectrum[k]>0){powerSpectrum[k]=Math.log(powerSpectrum[k]*powerSpectrum[k])*logd+maxC;}
				//System.out.println(powerSpectrum[k]);
			}
			results.add(powerSpectrum);
		}
	}
	
	int[] smoothPitch(double[][] score, double[]amps, int harmlim){
	
		int y=score[0].length;
		int x=score.length;
		
		double besttrajsc=-1000000;
		int[] besttraj=new int[x];
		int[] traj=new int[x];
		
		for (int i=0; i<x; i++){
			
			int loc=0;
			double bests=-1000000;
			for (int j=0; j<y; j++){
				if (score[i][j]>bests){
					bests=score[i][j];
					loc=j;
				}
			}
			
			int c=loc;
			double trajsc=bests;
			traj[i]=loc;
			for (int j=i-1; j>=0; j--){
				int a=c-harmlim;
				if (a<0){a=0;}
				int b=c+harmlim;
				if (b>=score[j].length){b=score[j].length-1;}
				int aloc=0;
				double abests=-1000000;
				for (int k=a; k<=b; k++){
					if (score[j][k]>abests){
						abests=score[j][k];
						aloc=k;
					}
				}
				c=aloc;
				traj[j]=aloc;
				trajsc+=abests;
			}
			c=loc;
			for (int j=i+1; j<x; j++){
				int a=c-harmlim;
				if (a<0){a=0;}
				int b=c+harmlim;
				if (b>=score[j].length){b=score[j].length-1;}
				int aloc=0;
				double abests=-1000000;
				for (int k=a; k<=b; k++){
					if (score[j][k]>abests){
						abests=score[j][k];
						aloc=k;
					}
				}
				c=aloc;
				traj[j]=aloc;
				trajsc+=abests;
			}
			if (trajsc>besttrajsc){
				besttrajsc=trajsc;
				System.arraycopy(traj, 0, besttraj, 0, x);
			}
		}
		return besttraj;
	}
	
	
	private int[] smoothPitch(double[][] score, int[]rpitch, double[]amps){
	
		int size=rpitch.length;
		//int corramt=(int)Math.round(20/dx);
		int corramt=10;
		int i,j, placex, placey, dir, x, y, cut;
		double bestdir;
		double[][] scores=new double[size][ny];
		//for (i=0; i<size; i++){
		//	scores[i]=new double[score[i].length];
		//}
		double temp;
		for (i=0; i<size; i++){
			for (j=0; j<score[i].length; j++){
				scores[i][j]+=score[i][j]*amps[i];
			}
			placex=i;
			placey=rpitch[i];

			cut=i+corramt;
			if (cut>size-1){cut=size-1;}
			
			while((placex<cut)&&(placey<score[placex].length)&&(score[placex][placey]>0)){
				x=placex+1;
				dir=rpitch[x];
				bestdir=-1000000000;
				for (j=0; j<9; j++){
					y=placey+j-4;
					if ((y<score[x].length)&&(y>=0)){
						if (score[x][y]>bestdir){
							bestdir=score[x][y];
							dir=y;
						}
					}
				}
				placex++;
				placey=dir;
				temp=score[i][rpitch[i]]*amps[i];
				scores[placex][placey]+=temp;
			}
			placex=i;
			placey=rpitch[i];
			
			cut=i-corramt;
			if (cut<0){cut=0;}
			
			while((placex>cut)&&(placey<score[placex].length)&&(score[placex][placey]>0)){
				x=placex-1;
				dir=rpitch[x];
				bestdir=-1000000000;
				for (j=0; j<9; j++){
					y=placey+j-4;
					if ((y<score[x].length)&&(y>=0)){
						if (score[x][y]>bestdir){
							bestdir=score[x][y];
							dir=y;
						}
					}
				}
				placex--;
				placey=dir;
				scores[placex][placey]+=score[i][rpitch[i]]*amps[i];
			}
			placex=i;
			placey=rpitch[i];
		}
			
				
		double max;
		int loc;
		for (i=0; i<size; i++){
			max=-100;
			loc=0;
			for (j=0; j<scores[i].length; j++){
				if (scores[i][j]>max){
					max=scores[i][j];
					loc=j;
				}
			}
			rpitch[i]=loc;
		}

		return rpitch;
	}
	
	void resetFonts(){
		timeAxisFont=new Font(fontFace, tickLabelFontStyle, tickLabelFontSize);
		freqAxisFont=new Font(fontFace, tickLabelFontStyle, tickLabelFontSize);	
		timeLabelFont=new Font(fontFace, axisLabelFontStyle, axisLabelFontSize);
		freqLabelFont=new Font(fontFace, axisLabelFontStyle, axisLabelFontSize);
		fs=new BasicStroke(lineWeight, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
	}
	
	
	
	void paintFrame(){
		
		DecimalFormat df = new DecimalFormat("#.#");
		
		if(song.maxf<=2000){
			minorFreqTick=100;
			majorFreqTick=500;
		}
		
	
		String label="Frequency (Hz)";
		if (frequencyUnit){
			label="Frequency (kHz)";
		}
	
		Graphics2D h=imf.createGraphics();
		h.setStroke(fs);
		h.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		FontMetrics fm=h.getFontMetrics(freqLabelFont);
		int w2=fm.stringWidth(label);
		int ma=fm.getMaxAscent();
		int md=fm.getMaxDescent();
		int h2=ma+md;
		BufferedImage vt=new BufferedImage(w2, h2, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=vt.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.BLACK);
		g.setFont(freqLabelFont);
		g.drawString(label, 0, ma);		
		g.dispose();
		
		
		//h.setColor(Color.WHITE);
		//h.fillRect(0,0, nnx, nny);
		//h.fillRect(0,0, xspace, nny);
		h.setColor(Color.BLACK);
		
		if (showFrame){			
			h.drawRect(xspace, yspace, tnx+1, tny+1);
		}
		else{
			h.drawLine(xspace, yspace, xspace, yspace+tny+1);
			h.drawLine(xspace, yspace+tny+1, xspace+tnx+1, yspace+tny+1);
		}
		double yt250=minorFreqTick/tdy;
		double yt1000=majorFreqTick/tdy;
		double ytick=yspace+tny+1;
		int yt=(int)Math.round(ytick);
		if (showMinorFreqTickMarks){
			while (ytick>yspace){
				if (interiorTickMarks){
					h.drawLine(xspace, yt, xspace+minorTickMarkLength, yt);
				}
				else{
					h.drawLine(xspace, yt, xspace-minorTickMarkLength, yt);
				}
				ytick-=yt250;
				yt=(int)Math.round(ytick);
			}
		}
		ytick=yspace+tny+1;
		yt=(int)Math.round(ytick);
		int ym=0;
		h.setFont(freqAxisFont);
		fm=h.getFontMetrics(freqAxisFont);
		
		int maxplwidth=0;
		
		int labelInc=fm.getMaxAscent()/2;	
		
		int tickAdjust=majorTickMarkLength+2;;
		
		if ((!showMajorFreqTickMarks)||(interiorTickMarks)){
			tickAdjust=2;
		}
		
		double kiloCorrector=1;
		if (frequencyUnit){
			kiloCorrector=0.001;
		}
				
		while (ytick>yspace){
			//Integer p=new Integer(ym);
			Integer p=new Integer((int)Math.round(kiloCorrector*ym));
			
			if (showFreqTickMarkLabels){
				String pl=df.format(kiloCorrector*ym);
				int plwidth=fm.stringWidth(pl);
				if (plwidth>maxplwidth){
					maxplwidth=plwidth;
				}
				h.drawString(pl, xspace-tickAdjust-plwidth, yt+labelInc);
			}
			if (showMajorFreqTickMarks){
				if (interiorTickMarks){
					h.drawLine(xspace, yt, xspace+majorTickMarkLength, yt);
				}
				else{
					h.drawLine(xspace, yt, xspace-majorTickMarkLength, yt);
				}
			}
			ytick-=yt1000;
			ym+=majorFreqTick;
			yt=(int)Math.round(ytick);
		}
		h.setFont(freqLabelFont);
		
		AffineTransform affineTransform = new AffineTransform(); 
		
		//System.out.println("POSITIONS: "+w2+" "+h2);
		
		double xpos=xspace-10-maxplwidth-(w2+h2)*0.5;
		
		affineTransform.setToTranslation(xpos, yspace+(tny-h2)*0.5);
		affineTransform.rotate(Math.toRadians(270), w2/2, h2/2); 

		h.drawImage(vt, affineTransform, this); 
		
		h.dispose();
		paintXAxisLabels();
	}
	
	void paintFrameAmp(double max){
	
		maxsl=(int)Math.round(Math.log(max)/Math.log(10));
		maxsl*=-1;
		StringBuffer pre=new StringBuffer("0.");
		for (int i=0; i<maxsl; i++){
			pre.append("0");
		}
		String prefix=pre.toString();
		
		Graphics2D h=imf.createGraphics();
		h.setStroke(fs);
		h.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		h.setColor(Color.WHITE);
		h.fillRect(0,0, nnx, nny);
		h.setColor(Color.BLACK);
		if (showFrame){			
			h.drawRect(xspace, yspace, tnx+1, tny+1);
		}
		else{
			h.drawLine(xspace, yspace, xspace, yspace+tny+1);
			h.drawLine(xspace, yspace+tny+1, xspace+tnx+1, yspace+tny+1);
		}
		
		double yt250=tny*0.0125;
		double yt1000=tny*0.05;
		
		double ytick=yspace+tny+1;
		int yt=(int)Math.round(ytick);
		while (ytick>yspace){
			h.drawLine(xspace, yt, xspace-3, yt);
			ytick-=yt250;
			yt=(int)Math.round(ytick);
		}
		ytick=yspace+tny+1;
		yt=(int)Math.round(ytick);
		//double ym=-1*max;
		h.setFont(freqAxisFont);
		for (int i=0; i<=20; i++){
			String pl="0";
			int j=i-10;
			if (j<0){
				j*=-1;
				pl="-"+prefix+j;
			}
			else if (j>0){
				pl=prefix+j;
			}
			if (i==0){
				pl="-"+max;
			}
			if (i==20){
				pl=" "+max;
			}
			
			int sl=pl.length();
						
			int xloc=xspace-(7*sl+7);
			h.drawString(pl, xloc, yt+5);
			
			h.drawLine(xspace, yt, xspace-5, yt);
			ytick-=yt1000;
			yt=(int)Math.round(ytick);
		}
		h.setFont(freqLabelFont);
		AffineTransform af = new AffineTransform();
		af.translate(-30., ((tny/2)+80.));
		af.rotate(3*Math.PI/2);		
		FontRenderContext renderContext = new FontRenderContext(null, false, false);
		h.transform(af);
		TextLayout layout = new TextLayout("Amplitude (V)" , h.getFont(), renderContext);
		layout.draw(h, 5, 50);
		h.dispose();
		paintXAxisLabels();
	}
	
	void paintFrameLogarithmic(){
		
		String label="Frequency (Hz)";
		
		Graphics2D h=imf.createGraphics();
		//h.setColor(Color.WHITE);
		//h.fillRect(0,0, nnx, nny);
		h.setColor(Color.BLACK);
		h.setStroke(fs);
		h.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		if (showFrame){			
			h.drawRect(xspace, yspace, tnx+1, tny+1);
		}
		else{
			h.drawLine(xspace, yspace, xspace, yspace+tny+1);
			h.drawLine(xspace, yspace+tny+1, xspace+tnx+1, yspace+tny+1);
		}
		
		double logCorrect=1/Math.log(2);
		
		double pm=Math.log(minFreq*song.dy)*logCorrect;
		int ps=(int)Math.ceil(pm);

		
		//int ym=(int)Math.round(Math.pow(2, pm+((tny-newy+1)*pe)));
		
		
		double pf=Math.log(ny*song.dy)*logCorrect;
		double pe=(pf-pm)/(tny+0.0);
		
		double startF=tny*(ps-pm)/(pf-pm);
		double increment=tny/(pf-pm);
		h.setFont(freqAxisFont);
		for (int i=ps; i<=pf; i++){
			int j=i-ps;
			
			int yloc=(int)Math.round(startF+j*increment);
			yloc=yspace+tny+1-yloc;
			h.drawLine(xspace, yloc, xspace-3, yloc);
			yloc-=3;
			
			double freq=Math.pow(2, i);
			Integer fp=new Integer((int)Math.round(freq));
			String pl=fp.toString();
			int len=pl.length();
			int xloc=xspace-(7+7*len);
			h.drawString(pl, xloc, yloc);
		}

		h.setFont(freqLabelFont);
		AffineTransform af = new AffineTransform();
		af.translate(-30., ((tny/2)+80.));
		af.rotate(3*Math.PI/2);		
		FontRenderContext renderContext = new FontRenderContext(null, false, false);
		h.transform(af);
		TextLayout layout = new TextLayout(label , h.getFont(), renderContext);
		layout.draw(h, 5, 50);
		h.dispose();
		paintXAxisLabels();
	}

	
	void paintXAxisLabels(){
		Graphics2D h=imf.createGraphics();
		h.setColor(Color.WHITE);
		h.setStroke(fs);
		h.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		int tickMarkOffset=majorTickMarkLength+2;
		if ((interiorTickMarks)||(!showMajorTimeTickMarks)){
			tickMarkOffset=2;
		}
		
		//h.fillRect(xspace,yspace+tny+2, tnx+10, yspace);
		h.setColor(Color.BLACK);		

		h.setFont(timeAxisFont);
		FontMetrics fm=h.getFontMetrics(timeAxisFont);
		int heightSpace=fm.getMaxAscent()+tickMarkOffset;
		
		int begin100=(int)(100*Math.ceil((currentMinX*stretchX)*tdx*0.01));
		double xtick=xspace+(begin100/tdx)-(currentMinX*stretchX);
		int xt=(int)Math.round(xtick);
		if (showMinorTimeTickMarks){
			while (xtick<xspace+tnx){
				if (interiorTickMarks){
					h.drawLine(xt, yspace+tny+1, xt, yspace+tny-minorTickMarkLength);
				}
				else{
					h.drawLine(xt, yspace+tny+1, xt, yspace+tny+minorTickMarkLength);
				}
				xtick+=100/tdx;
				xt=(int)Math.round(xtick);
			}
		}
		
		int begin500=(int)(500*Math.ceil((currentMinX*stretchX)*tdx*0.002));
		xtick=xspace+(begin500/tdx)-(currentMinX*stretchX);
		xt=(int)Math.round(xtick);
		int xm=begin500;
		
		double timeCorrector=1;
		if (timeUnit){
			timeCorrector=0.001;
		}
		
		while (xtick<xspace+tnx){
			//Integer p=new Integer((int)Math.round(0.001*xm));
			if (showTimeTickMarkLabels){
				Double p=new Double(timeCorrector*xm);
				String pl=p.toString();
				int xpl=xt-(fm.stringWidth(pl)/2);
				if (xpl<xspace){
					xpl=xspace;
				}
				h.drawString(pl, xpl, yspace+tny+heightSpace);
			}
			if (showMajorTimeTickMarks){
				if (interiorTickMarks){
					h.drawLine(xt, yspace+tny+1, xt, yspace+tny-majorTickMarkLength);
				}
				else{
					h.drawLine(xt, yspace+tny+1, xt, yspace+tny+majorTickMarkLength);
				}
			}
			xtick+=500/tdx;
			xt=(int)Math.round(xtick);
			xm+=500;
		}
		
		h.setFont(timeLabelFont);
		fm=h.getFontMetrics(timeLabelFont);
		if (timeUnit){
			h.drawString("Time (s)", (tnx/2)+xspace, yspace+tny+heightSpace+fm.getMaxAscent()+2);
		}
		else{
			h.drawString("Time (ms)", (tnx/2)+xspace, yspace+tny+heightSpace+fm.getMaxAscent()+2);
		}
		h.dispose();
	}	
	
	
	void paintAmpEnvelope(){
		BufferedImage imt=new BufferedImage(tnx, song.ny, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=imt.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.WHITE);
		g.fillRect(0,0, tnx, song.ny);
		g.setColor(Color.BLACK);
		
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
		//AlphaComposite ac2 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
		BasicStroke bs1=new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		BasicStroke bs2=new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		BasicStroke bs3=new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);		
		int corr=(int)Math.round(0.5*song.frameLength/song.timeStep);
		int[] sig=new int[tnx];
		g.setStroke(bs1);
		if (im!=null){
			if ((song.eleList!=null)&&(viewParameters[1])&&(viewParameters[0])){
				
				for (int i=0; i<song.eleList.size(); i++){
					Element ele=(Element)song.eleList.get(i);
					if ((ele.length>0)&&(ele.signal[ele.length-1][0]>currentMinX)&&(ele.signal[0][0]<currentMaxX)){
						int startX=tnx*(ele.signal[0][0]+corr-currentMinX)/(currentMaxX-currentMinX);
						if (startX<0){startX=0;}
						int endX=tnx*(ele.signal[ele.length-2][0]+corr-currentMinX)/(currentMaxX-currentMinX);
						if (endX>tnx){endX=tnx;}
						for (int j=startX; j<endX; j++){
							sig[j]=1;
						}
					}
				}
			}
		}
		
		double max=0;
		
		if (envelope[0].length==2){
			
			float maxAmp=-1000;
			float minAmp=1000;
			for (int i=0; i<envelope.length; i++){
				if (envelope[i][0]>maxAmp){
					maxAmp=envelope[i][0];
				}
				if (envelope[i][1]<minAmp){
					minAmp=envelope[i][1];
				}
			}
			
			max=Math.max(maxAmp, Math.abs(minAmp));
			
			double p=(int)Math.ceil(Math.log(max)/Math.log(10));
			max=Math.pow(10, p);
			
			double max2=0.5/max;
			
			for (int i=0; i<envelope.length; i++){
				int start=(int)Math.round(((envelope[i][0]*max2)+0.5)*ny);
				int end=(int)Math.round(((envelope[i][1]*max2)+0.5)*ny);
				if ((viewParameters[1])&&(sig[i]==1)){
					g.setColor(elementColor);
				}
				else{
					g.setColor(Color.BLACK);
				}
				g.drawLine(i, start, i, end);
			}
		}
		else{
			double ratio=tnx/(envelope.length+0.0);
			
			float maxAmp=-1000;
			float minAmp=1000;
			for (int i=0; i<envelope.length; i++){
				if (envelope[i][0]>maxAmp){
					maxAmp=envelope[i][0];
				}
				if (envelope[i][0]<minAmp){
					minAmp=envelope[i][0];
				}
			}
			
			max=Math.max(maxAmp, Math.abs(minAmp));
			double p=(int)Math.ceil(Math.log(max)/Math.log(10));
			max=Math.pow(10, p);
			
			double max2=0.5/max;
			
			for (int i=0; i<envelope.length-1; i++){
				int start=(int)Math.round(((envelope[i][0]*max2)+0.5)*ny);
				int end=(int)Math.round(((envelope[i+1][0]*max2)+0.5)*ny);
				
				int sx=(int)Math.round(i*ratio);
				int ex=(int)Math.round((i+1)*ratio);
				
				if ((viewParameters[1])&&(sig[(sx+ex)/2]==1)){
					g.setColor(elementColor);
				}
				else{
					g.setColor(Color.BLACK);
				}
				
				g.drawLine(sx, start, ex, end);
			}
		}

		paintFrameAmp(max);

		int i, j, k, sx, sy, ex;
		if (im!=null){
			if ((song.eleList!=null)&&(viewParameters[1])){
				g.setStroke(bs1);
				for (i=0; i<song.eleList.size(); i++){
					
					Element ele=(Element)song.eleList.get(i);
					
					
					if ((ele.length>0)&&(ele.signal[ele.length-1][0]>currentMinX)&&(ele.signal[0][0]<currentMaxX)){
						
						sx=tnx*(ele.signal[0][0]-currentMinX)/(currentMaxX-currentMinX);
						
						double correct=1/(song.frameSize*48.0);
						int [][] eleholder=new int[ele.length][16];
						for (j=0; j<ele.length; j++){
							int jj=j+5;
							for (k=0; k<4; k++){
								eleholder[j][k]=(int)Math.round(ny-(ele.measurements[jj][k]/ele.dy)-1);
							}
							for (k=4; k<8; k++){
								eleholder[j][k]=(int)(ny-(ele.measurements[jj][k]*ny));
							}
							eleholder[j][8]=(int)(ele.measurements[jj][8]*ny);
							if (eleholder[j][8]<0){eleholder[j][8]=0;}
							eleholder[j][8]=ny-eleholder[j][8];
							eleholder[j][9]=(int)(-0.5*ele.measurements[jj][9]*ny);
							//eleholder[j][9]=(int)(ny-((10+ele.measurements[jj][9])*0.1*ny));
							if (eleholder[j][9]>=ny){eleholder[j][9]=ny-1;}
							eleholder[j][10]=(int)Math.round(ele.measurements[jj][10]/ele.dy);
							eleholder[j][11]=(int)Math.round((ele.measurements[jj][11]*correct*ny));
							eleholder[j][12]=(int)Math.round(ny-(ele.measurements[jj][12]*0.01*ny));
							eleholder[j][13]=(int)Math.round(ny-((ele.measurements[jj][13])*0.125*ny));
							if (eleholder[j][13]>=ny){eleholder[j][13]=ny-1;}
							eleholder[j][14]=(int)Math.round(ny-(ele.measurements[jj][14]*0.125*ny));
						}	
						
						
						for (j=1; j<ele.length; j++){
							ex=tnx*(ele.signal[j][0]-currentMinX)/(currentMaxX-currentMinX);
							if ((sx>0)&&(ex<currentMaxX)){
								
								g.setComposite(ac);
								g.setStroke(bs2);
								
								int j1=j-1;
								if (viewParameters[4]){
									g.setColor(peakFreqColor);
									g.drawLine(sx, eleholder[j1][0], ex, eleholder[j][0]);
								}
								if (viewParameters[5]){
									g.setColor(fundFreqColor);
									g.drawLine(sx, eleholder[j1][3], ex, eleholder[j][3]);
								}
								if (viewParameters[6]){
									g.setColor(meanFreqColor);
									g.drawLine(sx, eleholder[j1][1], ex, eleholder[j][1]);
								}
								if (viewParameters[7]){
									g.setColor(medianFreqColor);
									g.drawLine(sx, eleholder[j1][2], ex, eleholder[j][2]);
								}
								if (viewParameters[8]){
									g.setColor(peakChangeColor);
									g.drawLine(sx, eleholder[j1][4], ex, eleholder[j][4]);
								}
								if (viewParameters[9]){
									g.setColor(fundChangeColor);
									g.drawLine(sx, eleholder[j1][7], ex, eleholder[j][7]);
								}
								if (viewParameters[10]){
									g.setColor(meanChangeColor);
									g.drawLine(sx, eleholder[j1][5], ex, eleholder[j][5]);
								}
								if (viewParameters[11]){
									g.setColor(medianChangeColor);
									g.drawLine(sx, eleholder[j1][6], ex, eleholder[j][6]);
								}
								if (viewParameters[12]){
									g.setColor(harmonicityColor);
									g.drawLine(sx, eleholder[j1][8], ex, eleholder[j][8]);
								}
								if (viewParameters[13]){
									g.setColor(wienerColor);
									g.drawLine(sx, eleholder[j1][9], ex, eleholder[j][9]);
								}
								if (viewParameters[14]){
									g.setColor(bandwidthColor);
									g.drawLine(sx, eleholder[j1][10], ex, eleholder[j][10]);
								}
								if (viewParameters[15]){
									g.setColor(amplitudeColor);
									g.drawLine(sx, eleholder[j1][11], ex, eleholder[j][11]);
								}
								if (viewParameters[16]){
									g.setColor(reverbColor);
									g.drawLine(sx, eleholder[j1][12], ex, eleholder[j][12]);
								}
								if (viewParameters[17]){
									g.setColor(trillAmpColor);
									g.drawLine(sx, eleholder[j1][13], ex, eleholder[j][13]);
								}
								if (viewParameters[18]){
									g.setColor(trillRateColor);
									g.drawLine(sx, eleholder[j1][14], ex, eleholder[j][14]);
								}
							}
							sx=ex;
						}
					}
				}
			}
		}		
		
		

		g.dispose();
		Graphics2D h=imf.createGraphics();
		h.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		h.drawImage(imt, xspace+1, yspace+1, this);
			
		h.setColor(Color.WHITE);
		h.setComposite(ac);
		h.fillRect(xspace, yspace-51, tnx, 50);
		h.setComposite(ac);
		if ((song.eleList!=null)&&(viewParameters[1])){
			h.setStroke(bs1);
			for (i=0; i<song.eleList.size(); i++){
				Element ele=(Element) song.eleList.get(i);
				if ((ele.length>0)&&(ele.signal[ele.length-1][0]>currentMinX)&&(ele.signal[0][0]<currentMaxX)){
					sy=yspace-1;
					double av=(ele.signal[0][0]+ele.signal[ele.length-1][0])*0.5;
					sx=(int)Math.round((((av*ele.timeStep/dx)-currentMinX)*stretchX)+xspace);
					if ((sx>xspace)&&(sx<tnx)){
						h.setColor(elementColor);
						Integer p=new Integer(i+1);
						String pl=p.toString();
						h.drawString(pl, sx, sy);
					}
				}
			}
		}
		if ((song.syllList!=null)&&(viewParameters[2])){
			h.setStroke(bs3);
			h.setColor(syllableColor);
			h.setComposite(ac);
			int shift1=22;
			int shift2=4;
			
			for (i=0; i<song.syllList.size(); i++){
				int[] ele=(int[])song.syllList.get(i);
				
				int x1=(int)Math.round(((ele[0]/dx)-currentMinX)*stretchX);
				int x2=(int)Math.round(((ele[1]/dx)-currentMinX)*stretchX);
				if ((x2>0)&&(x1<tnx)){
					if (x1<0){x1=0;}
					if (x2>tnx){x2=tnx;}
					int shift=shift1;
					for (j=0; j<song.syllList.size(); j++){
						int[] syll=(int[])song.syllList.get(j);
						int p=(syll[0]+syll[1])/2;
						if ((ele[0]<p)&&(ele[1]>p)&&(ele[1]-ele[0]>syll[1]-syll[0])){
							shift=shift2;
							j=song.syllList.size();
						}
					}
					
					if (shift==shift2){
						if (shift==4){
							shift+=2;
							shift2=shift;
						}
						else{
							shift-=2;
							shift2=shift;
						}
					}
					else{
						if (shift==22){
							shift-=2;
							shift1=shift;
						}
						else{
							shift+=2;
							shift1=shift;
						}
					}
					
					
					
					
					x2+=xspace;
					x1+=xspace;
					int x3=(int)Math.round(0.5*(x1+x2)-5);
					h.setColor(syllableColor);
					h.fillRect(x1, shift, (x2-x1), 12);
					h.setColor(Color.WHITE);
					Integer p=new Integer(i+1);
					String pl=p.toString();
					h.drawString(pl, x3, shift+11);
					
					//h.drawLine(x1, shift, x2, shift);
					//if (shift==16){shift=14;}
					//else{shift=16;}
					//Integer p=new Integer(i+1);
					//String pl=p.toString();
					//h.drawString(pl, x3, 12);
				}
			}
		}
		h.dispose();
		repaint();

	}
	
	
	void paintFound(){
	
		if (displayMode==1){
			paintAmpEnvelope();
		}
		else {
			paintSpectrogram();
		}
	}
	
	
	void paintSpectrogram(){
	
		double logCorrect=1/Math.log(2);
		double pm=Math.log(minFreq*song.dy)*logCorrect;
		double pf=Math.log(ny*song.dy)*logCorrect;
		double pe=(pf-pm)/(ny+0.0);
		
		BufferedImage imt=new BufferedImage(unx, song.ny, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=imt.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		
		g.drawImage(im, 0, 0, this);
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
		AlphaComposite ac2 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
		int inv=(int)Math.ceil(1/stretchY);
		BasicStroke bs1=new BasicStroke(1.0f*inv, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		BasicStroke bs2=new BasicStroke(1.0f*inv, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		BasicStroke bs3=new BasicStroke(2.0f*inv, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		int i, j, k, sx, sy, ex, ty, by;
		if (im!=null){
			if ((song.eleList!=null)&&(viewParameters[1])){
				g.setStroke(bs1);
				for (i=0; i<song.eleList.size(); i++){
					
					Element ele=(Element)song.eleList.get(i);
					
					
					if ((ele.length>0)&&(ele.signal[ele.length-1][0]>currentMinX)&&(ele.signal[0][0]<currentMaxX)){
						
						sx=ele.signal[0][0]-currentMinX;
						double correct=1/(song.frameSize*48.0);
						int [][] eleholder=new int[ele.length][16];
						for (j=0; j<ele.length; j++){
							int jj=j+5;
							for (k=0; k<4; k++){
								
								double m1=ele.measurements[jj][k]/dy;
								if (displayMode==2){
									double m2=((Math.log(ele.measurements[jj][k])*logCorrect)-pm)/pe;
									m1=m2;
								}
								eleholder[j][k]=(int)Math.round(ny-m1-1);
							}
	
							for (k=4; k<8; k++){
								eleholder[j][k]=(int)(ny-(ele.measurements[jj][k]*ny));
							}
							eleholder[j][8]=(int)(ele.measurements[jj][8]*ny);
							if (eleholder[j][8]<0){eleholder[j][8]=0;}
							eleholder[j][8]=ny-eleholder[j][8];
							eleholder[j][9]=(int)(ele.measurements[jj][9]);
							//eleholder[j][9]=(int)(ny-((10+ele.measurements[jj][9])*0.1*ny));
							if (eleholder[j][9]>=ny){eleholder[j][9]=ny-1;}
							eleholder[j][10]=(int)Math.round(ele.measurements[jj][10]/ele.dy);
							eleholder[j][11]=(int)Math.round((ele.measurements[jj][11]*correct*ny));
							eleholder[j][12]=(int)Math.round(ny-(ele.measurements[jj][12]*ny));
							//eleholder[j][13]=(int)Math.round(ny-((ele.measurements[jj][13])*0.0001*ny));
							//if (eleholder[j][13]>=ny){eleholder[j][13]=ny-1;}
							eleholder[j][13]=(int)Math.round(ny-((Math.log(Math.max(1, ele.measurements[jj][13])))*0.02*ny));
							eleholder[j][14]=(int)Math.round(ny-(ele.measurements[jj][14]*(0.02)*ny));
						}	
						
						if (viewParameters[0]){
							g.setColor(elementColor);
							g.setComposite(ac2);
						
							for (j=0; j<ele.length; j++){
								ex=ele.signal[j][0]-currentMinX;
								if ((ex>0)&&(ex<currentMaxX)){								
									int st=1;
									while ((st<ele.signal[j].length-1)&&(ele.signal[j][st]!=0)){
										ty=ele.signal[j][st];
										by=ele.signal[j][st+1];
										if (displayMode==2){
											double tya=(ny-ty-1)*dy;
											double bya=(ny-by-1)*dy;
											double m1=((Math.log(tya)*logCorrect)-pm)/pe;
											double m2=((Math.log(bya)*logCorrect)-pm)/pe;
											ty=(int)Math.round(ny-m1-1);
											by=(int)Math.round(ny-m2-1);
										}
										g.drawLine(ex, ty, ex, by);
										st+=2;
									}
								}
							}
						}
								
						g.setComposite(ac);
						g.setStroke(bs2);
						for (j=1; j<ele.length; j++){
							ex=ele.signal[j][0]-currentMinX;
							if ((sx>0)&&(ex<currentMaxX)){
								int j1=j-1;
								if (viewParameters[4]){
									g.setColor(peakFreqColor);
									g.drawLine(sx, eleholder[j1][0], ex, eleholder[j][0]);
								}
								if (viewParameters[5]){
									g.setColor(fundFreqColor);
									g.drawLine(sx, eleholder[j1][3], ex, eleholder[j][3]);
								}
								if (viewParameters[6]){
									g.setColor(meanFreqColor);
									g.drawLine(sx, eleholder[j1][1], ex, eleholder[j][1]);
								}
								if (viewParameters[7]){
									g.setColor(medianFreqColor);
									g.drawLine(sx, eleholder[j1][2], ex, eleholder[j][2]);
								}
								if (viewParameters[8]){
									g.setColor(peakChangeColor);
									g.drawLine(sx, eleholder[j1][4], ex, eleholder[j][4]);
								}
								if (viewParameters[9]){
									g.setColor(fundChangeColor);
									g.drawLine(sx, eleholder[j1][7], ex, eleholder[j][7]);
								}
								if (viewParameters[10]){
									g.setColor(meanChangeColor);
									g.drawLine(sx, eleholder[j1][5], ex, eleholder[j][5]);
								}
								if (viewParameters[11]){
									g.setColor(medianChangeColor);
									g.drawLine(sx, eleholder[j1][6], ex, eleholder[j][6]);
								}
								if (viewParameters[12]){
									g.setColor(harmonicityColor);
									g.drawLine(sx, eleholder[j1][8], ex, eleholder[j][8]);
								}
								if (viewParameters[13]){
									g.setColor(wienerColor);
									g.drawLine(sx, eleholder[j1][9], ex, eleholder[j][9]);
								}
								if (viewParameters[14]){
									g.setColor(bandwidthColor);
									g.drawLine(sx, eleholder[j1][10], ex, eleholder[j][10]);
								}
								if (viewParameters[15]){
									g.setColor(amplitudeColor);
									g.drawLine(sx, eleholder[j1][11], ex, eleholder[j][11]);
								}
								if (viewParameters[16]){
									g.setColor(reverbColor);
									g.drawLine(sx, eleholder[j1][12], ex, eleholder[j][12]);
								}
								if (viewParameters[17]){
									g.setColor(trillAmpColor);
									g.drawLine(sx, eleholder[j1][13], ex, eleholder[j][13]);
								}
								if (viewParameters[18]){
									g.setColor(trillRateColor);
									g.drawLine(sx, eleholder[j1][14], ex, eleholder[j][14]);
								}
							}
							sx=ex;
						}
					}
				}
			}
		}		
		
		

		g.dispose();
		
		Graphics2D h=imf.createGraphics();
		h.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		h.fillRect(0, 0, nnx, nny);
		if ((stretchX!=1)||(stretchY!=1)){
			h.drawImage(imt, xspace+1, yspace+1, tnx, tny, this);
			imt=null;
		}
		else{
			h.drawImage(imt, xspace+1, yspace+1, this);
			imt=null;
		}
		h.setColor(Color.WHITE);
		h.setComposite(ac);
		//h.fillRect(xspace, yspace-51, unx, 50);
		
		
		
		h.setComposite(ac);
		if ((song.eleList!=null)&&(viewParameters[1])){
			h.setStroke(fs);
			int disp=yspace/10;
			for (i=0; i<song.eleList.size(); i++){
				Element ele=(Element) song.eleList.get(i);
				if ((ele.length>0)&&(ele.signal[ele.length-1][0]>currentMinX)&&(ele.signal[0][0]<currentMaxX)){
					
					if (unitStyle==0){
						sy=yspace-1;
						double av=(ele.signal[0][0]+ele.signal[ele.length-1][0])*0.5;
						sx=(int)Math.round((((av*ele.timeStep/dx)-currentMinX)*stretchX)+xspace);
						if ((sx>xspace)&&(sx<tnx)){
							h.setColor(elementColor);
							Integer p=new Integer(i+1);
							String pl=p.toString();
							h.drawString(pl, sx, sy);
						}
					}
					else if (unitStyle==1){
						sy=yspace-disp;
						if (disp==yspace/10){disp=yspace/7;}
						else{disp=yspace/10;}
						int sx1=(int)Math.round((((ele.signal[0][0]*ele.timeStep/dx)-currentMinX)*stretchX)+xspace);
						int sx2=(int)Math.round((((ele.signal[ele.length-1][0]*ele.timeStep/dx)-currentMinX)*stretchX)+xspace);
						if ((sx1>xspace)&&(sx2<tnx)){
							h.setColor(Color.BLACK);
							h.drawLine(sx1, sy, sx2, sy);
						}
					}
				}
			}
		}
		//System.out.println("DRAWING: "+viewParameters[2]);
		if ((song.syllList!=null)&&(viewParameters[2])){
			
			
			
			
			if (unitStyle==0){
				h.setStroke(fs);
				h.setColor(syllableColor);
				h.setComposite(ac);
				int shift1=22;
				int shift2=4;
			
				for (i=0; i<song.syllList.size(); i++){
					int[] ele=(int[])song.syllList.get(i);
				
					int x1=(int)Math.round(((ele[0]/dx)-currentMinX)*stretchX);
					int x2=(int)Math.round(((ele[1]/dx)-currentMinX)*stretchX);
					if ((x2>0)&&(x1<tnx)){
						if (x1<0){x1=0;}
						if (x2>tnx){x2=tnx;}
						int shift=shift1;
						for (j=0; j<song.syllList.size(); j++){
							int[] syll=(int[])song.syllList.get(j);
							int p=(syll[0]+syll[1])/2;
							if ((ele[0]<p)&&(ele[1]>p)&&(ele[1]-ele[0]>syll[1]-syll[0])){
								shift=shift2;
								j=song.syllList.size();
							}
						}
					
						if (shift==shift2){
							if (shift==4){
								shift+=2;
								shift2=shift;
							}
							else{
								shift-=2;
								shift2=shift;
							}
						}
						else{
							if (shift==22){
								shift-=2;
								shift1=shift;
							}
							else{
								shift+=2;
								shift1=shift;
							}
						}
					
						x2+=xspace;
						x1+=xspace;
						int x3=(int)Math.round(0.5*(x1+x2)-5);
						h.setColor(syllableColor);
						h.fillRect(x1, shift, (x2-x1), 12);
						h.setColor(Color.WHITE);
						Integer p=new Integer(i+1);
						String pl=p.toString();
						h.drawString(pl, x3, shift+11);
					}
				}
			}
			else if (unitStyle==1){
				
				h.setStroke(fs);
				h.setColor(Color.BLACK);
				
				int[] avs=new int[song.eleList.size()];
				int[] starts=new int[song.eleList.size()];
				int[] ends=new int[song.eleList.size()];
				
				for (i=0; i<song.eleList.size(); i++){
					Element ele=(Element) song.eleList.get(i);
					int sx1=(int)Math.round((((ele.signal[0][0]*ele.timeStep/dx)-currentMinX)*stretchX));
					int sx2=(int)Math.round((((ele.signal[ele.length-1][0]*ele.timeStep/dx)-currentMinX)*stretchX));
					avs[i]=(sx1+sx2)/2;
					starts[i]=sx1;
					ends[i]=sx2;
				}
				
				for (i=0; i<song.syllList.size(); i++){
					int[] ele=(int[])song.syllList.get(i);
					int x1=(int)Math.round(((ele[0]/dx)-currentMinX)*stretchX);
					int x2=(int)Math.round(((ele[1]/dx)-currentMinX)*stretchX);

					int xs=100000;
					int xf=-100000;
					for (j=0; j<avs.length; j++){
						if ((avs[j]>x1)&&(avs[j]<x2)){
							
							if(starts[j]<xs){xs=starts[j];}
							if (ends[j]>xf){xf=ends[j];}
						}
					}
					
					if ((xs>0)&&(xf<tnx)){
					
						sy=yspace*7/10;
					
						for (j=0; j<song.syllList.size(); j++){
							int[] syll=(int[])song.syllList.get(j);
							int p=(syll[0]+syll[1])/2;
							if ((ele[0]<p)&&(ele[1]>p)&&(ele[1]-ele[0]>syll[1]-syll[0])){
								sy=yspace/2;
								j=song.syllList.size();
							}
						}
					
						h.drawLine(xs+xspace, sy, xf+xspace, sy);
					}
				}
			}
		}
		h.dispose();
		if (displayMode==0){
			paintFrame();
		}
		else if (displayMode==2){
			paintFrameLogarithmic();
		}
		paintXAxisLabels();
		repaint();
	}
	
	public void paintComponent(Graphics g) {
	
        super.paintComponent(g);  //paint background
		
		if (paintOver){
		
		if (bounds<2){
			g.drawImage(imf, 0, 0, this);
			Graphics2D g2=(Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	                RenderingHints.VALUE_ANTIALIAS_ON);
			/*if (pointList!=null){
				g2.setColor(Color.RED);
				
				g2.setComposite(ac);
				
				for (int i=minPressedX; i<=maxPressedX; i++){
					g2.drawLine(i+xspace, pList[i][0]+yspace, i+xspace, pList[i][1]+yspace);
				}
			}*/

			if (bounds==1){
				g2.drawImage(imf, 0, 0, this);
				g2.setColor(Color.BLACK);
				g2.setComposite(ac);
				g2.drawLine(newx+xspace, yspace, newx+xspace, yspace+tny);
				g2.drawLine(xspace, newy+yspace, tnx+xspace, newy+yspace);
				g2.setColor(Color.WHITE);
				
				g2.setComposite(ac2);
				g2.fillRect(xspace-40, newy+yspace-10, 35, 20);
				g2.fillRect(newx+xspace-5, yspace+tny+3, 35, 15);
				g2.setColor(Color.RED);
				if (displayMode==0){
					int ym=(int)Math.round((tny-newy+1)*tdy);
					Integer p=new Integer(ym);
					String pl=p.toString();
					g2.drawString(pl, xspace-40, newy+yspace+5);
				}
				else if (displayMode==1){
					int max=(int)Math.pow(10, -1*maxsl);
					double ym=2*((0.5*tny)-newy+1)/(tny+0.0)*max;
					Double p=new Double(ym);
					String pl=p.toString();
					if (pl.length()>5){
						pl=pl.substring(0, 5);
					}
					g2.drawString(pl, xspace-40, newy+yspace+5);
				}
				else if (displayMode==2){
				
					double logCorrect=1/Math.log(2);
					double pm=Math.log(minFreq*song.dy)*logCorrect;
					double pf=Math.log(ny*song.dy)*logCorrect;
					double pe=(pf-pm)/(tny+0.0);
					int ym=(int)Math.round(Math.pow(2, pm+((tny-newy+1)*pe)));
					Integer p=new Integer(ym);
					String pl=p.toString();
					g2.drawString(pl, xspace-40, newy+yspace+5);
				}		
				int xm=(int)Math.round((newx+(currentMinX/stretchX))*tdx);
				Integer p=new Integer(xm);
				String pl=p.toString();
				g2.drawString(pl,  newx+xspace-3, yspace+tny+15);
			}
			g2.dispose();
			imf.flush();
		}
		else{
			g.drawImage(imf, 0, 0, this);
			g.setColor(Color.BLACK);
			g.drawLine(soundposition+xspace-currentMinX, yspace, soundposition+xspace-currentMinX, yspace+tny);
		}
		
		readytopaint=true;
		}
	}
	
	        //Methods required by the MouseInputListener interface.
	public void mouseClicked(MouseEvent e) { 
	
			}

	public void mouseMoved(MouseEvent e) {
		if(started){
			oldx=newx;
			oldy=newy;
			newx = e.getX()-xspace;
			newy = e.getY()-yspace;
			   //if you drag the cursor off the spectrogram area, element is finshed
			if ((newx>tnx)||(newy>tny)||(newx<0)||(newy<0)){
				started=false;
				if (syllable){updateSyllable();}
				else{updateElement();}
			}
			else{
				updatePoint();
			}
		}
		
		newx = e.getX()-xspace;
		newy = e.getY()-yspace;
		if ((newx<tnx)&&(newy<tny)&&(newx>0)&&(newy>0)){bounds=1;}
		else{bounds=0;}
		repaint();
	}

	public void mouseExited(MouseEvent e) { 
		this.setCursor(Cursor.getDefaultCursor());
		//updateElement();
	}

	public void mouseReleased(MouseEvent e) {
		//updateElement();
	}
	
	public void mouseEntered(MouseEvent e) { 
		if(customCursor){
			this.setCursor(cur);
		}
		else{
			this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		}
	}
	public void mousePressed(MouseEvent e) { 
		if (editable){
			if (started){
				started=false;
				if (syllable){updateSyllable();}
				//else{updateElement();}
				else{updateElement();}
			}
			else{
				started=true;
				newx = e.getX()-xspace;
				newy = e.getY()-yspace;
				if ((newx<tnx)&&(newy<tny)){
					pointList=new int[unx][2];
					pList=new int[tnx][2];
					
					minPressedX=100000;
					maxPressedX=-1;
					
					oldx=newx;
					oldy=newy;
					updatePoint();
				}
				repaint();
			}
		}

	}
	public void mouseDragged(MouseEvent e) { 
		
	}
	
	public void updatePList(int x, int miny, int maxy){
		
		Graphics2D g2=imf.createGraphics();
		g2.setColor(Color.RED);
		g2.setComposite(ac);
		
		
		int startx=x;
		int endx=x;
		if (stretchX!=1){
			startx=(int)Math.round((x-0.5)*stretchX);
			if (startx<0){startx=0;}
			endx=(int)Math.round((x+0.5)*stretchX);
			if (endx>=tnx){endx=tnx-1;}
		}
		if (stretchY!=1){
			miny=(int)Math.round(miny*stretchY);
			maxy=(int)Math.round(maxy*stretchY);
		}
		int i2;
		for (int i=startx; i<=endx; i++){
			i2=i+xspace;
			if (pList[i][0]==0){
				pList[i][0]=miny;
				pList[i][1]=maxy;
				g2.drawLine(i2, miny+yspace, i2, maxy+yspace);				
			}
			else {
				if (pList[i][0]>miny){
					g2.drawLine(i2, miny+yspace, i2, pList[i][0]-1+yspace);
					pList[i][0]=miny;
				}
				if (pList[i][1]<maxy){
					g2.drawLine(i2, pList[i][1]+1+yspace, i2, maxy+yspace);
					pList[i][1]=maxy;
				}
			}
		}
		g2.dispose();
	}
	
	
	public void updatePoint(){					//updates the list of points for the element the user is currently selecting
		
		if (pointList!=null){
			int maxy=2;
			int miny=1;
			
			if (song.brushType==1){
				maxy=(int)Math.round((newy+song.brushSize)/stretchY);			//maxy and miny are the current positions of the cursor
				if (maxy>=ny){maxy=ny-1;}
				miny=(int)Math.round((newy-song.brushSize)/stretchY);
				if (miny<=0){miny=1;}
			}
			else if (song.brushType==2) {
				maxy=ny-1;
				miny=1;
			}
			else if (song.brushType==3) {
				double mf=ny/(song.maxf+0.0);
				maxy=(int)Math.round(song.maxBrush*mf);
				if (maxy>=ny){maxy=ny-1;}
				miny=(int)Math.round(song.minBrush*mf);
				if (miny>=maxy){maxy=ny-1;}
				if (miny<=0){miny=1;}
				maxy=ny-maxy;
				miny=ny-miny;
				//System.out.println(song.maxBrush+" "+song.minBrush+" "+song.maxf+" "+ny+" "+maxy+" "+miny);
			}
			int x=(int)Math.round(newx/stretchX);
			int ox=(int)Math.round(oldx/stretchX);
			//this next section updates pointList at the point in time the cursor is at
			
			
						
			if (pointList[x][0]==0){
				pointList[x][0]=miny;		
				pointList[x][1]=maxy;
								
				updatePList(x, miny, maxy);
			}
			else{
				if (pointList[x][0]>miny){
					pointList[x][0]=miny;
					updatePList(x, miny, maxy);
					
				}
				if (pointList[x][1]<maxy){
					pointList[x][1]=maxy;
					updatePList(x, miny, maxy);
				}
			}
			
			//the next section is necessary to update pointList between where you were last and where you are now (if you move the cursor quickly)
			
			
			if (x>ox+1){
				double distx=x-ox;
				double disty=(newy-oldy)/stretchY;
				double oy=oldy/stretchY;
				int brushAdj=(int)Math.round(song.brushSize/stretchY);
				for (int i=ox+1; i<x; i++){
					if (song.brushType==1){
						int y=(int)Math.round(((i-ox)/distx)*disty+oy);
						maxy=y+brushAdj;
						if (maxy>=ny){maxy=ny-1;}
						miny=y-brushAdj;
						if (miny<=0){miny=1;}
						if (pointList[i][0]==0){
							pointList[i][0]=miny;
							pointList[i][1]=maxy;
							updatePList(i, miny, maxy);
						}
						else{
							if (pointList[i][0]>miny){
								pointList[i][0]=miny;
								updatePList(i, miny, maxy);
							}
							if (pointList[i][1]<maxy){
								pointList[i][1]=maxy;
								updatePList(i, miny, maxy);
							}
						}
					}
					else if (song.brushType==2) {
						pointList[i][0]=1;
						pointList[i][1]=ny-1;
						updatePList(i, miny, maxy);
					}
					else if (song.brushType==3) {
						pointList[i][0]=1;
						pointList[i][1]=ny-1; 
						updatePList(i, miny, maxy);
					}
				}
				maxPressedX=x+1;
				minPressedX=ox;
			}
			if (ox>x+1){
				double distx=ox-x;
				double disty=(newy-oldy)/stretchY;
				double oy=oldy/stretchY;
				int brushAdj=(int)Math.round(song.brushSize/stretchY);
				for (int i=x+1; i<ox; i++){
					if (song.brushType==1){
						int y=(int)Math.round(((ox-i)/distx)*disty+oy);
						maxy=y+brushAdj;
						if (maxy>=ny){maxy=ny-1;}
						miny=y-brushAdj;
						if (miny<=0){miny=1;}
						if (pointList[i][0]==0){
							pointList[i][0]=miny;
							pointList[i][1]=maxy;
							updatePList(i, miny, maxy);
						}
						else{
							if (pointList[i][0]>miny){
								pointList[i][0]=miny;
								updatePList(i, miny, maxy);
							}
							if (pointList[i][1]<maxy){
								pointList[i][1]=maxy;
								updatePList(i, miny, maxy);
							}
						}
					}
					else if (song.brushType==2) {
						pointList[i][0]=1;
						pointList[i][1]=ny-1;
						updatePList(i, miny, maxy);
					}
					else if (song.brushType==3) {
						pointList[i][0]=1;
						pointList[i][1]=ny-1;
						updatePList(i, miny, maxy); 
					}
				}
				maxPressedX=ox;
				minPressedX=x+1;
			}
		}
	}
	
	public void clearUp(){
		imf=null;
		im=null;
		colpal=null;
		harm=null;
		pointList=null;
		lastPointList=null;
		pList=null;
		nout=null;
		phase=null;
		
		archivePointList=null;
		archiveLastElementsAdded=null;
		oldPList=null;
		envelope=null;
		gp=null;
		point=null;
		cur=null;
		
			
	}
	
}
