package lusc.net.github.ui;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import lusc.net.github.Defaults;

public class MDSOptions extends JPanel {
	
	JFormattedTextField numdims;
	Defaults defaults;

	public int numDims=100;
	
	public MDSOptions(Defaults defaults){
		this.defaults=defaults;

		numDims=defaults.getIntProperty("mdsnumdims", 2);

		
		NumberFormat num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(0);
		
		JPanel ndpan=new JPanel(new BorderLayout());
		
		numdims=new JFormattedTextField(num);
		numdims.setColumns(6);
		numdims.setValue(new Integer(numDims));
		
		JLabel numdimlab=new JLabel("Number of dimensions for NMDS: ");
		numdimlab.setLabelFor(numdims);
		ndpan.add(numdimlab, BorderLayout.WEST);	
		ndpan.add(numdims, BorderLayout.CENTER);
	
		this.setBorder(BorderFactory.createTitledBorder("Options"));
		this.setLayout(new GridLayout(0,1));
		
		
		this.add(ndpan);

	}
	
	public void wrapUp(){

		numDims=(int)((Number)numdims.getValue()).intValue();
		if (numDims<2){numDims=2;}
		defaults.setIntProperty("mdsnumdims", numDims);
	}
}
