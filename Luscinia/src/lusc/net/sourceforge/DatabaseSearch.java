package lusc.net.sourceforge;
//
//  DatabaseSearch.java
//  Luscinia
//
//  Created by Robert Lachlan on 2/9/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//
import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DatabaseSearch extends JPanel implements ActionListener {
	
	JPanel mainPanel=new JPanel();
	LinkedList searchRowList=new LinkedList();
	DatabaseView dv;
	
	public DatabaseSearch(DatabaseView dv){
		this.dv=dv;
		
	}
	
	
	public void constructUI(){
		this.setLayout(new BorderLayout());
		JButton search=new JButton("Search");
		search.addActionListener(this);
		this.add(search, BorderLayout.SOUTH);
		this.add(mainPanel, BorderLayout.CENTER);
	}
	
	
	public void addSearchRow(){
		DatabaseSearchRow dsr=new DatabaseSearchRow(this, true);
		mainPanel.add(dsr);
		searchRowList.add(dsr);
		
	}
	
	public void removeSearchRow(DatabaseSearchRow dsr){
		mainPanel.remove(dsr);
		searchRowList.remove(dsr);
		dsr=null;
		
	}
	
	
    public void actionPerformed(ActionEvent e) {
		performSearch();
	}
	
	public void performSearch(){
		
		
		
	}
	
	
	
	
	
}
