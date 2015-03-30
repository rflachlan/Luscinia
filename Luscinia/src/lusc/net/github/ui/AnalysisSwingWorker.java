package lusc.net.github.ui;


import javax.swing.*;

import java.awt.event.*;
import java.awt.*;
import java.text.*;
import java.util.*;

import lusc.net.github.Defaults;
import lusc.net.github.analysis.*;
import lusc.net.github.analysis.clustering.ClusterValidation;
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
	
	String[] s={"Element", "Element", "Syllable", "Syllable Transition", "Song", "Individual"};
	
	ComparisonResults[] comps=new ComparisonResults[6];
	boolean[] levels=new boolean[6];
	
	GeographicComparison[] geo=new GeographicComparison[6];
	DisplayPane[] dp=new DisplayPane[6];
	DisplayGeographicComparison[] dgc=new DisplayGeographicComparison[6];
	DisplayUPGMA dup[]=new DisplayUPGMA[6];
	DendrogramPanel denp[]=new DendrogramPanel[6];
	KMedoids km[]=new KMedoids[6];
	DisplaySimilarityProportions[] dsp=new DisplaySimilarityProportions[6];
	SNNDensity[] snn=new SNNDensity[6];
	EntropyAnalysis[] ent=new EntropyAnalysis[6];
	DisplayPC[] dpc=new DisplayPC[6];
	
	DisplaySummary ds;
	
	private static String SAVE_IMAGE = "save image";
	private static String EXPORT = "export";	
	
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
	
	//DisplayPane dsE, dsSy, dsSt, dsSo, dsInd;

	
	
	
	
	
	
	
	
	int pcsUsed=2;
	
	int xd, yd;
	boolean mdsNeeded;
	
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
		
		
		
		
		levels[0]=sop.element.isSelected();
		levels[1]=sop.elementCompression.isSelected();
		if (levels[1]&&levels[0]){levels[0]=false;}
		levels[2]=sop.syllable.isSelected();
		levels[3]=sop.syllableTransition.isSelected();
		levels[4]=sop.song.isSelected();
		levels[5]=sop.individual.isSelected();
		
		analysisLevels=0;
		for (int i=0; i<6; i++){
			if (levels[i]){analysisLevels++;}
		}
		
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
		if ((treecomp)||(mdscomp)||(clustcomp)||(hopcomp)||(geogcomp)){mdsNeeded=true;}
		

		
		
		analysisSteps=0;
		if (mdsNeeded){analysisSteps+=3;}
		if (treecomp){analysisSteps+=3;}
		if (geogcomp){analysisSteps++;}
		if (clustcomp){analysisSteps++;}
		if (hopcomp){analysisSteps++;}
		if (syntcomp){analysisSteps++;}
		
		analysisSteps*=analysisLevels;
		if (syntcomp){
			if (levels[4]){analysisSteps--;}
			if (levels[5]){analysisSteps--;}
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
		
		
		
		System.gc();
		System.out.println("Heap size is " + Runtime.getRuntime().totalMemory());
		System.out.println("Available memory: " + Runtime.getRuntime().freeMemory());
	}
	
	public String doInBackground(){
		
		try{
			compressResults();
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

		if (levels[0]){
			comps[0]=sg.getScores(0);
		}
		if (levels[1]){
			sg.compressElements();
			comps[1]=sg.getScores(1);
		}
		if (levels[2]){
			comps[2]=sg.getScores(2);
		}
		if ((levels[3])||(levels[4])||(levels[5])){	
			sg.compressSyllableTransitions();
			comps[3]=sg.getScores(3);
		}
		if ((levels[4])||(levels[5])){
			sg.compressSongs(useTransForSong, songUpperLimit, songLowerLimit);
			comps[4]=sg.getScores(4);
		}
		System.out.println("levels5: "+ levels[5]);
		if (levels[5]){
			sg.compressIndividuals();
			comps[5]=sg.getScores(5);
		}
		
		currentLevel=1;
		progress();
	}	
	
	
	public void analyze(){
				
		if ((clustcomp)||(snncomp)||(syntcomp)||(hopcomp)||(andcomp)||(distfunc)){ds=new DisplaySummary(defaults);}
		
		if (mdsNeeded){
			for (int i=0; i<6; i++){
				if (levels[i]){
					comps[i].checkMakeMDS(ndi, this);
				}
			}
		}
		
		if (matrixcomp){
			updateProgressLabel("drawing distance matrix");
			for (int i=0; i<levels.length; i++){
				if (levels[i]){
					//dp[i]=new DisplaySimilarity(comps[i].getDiss(), i, sg, dbc, xd, yd, defaults);
					dp[i]=new DisplaySimilarity(comps[i], sg.getSSB(), dbc, xd, yd, defaults);
					//MRPP mrpp=new MRPP(sg.scoresSyll, popId);
				}
			}
			
		}

		if ((treecomp)||(geogcomp)) {
			updateProgressLabel("calculating dendrograms");
			
			for (int i=0; i<levels.length; i++){
				if (levels[i]){
					Object[] x=makeUPGMA(comps[i]);
					dup[i]=(DisplayUPGMA)x[0];
					denp[i]=(DendrogramPanel)x[1];
				}
			}	
		}	
		
		if (geogcomp){
			updateProgressLabel("calculating geographic comparisons:");
			for (int i=0; i<levels.length; i++){
				if (levels[i]){
					geo[i]=new GeographicComparison(sg, i, dup[i]);
					progress();
					dgc[i]=new DisplayGeographicComparison(geo[i], (int)dim.getWidth(), (int)dim.getHeight(), sg, defaults);
				}
			}
		}		
		
		if (hopcomp){
			updateProgressLabel("calculating Hopkins:");
			for (int i=0; i<levels.length; i++){
				if (levels[i]){
					CalculateHopkinsStatistic chs=new CalculateHopkinsStatistic(comps[i].getMDS().getConfiguration(), 1000, i);
					progress();
					ds.addHopkins(chs);
				}
			}
		}
		
		if(distfunc){
			updateProgressLabel("calculating distance functions:");
			for (int i=0; i<levels.length; i++){
				if (levels[i]){
					DistanceNeighborFunctions dnf=new DistanceNeighborFunctions(comps[i].getDissT(), i);
					progress();
					ds.addDistFunc(dnf);
				}
			}
		}
			
		if (andcomp){
			updateProgressLabel("calculating multivariate dispersion:");
			for (int i=0; i<levels.length; i++){
				if (levels[i]){
					MultivariateDispersionTest mdt=new MultivariateDispersionTest(comps[i]);
					progress();
					ds.addAnderson(mdt);
				}
			}
		}	
		
		if ((clustcomp)||(distcomp)){
			updateProgressLabel("cluster analysis:");
			for (int i=0; i<levels.length; i++){
				if (levels[i]){
					km[i]=new KMedoids(comps[i].getDissT(), minClusterK, maxClusterK, i, comps[i].getMDS().getSDS(), 10);
					if ((i<3)&&(popcomp)){
						int[] popIds=comps[i].getPopulationListArray();
						int[] specIds=comps[i].getSpeciesListArray();
						km[i].runMRPP(popIds,specIds, comps[i].getDissT());
						for (int j=minClusterK; j<maxClusterK; j++){
							CompositionAnalyzer compa=new CompositionAnalyzer(km[i], j-minClusterK, sg, j, i, comps[i].getDissT());
						}
					}
					ds.addCluster(km[i]);
				}
			}
			progress();
			
		}
		
		if (distcomp){
			updateProgressLabel("drawing distance distributions");
			
			for (int i=0; i<levels.length; i++){
				if (levels[i]){
					dsp[i]=new DisplaySimilarityProportions(sg, km[i], i, (int)dim.getWidth(), (int)dim.getHeight(), defaults);
				}
			}
		}
		
		if (snncomp){
			updateProgressLabel("SNN clustering");
			
			for (int i=0; i<levels.length; i++){
				if (levels[i]){
					snn[i]=new SNNDensity(comps[i].getDissT(), snnK, snnEps, snnMinPts, i);
					if (popcomp){
						snn[i].runMRPP(comps[i].getPopulationListArray(), comps[i].getDissT());
					}
					progress();
					ds.addSNNCluster(snn[i]);
				}	
			}
		}	
		
		if (syntcomp){
			for (int i=0; i<4; i++){
				if (levels[i]){
					//ent[i]=new EntropyAnalysis(comps[i].getDissT(), maxSyntaxK, sg.getIndivData(4), comps[i].getLookUp(), i, syntaxMode);	
					ent[i]=new EntropyAnalysis(comps[i], maxSyntaxK, syntaxMode);	
					progress();
					ds.addSyntax(ent[i]);
					//EntropyPopulationComp epc=new EntropyPopulationComp(comps[i], maxSyntaxK);
				}
			}
		}	
			
		if (mdscomp){
			for (int i=0; i<levels.length; i++){
				if (levels[i]){
					dpc[i]=new DisplayPC(comps[i], sg.getSSB(), km[i], ent[i], snn[i], i, (int)dim.getWidth(), (int)dim.getHeight(), defaults);
				}
			}
		}
	}
	
	public void displayComparisons(){
		tabPane=new JTabbedPane();
		
		if ((syntcomp)||(clustcomp)||(hopcomp)||(andcomp)||(distfunc)){
			tabPane.addTab("Statistics", ds);
		}
		
		if (matrixcomp){
			for (int i=0; i<levels.length; i++){
				if (levels[i]){
					String t=s[i]+" Similarity Matrix";
					tabPane.addTab(t, dp[i]);
				}
			}
		}

		if (distcomp){	
			for (int i=0; i<levels.length; i++){
				if (levels[i]){
					String t=s[i]+" distance distribution";
					tabPane.addTab(t, dsp[i]);
				}
			}
		}
		
		if (mdscomp){
			for (int i=0; i<levels.length; i++){
				if (levels[i]){
					String t=s[i]+" mds";
					tabPane.addTab(t, dpc[i]);
				}
			}
		}
		
		if (treecomp) {
			for (int i=0; i<levels.length; i++){
				if (levels[i]){
					String t=s[i]+" dend";
					tabPane.addTab(t, dup[i]);
					String u=s[i]+" dendrogram";
					tabPane.addTab(u, denp[i]);
				}
			}
		}

		if (geogcomp){
			for (int i=0; i<levels.length; i++){
				if (levels[i]){
					String t=s[i]+" Geog. Anal.";
					tabPane.addTab(t, dgc[i]);
				}
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
	
	public Object[] makeUPGMA(ComparisonResults cr){
		double[] sds=cr.getMDS().getSDS();
		UPGMA upgma=new UPGMA(cr.getDissT(), dendrogramMode);
		progress();
		ClusterValidation cv=new ClusterValidation(dendrogramMode);
		progress();
		double[][] sil=cv.silhouettePValue(upgma, cr.getDissT(), sds);
		double[] sil1=sil[0];
		double [] sil3=cv.calculateWithinClusterDistance(upgma, cr.getDissT());
		double[][] avsil=new double[3][];
		avsil[0]=sil[0];
		//avsil[1]=cv.resamplingMethod(500, upgma, input);
		avsil[1]=sil[1];
		avsil[2]=cv.getAverageClusterV(sil3, true, upgma);
		
		DisplayUPGMA du=new DisplayUPGMA(upgma, cr, sg, (int)dim.getWidth(), (int)dim.getHeight(), sil1, avsil, defaults);
		
		Dendrogram dend=new Dendrogram(cr.getDissT(), dendrogramMode);
		dend.calcAverages(cr.getPosition());
		
		progress();
		DendrogramPanel den=new DendrogramPanel (dend, sg, cr.getType(), (int)dim.getWidth(), (int)dim.getHeight(), sil1, defaults);
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