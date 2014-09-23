package lusc.net.github;
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
import java.awt.image.BufferedImage;
import java.awt.image.*;

public class SimplePaintingPanel extends JPanel{
	
	BufferedImage imf, imf2;
	int numIms=1;
	int x=0;
	int y=0;
	JPanel imagePanel=new JPanel();
	
	public void paintImage(BufferedImage im){
		this.imf=im;
		x=-1;
		y=-1;
		this.setPreferredSize(new Dimension(imf.getWidth(), imf.getHeight()));
		System.out.println("IMAGE SIZE: "+imf.getWidth()+" "+imf.getHeight());
		repaint();
		imagePanel.add(this);
	}
	
	public void paintImageDot(BufferedImage im, int xx, int yy){
		this.imf=im;
		this.x=xx;
		this.y=yy;
		this.setPreferredSize(new Dimension(imf.getWidth(), imf.getHeight()));
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
		System.out.println("IMAGE SIZE: "+imf.getWidth()+" "+imf.getHeight());
		repaint();
		imagePanel.add(this);
	}
		
		
	public void paintComponent(Graphics g) {
		
		int upperLeftX = g.getClipBounds().x;
        int upperLeftY = g.getClipBounds().y;
        int visibleWidth = g.getClipBounds().width;
        int visibleHeight = g.getClipBounds().height;
        
        //System.out.println(upperLeftX+" "+upperLeftY+" "+visibleWidth+" "+visibleHeight+" "+imf.getWidth()+" "+imf.getHeight());
        
        if (upperLeftX+visibleWidth>imf.getWidth()){visibleWidth=imf.getWidth()-upperLeftX;}
        if (upperLeftY+visibleHeight>imf.getHeight()){visibleHeight=imf.getHeight()-upperLeftY;}
       
        //System.out.println(upperLeftX+" "+upperLeftY+" "+visibleWidth+" "+visibleHeight+" "+imf.getWidth()+" "+imf.getHeight());
 
        BufferedImage q=imf.getSubimage(upperLeftX,  upperLeftY,  visibleWidth,  visibleHeight);
        
		
		
		super.paintComponent(g);  //paint background
		g.drawImage(q, upperLeftX, upperLeftY, this);
		
		if (numIms==2){
			g.drawImage(imf2, imf.getWidth(), 0, this);
		}
		
		//System.out.println("image painted");
		if ((x>=0)&&(y>=0)){
			g.setColor(Color.RED);
			g.fillArc(x-3, y-3, 7, 7, 0, 360);
		}
	}
}
