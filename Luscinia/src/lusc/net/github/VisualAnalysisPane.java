package lusc.net.github;
//
//  VisualAnalysisPane.java
//  Luscinia
//
//  Created by Robert Lachlan on 7/3/07.
//  Copyright 2007 R.F.Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import javax.swing.*;
import java.awt.*;
import java.util.*;


public class VisualAnalysisPane extends JPanel {
		
	/**
	 * 
	 */
	private static final long serialVersionUID = -6188951329754304899L;
	JRadioButton syllable=new JRadioButton("Compare by syllable", true);
	JRadioButton song=new JRadioButton("Compare by song", true);
	JRadioButton random=new JRadioButton("Randomize order", true);
	JRadioButton allowVis=new JRadioButton("Compare using spectrograms", true);
	JRadioButton allowSound=new JRadioButton("Compare using sound playback", true);
	JCheckBox fitSignal=new JCheckBox("Fit signal in window", true);
	
	JRadioButton doVisualComparison=new JRadioButton("Carry out comparison", true);
	JRadioButton analyzeResults=new JRadioButton("Analyze saved results", false);
	
	String[] choiceScaleString={"2", "3", "4", "5", "10"};
	
	int[] choiceScaleInt={2, 3, 4, 5, 10};
	JComboBox choiceScale=new JComboBox(choiceScaleString);
	JLabel choiceScaleLabel=new JLabel("Number of levels of similarity: ");
	
	AnalysisChoose ac;
	
	public  VisualAnalysisPane (AnalysisChoose ac, boolean isSimple){
		this.ac=ac;
		this.setLayout(new BorderLayout());
		
		JPanel subScale=new JPanel();
		subScale.add(choiceScaleLabel);
		subScale.add(choiceScale);
		
		JPanel leftPanel=new JPanel(new GridLayout(0,1));
		leftPanel.setBorder(BorderFactory.createTitledBorder("Settings for visual analysis"));
		if (!isSimple){
			leftPanel.add(syllable);
			leftPanel.add(song);
		}
		leftPanel.add(random);
		leftPanel.add(allowVis);
		leftPanel.add(allowSound);
		leftPanel.add(fitSignal);
		leftPanel.add(subScale);
		
		
		this.add(leftPanel, BorderLayout.LINE_START);
		
		JPanel rightPanel=new JPanel(new GridLayout(0,1));
		rightPanel.setBorder(BorderFactory.createTitledBorder("Compare or Analyze"));
		rightPanel.add(doVisualComparison);
		rightPanel.add(analyzeResults);
		
		ButtonGroup bg=new ButtonGroup();
		bg.add(doVisualComparison);
		bg.add(analyzeResults);
		
		this.add(rightPanel, BorderLayout.LINE_END);
		
	}
	
	public void exportComplex(int schemeID, DataBaseController dbc, Defaults defaults){
		LinkedList<Object> outdata=dbc.getOutputVisual(schemeID);
		SaveDocument sd=new SaveDocument(this, defaults);
		boolean readyToWrite=sd.makeFile();
		if (readyToWrite){
			sd.writeString("user name");
			sd.writeString("song1 name");
			sd.writeString("song2 name");
			sd.writeString("song1 code");
			sd.writeString("song2 code");
			sd.writeString("syllable1");
			sd.writeString("syllable2");
			sd.writeString("score");
			sd.writeString("maximum score");
			sd.writeLine();
			for (int i=0; i<outdata.size(); i++){
				sd.writeString((String)outdata.get(i));
				i++;
				int[] dat=(int[])outdata.get(i);
				Song song1=dbc.loadSongFromDatabase(dat[0], 1);
				sd.writeString(song1.name);
				Song song2=dbc.loadSongFromDatabase(dat[1], 1);
				sd.writeString(song2.name);
				for (int j=0; j<dat.length; j++){
					sd.writeInt(dat[j]);
				}
				sd.writeLine();
			}
			sd.finishWriting();
		}
		ac.cleanUp();
	}
	
	public void exportSimple(int schemeID, DataBaseController dbc, Defaults defaults){
		LinkedList<Object> outdata=dbc.getOutputVisual(schemeID);
		SaveDocument sd=new SaveDocument(this, defaults);
		boolean readyToWrite=sd.makeFile();
		if (readyToWrite){
			sd.writeString("user name");
			sd.writeString("song1 name");
			sd.writeString("song2 name");
			sd.writeString("song1 code");
			sd.writeString("song2 code");
			sd.writeString("syllable1");
			sd.writeString("syllable2");
			sd.writeString("score");
			sd.writeString("maximum score");
			sd.writeLine();
			for (int i=0; i<outdata.size(); i++){
				sd.writeString((String)outdata.get(i));
				i++;
				int[] dat=(int[])outdata.get(i);
				Song song1=dbc.loadSongFromDatabase(dat[0], 1);
				sd.writeString(song1.name);
				if (dat[1]!=-1){
					Song song2=dbc.loadSongFromDatabase(dat[1], 1);
					sd.writeString(song2.name);
				}
				else{
					sd.writeString("no match");
					}
				for (int j=0; j<dat.length; j++){
					sd.writeInt(dat[j]);
				}
				sd.writeLine();
			}
			sd.finishWriting();
        }
		ac.cleanUp();
	}
	
}