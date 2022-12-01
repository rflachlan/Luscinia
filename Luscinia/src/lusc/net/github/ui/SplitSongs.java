package lusc.net.github.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Blob;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import lusc.net.github.Defaults;
import lusc.net.github.Song;
import lusc.net.github.analysis.AnalysisGroup;
import lusc.net.github.db.DataBaseController;

public class SplitSongs extends JPanel{

	JButton save=new JButton("Save");
	AnalysisGroup ag;
	
	public SplitSongs(AnalysisGroup ag, Defaults defaults){
		this.ag=ag;
		
		
		String thpath=defaults.props.getProperty("path");
		
				
		
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JFileChooser fc=null;
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
				int returnVal = fc.showSaveDialog(new JLabel("Save files"));
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					new File(fc.getSelectedFile().getPath()).mkdir();
					split(fc.getSelectedFile().getPath());
				}
			}
		});
		
		this.add(save);
	}
	
	public void split(String path) {
		Song[] songs=ag.getSongs();
		DataBaseController dbc=ag.getDBC();
		
		for (int i=0; i<songs.length; i++){
			int ID=songs[i].getSongID();
			Song song2=dbc.loadSongFromDatabase(ID, 0);
			
			songs[i].setFrameSize(song2.getFrameSize());
			songs[i].setStereo(song2.getStereo());
			songs[i].setSampleRate(song2.getSampleRate());
			songs[i].setRawData(song2.getRawData());
			songs[i].setBigEnd(song2.getBigEnd());
			songs[i].setSizeInBits(song2.getSizeInBits());
			songs[i].setSigned(song2.getSigned());
			songs[i].setOverallSize();
			
			songs[i].setFFTParameters();
			
			String name=song2.getName();
			if (name.endsWith(".wav")) {
				String name2=name.substring(0, name.length()-4);
				songs[i].setName(name2);
			}
	
			Song[] spl=songs[i].splitSong(true);
			for (int j=0; j<spl.length; j++) {
				if (spl[j]!=null) {
					spl[j].makeAudioFormat();
					spl[j].setName(spl[j].getName()+".wav");
					File file=new File(path, spl[j].getName());
					new SaveSound(spl[j], spl[j].getAf(), 0, spl[j].getRDLength(), file);	
				}
			}
		}	
	}
}
