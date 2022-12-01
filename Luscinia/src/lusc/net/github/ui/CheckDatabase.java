package lusc.net.github.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.LinkedList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import lusc.net.github.Defaults;
import lusc.net.github.Song;
import lusc.net.github.analysis.AnalysisGroup;

public class CheckDatabase extends JPanel{
	
	JTextArea results=new JTextArea();

	public CheckDatabase(AnalysisGroup ag, Defaults defaults){
		
		Song[] songs=ag.getSongs();
		
		for (int i=0; i<songs.length; i++){
			results.append(songs[i].getName()+"\n");
			LinkedList<String[]> sy=songs[i].checkSyllables();
			
			for (String[] x : sy){
				for (int k=0; k<x.length; k++){
					results.append(x[k]+" ");
				}
				results.append("\n");
			}
			
			LinkedList<String[]> el=songs[i].checkElements();
			
			for (String[] x : el){
				for (int k=0; k<x.length; k++){
					results.append(x[k]+" ");
				}
				results.append("\n");
			}

		}
		
		JScrollPane sp=new JScrollPane(results);
		sp.setPreferredSize(this.getSize());
		this.setLayout(new BorderLayout());
		this.add(sp, BorderLayout.CENTER);
		
		
		
	}
}
