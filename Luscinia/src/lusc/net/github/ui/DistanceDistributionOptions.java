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

public class DistanceDistributionOptions extends JPanel {
	
	JFormattedTextField numColsField, fontSizeField;
	Defaults defaults;

	public int numCols=100;
	public int fontSize=12;
	
	public DistanceDistributionOptions(Defaults defaults){
		this.defaults=defaults;

		numCols=defaults.getIntProperty("ddonumcols", 100);
		fontSize=defaults.getIntProperty("ddofontsize", 12);

		
		NumberFormat num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(0);
		
		numColsField=new JFormattedTextField(num);
		numColsField.setText(Integer.toString(numCols));
		JLabel numColsLabel=new JLabel("Number of points: ");
		numColsLabel.setLabelFor(numColsField);
		
		fontSizeField=new JFormattedTextField(num);
		fontSizeField.setText(Integer.toString(fontSize));
		JLabel fontSizeLabel=new JLabel("Font size: ");
		fontSizeLabel.setLabelFor(fontSizeField);
		
		this.setBorder(BorderFactory.createTitledBorder("Options"));
		this.setLayout(new GridLayout(0,1));
		
		JPanel numColsPanel=new JPanel(new BorderLayout());
		numColsPanel.add(numColsField, BorderLayout.CENTER);
		numColsPanel.add(numColsLabel, BorderLayout.WEST);
		this.add(numColsPanel);
		
		JPanel fontSizePanel=new JPanel(new BorderLayout());
		fontSizePanel.add(fontSizeField, BorderLayout.CENTER);
		fontSizePanel.add(fontSizeLabel, BorderLayout.WEST);
		this.add(fontSizePanel);
	}
	
	public void wrapUp(){

		numCols=(int)((Number)numColsField.getValue()).intValue();
		fontSize=(int)((Number)fontSizeField.getValue()).intValue();
		defaults.setIntProperty("ddonumcols", numCols);
		defaults.setIntProperty("ddofontsize", fontSize);
	}
}
