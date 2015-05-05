package lusc.net.github.ui.db;
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
import javax.swing.tree.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

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
        
        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON_OR_INSERT);
        tree.setTransferHandler(new TreeTransferHandler());
       // tree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
		
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
				//Object nodeInfo = selnode[0].getUserObject();
			}
		});

        JScrollPane scrollPane = new JScrollPane(tree);
        add(scrollPane);
    }
    
    class TreeTransferHandler extends TransferHandler {
        DataFlavor nodesFlavor;
        DataFlavor[] flavors = new DataFlavor[1];
        myNode[] nodesToRemove;

        public TreeTransferHandler() {
            try {
                String mimeType = DataFlavor.javaJVMLocalObjectMimeType +
                                  ";class=\"" +
                    myNode[].class.getName() +
                                  "\"";
                nodesFlavor = new DataFlavor(mimeType);
                flavors[0] = nodesFlavor;
            } catch(ClassNotFoundException e) {
                System.out.println("ClassNotFound: " + e.getMessage());
            }
        }

        public boolean canImport(TransferHandler.TransferSupport support) {
            if(!support.isDrop()) {
                return false;
            }
            support.setShowDropLocation(true);
            if(!support.isDataFlavorSupported(nodesFlavor)) {
                return false;
            }
            // Do not allow a drop on the drag source selections.
            JTree.DropLocation dl =
                    (JTree.DropLocation)support.getDropLocation();
            JTree tree = (JTree)support.getComponent();
            int dropRow = tree.getRowForPath(dl.getPath());
            int[] selRows = tree.getSelectionRows();
            for(int i = 0; i < selRows.length; i++) {
                if(selRows[i] == dropRow) {
                    return false;
                }
            }
            
            TreePath dest = dl.getPath();
            myNode target =
                (myNode)dest.getLastPathComponent();
            //System.out.println("TARGET LEVEL: "+target.getLevel());
            if (target.getLevel()==0){
            	return false;
            }
            
            // Do not allow MOVE-action drops if a non-leaf node is
            // selected unless all of its children are also selected.
            int action = support.getDropAction();
            if(action == MOVE) {
                return haveCompleteNode(tree);
            }
            // Do not allow a non-leaf node to be copied to a level
            // which is less than its source level.
            
            TreePath path = tree.getPathForRow(selRows[0]);
            myNode firstNode =
                (myNode)path.getLastPathComponent();
            if(firstNode.getChildCount() > 0 &&
                   target.getLevel() < firstNode.getLevel()) {
                return false;
            }
            return true;
        }

        private boolean haveCompleteNode(JTree tree) {
            int[] selRows = tree.getSelectionRows();
            TreePath path = tree.getPathForRow(selRows[0]);
            myNode first =
                (myNode)path.getLastPathComponent();
            int childCount = first.getChildCount();
            // first has children and no children are selected.
            if(childCount > 0 && selRows.length == 1)
                return false;
            // first may have children.
            for(int i = 1; i < selRows.length; i++) {
                path = tree.getPathForRow(selRows[i]);
                myNode next =
                    (myNode)path.getLastPathComponent();
                if(first.isNodeChild(next)) {
                    // Found a child of first.
                    if(childCount > selRows.length-1) {
                        // Not all children of first are selected.
                        return false;
                    }
                }
            }
            return true;
        }

        protected Transferable createTransferable(JComponent c) {
            JTree tree = (JTree)c;
            TreePath[] paths = tree.getSelectionPaths();
            if(paths != null) {
                // Make up a node array of copies for transfer and
                // another for/of the nodes that will be removed in
                // exportDone after a successful drop.
                List<myNode> copies =
                    new ArrayList<myNode>();
                List<myNode> toRemove =
                    new ArrayList<myNode>();
                myNode node =
                    (myNode)paths[0].getLastPathComponent();
                myNode copy = copy(node);
                copies.add(copy);
                toRemove.add(node);
                for(int i = 1; i < paths.length; i++) {
                   myNode next =
                        (myNode)paths[i].getLastPathComponent();
                    // Do not allow higher level nodes to be added to list.
                    if(next.getLevel() < node.getLevel()) {
                        break;
                    } else if(next.getLevel() > node.getLevel()) {  // child node
                        copy.add(copy(next));
                        // node already contains child
                    } else {                                        // sibling
                        copies.add(copy(next));
                        toRemove.add(next);
                    }
                }
               myNode[] nodes =
                    copies.toArray(new myNode[copies.size()]);
                nodesToRemove =
                    toRemove.toArray(new myNode[toRemove.size()]);
                return new NodesTransferable(nodes);
            }
            return null;
        }

        /** Defensive copy used in createTransferable. */
        private myNode copy(myNode node) {
            return node.clone();
        }

        protected void exportDone(JComponent source, Transferable data, int action) {
            if((action & MOVE) == MOVE) {
                JTree tree = (JTree)source;
                DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
                // Remove nodes saved in nodesToRemove in createTransferable.
                for(int i = 0; i < nodesToRemove.length; i++) {
                    model.removeNodeFromParent(nodesToRemove[i]);
                }
            }
        }

        public int getSourceActions(JComponent c) {
            return COPY_OR_MOVE;
        }

        public boolean importData(TransferHandler.TransferSupport support) {
            if(!canImport(support)) {
                return false;
            }
            // Extract transfer data.
            myNode[] nodes = null;
            try {
                Transferable t = support.getTransferable();
                nodes = (myNode[])t.getTransferData(nodesFlavor);
            } catch(UnsupportedFlavorException ufe) {
                System.out.println("UnsupportedFlavor: " + ufe.getMessage());
            } catch(java.io.IOException ioe) {
                System.out.println("I/O error: " + ioe.getMessage());
            }
            // Get drop location info.
            JTree.DropLocation dl =
                    (JTree.DropLocation)support.getDropLocation();
            int childIndex = dl.getChildIndex();
            //myNode destNode = (myNode)(dl.getPath().getLastPathComponent());
            
            //System.out.println(childIndex+" "+node.dex+" "+node.individual);
            
            //for (int i=0; i<nodes.length; i++){
            	//System.out.println("MOVE: "+nodes[i].dex+" "+nodes[i].individual);
            //}
            
            
            
            
            TreePath dest = dl.getPath();
            myNode parent =
                (myNode)dest.getLastPathComponent();
            
            
            sc.updateIndividualAllocation(parent, nodes);
            
            JTree tree = (JTree)support.getComponent();
            DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
            // Configure for drop mode.
            int index = childIndex;    // DropMode.INSERT
            if(childIndex == -1) {     // DropMode.ON
                index = parent.getChildCount();
            }
            // Add data to model.
            for(int i = 0; i < nodes.length; i++) {
                model.insertNodeInto(nodes[i], parent, index++);
            }
            return true;
        }

        public String toString() {
            return getClass().getName();
        }

        public class NodesTransferable implements Transferable {
            myNode[] nodes;

            public NodesTransferable(myNode[] nodes) {
                this.nodes = nodes;
             }

            public Object getTransferData(DataFlavor flavor)
                                     throws UnsupportedFlavorException {
                if(!isDataFlavorSupported(flavor))
                    throw new UnsupportedFlavorException(flavor);
                return nodes;
            }

            public DataFlavor[] getTransferDataFlavors() {
                return flavors;
            }

            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return nodesFlavor.equals(flavor);
            }
        }
    }
	
    public myNode[] getSelNode(){
    	return selnode;
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
	
	public void updateAddButton(){
		if ((selnode==null)||(selnode[0].getLevel()==0)){
			sc.addIndButton.setEnabled(true);
			sc.addSongButton.setEnabled(false);
			sc.addRecordingButton.setEnabled(false);
			sc.removeButton.setEnabled(false);
			sc.sonogramButton.setEnabled(false);
			sc.hideInformationPanel();
		}
		else{
			if (selnode[0].getLevel()==1){
				sc.addIndButton.setEnabled(false);
				sc.addSongButton.setEnabled(true);
				sc.addRecordingButton.setEnabled(true);
				sc.removeButton.setEnabled(true);
				sc.sonogramButton.setEnabled(false);
				sc.showInformationIndividual();
			}
			else{
				sc.addIndButton.setEnabled(false);
				sc.addSongButton.setEnabled(false);
				sc.addRecordingButton.setEnabled(false);
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

    public void addAbject(int x) {
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
			System.out.println("x: "+x);
			if (x==0){
				sc.openWav(parentNode, child);
			}
			else{
				System.out.println("here");
				sc.openRec(parentNode, child);
			}
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





