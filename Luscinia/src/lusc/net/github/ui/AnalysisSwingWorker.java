package lusc.net.github.ui;


import javax.swing.*;

import java.awt.event.*;
import java.awt.*;
import java.text.*;
import java.util.*;

import lusc.net.github.Defaults;
import lusc.net.github.analysis.*;
import lusc.net.github.analysis.clustering.KMedoids;
import lusc.net.github.analysis.clustering.SNNDensity;
import lusc.net.github.analysis.dendrograms.Dendrogram;
import lusc.net.github.analysis.dendrograms.UPGMA;
import lusc.net.github.analysis.multivariate.CalculateHopkinsStatistic;
import lusc.net.github.analysis.multivariate.MultiDimensionalScaling;
import lusc.net.github.analysis.multivariate.MultivariateDispersionTest;
import lusc.net.github.analysis.syntax.EntropyAnalysis;
import lusc.net.github.analysis.syntax.EntropyPopulationComp;
import lusc.net.github.analysis.syntax.SyntaxAnalysis;
import lusc.net.github.db.DataBaseController;
import lusc.net.github.ui.statistics.DendrogramPanel;
import lusc.net.github.ui.statistics.DisplayGeographicComparison;
import lusc.net.github.ui.statistics.DisplayPC;
import lusc.net.github.ui.statistics.DisplayPane;
import lusc.net.github.ui.statistics.DisplaySimilarity;
import lusc.net.github.ui.statistics.DisplaySimilarityProportions;
import lusc.net.github.ui.statistics.DisplaySummary;
import lusc.net.github.ui.statistics.DisplayUPGMA;

public class AnalysisSwingWorker extends SwingWorker<String, Object> implements ActionListener {
	
	Defaults defaults;
	
	private static String SAVE_IMAGE = "save image";
	private static String EXPORT = "export";	
	
	MultiDimensionalScaling mdsEle=null;
	MultiDimensionalScaling mdsSyll=null;
	MultiDimensionalScaling mdsSyTr=null;
	MultiDimensionalScaling mdsSong=null;
	
	KMedoids kmEle=null;
	KMedoids kmSyll=null;
	KMedoids kmSyTr=null;
	KMedoids kmSong=null;
	
	SNNDensity snnEle=null;
	SNNDensity snnSyll=null;
	SNNDensity snnSyTr=null;
	SNNDensity snnSong=null;
	
	
	EntropyAnalysis entEle=null;
	EntropyAnalysis entSyll=null;
	
	GeographicComparison geoEl=null;
	GeographicComparison geoSyl=null;
	GeographicComparison geoST=null;
	GeographicComparison geoSong=null;
	
	int ndi=5;
	int dendrogramMode=1;
	int syntaxMode=2;
	int minClusterK=2;
	int maxClusterK=10;
	int maxSyntaxK=10;
	int snnK=10;
	int snnMinPts=4;
	int snnEps=6;
	double songUpperLimit=0.5;
	double songLowerLimit=20;
	double geogPropLimit=5;
	
	DisplayPane dsE, dsSy, dsSt, dsSo;
	
	DisplaySummary ds=null;
	
	DisplayPC dpcEle=null;
	DisplayPC dpcSyll=null;
	DisplayPC dpcSyTr=null;
	DisplayPC dpcSong=null;
	
	DisplayUPGMA dup=null;
	DisplayUPGMA dupsy=null;
	DisplayUPGMA dupst=null;
	DisplayUPGMA dupso=null;
	
	DisplaySimilarityProportions dspE, dspSy, dspSt, dspSo;
	
	DisplayGeographicComparison dgcEl=null;
	DisplayGeographicComparison dgcSyll=null;
	DisplayGeographicComparison dgcST=null;
	DisplayGeographicComparison dgcSong=null;
	
	DendrogramPanel denpsy=null;
	DendrogramPanel denpst=null;
	DendrogramPanel denpel=null;
	DendrogramPanel denpso=null;
	
	int pcsUsed=2;
	
	int xd, yd;
	boolean anacomp, anael, anasy, anast, anaso, mdsNeeded;
	
	boolean matrixcomp, distcomp, treecomp, geogcomp, clustcomp, syntcomp, hopcomp, mdscomp, andcomp, distfunc, snncomp;
	
	boolean popcomp=true;
	
	boolean useTransForSong=true;
	
	JLabel progressLabel=new JLabel("Waiting to start");
	//SongGroup sg;
	AnalysisGroup sg;
	SyntaxAnalysis sa;
	DataBaseController dbc;
	AnalysisChoose ac;
	
	Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
	Dimension dim=new Dimension(d.width-600, d.height);
	
	JPanel resultsPanel=new JPanel();;
	JTabbedPane tabPane;
	
	NumberFormat num;
	
	int analysisLevels=0;
	int analysisSteps=0;
	int currentLevel=0;

	//public AnalysisSwingWorker (AnalysisChoose ac, StatOptionPanel sop, DataBaseController dbc, SongGroup sg, Defaults defaults){
	public AnalysisSwingWorker (AnalysisChoose ac, StatOptionPanel sop, DataBaseController dbc, AnalysisGroup sg, Defaults defaults){
		this.sg=sg;
		this.defaults=defaults;
		this.dbc=dbc;
		this.ac=ac;
		
		xd=(int)(dim.getWidth()-200);
		yd=(int)(dim.getHeight()-200);
		
		
		
		anacomp=sop.elementCompression.isSelected();
		anael=sop.element.isSelected();
		anasy=sop.syllable.isSelected();
		anast=sop.syllableTransition.isSelected();
		anaso=sop.song.isSelected();
		useTransForSong=sop.useTransForSong.isSelected();
		popcomp=sop.popComp.isSelected();
		
		matrixcomp=sop.matrix.isSelected();
		treecomp=sop.upgmaTree.isSelected();
		mdscomp=sop.nmds.isSelected();
		clustcomp=sop.cluster.isSelected();
		snncomp=sop.snn.isSelected();
		syntcomp=sop.syntaxCluster.isSelected();
		hopcomp=sop.hopkins.isSelected();
		distcomp=sop.distDistribution.isSelected();
		geogcomp=sop.geog.isSelected();
		andcomp=sop.anderson.isSelected();
		distfunc=sop.distfunc.isSelected();
		
		sop.syntaxMode=sop.syntOpts.getSelectedIndex();
		sop.dendrogramMode=sop.dendOpts.getSelectedIndex();
		
		ndi=sop.ndi;
		syntaxMode=sop.syntaxMode;
		dendrogramMode=sop.dendrogramMode;
		minClusterK=sop.minClusterK;
		maxClusterK=sop.maxClusterK;
		snnK=sop.snnK;
		snnMinPts=sop.snnMinPts;
		snnEps=sop.snnEps;
		maxSyntaxK=sop.maxSyntaxK;
		
		songUpperLimit=sop.songUpperLimit;
		songLowerLimit=sop.songLowerLimit;
		geogPropLimit=sop.geogPropLimit;
		
		sop.writeDefaults();
		
		mdsNeeded=false;
		if ((treecomp)||(mdscomp)||(clustcomp)||(hopcomp)){mdsNeeded=true;}
		

		analysisLevels=0;
		
		if (anael){analysisLevels++;}
		if (anasy){analysisLevels++;}
		if (anast){analysisLevels++;}
		if (anaso){analysisLevels++;}
		
		analysisSteps=0;
		if (mdsNeeded){analysisSteps+=3;}
		if (treecomp){analysisSteps+=3;}
		if (geogcomp){analysisSteps++;}
		if (clustcomp){analysisSteps++;}
		if (hopcomp){analysisSteps++;}
		if (syntcomp){analysisSteps++;}
		
		analysisSteps*=analysisLevels;
		if (syntcomp){
			if (anast){analysisSteps--;}
			if (anaso){analysisSteps--;}
		}
		analysisSteps+=2;
		progress();
		
		num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(10);
	}
	
	public void cleanUp(){
		System.out.println("Comparison Window's closing!");
		System.out.println("Heap size is " + Runtime.getRuntime().totalMemory());
		System.out.println("Available memory: " + Runtime.getRuntime().freeMemory());
		dup=null;
		dupsy=null;
		dupst=null;
		dupso=null;
		
		denpel=null;
		denpsy=null;
		denpst=null;
		denpso=null;
		
		mdsEle=null;
		mdsSyll=null;
		mdsSyTr=null;
		mdsSong=null;
		
		kmSyll=null;
		kmSyTr=null;
		kmSong=null;
		dpcEle=null;
		dpcSyll=null;
		dpcSyTr=null;
		dpcSong=null;
		
		ds=null;

		dspE=null;
		dspSy=null;
		dspSt=null;
		dspSo=null;
		System.gc();
		System.out.println("Heap size is " + Runtime.getRuntime().totalMemory());
		System.out.println("Available memory: " + Runtime.getRuntime().freeMemory());
	}
	
	public String doInBackground(){
		compressResults();
		try{
			analyze();
		}
		catch(Exception e){e.printStackTrace();}
		return "done";
	}
	
	public void done(){
		try{
			displayComparisons();
		}
		catch(Exception e){e.printStackTrace();}
		Integer prog=new Integer(0);
		firePropertyChange("progress", null, prog);
		if(!isCancelled()){
			ac.stepFour();
		}
		
	}
	
	public void compressResults(){
		
		
		
		if (anacomp){
			sg.compressElements();
		}
		
		if ((anasy)||(anast)||(anaso)){
			//sa=new SyntaxAnalysis(sg.scoresSyll, sg.lookUpSyls);
			//sg.transLabels=sa.transLabels;
			//sa.calcTransMatrix(sg);	
			sg.compressSyllableTransitions();
		}
		
		if (anaso){
			sg.compressSongs(useTransForSong, songUpperLimit, songLowerLimit);
		}
		
		currentLevel=1;
		progress();
		
		sg.makeNames();
		//makeDummyScores();
	}	
	
	

	/*
	 //THIS IS AN OLD FUNCTION FOR TESTING.
	public void makeDummyScores(){
		int p=5;
		int n=sg.getNSylls();
		float[][] mat=new float[n][p];
		float[][] dmat=new float[n][];
		for (int i=0; i<n; i++){
			dmat[i]=new float[i+1];
		}
		
		for (int i=0; i<n; i++){
			for (int j=0; j<p; j++){
				mat[i][j]=(float)(Math.random());
			}
		}
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				float tot=0;
				for (int k=0; k<p; k++){
					tot+=(mat[i][k]-mat[j][k])*(mat[i][k]-mat[j][k]);
				}
				dmat[i][j]=(float)Math.sqrt(tot);
			}
		}		
		sg.setScoresSyll(dmat);
	}
	*/
	
	public void analyze(){
		
		float[][] tempEle=new float[1][1];
		float[][] tempSyll=new float[1][1];
		float[][] tempSyTr=new float[1][1];
		float[][] tempSong=new float[1][1];
				
		if ((clustcomp)||(snncomp)||(syntcomp)||(hopcomp)||(andcomp)||(distfunc)){ds=new DisplaySummary(defaults);}
		
		
				
		if (anael){
			if (anacomp){
				tempEle=sg.copy(1);
				
			}
			else{
				tempEle=sg.copy(0);
			}
			
			if (mdsNeeded){
				if (ac.mdsProvided){
					mdsEle=ac.mdsEle;
				}
				if (mdsEle==null){
					mdsEle=calculateMDS(tempEle);
					ac.mdsProvided=true;
					ac.mdsEle=mdsEle;
				}
				tempEle=mdsEle.getDistanceMatrix();
			}
		}
		if (anasy){
			tempSyll=sg.copy(2);
			
			if (mdsNeeded){
				
				if (ac.mdsProvided){
					mdsSyll=ac.mdsSyll;
				}
				if (mdsSyll==null){
					mdsSyll=calculateMDS(tempSyll);
					ac.mdsProvided=true;
					ac.mdsSyll=mdsSyll;
				}
				tempSyll=mdsSyll.getDistanceMatrix();
				//sg.scoresSyll=tempSyll;
				//sg.compressSongs(useTransForSong, songUpperLimit, songLowerLimit);
			}
		}
		
		if (anast){
			tempSyTr=sg.copy(3);
			
			
			if (mdsNeeded){
				if (ac.mdsProvided){
					mdsSyTr=ac.mdsSyTr;
				}
				if (mdsSyTr==null){
					mdsSyTr=calculateMDS(tempSyTr);
					ac.mdsProvided=true;
					ac.mdsSyTr=mdsSyTr;
				}
				
				tempSyTr=mdsSyTr.getDistanceMatrix();
			}
		}
		
		if (anaso){
			tempSong=sg.copy(4);
			
			if (mdsNeeded){
				if (ac.mdsProvided){
					mdsSong=ac.mdsSong;
				}
				if (mdsSong==null){
					mdsSong=calculateMDS(tempSong);
					ac.mdsProvided=true;
					ac.mdsSong=mdsSong;
				}
				
				tempSong=mdsSong.getDistanceMatrix();
			}
		}
		if (matrixcomp){
			updateProgressLabel("drawing distance matrix");
			
			if (anael){
				if (anacomp){
					dsE=new DisplaySimilarity(tempEle, 1, sg, dbc, xd , yd, defaults);
				}
				else{
					dsE=new DisplaySimilarity(tempEle, 0, sg, dbc, xd , yd, defaults);
				}
			}	
			if (anasy){
				dsSy=new DisplaySimilarity(tempSyll, 2, sg, dbc, xd , yd, defaults);
				//int[] popId=sg.getPopulationListArray(2);
				//MRPP mrpp=new MRPP(sg.scoresSyll, popId);
			}
			if (anast){
				dsSt=new DisplaySimilarity(tempSyTr, 3, sg, dbc, xd , yd, defaults);
			}
			if (anaso){
				dsSo=new DisplaySimilarity(tempSong, 4, sg, dbc, xd , yd, defaults);
				//int[] popId=sg.getPopulationListArray(4);
				//MRPP mrpp=new MRPP(sg.scoresSong, popId);
			}
		}
		
		
		
		if (treecomp) {
			updateProgressLabel("calculating upgma trees");
						
			if (anael){
				if (anacomp){
					Object[] x=makeUPGMA(tempEle, mdsEle.getSDS(), 1, sg.getLabels(1));
					dup=(DisplayUPGMA)x[0];
					denpel=(DendrogramPanel)x[1];
				}
				else{
					Object[] x=makeUPGMA(tempEle, mdsEle.getSDS(), 0, sg.getLabels(0));
					dup=(DisplayUPGMA)x[0];
					denpel=(DendrogramPanel)x[1];
				}		
			}	
			if (anasy){
				Object[] x=makeUPGMA(tempSyll, mdsSyll.getSDS(), 2, sg.getLabels(2));
				dupsy=(DisplayUPGMA)x[0];
				denpsy=(DendrogramPanel)x[1];
			}
			if (anast){
				Object[] x=makeUPGMA(tempSyTr, mdsSyTr.getSDS(), 3, sg.getLabels(3));
				dupst=(DisplayUPGMA)x[0];
				denpst=(DendrogramPanel)x[1];
			}
			if (anaso){
				Object[] x=makeUPGMA(tempSong, mdsSong.getSDS(), 4, sg.getLabels(4));
				dupso=(DisplayUPGMA)x[0];
				denpso=(DendrogramPanel)x[1];
			}
		}	
		
		if (geogcomp){
			updateProgressLabel("calculating geographic comparisons:");
			if (anael){
				//geoEl=new GeographicComparison(sg, 0, geogPropLimit);
				if (dup==null){
					if (anacomp){
						Object[] x=makeUPGMA(tempEle, mdsEle.getSDS(), 1, sg.getLabels(1));
						dup=(DisplayUPGMA)x[0];
					}
					else{
						Object[] x=makeUPGMA(tempEle, mdsEle.getSDS(), 0, sg.getLabels(0));
						dup=(DisplayUPGMA)x[0];
					}	
				}
				geoEl=new GeographicComparison(sg, 0, dup);
				progress();
				dgcEl=new DisplayGeographicComparison(geoEl, (int)dim.getWidth(), (int)dim.getHeight(), sg, defaults);
			}
			if (anasy){
				//geoSyl=new GeographicComparison(sg, 2, geogPropLimit);
				if (dupsy==null){
					Object[] x=makeUPGMA(tempSyll, mdsSyll.getSDS(), 2, sg.getLabels(2));
					dupsy=(DisplayUPGMA)x[0];
				}
				geoSyl=new GeographicComparison(sg, 2, dupsy);
				progress();
				dgcSyll=new DisplayGeographicComparison(geoSyl, (int)dim.getWidth(), (int)dim.getHeight(), sg, defaults);
			}
			if (anast){
				//geoST=new GeographicComparison(sg, 3, geogPropLimit);
				if (dupst==null){
					Object[] x=makeUPGMA(tempSyTr, mdsSyTr.getSDS(), 3, sg.getLabels(3));
					dupst=(DisplayUPGMA)x[0];
				}
				geoST=new GeographicComparison(sg, 3, dupst);
				progress();
				dgcST=new DisplayGeographicComparison(geoST, (int)dim.getWidth(), (int)dim.getHeight(), sg, defaults);
			}
			if (anaso){
				//geoSong=new GeographicComparison(sg, 4, geogPropLimit);
				if (dupso==null){
					Object[] x=makeUPGMA(tempSong, mdsSong.getSDS(), 4, sg.getLabels(4));
					dupso=(DisplayUPGMA)x[0];
				}
				geoSong=new GeographicComparison(sg, 4, dupso);
				progress();
				dgcSong=new DisplayGeographicComparison(geoSong, (int)dim.getWidth(), (int)dim.getHeight(), sg, defaults);
			}
			
		}
		
		if (hopcomp){
			if (anael){
				CalculateHopkinsStatistic chs=new CalculateHopkinsStatistic(mdsEle.getConfiguration(), 1000, 1);
				progress();
				ds.addHopkins(chs);
			}
			if (anasy){
				CalculateHopkinsStatistic chs=new CalculateHopkinsStatistic(mdsSyll.getConfiguration(), 1000, 2);
				progress();
				ds.addHopkins(chs);
			}
			if (anast){
				CalculateHopkinsStatistic chs=new CalculateHopkinsStatistic(mdsSyTr.getConfiguration(), 1000, 3);
				progress();
				ds.addHopkins(chs);
			}
			if (anaso){
				CalculateHopkinsStatistic chs=new CalculateHopkinsStatistic(mdsSong.getConfiguration(), 1000, 4);
				progress();
				ds.addHopkins(chs);
			}
		}
		if(distfunc){
			if (anael){
				if(anacomp){
					DistanceNeighborFunctions dnf=new DistanceNeighborFunctions(tempEle, 1);
					progress();
					ds.addDistFunc(dnf);
				}
				else{
					DistanceNeighborFunctions dnf=new DistanceNeighborFunctions(tempEle, 0);
					progress();
					ds.addDistFunc(dnf);
				}
			}
			if (anasy){
				DistanceNeighborFunctions dnf=new DistanceNeighborFunctions(tempSyll, 2);
				progress();
				ds.addDistFunc(dnf);
			}
			if (anast){
				DistanceNeighborFunctions dnf=new DistanceNeighborFunctions(tempSyTr, 3);
				progress();
				ds.addDistFunc(dnf);
			}
			if (anaso){
				DistanceNeighborFunctions dnf=new DistanceNeighborFunctions(tempSong, 4);
				progress();
				ds.addDistFunc(dnf);
			}
			
			
			
		}
		if (andcomp){
			sg.calculateIndividuals();
			if (anael){
				if(anacomp){
					MultivariateDispersionTest mdt=new MultivariateDispersionTest(tempEle, sg.getPopulationListArray(0), 1, sg.getPopulations(), sg.getIndivData(1));
					progress();
					ds.addAnderson(mdt);
				}
				else{
					MultivariateDispersionTest mdt=new MultivariateDispersionTest(tempEle, sg.getPopulationListArray(0), 0, sg.getPopulations(), sg.getIndivData(0));
					progress();
					ds.addAnderson(mdt);
				}
			}
			if (anasy){
				MultivariateDispersionTest mdt=new MultivariateDispersionTest(tempSyll, sg.getPopulationListArray(2), 2, sg.getPopulations(), sg.getIndivData(2));
				progress();
				ds.addAnderson(mdt);
			}
			if (anast){
				MultivariateDispersionTest mdt=new MultivariateDispersionTest(tempSyTr, sg.getPopulationListArray(3), 3, sg.getPopulations(), sg.getIndivData(3));
				progress();
				ds.addAnderson(mdt);
			}
			if (anaso){
				MultivariateDispersionTest mdt=new MultivariateDispersionTest(tempSong, sg.getPopulationListArray(4), 4, sg.getPopulations(), sg.getIndivData(4));
				progress();
				ds.addAnderson(mdt);
			}
		}
		
		if (clustcomp){
			if (anael){
				kmEle=new KMedoids(tempEle, minClusterK, maxClusterK, 1, mdsEle.getSDS(), 10);
				if (popcomp){
					int[] popIds=sg.getPopulationListArray(0);
					int[] specIds=sg.getSpeciesListArray(0);
					kmEle.runMRPP(popIds,specIds, tempEle);
					System.out.println(maxClusterK);
					for (int i=minClusterK; i<maxClusterK; i++){
						CompositionAnalyzer compa=new CompositionAnalyzer(kmEle, i-minClusterK, sg, i, 1, tempEle);
					}
				}
				progress();
				ds.addCluster(kmEle);
			}
			if (anasy){
				//kmSyll=new KMedoids(tempSyll, minClusterK, maxClusterK, 2, mdsSyll.sds);
				kmSyll=new KMedoids(tempSyll, minClusterK, maxClusterK, 2, mdsSyll.getSDS(), 10);
				if (popcomp){
					int[] popIds=sg.getPopulationListArray(2);
					int[] specIds=sg.getSpeciesListArray(2);
					kmSyll.runMRPP(popIds, specIds, tempSyll);
					//kmSyll.runMRPP(popIds, sg.scoresSyll);
					System.out.println(maxClusterK);
					for (int i=minClusterK; i<maxClusterK; i++){
						CompositionAnalyzer compa=new CompositionAnalyzer(kmSyll, i-minClusterK, sg, i, 2, tempSyll);
					}
				}
				progress();
				ds.addCluster(kmSyll);
			}
			if (anast){
				kmSyTr=new KMedoids(tempSyTr, minClusterK, maxClusterK, 3, mdsSyTr.getSDS(),0);
				if (popcomp){
					int[] popIds=sg.getPopulationListArray(3);
					int[] specIds=sg.getSpeciesListArray(3);
					kmSyTr.runMRPP(popIds,specIds, tempSyTr);
				}
				progress();
				ds.addCluster(kmSyTr);
			}
			if (anaso){
				kmSong=new KMedoids(tempSong, minClusterK, maxClusterK, 4, mdsSong.getSDS(),0);
				if (popcomp){
					int[] popIds=sg.getPopulationListArray(4);
					int[] specIds=sg.getSpeciesListArray(4);
					kmSong.runMRPP(popIds, specIds,tempSong);
				}
				progress();
				ds.addCluster(kmSong);
			}
		}
		
		if (distcomp){
			updateProgressLabel("drawing distance distributions");
			
			if (anael){
				if (anacomp){
					dspE=new DisplaySimilarityProportions(sg, kmEle, 1, (int)dim.getWidth(), (int)dim.getHeight(), defaults);
				}
				else{
					dspE=new DisplaySimilarityProportions(sg, kmEle, 0, (int)dim.getWidth(), (int)dim.getHeight(), defaults);				
				}
				
			}	
			if (anasy){
				dspSy=new DisplaySimilarityProportions(sg, kmSyll, 2, (int)dim.getWidth(), (int)dim.getHeight(), defaults);
			}
			if (anast){
				dspSy=new DisplaySimilarityProportions(sg, kmSyTr, 3, (int)dim.getWidth(), (int)dim.getHeight(), defaults);
			}
			if (anaso){
				dspSo=new DisplaySimilarityProportions(sg, kmSong, 4, (int)dim.getWidth(), (int)dim.getHeight(), defaults);
			}
		}
		if (snncomp){
			if (anael){
				snnEle=new SNNDensity(tempEle, snnK, snnEps, snnMinPts, 0);
				if (popcomp){
					int[] popIds=sg.getPopulationListArray(0);
					snnEle.runMRPP(popIds, tempEle);
				}
				progress();
				ds.addSNNCluster(snnEle);
			}
			if (anasy){
				//kmSyll=new KMedoids(tempSyll, minClusterK, maxClusterK, 2, mdsSyll.sds);
				snnSyll=new SNNDensity(tempSyll, snnK, snnEps, snnMinPts, 2);
				if (popcomp){
					int[] popIds=sg.getPopulationListArray(2);
					snnSyll.runMRPP(popIds, tempSyll);
					//kmSyll.runMRPP(popIds, sg.scoresSyll);
				}
				progress();
				ds.addSNNCluster(snnSyll);
			}
			if (anast){
				snnSyTr=new SNNDensity(tempSyTr, snnK, snnEps, snnMinPts, 3);
				if (popcomp){
					int[] popIds=sg.getPopulationListArray(3);
					snnSyTr.runMRPP(popIds, tempSyTr);
				}
				progress();
				ds.addSNNCluster(snnSyTr);
			}
			if (anaso){
				snnSong=new SNNDensity(tempSong, snnK, snnEps, snnMinPts, 4);
				if (popcomp){
					int[] popIds=sg.getPopulationListArray(4);
					snnSong.runMRPP(popIds, tempSong);
				}
				progress();
				ds.addSNNCluster(snnSong);
			}
		}
		
		if (syntcomp){
			sg.calculateIndividuals();
			if (anael){
				entEle=new EntropyAnalysis(tempEle, maxSyntaxK, sg.getIndivData(4), sg.getLookUp(0), 1, syntaxMode);	
				progress();
				ds.addSyntax(entEle);
			}
			if (anasy){
				//float[][]test=simulateMatrix(mdsSyll.sds, sg.scoresSyll.length, false);
				
				//entSyll=new EntropyAnalysis(tempSyll, maxSyntaxK, sg.individuals, sg.lookUpSyls, 2, syntaxMode);
				entSyll=new EntropyAnalysis(tempSyll, maxSyntaxK, sg.getIndivData(4), sg.getLookUp(2), 2, syntaxMode);
				//entSyll=new EntropyAnalysis(test, maxSyntaxK, sg.individualSongs, sg.lookUpSyls, 2, syntaxMode);
				progress();
				ds.addSyntax(entSyll);
				EntropyPopulationComp epc=new EntropyPopulationComp(sg, 2, maxSyntaxK);
			}
		}
		
		if (mdscomp){
			
			if (anael){
				dpcEle=new DisplayPC(mdsEle, sg, kmEle, entEle, snnEle, 1, (int)dim.getWidth(), (int)dim.getHeight(), defaults);
			}	
			if (anasy){
				dpcSyll=new DisplayPC(mdsSyll, sg, kmSyll, entSyll, snnSyll, 2, (int)dim.getWidth(), (int)dim.getHeight(), defaults);
				
			}
			if (anast){
				
				dpcSyTr=new DisplayPC(mdsSyTr, sg, kmSyTr, null, snnSyTr, 3, (int)dim.getWidth(), (int)dim.getHeight(), defaults);
			}
			if (anaso){
				dpcSong=new DisplayPC(mdsSong, sg, kmSong, null, snnSong, 4, (int)dim.getWidth(), (int)dim.getHeight(), defaults);
			}
			
		}
	}
	
	public void displayComparisons(){
		tabPane=new JTabbedPane();
		
		if (matrixcomp){			
			if (anael){
				tabPane.addTab("Element Similarity Matrix", dsE);			
			}	
			if (anasy){
				tabPane.addTab("Syllable Similarity Matrix", dsSy);
			}
			if (anast){
				tabPane.addTab("Syllable Transition Similarity Matrix", dsSt);
			}
			if (anaso){
				tabPane.addTab("Song Similarity Matrix", dsSo);
			}
		}
		
		if ((syntcomp)||(clustcomp)||(hopcomp)||(andcomp)||(distfunc)){
			
			tabPane.addTab("Statistics", ds);
		}
		
		
		if (distcomp){		
			if (anael){
				tabPane.addTab("Element distance distribution", dspE); 
			}	
			if (anasy){
				tabPane.addTab("Syllable distance distribution", dspSy);
			}
			if (anast){
				tabPane.addTab("Syllable Transition distance distribution", dspSt);
			}
			if (anaso){
				tabPane.addTab("Song distance distribution", dspSo);
			}
		}
		
		if (mdscomp){
			if (anael){
				tabPane.addTab("Element mds", dpcEle);
			}	
			if (anasy){
				tabPane.addTab("Syllable mds", dpcSyll);
			}
			if (anast){
				tabPane.addTab("Syllable Transition mds", dpcSyTr);
				
			}
			if (anaso){
				tabPane.addTab("Song mds", dpcSong);
			}
		}
		if (treecomp) {
			if (anael){
				tabPane.addTab("Element dend", dup);
				tabPane.addTab("Ele dendrogram", denpel);
			}	
			if (anasy){
				tabPane.addTab("Syllable dend", dupsy);
				tabPane.addTab("Syll dendrogram", denpsy);
			}
			if (anast){
				tabPane.addTab("Syllable Transition dend", dupst);
				tabPane.addTab("Trans dendrogram", denpst);
				//tabPane.addTab("Prop dendrogram", denpsa);
			}
			if (anaso){
				tabPane.addTab("Song dend", dupso);
				tabPane.addTab("Song dendrogram", denpso);
			}
		}

		if (geogcomp){
			if (anael){
				tabPane.addTab("El. Geographic Anal.", dgcEl);
			}	
			if (anasy){
				tabPane.addTab("Syl. Geographic Anal.", dgcSyll);
			}
			if (anast){
				tabPane.addTab("Trans. Geographic Anal.", dgcST);
			}
			if (anaso){
				tabPane.addTab("Song Geographic Anal.", dgcSong);
			}
		}
		
		JButton save=new JButton("Save Image");
		save.setActionCommand(SAVE_IMAGE);
		save.addActionListener(this);
		JButton export=new JButton("Export data");
		export.setActionCommand(EXPORT);
		export.addActionListener(this);
		
		JPanel toppanel=new JPanel();
		toppanel.add(save);
		toppanel.add(export);
		resultsPanel=new JPanel(new BorderLayout());
		resultsPanel.setLayout(new BorderLayout());
		resultsPanel.add(toppanel, BorderLayout.NORTH);
		resultsPanel.add(tabPane, BorderLayout.CENTER);
		JScrollPane sp=new JScrollPane(sg.getSSB(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		resultsPanel.add(sp, BorderLayout.EAST);
	}
	
	public void updateProgressLabel(String s){
		progressLabel.setText(s);
		Rectangle rect=progressLabel.getBounds();
		rect.x=0;
		rect.y=0;
		progressLabel.paintImmediately(rect);
	}
	
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		
		DisplayPane gp=(DisplayPane)tabPane.getSelectedComponent();
		if (SAVE_IMAGE.equals(command)) {
			gp.saveImage();
		}
		else if (EXPORT.equals(command)) {
			gp.export();
		}
	}
	
		
	public MultiDimensionalScaling calculateMDS(float[][] input){
		
		int tndi=ndi;
		if (input.length<=tndi){
			tndi=input.length-1;
		}
		MultiDimensionalScaling mds=new MultiDimensionalScaling();
		
		try{
			mds.RunNonMetricAnalysis(input, tndi, this);
		}
		catch(Exception e){e.printStackTrace();}
		return mds;
	}
			
		
	
	public Object[] makeUPGMA(float[][] input, double[] sds, int type, double[] labels){
		UPGMA upgma=new UPGMA(input, dendrogramMode);
		progress();
		ClusterValidation cv=new ClusterValidation(upgma, input, dendrogramMode);
		progress();
		double[][] sil=cv.silhouettePValue(upgma, input, sds);
		double[] sil1=sil[0];
		double [] sil3=cv.calculateWithinClusterDistance(upgma, input);
		double[][] avsil=new double[3][];
		avsil[0]=sil[0];
		//avsil[1]=cv.resamplingMethod(500, upgma, input);
		avsil[1]=sil[1];
		avsil[2]=cv.getAverageClusterV(sil3, true, upgma);
		
		DisplayUPGMA du=new DisplayUPGMA(upgma, sg, type, (int)dim.getWidth(), (int)dim.getHeight(), sil1, avsil, defaults);
		
		Dendrogram dend=new Dendrogram(input, dendrogramMode);
		dend.calcAverages(labels);
		
		progress();
		DendrogramPanel den=new DendrogramPanel (dend, sg, type, (int)dim.getWidth(), (int)dim.getHeight(), sil1, defaults);
		Object[] x={du, den};
		return x;
	}
	
	public float[][] simulateMatrix(double[] sds, int n, boolean type){
		Random random=new Random(System.currentTimeMillis());
		float[][] out=new float[n][];
		
		for (int i=0; i<n; i++){
			out[i]=new float[i+1];
		}
		
		double[][] pcs=new double[n][sds.length];
		
		
		for(int i=0; i<n; i++){
			for (int j=0; j<sds.length; j++){
				if (type){
					pcs[i][j]=random.nextGaussian()*sds[j];
				}
				else{
					pcs[i][j]=random.nextDouble()*sds[j];
				}
			}
		}
		
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				double t=0;
				double s=0;
				for (int k=0; k<sds.length; k++){
					s=pcs[i][k]-pcs[j][k];
					t+=s*s;
				}
				out[i][j]=(float)Math.sqrt(t);
			}
		}
		
		pcs=null;
		
		return(out);
	}
	
	
	public void progress(){
		int p=(int)Math.round(100*currentLevel/(analysisSteps+0.0));
		System.out.println("STEP COMPLETED" +p);
		currentLevel++;
		Integer prog=new Integer(p);
		firePropertyChange("progress", null, prog);
	}
}