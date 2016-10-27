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
	int sex;
	String rank;
	String age;
	
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
		sex=Integer.parseInt((String)list.get(7));
		rank=(String)list.get(8);
		age=(String)list.get(9);
		
		
		/*
		if (!xco.equals("")){
			if (population.equals("Adirondacks, NY")){
				fixLatLong(18, "N");
			}
			else if (population.equals("Hudson Valley, NY")){
				fixLatLong(18, "N");
			}
			else if (population.equals("Montezuma, NY")){
				fixLatLong(18, "N");
			}
			else if (population.equals("Conneaut, PA")){
				fixLatLong(17, "N");
			}
			else if (population.equals("Horicon, WI")){
				fixLatLong(16, "N");
			}
			else if (population.equals("Waterloo, MI")){
				fixLatLong(16, "N");
			}
			
		}
		*/
		list=null;
	}
	
	private class UTM2Deg
	{
	    double latitude;
	    double longitude;
	    private  UTM2Deg(double Easting, double Northing, int Zone)
	    {
	        
	                   
	        double north = Northing;
	        latitude = (north/6366197.724/0.9996+(1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)-0.006739496742*Math.sin(north/6366197.724/0.9996)*Math.cos(north/6366197.724/0.9996)*(Math.atan(Math.cos(Math.atan(( Math.exp((Easting - 500000) / (0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((Easting - 500000) / (0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2)/3))-Math.exp(-(Easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*( 1 -  0.006739496742*Math.pow((Easting - 500000) / (0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2)/3)))/2/Math.cos((north-0.9996*6399593.625*(north/6366197.724/0.9996-0.006739496742*3/4*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.pow(0.006739496742*3/4,2)*5/3*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996 )/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4-Math.pow(0.006739496742*3/4,3)*35/27*(5*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/3))/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((Easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2))+north/6366197.724/0.9996)))*Math.tan((north-0.9996*6399593.625*(north/6366197.724/0.9996 - 0.006739496742*3/4*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.pow(0.006739496742*3/4,2)*5/3*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996 )*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4-Math.pow(0.006739496742*3/4,3)*35/27*(5*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/3))/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((Easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2))+north/6366197.724/0.9996))-north/6366197.724/0.9996)*3/2)*(Math.atan(Math.cos(Math.atan((Math.exp((Easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((Easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2)/3))-Math.exp(-(Easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((Easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2)/3)))/2/Math.cos((north-0.9996*6399593.625*(north/6366197.724/0.9996-0.006739496742*3/4*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.pow(0.006739496742*3/4,2)*5/3*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4-Math.pow(0.006739496742*3/4,3)*35/27*(5*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/3))/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((Easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2))+north/6366197.724/0.9996)))*Math.tan((north-0.9996*6399593.625*(north/6366197.724/0.9996-0.006739496742*3/4*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.pow(0.006739496742*3/4,2)*5/3*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4-Math.pow(0.006739496742*3/4,3)*35/27*(5*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/3))/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((Easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2))+north/6366197.724/0.9996))-north/6366197.724/0.9996))*180/Math.PI;
	        latitude=Math.round(latitude*10000000);
	        latitude=latitude/10000000;
	        longitude =Math.atan((Math.exp((Easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((Easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2)/3))-Math.exp(-(Easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((Easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2)/3)))/2/Math.cos((north-0.9996*6399593.625*( north/6366197.724/0.9996-0.006739496742*3/4*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.pow(0.006739496742*3/4,2)*5/3*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2* north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4-Math.pow(0.006739496742*3/4,3)*35/27*(5*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/3)) / (0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((Easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2))+north/6366197.724/0.9996))*180/Math.PI+Zone*6-183;
	        longitude=Math.round(longitude*10000000);
	        longitude=longitude/10000000;       
	    }   
	}
	
	void fixLatLong(int zone, String dir){
		
		Double xx1=Double.parseDouble(xco.trim());
		//coordinates[i][0]=xx1.intValue()*0.001;
		double lat=xx1.doubleValue();
		if (lat>1000){
		Double yy1=Double.parseDouble(yco.substring(3).trim());
		//coordinates[i][0]=xx1.intValue()*0.001;
		double lon=yy1.doubleValue();
		UTM2Deg ut=new UTM2Deg(lon, lat, zone);
		//System.out.println(lat+" "+lon+" "+zone+" "+ut.latitude+" "+ut.longitude);
		
		xco=new String(ut.latitude+"");
		yco=new String(ut.longitude+"");
		
		writeIndividual();
		}
	}
	
	
	void fixLatLong2(int zone, String dir){
		
		double e=0.081819191;
		double b=6356752.314;
		double a=6378137;
		double eisq=0.006739497;
		double k0=0.9996;
		
		double mucor=a*(1-(e*e*0.25)-(0.046875*e*e*e*e)-(0.01953125*e*e*e*e*e*e));
		double ei=(1-Math.sqrt(1-e*e))/(1+Math.sqrt(1-e*e));
		double c1=1.5*ei-(27/32.0)*ei*ei*ei;
		double c2=(21/16.0)*ei*ei-(55/32.0)*ei*ei*ei*ei;
		double c3=(151/96.0)*ei*ei*ei;
		double c4=(1097/512.0)*ei*ei*ei*ei;
		
		
		
		Double xx1=Double.parseDouble(xco.trim());
		//coordinates[i][0]=xx1.intValue()*0.001;
		double lat=xx1.doubleValue();
		
		Double yy1=Double.parseDouble(yco.trim());
		//coordinates[i][0]=xx1.intValue()*0.001;
		double lon=yy1.doubleValue();
		lon-=zone*1000000;
		
		if ((xx1>100000)&&(yy1>100000)){
		
			double latcor=lat;
			if (!dir.equals("N")){
				latcor=lat-10000000;
			}
			double epr=500000-lon;
			double arclength=lat/k0;
			double mu=arclength/mucor;
			double phi=mu+c1*Math.sin(2*mu)+c2*Math.sin(4*mu)+c3*Math.sin(6*mu)+c4*Math.sin(8*mu);
			double C1=eisq*Math.cos(phi)*Math.cos(phi);
			double T1=Math.tan(phi);
			T1*=T1;
			double N1=a/Math.sqrt(1-Math.pow(e*Math.sin(phi), 2));
			double R1=a*(1-e*e)/Math.pow(1-Math.pow(e*Math.sin(phi), 2),1.5);
			double D=epr/(N1*k0);
			double FACT1=N1*Math.tan(phi)/R1;
			double FACT2=D*D*0.5;
			double FACT3=(5+3*T1+10*C1-4*C1*C1-9*eisq)*D*D*D*D/24.0;
			double FACT4=(61+90*T1+298*C1+45*T1*T1-252*eisq-3*C1*C1)*D*D*D*D*D*D/720.0;
			double LoFACT1=D;
			double LoFACT2=(1+2*T1+C1)*D*D*D/6.0;
			double LoFACT3=(5-2*C1+28*T1-3*C1*C1+8*eisq+24*T1*T1)*D*D*D*D*D/120.0;
			double DeltaLong=(LoFACT1-LoFACT2+LoFACT3)/Math.cos(phi);
			double ZoneCM=6*zone-183;
			double rawlat=180*(phi-FACT1*(FACT2+FACT3+FACT4))/Math.PI;
			if (!dir.equals("N")){
				rawlat=0-rawlat;
			}
			double rawlong=ZoneCM-DeltaLong*180/Math.PI;
		
			//System.out.println("arclength: "+arclength);
			//System.out.println("mu: "+mu);
			//System.out.println("phi: "+phi);
			//System.out.println("C1: "+C1);
			//System.out.println("T1: "+T1);
			//System.out.println("N1: "+N1);
			//System.out.println("R1: "+R1);
			//System.out.println("D: "+D);
			//System.out.println("DeltaLong: "+DeltaLong);
			//System.out.println("ZoneCM: "+ZoneCM);

			xco=new String(rawlat+"");
			yco=new String(rawlong+"");
		}
		
		
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
	 * gets the Individual Age
	 * @return a String value of the Individual Age
	 */
	public String getAge(){
		return age;
	}
	
	/**
	 * sets the Individual Age
	 * @param s a String value of the Individual Age
	 */
	public void setAge(String s){
		age=s;
	}
	
	/**
	 * gets the Individual Sex
	 * @return an int value of the Individual Sex
	 */
	public int getSex(){
		return sex;
	}
	
	/**
	 * sets the Individual Sex
	 * @param s an int value of the Individual Sex
	 */
	public void setSex(int s){
		sex=s;
	}
	
	/**
	 * gets the Individual Rank
	 * @return a String value of the Individual Rank
	 */
	public String getRank(){
		return rank;
	}
	
	/**
	 * sets the Individual Rank
	 * @param s a String value of the Individual Rank
	 */
	public void setRank(String s){
		rank=s;
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
		String []query=new String[11];
		query[0]="UPDATE individual SET name='"+name+"' WHERE id="+ID;
		query[1]="UPDATE individual SET SpecID='"+species+"' WHERE id="+ID;
		query[2]="UPDATE individual SET PopID='"+population+"' WHERE id="+ID;
		query[3]="UPDATE individual SET locdesc='"+location+"' WHERE id="+ID;
		query[4]="UPDATE individual SET gridtype='"+gridType+"' WHERE id="+ID;
		query[5]="UPDATE individual SET gridx='"+xco+"' WHERE id="+ID;
		query[6]="UPDATE individual SET gridy='"+yco+"' WHERE id="+ID;
		query[7]="UPDATE individual SET name='"+name+"' WHERE id="+ID;
		query[8]="UPDATE individual SET sex="+sex+" WHERE id="+ID;
		query[9]="UPDATE individual SET rank='"+rank+"' WHERE id="+ID;
		query[10]="UPDATE individual SET age='"+age+"' WHERE id="+ID;
		for (int i=0; i<11; i++){
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



