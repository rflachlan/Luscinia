package lusc.net.github.ui;
//
//  SimpleAnalysis.java
//  Luscinia
//
//  Created by Robert Lachlan on 10/02/05.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import java.awt.*;

import javax.swing.*;

import java.awt.event.*;
import java.util.*;


public class SimpleAnalysis extends JPanel implements ActionListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3229154258521205847L;
	AnalysisChoose ac;
	private static String ADD = "add";
    private static String REMOVE = "remove";
	JButton add, remove;
	JList list;
	DefaultListModel listModel;
	JScrollPane listScroller;
	LinkedList leftList=new LinkedList();
	int[] table;

	public SimpleAnalysis (AnalysisChoose ac){
		this.ac=ac;
		table=new int[ac.archIds.length];
		JPanel pane=new JPanel();
		pane.setLayout(new BorderLayout());
		pane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		add=new JButton("Add Sound");
		add.setActionCommand(ADD);
		add.addActionListener(this);
		remove=new JButton("Remove Sound");
		remove.setActionCommand(REMOVE);
		remove.addActionListener(this);
		listModel = new DefaultListModel();
		list=new JList(listModel); 
		list.setVisibleRowCount(-1);
		listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(250, 500));
		
		JPanel sub=new JPanel();
		sub.add(add);
		sub.add(remove);
		pane.add(sub, BorderLayout.NORTH);
		pane.add(listScroller, BorderLayout.CENTER);
		
		this.add(pane);
	}
	
	public void loadScheme(LinkedList t){
		table=new int[ac.archIds.length];
		//leftList=new LinkedList();
		//listModel.removeAllElements();
		for (int j=1; j<t.size(); j++){
			int[] labels=(int[])t.get(j);
			Integer p=new Integer(labels[0]);
			boolean found=false;
			for (int k=0; k<leftList.size(); k++){
				Integer q=(Integer)leftList.get(k);
				if (q.intValue()==labels[0]){found=true;}
			}
			if (!found){leftList.add(p);}
			int a=0;
			for (int k=0; k<ac.archIds.length; k++){
				if (ac.archIds[k]==labels[0]){
					String nam=ac.archIndNams[k]+":"+ac.archNams[k];
					if (!found){listModel.addElement(nam);}
					//if (!found){listModel.addElement(ac.archNams[k]);}
					a=k;
				}
			}
			table[a]=1;
		}
	}
	
	public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        
        if (ADD.equals(command)) {
			int selected[]=ac.treePanel.findSelected();
			if (selected.length>0){
				for (int i=0; i<selected.length; i++){
					boolean found=false;
					for (int j=0; j<leftList.size(); j++){
						Integer q=(Integer)leftList.get(j);
						if (q.intValue()==selected[i]){found=true;}
					}
					if (!found){
						Integer p=new Integer(selected[i]);
						leftList.add(p);
						for (int j=0; j<ac.archIds.length; j++){
							if (selected[i]==ac.archIds[j]){
								String nam=ac.archIndNams[j]+":"+ac.archNams[j];
								if (!found){listModel.addElement(nam);}
								//listModel.addElement(ac.archNams[j]);}
							}
						}

					}
				}
			}
		}
		
        else if (REMOVE.equals(command)) {
			int index[] = list.getSelectedIndices();
			for (int i=0; i<index.length; i++){
				listModel.remove(index[i]-i);
				leftList.remove(index[i]-i);
			}
		}
		
	}
}
