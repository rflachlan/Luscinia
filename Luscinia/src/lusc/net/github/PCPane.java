package lusc.net.github;
//
//  PCPane.java
//  Luscinia
//
//  Created by Robert Lachlan on 11/8/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.awt.font.*;
import java.awt.geom.Path2D;
import java.awt.geom.*;


public class PCPane extends JPanel implements MouseInputListener{
	
	BufferedImage imf;
	DisplayPC dpc;
	Defaults defaults;
	
	Dimension dim=new Dimension(700, 500);
	double[][]location=null;
	
	int[][] lineConnectors1;
	
	Color[] colorPalette={Color.BLUE, Color.RED, Color.YELLOW.darker(), Color.BLACK, Color.GREEN, Color.CYAN, Color.orange, Color.MAGENTA, Color.PINK, Color.GRAY};
	
	int width, height, dimensionX, dimensionY, labelType, dataType, clusterN;
	
	double[][] data;
	SongGroup sg;
	boolean connectors,gridlines, flipX, flipY, linked;
	
	
	
	
	
	
	boolean selectedPoint=false;
	boolean selectedArea=false;
	
	int maxx, maxy, minx, miny, startx, starty, endx, endy;
	
	int gridSizeDefault=400;
	int xshift1=50;
	int yshift1=50;
	int legendSpacerDefault=50;
	double iconSize=2;	
	
	float lineWeight=1.0f;
	
	float scaler=1.0f;
	
	boolean enabled=false;
	boolean selectionStarted=false;
	int[] selP;
	
	AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f);
	
	int fontSize=12;
	
	public PCPane(int width, int height, DisplayPC dpc, Defaults defaults){
		this.width=width;
		this.height=height;
		this.dpc=dpc;
		this.defaults=defaults;
		dim=new Dimension(width, height);
		imf=new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		//this.addMouseInputListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		
	}
	
	public Path2D getTriangle(double midX, double midY, double isi){
		
		//int ics=(int)Math.round(iconSize*scaler);
		double ics=isi*scaler;
		
		double[] xpoints=new double[3];
		double[] ypoints=new double[3];
		
		xpoints[0]=midX-ics;
		xpoints[1]=midX;
		xpoints[2]=midX+ics;
		
		ypoints[0]=midY-ics;
		ypoints[1]=midY+ics;
		ypoints[2]=midY-ics;		
		//Polygon p=new Polygon(xpoints, ypoints, 3);
		
		Path2D.Double tp=new Path2D.Double();
		tp.moveTo(xpoints[0], ypoints[0]);
		tp.lineTo(xpoints[1], ypoints[1]);
		tp.lineTo(xpoints[2], ypoints[2]);
		tp.lineTo(xpoints[0], ypoints[0]);
		
		return tp;
	}
	
	public Path2D getInvertTriangle(double midX, double midY, double isi){
		
		//int ics=(int)Math.round(iconSize*scaler);
		
		//int[] xpoints=new int[3];
		//int[] ypoints=new int[3];
		
		double ics=isi*scaler;
		
		double[] xpoints=new double[3];
		double[] ypoints=new double[3];
		
		xpoints[0]=midX-ics;
		xpoints[1]=midX;
		xpoints[2]=midX+ics;
		
		ypoints[0]=midY+ics;
		ypoints[1]=midY-ics;
		ypoints[2]=midY+ics;		
		//Polygon p=new Polygon(xpoints, ypoints, 3);
		//return p;
		
		Path2D.Double tp=new Path2D.Double();
		tp.moveTo(xpoints[0], ypoints[0]);
		tp.lineTo(xpoints[1], ypoints[1]);
		tp.lineTo(xpoints[2], ypoints[2]);
		tp.lineTo(xpoints[0], ypoints[0]);
		
		return tp;
	}
	
	public Path2D getSquare(double midX, double midY, double isi){
		
		//int ics=(int)Math.round(iconSize*scaler);
		
		//int[] xpoints=new int[4];
		//int[] ypoints=new int[4];
		
		double ics=isi*scaler;
		
		double[] xpoints=new double[4];
		double[] ypoints=new double[4];
		
		xpoints[0]=midX-ics;
		xpoints[1]=midX-ics;
		xpoints[2]=midX+ics;
		xpoints[3]=midX+ics;
		
		ypoints[0]=midY-ics;
		ypoints[1]=midY+ics;
		ypoints[2]=midY+ics;	
		ypoints[3]=midY-ics;		
		//Polygon p=new Polygon(xpoints, ypoints, 4);
		//return p;
		
		Path2D.Double tp=new Path2D.Double();
		tp.moveTo(xpoints[0], ypoints[0]);
		tp.lineTo(xpoints[1], ypoints[1]);
		tp.lineTo(xpoints[2], ypoints[2]);
		tp.lineTo(xpoints[3], ypoints[3]);
		tp.lineTo(xpoints[0], ypoints[0]);
		
		return tp;
		
	}
	
	public Path2D getDiamond(double midX, double midY, double isi){
		
		//int ics=(int)Math.round(iconSize*scaler);
		
		//int[] xpoints=new int[4];
		//int[] ypoints=new int[4];
		
		double ics=isi*scaler;
		
		double[] xpoints=new double[4];
		double[] ypoints=new double[4];
		
		
		xpoints[0]=midX;
		xpoints[1]=midX-ics;
		xpoints[2]=midX;
		xpoints[3]=midX+ics;
		
		ypoints[0]=midY+ics;
		ypoints[1]=midY;
		ypoints[2]=midY-ics;	
		ypoints[3]=midY;		
		//Polygon p=new Polygon(xpoints, ypoints, 4);
		//return p;
		
		Path2D.Double tp=new Path2D.Double();
		tp.moveTo(xpoints[0], ypoints[0]);
		tp.lineTo(xpoints[1], ypoints[1]);
		tp.lineTo(xpoints[2], ypoints[2]);
		tp.lineTo(xpoints[3], ypoints[3]);
		tp.lineTo(xpoints[0], ypoints[0]);
		
		return tp;
		
	}
	
	public Path2D getCross(double midX, double midY, double isi){
		
		double ics=isi*scaler;
		
		double[] xpoints=new double[4];
		double[] ypoints=new double[4];
		
		
		xpoints[0]=midX-ics;
		xpoints[1]=midX+ics;
		xpoints[2]=midX-ics;
		xpoints[3]=midX+ics;
		
		ypoints[0]=midY+ics;
		ypoints[1]=midY-ics;
		ypoints[2]=midY-ics;	
		ypoints[3]=midY+ics;		
		
		Path2D.Double tp=new Path2D.Double();
		tp.moveTo(xpoints[0], ypoints[0]);
		tp.lineTo(xpoints[1], ypoints[1]);
		tp.moveTo(xpoints[2], ypoints[2]);
		tp.lineTo(xpoints[3], ypoints[3]);
		
		return tp;
		
	}
	
	public Ellipse2D getCircle(double midX, double midY, double isi){
		
		double ics=isi*scaler;		
		double ic2=2*ics+1;
		Ellipse2D.Double ell=new Ellipse2D.Double(midX-ics, midY-ics, ic2, ic2);
		
		return ell;
		
	}
	
	
	
	
	
	public float[] getColorScore(float p){
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
	
	public void paintIcon(int c, double x, double y, double isi, boolean drawEdge, Graphics2D g){
		int i=c;
		while(i>6){i-=6;}
		
		if (i==0){
			Ellipse2D ell=getCircle(x, y, isi);
			g.fill(ell);
			if (drawEdge){
				g.setColor(Color.BLACK);
				g.draw(ell);
			}
		}
		else {
			Path2D po=null;
			if (i==1){
				po=getTriangle(x, y, isi);
			}
			if (i==2){
				po=getSquare(x, y, isi);
			}
			if (i==3){
				po=getDiamond(x, y, isi);
			}
			if (i==4){
				po=getTriangle(x, y, isi);
			}
			if (i==5){
				po=getInvertTriangle(x, y, isi);
			}
			if (i==6){
				po=getCross(x, y, isi);
			}
			if (i<6){
				g.fill(po);
				if (drawEdge){
					g.setColor(Color.BLACK);
					g.draw(po);
				}
			}
			else{
				g.draw(po);
			}
		}

	}
	
	public Color[] getColorPalette(int n){
	
		Color[] palette=new Color[n];
		int j=0;
		for (int i=0; i<n; i++){
			
			if(j==colorPalette.length){j=0;}
			palette[i]=colorPalette[j];
			j++;
		}
		
		return palette;
	}
	

	public void paintPanel(SongGroup sg, double[][] data, int dimensionX, int dimensionY, int labelType, int dataType, int clusterN, boolean connectors, boolean gridlines, boolean flipX, boolean flipY, boolean linked){
		
		this.dimensionX=dimensionX;
		this.dimensionY=dimensionY;
		this.sg=sg;
		this.data=data;
		this.labelType=labelType;
		this.dataType=dataType;
		this.clusterN=clusterN;
		this.connectors=connectors;
		this.gridlines=gridlines;
		this.flipX=flipX;
		this.flipY=flipY;
		this.linked=linked;
		paintPanel();
		repaint();
	}
		
	public void paintPanel(){	
		int n=data.length;
		
		double absmax1=0;
		double absmin1=0;
		double absmax2=0;
		double absmin2=0;
		for (int i=0; i<n; i++){
			if (data[i][dimensionX]>absmax1){
				absmax1=data[i][dimensionX];
			}
			if (data[i][dimensionX]<absmin1){
				absmin1=data[i][dimensionX];
			}
			if (data[i][dimensionY]>absmax2){
				absmax2=data[i][dimensionY];
			}
			if (data[i][dimensionY]<absmin2){
				absmin2=data[i][dimensionY];
			}
		}
		
		double diff1=absmax1-absmin1;
		double diff2=absmax2-absmin2;
		double mult=Math.max(diff1,  diff2);
		int gridMult=(int)Math.floor(5.000*mult);
		gridMult*=2;
		double absmax=gridMult*0.05;
		System.out.println(absmax+" "+gridMult);
		
		Graphics2D g=imf.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0,0,imf.getWidth(), imf.getHeight());
		g.setColor(Color.BLACK);
		BasicStroke fs=new BasicStroke(lineWeight*scaler, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
		g.setStroke(fs);
		BasicStroke fs2=new BasicStroke(lineWeight*scaler*2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
		BasicStroke fs3=new BasicStroke(lineWeight*scaler*10, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
		
		
		RenderingHints hints =new RenderingHints(RenderingHints.KEY_RENDERING,
				 RenderingHints.VALUE_RENDER_QUALITY);
				hints.put(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		g.setRenderingHints(hints);
		int fsz=(int)Math.round(fontSize*scaler);
		
		Font fontDef=g.getFont();
		Font font=new Font(fontDef.getName(), fontDef.getStyle(), fsz);
		g.setFont(font);
		DecimalFormat nf=new DecimalFormat("#.#");
        FontRenderContext frc = g.getFontRenderContext();
        
        int gridsize=(int)Math.round(gridSizeDefault*scaler);
		
		int xsh=(int)Math.round(xshift1*scaler);
		int ysh=(int)Math.round(yshift1*scaler);
		
		int legendSpacer=(int)Math.round(legendSpacerDefault*scaler);
		
		int tickLength=(int)Math.round(3*scaler);
		int textSpacer=(int)Math.round(5*scaler);
		int ics=(int)Math.round(iconSize*scaler);
		
		int barWidth=(int)Math.round(25*scaler);
		int barHeight=(int)Math.round(200*scaler);
		int iconSpacer=(int)Math.round(25*scaler);
		
		double x,y;
		location=new double[n][2];
		
		Color[] colors=new Color[101];
		
		Color[] palette=new Color[clusterN];
		
		if (labelType==0){
			g.setColor(Color.BLACK);
		}
		else if (labelType==1){
			palette=getColorPalette(sg.populations.length);
			for (int i=0; i<sg.populations.length; i++){

				g.setColor(palette[i]);
				int xe=xsh+gridsize+legendSpacer;
				int ye=ysh+i*iconSpacer;
				paintIcon(i, xe, ye, iconSize, false, g);
				g.setColor(Color.BLACK);
				TextLayout layout = new TextLayout(sg.populations[i], font, frc);
				Rectangle r = layout.getPixelBounds(null, 0, 0);
				layout.draw(g, xe+ics*2+textSpacer, ye+0.5f*r.height);
			}		
		}
		else if (labelType==2){
			
			for (int i=0; i<barHeight; i++){
				
				float ii=i/(barHeight-1.0f);
				float[]f=getColorScore(ii);
				Color tc=new Color(f[0], f[1], f[2]);
				g.setColor(tc);
				g.fillRect(xsh+gridsize+legendSpacer, i+ysh, barWidth, 1);
				
			}
			
			g.setColor(Color.BLACK);
			g.drawRect(xsh+gridsize+legendSpacer, ysh, barWidth, barHeight);
			
			for (int i=0; i<=10; i++){
				int ypl=(int)Math.round(ysh+i*barHeight*0.1);
				g.drawLine(xsh+gridsize+legendSpacer+barWidth, ypl, xsh+gridsize+legendSpacer+barWidth+tickLength, ypl); 
			}
			
			TextLayout layoutS = new TextLayout("Start", font, frc);
			Rectangle r1 = layoutS.getPixelBounds(null, 0, 0);
			layoutS.draw(g, xsh+gridsize+legendSpacer+barWidth+textSpacer, ysh+0.5f*r1.height);
			TextLayout layoutE = new TextLayout("End", font, frc);
			Rectangle r2 = layoutE.getPixelBounds(null, 0, 0);
			layoutE.draw(g, xsh+gridsize+legendSpacer+barWidth+textSpacer, ysh+barHeight+0.5f*r2.height);
			TextLayout layoutM = new TextLayout("Mid", font, frc);
			Rectangle r3 = layoutM.getPixelBounds(null, 0, 0);
			layoutM.draw(g, xsh+gridsize+legendSpacer+barWidth+textSpacer, ysh+barHeight*0.5f+0.5f*r2.height);
			
		}
		
		else if (labelType==3){
			palette=getColorPalette(clusterN);
			for (int i=0; i<clusterN; i++){
				
				g.setColor(palette[i]);
				int xe=xsh+gridsize+legendSpacer;
				int ye=ysh+i*iconSpacer;
				paintIcon(i, xe, ye, iconSize, false, g);
				
				g.setColor(Color.BLACK);
				TextLayout layout = new TextLayout(Integer.toString(i+1), font, frc);
				Rectangle r = layout.getPixelBounds(null, 0, 0);
				int pp=(int)Math.round(xe+2*iconSize+textSpacer);
				layout.draw(g, pp, ye+0.5f*r.height);				
			}			
		}
		
		else if (labelType==4){
			palette=getColorPalette(clusterN);
			for (int i=0; i<clusterN; i++){
				
				g.setColor(palette[i]);
				int xe=xsh+gridsize+100;
				int ye=ysh+i*iconSpacer;
				paintIcon(i, xe, ye, iconSize, false, g);
				
				g.setColor(Color.BLACK);
				TextLayout layout = new TextLayout(Integer.toString(i+1), font, frc);
				Rectangle r = layout.getPixelBounds(null, 0, 0);
				int pp=(int)Math.round(xe+2*iconSize+textSpacer);
				layout.draw(g, pp, ye+0.5f*r.height);
							
			}		
		}
		
		else{
			palette=getColorPalette(clusterN);
			for (int i=0; i<clusterN; i++){
				
				g.setColor(palette[i]);
				int xe=xsh+gridsize+100;
				int ye=ysh+i*iconSpacer;
				paintIcon(i, xe, ye, iconSize, false, g);
				
				g.setColor(Color.BLACK);
				TextLayout layout = new TextLayout(Integer.toString(i+1), font, frc);
				Rectangle r = layout.getPixelBounds(null, 0, 0);
				int pp=(int)Math.round(xe+2*iconSize+textSpacer);
				layout.draw(g, pp, ye+0.5f*r.height);
							
			}		
		}
		
		
		double tenth=gridsize/(mult*10);
		int half=gridsize/2;
		if(gridlines){
			g.setColor(Color.LIGHT_GRAY);
			for (int i=1; i<5; i++){
				int q=(int)Math.round(tenth*i);
				if (q<half){
					g.drawLine(xsh+half+q, ysh, xsh+half+q, ysh+gridsize);
					g.drawLine(xsh, ysh+half+q, xsh+gridsize, ysh+half+q);
					g.drawLine(xsh+half-q, ysh, xsh+half-q, ysh+gridsize);
					g.drawLine(xsh, ysh+half-q, xsh+gridsize, ysh+half-q);
				}
			}
		}
		g.setColor(Color.BLACK);
		for (int i=1; i<5; i++){
			int q=(int)Math.round(tenth*i);
			if (q<half){
				g.drawLine(xsh+half+q, ysh+gridsize, xsh+half+q, ysh+gridsize+tickLength);
				g.drawLine(xsh-tickLength, ysh+half+q, xsh, ysh+half+q);
				g.drawLine(xsh+half-q, ysh+gridsize, xsh+half-q, ysh+gridsize+tickLength);
				g.drawLine(xsh-tickLength, ysh+half-q, xsh, ysh+half-q);
			}
		}
		
		g.drawLine(xsh+half, ysh, xsh+half, ysh+gridsize);
		g.drawLine(xsh, ysh+half, xsh+gridsize, ysh+half);
		
		g.drawLine(xsh, ysh, xsh+gridsize, ysh);
		g.drawLine(xsh, ysh+gridsize, xsh+gridsize, ysh+gridsize);
		
		g.drawLine(xsh, ysh, xsh, ysh+gridsize);
		g.drawLine(xsh+gridsize, ysh, xsh+gridsize, ysh+gridsize);
		//g.drawString("Canonical variable 1", 200+xsh, ysh+575);

		for (int i=0; i<=5; i++){
			int q=(int)Math.round(tenth*i);
			if (q<half){
				
				for (int k=-1; k<2; k+=2){
					if ((i>0)||(k>0)){
						double j=i*0.1*k;
						String s=nf.format(j);
						TextLayout layout = new TextLayout(s, font, frc);
						Rectangle r = layout.getPixelBounds(null, 0, 0);
			
						float x1=(float)(k*q-0.5f*r.width+xsh+half);
						float y1=ysh+gridsize+textSpacer+r.height;
						float x2=xsh-textSpacer-r.width;
						float y2=(float)(ysh+half-k*q+0.5f*r.height);
			
						layout.draw(g, x1, y1);
						layout.draw(g, x2, y2);
					}
				}
			}
		}
		
		double adj1=0.5*(absmax1+absmin1);
		double adj2=0.5*(absmax2+absmin2);
		g.setStroke(fs);
		for (int i=0; i<n; i++){
			
			double px=(data[i][dimensionX]-adj1)/(mult);
			px+=0.5;
			if (flipX){px=1-px;}
			double py=(data[i][dimensionY]-adj2)/(mult);
			py+=0.5;
			if (flipY){py=1-py;}
			
			
			x=px*gridsize+xsh;
			y=ysh+gridsize-py*gridsize;
			
			location[i][0]=x;
			location[i][1]=y;
			if ((connectors)&&(i>0)){
				
				if (labelType==2){
					float p=0;
					if ((dataType==2)&&(sg.syllLabels!=null)){
						p=(float)(0.5*(sg.syllLabels[i]+sg.syllLabels[i-1]));
					}
					else if ((dataType<2)&&(sg.eleLabels!=null)){
						p=(float)(0.5*(sg.eleLabels[i]+sg.eleLabels[i-1]));
					}
					else if ((dataType==3)&&(sg.transLabels!=null)){
						p=(float)(0.5*(sg.transLabels[i]+sg.transLabels[i-1]));
					}
					else if ((dataType==4)&&(sg.songLabels!=null)){
						p=(float)sg.songLabels[i];
					}
					if ((p<0)||(p>1)){
						g.setColor(Color.BLACK);
					}
					else{
						float[] c=getColorScore(p);
						g.setColor(new Color(c[0], c[1], c[2], 0.25f));
					}
				}
				else{
					g.setColor(new Color(0.25f, 0.25f, 0.25f, 0.25f));
				}
				boolean toDraw=false;
				if ((dataType<2)&&(sg.eleLabels[i]>0)){toDraw=true;}
				if ((dataType==2)&&(sg.syllLabels[i]>0)){toDraw=true;}
				if ((dataType==3)&&(sg.transLabels[i]>0)){toDraw=true;}
				if (toDraw){
					Line2D.Double line=new Line2D.Double(location[i-1][0], location[i-1][1], location[i][0], location[i][1]);
					g.draw(line);
				}
			}
		}
		
		g.setColor(Color.BLACK);
		for (int i=0; i<n; i++){
			
			x=location[i][0];
			y=location[i][1];
			
			if(labelType==0){
				paintIcon(0, x, y, iconSize, false, g);
			}
			else if (labelType==2){
				double p=0;
				if ((dataType==2)&&(sg.syllLabels!=null)){
					p=sg.syllLabels[i];
				}
				else if ((dataType<2)&&(sg.eleLabels!=null)){
					p=sg.eleLabels[i];
				}
				else if ((dataType==3)&&(sg.transLabels!=null)){
					p=sg.transLabels[i];
				}
				else if ((dataType==4)&&(sg.songLabels!=null)){
					p=sg.songLabels[i];
				}
				float q=(float)p;
				if ((q<0)||(q>1)){
					System.out.println("ALERT!!! Color outside limit. "+p);
					g.setColor(Color.BLACK);
				}
				else{
					float[] c=getColorScore(q);
					g.setColor(new Color(c[0], c[1], c[2]));
				}
				paintIcon(0, x, y, iconSize, false, g);
			}
			else if (labelType==1){
				int q=sg.lookUpPopulation(dataType, i);
				g.setColor(palette[q]);
				paintIcon(q, x, y, iconSize, false,  g);
			}	
			else if (labelType==3){				
				int best=dpc.km.overallAssignments[clusterN-2][i];
				g.setColor(palette[best]);
				while(best>6){best-=6;}
				paintIcon(best, x,y,iconSize, false, g);
			}
			
			else if (labelType==4){
				int best=dpc.ent.overallAssignments[clusterN-2][i];
				g.setColor(palette[best]);
				while(best>6){best-=6;}
				paintIcon(best, x,y,iconSize, false, g);
			}	
			else if (labelType==5){
				int best=dpc.snn.DBSCANclusters[i];
				g.setColor(palette[best]);
				while(best>6){best-=6;}
				paintIcon(best, x,y,iconSize, false, g);
			}
		}
		
		//DRAW SONGLINE!
		if ((selP!=null)&&(linked)){
			g.setStroke(fs2);
			for (int i=0; i<n; i++){
			
				x=location[i][0];
				y=location[i][1];
				
				boolean toDraw=false;
				int ref2=-1;
				double ref1=-1;
				if (dataType<2){ref1=sg.eleLabels[i]; ref2=sg.lookUpEls[i][0];}
				if (dataType==2){ref1=sg.syllLabels[i]; ref2=sg.lookUpSyls[i][0];}
				if (dataType==3){ref1=sg.transLabels[i]; ref2=sg.lookUpTrans[i][0];}
				if(ref2>=0){
					for (int z=0; z<selP.length; z++){
						if ((dataType<2)&&(ref2==sg.lookUpEls[selP[z]][0])){toDraw=true;}
						if ((dataType==2)&&(ref2==sg.lookUpSyls[selP[z]][0])){toDraw=true;}
						if ((dataType==3)&&(ref2==sg.lookUpTrans[selP[z]][0])){toDraw=true;}
					}
				}
				if (toDraw){
					if (ref1>0){
						g.setColor(Color.BLACK);
						Line2D.Double line=new Line2D.Double(location[i-1][0], location[i-1][1], location[i][0], location[i][1]);
						g.draw(line);
					}
				}
			}
			for (int i=0; i<n; i++){
				
				x=location[i][0];
				y=location[i][1];
				
				boolean toDraw=false;
				int ref2=-1;
				double ref1=-1;
				if (dataType<2){ref1=sg.eleLabels[i]; ref2=sg.lookUpEls[i][0];}
				if (dataType==2){ref1=sg.syllLabels[i]; ref2=sg.lookUpSyls[i][0];}
				if (dataType==3){ref1=sg.transLabels[i]; ref2=sg.lookUpTrans[i][0];}
				if(ref2>=0){
					for (int z=0; z<selP.length; z++){
						if ((dataType<2)&&(ref2==sg.lookUpEls[selP[z]][0])){toDraw=true;}
						if ((dataType==2)&&(ref2==sg.lookUpSyls[selP[z]][0])){toDraw=true;}
						if ((dataType==3)&&(ref2==sg.lookUpTrans[selP[z]][0])){toDraw=true;}
					}
				}
				if (toDraw){
					if(labelType==0){
						paintIcon(0, x, y, 2*iconSize, true, g);
					}
					else if (labelType==2){
						double p=0;
						if ((dataType==2)&&(sg.syllLabels!=null)){
							p=sg.syllLabels[i];
						}
						else if ((dataType<2)&&(sg.eleLabels!=null)){
							p=sg.eleLabels[i];
						}
						else if ((dataType==3)&&(sg.transLabels!=null)){
							p=sg.transLabels[i];
						}
						else if ((dataType==4)&&(sg.songLabels!=null)){
							p=sg.songLabels[i];
						}
						float q=(float)p;
						if ((q<0)||(q>1)){
							System.out.println("ALERT!!! Color outside limit. "+p);
							g.setColor(Color.BLACK);
						}
						else{
							float[] c=getColorScore(q);
							g.setColor(new Color(c[0], c[1], c[2]));
						}
						paintIcon(0, x, y, 2*iconSize, true, g);
					}
					else if (labelType==1){
						int q=sg.lookUpPopulation(dataType, i);
						g.setColor(palette[q]);
						paintIcon(q, x, y, 2*iconSize, true, g);
					}	
					else if (labelType==3){				
						int best=dpc.km.overallAssignments[clusterN-2][i];
						g.setColor(palette[best]);
						while(best>6){best-=6;}
						paintIcon(best, x,y,2*iconSize, true, g);
					}
					
					else if (labelType==4){
						int best=dpc.ent.overallAssignments[clusterN-2][i];
						g.setColor(palette[best]);
						while(best>6){best-=6;}
						paintIcon(best, x,y,2*iconSize, true, g);
					}	
					else if (labelType==5){
						int best=dpc.snn.DBSCANclusters[i];
						g.setColor(palette[best]);
						while(best>6){best-=6;}
						paintIcon(best, x,y,2*iconSize, true, g);
					}	
				}
			}
		}
		g.dispose();
		
	}
	
	public void mouseClicked(MouseEvent e) { 
		if (enabled){
			selectedPoint=true;
			selectedArea=false;
			selectionStarted=false;
			int x=e.getX();
			int y=e.getY();
			double min=100000000;
			int loc=0;
			for (int i=0; i<location.length; i++){
				double score=Math.sqrt((x-location[i][0])*(x-location[i][0])+(y-location[i][1])*(y-location[i][1]));
				if (score<min){
					min=score;
					loc=i;
				}
			}
			selP=new int[1];
			selP[0]=loc;
			dpc.pointsClicked(selP);
			paintPanel();
			repaint();	
		}
		//System.out.println("Clicked");
	}
	
	public void mouseMoved(MouseEvent e) {
		
	}
	
	
	public void mouseExited(MouseEvent e) { 
		enabled=false;
	}
	
	public void mouseReleased(MouseEvent e) {
		
		//System.out.println("released");
		selectionStarted=false;
		selectedArea=false;
		if (enabled){
			
			endx=e.getX();
			endy=e.getY();
			
			if ((Math.abs(startx-endx)>5)||(Math.abs(starty-endy)>5)){
				
				minx=Math.min(startx, endx);
				maxx=Math.max(startx, endx);
				miny=Math.min(starty, endy);
				maxy=Math.max(starty, endy);
				
				LinkedList points=new LinkedList();
				for (int i=0; i<location.length; i++){
					
					if((location[i][0]>=minx)&&(location[i][0]<=maxx)&&(location[i][1]>=miny)&&(location[i][1]<=maxy)){
						int[] t={i};
						points.add(t);
					}
				}
				if (points.size()>0){
					
					selP=new int[points.size()];
					
					minx=100000;
					maxx=-1000;
					miny=100000;
					maxy=-10000;
					
					
					
					for (int i=0; i<points.size(); i++){
						int[] a=(int[])points.get(i);
						selP[i]=a[0];
						
						if (location[selP[i]][0]<minx){
							minx=(int)Math.round(location[selP[i]][0]);
						}
						if (location[selP[i]][0]>maxx){
							maxx=(int)Math.round(location[selP[i]][0]);
						}
						if (location[selP[i]][1]<miny){
							miny=(int)Math.round(location[selP[i]][1]);
						}
						if (location[selP[i]][1]>maxy){
							maxy=(int)Math.round(location[selP[i]][1]);
						}
					}
					
					minx-=5;
					maxx+=5;
					miny-=5;
					maxy+=5;
					
					
					dpc.pointsClicked(selP);
					selectedArea=true;
				}
			}
		}	
		paintPanel();
		repaint();
	}
	
	public void mouseEntered(MouseEvent e) { 
		enabled=true;
	}
	public void mousePressed(MouseEvent e) { 
		//System.out.println("Pressed");
		
		if (enabled){
			selectionStarted=true;
			startx=e.getX();
			starty=e.getY();

		}	
	}
	public void mouseDragged(MouseEvent e) { 
		//System.out.println("Dragged");
		if (selectionStarted){
			selectedArea=true;
			endx=e.getX();
			endy=e.getY();
			minx=Math.min(startx, endx);
			maxx=Math.max(startx, endx);
			miny=Math.min(starty, endy);
			maxy=Math.max(starty, endy);
			//System.out.println("here");
			repaint();
		}
	}
	
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);  //paint background
		g.drawImage(imf, 0, 0, this);
		//System.out.println("here");
		if(selectedArea==true){
			Graphics2D g2=(Graphics2D) g;
			g2.setComposite(ac);
			g2.setColor(Color.RED);
			g2.fillRect(minx, miny, maxx-minx, maxy-miny);
			g2.dispose();
			
		}
		
		else if (selectedPoint==true){
			Graphics2D g2=(Graphics2D) g;
			int x=(int)Math.round(location[selP[0]][0]);
			int y=(int)Math.round(location[selP[0]][1]);
			g2.setComposite(ac);
			g2.setColor(Color.RED);
			g2.fillRect(x-7, y-7, 15, 15);
			g2.dispose();
			
		}
	}
	
	
	public void saveImage(){
		//SaveImage si=new SaveImage(imf, dpc, defaults);
		
		//si.save();
	}
	
	
	public BufferedImage resizeImage(double ratio){
		
		scaler=(float)ratio;
		
		int w1=(int)Math.round(ratio*(gridSizeDefault+xshift1+legendSpacerDefault+100));
		int h1=(int)Math.round(ratio*(gridSizeDefault+2*yshift1));
		
		imf=new BufferedImage(w1, h1, BufferedImage.TYPE_INT_ARGB);
		
		paintPanel();
		scaler=1.0f;
	
		return imf;
	}
	
	
	
	
}
