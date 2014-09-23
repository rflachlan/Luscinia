package lusc.net.sourceforge;
//
//  AnalysisChoose.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/10/2005.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

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
import javax.swing.JPanel;

import java.util.*;
import java.io.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class AnalysisChoose extends JPanel implements ActionListener, ChangeListener, WindowListener, PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static String EXPAND_COMMAND="expand";
	private static String NEXTSTEP="next";
	private static String STOPCOMMAND="stop";
	
	JRadioButton dtwAnalysis=new JRadioButton("Computer comparison by dynamic-time warping");
	JRadioButton parAnalysis=new JRadioButton("Comparison by parameter");
	JRadioButton visAnalysis=new JRadioButton("Comparison by inspection");
	JRadioButton dParAnalysis=new JRadioButton("Export parameter statistics to spreadsheet");
	
	int analysisMode=0;
	
	ButtonGroup bg=new ButtonGroup();
	
	int schemeType=0;
	boolean collapsed=true;
	
    protected DatabaseTree2 treePanel;
	LinkedList<String[]> tstore=new LinkedList<String[]>();
	LinkedList<String[]> ustore=new LinkedList<String[]>();
	String query=null;
	private int [] indq={1,2};
	private int [] sonq={1,2,3};
	JButton expandTree, nextButton, stopButton;
	
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
	
	SongGroup sg;
	ComparisonScheme cs;
	DTWPanel dtw;
	ParameterPanel pp;
	VisualAnalysisPane vap;
	VisualComparison visComp;
	SimpleVisualComparison simpVisComp;
	MeasurementSave measSave;
	StatOptionPanel sop;
	AnalysisSwingWorker answ;
	ComplexAnalysisDownload cad;
	JPanel schemePanel, comparisonType;
	JTabbedPane mainPanel=new JTabbedPane();
	
	SongLoaderSwingWorker clsw;
	DTWSwingWorker dtwsw;
	
	boolean mdsProvided=false;
	MultiDimensionalScaling mdsEle=null;
	MultiDimensionalScaling mdsSyll=null;
	MultiDimensionalScaling mdsSyTr=null;
	MultiDimensionalScaling mdsSong=null;
	
	JFrame f;
	
	public AnalysisChoose(DataBaseController dbc, Defaults defaults) {
		this.dbc=dbc;
		this.defaults=defaults;
		analysisMode=defaults.getIntProperty("analmod", 0);
		user=dbc.getUserID();
		buildUI();
	}
		
		
    public AnalysisChoose(DataBaseController dbc, String user, Defaults defaults) {
		
       
        this.dbc=dbc;
		this.user=user;
		this.defaults=defaults;
		analysisMode=defaults.getIntProperty("analmod", 0);
		buildUI();
	}
	
	public void buildUI(){
		
		 this.setLayout(new BorderLayout());
		
        //Create the components.
        treePanel = new DatabaseTree2(this);
        populateTree();

		expandTree=new JButton("Expand Tree");
		expandTree.setActionCommand(EXPAND_COMMAND);
		expandTree.addActionListener(this);
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
		buttonPanel.setLayout(new BorderLayout());
		buttonPanel.add(expandTree, BorderLayout.NORTH);
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
		bg.add(dParAnalysis);
		
		if (analysisMode==0){
			dtwAnalysis.setSelected(true);
		}
		else if (analysisMode==1){
			parAnalysis.setSelected(true);
		}
		else if (analysisMode==2){
			visAnalysis.setSelected(true);
		}
		else{
			dParAnalysis.setSelected(true);
		}
		
		JPanel rbgPanel=new JPanel(new GridLayout(0,1));
		
		rbgPanel.add(dtwAnalysis);
		rbgPanel.add(parAnalysis);
		rbgPanel.add(visAnalysis);
		rbgPanel.add(dParAnalysis);
		
		comparisonType.add(rbgPanel, BorderLayout.WEST);
		
		f=new JFrame("Analyze database");
		f.getContentPane().add(this);
		f.pack();
		f.setVisible(true);
		
		
    }

    public void populateTree() {
		
		extractFromDatabase();
		myNode nullpar=new myNode("temp");
		for (int i=0; i<tstore.size(); i++){
			String []nam=tstore.get(i);
			myNode chile=treePanel.addObject(null, nam[0], true);
			chile.dex=myIntV(nam[1]);
			if (nam[0]=="Undetermined"){nullpar=chile;}
		}
		
		int numind=treePanel.rootNode.getChildCount();
		archIds=new int[ustore.size()];
		archNams=new String[ustore.size()];
		archIndNams=new String[ustore.size()];
		for (int i=0; i<ustore.size(); i++){
			String [] nam=ustore.get(i);
			archIds[i]=myIntV(nam[1]);
			archNams[i]=nam[0];

			if (nam[2]==null){nam[2]="-1";}
			int par=myIntV(nam[2]);
			boolean found=false;
			for (int j=0; j<numind; j++){
				myNode posspar=(myNode)treePanel.rootNode.getChildAt(j);
				if (posspar.dex==par){
					found=true;
					String [] nam2=tstore.get(j);
					archIndNams[i]=nam2[0];
					
					myNode chile=treePanel.addObject(posspar, nam[0], true);
					chile.dex=myIntV(nam[1]);
					j=numind;
				}
			}
			if (!found){
				myNode chile=treePanel.addObject(nullpar, nam[0], true);
				chile.dex=myIntV(nam[1]);
			}
			
		}
    }
	
	public int myIntV(String s){
		Integer p1=Integer.valueOf(s);
		int p=p1.intValue();
		return p;
	}
	
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
		ustore=new LinkedList<String[]>();
		ustore.addAll(dbc.readFromDataBase("SELECT name, id, IndividualID FROM songdata", sonq));
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
		if (NEXTSTEP.equals(command)) {moveToNextStep(mainPanel.getSelectedIndex());}
		if(STOPCOMMAND.equals(command)){cancelTask(mainPanel.getSelectedIndex());}
	}	
	
	
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
			sop=new StatOptionPanel(dbc, sg, defaults, this);
			mainPanel.addTab("Statistics settings", sop);
		}
		else{
			cad=new ComplexAnalysisDownload(sg);
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
	
	public void stepTwo(SongGroup sg){
		
		stopButton.setEnabled(false);
		nextButton.setEnabled(true);
		
		this.sg=sg;
		//sg=cs.getSongs();
		
		defaults.getDTWParameters(sg);
		
		//if (dbc.dbName.startsWith("SwampSparrow")){sg.interpretSwampSparrowNotes();}
		//sg.updateVibrato();
		if (sg.songs.length==0){
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
	
	public void stepDTW(){
		
		
	}
	
	
	public void moveToNextStep(int stepPosition){
		
		nextButton.setEnabled(false);
		
		System.out.println(stepPosition);
	
		if (stepPosition==0){
			if (sg!=null){
				sg.cleanUp();
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
					dtw=new DTWPanel(dbc, sg, true);
					mainPanel.addTab("DTW settings", dtw);
					analysisMode=0;
				}
				
				else if (parAnalysis.isSelected()){
					pp=new ParameterPanel(sg);
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
				
				else if (dParAnalysis.isSelected()){
					measSave=new MeasurementSave(sg, defaults);
					mainPanel.addTab("Parameter output", measSave);
					analysisMode=3;
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
				
				dtwsw=new DTWSwingWorker(this, dtw, sg);
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
					sop=new StatOptionPanel(dbc, sg, defaults, this);
					mainPanel.addTab("Statistics settings", sop);
				}
				else{
					cad=new ComplexAnalysisDownload(sg);
					mainPanel.addTab("Save results", cad);
				}
				mainPanel.setSelectedIndex(3);
				mainPanel.revalidate();
			}
			
			else if (visAnalysis.isSelected()){
			
				if (vap.doVisualComparison.isSelected()){
					System.out.println(schemeType);
					if (schemeType==1){
						int q=cs.getComplexSchemeKey();
						visComp=new VisualComparison(sg, vap, q, this);
						mainPanel.addTab("Visual Comparison", visComp);
					}
					else{
						boolean fs=vap.fitSignal.isSelected();
						int q=cs.getSimpleSchemeKey();
						simpVisComp=new SimpleVisualComparison(sg, q, fs, this);
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
					sg.compressElements=sop.elementCompression.isSelected();
					answ=new AnalysisSwingWorker(this, sop, dbc, sg, defaults);
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
		if (sg!=null){
			sg.cleanUp();
		}
		if (sop!=null){
			sop.cleanUp();
		}
		sg=null;
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
	
}