package lusc.net.github.ui;
//
//  StatOptionPanel.java
//  Luscinia
//
//  Created by Robert Lachlan on 7/1/07.
//  Copyright 2007 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import javax.swing.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.*;

import lusc.net.github.Defaults;
import lusc.net.github.analysis.CompressComparisons;
//import lusc.net.github.analysis.SongGroup;
import lusc.net.github.analysis.AnalysisGroup;
import lusc.net.github.analysis.syntax.SyntaxAnalysis;
import lusc.net.github.db.DataBaseController;

public class StatOptionPanel extends JPanel implements PropertyChangeListener, ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6784655829143153598L;

	Defaults defaults;
	
	JRadioButton matrix=new JRadioButton("Distance matrix", false);
	JRadioButton distDistribution=new JRadioButton("Distance distribution", false);
	JRadioButton upgmaTree=new JRadioButton("Dendrogram", true);
	JRadioButton nmds=new JRadioButton("Nonmetric MDS", true);
	JRadioButton geog=new JRadioButton("Geographic analysis", false);
	JRadioButton mrpp=new JRadioButton("MRPP", false);
	JRadioButton anderson=new JRadioButton("Multivariate dispersion", false);
	JRadioButton distfunc=new JRadioButton("Distance/Nearest Neighbor", false);
	JRadioButton hopkins=new JRadioButton("Hopkins statistic", false);
	JRadioButton cluster=new JRadioButton("K-Medoids clustering", false);
	JRadioButton snn=new JRadioButton("SNN density clustering", false);
	JRadioButton syntaxCluster=new JRadioButton("Syntactical clustering", true);
	
	
	JRadioButton element=new JRadioButton("By element", false);
	JRadioButton syllable=new JRadioButton("By syllable", true);
	JRadioButton syllableTransition=new JRadioButton("By syllable transition", true);
	JRadioButton song=new JRadioButton("By song", false);
	JRadioButton individual=new JRadioButton("By individual", false);
	
	
	JRadioButton elementCompression=new JRadioButton("Compress element distance", true);
	JRadioButton useTransForSong=new JRadioButton("Use syllable transitions for song distance", true);
	JRadioButton cycle=new JRadioButton("Cycle at end of song: ", true);
	JRadioButton dtwComp=new JRadioButton("Use DTW to compress songs: ", true);
	//JRadioButton popComp=new JRadioButton("Population comparison", true);
	JRadioButton bestSongIndiv=new JRadioButton("Find best song matches for Individual comparisons: ", true);
	
	JFormattedTextField songUpperProp, songLowerProp, geogProp;
	
	JButton dendOptionsButton, distDOptionsButton, mdsOptionsButton, hopkinsOptionsButton, mrppOptionsButton, andersonOptionsButton,
		distFuncOptionsButton, kMedOptionsButton, snnOptionsButton, syntOptionsButton;
	
	DendrogramOptions dendOptions;
	DistanceDistributionOptions distDOptions;
	MDSOptions mdsOptions;
	HopkinsOptions hopkinsOptions;
	AndersonOptions andersonOptions;
	MRPPOptions mrppOptions;
	DistanceFunctionOptions distFuncOptions;
	KMedoidsOptions kMedOptions;
	SNNOptions snnOptions;
	SyntaxOptions syntOptions;
	
	
	boolean[] analysisTypes=new boolean[12];
	boolean[] analysisLevels=new boolean[5];
	boolean[] miscOptions=new boolean[4];
	

	JPanel resultsPanel;
	

	double gapWeighting=0;
	double syllRepWeighting=0;
	double songUpperLimit=50;
	double songLowerLimit=20;
	double geogPropLimit=5;
		
	
	int pcsUsed=2;
	
	int xd, yd;
	boolean anacomp, anael, anasy, anast, anaso;
	//boolean sequenceAnalysis=false;
	//boolean mdlAnalysis=false;
	//boolean analyzeForward=false;
	//boolean analyzeBackward=false;
	//boolean analyzeRelative=false;
	//boolean analyzeMissTerminal2=false;
	
	JLabel progressLabel=new JLabel("Waiting to start");
	//SongGroup sg;
	AnalysisGroup ag;
	SyntaxAnalysis sa;
	DataBaseController dbc;
	AnalysisChoose ac;

	Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
	Dimension dim=new Dimension(d.width-600, d.height);
	JTabbedPane tabPane;
	
	NumberFormat num;
	
	String st1=null;
	String sy1=null;
	String sy2=null;
	String sy3=null;
	String sy4=null;
	String sy5=null;
	String sy6=null;
	String sy7=null;
	String sy8=null;
	
	//public StatOptionPanel (DataBaseController dbc, SongGroup sg, Defaults defaults, AnalysisChoose ac){
	public StatOptionPanel (DataBaseController dbc, AnalysisGroup ag, Defaults defaults, AnalysisChoose ac){	
		this.ag=ag;
		this.defaults=defaults;
		this.ac=ac;
		defaults.getAnalysisOptions(this);
		
		num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(10);
		
		xd=(int)(dim.getWidth()-400);
		yd=(int)(dim.getHeight()-400);
		this.setPreferredSize(dim);
		
		dendOptions=new DendrogramOptions(defaults);
		dendOptionsButton=new JButton("options");
		dendOptionsButton.addActionListener(this);
		
		distDOptions=new DistanceDistributionOptions(defaults);
		distDOptionsButton=new JButton("options");
		distDOptionsButton.addActionListener(this);
		
		mdsOptions=new MDSOptions(defaults);
		mdsOptionsButton=new JButton("options");
		mdsOptionsButton.addActionListener(this);
		
		hopkinsOptions=new HopkinsOptions(defaults);
		hopkinsOptionsButton=new JButton("options");
		hopkinsOptionsButton.addActionListener(this);
		
		mrppOptions=new MRPPOptions(defaults);
		mrppOptionsButton=new JButton("options");
		mrppOptionsButton.addActionListener(this);
		
		andersonOptions=new AndersonOptions(defaults);
		andersonOptionsButton=new JButton("options");
		andersonOptionsButton.addActionListener(this);
		
		distFuncOptions=new DistanceFunctionOptions(defaults);
		distFuncOptionsButton=new JButton("options");
		distFuncOptionsButton.addActionListener(this);
		
		kMedOptions=new KMedoidsOptions(defaults);
		kMedOptionsButton=new JButton("options");
		kMedOptionsButton.addActionListener(this);
		
		snnOptions=new SNNOptions(defaults);
		snnOptionsButton=new JButton("options");
		snnOptionsButton.addActionListener(this);
		
		syntOptions=new SyntaxOptions(defaults);
		syntOptionsButton=new JButton("options");
		syntOptionsButton.addActionListener(this);
		
		JPanel optionsettings=new JPanel();
		optionsettings.setLayout(new GridLayout(0,1));
		optionsettings.add(matrix);
		
		JPanel distDPanel=new JPanel(new BorderLayout());
		distDPanel.add(distDistribution, BorderLayout.WEST);
		distDPanel.add(distDOptionsButton, BorderLayout.EAST);
		optionsettings.add(distDPanel);
		
		JPanel dendPanel=new JPanel(new BorderLayout());
		dendPanel.add(upgmaTree, BorderLayout.WEST);
		dendPanel.add(dendOptionsButton, BorderLayout.EAST);
		optionsettings.add(dendPanel);
		
		JPanel mdsPanel=new JPanel(new BorderLayout());
		mdsPanel.add(nmds, BorderLayout.WEST);
		mdsPanel.add(mdsOptionsButton, BorderLayout.EAST);
		optionsettings.add(mdsPanel);
		
		optionsettings.add(geog);
		
		
		JPanel hopkinsPanel=new JPanel(new BorderLayout());
		hopkinsPanel.add(hopkins, BorderLayout.WEST);
		hopkinsPanel.add(hopkinsOptionsButton, BorderLayout.EAST);
		optionsettings.add(hopkinsPanel);
		
		JPanel mrppPanel=new JPanel(new BorderLayout());
		mrppPanel.add(mrpp, BorderLayout.WEST);
		mrppPanel.add(mrppOptionsButton, BorderLayout.EAST);
		optionsettings.add(mrppPanel);
		
		JPanel andersonPanel=new JPanel(new BorderLayout());
		andersonPanel.add(anderson, BorderLayout.WEST);
		andersonPanel.add(andersonOptionsButton, BorderLayout.EAST);
		optionsettings.add(andersonPanel);
		
		JPanel dfuncPanel=new JPanel(new BorderLayout());
		dfuncPanel.add(distfunc, BorderLayout.WEST);
		dfuncPanel.add(distFuncOptionsButton, BorderLayout.EAST);
		optionsettings.add(dfuncPanel);
		
		JPanel clusterPanel=new JPanel(new BorderLayout());
		clusterPanel.add(cluster, BorderLayout.WEST);
		clusterPanel.add(kMedOptionsButton, BorderLayout.EAST);
		optionsettings.add(clusterPanel);
		
		JPanel snnPanel=new JPanel(new BorderLayout());
		snnPanel.add(snn, BorderLayout.WEST);
		snnPanel.add(snnOptionsButton, BorderLayout.EAST);
		optionsettings.add(snnPanel);
		
		JPanel syntPanel=new JPanel(new BorderLayout());
		syntPanel.add(syntaxCluster, BorderLayout.WEST);
		syntPanel.add(syntOptionsButton, BorderLayout.EAST);
		optionsettings.add(syntPanel);
		
		matrix.setSelected(analysisTypes[0]);
		distDistribution.setSelected(analysisTypes[1]);
		upgmaTree.setSelected(analysisTypes[2]);
		nmds.setSelected(analysisTypes[3]);
		geog.setSelected(analysisTypes[4]);
		hopkins.setSelected(analysisTypes[5]);
		anderson.setSelected(analysisTypes[8]);
		distfunc.setSelected(analysisTypes[9]);
		cluster.setSelected(analysisTypes[6]);
		snn.setSelected(analysisTypes[10]);
		syntaxCluster.setSelected(analysisTypes[7]);
		mrpp.setSelected(analysisTypes[11]);
		
		JPanel opane=new JPanel();
		opane.setLayout(new BorderLayout());
		opane.setBorder(BorderFactory.createTitledBorder("Types of analysis"));
		opane.add(optionsettings, BorderLayout.CENTER);
		
		JPanel hiersettings=new JPanel();
		hiersettings.setLayout(new GridLayout(0,1));
		hiersettings.add(element);
		
		hiersettings.add(syllable);
		hiersettings.add(syllableTransition);
		hiersettings.add(song);
		hiersettings.add(individual);
		
		element.setSelected(analysisLevels[0]);
		if (ag.getScores(0)==null){
			element.setSelected(false);
			element.setEnabled(false);
		}
		syllable.setSelected(analysisLevels[1]);
		syllableTransition.setSelected(analysisLevels[2]);
		song.setSelected(analysisLevels[3]);
		individual.setSelected(analysisLevels[4]);
		
				
		JPanel variablesPanel=new JPanel(new GridLayout(0,1));
		variablesPanel.setBorder(BorderFactory.createTitledBorder("Analysis variables"));
		
				
		JPanel songPropUpperPan=new JPanel(new BorderLayout());
		JLabel sprlab=new JLabel("Song dist. upper lim. (%): ");
		songPropUpperPan.add(sprlab, BorderLayout.WEST);
		songUpperProp=new JFormattedTextField(num);
		songUpperProp.setColumns(6);
		songUpperProp.addPropertyChangeListener("value", this);
		songUpperProp.setValue(new Double(songUpperLimit));
		songPropUpperPan.add(songUpperProp, BorderLayout.CENTER);
		
		JPanel songPropLowerPan=new JPanel(new BorderLayout());
		JLabel sprlab2=new JLabel("Song dist.lower lim. (%): ");
		songPropLowerPan.add(sprlab2, BorderLayout.WEST);
		songLowerProp=new JFormattedTextField(num);
		songLowerProp.setColumns(6);
		songLowerProp.addPropertyChangeListener("value", this);
		songLowerProp.setValue(new Double(songLowerLimit));
		songPropLowerPan.add(songLowerProp, BorderLayout.CENTER);
		
		JPanel geogPropPan=new JPanel(new BorderLayout());
		JLabel geoglab=new JLabel("Geog. Anal. threshold (%): ");
		geogPropPan.add(geoglab, BorderLayout.WEST);
		geogProp=new JFormattedTextField(num);
		geogProp.setColumns(6);
		geogProp.addPropertyChangeListener("value", this);
		geogProp.setValue(new Double(geogPropLimit));
		geogPropPan.add(geogProp, BorderLayout.CENTER);		
		
		variablesPanel.add(elementCompression);
		variablesPanel.add(dtwComp);
		variablesPanel.add(useTransForSong);
		variablesPanel.add(cycle);
		variablesPanel.add(bestSongIndiv);
		//variablesPanel.add(popComp);
		variablesPanel.add(songPropUpperPan);
		variablesPanel.add(songPropLowerPan);
		variablesPanel.add(geogPropPan);
		
		elementCompression.setSelected(miscOptions[0]);
		useTransForSong.setSelected(miscOptions[1]);
		//popComp.setSelected(miscOptions[2]);
		cycle.setSelected(miscOptions[3]);
		dtwComp.setSelected(miscOptions[4]);
		bestSongIndiv.setSelected(miscOptions[2]);
		
		JPanel hpane=new JPanel();
		hpane.setLayout(new BorderLayout());
		hpane.setBorder(BorderFactory.createTitledBorder("Units to analyze"));
		hpane.add(hiersettings, BorderLayout.CENTER);
		
		JPanel contentpane=new JPanel();
		contentpane.setLayout(new BorderLayout());
		contentpane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		contentpane.add(opane, BorderLayout.LINE_START);
		contentpane.add(hpane, BorderLayout.LINE_END);
		contentpane.add(variablesPanel, BorderLayout.SOUTH);
		this.add(contentpane);
	}
	
	public boolean[] getAnalysisTypes(){
		return analysisTypes;
	}
	
	public boolean[] getAnalysisLevels(){
		return analysisLevels;
	}
	
	public boolean[] getMiscOptions(){
		return miscOptions;
	}
	
	public double getSongUpperLimit(){
		return songUpperLimit;
	}
	
	public double getSongLowerLimit(){
		return songLowerLimit;
	}
	
	public double getGeogPropLimit(){
		return geogPropLimit;
	}
	
	public void setAnalysisTypes(boolean[] a){
		analysisTypes=a;
	}
	
	public void setAnalysisLevels(boolean[] a){
		analysisLevels=a;
	}
	
	public void setMiscOptions(boolean[] a){
		miscOptions=a;
	}
	
	public void setSongUpperLimit(double a){
		songUpperLimit=a;
	}
	
	public void setSongLowerLimit(double a){
		songLowerLimit=a;
	}
	
	public void setGeogPropLimit(double a){
		geogPropLimit=a;
	}
		
	
	public void writeDefaults(){
		analysisTypes[0]=matrix.isSelected();
		analysisTypes[1]=distDistribution.isSelected();
		analysisTypes[2]=upgmaTree.isSelected();
		analysisTypes[3]=nmds.isSelected();
		analysisTypes[4]=geog.isSelected();
		analysisTypes[5]=hopkins.isSelected();
		analysisTypes[6]=cluster.isSelected();
		analysisTypes[10]=snn.isSelected();
		analysisTypes[7]=syntaxCluster.isSelected();
		analysisTypes[8]=anderson.isSelected();
		analysisTypes[9]=distfunc.isSelected();
		analysisLevels[0]=element.isSelected();
		analysisLevels[1]=syllable.isSelected();
		analysisLevels[2]=syllableTransition.isSelected();
		analysisLevels[3]=song.isSelected();
		analysisLevels[4]=individual.isSelected();
		
		miscOptions[0]=elementCompression.isSelected();
		miscOptions[1]=useTransForSong.isSelected();
		miscOptions[2]=bestSongIndiv.isSelected();
		miscOptions[3]=cycle.isSelected();
		miscOptions[4]=dtwComp.isSelected();
		
		defaults.setAnalysisOptions(this);
		defaults.writeProperties();
	}
		
		
	
	public void cleanUp(){
		writeDefaults();
		System.out.println("Comparison Window's closing!");
		System.out.println("Heap size is " + Runtime.getRuntime().totalMemory());
		System.out.println("Available memory: " + Runtime.getRuntime().freeMemory());

		
		System.gc();
		System.out.println("Heap size is " + Runtime.getRuntime().totalMemory());
		System.out.println("Available memory: " + Runtime.getRuntime().freeMemory());
	}
	
	/*
	 
	 I THINK THIS METHOD IS OBSOLETE - CHECK ANALYSIS SWING WORKER
	public void compressResultsNull54(){
			
		anacomp=elementCompression.isSelected();
		anael=element.isSelected();
		anasy=syllable.isSelected();
		anast=syllableTransition.isSelected();
		anaso=song.isSelected();

		if (anacomp){
			if (sg.getScoresEle()!=null){
				sg.compressElements();
			}
		}
		if ((anasy)||(anast)||(anaso)){
			if (sg.getScoresEle()!=null){
				sg.compressSyllables2();
				if (sg.getScoresSyll2()!=null){
					sg.compressSyllables3();
				}
			}
			else{
				element.setEnabled(false);
				sg.compressSyllables5();
			}
		}
		
		if ((anasy)||(anast)||(anaso)){
			sa=new SyntaxAnalysis(sg.getScoresSyll(), sg.getLookUp(2));
			sg.transLabels=sa.transLabels;
			//sa.calcTransMatrix(sg);	
			sg.compressSyllableTransitions();
		}
		
		if (anaso){
			sg.compressSongs(useTransForSong.isSelected(), songUpperLimit, songLowerLimit);
		}
		
		sg.makeNames();
		//makeDummyScores();
	}
	
	*/
		
	public void updateProgressLabel(String s){
		progressLabel.setText(s);
		Rectangle rect=progressLabel.getBounds();
		rect.x=0;
		rect.y=0;
		progressLabel.paintImmediately(rect);
	}
	
	public void actionPerformed(ActionEvent e){
		if (e.getSource()==dendOptionsButton){
			JOptionPane.showMessageDialog(this, dendOptions);
			dendOptions.wrapUp();
		}
		if (e.getSource()==distDOptionsButton){
			JOptionPane.showMessageDialog(this, distDOptions);
			distDOptions.wrapUp();
		}
		if (e.getSource()==mdsOptionsButton){
			JOptionPane.showMessageDialog(this, mdsOptions);
			mdsOptions.wrapUp();
		}
		if (e.getSource()==hopkinsOptionsButton){
			JOptionPane.showMessageDialog(this, hopkinsOptions);
			hopkinsOptions.wrapUp();
		}
		if (e.getSource()==mrppOptionsButton){
			JOptionPane.showMessageDialog(this, mrppOptions);
			mrppOptions.wrapUp();
		}
		if (e.getSource()==andersonOptionsButton){
			JOptionPane.showMessageDialog(this, andersonOptions);
			andersonOptions.wrapUp();
		}
		if (e.getSource()==distFuncOptionsButton){
			JOptionPane.showMessageDialog(this, distFuncOptions);
			distFuncOptions.wrapUp();
		}
		if (e.getSource()==kMedOptionsButton){
			JOptionPane.showMessageDialog(this, kMedOptions);
			kMedOptions.wrapUp();
		}
		if (e.getSource()==snnOptionsButton){
			JOptionPane.showMessageDialog(this, snnOptions);
			snnOptions.wrapUp();
		}	
		if (e.getSource()==syntOptionsButton){
			JOptionPane.showMessageDialog(this, syntOptions);
			syntOptions.wrapUp();
		}
	}
	
	public void propertyChange(PropertyChangeEvent e) {
        Object source = e.getSource();
		
		if (source==songUpperProp){
			songUpperLimit=((Number)songUpperProp.getValue()).doubleValue();
			if (songUpperLimit>100){songUpperLimit=100;}
			if (songUpperLimit<songLowerLimit){songUpperLimit=songLowerLimit;}
			songUpperProp.setValue(new Double(songUpperLimit));
		}
		if (source==songLowerProp){
			songLowerLimit=((Number)songLowerProp.getValue()).doubleValue();
			if (songLowerLimit<0){songLowerLimit=0;}
			if (songUpperLimit<songLowerLimit){songLowerLimit=songUpperLimit;}
			songLowerProp.setValue(new Double(songLowerLimit));
		}
		if (source==geogProp){
			geogPropLimit=((Number)geogProp.getValue()).doubleValue();
			if (geogPropLimit<0){geogPropLimit=0;}
			if (geogPropLimit>100){geogPropLimit=100;}
			geogProp.setValue(new Double(geogPropLimit));
		}
	}

}