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

public class SNNOptions extends JPanel implements PropertyChangeListener{
	
	JFormattedTextField snnKF, snnMinPtsF, snnEpsF;
	Defaults defaults;
	
	int snnK=10;
	int snnMinPts=4;
	int snnEps=6;
	
	public SNNOptions(Defaults defaults){
		this.defaults=defaults;

		snnK=defaults.getIntProperty("snnk", 10);
		snnMinPts=defaults.getIntProperty("snnminpts", 10);
		snnEps=defaults.getIntProperty("snneps", 10);

		
		NumberFormat num=NumberFormat.getNumberInstance();
		num.setMaximumFractionDigits(0);
				
		JPanel snnpana=new JPanel(new BorderLayout());
		JLabel snnlab=new JLabel("SNN k: ");
		snnpana.add(snnlab, BorderLayout.WEST);
		snnKF=new JFormattedTextField(num);
		snnKF.setColumns(6);
		snnKF.addPropertyChangeListener("value", this);
		snnKF.setValue(new Integer(snnK));
		snnpana.add(snnKF, BorderLayout.CENTER);
		
		JPanel snnpanb=new JPanel(new BorderLayout());
		JLabel snnlabb=new JLabel("SNN MinPts: ");
		snnpanb.add(snnlabb, BorderLayout.WEST);
		snnMinPtsF=new JFormattedTextField(num);
		snnMinPtsF.setColumns(6);
		snnMinPtsF.addPropertyChangeListener("value", this);
		snnMinPtsF.setValue(new Integer(snnMinPts));
		snnpanb.add(snnMinPtsF, BorderLayout.CENTER);
		
		JPanel snnpanc=new JPanel(new BorderLayout());
		JLabel snnlabc=new JLabel("SNN EPS: ");
		snnpanc.add(snnlabc, BorderLayout.WEST);
		snnEpsF=new JFormattedTextField(num);
		snnEpsF.setColumns(6);
		snnEpsF.addPropertyChangeListener("value", this);
		snnEpsF.setValue(new Integer(snnEps));
		snnpanc.add(snnEpsF, BorderLayout.CENTER);
		
	
		this.setBorder(BorderFactory.createTitledBorder("Options"));
		this.setLayout(new GridLayout(0,1));
		
		
		this.add(snnpana);
		this.add(snnpanb);
		this.add(snnpanc);
	}
	
	public void propertyChange(PropertyChangeEvent e) {
        Object source = e.getSource();

        if (source==snnKF){
			snnK = (int)((Number)snnKF.getValue()).intValue();
			//if (maxClusterK>50+minClusterK){maxClusterK=50+minClusterK;}
			if (snnK<4){snnK=4;}
			snnKF.setValue(new Integer(snnK));
		}
		if (source==snnEpsF){
			snnEps = (int)((Number)snnEpsF.getValue()).intValue();
			//if (snnEps>=snnK){snnEps=snnK-1;}
			snnEpsF.setValue(new Integer(snnEps));
		}
		if (source==snnMinPtsF){
			snnMinPts = (int)((Number)snnMinPtsF.getValue()).intValue();
			if (snnMinPts>=snnK){snnMinPts=snnK-1;}
			snnMinPtsF.setValue(new Integer(snnMinPts));
		}
	}
	
	public void wrapUp(){
		defaults.setIntProperty("snnk", snnK);
		defaults.setIntProperty("snnminpts", snnMinPts);
		defaults.setIntProperty("snneps", snnEps);
	}
}
