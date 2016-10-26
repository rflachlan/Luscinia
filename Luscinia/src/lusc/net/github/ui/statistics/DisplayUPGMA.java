package lusc.net.github.ui.statistics;
//
//  DisplayUPGMA.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/10/2005.
//  Copyright 2005 __MyCompanyName__. All rights reserved.
//

//
//  displayUPGMA.java
//  SongDatabase
//
//  Created by Robert Lachlan on Fri Dec 03 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.text.*;

import lusc.net.github.Defaults;
import lusc.net.github.analysis.AnalysisGroup;
import lusc.net.github.analysis.ComparisonResults;
import lusc.net.github.analysis.dendrograms.TreeDat;
import lusc.net.github.analysis.dendrograms.UPGMA;
import lusc.net.github.analysis.multivariate.MultivariateDispersionTest;
import lusc.net.github.ui.SaveDocument;
import lusc.net.github.ui.SaveImage;
import lusc.net.github.ui.SpectrogramSideBar;

public class DisplayUPGMA extends DisplayPane implements ActionListener, MouseInputListener, PropertyChangeListener, ChangeListener{
	
	Defaults defaults;
	int ysize=600;
	int ydisp=10;
	int xdisp=510;
	int xdisp2=500;
	int xpl1, ypl1;
	double maxdist=100;
	double maxY=0;
	
	int lineWeight=1;
	int fontSize=10;
	
	int size=0;
	int size1=0;	
	
	int amt=0;
	int elespace=20;
	int clickedX=-1;
	int clickedY=-1;
	int clickedLoc=-1;
	double cutoff=0;
		
	boolean enabled=false;
	boolean greyscale=true;
	boolean colorScale=false;
	
	boolean drawDottedLine=false;
	int dlLoc=0;
	
	JLabel leavesLabel, cutOffLabel;
	JFormattedTextField leavesField, cutOffField;
	JSlider zoom;
	JScrollPane scrollPane;
	JPanel mainPanel=new JPanel(new BorderLayout());
	
	int width, height, elements, maxElements;
	protected Font bodyFont;
	TreeDat[] dat;
	UPGMA upgma;
	BufferedImage imf;
	int dataType=0; 
	ComparisonResults cr;
	AnalysisGroup sg;
	SpectrogramSideBar ssb;
	DrawSilhouetteGraph dsg=new DrawSilhouetteGraph();
	SimplePaintingPanel spg=new SimplePaintingPanel();
	double silhouettes[]=null;
	double avsils[][]=null;
	NumberFormat num, num2;
	boolean RECALC=true;
	double maxDist;
	double scale=1;
	
	String[] labels={"None", "Position", "Song", "Individual", "Population", "Species"};
	JComboBox<String> branchLabels;
	int branchLabelIndex=0;
	
	JCheckBox songname, indname, popname, specname, typename;
	boolean addsong, addind, addpop, addspec, addtype;
	
	public DisplayUPGMA (UPGMA upgma, ComparisonResults cr, AnalysisGroup sg, int width, int height, Defaults defaults){
		this.upgma=upgma;
		this.cr=cr;
		this.sg=sg;
		ssb=sg.getSSB();
		this.dataType=cr.getType();
		this.width=width;
		this.height=height;
		this.defaults=defaults;
		scale=defaults.getScaleFactor();
		//this.width=(int)Math.round(scale*width);
		dat=upgma.getDat();
		maxDist=upgma.getMaxDist();
		startDisplaying();
	}	
		
	public DisplayUPGMA (UPGMA upgma, ComparisonResults cr, AnalysisGroup sg, int width, int height, double[] silhouettes, double[][] avsils, Defaults defaults){
		this.upgma=upgma;
		this.sg=sg;
		this.cr=cr;
		ssb=sg.getSSB();
		this.dataType=cr.getType();
		this.width=width;
		this.height=height;
		this.silhouettes=silhouettes;
		this.avsils=avsils;
		this.defaults=defaults;
		scale=defaults.getScaleFactor();
		//this.width=(int)Math.round(scale*width);
		dat=upgma.getDat();	
		maxDist=upgma.getMaxDist();
		startDisplaying();	
	}
	
	
	//NEXT 3 FUNCTIONS ARE SLIGHTLY DODGY (OLD) REQUIREMENTS FOR GEOG ANALYSIS
	public UPGMA getUPGMA(){
		return upgma;
	}
	
	public int getSize1(){
		return size1;
	}
	
	public double[][] getAvSils(){
		return avsils;
	}
		
	public void startDisplaying(){
		num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(0);
		num2=NumberFormat.getNumberInstance();
		num2.setMaximumFractionDigits(5);
		size=dat.length;
		size1=size-1;
		
		Font fontDef=this.getFont();		
		
		int fsz=(int)Math.round(elespace*scale/2);
		bodyFont  = new Font(fontDef.getName(), fontDef.getStyle(), fsz);
        if (bodyFont == null) {
            bodyFont = new Font("SansSerif", Font.PLAIN, fsz);
        }
        
        Font font=new Font(fontDef.getName(), fontDef.getStyle(), 8);
		
		xdisp2=width-250;
		xdisp=xdisp2+10;
		
		setNodePositions();		
		maxElements=elements;
		
		zoom=new JSlider(JSlider.HORIZONTAL, 0, 1000, 1000);
		zoom.setPaintTicks(false);
		zoom.setPaintLabels(false);
		zoom.setBorder(BorderFactory.createEmptyBorder(10,20,10, 10));
		zoom.addChangeListener(this);
		
		leavesField=new JFormattedTextField(num);
		leavesField.setFont(font);
		leavesField.setColumns(6);
		leavesField.setValue(new Integer(elements));
		leavesField.addPropertyChangeListener("value", this);
		
		cutOffField=new JFormattedTextField(num2);
		cutOffField.setFont(font);
		cutOffField.setColumns(6);
		cutOffField.setValue(new Integer(0));
		cutOffField.addPropertyChangeListener("value", this);
		
		leavesLabel=new JLabel("Number of leaves: ");
		leavesLabel.setFont(font);
		leavesLabel.setBorder(BorderFactory.createEmptyBorder(10,10,10,20));
		cutOffLabel=new JLabel("Cut-off: ");
		cutOffLabel.setFont(font);
		cutOffLabel.setBorder(BorderFactory.createEmptyBorder(10,10,10,20));
		
		int choices=1;
		if (dataType==4){
			choices=3;
		}
		if (dataType==5){
			choices=4;
		}
		String[] subLab=new String[labels.length-choices+1];
		subLab[0]=labels[0];
		int ii=1;
		for (int i=choices; i<labels.length; i++){subLab[ii]=labels[i]; ii++;}
		branchLabels=new JComboBox(subLab);
		branchLabels.addActionListener(this);
		branchLabels.setFont(font);
		
		JLabel blLabel=new JLabel("Branch Label");
		blLabel.setFont(font);
		blLabel.setLabelFor(branchLabels);
		
		songname=new JCheckBox("Song");
		songname.setSelected(true);
		addsong=true;
		if (dataType==5){
			addsong=false;
		}
		songname.setFont(font);
		songname.addActionListener(this);
		
		indname=new JCheckBox("Individual");
		indname.setFont(font);
		addind=false;
		if (dataType==5){
			indname.setSelected(true);
			addind=true;
		}
		indname.addActionListener(this);
		
		popname=new JCheckBox("Population");
		popname.setFont(font);
		addpop=false;
		popname.addActionListener(this);
		
		specname=new JCheckBox("Species");
		specname.setFont(font);
		addspec=false;
		specname.addActionListener(this);
		
		typename=new JCheckBox("Type");
		typename.setSelected(false);
		addtype=false;
		typename.setFont(font);
		typename.addActionListener(this);
		
		
		//status=new JLabel("Number of leaves: "+elements+" Cut-off: 0");
		//status.setBorder(BorderFactory.createEmptyBorder(10,10,10,20));
		paintPanel();
		spg.paintImage(imf, scale);
		paintDSG();
		//spg.revalidate();
		//this.revalidate();
				
		scrollPane=new JScrollPane(spg.imagePanel);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setPreferredSize(new Dimension(width, height-400));
		spg.addMouseListener(this);
		JPanel topPane=new JPanel(new BorderLayout());
		topPane.add(zoom, BorderLayout.WEST);
		
		JPanel settingsPane=new  JPanel();
		settingsPane.add(leavesLabel);
		settingsPane.add(leavesField);
		settingsPane.add(cutOffLabel);
		settingsPane.add(cutOffField);
		
		JPanel labelPane=new JPanel();
		labelPane.add(blLabel);
		labelPane.add(branchLabels);
		
		JPanel namePane=new JPanel(new GridLayout(2,0));
		if (dataType<5){
			namePane.add(songname);
		}
		namePane.add(indname);
		namePane.add(popname);
		namePane.add(specname);
		if (dataType<5){
			namePane.add(typename);
		}
		
		JPanel rightPane=new JPanel(new BorderLayout());
		rightPane.add(labelPane, BorderLayout.WEST);
		rightPane.add(namePane, BorderLayout.CENTER);
		
		topPane.add(settingsPane, BorderLayout.CENTER);
		topPane.add(rightPane, BorderLayout.EAST);
		
		JPanel middlePane=new JPanel(new BorderLayout());
		middlePane.add(dsg, BorderLayout.NORTH);
		middlePane.add(scrollPane, BorderLayout.CENTER);
		
		this.add(topPane, BorderLayout.NORTH);
		this.add(middlePane, BorderLayout.CENTER);
		this.setPreferredSize(new Dimension(width-500, height-300));
	}
	
	public void redisplay(){
		size=dat.length;
		size1=size-1;
		
		bodyFont  = new Font("Arial", Font.PLAIN, elespace);
        if (bodyFont == null) {
            bodyFont = new Font("SansSerif", Font.PLAIN, elespace);
        }
		
		xdisp2=width-250;
		xdisp=xdisp2+10;
		
		setNodePositions();		
		paintPanel();
		paintDSG();
	}
	
	public void setNodePositions(){
		double xrt=0;
		int i,j,k;
		int[] id;
		maxY=0;
		double x, xch;
		elements=0;
		
		
		for (i=0; i<size; i++){
			xrt=dat[i].dist-cutoff;
			dat[i].xrt=xrt;
			if (xrt<0){xrt=0;}
			//dat[i].xloc=xdisp-((xrt)*xdisp2/(1-cutoff));
			
			dat[i].xloc=xdisp2*(1-(xrt/(1-cutoff)));
			
			dat[i].children=0;
		}
		
		for (i=size1; i>=0; i--){
			if  ((dat[i].xrt<=0)&&(dat[dat[i].parent].xrt>=0)){
				if ((i<size1)&&(dat[dat[i].parent].children==1)){
					dat[dat[i].parent].children=0;
					elements--;
				}
				dat[i].children=1;
				elements++;
			}
		}
		ysize=elements*elespace;
		for (i=0; i<size; i++){
			if (dat[i].daughters[0]>-1){
				dat[i].children+=dat[dat[i].daughters[0]].children+dat[dat[i].daughters[1]].children;
			}
		}
		
		dat[size1].xplace=0.5;
		dat[size1].xrange=1;
		dat[size1].xstart=0;
		dat[size1].yloc=0.5*ysize+ydisp;
		for (i=size1; i>=0; i--){	
			xch=0;
			if (dat[i].children>1){
				for (j=0; j<2; j++){
					k=dat[i].daughters[j];
					x=dat[k].children/(dat[i].children*2.0);
					xch=2*xch+x;
					dat[k].xplace=dat[i].xstart+xch*dat[i].xrange;
					dat[k].xrange=dat[k].children/(elements+0.0);
					dat[k].xstart=dat[k].xplace-0.5*dat[k].xrange;
					dat[k].yloc=dat[k].xplace*ysize+ydisp;
					if (dat[k].yloc>maxY){maxY=dat[k].yloc;}
				}
			}
		}		
	}
	
	public void drawDottedLine(int place){
		
		drawDottedLine=true;
		
		dlLoc=place;
		paintPanel();
		//spg.paintImage(imf);
		//paintDSG();
		//this.revalidate();
		spg.paintImage(imf, scale);
		spg.revalidate();
	}
	
	public void labelBranches(){
		
		if (branchLabelIndex==1){
			double[] d=cr.getPosition();
			for (int i=0; i<d.length; i++){
				dat[i].colval=d[i];
			}
		}
		if (branchLabelIndex==2){
			int nsongs=cr.getSongs().length;
			int[][] d=cr.getLookUp();
			for (int i=0; i<d.length; i++){
				dat[i].colval=d[i][0]/(nsongs+0.0);
			}
		}
		if (branchLabelIndex==3){
			int ninds=cr.getIndividualNames().length;
			int[] d=cr.getLookUpIndividuals();
			for (int i=0; i<d.length; i++){
				dat[i].colval=d[i]/(ninds+0.0);
			}
		}
		if (branchLabelIndex==4){
			int npops=cr.getPopulationNames().length;
			int[] d=cr.getPopulationListArray();
			for (int i=0; i<d.length; i++){
				dat[i].colval=d[i]/(npops+0.0);
			}
		}
		if (branchLabelIndex==5){
			int nspecs=cr.getSpeciesNames().length;
			int[] d=cr.getSpeciesListArray();
			for (int i=0; i<d.length; i++){
				dat[i].colval=d[i]/(nspecs+0.0);
			}
		}	
	}
	
	public float[] getColorScore(float p){
		//float[] z=getRobPalette(p);
		//float[] z=getHSBPalette(p, 0.5f, 1.0f, 0.85f, 1f);
		//Color[] cols={new Color(255,237,160), new Color(254,178,76), new Color(240,59,32)};
		//Color[] cols={new Color(165,0,38), new Color(215,48,39), new Color(244,109,67), new Color(253,174,97), new Color(254,224,144), new Color(245,245,191), new Color(224,233,248), new Color(171,217,233), new Color(116,173,209), new Color(69,117,180), new Color(49,54,149)};
		//Color[] cols={new Color(236,226,240), new Color(208,209,230), new Color(166,189,219), new Color(103,169,207), new Color(54,144,192), new Color(2,129,138), new Color(1,108,89), new Color(1,70,54)};
		//Color[] cols={Color.GREEN, Color.YELLOW, Color.RED};
		Color[] cols={new Color(77,77,255), new Color(235, 235, 100), new Color(239,21,21)};
		
		float[] z=getLinearPalette(p, cols);
		
		return z;
	}
	
	public float[] getHSBPalette(float p, float start, float end, float sat, float bright){
		float x=start+(end-start)*p;
		Color c = Color.getHSBColor(x, sat, bright);
		float[] results=c.getColorComponents(null);
		return results;
	}
	
	public float[] getLinearPalette(float p, Color[] c){
		
		int n=c.length;
		
		int x=(int)Math.floor(p*(n-1));
		if (x==n-1){x=n-2;}
		
		float y=p*(n-1)-x;
		System.out.println(n+" "+p+" "+x+" "+y);
		Color a=c[x];
		Color b=c[x+1];
		
		float[] ra=a.getColorComponents(null);
		float[] rb=b.getColorComponents(null);
		
		for (int i=0; i<ra.length; i++){
			ra[i]=rb[i]*y+ra[i]*(1-y);
		}
		return ra;
		
	}
	
	
	public float[] getRobPalette(float p){
		float redc=0;
		if (p>0.8){redc=1f;}
		else if ((p>0.2)&&(p<=0.6)){redc=0.0f;}
		else if (p<=0.2){redc=1-p*5f;}
		else {redc=(p-0.6f)*5f;}
		redc+=0.5f;
		redc/=1.5f;
		redc=1.0f-redc;
		
		float greenc=0f;
		if (p>0.6){greenc=0.0f;}
		else if (p<=0.4){greenc=1;}
		else {greenc=1-(p-0.4f)*5f;}
		greenc+=0.5f;
		greenc/=1.5f;
		greenc=1.0f-greenc;
		
		float bluec=0;
		if (p<=0.2){bluec=0.0f;}
		else if ((p>0.4)&&(p<=0.8)){bluec=1.0f;}
		else if (p<=0.4){bluec=(p-0.2f)*5f;}
		else {bluec=1-(p-0.8f)*5f;}
		bluec+=0.5f;
		bluec/=1.5f;
		bluec=1.0f-bluec;
		
		float[] results={redc, greenc, bluec};
		return results;
	}
	
	
	public void paintPanel(){
		
		
		String[] names=cr.getNames(addspec, addpop, addind, addsong, addtype);
			
		int ny=(int)Math.round(scale*(elespace*elements+2*ydisp));
		System.out.println("UPGMA TREE HEIGHT: "+ny);
		int widthx=(int)Math.round(scale*width);
		imf=new BufferedImage(widthx, ny, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=imf.createGraphics();
		int lw=(int)Math.round(lineWeight*scale);
		BasicStroke fs=new BasicStroke(lw, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
		g.setStroke(fs);
		
		RenderingHints hints =new RenderingHints(RenderingHints.KEY_RENDERING,
				 RenderingHints.VALUE_RENDER_QUALITY);
				hints.put(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		g.setRenderingHints(hints);
		
		g.setColor(Color.WHITE);
		g.fillRect(0,0, widthx, ny);
		g.setColor(Color.BLACK);
		int i, j, k, colorroot, xpl2, ypl2;
		xpl1=(int)Math.round(dat[size1].xloc*scale);
		ypl1=(int)Math.round(dat[size1].yloc*scale);
		//g.fillArc(xpl1-1, ypl1-1, 3, 3, 0, 360);
		/*
		Color[] colors=new Color[101];
		
		
		for (i=0; i<101; i++){
			
			float p=i/(100f);
			
			float redc=0;
			if (p>0.8){redc=1;}
			else if ((p>0.2)&&(p<=0.6)){redc=0;}
			else if (p<=0.2){redc=1-p*5f;}
			else {redc=(p-0.6f)*5f;}
			
			float greenc=0;
			if (p>0.6){greenc=0;}
			else if (p<=0.4){greenc=1;}
			else {greenc=1-(p-0.4f)*5f;}
			
			float bluec=0;
			if (p<=0.2){bluec=0;}
			else if ((p>0.4)&&(p<=0.8)){bluec=1;}
			else if (p<=0.4){bluec=(p-0.2f)*5f;}
			else {bluec=1-(p-0.8f)*5f;}
												
			colors[i]=new Color(redc, greenc, bluec);
			
			//g.setColor(colors[i]);
			//g.drawLine(5, i+5, 15, i+5);
			
		}
		*/
		//double[] labels=sg.getLabels(dataType);
		//if (labels==null){labels=new double[1];}
		
		for (i=size1; i>=0; i--){
			xpl1=(int)Math.round(dat[i].xloc*scale);
			ypl1=(int)Math.round(dat[i].yloc*scale);
			
			if (dat[i].children>1){
				for (j=0; j<2; j++){
				
					int q=dat[i].daughters[j];
					
					xpl2=(int)Math.round(dat[q].xloc*scale);
					ypl2=(int)Math.round(dat[q].yloc*scale);
					
					/*
					double meanout=0;
					if (silhouettes==null) {
						double meancount=0;
						for (k=0; k<dat[q].child.length; k++){
							if (labels[dat[q].child[k]]>-1){
								meanout+=labels[dat[q].child[k]];
								meancount++;
							}
						}
						if (meancount>0){
							meanout/=meancount;
						}
						else{
							meanout=-1;
						}
					}
					else if (silhouettes.length<dat.length){
						double meancount=0;
						for (k=0; k<dat[q].child.length; k++){
							if (silhouettes[dat[q].child[k]]>-1){
								meanout+=silhouettes[dat[q].child[k]];
								meancount++;
							}
						}
						if (meancount>0){
							meanout/=meancount;
						}
						else{
							meanout=-1;
						}
					}
					else{
						meanout=silhouettes[i];
					}
					int p=(int)Math.round(100*meanout);
					if ((p<0)||(p>colors.length)||(!colorScale)){
							g.setColor(Color.BLACK);
					}
					else{
						g.setColor(colors[p]);
					}		
					*/
					
					if (branchLabelIndex==0){
						g.setColor(Color.BLACK);
					}
					else{
						int nq=dat[q].child.length;
						System.out.println(q+" "+dat[q].children);
						if (nq==0){
							float[] fc=getColorScore((float)dat[q].colval);
							g.setColor(new Color(fc[0], fc[1], fc[2]));
						}
						else{
							float[] fct=new float[3];
							for (int m=0; m<dat[q].child.length; m++){
								float[] fc=getColorScore((float)dat[dat[q].child[m]].colval);
								for (int n=0; n<3; n++){
									fct[n]+=fc[n];
								}
							}
							float den=dat[q].child.length;
							for (int n=0; n<3; n++){
								fct[n]=fct[n]/den;
								if (fct[n]>1f){fct[n]=1;}
							}
							g.setColor(new Color(fct[0], fct[1], fct[2]));
						}
					}
					
					
					//g.setColor(Color.BLACK);
					g.drawLine(xpl1, ypl1, xpl1, ypl2);
					g.drawLine(xpl1, ypl2, xpl2, ypl2);
					//g.fillArc(xpl2-1, ypl2-1, 3, 3, 0, 360);
				}
			}
			else if (cutoff==0){
				g.setColor(Color.BLACK);
				g.setFont(bodyFont);
				
				g.drawString(names[dat[i].child[0]], xpl1+5, ypl1+5);
			}
		}
		
		if (drawDottedLine){
			g.setColor(Color.BLACK);
			int yq=0;
			
			while (yq<ny){
				int d=(int)Math.round(dlLoc*scale);
				g.drawLine(d, yq, d, yq+10);
				yq+=20;
			}
		}
		
		g.setColor(Color.WHITE);
		int f1=(int)(300*scale);
		int f2=(int)((ydisp-1)*scale);
		g.fillRect(xpl1,0, f1, f2);
		g.dispose();
	}
	
			
	//public void paintComponent(Graphics g) {
	//	super.paintComponent(g);  //paint background
	//	g.drawImage(imf, 0, 0, this);
	//	if	((clickedX>-1)&&(clickedY>-1)){
	//		g.setColor(Color.RED);
	//		g.fillArc(clickedX-3, clickedY-3, 7, 7, 0, 360);
	//	}
	//}
	
	public void paintDSG(){
				
		double[][] ypoints=new double[avsils.length][size1+1];
		int[] xpoints=new int[size1+1];
		int j=avsils[0].length-1;
		/*
		double[] templ=new double[size1+1];
		for (int i=0; i<size1+1; i++){
			templ[i]=dat[i].xloc;
		}
		Arrays.sort(templ);
		
		
		for (int i=size1; i>=0; i--){
			for (int ii=0; ii<size1; ii++){
				if (dat[ii].xloc==templ[i]){
					xpoints[i]=(int)Math.round(dat[ii].xloc);
					for (int k=0; k<avsils.length; k++){
						ypoints[k][i]=avsils[k][ii];
					}
				}
			}
		}
		
		*/
		
				
		for (int i=size1; i>=0; i--){
			xpoints[i]=(int)Math.round(dat[i].xloc);
			for (int k=0; k<avsils.length; k++){
				ypoints[k][i]=avsils[k][j];
			}
			j--;
		}
		
		//int f1=(int)Math.round(1*scale);
		//int f2=(int)Math.round(100*scale);
		int f3=(int)Math.round(90*scale);
		int f4=(int)Math.round(xdisp*scale);

		dsg.paintGraphUPGMA(ypoints, xpoints, 1, 100, width, f3, f4, this, scale);	
	}
	
	public void mouseClicked(MouseEvent e) { 
		
		if (enabled){
			int x=e.getX();
			int y=e.getY();
			/*
			if (x>xpl1+3){
				
				if (cutoff==0){
					
					int q=0;
					int best=1000000;
					
					for (int i=size1; i>=0; i--){
						if (dat[i].children==1){
							int r=(int)Math.abs(dat[i].yloc-y);
							if (r<best){
								best=r;
								q=i;
							}
						}
					}
					
					int s=0;
					if (dataType==0){
						
						s=sg.lookUpElsC[dat[q].child[0]][0];						
					}
					if (dataType==1){
						s=sg.lookUpEls[dat[q].child[0]][0];
					}
					if (dataType==2){
						s=sg.lookUpSyls[dat[q].child[0]][0];
					}
					if (dataType==3){
						s=sg.lookUpTrans[dat[q].child[0]][0];
					}
					if (dataType==4){
						s=q;
					}
					
					
					//String nn=(String)JOptionPane.showInputDialog(this, "new song name", "Dialog", JOptionPane.PLAIN_MESSAGE, null, null, sg.songs[s].name);
					
					//sg.songs[s].name=nn;
					
					
					
					//sg.dbc.writeSongInfo(sg.songs[s]);
					
					
					MainPanel mp=new MainPanel(sg.dbc, sg.songs[s].songID, sg.defaults);
					mp.startDrawing();
					
					
				}
				
				
				
			}
			*/
			
			
			
			double min=100000000;
			int loc=0;
			for (int i=0; i<dat.length; i++){
				if (dat[i].children>=1){
					double score=Math.sqrt((x-dat[i].xloc)*(x-dat[i].xloc)+(y-dat[i].yloc)*(y-dat[i].yloc));
					if (score<min){
						min=score;
						loc=i;
					}
				}
			}
			clickedLoc=loc;
			clickedY=(int)Math.round(dat[loc].yloc);
			clickedX=(int)Math.round(dat[loc].xloc);
			
			ssb.draw(dataType, dat[loc].child);
			
			spg.paintImageDot(imf, clickedX, clickedY, scale);
			spg.revalidate();
				//repaint();	
		}
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) { 
		enabled=false;
	}

	public void mouseReleased(MouseEvent e) {
	}
	
	public void mouseEntered(MouseEvent e) { 
		enabled=true;
	}
	public void mousePressed(MouseEvent e) { }
	public void mouseDragged(MouseEvent e) { 
		
	}
	
	public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (RECALC){
        	if (!source.getValueIsAdjusting()) {
        		cutoff=0.001*source.getValue();
        		cutoff=1-cutoff;
        		//int cutoff2=(int)source.getValue();
        		setNodePositions();
        		ysize=elements*elespace;
			
        		//DecimalFormat df=new DecimalFormat("0.###");
			
        		//status.setText("Number of leaves: "+elements+" Cut-off: "+df.format(cutoff*upgma.maxDist));
			
        		RECALC=false;
        		leavesField.setValue(elements);
        		cutOffField.setValue(cutoff*maxDist);
        		paintPanel();
        		spg.paintImage(imf, scale);
        		//repaint();
        		paintDSG();
        		this.revalidate();
        		scrollPane.revalidate();
        		RECALC=true;
        	}
        }
    }
	
	public void propertyChange(PropertyChangeEvent e) {
        Object source = e.getSource();
        if (RECALC){
        	if (source==leavesField){
        		RECALC=false;
        		int p = (int)((Number)leavesField.getValue()).intValue();
        		if (p<2){
        			p=2;
        			leavesField.setValue(2);
        		}
        		if (p>maxElements){
        			p=maxElements;
        			leavesField.setValue(maxElements);
        		}
        		elements=p;
        		int q=dat.length-p+1;
        		cutoff=dat[q].dist;
        		//System.out.println("Cutoff: "+dat[q].dist+" "+q+" "+upgma.maxDist+" "+cutoff);
        		setNodePositions();
    			ysize=elements*elespace;
        		
        		cutOffField.setValue(cutoff*maxDist);
        		int r=(int)Math.round(1000*(1-cutoff));
        		zoom.setValue(r);
        		paintPanel();
    			spg.paintImage(imf, scale);
    			paintDSG();
    			this.revalidate();
    			scrollPane.revalidate();
        		RECALC=true;
        	}
        	else if (source==cutOffField){
        		double p = (double)((Number)cutOffField.getValue()).doubleValue();
        		if (p<0){
        			p=0;
        			cutOffField.setValue(0);
        		}
        		
        		if (p>maxDist){
        			p=maxDist;
        			cutOffField.setValue(maxDist);
        		}
        		cutoff=(p/maxDist);
        		setNodePositions();
    			ysize=elements*elespace;
        		RECALC=false;
        		int q=(int)Math.round(1000*(1-cutoff));
        		zoom.setValue(q);
        		paintPanel();
    			spg.paintImage(imf, scale);
    			paintDSG();
    			this.revalidate();
    			scrollPane.revalidate();
    			leavesField.setValue(elements);
    			RECALC=true;
        	}
        }
        
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==branchLabels){
			String s=(String)branchLabels.getSelectedItem();
			branchLabelIndex=0;
			for (int i=0; i<labels.length; i++){
				if (s.equals(labels[i])){
					branchLabelIndex=i;
					i=labels.length;
				}
			}
			labelBranches();
		}
		else if(e.getSource()==songname){
			addsong=songname.isSelected();
		}
		else if(e.getSource()==popname){
			addpop=popname.isSelected();
		}
		else if(e.getSource()==indname){
			addind=indname.isSelected();
		}
		else if(e.getSource()==specname){
			addspec=specname.isSelected();
		}
		else if (e.getSource()==typename){
			addtype=typename.isSelected();
		}

		paintPanel();
		spg.paintImage(imf, scale);
		spg.revalidate();
	}
	
	public void export(){
		
		String[] names=cr.getNames();
		
		JPanel optionPanel=new JPanel();
		
		//JLabel choose=new JLabel();
		
		JRadioButton dendro=new JRadioButton("Raw tree data");
		JRadioButton validation=new JRadioButton("Validation statistics");
		JRadioButton repSize=new JRadioButton("Repertoire sizes");
		JRadioButton classif=new JRadioButton("Classification stats");
		JRadioButton central=new JRadioButton("Centrality stats");
		
		//optionPanel.add(choose);
		optionPanel.add(dendro);
		optionPanel.add(validation);
		optionPanel.add(repSize);
		optionPanel.add(classif);
		optionPanel.add(central);
		
		int co=JOptionPane.showConfirmDialog(this, optionPanel, "Select items to save", JOptionPane.OK_CANCEL_OPTION);
		System.out.println("CO out "+co);							
		if (co==0){
			boolean rawOut=dendro.isSelected();
			boolean vOut=validation.isSelected();
			boolean rOut=repSize.isSelected();
			boolean cOut=classif.isSelected();
			boolean centOut=central.isSelected();
			
			SaveDocument sd=new SaveDocument(this, defaults);
			boolean readyToWrite=sd.makeFile();
			if (readyToWrite){
		
				int j=avsils[0].length-1;
				if (vOut){
					for (int i=size1; i>=0; i--){
			
						sd.writeDouble(dat[i].dist*maxDist);
						for (int k=0; k<avsils.length; k++){
							sd.writeDouble(avsils[k][j]);
						}
						j--;
						sd.writeLine();
					}
				}
			
			
				if (cOut){
					sd.writeSheet("Classifications");
					sd.writeDouble(cutoff);
					sd.writeLine();
					sd.writeString("y points");
					//sd.writeString("upgma scores");
					
					//sd.writeLine();
			
					int[][] cats=upgma.calculateClassificationMembers(500);
					//float[][] scores=upgma.calculateMeanClusterDistances(100);
			
					for(int i=1; i<cats[0].length; i++){
						sd.writeInt(i+1);
					}
					sd.writeLine();
					
					for (int i=0; i<upgma.getLength(); i++){
						sd.writeString(names[i]);
						
				
						for	(j=1; j<cats[i].length; j++){
							sd.writeInt(cats[i][j]);
						}

						sd.writeLine();
					}
					
					sd.writeSheet("Cluster dist");
					MultivariateDispersionTest mdt=new MultivariateDispersionTest(upgma.getInput(), upgma);
					double[][] clusterDev=mdt.getClusterDev();
					
					for (int i=0; i<clusterDev[1].length; i++){
						sd.writeString(names[i]);
				
						for	(j=1; j<clusterDev.length; j++){
							sd.writeDouble(clusterDev[j][i]);
						}
						sd.writeLine();
					}
				}
				if (centOut){
					sd.writeSheet("Centrality");
					sd.writeDouble(cutoff);
					sd.writeLine();
					sd.writeString("y points");
					//sd.writeString("upgma scores");
					sd.writeString("node children...");
					sd.writeLine();
			
					int[][] cats=upgma.calculateClassificationMembers(100);
					//float[][] scores=upgma.calculateMeanClusterDistances(100);
			
					double[][] dist=cr.getDiss();
					
					for (int i=0; i<upgma.getLength(); i++){
						sd.writeString(names[i]);
						
				
						for	(j=1; j<cats[i].length; j++){
							double p=0;
							double q=0;
							for (int k=0; k<upgma.getLength(); k++){
								if (cats[i][j]==cats[k][j]){
									if (i<k){
										p+=dist[k][i];
										q++;
									}
									if (i>k){
										p+=dist[i][k];
										q++;
									}
								}
							}
							if (q==0){q=1;}
							sd.writeDouble(p/q);
						}

						sd.writeLine();
					}
					
					
					
				}
				if (rawOut){
					sd.writeSheet("Raw data");
					
					for (int i=0; i<dat.length; i++){
						sd.writeDouble(dat[i].xplace);
						sd.writeDouble(dat[i].dist);
						for (j=0; j<dat[i].children; j++){
							sd.writeString(names[dat[i].child[j]]);
						}
						sd.writeLine();
					}
				}
				sd.finishWriting();
			}
		}
	}

	public void saveImage(){
	
		int x1=imf.getWidth();
		int y1=imf.getHeight();
		int x2=dsg.imf.getWidth();
		int y2=dsg.imf.getHeight();
		
		int x=x1;
		if (x2>x1){x=x2;}
		int y=y1+y2;
	
	
		BufferedImage imt=new BufferedImage(x, y, BufferedImage.TYPE_INT_ARGB);
	
		Graphics2D g=imt.createGraphics();
		g.drawImage(dsg.imf, 0, 0, this);
		g.drawImage(imf, 0, y2, this);
		g.dispose();
	
		SaveImage si=new SaveImage(imt, this, defaults);
		//si.save();
	}
	
	public BufferedImage resizeImage(double ratio){
		
		int archiveWidth=width;
		int archiveHeight=height;
		int archiveElespace=elespace;
		
		width=(int)Math.round(width*ratio);
		height=(int)Math.round(height*ratio);
		elespace=(int)Math.round(elespace*ratio);
		
		//makeImage();
		//updateDisplay(cutoff);
		
		redisplay();
		
		int x1=imf.getWidth();
		int y1=imf.getHeight();
		int x2=dsg.imf.getWidth();
		int y2=dsg.imf.getHeight();
		
		int x=x1;
		if (x2>x1){x=x2;}
		int y=y1+y2;
		
		
		BufferedImage imt=new BufferedImage(x, y, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g=imt.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0,0,x,y);
		g.drawImage(dsg.imf, 0, 0, this);
		g.drawImage(imf, 0, y2, this);
		g.dispose();
		
		
		width=archiveWidth;
		height=archiveHeight;
		elespace=archiveElespace;
		//makeImage();
		//updateDisplay(cutoff);
		
		redisplay();
		
		return imt;
	}
	
}

