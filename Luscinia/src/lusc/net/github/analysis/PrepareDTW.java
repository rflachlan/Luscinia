package lusc.net.github.analysis;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

import lusc.net.github.Element;
import lusc.net.github.Song;
import lusc.net.github.ui.SaveDocument;
import lusc.net.github.ui.compmethods.ABXdiscrimination;
import lusc.net.github.ui.compmethods.DTWSwingWorker;
import lusc.net.github.ui.compmethods.DTWPanel;
import lusc.net.github.ui.compmethods.TunerInfo;


public class PrepareDTW {
	
	//double[] normReal;
	//double meanSDTemp=0;
	
	//boolean[][] matel;
	
	int numParams=0;
	int numTempParams=0;
	int numExtraParams=0;
	double[] validParameters, validTempParameters;
	int[] paramType, paramTempType;
	double[][][] data, dataTemp;
	double[][] dataAmp;
	int[][] dataLoc;
	int[][] elementPos, phrasePos;
	double[] sdReal, sdRealStitch, sdRealPhrase;
	double[] sdRealTemp, sdRealStitchTemp, sdRealPhraseTemp;
	
	boolean dynamicWarp=true;
	boolean interpolateWarp=true;
	boolean squaredDist=false;
	
	boolean weightByAmp=false;
	boolean logFrequencies=false;
	boolean normaliseWithSDs=false;
	int stitchSyllables=0;
	double stitchThreshold=30;
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
	TunerInfo tunerInfo;
	DTWPanel dtw;
	boolean tune;
	boolean slopeEnabled=false;
	
	LinkedList<int[][]> syllocs;
	
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
		tunerInfo=dtw.getTunerInfo();
		tune=tunerInfo.getTune();
		stitchThreshold=dtw.getStitchThreshold();
		this.dtw=dtw;
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
	
	public double[][][] getData(){
		return data;
	}
	
	public double[][][] getDataT(){
		return dataTemp;
	}
	
	public double[][] getDataAmp(){
		return dataAmp;
	}
	
	public int[][] getDataLoc(){
		return dataLoc;
	}
	
	public int[][] getElPos(){
		return elementPos;
	}
	
	public double[] getSD(){
		return sdReal;
	}
	
	public double[] getSDTemp(){
		return sdRealTemp;
	}
	
	public double[] getValidParameters(){
		return validParameters;
	}
	
	public double[] getValidTempPar(){
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
		
		for (int i=2; i<21; i++){
			if ((type)&&(parameterValues[i]>0)){numParams++;}
		}
		if ((type)&&(parameterValues[0]>0)){numTempParams++;}
		if ((type)&&(parameterValues[1]>0)){numTempParams++;}
		
		validParameters=new double[numParams];
		paramType=new int[numParams];
		validTempParameters=new double[numTempParams];
		paramTempType=new int[numTempParams];
		numParams=0;
		for (int i=2; i<21; i++){
			if ((type)&&(parameterValues[i]>0)){
				validParameters[numParams]=parameterValues[i];
				paramType[numParams]=i-2;
				numParams++;
			}
		}
		numTempParams=0;
		
		if ((type)&&(parameterValues[0]>0)){
			validTempParameters[0]=parameterValues[0];
			paramTempType[0]=0;
			numTempParams++;
		}
		if ((type)&&(parameterValues[1]>0)){
			validTempParameters[numTempParams]=parameterValues[1];
			paramTempType[numTempParams]=1;
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
		Song[] songs=ag.getSongs();
		for (int i=0; i<songs.length; i++){
			songs[i].compressElements(mainReductionFactor, minPoints, logFrequencies, slopeATanTransform);
		}
	}
	
	public void stitchSyllablesAll(){
		Song[] songs=ag.getSongs();
		int thresh=Integer.MAX_VALUE;
		for (int i=0; i<songs.length; i++){
			songs[i].mergeEleList(thresh);
		}	
	}
	
	
	public void stitchSyllables(){
		Song[] songs=ag.getSongs();
		//stitchThreshold=-100000;
		for (int i=0; i<songs.length; i++){
			songs[i].mergeEleList(stitchThreshold);
		}	
	}
	
	public void extractData(){
		int n=0;
		Song[] songs=ag.getSongs();
		for (int i=0; i<songs.length; i++){
			n+=songs[i].getNumElements2();
		}	
		System.out.println("NUM ELES: "+n);
		dataTemp=new double[n][numTempParams][];
		data=new double[n][numParams][];
		dataAmp=new double[n][];
		dataLoc=new int[n][];
		
		n=0;
		for (int i=0; i<songs.length; i++){
			LinkedList<Element> eles=songs[i].getEleList2();
			for (Element ele: eles){
				dataTemp[n]=ele.extractTempParams(paramTempType);
				
				if (dataTemp[n].length==0){System.out.println("errora"+i);}
				if (dataTemp[n][0].length==0){System.out.println("errorb"+i);}
				
				//System.out.println(dataTemp[n][0][0]+" "+dataTemp[n][0][dataTemp[n][0].length-1]);
				data[n]=ele.extractParams(paramType);
				dataAmp[n]=ele.extractAmpParams();
				dataLoc[n]=ele.extractLocParams();
				ele.setId(n);
				n++;
			}
		}	
	}
	
	
	
	public void prepareToNormalize(){
		sdReal=normalize(data, numParams);
		if (numTempParams>0){
			sdRealTemp=normalize(dataTemp, numTempParams);
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
	
	public double[] normalize(double[][][] data, int np){
	
		int dims=data[0].length;

		double[] average=new double[np];
		double count=0;
		
		for (int i=0; i<data.length; i++){
			int le=data[i][0].length;
			for (int j=0; j<le; j++){		
				for (int k=0; k<np; k++){
					average[k]+=data[i][k][j];
				}
			}
			count+=le;
		}

		for (int i=0; i<np; i++){
			average[i]/=count;
		}
		
		double[] sd=new double[np];
		double[] out=new double[np];
		double w;
		
		for (int i=0; i<data.length; i++){		
			for (int j=0; j<data[i][0].length; j++){
				for (int k=0; k<np; k++){
					w=data[i][k][j]-average[k];
					sd[k]+=w*w;
				}
			}
		}
		
		for (int i=0; i<np; i++){
			out[i]=Math.sqrt(sd[i]/(count-1.0));
			//sdrecord.add(x);
			
			System.out.println(i+" "+out[i]);
		}	
		
		return(out);
	}
	
	public double[][] startDTW(DTWSwingWorker dtws, boolean stitch){
		
		double[][] results=null;
		if (tune){
			results=tuneDTW(dtws, stitch);			
		}
		else{
			results=runDTW(dtws, stitch, true);
		}
		return results;
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
				double[][] scores=runDTW(dtws, stitch, false);
			
			
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
			//System.out.print(name+" ");
			//for (int i=0; i<scores.length; i++){
				//System.out.print((scores[i]/(nsamps+0.00))+" ");
			//}
			//System.out.println();
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
			//System.out.println(choices[i-1]);
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
			//System.out.println(repi+" "+bestScore+" "+prevScore);
			//validParameters=new double[validParameters.length];
			double[] oldvp=new double[validParameters.length];
			for (int repj=0; repj<validParameters.length; repj++){
				oldvp[repj]=validParameters[repj];
				validParameters[repj]=Math.exp(Math.log(validParameters[repj])+random.nextGaussian()*0.2) ;
				if (validParameters[repj]<=0.01){validParameters[repj]=0.01;}
			}
			double[][] scores=runDTW(dtws, stitch, false);
			
			
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

	public int[][] parseExternalData(ComparisonResults cra){
		LinkedList<String[]> data=tunerInfo.getData();
		
		
		int[][]lookUps=cra.getLookUp();
		String[][] sn=cra.getNamesArray(false, false, false, true, false);
		//matel=new boolean[sn.length][sn.length];
		System.out.println(sn.length);
		//public String[] getNames(boolean incspec, boolean incpop, boolean incind, boolean incday, boolean incsong, boolean inctype){

		int nr=data.size()-1;
		
		int[][] combos=new int[nr][7];
		
		Song[] songs=ag.getSongs();
		
		for (int i=1; i<data.size(); i++){
			String[] s=data.get(i);
			
			//-1 means LEFT, 1 means RIGHT B IS ON LEFT (BXA)
			
			
			if (s[0].equals("-1")){combos[i-1][3]=-1;}
			else if (s[0].equals("1")){combos[i-1][3]=1;}		
			else if (s[0].equals("Left")) {combos[i-1][3]=-1;}
			else if (s[0].equals("Right")) {combos[i-1][3]=1;}
			else {System.out.println("CHOICE NOT FOUND! "+i+" "+s[7]);}
			
			//else{System.out.println(s[7]);}
			//int[] els=new int[3];
			for (int j=0; j<3; j++){
				
				String songname=s[7+j].toLowerCase();
				//String songid=s[1+j];
				int sylname=Integer.parseInt(s[4+j])-1;
				
				//System.out.println(i+" "+j+" "+songname+" "+sylname);
				boolean found =false;
				for (int k=0; k<sn.length; k++){
					String s2=sn[k][0].toLowerCase();
					
					if ((s2.equals(songname))&&(lookUps[k][1]==sylname)){
						combos[i-1][j]=k;
						//els[j]=k;
						k=sn.length;
						found=true;
						
					}
				}	
				if (!found) {
					System.out.println("STIMULI NOT FOUND: "+i+" "+songname+" "+sylname);
				}
			}
			//for (int j=0; j<3; j++) {
			//	for (int k=0; k<3; k++) {
			//		matel[els[j]][els[k]]=true;
			//	}
			//}
			System.out.println(combos[i-1][3]+" "+combos[i-1][0]+" "+combos[i-1][1]+" "+combos[i-1][2]);
		}
		return combos;
	}
	
	public int[][][] parseIndividualDataB(){
		
		
		Song[] songs=ag.getSongs();
		
		int[][][] out=new int[songs.length][][];
		
		for (int i=0; i<songs.length; i++){
			LinkedList<int[]> d=new LinkedList<int[]>();
			for (int j=0; j<songs.length; j++){
				if (songs[i].getIndividualID()==songs[j].getIndividualID()){
					for (int k=0; k<songs.length; k++){
						if (songs[i].getIndividualID()!=songs[k].getIndividualID()){	
							int a[] ={j,i,k,1};
							d.add(a);
						}
					}
				}
			}
			out[i]=new int[d.size()][];
			int b=0;
			for (int[] a :d){
				out[i][b]=a;
				b++;
			}
		}
		
		//int[][] out=new int[d.size()][];
		
		return out;
	}
	
	public int[][] parseIndividualData(){
		
		
		Song[] songs=ag.getSongs();
		
		int[][] out=new int[songs.length][];
		LinkedList<int[]> d=new LinkedList<int[]>();
		
		for (int i=0; i<songs.length; i++){
			for (int j=0; j<songs.length; j++){
				if (songs[i].getIndividualID()==songs[j].getIndividualID()){
					for (int k=0; k<songs.length; k++){
						if (songs[i].getIndividualID()!=songs[k].getIndividualID()){	
							int a[] ={j,i,k,1};
							d.add(a);
						}
					}
				}
			}
			
		}
		out=new int[d.size()][];
		int b=0;
		for (int[] a :d){
			out[b]=a;
			b++;
		}
		//int[][] out=new int[d.size()][];
		
		return out;
	}
	
	public int[][] parseIndividualTypeData(){
		
		LinkedList<int[]> d=new LinkedList<int[]>();
		Song[] songs=ag.getSongs();
		for (int i=0; i<songs.length; i++){
			for (int j=0; j<i; j++){
				if ((songs[i].getIndividualID()==songs[j].getIndividualID())&&(songs[i].getType().trim().equalsIgnoreCase(songs[j].getType().trim()))){
					for (int k=0; k<songs.length; k++){
						if (songs[i].getIndividualID()!=songs[k].getIndividualID()){							
							int a[] ={j,i,k,1};
							d.add(a);

						}
					}
				}
			}
		}
		
		int[][] out=new int[d.size()][];
		int b=0;
		System.out.println("Combos: "+out.length);
		for (int[] a :d){
			out[b]=a;
			System.out.println("C: "+a[0]+" "+a[1]+" "+a[2]);
			b++;	
		}
		return out;
	}
	
	public int[][] parseIndividualTypeData(boolean[][] compmat){
		
		LinkedList<int[]> d=new LinkedList<int[]>();
		Song[] songs=ag.getSongs();
		for (int i=0; i<songs.length; i++){
			for (int j=0; j<i; j++){
				if ((songs[i].getIndividualID()==songs[j].getIndividualID())&&(songs[i].getType().trim().equalsIgnoreCase(songs[j].getType().trim()))){
					for (int k=0; k<songs.length; k++){
						if (songs[i].getIndividualID()!=songs[k].getIndividualID()){
							if (compmat[i][k]){
								int a[] ={j,k,i,1};
								d.add(a);
							}
							
						}
					}
				}
			}
		}
		
		int[][] out=new int[d.size()][];
		int b=0;
		for (int[] a :d){
			out[b]=a;
			System.out.println(songs[a[0]].getName()+" "+songs[a[1]].getName()+" "+songs[a[2]].getName());
			b++;
		}
		return out;
	}


	public double[][] tuneDTW(DTWSwingWorker dtws, boolean stitch){
		int[][] combos=null;
		int[][][] combos2=null;
		int type=tunerInfo.getTuneOption();
		boolean[][] compmat=null;
		int level=tunerInfo.getAnalysisLevel();
		
		if (type==0){
			double[][] scoresa=runDTW(dtws, stitch, false);
			ag.setScores(level, scoresa);
			ComparisonResults cra=ag.getScores(level);
			combos=parseExternalData(cra);	
			
			compmat=new boolean[scoresa.length][scoresa.length];
			for (int i=0; i<combos.length; i++) {
				for (int j=0; j<3; j++) {
					for (int k=0; k<3; k++) {
						compmat[combos[i][j]][combos[i][k]]=true;
					}
				}
			}
			System.out.println(cra.getDiss().length+" "+scoresa.length);
			
		}
		else if (type==1){
			combos=parseIndividualData();
		}
		else if (type==2){
		
			double[][] scoresa=runDTW(dtws, stitch, false);
		
			ag.setScores(0, scoresa);
			ag.compressSyllables();
			ag.compressSongs(true, false, false, false, false, 100, 0);
			ComparisonResults cra=ag.getScores(5);
			scoresa=cra.getDiss();
			int nch=5;
			int nc=scoresa.length;
			int[][]minchoices=new int[nc][nch];
		
			double[][] sqm=new double[nc][nc];
			for (int i=0; i<nc; i++){
				for (int j=0; j<nc; j++){
					if (i>j){sqm[i][j]=scoresa[i][j];}
					else{sqm[i][j]=scoresa[j][i];}
				}
			}
		
			boolean[][] compmat2=new boolean[nc][nc];
			double[] p=new double[nc];
			for (int i=0; i<nc; i++){
				System.arraycopy(sqm[i], 0, p, 0, nc);
				Arrays.sort(p);
				for (int j=0; j<nch; j++){
					double x=p[j+1];
					int q=0;
					for (int k=0; k<nc; k++){
						if (sqm[i][k]==x){
							q=k;
						}
					}
					minchoices[i][j]=q;
					compmat2[i][q]=true;
				//System.out.println(i+" "+j+" "+minchoices[i][j]);
				}
			}
		
		
			ComparisonResults ce=ag.getScores(0);
			scoresa=ce.getDiss();
			int ne=scoresa.length;
			compmat=new boolean[ne][ne];
		
		
		
			int[][]lookUp=ce.getLookUp();
			for (int i=0; i<ne; i++){
				int a=lookUp[i][0];
				for (int j=0; j<ne; j++){
					int b=lookUp[j][0];
					boolean found=false;
					for (int k=0; k<nch; k++){
						if (minchoices[a][k]==b){
							found=true;
						}
					}
					compmat[i][j]=found;
				}
			}
		/*
		for (int i=0; i<ne; i++){
			for (int j=0; j<ne; j++){
				System.out.print(compmat[i][j]+" ");
			}
			System.out.println();
		}
			*/	
			combos=parseIndividualTypeData(compmat2);
			System.out.println("FINISHED SORTING DATA: "+combos.length);
		}	
		
		
		//index 0: songA, 1: songB, 2: songX
		//index: 3: choice (L vs R): L: -1, R: 1, No Choice: 0
		//we used BXA design. Left was B (I THINK!). -1 means X was closer to B; 1 means X was closer to A
		
		Random random=new Random(System.currentTimeMillis());
		double bestScore=0;
		double prevScore=Double.NEGATIVE_INFINITY;
		double[][] bestResults=null;
		int nsamps=tunerInfo.getNSamps();
		double[][] results=new double[nsamps][validParameters.length+validTempParameters.length+1];
		int printcount=0;
		for (int repi=0; repi<nsamps; repi++){
			System.out.println("Starting: "+repi);
			int progr=(int)Math.round(100*(repi+1)/(nsamps+0.0));
			dtws.progress(progr);
			
			
			//validParameters=new double[validParameters.length];
			double[] oldvp=new double[validParameters.length];
			double[] oldtp=new double[validTempParameters.length];
			
			double sx=0;
			
			//double oldarctan=slopeATanTransform;
			
			double tuneParam=0.1;

			for (int repj=0; repj<validParameters.length; repj++){
				oldvp[repj]=validParameters[repj];
				validParameters[repj]=Math.exp(Math.log(validParameters[repj])+random.nextGaussian()*tuneParam) ;
				if (validParameters[repj]<=0.000001){validParameters[repj]=0.000001;}
				if (validParameters[repj]>0.4){validParameters[repj]=0.4;}
				sx+=validParameters[repj];
				System.out.print(validParameters[repj]+" ");
			}
			System.out.println();
			
			
			for (int repj=0; repj<validTempParameters.length; repj++){
				oldtp[repj]=validTempParameters[repj];
				//validTempParameters[repj]=1;
				validTempParameters[repj]=Math.exp(Math.log(validTempParameters[repj])+random.nextGaussian()*0.2) ;
				if (validTempParameters[repj]<=0.01){validTempParameters[repj]=0.01;}
				sx+=validTempParameters[repj];
			}
			
			for (int repj=0; repj<validParameters.length; repj++){
				validParameters[repj]/=sx;
			}
			for (int repj=0; repj<validTempParameters.length; repj++){
				validTempParameters[repj]/=sx;
			}
			
			double[][] scores=runDTW(dtws, stitch, false, compmat);
			
			
			ag.setScores(level, scores);
			
			
			ComparisonResults cr=ag.getScores(level);
			scores=cr.getDiss();
			
			
			
		
			//System.out.println("Evaluating score: "+repi);
			double[] sc=null;
			if (type!=1) {
				sc=evaluateScore(scores, combos, random);
			}
			else {
				sc=evaluateScore(scores, combos, random);
			}
			double score=sc[0];
			//if (printcount==10){
				//System.out.println(repi+" "+sc[0]+" "+validTempParameters[0]+" "+validParameters[0]+" "+validParameters[1]+" "+validParameters[2]+" "+validParameters[3]);
				//printcount=0;
			//}
			//printcount++;
			double alph=Math.exp(score-prevScore);
			
			System.out.println("SCORES: "+repi+" "+score+" "+prevScore+" "+alph);
			results[repi][results[repi].length-1]=score;
			if (score<prevScore){		
				if (random.nextDouble()>alph){
					//REJECT
					for (int i=0; i<validParameters.length; i++){
						validParameters[i]=oldvp[i];
					}
					for (int i=0; i<validTempParameters.length; i++){
						validTempParameters[i]=oldtp[i];
					}
					//repi--;
					//prevScore=score;
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
			
			for (int i=0; i<validParameters.length; i++){
				results[repi][i]=validParameters[i];
			}
			for (int i=0; i<validTempParameters.length; i++){
				results[repi][i+validParameters.length]=validTempParameters[i];
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
		
		//System.out.println("Here1");
		SaveDocument sd=tunerInfo.getSD();
		//System.out.println("Here2");
		//boolean readyToWrite=sd.makeFile();
		//System.out.println("Here3");
		for (int i=0; i<results.length; i++){
			sd.writeInt(i+1);
			for (int j=0; j<results[i].length; j++){
				sd.writeDouble(results[i][j]);
					//System.out.print(results[i][j]+", ");
			}
			sd.writeLine();
				//System.out.println();
		}
		
		sd.finishWriting();
		
		System.out.println("ANALYSIS FINISHED! "+bestScore);
		for (int i=0; i<validParameters.length; i++){
			System.out.println(validParameters[i]);
		}
		
		
		
		return bestResults;
	}
	
	public double[][] tuneDTWOld(DTWSwingWorker dtws, boolean stitch){
		
		LinkedList<String[]> data=new LinkedList<String[]>();
		try{
			//Scanner scanner = new Scanner(new File("/Users/Rob/Desktop/ResultsDay5ImacCopy.csv"));
			//Scanner scanner = new Scanner(new File("/Users/Rob/Desktop/ResultsDay4Laptop.csv"));
			Scanner scanner = new Scanner(new File("/Users/Rob/Desktop/PercepResults.csv"));
			while(scanner.hasNext()){
				
				String line=scanner.nextLine();
				String[] fields = line.split(",");
				
				if ((fields.length==11)&&(fields[7]!="0")){
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
				validParameters[repj]=Math.exp(Math.log(validParameters[repj])+random.nextGaussian()*0.05) ;
				if (validParameters[repj]<=0.01){validParameters[repj]=0.01;}
			}
			double[][] scores=runDTW(dtws, stitch, false);
			
			
			for (int i=0; i<validParameters.length; i++){
				results[repi][i]=validParameters[i];
			}
			double[] sc=evaluateScore(scores, combos, choices, random);
			double score=sc[0];
			//System.out.println("SCORES: "+repi+" "+sc[0]+" "+sc[1]+" "+sc[2]);
			double alph=Math.exp(score-prevScore);
			System.out.println("SCORES: "+repi+" "+score+" "+prevScore+" "+alph);
			results[repi][validParameters.length]=score;
			if (score<prevScore){
				
				
				if (random.nextDouble()>alph){
					//REJECT
					for (int i=0; i<validParameters.length; i++){
						validParameters[i]=oldvp[i];
					}
					repi--;
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
			if ((score1<=0)||(score2<=0)) {System.out.println("warning: "+x+" "+a+" "+b+" "+score1+" "+score2);}
			double xp=(score1-score2)/(score1+score2);
			
			//score1>score2... dtw thinks should have chosen score2
			//xp... positive number
			//xq... <0.5
			//score1<score2...  x closer to a than to b... xq>0.5
			
			double xq=1/(1+Math.exp(4*xp));
			//System.out.println(score1+" "+score2+" "+xp+" "+xq+" "+s);
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
	
	public double[] evaluateScore(double[][] scores, int[][] combos, Random random){
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
			if ((score1<=0)||(score2<=0)) {System.out.println("warning: "+x+" "+a+" "+b+" "+score1+" "+score2);}
			
			double xp=(score1-score2)/(score1+score2);
			
			//I predict that score1 should be < score2. xp should be negative.
			
			
			//score1>score2... dtw thinks should have chosen score2...b
			//xp... positive number
			//xq... <0.5
			//score1<score2...  x closer to a than to b... xq>0.5
			
			double xq=1/(1+Math.exp(xp));
			
			//I predict xq ->1
			
			//double xr=random.nextDouble();
			
			//int xs=0;
			//if (xr<xq){xs=1;}
			
			//System.out.println(score1+" "+score2+" "+xp+" "+xq+" "+s);
			
			if (combos[i][3]==1){
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
			
			//if (xs==1){
			//	u+=Math.log(xq);
			//}
			//else{
			//	u+=Math.log(1-xq);
			//}			
		}
		
		return new double[]{s,t};
		
	}
	
	public double[] evaluateScore(double[][] scores, int[][][] combos, Random random){
		double s=0;
		double t=0;
		double u=0;
		//choices==-1 means that B is closer to X than A.
		
		for (int i=0; i<combos.length; i++){
			double s1=0;
			double t1=0;
			double u1=0;
			
			for (int j=0; j<combos[i].length; j++) {
			
				int x=combos[i][j][2];
				int a=combos[i][j][0];
				int b=combos[i][j][1];
			
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
				double xq=1/(1+Math.exp(4*xp));
				double xr=random.nextDouble();
			
				int xs=0;
				if (xr<xq){xs=1;}
			
				if (combos[i][j][3]==1){
					s1+=Math.log(xq);
				}
				else{
					s1+=Math.log(1-xq);
				}
			
				if (xq>0.5){
					t1+=Math.log(xq);
				}
				else{
					t1+=Math.log(1-xq);
				}
			
				if (xs==1){
					u1+=Math.log(xq);
				}
				else{
					u1+=Math.log(1-xq);
				}	
			}
			s+=s1/combos[i].length+0.0;
			t+=t1/combos[i].length+0.0;
			u+=u1/combos[i].length+0.0;
			
		}
		
		return new double[]{s,t, u};
		
	}
	
	
	public synchronized CompareThread2 runDTWpair(DTWSwingWorker dtws, boolean stitch, int id1, int id2){
		
		int eleSize=data.length;
		//if(stitch){
			//eleSize=dataSyls.length;
		//}
		
		
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
		
		//for (int i=0; i<sdReal.length; i++){
		//	System.out.println(i+" "+sdReal[i]);
		//}
		
		System.out.println("LOCS: "+id1+" "+id2);
		double[] sdOver=new double[15];
		
		
		int maxlength=0;
		int e=eleSize;
		//if (stitch){e=dataSyls.length;}
		//if (!stitch){
			for (int lb=0; lb<eleSize; lb++){
				if (data[lb][0].length>maxlength){maxlength=data[lb][0].length;}
			}
		//}
		//else{
			//for (int lb=0; lb<e; lb++){
				//if (dataSyls[lb][0].length>maxlength){maxlength=dataSyls[lb][0].length;}
			//}
		//}		
		
		double[] scoresX=new double[id1+1];
		System.out.println("STARTING COMPARISON");
		CompareThread2 ct=null;
		try{
			ct=new CompareThread2(maxlength, this, id1, id2, true);
			ct.setPriority(Thread.MIN_PRIORITY);
			ct.start();
			ct.join();
		}
		catch (Exception f){
			f.printStackTrace();			
		}

		return ct;	
	}
	
	public synchronized double[][] runDTW(DTWSwingWorker dtws, boolean stitch, boolean prog){
		double[][] scoresH=null;
		
		
		int ncores=Runtime.getRuntime().availableProcessors();
		
		int eleSize=data.length;
		if(stitch){
			eleSize=data.length;
		}
		
		
		int[][] elpos=null;
		
		if(stitch){
			elpos=elementPos;
			
		}
		
		double[][] scores=new double[eleSize][eleSize];
		double[][] scoresX=new double[ncores][eleSize];
		
		CompareThread2 ct[]=new CompareThread2[ncores];
		
		int maxlength=0;
		int e=eleSize;
		for (int lb=0; lb<eleSize; lb++){
			if (data[lb][0].length>maxlength){maxlength=data[lb][0].length;}
		}
		
		int[] starts=new int[ncores];
		int[] stops=new int[ncores];
		for (int i=0; i<ncores; i++){
			starts[i]=i*(e/ncores);
			stops[i]=(i+1)*(e/ncores);
		}
		stops[ncores-1]=e;
		
		
		int f=(int)Math.ceil(e/(ncores+0.0));
		for (int k=0; k<f; k++) {
			if (prog){
				int progr=(int)Math.round(100*(k+1)/f);
				dtws.progress(progr);
			}
			int q=k*ncores;
			for (int cores=0; cores<ncores; cores++){
				int r=q+cores;
				if (r<e) {
					ct[cores]=new CompareThread2(maxlength, this, r, false);
					ct[cores].start();
				}
			}
			
			try{
				for (int cores=0; cores<ncores; cores++){
					int r=q+cores;
					if (r<e) {
						ct[cores].join();
						System.arraycopy(ct[cores].scores, 0, scores[q+cores], 0, e);
					}
				}
			}
			catch (Exception g){
				g.printStackTrace();
			}
			
			
		}
		
		/*
		for (int k=0; k<e; k++){
			
			if (dtws.isCancelled()){
				break;
			}
			if (prog){
				int progr=(int)Math.round(100*(k+1)/eleSize);
				dtws.progress(progr);
			}
		
			for (int cores=0; cores<ncores; cores++){
				//ct[cores]=new CompareThread(maxlength, data1, data2, data3, elpos, sdReal, sdRatio, validParameters, weightByAmp, scoresX[cores], starts[cores], stops[cores], k);
				ct[cores]=new CompareThread(maxlength, this, scoresX[cores], starts[cores], stops[cores], k, false);
				//ct[cores]=new CompareThread(maxlength, this, scoresX[cores], starts[cores], stops[cores], k, true);
				
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
		*/
		scoresH=new double[e][];
		for (int i=0; i<e; i++){
			scoresH[i]=new double[i+1];
			for (int j=0; j<i; j++){
				//scoresH[i][j]=Math.min(scores[i][j], scores[j][i]);
				scoresH[i][j]=Math.max(scores[i][j], scores[j][i]);
				
				//System.out.print(scoresH[i][j]+" ");
				
				//scoresH[i][j]=0.5f*(scores[i][j]+scores[j][i]);
				if ((Double.isNaN(scores[i][j]))||(Double.isNaN(scores[j][i]))){
					System.out.println("NaN TROUBLE:"+" "+scores[i][j]+" "+scores[j][i]);
					//System.out.println(songs[lookUpEls[i][0]].getIndividualName()+" "+songs[lookUpEls[i][0]].getName()+" "+lookUpEls[i][1]);
					//System.out.println(songs[lookUpEls[j][0]].getIndividualName()+" "+songs[lookUpEls[j][0]].getName()+" "+lookUpEls[j][1]);
				}
				
			}
			//System.out.println();
		}
		
		
		return scoresH;	
	}
	
	public synchronized double[][] runDTW(DTWSwingWorker dtws, boolean stitch, boolean prog, boolean[][] mat){
		double[][] scoresH=null;
		
		
		int ncores=Runtime.getRuntime().availableProcessors();
		
		int eleSize=data.length;
		if(stitch){
			eleSize=data.length;
		}
		
		
		int[][] elpos=null;
		
		if(stitch){
			elpos=elementPos;
			
		}
		
		double[][] scores=new double[eleSize][eleSize];
		double[][] scoresX=new double[ncores][eleSize];
		
		CompareThread2 ct[]=new CompareThread2[ncores];
		
		int maxlength=0;
		int e=eleSize;
		for (int lb=0; lb<eleSize; lb++){
			if (data[lb][0].length>maxlength){maxlength=data[lb][0].length;}
		}
		
		int[] starts=new int[ncores];
		int[] stops=new int[ncores];
		for (int i=0; i<ncores; i++){
			starts[i]=i*(e/ncores);
			stops[i]=(i+1)*(e/ncores);
		}
		stops[ncores-1]=e;
		
		
		int f=(int)Math.ceil(e/(ncores+0.0));
		for (int k=0; k<f; k++) {
			if (prog){
				int progr=(int)Math.round(100*(k+1)/f);
				dtws.progress(progr);
			}
			int q=k*ncores;
			for (int cores=0; cores<ncores; cores++){
				int r=q+cores;
				if (r<e) {
					ct[cores]=new CompareThread2(maxlength, this, r, false, mat);
					ct[cores].start();
				}
			}
			
			try{
				for (int cores=0; cores<ncores; cores++){
					int r=q+cores;
					if (r<e) {
						ct[cores].join();
						System.arraycopy(ct[cores].scores, 0, scores[q+cores], 0, e);
					}
				}
			}
			catch (Exception g){
				g.printStackTrace();
			}
			
			
		}

		scoresH=new double[e][];
		for (int i=0; i<e; i++){
			scoresH[i]=new double[i+1];
			for (int j=0; j<i; j++){
				scoresH[i][j]=Math.max(scores[i][j], scores[j][i]);

				if ((Double.isNaN(scores[i][j]))||(Double.isNaN(scores[j][i]))){
					System.out.println("NaN TROUBLE:"+" "+scores[i][j]+" "+scores[j][i]);
				}
				
			}
		}
		
		
		return scoresH;	
	}
	
	public synchronized double[][] runDTWX(DTWSwingWorker dtws, boolean stitch, boolean prog, boolean[][]compmat){
		double[][] scoresH=null;
		
		
		int ncores=Runtime.getRuntime().availableProcessors();
		
		int eleSize=data.length;
		if(stitch){
			eleSize=data.length;
		}
		
		
		int[][] elpos=null;
		
		if(stitch){
			elpos=elementPos;
			
		}
		
		double[][] scores=new double[eleSize][eleSize];
		
		double[][] scoresX=new double[ncores][eleSize];
		
		CompareThread ct[]=new CompareThread[ncores];
		
		int maxlength=0;
		int e=eleSize;
		for (int lb=0; lb<eleSize; lb++){
			if (data[lb][0].length>maxlength){maxlength=data[lb][0].length;}
		}
		
		
		int[] starts=new int[ncores];
		int[] stops=new int[ncores];
		for (int i=0; i<ncores; i++){
			starts[i]=i*(e/ncores);
			stops[i]=(i+1)*(e/ncores);
		}
		stops[ncores-1]=e;
		
		
		for (int k=0; k<e; k++){
			
			if (dtws.isCancelled()){
				break;
			}
			if (prog){
				int progr=(int)Math.round(100*(k+1)/eleSize);
				dtws.progress(progr);
			}
		
			for (int cores=0; cores<ncores; cores++){
				ct[cores]=new CompareThread(maxlength, this, scoresX[cores], starts[cores], stops[cores], k, false, compmat);
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
				scoresH[i][j]=Math.max(scores[i][j], scores[j][i]);
								
				if ((Double.isNaN(scores[i][j]))||(Double.isNaN(scores[j][i]))){
					System.out.println("NaN TROUBLE:"+" "+scores[i][j]+" "+scores[j][i]);
				}
				
			}
		}
		
		
		return scoresH;	
	}
	

	
}


