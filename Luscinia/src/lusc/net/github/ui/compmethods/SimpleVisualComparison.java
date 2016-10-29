package lusc.net.github.ui.compmethods;
//
//  SimpleVisualAnalysis.java
//  Luscinia
//
//  Created by Robert Lachlan on 02/10/2005.
//  Copyright 2005 __MyCompanyName__. All rights reserved.
//


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

public class SimpleVisualComparison extends JPanel implements PropertyChangeListener, ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7325278160764952259L;
	LinkedList repertoireList;
	SpectrPane s1, s2;
	Song song2;
	boolean updateable=true;
	boolean started=true;
	boolean fitOnScreen=true;
	boolean allowSound=true;
	boolean allowVis=true;
	boolean randomOrder=true;
	DataBaseController dbc;
	int progress=0;
	int total;
	int place=0;
	int[] placeInd;
	int spectHeight=0;
	Dimension d=new Dimension(800,600);

	JButton next=new JButton("Spectrograms don't match, Next");
	JButton newType=new JButton("Spectrograms don't match, New Type");
	JButton matchWorse=new JButton("Spectrograms match, 1 better");
	JButton matchBetter=new JButton("Spectrograms match, 2 better");
	JButton update1=new JButton("Update");
	JButton update2=new JButton("Update");
	JButton play1=new JButton("Play sound 1");
	JButton play2=new JButton("Play sound 2");
	JLabel position=new JLabel("1 of 1");
	String progressLabel;

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
	JPanel contentpane=new JPanel();
	
	String user;
	boolean listener=true;
	int cycle=0;
	int sckey;
	int width=100;
	NumberFormat num;
	AnalysisChoose ac;
	//SongGroup sg;
	AnalysisGroup sg;
	Song[] songs;
	VisualAnalysisPane vap;
	Defaults defaults;
	
	Random random=new Random(System.currentTimeMillis());
	
	//public SimpleVisualComparison(SongGroup sg, int sckey, boolean fitOnScreen, AnalysisChoose ac){
	public SimpleVisualComparison(AnalysisGroup sg, int sckey, VisualAnalysisPane vap, AnalysisChoose ac, Defaults defaults){
		this.sg=sg;
		sg.checkAndLoadRawData();
		songs=sg.getSongs();
		this.defaults=defaults;
		this.dbc=sg.getDBC();
		this.sckey=sckey;
		this.vap=vap;
		this.fitOnScreen=vap.getFitSignalSelected();
		this.allowSound=vap.getAllowSound();
		this.allowVis=vap.getAllowVis();
		this.randomOrder=vap.getRandom();
		this.ac=ac;
		num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(10);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		width=(int)(d.getWidth()-60);
		spectHeight=(int)((d.getHeight()-200)*0.5)-25;
		s1=new SpectrPane(defaults);
		s2=new SpectrPane(defaults);
		s1.setLimitedOptions(spectHeight, width);
		s2.setLimitedOptions(spectHeight, width);

		this.setLayout(new GridLayout(0,1));
		JPanel sound1=new JPanel(new BorderLayout());
		JPanel sound2=new JPanel (new BorderLayout());
		sound1.add(s1, BorderLayout.CENTER);
		sound2.add(s2, BorderLayout.CENTER);
		contentpane.add(sound1);
		contentpane.add(sound2);
		
		placeInd=new int[songs.length];
		for (int i=0; i<songs.length; i++){
			placeInd[i]=i;
		}
		for (int i=0; i<songs.length; i++){
			int p=random.nextInt(songs.length-i);
			int q=placeInd[p];
			placeInd[p]=placeInd[i];
			placeInd[i]=q;
		}
		
		sg.checkAndLoadRawData(placeInd[place]);
		song2=songs[placeInd[place]];
		
		System.out.println("DONE THIS ONE");
		
		place++;
		
		
		System.out.println("DONE THIS ONE TOO");
		
		if (fitOnScreen){
			song2.setTimeStep((song2.getOverallSize()*1000)/(width*song2.getSampleRate()));
		}
		repertoireList=new LinkedList();		
		
		d = Toolkit.getDefaultToolkit().getScreenSize();
		d.height-=200;
		this.setPreferredSize(d);
		
		total=songs.length-1;
		progressLabel="Simple Visual Comparison - Status: 0 of "+total+" ";
		next.addActionListener(this);
		next.setMnemonic(KeyEvent.VK_N);
		next.setDisplayedMnemonicIndex(26);	
		newType.addActionListener(this);
		newType.setMnemonic(KeyEvent.VK_T);
		newType.setDisplayedMnemonicIndex(30);	
		JPanel comparisonPanel=new JPanel(new GridLayout(0,1));
		comparisonPanel.setBorder(BorderFactory.createTitledBorder("Comparison"));
		JPanel leftPanel=new JPanel(new BorderLayout());
		leftPanel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
		JPanel statusPanel=new JPanel(new BorderLayout());
		//statusPanel.add(progressLabel, BorderLayout.WEST);
		statusPanel.add(next, BorderLayout.WEST);
		statusPanel.add(position, BorderLayout.CENTER);
		statusPanel.add(newType, BorderLayout.EAST);
		leftPanel.add(statusPanel, BorderLayout.WEST);
		
		
		comparisonPanel.add(leftPanel);
		JPanel songComparison=new JPanel(new BorderLayout());
		matchWorse.addActionListener(this);
		matchWorse.setMnemonic(KeyEvent.VK_1);
		matchWorse.setDisplayedMnemonicIndex(20);	
		songComparison.add(matchWorse, BorderLayout.WEST);
		matchBetter.addActionListener(this);
		matchBetter.setMnemonic(KeyEvent.VK_2);
		matchBetter.setDisplayedMnemonicIndex(20);
		songComparison.add(matchBetter, BorderLayout.EAST);
		comparisonPanel.add(songComparison);
		JPanel rightPanel=new JPanel(new BorderLayout());
		rightPanel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
		play1.addActionListener(this);
		rightPanel.add(play1, BorderLayout.WEST);
		play2.addActionListener(this);
		rightPanel.add(play2, BorderLayout.EAST);
		
		if (allowSound){comparisonPanel.add(rightPanel);}

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

		getNextPair2(true);

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
	}
	
	public void matchThisPair(boolean beginning){
		listener=false;
		cycle--;
		LinkedList holder=(LinkedList)repertoireList.get(repertoireList.size()-cycle-1);
		if (beginning){
			//Song song=(Song)holder.get(0);
			//song.clearUp();
			holder.addFirst(song2);
		}
		else{
			//song2.clearUp();
			holder.addLast(song2);
		}
		//repertoireList.remove(cycle);
		//repertoireList.add(cycle, holder);
		song2=null;
		sg.checkAndLoadRawData(placeInd[place]);
		song2=songs[placeInd[place]];
		
		place++;
		
		if (fitOnScreen){
			song2.setTimeStep((song2.getOverallSize()*1000)/(width*song2.getSampleRate()));
		}
		song2.setFFTParameters();
		s2.relaunch(song2, false, true);
		s2.setPreferredSize(new Dimension(s2.getNnx(), s2.getNny()));
		s2.paintFound();
		cycle=0;
		position.setText((cycle)+" of "+repertoireList.size());
		listener=true;
	}
	
	public Song getCurrentSong(){
		int c=cycle-1;
		LinkedList holder=(LinkedList)repertoireList.get(repertoireList.size()-c-1);
		Song song1=(Song)holder.get(0);
		return (song1);
	}
	
	public void getNextPair1(boolean updateType){

		listener=false;
		if (cycle==repertoireList.size()){cycle=0;}
		//if (song1!=null){song1.setOut(null);}
		LinkedList holder=(LinkedList)repertoireList.get(repertoireList.size()-cycle-1);
		cycle++;
		//song1=null;
		Song song1=(Song)holder.get(0);
		
		
		song1.setFFTParameters();
		if (allowVis){
			if (song1.getOut()!=null){song1.setUpdateFFT(false);}
			s1.relaunch(song1, false, true);
			song1.setUpdateFFT(true);
			s1.setPreferredSize(new Dimension(s1.getNnx(), s1.getNny()));
		}
		if (updateType){setValues1();}
		else{setValues2();}	
		position.setText((cycle)+" of "+repertoireList.size());
		listener=true;
	}
		
	public void getNextPair2(boolean updateType){
		listener=false;
		cycle=0;
		LinkedList holder=new LinkedList();
		//song2.clearUp();
		holder.add(song2);
		repertoireList.add(holder);
		//song2=null;
		sg.checkAndLoadRawData(placeInd[place]);
		song2=songs[placeInd[place]];

		place++;
		
		if (fitOnScreen){
			song2.setTimeStep((song2.getOverallSize()*1000)/(width*song2.getSampleRate()));
		}
		getNextPair1(updateType);
		song2.setFFTParameters();
		if (updateType){setValues1();}
		else{setValues2();}
		if (allowVis){
			s2.relaunch(song2, false, true);
			s2.setPreferredSize(new Dimension(s2.getNnx(), s2.getNny()));
		}
		progress++;
		position.setText((cycle)+" of "+repertoireList.size());
		listener=true;
	}

	
	public void saveAndQuit(){
		System.out.println("Finished");
		String userName=JOptionPane.showInputDialog("Please enter your name");
		if(userName!=null){
			String b=" , ";
			String t=")";
			String s="INSERT INTO comparesong (user, song1, song2, score, max_score, scheme_id)VALUES (";
			String s2="DELETE FROM comparesong WHERE user= '"+userName+"' AND scheme_id="+sckey;
			dbc.writeToDataBase(s2);
			int p2=-1;
			int p=1;
			for (int i=0; i<repertoireList.size(); i++){
				LinkedList holder=(LinkedList)repertoireList.get(i);
				Song song1=(Song)holder.get(0);
				if (holder.size()>1){
					for (int j=1; j<holder.size(); j++){
						Song song2=(Song)holder.get(j);
						dbc.writeToDataBase(s+"'"+userName+"'"+b+song1.getSongID()+b+song2.getSongID()+b+p+b+1+b+sckey+t);
					}
				}
				else{
					dbc.writeToDataBase(s+"'"+userName+"'"+b+song1.getSongID()+b+p2+b+p+b+1+b+sckey+t);
				}
			}
		}
		ac.cleanUp();
	}
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source==next){
			//if (progress==total){
				//saveAndQuit();
			//}
			//else{
			progressLabel="Simple Visual Comparison - Status: "+progress+" of "+total+" ";
			getNextPair1(false);
			//}
		}
		if (source==newType){
			if (progress==total){
				saveAndQuit();
			}
			else{
				progressLabel="Simple Visual Comparison - Status: "+progress+" of "+total+" ";
				getNextPair2(false);
			}
		}
		if (source==matchWorse){
			matchThisPair(false);
			progress++;
			if (progress==total){
				saveAndQuit();
			}
			else{
				progressLabel="Simple Visual Comparison - Status: "+progress+" of "+total+" ";
				getNextPair1(false);
			}
		}
		if (source==matchBetter){
			matchThisPair(true);
			progress++;
			if (progress==total){
				saveAndQuit();
			}
			else{
				progressLabel="Simple Visual Comparison - Status: "+progress+" of "+total+" ";
				getNextPair1(false);
			}
		}
		if ((listener)&&(source==play1)){
			
			LinkedList holder=(LinkedList)repertoireList.get(repertoireList.size()-cycle-1);
			cycle++;
			//song1=null;
			Song song1=getCurrentSong();
			
			song1.prepPlaybackAll();
		}
		if ((listener)&&(source==play2)){
			song2.prepPlaybackAll();
		}
		if (source==update1){replot1();}
		if (source==update2){replot2();}
		
	}
	
	public void propertyChange(PropertyChangeEvent e) {
        Object source = e.getSource();
		if (started){
			started=false;
			if (source==dynRange1){
				double s = ((Number)dynRange1.getValue()).doubleValue();
				if (s<=0){s=0.00001;}
				dynRange1.setValue(new Double(s));
				Song song1=getCurrentSong();
				song1.setDynRange(s);
			}
			if (source==echoRemoval1){
				double s = ((Number)echoRemoval1.getValue()).doubleValue();
				if (s<0){s=0;}
				if (s>1000){s=1000;}
				echoRemoval1.setValue(new Double(s));
				Song song1=getCurrentSong();
				song1.setEchoComp(s);
			}
			if (source==echoTail1){
				int s = (int)((Number)echoTail1.getValue()).doubleValue();
				if (s<0){s=0;}
				echoTail1.setValue(new Double(s));
				Song song1=getCurrentSong();
				song1.setEchoRange(s);
			}
			if (source==dynEq1){
				int te = ((Number)dynEq1.getValue()).intValue();
				if (te<0){te=0;}
				Song song1=getCurrentSong();
				song1.setDynEqual(te);
				dynEq1.setValue(new Double(te));
			}
			if (source==filterCutOff1){
				double te = ((Number)filterCutOff1.getValue()).doubleValue();
				Song song1=getCurrentSong();
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
	
	public void setValues1(){
		Song song1=getCurrentSong();
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
	
	public void setValues2(){
		Song song1=getCurrentSong();
		double s = ((Number)dynRange1.getValue()).doubleValue();
		if (s<=0){s=0.00001;}
		dynRange1.setValue(new Double(s));
		song1.setDynRange(s);
		
		s = ((Number)echoRemoval1.getValue()).doubleValue();
		if (s<0){s=0;}
		if (s>1000){s=1000;}
		echoRemoval1.setValue(new Double(s));
		song1.setEchoComp(s);
		
		int t = (int)((Number)echoTail1.getValue()).doubleValue();
		if (t<0){t=0;}
		echoTail1.setValue(new Double(t));
		song1.setEchoRange(t);
		
		t = ((Number)dynEq1.getValue()).intValue();
		if (t<0){t=0;}
		song1.setDynEqual(t);
		dynEq1.setValue(new Double(t));
		
		double te = ((Number)filterCutOff1.getValue()).doubleValue();
		if (te<0){te=0;}
		if (te>song1.getMaxF()){te=song1.getMaxF();}
		song1.setFrequencyCutOff(te);
		//song1.makeFilterBank();
		filterCutOff1.setValue(new Double(te));
		
		s = ((Number)dynRange2.getValue()).doubleValue();
		if (s<=0){s=0.00001;}
		dynRange2.setValue(new Double(s));
		song2.setDynRange(s);
		
		s = ((Number)echoRemoval2.getValue()).doubleValue();
		if (s<0){s=0;}
		if (s>1000){s=1000;}
		echoRemoval2.setValue(new Double(s));
		song2.setEchoComp(s);
		
		t = (int)((Number)echoTail2.getValue()).doubleValue();
		if (t<0){t=0;}
		echoTail2.setValue(new Double(t));
		song2.setEchoRange(t);
		
		t = ((Number)dynEq2.getValue()).intValue();
		if (t<0){t=0;}
		song2.setDynEqual(t);
		dynEq2.setValue(new Double(t));
		
		te = ((Number)filterCutOff2.getValue()).doubleValue();
		if (te<0){te=0;}
		if (te>song2.getMaxF()){te=song2.getMaxF();}
		song2.setFrequencyCutOff(te);
		//song1.makeFilterBank();
		filterCutOff2.setValue(new Double(te));
		
	}

	
}
