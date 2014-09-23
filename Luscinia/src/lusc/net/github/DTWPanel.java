package lusc.net.github;
//
//  DTWPanel.java
//  Luscinia
//
//  Created by Robert Lachlan on 6/30/07.
//  Copyright 2007 R.F.Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import javax.swing.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.*;
import java.text.*;
import java.util.*;


public class DTWPanel extends JPanel implements ActionListener, PropertyChangeListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2798852000648684253L;
	static String EXPORT="export";
	static String SAVE_IMAGE="save image";
	
	int progress=0;
	int nextProgressTarget=10;
	
	JRadioButton weightByAmp=new JRadioButton("Weight by relative amplitude", true);
	//JRadioButton normalizePerElement=new JRadioButton("Normalize each element", false);
	JRadioButton logFrequencies=new JRadioButton("Log transform frequencies", true);
	//JRadioButton compareWholeSyllables=new JRadioButton("Stitch and compare syllables", true);
	
	String[] stitchOptions={"Individual elements", "Stitch elements", "Both"};
	JComboBox stitch=new JComboBox(stitchOptions);
	
	JPanel cpane;
	JTabbedPane tabPane=new JTabbedPane();
	
	String[] parameters={"Time", "Relative position", "Peak frequency", "Mean frequency", "Median frequency", "Fundamental frequency", "Peak frequency change", "Mean frequency change",
						"Median frequency change", "Fundamental frequency change", "Harmonicity", "Wiener entropy", "Frequency bandwidth", "Amplitude", "Vibrato rate", "Vibrato amplitude", 
						"Gap between elements", "PF norm", "MF norm", "MeF norm", "FF norm", "Upper cut-off", "Lower cut-off"};

	JFormattedTextField[] parametersTF;
	
	JFormattedTextField compressionFactor, minPoints, sdRatio, offsetRemoval, stitchPunishment, alignmentCost, addSyllReps;
		
	boolean started=false;
	boolean textOut=false;
	boolean enableSyllableStitching=true;
		
	int pcsUsed=2;
	
	Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
	Dimension dim=new Dimension(d.width-400, d.height);
	
	int mode=0;
	
	JLabel progressLabel=new JLabel("Waiting to start. Click 'Next step' to start comparison");
	LinkedList compScheme;
	DataBaseController dbc;
	SongGroup sg;
	
	NumberFormat num;
	

	public  DTWPanel (DataBaseController dbc, SongGroup sg, boolean enableSyllableStitching){
		this.dbc=dbc;
		this.sg=sg;
		this.enableSyllableStitching=enableSyllableStitching;
		num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(10);	
		mode=1;
		parameterSetting();
		makeFrame();
	}
	
	public DTWPanel (DataBaseController dbc, SongGroup sg, LinkedList compScheme){
		this.dbc=dbc;
		this.sg=sg;
		this.compScheme=compScheme;
		num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(10);
		mode=0;
		parameterSetting();
		makeFrame();
	}
	
	public void parameterSetting(){
		JPanel weightingPanel=new JPanel();
		weightingPanel.setBorder(BorderFactory.createTitledBorder("Weightings for dynamic-time-warping"));
		weightingPanel.setLayout(new GridLayout(0,2));
		int buttonSet=parameters.length;
		parametersTF=new JFormattedTextField[buttonSet];
		
		for (int i=0; i<buttonSet; i++){
		
			JLabel parameterLabel=new JLabel(parameters[i]);
			weightingPanel.add(parameterLabel);
			parametersTF[i]=new JFormattedTextField();
			parametersTF[i].setValue(new Double(sg.parameterValues[i]));
			parametersTF[i].setColumns(10);
			parametersTF[i].addPropertyChangeListener("value", this);
			weightingPanel.add(parametersTF[i]);
		}
	
		JPanel optionsPanel=new JPanel();
		optionsPanel.setBorder(BorderFactory.createTitledBorder("Dynamic-time-warping options"));
		optionsPanel.setLayout(new GridLayout(0,2));
		
		JLabel compressionLabel=new JLabel("Compression factor: ");
		optionsPanel.add(compressionLabel);
		compressionFactor=new JFormattedTextField();
		compressionFactor.setValue(new Double(sg.mainReductionFactor));
		compressionFactor.setColumns(10);
		compressionFactor.addPropertyChangeListener("value", this);
		optionsPanel.add(compressionFactor);
		
		JLabel minPointsLabel=new JLabel("Minimum element length: ");
		optionsPanel.add(minPointsLabel);
		minPoints=new JFormattedTextField();
		minPoints.setValue(new Integer(sg.minPoints));
		minPoints.setColumns(10);
		minPoints.addPropertyChangeListener("value", this);
		optionsPanel.add(minPoints);
		
		JLabel ratioLabel=new JLabel("SD ratio: ");
		optionsPanel.add(ratioLabel);
		sdRatio=new JFormattedTextField();
		sdRatio.setValue(new Double(sg.sdRatio));
		sdRatio.setColumns(10);
		sdRatio.addPropertyChangeListener("value", this);
		optionsPanel.add(sdRatio);
		
		JLabel syllRepsLabel=new JLabel("Syllable repetition weighting: ");
		optionsPanel.add(syllRepsLabel);
		addSyllReps=new JFormattedTextField();
		addSyllReps.setValue(new Double(sg.syllableRepetitionWeighting));
		addSyllReps.setColumns(10);
		addSyllReps.addPropertyChangeListener("value", this);
		optionsPanel.add(addSyllReps);
		
		/*
		JLabel offsetLabel=new JLabel("Offset removal: ");
		optionsPanel.add(offsetLabel);
		offsetRemoval=new JFormattedTextField();
		offsetRemoval.setValue(new Double(sg.offsetRemoval));
		offsetRemoval.setColumns(10);
		offsetRemoval.addPropertyChangeListener("value", this);
		optionsPanel.add(offsetRemoval);
		*/
		
		if (enableSyllableStitching){
			JLabel stitchLabel=new JLabel("Cost for stitching syllables: ");
			optionsPanel.add(stitchLabel);
			stitchPunishment=new JFormattedTextField();
			stitchPunishment.setValue(new Double(sg.stitchPunishment));
			stitchPunishment.setColumns(10);
			stitchPunishment.addPropertyChangeListener("value", this);
			optionsPanel.add(stitchPunishment);
		}
		
		JLabel alignmentLabel=new JLabel("Cost for alignment error: ");
		optionsPanel.add(alignmentLabel);
		alignmentCost=new JFormattedTextField();
		alignmentCost.setValue(new Double(sg.alignmentCost));
		alignmentCost.setColumns(10);
		alignmentCost.addPropertyChangeListener("value", this);
		optionsPanel.add(alignmentCost);
		
		optionsPanel.add(weightByAmp);
		weightByAmp.setSelected(sg.weightByAmp);
		optionsPanel.add(logFrequencies);
		logFrequencies.setSelected(sg.logFrequencies);
		
		JPanel stitchPanel=new JPanel(new BorderLayout());
		stitch.setSelectedIndex(sg.stitchSyllables);
		JLabel stitchLabel=new JLabel("Syllable comparison method: ");
		stitchPanel.add(stitchLabel, BorderLayout.WEST);
		stitchPanel.add(stitch, BorderLayout.CENTER);
		
		optionsPanel.add(stitchPanel);
		//compareWholeSyllables.setSelected(sg.stitchSyllables);
		
		//if (enableSyllableStitching){	
		//	optionsPanel.add(compareWholeSyllables);
		//}
		cpane=new JPanel();
		cpane.setLayout(new BorderLayout());
		cpane.add(weightingPanel, BorderLayout.LINE_START);
		cpane.add(optionsPanel, BorderLayout.LINE_END);
	}
		
	public void makeFrame(){
		JPanel contentpane=new JPanel();
		contentpane.setLayout(new BorderLayout());
		contentpane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		contentpane.add(cpane, BorderLayout.LINE_START);
		
		JPanel bottomPane=new JPanel(new BorderLayout());
		bottomPane.add(progressLabel, BorderLayout.CENTER);
		contentpane.add(bottomPane, BorderLayout.SOUTH);
		
		this.add(contentpane);
	}
	
	public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

		if (EXPORT.equals(command)) {
			DisplayPane dp=(DisplayPane)tabPane.getSelectedComponent();
			dp.export();
		}
		else if (SAVE_IMAGE.equals(command)) {
			DisplayPane dp=(DisplayPane)tabPane.getSelectedComponent();
			dp.saveImage();
		}
			
	}
	
	public void startAnalysis(){ //truly horrible
		progressLabel.setText("Preparing elements...");
		Rectangle rect=progressLabel.getBounds();
		rect.x=0;
		rect.y=0;
			
		progressLabel.paintImmediately(rect);
		progress=0;
		nextProgressTarget=0;

		System.out.println("DTWPanel: Labeling elements");
		sg.makeNames();
		sg.weightByAmp=weightByAmp.isSelected();
		sg.stitchSyllables=stitch.getSelectedIndex();
		
		System.out.println("DTWPanel: Setting parameters");
		sg.defaults.setDTWParameters(sg);
		
		System.out.println("DTWPanel: getting valid parameters");
		sg.getValidParameters(true);
		System.out.println("DTWPanel: compressing data");
		sg.compressData2();
				
		progressLabel.setText("data compressed");
		System.out.println("DTWPanel: prepare to normalize");
		sg.prepareToNormalize(sg.data);
		progressLabel.setText("average/sd whole list");
		System.out.println("DTWPanel: prepare to normalize per element");
		sg.prepareToNormalizePerElement(sg.data);
		progressLabel.setText("average/sd per element");
		//sg.normalizePerElement=normalizePerElement.isSelected();
		
		//sg.data=sg.normalize(sg.data);
		//sg.prepareToNormalizePerElement(sg.data);
		//progressLabel.setText("average/sd per element");
		//sg.normalizePerElement=normalizePerElement.isSelected();
		//progressLabel.setText("normalize");
		
		if (stitch.getSelectedIndex()!=1){
			System.out.println("DTWPanel: element DTW running");
			//sg.scoresEle=sg.runDTW();
			//System.out.println("DTWPanel: normalizing scores");
			//sg.scoresEle=sg.normalizeScores(sg.scoresEle);
			//System.out.println("DTWPanel: transforming scores");
			//sg.scoresEle=sg.transformScores(sg.scoresEle);
		}
		else{
			sg.scoresEle=null;
		}
		if (stitch.getSelectedIndex()!=0){
			System.out.println("DTWPanel: syllable analysis");
			System.out.println("DTWPanel: getting parameters");
			sg.getValidParameters(true);
			System.out.println("DTWPanel: compressing data");
			sg.compressData2();
			System.out.println("DTWPanel: stitching syllables");
			sg.data=sg.stitchSyllables();
			System.out.println("DTWPanel: normalizing");
			sg.prepareToNormalize(sg.data);
			sg.prepareToNormalizePerElement(sg.data);
			//sg.normalizePerElement=normalizePerElement.isSelected();
			//sg.data=sg.normalize(sg.data);
			//sg.prepareToNormalize(sg.data);
			//sg.prepareToNormalizePerElement(sg.data);
			System.out.println("DTWPanel: running analysis");
			//sg.scoresSyll2=sg.runDTW();
			//sg.scoresSyll2=sg.normalizeScores(sg.scoresSyll2);
			//sg.scoresSyll2=sg.transformScores(sg.scoresSyll2);  WHY AREN'T THESE NORMALIZED?!
		}
		else{
			sg.scoresSyll2=null;
		}
		
	}

	public void simpleAnalysis(){
		progressLabel.setText("Preparing elements...");
		Rectangle rect=progressLabel.getBounds();
		rect.x=0;
		rect.y=0;
		
		progressLabel.paintImmediately(rect);
		progress=0;
		nextProgressTarget=0;
		sg.countEleNumber();
		sg.weightByAmp=weightByAmp.isSelected();		
		sg.defaults.setDTWParameters(sg);
		sg.getValidParameters(true);
		sg.compressData2();
		
		progressLabel.setText("data compressed");
		
		sg.prepareToNormalize(sg.data);
		progressLabel.setText("average/sd whole list");
		
		sg.prepareToNormalizePerElement(sg.data);
		progressLabel.setText("average/sd per element");
		//sg.normalizePerElement=normalizePerElement.isSelected();
		
		
		//sg.scoresEle=sg.runDTW();
		sg.scoresEle=sg.normalizeScores(sg.scoresEle);
		sg.scoresEle=sg.transformScores(sg.scoresEle);
		
	}
	
	public void updateProgressLabel(){
		progress++;
		if (progress>=nextProgressTarget){
			nextProgressTarget+=10;
			progressLabel.setText("Comparing elements: "+progress+" of "+sg.eleNumber);
			Rectangle rect=progressLabel.getBounds();
			rect.x=0;
			rect.y=0;
			progressLabel.paintImmediately(rect);
		}
	}
	
	public void propertyChange(PropertyChangeEvent e) {
        Object source = e.getSource();
		for (int i=0; i<parametersTF.length; i++){
			if (source==parametersTF[i]){
				sg.parameterValues[i] = ((Number)parametersTF[i].getValue()).doubleValue();
				if (sg.parameterValues[i]>1000000){sg.parameterValues[i]=1000000;}
				if (sg.parameterValues[i]<0){sg.parameterValues[i]=0;}
				parametersTF[i].setValue(new Double(sg.parameterValues[i]));		
			}
		}
		
		if (source==compressionFactor){
			sg.mainReductionFactor=((Number)compressionFactor.getValue()).doubleValue();
			if (sg.mainReductionFactor>1){sg.mainReductionFactor=1;}
			if (sg.mainReductionFactor<0.001){sg.mainReductionFactor=0.001;}
			compressionFactor.setValue(new Double(sg.mainReductionFactor));
		}
		if (source==minPoints){
			sg.minPoints=((Number)minPoints.getValue()).intValue();
			if (sg.minPoints<1){sg.minPoints=1;}
			minPoints.setValue(new Integer(sg.minPoints));
		}
		if (source==sdRatio){
			sg.sdRatio=((Number)sdRatio.getValue()).doubleValue();
			if (sg.sdRatio<0){sg.sdRatio=0;}
			if (sg.sdRatio>1){sg.sdRatio=1;}
			sdRatio.setValue(new Double(sg.sdRatio));
		}
		if (source==offsetRemoval){
			sg.offsetRemoval=((Number)offsetRemoval.getValue()).doubleValue();
			if (sg.offsetRemoval<0){sg.offsetRemoval=0;}
			if (sg.offsetRemoval>1){sg.offsetRemoval=1;}
			offsetRemoval.setValue(new Double(sg.offsetRemoval));
		}
		if (source==stitchPunishment){
			sg.stitchPunishment=((Number)stitchPunishment.getValue()).doubleValue();
			if (sg.stitchPunishment<0){sg.stitchPunishment=0;}
			stitchPunishment.setValue(new Double(sg.stitchPunishment));
		}
		if (source==alignmentCost){
			sg.alignmentCost=((Number)alignmentCost.getValue()).doubleValue();
		}
		if (source==addSyllReps){
			sg.syllableRepetitionWeighting=(double)((Number)addSyllReps.getValue()).doubleValue();
			if (sg.syllableRepetitionWeighting<=0.00000001){sg.syllableRepetitionWeighting=0;}
			addSyllReps.setValue(new Double(sg.syllableRepetitionWeighting));
		}
	}
}