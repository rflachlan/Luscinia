package lusc.net.github.ui.statistics;
//
//  DisplaySimilarity.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/10/2005.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;

import lusc.net.github.Defaults;
import lusc.net.github.Song;
//import lusc.net.github.analysis.SongGroup;
import lusc.net.github.analysis.AnalysisGroup;
import lusc.net.github.db.DataBaseController;
import lusc.net.github.ui.SaveDocument;
import lusc.net.github.ui.SaveImage;
import lusc.net.github.ui.SpectrogramSideBar;

public class DisplaySimilarity extends DisplayPane implements MouseInputListener{
	
	Defaults defaults;
	BufferedImage imf;
	//SongGroup sg;
	AnalysisGroup sg;
	float[][]scores;
	double score=-1;
	double unit=1;
	boolean enabled=false;
	DataBaseController dbc;
	SpectrogramSideBar ssb;
	JPanel mainPanel=new JPanel(new BorderLayout());
	int height, width, dataType;
	
	//public DisplaySimilarity (float[][]scores, int dataType, SongGroup sg, DataBaseController dbc, int width, int height, Defaults defaults){
	public DisplaySimilarity (float[][]scores, int dataType, AnalysisGroup sg, DataBaseController dbc, int width, int height, Defaults defaults){
		this.scores=scores;
		this.sg=sg;
		ssb=sg.getSSB();
		this.dbc=dbc;
		this.height=height;
		this.width=width;
		System.out.println("Displaying matrix "+width+" "+height);
		this.dataType=dataType;
		this.defaults=defaults;
		this.addMouseListener(this);

		
		paintPanel(width, height);
		repaint();
	}
	
	
	public void paintPanel(int width, int height){
		imf=new BufferedImage(width+200, height, BufferedImage.TYPE_INT_ARGB);
		this.setPreferredSize(new Dimension(width+200, height));
		int le1=scores.length;
		int le2=scores[0].length;
		double greyscale=1;
		double maxscore=0;
		double minscore=100000000;
		for (int i=0; i<le1; i++){
			le2=scores[i].length;
			for (int j=0; j<le2; j++){
				if (scores[i][j]>maxscore){maxscore=scores[i][j];}
				if (scores[i][j]<minscore){minscore=scores[i][j];}
			}
		}
		maxscore-=minscore;
		Graphics2D g=imf.createGraphics();
		if (height>width){
			unit=(width-5)/(1.0+le1);
		}
		else{
			unit=(height-5)/(1.0+le1);
		}
		int unit2=(int)Math.ceil(unit);
		//System.out.println("UNITS: "+height+" "+width+" "+le1+" "+unit);
		if (unit2==0){unit2=1;}
		int x=25;
		int y=0;
		Color c;
		float co;
		double t;
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, imf.getWidth(), imf.getHeight());
		
		for (int i=0; i<le1; i++){
			x=(int)(5+i*unit);
			le2=scores[i].length;
			for (int j=0; j<le2; j++){
				y=(int)(unit*le1+5-unit*j);
				t=(scores[i][j]-minscore)/maxscore;
				if (t<0){t=0;}
				co=(float)Math.pow(t, greyscale);
				c=new Color(co, co, co);
				g.setColor(c);
				g.fillRect(x, y, unit2, unit2);
			}
			
		}
		System.out.println("SCORE: "+score);
		if (score>-1){
			g.setColor(Color.BLACK);
			String ss=Double.toString(score);
			g.drawString(ss, (int)Math.round(25+le1*unit), 100);
		}
		g.dispose();
	}
		
	public void mouseClicked(MouseEvent e) { 
		if (enabled){
			int x=e.getX();
			int y=e.getY();
			
			int ychoice=(int)Math.ceil((unit*scores.length+5-y)/unit);
			int xchoice=(int)Math.floor((x-5)/unit);
			if (ychoice<0){ychoice=0;}
			if (xchoice<0){xchoice=0;}
			int []wrap={xchoice, ychoice};
			ssb.draw(dataType, wrap);
			score=scores[xchoice][ychoice];
			System.out.println("SCORE: "+score);
			paintPanel(width, height);
			repaint();	
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

		
	public void paintComponent(Graphics g) {
		super.paintComponent(g);  //paint background
		g.drawImage(imf, 0, 0, this);
	}
	
	public void saveImage(){
		System.out.println("SAVING IMAGE");
		SaveImage si=new SaveImage(imf, this, defaults);
		//si.save();
	}
	
public BufferedImage resizeImage(double ratio){
		
		int archiveWidth=width;
		int archiveHeight=height;
		
		width=(int)Math.round(width*ratio);
		height=(int)Math.round(height*ratio);
		
		paintPanel(width, height);
		
		BufferedImage imt=new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g=imt.createGraphics();
		g.drawImage(imf, 0, 0, this);
		g.dispose();
		
		width=archiveWidth;
		height=archiveHeight;
		paintPanel(width, height);
		return imt;
	}
	
	public void export(){
		SaveDocument sd=new SaveDocument(this, defaults);
		boolean readyToWrite=sd.makeFile();
		
		if (readyToWrite){
			sd.writeString("IndividualName1");
			sd.writeString("IndividualName2");
			sd.writeString("Sound1");
			sd.writeString("Sound2");
			sd.writeString("DistanceScore");
			//sd.writeString("GeographicalDistance");
			sd.writeLine();
			
			String[] inds=new String[scores.length];
			String[] sounds=new String[scores.length];
			
			Song[] songs=sg.getSongs();
			
			for (int i=0; i<scores.length; i++){
			
				int[] g=sg.getId(dataType, i);
			
				//String sx1=null;
				//String sy1=null;
				String sn1=null;
				
				if (g.length>0){
					//sx1=(String)sg.songs[g[0]].locationX;
					//sy1=(String)sg.songs[g[0]].locationY;
					sn1=(String)songs[g[0]].getIndividualName();
				}
				
				inds[i]=sn1;
				if (dataType<3){
					sounds[i]=songs[g[0]].getName()+","+(g[1]+1);
				}
				else{
					sounds[i]=songs[g[0]].getName();
				}
			}
			
				
				//int x1=0;
				//int y1=0;
				//boolean distanceCalculatedX=false;
				//if ((sx1!=null)&&(sy1!=null)){
					//distanceCalculatedX=true;
					//try{
						//Integer xx1=new Integer(sx1);
						//x1=xx1.intValue();
					//}
					//catch(Exception e){}
						//try{
						//Integer yy1=new Integer(sy1);
						//y1=yy1.intValue();
					//}
					//catch(Exception e){}
				//}
			
			for (int i=0; i<scores.length; i++){
				for (int j=i; j<scores.length; j++){
					
					sd.writeString(inds[i]);
					sd.writeString(inds[j]);
					sd.writeString(sounds[i]);
					sd.writeString(sounds[j]);
					sd.writeDouble(scores[j][i]);
					//if ((distanceCalculatedX) && (distanceCalculatedY)){sd.writeDouble(dist);}
					sd.writeLine();
				}	
			}
			sd.finishWriting();
			
		}
			
	}
}
