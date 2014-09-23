package lusc.net.sourceforge;
//
//  SongGroup.java
//  Luscinia
//
//  Created by Robert Lachlan on 10/15/06.
//  Copyright 2006 R.F.Lachlan. All rights reserved.
//

import java.util.*;
import javax.swing.*;

public class SongGroup {
	
	Song[] songs;
	
	double[][][] data;
	double[][] dataSum;
	int[][] elementPos;
	int[][] individualSongs, individualTrans, individualSyl, individualEle, lookUpEls, lookUpElsC, lookUpSyls, lookUpTrans, lookUpTrans2;
	int[] lookUpSongs, paramType, priors, posts;
	int[] sylCounts, eleCounts;
	int songNumber, individualNumber, eleNumber, eleNumberC, syllNumber, transNumber, maxEleLength, maxSyllLength, maxTransLength, maxSongLength, numParams, numTempParams;
	int numberOfCPUs=2;
	double mainReductionFactor=0.25;
	int minPoints=5;
	double minVar=0.2;
	double stitchPunishment=150;
	double sdRatio=0.5;
	double offsetRemoval=0.0;
	double alignmentCost=0.2;
	
	double syllableRepetitionWeighting=0;
	
	
	float[][] scoresEle, scoresEleC, scoresSyll, scoresSyll2, scoreTrans, scoresSong;
	String[]  eleNames, eleNamesC, syllNames, transNames, songNames, populations, species;
	
	int[] syllableRepetitions;
	
	double[] average, sd, sdReal, sdBetween, syllLabels, transLabels, eleLabels, songLabels;
	double[][] avind, sdind, sdRealInd;
	double[] parameterValues={1,0,0,0,0,1,0,0,0,1,0,0,0,0,1,0,0, 0, 0,0,0,0,0};
	double[] statValues={1, 0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0, 0,0,0,0,0}; 
	double[] validParameters;
	boolean normalizePerElement=false;
	boolean weightByAmp=true;
	boolean compressElements=false;
	boolean logFrequencies=true;
	int stitchSyllables=0;
	//boolean stitchSyllables=false;
	boolean[][] compScheme;
	SpectrogramSideBar ssb=new SpectrogramSideBar(this);
	Defaults defaults;
	DataBaseController dbc;
	Random random=new Random(System.currentTimeMillis());
		
	public SongGroup(Song[] songs, Defaults defaults, DataBaseController dbc){
		this.songs=songs;
		
		calculateSyllableRepetitions();
		
		this.defaults=defaults;
		this.dbc=dbc;
		songNumber=songs.length;
		
		//updateWEntropy();
		//updateVibrato();
		//updateFrequencyChange();
		//updateHarmonicity();
		//select out the middle syllable of each phrase only:
		
	}
	
	public SongGroup(Song[] songs, Defaults defaults){
		this.songs=songs;
		this.defaults=defaults;
		songNumber=songs.length;
	}
	
	public SongGroup(Song[] songs, boolean[][] compScheme, Defaults defaults, DataBaseController dbc){
		this.songs=songs;
		this.compScheme=compScheme;
		this.defaults=defaults;
		this.dbc=dbc;
		songNumber=songs.length;
	}
	
	public void calculateSyllableRepetitions(){
		int n=0;
		for (int i=0; i<songs.length; i++){
			n+=songs[i].phrases.size();
		}
		
		syllableRepetitions=new int[n];
		int k=0;
		for (int i=0; i<songs.length; i++){
			for (int j=0; j<songs[i].phrases.size(); j++){
				int[][] p=(int[][])songs[i].phrases.get(j);
				syllableRepetitions[k]=p.length;
				k++;
			}
		}
	}
	
	public void makeEveryElementASyllable(){
		for (int i=0; i<songs.length; i++){
			LinkedList<int[][]> phrases=new LinkedList<int[][]>();
			for (int j=0; j<songs[i].eleList.size(); j++){
				int[][] a=new int[1][1];
				a[0][0]=j;
				phrases.add(a);
			}
			songs[i].phrases=phrases;
		}
	}
	
	public void segmentSyllableBasedOnThreshold(double thresh){
		for (int i=0; i<songs.length; i++){
			LinkedList<int[][]> phrases=new LinkedList<int[][]>();
			
			int j=0;
			
			while (j<songs[i].eleList.size()){
				int k=j;
				Element ele=(Element)songs[i].eleList.get(j);
				while ((ele.timeAfter<thresh)&&(ele.timeAfter>-10000)){
					j++;
					ele=(Element)songs[i].eleList.get(j);
				}
				int[][] a=new int[1][j-k+1];
				for (int l=k; l<=j; l++){
					a[0][l-k]=l;
				}
				phrases.add(a);
				j++;
			}
			
			songs[i].phrases=phrases;
		}
	}
	
	
	public void pickJustOneExamplePerPhrase(){
		SyllableCompressor sc=new SyllableCompressor();
		for (int i=0; i<songs.length; i++){
			sc.compressSong2(songs[i]);
			//sc.compressSong3(songs[i]);
			songs[i]=sc.s;
		}
		
	}
	
	public void pickJustOneExamplePerPhrase2(){
		
		for (int i=0; i<songs.length; i++){
			//Song song=new Song();
			//song.name=songs[i].name;
			//song.individualName=songs[i].individualName;
			LinkedList<Element> eleList=new LinkedList<Element>();
			LinkedList<int[][]> phrases=new LinkedList<int[][]>();
			//song.songID=songs[i].songID;
			int eleCount=0;
			for (int j=0; j<songs[i].phrases.size(); j++){
				int[][] ph=(int[][])songs[i].phrases.get(j);
				if (ph.length>1){
					int b=ph.length/2;
					int c=ph[b].length;
					for (int k=0; k<ph[b].length; k++){
						if (ph[b][k]==-1){
							c--;
						}
					}
							
					//for (int k=0; k<c; k++){
					//	if(ph[b][k]>=0){
					//		Element ele=(Element)songs[i].eleList.get(ph[b-1][k]);
					//		eleList.add(ele);
					//	}
					//}
					
					int d=0;
					for (int k=0; k<c; k++){
						if(ph[b][k]>=0){
							
							Element ele=(Element)songs[i].eleList.get(ph[b][k]);
							//System.out.println(((ele.measurements[0][0]-ele.measurements[1][0])/(ele.length*ele.timeStep)));
							//if ((((ele.measurements[0][0]-ele.measurements[1][0])/(ele.length*ele.timeStep))>thresh)&&(ele.measurements[5][0]>ele.measurements[ele.measurements.length-1][0])){
							//if(ele.measurements[4][4]<thresh){
								//System.out.println("inc");
								eleList.add(ele);
								//eleCount++;
								d++;
							//}
						}
					}
					
					//for (int k=0; k<c; k++){
					//	if(ph[b][k]>=0){
					//		Element ele=(Element)songs[i].eleList.get(ph[b+1][k]);
					//		eleList.add(ele);
					//	}
					//}
					
					int ph2[][]=new int[1][d];
					//for (int kk=0; kk<3; kk++){
						for (int k=0; k<d; k++){
							ph2[0][k]=eleCount;
							eleCount++;
						}
					//}
					if (d>0){
						//System.out.println(i+" !");
						phrases.add(ph2);
					}
				}
				else{
					int ph2[][]=new int[1][ph[0].length];
					for (int k=0; k<ph[0].length; k++){
						Element ele=(Element)songs[i].eleList.get(ph[0][k]);
						eleList.add(ele);
						ph2[0][k]=eleCount;
						eleCount++;
					}
					phrases.add(ph2);
				}
					
			}
			//System.out.println(eleCount);
			//if (eleCount>0){newsong.add(song);}
			
			songs[i].phrases=phrases;
			songs[i].eleList=eleList;
			
			//songs[i]=song;
		}
		//System.out.println("Number of songs included: "+songs.length);
		//songs=new Song[newsong.size()];
		//songs=(Song[])newsong.toArray(songs);
		//System.out.println("Number of songs included: "+songs.length);
		//songNumber=songs.length;
	}
	
	public void checkAndLoadRawData(int i){
		if (songs[i].rawData==null){
			loadSongRawData(i);
		}
	}
	
	public void loadSongRawData(int i){
		Song song=dbc.loadSongFromDatabase(songs[i].songID, 0);
		songs[i]=song;
		System.out.println(songs[i].maxf+" "+songs[i].dynRange);
		if ((songs[i].maxf<=1)||(songs[i].dynRange<1)){
			defaults.getSongParameters(songs[i]);
		}
		System.out.println(songs[i].rawData.length);
		
	}
	
	public void cleanUp(){
		songs=null;
		data=null;
		individualSongs=null;
		individualTrans=null;
		individualSyl=null;
		individualEle=null;
		lookUpEls=null;
		lookUpSyls=null;
		lookUpTrans=null;
		lookUpSongs=null;
		scoresEle=null;
		scoresEleC=null;
		scoresSyll=null;
		scoresSyll2=null;
		scoreTrans=null;
		scoresSong=null;
		compScheme=null;
	}
	
	public void updateVibrato(){
		
		//song.setFFTParameters();
		//s=new SpectrPane(song, true, false, this);
		
		SpectrPane sp=new SpectrPane();
		for (int i=0; i<songs.length; i++){
			checkAndLoadRawData(i);
			try{
				System.out.println("updating: "+i+" "+songs[i].eleList.size()+" "+songs.length+" "+songs[i].individualName+" "+songs[i].name);
				songs[i].setFFTParameters();
				songs[i].setFFTParameters2(songs[i].nx);
				songs[i].makeMyFFT(0, songs[i].overallLength);
				sp.ny=songs[i].ny;
				sp.dy=songs[i].dy;
				sp.dx=songs[i].dx;
				sp.nx=songs[i].nx;
				sp.nout=songs[i].out;
				sp.song=songs[i];
				sp.updateTrillMeasures();
				dbc.writeSongMeasurements(songs[i]);
			}
			catch (Exception e) {
				//System.out.println(e);
				e.printStackTrace();
			}
		}
		System.out.println("done with update?");
		
	}
	
	public void updateFrequencyChange(){

		SpectrPane sp=new SpectrPane();
		for (int i=0; i<songs.length; i++){
			checkAndLoadRawData(i);
			try{
				System.out.println("updating: "+i+" "+songs[i].eleList.size()+" "+songs.length+" "+songs[i].individualName+" "+songs[i].name);
				System.out.println(songs[i].timeStep+" "+songs[i].dy);
				songs[i].setFFTParameters();
				songs[i].setFFTParameters2(songs[i].nx);
				songs[i].makeMyFFT(0, songs[i].nx);
				//sp.nout=songs[i].out;
				sp.ny=songs[i].ny;
				sp.dy=songs[i].dy;
				sp.dx=songs[i].dx;
				sp.nx=songs[i].nx;
				sp.nout=songs[i].out;
				sp.song=songs[i];
				sp.updateChangeMeasures();
				dbc.writeSongMeasurements(songs[i]);
			}
			catch (Exception e) {
				//System.out.println(e);
				e.printStackTrace();
			}
		}
		System.out.println("done with update?");
	}
	
	public void updateWEntropy(){

		SpectrPane sp=new SpectrPane();
		for (int i=0; i<songs.length; i++){
			checkAndLoadRawData(i);
			try{
				System.out.println("updating: "+i+" "+songs[i].eleList.size()+" "+songs.length+" "+songs[i].individualName+" "+songs[i].name);
				System.out.println(songs[i].timeStep+" "+songs[i].dy);
				songs[i].setFFTParameters();
				songs[i].setFFTParameters2(songs[i].nx);
				songs[i].makeMyFFT(0, songs[i].nx);
				//sp.nout=songs[i].out;
				sp.ny=songs[i].ny;
				sp.dy=songs[i].dy;
				sp.dx=songs[i].dx;
				sp.nx=songs[i].nx;
				sp.nout=songs[i].out;
				sp.song=songs[i];
				sp.updateWienerEntropy();
				dbc.writeSongMeasurements(songs[i]);
			}
			catch (Exception e) {
				//System.out.println(e);
				e.printStackTrace();
			}
		}
		System.out.println("done with update?");
	}
	
	public void updateHarmonicity(){
		SpectrPane sp=new SpectrPane();
		for (int i=0; i<songs.length; i++){
			try{
				System.out.println("updating: "+i+" "+songs[i].eleList.size()+" "+songs.length+" "+songs[i].individualName+" "+songs[i].name);
				System.out.println(songs[i].timeStep+" "+songs[i].dy);
				checkAndLoadRawData(i);
				songs[i].setFFTParameters();
				songs[i].setFFTParameters2(songs[i].nx);
				songs[i].makeMyFFT(0, songs[i].nx);
				sp.nout=songs[i].out;
				sp.ny=songs[i].ny;
				sp.dy=songs[i].dy;
				sp.dx=songs[i].dx;
				sp.nx=songs[i].nx;
				sp.nout=songs[i].out;
				sp.song=songs[i];
				sp.updateHarmonicity();
				dbc.writeSongMeasurements(songs[i]);
			}
			catch (Exception e) {
				//System.out.println(e);
				e.printStackTrace();
			}
		}
		System.out.println("done with update?");
	}
	
	public void interpretSwampSparrowNotes(){
	
		int ind, ind2, ind3, ind4;

	
		LinkedList<int[][]> q=new LinkedList<int[][]>();
		int count=0;
		int newcount=0;
		int increment=0;
		int maxSong=0;
		String[] snnames={"ny", "nc", "pa"};
		for (int i=0; i<songs.length; i++){
			
			newcount++;
			if (newcount==7){
				increment=maxSong;
				newcount=1;
			}
		
			String s=songs[i].notes;
			
			LinkedList<int[]> p=new LinkedList<int[]>();
			
			
			ind=0;
			ind4=0;
			
			while(ind<s.length()){
				ind2=s.indexOf(",", ind);
				ind3=s.indexOf(",", ind2+1);
				ind4=s.indexOf(";", ind);
								
				String t=s.substring(ind, ind2);
				String u=s.substring(ind2+1, ind3);
				String v=s.substring(ind3+1, ind4);
				
							
				int ti=Integer.parseInt(t);
				int ui=Integer.parseInt(u);
				int vi=Integer.parseInt(v);
				
				ti+=increment;
				if (ti>maxSong){
					maxSong=ti;
				}
				
				int sn=0;
				if (songs[i].name.startsWith("nc")){
					sn=1;
				}
				else if (songs[i].name.startsWith("labpa")){
					sn=2;
				}
				
				int[] a={sn, ti, ui, vi};
				p.add(a);
				ind=ind4+1;
			}	
			
			if (p.size()!=songs[i].eleList.size()){
				System.out.println("PROBLEM!: "+p.size()+" "+songs[i].eleList.size());
			}
			
			int[][] b=new int[p.size()][6];
				
			for (int j=0; j<p.size(); j++){
				int[] c=p.get(j);
				
				for (int k=0; k<4; k++){
					b[j][k]=c[k];
				}
				b[j][4]=i;
				b[j][5]=j;
			}	
			q.add(b);
			count+=b.length;
		}
				
		int[][] results=new int[count][7];
	
		count=0;
		
		for (int i=0; i<q.size(); i++){
			int[][] a=q.get(i);
			for (int j=0; j<a.length; j++){
				for (int k=0; k<6; k++){
					results[count][k]=a[j][k];
				}
				count++;
			}				
		}
		
		boolean[] foundYet=new boolean[count];
		
		for (int i=0; i<count; i++){
			foundYet[i]=false;
		}
		
		LinkedList<Song> songlist=new LinkedList<Song>();
		
		for (int i=0; i<count; i++){
			if (!foundYet[i]){
				Song newSong=new Song();
				newSong.eleList=new LinkedList<Element>();
				newSong.syllList=new LinkedList<int[]>();
			
				newSong.name=snnames[results[i][0]]+results[i][1];
				
				System.out.println(results[i][0]+" "+results[i][1]+" "+results[i][2]);
				
				for (int j=0; j<results[i][2]; j++){
					boolean alreadyThere=false;
					for (int k=0; k<count; k++){
						if ((results[k][0]==results[i][0])&&(results[k][1]==results[i][1])&&(results[k][3]==j+1)){
							if (alreadyThere){
								System.out.println("duplicate! "+results[k][0]+" "+results[k][1]+" "+results[k][2]+" "+results[k][3]);
							}
							Element ele=(Element)songs[results[k][4]].eleList.get(results[k][5]);
							newSong.eleList.add(ele);
							foundYet[k]=true;
							
							alreadyThere=true;
						}
					}
				}
				int cumPos=0;
				for (int j=0; j<newSong.eleList.size(); j++){
					Element ele=(Element)newSong.eleList.get(j);
					ele.begintime=cumPos;
					cumPos+=(int)Math.round(50+(ele.length/ele.timeStep));
				}
					
				newSong.interpretSyllables();
				songlist.add(newSong);
			}
		}
		
		songs=new Song[songlist.size()];
		for (int i=0; i<songlist.size(); i++){
			songs[i]=songlist.get(i);
		}
		songlist=null;
		results=null;
		
		songNumber=songs.length;
		
		/*
		int x=1;
		int y=1;
		for (int j=0; j<results.length; j++){
			boolean found=false;
			for (int k=0; k<results.length; k++){
				if ((results[k][0]==x)&&(results[k][2]==y)){
					results[k][5]=j;
					y++;
					if (y>results[k][1]){
						y=1;
						x++;
					}
					found=true;
					k=results.length;
				}
			}
			if (!found){
				j--;
				y=1;
				x++;
			}
		}	
		
		int[][] r=new int[results.length][5];
		int songCount=1;
		for (int i=0; i<r.length; i++){
			for (int j=0; j<r.length; j++){
				if (results[j][5]==i){
					for (int k=0; k<5; k++){
						r[i][k]=results[j][k];
					}
					j=r.length;
					if ((i>0)&&(r[i][0]!=r[i-1][0])){
						songCount++;
					}
				}
			}
		}
		results=null;

		Song[] songs2=new Song[songCount];
		int j=-1;
		int counter=0;
		for (int i=0; i<r.length; i++){
			if ((i==0)||(r[i][0]!=r[i-1][0])){
				j++;
				songs2[j]=new Song();
				songs2[j].eleList=new LinkedList();
				songs2[j].syllList=new LinkedList();
				counter=0;
				songs2[j].name=songs[r[i][3]].name;
			}
			
			Element ele=(Element)songs[r[i][3]].eleList.get(r[i][4]);
			ele.begintime=counter+20;
			counter=ele.begintime+ele.length;
			songs2[j].eleList.add(ele);
		}
		
		for (int i=0; i<songs2.length; i++){
			songs2[i].interpretSyllables();
		}
		
		songs=songs2;
		songNumber=songs.length;
	
		*/
	}
	
	public void countEleNumber(){
		eleNumber=0;
		syllNumber=0;
		eleNumberC=0;
		lookUpSongs=new int[songNumber];
		sylCounts=new int[songNumber];
		eleCounts=new int[songNumber];
		int songCount=songNumber;
		for (int i=0; i<songs.length; i++){
			//if (songs[i].phrases==null){songs[i].interpretSyllables();}
			lookUpSongs[i]=eleNumber;
			if (songs[i].phrases.size()==0){
				//System.out.println("alert");
				songCount--;
			}
			//System.out.println(i+" "+songs[i].phrases.size());
			sylCounts[i]=songs[i].phrases.size();
			eleCounts[i]=songs[i].eleList.size();
			if (songs[i].eleList!=null){
				eleNumber+=songs[i].eleList.size();
			}
			else{
			}
			if (songs[i].phrases!=null){
				syllNumber+=songs[i].phrases.size();
				for (int j=0; j<songs[i].phrases.size(); j++){
					int[][] p=(int[][])songs[i].phrases.get(j);
					eleNumberC+=p[0].length;
				}
			}
		}
		transNumber=syllNumber-songCount;
		//System.out.println(syllNumber+" "+songNumber+" "+songCount);
		if (transNumber<0){
			transNumber=0;
		}
		//transNumber=syllNumber;
		lookUpEls=new int[eleNumber][2];
		lookUpElsC=new int[eleNumberC][3];
		lookUpSyls=new int[syllNumber][2];
		lookUpTrans=new int[transNumber][4];
		
		int count1=0;
		int count2=0;
		int count3=0;
		int count4=0;
		for (int i=0; i<songs.length; i++){
			if (songs[i].eleList!=null){
				int a=songs[i].eleList.size();
				for (int j=0; j<a; j++){
					lookUpEls[count1][0]=i;
					lookUpEls[count1][1]=j;
					count1++;
				}
			}
			if (songs[i].phrases!=null){
				int a=songs[i].phrases.size();
				int c=0;
				for (int j=0; j<a; j++){
					int[][] p=(int[][])songs[i].phrases.get(j);
					for (int k=0; k<p[0].length; k++){
						lookUpElsC[count2][0]=i;
						
						int b=p.length-1;
						while (p[b][k]==-1){b--;}
					
						lookUpElsC[count2][1]=p[b][k];
						lookUpElsC[count2][2]=c;
						count2++;
						c++;
					}				
					lookUpSyls[count3][0]=i;
					lookUpSyls[count3][1]=j;
					
					if (j>0){
						lookUpTrans[count4][0]=i;
						lookUpTrans[count4][1]=j;
						lookUpTrans[count4][2]=count3-1;
						lookUpTrans[count4][3]=count3;
						count4++;
					}
					count3++;
				}
				
				//lookUpTrans[count4][0]=i;
				//lookUpTrans[count4][1]=-1;
				//count4++;
				
			}
		}
		calculateMaxima();
	}

	
	
	public void setSyllLabels(int[] dat){
		System.out.println("Setting syll labels 1");
		if (dat.length==syllNumber){
	
			int max=0;
			int min=100000;
			
			for (int i=0; i<syllNumber; i++){
				if (dat[i]>max){
					max=dat[i];
				}
				if ((dat[i]>=0)&&(dat[i]<min)){
					min=dat[i];
				}
			}
			double maxd=max;
			double mind=min;
			for (int i=0; i<syllNumber; i++){
				syllLabels[i]=(float)((dat[i]-mind)/(maxd-mind));
			}
		}
	}
	
	public double[] createSyllLabelMissTwo(){
		int count=0;
		for (int i=0; i<syllNumber; i++){
			if ((i<syllNumber-2)&&(lookUpSyls[i][0]==lookUpSyls[i+1][0])&&(lookUpSyls[i][0]==lookUpSyls[i+2][0])){
				count++;
			}
		}
		double[] results=new double[count];
		count=0;
		for (int i=0; i<syllNumber; i++){
			if ((i<syllNumber-2)&&(lookUpSyls[i][0]==lookUpSyls[i+1][0])&&(lookUpSyls[i][0]==lookUpSyls[i+2][0])){
				double n=songs[lookUpSyls[i][0]].phrases.size();
				n--;
				int m=lookUpSyls[i][1];
				results[count]=m/n;
				count++;
			}
		}
		return(results);
	}
		
	public float[][] contractSyllDists(){
	
		int count=0;
		for (int i=0; i<syllNumber; i++){
			if ((i<syllNumber-2)&&(lookUpSyls[i][0]==lookUpSyls[i+1][0])&&(lookUpSyls[i][0]==lookUpSyls[i+2][0])){
				count++;
			}
		}
		float[][] results=new float[count][];
		for (int i=0; i<count; i++){
			results[i]=new float[i+1];
		}
		
		count=0;
		
		count=0;
		
		int lookUpSyls2[][]=new int[syllNumber-2*songNumber][2];
		String[] syllNames2=new String[syllNumber-2*songNumber];
		for (int i=0; i<syllNumber; i++){
			if ((i<syllNumber-2)&&(lookUpSyls[i][0]==lookUpSyls[i+1][0])&&(lookUpSyls[i][0]==lookUpSyls[i+2][0])){
				
				lookUpSyls2[count][0]=lookUpSyls[i][0];
				lookUpSyls2[count][1]=lookUpSyls[i][1];
				syllNames2[count]=syllNames[i];
				
				int count2=0;
				for (int j=0; j<i; j++){
					if ((j<syllNumber-2)&&(lookUpSyls[j][0]==lookUpSyls[j+1][0])&&(lookUpSyls[j][0]==lookUpSyls[j+2][0])){
						results[count][count2]=scoresSyll[i][j];
						count2++;
					}
				}
				count++;
			}
		}
		
		lookUpSyls=lookUpSyls2;
		syllNumber=lookUpSyls.length;
		syllNames=syllNames2;
		return results;
	}
	
	
	public int[][] getSequencePattern(){
	
		int[][] p=new int[syllNumber-songNumber][2];
		
		int j=0;
		for (int i=0; i<syllNumber-1; i++){
			if (lookUpSyls[i][0]==lookUpSyls[i+1][0]){
				p[j][0]=i;
				p[j][1]=i+1;
				j++;
			}
		}
	
	
		return p;
	
	}
	
	
	public void createSyllLabelsPosition(int type){
		
		System.out.println("Setting syll labels 2 - position");
		
		syllLabels=new double[syllNumber];
		for (int i=0; i<syllNumber; i++){
		
			int count=0;
			while ((i+count<syllNumber)&&(lookUpSyls[i+count][0]==lookUpSyls[i][0])){
				count++;
			}
			double n=lookUpSyls[i+count-1][1]+1;
		
			//double n=songs[lookUpSyls[i][0]].phrases.size();
			
			
			
			n--;
			int m=lookUpSyls[i][1];
			if (type==0){
				syllLabels[i]=m;
			}
			else if (type==1){
				syllLabels[i]=n-m;
			}
			else{
				syllLabels[i]=m/n;
			}
		}
		
	}
	
	
	
	
	
	
	public void labelSyllables(){
		
		System.out.println("Setting syll labels 3");
		
		syllLabels=new double[syllNumber];
		double max=0;
		for (int i=0; i<songs.length; i++){
			if (songs[i].phrases.size()>max){max=songs[i].phrases.size();}
		}
		
		
		
		for (int i=0; i<syllNumber; i++){
		
			
		
			/*
			int j=lookUpSyls[i][0];
			String s=songs[j].species;
			if (s.startsWith("L")){
				syllLabels[i]=0;
			}
			else if(s.startsWith("H")){
				syllLabels[i]=1;
			}
			else{
				syllLabels[i]=0.5;
			}
			*/
			/*
			int j=lookUpSyls[i][0];
			String s=songs[j].individualName;
			if (s.startsWith("cap")){
				syllLabels[i]=0;
			}
			else if(s.startsWith("vin")){
				syllLabels[i]=1;
			}
			else{
				syllLabels[i]=0.5;
			}
			*/
			//syllLabels[i]=(songs[lookUpSyls[i][0]].phrases.size()-lookUpSyls[i][1])/(max);
		
			
				
			syllLabels[i]=lookUpSyls[i][1]/(songs[lookUpSyls[i][0]].phrases.size()-1.0);
			//System.out.println(syllLabels[i]+" "+lookUpSyls[i][1]+" "+songs[lookUpSyls[i][0]].phrases.size());
			/*int j=lookUpSyls[i][0];
			if (songs[j].name.startsWith("H")){
				syllLabels[i]=0;
			}
			else if (songs[j].name.startsWith("G")){
				syllLabels[i]=0.5;
			}
			else{
				syllLabels[j]=1;
			}*/
		}
	}
	
	public void labelTransitions(){
		transLabels=new double[transNumber];
		for (int i=0; i<transNumber; i++){
			transLabels[i]=(lookUpTrans[i][1]-1.0)/(songs[lookUpTrans[i][0]].phrases.size()-2.0);
			if (songs[lookUpTrans[i][0]].phrases.size()<=2){
				transLabels[i]=0.5;
			}
		}
	}

	
	public void labelElements(){
		if (compressElements){
			eleLabels=new double[eleNumberC];
			for (int i=0; i<eleNumberC; i++){
				int j=lookUpElsC[i][0];
				double a=0;
				for (int k=0; k<songs[j].phrases.size(); k++){
					int[][]p=(int[][])songs[j].phrases.get(k);
					a+=p[0].length;
					
				}
				a--;
				eleLabels[i]=lookUpElsC[i][2]/a;
				
				//System.out.println(eleLabels[i]+" "+lookUpElsC[i][2]+" "+a);
				
			}
		}
		else{
			eleLabels=new double[eleNumber];
			for (int i=0; i<eleNumber; i++){
				//eleLabels[i]=lookUpEls[i][1]/(songs[lookUpEls[i][0]].eleList.size()-1.0);
				eleLabels[i]=lookUpEls[i][0]/(songNumber-1.0);
			}
		}
	}
	
	public void labelSongs(){
		songLabels=new double[songs.length];
		
		for (int i=0; i<songs.length; i++){
		
			String s=songs[i].individualName;
			if (s.startsWith("cap")){
				songLabels[i]=0;
			}
			else if(s.startsWith("vin")){
				songLabels[i]=1;
			}
			else{
				songLabels[i]=0.5;
			}

		
		/*
			if (songs[i].name.startsWith("H")){
				songLabels[i]=0;
			}
			else if (songs[i].name.startsWith("G")){
				songLabels[i]=0.5;
			}
			else{
				songLabels[i]=1;
			}
		*/
		}
	}
	
	public void getPopulationNames(){
		LinkedList<String> populationName=new LinkedList<String>();
		for (int i=0; i<songs.length; i++){
			String s=songs[i].population;
			boolean matched=false;
			for (int j=0; j<populationName.size(); j++){
				String t=populationName.get(j);
				if (t.startsWith(s)){
					matched=true;
					j=populationName.size();
				}
			}
			if (!matched){
				//System.out.println("NEW POPULATION: "+s);
				populationName.add(s);
			}
		}
		populations=new String[populationName.size()];
		
		populations=populationName.toArray(populations);
	}
	
	public void getSpeciesNames(){
		LinkedList<String> speciesName=new LinkedList<String>();
		for (int i=0; i<songs.length; i++){
			String s=songs[i].species;
			boolean matched=false;
			for (int j=0; j<speciesName.size(); j++){
				String t=speciesName.get(j);
				if (t.startsWith(s)){
					matched=true;
					j=speciesName.size();
				}
			}
			if (!matched){
				//System.out.println("NEW POPULATION: "+s);
				speciesName.add(s);
			}
		}
		species=new String[speciesName.size()];
		
		species=speciesName.toArray(species);
	}
	

	
	public int[] getPopulationListArray(int h){
		getPopulationNames();
		int[] results=new int[1];
		if (h==0){
			results=new int[lookUpEls.length];
		
			for (int i=0; i<lookUpEls.length; i++){
				for (int j=0; j<populations.length; j++){
					if (songs[lookUpEls[i][0]].population.equals(populations[j])){
						results[i]=j;
						j=populations.length;
					}
				}
			}
		}
		if (h==1){
			results=new int[lookUpElsC.length];
		
			for (int i=0; i<lookUpElsC.length; i++){
				for (int j=0; j<populations.length; j++){
					if (songs[lookUpElsC[i][0]].population.equals(populations[j])){
						results[i]=j;
						j=populations.length;
					}
				}
			}
		}
		if (h==2){
			results=new int[lookUpSyls.length];
		
			for (int i=0; i<lookUpSyls.length; i++){
				for (int j=0; j<populations.length; j++){
					if (songs[lookUpSyls[i][0]].population.equals(populations[j])){
						results[i]=j;
						j=populations.length;
					}
				}
			}
		}
		else if (h==3){
			results=new int[lookUpTrans.length];
			
			for (int i=0; i<lookUpTrans.length; i++){
				for (int j=0; j<populations.length; j++){
					if (songs[lookUpTrans[i][0]].population.equals(populations[j])){
						results[i]=j;
						j=populations.length;
					}
				}
			}
		}
		else if (h==4){
			results=new int[songs.length];
			for (int i=0; i<songs.length; i++){
				for (int j=0; j<populations.length; j++){
					if (songs[i].population.equals(populations[j])){
						results[i]=j;
						j=populations.length;
					}
				}
			}
		}
		return results;
	}
	
	public int lookUpPopulation(int a, int b){
		int c=0;
		if (a==0){
			c=lookUpEls[b][0];
		}
		else if (a==1){
			c=lookUpElsC[b][0];
		}
		else if (a==2){
			c=lookUpSyls[b][0];
		}
		else if (a==3){
			c=lookUpTrans[b][0];
		}
		else{
			c=b;
		}
		int r=0;
		for (int i=0; i<populations.length; i++){
			if (populations[i].startsWith(songs[c].population)){
				r=i;
				i=populations.length;
			}
		}
		return r;
	}
		
	
	public int[] getSpeciesListArray(int h){
		getSpeciesNames();
		int[] results=new int[1];
		if (h==0){
			results=new int[lookUpEls.length];
		
			for (int i=0; i<lookUpEls.length; i++){
				for (int j=0; j<species.length; j++){
					if (songs[lookUpEls[i][0]].species.equals(species[j])){
						results[i]=j;
						j=species.length;
					}
				}
			}
		}
		if (h==1){
			results=new int[lookUpElsC.length];
		
			for (int i=0; i<lookUpElsC.length; i++){
				for (int j=0; j<species.length; j++){
					if (songs[lookUpElsC[i][0]].species.equals(species[j])){
						results[i]=j;
						j=species.length;
					}
				}
			}
		}
		if (h==2){
			results=new int[lookUpSyls.length];
		
			for (int i=0; i<lookUpSyls.length; i++){
				for (int j=0; j<species.length; j++){
					if (songs[lookUpSyls[i][0]].species.equals(species[j])){
						results[i]=j;
						j=species.length;
					}
				}
			}
		}
		else if (h==3){
			results=new int[lookUpTrans.length];
			
			for (int i=0; i<lookUpTrans.length; i++){
				for (int j=0; j<species.length; j++){
					if (songs[lookUpTrans[i][0]].species.equals(species[j])){
						results[i]=j;
						j=species.length;
					}
				}
			}
		}
		else if (h==4){
			results=new int[songs.length];
			for (int i=0; i<songs.length; i++){
				for (int j=0; j<species.length; j++){
					if (songs[i].species.equals(species[j])){
						results[i]=j;
						j=species.length;
					}
				}
			}
		}
		return results;
	}
		
	
	public int[] getPositionListArray(int h){
		
		int[] results=new int[1];
		
		if (h==1){

			results=new int[lookUpSyls.length];
		
			for (int i=0; i<lookUpSyls.length; i++){
				
				int j=i;
				while ((j<lookUpSyls.length)&&(lookUpSyls[j][0]==lookUpSyls[i][0])){
					j++;
				}
				j--;
			
				double p=lookUpSyls[i][1]/(lookUpSyls[j][1]+0.0);
			
				results[i]=(int)Math.round(p*6);
			
			}
		}
		else if (h==2){
			results=new int[lookUpTrans.length];
			
			for (int i=0; i<lookUpTrans.length; i++){
				
				int j=i;
				while ((j<lookUpTrans.length)&&(lookUpTrans[j][0]==lookUpTrans[i][0])){
					j++;
				}
				j--;
				
				double p=lookUpTrans[i][1]/(lookUpTrans[j][1]+0.0);
				
				results[i]=(int)Math.round(p*6);
				
			}
		}
		
		return results;
	}

	
	
	
	public void makeNames(){
		countEleNumber();
		makeEleNames();
		makeEleNamesC();
		makeSyllNames();
		makeTransNames();
		makeSongNames();
		labelElements();
		labelSyllables();
		labelTransitions();
		labelSongs();
		getPopulationNames();
	}
	
	public void makeEleNames(){
		eleNames=new String[eleNumber];
		int count=0;
		for (int i=0; i<songs.length; i++){
			String n3=songs[i].name;
			if (n3.endsWith(".wav")){
				int length=n3.length();
				n3=songs[i].name.substring(0, length-4);
			}
			for (int j=0; j<songs[i].eleList.size(); j++){
				Integer gr=new Integer(j+1);
				eleNames[count]=songs[i].individualName+":"+n3+","+gr.toString();
				//eleNames[count]=songs[i].individualName;
				count++;
			}
		}
	}
	
	public void makeEleNamesC(){
		eleNamesC=new String[eleNumberC];
		int count=0;
		int maxLength=0;
		for (int i=0; i<songs.length; i++){
			if (songs[i].eleList.size()>maxLength){
				maxLength=songs[i].eleList.size();
			}
		}
		String[] numberString=new String[maxLength+1];
		for (int i=0; i<numberString.length; i++){
			Integer gr1=new Integer(i);
			numberString[i]=gr1.toString();
		}
		for (int i=0; i<songs.length; i++){
			String n3=songs[i].name;
			if (n3.endsWith(".wav")){
				int length=n3.length();
				n3=songs[i].name.substring(0, length-4);
			}
			for (int j=0; j<songs[i].phrases.size(); j++){
				int[][] p=(int[][])songs[i].phrases.get(j);
				
				for (int k=0; k<p[0].length; k++){
					StringBuffer sb=new StringBuffer();
					
					for (int w=0; w<p.length; w++){
						if (p[w].length>k){
							sb.append(numberString[p[w][k]+1]+",");
						}
					}
					sb.deleteCharAt(sb.length()-1);
					eleNamesC[count]=songs[i].individualName+": "+n3+": "+sb.toString();
					
					//eleNamesC[count]=songs[i].individualName+":"+n3+","+numberString[j+1]+"."+numberString[k+1];
					
					count++;
				}
			}
		}
	}
	
	public void makeSyllNames(){
		System.out.println("Setting syll labels 4");
		syllNames=new String[syllNumber];
		syllLabels=new double[syllNumber];
		int count=0;
		for (int i=0; i<songs.length; i++){
			String n3=songs[i].name;
			if (n3.endsWith(".wav")){
				int length=n3.length();
				n3=songs[i].name.substring(0, length-4);
			}
			for (int j=0; j<songs[i].phrases.size(); j++){
				Integer gr=new Integer(j+1);
				syllNames[count]=songs[i].individualName+":"+n3+","+gr.toString();
				
				
				if (syllNames[count].startsWith("ElementOne")){
					syllNames[count]="1";
					syllLabels[count]=0;
				}
				if (syllNames[count].startsWith("ElementTwo")){
					syllNames[count]="2";
					syllLabels[count]=0.2;
				}
				if (syllNames[count].startsWith("ElementThree")){
					syllNames[count]="3";
					syllLabels[count]=0.4;
				}
				if (syllNames[count].startsWith("ElementFour")){
					syllNames[count]="4";
					syllLabels[count]=0.6;
				}
				if (syllNames[count].startsWith("ElementFive")){
					syllNames[count]="5";
					syllLabels[count]=0.8;
				}
				if (syllNames[count].startsWith("ElementSix")){
					syllNames[count]="6";
					syllLabels[count]=1;
				}
				
				count++;
			}
		}
	}
	
	public void makeTransNames(){
		transNames=new String[transNumber];
		
		for (int i=0; i<transNumber; i++){
			String n=songs[lookUpTrans[i][0]].name;
			if (n.endsWith(".wav")){
				int length=n.length();
				n=n.substring(0, length-4);
			}
			Integer gr=new Integer(lookUpTrans[i][1]+1);
			transNames[i]=songs[lookUpTrans[i][0]].individualName+":"+n+","+gr.toString();
		}
	}
	
	public void makeSongNames(){
		songNames=new String[songNumber];
		for (int i=0; i<songs.length; i++){
			String n3=songs[i].name;
			if (n3.endsWith(".wav")){
				int length=n3.length();
				n3=songs[i].name.substring(0, length-4);
			}
			
			//songNames[i]=songs[i].individualName+":"+n3;
			songNames[i]=n3;
		}                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
	}
	
	
	public void calculateMaxima(){
		maxEleLength=0;
		maxSyllLength=0;
		maxTransLength=0;
		maxSongLength=0;
		int a;
		for (int i=0; i<songs.length; i++){
			for (int j=0; j<songs[i].eleList.size(); j++){
				Element ele=(Element)songs[i].eleList.get(j);
				a=ele.length;
				if (a>maxEleLength){maxEleLength=a;}
			}
			int syllLengthPrev=0;
			for (int j=0; j<songs[i].phrases.size(); j++){
				int[][]p=(int[][])songs[i].phrases.get(j);
				boolean problem=false;
				for (int k=0; k<p.length; k++){
					if (p[k].length==0){problem=true;}
				}
				if (problem){
					String s="There seems to be a problem with individual "+songs[i].individualName+", song "+songs[i].name+", syllable "+(j+1);
					JOptionPane.showMessageDialog(null,s,"Alert!", JOptionPane.ERROR_MESSAGE);
				}
				else{
					a=p.length-1;
					while (p[a][p[a].length-1]==-1){a--;}
				
					Element ele1=(Element)songs[i].eleList.get(p[a][0]);
					Element ele2=(Element)songs[i].eleList.get(p[a][p[a].length-1]);
				
					int syllLength=ele2.begintime+ele2.length-ele1.begintime;
				
					if (syllLength>maxSyllLength){maxSyllLength=syllLength;}
					
					int transLength=syllLength+syllLengthPrev;
					if (transLength>maxTransLength){maxTransLength=transLength;}
					
					syllLengthPrev=(int)Math.round(syllLength+(ele2.timeAfter/ele2.timeStep));
					
				}
			}
			Element ele1=(Element)songs[i].eleList.get(0);
			Element ele2=(Element)songs[i].eleList.get(songs[i].eleList.size()-1);
			
			int songLength=ele2.begintime+ele2.length-ele1.begintime;
			if (songLength>maxSongLength){maxSongLength=songLength;}
		}
	}

	public void calculateIndividuals(){
		//This method calculates the class arrays "individuals" which contains the song-types, transitions and elements owned by each individual in the set of songs
		int countInds=0;
		int[][] indLocs=new int[songNumber][5];
		for (int i=0; i<songNumber; i++){
			boolean found=false;
			for (int j=0; j<countInds; j++){
				if (songs[i].individualID==indLocs[j][0]){
					indLocs[j][1]++;
					indLocs[j][2]+=songs[i].eleList.size();
					indLocs[j][3]+=songs[i].phrases.size();
					indLocs[j][4]+=songs[i].phrases.size()-1;
					found=true; 
					j=countInds;
				}
			}
			if(!found){
				indLocs[countInds][0]=songs[i].individualID;
				System.out.println((countInds+1)+" "+songs[i].individualName);
				indLocs[countInds][1]=1;
				indLocs[countInds][2]=songs[i].eleList.size();
				indLocs[countInds][3]=songs[i].phrases.size();
				indLocs[countInds][4]=songs[i].phrases.size()-1;
				countInds++;
			}
		}
		individualSongs=new int[countInds][];
		for (int i=0; i<countInds; i++){
			individualSongs[i]=new int[indLocs[i][1]];
			int count2=0;
			for (int j=0; j<songNumber; j++){
				if (songs[j].individualID==indLocs[i][0]){
					individualSongs[i][count2]=j; 
					count2++;
				}
			}
		}
		individualTrans=new int[countInds][];
		for (int i=0; i<countInds; i++){
			individualTrans[i]=new int[indLocs[i][4]];
			int count2=0;
			for (int j=0; j<lookUpTrans.length; j++){
				int p=lookUpTrans[j][0];
				if (songs[p].individualID==indLocs[i][0]){
					individualTrans[i][count2]=j; 
					count2++;
				}
			}
		}
		individualSyl=new int[countInds][];
		for (int i=0; i<countInds; i++){
			individualSyl[i]=new int[indLocs[i][3]];
			int count2=0;
			for (int j=0; j<lookUpSyls.length; j++){
				int p=lookUpSyls[j][0];
				if (songs[p].individualID==indLocs[i][0]){
					individualSyl[i][count2]=j; 
					count2++;
				}
			}
		}
		individualEle=new int[countInds][];
		for (int i=0; i<countInds; i++){
			individualEle[i]=new int[indLocs[i][2]];
			int count2=0;
			for (int j=0; j<lookUpEls.length; j++){
				int p=lookUpEls[j][0];
				if (songs[p].individualID==indLocs[i][0]){
					individualEle[i][count2]=j; 
					count2++;
				}
			}
		}
		indLocs=null;
		individualNumber=countInds;
	}
	
	public int[] getIndividualListArray(int h){
		calculateIndividuals();
		int[] results=new int[1];
		
		
		if (h==1){
			results=new int[syllNumber];
		
			for (int i=0; i<syllNumber; i++){
			
				int p=lookUpSyls[i][0];
			
			
				for (int j=0; j<individualSongs.length; j++){
					for (int k=0; k<individualSongs[j].length; k++){
						if (individualSongs[j][k]==p){
							results[i]=j;
							j=individualSongs.length-1;
							k=individualSongs[j].length; 
						}
					}
				}
			}
		}
		else if (h==2){
			results=new int[transNumber];
			
			for (int i=0; i<transNumber; i++){
				
				int p=lookUpTrans[i][0];
				
				
				for (int j=0; j<individualSongs.length; j++){
					for (int k=0; k<individualSongs[j].length; k++){
						if (individualSongs[j][k]==p){
							results[i]=j;
							j=individualSongs.length-1;
							k=individualSongs[j].length; 
						}
					}
				}
			}
		}
		return results;	
	}
	
	public int[] calculateSongAssignments(int type){
		
		int[] results;
		
		if (type==0){
			results=new int[lookUpEls.length];
			
			for (int i=0; i<lookUpEls.length; i++){
				for (int j=0; j<individualSongs.length; j++){
					for (int k=0; k<individualSongs[j].length; k++){
						if (individualSongs[j][k]==lookUpEls[i][0]){
							results[i]=j;
						}
					}
				}
			}
		}
		else if (type==1){
			results=new int[lookUpElsC.length];
			
			for (int i=0; i<lookUpElsC.length; i++){
				for (int j=0; j<individualSongs.length; j++){
					for (int k=0; k<individualSongs[j].length; k++){
						if (individualSongs[j][k]==lookUpElsC[i][0]){
							results[i]=j;
						}
					}
				}
			}
		}
		else if (type==2){
			results=new int[lookUpSyls.length];
			
			for (int i=0; i<lookUpSyls.length; i++){
				for (int j=0; j<individualSongs.length; j++){
					for (int k=0; k<individualSongs[j].length; k++){
						if (individualSongs[j][k]==lookUpSyls[i][0]){
							results[i]=j;
						}
					}
				}
			}
		}
		else if (type==3){
			results=new int[lookUpTrans.length];
			
			for (int i=0; i<lookUpTrans.length; i++){
				for (int j=0; j<individualSongs.length; j++){
					for (int k=0; k<individualSongs[j].length; k++){
						if (individualSongs[j][k]==lookUpTrans[i][0]){
							results[i]=j;
						}
					}
				}
			}
		}
		else {
			results=new int[songs.length];
			
			for (int i=0; i<songs.length; i++){
				for (int j=0; j<individualSongs.length; j++){
					for (int k=0; k<individualSongs[j].length; k++){
						if (individualSongs[j][k]==i){
							results[i]=j;
						}
					}
				}
			}
		}
		
		
		
		
		return results;
		
		
	}
	
	public void getValidParameters(boolean type){
		numParams=0;
		numTempParams=0;
		for (int i=0; i<21; i++){
			if ((type)&&(parameterValues[i]>0)){numParams++;}
			if ((!type)&&(statValues[i]>0)){numParams++;}
		}
		for (int i=0; i<2; i++){
			if ((type)&&(parameterValues[i]>0)){numTempParams++;}
			if ((!type)&&(statValues[i]>0)){numTempParams++;}
		}
		validParameters=new double[numParams];
		paramType=new int[numParams];
		numParams=0;
		for (int i=0; i<21; i++){
			if ((type)&&(parameterValues[i]>0)){
				validParameters[numParams]=parameterValues[i];
				paramType[numParams]=i;
				numParams++;
			}
			if ((!type)&&(statValues[i]>0)){
				validParameters[numParams]=statValues[i];
				paramType[numParams]=i;
				numParams++;
			}
		}
	}
	
	public double[][] compressElement(Element ele){
		int s=ele.length;
		int s2=s-2;
		
		int npa2=numParams;
		if (weightByAmp){npa2++;}
		double diff=ele.measurements[1][11]-ele.measurements[0][11];
		if (diff<10){diff=10;}
		double diff2=diff+ele.measurements[0][11];
		
		double[][]d=new double[s][npa2];
		
		for (int i=0; i<s; i++){
			int ii=i+5;
			for (int j=0; j<numParams; j++){
				if (paramType[j]==0){
					d[i][j]=ele.timeStep*i;
				}
				else if (paramType[j]==1){
					d[i][j]=i/s2;
				}
				else if (paramType[j]==16){
					if (ele.timeAfter!=-10000){d[i][j]=ele.timeAfter;}
					else{d[i][j]=0.5;}
				}
				else if ((paramType[j]<6)&&(logFrequencies)){
					d[i][j]=Math.log(ele.measurements[ii][paramType[j]-2]);
				}
				else if (paramType[j]<10){
					d[i][j]=ele.measurements[ii][paramType[j]-2];
					if (!(d[i][j]<1)&&!(d[i][j]>0)){
						d[i][j]=0.5;
					}
				}
				else if (paramType[j]<14){
					d[i][j]=ele.measurements[ii][paramType[j]-2];
					
				}
				else{
					d[i][j]=ele.measurements[ii][paramType[j]-1];
					
				}
				if (weightByAmp){
					d[i][numParams]=(diff2-ele.measurements[ii][11])/diff;
				}
			}
		}
		double[] stDev=getStandardDeviations(d);
		for (int i=0; i<stDev.length; i++){
			stDev[i]/=10.0;
		}
		
		
		double[] scores=new double[s];
		
		boolean[] status=new boolean[s];
		boolean[] bestStat=new boolean[s];
		for (int i=0; i<s; i++){
			status[i]=true;
			bestStat[i]=true;
		}
		
		double log2Adj=1/Math.log(2);
		
		for (int i=1; i<s2; i++){
			double bestScore=100000000;
			int loc=0;
			for (int j=1; j<=s2; j++){
				
				if (status[j]){
					double overallScore=0;
					status[j]=false;
					
					for (int g=1; g<s; g++){
						if (!status[g]){
							int j1=g-1;
							int j2=g;
							while(!status[j2]){j2++;}
							double dist=j2-j1;
							
							for (int k=j1+1; k<j2; k++){
								double score=0;
								for (int a=0; a<numParams; a++){
									double estParam=(((d[j2][a]-d[j1][a])/dist)*(k-j1)+d[j1][a]-d[k][a])/stDev[a];
									score+=estParam*estParam;
								}
								score=Math.log(Math.sqrt(score))*log2Adj;
								if (score<0){score=0;}
								overallScore+=score;
							}
							
							g=j2;
						}
					}
					status[j]=true;
					if (overallScore<bestScore){
						bestScore=overallScore;
						loc=j;
					}
				}
			}
			
			scores[i]=bestScore+2*Math.log(s-i)*log2Adj;
			status[loc]=false;
			boolean found=false;
			for (int j=s/3; j<i; j++){
				if (scores[j]<scores[i]){
					found=true;
					j=i;
				}
			}
			if (!found){
				for (int j=0; j<s; j++){
					bestStat[j]=status[j];
				}
			}
		}
		
		int compLength=0;
		for (int i=0; i<s; i++){
			if (bestStat[i]){
				compLength++;
			}
		}
		double d2[][]=new double[npa2][compLength];
		
		int a=0;
		for (int i=0; i<s; i++){
			if (bestStat[i]){
				for (int j=0; j<npa2; j++){
					d2[j][a]=d[i][j];
				}
				a++;
			}
		}
		
		return d2;
	
	}
	
	public double[] getStandardDeviations(double[][] d){
		
		int length=d.length;
		int dims=d[0].length;
	
		double[] sd=new double[dims];
	
		double[] avs=getAverages(d);
	
		for (int i=0; i<length; i++){
			for (int j=0; j<dims; j++){
				double w=d[i][j]-avs[j];
				sd[j]+=w*w;
			}
		}
		
		for (int i=0; i<dims; i++){
			sd[i]=Math.sqrt(sd[i]/(length-1.0));
		}
	
		return (sd);
	}
	
	public double[] getAverages(double[][]d){
		int length=d.length;
		int dims=d[0].length;
	
		double[] avs=new double[length];
	
		for (int i=0; i<length; i++){
			for (int j=0; j<dims; j++){
				avs[j]+=d[i][j];
			}
		}
		
		double p=length;
		for (int i=0; i<dims; i++){
			avs[i]/=p;
		}
	
		return (avs);
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
			int eleSizeS=songs[i].eleList.size();
			for (int j=0; j<eleSizeS; j++){
				Element ele=(Element)songs[i].eleList.get(j);
				
				int y=0;
				for (int a=0; a<14; a++){
					if (parameterMatrix[0][a]){
						dataSum[x][y]=ele.measurements[4][a];
						y++;
					}
					if (parameterMatrix[1][a]){
						dataSum[x][y]=ele.measurements[0][a];
						y++;
					}
					if (parameterMatrix[2][a]){
						dataSum[x][y]=ele.measurements[1][a];
						y++;
					}
					if (parameterMatrix[3][a]){
						dataSum[x][y]=ele.measurements[2][a]/(ele.timeStep*ele.length);
						y++;
					}
					if (parameterMatrix[4][a]){
						dataSum[x][y]=ele.measurements[3][a]/(ele.timeStep*ele.length);
						y++;
					}
					if (parameterMatrix[5][a]){
						dataSum[x][y]=ele.measurements[5][a];
						y++;
					}
					if (parameterMatrix[6][a]){
						dataSum[x][y]=ele.measurements[ele.measurements.length-1][a];
						y++;
					}
					if (parameterMatrix[7][a]){
						double[] temp=new double[ele.measurements.length-5];
						for (int k=0; k<temp.length; k++){temp[k]=ele.measurements[k+5][a];}
						dataSum[x][y]=bs.calculateSD(temp, true);
						y++;
					}
				}
				if (parameterMatrix[0][14]){
					dataSum[x][y]=Math.log(ele.length*ele.timeStep);
					y++;
				}
				if (parameterMatrix[0][15]){
					dataSum[x][y]=ele.timeAfter;
					if (ele.timeAfter==-10000){
						dataSum[x][y]=20;
					}
				}
				x++;
			}
		}
	}	
	
	public void compressData(){
		
		data=new double[eleNumber][][];
		int count=0;
		for (int i=0; i<songs.length; i++){
			int eleSizeS=songs[i].eleList.size();
			for (int j=0; j<eleSizeS; j++){
				Element ele=(Element)songs[i].eleList.get(j);
				data[count]=compressElement(ele);
				count++;
			}
		}
	}
	
	
	
	
	
	public void compressData2(){
	
		int npa2=numParams+1;
		if (weightByAmp){npa2++;}
		
		data=new double[eleNumber][][];
		int[] count;
						
		double reductionFactor=mainReductionFactor;
		//double ef=1/Math.log(mainReductionFactor);
		
		int s,t,c;
		int np=numParams;
		int p=0;
		double s2;
		for (int i=0; i<songs.length; i++){
			int eleSizeS=songs[i].eleList.size();

			for (int j=0; j<eleSizeS; j++){
				Element ele=(Element)songs[i].eleList.get(j);
				s=ele.length;
				
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
				data[p]=new double[npa2][t];
				count=new int[t];
				double diff=ele.measurements[0][11]-ele.measurements[1][11];
				double diff2=ele.measurements[0][11];
				
				//System.out.println(s+" "+t+" "+reductionFactor+" "+ele.timeStep);
				
				for (int a=0; a<s; a++){
					c=(int)Math.floor(a*reductionFactor+0.0000000001);
					if (c==t){c--;}
					
					//System.out.println(a+" "+c);
					
					count[c]++;
					int ab=a+5;
					
					for (int q=0; q<numParams; q++){
						
						if (paramType[q]==0){
							data[p][q][c]+=ele.timeStep*a;
							//binned[c][q][count[c]-1]=ele.timeStep*a;
						}
						else if (paramType[q]==1){
							data[p][q][c]+=a/s2;
							//binned[c][q][count[c]-1]=a/s2;
						}
						else if (paramType[q]==16){
							if (ele.timeAfter!=-10000){
								data[p][q][c]+=ele.timeAfter;
								//binned[c][q][count[c]-1]=ele.timeAfter;
							}
							else{
								//binned[c][q][count[c]-1]=50;
								data[p][q][c]+=50;
							}
						}
						else if (paramType[q]<6){
							if(logFrequencies){
								data[p][q][c]+=Math.log(ele.measurements[ab][paramType[q]-2]);
							}
							else{
								data[p][q][c]+=ele.measurements[ab][paramType[q]-2];
							}
							//binned[c][q][count[c]-1]=Math.log(ele.measurements[ab][paramType[q]-2]);
						}
						/*
						else if (paramType[q]<10){
							if(logFrequencies){
								data[p][q][c]+=Math.log(ele.measurements[ab][paramType[q]-6])-Math.log(ele.measurements[5][paramType[q]-6]);
							}
							else{
								data[p][q][c]+=ele.measurements[ab][paramType[q]-6]-ele.measurements[5][paramType[q]-6];
							}
						}
						*/
						else if (paramType[q]<14){
							data[p][q][c]+=ele.measurements[ab][paramType[q]-2];

						}
						else if (paramType[q]==15){
								
							double ta=ele.measurements[ab][paramType[q]-2];
								
							ta=Math.log(Math.max(ta,1));
								//if (ta<10){ta=10;}
								//if (ta>50){ta=50;}
								//System.out.println("VA "+ab+" "+ta+" "+c+" "+ele.measurements[ab][paramType[q]-1]);
							data[p][q][c]+=ta;
								//binned[c][q][count[c]-1]=ta;
								 
						}
						else if ((paramType[q]>16)&&(paramType[q]<21)){
							data[p][q][c]+=ele.measurements[ab][paramType[q]-17]-ele.measurements[4][paramType[q]-17];
						}
					
						else{
							data[p][q][c]+=ele.measurements[ab][paramType[q]-2];
								//binned[c][q][count[c]-1]=ele.measurements[ab][paramType[q]-1];
						}
						
					}
					if (weightByAmp){
						data[p][np][c]+=(diff2-ele.measurements[ab][11])/diff;
						data[p][np][c]+=0.01;
						//binned[c][np][count[c]-1]=((diff2-ele.measurements[ab][11])/diff)+0.5;
					}					
				}
				for (int a=0; a<t; a++){
					for (int b=0; b<numParams; b++){
						data[p][b][a]/=count[a]+0.0;
						//System.out.println(a+" "+b+" "+data[p][b][a]+" "+count[a]);
						//data[p][b][a]=bs.calculateMedian(binned[a][b]);
					}
					if (weightByAmp){
						data[p][np][a]/=count[a]+0.0;
						//System.out.println(a+" "+data[p][np][a]);
						//data[p][np][a]=bs.calculateMedian(binned[a][np]);
					}
				}
				data[p][data[p].length-1][t-1]=1;
				p++;
			}
		}
	}

	
	public double[][][] stitchSyllables(){		//a method to stitch together elements to generate one submatrix for each syllable
	
		int countSyls=0;
		for (int i=0; i<songs.length; i++){
			for (int j=0; j<songs[i].phrases.size(); j++){
				int[][] p=(int[][]) songs[i].phrases.get(j);
				countSyls+=p.length;
			}
		}
		System.out.println("Number of syls: "+countSyls);
		int dims=data[0].length;
		double[][][] dataSyls=new double[countSyls][dims][];
		elementPos=new int[countSyls][];
		countSyls=0;
		int countEls=0;
		for (int i=0; i<songs.length; i++){
			//System.out.println(songs[i].name);
			for (int j=0; j<songs[i].phrases.size(); j++){
				int[][] p=(int[][]) songs[i].phrases.get(j);
				
				for (int k=0; k<p.length; k++){
					int elePos=countEls;
					
					int sylLength=0;
					for (int g=0; g<p[k].length; g++){
						if (p[k][g]>=0){
							sylLength+=data[elePos][0].length;
							elePos++;
						}
					}
					
					for (int g=0; g<dims; g++){
						dataSyls[countSyls][g]=new double[sylLength];
					}
					elementPos[countSyls]=new int[sylLength];
					sylLength=0;
					Element ele=(Element)songs[lookUpEls[countEls][0]].eleList.get(lookUpEls[countEls][1]);
					double startPos=ele.begintime*ele.timeStep;
					for (int g=0; g<p[k].length; g++){
						if (p[k][g]>=0){
							Element ele2=(Element)songs[lookUpEls[countEls][0]].eleList.get(lookUpEls[countEls][1]);
							
							double adjust=(ele2.timeStep*ele2.begintime)-startPos;
							for (int b=0; b<data[countEls][0].length; b++){
								for (int a=0; a<dims; a++){
									dataSyls[countSyls][a][sylLength]=data[countEls][a][b];
									if ((a<numParams)&&(paramType[a]==0)){
										dataSyls[countSyls][a][sylLength]+=adjust;
										//System.out.println(g+" "+dataSyls[countSyls][a][sylLength]);
									}
									//System.out.print(dataSyls[countSyls][a][sylLength]+" ");
								}
								elementPos[countSyls][sylLength]=g;
								//dataSyls[countSyls][dims-1][sylLength]=1;
								//System.out.println(countSyls+" "+sylLength);
								sylLength++;
							}
							countEls++;
						}
					}
					/*for (int a=0; a<numParams; a++){
						if (paramType[a]==16){
							int ds=dataSyls[countSyls][a].length-1;
							double lastLength=dataSyls[countSyls][a][ds];
							for (int b=0; b<dataSyls[countSyls][a].length; b++){
								dataSyls[countSyls][a][b]=lastLength;
							}
						}
					}*/
					countSyls++;
				}
			}
		}
		return dataSyls;
	}
			
	
	public void prepareToNormalize(double[][][] data){
	
		
		double totWeight=0;
		for (int i=0; i<numParams; i++){
			totWeight+=validParameters[i];
		}

		average=new double[numParams];
		double count=0;
		
		double withinSum[]=new double[numParams];
		double betweenSum[]=new double[numParams];
		
		for (int i=0; i<data.length; i++){
			
			
			
			int le=data[i][0].length;
			//System.out.println(i+" "+le);
			withinSum=new double[numParams];
			
			for (int j=0; j<le; j++){
				
				for (int k=0; k<numParams; k++){
					
					//System.out.print(data[i][k][j]+" ");
					
					average[k]+=data[i][k][j];
					withinSum[k]+=data[i][k][j];
				}
				//System.out.println();
			}
			//System.out.println();
			
			for (int j=0; j<numParams; j++){
				withinSum[j]/=(le+0.0);
				betweenSum[j]+=withinSum[j];
			}
			
			
			//for (int j=0; j<le; j++){
			//	data[i][2][j]=data[i][1][j]-withinSum[1];
			//	average[2]=0;
			//	betweenSum[2]=0;
			//	withinSum[2]=0;
			//}
			
			count+=le;
		}

		for (int i=0; i<numParams; i++){
			average[i]/=count;
			betweenSum[i]/=data.length+0.0;
		}
		
		sd=new double[15];
		sdReal=new double[15];
		sdBetween=new double[15];
		double w;
		
		for (int i=0; i<data.length; i++){
			
			withinSum=new double[numParams];
		
			for (int j=0; j<data[i][0].length; j++){
				for (int k=0; k<numParams; k++){
					w=data[i][k][j]-average[k];
					sd[k]+=w*w;
					withinSum[k]+=data[i][k][j];
				}
			}
			
			for (int j=0; j<numParams; j++){
				withinSum[j]/=data[i][0].length+0.0;
				w=withinSum[j]-betweenSum[j];
				sdBetween[j]+=w*w;
			}
		}
		System.out.println("SongGroup: prepareToNormalize: parameter summaries - parameter number, sdreal, sd, sdbet, datalength");
		for (int i=0; i<numParams; i++){
			sdReal[i]=Math.sqrt(sd[i]/(count-1.0));
			sd[i]=validParameters[i]/(totWeight*sdReal[i]);
			sdBetween[i]=Math.sqrt(sdBetween[i]/(data.length-1.0));
			System.out.println(i+" "+average[i]+" "+sdReal[i]+" "+sd[i]+" "+sdBetween[i]+" "+data.length);
		}	
		System.out.println("Sample size: "+data.length);
	}
	
	public void prepareToNormalizePerElement(double[][][] data){
	
		double totWeight=0;
		for (int i=0; i<numParams; i++){
			totWeight+=validParameters[i];
		}

		double w;
		double sd2[]=new double[numParams];
		
		avind=new double[data.length][numParams];
		sdind=new double[data.length][numParams];
		sdRealInd=new double[data.length][numParams];
		
		for (int i=0; i<data.length; i++){
			int le=data[i][0].length;
			double tot=0;
			for (int j=0; j<le; j++){
				for (int k=0; k<numParams; k++){
					if (weightByAmp){
						avind[i][k]+=data[i][k][j]*data[i][numParams][j];
					}
					else{
						avind[i][k]+=data[i][k][j];
					}
				}
				if (weightByAmp){
					tot+=data[i][numParams][j];
				}
			}
			
			for (int j=0; j<numParams; j++){
				if (weightByAmp){
					avind[i][j]/=tot;
				}
				else{
					avind[i][j]/=le+0.0;
				}
			}
			
			for (int j=0; j<le; j++){
				for (int k=0; k<numParams; k++){
					w=data[i][k][j]-avind[i][k];
					if (weightByAmp){
						w*=data[i][numParams][j];
					}
					sdind[i][k]+=w*w;
				}
			}
			for (int j=0; j<numParams; j++){
				if (weightByAmp){
					sdind[i][j]=Math.sqrt(sdind[i][j]/(tot-1.0));
				}
				else{
					sdind[i][j]=Math.sqrt(sdind[i][j]/(le-1.0));
				}
				sdRealInd[i][j]=sdind[i][j];
				if (sdind[i][j]<sdReal[j]*minVar){
					sdind[i][j]=sdReal[j]*minVar;
				}
				sdind[i][j]=validParameters[j]/(totWeight*sdind[i][j]);
				sd2[j]+=sdind[i][j];
			}
		}
		for (int i=0; i<numParams; i++){
			sd2[i]/=data.length*1.0;
		}
		
	}
	
	public double[][][] normalize(double[][][] data){
	
		for (int i=0; i<data.length; i++){
			int le=data[i][0].length;
			for (int k=0; k<le; k++){
				//data[i][2][k]=(data[i][1][k]-avind[i][1])*sdind[i][1];
				//data[i][2][k]=(data[i][1][k]-avind[i][1]);
				//for (int j=0; j<numParams; j++){
				//	if (normalizePerElement){
						//data[i][j][k]=(data[i][j][k]-avind[i][j])*sdind[i][j];
				//		data[i][j][k]=data[i][j][k]-avind[i][j];
					//}
				//	else{
						//data[i][j][k]=(data[i][j][k]-average[j])*sd[j];
				//		data[i][j][k]=data[i][j][k]-average[j];
					
				//	}
				//}
			}
		}
		
		return data;
	
	}
	
		
	public double[][][] getSyllableData (Song[] songs, double[][][] d){
		
		int syllSize=0;
		for (int i=0; i<songs.length; i++){
			for (int j=0; j<songs[i].phrases.size(); j++){
				int[][]p=(int[][])songs[i].phrases.get(j);
				syllSize+=p.length;
			}
		}		

		double[][][] data=new double[syllSize][][];


		int loc=0;
		int p=0;
		int a,x,y;
		int numParams=d[0][0].length;
		
		for (int i=0; i<songs.length; i++){
			int phraseSize=songs[i].phrases.size();
			for (int j=0; j<phraseSize; j++){
			
				int[][]q=(int[][])songs[i].phrases.get(j);
				for (int k=0; k<q.length; k++){
				
					
					x=0;
					y=0;
					for (int kk=0; kk<q[k].length; kk++){
						if (q[k][kk]!=-1){
							a=q[k][kk]+loc;
							x+=d[a].length;
							y=kk;
						}
					}
					data[p]=new double[x][numParams];
					
					x=0;
					int starttime=0;
					double addOn=0;
					Element ele=(Element)songs[i].eleList.get(q[k][y]);
					double time=ele.begintime+ele.length;
					
					
					for (int kk=0; kk<q[k].length; kk++){
						if (q[k][kk]!=-1){
							a=q[k][kk]+loc;
							ele=(Element)songs[i].eleList.get(q[k][kk]);
							if (x==0){
								starttime=ele.begintime;
								time-=starttime;
							}
							addOn=(ele.begintime-starttime)*ele.timeStep*sd[0];
							for (int ij=0; ij<d[a].length; ij++){
								for (int jj=0; jj<numParams; jj++){
									data[p][x][jj]=d[a][ij][jj];
									if ((parameterValues[0]>0)&&(jj==0)){
										data[p][x][jj]+=addOn;
									}
									if ((parameterValues[1]>0)&&(jj==1)){
										data[p][x][jj]=(ele.compressedPoints[ij]+ele.begintime-starttime)/time;
									}
								}
								x++;
							}
						}
					}
					p++;
				}
				
			}
			loc+=songs[i].eleList.size();
			
		}
		return data;
	}

	
	

	public synchronized float[][] runDTW(DTWSwingWorker dtws, boolean stitch){
		
		int ncores=Runtime.getRuntime().availableProcessors();
		
		int eleSize=data.length;
		
		int[][] elpos=null;
		if(stitch){
			elpos=elementPos;
			System.out.println("STITCHING ENABLED");
		}
		
		float[][] scores=new float[eleSize][eleSize];
		
		for (int i=0; i<sdReal.length; i++){
			System.out.println(i+" "+sdReal[i]);
		}
		double[] sdOver=new double[15];
		
		//sdOver[0]=59.00612164;
		//sdOver[1]=0.344577721;
		//sdOver[2]=0.120634842;
		//sdOver[3]=2.487608393;
		
		//sw sp over
		//sdOver[0]=46.60316384;
		//sdOver[1]=0.261371111;
		//sdOver[2]=0.14667803;
		//sdOver[3]=0.716237353;
		
		//chaffinch over
		sdOver[0]=69.1648014;
		sdOver[1]=0.363102481;
		sdOver[2]=0.113096018;
		sdOver[3]=2.960793968;

		float[][] scoresX=new float[ncores][eleSize];
		
		CompareThread4 ct[]=new CompareThread4[ncores];
		
		int maxlength=0;
		for (int lb=0; lb<eleSize; lb++){
			if (data[lb][0].length>maxlength){maxlength=data[lb][0].length;}
		}
		
		int[] starts=new int[ncores];
		int[] stops=new int[ncores];
		for (int i=0; i<ncores; i++){
			starts[i]=i*(eleSize/ncores);
			stops[i]=(i+1)*(eleSize/ncores);
			System.out.println(starts[i]+" "+stops[i]+" "+eleSize);
		}
		stops[ncores-1]=eleSize;
		
		
		for (int k=0; k<eleSize; k++){
			
			if (dtws.isCancelled()){
				break;
			}
			int prog=(int)Math.round(100*(k+1)/eleSize);
			dtws.progress(prog);
			
			
			
			for (int cores=0; cores<ncores; cores++){
				ct[cores]=new CompareThread4(maxlength, data, elpos, sdReal, sdRatio, validParameters, weightByAmp, scoresX[cores], starts[cores], stops[cores], k);
				//ct[cores]=new CompareThread4(maxlength, data, elpos, sdOver, sdRatio, validParameters, weightByAmp, scoresX[cores], starts[cores], stops[cores], k);
				ct[cores].setPriority(Thread.MIN_PRIORITY);
				ct[cores].start();
			}
			
			try{
				for (int cores=0; cores<ncores; cores++){
					ct[cores].join();
					System.arraycopy(scoresX[cores], starts[cores], scores[k], starts[cores], stops[cores]-starts[cores]);
				}
				//ct2.join();
			}
			catch (Exception e){
				e.printStackTrace();
			}
			
			
			//System.arraycopy(scores1, 0, scores[i], 0, j);
			//System.arraycopy(scores2, j, scores[i], j, (i-j));
			//if (dp!=null){dp.updateProgressLabel();}
		}
		
		float[][] scoresH=new float[eleSize][];
		for (int i=0; i<eleSize; i++){
			scoresH[i]=new float[i+1];
			for (int j=0; j<i; j++){
				//scoresH[i][j]=Math.min(scores[i][j], scores[j][i]);
				scoresH[i][j]=Math.max(scores[i][j], scores[j][i]);
				//scoresH[i][j]=0.5f*(scores[i][j]+scores[j][i]);
				if ((Float.isNaN(scores[i][j]))||(Float.isNaN(scores[j][i]))){
					System.out.println("NaN TROUBLE:"+" "+scores[i][j]+" "+scores[j][i]);
					System.out.println(songs[lookUpEls[i][0]].individualName+" "+songs[lookUpEls[i][0]].name+" "+lookUpEls[i][1]);
					System.out.println(songs[lookUpEls[j][0]].individualName+" "+songs[lookUpEls[j][0]].name+" "+lookUpEls[j][1]);
				}
				
			}
		}
		
		
		return scoresH;	
	}
	/*
	public float[][] compareUnthreaded(){
		
		int eleSize=data.length;
		float[][] scores=new float[eleSize][];
		
		for (int i=0; i<eleSize; i++){
			scores[i]=new float[i+1];
		}

		float[] scores1=new float[eleSize];	
		CompareThread4 ct;
		
		int maxlength=0;
		int d=data.length;
		for (int lb=0; lb<d; lb++){
			if (data[lb][0].length>maxlength){maxlength=data[lb][0].length;}
		}

		for (int k=1; k<eleSize; k++){
			ct=new CompareThread4(maxlength, data, weightByAmp, scores1);
			ct.setup(0,k,k);
			
			ct.startAnalysis();

			System.arraycopy(scores1, 0, scores[k], 0, k);
		}
		return scores;	
	}
	*/
	
	
	public float[][] normalizeScores(float[][] score){

		double sd=getMatrixSD(score);
		for (int i=0; i<score.length; i++){
			for (int j=0; j<i; j++){
				score[i][j]=(float)(score[i][j]/sd);
			}
		}
		
		return score;
	}
	
	public double getMatrixAv(float[][] score){
		double a=0;
		double b=0;
		for (int i=0; i<score.length; i++){
			for (int j=0; j<i; j++){
				a+=score[i][j];
				b++;
			}
		}
		double av=a/b;
		return av;
	}
	
	public double getMatrixSD(float[][] score){
		
		double a=0;
		double b=0;
		double c=0;
		double av=getMatrixAv(score);
		for (int i=0; i<score.length; i++){
			for (int j=0; j<i; j++){
				c=score[i][j]-av;
				a+=c*c;
				a++;
			}
		}
		double sd=Math.sqrt(a/(b-1));
		
		return sd;
	}
		
	public float[][] transformScores(float[][] scores){
		System.out.println("Song group: transform scores. P18="+parameterValues[18]+" P17="+parameterValues[17]);
		if ((parameterValues[18]<parameterValues[17])&&(parameterValues[18]>=0)&&(parameterValues[17]<=100)){
			scores=gateScores(scores, parameterValues[18], parameterValues[17]);
		}
		return(scores);
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
	
	public float[][] addOverallDifferencesToScore(float[][] score){
	
		double totWeight=0;
		for (int i=0; i<numParams; i++){
			totWeight+=validParameters[i];
		}
	
		double[] averageDiffs=new double[numParams];
		double[] averageSDs=new double[numParams];
		double averageScore=0;
		double[] sdDiffs=new double[numParams];
		double[] sdSds=new double[numParams];
		double sdScore=0;
		
		boolean[]logParam=new boolean[numParams];
		logParam[0]=true;
	
		double count=0;		
		int eleNumber=score.length;
		
		for (int i=0; i<eleNumber; i++){
			for (int j=0; j<i; j++){

				averageScore+=score[i][j];
				for (int k=0; k<numParams; k++){
					if (logParam[k]){
						averageDiffs[k]+=Math.abs(Math.log(avind[i][k])-Math.log(avind[j][k]));
						averageSDs[k]+=Math.abs(Math.log(sdRealInd[i][k])-Math.log(sdRealInd[j][k]));
					}
					else{
						averageDiffs[k]+=Math.abs(avind[i][k]-avind[j][k]);
						averageSDs[k]+=Math.abs(sdRealInd[i][k]-sdRealInd[j][k]);
					}
				}
				count++;
			}
		}
		
		averageScore/=count;
		for (int i=0; i<numParams; i++){
			averageDiffs[i]/=count;
			averageSDs[i]/=count;
		}
		
		double w;
		for (int i=0; i<eleNumber; i++){
			for (int j=0; j<i; j++){				
				w=score[i][j]-averageScore;
				sdScore+=w*w;
				for (int k=0; k<numParams; k++){
					if (logParam[k]){
						w=averageDiffs[k]-Math.abs(Math.log(avind[i][k])-Math.log(avind[j][k]));
						sdDiffs[k]+=w*w;
						w=averageSDs[k]-Math.abs(Math.log(sdRealInd[i][k])-Math.log(sdRealInd[j][k]));
						sdSds[k]+=w*w;
					}
					else{
						w=averageDiffs[k]-Math.abs(avind[i][k]-avind[j][k]);
						sdDiffs[k]+=w*w;
						w=averageSDs[k]-Math.abs(sdRealInd[i][k]-sdRealInd[j][k]);
						sdSds[k]+=w*w;
					}
				}
			}
		}
		
		count--;
		sdScore=Math.sqrt(count/sdScore);
		for (int i=0; i<numParams; i++){
			sdDiffs[i]=(validParameters[i]/totWeight)*Math.sqrt(count/sdDiffs[i]);
			if (sdSds[i]>0){
				sdSds[i]=(validParameters[i]/totWeight)*Math.sqrt(count/sdSds[i]);
			}
		}
		
		for (int i=0; i<eleNumber; i++){
			for (int j=0; j<i; j++){
				score[i][j]*=sdScore*0.5;				
				for (int k=0; k<numParams; k++){
					if (logParam[k]){
						score[i][j]+=Math.abs(Math.log(avind[i][k])-Math.log(avind[j][k]))*sdDiffs[k];
					}
					else{
						score[i][j]+=Math.abs(avind[i][k]-avind[j][k])*sdDiffs[k];
					}
				}
			}
		}
		
		return score;
		
	}

	
	public double[][][] getGapDistances(double[][][] data){
	
		int length=data.length;
		double[][][] intergaps=new double[length][][];
		
		for (int i=0; i<length; i++){
			int length2=data[i].length-1;
			int dims=data[i][0].length;
			if (weightByAmp){dims--;}
			intergaps[i]=new double[length2][2];
			
			for (int j=0; j<length2; j++){
				int jj=j+1;
				for (int k=0; k<dims; k++){
					intergaps[i][j][0]+=(data[i][j][k]-data[i][jj][k])*(data[i][j][k]-data[i][jj][k]);
				}
				intergaps[i][j][1]=1/(2*Math.sqrt(intergaps[i][j][0]));
			}
		}
		return intergaps;
	}
	
	public void calculateSyllablePriorsPosts(){
		priors=new int[syllNumber];
		posts=new int[syllNumber];
		
		for (int i=0; i<syllNumber; i++){
			int p=lookUpSyls[i][0];
			if ((i==0)||(lookUpSyls[i-1][0]!=p)){
				priors[i]=-1;
			}
			else{
				priors[i]=i-1;
			}
			if ((i==syllNumber-1)||(lookUpSyls[i+1][0]!=p)){
				posts[i]=-1;
			}
			else{
				posts[i]=i+1;
			}
		}
	}
	
	public float[][] calculateSyllableSyntaxDistances(){
		float endMisMatchScore=3;
		
		float[][] results=new float[syllNumber][];
		
		for (int i=0; i<syllNumber; i++){
			results[i]=new float[i+1];
		}
		
		float priorScore=0;
		float postScore=0;
		for (int i=0; i<syllNumber; i++){
			for (int j=0; j<i; j++){
				if ((priors[i]>=0)&&(priors[j]>=0)){
					priorScore=scoresSyll[priors[i]][priors[j]];
				}
				else if ((priors[i]<0)&&(priors[j]<0)){
					priorScore=-1;
				}
				else{
					priorScore=-1;
				}
				if ((posts[i]>=0)&&(posts[j]>=0)){
					postScore=scoresSyll[posts[i]][posts[j]];
				}
				else if ((posts[i]<0)&&(posts[j]<0)){
					postScore=-1;
				}
				else{
					postScore=-1;
				}
				results[i][j]=(priorScore+postScore)*0.5f;
				if (priorScore<0){
					results[i][j]=postScore;
				}
				if (postScore<0){
					results[i][j]=priorScore;
				}
				if ((priorScore<0)&&(postScore<0)){
					results[i][j]=endMisMatchScore;
				}
			}
		}
		
		return results;
	}
				
		
	public void syntactAdjustSyllScores(){
	
		int repeats=50;
		float incrementFraction=1f;
		calculateSyllablePriorsPosts();
		
		for (int i=0; i<repeats; i++){
		
			float[][] results=calculateSyllableSyntaxDistances();
			
			for (int j=0; j<syllNumber; j++){
				for (int k=0; k<j; k++){
					scoresSyll[j][k]+=results[j][k]*incrementFraction;
				}
			}
			
			scoresSyll=normalizeScores(scoresSyll);
		}
	}
	
	public void calculateSyllableDistanceDistributions3(){
		System.out.println("Setting syll labels 4 - distribution");
		
		int posDim=10;
		int distDim=20;
		int[][] results=new int[distDim][posDim];		//first dimension is partition of distance scale; second dimension is distance in terms of position
		int[] results2=new int[distDim];
		double tot=0;
		double av=0;
		for (int i=0; i<syllNumber; i++){
			for (int j=0; j<i; j++){
				av+=scoresSyll[i][j];
				tot++;
			}
		}
		av/=tot;
		double threshold=av-1.5;
		double threshold2=av-1.5;
		int repeats=10000;
		double[] distNext=new double[syllNumber];
				
		for (int i=0; i<syllNumber; i++){
			for (int j=0; j<i; j++){
				int a=(int)Math.round((scoresSyll[i][j]-av)*0.25*distDim);
				a+=distDim/2;
				if (a>=distDim){a=distDim-1;}
				if (a<0){a=0;}
				results2[a]++;
			}
		}
	
		for (int i=0; i<syllNumber-1; i++){
			int firstLoc=lookUpSyls[i][0];
			
			int j=i+1;
			while ((j<syllNumber)&&(lookUpSyls[j][0]==firstLoc)){
				
				int k=j-i;
				if (k>=posDim){k=posDim-1;}
				
				int a=(int)Math.round((scoresSyll[j][i]-av)*0.25*distDim);
				a+=distDim/2;
				if (a>=distDim){a=distDim-1;}
				if (a<0){a=0;}
				results[a][k]++;
				j++;
			}
		}
		
		for (int i=0; i<syllNumber; i++){
			int firstLoc=lookUpSyls[i][0];
			int j=i+1;
			//int jj=j+1;
			if ((j<syllNumber)&&(lookUpSyls[j][0]==firstLoc)){
				//distNext[i]=(scoresSyll[j][i]-av)*0.25;
				//distNext[i]+=0.5;
				//distNext[i]=Math.atan2(scoresSyll[j][i], scoresSyll[jj][i])/(0.5*Math.PI);
				if (scoresSyll[j][i]<threshold2){
					distNext[i]=1;
				}
				else{
					distNext[i]=0;
				}
				
				if (distNext[i]<0){distNext[i]=0;}
				if (distNext[i]>1){distNext[i]=1;}
			}
			else{
				distNext[i]=0.5;
			}
		}
		
		for (int i=0; i<syllNumber; i++){
			syllLabels[i]=0;
			double counter=0;
			for (int j=0; j<syllNumber; j++){
				int ii=i; 
				int jj=j;
				if (j>i){
					ii=j;
					jj=i;
				}
				if ((scoresSyll[ii][jj]<=threshold)&&(distNext[j]!=1.5)){
					counter++;
					syllLabels[i]+=distNext[j];
				}
			}
			syllLabels[i]/=counter;
		}
		
		int[] order=new int[syllNumber];
		int prop=(int)Math.floor(repeats*0.05);
		double[][]maxScores=new double[syllNumber][prop];
		double[][]minScores=new double[syllNumber][prop];
		
		for (int i=0; i<syllNumber; i++){
			for (int j=0; j<prop; j++){
				minScores[i][j]=1000;
			}
		}
		
		int stupidcounter=0;
		for (int i=0; i<repeats; i++){
			if (stupidcounter==1000){
				stupidcounter=0;
			}
			stupidcounter++;
			reorder(order);
			for (int j=0; j<syllNumber; j++){
				int firstLoc=lookUpSyls[j][0];
				int k=j+1;
				int ii=order[j];
				if ((k<syllNumber)&&(lookUpSyls[k][0]==firstLoc)){
					
					int kk=order[k];
					int jj=order[j];
					if (kk<jj){
						kk=order[j];
						jj=order[k];
					}
					if (scoresSyll[kk][jj]<threshold2){
						distNext[ii]=1;
					}
					else{
						distNext[ii]=0;
					}
				}
				else{
					distNext[ii]=0.5;
				}
			}
			for (int j=0; j<syllNumber; j++){
				double score=0;
				double counter=0;
				for (int k=0; k<syllNumber; k++){
					int ii=j; 
					int jj=k;
					if (k>j){
						ii=k;
						jj=j;
					}
					if ((scoresSyll[ii][jj]<=threshold)&&(distNext[j]!=0.5)){
						counter++;
						score+=distNext[k];
					}
				}
				score/=counter;
				
				int loc=0;
				double minmax=maxScores[j][0];
				for (int k=1; k<prop; k++){
					if (maxScores[j][k]<minmax){
						minmax=maxScores[j][k];
						loc=k;
					}
				}				
				if (score>minmax){
					maxScores[j][loc]=score;
				}
				loc=0;
				double maxmin=minScores[j][0];
				for (int k=1; k<prop; k++){
					if (minScores[j][k]>maxmin){
						maxmin=minScores[j][k];
						loc=k;
					}
				}	
				if (score<maxmin){
					minScores[j][loc]=score;
				}
				
				
			}	
		}
		
		double pthresh[]=new double[syllNumber];
		int[] doneAlready=new int[syllNumber];
		for (int i=0; i<syllNumber; i++){
			double p=1/(syllNumber-i-0.0);
			pthresh[i]=1-Math.pow(0.95, p);
			doneAlready[i]=1;
		}

		for (int i=0; i<syllNumber; i++){
			Arrays.sort(maxScores[i]);
			Arrays.sort(minScores[i]);
		}
		
		int countthresh=0;
		for (int j=0; j<syllNumber; j++){
			
			double jmin=1000;
			int loc=0;
			for (int k=0; k<syllNumber; k++){
				if ((doneAlready[k]==1)&&(syllLabels[k]<jmin)){
					jmin=syllLabels[k];
					loc=k;
				}
			}
			doneAlready[loc]=0;
			int ploc=(int)Math.floor(pthresh[countthresh]*repeats);
			
			if ((ploc>=0)&&(ploc<prop)&&(syllLabels[loc]<minScores[j][ploc])){
				countthresh++;
				syllLabels[loc]=0;
			}
			else{
				syllLabels[loc]=0.5;
				j=syllNumber;
			}
		}
		
		countthresh=0;
		for (int j=0; j<syllNumber; j++){
			double jmax=0;
			int loc=0;
			for (int k=0; k<syllNumber; k++){
				if ((doneAlready[k]==1)&&(syllLabels[k]>jmax)){
					jmax=syllLabels[k];
					loc=k;
				}
			}
			doneAlready[loc]=0;
			int ploc=(int)Math.floor(pthresh[countthresh]*repeats);
			ploc=prop-ploc-1;
			if ((ploc>=0)&&(ploc<prop)&&(syllLabels[loc]>maxScores[j][ploc])){
				countthresh++;
				syllLabels[loc]=1;
			}
			else{
				syllLabels[loc]=0.5;
				j=syllNumber;
			}
		}
		
		for (int j=0; j<syllNumber; j++){
			if (doneAlready[j]==1){
				syllLabels[j]=0.5;
			}
		}
	}
	
	
	public void calculateSyllableDistanceDistributions2(){
		System.out.println("Setting syll labels 5 - distribution");
		double avScore=0;
		double countScore=0;
		for (int i=0; i<syllNumber; i++){
			for (int j=0; j<i; j++){
				avScore+=scoresSyll[i][j];
				countScore++;
			}
		}
		avScore/=countScore;
		
		
		double distanceThreshold1=avScore-1.5;
		double distanceThreshold2=avScore-1.5;
		
		boolean[] followers=new boolean[syllNumber];
		
		for (int i=0; i<syllNumber; i++){
			if ((i<syllNumber-1)&&(lookUpSyls[i][0]==lookUpSyls[i+1][0])){
				followers[i]=true;
			}
			else{
				followers[i]=false;
			}
		}		
		
		int[][] members=new int [syllNumber][];
		
		for (int i=0; i<syllNumber; i++){
			int count=0;
			for (int j=0; j<syllNumber-1; j++){
				int ii=i;
				int jj=j;
				if (j>i){
					ii=j;
					jj=i;
				}
				if ((scoresSyll[ii][jj]<distanceThreshold1)&&(lookUpSyls[j][0]==lookUpSyls[j+1][0])){
					count++;
				}
			}
			members[i]=new int[count];
			count=0;
			for (int j=0; j<syllNumber-1; j++){
				int ii=i;
				int jj=j;
				if (j>i){
					ii=j;
					jj=i;
				}
				if ((scoresSyll[ii][jj]<distanceThreshold1)&&(lookUpSyls[j][0]==lookUpSyls[j+1][0])){
					members[i][count]=j;
					count++;
				}
			}
		}
		int[] realCount=new int[syllNumber];
		
		for (int i=0; i<syllNumber; i++){
			for (int j=0; j<members[i].length; j++){
				int k=members[i][j];
				if (scoresSyll[k+1][k]<distanceThreshold2){realCount[i]++;}
			}
		}
		
		int repeats=10000;
		
		int prop=(int)Math.round(repeats*0.025);
		
		int[][] maxScores=new int[syllNumber][prop];
		int[][] minScores=new int[syllNumber][prop];
		
		for (int i=0; i<syllNumber; i++){
			for (int j=0; j<prop; j++){
				minScores[i][j]=1000000;
			}
		}
		
		int minmaxlocs[]=new int[syllNumber];
		int maxminlocs[]=new int[syllNumber];
		
		
		int[] order=new int[syllNumber];
		int[] lookUpOrder=new int[syllNumber];
		
				
		for (int i=0; i<repeats; i++){
		
			reorder(order, lookUpOrder);			
			for (int j=0; j<syllNumber; j++){
				int simCount=0;
				for (int k=0; k<members[j].length; k++){
					int a=lookUpOrder[members[j][k]];
					if (followers[a]){
						int b=order[a];
						int c=order[a+1];
						if (c>b){
							int d=b;
							b=c;
							c=d;
						}
						if (scoresSyll[b][c]<distanceThreshold2){simCount++;}
					}
				}
				
				
				if (simCount>maxScores[j][minmaxlocs[j]]){
					maxScores[j][minmaxlocs[j]]=simCount;
					int minmax=1000000;
					int loc=0;
					for (int x=0; x<prop; x++){
						if (maxScores[j][x]<minmax){
							minmax=maxScores[j][x];
							loc=x;
						}
					}
					minmaxlocs[j]=loc;
				}
				if (simCount<=minScores[j][maxminlocs[j]]){
					minScores[j][maxminlocs[j]]=simCount;
					int maxmin=-1;
					int loc=0;
					for (int x=0; x<prop; x++){
						if (minScores[j][x]>maxmin){
							maxmin=minScores[j][x];
							loc=x;
						}
					}
					maxminlocs[j]=loc;
				}				
			}
		}
		
		
		int[] probsMax=new int[syllNumber];
		int[] probsMin=new int[syllNumber];
		
		for (int i=0; i<syllNumber; i++){
			Arrays.sort(maxScores[i]);
			Arrays.sort(minScores[i]);
			for (int j=0; j<prop; j++){
				if (maxScores[i][j]>=realCount[i]){
					probsMax[i]=prop-j;
					if (j==0){probsMax[i]=repeats/2;}
					j=prop;
				}
			}
			for (int j=prop-1; j>=0; j--){
				if (minScores[i][j]<=realCount[i]){
					probsMin[i]=j;
					if (j==prop-1){probsMin[i]=repeats/2;}
					j=-1;
				}
			}
			
			
			
			syllLabels[i]=0.5f;
			if (probsMin[i]<repeats/2){
				syllLabels[i]=(float)(probsMin[i]/(repeats+0.0));
			}
			else if (probsMax[i]<repeats/2){
				syllLabels[i]=(float)((repeats-probsMax[i])/(repeats+0.0));
			}
		}
		
	}
	
	
	
	public void calculateSyllableDistanceDistributions(){
		System.out.println("Setting syll labels 6 - distribution");
		int nearestNumber=40;
		
		boolean[] followers=new boolean[syllNumber];
		
		for (int i=0; i<syllNumber; i++){
			if ((i<syllNumber-1)&&(lookUpSyls[i][0]==lookUpSyls[i+1][0])){
				followers[i]=true;
			}
			else{
				followers[i]=false;
			}
		}		
		
		int[][] members=new int [syllNumber][nearestNumber];
		float[] scorep=new float[nearestNumber];
		for (int i=0; i<syllNumber; i++){
			int count=0;
			int place=0;
			while (count<nearestNumber){
				if (followers[place]){
					members[i][count]=place;
					int ii=i;
					int jj=place;
					if (jj>ii){
						ii=place;
						jj=i;
					}
					scorep[count]=scoresSyll[ii][jj];
					count++;
				}
				place++;
			}				
			for (int j=place; j<syllNumber-1; j++){
				if (followers[j]){
					int ii=i;
					int jj=j;
					if (j>i){
						ii=j;
						jj=i;
					}
					float score=scoresSyll[ii][jj];
				
					float max=0;
					int loc=0;
				
					for (int k=0; k<nearestNumber; k++){
						if (max<scorep[k]){
							max=scorep[k];
							loc=k;
						}
					}
					if (score<max){
						scorep[loc]=score;
						members[i][loc]=j;
					}
				}
			}
		}
		
		int[] countInd=new int[syllNumber];
		for (int i=0; i<syllNumber; i++){
			if (followers[i]){
				int ii=i+1;
				for (int j=0; j<nearestNumber; j++){
					if (members[i][j]==ii){
						countInd[i]=1;
						j=nearestNumber;
					}
				}
			}
		}
		
		int[] realCount=new int[syllNumber];		
		for (int i=0; i<syllNumber; i++){
			for (int j=0; j<nearestNumber; j++){
				realCount[i]+=countInd[members[i][j]];
			}
		}
		
		int repeats=10000;
		
		int prop=(int)Math.round(repeats*0.025);
		
		int[][] maxScores=new int[syllNumber][prop];
		int[][] minScores=new int[syllNumber][prop];
		
		for (int i=0; i<syllNumber; i++){
			for (int j=0; j<prop; j++){
				minScores[i][j]=1000000;
			}
		}
		
		int minmaxlocs[]=new int[syllNumber];
		int maxminlocs[]=new int[syllNumber];
		
		
		int[] order=new int[syllNumber];
		int[] lookUpOrder=new int[syllNumber];
		
				
		int stupidcounter=0;
		for (int i=0; i<repeats; i++){
			if (stupidcounter==1000){
				stupidcounter=0;
			}
			stupidcounter++;
		
			reorder(order, lookUpOrder);
						
			for (int j=0; j<syllNumber; j++){
				int simCount=0;
				for (int k=0; k<members[j].length; k++){
					int a1=members[j][k];
					int b1=lookUpOrder[a1];
					int b2=b1+1;
					if (b1==syllNumber-1){
						b2=0;
					}
					int a2=order[b2];
					
					for (int c=0; c<nearestNumber; c++){
						if (members[a1][c]==a2){
							simCount++;
							c=nearestNumber;
						}
					}
				}
				
				
				if (simCount>maxScores[j][minmaxlocs[j]]){
					maxScores[j][minmaxlocs[j]]=simCount;
					int minmax=1000000;
					int loc=0;
					for (int x=0; x<prop; x++){
						if (maxScores[j][x]<minmax){
							minmax=maxScores[j][x];
							loc=x;
						}
					}
					minmaxlocs[j]=loc;
				}
				if (simCount<=minScores[j][maxminlocs[j]]){
					minScores[j][maxminlocs[j]]=simCount;
					int maxmin=-1;
					int loc=0;
					for (int x=0; x<prop; x++){
						if (minScores[j][x]>maxmin){
							maxmin=minScores[j][x];
							loc=x;
						}
					}
					maxminlocs[j]=loc;
				}				
			}
		}
		
		
		int[] probsMax=new int[syllNumber];
		int[] probsMin=new int[syllNumber];
		
		for (int i=0; i<syllNumber; i++){
			Arrays.sort(maxScores[i]);
			Arrays.sort(minScores[i]);
				
			for (int j=0; j<prop; j++){
				if (maxScores[i][j]>=realCount[i]){
					probsMax[i]=prop-j;
					if (j==0){probsMax[i]=repeats/2;}
					j=prop;
				}
			}
			for (int j=prop-1; j>=0; j--){
				if (minScores[i][j]<=realCount[i]){
					probsMin[i]=j;
					if (j==prop-1){probsMin[i]=repeats/2;}
					j=-1;
				}
			}
			
			
			
			syllLabels[i]=0.5f;
			if (probsMin[i]<repeats/2){
				syllLabels[i]=(float)(probsMin[i]/(repeats+0.0));
			}
			else if (probsMax[i]<repeats/2){
				syllLabels[i]=(float)((repeats-probsMax[i])/(repeats+0.0));
			}
		}
		
	}	
	
	public void calculateSyllableDistanceDistributions4(){
		System.out.println("Setting syll labels 7 - distribution");
		int nearestNumber=20;
		if (nearestNumber>syllNumber/4){
			nearestNumber=syllNumber/4;
		}
		
		boolean[] followers=new boolean[syllNumber];
		
		for (int i=0; i<syllNumber; i++){
			if ((i<syllNumber-1)&&(lookUpSyls[i][0]==lookUpSyls[i+1][0])){
				followers[i]=true;
			}
			else{
				followers[i]=false;
			}
		}	
		
		int[][] rankedScores=new int[syllNumber][syllNumber];
		
		float[] scoreRank=new float[syllNumber];
		float[] compRank=new float[syllNumber];
		
		for (int i=0; i<syllNumber; i++){
		
		
			for (int j=0; j<syllNumber; j++){
				if (j<i){
					scoreRank[j]=scoresSyll[i][j];
				}
				else{
					scoreRank[j]=scoresSyll[j][i];
				}
				compRank[j]=scoreRank[j];
			}
			Arrays.sort(scoreRank);
			
			
			for (int j=0; j<syllNumber; j++){
				
				for (int k=0; k<syllNumber; k++){
					if (scoreRank[j]==compRank[k]){
						rankedScores[i][k]=j;
						//if (rankedScores[i][k]>200){rankedScores[i][k]=200;}
						k=syllNumber;
					}
				}
			}
		}
		
		int[][] members=new int [syllNumber][nearestNumber];
		float[] scorep=new float[nearestNumber];
		for (int i=0; i<syllNumber; i++){
			int count=0;
			int place=0;
			while (count<nearestNumber){
				if (followers[place]){
					members[i][count]=place;
					int ii=i;
					int jj=place;
					if (jj>ii){
						ii=place;
						jj=i;
					}
					scorep[count]=scoresSyll[ii][jj];
					count++;
				}
				place++;
			}	
			
			for (int j=place; j<syllNumber-1; j++){
				if (followers[j]){
					int ii=i;
					int jj=j;
					if (j>i){
						ii=j;
						jj=i;
					}
					float score=scoresSyll[ii][jj];
				
					float max=0;
					int loc=0;
				
					for (int k=0; k<nearestNumber; k++){
						if (max<scorep[k]){
							max=scorep[k];
							loc=k;
						}
					}
					if (score<max){
						scorep[loc]=score;
						members[i][loc]=j;
					}
				}
			}
		}
		
		int[] countInd=new int[syllNumber];
		int test=0;
		BasicStatistics bs=new BasicStatistics();
		
		for (int i=0; i<syllNumber; i++){
			if (followers[i]){
				int ii=i+1;
				countInd[i]=rankedScores[i][ii];
				
				test=lookUpSyls[i][0];
				
				double[] a1=new double[songs[lookUpSyls[i][0]].phrases.size()-1];
				
				int a2=0;
				ii++;
				while ((ii<syllNumber)&&(lookUpSyls[ii][0]==test)){
					a1[a2]=rankedScores[i][ii];
					a2++;
					ii++;
				}
				if (i>0){
					ii=i-1;
					while ((ii>=0)&&(lookUpSyls[ii][0]==test)){
						a1[a2]=rankedScores[i][ii];
						a2++;
						ii--;
					}
				}
				
				double p=bs.calculateMedian(a1);
				
				if (p>countInd[i]){
				}
				else{
				}
				
			}
		}
		
		int[] realCount=new int[syllNumber];		
		for (int i=0; i<syllNumber; i++){
			for (int j=0; j<nearestNumber; j++){
				realCount[i]+=countInd[members[i][j]];
			}
			//syllLabels[i]=(float)(realCount[i]/(nearestNumber*syllNumber+0.0f));
			//syllLabels[i]=(float)(countInd[i]/(syllNumber+0.0));
		}
		
		int repeats=10000;
		
		int prop=(int)Math.round(repeats*0.05);
		
		int[][] maxScores=new int[syllNumber][prop];
		int[][] minScores=new int[syllNumber][prop];
		
		for (int i=0; i<syllNumber; i++){
			for (int j=0; j<prop; j++){
				minScores[i][j]=1000000;
			}
		}
		
		int minmaxlocs[]=new int[syllNumber];
		int maxminlocs[]=new int[syllNumber];
		
		
		int[] order=new int[syllNumber];
		int[] lookUpOrder=new int[syllNumber];
		
				
		int stupidcounter=0;
		
		
		double[] overallNext=new double[repeats];
		
		for (int i=0; i<repeats; i++){
			if (stupidcounter==1000){
				stupidcounter=0;
			}
			stupidcounter++;
			reorder(order, lookUpOrder);
			double avNextSim=0;
			double countNextSim=0;
			
			for (int j=0; j<syllNumber; j++){
			
				if (followers[j]){
					int x1=order[j];
					int x2=order[j+1];
					
					avNextSim+=rankedScores[x1][x2];
					countNextSim++;
				}
			
			
				int simCount=0;
				for (int k=0; k<members[j].length; k++){
					int a1=members[j][k];
					int b1=lookUpOrder[a1];
					int b2=b1+1;
					if (b1==syllNumber-1){
						b2=0;
					}
					int a2=order[b2];
					simCount+=rankedScores[a1][a2];
					
				}
				
				
				if (simCount>maxScores[j][minmaxlocs[j]]){
					maxScores[j][minmaxlocs[j]]=simCount;
					int minmax=1000000;
					int loc=0;
					for (int x=0; x<prop; x++){
						if (maxScores[j][x]<minmax){
							minmax=maxScores[j][x];
							loc=x;
						}
					}
					minmaxlocs[j]=loc;
				}
				if (simCount<=minScores[j][maxminlocs[j]]){
					minScores[j][maxminlocs[j]]=simCount;
					int maxmin=-1;
					int loc=0;
					for (int x=0; x<prop; x++){
						if (minScores[j][x]>maxmin){
							maxmin=minScores[j][x];
							loc=x;
						}
					}
					maxminlocs[j]=loc;
				}				
			}
			overallNext[i]=avNextSim/countNextSim;
		}
		
		
		int[] probsMax=new int[syllNumber];
		int[] probsMin=new int[syllNumber];
		
		for (int i=0; i<syllNumber; i++){
			Arrays.sort(maxScores[i]);
			Arrays.sort(minScores[i]);
						
			for (int j=0; j<prop; j++){
				if (maxScores[i][j]>=realCount[i]){
					probsMax[i]=prop-j;
					if (j==0){probsMax[i]=repeats/2;}
					j=prop;
				}
			}
			for (int j=prop-1; j>=0; j--){
				if (minScores[i][j]<=realCount[i]){
					probsMin[i]=j;
					if (j==prop-1){probsMin[i]=repeats/2;}
					j=-1;
				}
			}
			
			
			
			syllLabels[i]=0.5f;
			if (probsMin[i]<repeats/2){
				syllLabels[i]=(float)(probsMin[i]/(repeats+0.0));
			}
			else if (probsMax[i]<repeats/2){
				syllLabels[i]=(float)((repeats-probsMax[i])/(repeats+0.0));
			}
						
		}		
		
		Arrays.sort(overallNext);
		/*int loc=repeats;
		for (int i=0; i<repeats; i++){
			if (overallNext[i]>averageNext){
				loc=i;
				i=repeats;
			}
		}*/
	}		
		
	public void reorder(int[] order){
		int n=order.length;
		int m=n;
		int c, b;
		for (int i=0; i<n; i++){
			order[i]=i;
		}
		
		for (int i=0; i<n; i++){
			c=random.nextInt(m);
			c+=i;
			b=order[i];
			order[i]=order[c];
			order[c]=b;
			m--;
		}
	}
	
	public void reorder(int[] order1, int[] order2){
		int n=order1.length;
		int m=n;
		int c, b;
		for (int i=0; i<n; i++){
			order1[i]=i;
			order2[i]=i;
		}
		
		for (int i=0; i<n; i++){
			c=random.nextInt(m);
			c+=i;
			b=order1[i];
			order1[i]=order1[c];
			order1[c]=b;
			m--;
			order2[order1[i]]=i;
		}
	}
	
	public double[][] reorderArray(double[][] arr){
		int n=arr[0].length;
		int m=arr.length;
		
		int[] placeholder=new int [n];
		for (int i=0; i<n; i++){
			placeholder[i]=i;
		}
		
		for (int i=0; i<n; i++){
			int p=random.nextInt(n-i);
			p+=i;
			placeholder[i]=p;
			placeholder[p]=i;
		}
		double[][] out=new double[m][n];
				
		for (int i=0; i<n; i++){
			int a=placeholder[i];
			
			for (int j=0; j<m; j++){
				out[j][i]=arr[j][a];
			}
		}
		
		return out;
		
	}
	
	public int[] reorderMatrix(float[][] inmatrix, float[][] outmatrix){
		int n=inmatrix.length;
		
		int[] placeholder=new int [n];
		for (int i=0; i<n; i++){
			placeholder[i]=i;
		}
		
		for (int i=0; i<n; i++){
			int p=random.nextInt(n-i);
			p+=i;
			int q=placeholder[i];
			placeholder[i]=placeholder[p];
			placeholder[p]=q;
		}
		
		for (int i=0; i<n; i++){
			int a=placeholder[i];
			for (int j=0; j<i; j++){
				int b=placeholder[j];
				if (a>b){
					outmatrix[i][j]=inmatrix[a][b];
				}
				else{
					outmatrix[i][j]=inmatrix[b][a];
				}
			}
		}
		return placeholder;
	}
	
	
	public void compressElement(){
		for (int i=0; i<songs.length; i++){
			for (int j=0; j<songs[i].eleList.size(); j++){
				Element ele=(Element)songs[i].eleList.get(j);
				ele.compressElements(3);
			}
		}
	}
	
	
	public void compressElements(){
		CompressComparisons cc=new CompressComparisons();
		scoresEleC=cc.compareElements(scoresEle, songs);
	}
	
	public void compressSyllables(){
		CompressComparisons cc=new CompressComparisons();
		scoresSyll=cc.compareSyllables(scoresEle, songs);
	}
	
	public void compressSyllables2(){
		CompressComparisons cc=new CompressComparisons();
		//scoresSyll2=cc.compareSyllables2(scoresSyll2, songs);
		//scoresSyll=cc.compareSyllables3(scoresEle, songs);
		//scoresSyll=cc.compareSyllables4(scoresEle, songs);
		scoresSyll=cc.phraseComp(scoresEle, songs, (float)alignmentCost);
		//double minDist=0;
		
		
		//for (int i=0; i<scoresSyll.length; i++){
		//	for (int j=0; j<i; j++){
		//		if (scoresSyll[i][j]>0){
		//			scoresSyll[i][j]=(float)Math.log(scoresSyll[i][j]);
		//			if (scoresSyll[i][j]<minDist){minDist=scoresSyll[i][j];}
		//		}
				//scoresSyll[i][j]=random.nextFloat();
				
				//if (scoresSyll2[i][j]<scoresSyll[i][j]){
				//	scoresSyll[i][j]=scoresSyll2[i][j];
				//}
		//	}
		//}
		//for (int i=0; i<scoresSyll.length; i++){
		//	for (int j=0; j<i; j++){
				//if (scoresSyll[i][j]>0){
				//	scoresSyll[i][j]+=minDist+0.00001;
				//}				
		//	}
		//}
		
		/*
		double[][] loc=new double[scoresSyll.length][8];
		for (int i=0; i<loc.length; i++){
			for (int j=0; j<8; j++){
				loc[i][j]=random.nextDouble();
			}
		}
		
		for (int i=0; i<scoresSyll.length; i++){
			for (int j=0; j<i; j++){
			
				double score=0;
				for (int k=0; k<8; k++){
					score+=(loc[i][k]-loc[j][k])*(loc[i][k]-loc[j][k]);
				}
				
				scoresSyll[i][j]=(float)Math.sqrt(score);
			}
		}
		*/
	}
	
	public void syntaxCompression(){
		scoreTrans=new float[syllNumber][];
		for (int i=0; i<syllNumber; i++){
			scoreTrans[i]=new float[i+1];
		}
		CompressComparisons cc=new CompressComparisons();
		cc.syntaxCompression(scoresSyll, scoreTrans, lookUpSyls);
	}
	
	public float[][] gateScores(float[][] scores, double hiPass, double loPass){
	
		int n=scores.length;
		int m=n*(n-1)/2;
	
		float[] h=new float[m];
		
		int c=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				h[c]=scores[i][j];
				c++;
			}
		}
		BasicStatistics bs=new BasicStatistics();
		
		float lowerLimit=bs.calculatePercentile(h, hiPass, false);
		float upperLimit=bs.calculatePercentile(h, loPass, false);

		h=null;
		upperLimit-=lowerLimit;
		float[][] scores2=new float[n][];
		for (int i=0; i<n; i++){
			scores2[i]=new float[i+1];
			for (int j=0; j<i; j++){
				scores2[i][j]=scores[i][j]-lowerLimit;
				if (scores2[i][j]<0){scores2[i][j]=0;}
				scores2[i][j]/=upperLimit;
				if (scores2[i][j]>1){scores2[i][j]=1f;}
			}
		}
		return scores2;
	}
	
	public float[][] logisticTransform(float[][] scores, boolean squareRoot, double centerParam, double mult){
		
		int n=scores.length;
		int m=n*(n-1)/2;
	
		float[] h=new float[m];
		
		int c=0;
		for (int i=0; i<n; i++){
			for (int j=0; j<i; j++){
				h[c]=scores[i][j];
				c++;
			}
		}
		BasicStatistics bs=new BasicStatistics();
		
		float limit=bs.calculatePercentile(h, centerParam, false);

		
		
		
		h=null;
		float[][] scores2=new float[n][];
		for (int i=0; i<n; i++){
			scores2[i]=new float[i+1];
			for (int j=0; j<i; j++){
				
				double x=-1*mult*(scores[i][j]-limit);
				if (squareRoot){
					x=-1*mult*(Math.sqrt(scores[i][j])-Math.sqrt(limit));
				}
				
				x=1/(1+Math.exp(x));
				
				scores2[i][j]=(float)(x);

			}
		}
		return scores2;
	}
	
	public void compressSyllables3(){
		//this is what happens when you get both stitched and non-stitched syllable comparisons
		//this method compares the two comparisons...
		
		//scoresSyll=normalizeScores(scoresSyll);
		
		CompressComparisons cc=new CompressComparisons();
		
		scoresSyll2=cc.compareSyllables5(scoresSyll2, songs, alignmentCost);
		
		//scoresSyll2=normalizeScores(scoresSyll2);
		
		/*
		int p[]=new int[syllNumber];
		int count=0;
		for (int i=0; i<songNumber; i++){
			for (int j=0; j<songs[i].phrases.size(); j++){
				int[][] q=(int[][])songs[i].phrases.get(j);
				p[count]=q[0].length;
				count++;
			}
		}
		*/
		
		for (int i=0; i<scoresSyll.length; i++){
			for (int j=0; j<i; j++){
				//if (p[i]!=p[j]){
				System.out.println(i+" "+j+" "+scoresSyll[i][j]+" "+scoresSyll2[i][j]);
					scoresSyll[i][j]=(float)Math.max(scoresSyll[i][j], scoresSyll2[i][j]+stitchPunishment);
					
					//scoresSyll[i][j]=scoresSyll2[i][j];
				//}
				
				
				//scoresSyll[i][j]=scoresSyll2[i][j];
				//if (scoresSyll[i][j]==scoresSyll2[i][j]*stitchPunishment){
				//	count1++;
				//}
				//else{
				//	count2++;
				//}
			}
		}
		
	}
	
	public void compressSyllableTransitions(){
		CompressComparisons cc=new CompressComparisons();
		
		scoreTrans=cc.compareSyllableTransitions2(scoresSyll, songs);
		
	}
	
	public void compressSongs(boolean useTrans, double lowerProp, double upperProp){
		try{
			System.out.println("Compressing songs");
			CompressComparisons cc=new CompressComparisons();
			//float[][]sy=logisticTransform(scoresSyll, true, 0.02, 10);
			//float[][]sy=gateScores(scoresSyll, 0.5, 20);
			if (!useTrans){
				scoresSong=cc.compareSongsDigram(scoresSyll, songs, useTrans);
			}
			else{
				scoresSong=cc.compareSongsDTW(scoresSyll, songs);	
			}
		}
		catch(Exception e){e.printStackTrace();}
	}
	
	public void compressSongsAsymm(){
		CompressComparisons cc=new CompressComparisons();
		scoresSong=cc.compareSongsAsymm(scoresSyll, songs, compScheme);
	}
	
	
	public void augmentSyllDistanceMatrixWithSyllableReps(){
		if (syllableRepetitions.length!=scoresSyll.length){
			System.out.println("AUGMENTATION FAILED: ERROR IN MATRIX SIZE");
		}
		else{
			double sd1=getMatrixAv(scoresSyll);
			//sd=syllableRepetitionWeighting*sd;
			float[][] tempMat=new float[syllableRepetitions.length][];
			for (int i=0; i<syllableRepetitions.length; i++){
				tempMat[i]=new float[i+1];
				for (int j=0; j<i; j++){
					double q=Math.log(syllableRepetitions[i])-Math.log(syllableRepetitions[j]);
					tempMat[i][j]=(float)Math.sqrt(q*q);
				}
			}
			double sd2=getMatrixAv(tempMat);
			//sd2=0.749591084;
			float weight=(float)(syllableRepetitionWeighting*sd1/sd2);
			System.out.println("WEIGHT: "+weight+" "+sd2);
			//weight=0.12161444f;
			//weight=0.05111025f;
			//weight=0.1022205f;
			for (int i=0; i<syllableRepetitions.length; i++){
				for (int j=0; j<i; j++){
					scoresSyll[i][j]+=weight*tempMat[i][j];
				}
			}
			
		}
	}
	
	
	public void syllLabelsDensity(){
		System.out.println("Setting syll labels 8 - density");
		double threshold=0.5;
		double maxDens=0;
		double minDens=1000000;
		double[] temp=new double[syllNumber];
		for (int i=0; i<syllNumber; i++){
			double a=0;
			for (int j=0; j<i; j++){
				temp[j]=scoresSyll[i][j];
				if (scoresSyll[i][j]<threshold){
					a++;
				}
			}
			temp[i]=0;
			for (int j=i+1; j<syllNumber; j++){
				temp[j]=scoresSyll[j][i];
				if (scoresSyll[j][i]<threshold){
					a++;
				}
			}
			Arrays.sort(temp);
			a=Math.log(temp[5]*1000000);
			
			syllLabels[i]=a;
			if (a>maxDens){
				maxDens=a;
			}
			if (a<minDens){
				minDens=a;
			}
		
		}
		
		for (int i=0; i<syllNumber; i++){
			syllLabels[i]=(maxDens-syllLabels[i])/(maxDens-minDens);
		}
	
	}
	
	
	
	public int getSharedSyllCount(int a, int b){
		int count=0;
		int counta=0;
		int countb=0;
		double mindiff=1000000000;
		for (int i=0; i<syllNumber; i++){
			//System.out.println(lookUpSyls[i][0]);
			if (lookUpSyls[i][0]==a){	
				counta++;
				int p=0;
				for (int j=0; j<i; j++){
					if (lookUpSyls[j][0]==b){
						countb++;
						if (scoresSyll[i][j]<parameterValues[17]){
							p++;
						}
						if (scoresSyll[i][j]<mindiff){
							mindiff=scoresSyll[i][j];
						}
					}
				}
				for (int j=i; j<syllNumber; j++){
					if (lookUpSyls[j][0]==b){
						countb++;
						if (scoresSyll[j][i]<parameterValues[17]){
							p++;
						}
						if (scoresSyll[j][i]<mindiff){
							mindiff=scoresSyll[j][i];
						}
					}
				}
				if (p>0){count++;}
				//count+=p;
			}
		}
		//System.out.println(syllNumber+" "+lookUpSyls.length+" "+scoresSyll.length);
		System.out.println("Syll sharing: "+a+" "+b+" "+counta+" "+countb+" "+count+" "+parameterValues[17]+" "+mindiff);
		return count;
	}
	
	public int getSharedTransCount(int a, int b){
		int count=0;
		for (int i=0; i<transNumber; i++){
			if (lookUpTrans[i][0]==a){				
				for (int j=0; j<i; j++){
					if (lookUpTrans[j][0]==b){
						if ((scoresSyll[lookUpTrans[i][2]][lookUpTrans[j][2]]<parameterValues[17])&&(scoresSyll[lookUpTrans[i][3]][lookUpTrans[j][3]]<parameterValues[17])){
							count++;
						}
					}
				}
				for (int j=i; j<transNumber; j++){
					if (lookUpTrans[j][0]==b){
						if ((scoresSyll[lookUpTrans[j][2]][lookUpTrans[i][2]]<parameterValues[17])&&(scoresSyll[lookUpTrans[j][3]][lookUpTrans[i][3]]<parameterValues[17])){
							count++;
						}
					}
				}
			}
		}
		System.out.println("Trans sharing: "+a+" "+b+" "+count);
		return count;
	}
	
	public double getWeightedSharedTransCount(int a, int b){
		double count=0;
		for (int i=0; i<transNumber; i++){
			//if ((lookUpTrans[i][0]==a)&&(lookUpTrans[i][1]>0)){		
			if (lookUpTrans[i][0]==a){	
				double c=0;
				for (int j=0; j<i; j++){
					if (lookUpTrans[j][0]==b){
						if ((scoresSyll[lookUpTrans[i][2]][lookUpTrans[j][2]]<parameterValues[17])&&(scoresSyll[lookUpTrans[i][3]][lookUpTrans[j][3]]<parameterValues[17])){
							c++;
						}
					}
				}
				for (int j=i; j<transNumber; j++){
					if (lookUpTrans[j][0]==b){
						if ((scoresSyll[lookUpTrans[j][2]][lookUpTrans[i][2]]<parameterValues[17])&&(scoresSyll[lookUpTrans[j][3]][lookUpTrans[i][3]]<parameterValues[17])){
							c++;
						}
					}
				}
				if (c>0){
					int x1=lookUpTrans[i][2];
					int x2=lookUpTrans[i][3];
					double d=0;
					
					for (int j=0; j<syllNumber; j++){
						if (lookUpSyls[j][0]==b){
							if ((j>x1)&&(scoresSyll[j][x1]<parameterValues[17])){
								d++;
							}
							if ((j<x1)&&(scoresSyll[x1][j]<parameterValues[17])){
								d++;
							}
							if ((j>x2)&&(scoresSyll[j][x2]<parameterValues[17])){
								d++;
							}
							if ((j<x2)&&(scoresSyll[x2][j]<parameterValues[17])){
								d++;
							}
						}
					}
					count+=2*c/d;
				}
			}
		}
		System.out.println("Weighted sharing: "+a+" "+b+" "+count);
		return count;
	}
	
	
	public void drawElements(int[] els){
		int[][] elsToDraw=new int[els.length][2];
		for (int i=0; i<els.length; i++){
			if (compressElements){
				elsToDraw[i][0]=lookUpElsC[els[i]][0];
				elsToDraw[i][1]=lookUpElsC[els[i]][1];				
			}
			else{
				elsToDraw[i][0]=lookUpEls[els[i]][0];
				elsToDraw[i][1]=lookUpEls[els[i]][1];
			}
		}
		ssb.drawElements(elsToDraw);
	}
	
	public void drawSyllables(int[] syls){
		int[][] sylsToDraw=new int[syls.length][2];
		for (int i=0; i<syls.length; i++){
			sylsToDraw[i][0]=lookUpSyls[syls[i]][0];
			sylsToDraw[i][1]=lookUpSyls[syls[i]][1];
		}
		ssb.drawSyllables(sylsToDraw);
		//ssb.drawPhraseEx(sylsToDraw);
	}
	
	public void drawTransitions(int[] trans){
		int[][] transToDraw=new int[trans.length][4];
		for (int i=0; i<trans.length; i++){
			transToDraw[i][0]=lookUpSyls[lookUpTrans[trans[i]][2]][0];
			transToDraw[i][1]=lookUpSyls[lookUpTrans[trans[i]][2]][1];
			transToDraw[i][2]=lookUpSyls[lookUpTrans[trans[i]][3]][0];
			transToDraw[i][3]=lookUpSyls[lookUpTrans[trans[i]][3]][1];

		}
		ssb.drawTransitions(transToDraw);
		//ssb.drawSyllables(transToDraw);
	}
	
	public void drawSongs(int[] songs){
		ssb.drawSongs(songs);
	}
	
	public int[] getId(int dataType, int i){
		int[] j=new int[2];
		if (dataType==0){
			j[0]=lookUpEls[i][0];
			j[1]=lookUpEls[i][1];
		}
		else if (dataType==1){
			j[0]=lookUpElsC[i][0];
			j[1]=lookUpElsC[i][1];
		}
		else if (dataType==2){
			j[0]=lookUpSyls[i][0];
			j[1]=lookUpSyls[i][1];
		}
		else if (dataType==3){
			j[0]=lookUpTrans[i][0];
			j[1]=lookUpTrans[i][1];
		}
		else{
			j[0]=i;
			j[1]=-1;
		}
		return (j);
	}
	
	public void draw(int dataType, int[]contents){
		if (dataType==0){
			drawElements(contents);
		}
		else if (dataType==1){
			drawElements(contents);
		}
		else if (dataType==2){
			drawSyllables(contents);
		}
		else if (dataType==3){
			drawTransitions(contents);
		}
		else{
			drawSongs(contents);
		}
	}
	
	
	public int[] getIDCounts(){
	
		String[] s={" :ny", " :nc", " :pa"};
		//String[] s={"Leid", "Lew", "FifeS", "FifeT"};
		//String[] s={"FifeS", "FifeT"};
		//String[] s={" ", "Biel", "Utah"};
		
		int[] results=new int[s.length];
	
	
		for (int i=0; i<syllNames.length; i++){
			boolean found=false;
			for (int j=0; j<s.length; j++){
				if (syllNames[i].startsWith(s[j])){
					results[j]++;
					found=true;
				}
			}
			if (!found){
				results[0]++;
			}
		}
		return results;
	}
	
	public int[] getIDLabels(int[] input){
		String[] s={" :ny", " :nc", " :pa"};
		//String[] s={"Leid", "Lew", "FifeS", "FifeT"};
		//String[] s={"FifeS", "FifeT"};
		//String[] s={" ", "Biel", "Utah"};
		
		int n=input.length;
		int[] results=new int[s.length];
	
	
		for (int i=0; i<n; i++){
			boolean found=false;
			for (int j=0; j<s.length; j++){
				if (syllNames[input[i]].startsWith(s[j])){
					results[j]++;
					j=s.length;
					found=true;
				}
			}
			if (!found){
				results[0]++;
			}
		}
		
		return results;
	}
	
	public float[][] splitMatrix(int[] label, int h, int ind){
		int c=0;
		for (int i=0; i<label.length; i++){
			if (label[i]==ind){c++;}
		}
		
		float[][] results=new float[c][];
		for (int i=0; i<c; i++){
			results[i]=new float[i+1];
		}
		
		int ii=0;
		int jj=0;
		for (int i=0; i<label.length; i++){
			jj=0;
			if (label[i]==ind){
				for (int j=0; j<i; j++){
					if (label[j]==ind){
						if (h==0){
							results[ii][jj]=scoresEle[i][j];
						}
						else if (h==1){
							results[ii][jj]=scoresEleC[i][j];
						}
						else if (h==2){
							results[ii][jj]=scoresSyll[i][j];
						}
						else if (h==3){
							results[ii][jj]=scoreTrans[i][j];
						}
						else{
							results[ii][jj]=scoresSong[i][j];
						}
						jj++;
					}
				}
				ii++;
			}
		}
		return results;
	}
	
	public int[] splitCounts(int[] label, int h, int ind){
		int c=0;
		for (int i=0; i<label.length; i++){
			if (label[i]==ind){c++;}
		}
		
		int[] results=new int[c];
		
		int ii=0;
		for (int i=0; i<label.length; i++){
			if (label[i]==ind){
				if (h==0){
					results[ii]=eleCounts[i];
				}
				else if (h==1){
					results[ii]=eleCounts[i];
				}
				else if (h==2){
					results[ii]=sylCounts[i];
				}
				ii++;
			}
		}
		return results;
	}
	
	public int[][] splitIndSongs(int[] label, int ind){
		int c=0;
		for (int i=0; i<individualSongs.length; i++){
			int j=individualSongs[i][0];
			if (label[j]==ind){c++;}
		}
		
		int[][] results=new int[c][];
		
		int[] mat=new int[label.length];
		int d=0;
		for (int i=0; i<label.length; i++){
			if (label[i]==ind){
				mat[i]=d;
				d++;
			}
			else{
				mat[i]=-1;
			}
		}
		
		int ii=0;
		for (int i=0; i<individualSongs.length; i++){
			int j=individualSongs[i][0];
			if (label[j]==ind){
				results[ii]=new int[individualSongs[i].length];
				for (int k=0; k<individualSongs[i].length; k++){
					results[ii][k]=mat[individualSongs[i][k]];
				}
				ii++;
			}
		}
		return results;
	}
	
	public int[][] splitLookUps(int[] label, int h, int ind){
		int c=0;
		for (int i=0; i<label.length; i++){
			if (label[i]==ind){c++;}
		}
		
		int[][] results=new int[c][];
		
		int ii=0;
		for (int i=0; i<label.length; i++){
			if (label[i]==ind){
				if (h==0){
					results[ii]=new int[lookUpEls[i].length];
					System.arraycopy(lookUpEls[i], 0, results[ii], 0, lookUpEls[i].length);
				}
				else if (h==1){
					results[ii]=new int[lookUpElsC[i].length];
					System.arraycopy(lookUpElsC[i], 0, results[ii], 0, lookUpElsC[i].length);
				}
				else if (h==2){
					results[ii]=new int[lookUpSyls[i].length];
					System.arraycopy(lookUpSyls[i], 0, results[ii], 0, lookUpSyls[i].length);
				}
				else if (h==3){
					results[ii]=new int[lookUpTrans[i].length];
					System.arraycopy(lookUpTrans[i], 0, results[ii], 0, lookUpTrans[i].length);
				}
				ii++;
			}
		}
		return results;
	}
	
	
	
}
