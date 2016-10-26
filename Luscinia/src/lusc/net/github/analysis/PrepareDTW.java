package lusc.net.github.analysis;

import java.io.File;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

import lusc.net.github.Element;
import lusc.net.github.Song;
import lusc.net.github.ui.compmethods.ABXdiscrimination;
import lusc.net.github.ui.compmethods.DTWSwingWorker;
import lusc.net.github.ui.compmethods.DTWPanel;


public class PrepareDTW {
	
	//double[] normReal;
	//double meanSDTemp=0;
	
	int numParams=0;
	int numTempParams=0;
	int numExtraParams=0;
	double[] validParameters;
	double validTempParameters;
	int[] paramType;
	double[][][] data, dataExtra, dataSyls, dataSylsExtra;
	double[][] dataTemp, dataSylsTemp;
	int[][] elementPos;
	double[] sdReal, sdRealStitch;
	double sdRealTemp, sdRealStitchTemp;
	
	boolean dynamicWarp=true;
	boolean interpolateWarp=true;
	boolean squaredDist=false;
	
	boolean weightByAmp=false;
	boolean logFrequencies=false;
	boolean normaliseWithSDs=false;
	int stitchSyllables=0;
	int alignmentPoints=3;
	double mainReductionFactor=0.25;
	double maximumWarp=0.25;
	int minPoints=5;
	double minVar=0.2;
	double sdRatio=1;
	double sdRatioNT=0;
	double offsetRemoval=0.0;
	public double[] parameterValues={1,0,0,0,0,1,0,0,0,1,0,0,0,0,1,0,0, 0, 0,0,0,0,0};
	
	double slopeATanTransform=0.01;
	
	AnalysisGroup ag;
	
	
	public PrepareDTW(DTWPanel dtw, AnalysisGroup ag){
		this.ag=ag;
		parameterValues=dtw.getParameterValues();
		weightByAmp=dtw.getWeightByAmp();
		stitchSyllables=dtw.getStitchSyllables();
		logFrequencies=dtw.getLogFrequencies();
		normaliseWithSDs=dtw.getWeightBySD();
		mainReductionFactor=dtw.getMainReductionFactor();
		offsetRemoval=dtw.getOffsetRemoval();
		sdRatio=dtw.getSDRatio();
		sdRatioNT=dtw.getSDRatio2();
		minPoints=dtw.getMinPoints();
		alignmentPoints=dtw.getAlignmentPoints();
		interpolateWarp=dtw.getInterpolate();
		dynamicWarp=dtw.getDynamicWarping();
		maximumWarp=dtw.getMaximumWarp();
		squaredDist=dtw.getSquared();
		slopeATanTransform=dtw.getATanTransform();
	}
	
	
	public double[] getParameterValues(){
		return parameterValues;
	}
	
	public boolean getWeightByAmp(){
		return weightByAmp;
	}
	
	public boolean getNormaliseWithSds(){
		return normaliseWithSDs;
	}
	
	public int getStitchSyllables(){
		return stitchSyllables;
	}
	
	public int getAlignmentPoints(){
		return alignmentPoints;
	}
	
	public boolean getLogFrequencies(){
		return logFrequencies;
	}
	
	public boolean getInterpolateWarp(){
		return interpolateWarp;
	}
	
	public boolean getSquared(){
		return squaredDist;
	}
	
	public boolean getDynamicWarp(){
		return dynamicWarp;
	}
	
	public double getMainReductionFactor(){
		return mainReductionFactor;
	}
	
	public double getOffsetRemoval(){
		return offsetRemoval;
	}
	
	public double getSDRatio(){
		return sdRatio;
	}
	
	public double getSDRatioNT(){
		return sdRatioNT;
	}
	
	public double getMaximumWarp(){
		return maximumWarp;
	}
	
	public int getMinPoints(){
		return minPoints;
	}
	
	public double[][][] getData(int a){
		double[][][] d=null;
		if (a==0){d=data;}
		else if (a==1){d=dataExtra;}
		else if (a==2){d=dataSyls;}
		else if (a==3){d=dataSylsExtra;}
		return d;
	}
	
	public double[][] getDataT(int a){
		double[][] d=null;
		if (a==0){d=dataTemp;}
		else if (a==1){d=dataSylsTemp;}
		return d;
	}
	
	public int[][] getElPos(){
		return elementPos;
	}
	
	public double[] getSD(int a){
		double[] d=null;
		if (a==0){d=sdReal;}
		else if (a==1){d=sdRealStitch;}
		return d;
	}
	
	public double getSDTemp(int a){
		double d=0;
		if (a==0){d=sdRealTemp;}
		else if (a==1){d=sdRealStitchTemp;}
		return d;
	}
	
	public double[] getValidParameters(){
		return validParameters;
	}
	
	public double getValidTempPar(){
		return validTempParameters;
	}
	
	public int getNumTempPars(){
		return numTempParams;
	}
	
	public void setParameterValues(double[] pv){
		parameterValues=pv;
	}
	
	public void setWeightByAmp(boolean w){
		weightByAmp=w;
	}
	
	public void setStitchSyllables(int a){
		stitchSyllables=a;
	}
	
	public void setLogFrequencies(boolean a){
		logFrequencies=a;
	}
	
	public void setMainReductionFactor(double a){
		mainReductionFactor=a;
	}
	
	public void setOffsetRemoval(double a){
		offsetRemoval=a;
	}
	
	public void setSDRatioNT(double a){
		sdRatioNT=a;
	}
	
	public void setMinPoints(int a){
		minPoints=a;
	}
	
	public void getValidParameters(boolean type){
		numParams=0;
		numTempParams=0;
		
		for (int i=1; i<21; i++){
			if ((type)&&(parameterValues[i]>0)){numParams++;}
		}
		if ((type)&&(parameterValues[0]>0)){numTempParams++;}
		
		validParameters=new double[numParams];
		paramType=new int[numParams];
		validTempParameters=0;
		
		numParams=0;
		for (int i=1; i<21; i++){
			if ((type)&&(parameterValues[i]>0)){
				validParameters[numParams]=parameterValues[i];
				paramType[numParams]=i;
				numParams++;
			}
		}
		if ((type)&&(parameterValues[0]>0)){
			validTempParameters=parameterValues[0];
		}
	}
	
	
	public double[] getAverages(double[][] m){
		
		int n1=m.length;
		
		double[] out=new double[4];
		
		int a=11;
		
		for (int i=0; i<4; i++){
			double t=0;
			double u=0;
			for (int j=5; j<n1; j++){
				if (logFrequencies){
					t+=Math.log(m[j][i])*m[j][a];	
				}
				else{
					t+=m[j][i]*m[j][a];
				}
				u+=m[j][a];
			}
			t/=u;
			out[i]=t;
			
			//System.out.println(i+" "+out[i]+" "+Math.log(m[4][i]));
			
		}
		
		
		
		return out;
	}
	
	public void compressData(){
		
		numExtraParams=1;
		if (weightByAmp){
			numExtraParams++;
		}
		
		int eleNumber=ag.getLengths(0);
		Song[] songs=ag.getSongs();
		
		data=new double[eleNumber][][];
		dataTemp=new double[eleNumber][];
		dataExtra=new double[eleNumber][][];
		
		double[] count;
						
		double reductionFactor=mainReductionFactor;
		
		int s,t,c;
		int np=numParams;
		int p=0;
		double s2;
		for (int i=0; i<songs.length; i++){
			int eleSizeS=songs[i].getNumElements();
			//System.out.println(eleSizeS);
			for (int j=0; j<eleSizeS; j++){
				Element ele=(Element)songs[i].getElement(j);
				double[][] measu=ele.getMeasurements();
				
				
				double[] averages=getAverages(measu);
				
				s=ele.getLength();
				
				reductionFactor=mainReductionFactor;
				t=(int)Math.round(reductionFactor*s);
								
				if (t<minPoints){
					t=minPoints;
					reductionFactor=t/(s+0.0);
					if (s<minPoints){
						t=s;
						reductionFactor=1;
					}
				}
				
				
				s2=s-1;
				data[p]=new double[numParams][t];
				if (numTempParams>0){
					dataTemp[p]=new double[t];
				}
				dataExtra[p]=new double[numExtraParams][t];
				
				count=new double[t];
				double diff=measu[0][11]-measu[1][11];
				double diff2=measu[0][11];
				
				
				for (int a=0; a<s; a++){
					c=(int)Math.floor(a*reductionFactor+0.0000000001);
					if (c==t){c--;}
					
					count[c]++;
					int ab=a+5;
					
					if(numTempParams>0){		
						dataTemp[p][c]+=ele.getTimeStep()*a;
					}
					
					for (int q=0; q<numParams; q++){
						if (paramType[q]==1){
							data[p][q][c]+=a/s2;
						}
						else if (paramType[q]<6){
							if (logFrequencies){
								data[p][q][c]+=Math.log(measu[ab][paramType[q]-2]);
							}
							else{
								data[p][q][c]+=measu[ab][paramType[q]-2];
							}
						}
						else if (paramType[q]<10){
							
							data[p][q][c]+=Math.atan2(measu[ab][paramType[q]-2], slopeATanTransform);
							//System.out.println(paramType[q]+" "+data[p][q][c]+" "+measu[ab][paramType[q]-2]+" "+slopeATanTransform);
						}
						else if (paramType[q]<14){
							data[p][q][c]+=measu[ab][paramType[q]-2];

						}
						else if (paramType[q]==15){
							double ta=measu[ab][paramType[q]-2];
							ta=Math.log(Math.max(ta,1));
							data[p][q][c]+=ta;
								 
						}
						else if (paramType[q]==16){
							if (ele.getTimeAfter()!=-10000){
								data[p][q][c]+=ele.getTimeAfter();
							}
							else{
								data[p][q][c]+=50;
							}
							//System.out.println("Time gap: "+ele.getTimeAfter());
						}
						else if ((paramType[q]>16)&&(paramType[q]<21)){
							if (logFrequencies){
								//data[p][q][c]+=Math.log(measu[ab][paramType[q]-17])-Math.log(measu[4][paramType[q]-17]);
								data[p][q][c]+=Math.log(measu[ab][paramType[q]-17])-averages[paramType[q]-17];
							}
							else{
								//data[p][q][c]+=measu[ab][paramType[q]-17]-measu[4][paramType[q]-17];
								data[p][q][c]+=measu[ab][paramType[q]-17]-averages[paramType[q]-17];
							}
							
						}
					
						else{
							data[p][q][c]+=measu[ab][paramType[q]-2];
						}
					}
					
					if (weightByAmp){
						dataExtra[p][1][c]+=(diff2-measu[ab][11])/diff;
						dataExtra[p][1][c]+=0.01;
					}					
				}
				
				for (int a=0; a<t; a++){
					if (numTempParams>0){
						dataTemp[p][a]/=count[a];
					}
					for (int b=0; b<numParams; b++){
						data[p][b][a]/=count[a];
					}
					if (weightByAmp){
						dataExtra[p][1][a]/=count[a];
					}
				}
				dataExtra[p][0][t-1]=1;
				p++;
			}
		}
	}
	
	public void stitchSyllables(){		//a method to stitch together elements to generate one submatrix for each syllable
	
		Song[] songs=ag.getSongs();
		//int[][] lookUpEls=ag.getLookUp(0);
		
		int countSyls=0;
		for (int i=0; i<songs.length; i++){
			for (int j=0; j<songs[i].getNumPhrases(); j++){
				int[][] p=(int[][]) songs[i].getPhrase(j);
				countSyls+=p.length;
			}
		}
		
		dataSylsTemp=new double[countSyls][];
		dataSyls=new double[countSyls][numParams][];
		dataSylsExtra=new double[countSyls][numExtraParams][];
		
		elementPos=new int[countSyls][];
		countSyls=0;
		int countEls=0;
		
		for (int i=0; i<songs.length; i++){
			for (int j=0; j<songs[i].getNumPhrases(); j++){
				int[][] p=(int[][]) songs[i].getPhrase(j);
				
				
				
				
				
				
				for (int k=0; k<p.length; k++){
					int elePos=countEls;
					
					int sylLength=0;
					for (int g=0; g<p[k].length; g++){
						if (p[k][g]>=0){
							sylLength+=data[elePos][0].length;
							elePos++;
						}
					}
					
					
					

					double[] means=new double[paramType.length];
					
					for (int q=0; q<paramType.length; q++){
						if ((paramType[q]>16)&&(paramType[q]<21)){
							double le=0;
							for (int g=0; g<p[k].length; g++){
								if (p[k][g]>=0){
									Element ele=(Element)songs[i].getElement(p[k][g]);
									double[][] measu=ele.getMeasurements();
									
									if (logFrequencies){
										means[q]+=ele.getLength()*Math.log(measu[4][paramType[q]-17]);
									}
									else{
										means[q]+=ele.getLength()*measu[4][paramType[q]-17];
									}
									le+=ele.getLength();
								}
							}	
							means[q]/=le;
						}
					}	
					
					double[] dst=new double[sylLength];
					double[][] ds=new double[numParams][sylLength];
					double[][] dse=new double[numExtraParams][sylLength];
					
					if(numTempParams>0){
						dataSylsTemp[countSyls]=new double[sylLength];
					}
					for (int g=0; g<numParams; g++){
						dataSyls[countSyls][g]=new double[sylLength];
					}
					for (int g=0; g<numExtraParams; g++){
						dataSylsExtra[countSyls][g]=new double[sylLength];
					}
					
					elementPos[countSyls]=new int[sylLength];
					sylLength=0;
					
					//System.out.println(songs[i].getName()+" "+j+" "+k);
					int q=0;
					while (p[k][q]<0){q++;}
					
					Element ele=(Element)songs[i].getElement(p[k][q]);
					double[][] measu=ele.getMeasurements();
					double startPos=ele.getBeginTime()*ele.getTimeStep();
					for (int g=0; g<p[k].length; g++){
						if (p[k][g]>=0){
							Element ele2=(Element)songs[i].getElement(p[k][g]);
							
							double adjust=(ele2.getTimeStep()*ele2.getBeginTime())-startPos;
							for (int b=0; b<data[countEls][0].length; b++){
								if(numTempParams>0){
									//dataSylsTemp[countSyls][sylLength]=dataTemp[countEls][b]+adjust;
									
									dst[sylLength]=dataTemp[countEls][b]+adjust;
								}
								for (int a=0; a<numParams; a++){
									//dataSyls[countSyls][a][sylLength]=data[countEls][a][b];
									if ((paramType[a]<=16)||(paramType[a]>=21)){
										ds[a][sylLength]=data[countEls][a][b];
									}
									else{
										double av=measu[4][paramType[a]-17];
										if (logFrequencies){
											av=Math.log(av);
										}
										ds[a][sylLength]=(data[countEls][a][b]+av)-means[a];
									}
								}
								for (int a=0; a<numExtraParams; a++){
									//dataSylsExtra[countSyls][a][sylLength]=dataExtra[countEls][a][b];
									dse[a][sylLength]=dataExtra[countEls][a][b];
								}
								elementPos[countSyls][sylLength]=g;
								sylLength++;
							}
							countEls++;
						}
					}
					
					for (int g=0; g<sylLength; g++){
						double min=1000000000;
						int minloc=-1;
						for (int h=0; h<sylLength; h++){		
							if (dst[h]<min){
								min=dst[h];
								minloc=h;
							}
						}
						
						dataSylsTemp[countSyls][g]=dst[minloc];
						for (int a=0; a<numParams; a++){
							dataSyls[countSyls][a][g]=ds[a][minloc];
						}
						for (int a=0; a<numExtraParams; a++){
							dataSylsExtra[countSyls][a][g]=dse[a][minloc];
						}
						dst[minloc]=1000000000;
					}
					
					
					
					countSyls++;
				}
			}
		}
	}
	
	public void prepareToNormalize(){
		sdReal=normalize(data);
		if (numTempParams>0){
			sdRealTemp=normalize(dataTemp);
		}
	}
	
	public void prepareToNormalizeStitch(){
		sdRealStitch=normalize(dataSyls);
		if (numTempParams>0){
			sdRealStitchTemp=normalize(dataSylsTemp);
		}	
	}
	
	public double normalize(double[][] data){
		double average=0;
		double count=0;
		
		for (int i=0; i<data.length; i++){
			int le=data[i].length;
			for (int j=0; j<le; j++){		
				average+=data[i][j];
			}
			count+=le;
		}

		average/=count;
		
		double sd=0;
		double out=0;
		double w;
		
		for (int i=0; i<data.length; i++){		
			for (int j=0; j<data[i].length; j++){
				w=data[i][j]-average;
				sd+=w*w;
			}
		}
		
		out=Math.sqrt(sd/(count-1.0));	
		
		return(out);
	}
	
	public double[] normalize(double[][][] data){
	
		int dims=data[0].length;

		double[] average=new double[numParams];
		double count=0;
		
		for (int i=0; i<data.length; i++){
			int le=data[i][0].length;
			for (int j=0; j<le; j++){		
				for (int k=0; k<numParams; k++){
					average[k]+=data[i][k][j];
				}
			}
			count+=le;
		}

		for (int i=0; i<numParams; i++){
			average[i]/=count;
		}
		
		double[] sd=new double[numParams];
		double[] out=new double[numParams];
		double w;
		
		for (int i=0; i<data.length; i++){		
			for (int j=0; j<data[i][0].length; j++){
				for (int k=0; k<numParams; k++){
					w=data[i][k][j]-average[k];
					sd[k]+=w*w;
				}
			}
		}
		
		for (int i=0; i<numParams; i++){
			out[i]=Math.sqrt(sd[i]/(count-1.0));
		}	
		
		return(out);
	}
	
	public double[][] tuneDTWInd(DTWSwingWorker dtws, boolean stitch){
		
		LinkedList<String[]> data=new LinkedList<String[]>();
		try{
			//Scanner scanner = new Scanner(new File("/Users/Rob/Desktop/ResultsDay5ImacCopy.csv"));
			//Scanner scanner = new Scanner(new File("/Users/Rob/Desktop/ResultsDay4Laptop.csv"));
			Scanner scanner = new Scanner(new File("/Users/Rob/Desktop/ResultsPercepFinal.csv"));
			while(scanner.hasNext()){
				
				String line=scanner.nextLine();
				String[] fields = line.split(",");
				
				if (fields.length==10){
					data.add(fields);
				}
				else{
					System.out.println("OOPS: "+line);
				}

			}
			scanner.close();
		}
		catch(Exception e){e.printStackTrace();}
		
		
		
		//ABXdiscrimination abx=new ABXdiscrimination(ag, ag.getDefaults(), true);
		//abx.getResultsFromDB();
		
		//int[][] c=abx.otherData;
		
		
		
		Song[] songs=ag.getSongs();
		
		
		LinkedList<LinkedList<String[]>> perind=new LinkedList<LinkedList<String[]>>();
		
		for (int i=1; i<data.size(); i++){
			String[] sa=data.get(i);
			boolean found=false;
			for (int j=0; j<perind.size(); j++){
				LinkedList<String[]> sc=perind.get(j);
				String[] sd=sc.get(0);
				if (sd[0].equals(sa[0])){
					sc.add(sa);
					found=true;
					j=perind.size();
				}
			}
			if (!found){
				LinkedList<String[]> sc = new LinkedList<String[]>();
				sc.add(sa);
				perind.add(sc);
			}	
		}
		
		
		Random random=new Random(System.currentTimeMillis());
		
		for (LinkedList<String[]> data2 : perind){
			
			String[] t=data2.get(0);
			String name=t[0];
			//if ((name.startsWith("HOS"))||(name.startsWith("YUV"))||(name.startsWith("leo"))){
			//if ((name.startsWith("leo"))){

			int nr=data2.size();
			int[] choices=new int[nr];
			int[][] combos=new int[nr][3];	
			//System.out.println(name+" "+data2.size());
			for (int i=0; i<data2.size(); i++){
				String[] s=data2.get(i);
			
				if (s[7].equals("-1")){choices[i]=-1;}
				else if (s[7].equals("1")){choices[i]=1;}
				for (int j=0; j<3; j++){
				
					int jj=j*2+2;
				
					for (int k=0; k<songs.length; k++){
						if (songs[k].getName().equals(s[jj])){
							combos[i][j]=k;
							k=songs.length;
						}
					}	
				}			
			}
		
		
		
		//index 0: songA, 1: songB, 2: songX
		//index: 3: choice (L vs R): L: -1, R: 1, No Choice: 0
		//we used BXA design. Left was B (I THINK!). -1 means X was closer to B; 1 means X was closer to A
		
		
			double bestScore=0;
			double prevScore=Double.NEGATIVE_INFINITY;
			double[][] bestResults=null;
			int nsamps=10000;
			double[][] results=new double[nsamps][validParameters.length+1];
		
			for (int repi=0; repi<nsamps; repi++){
			
			//validParameters=new double[validParameters.length];
				double[] oldvp=new double[validParameters.length];
				for (int repj=0; repj<validParameters.length; repj++){
					oldvp[repj]=validParameters[repj];
					validParameters[repj]=Math.exp(Math.log(validParameters[repj])+random.nextGaussian()*0.2) ;
					if (validParameters[repj]<=0.01){validParameters[repj]=0.01;}
				}
				double[][] scores=runDTW(dtws, stitch);
			
			
				for (int i=0; i<validParameters.length; i++){
					results[repi][i]=validParameters[i];
				}
				double[] sc=evaluateScore(scores, combos, choices);
				double score=sc[0];
				results[repi][validParameters.length]=score;
				if (score<prevScore){
				
					double alph=Math.exp(score-prevScore);
					if (random.nextDouble()>alph){
					//REJECT
						for (int i=0; i<validParameters.length; i++){
							validParameters[i]=oldvp[i];
						}
					}
					else{
					//ACCEPT
						prevScore=score;
					}
				}
				else{
				//ACCEPT
					prevScore=score;
					bestScore=score;
					bestResults=scores;
				}
			}
		
			//System.out.println("ANALYSIS FINISHED! "+bestScore);
			//for (int i=0; i<validParameters.length; i++){
				//System.out.println(validParameters[i]);
			//}
			
			double[] scores=new double[results[0].length];
			
			for (int i=0; i<results.length; i++){
			//System.out.print(i+1+", ");
				for (int j=0; j<results[i].length; j++){
				//System.out.print(results[i][j]+", ");
					scores[j]+=results[i][j];
				}
			//System.out.println();
			}
			System.out.print(name+" ");
			for (int i=0; i<scores.length; i++){
				System.out.print((scores[i]/(nsamps+0.00))+" ");
			}
			System.out.println();
		}
		//}
		return null;
	}
	
public double[][] tuneDTWAll(DTWSwingWorker dtws, boolean stitch){
		
		LinkedList<String[]> data=new LinkedList<String[]>();
		try{
			//Scanner scanner = new Scanner(new File("/Users/Rob/Desktop/ResultsDay5ImacCopy.csv"));
			//Scanner scanner = new Scanner(new File("/Users/Rob/Desktop/ResultsDay4Laptop.csv"));
			Scanner scanner = new Scanner(new File("/Users/Rob/Desktop/ResultsPercepFinal.csv"));
			while(scanner.hasNext()){
				
				String line=scanner.nextLine();
				String[] fields = line.split(",");
				
				if (fields.length==10){
					data.add(fields);
				}
				else{
					System.out.println("OOPS: "+line);
				}

			}
			scanner.close();
		}
		catch(Exception e){e.printStackTrace();}
		
		
		
		//ABXdiscrimination abx=new ABXdiscrimination(ag, ag.getDefaults(), true);
		//abx.getResultsFromDB();
		
		//int[][] c=abx.otherData;
		
		int nr=data.size()-1;
		
		int[] choices=new int[nr];

		int[][] combos=new int[nr][3];
		
		Song[] songs=ag.getSongs();
		
		for (int i=1; i<data.size(); i++){
			String[] s=data.get(i);
			
			//-1 means LEFT, 1 means RIGHT B IS ON LEFT (BXA)
			
			
			if (s[7].equals("-1")){choices[i-1]=-1;}
			else if (s[7].equals("1")){choices[i-1]=1;}
			System.out.println(choices[i-1]);
			//System.out.println(nr+" "+s[2]+" "+s[4]+" "+s[6]);
			for (int j=0; j<3; j++){
				
				int jj=j*2+2;
				
				for (int k=0; k<songs.length; k++){
					if (songs[k].getName().equals(s[jj])){
						combos[i-1][j]=k;
						k=songs.length;
					}
				}	
			}
			
			//System.out.println(i+" "+combos[i-1][0]+" "+combos[i-1][1]+" "+combos[i-1][2]);
			
		}
		
		
		
		//index 0: songA, 1: songB, 2: songX
		//index: 3: choice (L vs R): L: -1, R: 1, No Choice: 0
		//we used BXA design. Left was B (I THINK!). -1 means X was closer to B; 1 means X was closer to A
		
		Random random=new Random(System.currentTimeMillis());
		double bestScore=0;
		double prevScore=Double.NEGATIVE_INFINITY;
		double[][] bestResults=null;
		int nsamps=10000;
		double[][] results=new double[nsamps][validParameters.length+1];
		
		for (int repi=0; repi<nsamps; repi++){
			
			//validParameters=new double[validParameters.length];
			double[] oldvp=new double[validParameters.length];
			for (int repj=0; repj<validParameters.length; repj++){
				oldvp[repj]=validParameters[repj];
				validParameters[repj]=Math.exp(Math.log(validParameters[repj])+random.nextGaussian()*0.2) ;
				if (validParameters[repj]<=0.01){validParameters[repj]=0.01;}
			}
			double[][] scores=runDTW(dtws, stitch);
			
			
			for (int i=0; i<validParameters.length; i++){
				results[repi][i]=validParameters[i];
			}
			double[] sc=evaluateScore(scores, combos, choices);
			double score=sc[0];
			results[repi][validParameters.length]=score;
			if (score<prevScore){
				
				double alph=Math.exp(score-prevScore);
				if (random.nextDouble()>alph){
					//REJECT
					for (int i=0; i<validParameters.length; i++){
						validParameters[i]=oldvp[i];
					}
				}
				else{
					//ACCEPT
					prevScore=score;
				}
			}
			else{
				//ACCEPT
				prevScore=score;
				bestScore=score;
				bestResults=scores;
			}
			//else{
				//bestScore=score;
				//bestResults=scores;
			//}
			
			
			
			
			//for (int i=0; i<validParameters.length; i++){
				//System.out.print(validParameters[i]+" ");
			//}
			//System.out.println();
			
		}
		
		System.out.println("ANALYSIS FINISHED! "+bestScore);
		for (int i=0; i<validParameters.length; i++){
			System.out.println(validParameters[i]);
		}
		
		for (int i=0; i<results.length; i++){
			System.out.print(i+1+", ");
			for (int j=0; j<results[i].length; j++){
				System.out.print(results[i][j]+", ");
			}
			System.out.println();
		}
		
		return bestResults;
	}
	
	public double[][] tuneDTW(DTWSwingWorker dtws, boolean stitch){
		
		LinkedList<String[]> data=new LinkedList<String[]>();
		try{
			//Scanner scanner = new Scanner(new File("/Users/Rob/Desktop/ResultsDay5ImacCopy.csv"));
			//Scanner scanner = new Scanner(new File("/Users/Rob/Desktop/ResultsDay4Laptop.csv"));
			Scanner scanner = new Scanner(new File("/Users/Rob/Desktop/ResultsPercepFinal.csv"));
			while(scanner.hasNext()){
				
				String line=scanner.nextLine();
				String[] fields = line.split(",");
				
				if (fields.length==10){
					data.add(fields);
				}
				else{
					System.out.println("OOPS: "+line);
				}

			}
			scanner.close();
		}
		catch(Exception e){e.printStackTrace();}
		
		
		
		//ABXdiscrimination abx=new ABXdiscrimination(ag, ag.getDefaults(), true);
		//abx.getResultsFromDB();
		
		//int[][] c=abx.otherData;
		
		int nr=data.size()-1;
		
		int[] choices=new int[nr];

		int[][] combos=new int[nr][3];
		
		Song[] songs=ag.getSongs();
		
		for (int i=1; i<data.size(); i++){
			String[] s=data.get(i);
			
			//-1 means LEFT, 1 means RIGHT B IS ON LEFT (BXA)
			
			
			if (s[7].equals("-1")){choices[i-1]=-1;}
			else if (s[7].equals("1")){choices[i-1]=1;}
			System.out.println(choices[i-1]);
			//System.out.println(nr+" "+s[2]+" "+s[4]+" "+s[6]);
			for (int j=0; j<3; j++){
				
				int jj=j*2+2;
				
				for (int k=0; k<songs.length; k++){
					if (songs[k].getName().equals(s[jj])){
						combos[i-1][j]=k;
						k=songs.length;
					}
				}	
			}
			
			//System.out.println(i+" "+combos[i-1][0]+" "+combos[i-1][1]+" "+combos[i-1][2]);
			
		}
		
		
		
		//index 0: songA, 1: songB, 2: songX
		//index: 3: choice (L vs R): L: -1, R: 1, No Choice: 0
		//we used BXA design. Left was B (I THINK!). -1 means X was closer to B; 1 means X was closer to A
		
		Random random=new Random(System.currentTimeMillis());
		double bestScore=0;
		double prevScore=Double.NEGATIVE_INFINITY;
		double[][] bestResults=null;
		int nsamps=1000;
		double[][] results=new double[nsamps][validParameters.length+1];
		
		for (int repi=0; repi<nsamps; repi++){
			
			//validParameters=new double[validParameters.length];
			//double[] oldvp=new double[validParameters.length];
			//for (int repj=0; repj<validParameters.length; repj++){
				//oldvp[repj]=validParameters[repj];
				//validParameters[repj]=Math.exp(Math.log(validParameters[repj])+random.nextGaussian()*0.2) ;
				//if (validParameters[repj]<=0.01){validParameters[repj]=0.01;}
			//}
			double[][] scores=runDTW(dtws, stitch);
			
			
			for (int i=0; i<validParameters.length; i++){
				results[repi][i]=validParameters[i];
			}
			double[] sc=evaluateScore(scores, combos, choices, random);
			double score=sc[0];
			System.out.println("SCORES: "+sc[0]+" "+sc[1]+" "+sc[2]);
			results[repi][validParameters.length]=score;
			if (score<prevScore){
				
				double alph=Math.exp(score-prevScore);
				if (random.nextDouble()>alph){
					//REJECT
					for (int i=0; i<validParameters.length; i++){
						//validParameters[i]=oldvp[i];
					}
				}
				else{
					//ACCEPT
					prevScore=score;
				}
			}
			else{
				//ACCEPT
				prevScore=score;
				bestScore=score;
				bestResults=scores;
			}
			//else{
				//bestScore=score;
				//bestResults=scores;
			//}
			
			
			
			
			//for (int i=0; i<validParameters.length; i++){
				//System.out.print(validParameters[i]+" ");
			//}
			//System.out.println();
			
		}
		
		System.out.println("ANALYSIS FINISHED! "+bestScore);
		for (int i=0; i<validParameters.length; i++){
			System.out.println(validParameters[i]);
		}
		
		for (int i=0; i<results.length; i++){
			System.out.print(i+1+", ");
			for (int j=0; j<results[i].length; j++){
				System.out.print(results[i][j]+", ");
			}
			System.out.println();
		}
		
		return bestResults;
	}
	
	public double evaluateScoreOld(double[][] scores, int[][] combos, int[] choices){
		double s=0;
		
		//choices==-1 means that B is closer to X than A.
		
		for (int i=0; i<combos.length; i++){
			
			int x=combos[i][2];
			int a=combos[i][0];
			int b=combos[i][1];
			
			double score1=0;
			double score2=0;
			
			if (x>a){
				score1=scores[x][a];
			}
			else{
				score1=scores[a][x];
			}
			if (x>b){
				score2=scores[x][b];
			}
			else{
				score2=scores[b][x];
			}
			
			if ((score1<score2)&&(choices[i]==1)){s++;}
			if ((score1>score2)&&(choices[i]==-1)){s++;}
			
			
			
		}
		
		return s;
		
	}
	
	public double[] evaluateScore(double[][] scores, int[][] combos, int[] choices){
		double s=0;
		double t=0;
		double u=0;
		//choices==-1 means that B is closer to X than A.
		
		for (int i=0; i<combos.length; i++){
			
			int x=combos[i][2];
			int a=combos[i][0];
			int b=combos[i][1];
			
			double score1=0;
			double score2=0;
			
			if (x>a){
				score1=scores[x][a];
			}
			else{
				score1=scores[a][x];
			}
			if (x>b){
				score2=scores[x][b];
			}
			else{
				score2=scores[b][x];
			}
			
			double xp=(score1-score2)/(score1+score2);
			
			//score1>score2... dtw thinks should have chosen score2
			//xp... positive number
			//xq... <0.5
			//score1<score2...  x closer to a than to b... xq>0.5
			
			double xq=1/(1+Math.exp(4*xp));
			
			if (choices[i]==1){
				s+=Math.log(xq);
			}
			else{
				s+=Math.log(1-xq);
			}
			
			if (xq>0.5){
				t+=Math.log(xq);
			}
			else{
				t+=Math.log(1-xq);
			}
			
			//if ((score1<score2)&&(choices[i]==1)){s++;}
			//if ((score1>score2)&&(choices[i]==-1)){s++;}
			
		}
		
		return new double[]{s,t};
		
	}
	
	public double[] evaluateScore(double[][] scores, int[][] combos, int[] choices, Random random){
		double s=0;
		double t=0;
		double u=0;
		//choices==-1 means that B is closer to X than A.
		
		for (int i=0; i<combos.length; i++){
			
			int x=combos[i][2];
			int a=combos[i][0];
			int b=combos[i][1];
			
			double score1=0;
			double score2=0;
			
			if (x>a){
				score1=scores[x][a];
			}
			else{
				score1=scores[a][x];
			}
			if (x>b){
				score2=scores[x][b];
			}
			else{
				score2=scores[b][x];
			}
			
			double xp=(score1-score2)/(score1+score2);
			
			//score1>score2... dtw thinks should have chosen score2
			//xp... positive number
			//xq... <0.5
			//score1<score2...  x closer to a than to b... xq>0.5
			
			double xq=1/(1+Math.exp(4*xp));
			
			double xr=random.nextDouble();
			
			int xs=0;
			if (xr<xq){xs=1;}
			
			if (choices[i]==1){
				s+=Math.log(xq);
			}
			else{
				s+=Math.log(1-xq);
			}
			
			if (xq>0.5){
				t+=Math.log(xq);
			}
			else{
				t+=Math.log(1-xq);
			}
			
			if (xs==1){
				u+=Math.log(xq);
			}
			else{
				u+=Math.log(1-xq);
			}
			
			//if ((score1<score2)&&(choices[i]==1)){s++;}
			//if ((score1>score2)&&(choices[i]==-1)){s++;}
			
		}
		
		return new double[]{s,t, u};
		
	}
	
	
	public synchronized CompareThread runDTWpair(DTWSwingWorker dtws, boolean stitch, int id1, int id2){
		
		int eleSize=data.length;
		if(stitch){
			eleSize=dataSyls.length;
		}
		
		
		int[][] elpos=null;
		//double[][][] data1=data;
		//double[][] data2=dataTemp;
		//double[][][] data3=dataExtra;
		if(stitch){
			elpos=elementPos;
			//data1=dataSyls;
			//data2=dataSylsTemp;
			//data3=dataSylsExtra;
			System.out.println("STITCHING ENABLED");
		}
		
		double[][] scores=new double[eleSize][eleSize];
		
		for (int i=0; i<sdReal.length; i++){
			System.out.println(i+" "+sdReal[i]);
		}
		double[] sdOver=new double[15];
		
		
		int maxlength=0;
		int e=eleSize;
		if (stitch){e=dataSyls.length;}
		if (!stitch){
			for (int lb=0; lb<eleSize; lb++){
				if (data[lb][0].length>maxlength){maxlength=data[lb][0].length;}
			}
		}
		else{
			for (int lb=0; lb<e; lb++){
				if (dataSyls[lb][0].length>maxlength){maxlength=dataSyls[lb][0].length;}
			}
		}		
		
		double[] scoresX=new double[id1+1];
		System.out.println("STARTING COMPARISON");
		CompareThread ct=null;
		try{
			ct=new CompareThread(maxlength, this, stitch, scoresX, id1, id1+1, id2, true);
			ct.setPriority(Thread.MIN_PRIORITY);
			ct.start();
			ct.join();
		}
		catch (Exception f){
			f.printStackTrace();			
		}

		return ct;	
	}
	

	public synchronized double[][] runDTW(DTWSwingWorker dtws, boolean stitch){
		double[][] scoresH=null;
		
		
		int ncores=Runtime.getRuntime().availableProcessors();
		
		int eleSize=data.length;
		if(stitch){
			eleSize=dataSyls.length;
		}
		
		
		int[][] elpos=null;
		//double[][][] data1=data;
		//double[][] data2=dataTemp;
		//double[][][] data3=dataExtra;
		if(stitch){
			elpos=elementPos;
			//data1=dataSyls;
			//data2=dataSylsTemp;
			//data3=dataSylsExtra;
			//System.out.println("STITCHING ENABLED");
		}
		
		double[][] scores=new double[eleSize][eleSize];
		
		for (int i=0; i<sdReal.length; i++){
			System.out.println("SD REAL!: "+i+" "+sdReal[i]);
		}
		double[] sdOver=new double[15];

		double[][] scoresX=new double[ncores][eleSize];
		
		CompareThread ct[]=new CompareThread[ncores];
		
		int maxlength=0;
		int e=eleSize;
		if (stitch){e=dataSyls.length;}
		if (!stitch){
			for (int lb=0; lb<eleSize; lb++){
				if (data[lb][0].length>maxlength){maxlength=data[lb][0].length;}
			}
		}
		else{
			for (int lb=0; lb<e; lb++){
				if (dataSyls[lb][0].length>maxlength){maxlength=dataSyls[lb][0].length;}
			}
		}
		
		
		int[] starts=new int[ncores];
		int[] stops=new int[ncores];
		for (int i=0; i<ncores; i++){
			starts[i]=i*(e/ncores);
			stops[i]=(i+1)*(e/ncores);
			//System.out.println(starts[i]+" "+stops[i]+" "+eleSize);
		}
		stops[ncores-1]=e;
		
		
		for (int k=0; k<e; k++){
			
			if (dtws.isCancelled()){
				break;
			}
			int prog=(int)Math.round(100*(k+1)/eleSize);
			dtws.progress(prog);
		
			for (int cores=0; cores<ncores; cores++){
				//ct[cores]=new CompareThread(maxlength, data1, data2, data3, elpos, sdReal, sdRatio, validParameters, weightByAmp, scoresX[cores], starts[cores], stops[cores], k);
				ct[cores]=new CompareThread(maxlength, this, stitch, scoresX[cores], starts[cores], stops[cores], k, false);
				//ct[cores]=new CompareThread4(maxlength, data, elpos, sdOver, sdRatio, validParameters, weightByAmp, scoresX[cores], starts[cores], stops[cores], k);
				ct[cores].setPriority(Thread.MIN_PRIORITY);
				ct[cores].start();
			}
			
			try{
				for (int cores=0; cores<ncores; cores++){
					ct[cores].join();
					System.arraycopy(scoresX[cores], starts[cores], scores[k], starts[cores], stops[cores]-starts[cores]);
				}
			}
			catch (Exception f){
				f.printStackTrace();
			}
		}
		
		scoresH=new double[e][];
		for (int i=0; i<e; i++){
			scoresH[i]=new double[i+1];
			for (int j=0; j<i; j++){
				//scoresH[i][j]=Math.min(scores[i][j], scores[j][i]);
				scoresH[i][j]=Math.max(scores[i][j], scores[j][i]);
				//scoresH[i][j]=0.5f*(scores[i][j]+scores[j][i]);
				if ((Double.isNaN(scores[i][j]))||(Double.isNaN(scores[j][i]))){
					System.out.println("NaN TROUBLE:"+" "+scores[i][j]+" "+scores[j][i]);
					//System.out.println(songs[lookUpEls[i][0]].getIndividualName()+" "+songs[lookUpEls[i][0]].getName()+" "+lookUpEls[i][1]);
					//System.out.println(songs[lookUpEls[j][0]].getIndividualName()+" "+songs[lookUpEls[j][0]].getName()+" "+lookUpEls[j][1]);
				}
				
			}
		}
		
		/*
		double totweight=0;
		for (int i=0; i<validParameters.length; i++){
			totweight+=validParameters[i];
		}
		totweight+=validTempParameters;
		
		int p=0;
		if (stitch){p=1;}
		
		double[][] dataT=getDataT(p);
		double sdsT=getSDTemp(p);
		
		double sdT=0;
		if (numTempParams>0){
			for (int i=0; i<eleSize; i++){
				for (int j=0; j<eleSize; j++){
					double sdT2=0;
					sdT2+=Math.max(dataT[i][dataT[i].length-1]-dataT[i][0],dataT[j][dataT[j].length-1]-dataT[j][0]);
					sdT2=sdT2*sdRatio+(1-sdRatio)*sdsT;
					sdT2=validTempParameters/(totweight*sdT2);
					sdT+=sdT2;
				}
			}
			sdT/=(eleSize*eleSize)*1.0;
		}
		meanSDTemp=sdT;
		normReal=new double[sdReal.length];
		
		double[] sdR=getSD(p);
		
		for (int i=0; i<sdReal.length; i++){
			normReal[i]=validParameters[i]/(totweight*sdR[i]);
		}
		*/
		//System.out.println("ACTUAL COMPARATORS "+sdT);
		//for (int i=0; i<sdReal.length; i++){
			//System.out.println(normReal[i]);
		//}
		
		/*
		int r3=0;
		double r4[]=new double[xx.length];
		double r4m=0;
		for (int r1=0; r1<scoresH.length; r1++){
			for (int r2=0; r2<r1; r2++){
				r4[r3]=scoresH[r1][r2]/xx[r3];
				r4m+=r4[r3];
				r3++;
			}
		}
		
		r4m/=r3+0.0;
		double r5=0;
		for (int r1=0; r1<xx.length; r1++){
			r5+=(r4[r1]-r4m)*(r4[r1]-r4m);
		}
		if (r5<rbest){
			rbest=r5;
			for (int r1=0; r1<4; r1++){
				System.out.print(validParameters[r1]+" ");
			}
			System.out.println(r4m+" "+r5+" "+validTempParameters);
		}
		else{
			for (int r1=0; r1<4; r1++){
				validParameters[r1]=oldvp[r1];
			}
		}
		
		}
		*/
		return scoresH;	
	}
	
	
}


