package lusc.net.github.ui.statistics;
//
//  DisplayGeographicComparison.java
//  Luscinia
//
//  Created by Robert Lachlan on 9/15/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

import javax.swing.*;

import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import lusc.net.github.Defaults;
import lusc.net.github.analysis.ComparisonResults;
import lusc.net.github.analysis.GeographicComparison;
import lusc.net.github.analysis.dendrograms.UPGMA;
import lusc.net.github.analysis.AnalysisGroup;
//import lusc.net.github.analysis.SongGroup;
import lusc.net.github.ui.SaveDocument;
import lusc.net.github.ui.SaveImage;


public class DisplayDevelopment extends DisplayPane implements PropertyChangeListener{
	
	Defaults defaults;
	int width, height;
	BufferedImage imf;
	UPGMA upgma;
	ComparisonResults cr;
	
	int numCats=0;
	double[][] props;
	int[] daya;
	int xst,yst,gw,gh;
	int numCols=1000;

	JPanel mainPanel=new JPanel(new BorderLayout());
	SimplePaintingPanel spp=new SimplePaintingPanel();

	double scale=1;
	int fontSize=12;
	
	Color[] colorPalette={Color.BLUE, Color.RED, Color.YELLOW.darker(), Color.BLACK, Color.GREEN, Color.CYAN.darker(), Color.orange, Color.MAGENTA, Color.PINK, Color.GRAY};

	
	JFormattedTextField numCategories;
	NumberFormat num, num2;
	
	public DisplayDevelopment(DisplayUPGMA dup, ComparisonResults cr, int width, int height, Defaults defaults){
		this.upgma=dup.getUPGMA();
		this.cr=cr;
		this.width=width;
		this.height=height;
		this.setPreferredSize(new Dimension(width, height));
		this.defaults=defaults;
		this.scale=defaults.getScaleFactor();
		
		
		num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(0);
		num2=NumberFormat.getNumberInstance();
		num2.setMaximumFractionDigits(3);
		
		
		xst=60;
		yst=50;
		gw=width-100;
		gh=300;
		numCols=gw;
		
		
		numCats=upgma.getLength()/2;
		
		numCategories=new JFormattedTextField(num);
		numCategories.setColumns(6);
		numCategories.setValue(new Integer(numCats));
		numCategories.addPropertyChangeListener("value", this);
		
		JPanel zoomPanel=new JPanel(new BorderLayout());
		
		JPanel z2P=new JPanel(new BorderLayout());
		JLabel z2L=new JLabel("Number of categories");
		z2P.add(z2L, BorderLayout.WEST);
		z2P.add(numCategories, BorderLayout.CENTER);
		zoomPanel.add(z2P, BorderLayout.SOUTH);
		
		calculateValues();
		drawGraph(scale);
		
		this.add(zoomPanel, BorderLayout.NORTH);
		this.add(spp.imagePanel, BorderLayout.CENTER);
				
	}
	
	public DisplayDevelopment(ComparisonResults cr, int width, int height, Defaults defaults){
		this.cr=cr;
		this.width=width;
		this.height=height;
		this.setPreferredSize(new Dimension(width, height));
		this.defaults=defaults;
		this.scale=defaults.getScaleFactor();
		
		
		num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(0);
		num2=NumberFormat.getNumberInstance();
		num2.setMaximumFractionDigits(3);
		
		
		calculateValues2();
		
	}
	
	
	public float[] getColorScore(float p){
		//float[] z=getRobPalette(p);
		//float[] z=getHSBPalette(p, 0.5f, 1.0f, 0.85f, 1f);
		//Color[] cols={new Color(255,237,160), new Color(254,178,76), new Color(240,59,32)};
		//Color[] cols={new Color(165,0,38), new Color(215,48,39), new Color(244,109,67), new Color(253,174,97), new Color(254,224,144), new Color(245,245,191), new Color(224,233,248), new Color(171,217,233), new Color(116,173,209), new Color(69,117,180), new Color(49,54,149)};
		//Color[] cols={new Color(236,226,240), new Color(208,209,230), new Color(166,189,219), new Color(103,169,207), new Color(54,144,192), new Color(2,129,138), new Color(1,108,89), new Color(1,70,54)};
		//Color[] cols={Color.GREEN, Color.YELLOW, Color.RED};
		Color[] cols={new Color(77,77,255), new Color(235, 235, 100), new Color(239,21,21)};
		
		float[] z=getLinearPalette(p, cols);
		
		return z;
	}
	
	public float[] getLinearPalette(float p, Color[] c){
		
		int n=c.length;
		
		int x=(int)Math.floor(p*(n-1));
		if (x==n-1){x=n-2;}
		
		float y=p*(n-1)-x;
		//System.out.println(n+" "+p+" "+x+" "+y);
		Color a=c[x];
		Color b=c[x+1];
		
		float[] ra=a.getColorComponents(null);
		float[] rb=b.getColorComponents(null);
		
		for (int i=0; i<ra.length; i++){
			ra[i]=rb[i]*y+ra[i]*(1-y);
		}
		return ra;
		
	}
	
	public void calculateValues2() {
		
	}
	
	public void calculateValues(){
		int[][] cats=upgma.calculateClassificationMembers(numCats+1);
		
		int[] categories=new int[cats.length];
		int maxCat=0;
		for (int i=0; i<cats.length; i++){
			categories[i]=cats[i][numCats];
			if (categories[i]>maxCat) {maxCat=categories[i];}
		}
		maxCat++;
		
		int[][] lookUps=cr.getLookUp();
		
		long[] times=cr.getTimes();
		
		long minTime=Long.MAX_VALUE;
		long maxTime=Long.MIN_VALUE;
		for (int i=0; i<times.length; i++) {
			if (times[i]>maxTime) {maxTime=times[i];}
			if (times[i]<minTime) {minTime=times[i];}
		}
		
		double day=1000*60*60*24;
		
		int numDays=(int)Math.ceil((maxTime-minTime)/day);
		
		System.out.println("DEV1: "+numDays+" "+minTime+ " "+maxTime);
		System.out.println("DEV2: "+maxCat+" "+numCats);
		
		double[][] props2=new double[numCats+2][numDays];
		
		
		boolean[] daysocc=new boolean[numDays];
		
		for (int i=0; i<categories.length; i++) {
			int dayCat=(int)Math.floor((times[i]-minTime)/day);
			System.out.println(categories[i]+" "+dayCat);
			props2[categories[i]][dayCat]++;
			daysocc[dayCat]=true;
		}
		
		
		int daysx=0;
		for (int i=0; i<numDays; i++) {
			double sum=0;
			for (int j=0; j<props2.length; j++) {
				sum+=props2[j][i];
			}
			if (sum>0) {
				for (int j=0; j<props2.length; j++) {
					props2[j][i]/=sum;
				}
				daysx++;
			}
		}	
		
		props=new double[props2.length][daysx];
		daya=new int[daysx];
		daysx=0;
		for (int i=0; i<numDays; i++) {
			double sum=0;
			for (int j=0; j<props2.length; j++) {
				sum+=props2[j][i];
			}
			if (sum>0) {
				for (int j=0; j<props2.length; j++) {
					props[j][daysx]=props2[j][i];
					System.out.print(props[j][daysx]+" ");
				}
				daya[daysx]=i;
				System.out.println();
				daysx++;
				
			}
		}	
		
		
	}
	
	public void drawGraph(double sc){
		
		int w=(int)(width*sc);
		int h=(int)(height*sc);
		
		imf=new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		
		float maxprop=0.25f;
		
		Graphics2D g=imf.createGraphics();
		
		int xsp=(int)Math.floor(w/(props.length+0.0));
		int ysp=(int)Math.floor(h/(props[0].length+0.0));
		
		for (int i=0; i<props.length; i++) {
			for (int j=0; j<props[i].length; j++) {
				float cs=(float)props[i][j]/maxprop;
				if (cs>=1) {cs=1f;}
				
				float[] c=getColorScore(cs);
				g.setColor(new Color(c[0], c[1], c[2]));
				if (cs<=0.000001f) {g.setColor(Color.WHITE);}
				int x1=i*xsp;
				//int x2=(i+1)*xsp;
				//int y1=height-(i+1)*ysp;
				int y2=height-j*ysp;
				//System.out.println(x1+" "+y2+" "+cs+" "+props[i][j]);
				g.fillRect(x1, y2, xsp, ysp);
			}
		}
			
		spp.paintImage(imf, scale);
	}
	
	
	public void propertyChange(PropertyChangeEvent e) {

       numCats = (int)((Number)numCategories.getValue()).intValue();
       calculateValues();
       drawGraph(scale);
       spp.revalidate();
        
	}
	
	public void saveImage(){
		new SaveImage(imf, this, defaults);
		//si.save();
	}
	
	public BufferedImage resizeImage(double ratio){
		drawGraph(ratio);
		BufferedImage imf1=imf;
		return imf1;
	}	
	
	public void export(){
		SaveDocument sd=new SaveDocument(this, defaults);
		boolean readyToWrite=sd.makeFile();
		if (readyToWrite){
			
			sd.writeString("Day");
			sd.writeString("Types...");
			sd.writeLine();
			for (int i=0; i<props[0].length; i++) {
				sd.writeInt(daya[i]);
				for (int j=0; j<props.length; j++) {
					sd.writeDouble(props[j][i]);
				}
				sd.writeLine();
			}
			sd.finishWriting();
		}
		
	}
}
