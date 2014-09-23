package lusc.net.sourceforge;
//
//  ComplexAnalysis.java
//  Luscinia
//
//  Created by Robert Lachlan on 9/29/05.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class ComplexAnalysis extends JPanel implements ActionListener, ListSelectionListener{
	
	AnalysisChoose ac;
	private static String ADD1 = "add1";
    private static String REMOVE1 = "remove1";
	private static String ADD2 = "add2";
    private static String REMOVE2 = "remove2";
	JButton add1, add2, remove1, remove2;
	JList list1, list2;
	DefaultListModel listModel1, listModel2;
	JScrollPane listScroller1, listScroller2;
	
	LinkedList leftList=new LinkedList();
	LinkedList rightList=new LinkedList();
	int table[][]=null;
	

	public ComplexAnalysis (AnalysisChoose ac){
		this.ac=ac;
		table=new int[ac.archIds.length][ac.archIds.length];
		JPanel pane1=new JPanel();
		pane1.setLayout(new BorderLayout());
		pane1.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		add1=new JButton("Add Sound");
		add1.setActionCommand(ADD1);
		add1.addActionListener(this);
		remove1=new JButton("Remove Sound");
		remove1.setActionCommand(REMOVE1);
		remove1.addActionListener(this);
		listModel1 = new DefaultListModel();
		list1=new JList(listModel1); 
		list1.addListSelectionListener(this);
		list1.setVisibleRowCount(-1);
		listScroller1 = new JScrollPane(list1);
		listScroller1.setPreferredSize(new Dimension(250, 500));
		
		JPanel sub1=new JPanel();
		sub1.add(add1);
		sub1.add(remove1);
		pane1.add(sub1, BorderLayout.NORTH);
		pane1.add(listScroller1, BorderLayout.CENTER);
		
		JPanel pane2=new JPanel();
		pane2.setLayout(new BorderLayout());
		pane2.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		add2=new JButton("Add Sound");
		add2.setActionCommand(ADD2);
		add2.addActionListener(this);
		remove2=new JButton("Remove Sound");
		remove2.setActionCommand(REMOVE2);
		remove2.addActionListener(this);
		listModel2 = new DefaultListModel();
		list2=new JList(listModel2); 
		list2.addListSelectionListener(this);
		list2.setVisibleRowCount(-1);
		listScroller2 = new JScrollPane(list2);
		listScroller2.setPreferredSize(new Dimension(250, 500));
		
		JPanel sub2=new JPanel();
		sub2.add(add2);
		sub2.add(remove2);
		pane2.add(sub2, BorderLayout.NORTH);
		pane2.add(listScroller2, BorderLayout.CENTER);
		
		JPanel pane3=new JPanel();
		pane3.setLayout(new GridLayout(0,1));
		pane3.setBorder(BorderFactory.createEmptyBorder(10,10,200, 10));

		this.add(pane1);
		this.add(pane2);
	}
	
	public void loadScheme(LinkedList t){
		table=new int[ac.archIds.length][ac.archIds.length];
		rightList=new LinkedList();
		leftList=new LinkedList();
		listModel1.removeAllElements();
		listModel2.removeAllElements();
		for (int j=1; j<t.size(); j++){
			int[] labels=(int[])t.get(j);
			Integer p=new Integer(labels[0]);
			boolean found1=false;
			for (int k=0; k<leftList.size(); k++){
				Integer q=(Integer)leftList.get(k);
				if (q.intValue()==labels[0]){found1=true;}
			}
			if (!found1){leftList.add(p);}
			Integer p2=new Integer(labels[1]);
			boolean found2=false;
			for (int k=0; k<rightList.size(); k++){
				Integer q2=(Integer)rightList.get(k);
				if (q2.intValue()==labels[1]){found2=true;}
			}
			if (!found2){rightList.add(p2);}
			int a=0;
			int b=0;
			for (int k=0; k<ac.archIds.length; k++){
				if (ac.archIds[k]==labels[0]){
					if (!found1){listModel1.addElement(ac.archIndNams[k]+" "+ac.archNams[k]);}
					a=k;
				}
				if (ac.archIds[k]==labels[1]){
					if (!found2){listModel2.addElement(ac.archIndNams[k]+" "+ac.archNams[k]);}
					b=k;
				}
			}
			table[b][a]=1;
		}
	}
	
	public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        
        if (ADD1.equals(command)) {
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
							if (selected[i]==ac.archIds[j]){listModel1.addElement(ac.archNams[j]);}
						}

					}
				}
			}
		}
		
        else if (REMOVE1.equals(command)) {
			int index[] = list1.getSelectedIndices();
			for (int i=0; i<index.length; i++){
				listModel1.remove(index[i]-i);
				leftList.remove(index[i]-i);
			}
			removeFromTable(index, true);
		}
		
		else if (ADD2.equals(command)) {
			int selected[]=ac.treePanel.findSelected();
			
			if (selected.length>0){
				int index[] = list1.getSelectedIndices();
				
				if (list1.isSelectionEmpty()){
					int siz=listModel1.getSize();
					index=new int[siz];
					for (int i=0; i<siz; i++){
						index[i]=i;
					}
				}
				for (int i=0; i<index.length; i++){
					Integer q=(Integer)leftList.get(index[i]);
					index[i]=q.intValue();
				}
				selected=addToTable(selected, index);
				for (int i=0; i<selected.length; i++){
					boolean found=false;
					for (int j=0; j<rightList.size(); j++){
						Integer q=(Integer)rightList.get(j);
						if (q.intValue()==selected[i]){found=true;}
					}
					if (!found){
						Integer p=new Integer(selected[i]);
						rightList.add(p);
						for (int j=0; j<ac.archIds.length; j++){
							if (selected[i]==ac.archIds[j]){listModel2.addElement(ac.archNams[j]);}
						}
					}
				}
			
			}
		}
		
		else if (REMOVE2.equals(command)) {
			int index[] = list2.getSelectedIndices();
			for (int i=0; i<index.length; i++){
				listModel2.remove(index[i]-i);
				rightList.remove(index[i]-i);
			}
			removeFromTable(index, false);
		}
	}
	
	public int[]addToTable(int[] first, int[]second){
		
		int[] found=new int[first.length];
		int count=0;
		for (int i=0; i<first.length; i++){
			for (int ii=0; ii<ac.archIds.length; ii++){
				if (ac.archIds[ii]==first[i]){
					for (int j=0; j<second.length; j++){
						if (first[i]!=second[j]){
							for (int jj=0; jj<ac.archIds.length; jj++){
								if (ac.archIds[jj]==second[j]){
									if (table[ii][jj]==0){
										table[ii][jj]=1;
										found[i]=1;
									}
								}
							}
						}
					}
				}
			}
			if (found[i]==1){count++;}
		}
		int [] out=new int [count];
		count=0;
		for (int i=0; i<first.length; i++){
			if (found[i]==1){
				out[count]=first[i];
				count++;
			}
		}
		return out;
	}
	
	public void removeFromTable(int[] data, boolean first){
	
		for (int i=0; i<data.length; i++){
			for (int j=0; j<table.length; j++){
				if (data[i]==ac.archIds[j]){
					for (int k=0; k<table.length; k++){
						if((first)&&(table[k][j]==1)){table[k][j]=0;}
						if((!first)&&(table[j][k]==1)){table[j][k]=0;}
					}
				}
			}
		}
		
		for (int i=0; i<table.length; i++){
			boolean found=false;
			for (int j=0; j<table.length; j++){
				if (table[i][j]==1){found=true;}
			}
			if (!found){
				int id=ac.archIds[i];
				for (int j=0; j<rightList.size(); j++){
					Integer p=(Integer)rightList.get(j);
					if (id==p.intValue()){
						rightList.remove(j);
						listModel2.remove(j);
						j--;
					}
				}
			}
		}
	}
	
	public int[] findIndices(int[]ind, boolean first){
	
		int poss[]=new int[ac.archIds.length];
		int count=0;
		int id=0;
		for (int i=0; i<ind.length; i++){
			if (first){
				Integer q=(Integer)leftList.get(ind[i]);
				id=q.intValue();
			}
			else{
				Integer q=(Integer)rightList.get(ind[i]);
				id=q.intValue();
			}
			for (int j=0; j<ac.archIds.length; j++){
				if (id==ac.archIds[j]){
					for (int k=0; k<table.length; k++){
						if ((first)&&(table[k][j]==1)){poss[k]=1; count++;}
						if ((!first)&&(table[j][k]==1)){poss[k]=1; count++;}
					}
				}
			}
		}
		int[] out=new int[count];
		count=0;
		for (int i=0; i<poss.length; i++){
			if (poss[i]==1){
				if (first){
					for (int j=0; j<rightList.size(); j++){
						Integer q=(Integer)rightList.get(j);
						if (ac.archIds[i]==q.intValue()){
							out[count]=j;
							count++;
						}
					}
				}
				else{
					for (int j=0; j<leftList.size(); j++){
						Integer q=(Integer)leftList.get(j);
						if (ac.archIds[i]==q.intValue()){
							out[count]=j;
							count++;
						}
					}
				}
			}
		}
		return out;				
	}
	
	public void valueChanged(ListSelectionEvent e) {
		list1.removeListSelectionListener(this);
		list2.removeListSelectionListener(this);
		JList lsm = (JList)e.getSource();
		if (!lsm.isSelectionEmpty()) {
			if (lsm==list1){
				int[] ind=list1.getSelectedIndices();
				int[] selectedchoice=findIndices(ind, true);
				list2.setSelectedIndices(selectedchoice);
			}
			else{
				int[] ind=list2.getSelectedIndices();
				int[] selectedchoice=findIndices(ind, false);
				list1.setSelectedIndices(selectedchoice);
			}
		}
		list1.addListSelectionListener(this);
		list2.addListSelectionListener(this);
	}
}
