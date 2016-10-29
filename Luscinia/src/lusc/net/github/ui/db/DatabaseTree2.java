package lusc.net.github.ui.db;
//
//  DatabaseTree2.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/10/2005.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.


import java.awt.GridLayout;
import java.awt.Toolkit;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import lusc.net.github.db.DataBaseController;
import lusc.net.github.ui.AnalysisChoose;

public class DatabaseTree2 extends JPanel {
    protected myNode rootNode;
    protected DefaultTreeModel treeModel;
    protected JTree tree;
    private Toolkit toolkit = Toolkit.getDefaultToolkit();
	private int indc=0;
	myNode selnode;
	AnalysisChoose ac;
	JScrollPane scrollPane;
	
    public DatabaseTree2(AnalysisChoose ac, DataBaseController dbc) {
        super(new GridLayout(1,0));
		this.ac=ac;
        rootNode = new myNode(dbc.getDBName());
        treeModel = new DefaultTreeModel(rootNode);

        tree = new JTree(treeModel);
        tree.setEditable(false);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        tree.setShowsRootHandles(true);

        scrollPane = new JScrollPane(tree);
        add(scrollPane);
    }
    
    public myNode getRootNode(){
    	return rootNode;
    }
	
		
	public void expandNode() {
		int row = 0;
		while (row < tree.getRowCount()) {
			tree.expandRow(row);
			row++;
		}
    }
	
	public void collapseNode(){
		int row=tree.getRowCount() - 1;
		while (row>=0){
			tree.collapseRow(row);
			row--;
		}
	}
	
	public int[] findSelected(){
		TreePath[] tp=tree.getSelectionPaths();
		int [] t=new int[tp.length];
		int j=0;
		
		for (int i=0; i<tp.length; i++){
			myNode mn=(myNode)tp[i].getLastPathComponent();
			int k=mn.getLevel();
			if (k==2){t[i]=mn.dex; j++;}
			else{t[i]=-1;}
		}
		int []dexs=new int[j];
		j=0;
		for (int i=0; i<tp.length; i++){
			if (t[i]!=-1){
				dexs[j]=t[i];
				j++;
			}
		}
		return dexs;
	}
	
    public myNode addObject(myNode parent, Object child, boolean shouldBeVisible) {
        myNode childNode = new myNode(child);
        if (parent == null) {parent = rootNode;}
        treeModel.insertNodeInto(childNode, parent, parent.getChildCount());
        //if (shouldBeVisible) {tree.scrollPathToVisible(new TreePath(childNode.getPath()));}
        return childNode;
    }
    
    
    public void removeAllNodes() {
    	rootNode.removeAllChildren();
    }

	
}





