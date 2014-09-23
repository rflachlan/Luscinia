package lusc.net.sourceforge;
//
//  DataBaseController.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/10/2005.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import java.util.*;
import java.io.*;

public class DataBaseController {
	String dbName;
	private DbConnection db;
	
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
	
	
		
	public Song loadSongFromDatabase(int ID, int info){
		//db.doConnect();
		if (db.connected){
			Song song=db.loadSongFromDatabase(ID, info);
			//System.out.println("I: "+song.individualID);
			song.eleList=db.loadElementsFromDatabase(ID, song);
			//System.out.println("I: "+song.individualID);
			song.syllList=db.loadSyllablesFromDatabase(ID);
			if (db.oldloader){
				System.out.println("UPDATING!!!");
				for(int i=0; i<song.syllList.size(); i++){
					int[] s=song.syllList.get(i);
					int[]t=new int[2];
					t[0]=(int)Math.round(s[0]*song.dx);
					t[1]=(int)Math.round(s[1]*song.dx);
					song.syllList.remove(i);
					song.syllList.add(t);
				}
			}

			song.sortSyllsEles();
			song.interpretSyllables();
						
			LinkedList<?> store2=db.populateContentPane(song.individualID);
			song.songID=ID;
			song.individualName=(String)store2.get(0);
			song.locationX=(String)store2.get(3);
			song.locationY=(String)store2.get(4);
			song.species=(String)store2.get(5);
			song.population=(String)store2.get(6);
			System.out.println(song.population);
			if (song.species==null){song.species=" ";}
			if (song.population==null){song.population=" ";}
			//db.disconnect();
			return song;
		}
		return null;
	}
	
	public void writeWholeSong(Song song, int indID, File f){
		
		writeToDataBase("INSERT INTO songdata (name, IndividualID) VALUES ('"+song.name+"' , "+indID+")");		
		song.songID=readSongID(song.name, indID);
		System.out.println(song.songID);
		writeSongIntoDatabase(song.name, song.songID, f);
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
	
	protected DbConnection getConnection(){
		return db;
	}
	
	protected String getDBaseName(){
		return db.dbase;
	}
	
	protected String getUserID(){
		return db.uname;
	}
	
	protected LinkedList<String[]> getUserList(){ 
		String st="SELECT * FROM INFORMATION_SCHEMA.USERS";
		int[] a={1,2};
		LinkedList<String[]> s=db.readFromDataBase(st, a);
		return s;		
	}
	
	protected void dropUser(String s){ 
		String st="DROP USER "+s;
		db.writeToDataBase(st);	
	}
	
	protected void addUser(String s){ 
		String st="CREATE USER "+s+" PASSWORD ' '";
		String st2="GRANT ALL ON comparesong, comparesyll, compareele, comparesongcomp,comparesongcomp, element, individual, population, songdata, species, specpop, syllable, wavs TO "+s;

		db.writeToDataBase(st);	
		db.writeToDataBase(st2);
	}
	
	protected void setPassword(String uname, String pword, Boolean b){
		String st="DROP USER "+uname;
		db.writeToDataBase(st);
		String t="";
		if (b.booleanValue()==true){t=" ADMIN";}
		String st2="CREATE USER "+uname+" PASSWORD '"+pword+"'"+t;
		String st3="GRANT ALL ON comparesong, comparesyll, compareele, comparesongcomp,comparesongcomp, element, individual, population, songdata, species, specpop, syllable, wavs TO "+uname;
		db.writeToDataBase(st2);
		db.writeToDataBase(st3);
	}
	
	protected void alterAdmin(String s, Boolean b){
		String t="FALSE";
		if (b.booleanValue()==true){
			t="TRUE";
		}
		String st="ALTER USER "+s+" ADMIN "+t;
		db.writeToDataBase(st);
	}
	
	protected String getURLinfo(){
		return db.URLinfo;
		
	}
	
	
	
}
