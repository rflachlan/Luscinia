package lusc.net.github.ui.compmethods;
//
//  ParameterOutput.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/10/2005.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.
//

import java.awt.*;

import javax.swing.*;

import java.awt.event.*;
import java.util.*;

import lusc.net.github.Defaults;
import lusc.net.github.Element;
import lusc.net.github.Song;
import lusc.net.github.db.DataBaseController;
import lusc.net.github.ui.SaveDocument;

public class ParameterOutput extends JPanel implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3997960730788696028L;

	String [] parameters={"Parameters", "Record time", "Record date", "Recordist", "Record location", "Record equipment", "Notes", "Start time", "Length", "Gap before", "Gap after", 
	"Peak frequency", "Mean frequency", "Median frequency", "Fundamental frequency", "Peak frequency change", "Mean frequency change", "Median frequency change", "Fundamental frequency change", 
	"Harmonicity", "Wiener entropy", "Frequency bandwidth", "Amplitude", "Reverberation", "Vibrato amp", "Vibrato rate", "Abs PFC", "Abs MeanFC", "Abs MedianFC", "Abs FFC", "Overall instantaneous peak frequency", "Overall peak frequency"};	
	
	String [] measures={"Maximum", "Minimum", "Time of maximum", "Time of minimum", "Average (mean)", "Start", "End"};
	String [] details={"Individual name", "Song name", "Syllable Number", "Element Number", "Syllable repeats"};
	String [] details2={"Time Step", "Frame Length", "Maximum frequency", "Windowing Method", "Dynamic Range", "Dynamic Equalization", "Echo Tail", "Echo Reduction", "dy"};
	int numParams=parameters.length;
	JRadioButton[] rbSet=new JRadioButton[numParams];
	JRadioButton[] meSet=new JRadioButton[7];
	JLabel description=new JLabel("Use this option to save general parameters of each element");
	int[] songs;
	DataBaseController dbc;
	LinkedList data;
	boolean[] chooser=new boolean[numParams];
	boolean[] chooser2=new boolean[7];
	boolean mode=true;
	Song song;
	Defaults defaults;
	boolean rawElementOutput=false;
	boolean compressedElementOutput=true;
	boolean rawSyllableOutput=false;
	boolean compressedSyllableOutput=true;
	
	
	public ParameterOutput(DataBaseController dbc, Song song, Defaults defaults){
		this.song=song;
		this.dbc=dbc;
		this.defaults=defaults;
		mode=false;
		for (int i=0; i<numParams; i++){chooser[i]=true;}
		for (int i=0; i<7; i++){chooser2[i]=true;}
	}

	public ParameterOutput (DataBaseController dbc, int[] songs){
		this.songs=songs;
		this.dbc=dbc;
		Font font=new Font("Sans-Serif", Font.PLAIN, 10);
		mode=true;
		for (int i=0; i<numParams; i++){chooser[i]=true;}
		for (int i=0; i<7; i++){chooser2[i]=true;}
		
		JPanel mainpanel=new JPanel(new BorderLayout());
		mainpanel.add(description, BorderLayout.NORTH);
		JPanel paramPanel=new JPanel(new GridLayout(0,1));
		for (int i=0; i<numParams; i++){
			rbSet[i]=new JRadioButton(parameters[i]);
			rbSet[i].setFont(font);
			rbSet[i].setSelected(true);
			paramPanel.add(rbSet[i]);
		}
		mainpanel.add(paramPanel, BorderLayout.EAST);
		JPanel measurePanel=new JPanel(new GridLayout(0,1));
		for (int i=0; i<7; i++){
			meSet[i]=new JRadioButton(measures[i]);
			meSet[i].setFont(font);
			meSet[i].setSelected(true);
			measurePanel.add(meSet[i]);
		}
		mainpanel.add(measurePanel, BorderLayout.WEST);
		JPanel startpanel=new JPanel();
		JButton save=new JButton("save");
		save.addActionListener(this);
		startpanel.add(save);
		
		this.setLayout(new BorderLayout());
		this.add(mainpanel, BorderLayout.CENTER);
		this.add(startpanel, BorderLayout.SOUTH);
	}
	
	public void actionPerformed(ActionEvent evt) {
		for (int i=0; i<numParams; i++){
			if (rbSet[i].isSelected()){
				chooser[i]=true;
			}
			else{
				chooser[i]=false;
			}
		}
		for (int i=0; i<7; i++){
			if (meSet[i].isSelected()){
				chooser2[i]=true;
			}
			else{
				chooser2[i]=false;
			}
		}
		calculateParameters();
	}
	
	public void calculateParameters(){
		SaveDocument sd=new SaveDocument(this, defaults);
		calculate(sd);
	}
	
	public void calculateParameters(String path, String name){
		SaveDocument sd=new SaveDocument(path, name);
		calculate(sd);
	}
	
		
	public void calculate(SaveDocument sd){
		boolean readyToWrite=sd.makeFile();
		if (readyToWrite){
			if(rawElementOutput){
				for (int i=0; i<details.length; i++){
					sd.writeString(details[i]);
				}
				if (chooser[0]){
					for (int i=0; i<details2.length; i++){
						sd.writeString(details2[i]);
					}
				}
				for (int i=1; i<11; i++){
					if (chooser[i]){
						sd.writeString(parameters[i]);
					}
				}
				for (int i=11; i<30; i++){
					if (chooser[i]){
						for (int j=0; j<7; j++){
							if (chooser2[j]){
								sd.writeString(parameters[i]+" "+measures[j]);
							}
						}						
					}
				}
				for (int i=31; i<32; i++){
					if (chooser[i]){
						sd.writeString(parameters[i]);
					}
				}
				sd.writeLine();
				int amt=1;
				if (mode){amt=songs.length;}		
				for (int i=0; i<amt; i++){
					if (mode){song=dbc.loadSongFromDatabase(songs[i], 2);}
					Calendar cal=Calendar.getInstance();
					cal.setTimeInMillis(song.getTDate());
					int hour=cal.get(Calendar.HOUR_OF_DAY);
					int minute=cal.get(Calendar.MINUTE);
					int day=cal.get(Calendar.DAY_OF_MONTH);
					int monthid=cal.get(Calendar.MONTH);
					int year=cal.get(Calendar.YEAR);
					double second=cal.get(Calendar.SECOND)+(cal.get(Calendar.MILLISECOND)*0.001);
			
			
					LinkedList details=dbc.populateContentPane(song.getIndividualID());
					String indname=(String)details.get(0);
					int ne=song.getNumElements();
					int ns=song.getNumSyllables();
					for (int j=0; j<ne; j++){
						Element ele=(Element)song.getElement(j);
						ele.calculateStatisticsAbsolute();
						int[][] signal=ele.getSignal();
						double[][] measurements=ele.getMeasurements();
						int syll=-1;
						int aa=ele.getLength()-1;
						for (int b=0; b<ns; b++){
							int[] dat=(int[])song.getSyllable(b);
							if ((signal[0][0]*ele.getTimeStep()<dat[1])&&(signal[aa][0]*ele.getTimeStep()>dat[0])){
								syll=b+1;
							}
						}
			
						sd.writeString(indname);
						sd.writeString(song.getName());
						sd.writeInt(syll);
						sd.writeInt(j+1);
						if (chooser[0]){
							sd.writeDouble(ele.getTimeStep());
							sd.writeDouble(ele.getFrameLength());
							sd.writeInt(ele.getMaxF());
							sd.writeInt(ele.getWindowMethod());
							sd.writeDouble(ele.getDynRange());
							sd.writeDouble(ele.getDynEqual());
							sd.writeInt(ele.getEchoRange());
							sd.writeDouble(ele.getEchoComp());
							sd.writeDouble(ele.getDy());
						}
						if (chooser[1]){
							sd.writeString(""+hour+":"+minute+":"+second);
						}
						if (chooser[2]){
							sd.writeString(""+day+":"+monthid+":"+year);
						}
						if (chooser[3]){
							sd.writeString(" "+song.getRecordist());
						}
						if (chooser[4]){
							sd.writeString(" "+song.getLocation());
						}
						if (chooser[5]){
							sd.writeString(" "+song.getRecordEquipment());
						}
						if (chooser[6]){
							sd.writeString(" "+song.getNotes());
						}
						if (chooser[7]){
							sd.writeDouble(ele.getBeginTime()*ele.getTimeStep());
						}
						if (chooser[8]){
							sd.writeFloat(ele.getTimelength());
						}
						if (chooser[9]){
							sd.writeFloat(ele.getTimeBefore());
						}
						if (chooser[10]){
							sd.writeFloat(ele.getTimeAfter());
						}
						for (int k=11; k<30; k++){
							if (chooser[k]){
								for (int a=0; a<6; a++){
									if(chooser2[a]){
										sd.writeDouble(measurements[a][k-11]);
									}
								}
								if (chooser2[6]){sd.writeDouble(measurements[measurements.length-1][k-11]);}
							}
						}
						if (chooser[30]){
							sd.writeDouble(ele.getOverallPeak1());
						}
						if (chooser[31]){
							sd.writeDouble(ele.getOverallPeak2());
						}
						sd.writeLine();
					}
					
				}
				sd.finishWriting();
			}
			if(compressedElementOutput){
				for (int i=0; i<details.length; i++){
					sd.writeString(details[i]);
				}
				if (chooser[0]){
					for (int i=0; i<details2.length; i++){
						sd.writeString(details2[i]);
					}
				}
				for (int i=1; i<11; i++){
					if (chooser[i]){
						sd.writeString(parameters[i]);
					}
				}
				for (int i=11; i<30; i++){
					if (chooser[i]){
						for (int j=0; j<7; j++){
							if (chooser2[j]){
								sd.writeString(parameters[i]+" "+measures[j]);
							}
						}						
					}
				}
				for (int i=31; i<32; i++){
					if (chooser[i]){
						sd.writeString(parameters[i]);
					}
				}
				sd.writeLine();
				int amt=1;
				if (mode){amt=songs.length;}		
				for (int i=0; i<amt; i++){
					if (mode){song=dbc.loadSongFromDatabase(songs[i], 2);}
					Calendar cal=Calendar.getInstance();
					cal.setTimeInMillis(song.getTDate());
					int hour=cal.get(Calendar.HOUR_OF_DAY);
					int minute=cal.get(Calendar.MINUTE);
					int day=cal.get(Calendar.DAY_OF_MONTH);
					int monthid=cal.get(Calendar.MONTH);
					int year=cal.get(Calendar.YEAR);
					double second=cal.get(Calendar.SECOND)+(cal.get(Calendar.MILLISECOND)*0.001);
			
			
					LinkedList details=dbc.populateContentPane(song.getIndividualID());
					String indname=(String)details.get(0);
					song.interpretSyllables();
					int np=song.getNumPhrases();
					for (int j=0; j<np; j++){
						int[][] phrase=(int[][])song.getPhrase(j);
						for (int jj=0; jj<phrase[0].length; jj++){
							int countEles=0;
							for (int a=0; a<phrase.length; a++){
								if (phrase[a][jj]!=-1){countEles++;}
							}
							Element [] ele=new Element [countEles];
							countEles=0;
							for (int a=0; a<phrase.length; a++){
								if (phrase[a][jj]!=-1){
									ele[countEles]=(Element)song.getElement(phrase[a][jj]);
									ele[countEles].calculateStatisticsAbsolute();
									countEles++;
								}
							}
									
							sd.writeString(indname);
							sd.writeString(song.getName());
							sd.writeInt(j+1);
							sd.writeInt(jj+1);
							sd.writeInt(phrase.length);
							if (chooser[0]){
								sd.writeDouble(ele[0].getTimeStep());
								sd.writeDouble(ele[0].getFrameLength());
								sd.writeInt(ele[0].getMaxF());
								sd.writeInt(ele[0].getWindowMethod());
								sd.writeDouble(ele[0].getDynRange());
								sd.writeDouble(ele[0].getDynEqual());
								sd.writeInt(ele[0].getEchoRange());
								sd.writeDouble(ele[0].getEchoComp());
								sd.writeDouble(ele[0].getDy());
							}
							if (chooser[1]){
								sd.writeString(""+hour+":"+minute+":"+second);
							}
							if (chooser[2]){
								sd.writeString(""+day+":"+monthid+":"+year);
							}
							if (chooser[3]){
								sd.writeString(" "+song.getRecordist());
							}
							if (chooser[4]){
								sd.writeString(" "+song.getLocation());
							}
							if (chooser[5]){
								sd.writeString(" "+song.getRecordEquipment());
							}
							if (chooser[6]){
								sd.writeString(" "+song.getNotes());
							}
							if (chooser[7]){
								sd.writeDouble(ele[0].getBeginTime()*ele[0].getTimeStep());
							}
							if (chooser[8]){
								double tot=0;
								for (int a=0; a<countEles; a++){tot+=ele[a].getTimelength();}
								sd.writeDouble(tot/(countEles+0.0));
							}


							if (chooser[9]){
								double tot=0;
								double counter=0;
								for (int a=0; a<countEles; a++){
									if (ele[a].getTimeBefore()!=-10000){
										tot+=ele[a].getTimeBefore();
										counter++;
									}
								}
								if (counter>0){
									sd.writeDouble(tot/(counter));
								}
								else{
									sd.writeDouble(-10000);
								}
							}
							if (chooser[10]){
								double tot=0;
								double counter=0;
								for (int a=0; a<countEles; a++){
									if (ele[a].getTimeAfter()!=-10000){
										tot+=ele[a].getTimeAfter();
										counter++;
									}
								}
								if (counter>0){
									sd.writeDouble(tot/(counter));
								}
								else{
									sd.writeDouble(-10000);
								}
							}
							for (int k=11; k<30; k++){
								if (chooser[k]){
									for (int a=0; a<6; a++){
										if(chooser2[a]){
											double tot=0;
											for (int b=0; b<countEles; b++){
												double[][] measurements=ele[b].getMeasurements();
												tot+=measurements[a][k-11];
											}
											sd.writeDouble(tot/(countEles+0.0));
										}
									}
									if (chooser2[6]){
										double tot=0;
										for (int b=0; b<countEles; b++){
											double[][] measurements=ele[b].getMeasurements();
											tot+=measurements[measurements.length-1][k-11];
										}
										sd.writeDouble(tot/(countEles+0.0));
									}
								}
							}
							if (chooser[30]){
								double tot=0;
								for (int b=0; b<countEles; b++){tot+=ele[b].getOverallPeak1();}
								sd.writeDouble(tot/(countEles+0.0));
							}
							if (chooser[31]){
								double tot=0;
								for (int b=0; b<countEles; b++){tot+=ele[b].getOverallPeak2();}
								sd.writeDouble(tot/(countEles+0.0));
							}
							sd.writeLine();
						}
					}
					sd.finishWriting();
				}
			}
		}
		
	}
}
