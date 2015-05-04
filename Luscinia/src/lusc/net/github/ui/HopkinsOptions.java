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

public class HopkinsOptions extends JPanel {
	
	JFormattedTextField repeatsField, picksField;
	Defaults defaults;
	
	String[] distributions={"Normal", "Uniform"};
	JComboBox distBox=new JComboBox(distributions);

	public int numRepeats=10000;
	int maxPicks=5;
	int distSel=0;

	
	public HopkinsOptions(Defaults defaults){
		this.defaults=defaults;

		numRepeats=defaults.getIntProperty("hopknumrep", 10000);
		maxPicks=defaults.getIntProperty("hopkmaxpick", 5);
		distSel=defaults.getIntProperty("hopkdist", 0);

		
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
		
		JPanel picksPanel=new JPanel(new BorderLayout());
		picksField=new JFormattedTextField(num);
		picksField.setColumns(6);
		picksField.setValue(new Integer(maxPicks));
		JLabel pickslab=new JLabel("k-nearest neighbor: ");
		pickslab.setLabelFor(picksField);
		picksPanel.add(pickslab, BorderLayout.WEST);	
		picksPanel.add(picksField, BorderLayout.CENTER);
		
		JPanel distPanel=new JPanel(new BorderLayout());
		distBox.setSelectedIndex(distSel);
		JLabel distlab=new JLabel("Probability distribution: ");
		distlab.setLabelFor(distBox);
		distPanel.add(distlab, BorderLayout.WEST);	
		distPanel.add(distBox, BorderLayout.CENTER);
	
		this.setBorder(BorderFactory.createTitledBorder("Options"));
		this.setLayout(new GridLayout(0,1));
		
		
		this.add(repeatsPanel);
		this.add(picksPanel);
		this.add(distPanel);
	}
	
	public void wrapUp(){

		numRepeats=(int)((Number)repeatsField.getValue()).intValue();
		if (numRepeats<1){numRepeats=1;}
		maxPicks=(int)((Number)picksField.getValue()).intValue();
		if (maxPicks<1){maxPicks=1;}
		distSel=distBox.getSelectedIndex();
		defaults.setIntProperty("hopknumrep", numRepeats);
		defaults.setIntProperty("hopkmaxpick", maxPicks);
		defaults.setIntProperty("hopkdist", distSel);
	}
}
