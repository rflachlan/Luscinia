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
import lusc.net.github.analysis.GeographicComparison;
import lusc.net.github.analysis.AnalysisGroup;
//import lusc.net.github.analysis.SongGroup;
import lusc.net.github.ui.SaveDocument;


public class DisplayGeographicComparison extends DisplayPane implements PropertyChangeListener{
	
	Defaults defaults;
	int width, height;
	BufferedImage imf;
	GeographicComparison ci;
	double[][] results;
	int xst,yst,gw,gh;
	int numCols=1000;
	int sigPlaces=1;
	double dx=0;
	double max=0;
	double maxDist, minDist;
	double logc=1/Math.log(10);
	//JSlider zoom1;
	JLabel z1Lab, z2Lab;
	//, imLabel;
	JPanel mainPanel=new JPanel(new BorderLayout());
	AnalysisGroup sg;
	SimplePaintingPanel spp=new SimplePaintingPanel();
	//SongGroup sg;
	double scale=1;
	int fontSize=12;
	
	Color[] colorPalette={Color.BLUE, Color.RED, Color.YELLOW.darker(), Color.BLACK, Color.GREEN, Color.CYAN.darker(), Color.orange, Color.MAGENTA, Color.PINK, Color.GRAY};

	
	JFormattedTextField numCategories, thresholdSimilarity, numDistCategories;
	NumberFormat num, num2;
	
	//public DisplayGeographicComparison(GeographicComparison ci, int width, int height, SongGroup sg, Defaults defaults){
	public DisplayGeographicComparison(GeographicComparison ci, int width, int height, AnalysisGroup sg, Defaults defaults){
		this.ci=ci;
		this.width=width;
		this.height=height;
		this.sg=sg;
		this.setPreferredSize(new Dimension(width, height));
		this.defaults=defaults;
		this.scale=defaults.getScaleFactor();
		
		imf=new BufferedImage((int)(width*scale), (int)(height*scale), BufferedImage.TYPE_INT_ARGB);
		
		num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(0);
		num2=NumberFormat.getNumberInstance();
		num2.setMaximumFractionDigits(3);
		
		
		xst=60;
		yst=50;
		gw=width-100;
		gh=300;
		numCols=gw;
		System.out.println(ci.getNumInds()+" "+ci.getNumCategories());
		//
		//zoom1=new JSlider(JSlider.HORIZONTAL, 0, ci.getNumInds(), ci.getNumCategories());
		//zoom1=new JSlider(JSlider.HORIZONTAL, 0, ci.numComps, ci.numCategories);
		//zoom1.setPaintTicks(false);
		//zoom1.setPaintLabels(false);
		//zoom1.setBorder(BorderFactory.createEmptyBorder(10,20,10, 10));
		//zoom1.addChangeListener(this);
		
		numDistCategories=new JFormattedTextField(num);
		numDistCategories.setColumns(6);
		numDistCategories.setValue(new Integer(ci.getNumTypes()));
		numDistCategories.addPropertyChangeListener("value", this);
		
		//zoom2=new JSlider(JSlider.HORIZONTAL, 0, ci.upgma.le, ci.numTypes);
		
		numCategories=new JFormattedTextField(num);
		numCategories.setColumns(6);
		numCategories.setValue(new Integer(ci.getNumTypes()));
		numCategories.addPropertyChangeListener("value", this);
		
		thresholdSimilarity=new JFormattedTextField(num2);
		thresholdSimilarity.setColumns(6);
		thresholdSimilarity.setValue(new Integer(ci.getNumTypes()));
		thresholdSimilarity.addPropertyChangeListener("value", this);
		
		//zoom2=new JSlider(JSlider.HORIZONTAL, 0, 500, 20);
		//zoom2.setPaintTicks(false);
		//zoom2.setPaintLabels(false);
		//zoom2.setBorder(BorderFactory.createEmptyBorder(10,20,10, 10));
		//zoom2.addChangeListener(this);
		
		JPanel zoomPanel=new JPanel(new BorderLayout());
		
		JPanel z1P=new JPanel(new BorderLayout());
		JLabel z1L=new JLabel("Number of distance categories");
		//z1Lab=new JLabel(Integer.toString(ci.getNumCategories()));
		z1P.add(z1L, BorderLayout.WEST);
		//z1P.add(zoom1, BorderLayout.CENTER);
		z1P.add(numDistCategories, BorderLayout.CENTER);
		//z1P.add(z1Lab, BorderLayout.EAST);
		zoomPanel.add(z1P, BorderLayout.NORTH);
		
		JPanel z2P=new JPanel(new BorderLayout());
		JLabel z2L=new JLabel("Number of categories");
		//z2Lab=new JLabel(Integer.toString(ci.numTypes));
		z2P.add(z2L, BorderLayout.WEST);
		//z2P.add(zoom2, BorderLayout.CENTER);
		z2P.add(numCategories, BorderLayout.CENTER);
		//z2P.add(z2Lab, BorderLayout.EAST);
		zoomPanel.add(z2P, BorderLayout.SOUTH);
		
		//imLabel=new JLabel();
		//JPanel imagePanel=new JPanel();
		//imagePanel.add(imLabel);
		
		
		calculateValues();
		drawGraph();
		
		this.add(zoomPanel, BorderLayout.NORTH);
		this.add(spp.imagePanel, BorderLayout.CENTER);
		//this.add(imagePanel, BorderLayout.CENTER);
				
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
	
	public void calculateValues(){
		
		int nc=ci.getNumCategories();
		
		max=0;
		maxDist=0;
		minDist=60000;
		//System.out.println("Formatting data");
		double[][] confidenceIntervals=ci.getConfidenceIntervals();
		double[][] distanceCategories=ci.getDistanceCategories();
		double[] meanScore=ci.getMeanScore();
		double[][] meanScoreByGroup=ci.getMeanScoreByGroup();
		
		
		results=new double[nc][4+meanScoreByGroup.length];
		
		for (int i=0; i<nc; i++){
			if (confidenceIntervals[1][i]>max){max=confidenceIntervals[1][i];}
			for (int j=0; j<meanScoreByGroup.length; j++){
				if (meanScoreByGroup[j][i]>max){max=meanScoreByGroup[j][i];}
			}
			if (minDist>distanceCategories[1][i]){minDist=distanceCategories[1][i];}
			if (maxDist<distanceCategories[1][i]){maxDist=distanceCategories[1][i];}
		}
		if (maxDist>0){
			maxDist=Math.log(maxDist)*logc;
		}
		if (minDist>0){
			minDist=0.1*Math.floor(10*Math.log(minDist)*logc);
		}
		//System.out.println("Max value is: "+max+" MaxDist is: "+maxDist+" MinDist is: "+minDist);
		if (max>0){
			max=Math.ceil(10*max);
			
			//sigPlaces=(int)((max*-1)+3);
			sigPlaces=4;
			//max=Math.pow(10, max);
			max*=0.1;
			if (max>0.5){max=1;}
			for (int i=0; i<nc; i++){
				results[i][0]=1-(meanScore[i]/max);
				results[i][1]=1-(confidenceIntervals[0][i]/max);
				results[i][2]=1-(confidenceIntervals[1][i]/max);
				results[i][3]=((Math.log(distanceCategories[1][i])*logc)-minDist)/(maxDist-minDist);
				
				for (int j=0; j<meanScoreByGroup.length; j++){
					results[i][4+j]=1-(meanScoreByGroup[j][i]/max);
				}
				
				//System.out.println(results[i][0]+" "+results[i][1]+" "+results[i][2]+" "+results[i][3]+" "+ci.distanceCategories[1][i]+" "+ci.meanScore[i]+" "+ci.confidenceIntervals[0][i]+" "+ci.confidenceIntervals[1][i]);
			}
		}
		//System.out.println("done!");
	}
	
	public void drawGraph(){
		
		int yspan=gh;
		double sx, ex, sy, ey;
		Graphics2D g=imf.createGraphics();
		
		BasicStroke fs=new BasicStroke((int)(scale), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
		g.setStroke(fs);
		
		RenderingHints hints =new RenderingHints(RenderingHints.KEY_RENDERING,
				 RenderingHints.VALUE_RENDER_QUALITY);
				hints.put(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		int fsz=(int)Math.round(fontSize*scale);
		Font fontDef=g.getFont();
		Font font=new Font(fontDef.getName(), fontDef.getStyle(), fsz);
		g.setFont(font);
		FontRenderContext frc = g.getFontRenderContext();
		
		g.setColor(Color.WHITE);
		Rectangle2D.Double r1=new Rectangle2D.Double(0, 0, width*scale, height*scale);
		g.fill(r1);
		
		
		//g.fillRect(0, 0, width, height);
		
		g.setColor(Color.BLACK);
		Rectangle2D.Double r2=new Rectangle2D.Double(xst*scale, yst*scale, gw*scale, gh*scale);
		g.draw(r2);
	
		int nc=ci.getNumCategories();
		
		for (int i=1; i<nc; i++){
			sx=scale*(yspan*(results[i-1][0])+yst);
			ex=scale*(yspan*(results[i][0])+yst);
			sy=scale*(gw*results[i-1][3]+xst);
			ey=scale*(gw*results[i][3]+xst);
			Line2D.Double l1=new Line2D.Double(sy, sx, ey, ex);
			g.draw(l1);			
		}
		
		double diam=5;
		
		for (int i=0; i<nc; i++){
			ex=scale*(yspan*(results[i][0])+yst-(diam*0.5));
			ey=scale*(gw*results[i][3]+xst-(diam*0.5));
			//Rectangle2D.Double r3=new Rectangle2D.Double(ey, ex, diam*scale, diam*scale);
			Arc2D.Double r3=new Arc2D.Double(ey, ex, diam*scale, diam*scale, 0.0, 360.0, Arc2D.Double.OPEN);
			
			g.fill(r3);
		}
		
		g.setColor(Color.GRAY);
		for (int i=1; i<nc; i++){
			for (int j=1; j<3; j++){
				sx=scale*(yspan*(results[i-1][j])+yst);
				ex=scale*(yspan*(results[i][j])+yst);
				sy=scale*(gw*results[i-1][3]+xst);
				ey=scale*(gw*results[i][3]+xst);
				Line2D.Double l1=new Line2D.Double(sy, sx, ey, ex);
				g.draw(l1);	
			}
		}
		
		int numgr=results[0].length-4;
		Color[] pal=getColorPalette(numgr);
		for (int i=0; i<numgr; i++){
			g.setColor(pal[i]);
			for (int j=1; j<nc; j++){
				sx=scale*(yspan*(results[j-1][i+4])+yst);
				ex=scale*(yspan*(results[j][i+4])+yst);
				sy=scale*(gw*results[j-1][3]+xst);
				ey=scale*(gw*results[j][3]+xst);
				Line2D.Double l1=new Line2D.Double(sy, sx, ey, ex);
				g.draw(l1);	
			}
		}
		
		
		g.setColor(Color.BLACK);
		
		double place=Math.pow(10, Math.floor(minDist));
		double place2=0;
		double place3=0;
		float xpl=0;
		float ypl=(float)(scale*(yst+gh+5));
		double ypl1=scale*(yst+gh);
		double ypl2=scale*(yst+gh+3);
		int maxHeight=0;
		while (place<Math.pow(10, maxDist)){
			
			place2=Math.log(place)*logc;
			place2=gw*(place2-minDist)/(maxDist-minDist);
			
			Integer p=new Integer((int)place);
			String pl=p.toString();
			TextLayout layout = new TextLayout(pl, font, frc);
			Rectangle r = layout.getPixelBounds(null, 0, 0);
			if (r.height>maxHeight){maxHeight=r.height;}
			xpl=(float)(scale*(place2+xst));
			if (place>Math.pow(10, minDist)){
				layout.draw(g, xpl-(r.width/2), ypl+r.height);
				//g.drawString(pl, xpl, ypl+r.height);
				Line2D.Double l1=new Line2D.Double(xpl, ypl1, xpl, ypl2);
				g.draw(l1);
				//g.drawLine(xpl+3, yst+gh, xpl+3, yst+gh+3);
			}
			int count=2;
			place3=place*count;
			while ((count<10)&&(place3<Math.pow(10, maxDist))){
				place2=Math.log(place3)*logc;
				place2=gw*(place2-minDist)/(maxDist-minDist);
				//xpl=(int)Math.round(place2+xst);
				xpl=(float)(scale*(place2+xst));
				if (place3>Math.pow(10, minDist)){
					Line2D.Double l1=new Line2D.Double(xpl, ypl1, xpl, ypl2);
					g.draw(l1);
					//g.drawLine(xpl, yst+gh, xpl, yst+gh+3);
				}
				count++;
				place3=place*count;
			}
			place*=10;
		}
		
		
		String xLabel="Distance (m)";
		TextLayout layout = new TextLayout(xLabel, font, frc);
		Rectangle r = layout.getPixelBounds(null, 0, 0);
		xpl=(float)(scale*(xst+(gw/2))-(r.width/2));
		ypl=(float)(scale*(yst+gh+10)+maxHeight+r.height);
		layout.draw(g, xpl, ypl);
		
		DecimalFormat nf=new DecimalFormat("#.##");
		
		double tickspace=0.1*gh;
		place=0;
		double tickVal1=0;
		//int ypl=0;
		int maxWidth=0;
		double xpl1=scale*xst;
		double xpl2=scale*(xst-3);
		xpl=(float)(scale*(xst-5));
		
		while (place<gh){
			
			String pl1= nf.format(tickVal1);
			layout = new TextLayout(pl1, font, frc);
			r = layout.getPixelBounds(null, 0, 0);
			if (r.width>maxWidth){maxWidth=r.width;}
			
			ypl=(float)(scale*(yst+gh-place));
			
			layout.draw(g, xpl-r.width, ypl+(r.height/2));
			
			Line2D.Double l1=new Line2D.Double(xpl1, ypl, xpl2, ypl);
			g.draw(l1);
	
			tickVal1+=0.1*max;
			place+=tickspace;
		}
		
		
		layout = new TextLayout("Jaccard Index" , font, frc);
		r = layout.getPixelBounds(null, 0, 0);	
		
		AffineTransform affineTransform = new AffineTransform(); 		
		double xpos=scale*(xst-10)-maxWidth-(r.width/2);
		double ypos=scale*(yst+gh*0.5)+(r.height/2);
		affineTransform.setToTranslation(xpos, ypos);
		affineTransform.rotate(Math.toRadians(270), r.width/2, r.height/2); 

		g.transform(affineTransform);
		
		layout.draw(g, 0, 0);
		
		g.dispose();
		
		//imLabel.setIcon(new ImageIcon(imf));
		
		spp.paintImage(imf, scale);
		
		//repaint();
	}
	
	//public void paintComponent(Graphics g) {
	//	super.paintComponent(g);  //paint background
	//	g.drawImage(imf, 0, 0, this);
	//}
	/*
	public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
        	int value1=(int)zoom1.getValue();
        	int value2 = (int)((Number)numCategories.getValue()).intValue();
        	//int value2=(int)zoom2.getValue();
        	z1Lab.setText(Integer.toString(value1));
        	//z2Lab.setText(Integer.toString(value2));
        	ci.doComparison(value1, value2);
        	calculateValues();
			drawGraph();
			spp.revalidate();
        }
    }
    */
	
	public void propertyChange(PropertyChangeEvent e) {
       // Object source = e.getSource();
       // if (source==numCategories){
        	int p = (int)((Number)numCategories.getValue()).intValue();
        	//int value1=(int)zoom1.getValue();
        	int value1=(int)((Number)numDistCategories.getValue()).intValue();
        	ci.doComparison(value1, p);
        	calculateValues();
			drawGraph();
			spp.revalidate();
        //}
       // if (source==numDistCategories){
        	
        //}
        
	}
	
	public void export(){
		SaveDocument sd=new SaveDocument(this, defaults);
		boolean readyToWrite=sd.makeFile();
		if (readyToWrite){
			sd.writeString("distance");
			sd.writeString("average similarity");
			sd.writeString("maximum ci");
			sd.writeString("minimum ci");
			sd.writeLine();
			int nc=ci.getNumCategories();
			
			double[][] confidenceIntervals=ci.getConfidenceIntervals();
			double[][] distanceCategories=ci.getDistanceCategories();
			double[] meanScore=ci.getMeanScore();
			double[][] coordinates=ci.getCoordinates();
			int[][] repertoires=ci.getRepertoires();
			int[][] repertoires2=ci.getRepertoires2();
			double[][] meanScoreByGroup=ci.getMeanScoreByGroup();
			
			for (int i=0; i<nc; i++){
				sd.writeDouble(distanceCategories[0][i]);
				sd.writeDouble(distanceCategories[1][i]);
				sd.writeDouble(meanScore[i]);
				sd.writeDouble(confidenceIntervals[0][i]);
				sd.writeDouble(confidenceIntervals[1][i]);
				for (int j=0; j<meanScoreByGroup.length; j++){
					sd.writeDouble(meanScoreByGroup[j][i]);
				}
				sd.writeLine();
			}
			sd.writeSheet("repertoires");
			for (int i=0; i<coordinates.length; i++){
				sd.writeInt(i+1);
				sd.writeDouble(coordinates[i][0]);
				sd.writeDouble(coordinates[i][1]);
				for (int j=0; j<repertoires[i].length; j++){
					sd.writeInt(repertoires[i][j]);
				}
				sd.writeLine();
			}
			
			if (repertoires2!=null){
				sd.writeSheet("repertoires2");
				for (int i=0; i<coordinates.length; i++){
					sd.writeInt(i+1);
					sd.writeDouble(coordinates[i][0]);
					sd.writeDouble(coordinates[i][1]);
					for (int j=0; j<repertoires2[i].length; j++){
						sd.writeInt(repertoires2[i][j]);
					}
					sd.writeLine();
				}
			}
			
			sd.writeSheet("distances");
			double[][] geog=ci.getGeographicalDistances();
			for (int i=0; i<geog.length; i++){
				for (int j=0; j<geog[i].length; j++){
					sd.writeDouble(geog[i][j]);
				}
				sd.writeLine();
			}
			sd.writeLine();
			sd.finishWriting();
		}
		
	}
}
