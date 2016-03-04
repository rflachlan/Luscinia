package lusc.net.github.ui;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import lusc.net.github.Defaults;

public class KMedoidsOptions extends JPanel implements PropertyChangeListener{
	
	JFormattedTextField clustKs, clustKsb, reseedField, simField;
	Defaults defaults;
	JCheckBox useRawBox=new JCheckBox("Use raw dissimilarity matrix");
	
	public int minClusterK=2;
	public int maxClusterK=10;
	public int numReseeds=20;
	public int nsims=10;
	public boolean useRaw=false;
	
	
	public KMedoidsOptions(Defaults defaults){
		this.defaults=defaults;

		minClusterK=defaults.getIntProperty("kmedmink", 2);
		maxClusterK=defaults.getIntProperty("kmedmaxk", 10);
		numReseeds=defaults.getIntProperty("kmedreseed", 20);
		nsims=defaults.getIntProperty("kmednsim", 10);
		useRaw=defaults.getBooleanProperty("kmeduseraw", false);
		
		useRawBox.setSelected(useRaw);
		
		NumberFormat num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(0);
				
		JPanel kcpana=new JPanel(new BorderLayout());
		JLabel kclab=new JLabel("Maximum K-medoid clusters: ");
		kcpana.add(kclab, BorderLayout.WEST);
		clustKs=new JFormattedTextField(num);
		clustKs.setColumns(6);
		clustKs.setValue(new Integer(maxClusterK));
		clustKs.addPropertyChangeListener("value", this);
		
		kcpana.add(clustKs, BorderLayout.CENTER);
		
		JPanel kcpanb=new JPanel(new BorderLayout());
		JLabel kclabb=new JLabel("Minimum K-medoid clusters: ");
		kcpanb.add(kclabb, BorderLayout.WEST);
		clustKsb=new JFormattedTextField(num);
		clustKsb.setColumns(6);
		clustKsb.setValue(new Integer(minClusterK));
		clustKsb.addPropertyChangeListener("value", this);
		
		kcpanb.add(clustKsb, BorderLayout.CENTER);
		
		JPanel reseedPan=new JPanel(new BorderLayout());
		JLabel reseedLab=new JLabel("Number of reseeds: ");
		reseedPan.add(reseedLab, BorderLayout.WEST);
		reseedField=new JFormattedTextField(num);
		reseedField.setColumns(8);
		reseedField.setValue(new Integer(numReseeds));
		reseedField.addPropertyChangeListener("value", this);
		
		reseedPan.add(reseedField, BorderLayout.CENTER);
		
		JPanel simPan=new JPanel(new BorderLayout());
		JLabel simLab=new JLabel("Number of simulations: ");
		simPan.add(simLab, BorderLayout.WEST);
		simField=new JFormattedTextField(num);
		simField.setColumns(8);
		simField.setValue(new Integer(nsims));
		simField.addPropertyChangeListener("value", this);
		
		simPan.add(simField, BorderLayout.CENTER);
		
	
		this.setBorder(BorderFactory.createTitledBorder("Options"));
		this.setLayout(new GridLayout(0,1));
		
		
		this.add(kcpana);
		this.add(kcpanb);
		this.add(reseedPan);
		this.add(simPan);
		this.add(useRawBox);

	}
	
	public void propertyChange(PropertyChangeEvent e) {
        Object source = e.getSource();

		if (source==clustKs){
			maxClusterK = (int)((Number)clustKs.getValue()).intValue();
			//if (maxClusterK>50+minClusterK){maxClusterK=50+minClusterK;}
			if (maxClusterK<2+minClusterK){maxClusterK=2+minClusterK;}
			clustKs.setValue(new Integer(maxClusterK));
		}
		if (source==clustKsb){
			minClusterK = (int)((Number)clustKsb.getValue()).intValue();
			if (minClusterK>maxClusterK-2){minClusterK=maxClusterK-2;}
			if (minClusterK<2){minClusterK=2;}
			clustKsb.setValue(new Integer(minClusterK));
		}
		if (source==reseedField){
			numReseeds = (int)((Number)reseedField.getValue()).intValue();
			if (numReseeds<1){numReseeds=1;}
			reseedField.setValue(new Integer(numReseeds));
		}
		if (source==simField){
			nsims = (int)((Number)simField.getValue()).intValue();
			if (nsims<1){nsims=1;}
			simField.setValue(new Integer(nsims));
		}
	}
	
	public void wrapUp(){
		useRaw=useRawBox.isSelected();
		defaults.setIntProperty("kmedmink", minClusterK);
		defaults.setIntProperty("kmedmaxk", maxClusterK);
		defaults.setIntProperty("kmedreseed", numReseeds);
		defaults.setIntProperty("kmednsim", nsims);
		defaults.setBooleanProperty("kmeduseraw", useRaw);
	}
}
