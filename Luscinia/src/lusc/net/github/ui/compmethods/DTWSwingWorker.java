package lusc.net.github.ui.compmethods;
//
//  DTWSwingWorker.java
//  Luscinia
//
//  Created by Robert Lachlan on 11/25/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

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
	
	public DTWSwingWorker(AnalysisChoose ac, DTWPanel dtwp, AnalysisGroup ag, Defaults defaults){
	//public DTWSwingWorker(AnalysisChoose ac, DTWPanel dtwp, SongGroup sg, Defaults defaults){
		this.ac=ac;
		this.dtwp=dtwp;
		//this.sg=sg;
		this.ag=ag;
		this.defaults=defaults;
	}
	
	
	public PrepareDTW doInBackground(){ //truly horrible
		PrepareDTW pdtw=new PrepareDTW(dtwp, ag);
		ag.setSyllableRepetitionWeighting(dtwp.getSyllableRepetitionWeighting());
		ag.setStitchPunishment(dtwp.getStitchPunishment());
		ag.setAlignmentCost(dtwp.getAlignmentCost());
		
		
		try{		
			System.out.println("DTWPanel: Labeling elements");
			ag.makeNames();
			System.out.println("DTWPanel: Setting parameters");
			defaults.setDTWParameters(dtwp);

			System.out.println("DTWPanel: getting valid parameters");
			pdtw.getValidParameters(true);
			System.out.println("DTWPanel: compressing data");
			pdtw.compressData();
			System.out.println("DTWPanel: prepare to normalize");
			pdtw.prepareToNormalize();
			
			if (dtwp.stitch.getSelectedIndex()!=1){
				System.out.println("DTWPanel: element DTW running");
				ag.setScores(0, pdtw.runDTW(this, false));
				ag.compressSyllables();
			}
			else{
				ag.setScores(0, null);
				ag.setScores(2, null);
			}
			if (dtwp.stitch.getSelectedIndex()!=0){
				System.out.println("DTWPanel: stitching syllables");
				pdtw.stitchSyllables();
				System.out.println("DTWPanel: normalizing");
				pdtw.prepareToNormalizeStitch();
				System.out.println("DTWPanel: syllable DTW running");
				ag.setScores(5, pdtw.runDTW(this, true));
			
				if (dtwp.stitch.getSelectedIndex()==1){
					ag.compressSyllablesStitch();
				}
				else{
					ag.compressSyllablesBoth();
				}
			}
			else{
				ag.setScores(5,null);
			}
			if (ag.getSyllableRepetitionWeighting()>0){
				ag.augmentSyllDistanceMatrixWithSyllableReps();
			}
			System.out.println("DTWPanel: analysis finished");
		}
		catch(Exception e){
			e.printStackTrace();
		}	
		return pdtw;
	}
	
	/*	
	public SongGroup doInBackground(){ //truly horrible

		try{
			System.out.println("DTWPanel: Labeling elements");
			sg.makeNames();
			//sg.setWeightByAmp(dtwp.weightByAmp.isSelected());
			//sg.logFrequencies=dtwp.logFrequencies.isSelected();
			//sg.stitchSyllables=dtwp.stitch.getSelectedIndex();
			System.out.println("DTWPanel: Setting parameters");
			//defaults.setDTWParameters(sg);
			defaults.setDTWParameters(dtwp);
			
			
			System.out.println("DTWPanel: getting valid parameters");
			sg.getValidParameters(true);
			System.out.println("DTWPanel: compressing data");
			sg.compressData2();
			System.out.println("DTWPanel: prepare to normalize");
			sg.prepareToNormalize();
			System.out.println("DTWPanel: prepare to normalize per element");
			sg.prepareToNormalizePerElement();
			//sg.normalizePerElement=dtwp.normalizePerElement.isSelected();
			//sg.normalize(sg.data);
			if (dtwp.stitch.getSelectedIndex()!=1){
				System.out.println("DTWPanel: element DTW running");
				sg.setScoresEle(sg.runDTW(this, false));
				sg.compressSyllables2();
			}
			else{
				sg.setScoresEle(null);
				sg.setScoresSyll(null);
			}
			if (dtwp.stitch.getSelectedIndex()!=0){
				System.out.println("DTWPanel: syllable analysis");
				System.out.println("DTWPanel: getting parameters");
				sg.getValidParameters(true);
				System.out.println("DTWPanel: compressing data");
				sg.compressData2();
				System.out.println("DTWPanel: stitching syllables");
				sg.stitchSyllables2();
				System.out.println("DTWPanel: normalizing");
				sg.prepareToNormalize();
				sg.prepareToNormalizePerElement();
				//sg.normalize(sg.data);
				//sg.prepareToNormalize(sg.data);
				//sg.prepareToNormalizePerElement(sg.data);
				//sg.normalizePerElement=dtwp.normalizePerElement.isSelected();
				System.out.println("DTWPanel: syllable DTW running");
				sg.setScoresSyll2(sg.runDTW(this, true));
				
				//sg.scoresSyll2=sg.normalizeScores(sg.scoresSyll2);
				//System.out.println("DTWPanel: transforming scores");
				//sg.scoresSyll2=sg.transformScores(sg.scoresSyll2);
			
				if (dtwp.stitch.getSelectedIndex()==1){
					sg.compressSyllables5();
				}
				else{
					sg.compressSyllables3();
				}
			
			
			}
			else{
				sg.setScoresSyll2(null);
			}
			if (sg.getSyllableRepetitionWeighting()>0){
				sg.augmentSyllDistanceMatrixWithSyllableReps();
			}
			System.out.println("DTWPanel: analysis finished");
		}
		catch(Exception e){
			e.printStackTrace();
		}	
		return sg;
	}
	*/
	
	
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
	

}
