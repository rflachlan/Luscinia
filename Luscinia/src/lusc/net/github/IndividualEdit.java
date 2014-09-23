package lusc.net.github;
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
	JLabel gridx=new JLabel("Latitude:   ");
	JLabel gridy=new JLabel("Longitude: ");
	
	JTextField nameT,gridXT, gridYT, speciesT, populationT;
	JTextArea locDescT;

	String [] u={"UTM"};
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
	
	
	
	public void updatePopulation(){
		if (individual.name.startsWith("MI")){
			individual.population="Waterloo, MI";
		}
		else if (individual.name.startsWith("MA")){
			individual.population="Massachusetts";
		}
		else if (individual.name.startsWith("NYADI")){
			individual.population="Adirondacks, NY";
		}
		else if (individual.name.startsWith("NYHV")){
			individual.population="Hudson Valley, NY";
		}
		else if (individual.name.startsWith("NYMO")){
			individual.population="Montezuma, NY";
		}
		else if (individual.name.startsWith("PA")){
			individual.population="Conneaut, PA";
		}
		else if (individual.name.startsWith("WI")){
			individual.population="Horicon, WI";
		}
		individual.writeIndividual();
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
				
		nameT=new JTextField(individual.name);
		nameT.setColumns(15);
		locDescT=new JTextArea(individual.location);
		locDescT.setColumns(15);
		locDescT.setRows(5);
		locDescT.setBorder(BorderFactory.createLoweredBevelBorder());
		locDescT.setLineWrap(true);
		locDescT.setWrapStyleWord(true);
		
		gridXT=new JTextField(individual.xco);
		gridXT.setColumns(15);
		gridYT=new JTextField(individual.yco);;
		gridYT.setColumns(15);
		speciesT=new JTextField(individual.species);
		speciesT.setColumns(15);
		populationT=new JTextField(individual.population);
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
		speciesT.setText(individual.species);
		populationT.setText(individual.population);
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
		treePanel.selnode[0].setUserObject(nam);
		treePanel.revalidate();
		treePanel.repaint();
		individual.name=nameT.getText();
		individual.species=speciesT.getText();
		individual.population=populationT.getText();
		individual.location=locDescT.getText();
		individual.gridType=u[gridTypeT.getSelectedIndex()];
		individual.xco=gridXT.getText();
		individual.yco=gridYT.getText();
		individual.ID=treePanel.selnode[0].dex;
		individual.writeIndividual();
		
		defaults.setIndividualDetails(individual);
		
	}
	
}
