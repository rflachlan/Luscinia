package lusc.net.github.ui.db;
//  DatabaseView.java
//  Luscinia
//
//  Created by Robert Lachlan on 18/2/2008.
//  Copyright 2008 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import java.awt.*;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import java.util.*;
import java.io.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import lusc.net.github.Defaults;
import lusc.net.github.Individual;
import lusc.net.github.Luscinia;
import lusc.net.github.Song;
import lusc.net.github.db.DataBaseController;
import lusc.net.github.ui.AnalysisChoose;
import lusc.net.github.ui.IndividualEdit;
import lusc.net.github.ui.IndividualReview;
import lusc.net.github.ui.SaveSound;
import lusc.net.github.ui.SongInformation;
import lusc.net.github.ui.SpectrogramFileFilter;
import lusc.net.github.ui.TabType;
import lusc.net.github.ui.spectrogram.MainPanel;
import lusc.net.github.ui.spectrogram.SpectrPane;

public class DatabaseView extends TabType implements ActionListener {
	
    private int newNodeSuffix = 1;
    private static String ADD_IND_COMMAND = "add individual";
    private static String ADD_SPEC_COMMAND = "add species";
    private static String ADD_POP_COMMAND = "add population";
	private static String ADD_SONG_COMMAND = "add song";
	private static String ADD_RECORDING_COMMAND = "add recording";
    private static String REMOVE_COMMAND = "remove";
    private static String INFORMATION_COMMAND = "information";
	private static String SONO_COMMAND = "sonogram";
	private static String SIMPLE_COMMAND = "simple";
	private static String IND_REV_COMMAND = "ind rev";
	private static String ANALYZE_COMMAND = "analyze";
	private static String EXPAND_COMMAND="expand";
	private static String COPY_COMMAND="copy";
	private static String LOG_OUT_COMMAND = "logout";
	private static String MANAGE_USERS_COMMAND = "users";
	private static String BY_DAY_COMMAND = "by day";
	private static String BY_WEEK_COMMAND = "by week";
	private static String BY_NONE_COMMAND = "by none";
	
	boolean collapsed=true;
	boolean dateLevel=false;
	boolean weekLevel=true;
   
	LinkedList tstore=new LinkedList();
	LinkedList ustore=new LinkedList();
	String query=null;
	private int [] indq={1,2,3,4};
	private int [] sonq={1,2,3};
	private int [] eleq={1};
	int addType=0;
	JButton manageUsers, addIndButton, addSpecButton, addPopButton, addSongButton, addRecordingButton, removeButton, expandTreeButton, analysisButton, sonogramButton, simpleButton, indRevButton, expandButton, logOutButton, copyButton;
	JCheckBox informationCheckBox;
	
	File file;
	LinkedList spectrogramList=new LinkedList();
	LinkedList analysisList=new LinkedList();
	
	public DatabaseTree treePanel;
	JPanel sidePanel, informationPanel;
	
	DataBaseController dbc;
	Defaults defaults;
	Luscinia luscinia;

    public DatabaseView(DataBaseController dbc, Defaults defaults, Luscinia luscinia) {
		
        //super(new BorderLayout());
        this.dbc=dbc;
		this.defaults=defaults;
		this.luscinia=luscinia;
		this.setLayout(new BorderLayout());
		isSdLogin=false;
        //Create the components.
        treePanel = new DatabaseTree(this, dbc.getDBName());
        populateTree(treePanel);
		treePanel.setPreferredSize(new Dimension(300, 600));
		informationPanel=new JPanel();
		//informationPanel.setPreferredSize(new Dimension(200, 600));
		buildButtonSideBar();
		
		this.setLayout(new BorderLayout());
		this.add(sidePanel, BorderLayout.LINE_START);
		this.add(treePanel, BorderLayout.CENTER);
		this.add(informationPanel, BorderLayout.LINE_END);
		
		//Updater updater=new Updater(dbc, defaults);
		//this.setPreferredSize(new Dimension(800, 600));
		//updateMeasurements();
	}
    
    public DataBaseController getDBController(){
    	return dbc;
    }
    
    public DatabaseTree getDBTree(){
    	return treePanel;
    }
    
    public void refreshTree(){
    	String expState=treePanel.getExpansionState();
    	//myNode[] sn=treePanel.selnode;
    	//TreePath tp=new TreePath(sn[0].getPath());
    	
    	int tr=treePanel.tree.getMinSelectionRow();
    	if (tr>0){tr--;}
    	
    	this.remove(treePanel);
    	treePanel = new DatabaseTree(this, dbc.getDBName());
    	populateTree(treePanel);
    	treePanel.restoreExpansionState(expState);
    	treePanel.tree.scrollRowToVisible(tr);
    	this.add(treePanel, BorderLayout.CENTER);
    	
    	this.revalidate();
    }
		
	public void buildButtonSideBar(){
		addSpecButton = new JButton("Add Species");
        addSpecButton.setActionCommand(ADD_SPEC_COMMAND);
        addSpecButton.addActionListener(this);
		addSpecButton.setEnabled(true);
		
		addPopButton = new JButton("Add Population");
        addPopButton.setActionCommand(ADD_IND_COMMAND);
        addPopButton.addActionListener(this);
		addPopButton.setEnabled(false);
		
        addIndButton = new JButton("Add Individual");
        addIndButton.setActionCommand(ADD_IND_COMMAND);
        addIndButton.addActionListener(this);
		addIndButton.setEnabled(false);
		
		addSongButton = new JButton("Add Song");
        addSongButton.setActionCommand(ADD_SONG_COMMAND);
        addSongButton.addActionListener(this);
		addSongButton.setEnabled(false);
		
		addRecordingButton = new JButton("Add Recording");
		addRecordingButton.setActionCommand(ADD_RECORDING_COMMAND);
		addRecordingButton.addActionListener(this);
		addRecordingButton.setEnabled(false);
        
        removeButton = new JButton("Remove");
        removeButton.setActionCommand(REMOVE_COMMAND);
        removeButton.addActionListener(this);
		removeButton.setEnabled(false);
		
		informationCheckBox = new JCheckBox("Show Information");
		informationCheckBox.setSelected(true);
		informationCheckBox.setActionCommand(INFORMATION_COMMAND);
		informationCheckBox.addActionListener(this);
		
		sonogramButton = new JButton("Make Sonogram");
		sonogramButton.setActionCommand(SONO_COMMAND);
		sonogramButton.addActionListener(this);
		sonogramButton.setEnabled(false);
		
		simpleButton = new JButton("Simple Sonogram");
		simpleButton.setActionCommand(SIMPLE_COMMAND);
		simpleButton.addActionListener(this);
		simpleButton.setEnabled(false);
		
		indRevButton = new JButton("Individual Review");
		indRevButton.setActionCommand(IND_REV_COMMAND);
		indRevButton.addActionListener(this);
		//indRevButton.setEnabled(false);
		
		analysisButton = new JButton("Analyze");
		analysisButton.setActionCommand(ANALYZE_COMMAND);
		analysisButton.addActionListener(this);
		
		copyButton = new JButton("Copy");
		copyButton.setActionCommand(COPY_COMMAND);
		copyButton.addActionListener(this);
		
		expandButton=new JButton("Expand Tree");
		expandButton.setActionCommand(EXPAND_COMMAND);
		expandButton.addActionListener(this);
		
		logOutButton=new JButton("Log-out");
		logOutButton.setActionCommand(LOG_OUT_COMMAND);
		logOutButton.addActionListener(this);
		
		manageUsers=new JButton("Manage Users");
		manageUsers.setActionCommand(MANAGE_USERS_COMMAND);
		manageUsers.addActionListener(this);
		
		
		JRadioButton byWeek=new JRadioButton("Show week");
		byWeek.setActionCommand(BY_WEEK_COMMAND);
		byWeek.setSelected(weekLevel);
		byWeek.addActionListener(this);
		JRadioButton byDay=new JRadioButton("Show day");
		byDay.setActionCommand(BY_DAY_COMMAND);
		byDay.setSelected(dateLevel);
		byDay.addActionListener(this);
		JRadioButton byNone=new JRadioButton("Show none");
		byNone.setActionCommand(BY_NONE_COMMAND);
		byNone.setSelected(!(weekLevel&dateLevel));
		byNone.addActionListener(this);
		
		ButtonGroup g=new ButtonGroup();
		g.add(byWeek);
		g.add(byDay);
		g.add(byNone);
		
		sidePanel=new JPanel();
		sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.PAGE_AXIS));
		
		sidePanel.add(addSpecButton);
		sidePanel.add(addPopButton);
		sidePanel.add(addIndButton);
		sidePanel.add(addSongButton);
		sidePanel.add(addRecordingButton);
		sidePanel.add(removeButton);
		sidePanel.add(informationCheckBox);
		sidePanel.add(sonogramButton);
		sidePanel.add(simpleButton);
		sidePanel.add(indRevButton);
		sidePanel.add(analysisButton);
		sidePanel.add(expandButton);
		sidePanel.add(copyButton);
		sidePanel.add(manageUsers);
		sidePanel.add(logOutButton);
		sidePanel.add(byWeek);
		sidePanel.add(byDay);
		sidePanel.add(byNone);
		
    }
	
	public void clearUp(){
		for (int i=0; i<spectrogramList.size(); i++){
			MainPanel mp=(MainPanel)spectrogramList.get(i);
			mp.cleanUp();
		}
		for (int i=0; i<analysisList.size(); i++){
			AnalysisChoose ac=(AnalysisChoose)analysisList.get(i);
			ac.cleanUp();
		}
	
	}
	
	
	
	public void addToTree(DatabaseTree treePanel, LinkedList<String[]> t, String[] types) {
		
		//long t1=0;
		//long t2=0;
		//long t3=0;
		
		int[] ids=getSongIndividualIDs();
		
		int numind=treePanel.rootNode.getChildCount();
		System.out.println(t.size());
		if (t.size()>0) {
			String[] x=t.get(0);
			System.out.println("adding data "+x.length);
			int p=x.length-1;
			
			
			for (int i=0; i<t.size(); i++) {
				
				long t0=System.currentTimeMillis();
				
				System.out.println("adding data individual: "+i);
				x=t.get(i);
				myNode refNode=treePanel.rootNode;
				for (int j=0; j<p; j++) {
					System.out.println("adding data level: "+types[j]+" "+x[j]);
					
					//t1=System.currentTimeMillis();
					
					refNode=checkAndAdd(refNode, x[j], treePanel, types[j]);
					//t2+=System.currentTimeMillis()-t1;
					//t1=System.currentTimeMillis();
					if (j==p-1) {
						System.out.println("adding song");
						refNode.dex=myIntV(x[x.length-1]);
						addSongToTree(refNode, ids);
						
						//t3+=System.currentTimeMillis()-t1;
						
					}
					
					
				}
				
			}

		}
		
		//System.out.println("TIME TAKEN: "+t2+" "+t3);		
		
	}
	
	public myNode checkAndAdd(myNode refNode, String name, DatabaseTree treePanel, String type) {
		
		int numNodes=refNode.getChildCount();
		boolean found=false;
		for (int i=0; i<numNodes; i++) {
			myNode posspar=(myNode)refNode.getChildAt(i);
			String nam=(String)posspar.getUserObject();
			//System.out.println(nam+" "+name);
			if (nam.compareTo(name)==0) {
				//System.out.println("Matched");
				return posspar;
			}
		}
		System.out.println("Not matched");	
		myNode node=treePanel.addObject(refNode, name, true, type);
		return node;
	}
	
	
	public int[] getSongIndividualIDs() {
		int[] out=new int[ustore.size()];
		
		for (int i=0; i<out.length; i++) {
			Song nam=(Song)ustore.get(i);
			int par=nam.getIndividualID();
			out[i]=par;
		}
		
		return out;
	}
	
	
	public void addSongToTree(myNode refNode, int[] ids) {
		
		int q=refNode.dex;
		
		for (int i=0; i<ids.length; i++) {
			if (ids[i]==q) {
				Song nam=(Song)ustore.get(i);
				String nam0=nam.getName();
				int nam1=nam.getSongID();
				
				
				if ((dateLevel)||(weekLevel)) {
					long daySong=0l;
					if (weekLevel) {
						daySong=nam.getWeek();
					}
					else {
						daySong=nam.getDay();
					}
					int nd=refNode.getChildCount();
					boolean found2=false;
					for (int jj=0; jj<nd; jj++) {
						myNode possday=(myNode)refNode.getChildAt(jj);
						if (possday.day==daySong) {
							found2=true;
							myNode chile=treePanel.addObject(possday, nam0, true, "Song");
							chile.dex=nam1;
							if (nam.getNumSylls()>0){
								chile.isMeasured=true;
							
							}
							jj=nd;
						}
					}
					if (!found2){
						myNode newDay=treePanel.addObject(refNode, daySong, true, "Week");
						myNode chile=treePanel.addObject(newDay, nam0, true, "Song");
						chile.dex=nam1;
						if (nam.getNumSylls()>0){
							chile.isMeasured=true;
						}
					}	
				}

				else {
				
					myNode chile=treePanel.addObject(refNode, nam0, true, "Song");
					chile.dex=nam1;
					if (nam.getNumSylls()>0){
						chile.isMeasured=true;
					
					}
				}
			}
		}
		
	}
	
    public void populateTree(DatabaseTree treePanel) {
		
		extractFromDatabase();
		
		//System.out.println("POPULATING TREE: "+tstore.size()+" "+ustore.size());
		
		myNode nullpar=new myNode("temp");
		
		
		String[] types= {"Species", "Population", "Individual"};
		addToTree(treePanel, tstore, types);
	
		/*
		
		for (int i=0; i<tstore.size(); i++){
			String []nam=(String[])tstore.get(i);
			myNode chile=treePanel.addObject(null, nam[0], true);
			chile.dex=myIntV(nam[1]);
			
			//IndividualEdit ie=new IndividualEdit(treePanel, dbc, chile.dex, defaults);
			
			
			if (nam[0]=="Undetermined"){nullpar=chile;}
		}
		int numind=treePanel.rootNode.getChildCount();
		for (int i=0; i<ustore.size(); i++){
			Song nam=(Song)ustore.get(i);
			//if (nam[2]==null){nam[2]="-1";}
			int par=nam.getIndividualID();
			String nam0=nam.getName();
			int nam1=nam.getSongID();
			boolean found=false;
			for (int j=0; j<numind; j++){
				
				
				myNode posspar=(myNode)treePanel.rootNode.getChildAt(j);
				if (posspar.dex==par){
					found=true;
					
					if (dateLevel) {
						long daySong=nam.getDay();
						int nd=posspar.getChildCount();
						boolean found2=false;
						for (int jj=0; jj<nd; jj++) {
							myNode possday=(myNode)posspar.getChildAt(jj);
							if (possday.day==daySong) {
								found2=true;
								myNode chile=treePanel.addObject(possday, nam0, true);
								chile.dex=nam1;
								if (nam.getNumSylls()>0){
									chile.isMeasured=true;
								
								}
								j=numind;
								jj=nd;
							}
						}
						if (!found2){
							myNode newDay=treePanel.addObject(posspar, daySong, true);
							myNode chile=treePanel.addObject(newDay, nam0, true);
							chile.dex=nam1;
							if (nam.getNumSylls()>0){
								chile.isMeasured=true;
							
							}
							j=numind;
						}	
					}
					else {
					
						myNode chile=treePanel.addObject(posspar, nam0, true);
						chile.dex=nam1;
						if (nam.getNumSylls()>0){
							chile.isMeasured=true;
						
						}
						j=numind;
					}
				}
			}
			if (!found){
				myNode chile=treePanel.addObject(nullpar, nam0, true);
				chile.dex=nam1;
				if (nam.getNumSylls()>0){
					chile.isMeasured=true;
				}
			}
			
		}
		*/
    }
	
	public int myIntV(String s){
		Integer p1=Integer.valueOf(s);
		int p=p1.intValue();
		return p;
	}
	
	
	public void extractFromDatabase(){
		
		//System.out.println("EXTRACTING FROM DB");
		
		tstore=null;
		tstore=new LinkedList();
		query="SELECT SpecID, PopID, name, id FROM individual";
		tstore.addAll(dbc.readFromDataBase(query, indq));
		
		
		
		String[] s;
		String seg;
		int loc=0;
		for (int i=0; i<tstore.size(); i++){
			loc=i;
			s=(String[])tstore.get(i);
			seg=s[0];
			for (int j=i+1; j<tstore.size(); j++){
				s=(String[])tstore.get(j);
				if (seg.compareToIgnoreCase(s[0])<0){
					seg=s[0];
					loc=j;
				}
			}
			s=(String[])tstore.get(loc);
			tstore.remove(loc);
			tstore.add(0, s);
		}
		
		//System.out.println("LOADING SONG DETAILS");
		
		ustore=null;
		ustore=new LinkedList();
		ustore=dbc.loadSongDetailsFromDatabase();
		
		//System.out.println("SORTING SONGS");
		
		try{
			Collections.sort(ustore, new ByDate());
			Collections.sort(ustore, new HasSylls());
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		
		//System.out.println("NUM SONGS: "+ustore.size());
		
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
	
	public void renameNode (myNode temp){
		if (temp.song) {renameSong(temp);}
		else if (temp.individual) {renameIndividual(temp);}
		else if (temp.population) {renamePopulation(temp);}
		else if (temp.species) {renameSpecies(temp);}
		
		
		//if (temp.getLevel()>1){renameSong(temp);}
		//else{renameIndividual(temp);}
		refreshTree();
		//extractFromDatabase();
	}
	
	public void renameSong (myNode temp){
		String t=temp.toString();
		dbc.writeToDataBase("UPDATE songdata SET name='"+t+"' WHERE id="+temp.dex);
	}
	
	public void renameIndividual (myNode temp){
		String t=temp.toString();
		dbc.writeToDataBase("UPDATE individual SET name='"+t+"' WHERE id="+temp.dex);
	}
	
	public void renamePopulation(myNode temp) {
		String t=temp.toString();
		Enumeration<TreeNode> ch=temp.children();
		while (ch.hasMoreElements()) {
		    myNode child = (myNode)ch.nextElement();
		    dbc.writeToDataBase("UPDATE individual SET PopID='"+t+"' WHERE id="+child.dex);
		}
	}
	
	public void renameSpecies(myNode temp) {
		String t=temp.toString();
		Enumeration<TreeNode> ch=temp.children();
		while (ch.hasMoreElements()) {
		    myNode child = (myNode)ch.nextElement();
		    Enumeration<TreeNode> gch=child.children();
		    while (gch.hasMoreElements()) {
		    	myNode gchild = (myNode)gch.nextElement();
		    	dbc.writeToDataBase("UPDATE individual SET SpecID='"+t+"' WHERE id="+gchild.dex);
		    }
		}
	}
	
	public void removeFromDataBase(myNode temp){
		int p=temp.getChildCount();
		for (int j=0; j<temp.getChildCount(); j++){
			myNode temp2=(myNode)temp.getChildAt(j);
			removeFromDataBase(temp2);
		}
		if (temp.getType().equals("Song")){removeSong(temp);}
		else if(temp.getType().equals("Individual")) {removeIndividual(temp);}	
		extractFromDatabase();	
	}
	
	/*
	public void removeFromDataBase(myNode temp){
		for (int j=0; j<temp.getChildCount(); j++){
			myNode temp2=(myNode)temp.getChildAt(j);
			removeSong(temp2);
		}
		if (temp.getLevel()>1){removeSong(temp);}
		else{removeIndividual(temp);}	
		extractFromDatabase();	
	}
	*/
	
	public void removeSong (myNode temp){
		//System.out.println(spectrogramList.size());
		for (int i=0; i<spectrogramList.size(); i++){
			MainPanel mp=(MainPanel)spectrogramList.get(i);
			if (temp.dex==mp.getSong().getSongID()){
				mp.cleanUp();
				mp=null;
			}
		}
		
		dbc.writeToDataBase("DELETE FROM songdata WHERE id="+temp.dex);
		dbc.writeToDataBase("DELETE FROM wavs WHERE songid="+temp.dex);
		dbc.writeToDataBase("DELETE FROM element WHERE songID="+temp.dex);
	}
	
	
	public void removeIndividual(myNode temp){
		dbc.writeToDataBase("DELETE FROM individual WHERE id="+temp.dex);
		
		
		dbc.writeToDataBase("DELETE FROM songdata WHERE individualID="+temp.dex);
		//dbc.writeToDataBase("DELETE FROM wavs WHERE songid="+temp.dex);
		//dbc.writeToDataBase("DELETE FROM element WHERE songID="+temp.dex);
		
		
		for (int i=0; i<spectrogramList.size(); i++){
			MainPanel mp=(MainPanel)spectrogramList.get(i);
			if (temp.dex==mp.getSong().getIndividualID()){
				mp.cleanUp();
				mp=null;
			}
		}
		
	}
	
	public void addNewIndividual(myNode chile){
		String nam=chile.toString();
		myNode pop=(myNode)chile.getParent();
		String population=pop.toString();
		myNode spec=(myNode)pop.getParent();
		String species=spec.toString();
		dbc.writeToDataBase("INSERT INTO individual (name, SpecID, PopID) VALUES ('"+nam+"' , '"+species+"' , '"+population+"')");
		extractFromDatabase();
		int maxind=-1;
		for (int i=0; i<tstore.size(); i++){
			String[]nam2=(String[])tstore.get(i);
			int p=myIntV(nam2[nam2.length-1]);
			if (p>maxind){maxind=p;}
		}
		chile.dex=maxind;
	}
	
	public void addNewSong(myNode chile, myNode par){
		String nam=chile.toString();
		dbc.writeToDataBase("INSERT INTO songdata (name, IndividualID) VALUES ('"+nam+"' , "+par.dex+")");
		extractFromDatabase();
		int maxind=-1;
		for (int i=0; i<ustore.size(); i++){
			Song nam2=(Song)ustore.get(i);
			int p=nam2.getSongID();
			if (p>maxind){maxind=p;}
		}
		chile.dex=maxind;
	}

	public void showInformationIndividual(){
		IndividualEdit i=new IndividualEdit(this, dbc, treePanel.selnode[0].dex, defaults);
		informationPanel.removeAll();
		informationPanel.add(i);
		informationPanel.validate();
		informationPanel.revalidate();
		this.repaint();
	}
	
	public void showInformationSong(){
		SongInformation si=new SongInformation(this, dbc, treePanel.selnode[0].dex, defaults);
		informationPanel.removeAll();
		informationPanel.add(si);
		informationPanel.validate();
		informationPanel.revalidate();
		this.repaint();
	}
	
	public void hideInformationPanel(){
		informationPanel.removeAll();
		informationPanel.validate();
		informationPanel.revalidate();
		this.repaint();
	}
	
	public int getSongLocation(String s){
		int p=-1;
		for (int i=0; i<tstore.size(); i++){
			String[] x=(String[])tstore.get(i);
			if (x[0].equals(s)){
				p=myIntV(x[1]);
				
			}
		}		
		return p;
	}
	
	public void updateIndividualAllocation(myNode node, myNode[] nodes){
		int ID=node.dex;
		for (int i=0; i<nodes.length; i++){
			Song song=dbc.loadSongFromDatabase(nodes[i].dex, 0);
			song.setIndividualID(ID);
			dbc.writeSongInfo(song);
		}
		refreshTree();
		
	}
	    
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        try{
			if (ADD_SONG_COMMAND.equals(command)) {
				//int n = JOptionPane.showConfirmDialog(this,"Do you really want to add a wav file here?\n"+"(It will permanently remove whatever wav\n"+"is stored in the database)","Confirm Change", JOptionPane.YES_NO_OPTION);
				//if (n==0){openWav();}
				treePanel.addAbject(0);
			}
			else if (ADD_IND_COMMAND.equals(command)) {
				treePanel.addAbject(1);
			}
			else if (ADD_SPEC_COMMAND.equals(command)) {
				treePanel.addAbject(3);
			}
			else if (ADD_POP_COMMAND.equals(command)) {
				treePanel.addAbject(4);
			}
			else if (ADD_RECORDING_COMMAND.equals(command)) {
				treePanel.addAbject(2);
			}
			else if (REMOVE_COMMAND.equals(command)) {
				int n = JOptionPane.showConfirmDialog(this,"Do you really want to permanently delete this?","Confirm Delete", JOptionPane.YES_NO_OPTION);
				if (n==0){
					treePanel.removeCurrentNode();
					
				}
			}
			else if (INFORMATION_COMMAND.equals(command)){
				if (informationCheckBox.isSelected()){
					if (treePanel.selnode[0].getLevel()==1){
						showInformationIndividual();
					}
					else if (treePanel.selnode[0].getLevel()==2){
						showInformationSong();
					}
					else{
						hideInformationPanel();
					}
				}
				else{
					hideInformationPanel();
				}
			}
			else if (SONO_COMMAND.equals(command)) {
				openSpectrogram();
			}
			else if (SIMPLE_COMMAND.equals(command)) {
				openSimpleSpectrogram();
			}
			else if (IND_REV_COMMAND.equals(command)) {
				openIndividualReview();
			}
			else if (ANALYZE_COMMAND.equals(command)) {
				AnalysisChoose ac=new AnalysisChoose(dbc, defaults);
				analysisList.add(ac);
			}
			else if (EXPAND_COMMAND.equals(command)) {
				if (collapsed){
					treePanel.expandNode();
					collapsed=false;
					expandButton.setText("Collapse tree");
				}
				else{
					treePanel.collapseNode();
					collapsed=true;
					expandButton.setText("Expand tree");
				}
			}
			else if (COPY_COMMAND.equals(command)) {copyDB();}
			else if (MANAGE_USERS_COMMAND.equals(command)){
				UserManagement um=new UserManagement(dbc);
				
			}
			else if (LOG_OUT_COMMAND.equals(command)) {
				//luscinia.removeConnection(this);
				luscinia.loggedOut(this);
			}
			else if (BY_WEEK_COMMAND.equals(command)) {
				weekLevel=true;
				dateLevel=false;
				refreshTree();
			}
			else if (BY_DAY_COMMAND.equals(command)) {
				weekLevel=false;
				dateLevel=true;
				refreshTree();
			}
			else if (BY_NONE_COMMAND.equals(command)) {
				weekLevel=false;
				dateLevel=false;
				refreshTree();
			}
		}
		catch(Exception error){
			System.out.println(error);
			error.printStackTrace();
		}
    }
	/*
	public void openWav(){
		File [] file;
		dbc.writeToDataBase("DELETE FROM wavs WHERE songid="+treePanel.selnode.dex);
		dbc.writeToDataBase("DELETE FROM element WHERE songID="+treePanel.selnode.dex);
		JFileChooser fc=new JFileChooser();
		String defPath=defaults.props.getProperty("path");
		if (defPath!=null){fc=new JFileChooser(defPath);}
		fc.addChoosableFileFilter(new SpectrogramFileFilter());
		//fc
		int returnVal = fc.showOpenDialog(DatabaseView.this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file  = fc.getSelectedFiles();
			for (int i=0; i<file.length; i++){
				defPath= file[i].getPath();
				String defName=file[i].getName();
				defaults.props.setProperty("path", defPath);
				defaults.props.setProperty("filename", defName);
				if ((defName.endsWith(".wav"))||(defName.endsWith(".aiff"))||(defName.endsWith(".aif"))){
					System.out.println("HERE");
					treePanel.selnode.setUserObject(defName);
					renameSong(treePanel.selnode);
					dbc.writeSongIntoDatabase(defName, treePanel.selnode.dex, file[i]);
				}
			}
			file=null;
		}
	}
	 */
	/*
	public void backUpDB(){		
		int returnVal = JFileChooser.APPROVE_OPTION;
		JFileChooser fc=new JFileChooser();			
		String defPath=defaults.props.getProperty("path");
		if (defPath!=null){
			try{
				fc=new JFileChooser(defPath);
				File fs=new File(defPath, dbc.getDBaseName());
				fc.setSelectedFile(fs);
			}
			catch(Exception e){
				fc=new JFileChooser();
			}
		}
		else{
			fc=new JFileChooser();
		}
		fc.setDialogType(JFileChooser.SAVE_DIALOG);
		returnVal = fc.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
			int cont=0;
			if (file.exists()){
				cont= JOptionPane.showConfirmDialog(this,"Do you really want to overwrite this file?\n"+"(It will be deleted permanently)","Confirm Overwrite", JOptionPane.YES_NO_OPTION);
			}
			if (cont==0){
				defPath=file.getPath();
				String defName=file.getName();
				defaults.props.setProperty("path", defPath);
				defaults.props.setProperty("filename", defName);
				BackUp bu=new BackUp(dbc, defPath, defaults);
			}
		}
	}
	*/
	public void copyDB(){
		SdLogin sdlogin=new SdLogin(this.luscinia, false, defaults);

		
		int n=JOptionPane.showOptionDialog(this, sdlogin, "Choose a location or database to copy to", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
		
		if (n==0){
			DataBaseController dbc2=luscinia.getDBController();
			if (dbc2==null){
				JOptionPane.showMessageDialog(this,"Log-in to a second database before clicking ok!", "Copy error!", JOptionPane.OK_OPTION);
			}
			else{
				extractFromDatabase();
				//System.out.println(tstore.size()+" "+ustore.size());
			
				int[][] idTranslater=new int[tstore.size()][2];
			
				for (int i=0; i<tstore.size(); i++){
					String []nam=(String[])tstore.get(i);
					for (int j=0; j<nam.length; j++) {
						System.out.print(nam[j]+" ");
					}
					System.out.println();
					int p=myIntV(nam[3]);
					idTranslater[i][0]=p;
					Individual individual=new Individual(dbc, p);
					//individual.setPopulation(individual.getName().substring(0, 3));
					individual.setDBC(dbc2);
					individual.insertIntoDB();
					idTranslater[i][1]=dbc2.readIndividualNameFromDB(individual.getName());
					//System.out.println(idTranslater[i][0]+" "+idTranslater[i][1]);
				}
			
				int[][] songTranslater=new int[ustore.size()][2];
				
				for (int i=0; i<ustore.size(); i++){
					try{
						Song nam=(Song)ustore.get(i);
						int p=nam.getSongID();
						Song song=dbc.loadSongFromDatabase(p, 0);
						
						//song.updateElements();
						
						
						songTranslater[i][0]=song.getSongID();
						songTranslater[i][1]=i+1;
						
						song.makeAudioFormat();
						int newid=-1;
						for (int j=0; j<idTranslater.length; j++){
							if (idTranslater[j][0]==song.getIndividualID()){
								newid=idTranslater[j][1];
								j=idTranslater.length;
							}
						}
						//System.out.println("MATCH: "+song.getIndividualID()+" "+newid);
						File f=File.createTempFile("ltmp", "wav");
						song.setIndividualID(newid);
						SaveSound ss=new SaveSound(song, song.getAf(), 0, song.getRDLength(), f); 
						//System.out.println("Written song");
						dbc2.writeWholeSong(song, newid);
						//System.out.println("Written song "+song.getName()+" "+i+" "+ustore.size());
						f.delete();
					}
					catch(Exception e){
						e.printStackTrace();
						dbc.reconnect();
					}
				}
				
				LinkedList schemes1=dbc.loadSchemes(true);
				LinkedList schemes2=dbc.loadSchemes(false);
				LinkedList so=new LinkedList();
				so.addAll(dbc2.readFromDataBase("SELECT name, id, IndividualID FROM songdata", sonq));
				
				
				for (int i=0; i<schemes1.size(); i++){
					
					LinkedList sch=(LinkedList)schemes1.get(i);

					String s="INSERT INTO comparescheme (name, song1, song2, max_score, syll_comp, song_comp)VALUES (";
					String b=" , ";
					String t=")";
					String name=(String)sch.get(0);
					
					int be1=0;
					int bs1=0;
					
					for (int j=1; j<sch.size(); j++){
						int[] labels=(int[])sch.get(j);
						
						
						for (int k=0; k<songTranslater.length; k++){
							if (songTranslater[k][0]==labels[0]){
								labels[0]=songTranslater[k][1];
								k=songTranslater.length;
							}
						}
						
						for (int k=0; k<songTranslater.length; k++){
							if (songTranslater[k][0]==labels[1]){
								labels[1]=songTranslater[k][1];
								k=songTranslater.length;
							}
						}
						
						
						String s2=s+"'"+name+"'"+b+labels[0]+b+labels[1]+b+1+b+be1+b+bs1+t;
						dbc2.writeToDataBase(s2);
					}
				}
				
				
				for (int i=0; i<schemes2.size(); i++){
					
					LinkedList sch=(LinkedList)schemes2.get(i);
					
					String s="INSERT INTO comparescheme (name, song1, song2, max_score, syll_comp, song_comp)VALUES (";
					String b=" , ";
					String t=")";
					String name=(String)sch.get(0);
					
					int be1=0;
					int bs1=0;
					
					int def=-1;
					
					for (int j=1; j<sch.size(); j++){
						int[] labels=(int[])sch.get(j);
						
						for (int k=0; k<songTranslater.length; k++){
							if (songTranslater[k][0]==labels[0]){
								labels[0]=songTranslater[k][1];
								k=songTranslater.length;
							}
						}
						
						String s2=s+"'"+name+"'"+b+labels[0]+b+def+b+1+b+be1+b+bs1+t;
						dbc2.writeToDataBase(s2);
					}
				}
				luscinia.loggedOut();
			}
		}
		JOptionPane.showMessageDialog(this, "Copying completed");
	}
	
	public void openWav(myNode parentNode, Object child){
		File [] file;
		JFileChooser fc=new JFileChooser();
		String defPath=defaults.props.getProperty("path");
		
		if (defPath!=null){fc=new JFileChooser(defPath);}
		
		SpectrogramFileFilter[] sff={new SpectrogramFileFilter("wav"), new SpectrogramFileFilter("aiff"), new SpectrogramFileFilter("aif"), new SpectrogramFileFilter("mp3")};
		
		fc.addChoosableFileFilter(sff[0]);
		fc.addChoosableFileFilter(sff[1]);
		fc.addChoosableFileFilter(sff[2]);
		fc.addChoosableFileFilter(sff[3]);
		int p=defaults.getDefaultSoundFormat();
		
		fc.setFileFilter(sff[p]);
		
		fc.setMultiSelectionEnabled(true);
		int returnVal = fc.showOpenDialog(DatabaseView.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file  = fc.getSelectedFiles();
			treePanel.selnode=new myNode[file.length];
			for (int i=0; i<file.length; i++){
				defPath=file[i].getParent();
				String defName=file[i].getName();
				defaults.props.setProperty("path", defPath);
				defaults.props.setProperty("filename", defName);
				SpectrogramFileFilter sfu=(SpectrogramFileFilter)fc.getFileFilter();
				
				if (sfu==sff[0]){
					defaults.setDefaultSoundFormat(0);
				}
				else if (sfu==sff[1]){
					defaults.setDefaultSoundFormat(1);
				}
				else if (sfu==sff[2]){
					defaults.setDefaultSoundFormat(2);
				}
				
				String lcdefn=defName.toLowerCase();
				
				if ((lcdefn.endsWith(".wav"))||(lcdefn.endsWith(".aiff"))||(lcdefn.endsWith(".aif"))||(lcdefn.endsWith(".mp3"))){
					myNode ch=treePanel.addObject(parentNode, child, true);
					addNewSong(ch, parentNode);
					treePanel.selnode[i]=ch;				
					treePanel.selnode[i].setUserObject(defName);
					renameSong(treePanel.selnode[i]);
					
					//Song song=new Song(file[i], parentNode.dex);
					//MainPanel mp=new MainPanel(dbc, song, defaults, spectrogramList, this);
					//mp.startDrawing();
					//spectrogramList.add(mp);
					dbc.writeSongIntoDatabase(defName, treePanel.selnode[i].dex, file[i]);
				}
			}
			file=null;
		}
	}
	
	public void openRec(myNode parentNode, Object child){
		File [] file;
		JFileChooser fc=new JFileChooser();
		String defPath=defaults.props.getProperty("path");
		
		if (defPath!=null){fc=new JFileChooser(defPath);}
		
		SpectrogramFileFilter[] sff={new SpectrogramFileFilter("wav"), new SpectrogramFileFilter("aiff"), new SpectrogramFileFilter("aif"), new SpectrogramFileFilter("mp3")};
		
		fc.addChoosableFileFilter(sff[0]);
		fc.addChoosableFileFilter(sff[1]);
		fc.addChoosableFileFilter(sff[2]);
		fc.addChoosableFileFilter(sff[3]);
		int p=defaults.getDefaultSoundFormat();
		
		fc.setFileFilter(sff[p]);
		
		fc.setMultiSelectionEnabled(false);
		int returnVal = fc.showOpenDialog(DatabaseView.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			//file  = fc.getSelectedFiles();
			File file2=fc.getSelectedFile();
			//treePanel.selnode=new myNode[file.length];
			treePanel.selnode=new myNode[1];
			//for (int i=0; i<file.length; i++){
				defPath=file2.getParent();
				String defName=file2.getName();
				defaults.props.setProperty("path", defPath);
				defaults.props.setProperty("filename", defName);
				SpectrogramFileFilter sfu=(SpectrogramFileFilter)fc.getFileFilter();
				
				if (sfu==sff[0]){
					defaults.setDefaultSoundFormat(0);
				}
				else if (sfu==sff[1]){
					defaults.setDefaultSoundFormat(1);
				}
				else if (sfu==sff[2]){
					defaults.setDefaultSoundFormat(2);
				}
				
				String lcdefn=defName.toLowerCase();
				
				if ((lcdefn.endsWith(".wav"))||(lcdefn.endsWith(".aiff"))||(lcdefn.endsWith(".aif"))||(lcdefn.endsWith(".mp3"))){
					//myNode ch=treePanel.addObject(parentNode, child, true);
					//addNewSong(ch, parentNode);
					//treePanel.selnode[i]=ch;				
					//treePanel.selnode[i].setUserObject(defName);
					//renameSong(treePanel.selnode[i]);
					
					Song song=new Song(file2, parentNode.dex);
					MainPanel mp=new MainPanel(dbc, song, defaults, spectrogramList, this, false);
					mp.startDrawing(true);
					spectrogramList.add(mp);
					//dbc.writeSongIntoDatabase(defName, treePanel.selnode[i].dex, file[i]);
				//}
			}
			file=null;
		}
	}

	
	public void openSpectrogram(){
		if (treePanel.selnode!=null){
			for (int i=0; i<treePanel.selnode.length; i++){
				
				makeMainPanel(treePanel.selnode[i].dex, false);
			}
		}
		
	}
	
	public void openSimpleSpectrogram(){
		if (treePanel.selnode!=null){
			for (int i=0; i<treePanel.selnode.length; i++){		
				makeMainPanel(treePanel.selnode[i].dex, true);
			}
		}
		
	}
	
	
	public void openIndividualReview(){
		if (treePanel.selnode!=null){
			
			
			myNode[] daughters=new myNode[treePanel.selnode[0].getChildCount()];
			int[] dexes=new int[daughters.length];
			for (int i=0; i<daughters.length; i++){
				daughters[i]=(myNode)treePanel.selnode[0].getChildAt(i);
				dexes[i]=daughters[i].getDex();
			}
			
			IndividualReview ir=new IndividualReview(dexes, defaults, dbc);
			
		}
		
	}
	
	
	public void makeMainPanel(int id, boolean justGuide){
		MainPanel mp=new MainPanel(dbc, id, defaults, spectrogramList, this, justGuide);
		mp.startDrawing(true);
		spectrogramList.add(mp);
	}
	
	
	
	/*
	public void updateMeasurements(){
		
		
		System.out.println("UPDATING: "+ustore.size());
		
		SpectrPane s;
		
		int a=ustore.size();
		int b=a*3/10;
		
		for (int i=0; i<b; i++){
			String [] nam=(String[])ustore.get(i);
			int p=myIntV(nam[1]);
			System.out.println("a");
			
			Song song=dbc.loadSongFromDatabase(p, 0);
			if (song.getMaxF()<=0){
				defaults.getSongParameters(song);
			}
			
			//ALERT ARE THE FOLLOWING LINES RIGHT? I SHOULDN'T BE SETTING DEFAULTS HERE!
			song.setDynRange(40);
			song.setEchoRange(50);
			song.setEchoComp(1.0f);
			song.setDynEqual(200);
			
			song.setFFTParameters();
			System.out.println("b");
			s=new SpectrPane(song, false, false, null);
			System.out.println("c");
			
			//s.restart();
			System.out.println("d");
			
			song.setFFTParameters2(song.getNx());
			song.makeMyFFT(0, song.getNx());
			s.setNout(song.getOut());
			//s.relocate(0, song.nx);
			
			System.out.println("updating: "+song.getName());
			
			song.getMeasurer().updateTrillMeasuresX();
			dbc.writeSongMeasurements(song);
			
			
			song.clearUp();
			song=null;
			s.clearUp();
			s=null;
			
			System.gc();

			
			
		}
	}
	*/
	
	
	public void cleanUp(){
		for (int i=0; i<spectrogramList.size(); i++){
			MainPanel mp=(MainPanel)spectrogramList.get(i);
			mp.cleanUp();
			mp=null;
		}
	}
	
}