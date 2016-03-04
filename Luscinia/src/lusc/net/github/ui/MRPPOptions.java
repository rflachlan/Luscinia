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

public class MRPPOptions extends JPanel {
	
	JFormattedTextField repeatsField;
	Defaults defaults;
	
	String[] levels={"Species", "Population", "Individual"};
	JComboBox levelBox=new JComboBox(levels);
	
	String[] weightings={"n", "n-1", "n(n-1)"};
	JComboBox weightingBox=new JComboBox(weightings);
	
	JCheckBox pairwiseBox=new JCheckBox("Calculate pairwise differences: ");

	public int numRepeats=10000;
	public int levelSel=1;
	public int weightingSel=0;
	public boolean pairwise=false;
	
	
	public MRPPOptions(Defaults defaults){
		this.defaults=defaults;

		numRepeats=defaults.getIntProperty("mrppnumrep", 10000);
		levelSel=defaults.getIntProperty("mrpplevel", 1);
		weightingSel=defaults.getIntProperty("mrppweight", 0);
		pairwise=defaults.getBooleanProperty("mrpppair", true);
		
		NumberFormat num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(0);
		
		JPanel repeatsPanel=new JPanel(new BorderLayout());
		repeatsField=new JFormattedTextField(num);
		repeatsField.setColumns(6);
		repeatsField.setValue(new Integer(numRepeats));
		JLabel repeatslab=new JLabel("Number of resamples: ");
		repeatslab.setLabelFor(repeatsField);
		repeatsPanel.add(repeatslab, BorderLayout.WEST);	
		repeatsPanel.add(repeatsField, BorderLayout.CENTER);
		
		JPanel levelPanel=new JPanel(new BorderLayout());
		levelBox.setSelectedIndex(levelSel);
		JLabel levellab=new JLabel("Level to compare: ");
		levellab.setLabelFor(levelBox);
		levelPanel.add(levellab, BorderLayout.WEST);	
		levelPanel.add(levelBox, BorderLayout.CENTER);
		
		JPanel weightingPanel=new JPanel(new BorderLayout());
		weightingBox.setSelectedIndex(weightingSel);
		JLabel weightinglab=new JLabel("Weighting method: ");
		weightinglab.setLabelFor(weightingBox);
		weightingPanel.add(weightinglab, BorderLayout.WEST);	
		weightingPanel.add(weightingBox, BorderLayout.CENTER);
		
		pairwiseBox.setSelected(pairwise);
	
		this.setBorder(BorderFactory.createTitledBorder("Options"));
		this.setLayout(new GridLayout(0,1));
		
		
		this.add(repeatsPanel);
		this.add(levelPanel);
		this.add(weightingPanel);
		this.add(pairwiseBox);
	}
	
	public void wrapUp(){

		numRepeats=(int)((Number)repeatsField.getValue()).intValue();
		if (numRepeats<1){numRepeats=1;}
		levelSel=levelBox.getSelectedIndex();
		weightingSel=weightingBox.getSelectedIndex();
		defaults.setIntProperty("mrppnumrep", numRepeats);
		defaults.setIntProperty("mrpplevel", levelSel);
		defaults.setIntProperty("mrppweight", weightingSel);
		defaults.setBooleanProperty("mrpppair", pairwise);
	}
}
