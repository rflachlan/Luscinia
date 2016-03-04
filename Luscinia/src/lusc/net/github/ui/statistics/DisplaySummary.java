package lusc.net.github.ui.statistics;
//
//  DisplaySummary.java
//  Luscinia
//
//  Created by Robert Lachlan on 9/12/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//


import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.text.*;

import lusc.net.github.Defaults;
import lusc.net.github.analysis.DistanceNeighborFunctions;
import lusc.net.github.analysis.clustering.KMedoids;
import lusc.net.github.analysis.clustering.SNNDensity;
import lusc.net.github.analysis.multivariate.CalculateHopkinsStatistic;
import lusc.net.github.analysis.multivariate.MRPP;
import lusc.net.github.analysis.multivariate.MultivariateDispersionTest;
import lusc.net.github.analysis.syntax.EntropyAnalysis;
import lusc.net.github.analysis.syntax.MarkovChain;
import lusc.net.github.analysis.syntax.SWMLEntropyEstimate;
import lusc.net.github.ui.SaveDocument;


public class DisplaySummary  extends DisplayPane {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5452616185703704872L;
	Dimension dim=new Dimension(600, 400);
	JPanel messagePanel=new JPanel();
	Font font=new Font("Sans-Serif", Font.PLAIN, 9);
	
	JTextArea texter=new JTextArea();
	
	String returner=System.getProperty("line.separator");
	String[] levels={"Elements", "Elements", "Syllables", "Transitions", "Songs", "Individuals"};
	String tabc="\u0009";
	
	LinkedList<KMedoids> cluster=new LinkedList<KMedoids>();
	LinkedList<SNNDensity> snncluster=new LinkedList<SNNDensity>();
	LinkedList<EntropyAnalysis> syntax=new LinkedList<EntropyAnalysis>();
	LinkedList<CalculateHopkinsStatistic> hopkins=new LinkedList<CalculateHopkinsStatistic>();
	LinkedList<String> miscellaneous=new LinkedList<String>();
	LinkedList<MultivariateDispersionTest> anderson=new LinkedList<MultivariateDispersionTest>();
	LinkedList<DistanceNeighborFunctions> distfunc=new LinkedList<DistanceNeighborFunctions>();
	LinkedList<MRPP> mrppl=new LinkedList<MRPP>();
	
	Defaults defaults;
	
	public DisplaySummary (Defaults defaults){
		this.defaults=defaults;
		texter.setTabSize(texter.getTabSize()-3);
		texter.setFont(font);
		texter.setLineWrap(true);
		JScrollPane scroller=new JScrollPane(texter);
		scroller.setPreferredSize(dim);
		//messagePanel.setSize(dim);
		//messagePanel.setLayout(new GridLayout(0,1));
		
		
		this.add(scroller);
	}
	
	public void addDouble(String s, double[] data){
		String sp=" ";
		StringBuffer sr=new StringBuffer();
		sr.append(s);
		sr.append(sp);
		for (int i=0; i<data.length; i++){
			Double j=new Double(data[i]);
			sr.append(j.toString());
			sr.append(sp);
		}
		String st=sr.toString();
		System.out.println(st);
		miscellaneous.add(st);
		texter.append(st);
		texter.append(returner);
	}
	
	public void addString(String s){
		System.out.println(s);
		miscellaneous.add(s);
		texter.append(s);
		texter.append(returner);
	}
	
	public void addHopkins(CalculateHopkinsStatistic chs){
		hopkins.add(chs);
		texter.append("Hopkins statistics for "+levels[chs.getType()]);
		texter.append(returner);
		String resultString=chs.getResultString();
		texter.append(resultString);
		texter.append(returner);
		//for (int i=0; i<resultString.length; i++){
			//texter.append(resultString[i]);
			//texter.append(returner);
		//}
		
	}
	
	public void addDistFunc(DistanceNeighborFunctions dnf){
		DecimalFormat df = new DecimalFormat("#.####");
		distfunc.add(dnf);
		int type=dnf.getType();
		texter.append("NN Dists for "+levels[type]);
		texter.append(returner);
		double[] meanNN=dnf.getMeanNN();
		double[] densThresh=dnf.getDensityThresholds();
		texter.append("k"+tabc+"Nearest Neighbour Distance");
		texter.append(returner);
		for (int i=0; i<meanNN.length; i++){
			texter.append((i+1)+tabc+meanNN[i]);
			texter.append(returner);
		}
		texter.append(returner);
		texter.append("Densities for "+levels[type]);
		texter.append(returner);
		texter.append("Threshold"+tabc+"Rel. Thresh."+tabc+"Density");
		texter.append(returner);
		double[] meanDens=dnf.getMeanDens();
		for (int i=0; i<meanDens.length; i++){
			texter.append(densThresh[i]+tabc+df.format(1/(i+1.0))+tabc+meanDens[i]+tabc);
			texter.append(returner);
		}
		texter.append(returner);
	}
	
	public void addAnderson(MultivariateDispersionTest mdt){
		DecimalFormat df = new DecimalFormat("#.####");
		anderson.add(mdt);
		int type=mdt.getType();
		texter.append("Multivariate Dispersion Test for "+levels[type]+"  (Anderson 1996)");
		texter.append(returner);
		texter.append(tabc);
		String[] popNames=mdt.getNames();
		double[][] meanScores=mdt.getMeanScores();
		
		for (int i=0; i<popNames.length; i++){
			texter.append(popNames[i]+tabc);
		}
		texter.append(returner);
		
		for (int i=0; i<meanScores.length; i++){
			texter.append(popNames[i]+tabc);
			for (int j=0; j<meanScores[i].length; j++){
				texter.append(df.format(meanScores[i][j])+tabc);
			}
			texter.append(returner);
		}
		texter.append(returner);
		texter.append(tabc);
		for (int i=0; i<meanScores.length; i++){
			texter.append(popNames[i]+tabc);
		}
		texter.append(returner);
		
		double[][] spatialMedianComp=mdt.getSpatialMedianComp();
		
		for (int i=0; i<spatialMedianComp.length; i++){
			texter.append(popNames[i]+tabc);
			for (int j=0; j<spatialMedianComp[i].length; j++){
				texter.append(df.format(spatialMedianComp[i][j])+tabc);
			}
			texter.append(returner);
		}
		
		double testFScore=mdt.getTestFScore();
		double pValue=mdt.getPValue();
		
		texter.append(returner);
		texter.append("F statistic:"+tabc);
		texter.append(df.format(testFScore));
		texter.append(returner);
		texter.append("p:"+tabc);
		texter.append(df.format(pValue));	
		texter.append(returner);
	}
	
	public void addSyntax(EntropyAnalysis ent){
		
		int type=ent.getType();
		int mode=ent.getMode();
		
		DecimalFormat df = new DecimalFormat("#.####");
		DecimalFormat df2 = new DecimalFormat("#.######");
		syntax.add(ent);
		if (type<2){
			texter.append("Syntax statistics for elements");
		}
		else if (type==2){
			texter.append("Syntax statistics for syllables");
		}
		texter.append(returner);
		if (mode>0){
			texter.append("Match length entropy estimates: ");
			texter.append(returner);
			texter.append("(Jackknife Confidence Intervals)");
			texter.append(returner);
			texter.append(returner);
			String[] s={"k","overall", "0.0005", "0.005", "0.025", "0.5", "0.975", "0.995", "0.9995", "sd"};
			for (int i=0; i<s.length; i++){
				texter.append(s[i]+tabc);
			}
			texter.append(returner);
			SWMLEntropyEstimate[] swml=ent.getSWMLEntropyEstimate();
			for (int i=0; i<swml.length; i++){
				texter.append(swml[i].getKv()+tabc);
				texter.append(df.format(swml[i].getRho())+tabc);
				double[] jackknifeScores=swml[i].getJackknifeScores();
				for (int j=0; j<jackknifeScores.length; j++){
					texter.append(df.format(jackknifeScores[j])+tabc);
				}
				double jackknifeSD=swml[i].getJackknifeSD();
				texter.append(df2.format(jackknifeSD)+tabc);
				texter.append(returner);
			}	
			
			texter.append(returner);				  
		}
		if (mode!=1){
			texter.append("Markov chain redundancy estimates: ");
			texter.append(returner);
			String[] s={"k","0.0005", "0.005", "0.025", "0.5", "0.975", "0.995", "0.9995"};
			for (int i=0; i<s.length; i++){
				texter.append(s[i]+tabc);
			}
			texter.append(returner);
			MarkovChain[] mkc=ent.getMarkovChains();
			for (int i=0; i<mkc.length; i++){
				texter.append((i+2)+tabc);
				double[] redundancy=mkc[i].getRedundancy();
				for (int j=0; j<redundancy.length; j++){
					texter.append(df.format(redundancy[j])+tabc);
				}
				texter.append(returner);
			}
			texter.append("Markov chain entropy estimates: ");
			texter.append(returner);
			for (int i=0; i<s.length; i++){
				texter.append(s[i]+tabc);
			}
			texter.append(returner);
			for (int i=0; i<mkc.length; i++){
				texter.append((i+2)+tabc);
				double[] entropy=mkc[i].getEntropy();
				for (int j=0; j<entropy.length; j++){
					texter.append(df.format(entropy[j])+tabc);
				}
				texter.append(returner);
			}
			texter.append("Markov chain Zero-Order entropy estimates: ");
			texter.append(returner);
			for (int i=0; i<s.length; i++){
				texter.append(s[i]+tabc);
			}
			texter.append(returner);
			for (int i=0; i<mkc.length; i++){
				texter.append((i+2)+tabc);
				double[] zeroOrder=mkc[i].getZeroOrder();
				for (int j=0; j<zeroOrder.length; j++){
					texter.append(df.format(zeroOrder[j])+tabc);
				}
				texter.append(returner);
			}
			texter.append("Positional entropy estimates: ");
			texter.append(returner);
			for (int i=0; i<s.length; i++){
				texter.append(s[i]+tabc);
			}
			texter.append(returner);
			for (int i=0; i<mkc.length; i++){
				texter.append((i+2)+tabc);
				double[] resultArrayP=mkc[i].getResultArrayP();
				for (int j=0; j<resultArrayP.length; j++){
					texter.append(df.format(resultArrayP[j])+tabc);
				}
				texter.append(returner);
			}
			
		}
	}
	
	public void addCluster(KMedoids km){
		cluster.add(km);
		int type=km.getType();
		texter.append("K-medoids statistics for "+levels[type]);
		texter.append(returner);
		texter.append("k Global Silhouette index");
		texter.append(returner);
		double[] globalSilhouette=km.getGlobalSilhouette();
		double[] simulatedSilhouette=km.getSimulatedSilhouette();
		for (int i=0; i<globalSilhouette.length; i++){
			texter.append((i+km.getMinK())+" "+globalSilhouette[i]+" "+simulatedSilhouette[i]);
			texter.append(returner);
		}
	}
	
	public void addSNNCluster(SNNDensity snn){
		snncluster.add(snn);
		int type=snn.getType();
		texter.append("SNN summary for "+levels[type]);
		texter.append(returner);
		texter.append("number of clusters: "+snn.getNumClusts());
		texter.append(returner);
		
	}
	
	public void addMRPP(MRPP mrpp){
		mrppl.add(mrpp);
		int type=mrpp.getType();
		texter.append("MRPP summary for "+levels[type]);
		texter.append(returner);
		texter.append("Empirical delta: "+mrpp.getEmpiricalDelta()+" Expected delta: "+mrpp.getExpectedDelta());
		texter.append(returner);
		texter.append("p-value: "+mrpp.getPValue()+" a-value: "+mrpp.getAValue());
		texter.append(returner);
		
		if (mrpp.getPairwise()){
			String[] levelNames=mrpp.getLevelNames();
			
			double[][] pv=mrpp.getPairwisePValue();
			texter.append("Pairwise p-values: ");
			texter.append(returner);
			
			texter.append("        ");
			for (int i=0; i<levelNames.length; i++){
				texter.append(levelNames[i]+" ");
			}
			texter.append(returner);
			for (int i=0; i<levelNames.length; i++){
				texter.append(levelNames[i]+" ");	
				for (int j=0; j<levelNames.length; j++){
					texter.append(pv[i][j]+" ");
				}
				texter.append(returner);
			}
			double[][] av=mrpp.getPairwiseAValue();
			texter.append("Pairwise a-values: ");
			texter.append(returner);
			
			texter.append("        ");
			for (int i=0; i<levelNames.length; i++){
				texter.append(levelNames[i]+" ");
			}
			texter.append(returner);
			for (int i=0; i<levelNames.length; i++){
				texter.append(levelNames[i]+" ");	
				for (int j=0; j<levelNames.length; j++){
					texter.append(av[i][j]+" ");
				}
				texter.append(returner);
			}	
		}		
	}
	
	
	public void export(){
		SaveDocument sd=new SaveDocument(this, defaults);
		boolean readyToWrite=sd.makeFile();
		if (readyToWrite){
			for (int i=0; i<miscellaneous.size(); i++){
				String s=(String)miscellaneous.get(i);
				sd.writeString(s);
				sd.writeLine();
			}
			if (hopkins.size()>0){
				sd.writeSheet("Hopkins statistics");
				for (int i=0; i<hopkins.size(); i++){
					CalculateHopkinsStatistic chs=(CalculateHopkinsStatistic)hopkins.get(i);
					int type=chs.getType();
					sd.writeString("Hopkins statistics for "+levels[type]);
					sd.writeLine();
					
					sd.writeLine();
					sd.writeString("nth nearest neighbour");
					sd.writeString("mean");
					sd.writeString("sd");
					sd.writeString("upper 2.5%ile");
					sd.writeString("lower 2.5%ile");
					sd.writeLine();
					int picks=chs.getPicks();
					double[] results=chs.getResults();
					sd.writeInt(picks);
					for (int k=0; k<results.length; k++){
						sd.writeDouble(results[k]);
					}
					sd.writeLine();
				}
			}
			if(distfunc.size()>0){
				sd.writeSheet("Dist Funcs");
				for (int i=0; i<distfunc.size(); i++){
					DistanceNeighborFunctions dnf=(DistanceNeighborFunctions)distfunc.get(i);
					int type=dnf.getType();
					sd.writeString("Densities and NN Dists for "+levels[type]);
					sd.writeLine();
					int n=dnf.getN();
					int nBins=dnf.getNBins();
					int dBins=dnf.getDBins();
					double[][] nNeighbors=dnf.getNNeighbors();
					double[][] densities=dnf.getDensities();
					for (int j=0; j<n; j++){
						for (int k=0; k<nBins; k++){
							sd.writeDouble(nNeighbors[j][k]);
						}
						for (int k=0; k<dBins; k++){
							sd.writeDouble(densities[j][k]);
						}
						sd.writeLine();
					}
				}
			}
			
			
			if (anderson.size()>0){
				sd.writeSheet("Multivariate Dispersion");
				for (int i=0; i<anderson.size(); i++){
					MultivariateDispersionTest mdt=(MultivariateDispersionTest)anderson.get(i);
					int type=mdt.getType();
					sd.writeString("Multivariate Dispersion Test for "+levels[type]+"  (Anderson 1996)");
					sd.writeLine();
					sd.writeString(" ");
					double[][] meanScores=mdt.getMeanScores();
					String[] popNames=mdt.getNames();
					
					for (int j=0; j<meanScores.length; j++){
						sd.writeString(popNames[j]);
					}
					sd.writeLine();
					
					for (int j=0; j<meanScores.length; j++){
						sd.writeString(popNames[j]);
						for (int k=0; k<meanScores[j].length; k++){
							sd.writeDouble(meanScores[j][k]);
						}
						sd.writeLine();
					}
					sd.writeLine();
					
					sd.writeString(" ");
					double[][] spatialMedianComp=mdt.getSpatialMedianComp();
					
					for (int j=0; j<spatialMedianComp.length; j++){
						sd.writeString(popNames[j]);
					}
					sd.writeLine();
					
					for (int j=0; j<spatialMedianComp.length; j++){
						sd.writeString(popNames[j]);
						for (int k=0; k<spatialMedianComp[j].length; k++){
							sd.writeDouble(spatialMedianComp[j][k]);
						}
						sd.writeLine();
					}
					sd.writeLine();
					
					sd.writeString("F statistic:");
					sd.writeDouble(mdt.getTestFScore());
					sd.writeLine();
					sd.writeString("p:");
					sd.writeDouble(mdt.getPValue());
					sd.writeLine();
					sd.writeLine();
					int maxPerPop=0;
					/*
					double[][][] indScore2=mdt.getIndScore2();
					
					for (int j=0; j<indScore2.length; j++){
						for (int jj=0; jj<indScore2[j].length; jj++){
							if(indScore2[j][jj].length>maxPerPop){maxPerPop=indScore2[j][jj].length;}
						}
					}
					for (int j=0; j<maxPerPop; j++){
						for (int k=0; k<indScore2.length; k++){
							for (int kk=0; kk<indScore2[k].length; kk++){
								if (indScore2[k][kk].length>j){
									sd.writeDouble(indScore2[k][kk][j]);
								}
								else{
									sd.writeString(" ");
								}
							}
						}
						sd.writeLine();
					}
					maxPerPop=0;
					*/
					sd.writeLine();
					double[][][] groupScore=mdt.getGroupScore();
					for (int j=0; j<groupScore.length; j++){
						for (int jj=0; jj<groupScore[j].length; jj++){
							if(groupScore[j][jj].length>maxPerPop){maxPerPop=groupScore[j][jj].length;}
						}
					}
					for (int j=0; j<maxPerPop; j++){
						for (int k=0; k<groupScore.length; k++){
							for (int kk=0; kk<groupScore[k].length; kk++){
								if (groupScore[k][kk].length>j){
									sd.writeDouble(groupScore[k][kk][j]);
								}
								else{
									sd.writeString(" ");
								}
							}
						}
						sd.writeLine();
					}
					
					
				}
			}
			
			if (cluster.size()>0){
				sd.writeSheet("Cluster statistics");
				for (int i=0; i<cluster.size(); i++){
					KMedoids km=(KMedoids)cluster.get(i);
					int type=km.getType();
					sd.writeString("K-medoids statistics for "+levels[type]);
					sd.writeLine();
					sd.writeString("k");
					sd.writeString("Global Silhouette Index");
					sd.writeLine();
					double[] globalSilhouette=km.getGlobalSilhouette();
					double[] simulatedSilhouette=km.getSimulatedSilhouette();
					for (int j=0; j<globalSilhouette.length; j++){
						sd.writeInt(j+2);
						sd.writeDouble(globalSilhouette[j]);
						sd.writeDouble(simulatedSilhouette[j]);
						sd.writeLine();
					}
				}
			}
			if (syntax.size()>0){
				sd.writeSheet("Syntax statistics");
				for (int i=0; i<syntax.size(); i++){
					EntropyAnalysis ent=(EntropyAnalysis)syntax.get(i);
					int type=ent.getType();
					int mode=ent.getMode();
					if (type<2){
						sd.writeString("Syntax statistics for elements");
					}
					else if (type==2){
						sd.writeString("Syntax statistics for syllables");
					}
					sd.writeLine();
					
					if (mode>0){
						sd.writeString("Match length entropy estimates: ");
						sd.writeLine();
						
						sd.writeString("(Jacknife Confidence Intervals)");
						sd.writeLine();
						sd.writeLine();
						String[] s={"k","overall","0.0005", "0.005", "0.025", "0.5", "0.975", "0.995", "0.9995", "sd"};
						for (int j=0; j<s.length; j++){
							sd.writeString(s[j]);
						}
						sd.writeLine();
						SWMLEntropyEstimate[] swml=ent.getSWMLEntropyEstimate();
						for (int j=0; j<swml.length; j++){
							sd.writeInt(swml[j].getKv());
							sd.writeDouble(swml[j].getRho());
							double[] jackknifeScores=swml[j].getJackknifeScores();
							for (int k=0; k<jackknifeScores.length; k++){
								sd.writeDouble(jackknifeScores[k]);
							}
							sd.writeDouble(swml[j].getJackknifeSD());
							sd.writeLine();
						}	
						
					}
					if (mode!=1){
						String[] s={"k","0.005", "0.005", "0.025", "0.5", "0.975", "0.995", "0.9995"};
						
						sd.writeString("Markov chain redundancy estimates: ");
						sd.writeLine();
						
						for (int j=0; j<s.length; j++){
							sd.writeString(s[j]);
						}
						sd.writeLine();
						MarkovChain[] mkc=ent.getMarkovChains();
						for (int j=0; j<mkc.length; j++){
							sd.writeInt(j+2);
							double[] redundancy=mkc[j].getRedundancy();
							for (int k=0; k<redundancy.length; k++){
								sd.writeDouble(redundancy[k]);
							}
							sd.writeLine();
						}
						
						sd.writeString("Markov chain entropy estimates: ");
						sd.writeLine();
						
						for (int j=0; j<s.length; j++){
							sd.writeString(s[j]);
						}
						sd.writeLine();
						
						for (int j=0; j<mkc.length; j++){
							sd.writeInt(j+2);
							double[] entropy=mkc[j].getEntropy();
							for (int k=0; k<entropy.length; k++){
								sd.writeDouble(entropy[k]);
							}
							sd.writeLine();
						}
						
						sd.writeString("Markov chain Zero Order estimates: ");
						sd.writeLine();
						
						for (int j=0; j<s.length; j++){
							sd.writeString(s[j]);
						}
						sd.writeLine();
						
						for (int j=0; j<mkc.length; j++){
							sd.writeInt(j+2);
							double[] zeroOrder=mkc[j].getZeroOrder();
							for (int k=0; k<zeroOrder.length; k++){
								sd.writeDouble(zeroOrder[k]);
							}
							sd.writeLine();
						}
						
						sd.writeString("Positional entropy estimates: ");
						sd.writeLine();
						for (int j=0; j<s.length; j++){
							sd.writeString(s[j]);
						}
						sd.writeLine();
						
						for (int j=0; j<mkc.length; j++){
							sd.writeInt(j+2);
							double[] resultArrayP=mkc[j].getResultArrayP();
							for (int k=0; k<resultArrayP.length; k++){
								sd.writeDouble(resultArrayP[k]);
							}
							sd.writeLine();
						}
					}
				}
			}
			if (mrppl.size()>0){
				sd.writeSheet("MRPP statistics");
				for (int i=0; i<mrppl.size(); i++){
					MRPP mrpp=mrppl.get(i);
					int type=mrpp.getType();
					
					sd.writeString("MRPP summary for "+levels[type]);
					sd.writeLine();
					sd.writeString("Empirical delta:");
					sd.writeDouble(mrpp.getEmpiricalDelta());
					sd.writeLine();
					sd.writeString("Expected delta:");
					sd.writeDouble(mrpp.getExpectedDelta());
					sd.writeLine();
					sd.writeString("p-value: ");
					sd.writeDouble(mrpp.getPValue());
					sd.writeLine();
					sd.writeString("a-value: ");
					sd.writeDouble(mrpp.getAValue());
					sd.writeLine();
					
					if (mrpp.getPairwise()){
						sd.writeLine();
						String[] levelNames=mrpp.getLevelNames();
						
						double[][] pv=mrpp.getPairwisePValue();
						sd.writeString("Pairwise p-values: ");
						sd.writeLine();
						
						sd.writeString(" ");
						for (int ii=0; ii<levelNames.length; ii++){
							sd.writeString(levelNames[ii]+" ");
						}
						sd.writeLine();
						for (int ii=0; ii<levelNames.length; ii++){
							sd.writeString(levelNames[ii]);	
							for (int j=0; j<levelNames.length; j++){
								sd.writeDouble(pv[ii][j]);
							}
							sd.writeLine();
						}
						sd.writeLine();
						
						double[][] av=mrpp.getPairwiseAValue();
						sd.writeString("Pairwise a-values: ");
						sd.writeLine();
						
						sd.writeString(" ");
						for (int ii=0; ii<levelNames.length; ii++){
							sd.writeString(levelNames[ii]);
						}
						sd.writeLine();
						for (int ii=0; ii<levelNames.length; ii++){
							sd.writeString(levelNames[ii]);	
							for (int j=0; j<levelNames.length; j++){
								sd.writeDouble(av[ii][j]);
							}
							sd.writeLine();
						}	
						sd.writeLine();
						
						double[][] expec=mrpp.getPairwiseExpectedDelta();
						sd.writeString("Pairwise expected delta: ");
						sd.writeLine();
						
						sd.writeString(" ");
						for (int ii=0; ii<levelNames.length; ii++){
							sd.writeString(levelNames[ii]);
						}
						sd.writeLine();
						for (int ii=0; ii<levelNames.length; ii++){
							sd.writeString(levelNames[ii]);	
							for (int j=0; j<levelNames.length; j++){
								sd.writeDouble(expec[ii][j]);
							}
							sd.writeLine();
						}	
						sd.writeLine();
						
						double[][] empir=mrpp.getPairwiseEmpiricalDelta();
						sd.writeString("Pairwise empirical delta: ");
						sd.writeLine();
						
						sd.writeString(" ");
						for (int ii=0; ii<levelNames.length; ii++){
							sd.writeString(levelNames[ii]);
						}
						sd.writeLine();
						for (int ii=0; ii<levelNames.length; ii++){
							sd.writeString(levelNames[ii]);	
							for (int j=0; j<levelNames.length; j++){
								sd.writeDouble(empir[ii][j]);
							}
							sd.writeLine();
						}	
						sd.writeLine();
					}	
					
				}
			}
		}
		sd.finishWriting();
		
	}
	
	public void saveImage(){
		JOptionPane.showMessageDialog(this, "Sorry, no image to be saved from this tab");
		
	}
}
