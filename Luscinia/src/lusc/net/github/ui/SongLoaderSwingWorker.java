package lusc.net.github.ui;
//
//  SongLoaderSwingWorker.java
//  Luscinia
//
//  Created by Robert Lachlan on 11/25/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

import javax.swing.*;

import java.util.*;

import lusc.net.github.Song;
import lusc.net.github.analysis.AnalysisGroup;

public class SongLoaderSwingWorker extends SwingWorker<AnalysisGroup, Object> {
	
	ComparisonScheme cs;
	AnalysisChoose ac;
	AnalysisGroup sg;
	
	private static String THRESH_COMMAND="threshold";
	
	public SongLoaderSwingWorker (ComparisonScheme cs, AnalysisChoose ac){
		this.cs=cs;
		this.ac=ac;
		
	}
	
	public AnalysisGroup doInBackground(){
				
		int tabID=cs.tabpane.getSelectedIndex();
		
		if (tabID==1){
			
			LinkedList<Song> songList=new LinkedList<Song>();
			LinkedList<int[]> idlist=new LinkedList<int[]>();
			int[][] table=cs.ca.table;
			for (int i=0; i<table.length; i++){
				for (int j=0; j<table.length; j++){
					if (table[j][i]==1){
						if ((i!=j)&&(table[i][j]==1)){table[i][j]=0;}
						int[] s={ac.archIds[i], ac.archIds[j]};
						int found[]={-1, -1};
						for (int k=0; k<songList.size(); k++){
							Song song=songList.get(k);
							if (song.getSongID()==s[0]){found[0]=k;}
							if (song.getSongID()==s[1]){found[1]=k;}
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
				Integer prog=new Integer((int)Math.round(100*(i+1)/table.length));
				firePropertyChange("progress", null, prog);
			}
			Song[] songs=new Song[songList.size()];
			songs=songList.toArray(songs);
			
			boolean[][] compScheme=new boolean[songs.length][songs.length];
			
			for (int i=0; i<idlist.size(); i++){
				int[] pair=idlist.get(i);
				compScheme[pair[0]][pair[1]]=true;
			}
			System.out.println("HERE");
			sg=new AnalysisGroup(songs, compScheme, cs.defaults, ac.dbc);
			if (cs.selectOneSyllable1.isSelected()){
				sg.pickJustOneExamplePerPhrase();
			}
			if (cs.eachElementASyllable1.isSelected()){
				sg.makeEveryElementASyllable();
			}
			if (cs.sylsByThresh1.isSelected()){
				
				sg.segmentSyllableBasedOnThreshold(cs.thresh1);
			}
		}  
		else {
			int ls=cs.sa.leftList.size();
			LinkedList<Song> songList=new LinkedList<Song>();
			for (int i=0; i<ls; i++){
				if (isCancelled()){break;}
				Integer q=(Integer)cs.sa.leftList.get(i);
				for (int j=0; j<ac.archIds.length; j++){
					if (q.intValue()==ac.archIds[j]){
						Song song=ac.dbc.loadSongFromDatabase(ac.archIds[j], 1);
						int ne=song.getNumElements();
						if (ne>0){
							LinkedList<String> list1=ac.dbc.populateContentPane(song.getIndividualID());
							song.setSx((String)list1.get(3));
							song.setSy((String)list1.get(4));
							
							songList.add(song);
						}	
					}
				}
				
				Integer prog=new Integer((int)Math.round(100*(i+1)/ls));
				firePropertyChange("progress", null, prog);
			}
			//int sckey=schemeKey2[load2.getSelectedIndex()];
			Song[] songs=new Song[songList.size()];
			
			songs=songList.toArray(songs);
			
			songList=null;
			try{
				sg=new AnalysisGroup(songs, cs.defaults, ac.dbc);
			}
			catch(Exception e){
				e.printStackTrace();
			}
			System.out.println("finished");
			if (cs.selectOneSyllable2.isSelected()){
				sg.pickJustOneExamplePerPhrase();
			}
			if (cs.eachElementASyllable2.isSelected()){
				sg.makeEveryElementASyllable();
			}
			if (cs.sylsByThresh2.isSelected()){
				sg.segmentSyllableBasedOnThreshold(cs.thresh2);
			}
		}
		
		return (sg);
    }
	
	protected void done(){
		Integer prog=new Integer(0);
		firePropertyChange("progress", null, prog);
		if (!isCancelled()){
			ac.stepTwo(sg);
		}
	}


}
