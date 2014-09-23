package lusc.net.github;
//
//  Updater.java
//  Luscinia
//
//  Created by Robert Lachlan on 8/24/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

import java.util.*;




public class Updater {
	DataBaseController dbc;
	Defaults defaults;
	LinkedList tstore, ustore;
	int[][] numberOfSongs;
	
	public Updater(DataBaseController dbc, Defaults defaults){
		this.dbc=dbc;
		this.defaults=defaults;
		
		startUpdate3();
	}
	
	public void startUpdate2(){
		//this is a kludgy fix for errors in fundamental frequency change estimation that occurred temporarily and only affected one or two db's
		
		extractFromDatabase();
		
		int numIndividuals=tstore.size();
		SpectrPane sp=new SpectrPane();
		for (int i=0; i<numIndividuals; i++){
			//for (int i=1; i<2; i++){
			for (int j=0; j<numberOfSongs[i].length; j++){
				try{
					String[] so=(String[])ustore.get(numberOfSongs[i][j]);
					int songId=myIntV(so[1]);
					Song song=dbc.loadSongFromDatabase(songId, 0); 
					System.out.println(song.name);
					if (song.population.startsWith("Europe, Hamp")){
						for (int k=0; k<song.eleList.size(); k++){
							Element ele=(Element)song.eleList.get(k);
							for (int a=0; a<ele.measurements.length; a++){
								ele.measurements[a][7]=ele.measurements[a][4];
							}
						}
						dbc.writeSongMeasurements(song);
					}
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	
	public void startUpdate3(){
		//this updatesAzores population labels
		
		extractFromDatabase();
		
		int numIndividuals=tstore.size();
		SpectrPane sp=new SpectrPane();
		for (int i=0; i<numIndividuals; i++){
			//for (int i=1; i<2; i++){
			for (int j=0; j<numberOfSongs[i].length; j++){
				try{
					String[] so=(String[])ustore.get(numberOfSongs[i][j]);
					int songId=myIntV(so[1]);
					Song song=dbc.loadSongFromDatabase(songId, 0); 
					System.out.println(song.name);
					if (song.population.startsWith("Azores")){
						String s="Azores";
						if ((song.name.startsWith("fai"))||(song.name.startsWith("Fai"))){
							s="Azores, Faial";
						}
						if ((song.name.startsWith("flo"))||(song.name.startsWith("Flo"))){
							s="Azores, Flores";
						}
						if ((song.name.startsWith("mig"))||(song.name.startsWith("Mig"))){
							s="Azores, Sao Miguel";
						}
						if ((song.name.startsWith("pic"))||(song.name.startsWith("Pic"))){
							s="Azores, Pico";
						}
						
						String t="UPDATE individual SET PopID='"+s+"' WHERE id="+song.individualID;
						
						dbc.writeToDataBase(t);
						
						String u=song.name.substring(0, 5);
						String v="UPDATE individual SET name='"+u+"' WHERE id="+song.individualID;
						dbc.writeToDataBase(v);
					}
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	
		
	public void startUpdate(){
		//this update recalculates all the vibrato amplitude values
		
		extractFromDatabase();

		int numIndividuals=tstore.size();
		SpectrPane sp=new SpectrPane();
		for (int i=0; i<numIndividuals; i++){
			//for (int i=1; i<2; i++){
			for (int j=0; j<numberOfSongs[i].length; j++){
				try{
					String[] so=(String[])ustore.get(numberOfSongs[i][j]);
					int songId=myIntV(so[1]);
					Song song=dbc.loadSongFromDatabase(songId, 0); 
					System.out.println(song.name);
					sp.song=song;
					sp.updateTrillMeasures();
					dbc.writeSongMeasurements(song);
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	
	public int myIntV(String s){
		Integer p1=Integer.valueOf(s);
		int p=p1.intValue();
		return p;
	}
	
	public void extractFromDatabase(){
		int [] indq={1,2};
		int [] sonq={1,2,3};
		tstore=null;
		tstore=new LinkedList();
		String query="SELECT name, id FROM individual";
		tstore.addAll(dbc.readFromDataBase(query, indq));
		
		ustore=null;
		ustore=new LinkedList();
		query="SELECT name, id, IndividualID FROM songdata";
		ustore.addAll(dbc.readFromDataBase(query, sonq));
		numberOfSongs=new int[tstore.size()][];
		for (int i=0; i<tstore.size(); i++){
			String[] s=(String[])tstore.get(i);
			int counter=0;
			for (int j=0; j<ustore.size(); j++){
				String[] t=(String[])ustore.get(j);
				if (s[1].equals(t[2])){
					counter++;
				}
			}
			numberOfSongs[i]=new int[counter];
			counter=0;
			for (int j=0; j<ustore.size(); j++){
				String[] t=(String[])ustore.get(j);
				if (s[1].equals(t[2])){
					numberOfSongs[i][counter]=j;
					counter++;
				}
			}
		}
	}
	
	
}
		 
	


