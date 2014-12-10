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
import java.text.*;

import lusc.net.github.Defaults;
import lusc.net.github.analysis.CompressComparisons;
//import lusc.net.github.analysis.SongGroup;
import lusc.net.github.analysis.AnalysisGroup;
import lusc.net.github.analysis.syntax.SyntaxAnalysis;
import lusc.net.github.db.DataBaseController;

public class StatOptionPanel extends JPanel implements PropertyChangeListener {

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
	
	
	JRadioButton elementCompression=new JRadioButton("Compress element distance", true);
	JRadioButton useTransForSong=new JRadioButton("Use syllable transitions for song distance", true);
	JRadioButton popComp=new JRadioButton("Population comparison", true);
	
	String[] syntaxOptions={"Markov Chain", "Match length", "Both"};
	String[] dendrogramOptions={"UPGMA", "Ward's Method", "Flexible Beta (-0.25)", "Complete linkage", "Single linkage"};
	JComboBox syntOpts=new JComboBox(syntaxOptions);
	JComboBox dendOpts=new JComboBox(dendrogramOptions);
	JFormattedTextField numdims, syntKs, clustKs, clustKsb, snnKF, snnMinPtsF, snnEpsF, songUpperProp, songLowerProp, geogProp;
	
	boolean[] analysisTypes=new boolean[11];
	boolean[] analysisLevels=new boolean[4];
	boolean[] miscOptions=new boolean[3];
	

	JPanel resultsPanel;
	
	int ndi=5;
	int dendrogramMode=1;
	int syntaxMode=2;
	int minClusterK=2;
	int maxClusterK=10;
	int snnK=10;
	int snnMinPts=4;
	int snnEps=6;
	int maxSyntaxK=10;
	double gapWeighting=0;
	double syllRepWeighting=0;
	double songUpperLimit=0.5;
	double songLowerLimit=20;
	double geogPropLimit=5;
		
	
	int pcsUsed=2;
	
	int xd, yd;
	boolean anacomp, anael, anasy, anast, anaso;
	boolean sequenceAnalysis=false;
	boolean mdlAnalysis=false;
	boolean analyzeForward=false;
	boolean analyzeBackward=false;
	boolean analyzeRelative=false;
	boolean analyzeMissTerminal2=false;
	
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
		JPanel optionsettings=new JPanel();
		optionsettings.setLayout(new GridLayout(0,1));
		optionsettings.add(matrix);
		optionsettings.add(distDistribution);
		optionsettings.add(upgmaTree);
		optionsettings.add(nmds);
		optionsettings.add(geog);
		
		optionsettings.add(hopkins);
		optionsettings.add(anderson);
		optionsettings.add(distfunc);
		optionsettings.add(cluster);
		optionsettings.add(snn);
		optionsettings.add(syntaxCluster);
		
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
		
		element.setSelected(analysisLevels[0]);
		if (ag.getScoresEle()==null){
			element.setSelected(false);
			element.setEnabled(false);
		}
		syllable.setSelected(analysisLevels[1]);
		syllableTransition.setSelected(analysisLevels[2]);
		song.setSelected(analysisLevels[3]);
		
				
		JPanel variablesPanel=new JPanel(new GridLayout(0,1));
		variablesPanel.setBorder(BorderFactory.createTitledBorder("Analysis variables"));
		
		JPanel ndpan=new JPanel(new BorderLayout());
		JLabel numdimlab=new JLabel("Number of dimensions for NMDS: ");
		ndpan.add(numdimlab, BorderLayout.WEST);
		numdims=new JFormattedTextField(num);
		numdims.setColumns(6);
		numdims.addPropertyChangeListener("value", this);
		numdims.setValue(new Integer(ndi));
		ndpan.add(numdims, BorderLayout.CENTER);
		
		JPanel kcpana=new JPanel(new BorderLayout());
		JLabel kclab=new JLabel("      Maximum K-medoid clusters: ");
		kcpana.add(kclab, BorderLayout.WEST);
		clustKs=new JFormattedTextField(num);
		clustKs.setColumns(6);
		clustKs.addPropertyChangeListener("value", this);
		clustKs.setValue(new Integer(maxClusterK));
		kcpana.add(clustKs, BorderLayout.CENTER);
		
		JPanel kcpanb=new JPanel(new BorderLayout());
		JLabel kclabb=new JLabel("      Minimum K-medoid clusters: ");
		kcpanb.add(kclabb, BorderLayout.WEST);
		clustKsb=new JFormattedTextField(num);
		clustKsb.setColumns(6);
		clustKsb.addPropertyChangeListener("value", this);
		clustKsb.setValue(new Integer(minClusterK));
		kcpanb.add(clustKsb, BorderLayout.CENTER);
		
		JPanel kcpan=new JPanel(new BorderLayout());
		kcpan.add(kcpana, BorderLayout.WEST);
		kcpan.add(kcpanb, BorderLayout.EAST);
		
		JPanel snnpana=new JPanel(new BorderLayout());
		JLabel snnlab=new JLabel("SNN k: ");
		snnpana.add(snnlab, BorderLayout.WEST);
		snnKF=new JFormattedTextField(num);
		snnKF.setColumns(6);
		snnKF.addPropertyChangeListener("value", this);
		snnKF.setValue(new Integer(snnK));
		snnpana.add(snnKF, BorderLayout.CENTER);
		
		JPanel snnpanb=new JPanel(new BorderLayout());
		JLabel snnlabb=new JLabel("SNN MinPts: ");
		snnpanb.add(snnlabb, BorderLayout.WEST);
		snnMinPtsF=new JFormattedTextField(num);
		snnMinPtsF.setColumns(6);
		snnMinPtsF.addPropertyChangeListener("value", this);
		snnMinPtsF.setValue(new Integer(snnMinPts));
		snnpanb.add(snnMinPtsF, BorderLayout.CENTER);
		
		JPanel snnpanc=new JPanel(new BorderLayout());
		JLabel snnlabc=new JLabel("SNN EPS: ");
		snnpanc.add(snnlabc, BorderLayout.WEST);
		snnEpsF=new JFormattedTextField(num);
		snnEpsF.setColumns(6);
		snnEpsF.addPropertyChangeListener("value", this);
		snnEpsF.setValue(new Integer(snnEps));
		snnpanc.add(snnEpsF, BorderLayout.CENTER);
		
		JPanel snnpan=new JPanel(new BorderLayout());
		snnpan.add(snnpana, BorderLayout.WEST);
		snnpan.add(snnpanb, BorderLayout.CENTER);
		snnpan.add(snnpanc, BorderLayout.EAST);
		
		JPanel kspan=new JPanel(new BorderLayout());
		JLabel kslab=new JLabel("        Maximum K syntax clusters: ");
		kspan.add(kslab, BorderLayout.WEST);
		syntKs=new JFormattedTextField(num);
		syntKs.setColumns(6);
		syntKs.addPropertyChangeListener("value", this);
		syntKs.setValue(new Integer(maxSyntaxK));
		kspan.add(syntKs, BorderLayout.CENTER);
		
		JPanel sypan=new JPanel(new BorderLayout());
		JLabel sylab=new JLabel("     Syntax methods: ");
		syntOpts.setSelectedIndex(syntaxMode);
		sypan.add(sylab, BorderLayout.WEST);
		sypan.add(syntOpts, BorderLayout.CENTER);
		
		JPanel dendpan=new JPanel(new BorderLayout());
		JLabel dendlab=new JLabel("Dendrogram method: ");
		dendOpts.setSelectedIndex(dendrogramMode);
		dendpan.add(dendlab, BorderLayout.WEST);
		dendpan.add(dendOpts, BorderLayout.CENTER);
				
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
		variablesPanel.add(useTransForSong);
		variablesPanel.add(popComp);
		variablesPanel.add(ndpan);
		variablesPanel.add(kcpan);
		variablesPanel.add(snnpan);
		variablesPanel.add(kspan);
		variablesPanel.add(sypan);
		variablesPanel.add(dendpan);
		variablesPanel.add(songPropUpperPan);
		variablesPanel.add(songPropLowerPan);
		variablesPanel.add(geogPropPan);
		
		
		elementCompression.setSelected(miscOptions[0]);
		useTransForSong.setSelected(miscOptions[1]);
		popComp.setSelected(miscOptions[2]);
		
		
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
	
	public int getndi(){
		return ndi;
	}
	
	public int getDendrogramMode(){
		return dendrogramMode;
	}
	
	public int getSyntaxMode(){
		return syntaxMode;
	}
	
	public int getMaxClusterK(){
		return maxClusterK;
	}
	
	public int getMinClusterK(){
		return minClusterK;
	}
	
	public int getsnnK(){
		return snnK;
	}
	
	public int getsnnMinPts(){
		return snnMinPts;
	}
	
	public int getsnnEps(){
		return snnEps;
	}
	
	public int getMaxSyntaxK(){
		return maxSyntaxK;
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
	
	public void setndi(int a){
		ndi=a;
	}
	
	public void setDendrogramMode(int a){
		dendrogramMode=a;
	}
	
	public void setSyntaxMode(int a){
		syntaxMode=a;
	}
	
	public void setMaxClusterK(int a){
		maxClusterK=a;
	}
	
	public void setMinClusterK(int a){
		minClusterK=a;
	}
	
	public void setsnnK(int a){
		snnK=a;
	}
	
	public void setsnnMinPts(int a){
		snnMinPts=a;
	}
	
	public void setsnnEps(int a){
		snnEps=a;
	}
	
	public void setMaxSyntaxK(int a){
		maxSyntaxK=a;
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
		
		miscOptions[0]=elementCompression.isSelected();
		miscOptions[1]=useTransForSong.isSelected();
		miscOptions[2]=popComp.isSelected();
		
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
	
	
	public void propertyChange(PropertyChangeEvent e) {
        Object source = e.getSource();
		if (source==numdims){
			ndi = (int)((Number)numdims.getValue()).intValue();
			if (ndi>500){ndi=500;}
			if (ndi<2){ndi=2;}
			numdims.setValue(new Integer(ndi));
			ac.mdsProvided=false;
		}
		if (source==syntKs){
			maxSyntaxK = (int)((Number)syntKs.getValue()).intValue();
			if (maxSyntaxK>50){maxSyntaxK=50;}
			if (maxSyntaxK<2){maxSyntaxK=2;}
			syntKs.setValue(new Integer(maxSyntaxK));
		}
		if (source==clustKs){
			maxClusterK = (int)((Number)clustKs.getValue()).intValue();
			//if (maxClusterK>50+minClusterK){maxClusterK=50+minClusterK;}
			if (maxClusterK<2+minClusterK){maxClusterK=2+minClusterK;}
			clustKs.setValue(new Integer(maxClusterK));
		}
		if (source==clustKsb){
			minClusterK = (int)((Number)clustKsb.getValue()).intValue();
			if (minClusterK>maxClusterK-2){minClusterK=maxClusterK-2;}
			if (minClusterK<2){minClusterK=2;}
			clustKsb.setValue(new Integer(minClusterK));
		}
		if (source==snnKF){
			snnK = (int)((Number)snnKF.getValue()).intValue();
			//if (maxClusterK>50+minClusterK){maxClusterK=50+minClusterK;}
			if (snnK<4){snnK=4;}
			snnKF.setValue(new Integer(snnK));
		}
		if (source==snnEpsF){
			snnEps = (int)((Number)snnEpsF.getValue()).intValue();
			//if (snnEps>=snnK){snnEps=snnK-1;}
			snnEpsF.setValue(new Integer(snnEps));
		}
		if (source==snnMinPtsF){
			snnMinPts = (int)((Number)snnMinPtsF.getValue()).intValue();
			if (snnMinPts>=snnK){snnMinPts=snnK-1;}
			snnMinPtsF.setValue(new Integer(snnMinPts));
		}
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