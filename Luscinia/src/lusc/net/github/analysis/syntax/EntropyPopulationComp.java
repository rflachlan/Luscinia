package lusc.net.github.analysis.syntax;

import java.util.*;

import lusc.net.github.analysis.BasicStatistics;
import lusc.net.github.analysis.AnalysisGroup;
import lusc.net.github.analysis.ComparisonResults;

public class EntropyPopulationComp {

	
	//public EntropyPopulationComp(ComparisonResults cr, int h, int maxSyntaxK){
	public EntropyPopulationComp(ComparisonResults cr, int minSyntaxK, int maxSyntaxK){
		
		
		int[] popId=cr.getPopulationListArray();
		//int[] popId2=sg.getPopulationListArray(4);
		String[] popNames=cr.getPopulationNames();
		int numPops=0;
		for (int i=0; i<popId.length; i++){
			if (popId[i]>numPops){numPops=popId[i];}
		}
		numPops++;
		EntropyAnalysis[] ea=new EntropyAnalysis[numPops];
				
		for (int i=0; i<numPops; i++){
			//System.out.println("Analyzing population: "+popNames[i]);
			
			ComparisonResults crn=cr.splitCompResults(i);
			

			//ea[i]=new EntropyAnalysis(crn, minSyntaxK, maxSyntaxK, 0, false, 50, false);	
		}
		
		//medianComp
		
		int[] meds=new int[numPops];
		
		for (int i=0; i<numPops; i++){
			
			double[] scores=new double[ea[i].mkc.length];
			for (int j=0; j<ea[i].mkc.length; j++){
				scores[j]=ea[i].mkc[j].redundancy[3];
			}
			Arrays.sort(scores);
			
			int ml=ea[i].mkc.length/2;
			
			for (int j=0; j<ea[i].mkc.length; j++){
				double r=ea[i].mkc[j].redundancy[3];
				if (r==scores[ml]){
					meds[i]=j; 
				}
			}
		}
		
		for (int i=0; i<numPops; i++){
			for (int j=0; j<i; j++){
				System.out.println("MEDIAN COMPARISON: "+popNames[i]+" "+popNames[j]+" "+meds[i]+" "+meds[j]);
				for (int k=0; k<7; k++){
					System.out.println(ea[i].mkc[meds[i]].redundancy[k]+" "+ea[j].mkc[meds[j]].redundancy[k]);
				}
				popComp(ea[i].mkc[meds[i]], ea[j].mkc[meds[j]]);
			}
		}
		
		//maxComp
		
		int[] loc=new int[numPops];
		double[] max=new double[numPops];
		for (int i=0; i<numPops; i++){
			loc[i]=-1;
			max[i]=-1;
		}
				
		for (int i=0; i<numPops; i++){
				
			for (int j=0; j<ea[i].mkc.length; j++){
				double r=ea[i].mkc[j].redundancy[3];
				if (r>max[i]){
					max[i]=r; 
					loc[i]=j;
				}
			}
		}
				
		for (int i=0; i<numPops; i++){
			for (int j=0; j<i; j++){
				System.out.println("MAXIMUM COMPARISON: "+popNames[i]+" "+popNames[j]+" "+max[i]+" "+max[j]+" "+loc[i]+" "+loc[j]);
				for (int k=0; k<7; k++){
					System.out.println(ea[i].mkc[loc[i]].redundancy[k]+" "+ea[j].mkc[loc[j]].redundancy[k]);
				}
				popComp(ea[i].mkc[loc[i]], ea[j].mkc[loc[j]]);
			}
		}
				
		//k=6 Comp
				
		for (int i=0; i<numPops; i++){
			for (int j=0; j<i; j++){
				System.out.println("k=6 COMPARISON: "+popNames[i]+" "+popNames[j]+" "+ea[i].mkc[4].redundancy[3]+" "+ea[j].mkc[4].redundancy[3]);
				for (int k=0; k<7; k++){
					System.out.println(ea[i].mkc[4].redundancy[k]+" "+ea[j].mkc[4].redundancy[k]);
				}
				popComp(ea[i].mkc[4], ea[j].mkc[4]);
			}
		}
	
	}
	
	public void popComp(MarkovChain mkc1, MarkovChain mkc2){
		double[] m1=mkc1.bootstrapEntropyNoOrder();
		double[] m2=mkc2.bootstrapEntropyNoOrder();
		int c=0;
		BasicStatistics bs=new BasicStatistics();
		double av1=bs.calculateMean(m1);
		double av2=bs.calculateMean(m2);
		for (int i=0; i<m1.length; i++){
			if (m1[i]>m2[i]){c++;}
		}
		System.out.println(av1+" "+av2+" "+c);
		Arrays.sort(m1);
		Arrays.sort(m2);
		int cutOffs[]={50, 500, 2500, 50000, 97500, 99500, 99950};
		for (int k=0; k<7; k++){
			System.out.println(m1[cutOffs[k]]+" "+m2[cutOffs[k]]);
		}
		
	}
	
	
}
