package lusc.net.sourceforge;
//
//  SongInformation.java
//  Luscinia
//
//  Created by Robert Lachlan on 2/19/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//


import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;

public class SongInformation extends JPanel implements ActionListener{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6109142569651548020L;
	private static String SAVE_COMMAND = "save";
	private static String SAVE_SOUND_COMMAND = "save sound";
	private static String USE_LAST_COMMAND = "use last";
	
	DataBaseController dbc;
	DatabaseTree treePanel;
	Dimension dim=new Dimension(400, 300);
	JButton save=new JButton("Save");
	JButton saveSound=new JButton("Export sound");
	JButton useLast=new JButton("Use last saved details");
	
	JLabel nameL=new JLabel("Song name: ");
	JLabel eleNumberL, phrNumberL;
	JLabel sylNumberL=new JLabel("Number of syllables: ");
	JLabel locationL=new JLabel("Location: ");
	JLabel recordEquipmentL=new JLabel("Recording equipment: ");
	JLabel recordistL=new JLabel("Recordist: ");
	JLabel notesL=new JLabel("Notes: ");
	JLabel dateL=new JLabel("  Recording date (dd:mm:yyyy): ");
	JLabel timeL=new JLabel("Recording time (hh:mm:ss:mmm): ");
	
	JLabel eleNumber, sylNumber;
	
	JTextField nameT,eleNumberT, sylNumberT, recordEquipmentT, recordistT, locationT;
	JTextArea notesT;
	
	JSpinner timeSpinner, dateSpinner;
	SpinnerDateModel timeModel, dateModel;
	Calendar cal=Calendar.getInstance();
	  
	JPanel contentPane=new JPanel();
	int ID;
	Song song;
	Defaults defaults;
	
	public SongInformation(DatabaseTree treePanel, DataBaseController dbc, int ID, Defaults defaults){
		this.treePanel=treePanel;
		this.dbc=dbc;
		this.ID=ID;
		this.defaults=defaults;
		populateContentPane();
	}
	
	public void populateContentPane(){		
	
		song=dbc.loadSongFromDatabase(ID, 0);
	
		int p=song.eleList.size();
		String s1="Number of elements: "+p;
		eleNumberL=new JLabel(s1);
		int q=song.syllList.size();
		String s2="Number of syllables: "+q;
		sylNumberL=new JLabel(s2);
		
		if (song.phrases==null){song.interpretSyllables();}
		int r=song.phrases.size();
		String s3="Number of phrases "+r;
		phrNumberL=new JLabel(s3);
		
		nameT=new JTextField(song.name);
		nameT.setColumns(15);
		recordEquipmentT=new JTextField(song.recordEquipment);
		recordEquipmentT.setColumns(15);
		recordistT=new JTextField(song.recordist);
		recordistT.setColumns(15);
		locationT=new JTextField(song.location);
		locationT.setColumns(15);

		notesT=new JTextArea(song.notes);
		notesT.setColumns(15);
		notesT.setRows(5);
		notesT.setBorder(BorderFactory.createLoweredBevelBorder());
		notesT.setLineWrap(true);
		notesT.setWrapStyleWord(true);
		
		save.setActionCommand(SAVE_COMMAND);
		save.addActionListener(this);
				
		saveSound.setActionCommand(SAVE_SOUND_COMMAND);
		saveSound.addActionListener(this);
		
		useLast.setActionCommand(USE_LAST_COMMAND);
		useLast.addActionListener(this);
	
		//contentPane.setLayout(new GridLayout(0,2));
		
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
		
		contentPane.setBorder(BorderFactory.createMatteBorder(5,5,5,5,this.getBackground()));
		
		cal.setTimeInMillis(song.tDate);
		Date initDate = cal.getTime();
        cal.setTimeInMillis(System.currentTimeMillis());
		Date latestDate = cal.getTime();
		cal.add(Calendar.YEAR, -50);
		Date earliestDate = cal.getTime();
		cal.setTimeInMillis(song.tDate);
		
        dateModel = new SpinnerDateModel(initDate, earliestDate,latestDate, Calendar.DAY_OF_MONTH);
		dateSpinner=new JSpinner(dateModel);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy"));
		
		
		cal.setTimeInMillis(song.tDate);
		Date initTime = cal.getTime();
		
		cal.add(Calendar.HOUR, 24);
					
		cal.add(Calendar.HOUR, -48);
		
		cal.setTimeInMillis(song.tDate);
		
		timeModel = new SpinnerDateModel(initTime, earliestDate, latestDate, Calendar.MILLISECOND);
		timeSpinner=new JSpinner(timeModel);
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm:ss:SSS"));
		timeModel.setCalendarField(Calendar.MILLISECOND);

		JPanel namePane=new JPanel(new BorderLayout());
		namePane.add(nameL, BorderLayout.LINE_START);
		namePane.add(nameT, BorderLayout.CENTER);
		
		JPanel timePane=new JPanel(new BorderLayout());
		timePane.add(timeL, BorderLayout.LINE_START);
		timePane.add(timeSpinner, BorderLayout.CENTER);
		
		JPanel datePane=new JPanel(new BorderLayout());
		datePane.add(dateL, BorderLayout.LINE_START);
		datePane.add(dateSpinner, BorderLayout.CENTER);
		
		JPanel recEqPane=new JPanel(new BorderLayout());
		recEqPane.add(recordEquipmentL, BorderLayout.LINE_START);
		recEqPane.add(recordEquipmentT, BorderLayout.CENTER);

		JPanel recPane=new JPanel(new BorderLayout());
		recPane.add(recordistL, BorderLayout.LINE_START);
		recPane.add(recordistT, BorderLayout.CENTER);

		JPanel notesPane=new JPanel(new BorderLayout());
		notesPane.add(notesL, BorderLayout.LINE_START);
		notesPane.add(notesT, BorderLayout.CENTER);
		
		JPanel locationPane=new JPanel(new BorderLayout());
		locationPane.add(locationL, BorderLayout.LINE_START);
		locationPane.add(locationT, BorderLayout.CENTER);
		
		contentPane.add(namePane);
		contentPane.add(eleNumberL);
		contentPane.add(sylNumberL);
		contentPane.add(phrNumberL);
		contentPane.add(timePane);
		contentPane.add(datePane);
		contentPane.add(locationPane);
		contentPane.add(recEqPane);
		contentPane.add(recPane);
		contentPane.add(notesPane);
		contentPane.add(useLast);
		contentPane.add(saveSound);
		contentPane.add(save);
		
		this.add(contentPane);

	}
	
	/*
	public void wrapUp(){
		String []query=new String[5];
		treePanel.selnode.setUserObject(nameT.getText());
		query[0]="UPDATE songdata SET name='"+nameT.getText()+"' WHERE id="+ID;
		query[1]="UPDATE songdata SET RecordingEquipment='"+recordEquipmentT.getText()+"' WHERE id="+ID;
		query[2]="UPDATE songdata SET Recorder='"+recordistT.getText()+"' WHERE id="+ID;
		query[3]="UPDATE songdata SET call_context='"+notesT.getText()+"' WHERE id="+ID;
		query[4]="UPDATE individual SET name='"+treePanel.selnode.toString()+"' WHERE id="+treePanel.selnode.dex;
		for (int i=0; i<5; i++){
			System.out.println(i);
			dbc.writeToDataBase(query[i]);
		}
	}
	*/
	
	public void actionPerformed(ActionEvent e){
		String command = e.getActionCommand();
		if (SAVE_COMMAND.equals(command)) {
			wrapUp();
		}
		else if (SAVE_SOUND_COMMAND.equals(command)){
			Song song=dbc.loadSongFromDatabase(ID, 0);
			song.makeAudioFormat();
			@SuppressWarnings("unused")
			SaveSound ss=new SaveSound(song, song.af, this, 0, song.rawData.length, defaults); 									   									   
		}
		else if (USE_LAST_COMMAND.equals(command)){
			
			defaults.getSongDetails(song);
			recordEquipmentT.setText(song.recordEquipment);
			recordistT.setText(song.recordist);
			locationT.setText(song.location);			
		}
									   
									   
		
	}
	
	public void addInLast(){
		defaults.getSongDetails(song);
		recordEquipmentT.setText(song.recordEquipment);
		recordistT.setText(song.recordist);
		locationT.setText(song.location);
	}
	
	public void wrapUp(){
	
		treePanel.selnode[0].setUserObject(nameT.getText());
		treePanel.revalidate();
		treePanel.repaint();
		Calendar timeCal=Calendar.getInstance();
		timeCal.setTime(timeModel.getDate());
		
		Calendar dateCal=Calendar.getInstance();
		dateCal.setTime(dateModel.getDate());
		dateCal.add(Calendar.HOUR, timeCal.get(Calendar.HOUR)-dateCal.get(Calendar.HOUR));
		dateCal.add(Calendar.MINUTE, timeCal.get(Calendar.MINUTE)-dateCal.get(Calendar.MINUTE));
		dateCal.add(Calendar.SECOND, timeCal.get(Calendar.SECOND)-dateCal.get(Calendar.SECOND));
		dateCal.add(Calendar.MILLISECOND, timeCal.get(Calendar.MILLISECOND)-dateCal.get(Calendar.MILLISECOND));
		
		song.name=nameT.getText();
		song.tDate=dateCal.getTimeInMillis();
		song.notes=notesT.getText();
		song.recordEquipment=recordEquipmentT.getText();
		song.recordist=recordistT.getText();
		song.location=locationT.getText();
		defaults.setSongDetails(song);
		dbc.writeSongInfo(song);
	}
	
}
