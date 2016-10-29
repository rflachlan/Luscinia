package lusc.net.github.ui.db;
//
//  myNode.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/10/2005.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.tree.DefaultMutableTreeNode;

public class myNode extends DefaultMutableTreeNode{
	
	int dex=0;
	boolean individual=false;
	boolean song=false;
	boolean population=false;
	boolean species=false;
	boolean isMeasured=false;
	boolean isDay=false;
	boolean isWeek=false;
	long day;
	String type;
	
	public myNode(Object name){
		this.userObject=name;
	}
	
	public myNode(Object name, String type){
		this.userObject=name;
		this.type=type;
		matchType();
	}
	
	public myNode(myNode node){
		this.userObject=node.userObject;
		this.individual=node.individual;
		this.dex=node.dex;
	}
	
	public myNode(long day, String type) {
		this.day=day;
		this.type=type;
		Calendar cal=Calendar.getInstance();
		cal.setTimeInMillis(day);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		this.userObject=dateFormat.format(cal.getTime());
	}
	
	public void matchType() {
		if (type.compareTo("Population")==0) {population=true;}
		else if (type.compareTo("Species")==0) {species=true;}
		else if (type.compareTo("Individual")==0) {individual=true;}
		else if (type.compareTo("Song")==0) {song=true;}
		else if (type.compareTo("Day")==0) {isDay=true;}
		
		
	}
	
	public String getType() {
		return type;
	}
	
	public int getDex(){
		return dex;
	}
	
	public void setDex(int a){
		dex=a;
	}
	
	public myNode clone(){
		myNode node=new myNode(this);
		return node;
	}
	
}
