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
import lusc.net.github.analysis.dendrograms.TreeDat;
import lusc.net.github.analysis.dendrograms.UPGMA;
import lusc.net.github.analysis.multivariate.MultivariateDispersionTest;
import lusc.net.github.ui.SaveDocument;
import lusc.net.github.ui.SaveImage;
import lusc.net.github.ui.SpectrogramSideBar;

public class DisplayUPGMA extends DisplayPane implements MouseInputListener, PropertyChangeListener, ChangeListener{
	
	Defaults defaults;
	int ysize=600;
	int ydisp=10;
	int xdisp=510;
	int xdisp2=500;
	int xpl1, ypl1;
	double maxdist=100;
	double maxY=0;
	
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
	AnalysisGroup sg;
	SpectrogramSideBar ssb;
	DrawSilhouetteGraph dsg=new DrawSilhouetteGraph();
	SimplePaintingPanel spg=new SimplePaintingPanel();
	double silhouettes[]=null;
	double avsils[][]=null;
	NumberFormat num, num2;
	boolean RECALC=true;
	double maxDist;
	
	public DisplayUPGMA (UPGMA upgma, AnalysisGroup sg, int dataType, int width, int height, Defaults defaults){
		this.upgma=upgma;
		this.sg=sg;
		ssb=sg.getSSB();
		this.dataType=dataType;
		this.width=width;
		this.height=height;
		this.defaults=defaults;
		dat=upgma.getDat();
		maxDist=upgma.getMaxDist();
		startDisplaying();
	}	
		
	public DisplayUPGMA (UPGMA upgma, AnalysisGroup sg, int dataType, int width, int height, double[] silhouettes, double[][] avsils, Defaults defaults){
		this.upgma=upgma;
		this.sg=sg;
		ssb=sg.getSSB();
		this.dataType=dataType;
		this.width=width;
		this.height=height;
		this.silhouettes=silhouettes;
		this.avsils=avsils;
		this.defaults=defaults;
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

		bodyFont  = new Font("Arial", Font.PLAIN, elespace/2);
        if (bodyFont == null) {
            bodyFont = new Font("SansSerif", Font.PLAIN, elespace/2);
        }
		
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
		leavesField.setColumns(6);
		leavesField.setValue(new Integer(elements));
		leavesField.addPropertyChangeListener("value", this);
		
		cutOffField=new JFormattedTextField(num2);
		cutOffField.setColumns(6);
		cutOffField.setValue(new Integer(0));
		cutOffField.addPropertyChangeListener("value", this);
		
		leavesLabel=new JLabel("Number of leaves: ");
		leavesLabel.setBorder(BorderFactory.createEmptyBorder(10,10,10,20));
		cutOffLabel=new JLabel("Cut-off: ");
		cutOffLabel.setBorder(BorderFactory.createEmptyBorder(10,10,10,20));
		
		//status=new JLabel("Number of leaves: "+elements+" Cut-off: 0");
		//status.setBorder(BorderFactory.createEmptyBorder(10,10,10,20));
		paintPanel();
		spg.paintImage(imf);
		paintDSG();
		//spg.revalidate();
		//this.revalidate();
				
		scrollPane=new JScrollPane(spg.imagePanel);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setPreferredSize(new Dimension(width, height-400));
		spg.addMouseListener(this);
		JPanel topPane=new JPanel(new BorderLayout());
		topPane.add(zoom, BorderLayout.CENTER);
		
		JPanel settingsPane=new  JPanel();
		settingsPane.add(leavesLabel);
		settingsPane.add(leavesField);
		settingsPane.add(cutOffLabel);
		settingsPane.add(cutOffField);
		
		//topPane.add(status, BorderLayout.EAST);
		topPane.add(settingsPane, BorderLayout.EAST);
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
		System.out.println("1");
		paintPanel();
		System.out.println("2");
		//spg.paintImage(imf);
		System.out.println("3");
		//paintDSG();
		this.revalidate();
		System.out.println("4");
	}
	
	
	public void paintPanel(){
		
		
		int ny=elespace*elements+2*ydisp;
		System.out.println("UPGMA TREE HEIGHT: "+ny);
		imf=new BufferedImage(width, ny, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=imf.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0,0, width, ny);
		g.setColor(Color.BLACK);
		int i, j, k, colorroot, xpl2, ypl2;
		xpl1=(int)Math.round(dat[size1].xloc);
		ypl1=(int)Math.round(dat[size1].yloc);
		//g.fillArc(xpl1-1, ypl1-1, 3, 3, 0, 360);
		
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
		
		double[] labels=sg.getLabels(dataType);
		if (labels==null){labels=new double[1];}
		
		for (i=size1; i>=0; i--){
			xpl1=(int)Math.round(dat[i].xloc);
			ypl1=(int)Math.round(dat[i].yloc);
			
			if (dat[i].children>1){
				for (j=0; j<2; j++){
				
					int q=dat[i].daughters[j];
					
					xpl2=(int)Math.round(dat[q].xloc);
					ypl2=(int)Math.round(dat[q].yloc);
					
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
						//if (meanout>0.5){
						//	meanout=Math.sqrt(2*(meanout-0.5))*0.5+0.5;
						//}
						//else{
						//	meanout=0.5-Math.sqrt(2*(0.5-meanout))*0.5;
						//}
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
					//g.setColor(Color.BLACK);
					g.drawLine(xpl1, ypl1, xpl1, ypl2);
					g.drawLine(xpl1, ypl2, xpl2, ypl2);
					//g.fillArc(xpl2-1, ypl2-1, 3, 3, 0, 360);
				}
			}
			else if (cutoff==0){
				g.setColor(Color.BLACK);
				g.setFont(bodyFont);
				String[] names=sg.getNames(dataType);
				g.drawString(names[dat[i].child[0]], xpl1+5, ypl1+5);
			}
		}
		
		if (drawDottedLine){
			
			int yq=0;
			
			while (yq<ny){
				
				g.drawLine(dlLoc, yq, dlLoc, yq+10);
				yq+=20;
			}
		}
		
		g.setColor(Color.WHITE);
		g.fillRect(xpl1,0, 300, ydisp-1);
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
		

		dsg.paintGraphUPGMA(ypoints, xpoints, 1, 100, width, 90, xdisp, this);	
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
			
			spg.paintImageDot(imf, clickedX, clickedY);
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
        		spg.paintImage(imf);
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
    			spg.paintImage(imf);
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
    			spg.paintImage(imf);
    			paintDSG();
    			this.revalidate();
    			scrollPane.revalidate();
    			leavesField.setValue(elements);
    			RECALC=true;
        	}
        }
        
	}
	
	public void export(){
		
		String[] names=sg.getNames(dataType);
		
		JPanel optionPanel=new JPanel();
		
		//JLabel choose=new JLabel();
		
		JRadioButton dendro=new JRadioButton("Raw tree data");
		JRadioButton validation=new JRadioButton("Validation statistics");
		JRadioButton repSize=new JRadioButton("Repertoire sizes");
		JRadioButton classif=new JRadioButton("Classification stats");
		
		//optionPanel.add(choose);
		optionPanel.add(dendro);
		optionPanel.add(validation);
		optionPanel.add(repSize);
		optionPanel.add(classif);
		
		int co=JOptionPane.showConfirmDialog(this, optionPanel, "Select items to save", JOptionPane.OK_CANCEL_OPTION);
		System.out.println("CO out "+co);							
		if (co==0){
			boolean rawOut=dendro.isSelected();
			boolean vOut=validation.isSelected();
			boolean rOut=repSize.isSelected();
			boolean cOut=classif.isSelected();
			
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
					sd.writeString("node children...");
					sd.writeLine();
			
					int[][] cats=upgma.calculateClassificationMembers(100);
					//float[][] scores=upgma.calculateMeanClusterDistances(100);
			
					
					
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

