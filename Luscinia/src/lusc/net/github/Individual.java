package lusc.net.github;
//
//  Individual.java
//  Luscinia
//
//  Created by Robert Lachlan on 3/27/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//

import java.util.*;

import lusc.net.github.db.DataBaseController;

/**
 * Individual is the base unit for an individual. Individuals contain an array of metadata 
 * that is relevant for archiving and analysis. At the moment, Individuals do not contain
 * a song repertoire. That is probably a mistake...
 * @author Rob
 *
 */
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
	
	/**
	 * Construct an Individual object using a DataBaseController and an ID code. The function calls
	 * {@link #getIndividual} to load the Individual from the database.
	 * @param dbc
	 * @param ID
	 */
	public Individual( DataBaseController dbc, int ID){
		this.dbc=dbc;
		this.ID=ID;
		getIndividual();
	}

	/**
	 * This method loads up an Individual from the database.
	 */
	void getIndividual(){
		LinkedList<String> list=dbc.populateContentPane(ID);
		name=(String)list.get(0);
		location=(String)list.get(1);
		gridType=(String)list.get(2);
		xco=(String)list.get(3);
		yco=(String)list.get(4);
		species=(String)list.get(5);
		population=(String)list.get(6);
		list=null;
	}
	
	/**
	 * gets the name of an individual
	 * @return a String representing the name of the Individual (user-provided)
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * sets the name of an individual.
	 * @param s a String representing the name of the Individual
	 */
	public void setName(String s){
		name=s;
	}
	
	/**
	 * gets the location field for the Individual (a user-provided description of the location)
	 * @return a String representing location.
	 */
	public String getLocation(){
		return location;
	}
	
	/**
	 * sets the location for the Individual
	 * @param s a String representing location
	 */
	public void setLocation(String s){
		location=s;
	}
	
	/**
	 * gets the type of Grid used for coordinates. At the moment, only UTM is functional...
	 * Latitude and longitude should be included in the future. This requires addition of 
	 * some external api to estimate distance between coordinates.
	 * @return a String representing grid type
	 */
	public String getGridType(){
		return gridType;
	}
	
	/**
	 * sets the type of grid used. 
	 * @param s
	 */
	public void setGridType(String s){
		gridType=s;
	}
	
	/**
	 * gets the x coordinate of the individual (ie longitude)
	 * @return a String value of longitude. This must be parsed into a numeric format by methods that want to use it.
	 */
	public String getXco(){
		return xco;
	}
	
	/**
	 * sets the x coordinate of the individual (ie longitude)
	 * @param s a String value of the longitude
	 */
	public void setXco(String s){
		xco=s;
	}
	
	/**
	 * gets the y coordinate of the individual (ie latitude)
	 * @return a String value of latitude. This must be parsed into a numeric format by methods that want to use it.
	 */
	public String getYco(){
		return yco;
	}
	
	/**
	 * sets the x coordinate of the individual (ie latitude)
	 * @param s a String value of the latitude
	 */
	public void setYco(String s){
		yco=s;
	}
	
	/**
	 * gets the Species name
	 * @return a String value of the species
	 */
	public String getSpecies(){
		return species;
	}
	
	/**
	 * sets the Species name
	 * @param s a String value of the species
	 */
	public void setSpecies(String s){
		species=s;
	}
	
	/**
	 * gets the Population name
	 * @return a String value of the population name
	 */
	public String getPopulation(){
		return population;
	}
	
	/**
	 * sets the Population name
	 * @param s a String value of the population name
	 */
	public void setPopulation(String s){
		population=s;
	}
	
	/**
	 * gets the ID code. This allows coordination with song objects that have an individual ID too
	 * @return an int value for ID
	 */
	public int getID(){
		return ID;
	}
	
	/**
	 * sets the ID code.
	 * @param s an int value for ID
	 */
	public void setID(int s){
		ID=s;
	}
	
	
	/**
	 * gets the DataBaseController used by this Individual.
	 * @return a DataBaseController object
	 * @see lusc.net.github.db.DataBaseController
	 */
	public DataBaseController getDBC(){
		return dbc;
	}
	
	/**
	 * sets the DataBaseController used by this Individual.
	 * @param d a DataBaseController object
	 * @see lusc.net.github.db.DataBaseController
	 */
	public void setDBC(DataBaseController d){
		dbc=d;
	}
	
	/**
	 * this method updates the individual details into the database.
	 */
	public void writeIndividual(){
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
	
	/**
	 * this method inserts a new individual into the database.
	 */
	public void insertIntoDB(){
		dbc.writeToDataBase("INSERT INTO individual (name, SpecID, PopID, locdesc, gridtype, gridx, gridy) VALUES ('"+name+"','"+species+"','"+population+"','"+location+"','"+gridType+"','"+xco+"','"+yco+"')");
	}

}



