package lusc.net.github.ui.compmethods;
//
//  ParameterPanel.java
//  Luscinia
//
//  Created by Robert Lachlan on 7/10/07.
//  Copyright 2007 R.F.Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import javax.swing.*;

import java.awt.*;
import java.util.*;

import lusc.net.github.Defaults;
import lusc.net.github.analysis.AnalysisGroup;
import lusc.net.github.analysis.ParameterAnalysis;
//import lusc.net.github.analysis.SongGroup;

public class ParameterPanel extends JPanel{


	/**
	 * 
	 */
	private static final long serialVersionUID = 4656703390547940972L;

	JRadioButton[][] buttonArray=new JRadioButton[8][17];
	
	String[] parameters={"Peak frequency", "Mean frequency", "Median frequency", "Fundamental frequency", "Peak freq. change", "Mean freq. change",
						"Median freq. change", "Fund. freq. change", "Harmonicity", "Wiener entropy", "Frequency bandwidth", "Amplitude", "Vibrato amplitude", "Vibrato rate", 
						"Time", "Gap between elements"};
						
	String[] headings={"Parameter", "Mean", "Max", "Min", "Time Max", "Time Min", "Start", "End", "Standard Deviation"};
	
	JLabel byRowLabel=new JLabel("Normalize per row: ");
	
	JRadioButton byRow=new JRadioButton();
	
	JLabel logFrequenciesLabel=new JLabel("Log-transform frequencies: ");
	
	JRadioButton logFrequencies=new JRadioButton();
	
	JLabel logTimeLabel=new JLabel("Log-transform time: ");
	
	JRadioButton logTime=new JRadioButton();
	
	
	//SongGroup sg;
	AnalysisGroup ag;
	LinkedList compScheme;
	Defaults defaults;
	JPanel cpane;
	int mode=0;
	
	//public  ParameterPanel (SongGroup sg){
		//this.sg=sg;
	public  ParameterPanel (AnalysisGroup ag, Defaults defaults){
		this.ag=ag;
		this.defaults=defaults;
		mode=1;
		parameterSetting();
		makeFrame();
	}
	
	//public ParameterPanel (SongGroup sg, LinkedList compScheme){
		//this.sg=sg;
	public ParameterPanel (AnalysisGroup ag, LinkedList compScheme, Defaults defaults){
		this.ag=ag;
		this.compScheme=compScheme;
		this.defaults=defaults;
		mode=0;
		parameterSetting();
		makeFrame();
	}
	
	public void parameterSetting(){
	
		JPanel buttonPanel=new JPanel(new GridLayout(17, 9));
		
		boolean[][] defaultArray=defaults.getParameterPanelArray();
		
		for (int i=0; i<9; i++){
			JLabel label=new JLabel(headings[i]);
			buttonPanel.add(label);
		}
		
		for (int i=0; i<14; i++){
		
			JLabel label=new JLabel(parameters[i]);
			buttonPanel.add(label);
			
			for (int j=0; j<8; j++){
				buttonArray[j][i]=new JRadioButton(" ");
				buttonArray[j][i].setSelected(defaultArray[j][i]);
				buttonPanel.add(buttonArray[j][i]);
			}
		}	
		
		for (int i=0; i<2; i++){
			JLabel label=new JLabel(parameters[i+14]);
			buttonPanel.add(label);
			
			buttonArray[0][i+14]=new JRadioButton(" ");
			buttonArray[0][i+14].setSelected(defaultArray[0][i+14]);
			buttonPanel.add(buttonArray[0][i+14]);
			
			for (int j=0; j<7; j++){
				JLabel blank=new JLabel(" ");
				buttonPanel.add(blank);
			}
		}
		
	
		buttonPanel.setBorder(BorderFactory.createTitledBorder("Parameters to compare"));
		
		cpane=new JPanel();
		cpane.setLayout(new BorderLayout());
		cpane.add(buttonPanel, BorderLayout.LINE_START);
		
		byRow.setSelected(defaults.getBooleanProperty("parpanbyrow", false));
		logFrequencies.setSelected(defaults.getBooleanProperty("parpanlogf", false));
		logTime.setSelected(defaults.getBooleanProperty("parpanlogt", false));
		
		JPanel optionsPanel1=new JPanel(new BorderLayout());
		optionsPanel1.add(byRowLabel, BorderLayout.LINE_START);
		optionsPanel1.add(byRow, BorderLayout.CENTER);
		
		JPanel optionsPanel2=new JPanel(new BorderLayout());
		optionsPanel2.add(logFrequenciesLabel, BorderLayout.LINE_START);
		optionsPanel2.add(logFrequencies, BorderLayout.CENTER);
		
		JPanel optionsPanel3=new JPanel(new BorderLayout());
		optionsPanel3.add(logTimeLabel, BorderLayout.LINE_START);
		optionsPanel3.add(logTime, BorderLayout.CENTER);
		
		JPanel optionsPanel=new JPanel(new GridLayout(0,1));
		optionsPanel.add(optionsPanel1);
		optionsPanel.add(optionsPanel2);
		optionsPanel.add(optionsPanel3);
		
		cpane.add(optionsPanel, BorderLayout.SOUTH);
	}
		
	public void makeFrame(){
		JPanel contentpane=new JPanel();
		contentpane.setLayout(new BorderLayout());
		contentpane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		contentpane.add(cpane, BorderLayout.LINE_START);
		
		JPanel bottomPane=new JPanel(new BorderLayout());
		contentpane.add(bottomPane, BorderLayout.SOUTH);
		
		this.add(contentpane);
	}
	
	public void startAnalysis(){
	
				
		boolean[][] parameterMatrix=new boolean[8][17];
		
		for (int i=0; i<8; i++){
			for (int j=0; j<17; j++){
				if ((buttonArray[i][j]!=null)&&(buttonArray[i][j].isSelected())){
					parameterMatrix[i][j]=true;
				}
				else{
					parameterMatrix[i][j]=false;
				}
				System.out.print(parameterMatrix[i][j]+" ");
			}
			System.out.println();
		}
		
		defaults.setParameterPanelArray(parameterMatrix);
		defaults.setBooleanProperty("parpanbyrow", byRow.isSelected());
		defaults.setBooleanProperty("parpanlogf", logFrequencies.isSelected());
		defaults.setBooleanProperty("parpanlogt", logTime.isSelected());
		
		ParameterAnalysis pa=new ParameterAnalysis(ag.getSongs(), ag.getLengths(0));
		pa.setMatrix(parameterMatrix);
		pa.setByRow(byRow.isSelected());
		pa.setLogFrequencies(logFrequencies.isSelected());
		pa.setLogTime(logTime.isSelected());
		pa.calculateSummaries();
		ag.setScores(0, pa.calculateDistancesFromParameters());
		ag.compressSyllables();
	}
	
}
