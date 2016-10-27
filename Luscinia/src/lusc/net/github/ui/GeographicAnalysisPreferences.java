
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

public class GeographicAnalysisPreferences extends JPanel {
	
	JFormattedTextField repeatsField;
	Defaults defaults;
	
	String[] levels={"Type", "Species", "Population", "Individual"};
	JComboBox levelBox=new JComboBox(levels);
	
	public int numRepeats=10000;
	public int levelSel=1;
	
	
	public GeographicAnalysisPreferences(Defaults defaults){
		this.defaults=defaults;

		numRepeats=defaults.getIntProperty("geognumrep", 10000);
		levelSel=defaults.getIntProperty("geoglevel", 1);
		
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
		
		this.setBorder(BorderFactory.createTitledBorder("Options"));
		this.setLayout(new GridLayout(0,1));
		
		
		this.add(repeatsPanel);
		this.add(levelPanel);
	}
	
	public void wrapUp(){

		numRepeats=(int)((Number)repeatsField.getValue()).intValue();
		if (numRepeats<1){numRepeats=1;}
		levelSel=levelBox.getSelectedIndex();
		defaults.setIntProperty("geognumrep", numRepeats);
		defaults.setIntProperty("geoglevel", levelSel);
	}
}
