package lusc.net.github.ui.compmethods;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import lusc.net.github.Defaults;
import lusc.net.github.ui.SaveDocument;

public class TunerInfo extends JPanel implements ActionListener{
	
	LinkedList<String[]> data=new LinkedList<String[]>();;
	String inputFileName=null;
	
	SaveDocument sd;
	Defaults defaults;
	String[] tuneOptions={"External File", "Individual", "Song Type"};
	JComboBox<String> tuneOp=new JComboBox<String>(tuneOptions);
	int tuneOption=0;
	
	String[] analysisLevel={"Element", "Syllable", "Phrase", "Song"};
	JComboBox<String> anLev=new JComboBox<String>(analysisLevel);
	int analyLev=0;
	
	JFormattedTextField nSampField;
	
	int nsamples=10000;
	
	JLabel flab=new JLabel("Location of external file: ");
	JLabel slab=new JLabel("Location of output file: ");
	
	JButton fileButton=new JButton("Set file");
	JButton fileButton2=new JButton("Set file");
	
	boolean tune=false;
	
	public TunerInfo(boolean tune, Defaults defaults){
		this.tune=tune;
		this.defaults=defaults;
		sd=new SaveDocument(this, defaults);
		if(tune){
			makePanel();
		}
		
	}
	
	public LinkedList<String[]> getData(){
		return data;
	}
	
	public int getTuneOption(){
		return tuneOption;
	}
	
	public int getAnalysisLevel(){
		return analyLev;
	}
	
	public int getNSamps(){
		return nsamples;
	}
	
	public boolean getTune(){
		return tune;
	}
	
	public SaveDocument getSD(){
		return sd;
	}
	
	public void getParameters(){
		tuneOption=tuneOp.getSelectedIndex();
		nsamples=(int)((Number)nSampField.getValue()).intValue();
		if (inputFileName!=null){
			parseExternalFile();
		}
		analyLev=anLev.getSelectedIndex();
		if (analyLev>0) {analyLev++;}
	}
	
	public void setLoc(){
		JFileChooser fc=new JFileChooser();
		
		int returnVal = fc.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file  = fc.getSelectedFile();
			inputFileName=file.getAbsolutePath();
			String s=file.getName();
			flab.setText("Location of external file: "+s);
		}
	}
	
	public void setOutput(){
		sd.makeFile();
		
		String s=sd.getName();
		slab.setText("Output location:"+s);
		
	}
	
	public void makePanel(){
		this.setPreferredSize(new Dimension(400,100));
		this.setLayout(new GridLayout(0,1));
		
		JPanel typePanel=new JPanel(new BorderLayout());
		
		JLabel typeLabel=new JLabel("Type of tuning: ");
		
		typePanel.add(typeLabel, BorderLayout.WEST);
		typePanel.add(tuneOp, BorderLayout.CENTER);
		
		this.add(typePanel);
		
		JPanel levelPanel=new JPanel(new BorderLayout());
		
		JLabel levelLabel=new JLabel("Level of analysis: ");
		
		levelPanel.add(levelLabel, BorderLayout.WEST);
		levelPanel.add(anLev, BorderLayout.CENTER);
		
		this.add(levelPanel);
		
		
		JPanel filePanel=new JPanel(new BorderLayout());
		
		filePanel.add(flab, BorderLayout.CENTER);
		
		fileButton.addActionListener(this);
		filePanel.add(fileButton, BorderLayout.EAST);
		
		this.add(filePanel);
		
		JPanel filePanel2=new JPanel(new BorderLayout());
		
		filePanel2.add(slab, BorderLayout.CENTER);
		
		fileButton2.addActionListener(this);
		filePanel2.add(fileButton2, BorderLayout.EAST);
		
		this.add(filePanel2);
		
		NumberFormat num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(0);
		
		JPanel nPanel=new JPanel(new BorderLayout());
		nSampField=new JFormattedTextField(num);
		nSampField.setColumns(10);
		nSampField.setValue(new Integer(nsamples));
		JLabel nSampLabel=new JLabel("Number of samples: ");
		nSampLabel.setLabelFor(nSampField);
		nPanel.add(nSampLabel, BorderLayout.WEST);	
		nPanel.add(nSampField, BorderLayout.CENTER);
		this.add(nPanel);		
	}
	
	public void parseExternalFile(){
		data=new LinkedList<String[]>();
		try{
			Scanner scanner = new Scanner(new File(inputFileName));
			while(scanner.hasNext()){		
				String line=scanner.nextLine();
				String[] fields = line.split(",");
				
				//if ((fields.length==11)&&(!fields[7].equals("0"))){
				//	data.add(fields);
				//}
				//else{
				//	System.out.println("OOPS: "+line);
				//}
				
				if ((fields.length==10)&&(!fields[9].equals("SongX"))){
					data.add(fields);
				}
				
				//if ((fields.length==22)&&(!fields[10].equals("Song2X"))){
				//	data.add(fields);
				//}
				else {
					System.out.println("OOPS: "+line);
				}

			}
			scanner.close();
		}
		catch(Exception e){e.printStackTrace();}
	}
	
	public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source.equals(fileButton)){
        	setLoc();
        }
        else{
        	setOutput();
        }
       
	}

}
