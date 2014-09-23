package lusc.net.sourceforge;
//
//  DisplayGeographicComparison.java
//  Luscinia
//
//  Created by Robert Lachlan on 9/15/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

import javax.swing.*;
import javax.swing.event.*;
import java.awt.geom.AffineTransform;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;


public class DisplayGeographicComparison extends DisplayPane implements PropertyChangeListener, ChangeListener{
	
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
	JSlider zoom1, zoom2;
	JLabel z1Lab, z2Lab, imLabel;
	JPanel mainPanel=new JPanel(new BorderLayout());
	SongGroup sg;
	
	JFormattedTextField numCategories, thresholdSimilarity;
	NumberFormat num, num2;
	
	public DisplayGeographicComparison(GeographicComparison ci, int width, int height, SongGroup sg, Defaults defaults){
		this.ci=ci;
		this.width=width;
		this.height=height;
		this.sg=sg;
		this.setPreferredSize(new Dimension(width, height));
		this.defaults=defaults;
		imf=new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(0);
		num2=NumberFormat.getNumberInstance();
		num2.setMaximumFractionDigits(3);
		
		
		xst=60;
		yst=50;
		gw=width-100;
		gh=300;
		numCols=gw;
		
		JPanel zoomPanel=new JPanel(new BorderLayout());
		zoom1=new JSlider(JSlider.HORIZONTAL, 0, ci.numInds, ci.numCategories);
		//zoom1=new JSlider(JSlider.HORIZONTAL, 0, ci.numComps, ci.numCategories);
		zoom1.setPaintTicks(false);
		zoom1.setPaintLabels(false);
		zoom1.setBorder(BorderFactory.createEmptyBorder(10,20,10, 10));
		zoom1.addChangeListener(this);
		
		//zoom2=new JSlider(JSlider.HORIZONTAL, 0, ci.upgma.le, ci.numTypes);
		
		numCategories=new JFormattedTextField(num);
		numCategories.setColumns(6);
		numCategories.setValue(new Integer(ci.numTypes));
		numCategories.addPropertyChangeListener("value", this);
		
		thresholdSimilarity=new JFormattedTextField(num2);
		thresholdSimilarity.setColumns(6);
		thresholdSimilarity.setValue(new Integer(ci.numTypes));
		thresholdSimilarity.addPropertyChangeListener("value", this);
		
		//zoom2=new JSlider(JSlider.HORIZONTAL, 0, 500, 20);
		//zoom2.setPaintTicks(false);
		//zoom2.setPaintLabels(false);
		//zoom2.setBorder(BorderFactory.createEmptyBorder(10,20,10, 10));
		//zoom2.addChangeListener(this);
		
		JPanel z1P=new JPanel(new BorderLayout());
		JLabel z1L=new JLabel("Number of distance categories");
		z1Lab=new JLabel(Integer.toString(ci.numCategories));
		z1P.add(z1L, BorderLayout.WEST);
		z1P.add(zoom1, BorderLayout.CENTER);
		z1P.add(z1Lab, BorderLayout.EAST);
		zoomPanel.add(z1P, BorderLayout.NORTH);
		
		JPanel z2P=new JPanel(new BorderLayout());
		JLabel z2L=new JLabel("Number of categories");
		//z2Lab=new JLabel(Integer.toString(ci.numTypes));
		z2P.add(z2L, BorderLayout.WEST);
		//z2P.add(zoom2, BorderLayout.CENTER);
		z2P.add(numCategories, BorderLayout.CENTER);
		//z2P.add(z2Lab, BorderLayout.EAST);
		zoomPanel.add(z2P, BorderLayout.SOUTH);
		
		imLabel=new JLabel();
		JPanel imagePanel=new JPanel();
		imagePanel.add(imLabel);
		
		
		calculateValues();
		drawGraph();
		
		this.add(zoomPanel, BorderLayout.NORTH);
		this.add(imagePanel, BorderLayout.CENTER);
				
	}
	
	public void calculateValues(){
		
		results=new double[ci.numCategories][4];
		max=0;
		maxDist=0;
		minDist=60000;
		//System.out.println("Formatting data");
		for (int i=0; i<ci.numCategories; i++){
			if (ci.confidenceIntervals[1][i]>max){max=ci.confidenceIntervals[1][i];}
			if (minDist>ci.distanceCategories[1][i]){minDist=ci.distanceCategories[1][i];}
			if (maxDist<ci.distanceCategories[1][i]){maxDist=ci.distanceCategories[1][i];}
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
			for (int i=0; i<ci.numCategories; i++){
				results[i][0]=1-(ci.meanScore[i]/max);
				results[i][1]=1-(ci.confidenceIntervals[0][i]/max);
				results[i][2]=1-(ci.confidenceIntervals[1][i]/max);
				results[i][3]=((Math.log(ci.distanceCategories[1][i])*logc)-minDist)/(maxDist-minDist);
				//System.out.println(results[i][0]+" "+results[i][1]+" "+results[i][2]+" "+results[i][3]+" "+ci.distanceCategories[1][i]+" "+ci.meanScore[i]+" "+ci.confidenceIntervals[0][i]+" "+ci.confidenceIntervals[1][i]);
			}
		}
		//System.out.println("done!");
	}
	
	public void drawGraph(){
		
		int yspan=gh;
		int sx, ex, sy, ey;
		Graphics2D g=imf.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		g.setColor(Color.BLACK);
		g.drawRect(xst, yst, gw, gh);
		String xLabel="Distance (m)";
		g.drawString(xLabel, xst+(gw/2)-20, yst+gh+40);
		
		
		for (int i=1; i<ci.numCategories; i++){
			sx=(int)Math.round(yspan*(results[i-1][0])+yst);
			ex=(int)Math.round(yspan*(results[i][0])+yst);
			sy=(int)Math.round(gw*results[i-1][3]+xst);
			ey=(int)Math.round(gw*results[i][3]+xst);
			g.drawLine(sy, sx, ey, ex);
			
		}
		
		for (int i=0; i<ci.numCategories; i++){
			ex=(int)Math.round(yspan*(results[i][0])+yst);
			ey=(int)Math.round(gw*results[i][3]+xst);
			g.fillRect(ey-1, ex-1, 3, 3);
		}
		
		g.setColor(Color.GRAY);
		for (int i=1; i<ci.numCategories; i++){
			for (int j=1; j<3; j++){
				sx=(int)Math.round(yspan*(results[i-1][j])+yst);
				ex=(int)Math.round(yspan*(results[i][j])+yst);
				sy=(int)Math.round(gw*results[i-1][3]+xst);
				ey=(int)Math.round(gw*results[i][3]+xst);
				g.drawLine(sy, sx, ey, ex);
			}
		}
		
		g.setColor(Color.BLACK);
		
		double place=Math.pow(10, Math.floor(minDist));
		double place2=0;
		double place3=0;
		int xpl=0;
		while (place<Math.pow(10, maxDist)){
			
			place2=Math.log(place)*logc;
			place2=gw*(place2-minDist)/(maxDist-minDist);
			
			Integer p=new Integer((int)place);
			String pl=p.toString();
			
			xpl=(int)Math.round(place2-3+xst);
			if (place>Math.pow(10, minDist)){
				g.drawString(pl, xpl, yst+gh+20);
				g.drawLine(xpl+3, yst+gh, xpl+3, yst+gh+3);
			}
			int count=2;
			place3=place*count;
			while ((count<10)&&(place3<Math.pow(10, maxDist))){
				place2=Math.log(place3)*logc;
				place2=gw*(place2-minDist)/(maxDist-minDist);
				xpl=(int)Math.round(place2+xst);
				if (place3>Math.pow(10, minDist)){
					g.drawLine(xpl, yst+gh, xpl, yst+gh+3);
				}
				count++;
				place3=place*count;
			}
			place*=10;
		}
		
		
		double tickspace=0.1*gh;
		place=0;
		double tickVal1=0;
		int ypl=0;
		while (place<gh){
			Double p1=new Double(tickVal1);
			String pl1=p1.toString();
			if (pl1.length()>sigPlaces){
				pl1=pl1.substring(0, sigPlaces);
			}
			ypl=(int)Math.round(yst+gh-place);
			g.drawString(pl1, xst-(8*sigPlaces), ypl+3);
			g.drawLine(xst, ypl, xst-3, ypl);
			tickVal1+=0.1*max;
			place+=tickspace;
		}
		
		
		AffineTransform af = new AffineTransform();
		af.translate(-30., ((gh/2)+80.));
		af.rotate(3*Math.PI/2);		
		FontRenderContext renderContext = new FontRenderContext(null, false, false);
		g.transform(af);
		TextLayout layout = new TextLayout("Jaccard Index" , g.getFont(), renderContext);
		layout.draw(g, 5, 50);
		g.dispose();
		
		imLabel.setIcon(new ImageIcon(imf));
		
		//repaint();
	}
	
	//public void paintComponent(Graphics g) {
	//	super.paintComponent(g);  //paint background
	//	g.drawImage(imf, 0, 0, this);
	//}
	
	public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
        	int value1=(int)zoom1.getValue();
        	int value2=(int)zoom2.getValue();
        	z1Lab.setText(Integer.toString(value1));
        	//z2Lab.setText(Integer.toString(value2));
        	ci.doComparison(value1, value2);
        	calculateValues();
			drawGraph();
        }
    }
	
	public void propertyChange(PropertyChangeEvent e) {
        Object source = e.getSource();
        System.out.println("HERE");
        if (source==numCategories){
        	int p = (int)((Number)numCategories.getValue()).intValue();
        	int value1=(int)zoom1.getValue();
        	ci.doComparison(value1, p);
        	calculateValues();
			drawGraph();
        }
        
	}
	
	public void export(){
		SaveDocument sd=new SaveDocument(this, sg.defaults);
		boolean readyToWrite=sd.makeFile();
		if (readyToWrite){
			sd.writeString("distance");
			sd.writeString("average similarity");
			sd.writeString("maximum ci");
			sd.writeString("minimum ci");
			sd.writeLine();
			for (int i=0; i<ci.numCategories; i++){
				sd.writeDouble(ci.distanceCategories[1][i]);
				sd.writeDouble(ci.meanScore[i]);
				sd.writeDouble(ci.confidenceIntervals[0][i]);
				sd.writeDouble(ci.confidenceIntervals[1][i]);
				sd.writeLine();
			}
			sd.writeSheet("details");
			for (int i=0; i<ci.sg.individualNumber; i++){
				sd.writeInt(i+1);
				sd.writeDouble(ci.coordinates[i][0]);
				sd.writeDouble(ci.coordinates[i][1]);
				for (int j=0; j<ci.repertoires[i].length; j++){
					sd.writeInt(ci.repertoires[i][j]);
				}
				sd.writeLine();
			}
			sd.finishWriting();
		}
		
	}
}
