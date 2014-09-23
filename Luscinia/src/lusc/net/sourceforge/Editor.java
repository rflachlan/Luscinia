package lusc.net.sourceforge;
//
//  Editor.java
//  Luscinia
//
//  Created by Robert Lachlan on 5/11/06.
//  Copyright 2006 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import javax.sound.sampled.*;

public class Editor {

	String thpath, name;

	public Editor(DataBaseController dbc, String user, JPanel pane, int test){
	

		File file;
		JFileChooser fc=new JFileChooser();
		ReadIniFile();
		if (thpath!=null){fc=new JFileChooser(thpath);}
		fc.addChoosableFileFilter(new SpectrogramFileFilter());
		fc.setMultiSelectionEnabled(false);
		int returnVal = fc.showOpenDialog(pane);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file  = fc.getSelectedFile();
			thpath=file.getPath();
			name=file.getName();
			WriteIniFile(thpath, name);
			if (name.endsWith(".wav")){
				try {
					Song song=new Song();
					song.tDate=file.lastModified();
					AudioInputStream AFStream = AudioSystem.getAudioInputStream(file);
					song.sampleRate=AFStream.getFormat().getSampleRate();
					song.stereo=AFStream.getFormat().getChannels();
					song.frameSize=AFStream.getFormat().getFrameSize();
					int length=(int)(song.frameSize*AFStream.getFrameLength());
					song.bigEnd=AFStream.getFormat().isBigEndian();
					AudioFormat.Encoding afe=AFStream.getFormat().getEncoding();
					song.signed=false;
					if (afe.toString().startsWith("PCM_SIGNED")){song.signed=true;}
					song.ssizeInBits=AFStream.getFormat().getSampleSizeInBits();
					//MainPanel mp=new MainPanel(dbc, song, 0, user);
				}
				catch (Exception e){}
			}
		} 
		file=null;
	}
	
	void ReadIniFile(){
		try{
			BufferedReader in = new BufferedReader(new FileReader("Ini.txt"));
			thpath=in.readLine();
			in.close();
	    }
	    catch(IOException e){}
	}
	
	void WriteIniFile(String path, String name){
		File output;
		try{
			output=new File("Ini.txt");
			PrintWriter Results = new PrintWriter(new FileWriter(output));	   
			Results.println(path);
			Results.println(name);
			Results.close();
		}
		catch(IOException e){}
	}	

}

