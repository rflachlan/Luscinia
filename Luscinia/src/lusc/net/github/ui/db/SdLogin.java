package lusc.net.github.ui.db;
//
//  sdLogin.java
//  Luscinia
//
//  Created by Robert Lachlan on 9/29/05.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.


import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.text.*;

import javax.swing.BorderFactory; 
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import lusc.net.github.Defaults;
import lusc.net.github.Luscinia;
import lusc.net.github.db.DbConnection;
import lusc.net.github.db.DbConnection2;
import lusc.net.github.db.DbConnection3;
import lusc.net.github.ui.TabType;

public class SdLogin extends TabType implements ActionListener, ListSelectionListener{
	
	Defaults defaults;
	JTextField username=new JTextField(25);
	JTextField lusername=new JTextField(25);
	JTextField husername=new JTextField(25);
	JPasswordField password=new JPasswordField(25);
	JPasswordField lpassword=new JPasswordField(25);
	JPasswordField hpassword=new JPasswordField(25);
	JLabel unam=new JLabel("User Name:     ");
	JLabel lunam=new JLabel("User Name:     ");
	JLabel hunam=new JLabel("User Name:     ");
	JLabel pass=new JLabel("Password:       ");
	JLabel lpass=new JLabel("Password:       ");
	JLabel hpass=new JLabel("Password:       ");
	JLabel statusLabel=new JLabel("Not logged in");
	JButton login=new JButton("Log In");
	
	JComboBox dbases;
	DefaultComboBoxModel dbasemodel=new DefaultComboBoxModel();
	
	JRadioButton localDB=new JRadioButton("Local database");
	JRadioButton networkDB=new JRadioButton("Networked database");
	DefaultComboBoxModel recentLocationsModel=new DefaultComboBoxModel();
	DefaultComboBoxModel recentH2LocationsModel=new DefaultComboBoxModel();
	LinkedList locationsList=new LinkedList();
	LinkedList h2LocationsList=new LinkedList();
	JComboBox recentLocalLocations;
	JComboBox recentH2Locations;
	JButton addNewLocation=new JButton("+");
	JButton deleteLocation=new JButton("-");
	JButton openLocation=new JButton("Find");
	JButton addNewH2Location=new JButton("+");
	JButton deleteH2Location=new JButton("-");
	JButton openH2Location=new JButton("Find");
	JButton addNewNetLocation=new JButton("+");
	JButton deleteNetLocation=new JButton("-");
	JButton addNewNetDatabase=new JButton("+");
	JButton deleteNetDatabase=new JButton("-");
	JButton addNewNetUser=new JButton("+");
	JButton deleteNetUser=new JButton("-");
	JList networkLocationList;
	DefaultListModel networkLocationsModel=new DefaultListModel();
	JCheckBox enableRemoteAccess;
	
	JFormattedTextField portfield;
	
	JPanel networkDBPanel, localDBPanel, h2DBPanel, centerPanel;
	
	String uname=" ";
	String luname=" ";
	String huname=" ";
	String pword=" ";
	String lpword=" ";
	String hpword=" ";
	String dbase=" ";
	int DBMODE=2;
	String [] databases;
	
	private Luscinia sd;
	//private DatabaseView dv;
	NumberFormat num;
		
	private static String LOGIN_COMMAND = "login";
	private static String EDIT_COMMAND = "edit";
	private static String LOCATION_COMMAND = "location";
	private static String ENGINE_COMMAND = "engine";
	private static String LOCAL_DB="local db";
	private static String NETWORK_DB="network db";
	private static String ADD_NET_LOC="add net loc";
	private static String REM_NET_LOC="rem net loc";
	private static String ADD_NET_DAT="add net dat";
	private static String REM_NET_DAT="rem net dat";
	private static String REM_NET_USER="rem net user";
	private static String ADD_NET_USER="add net user";
	private static String ADD_LOC_LOC="add loc loc";
	private static String REM_LOC_LOC="rem loc loc";
	private static String OPEN_LOC_LOC="open loc loc";
	private static String ADD_H2_LOC="add h2 loc";
	private static String REM_H2_LOC="rem h2 loc";
	private static String OPEN_H2_LOC="open h2 loc";
	private static String REMOTE_COMMAND="remote";

	Dimension dim=new Dimension(55, 30);
	boolean context=false;
	boolean loggedIn=false;
	DbConnection db;


	public SdLogin(Luscinia sd, boolean context, Defaults defaults){
		this.context=context;
		this.sd=sd;
		
		this.setBorder(BorderFactory.createMatteBorder(5,100,100,100,this.getBackground()));
		isSdLogin=true;
		
		this.defaults=defaults;
		
		DBMODE=defaults.getIntProperty("engine");
		uname=defaults.props.getProperty("user");
		luname=defaults.props.getProperty("luser", "SA");
		huname=defaults.props.getProperty("huser");

		
		this.setLayout(new BorderLayout());
		
		ButtonGroup bg=new ButtonGroup();
		bg.add(localDB);
		bg.add(networkDB);
		if ((DBMODE==0)||(DBMODE==2)){
			DBMODE=2;
			localDB.setSelected(true);
		}
		else{
			networkDB.setSelected(true);
		}
		localDB.addActionListener(this);
		networkDB.addActionListener(this);
		localDB.setActionCommand(LOCAL_DB);
		networkDB.setActionCommand(NETWORK_DB);
		
		
		JPanel northPane=new JPanel(new BorderLayout());
		northPane.setBorder(BorderFactory.createMatteBorder(0,50,10,50,this.getBackground()));
		northPane.add(localDB, BorderLayout.WEST);
		northPane.add(networkDB, BorderLayout.EAST);
		this.add(northPane, BorderLayout.NORTH);
		
		
		
		if (DBMODE==2){
			constructLocalPanel();
			this.add(localDBPanel, BorderLayout.CENTER);
		}
		else{
			constructH2NetworkPanel();
			this.add(h2DBPanel, BorderLayout.CENTER);
			//constructNetworkPanel();
			//this.add(networkDBPanel, BorderLayout.CENTER);
		}
		centerPanel=new JPanel();
		
		
				
		JPanel southPanel=new JPanel(new BorderLayout());
		southPanel.setBorder(BorderFactory.createMatteBorder(10,200,0,200,this.getBackground()));
		
		login.addActionListener(this);		
		login.setActionCommand(LOGIN_COMMAND);
		if (context){
			southPanel.add(login, BorderLayout.CENTER);
		}
		else{
			southPanel.add(login, BorderLayout.LINE_START);
			southPanel.add(statusLabel, BorderLayout.LINE_END);
		}

		//editText.addActionListener(this);
		//editSpec.addActionListener(this);
		
		//southPanel.add(editText, BorderLayout.WEST);
		//southPanel.add(editSpec, BorderLayout.EAST);
		
		//this.add(centerPanel, BorderLayout.CENTER);
		this.add(southPanel, BorderLayout.SOUTH);
		
	}
	
	public void constructLocalPanel(){
		
		num=NumberFormat.getIntegerInstance();
		num.setMaximumIntegerDigits(4);
		num.setGroupingUsed(false);
		
		portfield=new JFormattedTextField(num);
		int po=defaults.getIntProperty("port", 8080);
		portfield.setValue(new Integer(po));
		portfield.setEnabled(false);
		
		
		localDBPanel=new JPanel(new BorderLayout());
		updateLocalLocationModel();
		recentLocalLocations=new JComboBox(recentLocationsModel);
		int p=defaults.getIntProperty("lochsq");
		if ((p>=0)&&(p<recentLocationsModel.getSize())){
			recentLocalLocations.setSelectedIndex(p);
		}
		JPanel toppanel=new JPanel(new BorderLayout());
		JLabel dbloc=new JLabel("Database location: ");
		dbloc.setBorder(BorderFactory.createMatteBorder(5,5,5,5,this.getBackground()));
		toppanel.add(dbloc, BorderLayout.WEST);
		toppanel.add(recentLocalLocations, BorderLayout.CENTER);
		
		
		
		
		JPanel buttonPanel=new JPanel(new BorderLayout());
		buttonPanel.setBorder(BorderFactory.createMatteBorder(10,0,10,0,this.getBackground()));
		addNewLocation.addActionListener(this);
		addNewLocation.setActionCommand(ADD_LOC_LOC);
		addNewLocation.setPreferredSize(dim);
		deleteLocation.addActionListener(this);
		deleteLocation.setActionCommand(REM_LOC_LOC);
		deleteLocation.setPreferredSize(dim);
		openLocation.addActionListener(this);
		openLocation.setActionCommand(OPEN_LOC_LOC);
		//openLocation.setPreferredSize(new Dimension(75, 25));
		enableRemoteAccess=new JCheckBox("Enable remote access");
		enableRemoteAccess.addActionListener(this);
		enableRemoteAccess.setActionCommand(REMOTE_COMMAND);
		
		JPanel buttonPanel2=new JPanel(new BorderLayout());
		buttonPanel2.add(addNewLocation, BorderLayout.LINE_START);
		buttonPanel2.add(openLocation, BorderLayout.CENTER);
		buttonPanel2.add(deleteLocation, BorderLayout.LINE_END);
		JPanel buttonPanel3=new JPanel(new BorderLayout());
		buttonPanel3.add(enableRemoteAccess, BorderLayout.LINE_START);
		JLabel plabel=new JLabel("Port:");
		JPanel portPanel=new JPanel(new BorderLayout());
		portPanel.add(plabel, BorderLayout.CENTER);
		portPanel.add(portfield, BorderLayout.LINE_END);
		buttonPanel3.add(portPanel, BorderLayout.LINE_END);
		//toppanel.add(, BorderLayout.SOUTH);
		buttonPanel.add(buttonPanel3, BorderLayout.LINE_START);
		buttonPanel.add(buttonPanel2, BorderLayout.LINE_END);
		
		
		lusername.setText(luname);
		JPanel userPanel=new JPanel(new BorderLayout());
		userPanel.add(lunam, BorderLayout.LINE_START);
		userPanel.add(lusername, BorderLayout.CENTER);
		
		JPanel passwordPanel=new JPanel(new BorderLayout());
		passwordPanel.add(lpass, BorderLayout.LINE_START);
		passwordPanel.add(lpassword, BorderLayout.CENTER);
	
		JPanel centerPanel=new JPanel(new BorderLayout());
		centerPanel.setBorder(BorderFactory.createMatteBorder(50,50,10,50,this.getBackground()));
		//centerPanel.setBorder(BorderFactory.createMatteBorder(10,10,10,5,this.getBackground()));
		
		JPanel userPasswordPanel=new JPanel(new GridLayout(0,1));		
		userPasswordPanel.add(userPanel);
		userPasswordPanel.add(passwordPanel);
		centerPanel.add(userPasswordPanel, BorderLayout.CENTER);
		
		JPanel tpane=new JPanel(new BorderLayout());
		tpane.setBorder(BorderFactory.createMatteBorder(50,50,10,50,this.getBackground()));
		tpane.add(toppanel, BorderLayout.CENTER);
		tpane.add(buttonPanel, BorderLayout.SOUTH);
		
		localDBPanel.add(tpane, BorderLayout.NORTH);
		//localDBPanel.add(buttonPanel, BorderLayout.SOUTH);
		localDBPanel.add(centerPanel, BorderLayout.SOUTH);
		
		localDBPanel.setBorder(BorderFactory.createRaisedBevelBorder());
	}
	
	public void constructH2NetworkPanel(){
		h2DBPanel=new JPanel(new BorderLayout());
		updateH2LocationModel();
		recentH2Locations=new JComboBox(recentH2LocationsModel);
		int p=defaults.getIntProperty("neth2");
		if ((p>=0)&&(p<recentH2LocationsModel.getSize())){
			recentH2Locations.setSelectedIndex(p);
		}
		JPanel toppanel=new JPanel(new BorderLayout());
		JLabel dbloc=new JLabel("Database location: ");
		dbloc.setBorder(BorderFactory.createMatteBorder(5,5,5,5,this.getBackground()));
		toppanel.add(dbloc, BorderLayout.WEST);
		toppanel.add(recentH2Locations, BorderLayout.CENTER);
		
		JPanel buttonPanel=new JPanel(new BorderLayout());
		buttonPanel.setBorder(BorderFactory.createMatteBorder(10, 300,10,10,this.getBackground()));
		//addNewH2Location.addActionListener(this);
		//addNewH2Location.setActionCommand(ADD_H2_LOC);
		//addNewH2Location.setPreferredSize(dim);
		deleteH2Location.addActionListener(this);
		deleteH2Location.setActionCommand(REM_H2_LOC);
		deleteH2Location.setPreferredSize(dim);
		openH2Location.addActionListener(this);
		openH2Location.setActionCommand(OPEN_H2_LOC);
		openH2Location.setPreferredSize(new Dimension(75, 25));
		//JPanel buttonPanel2=new JPanel(new BorderLayout());
		//buttonPanel2.add(addNewH2Location, BorderLayout.LINE_START);
		//buttonPanel2.add(deleteH2Location, BorderLayout.LINE_END);
		buttonPanel.add(openH2Location, BorderLayout.LINE_START);
		buttonPanel.add(deleteH2Location, BorderLayout.LINE_END);
		//h2DBPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		husername.setText(huname);
		JPanel userPanel=new JPanel(new BorderLayout());
		userPanel.add(hunam, BorderLayout.LINE_START);
		userPanel.add(husername, BorderLayout.CENTER);
		
		JPanel passwordPanel=new JPanel(new BorderLayout());
		passwordPanel.add(hpass, BorderLayout.LINE_START);
		passwordPanel.add(hpassword, BorderLayout.CENTER);
	
		JPanel centerPanel=new JPanel(new BorderLayout());
		centerPanel.setBorder(BorderFactory.createMatteBorder(10,50,10,50,this.getBackground()));
		//centerPanel.setBorder(BorderFactory.createMatteBorder(10,10,10,5,this.getBackground()));
		
		JPanel userPasswordPanel=new JPanel(new GridLayout(0,1));		
		userPasswordPanel.add(userPanel);
		userPasswordPanel.add(passwordPanel);
		centerPanel.add(userPasswordPanel, BorderLayout.CENTER);
		
		JPanel tpane=new JPanel(new BorderLayout());
		tpane.setBorder(BorderFactory.createMatteBorder(50,50,10,50,this.getBackground()));
		tpane.add(toppanel, BorderLayout.CENTER);
		tpane.add(buttonPanel, BorderLayout.SOUTH);
		
		h2DBPanel.add(tpane, BorderLayout.NORTH);
		//h2DBPanel.add(buttonPanel, BorderLayout.SOUTH);
		h2DBPanel.add(centerPanel, BorderLayout.SOUTH);
		
		
		h2DBPanel.setBorder(BorderFactory.createRaisedBevelBorder());
	}
	
	public void updateLocalLocationModel(){
		recentLocationsModel.removeAllElements();
		locationsList.clear();
		LinkedList localLocations=defaults.getStringList("locallocs");
		for (int i=0; i<localLocations.size(); i++){
			String s=(String)localLocations.get(i);
			StringBuffer sb=new StringBuffer(s);
			sb.append(" *");
			
			recentLocationsModel.addElement(sb.toString());
			locationsList.add(s);
		}
		LinkedList h2locations=defaults.getStringList("h2locs");
		for (int i=0; i<h2locations.size(); i++){
			String s=(String)h2locations.get(i);
			//System.out.println(s);
			int p=s.length();
			
			String s2=s.substring(s.lastIndexOf(File.separator)+1, p);
			recentLocationsModel.addElement(s2);
			locationsList.add(s);
		}
	}
	
	public void updateH2LocationModel(){
		recentH2LocationsModel.removeAllElements();
		h2LocationsList.clear();
		
		LinkedList h2locations=defaults.getStringList("h2netlocs");
		for (int i=0; i<h2locations.size(); i++){
			String s=(String)h2locations.get(i);
			//System.out.println(s);
			int p=s.length();
			
			String s2=s.substring(s.lastIndexOf(File.separator)+1, p);
			recentH2LocationsModel.addElement(s2);
			h2LocationsList.add(s);
		}
	}
	
	public void addLocalLocation(String s, boolean isH2){
		System.out.println("added file: "+s);
		int recentMemory=10;
		System.out.println("IS H2"+isH2);
		if (isH2){
			LinkedList localLocations=defaults.getStringList("h2locs");
			if (localLocations.size()==recentMemory){
				localLocations.removeLast();
			}
			localLocations.add(0, s);
			defaults.setStringList("h2locs", localLocations);
		}
		else{
			LinkedList localLocations=defaults.getStringList("locallocs");
			if (localLocations.size()==recentMemory){
				localLocations.removeLast();
			}
			localLocations.add(0, s);
			defaults.setStringList("locallocs", localLocations);
		}
		System.out.println(isH2);
	}
	
	public void addH2Location(String s){
		System.out.println("added file: "+s);
		int recentMemory=10;
		LinkedList localLocations=defaults.getStringList("h2netlocs");
		if (localLocations.size()==recentMemory){
			localLocations.removeLast();
		}
		localLocations.add(0, s);
		defaults.setStringList("h2netlocs", localLocations);

	}
	
	public void constructNetworkPanel(){
		networkDBPanel=new JPanel(new BorderLayout());
		networkDBPanel.setPreferredSize(new Dimension(800, 500));
		JPanel locationPanel=new JPanel(new BorderLayout());
		locationPanel.setBorder(BorderFactory.createMatteBorder(10,10,10,5,this.getBackground()));
		updateNetworkLocationModel();
		
		ArrayListTransferHandler arrayListHandler = new ArrayListTransferHandler();
		
		networkLocationList=new JList(networkLocationsModel);
		networkLocationList.setBorder(BorderFactory.createLoweredBevelBorder());
		networkLocationList.setDragEnabled(true);
		networkLocationList.setLayoutOrientation(JList.VERTICAL);
		networkLocationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		networkLocationList.setTransferHandler(arrayListHandler);
		if (networkLocationsModel.size()>0){
			int q=defaults.getIntProperty("locmys");
			if ((q>=0)&&(q<dbasemodel.getSize())){
				networkLocationList.setSelectedIndex(q);
			}
			else{
				networkLocationList.setSelectedIndex(0);
			}
		}
		networkLocationList.addListSelectionListener(this);
		
		JLabel dbloc=new JLabel("Database location:  ");
		locationPanel.add(dbloc, BorderLayout.WEST);
		locationPanel.add(networkLocationList, BorderLayout.CENTER);
		JPanel buttonPanel=new JPanel(new BorderLayout());
		buttonPanel.setBorder(BorderFactory.createMatteBorder(10,450,10,5,this.getBackground()));
		addNewNetLocation.addActionListener(this);
		addNewNetLocation.setActionCommand(ADD_NET_LOC);
		addNewNetLocation.setPreferredSize(dim);
		deleteNetLocation.addActionListener(this);
		deleteNetLocation.setActionCommand(REM_NET_LOC);
		deleteNetLocation.setPreferredSize(dim);
		buttonPanel.add(addNewNetLocation, BorderLayout.WEST);
		buttonPanel.add(deleteNetLocation, BorderLayout.EAST);
		locationPanel.add(buttonPanel, BorderLayout.SOUTH);
		JPanel databasePanel=new JPanel(new BorderLayout());
		databasePanel.setBorder(BorderFactory.createMatteBorder(10,10,10,5,this.getBackground()));
		dbases=new JComboBox(dbasemodel);
		
		JLabel dbname=new JLabel("Database:       ");
		databasePanel.add(dbname, BorderLayout.WEST);
		databasePanel.add(dbases, BorderLayout.CENTER);
		
		JPanel buttonPanel2=new JPanel(new BorderLayout());
		buttonPanel2.setBorder(BorderFactory.createMatteBorder(10,450,10,5,this.getBackground()));
		addNewNetDatabase.addActionListener(this);
		addNewNetDatabase.setActionCommand(ADD_NET_DAT);
		addNewNetDatabase.setPreferredSize(dim);
		deleteNetDatabase.addActionListener(this);
		deleteNetDatabase.setActionCommand(REM_NET_DAT);
		deleteNetDatabase.setPreferredSize(dim);
		buttonPanel2.add(addNewNetDatabase, BorderLayout.WEST);
		buttonPanel2.add(deleteNetDatabase, BorderLayout.EAST);
		databasePanel.add(buttonPanel2, BorderLayout.SOUTH);
		
		JPanel northPanel=new JPanel(new BorderLayout());
		northPanel.add(locationPanel, BorderLayout.CENTER);
		northPanel.add(databasePanel, BorderLayout.SOUTH);
	
		networkDBPanel.add(northPanel, BorderLayout.NORTH);
	
		if (networkLocationsModel.size()>0){
			String s=(String)networkLocationsModel.getElementAt(0);
			makeConnection(s);
			int q=defaults.getIntProperty("datmys");
			if ((q>=0)&&(q<dbasemodel.getSize())){
				dbases.setSelectedIndex(q);
			}
		}
		
		username.setText(uname);
		JPanel userPanel=new JPanel(new BorderLayout());
		userPanel.add(unam, BorderLayout.LINE_START);
		userPanel.add(username, BorderLayout.CENTER);
		
		JPanel passwordPanel=new JPanel(new BorderLayout());
		passwordPanel.add(pass, BorderLayout.LINE_START);
		passwordPanel.add(password, BorderLayout.CENTER);
	
		JPanel centerPanel=new JPanel(new BorderLayout());
		//centerPanel.setBorder(BorderFactory.createMatteBorder(10,50,10,50,this.getBackground()));
		centerPanel.setBorder(BorderFactory.createMatteBorder(10,10,10,5,this.getBackground()));
		
		JPanel userPasswordPanel=new JPanel(new GridLayout(0,1));		
		userPasswordPanel.add(userPanel);
		userPasswordPanel.add(passwordPanel);
		centerPanel.add(userPasswordPanel, BorderLayout.CENTER);
		
		JPanel buttonPanel3=new JPanel(new BorderLayout());
		buttonPanel3.setBorder(BorderFactory.createMatteBorder(10,450,10,5,this.getBackground()));
		addNewNetUser.addActionListener(this);
		addNewNetUser.setActionCommand(ADD_NET_USER);
		addNewNetUser.setPreferredSize(dim);
		deleteNetUser.addActionListener(this);
		deleteNetUser.setActionCommand(REM_NET_USER);
		deleteNetUser.setPreferredSize(dim);
		buttonPanel3.add(addNewNetUser, BorderLayout.WEST);
		buttonPanel3.add(deleteNetUser, BorderLayout.EAST);
		centerPanel.add(buttonPanel3, BorderLayout.SOUTH);
		
		networkDBPanel.add(centerPanel, BorderLayout.CENTER);

		networkDBPanel.setBorder(BorderFactory.createRaisedBevelBorder());
	}
	
	public void updateNetworkLocationModel(){
		networkLocationsModel.removeAllElements();
		LinkedList netloc=defaults.getStringList("networklocs");
		for (int i=0; i<netloc.size(); i++){
			String s=(String)netloc.get(i);
			networkLocationsModel.addElement(s);
		}
	}
	
	public void addNetworkLocation(String s){
		LinkedList netloc=defaults.getStringList("networklocs");
		netloc.add(0, s);
		defaults.setStringList("networklocs", netloc);
	}
	
	public void removeNetworkLocation(){
		int q=networkLocationList.getSelectedIndex();
		if (q>-1){
			LinkedList netloc=defaults.getStringList("networklocs");
			netloc.remove(q);
			defaults.setStringList("networklocs", netloc);
		}
	}

	public void makeConnection(String sloc){
		DbConnection3 db3=new DbConnection3(sloc, 0);
		db3.connect();
		if (!db3.getConnected()){
			if (db3.getConnectionError()==0){
				String s="Luscinia was unable to connect with the database server (most likely causes: internet not connected; database server switched off)";
				JOptionPane.showMessageDialog(null,s,"Alert!", JOptionPane.ERROR_MESSAGE);
			}
			else if (db3.getConnectionError()==1045){
				String s="The database server des not appear to be configured for Luscinia. Configure it now?";
				int q=JOptionPane.showConfirmDialog(null,s,"Alert!", JOptionPane.ERROR_MESSAGE);
				if (q==0){
					setUpSQL();
				}
			}
			databases=new String[1];
			databases[0]=" ";
		}
		
		else{
			databases=db3.readFromDataBase();
		}
		dbasemodel.removeAllElements();
		for (int i=0; i<databases.length; i++){
			dbasemodel.addElement(databases[i]);
		}
	}
	
	public void setUpSQL(){
		JPanel qpane=new JPanel(new GridLayout(0,1,10,10));
		
		JTextArea locationsField=new JTextArea();
		String contents="If you are using MySQL as an engine, Luscinia needs to create a user-account on the database server in order to be able to read which databases are present on the server when it (Luscinia) is started. Clicking this button will attempt to create such a user.";
		locationsField.setText(contents);
		locationsField.setColumns(50);
		locationsField.setRows(5);
		locationsField.setLineWrap(true);
		locationsField.setWrapStyleWord(true);
		locationsField.setEditable(false);
		
		qpane.add(locationsField);
		JLabel unaml=new JLabel("User Name:     ");
		JLabel passl=new JLabel("Password:      ");
		JTextField usernametf=new JTextField(25);
		JPasswordField passwordpf=new JPasswordField(25);
		JPanel unpanel=new JPanel(new BorderLayout());
		unpanel.add(unaml, BorderLayout.LINE_START);
		unpanel.add(usernametf, BorderLayout.CENTER);
		qpane.add(unpanel);
		JPanel ppanel=new JPanel(new BorderLayout());
		ppanel.add(passl, BorderLayout.LINE_START);
		ppanel.add(passwordpf, BorderLayout.CENTER);
		qpane.add(ppanel);

		
		int n=JOptionPane.showOptionDialog(this, qpane, "Configure MySQL", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
		if (n==0){
			int p=networkLocationList.getSelectedIndex();
			if (p==-1){p=0;}
			String sloc=(String)networkLocationsModel.getElementAt(p);
			uname = usernametf.getText();
			pword=new String(passwordpf.getPassword());						
			DbConnection2 db2=new DbConnection2();
			db2.connect(uname, pword, "mysql", sloc, 1);
			if (!db2.getConnected()){
				String s="Sorry, Luscinia was unable to connect to the database engine with the details you provided";
				JOptionPane.showMessageDialog(null,s,"Log-in failed", JOptionPane.OK_OPTION);
			}
			else{
			
				boolean rv=db2.grantAnonymousPermissions();
				if (!rv){
					String s="Luscinia was unable to configure MySQL. Please check your MySQL installation";
					JOptionPane.showMessageDialog(null,s,"Configuration failed", JOptionPane.OK_OPTION);
				}
				else{
					String s="MySQL should be configured for Luscinia now";
					JOptionPane.showMessageDialog(null,s,"Configuration successful", JOptionPane.OK_OPTION);
				}
				db2.disconnect();
			}
			db2=null;
		}
	}

	
	public void refresh(){
		this.revalidate();
	}
	
	public void startLogin(){
		
		String sloc=" ";
		int port=0;
		boolean startServer=false;
		if (DBMODE==1){
			int p=networkLocationList.getSelectedIndex();
			if (p==-1){p=0;}
			sloc=(String)networkLocationsModel.getElementAt(p);
			
			uname = username.getText();
			pword=new String(password.getPassword());
			int q=dbases.getSelectedIndex();
			if (q==-1){
				q=0;
			}			
			dbase=databases[q];
			defaults.props.setProperty("user", uname);
			defaults.setIntProperty("datmys", q);
			defaults.setIntProperty("locmys", p);
			
		}
		else if (DBMODE==2){
			//sloc=(String)recentLocalLocations.getSelectedItem();
			int p=recentLocalLocations.getSelectedIndex();
			if (p==-1){p=0;}
			sloc=(String)locationsList.get(p);
			//System.out.println(locationsList);
			if (sloc!=null){
				System.out.println("LOGGING IN: "+sloc);
				if (sloc.contains("luschsqldb")){
					//sloc=sloc.substring(0, sloc.length()-2);
					DBMODE=0;
					JOptionPane.showMessageDialog(this, "This is an older HSQLDB database. Consider upgrading to the newer H2 database.");
				}
				
				if (enableRemoteAccess.isSelected()){
					startServer=true;
				}
				//uname="sa";
				//pword="";
				uname = lusername.getText();
				pword=new String(lpassword.getPassword());
				port=(int)((Number)portfield.getValue()).intValue();
				defaults.props.setProperty("luser", uname);
				defaults.setIntProperty("port", port);
				dbase=sloc.substring(sloc.lastIndexOf("/")+1, sloc.length()); 
				defaults.setIntProperty("lochsq", recentLocalLocations.getSelectedIndex());
			}
			else{
			
			}
		}
		else if (DBMODE==3){
			int p=recentH2Locations.getSelectedIndex();
			if (p==-1){p=0;}
			sloc=(String)h2LocationsList.get(p);
			//System.out.println(locationsList);
			if (sloc!=null){

				//uname="sa";
				//pword="";
				uname = husername.getText();
				pword=new String(hpassword.getPassword());
				defaults.props.setProperty("huser", uname);
				dbase=sloc.substring(sloc.lastIndexOf("/")+1, sloc.length()); 
				defaults.setIntProperty("neth2", recentH2Locations.getSelectedIndex());
			}
			else{
			
			}
		
		}
		System.out.println("HERE"+DBMODE+" "+sloc+" "+dbase);
		String[] vers={sd.getLVersion(), sd.getDVersion()};
		db=new DbConnection(DBMODE, sloc, dbase, uname, pword, vers, startServer, port);
		System.out.println("ALSO");
		boolean check=db.doConnect();
		System.out.println("DONE");
		//db.disconnect();
		
		if (check){
			//sd.addNewConnection(db);
			if (context){
				sd.loggedIn(this, db);
			}
			else{
				sd.loggedInNoTab(db);
				statusLabel.setText("Logged in to "+dbase);
				loggedIn=true;
				login.setText("Log out");
			}
		}
		else{
			JOptionPane.showMessageDialog(this, "Connection failed. Check your user name and password.");
			db.clearUp();
			db=null;
		}
		defaults.writeProperties();
		if (DBMODE==0){DBMODE=2;}
	}
	
	public void startLogout(){
		defaults.writeProperties();
		db.clearUp();
		db=null;
		loggedIn=false;
		login.setText("Log in");
		statusLabel.setText("Not logged in");
		
	}
	
	public void addDatabaseQuery(){
		JPanel qpane=new JPanel(new GridLayout(0,1,10,10));
		JLabel unaml=new JLabel("User Name:     ");
		JLabel passl=new JLabel("Password:      ");
		JLabel dbasel=new JLabel("Database:      ");
		JTextField usernametf=new JTextField(25);
		JPasswordField passwordpf=new JPasswordField(25);
		JTextField databasetf=new JTextField(25);
		JLabel instructions=new JLabel("Enter administrator log-in details, and name of new database");
		
		qpane.add(instructions);
		JPanel unpanel=new JPanel(new BorderLayout());
		unpanel.add(unaml, BorderLayout.LINE_START);
		unpanel.add(usernametf, BorderLayout.CENTER);
		qpane.add(unpanel);
		JPanel ppanel=new JPanel(new BorderLayout());
		ppanel.add(passl, BorderLayout.LINE_START);
		ppanel.add(passwordpf, BorderLayout.CENTER);
		qpane.add(ppanel);
		JPanel dpanel=new JPanel(new BorderLayout());
		dpanel.add(dbasel, BorderLayout.LINE_START);
		dpanel.add(databasetf, BorderLayout.CENTER);
		qpane.add(dpanel);
		
		int n=JOptionPane.showOptionDialog(this, qpane, "Add a database", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
		if (n==0){
		
			int p=networkLocationList.getSelectedIndex();
			if (p==-1){p=0;}
			String sloc=(String)networkLocationsModel.getElementAt(p);
			uname = usernametf.getText();
			pword=new String(passwordpf.getPassword());			
			dbase=databasetf.getText();
			
			DbConnection2 db2=new DbConnection2();
			db2.connect(uname, pword, "mysql", sloc, 1);
			
			if (!db2.getConnected()){
				String s="Sorry, Luscinia was unable to connect to the database engine with the details you provided";
				JOptionPane.showMessageDialog(null,s,"Log-in failed", JOptionPane.OK_OPTION);
			}
			else{
				boolean rv=db2.createDatabase(dbase);
				if (!rv){
					String s="Sorry, Luscinia was unable to create a new database. Please check the configuration of the database engine, and your user profile";
					JOptionPane.showMessageDialog(null,s,"Database creation failed", JOptionPane.OK_OPTION);
				}
				else{
					makeConnection(sloc);
					String s="The new database, "+dbase+", was successfully created";
					JOptionPane.showMessageDialog(null,s,"Database creation succeeded", JOptionPane.OK_OPTION);
				}
				db2.disconnect();
			}
			db2=null;
		}
	}
	
	public void deleteDatabaseQuery(){
		int q=dbases.getSelectedIndex();
		if (q==-1){
			String s="No database was selected. Please select a database to delete.";
			JOptionPane.showMessageDialog(null,s,"No database selected", JOptionPane.OK_OPTION);
		}			
		else{
			String dbase=databases[q];
			JPanel qpane=new JPanel(new GridLayout(0,1,10,10));
			JLabel unaml=new JLabel("User Name:     ");
			JLabel passl=new JLabel("Password:      ");
			JTextField usernametf=new JTextField(25);
			JPasswordField passwordpf=new JPasswordField(25);
			JLabel instructions=new JLabel("If you are sure you wish to permanently delete this database, enter administrator log-in details and click ok");
			qpane.add(instructions);
			JPanel unpanel=new JPanel(new BorderLayout());
			unpanel.add(unaml, BorderLayout.LINE_START);
			unpanel.add(usernametf, BorderLayout.CENTER);
			qpane.add(unpanel);
			JPanel ppanel=new JPanel(new BorderLayout());
			ppanel.add(passl, BorderLayout.LINE_START);
			ppanel.add(passwordpf, BorderLayout.CENTER);
			qpane.add(ppanel);

		
			int n=JOptionPane.showOptionDialog(this, qpane, "Delete a database", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
			if (n==0){
				int p=networkLocationList.getSelectedIndex();
				if (p==-1){p=0;}
				String sloc=(String)networkLocationsModel.getElementAt(p);
				uname = usernametf.getText();
				pword=new String(passwordpf.getPassword());						
				DbConnection2 db2=new DbConnection2();
				db2.connect(uname, pword, "mysql", sloc, 1);
				if (!db2.getConnected()){
					String s="Sorry, Luscinia was unable to connect to the database engine with the details you provided";
					JOptionPane.showMessageDialog(null,s,"Log-in failed", JOptionPane.OK_OPTION);
				}
				else{
					boolean rv=db2.deleteDatabase(dbase);
					if (!rv){
						String s="Sorry, Luscinia was unable to delete new database. Please check the configuration of the database engine, and your user profile";
						JOptionPane.showMessageDialog(null,s,"Database deletion failed", JOptionPane.OK_OPTION);
					}
					else{
						makeConnection(sloc);
						String s="The new database, "+dbase+", was successfully deleted";
						JOptionPane.showMessageDialog(null,s,"Database deletion succeeded", JOptionPane.OK_OPTION);
					}
					db2.disconnect();
				}
				db2=null;
			}
		}
	}
	
	public void attemptLogin(String s){
		DBMODE=2;
		boolean found=false;
		for (int i=0; i<locationsList.size(); i++){
			String s2=(String)locationsList.get(i);
			if (s2.startsWith(s)){
				recentLocalLocations.setSelectedIndex(i);
				startLogin();
				i=locationsList.size();
				found=true;
			}
		}
		if (!found){
			File file=new File(s);
			openLocalLocation(file);
			attemptLogin(s);
		}
	}
	
	public void addUserQuery(){
		
		JLabel unaml1=new JLabel("Admin Name:     ");
		JLabel passl1=new JLabel("Admin Password:      ");
		JTextField usernametf1=new JTextField(25);
		JPasswordField passwordpf1=new JPasswordField(25);
		JLabel unaml2=new JLabel("New User Name:             ");
		JLabel passl2=new JLabel("New User Password:         ");
		JLabel passl3=new JLabel("New User Password (repeat):");
		JTextField usernametf2=new JTextField(25);
		JPasswordField passwordpf2=new JPasswordField(25);
		JPasswordField passwordpf3=new JPasswordField(25);
		JTextField databasetf=new JTextField(25);
		
		JCheckBox globalPrivileges=new JCheckBox("Give user global privileges");
		globalPrivileges.setSelected(false);
		
		String[] utypes={"Analysis only", "Normal user", "Administrator"};
		JComboBox userTypes=new JComboBox(utypes);
		userTypes.setSelectedIndex(0);
		
		String contents=new String("Select name and password for new user. Administrative privileges required");
		JTextArea locationsField=new JTextArea();
		locationsField.setText(contents);
		locationsField.setColumns(50);
		locationsField.setRows(5);
		locationsField.setLineWrap(true);
		locationsField.setWrapStyleWord(true);
		locationsField.setEditable(false);
		
		JPanel qpane=new JPanel(new BorderLayout());
		qpane.add(locationsField, BorderLayout.SOUTH);
		
		JPanel adminPanel=new JPanel(new GridLayout(0,1,10,10));
		TitledBorder title = BorderFactory.createTitledBorder("Admin log-on");
		adminPanel.setBorder(title);		
		JPanel unpanel1=new JPanel(new BorderLayout());
		unpanel1.add(unaml1, BorderLayout.LINE_START);
		unpanel1.add(usernametf1, BorderLayout.CENTER);
		adminPanel.add(unpanel1);
		JPanel ppanel1=new JPanel(new BorderLayout());
		ppanel1.add(passl1, BorderLayout.LINE_START);
		ppanel1.add(passwordpf1, BorderLayout.CENTER);
		adminPanel.add(ppanel1);
		qpane.add(adminPanel, BorderLayout.NORTH);
		
		JPanel userPanel=new JPanel(new GridLayout(0,1,10,10));
		TitledBorder title2 = BorderFactory.createTitledBorder("New user details");
		userPanel.setBorder(title2);		
		JPanel unpanel2=new JPanel(new BorderLayout());
		unpanel2.add(unaml2, BorderLayout.LINE_START);
		unpanel2.add(usernametf2, BorderLayout.CENTER);
		userPanel.add(unpanel2);
		JPanel ppanel2=new JPanel(new BorderLayout());
		ppanel2.add(passl2, BorderLayout.LINE_START);
		ppanel2.add(passwordpf2, BorderLayout.CENTER);
		userPanel.add(ppanel2);
		JPanel ppanel3=new JPanel(new BorderLayout());
		ppanel3.add(passl3, BorderLayout.LINE_START);
		ppanel3.add(passwordpf3, BorderLayout.CENTER);
		userPanel.add(ppanel3);
		JPanel detailsPanel=new JPanel(new BorderLayout());
		detailsPanel.add(globalPrivileges, BorderLayout.LINE_START);
		detailsPanel.add(userTypes, BorderLayout.LINE_END);
		userPanel.add(detailsPanel);
		qpane.add(userPanel, BorderLayout.CENTER);
		
		
		int n=JOptionPane.showOptionDialog(this, qpane, "Add a user", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
		if (n==0){
			
			String s1=new String(passwordpf2.getPassword());
			String s2=new String(passwordpf3.getPassword());
			
			if (s1.compareTo(s2)!=0){
				String s3="New user's passwords did not match! Please try again.";
				JOptionPane.showMessageDialog(null,s3,"New password problem", JOptionPane.OK_OPTION);
			}
			else{
				int q=dbases.getSelectedIndex();
				if ((q==-1)&&(!globalPrivileges.isSelected())){
					String s3="No database was selected. Please select a database to delete.";
					JOptionPane.showMessageDialog(null,s3,"No database selected", JOptionPane.OK_OPTION);
				}			
				else{
					String dbase="mysql";
					if (!globalPrivileges.isSelected()){
						dbase=databases[q];
					}
					
					int p=networkLocationList.getSelectedIndex();
					if (p==-1){p=0;}
					String sloc=(String)networkLocationsModel.getElementAt(p);
					uname = usernametf1.getText();
					pword=new String(passwordpf1.getPassword());			
			
					DbConnection2 db2=new DbConnection2();
					db2.connect(uname, pword, dbase, sloc, 1);
			
					if (!db2.getConnected()){
						String s="Sorry, Luscinia was unable to connect to the database engine with the details you provided";
						JOptionPane.showMessageDialog(null,s,"Log-in failed", JOptionPane.OK_OPTION);
					}
					else{
						String nuname=usernametf2.getText();
						String nupass=s1;
						int privLevel=userTypes.getSelectedIndex();					
						boolean globalPriv=globalPrivileges.isSelected();
						boolean rv=db2.createUser(nuname, nupass, dbase, privLevel, globalPriv);
						if (!rv){
							String s="Sorry, Luscinia was unable to register the new user. Please check the configuration of the database engine, and your user profile";
							JOptionPane.showMessageDialog(null,s,"User registration failed", JOptionPane.OK_OPTION);
						}
						else{
							String s="The new username, "+nuname+", was successfully registered";
							JOptionPane.showMessageDialog(null,s,"User registration succeeded", JOptionPane.OK_OPTION);
						}
					}
					db2.disconnect();
					db2=null;
				}
			}
		}
	}
	
	public void deleteUserQuery(){
		
		JLabel unaml1=new JLabel("Admin Name:     ");
		JLabel passl1=new JLabel("Admin Password:      ");
		JTextField usernametf1=new JTextField(25);
		JPasswordField passwordpf1=new JPasswordField(25);
		JLabel unaml2=new JLabel("Deleted User Name:             ");
		JTextField usernametf2=new JTextField(25);
		JTextField databasetf=new JTextField(25);
		
		String contents=new String("Enter username that is to be deleted. Administrative privileges required");
		JTextArea locationsField=new JTextArea();
		locationsField.setText(contents);
		locationsField.setColumns(50);
		locationsField.setRows(5);
		locationsField.setLineWrap(true);
		locationsField.setWrapStyleWord(true);
		locationsField.setEditable(false);
		
		JPanel qpane=new JPanel(new BorderLayout());
		qpane.add(locationsField, BorderLayout.SOUTH);
		
		JPanel adminPanel=new JPanel(new GridLayout(0,1,10,10));
		TitledBorder title = BorderFactory.createTitledBorder("Admin log-on");
		adminPanel.setBorder(title);		
		JPanel unpanel1=new JPanel(new BorderLayout());
		unpanel1.add(unaml1, BorderLayout.LINE_START);
		unpanel1.add(usernametf1, BorderLayout.CENTER);
		adminPanel.add(unpanel1);
		JPanel ppanel1=new JPanel(new BorderLayout());
		ppanel1.add(passl1, BorderLayout.LINE_START);
		ppanel1.add(passwordpf1, BorderLayout.CENTER);
		adminPanel.add(ppanel1);
		qpane.add(adminPanel, BorderLayout.NORTH);
		
		JPanel userPanel=new JPanel(new GridLayout(0,1,10,10));
		TitledBorder title2 = BorderFactory.createTitledBorder("Deleted user details");
		userPanel.setBorder(title2);		
		JPanel unpanel2=new JPanel(new BorderLayout());
		unpanel2.add(unaml2, BorderLayout.LINE_START);
		unpanel2.add(usernametf2, BorderLayout.CENTER);
		userPanel.add(unpanel2);
		qpane.add(userPanel, BorderLayout.CENTER);
		
		
		int n=JOptionPane.showOptionDialog(this, qpane, "Delete a user", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
		if (n==0){
			int q=dbases.getSelectedIndex();
			if (q==-1){
				String s3="No database was selected. Please select a database to delete.";
				JOptionPane.showMessageDialog(null,s3,"No database selected", JOptionPane.OK_OPTION);
			}			
			else{
				dbase=databases[q];				
				int p=networkLocationList.getSelectedIndex();
				if (p==-1){p=0;}
				String sloc=(String)networkLocationsModel.getElementAt(p);
				uname = usernametf1.getText();
				pword=new String(passwordpf1.getPassword());			
		
				DbConnection2 db2=new DbConnection2();
				db2.connect(uname, pword, dbase, sloc, 1);
			
				if (!db2.getConnected()){
					String s="Sorry, Luscinia was unable to connect to the database engine with the details you provided";
					JOptionPane.showMessageDialog(null,s,"Log-in failed", JOptionPane.OK_OPTION);
				}
				else{
					String nuname=usernametf2.getText();
					boolean rv=db2.dropUser(nuname, dbase);
					if (!rv){
					String s="Sorry, Luscinia was unable to delete the user. Please check the configuration of the database engine, and your user profile";
						JOptionPane.showMessageDialog(null,s,"User deletion failed", JOptionPane.OK_OPTION);
					}
					else{
						String s="The username, "+nuname+", was successfully deleted";
						JOptionPane.showMessageDialog(null,s,"User deletion succeeded", JOptionPane.OK_OPTION);
					}
				}
				db2.disconnect();
				db2=null;
			}
		}
	}
	
	public void valueChanged(ListSelectionEvent evt){
		String s=(String)networkLocationList.getSelectedValue();
		
		if (s!=null){makeConnection(s);}
	}
	
	public void actionPerformed(ActionEvent evt) {
		
		String command = evt.getActionCommand();
		
		if (LOCAL_DB.equals(command)){
			DBMODE=2;
			
			//this.remove(networkDBPanel);
			this.remove(h2DBPanel);
			this.validate();
			constructLocalPanel();
			this.add(localDBPanel, BorderLayout.CENTER);
			this.revalidate();
			this.repaint();
			defaults.setIntProperty("engine", DBMODE);
		}
		if (NETWORK_DB.equals(command)){
			DBMODE=3;
			this.remove(localDBPanel);
			this.validate();
			//constructNetworkPanel();
			//this.add(networkDBPanel, BorderLayout.CENTER);
			constructH2NetworkPanel();
			this.add(h2DBPanel, BorderLayout.CENTER);
			this.revalidate();
			this.repaint();
			defaults.setIntProperty("engine", DBMODE);
		}
		if (ADD_NET_LOC.equals(command)){
			JTextField tb=new JTextField(20);
			JOptionPane.showMessageDialog(this,tb,"Enter name of new db location", JOptionPane.OK_OPTION);
			String name=tb.getText();
			if ((name!="")&&(!name.startsWith(" "))){
				addNetworkLocation(name);
				updateNetworkLocationModel();
			}
			else{
				JOptionPane.showMessageDialog(this, "Please enter a different location name");
			}
		}
		if (REM_NET_LOC.equals(command)){
			removeNetworkLocation();
			updateNetworkLocationModel();
		}
		if (ADD_LOC_LOC.equals(command)){
			JFileChooser fc=new JFileChooser();
			
			String defPath=defaults.props.getProperty("path");
			if (defPath!=null){fc=new JFileChooser(defPath);}
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			int returnVal = fc.showDialog(this, "Make New Database File");
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file=fc.getSelectedFile();
				String n1=file.getPath();
				String n2=n1+".luscdb";
				File file2=new File(n2);
				if (!file2.exists()){
					boolean tryit=file2.mkdir();
					if (tryit){
						defPath=file2.getPath();
						String defName=file2.getName();
						defaults.props.setProperty("path", defPath);
						defaults.props.setProperty("filename", defName);
						
						
						String loc=defPath+File.separator+file.getName();
						System.out.println(loc+" "+defPath);
						
						DbConnection2 tdb=new DbConnection2();
						tdb.connect(null, null, null, loc, DBMODE);
						tdb.createDatabase(defName);
					
						addLocalLocation(loc, true);
						updateLocalLocationModel();
						
						tdb.shutdown();
						tdb=null;	
					}
				}
				else {
					JOptionPane.showMessageDialog(null, "Please enter a different location name");
				}
			}
		}
		
		
		if (REM_LOC_LOC.equals(command)){
			int p=recentLocalLocations.getSelectedIndex();
			if (p==-1){
				JOptionPane.showMessageDialog(null, "Please select a location to delete");
			}
			else{
				int n=JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this location?");
				if (n==0){
					LinkedList localLocations=defaults.getStringList("locallocs");
					if (p<localLocations.size()){
						localLocations.remove(p);
						defaults.setStringList("locallocs", localLocations);
					}
					else{
						p-=localLocations.size();
						LinkedList h2List=defaults.getStringList("h2locs");
						h2List.remove(p);
						defaults.setStringList("h2locs", h2List);
					}
					updateLocalLocationModel();
				}
			}
		}
		
		if (REM_H2_LOC.equals(command)){
			int p=recentH2Locations.getSelectedIndex();
			if (p==-1){
				JOptionPane.showMessageDialog(null, "Please select a location to delete");
			}
			else{
				int n=JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this location?");
				if (n==0){
					LinkedList h2List=defaults.getStringList("h2netlocs");
					h2List.remove(p);
					defaults.setStringList("h2netlocs", h2List);
					updateH2LocationModel();
				}
			}
		}
			
		if (OPEN_LOC_LOC.equals(command)){
			
			JFileChooser fc=new JFileChooser();
			String defPath=defaults.props.getProperty("path");
			if (defPath!=null){fc=new JFileChooser(defPath);}
			if (System.getProperty("os.name").startsWith("Mac")){
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			}
			else{
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			}
			fc.addChoosableFileFilter(new HSQLDBFileFilter());
			fc.addChoosableFileFilter(new H2FileFilter());
			System.out.println(fc.getFileFilter().getDescription());
			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file=fc.getSelectedFile();
				openLocalLocation(file);
			}
			
		}
		
		if (OPEN_H2_LOC.equals(command)){
			
			String loc=JOptionPane.showInputDialog("Enter location for networked database:");
			
			addH2Location(loc);
			updateH2LocationModel();

		}
		
		
		if (ADD_NET_DAT.equals(command)){
			addDatabaseQuery();
		}
		if (REM_NET_DAT.equals(command)){
			deleteDatabaseQuery();
		}
		if (ADD_NET_USER.equals(command)){
			addUserQuery();
		}
		if (REM_NET_USER.equals(command)){
			deleteUserQuery();
		}

		if (LOGIN_COMMAND.equals(command)) {
			if (!loggedIn){
				startLogin();
			}
			else{
				startLogout();
			}
		}
		
		if (REMOTE_COMMAND.equals(command)){
			portfield.setEnabled(enableRemoteAccess.isSelected());
			
		}
		
	}
	
	public void openLocalLocation(File file){

		String defPath=file.getPath();
		String defName=file.getName();
		
		boolean isH2=true;
		
		if (file.getName().endsWith(".luschsqldb")){isH2=false;}
		
		
		
		String loc=defPath+File.separator+defName.substring(0, defName.lastIndexOf("."));
		
		defaults.props.setProperty("path", defPath);
		defaults.props.setProperty("filename", defName);
		
		addLocalLocation(loc, isH2);
		updateLocalLocationModel();
	}

	
}
