package lusc.net.github.ui.statistics;
//
//  DrawSilhouetteGraph.java
//  Luscinia
//
//  Created by Robert Lachlan on 8/17/07.
//  Copyright 2007 Robert Lachlan. All rights reserved.
//

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;

public class DrawSilhouetteGraph extends JPanel implements MouseInputListener{

	BufferedImage imf;
	DisplayUPGMA dupgma;
	boolean active=true;
	int[] xscores;
	Color[] palette={Color.RED, Color.BLUE, Color.GREEN, Color.CYAN, Color.PINK};
	double scale=1.0;

	public DrawSilhouetteGraph (){
		
	}
	
	public void paintGraph(double[] yscores, double[] xscores, double ymax, double xmax, int height, int width, int graphHeight, int graphWidth, double scale){
		this.scale=scale;
		this.setPreferredSize(new Dimension(width, height));
		imf=new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=imf.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0,0, width, height);
		
		double yoff=(height-graphHeight)*0.5;
		double xoff=(width-graphWidth)*0.5;
		
		g.setColor(Color.BLACK);
		g.drawRect((int)xoff, (int)yoff, graphWidth, graphHeight);
	
		int n=yscores.length;
		
		int xpl=(int)Math.round((xscores[0]/xmax)*graphWidth+xoff);
		int ypl=(int)Math.round((yscores[0]/ymax)*graphHeight+yoff);
		int xpl2, ypl2;
		for (int i=1; i<n; i++){
			g.fillArc(xpl-1, ypl-1, 3, 3, 0, 360);
			xpl2=(int)Math.round((xscores[i]/xmax)*graphWidth+xoff);
			ypl2=(int)Math.round((yscores[i]/ymax)*graphHeight+yoff);
			g.drawLine(xpl, ypl, xpl2, ypl2);
			xpl=xpl2;
			ypl=ypl2;
		}
	
		g.dispose();
		repaint();
	}
	
	public void paintGraphUPGMA(double[][] yscores, int[] xscores, double ymax, int height, int width, int graphHeight, int graphWidth, DisplayUPGMA dupgma, double scale){
		
		this.xscores=xscores;
		this.dupgma=dupgma;
		this.scale=scale;
		
		int n=yscores[0].length;
		int m=yscores.length;
		int minx=10000; 
		int maxx=0;
		double[] maxy=new double[m];;
		for (int i=0; i<n; i++){
			if (xscores[i]>maxx){maxx=xscores[i];}
			if (xscores[i]<minx){minx=xscores[i];}
			for (int j=0; j<m; j++){
				if (yscores[j][i]>maxy[j]){maxy[j]=yscores[j][i];}
			}
		}
		this.setPreferredSize(new Dimension(width, height));
		//width=graphWidth+minx+250;
		System.out.println("WIDTH1: "+width);
		width=(int)Math.round(scale*(maxx+200));
		System.out.println("WIDTH2: "+width);
		height=(int)Math.round(scale*height);
		
		
		imf=new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=imf.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0,0, width, height);

		double yoff=(height-graphHeight)*0.5;
		
		minx=(int)Math.round(minx*scale);
		maxx=(int)Math.round(maxx*scale);
		
		g.setColor(Color.BLACK);
		g.drawRect(minx, (int)yoff, maxx-minx, graphHeight);
	
		
		
		int xpl=(int)Math.round(xscores[0]*scale);
		int ypl[]=new int[m];
		for (int i=0; i<m; i++){
			maxy[i]=ymax;
			ypl[i]=(int)Math.round(((maxy[i]-yscores[i][0])/maxy[i])*graphHeight+yoff);
		}
		int xpl2, ypl2;
		int diam=(int)Math.round(3*scale);
		int diam2=(int)Math.round(scale);
		for (int i=1; i<n; i++){
			xpl2=(int)Math.round(xscores[i]*scale);
			for (int j=0; j<m; j++){
				g.setColor(palette[j]);
				g.fillArc(xpl-diam2, ypl[j]-diam2, diam, diam, 0, 360);
				
				//System.out.println("PLOTTED LINES: "+yscores[i]+" "+maxy+" "+xpl2);
				ypl2=(int)Math.round(((maxy[j]-yscores[j][i])/maxy[j])*graphHeight+yoff);
				
				g.fillArc(xpl2-diam2, ypl2-diam2, diam, diam, 0, 360);
				g.drawLine(xpl, ypl[j], xpl2, ypl2);
				ypl[j]=ypl2;
			}
			xpl=xpl2;
		}
		
		g.setColor(Color.WHITE);
		g.fillRect(minx, (int)(yoff+graphHeight+1), maxx-minx, (int)(height-yoff-graphHeight-1));
		g.dispose();
		repaint();	
		this.addMouseListener(this);
		
	}
	
	public void mouseClicked(MouseEvent e) { 
		if (active){
			active=false;
			int x=e.getX();
			System.out.println("X is at: "+x);
			int loc=-1;
			int best=1000000;
			for (int i=0; i<xscores.length; i++){
			
				int p=(int)Math.abs(x-xscores[i]);
				if (p<best){
					best=p;
					loc=i;
				}
			}
			dupgma.drawDottedLine(xscores[loc]);
		}
		active=true;
	}
	
	public void mouseMoved(MouseEvent e) {
	}
	
	public void mouseExited(MouseEvent e) { 
	}
	
	public void mouseReleased(MouseEvent e) {
	}
	
	public void mouseEntered(MouseEvent e) { 
	}
	public void mousePressed(MouseEvent e) { }
	public void mouseDragged(MouseEvent e) { 
		
	}
			
	public void paintComponent(Graphics g) {
		super.paintComponent(g);  //paint background
		Graphics2D g2=(Graphics2D) g;
		g2.scale(1/scale, 1/scale);
		System.out.println(imf.getWidth()+" "+imf.getHeight());
		g2.drawImage(imf, 7, 0, this);
		//g.drawImage(imf, 7, 0, this);
	}
	
}
