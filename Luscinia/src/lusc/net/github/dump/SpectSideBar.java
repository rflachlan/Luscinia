package lusc.net.github.dump;
//
//  SpectSideBar.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/10/2005.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.


import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;

import lusc.net.github.Element;
import lusc.net.github.Song;

public class SpectSideBar extends JPanel{

	Song[] songs=new Song[1];
	String[] nam=new String[1];
	int[][] scheme=null;
	int maxEleLength=0;
	int maxSyllLength=0;
	int maxSongLength=0;
	int mode=0;
	int imxsize=0; 
	int imysize=0;
	BufferedImage ims=new BufferedImage(300, 700, BufferedImage.TYPE_INT_ARGB);;
	Font bodyFont;
	
	public SpectSideBar(int a){
		this.setPreferredSize(new Dimension(300,700));
		bodyFont  = new Font("Arial", Font.PLAIN, 10);
        if (bodyFont == null) {
            bodyFont = new Font("SansSerif", Font.PLAIN, 10);
        }
	}
	
	public void calculateMaxima(){
	
		maxEleLength=0;
		maxSyllLength=0;
		maxSongLength=0;
		int a;
		for (int i=0; i<songs.length; i++){
			for (int j=0; j<songs[i].getNumElements(); j++){
				Element ele=(Element)songs[i].getElement(j);
				int[][] signal=ele.getSignal();
				a=signal.length;
				if (a>maxEleLength){maxEleLength=a;}
				if (signal[a-1][0]>maxSongLength){maxSongLength=signal[a-1][0];}
			}
			for (int j=0; j<songs[i].getNumSyllables(); j++){
				int[]s=(int[])songs[i].getSyllable(j);
				a=s[1]-s[0];
				if (a>maxSyllLength){maxSyllLength=a;}
			}
		}
	
	}
	
	public BufferedImage drawElements(int[] indata, int ysize){
		if (songs[0]!=null){
			int imnum=indata.length;
			imysize=100;
			ysize=imysize*(imnum+1);
			imxsize=180;
			
			ims=new BufferedImage(300, ysize, BufferedImage.TYPE_INT_ARGB);
			this.setPreferredSize(new Dimension(300, ysize));
			this.revalidate();
			Graphics2D g=ims.createGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0,0,200,ysize);
			g.setColor(Color.BLACK);
			
			float dx=(float)(imxsize/(maxEleLength+0.0));
			if (dx>1){dx=1;}
			int x,y1,y2, ys, x1, x2, j2, j3;
			double ny;
			for (int i=0; i<imnum; i++){
				ys=50+i*imysize;
				g.drawRect(10, 50+i*imysize, imxsize, imysize);
				
				g.setFont(bodyFont);
				int ii=scheme[indata[i]][0];
				if (mode==0){
					Element ele=(Element)songs[ii].getElement(scheme[indata[i]][1]);
					int[][] signal=ele.getSignal();
					double[][] measurements=ele.getMeasurements();
					g.setColor(Color.BLACK);
					
					ny=imysize*ele.getDy()/(ele.getMaxF()+0.0);
					for (int j=0; j<signal.length; j++){
						x=(int)Math.round(10+(j*dx));
						for (int k=1; k<signal[j].length; k+=2){
							y1=(int)Math.round(ny*signal[j][k]);
							y2=(int)Math.round(ny*signal[j][k+1]);
							g.drawLine(x,ys+y1,x,ys+y2);
						}
						
					}
					
					g.setColor(Color.RED);
					
					ny=imysize/(ele.getMaxF()+0.0);
					for (int j=0; j<signal.length-1; j++){
						j2=j+5;
						j3=j+6;
						x1=(int)Math.round(10+(j*dx));
						x2=(int)Math.round(10+((j+1)*dx));
						y1=(int)Math.round(ny*measurements[j2][3]);
						y2=(int)Math.round(ny*measurements[j3][3]);
						System.out.println(x1+" "+x2+" "+y1+" "+y2+" "+ny+" "+ys);
						g.drawLine(x1,ys+y1,x2,ys+y2);
					}
					
				}
				if (mode==1){
					
				}
			}
			repaint();
			return ims;
		}
		return null;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);  //paint background
		g.drawImage(ims, 0, 0, this);
	}
}
