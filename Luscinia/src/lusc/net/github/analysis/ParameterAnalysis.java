package lusc.net.github.analysis;

import lusc.net.github.Element;
import lusc.net.github.Song;

public class ParameterAnalysis {

	Song[] songs;
	int eleNumber=0;
	double[][] dataSum;
	
	public ParameterAnalysis(Song[] songs, int eleNumber){
		this.songs=songs;
		this.eleNumber=eleNumber;
	}
	
	public void calculateSummaries(boolean[][] parameterMatrix){
		
		int numParams=0;
		for (int i=0; i<parameterMatrix.length; i++){
			for (int j=0; j<parameterMatrix[i].length; j++){
				if (parameterMatrix[i][j]){numParams++;}
			}
		}
	
	
		dataSum=new double[eleNumber][numParams];
		BasicStatistics bs=new BasicStatistics();
		int x=0;
		for (int i=0; i<songs.length; i++){
			int eleSizeS=songs[i].getNumElements();
			for (int j=0; j<eleSizeS; j++){
				Element ele=(Element)songs[i].getElement(j);
				double[][] measu=ele.getMeasurements();
				int y=0;
				for (int a=0; a<14; a++){
					if (parameterMatrix[0][a]){
						dataSum[x][y]=measu[4][a];
						y++;
					}
					if (parameterMatrix[1][a]){
						dataSum[x][y]=measu[0][a];
						y++;
					}
					if (parameterMatrix[2][a]){
						dataSum[x][y]=measu[1][a];
						y++;
					}
					if (parameterMatrix[3][a]){
						dataSum[x][y]=measu[2][a]/(ele.getTimeStep()*ele.getLength());
						y++;
					}
					if (parameterMatrix[4][a]){
						dataSum[x][y]=measu[3][a]/(ele.getTimeStep()*ele.getLength());
						y++;
					}
					if (parameterMatrix[5][a]){
						dataSum[x][y]=measu[5][a];
						y++;
					}
					if (parameterMatrix[6][a]){
						dataSum[x][y]=measu[measu.length-1][a];
						y++;
					}
					if (parameterMatrix[7][a]){
						double[] temp=new double[measu.length-5];
						for (int k=0; k<temp.length; k++){temp[k]=measu[k+5][a];}
						dataSum[x][y]=bs.calculateSD(temp, true);
						y++;
					}
				}
				if (parameterMatrix[0][14]){
					dataSum[x][y]=Math.log(ele.getLength()*ele.getTimeStep());
					y++;
				}
				if (parameterMatrix[0][15]){
					dataSum[x][y]=ele.getTimeAfter();
					if (ele.getTimeAfter()==-10000){
						dataSum[x][y]=20;
					}
				}
				x++;
			}
		}
	}	
	
	public float[][] calculateDistancesFromParameters(boolean[][] parameterMatrix, boolean normalizePerRow){

		int numParams=0;
		int[] countPerType=new int[parameterMatrix[0].length];
		for (int i=0; i<parameterMatrix.length; i++){
			for (int j=0; j<parameterMatrix[i].length; j++){
				if (parameterMatrix[i][j]){
					numParams++;
					countPerType[j]++;
				}
			}
		}
		
		double adjustCount[]=new double[numParams];
		if (normalizePerRow){
			numParams=0;
			for (int i=0; i<parameterMatrix.length; i++){
				for (int j=0; j<parameterMatrix[i].length; j++){
					if (parameterMatrix[i][j]){
						adjustCount[numParams]=countPerType[j];
						numParams++;
					}
				}
			}
		}
		else{
			for (int i=0; i<numParams; i++){
				adjustCount[i]=1;
			}
		}
								
		float[][] results=new float[eleNumber][];
		for (int i=0; i<eleNumber; i++){
			results[i]=new float[i+1];
		}
		
		
		double means[]=new double[numParams];
		
		for (int i=0; i<eleNumber; i++){
			for (int j=0; j<numParams; j++){	
				means[j]+=dataSum[i][j];
			}
		}
		
		for (int i=0; i<numParams; i++){
			means[i]/=eleNumber+0.0;
			System.out.println("Means: "+i+" "+means[i]);
		}
		
		double[] sds=new double[numParams];
		double a,b;
		for (int i=0; i<eleNumber; i++){
			for (int j=0; j<numParams; j++){	
				a=dataSum[i][j]-means[j];
				sds[j]+=a*a;
			}
		}
		
		for (int i=0; i<numParams; i++){
			sds[i]=1/(adjustCount[i]*Math.sqrt(sds[i]/(eleNumber-1.0)));
			System.out.println("SDS: "+i+" "+sds[i]);
		}
		
		
		for (int i=0; i<eleNumber; i++){
			for (int j=0; j<i; j++){
			
				a=0;
				b=0;
				for (int k=0; k<numParams; k++){
					
					b=(dataSum[i][k]-dataSum[j][k])*sds[k];
					a+=b*b;
				}
				results[i][j]=(float)Math.sqrt(a);
			}
		}
		return results;
	}
}
