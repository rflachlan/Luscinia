package lusc.net.github.ui.statistics;
//
//  DisplayDendrogram.java
//  Luscinia
//
//  Created by Robert Lachlan on 10/2/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.

import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.awt.Font;

import lusc.net.github.Defaults;
import lusc.net.github.analysis.AnalysisGroup;
import lusc.net.github.analysis.dendrograms.TreeDat;

public class DisplayDendrogram extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1148310531110312540L;
	Defaults defaults;
	int ysize=600;
	int ydisp=10;
	int xdisp=510;
	int xdisp2=500;
	double maxdist=100;
	double maxY=0;
	
	int size=0;
	int size1=0;	
	int unitSize=0;
	int amt=0;
	int elespace=10;
	int elespace2=10;
	int maxy=1500;
	int clickedX=-1;
	int clickedY=-1;
	int clickedLoc=-1;
		
	boolean enabled=false;
	boolean greyscale=true;
	boolean includeNodeMarkers=false;
	boolean displayColors=false;
	boolean displayArcs=false;
	boolean displayText=false;
	
	int width, ygap, elements;
	protected Font bodyFont;
	
	BufferedImage imf;
	Color[] colors=null;
	TreeDat[] dat;
	String[] name;
	AnalysisGroup sg;
	int dataType=0; 
	
	double silhouettes[]=null;
	double avsils[][]=null;
	double nodeMarkers[]=null;
	
	public DisplayDendrogram (TreeDat[] dat, String[] name, AnalysisGroup sg, int dataType, int width, int elespace, int maxy, int unitSize, Defaults defaults){
		this.sg=sg;
		this.dataType=dataType;
		this.width=width;
		this.elespace=elespace;
		this.maxy=maxy;
		this.dat=dat;	
		this.name=name;
		this.unitSize=unitSize;
		this.defaults=defaults;
		makeColorPalette();
		startDisplaying(0);
		includeNodeMarkers=false;
	}	
	
	public DisplayDendrogram (TreeDat[] dat, String[] name, AnalysisGroup sg, int dataType, int width, int elespace, int maxy, int unitSize, double[] nodeMarkers, Defaults defaults){
		this.sg=sg;
		this.dataType=dataType;
		this.width=width;
		this.elespace=elespace;
		this.maxy=maxy;
		this.dat=dat;	
		this.name=name;
		this.unitSize=unitSize;
		this.nodeMarkers=nodeMarkers;
		this.defaults=defaults;
		makeColorPalette();
		startDisplaying(0);
		includeNodeMarkers=true;
	}	
	
	public void makeColorPalette(){
		colors=new Color[101];
		for (int i=0; i<101; i++){
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
		}
	}
		
	public void startDisplaying(double cutoff){
		size=dat.length;
		size1=size-1;
		bodyFont  = new Font("Arial", Font.PLAIN, elespace);
        if (bodyFont == null) {
            bodyFont = new Font("SansSerif", Font.PLAIN, elespace);
        }
		xdisp2=width-250;
		xdisp=xdisp2+10;
		setNodePositions(cutoff);		
		paintPanel(cutoff);				
	}
	
	public void updateDendrogram(double cutoff){
		setNodePositions(cutoff);
		paintPanel(cutoff);
	}
	
	
	public void setNodePositions(double cutoff){
		double xrt=0;
		int i,j,k;
		maxY=0;
		double x, xch;
		elements=0;
		int width2=width-30;
		for (i=0; i<size; i++){
			xrt=dat[i].dist-cutoff;
			dat[i].xrt=xrt;
			if (xrt<0){xrt=0;}
			
			dat[i].xloc=width2*(1-(xrt/(1-cutoff)))+5;
			
			//dat[i].xloc=xdisp-((xrt)*xdisp2/(1-cutoff));
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
		elespace2=elespace;
		
		ysize=elements*elespace2;
		
		if ((ysize>maxy)&&(elespace2>10)){
			elespace2=maxy/elements;
			if (elespace2<10){elespace2=10;}
			ysize=elespace2*elements;
		}
		
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
	
	public void paintPanel(double cutoff){
		
		int ny=elespace2*elements+2*ydisp;
		
		System.out.println(ny+" "+elespace2+" "+elements);
		
		imf=new BufferedImage(width, ny, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=imf.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0,0, width, ny);

		int i, j, xpl1, xpl2, ypl1, ypl2;

		BasicStroke bs=new BasicStroke(unitSize);
		
		g.setStroke(bs);
		
		xpl1=(int)Math.round(dat[size1].xloc);
		ypl1=(int)Math.round(dat[size1].yloc);
		//g.fillArc(xpl1-dia1, ypl1-dia1, dia2, dia2, 0, 360);
		g.setColor(Color.BLACK);
		NumberFormat formatter = new DecimalFormat("0.000");
		Font labelFont=new Font("SansSerif", Font.PLAIN, 20);
		FontMetrics fm=g.getFontMetrics(labelFont);

		g.setFont(labelFont);
		for (i=size1; i>=0; i--){
			xpl1=(int)Math.round(dat[i].xloc);
			ypl1=(int)Math.round(dat[i].yloc);
			if (dat[i].children>1){
				for (j=0; j<2; j++){
					int q=dat[i].daughters[j];
					xpl2=(int)Math.round(dat[q].xloc);
					ypl2=(int)Math.round(dat[q].yloc);
					int p=(int)Math.round(100*dat[q].colval);
					if ((p<0)||(p>colors.length)){
							g.setColor(Color.BLACK);
					}
					else{
						if (displayColors){
							g.setColor(colors[p]);
						}
						if (displayArcs){
							int xwid=(int)Math.round(elespace2*dat[q].colval);
							int xlo=(xpl1+xpl2-xwid)/2;
							int ylo=ypl2-(xwid/2);
							g.fillArc(xlo, ylo, xwid, xwid, 0, 360);
						}
						if (displayText){
							String s = formatter.format(dat[q].colval);
							int w2=fm.stringWidth(s);
							int x2=(xpl1+xpl2-w2)/2;
							if (x2+w2>=xpl2){
								x2=xpl2-w2-1;
							}
							if (x2<=xpl1){
								x2=xpl1+1;
							}
							int y2=ypl2-2;
							g.drawString(s, x2, y2);	
						}
					}
					g.drawLine(xpl1, ypl1, xpl1, ypl2);
					g.drawLine(xpl1, ypl2, xpl2, ypl2);
				}
			}
			//if ((xpl1>=0)&&(includeNodeMarkers)){
			//	dia1=adjNodeMarkers[i]/2;
			//	dia2=adjNodeMarkers[i];
			//	g.fillArc(xpl1-dia1, ypl1-dia1, dia2, dia2, 0, 360);
			//}

			//else if (cutoff==0){
			//	g.setColor(Color.BLACK);
			//	g.setFont(bodyFont);
			//	g.drawString(name[dat[i].child[0]], xpl1+5, ypl1+5);
			//}
		}
		g.dispose();
	}
	
	public LinkedList<int[]> getExamples(int maxExamples){
	
		int numExamples=maxExamples;
	
		LinkedList<int[]> p=new LinkedList<int[]>();
		int i, j, k, ypl2;
		if (elespace2>=maxExamples){
			
			for (i=size1; i>=0; i--){
				if (dat[i].children>1){
					for (j=0; j<2; j++){
						int q=dat[i].daughters[j];
						if (dat[q].children<=1){
							
							ypl2=(int)Math.round(dat[q].yloc);
							int ne=numExamples;
							int s=dat[q].child.length;
							if (s<ne){
								ne=s;
							}
							
							int[] choices=new int[ne+2];
							choices[0]=ypl2;
							choices[1]=elespace2;
							double ine=1/(ne+0.0);
							for (k=0; k<ne; k++){
								int c=(int)Math.round(k*ine*s);
								choices[k+2]=dat[q].child[c];
							}
							p.add(choices);
						}
					}
				}
			}
		}
		return p;
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);  //paint background
		g.drawImage(imf, 0, 0, this);
	}
	
	public int[] getClosest(int x, int y){
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
		
		int[] results={clickedLoc, clickedY, clickedX};
		return results;
	}
	
	public int[] calculateNodeMarkers(){
	
		double maxNodeSize=-100000;
		double minNodeSize=1000000;
		for (int i=0; i<nodeMarkers.length-1; i++){
			if (maxNodeSize<nodeMarkers[i]){
				maxNodeSize=nodeMarkers[i];
			}
			if (minNodeSize>nodeMarkers[i]){
				minNodeSize=nodeMarkers[i];
			}

		}
		//maxNodeSize-=0.3;
		maxNodeSize-=minNodeSize;
		
		//maxNodeSize=Math.log(1.5);
		//minNodeSize=Math.log(1/1.5);
		//maxNodeSize-=minNodeSize;
		
		int[] out=new int[nodeMarkers.length];
		for (int i=0; i<nodeMarkers.length; i++){
			out[i]=(int)(2*ydisp*(nodeMarkers[i]-minNodeSize)/maxNodeSize);
			if (out[i]<1){out[i]=1;}
			if (out[i]>2*ydisp){out[i]=2*ydisp;}
		}
		return out;
	}

}

