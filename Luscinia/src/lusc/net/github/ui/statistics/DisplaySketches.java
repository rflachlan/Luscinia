package lusc.net.github.ui.statistics;
//
//  DisplaySketches.java
//  Luscinia
//
//  Created by Robert Lachlan on 10/3/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;

import lusc.net.github.Song;
import lusc.net.github.Element;
import lusc.net.github.Syllable;
import lusc.net.github.analysis.AnalysisGroup;

public class DisplaySketches {

	AnalysisGroup sg;

	BufferedImage ims=null; 
	Font bodyFont;
	
	boolean signalLogPlot=false;
	boolean fundLogPlot=false;
	boolean contour=true;
	boolean logContour=false;
	int inBuffer=25;
	int outBuffer=25;
	BasicStroke bs1=new BasicStroke(1);
	BasicStroke bs2=new BasicStroke(2);

	
	public DisplaySketches(AnalysisGroup sg){
		this.sg=sg;
	}
	
	public BufferedImage drawElement (int element, int height, int inBuffer, int outBuffer, double dx, boolean decorate){
		int[][] lookUpEls=sg.getLookUp(0);
		int so=lookUpEls[element][0];
		int el=lookUpEls[element][1];
		
		Song song=sg.getSong(so);
		
		Element ele=(Element)song.getElement(el);
		int[][] signal=ele.getSignal();
		double[][] measurements=ele.getMeasurements();
		
		int mf=ele.getMaxF();
		double edy=ele.getDy();
		int eleLength=ele.getLength();
		
		int le=(int)Math.round(ele.getLength()*dx+inBuffer+outBuffer);
		BufferedImage ims=new BufferedImage(le, height, BufferedImage.TYPE_INT_ARGB);
	
		Graphics2D g=ims.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0,0,le,height);
		g.setColor(Color.BLACK);
		if (decorate){
			g.drawRect(0,0,le-1, height-1);
		}
		double ny;
		int y1, y2, x, x1, x2, j2, j3;
		g.setColor(Color.GRAY);
		if (signalLogPlot){
			double maxf=Math.log(mf+0.0);
			double minf=Math.log(100);
			ny=height/(maxf-minf);
			for (int i=0; i<eleLength; i++){
				x=(int)Math.round(inBuffer+(i*dx));
				for (int j=1; j<signal[i].length; j+=2){
					y1=(int)Math.round(ny*(maxf-Math.log(mf-(signal[i][j]*edy))));
					y2=(int)Math.round(ny*(maxf-Math.log(mf-(signal[i][j+1]*edy))));
					g.drawLine(x,y1,x,y2);
				}
			}
		}
		else{
			double maxf=mf;
			ny=height/maxf;
			for (int i=0; i<eleLength; i++){
				x=(int)Math.round(inBuffer+(i*dx));
				for (int j=1; j<signal[i].length; j+=2){
					y1=(int)Math.round(ny*(signal[i][j]*edy));
					y2=(int)Math.round(ny*(signal[i][j+1]*edy));
					g.drawLine(x,y1,x,y2);
				}
			}
		}
		if (contour){
			g.setColor(Color.BLACK);
			if (logContour){
				double maxf=Math.log(mf+0.0);
				double minf=Math.log(100);
				ny=height/(maxf-minf);
				for (int i=0; i<signal.length-1; i++){
					j2=i+5;
					j3=i+6;
					x1=(int)Math.round(inBuffer+(i*dx));
					x2=(int)Math.round(inBuffer+((i+1)*dx));
					y1=(int)Math.round(ny*(maxf-Math.log(measurements[j2][3])));
					y2=(int)Math.round(ny*(maxf-Math.log(measurements[j3][3])));
					g.drawLine(x1,y1,x2,y2);
				}
			}
			else{
				double maxf=mf+0.0;
				ny=height/maxf;
				for (int j=0; j<signal.length-1; j++){
					j2=j+5;
					j3=j+6;
					x1=(int)Math.round(inBuffer+(j*dx));
					x2=(int)Math.round(inBuffer+((j+1)*dx));
					y1=(int)Math.round(ny*(maxf-measurements[j2][3]));
					y2=(int)Math.round(ny*(maxf-measurements[j3][3]));
					g.drawLine(x1,y1,x2, y2);
				}
			}
		}
		g.dispose();
		return ims;
	}
	
	public BufferedImage drawElement (Element ele, int height, boolean decorate){
		
		int inBuffer=(int)Math.round(ele.getTb()/ele.getTimeStep());
		int outBuffer=(int)Math.round(ele.getTa()/ele.getTimeStep());
		if (inBuffer<0){inBuffer=5;}
		if (outBuffer<0){outBuffer=5;}
		int eleLength=ele.getLength();
		int mf=ele.getMaxF();
		double edy=ele.getDy();
		int[][] signal=ele.getSignal();
		double[][] measurements=ele.getMeasurements();
		
		
		int le=(int)Math.round(eleLength+inBuffer+outBuffer);
		BufferedImage ims=new BufferedImage(le, height, BufferedImage.TYPE_INT_ARGB);
	
		Graphics2D g=ims.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0,0,le,height);
		g.setColor(Color.BLACK);
		g.setStroke(bs2);
		if (decorate){
			//g.drawRect(0,0,le-1, height-1);
			
			g.drawLine(0,0,le-1,0);
			g.drawLine(le-1,0,le-1,height-1);
			g.drawLine(le-1,height-1,0,height-1);
			g.drawLine(0,0,0,height-1);
			
		}
		g.setStroke(bs1);
		double ny;
		int y1, y2, x, x1, x2, j2, j3;
		g.setColor(Color.GRAY);
		if (signalLogPlot){
			double maxf=Math.log(mf+0.0);
			double minf=Math.log(100);
			ny=height/(maxf-minf);
			for (int i=0; i<eleLength; i++){
				x=(int)Math.round(inBuffer+i);
				for (int j=1; j<signal[i].length; j+=2){
					y1=(int)Math.round(ny*(maxf-Math.log(mf-(signal[i][j]*edy))));
					y2=(int)Math.round(ny*(maxf-Math.log(mf-(signal[i][j+1]*edy))));
					g.drawLine(x,y1,x,y2);
				}
			}
		}
		else{
			double maxf=mf+0.0;
			ny=height/maxf;
			for (int i=0; i<eleLength; i++){
				x=(int)Math.round(inBuffer+i);
				for (int j=1; j<signal[i].length; j+=2){
					y1=(int)Math.round(ny*(signal[i][j]*edy));
					y2=(int)Math.round(ny*(signal[i][j+1]*edy));
					g.drawLine(x,y1,x,y2);
				}
			}
		}
		if (contour){
			//g.setStroke(bs2);
			g.setColor(Color.BLACK);
			if (logContour){
				double maxf=Math.log(mf+0.0);
				double minf=Math.log(100);
				ny=height/(maxf-minf);
				for (int i=0; i<signal.length-1; i++){
					j2=i+5;
					j3=i+6;
					x1=(int)Math.round(inBuffer+i);
					x2=(int)Math.round(inBuffer+i+1);
					y1=(int)Math.round(ny*(maxf-Math.log(measurements[j2][3])));
					y2=(int)Math.round(ny*(maxf-Math.log(measurements[j3][3])));
					g.drawLine(x1,y1,x2,y2);
				}
			}
			else{
				double maxf=mf+0.0;
				ny=height/maxf;
				for (int i=0; i<signal.length-1; i++){
					j2=i+5;
					j3=i+6;
					x1=(int)Math.round(inBuffer+i);
					x2=(int)Math.round(inBuffer+i+1);
					y1=(int)Math.round(ny*(maxf-measurements[j2][3]));
					y2=(int)Math.round(ny*(maxf-measurements[j3][3]));
					g.drawLine(x1,y1,x2, y2);
				}
			}
		}
		g.dispose();
		return ims;
	}
	
		
	/*
	public BufferedImage drawSyllable (int so, int syll, int height, boolean decorate){
		int[] syl=(int[])sg.songs[so].syllList.get(syll);
		int d=syl.length;
		BufferedImage[] els=new BufferedImage[d];
		int totalLength=0;
		for (int i=0; i<d; i++){
			Element ele=(Element)sg.songs[so].eleList.get(syl[i]);
			els[i]=drawElement(ele, height, false);
			totalLength+=els[i].getWidth();
		}
		BufferedImage ims=new BufferedImage(totalLength, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=ims.createGraphics();
		totalLength=0;
		for (int i=0; i<d; i++){
			g.drawImage(els[i], totalLength, 0, null);
			totalLength+=els[i].getWidth();
			els[i]=null;
		}
		els=null;
		g.setColor(Color.BLACK);
		if (decorate){
			g.drawRect(0,0,totalLength-1, height-1);
		}
		g.dispose();
		return ims;
	}
	 */
	
		
	public BufferedImage drawElements (Element[] ele, int height, boolean decorate){
		
		int d=ele.length;
		
		int begintime=-1;
		int maxtime=0;
		for (int i=0; i<d; i++){
			if (ele[i]!=null){
				if (begintime<0){begintime=ele[i].getBeginTime();}
				int p=ele[i].getBeginTime()+ele[i].getLength();
				if (p>maxtime){maxtime=p;}
			}
		}
		
		int inBuffer=5;
		int outBuffer=5;
		
		int le=(int)Math.round(maxtime-begintime+inBuffer+outBuffer);
		BufferedImage ims=new BufferedImage(le, height, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g=ims.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0,0,le,height);
		g.setColor(Color.BLACK);
		g.setStroke(bs2);
		if (decorate){
			//g.drawRect(0,0,le-1, height-1);
			
			g.drawLine(0,0,le-1,0);
			g.drawLine(le-1,0,le-1,height-1);
			g.drawLine(le-1,height-1,0,height-1);
			g.drawLine(0,0,0,height-1);
			
		}
		g.setStroke(bs1);
		double ny;
		int y1, y2, x, x1, x2, j2, j3;
		g.setColor(Color.GRAY);
		if (signalLogPlot){
			for (int el=0; el<d; el++){
				if (ele[el]!=null){
					int[][] signal=ele[el].getSignal();
					int mf=ele[el].getMaxF();
					double edy=ele[el].getDy();
					double maxf=Math.log(ele[el].getMaxF()+0.0);
					double minf=Math.log(100);
					ny=height/(maxf-minf);
					
					for (int i=0; i<ele[el].getLength(); i++){
						x=(int)Math.round(inBuffer+i+ele[el].getBeginTime()-begintime);
						for (int j=1; j<signal[i].length; j+=2){
							y1=(int)Math.round(ny*(maxf-Math.log(mf-(signal[i][j]*edy))));
							y2=(int)Math.round(ny*(maxf-Math.log(mf-(signal[i][j+1]*edy))));
							g.drawLine(x,y1,x,y2);
						}
					}
				}
			}
		}
		else{
			for (int el=0; el<d; el++){
				if (ele[el]!=null){
					int[][] signal=ele[el].getSignal();
					double edy=ele[el].getDy();
					double maxf=ele[el].getMaxF()+0.0;
					ny=height/maxf;
			
					for (int i=0; i<ele[el].getLength(); i++){
						x=(int)Math.round(inBuffer+i+ele[el].getBeginTime()-begintime);
						for (int j=1; j<signal[i].length; j+=2){
							y1=(int)Math.round(ny*(signal[i][j]*edy));
							y2=(int)Math.round(ny*(signal[i][j+1]*edy));
							g.drawLine(x,y1,x,y2);
						}
					}
				}
			}
		}
		if (contour){
			//g.setStroke(bs2);
			g.setColor(Color.BLACK);
			if (logContour){
				for (int el=0; el<d; el++){
					if (ele[el]!=null){
						int[][] signal=ele[el].getSignal();
						double[][] measurements=ele[el].getMeasurements();
						double edy=ele[el].getDy();
						double maxf=Math.log(ele[el].getMaxF()+0.0);
						double minf=Math.log(100);
						ny=height/(maxf-minf);
				
						for (int i=0; i<signal.length-1; i++){
							j2=i+5;
							j3=i+6;
							x1=(int)Math.round(inBuffer+i+ele[el].getBeginTime()-begintime);
							x2=(int)Math.round(inBuffer+i+1+ele[el].getBeginTime()-begintime);
							y1=(int)Math.round(ny*(maxf-Math.log(measurements[j2][3])));
							y2=(int)Math.round(ny*(maxf-Math.log(measurements[j3][3])));
							g.drawLine(x1,y1,x2,y2);
						}
					}
				}
			}
			else{
				for (int el=0; el<d; el++){
					if (ele[el]!=null){
						int[][] signal=ele[el].getSignal();
						double[][] measurements=ele[el].getMeasurements();
						double edy=ele[el].getDy();
						double maxf=ele[el].getMaxF()+0.0;
						ny=height/maxf;
				
						for (int i=0; i<signal.length-1; i++){
							j2=i+5;
							j3=i+6;
							x1=(int)Math.round(inBuffer+i+ele[el].getBeginTime()-begintime);
							x2=(int)Math.round(inBuffer+i+1+ele[el].getBeginTime()-begintime);
							y1=(int)Math.round(ny*(maxf-measurements[j2][3]));
							y2=(int)Math.round(ny*(maxf-measurements[j3][3]));
							g.drawLine(x1,y1,x2, y2);
						}
					}
				}
			}
		}
		g.dispose();
		return ims;
	}
	
	public BufferedImage drawSyllable (int so, int syll, int height, boolean decorate){
		Song song=sg.getSong(so);
		//int[] syl=(int[])song.getSyllable(syll);
		
		LinkedList<Syllable> sl=song.getBaseLevelSyllables();
		Syllable sy=sl.get(syll);
		Element[] ele=(Element[])sy.getElements2().toArray(new Element[sy.getNumEles2()]);
		/*
		
		int d=syl.length;
		
		Element[] ele=new Element[d];
		int begintime=-1;
		int maxtime=0;
		for (int i=0; i<d; i++){
			if (syl[i]>0){
				ele[i]=(Element)song.getElement(syl[i]);
			}
		}
		*/
		BufferedImage ims=drawElements(ele, height, decorate);
		return ims;
	}
	
	public BufferedImage drawTransition(int transition, int height, boolean decorate){
		int[][] lookUpTrans=sg.getLookUp(3);
		int[][] lookUpSyls=sg.getLookUp(2);
		int so=lookUpTrans[transition][0];
		int tr=lookUpSyls[lookUpTrans[transition][2]][1];
		Song song=sg.getSong(so);
		
		BufferedImage[] syls=new BufferedImage[2];
		int totalLength=0;
		
		
		
		for (int i=0; i<2; i++){
			
			
			Syllable ph=song.getPhrase(tr+i);
			
			Syllable sy=ph.getSyllable(ph.getNumSyllables()/2);
			
			Element[] eles=sy.getElements2().toArray(new Element[sy.getNumEles2()]);
	
			//int[][] phrase=(int[][])song.getPhrase(tr+i);
	
			//int c=phrase.length/2;
			//int d=phrase[c].length;

			
			
			//Element[] eles=new Element[d];
			/*
			boolean complete=false;
			while(complete=false){
				complete=true;
				for (int j=0; i<d; i++){
					if (phrase[c][j]!=-1){
						eles[j]=(Element)song.getElement(phrase[c][j]);
					}
					else if(c<phrase.length-1){
						c++;
						complete=false;
					}
					else{
						eles[j]=null;
					}
				}
			}
			*/
			syls[i]=drawElements(eles, height, decorate);
			totalLength+=syls[i].getWidth();
		}
		BufferedImage ims=new BufferedImage(totalLength, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=ims.createGraphics();
		totalLength=0;
		for (int i=0; i<2; i++){
			g.drawImage(syls[i], totalLength, 0, null);
			totalLength+=syls[i].getWidth();
			syls[i]=null;
		}
		syls=null;
		g.setColor(Color.BLACK);
		g.drawRect(0,0,totalLength-1, height-1);
		g.dispose();
		return ims;
	}
	
	
	
	/*
	public BufferedImage drawTransition(int transition, int height){
		int so=sg.lookUpTrans[transition][0];
		int tr=sg.lookUpSyls[sg.lookUpTrans[transition][2]][1];
		
		BufferedImage[] syls=new BufferedImage[2];
		int totalLength=0;
		for (int i=0; i<2; i++){
			syls[i]=drawPhraseEx(so, tr+i, height, false);
			totalLength+=syls[i].getWidth();
		}
		BufferedImage ims=new BufferedImage(totalLength, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=ims.createGraphics();
		totalLength=0;
		for (int i=0; i<2; i++){
			g.drawImage(syls[i], totalLength, 0, null);
			totalLength+=syls[i].getWidth();
			syls[i]=null;
		}
		syls=null;
		g.setColor(Color.BLACK);
		g.drawRect(0,0,totalLength-1, height-1);
		g.dispose();
		return ims;
	}
	 */
	
	public BufferedImage drawTransition2(int transition, int height){

		int[][] lookUpTrans2=sg.getLookUp(4);
		int[][] lookUpSyls=sg.getLookUp(2);
		
		
		BufferedImage[] syls=new BufferedImage[2];
		int totalLength=0;
		for (int i=0; i<2; i++){
			int p=lookUpTrans2[transition][i];
			int so=lookUpSyls[p][0];
			int tr=lookUpSyls[p][1];
			
			System.out.println("WHAT TO DRAW: "+transition+" "+p+" "+so+" "+tr);
			
			syls[i]=drawPhraseEx(so, tr, height, false);
			totalLength+=syls[i].getWidth();
		}
		BufferedImage ims=new BufferedImage(totalLength, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=ims.createGraphics();
		totalLength=0;
		for (int i=0; i<2; i++){
			g.drawImage(syls[i], totalLength, 0, null);
			totalLength+=syls[i].getWidth();
			syls[i]=null;
		}
		syls=null;
		g.setColor(Color.BLACK);
		g.drawRect(0,0,totalLength-1, height-1);
		g.dispose();
		return ims;
	}
	/*
	public BufferedImage draw (int so, int ph, int height, boolean decorate){
		
		int[][] phrase=(int[][])sg.songs[so].phrases.get(ph);
		
		int c=phrase.length/2;
		int d=phrase[c].length;
		//while ((phrase[c][0]==-1)||(phrase[c][d-1]==-1)){
		//	c--;
		//}
		BufferedImage[] els=new BufferedImage[d];
		int totalLength=0;
		for (int i=0; i<d; i++){
			if (phrase[c][i]!=-1){
				Element ele=(Element)sg.songs[so].eleList.get(phrase[c][i]);
				els[i]=drawElement(ele, height, false);
				totalLength+=els[i].getWidth();
			}
		}
		BufferedImage ims=new BufferedImage(totalLength, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=ims.createGraphics();
		totalLength=0;
		for (int i=0; i<d; i++){
			if (phrase[c][i]!=-1){
				g.drawImage(els[i], totalLength, 0, null);
				totalLength+=els[i].getWidth();
				els[i]=null;
			}
		}
		els=null;
		g.setColor(Color.BLACK);
		if (decorate){
			g.setStroke(bs2);
			g.drawRect(0,0,totalLength-1, height-1);
		}
		g.dispose();
		return ims;
	}
	*/
	
	public BufferedImage drawPhraseEx (int so, int ph, int height, boolean decorate){
		
		Song song=sg.getSong(so);
		
		//int[][] phrase=(int[][])song.getPhrase(ph);
		
		
		Syllable phr=song.getPhrase(ph);
		
		Syllable sy=phr.getSyllable(phr.getNumSyllables()/2);
		
		Element[] ele=sy.getElements2().toArray(new Element[sy.getNumEles2()]);

		
		
		/*
		int c=phrase.length/2;
		int d=phrase[c].length;
		
		Element[] ele=new Element[d];

		for (int i=0; i<d; i++){
			if (phrase[c][i]!=-1){
				ele[i]=(Element)song.getElement(phrase[c][i]);
			}
		}
		*/
		BufferedImage ims=drawElements(ele, height, decorate);
		return ims;
	}
	
		
	
	/*
	public BufferedImage drawSong(int so, int height){
		
		int p=sg.songs[so].syllList.size();
		
		BufferedImage[] syls=new BufferedImage[p];
		int totalLength=0;
		for (int i=0; i<p; i++){
			syls[i]=drawSyllable(so, i, height, false);
			totalLength+=syls[i].getWidth();
		}
		BufferedImage ims=new BufferedImage(totalLength, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=ims.createGraphics();
		totalLength=0;
		for (int i=0; i<p; i++){
			g.drawImage(syls[i], totalLength, 0, null);
			totalLength+=syls[i].getWidth();
			syls[i]=null;
		}
		syls=null;
		g.setColor(Color.BLACK);
		g.drawRect(0,0,totalLength-1, height-1);
		g.dispose();
		return ims;
	}
	 */
	
	public BufferedImage drawSong(int so, int height, boolean decorate){
		Song song=sg.getSong(so);
		int p=song.getNumElements();
		Element[] eles=new Element[p];
		
		for (int i=0; i<p; i++){
			eles[i]=(Element)song.getElement(i);
		}
		
		BufferedImage bi=drawElements(eles, height, decorate);
		return bi;

	}
}
