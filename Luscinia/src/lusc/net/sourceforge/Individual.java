package lusc.net.sourceforge;
//
//  Individual.java
//  Luscinia
//
//  Created by Robert Lachlan on 3/27/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//

import java.util.*;

public class Individual {
	
	DataBaseController dbc;
	String name;
	String location;
	String gridType;
	String xco;
	String yco;
	String species;
	String population;
	int ID;
	
	public Individual( DataBaseController dbc, int ID){
		this.dbc=dbc;
		this.ID=ID;
		getIndividual();
	}

	void getIndividual(){
		LinkedList list=dbc.populateContentPane(ID);
		name=(String)list.get(0);
		location=(String)list.get(1);
		gridType=(String)list.get(2);
		xco=(String)list.get(3);
		yco=(String)list.get(4);
		species=(String)list.get(5);
		population=(String)list.get(6);
		list=null;
	}
	
	void writeIndividual(){
		String []query=new String[8];
		query[0]="UPDATE individual SET name='"+name+"' WHERE id="+ID;
		query[1]="UPDATE individual SET SpecID='"+species+"' WHERE id="+ID;
		query[2]="UPDATE individual SET PopID='"+population+"' WHERE id="+ID;
		query[3]="UPDATE individual SET locdesc='"+location+"' WHERE id="+ID;
		query[4]="UPDATE individual SET gridtype='"+gridType+"' WHERE id="+ID;
		query[5]="UPDATE individual SET gridx='"+xco+"' WHERE id="+ID;
		query[6]="UPDATE individual SET gridy='"+yco+"' WHERE id="+ID;
		query[7]="UPDATE individual SET name='"+name+"' WHERE id="+ID;
		for (int i=0; i<8; i++){
			dbc.writeToDataBase(query[i]);
		}
	}
	
	void insertIntoDB(){
		dbc.writeToDataBase("INSERT INTO individual (name, SpecID, PopID, locdesc, gridtype, gridx, gridy) VALUES ('"+name+"','"+species+"','"+population+"','"+location+"','"+gridType+"','"+xco+"','"+yco+"')");
	}

}



