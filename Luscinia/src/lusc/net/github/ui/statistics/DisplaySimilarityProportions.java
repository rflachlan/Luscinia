package lusc.net.github.ui.statistics;
//
//  DisplaySimilarityProportions.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/11/2005.
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
//import lusc.net.github.analysis.SongGroup;
import lusc.net.github.analysis.AnalysisGroup;
import lusc.net.github.analysis.clustering.KMedoids;
import lusc.net.github.ui.SaveDocument;
import lusc.net.github.ui.SaveImage;

public class DisplaySimilarityProportions extends DisplayPane {
	
	Defaults defaults;
	int width, height;
	BufferedImage imf;
	//SongGroup sg;
	AnalysisGroup sg;
	int [] results1, results2, results3;
	int[][] catresults1, catresults2;
	int[][]categories=null;
	int numcats=0;
	float [][] labelledResults;
	int xst,yst,gw,gh, type;
	double xunit=1;
	int numCols=100;
	int numCategories=6;
	double dx=0;
	
	//public DisplaySimilarityProportions(SongGroup sg, KMedoids km, int type, int width, int height, Defaults defaults){

	public DisplaySimilarityProportions(AnalysisGroup sg, KMedoids km, int type, int width, int height, Defaults defaults){
		this.sg=sg;
		this.width=width;
		this.height=height;
		this.type=type;
		this.defaults=defaults;
		
		if (km!=null){
			categories=km.getOverallAssignments();
			numcats=categories.length;
		}
		this.setPreferredSize(new Dimension(width, height));
		imf=new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		xst=50;
		yst=50;
		gw=width-100;
		gh=500;
		
		xunit=gw/(numCols+0.0);
		
		//numCols=gw;
		calculateValues();
		//calculateLabelledValues();
		//calculateRandomGraph();
		drawGraph();
	}
	
	public void calculateValues(){
		results1=new int[numCols+1];
		results2=new int[numCols+1];
		results3=new int[numCols+1];
		if(categories!=null){
			catresults1=new int[numcats][numCols+1];
			catresults2=new int[numcats][numCols+1];
		}
		double max=0;
		double tot=0;
		double count=0;
		int place;
		double score=0;
		
		int scoreLength=0;
		int[]popIds={0};
		scoreLength=sg.getLengths(type);
		popIds=sg.getPopulationListArray(type);
		double[][] scores=sg.getScores(type).getDiss();
		int[][] lookUp=sg.getLookUp(type);

		
		for (int i=0; i<scoreLength; i++){
			for (int j=0; j<i; j++){
				score=scores[i][j];
				tot+=score;
				count++;
				if (score>max){max=score;}
			}
		}
		tot/=count;
		tot*=4;

		//tot/=count;
		//tot*=4;
		dx=tot/(numCols+0.0);
		for (int i=0; i<scoreLength; i++){
			for (int j=0; j<i; j++){
				boolean samesong=false;
				score=scores[i][j];
				if ((type<4)&&(lookUp[i][0]==lookUp[j][0])){
					samesong=true;
				}
		
				place=(int)Math.round(score*numCols/tot);
				if ((place>=0)&&(place<results1.length)){
					results1[place]++;
					if (samesong){
						results2[place]++;
					}
					if (popIds[i]==popIds[j]){
						results3[place]++;
					}
					if (categories!=null){
						for (int l=0; l<numcats; l++){
							if (categories[l][i]==categories[l][j]){
								catresults1[l][place]++;
								if (popIds[i]==popIds[j]){
									catresults2[l][place]++;
								}
							}
						}
					}
				}
			}
		}
		
	}
	
	public void calculateLabelledValues(){
		labelledResults=new float[numCols+1][numCategories+1];
		double max=0;
		double tot=0;
		double count=0;
		int place;
		int locDiff=0;
		double score=0;
		
		int scoreLength=sg.getLengths(type);
		double[][] scores=sg.getScores(type).getDiss();
		int[][] lookUp=sg.getLookUp(type);
		double[] labels=sg.getLabels(type);


		
		for (int i=0; i<scoreLength; i++){
			for (int j=0; j<i; j++){
				score=scores[i][j];
				tot+=score;
				count++;
				if (score>max){max=score;}
			}
		}
		tot/=count;
		tot*=4;
		dx=tot/(numCols+0.0);
				
		
		for (int i=0; i<scoreLength; i++){
			for (int j=0; j<i; j++){
				//if (sg.lookUpEls[i][0]==sg.lookUpEls[j][0]){
				score=scores[i][j];
				place=(int)Math.round(score*numCols/tot);
				if ((place>=0)&&(place<results1.length)){
					if (type<4){
						locDiff=(int)Math.round(numCategories*Math.abs(labels[i]-labels[j]));
						if (locDiff>numCategories){locDiff=numCategories-1;}
					}
					labelledResults[place][locDiff]++;
				}
				//}
			}
		}
		float[] maxLocs=new float[numCategories+1];
		for (int i=0; i<numCols+1; i++){
			for (int j=0; j<numCategories+1; j++){
				if (labelledResults[i][j]>maxLocs[j]){maxLocs[j]=labelledResults[i][j];}
			}
		}
		for (int i=0; i<numCols+1; i++){
			for (int j=0; j<numCategories+1; j++){
				labelledResults[i][j]/=maxLocs[j];
			}
		}
	}

	public void drawGraph(){
		
		
		float max2=0;
		float max3=0;
		float max4=0;
		for (int i=0; i<numCols+1; i++){
			if (results1[i]>max2){max2=results1[i];}
			if (results2[i]>max3){max3=results2[i];}
			if (results3[i]>max4){max4=results3[i];}
		}
		
		
		
		int yspan=gh;
		int sx, ex, stx, enx;
		Graphics2D g=imf.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		g.setColor(Color.BLACK);
		g.drawRect(xst, yst, gw, gh);
		g.setColor(Color.RED);
		for (int i=1; i<numCols; i++){
			double p=1-(results1[i-1]/max2);
			double q=1-(results1[i]/max2);
			sx=(int)Math.round(yspan*p+yst);
			ex=(int)Math.round(yspan*q+yst);
			
			stx=(int)Math.round((i-1)*xunit+xst);
			enx=(int)Math.round(i*xunit+xst);
			
			g.drawLine(stx, sx, enx, ex);
		}
		g.setColor(Color.BLUE);
		for (int i=1; i<numCols; i++){
			double p=1-(results2[i-1]/max3);
			double q=1-(results2[i]/max3);
			sx=(int)Math.round(yspan*p+yst);
			ex=(int)Math.round(yspan*q+yst);
			
			stx=(int)Math.round((i-1)*xunit+xst);
			enx=(int)Math.round(i*xunit+xst);
			
			g.drawLine(stx, sx, enx, ex);
		}
		g.setColor(Color.GREEN);
		for (int i=1; i<numCols; i++){
			double p=1-(results3[i-1]/max4);
			double q=1-(results3[i]/max4);
			sx=(int)Math.round(yspan*p+yst);
			ex=(int)Math.round(yspan*q+yst);
			
			stx=(int)Math.round((i-1)*xunit+xst);
			enx=(int)Math.round(i*xunit+xst);
			
			g.drawLine(stx, sx, enx, ex);
		}
		/*
		g.setColor(Color.GRAY);
		for (int i=1; i<numCols; i++){
			sx=(int)Math.round(yspan*(1-resultsR[i-1])+yst);
			ex=(int)Math.round(yspan*(1-resultsR[i])+yst);
			g.drawLine(i-1+xst, sx, i+xst, ex);
		}
		*/
		/*
		Color[] colors=new Color[numCategories+1];

		for (int i=0; i<numCategories+1; i++){
			float p=i/(numCategories-0.0f);
			float q=1f-p;
			float r=Math.min(p,q);
			p-=0.5f;
			q-=0.5f;
			if (p<0){p=0f;}
			if (q<0){q=0f;}			
			colors[i]=new Color(p,r,q);
		}
		
		for (int j=0; j<numCategories+1; j++){
			g.setColor(colors[j]);
			//if (j==0){g.setColor(Color.RED);}
			//else{g.setColor(Color.BLUE);}
			for (int i=1; i<numCols; i++){
				sx=(int)Math.round(yspan*(1-labelledResults[i-1][j])+yst);
				ex=(int)Math.round(yspan*(1-labelledResults[i][j])+yst);
				stx=(int)Math.round((i-1)*xunit+xst);
				enx=(int)Math.round(i*xunit+xst);
			
				g.drawLine(stx, sx, enx, ex);
			}
		}
		*/
		
	
		g.setColor(Color.BLACK);
		double tickspace=0.1/dx;
		double place=0;
		int tickVal=0;
		int tickVal1=0;
		int xpl=0;
		while (place*xunit<gw){
			Integer p=new Integer(tickVal);
			String pl=p.toString();
			Integer p1=new Integer(tickVal1);
			String pl1=p1.toString();
			xpl=(int)Math.round(place*xunit-3+xst);
			g.drawString(pl1+"."+pl, xpl, yst+gh+20);
			tickVal++;
			if (tickVal==10){
				tickVal=0;
				tickVal1++;
			}
			place+=tickspace;
		}
		
		g.dispose();
		repaint();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);  //paint background
		g.drawImage(imf, 0, 0, this);
	}
	
	public void saveImage(){
		SaveImage si=new SaveImage(imf, this, defaults);
		//si.save();
	}
	
	public void export(){
		SaveDocument sd=new SaveDocument(this, defaults);
		boolean readyToWrite=sd.makeFile();
		if (readyToWrite){
			sd.writeString("Dissimilarity");
			sd.writeString("Overall");
			sd.writeString("Within-Population");
			sd.writeString("Within-Individual");
			
			if (categories!=null){
				for (int i=0; i<numcats; i++){
					sd.writeString("Within cat k="+(i+2));
				}
				for (int i=0; i<numcats; i++){
					sd.writeString("Within cat, within pop k="+(i+2));
				}
			
			}
			sd.writeLine();
			
			for (int j=0; j<numCols; j++){
				sd.writeDouble(dx*(j+1));
				
				sd.writeInt(results1[j]);
				sd.writeInt(results3[j]);
				sd.writeInt(results2[j]);
				if (categories!=null){
					for (int i=0; i<numcats; i++){
						sd.writeInt(catresults1[i][j]);
					}
					for (int i=0; i<numcats; i++){
						sd.writeInt(catresults2[i][j]);
					}
				
				}
			
				sd.writeLine();
			}
			
			sd.finishWriting();
		}
	}

}
