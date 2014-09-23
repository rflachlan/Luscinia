package lusc.net.github;
//
//  DbConnection3.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/10/2005.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import java.util.*;
import java.io.*;
import java.sql.*;

public class DbConnection3 {
	Connection con=null;
	boolean connected=false;
	String loc="localhost";
	String[] forbiddenNames={"mysql", "MySQL", "test", "information_schema"};
	//String[] forbiddenNames={};
	String uname="birdy";
	String pword="9876";
	int DBMODE=0;
	int connectionError=-1;
	
	public DbConnection3(String loc, int DBMODE){
		this.loc=loc;
		this.DBMODE=DBMODE;
	}
	
	public void connect (){
		String url=" ";
		if (DBMODE==0){
			url="jdbc:mysql://"+loc+":3306/";
		}
		else if (DBMODE==1){
			url="jdbc:hsqldb:file:thedb";
		}
		else if (DBMODE==3){
			url="jdbc:h2:"+loc;
		}
		ResultSet rs;
		try {
			if (DBMODE==0){
				Class.forName("com.mysql.jdbc.Driver");
			}
			else if (DBMODE==1){
				Class.forName("org.hsqldb.jdbcDriver");
			}
			else if (DBMODE==2){
				Class.forName("org.h2.Driver");
			}
			DriverManager.setLoginTimeout(5);
			con = DriverManager.getConnection(url, uname, pword); 
		}
		catch(SQLException ex) {
			connectionError=ex.getErrorCode();
			
            ex.printStackTrace();
			System.out.println("SQLException: " + ex.getMessage()); 
            System.out.println("SQLState: " + ex.getSQLState()); 
            System.out.println("VendorError: " + ex.getErrorCode());
        }

		catch(Exception e){
			//connectionError=-10;
		}
		finally{
			if (con!=null){
				connected=true;
			}
        }
    }

	public void disconnect(){
		if( con != null ) {
			try { con.close(); connected=false;}
			catch( Exception e ) { }
		}
	}
	
		
	public String[] readFromDataBase(){
		String[] results=null;
		if (DBMODE==0){
			results=readFromDataBaseMySQL();
		}
		else if (DBMODE==1){
			results=readFromDataBaseHSQLDB();
		}
		else if (DBMODE==3){
			results=readFromDataBaseH2();
		}
		return results;
	}
	
	public String[] readFromDataBaseH2(){	
		String[] out={" "};
		connect();
		Statement stmt = null; 
		ResultSet rs = null; 
		String query="SHOW DATABASES";
		try {
			stmt = con.createStatement(); 
			rs = stmt.executeQuery(query); 
			if (stmt.execute(query)) { 
				rs = stmt.getResultSet();
				LinkedList t=new LinkedList();
				while (rs.next()){
					String nam=rs.getString(1);
					boolean found=false;
					for (int i=0; i<forbiddenNames.length; i++){
						if (nam.compareToIgnoreCase(forbiddenNames[i])==0){
							i=forbiddenNames.length;
							found=true;
						}
					}
					if (!found){t.add(nam);}
				}
				out=new String[t.size()];
				for (int i=0; i<t.size(); i++){
					out[i]=(String)t.get(i);
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
		disconnect();
		return out;
	}
	
	public String[] readFromDataBaseMySQL(){	
		String[] out={" "};
		connect();
		Statement stmt = null; 
		ResultSet rs = null; 
		String query="SHOW DATABASES";
		try {
			stmt = con.createStatement(); 
			rs = stmt.executeQuery(query); 
			if (stmt.execute(query)) { 
				rs = stmt.getResultSet();
				LinkedList t=new LinkedList();
				while (rs.next()){
					String nam=rs.getString(1);
					boolean found=false;
					for (int i=0; i<forbiddenNames.length; i++){
						if (nam.compareToIgnoreCase(forbiddenNames[i])==0){
							i=forbiddenNames.length;
							found=true;
						}
					}
					if (!found){t.add(nam);}
				}
				out=new String[t.size()];
				for (int i=0; i<t.size(); i++){
					out[i]=(String)t.get(i);
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
		disconnect();
		return out;
	}
	
	public String[] readFromDataBaseHSQLDB(){
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
}