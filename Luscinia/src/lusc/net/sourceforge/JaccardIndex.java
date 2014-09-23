package lusc.net.sourceforge;
//
//  JaccardIndex.java
//  Luscinia
//
//  Created by Robert Lachlan on 3/17/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

public class JaccardIndex {
	
	int[][] labels1, labels2;
	
	double[] jaccard;
	
	
	public JaccardIndex (KMedoids km1, KMedoids km2){
		labels1=km1.overallAssignments;
		labels2=km2.overallAssignments;
		
		int k=labels1.length;
		int n=labels1[0].length;
		
		System.out.println(labels1.length+" "+labels2.length+" "+labels1[0].length+" "+labels2[0].length);
		
		
		jaccard=new double[k];
		
		
		for (int i=0; i<k; i++){
			
			int ii=i+2;
			double[][] countTable=new double[ii][ii];
			
			double[] rowCounts=new double[ii];
			double[] columnCounts=new double[ii];
			
			for (int j=0; j<n; j++){
			
				countTable[labels1[i][j]][labels2[i][j]]++;
				rowCounts[labels1[i][j]]++;
				columnCounts[labels2[i][j]]++;
			}
			
			double tot=0;
			for (int a=0; a<ii; a++){
				double jim=0;
				for (int b=0; b<ii; b++){
					
					double c=countTable[a][b]/(rowCounts[a]-countTable[a][b]+columnCounts[b]);
					
					if (c>jim){jim=c;}
				}
				
				tot+=jim*rowCounts[a];
			}
			
			jaccard[i]=tot/(n+0.0);
			
			System.out.println("JACCARD: "+i+" "+jaccard[i]);
			
		}
	}
					
					
					
			

}
