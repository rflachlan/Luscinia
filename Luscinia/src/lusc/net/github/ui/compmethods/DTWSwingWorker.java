package lusc.net.github.ui.compmethods;
//
//  DTWSwingWorker.java
//  Luscinia
//
//  Created by Robert Lachlan on 11/25/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

import java.util.LinkedList;

import javax.swing.*;

import lusc.net.github.analysis.CompressComparisons;
//import lusc.net.github.analysis.SongGroup;
import lusc.net.github.analysis.AnalysisGroup;
import lusc.net.github.analysis.PrepareDTW;
import lusc.net.github.ui.AnalysisChoose;
import lusc.net.github.Defaults;


public class DTWSwingWorker extends SwingWorker{
	
	AnalysisChoose ac;
	DTWPanel dtwp;
	//SongGroup sg;
	AnalysisGroup ag;
	Defaults defaults;
	PrepareDTW pdtw;
			
	public DTWSwingWorker(AnalysisChoose ac, DTWPanel dtwp, AnalysisGroup ag, Defaults defaults){
	//public DTWSwingWorker(AnalysisChoose ac, DTWPanel dtwp, SongGroup sg, Defaults defaults){
		this.ac=ac;
		this.dtwp=dtwp;
		//this.sg=sg;
		this.ag=ag;
		this.defaults=defaults;
	}
	
	public PrepareDTW doInBackground(){ 
		return(analyze());
	}
		
		
	public PrepareDTW analyze(){	
		pdtw=new PrepareDTW(dtwp, ag);
		ag.setSyllableRepetitionWeighting(dtwp.getSyllableRepetitionWeighting());
		ag.setStitchPunishment(dtwp.getStitchPunishment());
		ag.setAlignmentCost(dtwp.getAlignmentCost());
		
		
		try{		
			
			System.out.println("DTWPanel: Setting parameters");
			defaults.setDTWParameters(dtwp);

			ag.makeIndividualDays();
			
			
			System.out.println("DTWPanel: getting valid parameters");
			pdtw.getValidParameters(true);
			System.out.println("DTWPanel: compressing data");
			
			pdtw.compressData();
			
			if (dtwp.stitch.getSelectedIndex()!=0){
				pdtw.stitchSyllables();
			}
			
			pdtw.extractData();
			pdtw.prepareToNormalize();
			
			if (dtwp.stitch.getSelectedIndex()==0){
				ag.setScores(0, pdtw.startDTW(this, false));
			}
			
			if (dtwp.stitch.getSelectedIndex()>0){
				ag.setScores(0, pdtw.startDTW(this, true));
			}
			
			ag.compressSyllables();
			
			if (dtwp.stitch.getSelectedIndex()==2){
				pdtw.stitchSyllablesAll();
				pdtw.extractData();
				pdtw.prepareToNormalize();
				ag.augmentScores(pdtw.startDTW(this, true));	
			}
			
			ag.compressPhrases();
			
			if (ag.getSyllableRepetitionWeighting()>0){
				ag.augmentSyllDistanceMatrixWithSyllableReps();
			}
			
			
			/*
			System.out.println("DTWPanel: prepare to normalize");
			pdtw.prepareToNormalize();
			
			if (dtwp.stitch.getSelectedIndex()!=1){
				System.out.println("DTWPanel: element DTW running");
				//ag.setScores(0, pdtw.runDTW(this, false));
				ag.setScores(0, pdtw.startDTW(this, false));
				ag.compressSyllables();
			}
			else{
				//ag.setScores(0, null);
				//ag.setScores(2, null);
			}
			if (dtwp.stitch.getSelectedIndex()!=0){
				System.out.println("DTWPanel: stitching syllables");
				LinkedList<int[][]> d=pdtw.stitchSyllables(40);
				
				System.out.println("DTWPanel: normalizing");
				pdtw.prepareToNormalizeStitch();
				System.out.println("DTWPanel: syllable DTW running");
				ag.setScores(6, pdtw.startDTW(this, true));
				//ag.setScores(6, pdtw.runDTW(this, true));
				//ag.setScores(6, pdtw.tuneDTW(this, true));
				if (dtwp.stitch.getSelectedIndex()==1){
					ag.compressSyllablesStitch(d);
				}
				else{
					ag.compressSyllablesBoth();
				}
			}
			else{
				//ag.setScores(5,null);
			}
			if (ag.getSyllableRepetitionWeighting()>0){
				ag.augmentSyllDistanceMatrixWithSyllableReps();
			}
			System.out.println("DTWPanel: analysis finished");
			
			*/
		}
		catch(Exception e){
			e.printStackTrace();
		}	
		return pdtw;
	}
	
	public void progress(int p){
		Integer prog=new Integer(p);
		firePropertyChange("progress", null, prog);
	}
	
	public void done(){
		Integer prog=new Integer(0);
		firePropertyChange("progress", null, prog);
		if(!isCancelled()){
			ac.stepThree();
		}
	}
	
	public PrepareDTW getPDTW(){
		return pdtw;
	}

}
