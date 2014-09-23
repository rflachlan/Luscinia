package lusc.net.sourceforge;
//
//  DatabaseSearchRow.java
//  Luscinia
//
//  Created by Robert Lachlan on 2/9/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DatabaseSearchRow extends JPanel implements ActionListener {

	JButton addRow=new JButton("+");
	JButton removeRow=new JButton("-");
	
	String[] objectTypes={"Individual", "Recording"};
	String[] individualFields={"Individual Name", "Species", "Population", "Location", "Latitude Range", "Longitude Range"};
	String[] recordingFields={"Recording Name", "Number of elements", "Number of phrases", "Number of syllables", "Time", "Date", "Location", "Equipment", "Recordist", "Notes"};
	
	
	public DatabaseSearchRow(DatabaseSearch ds, boolean isFirstRow){
		if (!isFirstRow){
			//add removeRowButton
		}
		
	}
	
	
	
	
	public void createClause(){
		
		
	}
	
	public void actionPerformed(ActionEvent e) {

	}

}
