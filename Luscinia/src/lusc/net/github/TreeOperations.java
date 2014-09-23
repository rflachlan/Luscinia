package lusc.net.github;
//
//  TreeOperations.java
//  Luscinia
//
//  Created by Robert Lachlan on 12/21/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

import java.util.*;

public class TreeOperations {

	TreeDat[] data=null;
	
	public TreeOperations(TreeDat[] data){
		this.data=data;
	}
	
	public LinkedList<TreeDat> convertToLinkedList(TreeDat[] dat){
		LinkedList<TreeDat> r=new LinkedList<TreeDat>();
		
		for (int i=0; i<dat.length; i++){
			r.add(dat[i]);
		}
		return r;
	}
	
	public TreeDat[] pruneN(int n){
	
		boolean[] list=new boolean[data.length];
		for (int i=0; i<list.length; i++){
			list[i]=true;
		}	
		
		int q=data[data.length-1].children;
		
		for (int i=q; i<2*q-n; i++){
			list[data[i].daughters[0]]=false;
			list[data[i].daughters[1]]=false;
		}
		
		TreeDat[] results=new TreeDat[n];
		
		int count=0;
		for (int i=0; i<2*q-n; i++){
			if (list[i]){
				results[count]=data[i];
				count++;
			}
		}
		
		return results;
	}
	
	

}
