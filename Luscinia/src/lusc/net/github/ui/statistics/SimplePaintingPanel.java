package lusc.net.github.ui.statistics;
//
//  SimplePaintingPanel.java
//  Luscinia
//
//  Created by Robert Lachlan on 8/20/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

public class SimplePaintingPanel extends JPanel{
	
	BufferedImage imf, imf2;
	int numIms=1;
	int x=0;
	int y=0;
	JPanel imagePanel=new JPanel();
	double scale=1.0;
	
	public void paintImage(BufferedImage im, double scale){
		this.imf=im;
		this.scale=scale;
		x=-1;
		y=-1;
		
		int xpref=(int)Math.round(im.getWidth()/scale);
		int ypref=(int)Math.round(im.getHeight()/scale);
		
		this.setPreferredSize(new Dimension(xpref, ypref));
		System.out.println("IMAGE SIZE: "+imf.getWidth()+" "+imf.getHeight());
		repaint();
		imagePanel.add(this);
	}
	
	public void paintImageDot(BufferedImage im, int xx, int yy, double scale){
		this.imf=im;
		this.x=xx;
		this.y=yy;
		this.scale=scale;
		int xpref=(int)Math.round(im.getWidth()/scale);
		int ypref=(int)Math.round(im.getHeight()/scale);
		
		this.setPreferredSize(new Dimension(xpref, ypref));
		System.out.println("IMAGE SIZE: "+imf.getWidth()+" "+imf.getHeight());
		repaint();
		imagePanel.add(this);
	}
	
	public void paint2Images(BufferedImage im1, BufferedImage im2){
		this.imf=im1;
		this.imf2=im2;
		x=-1;
		y=-1;
		numIms=2;
		this.setPreferredSize(new Dimension(imf.getWidth()+imf2.getWidth(), imf.getHeight()));
		System.out.println("IMAGE SIZE: "+imf.getWidth()+" "+imf.getHeight()+" "+imf2.getWidth()+" "+imf2.getHeight());
		repaint();
		imagePanel.add(this);
	}
		
		
	public void paintComponent(Graphics g) {
		
		int upperLeftX = (int)Math.round(g.getClipBounds().x*scale);
        int upperLeftY = (int)Math.round(g.getClipBounds().y*scale);
        int visibleWidth = (int)Math.round(g.getClipBounds().width*scale);
        int visibleHeight = (int)Math.round(g.getClipBounds().height*scale);
        
        System.out.println(upperLeftX+" "+upperLeftY+" "+visibleWidth+" "+visibleHeight+" "+imf.getWidth()+" "+imf.getHeight());
        
        if (upperLeftX+visibleWidth>imf.getWidth()){visibleWidth=imf.getWidth()-upperLeftX;}
        if (upperLeftY+visibleHeight>imf.getHeight()){visibleHeight=imf.getHeight()-upperLeftY;}
       
        System.out.println(upperLeftX+" "+upperLeftY+" "+visibleWidth+" "+visibleHeight+" "+imf.getWidth()+" "+imf.getHeight());
 
        BufferedImage q=imf.getSubimage(upperLeftX,  upperLeftY,  visibleWidth,  visibleHeight);
        
		
		
		super.paintComponent(g);  //paint background
		Graphics2D g2=(Graphics2D) g;
		g2.scale(1/scale, 1/scale);
		g2.drawImage(q, upperLeftX, upperLeftY, this);
		
		if (numIms==2){
			g2.drawImage(imf2, imf.getWidth(), 0, this);
		}
		
		//System.out.println("image painted");
		if ((x>=0)&&(y>=0)){
			int x2=(int)Math.round((x-3)*scale);
			int y2=(int)Math.round((y-3)*scale);
			int diam=(int)Math.round(7*scale);
			g2.setColor(Color.RED);
			g2.fillArc(x2, y2, diam, diam, 0, 360);
		}
	}
}
