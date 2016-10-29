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
	
	
	JFormattedTextField syntKs, syntKmin, threshfield;
	
	
	String[] syntaxOptions={"Markov Chain", "Match length", "Category-less"};
	JComboBox syntOpts=new JComboBox(syntaxOptions);
	
	Defaults defaults;
	
	public int syntaxMode=2;
	public int maxSyntaxK=10;
	public int minSyntaxK=2;
	public double thresh=0.1;
	
	public SyntaxOptions(Defaults defaults){
		this.defaults=defaults;
		
		syntaxMode=defaults.getIntProperty("syntmode", 2);
		maxSyntaxK=defaults.getIntProperty("syntmaxk", 10);
		minSyntaxK=defaults.getIntProperty("syntmink", 2);
		thresh=defaults.getDoubleProperty("syntthresh", 0.1);
		
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
		
		JPanel kspan2=new JPanel(new BorderLayout());
		JLabel kslab2=new JLabel("        Minimum K syntax clusters: ");
		kspan2.add(kslab2, BorderLayout.WEST);
		syntKmin=new JFormattedTextField(num);
		syntKmin.setColumns(6);
		syntKmin.setValue(new Integer(minSyntaxK));
		kspan2.add(syntKmin, BorderLayout.CENTER);
		
		JPanel threshpan=new JPanel(new BorderLayout());
		JLabel threshlab=new JLabel("        Similarity threshold: ");
		threshpan.add(threshlab, BorderLayout.WEST);
		threshfield=new JFormattedTextField(num);
		threshfield.setColumns(6);
		threshfield.setValue(new Double(thresh));
		threshpan.add(threshfield, BorderLayout.CENTER);
	
		JPanel sypan=new JPanel(new BorderLayout());
		JLabel sylab=new JLabel("     Syntax methods: ");
		syntOpts.setSelectedIndex(syntaxMode);
		sypan.add(sylab, BorderLayout.WEST);
		sypan.add(syntOpts, BorderLayout.CENTER);
		
		this.add(kspan);
		this.add(kspan2);
		this.add(threshpan);
		this.add(sypan);
	}
	
	public void wrapUp(){
		
		maxSyntaxK=(int)((Number)syntKs.getValue()).intValue();
		minSyntaxK=(int)((Number)syntKmin.getValue()).intValue();
		if (maxSyntaxK<2){maxSyntaxK=2;}
		thresh=((Number)threshfield.getValue()).doubleValue();
		syntaxMode=syntOpts.getSelectedIndex();
		defaults.setIntProperty("syntmode", syntaxMode);
		defaults.setIntProperty("syntmaxk", maxSyntaxK);
		defaults.setIntProperty("syntmink", minSyntaxK);
		defaults.setDoubleProperty("syntthresh", thresh, 6);
	}
}
