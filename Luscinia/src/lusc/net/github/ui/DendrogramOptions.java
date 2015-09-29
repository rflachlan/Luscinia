package lusc.net.github.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import lusc.net.github.Defaults;

public class DendrogramOptions extends JPanel {
	
	
	JCheckBox interactiveViewBox=new JCheckBox("Interactive View");
	JCheckBox printViewBox=new JCheckBox("Publication View");
	JCheckBox useRawBox=new JCheckBox("Use raw dissimilarity matrix");
	
	String[] dendrogramOptions={"UPGMA", "Ward's Method", "Flexible Beta", "Complete linkage", "Single linkage"};
	JComboBox dendOpts=new JComboBox(dendrogramOptions);
	
	JFormattedTextField betaField;
	Defaults defaults;
	
	boolean intView, printView, useRaw;
	int dendType;
	double beta=-0.25;
	
	public DendrogramOptions(Defaults defaults){
		this.defaults=defaults;
		
		intView=defaults.getBooleanProperty("dendintview", true);		
		printView=defaults.getBooleanProperty("dendprintview", false);
		useRaw=defaults.getBooleanProperty("denduseraw", false);
		dendType=defaults.getIntProperty("dendtype", 0);
		beta=defaults.getDoubleProperty("dendbeta", 100000, -0.25);
		
		System.out.println("DEF GET: "+intView+" "+printView+" "+useRaw+" "+dendType+" "+beta);

		
		interactiveViewBox.setSelected(intView);
		printViewBox.setSelected(printView);
		useRawBox.setSelected(useRaw);
		dendOpts.setSelectedIndex(dendType);
		JLabel dendOptsLabel=new JLabel("Dendrogram Method: ");
		dendOptsLabel.setLabelFor(dendOpts);
		
		NumberFormat num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(5);
		betaField=new JFormattedTextField(num);
		betaField.setText(Double.toString(beta));
		JLabel betaFieldLabel=new JLabel("Beta: ");
		betaFieldLabel.setLabelFor(betaField);
		
		this.setBorder(BorderFactory.createTitledBorder("Options"));
		this.setLayout(new GridLayout(0,1));
		
		this.add(interactiveViewBox);
		this.add(printViewBox);
		this.add(useRawBox);
		
		JPanel optsPanel=new JPanel(new BorderLayout());
		optsPanel.add(dendOpts, BorderLayout.CENTER);
		optsPanel.add(dendOptsLabel, BorderLayout.WEST);
		this.add(optsPanel);
		
		JPanel betaPanel=new JPanel(new BorderLayout());
		betaPanel.add(betaField, BorderLayout.CENTER);
		betaPanel.add(betaFieldLabel, BorderLayout.WEST);
		this.add(betaPanel);
	}
	
	public void wrapUp(){
		intView=interactiveViewBox.isSelected();
		printView=printViewBox.isSelected();
		useRaw=useRawBox.isSelected();
		dendType=dendOpts.getSelectedIndex();
		beta=(double)((Number)betaField.getValue()).doubleValue();
		System.out.println("DEF SET: "+intView+" "+printView+" "+useRaw+" "+dendType+" "+beta);

		defaults.setBooleanProperty("dendintview", intView);
		defaults.setBooleanProperty("dendprintview", printView);
		defaults.setBooleanProperty("denduseraw", useRaw);
		defaults.setIntProperty("dendtype", dendType);
		defaults.setDoubleProperty("dendbeta", beta, 100000);
	}
}
