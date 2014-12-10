package lusc.net.github.ui.statistics;
//
//  DisplaySummary.java
//  Luscinia
//
//  Created by Robert Lachlan on 9/12/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//


import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.text.*;

import lusc.net.github.Defaults;
import lusc.net.github.analysis.DistanceNeighborFunctions;
import lusc.net.github.analysis.clustering.KMedoids;
import lusc.net.github.analysis.clustering.SNNDensity;
import lusc.net.github.analysis.multivariate.CalculateHopkinsStatistic;
import lusc.net.github.analysis.multivariate.MultivariateDispersionTest;
import lusc.net.github.analysis.syntax.EntropyAnalysis;
import lusc.net.github.analysis.syntax.MarkovChain;
import lusc.net.github.analysis.syntax.SWMLEntropyEstimate;
import lusc.net.github.ui.SaveDocument;


public class DisplaySummary  extends DisplayPane {
	
	Dimension dim=new Dimension(600, 400);
	JPanel messagePanel=new JPanel();
	Font font=new Font("Sans-Serif", Font.PLAIN, 9);
	
	JTextArea texter=new JTextArea();
	
	String returner=System.getProperty("line.separator");
	String tabc="\u0009";
	
	LinkedList cluster=new LinkedList();
	LinkedList snncluster=new LinkedList();
	LinkedList syntax=new LinkedList();
	LinkedList hopkins=new LinkedList();
	LinkedList miscellaneous=new LinkedList();
	LinkedList anderson=new LinkedList();
	LinkedList distfunc=new LinkedList();
	
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
		if (chs.getType()<2){
			texter.append("Hopkins statistics for elements");
		}
		else if (chs.getType()==2){
			texter.append("Hopkins statistics for syllables");
		}
		else if (chs.getType()==3){
			texter.append("Hopkins statistics for syllable transitions");
		}
		else if (chs.getType()==4){
			texter.append("Hopkins statistics for songs");
		}
		texter.append(returner);
		String[] resultString=chs.getResultString();
		for (int i=0; i<resultString.length; i++){
			texter.append(resultString[i]);
			texter.append(returner);
		}
		
	}
	
	public void addDistFunc(DistanceNeighborFunctions dnf){
		DecimalFormat df = new DecimalFormat("#.####");
		distfunc.add(dnf);
		int type=dnf.getType();
		if (type<2){
			texter.append("Densities and NN Dists for Elements");
		}
		else if (type==2){
			texter.append("Densities and NN Dists for Syllables");
		}
		else if (type==3){
			texter.append("Densities and NN Dists for Syllable Transitions");
		}
		else if (type==4){
			texter.append("Densities and NN Dists for Songs");
		}
		texter.append(returner);
		float[] meanNN=dnf.getMeanNN();
		for (int i=0; i<meanNN.length; i++){
			texter.append(meanNN[i]+tabc);
		}
		texter.append(returner);
		float[] meanDens=dnf.getMeanDens();
		for (int i=0; i<meanDens.length; i++){
			texter.append(meanDens[i]+tabc);
		}
		
	}
	
	public void addAnderson(MultivariateDispersionTest mdt){
		DecimalFormat df = new DecimalFormat("#.####");
		anderson.add(mdt);
		int type=mdt.getType();
		if (type<2){
			texter.append("Multivariate Dispersion Test for Elements (Anderson 1996)");
		}
		else if (type==2){
			texter.append("Multivariate Dispersion Test for Syllables (Anderson 1996)");
		}
		else if (type==3){
			texter.append("Multivariate Dispersion Test for Syllable Transitions (Anderson 1996)");
		}
		else if (type==4){
			texter.append("Multivariate Dispersion Test for Songs (Anderson 1996)");
		}
		texter.append(returner);
		texter.append(tabc);
		String[] popNames=mdt.getPopNames();
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
		if (type<2){
			texter.append("K-medoids statistics for elements");
		}
		else if (type==2){
			texter.append("K-medoids statistics for syllables");
		}
		else if (type==3){
			texter.append("K-medoids statistics for syllable transitions");
		}
		else if (type==4){
			texter.append("K-medoids statistics for songs");
		}
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
		if (type<2){
			texter.append("SNN summary for elements");
		}
		else if (type==2){
			texter.append("SNN summary for syllables");
		}
		else if (type==3){
			texter.append("SNN summary for syllable transitions");
		}
		else if (type==4){
			texter.append("SNN summary for songs");
		}
		texter.append(returner);
		texter.append("number of clusters: "+snn.getNumClusts());
		texter.append(returner);
		
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
					if (chs.getType()<2){
						sd.writeString("Hopkins statistics for elements");
					}
					else if (chs.getType()==2){
						sd.writeString("Hopkins statistics for syllables");
					}
					else if (chs.getType()==3){
						sd.writeString("Hopkins statistics for syllable transitions");
					}
					else if (chs.getType()==4){
						sd.writeString("Hopkins statistics for songs");
					}
					sd.writeLine();
					
					sd.writeLine();
					sd.writeString("nth nearest neighbour");
					sd.writeString("mean");
					sd.writeString("sd");
					sd.writeString("upper 2.5%ile");
					sd.writeString("lower 2.5%ile");
					sd.writeLine();
					int[] picks=chs.getPicks();
					double[][] results=chs.getResults();
					for (int j=0; j<picks.length; j++){
						sd.writeInt(picks[j]);
						for (int k=0; k<results[j].length; k++){
							sd.writeDouble(results[j][k]);
						}
						sd.writeLine();
					}
				}
			}
			if(distfunc.size()>0){
				sd.writeSheet("Dist Funcs");
				for (int i=0; i<distfunc.size(); i++){
					DistanceNeighborFunctions dnf=(DistanceNeighborFunctions)distfunc.get(i);
					int type=dnf.getType();
					if (type<2){
						sd.writeString("Densities and NN Dists for Elements");
					}
					else if (type==2){
						sd.writeString("Densities and NN Dists for Syllables");
					}
					else if (type==3){
						sd.writeString("Densities and NN Dists for Syllable Transitions");
					}
					else if (type==4){
						sd.writeString("Densities and NN Dists for Songs");
					}
					sd.writeLine();
					int n=dnf.getN();
					int nBins=dnf.getNBins();
					int dBins=dnf.getDBins();
					float[][] nNeighbors=dnf.getNNeighbors();
					float[][] densities=dnf.getDensities();
					for (int j=0; j<n; j++){
						for (int k=0; k<nBins; k++){
							sd.writeFloat(nNeighbors[j][k]);
						}
						for (int k=0; k<dBins; k++){
							sd.writeFloat(densities[j][k]);
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
					if (type<2){
						sd.writeString("Multivariate Dispersion Test for Elements (Anderson 1996)");
					}
					else if (type==2){
						sd.writeString("Multivariate Dispersion Test for Syllables (Anderson 1996)");
					}
					else if (type==3){
						sd.writeString("Multivariate Dispersion Test for Syllable Transitions (Anderson 1996)");
					}
					else if (type==4){
						sd.writeString("Multivariate Dispersion Test for Songs (Anderson 1996)");
					}
					sd.writeLine();
					sd.writeString(" ");
					double[][] meanScores=mdt.getMeanScores();
					String[] popNames=mdt.getPopNames();
					
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
					sd.writeLine();
					double[][][] popScore=mdt.getPopScore();
					for (int j=0; j<popScore.length; j++){
						for (int jj=0; jj<popScore[j].length; jj++){
							if(popScore[j][jj].length>maxPerPop){maxPerPop=popScore[j][jj].length;}
						}
					}
					for (int j=0; j<maxPerPop; j++){
						for (int k=0; k<popScore.length; k++){
							for (int kk=0; kk<popScore[k].length; kk++){
								if (popScore[k][kk].length>j){
									sd.writeDouble(popScore[k][kk][j]);
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
					if (type<2){
						sd.writeString("K-medoids statistics for elements");
					}
					else if (type==2){
						sd.writeString("K-medoids statistics for syllables");
					}
					else if (type==3){
						sd.writeString("K-medoids statistics for syllable transitions");
					}
					else if (type==4){
						sd.writeString("K-medoids statistics for songs");
					}
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
		}
		sd.finishWriting();
		
	}
	
	public void saveImage(){
		JOptionPane.showMessageDialog(this, "Sorry, no image to be saved from this tab");
		
	}
}
