package lusc.net.github;
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
import java.awt.image.BufferedImage;
import java.awt.image.*;
import java.util.*;

public class DisplaySketches {

	SongGroup sg;

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

	
	public DisplaySketches(SongGroup sg){
		this.sg=sg;
	}
	
	public BufferedImage drawElement (int element, int height, int inBuffer, int outBuffer, double dx, boolean decorate){
		int so=sg.lookUpEls[element][0];
		int el=sg.lookUpEls[element][1];
		
		Element ele=(Element)sg.songs[so].eleList.get(el);
		
		int le=(int)Math.round(ele.length*dx+inBuffer+outBuffer);
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
			double maxf=Math.log(ele.maxf+0.0);
			double minf=Math.log(100);
			ny=height/(maxf-minf);
			for (int i=0; i<ele.length; i++){
				x=(int)Math.round(inBuffer+(i*dx));
				for (int j=1; j<ele.signal[i].length; j+=2){
					y1=(int)Math.round(ny*(maxf-Math.log(ele.maxf-(ele.signal[i][j]*ele.dy))));
					y2=(int)Math.round(ny*(maxf-Math.log(ele.maxf-(ele.signal[i][j+1]*ele.dy))));
					g.drawLine(x,y1,x,y2);
				}
			}
		}
		else{
			double maxf=ele.maxf+0.0;
			ny=height/maxf;
			for (int i=0; i<ele.length; i++){
				x=(int)Math.round(inBuffer+(i*dx));
				for (int j=1; j<ele.signal[i].length; j+=2){
					y1=(int)Math.round(ny*(ele.signal[i][j]*ele.dy));
					y2=(int)Math.round(ny*(ele.signal[i][j+1]*ele.dy));
					g.drawLine(x,y1,x,y2);
				}
			}
		}
		if (contour){
			g.setColor(Color.BLACK);
			if (logContour){
				double maxf=Math.log(ele.maxf+0.0);
				double minf=Math.log(100);
				ny=height/(maxf-minf);
				for (int i=0; i<ele.signal.length-1; i++){
					j2=i+5;
					j3=i+6;
					x1=(int)Math.round(inBuffer+(i*dx));
					x2=(int)Math.round(inBuffer+((i+1)*dx));
					y1=(int)Math.round(ny*(maxf-Math.log(ele.measurements[j2][3])));
					y2=(int)Math.round(ny*(maxf-Math.log(ele.measurements[j3][3])));
					g.drawLine(x1,y1,x2,y2);
				}
			}
			else{
				double maxf=ele.maxf+0.0;
				ny=height/maxf;
				for (int j=0; j<ele.signal.length-1; j++){
					j2=j+5;
					j3=j+6;
					x1=(int)Math.round(inBuffer+(j*dx));
					x2=(int)Math.round(inBuffer+((j+1)*dx));
					y1=(int)Math.round(ny*(maxf-ele.measurements[j2][3]));
					y2=(int)Math.round(ny*(maxf-ele.measurements[j3][3]));
					g.drawLine(x1,y1,x2, y2);
				}
			}
		}
		g.dispose();
		return ims;
	}
	
	public BufferedImage drawElement (Element ele, int height, boolean decorate){
		
		int inBuffer=(int)Math.round(ele.tb/ele.timeStep);
		int outBuffer=(int)Math.round(ele.ta/ele.timeStep);
		if (inBuffer<0){inBuffer=5;}
		if (outBuffer<0){outBuffer=5;}
		
		
		int le=(int)Math.round(ele.length+inBuffer+outBuffer);
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
			double maxf=Math.log(ele.maxf+0.0);
			double minf=Math.log(100);
			ny=height/(maxf-minf);
			for (int i=0; i<ele.length; i++){
				x=(int)Math.round(inBuffer+i);
				for (int j=1; j<ele.signal[i].length; j+=2){
					y1=(int)Math.round(ny*(maxf-Math.log(ele.maxf-(ele.signal[i][j]*ele.dy))));
					y2=(int)Math.round(ny*(maxf-Math.log(ele.maxf-(ele.signal[i][j+1]*ele.dy))));
					g.drawLine(x,y1,x,y2);
				}
			}
		}
		else{
			double maxf=ele.maxf+0.0;
			ny=height/maxf;
			for (int i=0; i<ele.length; i++){
				x=(int)Math.round(inBuffer+i);
				for (int j=1; j<ele.signal[i].length; j+=2){
					y1=(int)Math.round(ny*(ele.signal[i][j]*ele.dy));
					y2=(int)Math.round(ny*(ele.signal[i][j+1]*ele.dy));
					g.drawLine(x,y1,x,y2);
				}
			}
		}
		if (contour){
			//g.setStroke(bs2);
			g.setColor(Color.BLACK);
			if (logContour){
				double maxf=Math.log(ele.maxf+0.0);
				double minf=Math.log(100);
				ny=height/(maxf-minf);
				for (int i=0; i<ele.signal.length-1; i++){
					j2=i+5;
					j3=i+6;
					x1=(int)Math.round(inBuffer+i);
					x2=(int)Math.round(inBuffer+i+1);
					y1=(int)Math.round(ny*(maxf-Math.log(ele.measurements[j2][3])));
					y2=(int)Math.round(ny*(maxf-Math.log(ele.measurements[j3][3])));
					g.drawLine(x1,y1,x2,y2);
				}
			}
			else{
				double maxf=ele.maxf+0.0;
				ny=height/maxf;
				for (int i=0; i<ele.signal.length-1; i++){
					j2=i+5;
					j3=i+6;
					x1=(int)Math.round(inBuffer+i);
					x2=(int)Math.round(inBuffer+i+1);
					y1=(int)Math.round(ny*(maxf-ele.measurements[j2][3]));
					y2=(int)Math.round(ny*(maxf-ele.measurements[j3][3]));
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
				if (begintime<0){begintime=ele[i].begintime;}
				int p=ele[i].begintime+ele[i].length;
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
					double maxf=Math.log(ele[el].maxf+0.0);
					double minf=Math.log(100);
					ny=height/(maxf-minf);
					
					for (int i=0; i<ele[el].length; i++){
						x=(int)Math.round(inBuffer+i+ele[el].begintime-begintime);
						for (int j=1; j<ele[el].signal[i].length; j+=2){
							y1=(int)Math.round(ny*(maxf-Math.log(ele[el].maxf-(ele[el].signal[i][j]*ele[el].dy))));
							y2=(int)Math.round(ny*(maxf-Math.log(ele[el].maxf-(ele[el].signal[i][j+1]*ele[el].dy))));
							g.drawLine(x,y1,x,y2);
						}
					}
				}
			}
		}
		else{
			for (int el=0; el<d; el++){
				if (ele[el]!=null){
					double maxf=ele[el].maxf+0.0;
					ny=height/maxf;
			
					for (int i=0; i<ele[el].length; i++){
						x=(int)Math.round(inBuffer+i+ele[el].begintime-begintime);
						for (int j=1; j<ele[el].signal[i].length; j+=2){
							y1=(int)Math.round(ny*(ele[el].signal[i][j]*ele[el].dy));
							y2=(int)Math.round(ny*(ele[el].signal[i][j+1]*ele[el].dy));
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
						double maxf=Math.log(ele[el].maxf+0.0);
						double minf=Math.log(100);
						ny=height/(maxf-minf);
				
						for (int i=0; i<ele[el].signal.length-1; i++){
							j2=i+5;
							j3=i+6;
							x1=(int)Math.round(inBuffer+i+ele[el].begintime-begintime);
							x2=(int)Math.round(inBuffer+i+1+ele[el].begintime-begintime);
							y1=(int)Math.round(ny*(maxf-Math.log(ele[el].measurements[j2][3])));
							y2=(int)Math.round(ny*(maxf-Math.log(ele[el].measurements[j3][3])));
							g.drawLine(x1,y1,x2,y2);
						}
					}
				}
			}
			else{
				for (int el=0; el<d; el++){
					if (ele[el]!=null){
						double maxf=ele[el].maxf+0.0;
						ny=height/maxf;
				
						for (int i=0; i<ele[el].signal.length-1; i++){
							j2=i+5;
							j3=i+6;
							x1=(int)Math.round(inBuffer+i+ele[el].begintime-begintime);
							x2=(int)Math.round(inBuffer+i+1+ele[el].begintime-begintime);
							y1=(int)Math.round(ny*(maxf-ele[el].measurements[j2][3]));
							y2=(int)Math.round(ny*(maxf-ele[el].measurements[j3][3]));
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
		int[] syl=(int[])sg.songs[so].syllList.get(syll);
		int d=syl.length;
		
		Element[] ele=new Element[d];
		int begintime=-1;
		int maxtime=0;
		for (int i=0; i<d; i++){
			if (syl[i]>0){
				ele[i]=(Element)sg.songs[so].eleList.get(syl[i]);
			}
		}
		BufferedImage ims=drawElements(ele, height, decorate);
		return ims;
	}
	
	public BufferedImage drawTransition(int transition, int height, boolean decorate){
		int so=sg.lookUpTrans[transition][0];
		int tr=sg.lookUpSyls[sg.lookUpTrans[transition][2]][1];
		
		
		BufferedImage[] syls=new BufferedImage[2];
		int totalLength=0;
		
		for (int i=0; i<2; i++){
	
			int[][] phrase=(int[][])sg.songs[so].phrases.get(tr+i);
	
			int c=phrase.length/2;
			int d=phrase[c].length;

			
			
			Element[] eles=new Element[d];
			boolean complete=false;
			while(complete=false){
				complete=true;
				for (int j=0; i<d; i++){
					if (phrase[c][j]!=-1){
						eles[j]=(Element)sg.songs[so].eleList.get(phrase[c][j]);
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

		BufferedImage[] syls=new BufferedImage[2];
		int totalLength=0;
		for (int i=0; i<2; i++){
			int p=sg.lookUpTrans2[transition][i];
			int so=sg.lookUpSyls[p][0];
			int tr=sg.lookUpSyls[p][1];
			
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
		
		int[][] phrase=(int[][])sg.songs[so].phrases.get(ph);
		
		int c=phrase.length/2;
		int d=phrase[c].length;
		
		Element[] ele=new Element[d];

		for (int i=0; i<d; i++){
			if (phrase[c][i]!=-1){
				ele[i]=(Element)sg.songs[so].eleList.get(phrase[c][i]);
			}
		}
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
		
		int p=sg.songs[so].eleList.size();
		Element[] eles=new Element[p];
		
		for (int i=0; i<p; i++){
			eles[i]=(Element)sg.songs[so].eleList.get(i);
		}
		
		BufferedImage bi=drawElements(eles, height, decorate);
		return bi;

	}
}
