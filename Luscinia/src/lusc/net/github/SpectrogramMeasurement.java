package lusc.net.github;

//import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.LinkedList;

import lusc.net.github.analysis.BasicStatistics;
//import lusc.net.github.ui.*;
import lusc.net.github.ui.spectrogram.SpectrPane;

/**
 * This Class measures elements within spectrograms - measuring a range of acoustic parameters
 * It interacts closely with the {@link Song} class.
 * @author Rob
 *
 */
public class SpectrogramMeasurement {

	Song song;
	float [][] nout;
	double dy, dx;
	int ny;
	int minFreq=5;
	double octstep=10;
	/**
	 * This constructor builds from a {@link Song} object
	 * @param song
	 */
	public SpectrogramMeasurement(Song song){
		this.song=song;
		this.dy=song.dy;
		this.dx=song.dx;
		this.ny=song.ny;
	}
	
	/**
	 * This is a deprecated method that carries out a Gaussian blur. It should be moved
	 * to Matrix2DOperations, if it is to be kept.
	 * @param data
	 */
	void gaussianBlur(double[][] data){
		double vari=2;
		float[][] kernel=new float[5][5];
		for (int i=0; i<=4; i++){
			int ii=i-2;
			for (int j=0; j<=4; j++){
				int jj=j-2;
				kernel[i][j]=(float)Math.exp(-1*ii*ii/vari);
				kernel[i][j]*=(float)Math.exp(-1*jj*jj/vari);
			}
		}
		
		int dim1=data.length; 
		int dim2=data[0].length;
	
		double[][]out=new double[dim1][dim2];
		double tot=0;
		int g,h,i,j,gg,hh;
		double[] av=new double[dim1];
		for (i=0; i<dim1; i++){
			for (j=0; j<dim2; j++){
				tot=0;
				for (g=0; g<=4; g++){
					gg=i+g-2;
					if ((gg>=0)&&(gg<dim1)){
						for (h=0; h<=4; h++){
							hh=j+h-2;
							if((hh>=0)&&(hh<dim2)){
								out[i][j]+=data[gg][hh]*kernel[g][h];
								tot+=kernel[g][h];
							}
						}
					}
				}
				out[i][j]/=tot;
				av[i]+=out[i][j];
			}
			
		}
		for (i=0; i<dim1; i++){
			av[i]/=dim2+0.0;
		}
		for (i=0; i<dim1; i++){
			for (j=0; j<dim2; j++){
				//data[i][j]-=out[i][j]-av[i];
				
				data[i][j]=out[i][j];
				
			}
		}
		out=null;
		kernel=null;
	}
	
	/**
	 * This method sets up the nout array which is essential for measurement.
	 */
	public void setUp(){
		nout=song.getOut();
	}
	
	
	/**
	 * This method uses a hysteresis loop method to search for a signal within a highlighted
	 * region of a spectrogram
	 * @param pointList a highlighted region of the spectrogram. Lists min and max freqs
	 * for a range of times
	 * @param unx length of the spectrogram (shouldn't be necessary!)
	 * @return a LinkedList of int[][] containing signal locations
	 */
	public LinkedList<int[][]> getSignal(int[] pointList1, int[] pointList2, int unx, boolean detectHarmonics){						
		setUp();
		//This method identifies which parts of the highlighted sound are loud enough to be "signal"
		//and joins them together on the basis of temporal contiguity into elements (further lumping/splitting
		//of elements can happen in later methods)
		LinkedList<int[][]> tList=new LinkedList<int[][]>();
		
		
		
		int minx=0;
		while (pointList1[minx]==0){minx++;}
		int maxx=unx-2;								//this bit identifies the start and end of the element
		while (pointList1[maxx]==0){maxx--;}
		maxx++;
	
		int p1;
		for (int i=minx; i<maxx; i++){
			p1=ny-pointList1[i]-1;
			pointList1[i]=ny-pointList2[i]-1;
			pointList2[i]=p1;
			if (pointList2[i]==ny-1){pointList2[i]--;}
			if (pointList1[i]==0){pointList1[i]++;}
		}
	
		int ou[][]=new int[ny][unx];
		//int the2list[][]=new int [ny*(maxx-minx)][2];
		LinkedList<int[]> the2list=new LinkedList<int[]>();
	
		double focal;
		float c=(float)(1/song.dynMax);
		double u1=song.upperLoop;
		double l1=song.lowerLoop;
	
		//System.out.println("loop "+u1+" "+l1);
		
		for (int j=minx; j<maxx; j++){
			//System.out.println("POINT RANGE: "+pointList1[j]+" "+pointList2[j]);
			for (int i=pointList1[j]; i<pointList2[j]; i++){
				focal=nout[i][j]*c;
				if (focal>u1){
					//int[] new2={i, j};
					//the2list.add(new2);
					ou[i][j]=2;
				}
				else if (focal>l1){ou[i][j]=1;}	
				else{ou[i][j]=0;}
			}
			
			if (detectHarmonics) {
				for (int h=2; h<10; h++) {
					
					double h2=0.5*(pointList1[j]+pointList2[j]);
					double h3= 0.5*(pointList2[j]-pointList1[j]);
					int h4=(int)Math.round(h2*h-h3);
					int h5=(int)Math.round(h2*h+h3);	//This code keeps the bands roughly even widths...
					if (h5>ny-5) {h5=ny-5;}
					for (int i=h4; i<h5; i++){
						//System.out.println(i+" "+ny);
						focal=nout[i][j]*c;
						if (focal>u1){
								//int[] new2={i, j};
								//the2list.add(new2);
							ou[i][j]=2;
						}
						else if (focal>l1){ou[i][j]=1;}	
						else{ou[i][j]=0;}
					}
				}
			}
			
		}
	
		int i,j,k, a,b, ia, jb, ii, jj;
	
	
		int counter=3;
	
		for (j=minx; j<maxx; j++){
			for (i=0; i<ny; i++){
				if (ou[i][j]==2){
					int[]n3={i, j};
					ou[i][j]=counter;
					the2list=new LinkedList<int[]>();
					the2list.add(n3);
					while(the2list.size()>0){
						int[] n2=the2list.get(0);
						the2list.remove(0);
						ii=n2[0];
						jj=n2[1];
						for (a=-1; a<=1; a++){
							ia=ii+a;
							for (b=-1; b<=1; b++){
								jb=jj+b;
								if ((ia>=0)&&(ia<ny)&&(jb>=0)&&(jb<unx)){
									if ((ou[ia][jb]==1)||(ou[ia][jb]==2)){
										ou[ia][jb]=counter;
										int[]n4={ia, jb};
										the2list.add(n4);
									}
								}
							}
						}
					}
					counter++;
				}
			}
		}
	
		int numEls=counter-3;
		System.out.println("NUMBER ELS DISCOVERED: "+numEls);
		int[]starts=new int[numEls];
		int[]ends=new int[numEls];
		boolean[] exists=new boolean[numEls];
		for (k=0; k<numEls; k++){
			int mx1=-1;
			int mx2=0;
			for (j=minx; j<maxx; j++){
				for (ii=0; ii<ny; ii++){
					if (ou[ii][j]==k+3){
						if (mx1==-1){
							mx1=j;
						}
						mx2=j;
					}
				}
			}
			mx2++;
			starts[k]=mx1;
			ends[k]=mx2;
			exists[k]=true;
		}
	
		double gapThresh=song.minGap/song.timeStep;
	
		for (k=0; k<numEls; k++){
			//System.out.println(numEls);
			if (exists[k]){
				for (j=0; j<k; j++){
					if (exists[j]){
						boolean merge=false;
	
						if ((starts[j]>starts[k]-gapThresh)&&(starts[j]<=ends[k]+gapThresh)){
							merge=true;
						}
						if ((starts[k]>starts[j]-gapThresh)&&(starts[k]<=ends[j]+gapThresh)){
							merge=true;
						}
	
						if (merge){
							exists[j]=false;
							for (jj=minx; jj<maxx; jj++){
								for (ii=0; ii<ny; ii++){
									if (ou[ii][jj]==j+3){
										ou[ii][jj]=k+3;
									}
								}
							}
						}
					}
				}
			}
		}
	
	
		for (k=0; k<numEls; k++){
			int mx1=-1;
			int mx2=0;
			for (j=minx; j<maxx; j++){
				for (ii=pointList1[j]; ii<ny; ii++){
					if (ou[ii][j]==k+3){
						if (mx1==-1){
							mx1=j;
						}
						mx2=j;
					}
				}
			}
			if (mx1>-1){
				mx2++;
				int[][] data=new int[mx2-mx1][];
	
				int nullCount=0;
	
				for (j=mx1; j<mx2; j++){
					int cc=0;
					for (ii=pointList1[j]; ii<ny; ii++){
						//System.out.print(ou[ii][j]+" ");
						if ((ou[ii][j]==k+3)&&(ou[ii-1][j]!=k+3)){
							cc++;
						}
						if ((ou[ii-1][j]==k+3)&&(ou[ii][j]!=k+3)){
							cc++;
						}
					}
					if (ou[ny-1][j]==k+3){
						cc++;
					}
	
					jj=j-mx1;
					data[jj]=new int[cc+1];
					data[jj][0]=j;
	
					if (cc==0){nullCount++;}
	
					//System.out.println(cc);
	
					cc=1;
	
					for (ii=pointList1[j]; ii<ny; ii++){
						if ((ou[ii][j]==k+3)&&(ou[ii-1][j]!=k+3)){
							data[jj][cc]=ii;
							cc++;
						}
						if ((ou[ii-1][j]==k+3)&&(ou[ii][j]!=k+3)){
							data[jj][cc]=ii;
							cc++;
						}
					}
					if (ou[ny-1][j]==k+3){
						data[jj][cc]=pointList2[j];
					}
				}
				
				int[][] data2=new int[data.length-nullCount][];
				ii=0;
				for (j=0; j<data.length; j++){
					if (data[j].length>1){
						data2[ii]=new int[data[j].length];
						System.arraycopy(data[j],0, data2[ii],0,data[j].length);
						ii++;
					}
				}
				tList.add(data2);
			}
		}
	
	
	
		//Format of tList at this point: LinkedList containing int[][]'s. First dimension of int[][] is time. Second dimension frequencies.
		//First slot filled with time. Next two frequency slots are left blank for now (will be filled with: peak freq, fund freq below). Then, there are a variable
		//number of pairs: min, max; min, max etc. that represent minima and maxima for "chunks" of signal. e.g a tonal signal will have just one
		//min and max pair, while a harmonic signal will have multiple pairs one for each harmonic.
		
		
		
		return tList;	
	}
	
	
	/**
	 * This method erases marked parts of elements....
	 * @param pointList a highlighted region of the spectrogram. Lists min and max freqs
	 * for a range of times
	 * @param unx length of the spectrogram (shouldn't be necessary!)
	 * @return a LinkedList of int[][] containing signal locations
	 */
	public LinkedList<int[][]> eraseSignal(int[] pointList1, int[] pointList2, int currentMinX){						
		setUp();
		
		LinkedList<int[][]> alteredElements=new LinkedList<int[][]>();
		LinkedList<Element> removeList=new LinkedList<Element>();
		int minx=0;
		while (pointList1[minx]==0){minx++;}
		int maxx=pointList1.length-2;								//this bit identifies the start and end of the element
		while (pointList1[maxx]==0){maxx--;}
		maxx++;
	
		int p1;
		for (int i=minx; i<maxx; i++){
			p1=ny-pointList1[i]-1;
			pointList1[i]=ny-pointList2[i]-1;
			pointList2[i]=p1;
			if (pointList2[i]==ny-1){pointList2[i]--;}
			if (pointList1[i]==0){pointList1[i]++;}
		}
		
		for (Element ele : song.eleList){
			int start=ele.getBeginTime()-currentMinX;
			int end=ele.getLength()+start;
			
			//System.out.println("CHECKING: "+start+" "+end+" "+minx+" "+maxx);
			
			if (((start<minx)&&(end>minx))||((start>=minx)&&(start<maxx))){
				//System.out.println("PROCESSING");
				
				removeList.add(ele);
				
				int[][] signal=ele.getSignal();
				
				LinkedList<boolean[]> cols=new LinkedList<boolean[]>();
				
				
				for (int j=0; j<signal.length; j++){
					int y=0;
					boolean[] col=new boolean[ny];
					for (int k=0; k<ny; k++){col[k]=false;}
					
					
					//System.out.println(signal[j].length);
					for (int k=1; k<signal[j].length; k+=2){
						//System.out.println(j+" "+k+" "+signal[j][k]+" "+signal[j][k+1]);
						for (int a=ny-signal[j][k]; a<ny-signal[j][k+1]; a++){
							col[a]=true;
							y++;
						}	
					}
					//System.out.println(y);
					for (int i=minx; i<maxx; i++){
						int x=i+currentMinX;				
						if (signal[j][0]==x){
							for (int k=pointList1[i]; k<pointList2[i]; k++){
								if (col[k]){
									y--;
									col[k]=false;
								}	
							}
						}
					}
					//System.out.println(y);		
					if (y>0){
						cols.add(col);
					}
					if ((y==0)||(j==signal.length-1)){
						int[][] sig=new int[cols.size()][];
						
						for (int i=0; i<cols.size(); i++){
							boolean[] col2=cols.get(i);
							
							int jj=j;
							if ((y==0)&&(j>0)){jj--;}
							
							int xloc=signal[jj][0]-cols.size()+i-currentMinX+1;
							
							//System.out.println(y+" "+j+" "+signal.length+" "+cols.size()+" "+i);
							//System.out.println(xloc+" "+signal[i][0]);
							
							int segs=0;
							for (int k=0; k<ny-1; k++){
								if ((!col2[k])&&(col2[k+1])){
									segs++;
								}
							}
							
							sig[i]=new int[1+2*segs];
							//System.out.println("NEW SIG: "+sig.length+" "+i+" "+sig[i].length);
							sig[i][0]=xloc;
							int a=1;
							for (int k=0; k<ny-1; k++){
								if (col2[k]!=col2[k+1]){
									sig[i][a]=k;
									a++;
								}		
							}								
						}
						cols.removeAll(cols);		
						alteredElements.add(sig);
					}
				}		
			}	
		}
		song.eleList.removeAll(removeList);
		return alteredElements;
	
	}
	
	/**
	 * This method attempts to merge two measured elements into one - based on a user decision
	 * to do so. This is currently a bit buggy, and needs some investigation
	 * @param p the index of the first element to merge in the eleList
	 * @param sp the {@link lusc.net.github.ui.spectrogram.SpectrPane} object that called merge
	 * @param currentMinX current minimum time on the spectrogram
	 */
	public void merge(int p, SpectrPane sp, int currentMinX){
		Element ele1=(Element)song.getElement(p);
		Element ele2=(Element)song.getElement(p+1);
		
		
		int diff=ele2.signal[0][0]-ele1.signal[ele1.signal.length-1][0];
				
		if (diff>0){
			song.eleList.remove(p);
			song.eleList.remove(p);
			Element ele=new Element(ele1, ele2);
			double q=ele.begintime+0.5*(ele.length);
			int count=0;
			for (int j=0; j<song.eleList.size(); j++){
				Element ele3=(Element)song.eleList.get(j);
				double p2=ele3.begintime+0.5*(ele3.length);
				if (p2<q){count++;}
				else{j=song.eleList.size();}
			}
			song.eleList.add(count, ele);
		}
		
		
		else{
			for (int i=0; i<ele1.signal.length; i++){
				for (int j=1; j<ele1.signal[i].length; j++){
					ele1.signal[i][j]=ny-1-ele1.signal[i][j];
				}
				ele1.signal[i][0]-=currentMinX;
			}
			for (int i=0; i<ele2.signal.length; i++){
				for (int j=1; j<ele2.signal[i].length; j++){
					ele2.signal[i][j]=ny-1-ele2.signal[i][j];
				}
				ele2.signal[i][0]-=currentMinX;
			}

			if ((ele1.signal[0][0]>0)&&(ele2.signal[0][0]>0)&&(ele1.signal[ele1.signal.length-1][0]<nout[0].length)&&(ele2.signal[ele2.signal.length-1][0]<nout[0].length)){
				song.eleList.remove(p);
				song.eleList.remove(p);
			
					
				int tot=ele1.length+ele2.length;
				
				for (int i=0; i<ele1.signal.length; i++){
					for (int j=0; j<ele2.signal.length; j++){
						if (ele1.signal[i][0]==ele2.signal[j][0]){
							tot--;
						}
					}
				}
				
				
				int[][] signal=new int[tot][];
				for (int i=0; i<ele1.signal.length; i++){
					signal[i]=new int[ele1.signal[i].length];
					for (int j=0; j<ele1.signal[i].length; j++){
						signal[i][j]=ele1.signal[i][j];
					}
				}
				
				int ii=ele1.signal.length;
				for (int i=0; i<ele2.signal.length; i++){
					int loc=-1;
					for (int j=0; j<ele1.signal.length; j++){
						if (ele1.signal[j][0]==ele2.signal[i][0]){
							loc=j;
							j=ele1.signal.length;
						}
					}
					
					if (loc==-1){
						signal[ii]=new int[ele2.signal[i].length];
						for (int j=0; j<ele2.signal[i].length; j++){
							signal[ii][j]=ele2.signal[i][j];
						}
						ii++;
					}

			
					else{
						boolean[] board=new boolean[ny];
						for (int j=1; j<ele1.signal[loc].length; j+=2){
							for (int k=ele1.signal[loc][j]; k<ele1.signal[loc][j+1]; k++){
								board[k]=true;
							}
						}
						for (int j=1; j<ele2.signal[i].length; j+=2){
							for (int k=ele2.signal[i][j]; k<ele2.signal[i][j+1]; k++){
								board[k]=true;
							}
						}
						boolean c=false;
						int count=0;
						for (int j=0; j<ny; j++){
							if (board[j]!=c){
								count++;
								c=!c;
							}
						}
						signal[loc]=new int[count+1];
						signal[loc][0]=ele1.signal[loc][0];
						c=false;
						count=1;
						for (int j=0; j<ny; j++){
							if (board[j]!=c){
								signal[loc][count]=j;
								count++;
								c=!c;
							}
						}
					}
				}
				LinkedList<int[][]> holder=new LinkedList<int[][]>();
				holder.add(signal);
				measureAndAddElements(holder, sp, currentMinX);
			}
		}
	}

	/**
	 * This is the organizing method for measuring acoustic parameters of new elements, and
	 * constructing new Element objects from the measurements.
	 * @param signals a list of new elements, described by their locations on the spectrograms
	 * @param sp the {@link lusc.net.github.ui.spectrogram.SpectrPane} object that called this
	 * @param currentMinX current minimum time value of the spectrogram.
	 * @return an int containing the number of added elements
	 */
	public int measureAndAddElements(LinkedList<int[][]> signals, SpectrPane sp, int currentMinX){
		LinkedList<double[][]> freqList=new LinkedList<double[][]>();
		LinkedList<double[][]> freqChangeList=new LinkedList<double[][]>();
		LinkedList<double[]> harmList=new LinkedList<double[]>();
		LinkedList<double[]> wienerList=new LinkedList<double[]>();
		LinkedList<int[]> bandwidthList=new LinkedList<int[]>();
		LinkedList<double[]> ampList=new LinkedList<double[]>();
		LinkedList<double[]> powerList=new LinkedList<double[]>();
		LinkedList<double[][]> trillList=new LinkedList<double[][]>();
		measureFrequencies(signals, freqList);
		measureFrequencyChange4(signals, freqList, freqChangeList);
		measureTrills(freqList, trillList);
		measureHarmonicity(signals, freqList, harmList);
		measureWienerEntropy(signals, wienerList);
		measureBandwidth(signals, bandwidthList);
		measureAmplitude(freqList, signals, ampList);
		calculatePowerSpectrum(signals, powerList);
		LinkedList<double[][]> measures=new LinkedList<double[][]>();
		for (int i=0; i<freqList.size(); i++){
			
			
			double[] harmonicity=harmList.get(i);
			double[] wiener=wienerList.get(i);
			int[] bandwidth=bandwidthList.get(i);
			double[] amplitude=ampList.get(i);
			double[][] freqChange=freqChangeList.get(i);
			double[][] freq=freqList.get(i);
			double[][] trill=trillList.get(i);
			double[][] measureP=new double[harmonicity.length][15];
			for (int j=0; j<harmonicity.length; j++){
				measureP[j][0]=freq[j][0]*dy;
				measureP[j][1]=freq[j][1]*dy;
				measureP[j][2]=freq[j][2]*dy;
				measureP[j][3]=freq[j][3]*dy;
				measureP[j][4]=freqChange[j][0];
				measureP[j][5]=freqChange[j][1];
				measureP[j][6]=freqChange[j][2];
				measureP[j][7]=freqChange[j][3];
				measureP[j][8]=harmonicity[j];
				measureP[j][9]=wiener[j];
				measureP[j][10]=bandwidth[j]*dy;
				measureP[j][11]=amplitude[j];
				measureP[j][12]=trill[j][2];
				measureP[j][13]=trill[j][0];
				measureP[j][14]=trill[j][1];
			}
			measures.add(measureP);
		}		
		freqList=null;
		freqChangeList=null;
		harmList=null;
		wienerList=null;
		ampList=null;
		bandwidthList=null;
		fixTimesAndFreqs(signals, currentMinX);
		int numberElementsAddedLastTime=signals.size();
		
		
		int[] archiveLastElementsAdded=new int[numberElementsAddedLastTime];
		
		for (int i=0; i<numberElementsAddedLastTime; i++){
			int[][]t=signals.get(i);
			double[][]t2=measures.get(i);
			double[]t3=powerList.get(i);
			Element ele=new Element(song, t, t2, t3);
			ele.calculateStatistics();
			double p=ele.begintime+0.5*(ele.length);
			int count=0;
			for (int j=0; j<song.eleList.size(); j++){
				Element ele3=(Element)song.eleList.get(j);
				double p2=ele3.begintime+0.5*(ele3.length);
				if (p2<p){count++;}
				else{j=song.eleList.size();}
			}
			song.eleList.add(count, ele);
			archiveLastElementsAdded[i]=count;
		}
		
		if (sp!=null) {
			sp.archiveElements(archiveLastElementsAdded);
		}
		
		signals=null;
		measures=null;
		return numberElementsAddedLastTime;
	}
	
	/**
	 * Deprecated
	 * @param tList
	 * @param sigList
	 */
	void getSignalLoc(LinkedList<int[][]> tList, LinkedList<boolean[][]> sigList){
		int elNum=tList.size();
		for (int i=0; i<elNum; i++){
			int[][]data=tList.get(i);
			int eleLength=data.length;
			boolean[][] sig=new boolean[eleLength][ny];
			for (int j=0; j<eleLength; j++){
				for (int k=0; k<ny; k++){sig[j][k]=false;}
				int chunkNum=data[j].length;
				int time=data[j][0];
				//float peakF=-100000;
				//float sumF=0;
				//int peakFLoc=0;
				for (int k=1; k<chunkNum; k+=2){
					for (int a=data[j][k]; a<data[j][k+1]; a++){
						if (nout[a][time]>0){
							sig[j][a]=true;
						}
					}
				}
			}
			sigList.add(sig);
		}
	}




	/**
	 * This method fixes the times of measured elements from ones relative to the spectrogram
	 * to ones relative to the whole song. Similarly it fixes frequency measures
	 * @param signals input signal location
	 * @param currentMinX current minimum time of the spectrogram
	 */
	private void fixTimesAndFreqs(LinkedList<int[][]> signals, int currentMinX){

		int elNum=signals.size();

		for (int i=0; i<elNum; i++){
			int[][]data=signals.get(i);
			int eleLength=data.length;
			for (int j=0; j<eleLength; j++){
				data[j][0]+=currentMinX;
				for (int k=1; k<data[j].length; k++){
					data[j][k]=ny-data[j][k]-1;
				}
			}

			signals.remove(i);
			signals.add(i, data);
		}
	}


	/**
	 * This method calculates a pseudo-power spectrum by summing spectrogram values for an element
	 * @param signal location of an element
	 * @param results LinkedList into which to insert the results
	 */
	public void calculatePowerSpectrum(LinkedList<int[][]> signal, LinkedList<double[]> results){

		//this method measures the power spectrum of a discovered element.
		for (int i=0; i<signal.size(); i++){
			int[][] ele=signal.get(i);
			double[] powerSpectrum=new double[song.ny];
			int j=0;
			int a=ele.length;
			//double logd=Math.log(10)*0.1;
			//double maxC=song.dynRange-Math.log(song.maxPossAmp)*logd;

			for (j=0; j<ele.length; j++){
				int b=ele[j][0];
				for (int k=1; k<ele[j].length; k+=2){
					//System.out.println(b+" "+ele[j][k]+" "+ele[j][k+1]);
					for (int l=ele[j][k]; l<ele[j][k+1]; l++){

						powerSpectrum[l]+=song.out1[l][b];
					}
				}
			}

			//logd=10/(Math.log(10));

			for (int k=0; k<powerSpectrum.length; k++){
				powerSpectrum[k]/=a+0.0;
				//if (powerSpectrum[k]>0){powerSpectrum[k]=Math.log(powerSpectrum[k]*powerSpectrum[k])*logd+maxC;}
				//System.out.println(powerSpectrum[k]);
			}
			results.add(powerSpectrum);
		}
	}

	/**
	 * An algorithm to attempt to smooth fundamental frequency measures - preventing
	 * ff values from jumping octaves.
	 * @param score input frequency measures
	 * @param amps amplitudes along the trajectory
	 * @param harmlim a parameter related to jump suppression 
	 * @return an int[] of smoothed pitch trajectory
	 */
	int[] smoothPitch(double[][] score, double[]amps, int harmlim){

		int y=score[0].length;
		int x=score.length;

		double besttrajsc=-1000000;
		int[] besttraj=new int[x];
		int[] traj=new int[x];

		for (int i=0; i<x; i++){

			int loc=0;
			double bests=-1000000;
			for (int j=0; j<y; j++){
				if (score[i][j]>bests){
					bests=score[i][j];
					loc=j;
				}
			}

			int c=loc;
			double trajsc=bests;
			traj[i]=loc;
			for (int j=i-1; j>=0; j--){
				int a=c-harmlim;
				if (a<0){a=0;}
				int b=c+harmlim;
				if (b>=score[j].length){b=score[j].length-1;}
				int aloc=0;
				double abests=-1000000;
				for (int k=a; k<=b; k++){
					if (score[j][k]>abests){
						abests=score[j][k];
						aloc=k;
					}
				}
				c=aloc;
				traj[j]=aloc;
				trajsc+=abests;
			}
			c=loc;
			for (int j=i+1; j<x; j++){
				int a=c-harmlim;
				if (a<0){a=0;}
				int b=c+harmlim;
				if (b>=score[j].length){b=score[j].length-1;}
				int aloc=0;
				double abests=-1000000;
				for (int k=a; k<=b; k++){
					if (score[j][k]>abests){
						abests=score[j][k];
						aloc=k;
					}
				}
				c=aloc;
				traj[j]=aloc;
				trajsc+=abests;
			}
			if (trajsc>besttrajsc){
				besttrajsc=trajsc;
				System.arraycopy(traj, 0, besttraj, 0, x);
			}
		}
		return besttraj;
	}


	/**
	 * Deprecated
	 * @param score
	 * @param rpitch
	 * @param amps
	 * @return an int[] of smoothed pitch
	 */
	int[] smoothPitch(double[][] score, int[]rpitch, double[]amps){

		int size=rpitch.length;
		//int corramt=(int)Math.round(20/dx);
		int corramt=10;
		int i,j, placex, placey, dir, x, y, cut;
		double bestdir;
		double[][] scores=new double[size][ny];
		//for (i=0; i<size; i++){
		//	scores[i]=new double[score[i].length];
		//}
		double temp;
		for (i=0; i<size; i++){
			for (j=0; j<score[i].length; j++){
				scores[i][j]+=score[i][j]*amps[i];
			}
			placex=i;
			placey=rpitch[i];

			cut=i+corramt;
			if (cut>size-1){cut=size-1;}

			while((placex<cut)&&(placey<score[placex].length)&&(score[placex][placey]>0)){
				x=placex+1;
				dir=rpitch[x];
				bestdir=-1000000000;
				for (j=0; j<9; j++){
					y=placey+j-4;
					if ((y<score[x].length)&&(y>=0)){
						if (score[x][y]>bestdir){
							bestdir=score[x][y];
							dir=y;
						}
					}
				}
				placex++;
				placey=dir;
				temp=score[i][rpitch[i]]*amps[i];
				scores[placex][placey]+=temp;
			}
			placex=i;
			placey=rpitch[i];

			cut=i-corramt;
			if (cut<0){cut=0;}

			while((placex>cut)&&(placey<score[placex].length)&&(score[placex][placey]>0)){
				x=placex-1;
				dir=rpitch[x];
				bestdir=-1000000000;
				for (j=0; j<9; j++){
					y=placey+j-4;
					if ((y<score[x].length)&&(y>=0)){
						if (score[x][y]>bestdir){
							bestdir=score[x][y];
							dir=y;
						}
					}
				}
				placex--;
				placey=dir;
				scores[placex][placey]+=score[i][rpitch[i]]*amps[i];
			}
			placex=i;
			placey=rpitch[i];
		}


		double max;
		int loc;
		for (i=0; i<size; i++){
			max=-100;
			loc=0;
			for (j=0; j<scores[i].length; j++){
				if (scores[i][j]>max){
					max=scores[i][j];
					loc=j;
				}
			}
			rpitch[i]=loc;
		}

		return rpitch;
	}
	
	
	
	/**
	 * Method to measure four frequency trajectories of an element
	 * @param tList list (int[][]) of input signal locations
	 * @param freqList output list (double[][]) of measured frequency trajectories
	 */
	private void measureFrequencies(LinkedList<int[][]> tList, LinkedList<double[][]> freqList){
		
		double[] phase=song.getPhase();
		
		int elNum=tList.size();
		for (int i=0; i<elNum; i++){
			int[][]data=tList.get(i);
			int eleLength=data.length;
			double [][] freqMeasures=new double[eleLength][4];
			//Following section measures the peak frequency within the signal at each time point within the signal, puts it in data[x][1]
			
			double [] amp=new double[eleLength];
			double []peakamp=new double[eleLength];
			for (int j=0; j<eleLength; j++){
				int chunkNum=data[j].length;
				int time=data[j][0];
				float peakF=-100000;
				float sumF=0;
				int peakFLoc=0;
				for (int k=1; k<chunkNum; k+=2){
					for (int a=data[j][k]; a<data[j][k+1]; a++){
						if (nout[a][time]>0){
							amp[j]+=nout[a][time];
							sumF+=nout[a][time]*a;
							if (nout[a][time]>peakF){
								peakF=nout[a][time];
								peakFLoc=a;
							}
						}
					}
				}
				freqMeasures[j][0]=peakFLoc;
				peakamp[j]=peakF;
				freqMeasures[j][1]=sumF/amp[j];
				
				double medianThreshold=amp[j]*0.5;
				if (amp[j]<0){
					medianThreshold=amp[j]*2;
				}
				double medianCount=0;
				boolean finished=false;
				for (int k=1; k<chunkNum; k+=2){
					for (int a=data[j][k]; a<data[j][k+1]; a++){
						medianCount+=Math.max(0, nout[a][time]);
						if ((nout[a][time]>0)&&(medianCount>medianThreshold)){
							freqMeasures[j][2]=a+((medianCount-medianThreshold)/nout[a][time]);
							finished=true;
						}
						if (finished){a=data[j][k+1];}
					}
					if (finished){k=chunkNum+1;}
				}
			}
			
			
			//Following section estimates the fundamental frequency at each time point, puts it in data[x][2]
			
			int x=0;
			//double ratioAdjust=song.fundAdjust;
			double subSuppression=song.fundAdjust;
			int harmlimit=(int)Math.round(0.5*song.fundJumpSuppression);
			
			double[][]hscor=new double[eleLength][ny];
			double[] logTransform=new double[ny];
			double log2Adj=1/Math.log(2);
			double logMax=Math.log(ny-1)*log2Adj;
			double logMin=Math.log(minFreq)*log2Adj;
			double step=(logMax-logMin)/(ny+0.0);

			for (int j=0; j<ny; j++){
				logTransform[j]=Math.pow(2, logMin+(j*step));
			}
			
			double[] spectrum=new double[ny];
			int rpitch[]=new int[eleLength];
			double scmax=0;
			//int loc=0;
			for (int j=0; j<eleLength; j++){
				x=data[j][0];
				scmax=-100000;
				//int maxP=data[j][data[j].length-1];
				int chunkNum=data[j].length;
				spectrum=new double[ny];
				/*
				for (int a=0; a<ny; a++){
					spectrum[a]=nout[a][x];
					if (spectrum[a]<0){
						spectrum[a]=0;
					}
					spectrum[a]=Math.pow(spectrum[a], subSuppression);
				}
				*/
				for (int a=1; a<chunkNum; a+=2){
					for (int b=data[j][a]; b<data[j][a+1]; b++){
						spectrum[b]=Math.max(0, nout[b][x]);
						spectrum[b]=Math.pow(spectrum[b], subSuppression);
					}
				}
				
				int count=0;
				for (int a=0; a<ny; a++){
					double sumxy=0;
					for (int b=0; b<ny; b++){
						double q=phase[count];
						sumxy+=q*spectrum[b];
						count++;
					}
					//hscor[j][a]=(sumxy/sumxx[a]);
					hscor[j][a]=(sumxy);
					if (hscor[j][a]>=scmax){
						scmax=hscor[j][a];
						rpitch[j]=a;
					}
				}
			}

			//rpitch=smoothPitch(hscor, rpitch, amp);
			rpitch=smoothPitch(hscor, amp, harmlimit);
			for (int j=0; j<eleLength; j++){
				freqMeasures[j][3]=logTransform[rpitch[j]];
				//System.out.println(logTransform[rpitch[j]]+" "+logTransform[rpitch[j]+1]);
			}
	
			freqList.add(freqMeasures);
			
			
		}
	}
	
	/**
	 * Deprecated!
	 * Method to measure trajectories of frequency change, defined as arctan transforms of 
	 * frequency slopes
	 * @param tList input signal locations
	 * @param freqList input frequency trajectories
	 * @param freqChangeList output frequency change trajectories
	 */
	void measureFrequencyChange(LinkedList<int[][]> tList, LinkedList<double[][]> freqList, LinkedList<double[][]> freqChangeList){
		
		int elNum=tList.size();
		for (int i=0; i<elNum; i++){
			int[][]data=tList.get(i);
			int eleLength=data.length;
			
			double[][] hscor=new double[eleLength][ny];
			
			double[][] freqMeasures=freqList.get(i);
			
			int boxSize=10;
			int x, xp, minxf, maxxf;
			double sxy, sx2, tot, ylog, yref;
			
			double dat[][]=new double[eleLength][4];
		
			boolean[][] signal=new boolean[eleLength][ny];
			
			for (int k=0; k<eleLength; k++){
				//System.out.println(data[k].length+" "+eleLength+" "+data[k][1]+" "+data[k][2]+" "+ny);
				xp=data[k][0];
				for (int j=0; j<ny; j++){
					signal[k][j]=false;
				}
				for (int j=1; j<data[k].length; j+=2){
					for (int m=data[k][j]; m<data[k][j+1]; m++){
						signal[k][m]=true;
					}
				}
				//for (int j=0; j<ny; j++){
					//int pp=0;
					//if (signal[k][j]){pp=1;}
					//System.out.print(pp);
				//}
				//System.out.println();
				
				for (int j=1; j<ny; j++){
					int p=j;
					double pc=0;
					hscor[k][j]=0;
					while (p<ny){
						if (signal[k][p]){hscor[k][j]+=nout[p][xp];}
						pc++;
						p+=j;
					}
					hscor[k][j]/=pc;
					
					//System.out.println(k+" "+j+" "+xp+" "+pc+" "+hscor[k][j]+" "+nout[j][xp]+" "+freqMeasures[k][3]);
				}
				//System.out.println();
					
				
			}
			for (int j=0; j<4; j++){
				for (int k=0; k<eleLength; k++){
					sxy=0;
					sx2=0;
					yref=Math.log(freqMeasures[k][j]*dy);
					
					
					minxf=100000;
					maxxf=0;
					for (int a=0-boxSize; a<=boxSize; a++){
						x=a+k;
						if ((x>=0)&&(x<eleLength)){
							if (freqMeasures[x][j]<minxf){minxf=(int)Math.round(freqMeasures[x][j]);}
							if (freqMeasures[x][j]>maxxf){maxxf=(int)Math.round(freqMeasures[x][j]);}
						}
					}
					minxf-=boxSize;
					maxxf+=boxSize;
					for (int a=0-boxSize; a<=boxSize; a++){
						xp=data[k][0]+a;
						x=a+k;
						if ((x>=0)&&(x<eleLength)){
							for (int b=minxf; b<=maxxf; b++){
								if ((b>0)&&(b<ny)){
									ylog=Math.log(b*dy)-yref;
									if (j==3){
										sxy+=a*dx*ylog*hscor[x][b];
										sx2+=a*a*dx*dx*hscor[x][b];
									}
									else if (signal[x][b]){
										sxy+=a*dx*ylog*nout[b][xp];
										sx2+=a*a*dx*dx*nout[b][xp];
									}
								}
							}
						
						}
					}
					tot=30*sxy/sx2;
					//System.out.println(sxy+" "+sx2+" "+minxf+" "+maxxf);
					dat[k][j]=(float)(0.5+(Math.atan2(tot, 1)/Math.PI));
				}
			}
			freqChangeList.add(dat);
		}
	}
	
/**
 * Deprecated
 * @param freqList
 * @param freqChangeList
 */
void measureFrequencyChange2a(LinkedList<double[][]> freqList, LinkedList<double[][]> freqChangeList){
		
		int elNum=freqList.size();
		for (int i=0; i<elNum; i++){
			
						
			double[][] freqMeasures=freqList.get(i);
			int eleLength=freqMeasures.length;
			double dxx=1;
			if (dx<1){dxx=dx;}
			int boxSize=(int)Math.round(5/dxx);
			
			if (boxSize>=eleLength){boxSize=eleLength-1;}
			int x, y;
			double sxy, sx2, tot, ylog;
			
			double dat[][]=new double[eleLength][4];
		
			for (int j=0; j<4; j++){
				double[] f=new double[eleLength];
				for (int k=0; k<eleLength; k++){
					double c=0;
					for (int a=0; a<=boxSize; a++){
						x=a+k;
						y=k-a;
						if ((y>=0)&&(x<eleLength)){
							f[k]+=Math.log(freqMeasures[x][j]*dy);
							f[k]+=Math.log(freqMeasures[y][j]*dy);
							c+=2;
						}
					}
					f[k]/=c;
					//f[k]=Math.log(freqMeasures[k][j]*dy);
				}
				
				
				
				for (int k=0; k<eleLength; k++){
					sxy=0;
					sx2=0;
					

					for (int a=0-boxSize; a<=boxSize; a++){
						x=a+k;
						
						if ((x>=0)&&(x<eleLength)){
							ylog=f[x]-f[k];
							sxy+=a*ylog;
							sx2+=a*a*dx*dx;
						}
					}
					tot=20*sxy/(sx2);
					dat[k][j]=(float)(0.5+(Math.atan2(tot, 1)/Math.PI));
				}
				for (int k=0; k<boxSize; k++){
					dat[k][j]=dat[boxSize][j];
					dat[eleLength-1-k][j]=dat[eleLength-boxSize-1][j];
				}
			}
			freqChangeList.add(dat);
		}
	}

	/**
	 * Method to measure trajectories of frequency change, defined as arctan transforms of 
	 * frequency slopes
	 * @param tList input signal locations
	 * @param freqList input frequency trajectories
	 * @param freqChangeList output frequency change trajectories
	 */
	private void measureFrequencyChange4(LinkedList<int[][]> tList, LinkedList<double[][]> freqList, LinkedList<double[][]> freqChangeList){
	
		double[] logs=new double[ny];
		for (int j=0; j<ny; j++){
			logs[j]=Math.log(j+1.5);
		}
		
		
		int elNum=freqList.size();
		for (int i=0; i<elNum; i++){
					
			//System.out.println("Element: "+(i+1));
			
			double[][] freqMeasures=freqList.get(i);
			int[][] data=tList.get(i);
			int eleLength=freqMeasures.length;
			double dxx=1;
			if (dx<1){dxx=dx;}
			int boxSize=(int)Math.round(4/dxx);
		
			if (boxSize>=eleLength/2){boxSize=(eleLength/2)-1;}
			int x;
			double sxy, sx2, tot, sx, sy, cx;
			
			double[][] fa=new double[eleLength][ny];
			for (int j=0; j<eleLength; j++){
				int chunkNum=data[j].length;
				int time=data[j][0];
				for (int k=1; k<chunkNum; k+=2){
					for (int j2=data[j][k]; j2<data[j][k+1]; j2++){
						if (j2<0){
							System.out.println("Problem: "+data[j][k]+" "+data[j][k+1]+" "+j2+" "+j+" "+k+" "+i);
							for (int a=0; a<data[j].length; a++) {
								System.out.println(data[j][a]);
							}
						}
						if (time<0){
							System.out.println("Problem: "+time+" "+j+" "+k);
						}
						double n=nout[j2][time];
						if (n>0){
							fa[j][j2]=n*n;
						}
					}
				}
			}
			
			
				
			double dat[][]=new double[eleLength][4];
			for (int j=0; j<4; j++){
				for (int k=boxSize; k<eleLength-boxSize; k++){
					
					
					sy=0;
					sx=0;
					cx=0;
					sxy=0;
					sx2=0;
					for (int a=0-boxSize; a<=boxSize; a++){
						x=a+k;
						double x2=x*dx;
						if ((x>=0)&&(x<eleLength)){
							double f=Math.log(freqMeasures[x][j]);
							double ft=Math.exp(f+logs[0]);
							int harm=0;
							
							for (int b=0; b<ny; b++){
								double n=fa[x][b];
								if (n>0){
									double m=0;
									if (j==3){
										while (b>ft){
											harm++;
											ft=Math.exp(f+logs[harm]);
										}
										m=harm;
									}
									double m2=Math.log(b-freqMeasures[x][j]*m);
									sy+=m2*n;
									sx+=x2*n;
									sx2+=x2*x2*n;
									sxy+=m2*x2*n;
									cx+=n;
								}
							}
							
							
							/*
							int chunkNum=data[x].length;
							int time=data[x][0];
						
							for (int i2=1; i2<chunkNum; i2+=2){
								for (int j2=data[x][i2]; j2<data[x][i2+1]; j2++){
									double n=nout[j2][time];
									if (n>0){
										double m=0;
										if (j==3){
											m=Math.round(j2/freqMeasures[k][j]);
											if (m==0){m=1;}
										}
										
										double m2=Math.log(dy*j2/m);
										sy+=m2*n;
										sx+=x*dx*n;
										sx2+=x*x*dx*dx*n;
										sxy+=m2*x*dx*n;
										cx+=n;
									}
								}
							}
							*/
						}
					}
					
					tot=(sxy*cx-(sx*sy))/(cx*sx2-sx*sx);
					//tot=30*sxy/(sx2);
					//dat[k][j]=(float)(0.5+(Math.atan2(tot, 1)/Math.PI));
					
					if (k<1){System.out.println(k+" "+boxSize+" "+dx);}
					
					dat[k][j]=tot;
					//if (j==3){System.out.println(k+" "+sy+" "+sx+" "+sxy+" "+sx2+" "+tot+" "+dat[k][j]);}
				}
				
				for (int k=0; k<boxSize; k++){
					dat[k][j]=dat[boxSize][j];
				}
				for (int k=eleLength-boxSize; k<eleLength; k++){
					dat[k][j]=dat[eleLength-boxSize-1][j];
				}
			}
			freqChangeList.add(dat);
		}
	}

	/**
	 * Deprecated
	 * @param freqList
	 * @param freqChangeList
	 */
	void measureFrequencyChange2(LinkedList<double[][]> freqList, LinkedList<double[][]> freqChangeList){
		
		int elNum=freqList.size();
		for (int i=0; i<elNum; i++){
						
			double[][] freqMeasures=freqList.get(i);
			int eleLength=freqMeasures.length;
			double dxx=1;
			if (dx<1){dxx=dx;}
			int boxSize=(int)Math.round(4/dxx);
			
			//if (boxSize>=eleLength/2){boxSize=eleLength/2;}
			int x;
			double sxy, sx2, tot, ylog, sx, sy, cx;
			
			double dat[][]=new double[eleLength][4];
			double[] f=new double[eleLength];
			for (int j=0; j<4; j++){
				for (int k=0; k<eleLength; k++){
					
					f[k]=Math.log(freqMeasures[k][j]*dy);
				}
				
				
				for (int k=0; k<eleLength; k++){
					sy=0;
					sx=0;
					cx=0;
					for (int a=0-boxSize; a<=boxSize; a++){
						x=a+k;
						if ((x>=0)&&(x<eleLength)){
							sy+=f[x];
							sx+=x*dx;
							cx++;
						}
					}
					sy/=cx;
					sx/=cx;
					sxy=0;
					sx2=0;
					for (int a=0-boxSize; a<=boxSize; a++){
						x=a+k;
						if ((x>=0)&&(x<eleLength)){
							ylog=f[x];
							sxy+=(ylog-sy)*(x*dx-sx);
							sx2+=(x*dx-sx)*(x*dx-sx);
						}
					}
					//System.out.println(sxy+ " "+sx2);
					//if (j==3){System.out.println(k+" "+sy+" "+sx+" "+sxy+" "+sx2);}
					tot=30*sxy/(sx2);
					dat[k][j]=(float)(0.5+(Math.atan2(tot, 1)/Math.PI));
					//if (j==3){System.out.println(k+" "+sy+" "+sx+" "+sxy+" "+sx2+" "+tot+" "+dat[k][j]);}
				}
				//for (int k=0; k<boxSize; k++){
					//dat[k][j]=dat[boxSize][j];
					//dat[eleLength-1-k][j]=dat[eleLength-boxSize-1][j];
				//}
			}
			freqChangeList.add(dat);
		}
	}

	/**
	 * Deprecated
	 * @param tList
	 * @param sigList
	 * @param freqChangeList2
	 */
	void measureFrequencyChange3(LinkedList<int[][]> tList, LinkedList<boolean[][]> sigList, LinkedList<double[][]> freqChangeList2){
		
		int elNum=sigList.size();
		
		double[] f=new double[ny];
		for (int i=1; i<ny; i++){f[i]=Math.log(i);}
		
		for (int i=0; i<elNum; i++){
			
			int[][]data=tList.get(i);
			
			boolean[][] sig=sigList.get(i);
			int eleLength=sig.length;
			int boxSize=(int)Math.round(5/dx);
			
			if (boxSize>=eleLength){boxSize=eleLength-1;}
			int x,x2, y;
			double sx, sy, cx, cy, sxy, sxx, tot, c, ns;
			
			double dat[][]=new double[eleLength][1];
			
			for (int j=0; j<eleLength; j++){
					tot=0;
					c=0;
					int time=data[j][0];
					for (int k=2; k<ny-1; k++){
						if ((sig[j][k])&&(nout[k-1][time]<nout[k][time])&&(nout[k+1][time]<nout[k][time])){
							sy=0;
							cy=0;
							sx=0;
							cx=0;
							for (int a=0-boxSize; a<=boxSize; a++){
								for (int b=0-boxSize; b<=boxSize; b++){
									x=a+j;
									y=b+k;
									x2=a+time;
									//System.out.println(x+" "+y+" "+a+" "+b+" "+x2+" "+eleLength+" "+ny);
									if ((x>=0)&&(x<eleLength)&&(y>0)&&(y<ny)&&(sig[x][y])&&(nout[y][x2]>0)){
										//System.out.println("h");
										sy+=nout[y][x2]*f[y];
										cy+=nout[y][x2];
										sx+=nout[y][x2]*x*dx;
										cx+=nout[y][x2];
									}
								}
							}
							sy/=cy;
							sx/=cx;
							sxy=0;
							sxx=0;
							ns=0;
							for (int a=0-boxSize; a<=boxSize; a++){
								for (int b=0-boxSize; b<=boxSize; b++){
									x=a+j;
									y=b+k;
									x2=a+time;
									if ((x>=0)&&(x<eleLength)&&(y>0)&&(y<ny)&&(sig[x][y])&&(nout[y][x2]>0)){
										sxy+=nout[y][x2]*nout[y][x2]*(f[y]-sy)*(x*dx-sx);
										sxx+=nout[y][x2]*nout[y][x2]*(x*dx-sx)*(x*dx-sx);
										ns+=nout[y][x2];
									}
								}
							}
							//System.out.println(j+" "+k+" "+sxx+" "+sxy);
							tot+=ns*30*sxy/sxx;
							c+=ns;
						}
					}
					tot/=c;
					dat[j][0]=(float)(0.5+(Math.atan2(tot, 1)/Math.PI));
					//System.out.println(tot+" "+c);
			}
			for (int k=0; k<boxSize; k++){
				dat[k][0]=dat[boxSize][0];
				dat[eleLength-1-k][0]=dat[eleLength-boxSize-1][0];
			}
			freqChangeList2.add(dat);
		}
	}
	
	/**
	 * Deprecated
	 * @param song
	 */
	public void redodiffs(Song song){
	
		
		for (int i=0; i<song.eleList.size(); i++){
		
			Element ele=(Element)song.eleList.get(i);
	
			for (int j=6; j<10; j++){				
				double max=0;
				double min=1000000;
			
				for (int k=0; k<ele.measurements.length; k++){
			
					double p=Math.tan((ele.measurements[k][j]-0.5)*Math.PI);
					p=p*0.3;
					p=0.5+(Math.atan2(p, 1)/Math.PI);
					ele.measurements[k][j]=p;
			
					if (p>max){
						max=p;
					}
					if (p<min){
						min=p;
					}
				}
			}
		}
	}
	

	
	
	

	
	/**
	 * Measure harmonicity trajectories for the element - based on measured fundamental
	 * frequency estimate trajectories
	 * @param tList input signal locations
	 * @param freqList input frequency trajectories
	 * @param harmList output harmonicity trajectories
	 */
	private void measureHarmonicity(LinkedList<int[][]> tList, LinkedList<double[][]> freqList, LinkedList<double[]> harmList){ 
		int elNum=tList.size();
		int x;
		double c, tot;
		boolean[] signal=new boolean[ny];
		for (int i=0; i<elNum; i++){
			int[][]data=tList.get(i);
			double[][]freqMeasures=freqList.get(i);
			int eleLength=data.length;
			//Following section measures harmonicity
			double[] dat2=new double[eleLength];
			for (int j=0; j<eleLength; j++){
				x=data[j][0];
				tot=0;
				int chunkNum=data[j].length;
				double maxNout=0;
				
				for (int k=0; k<ny; k++){
					signal[k]=false;
				}
				for (int a=1; a<chunkNum; a+=2){
					for (int b=data[j][a]; b<data[j][a+1]; b++){
						if (nout[b][x]>maxNout){maxNout=nout[b][x];}
						signal[b]=true;
					}
				}
				for (int a=0; a<ny; a++){

					c=a/freqMeasures[j][3];
					c=Math.abs(c-Math.round(c));
					double z=0;
					if (signal[a]){
						z=nout[a][x];
						if (z<0){z=0;}
					}
					double y=Math.pow(10, (z/maxNout))-1;
					if (c<0.25){dat2[j]+=y;}
					tot+=y;
				}
				if ((tot<=0)||(maxNout<=0)){
					dat2[j]=0;
				}
				else{
					dat2[j]/=tot;
				}
			}
			
			double[]median=new double[data.length];
			double holder[]=new double[11];
			int a, count;
			for (int j=0; j<eleLength; j++){
				count=0;
				for (int k=-5; k<=5; k++){
					a=j+k;
					if ((a>=0)&&(a<eleLength)){
						holder[count]=dat2[a];
						count++;
					}
				}
				Arrays.sort(holder, 0, count);
				median[j]=holder[count/2];
			}
			 
			harmList.add(median);
		}
	}

	
	/**
	 * Method to measure Wiener Entropy
	 * @param tList input signal location
	 * @param results output WE trajectories
	 */
	private void measureWienerEntropy(LinkedList<int[][]> tList, LinkedList<double[]> results){
	
		//Calculates the Wiener Entropy (ratio of Geometric:Arithmetic means of the power spectrum).
		//Uses the fact that spectrogram is already in decibel format to speed-up calculation of the geometric mean.
	
		int elNum=tList.size();
		boolean signal[]=new boolean[ny];
		
		double logd=10/(Math.log(10));
		double maxC=song.dynRange-Math.log(song.maxPossAmp)*logd;
		//double minPower=Math.pow(10, (0-maxC)*0.1);
		double minPower=Math.pow(10, -1);
		int x=0;
		for (int i=0; i<elNum; i++){
			int[][]data=tList.get(i);
			int eleLength=data.length;
			double [] wienerEntropies=new double[eleLength];
			for (int j=0; j<eleLength; j++){
				x=data[j][0];
				int chunkNum=data[j].length;
				double scoreArith=0;
				double scoreGeom=0;
				
				//double scoreArith2=0;
				//double scoreGeom2=0;
				for (int k=0; k<ny; k++){
					signal[k]=false;
				}
				double maxNout=0;
				for (int k=1; k<chunkNum; k+=2){
					for (int a=data[j][k]; a<data[j][k+1]; a++){
						signal[a]=true;
						if (nout[a][x]>maxNout){maxNout=nout[a][x];}
					}
				}
				maxNout-=100;
				minPower=Math.pow(10, maxNout);
				//double c=0;
				for (int k=0; k<ny; k++){
					if (signal[k]){
						//double amp=nout[k][x]/maxNout;
						double amp=nout[k][x];
						//scoreArith+=Math.pow(10, (amp-maxC)*0.1);
						scoreArith+=Math.pow(10, (amp-1));
						scoreGeom+=amp;
					}
					else{
						scoreArith+=minPower;
						scoreGeom+=maxNout;
						
						//double amp=nout[k][x];
						//scoreArith+=Math.pow(10, (amp-maxC)*0.1);
						//scoreArith+=Math.pow(10, (amp-1));
						//scoreGeom+=amp;
						
						//scoreArith2+=Math.pow(10, (amp-1));
						//scoreGeom2+=amp;
						//c++;
					}
				}
				//System.out.println(scoreGeom+" "+scoreArith);
				
				
				//scoreGeom-=maxC*ny;
				
				scoreArith/=ny+0.0;
				//scoreGeom=Math.pow(10, 0.1*((scoreGeom/(ny+0.0))-maxC));
				
				
				scoreGeom=Math.pow(10, ((scoreGeom/(ny+0.0))-1));
				
				//scoreArith2/=c;
				//scoreGeom2=Math.pow(10, ((scoreGeom2/c)-1));
				
				
				
				wienerEntropies[j]=-1*Math.log(scoreGeom/scoreArith);
				
				//wienerEntropies[j]=-1*Math.log((scoreGeom-scoreGeom2)/(scoreArith-scoreArith2));
				//System.out.println("W "+wienerEntropies[j]+" "+scoreGeom+" "+scoreArith+" "+minPower+" "+maxNout);
			}
			results.add(wienerEntropies);
		}
	}
	
	/**
	 * Method to measure basic frequency bandwidth parameter
	 * @param tList input signal locations
	 * @param results output bandwidth trajectories
	 */
	private void measureBandwidth(LinkedList<int[][]> tList, LinkedList<int[]> results){
		int elNum=tList.size();

		for (int i=0; i<elNum; i++){
			int[][]data=tList.get(i);
			int eleLength=data.length;
			int[] dat=new int[eleLength];
			int a, b, c, x;
			float maxs;
			for (int j=0; j<eleLength; j++){
				x=data[j][0];
				maxs=-100000;
				a=data[j][1];
				while ((a<=data[j][2])&&(nout[a][x]>maxs)){
					maxs=nout[a][x];
					a++;
				}
				
				maxs=-1000000;
				b=data[j][data[j].length-1];
				c=data[j][data[j].length-2];
				while ((b>=c)&&(nout[b][x]>maxs)){
					maxs=nout[b][x];
					b--;
				}
				dat[j]=b-a+2;
			}
			results.add(dat);
		}
	}
	
	/**
	 * Method to measure spectrogram(!) amplitudes of elements
	 * @param tList input signal locations
	 * @param uList transformed signal locations(?)
	 * @param results output amplitude trajectories
	 */
	private void measureAmplitude(LinkedList<double[][]> tList, LinkedList<int[][]> uList, LinkedList<double[]> results){
		int elNum=tList.size();
		int v;		
		for (int i=0; i<elNum; i++){
			double[][]data=tList.get(i);
			int[][] data2=uList.get(i);
			int eleLength=data.length;
			double[] dat=new double[eleLength];
			for (int j=0; j<eleLength; j++){
				v=(int)Math.round(data[j][0]);
				//System.out.println(j+" "+eleLength+" "+v+" "+data2[j][0]+" "+nout.length+" "+nout[0].length);
				dat[j]=song.dynRange-nout[v][data2[j][0]];
			}
			results.add(dat);
		}
	}
	
	/**
	 * Method to measure an estimate of reverberation. Deprecated
	 * @param tList
	 * @param results
	 */
	void measureReverberation(LinkedList<int[][]> tList, LinkedList<double[]> results){
		
		int elNum=tList.size();
		
		double dbDecayThreshold=20;
		
		int window=(int)Math.round(50/song.timeStep);
		if (window<1){window=1;}
		
		for (int i=0; i<elNum; i++){
			int[][]data=(int[][])tList.get(i);
			int eleLength=data.length;
			double[] dat=new double[eleLength];
			for (int j=0; j<eleLength; j++){
				double dc=0;
				double co=0;
				int loc3=200;
				int loc2=data[j][1];
				int ij=data[j][0];
				int ii=ij;
				double db=-1000000;
				for (int ak=-10; ak<10; ak++){
					int aj=ak+ij;
					if ((aj>0)&&(aj<nout[loc2].length)&&(nout[loc2][aj]>db)){
						db=nout[loc2][aj];
						ii=aj;
					}
				}
				
				double dbStop=db-dbDecayThreshold;
				for (int ak=1; ak<200; ak++){
					dc=0;
					co=0;
					if (ak+window<nout[0].length){
						for (int aj=0; aj<window; aj++){
							if (ii+ak+aj<nout[loc2].length){
								dc+=nout[loc2][ii+ak+aj];
								co++;
							}
						}
						dc/=co;
						//if (dc<stopRatio*db){
						if (dc<dbStop){
							loc3=ak;
							ak=201;
						}

					}		
				}
				dat[j]=loc3*song.timeStep;
			}
			results.add(dat);
		}
	}

	/**
	 * Deprecated
	 * @param tList
	 * @param results
	 */
	void measureReverberationRegression(LinkedList<int[][]> tList, LinkedList<double[]> results){
		
		int elNum=tList.size();
		double timeAmpEquivalence=0.01*song.timeStep;
		int window=(int)Math.round(100/song.timeStep);
		if (window<1){window=1;}
		
		for (int i=0; i<elNum; i++){
			int[][]data=tList.get(i);
			int eleLength=data.length;
			double[] dat=new double[eleLength];
			for (int j=0; j<eleLength; j++){
				int a=data[j][1];
				int c=data[j][0];
				int b=c;
				double db=-1000000;
				for (int ak=-10; ak<10; ak++){
					int aj=ak+c;
					if ((aj>0)&&(aj<nout[a].length)&&(nout[a][aj]>db)){
						db=nout[a][aj];
						b=aj;
					}
				}
				
				double sumx=0;
				double sumy=0;
				double sumxy=0;
				double sumxx=0;
				double count=0;
				double t,f;
				
				for (int k=0; k<window; k++){
					int d=b+k;
					if (d<nout[a].length){
						t=k*timeAmpEquivalence;
						f=nout[a][d];
						sumx+=t;
						sumx+=t*t;
						sumy+=f;
						sumxy+=t*f;
						count++;
					}		
				}
				count=1/count;
				dat[j]=(sumxy-(sumx*sumy*count))/(sumxx-(sumx*sumx*count));
			}
			results.add(dat);
		}
	}
	
	/**
	 * Method to measure vibrato/trill/periodic frequency modulation quality of elements
	 * This is quite complex, and this method just sends on to calculateTrill5
	 * @param freqs input frequency trajectories
	 * @param trills output trill measure trajectories
	 */
	private void measureTrills(LinkedList<double[][]> freqs, LinkedList<double[][]> trills){
		for (int i=0; i<freqs.size(); i++){
			double[][] freqMeasures=freqs.get(i);
				
			double [] peakFreqs=new double[freqMeasures.length];
			for (int j=0; j<freqMeasures.length; j++){
				//peakFreqs[j]=freqMeasures[j][3];
				
				//System.out.println(freqMeasures[j][0]+" "+freqMeasures[j][3]);
				//if (Math.abs(freqMeasures[j][0]-freqMeasures[j][3])<freqMeasures[j][3]*0.25){
				//	peakFreqs[j]=freqMeasures[j][0];
				//}
				//else{
					//System.out.println("sw");
					peakFreqs[j]=freqMeasures[j][3];
				//}
				
				//System.out.println(freqMeasures[j][0]+" "+freqMeasures[j][3]+" "+peakFreqs[j]);
			}
			
			//for (int j=0; j<freqMeasures.length-1; j++){
			//	peakFreqs[j]=peakFreqs[j]-peakFreqs[j+1];
				//if (peakFreqs[j]>0){peakFreqs[j]=Math.sqrt(peakFreqs[j]);}
				//else{
				//	peakFreqs[j]=-1*Math.sqrt(-1*peakFreqs[j]);
				//}
				
			//}
			//peakFreqs[freqMeasures.length-1]=peakFreqs[freqMeasures.length-2];
			//double[][] r=calculateFFT(peakFreqs);
			//trillList.add(r);
			
			//int[][] els=(int[][])tList.get(i);
			
			//double[][]r=calculateTrill2(peakFreqs, els);
			
			if (peakFreqs.length>1){
			
				double[][]r=calculateTrill5(peakFreqs, song.maxTrillWavelength);
			//System.out.println("eg1: "+r[0][0]);
			//double[][] r=calculateTrill(peakFreqs);
			//System.out.println("eg2: "+r[0][0]);

				trills.add(r);
			}
			else{
				double[][] r={{0,0,0}};
				trills.add(r);
			}
			/*
			
			if (freqMeasures.length>32){
				double[][] r=calculateFFT(peakFreqs, 32);
				trills.add(r);
			}
			else if (freqMeasures.length>16){
				double[][] r=calculateFFT(peakFreqs, 16);
				trills.add(r);
			}
			else{
				double[][] r=new double[freqMeasures.length][2];
				trills.add(r);
			}*/
		}
	}
	
	
	/**
	 * Method to measure vibrato characteristics of a set of frequency trajectories
	 * @param contours input trajectories
	 * @param max a parameter specifying the maximum wavelength of detected vibrato
	 * @return a double[][] containing trill parameters trajectory
	 */
	public double[][] calculateTrill5(double[] contours, double max){
		
		
		double thresh=0;
		double threshwav=max/song.timeStep;
		double threshasym=0.5;
		
		double threshold2=0.25;
		
		double threshold=contours.length*0.2;
		
		double minJump=2;
		
		LinkedList<int[]> peaks=new LinkedList<int[]>();
		
		int n=contours.length;
		
		boolean up=true;
		if (contours[1]>contours[0]){
			up=true;
		}
		else if (contours[1]<contours[0]){
			up=false;
		}
		else{
			for (int i=0; i<n; i++){
				if (contours[i]!=contours[0]){
					if (contours[i]>contours[0]){
						up=true;
					}
					else{
						up=false;
					}
					i=n;
				}
			}
		}
		//System.out.println(contours[0]+" "+contours[1]+" "+up);
		int loccont=0;
		
		for (int i=1; i<n; i++){
			//System.out.println(contours[i]);
			if (up){
				if(contours[i]<contours[loccont]-minJump){
					int[] q={i-1, 0};
					peaks.add(q);
					up=false;
					loccont=i;
					//System.out.println(q[0]+" "+q[1]+" "+contours[i-1]);
				}
				else if (contours[i]>contours[loccont]){
					loccont=i;
				}
			}
			if (!up){
				if(contours[i]>contours[loccont]+minJump){
					int[] q={i-1, 1};
					peaks.add(q);
					up=true;
					//System.out.println(q[0]+" "+q[1]+" "+contours[i-1]);
					loccont=i;
				}
				else if (contours[i]<contours[loccont]){
					loccont=i;
				}
			}
		}
		
		double[][] results=new double[n][3];
		int m=peaks.size();
		//System.out.println("peaks detected: "+m);
		
		if (m>5){
			double[]wavelengths=new double[m-2];
			double[]amps=new double[m-2];
			double[]asym=new double[m-2];
			int[] locs=new int[m-2];
		
			for (int i=1; i<m-1; i++){
				int[] pm1=peaks.get(i-1);
				int[] p=peaks.get(i);
				int[] pp1=peaks.get(i+1);
				wavelengths[i-1]=pp1[0]-pm1[0];
				//wavelengths[i-1]=Math.min(pp1[0]-p[0], p[0]-pm1[0]);
				//System.out.println(wavelengths[i-1]);
				double s=Math.abs(contours[p[0]]-contours[pm1[0]]);
				double t=Math.abs(contours[p[0]]-contours[pp1[0]]);
			
				//amps[i-1]=100*Math.abs(0.5*(s+t))/contours[p[0]];
				amps[i-1]=100*Math.min(s,t)/contours[p[0]];
				if (p[1]==0){
					asym[i-1]=(p[0]-pm1[0])/wavelengths[i-1];
				}
				else{
					asym[i-1]=(pp1[0]-p[0])/wavelengths[i-1];
				}
				locs[i-1]=p[0];
				//System.out.println(locs[i-1]+" "+amps[i-1]+" "+wavelengths[i-1]+" "+asym[i-1]);
			}
			
			double[] sumsq=new double[m-2];
			
			for (int i=0; i<m-2; i++){
				double ss1=0;
				for (int j=1; j<4; j++){
					double jj=locs[i]+j*wavelengths[i];
					double mindi=1000000;
					for (int k=i+j; k<locs.length; k++){
						if (Math.abs(wavelengths[i]-wavelengths[k])/Math.min(wavelengths[i], wavelengths[k])<1.5){
							double di=(locs[k]-jj)*(locs[k]-jj);
							if (di<mindi){mindi=di;}
						}
					}
					ss1+=mindi;
				}
				
				double ss2=0;
				for (int j=1; j<4; j++){
					double jj=locs[i]-j*wavelengths[i];
					double mindi=1000000;
					for (int k=0; k<i; k++){
						double di=(locs[k]-jj)*(locs[k]-jj);
						if (di<mindi){mindi=di;}
					}
					ss2+=mindi;
				}
				sumsq[i]=Math.sqrt(Math.min(ss1, ss2));
				if (wavelengths[i]>8){
					sumsq[i]/=wavelengths[i];
				}
				else{
					sumsq[i]/=8;
				}
				//System.out.println(wavelengths[i]+" "+sumsq[i]);
			}
		
		
			for (int i=0; i<n; i++){
				int loc=-1;
				double score=1000000;
				double bestscore=1000000;
				for (int j=0; j<m-2; j++){
					score=Math.abs(locs[j]-i);
					//if ((score<bestscore)&&(score<wavelengths[j])&&(sumsq[j]<threshold)){							//only consider local peaks that are within 1 wavelength of focal point
					if ((score<bestscore)&&(score<wavelengths[j])&&(wavelengths[j]<threshold)&&(sumsq[j]<threshold2)){	
						bestscore=score;
						loc=j;
					}
				}
				if(loc>=0){
					results[i][0]=amps[loc]*song.dy;
					//results[i][0]=amps[loc];
				}
				if (results[i][0]>thresh){
					results[i][1]=wavelengths[loc]*song.timeStep;
					results[i][2]=asym[loc];
				}
				else{
					results[i][1]=threshwav;
					results[i][2]=threshasym;
				}
				//System.out.println(song.dy+" "+i+" "+results[i][0]+" "+results[i][1]+" "+results[i][2]);
			}
		}
		else{
			for (int i=0; i<n; i++){
				results[i][1]=threshwav;
				results[i][2]=threshasym;
			}
		}
			
		return results;
	}

	/**
	 * Deprecated
	 * @param periods
	 * @return a double[][]
	 */
	public double[][] calculateSinWaves(double[] periods){
		int n=periods.length;
		
		double[][] sines=new double[n][];
		
		
		for (int i=0; i<n; i++){
			int p=(int)Math.ceil(periods[i]);
			double q=2*Math.PI/periods[i];
			p*=2;
			sines[i]=new double[p];
			for (int j=0; j<p; j++){
				sines[i][j]=Math.cos(j*q)-Math.cos((j+1)*q);
				//sines[i][j]=Math.cos(j*q);
				//System.out.print(sines[i][j]+" ");
			}
			//System.out.println();
		}
		return sines;
	}
	
	
	/**
	 * Deprecated?
	 * @param da
	 * @param startpoint
	 * @param endpoint
	 * @return a double[]
	 */
	public double[] regress(double[] da, int startpoint, int endpoint){
		
		double sumxy=0;
		double sumx=0;
		double sumy=0;
		double sumxx=0;
		double sumyy=0;
		double den=endpoint-startpoint+1.0;
		for (int k=startpoint; k<=endpoint; k++){
			sumxy+=k*da[k];
			sumx+=k;
			sumy+=da[k];
			sumxx+=k*k;
			sumyy+=da[k]*da[k];
		}
		
		double[] results=new double[3];
		
		double sumx2=sumx*sumx;
		double sumy2=sumy*sumy;
		double sumxy2=sumx*sumy;
		
		double p=(den*sumxx)-sumx2;
		double q=(den*sumyy)-sumy2;
		double r=(den*sumxy)-sumxy2;
		
		results[1]=r/p;

		results[2]=r/(Math.sqrt(p)*Math.sqrt(q));
		results[2]=results[2]*results[2];
		if ((p<=0)||(q<=0)){
			results[2]=0;
			results[1]=0;
		}
		results[0]=(sumy/den)-results[1]*(sumx/den);
		return results;
	}
	
	/**
	 * Deprecated
	 * @param a
	 * @param b
	 * @param x
	 * @param y
	 * @return a double
	 */
	public double pointToLine(double a, double b, double x, double y){
		
		
		double xl=(x+a*(y-b))/(1+a*a);
		double yl=a*xl+b;
		
		double dx=xl-x;
		double dy=yl-y;
		
		double r=Math.sqrt(dx*dx+dy*dy);
		
		//double[] results={xl, yl, r};
		
		return r;
	}
	
	/**
	 * Deprecated
	 * @param a
	 * @param b
	 * @param x
	 * @param y
	 * @return a double
	 */
	public double verticalPointToLine(double a, double b, double x, double y){
		
		double expectedY=a*x+b;
		
		double r=Math.abs(expectedY-y);
		
		return r;
	}
		
	
	
	
	/**
	 * Deprecated
	 * @param data
	 * @return a double[][]
	 */
	public double[][] calculateTrill(double[] data){
		int n=data.length;
		BasicStatistics bs=new BasicStatistics();
		double mean=bs.calculateMean(data);
		
		for (int i=0; i<n; i++){
			data[i]-=mean;
		}
		
		int nfreqs=50;
		int minPeriod=2;
		int maxPeriod=64;
		
		double[][] compTable=new double[n][nfreqs];
		double[][] corrTable=new double[n][nfreqs];
	
		double[] frame=new double[nfreqs];
		int [] frame2=new int[nfreqs];
		double adj2=1/Math.log(2);
		
		double minLog=Math.log(minPeriod)*adj2;
		double maxLog=Math.log(maxPeriod)*adj2;
		double gapP=(maxLog-minLog)/(nfreqs-1.0);
		
		for (int i=0; i<nfreqs; i++){
			double p=(i*gapP)+minLog;
			frame2[i]=(int)Math.round(Math.pow(2, p));
			frame[i]=Math.pow(2, p);
			//System.out.println("FRAME: "+frame[i]);
		}
		
		
		
		double[][] sines=calculateSinWaves(frame);
		int loca, locb, kk;
		double sumxy, sumx, sumy, sumxx, sumyy, regb, rega, adjy, den;
		for (int i=0; i<n; i++){
			for (int j=2; j<nfreqs; j++){
				int m=sines[j].length;
				if (n<=m*2){
					corrTable[i][j]=-1;
				}
				else{
				
					int p=m/2;
					loca=i-p;
					if (loca<0){
						loca=0;
					}
					locb=loca+m;
					if (locb>=n){
						locb=n-1;
						loca=locb-m;
					}
					
					sumxy=0;
					sumx=0;
					sumy=0;
					sumxx=0;
					den=m+0.0;
					for (int k=0; k<m; k++){
						kk=k+loca;
						sumxy+=k*data[kk];
						sumx+=k;
						sumy+=data[kk];
						sumxx+=k*k;
					}
					regb=(sumxy-(sumx*sumy/den))/(sumxx-(sumx*sumx/den));
					rega=(sumy/den)-regb*(sumx/den);
										
					sumxy=0;
					sumx=0;
					sumy=0;
					sumxx=0;
					sumyy=0;
					for (int k=0; k<m; k++){
						adjy=data[loca+k]-rega-regb*k;
						//adjy=data[loca+k];
						sumxy+=adjy*sines[j][k];
						sumx+=sines[j][k];
						sumy+=adjy;
						sumxx+=sines[j][k]*sines[j][k];
						sumyy+=adjy*adjy;
						//compTable[i][j]+=adjy*sines[j][k];
					}
					//compTable[i][j]/=den;
					
					//corrTable[i][j]=sumxy;
					sumxy=sumxy-((sumx*sumy)/den);
					sumxx=sumxx-(sumx*sumx/den);
					sumyy=sumyy-(sumy*sumy/den);
					if (sumyy==0){sumyy=1;}
					compTable[i][j]=(sumxy/sumxx);
					corrTable[i][j]=(sumxy/Math.sqrt(sumxx*sumyy));
					
				}
			}
		}
		
		double[][] results=new double[n][2];
		
		double score;
		int offset=-1;
		int freq=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<nfreqs; j++){
				//System.out.println(n+" "+j+" "+sines[j].length);
				if (n>=sines[j].length*4){
					int aa=i-frame2[j];
					if (aa<0){aa=0;}
					int bb=i+frame2[j];
					if (bb>=n){bb=n-1;}
					//System.out.println(j+" "+sines[j].length+" "+aa+" "+bb);
					for (int k=aa; k<=bb; k++){
					
				//loca=i-frame2[j];
				//if (loca<0){loca=0;}
				//locb=i+frame2[j];
				//if (locb>=n){locb=n-1;}
				//score=0;
				//for (int k=loca; k<=locb; k++){
				//	score+=corrTable[k][j];
				//}
						//score=corrTable[i][j]*Math.sqrt(compTable[i][j]);
						score=corrTable[k][j];
				//score/=(locb-loca+1.0);
						if (score>results[i][0]){
					
							results[i][0]=score;
							results[i][1]=j;
							offset=k;
							freq=j;
						}
					}
				}
			}
			
			if (offset>=0){
				//System.out.println(offset+" "+freq+" "+results[i][0]);
			
				int m=sines[freq].length;
				int p=m/2;
				loca=offset-p;
				if (loca<0){
					loca=0;
				}
				locb=loca+m;
				if (locb>=n){
					locb=n-1;
					loca=locb-m;
				}
					
				sumxy=0;
				sumx=0;
				sumy=0;
				sumxx=0;
				den=m+0.0;
				for (int k=0; k<m; k++){
					kk=k+loca;
					sumxy+=k*data[kk];
					sumx+=k;
					sumy+=data[kk];
					sumxx+=k*k;
				}
				regb=(sumxy-(sumx*sumy/den))/(sumxx-(sumx*sumx/den));
				rega=(sumy/den)-regb*(sumx/den);
				double bestA=100000;
				int ampVal=0;
				int maxAmp=50;
				for (int w=0; w<maxAmp; w++){
					sumx=0;
					for (int k=0; k<m; k++){
						adjy=data[loca+k]-rega-regb*k;
						sumx+=(adjy-(sines[freq][k]*(frame[w]-minPeriod)))*(adjy-(sines[freq][k]*(frame[w]-minPeriod)));
					}
					sumx=Math.sqrt(sumx/den);
					if (sumx<bestA){
						bestA=sumx;
						ampVal=w;
					}
				}
				results[i][0]=frame[ampVal]-minPeriod;
			//System.out.println(n+" "+sines[5].length);
				//System.out.println(results[i][0]+" "+results[i][1]);
			
			}
		}
		return results;
	}

	/**
	 * Deprecated
	 * @param data
	 * @param data2
	 * @return a double[][]
	 */
	public double[][] calculateTrill2(double[] data, int[][] data2){
		
		
		double dampenFactor=0.1;
		int minPeriod=2;
		int maxPeriod=20;
		int rep=2;
		int et=data.length;
		int fc=nout.length;
		
		float[][] tempel=new float[et][fc];
		double[][]results=new double[et][2];
		double[] bestScores=new double[et];
		
		float c=(float)(1/(song.dynMax*song.dynRange));
		
		for (int i=0; i<et; i++){
			
			float tot=0;
			int d=data2[i][0];
			for (int j=1; j<data2[i].length; j+=2){
				for (int k=data2[i][j]; k<data2[i][j+1]; k++){
					tempel[i][k]=nout[k][d]*c;
					if (tempel[i][k]<0){tempel[i][k]=0;}
					tot=tot+tempel[i][k];
				}
			}
			for (int j=0; j<tempel[i].length; j++){
				tempel[i][j]/=tot;
				//System.out.print(tempel[i][j]+" ");
			}
			//System.out.println();
		}
		
		int[] mean=new int[et];
		double[] diffs=new double[et];
		for (int i=minPeriod; i<maxPeriod; i++){
			for (int j=0; j<et; j++){
			
				int a=j-i;
				if (a<0){a=0;}
				int b=j+j-a;
				//int b=j+i;
				if (b>=et){
					b=et-1;
					a=j+j-b;
				}
				
				double sum=0;
				double tot=0;
				for (int k=a; k<=b; k++){
					sum+=data[k];
					tot++;
				}
				sum/=tot;
				mean[j]=(int)Math.round(sum);
				diffs[j]=Math.abs(sum-data[j]);
				//System.out.println(i+" "+j+" "+mean[j]+" "+data[j]+" "+sum+" "+diffs[j]);
			}
			double[]s=new double[et];
					
			for (int j=0; j<et; j++){
				int k=j+rep*i;
				if (k<et){
					int diff=mean[k]-mean[j];
					for (int a=0; a<fc; a++){
						int b=a+diff;
						if ((b>=0)&&(b<fc)){
							s[j]+=tempel[j][b]*tempel[k][a];
						}
					}
					double self=0;
					for (int a=0; a<fc; a++){
						self+=tempel[j][a]*tempel[j][a];
					}	
					s[j]/=self;
				}
				else{
					s[j]=-1;
				}
	
			}
			for (int j=0; j<et; j++){
				int k=j-rep*i;
				if (k<0){k=0;}
				double score=0;
				double tot=0;
				for (int a=k; a<=j; a++){
					if (s[a]>=0){
						score+=s[a];
						tot++;
					}
				}
				if (tot>0){
					score/=tot;
				}
				//System.out.println(score+" "+tot);
				//score/=Math.log(i);
				
				score/=1+dampenFactor*i;
				
				if (score>bestScores[j]){
					bestScores[j]=score;
					results[j][1]=i/dx;
					
					double b=0;
					double t=0;
					
					int aa=j-rep*i;
					if (aa<0){aa=0;}
					int ab=j+rep*i;
					if (ab>=et){ab=et-1;}
					
					double[] temp=new double[ab-aa+1];
					for (int a=aa; a<=ab; a++){
						b+=diffs[a];
						temp[a-aa]=diffs[a];
						//System.out.print(diffs[a]+" ");
						t++;
					}
					//System.out.println();
					if (t>0){
						results[j][0]=b/t;
						Arrays.sort(temp);
						results[j][0]=temp[temp.length/2];
					}				
				
				}				
				
			}
		}
		for (int j=0; j<et; j++){
			if (bestScores[j]<0.2){
				results[j][1]=0;
				results[j][0]=0;
			}
			//System.out.println(j+" "+bestScores[j]+" "+results[j][1]+" "+results[j][0]);
			
		}
		return results;
	}

	/**
	 * Deprecated
	 * @param contours
	 * @param data
	 * @param windowLength
	 * @return a double[][]
	 */
	public double[][] calculateTrill3(double[] contours, int[][] data, int windowLength){
		
		if (contours.length<windowLength*2){
			windowLength=contours.length/2;
		}
		
		//double coeff=0.75;
		double coeff2=20;
		//int w2=windowLength/2;
		int n=data.length;
		
		
		
		int[]max=new int[n];
		int[]min=new int[n];
		
		for (int i=0; i<n; i++){
			
			int c=data[i][0];
			int a=(int)Math.round(contours[i]);
			int b=data[i][1];
			double q=nout[a][c]-coeff2;
			if (q<0){q=0;}
			while((a>=b)&&(nout[a][c]>q)){
				a--;
			}
			min[i]=a;
			
			a=(int)Math.round(contours[i]);
			b=data[i][2];
			q=nout[a][c]-coeff2;
			while((a<=b)&&(a<nout.length)&&(nout[a][c]>q)){
				a++;
			}
			max[i]=a;
			
			
			
			
			/*
			int a=data[i][1];
			int b=data[i][2];
			
			
			//System.out.println(c+" "+a+" "+b);
			double tot=0;
			double m=0;
			int ml=0;
			double mina=1000000;
			for (int j=a; j<=b; j++){
				if (nout[j][c]>=0){
					tot+=nout[j][c];
					if (nout[j][c]>m){
						m=nout[j][c];
						ml=j;
					}
					if (nout[j][c]<mina){
						mina=nout[j][c];
					}
				}
			}
			
			tot-=(b-a+1)*mina;
			
			double cum=m-mina;
			int loc1=ml;
			int loc2=ml;
			for (int j=ml; j>0; j--){
				loc1++;
				loc2--;
				if (loc1>b){
					loc1--;
				}
				else{
					if (nout[loc1][c]>0){
						cum+=nout[loc1][c]-mina;
					}
				}
				if (loc2<a){
					loc2++;
				}
				else{
					if (nout[loc2][c]>0){
						cum+=nout[loc2][c]-mina;
					}
				}
				//cum+=nout[loc1][c]+nout[loc2][c];
			}
			max[i]=loc1;
			min[i]=loc2;
			*/
			//System.out.println(i+" "+max[i]+" "+min[i]);
		}
		
		
		double[][] results=new double[n][2];
		//System.out.println("made it");
		
		for (int i=0; i<n; i++){
			
			
			int p=windowLength/2;
			int loca=i-p;
			if (loca<0){
				loca=0;
			}
			int locb=loca+windowLength;
			if (locb>=n){
				locb=n-1;
				loca=locb-windowLength;
			}
			
			double regb1=0;
			double rega1=0;
			double regb2=0;
			double rega2=0;
			double bestcorr=-2;
			int locreg=0;
			
			for (int k=2; k<windowLength-2; k++){
				double[] r1=regress(contours, loca, loca+k);
				double[] r2=regress(contours, loca+k, locb);
				double score=r1[2]*k+r2[2]*(windowLength-k);
				if (score>bestcorr){
					bestcorr=score;
					regb1=r1[1];
					rega1=r1[0];
					regb2=r2[1];
					rega2=r2[0];
					locreg=k+loca;
				}
			}
			
			
			//System.out.println(contours[i]+" "+locreg+" "+rega1+" "+regb1+" "+rega2+" "+regb2+" "+bestcorr+" "+loca+" "+locb);
			
			
			/*
			double sumxy=0;
			double sumx=0;
			double sumy=0;
			double sumxx=0;
			double den=windowLength+0.0;
			for (int k=0; k<windowLength; k++){
				int kk=k+loca;
				sumxy+=k*contours[kk];
				sumx+=k;
				sumy+=contours[kk];
				sumxx+=k*k;
			}
			double regb=(sumxy-(sumx*sumy/den))/(sumxx-(sumx*sumx/den));
			double rega=(sumy/den)-regb*(sumx/den);
			//System.out.println(regb+" "+rega);
			 */
			double bestMax=0;
			double bestMin=1000000;

			double sumLarge=0;
			double maxx=0;
			double minn=0;
			double regb=0;
			for (int k=0; k<windowLength; k++){
				int kk=loca+k;
				if (kk<=locreg){
					maxx=max[kk]-rega1-regb1*kk;
					minn=min[kk]-rega1-regb1*kk;
					regb=regb1;
				}
				else{
					maxx=max[kk]-rega2-regb2*kk;
					minn=min[kk]-rega2-regb2*kk;
					regb=regb2;
				}
				//double maxx=max[loca+k]-rega-regb*k;
				//double minn=min[loca+k]-rega-regb*k;
				if (maxx>bestMax){
					bestMax=maxx;
				}
				if (minn<bestMin){
					bestMin=minn;
				}

				double minna=Math.abs(minn);
				sumLarge+=Math.max(minna, maxx)-Math.abs(0.5*regb*song.frameLength/song.timeStep);
			}

			results[i][0]=sumLarge*song.dy/(windowLength+0.0);
		}
		return results;
	}
	
	/**
	 * Deprecated
	 * @param contours
	 * @param data
	 * @param windowLength
	 * @return a double[][]
	 */
	public double[][] calculateTrill3a(double[] contours, int[][] data, int windowLength){
		
		if (contours.length<windowLength*2){
			windowLength=contours.length/2;
		}
		double coeff=0.75;
		int n=data.length;

		int[]max=new int[n];
		int[]min=new int[n];
		
		for (int i=0; i<n; i++){
			
			min[i]=data[i][1];
			max[i]=data[i][2];
			//System.out.println(max[i]+" "+min[i]+" "+contours[i]);
			if (contours[i]<min[i]){
				min[i]=(int)Math.floor(contours[i]);
			}
			if (contours[i]>max[i]){
				max[i]=(int)Math.ceil(contours[i]);
			}
			
		}
		
		
		double[][] results=new double[n][2];
		
		for (int i=0; i<n; i++){
			
			
			int p=windowLength/2;
			int loca=i-p;
			if (loca<0){
				loca=0;
			}
			int locb=i+p;
			if (locb>=n){
				locb=n-1;
			}
			
			int wl2=locb-loca+1;
			
			
			
			double regb1=0;
			double rega1=0;
			double regb2=0;
			double rega2=0;
			double bestcorr=-2;
			int locreg=0;
			
			for (int k=2; k<wl2-2; k++){
				double[] r1=regress(contours, loca, loca+k);
				double[] r2=regress(contours, loca+k, locb);
				double score=r1[2]*k+r2[2]*(wl2-k);
				if (score>bestcorr){
					bestcorr=score;
					regb1=r1[1];
					rega1=r1[0];
					regb2=r2[1];
					rega2=r2[0];
					locreg=k+loca;
				}
			}
			
			
			//System.out.println(contours[i]+" "+locreg+" "+rega1+" "+regb1+" "+rega2+" "+regb2+" "+bestcorr+" "+loca+" "+locb);

			//System.out.println(regb+" "+rega);
			double bestMax=0;
			double bestMin=1000000;

			double sumExp=0;
			double maxx=0;
			double minn=0;
			double exp=0;
			
			double slopeAdj=Math.max(Math.abs(regb1), Math.abs(regb2))*coeff*wl2;		//the steeper the slope, the larger we expect the bandwidth to be due to spectrogram limitations. This factor is a crude way of adjusting for this.
			
			
			for (int k=loca; k<=locb; k++){
				int kk=k-loca;
				//double maxx=max[k]-rega-regb*kk;
				//double minn=min[k]-rega-regb*kk;
				
				if (k<=locreg){
					maxx=max[k]-rega1-regb1*kk;
					minn=min[k]-rega1-regb1*kk;
					exp=contours[k]-rega1-regb1*kk;
				}
				else{
					maxx=max[k]-rega2-regb2*kk;
					minn=min[k]-rega2-regb2*kk;
					exp=contours[k]-rega2-regb2*kk;
				}
				
				
				
				//System.out.println(i+" "+k+" "+exp+" "+contours[k]+" "+slopeAdj+" "+rega1+" "+rega2+" "+regb1+" "+regb2);
				
				
				if (maxx>bestMax){
					bestMax=maxx;
				}
				if (minn<bestMin){
					bestMin=minn;
				}

				sumExp+=Math.abs(exp);
			}
			
			//System.out.println("R: "+i+" "+sumExp+" "+slopeAdj);
			slopeAdj=0;
			//System.out.println(bestMax+" "+bestMin);
			//results[i][0]=(bestMax-bestMin-slopeAdj)*song.dy;
			//results[i][0]=(sumMax-sumMin)*song.dy/(windowLength+0.0);
			//results[i][0]=(sumLarge*song.dy/(locb-loca+1.0))-slopeAdj;
			results[i][0]=song.dy*(sumExp-slopeAdj);
			
		}
		return results;
	}
	
	
	/**
	 * Deprecated
	 * @param contours
	 * @param windowLength
	 * @return a double[][]
	 */
	public double[][] calculateTrill4(double[] contours, int windowLength){
		
		
		windowLength=contours.length/3;
		if (windowLength>40){windowLength=40;}
		if (windowLength<10){
			windowLength=10;
			if (windowLength>=contours.length){windowLength=contours.length-1;}
		}

		int n=contours.length;
		
		double[][] results=new double[n][2];
		
		for (int i=0; i<n; i++){
			
			int p=windowLength/2;
			int loca=i-p;
			if (loca<0){
				loca=0;
			}
			int locb=loca+windowLength;
			if (locb>=n){
				locb=n-1;
				loca=locb-windowLength;
				if (loca<0){loca=0;}
			}
			
			double wl2=locb-loca+1.0;
			
			double bestDist=1000000;
			
			for (int k=loca+2; k<locb-2; k++){
				double[] r1=regress(contours, loca, k);
				double[] r2=regress(contours, k, locb);
				double score=0;
				for (int j=loca; j<=locb; j++){
					if (j<=k){
						score+=pointToLine(r1[1], r1[0], j, contours[j]);
					}
					else{
						score+=pointToLine(r2[1], r2[0], j, contours[j]);
					}
				}
				if (score<bestDist){
					bestDist=score;
				}
			}
			if (bestDist==1000000){bestDist=1;}
			//System.out.println(i+" "+contours[i]+" "+bestDist+" "+locreg+" "+regb1+" "+regb2+" "+rega1+" "+rega2+" "+cc1+" "+cc2);
			
			results[i][0]=song.dy*10*(bestDist/wl2);
			
		}
		return results;
	}
	
	
	/**
	 * Deprecated method to update element start times
	 */
	public void updateEleStartTimesX(){
		
		for (int i=0; i<song.eleList.size(); i++){
			Element ele=(Element)song.eleList.get(i);
			ele.begintime=(int)Math.round(ele.begintime*0.998);
			
			for (int j=0; j<ele.signal.length; j++){
				ele.signal[j][0]=(int)Math.round(ele.signal[j][0]*0.998);
			}
			
		}
	}
	
	/**
	 * Deprecated method to update harmonicity measures
	 */
	public void updateHarmonicityX(){
		LinkedList<Element> eleList=new LinkedList<Element>();
		for (int i=0; i<song.eleList.size(); i++){
			//System.out.println("Q "+i);
			Element ele=(Element)song.eleList.get(i);
			
			int[][] signal=ele.signal;
			int eleLength=signal.length;
			for (int j=0; j<eleLength; j++){
				for (int k=1; k<signal[j].length; k++){
					signal[j][k]=ny-signal[j][k]-1;
				}
			}
			LinkedList<int[][]> slist=new LinkedList<int[][]>();
			slist.add(signal);
			double[][] freq=new double[ele.measurements.length-5][4];
			for (int j=0; j<freq.length; j++){
				for (int k=0; k<4; k++){
					freq[j][k]=ele.measurements[j+5][k]/ele.dy;
				}
			}
			LinkedList<double[][]> freqList=new LinkedList<double[][]>();
			freqList.add(freq);
			
			LinkedList<double[]> harmList=new LinkedList<double[]>();
			try{
				measureHarmonicity(slist, freqList, harmList);
			}
			catch(Error e){System.out.println(e);}
			
			double[] harm=harmList.get(0);
			
			double[][] newm=new double[freq.length][15];
			
			for (int j=0; j<freq.length; j++){
				for (int k=0; k<15; k++){
					newm[j][k]=ele.measurements[j+5][k];
				}
				
				newm[j][8]=harm[j];
			}
			
			for (int j=0; j<eleLength; j++){
				for (int k=1; k<signal[j].length; k++){
					signal[j][k]=ny-signal[j][k]-1;
				}
			}
			
			ele.measurements=newm;
			ele.calculateStatistics();
			eleList.add(ele);
		}
		//System.out.println("done");
		song.eleList=eleList;

	}
		
	
	/**
	 * Deprecated method to update trill measures
	 */
	public void updateTrillMeasuresX(){
		//System.out.print(song.eleList.size()+" P ");
		LinkedList<Element> eleList=new LinkedList<Element>();
		for (int i=0; i<song.eleList.size(); i++){
			//System.out.println("Q "+i);
			Element ele=(Element)song.eleList.get(i);
			double[][] freq=new double[ele.measurements.length-5][4];
			for (int j=0; j<freq.length; j++){
				for (int k=0; k<4; k++){
					freq[j][k]=ele.measurements[j+5][k]/ele.dy;
				}
			}
			LinkedList<double[][]> freqList=new LinkedList<double[][]>();
			freqList.add(freq);
			
			LinkedList<double[][]> trillList=new LinkedList<double[][]>();
			try{
				measureTrills(freqList, trillList);
			}
			catch(Error e){System.out.println(e);}
			
			double[][] trill=trillList.get(0);
			
			double[][] newm=new double[freq.length][15];
			
			for (int j=0; j<freq.length; j++){
				for (int k=0; k<13; k++){
					newm[j][k]=ele.measurements[j+5][k];
				}
				for (int k=0; k<2; k++){
					newm[j][13+k]=trill[j][k];
				}
			}
			ele.measurements=newm;
			ele.calculateStatistics();
			eleList.add(ele);
		}
		//System.out.println("done");
		song.eleList=eleList;
	}
	
	/**
	 * Deprecated method to update frequency change measures
	 */
	public void updateChangeMeasures(){
		//System.out.print(song.eleList.size()+" P ");
		LinkedList<Element> eleList=new LinkedList<Element>();
		for (int i=0; i<song.eleList.size(); i++){
			//System.out.println("Q "+i);
			Element ele=(Element)song.eleList.get(i);
			int[][] signal=ele.signal;
			int eleLength=signal.length;
			for (int j=0; j<eleLength; j++){
				for (int k=1; k<signal[j].length; k++){
					signal[j][k]=ny-signal[j][k]-1;
				}
			}
			
			LinkedList<int[][]> tList=new LinkedList<int[][]>();
			tList.add(signal);
			//System.out.println("STARTS: "+signal[0][0]+" "+nout.length+" "+nout[0].length);
			
			double[][] freq=new double[ele.measurements.length-5][4];
			for (int j=0; j<freq.length; j++){
				for (int k=0; k<4; k++){
					freq[j][k]=(ele.measurements[j+5][k]/ele.dy);
				}
			}
			LinkedList<double[][]> freqList=new LinkedList<double[][]>();
			freqList.add(freq);
			
			LinkedList<double[][]> freqChangeList=new LinkedList<double[][]>();
			try{
				//measureFrequencyChange2(freqList, freqChangeList);
				measureFrequencyChange4(tList, freqList, freqChangeList);
			}
			catch(Error e){e.printStackTrace();}
			
			double[][] freqChange=freqChangeList.get(0);
			
			double[][] newm=new double[freq.length][15];
			
			for (int j=0; j<freq.length; j++){
				for (int k=0; k<15; k++){
					newm[j][k]=ele.measurements[j+5][k];
				}
				for (int k=0; k<4; k++){
					//System.out.print(freqChange[j][k]+" ");
					newm[j][4+k]=freqChange[j][k];
					
					//System.out.print(newm[j][4+k]+" "+newm[j][k]+" ");
					
					
				}
				//System.out.println();
				//System.out.println();
			}
			
			for (int j=0; j<eleLength; j++){
				for (int k=1; k<signal[j].length; k++){
					signal[j][k]=ny-signal[j][k]-1;
				}
			}
			
			ele.measurements=newm;
			ele.calculateStatistics();
			eleList.add(ele);
		}
		//System.out.println("done");
		song.eleList=eleList;
	}
	
	/**
	 * Deprecated method to update Wiener Entropy measures.
	 */
	public void updateWienerEntropyX(){
		//System.out.print(song.eleList.size()+" P ");
		LinkedList<Element> eleList=new LinkedList<Element>();
		for (int i=0; i<song.eleList.size(); i++){
			//System.out.println("Q "+i);
			Element ele=(Element)song.eleList.get(i);
			int[][] signal=ele.signal;
			int eleLength=signal.length;
			for (int j=0; j<eleLength; j++){
				for (int k=1; k<signal[j].length; k++){
					signal[j][k]=ny-signal[j][k]-1;
				}
			}
			
			LinkedList<int[][]> tList=new LinkedList<int[][]>();
			tList.add(signal);
			//System.out.println("STARTS: "+signal[0][0]+" "+nout.length+" "+nout[0].length);
			
			
			
			LinkedList<double[]> entList=new LinkedList<double[]>();
			try{
				measureWienerEntropy(tList, entList);
			}
			catch(Error e){e.printStackTrace();}
			
			double[] ent=entList.get(0);
			
			double[][] newm=new double[ent.length][15];
			
			for (int j=0; j<ent.length; j++){
				for (int k=0; k<15; k++){
					newm[j][k]=ele.measurements[j+5][k];
				}
				newm[j][9]=ent[j];
			}
			
			for (int j=0; j<eleLength; j++){
				for (int k=1; k<signal[j].length; k++){
					signal[j][k]=ny-signal[j][k]-1;
				}
			}
			
			ele.measurements=newm;
			ele.calculateStatistics();
			eleList.add(ele);
		}
		System.out.println("done");
		song.eleList=eleList;
	}
	
	
	public void checkMinimumLengths(LinkedList<int[][]> signals){
		double x=song.minLength/song.timeStep;
		for (int i=0; i<signals.size(); i++){
			int[][] s=signals.get(i);
			if (s.length<x){
				signals.remove(i);
				i--;
			}
		}
		
	}
	
	public void segment(LinkedList<int[][]> signals, boolean force){
		
		//This method checks through the discovered elements, and joins together those elements that are separated by less than minGap
	
		int i, j, k, jj, loc;
		
		double r=song.getMinGap()/song.getTimeStep();
		
		for (i=0; i<signals.size()-1; i++){
			int[][]t1=signals.get(i);			//t1 is the first signal space
			int[][]t2=signals.get(i+1);		//t2 is the second signal space
			
			if ((t2[0][0]-t1[t1.length-1][0]<r)||(force)){			//if the two signals are closer than minGap to each other... then we have to join them up!
				
				
				int newLength=t2[t2.length-1][0]-t1[0][0]+1;
				
				int [][]u=new int [newLength][];	//u is the new signal space for our new joined up element
				
				for (j=0; j<t1.length; j++){		//this is the easy bit: copy the first signal into the beginning of the new vectors (u, u2)
					u[j]=new int[t1[j].length];
					for (k=0; k<u[j].length; k++){u[j][k]=t1[j][k];}
				}
				
				
				jj=t2[0][0]-t1[0][0];
				for (j=0; j<t2.length; j++){		//this is also easy: copy the second signal into the end of the new vectors
					u[jj]=new int[t2[j].length];
					for (k=0; k<u[jj].length; k++){u[jj][k]=t2[j][k];}
					jj++;
				}
				
				jj=t2[0][0]-t1[0][0];
				int bottom1=t1[t1.length-1][1];
				int bottom2=t2[0][1];
				int top1=t1[t1.length-1][t1[t1.length-1].length-1];
				int top2=t2[0][t2[0].length-1];
				int st, sb;
				double place;
				double diff=jj-t1.length;
				float max2;
				
				for (j=t1.length; j<jj; j++){			//now comes the difficult bit: filling in the gap between the two signals (if there is one)
					u[j]=new int[3];			//make a new vector: we assume there's only one band of signal present!
					u[j][0]=t1[0][0]+j;					//fill in the time value (easy!)
					
					
					place=(j-t1.length)/diff;
					st=(int)Math.round(place*top1+(1-place)*top2);
					sb=(int)Math.round(place*bottom1+(1-place)*bottom2);
					
					max2=-1000000;
					loc=0;
					for (k=sb; k<=st; k++){
						if (nout[k][u[j][0]]>max2){
							max2=nout[k][u[j][0]];
							loc=k;
						}
					}
					
					u[j][1]=sb;
					for (k=loc; k>=sb; k--){
						if (nout[k][u[j][0]]<max2*0.25){
							u[j][1]=k;
							k=sb-1;
						}
					}
					
					u[j][2]=st;
					for (k=loc; k<=st; k++){
						if (nout[k][u[j][0]]<max2*0.25){
							u[j][2]=k;
							k=st+1;
						}
					}
					if (u[j][2]==u[j][1]){
						u[j][2]++;
						if (u[j][2]>=nout.length){
							u[j][2]=nout.length-1;
						}
						u[j][1]--;
						if (u[j][1]<0){
							u[j][1]=0;
						}
					}					
				}
				signals.remove(i);
				signals.remove(i);
				signals.add(i, u);
				i--;
			}
		}
	}
	
	
	
	
	
}
