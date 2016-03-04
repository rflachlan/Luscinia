package lusc.net.github.ui.compmethods;
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

import lusc.net.github.Defaults;
//import lusc.net.github.analysis.SongGroup;
import lusc.net.github.analysis.AnalysisGroup;
import lusc.net.github.db.DataBaseController;
import lusc.net.github.ui.statistics.DisplayPane;


public class DTWPanel extends JPanel implements ActionListener, PropertyChangeListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2798852000648684253L;
	static String EXPORT="export";
	static String SAVE_IMAGE="save image";
	static String WEIGHT_BY_AMP="weightbyamp";
	static String LOG_FREQUENCIES="log frequencies";
	static String NORMALISE_SDS="normaliseSDs";
	static String STITCH_OPTIONS="stitchoptions";
	static String ALIGN_OPTIONS="alignoptions";
	static String INTERPOLATE_OPT="interpolate";
	static String WARP_TYPE_OPT="warp type";
	static String SQUARED_DIST="squared dist";
	
	
	int progress=0;
	int nextProgressTarget=10;
	
	JRadioButton weightByAmpB=new JRadioButton("Weight by relative amplitude", true);
	//JRadioButton normalizePerElement=new JRadioButton("Normalize each element", false);
	JRadioButton logFrequenciesB=new JRadioButton("Log transform frequencies", true);
	//JRadioButton compareWholeSyllables=new JRadioButton("Stitch and compare syllables", true);
	
	JRadioButton normaliseWithSDsB=new JRadioButton("Weight features with SDs", true);
	
	JRadioButton interpolateB=new JRadioButton("Interpolate in TW", true);
	
	JRadioButton squaredDist=new JRadioButton("Use squared distance in TW", false);
	
	JRadioButton warpTypeB=new JRadioButton("Dynamic warping", true);
	
	String[] stitchOptions={"Individual elements", "Stitch elements", "Both"};
	JComboBox stitch=new JComboBox(stitchOptions);
	
	String[] alignPOptions={"Start", "End", "Centre", "Three Points", "Five Points"};
	JComboBox alignP=new JComboBox(alignPOptions);
	
	JPanel cpane;
	JTabbedPane tabPane=new JTabbedPane();
	
	boolean weightByAmp=false;
	boolean logFrequencies=false;
	boolean normaliseWithSDs=false;
	boolean interpolate=true;
	boolean dynamicWarping=true;
	boolean squared=false;
	
	int stitchSyllables=0;
	double mainReductionFactor=0.25;
	int minPoints=5;
	double minVar=0.2;
	double stitchPunishment=150;
	double maximumWarp=0.25;
	double sdRatio=1;
	double sdRatio2=0;
	double offsetRemoval=0.0;
	double alignmentCost=0.2;
	int alignmentPoints=3;
	double syllableRepetitionWeighting=0;
	public double[] parameterValues={1,0,0,0,0,1,0,0,0,1,0,0,0,0,1,0,0, 0, 0,0,0,0,0};
	
	String[] parameters={"Time", "Relative position", "Peak frequency", "Mean frequency", "Median frequency", "Fundamental frequency", "Peak frequency change", "Mean frequency change",
						"Median frequency change", "Fundamental frequency change", "Harmonicity", "Wiener entropy", "Frequency bandwidth", "Amplitude", "Vibrato rate", "Vibrato amplitude", 
						"Gap between elements", "PF norm", "MF norm", "MeF norm", "FF norm", "Upper cut-off", "Lower cut-off"};

	JFormattedTextField[] parametersTF;
	
	JFormattedTextField compressionFactorTF, minPointsTF, sdRatioTF, sdRatioTF2, offsetRemovalTF, stitchPunishmentTF, alignmentCostTF, addSyllRepsTF, maximumWarpTF;
		
	boolean started=false;
	boolean textOut=false;
	boolean enableSyllableStitching=true;
		
	int pcsUsed=2;
	
	Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
	Dimension dim=new Dimension(d.width-400, d.height);
	
	int mode=0;
	
	//JLabel progressLabel=new JLabel("Waiting to start. Click 'Next step' to start comparison");
	LinkedList compScheme;
	DataBaseController dbc;
	//SongGroup sg;
	AnalysisGroup ag;
	Defaults defaults;
	NumberFormat num;
	
	//public  DTWPanel (DataBaseController dbc, SongGroup sg, boolean enableSyllableStitching, Defaults defaults){
	public  DTWPanel (DataBaseController dbc, AnalysisGroup ag, boolean enableSyllableStitching, Defaults defaults){
		this.dbc=dbc;
		//this.sg=sg;
		this.ag=ag;
		this.enableSyllableStitching=enableSyllableStitching;
		this.defaults=defaults;
		getDTWParameters();
		num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(10);	
		mode=1;
		parameterSetting();
		makeFrame();
	}
	
	//public DTWPanel (DataBaseController dbc, SongGroup sg, LinkedList compScheme, Defaults defautls){
	public DTWPanel (DataBaseController dbc, AnalysisGroup ag, LinkedList compScheme, Defaults defautls){
		this.dbc=dbc;
		//this.sg=sg;
		this.ag=ag;
		this.compScheme=compScheme;
		this.defaults=defaults;
		getDTWParameters();
		num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(10);
		mode=0;
		parameterSetting();
		makeFrame();
	}
	
	void getDTWParameters(){
		defaults.getDTWParameters(this);
	}
	
	void setDTWParameters(){
		defaults.setDTWParameters(this);
	}
	
	public double[] getParameterValues(){
		return parameterValues;
	}
	
	public boolean getWeightByAmp(){
		return weightByAmp;
	}
	
	public boolean getInterpolate(){
		return interpolate;
	}
	
	public boolean getSquared(){
		return squared;
	}
	
	public int getStitchSyllables(){
		return stitchSyllables;
	}
	
	public int getAlignmentPoints(){
		return alignmentPoints;
	}
	
	public boolean getLogFrequencies(){
		return logFrequencies;
	}
	
	public boolean getWeightBySD(){
		return normaliseWithSDs;
	}
	
	public boolean getDynamicWarping(){
		return dynamicWarping;
	}
	
	public double getMainReductionFactor(){
		return mainReductionFactor;
	}
	
	public double getOffsetRemoval(){
		return offsetRemoval;
	}
	
	public double getSDRatio(){
		return sdRatio;
	}
	
	public double getSDRatio2(){
		return sdRatio2;
	}
	
	public double getStitchPunishment(){
		return stitchPunishment;
	}
	
	public double getAlignmentCost(){
		return alignmentCost;
	}
	
	public double getMaximumWarp(){
		return maximumWarp;
	}
	
	public int getMinPoints(){
		return minPoints;
	}
	
	public double getSyllableRepetitionWeighting(){
		return syllableRepetitionWeighting;
	}
	
	public void setParameterValues(double[] pv){
		parameterValues=pv;
	}
	
	public void setWeightByAmp(boolean w){
		System.out.println("WEIGHT BY AMP: "+w);
		weightByAmp=w;
	}
	
	public void setInterpolate(boolean w){
		interpolate=w;
	}
	
	public void setSquared(boolean w){
		squared=w;
	}
	
	public void setStitchSyllables(int a){
		stitchSyllables=a;
	}
	
	public void setAlignmentPoints(int a){
		alignmentPoints=a;
	}
	
	public void setLogFrequencies(boolean a){
		logFrequencies=a;
	}
	
	public void setWeightBySD(boolean a){
		normaliseWithSDs=a;
	}
	
	public void setDynamicWarping(boolean a){
		dynamicWarping=a;
	}
	
	public void setMainReductionFactor(double a){
		mainReductionFactor=a;
	}
	
	public void setOffsetRemoval(double a){
		offsetRemoval=a;
	}
	
	public void setSDRatio(double a){
		sdRatio=a;
	}
	
	public void setSDRatio2(double a){
		sdRatio2=a;
	}
	
	public void setStitchPunishment(double a){
		stitchPunishment=a;
	}
	
	public void setAlignmentCost(double a){
		alignmentCost=a;
	}
	
	public void setMaximumWarp(double a){
		maximumWarp=a;
	}
	
	public void setMinPoints(int a){
		minPoints=a;
	}
	
	public void setSyllableRepetitionWeighting(double a){
		syllableRepetitionWeighting=a;
	}
	
	public void parameterSetting(){
		JPanel weightingPanel=new JPanel();
		weightingPanel.setBorder(BorderFactory.createTitledBorder("Weightings for time-warping"));
		weightingPanel.setLayout(new GridLayout(0,2));
		int buttonSet=parameters.length;
		parametersTF=new JFormattedTextField[buttonSet];
		
		for (int i=0; i<buttonSet; i++){
		
			JLabel parameterLabel=new JLabel(parameters[i]);
			weightingPanel.add(parameterLabel);
			parametersTF[i]=new JFormattedTextField();
			//parametersTF[i].setValue(new Double(parameterValues[i]));
			parametersTF[i].setValue(new Double(parameterValues[i]));	//YOU NEED TO MAKE THIS TLK TO DEFAULTS AND TP DTWPREP!
			parametersTF[i].setColumns(10);
			parametersTF[i].addPropertyChangeListener("value", this);
			weightingPanel.add(parametersTF[i]);
		}
	
		JPanel optionsPanel=new JPanel();
		optionsPanel.setBorder(BorderFactory.createTitledBorder("Time-warping options"));
		optionsPanel.setLayout(new GridLayout(0,2));
		
		JLabel compressionLabel=new JLabel("Compression factor: ");
		optionsPanel.add(compressionLabel);
		compressionFactorTF=new JFormattedTextField();
		compressionFactorTF.setValue(new Double(mainReductionFactor));
		compressionFactorTF.setColumns(10);
		compressionFactorTF.addPropertyChangeListener("value", this);
		optionsPanel.add(compressionFactorTF);
		
		JLabel minPointsLabel=new JLabel("Minimum element length: ");
		optionsPanel.add(minPointsLabel);
		minPointsTF=new JFormattedTextField();
		minPointsTF.setValue(new Integer(minPoints));
		minPointsTF.setColumns(10);
		minPointsTF.addPropertyChangeListener("value", this);
		optionsPanel.add(minPointsTF);
		
		JLabel ratioLabel=new JLabel("Time SD weighting: ");
		optionsPanel.add(ratioLabel);
		sdRatioTF=new JFormattedTextField();
		sdRatioTF.setValue(new Double(sdRatio));
		sdRatioTF.setColumns(10);
		sdRatioTF.addPropertyChangeListener("value", this);
		optionsPanel.add(sdRatioTF);
		
		JLabel ratioLabel2=new JLabel("Non-Time SD weighting: ");
		optionsPanel.add(ratioLabel2);
		sdRatioTF2=new JFormattedTextField();
		sdRatioTF2.setValue(new Double(sdRatio2));
		sdRatioTF2.setColumns(10);
		sdRatioTF2.addPropertyChangeListener("value", this);
		optionsPanel.add(sdRatioTF2);
		
		JLabel syllRepsLabel=new JLabel("Syllable repetition weighting: ");
		optionsPanel.add(syllRepsLabel);
		addSyllRepsTF=new JFormattedTextField();
		addSyllRepsTF.setValue(new Double(syllableRepetitionWeighting));
		addSyllRepsTF.setColumns(10);
		addSyllRepsTF.addPropertyChangeListener("value", this);
		optionsPanel.add(addSyllRepsTF);
		
		JLabel maxWarpLabel=new JLabel("Maximum warp (%): ");
		optionsPanel.add(maxWarpLabel);
		maximumWarpTF=new JFormattedTextField();
		maximumWarpTF.setValue(new Double(maximumWarp));
		maximumWarpTF.setColumns(10);
		maximumWarpTF.addPropertyChangeListener("value", this);
		optionsPanel.add(maximumWarpTF);
		
		/*
		JLabel offsetLabel=new JLabel("Offset removal: ");
		optionsPanel.add(offsetLabel);
		offsetRemovalTF=new JFormattedTextField();
		offsetRemovalTF.setValue(new Double(sg.offsetRemovalTF));
		offsetRemovalTF.setColumns(10);
		offsetRemovalTF.addPropertyChangeListener("value", this);
		optionsPanel.add(offsetRemovalTF);
		*/
		
		if (enableSyllableStitching){
			JLabel stitchLabel=new JLabel("Cost for stitching syllables: ");
			optionsPanel.add(stitchLabel);
			stitchPunishmentTF=new JFormattedTextField();
			stitchPunishmentTF.setValue(new Double(stitchPunishment));
			stitchPunishmentTF.setColumns(10);
			stitchPunishmentTF.addPropertyChangeListener("value", this);
			optionsPanel.add(stitchPunishmentTF);
		}
		
		JLabel alignmentLabel=new JLabel("Cost for alignment error: ");
		optionsPanel.add(alignmentLabel);
		alignmentCostTF=new JFormattedTextField();
		alignmentCostTF.setValue(new Double(alignmentCost));
		alignmentCostTF.setColumns(10);
		alignmentCostTF.addPropertyChangeListener("value", this);
		optionsPanel.add(alignmentCostTF);
		
		weightByAmpB.setSelected(weightByAmp);
		weightByAmpB.setActionCommand(WEIGHT_BY_AMP);
		weightByAmpB.addActionListener(this);
		optionsPanel.add(weightByAmpB);
		logFrequenciesB.setSelected(logFrequencies);
		logFrequenciesB.setActionCommand(LOG_FREQUENCIES);
		logFrequenciesB.addActionListener(this);
		optionsPanel.add(logFrequenciesB);
		normaliseWithSDsB.setSelected(normaliseWithSDs);
		normaliseWithSDsB.setActionCommand(NORMALISE_SDS);
		normaliseWithSDsB.addActionListener(this);
		optionsPanel.add(normaliseWithSDsB);
		interpolateB.setSelected(interpolate);
		interpolateB.setActionCommand(INTERPOLATE_OPT);
		interpolateB.addActionListener(this);
		optionsPanel.add(interpolateB);
		squaredDist.setSelected(squared);;
		squaredDist.setActionCommand(SQUARED_DIST);
		squaredDist.addActionListener(this);
		optionsPanel.add(squaredDist);
		
		warpTypeB.setSelected(dynamicWarping);
		warpTypeB.setActionCommand(WARP_TYPE_OPT);
		warpTypeB.addActionListener(this);
		optionsPanel.add(warpTypeB);
		//JLabel blank=new JLabel(" ");
		//optionsPanel.add(blank);
		
		JPanel stitchPanel=new JPanel(new BorderLayout());
		stitch.setSelectedIndex(stitchSyllables);
		stitch.setActionCommand(STITCH_OPTIONS);
		stitch.addActionListener(this);
		JLabel stitchLabel=new JLabel("Syllable comparison method: ");
		stitchPanel.add(stitchLabel, BorderLayout.WEST);
		stitchPanel.add(stitch, BorderLayout.CENTER);
		
		optionsPanel.add(stitchPanel);
		
		JPanel alignPanel=new JPanel(new BorderLayout());
		alignP.setSelectedIndex(alignmentPoints);
		alignP.setActionCommand(ALIGN_OPTIONS);
		alignP.addActionListener(this);
		JLabel alignLabel=new JLabel("Alignment Points: ");
		alignPanel.add(alignLabel, BorderLayout.WEST);
		alignPanel.add(alignP, BorderLayout.CENTER);
		
		optionsPanel.add(alignPanel);
		
		
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
		
		//JPanel bottomPane=new JPanel(new BorderLayout());
		//bottomPane.add(progressLabel, BorderLayout.CENTER);
		//contentpane.add(bottomPane, BorderLayout.SOUTH);
		
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
		else if (WEIGHT_BY_AMP.equals(command)){
			weightByAmp=weightByAmpB.isSelected();
		}
		else if (LOG_FREQUENCIES.equals(command)){
			logFrequencies=logFrequenciesB.isSelected();
			System.out.println(logFrequencies);
		}
		else if (NORMALISE_SDS.equals(command)){
			normaliseWithSDs=normaliseWithSDsB.isSelected();
		}
		else if (INTERPOLATE_OPT.equals(command)){
			interpolate=interpolateB.isSelected();
		}
		else if (STITCH_OPTIONS.equals(command)){
			stitchSyllables=stitch.getSelectedIndex();
		}
		else if (ALIGN_OPTIONS.equals(command)){
			alignmentPoints=alignP.getSelectedIndex();
		}
		else if (WARP_TYPE_OPT.equals(command)){
			dynamicWarping=warpTypeB.isSelected();
		}
		else if (SQUARED_DIST.equals(command)){
			squared=squaredDist.isSelected();
		}
			
	}
	
	/*
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
		sg.setWeightByAmp(weightByAmp.isSelected());
		sg.setStitchSyllables(stitch.getSelectedIndex());
		
		System.out.println("DTWPanel: Setting parameters");
		defaults.setDTWParameters(sg);
		
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
	
	*/
	
	public void propertyChange(PropertyChangeEvent e) {
        Object source = e.getSource();
		for (int i=0; i<parametersTF.length; i++){
			if (source==parametersTF[i]){
				parameterValues[i] = ((Number)parametersTF[i].getValue()).doubleValue();
				if (parameterValues[i]>1000000){parameterValues[i]=1000000;}
				if (parameterValues[i]<0){parameterValues[i]=0;}
				parametersTF[i].setValue(new Double(parameterValues[i]));		
			}
		}
		
		if (source==compressionFactorTF){
			double s=((Number)compressionFactorTF.getValue()).doubleValue();
			if (s>1){s=1;}
			if (s<0.001){s=0.001;}
			mainReductionFactor=s;
			compressionFactorTF.setValue(new Double(s));
		}
		if (source==minPointsTF){
			int s=((Number)minPointsTF.getValue()).intValue();
			if (s<1){s=1;}
			minPoints=s;
			minPointsTF.setValue(new Integer(s));
		}
		if (source==sdRatioTF){
			double s=((Number)sdRatioTF.getValue()).doubleValue();
			if (s<0){s=0;}
			if (s>1){s=1;}
			sdRatio=s;
			sdRatioTF.setValue(new Double(s));
		}
		if (source==sdRatioTF2){
			double s=((Number)sdRatioTF2.getValue()).doubleValue();
			if (s<0){s=0;}
			if (s>1){s=1;}
			sdRatio2=s;
			sdRatioTF2.setValue(new Double(s));
		}
		if (source==offsetRemovalTF){
			double s=((Number)offsetRemovalTF.getValue()).doubleValue();
			if (s<0){s=0;}
			if (s>1){s=1;}
			offsetRemoval=s;
			offsetRemovalTF.setValue(new Double(s));
		}
		if (source==stitchPunishmentTF){
			double s=((Number)stitchPunishmentTF.getValue()).doubleValue();
			//if (s<0){s=0;}
			stitchPunishment=s;
			stitchPunishmentTF.setValue(new Double(s));
		}
		if (source==alignmentCostTF){
			double s=((Number)alignmentCostTF.getValue()).doubleValue();
			if (s<0){s=0;}
			alignmentCost=s;
			alignmentCostTF.setValue(new Double(s));
		}
		if (source==addSyllRepsTF){
			double s=(double)((Number)addSyllRepsTF.getValue()).doubleValue();
			if (s<=0.00000001){s=0;}
			syllableRepetitionWeighting=s;
			addSyllRepsTF.setValue(new Double(s));
		}
		if (source==maximumWarpTF){
			double s=(double)((Number)maximumWarpTF.getValue()).doubleValue();
			if (s<=0.00000001){s=0;}
			if (s>=100){s=100;}
			maximumWarp=s;
			maximumWarpTF.setValue(new Double(s));
		}
	}
	
}