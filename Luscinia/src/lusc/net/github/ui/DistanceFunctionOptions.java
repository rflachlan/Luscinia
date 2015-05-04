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

public class DistanceFunctionOptions extends JPanel {
	
	JFormattedTextField densityField, nnField;
	Defaults defaults;

	public int densityCount=10;
	public int knn=10;

	
	public DistanceFunctionOptions (Defaults defaults){
		this.defaults=defaults;

		densityCount=defaults.getIntProperty("distfuncdens", 10);
		knn=defaults.getIntProperty("distfuncknn", 10);
		
		NumberFormat num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(0);
		
		JPanel densPanel=new JPanel(new BorderLayout());
		densityField=new JFormattedTextField(num);
		densityField.setColumns(6);
		densityField.setValue(new Integer(densityCount));
		JLabel densitylab=new JLabel("Number of density categories: ");
		densitylab.setLabelFor(densityField);
		densPanel.add(densitylab, BorderLayout.WEST);	
		densPanel.add(densityField, BorderLayout.CENTER);
		
		JPanel nnPanel=new JPanel(new BorderLayout());
		nnField=new JFormattedTextField(num);
		nnField.setColumns(6);
		nnField.setValue(new Integer(knn));
		JLabel nnlab=new JLabel("Number of nearest neighbor categories: ");
		nnlab.setLabelFor(nnField);
		nnPanel.add(nnlab, BorderLayout.WEST);	
		nnPanel.add(nnField, BorderLayout.CENTER);
	
		this.setBorder(BorderFactory.createTitledBorder("Options"));
		this.setLayout(new GridLayout(0,1));
		
		
		this.add(densPanel);
		this.add(nnPanel);
	}
	
	public void wrapUp(){

		densityCount=(int)((Number)densityField.getValue()).intValue();
		if (densityCount<1){densityCount=1;}
		knn=(int)((Number)nnField.getValue()).intValue();
		if (knn<1){knn=1;}
		defaults.setIntProperty("distfuncdens", densityCount);
		defaults.setIntProperty("distfuncknn", knn);
	}
}
