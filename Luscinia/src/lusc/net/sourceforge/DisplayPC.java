package lusc.net.sourceforge;
//
//  DisplayPC.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/10/2005.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.


import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

public class DisplayPC  extends DisplayPane implements  ActionListener{
	
	private static String labs[] = {"None","Population", "Position", "K-Medoid Cluster", "Syntax Cluster", "SNN Cluster"};
	
	Defaults defaults;
	SongGroup sg;

	Dimension dim=new Dimension(800, 800);
	int[][]location=null;
	
	double [][] data;
	int n=0;
	int width, height, dataType, numdims;
	//boolean enabled=false;
	boolean connected=false;
	boolean linked=true;
	boolean gridlines=false;
	int dimensionX=0;
	int dimensionY=1;
	
	int cluster=2;
	
	private static String DIM1 = "dim1";
	private static String DIM2 = "dim2";
	private static String LABEL="label";
	private static String CLUSTER="cluster";
	private static String CLEAR="clear";
	private static String CONNECT="connect";
	private static String GRIDS="grids";
	private static String SALL="sall";
	private static String LINK="link";
	
	int labelType=0;

	PCPane pcp;
	JButton clearButton, flipXButton, flipYButton, selectAllCat; 
	
	boolean flipX=false;
	boolean flipY=false;
	
	MultiDimensionalScaling mds;
	KMedoids km;
	SNNDensity snn;
	EntropyAnalysis ent;
	Font font=new Font("Sans-Serif", Font.PLAIN, 9);

	
	public DisplayPC(MultiDimensionalScaling mds, SongGroup sg, KMedoids km, EntropyAnalysis ent, SNNDensity snn, int dataType, int width, int height, Defaults defaults){
		
		this.mds=mds;
		this.sg=sg;
		this.km=km;
		this.ent=ent;
		this.snn=snn;
		this.dataType=dataType;
		this.width=width;
		this.height=height;
		this.defaults=defaults;
		
		n=mds.n;
		numdims=mds.npcs;
		data=mds.configuration;
		
		//normalize();
		
		dim=new Dimension(width, height);
		this.setPreferredSize(dim);
		this.setLayout(new BorderLayout());
		
		JPanel messagePanel=new JPanel();
		messagePanel.setLayout(new GridLayout(0,1));
		messagePanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		JLabel label1=new JLabel("Variation explained by "+numdims+" dimensions: "+(100*mds.percentExplained[mds.percentExplained.length-1])+"%");
		label1.setFont(font);
		messagePanel.add(label1);
		JLabel label2=new JLabel((100*mds.percentExplained[0])+"% explained by PC1, "+(100*(mds.percentExplained[1]-mds.percentExplained[0]))+"% explained by PC2");
		label2.setFont(font);
		messagePanel.add(label2);
		int q1=-1;
		for (int i=0; i<mds.percentExplained.length; i++){
			if (mds.percentExplained[i]>0.8){
				q1=i+1;
				i=mds.percentExplained.length;
			}
		}
		if (q1>=0){
			JLabel label3=new JLabel(q1+" PCs required to explain 80% of variation");
			label3.setFont(font);
			messagePanel.add(label3);
		}
		int q2=-1;
		for (int i=0; i<mds.percentExplained.length; i++){
			if (mds.percentExplained[i]>0.9){
				q2=i+1;
				i=mds.percentExplained.length;
			}
		}
		if (q2>=0){
			JLabel label4=new JLabel(q2+" PCs required to explain 90% of variation");
			label4.setFont(font);
			messagePanel.add(label4);
		}
									 
		JLabel label5=new JLabel("Kruskal NMDS stress: "+mds.stressFactor);
		label5.setFont(font);
		messagePanel.add(label5);
		
		JPanel northPanel=makeDropDown();
		
		JPanel newTopPanel=new JPanel(new BorderLayout());
		newTopPanel.add(messagePanel, BorderLayout.CENTER);
		newTopPanel.add(northPanel, BorderLayout.SOUTH);
		
		this.add(newTopPanel, BorderLayout.NORTH);
		
		pcp=new PCPane(width, height-150, this, defaults);
		pcp.paintPanel(sg, data, dimensionX, dimensionY, labelType, dataType, cluster, connected, gridlines, flipX, flipY, linked);
		this.add(pcp, BorderLayout.CENTER);
		
	}

	

	
	public JPanel makeDropDown(){
		
		int maxks=0;
		
		if (km!=null){
			maxks=km.overallAssignments.length;
		}
		if (ent!=null){
			if (ent.overallAssignments.length>maxks){
				maxks=ent.overallAssignments.length;
			}
		}
		JLabel clusterLabel=new JLabel("Number of clusters: ");
		clusterLabel.setFont(font);
		
		
		JLabel pointLabel=new JLabel("Labels for points: ");
		pointLabel.setFont(font);
		int p=3;
		if (km!=null){p++;}
		if (ent!=null){p++;}
		if (snn!=null){p++;}
		
		String[] labelTypes=new String[p];
		
		labelTypes[0]=labs[0];
		labelTypes[1]=labs[1];
		labelTypes[2]=labs[2];
		p=3;
		if (km!=null){
			labelTypes[p]=labs[3];
			p++;
		}
		if (ent!=null){
			labelTypes[p]=labs[4];
			p++;
		}
		if (snn!=null){
			labelTypes[p]=labs[5];
			p++;
		}
					
	
		JComboBox labelSelector=new JComboBox(labelTypes);
		labelSelector.setFont(font);
		labelSelector.setSelectedIndex(0);
		labelSelector.addActionListener(this);
		labelSelector.setActionCommand(LABEL);
		
		String[] labels=new String[numdims];		
		for (int i=0; i<numdims; i++){
			Integer j=new Integer(i+1);
			labels[i]=j.toString();
		}
		
		clearButton=new JButton("Clear selection");
		clearButton.setEnabled(false);
		clearButton.addActionListener(this);
		clearButton.setActionCommand(CLEAR);
		clearButton.setFont(font);
		
		selectAllCat=new JButton("Select all within class");
		selectAllCat.setEnabled(false);
		selectAllCat.addActionListener(this);
		selectAllCat.setActionCommand(SALL);
		selectAllCat.setFont(font);
				
		
		flipXButton=new JButton("Flip X-axis");
		flipYButton=new JButton("Flip Y-axis");
		flipYButton.addActionListener(this);
		flipXButton.addActionListener(this);
		flipXButton.setFont(font);
		flipYButton.setFont(font);
		
		JComboBox dimSelector1=new JComboBox(labels);
		dimSelector1.setSelectedIndex(0);
		dimSelector1.addActionListener(this);
		dimSelector1.setActionCommand(DIM1);
		JComboBox dimSelector2=new JComboBox(labels);
		dimSelector2.setSelectedIndex(1);
		dimSelector2.addActionListener(this);
		dimSelector2.setActionCommand(DIM2);
		JLabel labelX=new JLabel("X-dimension:");
		JLabel labelY=new JLabel("Y-dimension:");
		labelX.setFont(font);
		labelY.setFont(font);
		dimSelector1.setFont(font);
		dimSelector2.setFont(font);
		
		
		JCheckBox connectors=new JCheckBox("Connect within songs");
		connectors.setFont(font);
		JCheckBox grids=new JCheckBox("Show grid lines");
		grids.setFont(font);
		if (dataType==4){connectors.setEnabled(false);}
		connectors.setSelected(false);
		connectors.addActionListener(this);
		connectors.setActionCommand(CONNECT);
		connected=false;
		
		JCheckBox link=new JCheckBox("Link within songs");
		link.setFont(font);
		if (dataType==4){link.setEnabled(false);}
		link.setSelected(true);
		link.addActionListener(this);
		link.setActionCommand(LINK);
		linked=true;
		
		grids.setSelected(true);
		grids.addActionListener(this);
		grids.setActionCommand(GRIDS);
		gridlines=true;
		
		JPanel xpane=new JPanel(new BorderLayout());
		xpane.add(labelX, BorderLayout.WEST);
		xpane.add(dimSelector1, BorderLayout.CENTER);
		xpane.add(flipXButton, BorderLayout.EAST);
		
		JPanel ypane=new JPanel(new BorderLayout());
		ypane.add(labelY, BorderLayout.WEST);
		ypane.add(dimSelector2, BorderLayout.CENTER);
		ypane.add(flipYButton, BorderLayout.EAST);
		
		JPanel WPane=new JPanel(new GridLayout(0,1));
		WPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		WPane.add(xpane);
		WPane.add(ypane);
		
		JPanel tpane=new JPanel(new BorderLayout());
		tpane.add(pointLabel, BorderLayout.CENTER);
		tpane.add(labelSelector, BorderLayout.EAST);
		
		JPanel upane=new JPanel(new BorderLayout());
		if (maxks>0){
			upane.add(clusterLabel, BorderLayout.CENTER);
			String[] clusterN=new String[maxks];
			for (int i=0; i<maxks; i++){
				clusterN[i]=Integer.toString(i+2);
			}
			JComboBox clusterSelector=new JComboBox(clusterN);
			clusterSelector.setSelectedIndex(0);
			clusterSelector.addActionListener(this);
			clusterSelector.setActionCommand(CLUSTER);
			upane.add(clusterSelector, BorderLayout.EAST);
		}
		
		JPanel CPane=new JPanel(new GridLayout(0,1));
		CPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		CPane.add(tpane);
		CPane.add(upane);
		
		JPanel EPane=new JPanel(new GridLayout(0,1));
		EPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		EPane.add(clearButton);
		EPane.add(selectAllCat);
		
		JPanel FPane=new JPanel(new GridLayout(0,1));
		EPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		FPane.add(connectors);
		FPane.add(grids);
		FPane.add(link);
				
		JPanel rpane=new JPanel(new GridLayout(1,0));
		rpane.setBorder(BorderFactory.createEmptyBorder(0,0,0,100));
		rpane.add(WPane);
		rpane.add(CPane);
		rpane.add(EPane);
		rpane.add(FPane);
		
		return rpane;
	
	}
	
	/*public void normalize(){
		int npcs=data[0].length;
		
		double[][] maxmin=new double[npcs][2];
		for (int i=0; i<npcs; i++){
			maxmin[i][0]=10000000;
			maxmin[i][1]=-10000000;
		}
		for (int i=0; i<n; i++){
			for (int j=0; j<npcs; j++){
				if (data[i][j]<maxmin[j][0]){maxmin[j][0]=data[i][j];}
				if (data[i][j]>maxmin[j][1]){maxmin[j][1]=data[i][j];}
			}
		}
		
		double min=10000000;
		double max=-10000000;
		
		for (int i=0; i<n; i++){
			for (int j=0; j<npcs; j++){
				data[i][j]-=0.5f*(maxmin[j][1]+maxmin[j][0]);
				if (data[i][j]<min){min=data[i][j];}
				if (data[i][j]>max){max=data[i][j];}
			}
		}
		
		for (int i=0; i<n; i++){
			for (int j=0; j<npcs; j++){
				data[i][j]/=(max-min);
				data[i][j]+=0.5f;
			}
		}
		
	}
	*/
	/*
	public void normalizeX(){
		int npcs=data[0].length;

		for (int i=0; i<n; i++){
			for (int j=0; j<npcs; j++){
				data[i][j]+=0.5f;
			}
		}
		
	}
	*/
	
	public void pointsClicked(int[] wrap){
		sg.draw(dataType, wrap);
		clearButton.setEnabled(true);
		
	}
	
	
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource()==clearButton){
			pcp.selectedPoint=false;
			pcp.selectedArea=false;
			pcp.selP=null;
			
			clearButton.setEnabled(false);
		}
		else if (e.getSource()==flipXButton){
			flipX=!flipX;
		}
		else if (e.getSource()==flipYButton){
			flipY=!flipY;
		}
		else{
			String command = e.getActionCommand();
			if (GRIDS.equals(command)){
				if (gridlines){gridlines=false;}
				else{gridlines=true;}
			}
			else if (SALL.equals(command)){
				if (pcp.selP!=null){
					for (int i=0; i<pcp.selP.length; i++){
						int a=pcp.selP[i];
						
						if (labelType==3){
							int b=km.overallAssignments[cluster-2][a];
							int c=0;
							for (int j=0; j<km.overallAssignments[cluster-2].length; j++){
								if (km.overallAssignments[cluster-2][j]==b){
									c++;
								}
							}
							int[] wr=new int[c];
							c=0;
							for (int j=0; j<km.overallAssignments[cluster-2].length; j++){
								if (km.overallAssignments[cluster-2][j]==b){
									wr[c]=j;
									c++;
								}
							}
							sg.draw(dataType, wr);
						}
						else if (labelType==4){
							int b=ent.overallAssignments[cluster-2][a];
							int c=0;
							for (int j=0; j<km.overallAssignments[cluster-2].length; j++){
								if (ent.overallAssignments[cluster-2][j]==b){
									c++;
								}
							}
							int[] wr=new int[c];
							c=0;
							for (int j=0; j<km.overallAssignments[cluster-2].length; j++){
								if (ent.overallAssignments[cluster-2][j]==b){
									wr[c]=j;
									c++;
								}
							}
							sg.draw(dataType, wr);
						}	
						else if (labelType==5){
							int b=snn.DBSCANclusters[a];
							int c=0;
							for (int j=0; j<snn.DBSCANclusters.length; j++){
								if (snn.DBSCANclusters[j]==b){
									c++;
								}
							}
							int[] wr=new int[c];
							c=0;
							for (int j=0; j<snn.DBSCANclusters.length; j++){
								if (snn.DBSCANclusters[j]==b){
									wr[c]=j;
									c++;
								}
							}
							sg.draw(dataType, wr);
						}	
					}
					
				}
			}
			else if (LINK.equals(command)){
				if (linked){linked=false;}
				else{linked=true;}
			}
			else if (CONNECT.equals(command)){
				if (connected){connected=false;}
				else{connected=true;}
			}
			else{
				JComboBox cb = (JComboBox)e.getSource();
				int p=cb.getSelectedIndex();
				String s=(String)cb.getSelectedItem();
				if (DIM1.equals(command)) {
					dimensionX=p;
					pcp.selectedPoint=false;
					pcp.selectedArea=false;
				}
				else if (DIM2.equals(command)) {
					dimensionY=p;
					pcp.selectedPoint=false;
					pcp.selectedArea=false;
				}
				else if (LABEL.equals(command)){
				
					for (int i=0; i<labs.length; i++){
						if (labs[i]==s){labelType=i;}
					}
					if ((labelType==3)||(labelType==4)||(labelType==5)){
						selectAllCat.setEnabled(true);
					}
					else{
						selectAllCat.setEnabled(false);
					}
				}
				else if (CLUSTER.equals(command)){
					cluster=p+2;
				}
			}
		}
		int tcluster=cluster;
		if (labelType==5){
			tcluster=snn.numClusts+1;
		}
		
		pcp.paintPanel(sg, data, dimensionX, dimensionY, labelType, dataType, tcluster, connected, gridlines, flipX, flipY, linked);
		this.revalidate();
	}

	public void export(){
		SaveDocument sd=new SaveDocument(this, defaults);
		boolean readyToWrite=sd.makeFile();
		if (readyToWrite){
			sd.writeString("Population");
			sd.writeString("Individual");
			sd.writeString("Song");
			for (int i=0; i<data[0].length; i++){
				sd.writeString("pc_"+(i+1)+"_value");
			}
			if (km!=null){
				for (int i=0; i<km.overallAssignments.length; i++){
					sd.writeString("# km_cats_"+(i+2)+"_value");
				}
			}
			if (ent!=null){
				for (int i=0; i<ent.overallAssignments.length; i++){
					sd.writeString("# end_cats_"+(i+2)+"_value");
				}
			}
			sd.writeLine();
			for (int i=0; i<data.length; i++){
				int[] g=sg.getId(dataType, i);
				String sn2=sg.songs[g[0]].population;
				sd.writeString(sn2);
				String sn1=(String)sg.songs[g[0]].individualName;
				sd.writeString(sn1);
				if (dataType<3){
					sd.writeString(sg.songs[g[0]].name+","+(g[1]+1));
				}
				else{
					sd.writeString(sg.songs[g[0]].name);
				}
				for (int j=0; j<data[i].length; j++){
					sd.writeDouble(data[i][j]);
				}
				
				if (km!=null){
					for (int j=0; j<km.overallAssignments.length; j++){
						sd.writeInt(km.overallAssignments[j][i]+1);
					}
				}
				
				if (ent!=null){
					for (int j=0; j<ent.overallAssignments.length; j++){
						sd.writeInt(ent.overallAssignments[j][i]+1);
					}
				}
				
				sd.writeLine();
			}
			
			sd.writeSheet("Ordination stats");
			if (mds!=null){
				double sf[]=mds.percentExplained;
				double sf2[]=mds.eigenValues;
				sd.writeString("PC");
				sd.writeString("Cumulative % explained");
				sd.writeString("Eigenvalue");
				sd.writeLine();
				for (int i=0; i<sf.length; i++){
					sd.writeInt(i+1);
					sd.writeDouble(sf[i]*100);
					sd.writeDouble(sf2[i]);
					sd.writeLine();
				}
			}
			if(mds!=null){
				sd.writeString("NMDS stress (Kruskal)");
				sd.writeDouble(mds.stressFactor);
				sd.writeLine();
			}
			
			sd.finishWriting();
		}
	}
	
	public void saveImage(){
		SaveImage si=new SaveImage(pcp.imf, this, defaults);
		//pcp.saveImage();

	}
	
	
	public BufferedImage resizeImage(double ratio){
		
		BufferedImage imf=pcp.resizeImage(ratio);
		pcp.resizeImage(1.0f);
		return imf;
	}
	
	
	
	
}
