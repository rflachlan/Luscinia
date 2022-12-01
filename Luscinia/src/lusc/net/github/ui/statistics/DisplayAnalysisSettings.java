package lusc.net.github.ui.statistics;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.*;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import lusc.net.github.Defaults;
import lusc.net.github.analysis.PrepareDTW;
import lusc.net.github.ui.AnalysisChoose;
import lusc.net.github.ui.SaveDocument;

public class DisplayAnalysisSettings extends DisplayPane{
	
	Defaults defaults;
	AnalysisChoose ac;
	LinkedList<String[]> data=new LinkedList<String[]>();
	
	public DisplayAnalysisSettings (Defaults defaults, AnalysisChoose ac, PrepareDTW pdtw){

		this.defaults=defaults;
		this.ac=ac;
		
		
		//JPanel mainpanel=new JPanel(new GridLayout(0,1));
		Font font=new Font("Sans-Serif", Font.PLAIN, 9);
		JTextArea output=new JTextArea(100,100);
		output.setFont(font);
		output.setEditable(false);
		JScrollPane scroll=new JScrollPane(output);
		
		
		String[] v=defaults.getVersions();
		String[]v1= {"Luscinia version", v[0]};
		String[]v2= {"Luscinia db version", v[1]};
		
		String[] t1= {"Date", java.time.LocalDate.now().toString()};
		String[] t2= {"Time", java.time.LocalTime.now().toString()};
		
		data.add(v1);
		data.add(v2);
		data.add(t1);
		data.add(t2);

		output.append("Luscinia version: "+v[0]+"\n");
		output.append("Luscinia db version: "+v[1]+"\n");
		output.append("Current date: "+t1[1]+"\n");
		output.append("Current time: "+t2[1]+"\n");
		
		LinkedList<String[]> analysisParams=ac.getAnalysisParameters();
		data.addAll(analysisParams);
		
		
		for (int i=0; i<analysisParams.size(); i++) {
			String[] x=analysisParams.get(i);
			output.append(x[0]+": "+x[1]+"\n");
		}
		
		int c=0;
		double[] sdt=pdtw.getSDTemp();
		for (int i=0; i<sdt.length; i++) {
			String[] x= {analysisParams.get(c)[0], Double.toString(sdt[i])};
			data.add(x);
			output.append(x[0]+" sd: "+x[1]+"\n");
			c++;
		}
		
		double[] sd=pdtw.getSD();
		for (int i=0; i<sd.length; i++) {
			String[] x= {analysisParams.get(c)[0], Double.toString(sd[i])};
			data.add(x);
			output.append(x[0]+" sd: "+x[1]+"\n");
			c++;
		}
		
		this.setLayout(new BorderLayout());
		this.add(scroll, BorderLayout.CENTER);
		
	}
	
	public void export(){
		SaveDocument sd=new SaveDocument(this, defaults);
		boolean readyToWrite=sd.makeFile();
		if (readyToWrite){
			sd.writeString("Parameter");
			sd.writeString("Value");
			sd.writeLine();
			
			for (int i=0; i<data.size(); i++) {
				String[] x=data.get(i);
				sd.writeString(x[0]);
				sd.writeString(x[1]);
				sd.writeLine();
			}	
			sd.finishWriting();
		}
		
	}
	

}
