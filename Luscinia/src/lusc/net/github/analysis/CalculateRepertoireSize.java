package lusc.net.github.analysis;
//
//  CalculateRepertoireSize.java
//  Luscinia
//
//  Created by Robert Lachlan on 4/14/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

import java.util.*;

import lusc.net.github.Song;
import lusc.net.github.analysis.dendrograms.TreeDat;
import lusc.net.github.analysis.dendrograms.UPGMA;

public class CalculateRepertoireSize {
	
	double[][] results;
	
	//I THINK THIS IS TO BE JUNKED...
	
	public CalculateRepertoireSize(Song[] songs, UPGMA upgma, int[][] lookUps){
	
		
		
		LinkedList<Song> inds=new LinkedList<Song>();
		
		
		
		for (int i=0; i<songs.length; i++){
			System.out.println(songs[i].getIndividualID());
			boolean found=false;
			for (int j=0; j<inds.size(); j++){
				Song song=inds.get(j);
				if (song.getIndividualID()==songs[i].getIndividualID()){
					found=true;
					j=inds.size();
				}
			}
			if (!found){
				inds.add(songs[i]);
			}
		}
		
		
		int[] individuals=new int[inds.size()];
		
		for (int i=0; i<inds.size(); i++){
			Song song=inds.get(i);
			individuals[i]=song.getIndividualID();
			System.out.println("Individual: "+(i+1)+" "+song.getIndividualName());
		}
		inds=null;
	
		
		
		int[][] pm=upgma.getPartitionMembers();
		
		
		results=new double[individuals.length][pm.length];
		
		System.out.println("Calculating repertoire size: "+individuals.length+" individuals "+songs.length+" songs");
		
		boolean[] foundYet=new boolean[individuals.length];
		
		for (int i=0; i<pm.length; i++){
			if (pm[i]!=null){
				for (int j=0; j<pm[i].length; j++){
					
					for (int k=0; k<individuals.length; k++){
						foundYet[k]=false;
					}
					TreeDat[] dats=upgma.getDat();
					TreeDat dat=dats[pm[i][j]];
					for (int k=0; k<dat.children; k++){
						int p=lookUps[dat.child[k]][0];
						int q=songs[p].getIndividualID();
						for (int a=0; a<individuals.length; a++){
							if (q==individuals[a]){
								if (!foundYet[a]){
									foundYet[a]=true;
									results[a][i]++;
								}
							}
						}
					}
				}
				for (int j=0; j<individuals.length; j++){
					System.out.print(results[j][i]+" ");
				}
				System.out.println();
			}
		}
	}
	
	

}
