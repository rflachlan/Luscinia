package lusc.net.github.ui.spectrogram;
//
//  SpectrPane.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/10/2005.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.


import java.awt.geom.AffineTransform;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.*;

import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import lusc.net.github.Defaults;
import lusc.net.github.Element;
import lusc.net.github.Song;
import lusc.net.github.SpectrogramMeasurement;
import lusc.net.github.Syllable;
import lusc.net.github.ui.statistics.DisplayPane;

public class SpectrPane extends DisplayPane implements MouseListener, MouseMotionListener{

	/**
	 * 
	 */
	
	
	public boolean showBeamFormOffset=false;
	
	private static final long serialVersionUID = -87325727540762828L;
	BufferedImage imf, im, imf2;
	boolean started=false;
	boolean displayEdge=false;
	boolean editable=true;
	boolean syllable=false;
	
	boolean customCursor=false;
	boolean automaticMerge=false;
	boolean paintOver=true;
	boolean clickDrag=false;
	boolean moving=false;
	boolean erase=false;
	boolean findHarmonics=false;
	
	boolean editMode=true;
	boolean showDirection=false;
			
	boolean[]viewParameters=new boolean[19];
	
	boolean readyToUpdateGP=false;
	
	int counterTest=0;
	
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
	int labelFontSize=12;
	
	String fontFace="Arial";
	
	int tickLabelFontStyle=Font.PLAIN;
	int axisLabelFontStyle=Font.PLAIN;
	int labelFontStyle=Font.PLAIN;
	
	Font timeAxisFont=new Font(fontFace, tickLabelFontStyle, tickLabelFontSize);
	Font freqAxisFont=new Font(fontFace, tickLabelFontStyle, tickLabelFontSize);	
	Font timeLabelFont=new Font(fontFace, axisLabelFontStyle, axisLabelFontSize);
	Font freqLabelFont=new Font(fontFace, axisLabelFontStyle, axisLabelFontSize);
	Font eleLabelFont=new Font(fontFace, labelFontStyle, labelFontSize);
	Font syllLabelFont=new Font(fontFace, labelFontStyle, labelFontSize);
	
	
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
	
	LinkedList<int[]> eleLocList=new LinkedList<int[]>();
	LinkedList<int[]> syllLocList=new LinkedList<int[]>();
	
	double multiplier=1.0;
	BasicStroke fs=new BasicStroke((float)(lineWeight), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
	
	double stretchX=1;
	double stretchY=1;
	int compressYToFit=0;
	int compressXToFit=0;
	
	
	int ny2, brushAdj, bt;
	double maxyp, minyp;
	
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
	int nnx2, nny2;
	int tnx=0;
	int unx=0;
	int oldx=0;
	int oldy=0;
	int newx=0;
	int newy=0;
	
	int currentMinX=0;
	int currentMaxX=0;
	int archiveMinX=0;
	int archiveMaxX=0;
	int xspace=75;
	int xspace2=10;
	int yspace=150;
	int maxsl=0;
	double dx,dy, tdx, tdy;
	int nx, ny, tny;
	int minPressedX=0;
	int maxPressedX=0;
	int colpal[];
	double harm[][];
	int[] pointList1, pointList2=null;
	int[] archivePointList1, archivePointList2=null;
	int[] archiveLastElementsAdded=null;
	int[] lastPointList1, lastPointList2=null;
	int[] pList1, pList2=null;
	int[] oldPList1, oldPList2=null;
	float [][] nout, envelope, phase;
	double[] directionVector;
	
	
	GuidePanel gp;
	Song song;
	SpectrogramMeasurement songMeas;
	
	double upper=35;
	double lower=5;
		
	Point point=null;
	
	Cursor cur;
	MainPanel mp;
	float [] trig1, trig2;
	//int [] places1,  places2,  places3,  places4;
	int bitTab[][];
	float window[];
	int counter=0;
	int counter2=0;
	
	int xminr,xmaxr,ymaxr,yminr;
	
	AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f);
	AlphaComposite ac2 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
	float elementAlpha=0.5f;
	boolean makeFit=false;
	
	
	//JComboBox deleteList=null;
	
	public SpectrPane(Song song, boolean editable, boolean start, MainPanel mp){
		this.mp=mp;
		songMeas=song.getMeasurer();
		
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win")){
		    elementAlpha=0.3f;
		}
		
		int re=mp.defaults.getScaleFactor();
		multiplier=re;
		
		
		resetFonts();
		resetMultiplier();
		relaunch(song, editable, start);
		
		
	}
	
	public SpectrPane(Defaults defaults){
		
		int re=defaults.getScaleFactor();
		multiplier=re;
		
		resetFonts();
		resetMultiplier();
		
		
	}
	
	public SpectrPane(Song song, Defaults defaults, boolean view){
		
		int re=defaults.getScaleFactor();
		multiplier=re;
		//makeFit=true;

		resetFonts();
		resetMultiplier();
		relaunch(song, false, false);
		//song.setUpdateFFT(true);
		if (view){
			viewAll();
		}
		//System.out.println("DIMENSIONS: "+tnx+" "+unx+" "+song.getNx());
		
	}
	
	
	
	public void relaunch(Song song, boolean editable, boolean start){
		
		//System.out.println("PROGRESS: Producing a spectrogram");
		
		this.song=song;
		this.editable=editable;
		//this.setLayout(new BorderLayout());
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
	
	public void setGP(GuidePanel gp){
		this.gp=gp;
	}
	
	public void makeGuidePanel(double minX, int gpmaxf){
		
		System.out.println("PROGRESS: Making a guide panel");
		
		//if (this.gp==null){this.gp=gp;}
		
		int dispMode=displayMode;
		displayMode=0;
		
		readyToUpdateGP=false;
		double timeStepArch=song.getTimeStep();
		double frameLengthArch=song.getFrameLength();
		int maxfArch=song.getMaxF();
		int dynEqualArch=song.getDynEqual();
		double dynRangeArch=song.getDynRange();
		int unxArch=unx;
		int tnxArch=tnx;
		int nyArch=ny;
		int currentMinXArch=currentMinX;
		int currentMaxXArch=currentMaxX;
		double stretchYArch=stretchY;
		double stretchXArch=stretchX;
		int locationArch=location;
		double echoComp=song.getEchoComp();
		float noiseRemArch=song.getNoiseRemoval();
		double cutOffArch=song.getFrequencyCutOff();
		//song.frameLength=5;
		song.setFrameLength(256.0*1000/song.getSampRate());
		song.setMaxF(gpmaxf);
		
		double ts=song.getOverallLength()/(d.width-60.0);
		if (ts>song.getFrameLength()){
			ts=Math.max(song.getFrameLength(), 0.25*ts);
		}

		song.setTimeStep(ts);
		
		
		//System.out.println("GP TIME STEP: "+song.getTimeStep());
		//if (song.timeStep>song.frameLength){song.timeStep=song.frameLength;}
		//song.timeStep=10;
		song.setDynEqual(500);
		song.setDynRange(40);
		song.setEchoComp(0);
		song.setNoiseRemoval(0);
		song.setFrequencyCutOff(1500);
		song.setFFTParameters();		
		song.setGuideSpect(true);
		
		unx=song.getNx();
		ny=song.getNy();
		
		if (unx<minX){
			//song.setTimeStep(song.getTimeStep()*unx/minX);
			//song.setFFTParameters();
		}
		
		BufferedImage imf=new BufferedImage(unx, ny, BufferedImage.TYPE_INT_ARGB);

		
		currentMinX=0;
		currentMaxX=unx;
		song.setFFTParameters2(unx);
		
		System.out.println("GUIDE PANEL: "+currentMinX+" "+currentMaxX);
		
		song.makeMyFFT(currentMinX, currentMaxX);
		System.out.println("MADE GUIDE PANEL");
		nout=song.getOut();
		phase=song.getPhaseSP();
		updatePixelVals(imf);
		
		gp.makePanel(imf, song, this);
		
		System.out.println("Done with Guide panel");
		
		song.setFrameLength(frameLengthArch);
		song.setMaxF(maxfArch);
		song.setTimeStep(timeStepArch);
		song.setDynEqual(dynEqualArch);
		song.setDynRange(dynRangeArch);
		song.setEchoComp(echoComp);
		song.setNoiseRemoval(noiseRemArch);
		song.setFrequencyCutOff(cutOffArch);
		song.setFFTParameters();
		song.setGuideSpect(false);
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
	
	public void makeSpectrogramWholeSong() {
		
		int archMinX=currentMinX;
		int archMaxX=currentMaxX;
		int archUnx=unx;
		int archSMode=song.getStereoMode();
		unx=song.getNx();
		currentMinX=0;
		currentMaxX=unx;
		int dynEqualArch=song.getDynEqual();
		double tsArch=song.getTimeStep();
		song.setDynEqual(0);
		
		song.setTimeStep(5);
		song.setStereoMode(0);
		//song.setTimeStep(5);
		song.setFFTParameters();
		song.setFFTParameters2(unx);
		song.makeMyFFT(currentMinX, currentMaxX);
		song.setDynEqual(dynEqualArch);
		//song.setTimeStep(tsArch);
		
		song.setTimeStep(tsArch);
		song.setStereoMode(archSMode);
		currentMinX=archMinX;
		currentMaxX=archMaxX;
		unx=archUnx;
		//song.setFFTParameters();
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
		
		//yspace=(int)Math.round(yspace*ratio);
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
	
	public boolean[] getViewParameters(){
		return viewParameters;
	}
	
	public void setViewParameters(boolean[] vp){
		viewParameters=vp;
	}
	
	public int getNnx(){
		return nnx;
	}
	
	public int getNny(){
		return nny;
	}
	
	public double getDx(){
		return dx;
	}
	
	public BufferedImage getImf(){
		return imf;
	}
	
	public BufferedImage getIm(){
		return im;
	}
	
	public int getCurrentMinX(){
		return currentMinX;
	}
	
	public int getCurrentMaxX(){
		return currentMaxX;
	}
	
	public Song getSong(){
		return song;
	}
	
	public void setClickDrag(boolean b){
		clickDrag=b;
	}
	
	
	public void setLimitedOptions(int spectHeight, int width){
		viewParameters[1]=false;
		compressYToFit=spectHeight;
		compressXToFit=width;
	}
	
	public void setCompressYToFit(int s){
		compressYToFit=s;
	}
	
	public void setCompressXToFit(int s){
		compressXToFit=s;
	}
	
	public void setNout(float[][] x){
		nout=x;
	}
	
	public void setViewParameters(int s, boolean a){
		viewParameters[s]=a;
	}
	
	public void setDisplayMode(int s){
		displayMode=s;
	}
	
	public void restart(){
		
		//System.out.println("PROGRESS: Setting the bounds");		
		
		nnx=(int)d.getWidth()-100;
		
		
		dx=song.getDx();
		tdx=dx/stretchX;
		nx=song.getNx();
		tnx=(int)Math.round(stretchX*nx);
		
		
		if (makeFit){
			nnx=(int)Math.round(tnx+((xspace+xspace2)/multiplier));
		}
		
		ny=song.getNy();
		tny=(int)Math.round(ny*stretchY);
		nny=(int)Math.round(tny+(2*yspace/multiplier));
		if (compressYToFit>0){
			tny=ny;
			nny=tny+yspace*3;
			if (nny>compressYToFit){
				nny=compressYToFit;
				tny=nny-yspace*3;
				stretchY=tny/(ny+0.0);
			}
		}
		dy=song.getDy();
		tdy=dy/stretchY;
		if (compressXToFit>0){
			nnx=compressXToFit;
			if (nx>nnx-xspace-xspace2){
				tnx=nnx-xspace-xspace2;
				stretchX=tnx/(nx+0.0);
			}
		}
		if (tnx>nnx-xspace-xspace2){tnx=nnx-xspace-xspace2;}
		
		tnx=(int)Math.round(tnx*multiplier);
		tny=(int)Math.round(tny*multiplier);
		tdx=tdx/multiplier;
		tdy=tdy/multiplier;
			
		unx=(int)Math.round(tnx/stretchX);
		
		this.setPreferredSize(new Dimension(nnx, nny));
		this.revalidate();
		
		
		nnx2=(int)Math.round(nnx*multiplier);
		nny2=(int)Math.round(nny*multiplier);
		//System.out.println(song.getName());
		//System.out.println("PANEL DIMENSIONS: "+nny+" "+nnx+" "+nx+" "+ny+" "+tny+" "+tnx+" "+yspace+" "+multiplier+" "+nny2);
		imf=new BufferedImage(nnx2, nny2, BufferedImage.TYPE_INT_ARGB);
		im=new BufferedImage(unx, ny, BufferedImage.TYPE_INT_ARGB);
		//paintFrame();
		pList1=new int[tnx];
		pList2=new int[tnx];
		oldPList1=new int[tnx];
		oldPList2=new int[tnx];
		//if (editable){fixElements();}
		if (location==-1){
			location=0;
		}
		
		relocate(location);
		customCursor=System.getProperty("os.name").endsWith("Mac OS X");
	}

	void relocate(int start){
		//System.out.println("PROGRESS: Setting the position");
		location=start;
		currentMinX=(int)Math.round(start-(0.5*unx));
		if (currentMinX<0){currentMinX=0;}
		currentMaxX=currentMinX+unx;
		//System.out.println("LOCS: "+currentMinX+" "+currentMaxX+" "+unx+" "+start);
		if (currentMaxX>nx){
			currentMaxX=nx;
			currentMinX=currentMaxX-unx;
			if (currentMinX<0){currentMinX=0;}
		}
		//System.out.println("LOCS: "+currentMinX+" "+currentMaxX+" "+unx+" "+start+" "+nx);
		nout=null;
		//System.out.println("PROGRESS: Running the FFT");
		song.setFFTParameters2(unx);
		song.makeMyFFT(currentMinX, currentMaxX);
		song.makeAmplitude(currentMinX, currentMaxX, tnx);
		nout=song.getOut();
		phase=song.getPhaseSP();
		showDirection=song.recordOffset;
		if (showDirection) {
			directionVector=song.offsetDirection;
			//for (int i=0; i<directionVector.length; i++) {
			//	System.out.println(directionVector[i]);
			//}
		}
		
		song.makePhase();
		songMeas=song.getMeasurer();
		envelope=song.getEnvelope();
		if (displayMode!=2){
			updatePixelVals(im);
		}
		else {
			updatePixelValsPitchThreaded(im);
		}
		paintFound();
		if(readyToUpdateGP){gp.updateBoundaries();}
	}
	
	public void viewAll(){
		
		unx=song.getNx();
		ny=song.getNy();
		imf=new BufferedImage(unx, ny, BufferedImage.TYPE_INT_ARGB);
		currentMinX=0;
		currentMaxX=unx;
		song.setFFTParameters2(unx);
		song.makeMyFFT(currentMinX, currentMaxX);
		nout=song.getOut();
		phase=song.getPhaseSP();
		updatePixelVals(imf);
		System.out.println(imf.getWidth()+" "+imf.getHeight());
		
		/*
		//System.out.println("PROGRESS: Setting the position with stretch "+tnx);
		int start=0;
		int end=song.getNx();
		tnx=end;
		location=(end+start)/2;
		double stretch=100;
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
		//System.out.println(stretchX+" "+location+" "+tnx+" "+start+" "+end);
		restart();
		System.out.println(im.getWidth()+" "+im.getHeight());
		*/
	}
	
	void relocate(int start, int end){
		//System.out.println("PROGRESS: Setting the position with stretch "+tnx);
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
		//System.out.println(stretchX+" "+location+" "+tnx+" "+start+" "+end);
		restart();
	}
	
	void moveForward(double amt){
		
		
		int minx=getCurrentMinX();
		int maxx=getCurrentMaxX();
		
		double d=amt*(maxx-minx);
		
		int w=(int)Math.round(((maxx+minx)/2)+d);
		//System.out.println("FORW: "+w+" "+minx+" "+maxx);
		relocate(w);
	}
	
	void moveBackward(double amt){
		int minx=getCurrentMinX();
		int maxx=getCurrentMaxX();
		int w=((maxx+minx)/2)-maxx+minx;
		//System.out.println("BACK: "+w+" "+minx+" "+maxx);
		relocate(w);
	}
	
	/*
	 THIS METHOD WAS AN ATTEMPT TO FIX ELEMENTS WHEN THE SPECTROGRAM SETTINGS HAVE BEEN CHANGED 
	 
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
	
	*/
	
	void updatePixelValsPitchThreaded(BufferedImage img){
		float[][] pout=song.runPitchCalculator(unx);
		
		//float c=(float)(1/(song.getDynMax()*song.getDynRange()));
		
		for (int i=0; i<unx; i++){
			for (int j=0; j<ny; j++){
				int h=ny-j-1;
				double p=1-(pout[j][i]);
				int q=(int)(p*255);
				if (q>255){q=255;}
				if (q<0){q=0;}
				int sh=q;
				//img.setRGB(i, h, colpal[sh]);
				if (!showDirection) {
					img.setRGB(i, h, colpal[(int)(sh*255)]);
				}
				else {
					if (directionVector[i]>0) {
						int gb=(int)(sh*255*(1-directionVector[i]));
						Color c=new Color((int)(sh*255),gb,gb);
						img.setRGB(i, h, c.getRGB());
					}
					else {
						int gb=(int)(sh*255*(1+directionVector[i]));
						Color c=new Color(gb, gb, (int)(sh*255));
						img.setRGB(i, h, c.getRGB());
					}
				}
			}
		}
	}
	
	/*
	void updatePixelValsPitchThreaded2(BufferedImage img){
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
	*/
	/*
	void updatePixelVals(BufferedImage img){
		int i,j,h;
		float sh;
		int gb=0;
		int gc=0;
		float c=(float)(1/(song.getDynMax()*song.getDynRange()));
		//System.out.println("C: "+c+" "+song.dynMax+" "+song.dynRange);
		for (j=0; j<ny; j++){
			h=ny-j-1;
			for (i=0; i<unx; i++){
				sh=1-(nout[j][i]*c);
				if (sh<0){sh=0;}
				if (sh>1){sh=1;}
				//img.setRGB(i, h, colpal[(int)(sh*255)]);
				if (!showDirection) {
					img.setRGB(i, h, colpal[(int)(sh*255)]);
				}
				else {
					if (directionVector[i]>0) {
						
						gb=(int)(255*(1-(1-sh)*(1-directionVector[i])));
						gc=(int)(sh*255);
						//gb=(int)(sh*255*(1-directionVector[i]));
						if (gb<0) {gb=0;}
						if (gb>255) {gb=255;}
						Color cx=new Color(gc,gc,gb);
						img.setRGB(i, h, cx.getRGB());
					}
					else {
						
						gb=(int)(255*(1-(1-sh)*(1+directionVector[i])));
						gc= (int)(sh*255);
						//gb=(int)(sh*255*(1+directionVector[i]));
						if (gb<0) {gb=0;}
						if (gb>255) {gb=255;}
						Color cx=new Color(gb, gc, gc);
						img.setRGB(i, h, cx.getRGB());
					}
					//System.out.println(directionVector[i]+" "+gb);
				}
			}
		}
	}*/
	
	
	void updatePixelVals(BufferedImage img){
		int i,j,h;
		float sh;
		int gb=0;
		int gc=0;
		float c=(float)(1/(song.getDynMax()*song.getDynRange()));
		//System.out.println("C: "+c+" "+song.dynMax+" "+song.dynRange);
		for (j=0; j<ny; j++){
			h=ny-j-1;
			for (i=0; i<unx; i++){
				sh=1-(nout[j][i]*c);
				if (sh<0){sh=0;}
				if (sh>1){sh=1;}
				//System.out.println(j+" "+i+" "+phase[j][i]+" "+nout[j][i]);
				
				
				if (showBeamFormOffset) {
					if (phase[j][i]<0) {
						gb=(int)(255*(1-(1-sh)*(1+(phase[j][i]/Math.PI))));
						gc=(int)(sh*255);
						if (gb<0) {gb=0;}
						if (gb>255) {gb=255;}
						Color cx=new Color(gc,gc,gb);
						img.setRGB(i, h, cx.getRGB());
					}
					else {
						gb=(int)(255*(1-(1-sh)*(1-(phase[j][i]/Math.PI))));
						gc=(int)(sh*255);
						if (gb<0) {gb=0;}
						if (gb>255) {gb=255;}
						Color cx=new Color(gb,gc,gc);
						img.setRGB(i, h, cx.getRGB());
					}
				}
				else {
					gc=(int)(sh*255);
					Color cx=new Color(gc,gc,gc);
					img.setRGB(i, h, cx.getRGB());
				}
			}
		}
	}
	
	
	/*
	void updatePixelValsPitchOld(BufferedImage img){
		System.out.println("HERE");
		double[] spectrum=new double[ny];
		
		double[]hscor=new double[ny];

		
		double subSuppression=song.getFundAdjust();
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
			
			double q=peakAmp/(song.getDynRange());
			if (maxHscor>0){
				for (int j=minFreq; j<ny; j++){
					//hscor[j]=1-(hscor[j]/maxHscor);
					//hscor[j]*=hscor[j];
					hscor[j]=Math.pow(hscor[j], 2)*q;
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
	*/
	
	
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
						
			double minAmp=peakAmp-song.getDynRange();

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

						
			for (int j=song.getMinFreq(); j<ny; j++){
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
				if (!showDirection) {
					im.setRGB(i, h, colpal[(int)(sh*255)]);
				}
				else {
					if (directionVector[i]>0) {
						int gb=(int)(sh*255*(1-directionVector[i]));
						Color c=new Color((int)(sh*255),gb,gb);
						im.setRGB(i, h, c.getRGB());
					}
					else {
						int gb=(int)(sh*255*(1+directionVector[i]));
						Color c=new Color(gb, gb, (int)(sh*255));
						im.setRGB(i, h, c.getRGB());
					}
				}
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
		int time_samples=(int)Math.round(750/song.getDx());
		int[] model=new int[time_samples];
		
		double freqch=9300/(time_samples+0.0);
		double rows_per_ms=freqch/song.getDy();
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
		songMeas.merge(p, this, currentMinX);
		paintFound();
		gp.draw();
	}
	
	void selectAll(){
		pointList1=new int[unx];
		pointList2=new int[unx];
		for (int i=1; i<unx-1; i++){
			pointList1[i]=1;
			pointList2[i]=ny-2;
		}
		updateElement();
	}
	
	void searchSongs(){
		
		
	}
	
	void reDoLastSelectionX(){
		if (lastPointList1!=null){
			pointList1=new int[lastPointList1.length];
			System.arraycopy(lastPointList1, 0, pointList1, 0, pointList1.length);
			pointList2=new int[lastPointList2.length];
			System.arraycopy(lastPointList2, 0, pointList2, 0, pointList2.length);
			
			LinkedList<int[][]> signals=songMeas.getSignal(pointList1, pointList2, unx, findHarmonics);
			songMeas.segment(signals, false);
			songMeas.measureAndAddElements(signals, this, currentMinX);
			paintFound();
			gp.draw();
		}
	}

	
	
	private void updateSyllable(){
		if (pointList1!=null){
			int minx=0;
			while (pointList1[minx]==0){minx++;}
			int maxx=unx-2;								//this bit identifies the start and end of the element
			while (pointList1[maxx]==0){maxx--;}
			maxx++;
			//System.out.println(dx);
			int[] s={(int)Math.round(dx*(minx+currentMinX)), (int)Math.round(dx*(maxx+currentMinX))};
			//System.out.println("SYLL: "+s[0]+" "+s[1]+" "+minx+" "+maxx);
			int p=s[0]+s[1];
			int count=0;
			
			
			
			if ((editMode)||(!erase)) {
				//System.out.println("Syllable being added");
				LinkedList<Syllable> syls=song.getSyllList();
				for (Syllable sy: syls){
					//int[] s2=sy.getEleIds();
					int[] s2=sy.getLoc();
					int p2=s2[0]+s2[1];
					if (p2<p){count++;}
					//else{j=ns;}
				}
				
				song.addSyllable(count, s);
			
			
				mp.updateSyllableLists();
				mp.cloneLists();
				pointList1=null;
				pointList2=null;
				pList1=new int[tnx];
				pList2=new int[tnx];
				oldPList1=new int[tnx];
				oldPList2=new int[tnx];
				paintFound();
				gp.draw();
			}
			else {
				System.out.println("ERASE!");
				LinkedList<Syllable> syls=song.getSyllList();
				for (Syllable sy: syls){
					//int[] s2=sy.getEleIds();
					int[] s2=sy.getLoc();
					int p2=s2[0]+s2[1];
					if (p2<p){count++;}
					//else{j=ns;}
					
					if (s[0]<s2[0]) {
							if(s[1]>=s2[0]){
								syls.remove(sy);
							}
					}
					else if (s[0]<s2[1]) {
						syls.remove(sy);
					}					
				}
				
				paintFound();
				gp.draw();
				
			}
		}
	}
	
	/*
	 
	 THIS IS AN OLD METHOD TO MEASURE REVERBERATION ALONG A SYNTHESIZED SOUND
	 DOESN'T BELONG IN LUSCINIA REALLY
	
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
		
		Element ele=(Element)song.getElement(song.getNumElements()-1);
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
	
	*/
		
		
	public int reEstimateElements(){
	
	
		for (int i=archiveLastElementsAdded.length-1; i>=0; i--){
			song.removeElement(archiveLastElementsAdded[i]);
		}
	
		pointList1=archivePointList1;
		pointList2=archivePointList2;
		
		if ((currentMinX!=archiveMinX)||(currentMaxX!=archiveMaxX)){
			relocate(archiveMinX, archiveMaxX);	
		}
		updateElement();
		
		return 1;
	}
	
	private void getLastPointList(){
		lastPointList1=new int[pointList1.length];
		lastPointList2=new int[pointList2.length];
		for (int i=0; i<pointList1.length; i++){
			lastPointList1[i]=(int)Math.round(pointList1[i]/multiplier);
			if (lastPointList1[i]<0){lastPointList1[i]=0;}
			if (lastPointList1[i]>=ny){lastPointList1[i]=ny-1;}
			lastPointList2[i]=(int)Math.round(pointList2[i]/multiplier);
			if (lastPointList2[i]<0){lastPointList2[i]=0;}
			if (lastPointList2[i]>=ny){lastPointList2[i]=ny-1;}		
		}
	}
		
	private void updateElement(){
		if (pointList1!=null){
			getLastPointList();
			if (!erase){
				
				LinkedList<int[][]> signals=songMeas.getSignal(lastPointList1, lastPointList2, unx, findHarmonics);
				if (signals.size()>0){
					archiveMinX=currentMinX;
					archiveMaxX=currentMaxX;
					archivePointList1=new int[lastPointList1.length];
					System.arraycopy(pointList1, 0, archivePointList1, 0, pointList1.length);
					archivePointList2=new int[lastPointList2.length];
					System.arraycopy(pointList2, 0, archivePointList2, 0, pointList2.length);
					
				}
			//segmentMDL(signals);
				if (automaticMerge){songMeas.segment(signals, false);}
				//checkMinimumLengths(signals);
				measureAndAddElements(signals);
			
				mp.updateElementLists();
				mp.cloneLists();
				//mp.redo.setEnabled(true);
				paintFound();
				gp.draw();
			}
			else{
				if (editMode) {
				//System.out.println("erasing...");
					LinkedList<int[][]> signals=songMeas.eraseSignal(lastPointList1, lastPointList2, currentMinX);
					//System.out.println("elements to remeasure: "+signals.size());
					//checkMinimumLengths(signals);
					measureAndAddElements(signals);
			
					mp.updateElementLists();
					mp.cloneLists();
					//mp.redo.setEnabled(true);
					paintFound();
					gp.draw();
				}
				else {
					System.out.println("DELETING!");
				}
			}
		}
	}
	
	public void remeasureAll(){
		
		int a=currentMinX;
		int b=currentMaxX;
		float c=(float)(1/song.getDynMax());
		relocate(0, nx);
		songMeas.setUp();
		LinkedList<int[][]> tList=new LinkedList<int[][]>();
		int ne=song.getNumElements();
		for (int i=0; i<ne; i++){
			Element ele=(Element)song.getElement(i);
			int[][] signal=ele.getSignal();
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
						if (nout[aa][time]*c>song.getLowerLoop()){
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
		song.clearElements();
		measureAndAddElements(tList);
		//System.out.println("remeasuring complete");
		relocate(a,b);
	
	}

	public void remeasureAll(int temp){
		
		int a=currentMinX;
		int b=currentMaxX;
		
		relocate(0, nx);
		LinkedList<int[][]> tList=new LinkedList<int[][]>();
		int ne=song.getNumElements();
		for (int i=0; i<ne; i++){
			Element ele=(Element)song.getElement(i);
			int[][] signal=ele.getSignal();
			
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
		song.clearElements();
		measureAndAddElements(tList);
		
		relocate(a,b);

	}
	
	
	
	
	
	
	
	
	public void archiveElements(int[] a){
		archiveLastElementsAdded=a;
	}
	
	private int measureAndAddElements(LinkedList<int[][]> signals){
		int n=songMeas.measureAndAddElements(signals, this, currentMinX);
		pointList1=null;
		pointList2=null;
		pList1=new int[tnx];
		pList2=new int[tnx];
		oldPList1=new int[tnx];
		oldPList2=new int[tnx];
		return n;
	}

	
	
	void resetFonts(){
		int x=(int)(tickLabelFontSize*multiplier);
		int y=(int)(axisLabelFontSize*multiplier);
		int z=(int)(labelFontSize*multiplier);
		timeAxisFont=new Font(fontFace, tickLabelFontStyle, x);
		freqAxisFont=new Font(fontFace, tickLabelFontStyle, x);	
		timeLabelFont=new Font(fontFace, axisLabelFontStyle, y);
		freqLabelFont=new Font(fontFace, axisLabelFontStyle, y);
		eleLabelFont=new Font(fontFace, labelFontStyle, z);
		syllLabelFont=new Font(fontFace, labelFontStyle, z);
		
		BufferedImage imtemp=new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
		Graphics2D h=imtemp.createGraphics();
		FontMetrics fm=h.getFontMetrics(freqLabelFont);
		int ma=fm.getMaxAscent();
		int md=fm.getMaxDescent();
		int w1=ma+md;
		
		String label="10000";
		fm=h.getFontMetrics(freqAxisFont);
		int w2=fm.stringWidth(label);
		
		int w3=(int)(1.5*w1+1.5*w2);
		
		fm=h.getFontMetrics(timeAxisFont);
		ma=fm.getMaxAscent();
		md=fm.getMaxDescent();
		int h1=ma+md;
		
		fm=h.getFontMetrics(timeLabelFont);
		ma=fm.getMaxAscent();
		md=fm.getMaxDescent();
		int h2=ma+md;
		
		int h3= (int)(3*h1+3*h2);
		
		//System.out.println("Font heights: "+h3+" "+w3+" "+yspace+" "+xspace);
		
		yspace=h3;
		xspace=w3;
		
		//restart();
		
		fs=new BasicStroke((float)(lineWeight*multiplier), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
		h.dispose();
	}
	
	void resetMultiplier(){
		xspace=(int)Math.round(xspace*multiplier);
		yspace=(int)Math.round(yspace*multiplier);
	}
	
	
	
	void paintFrame(){
		
		DecimalFormat df = new DecimalFormat("#.#");
		
		if(song.getMaxF()<=2000){
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
		
		int mtl=(int)Math.round(minorTickMarkLength*multiplier);
		if (interiorTickMarks){mtl+=xspace;}
		else{mtl=xspace-mtl;}
		
		if (showMinorFreqTickMarks){
			while (ytick>yspace){
				
				h.drawLine(xspace, yt, mtl, yt);
				
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
		
		int tickAdjust=(int)Math.round((majorTickMarkLength+2)*multiplier);
		
		mtl=(int)Math.round(majorTickMarkLength*multiplier);
		if (interiorTickMarks){mtl+=xspace;}
		else{mtl=xspace-mtl;}
		
		if ((!showMajorFreqTickMarks)||(interiorTickMarks)){
			tickAdjust=2;
		}
		
		double kiloCorrector=1;
		if (frequencyUnit){
			kiloCorrector=0.001;
		}
				
		while (ytick>yspace){
			//Integer p=new Integer(ym);
			//Integer p=new Integer((int)Math.round(kiloCorrector*ym));
			
			if (showFreqTickMarkLabels){
				String pl=df.format(kiloCorrector*ym);
				int plwidth=fm.stringWidth(pl);
				if (plwidth>maxplwidth){
					maxplwidth=plwidth;
				}
				h.drawString(pl, xspace-tickAdjust-plwidth, yt+labelInc);
			}
			if (showMajorFreqTickMarks){
				h.drawLine(xspace, yt, mtl, yt);
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
		
		String label="Amplitude (V)";
		int tickAdjust=(int)Math.round((majorTickMarkLength+2)*multiplier);
		
		int pm=(int)Math.round(-1*Math.log(max)/Math.log(10));

		double pmx=Math.pow(10, pm*-1);
		if (pmx<max){pmx*=10;}
		StringBuffer sb=new StringBuffer("#.#");
		
		for (int i=0; i<pm; i++){
			sb.append('#');
		}
		
		DecimalFormat df = new DecimalFormat(sb.toString());
		
		//System.out.println("decimal places "+max+" "+pm+" "+pmx+" "+sb.toString());
		
		int maxplwidth=0;
	
		maxsl=(int)Math.round(Math.log(max)/Math.log(10));
		maxsl*=-1;
		StringBuffer pre=new StringBuffer("0.");
		for (int i=0; i<maxsl; i++){
			pre.append("0");
		}
		//String prefix=pre.toString();
		
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
		
	
		
		h.setColor(Color.WHITE);
		h.fillRect(0,0, nnx2, nny2);
		h.setColor(Color.BLACK);
		fm=h.getFontMetrics(freqAxisFont);
		
		int labelInc=fm.getMaxAscent()/2;
		
		if (showFrame){			
			h.drawRect(xspace, yspace, tnx+1, tny+1);
		}
		else{
			h.drawLine(xspace, yspace, xspace, yspace+tny+1);
			h.drawLine(xspace, yspace+tny+1, xspace+tnx+1, yspace+tny+1);
		}
		
		double yt250=tny*0.0125*pmx/max;
		double yt1000=tny*0.05*pmx/max;
		
		double ytick=yspace+1+tny*((max+pmx)/(2*max));
		int yt=(int)Math.round(ytick);
		while (ytick>yspace){
			if (ytick<yspace+tny){
				h.drawLine(xspace, yt, xspace-3, yt);
			}
			ytick-=yt250;
			yt=(int)Math.round(ytick);
		}
		ytick=yspace+1+tny*((max+pmx)/(2*max));
		yt=(int)Math.round(ytick);
		//double ym=-1*max;
		h.setFont(freqAxisFont);
		for (int i=0; i<=20; i++){
			
			/*
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
			*/
			
			double plq=(i-10)*0.1*pmx;
			if (Math.abs(plq)<max){
			
			
				String pl=df.format(plq);
				int plwidth=fm.stringWidth(pl);
				if (plwidth>maxplwidth){
					maxplwidth=plwidth;
				}
				h.drawString(pl, xspace-tickAdjust-plwidth, yt+labelInc);

				h.drawLine(xspace, yt, xspace-5, yt);	
			}
			ytick-=yt1000;
			yt=(int)Math.round(ytick);
		}
		
		AffineTransform affineTransform = new AffineTransform(); 
		
		//System.out.println("POSITIONS: "+w2+" "+h2);
		
		double xpos=xspace-10-maxplwidth-(w2+h2)*0.5;
		
		affineTransform.setToTranslation(xpos, yspace+(tny-h2)*0.5);
		affineTransform.rotate(Math.toRadians(270), w2/2, h2/2); 

		h.drawImage(vt, affineTransform, this); 
		
		/*
		h.setFont(freqLabelFont);
		AffineTransform af = new AffineTransform();
		af.translate(-30., ((tny/2)+80.));
		af.rotate(3*Math.PI/2);		
		FontRenderContext renderContext = new FontRenderContext(null, false, false);
		h.transform(af);
		TextLayout layout = new TextLayout("Amplitude (V)" , h.getFont(), renderContext);
		layout.draw(h, 5, 50);
		*/
		h.dispose();
		paintXAxisLabels();
	}
	
	void paintFrameLogarithmic(){
		
		String label="Frequency (Hz)";
		int tickAdjust=(int)Math.round((majorTickMarkLength+2)*multiplier);
		
		DecimalFormat df = new DecimalFormat("#.#");
		
		int maxplwidth=0;
		
		Graphics2D h=imf.createGraphics();
		//h.setColor(Color.WHITE);
		//h.fillRect(0,0, nnx, nny);
		h.setColor(Color.BLACK);
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
		
		fm=h.getFontMetrics(freqAxisFont);
		
		int labelInc=fm.getMaxAscent()/2;
		
		if (showFrame){			
			h.drawRect(xspace, yspace, tnx+1, tny+1);
		}
		else{
			h.drawLine(xspace, yspace, xspace, yspace+tny+1);
			h.drawLine(xspace, yspace+tny+1, xspace+tnx+1, yspace+tny+1);
		}
		
		double logCorrect=1/Math.log(2);
		
		double pm=Math.log(song.getMinFreq()*song.getDy())*logCorrect;
		int ps=(int)Math.ceil(pm);

		
		//int ym=(int)Math.round(Math.pow(2, pm+((tny-newy+1)*pe)));
		
		
		double pf=Math.log(ny*song.getDy())*logCorrect;
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
			
			String pl=df.format(freq);
			int plwidth=fm.stringWidth(pl);
			if (plwidth>maxplwidth){
				maxplwidth=plwidth;
			}
			h.drawString(pl, xspace-tickAdjust-plwidth, yloc+labelInc);
			
			
			/*
			Integer fp=new Integer((int)Math.round(freq));
			String pl=fp.toString();
			int len=pl.length();
			int xloc=xspace-(7+7*len);
			h.drawString(pl, xloc, yloc);
			*/
		}

		AffineTransform affineTransform = new AffineTransform(); 
		
		//System.out.println("POSITIONS: "+w2+" "+h2);
		
		double xpos=xspace-10-maxplwidth-(w2+h2)*0.5;
		
		affineTransform.setToTranslation(xpos, yspace+(tny-h2)*0.5);
		affineTransform.rotate(Math.toRadians(270), w2/2, h2/2); 

		h.drawImage(vt, affineTransform, this); 
		
		/*
		h.setFont(freqLabelFont);
		AffineTransform af = new AffineTransform();
		af.translate(-30., ((tny/2)+80.));
		af.rotate(3*Math.PI/2);		
		FontRenderContext renderContext = new FontRenderContext(null, false, false);
		h.transform(af);
		TextLayout layout = new TextLayout(label , h.getFont(), renderContext);
		layout.draw(h, 5, 50);
		*/
		h.dispose();
		paintXAxisLabels();
	}

	
	void paintXAxisLabels(){
		Graphics2D h=imf.createGraphics();
		h.setColor(Color.WHITE);
		h.setStroke(fs);
		h.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		int tickMarkOffset=(int)Math.round((majorTickMarkLength+2)*multiplier);
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
		
		int mtl=(int)Math.round(minorTickMarkLength*multiplier);
		if (interiorTickMarks){
			mtl=yspace+tny-mtl;
		}
		else{
			mtl=yspace+tny+mtl;
		}
		
		double maxX=(xspace+tnx);
		
		if (showMinorTimeTickMarks){
			while (xtick<maxX){
				h.drawLine(xt, yspace+tny+1, xt, mtl);
				xtick+=100/(multiplier*tdx);
				xt=(int)Math.round(xtick);
			}
		}
		
		int begin500=(int)(500*Math.ceil((currentMinX*stretchX*multiplier)*tdx*0.002));
		xtick=xspace+(begin500/(multiplier*tdx))-(currentMinX*stretchX);
		//System.out.println(currentMinX+" "+begin500+" "+begin100+" "+tdx+" "+xtick);
		xt=(int)Math.round(xtick);
		int xm=begin500;
		
		double timeCorrector=1;
		if (timeUnit){
			timeCorrector=0.001;
		}
		
		mtl=(int)Math.round(majorTickMarkLength*multiplier);
		if (interiorTickMarks){
			mtl=yspace+tny-mtl;
		}
		else{
			mtl=yspace+tny+mtl;
		}
		
		
		
		while (xtick<maxX){
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
				h.drawLine(xt, yspace+tny+1, xt, mtl);
			}
			xtick+=500/(multiplier*tdx);
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
		
		int nym=(int)(song.getNy()*multiplier);
		
		BufferedImage imt=new BufferedImage(tnx, nym, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=imt.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.WHITE);
		g.fillRect(0,0, tnx, nym);
		g.setColor(Color.BLACK);
		
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
		//AlphaComposite ac2 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
		BasicStroke bs1=new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		BasicStroke bs2=new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		BasicStroke bs3=new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);		
		int corr=(int)Math.round(0.5*song.getFrameLength()/song.getTimeStep());
		int[] sig=new int[tnx];
		g.setStroke(bs1);
		if (im!=null){
			int ne=song.getNumElements();
			if ((ne>0)&&(viewParameters[1])&&(viewParameters[0])){
				
				for (int i=0; i<ne; i++){
					Element ele=(Element)song.getElement(i);
					int[][] signal=ele.getSignal();
					int eleLength=ele.getLength();
					if ((eleLength>0)&&(signal[eleLength-1][0]>currentMinX)&&(signal[0][0]<currentMaxX)){
						int startX=tnx*(signal[0][0]+corr-currentMinX)/(currentMaxX-currentMinX);
						if (startX<0){startX=0;}
						int endX=tnx*(signal[eleLength-2][0]+corr-currentMinX)/(currentMaxX-currentMinX);
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
			//System.out.println("A "+p+" "+max);
			p--;
			max=Math.ceil(max*Math.pow(10, -1*p))*Math.pow(10, p);
			//System.out.println("B "+max);
			//max=Math.pow(10, p*0.1);
			
			double max2=0.5/max;
			
			for (int i=0; i<envelope.length; i++){
				int start=(int)Math.round(((envelope[i][0]*max2)+0.5)*ny*multiplier);
				int end=(int)Math.round(((envelope[i][1]*max2)+0.5)*ny*multiplier);
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
				int start=(int)Math.round(((envelope[i][0]*max2)+0.5)*ny*multiplier);
				int end=(int)Math.round(((envelope[i+1][0]*max2)+0.5)*ny*multiplier);
				
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
			int ne=song.getNumElements();
			double correct=1/(song.getFrameSize()*48.0);
			if ((ne>0)&&(viewParameters[1])){
				g.setStroke(bs1);
				for (i=0; i<ne; i++){
					
					Element ele=(Element)song.getElement(i);
					int[][] signal=ele.getSignal();
					int eleLength=ele.getLength();
					double[][] measurements=ele.getMeasurements();
					
					if ((eleLength>0)&&(signal[eleLength-1][0]>currentMinX)&&(signal[0][0]<currentMaxX)){
						
						sx=tnx*(signal[0][0]-currentMinX)/(currentMaxX-currentMinX);
						
						
						int [][] eleholder=new int[eleLength][16];
						for (j=0; j<eleLength; j++){
							int jj=j+5;
							for (k=0; k<4; k++){
								eleholder[j][k]=(int)Math.round(ny-(measurements[jj][k]/ele.getDy())-1);
							}
							for (k=4; k<8; k++){
								eleholder[j][k]=(int)(ny-(measurements[jj][k]*ny));
							}
							eleholder[j][8]=(int)(measurements[jj][8]*ny);
							if (eleholder[j][8]<0){eleholder[j][8]=0;}
							eleholder[j][8]=ny-eleholder[j][8];
							eleholder[j][9]=(int)(-0.5*measurements[jj][9]*ny);
							//eleholder[j][9]=(int)(ny-((10+measurements[jj][9])*0.1*ny));
							if (eleholder[j][9]>=ny){eleholder[j][9]=ny-1;}
							eleholder[j][10]=(int)Math.round(measurements[jj][10]/ele.getDy());
							eleholder[j][11]=(int)Math.round((measurements[jj][11]*correct*ny));
							eleholder[j][12]=(int)Math.round(ny-(measurements[jj][12]*0.01*ny));
							eleholder[j][13]=(int)Math.round(ny-((measurements[jj][13])*0.125*ny));
							if (eleholder[j][13]>=ny){eleholder[j][13]=ny-1;}
							eleholder[j][14]=(int)Math.round(ny-(measurements[jj][14]*0.125*ny));
						}	
						
						
						for (j=1; j<eleLength; j++){
							ex=tnx*(signal[j][0]-currentMinX)/(currentMaxX-currentMinX);
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
		int ne=song.getNumElements();
		if ((ne>0)&&(viewParameters[1])){
			h.setStroke(bs1);
			for (i=0; i<ne; i++){
				Element ele=(Element) song.getElement(i);
				int[][] signal=ele.getSignal();
				int eleLength=ele.getLength();
				double[][] measurements=ele.getMeasurements();
				if ((eleLength>0)&&(signal[eleLength-1][0]>currentMinX)&&(signal[0][0]<currentMaxX)){
					sy=yspace-1;
					double av=(signal[0][0]+signal[eleLength-1][0])*0.5;
					sx=(int)Math.round((((av*ele.getTimeStep()/dx)-currentMinX)*stretchX)+xspace);
					if ((sx>xspace)&&(sx<tnx)){
						h.setColor(elementColor);
						Integer p=new Integer(i+1);
						String pl=p.toString();
						h.drawString(pl, sx, sy);
					}
				}
			}
		}
		LinkedList<Syllable> sylls=song.getSyllList();
		int ns=sylls.size();
		if ((ns>0)&&(viewParameters[2])){
			h.setStroke(bs3);
			h.setColor(syllableColor);
			h.setComposite(ac);
			int shift1=22;
			int shift2=4;
			
			for (i=0; i<ns; i++){
				Syllable syl=sylls.get(i);
				int[] ele=syl.getLoc();
				
				int x1=(int)Math.round(((ele[0]/dx)-currentMinX)*stretchX);
				int x2=(int)Math.round(((ele[1]/dx)-currentMinX)*stretchX);
				if ((x2>0)&&(x1<tnx)){
					if (x1<0){x1=0;}
					if (x2>tnx){x2=tnx;}
					int shift=shift1;
					for (j=0; j<ns; j++){
						Syllable syl2=sylls.get(j);
						int[] syll=syl2.getLoc();
						//int[] syll=(int[])song.getSyllable(j);
						int p=(syll[0]+syll[1])/2;
						if ((ele[0]<p)&&(ele[1]>p)&&(ele[1]-ele[0]>syll[1]-syll[0])){
							shift=shift2;
							j=ns;
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
		

	}
	
	
	public void paintFound(){
	
		if (displayMode==1){
			paintAmpEnvelope();
		}
		else {
			paintSpectrogram();
		}
		/*
		int xx=(int)Math.round(imf.getWidth()/multiplier);
		int yy=(int)Math.round(imf.getHeight()/multiplier);
		imf2=new BufferedImage(xx, yy, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=imf2.createGraphics();
		
		Graphics2D g2=(Graphics2D)g;
		
		g2.scale(1/multiplier, 1/multiplier);				
		g2.drawImage(imf, 0, 0, this);
		g2.dispose();
		g.dispose();
		System.out.print("HERE! "+xx+" "+yy);
		*/
		repaint();
	}
	
	
	void paintSpectrogram(){
		//System.out.println("PAINTINGSPECTROGRAM!");
		double logCorrect=1/Math.log(2);
		double pm=Math.log(song.getMinFreq()*song.getDy())*logCorrect;
		double pf=Math.log(ny*song.getDy())*logCorrect;
		double pe=(pf-pm)/(ny+0.0);
		
		int xx=(int)Math.round(multiplier*unx);
		int yy=(int)Math.round(multiplier*song.getNy());
		
		BufferedImage imt=new BufferedImage(xx, yy, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=imt.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		
		xx=(int)Math.round(multiplier*im.getWidth());
		yy=(int)Math.round(multiplier*im.getHeight());
		
		
		g.drawImage(im, 0, 0, xx, yy, this);
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
		AlphaComposite ac2 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, elementAlpha);
		//AlphaComposite ac2 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f);
		int inv=(int)Math.ceil(1/stretchY);
		BasicStroke bs1=new BasicStroke((float)(multiplier*inv), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		BasicStroke bs2=new BasicStroke((float)(multiplier*inv), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		BasicStroke bs3=new BasicStroke((float)(multiplier*2.0*inv), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		int i, j, k, sx, sy, ex, ty, by;
		int ny2=(int)Math.round(multiplier*ny);
		if (im!=null){
			int ne=song.getNumElements();
			if ((ne>0)&&(viewParameters[1])){
				g.setStroke(bs1);
				for (i=0; i<ne; i++){
					
					Element ele=(Element)song.getElement(i);
					int[][] signal=ele.getSignal();
					int eleLength=ele.getLength();
					double[][] measurements=ele.getMeasurements();
					
					if ((eleLength>0)&&(signal[eleLength-1][0]>currentMinX)&&(signal[0][0]<currentMaxX)){
						
						sx=(int)Math.round(multiplier*(signal[0][0]-currentMinX));
						double correct=1/(song.getFrameSize()*48.0);
						int [][] eleholder=new int[eleLength][16];
						for (j=0; j<eleLength; j++){
							int jj=j+5;
							for (k=0; k<4; k++){
								
								double m1=measurements[jj][k]/dy;
								if (displayMode==2){
									double m2=((Math.log(measurements[jj][k])*logCorrect)-pm)/pe;
									m1=m2;
								}
								eleholder[j][k]=(int)Math.round(multiplier*(ny-m1-1));
							}
	
							for (k=4; k<8; k++){
								
								double m1=0.5+(Math.atan2(measurements[jj][k], 0.03)/Math.PI);
								//dat[k][j]=(float)(0.5+(Math.atan2(tot, 1)/Math.PI));
								//System.out.println(measurements[jj][k]+" "+m1);
								//eleholder[j][k]=(int)(ny2-(measurements[jj][k]*ny2));
								eleholder[j][k]=(int)(ny2-(m1*ny2));
							}
							eleholder[j][8]=(int)Math.round(measurements[jj][8]*ny2);
							if (eleholder[j][8]<0){eleholder[j][8]=0;}
							eleholder[j][8]=ny2-eleholder[j][8];
							eleholder[j][9]=(int)(measurements[jj][9]);
							//eleholder[j][9]=(int)(ny-((10+ele.measurements[jj][9])*0.1*ny));
							if (eleholder[j][9]>=ny2){eleholder[j][9]=ny2-1;}
							eleholder[j][10]=(int)Math.round(measurements[jj][10]/ele.getDy());
							eleholder[j][11]=(int)Math.round((measurements[jj][11]*correct*ny2));
							eleholder[j][12]=(int)Math.round(ny2-(measurements[jj][12]*ny2));
							//eleholder[j][13]=(int)Math.round(ny-((ele.measurements[jj][13])*0.0001*ny));
							//if (eleholder[j][13]>=ny){eleholder[j][13]=ny-1;}
							eleholder[j][13]=(int)Math.round(ny2-((Math.log(Math.max(1, measurements[jj][13])))*0.02*ny2));
							eleholder[j][14]=(int)Math.round(ny2-(measurements[jj][14]*(0.02)*ny2));
						}	
						
						if (viewParameters[0]){
							g.setColor(elementColor);
							g.setComposite(ac2);
						
							for (j=0; j<eleLength; j++){
								ex=(int)Math.round(multiplier*(signal[j][0]-currentMinX));
								if ((ex>0)&&(ex<currentMaxX*multiplier)){								
									int st=1;
									while ((st<signal[j].length-1)&&(signal[j][st]!=0)){
										ty=(int)Math.round(multiplier*signal[j][st]);
										by=(int)Math.round(multiplier*signal[j][st+1]);
										if (displayMode==2){
											double tya=(ny-ty-1)*dy;
											double bya=(ny-by-1)*dy;
											double m1=((Math.log(tya)*logCorrect)-pm)/pe;
											double m2=((Math.log(bya)*logCorrect)-pm)/pe;
											ty=(int)Math.round(multiplier*(ny-m1-1));
											by=(int)Math.round(multiplier*(ny-m2-1));
										}
										g.drawLine(ex, ty, ex, by);
										st+=2;
									}
								}
							}
						}
								
						g.setComposite(ac);
						g.setStroke(bs2);
						for (j=1; j<eleLength; j++){
							ex=(int)Math.round(multiplier*(signal[j][0]-currentMinX));
							if ((sx>0)&&(ex<currentMaxX*multiplier)){
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
		//System.out.println("IMF DIMS: "+nnx2+" "+nny2);
		h.fillRect(0, 0, nnx2, nny2);
		
		if ((stretchX!=1)||(stretchY!=1)){
			h.drawImage(imt, xspace+1, yspace+1, tnx, tny, this);
			imt=null;
		}
		else{
			h.drawImage(imt, xspace+1, yspace+1, tnx, tny, this);
			imt=null;
		}
		h.setColor(Color.WHITE);
		h.setComposite(ac);
		//h.fillRect(xspace, yspace-51, unx, 50);
		
		h.setFont(eleLabelFont);
		FontMetrics fm=h.getFontMetrics(eleLabelFont);
		//String tester="0123456789";
		int ph=fm.getHeight();
		
		h.setComposite(ac);
		int ne=song.getNumElements();
		if ((ne>0)&&(viewParameters[1])){
			
			eleLocList=new LinkedList<int[]>();
			
			h.setStroke(fs);
			int disp=yspace/10;
			for (i=0; i<ne; i++){
				Element ele=(Element) song.getElement(i);
				int[][] signal=ele.getSignal();
				int eleLength=ele.getLength();
				//double[][] measurements=ele.getMeasurements();
				if ((eleLength>0)&&(signal[eleLength-1][0]>currentMinX)&&(signal[0][0]<currentMaxX)){
					
					if (unitStyle==0){
						sy=yspace-ph/2;
						double av=(signal[0][0]+signal[eleLength-1][0])*0.5;
						sx=(int)Math.round((((av*ele.getTimeStep()/dx)-currentMinX)*stretchX)+xspace);
						if ((sx>xspace)&&(sx<tnx+xspace)){
							h.setColor(elementColor);
							Integer p=new Integer(i+1);
							String pl=p.toString();
							h.drawString(pl, sx, sy);
							int[] eleLoc={i, sx, sy};
							//System.out.println("Adding element: "+i+" "+sx+" "+sy);
							eleLocList.add(eleLoc);
							
						}
					}
					else if (unitStyle==1){
						sy=yspace-disp;
						if (disp==yspace/10){disp=yspace/7;}
						else{disp=yspace/10;}
						double qx=ele.getTimeStep()/dx;
						int sx1=(int)Math.round((((signal[0][0]*qx)-currentMinX)*stretchX)+xspace);
						int sx2=(int)Math.round((((signal[eleLength-1][0]*qx)-currentMinX)*stretchX)+xspace);
						if ((sx1>xspace)&&(sx2<tnx)){
							h.setColor(Color.BLACK);
							h.drawLine(sx1, sy, sx2, sy);
						}
						int[] eleLoc={i, sx1, sy, sx2};
						eleLocList.add(eleLoc);
					}
				}
			}
		}
		//System.out.println("DRAWING: "+viewParameters[2]);
		LinkedList<Syllable> sylls=song.getSyllList();
		int ns=sylls.size();
		if ((ns>0)&&(viewParameters[2])){
			
			
			h.setFont(syllLabelFont);
			fm=h.getFontMetrics(syllLabelFont);
			String tester="0123456789";
			FontRenderContext frc = h.getFontRenderContext();
	        GlyphVector gv = syllLabelFont.createGlyphVector(frc, tester);
	        int pj=(int)gv.getPixelBounds(null, 0, 0).getHeight();
			
			
			
			//int pj=fm.getHeight();
			int pj2=(int)Math.round(pj*1.2);
			int pj3=(int)Math.round(pj*1.4);
			int pj4=(int)Math.round(pj*0.1);
			
			//System.out.println(pj+" "+pj2+" "+pj3+" "+pj4);
			
			
			syllLocList=new LinkedList<int[]>();
			
			if (unitStyle==0){
				h.setStroke(fs);
				h.setColor(syllableColor);
				h.setComposite(ac);
				int shift1=yspace-2*ph-pj3;
				int shift2=yspace-2*ph-2*pj3;
			
				for (i=0; i<ns; i++){
					Syllable s1=sylls.get(i);
					int[] ele=s1.getLoc();
					if (!editMode) {
						h.setComposite(ac);
					}
					int x1=(int)Math.round(((ele[0]/dx)-currentMinX)*stretchX);
					int x2=(int)Math.round(((ele[1]/dx)-currentMinX)*stretchX);
					if ((x2>0)&&(x1<tnx)){
						if (x1<0){x1=0;}
						if (x2>tnx){x2=tnx;}
						int shift=shift1;
						for (j=0; j<ns; j++){
							Syllable s2=sylls.get(j);
							int[] syll=s2.getLoc();
							int p=(syll[0]+syll[1])/2;
							if ((ele[0]<p)&&(ele[1]>p)&&(ele[1]-ele[0]>syll[1]-syll[0])){
								shift=shift2;
								j=ns;
							}
						}
						/*
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
						*/
					
						x2+=xspace;
						x1+=xspace;
						int x3=(int)Math.round(0.5*(x1+x2)-5);
						h.setColor(syllableColor);
						h.fillRect(x1, shift, (x2-x1), pj2);
						h.setColor(Color.WHITE);
						Integer p=new Integer(i+1);
						String pl=p.toString();
						int yloc=shift+pj2-pj4;
						h.drawString(pl, x3, yloc);
						
						if (!editMode) {
							h.setColor(syllableColor);
							h.setComposite(ac2);
							h.fillRect(x1, yspace, (x2-x1), ny2);
						}
						
						
						
						int[] syllLoc={i, x3, yloc};
						syllLocList.add(syllLoc);
						
					}
				}
			}
			else if (unitStyle==1){
				
				h.setStroke(fs);
				h.setColor(Color.BLACK);
				
				int[] avs=new int[ne];
				int[] starts=new int[ne];
				int[] ends=new int[ne];
				
				for (i=0; i<ne; i++){
					Element ele=(Element) song.getElement(i);
					int[][] signal=ele.getSignal();
					int eleLength=ele.getLength();
					double qx=ele.getTimeStep()/dx;
					int sx1=(int)Math.round((((signal[0][0]*qx)-currentMinX)*stretchX));
					int sx2=(int)Math.round((((signal[eleLength-1][0]*qx)-currentMinX)*stretchX));
					avs[i]=(sx1+sx2)/2;
					starts[i]=sx1;
					ends[i]=sx2;
				}
				
				for (i=0; i<ns; i++){
					Syllable s1=sylls.get(i);
					int[] ele=s1.getLoc();
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
					
						for (j=0; j<ns; j++){
							Syllable s2=sylls.get(j);
							int[] syll=s2.getLoc();
							int p=(syll[0]+syll[1])/2;
							if ((ele[0]<p)&&(ele[1]>p)&&(ele[1]-ele[0]>syll[1]-syll[0])){
								sy=yspace/2;
								j=ns;
							}
						}
					
						h.drawLine(xs+xspace, sy, xf+xspace, sy);
						
						int[] syllLoc={i, xs+xspace, sy, xf+xspace};
						syllLocList.add(syllLoc);
						
					}
				}
			}
		}
		h.dispose();
		if ((displayMode==0)||(displayMode==3)){
			paintFrame();
		}
		else if (displayMode==2){
			paintFrameLogarithmic();
		}
		paintXAxisLabels();
		
	}
	
	public void paintComponent(Graphics g) {
		
	    super.paintComponent(g);  //paint background
	    
	    //System.out.println("Main panel painting");
	    
	    //if(!moving){
	
	    Graphics2D g2=(Graphics2D)g;
	    	
	    if (paintOver){			
			if (bounds<2){
				g2.scale(1/multiplier, 1/multiplier);				
			   	g2.drawImage(imf, 0, 0, this);
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			               RenderingHints.VALUE_ANTIALIAS_ON);
									
				if ((bounds==1)&&(!started)){
					g2.setColor(Color.BLACK);
					g2.setComposite(ac);
					g2.drawLine(newx+xspace, yspace, newx+xspace, yspace+tny);
					g2.drawLine(xspace, newy+yspace, tnx+xspace, newy+yspace);
					g2.setColor(Color.WHITE);
						
					g2.setComposite(ac2);
					g2.fillRect(xspace-40, newy+yspace-10, 35, 20);
					g2.fillRect(newx+xspace-5, yspace+tny+3, 35, 15);
					g2.setColor(Color.RED);
					g2.setFont(timeAxisFont);
					int tickMarkOffset=(int)Math.round((majorTickMarkLength+2)*multiplier);
					if ((interiorTickMarks)||(!showMajorTimeTickMarks)){
						tickMarkOffset=2;
					}
						
					FontMetrics fm=g2.getFontMetrics(timeAxisFont);
						//t4=System.currentTimeMillis();
					if (displayMode==0){
						int ym=(int)Math.round((tny-newy+1)*tdy);
						Integer p=new Integer(ym);
						String pl=p.toString();
						int xq=xspace-fm.stringWidth(pl)-5;
						g2.setColor(Color.WHITE);
						g2.fillRect(xq, newy+yspace+5-fm.getHeight(), fm.stringWidth(pl), fm.getHeight());
						g2.setColor(Color.RED);
						g2.drawString(pl, xq, newy+yspace+5);
					}
					else if (displayMode==1){
						int max=(int)Math.pow(10, -1*maxsl);
						double ym=2*((0.5*tny)-newy+1)/(tny+0.0)*max;
						Double p=new Double(ym);
						String pl=p.toString();
						if (pl.length()>5){
							pl=pl.substring(0, 5);
						}
						int xq=xspace-fm.stringWidth(pl)-5;
						g2.setColor(Color.WHITE);
						g2.fillRect(xq, newy+yspace+5-fm.getHeight(), fm.stringWidth(pl), fm.getHeight());
						g2.setColor(Color.RED);
						g2.drawString(pl, xq, newy+yspace+5);
					}
					else if (displayMode==2){
						
						double logCorrect=1/Math.log(2);
						double pm=Math.log(song.getMinFreq()*song.getDy())*logCorrect;
						double pf=Math.log(ny*song.getDy())*logCorrect;
						double pe=(pf-pm)/(tny+0.0);
						int ym=(int)Math.round(Math.pow(2, pm+((tny-newy+1)*pe)));
						Integer p=new Integer(ym);
						String pl=p.toString();
						int xq=xspace-fm.stringWidth(pl)-5;
						g2.setColor(Color.WHITE);
						g2.fillRect(xq, newy+yspace+5-fm.getHeight(), fm.stringWidth(pl), fm.getHeight());
						g2.setColor(Color.RED);
						g2.drawString(pl, xq, newy+yspace+5);
					}	
						//t5=System.currentTimeMillis();
					int xm=(int)Math.round(multiplier*(newx+(currentMinX*stretchX))*tdx);
						
						//int begin500=(int)(500*Math.ceil((currentMinX*stretchX)*tdx*0.002));
						
						
					Integer p=new Integer(xm);
					String pl=p.toString();
						
					int heightSpace=fm.getMaxAscent()+tickMarkOffset;
					g2.setColor(Color.WHITE);
					g2.fillRect(newx+xspace-3, yspace+tny+heightSpace-fm.getHeight(), fm.stringWidth(pl), fm.getHeight());
					g2.setColor(Color.RED);
					g2.drawString(pl,  newx+xspace-3, yspace+tny+heightSpace);
						//t6=System.currentTimeMillis();
						
				}
				
			}
			else{
				g2.scale(1/multiplier, 1/multiplier);
				g2.drawImage(imf, 0, 0, this);
				g2.setColor(Color.BLACK);
				g2.drawLine(soundposition+xspace-currentMinX, yspace, soundposition+xspace-currentMinX, yspace+tny);
			}
	
		}
	    	
	    g2.dispose();
	}

	/*
	public void paintComponent(Graphics g) {
		
        super.paintComponent(g);  //paint background
        
        System.out.println("Main panel painting");
        
        //if(!moving){
    
        	Graphics2D g2=(Graphics2D)g;
        	g2.scale(1/multiplier, 1/multiplier);				
        	g2.drawImage(imf, 0, 0, this);
        	
        	
        	if (paintOver){			
				if (bounds<2){
	
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			                RenderingHints.VALUE_ANTIALIAS_ON);
									
					if (bounds==1){
						//g2.drawImage(imf, 0, 0, this);
						g2.setColor(Color.BLACK);
						g2.setComposite(ac);
						g2.drawLine(newx+xspace, yspace, newx+xspace, yspace+tny);
						g2.drawLine(xspace, newy+yspace, tnx+xspace, newy+yspace);
						g2.setColor(Color.WHITE);
						
						g2.setComposite(ac2);
						g2.fillRect(xspace-40, newy+yspace-10, 35, 20);
						g2.fillRect(newx+xspace-5, yspace+tny+3, 35, 15);
						g2.setColor(Color.RED);
						g2.setFont(timeAxisFont);
						int tickMarkOffset=(int)Math.round((majorTickMarkLength+2)*multiplier);
						if ((interiorTickMarks)||(!showMajorTimeTickMarks)){
							tickMarkOffset=2;
						}
						
						FontMetrics fm=g2.getFontMetrics(timeAxisFont);
						//t4=System.currentTimeMillis();
						if (displayMode==0){
							int ym=(int)Math.round((tny-newy+1)*tdy);
							Integer p=new Integer(ym);
							String pl=p.toString();
							int xq=xspace-fm.stringWidth(pl)-5;
							g2.setColor(Color.WHITE);
							g2.fillRect(xq, newy+yspace+5-fm.getHeight(), fm.stringWidth(pl), fm.getHeight());
							g2.setColor(Color.RED);
							g2.drawString(pl, xq, newy+yspace+5);
						}
						else if (displayMode==1){
							int max=(int)Math.pow(10, -1*maxsl);
							double ym=2*((0.5*tny)-newy+1)/(tny+0.0)*max;
							Double p=new Double(ym);
							String pl=p.toString();
							if (pl.length()>5){
								pl=pl.substring(0, 5);
							}
							int xq=xspace-fm.stringWidth(pl)-5;
							g2.setColor(Color.WHITE);
							g2.fillRect(xq, newy+yspace+5-fm.getHeight(), fm.stringWidth(pl), fm.getHeight());
							g2.setColor(Color.RED);
							g2.drawString(pl, xq, newy+yspace+5);
						}
						else if (displayMode==2){
						
							double logCorrect=1/Math.log(2);
							double pm=Math.log(song.getMinFreq()*song.getDy())*logCorrect;
							double pf=Math.log(ny*song.getDy())*logCorrect;
							double pe=(pf-pm)/(tny+0.0);
							int ym=(int)Math.round(Math.pow(2, pm+((tny-newy+1)*pe)));
							Integer p=new Integer(ym);
							String pl=p.toString();
							int xq=xspace-fm.stringWidth(pl)-5;
							g2.setColor(Color.WHITE);
							g2.fillRect(xq, newy+yspace+5-fm.getHeight(), fm.stringWidth(pl), fm.getHeight());
							g2.setColor(Color.RED);
							g2.drawString(pl, xq, newy+yspace+5);
						}	
						//t5=System.currentTimeMillis();
						int xm=(int)Math.round(multiplier*(newx+(currentMinX*stretchX))*tdx);
						
						//int begin500=(int)(500*Math.ceil((currentMinX*stretchX)*tdx*0.002));
						
						
						Integer p=new Integer(xm);
						String pl=p.toString();
						
						int heightSpace=fm.getMaxAscent()+tickMarkOffset;
						g2.setColor(Color.WHITE);
						g2.fillRect(newx+xspace-3, yspace+tny+heightSpace-fm.getHeight(), fm.stringWidth(pl), fm.getHeight());
						g2.setColor(Color.RED);
						g2.drawString(pl,  newx+xspace-3, yspace+tny+heightSpace);
						//t6=System.currentTimeMillis();
						
					}
					
					g2.dispose();
				}
				else{
					g2.scale(1/multiplier, 1/multiplier);
					g2.drawImage(imf, 0, 0, this);
					//g.drawImage(imf, 0, 0, this);
					g2.setColor(Color.BLACK);
					g2.drawLine(soundposition+xspace-currentMinX, yspace, soundposition+xspace-currentMinX, yspace+tny);
				}

			}
        	
        	g2.dispose();
	}
	*/
	
	
	        //Methods required by the MouseInputListener interface.
	public void mouseClicked(MouseEvent e) { 
		System.out.println("Mouse clicked");
		if (bounds==0){
			
			double x=multiplier*e.getX();
			double y=multiplier*e.getY();
			
			double best=multiplier*400;
			int id=-1;
			int type=0;
			
			for (int[] el: eleLocList){
				
				double d=(el[1]-x)*(el[1]-x)+(el[2]-y)*(el[2]-y);
				//System.out.println("C: "+d+" "+el[1]+" "+x+" "+el[2]+" "+y);
				if (d<best){
					//System.out.println("FOUND ELE: "+el[0]);
					best=d;
					id=el[0];
					type=0;
				}	
			}
			
			for (int[] syl: syllLocList){
				
				double d=(syl[1]-x)*(syl[1]-x)+(syl[2]-y)*(syl[2]-y);
				if (d<best){
					//System.out.println("FOUND SYL: "+syl[0]);
					best=d;
					id=syl[0];
					type=1;
				}	
			}
			
			if (id>=0){
				int ne=song.getNumElements();
				PopUp popup=new PopUp(mp, e.getX(), e.getY(), this, id, type, ne);
				//System.out.println(best+" "+id+" "+type);
			}
		}
		else{
			
			if ((!editMode)&&(erase)) {
				System.out.println("ERASE CLICK!");
				
				newx = (int)(multiplier*e.getX())-xspace;
				int x=(int)Math.round(newx/(stretchX));
				x=(int)Math.round(dx*(x+currentMinX));
				LinkedList<Syllable> syls=song.getSyllList();
				
				
				
				
				int loc=-1;
				for (int i=0; i<syls.size(); i++){
					Syllable sy=syls.get(i);
					//int[] s2=sy.getEleIds();
					int[] s2=sy.getLoc();
					System.out.println(x+" "+s2[0]+" "+s2[1]);
					
					if ((x>=s2[0])&&(x<=s2[1])) {
						loc=i;
						i=syls.size();
						//System.out.println("Syll deleted");
						//syls.remove(sy);
					}					
				}
				if (loc>-1) {
					syls.remove(loc);
				}
			
				paintFound();
				gp.draw();
			}
			else {
				repaint();
			}
		}
		
		
		
		
	}

	public void mouseMoved(MouseEvent e) {
		//System.out.println("Mouse moved");
		counterTest++;
		//System.out.println("Mouse moved "+counterTest);
		
		if(started){
			oldx=newx;
			oldy=newy;
		}
		
		newx = (int)(multiplier*e.getX())-xspace;
		newy = (int)(multiplier*e.getY())-yspace;
			   
		//if you drag the cursor off the spectrogram area, element is finshed
			
		if ((newx>tnx)||(newy>tny)||(newx<0)||(newy<0)){
			bounds=0;
		}
		else{
			moving=true;
			bounds=1;
		}
		if (started){
			if (bounds==0){
				started=false;
				if ((syllable)&&(!erase)){updateSyllable();}
				else{updateElement();}
				repaint();
			}
			else{
				updatePoint();
				//repaint(newx, newy, 100, 100);
				//System.out.println(xminr+" "+yminr+" "+xmaxr+" "+ymaxr+" "+(newx+xspace)+" "+(newy+yspace));
				repaint(xminr, yminr, xmaxr, ymaxr);
				//repaint();
			}
			
		}
		else{
			repaint();
		}
		//newx = (int)(multiplier*e.getX())-xspace;
		//newy = (int)(multiplier*e.getY())-yspace;
		//if ((newx<tnx)&&(newy<tny)&&(newx>0)&&(newy>0)){bounds=1;}
		//else{bounds=0;}
		//System.out.print("PAINTING");
		//mainPane.repaint();
		//System.out.println("Sending to drawing pane");
		//drawingPane.repaint();
		//System.out.println("goodbye");
		
	}

	public void mouseExited(MouseEvent e) { 
		this.setCursor(Cursor.getDefaultCursor());
		//updateElement();
		
		System.out.println("Mouse exited "+counterTest);
	}

	public void mouseReleased(MouseEvent e) {
		System.out.println("Mouse released");
		//updateElement();
		if (clickDrag&&editable&&started){
			if (editMode) {
				started=false;
				if (syllable){updateSyllable();}
				//else{updateElement();}
				else{updateElement();}
			}
			else {
				if (erase) {
					/*
					System.out.println("ERASE!");
					
					newx = (int)(multiplier*e.getX())-xspace;
					int x=(int)Math.round(newx/(stretchX));
					
					LinkedList<Syllable> syls=song.getSyllList();
					for (Syllable sy: syls){
						//int[] s2=sy.getEleIds();
						int[] s2=sy.getLoc();

						if ((x>=s2[0])&&(s2[0]<-s2[1])) {
							syls.remove(sy);
						}					
					}
				
					paintFound();
					gp.draw();
					*/
				}
			}
		}
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
		System.out.println("Mouse pressed");
		if (editable){
			if (started){
				started=false;
				if ((syllable)&&(!erase)){updateSyllable();}
				//else{updateElement();}
				else{updateElement();}
			}
			else{
				if (bounds==1){
					if ((editMode)||(!erase)) {
						started=true;
						newx = (int)(multiplier*e.getX())-xspace;
						newy = (int)(multiplier*e.getY())-yspace;
						if ((newx<tnx)&&(newy<tny)){
							pointList1=new int[unx];
							pointList2=new int[unx];
							pList1=new int[tnx];
							pList2=new int[tnx];
							minPressedX=100000;
							maxPressedX=-1;
					
							oldx=newx;
							oldy=newy;
							updatePointCalculations();
							updatePoint();
						}
					}
				}
				
				repaint();
			}
		}

	}
	public void mouseDragged(MouseEvent e) { 
		System.out.println("Mouse dragged");
		//System.out.print("hello");
		if (clickDrag){
			if(started){
				oldx=newx;
				oldy=newy;
				newx = (int)(multiplier*e.getX())-xspace;
				newy = (int)(multiplier*e.getY())-yspace;
					   //if you drag the cursor off the spectrogram area, element is finshed
				if ((newx>tnx)||(newy>tny)||(newx<0)||(newy<0)){
					started=false;
					if (syllable){updateSyllable();}
					else{updateElement();}
					repaint();
				}
				else{
					updatePoint();
					repaint(xminr, yminr, xmaxr, ymaxr);
				}
			}
			else{
				repaint();
			}
				
			newx = (int)(multiplier*e.getX())-xspace;
			newy = (int)(multiplier*e.getY())-yspace;
			if ((newx<tnx)&&(newy<tny)&&(newx>0)&&(newy>0)){bounds=1; moving=true;}
			else{bounds=0;}
			
			//repaint();
			//System.out.println("goodbye");
		}
	}
	
	public void updatePList(int x, int miny, int maxy, Graphics2D g2){
		
		int startx=x;
		int endx=x;
		if (stretchX!=1){
			startx=(int)Math.round((x-0.5)*(stretchX));
			if (startx<0){startx=0;}
			endx=(int)Math.round((x+0.5)*(stretchX));
			if (endx>=tnx){endx=tnx-1;}
		}
		if (stretchY!=1){
			miny=(int)Math.round(miny*(stretchY));
			maxy=(int)Math.round(maxy*(stretchY));
		}
		
		int i2;
		int maxy2, miny2;
		for (int i=startx; i<=endx; i++){
			i2=i+xspace;
			maxy2=maxy;
			miny2=miny;
			if (pList1[i]==0){
				pList1[i]=miny2;
				pList2[i]=maxy2;
				g2.drawLine(i2, miny2+yspace, i2, maxy2+yspace);
			}
			else {
				if (pList1[i]>miny){
					maxy2=pList1[i]-1;
					//g2.drawLine(i2, miny+yspace, i2, pList1[i]-1+yspace);
					pList1[i]=miny2;
					g2.drawLine(i2, miny2+yspace, i2, maxy2+yspace);
				}
				if (pList2[i]<maxy){
					miny2=pList2[i]+1;
					//g2.drawLine(i2, pList2[i]+1+yspace, i2, maxy+yspace);
					pList2[i]=maxy2;
					g2.drawLine(i2, miny2+yspace, i2, maxy2+yspace);
				}
			}
			
			if (startx<xminr){xminr=startx;}
			if (endx>xmaxr){xmaxr=endx;}
			if (miny2<yminr){yminr=miny2;}
			if(maxy2>ymaxr){ymaxr=maxy2;}
		}
		
		
		//startx+=xspace;
		//endx+=xspace;
		//miny+=yspace;
		//maxy+=yspace;
		
		//System.out.println(startx+" "+endx+" "+miny+" "+maxy);
		
		
		
	}
	
	public void updatePointCalculations(){
		ny2=(int)Math.round(ny*multiplier);
		bt=song.getBrushType();
		if (bt==1){
			int bs=song.getBrushSize();
			maxyp=(multiplier*bs)/(stretchY);			//maxy and miny are the current positions of the cursor
			if (maxyp>=ny2){maxyp=ny2-1;}
			//minyp=(multiplier*bs)/(stretchY);
			//if (minyp<=0){minyp=1;}
		}
		else if (bt==2) {
			maxyp=ny2-1;
			minyp=1;
		}
		else if (bt==3) {
			double mf=ny2/(song.getMaxF()+0.0);
			maxyp=(song.getMinBrush()*mf);
			if (maxyp>=ny2){maxyp=ny2-1;}
			minyp=(song.getMaxBrush()*mf);
			//if (miny>=maxy){maxy=ny2-1;}
			if (minyp<=0){minyp=1;}
			maxyp=ny2-maxyp;
			minyp=ny2-minyp;
			//System.out.println(song.maxBrush+" "+song.minBrush+" "+song.maxf+" "+ny+" "+maxy+" "+miny);
		}
		
		brushAdj=(int)Math.round(multiplier*song.getBrushSize()/(stretchY));
		
	}
	
	
	public void updatePoint(){					//updates the list of points for the element the user is currently selecting
		
		
		Graphics2D g2=imf.createGraphics();
		if (!erase){
			g2.setColor(Color.RED);
		}
		else{
			g2.setColor(Color.BLUE);
		}
		g2.setComposite(ac);
		
		
		int minyc=0;
		int maxyc=0;
		if (bt==3){
			double mf=ny2/(song.getMaxF()+0.0);
			maxyc=(int)Math.round(song.getMinBrush()*mf);
			if (maxyc>=ny2){maxyc=ny2-1;}
			if (maxyc<=0){maxyc=1;}
			minyc=(int)Math.round(song.getMaxBrush()*mf);
			//if (miny>=maxy){maxy=ny2-1;}
			if (minyc<=0){minyc=1;}
			if (minyc>=ny2){minyc=ny2-1;}
			maxyc=ny2-maxyc;
			minyc=ny2-minyc;
		}
		else if (bt==2){
			minyc=1;
			maxyc=ny2-1;
		}
		
		xmaxr=0;
		xminr=nx;
		ymaxr=0;
		yminr=ny;
		
		//System.out.println(stretchX);
		if (pointList1!=null){
			int x=(int)Math.round(newx/(stretchX));
			int ox=(int)Math.round(oldx/(stretchX));
			int miny=(int)Math.round((newy/stretchY)-maxyp);
			int maxy=(int)Math.round((newy/stretchY)+maxyp);
			//this next section updates pointList at the point in time the cursor is at
			
			
			//System.out.println(minyc+" "+maxyc+" "+miny+" "+maxy);
			
			if (bt>1) {
				miny=minyc;
				maxy=maxyc;
			}
	
			if (pointList1[x]==0){
				pointList1[x]=miny;		
				pointList2[x]=maxy;
				updatePList(x, miny, maxy, g2);
			}
			else{
				if (pointList1[x]>miny){
					pointList1[x]=miny;	
					updatePList(x, miny, maxy, g2);
				}
				if (pointList2[x]<maxy){
					pointList2[x]=maxy;
					updatePList(x, miny, maxy, g2);
				}
			}
			
			
			
			
			
			
			
			
			//the next section is necessary to update pointList between where you were last and where you are now (if you move the cursor quickly)
			
			
			if (x>ox+1){
				double distx=x-ox;
				double disty=(newy-oldy)/(stretchY);
				double oy=oldy/stretchY;
				
				for (int i=ox+1; i<x; i++){
					
					if (bt==1){
						int y=(int)Math.round(((i-ox)/distx)*disty+oy);
						maxy=y+brushAdj;
						if (maxy>=ny2){maxy=ny2-1;}
						miny=y-brushAdj;
						if (miny<=0){miny=1;}
						if (pointList1[i]==0){
							pointList1[i]=miny;
							pointList2[i]=maxy;
							updatePList(i, miny, maxy, g2);
						}
						else{
							if (pointList1[i]>miny){
								pointList1[i]=miny;
								updatePList(i, miny, maxy, g2);
							}
							if (pointList2[i]<maxy){
								pointList2[i]=maxy;
								updatePList(i, miny, maxy, g2);
							}
						}
					}
					else {
						miny=minyc;
						maxy=maxyc;
						if (pointList1[i]==0){
							pointList1[i]=minyc;
							pointList2[i]=maxyc;
							updatePList(i, minyc, maxyc, g2);
						}
						
					}
				}
				maxPressedX=x+1;
				minPressedX=ox;
			}
			if (ox>x+1){
				double distx=ox-x;
				double disty=(newy-oldy)/(stretchY);
				double oy=oldy/(stretchY);
				int brushAdj=(int)Math.round(multiplier*song.getBrushSize()/(stretchY));
				for (int i=x+1; i<ox; i++){
					if (bt==1){
						int y=(int)Math.round(((ox-i)/distx)*disty+oy);
						maxy=y+brushAdj;
						if (maxy>=ny2){maxy=ny2-1;}
						miny=y-brushAdj;
						if (miny<=0){miny=1;}
						if (pointList1[i]==0){
							pointList1[i]=miny;
							pointList2[i]=maxy;
							updatePList(i, miny, maxy, g2);
						}
						else{
							if (pointList1[i]>miny){
								pointList1[i]=miny;
								updatePList(i, miny, maxy, g2);
							}
							if (pointList2[i]<maxy){
								pointList2[i]=maxy;
								updatePList(i, miny, maxy, g2);
							}
						}
					}
					else{
						miny=minyc;
						maxy=maxyc;
						if (pointList1[i]==0){
							pointList1[i]=miny;
							pointList2[i]=maxy;
							updatePList(i, miny, maxy, g2);
						}
					}
					/*
					else if (bt==2) {
						miny=1;
						maxy=ny2-1;
						pointList1[i]=miny;
						pointList2[i]=maxy;
						updatePList(i, miny, maxy, g2);
					}
					else if (bt==3) {
						double mf=ny2/(song.getMaxF()+0.0);
						maxy=(int)Math.round(song.getMinBrush()*mf);
						if (maxy>=ny2){maxy=ny2-1;}
						miny=(int)Math.round(song.getMaxBrush()*mf);
						//if (miny>=maxy){maxy=ny2-1;}
						if (miny<=0){miny=1;}
						maxy=ny2-maxy;
						miny=ny2-miny;
						pointList1[i]=miny;
						pointList2[i]=maxy;
						updatePList(i, miny, maxy, g2); 
					}
					*/
				}
				maxPressedX=ox;
				minPressedX=x+1;
			}
		}
		g2.dispose();
		
		xmaxr-=xminr;
		ymaxr-=yminr;
		xminr=(int)((xminr-10+xspace)/multiplier);
		xmaxr=(int)((xmaxr+10+xspace)/multiplier);
		yminr=(int)((yminr-10+yspace)/multiplier);
		ymaxr=(int)((ymaxr+10+yspace)/multiplier);
		
	}
	
	public void clearUp(){
		imf=null;
		im=null;
		colpal=null;
		harm=null;
		pointList1=null;
		lastPointList1=null;
		pointList2=null;
		lastPointList2=null;
		archivePointList1=null;
		archivePointList2=null;
		pList1=null;
		pList2=null;
		oldPList1=null;
		oldPList2=null;
		nout=null;
		//phase=null;
		
		
		archiveLastElementsAdded=null;
		
		envelope=null;
		gp=null;
		point=null;
		cur=null;
		
			
	}
	
	
	
	
}
