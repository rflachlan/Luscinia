package lusc.net.github.ui;
//
//  MeasurementSave.java
//  Luscinia
//
//  Created by Robert Lachlan on 03/10/2005.
//  Copyright 2005 __MyCompanyName__. All rights reserved.
//

import java.awt.*;

import javax.swing.*;

import lusc.net.github.Defaults;
//import lusc.net.github.analysis.SongGroup;
import lusc.net.github.analysis.AnalysisGroup;
import lusc.net.github.ui.compmethods.ElementOutput;
import lusc.net.github.ui.compmethods.StatisticsOutput;

public class MeasurementSave extends JPanel {

	ElementOutput eo;
	StatisticsOutput sop;
	JTabbedPane tabbedPane;
	
	//public MeasurementSave(SongGroup sg, Defaults defaults){
	public MeasurementSave(AnalysisGroup ag, Defaults defaults){
		//eo=new ElementOutput(sg, defaults);
		//sop=new StatisticsOutput(sg, defaults);
		eo=new ElementOutput(ag, defaults);
		sop=new StatisticsOutput(ag, defaults);
		
		tabbedPane=new JTabbedPane();
		tabbedPane.setTabPlacement(2);
		//tabbedPane.addTab("Element Parameters", po);
		tabbedPane.addTab("Statistics", sop);
		tabbedPane.addTab("Element Measures", eo);
		this.add(tabbedPane);
	}
	
	public void output(){
		if (tabbedPane.getSelectedIndex()==0){
			sop.calculateStatistics();
		}
		else if (tabbedPane.getSelectedIndex()==1){
			eo.calculateElements();
		}
			
		
	}
	
	

}
