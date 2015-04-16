package lusc.net.github.ui.compmethods;
//
//  VisualComparison.java
//  Luscinia
//
//  Created by Robert Lachlan on 9/29/05.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.


import java.awt.*;

import javax.swing.*;

import java.util.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.text.*;

import lusc.net.github.Defaults;
import lusc.net.github.Song;
//import lusc.net.github.analysis.SongGroup;
import lusc.net.github.analysis.AnalysisGroup;
import lusc.net.github.db.DataBaseController;
import lusc.net.github.ui.AnalysisChoose;
import lusc.net.github.ui.spectrogram.SpectrPane;

public class VisualComparison extends JPanel implements PropertyChangeListener, ActionListener{

	/**
	 * 
	 */

	private static final long serialVersionUID = -8737156708045785019L;
	//LinkedList<Song> songlist;
	LinkedList<int[]> idlist;
	int[][] songResults;
	int[][][] syllResults;
	//LinkedList<Object> indata;
	SpectrPane s1, s2;
	Song song1, song2;
	int[][] scores;
	int compareSteps;
	boolean randomOrder;
	boolean bySyllable;
	boolean bySong;
	boolean allowVisuals;
	boolean allowSounds;
	boolean compressToFit=false;
	boolean updateable=true;
	boolean started=true;
	boolean location1presence=true;
	boolean location2presence=true;
	int progress=0;
	int total;
	int spectHeight=0;
	int width=0;
	int pbSpeed=1;
	Dimension d=new Dimension(800,600);

	JButton next=new JButton("Next");
	JButton previous=new JButton("Previous");
	JButton finish=new JButton("Finish");
	JButton update1=new JButton("Update");
	JButton update2=new JButton("Update");
	JLabel progressLabel;
	JLabel songSimL=new JLabel("Song Similarity");
	JLabel syll1a=new JLabel("Sound ");
	JLabel syll1b=new JLabel("in 1 ");
	JLabel syll2a=new JLabel("is most like ");
	JLabel syll2b=new JLabel("in 2 ");
	JLabel syll3=new JLabel("with a score of ");
	JLabel PlayA=new JLabel("Play Sound 1: ");
	JLabel PlayB=new JLabel("Play Sound 2: ");
	JLabel PlaySpeed=new JLabel("Playback Speed: ");
	JLabel echoRemovalL1=new JLabel("Echo removal: ");
	JLabel echoTailL1=new JLabel("Echo Tail (ms):");
	JLabel dynRangeL1=new JLabel("Dynamic Range (dB):");
	JLabel dynEqL1=new JLabel("Dynamic Eq:");
	JLabel filterCutOffL1=new JLabel("Filter Cut-Off (Hz):");
	JLabel echoRemovalL2=new JLabel("Echo removal: ");
	JLabel echoTailL2=new JLabel("Echo Tail (ms):");
	JLabel dynRangeL2=new JLabel("Dynamic Range (dB):");
	JLabel dynEqL2=new JLabel("Dynamic Eq:");
	JLabel filterCutOffL2=new JLabel("Filter Cut-Off (Hz):");
	
	JFormattedTextField dynRange1, dynEq1, echoRemoval1, echoTail1, filterCutOff1, dynRange2, dynEq2, echoRemoval2, echoTail2, filterCutOff2;
	JComboBox songSim, syllSim, syllChoice1, syllChoice2, playa, playb, playSpeed;
	//JPanel sound1, sound2;
	String[]choices;
	int[]choicesL;
	boolean listener=true;
	boolean listener2=true;
	int sckey;
	NumberFormat num;
	JPanel contentpane=new JPanel();
	
	//SongGroup sg;
	AnalysisGroup sg;
	Song[] songs;
	AnalysisChoose ac;
	DataBaseController dbc;
	Defaults defaults;
	
	
	//public VisualComparison(SongGroup sg,  VisualAnalysisPane vap, int sckey, AnalysisChoose ac, DataBaseController dbc){
	public VisualComparison(AnalysisGroup sg,  VisualAnalysisPane vap, int sckey, AnalysisChoose ac, Defaults defaults, DataBaseController dbc){
			
	//public VisualComparison(LinkedList<Song> songlist, LinkedList<int[]> idlist, int compareSteps, 
		//	boolean randomOrder, boolean bySyllable, boolean bySong, DataBaseController dbc, String user, int sckey){
		
		System.out.println("Starting visual comparison");
		
		this.sg=sg;
		songs=sg.getSongs();
		randomOrder=vap.random.isSelected();
		bySyllable=vap.syllable.isSelected();
		bySong=vap.song.isSelected();
		compressToFit=vap.fitSignal.isSelected();
		int a=vap.choiceScale.getSelectedIndex();
		compareSteps=vap.choiceScaleInt[a];
		allowVisuals=vap.allowVis.isSelected();
		allowSounds=vap.allowSound.isSelected();
		this.ac=ac;
		this.dbc=dbc;
		this.sckey=sckey;
		this.defaults=defaults;
		
		idlist=new LinkedList<int[]>();
		boolean[][] compScheme=sg.getCompScheme();
		for (int i=0; i<compScheme.length; i++){
			for (int j=0; j<compScheme.length; j++){
				if (compScheme[i][j]){
					int[] p={i,j};
					idlist.add(p);
				}
			}
		}
		
		num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(10);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		width=(int)(d.getWidth()-60);
		spectHeight=(int)((d.getHeight()-200)*0.5)-35;
		//int[] spair=(int[])idlist.get(progress);
		s1=new SpectrPane(defaults);
		s2=new SpectrPane(defaults);
		//s1.viewParameters[3]=false;
		s1.setCompressYToFit(spectHeight);
		if (compressToFit){
			s1.setCompressXToFit(width);
		}
		else{
			s1.setCompressXToFit(0);
		}
		//s2.viewParameters[3]=false;
		s2.setCompressYToFit(spectHeight);
		if (compressToFit){
			s2.setCompressXToFit(width);
		}
		else{
			s2.setCompressXToFit(0);
		}
		if(bySyllable){
			s1.setViewParameters(2, true);
			s2.setViewParameters(2, true);
		}
		
		if(!allowVisuals){
			s1.setDisplayMode(1);
			s2.setDisplayMode(1);
		}
		
		contentpane.setLayout(new GridLayout(0,1));
		
		JScrollPane scroller1=new JScrollPane(s1);
		JScrollPane scroller2=new JScrollPane(s2);
		//sound1=new JPanel(new BorderLayout());
		//sound2=new JPanel (new BorderLayout());
		//sound1.add(s1, BorderLayout.CENTER);
		//sound2.add(s2, BorderLayout.CENTER);

		contentpane.add(scroller1);
		contentpane.add(scroller2);
		
		System.out.println("Sounds made");
		
		//indata=dbc.getOutputVisual(sckey);
		
		d = Toolkit.getDefaultToolkit().getScreenSize();
		d.height-=200;
		contentpane.setPreferredSize(d);

		choicesL=new int[compareSteps];
		choices=new String[compareSteps+1];
		choices[0]=" ";
		for (int i=0; i<compareSteps; i++){
			choicesL[i]=i;
			Integer i2=new Integer(i);
			String i3=i2.toString();
			choices[i+1]=i3;
		}	
		
		if (randomOrder){Collections.shuffle(idlist);}
		total=idlist.size();
		
		songResults=new int[total][];
		syllResults=new int[total][][];
		
		progressLabel=new JLabel("Status: 1 of "+total+" ");
		next.addActionListener(this);
		
		previous.addActionListener(this);
	
		JPanel comparisonPanel=new JPanel(new GridLayout(0,1));
		comparisonPanel.setBorder(BorderFactory.createTitledBorder("Comparison"));
		JPanel leftPanel=new JPanel(new BorderLayout());
		leftPanel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
		JPanel statusPanel=new JPanel(new BorderLayout());
		statusPanel.add(progressLabel, BorderLayout.CENTER);
		statusPanel.add(next, BorderLayout.EAST);
		statusPanel.add(previous, BorderLayout.WEST);
		finish.addActionListener(this);
		leftPanel.add(finish, BorderLayout.CENTER);
		
		leftPanel.add(statusPanel, BorderLayout.WEST);
		if ((bySong)||(!bySyllable)){
			JPanel songComparison=new JPanel(new BorderLayout());
			songComparison.add(songSimL, BorderLayout.CENTER);
			songSim=new JComboBox(choices);
			songSim.setMaximumRowCount(12);
			songComparison.add(songSim, BorderLayout.EAST);
			leftPanel.add(songComparison, BorderLayout.EAST);
		}
		comparisonPanel.add(leftPanel);
		if (bySyllable){
			JPanel syllableComparison=new JPanel(new BorderLayout());
			syllableComparison.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
			syllChoice1=new JComboBox();
			syllChoice1.setMaximumRowCount(25);
			syllChoice2=new JComboBox();
			syllChoice2.setMaximumRowCount(25);
			syllSim=new JComboBox(choices);
			syllSim.setMaximumRowCount(12);
			syllChoice1.addActionListener(this);
			syllChoice2.addActionListener(this);
			syllSim.addActionListener(this);
			JPanel syllPanel1=new JPanel(new BorderLayout());
			syllPanel1.add(syll1a, BorderLayout.WEST);
			syllPanel1.add(syllChoice1, BorderLayout.CENTER);
			syllPanel1.add(syll1b, BorderLayout.EAST);
			JPanel syllPanel2=new JPanel(new BorderLayout());
			syllPanel2.add(syll2a, BorderLayout.WEST);
			syllPanel2.add(syllChoice2, BorderLayout.CENTER);
			syllPanel2.add(syll2b, BorderLayout.EAST);
			JPanel syllPanel3=new JPanel(new BorderLayout());
			syllPanel3.add(syll3, BorderLayout.WEST);
			syllPanel3.add(syllSim, BorderLayout.CENTER);
			syllableComparison.add(syllPanel1, BorderLayout.WEST);
			syllableComparison.add(syllPanel2, BorderLayout.CENTER);
			syllableComparison.add(syllPanel3, BorderLayout.EAST);
			comparisonPanel.add(syllableComparison);
		}
		JPanel rightPanel=new JPanel(new BorderLayout());
		rightPanel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
		playa=new JComboBox();
		playa.addActionListener(this);
		JPanel playersa=new JPanel(new BorderLayout());
		playersa.add(PlayA, BorderLayout.CENTER);
		playersa.add(playa, BorderLayout.EAST);
		rightPanel.add(playersa, BorderLayout.WEST);
		playb=new JComboBox();
		playb.addActionListener(this);
		JPanel playersb=new JPanel(new BorderLayout());
		playersb.add(PlayB, BorderLayout.CENTER);
		playersb.add(playb, BorderLayout.EAST);
		rightPanel.add(playersb, BorderLayout.CENTER);
		
		String[] plsp={"Full", "Half", "Quarter", "Eigth"};
		playSpeed=new JComboBox(plsp);
		playSpeed.addActionListener(this);
		JPanel psp=new JPanel(new BorderLayout());
		psp.add(PlaySpeed, BorderLayout.CENTER);
		psp.add(playSpeed, BorderLayout.EAST);
		rightPanel.add(psp, BorderLayout.EAST);
		
		if (allowSounds){
			comparisonPanel.add(rightPanel);
		}
		
		update1.addActionListener(this);
		dynRange1=new JFormattedTextField(num);
		dynRange1.setColumns(6);
		dynRange1.addPropertyChangeListener("value", this);
		dynEq1=new JFormattedTextField(num);
		dynEq1.setColumns(6);
		dynEq1.addPropertyChangeListener("value", this);
		echoRemoval1=new JFormattedTextField(num);
		echoRemoval1.setColumns(6);
		echoRemoval1.addPropertyChangeListener("value", this);
		echoTail1=new JFormattedTextField(num);
		echoTail1.setColumns(6);
		echoTail1.addPropertyChangeListener("value", this);
		filterCutOff1=new JFormattedTextField(num);
		filterCutOff1.setColumns(6);
		filterCutOff1.addPropertyChangeListener("value", this);
		
		update2.addActionListener(this);
		dynRange2=new JFormattedTextField(num);
		dynRange2.setColumns(6);
		dynRange2.addPropertyChangeListener("value", this);
		dynEq2=new JFormattedTextField(num);
		dynEq2.setColumns(6);
		dynEq2.addPropertyChangeListener("value", this);
		echoRemoval2=new JFormattedTextField(num);
		echoRemoval2.setColumns(6);
		echoRemoval2.addPropertyChangeListener("value", this);
		echoTail2=new JFormattedTextField(num);
		echoTail2.setColumns(6);
		echoTail2.addPropertyChangeListener("value", this);
		filterCutOff2=new JFormattedTextField(num);
		filterCutOff2.setColumns(6);
		filterCutOff2.addPropertyChangeListener("value", this);
		getNextPair();
		
		JTabbedPane appearance=new JTabbedPane(JTabbedPane.LEFT);
		
		
		JPanel sound1Panel=new JPanel(new GridLayout(0,2));
		sound1Panel.add(update1);
		JPanel echoRemovalP1=new JPanel(new BorderLayout());
		echoRemovalP1.add(echoRemovalL1, BorderLayout.CENTER);
		echoRemovalP1.add(echoRemoval1, BorderLayout.EAST);
		sound1Panel.add(echoRemovalP1);
		JPanel dynRangeP1=new JPanel(new BorderLayout());
		dynRangeP1.add(dynRangeL1, BorderLayout.CENTER);
		dynRangeP1.add(dynRange1, BorderLayout.EAST);
		sound1Panel.add(dynRangeP1);
		JPanel echoTailP1=new JPanel(new BorderLayout());
		echoTailP1.add(echoTailL1, BorderLayout.CENTER);
		echoTailP1.add(echoTail1, BorderLayout.EAST);
		sound1Panel.add(echoTailP1);
		JPanel dynEqP1=new JPanel(new BorderLayout());
		dynEqP1.add(dynEqL1, BorderLayout.CENTER);
		dynEqP1.add(dynEq1, BorderLayout.EAST);
		sound1Panel.add(dynEqP1);
		JPanel filterCutOffP1=new JPanel(new BorderLayout());
		filterCutOffP1.add(filterCutOffL1, BorderLayout.CENTER);
		filterCutOffP1.add(filterCutOff1, BorderLayout.EAST);
		sound1Panel.add(filterCutOffP1);
		appearance.addTab("S 1", sound1Panel);
		
		JPanel sound2Panel=new JPanel(new GridLayout(0,2));
		sound2Panel.add(update2);
		JPanel echoRemovalP2=new JPanel(new BorderLayout());
		echoRemovalP2.add(echoRemovalL2, BorderLayout.CENTER);
		echoRemovalP2.add(echoRemoval2, BorderLayout.EAST);
		sound2Panel.add(echoRemovalP2);
		JPanel dynRangeP2=new JPanel(new BorderLayout());
		dynRangeP2.add(dynRangeL2, BorderLayout.CENTER);
		dynRangeP2.add(dynRange2, BorderLayout.EAST);
		sound2Panel.add(dynRangeP2);
		JPanel echoTailP2=new JPanel(new BorderLayout());
		echoTailP2.add(echoTailL2, BorderLayout.CENTER);
		echoTailP2.add(echoTail2, BorderLayout.EAST);
		sound2Panel.add(echoTailP2);
		JPanel dynEqP2=new JPanel(new BorderLayout());
		dynEqP2.add(dynEqL2, BorderLayout.CENTER);
		dynEqP2.add(dynEq2, BorderLayout.EAST);
		sound2Panel.add(dynEqP2);
		JPanel filterCutOffP2=new JPanel(new BorderLayout());
		filterCutOffP2.add(filterCutOffL2, BorderLayout.CENTER);
		filterCutOffP2.add(filterCutOff2, BorderLayout.EAST);
		sound2Panel.add(filterCutOffP2);
		appearance.addTab("S 2", sound2Panel);

		JPanel topPanel=new JPanel(new BorderLayout());
		topPanel.add(comparisonPanel, BorderLayout.WEST);
		topPanel.add(appearance, BorderLayout.EAST);

		this.setLayout(new BorderLayout());
		this.add(contentpane, BorderLayout.CENTER);
		this.add(topPanel, BorderLayout.NORTH);
		
		System.out.println("Panels made");
	}
	
	public void getNextPair(){
		System.out.println("getting pair");
		listener=false;
		if (song1!=null){song1.setOut(null);}
		if (song2!=null){song2.setOut(null);}
		int[] spair=(int[])idlist.get(progress);
		
		sg.checkAndLoadRawData(spair[0]);
		sg.checkAndLoadRawData(spair[1]);
		
		song1=songs[spair[0]];
		song2=songs[spair[1]];
		song1.interpretSyllables();
		song2.interpretSyllables();
		//System.out.println(song1.name+" "+song2.name+" "+song1.timeStep);
		song1.setFFTParameters();
		song2.setFFTParameters();
		//System.out.println("Space: "+spectHeight);
		
		if(compressToFit){s1.setCompressXToFit(width);}
		else{s1.setCompressXToFit(song1.getNx());}
		if(bySyllable){
			s1.setViewParameters(2,true);
			s1.setViewParameters(3,true);
		}
		s1.relaunch(song1, false, true);
		//s1.relocate(0, song1.nx);
		
		if(compressToFit){s2.setCompressXToFit(width);}
		else{s2.setCompressXToFit(song2.getNx());}
		if(bySyllable){
			s2.setViewParameters(2,true);
			s2.setViewParameters(3,true);
		}
		s2.relaunch(song2, false, true);
		//s2.relocate(0, song2.nx);
		s1.setPreferredSize(new Dimension(s1.getNnx(), s1.getNny()));
		s2.setPreferredSize(new Dimension(s2.getNnx(), s2.getNny()));
		//System.out.println(s1.nnx+" "+s2.nnx);
		//s1.paintFound();
		//s2.paintFound();
		playa.removeAllItems();
		playa.addItem("All");
		playb.removeAllItems();
		playb.addItem("All");
		for (int i=0; i<song1.getNumSyllables(); i++){
			playa.addItem((i+1));
		}
		for (int i=0; i<song2.getNumSyllables(); i++){
			playb.addItem((i+1));
		}
		if(bySyllable){
			int size1=0;
			boolean[] phraseId1=song1.getPhraseID();
			for (int i=0; i<phraseId1.length; i++){
				if (phraseId1[i]){size1++;}
			}
			System.out.println("test: "+size1);
			if (size1==0){
				int[] tot={0, song1.getNx()};
				song1.addSyllable(tot);
				size1=1;
			}
			int size2=0;
			boolean[] phraseId2=song2.getPhraseID();
			for (int i=0; i<phraseId2.length; i++){
				if (phraseId2[i]){size2++;}
			}
			System.out.println("test: "+size2);
			if (size2==0){
				int[] tot={0, song2.getNx()};
				song2.addSyllable(tot);
				size2=1;
			}
			scores=new int[size1+1][2];
			syllChoice1.removeAllItems();
			choicesL=new int[size1];
			choices=new String[size1+1];
			choices[0]=" ";
			syllChoice1.addItem(choices[0]);
			int ii=0;
			for (int i=0; i<phraseId1.length; i++){
				if (phraseId1[i]){
					choicesL[ii]=i;
					Integer i2=new Integer(ii+1);
					String i3=i2.toString();
					choices[ii+1]=i3;
					syllChoice1.addItem(choices[ii+1]);
					ii++;
				}
			}
			syllChoice2.removeAllItems();
			choicesL=new int[size2];
			choices=new String[size2+1];
			choices[0]=" ";
			syllChoice2.addItem(choices[0]);
			int jj=0;
			for (int i=0; i<phraseId2.length; i++){
				if (phraseId2[i]){
					choicesL[jj]=i;
					Integer i2=new Integer(jj+1);
					String i3=i2.toString();
					choices[jj+1]=i3;
					syllChoice2.addItem(choices[jj+1]);
					//playb.addItem(choices[i+1]);
					jj++;
				}
			}
			syllSim.setSelectedIndex(0);
			syllChoice1.setSelectedIndex(0);
			syllChoice2.setSelectedIndex(0);
			
		}
		setValues();
		if (songResults[progress]!=null){
			songSim.setSelectedIndex(songResults[progress][2]+1);
		}
		else{
			songSim.setSelectedIndex(0);
		}
		if ((syllResults[progress]!=null)&&(syllResults[progress][0]!=null)){
			for (int i=0; i<scores.length; i++){
				scores[i][0]=syllResults[progress][i][4];
				scores[i][1]=syllResults[progress][i][3];
			}
		}
		listener=true;
		System.out.println("got pair");
	}
	
	public void saveAndQuit(){
		String b=" , ";
		String t=")";
		String userName=JOptionPane.showInputDialog("Please enter your name");
		if(userName!=null){
			if (bySong){
				String s="INSERT INTO comparesong (user, song1, song2, score, max_score, scheme_id)VALUES (";
				String s2="DELETE FROM comparesong WHERE user= '"+userName+"' AND song1="+song1.getSongID()+" AND song2="+song2.getSongID()+" AND scheme_id="+sckey;
				dbc.writeToDataBase(s2);
				for (int i=0; i<songResults.length; i++){
					int[] r=songResults[i];
					dbc.writeToDataBase(s+"'"+userName+"'"+b+r[0]+b+r[1]+b+r[2]+b+r[3]+b+r[4]+t);
				}
				
			}
			if (bySyllable){
				String s="INSERT INTO comparesyll (user, song1, song2, syll1, score, syll2, max_score, scheme_id)VALUES (";
				String s2="DELETE FROM comparesyll WHERE user='"+userName+"' AND song1="+song1.getSongID()+" AND song2="+song2.getSongID()+" AND scheme_id="+sckey;
				dbc.writeToDataBase(s2);
				for (int i=0; i<syllResults.length; i++){
					if (syllResults[i]!=null){
						for (int j=0; j<syllResults[i].length; j++){
							if (syllResults[i][j]!=null){
								int[]r=syllResults[i][j];
								dbc.writeToDataBase(s+"'"+userName+"'"+b+r[0]+b+r[1]+b+r[2]+b+r[3]+b+r[4]+b+r[5]+b+r[6]+t);
							}
						}
					}
				}
				
			}
		}
		ac.cleanUp();
	}
	
	public void saveScores(){
		
		if (bySong){
			int p=songSim.getSelectedIndex()-1;
			if (p>=0){
				int[] results={song1.getSongID(), song2.getSongID(), p, compareSteps, sckey};
				songResults[progress]=results;
			}
		}
		if (bySyllable){
			syllResults[progress]=new int[scores.length][];
			for (int i=1; i<scores.length; i++){
				int p=scores[i][0];
				if (p>=1){
					int[]results={song1.getSongID(), song2.getSongID(), i, scores[i][1], p, compareSteps, sckey};
					syllResults[progress][i]=results;
				}
			}
		}

	}
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if ((source==syllChoice1)||(source==syllChoice2)||(source==syllSim)){
			if ((listener)&&(listener2)){
				listener2=false;
				if (bySyllable){
					int a=syllChoice1.getSelectedIndex();
					int b=syllChoice2.getSelectedIndex();
					int c=syllSim.getSelectedIndex();
					if (a>-1){
						if (source==syllChoice1){
							syllChoice2.removeAllItems();
							for (int i=0; i<choices.length; i++){syllChoice2.addItem(choices[i]);}
							syllChoice2.setSelectedIndex(scores[a][0]);
							for (int i=0; i<scores.length; i++){
								if ((a!=i)&&(scores[i][0]!=0)){syllChoice2.removeItem(choices[scores[i][0]]);}
							}
							syllSim.setSelectedIndex(scores[a][1]);
						}
						if (source==syllChoice2){
							syllSim.setSelectedIndex(scores[a][1]);
						}
						if (source==syllSim){
							if (a>0){
								for (int j=0; j<choices.length; j++){
									if (choices[j].equals(syllChoice2.getSelectedItem())){b=j;}
								}
								scores[a][0]=b;
								scores[a][1]=c;
							}
						}
					}
				}
			}
		}
		if (source==songSim){
			if (songSim.getSelectedIndex()>0){next.setEnabled(true);}
		}
		if (source==playSpeed) {
			JComboBox cb = (JComboBox)e.getSource();
			int ind=cb.getSelectedIndex();
			pbSpeed=0;
			if (ind==0){pbSpeed=1;}
			else if (ind==1){pbSpeed=2;}
			else if (ind==2){pbSpeed=4;}
			else if (ind==3){pbSpeed=8;}
			
		}
		if ((listener)&&(source==playa)){
			song1.setPlaybackDivider(pbSpeed);
			song1.setUpPlayback();
			int syllch=playa.getSelectedIndex();
			if (syllch==0){song1.playSound(0, song1.getRawDataLength());}
			else{
				syllch--;
				int[] sya=(int[])song1.getSyllable(syllch);
				
				int[]sy={sya[0], sya[1]};
				sya[0]-=7;
				if (sya[0]<0){sya[0]=0;}
				sya[1]+=7;
				if (sya[1]>=song1.getOverallLength()){
					sya[1]=song1.getOverallLength()-1;
				}
				for (int i=0; i<2; i++){
					if (song1.getSizeInBits()==16){
						sy[i]*=(int)Math.round(song1.getSampleRate()*song1.getStereo()*0.002);
					}
				}
				
				if(song1.getRawDataLength()>sy[1]){
					song1.playSound(sy[0], sy[1]);		
				}
				sy=null;
			}
		}
		if ((listener)&&(source==playb)){
			song2.setPlaybackDivider(pbSpeed);
			song2.setUpPlayback();
			int syllch=playb.getSelectedIndex();
			if (syllch==0){song2.playSound(0, song2.getRawDataLength());}
			else{
				syllch--;
				int[] sya=(int[])song2.getSyllable(syllch);
				int[]sy={sya[0], sya[1]};
				sya[0]-=7;
				if (sya[0]<0){sya[0]=0;}
				sya[1]+=7;
				if (sya[1]>=song1.getOverallLength()){
					sya[1]=song1.getOverallLength()-1;
				}
				for (int i=0; i<2; i++){
					if (song2.getSizeInBits()==16){
						sy[i]*=(int)Math.round(song2.getSampleRate()*song2.getStereo()*0.002);
					}
				}
				if(song2.getRawDataLength()>sy[1]){
					song2.playSound(sy[0], sy[1]);		
				}
				sy=null;
			}
		}
		if (source==update1){replot1();}
		if (source==update2){replot2();}
		
		if (source==finish){saveAndQuit();}
		
		if (source==next){
			System.out.println("next clicked");
			saveScores();
			//if (progress>0){saveScores();}
			progress++;
			if (progress==total){
				progress=0;
			}
			progressLabel.setText("Status: "+(progress+1)+" of "+total+" ");
			getNextPair();
		}
		if (source==previous){
			System.out.println("previous clicked");
			saveScores();
			//if (progress>0){saveScores();}
			progress--;
			if (progress<0){
				progress=total-1;
			}
			progressLabel.setText("Status: "+(progress+1)+" of "+total+" ");
			getNextPair();
		}
		
		listener2=true;
	}
	
	public void propertyChange(PropertyChangeEvent e) {
        Object source = e.getSource();
		if (started){
			started=false;
			if (source==dynRange1){
				double s = ((Number)dynRange1.getValue()).doubleValue();
				if (s<=0){s=0.00001;}
				dynRange1.setValue(new Double(s));
				song1.setDynRange(s);
			}
			if (source==echoRemoval1){
				double s = ((Number)echoRemoval1.getValue()).doubleValue();
				if (s<0){s=0;}
				if (s>1000){s=1000;}
				echoRemoval1.setValue(new Double(s));
				song1.setEchoComp(s);
			}
			if (source==echoTail1){
				int s = (int)((Number)echoTail1.getValue()).doubleValue();
				if (s<0){s=0;}
				echoTail1.setValue(new Double(s));
				song1.setEchoRange(s);
			}
			if (source==dynEq1){
				int te = ((Number)dynEq1.getValue()).intValue();
				if (te<0){te=0;}
				song1.setDynEqual(te);
				dynEq1.setValue(new Double(te));
			}
			if (source==filterCutOff1){
				double te = ((Number)filterCutOff1.getValue()).doubleValue();
				if (te<0){te=0;}
				if (te>song1.getMaxF()){te=song1.getMaxF();}
				song1.setFrequencyCutOff(te);
				//song1.makeFilterBank();
				filterCutOff1.setValue(new Double(te));
			}
			if (source==dynRange2){
				double s = ((Number)dynRange2.getValue()).doubleValue();
				if (s<=0){s=0.00001;}
				dynRange2.setValue(new Double(s));
				song2.setDynRange(s);
			}
			if (source==echoRemoval2){
				double s = ((Number)echoRemoval2.getValue()).doubleValue();
				if (s<0){s=0;}
				if (s>1000){s=1000;}
				echoRemoval2.setValue(new Double(s));
				song2.setEchoComp(s);
			}
			if (source==echoTail2){
				int s = (int)((Number)echoTail2.getValue()).doubleValue();
				if (s<0){s=0;}
				echoTail2.setValue(new Double(s));
				song2.setEchoRange(s);
			}
			if (source==dynEq2){
				int te = ((Number)dynEq2.getValue()).intValue();
				if (te<0){te=0;}
				song2.setDynEqual(te);
				dynEq2.setValue(new Double(te));
			}
			if (source==filterCutOff2){
				double te = ((Number)filterCutOff2.getValue()).doubleValue();
				if (te<0){te=0;}
				if (te>song2.getMaxF()){te=song2.getMaxF();}
				song2.setFrequencyCutOff(te);
				//song1.makeFilterBank();
				filterCutOff2.setValue(new Double(te));
			}
			started=true;
		}
	}
	
	public void replot1(){
		Cursor c=this.getCursor();
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		s1.restart();
		this.setCursor(c);
	}
	
	public void replot2(){
		Cursor c=this.getCursor();
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		s2.restart();
		this.setCursor(c);
	}
	
	public void setValues(){
		dynRange1.setValue(new Double(song1.getDynRange()));
		dynEq1.setValue(new Double(song1.getDynEqual()));
		echoRemoval1.setValue(new Double(song1.getEchoComp()));
		echoTail1.setValue(new Double(song1.getEchoRange()));
		filterCutOff1.setValue(new Double(song1.getFrequencyCutOff()));
		
		dynRange2.setValue(new Double(song2.getDynRange()));
		dynEq2.setValue(new Double(song2.getDynEqual()));
		echoRemoval2.setValue(new Double(song2.getEchoComp()));
		echoTail2.setValue(new Double(song2.getEchoRange()));
		filterCutOff2.setValue(new Double(song2.getFrequencyCutOff()));
	}
}
