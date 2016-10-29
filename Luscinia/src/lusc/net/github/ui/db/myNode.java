package lusc.net.github.ui.db;
//
//  myNode.java
//  Luscinia
//
//  Created by Robert Lachlan on 01/10/2005.
//  Copyright 2005 Robert Lachlan. All rights reserved.
//	This program is provided under the GPL 2.0 software licence. Please see accompanying material for details.

import javax.swing.tree.DefaultMutableTreeNode;

public class myNode extends DefaultMutableTreeNode{
	
	int dex=0;
	boolean individual=true;
	boolean isMeasured=false;
	
	public myNode(Object name){
		this.userObject=name;
	}
	
	public myNode(myNode node){
		this.userObject=node.userObject;
		this.individual=node.individual;
		this.dex=node.dex;
	}
	
	public int getDex(){
		return dex;
	}
	
	public void setDex(int a){
		dex=a;
	}
	
	public myNode clone(){
		myNode node=new myNode(this);
		return node;
	}
	
}
