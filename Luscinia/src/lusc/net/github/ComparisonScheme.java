package lusc.net.github;

//
//  ComparisonScheme.java
//  Luscinia
//
//  Created by Robert Lachlan on 6/30/07.
//  Copyright 2007 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import java.awt.*;

import javax.swing.*;

import java.awt.event.*;
import java.util.*;

public class ComparisonScheme extends JPanel implements ActionListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1505772174436763652L;
	AnalysisChoose ac;
	private static String SAVE1 = "save1";
	private static String SAVE2 = "save2";
	private static String LOAD1 = "load1";
	private static String LOAD2 = "load2";
	private static String DELETE1 = "delete1";
	private static String DELETE2 = "delete2";
	private static String THRESH1 = "thresh1";
	private static String THRESH2 = "thresh2";
	
	JButton save1, save2;

	String[] schemesList1, schemesList2;

	int[] schemeKey1, schemeKey2;

	JComboBox load1, load2, delete1, delete2;

	DefaultComboBoxModel loadModel1, loadModel2, deleteModel1, deleteModel2;

	JLabel loadL1=new JLabel("Load Scheme from Database:");
	JLabel loadL2=new JLabel("Load Scheme from Database:");
	JLabel deleteL1=new JLabel("Delete Scheme from Database:");
	JLabel deleteL2=new JLabel("Delete Scheme from Database:");
	JPanel contentpane;
	
	JRadioButton selectOneSyllable1=new JRadioButton("Select one syllable per phrase", false);
	JRadioButton eachElementASyllable1=new JRadioButton("Make each element a phrase", false);
	JRadioButton selectOneSyllable2=new JRadioButton("Select one syllable per phrase", false);
	JRadioButton eachElementASyllable2=new JRadioButton("Make each element a phrase", false);
	JRadioButton sylsByThresh1=new JRadioButton("Segment syllables by threshold", false);
	JRadioButton sylsByThresh2=new JRadioButton("Segment syllables by threshold", false);
	
	LinkedList schemes1=new LinkedList();
	LinkedList schemes2=new LinkedList();
	LinkedList songslist=new LinkedList();
	boolean trigger=true;
	Defaults defaults;
	JTabbedPane tabpane=new JTabbedPane();
	
	SimpleAnalysis sa;
	ComplexAnalysis ca;
	
	double thresh1=5;
	double thresh2=5;

	public ComparisonScheme (AnalysisChoose ac, Defaults defaults){
		this.ac=ac;
		this.defaults=defaults;
		
		//tabpane.setTabPlacement(2);
				
		JPanel pane2=new JPanel();
		pane2.setLayout(new GridLayout(0,1));
		pane2.setBorder(BorderFactory.createEmptyBorder(10,10,200, 10));
		loadModel2=new DefaultComboBoxModel();
		deleteModel2=new DefaultComboBoxModel();
		makeSchemesList2();
		load2=new JComboBox(loadModel2);
		load2.setActionCommand(LOAD2);
		load2.addActionListener(this);	
		delete2=new JComboBox(deleteModel2);
		delete2.setActionCommand(DELETE2);
		delete2.addActionListener(this);	
		save2=new JButton("Save Comparison to Database");
		save2.setActionCommand(SAVE2);
		save2.addActionListener(this);
		sylsByThresh2.setActionCommand(THRESH2);
		sylsByThresh2.addActionListener(this);
		
		pane2.add(save2);
		pane2.add(loadL2);
		pane2.add(load2);
		pane2.add(deleteL2);
		pane2.add(delete2);
		pane2.add(selectOneSyllable2);
		pane2.add(eachElementASyllable2);
		pane2.add(sylsByThresh2);
		
		JPanel leftPanel2=new JPanel(new BorderLayout());
		leftPanel2.add(pane2, BorderLayout.NORTH);
		
		sa=new SimpleAnalysis(ac);
		JPanel tab2=new JPanel(new BorderLayout());
		tab2.add(sa, BorderLayout.CENTER);
		tab2.add(leftPanel2, BorderLayout.EAST);
		tabpane.addTab("Simple scheme", tab2);
				
		JPanel pane1=new JPanel();
		pane1.setLayout(new GridLayout(0,1));
		pane1.setBorder(BorderFactory.createEmptyBorder(10,10,200, 10));
		loadModel1=new DefaultComboBoxModel();
		deleteModel1=new DefaultComboBoxModel();
		makeSchemesList1();
		load1=new JComboBox(loadModel1);
		load1.setActionCommand(LOAD1);
		load1.addActionListener(this);	
		delete1=new JComboBox(deleteModel1);
		delete1.setActionCommand(DELETE1);
		delete1.addActionListener(this);		
		save1=new JButton("Save Comparison to Database");
		save1.setActionCommand(SAVE1);
		save1.addActionListener(this);
		sylsByThresh1.setActionCommand(THRESH1);
		sylsByThresh1.addActionListener(this);
					
		pane1.add(save1);
		pane1.add(loadL1);
		pane1.add(load1);
		pane1.add(deleteL1);
		pane1.add(delete1);
		pane1.add(selectOneSyllable1);
		pane1.add(eachElementASyllable1);
		pane1.add(sylsByThresh1);
		
		JPanel leftPanel1=new JPanel(new BorderLayout());
		leftPanel1.add(pane1, BorderLayout.NORTH);
		
		ca=new ComplexAnalysis(ac);
		JPanel tab1=new JPanel(new BorderLayout());
		tab1.add(ca, BorderLayout.CENTER);
		tab1.add(leftPanel1, BorderLayout.EAST);
		tabpane.addTab("Complex scheme", tab1);
		
		contentpane=new JPanel(new BorderLayout());

		contentpane.add(tabpane, BorderLayout.CENTER);
		
		this.add(contentpane);
	}
	
	
	public void makeSchemesList1(){
		schemes1=ac.dbc.loadSchemes(true);
		loadModel1.removeAllElements();
		deleteModel1.removeAllElements();
		schemesList1=new String[schemes1.size()];
		schemeKey1=new int[schemes1.size()];
		loadModel1.addElement(" ");
		deleteModel1.addElement(" ");
		for (int i=0; i<schemes1.size(); i++){
			LinkedList t=(LinkedList)schemes1.get(i);
			schemesList1[i]=(String)t.get(0);
			loadModel1.addElement(schemesList1[i]);
			deleteModel1.addElement(schemesList1[i]);
			int[] a=(int[])t.get(1);
			schemeKey1[i]=a[2];
		}
	}
	
	public void makeSchemesList2(){
		schemes2=ac.dbc.loadSchemes(false);
		loadModel2.removeAllElements();
		deleteModel2.removeAllElements();
		schemesList2=new String[schemes2.size()];
		schemeKey2=new int[schemes2.size()];
		loadModel2.addElement(" ");
		deleteModel2.addElement(" ");
		for (int i=0; i<schemes2.size(); i++){
			LinkedList t=(LinkedList)schemes2.get(i);
			schemesList2[i]=(String)t.get(0);
			loadModel2.addElement(schemesList2[i]);
			deleteModel2.addElement(schemesList2[i]);
			int[] a=(int[])t.get(1);
			schemeKey2[i]=a[2];
		}
	}
	
	public void loadScheme1(int i){
		LinkedList t=(LinkedList)schemes1.get(i);
		ca.loadScheme(t);
		int[] setup=(int[])t.get(1);
	}
	
	public void loadScheme2(int i){
		LinkedList t=(LinkedList)schemes2.get(i);
		sa.loadScheme(t);
	}
	
	public void deleteScheme1(int i){
		LinkedList t=(LinkedList)schemes1.get(i);
		String s=(String)t.get(0);
		ac.dbc.writeToDataBase("DELETE FROM comparescheme WHERE name='"+s+"'");
		makeSchemesList1();
	}
	
	public void deleteScheme2(int i){
		LinkedList t=(LinkedList)schemes2.get(i);
		String s=(String)t.get(0);
		ac.dbc.writeToDataBase("DELETE FROM comparescheme WHERE name='"+s+"'");
		makeSchemesList2();
	}
	
	
	public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (LOAD1.equals(command)){
			JComboBox cb = (JComboBox)e.getSource();
			int j=cb.getSelectedIndex()-1;
			if ((j>=0)&&(trigger)){loadScheme1(j);}
		}
		if (LOAD2.equals(command)){
			JComboBox cb = (JComboBox)e.getSource();
			int j=cb.getSelectedIndex()-1;
			if ((j>=0)&&(trigger)){loadScheme2(j);}
		}
		if (THRESH1.equals(command)){
			String s=JOptionPane.showInputDialog(null, "Segmentation Threshold:");
			thresh1=Double.parseDouble(s);
		}
		if (THRESH2.equals(command)){
			String s=JOptionPane.showInputDialog(null, "Segmentation Threshold:");
			thresh2=Double.parseDouble(s);
		}
		if (DELETE1.equals(command)){
			if (trigger){
				JComboBox cb = (JComboBox)e.getSource();
				int n=JOptionPane.showConfirmDialog(this,"Are you sure you want to delete this scheme?\n"+"(This action cannot be reversed)","Confirm Scheme Overwrite", JOptionPane.YES_NO_OPTION);
				if (n==0){
					trigger=false;
					int j=cb.getSelectedIndex()-1;
					if (j>=0){deleteScheme1(j);}
					trigger=true;
				}
			}
		}
		if (DELETE2.equals(command)){
			if (trigger){
				JComboBox cb = (JComboBox)e.getSource();
				int n=JOptionPane.showConfirmDialog(this,"Are you sure you want to delete this scheme?\n"+"(This action cannot be reversed)","Confirm Scheme Overwrite", JOptionPane.YES_NO_OPTION);
				if (n==0){
					trigger=false;
					int j=cb.getSelectedIndex()-1;
					if (j>=0){deleteScheme2(j);}
					trigger=true;
				}
			}
		}
        if (SAVE1.equals(command)){
			trigger=false;
			//String s="INSERT INTO comparescheme (name, song1, max_score)VALUES (";
			String s="INSERT INTO comparescheme (name, song1, song2, max_score, syll_comp, song_comp)VALUES (";
			//String s="INSERT INTO comparescheme (name, song1, max_score, syll_comp, song_comp)VALUES (";
			System.out.println("SAVE 1 ENTERED");
			String b=" , ";
			String t=")";
			String question="Enter name for experiment:";
			String name=JOptionPane.showInputDialog(question);
			System.out.println("here");
			boolean existsAlready=ac.dbc.testScheme(name);
			
			if (existsAlready){
				int n=JOptionPane.showConfirmDialog(null,"That name already exists in the database\n"+"Do you want to remove it?","Confirm Scheme Overwrite", JOptionPane.YES_NO_OPTION);
				if (n==0){
					ac.dbc.writeToDataBase("DELETE FROM comparescheme WHERE name='"+name+"'");
					existsAlready=false;
				}
			}		
			System.out.println("here "+existsAlready);	
			if (!existsAlready){
			
				ac.dbc.writeToDataBase("DELETE FROM comparescheme WHERE name='"+name+"'");
			
				int be1=0;
				int bs1=0;
			
				int[][] table=ca.table;
				for (int i=0; i<table.length; i++){
					for (int j=0; j<table.length; j++){
						if (table[i][j]==1){
							String s2=s+"'"+name+"'"+b+ac.archIds[j]+b+ac.archIds[i]+b+1+b+be1+b+bs1+t;
							System.out.println(s2);
							ac.dbc.writeToDataBase(s2);
						}
					}
				}
			}
			makeSchemesList1();
			trigger=true;
		}
		if (SAVE2.equals(command)){
			trigger=false;
			//String s="INSERT INTO comparescheme (name, song1, max_score)VALUES (";
			String s="INSERT INTO comparescheme (name, song1, song2, max_score, syll_comp, song_comp)VALUES (";
			//String s="INSERT INTO comparescheme (name, song1, song2, max_score, syll_comp, song_comp)VALUES (";
			System.out.println("SAVE 2 ENTERED");
			String b=" , ";
			String t=")";
			int def=-1;
			String question="Enter name for experiment:";
			String name=JOptionPane.showInputDialog(question);
			if (name!=null){
				boolean existsAlready=ac.dbc.testScheme(name);
			
				if (existsAlready){
					int n=JOptionPane.showConfirmDialog(this,"That name already exists in the database\n"+"Do you want to remove it?","Confirm Scheme Overwrite", JOptionPane.YES_NO_OPTION);
					if (n==0){
						ac.dbc.writeToDataBase("DELETE FROM comparescheme WHERE name='"+name+"'");
						existsAlready=false;
					}
				}
				if (!existsAlready){
					int be1=0;
					int bs1=0;
					for	(int i=0; i<sa.leftList.size(); i++){
						Integer q=(Integer)sa.leftList.get(i);
						for (int j=0; j<ac.archIds.length; j++){
							if (q.intValue()==ac.archIds[j]){						
								String s2=s+"'"+name+"'"+b+ac.archIds[j]+b+def+b+def+b+def+b+def+t;
								System.out.println(s2);
								ac.dbc.writeToDataBase(s2);
							}
						}
					}
				}
			
				makeSchemesList2();
				trigger=true;
			}
		}
	}
	
	public int getSimpleSchemeKey(){
		int sckey=-1;
		if(load2.getSelectedIndex()>0){
			sckey=schemeKey2[load2.getSelectedIndex()-1];		//some key to do with identifying which scheme it is (unique id from db?)
		}
		return sckey;
	}

	
	public int getComplexSchemeKey(){
		int sckey=-1;
		if(load1.getSelectedIndex()>0){
			sckey=schemeKey1[load1.getSelectedIndex()-1];		//some key to do with identifying which scheme it is (unique id from db?)
		}
		return sckey;
	}

	
	public SongGroup getSongs(){

		SongGroup sg;
		int tabID=tabpane.getSelectedIndex();
			
		if (tabID==1){
			
				int n=0;
				LinkedList songList=new LinkedList();
				LinkedList idlist=new LinkedList();
				int[][] table=ca.table;
				for (int i=0; i<table.length; i++){
					for (int j=0; j<table.length; j++){
						if (table[j][i]==1){
							if ((i!=j)&&(table[i][j]==1)){table[i][j]=0;}
							int[] s={ac.archIds[i], ac.archIds[j]};
							int found[]={-1, -1};
							for (int k=0; k<songList.size(); k++){
								Song song=(Song)songList.get(k);
								if (song.songID==s[0]){found[0]=k;}
								if (song.songID==s[1]){found[1]=k;}
							}
							for (int l=0; l<2; l++){
								if (found[l]==-1){
									Song song=ac.dbc.loadSongFromDatabase(s[l], 1);
									songList.add(song);
									found[l]=songList.size()-1;
								}
							}
							idlist.add(found);
						}
					}
				}
				Song[] songs=new Song[songList.size()];
				songs=(Song[])songList.toArray(songs);
			
				boolean[][] compScheme=new boolean[songs.length][songs.length];
		
				for (int i=0; i<idlist.size(); i++){
					int[] pair=(int[])idlist.get(i);
					compScheme[pair[0]][pair[1]]=true;
				}

				sg=new SongGroup(songs, compScheme, defaults, ac.dbc);
				if (selectOneSyllable1.isSelected()){
					sg.pickJustOneExamplePerPhrase();
				}
			}  
			else {
				int ls=sa.leftList.size();
				LinkedList songList=new LinkedList();
				for (int i=0; i<ls; i++){
					Integer q=(Integer)sa.leftList.get(i);
					for (int j=0; j<ac.archIds.length; j++){
						if (q.intValue()==ac.archIds[j]){
							Song song=ac.dbc.loadSongFromDatabase(ac.archIds[j], 1);
							if (song.eleList.size()>0){
								LinkedList list1=ac.dbc.populateContentPane(song.individualID);
								song.sx=(String)list1.get(3);
								song.sy=(String)list1.get(4);
							
								if ((song.eleList!=null)&&(song.eleList.size()>0)){
									songList.add(song);
								}
							}	
						}
					}
				}
				//int sckey=schemeKey2[load2.getSelectedIndex()];
				Song[] songs=new Song[songList.size()];
				
				songs=(Song[])songList.toArray(songs);
				
				songList=null;
				sg=new SongGroup(songs, defaults, ac.dbc);
				if (selectOneSyllable2.isSelected()){
					sg.pickJustOneExamplePerPhrase();
				}
			}
			
			return (sg);
    }
	
}

