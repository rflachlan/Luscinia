package lusc.net.github;
//
//  DTWSwingWorker.java
//  Luscinia
//
//  Created by Robert Lachlan on 11/25/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

import javax.swing.*;


public class DTWSwingWorker extends SwingWorker{
	
	AnalysisChoose ac;
	DTWPanel dtwp;
	SongGroup sg;
	
	public DTWSwingWorker(AnalysisChoose ac, DTWPanel dtwp, SongGroup sg){
		this.ac=ac;
		this.dtwp=dtwp;
		this.sg=sg;
	}
	
		
	public SongGroup doInBackground(){ //truly horrible

		try{
		System.out.println("DTWPanel: Labeling elements");
		sg.makeNames();
		sg.weightByAmp=dtwp.weightByAmp.isSelected();
		sg.logFrequencies=dtwp.logFrequencies.isSelected();
		sg.stitchSyllables=dtwp.stitch.getSelectedIndex();
		System.out.println("DTWPanel: Setting parameters");
		sg.defaults.setDTWParameters(sg);
		System.out.println("DTWPanel: getting valid parameters");
		sg.getValidParameters(true);
		System.out.println("DTWPanel: compressing data");
		sg.compressData2();
		System.out.println("DTWPanel: prepare to normalize");
		sg.prepareToNormalize(sg.data);
		System.out.println("DTWPanel: prepare to normalize per element");
		sg.prepareToNormalizePerElement(sg.data);
		//sg.normalizePerElement=dtwp.normalizePerElement.isSelected();
		//sg.normalize(sg.data);
		if (dtwp.stitch.getSelectedIndex()!=1){
			System.out.println("DTWPanel: element DTW running");
			sg.scoresEle=sg.runDTW(this, false);
			sg.compressSyllables2();
		}
		else{
			sg.scoresEle=null;
			sg.scoresSyll=null;
		}
		if (dtwp.stitch.getSelectedIndex()!=0){
			System.out.println("DTWPanel: syllable analysis");
			System.out.println("DTWPanel: getting parameters");
			sg.getValidParameters(true);
			System.out.println("DTWPanel: compressing data");
			sg.compressData2();
			System.out.println("DTWPanel: stitching syllables");
			sg.data=sg.stitchSyllables();
			System.out.println("DTWPanel: normalizing");
			sg.prepareToNormalize(sg.data);
			sg.prepareToNormalizePerElement(sg.data);
			//sg.normalize(sg.data);
			//sg.prepareToNormalize(sg.data);
			//sg.prepareToNormalizePerElement(sg.data);
			//sg.normalizePerElement=dtwp.normalizePerElement.isSelected();
			System.out.println("DTWPanel: syllable DTW running");
			sg.scoresSyll2=sg.runDTW(this, true);
			//sg.scoresSyll2=sg.normalizeScores(sg.scoresSyll2);
			//System.out.println("DTWPanel: transforming scores");
			//sg.scoresSyll2=sg.transformScores(sg.scoresSyll2);
			
			if (dtwp.stitch.getSelectedIndex()==1){
				
				CompressComparisons cc=new CompressComparisons();
				sg.scoresSyll=cc.compareSyllables5(sg.scoresSyll2, sg.songs, sg.alignmentCost);
			}
			else{
				sg.compressSyllables3();
			}
			
			
		}
		else{
			
			
			sg.scoresSyll2=null;
		}
		
		if (sg.syllableRepetitionWeighting>0){
			sg.augmentSyllDistanceMatrixWithSyllableReps();
		}
		
		System.out.println("DTWPanel: analysis finished");
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return sg;
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
	

}
