package lusc.net.github.ui.statistics;
//
//  DetailsOutput.java
//  Luscinia
//
//  Created by Robert Lachlan on 4/12/06.
//  Copyright 2006 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.
//

import java.awt.*;

import javax.swing.*;

import java.awt.event.*;
import java.util.*;

import lusc.net.github.Defaults;
import lusc.net.github.db.DataBaseController;
import lusc.net.github.ui.SaveDocument;

public class DetailsOutput extends JPanel implements ActionListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String [] parameters={"Individual Name", "Location", "Grid Type", "Latitude", "Longitude", "Species", "Population"};	
	int numParams=parameters.length;
	JRadioButton[] rbSet=new JRadioButton[numParams];
	JLabel description=new JLabel("Use this option to save details of individuals");
	int[] IDs;
	DataBaseController dbc;
	//LinkedList data;
	Defaults defaults;
	boolean verbose=false;
    

	public DetailsOutput (DataBaseController dbc, int[] IDs, Defaults defaults){
		this.IDs=IDs;
		this.dbc=dbc;
		this.defaults=defaults;
		
		JPanel mainpanel=new JPanel(new GridLayout(0,1));
		mainpanel.add(description);
		for (int i=0; i<numParams; i++){
			rbSet[i]=new JRadioButton(parameters[i]);
			rbSet[i].setSelected(true);
			mainpanel.add(rbSet[i]);
		}
		JPanel startpanel=new JPanel();
		JButton save=new JButton("save");
		save.addActionListener(this);
		startpanel.add(save);
		
		this.setLayout(new BorderLayout());
		this.add(mainpanel, BorderLayout.CENTER);
		this.add(startpanel, BorderLayout.SOUTH);
	}
	
	public void actionPerformed(ActionEvent evt) {
		calculateParameters();
	}
	
	public void calculateParameters(){
	
		SaveDocument sd=new SaveDocument(this, defaults);
		boolean readyToWrite=sd.makeFile();
		if (readyToWrite){
			
			for (int i=0; i<numParams; i++){
				if (rbSet[i].isSelected()){
					sd.writeString(parameters[i]);
				}
			}
			sd.writeLine();
			for (int i=0; i<IDs.length; i++){
				LinkedList<String> list=dbc.populateContentPane(IDs[i]);
				for (int j=0; j<numParams; j++){
					if (rbSet[j].isSelected()){
						String s=(String)list.get(j);
						if (s==null){s=" ";}
						sd.writeString(s);
					}
				}
				sd.writeLine();
			}
		}
	}
	
	public void calculateParameters2(String pathName, String fileName){
	
		SaveDocument sd=new SaveDocument(pathName, fileName);
	
		boolean readyToWrite=sd.makeFile();
		if (readyToWrite){
			for (int i=0; i<numParams; i++){
				if (rbSet[i].isSelected()){
					sd.writeString(parameters[i]);
				}
			}
			sd.writeLine();
			for (int i=0; i<IDs.length; i++){
				LinkedList<String> list=dbc.populateContentPane(IDs[i]);
				for (int j=0; j<numParams; j++){
					if (rbSet[j].isSelected()){
						String s=(String)list.get(j);
						if ((s==null)||(s.length()==0)){s=" ";}
						sd.writeString(s);
					}
				}
				sd.writeLine();
			}
			sd.finishWriting();
		}
	}
	
	public void calculateParametersVerbose(String pathName, String fileName){
	
		SaveDocument sd=new SaveDocument(pathName, fileName);
	
		String[] names={"<<Indi>>", "<<Locn>>" , "<<Grid>>", "<<Lati>>", "<<Long>>", "<<Spec>>", "<<Popn>>"};
	
		boolean readyToWrite=sd.makeFile();
		if (readyToWrite){
			for (int i=0; i<IDs.length; i++){
				LinkedList<String> plist= dbc.populateContentPane(IDs[i]);
				
				
				
				for (int j=0; j<numParams; j++){
					String s=(String)plist.get(j);
					if ((s==null)||(s.length()==0)){s=" ";}
					
					//s.replaceAll(cr, tab);
					//s.replaceAll(cr2, tab);
					s=s.replaceAll("\r?\n", " ");
					sd.writeString(names[j]+s);
					sd.writeLine();
				}
				sd.writeLine();
			}
			sd.finishWriting();
		}
	}

}
