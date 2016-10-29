package lusc.net.github.ui.statistics;
//
//  DisplaySimilarityProportions.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/11/2005.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.text.DecimalFormat;
import java.util.*;

import lusc.net.github.Defaults;
import lusc.net.github.analysis.ComparisonResults;
import lusc.net.github.ui.DistanceDistributionOptions;
import lusc.net.github.ui.SaveDocument;
import lusc.net.github.ui.SaveImage;

public class DisplaySimilarityProportions extends DisplayPane {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Defaults defaults;
	int width, height;
	BufferedImage imf;
	ComparisonResults cr;
	LinkedList<double[]> resultsM;
	LinkedList<String> patternNames;
	
	double xst,yst,gw,gh;
	int type;
	double xunit=1;
	int numCols=100;
	double dx=0;
	double mult=1.0;
	int fontSize=12;
	
	double maxVal;

	public DisplaySimilarityProportions(ComparisonResults cr, int width, int height, Defaults defaults, DistanceDistributionOptions ddo){
		this.cr=cr;;
		this.width=width;
		this.height=height;
		this.type=cr.getType();
		
		this.defaults=defaults;
		numCols=ddo.numCols;
		fontSize=ddo.fontSize;
		
		this.setPreferredSize(new Dimension(width, height));
		
		mult=defaults.getScaleFactor();	
		System.out.println(mult);
		
		preparePlot(mult);
		imf=drawGraph(resultsM, maxVal);
		
		repaint();
		
	}
		
		
	public void preparePlot(double mult){

		
		
		LinkedList<int[]> patterns=new LinkedList<int[]>();
		patternNames=new LinkedList<String>();
		
		patternNames.add("Overall");
		
		if (type<5){
			int[][] x=cr.getLookUp();
			int[] y=new int[x.length];
			for (int i=0; i<x.length; i++){y[i]=x[i][0];}
			patterns.add(y);
			patternNames.add("Song-type");
		}
		if (type<6){
			String[] ni=cr.getIndividualNames();
			if (ni.length>1){
				int[] x=cr.getLookUpIndividuals();
				patterns.add(x);
				patternNames.add("Individual");
				
			}
		}
		String[] np=cr.getPopulationNames();
		if (np.length>1){
			int[] x=cr.getPopulationListArray();
			patterns.add(x);
			patternNames.add("Population");
		}
		String[] ns=cr.getSpeciesNames();
		if (ns.length>1){
			int[] x=cr.getSpeciesListArray();
			patterns.add(x);
			patternNames.add("Species");
		}
		
		
		
		imf=new BufferedImage((int)(width*mult), (int)(height*mult), BufferedImage.TYPE_INT_ARGB);
		
		xst=75*mult;
		yst=50*mult;
		gw=mult*(width-200);
		gh=500*mult;
		
		fontSize=(int)Math.round(fontSize*mult);
	
		//numCols=gw;
		int[][] cats=calculateCatMatrix();
		xunit=gw/(numCols+0.0);
		
		resultsM=new LinkedList<double[]>();
		
		
		int[] sp=new int[cats.length];
		double[] r=calculateValues(cats, sp);
		resultsM.add(r);
		
		for (int i=0; i<patterns.size(); i++){
			int[] x=(int[])patterns.get(i);		
			double[] s=calculateValues(cats, x);
			resultsM.add(s);			
		}
		maxVal=getMax(resultsM);
		
	}
	
	
	public double getMaxD(LinkedList<double[][]> m){
		double max=0;
		for (int i=0; i<m.size(); i++){
			double[][] x=m.get(i);			
			for (int j=0; j<x.length; j++){
				for (int k=0; k<x[j].length; k++){
					if (x[j][k]>max){max=x[j][k];}
				}
			}
		}
		
		return max;
	}
	
	public double getMax(LinkedList<double[]> m){
		double max=0;
		for (int i=0; i<m.size(); i++){
			double[] x=m.get(i);			
			for (int j=0; j<x.length-1; j++){
				if (x[j]>max){max=x[j];}
			}
		}
		
		return max;
	}
	
	public int[][] calculateCatMatrix(){
		double[][] scores=cr.getDiss();
		int scoreLength=scores.length;
		
		if (numCols>scoreLength){
			numCols=scoreLength;
		}
		
		
		int[][] out=new int[scoreLength][];
		for (int i=0; i<scoreLength; i++){
			out[i]=new int[i+1];
		}
		
		
		double max=0;
		double tot=0;
		double count=0;
		double score=0;
		int place=0;
		
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
		
		tot=Math.min(tot, max);
		
		dx=tot/(numCols+0.0);
		for (int i=0; i<scoreLength; i++){
			for (int j=0; j<i; j++){
				score=scores[i][j];

				place=(int)Math.round(score*numCols/tot);
				out[i][j]=place;
			}
		}
		return out;
	}
	
	
	public double[] calculateValues(int[][] cats, int[] label){
		
		double[] results=new double[numCols+2];
		
		double counts=0;
		
		int n=cats.length;
		
		
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				if (label[i]==label[j]){
					results[cats[i][j]]++;
					counts++;
				}
			}
		}
		
		for (int i=0; i<results.length-1; i++){
			results[i]/=counts;
		}
		results[results.length-1]=counts;
		return results;
	}

	public double[][] calculateValuesO(int[][] cats, int[] label, int nlabs){
		
		double[][] results=new double[nlabs][numCols+1];
		
		double[] counts=new double[nlabs];
		
		int n=cats.length;
		
		
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				if (label[i]==label[j]){
					results[label[i]][cats[i][j]]++;
					counts[label[i]]++;
				}
			}
		}
		
		for (int i=0; i<results.length; i++){
			for (int j=0; j<results[i].length; j++){
				if (counts[j]>0){
					results[i][j]/=counts[i];
				}
			}
		}
		return results;
	}
	
	
	/*
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
	*/
	

	public BufferedImage drawGraph(LinkedList<double[]> results, double max){
		
		double sx, ex, stx, enx;
		Graphics2D g=imf.createGraphics();
		BasicStroke fs=new BasicStroke((int)(mult), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
		g.setStroke(fs);
		
		RenderingHints hints =new RenderingHints(RenderingHints.KEY_RENDERING,
				 RenderingHints.VALUE_RENDER_QUALITY);
				hints.put(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		Font fontDef=g.getFont();
		Font font=new Font(fontDef.getName(), fontDef.getStyle(), fontSize);
		g.setFont(font);
		FontRenderContext frc = g.getFontRenderContext();
				
		DecimalFormat nf=new DecimalFormat("#.#");
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, imf.getWidth(), imf.getHeight());
		g.setColor(Color.BLACK);
		Rectangle2D.Double r1=new Rectangle2D.Double(xst, yst, gw, gh);
		//g.drawRect(xst, yst, gw, gh);		
		g.draw(r1);
		
		
		
		int col=0;
		int numColors=results.size()-1;
		for (int i=0; i<results.size(); i++){
			double[] x=results.get(i);
			if (i==0){
				g.setColor(Color.BLACK);
			}
			else{
				float h=(float)(col/(numColors+0.0f));
				Color c=Color.getHSBColor(h, 0.85f, 1.0f);
				g.setColor(c);
				col++;
			}
			
			for (int k=1; k<numCols; k++){
				double p=1-(x[k-1]/max);
				double q=1-(x[k]/max);
				sx=gh*p+yst;
				ex=gh*q+yst;
					
				stx=(k-1)*xunit+xst;
				enx=k*xunit+xst;
				
				Line2D.Double l1=new Line2D.Double(stx, sx, enx, ex);
				
				//System.out.println(i+" "+sx+" "+ex+" "+stx+" "+enx+" "+x[k-1]+" "+x[k]);
				//g.drawLine(stx, sx, enx, ex);	
				g.draw(l1);
			}	
		}
	
		g.setColor(Color.BLACK);
		double tickspace=0.1/dx;
		double tickval=0;
		double place=0;
		float xpl=0;
		float ypl=0;
		float ypl2=0;
		while (place*xunit<gw){
			
			String pl=nf.format(tickval);
			xpl=(float)(place*xunit+xst);
			ypl=(float)(yst+gh);
			ypl2=(float)(ypl+5*mult);
			
			Line2D.Double l1=new Line2D.Double(xpl, ypl, xpl, ypl2);
			g.draw(l1);
			
			ypl=(float)(yst+gh+20*mult);
			
			
			TextLayout layout = new TextLayout(pl, font, frc);
			Rectangle r = layout.getPixelBounds(null, 0, 0);
			layout.draw(g, xpl-r.width/2, ypl);
			tickval+=0.1;
			place+=tickspace;
		}
		
		String xaxis="Dissimilarity";
		TextLayout layout2 = new TextLayout(xaxis, font, frc);
		Rectangle r2 = layout2.getPixelBounds(null, 0, 0);
		layout2.draw(g, (float)(gw/2+xst-r2.width), (float)(yst+gh+40*mult));
		
		
		double unit=0.1;
		String form="#.#";
		DecimalFormat nf2=new DecimalFormat(form);
		while (unit*2>max){
			unit/=10;
			form=form+"#";
			nf2=new DecimalFormat(form);
		}
		
		double tickspaceY=unit*gh/max;
		tickval=0;
		place=0;
		xpl=0;
		ypl=0;
		float xpl2=0;
		float minstart=1000000;
		while (place<gh){
			System.out.println(place+" "+gh+" "+tickspaceY);
			String pl=nf2.format(tickval);
			xpl=(float)(xst);
			ypl=(float)(yst+gh-place);
			xpl2=(float)(xst-5*mult);
			
			Line2D.Double l1=new Line2D.Double(xpl, ypl, xpl2, ypl);
			g.draw(l1);
			
			xpl=(float)(xst-10*mult);
			
			TextLayout layout = new TextLayout(pl, font, frc);
			Rectangle r = layout.getPixelBounds(null, 0, 0);
			xpl2=xpl-r.width;
			if (xpl2<minstart){minstart=xpl2;}
			layout.draw(g, xpl2, ypl+r.height/2);
			tickval+=unit;
			place+=tickspaceY;
		}
		
		
		String yaxis="Relative Frequency";
		TextLayout layout3 = new TextLayout(yaxis, font, frc);
		Rectangle r3 = layout3.getPixelBounds(null, 0, 0);
		
		int w2=r3.width;
		int h2=r3.height;
		BufferedImage vt=new BufferedImage(w2+5, h2+5, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2=vt.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(Color.BLACK);
		g2.setFont(font);
		layout3.draw(g2, 1, h2-1);		
		g2.dispose();
		
		AffineTransform affineTransform = new AffineTransform(); 		
		double xpos=minstart-5*mult-(w2/2)-h2;
		affineTransform.setToTranslation(xpos, yst+gh*0.5+(h2/2));
		affineTransform.rotate(Math.toRadians(270), w2/2, h2/2); 
		
		g.drawImage(vt, affineTransform, this); 
		
		
		
		col=0;
		for (int i=0; i<results.size(); i++){
			if (i==0){
				g.setColor(Color.BLACK);
			}
			else{
				float h=(float)(col/(numColors+0.0f));
				Color c=Color.getHSBColor(h, 0.85f, 1.0f);
				g.setColor(c);
				col++;
			}
			sx=gh*0.25+yst+20*mult*i;
			ex=sx;
			
			stx=gw+xst+mult*10;
			enx=stx+mult*20;
			
			Line2D.Double l1=new Line2D.Double(stx, sx, enx, ex);
			g.draw(l1);
			
			g.setColor(Color.BLACK);
			
			String s=patternNames.get(i);
			
			xpl=(float)(enx+5*mult);
			
			
			TextLayout layout = new TextLayout(s, font, frc);
			Rectangle r = layout.getPixelBounds(null, 0, 0);
			layout.draw(g, xpl, (float)(sx+0.5*r.height));
			
			
			//g.drawString(s, xpl, ypl);
		}
		g.dispose();	
		return (imf);
		
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);  //paint background
		Graphics2D g2=(Graphics2D) g;
		g2.scale(1/mult, 1/mult);
		//System.out.println(imf.getWidth()+" "+imf.getHeight());
		g2.drawImage(imf, 0, 0, this);
		//g.drawImage(imf, 0, 0, this);
	}
	
	public void saveImage(){
		new SaveImage(imf, this, defaults);
		//si.save();
	}
	
	public BufferedImage resizeImage(double ratio){
		preparePlot(ratio);

		BufferedImage imf1=drawGraph(resultsM, maxVal);
		
		preparePlot(mult);
		imf=drawGraph(resultsM, maxVal);
		
		return imf1;
	}	
	
	
	public void export(){
		SaveDocument sd=new SaveDocument(this, defaults);
		boolean readyToWrite=sd.makeFile();
		if (readyToWrite){
			sd.writeString("Dissimilarity");
			
			for (int i=0; i<patternNames.size(); i++){
				sd.writeString(patternNames.get(i));
			}
			
			sd.writeLine();
			
			for (int j=0; j<numCols; j++){
				sd.writeDouble(dx*(j+1));
				
				for (int i=0; i<resultsM.size(); i++){
					double[] x=resultsM.get(i);
					sd.writeDouble(x[j]);
				}
				sd.writeLine();
				
			}
			sd.writeInt(0);
			for (int i=0; i<resultsM.size(); i++){
				double[] x=resultsM.get(i);
				sd.writeDouble(x[x.length-1]);
			}
			sd.writeLine();
			sd.finishWriting();
		}
	}

}
