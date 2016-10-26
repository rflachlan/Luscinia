package lusc.net.github.ui;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import lusc.net.github.Defaults;

public class AndersonOptions extends JPanel {
	
	JFormattedTextField repeatsField;
	Defaults defaults;
	
	String[] levels={"Species", "Populations", "Individuals", "Sex", "Rank", "Age"};
	JComboBox levelsBox=new JComboBox(levels);

	public int numRepeats=10000;
	public int levelSel=1;

	
	public AndersonOptions(Defaults defaults){
		this.defaults=defaults;

		numRepeats=defaults.getIntProperty("andnumrep", 10000);
		levelSel=defaults.getIntProperty("andlevel", 1);
		
		NumberFormat num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(0);
		
		JPanel repeatsPanel=new JPanel(new BorderLayout());
		repeatsField=new JFormattedTextField(num);
		repeatsField.setColumns(6);
		repeatsField.setValue(new Integer(numRepeats));
		JLabel repeatslab=new JLabel("Number of permutations: ");
		repeatslab.setLabelFor(repeatsField);
		repeatsPanel.add(repeatslab, BorderLayout.WEST);	
		repeatsPanel.add(repeatsField, BorderLayout.CENTER);
		
		JPanel levelsPanel=new JPanel(new BorderLayout());
		levelsBox.setSelectedIndex(levelSel);
		JLabel levelslab=new JLabel("Level to compare: ");
		levelslab.setLabelFor(levelsBox);
		levelsPanel.add(levelslab, BorderLayout.WEST);	
		levelsPanel.add(levelsBox, BorderLayout.CENTER);
	
		this.setBorder(BorderFactory.createTitledBorder("Options"));
		this.setLayout(new GridLayout(0,1));
		
		
		this.add(repeatsPanel);
		this.add(levelsPanel);
	}
	
	public void wrapUp(){

		numRepeats=(int)((Number)repeatsField.getValue()).intValue();
		if (numRepeats<1){numRepeats=1;}
		levelSel=levelsBox.getSelectedIndex();
		defaults.setIntProperty("andnumrep", numRepeats);
		defaults.setIntProperty("andlevel", levelSel);
	}
}
