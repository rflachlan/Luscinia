package lusc.net.github.dump;
//
//  DisplayKMedoids.java
//  Luscinia
//
//  Created by Robert Lachlan on 9/5/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;

import lusc.net.github.Defaults;
import lusc.net.github.Element;
import lusc.net.github.Song;
import lusc.net.github.analysis.SongGroup;
import lusc.net.github.ui.SaveDocument;
import lusc.net.github.ui.statistics.DisplayPane;
import lusc.net.github.ui.statistics.DisplaySketches;
import lusc.net.github.ui.statistics.DrawSilhouetteGraph;
import lusc.net.github.ui.statistics.SimplePaintingPanel;

/*
public class DisplayKMedoids extends DisplayPane implements ChangeListener{

	Defaults defaults;
	int sketchHeight=50;
	int yBuffer=20;
	
	
	int ysize=600;
	int ydisp=10;
	int xdisp=510;
	int xdisp2=500;
	double maxdist=100;
	double maxY=0;
	
	int size=0;
	int size1=0;	
	int amt=0;
	int elespace2=10;
	int ypl2=50;
	int maxClusters=0;

	JSlider zoom;
	JLabel status;
	JScrollPane scrollPane;
	SimplePaintingPanel spg=new SimplePaintingPanel();
	BufferedImage sketchImage=null;
	JPanel middlePane=new JPanel(new BorderLayout());
	int width, ygap, elements, height;
	protected Font bodyFont;
	
	BufferedImage imf;
	Color[] colors=null;
	SongGroup sg;
	int dataType=0; 
	
	double silhouettes[]=null;
	double avsils[][]=null;
	int[][] assignments=null;
	
	DrawSilhouetteGraph dsg=new DrawSilhouetteGraph();
	DisplaySketches dsk;
	Random random=new Random(System.currentTimeMillis());
	
	int cutoff=2;
	Song[] songs;
	
	//THIS IS CURRENTLY NOT BEING USED. REMOVE?
	
	public DisplayKMedoids (int[][] assignments, double[][] avsils, SongGroup sg, int dataType, int width, int height, Defaults defaults){
		this.sg=sg;
		this.dataType=dataType;
		this.width=width;
		this.height=height;
		this.assignments=assignments;
		this.avsils=avsils;
		this.defaults=defaults;
		maxClusters=avsils[0].length;
		
		songs=sg.getSongs();
		
		
		
		dsk=new DisplaySketches(sg);
		startDisplaying();
	}	
	
	public void startDisplaying(){

		bodyFont  = new Font("Arial", Font.PLAIN, 10);
        if (bodyFont == null) {
            bodyFont = new Font("SansSerif", Font.PLAIN, 10);
        }
		
		xdisp2=width-250;
		xdisp=xdisp2+10;
		zoom=new JSlider(JSlider.HORIZONTAL, 2, maxClusters+1, 2);
		zoom.setMajorTickSpacing(1);
		zoom.setPaintTicks(true);
		zoom.setPaintLabels(false);
		zoom.setSnapToTicks(true);
		zoom.setBorder(BorderFactory.createEmptyBorder(10,20,10, 10));
		zoom.addChangeListener(this);
		
		
		status=new JLabel("Number of clusters: 2");
		status.setBorder(BorderFactory.createEmptyBorder(10,10,10,20));
		
		LinkedList t=getExamples();
		updateSketch(t);
		paintDSG();
		
		
		scrollPane=new JScrollPane(spg.imagePanel);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setPreferredSize(new Dimension(width, height-500));
		JPanel topPane=new JPanel(new BorderLayout());
		topPane.add(zoom, BorderLayout.NORTH);
		topPane.add(status, BorderLayout.EAST);
		topPane.add(dsg, BorderLayout.CENTER);
		middlePane.add(scrollPane, BorderLayout.CENTER);
		this.add(topPane, BorderLayout.NORTH);
		this.add(middlePane, BorderLayout.CENTER);
		this.setPreferredSize(new Dimension(width-500, height-300));
	}
		
	public void constructPanel(){
			
		spg.paintImage(sketchImage);
		spg.revalidate();
		
		//middlePane.remove(scrollPane);
		//scrollPane=new JScrollPane(spg.imagePanel);
		//middlePane.add(scrollPane, BorderLayout.CENTER);
		//middlePane.revalidate();
		this.revalidate();
	}
		
	public LinkedList getExamples(){
	
		int numExamples=5;
		LinkedList p=new LinkedList();
		for (int i=0; i<=cutoff; i++){
		
			LinkedList c=new LinkedList();
			int counter=0;
			for (int j=0; j<assignments[0].length; j++){
				if (assignments[cutoff-2][j]==i){
					int[] t={j};
					c.add(t);
					counter++;
				}
			}
			int ne=numExamples;
			if (counter<ne){
				ne=counter;
			}
			int[] d=new int[ne];
			for (int j=0; j<ne; j++){
				int q=random.nextInt(c.size());
				int[] t=(int[])c.get(q);
				c.remove(q);
				d[j]=t[0];
				//System.out.print(d[j]+" ");
			}
			//System.out.println();
			p.add(d);
		}
		return p;
	}
	
	public void updateSketch(LinkedList examples){
		
		int width2=width/2;
	
		BufferedImage[][] rowIm=new BufferedImage[examples.size()][];
		double maxBuWidth=0;
		for (int i=0; i<examples.size(); i++){
			int[] eg=(int[])examples.get(i);
			rowIm[i]=new BufferedImage[eg.length];
			int buWidth=0;
			for (int j=0; j<eg.length; j++){
				//System.out.print(eg[j]+" ");
				rowIm[i][j]=getSketch(sketchHeight+1, eg[j]);
				buWidth+=rowIm[i][j].getWidth();
			}
			//System.out.println();
			if (buWidth>maxBuWidth){
				maxBuWidth=buWidth;
			}
		}
		int height2=examples.size()*sketchHeight+2*yBuffer;
		sketchImage=new BufferedImage(width2+2, height2, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=sketchImage.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0,0,width2,height2);
		if (maxBuWidth<width2){
			maxBuWidth=width2;
		}
		g.setColor(Color.BLACK);
		Element ele=(Element)songs[0].getElement(0);
		int mf=ele.getMaxF();
		double ts=ele.getTimeStep();
		
		double interval=25.0/ts;
		
				
		
		for (int i=0; i<examples.size(); i++){
			int yp=yBuffer+(i*sketchHeight);
			
			
			int yticks=(int)Math.floor(mf/1999.0);
		
			int ytickslocs[]=new int[yticks];
		
			for (int yi=0; yi<yticks; yi++){
				int ii=(yi+1)*2000;
				if (dsk.fundLogPlot){
					ytickslocs[yi]=(int)Math.round(sketchHeight*Math.log(ii)/Math.log(mf));
				}
				else{
					ytickslocs[yi]=(int)Math.round(sketchHeight*ii/(mf+0.0));
				}
				ytickslocs[yi]=yp+sketchHeight-ytickslocs[yi];
			}

			

			int xc=0;
			for (int j=0; j<rowIm[i].length; j++){
				xc+=rowIm[i][j].getWidth();
			}
			
			double xcor=width2/(maxBuWidth+0.0);
			int xloc=0;
			for (int j=0; j<rowIm[i].length; j++){
				int xp=(int)Math.round(rowIm[i][j].getWidth()*xcor);
				g.drawImage(rowIm[i][j], xloc, yp, xloc+xp, yp+sketchHeight+1, 0, 0, rowIm[i][j].getWidth(), rowIm[i][j].getHeight(), null);
				g.drawRect(xloc, yp, xp, sketchHeight);
				int k=0;
				while (k<xp){
					g.drawLine(xloc+k, yp+sketchHeight, xloc+k, yp+sketchHeight-2);
					k+=(int)Math.round(interval*xp/(rowIm[i][j].getWidth()+0.0));
				}
				for (int yt=0; yt<yticks; yt++){
					g.drawLine(xloc, ytickslocs[yt], xloc+2, ytickslocs[yt]);
				}
				xloc+=xp;
				rowIm[i][j]=null;
			}
			
		}
		rowIm=null;
		g.dispose();	
	}
	
	
	
	public BufferedImage getSketch(int height, int unit){
BufferedImage bi=null;
		
		if (dataType<3){
			int[][] lookUp=sg.getLookUp(dataType);
			int so=lookUp[unit][0];
			int el=lookUp[unit][1];
			Element ele=(Element)songs[so].getElement(el);
			if (dataType<2){
				bi=dsk.drawElement(ele, height, true);
			}
			else if (dataType==2){
				bi=dsk.drawPhraseEx(so, el, height, false);
			}
		}
					
		else if (dataType==3){
			bi=dsk.drawTransition(unit, height, true);
		}
		else if (dataType==4){
			bi=dsk.drawSong(unit, height, true);
		}
		else if (dataType==5){
			bi=dsk.drawTransition2(unit, height);
		}
		return bi;
	}
	
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);  //paint background
		g.drawImage(imf, 0, 0, this);
	}
	
	public void paintDSG(){
				
		double[][] ypoints=new double[avsils.length][cutoff+1];
		int[] xpoints=new int[cutoff+1];
		
			
		for (int i=0; i<=cutoff; i++){
			xpoints[i]=(int)Math.round(i*300/(cutoff+0.0));
			for (int k=0; k<avsils.length; k++){
				ypoints[k][i]=avsils[k][i];
			}
		}
		//dsg.paintGraphUPGMA(ypoints, xpoints, 1, 100, 400, 90, 300);	
	}

	
		
	public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
			cutoff=(int)source.getValue();
			//cutoff+=2;
			System.out.println(cutoff);
			LinkedList t=getExamples();
			updateSketch(t);
			paintDSG();
			constructPanel();
			System.out.println(cutoff);
        }
    }
	
	public void export(){
		SaveDocument sd=new SaveDocument(this, defaults);
		boolean readyToWrite=sd.makeFile();
		if (readyToWrite){
			
			sd.writeString("N");
			sd.writeString("MK Entropy");
			sd.writeString("Entropy Lower");
			sd.writeString("Entropy Upper");
			sd.writeString("AB Entropy");
			sd.writeString("Entropy Lower");
			sd.writeString("Entropy Upper");
			sd.writeLine();
			
			for (int i=0; i<avsils[0].length; i++){
				
				sd.writeInt(i+2);
				sd.writeDouble(avsils[0][i]);
				sd.writeDouble(avsils[1][i]);
				sd.writeDouble(avsils[2][i]);
				sd.writeDouble(avsils[3][i]);
				sd.writeDouble(avsils[4][i]);
				sd.writeDouble(avsils[5][i]);
				sd.writeLine();
			}
			sd.finishWriting();
		}
	}
	
	

}

*/