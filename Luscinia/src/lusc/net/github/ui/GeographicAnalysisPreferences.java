
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
	
	JFormattedTextField repeatsField, thresholdField, catField;
	
	Defaults defaults;
	
	String[] levels={"Type", "Species", "Population", "Individual"};
	JComboBox levelBox=new JComboBox(levels);
	
	public int numRepeats=10000;
	public int levelSel=1;
	
	public int numCategories=10;
	public double thresholdDiss=0.1;
	
	public GeographicAnalysisPreferences(Defaults defaults){
		this.defaults=defaults;

		numRepeats=defaults.getIntProperty("geognumrep", 10000);
		levelSel=defaults.getIntProperty("geoglevel", 1);
		
		numCategories=defaults.getIntProperty("geognumcat", 10);
		thresholdDiss=defaults.getDoubleProperty("geogthresh", 1000, 0.1);
		
		
		
		NumberFormat num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(0);
		
		NumberFormat num2=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(6);
		
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
		
		JPanel catPanel=new JPanel(new BorderLayout());
		catField=new JFormattedTextField(num);
		catField.setColumns(6);
		catField.setValue(new Integer(numCategories));
		JLabel catlab=new JLabel("Number of distance categories: ");
		catlab.setLabelFor(catField);
		catPanel.add(catlab, BorderLayout.WEST);	
		catPanel.add(catField, BorderLayout.CENTER);
		
		JPanel threshPanel=new JPanel(new BorderLayout());
		thresholdField=new JFormattedTextField(num2);
		thresholdField.setColumns(6);
		thresholdField.setValue(new Double(thresholdDiss));
		JLabel threshlab=new JLabel("Dissimilarity threshold: ");
		threshlab.setLabelFor(thresholdField);
		threshPanel.add(threshlab, BorderLayout.WEST);	
		threshPanel.add(thresholdField, BorderLayout.CENTER);
		
		this.setBorder(BorderFactory.createTitledBorder("Options"));
		this.setLayout(new GridLayout(0,1));
		
		
		this.add(repeatsPanel);
		this.add(levelPanel);
		this.add(catPanel);
		this.add(threshPanel);
	}
	
	public void wrapUp(){

		numRepeats=(int)((Number)repeatsField.getValue()).intValue();
		if (numRepeats<1){numRepeats=1;}
		levelSel=levelBox.getSelectedIndex();
		
		thresholdDiss=(double)((Number)thresholdField.getValue()).doubleValue();
		numCategories=(int)((Number)catField.getValue()).intValue();
		
		defaults.setIntProperty("geognumrep", numRepeats);
		defaults.setIntProperty("geoglevel", levelSel);
		defaults.setIntProperty("geognumcat", numCategories);
		defaults.setDoubleProperty("geogthresh", thresholdDiss, 1000);
	}
}
