package lusc.net.github.analysis;

import lusc.net.github.Element;
import lusc.net.github.Song;
import lusc.net.github.ui.compmethods.DTWSwingWorker;
import lusc.net.github.ui.compmethods.DTWPanel;


public class PrepareDTW {
	
	double[] normReal;
	double meanSDTemp=0;
	
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
	
	boolean weightByAmp=false;
	boolean logFrequencies=false;
	boolean normaliseWithSDs=false;
	int stitchSyllables=0;
	int alignmentPoints=3;
	double mainReductionFactor=0.25;
	double maximumWarp=0.25;
	int minPoints=5;
	double minVar=0.2;
	double sdRatio=0.5;
	double offsetRemoval=0.0;
	public double[] parameterValues={1,0,0,0,0,1,0,0,0,1,0,0,0,0,1,0,0, 0, 0,0,0,0,0};
	
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
		minPoints=dtw.getMinPoints();
		alignmentPoints=dtw.getAlignmentPoints();
		interpolateWarp=dtw.getInterpolate();
		dynamicWarp=dtw.getDynamicWarping();
		maximumWarp=dtw.getMaximumWarp();
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
	
	public void setSDRatio(double a){
		sdRatio=a;
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
						}
						else if ((paramType[q]>16)&&(paramType[q]<21)){
							data[p][q][c]+=measu[ab][paramType[q]-17]-measu[4][paramType[q]-17];
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
					
					int q=0;
					while (p[k][q]<0){q++;}
					
					Element ele=(Element)songs[i].getElement(p[k][q]);
					
					double startPos=ele.getBeginTime()*ele.getTimeStep();
					for (int g=0; g<p[k].length; g++){
						if (p[k][g]>=0){
							Element ele2=(Element)songs[i].getElement(p[k][g]);
							
							double adjust=(ele2.getTimeStep()*ele2.getBeginTime())-startPos;
							for (int b=0; b<data[countEls][0].length; b++){
								if(numTempParams>0){
									dataSylsTemp[countSyls][sylLength]=dataTemp[countEls][b]+adjust;
								}
								for (int a=0; a<numParams; a++){
									dataSyls[countSyls][a][sylLength]=data[countEls][a][b];
								}
								for (int a=0; a<numExtraParams; a++){
									dataSylsExtra[countSyls][a][sylLength]=dataExtra[countEls][a][b];
								}
								elementPos[countSyls][sylLength]=g;
								sylLength++;
							}
							countEls++;
						}
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
	
	
	
	
	public synchronized double[][] runDTW(DTWSwingWorker dtws, boolean stitch){
		
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
			System.out.println("STITCHING ENABLED");
		}
		
		double[][] scores=new double[eleSize][eleSize];
		
		for (int i=0; i<sdReal.length; i++){
			System.out.println(i+" "+sdReal[i]);
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
			System.out.println(starts[i]+" "+stops[i]+" "+eleSize);
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
				ct[cores]=new CompareThread(maxlength, this, stitch, scoresX[cores], starts[cores], stops[cores], k);
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
		
		double[][] scoresH=new double[e][];
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
		
		System.out.println("ACTUAL COMPARATORS "+sdT);
		for (int i=0; i<sdReal.length; i++){
			System.out.println(normReal[i]);
		}
		
		
		return scoresH;	
	}
	
	
}


