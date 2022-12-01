package lusc.net.github.ui;


import lusc.net.github.*;
//import lusc.net.github.analysis.SongGroup;
import lusc.net.github.analysis.AnalysisGroup;
import lusc.net.github.analysis.multivariate.MultiDimensionalScaling;
import lusc.net.github.db.DataBaseController;
import lusc.net.github.ui.compmethods.ABXdiscrimination;
import lusc.net.github.ui.compmethods.DTWPanel;
import lusc.net.github.ui.compmethods.DTWSwingWorker;
import lusc.net.github.ui.compmethods.ParameterPanel;
import lusc.net.github.ui.compmethods.SimpleVisualComparison;
import lusc.net.github.ui.compmethods.VisualAnalysisPane;
import lusc.net.github.ui.compmethods.VisualComparison;
import lusc.net.github.ui.db.DatabaseTree2;
import lusc.net.github.ui.db.myNode;
import lusc.net.github.ui.db.DatabaseView.ByDate;
import lusc.net.github.ui.db.DatabaseView.HasSylls;

import java.awt.*;

import javax.swing.*;

import java.awt.BorderLayout;

import javax.swing.BorderFactory; 
import javax.swing.border.Border;
import javax.swing.event.*;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import java.util.*;
import java.io.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class AnalysisChoose extends JPanel implements ActionListener, ChangeListener, WindowListener, PropertyChangeListener {

	/**
	 * AnalysisChoose is the root of the analysis 'wizard' that steps through the analysis process.
	 * Analysis proceeds in a step-like architecture, first loading up sounds from the database,
	 * then deciding which type of analyses to run, then setting parameters for the analysis, and 
	 * finally running statistical analyses on the results of the analysis (sometimes). This stepwise
	 * method is a bit limiting and may be changed in the future.
	 */
	private static final long serialVersionUID = 1L;
	private static String EXPAND_COMMAND="expand";
	private static String ANALYSE_ALL="show all";
	private static String NEXTSTEP="next";
	private static String STOPCOMMAND="stop";
	private static String MDS_VIEW="mds view";
	private static String DENDROGRAM_VIEW="dendrogram view";
	
	JRadioButton dtwAnalysis=new JRadioButton("Computer comparison by time warping");
	JRadioButton parAnalysis=new JRadioButton("Comparison by parameter");
	JRadioButton visAnalysis=new JRadioButton("Comparison by inspection");
	JRadioButton discAnalysis=new JRadioButton("Discrimination analysis");
	JRadioButton dParAnalysis=new JRadioButton("Export parameter statistics to spreadsheet");
	JRadioButton dTestDB=new JRadioButton("Check database measurements");
	JRadioButton dSplitSongs=new JRadioButton("Split songs into phrases");
	JRadioButton dExportWavs=new JRadioButton("Export sound files");
	
	int analysisMode=0;
	
	ButtonGroup bg=new ButtonGroup();
	
	int schemeType=0;
	boolean collapsed=true;
	
    protected DatabaseTree2 treePanel;
	LinkedList<String[]> tstore=new LinkedList<String[]>();
	LinkedList<Song> ustore=new LinkedList<Song>();
	String query=null;
	private int [] indq={1,2};
	//private int [] sonq={1,2,3};
	JButton expandTree, nextButton, stopButton;
	JCheckBox showAllSongsButton;
	JProgressBar progress=new JProgressBar(0, 100);
	
	int stepPosition=0;
	int finalStepPosition=4;
	
	File file;
	String thpath, name;
	String user;
	DataBaseController dbc;
	Defaults defaults;
	String[] archNams=null;
	String[] archIndNams=null;
	int[] archIds=null;
	
	//SongGroup sg;
	AnalysisGroup ag;
	ComparisonScheme cs;
	DTWPanel dtw;
	ParameterPanel pp;
	VisualAnalysisPane vap;
	VisualComparison visComp;
	ABXdiscrimination abx;
	SimpleVisualComparison simpVisComp;
	MeasurementSave measSave;
	CheckDatabase testDB;
	SplitSongs splitSongs;
	ExportSongs exportSongs;
	
	StatOptionPanel sop;
	AnalysisSwingWorker answ;
	ComplexAnalysisDownload cad;
	JPanel schemePanel, comparisonType;
	JTabbedPane mainPanel=new JTabbedPane();
	
	SongLoaderSwingWorker clsw;
	DTWSwingWorker dtwsw;

	boolean mdsProvided=false;
	
	boolean showAllSongs=false;
	//MultiDimensionalScaling mdsEle=null;
	//MultiDimensionalScaling mdsSyllTrue=null;
	//MultiDimensionalScaling mdsSyll=null;
	//MultiDimensionalScaling mdsSyTr=null;
	//MultiDimensionalScaling mdsSong=null;
	//MultiDimensionalScaling mdsInd=null;
	JFrame f;
	
	
	/**
	 * This constructer requires a database connection and a defaults object
	 * @param dbc DataBaseController providing a connection to a database
	 * @param defaults a Defaults object
	 */
	
	public AnalysisChoose(DataBaseController dbc, Defaults defaults) {
		this.dbc=dbc;
		this.defaults=defaults;
		analysisMode=defaults.getIntProperty("analmod", 0);
		user=dbc.getUserID();
		buildUI();
	}
		
	/**
	 * This constructor requires a database connection, a defaults object and a user
	 * @param dbc DataBaseController providing a connection to a database
	 * @param user a database user id
	 * @param defaults a Defaults object
	 */
    public AnalysisChoose(DataBaseController dbc, String user, Defaults defaults) {
		
       
        this.dbc=dbc;
		this.user=user;
		this.defaults=defaults;
		analysisMode=defaults.getIntProperty("analmod", 0);
		buildUI();
	}
	
    
    /**
     * buildUI is called from the constructor and creates the user interface to begin analysis
     */
    
	public void buildUI(){
		
		 this.setLayout(new BorderLayout());
		
        //Create the components.
		treePanel = new DatabaseTree2(this, dbc);
        populateTree();

		expandTree=new JButton("Expand Tree");
		expandTree.setActionCommand(EXPAND_COMMAND);
		expandTree.addActionListener(this);
		
		showAllSongsButton=new JCheckBox("Analyse all songs");
		showAllSongsButton.setSelected(showAllSongs);
		showAllSongsButton.setActionCommand(ANALYSE_ALL);	
		showAllSongsButton.addActionListener(this);
        //Lay everything out.
        treePanel.setPreferredSize(new Dimension(300, 600));
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		dim.setSize(dim.getWidth()-40, dim.getHeight()-80);
        this.setPreferredSize(dim);
		JPanel dbPane=new JPanel(new BorderLayout());
		Border empty1=BorderFactory.createEmptyBorder(20, 0, 20, 0);
		dbPane.setBorder(empty1);
		dbPane.add(treePanel, BorderLayout.CENTER);
		
		JPanel buttonPanel=new JPanel();
		buttonPanel.setLayout(new GridLayout(0,1));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(10,10,200, 10));
		buttonPanel.add(expandTree);
		buttonPanel.add(showAllSongsButton);
		//panel.add(compareComputer);
		//panel.add(syllablize);
		//panel.add(visualComparison);
		//panel.add(parameterOut);

		dbPane.add(buttonPanel, BorderLayout.EAST);
		
		cs=new ComparisonScheme(this, defaults);
				
		schemePanel=new JPanel (new BorderLayout());	
		
		schemePanel.add(cs, BorderLayout.CENTER);
		schemePanel.add(dbPane, BorderLayout.EAST);
		
		mainPanel.addTab("Scheme", schemePanel);
	
		this.add(mainPanel, BorderLayout.CENTER);
		
		JPanel navigationPanel= new JPanel (new BorderLayout());	
		
		Border empty=BorderFactory.createEmptyBorder(0, 50, 0, 50);
		navigationPanel.setBorder(empty);
		
		JPanel buttonPanel2=new JPanel(new GridLayout());
		nextButton=new JButton("Next step");
		nextButton.setActionCommand(NEXTSTEP);
		nextButton.addActionListener(this);
		
		
		stopButton=new JButton("Stop");
		stopButton.setActionCommand(STOPCOMMAND);
		stopButton.setEnabled(false);
		stopButton.addActionListener(this);
		
		mainPanel.addChangeListener(this);
				
		buttonPanel2.add(nextButton);
		buttonPanel2.add(progress);
		buttonPanel2.add(stopButton);
		
		navigationPanel.add(buttonPanel2, BorderLayout.WEST);
		
		this.add(navigationPanel, BorderLayout.SOUTH);
		
		comparisonType=new JPanel(new BorderLayout());
		
		bg.add(dtwAnalysis);
		bg.add(parAnalysis);
		bg.add(visAnalysis);
		bg.add(discAnalysis);
		bg.add(dParAnalysis);
		bg.add(dTestDB);
		bg.add(dSplitSongs);
		bg.add(dExportWavs);
		
		if (analysisMode==0){
			dtwAnalysis.setSelected(true);
		}
		else if (analysisMode==1){
			parAnalysis.setSelected(true);
		}
		else if (analysisMode==2){
			visAnalysis.setSelected(true);
		}
		else if (analysisMode==3){
			discAnalysis.setSelected(true);
		}
		else if (analysisMode==4){
			dParAnalysis.setSelected(true);
		}
		else if (analysisMode==5){
			dTestDB.setSelected(true);
		}
		else if (analysisMode==6){
			dSplitSongs.setSelected(true);
		}
		else {
			dExportWavs.setSelected(true);
		}
		
		JPanel rbgPanel=new JPanel(new GridLayout(0,1));
		
		rbgPanel.add(dtwAnalysis);
		rbgPanel.add(parAnalysis);
		rbgPanel.add(visAnalysis);
		rbgPanel.add(discAnalysis);
		rbgPanel.add(dParAnalysis);
		rbgPanel.add(dTestDB);
		rbgPanel.add(dSplitSongs);
		rbgPanel.add(dExportWavs);
		
		
		comparisonType.add(rbgPanel, BorderLayout.WEST);
		
		JMenuBar menuBar=new JMenuBar();
		JMenu menu=new JMenu("Display Options");

		JMenuItem MDSView=new JMenuItem("MDS display");
		MDSView.setActionCommand(MDS_VIEW);
		MDSView.addActionListener(this);
		menu.add(MDSView);
		
		JMenuItem DendrogramView=new JMenuItem("Dendrogram");
		DendrogramView.setActionCommand(DENDROGRAM_VIEW);
		DendrogramView.addActionListener(this);
		menu.add(DendrogramView);
		

		
		menuBar.add(menu);
		
		
		f=new JFrame("Analyze database");
		f.getContentPane().add(this);
		f.setJMenuBar(menuBar);
		f.pack();
		f.setVisible(true);
		
		
    }

	/**
	 * populateTree is called when the UI is being built (buildUI) and creates a view of the database.
	 * This allows users to select sounds from within the database to analyze.
	 */
	
    public void populateTree() {
    	treePanel.removeAllNodes();
		extractFromDatabase();
		myNode nullpar=new myNode("temp");
		for (int i=0; i<tstore.size(); i++){
			String []nam=tstore.get(i);
			myNode chile=treePanel.addObject(null, nam[0], true);
			chile.setDex(myIntV(nam[1]));
			if (nam[0]=="Undetermined"){nullpar=chile;}
		}
		
		int numind=treePanel.getRootNode().getChildCount();
		archIds=new int[ustore.size()];
		archNams=new String[ustore.size()];
		archIndNams=new String[ustore.size()];
		System.out.println("INCLUDE ALL: "+showAllSongs+" "+ustore.size());
		for (int i=0; i<ustore.size(); i++){
			Song nam=ustore.get(i);
			//nam=dbc.loadSongFromDatabase(nam.getSongID(), 0);
			//System.out.println(nam.getIndividualName()+" "+nam.getNumSyllables()+" "+nam.getNumElements());
			if (showAllSongs||(nam.getNumSylls()>0)){
			////if (nam.getNumSyllables()>0){IGNORE THIS
				archIds[i]=nam.getSongID();
				archNams[i]=nam.getName();

				int par=nam.getIndividualID();
				boolean found=false;
				for (int j=0; j<numind; j++){
					myNode posspar=(myNode)treePanel.getRootNode().getChildAt(j);
					if (posspar.getDex()==par){
						found=true;
						String [] nam2=tstore.get(j);
						archIndNams[i]=nam2[0];
					
						myNode chile=treePanel.addObject(posspar, nam.getName(), true);
						chile.setDex(nam.getSongID());
						j=numind;
					}
				}
				if (!found){
					myNode chile=treePanel.addObject(nullpar, nam.getName(), true);
					chile.setDex(nam.getSongID());
				}
			}
		}
    }
	
	public int myIntV(String s){
		Integer p1=Integer.valueOf(s);
		int p=p1.intValue();
		return p;
	}
	
	/**
	 * This method connects to the database and extracts individuals and sound names for the populateTree method.
	 */
	
	public void extractFromDatabase(){
		tstore=null;
		tstore=new LinkedList<String[]>();
		String [] nulleg={"Undetermined", "-1"};
		tstore.add(nulleg);
		tstore.addAll(dbc.readFromDataBase("SELECT name, id FROM individual", indq));
		String[] s;
		String seg;
		int loc=0;
		for (int i=0; i<tstore.size(); i++){
			loc=i;
			s=tstore.get(i);
			seg=s[0];
			for (int j=i+1; j<tstore.size(); j++){
				s=tstore.get(j);
				if (seg.compareToIgnoreCase(s[0])<0){
					seg=s[0];
					loc=j;
				}
			}
			s=tstore.get(loc);
			tstore.remove(loc);
			tstore.add(0, s);
		}
		ustore=null;
		ustore=new LinkedList<Song>();
		ustore=dbc.loadSongDetailsFromDatabase();
		Collections.sort(ustore, new ByDate());
		Collections.sort(ustore, new HasSylls());
	}
	
	public class ByAlphabet implements Comparator<Song> {
		@Override
		public int compare(Song a, Song b) {
		    return a.getName().compareTo(b.getName());
		}
	}
	
	public class ByDate implements Comparator<Song> {
		@Override
		public int compare(Song a, Song b) {
			long l1=a.getTDate()-b.getTDate();
			int p=1;
			if (l1<0){
				p=-1;
			}
			if (l1==0){
				p=0;
				//System.out.println(a.getName()+" "+b.getName()+" "+p);
			}
			//System.out.println(a.getName()+" "+b.getName()+" "+p);
		    return p;
		}
	}
	
	public class HasSylls implements Comparator<Song> {
		@Override
		public int compare(Song a, Song b) {
			int p1=a.getNumSylls();
			int p2=b.getNumSylls();
			
			int p=0;
			if ((p1>0)&&(p2==0)){p=-1;}
			if ((p1==0)&&(p2>0)){p=1;}
			
		    return p;
		}
	}
	
	
	
	 public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
		if (EXPAND_COMMAND.equals(command)) {
			if (collapsed){
				treePanel.expandNode();
				collapsed=false;
				expandTree.setText("Collapse tree");
			}
			else{
				treePanel.collapseNode();
				collapsed=true;
				expandTree.setText("Expand tree");
			}
		}
		if (ANALYSE_ALL.equals(command)) {
			showAllSongs=!showAllSongs;
			populateTree();
			this.revalidate();
		}
		if (NEXTSTEP.equals(command)) {moveToNextStep(mainPanel.getSelectedIndex());}
		if(STOPCOMMAND.equals(command)){cancelTask(mainPanel.getSelectedIndex());}
		if (MDS_VIEW.equals(command)){
			PCPaneOptions ppo=new PCPaneOptions();
		}
		if (DENDROGRAM_VIEW.equals(command)){
			DendrogramOptions ddo=new DendrogramOptions(defaults);
		}
	}	
	
	 
	/**
	 * This method is meant to interrupt parts of the analysis that are underway. These are frequently 
	 * very time consuming so this can be useful. However, it doesn't work very effectively at present!
	 * @param stepPostition the position the analysis has reached
	 */
			
	
	public void cancelTask(int stepPostition){
		progress.setValue(new Integer(0));
		nextButton.setEnabled(true);
		stopButton.setEnabled(false);
		if (stepPosition==0){
			boolean s=clsw.cancel(true);
			System.out.println("does it think it stopped it?" +s);
		}
		else if (stepPosition==2){
			dtwsw.cancel(true);
		}
		else if (stepPosition==3){
			answ.cancel(true);
		}
		
	}
	
	//public void stepTwo(SongGroup sg){
	public void stepTwo(AnalysisGroup ag){
		
		stopButton.setEnabled(false);
		nextButton.setEnabled(true);
		
		//this.sg=sg;
		this.ag=ag;
		//sg=cs.getSongs();
		
		//defaults.getDTWParameters(sg);
		
		
		//if (dbc.dbName.startsWith("SwampSparrow")){sg.interpretSwampSparrowNotes();}
		//sg.updateVibrato();
		//if (sg.getNSongs()==0){
		if (ag.getLengths(4)==0){
			JOptionPane.showMessageDialog(this,"You must select some sounds to compare before proceeding to the next stage");
		}
		else{
			schemeType=cs.tabpane.getSelectedIndex();
			
			tabCorrection(1);
			
			mainPanel.addTab("Comparison type", comparisonType);
			mainPanel.revalidate();
			mainPanel.setSelectedIndex(1);
			stepPosition++;				
		}
	}
	
	public void stepThree(){
		stopButton.setEnabled(false);
		nextButton.setEnabled(true);
		if (schemeType==0){
			if (sop!=null){
				sop.cleanUp();
			}
			if (answ!=null){
				answ.cleanUp();
			}
			//sop=new StatOptionPanel(dbc, sg, defaults, this);
			sop=new StatOptionPanel(dbc, ag, defaults, this);
			mainPanel.addTab("Statistics settings", sop);
		}
		else{
			//cad=new ComplexAnalysisDownload(sg, defaults);
			cad=new ComplexAnalysisDownload(ag, defaults);
			mainPanel.addTab("Save results", cad);
		}
		mainPanel.revalidate();
		mainPanel.setSelectedIndex(mainPanel.getTabCount()-1);
		stepPosition++;
	}

	public void stepFour(){
		stopButton.setEnabled(false);
		nextButton.setEnabled(true);
		if (schemeType==0){
			mainPanel.addTab("Results", answ.resultsPanel);
		}
		mainPanel.revalidate();
		mainPanel.setSelectedIndex(mainPanel.getTabCount()-1);
		stepPosition++;
	}
	
	public void moveToNextStep(int stepPosition){
		
		nextButton.setEnabled(false);
		
		System.out.println(stepPosition);
	
		if (stepPosition==0){
			//if (sg!=null){
				//sg.cleanUp();
			//}
			if (ag!=null){
				ag.cleanUp();
			}
			
			//sg=cs.execute();
			
			clsw=new SongLoaderSwingWorker(cs, this);
			clsw.addPropertyChangeListener(this);
			stopButton.setEnabled(true);
			clsw.execute();
		}
		else if (stepPosition==1){
			ButtonModel bm=bg.getSelection();
			if (bm==null){
				JOptionPane.showMessageDialog(this,"You must select some a type of analysis before proceeding to the next stage");
			}
			else{
				
				tabCorrection(2);
				
				if (dtwAnalysis.isSelected()){
					//dtw=new DTWPanel(dbc, sg, true, defaults);
					dtw=new DTWPanel(dbc, ag, true, defaults);
					mainPanel.addTab("TW settings", dtw);
					analysisMode=0;
				}
				
				else if (parAnalysis.isSelected()){
					//pp=new ParameterPanel(sg);
					pp=new ParameterPanel(ag, defaults);
					mainPanel.addTab("Parametric analysis", pp);
					analysisMode=1;
					
				}
				
				else if (visAnalysis.isSelected()){
					boolean isSimple=true;
					if (schemeType==1){isSimple=false;}
					vap=new VisualAnalysisPane(this, isSimple);
					mainPanel.addTab("Visual analysis settings", vap);
					analysisMode=2;
				}
				
				else if (discAnalysis.isSelected()){
					
					abx=new ABXdiscrimination(ag, defaults);
					
					mainPanel.addTab("ABX experiment", abx);
				}
				
				else if (dParAnalysis.isSelected()){
					//measSave=new MeasurementSave(sg, defaults);
					measSave=new MeasurementSave(ag, defaults);
					mainPanel.addTab("Parameter output", measSave);
					analysisMode=3;
				}
				else if (dTestDB.isSelected()){
					//measSave=new MeasurementSave(sg, defaults);
					testDB=new CheckDatabase(ag, defaults);
					mainPanel.addTab("Database test", testDB);
				}
				else if (dSplitSongs.isSelected()) {
					splitSongs=new SplitSongs(ag, defaults);
					mainPanel.addTab("Split songs", splitSongs);
				}
				else if (dExportWavs.isSelected()) {
					exportSongs=new ExportSongs(ag, defaults);
					mainPanel.addTab("Export songs", exportSongs);
					
				}
				defaults.setIntProperty("analmod", analysisMode);
				
				mainPanel.setSelectedIndex(2);
				mainPanel.revalidate();
				stepPosition++;
			}
		}
		else if (stepPosition==2){
			
			tabCorrection(3);
			
			if (dtwAnalysis.isSelected()){
				
				//dtwsw=new DTWSwingWorker(this, dtw, sg, defaults);
				dtwsw=new DTWSwingWorker(this, dtw, ag, defaults);
				dtwsw.addPropertyChangeListener(this);
				stopButton.setEnabled(true);
				dtwsw.execute();
				

				//dtw.startAnalysis();
				
			}
			
			else if (parAnalysis.isSelected()){
				pp.startAnalysis();
				if (schemeType==0){
					if (sop!=null){
						sop.cleanUp();
					}
					//sop=new StatOptionPanel(dbc, sg, defaults, this);
					sop=new StatOptionPanel(dbc, ag, defaults, this);
					mainPanel.addTab("Statistics settings", sop);
				}
				else{
					//cad=new ComplexAnalysisDownload(sg, defaults);
					cad=new ComplexAnalysisDownload(ag, defaults);
					mainPanel.addTab("Save results", cad);
				}
				mainPanel.setSelectedIndex(3);
				mainPanel.revalidate();
			}
			
			else if (visAnalysis.isSelected()){
			
				if (vap.getIsVisualComparisonSelected()){
					System.out.println(schemeType);
					if (schemeType==1){
						int q=cs.getComplexSchemeKey();
						//visComp=new VisualComparison(sg, vap, q, this, dbc);
						visComp=new VisualComparison(ag, vap, q, this, defaults, dbc);
						mainPanel.addTab("Visual Comparison", visComp);
					}
					else{
						//boolean fs=vap.getFitSignalSelected();
						int q=cs.getSimpleSchemeKey();
						//simpVisComp=new SimpleVisualComparison(sg, q, fs, this);
						simpVisComp=new SimpleVisualComparison(ag, q, vap, this, defaults);
						mainPanel.addTab("Simple Visual Comparison", simpVisComp);
					}
					mainPanel.setSelectedIndex(3);
				}
				else{
					if (schemeType==1){
						int keyID=cs.getComplexSchemeKey();
						vap.exportComplex(keyID, dbc, defaults);
					}
					else{
						int keyID=cs.getSimpleSchemeKey();
						vap.exportSimple(keyID, dbc, defaults);
					}
					mainPanel.setSelectedIndex(2);
				}
				
				mainPanel.revalidate();
			}
			else if (dParAnalysis.isSelected()){
				measSave.output();
			}
			
		}
		
		else if (stepPosition==3){
			
			tabCorrection(4);
			
			if ((dtwAnalysis.isSelected())||(parAnalysis.isSelected())){
				if (schemeType==0){
					
					//IS THIS NEXT LINE IN THE RIGHT PLACE??!!
					//sg.setCompressElements(sop.elementCompression.isSelected());
					ag.setCompressElements(sop.elementCompression.isSelected());
					answ=new AnalysisSwingWorker(this, sop, dbc, ag, defaults);
					answ.addPropertyChangeListener(this);
					stopButton.setEnabled(true);
					answ.execute();

				}
				else{
					cad.compressResults();
					cad.export();
				}
			}
		
			
			
			stepPosition++;
		}
	
		
	}
	
	public void tabCorrection(int n){
		int numTabs=mainPanel.getTabCount();
		if (numTabs>n){
			for (int i=n; i<numTabs; i++){
				mainPanel.removeTabAt(n);
			}
		}
	}
	
	public void stateChanged(ChangeEvent e){
		int tabPosition=mainPanel.getSelectedIndex();
		System.out.println(tabPosition);
		if (tabPosition==4){
			nextButton.setEnabled(false);
		}
		else if ((visAnalysis.isSelected())&&(tabPosition==3)){
			nextButton.setEnabled(false);
		}
		else {
			nextButton.setEnabled(true);
		}	
	}
	
	public void cleanUp(){
		System.out.println("CLEANING UP ANALYSIS WINDOW");
		//if (sg!=null){
			//sg.cleanUp();
		//}
		if (ag!=null){
			ag.cleanUp();
		}
		if (sop!=null){
			sop.cleanUp();
		}
		//sg=null;
		ag=null;
		cs=null;
		dtw=null;
		vap=null;
		visComp=null;
		simpVisComp=null;
		measSave=null;
		sop=null;
		cad=null;
		System.gc();
		f.dispose();
	}
	
	public void windowClosing(WindowEvent e){
		cleanUp();
	}
	public void windowClosed(WindowEvent e){
		cleanUp();
	}
	public void windowActivated(WindowEvent e){}
	public void windowDeactivated(WindowEvent e){}
	public void windowIconified(WindowEvent e){}
	public void windowDeiconified(WindowEvent e){}
	public void windowOpened(WindowEvent e){}
	
	public void propertyChange(PropertyChangeEvent evt){
		if ("progress".equals(evt.getPropertyName())){
			progress.setValue((Integer)evt.getNewValue());
		}
	}
	
	public DTWSwingWorker getDTWSW(){
		return dtwsw;
	}
	
	public LinkedList<String[]> getAnalysisParameters(){
		if (analysisMode==0) {
			return dtw.getAnalysisParameters();
		}
		else {
			
			
			
			
		}
		return null;
	}
	
	
}