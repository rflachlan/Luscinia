package lusc.net.sourceforge;
//
//  Luscinia.java
//  Luscinia
//
//  Created by Robert Lachlan on 9/29/05.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;
import java.util.Locale;
import java.util.List;
import java.util.*;
import java.io.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.BorderFactory; 
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.event.*;
import java.lang.reflect.*;
import com.apple.eawt.event.*;
import com.apple.eawt.*;

public class Luscinia implements WindowListener, ActionListener, ChangeListener, OpenFilesHandler {

	//protected ResourceBundle rsc;
	//SdLogin login;
	Defaults defaults;
	
	DataBaseController dbc=null;
	DbConnection mdb;

	LinkedList connections=new LinkedList();
	LinkedList logins=new LinkedList();
	JTabbedPane tabbedPane=new JTabbedPane();
	JButton addButton=new JButton("+");
	JButton removeButton=new JButton("-");
	
	private static String ADD_TAB = "add tab";
	private static String REMOVE_TAB = "remove tab";
	
	String lversion="1.1.12.12.02";
	String dversion="1.1.12.12.02";
	
	
	
	public static boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));
	JFrame frame;
	
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
		try{
			Application app=Application.getApplication();
			app.setOpenFileHandler(this);
		}
		catch(Exception e){
			System.out.println("Non-apple OS");
		}
		Container container=frame.getContentPane();
		JMenuBar menuBar=new JMenuBar();
		JMenu menu=new JMenu("Luscinia");
		JMenuItem about=new JMenuItem("About Luscinia");
		about.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				AboutBox ab=new AboutBox(lversion);
			}
		});

		menu.add(about);
		menuBar.add(menu);

		//rsc=ResourceBundle.getBundle ("resources", Locale.getDefault());
        container.setLayout(new BorderLayout());
		defaults=new Defaults();
		defaults.lnf=UIManager.getLookAndFeel();
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
		String osName=System.getProperty("os.name");
		//if (osName.startsWith("Mac")){
			//MacOSAppAdapter moaa=new MacOSAppAdapter(frame, this);
			//moaa.register();
		//}
		
		registerForMacOSXEvents();
		
		frame.setJMenuBar(menuBar);
        frame.pack();
        frame.setVisible(true);
    }
	
	public void addNewTab(){
		SdLogin login=new SdLogin(this, true);
		login.setPreferredSize(new Dimension(800, 550));
		tabbedPane.addTab("Log In", login);
		tabbedPane.setSelectedComponent(login);
	}
	
	public void removeTab(){
		int p=tabbedPane.getSelectedIndex();
		tabbedPane.removeTabAt(p);
	}
	
	public void loggedIn(SdLogin sd, DbConnection db){
		int p=tabbedPane.indexOfComponent(sd);
		tabbedPane.remove(sd);
		DataBaseController dbc=new DataBaseController(db);
		DatabaseView dv=new DatabaseView(dbc, defaults, this);
		tabbedPane.add(dv, p);
		tabbedPane.setTitleAt(p, db.dbase);
		connections.add(db);
		tabbedPane.setSelectedIndex(p);
	}
	
	public void loggedInNoTab(DbConnection db){
		mdb=db;
		dbc=new DataBaseController(db);
	}
	
	public void loggedOut(DatabaseView dv){
		DbConnection db=dv.dbc.getConnection();
		connections.remove(db);
		db.clearUp();
		db=null;
		dv.dbc=null;
		int p=tabbedPane.indexOfComponent(dv);
		tabbedPane.remove(dv);
		dv.clearUp();
		dv=null;
		SdLogin login=new SdLogin(this, true);
		tabbedPane.add(login, p);
		tabbedPane.setTitleAt(p, "Log-In");
		tabbedPane.setSelectedIndex(p);
	}
	
	public void loggedOut(){
		mdb.clearUp();
		mdb=null;
	}
		
		
	public void addNewConnection(DbConnection db){
		DataBaseController dbc=new DataBaseController(db);
		DatabaseView dv=new DatabaseView(dbc, defaults, this);
		tabbedPane.addTab(db.dbase, dv);
		connections.add(db);
	}
	
	public void removeConnection(DatabaseView dv){
		DbConnection db=dv.dbc.getConnection();
		connections.remove(db);
		db.clearUp();
		db=null;
		dv.dbc=null;
		tabbedPane.remove(dv);
		dv.clearUp();
		dv=null;
	}
	
	// Generic registration with the Mac OS X application menu
    // Checks the platform, then attempts to register with the Apple EAWT
    // See OSXAdapter.java to see how this is done without directly referencing any Apple APIs
    
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
	
    public void about() {

    }
	
    public void preferences() {

    }
	
	public boolean quit() {  
		System.out.println("QUIT SENT");
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
	
	public void quit2() {  
		System.out.println("QUIT SENT");
        for (int i=0; i<connections.size(); i++){
        	DbConnection db=(DbConnection)connections.get(i);
        	db.clearUp();
        }
    }
		
	public void windowClosing(WindowEvent e){
		closer();
	}
	public void windowClosed(WindowEvent e){
		closer();
	}
	public void windowActivated(WindowEvent e){}
	public void windowDeactivated(WindowEvent e){}
	public void windowIconified(WindowEvent e){}
	public void windowDeiconified(WindowEvent e){}
	public void windowOpened(WindowEvent e){}
	
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
	
	public void actionPerformed(ActionEvent evt) {
		String command = evt.getActionCommand();
		if (REMOVE_TAB.equals(command)){
			removeTab();
		}
		if (ADD_TAB.equals(command)){
			addNewTab();
		}
	}
	
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

	
	
	
	public void closer(){		
		for (int i=1; i<tabbedPane.getTabCount(); i++){
			DatabaseView dv=(DatabaseView)tabbedPane.getComponentAt(1);
			removeConnection(dv);
		}
		defaults.writeProperties();
		System.out.println("CLOSING");
		System.exit(0);
	}

    public static void main (String args[]) {
		try{
			
			String osName=System.getProperty("os.name");
			
			
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
