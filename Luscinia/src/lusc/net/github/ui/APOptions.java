package lusc.net.github.ui;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import lusc.net.github.Defaults;

public class APOptions extends JPanel implements PropertyChangeListener{
	
	JFormattedTextField lambdaF, skkF, repsF;
	Defaults defaults;
	
	int reps=10;
	double skk=0.5;
	double lambda=0.5;
	
	public APOptions(Defaults defaults){
		this.defaults=defaults;

		reps=defaults.getIntProperty("apreps", 10);
		skk=defaults.getDoubleProperty("apskk", 100000, 0.5);
		lambda=defaults.getDoubleProperty("aplambda", 100000, 0.5);
		
		
		System.out.println("READ DEF: "+skk+" "+lambda);
		
		NumberFormat num1=NumberFormat.getNumberInstance();
		num1.setMaximumFractionDigits(0);
		NumberFormat num2=NumberFormat.getNumberInstance();
		num2.setMaximumFractionDigits(6);
				
		JPanel appana=new JPanel(new BorderLayout());
		JLabel skklab=new JLabel("s(k,k): ");
		appana.add(skklab, BorderLayout.WEST);
		skkF=new JFormattedTextField(num2);
		skkF.addPropertyChangeListener("value", this);
		skkF.setText(Double.toString(skk));
		appana.add(skkF, BorderLayout.CENTER);
		
		JPanel appanb=new JPanel(new BorderLayout());
		JLabel lambdaLab=new JLabel("Lambda: ");
		appanb.add(lambdaLab, BorderLayout.WEST);
		lambdaF=new JFormattedTextField(num2);
		lambdaF.setColumns(8);
		lambdaF.addPropertyChangeListener("value", this);
		lambdaF.setText(Double.toString(lambda));
		appanb.add(lambdaF, BorderLayout.CENTER);
		
		JPanel appanc=new JPanel(new BorderLayout());
		JLabel repsLab=new JLabel("Repetitions: ");
		appanc.add(repsLab, BorderLayout.WEST);
		repsF=new JFormattedTextField(num1);
		repsF.setColumns(6);
		repsF.addPropertyChangeListener("value", this);
		repsF.setValue(new Integer(reps));
		appanc.add(repsF, BorderLayout.CENTER);
		
	
		this.setBorder(BorderFactory.createTitledBorder("Options"));
		this.setLayout(new GridLayout(0,1));
		
		
		this.add(appana);
		this.add(appanb);
		this.add(appanc);
	}
	
	public void propertyChange(PropertyChangeEvent e) {
        Object source = e.getSource();

        if (source==skkF){
			skk = (double)((Number)skkF.getValue()).doubleValue();
			if (skk<0){skk=0;}
			if (skk>1){skk=1;}
			skkF.setText(Double.toString(skk));
		}
		if (source==lambdaF){
			lambda = (double)((Number)lambdaF.getValue()).doubleValue();
			if (lambda <0){lambda =0;}
			if (lambda >1){lambda =1;}
			lambdaF.setText(Double.toString(lambda));
		}
		if (source==repsF){
			reps = (int)((Number)repsF.getValue()).intValue();
			if (reps<=0){reps=1;}
			repsF.setValue(new Integer(reps));
		}
	}
	
	public void wrapUp(){
		
		System.out.println("WRITE DEF: "+skk+" "+lambda+" "+reps);
		defaults.setIntProperty("apreps", reps);
		defaults.setDoubleProperty("apskk", skk, 100000);
		defaults.setDoubleProperty("aplambda", lambda, 100000);
	}
}
