package lusc.net.github.ui;
//
//  IndividualEdit.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/10/2005.
//  Copyright 2005 __MyCompanyName__. All rights reserved.
//

//
//  IndividualEdit.java
//  SongDatabase
//
//  Created by Robert Lachlan on Wed Nov 17 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import java.awt.*;

import javax.swing.*;

import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.*;

import lusc.net.github.Defaults;
import lusc.net.github.Individual;
import lusc.net.github.db.DataBaseController;
import lusc.net.github.ui.db.DatabaseTree;

public class IndividualEdit extends JPanel{
	
	Defaults defaults;
	DataBaseController dbc;
	DatabaseTree treePanel;
	Dimension dim=new Dimension(400, 300);
	JButton save=new JButton("save");
	JButton useLast=new JButton("use last saved details");
	
	//String nam, loc, gty, gx, gy, spe, pop;
	
	JLabel name=new JLabel("Individual name: ");
	JLabel species=new JLabel("Species:     ");
	JLabel population=new JLabel("Population: ");
	JLabel locDesc=new JLabel("Description of Location: ");
	JLabel gridType=new JLabel("Grid Type: ");
	JLabel gridx=new JLabel("Latitude (N):   ");
	JLabel gridy=new JLabel("Longitude (E): ");
	
	JTextField nameT,gridXT, gridYT, speciesT, populationT;
	JTextArea locDescT;

	String [] u={"Latitude/Longitude"};
	JComboBox gridTypeT=new JComboBox(u);
	  
	JPanel contentPane=new JPanel();
	int ID;
	
	Individual individual;

	public IndividualEdit(DatabaseTree treePanel, DataBaseController dbc, int ID, Defaults defaults){
		this.treePanel=treePanel;
		this.dbc=dbc;
		this.ID=ID;
		this.defaults=defaults;
		individual=new Individual(dbc, ID);
		populateContentPane();
		
		//updatePopulation();
		
	}
	
	public void populateContentPane(){	
		/*
		LinkedList list=dbc.populateContentPane(ID);
		if (list.size()==7){
			nam=(String)list.get(0);
			loc=(String)list.get(1);
			gty=(String)list.get(2);
			gx=(String)list.get(3);
			gy=(String)list.get(4);
			spe=(String)list.get(5);
			pop=(String)list.get(6);
		}
		*/
				
		nameT=new JTextField(individual.getName());
		nameT.setColumns(15);
		locDescT=new JTextArea(individual.getLocation());
		locDescT.setColumns(15);
		locDescT.setRows(5);
		locDescT.setBorder(BorderFactory.createLoweredBevelBorder());
		locDescT.setLineWrap(true);
		locDescT.setWrapStyleWord(true);
		
		gridXT=new JTextField(individual.getXco());
		gridXT.setColumns(15);
		gridYT=new JTextField(individual.getYco());;
		gridYT.setColumns(15);
		speciesT=new JTextField(individual.getSpecies());
		speciesT.setColumns(15);
		populationT=new JTextField(individual.getPopulation());
		populationT.setColumns(15);
		
		save.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){wrapUp();}});
	
		useLast.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){addInLast();}});
		//contentPane.setLayout(new GridLayout(0,2));
		
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
		
		contentPane.setBorder(BorderFactory.createMatteBorder(5,5,5,5,this.getBackground()));
		
		JPanel namePane=new JPanel(new BorderLayout());
		namePane.add(name, BorderLayout.LINE_START);
		namePane.add(nameT, BorderLayout.CENTER);
		JPanel speciesPane=new JPanel(new BorderLayout());
		speciesPane.add(species, BorderLayout.LINE_START);
		speciesPane.add(speciesT, BorderLayout.CENTER);
		JPanel populationPane=new JPanel(new BorderLayout());
		populationPane.add(population, BorderLayout.LINE_START);
		populationPane.add(populationT, BorderLayout.CENTER);
		JPanel locPane=new JPanel(new BorderLayout());
		locPane.add(locDesc, BorderLayout.LINE_START);
		locPane.add(locDescT, BorderLayout.CENTER);
		JPanel gridTypePane=new JPanel(new BorderLayout());
		gridTypePane.add(gridType, BorderLayout.LINE_START);
		gridTypePane.add(gridTypeT, BorderLayout.CENTER);
		JPanel gridXPane=new JPanel(new BorderLayout());
		gridXPane.add(gridx, BorderLayout.LINE_START);
		gridXPane.add(gridXT, BorderLayout.CENTER);
		JPanel gridYPane=new JPanel(new BorderLayout());
		gridYPane.add(gridy, BorderLayout.LINE_START);
		gridYPane.add(gridYT, BorderLayout.CENTER);
		
		contentPane.add(namePane);
		contentPane.add(speciesPane);
		contentPane.add(populationPane);
		contentPane.add(locPane);
		contentPane.add(gridTypePane);
		contentPane.add(gridXPane);
		contentPane.add(gridYPane);
		contentPane.add(useLast);
		contentPane.add(save);
		this.add(contentPane);
	}
	
	public void addInLast(){
		defaults.getIndividualDetails(individual);
		speciesT.setText(individual.getSpecies());
		populationT.setText(individual.getPopulation());
	}
	
	
	public void wrapUp(){
		/*
		nam=nameT.getText();
		spe=speciesT.getText();
		pop=populationT.getText();
		String []query=new String[8];
		treePanel.selnode.setUserObject(nam);

		query[0]="UPDATE individual SET name='"+nam+"' WHERE id="+ID;
		query[1]="UPDATE individual SET SpecID='"+spe+"' WHERE id="+ID;
		query[2]="UPDATE individual SET PopID='"+pop+"' WHERE id="+ID;
		query[3]="UPDATE individual SET locdesc='"+locDescT.getText()+"' WHERE id="+ID;
		query[4]="UPDATE individual SET gridtype='"+u[gridTypeT.getSelectedIndex()]+"' WHERE id="+ID;
		query[5]="UPDATE individual SET gridx='"+gridXT.getText()+"' WHERE id="+ID;
		query[6]="UPDATE individual SET gridy='"+gridYT.getText()+"' WHERE id="+ID;
		query[7]="UPDATE individual SET name='"+treePanel.selnode.toString()+"' WHERE id="+treePanel.selnode.dex;
		for (int i=0; i<8; i++){
			dbc.writeToDataBase(query[i]);
		}
		*/
		String nam=nameT.getText();
		treePanel.getSelNode()[0].setUserObject(nam);
		treePanel.revalidate();
		treePanel.repaint();
		individual.setName(nameT.getText());
		individual.setSpecies(speciesT.getText());
		individual.setPopulation(populationT.getText());
		individual.setLocation(locDescT.getText());
		individual.setGridType(u[gridTypeT.getSelectedIndex()]);
		individual.setXco(gridXT.getText());
		individual.setYco(gridYT.getText());
		individual.setID(treePanel.getSelNode()[0].getDex());
		individual.writeIndividual();
		
		defaults.setIndividualDetails(individual);
		
	}
	
}
