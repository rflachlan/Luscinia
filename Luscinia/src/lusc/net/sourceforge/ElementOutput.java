package lusc.net.sourceforge;
//
//  ElementOutput.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/10/2005.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.util.*;

public class ElementOutput extends JPanel{

	String [] parameters={"Peak frequency", "Mean frequency", "Median frequency", "Fundamental frequency", "Peak frequency change", "Mean frequency change", "Median frequency change", 
	"Fundamental frequency change", "Wiener entropy", "Harmonicity", "Frequency bandwidth", "Amplitude", "Vibrato rate", "Vibrato amplitude",  "Vibrato asymmetry"};
	//String [] parameters={"Peak frequency", "Mean frequency", "Median frequency", "Fundamental frequency", "Peak frequency change", "Mean frequency change", "Median frequency change", 
	//		"Fundamental frequency change", "Wiener entropy", "Harmonicity", "Frequency bandwidth", "Amplitude", "Vibrato amplitude", "Vibrato rate", "Vibrato asymmetry", "Minimum Frequency", "Maximum frequency", "Signal location"};
	String [] details={"Individual name", "Song name", "Syllable Number", "Element Number"};
	String [] details2={"Time Step", "Frame Length", "Maximum frequency", "Windowing Method", "Dynamic Equalization", "Dynamic Range", "Echo Tail", "Echo Reduction", "dy"};
	LinkedList data=new LinkedList();
	int numParams=parameters.length;
	JRadioButton[] rb_set=new JRadioButton[numParams];
	Song[] songs;
	JLabel description=new JLabel("Use this option to save raw spectrogram measurements");
	JCheckBox allPoints=new JCheckBox("Use every time bin");
	JLabel numPointsLabel=new JLabel("Number of points per element: ");
	JFormattedTextField numPointsField=new JFormattedTextField();
	Defaults defaults;
	
	int numPoints=50;
	
	
	public ElementOutput (SongGroup sg, Defaults defaults){
		this.songs=sg.songs;
		this.defaults=defaults;
		this.setLayout(new BorderLayout());
		this.add(description, BorderLayout.NORTH);
		
		JPanel mainpanel=new JPanel(new GridLayout(0,1));
		//mainpanel.add(description);
		for (int i=0; i<numParams; i++){
			rb_set[i]=new JRadioButton(parameters[i]);
			rb_set[i].setSelected(false);
			mainpanel.add(rb_set[i]);
		}
		//JPanel startpanel=new JPanel();
		//JButton save=new JButton("save");
		//save.addActionListener(this);
		//startpanel.add(save);
		
		this.add(mainpanel, BorderLayout.CENTER);
		
		allPoints.setSelected(false);
		numPointsField.setValue(new Integer(numPoints));
		numPointsField.setColumns(10);
		
		JPanel optionpanel=new JPanel(new BorderLayout());
		optionpanel.add(allPoints, BorderLayout.NORTH);
		JPanel subpanel=new JPanel(new BorderLayout());
		subpanel.add(numPointsLabel, BorderLayout.WEST);
		subpanel.add(numPointsField, BorderLayout.CENTER);
		optionpanel.add(subpanel);
		this.add(optionpanel, BorderLayout.EAST);
		//this.add(startpanel, BorderLayout.SOUTH);
	}
	
	public void calculateElements(boolean suppress){
	
		if (!suppress){
			SaveDocument sd=new SaveDocument(this, defaults);
			boolean readyToWrite=sd.makeFile();
			if (readyToWrite){	
				sd.writeString(details[0]);
				sd.writeString(details[1]);
				sd.writeString(details[2]);
				sd.writeString(details[3]);
				if (rb_set[0].isSelected()){
					for (int i=0; i<details2.length; i++){
						sd.writeString(details2[i]);
					}
				}
				for (int i=1; i<numParams; i++){
					if (rb_set[i].isSelected()){
						sd.writeString(parameters[i]);
					}
				}
				sd.writeLine();
					
				for (int i=0; i<songs.length; i++){
					String indname=songs[i].individualName;
						
					for (int j=0; j<songs[i].eleList.size(); j++){
				
						Element ele=(Element)songs[i].eleList.get(j);
						int syll=-1;
						int aa=ele.length-1;
						for (int b=0; b<songs[i].syllList.size(); b++){
							int[] dat=(int[])songs[i].syllList.get(b);
							if ((ele.signal[0][0]>=dat[0])&&(ele.signal[aa][0]<=dat[1])){
								syll=b+1;
							}
						}
						for (int k=0; k<ele.length; k++){
							int kk=k+5;
							sd.writeString(indname);
							sd.writeString(songs[i].name);
							sd.writeInt(syll);
							sd.writeInt(j+1);
							if (rb_set[0].isSelected()){
								sd.writeDouble(ele.timeStep);
								sd.writeDouble(ele.frameLength);
								sd.writeInt(ele.maxf);
								sd.writeInt(ele.windowMethod);
								sd.writeDouble(ele.dynRange);
								sd.writeDouble(ele.dynEqual);
								sd.writeInt(ele.echoRange);
								sd.writeDouble(ele.echoComp);
								sd.writeDouble(ele.dy);
							}
							if (rb_set[1].isSelected()){sd.writeDouble(ele.signal[k][0]*ele.timeStep);}
							for (int a=0; a<15; a++){
								if (rb_set[(a+2)].isSelected()){
									sd.writeDouble(ele.measurements[kk][a]);
								}
							}
					
							if (rb_set[15].isSelected()){sd.writeDouble(ele.maxf-ele.signal[k][1]*ele.dy);}
							if (rb_set[16].isSelected()){sd.writeDouble(ele.maxf-ele.signal[k][ele.signal[k].length-1]*ele.dy);}
										
							if (rb_set[17].isSelected()){
								for (int m=1; m<ele.signal[k].length; m++){
									sd.writeDouble(ele.maxf-ele.signal[k][m]*ele.dy);
								}
							}
							sd.writeLine();
						}
						sd.finishWriting();
					}
				}
			}
		}
		
	}
	
	public void calculateElements(){
		
		SaveDocument sd=new SaveDocument(this, defaults);
		boolean readyToWrite=sd.makeFile();
		if (readyToWrite){	

			sd.writeString(details[0]);
			sd.writeString(details[1]);
			sd.writeString(details[2]);
			sd.writeString(details[3]);
			if (rb_set[0].isSelected()){
				for (int i=0; i<details2.length; i++){
					sd.writeString(details2[i]);
				}
			}
			sd.writeLine();
			
			for (int i=0; i<songs.length; i++){
				String indname=songs[i].individualName;
				for (int j=0; j<songs[i].eleList.size(); j++){
					Element ele=(Element)songs[i].eleList.get(j);
					int syll=-1;
					int aa=ele.length-1;
					for (int b=0; b<songs[i].syllList.size(); b++){
						int[] dat=(int[])songs[i].syllList.get(b);
						if ((ele.signal[0][0]>=dat[0])&&(ele.signal[aa][0]<=dat[1])){
							syll=b+1;
						}
					}
					sd.writeString(indname);
					sd.writeString(songs[i].name);
					sd.writeInt(syll);
					sd.writeInt(j+1);
					if (rb_set[0].isSelected()){
						sd.writeDouble(ele.timeStep);
						sd.writeDouble(ele.frameLength);
						sd.writeInt(ele.maxf);
						sd.writeInt(ele.windowMethod);
						sd.writeDouble(ele.dynRange);
						sd.writeDouble(ele.dynEqual);
						sd.writeInt(ele.echoRange);
						sd.writeDouble(ele.echoComp);
						sd.writeDouble(ele.dy);
					}
					sd.writeLine();
				}
			}
			
			
			for (int i=0; i<numParams; i++){
				if (rb_set[i].isSelected()){
					sd.writeSheet(parameters[i]);
					
					
					for (int ii=0; ii<songs.length; ii++){
						
						for (int j=0; j<songs[ii].eleList.size(); j++){
				
							Element ele=(Element)songs[ii].eleList.get(j);
							int p=numPoints;
							if (allPoints.isSelected()){
								p=ele.length;
							}
							double[] meas=ele.getMeasurements(i, p);
							for (int k=0; k<numPoints; k++){
								sd.writeDouble(meas[k]);
							}
							sd.writeLine();
						}
					}
				}
			}
						
			sd.finishWriting();
		}
		
	}
	
}
