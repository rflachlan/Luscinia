package lusc.net.sourceforge;
//
//  DendrogramPanel.java
//  Luscinia
//
//  Created by Robert Lachlan on 10/3/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.text.*;


public class DendrogramPanel extends DisplayPane implements ChangeListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6895812043248447269L;
	Defaults defaults;
	int elespace=50;
	int clickedX=-1;
	int clickedY=-1;
	int clickedLoc=-1;
	double cutoff=0;
	int maxExamples=5;
	boolean decorateNodes=false;
	JLabel status;
	JSlider zoom;
	JScrollPane scrollPane;
	JPanel mainPanel=new JPanel(new BorderLayout());
	BufferedImage sketchImage=null;
	int width, height;
	protected Font bodyFont;
	TreeDat[] dat;
	Dendrogram den;
	DisplayDendrogram dden;
	DisplaySketches dsk=null;
	BufferedImage imf;
	int dataType=0; 
	SongGroup sg;
	DrawSilhouetteGraph dsg=new DrawSilhouetteGraph();
	SimplePaintingPanel spg=new SimplePaintingPanel();
	double silhouettes[]=null;
	double avsils[][]=null;
	double[] nodeDetails=null;
	
	public DendrogramPanel (Dendrogram den, SongGroup sg, int dataType, int width, int height, Defaults defaults){
		this.den=den;
		this.sg=sg;
		this.dataType=dataType;
		this.width=width;
		this.height=height;
		this.defaults=defaults;
		decorateNodes=false;
		dat=den.dat;	
		buildUI();
	}
	
	public DendrogramPanel (Dendrogram den, SongGroup sg, int dataType, int width, int height, double[] nodeDetails, Defaults defaults){
		this.den=den;
		this.sg=sg;
		this.dataType=dataType;
		this.width=width;
		this.height=height;

		this.nodeDetails=nodeDetails;
		this.defaults=defaults;
		decorateNodes=true;
		dat=den.dat;	
		buildUI();
	}
	
	void makeImage(){
		int dwidth=width/2;
		String[] name=null;
		if (dataType==0){
			name=sg.eleNamesC;
		}
		else if (dataType==1){
			name=sg.eleNames;
		}
		else if (dataType==2){
			name=sg.syllNames;
		}
		else if (dataType==3){
			name=sg.transNames;
		}
		else{
			name=sg.songNames;
		}
		
		if (decorateNodes){
			dden=new DisplayDendrogram(dat, name, sg, dataType, dwidth, elespace, 2, nodeDetails, defaults);
		}
		else{
			dden=new DisplayDendrogram(dat, name, sg, dataType, dwidth, elespace, 2, defaults);
		}
		for (int i=0; i<sg.songs.length; i++){
			
			for (int j=0; j<sg.songs[i].eleList.size(); j++){
				Element ele=(Element)sg.songs[i].eleList.get(j);
				ele.tb=ele.timeBefore;
				ele.ta=ele.timeAfter;
			}
			
			for (int j=0; j<sg.songs[i].phrases.size(); j++){
				int[][] ph=(int[][])sg.songs[i].phrases.get(j);
				
				
				
				for (int a=0; a<ph.length; a++){
					int c=0;
					int b=ph[a][0];
					
					while (b==-1){
						System.out.println("WEIRD SONG: "+sg.songs[i].name+" "+sg.songs[i].individualName);
						c++;
						b=ph[a][c];
					}
					Element ele=(Element)sg.songs[i].eleList.get(b);
					ele.tb=25;
					ele.ta=25;
					c=ph[a].length-1;
					b=ph[a][c];
					while (b==-1){
						c--;
						b=ph[a][c];
					}
					ele=(Element)sg.songs[i].eleList.get(b);
					ele.tb=25;
					ele.ta=25;
				}
			}
		}
	}
	
	void buildUI(){
		//width=width*2;
		//height=height*2;
		System.out.println("WIDTH: "+width);
		dsk=new DisplaySketches(sg);
		
		
		
						
		makeImage();
		
		
		scrollPane=new JScrollPane(null);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		updateDisplay(0);
		this.setLayout(new BorderLayout());
		
		this.add(scrollPane, BorderLayout.CENTER);
		
		zoom=new JSlider(JSlider.HORIZONTAL, 0, 1000, 1000);
		zoom.setPaintTicks(false);
		zoom.setPaintLabels(false);
		zoom.setBorder(BorderFactory.createEmptyBorder(10,20,10, 10));
		zoom.addChangeListener(this);
		
		status=new JLabel("Number of leaves: "+dden.elements+" Cut-off: 0");
		status.setBorder(BorderFactory.createEmptyBorder(10,10,10,20));
		
		JPanel zoomPanel=new JPanel(new BorderLayout());
		zoomPanel.add(zoom, BorderLayout.CENTER);
		zoomPanel.add(status, BorderLayout.EAST);
		this.add(zoomPanel, BorderLayout.NORTH);
		
		
		
	}
	
	
	public void updateDisplay(double cutoff){
		dden.updateDendrogram(cutoff);
		LinkedList<int[]> examples=dden.getExamples(maxExamples);
		updateSketch(examples);
		constructPanel();
	}	
	
	public void updateSketch(LinkedList<int[]> examples){
		
		int width2=width/2;
		System.out.println("PANEL WIDTH: "+width2+" "+width);
		BufferedImage[][] rowIm=new BufferedImage[examples.size()][];
		double maxBuWidth=0;
		for (int i=0; i<examples.size(); i++){
			int[] eg=(int[])examples.get(i);
			rowIm[i]=new BufferedImage[eg.length-2];
			int buWidth=0;
			for (int j=0; j<eg.length-2; j++){
				//System.out.println("ELE WIDTHS: "+eg[1]+" "+eg[j+2]);
				rowIm[i][j]=getSketch(eg[1]+1, eg[j+2]);
				buWidth+=rowIm[i][j].getWidth();
				//System.out.println(rowIm[i][j].getHeight()+" "+rowIm[i][j].getWidth());
			}
			if (buWidth>maxBuWidth){
				maxBuWidth=buWidth;
			}
		}
		int height2=dden.imf.getHeight();
		sketchImage=new BufferedImage((int)maxBuWidth+2, height2, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=sketchImage.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0,0,width2,height2);
		if (maxBuWidth<width2){
			maxBuWidth=width2;
		}
		g.setColor(Color.BLACK);
		
		Element ele=(Element)sg.songs[0].eleList.get(0);
		
		double interval=25.0/ele.timeStep;
		
				
		
		for (int i=0; i<examples.size(); i++){
			int[] eg=(int[])examples.get(i);
			int yp=eg[0]-(eg[1]/2);
			
			
			int yticks=(int)Math.floor(ele.maxf/1999.0);
		
			int ytickslocs[]=new int[yticks];
		
			for (int yi=0; yi<yticks; yi++){
				int ii=(yi+1)*2000;
				if (dsk.fundLogPlot){
					ytickslocs[yi]=(int)Math.round(eg[1]*Math.log(ii)/Math.log(ele.maxf));
				}
				else{
					ytickslocs[yi]=(int)Math.round(eg[1]*ii/(ele.maxf+0.0));
				}
				ytickslocs[yi]=yp+eg[1]-ytickslocs[yi];
			}
			
			double xcor=width2/(maxBuWidth+0.0);
			int xloc=0;
			for (int j=0; j<rowIm[i].length; j++){
				int xp=(int)Math.round(rowIm[i][j].getWidth()*xcor);
				g.drawImage(rowIm[i][j], xloc, yp, xloc+xp, yp+eg[1]+1, 0, 0, rowIm[i][j].getWidth(), rowIm[i][j].getHeight(), null);
				g.drawRect(xloc, yp, xp, eg[1]);
				int k=0;
				while (k<xp){
					g.drawLine(xloc+k, yp+eg[1], xloc+k, yp+eg[1]-2);
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
		
		if (dataType==0){
			int so=sg.lookUpElsC[unit][0];
			int el=sg.lookUpElsC[unit][1];
			Element ele=(Element)sg.songs[so].eleList.get(el);
			bi=dsk.drawElement(ele, height, true);
		}
		else if (dataType==1){
			int so=sg.lookUpEls[unit][0];
			int el=sg.lookUpEls[unit][1];
			Element ele=(Element)sg.songs[so].eleList.get(el);
			bi=dsk.drawElement(ele, height, true);
		}
		else if (dataType==2){
			int so=sg.lookUpSyls[unit][0];
			int ph=sg.lookUpSyls[unit][1];
			bi=dsk.drawPhraseEx(so, ph, height, false);
			//bi=dsk.drawSyllable(so, ph, height, false);
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
	
	public void constructPanel(){
		spg.paint2Images(dden.imf, sketchImage);
		spg.revalidate();
		
		this.remove(scrollPane);
		scrollPane=new JScrollPane(spg);
		this.add(scrollPane);
		this.revalidate();
	}
	
	/*	
	public void paintDSG(){
				
		double[][] ypoints=new double[avsils.length][size1+1];
		int[] xpoints=new int[size1+1];
		int j=avsils[0].length-1;
		for (int i=size1; i>=0; i--){
			xpoints[i]=(int)Math.round(dat[i].xloc);
			for (int k=0; k<avsils.length; k++){
				ypoints[k][i]=avsils[k][j];
			}
			//System.out.println(ypoints[0][i]+" "+xpoints[i]+" "+i);
			j--;
		}
		
		dsg.paintGraphUPGMA(ypoints, xpoints, 1, 100, width, 90, xdisp);	
	}
	*/
	
		
	public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
			cutoff=0.001*source.getValue();
			cutoff=1-cutoff;
			
			updateDisplay(cutoff);
			
			DecimalFormat df=new DecimalFormat("0.###");
			
			status.setText("Number of leaves: "+dden.elements+" Cut-off: "+df.format(cutoff));
			
			
			
        }
    }
	
	public void export(){
	
	}

	public void saveImage(){
	
		int x1=dden.imf.getWidth();
		int y1=dden.imf.getHeight();
		int x2=sketchImage.getWidth();
		
		int x=x1+x2;
		int y=y1;	
		
		BufferedImage imt=new BufferedImage(x, y, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g=imt.createGraphics();
		g.drawImage(dden.imf, 0, 0, this);
		g.drawImage(sketchImage, x1, 0, this);
		g.dispose();
		
		@SuppressWarnings("unused")
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
		
		makeImage();
		updateDisplay(cutoff);
		
		int x1=dden.imf.getWidth();
		int y1=dden.imf.getHeight();
		int x2=sketchImage.getWidth();		
		int x=x1+x2;
		int y=y1;	
		
		BufferedImage imt=new BufferedImage(x, y, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g=imt.createGraphics();
		g.drawImage(dden.imf, 0, 0, this);
		g.drawImage(sketchImage, x1, 0, this);
		g.dispose();
		
		
		width=archiveWidth;
		height=archiveHeight;
		elespace=archiveElespace;
		makeImage();
		updateDisplay(cutoff);
		return imt;
	}
}

