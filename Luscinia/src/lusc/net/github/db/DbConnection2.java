package lusc.net.github.db;
//
//  DbConnection2.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/10/2005.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import java.util.*;
import java.sql.*;
import java.io.*;
import javax.sound.sampled.*;
import java.nio.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.text.*;

public class DbConnection2 {
	Connection con=null;
	boolean connected=false;
	String uname=" ";
	protected String pword=" ";
	String loc="localhost";
	String dbase=" ";
	int DBMODE=0;
	
	
	public void connect(){
		connect (uname, pword, dbase, loc, DBMODE);
	}
	
	public boolean getConnected(){
		return connected;
	}
	
	
	public void connect (String uname, String pword, String dbase, String loc, int DBMODE){
	
		this.uname=uname;
		this.pword=pword;
		this.dbase=dbase;
		this.loc=loc;
		this.DBMODE=DBMODE;
		//dbase=" ";
		String url=" ";
		try{
			if (DBMODE==1){
				url="jdbc:mysql://"+loc+":3306/"+dbase;
			}
			else if (DBMODE==2){
				url="jdbc:h2:"+loc;
				uname="sa";
				pword="";
			}
			else if (DBMODE==0){
				url="jdbc:hsqldb:file:"+loc;
				uname="sa";
				pword="";
			}
			if ((dbase!=null)&&(dbase.startsWith(" "))){
				if (DBMODE==1){
					url="jdbc:mysql://"+loc+":3306/mysql";
				}
				else if (DBMODE==0){
					url="jdbc:hsqldb:file:";
				}
			}
			
			
			//String url="jdbc:mysql://localhost:3306/"+dbase;
			ResultSet rs;
			try {
				if (DBMODE==1){
					Class.forName("com.mysql.jdbc.Driver");
				}
				else if (DBMODE==2){
					Class.forName("org.h2.Driver");
				}
				else if (DBMODE==0){
					Class.forName("org.hsqldb.jdbcDriver");
				}
			}
			catch(Exception e){}
			con = DriverManager.getConnection(url, uname, pword); 
			if (con!=null){connected=true;}
        }
        catch(SQLException ex ) {
            ex.printStackTrace();
			System.out.println("SQLException: " + ex.getMessage()); 
            System.out.println("SQLState: " + ex.getSQLState()); 
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        finally {

        }
    }
	
	public void shutdown(){
		try{
			Statement st = con.createStatement();
			st.execute("SHUTDOWN");
			con.close();    // if there are no other open connection
		}
		catch(SQLException ex ) {
            ex.printStackTrace();
			System.out.println("SQLException: " + ex.getMessage()); 
            System.out.println("SQLState: " + ex.getSQLState()); 
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        finally {

        }
    }


	public void disconnect(){
		Statement stmt=null;
		try { 
			if (DBMODE==1){
				con.close(); connected=false;
			}
			else if (DBMODE==0){
				stmt = con.createStatement(); 
				stmt.executeUpdate("SHUTDOWN");
				connected=false;
			}
		}
		catch( Exception e ) { }		
		finally { 
			if (stmt != null) { 
				try { stmt.close();} catch (SQLException sqlEx) {} 
				stmt = null; 
			} 
		}
		
	}
	
	public String[] getListOfDatabases(){
		String[] results=null;
		if (DBMODE==1){
			results=getListOfDatabasesMySQL();
		}
		else{
			results=getListOfDatabasesHSQLDB();
		}
		return results;
	}
	
	
	public String[] getListOfDatabasesMySQL(){
		Statement stmt = null; 
		ResultSet rs = null; 
		String[] nam=null;
		String query="SHOW DATABASES";
		try {
			stmt = con.createStatement(); 
			rs = stmt.executeQuery(query); 
			if (stmt.execute(query)) { 
				rs = stmt.getResultSet();
				LinkedList t=new LinkedList();
				while (rs.next()){
					String s=rs.getString("Database");
					if (!s.startsWith("mysql")){t.add(s);}
				}
				nam=new String[t.size()];
				for (int i=0; i<t.size(); i++){
					nam[i]=(String)t.get(i);
				}
			} 
		} 
		catch (SQLException ex){
			System.out.println("SQLException: " + ex.getMessage()); 
            System.out.println("SQLState: " + ex.getSQLState()); 
            System.out.println("VendorError: " + ex.getErrorCode());

		}
		finally { 
			if (rs != null) { 
				try {rs.close();} catch (SQLException sqlEx) {} 
				rs = null; 
			}
			if (stmt != null) { 
				try { stmt.close();} catch (SQLException sqlEx) {} 
				stmt = null; 
			} 
		}
		return nam;	
	}
	
	public String[] getListOfDatabasesHSQLDB(){
		String[] results=null;
		try{
			File dbFile=new File(loc);
			File[] individualFiles1=dbFile.listFiles();
			LinkedList directoryFiles=new LinkedList();
			for (int i=0; i<individualFiles1.length; i++){
				if (individualFiles1[i].isDirectory()){
					directoryFiles.add(individualFiles1[i]);
				}
			}
	
			results=new String[directoryFiles.size()];
			for (int i=0; i<results.length; i++){
				File file=(File)directoryFiles.get(i);
				results[i]=file.getName();
			}
			
		}
		catch (Exception e){}
		return results;
	}
	
	public String[] getListOfUsers(){
		
		Statement stmt = null; 
		ResultSet rs = null; 
		String[] nam=null;
		String query="SELECT user FROM mysql.user";
		try {
			stmt = con.createStatement(); 
			rs = stmt.executeQuery(query); 
			if (stmt.execute(query)) { 
				rs = stmt.getResultSet();
				LinkedList t=new LinkedList();
				while (rs.next()){
					String s=rs.getString(1);
					t.add(s);
				}
				nam=new String[t.size()];
				for (int i=0; i<t.size(); i++){
					nam[i]=(String)t.get(i);
				}
			} 
		} 
		catch (Exception e){
		
		}
		finally { 
			if (rs != null) { 
				try {rs.close();} catch (SQLException sqlEx) {} 
				rs = null; 
			}
			if (stmt != null) { 
				try { stmt.close();} catch (SQLException sqlEx) {} 
				stmt = null; 
			} 
		}
		return nam;	
	}
	
	public String[] getListOfTables(String database){
		
		Statement stmt = null; 
		ResultSet rs = null; 
		String[] nam=null;
		String query="SHOW TABLES FROM "+database;
		try {
			stmt = con.createStatement(); 
			rs = stmt.executeQuery(query); 
			if (stmt.execute(query)) { 
				rs = stmt.getResultSet();
				LinkedList t=new LinkedList();
				while (rs.next()){
					String s=rs.getString(1);
					t.add(s);
				}
				nam=new String[t.size()];
				
				for (int i=0; i<t.size(); i++){
					nam[i]=(String)t.get(i);
				}
			} 
		} 
		catch (Exception e){
		
		}
		finally { 
			if (rs != null) { 
				try {rs.close();} catch (SQLException sqlEx) {} 
				rs = null; 
			}
			if (stmt != null) { 
				try { stmt.close();} catch (SQLException sqlEx) {} 
				stmt = null; 
			} 
		}
		return nam;	
	}
	
	public boolean deleteDatabase(String s){
		boolean rv=true;
		if (DBMODE==1){
			rv=deleteDatabaseMySQL(s);
		}
		else if (DBMODE==0){
			//rv=deleteDatabaseHSQLDB(s);
			//No need to do this yet?
		}
		return rv;
	}
	
	public boolean deleteDatabaseMySQL(String s){
		Statement stmt = null; 
		ResultSet rs = null; 
		String querya="DROP DATABASE "+s;
		boolean rv=true;
		try {
			stmt = con.createStatement(); 
			stmt.executeUpdate(querya);
			} 
		catch (Exception e){
			e.printStackTrace();
			rv=false;
		}
		finally { 
			if (rs != null) { 
				try {rs.close();} catch (SQLException sqlEx) {} 
				rs = null; 
			}
			if (stmt != null) { 
				try { stmt.close();} catch (SQLException sqlEx) {} 
				stmt = null; 
			} 
		}
		return rv;
	}


	public boolean createDatabase(String s){
		boolean rv=true;
		if (DBMODE==1){
			rv=createDatabaseMySQL(s);
		}
		else {
			rv=createDatabaseHSQLDB(s);
		}
		return rv;
	}
	
	
	public boolean createDatabaseMySQL(String s){
		Statement stmt = null; 
		ResultSet rs = null; 
		String querya="CREATE DATABASE "+s;
		String queryb="CREATE TABLE "+s+".";
		//String queryb="CREATE TABLE ";
		boolean rv=true;
		try {
			stmt = con.createStatement(); 
			stmt.executeUpdate(querya);
			
			String comp="comparescheme (id INT PRIMARY KEY NOT NULL AUTO_INCREMENT, name CHAR(30), song1 INT, song2 INT, max_score INT, syll_comp INT, song_comp INT)";

			

			stmt = con.createStatement(); 
			stmt.executeUpdate(queryb+comp);
			
			String comp2="comparesong (user CHAR(50), song1 INT, song2 INT, score FLOAT, max_score FLOAT, scheme_id INT)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(queryb+comp2);
			
			String comp3="comparesyll (user CHAR(50), song1 INT, song2 INT, syll1 INT, syll2 INT, score FLOAT, max_score FLOAT, scheme_id INT)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(queryb+comp3);
			
			String comp4="compareele (user CHAR(50), song1 INT, song2 INT, syll1 INT, syll2 INT, ele1 INT, ele2 INT, score FLOAT, max_score FLOAT, scheme_id INT)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(queryb+comp4);
			
			String comp5="comparesongcomp (song1 INT, song2 INT, score BLOB)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(queryb+comp5);
			
			String eleindex="ALTER TABLE "+s+".comparesong ADD INDEX (song1)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			eleindex="ALTER TABLE "+s+".comparesongcomp ADD INDEX (song1)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			eleindex="ALTER TABLE "+s+".comparesongcomp ADD INDEX (song2)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			eleindex="ALTER TABLE "+s+".comparesong ADD INDEX (song2)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			eleindex="ALTER TABLE "+s+".comparesong ADD INDEX (scheme_id)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			eleindex="ALTER TABLE "+s+".comparesyll ADD INDEX (song1)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			eleindex="ALTER TABLE "+s+".comparesyll ADD INDEX (song2)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			eleindex="ALTER TABLE "+s+".comparesyll ADD INDEX (syll1)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			eleindex="ALTER TABLE "+s+".comparesyll ADD INDEX (syll2)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			eleindex="ALTER TABLE "+s+".comparesyll ADD INDEX (scheme_id)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			eleindex="ALTER TABLE "+s+".compareele ADD INDEX (song1)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			eleindex="ALTER TABLE "+s+".compareele ADD INDEX (song2)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			eleindex="ALTER TABLE "+s+".compareele ADD INDEX (syll1)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			eleindex="ALTER TABLE "+s+".compareele ADD INDEX (syll2)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			eleindex="ALTER TABLE "+s+".compareele ADD INDEX (scheme_id)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			//String ele="element (id INT PRIMARY KEY NOT NULL AUTO_INCREMENT, SongID INT, data mediumtext, data2 mediumtext, powerspectrum mediumtext, starttime int, maxfreq float, minfreq float, startfreq float, endfreq float, avfreq float, maxfundfreq float, minfundfreq float, startfundfreq float, endfundfreq float, avfundfreq float, maxpeakfreq float, minpeakfreq float, overallpeakfreq float, peakfreq float, timelength float, gapbefore float, gapafter float, timemax float, timemin float, timemaxfund float, timeminfund float, maxband float, minband float, startband float, endband float, avband float, timestep float, framelength float, maxf int, windowmethod int, dy float, dynrange float, dyncomp float, echorange int, echocomp float)";
			String ele="element (id INT PRIMARY KEY NOT NULL AUTO_INCREMENT, SongID INT, signal mediumtext, peakfreq mediumtext, fundfreq mediumtext, meanfreq mediumtext, medianfreq mediumtext, peakfreqchange mediumtext, fundfreqchange mediumtext, meanfreqchange mediumtext, medianfreqchange mediumtext, harmonicity mediumtext, wiener mediumtext, bandwidth mediumtext, amplitude mediumtext, reverberation mediumtext, trillamp mediumtext, trillrate mediumtext, powerspectrum mediumtext, starttime int, overallpeakfreq1 float, overallpeakfreq2 float, timelength float, gapbefore float, gapafter float, timestep float, framelength float, maxf int, windowmethod int, dy float, dynrange float, dyncomp float, echorange int, echocomp float)";
			
			stmt = con.createStatement(); 
			stmt.executeUpdate(queryb+ele);
			
			
			
			StringBuffer indb=new StringBuffer("individual (id INT PRIMARY KEY NOT NULL AUTO_INCREMENT, numsongs int");
			indb.append(", name VARCHAR(50)");
			indb.append(", SpecID VARCHAR(50)");
			indb.append(", PopID VARCHAR(50)");
			indb.append(", locdesc text");
			indb.append(", gridtype varchar(50), gridx varchar(50), gridy varchar(50)");
			indb.append(", sex INT");
			indb.append(", rank VARCHAR(50)");
			indb.append(", age VARCHAR(50)");
			indb.append(")");
			String ind=indb.toString();
			stmt = con.createStatement(); 
			stmt.executeUpdate(queryb+ind);

			String pop="population (id INT PRIMARY KEY NOT NULL AUTO_INCREMENT, name VARCHAR(100))";
			stmt = con.createStatement(); 
			stmt.executeUpdate(queryb+pop);
			
			StringBuffer songdb=new StringBuffer("songdata (id INT PRIMARY KEY NOT NULL AUTO_INCREMENT, IndividualID INT, name VARCHAR(100), ");
			songdb.append("echocomp FLOAT, echorange INT, dyncomp FLOAT, dynrange INT, maxfreq INT, framelength FLOAT, timestep FLOAT, filtercutoff FLOAT, windowmethod INT, dx FLOAT, dy FLOAT, samplerate INT, user VARCHAR(50)");
			songdb.append(", call_location VARCHAR(100)");
			songdb.append(", call_context TEXT");
			songdb.append(", RecordingEquipment VARCHAR(100)");
			songdb.append(", Recorder VARCHAR(100)");
			songdb.append(", noise1 FLOAT, noise2 INT, noise3 INT");
			songdb.append(")");
			String songd=songdb.toString();
			stmt = con.createStatement(); 
			stmt.executeUpdate(queryb+songd);
						
			
			String spec="species (id INT PRIMARY KEY NOT NULL AUTO_INCREMENT, name VARCHAR(100))";
			stmt = con.createStatement(); 
			stmt.executeUpdate(queryb+spec);
			
			String specpop="specpop (id INT PRIMARY KEY NOT NULL AUTO_INCREMENT, spid INT, popid INT)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(queryb+specpop);
			
			String syll="syllable (songid INT, starttime INT, endtime INT)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(queryb+syll);
			
			String wavs="wavs (id INT PRIMARY KEY NOT NULL AUTO_INCREMENT, songid INT, filename VARCHAR(50), wav LONGBLOB, samplerate DOUBLE, framesize INT, stereo INT, bigend INT, signed INT, ssizeinbits INT, time BIGINT)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(queryb+wavs);
			
			eleindex="ALTER TABLE "+s+".element ADD INDEX (SongID)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			String songindex="ALTER TABLE "+s+".songdata ADD INDEX (IndividualID)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(songindex);
			
			String wavindex="ALTER TABLE "+s+".wavs ADD INDEX (songid)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(wavindex);
			
			String dbdetails="dbdetails (version VARCHAR, luscvers VARCHAR)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(queryb+dbdetails);
			
		} 
		catch (Exception e){
			e.printStackTrace();
			rv=false;
		}
		finally { 
			if (rs != null) { 
				try {rs.close();} catch (SQLException sqlEx) {} 
				rs = null; 
			}
			if (stmt != null) { 
				try { stmt.close();} catch (SQLException sqlEx) {} 
				stmt = null; 
			} 
		}
		return rv;
	}

	
	public synchronized boolean createDatabaseHSQLDB(String s){
		
		Statement stmt = null; 
		ResultSet rs = null; 
		
		boolean rv=true;
		
		//File testFile=new File(loc);
		//if (!testFile.exists()){
		//	boolean tryit=testFile.mkdir();
		//}
		
		//File file=new File(loc, s);
		//String thPath=file.getPath();
		//boolean tryit=file.mkdir();		
		String querya="CREATE CACHED TABLE ";
		String queryb="CREATE TABLE ";
		try {
			stmt = con.createStatement(); 
			
			String comp="comparescheme (id INT IDENTITY PRIMARY KEY, name CHAR(30), song1 INT, song2 INT, max_score INT, syll_comp INT, song_comp INT)";

			stmt = con.createStatement(); 
			stmt.executeUpdate(queryb+comp);
			
			String comp2="comparesong (user CHAR(50), song1 INT, song2 INT, score FLOAT, max_score FLOAT, scheme_id INT)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(queryb+comp2);
			
			String comp3="comparesyll (user CHAR(50), song1 INT, song2 INT, syll1 INT, syll2 INT, score FLOAT, max_score FLOAT, scheme_id INT)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(queryb+comp3);
			
			String comp4="compareele (user CHAR(50), song1 INT, song2 INT, syll1 INT, syll2 INT, ele1 INT, ele2 INT, score FLOAT, max_score FLOAT, scheme_id INT)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(queryb+comp4);
			
			String comp5="comparesongcomp (song1 INT, song2 INT, score LONGVARBINARY)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(queryb+comp5);
			
			String comp6="comparetriplet (user CHAR(50), songA INT, songB INT, songX INT, choice INT, trial INT, exptype INT)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(queryb+comp6);
			
			String eleindex="CREATE INDEX index1 ON comparesong (song1)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			eleindex="CREATE INDEX index2 ON comparesongcomp (song1)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			eleindex="CREATE INDEX index3 ON comparesongcomp (song2)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			eleindex="CREATE INDEX index4 ON comparesong (song2)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			eleindex="CREATE INDEX index5 ON comparesong (scheme_id)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			eleindex="CREATE INDEX index6 ON comparesyll (song1)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			eleindex="CREATE INDEX index7 ON comparesyll (song2)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			eleindex="CREATE INDEX index8 ON comparesyll (syll1)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			eleindex="CREATE INDEX index9 ON comparesyll (syll2)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			eleindex="CREATE INDEX index10 ON comparesyll (scheme_id)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			eleindex="CREATE INDEX index11 ON compareele (song1)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			eleindex="CREATE INDEX index12 ON compareele (song2)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			eleindex="CREATE INDEX index13 ON compareele (syll1)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			eleindex="CREATE INDEX index14 ON compareele (syll2)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			eleindex="CREATE INDEX index15 ON compareele (scheme_id)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			String ele="element (id INT IDENTITY PRIMARY KEY , SongID INT, signal LONGVARCHAR, peakfreq LONGVARCHAR, fundfreq LONGVARCHAR, meanfreq LONGVARCHAR, medianfreq LONGVARCHAR, peakfreqchange LONGVARCHAR, fundfreqchange LONGVARCHAR, meanfreqchange LONGVARCHAR, medianfreqchange LONGVARCHAR, harmonicity LONGVARCHAR, wiener LONGVARCHAR, bandwidth LONGVARCHAR, amplitude LONGVARCHAR, reverberation LONGVARCHAR, trillamp LONGVARCHAR, trillrate LONGVARCHAR, powerspectrum LONGVARCHAR, starttime int, overallpeakfreq1 float, overallpeakfreq2 float, timelength float, gapbefore float, gapafter float, timestep float, framelength float, maxf int, windowmethod int, dy float, dynrange float, dyncomp float, echorange int, echocomp float)";
			
			stmt = con.createStatement(); 
			stmt.executeUpdate(querya+ele);
			
			
			
			StringBuffer indb=new StringBuffer("individual (id INT IDENTITY PRIMARY KEY, numsongs int");
			indb.append(", name VARCHAR");
			indb.append(", SpecID VARCHAR");
			indb.append(", PopID VARCHAR");
			indb.append(", locdesc LONGVARCHAR");
			indb.append(", gridtype VARCHAR, gridx VARCHAR, gridy VARCHAR");
			indb.append(", sex INT");
			indb.append(", rank VARCHAR");
			indb.append(", age VARCHAR");
			indb.append(")");
			String ind=indb.toString();
			stmt = con.createStatement(); 
			stmt.executeUpdate(queryb+ind);

			String pop="population (id INT IDENTITY PRIMARY KEY , name VARCHAR)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(queryb+pop);
			
			StringBuffer songdb=new StringBuffer("songdata (id INT IDENTITY PRIMARY KEY, IndividualID INT, name VARCHAR, ");
			songdb.append("echocomp FLOAT, echorange INT, dyncomp FLOAT, dynrange INT, maxfreq INT, framelength FLOAT, timestep FLOAT, filtercutoff FLOAT, windowmethod INT, dx FLOAT, dy FLOAT, samplerate INT, user VARCHAR");
			songdb.append(", call_location VARCHAR");
			songdb.append(", call_context LONGVARCHAR");
			songdb.append(", RecordingEquipment VARCHAR");
			songdb.append(", Recorder VARCHAR");
			songdb.append(", noise1 FLOAT, noise2 INT, noise3 INT");
			songdb.append(")");
			String songd=songdb.toString();
			stmt = con.createStatement(); 
			stmt.executeUpdate(queryb+songd);
						
			
			String spec="species (id INT IDENTITY PRIMARY KEY , name VARCHAR)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(queryb+spec);
			
			String specpop="specpop (id INT IDENTITY PRIMARY KEY , spid INT, popid INT)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(queryb+specpop);
			
			String syll="syllable (songid INT, starttime INT, endtime INT)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(queryb+syll);
			
			String wavs="wavs (id INT IDENTITY PRIMARY KEY, songid INT, filename VARCHAR, wav LONGVARBINARY, samplerate DOUBLE, framesize INT, stereo INT, bigend INT, signed INT, ssizeinbits INT, time BIGINT)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(querya+wavs);
			
			eleindex="CREATE INDEX index16 ON element (SongID)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(eleindex);
			
			String songindex="CREATE INDEX index17 ON songdata (IndividualID)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(songindex);
			
			String wavindex="CREATE INDEX index18 ON wavs (songid)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(wavindex);
			
			String dbdetails="dbdetails (version VARCHAR, luscvers VARCHAR)";
			stmt = con.createStatement(); 
			stmt.executeUpdate(queryb+dbdetails);
			
		} 
		catch (Exception e){
			e.printStackTrace();
			rv=false;
		}
		finally { 
			if (rs != null) { 
				try {rs.close();} catch (SQLException sqlEx) {} 
				rs = null; 
			}
			if (stmt != null) { 
				try { stmt.close();} catch (SQLException sqlEx) {} 
				stmt = null; 
			} 
		}
		return rv;
	}
	
	public boolean grantAnonymousPermissions(){
		Statement stmt = null; 
		ResultSet rs = null; 
		String query="Grant SHOW DATABASES on *.* TO 'birdy' IDENTIFIED BY '9876'";
		boolean rv=true;
		try {
			stmt = con.createStatement(); 
			stmt.executeUpdate(query); 
		} 
		catch (Exception e){
			e.printStackTrace();
			rv=false;
		}
		finally { 
			if (rs != null) { 
				try {rs.close();} catch (SQLException sqlEx) {} 
				rs = null; 
			}
			if (stmt != null) { 
				try { stmt.close();} catch (SQLException sqlEx) {} 
				stmt = null; 
			} 
		}
		return rv;
	}

	
	public boolean grantPermissions(String dbase, String user, String pword, int utype){	
	
		Statement stmt = null; 
		ResultSet rs = null; 
		String[] uoptions={"All ", "ALTER, SELECT, UPDATE, DELETE, INSERT ", "SELECT "};
		String query="GRANT "+uoptions[utype]+"ON "+dbase+".* TO "+user+" IDENTIFIED BY '"+pword+"' WITH GRANT OPTION";
		String query2="GRANT "+uoptions[1]+" ON MySql.* TO "+user+" IDENTIFIED BY '"+pword+"' WITH GRANT OPTION";
		
		String query3="CREATE USER "+user+" PASSWORD "+pword;
		String query4=" ADMIN";
		
		boolean success=true;
		
		try {
			stmt = con.createStatement(); 
			if (DBMODE==1){
				stmt.executeUpdate(query); 
				if (utype==0){
					stmt = con.createStatement(); 
					stmt.executeUpdate(query2);
				}
			}
			else if (DBMODE==0){
				if (utype>0){
					stmt.executeUpdate(query3);
				}
				else{
					stmt.executeUpdate(query3+query4);
				}
			}
		} 
		catch (Exception e){
			success=false;
			e.printStackTrace();
		}
		finally { 
			if (rs != null) { 
				try {rs.close();} catch (SQLException sqlEx) {} 
				rs = null; 
			}
			if (stmt != null) { 
				try { stmt.close();} catch (SQLException sqlEx) {} 
				stmt = null; 
			} 
		}
		return success;
	}
	
	public boolean createUser(String user, String pword, String dbase, int utype, boolean global){	
	
		Statement stmt = null; 
		ResultSet rs = null; 	
		
		String[] uoptions={"SELECT ", "ALTER, SELECT, UPDATE, DELETE, INSERT ", "All "};
		
		if (global){
			dbase="*";
		}
		
		
		String query1="CREATE USER '"+user+"' IDENTIFIED BY '"+pword+"'";
		String query2="GRANT "+uoptions[utype]+"ON "+dbase+".* TO '"+user+"' WITH GRANT OPTION";
		String query3="GRANT CREATE USER ON *.* TO '"+user+"' WITH GRANT OPTION";

		boolean success=true;
		
		try {
			stmt = con.createStatement(); 
			stmt.executeUpdate(query1);
			stmt.executeUpdate(query2);
			if (utype==2){
				stmt.executeUpdate(query3);
			}
		}
		catch (Exception e){
			success=false;
			e.printStackTrace();
		}
		finally { 
			if (rs != null) { 
				try {rs.close();} catch (SQLException sqlEx) {} 
				rs = null; 
			}
			if (stmt != null) { 
				try { stmt.close();} catch (SQLException sqlEx) {} 
				stmt = null; 
			} 
		}
		return success;
	}
	
	public boolean dropUser(String user, String dbase){
		
		boolean success=true;
		String[] subjectRights=getGrants(user);
		String[] adminRights=getGrants(uname);
		
		if ((user.compareTo("birdy")==0)||(user.compareTo("root")==0)||(subjectRights.length>adminRights.length)){
			success=false;
		}
		else{
		
			for (int i=0; i<subjectRights.length; i++){
				boolean found=false;
				for (int j=0; j<adminRights.length; j++){
					if (subjectRights[i].compareTo(adminRights[j])==0){
						found=true;
						j=adminRights.length;
					}
					if (!found){
						success=false;
						i=subjectRights.length;
					}
				}
			}
			if (success){
		
				Statement stmt = null; 
				ResultSet rs = null;
		
				String query1="DROP USER '"+user+"'";		
				try {
					stmt = con.createStatement(); 
					stmt.executeUpdate(query1);
				}
				catch (Exception e){
					success=false;
					e.printStackTrace();
				}
				finally { 
					if (rs != null) { 
						try {rs.close();} catch (SQLException sqlEx) {} 
						rs = null; 
					}
					if (stmt != null) { 
						try { stmt.close();} catch (SQLException sqlEx) {} 
						stmt = null; 
					}
				}
			}
		}
		return success;
	}
	
	public boolean revokeUserPrivileges(String user, String dbase){
		Statement stmt = null; 
		ResultSet rs = null;
				
		String query1="REVOKE ALL PRIVILEGES, GRANT OPTION ON "+dbase+".* TO '"+user+"'";
		String query2="REVOKE CREATE USER, GRANT OPTION ON *.* TO '"+user+"'";
		boolean success=true;
		
		try {
			stmt = con.createStatement(); 
			stmt.executeUpdate(query1);
			stmt.executeUpdate(query2);
		}
		catch (Exception e){
			success=false;
			e.printStackTrace();
		}
		finally { 
			if (rs != null) { 
				try {rs.close();} catch (SQLException sqlEx) {} 
				rs = null; 
			}
			if (stmt != null) { 
				try { stmt.close();} catch (SQLException sqlEx) {} 
				stmt = null; 
			} 
		}
		return success;
	}


	public String[] getGrants(String user){
		String query="SELECT Db from mysql.db WHERE user='"+user+"'";
		PreparedStatement stmt = null; 
		ResultSet rs = null; 
		LinkedList results=new LinkedList();
		try {
			stmt = con.prepareStatement(query);
			rs = stmt.executeQuery( );
			int i=0;
			while( rs.next( ) ){
				System.out.println(user);
				String s=rs.getString("Db");
				results.add(s);
				System.out.println(results);
				i++;
			}
		}
		 
		catch (Exception e){
			e.printStackTrace();
		}

		finally { 
			if (rs != null) { 
				try {rs.close();} catch (SQLException sqlEx) {} 
				rs = null; 
			}
			if (stmt != null) { 
				try { stmt.close();} catch (SQLException sqlEx) {} 
				stmt = null; 
			} 
		}
		String[] out=new String[results.size()];
		for (int i=0; i<out.length; i++){
			out[i]=(String)results.get(i);
		}
		results=null;
		return out;
	}
	

	public void writeToDataBase(String query){
		Statement stmt = null; 
		ResultSet rs = null; 
		try {
			stmt = con.createStatement(); 
			stmt.executeUpdate(query); 
		} 
		catch (Exception e){
			e.printStackTrace();
		}
		finally { 
			if (rs != null) { 
				try {rs.close();} catch (SQLException sqlEx) {} 
				rs = null; 
			}
			if (stmt != null) { 
				try { stmt.close();} catch (SQLException sqlEx) {} 
				stmt = null; 
			} 
		}
	}
	
	public LinkedList readFromDataBase(String query, int[] whattoget){
		LinkedList store=new LinkedList();
		Statement stmt = null; 
		ResultSet rs = null; 
		try {
			stmt = con.createStatement(); 
			rs = stmt.executeQuery(query); 
			if (stmt.execute(query)) { 
				rs = stmt.getResultSet();
				while (rs.next()){
					String [] nam=new String[whattoget.length];
					for (int i=0; i<whattoget.length; i++){
						nam[i]=rs.getString(whattoget[i]);
					}
					store.add(nam);
				}
			} 
		} 
		catch (Exception e){}
		finally { 
			if (rs != null) { 
				try {rs.close();} catch (SQLException sqlEx) {} 
				rs = null; 
			}
			if (stmt != null) { 
				try { stmt.close();} catch (SQLException sqlEx) {} 
				stmt = null; 
			} 
		}
		return store;
	}
}
