package lusc.net.sourceforge;
//
//  PasswordPanel.java
//  Luscinia
//
//  Created by Robert Lachlan on 1/3/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.*;
import java.text.*;


public class PasswordPanel extends JPanel{

	JTextField username=new JTextField(25);
	JPasswordField password=new JPasswordField(25);
	JLabel unam=new JLabel("User Name:     ");
	JLabel pass=new JLabel("Password:      ");
	
	JLabel loc=new JLabel("Database location: ");
	JComboBox location;
	
	JLabel dbase=new JLabel("Database:      ");
	JComboBox database;
	String[] dbaseNames=null;
	DefaultComboBoxModel dbaseModel=new DefaultComboBoxModel();
	DbConnection3 dbcon;
	int DBMODE=-1;
	LinkedList locations;
	
	public PasswordPanel(LinkedList locations){
		this.locations=locations;
		createPanel(false);
	}

	public PasswordPanel(LinkedList locations, int DBMODE){
		this.locations=locations;
		this.DBMODE=DBMODE;
		database=new JComboBox(dbaseModel);
		String firstLoc=(String)locations.get(0);
		updateModel(firstLoc);
		createPanel(true);
	}
	
	public void updateModel(String loc){
		
		dbaseModel.removeAllElements();
		dbcon=new DbConnection3(loc, DBMODE);
		dbaseNames=dbcon.readFromDataBase();
		if (dbaseNames!=null){
			database.setEnabled(true);
			for (int i=0; i<dbaseNames.length; i++){
				dbaseModel.addElement(dbaseNames[i]);
			}
		}
		else{
			database.setEnabled(false);
		}
			
	}
	
	public void createPanel(boolean includeDatabase){
		this.setLayout(new GridLayout(0,1,10,10));
		
		String[] slocs=new String[locations.size()];
		slocs=(String[])locations.toArray(slocs);
		location=new JComboBox(slocs);
		if (includeDatabase){
			location.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					JComboBox cb = (JComboBox)e.getSource();
					int ind=cb.getSelectedIndex();
					String locName=(String)locations.get(ind);
					updateModel(locName);
				}
			});
		}
		
		JPanel userP=new JPanel();
		userP.setLayout(new BoxLayout(userP, BoxLayout.LINE_AXIS));
		userP.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
		userP.add(Box.createHorizontalGlue());
		JPanel passP=new JPanel();
		passP.setLayout(new BoxLayout(passP, BoxLayout.LINE_AXIS));
		passP.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
		passP.add(Box.createHorizontalGlue());
		JPanel locationP=new JPanel();
		locationP.setLayout(new BoxLayout(locationP, BoxLayout.LINE_AXIS));
		locationP.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
		locationP.add(Box.createHorizontalGlue());
		
		
		userP.add(unam);
		userP.add(Box.createRigidArea(new Dimension(5,0)));
		userP.add(username);
		this.add(userP);
		passP.add(pass);
		passP.add(Box.createRigidArea(new Dimension(5,0)));
		passP.add(password);
		this.add(passP);
		locationP.add(loc);
		locationP.add(Box.createRigidArea(new Dimension(5,0)));
		locationP.add(location);
		this.add(locationP);
		
		if (includeDatabase){
			JPanel databaseP=new JPanel();
			databaseP.setLayout(new BoxLayout(databaseP, BoxLayout.LINE_AXIS));
			databaseP.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
			databaseP.add(Box.createHorizontalGlue());
			databaseP.add(dbase);
			databaseP.add(Box.createRigidArea(new Dimension(5,0)));
			databaseP.add(database);
			this.add(databaseP);
		}
	}
	
	String getUserName(){
		return(username.getText());
	}
	
	String getPassword(){
		String pword=new String(password.getPassword());
		return pword;
	}
	
	public int getLoc(){
		return location.getSelectedIndex();
	}
	
	String getDBLocation(){
		int p=location.getSelectedIndex();
		String s=(String)locations.get(p);
		return s;
	}
	
	String getDBName(){
		int p=database.getSelectedIndex();
		String s=dbaseNames[p];
		return s;
	}
	
}