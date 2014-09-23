package lusc.net.sourceforge;
//
//  DatabaseTree.java
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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

public class DatabaseTree extends JPanel {
    protected myNode rootNode;
    protected DefaultTreeModel treeModel;
    protected JTree tree;
    private Toolkit toolkit = Toolkit.getDefaultToolkit();
	private int indc=0;
	private DatabaseView sc;
	myNode[] selnode;
	
    public DatabaseTree(DatabaseView sc, String name) {
        super(new GridLayout(1,0));
		this.sc=sc;
		
        rootNode = new myNode(name);
        treeModel = new DefaultTreeModel(rootNode);
        treeModel.addTreeModelListener(new MyTreeModelListener());

        tree = new JTree(treeModel);
        tree.setEditable(true);
        //tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        tree.setShowsRootHandles(true);
		
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				TreePath[] currentSelection=tree.getSelectionPaths();
				//TreePath currentSelection = tree.getSelectionPath();
				if (currentSelection != null) {
					System.out.println(currentSelection.length);
					selnode=new myNode[currentSelection.length];
					for (int i=0; i<currentSelection.length; i++){
						selnode[i] = (myNode)(currentSelection[i].getLastPathComponent());
					}
				}
									  else{selnode=null;}
				//selnode = (myNode)tree.getLastSelectedPathComponent();
				updateAddButton();
				if (selnode == null) return;
				Object nodeInfo = selnode[0].getUserObject();
			}
		});

        JScrollPane scrollPane = new JScrollPane(tree);
        add(scrollPane);
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
	
	public void updateAddButton(){
		if ((selnode==null)||(selnode[0].getLevel()==0)){
			sc.addIndButton.setEnabled(true);
			sc.addSongButton.setEnabled(false);
			sc.removeButton.setEnabled(false);
			sc.sonogramButton.setEnabled(false);
			sc.hideInformationPanel();
		}
		else{
			if (selnode[0].getLevel()==1){
				sc.addIndButton.setEnabled(false);
				sc.addSongButton.setEnabled(true);
				sc.removeButton.setEnabled(true);
				sc.sonogramButton.setEnabled(false);
				sc.showInformationIndividual();
			}
			else{
				sc.addIndButton.setEnabled(false);
				sc.addSongButton.setEnabled(false);
				sc.removeButton.setEnabled(true);
				sc.sonogramButton.setEnabled(true);
				sc.showInformationSong();
			}
		}
		//sc.informationPanel.removeAll();
		//sc.informationPanel.validate();
		//sc.informationPanel.revalidate();
		sc.repaint();
	}
	
	
    public void removeCurrentNode() {
		TreePath[] currentSelection=tree.getSelectionPaths();
		
        //TreePath currentSelection = tree.getSelectionPath();
        if (currentSelection != null) {
			System.out.println(currentSelection.length);
			myNode[] nodes=new myNode[currentSelection.length];
			for (int i=0; i<currentSelection.length; i++){
				nodes[i] = (myNode)(currentSelection[i].getLastPathComponent());
			}
			for (int i=0; i<currentSelection.length; i++){
				sc.removeFromDataBase(nodes[i]);
				MutableTreeNode parent = (MutableTreeNode)(nodes[i].getParent());
				if (parent != null) {
					treeModel.removeNodeFromParent(nodes[i]);
					//return;
				}
            }
        } 
    }

    public void addAbject() {
        myNode parentNode = null;
        TreePath parentPath = tree.getSelectionPath();
		Object child;
		int a=0;
        if (parentPath == null) {
            parentNode = rootNode;
			child="New Individual"+indc++;
			a=0;
        } else {
			a=parentPath.getPathCount()-1;
			if (a>1){a=1;}
			if (a==0){
				child="New Individual"+indc++;
			}
			else{
				child="New Song"+indc++;
			}
			parentNode = (myNode)(parentPath.getPathComponent(a));
        }

        if (a==0){
			myNode ch=addObject(parentNode, child, true);
			sc.addNewIndividual(ch);
		}
		else{
			//myNode ch=addObject(parentNode, child, true);
			//sc.addNewSong(ch, parentNode);
			//selnode=ch;
			sc.openWav(parentNode, child);
		}
    }

	public myNode addObject(Object child) {
        myNode parentNode = null;
        TreePath parentPath = tree.getSelectionPath();
		int a=0;
        if (parentPath == null) {
            parentNode = rootNode;
			child="_New_Individual"+indc++;
			a=0;
        } else {
			a=parentPath.getPathCount()-1;
			if (a>1){a=1;}
			if (a==0){
				child="_New_Individual"+indc++;
			}
			else{child="New Song"+indc++;}
			parentNode = (myNode)(parentPath.getPathComponent(a));
        }

        return addObject(parentNode, child, true);
    }

    public myNode addObject(myNode parent, Object child, boolean shouldBeVisible) {
        myNode childNode = new myNode(child);
        if (parent == null) {parent = rootNode;}
        treeModel.insertNodeInto(childNode, parent, parent.getChildCount());
        return childNode;
    }

    class MyTreeModelListener implements TreeModelListener {
        public void treeNodesChanged(TreeModelEvent e) {
            myNode node;
            node = (myNode)
                     (e.getTreePath().getLastPathComponent());

            try {
                int index = e.getChildIndices()[0];
                node = (myNode)
                       (node.getChildAt(index));
            } catch (NullPointerException exc) {}
			sc.renameNode(node);
        }
        public void treeNodesInserted(TreeModelEvent e) {
        }
        public void treeNodesRemoved(TreeModelEvent e) {
        }
        public void treeStructureChanged(TreeModelEvent e) {
        }
    }
}





