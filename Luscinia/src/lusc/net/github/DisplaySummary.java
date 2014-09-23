package lusc.net.github;
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
import java.awt.image.BufferedImage;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.text.*;

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
		if (chs.type<2){
			texter.append("Hopkins statistics for elements");
		}
		else if (chs.type==2){
			texter.append("Hopkins statistics for syllables");
		}
		else if (chs.type==3){
			texter.append("Hopkins statistics for syllable transitions");
		}
		else if (chs.type==4){
			texter.append("Hopkins statistics for songs");
		}
		texter.append(returner);
		for (int i=0; i<chs.resultString.length; i++){
			texter.append(chs.resultString[i]);
			texter.append(returner);
		}
		
	}
	
	public void addDistFunc(DistanceNeighborFunctions dnf){
		DecimalFormat df = new DecimalFormat("#.####");
		distfunc.add(dnf);
		if (dnf.type<2){
			texter.append("Densities and NN Dists for Elements");
		}
		else if (dnf.type==2){
			texter.append("Densities and NN Dists for Syllables");
		}
		else if (dnf.type==3){
			texter.append("Densities and NN Dists for Syllable Transitions");
		}
		else if (dnf.type==4){
			texter.append("Densities and NN Dists for Songs");
		}
		texter.append(returner);
		for (int i=0; i<dnf.meanNN.length; i++){
			texter.append(dnf.meanNN[i]+tabc);
		}
		texter.append(returner);
		for (int i=0; i<dnf.meanDens.length; i++){
			texter.append(dnf.meanDens[i]+tabc);
		}
		
	}
	
	public void addAnderson(MultivariateDispersionTest mdt){
		DecimalFormat df = new DecimalFormat("#.####");
		anderson.add(mdt);
		if (mdt.type<2){
			texter.append("Multivariate Dispersion Test for Elements (Anderson 1996)");
		}
		else if (mdt.type==2){
			texter.append("Multivariate Dispersion Test for Syllables (Anderson 1996)");
		}
		else if (mdt.type==3){
			texter.append("Multivariate Dispersion Test for Syllable Transitions (Anderson 1996)");
		}
		else if (mdt.type==4){
			texter.append("Multivariate Dispersion Test for Songs (Anderson 1996)");
		}
		texter.append(returner);
		texter.append(tabc);
		for (int i=0; i<mdt.meanScores.length; i++){
			texter.append(mdt.popNames[i]+tabc);
		}
		texter.append(returner);
		
		for (int i=0; i<mdt.meanScores.length; i++){
			texter.append(mdt.popNames[i]+tabc);
			for (int j=0; j<mdt.meanScores[i].length; j++){
				texter.append(df.format(mdt.meanScores[i][j])+tabc);
			}
			texter.append(returner);
		}
		texter.append(returner);
		texter.append(tabc);
		for (int i=0; i<mdt.meanScores.length; i++){
			texter.append(mdt.popNames[i]+tabc);
		}
		texter.append(returner);
		
		for (int i=0; i<mdt.spatialMedianComp.length; i++){
			texter.append(mdt.popNames[i]+tabc);
			for (int j=0; j<mdt.spatialMedianComp[i].length; j++){
				texter.append(df.format(mdt.spatialMedianComp[i][j])+tabc);
			}
			texter.append(returner);
		}
		texter.append(returner);
		texter.append("F statistic:"+tabc);
		texter.append(df.format(mdt.testFScore));
		texter.append(returner);
		texter.append("p:"+tabc);
		texter.append(df.format(mdt.pValue));	
		texter.append(returner);
	}
	
	public void addSyntax(EntropyAnalysis ent){
		DecimalFormat df = new DecimalFormat("#.####");
		DecimalFormat df2 = new DecimalFormat("#.######");
		syntax.add(ent);
		if (ent.type<2){
			texter.append("Syntax statistics for elements");
		}
		else if (ent.type==2){
			texter.append("Syntax statistics for syllables");
		}
		texter.append(returner);
		if (ent.mode>0){
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
			for (int i=0; i<ent.swml.length; i++){
				texter.append(ent.swml[i].kv+tabc);
				texter.append(df.format(ent.swml[i].rho)+tabc);
				for (int j=0; j<ent.swml[i].jackknifeScores.length; j++){
					texter.append(df.format(ent.swml[i].jackknifeScores[j])+tabc);
				}
				
				texter.append(df2.format(ent.swml[i].jackknifeSD)+tabc);
				texter.append(returner);
			}	
			
			texter.append(returner);				  
		}
		if (ent.mode!=1){
			texter.append("Markov chain redundancy estimates: ");
			texter.append(returner);
			String[] s={"k","0.0005", "0.005", "0.025", "0.5", "0.975", "0.995", "0.9995"};
			for (int i=0; i<s.length; i++){
				texter.append(s[i]+tabc);
			}
			texter.append(returner);
			for (int i=0; i<ent.mkc.length; i++){
				texter.append((i+2)+tabc);
				for (int j=0; j<ent.mkc[i].redundancy.length; j++){
					texter.append(df.format(ent.mkc[i].redundancy[j])+tabc);
				}
				texter.append(returner);
			}
			texter.append("Markov chain entropy estimates: ");
			texter.append(returner);
			for (int i=0; i<s.length; i++){
				texter.append(s[i]+tabc);
			}
			texter.append(returner);
			for (int i=0; i<ent.mkc.length; i++){
				texter.append((i+2)+tabc);
				for (int j=0; j<ent.mkc[i].entropy.length; j++){
					texter.append(df.format(ent.mkc[i].entropy[j])+tabc);
				}
				texter.append(returner);
			}
			texter.append("Markov chain Zero-Order entropy estimates: ");
			texter.append(returner);
			for (int i=0; i<s.length; i++){
				texter.append(s[i]+tabc);
			}
			texter.append(returner);
			for (int i=0; i<ent.mkc.length; i++){
				texter.append((i+2)+tabc);
				for (int j=0; j<ent.mkc[i].zeroOrder.length; j++){
					texter.append(df.format(ent.mkc[i].zeroOrder[j])+tabc);
				}
				texter.append(returner);
			}
			texter.append("Positional entropy estimates: ");
			texter.append(returner);
			for (int i=0; i<s.length; i++){
				texter.append(s[i]+tabc);
			}
			texter.append(returner);
			for (int i=0; i<ent.mkc.length; i++){
				texter.append((i+2)+tabc);
				for (int j=0; j<ent.mkc[i].resultArrayP.length; j++){
					texter.append(df.format(ent.mkc[i].resultArrayP[j])+tabc);
				}
				texter.append(returner);
			}
			
		}
	}
	
	public void addCluster(KMedoids km){
		cluster.add(km);
		if (km.type<2){
			texter.append("K-medoids statistics for elements");
		}
		else if (km.type==2){
			texter.append("K-medoids statistics for syllables");
		}
		else if (km.type==3){
			texter.append("K-medoids statistics for syllable transitions");
		}
		else if (km.type==4){
			texter.append("K-medoids statistics for songs");
		}
		texter.append(returner);
		texter.append("k Global Silhouette index");
		texter.append(returner);
		for (int i=0; i<km.globalSilhouette.length; i++){
			texter.append((i+km.minK)+" "+km.globalSilhouette[i]+" "+km.simulatedSilhouette[i]);
			texter.append(returner);
		}
	}
	
	public void addSNNCluster(SNNDensity snn){
		snncluster.add(snn);
		if (snn.type<2){
			texter.append("SNN summary for elements");
		}
		else if (snn.type==2){
			texter.append("SNN summary for syllables");
		}
		else if (snn.type==3){
			texter.append("SNN summary for syllable transitions");
		}
		else if (snn.type==4){
			texter.append("SNN summary for songs");
		}
		texter.append(returner);
		texter.append("number of clusters: "+snn.numClusts);
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
					if (chs.type<2){
						sd.writeString("Hopkins statistics for elements");
					}
					else if (chs.type==2){
						sd.writeString("Hopkins statistics for syllables");
					}
					else if (chs.type==3){
						sd.writeString("Hopkins statistics for syllable transitions");
					}
					else if (chs.type==4){
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
					for (int j=0; j<chs.resultString.length; j++){
						sd.writeInt(chs.picks[j]);
						for (int k=0; k<chs.results[j].length; k++){
							sd.writeDouble(chs.results[j][k]);
						}
						sd.writeLine();
					}
				}
			}
			if(distfunc.size()>0){
				sd.writeSheet("Dist Funcs");
				for (int i=0; i<distfunc.size(); i++){
					DistanceNeighborFunctions dnf=(DistanceNeighborFunctions)distfunc.get(i);
					if (dnf.type<2){
						sd.writeString("Densities and NN Dists for Elements");
					}
					else if (dnf.type==2){
						sd.writeString("Densities and NN Dists for Syllables");
					}
					else if (dnf.type==3){
						sd.writeString("Densities and NN Dists for Syllable Transitions");
					}
					else if (dnf.type==4){
						sd.writeString("Densities and NN Dists for Songs");
					}
					sd.writeLine();
					for (int j=0; j<dnf.n; j++){
						for (int k=0; k<dnf.nBins; k++){
							sd.writeFloat(dnf.nNeighbors[j][k]);
						}
						for (int k=0; k<dnf.dBins; k++){
							sd.writeFloat(dnf.densities[j][k]);
						}
						sd.writeLine();
					}
				}
			}
			
			
			if (anderson.size()>0){
				sd.writeSheet("Multivariate Dispersion");
				for (int i=0; i<anderson.size(); i++){
					MultivariateDispersionTest mdt=(MultivariateDispersionTest)anderson.get(i);
					if (mdt.type<2){
						sd.writeString("Multivariate Dispersion Test for Elements (Anderson 1996)");
					}
					else if (mdt.type==2){
						sd.writeString("Multivariate Dispersion Test for Syllables (Anderson 1996)");
					}
					else if (mdt.type==3){
						sd.writeString("Multivariate Dispersion Test for Syllable Transitions (Anderson 1996)");
					}
					else if (mdt.type==4){
						sd.writeString("Multivariate Dispersion Test for Songs (Anderson 1996)");
					}
					sd.writeLine();
					sd.writeString(" ");
					for (int j=0; j<mdt.meanScores.length; j++){
						sd.writeString(mdt.popNames[j]);
					}
					sd.writeLine();
					
					for (int j=0; j<mdt.meanScores.length; j++){
						sd.writeString(mdt.popNames[j]);
						for (int k=0; k<mdt.meanScores[j].length; k++){
							sd.writeDouble(mdt.meanScores[j][k]);
						}
						sd.writeLine();
					}
					sd.writeLine();
					
					sd.writeString(" ");
					for (int j=0; j<mdt.spatialMedianComp.length; j++){
						sd.writeString(mdt.popNames[j]);
					}
					sd.writeLine();
					
					for (int j=0; j<mdt.spatialMedianComp.length; j++){
						sd.writeString(mdt.popNames[j]);
						for (int k=0; k<mdt.spatialMedianComp[j].length; k++){
							sd.writeDouble(mdt.spatialMedianComp[j][k]);
						}
						sd.writeLine();
					}
					sd.writeLine();
					
					sd.writeString("F statistic:");
					sd.writeDouble(mdt.testFScore);
					sd.writeLine();
					sd.writeString("p:");
					sd.writeDouble(mdt.pValue);
					sd.writeLine();
					sd.writeLine();
					int maxPerPop=0;
					
					for (int j=0; j<mdt.indScore2.length; j++){
						for (int jj=0; jj<mdt.indScore2[j].length; jj++){
							if(mdt.indScore2[j][jj].length>maxPerPop){maxPerPop=mdt.indScore2[j][jj].length;}
						}
					}
					for (int j=0; j<maxPerPop; j++){
						for (int k=0; k<mdt.indScore2.length; k++){
							for (int kk=0; kk<mdt.indScore2[k].length; kk++){
								if (mdt.indScore2[k][kk].length>j){
									sd.writeDouble(mdt.indScore2[k][kk][j]);
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
					for (int j=0; j<mdt.popScore.length; j++){
						for (int jj=0; jj<mdt.popScore[j].length; jj++){
							if(mdt.popScore[j][jj].length>maxPerPop){maxPerPop=mdt.popScore[j][jj].length;}
						}
					}
					for (int j=0; j<maxPerPop; j++){
						for (int k=0; k<mdt.popScore.length; k++){
							for (int kk=0; kk<mdt.popScore[k].length; kk++){
								if (mdt.popScore[k][kk].length>j){
									sd.writeDouble(mdt.popScore[k][kk][j]);
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
					if (km.type<2){
						sd.writeString("K-medoids statistics for elements");
					}
					else if (km.type==2){
						sd.writeString("K-medoids statistics for syllables");
					}
					else if (km.type==3){
						sd.writeString("K-medoids statistics for syllable transitions");
					}
					else if (km.type==4){
						sd.writeString("K-medoids statistics for songs");
					}
					sd.writeLine();
					sd.writeString("k");
					sd.writeString("Global Silhouette Index");
					sd.writeLine();
					for (int j=0; j<km.globalSilhouette.length; j++){
						sd.writeInt(j+2);
						sd.writeDouble(km.globalSilhouette[j]);
						sd.writeDouble(km.simulatedSilhouette[j]);
						sd.writeLine();
					}
				}
			}
			if (syntax.size()>0){
				sd.writeSheet("Syntax statistics");
				for (int i=0; i<syntax.size(); i++){
					EntropyAnalysis ent=(EntropyAnalysis)syntax.get(i);
					if (ent.type<2){
						sd.writeString("Syntax statistics for elements");
					}
					else if (ent.type==2){
						sd.writeString("Syntax statistics for syllables");
					}
					sd.writeLine();
					
					if (ent.mode>0){
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
						for (int j=0; j<ent.swml.length; j++){
							sd.writeInt(ent.swml[j].kv);
							sd.writeDouble(ent.swml[j].rho);
							for (int k=0; k<ent.swml[j].jackknifeScores.length; k++){
								sd.writeDouble(ent.swml[j].jackknifeScores[k]);
							}
							sd.writeDouble(ent.swml[j].jackknifeSD);
							sd.writeLine();
						}	
						
					}
					if (ent.mode!=1){
						String[] s={"k","0.005", "0.005", "0.025", "0.5", "0.975", "0.995", "0.9995"};
						
						sd.writeString("Markov chain redundancy estimates: ");
						sd.writeLine();
						
						for (int j=0; j<s.length; j++){
							sd.writeString(s[j]);
						}
						sd.writeLine();
						
						for (int j=0; j<ent.mkc.length; j++){
							sd.writeInt(j+2);
							for (int k=0; k<ent.mkc[j].redundancy.length; k++){
								sd.writeDouble(ent.mkc[j].redundancy[k]);
							}
							sd.writeLine();
						}
						
						sd.writeString("Markov chain entropy estimates: ");
						sd.writeLine();
						
						for (int j=0; j<s.length; j++){
							sd.writeString(s[j]);
						}
						sd.writeLine();
						
						for (int j=0; j<ent.mkc.length; j++){
							sd.writeInt(j+2);
							for (int k=0; k<ent.mkc[j].entropy.length; k++){
								sd.writeDouble(ent.mkc[j].entropy[k]);
							}
							sd.writeLine();
						}
						
						sd.writeString("Markov chain Zero Order estimates: ");
						sd.writeLine();
						
						for (int j=0; j<s.length; j++){
							sd.writeString(s[j]);
						}
						sd.writeLine();
						
						for (int j=0; j<ent.mkc.length; j++){
							sd.writeInt(j+2);
							for (int k=0; k<ent.mkc[j].zeroOrder.length; k++){
								sd.writeDouble(ent.mkc[j].zeroOrder[k]);
							}
							sd.writeLine();
						}
						
						sd.writeString("Positional entropy estimates: ");
						sd.writeLine();
						for (int j=0; j<s.length; j++){
							sd.writeString(s[j]);
						}
						sd.writeLine();
						
						for (int j=0; j<ent.mkc.length; j++){
							sd.writeInt(j+2);
							for (int k=0; k<ent.mkc[j].resultArrayP.length; k++){
								sd.writeDouble(ent.mkc[j].resultArrayP[k]);
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
