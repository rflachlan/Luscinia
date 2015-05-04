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

public class SyntaxOptions extends JPanel {
	
	
	JFormattedTextField syntKs;
	
	
	String[] syntaxOptions={"Markov Chain", "Match length", "Both"};
	JComboBox syntOpts=new JComboBox(syntaxOptions);
	
	Defaults defaults;
	
	int syntaxMode=2;
	int maxSyntaxK=10;
	
	public SyntaxOptions(Defaults defaults){
		this.defaults=defaults;
		
		syntaxMode=defaults.getIntProperty("syntmode", 2);
		maxSyntaxK=defaults.getIntProperty("syntmaxk", 10);
		
		NumberFormat num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(5);
		
		
		this.setBorder(BorderFactory.createTitledBorder("Options"));
		this.setLayout(new GridLayout(0,1));
		
		JPanel kspan=new JPanel(new BorderLayout());
		JLabel kslab=new JLabel("        Maximum K syntax clusters: ");
		kspan.add(kslab, BorderLayout.WEST);
		syntKs=new JFormattedTextField(num);
		syntKs.setColumns(6);
		syntKs.setValue(new Integer(maxSyntaxK));
		kspan.add(syntKs, BorderLayout.CENTER);
		
		JPanel sypan=new JPanel(new BorderLayout());
		JLabel sylab=new JLabel("     Syntax methods: ");
		syntOpts.setSelectedIndex(syntaxMode);
		sypan.add(sylab, BorderLayout.WEST);
		sypan.add(syntOpts, BorderLayout.CENTER);
		
		this.add(kspan);
		this.add(sypan);
	}
	
	public void wrapUp(){
		
		maxSyntaxK=(int)((Number)syntKs.getValue()).intValue();
		if (maxSyntaxK<2){maxSyntaxK=2;}
		syntaxMode=syntOpts.getSelectedIndex();
		defaults.setIntProperty("syntmode", syntaxMode);
		defaults.setIntProperty("syntmaxk", maxSyntaxK);
	}
}
