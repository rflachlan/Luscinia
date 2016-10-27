package lusc.net.github.ui.spectrogram;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class PopUp implements ActionListener{
	
	MainPanel mp;
	int x,y, id, type, ne;
	Component component;
	
	JPopupMenu pop=new JPopupMenu();
	JMenuItem delete=new JMenuItem("Delete");
	JMenuItem mergeLeft=new JMenuItem("Merge with previous");
	JMenuItem mergeRight=new JMenuItem("Merge with next");
	
	public PopUp(MainPanel mp, int x, int y, Component component, int id, int type, int ne){
		this.mp=mp;
		this.x=x;
		this.y=y;
		this.component=component;
		this.id=id;
		this.type=type;
		this.ne=ne;
		
		delete.addActionListener(this);	
		mergeLeft.addActionListener(this);
		mergeRight.addActionListener(this);
		
		System.out.println("POPUP"+type+" "+id+" "+ne);
		
		pop.add(delete);
		if (type==0){
			if (id>0){
				pop.add(mergeLeft);
			}
			if (id<ne-1){
				pop.add(mergeRight);
			}
		}	
		pop.show(component, x,y);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==delete){
			if (type==0){
				mp.deleteElement(id);
			}
			else{
				mp.deleteSyllable(id);
			}
		}
		if (e.getSource()==mergeLeft){
			mp.s.merge(id-1);
		}
		else if (e.getSource()==mergeRight){
			mp.s.merge(id);
		}
		
	}

}
