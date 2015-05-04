package lusc.net.github.db;
//
//  DataBaseController.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/10/2005.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import java.util.*;
import java.io.*;

import lusc.net.github.Song;

public class DataBaseController {
	String dbName;
	private DbConnection db;
	
	public String getDBName(){
		return dbName;
	}
	
	public void setDBName(String s){
		dbName=s;
	}
	
	public DataBaseController(DbConnection db){
		this.db=db;
		dbName=db.dbase;
	}
	
	public void reconnect(){
		db.doConnect();
	}
	
	public LinkedList loadSchemes(boolean complex){
		//db.doConnect();
		LinkedList a=db.loadSchemes(complex);
		//db.disconnect();
		return a;
	}
	
	public boolean testScheme(String testName){
		//db.doConnect();
		boolean existsAlready=db.testScheme(testName);
		//db.disconnect();
		return existsAlready;
	}
	
	public int writeIndividualIntoDatabase(String[] details){
		//db.doConnect();
		int a=db.writeIndividualIntoDatabase(details);
		//db.disconnect();
		return a;
	}
	
	public void writeToDataBase(String s){
		//db.doConnect();
		db.writeToDataBase(s);
		//db.disconnect();
	}
	
	public void writeElementComparison(int song1, int ele1, int song2, int ele2, float score){
		//db.doConnect();
		db.writeElementComparison(song1, ele1, song2, ele2, score);
		//db.disconnect();
	}
	
	public void writeCompSongComparison(int song1, int song2, float[][] score){
		//db.doConnect();
		db.writeCompSongComparison(song1, song2, score);
		//db.disconnect();
	}
	/*
	public LinkedList readCompSongComparison(int[][] details){
		//db.doConnect();
		LinkedList results=db.readCompSongComparison(details);
		//db.disconnect();
		return results;
	}
	*/
	public void writeEleForm(float[] results, int id){
		//db.doConnect();
		db.writeEleForm(results, id);
		//db.disconnect();
	}
	
	public float[][] getSongComp(int a, int b, int c, int d){
		//db.doConnect();
		float[][] results=db.getSongComp(a, b, c, d);
		//db.disconnect();
		return results;
	}
	
	public float[][][] getSongComp(int a, int b, int[]c){
		//db.doConnect();
		float[][][] results=db.getSongComp(a, b, c);
		//db.disconnect();
		return results;
	}
	
	public void resetCompTable(){
		//db.doConnect();
		//db.resetCompTable();
		//db.disconnect();
	}
	
	public String[] showColumns(String s){
		//db.doConnect();
		String[] out=db.showColumns(s);
		//db.disconnect();
		return out;
	}
	
	public LinkedList<String[]> readFromDataBase(String s, int[]whattoget){
		//db.doConnect();
		LinkedList<String[]> store=db.readFromDataBase(s, whattoget);
		//db.disconnect();
		return store;
	}
	
	public int[] readFromDataBase(){
		//db.doConnect();
		int[] store=db.readFromDataBase();
		//db.disconnect();
		return store;
	}
	
	public int readIndividualNameFromDB(String name){
		int re=db.readIndividualNameFromDB(name);
		return re;
	}
	
	public int readSongID(String name, int indID){
		int re=db.readSongID(name, indID);
		return re;
	}
	
	public LinkedList<String> populateContentPane(int ID){
		//db.doConnect();
		LinkedList<String> store=db.populateContentPane(ID);
		//db.disconnect();
		return store;
	}
	
	
	
	public LinkedList<?> loadParametersFromDatabase(int ID){
		//db.doConnect();
		LinkedList<?> store=db.loadParametersFromDatabase(ID);
		//db.disconnect();
		return store;
	}
	
	public LinkedList<Song> loadSongDetailsFromDatabase(){
		LinkedList<Song> olist=db.loadSongDetailsFromDatabase();
		return olist;
	}

	
	
		
	public Song loadSongFromDatabase(int ID, int info){
		//db.doConnect();
		if (db.connected){
			Song song=db.loadSongFromDatabase(ID, info);
			//System.out.println("I: "+song.individualID);
			song.setEleList(db.loadElementsFromDatabase(ID, song));
			//System.out.println("I: "+song.individualID);
			song.setSyllList(db.loadSyllablesFromDatabase(ID));
			if (db.oldloader){
				song.updateSyllableList();
			}

			song.sortSyllsEles();
			song.interpretSyllables();
						
			LinkedList<?> store2=db.populateContentPane(song.getIndividualID());
			song.setSongID(ID);
			song.setIndividualName((String)store2.get(0));
			song.setGridType((String)store2.get(2));
			song.setLocationX((String)store2.get(3));
			song.setLocationY((String)store2.get(4));
			song.setSpecies((String)store2.get(5));
			song.setPopulation((String)store2.get(6));

			return song;
		}
		return null;
	}
	
	public void writeWholeSong(Song song, int indID, File f){
		
		String sname=song.getName();
		writeToDataBase("INSERT INTO songdata (name, IndividualID) VALUES ('"+sname+"' , "+indID+")");		
		song.setSongID(readSongID(sname, indID));
		writeSongIntoDatabase(sname, song.getSongID(), f);
		writeSongMeasurements(song);
		writeSongInfo(song);
		//is that all???
	}
	
	public void writeSongMeasurements(Song song){
		//db.doConnect();
		db.writeSongMeasurements(song);
		//db.disconnect();
	}
	
	public void writeSongIntoDatabase(String name, int p, File f){
		//db.doConnect();
		db.writeSongIntoDatabase(name, p, f);
		//db.disconnect();
	}
	
	public void writeSongIntoDatabase(Song song){
		//db.doConnect();
		db.writeSongIntoDatabase(song);
		//db.disconnect();
	}
	
	public void writeSongInfo(Song song){
		//db.doConnect();
		db.writeSongInfo(song);
		//db.disconnect();
	}
	
	public void writeDateIntoDatabase(String name, int ID){
		//db.doConnect();
		writeDateIntoDatabase(name, ID);
		//db.disconnect();
	}
	
	public void writeTimeIntoDatabase(String name, int ID){
		//db.doConnect();
		writeTimeIntoDatabase(name, ID);
		//db.disconnect();
	}
	
	public LinkedList<Object> getOutputVisual(int s){
		//db.doConnect();
		LinkedList<Object> outd=db.getOutputVisual(s);
		//db.disconnect();
		return outd;
	}
	
	public DbConnection getConnection(){
		return db;
	}
	
	public String getDBaseName(){
		return db.dbase;
	}
	
	public String getUserID(){
		return db.uname;
	}
	
	public LinkedList<String[]> getUserList(){ 
		String st="SELECT * FROM INFORMATION_SCHEMA.USERS";
		int[] a={1,2};
		LinkedList<String[]> s=db.readFromDataBase(st, a);
		return s;		
	}
	
	public void dropUser(String s){ 
		String st="DROP USER "+s;
		db.writeToDataBase(st);	
	}
	
	public void addUser(String s){ 
		String st="CREATE USER "+s+" PASSWORD ' '";
		String st2="GRANT ALL ON comparesong, comparesyll, compareele, comparesongcomp,comparesongcomp, element, individual, population, songdata, species, specpop, syllable, wavs TO "+s;

		db.writeToDataBase(st);	
		db.writeToDataBase(st2);
	}
	
	public void addUserPassword(String s, String p){ 
		String st="CREATE USER "+s+" PASSWORD '"+p+"'";
		String st2="GRANT ALL ON comparesong, comparesyll, compareele, comparesongcomp,comparesongcomp, element, individual, population, songdata, species, specpop, syllable, wavs TO "+s;

		db.writeToDataBase(st);	
		db.writeToDataBase(st2);
	}
	
	public void setPassword(String uname, String pword){
		//String st="DROP USER "+uname;
		//db.writeToDataBase(st);
		String st2="ALTER USER "+uname+" SET PASSWORD '"+pword+"'";
		db.writeToDataBase(st2);
	}
	
	public void alterAdmin(String s, Boolean b){
		String t="FALSE";
		if (b.booleanValue()==true){
			t="TRUE";
		}
		String st="ALTER USER "+s+" ADMIN "+t;
		db.writeToDataBase(st);
	}
	
	public String getURLinfo(){
		return db.URLinfo;
		
	}
	
	
	
}
