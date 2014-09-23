package lusc.net.sourceforge;
//
//  SearchPanel.java
//  Luscinia
//
//  Created by Robert Lachlan on 2/23/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import javax.swing.*;

public class SearchPanel extends JPanel {


	String[] individualFields={"Name", "Species", "Population", "Location", "Grid Location"};
	
	int[] individualFieldTypes={0, 0, 0, 0, 1};
	
	String[] songFields={"Name", "Number of Syllables", "Number of Elements", "Recordist", "Recording equipment", "Time", "Date", "Notes"};
	
	int[] songFieldTypes={0, 1, 1, 0, 0, 1, 1, 0};
	
	String[] numericOperators={"=", "<", ">"};
	String[] textOperators={"contains", "equals"};	
	String[] queryCombinationOperators={"And", "Or"};

	DataBaseController dbc;

	public SearchPanel(DataBaseController dbc){
		this.dbc=dbc;
	}
	
		

}
