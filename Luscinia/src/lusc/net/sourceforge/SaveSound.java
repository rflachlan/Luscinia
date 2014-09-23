package lusc.net.sourceforge;
//
//  SaveSound.java
//  Luscinia
//
//  Created by Robert Lachlan on 20/10/2005.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFileFormat;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;


public class SaveSound {
	String thpath, name;
	File file;
	Component panel;
	Defaults defaults;

	public SaveSound(Song song, AudioFormat af, Component panel, int start, int end, Defaults defaults){
		
		File trialFile=new File(song.name);
		
		this.panel=panel;	
		this.defaults=defaults;
		String thpath=defaults.props.getProperty("path");
		int returnVal = JFileChooser.APPROVE_OPTION;
		String[] formats={"wav"};
		JFileChooser fc=new JFileChooser();			
		if (thpath!=null){
			try{
				fc=new JFileChooser(thpath);
			}
			catch(Exception e){
				fc=new JFileChooser();
			}
		}
		else{fc=new JFileChooser();}
		fc.setDialogType(JFileChooser.SAVE_DIALOG);
		fc.setAcceptAllFileFilterUsed(false);
		for (int i=0; i<formats.length; i++){
			SpectrogramFileFilter sff=new SpectrogramFileFilter();
			sff.names=formats[i];
			fc.addChoosableFileFilter(sff);
		}
		fc.setSelectedFile(trialFile);
		returnVal = fc.showSaveDialog(panel);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
			int cont=0;
			if (file.exists()){
				cont= JOptionPane.showConfirmDialog(panel,"Do you really want to overwrite this file?\n"+"(It will be deleted permanently)","Confirm Overwrite", JOptionPane.YES_NO_OPTION);
			}
			if (cont==0){
				thpath=file.getParent();
				name=file.getName();
				String fname=file.getPath();
				SpectrogramFileFilter sf2=(SpectrogramFileFilter) fc.getFileFilter();
				
				if (!file.getName().endsWith(sf2.names)){
					file=new File(fname+"."+sf2.names);
				}
				else{
					file=new File(fname);
				}
				System.out.println("writing file to "+thpath+" "+sf2.names);
				defaults.props.setProperty("path", thpath);
				defaults.props.setProperty("filename", name);
				AudioFileFormat.Type targetFileType = AudioFileFormat.Type.WAVE;
				ByteArrayInputStream bais=new ByteArrayInputStream(song.rawData, start, end-start);
				long size = (end-start) / af.getFrameSize();
				AudioInputStream ais=new AudioInputStream(bais, af, size);
				int nWrittenBytes = 0;
				try{
					nWrittenBytes = AudioSystem.write(ais, targetFileType, file);
				}
				catch (IOException e){
					e.printStackTrace();
				}
			}
		}
	}
	
	public SaveSound(Song song, AudioFormat af, int start, int end, File file){
		AudioFileFormat.Type targetFileType = AudioFileFormat.Type.WAVE;
		ByteArrayInputStream bais=new ByteArrayInputStream(song.rawData, start, end-start);
		long size = (end-start) / af.getFrameSize();
		AudioInputStream ais=new AudioInputStream(bais, af, size);
		int nWrittenBytes = 0;
		try{
			nWrittenBytes = AudioSystem.write(ais, targetFileType, file);
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}	
}
