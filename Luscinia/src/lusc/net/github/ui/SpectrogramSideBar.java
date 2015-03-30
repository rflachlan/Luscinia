package lusc.net.github.ui;
//
//  SpectrogramSideBar.java
//  Luscinia
//
//  Created by Robert Lachlan on 10/16/06.
//  Copyright 2006 __MyCompanyName__. All rights reserved.
//

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;

import lusc.net.github.Element;
import lusc.net.github.Song;
//import lusc.net.github.analysis.SongGroup;
import lusc.net.github.analysis.AnalysisGroup;
import lusc.net.github.ui.spectrogram.MainPanel;

public class SpectrogramSideBar extends JPanel implements MouseInputListener{

	//SongGroup sg;
	AnalysisGroup sg;
	Song[] songs;
	AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f);
	int[] lookUpSongs=new int[1];
	int imxsize=0; 
	int imysize=0;
	int ygap=100;
	int ysize=100;
	BufferedImage ims=new BufferedImage(300, 700, BufferedImage.TYPE_INT_ARGB);;
	Font bodyFont;
	
	boolean signalLogPlot=false;
	boolean fundLogPlot=true;
	
	JCheckBox contour=new JCheckBox("Show frequency contour", true);
	JCheckBox logContour=new JCheckBox("Show contour as log", true);
	
	int checked=-1;
	

	//public SpectrogramSideBar(SongGroup sg){
	public SpectrogramSideBar(AnalysisGroup sg){
		this.sg=sg;
		this.setLayout(new BorderLayout());
		this.addMouseListener(this);
		JPanel checkBoxPanel=new JPanel(new GridLayout(0,1));
		checkBoxPanel.add(contour);
		checkBoxPanel.add(logContour);
		this.add(checkBoxPanel, BorderLayout.NORTH);
		
		this.setPreferredSize(new Dimension(300,700));
		bodyFont  = new Font("Arial", Font.PLAIN, 10);
        if (bodyFont == null) {
            bodyFont = new Font("SansSerif", Font.PLAIN, 10);
        }
		
	}
	
	public void draw(int dataType, int[]contents){
		if (dataType==0){
			drawElements(contents, false);
		}
		else if (dataType==1){
			drawElements(contents, true);
		}
		else if (dataType==2){
			drawSyllables(contents);
		}
		else if (dataType==3){
			drawTransitions(contents);
		}
		else if (dataType==4){
			drawSongs(contents);
		}
	}
	
	public void drawElements(int[] els, boolean compressElements){
		int[][] elsToDraw=new int[els.length][2];
		if (compressElements){
			int[][] lookUp=sg.getLookUp(1);
			for (int i=0; i<els.length; i++){
				elsToDraw[i][0]=lookUp[els[i]][0];
				elsToDraw[i][1]=lookUp[els[i]][1];				
			}
		}
		else{
			int[][] lookUp=sg.getLookUp(0);
			for (int i=0; i<els.length; i++){
				elsToDraw[i][0]=lookUp[els[i]][0];
				elsToDraw[i][1]=lookUp[els[i]][1];
			}
		}
		drawElements(elsToDraw);
	}
	
	public void drawSyllables(int[] syls){
		int[][] sylsToDraw=new int[syls.length][2];
		int[][] lookUp=sg.getLookUp(2);
		for (int i=0; i<syls.length; i++){
			sylsToDraw[i][0]=lookUp[syls[i]][0];
			sylsToDraw[i][1]=lookUp[syls[i]][1];
		}
		drawSyllables(sylsToDraw);
		//ssb.drawPhraseEx(sylsToDraw);
	}
	
	public void drawTransitions(int[] trans){
		int[][] transToDraw=new int[trans.length][4];
		int[][] lookUpSyls=sg.getLookUp(2);
		int[][] lookUpTrans=sg.getLookUp(3);
		for (int i=0; i<trans.length; i++){
			transToDraw[i][0]=lookUpSyls[lookUpTrans[trans[i]][2]][0];
			transToDraw[i][1]=lookUpSyls[lookUpTrans[trans[i]][2]][1];
			transToDraw[i][2]=lookUpSyls[lookUpTrans[trans[i]][3]][0];
			transToDraw[i][3]=lookUpSyls[lookUpTrans[trans[i]][3]][1];

		}
		drawTransitions(transToDraw);
		//ssb.drawSyllables(transToDraw);
	}

	public BufferedImage drawElements(int[][] elements){
		this.songs=sg.getSongs();
		checked=-1;
		int imnum=elements.length;
		imysize=100;
		ysize=imysize*(imnum+1);
		imxsize=300;
		lookUpSongs=new int[imnum];
		
		ims=new BufferedImage(300, ysize, BufferedImage.TYPE_INT_ARGB);
		this.setPreferredSize(new Dimension(300, ysize));
		this.revalidate();
		
		Graphics2D g=ims.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0,0,300,ysize);
		
		g.setColor(Color.BLACK);
		float dx=(float)(imxsize/(sg.getScores(0).getMaxLength()+0.0));
		if (dx>1){dx=1;}
		int x,y1,y2, ys, x1, x2, j2, j3;
		double ny;
		for (int i=0; i<imnum; i++){
			if (contour.isSelected()){
				g.setColor(Color.GRAY);
			}
			else{
				g.setColor(Color.BLACK);
			}
			ys=50+i*imysize;
			g.drawRect(0, 50+i*imysize, imxsize, imysize);
			lookUpSongs[i]=elements[i][0];
			Element ele=(Element)songs[elements[i][0]].getElement(elements[i][1]);
			int[][] signal=ele.getSignal();
			int eleLength=ele.getLength();
			int mf=ele.getMaxF();
			double dy=ele.getDy();
			
			if (signalLogPlot){
				double maxf=Math.log(mf+0.0);
				double minf=Math.log(100);
				ny=imysize/(maxf-minf);
				for (int j=0; j<eleLength; j++){
					x=(int)Math.round(0+(j*dx));
					for (int k=1; k<signal[j].length; k+=2){
						y1=(int)Math.round(ny*(maxf-Math.log(mf-(signal[j][k]*dy))));
						y2=(int)Math.round(ny*(maxf-Math.log(mf-(signal[j][k+1]*dy))));
						g.drawLine(x,ys+y1,x,ys+y2);
					}
				}
			}
			else{
				double maxf=mf+0.0;
				ny=imysize/maxf;
				for (int j=0; j<eleLength; j++){
					x=(int)Math.round(0+(j*dx));
					for (int k=1; k<signal[j].length; k+=2){
						y1=(int)Math.round(ny*(signal[j][k]*dy));
						y2=(int)Math.round(ny*(signal[j][k+1]*dy));
						g.drawLine(x,ys+y1,x,ys+y2);
					}
				}
			}
			
			if (contour.isSelected()){
				double[][] measurements=ele.getMeasurements();
				g.setColor(Color.RED);
				if (logContour.isSelected()){
					double maxf=Math.log(mf+0.0);
					double minf=Math.log(100);
					ny=imysize/(maxf-minf);
					for (int j=0; j<signal.length-1; j++){
						j2=j+5;
						j3=j+6;
						x1=(int)Math.round(0+(j*dx));
						x2=(int)Math.round(0+((j+1)*dx));
						y1=(int)Math.round(ny*(maxf-Math.log(measurements[j2][3])));
						y2=(int)Math.round(ny*(maxf-Math.log(measurements[j3][3])));
						g.drawLine(x1,ys+y1,x2,ys+y2);
					}
				}
				else{
					double maxf=mf+0.0;
					ny=imysize/maxf;
					for (int j=0; j<signal.length-1; j++){
						j2=j+5;
						j3=j+6;
						x1=(int)Math.round(0+(j*dx));
						x2=(int)Math.round(0+((j+1)*dx));
						y1=(int)Math.round(ny*(maxf-measurements[j2][3]));
						y2=(int)Math.round(ny*(maxf-measurements[j3][3]));
						g.drawLine(x1,ys+y1,x2,ys+y2);
					}
				}
			}
		}
		repaint();
		g.dispose();
		return ims;
	}
	
	public BufferedImage drawSyllables(int[][] syllables){
		this.songs=sg.getSongs();
		checked=-1;
		int imnum=syllables.length;
		imysize=100;
		ysize=imysize*(imnum+1);
		imxsize=300;
		lookUpSongs=new int[imnum];
		ims=new BufferedImage(300, ysize, BufferedImage.TYPE_INT_ARGB);
		this.setPreferredSize(new Dimension(300, ysize));
		this.revalidate();
		
		Graphics2D g=ims.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0,ygap,300,ysize);
		g.setColor(Color.BLACK);
			
		float dx=(float)(imxsize/(sg.getScores(2).getMaxLength()+0.0));
		if (dx>1){dx=1;}
		int x,y1,y2, ys, j2, j3, x1, x2;
		double ny;
		for (int i=0; i<imnum; i++){
			if (contour.isSelected()){
				g.setColor(Color.GRAY);
			}
			else{
				g.setColor(Color.BLACK);
			}

			ys=ygap+i*imysize;
			g.drawRect(0, ygap+i*imysize, imxsize, imysize);
			lookUpSongs[i]=syllables[i][0];
			int[][] p=(int[][])songs[syllables[i][0]].getPhrase(syllables[i][1]);
			int a=p.length/2;
			//if (p[a][p[a].length-1]==-1){a--;}
			int startPosition=0;
			for (int j=0; j<p[a].length; j++){
				if (contour.isSelected()){
					g.setColor(Color.GRAY);
				}
				else{
					g.setColor(Color.BLACK);
				}
				if (p[a][j]!=-1){
					Element ele=(Element)songs[syllables[i][0]].getElement(p[a][j]);
					int[][] signal=ele.getSignal();
					int eleLength=ele.getLength();
					int mf=ele.getMaxF();
					double dy=ele.getDy();
					
					if (j==0){startPosition=ele.getBeginTime();}
					float s=(ele.getBeginTime()-startPosition)*dx;
					ny=imysize*dy/(mf+0.0);
					for (int k=0; k<eleLength; k++){
						x=(int)Math.round(0+(k*dx)+s);
						for (int kk=1; kk<signal[k].length; kk+=2){
							y1=(int)Math.round(ny*signal[k][kk]);
							y2=(int)Math.round(ny*signal[k][kk+1]);
							g.drawLine(x,ys+y1,x,ys+y2);
						}
					}
				
					if (contour.isSelected()){
						double[][] measurements=ele.getMeasurements();
						g.setColor(Color.RED);
						if (logContour.isSelected()){
							double maxf=Math.log(mf+0.0);
							double minf=Math.log(100);
							ny=imysize/(maxf-minf);
							for (int k=0; k<signal.length-1; k++){
								j2=k+5;
								j3=k+6;
								x1=(int)Math.round(0+(k*dx+s));
								x2=(int)Math.round(0+((k+1)*dx)+s);
								y1=(int)Math.round(ny*(maxf-Math.log(measurements[j2][3])));
								y2=(int)Math.round(ny*(maxf-Math.log(measurements[j3][3])));
								g.drawLine(x1,ys+y1,x2,ys+y2);
							}
						}
						else{
							double maxf=mf+0.0;
							ny=imysize/maxf;
							for (int k=0; k<signal.length-1; k++){
								j2=k+5;
								j3=k+6;
								x1=(int)Math.round(0+(k*dx)+s);
								x2=(int)Math.round(0+((k+1)*dx+s));
								y1=(int)Math.round(ny*(maxf-measurements[j2][3]));
								y2=(int)Math.round(ny*(maxf-measurements[j3][3]));
								g.drawLine(x1,ys+y1,x2,ys+y2);
							}
						}
					}
				}	
			}
		}
		repaint();
		g.dispose();
		return ims;
	}
	
	public BufferedImage drawTransitions2(int[][] transitions){
		this.songs=sg.getSongs();
		checked=-1;
		int imnum=transitions.length;
		imysize=100;
		ysize=imysize*(imnum+1);
		imxsize=300;
		lookUpSongs=new int[imnum];
		ims=new BufferedImage(300, ysize, BufferedImage.TYPE_INT_ARGB);
		this.setPreferredSize(new Dimension(300, ysize));
		this.revalidate();
		
		Graphics2D g=ims.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0,0,300,ysize);
		g.setColor(Color.BLACK);
			
		float dx=(float)(imxsize/(sg.getScores(3).getMaxLength()+0.0));
		if (dx>1){dx=1;}
		int x,y1,y2, ys;
		double ny;
		for (int i=0; i<imnum; i++){
			ys=50+i*imysize;
			g.drawRect(0, 50+i*imysize, imxsize, imysize);
			
			int songT=transitions[i][0];
			lookUpSongs[i]=songT;
			int p1=transitions[i][1];
			
			int p2=transitions[i][1]+1;
			if (p1==-2){
				p2=songs[songT].getNumPhrases()-1;
			}
			int[][]q1=new int[1][1];
			int[][]q2=new int[1][1];
			int a1=0;
			int a2=0;
			int startPosition=0;
			int endPosition=0;
			if (p1>=0){
				q1=(int[][])songs[songT].getPhrase(p1);
				a1=q1.length-1;
				if (q1[a1][q1[a1].length-1]==-1){a1--;}
				
				for (int j=0; j<q1[a1].length; j++){
					Element ele=(Element)songs[songT].getElement(q1[a1][j]);
					int[][] signal=ele.getSignal();
					int eleLength=ele.getLength();
					int mf=ele.getMaxF();
					double dy=ele.getDy();
					int beginTime=ele.getBeginTime();
					if (j==0){
						startPosition=beginTime;
					}
					float s=(beginTime-startPosition)*dx;
					ny=imysize*dy/(mf+0.0);
					for (int k=0; k<eleLength; k++){
						x=(int)Math.round(0+(k*dx)+s);
						for (int kk=1; kk<signal[k].length; kk+=2){
							y1=(int)Math.round(ny*signal[k][kk]);
							y2=(int)Math.round(ny*signal[k][kk+1]);
							g.drawLine(x,ys+y1,x,ys+y2);
						}
					}
					endPosition=(int)Math.round(beginTime-startPosition+eleLength+(ele.getTimeAfter()/ele.getTimeStep()));
				}
			}
			if (p2>=0){
				q2=(int[][])songs[songT].getPhrase(p2);
				a2=q2.length-1;
				if (q2[a2][q2[a2].length-1]==-1){a2--;}
				
				if (endPosition==0){
					Element ele1=(Element)songs[songT].getElement(q2[a2][0]);
				
					Element ele2=(Element)songs[songT].getElement(q2[a2][q2[a2].length-1]);
					
					endPosition=sg.getScores(3).getMaxLength()-(ele2.getBeginTime()+ele2.getLength()-ele1.getBeginTime());
					
				}
				
				for (int j=0; j<q2[a2].length; j++){
					Element ele=(Element)songs[songT].getElement(q2[a2][j]);
					int[][] signal=ele.getSignal();
					int eleLength=ele.getLength();
					int mf=ele.getMaxF();
					double dy=ele.getDy();
					int beginTime=ele.getBeginTime();
					if (j==0){
						startPosition=beginTime;
					}
					float s=(beginTime-startPosition+endPosition)*dx;
					ny=imysize*dy/(mf+0.0);
					for (int k=0; k<eleLength; k++){
						x=(int)Math.round(0+(k*dx)+s);
						for (int kk=1; kk<signal[k].length; kk+=2){
							y1=(int)Math.round(ny*signal[k][kk]);
							y2=(int)Math.round(ny*signal[k][kk+1]);
							g.drawLine(x,ys+y1,x,ys+y2);
						}
					}
				}
			}
			
			
			
		}
		repaint();
		g.dispose();
		return ims;
	}
	
	public BufferedImage drawTransitions(int[][] transitions){
		this.songs=sg.getSongs();
		checked=-1;
		int imnum=transitions.length;
		imysize=100;
		ysize=imysize*(imnum+1);
		imxsize=300;
		lookUpSongs=new int[imnum];
		ims=new BufferedImage(300, ysize, BufferedImage.TYPE_INT_ARGB);
		this.setPreferredSize(new Dimension(300, ysize));
		this.revalidate();
		
		Graphics2D g=ims.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0,0,300,ysize);
		g.setColor(Color.BLACK);
			
		float dx=(float)(imxsize/(3*sg.getScores(2).getMaxLength()+0.0));
		if (dx>1){dx=1;}
		int x,x1, x2, j2, j3, y1,y2, ys;
		double ny;
		for (int i=0; i<imnum; i++){
			ys=50+i*imysize;
			g.drawRect(0, 50+i*imysize, imxsize, imysize);
			
			//int tsong=transitions[i][0];
			//int maxP=sg.songs[tsong].phrases.size();
			//int startp=transitions[i][1];
			//if (startp>0){startp--;}
			//int endp=transitions[i][1]+1;
			//if (endp<maxP){endp++;}
			
			//int startp=transitions[i][1]-1;
			//int endp=transitions[i][1]+1;
			
			int tsong=0;
			int tphrase=0;
			
			
			
			float startPosition=0f;
			float endPosition=0f;
			for (int j=0; j<2; j++){
				
				if (j==0){
					tsong=transitions[i][0];
					tphrase=transitions[i][1];
				}
				else{
					tsong=transitions[i][2];
					tphrase=transitions[i][3];
				}
				
				lookUpSongs[i]=tsong;
				//int[][] p=(int[][])sg.songs[tsong].phrases.get(j);
				
				int[][] p=(int[][])songs[tsong].getPhrase(tphrase);
				
				int w=p.length-1;
				if (p[w][p[w].length-1]==-1){w--;}
				
				for (int k=0; k<p[w].length; k++){
					if (contour.isSelected()){
						g.setColor(Color.GRAY);
					}	
					else{
						g.setColor(Color.BLACK);
					}
					Element ele=(Element)songs[tsong].getElement(p[w][k]);
					int[][] signal=ele.getSignal();
					int eleLength=ele.getLength();
					int mf=ele.getMaxF();
					double dy=ele.getDy();
					int beginTime=ele.getBeginTime();
					
					startPosition=endPosition+(ele.getTimeBefore()*dx);
					float s=startPosition;
					if (s<5+endPosition){s=5f+endPosition;}
					ny=imysize*dy/(mf+0.0);
					for (int a=0; a<eleLength; a++){
						x=(int)Math.round(0+(a*dx)+s);
						endPosition=x;
						for (int b=1; b<signal[a].length; b+=2){
							y1=(int)Math.round(ny*signal[a][b]);
							y2=(int)Math.round(ny*signal[a][b+1]);
							g.drawLine(x,ys+y1,x,ys+y2);
						}
					}
				
					if (contour.isSelected()){
						double[][] measurements=ele.getMeasurements();
						g.setColor(Color.RED);
						if (logContour.isSelected()){
							double maxf=Math.log(mf+0.0);
							double minf=Math.log(100);
							ny=imysize/(maxf-minf);
							for (int a=0; a<signal.length-1; a++){
								j2=a+5;
								j3=a+6;
								x1=(int)Math.round(0+(a*dx+s));
								x2=(int)Math.round(0+((a+1)*dx)+s);
								y1=(int)Math.round(ny*(maxf-Math.log(measurements[j2][3])));
								y2=(int)Math.round(ny*(maxf-Math.log(measurements[j3][3])));
								g.drawLine(x1,ys+y1,x2,ys+y2);
							}
						}
						else{
							double maxf=mf+0.0;
							ny=imysize/maxf;
							for (int a=0; a<signal.length-1; a++){
								j2=a+5;
								j3=a+6;
								x1=(int)Math.round(0+(a*dx)+s);
								x2=(int)Math.round(0+((a+1)*dx+s));
								y1=(int)Math.round(ny*(maxf-measurements[j2][3]));
								y2=(int)Math.round(ny*(maxf-measurements[j3][3]));
								g.drawLine(x1,ys+y1,x2,ys+y2);
							}
						}
					}
				}	
			}
		}
		repaint();
		g.dispose();
		return ims;
	}
	
	public BufferedImage drawSongs(int[] songids){
		this.songs=sg.getSongs();
		checked=-1;
		int imnum=songids.length;
		imysize=100;
		ysize=imysize*(imnum+1);
		imxsize=300;
		lookUpSongs=new int[imnum];
		ims=new BufferedImage(300, ysize, BufferedImage.TYPE_INT_ARGB);
		this.setPreferredSize(new Dimension(300, ysize));
		this.revalidate();
		
		Graphics2D g=ims.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0,0,300,ysize);
		g.setColor(Color.BLACK);
			
		float dx=(float)(imxsize/(sg.getScores(4).getMaxLength()+0.0));
		if (dx>1){dx=1;}
		int x,y1,y2, ys;
		double ny;
		for (int i=0; i<imnum; i++){
			ys=50+i*imysize;
			g.drawRect(0, 50+i*imysize, imxsize, imysize);
			int startPosition=0;
			lookUpSongs[i]=songids[i];
			for (int j=0; j<songs[songids[i]].getNumElements(); j++){
				Element ele=(Element)songs[songids[i]].getElement(j);
				if (j==0){startPosition=ele.getBeginTime();}
				float s=(ele.getBeginTime()-startPosition)*dx;
				ny=imysize*ele.getDy()/(ele.getMaxF()+0.0);
				int[][] signal=ele.getSignal();
				for (int k=0; k<signal.length; k++){
					x=(int)Math.round(0+(k*dx)+s);
					for (int kk=1; kk<signal[k].length; kk+=2){
						y1=(int)Math.round(ny*signal[k][kk]);
						y2=(int)Math.round(ny*signal[k][kk+1]);
						g.drawLine(x,ys+y1,x,ys+y2);
					}
				}
			}
		}
		repaint();
		g.dispose();
		return ims;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);  //paint background
		int upperLeftX = g.getClipBounds().x;
        int upperLeftY = g.getClipBounds().y;
        int visibleWidth = g.getClipBounds().width;
        int visibleHeight = g.getClipBounds().height;
        
        //System.out.println(upperLeftX+" "+upperLeftY+" "+visibleWidth+" "+visibleHeight+" "+imf.getWidth()+" "+imf.getHeight());
        
        if (upperLeftX+visibleWidth>ims.getWidth()){visibleWidth=ims.getWidth()-upperLeftX;}
        if (upperLeftY+visibleHeight>ims.getHeight()){visibleHeight=ims.getHeight()-upperLeftY;}
       
        //System.out.println(upperLeftX+" "+upperLeftY+" "+visibleWidth+" "+visibleHeight+" "+ims.getWidth()+" "+ims.getHeight());
 
        BufferedImage q=ims.getSubimage(upperLeftX,  upperLeftY,  visibleWidth,  visibleHeight);
		
		
		g.drawImage(q, 0, upperLeftY, this);
		
		if (checked>=0){
			Graphics2D g2=(Graphics2D) g;
			g2.setComposite(ac);
			g2.setColor(Color.RED);
			g2.fillRect(0, upperLeftY+50+checked*imysize, imxsize, imysize);
		}
		
	}
	
	public void mouseClicked(MouseEvent e) { 
		
		int x=e.getX();
		int y=e.getY();
		
		checked=(int)Math.round((y-ygap)/imysize);
		if (checked>=lookUpSongs.length){checked=-1;}
		
		if (checked>=0){
			
			revalidate();
			repaint();
			Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
			setCursor(hourglassCursor);
			new SongOpener().execute();
			
			
		}
	}
	
	class SongOpener extends SwingWorker<String, Object> {
		
		public String doInBackground() {
			MainPanel mp=new MainPanel(sg.getDBC(), songs[lookUpSongs[checked]].getSongID(), sg.getDefaults());
			mp.startDrawing();
	        return new String("done");
		}

	       @Override
	   protected void done() {
	    	   Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
	    	   setCursor(normalCursor);
	       }

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

}
