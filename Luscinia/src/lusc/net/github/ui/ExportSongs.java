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

public class ExportSongs extends JPanel{

	JButton save=new JButton("Save");
	AnalysisGroup ag;
	
	public ExportSongs(AnalysisGroup ag, Defaults defaults){
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
					save(fc.getSelectedFile().getPath());
				}
			}
		});
		
		this.add(save);
	}
	
	public void save(String path) {
		Song[] songs=ag.getSongs();
		DataBaseController dbc=ag.getDBC();
		
		for (int i=0; i<songs.length; i++){
			int ID=songs[i].getSongID();
			Song song2=dbc.loadSongFromDatabase(ID, 0);
			song2.makeAudioFormat();
			
			String indname=song2.getIndividualName();
			File folder=new File(path, indname);
			if (!folder.exists()) {
				folder.mkdir();
			}
			
			File file=new File(folder.getPath(), song2.getName()+".wav");
			new SaveSound(song2, song2.getAf(), 0, song2.getRDLength(), file);
		}
	}
			
}
