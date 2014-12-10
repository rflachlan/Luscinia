package lusc.net.github.ui.spectrogram;
//
//  GuidePanel.java
//  Luscinia
//
//  Created by Robert Lachlan on 30/03/2006.
//  Copyright 2006 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.text.*;

import lusc.net.github.Element;
import lusc.net.github.Song;

public class GuidePanel extends JPanel implements MouseInputListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2673674588900139019L;
	Song song;
	BufferedImage im, imf, imf2;
	SpectrPane sp;
	boolean started=false;
	boolean direction=true;
	Color elementColor=new Color(0, 166, 81);
	Color syllableColor=new Color(255, 0, 0);
	int minx=0;
	int maxx=0;
	double timeStep;
	
	int panelHeight=100;
	int panelHeight2;
	int markIncrement=20;
	int eleStartHeight, sylStartHeight, eleHeight, sylHeight, startSeg, endSeg, xsize;
	
	AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f);
	AlphaComposite ac2 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
	
	boolean lineDrawing=false;
	int linePosition=0;
	
	
	public GuidePanel(int panelHeight){
		this.panelHeight=panelHeight;
		panelHeight2=panelHeight-markIncrement;
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	public void makePanel(BufferedImage im, Song song, SpectrPane sp){
		
		System.out.println("making a guide panel");
		
		this.im=im;				
		this.sp=sp;
		this.song=song;
		timeStep=song.getTimeStep();
		
		xsize=im.getWidth();
		//System.out.println("...its size is: "+xsize);
		
		eleStartHeight=(int)Math.round(panelHeight*0.2);
		eleHeight=panelHeight-eleStartHeight;
		eleHeight=(int)Math.round(panelHeight*0.1);
		sylStartHeight=(int)Math.round(panelHeight*0.05);
		sylHeight=eleHeight;
		
		this.setPreferredSize(new Dimension(xsize, panelHeight));
		
	}
	
	public void draw(){
		lineDrawing=false;
		imf=new BufferedImage(xsize, panelHeight, BufferedImage.TYPE_INT_ARGB);
		imf2=new BufferedImage(xsize, panelHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=imf.createGraphics();
		g.drawImage(im, 0,0, xsize, panelHeight2, this);
		g.setColor(Color.WHITE);
		g.fillRect(0,panelHeight2, xsize, markIncrement);
		int ne=song.getNumElements();
		if (ne>0){
			g.setColor(elementColor);
			g.setComposite(ac);
			for (int i=0; i<ne; i++){
				Element ele=(Element)song.getElement(i);
				int start=(int)Math.round(ele.getBeginTime()*ele.getTimeStep()/timeStep);
				int width=(int)Math.round(ele.getLength()*ele.getTimeStep()/timeStep);
				g.fillRect(start, eleStartHeight, width, eleHeight);
			}
		}
		int ns=song.getNumSyllables();
		if (ns>0){
			g.setColor(syllableColor);
			g.setComposite(ac);
			for (int i=0; i<ns; i++){
				int[] syll=(int[])song.getSyllable(i);
				int start=(int)Math.round(syll[0]/timeStep);
				int width=(int)Math.round((syll[1]-syll[0])/timeStep);
				g.fillRect(start, sylStartHeight, width, sylHeight);
			}
		}
		
		g.setColor(Color.BLACK);
		
		g.drawLine(0, panelHeight2, xsize, panelHeight2);
		
		double xc=0;
		double interval=1000/timeStep;
		int xcount=0;
		int textAd=0;
		
		int[] choices={1,2,5,10,20,50};
		
		int maxX=(int)Math.round(interval*xsize);
		Font font=g.getFont();
		FontRenderContext frc = g.getFontRenderContext();
		
		TextLayout layout = new TextLayout(Integer.toString(maxX), font, frc);
		Rectangle r = layout.getPixelBounds(null, 0, 0);
		
		int chsel=0;
		for (int i=0; i<choices.length; i++){
			if (interval*choices[i]>r.width){
				chsel=i;
				i=choices.length;
			}
		}
		
		
		while (xc<xsize){
			int xpos=(int)Math.round(xc);
			g.drawLine(xpos, panelHeight2, xpos, panelHeight2+3);
			String s=Integer.toString(xcount);
			if (xcount==0){
				textAd=0;
			}
			else{
				textAd=-4;
			}
			g.drawString(s, xpos+textAd, panelHeight-2);
			xc+=interval*choices[chsel];
			xcount+=choices[chsel];;
		}
								
		g.dispose();
		repaint();
	}
	
	public void updateBoundaries(){
	
		startSeg=(int)Math.round(song.getStartTime()*song.getTimeStep()/timeStep);
		endSeg=(int)Math.round(song.getEndTime()*song.getTimeStep()/timeStep);
		if (endSeg>xsize){endSeg=xsize;}
		endSeg-=startSeg;
		draw();
	}
	
	public void drawLine(double position){
		lineDrawing=true;
		linePosition=(int)Math.round(position*xsize);
		repaint();
	}
		
	public void paintComponent(Graphics g) {
        super.paintComponent(g);  //paint background
		g.drawImage(imf, 0, 0, this);
		Graphics2D h=(Graphics2D)g;
		
		if (lineDrawing){
			h.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			h.setColor(Color.BLACK);
			h.drawLine(linePosition, 0, linePosition, panelHeight);
		}
		
		
		h.setColor(Color.RED);
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f);
		h.setComposite(ac);
		h.fillRect(startSeg, 0, endSeg, panelHeight);
		h.dispose();
	}
	
	        //Methods required by the MouseInputListener interface.
	public void mouseClicked(MouseEvent e) { 
		/*
		if (started){
			int newx = e.getX();
			System.out.println("MOUSE CLICKED "+newx+" "+xsize);
			if (newx<0){newx=0;}
			if (newx>xsize){newx=xsize;}
			//newx=(int)Math.round(newx*song.nx/(xsize+0.0));
			newx=(int)Math.round(newx*timeStep/song.timeStep);
			//newx=(int)Math.round(newx*timeStep);
			System.out.println(newx+" "+xsize+" "+timeStep+" "+song.timeStep);
			sp.relocate(newx);
		}
		*/
	}

	public void mouseMoved(MouseEvent e){}

	public void mouseExited(MouseEvent e) {
		started=false;
	}

	public void mouseReleased(MouseEvent e) {
		//System.out.println("MOUSE RELEASED");
		if (minx<1000000){
			//minx=(int)Math.round(minx*song.nx/(xsize+0.0));
			//maxx=(int)Math.round(maxx*song.nx/(xsize+0.0));
			//System.out.println(minx+" "+maxx);
			//System.out.println("MOUSE DRAGGED: "+minx+" "+maxx);
			if (maxx-minx>5){
				minx=(int)Math.round(minx*timeStep/song.getTimeStep());
				maxx=(int)Math.round(maxx*timeStep/song.getTimeStep());
				sp.relocate(minx, maxx);
				//System.out.println("MOUSE DRAGGED: "+minx+" "+maxx);
			}
			else{
				int newx=(int)Math.round((minx+maxx)*0.5*timeStep/song.getTimeStep());
				//System.out.println(newx);
				sp.relocate(newx);
			}
		}
	}
	
	public void mouseEntered(MouseEvent e) {
		started=true;
	}
	public void mousePressed(MouseEvent e) {
		//System.out.println("MOUSE PRESSED");
		int newx = e.getX();
		if (newx<0){newx=0;}
		minx=newx;
		maxx=newx;
	}
	public void mouseDragged(MouseEvent e) {
		//System.out.println("MOUSE DRAGGED");
		int newx = e.getX();
		if (newx<0){newx=0;}
		if (newx>xsize){newx=xsize;}
		
		if (newx<minx){
			minx=newx;
			direction=false;
		}
		else if(newx>maxx){
			maxx=newx;
			direction=true;
		}
		else if (direction){
			maxx=newx;
		}
		else{
			minx=newx;
		}
		
		startSeg=minx;
		endSeg=maxx-minx;
		repaint();
	}
	
	public void clearUp(){
		im=null;
		imf=null;
	}
}
