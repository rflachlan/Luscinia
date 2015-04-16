package lusc.net.github;
//
//  Luscinia.java
//  Luscinia
//
//  Created by Robert Lachlan on 9/29/05.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import javax.swing.*;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.io.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
//import java.beans.PropertyChangeListener;
//import java.beans.PropertyChangeEvent;


import javax.swing.BorderFactory; 
//import javax.swing.border.Border;
import javax.swing.event.*;

import lusc.net.github.db.*;
import lusc.net.github.ui.AboutBox;
import lusc.net.github.ui.TabType;
import lusc.net.github.ui.db.DatabaseView;
import lusc.net.github.ui.db.SdLogin;

//import com.apple.eawt.event.*;
//import com.apple.eawt.*;

/**
 * This is the main class for the application, and contains the main method entry point.
 * It also contains various listeners for shutdowns, and an important list of database connections
 * @author Rob
 *
 */
//public class Luscinia implements WindowListener, ActionListener, ChangeListener, OpenFilesHandler {
public class Luscinia implements WindowListener, ActionListener, ChangeListener{

	//protected ResourceBundle rsc;
	//SdLogin login;
	Defaults defaults;
	
	DataBaseController dbc=null;
	DbConnection mdb;

	LinkedList<DbConnection> connections=new LinkedList<DbConnection>();
	//LinkedList logins=new LinkedList();
	JTabbedPane tabbedPane=new JTabbedPane();
	JButton addButton=new JButton("+");
	JButton removeButton=new JButton("-");
	
	private static String ADD_TAB = "add tab";
	private static String REMOVE_TAB = "remove tab";
	
	String lversion="2.16.04.15.01";
	String dversion="2.16.04.15.01";
	
	
	
	public static boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));
	JFrame frame;

	/**
	 * This method gets the DataBaseController object used by this Luscinia instance.
	 * @return a DataBaseController object
	 * @see lusc.net.github.db.DataBaseController
	 */
	public DataBaseController getDBController(){
		return dbc;
	}
	
	/**
	 * This method gets information about whether the system that Luscinia is operating on has a Retina screen or something similar
	 * @return an integer value of the scale factor (2 indicates retina screen, for eg).
	 */
	public static int getGraphicsScalingFactor() {
		 GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		 final GraphicsDevice device = env.getDefaultScreenDevice();
		 try {
		   java.lang.reflect.Field field = device.getClass().getDeclaredField("scale");
		   if (field != null) {
		       field.setAccessible(true);
		       Object scale = field.get(device);
		 
		       if (scale instanceof Integer) {
		         return ((Integer)scale).intValue();
		       }
		     }
		   } catch (Exception ignore) {}

		  return 0;
		}
	
	/**
	 * This method gets the version of Luscinia being used
	 * @return a String value of Luscinia version
	 */
	public String getLVersion(){
		return lversion;
	}
	
	/**
	 * This method gets the version of the Database being used
	 * @return a String value of database version
	 */
	public String getDVersion(){
		return dversion;
	}
	
	/**
	 * This method constructs the top level frame of the login window.
	 * {@link lusc.net.github.ui.db.SdLogin} objects fit within tabs in this window
	 */
	
	private void buildUI() {

		
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
		    	quit2();
		    	System.out.println("SAFELY LOGGED OUT");
		    }
		});

		JFrame.setDefaultLookAndFeelDecorated(true);
        frame = new JFrame("Luscinia");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(this);
		//try{
			//Application app=Application.getApplication();
			//app.setOpenFileHandler(this);
		//}
		//catch(Exception e){
			//System.out.println("Non-apple OS");
		//}
		Container container=frame.getContentPane();
		JMenuBar menuBar=new JMenuBar();
		JMenu menu=new JMenu("Luscinia");
		JMenuItem about=new JMenuItem("About Luscinia");
		about.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				new AboutBox(lversion);
			}
		});

		menu.add(about);
		menuBar.add(menu);

		//rsc=ResourceBundle.getBundle ("resources", Locale.getDefault());
        container.setLayout(new BorderLayout());
        
        int sc=getGraphicsScalingFactor();
        
		defaults=new Defaults();
		defaults.setScaleFactor(sc);
		//defaults.lnf=UIManager.getLookAndFeel();
		container.setPreferredSize(new Dimension(800, 600));
		//tabbedPane.setPreferredSize(new Dimension(800, 550));
		container.add(tabbedPane, BorderLayout.CENTER);
		
		tabbedPane.addChangeListener(this);
		
		addNewTab();
		
		JPanel buttonPanel=new JPanel(new BorderLayout());
		buttonPanel.setBorder(BorderFactory.createMatteBorder(10,650,5,50,container.getBackground()));
		
		addButton.addActionListener(this);
		addButton.setActionCommand(ADD_TAB);
		addButton.setPreferredSize(new Dimension(55, 25));
		removeButton.setEnabled(false);
		removeButton.addActionListener(this);
		removeButton.setActionCommand(REMOVE_TAB);
		removeButton.setPreferredSize(new Dimension(55, 25));
		buttonPanel.add(addButton, BorderLayout.WEST);
		buttonPanel.add(removeButton, BorderLayout.EAST);

		container.add(buttonPanel, BorderLayout.SOUTH);
		
		/*
		login = new SdLogin(this);
        tabbedPane.addTab("Log In", login);
        login.setAlignmentX(Component.LEFT_ALIGNMENT);
		*/
		//String osName=System.getProperty("os.name");
		//if (osName.startsWith("Mac")){
			//MacOSAppAdapter moaa=new MacOSAppAdapter(frame, this);
			//moaa.register();
		//}
		
		//registerForMacOSXEvents();
		
		frame.setJMenuBar(menuBar);
        frame.pack();
        frame.setVisible(true);
    }
	
	/**
	 * This method is called when the user wants to add a new database connection
	 * It creates a {@link lusc.net.github.ui.db.SdLogin} object and adds it to the toplevel tabbed pane
	 */
	public void addNewTab(){
		SdLogin login=new SdLogin(this, true, defaults);
		login.setPreferredSize(new Dimension(800, 550));
		tabbedPane.addTab("Log In", login);
		tabbedPane.setSelectedComponent(login);
	}
	
	/**
	 * This method removes a database tabbed pane ({@link lusc.net.github.ui.db.SdLogin} object)
	 */
	public void removeTab(){
		int p=tabbedPane.getSelectedIndex();
		tabbedPane.removeTabAt(p);
	}
	
	/**
	 * This method is called when the user successfully logs in. It is called from 
	 * {@link lusc.net.github.ui.db}.SdLogin
	 * @param sd an  @link lusc.net.github.ui.db.SdLogin object
	 * @param db a @link lusc.net.github.db.DBConnection object
	 */
	public void loggedIn(SdLogin sd, DbConnection db){
		int p=tabbedPane.indexOfComponent(sd);
		tabbedPane.remove(sd);
		DataBaseController dbc=new DataBaseController(db);
		DatabaseView dv=new DatabaseView(dbc, defaults, this);
		tabbedPane.add(dv, p);
		tabbedPane.setTitleAt(p, db.getDBase());
		connections.add(db);
		tabbedPane.setSelectedIndex(p);
	}
	
	/**
	 * I think this method may be unused.
	 * @param db a {@link lusc.net.github.db.DbConnection} object
	 */
	public void loggedInNoTab(DbConnection db){
		mdb=db;
		dbc=new DataBaseController(db);
	}
	
	/**
	 * This method is called when the user logs out of a database, from a
	 * {@link lusc.net.github.ui.db.SdLogin} object
	 * @param dv a {@link lusc.net.github.ui.db.DatabaseView} object
	 */
	public void loggedOut(DatabaseView dv){
		DataBaseController dbc=dv.getDBController();
		DbConnection db=dbc.getConnection();
		connections.remove(db);
		db.clearUp();
		db=null;
		dbc=null;
		int p=tabbedPane.indexOfComponent(dv);
		tabbedPane.remove(dv);
		dv.clearUp();
		dv=null;
		SdLogin login=new SdLogin(this, true, defaults);
		tabbedPane.add(login, p);
		tabbedPane.setTitleAt(p, "Log-In");
		tabbedPane.setSelectedIndex(p);
	}
	
	/**
	 * This method is called at the end of copying a database, from a
	 * {@link lusc.net.github.ui.db.DatabaseView} object
	 */
	public void loggedOut(){
		mdb.clearUp();
		mdb=null;
	}
		
		
	/**
	 * This method may be redundant
	 * @param db
	 */
	public void addNewConnection(DbConnection db){
		DataBaseController dbc=new DataBaseController(db);
		DatabaseView dv=new DatabaseView(dbc, defaults, this);
		tabbedPane.addTab(db.getDBase(), dv);
		connections.add(db);
	}
	
	/**
	 * This method is called from the {@link #closer()} method during shutdown.
	 * @param dv
	 */
	void removeConnection(DatabaseView dv){
		DataBaseController dbc=dv.getDBController();
		DbConnection db=dbc.getConnection();
		connections.remove(db);
		db.clearUp();
		db=null;
		dbc=null;
		tabbedPane.remove(dv);
		dv.clearUp();
		dv=null;
	}
	
	// Generic registration with the Mac OS X application menu
    // Checks the platform, then attempts to register with the Apple EAWT
    // See OSXAdapter.java to see how this is done without directly referencing any Apple APIs
    
	/**
	 * Currently unused, this method was added to implement Mac OS specific behaviour.
	 */
	public void registerForMacOSXEvents() {
        if (MAC_OS_X) {
			//System.out.println("HERE I AM");
            try {
                // Generate and register the OSXAdapter, passing it a hash of all the methods we wish to
                // use as delegates for various com.apple.eawt.ApplicationListener methods
                //OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("quit", (Class[])null));
                //OSXAdapter.setAboutHandler(this, getClass().getDeclaredMethod("about", (Class[])null));
                //OSXAdapter.setPreferencesHandler(this, getClass().getDeclaredMethod("preferences", (Class[])null));
                //OSXAdapter.setFileHandler(this, getClass().getDeclaredMethod("loadImageFile", new Class[] { String.class }));
            	
            } catch (Exception e) {
                System.err.println("Error while loading the OSXAdapter:");
                e.printStackTrace();
            }
        }
    }
	
    /**
     *Holder in place for an about box?
     */
    public void about() {

    }
	
    /**
     * Holder in place for a preferences pane 
     */
    public void preferences() {

    }
	
	/**
	 * This is a method to ask for user feedback if he/she tries to quite the program.
	 * I haven't got it to work yet, so it is not used. But it would be nice...
	 * @return a boolean informing whether user consents to quite the program
	 */
	public boolean quit() {  
        int option = JOptionPane.showConfirmDialog(frame, "Are you sure you want to quit?", "Quit?", JOptionPane.YES_NO_OPTION);
        boolean isquit=(option == JOptionPane.YES_OPTION);
        if (isquit){
        	for (int i=0; i<connections.size(); i++){
        		DbConnection db=(DbConnection)connections.get(i);
        		db.clearUp();
        	}
        	
        	
        }
        return isquit;
    }
	
	/**
	 * 
	 */
	public void quit2() {  
		System.out.println("QUIT SENT");
        for (int i=0; i<connections.size(); i++){
        	DbConnection db=(DbConnection)connections.get(i);
        	db.clearUp();
        }
    }
		
	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	public void windowClosing(WindowEvent e){
		closer();
	}
	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	public void windowClosed(WindowEvent e){
		closer();
	}
	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	public void windowActivated(WindowEvent e){}
	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
	 */
	public void windowDeactivated(WindowEvent e){}
	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	public void windowIconified(WindowEvent e){}
	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
	 */
	public void windowDeiconified(WindowEvent e){}
	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	public void windowOpened(WindowEvent e){}
	
	/* (non-Javadoc)
	 * @see com.apple.eawt.OpenFilesHandler#openFiles(com.apple.eawt.AppEvent.OpenFilesEvent)
	 */
	/*
	public void openFiles(AppEvent.OpenFilesEvent e) {
		List<File> files=e.getFiles();
		File f=files.get(0);
		String s=f.getPath();
		System.out.println(s+" found a file");
		boolean found=false;
		for (int i=0; i<tabbedPane.getTabCount(); i++){
			if (tabbedPane.getComponent(i) instanceof SdLogin){
				found=true;
				SdLogin x=(SdLogin)tabbedPane.getComponent(i);
				x.attemptLogin(s);
				i=tabbedPane.getTabCount();
			}
		}
		if (!found){
			addNewTab();
			for (int i=0; i<tabbedPane.getTabCount(); i++){
				if (tabbedPane.getComponent(i) instanceof SdLogin){
					SdLogin x=(SdLogin)tabbedPane.getComponent(i);
					x.attemptLogin(s);
					i=tabbedPane.getTabCount();
				}
			}
		}
		
	}
	*/
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		String command = evt.getActionCommand();
		if (REMOVE_TAB.equals(command)){
			removeTab();
		}
		if (ADD_TAB.equals(command)){
			addNewTab();
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e) {
		TabType comp=(TabType)tabbedPane.getSelectedComponent();
		if (comp!=null){
			if (comp.isSdLogin){
				removeButton.setEnabled(true);
			}
			else{
				removeButton.setEnabled(false);
			}
		}
		if (tabbedPane.getTabCount()==1){
			removeButton.setEnabled(false);
		}
	}

	
	
	
	/**
	 * Method that coordinates closing down Luscinia, shutting down each connection and 
	 * exiting.
	 */
	public void closer(){		
		for (int i=1; i<tabbedPane.getTabCount(); i++){
			DatabaseView dv=(DatabaseView)tabbedPane.getComponentAt(1);
			removeConnection(dv);
		}
		defaults.writeProperties();
		System.out.println("CLOSING");
		System.exit(0);
	}

    /**
     * main method for the Luscinia app
     * @param args
     */
    public static void main (String args[]) {
		try{
			
			//String osName=System.getProperty("os.name");
			
			
			try { 
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
			
			
			/*
			if (osName.startsWith("Mac")){
				//System.setProperty("Quaqua.tabLayoutPolicy","wrap");
			
				try { 
					//UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			else{
			
				try {
					UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
				} 
				catch (UnsupportedLookAndFeelException e) {}
	       // handle exception
			}
			*/
			
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					Luscinia controller = new Luscinia();
					controller.buildUI();
				}
			});
		}
		catch(Exception e){
		
		}
				
    }
}
